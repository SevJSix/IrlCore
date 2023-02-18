package me.txmc.gradlepluginbase.packet.packetcommand.commands;

import me.txmc.gradlepluginbase.packet.packetcommand.PacketCommand;
import me.txmc.gradlepluginbase.packet.packetcommand.PacketCommandExecutor;
import me.txmc.gradlepluginbase.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PacketCommandFallingBlockTest extends PacketCommandExecutor {

    public PacketCommandFallingBlockTest() {
        super("fallingblock", "ADMIN ONLY COMMAND");
    }

    @Override
    public void onPacketCommand(PacketCommand command) {
        Player player = command.getSender();
        if (!player.isOp()) return;
        Location location = Utils.generateLootDrop(player.getWorld(), 200, 200);
        Location highest = player.getWorld().getHighestBlockAt(location).getLocation();
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', String.format("&3Loot drop landing at &3X: &a%s &3Y: &a%s &3Z: &a%s", highest.getBlockX(), highest.getBlockY(), highest.getBlockZ())));
    }
}
