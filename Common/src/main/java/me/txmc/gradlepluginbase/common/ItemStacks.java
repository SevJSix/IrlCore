package me.txmc.gradlepluginbase.common;

import net.minecraft.server.v1_8_R3.ItemStack;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;

public class ItemStacks {

    public static final ItemStack STONE = genStack(Material.STONE);
    public static final ItemStack DIRT = genStack(Material.DIRT);
    public static final ItemStack COBBLESTONE = genStack(Material.COBBLESTONE);
    public static final ItemStack OAK_LOG = genStack(Material.LOG);
    public static final ItemStack SPRUCE_LOG = genStack(Material.LOG, 1, 1);
    public static final ItemStack BIRCH_LOG = genStack(Material.LOG, 1, 2);
    public static final ItemStack JUNGLE_LOG = genStack(Material.LOG, 1, 3);
    public static final ItemStack BOW = genStack(Material.BOW);
    public static final ItemStack ARROW = genStack(Material.ARROW);
    public static final ItemStack COAL = genStack(Material.COAL);
    public static final ItemStack DIAMOND = genStack(Material.DIAMOND);
    public static final ItemStack IRON_INGOT = genStack(Material.IRON_INGOT);
    public static final ItemStack GOLD_INGOT = genStack(Material.GOLD_INGOT);
    public static final ItemStack STICK = genStack(Material.STICK);
    public static final ItemStack GRAVEL = genStack(Material.GRAVEL);
    public static final ItemStack FLINT = genStack(Material.FLINT);
    public static final ItemStack GUNPOWDER = genStack(Material.SULPHUR);
    public static final ItemStack EMERALD = genStack(Material.EMERALD);
    public static final ItemStack BLAZE_ROD = genStack(Material.BLAZE_ROD);
    public static final ItemStack GOD_APPLE = genStack(Material.GOLDEN_APPLE, 1, 1);
    public static final ItemStack ANVIL = genStack(Material.ANVIL);
    public static final ItemStack BEDROCK = genStack(Material.BEDROCK);
    public static final ItemStack SAND = genStack(Material.SAND);
    public static final ItemStack IRON_PICKAXE = genStack(Material.IRON_PICKAXE);
    public static final ItemStack ENDER_PEARL = genStack(Material.ENDER_PEARL);
    public static final ItemStack NETHER_STAR = genStack(Material.NETHER_STAR);

    public static net.minecraft.server.v1_8_R3.ItemStack genStack(Material material, int count, int data) {
        return new net.minecraft.server.v1_8_R3.ItemStack(CraftMagicNumbers.getItem(material), count, data);
    }

    public static net.minecraft.server.v1_8_R3.ItemStack genStack(Material material, int count) {
        return new net.minecraft.server.v1_8_R3.ItemStack(CraftMagicNumbers.getItem(material), count);
    }

    public static net.minecraft.server.v1_8_R3.ItemStack genStack(Material material) {
        return new net.minecraft.server.v1_8_R3.ItemStack(CraftMagicNumbers.getItem(material));
    }
}
