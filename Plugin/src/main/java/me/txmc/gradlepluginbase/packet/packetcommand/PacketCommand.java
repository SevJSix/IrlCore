package me.txmc.gradlepluginbase.packet.packetcommand;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
@Getter
public class PacketCommand {

    private final String[] args;
    private final String command;
    private final Player sender;
}
