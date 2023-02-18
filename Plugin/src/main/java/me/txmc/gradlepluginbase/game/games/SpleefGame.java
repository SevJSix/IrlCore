package me.txmc.gradlepluginbase.game.games;

import com.xxmicloxx.NoteBlockAPI.model.RepeatMode;
import com.xxmicloxx.NoteBlockAPI.songplayer.RadioSongPlayer;
import io.netty.util.internal.ConcurrentSet;
import lombok.Getter;
import lombok.Setter;
import me.txmc.gradlepluginbase.Main;
import me.txmc.gradlepluginbase.common.CommonUtils;
import me.txmc.gradlepluginbase.game.MiniGame;
import me.txmc.gradlepluginbase.utils.*;
import net.md_5.bungee.api.chat.BaseComponent;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.github.paperspigot.Title;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Getter
@Setter
public class SpleefGame extends MiniGame {

    private final ConcurrentHashMap<Player, RadioSongPlayer> songPlayers = new ConcurrentHashMap<>();
    private Player winner;
    private final ConcurrentSet<Player> playersFallen = new ConcurrentSet<>();
    private boolean removedFirstLayer = false;
    private boolean isWon = false;

    public SpleefGame(World world, Player... participants) {
        super(MinigameType.SPLEEF, TeamSetting.NONE, false, world, participants);
    }

    @Override
    public void onStart() {
        saveAllLogoutSpots();

        try {
            WorldEditUtils.pasteSchematicMcEdit(getWorld(), "arenaspleef", new Location(getWorld(), 0.5, 0, 0.5));
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        Location centralLocation = new Location(getWorld(), 0.5, 60, 0.5);
        List<Location> possibleStartLocations = startLocations(centralLocation);
        HashMap<Player, Location> actualStartLocations = new HashMap<>();
        for (Player participant : getParticipants()) {
            Location currentLocation = null;
            while (currentLocation == null) {
                currentLocation = possibleStartLocations.get(ThreadLocalRandom.current().nextInt(0, possibleStartLocations.size()));
                Location finalCurrentLocation = currentLocation;
                if (getParticipants().size() > 1 && getParticipants().stream().filter(player -> !Objects.equals(participant, player) && Objects.equals(player.getWorld(), getWorld())).anyMatch(player -> finalCurrentLocation.distance(player.getLocation()) < 15)) {
                    currentLocation = null;
                } else {
                    currentLocation.setDirection(centralLocation.subtract(currentLocation).toVector());
                    actualStartLocations.put(participant, currentLocation);
                    possibleStartLocations.remove(currentLocation);
                }
            }
        }
        actualStartLocations.forEach(Entity::teleport);
        getParticipants().forEach(this::setPlayerReady);
        getWorld().setPVP(false);
        new Timer().schedule(new TimerTask() {
            int times = 0;

            @Override
            public void run() {
                if (times == 5) {
                    Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                        MusicAPI api = Main.getInstance().getMusicAPI();
                        songPlayers.clear();
                        getParticipants().forEach(player -> {
                            songPlayers.put(player, api.playSong(api.getSong("luigicircuit"), player));
                            songPlayers.get(player).setRepeatMode(RepeatMode.ALL);
                        });
                        setOngoing(true);
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
    }

    @Override
    public void onEnd() {
        broadcast(String.format("&3%s &5Won the Spleef Match!", winner != null ? winner.getName() : "Nobody"));
        isWon = true;
        songPlayers.forEach(((player, radioSongPlayer) -> radioSongPlayer.setPlaying(false)));
        spawnVictoryFireworks();
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
        if (getParticipants().size() - playersFallen.size() <= 1 && !removedFirstLayer) {
            int radius = 30;
            for (int x = -radius; x < radius; x++) {
                for (int z = -radius; z < radius; z++) {
                    Block block = getWorld().getBlockAt(x, 59, z);
                    if (block.getType() == Material.SNOW_BLOCK) block.setType(Material.AIR);
                }
            }
            Utils.playSoundToAll(Sound.FIZZ, 10f, 1f);
            broadcast(String.format("&b%s &6was the last player remaining on the top!", Objects.requireNonNull(getParticipants().stream().filter(player -> !playersFallen.contains(player)).findAny().orElse(null)).getName()));
            removedFirstLayer = true;
            return;
        }
        getParticipants().forEach(player -> {
            if (player.getGameMode() == GameMode.SURVIVAL) {
                EntityPlayer ep = CommonUtils.getNMSPlayer(player);
                if (ep.fallDistance >= 15) {
                    broadcast(String.format("&b%s just fell!", player.getName()));
                    ep.fallDistance = 0;
                    playersFallen.add(player);
                }
            }
        });
    }

    @Override
    public void eliminate(Player player, BaseComponent message) {
        Bukkit.getOnlinePlayers().forEach(onlinePlayer -> onlinePlayer.sendMessage(message));
        removeParticipant(player);
        if (getParticipants().size() <= 1) {
            setWinner(getParticipants().size() > 0 ? getParticipants().stream().findFirst().orElse(null) : null);
            if (getWinner() != null) {
                getWinner().setGameMode(GameMode.CREATIVE);
            }
            onEnd();
        }
    }

    @Override
    public void setPlayerReady(Player player) {
        if (player.isOnline() && !player.isDead()) {
            player.setGameMode(GameMode.SURVIVAL);
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
            player.setFireTicks(0);
            player.setMaxHealth(20);
            player.setHealth(20);
            player.setFoodLevel(20);
            player.getActivePotionEffects().clear();
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 100000, 10, true, false));
            ItemStack spade = new ItemStack(Material.DIAMOND_SPADE);
            spade.addUnsafeEnchantment(Enchantment.DIG_SPEED, 10);
            player.getInventory().addItem(spade);
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
                player.setHealth(20);
                player.setFoodLevel(20);
                player.getActivePotionEffects().clear();
                Utils.resetDisplayName(player);
            }
        }
    }

    public List<Location> startLocations(Location origin) {
        return Utils.genSprialLocations(origin, 26);
    }

    public void spawnVictoryFireworks() {
        if (winner != null) {
            new Timer().schedule(new TimerTask() {
                int times = 0;

                @Override
                public void run() {
                    if (times > 8) {
                        this.cancel();
                        return;
                    }
                    Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                        Utils.spawnFirework(winner.getLocation());
                    });
                    times++;
                }
            }, 0L, 1000L);
        }
    }
}
