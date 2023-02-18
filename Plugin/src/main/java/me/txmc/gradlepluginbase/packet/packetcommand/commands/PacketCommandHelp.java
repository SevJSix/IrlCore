package me.txmc.gradlepluginbase.packet.packetcommand.commands;

import me.txmc.gradlepluginbase.Main;
import me.txmc.gradlepluginbase.packet.packetcommand.PacketCommand;
import me.txmc.gradlepluginbase.packet.packetcommand.PacketCommandExecutor;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;

public class PacketCommandHelp extends PacketCommandExecutor {
    public PacketCommandHelp() {
        super("help", "shows all available user commands");
    }

    @Override
    public void onPacketCommand(PacketCommand command) {
        Player player = command.getSender();
        StringBuilder builder = new StringBuilder();
        builder.append("\n");
        List<PacketCommandExecutor> commandsAvailable = Main.getInstance().getPacketCommandManager().getPacketCommands();
        if (command.getArgs().length > 0 && command.getArgs()[0].equals("admin")) {
            for (PacketCommandExecutor cmd : commandsAvailable) {
                if (isAdminCommand(cmd)) {
                    builder.append("&7.").append(cmd.getCommand()).append(" &3- &a").append(cmd.getDescription()).append("\n");
                }
            }
        } else {
            for (PacketCommandExecutor cmd : commandsAvailable) {
                if (!isAdminCommand(cmd)) {
                    builder.append("&7.").append(cmd.getCommand()).append(" &3- &a").append(cmd.getDescription()).append("\n");
                }
            }
        }
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', builder.toString()));
    }

    private boolean isAdminCommand(PacketCommandExecutor cmd) {
        return cmd.getDescription().equals("ADMIN ONLY COMMAND");
    }
}
