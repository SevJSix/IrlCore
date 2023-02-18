package me.txmc.gradlepluginbase.packet.packetcommand.commands;

import me.txmc.gradlepluginbase.packet.packetcommand.PacketCommand;
import me.txmc.gradlepluginbase.packet.packetcommand.PacketCommandExecutor;
import me.txmc.gradlepluginbase.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class PacketCommandMessage extends PacketCommandExecutor {
    public PacketCommandMessage() {
        super("msg", "message a player a private message");
    }

    @Override
    public void onPacketCommand(PacketCommand command) {
        String[] args = command.getArgs();
        Player player = command.getSender();
        Player target = Bukkit.getPlayer(args[0]);
        if (target.isOnline()) {
            String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            if (message.isEmpty()) {
                Utils.sendMessage(player, "&cYou cannot send an empty message");
            } else {
                player.sendMessage(ChatColor.LIGHT_PURPLE + "To " + target.getName() + ": " + message);
                target.sendMessage(ChatColor.LIGHT_PURPLE + "From " + player.getName() + ": " + message);
            }
        } else {
            Utils.sendMessage(player, "&cThat player is not online. Check your message to see you are sending to the right player");
        }
    }
}
