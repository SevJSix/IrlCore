package me.txmc.gradlepluginbase.packet.packetcommand.commands;

import me.txmc.gradlepluginbase.packet.packetcommand.PacketCommand;
import me.txmc.gradlepluginbase.packet.packetcommand.PacketCommandExecutor;
import me.txmc.gradlepluginbase.utils.Utils;
import net.minecraft.server.v1_8_R3.ItemStack;
import net.minecraft.server.v1_8_R3.Items;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagString;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;

public class PacketCommandTNTBowGiver extends PacketCommandExecutor {
    public PacketCommandTNTBowGiver() {
        super("tntbow", "ADMIN ONLY COMMAND");
    }

    @Override
    public void onPacketCommand(PacketCommand command) {
        Player player = command.getSender();
        if (!player.isOp()) return;
        ItemStack bow = new ItemStack(Items.BOW);
        NBTTagCompound compound = new NBTTagCompound();
        compound.set("TNTBow", new NBTTagString("This bow is a valid tnt bow"));
        bow.setTag(compound);
        player.getInventory().addItem(CraftItemStack.asBukkitCopy(bow));
        if (player.getGameMode() == GameMode.SURVIVAL) {
            player.getInventory().addItem(new org.bukkit.inventory.ItemStack(Material.ARROW, 64));
        }
        Utils.sendMessage(player, "&aGave you a TNT bow");

    }
}
