package me.txmc.gradlepluginbase.packet.packetcommand.commands;

import lombok.SneakyThrows;
import me.txmc.gradlepluginbase.Main;
import me.txmc.gradlepluginbase.packet.packetcommand.PacketCommand;
import me.txmc.gradlepluginbase.packet.packetcommand.PacketCommandExecutor;
import me.txmc.gradlepluginbase.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;

public class PacketCommandMotd extends PacketCommandExecutor {

    public PacketCommandMotd() {
        super("motd", "add a message of the day to the server");
    }

    @Override
    public void onPacketCommand(PacketCommand command) {
        String[] args = command.getArgs();
        Player player = command.getSender();
        if (args.length == 0) {
            Utils.sendMessage(player, "&cMake sure you type an motd.");
            return;
        }
        String motd = ChatColor.translateAlternateColorCodes('&', String.join(" ", args));
        addMotd(motd);
        Utils.sendMessage(player, "&aAdded Motd: &r" + motd);
    }

    @SneakyThrows
    public void addMotd(String motd) {
        List<String> motds = Main.getInstance().getConfig().getStringList("Motds");
        motds.add(motd);
        Main.getInstance().getConfig().set("Motds", motds);
        Main.getInstance().saveConfig();
        Main.getInstance().reloadConfig();
    }
}
