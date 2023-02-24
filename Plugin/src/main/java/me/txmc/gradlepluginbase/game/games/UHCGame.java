package me.txmc.gradlepluginbase.game.games;

import lombok.Getter;
import lombok.Setter;
import me.txmc.gradlepluginbase.Main;
import me.txmc.gradlepluginbase.common.CommonUtils;
import me.txmc.gradlepluginbase.common.npc.NPCManager;
import me.txmc.gradlepluginbase.game.MiniGame;
import me.txmc.gradlepluginbase.game.games.uhc.HerobrineListener;
import me.txmc.gradlepluginbase.utils.LogoutIO;
import me.txmc.gradlepluginbase.utils.LogoutSpot;
import me.txmc.gradlepluginbase.utils.Utils;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
public class UHCGame extends MiniGame {

    private final World world;
    private Object winner;
    private int herobrineSpawns = 0;
    private Player herobrinesFirstHaunt = null;
    private int timesProgressed = 0;
    private double borderSize = 0;

    public UHCGame(World world) {
        super(MinigameType.UHC, serverWideTeamSetting.getValue(), true, world, Bukkit.getOnlinePlayers().toArray(new Player[0]));
        this.world = world;
        this.world.getWorldBorder().setCenter(0.5, 0.5);
    }

    public UHCGame(World world, List<Player> players) {
        super(MinigameType.UHC, serverWideTeamSetting.getValue(), true, world, players.toArray(new Player[0]));
        this.world = world;
        this.world.getWorldBorder().setCenter(0.5, 0.5);
    }

    public boolean isGracePeriod() {
        return timesProgressed == 0;
    }

    @Override
    public void onStart() {
        saveAllLogoutSpots();

        borderSize = 1600; // set initial border size
        world.getWorldBorder().setSize(borderSize * 2);
        if (serverWideTeamSetting.getValue() == TeamSetting.NONE) {
            getParticipants().forEach(player -> {
                setPlayerReady(player);
                Utils.randomlySpawnPlayer(player, world, (int) (borderSize - 2), (int) borderSize - 2);
            });
        } else {
            for (Team team : serverWideTeams) {
                Location randomLocation = Utils.getRandomLocation(this.world, (int) (borderSize - 2), (int) borderSize - 2);
                team.getMembers().forEach(member -> {
                    setPlayerReady(member);
                    member.teleport(randomLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);
                });
            }
        }
        playWitherSpawn();
        broadcast("&3UHC started on mode: &e&l" + serverWideTeamSetting.getValue().name());
        getParticipants().forEach(player -> { // check to see if players glitched into blocks
            if (player.getLocation().getBlock().getRelative(BlockFace.UP).getType().isSolid()) {
                player.teleport(player.getWorld().getHighestBlockAt(player.getLocation()).getLocation());
            }
        });
        setNextProgressTick((int) (Utils.getCurrentTick() + (20 * TimeUnit.MINUTES.toSeconds(2))));
        setOngoing(true);
    }

    @Override
    public void setPlayerReady(Player player) {
        if (player.isOnline() && !player.isDead()) {
            player.setGameMode(GameMode.SURVIVAL);
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
            player.setMaxHealth(60);
            player.setHealth(60);
            player.setFoodLevel(20);
            player.setFireTicks(0);
            player.getActivePotionEffects().clear();
            Utils.givePlayerStarterGear(player);
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
                player.setMaxHealth(20);
                player.setHealth(20);
                player.setFoodLevel(20);
                player.setFireTicks(0);
                player.getActivePotionEffects().clear();
                Utils.resetDisplayName(player);
            }
        }
    }

    @Override
    public void onEnd() {
        setOngoing(false);
        broadcast(String.format("%s &r&5Won The UHC Match!", winner == null ? "&3Nobody" : winner instanceof Player ? "&3" + ((Player) winner).getName() : "&3Team " + ((Team) winner).getDisplayName()));
        if (NPCManager.getHerobrine() != null) {
            NPCManager.removeHerobrine();
            HerobrineListener.playerProximity.forEach(player -> {
                try {
                    Method exitProximityM = HerobrineListener.class.getDeclaredMethod("exitProximity", Player.class);
                    exitProximityM.setAccessible(true);
                    exitProximityM.invoke(HerobrineListener.getInstance(), player);
                    HerobrineListener.changeSky(player, true);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            });
            HerobrineListener.playerProximity.clear();
        }
        if (winner != null) {
            Location loc = winner instanceof Player ? ((Player) winner).getLocation() : ((Team) winner).getAny().getLocation();
            Utils.spawnFirework(loc);
            new Timer().schedule(new TimerTask() {
                int times = 0;

                @Override
                public void run() {
                    if (times > 8) {
                        this.cancel();
                        return;
                    }
                    Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                        Utils.spawnFirework(loc);
                    });
                    times++;
                }
            }, 0L, 1000L);
        }
        resetMinigameData();

        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
            Bukkit.getOnlinePlayers().forEach(this::resetPlayerState);
        }, 20 * 8L);
    }

    @Override
    public void onProgress(int currentTick) {
        borderSize = world.getWorldBorder().getSize() / 2;
        if (timesProgressed == 0) {
            playWitherDeath();
            broadcast("&8Grace Period Over! &4&lPVP Enabled");
            spawnDrops((int) Math.round(getParticipants().size() * 0.5) + 1, (int) TimeUnit.MINUTES.toSeconds(5), (int) TimeUnit.MINUTES.toSeconds(10));
        } else {
            broadcast(String.format("World border shrinking from &a%s &3blocks to &a%s &3blocks", (int) borderSize, (int) borderSize / 2));
            borderSize = borderSize / 2;
            world.getWorldBorder().setSize(borderSize * 2, (long) (Math.sqrt(borderSize) * 15));
            spawnDrops((int) Math.round(getParticipants().size() * 0.5) + 1, (int) TimeUnit.MINUTES.toSeconds(1), (int) TimeUnit.MINUTES.toSeconds(3));
        }
        timesProgressed++;
    }

    @Override
    public void onTick(int currentTick) {
        if (currentTick == getNextProgressTick()) {
            onProgress(currentTick);
            setNext(true);
        } else if (isNext()) {
            int nextTick = currentTick + (20 * (timesProgressed == 1 ? (int) TimeUnit.MINUTES.toSeconds(15) : timesProgressed == 2 ? (int) TimeUnit.MINUTES.toSeconds(10) : timesProgressed == 3 ? (int) TimeUnit.MINUTES.toSeconds(6) : (int) TimeUnit.MINUTES.toSeconds(4)));
            setNextProgressTick(nextTick);
            setNext(false);
        }
    }

    @Override
    public void eliminate(Player player, BaseComponent message) {
        Bukkit.getOnlinePlayers().forEach(onlinePlayer -> onlinePlayer.sendMessage(message));
        player.getLocation().getWorld().strikeLightningEffect(player.getLocation());
        removeParticipant(player);
        if (isPlayerInTeam(player)) {
            Team team = getPlayerTeam(player);
            team.remove(player, false);
            if (team.getMembers().size() < 1) {
                serverWideTeams.remove(team);
                broadcast(String.format("&3Team %s &3has been eliminated!", team.getDisplayName()));
            } else {
                team.getMembers().forEach(member -> {
                    Utils.setDisplayName(team.getTeamColor(), member, team.getName());
                });
            }
        }
        if (isHasTeams()) {
            if (serverWideTeams.size() <= 1) {
                setWinner(serverWideTeams.size() < 1 ? null : serverWideTeams.stream().findFirst().orElse(null));
                onEnd();
                return;
            }
        }
        if (getParticipants().size() <= 1) {
            setWinner(getParticipants().size() > 0 ? getParticipants().stream().findFirst().orElse(null) : null);
            onEnd();
        }
    }

    public void spawnDrops(int numberOfDrops, int origin, int bound) {
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
            if (isOngoing()) {
                for (int i = 0; i < numberOfDrops; i++) {
                    spawnLootDrop((int) borderSize, (int) borderSize);
                }
                playLootDropSound();
            }
        }, (20L * ThreadLocalRandom.current().nextInt(origin, bound)));
    }

    public void spawnLootDrop(int xRange, int zRange) {
        Location location = Utils.generateLootDrop(world, xRange, zRange);
        Location highest = location.getWorld().getHighestBlockAt(location).getLocation();
        broadcast(String.format("&3Loot drop landing at &3X: &a%s &3Y: &a%s &3Z: &a%s", highest.getBlockX(), highest.getBlockY(), highest.getBlockZ()));
    }

    public void playWitherDeath() {
        getParticipants().forEach(player -> player.playSound(player.getLocation(), Sound.WITHER_DEATH, 10.0F, 1.0F));
    }

    public void playWitherSpawn() {
        getParticipants().forEach(player -> player.playSound(player.getLocation(), Sound.WITHER_SPAWN, 10.0F, 1.0F));
    }

    public void playLootDropSound() {
        getParticipants().forEach(player -> player.playSound(player.getLocation(), Sound.LEVEL_UP, 10.0F, 1.0F));
    }
}
