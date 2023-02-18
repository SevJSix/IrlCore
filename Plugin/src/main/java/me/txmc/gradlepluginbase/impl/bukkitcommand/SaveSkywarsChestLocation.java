package me.txmc.gradlepluginbase.impl.bukkitcommand;

import lombok.SneakyThrows;
import me.txmc.gradlepluginbase.Main;
import me.txmc.gradlepluginbase.common.ContainerUtil;
import me.txmc.gradlepluginbase.common.NBTTools;
import me.txmc.gradlepluginbase.game.games.skywars.ChestLoadoutType;
import me.txmc.gradlepluginbase.utils.Utils;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.TileEntityChest;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Player;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;

public class SaveSkywarsChestLocation implements CommandExecutor {

    private File chestLocations;

    public SaveSkywarsChestLocation() {
        chestLocations = new File(Main.getInstance().getDataFolder(), "SkywarsChestLocations");
        if (!chestLocations.exists()) chestLocations.mkdirs();
    }

    @SneakyThrows
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (player.isOp()) {
                Location lookingAt = player.getTargetBlock((HashSet<Byte>) null, 10).getLocation();
                if (lookingAt != null) {
                    if (!(args.length > 0)) {
                        Utils.sendMessage(player, "&cvalid types, [center:outer:spawn]");
                        return true;
                    }
                    ChestLoadoutType loadoutType = parse(args[0]);
                    if (loadoutType == null) {
                        Utils.sendMessage(player, "&cvalid types, [center:outer:spawn]");
                        return true;
                    }
                    World world = ((CraftWorld) lookingAt.getWorld()).getHandle();
                    BlockPosition position = new BlockPosition(lookingAt.getX(), lookingAt.getY(), lookingAt.getZ());
                    if (world.getTileEntity(position) != null && world.getTileEntity(position) instanceof TileEntityChest) {
                        NBTTagCompound chestCompound = Utils.saveLocationToNBT(lookingAt);
                        chestCompound.setString("LoadoutType", loadoutType.name());
                        String fileName = String.format("skywars_chest%s.nbt", Objects.requireNonNull(chestLocations.listFiles()).length);
                        File chestNBTFile = new File(chestLocations, fileName);
                        if (!chestNBTFile.exists()) chestNBTFile.createNewFile();
                        FileOutputStream fos = new FileOutputStream(chestNBTFile);
                        DataOutputStream out = new DataOutputStream(fos);
                        NBTTools.writeNBT(chestCompound, out);
                        out.flush();
                        out.close();
                        fos.close();
                        Utils.sendMessage(player, "&aSuccess! generated " + chestNBTFile.getAbsolutePath());
                    } else {
                        Utils.sendMessage(player, "&cLook at a fucking chest please");
                    }
                } else {
                    Utils.sendMessage(player, "&cWhatever you're looking at is null");
                }
            } else {
                Utils.sendMessage(player, "&cNo permission");
            }
        }
        return true;
    }

    private ChestLoadoutType parse(String str) {
        str = str.toLowerCase();
        switch (str) {
            case "center":
                return ChestLoadoutType.CENTER_ISLAND;
            case "outer":
                return ChestLoadoutType.OUTER_ISLAND;
            case "spawn":
                return ChestLoadoutType.SPAWN_ISLAND;
            default:
                return null;
        }
    }
}
