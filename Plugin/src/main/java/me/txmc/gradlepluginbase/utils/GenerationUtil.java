package me.txmc.gradlepluginbase.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class GenerationUtil {

    public static void generate(Location location) {
        World world = location.getWorld();
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        // Generate the platform
        for (int i = x; i < x + 11; i++) {
            for (int j = z; j < z + 11; j++) {
                Block block = world.getBlockAt(i, y + 1, j);
                block.setType(Material.COBBLESTONE);
            }
        }
    }
}
