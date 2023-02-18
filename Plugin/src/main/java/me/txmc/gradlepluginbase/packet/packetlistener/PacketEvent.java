package me.txmc.gradlepluginbase.packet.packetlistener;

import lombok.Getter;
import net.minecraft.server.v1_8_R3.Packet;
import org.bukkit.entity.Player;

@Getter
public class PacketEvent {

    private final Player player;
    private Packet<?> packet;
    private final PacketEventType type;
    private boolean cancelled;

    public PacketEvent(Player player, PacketEventType type, Packet<?> packet) {
        this.player = player;
        this.type = type;
        this.packet = packet;
    }

    public void setPacket(Packet<?> packet) {
        this.packet = packet;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public enum PacketEventType {
        INCOMING,
        OUTGOING
    }
}
