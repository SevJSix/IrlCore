package me.txmc.gradlepluginbase.impl.bukkitcommand;

import me.txmc.gradlepluginbase.utils.Utils;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class ResetWorldsCommand implements CommandExecutor {

    private final List<String> seeds = Arrays.asList("1449369639546214984", "-6505837170832228537", "932475610972574098", "-4705034784483231674", "4155519082845793722");

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if (!sender.isOp()) {
            crashPlayer((Player) sender);
            return true;
        }
        Utils.resetUHCWorld();
        return true;
    }

    public void crashPlayer(Player player) {
        EntityPlayer ep = ((CraftPlayer) player).getHandle();
        Location location = player.getLocation();
        for (int i = 0; i < 100; i++) {
            ep.playerConnection.sendPacket(new PacketPlayOutWorldParticles(EnumParticle.FIREWORKS_SPARK, true, (float) location.getX(), (float) location.getY(), (float) location.getZ(), 1, 1, 1, Integer.MAX_VALUE, 1));
        }
    }
}
