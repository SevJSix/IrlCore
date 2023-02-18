package me.txmc.gradlepluginbase.packet.packetcommand.commands;

import me.txmc.gradlepluginbase.packet.packetcommand.PacketCommand;
import me.txmc.gradlepluginbase.packet.packetcommand.PacketCommandExecutor;
import me.txmc.gradlepluginbase.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class PacketCommandSpectate extends PacketCommandExecutor {
    public PacketCommandSpectate() {
        super("spec", "spectate people");
    }

    @Override
    public void onPacketCommand(PacketCommand command) {
        Player player = command.getSender();
        if (player.getGameMode() != GameMode.SPECTATOR) {
            Utils.sendMessage(player, "&cYou must be in spectator mode to use this");
            return;
        }
        String[] args = command.getArgs();
        if (args.length > 0) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target != null && target.isOnline()) {
                if (target.getGameMode() == GameMode.SPECTATOR) {
                    Utils.sendMessage(player, "&cYou can't spectate someone who is a spectator");
                    return;
                }
                if (player.getSpectatorTarget() == target) {
                    player.setSpectatorTarget(null);
                    Utils.sendMessage(player, String.format("&cSTOPPED &3spectating %s", target.getName()));
                } else {
                    if (player.getSpectatorTarget() != null) player.setSpectatorTarget(null);
                    player.teleport(target);
                    player.setSpectatorTarget(target);
                    Utils.sendMessage(player, String.format("&aSTARTED &3specating %s", target.getName()));
                }
            } else {
                Utils.sendMessage(player, String.format("&c%s is not online", args[0]));
            }
        } else {
            Utils.sendMessage(player, "&cInclude a player to spectate");
        }
    }
}
