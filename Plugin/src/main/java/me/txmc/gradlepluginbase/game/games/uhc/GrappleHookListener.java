package me.txmc.gradlepluginbase.game.games.uhc;

import me.txmc.gradlepluginbase.common.CommonUtils;
import me.txmc.gradlepluginbase.utils.Utils;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.EntityChicken;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class GrappleHookListener implements Listener {

    private final HashMap<Projectile, Player> map = new HashMap<>();
    private final HashMap<Player, List<Location>> locationMap = new HashMap<>();

    @EventHandler
    public void onLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity() instanceof Snowball) {
            if (event.getEntity().getShooter() instanceof Player) {
                Player player = (Player) event.getEntity().getShooter();
                map.putIfAbsent(event.getEntity(), player);
                locationMap.putIfAbsent(player, Utils.traceEntity(event.getEntity()));
            }
        }
    }

    @EventHandler
    public void onLand(ProjectileHitEvent event) {
        if (map.containsKey(event.getEntity())) {
            Player player = map.get(event.getEntity());
            Utils.run(() -> {
                EntityChicken chicken = Utils.spawnInvisibleChicken(player.getLocation());
                EntityPlayer entityPlayer = CommonUtils.getNMSPlayer(player);
                entityPlayer.mount(chicken);
                List<Location> locations = locationMap.get(player);
                new Timer().schedule(new TimerTask() {
                    int index = 0;
                    @Override
                    public void run() {
                        if (index == locations.size()) {
                            this.cancel();
                            Utils.run(() -> {
                                entityPlayer.vehicle = null;
                                chicken.die();
                                map.remove(event.getEntity());
                                locationMap.remove(player);
                            });
                        }
                        Utils.run(() -> {
                            BlockPosition position = CommonUtils.toBlockPos(locations.get(index));
                            chicken.setPosition(position.getX(), position.getY(), position.getZ());
                        });
                        index++;
                    }
                }, 0L, 50L);
            });
        }
    }
}
