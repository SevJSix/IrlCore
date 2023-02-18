package me.txmc.gradlepluginbase.packet.packetcommand.commands;

import me.txmc.gradlepluginbase.packet.packetcommand.PacketCommand;
import me.txmc.gradlepluginbase.packet.packetcommand.PacketCommandExecutor;
import me.txmc.gradlepluginbase.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class PacketCommandNickName extends PacketCommandExecutor {
    public PacketCommandNickName() {
        super("nick", "give yourself a custom nick name");
    }

    @Override
    public void onPacketCommand(PacketCommand command) {
        String[] args = command.getArgs();
        Player player = command.getSender();
        if (args.length == 0) {
            Utils.sendMessage(player, "&cNickname cannot be empty");
            return;
        }
        String nickName = ChatColor.translateAlternateColorCodes('&', args[0]);
        if (nickName.isEmpty()) {
            Utils.sendMessage(player, "&cNickname cannot be empty");
            return;
        }
        player.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&r~" + nickName + "&r"));
        Utils.sendMessage(player, "&aSet your nick name to &r~" + nickName);
    }
}
