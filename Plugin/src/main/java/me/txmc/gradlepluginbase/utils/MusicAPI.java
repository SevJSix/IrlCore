package me.txmc.gradlepluginbase.utils;

import com.xxmicloxx.NoteBlockAPI.model.Playlist;
import com.xxmicloxx.NoteBlockAPI.model.Song;
import com.xxmicloxx.NoteBlockAPI.songplayer.RadioSongPlayer;
import com.xxmicloxx.NoteBlockAPI.utils.NBSDecoder;
import lombok.Getter;
import lombok.SneakyThrows;
import me.txmc.gradlepluginbase.Main;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
public class MusicAPI {

    private final Main plugin;
    private File musicDataFolder;

    public MusicAPI(Main plugin) {
        this.plugin = plugin;
        try {
            musicDataFolder = new File(plugin.getDataFolder(), "music");
            if (!musicDataFolder.exists()) musicDataFolder.mkdirs();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public Playlist getPlaylist(String name) {
        List<Song> songs = new ArrayList<>();
        for (File file : Objects.requireNonNull(musicDataFolder.listFiles())) {
            if (file.isFile() && file.getName().equals(name)) {
                for (File songFolderFile : Objects.requireNonNull(file.listFiles())) {
                    if (songFolderFile.isFile() && songFolderFile.getName().endsWith(".nbs")) {
                        Song song = NBSDecoder.parse(songFolderFile);
                        songs.add(song);
                    }
                }
            }
        }
        return new Playlist(songs.toArray(new Song[0]));
    }

    @SneakyThrows
    public File createPlaylist(String name) {
        File file = new File(musicDataFolder, name);
        if (!file.exists()) file.mkdirs();
        return file;
    }

    public Song getSong(String name) {
        Song song = null;
        for (File file : Objects.requireNonNull(musicDataFolder.listFiles())) {
            if (file.isFile() && file.getName().equals(name.concat(".nbs"))) {
                song = NBSDecoder.parse(file);
                break;
            } else if (file.isDirectory()) {
                for (File playlistFile : Objects.requireNonNull(file.listFiles())) {
                    if (playlistFile.isFile() && playlistFile.getName().equals(name.concat(".nbs"))) {
                        song = NBSDecoder.parse(file);
                        break;
                    }
                }
            }
        }
        return song;
    }

    public RadioSongPlayer playSong(Song song, Player... players) {
        RadioSongPlayer songPlayer = new RadioSongPlayer(song);
        for (Player player : players) {
            songPlayer.addPlayer(player);
        }
        songPlayer.setPlaying(true);
        return songPlayer;
    }

    public RadioSongPlayer playPlaylist(Playlist playlist, Player... players) {
        RadioSongPlayer songPlayer = new RadioSongPlayer(playlist);
        for (Player player : players) {
            songPlayer.addPlayer(player);
        }
        songPlayer.setPlaying(true);
        return songPlayer;
    }
}
