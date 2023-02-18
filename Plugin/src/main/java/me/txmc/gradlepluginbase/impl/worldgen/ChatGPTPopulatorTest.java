package me.txmc.gradlepluginbase.impl.worldgen;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.generator.BlockPopulator;

import java.util.Random;

public class ChatGPTPopulatorTest extends BlockPopulator {

    private static final int CHUNK_SIZE = 16;
    private static final int SEA_LEVEL = 64;
    private static final int VEIN_SIZE = 2; // Size of the diamond ore veins

    @Override
    public void populate(World world, Random random, Chunk chunk) {
        int x = chunk.getX() * CHUNK_SIZE;
        int z = chunk.getZ() * CHUNK_SIZE;

        // Generate diamond ore veins randomly across the chunk
        for (int i = 0; i < 10; i++) {
            int cx = x + random.nextInt(CHUNK_SIZE);
            int cz = z + random.nextInt(CHUNK_SIZE);
            int cy = random.nextInt(SEA_LEVEL - VEIN_SIZE); // Only generate below sea level

            generateVein(world, random, cx, cy, cz);
        }
    }

    private void generateVein(World world, Random random, int x, int y, int z) {
        // Generate a diamond ore vein at the given coordinates
        for (int dx = -VEIN_SIZE; dx <= VEIN_SIZE; dx++) {
            for (int dy = -VEIN_SIZE; dy <= VEIN_SIZE; dy++) {
                for (int dz = -VEIN_SIZE; dz <= VEIN_SIZE; dz++) {
                    if (random.nextInt(4) == 0) { // Randomly skip some blocks
                        Block block = world.getBlockAt(x + dx, y + dy, z + dz);
                        if (block.getType() == Material.STONE) { // Only replace stone blocks
                            block.setType(Material.DIAMOND_ORE);
                        }
                    }
                }
            }
        }
    }
}
