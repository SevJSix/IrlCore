package me.txmc.gradlepluginbase.common.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.WorldBorder;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@RequiredArgsConstructor
@Getter
public class BorderUpdateEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final WorldBorder worldBorder;
    private final int trueSize;

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}