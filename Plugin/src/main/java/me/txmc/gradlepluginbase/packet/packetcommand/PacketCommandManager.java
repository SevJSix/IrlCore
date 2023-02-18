package me.txmc.gradlepluginbase.packet.packetcommand;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PacketCommandManager {

    @Getter
    private final List<PacketCommandExecutor> packetCommands;

    public PacketCommandManager() {
        this.packetCommands = new ArrayList<>();
    }

    public void registerCommand(PacketCommandExecutor command) {
        packetCommands.add(command);
    }

    public void dispatchCommand(PacketCommand command) {
        packetCommands.forEach(cmd -> {
            if (Objects.equals(cmd.getCommand(), command.getCommand())) {
                cmd.onPacketCommand(command);
            }
        });
    }
}
