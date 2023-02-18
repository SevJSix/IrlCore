package me.txmc.gradlepluginbase.packet.packetcommand.commands;

import me.txmc.gradlepluginbase.packet.packetcommand.PacketCommand;
import me.txmc.gradlepluginbase.packet.packetcommand.PacketCommandExecutor;
import me.txmc.gradlepluginbase.utils.Utils;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.ItemStack;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class PacketCommandNBTTagAdder extends PacketCommandExecutor {

    public PacketCommandNBTTagAdder() {
        super("nbtadd", "ADMIN ONLY COMMAND");
    }

    @Override
    public void onPacketCommand(PacketCommand command) {
        Player player = command.getSender();
        if (!player.isOp()) return;
        String[] args = command.getArgs();
        if (args.length > 0) {
            String arg = args[0];
            EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
            ItemStack holding = entityPlayer.inventory.getItemInHand();
            if (holding != null) {
                NBTTagCompound compound = holding.getTag();
                if (compound == null) compound = new NBTTagCompound();
                compound.setString(arg, String.format("this is a %s nbt tag", arg));
                holding.setTag(compound);
                Utils.sendMessage(player, "&3Added &a" + arg + " &3to " + holding);
            }
        }
    }
}
