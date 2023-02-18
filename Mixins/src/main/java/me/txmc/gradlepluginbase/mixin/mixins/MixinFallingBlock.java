package me.txmc.gradlepluginbase.mixin.mixins;

import lombok.SneakyThrows;
import me.txmc.gradlepluginbase.common.events.LootDropLandEvent;
import me.txmc.gradlepluginbase.common.events.LootDropMoveEvent;
import me.txmc.rtmixin.CallbackInfo;
import me.txmc.rtmixin.mixin.MethodInfo;
import me.txmc.rtmixin.mixin.Replace;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.event.CraftEventFactory;

import java.lang.reflect.Field;
import java.util.Iterator;

public class MixinFallingBlock {

    private static int count = 0;

    public static void spawnFirework(World world, BlockPosition location) {
        ItemStack itemStack = generateFirework();
        EntityFireworks fireworks = new EntityFireworks(world, location.getX() + 0.5, location.getY() + 0.5, location.getZ() + 0.5, itemStack);
        world.addEntity(fireworks);
    }

    public static ItemStack generateFirework() {
        ItemStack rocket = new ItemStack(Items.FIREWORKS);
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagCompound fireworks = new NBTTagCompound();
        NBTTagList explosions = new NBTTagList();
        NBTTagCompound fireworkComp = new NBTTagCompound();
        fireworkComp.setByte("Type", (byte) 1);
        fireworkComp.setByte("Flicker", (byte) 1);
        fireworkComp.setIntArray("Colors", new int[]{255, 16711680, 16777215});
        explosions.add(fireworkComp);
        fireworks.set("Explosions", explosions);
        tag.set("Fireworks", fireworks);
        rocket.setTag(tag);
        return rocket;
    }

    @SneakyThrows
    @Replace(info = @MethodInfo(_class = EntityFallingBlock.class, name = "t_", sig = {}, rtype = void.class))
    public static void mixinFalling(CallbackInfo ci) {
        EntityFallingBlock fallingBlock = (EntityFallingBlock) ci.getSelf();
        Block block = fallingBlock.getBlock().getBlock();
        if (block.getMaterial() == Material.AIR) {
            fallingBlock.die();
        } else {
            fallingBlock.lastX = fallingBlock.locX;
            fallingBlock.lastY = fallingBlock.locY;
            fallingBlock.lastZ = fallingBlock.locZ;
            BlockPosition blockposition;
            if (fallingBlock.ticksLived++ == 0) {
                blockposition = new BlockPosition(fallingBlock);
                if (fallingBlock.world.getType(blockposition).getBlock() == block && !CraftEventFactory.callEntityChangeBlockEvent(fallingBlock, blockposition.getX(), blockposition.getY(), blockposition.getZ(), Blocks.AIR, 0).isCancelled()) {
                    fallingBlock.world.setAir(blockposition);
                    fallingBlock.world.spigotConfig.antiXrayInstance.updateNearbyBlocks(fallingBlock.world, blockposition);
                } else if (!fallingBlock.world.isClientSide) {
                    fallingBlock.die();
                    return;
                }
            }

            fallingBlock.motY -= 0.03999999910593033;
            fallingBlock.move(fallingBlock.motX, fallingBlock.motY, fallingBlock.motZ);
            if (fallingBlock.tileEntityData != null && fallingBlock.tileEntityData.hasKey("IsLootDrop")) {
                LootDropMoveEvent event = new LootDropMoveEvent(new Location(fallingBlock.world.getWorld(), fallingBlock.locX, fallingBlock.locY, fallingBlock.locZ));
                Bukkit.getPluginManager().callEvent(event);
            }
            fallingBlock.ticksLived = 10;
            if (fallingBlock.inUnloadedChunk && fallingBlock.world.paperSpigotConfig.removeUnloadedFallingBlocks) {
                fallingBlock.die();
            }

            if (fallingBlock.world.paperSpigotConfig.fallingBlockHeightNerf != 0 && fallingBlock.locY > (double) fallingBlock.world.paperSpigotConfig.fallingBlockHeightNerf) {
                if (fallingBlock.dropItem) {
                    fallingBlock.a(new ItemStack(block, 1, block.getDropData(fallingBlock.getBlock())), 0.0F);
                }

                fallingBlock.die();
            }

            fallingBlock.motX *= 0.9800000190734863;
            fallingBlock.motY *= 0.9800000190734863;
            fallingBlock.motZ *= 0.9800000190734863;
            if (!fallingBlock.world.isClientSide) {
                blockposition = new BlockPosition(fallingBlock);
                if (fallingBlock.tileEntityData != null && fallingBlock.tileEntityData.hasKey("IsLootDrop")) {
                    if (count == 10) {
                        spawnFirework(fallingBlock.world, blockposition.up());
                        count = 0;
                    } else {
                        count++;
                    }
                }
                if (fallingBlock.onGround || fallingBlock.vehicle != null && fallingBlock.vehicle.onGround) {
                    if (fallingBlock.vehicle != null) {
                        if (fallingBlock.tileEntityData != null && fallingBlock.tileEntityData.hasKey("IsLootDrop")) {
                            EntityChicken chicken = (EntityChicken) fallingBlock.vehicle;
                            fallingBlock.die();
                            chicken.die();
                            LootDropLandEvent event = new LootDropLandEvent(fallingBlock.world, blockposition);
                            Bukkit.getPluginManager().callEvent(event);
                            return;
                        }
                    }
                    fallingBlock.motX *= 0.699999988079071;
                    fallingBlock.motZ *= 0.699999988079071;
                    fallingBlock.motY *= -0.5;
                    if (fallingBlock.world.getType(blockposition).getBlock() != Blocks.PISTON_EXTENSION) {
                        fallingBlock.die();
                        Field booleanE = EntityFallingBlock.class.getDeclaredField("e");
                        booleanE.setAccessible(true);
                        boolean fall = booleanE.getBoolean(fallingBlock);
                        if (!fall) {
                            if (fallingBlock.world.a(block, blockposition, true, EnumDirection.UP, null, null) && !BlockFalling.canFall(fallingBlock.world, blockposition.down()) && blockposition.getX() >= -30000000 && blockposition.getZ() >= -30000000 && blockposition.getX() < 30000000 && blockposition.getZ() < 30000000 && blockposition.getY() >= 0 && blockposition.getY() < 256 && fallingBlock.world.getType(blockposition) != fallingBlock.getBlock()) {
                                if (CraftEventFactory.callEntityChangeBlockEvent(fallingBlock, blockposition.getX(), blockposition.getY(), blockposition.getZ(), fallingBlock.getBlock().getBlock(), fallingBlock.getBlock().getBlock().toLegacyData(fallingBlock.getBlock())).isCancelled()) {
                                    return;
                                }
                                fallingBlock.world.setTypeAndData(blockposition, fallingBlock.getBlock(), 3);
                                fallingBlock.world.spigotConfig.antiXrayInstance.updateNearbyBlocks(fallingBlock.world, blockposition);
                                if (block instanceof BlockFalling) {
                                    ((BlockFalling) block).a_(fallingBlock.world, blockposition);
                                }

                                if (fallingBlock.tileEntityData != null && block instanceof IContainer) {
                                    TileEntity tileentity = fallingBlock.world.getTileEntity(blockposition);
                                    if (tileentity != null) {
                                        NBTTagCompound nbttagcompound = new NBTTagCompound();
                                        tileentity.b(nbttagcompound);
                                        Iterator iterator = fallingBlock.tileEntityData.c().iterator();

                                        while (iterator.hasNext()) {
                                            String s = (String) iterator.next();
                                            NBTBase nbtbase = fallingBlock.tileEntityData.get(s);
                                            if (!s.equals("x") && !s.equals("y") && !s.equals("z")) {
                                                nbttagcompound.set(s, nbtbase.clone());
                                            }
                                        }

                                        tileentity.a(nbttagcompound);
                                        tileentity.update();
                                    }
                                }
                            } else if (fallingBlock.dropItem && fallingBlock.world.getGameRules().getBoolean("doEntityDrops")) {
                                fallingBlock.a(new ItemStack(block, 1, block.getDropData(fallingBlock.getBlock())), 0.0F);
                            }
                        }
                    }
                } else if (fallingBlock.ticksLived > 100 && !fallingBlock.world.isClientSide && (blockposition.getY() < 1 || blockposition.getY() > 256) || fallingBlock.ticksLived > 600) {
                    if (fallingBlock.dropItem && fallingBlock.world.getGameRules().getBoolean("doEntityDrops")) {
                        fallingBlock.a(new ItemStack(block, 1, block.getDropData(fallingBlock.getBlock())), 0.0F);
                    }

                    fallingBlock.die();
                }
            }
        }
    }
}
