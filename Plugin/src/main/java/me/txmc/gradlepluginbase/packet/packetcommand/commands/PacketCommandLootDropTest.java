package me.txmc.gradlepluginbase.packet.packetcommand.commands;

import me.txmc.gradlepluginbase.packet.packetcommand.PacketCommand;
import me.txmc.gradlepluginbase.packet.packetcommand.PacketCommandExecutor;
import me.txmc.gradlepluginbase.utils.Utils;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PacketCommandLootDropTest extends PacketCommandExecutor {
    public PacketCommandLootDropTest() {
        super("spawnlootdrop", "ADMIN ONLY COMMAND");
    }

    @Override
    public void onPacketCommand(PacketCommand command) {
        Player player = command.getSender();
        if (!player.isOp()) return;
        Location location = Utils.generateLootDrop(player.getWorld(), 200, 200);
        player.sendMessage(String.format("%s, %s, %s", location.getX(), location.getY(), location.getZ()));
    }
}
