package me.txmc.gradlepluginbase.utils;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.schematic.MCEditSchematicFormat;
import com.sk89q.worldedit.session.ClipboardHolder;
import lombok.SneakyThrows;
import me.txmc.gradlepluginbase.Main;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Objects;

public class WorldEditUtils {

    public static File dataFolder = Main.getInstance().getSchematicsDataFolder();

    public static SchematicResult pasteSchematic(World world, String schematicName, Location pasteLocation) throws Throwable {
        File schemFile = getSchematicByName(schematicName);
        if (schemFile == null) return SchematicResult.FAILED;
        if (!schemFile.exists()) return SchematicResult.FAILED;
        ClipboardFormat format = ClipboardFormat.findByFile(schemFile);
        if (format == null) return SchematicResult.FAILED;
        FileInputStream fis = new FileInputStream(schemFile);
        ClipboardReader reader = format.getReader(Files.newInputStream(schemFile.toPath()));
        BukkitWorld bukkitWorld = new BukkitWorld(world);
        if (!pasteLocation.getChunk().isLoaded()) pasteLocation.getChunk().load();
        EditSession session = WorldEdit.getInstance().getEditSessionFactory().getEditSession(bukkitWorld, -1);
        Clipboard clipboard = reader.read(bukkitWorld.getWorldData());
        Operation operation = new ClipboardHolder(clipboard, bukkitWorld.getWorldData())
                .createPaste(session, bukkitWorld.getWorldData())
                .to(Vector.toBlockPoint(pasteLocation.getX(), pasteLocation.getY(), pasteLocation.getZ()))
                .build();
        Operations.complete(operation);
        session.flushQueue();
        fis.close();
        return SchematicResult.SUCCESS;
    }

    public static void pasteSchematicMcEdit(World world, String schematicName, Location pasteLocation) throws Throwable {
        File schemFile = getSchematicByName(schematicName);
        if (schemFile == null) return;
        if (!schemFile.exists()) return;
        ClipboardFormat format = ClipboardFormat.findByFile(schemFile);
        if (format == null) return;
        BukkitWorld bukkitWorld = new BukkitWorld(world);
        EditSession session = WorldEdit.getInstance().getEditSessionFactory().getEditSession(bukkitWorld, -1);
        MCEditSchematicFormat.getFormat(schemFile).load(schemFile).paste(session, new Vector(pasteLocation.getX(), pasteLocation.getY(), pasteLocation.getZ()), false, false);
    }

    @SneakyThrows
    public static File getSchematicByName(String schematicName) {
        File schemFile = getSchematicFromFile(dataFolder, schematicName);
        // check world edit schematic folder if it doesn't exist in the plugin's schematic folder
        if (schemFile == null) {
            String path = Main.getInstance().getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath().replace(Main.getInstance().getPluginName(), "/WorldEdit/schematics");
            File schematicFolder = FileUtils.getFileByPath(path);
            if (schematicFolder == null) return null;
            schemFile = getSchematicFromFile(schematicFolder, schematicName);
            // check uuid folders in schematic folder
            if (schemFile == null) {
                for (File file : FileUtils.getFoldersInDirectory(schematicFolder)) {
                    schemFile = getSchematicFromFile(file, schematicName);
                    if (schemFile != null) return schemFile;
                }
            }
        }
        // return final schematic file
        return schemFile;
    }

    public static File getSchematicFromFile(File dataFolder, String schematicName) {
        return Arrays.stream(Objects.requireNonNull(dataFolder.listFiles())).filter(file -> file.getName().endsWith(".schematic")).filter(file -> file.getName().split("\\.")[0].equals(schematicName)).findFirst().orElse(null);
    }

    public enum SchematicResult {
        FAILED,
        NO_NBT_TAG,
        SUCCESS
    }
}
