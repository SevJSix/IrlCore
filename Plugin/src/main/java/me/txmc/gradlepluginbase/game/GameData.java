package me.txmc.gradlepluginbase.game;

import me.txmc.gradlepluginbase.game.exception.PlayerAlreadyInGameException;
import me.txmc.gradlepluginbase.game.games.UHCGame;
import me.txmc.gradlepluginbase.game.queue.event.PlayerQueueEvent;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public interface GameData {
    Set<Player> queuedPlayers = new HashSet<>();
    Set<MiniGame.Team> serverWideTeams = new HashSet<>();
    Set<MiniGame> serverWideMinigames = new HashSet<>();
    AtomicTeamSetting serverWideTeamSetting = new AtomicTeamSetting(MiniGame.TeamSetting.NONE);

    default boolean isGameOngoing(MiniGame game) {
        return game != null && game.isOngoing();
    }

    default boolean isAnyGameOngoing() {
        for (MiniGame miniGame : serverWideMinigames) {
            if (miniGame.isOngoing()) return true;
        }
        return false;
    }

    default UHCGame getUHCGame() {
        return serverWideMinigames.stream().filter(miniGame -> miniGame instanceof UHCGame).map(miniGame -> (UHCGame) miniGame).findAny().orElse(null);
    }

    default boolean isPlayerInGame(Player player) {
        for (MiniGame ongoingGame : serverWideMinigames) {
            if (ongoingGame.getParticipants().contains(player)) return true;
        }
        return false;
    }

    default MiniGame.Team getTeamByName(String teamName) {
        for (MiniGame.Team serverTeam : serverWideTeams) {
            if (serverTeam.getName().equalsIgnoreCase(teamName)) {
                return serverTeam;
            }
        }
        return null;
    }

    default MiniGame getPlayerGame(Player player) {
        MiniGame miniGame = null;
        for (MiniGame game : serverWideMinigames) {
            if (game.getParticipants().contains(player)) miniGame = game;
        }
        return miniGame;
    }

    default boolean isPlayerInTeam(Player player) {
        for (MiniGame.Team team : serverWideTeams) {
            if (team.getMembers().contains(player)) return true;
        }
        return false;
    }

    default MiniGame getGameFromWorld(World world) {
        for (MiniGame game : serverWideMinigames) {
            if (game.getWorld().equals(world)) return game;
        }
        return null;
    }

    default MiniGame.Team getPlayerTeam(Player player) {
        for (MiniGame.Team team : serverWideTeams) {
            if (team.getMembers().contains(player)) return team;
        }
        return null;
    }

    default void resetMinigameData() {
        queuedPlayers.clear();
        serverWideTeams.clear();
        serverWideMinigames.clear();
    }

    default boolean areAllTeamMembersInGame(MiniGame.Team team) {
        int i = 0;
        boolean allInGame = true;
        while (i < team.getMembers().size()) {
            if (!isPlayerInGame(new ArrayList<>(team.getMembers()).get(i))) {
                allInGame = false;
                break;
            }
            i++;
        }
        return allInGame;
    }

    default void queuePlayer(Player player, boolean announce) throws PlayerAlreadyInGameException {
        if (isPlayerInGame(player))
            throw new PlayerAlreadyInGameException(String.format("%s is already in a game!", player.getName()));
        queuedPlayers.add(player);
        PlayerQueueEvent.PlayerQueueIn event = new PlayerQueueEvent.PlayerQueueIn(player, announce);
        Bukkit.getPluginManager().callEvent(event);
    }

    default boolean isQueued(Player player) {
        return queuedPlayers.contains(player);
    }

    default void unQueuePlayer(Player player, boolean announce) {
        queuedPlayers.remove(player);
        PlayerQueueEvent.PlayerUnQueue event = new PlayerQueueEvent.PlayerUnQueue(player, announce);
        Bukkit.getPluginManager().callEvent(event);
    }
}
