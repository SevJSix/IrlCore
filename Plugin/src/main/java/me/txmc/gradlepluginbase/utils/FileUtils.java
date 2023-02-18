package me.txmc.gradlepluginbase.utils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class FileUtils {

    public static File getFileByPath(String filePath) {
        Path path = Paths.get(filePath);
        File file = new File(path.toAbsolutePath().toUri());
        if (!file.exists()) return null;
        return file;
    }

    public static List<File> getFoldersInDirectory(File dir) {
        List<File> folders = new ArrayList<>();
        Arrays.stream(Objects.requireNonNull(dir.listFiles())).filter(File::isDirectory).forEach(folders::add);
        return folders;
    }
}
