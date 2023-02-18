package me.txmc.gradlepluginbase.packet.packetlistener;

import java.util.ArrayList;
import java.util.List;

public class PacketManager {

    private final List<PacketListener> listeners = new ArrayList<>();

    public void registerPacketListener(PacketListener listener) {
        listeners.add(listener);
    }

    public void callEvent(PacketEvent event) {
        listeners.forEach(listener -> listener.onPacket(event));
    }
}
