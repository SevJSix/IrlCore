package me.txmc.gradlepluginbase.packet.packetcommand.commands;

import lombok.SneakyThrows;
import me.txmc.gradlepluginbase.Main;
import me.txmc.gradlepluginbase.common.ContainerUtil;
import me.txmc.gradlepluginbase.common.NBTTools;
import me.txmc.gradlepluginbase.packet.packetcommand.PacketCommand;
import me.txmc.gradlepluginbase.packet.packetcommand.PacketCommandExecutor;
import me.txmc.gradlepluginbase.utils.Utils;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Player;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;

public class PacketCommandSaveChest extends PacketCommandExecutor {

    public PacketCommandSaveChest() {
        super("lootchest", "ADMIN ONLY COMMAND");
    }

    @SneakyThrows
    @Override
    public void onPacketCommand(PacketCommand command) {
        Player player = command.getSender();
        if (!player.isOp()) return;
        Location lookingAt = player.getTargetBlock((HashSet<Byte>) null, 10).getLocation();
        if (lookingAt != null) {
            World world = ((CraftWorld) lookingAt.getWorld()).getHandle();
            BlockPosition position = new BlockPosition(lookingAt.getX(), lookingAt.getY(), lookingAt.getZ());
            if (world.getTileEntity(position) != null && world.getTileEntity(position) instanceof TileEntityChest) {
                TileEntityChest chest = (TileEntityChest) world.getTileEntity(position);
                NBTTagCompound chestCompound = ContainerUtil.saveItemListToNBTTag(new NBTTagCompound(), Arrays.asList(chest.getContents()));
                String fileName = String.format("chest%s.nbt", Objects.requireNonNull(Main.getInstance().getChestNBTDataFolder().listFiles()).length);
                File chestNBTFile = new File(Main.getInstance().getChestNBTDataFolder(), fileName);
                if (!chestNBTFile.exists()) chestNBTFile.createNewFile();
                FileOutputStream fos = new FileOutputStream(chestNBTFile);
                DataOutputStream out = new DataOutputStream(fos);
//                NBTCompressedStreamTools.a(chestCompound, (OutputStream) out);
                NBTTools.writeNBT(chestCompound, out);
                out.flush();
                out.close();
                fos.close();
                Main.getInstance().getChestCompounds().add(chestCompound);
                Utils.sendMessage(player, "&aSuccess! generated " + chestNBTFile.getAbsolutePath());
            } else {
                Utils.sendMessage(player, "&cLook at a fucking chest please");
            }
        } else {
            Utils.sendMessage(player, "&cWhatever you're looking at is null");
        }
    }
}
