package me.txmc.gradlepluginbase.impl.listener.lobby;

import io.netty.util.internal.ConcurrentSet;
import me.txmc.gradlepluginbase.common.CommonUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;

public class LobbyListeners implements Listener {

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            if (event.getEntity().getWorld().getName().equalsIgnoreCase("lobby")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (!event.getPlayer().isOp() && event.getPlayer().getWorld().getName().equalsIgnoreCase("lobby")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (!event.getPlayer().isOp() && event.getPlayer().getWorld().getName().equalsIgnoreCase("lobby")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.getPlayer().getWorld().getName().equalsIgnoreCase("lobby")) {
            if (event.getPlayer().getLocation().getY() < 40) {
                event.setCancelled(true);
                event.getPlayer().teleport(new Location(Bukkit.getWorld("lobby"), -71.5, 95, 46.5));
            }
        }
    }
}
