package me.txmc.gradlepluginbase.game.queue.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.txmc.gradlepluginbase.game.MiniGame;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@RequiredArgsConstructor
@Getter
public class PlayerTeamEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final MiniGame.Team team;
    private final boolean announce;

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static class TeamJoin extends PlayerTeamEvent {

        public TeamJoin(Player player, MiniGame.Team team, boolean announce) {
            super(player, team, announce);
        }
    }

    public static class TeamLeave extends PlayerTeamEvent {

        public TeamLeave(Player player, MiniGame.Team team, boolean announce) {
            super(player, team, announce);
        }
    }

    public static class TeamCreate extends PlayerTeamEvent {

        public TeamCreate(Player player, MiniGame.Team team, boolean announce) {
            super(player, team, announce);
        }
    }
}
