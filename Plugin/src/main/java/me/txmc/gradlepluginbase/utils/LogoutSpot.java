package me.txmc.gradlepluginbase.utils;

import lombok.Getter;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagList;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventoryPlayer;

import java.util.UUID;

@Getter
public class LogoutSpot {

    private final UUID playerUUID;
    private final Location location;
    private final NBTTagCompound inventoryDataTag;

    public LogoutSpot(UUID playerUUID, Location location) {
        this.playerUUID = playerUUID;
        this.location = location;
        this.inventoryDataTag = saveInventory();
    }

    public LogoutSpot(UUID playerUUID, Location location, NBTTagCompound inventoryDataTag) {
        this.playerUUID = playerUUID;
        this.location = location;
        this.inventoryDataTag = inventoryDataTag;
    }

    public NBTTagCompound saveInventory() {
        NBTTagCompound inventoryCompound = new NBTTagCompound();
        NBTTagList items = new NBTTagList();
        ((CraftInventoryPlayer) Bukkit.getPlayer(playerUUID).getInventory()).getInventory().a(items);
        inventoryCompound.set("InvContents", items);
        return inventoryCompound;
    }
}
