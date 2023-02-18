package me.txmc.gradlepluginbase.packet.packetcommand.commands;

import com.xxmicloxx.NoteBlockAPI.model.Song;
import com.xxmicloxx.NoteBlockAPI.songplayer.RadioSongPlayer;
import me.txmc.gradlepluginbase.Main;
import me.txmc.gradlepluginbase.packet.packetcommand.PacketCommand;
import me.txmc.gradlepluginbase.packet.packetcommand.PacketCommandExecutor;
import me.txmc.gradlepluginbase.utils.MusicAPI;
import me.txmc.gradlepluginbase.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

public class PacketCommandMusic extends PacketCommandExecutor {

    private RadioSongPlayer songPlayer = null;

    private final HashMap<Player, RadioSongPlayer> songPlayerMap = new HashMap<>();

    public PacketCommandMusic() {
        super("play", "play a noteblock song");
    }

    @Override
    public void onPacketCommand(PacketCommand command) {
        Player player = command.getSender();
        String[] args = command.getArgs();
        MusicAPI musicAPI = Main.getInstance().getMusicAPI();
        if (args.length > 0) {
            if (args[0].equals("stop_song") && songPlayerMap.containsKey(player)) {
                songPlayerMap.get(player).setPlaying(false);
                return;
            }
            String name = String.join(" ", Arrays.copyOfRange(args, 0, args.length));
            Song song = musicAPI.getSong(name);
            if (song != null) {
                RadioSongPlayer radioSongPlayer = musicAPI.playSong(song, player);
                songPlayerMap.put(player, radioSongPlayer);
            } else {
                Utils.sendMessage(player, String.format("&c%s is not a song in the music folder!", name));
            }
        } else {
            StringBuilder builder = new StringBuilder();
            File dataFolder = musicAPI.getMusicDataFolder();
            builder.append("Songs (").append((int) Arrays.stream(Objects.requireNonNull(dataFolder.listFiles())).filter(File::isFile).count()).append("): ");
            for (File file : Objects.requireNonNull(dataFolder.listFiles())) {
                if (file.isFile() && file.getName().endsWith(".nbs")) {
                    builder.append("&a").append(file.getName()).append("&r, ");
                }
            }
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', builder.toString()));
        }
    }
}
