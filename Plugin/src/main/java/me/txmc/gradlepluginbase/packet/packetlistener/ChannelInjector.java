package me.txmc.gradlepluginbase.packet.packetlistener;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import lombok.AllArgsConstructor;
import me.txmc.gradlepluginbase.Main;
import net.minecraft.server.v1_8_R3.Packet;
import org.bukkit.entity.Player;

@AllArgsConstructor
public class ChannelInjector extends ChannelDuplexHandler {

    private final Player player;
    private final Main plugin;

    @Override
    public void write(ChannelHandlerContext channelHandlerContext, Object o, ChannelPromise channelPromise) {
        try {
            PacketEvent event = new PacketEvent(player, PacketEvent.PacketEventType.OUTGOING, (Packet<?>) o);
            plugin.getManager().callEvent(event);
            if (event.isCancelled()) return;
            super.write(channelHandlerContext, event.getPacket(), channelPromise);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        try {
            PacketEvent event = new PacketEvent(player, PacketEvent.PacketEventType.INCOMING, (Packet<?>) o);
            plugin.getManager().callEvent(event);
            if (event.isCancelled()) return;
            super.channelRead(channelHandlerContext, event.getPacket());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
