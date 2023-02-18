package me.txmc.gradlepluginbase.packet.packetcommand.commands;

import me.txmc.gradlepluginbase.game.GameData;
import me.txmc.gradlepluginbase.game.games.UHCGame;
import me.txmc.gradlepluginbase.packet.packetcommand.PacketCommand;
import me.txmc.gradlepluginbase.packet.packetcommand.PacketCommandExecutor;
import me.txmc.gradlepluginbase.utils.Utils;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class PacketCommandHeal extends PacketCommandExecutor implements GameData {
    public PacketCommandHeal() {
        super("heal", "heal yourself to full health and hunger");
    }

    private final HashMap<Player, Integer> tickMap = new HashMap<>();

    @Override
    public void onPacketCommand(PacketCommand command) {
        Player player = command.getSender();
        if (getPlayerGame(player) != null && getPlayerGame(player) instanceof UHCGame) {
            Utils.sendMessage(player, "&cCant fucking heal in uhc retard");
            return;
        }
        MinecraftServer minecraftServer = MinecraftServer.getServer();
        int currentTick = minecraftServer.at();
        int nextTick = 12000;
        if (!tickMap.containsKey(player)) {
            tickMap.put(player, currentTick);
            heal(player);
        } else if (tickMap.get(player) + nextTick - currentTick < 0) {
            heal(player);
            tickMap.replace(player, currentTick);
        } else {
            Utils.sendMessage(player, String.format("&3You must wait &a%s &3 to heal again", Utils.getElapsedTimeFromTicks(Utils.getCurrentTick(), tickMap.get(player) + nextTick)));
        }
    }

    private void heal(Player player) {
        player.setHealth(20);
        player.setFoodLevel(20);
        Utils.sendMessage(player, "&aHealed. Thanks to HungerFF");
    }
}
