package me.txmc.gradlepluginbase.game.games.uhc;

import me.txmc.gradlepluginbase.Main;
import me.txmc.gradlepluginbase.common.events.LootDropLandEvent;
import me.txmc.gradlepluginbase.common.events.LootDropMoveEvent;
import me.txmc.gradlepluginbase.utils.Utils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;

public class LootDropListener implements Listener {

    @EventHandler
    public void onMove(LootDropMoveEvent event) {
        Location location = event.getLocation();
        List<Player> players = Utils.getNearbyPlayers(location, 50);
        for (Location relative : getCardinalRelatives(location)) {
            Object packet = Main.getInstance().getParticles().SMOKE_LARGE().packet(true, relative);
            players.forEach(player -> Main.getInstance().getParticles().sendPacket(player, packet));
        }
    }

    public static Location[] getCardinalRelatives(Location location) {
        return new Location[]{location.clone().add(1, 0, 0), location.clone().add(-1, 0, 0), location.clone().add(0, 0, 1), location.clone().add(0, 0, -1)};
    }

    @EventHandler
    public void onLootDropLand(LootDropLandEvent event) {
        Utils.placeChest(event.getWorld(), event.getPosition());
        Location location = new Location(event.getWorld().getWorld(), event.getPosition().getX(), event.getPosition().getY(), event.getPosition().getZ());
        List<Player> players = Utils.getNearbyPlayers(location, 50);
        for (Location relative : getCardinalRelatives(location)) {
            Object packet = Main.getInstance().getParticles().CLOUD().packet(true, relative);
            players.forEach(player -> Main.getInstance().getParticles().sendPacket(player, packet));
        }
    }
}
