package me.txmc.gradlepluginbase.impl.bukkitcommand;

import lombok.SneakyThrows;
import me.txmc.gradlepluginbase.Main;
import me.txmc.gradlepluginbase.common.NBTTools;
import me.txmc.gradlepluginbase.utils.Utils;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Objects;

public class SaveSkywarsSpawnLocation implements CommandExecutor {
    private final File spawnLocationsDataFolder;

    public SaveSkywarsSpawnLocation() {
        spawnLocationsDataFolder = new File(Main.getInstance().getDataFolder(), "SkywarsSpawnLocations");
        if (!spawnLocationsDataFolder.exists()) spawnLocationsDataFolder.mkdirs();
    }

    @SneakyThrows
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (player.isOp()) {
                Location location = new Location(player.getWorld(), player.getLocation().getBlockX() + 0.5, player.getLocation().getY(), player.getLocation().getBlockZ() + 0.5);
                NBTTagCompound chestCompound = Utils.saveLocationToNBT(location);
                String fileName = String.format("skywars_location%s.nbt", Objects.requireNonNull(spawnLocationsDataFolder.listFiles()).length);
                File chestNBTFile = new File(spawnLocationsDataFolder, fileName);
                if (!chestNBTFile.exists()) chestNBTFile.createNewFile();
                FileOutputStream fos = new FileOutputStream(chestNBTFile);
                DataOutputStream out = new DataOutputStream(fos);
                NBTTools.writeNBT(chestCompound, out);
                out.flush();
                out.close();
                fos.close();
                Utils.sendMessage(player, "&aSuccess! generated " + chestNBTFile.getAbsolutePath());
            } else {
                Utils.sendMessage(player, "&cNo permission");
            }
        }
        return true;
    }
}
