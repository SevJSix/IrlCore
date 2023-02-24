package me.txmc.gradlepluginbase.game.games.uhc;

import me.txmc.gradlepluginbase.Main;
import me.txmc.gradlepluginbase.common.CommonUtils;
import me.txmc.gradlepluginbase.common.events.*;
import me.txmc.gradlepluginbase.common.npc.NPCManager;
import me.txmc.gradlepluginbase.game.GameData;
import me.txmc.gradlepluginbase.game.MiniGame;
import me.txmc.gradlepluginbase.game.games.UHCGame;
import me.txmc.gradlepluginbase.impl.worldgen.*;
import me.txmc.gradlepluginbase.utils.DeathMessage;
import me.txmc.gradlepluginbase.utils.ItemUtils;
import me.txmc.gradlepluginbase.utils.Utils;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftArrow;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class UHCListeners implements Listener, GameData {

    @EventHandler
    public void onTick(ServerTickEvent event) {
        serverWideMinigames.forEach(miniGame -> {
            if (miniGame.isOngoing()) {
                miniGame.onTick(event.getTick());
            }
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (isPlayerInGame(player)) {
            event.setQuitMessage(null);
            MiniGame game = getPlayerGame(player);
            if (game instanceof UHCGame) {
                UHCGame uhc = (UHCGame) game;
                uhc.eliminate(player, new TextComponent(Utils.translateChars(String.format("&3%s &4left the game, so they were eliminated!", player.getName()))));
            }
        }
        if (queuedPlayers.contains(player)) {
            queuedPlayers.remove(player);
            Utils.broadcastMessage(String.format("&c%s left the queue", player.getName()));
        }
        if (serverWideTeams.stream().anyMatch(team -> team.contains(player))) {
            MiniGame.Team team = getPlayerTeam(player);
            team.remove(player, false);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (Utils.hasLogoutSpot(player)) Utils.handleLogoutSpot(player);
        Utils.resetDisplayName(player);
    }

    @EventHandler
    public void onDamage(PreDamageEvent event) {
        if (!isAnyGameOngoing()) return;
        Player player = event.getPlayer().getBukkitEntity();
        if (isPlayerInGame(player) && getPlayerGame(player) instanceof UHCGame) {
            UHCGame game = (UHCGame) getPlayerGame(player);
            event.setCancelled(game.isGracePeriod());
            return;
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            Player player = (Player) event.getEntity();
            Player damager = (Player) event.getDamager();
            if (isPlayerInTeam(player) && isPlayerInTeam(damager)) {
                if (getPlayerTeam(player).contains(damager) || getPlayerTeam(player) == getPlayerTeam(damager)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!Objects.equals(event.getEntity().getWorld().getName(), "uhc")) return;
        LivingEntity entity = event.getEntity();
        if (entity instanceof Slime) event.setCancelled(true);
        if (entity instanceof Monster) {
            boolean doesNotHaveCustomName = !((CraftLivingEntity) entity).getHandle().hasCustomName();
            int blocksAway = Utils.getBlocksAwayFrom(entity, Utils.getNearestPlayer(entity));
            if (entity instanceof Zombie && doesNotHaveCustomName && new Random().nextInt(600) == 0 && blocksAway <= 30 && blocksAway != -1 && entity.getLocation().getY() > 60) {
                Zombie zombie = (Zombie) event.getEntity();
                Utils.spawnZombieHorde(zombie);

            } else {
                if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER_EGG || event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.BUILD_WITHER)
                    return;
                event.setCancelled(doesNotHaveCustomName);
            }
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        if (!Objects.equals(event.getEntity().getWorld().getName(), "uhc")) return;
        if (event.getEntity() instanceof Sheep || event.getEntity() instanceof Chicken || event.getEntity() instanceof Zombie) {
            Location location = event.getEntity().getLocation();
            location.getWorld().dropItemNaturally(location, new ItemStack(Material.FEATHER, 1));
        }
    }

    @EventHandler
    public void onLeavesDecay(LeavesDecayEvent event) {
        if (!Objects.equals(event.getBlock().getWorld().getName(), "uhc")) return;
        Location location = event.getBlock().getLocation();
        World world = location.getWorld();
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        if (rand.nextInt(0, 20) == 10) {
            world.dropItemNaturally(location, new ItemStack(Material.APPLE));
        } else if (rand.nextInt(0, 200) == 100) {
            world.dropItemNaturally(location, new ItemStack(Material.GOLDEN_APPLE));
            CommonUtils.getNearbyPlayers(location, 30).forEach(player -> {
                int blocksAway = Utils.getBlocksAwayFrom(location, player.getLocation());
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format("&6A golden apple dropped from a tree %s blocks away", blocksAway)));
            });
        }
    }

    @EventHandler
    public void onHeadConsume(PlayerHeadConsumeEvent event) {
        if (!Objects.equals(event.getPlayer().getBukkitEntity().getWorld().getName(), "uhc")) return;
        Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
            Player bukkitPlayer = event.getPlayer().getBukkitEntity();
            bukkitPlayer.playSound(bukkitPlayer.getLocation(), Sound.EAT, 1.0F, 1.0F);
            bukkitPlayer.playSound(bukkitPlayer.getLocation(), Sound.BURP, 1.0F, 1.0F);
            bukkitPlayer.getItemInHand().setAmount(bukkitPlayer.getItemInHand().getAmount() - 1);
            if (bukkitPlayer.getItemInHand().getAmount() == 1) {
                bukkitPlayer.getInventory().remove(bukkitPlayer.getItemInHand());
            }
            event.getPlayer().addEffect(new MobEffect(10, 400, 0, true, true));
            event.getPlayer().addEffect(new MobEffect(1, 200, 1, true, true));
        });
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!Objects.equals(event.getPlayer().getWorld().getName(), "uhc")) return;
        Player player = event.getPlayer();
        if (player != null) {
            if (player.getInventory().getItemInHand().getType() == Material.COMPASS) {
                Player closestPlayer = getNearestPlayer(player);
                if (closestPlayer == null) return;
                player.setCompassTarget(closestPlayer.getLocation());
                player.sendMessage(Utils.translateChars("&7You are closest to &c" + closestPlayer.getName() + ""));
            }
        }
    }

    private Player getNearestPlayer(Player player) {
        double distNear = 0.0D;
        Player closestPlayer = null;
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (!Objects.equals(getPlayerTeam(player), getPlayerTeam(onlinePlayer)) && player != onlinePlayer && !onlinePlayer.getGameMode().equals(GameMode.SPECTATOR)) {
                if (player.getWorld() == onlinePlayer.getWorld()) {
                    Location location = onlinePlayer.getLocation();
                    double dist = location.distance(player.getLocation());
                    if (closestPlayer == null || dist < distNear) {
                        closestPlayer = onlinePlayer;
                        distNear = dist;
                    }
                }
            }
        }
        return closestPlayer;
    }

    @EventHandler
    public void onDeath(PlayerPreDeathEvent event) {
        // various checks
        EntityPlayer entityPlayer = event.getPlayer();
        Player player = entityPlayer.getBukkitEntity();
        if (!isPlayerInGame(player)) return;
        if (!(getPlayerGame(player) instanceof UHCGame)) return;
        if (NPCManager.getHerobrine() != null && NPCManager.getHerobrine().getLocation().distance(player.getLocation()) <= 15)
            return;

        // handle when player dies
        event.setCancelled(true);
        CommonUtils.resetHealth(player);
        player.setGameMode(GameMode.SPECTATOR);
        Arrays.stream(player.getInventory().getContents()).filter(Objects::nonNull).forEach(item -> player.getWorld().dropItemNaturally(player.getLocation(), item));
        player.getInventory().clear();
        Utils.genPlayerHeadAtLocation(player.getLocation(), entityPlayer);
        player.getWorld().strikeLightningEffect(player.getLocation());

        // handle elimination messages and match progression
        UHCGame game = (UHCGame) getPlayerGame(player);
        DeathMessage elimination = new DeathMessage(event.getSource(), player);
        if (elimination.getKiller() != null && elimination.getKiller() instanceof Player)
            player.setSpectatorTarget(elimination.getKiller());
        game.eliminate(player, elimination.toComponent());
    }

    @EventHandler
    public void onBorder(BorderUpdateEvent event) {
        if (isAnyGameOngoing() && event.getTrueSize() < 10000) {
            if (getUHCGame() != null)
                getUHCGame().broadcast(String.format("&3World Border has been set to to &c%s blocks", event.getTrueSize()));
        }
    }

    @EventHandler
    public void onWorldInit(WorldInitEvent event) {
        if (Objects.equals(event.getWorld().getName(), "uhc")) {
            event.getWorld().getPopulators().add(new OrePopulator());
            event.getWorld().getPopulators().add(new ChatGPTPopulatorTest());
        }
    }

    @EventHandler
    public void onVillagerCreate(VillagerCreateEvent event) {
        event.setTradeOffer(ItemUtils.getOffers()[ThreadLocalRandom.current().nextInt(0, ItemUtils.getOffers().length)]);
    }

    @EventHandler
    public void onBow(ProjectileLaunchEvent event) {
        if (event.getEntity() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getEntity();
            if (arrow.getShooter() instanceof Player) {
                EntityPlayer shooter = ((CraftPlayer) arrow.getShooter()).getHandle();
                net.minecraft.server.v1_8_R3.ItemStack holding = shooter.inventory.getItemInHand();
                if (holding.getTag() != null && holding.getTag().hasKey("TNTBow")) {
                    arrow.remove();
                    holding.damage(50, shooter);
                    shooter.getBukkitEntity().playSound(shooter.getBukkitEntity().getLocation(), Sound.FIREWORK_LAUNCH, 10f, 1f);
                    shooter.getBukkitEntity().playSound(shooter.getBukkitEntity().getLocation(), Sound.ITEM_BREAK, 10f, getPitchFromDurability(holding.getData()));
                    EntityTNTPrimed tnt = new EntityTNTPrimed(arrow.getLocation(), shooter.world, arrow.getLocation().getX(), arrow.getLocation().getY(), arrow.getLocation().getZ(), shooter);
                    EntityArrow nmsArrow = ((CraftArrow) arrow).getHandle();
                    tnt.motX = nmsArrow.motX;
                    tnt.motY = nmsArrow.motY;
                    tnt.motZ = nmsArrow.motZ;
                    tnt.fuseTicks = 15;
                    shooter.world.addEntity(tnt);
                }
            }
        }
    }

    @EventHandler
    public void onBookEdit(PlayerEditBookEvent event) {
        net.minecraft.server.v1_8_R3.ItemStack holding = ((CraftPlayer) event.getPlayer()).getHandle().inventory.getItemInHand();
        if (holding.hasTag() && holding.getTag().hasKey("RodolfoRat")) {
            Player hacker = event.getPlayer();
            BookMeta meta = event.getNewBookMeta();
            String page = meta.getPage(1);
            List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
            Player target = null;
            for (Player player : players) {
                if (page.toLowerCase().contains(player.getName().toLowerCase())) {
                    target = player;
                }
            }
            if (target == hacker) {
                Utils.sendMessage(hacker, "&cTarget cannot be yourself!");
                event.setCancelled(true);
                event.setNewBookMeta(event.getPreviousBookMeta());
                return;
            }
            if (target == null) {
                Utils.sendMessage(hacker, "&cPlayer you selected is not online, try again.");
                event.setCancelled(true);
                event.setNewBookMeta(event.getPreviousBookMeta());
                return;
            }
            event.setSigning(true);
            meta.setTitle(Utils.translateChars("&2Rodolfo's RAT"));
            meta.setAuthor(Utils.translateChars("&bLuis 'Rodolfo' Saldivar"));
            meta.setPages(Utils.translateChars("&3Rodolfo Rat has successfully infiltrated &a" + target.getName() + "'s &3network."));
            event.setNewBookMeta(meta);
            Location location = hacker.getLocation();
            Location targetLocation = target.getLocation();
            hacker.teleport(targetLocation);
            target.teleport(location);
            hacker.playSound(hacker.getLocation(), Sound.ENDERMAN_TELEPORT, 10f, 1f);
            target.playSound(target.getLocation(), Sound.ENDERMAN_TELEPORT, 10f, 1f);
            target.sendMessage(Utils.translateChars(String.format("&a%s &3just used their rodolfo powers to hack you!", hacker.getName())));
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getBlockPlaced() != null) {
            EntityPlayer player = ((CraftPlayer) event.getPlayer()).getHandle();
            if (player.inventory.getItemInHand().hasTag() && player.inventory.getItemInHand().getTag().hasKey("ArkMouse")) {
                Block block = event.getBlockPlaced();
                List<Player> nearby = block.getLocation().getWorld().getNearbyEntities(block.getLocation(), 15, 15, 15).stream().filter(entity -> entity instanceof Player).map(entity -> (Player) entity).collect(Collectors.toList());
                EnumDirection direction = player.getDirection();
                BlockPosition position = new BlockPosition(block.getX(), block.getY(), block.getZ());
                List<BlockPosition> positions = new ArrayList<>();
                for (int i = 0; i < 15; i++) {
                    position = position.shift(direction);
                    positions.add(position);
                    BlockPosition blockPosClone = new BlockPosition(position);
                    for (int i1 = 0; i1 < 15; i1++) {
                        blockPosClone = blockPosClone.shift(EnumDirection.DOWN);
                        positions.add(blockPosClone);
                    }
                }
                positions.forEach(pos -> {
                    Location location = new Location(block.getWorld(), pos.getX(), pos.getY(), pos.getZ());
                    if (location.getBlock().getType() != Material.AIR && location.getBlock().getType() != Material.BEDROCK) {
                        Block b = location.getBlock();
                        b.breakNaturally(new ItemStack(Material.DIAMOND_PICKAXE));
                        Object packet = Main.getInstance().getParticles().SMOKE_LARGE().packet(true, location);
                        nearby.forEach(near -> Main.getInstance().getParticles().sendPacket(near, packet));
                    }
                });
            }
        }
    }

    private float getPitchFromDurability(int dura) {
        if (dura > 0 && dura <= 50) return 1.4f;
        else if (dura > 50 && dura <= 101) return 1.3f;
        else if (dura > 101 && dura <= 152) return 1.2f;
        else if (dura > 152 && dura <= 203) return 1.1f;
        else if (dura > 203 && dura <= 254) return 1.0f;
        else if (dura > 254 && dura <= 305) return 0.9f;
        else if (dura > 305 && dura <= 356) return 0.8f;
        else if (dura > 356) return 0.7f;
        else if (dura == 0) return 0.6f;
        return 1.0f;
    }
}
