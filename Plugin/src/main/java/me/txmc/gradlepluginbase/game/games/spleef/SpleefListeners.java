package me.txmc.gradlepluginbase.game.games.spleef;

import me.txmc.gradlepluginbase.Main;
import me.txmc.gradlepluginbase.common.CommonUtils;
import me.txmc.gradlepluginbase.common.events.PlayerPreDeathEvent;
import me.txmc.gradlepluginbase.game.GameData;
import me.txmc.gradlepluginbase.game.MiniGame;
import me.txmc.gradlepluginbase.game.games.SpleefGame;
import me.txmc.gradlepluginbase.utils.Utils;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

public class SpleefListeners implements Listener, GameData {

    @EventHandler
    public void onPreDeath(PlayerPreDeathEvent event) {
        Player player = event.getPlayer().getBukkitEntity();
        if (!isPlayerInGame(player)) return;
        if (!(getPlayerGame(player) instanceof SpleefGame)) return;
        event.setCancelled(true);
        player.getInventory().clear();
        SpleefGame game = (SpleefGame) getPlayerGame(player);
        game.eliminate(player, new TextComponent(Utils.translateChars(String.format("&a&lSPLEEF>>&r &3%s &4has been eliminated!", player.getName()))));
        player.setGameMode(GameMode.SPECTATOR);
        game.getParticipants().stream().findAny().ifPresent(player::setSpectatorTarget);
    }

    @EventHandler
    public void onFall(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (!isPlayerInGame(player)) return;
            if (!(getPlayerGame(player) instanceof SpleefGame)) return;
            if (event.getCause() == EntityDamageEvent.DamageCause.FALL) event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSpawn(CreatureSpawnEvent event) {
        if (event.getEntity().getWorld().getName().equalsIgnoreCase("spleef")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (!isPlayerInGame(player)) return;
            if (!(getPlayerGame(player) instanceof SpleefGame)) return;
            event.setFoodLevel(20);
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (!isPlayerInGame(player)) return;
        event.setCancelled(getPlayerGame(player) instanceof SpleefGame);
    }

    @EventHandler
    public void onSpawn(EntitySpawnEvent event) {
        if (event.getEntity() instanceof Item) {
            if (event.getEntity().getWorld().getName().equalsIgnoreCase("spleef") && ((Item) event.getEntity()).getItemStack().getType() != Material.SNOW_BALL) event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!isPlayerInGame(player)) return;
        if (!(getPlayerGame(player) instanceof SpleefGame)) return;
        SpleefGame game = (SpleefGame) getPlayerGame(player);
        if (!game.isOngoing()) {
            event.setCancelled(true);
            return;
        }
        Material material = event.getBlock().getType();
        event.getBlock().getDrops().clear();
        if (material != Material.SNOW && material != Material.SNOW_BLOCK && material != Material.TNT) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onWeather(WeatherChangeEvent event) {
        if (event.getWorld().getName().equals("spleef")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (isPlayerInGame(player)) {
            event.setQuitMessage(null);
            MiniGame game = getPlayerGame(player);
            if (game instanceof SpleefGame) {
                SpleefGame spleef = (SpleefGame) game;
                spleef.eliminate(player, new TextComponent(Utils.translateChars(String.format("&a&lSPLEEF>>&r &3%s &4has been eliminated due to leaving!", player.getName()))));
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!isPlayerInGame(player)) return;
        if (!(getPlayerGame(player) instanceof SpleefGame)) return;
        SpleefGame game = (SpleefGame) getPlayerGame(player);
        if (!game.isOngoing()) {
            Location cloned = event.getFrom().clone();
            EntityPlayer ep = CommonUtils.getNMSPlayer(player);
            cloned.setYaw(ep.yaw);
            cloned.setPitch(ep.pitch);
            player.teleport(cloned, PlayerTeleportEvent.TeleportCause.PLUGIN);
            return;
        }
        Location from = event.getFrom().clone().add(0, -1, 0);
        if (from.getBlock().getType() == Material.TNT) Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
            from.getBlock().setType(Material.AIR);
        }, (3L));
    }
}
