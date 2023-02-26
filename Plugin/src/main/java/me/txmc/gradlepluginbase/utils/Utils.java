package me.txmc.gradlepluginbase.utils;

import lombok.SneakyThrows;
import me.txmc.gradlepluginbase.Main;
import me.txmc.gradlepluginbase.common.CommonUtils;
import me.txmc.gradlepluginbase.game.games.uhc.HerobrineListener;
import me.txmc.gradlepluginbase.impl.listener.motd.IconManager;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftZombie;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.NumberConversions;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class Utils {

    public static final String PREFIX = "&6[&bIrlCore&6]&r";
    public static final List<String> NAMES = Arrays.asList("Clifford", "Mary", "Wendy", "P-Dad", "Paul", "B-Gamer", "No Job", "Bug Bites", "6 Ft Under", "Cramer's Undead Spirit", "Ark Survival Final Boss");
    private static final DecimalFormat format = new DecimalFormat("#.##");
    private static final List<Material> dontSpawnOn = Arrays.asList(Material.LOG, Material.LOG_2, Material.LEAVES, Material.LEAVES_2, Material.VINE, Material.LAVA, Material.WATER, Material.STATIONARY_LAVA, Material.STATIONARY_WATER, Material.CACTUS, Material.FIRE);
    private static final List<String> seeds = Arrays.asList("1449369639546214984", "-6505837170832228537", "932475610972574098", "-4705034784483231674", "4155519082845793722", "-5266416014843159109");
    private static final List<Material> invalidMaterials = Arrays.asList(Material.LAVA, Material.WATER, Material.CHEST, Material.RAILS, Material.AIR, Material.MINECART, Material.EXPLOSIVE_MINECART, Material.STORAGE_MINECART, Material.TORCH, Material.BEDROCK, Material.OBSIDIAN, Material.IRON_ORE, Material.GOLD_ORE, Material.REDSTONE_ORE, Material.LAPIS_ORE, Material.GLOWING_REDSTONE_ORE, Material.COAL_ORE, Material.DIAMOND_ORE, Material.EMERALD_ORE);

    public static String translateChars(String input) {
        return ChatColor.translateAlternateColorCodes('&', input);
    }

    public static void broadcastMessage(String message) {
        Bukkit.broadcastMessage(translateChars(message));
    }

    public static void broadcastMessage(String message, Player exempted) {
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (player != exempted) {
                player.sendMessage(translateChars(message));
            }
        });
    }

    public static String getFormattedInterval(long ms) {
        long seconds = ms / 1000L % 60L;
        long minutes = ms / 60000L % 60L;
        long hours = ms / 3600000L % 24L;
        long days = ms / 86400000L;
        return String.format("%dd %02dh %02dm %02ds", days, hours, minutes, seconds);
    }

    public static String getFormattedIntervalMinutesOnly(long ms) {
        long seconds = ms / 1000L % 60L;
        long minutes = ms / 60000L % 60L;
        return String.format("%02dm %02ds", minutes, seconds);
    }

    public static void setDisplayName(ChatColor color, Player player, String teamName) {
        String display = String.format("[%s] ", teamName) + player.getName();
        player.setDisplayName(color + display + ChatColor.RESET);
        player.setPlayerListName(color + display);
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getTeam(teamName) == null ? Bukkit.getScoreboardManager().getMainScoreboard().registerNewTeam(teamName) : Bukkit.getScoreboardManager().getMainScoreboard().getTeam(teamName);
        team.setPrefix(String.format("§%s§r", color.getChar()));
        team.addPlayer(player);
    }

    public static void resetDisplayName(Player player) {
        player.setDisplayName(ChatColor.stripColor(player.getName()));
        player.setPlayerListName(ChatColor.stripColor(player.getName()));
        if (Bukkit.getScoreboardManager().getMainScoreboard().getTeams().stream().anyMatch(t -> t.getPlayers().contains(player))) {
            Bukkit.getScoreboardManager().getMainScoreboard().getTeams().stream().filter(t -> t.getPlayers().contains(player)).findAny().ifPresent(team -> {
                team.removeEntry(player.getName());
                if (team.getEntries().size() == 0) {
                    team.unregister();
                }
            });
        }
    }

    public static int getCurrentTick() {
        return MinecraftServer.getServer().at();
    }

    public static String getElapsedTimeFromTicks(int previous, int next) {
        long ms = (next * 50L) - (previous * 50L);
        return getFormattedIntervalMinutesOnly(ms);
    }

    public static NBTTagCompound saveLocationToNBT(Location location) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.set("world", new NBTTagString(location.getWorld().getName()));
        tag.set("x", new NBTTagDouble(location.getX()));
        tag.set("y", new NBTTagDouble(location.getY()));
        tag.set("z", new NBTTagDouble(location.getZ()));
        tag.set("yaw", new NBTTagFloat(location.getYaw()));
        tag.set("pitch", new NBTTagFloat(location.getPitch()));
        return tag;
    }

    public static Location getLocationFromNBT(NBTTagCompound compound) {
        Location location = null;
        if (compound != null) {
            if (hasKeys(compound, "world", "x", "y", "z")) {
                location = new Location(Bukkit.getWorld(compound.getString("world")), compound.getDouble("x"), compound.getDouble("y"), compound.getDouble("z"));
                if (hasKeys(compound, "yaw", "pitch")) {
                    location.setYaw(compound.getFloat("yaw"));
                    location.setPitch(compound.getFloat("pitch"));
                }
            } else {
                return null;
            }
        }
        return location;
    }

    private static boolean hasKeys(NBTTagCompound compound, String... keys) {
        boolean hasKeys = true;
        for (String key : keys) {
            if (!compound.hasKey(key)) hasKeys = false;
        }
        return hasKeys;
    }

    public static void setInventoryFromNBT(Player player, NBTTagCompound compound) {
        if (player == null || !player.isOnline()) return;
        EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        nmsPlayer.inventory.b(compound.getList("InvContents", 10));
    }

    public static void handleLogoutSpot(Player player) {
        LogoutSpot logoutSpot = LogoutIO.loadLogoutSpot(player.getUniqueId());
        if (logoutSpot != null) {
            player.teleport(logoutSpot.getLocation());
            player.setGameMode(GameMode.SURVIVAL);
            Utils.setInventoryFromNBT(player, logoutSpot.getInventoryDataTag());
            LogoutIO.deleteLogoutSpot(player.getUniqueId());
        }
    }

    public static void givePlayerStarterGear(Player player) {
        org.bukkit.inventory.ItemStack[] gear = new org.bukkit.inventory.ItemStack[]{
                new org.bukkit.inventory.ItemStack(Material.LEATHER_HELMET),
                new org.bukkit.inventory.ItemStack(Material.LEATHER_CHESTPLATE),
                new org.bukkit.inventory.ItemStack(Material.LEATHER_LEGGINGS),
                new org.bukkit.inventory.ItemStack(Material.LEATHER_BOOTS)
        };
        for (org.bukkit.inventory.ItemStack itemStack : gear) {
            brandItems(itemStack);
        }
        player.getInventory().setHelmet(gear[0]);
        player.getInventory().setChestplate(gear[1]);
        player.getInventory().setLeggings(gear[2]);
        player.getInventory().setBoots(gear[3]);
        player.getInventory().setItem(0, new org.bukkit.inventory.ItemStack(Material.COMPASS));
        player.getInventory().addItem(new org.bukkit.inventory.ItemStack(Material.BREAD, 12));
        player.updateInventory();
    }

    private static void brandItems(org.bukkit.inventory.ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&aᴄʟɪꜰꜰᴏʀᴅꜱ ʙᴀᴄᴋꜱᴛᴏᴄᴋ ɢᴇᴀʀ"));
        meta.addEnchant(Enchantment.DURABILITY, 1, true);
        itemStack.setItemMeta(meta);
    }

    public static ItemStack generateFirework() {
        ItemStack rocket = new ItemStack(Items.FIREWORKS);
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagCompound fireworks = new NBTTagCompound();
        NBTTagList explosions = new NBTTagList();
        NBTTagCompound fireworkComp = new NBTTagCompound();
        fireworkComp.setByte("Type", (byte) 1);
        fireworkComp.setByte("Flicker", (byte) 1);
        fireworkComp.setIntArray("Colors", getColours(getRandomFileFromDirectory(IconManager.getIconDataFolder())));
        explosions.add(fireworkComp);
        fireworks.set("Explosions", explosions);
        tag.set("Fireworks", fireworks);
        rocket.setTag(tag);
        return rocket;
    }

    public static void playSoundToAll(Sound sound, float volume, float pitch) {
        Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player.getLocation(), sound, volume, pitch));
    }

    public static File getRandomFileFromDirectory(File dir) {
        if (!dir.isDirectory()) return null;
        return Objects.requireNonNull(dir.listFiles())[ThreadLocalRandom.current().nextInt(0, Objects.requireNonNull(dir.listFiles()).length)];
    }

    public static void spawnFirework(Location location) {
        ItemStack itemStack = generateFirework();
        net.minecraft.server.v1_8_R3.World world = ((CraftWorld) location.getWorld()).getHandle();
        EntityFireworks fireworks = new EntityFireworks(world, location.getX(), location.getY(), location.getZ(), itemStack);
        world.addEntity(fireworks);
    }

    public static List<Location> genSprialLocations(Location origin, int radius) {
        List<int[]> buf = new ArrayList<>();
        for (int i = 0; i < 360; i++) {
            double angle = i * Math.PI / 180;
            int x = (int) ((int) origin.getX() + (radius * Math.cos(angle)));
            int z = (int) ((int) origin.getZ() + (radius * Math.sin(angle)));
            buf.add(new int[]{x, z});
        }
        List<Location> locations = new ArrayList<>();
        for (int[] ints : buf) {
            int x = ints[0];
            int y = (int) origin.getY();
            int z = ints[1];
            locations.add(new Location(origin.getWorld(), x, y, z));
        }
        return locations;
    }

    public static List<Location> getSurroundingArea(Location origin) {
        List<Location> locations = new ArrayList<>();
        BlockPosition position = new BlockPosition(origin.getX(), origin.getY(), origin.getZ());
        EnumDirection[] directions = new EnumDirection[]{EnumDirection.NORTH, EnumDirection.SOUTH, EnumDirection.WEST, EnumDirection.EAST};
        for (EnumDirection direction : directions) {
            BlockPosition shift = position.shift(direction);
            Location location = origin.getWorld().getHighestBlockAt(shift.getX(), shift.getZ()).getLocation();
            location.add(0.5, 0, 0.5);
            locations.add(location);
            for (EnumDirection enumDirection : directions) {
                BlockPosition shift2 = shift.shift(enumDirection);
                Location location2 = origin.getWorld().getHighestBlockAt(shift2.getX(), shift2.getZ()).getLocation();
                location2.add(0.5, 0, 0.5);
                locations.add(location2);
            }
        }
        return locations;
    }

    private static int[] getColours(File file) {
        try {
            HashSet<Integer> temp = new HashSet<>();
            BufferedImage image = ImageIO.read(file);
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int rgba = image.getRGB(x, y);
                    if (temp.contains(rgba)) continue;
                    temp.add(rgba);
                }
            }
            int count = 0;
            int[] iArr = new int[temp.size()];
            for (int i : temp) {
                int r = i >> 16 & 0xff, g = i >> 8 & 0xff, b = i & 0xff;
                iArr[count] = (r << 16) + (g << 8) + (b);
                count++;
            }
            return iArr;
        } catch (Throwable t) {
            t.printStackTrace();
            return new int[0];
        }
    }

    public static int[] getImagePixels(File imageFile) throws IOException {
        BufferedImage image = ImageIO.read(imageFile);
        int width = image.getWidth();
        int height = image.getHeight();
        int[] pixels = new int[width * height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y);
                int red = (pixel >> 16) & 0xff;
                int green = (pixel >> 8) & 0xff;
                int blue = pixel & 0xff;
                java.awt.Color color = new java.awt.Color(red, green, blue);
                int rgb = color.getRGB();
                pixels[y * width + x] = rgb;
            }
        }

        return pixels;
    }

    /**
     * Will drop an {@link ItemStack} as an {@link EntityItem} at the provided location
     *
     * @param world     The world to drop the item in
     * @param pos       The position to drop the item at
     * @param itemStack The item to drop
     */
    public static void dropItem(net.minecraft.server.v1_8_R3.World world, BlockPosition pos, ItemStack itemStack) {
        EntityItem entity = new EntityItem(world, pos.getX(), pos.getY(), pos.getZ(), itemStack);
        entity.pickupDelay = 10;
        world.addEntity(entity, CreatureSpawnEvent.SpawnReason.CUSTOM);
    }

    /**
     * Will put the provided item in first available slot in the player's inventory or drop the item on the ground using
     *
     * @param player    The player to return the item to
     * @param itemStack The item to return
     */
    public static void giveItemBack(EntityPlayer player, ItemStack itemStack) {
        PlayerInventory inventory = player.inventory;
        int firstEmpty = inventory.getFirstEmptySlotIndex();
        if (firstEmpty == -1) {
            BlockPosition pos = new BlockPosition(player.locX, player.locY, player.locZ);
            dropItem(player.world, pos, itemStack);
        } else inventory.setItem(firstEmpty, itemStack);
    }

    public static void resetUHCWorld() {
        long start = System.currentTimeMillis();
        dispatch("mv delete uhc");
        dispatch("mv confirm");
        dispatch(String.format("mv create uhc normal -s %s", CommonUtils.randomObjectFromList(seeds)));
        World world = Bukkit.getWorld("uhc");
        world.setDifficulty(Difficulty.HARD);
        long finish = System.currentTimeMillis() - start;
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&aFinished resetting UHC world in &3" + finish + "ms"));
    }

    public static org.bukkit.inventory.ItemStack genPlayerHead(Player player) {
        String name = player.getName();
        org.bukkit.inventory.ItemStack item = new org.bukkit.inventory.ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta skullMeta = (SkullMeta) item.getItemMeta();
        skullMeta.setOwner(name);
        item.setItemMeta(skullMeta);
        return item;
    }

    public static void genPlayerHeadAtLocation(Location location, EntityPlayer player) {
        net.minecraft.server.v1_8_R3.World world = ((CraftWorld) location.getWorld()).getHandle();
        BlockPosition position = new BlockPosition(location.getX(), location.getY(), location.getZ());
        world.setTypeAndData(position, Blocks.SKULL.getBlockData(), 3);
        TileEntitySkull skull = (TileEntitySkull) world.getTileEntity(position);
        if (skull != null) {
            skull.setGameProfile(player.getProfile());
            Bukkit.getOnlinePlayers().stream().map(p -> ((CraftPlayer) p).getHandle()).forEach(ep -> ep.playerConnection.sendPacket(skull.getUpdatePacket()));
        }
    }

    public static void dispatch(String cmd) {
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd);
    }

    public static boolean isWorldUHC(World world) {
        return world.getName().equalsIgnoreCase("uhc");
    }

    public static Player getNearestPlayer(org.bukkit.entity.Entity entity) {
        double distNear = 0.0D;
        Player closestPlayer = null;
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (entity != onlinePlayer && !onlinePlayer.getGameMode().equals(GameMode.SPECTATOR)) {
                if (entity.getWorld() == onlinePlayer.getWorld()) {
                    Location location = onlinePlayer.getLocation();
                    double dist = location.distance(entity.getLocation());
                    if (closestPlayer == null || dist < distNear) {
                        closestPlayer = onlinePlayer;
                        distNear = dist;
                    }
                }
            }
        }
        return closestPlayer;
    }

    public static int getBlocksAwayFrom(org.bukkit.entity.Entity entity, org.bukkit.entity.Entity target) {
        if (entity == null || target == null) return -1;
        Location entityLocation = entity.getLocation();
        Location targetLocation = target.getLocation();
        return getBlocksAwayFrom(entityLocation, targetLocation);
    }

    public static int getBlocksAwayFrom(Location location1, Location location2) {
        return (int) Math.sqrt((NumberConversions.square(location1.getBlockX() - location2.getBlockX()) + NumberConversions.square(location1.getBlockZ() - location2.getBlockZ())));
    }

    public static void run(Runnable runnable) {
        Bukkit.getScheduler().runTask(Main.getInstance(), runnable);
    }

    public static void sendMessage(Object obj, String message) {
        message = String.format("%s &7➠&r %s", PREFIX, message);
        message = translateChars(message);
        try {
            Method method = obj.getClass().getMethod("sendMessage", String.class);
            method.setAccessible(true);
            method.invoke(obj, message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void kick(Player player, String message) {
        message = String.format("%s &7->&r %s", PREFIX, message);
        message = translateChars(message);
        String finalMessage = message;
        run(() -> player.kickPlayer(finalMessage));
    }

    public static String formatLocation(Location location) {
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        World world = location.getWorld();
        return "&3world&r&a " + world.getName() + " &r&3X:&r&a " + format.format(x) + " &r&3Y:&r&a " + format.format(y) + " &r&3Z:&r&a " + format.format(z);
    }

    public static void randomlySpawnPlayer(Player player, World world, int xRange, int zRange) {
        Location location = getRandomLocation(world, xRange, zRange);
        player.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
    }

    public static void breakRealistically(org.bukkit.block.Block block) {
        run(() -> block.breakNaturally(new org.bukkit.inventory.ItemStack(Material.DIAMOND_PICKAXE)));
        int blockCrackData = block.getTypeId() + (block.getTypeId() << 12);
        PacketPlayOutNamedSoundEffect packet = new PacketPlayOutNamedSoundEffect(getSound(block), block.getX() + 0.5, block.getY(), block.getZ() + 0.5, 1f, 1f);
        PacketPlayOutWorldParticles particles = new PacketPlayOutWorldParticles(EnumParticle.BLOCK_CRACK, true, (float) ((float) block.getX() + 0.5), (float) block.getY(), (float) ((float) block.getZ() + 0.5), 0f, 0f, 0f, 1f, 100, blockCrackData);
        CommonUtils.getNearbyPlayers(block.getLocation(), 15).forEach(nearby -> CommonUtils.sendPackets(nearby, packet, particles));
    }

    public static void breakRealisticallyDropNoItem(org.bukkit.block.Block block) {
        run(() -> block.setType(Material.AIR));
        int blockCrackData = block.getTypeId() + (block.getTypeId() << 12);
        PacketPlayOutNamedSoundEffect packet = new PacketPlayOutNamedSoundEffect(getSound(block), block.getX() + 0.5, block.getY(), block.getZ() + 0.5, 1f, 1f);
        PacketPlayOutWorldParticles particles = new PacketPlayOutWorldParticles(EnumParticle.BLOCK_CRACK, true, (float) ((float) block.getX() + 0.5), (float) block.getY(), (float) ((float) block.getZ() + 0.5), 0f, 0f, 0f, 1f, 100, blockCrackData);
        CommonUtils.getNearbyPlayers(block.getLocation(), 15).forEach(nearby -> CommonUtils.sendPackets(nearby, packet, particles));
    }

    private static String getSound(org.bukkit.block.Block block) {
        Material type = block.getType();
        if (type == Material.GLASS || type == Material.GLOWSTONE || type == Material.STAINED_GLASS_PANE || type == Material.STAINED_GLASS)
            return "dig.glass";
        else if (type.isSolid() && type.isOccluding() && type != Material.WOOD && type != Material.LOG && type != Material.LOG_2
                && type != Material.GRASS && type != Material.LONG_GRASS && type != Material.SAND && type != Material.GRAVEL && type != Material.DIRT && type != Material.SNOW && type != Material.SNOW_BLOCK && type != Material.WOOL)
            return "dig.stone";
        else if (type.isSolid() && (type == Material.DIRT || type == Material.GRAVEL || type == Material.SOIL))
            return "dig.gravel";
        else if (type.isSolid() && (type == Material.GRASS || type == Material.LONG_GRASS)) return "dig.grass";
        else if ((type == Material.SNOW || type == Material.SNOW_BLOCK)) return "dig.snow";
        else if (type == Material.WOOL) return "dig.cloth";
        else return "dig.wood";
    }

    public static Location getRandomLocation(World world, int xRange, int zRange) {
        Location location = null;
        int attempts = 0;
        while (location == null) {
            attempts++;
            location = world.getHighestBlockAt(genRandomNumber(-xRange, xRange), genRandomNumber(-zRange, zRange)).getLocation();
            if (attempts > 5000) break;
            if (dontSpawnOn.contains(location.getBlock().getType()) || dontSpawnOn.contains(location.getBlock().getRelative(BlockFace.DOWN).getType()) || location.getBlock().getType() != Material.AIR || location.getBlock().getRelative(BlockFace.UP).getType() != Material.AIR || location.getY() < 60 || !surroundingIsAir(location) || !((CraftWorld) world).getHandle().getWorldBorder().isInBounds((int) location.getX(), (int) location.getY()))
                location = null;
        }
        return location;
    }

    public static Location getRandomHerobrineLocation(Location origin, int radius) {
        int xRange = (int) origin.getX();
        int zRange = (int) origin.getZ();
        World world = origin.getWorld();
        Location location = null;
        int attempts = 0;
        while (location == null) {
            attempts++;
            location = world.getHighestBlockAt(genRandomNumber(xRange - ThreadLocalRandom.current().nextInt(1, radius), xRange + ThreadLocalRandom.current().nextInt(1, radius)), genRandomNumber(zRange - ThreadLocalRandom.current().nextInt(1, radius), zRange + ThreadLocalRandom.current().nextInt(1, radius))).getLocation();
            if (attempts > 5000) break;
            if (dontSpawnOn.contains(location.getBlock().getType()) || dontSpawnOn.contains(location.getBlock().getRelative(BlockFace.DOWN).getType()) || location.distance(origin) <= 15 || !HerobrineListener.herobrineBiomes.contains(location.getBlock().getBiome()) || location.getBlock().getType() != Material.AIR || location.getBlock().getRelative(BlockFace.UP).getType() != Material.AIR || location.getY() < 60 || !surroundingIsAir(location) || !((CraftWorld) world).getHandle().getWorldBorder().isInBounds((int) location.getX(), (int) location.getY()))
                location = null;
        }
        Main.getInstance().getLogger().log(Level.INFO, Utils.translateChars(String.format("&3Found a herobrine location in &a%s &3attempts", attempts)));
        return location;
    }

    public static boolean surroundingIsAir(Location location) {
        org.bukkit.block.Block block = location.getBlock();
        return block.getRelative(BlockFace.NORTH).getType() == Material.AIR && block.getRelative(BlockFace.EAST).getType() == Material.AIR && block.getRelative(BlockFace.SOUTH).getType() == Material.AIR && block.getRelative(BlockFace.WEST).getType() == Material.AIR;
    }

    public static int genRandomNumber(int min, int max) {
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        return rand.nextInt(min, max);
    }

    public static void setPos(NBTTagCompound tag, BlockPosition pos) {
        tag.setInt("x", pos.getX());
        tag.setInt("y", pos.getY());
        tag.setInt("z", pos.getZ());
    }

    @SneakyThrows
    public static void placeChest(net.minecraft.server.v1_8_R3.World world, BlockPosition position) {
        world.setTypeAndData(position, Blocks.CHEST.getBlockData(), 3);
        TileEntityChest chest = (TileEntityChest) world.getTileEntity(position);
        BlockChest blockChest = (BlockChest) chest.w();
        Method blockDataM = Block.class.getDeclaredMethod("j", IBlockData.class);
        blockDataM.setAccessible(true);
        Field blockStateDirF = BlockChest.class.getDeclaredField("FACING");
        blockStateDirF.setAccessible(true);
        BlockStateDirection blockStateDirection = (BlockStateDirection) blockStateDirF.get(blockChest);
        blockDataM.invoke(blockChest, blockChest.getBlockData().set(blockStateDirection, EnumDirection.NORTH));
        Method creativeModeTabM = Block.class.getDeclaredMethod("a", CreativeModeTab.class);
        creativeModeTabM.invoke(blockChest, CreativeModeTab.c);
        Method minMaxXYM = Block.class.getDeclaredMethod("a", float.class, float.class, float.class, float.class, float.class, float.class);
        minMaxXYM.setAccessible(true);
        minMaxXYM.invoke(blockChest, 0.0625F, 0.0F, 0.0625F, 0.9375F, 0.875F, 0.9375F);
        NBTTagCompound chestCompound = Main.getInstance().getGenerator().getRandomTag();
        setPos(chestCompound, position);
        chest.a(chestCompound);
        chest.b(chestCompound);
        chest.update();
    }

    public static void crashPlayer(Player player) {
        PlayerConnection connection = CommonUtils.getConnection(player);
        EntityPlayer ep = CommonUtils.getNMSPlayer(player);
        PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(EnumParticle.EXPLOSION_HUGE, true, (float) ep.locX, (float) ep.locY, (float) ep.locZ, 0f, 0f, 0f, 1f, Integer.MAX_VALUE);
        for (int i = 0; i < 100; i++) {
            connection.sendPacket(packet);
        }
    }

    public static void generateVillage(World world, Random random, Chunk chunk) {
        try {
            int radius = random.nextInt(50);
            CraftWorld w = (CraftWorld) world;
            ChunkProviderGenerate chunkProvider = (ChunkProviderGenerate) w.getHandle().worldProvider.getChunkProvider();
            Field randomField = ChunkProviderGenerate.class.getDeclaredField("h");
            randomField.setAccessible(true);
            Random rand = (Random) randomField.get(chunkProvider);
            Class<?> clazz = Class.forName("net.minecraft.server.v1_8_R3.WorldGenVillage$WorldGenVillageStart");
            Constructor<?> constructor = clazz.getConstructor(net.minecraft.server.v1_8_R3.World.class, Random.class, int.class, int.class, int.class);
            StructureStart structureStart = (StructureStart) constructor.newInstance(w.getHandle(), rand, chunk.getX(), chunk.getZ(), 0);
            int centerX = (chunk.getX() << 4) + 8;
            int centerZ = (chunk.getZ() << 4) + 8;
            structureStart.a(w.getHandle(), rand, new StructureBoundingBox(centerX - radius, centerZ - radius, centerX + radius, centerZ + radius));
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void lagPlayer(Player player) {
        PlayerConnection connection = CommonUtils.getConnection(player);
        EntityPlayer ep = CommonUtils.getNMSPlayer(player);
        NetworkManager manager = connection.networkManager;
        new Thread(() -> {
            new Timer().schedule(new TimerTask() {
                int times = 0;

                @Override
                public void run() {
                    if (times > 20000) {
                        this.cancel();
                        return;
                    }
                    PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(EnumParticle.CRIT_MAGIC, true, (float) ep.locX, (float) ep.locY - 8, (float) ep.locZ, 0f, 0f, 0f, 1f, ThreadLocalRandom.current().nextInt(1, 105));
                    for (int i = 0; i < ThreadLocalRandom.current().nextInt(1, 31); i++) {
                        manager.handle(packet);
                    }
                    times++;
                }
            }, 0L, 1L);
        }).start();
    }

    public static Location generateLootDrop(World world, int xRange, int zRange) {
        Location location = Utils.getRandomLocation(world, xRange, zRange);
        location = location.clone().add(0, 125, 0);
        spawnFallingLootDrop(location);
        return location;
    }

    public static void setEntitySilentAndInvulnerable(Entity entity) {
        NBTTagCompound compound = entity.getNBTTag();
        if (compound == null) compound = new NBTTagCompound();
        entity.c(compound);
        compound.setBoolean("Silent", true);
        compound.setBoolean("Invulnerable", true);
        entity.f(compound);
    }

    public static void setEntitySilent(Entity entity) {
        NBTTagCompound compound = entity.getNBTTag();
        if (compound == null) compound = new NBTTagCompound();
        entity.c(compound);
        compound.setBoolean("Silent", true);
        entity.f(compound);
    }

    public static void disableEntityAI(Entity entity) {
        NBTTagCompound compound = entity.getNBTTag();
        if (compound == null) compound = new NBTTagCompound();
        entity.c(compound);
        compound.setInt("NoAI", 1);
        compound.setInt("NoGravity", 1);
        entity.f(compound);
    }

    public static ItemStack getHerobrineHead() {
        net.minecraft.server.v1_8_R3.ItemStack skull = new net.minecraft.server.v1_8_R3.ItemStack(Items.SKULL, 1, 3);
        NBTTagCompound tagCompound = new NBTTagCompound();
        tagCompound.set("SkullOwner", new NBTTagCompound());
        NBTTagCompound skullOwner = tagCompound.getCompound("SkullOwner");
        skullOwner.setString("Id", "9586e5ab-157a-4658-ad80-b07552a9ca63");
        skullOwner.setString("Name", "MHF_Herobrine");
        skullOwner.set("Properties", new NBTTagCompound());
        NBTTagCompound properties = skullOwner.getCompound("Properties");
        properties.set("textures", new NBTTagList());
        NBTTagList textures = properties.getList("textures", 10);
        NBTTagCompound signature = new NBTTagCompound();
        signature.setString("Signature", "pF0vTBmxQWUf9v8SXFNY50I//YmV4cGdfAEENk8J7E9ulIlZQQUEdtYFo/Gd0kqesqsc5v4P31dperP2Z2xMYjckYN3OJhX9xKMA+NbSL54P6X3KIw5EwIZS2oTZCJsQ7Hk2rEiecYeN/7w/NsGa+I9FqYPtqlFbWPXyvayQ67pTcMbX9O4zJbbLkScVKt+xVDFO8FiY4ddw86lg9W+BZcc7ulLTsG66sn5js6Aa5C6OcaWy7C4Aw93wtv0hAgDBa/bBn8/TozhMVmh5Pg28v56raKig098P8GeDZW9+xbLWNmD4Djn+nLZ8xmqGv395vRa5ddAbkiMIwd+XGwoOqeGd1o2PY7snIn2vHnRjCwbRv2UEtlme/AJRnzbBfmSfmJgCKKfy2q/ZF/4SNK8goxJqJz8IRK0JJ+NXUVGQMJYaFtEfdhr2RtwIcVEInBdUZQ4pHJpTQAeGwJE+bhowRIQSqp3s9MJUGbT8xAE4Ix1H1VWdgz0Hce4oMhTkmvTbrm4OmJwH1BZbMsemOmHsDp4mhKgMwdzNqh0gGdPuhCrtOngvc0MYsvLARE1B6uiC8rHyQ8qK9CIUKiPmo4z2ZfOU/7QALkSCEN/WBHFWrLc+a605LLBGStAE9WNfS7yfG1vjtzY1lBfx5lWXodDZFt/q+LoNiBziFV8xaASNJro=");
        NBTTagCompound value = new NBTTagCompound();
        value.setString("Value", "ewogICJ0aW1lc3RhbXAiIDogMTY3MTk5MzY3OTA3MCwKICAicHJvZmlsZUlkIiA6ICI5NTg2ZTVhYjE1N2E0NjU4YWQ4MGIwNzU1MmE5Y2E2MyIsCiAgInByb2ZpbGVOYW1lIiA6ICJNSEZfSGVyb2JyaW5lIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzJjNjVlZDI4MjljODNlMTE5YTgwZGZiMjIyMTY0NDNlODc4ZWYxMDY0OWM0YTM1NGY3NGJmNDVhZDA2YmMxYTciCiAgICB9CiAgfQp9");
        textures.add(value);
        textures.add(signature);
        skull.setTag(tagCompound);
        return skull;
    }

    public static void spawnFallingLootDrop(Location location) {
        // make a landing area
        org.bukkit.World bukkitWorld = location.getWorld();
        Location highest = bukkitWorld.getHighestBlockAt(location).getLocation();
        highest.getBlock().setType(org.bukkit.Material.GOLD_BLOCK);
        List<BlockFace> faces = Arrays.asList(BlockFace.WEST, BlockFace.EAST, BlockFace.SOUTH, BlockFace.NORTH);
        for (BlockFace face : faces) {
            org.bukkit.block.Block block = highest.getBlock().getRelative(face);
            block.setType(org.bukkit.Material.QUARTZ_BLOCK);
        }
        // handle the loot drop
        double x = location.getBlockX() + 0.5;
        double y = location.getBlockY() + 0.5;
        double z = location.getBlockZ() + 0.5;
        location = location.clone();
        net.minecraft.server.v1_8_R3.World world = ((CraftWorld) location.getWorld()).getHandle();
        EntityFallingBlock fallingBlock = new EntityFallingBlock(location, world, x, y, z, Blocks.CRAFTING_TABLE.getBlockData());
        EntityChicken chicken = new EntityChicken(world);
        setEntitySilentAndInvulnerable(chicken);
        chicken.setPosition(x, y, z);
        fallingBlock.ticksLived = 1;
        fallingBlock.dropItem = false;
        NBTTagCompound compound = new NBTTagCompound();
        compound.set("IsLootDrop", new NBTTagString("loot drop"));
        fallingBlock.tileEntityData = compound;
        world.addEntity(chicken);
        chicken.addEffect(new MobEffect(14, 10000, 3, true, true));
        world.addEntity(fallingBlock, CreatureSpawnEvent.SpawnReason.CUSTOM);
        fallingBlock.mount(chicken);
    }

    public static EntityChicken spawnInvisibleChicken(Location location) {
        EntityChicken chicken = new EntityChicken(((CraftWorld) location.getWorld()).getHandle());
        setEntitySilentAndInvulnerable(chicken);
        chicken.setPosition(location.getX(), location.getY(), location.getZ());
        ((CraftWorld) location.getWorld()).getHandle().addEntity(chicken);
        chicken.addEffect(new MobEffect(14, 10000, 3, true, true));
        return chicken;
    }

    public static List<Player> getNearbyPlayers(Location location, int radius) {
        return location.getWorld().getNearbyEntities(location, radius, radius, radius).stream().filter(entity -> entity instanceof Player).map(entity -> (Player) entity).collect(Collectors.toList());
    }

    public static void spawnZombieHorde(Zombie zombie) {
        Location zombieLocation = zombie.getLocation();
        EntityZombie entityZombie = ((CraftZombie) zombie).getHandle();
        zombie.getEquipment().setHelmet(ItemUtils.genZombieHelmet());
        zombie.getEquipment().setItemInHand(ItemUtils.genZombieAxe());
        Arrays.fill(entityZombie.dropChances, 100.0F);
        zombie.setMaxHealth(60);
        zombie.setHealth(60);
        zombie.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10000, 1, true, true));
        zombie.setBaby(false);
        zombie.setCustomName((String) CommonUtils.randomObjectFromList(NAMES));
        zombie.setCustomNameVisible(true);
        net.minecraft.server.v1_8_R3.World world = ((CraftWorld) zombie.getWorld()).getHandle();
        for (Location location : Utils.getSurroundingArea(zombieLocation)) {
            EntityZombie newZombie = new EntityZombie(world);
            newZombie.setPosition(location.getX(), location.getY(), location.getZ());
            newZombie.setCustomName("Weak Minion");
            ((Zombie) newZombie.getBukkitEntity()).getEquipment().setHelmet(new org.bukkit.inventory.ItemStack(Material.LEATHER_HELMET));
            newZombie.setHealth(newZombie.getHealth() / 4);
            world.addEntity(newZombie, CreatureSpawnEvent.SpawnReason.CUSTOM);
        }
        CommonUtils.getNearbyPlayers(zombieLocation, 80).forEach(player -> {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format("&4A zombie horde has spawned %s blocks away from you...", Utils.getBlocksAwayFrom(zombie, player))));
        });
    }

    public static List<Location> getAllLocations(World world, Location pos1, Location pos2) {
        List<Location> locations = new ArrayList<>();
        for (int x = Math.min(pos1.getBlockX(), pos2.getBlockX()); x <= Math.max(pos1.getBlockX(), pos2.getBlockX()); x++) {
            for (int y = Math.min(pos1.getBlockY(), pos2.getBlockY()); y <= Math.max(pos1.getBlockY(), pos2.getBlockY()); y++) {
                for (int z = Math.min(pos1.getBlockZ(), pos2.getBlockZ()); z <= Math.max(pos1.getBlockZ(), pos2.getBlockZ()); z++) {
                    Location location = new Location(world, x, y, z);
                    if (invalidMaterials.contains(location.getWorld().getBlockAt(location).getType())) continue;
                    locations.add(location);
                }
            }
        }
        return locations;
    }

    public static List<Location> traceEntity(org.bukkit.entity.Entity entity) {
        net.minecraft.server.v1_8_R3.World world = ((CraftWorld) entity.getWorld()).getHandle();
        net.minecraft.server.v1_8_R3.Entity nmsEntity = ((CraftEntity) entity).getHandle();
        Location loc = entity.getLocation();
        List<Location> raytrace = new ArrayList<>();
        MovingObjectPosition landing = null;
        double posX = loc.getX();
        double posY = loc.getY();
        double posZ = loc.getZ();
        BlockPosition originalPosition = new BlockPosition(posX, posY, posZ);
        float gravityModifier = Utils.getGravityModifier(entity);
        float motionModifier = Utils.getMotionModifier(entity);

        org.bukkit.util.Vector velocity = new org.bukkit.util.Vector(nmsEntity.motX, nmsEntity.motY, nmsEntity.motZ);
        if (nmsEntity instanceof EntityFireball) {
            EntityFireball fireball = (EntityFireball) nmsEntity;
            velocity = new org.bukkit.util.Vector(fireball.dirX, fireball.dirY, fireball.dirZ);
        }
        double motionX = velocity.getX();
        double motionY = velocity.getY();
        double motionZ = velocity.getZ();

        boolean hasLanded = false;
        while (!hasLanded && posY > 0.0D) {
            double fPosX = posX + motionX;
            double fPosY = posY + motionY;
            double fPosZ = posZ + motionZ;

            Vec3D start = new Vec3D(posX, posY, posZ);
            Vec3D future = new Vec3D(fPosX, fPosY, fPosZ);

            landing = world.rayTrace(start, future);
            hasLanded = (landing != null) && (landing.a() != null);

            posX = fPosX;
            posY = fPosY;
            posZ = fPosZ;
            motionX *= motionModifier;
            motionY *= motionModifier;
            motionZ *= motionModifier;
            motionY -= gravityModifier;
            raytrace.add(new Location(world.getWorld(), posX, posY, posZ));

            double distSquared = distanceSquared(originalPosition, posX, posY, posZ);
            if (distSquared > 48000) break;
        }
        return raytrace;
    }

    public static double distanceSquared(BlockPosition original, double x, double y, double z) {
        double xDif = (double) original.getX() - x;
        double yDif = (double) original.getY() - y;
        double zDif = (double) original.getZ() - z;
        return xDif * xDif + yDif * yDif + zDif * zDif;
    }

    public static float getMotionModifier(org.bukkit.entity.Entity entity) {
        switch (entity.getType()) {
            case FIREBALL:
            case SMALL_FIREBALL:
                return 1.10f;
            case WITHER_SKULL:
                return 1.15f;
            default:
                return 0.99f;
        }
    }

    public static float getGravityModifier(org.bukkit.entity.Entity entity) {
        switch (entity.getType()) {
            case SNOWBALL:
            case ENDER_PEARL:
            case EGG:
                return 0.03f;
            case ARROW:
            case SPLASH_POTION:
                return 0.05f;
            case THROWN_EXP_BOTTLE:
                return 0.07f;
            default:
                return 0.0f;
        }
    }

    public static boolean hasLogoutSpot(Player player) {
        return LogoutIO.loadLogoutSpot(player.getUniqueId()) != null;
    }
}
