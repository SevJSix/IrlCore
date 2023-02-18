package me.txmc.gradlepluginbase.impl.listener.motd;

import lombok.Getter;
import me.txmc.gradlepluginbase.Main;
import org.bukkit.Bukkit;
import org.bukkit.util.CachedServerIcon;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class IconManager {

    @Getter
    private static final List<CachedServerIcon> icons = new ArrayList<>();
    @Getter
    private static File iconDataFolder;

    static {
        try {
            iconDataFolder = new File(Main.getInstance().getDataFolder(), "icons");
            if (!iconDataFolder.exists()) iconDataFolder.mkdirs();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = resizedImage.createGraphics();
        graphics2D.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        graphics2D.dispose();
        return resizedImage;
    }

    public static CompletableFuture<Boolean> downloadIcon(String link) {
        return CompletableFuture.supplyAsync(() -> {
            File file = new File(iconDataFolder, String.format("icon%s", Objects.requireNonNull(iconDataFolder.listFiles()).length));
            try {
                if (!file.exists()) file.createNewFile();
                URL url = new URL(link);
                URLConnection conn = url.openConnection();
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/93.0.4577.63 Safari/537.36");
                conn.setDoInput(true);
                InputStream is = conn.getInputStream();
                BufferedImage image = ImageIO.read(is);
                if (image.getWidth() != 64 || image.getHeight() != 64) image = resizeImage(image, 64, 64);
                ImageIO.write(image, "png", file);
                CachedServerIcon icon = Bukkit.loadServerIcon(file);
                icons.add(icon);
                return true;
            } catch (Throwable t) {
                t.printStackTrace();
            }
            file.delete();
            return false;
        });
    }
}
