package me.txmc.gradlepluginbase.utils;

import lombok.SneakyThrows;
import me.txmc.gradlepluginbase.Main;
import me.txmc.gradlepluginbase.common.NBTTools;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.*;
import java.util.Objects;
import java.util.UUID;

public class LogoutIO {

    private static final File syncDataFolder;

    static {
        syncDataFolder = new File(Main.getInstance().getDataFolder(), "Sync");
        if (!syncDataFolder.exists()) syncDataFolder.mkdirs();
    }

    public static void saveLogoutSpot(LogoutSpot spot) {
        try {
            if (spot == null) return;
            File logFile = new File(syncDataFolder, spot.getPlayerUUID().toString() + ".nbt");
            if (!logFile.exists()) logFile.createNewFile();
            NBTTagCompound logoutCompound = new NBTTagCompound();
            logoutCompound.set("Location", Utils.saveLocationToNBT(spot.getLocation()));
            logoutCompound.set("SavedInventory", spot.getInventoryDataTag());
            FileOutputStream fos = new FileOutputStream(logFile);
            DataOutputStream out = new DataOutputStream(fos);
            NBTTools.writeNBT(logoutCompound, out);
            out.flush();
            out.close();
            fos.close();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void deleteLogoutSpot(UUID playerUUID) {
        for (File file : Objects.requireNonNull(syncDataFolder.listFiles())) {
            if (file.getName().split("\\.")[0].equals(playerUUID.toString())) {
                file.delete();
            }
        }
    }

    @SneakyThrows
    public static LogoutSpot loadLogoutSpot(UUID playerUUID) {
        File logFile = new File(syncDataFolder, playerUUID.toString() + ".nbt");
        if (!logFile.exists()) return null;
        NBTTagCompound tag = loadNBTSafely(logFile);
        NBTTagCompound locationTag = tag.getCompound("Location");
        Location location = new Location(Bukkit.getWorld(locationTag.getString("world")), locationTag.getDouble("x"), locationTag.getDouble("y"), locationTag.getDouble("z"),
                locationTag.getFloat("yaw"), locationTag.getFloat("pitch"));
        return new LogoutSpot(playerUUID, location, tag.getCompound("SavedInventory"));
    }

    public static NBTTagCompound loadNBTSafely(File file) throws Throwable {
        FileInputStream fis = new FileInputStream(file);
        DataInputStream in = new DataInputStream(fis);
        NBTTagCompound compound = NBTTools.readNBT(in);
        in.close();
        fis.close();
        return compound;
    }
}