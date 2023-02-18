package me.txmc.gradlepluginbase.packet.packetcommand;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public abstract class PacketCommandExecutor {

    private final String command;
    private final String description;

    public abstract void onPacketCommand(PacketCommand command);
}
