package me.txmc.gradlepluginbase.utils;

import lombok.SneakyThrows;
import me.txmc.gradlepluginbase.Main;
import me.txmc.gradlepluginbase.game.GameData;
import me.txmc.gradlepluginbase.game.games.SpleefGame;
import me.txmc.gradlepluginbase.game.games.UHCGame;
import net.minecraft.server.v1_8_R3.ChatComponentText;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerListHeaderFooter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class Tablist implements Listener, GameData {

    private static final DecimalFormat FORMAT = new DecimalFormat("##.##");
    private final List<String> headerOption = Main.getInstance().getConfig().getStringList("TabList.Header");
    private final List<String> footerOption = Main.getInstance().getConfig().getStringList("TabList.Footer");
    private final List<String> headerOptionUHC = Main.getInstance().getConfig().getStringList("UHCTab.Header");
    private final List<String> footerOptionUHC = Main.getInstance().getConfig().getStringList("UHCTab.Footer");

    private final List<String> headerOptionSpleef = Main.getInstance().getConfig().getStringList("SpleefTab.Header");
    private final List<String> footerOptionSpleef = Main.getInstance().getConfig().getStringList("SpleefTab.Footer");
    private final Field headerField;
    private final Field footerField;

    @SneakyThrows
    public Tablist() {
        headerField = PacketPlayOutPlayerListHeaderFooter.class.getDeclaredField("a");
        headerField.setAccessible(true);
        footerField = PacketPlayOutPlayerListHeaderFooter.class.getDeclaredField("b");
        footerField.setAccessible(true);
    }

    private static String formatTps() {
        double rawTps = MinecraftServer.getServer().tps1.getAverage();
        if (rawTps > 20.00) rawTps = 20.00;
        String formatted = FORMAT.format(rawTps);
        if (rawTps >= 17) formatted = ChatColor.GREEN + formatted;
        else if (rawTps < 17 && rawTps > 12) formatted = ChatColor.YELLOW + formatted;
        else if (rawTps < 12) formatted = ChatColor.RED + formatted;
        return formatted;
    }

    public void sendTab() {
        if (getOnlinePlayers().isEmpty()) return;
        getOnlinePlayers().forEach(player -> {
            try {
                String headerStr;
                String footerStr;
                if (isPlayerInGame(player) && getPlayerGame(player) instanceof UHCGame) {
                    headerStr = String.join("\n", headerOptionUHC);
                    footerStr = String.join("\n", footerOptionUHC);
                } else if (isPlayerInGame(player) && getPlayerGame(player) instanceof SpleefGame) {
                    headerStr = String.join("\n", headerOptionSpleef);
                    footerStr = String.join("\n", footerOptionSpleef);
                } else {
                    headerStr = String.join("\n", headerOption);
                    footerStr = String.join("\n", footerOption);
                }
                IChatBaseComponent header = new ChatComponentText(parseText(headerStr, player));
                IChatBaseComponent footer = new ChatComponentText(parseText(footerStr, player));
                PacketPlayOutPlayerListHeaderFooter packet = new PacketPlayOutPlayerListHeaderFooter();
                headerField.set(packet, header);
                footerField.set(packet, footer);
                ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public String parseText(String text, Player player) {
        int ping = getPlayerPing(player);
        String tab = ChatColor.translateAlternateColorCodes('&', text
                .replace("%players%", String.valueOf(getOnlinePlayers().size()))
                .replace("%ping%", String.valueOf(ping))
                .replace("%uptime%", Utils.getFormattedInterval(System.currentTimeMillis() - Main.START_TIME))
                .replace("%nether%", String.valueOf(Bukkit.getWorld("world_nether").getPlayers().size()))
                .replace("%overworld%", String.valueOf(Bukkit.getWorld("world").getPlayers().size()))
                .replace("%end%", String.valueOf(Bukkit.getWorld("world_the_end").getPlayers().size()))
                .replace("%uhcmatch%", ((isAnyGameOngoing()) ? "&r\n&r&7There is currently a &a&lUHC &7match in progress" : ""))
                .replace("%worldbordersize%", String.valueOf((int) Math.round(player.getWorld().getWorldBorder().getSize() / 2)))
                .replace("%tps%", formatTps())
        );
        if (getUHCGame() != null) {
            tab = tab.replace("%timetill%", Utils.getElapsedTimeFromTicks(Utils.getCurrentTick(), getUHCGame().getNextProgressTick()));
        }
        return tab;
    }


    public List<Player> getOnlinePlayers() {
        return new ArrayList<>(Bukkit.getOnlinePlayers());
    }

    public int getPlayerPing(Player player) {
        return ((CraftPlayer) player).getHandle().ping;
    }
}
