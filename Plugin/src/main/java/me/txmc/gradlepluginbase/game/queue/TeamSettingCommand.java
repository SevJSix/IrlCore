package me.txmc.gradlepluginbase.game.queue;

import me.txmc.gradlepluginbase.game.GameData;
import me.txmc.gradlepluginbase.game.MiniGame;
import me.txmc.gradlepluginbase.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class TeamSettingCommand implements CommandExecutor, GameData {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (args.length > 0) {
            serverWideTeamSetting.setValue(MiniGame.TeamSetting.valueOf(args[0].toUpperCase()));
            Utils.sendMessage(sender, "&3Set the team setting to &a" + MiniGame.TeamSetting.valueOf(args[0]).name());
        }
        return true;
    }
}
