package me.txmc.gradlepluginbase.impl.listener.motd;

import me.txmc.gradlepluginbase.Main;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.util.CachedServerIcon;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ServerPingListener implements Listener {

    @EventHandler
    public void onPing(ServerListPingEvent event) {
        List<String> motds = Main.getInstance().getConfig().getStringList("Motds");
        if (motds.isEmpty()) return;
        event.setMotd(ChatColor.translateAlternateColorCodes('&', motds.get(ThreadLocalRandom.current().nextInt(0, motds.size()))));
        if (IconManager.getIcons().size() > 0) {
            CachedServerIcon icon = IconManager.getIcons().get(ThreadLocalRandom.current().nextInt(0, IconManager.getIcons().size()));
            event.setServerIcon(icon);
        }
    }
}
