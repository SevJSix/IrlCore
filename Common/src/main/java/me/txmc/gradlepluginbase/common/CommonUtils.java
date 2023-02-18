package me.txmc.gradlepluginbase.common;

import lombok.SneakyThrows;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class CommonUtils {

    @SneakyThrows
    public static boolean isItemstackEmpty(net.minecraft.server.v1_8_R3.ItemStack itemStack) {
        Item airItem = Item.REGISTRY.get(new MinecraftKey("air"));
        return itemStack == null || itemStack.getItem() == airItem || itemStack.count <= 0;
    }

    public static List<Player> getNearbyPlayers(Location origin, double radius) {
        return origin.getWorld().getNearbyEntities(origin, radius, radius, radius).stream().filter(entity -> entity instanceof Player).map(entity -> (Player) entity).collect(Collectors.toList());
    }

    public static void resetHealth(Player player) {
        if (player.getMaxHealth() != 20) player.setMaxHealth(20);
        player.setHealth(20);
        player.setFoodLevel(20);
    }

    public static BlockPosition toBlockPos(Location location) {
        return new BlockPosition(location.getX(), location.getY(), location.getZ());
    }

    public static void playSound(Player player, Sound sound) {
        player.playSound(player.getLocation(), sound, 10f, 1f);
    }

    public static Block getBlockAtFeet(Player player) {
        Location location = player.getLocation();
        int attempts = 0;
        while (isAir(location)) {
            if (attempts > 500) break;
            location = location.getBlock().getRelative(BlockFace.DOWN).getLocation();
            attempts++;
        }
        return location.getBlock();
    }

    public static boolean isAir(Location location) {
        return location.getBlock().getType() == Material.AIR;
    }

    public static EntityPlayer getNMSPlayer(Player player) {
        return ((CraftPlayer) player).getHandle();
    }

    public static PlayerConnection getConnection(Player player) {
        return getNMSPlayer(player).playerConnection;
    }

    public static void sendPackets(Player player, Packet<?>... packets) {
        PlayerConnection conn = getConnection(player);
        for (Packet<?> packet : packets) {
            conn.sendPacket(packet);
        }
    }

    public static void spawnParticle(Player player, Location location, EnumParticle particle, int count, int data) {
        PacketPlayOutWorldParticles particles = new PacketPlayOutWorldParticles(particle, true, (float) ((float) location.getX() + 0.5), (float) location.getY(), (float) ((float) location.getZ() + 0.5), 0f, 0f, 0f, 1f, count, data);
        getConnection(player).sendPacket(particles);
    }

    public static void spawnParticle(Player player, Location location, EnumParticle particle, MaterialData data, int count) {
        spawnParticle(player, location, particle, count, data.getItemTypeId() + (data.getData() << 12));
    }

    public static Object randomObjectFromList(List<?> list) {
        return list.get(ThreadLocalRandom.current().nextInt(0, list.size()));
    }

    @SneakyThrows
    public static void initTablist(Object object, JavaPlugin plugin) {
        Class<?> clazz = object.getClass();
        Method method = clazz.getDeclaredMethod("sendTab");
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            try {
                method.invoke(object);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }, 0L, 20L);
    }
}
