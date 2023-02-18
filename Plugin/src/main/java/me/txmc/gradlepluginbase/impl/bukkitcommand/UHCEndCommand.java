package me.txmc.gradlepluginbase.impl.bukkitcommand;

import me.txmc.gradlepluginbase.game.GameData;
import me.txmc.gradlepluginbase.game.games.UHCGame;
import me.txmc.gradlepluginbase.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.stream.Collectors;

public class UHCEndCommand implements CommandExecutor, GameData {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if (sender.hasPermission("uhc.end")) {
            if (isAnyGameOngoing()) {
                List<UHCGame> uhcGames = serverWideMinigames.stream().filter(miniGame -> miniGame instanceof UHCGame).map(miniGame -> (UHCGame) miniGame).collect(Collectors.toList());
                uhcGames.forEach(UHCGame::onEnd);
                Utils.sendMessage(sender, "&aSuccessfully stopped UHC games");
            } else {
                Utils.sendMessage(sender, "&cThere is no UHC game in progress to end");
            }
        }
        return true;
    }
}
