package me.txmc.gradlepluginbase.packet.packetcommand.commands;

import me.txmc.gradlepluginbase.packet.packetcommand.PacketCommand;
import me.txmc.gradlepluginbase.packet.packetcommand.PacketCommandExecutor;
import me.txmc.gradlepluginbase.utils.Utils;
import net.minecraft.server.v1_8_R3.Entity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Player;

public class PacketCommandEntityAI extends PacketCommandExecutor {

    public PacketCommandEntityAI() {
        super("disablemobai", "ADMIN ONLY COMMAND");
    }

    @Override
    public void onPacketCommand(PacketCommand command) {
        Player player = command.getSender();
        if (!player.isOp()) return;
        player.getNearbyEntities(30, 30, 30).stream().filter(entity -> entity != player).forEach(entity -> {
            Entity nmsEntity = ((CraftEntity) entity).getHandle();
            Utils.disableEntityAI(nmsEntity);
            player.sendMessage("Disabled entity AI for " + nmsEntity);
        });
    }
}
