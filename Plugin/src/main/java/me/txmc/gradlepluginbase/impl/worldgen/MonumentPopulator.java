package me.txmc.gradlepluginbase.impl.worldgen;

import com.sk89q.worldedit.WorldEdit;
import lombok.SneakyThrows;
import me.txmc.gradlepluginbase.Main;
import me.txmc.gradlepluginbase.utils.WorldEditUtils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.generator.BlockPopulator;

import java.util.Random;
import java.util.logging.Level;

public class MonumentPopulator extends BlockPopulator {

    @Override
    public void populate(World world, Random random, Chunk chunk) {
        if (random.nextInt(60) == 0) {
            Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
                if (!chunk.isLoaded()) chunk.load();
                placeMonument(chunk, random);
            }, (20 * 2L));
        }
    }

    @SneakyThrows
    private void placeMonument(Chunk chunk, Random random) {
        Block block = chunk.getWorld().getHighestBlockAt((chunk.getX() * 16) + 8, (chunk.getZ() * 16) + 8);
        String schematicName = null;
        Location pasteLocation = block.getLocation();
        switch (block.getBiome()) {
            case FOREST:
            case FOREST_HILLS:
            case BIRCH_FOREST:
            case ROOFED_FOREST:
                if (random.nextInt(4) == 0) {
                    schematicName = "shrine";
                    pasteLocation = block.getLocation().clone().add(0, 45, 0);
                }
                break;
            case PLAINS:
            case DESERT:
            case MESA:
            case SAVANNA:
                if (random.nextInt(20) == 0) {
                    schematicName = "dolfohouse";
                } else if (random.nextInt(7) == 0) {
                    schematicName = "brandonvault";
                } else if (random.nextInt(5) == 0) {
                    schematicName = "skatepark";
                } else if (random.nextInt(3) == 0) {
                    schematicName = "herobrinetemple";
                } else if (random.nextInt(2) == 0) {
                    schematicName = "craftbar";
                }
        }
        if (schematicName != null) {
            WorldEditUtils.SchematicResult result = WorldEditUtils.pasteSchematic(chunk.getWorld(), schematicName, pasteLocation);
            if (result == WorldEditUtils.SchematicResult.SUCCESS) {
                Main.getInstance().getLogger().log(Level.INFO, "Pasted a monument at chunk " + chunk);
            } else if (result == WorldEditUtils.SchematicResult.FAILED) {
                Main.getInstance().getLogger().log(Level.INFO, "Failed to paste a monument at chunk " + chunk);
            }
        }
    }
}
