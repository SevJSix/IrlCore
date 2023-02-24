package me.txmc.gradlepluginbase.game.games.uhc;

import me.txmc.gradlepluginbase.Main;
import me.txmc.gradlepluginbase.common.CommonUtils;
import me.txmc.gradlepluginbase.utils.GenerationUtil;
import me.txmc.gradlepluginbase.utils.Utils;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.material.MaterialData;

import java.util.Timer;
import java.util.TimerTask;

public class TowerBuilderListener implements Listener {

    private static final BlockFace[] FACES = new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
    private static Location ORIGIN;
    private static Block CURRENT_BLOCK = null;
    private static int FACE_INDEX = 0;

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (event.getBlockPlaced().getType() == org.bukkit.Material.BEDROCK) {
            EntityPlayer entityPlayer = CommonUtils.getNMSPlayer(event.getPlayer());
            World world = entityPlayer.world;
            final BlockPosition[] position = {CommonUtils.toBlockPos(event.getBlockPlaced().getLocation())};
            if (entityPlayer.inventory.getItemInHand() != null && entityPlayer.inventory.getItemInHand().hasTag() && entityPlayer.inventory.getItemInHand().getTag().hasKey("TowerBuilder")) {
                EntityChicken chicken = new EntityChicken(world);
                Utils.setEntitySilentAndInvulnerable(chicken);
                Utils.disableEntityAI(chicken);
                chicken.setPosition(position[0].getX() + 0.5, position[0].getY() + 3.0, position[0].getZ() + 0.5);
                entityPlayer.setPosition(position[0].getX() + 0.5, position[0].getY() + 3.0, position[0].getZ() + 0.5);
                world.addEntity(chicken);
                chicken.addEffect(new MobEffect(14, 10000, 3, true, true));
                entityPlayer.mount(chicken);
                new Timer().schedule(new TimerTask() {
                    int blocksPlaced = 0;

                    @Override
                    public void run() {
                        Location location = new Location(world.getWorld(), position[0].getX() + 0.5, position[0].getY(), position[0].getZ() + 0.5, entityPlayer.yaw, entityPlayer.pitch);
                        if (blocksPlaced >= 20) {
                            this.cancel();
                            Utils.run(() -> {
                                entityPlayer.setPositionRotation(location.getX(), location.getY() + 2, location.getZ(), location.getYaw(), location.getPitch());
                                entityPlayer.vehicle = null;
                                chicken.die();
                                entityPlayer.getBukkitEntity().teleport(location.clone().add(0, 2, 0));
                                GenerationUtil.generate(location);
                            });
                        }
                        entityPlayer.setPositionRotation(location.getX(), location.getY() + 3, location.getZ(), location.getYaw(), location.getPitch());
                        chicken.setPosition(location.getX(), location.getY() + 3, location.getZ() + 0.5);
                        Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                            world.setTypeUpdate(position[0], Blocks.COBBLESTONE.getBlockData());
                            CommonUtils.playSound(entityPlayer.getBukkitEntity(), Sound.ITEM_PICKUP);
                        });
                        CommonUtils.spawnParticle(entityPlayer.getBukkitEntity(), location, EnumParticle.BLOCK_CRACK, new MaterialData(org.bukkit.Material.COBBLESTONE), 1);
                        position[0] = position[0].up();
                        blocksPlaced++;
                    }
                }, 0L, 100L);
            }
        }
    }
}
