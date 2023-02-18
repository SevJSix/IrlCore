package me.txmc.gradlepluginbase.packet.packetcommand;

import me.txmc.gradlepluginbase.Main;
import me.txmc.gradlepluginbase.packet.packetlistener.PacketEvent;
import me.txmc.gradlepluginbase.packet.packetlistener.PacketListener;
import me.txmc.gradlepluginbase.utils.Utils;
import net.minecraft.server.v1_8_R3.PacketPlayInChat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.Arrays;
import java.util.logging.Level;

public class PacketChatListener implements PacketListener {

    private final PacketCommandManager manager;

    public PacketChatListener(PacketCommandManager manager) {
        this.manager = manager;
    }

    @Override
    public void onPacket(PacketEvent event) {
        if (event.getPacket() instanceof PacketPlayInChat) {
            PacketPlayInChat packet = (PacketPlayInChat) event.getPacket();
            String rawMessage = packet.a();
            if (rawMessage.startsWith(".")) {
                event.setCancelled(true);
                String command = rawMessage.split(" ")[0].replace(".", "");
                if (manager.getPacketCommands().stream().anyMatch(pce -> pce.getCommand().equals(command))) {
                    String[] rawArgs = rawMessage.split(" ");
                    String[] args = Arrays.copyOfRange(rawArgs, 1, rawArgs.length);
                    PacketCommand packetCommand = new PacketCommand(args, command, event.getPlayer());
                    Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                        manager.dispatchCommand(packetCommand);
                        Main.getInstance().getLogger().log(Level.INFO, String.format("%s ran command: .%s %s", event.getPlayer().getName(), command, (args.length == 0) ? "" : Arrays.toString(args).replace("[", "").replace("]", "").replace(",", "")));
                    });
                } else {
                    Utils.sendMessage(event.getPlayer(), "&cUnknown Command. Type .help for all normal user commands");
                }
            } else if (!rawMessage.startsWith("/")) {
                event.setCancelled(true);
                String chatMessage = ChatColor.translateAlternateColorCodes('&', String.format("<%s> %s", event.getPlayer().getDisplayName(), rawMessage));
                Bukkit.getOnlinePlayers().forEach(player -> {
                    player.sendMessage(chatMessage);
                });
                Main.getInstance().getLogger().log(Level.INFO, chatMessage);
            }
        }
    }
}
