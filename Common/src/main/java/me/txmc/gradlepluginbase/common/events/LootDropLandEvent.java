package me.txmc.gradlepluginbase.common.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.World;
import net.minecraft.server.v1_8_R3.WorldBorder;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@RequiredArgsConstructor
@Getter
public class LootDropLandEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final World world;
    private final BlockPosition position;

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}