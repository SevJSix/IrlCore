package me.txmc.gradlepluginbase.game.games;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import me.txmc.gradlepluginbase.Main;
import me.txmc.gradlepluginbase.common.CommonUtils;
import me.txmc.gradlepluginbase.game.MiniGame;
import me.txmc.gradlepluginbase.game.games.skywars.ChestLoadoutType;
import me.txmc.gradlepluginbase.utils.*;
import net.md_5.bungee.api.chat.BaseComponent;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.ItemStack;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;
import org.github.paperspigot.Title;

import java.io.File;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Getter
@Setter
public class SkywarsGame extends MiniGame {

    private final File chestLocationsDataFolder;
    private final File spawnLocationsDataFolder;
    private final List<Location> spawnLocations;

    private Player winner;
    private boolean isWon;

    @SneakyThrows
    public SkywarsGame(World world, Player... participants) {
        super(MinigameType.SKY_WARS, serverWideTeamSetting.getValue(), true, world, participants);
        chestLocationsDataFolder = new File(Main.getInstance().getDataFolder(), "SkywarsChestLocations");
        if (!chestLocationsDataFolder.exists()) chestLocationsDataFolder.mkdirs();
        spawnLocations = new ArrayList<>();

        // parse spawn locations
        spawnLocationsDataFolder = new File(Main.getInstance().getDataFolder(), "SkywarsSpawnLocations");
        if (!spawnLocationsDataFolder.exists()) spawnLocationsDataFolder.mkdirs();
        for (File file : Objects.requireNonNull(spawnLocationsDataFolder.listFiles())) {
            if (file.isFile()) {
                NBTTagCompound compound = LogoutIO.loadNBTSafely(file);
                Location location = Utils.getLocationFromNBT(compound);
                spawnLocations.add(location);
            }
        }
    }

    @SneakyThrows
    @Override
    public void onStart() {
        saveAllLogoutSpots();

        // paste schematic of skywars islands
        broadcast("&aStarting skywars game... please wait while the map generates");
        WorldEditUtils.pasteSchematic(getWorld(), "skywarsmap1", new Location(getWorld(), 0.5, 1.0, 0.5));

        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {

            // initialize chest locations
            Map<Location, ChestLoadoutType> chestMap = new HashMap<>();
            for (File file : Objects.requireNonNull(chestLocationsDataFolder.listFiles())) {
                if (file.isFile()) {
                    NBTTagCompound compound = null;
                    try {
                        compound = LogoutIO.loadNBTSafely(file);
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                    Location location = Utils.getLocationFromNBT(compound);
                    ChestLoadoutType loadoutType = ChestLoadoutType.valueOf(compound.getString("LoadoutType"));
                    chestMap.put(location, loadoutType);
                }
            }
            if (chestMap.size() < 1) {
                broadcast("&cFailed to start SkyWars game. No Chest Locations");
                return;
            }

            setChestData(chestMap, new Random());
            World world = getWorld();
            world.setGameRuleValue("doDaylightCycle", "false");
            world.setTime(6000L);
            world.getEntities().stream().filter(entity -> !(entity instanceof Player)).forEach(Entity::remove);
            if (getParticipants().size() > spawnLocations.size()) {
                broadcast("&cToo many participants and not enough spawn locations");
                return;
            }

            genGlassCages();
            Iterator<Player> iterator = getParticipants().iterator();
            int index = 0;
            while (iterator.hasNext()) {
                Player player = iterator.next();
                Location location = spawnLocations.get(index);
                player.teleport(location);
                index++;
            }
            getParticipants().forEach(this::setPlayerReady);

            new Timer().schedule(new TimerTask() {
                int times = 0;

                @Override
                public void run() {
                    if (times == 5) {
                        Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                            setOngoing(true);
                            destroyGlassCages();
                        });
                        this.cancel();
                    }
                    getParticipants().forEach(player -> {
                        player.sendTitle(new Title(Utils.translateChars(String.format("&e&lStarting in &r&c%s", (5 - times) == 0 ? "NOW" : (5 - times))), "", 0, 10, 10));
                        player.playSound(player.getLocation(), Sound.NOTE_PLING, 10.0f, times == 5 ? 2.0f : 1.0f);
                    });
                    times++;
                }
            }, 1000L, 1000L);

        }, (20 * 8L));
    }

    @Override
    public void onEnd() {
        broadcast(String.format("&3%s &5Won the Skywars Match!", winner != null ? winner.getName() : "Nobody"));
        isWon = true;
        Utils.playSoundToAll(Sound.LEVEL_UP, 10f, 1f);
        resetMinigameData();
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
            Bukkit.getOnlinePlayers().forEach(this::resetPlayerState);
        }, 20 * 8L);
    }

    @Override
    public void onProgress(int currentTick) {

    }

    @Override
    public void onTick(int currentTick) {

    }

    @Override
    public void eliminate(Player player, BaseComponent message) {
        Bukkit.getOnlinePlayers().forEach(onlinePlayer -> onlinePlayer.sendMessage(message));
        removeParticipant(player);
        EntityPlayer ep = CommonUtils.getNMSPlayer(player);
        Utils.playSoundToAll(Sound.ENDERDRAGON_GROWL, 10f, 1f);
        if (ep.locY < 0 && player.getSpectatorTarget() == null)
            player.teleport(new Location(getWorld(), 0.5, 150, 0.5));
        if (getParticipants().size() <= 1) {
            setWinner(getParticipants().size() > 0 ? getParticipants().stream().findFirst().orElse(null) : null);
            onEnd();
        }
    }

    public void genGlassCages() {
        for (Location spawnLocation : spawnLocations) {
            glassLocations(spawnLocation).forEach(location -> location.getBlock().setType(Material.GLASS));
        }
    }

    public void destroyGlassCages() {
        for (Location spawnLocation : spawnLocations) {
            glassLocations(spawnLocation).forEach(location -> location.getBlock().setType(Material.AIR));
        }
    }

    public List<Location> glassLocations(Location location) {
        World world = getWorld();
        List<Location> locations = new ArrayList<>();
        Location standingBlock = world.getBlockAt(location.clone().subtract(0, 1, 0)).getLocation();
        Location roofBlock = world.getBlockAt(location.clone().add(0, 2, 0)).getLocation();
        locations.add(standingBlock);
        locations.add(roofBlock);
        BlockFace[] faces = new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
        for (int i = 0; i < 2; i++) {
            Location l = location.clone().add(0, i, 0);
            for (BlockFace face : faces) {
                locations.add(l.getBlock().getRelative(face).getLocation());
            }
        }
        return locations;
    }

    @Override
    public void setPlayerReady(Player player) {
        if (player.isOnline() && !player.isDead()) {
            player.setGameMode(GameMode.SURVIVAL);
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
            player.setFireTicks(0);
            player.setMaxHealth(40);
            player.setHealth(20);
            player.setFoodLevel(20);
            player.getActivePotionEffects().clear();
        }
    }

    @Override
    public void resetPlayerState(Player player) {
        if (player.isOnline() && !player.isDead()) {
            LogoutSpot logoutSpot = LogoutIO.loadLogoutSpot(player.getUniqueId());
            if (logoutSpot != null) {
                Location location = logoutSpot.getLocation();
                player.teleport(location);
                player.setGameMode(GameMode.SURVIVAL);
                Utils.setInventoryFromNBT(player, logoutSpot.getInventoryDataTag());
                LogoutIO.deleteLogoutSpot(player.getUniqueId());
                player.setFireTicks(0);
                player.setMaxHealth(20);
                player.setHealth(20);
                player.setFoodLevel(20);
                player.getActivePotionEffects().clear();
                Utils.resetDisplayName(player);
            }
        }
    }

    private void setChestData(Map<Location, ChestLoadoutType> chestMap, Random random) {
        Location worldCenter = new Location(getWorld(), 0.5, 0.5, 0.5);
        chestMap.forEach((location, loadoutType) -> {
            if (location.getBlock().getType() == Material.CHEST) {
                Chest chest = (Chest) location.getBlock().getState();
                fillChest(chest, createRandomItemAssortment(random, loadoutType).toArray(new ItemStack[0]));
            }
        });
    }

    private List<ItemStack> createRandomItemAssortment(Random random, ChestLoadoutType loadoutType) {
        List<ItemStack> stacks = new ArrayList<>();
        switch (loadoutType) {
            case SPAWN_ISLAND:
                ItemStack[] armor = selectRandom(ThreadLocalRandom.current().nextInt(1, 3), ChestHashData.armor, random);
                ItemStack[] sword = selectRandom(1, ChestHashData.weapons, random);
                ItemStack[] projectiles = selectRandom(ThreadLocalRandom.current().nextInt(1, 3), ChestHashData.projectiles, random);
                ItemStack[] blocks = selectRandom(ThreadLocalRandom.current().nextInt(1, 3), ChestHashData.blocks, random);
                Collections.addAll(stacks, armor);
                Collections.addAll(stacks, sword);
                Collections.addAll(stacks, projectiles);
                Collections.addAll(stacks, blocks);
                stacks.add(ItemUtils.genStack(Material.COOKED_BEEF, random.nextInt(2) == 0 ? 12 : 8));
                if (random.nextBoolean()) {
                    stacks.add(ItemUtils.genStack(Material.DIAMOND_PICKAXE, 1));
                }
                if (random.nextInt(8) == 0) {
                    Collections.addAll(stacks, ChestHashData.genBowWithArrows());
                }
                break;

            case OUTER_ISLAND:
                ItemStack[] outerIslandProjectiles = selectRandom(random.nextInt(2) == 0 ? 3 : 2, ChestHashData.projectiles, random);
                ItemStack[] outerIslandBlocks = selectRandom(random.nextInt(2) == 0 ? 3 : 2, ChestHashData.blocks, random);
                Collections.addAll(stacks, selectRandom(1, ChestHashData.potions, random));
                Collections.addAll(stacks, outerIslandBlocks);
                Collections.addAll(stacks, outerIslandProjectiles);
                stacks.add(ItemUtils.genStack(Material.COOKED_BEEF, random.nextInt(2) == 0 ? 12 : 8));
                if (random.nextInt(2) == 0) {
                    stacks.add(ItemUtils.genStack(Material.WATER_BUCKET, 1));
                }
                if (random.nextInt(2) == 0) {
                    stacks.add(ItemUtils.genStack(Material.LAVA_BUCKET, 1));
                }
                if (random.nextInt(2) == 0) {
                    stacks.add(ItemUtils.genStack(Material.DIAMOND_PICKAXE, 1));
                }
                if (random.nextInt(3) == 0) {
                    stacks.add(CraftItemStack.asNMSCopy(ItemUtils.genVelocityPearl(1)));
                }
                if (random.nextInt(4) == 0) {
                    stacks.add(ItemUtils.genStack(Material.ENDER_PEARL, 2));
                }
                if (random.nextInt(4) == 0) {
                    stacks.add(CraftItemStack.asNMSCopy(ItemUtils.genZombieAxe()));
                }
                if (random.nextInt(5) == 0) {
                    stacks.add(CraftItemStack.asNMSCopy(ItemUtils.genZombieHelmet()));
                }
                if (random.nextInt(8) == 0) {
                    stacks.add(CraftItemStack.asNMSCopy(ItemUtils.genGrenade(6)));
                }
                if (random.nextInt(10) == 0) {
                    stacks.add(ItemUtils.genStack(Material.GOLDEN_APPLE, 2));
                }
                if (random.nextInt(15) == 0) {
                    stacks.add(CraftItemStack.asNMSCopy(ItemUtils.genExplosiveBow()));
                    stacks.add(ItemUtils.genStack(Material.ARROW, 8));
                }
                break;

            case CENTER_ISLAND:
                org.bukkit.inventory.ItemStack pieceOfArmor = CraftItemStack.asBukkitCopy(ChestHashData.diamondArmor[random.nextInt(ChestHashData.diamondArmor.length)]);
                ItemUtils.enchant(pieceOfArmor, ChestHashData.enchantments.get(random.nextInt(ChestHashData.enchantments.size())), random.nextInt(2) == 0 ? 4 : 3);
                stacks.add(CraftItemStack.asNMSCopy(pieceOfArmor));
                Collections.addAll(stacks, selectRandom(random.nextInt(2) == 0 ? 2 : 1, ChestHashData.potions, random));
                Collections.addAll(stacks, selectRandom(ThreadLocalRandom.current().nextInt(1, 3), ChestHashData.projectiles, random));
                stacks.add(ItemUtils.genStack(Material.GOLDEN_APPLE, random.nextInt(2) == 0 ? 4 : 2));
                if (random.nextInt(2) == 0) {
                    stacks.add(ItemUtils.genStack(Material.TNT, 10));
                }
                if (random.nextInt(3) == 0) {
                    org.bukkit.inventory.ItemStack diamondSword = CraftItemStack.asBukkitCopy(ItemUtils.genStack(Material.DIAMOND_SWORD));
                    ItemUtils.enchant(diamondSword, Enchantment.DAMAGE_ALL, 1);
                    stacks.add(CraftItemStack.asNMSCopy(diamondSword));
                }
                if (random.nextInt(3) == 0) {
                    org.bukkit.inventory.ItemStack arkMouse = ItemUtils.genArkMouse();
                    arkMouse.setAmount(4);
                    stacks.add(CraftItemStack.asNMSCopy(arkMouse));
                }
                if (random.nextInt(4) == 0) {
                    stacks.add(ItemUtils.genStack(Material.ENDER_PEARL, 2));
                }
                if (random.nextInt(4) == 0) {
                    stacks.add(CraftItemStack.asNMSCopy(ItemUtils.genGrenade(4)));
                }
                if (random.nextInt(5) == 0) {
                    stacks.add(CraftItemStack.asNMSCopy(ItemUtils.genExplosiveBow()));
                    stacks.add(ItemUtils.genStack(Material.ARROW, 8));
                }
                if (random.nextInt(20) == 0) {
                    stacks.add(CraftItemStack.asNMSCopy(ItemUtils.genRodolfoBook()));
                }
                break;
        }
        return stacks;
    }

    private ItemStack[] selectRandom(int amount, ItemStack[] itemStacks, Random random) {
        ItemStack[] arrayOfItemStacks = new ItemStack[amount];
        for (int i = 0; i < arrayOfItemStacks.length; i++) {
            ItemStack item = null;
            while (item == null) {
                item = itemStacks[random.nextInt(itemStacks.length)];
                ItemStack finalItem = item;
                String item_name = finalItem.getName();
                boolean isArmor = Arrays.stream(ChestHashData.armor).anyMatch(itemStack -> itemStack.getName().equalsIgnoreCase(item_name));
                if (Arrays.asList(arrayOfItemStacks).contains(finalItem)) {
                    item = null;
                }
                if (isArmor) {
                    for (ItemStack arrayOfItemStack : arrayOfItemStacks) {
                        if (arrayOfItemStack != null) {
                            if (arrayOfItemStack.getName().substring(arrayOfItemStack.getName().indexOf(" ")).equalsIgnoreCase(item_name.substring(item_name.indexOf(" ")))) {
                                item = null;
                            }
                        }
                    }
                }
            }
            arrayOfItemStacks[i] = item;
        }
        return arrayOfItemStacks;
    }

    private void fillChest(Chest chest, ItemStack[] items) {
        chest.getInventory().clear();
        int size = chest.getBlockInventory().getSize();
        for (ItemStack item : items) {
            int index = -1;
            int attempts = 0;
            while (index == -1) {
                if (attempts > 5000) break;
                index = ThreadLocalRandom.current().nextInt(0, chest.getInventory().getSize());
                if (chest.getInventory().getItem(index) != null) {
                    index = -1;
                }
                attempts++;
            }
            if (index != -1) {
                chest.getInventory().setItem(index, CraftItemStack.asBukkitCopy(item));
            }
        }
    }

    public static class ChestHashData {

        public static ItemStack[] armor = new ItemStack[]{
                ItemUtils.genStack(Material.IRON_HELMET),
                ItemUtils.genStack(Material.IRON_CHESTPLATE),
                ItemUtils.genStack(Material.IRON_LEGGINGS),
                ItemUtils.genStack(Material.IRON_BOOTS),
                ItemUtils.genStack(Material.DIAMOND_HELMET),
                ItemUtils.genStack(Material.DIAMOND_CHESTPLATE),
                ItemUtils.genStack(Material.DIAMOND_LEGGINGS),
                ItemUtils.genStack(Material.DIAMOND_BOOTS),
                ItemUtils.genStack(Material.GOLD_HELMET),
                ItemUtils.genStack(Material.GOLD_CHESTPLATE),
                ItemUtils.genStack(Material.GOLD_LEGGINGS),
                ItemUtils.genStack(Material.GOLD_BOOTS),
                ItemUtils.genStack(Material.CHAINMAIL_HELMET),
                ItemUtils.genStack(Material.CHAINMAIL_CHESTPLATE),
                ItemUtils.genStack(Material.CHAINMAIL_LEGGINGS),
                ItemUtils.genStack(Material.CHAINMAIL_BOOTS)
        };

        public static ItemStack[] weapons = new ItemStack[]{
                ItemUtils.genEnchantedStack(Material.STONE_SWORD, 1, Enchantment.DAMAGE_ALL, 1),
                ItemUtils.genStack(Material.DIAMOND_SWORD),
                ItemUtils.genEnchantedStack(Material.FISHING_ROD, 1, Enchantment.DURABILITY, 1),
                ItemUtils.genStack(Material.IRON_AXE),
                ItemUtils.genEnchantedStack(Material.WOOD_SWORD, 1, Enchantment.FIRE_ASPECT, 2),
                ItemUtils.genEnchantedStack(Material.STICK, 1, Enchantment.KNOCKBACK, 3)
        };

        public static ItemStack[] blocks = new ItemStack[]{
                ItemUtils.genStack(Material.WOOD, 16),
                ItemUtils.genStack(Material.WOOD, 32),
                ItemUtils.genStack(Material.STONE, 16),
                ItemUtils.genStack(Material.STONE, 32),
        };

        public static ItemStack[] projectiles = new ItemStack[]{
                ItemUtils.genStack(Material.EGG, 16),
                ItemUtils.genStack(Material.EGG, 8),
                ItemUtils.genStack(Material.SNOW_BALL, 16),
                ItemUtils.genStack(Material.SNOW_BALL, 8),
        };
        public static ItemStack[] potions = new ItemStack[]{
                ItemUtils.genPotion(PotionType.SPEED, Potion.Tier.TWO, true),
                ItemUtils.genPotion(PotionType.STRENGTH, Potion.Tier.ONE, false),
                ItemUtils.genPotion(PotionType.FIRE_RESISTANCE, Potion.Tier.ONE, false),
                ItemUtils.genPotion(PotionType.INSTANT_HEAL, Potion.Tier.TWO, true),
                ItemUtils.genPotion(PotionType.WEAKNESS, Potion.Tier.ONE, true),
                ItemUtils.genPotion(PotionType.REGEN, Potion.Tier.TWO, false)
        };
        public static List<Enchantment> enchantments;
        public static ItemStack[] diamondArmor = new ItemStack[]{
                ItemUtils.genStack(Material.DIAMOND_HELMET),
                ItemUtils.genStack(Material.DIAMOND_CHESTPLATE),
                ItemUtils.genStack(Material.DIAMOND_LEGGINGS),
                ItemUtils.genStack(Material.DIAMOND_BOOTS)
        };

        static {
            enchantments = new ArrayList<>();
            enchantments.add(Enchantment.PROTECTION_FIRE);
            enchantments.add(Enchantment.PROTECTION_ENVIRONMENTAL);
            enchantments.add(Enchantment.PROTECTION_EXPLOSIONS);
            enchantments.add(Enchantment.PROTECTION_PROJECTILE);
        }

        public static ItemStack[] genBowWithArrows() {
            ItemStack[] itemStacks = new ItemStack[2];
            int level = ThreadLocalRandom.current().nextInt(1, 3);
            itemStacks[0] = ItemUtils.genEnchantedStack(Material.BOW, 1, Enchantment.ARROW_DAMAGE, level);
            itemStacks[1] = ItemUtils.genStack(Material.ARROW, level == 1 ? 20 : level == 2 ? 15 : level == 3 ? 10 : 1);
            return itemStacks;
        }
    }
}
