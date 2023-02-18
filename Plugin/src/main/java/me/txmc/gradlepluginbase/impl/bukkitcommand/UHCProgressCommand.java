package me.txmc.gradlepluginbase.impl.bukkitcommand;

import me.txmc.gradlepluginbase.game.GameData;
import me.txmc.gradlepluginbase.game.games.UHCGame;
import me.txmc.gradlepluginbase.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UHCProgressCommand implements CommandExecutor, GameData {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if (sender.hasPermission("uhc.progress")) {
            if (isAnyGameOngoing() && sender instanceof Player) {
                Player player = (Player) sender;
                if (getPlayerGame(player) instanceof UHCGame && strings.length > 0) {
                    UHCGame game = (UHCGame) getPlayerGame(player);
                    if (strings[0].equals("next")) {
                        game.setNext(true);
                        return true;
                    }
                    int toProgressAt = game.getNextProgressTick() - Integer.parseInt(strings[0]);
                    game.setNextProgressTick(toProgressAt);
                    Utils.sendMessage(player, "minigame will progress in " + toProgressAt + " ticks");
                }
                return true;
            }
        }
        return true;
    }
}