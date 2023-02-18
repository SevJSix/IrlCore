package me.txmc.gradlepluginbase.packet.packetcommand.commands;

import me.txmc.gradlepluginbase.packet.packetcommand.PacketCommand;
import me.txmc.gradlepluginbase.packet.packetcommand.PacketCommandExecutor;
import me.txmc.gradlepluginbase.utils.Utils;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class PacketCommandSeeNBTTag extends PacketCommandExecutor {
    public PacketCommandSeeNBTTag() {
        super("viewnbt", "view the nbt tag of the item in your hand");
    }

    @Override
    public void onPacketCommand(PacketCommand command) {
        Player player = command.getSender();
        EntityPlayer ep = ((CraftPlayer) player).getHandle();
        String nbt = String.valueOf(ep.inventory.getItemInHand() != null ? ep.inventory.getItemInHand().getTag() : "null");
        Utils.sendMessage(player, nbt);
        System.out.println(nbt);
    }
}
