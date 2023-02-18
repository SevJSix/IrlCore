package me.txmc.gradlepluginbase.game.queue.listener;

import me.txmc.gradlepluginbase.game.GameData;
import me.txmc.gradlepluginbase.game.MiniGame;
import me.txmc.gradlepluginbase.game.queue.event.PlayerQueueEvent;
import me.txmc.gradlepluginbase.game.queue.event.PlayerTeamEvent;
import me.txmc.gradlepluginbase.utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class QueueListeners implements Listener, GameData {

    private final String TEAMS_PREFIX = "&2&lTEAMS &r&7➠ &r";


    @EventHandler
    public void onQueueJoin(PlayerQueueEvent.PlayerQueueIn event) {
        if (!event.isAnnounce()) return;
        sendQueuedMessage(event.getPlayer(), false);
    }

    @EventHandler
    public void onQueueLeave(PlayerQueueEvent.PlayerUnQueue event) {
        if (!event.isAnnounce()) return;
        sendQueuedMessage(event.getPlayer(), true);
    }

    @EventHandler
    public void onTeamJoin(PlayerTeamEvent.TeamJoin event) {
        if (!event.isAnnounce()) return;
        sendTeamMessage(event.getPlayer(), event.getTeam(), false);
    }

    @EventHandler
    public void onTeamLeave(PlayerTeamEvent.TeamLeave event) {
        if (event.isAnnounce()) {
            MiniGame.Team team = event.getTeam();
            sendTeamMessage(event.getPlayer(), team, true);
            if (team.getMembers().size() == 0) {
                announceTeamDisbandment(team);
                serverWideTeams.remove(team);
            }
        }
        Utils.resetDisplayName(event.getPlayer());
    }

    @EventHandler
    public void onTeamCreate(PlayerTeamEvent.TeamCreate event) {
        if (!event.isAnnounce()) return;
        announceTeamCreation(event.getTeam(), event.getPlayer());
    }

    private void sendQueuedMessage(Player player, boolean removedFromQueue) {
        String message;
        String QUEUE_PREFIX = "&e&lQUEUE &r&7➠ &r";
        if (removedFromQueue) {
            message = String.format("%s&c%s left the queue.", QUEUE_PREFIX, player.getName());
        } else {
            message = String.format("%s&a%s joined the queue.", QUEUE_PREFIX, player.getName());
        }
        Utils.broadcastMessage(message);
    }

    private void sendTeamMessage(Player player, MiniGame.Team team, boolean removedFromTeam) {
        String message;
        if (removedFromTeam) {
            message = String.format("%s&3%s &cleft &3the team, &7%s", TEAMS_PREFIX, player.getName(), team.getName());
        } else {
            message = String.format("%s&3%s &ajoined &3the team, &7%s", TEAMS_PREFIX, player.getName(), team.getName());
        }
        Utils.broadcastMessage(message);
    }

    private void announceTeamCreation(MiniGame.Team team, Player creator) {
        String message;
        if (creator != null) {
            message = String.format("%s&6A new team was created by &3%s &6named &7%s", TEAMS_PREFIX, creator.getName(), team.getName());
        } else {
            message = String.format("%s&6A new team was created &6named &7%s", TEAMS_PREFIX, team.getName());
        }
        Utils.broadcastMessage(message);
    }

    private void announceTeamDisbandment(MiniGame.Team team) {
        Utils.broadcastMessage(String.format("%s&3The team, &7%s&r&3, was &cdisbanded", TEAMS_PREFIX, team.getName()));
    }
}
