package me.txmc.gradlepluginbase.impl.bukkitcommand;

import me.txmc.gradlepluginbase.game.GameData;
import me.txmc.gradlepluginbase.game.games.UHCGame;
import me.txmc.gradlepluginbase.utils.Utils;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UHCLeaveCommand implements CommandExecutor, GameData {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (getPlayerGame(player) != null && getPlayerGame(player) instanceof UHCGame && player.getGameMode() == GameMode.SPECTATOR) {
                UHCGame game = (UHCGame) getPlayerGame(player);
                game.removeParticipant(player);
                Utils.sendMessage(player, "&aYou have left the match");
                Utils.handleLogoutSpot(player);
            } else {
                Utils.sendMessage(player, "&cCannot leave at this time");
            }
        }
        return true;
    }
}
