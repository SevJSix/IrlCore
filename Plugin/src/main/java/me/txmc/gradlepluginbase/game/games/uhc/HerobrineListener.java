package me.txmc.gradlepluginbase.game.games.uhc;

import lombok.Getter;
import lombok.SneakyThrows;
import me.txmc.gradlepluginbase.Main;
import me.txmc.gradlepluginbase.common.CommonUtils;
import me.txmc.gradlepluginbase.common.events.PlayerPreDeathEvent;
import me.txmc.gradlepluginbase.common.npc.NPC;
import me.txmc.gradlepluginbase.common.npc.NPCManager;
import me.txmc.gradlepluginbase.common.npc.NPCProximity;
import me.txmc.gradlepluginbase.game.GameData;
import me.txmc.gradlepluginbase.game.MiniGame;
import me.txmc.gradlepluginbase.game.games.UHCGame;
import me.txmc.gradlepluginbase.utils.ItemUtils;
import me.txmc.gradlepluginbase.utils.Utils;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class HerobrineListener implements Listener, NPCProximity, GameData {

    public static final HashSet<Player> playerProximity = new HashSet<>();
    public static List<Biome> herobrineBiomes;
    public static Location herobrineSpawnLocation = null;
    @Getter
    private static HerobrineListener instance;

    static {
        herobrineBiomes = Arrays.asList(Biome.FOREST, Biome.FOREST_HILLS, Biome.BIRCH_FOREST, Biome.BIRCH_FOREST_HILLS);
    }

    private final HashMap<Player, Integer> playerDeathMap = new HashMap<>();
    private final List<Sound> scarySounds = Arrays.asList(Sound.GHAST_SCREAM, Sound.WITHER_HURT, Sound.ENDERMAN_DEATH, Sound.ENDERMAN_SCREAM);
    private final float[] pitches = new float[]{0.9f, 1f, 1.1f, 0.8f};
    private final List<PotionType> badPotions = Arrays.asList(PotionType.INSTANT_DAMAGE, PotionType.POISON, PotionType.WEAKNESS);

    public HerobrineListener() {
        instance = this;
    }

    public static void playRecord13(Player player, Location location) {
        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
        PacketPlayOutNamedSoundEffect soundEffect = new PacketPlayOutNamedSoundEffect("records.13", location.getX(), location.getY(), location.getZ(), 1000f, 1f);
        connection.sendPacket(soundEffect);
    }

    public static void changeSky(Player player, boolean reset) {
        float f = (reset) ? -1 : 4f;
        int i = 7;
        Bukkit.getWorlds().forEach(world -> {
            if (reset) {
                world.setTime(16000);
                ((CraftWorld) world).getHandle().worldData.setStorm(false);
            } else {
                world.setTime(6000);
            }
        });
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        PacketPlayOutGameStateChange packet = new PacketPlayOutGameStateChange(i, f);
        entityPlayer.playerConnection.sendPacket(packet);
        if (!reset) playRecord13(player, player.getLocation());
        if (reset) {
            for (WorldServer world : MinecraftServer.getServer().worlds) {
                WorldData data = world.getWorldData();
                data.setStorm(false);
            }
        }
    }

    public static void addToInventory(Player player, org.bukkit.inventory.ItemStack itemStack) {
        org.bukkit.inventory.ItemStack holding = player.getItemInHand();
        if (holding == null) player.setItemInHand(itemStack);
        else if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(holding);
            player.setItemInHand(itemStack);
        } else {
            player.getWorld().dropItemNaturally(player.getLocation(), itemStack);
        }
    }

    @EventHandler
    public void onMove1(PlayerMoveEvent event) {
        if (NPCManager.getHerobrine() == null) {
            Player player = event.getPlayer();
            Location location = player.getLocation();
            if (location.getY() > 60) {
                Biome biome = location.getBlock().getBiome();
                if (herobrineBiomes.contains(biome)) {
                    if (new Random().nextInt(4000) == 0) {
                        MiniGame miniGame = getPlayerGame(player);
                        if (miniGame == null || miniGame.getGameType() != MiniGame.MinigameType.UHC) return;
                        UHCGame match = (UHCGame) miniGame;
                        if (match.getTimesProgressed() > 0) {
                            if (!(match.getHerobrineSpawns() >= 2)) {
                                if (match.getHerobrinesFirstHaunt() != null && match.getHerobrinesFirstHaunt() == player)
                                    return;
                                if (match.getHerobrinesFirstHaunt() == null) match.setHerobrinesFirstHaunt(player);
                                match.setHerobrineSpawns(match.getHerobrineSpawns() + 1);
                                Location herobrineLocation = Utils.getRandomHerobrineLocation(location, 35);
                                herobrineSpawnLocation = herobrineLocation;
                                NPC npc = NPCManager.createNPC(herobrineLocation, "Herobrine", "ewogICJ0aW1lc3RhbXAiIDogMTY3MTk4NzA1Nzk2MiwKICAicHJvZmlsZUlkIiA6ICIwMzk1NzAyMjZiOTc0ZWE5ODJhNjM1NDJmOGIwNjc4MSIsCiAgInByb2ZpbGVOYW1lIiA6ICJMYUhtMzQiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQxZDFjM2VkN2M5YzUwMzczMjAyMjcxYTQxY2ZiNTExNWQ5NzJlYTUxOWMxN2YzMjQ4MmY5Y2I5MmM4N2MxMSIKICAgIH0KICB9Cn0=", "AM2vYb9Q2vcU+8CmgF9kdfT68a7a7dMfBfzgk2eYs9Ig05IXTIpm/65toEgIgPI8tXF8Z/+UqQXQGTX9NsfhGZvs2qSSfZYS2xMQf7yZzGMbgm6594hJqEE1B3wwmUA+HfD7DXDY6Ap7gjNCYv85R07SV9n1lhMeoNEHkJsSJVIK7RI2PQWRkvBVOnQCNrua4nQxVVs2RKGGHpQhxH9OW4qtWiz5/7pQPtAJIvy1PVlWIQWICTqReO0XPJLCWFC5faqlMQPVU041fPfN/0xzxNkGgCp4AJQ3o0X8NAav3X/U/FxHQUa8jgJc51GGzC3tt37B8Hol4dsBSyP1g1atWNqn9tqRW9LEt7GTaEFxzMKbcyZpeKL6CBsmmr9cMjBRupu8Ofoi6Brcol1nOYX3zyH+K80IRp1BIAjPsVQ4wyL8tK9Wv36tJ3mnUmvi8BBZHahZruDggyI/h/MkjXksHRmR6s1TOaKxphYlEQVmpKrF3FAXNCdKtOaOnx1pK1zeNpybTpiMmIUU0CED5TEUAghQ7dkyiHBxpa3kZDVKAVJLUqWYJPruPvV2o2aZ9NqHFQnV8qfJbAaCXA09yBTXUEDIzy5tbxmXIKHhuJHQJhPr9OBqjiciNpHvu5PDVNtT1gmuf+p6TX2ZwGFvfviS1WcdCvH7w4Pkq4I9G2oWXow=");
                                npc.setNameInvisible();
                                npc.getEntityPlayer().getBukkitEntity().setMaxHealth(40);
                                npc.getEntityPlayer().getBukkitEntity().setHealth(40);
                                int radius = 13;
                                for (int x = herobrineLocation.getBlockX() - radius; x < herobrineLocation.getBlockX() + radius; x++) {
                                    for (int y = herobrineLocation.getBlockY() - radius; y < herobrineLocation.getBlockY() + radius; y++) {
                                        for (int z = herobrineLocation.getBlockZ() - radius; z < herobrineLocation.getBlockZ() + radius; z++) {
                                            Location locationAt = new Location(player.getWorld(), x, y, z);
                                            Material typeAt = locationAt.getBlock().getType();
                                            if (typeAt == Material.LEAVES || typeAt == Material.LEAVES_2) {
                                                locationAt.getBlock().setType(Material.AIR);
                                            }
                                            if (typeAt == Material.LONG_GRASS) {
                                                locationAt.getBlock().setType(Material.AIR);
                                            }
                                            if (typeAt == Material.GRASS) {
                                                if (new Random().nextInt(3) == 0) {
                                                    locationAt.getBlock().setType(Material.GRAVEL);
                                                }
                                            }
                                            if ((typeAt.equals(Material.LOG) || typeAt.equals(Material.LOG_2)) && locationAt.getBlock().getRelative(BlockFace.DOWN).getType() == Material.DIRT) {
                                                Block toSet = locationAt.getBlock();
                                                for (int i = 0; i < ThreadLocalRandom.current().nextInt(7, 9); i++) {
                                                    toSet.setTypeIdAndData(toSet.getTypeId(), toSet.getData(), true);
                                                    toSet = toSet.getRelative(BlockFace.UP);
                                                }
                                                toSet.getWorld().getHighestBlockAt(toSet.getLocation()).getLocation().clone().add(0, -1, 0).getBlock().setType(Material.FIRE);
                                            }
                                        }
                                    }
                                }
                                Bukkit.broadcastMessage(Utils.translateChars(String.format("&c&lHerobrine is haunting %s. Defeat him before he becomes too powerful!", player.getName())));
                                player.sendMessage(Utils.translateChars("&8&oWalk towards the fiery light"));
                                changeSky(player, false);
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        NPC npc = NPCManager.getHerobrine();
        if (npc == null) return;
        Player player = event.getPlayer();
        handleEntityLook(npc, player);
        if (isInProximity(npc, player)) {
            if (!playerProximity.contains(player)) {
                enterProximity(player);
                playerProximity.add(player);
            }
        } else if (playerProximity.contains(player)) {
            exitProximity(player);
            playerProximity.remove(player);
        }
    }

    @EventHandler
    public void onFade(BlockFadeEvent event) {
        if (NPCManager.getHerobrine() != null) event.setCancelled(true);
    }

    @EventHandler
    public void onBurn(BlockBurnEvent event) {
        if (NPCManager.getHerobrine() != null) event.setCancelled(true);
    }

    @EventHandler
    public void onSpread(BlockSpreadEvent event) {
        if (NPCManager.getHerobrine() != null) event.setCancelled(true);
    }

    @EventHandler
    public void onFire(BlockIgniteEvent event) {
        if (NPCManager.getHerobrine() != null) event.setCancelled(true);
    }

    @SneakyThrows
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (NPCManager.getHerobrine() == null) return;
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            EntityPlayer npc = NPCManager.getHerobrine().getEntityPlayer();
            if (((CraftPlayer) event.getEntity()).getHandle().getId() == npc.getId()) {
                Player player = (Player) event.getDamager();
                if (!player.hasPotionEffect(PotionEffectType.BLINDNESS))
                    player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 10000, 2, true, true));
                if (new Random().nextInt(3) == 0) {
                    ItemStack pot = CraftItemStack.asNMSCopy(new Potion(badPotions.get(ThreadLocalRandom.current().nextInt(0, badPotions.size())), ThreadLocalRandom.current().nextInt(1, 2), true).toItemStack(1));
                    PacketPlayOutEntityEquipment equipment = new PacketPlayOutEntityEquipment(npc.getId(), npc.inventory.itemInHandIndex, pot);
                    npc.inventory.setItem(npc.inventory.itemInHandIndex, pot);
                    Bukkit.getOnlinePlayers().forEach(p -> CommonUtils.getConnection(p).sendPacket(equipment));
                    Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
                        if (npc.isAlive() && npc.getHealth() > 0) {
                            npc.playerInteractManager.useItem(npc, npc.world, pot);
                            PacketPlayOutEntityEquipment emptyPacket = new PacketPlayOutEntityEquipment(npc.getId(), npc.inventory.itemInHandIndex, new ItemStack(CraftMagicNumbers.getItem(Material.AIR)));
                            npc.inventory.setItem(npc.inventory.itemInHandIndex, new ItemStack(CraftMagicNumbers.getItem(Material.AIR)));
                            Bukkit.getOnlinePlayers().forEach(p -> CommonUtils.getConnection(p).sendPacket(emptyPacket));
                        }
                    }, 10L);
                }
                for (int i = 0; i < ThreadLocalRandom.current().nextInt(1, 3); i++) {
                    player.playSound(player.getLocation(), scarySounds.get(ThreadLocalRandom.current().nextInt(0, scarySounds.size())), 10f, pitches[ThreadLocalRandom.current().nextInt(0, pitches.length)]);
                    Location location = Utils.getRandomHerobrineLocation(npc.getBukkitEntity().getLocation(), 12);
                    EntityZombie zombie = new EntityZombie(npc.world);
                    zombie.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
                    net.minecraft.server.v1_8_R3.ItemStack skull = Utils.getHerobrineHead();
                    ((Zombie) zombie.getBukkitEntity()).getEquipment().setHelmet(CraftItemStack.asBukkitCopy(skull));
                    Arrays.fill(zombie.dropChances, 0f);
                    Utils.setEntitySilent(zombie);
                    zombie.setCustomName("Herobrine's Slave");
                    npc.world.addEntity(zombie, CreatureSpawnEvent.SpawnReason.CUSTOM);
                    zombie.setHealth(zombie.getHealth() / 4);
                    zombie.setGoalTarget(((CraftPlayer) player).getHandle(), EntityTargetEvent.TargetReason.CUSTOM, true);
                }
            }
        }
    }

    @EventHandler
    public void onTarget(EntityTargetLivingEntityEvent event) {
        if (NPCManager.getHerobrine() == null) return;
        NPC npc = NPCManager.getHerobrine();
        EntityPlayer player = npc.getEntityPlayer();
        if (event.getEntity() instanceof Zombie && event.getTarget() instanceof Player) {
            EntityPlayer target = ((CraftPlayer) event.getTarget()).getHandle();
            if (target.getId() == player.getId()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDeath1(PlayerPreDeathEvent event) {
        Player bukkitPlayer = event.getPlayer().getBukkitEntity();
        if (playerProximity.contains(bukkitPlayer)) {
            if (!playerDeathMap.containsKey(bukkitPlayer)) playerDeathMap.put(bukkitPlayer, 1);
            else playerDeathMap.replace(bukkitPlayer, playerDeathMap.get(bukkitPlayer) + 1);
            int deaths = playerDeathMap.get(bukkitPlayer);
            if (!(deaths >= 5)) {
                event.setCancelled(true);
                CommonUtils.playSound(bukkitPlayer, Sound.ENDERDRAGON_GROWL);
                bukkitPlayer.sendMessage(Utils.translateChars(String.format("&b&lYOU CHEATED DEATH. &r&bYou can only cheat death %s more times.", 5 - deaths)));
            } else {
                CommonUtils.playSound(bukkitPlayer, Sound.FIZZ);
                playerDeathMap.remove(bukkitPlayer);
                Bukkit.broadcastMessage(Utils.translateChars(String.format("&c%s died whilst fighting herobrine! Avenge them at X: %s, Y: %s, Z: %s", bukkitPlayer.getName(), bukkitPlayer.getLocation().getBlockX(), bukkitPlayer.getLocation().getBlockY(), bukkitPlayer.getLocation().getBlockZ())));
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerPreDeathEvent event) {
        if (NPCManager.getHerobrine() == null) return;
        NPC npc = NPCManager.getHerobrine();
        EntityPlayer player = event.getPlayer();
        if (npc.getEntityPlayer().getId() == player.getId()) {
            event.setCancelled(true);
            if (event.getSource() instanceof EntityDamageSource) {
                Entity entity = event.getSource().getEntity();
                if (entity instanceof EntityHuman) {
                    Bukkit.broadcastMessage(Utils.translateChars("&cHerobrine has been killed!"));
                    playerProximity.forEach(this::exitProximity);
                    playerProximity.clear();
                    NPCManager.removeHerobrine();
                    herobrineSpawnLocation = null;
                    Player killer = (Player) entity.getBukkitEntity();
                    changeSky(killer, true);
                    Location location = player.getBukkitEntity().getLocation();
                    killer.getWorld().strikeLightningEffect(location);
                    location.getBlock().setType(Material.NETHERRACK);
                    location.getBlock().getRelative(BlockFace.UP).setType(Material.FIRE);
                    Utils.giveItemBack(CommonUtils.getNMSPlayer(killer), CraftItemStack.asNMSCopy(ItemUtils.genLightningStick()));
                    killer.getWorld().getEntities().stream().filter(e -> ((CraftEntity) e).getHandle().hasCustomName() && e.getCustomName().equals("Herobrine's Slave") && e instanceof Zombie).forEach(e -> {
                        e.remove();
                        Location l = e.getLocation();
                        Object smoke = Main.getInstance().getParticles().SMOKE_LARGE().packet(true, l);
                        Object cloud = Main.getInstance().getParticles().SMOKE_LARGE().packet(true, l);
                        Main.getInstance().getParticles().sendPacket(killer, smoke);
                        Main.getInstance().getParticles().sendPacket(killer, cloud);
                        CommonUtils.playSound(killer, Sound.FIZZ);
                    });
                }
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack inHand = ((CraftPlayer) event.getPlayer()).getHandle().inventory.getItemInHand();
            if (inHand == null) return;
            if (inHand.getTag() == null || !inHand.getTag().hasKey("LightningUses")) return;
            int uses = inHand.getTag().getInt("LightningUses");
            if (uses >= 20) {
                inHand.getTag().remove("LightningUses");
                inHand.getTag().getCompound("display").setString("Name", Utils.translateChars("&e&lDAMAGED &c&lHerobrine Lightning Rod"));
                return;
            }
            Location location = event.getPlayer().getTargetBlock((HashSet<Byte>) null, 60).getLocation();
            if (location != null) {
                location.getWorld().strikeLightning(location);
                inHand.getTag().setInt("LightningUses", uses + 1);
                Utils.sendMessage(event.getPlayer(), String.format("&3You have used your lightning rod &a%s &3times", uses + 1));
            }
        }
    }

    @Override
    public void enterProximity(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 10000, 1, true, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 10000, 0, true, true));
        player.playSound(player.getLocation(), Sound.AMBIENCE_THUNDER, 10f, 1f);
        player.sendMessage(Utils.translateChars("&7You are currently suffering herobrine's aura."));
    }

    @Override
    public void exitProximity(Player player) {
        player.removePotionEffect(PotionEffectType.BLINDNESS);
        player.removePotionEffect(PotionEffectType.SLOW);
        player.removePotionEffect(PotionEffectType.CONFUSION);
        player.removePotionEffect(PotionEffectType.POISON);
        player.sendMessage(Utils.translateChars("&7No longer suffering herobrine's aura."));
    }

    @Override
    public void handleEntityLook(NPC npc, Player player) {
        Location location = npc.getLocation();
        location.setDirection(player.getLocation().subtract(location).toVector());
        byte yawAngle = (byte) ((location.getYaw() % 360) * 256 / 360);
        byte pitchAngle = (byte) ((location.getPitch() % 360) * 256 / 360);
        PacketPlayOutEntityHeadRotation headRotationPacket = new PacketPlayOutEntityHeadRotation(npc.getEntityPlayer(), yawAngle);
        PacketPlayOutEntity.PacketPlayOutEntityLook lookPacket = new PacketPlayOutEntity.PacketPlayOutEntityLook(npc.getEntityPlayer().getId(), yawAngle, pitchAngle, npc.getEntityPlayer().onGround);
        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
        connection.sendPacket(headRotationPacket);
        connection.sendPacket(lookPacket);
        npc.getEntityPlayer().setPositionRotation(location.getX(), location.getY(), location.getZ(), yawAngle, pitchAngle);
    }

    @Override
    public boolean isInProximity(NPC npc, Player player) {
        return npc.getLocation().distance(player.getLocation()) <= 15;
    }
}
