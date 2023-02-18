package me.txmc.gradlepluginbase.game.queue;

import lombok.SneakyThrows;
import me.txmc.gradlepluginbase.game.GameData;
import me.txmc.gradlepluginbase.game.MiniGame;
import me.txmc.gradlepluginbase.game.exception.PlayerAlreadyInGameException;
import me.txmc.gradlepluginbase.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;

public class QueueCommandRewrite implements CommandExecutor, GameData {

    @SneakyThrows
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;
        if (isPlayerInGame(player)) {
            Utils.sendMessage(player, "&cCannot use this command whilst in a game!");
            return true;
        }

        if (command.getName().equalsIgnoreCase("queue")) {
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("list")) {
                    if (queuedPlayers.size() > 0) {
                        StringBuilder builder = new StringBuilder();
                        builder.append("Queued Players (").append(queuedPlayers.size()).append("): ");
                        for (int i = 0; i < queuedPlayers.size(); i++) {
                            Player p = new ArrayList<>(queuedPlayers).get(i);
                            if (i == queuedPlayers.size() - 1) {
                                builder.append("&a").append(p.getName());
                            } else {
                                builder.append("&a").append(p.getName()).append("&r, ");
                            }
                        }
                        player.sendMessage(Utils.translateChars(builder.toString()));
                    } else {
                        Utils.sendMessage(player, "&cThere are currently no players in the queue");
                    }
                } else if (args[0].equalsIgnoreCase("all")) {
                    Bukkit.getOnlinePlayers().forEach(p -> {
                        if (!queuedPlayers.contains(p)) {
                            try {
                                queuePlayer(p, true);
                            } catch (PlayerAlreadyInGameException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                } else {
                    Utils.sendMessage(player, "&cInvalid argument. \"/queue list\" is the only argument that can be used");
                }
            } else {
                if (!queuedPlayers.contains(player)) {
                    queuePlayer(player, true);
                } else {
                    unQueuePlayer(player, true);
                    if (serverWideTeams.stream().anyMatch(team -> team.contains(player))) {
                        MiniGame.Team team = getPlayerTeam(player);
                        team.remove(player, true);
                        Utils.resetDisplayName(player);
                    }
                }
                return true;
            }
        }

        if (command.getName().equalsIgnoreCase("team")) {
            if (args[0].equalsIgnoreCase("join") || args[0].equalsIgnoreCase("create") || args[0].equalsIgnoreCase("leave") || args[0].equalsIgnoreCase("list")) {
                switch (args[0]) {
                    case "join":
                        if (args.length > 1) {
                            String teamName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                            if (!isPlayerInTeam(player)) {
                                if (serverWideTeams.stream().anyMatch(team -> team.getName().equalsIgnoreCase(teamName))) {
                                    MiniGame.Team team = getTeamByName(teamName);
                                    team.add(player, true);
                                    Utils.setDisplayName(team.getTeamColor(), player, teamName);
                                } else {
                                    Utils.sendMessage(player, String.format("&cSorry, but no team by the name of &a%s &cwas found", teamName));
                                }
                            } else {
                                Utils.sendMessage(player, String.format("&cAlready in a team! please type /team leave and then use /team join %s", teamName));
                            }
                        } else {
                            Utils.sendMessage(player, String.format("&cEnter a team name. /team %s [team_name]", args[0]));
                        }
                        break;
                    case "create":
                        if (args.length > 1) {
                            String teamNameToCreate = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                            if (teamNameToCreate.toCharArray().length > 16) {
                                Utils.sendMessage(player, String.format("&cCannot create a team with a name longer than 16 characters! Length inputted: %s", teamNameToCreate.toCharArray().length));
                                return true;
                            }
                            if (!isPlayerInTeam(player)) {
                                MiniGame.Team team = new MiniGame.Team(player, teamNameToCreate, player); // first param: creator of team to fire the team create event
                                serverWideTeams.add(team);
                                Utils.setDisplayName(team.getTeamColor(), player, team.getName());
                            } else {
                                Utils.sendMessage(player, String.format("&cAlready in a team! please type /team leave and then use /team create %s", teamNameToCreate));
                            }
                        } else {
                            Utils.sendMessage(player, String.format("&cEnter a team name. /team %s [team_name]", args[0]));
                        }
                        break;
                    case "leave":
                        if (isPlayerInTeam(player)) {
                            MiniGame.Team team = getPlayerTeam(player);
                            team.remove(player, true);
                            Utils.resetDisplayName(player);
                        } else {
                            Utils.sendMessage(player, "&cYou are currently in no team. therefore you cant leave any team.");
                        }
                        break;
                    case "list":
                        if (serverWideTeams.size() > 0) {
                            StringBuilder builder = new StringBuilder();
                            for (int i = 0; i < serverWideTeams.size(); i++) {
                                MiniGame.Team team = new ArrayList<>(serverWideTeams).get(i);
                                builder.append("&a").append(team.getName()).append(":&r").append("\n");
                                for (Player member : team.getMembers()) {
                                    builder.append(" &7- ").append(member.getName()).append("\n");
                                }
                            }
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', builder.toString()));
                        } else {
                            Utils.sendMessage(player, "&cThere are no teams at the moment");
                        }
                        break;
                }
            } else {
                Utils.sendMessage(player, "&cCommand Use: /team [join:create:leave:list] [team_name]");
                Utils.sendMessage(player, "&cNo need to include a team name when using /team leave or /team list");
            }
        }
        return true;
    }
}
