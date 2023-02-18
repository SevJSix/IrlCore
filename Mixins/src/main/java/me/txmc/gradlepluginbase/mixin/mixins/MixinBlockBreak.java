package me.txmc.gradlepluginbase.mixin.mixins;

import me.txmc.rtmixin.CallbackInfo;
import me.txmc.rtmixin.mixin.MethodInfo;
import me.txmc.rtmixin.mixin.Replace;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;

import java.util.concurrent.ThreadLocalRandom;

public class MixinBlockBreak {

    @Replace(info = @MethodInfo(_class = Block.class, name = "dropNaturally", sig = {World.class, BlockPosition.class, IBlockData.class, float.class, int.class}, rtype = void.class))
    public static void dropNaturally(CallbackInfo ci) {
        World world = (World) ci.getParameters()[0];
        BlockPosition position = (BlockPosition) ci.getParameters()[1];
        IBlockData data = (IBlockData) ci.getParameters()[2];
        float f = (float) ci.getParameters()[3];
        int i = (int) ci.getParameters()[4];
        if (!world.isClientSide) {
            Block block = (Block) ci.getSelf();
            int j = block.getDropCount(i, world.random);

            for (int k = 0; k < j; ++k) {
                if (world.random.nextFloat() < f) {
                    Item item = block.getDropType(data, world.random, i);
                    String worldName = world.getWorld().getName();
                    if (worldName.equals("uhc")) {
                        if (data.equals(Blocks.GOLD_ORE.getBlockData())) {
                            item = CraftMagicNumbers.getItem(org.bukkit.Material.GOLD_INGOT);
                        } else if (data.equals(Blocks.IRON_ORE.getBlockData())) {
                            item = CraftMagicNumbers.getItem(org.bukkit.Material.IRON_INGOT);
                        }
                    }
                    if (item != null) {
                        if (data.equals(Blocks.REDSTONE_ORE.getBlockData())) {
                            for (int i1 = 0; i1 < 3; i1++) {
                                Block.a(world, position, new ItemStack(item, 1, block.getDropData(data)));
                            }
                            return;
                        }
                        if (data.equals(Blocks.GOLD_ORE.getBlockData()) || data.equals(Blocks.IRON_ORE.getBlockData())) {
                            for (int i1 = 0; i1 < ThreadLocalRandom.current().nextInt(1, 4); i1++) {
                                Block.a(world, position, new ItemStack(item, 1, block.getDropData(data)));
                            }
                        } else {
                            Block.a(world, position, new ItemStack(item, 1, block.getDropData(data)));
                        }
                    }
                }
            }
        }
    }
}
