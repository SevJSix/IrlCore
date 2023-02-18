package me.txmc.gradlepluginbase.impl.worldgen;

import net.minecraft.server.v1_8_R3.BlockPosition;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;
import org.bukkit.generator.BlockPopulator;

import java.util.Random;

public class OrePopulator extends BlockPopulator {

    private final int[][] locationOffset = new int[][]{new int[]{1, 0, 0}, new int[]{-1, 0, 0}, new int[]{0, 0, 1}, new int[]{0, 0, -1}, new int[]{1, 0, 1}, new int[]{1, 0, -1}, new int[]{-1, 0, 1}, new int[]{-1, 0, -1}, new int[]{1, 1, 0}, new int[]{1, -1, 0}, new int[]{-1, 1, 0}, new int[]{-1, -1, 0}, new int[]{0, 1, -1}, new int[]{0, -1, 1}, new int[]{0, -1, -1}};

    @Override
    public void populate(World world, Random random, Chunk chunk) {
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < world.getSeaLevel(); y++) {
                for (int z = 0; z < 16; z++) {
                    Block block = chunk.getBlock(chunk.getX() * 16 + x, y, chunk.getZ() * 16 + z);
                    if (block.getType().equals(Material.STONE)) {
                        if (random.nextInt(40) == 0) {
                            genOreVein(block.getLocation(), random, getOreBasedOnRarity(random));
                        }
                    }
                }
            }
        }
    }

    private Material getOreBasedOnRarity(Random random) {
        Material material = null;
        if (random.nextInt(30) == 0) material = Material.DIAMOND_ORE;
        else if (random.nextInt(25) == 0) material = Material.EMERALD_ORE;
        else if (random.nextInt(20) == 0) material = Material.LAPIS_ORE;
        else if (random.nextInt(15) == 0) material = Material.GOLD_ORE;
        else if (random.nextInt(10) == 0) material = Material.REDSTONE_ORE;
        else if (random.nextInt(5) == 0) material = Material.IRON_ORE;
        else material = Material.COAL_ORE;
        return material;
    }

    private void genOreVein(Location location, Random random, Material material) {
        BlockPosition position = new BlockPosition(location.getX(), location.getY(), location.getZ());
        ((CraftWorld) location.getWorld()).getHandle().setTypeAndData(position, CraftMagicNumbers.getBlock(material).getBlockData(), 0);
    }

    private Location cloneRandomLocation(Location location, Random random) {
        int[] offsets = locationOffset[random.nextInt(locationOffset.length)];
        return location.clone().add(offsets[0], offsets[1], offsets[2]);
    }
}
