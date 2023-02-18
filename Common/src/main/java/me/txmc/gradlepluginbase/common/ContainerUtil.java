package me.txmc.gradlepluginbase.common;

import lombok.SneakyThrows;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class ContainerUtil {

    public static NBTTagCompound saveItemListToNBTTag(NBTTagCompound tagToSaveTo, List<ItemStack> itemList) {
        NBTTagList newTagList = new NBTTagList();
        for(int i = 0; i < itemList.size(); ++i) {
            ItemStack item = itemList.get(i);
            if (item != null && !item.getItem().equals(CraftMagicNumbers.getItem(Material.AIR)) && !CommonUtils.isItemstackEmpty(item)) {
                NBTTagCompound comp = new NBTTagCompound();
                comp.setByte("Slot", (byte)i);
                item.save(comp);
                newTagList.add(comp);
            }
        }
        if (!newTagList.isEmpty()) {
            tagToSaveTo.set("Items", newTagList);
        }
        return tagToSaveTo;
    }

    @SneakyThrows
    public static void setChestData(NBTTagCompound chestCompound, TileEntityChest chest, BlockPosition position) {
        if (chest != null) {
            setPos(chestCompound, position);
            chest.a(chestCompound);
            chest.b(chestCompound);
            chest.update();
        }
    }

    private static void setPos(NBTTagCompound tag, BlockPosition pos) {
        tag.setInt("x", pos.getX());
        tag.setInt("y", pos.getY());
        tag.setInt("z", pos.getZ());
    }
}
