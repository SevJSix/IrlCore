package me.txmc.gradlepluginbase.impl.worldgen;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VoidWorldGenerator extends ChunkGenerator {

    List<BlockPopulator> populators = new ArrayList<>();

    @Override
    public List<BlockPopulator> getDefaultPopulators(World world) {
        return populators;
    }

    @Override
    public byte[][] generateBlockSections(World world, Random random, int x, int z, BiomeGrid biomes) {
        return getByteResult(new byte[world.getMaxHeight() / 16][]);
    }

    @Override
    public short[][] generateExtBlockSections(World world, Random random, int x, int z, BiomeGrid biomes) {
        return getShortResult(new short[world.getMaxHeight() / 16][]);
    }

    private byte[][] getByteResult(byte[][] result) {
        return result;
    }

    private short[][] getShortResult(short[][] result) {
        return result;
    }

    private void setBlock(byte[][] result, int x, int y, int z, byte blockId) {
        if (result[y >> 4] == null) {
            result[y >> 4] = new byte[4096];
        }
        result[y>>4][((y&0xF) << 8) | (z << 4) | x] = blockId;
    }

    private void setBlock(short[][] result, int x, int y, int z, byte blockId) {
        if (result[y >> 4] == null) {
            result[y >> 4] = new short[4096];
        }
        result[y>>4][((y&0xF) << 8) | (z << 4) | x] = blockId;
    }
}