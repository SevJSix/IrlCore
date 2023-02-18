package me.txmc.gradlepluginbase.game.games.uhc;

import me.txmc.gradlepluginbase.Main;
import me.txmc.gradlepluginbase.common.CommonUtils;
import me.txmc.gradlepluginbase.utils.Utils;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.generator.ChunkGenerator;

import java.util.*;

public class LumberAxeListener implements Listener {

    public static List<Location> getLeavesLocations(Location origin) {
        List<Location> locations = new ArrayList<>();
        for (BlockFace value : BlockFace.values()) {
            Location shifted = origin.getBlock().getRelative(value).getLocation();
            if (shifted.getBlock().getTypeId() == 18) {
                locations.add(shifted);
            }
            for (BlockFace blockFace : BlockFace.values()) {
                Location shifted1 = shifted.getBlock().getRelative(blockFace).getLocation();
                if (shifted1.getBlock().getTypeId() == 18) {
                    locations.add(shifted1);
                }
            }
        }
        return locations;
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (!isLog(block)) return;
        Player player = event.getPlayer();
        ItemStack inHand = CommonUtils.getNMSPlayer(player).inventory.getItemInHand();
        if (inHand != null && inHand.hasTag() && inHand.getTag().hasKey("LumberAxe")) {
            Location location = block.getLocation();
            List<Location> logLocations = new ArrayList<>();
            boolean isLast = false;
            while (!isLast) {
                if (isLog(location.getBlock())) {
                    logLocations.add(location);
                    location = location.clone().add(0, 1, 0);
                } else {
                    isLast = true;
                }
            }

            logLocations.remove(0);
            new Thread(() -> {
                new Timer().schedule(new TimerTask() {
                    int times = 0;

                    @Override
                    public void run() {
                        if (times == logLocations.size()) {
                            this.cancel();
                            return;
                        }
                        Location logLocation = logLocations.get(times);
                        int data = logLocation.getBlock().getTypeId() + (logLocation.getBlock().getData() << 12);
                        Utils.run(() -> logLocation.getBlock().breakNaturally());
                        PacketPlayOutNamedSoundEffect packet = new PacketPlayOutNamedSoundEffect("dig.wood", logLocation.getX(), logLocation.getY(), logLocation.getZ(), 1f, 1f);
                        PacketPlayOutWorldParticles particles = new PacketPlayOutWorldParticles(EnumParticle.BLOCK_CRACK, true, (float) ((float) logLocation.getX() + 0.5), (float) logLocation.getY(), (float) ((float) logLocation.getZ() + 0.5), 0f, 0f, 0f, 1f, 100, data);
                        CommonUtils.getNearbyPlayers(logLocation, 12).forEach(nearby -> CommonUtils.sendPackets(nearby, packet, particles));
                        if (inHand.getTag().hasKey("FallingLeaves")) {
                            for (Location leavesLocation : getLeavesLocations(logLocation)) {
                                Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                                    EntityFallingBlock fallingBlock = new EntityFallingBlock(leavesLocation, ((CraftWorld) leavesLocation.getWorld()).getHandle(), leavesLocation.getX() + 0.5, leavesLocation.getBlockY(), leavesLocation.getBlockZ() + 0.5, CraftMagicNumbers.getBlock(leavesLocation.getBlock()).getBlockData());
                                    leavesLocation.getBlock().setType(Material.AIR);
                                    ((CraftWorld) leavesLocation.getWorld()).getHandle().addEntity(fallingBlock);
                                    fallingBlock.ticksLived = 1;
                                    fallingBlock.dropItem = new Random().nextInt(5) == 0;
                                });
                            }
                        }
                        times++;
                    }
                }, 0L, 90L);
            }).start();
        }
    }

    public boolean isLog(Block block) {
        return block.getTypeId() == 17 || block.getTypeId() == 5;
    }
}
