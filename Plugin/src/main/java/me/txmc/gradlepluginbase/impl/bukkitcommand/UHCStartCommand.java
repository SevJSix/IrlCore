package me.txmc.gradlepluginbase.impl.bukkitcommand;

import me.txmc.gradlepluginbase.game.GameData;
import me.txmc.gradlepluginbase.game.games.UHCGame;
import me.txmc.gradlepluginbase.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class UHCStartCommand implements CommandExecutor, GameData {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (sender.hasPermission("uhc.start")) {
            if (isAnyGameOngoing()) {
                Utils.sendMessage(sender, "&cThere is already a UHC match in progress");
                return true;
            }
            World world = Bukkit.getWorld("uhc");
            List<Player> queued = new ArrayList<>(queuedPlayers);
            UHCGame match = new UHCGame(world, queued);
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("randomteams")) {
                    match.createRandomTeams(queued);
                } else if (args[0].equalsIgnoreCase("redvsblue")) {
                    match.genRedVsBlueTeams(queued);
                }
            }
            match.onStart();
            Utils.sendMessage(sender, "&aUHC Match Started");
        } else {
            Utils.sendMessage(sender, "&cYou are not permitted to start a UHC match");
        }
        return true;
    }
}
