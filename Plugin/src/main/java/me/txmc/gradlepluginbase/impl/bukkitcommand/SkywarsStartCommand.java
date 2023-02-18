package me.txmc.gradlepluginbase.impl.bukkitcommand;

import me.txmc.gradlepluginbase.game.games.SkywarsGame;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SkywarsStartCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        SkywarsGame game = new SkywarsGame(Bukkit.getWorld("skywars"), Bukkit.getOnlinePlayers().toArray(new Player[0]));
        game.onStart();
        return true;
    }
}
