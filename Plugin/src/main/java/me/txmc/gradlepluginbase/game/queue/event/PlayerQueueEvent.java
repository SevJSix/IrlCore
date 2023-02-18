package me.txmc.gradlepluginbase.game.queue.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.server.v1_8_R3.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@RequiredArgsConstructor
@Getter
public class PlayerQueueEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final boolean announce;

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static class PlayerUnQueue extends PlayerQueueEvent {

        public PlayerUnQueue(Player player, boolean announce) {
            super(player, announce);
        }
    }

    public static class PlayerQueueIn extends PlayerQueueEvent {

        public PlayerQueueIn(Player player, boolean announce) {
            super(player, announce);
        }
    }
}
