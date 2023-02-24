package me.txmc.gradlepluginbase.game.games.skywars;

import me.txmc.gradlepluginbase.common.CommonUtils;
import me.txmc.gradlepluginbase.common.events.PlayerPreDeathEvent;
import me.txmc.gradlepluginbase.game.GameData;
import me.txmc.gradlepluginbase.game.MiniGame;
import me.txmc.gradlepluginbase.game.games.SkywarsGame;
import me.txmc.gradlepluginbase.packet.packetlistener.PacketEvent;
import me.txmc.gradlepluginbase.packet.packetlistener.PacketListener;
import me.txmc.gradlepluginbase.utils.Utils;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.PacketPlayInFlying;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

public class SkywarsListeners implements Listener, GameData {

    @EventHandler
    public void onPreDeath(PlayerPreDeathEvent event) {
        Player player = event.getPlayer().getBukkitEntity();
        if (!isPlayerInGame(player)) return;
        if (!(getPlayerGame(player) instanceof SkywarsGame)) return;
        event.setCancelled(true);
        player.getInventory().clear();
        SkywarsGame game = (SkywarsGame) getPlayerGame(player);
        game.eliminate(player, new TextComponent(Utils.translateChars(String.format("&6&lSKYWARS>>&r &3%s &4has been eliminated!", player.getName()))));
        player.setGameMode(GameMode.SPECTATOR);
        game.getParticipants().stream().findAny().ifPresent(player::setSpectatorTarget);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (player.getGameMode() == GameMode.SPECTATOR && player.getWorld().getName().equalsIgnoreCase("skywars")) {
                if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onFall(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (!isPlayerInGame(player)) return;
            if (!(getPlayerGame(player) instanceof SkywarsGame)) return;
            if (event.getCause() == EntityDamageEvent.DamageCause.FALL) event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSpawn(CreatureSpawnEvent event) {
        if (event.getEntity().getWorld().getName().equalsIgnoreCase("skywars")) {
            event.setCancelled(!(event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER_EGG));
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (!isPlayerInGame(player)) return;
        if (!(getPlayerGame(player) instanceof SkywarsGame)) return;
        SkywarsGame game = (SkywarsGame) getPlayerGame(player);
        event.setCancelled(!game.isOngoing());
        if (!event.isCancelled() && game.isOngoing() && event.getBlockPlaced().getType() == Material.TNT) {
            event.getBlockPlaced().setType(Material.AIR);
            player.getWorld().spawnEntity(event.getBlockPlaced().getLocation(), EntityType.PRIMED_TNT);
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!isPlayerInGame(player)) return;
        if (!(getPlayerGame(player) instanceof SkywarsGame)) return;
        SkywarsGame game = (SkywarsGame) getPlayerGame(player);
        event.setCancelled(!game.isOngoing());
    }

    @EventHandler
    public void onWeather(WeatherChangeEvent event) {
        if (event.getWorld().getName().equals("skywars")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (isPlayerInGame(player)) {
            event.setQuitMessage(null);
            MiniGame game = getPlayerGame(player);
            if (game instanceof SkywarsGame) {
                SkywarsGame skywars = (SkywarsGame) game;
                skywars.eliminate(player, new TextComponent(Utils.translateChars(String.format("&6&lSKYWARS>>&r &3%s &4has been eliminated due to leaving!", player.getName()))));
            }
        }
    }
}
