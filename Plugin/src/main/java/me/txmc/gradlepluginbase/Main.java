package me.txmc.gradlepluginbase;

import com.github.fierioziy.particlenativeapi.api.ParticleNativeAPI;
import com.github.fierioziy.particlenativeapi.api.Particles_1_8;
import com.github.fierioziy.particlenativeapi.plugin.ParticleNativePlugin;
import io.netty.channel.ChannelPipeline;
import lombok.Getter;
import me.txmc.gradlepluginbase.common.CommonUtils;
import me.txmc.gradlepluginbase.game.games.skywars.SkywarsListeners;
import me.txmc.gradlepluginbase.game.games.spleef.SpleefListeners;
import me.txmc.gradlepluginbase.game.games.uhc.*;
import me.txmc.gradlepluginbase.game.queue.QueueCommandRewrite;
import me.txmc.gradlepluginbase.game.queue.TeamSettingCommand;
import me.txmc.gradlepluginbase.game.queue.listener.QueueListeners;
import me.txmc.gradlepluginbase.impl.bukkitcommand.*;
import me.txmc.gradlepluginbase.impl.listener.PattonTrolling;
import me.txmc.gradlepluginbase.impl.listener.lobby.LobbyListeners;
import me.txmc.gradlepluginbase.impl.listener.motd.IconManager;
import me.txmc.gradlepluginbase.impl.listener.motd.ServerPingListener;
import me.txmc.gradlepluginbase.impl.worldgen.VoidWorldGenerator;
import me.txmc.gradlepluginbase.packet.packetcommand.PacketChatListener;
import me.txmc.gradlepluginbase.packet.packetcommand.PacketCommandManager;
import me.txmc.gradlepluginbase.packet.packetcommand.commands.*;
import me.txmc.gradlepluginbase.packet.packetlistener.ChannelInjector;
import me.txmc.gradlepluginbase.packet.packetlistener.PacketManager;
import me.txmc.gradlepluginbase.utils.*;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

import javax.persistence.Lob;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

@Getter
public final class Main extends JavaPlugin implements Listener {

    public static final long START_TIME = System.currentTimeMillis();
    @Getter
    private static Main instance;
    private PacketManager manager;
    private PacketCommandManager packetCommandManager;

    private CustomCrafting customCrafting;

    private File chestNBTDataFolder;
    private List<NBTTagCompound> chestCompounds;
    private RandomTagGenerator generator;
    private ParticleNativeAPI api;
    private Particles_1_8 particles;
    private Tablist tablist;
    private MusicAPI musicAPI;
    private File schematicsDataFolder;
    private String pluginName;

    @Override
    public void onEnable() {
        this.pluginName = this.getName() + "-" + this.getDescription().getVersion() + ".jar";
        instance = this;
        saveDefaultConfig();
        loadMixins();
        tablist = new Tablist();
        schematicsDataFolder = new File(getDataFolder(), "Schematics");
        if (!schematicsDataFolder.exists()) schematicsDataFolder.mkdirs();
        api = ParticleNativePlugin.getAPI();
        musicAPI = new MusicAPI(this);
        particles = api.getParticles_1_8();
        PattonTrolling nettyReworking = new PattonTrolling();
        UHCListeners uhcListeners = new UHCListeners();
        Bukkit.getPluginCommand("leave").setExecutor(new UHCLeaveCommand());
        Bukkit.getPluginCommand("resetuhc").setExecutor(new ResetWorldsCommand());
        Bukkit.getPluginCommand("startuhc").setExecutor(new UHCStartCommand());
        Bukkit.getPluginCommand("enduhc").setExecutor(new UHCEndCommand());
        Bukkit.getPluginCommand("f").setExecutor(new FireworkTestCommand());
        Bukkit.getPluginCommand("progress").setExecutor(new UHCProgressCommand());
        Bukkit.getPluginCommand("queue").setExecutor(new QueueCommandRewrite());
        Bukkit.getPluginCommand("team").setExecutor(new QueueCommandRewrite());
        Bukkit.getPluginCommand("teamsetting").setExecutor(new TeamSettingCommand());
        Bukkit.getPluginCommand("startspleef").setExecutor(new SpleefStartCommand());
        Bukkit.getPluginCommand("saveskywarschest").setExecutor(new SaveSkywarsChestLocation());
        Bukkit.getPluginCommand("startskywars").setExecutor(new SkywarsStartCommand());
        Bukkit.getPluginCommand("skywarslocation").setExecutor(new SaveSkywarsSpawnLocation());
        Bukkit.getPluginManager().registerEvents(uhcListeners, this);
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getPluginManager().registerEvents(new ServerPingListener(), this);
        Bukkit.getPluginManager().registerEvents(new LootDropListener(), this);
        Bukkit.getPluginManager().registerEvents(new GrenadeListener(), this);
        Bukkit.getPluginManager().registerEvents(new HerobrineListener(), this);
        Bukkit.getPluginManager().registerEvents(new LumberAxeListener(), this);
        Bukkit.getPluginManager().registerEvents(new QueueListeners(), this);
        Bukkit.getPluginManager().registerEvents(new SpleefListeners(), this);
        Bukkit.getPluginManager().registerEvents(new SkywarsListeners(), this);
        Bukkit.getPluginManager().registerEvents(new TowerBuilderListener(), this);
//        Bukkit.getPluginManager().registerEvents(new GrappleHookListener(), this);
        Bukkit.getPluginManager().registerEvents(new LobbyListeners(), this);
        Bukkit.getPluginManager().registerEvents(nettyReworking, this);
        Arrays.stream(Objects.requireNonNull(IconManager.getIconDataFolder().listFiles())).forEach(file -> {
            try {
                IconManager.getIcons().add(Bukkit.loadServerIcon(file));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        manager = new PacketManager();
        packetCommandManager = new PacketCommandManager();
        manager.registerPacketListener(new PacketChatListener(packetCommandManager));
        manager.registerPacketListener(nettyReworking);
        packetCommandManager.registerCommand(new PacketCommandHelp());
        packetCommandManager.registerCommand(new PacketCommandMessage());
        packetCommandManager.registerCommand(new PacketCommandHeal());
        packetCommandManager.registerCommand(new PacketCommandNickName());
        packetCommandManager.registerCommand(new PacketCommandMotd());
        packetCommandManager.registerCommand(new PacketCommandServerIcon());
        packetCommandManager.registerCommand(new PacketCommandSpectate());
        packetCommandManager.registerCommand(new PacketCommandSaveChest());
        packetCommandManager.registerCommand(new PacketCommandLootDropTest());
        packetCommandManager.registerCommand(new PacketCommandLogoutSpotTest());
        packetCommandManager.registerCommand(new PacketCommandTNTBowGiver());
        packetCommandManager.registerCommand(new PacketCommandFallingBlockTest());
        packetCommandManager.registerCommand(new PacketCommandEntityAI());
        packetCommandManager.registerCommand(new PacketCommandNBTTagAdder());
        packetCommandManager.registerCommand(new PacketCommandItemRename());
        packetCommandManager.registerCommand(new PacketCommandItemLore());
        packetCommandManager.registerCommand(new PacketCommandCraftingRecipes());
        packetCommandManager.registerCommand(new PacketCommandCustomItems());
        packetCommandManager.registerCommand(new PacketCommandSeeNBTTag());
        packetCommandManager.registerCommand(new PacketCommandMusic());
        packetCommandManager.registerCommand(nettyReworking);
        loadNBTFiles();
        customCrafting = new CustomCrafting();
        customCrafting.init();
        CommonUtils.initTablist(tablist, this);
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        return new VoidWorldGenerator();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        EntityPlayer player = ((CraftPlayer) event.getPlayer()).getHandle();
        ChannelPipeline pipeline = player.playerConnection.networkManager.channel.pipeline();
        if (pipeline.get(String.format("packet_listener%s", getName())) != null) return;
        pipeline.addBefore("packet_handler", String.format("packet_listener%s", getName()), new ChannelInjector(player.getBukkitEntity(), this));
        System.out.println("Successfully added " + player.displayName + " to a custom channel pipeline.");
        Utils.sendMessage(event.getPlayer(), String.format("&3Hello, &a%s&3, type &b\".help\" &3to see all user commands", event.getPlayer().getName()));
    }

    private void loadMixins() {
        File mixinJar = new File(".", "mixins-temp.jar");
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("mixins.dat");
            if (is == null) throw new RuntimeException("The plugin jar is missing the mixins");
            Files.copy(is, mixinJar.toPath());
            URLClassLoader ccl = new URLClassLoader(new URL[]{mixinJar.toURI().toURL()});
            Class<?> mixinMainClass = Class.forName(String.format("%s.mixin.MixinMain", getClass().getPackage().getName()), true, ccl);
            Object instance = mixinMainClass.newInstance();
            Method mainM = instance.getClass().getDeclaredMethod("init", JavaPlugin.class);
            mainM.invoke(instance, this);
        } catch (Throwable t) {
            getLogger().severe(String.format("Failed to load mixins due to %s. Please see the stacktrace below for more info", t.getClass().getName()));
            t.printStackTrace();
        } finally {
            if (mixinJar.exists()) mixinJar.delete();
        }
    }

    public void loadNBTFiles() {
        chestNBTDataFolder = new File(Main.getInstance().getDataFolder(), "nbt");
        if (!chestNBTDataFolder.exists()) chestNBTDataFolder.mkdirs();
        chestCompounds = new ArrayList<>();
        if (Objects.requireNonNull(chestNBTDataFolder.listFiles()).length > 0) {
            long start = System.currentTimeMillis();
            boolean completed = false;
            for (File file : Objects.requireNonNull(chestNBTDataFolder.listFiles())) {
                try {
                    NBTTagCompound compound = LogoutIO.loadNBTSafely(file);
                    chestCompounds.add(compound);
                    completed = true;
                } catch (Throwable t) {
                    t.printStackTrace();
                    completed = false;
                    break;
                }
            }
            if (completed) {
                long finish = System.currentTimeMillis() - start;
                getLogger().log(Level.INFO, ChatColor.GREEN + "Successfully loaded " + Objects.requireNonNull(chestNBTDataFolder.listFiles()).length + " NBT data tags in " + finish + "ms");
            } else {
                getLogger().log(Level.SEVERE, ChatColor.RED + "Error loading nbt data tags!");
            }
        }
        generator = new RandomTagGenerator(chestCompounds);
    }

    @Override
    public void onDisable() {
    }
}
