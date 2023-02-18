package me.txmc.gradlepluginbase.packet.packetcommand.commands;

import me.txmc.gradlepluginbase.packet.packetcommand.PacketCommand;
import me.txmc.gradlepluginbase.packet.packetcommand.PacketCommandExecutor;
import me.txmc.gradlepluginbase.utils.LogoutIO;
import me.txmc.gradlepluginbase.utils.LogoutSpot;
import me.txmc.gradlepluginbase.utils.Utils;
import org.bukkit.entity.Player;

public class PacketCommandLogoutSpotTest extends PacketCommandExecutor {
    public PacketCommandLogoutSpotTest() {
        super("logoutspot", "ADMIN ONLY COMMAND");
    }

    @Override
    public void onPacketCommand(PacketCommand command) {
        Player player = command.getSender();
        if (!player.isOp()) return;
        LogoutSpot spot = new LogoutSpot(player.getUniqueId(), player.getLocation());
        try {
            LogoutIO.saveLogoutSpot(spot);
            Utils.sendMessage(player, "&aSaved your current location and inventory to a file. You will be spawned here with your current inventory the next time you log in.");
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
