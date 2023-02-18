package me.txmc.gradlepluginbase.game.games.uhc;

import lombok.SneakyThrows;
import me.txmc.gradlepluginbase.Main;
import me.txmc.gradlepluginbase.common.CommonUtils;
import me.txmc.gradlepluginbase.utils.Utils;
import net.minecraft.server.v1_8_R3.EntityEnderPearl;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEnderPearl;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class GrenadeListener implements Listener {

    private static final HashSet<Player> damageExempted = new HashSet<>();

    @SneakyThrows
    @EventHandler
    public void onLaunch(ProjectileLaunchEvent event) {
        Projectile projectile = event.getEntity();
        if (projectile.getShooter() != null && projectile.getShooter() instanceof Player) {
            Player player = (Player) projectile.getShooter();
            EntityPlayer ep = ((CraftPlayer) player).getHandle();
            if (ep.inventory.getItemInHand() == null) return;
            if (projectile instanceof Snowball) {
                if (projectile.getShooter() != null && projectile.getShooter() instanceof Player) {
                    if (ep.inventory.getItemInHand().hasTag() && ep.inventory.getItemInHand().getTag().hasKey("Grenade")) {
                        projectile.remove();
                        List<Location> locations = Utils.traceEntity(projectile);
                        player.playSound(player.getLocation(), Sound.FIZZ, 10f, 1f);
                        new Timer().schedule(new TimerTask() {
                            int index = 0;
                            Location location;

                            @Override
                            public void run() {
                                if (index == locations.size()) {
                                    if (location != null)
                                        Utils.run(() -> location.getWorld().createExplosion(location, 4.0f, true));
                                    this.cancel();
                                    return;
                                }
                                location = locations.get(index);
                                if (CommonUtils.getNearbyPlayers(location, 0.5).size() > 0) {
                                    Utils.run(() -> location.getWorld().createExplosion(location, 4.0f, true));
                                    this.cancel();
                                    return;
                                }
                                Object packet1 = Main.getInstance().getParticles().SMOKE_LARGE().packet(true, location);
                                Object packet2 = Main.getInstance().getParticles().SMOKE_NORMAL().packet(true, location);
                                Object packet3 = Main.getInstance().getParticles().CLOUD().packet(true, location, 3);
                                Utils.getNearbyPlayers(location, 35).forEach(p -> {
                                    Main.getInstance().getParticles().sendPacket(p, packet1);
                                    Main.getInstance().getParticles().sendPacket(p, packet2);
                                    Main.getInstance().getParticles().sendPacket(p, packet3);
                                });
                                index++;
                            }
                        }, 0L, 60L);
                    }
                }
            } else if (projectile instanceof EnderPearl) {
                if (ep.inventory.getItemInHand().getTag() != null && ep.inventory.getItemInHand().getTag().hasKey("VelocityPearl")) {
                    EntityEnderPearl pearl = ((CraftEnderPearl) projectile).getHandle();
                    Field cF = EntityEnderPearl.class.getDeclaredField("c");
                    cF.setAccessible(true);
                    cF.set(pearl, null);
                    pearl.die();
                    player.setVelocity(player.getEyeLocation().getDirection().multiply(4.0));
                    Utils.getNearbyPlayers(player.getLocation(), 15).forEach(p -> p.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 10f, 1f));
                    damageExempted.add(player);
                }
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                if (damageExempted.contains(player)) {
                    event.setCancelled(true);
                    damageExempted.remove(player);
                }
            }
        }
    }
}
