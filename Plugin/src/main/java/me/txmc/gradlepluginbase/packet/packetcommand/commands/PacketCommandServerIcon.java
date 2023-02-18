package me.txmc.gradlepluginbase.packet.packetcommand.commands;

import me.txmc.gradlepluginbase.packet.packetcommand.PacketCommand;
import me.txmc.gradlepluginbase.packet.packetcommand.PacketCommandExecutor;
import me.txmc.gradlepluginbase.impl.listener.motd.IconManager;
import me.txmc.gradlepluginbase.utils.Utils;
import org.bukkit.entity.Player;

public class PacketCommandServerIcon extends PacketCommandExecutor {

    public PacketCommandServerIcon() {
        super("icon", "download a 64x64 server icon from a url");
    }

    @Override
    public void onPacketCommand(PacketCommand command) {
        Player player = command.getSender();
        String[] args = command.getArgs();
        if (args.length == 0) {
            Utils.sendMessage(player, "&cEnter a valid url");
            return;
        }
        IconManager.downloadIcon(args[0]).thenAcceptAsync(bool -> {
            if (bool) {
                Utils.sendMessage(player, "&aSuccessfully downloaded a server icon from &7" + args[0]);
            } else {
                Utils.sendMessage(player, "&cAn error occurred while downloading that icon. Check your link to make sure its a valid URL.");
            }
        });
    }
}
