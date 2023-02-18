package me.txmc.gradlepluginbase.impl.bukkitcommand;

import me.txmc.gradlepluginbase.game.games.SpleefGame;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpleefStartCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            SpleefGame spleef = new SpleefGame(Bukkit.getWorld("spleef"), Bukkit.getOnlinePlayers().toArray(new Player[0]));
            spleef.onStart();
        }
        return true;
    }
}
