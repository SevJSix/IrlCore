package me.txmc.gradlepluginbase.common.npc;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.scoreboard.CraftScoreboard;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class NPC {

    private Location location;
    private String name;
    private GameProfile gameProfile;
    private EntityPlayer entityPlayer;
    private String texture;
    private String signature;

    public NPC(Location location, String name, String texture, String signature) {
        this.location = location;
        this.name = name;
        this.texture = texture;
        this.signature = signature;
    }

    public void spawnIn() {
        MinecraftServer server = MinecraftServer.getServer();
        WorldServer worldServer = ((CraftWorld) location.getWorld()).getHandle();
        this.gameProfile = new GameProfile(UUID.randomUUID(), ChatColor.translateAlternateColorCodes('&', name));
        this.gameProfile.getProperties().put("textures", new Property("textures", texture, signature));
        this.entityPlayer = new EntityPlayer(server, worldServer, gameProfile, new PlayerInteractManager(worldServer));
        this.entityPlayer.setLocation(location.getX(), location.getY(), location.getZ(), 0.0f, 0.0f);
        this.entityPlayer.playerConnection = new PlayerConnection(server, new NetworkManager(EnumProtocolDirection.CLIENTBOUND), entityPlayer);
        worldServer.addEntity(entityPlayer);
        Bukkit.getOnlinePlayers().forEach(this::show);
    }

    public void setNameInvisible() {
        ScoreboardTeam team = new ScoreboardTeam(((CraftScoreboard) Bukkit.getScoreboardManager().getMainScoreboard()).getHandle(), entityPlayer.getName());
        team.setNameTagVisibility(ScoreboardTeamBase.EnumNameTagVisibility.NEVER);
        List<String> playerToAdd = new ArrayList<>();
        playerToAdd.add(entityPlayer.getName());
        Bukkit.getOnlinePlayers().forEach(player -> {
            PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
            connection.sendPacket(new PacketPlayOutScoreboardTeam(team, 1));
            connection.sendPacket(new PacketPlayOutScoreboardTeam(team, 0));
            connection.sendPacket(new PacketPlayOutScoreboardTeam(team, playerToAdd, 3));
        });
    }

    public void remove() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
            connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, entityPlayer));
            PacketPlayOutEntityDestroy destroyPacket = new PacketPlayOutEntityDestroy(entityPlayer.getId());
            connection.sendPacket(destroyPacket);
        });
        entityPlayer.die();
    }

    public void show(Player player) {
        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
        connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, entityPlayer));
        connection.sendPacket(new PacketPlayOutNamedEntitySpawn(entityPlayer));
        connection.sendPacket(new PacketPlayOutEntityHeadRotation(entityPlayer, (byte) ((location.getYaw() * 256f) / 360f)));
    }
}
