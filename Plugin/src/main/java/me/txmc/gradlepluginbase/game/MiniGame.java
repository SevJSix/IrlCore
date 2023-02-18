package me.txmc.gradlepluginbase.game;

import com.google.common.collect.Lists;
import io.netty.util.internal.ConcurrentSet;
import lombok.Getter;
import lombok.Setter;
import me.txmc.gradlepluginbase.game.queue.event.PlayerTeamEvent;
import me.txmc.gradlepluginbase.utils.LogoutIO;
import me.txmc.gradlepluginbase.utils.LogoutSpot;
import me.txmc.gradlepluginbase.utils.Utils;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;

@Getter
public abstract class MiniGame implements GameData {

    private final MinigameType gameType;
    private final boolean hasTeams;
    private final boolean isPVP;
    private final TeamSetting teamSetting;
    private final ConcurrentSet<Player> participants;
    private final List<Team> teams;
    private final World world;
    @Setter
    private boolean ongoing;
    @Setter
    private int nextProgressTick = 0;
    @Setter
    private boolean next;
    private final int originalMatchSize;

    public MiniGame(MinigameType minigameType, TeamSetting teamSetting, boolean isPVP, World world, Player... participants) {
        this.gameType = minigameType;
        this.teamSetting = teamSetting;
        hasTeams = teamSetting != TeamSetting.NONE;
        this.isPVP = isPVP;
        this.world = world;
        this.participants = new ConcurrentSet<>();
        if (teamSetting != TeamSetting.NONE) {
            List<Team> queuedTeams = new ArrayList<>(serverWideTeams);
            this.teams = queuedTeams;
            queuedTeams.forEach(team -> this.participants.addAll(team.getMembers()));
        } else {
            this.teams = null;
            Collections.addAll(this.participants, participants);
        }
        serverWideMinigames.add(this);
        originalMatchSize = this.participants.size();
    }

    public MiniGame(World world, Player... participants) {
        this.world = world;
        this.gameType = MinigameType.OTHER;
        teamSetting = TeamSetting.NONE;
        hasTeams = false;
        isPVP = false;
        this.participants = new ConcurrentSet<>();
        Collections.addAll(this.participants, participants);
        this.teams = null;
        serverWideMinigames.add(this);
        originalMatchSize = this.participants.size();
    }

    public String getRemaining() {
        return this.participants.size() + "/" + originalMatchSize;
    }

    public void removeParticipant(Player player) {
        this.participants.remove(player);
    }

    public void broadcast(String msg) {
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', String.format("%s>> &r&3" + msg, gameType == MinigameType.UHC ? "&c&lUHC" : gameType == MinigameType.SPLEEF ? "&a&lSPLEEF" : gameType == MinigameType.BED_WARS ? "&9&lBEDWARS" : gameType == MinigameType.SKY_WARS ? "&6&lSKYWARS" : "&7&lMINIGAME" + ">> &r&3" + msg)));
    }

    public abstract void onStart();

    public abstract void onEnd();

    public abstract void onProgress(int currentTick);

    public abstract void onTick(int currentTick);

    public abstract void eliminate(Player player, BaseComponent message);

    public abstract void setPlayerReady(Player player);

    public abstract void resetPlayerState(Player player);

    public void saveAllLogoutSpots() {
        getParticipants().forEach(player -> {
            LogoutSpot logoutSpot = new LogoutSpot(player.getUniqueId(), player.getLocation());
            LogoutIO.saveLogoutSpot(logoutSpot);
        });
    }

    public void createRandomTeams(List<Player> players) {
        this.participants.clear();
        this.participants.addAll(players);
        serverWideTeams.clear();
        Collections.shuffle(players);
        int teamIndex = -1;
        MiniGame.Team team = null;
        for (int i = 0; i < players.size(); i++) {
            if (teamIndex == -1) teamIndex = setIndex(teamIndex, -1, players.size());
            Player player = players.get(i);
            if (team == null) {
                team = new MiniGame.Team(player);
            } else {
                team.add(player, false);
            }
            Utils.sendMessage(player, "&3You have been assigned to team &a" + team.getDisplayName());
            Utils.setDisplayName(team.getTeamColor(), player, team.getName());
            if (teamIndex == i) {
                teamIndex = getIndexToStopAt(i, serverWideTeamSetting.getValue());
                serverWideTeams.add(team);
                team = null;
            }
        }
    }

    public void genRedVsBlueTeams(List<Player> players) {
        this.participants.clear();
        this.participants.addAll(players);
        serverWideTeams.clear();
        Collections.shuffle(players);
        int firstHalf = players.size() / 2;
        List<Player> redPlayers = players.subList(0, firstHalf);
        List<Player> bluePlayers = players.subList(firstHalf, players.size());
        Team redTeam = new Team("Red", redPlayers.toArray(new Player[0]));
        Team blueTeam = new Team("Blue", bluePlayers.toArray(new Player[0]));
        redTeam.setTeamColor(ChatColor.RED);
        blueTeam.setTeamColor(ChatColor.BLUE);
        redTeam.getMembers().forEach(member -> Utils.setDisplayName(redTeam.getTeamColor(), member, redTeam.getName()));
        blueTeam.getMembers().forEach(member -> Utils.setDisplayName(blueTeam.getTeamColor(), member, blueTeam.getName()));
        serverWideTeams.add(redTeam);
        serverWideTeams.add(blueTeam);
    }

    private int setIndex(int teamIndex, int currentIndex, int listSize) {
        teamIndex = getIndexToStopAt(currentIndex, serverWideTeamSetting.getValue());
        if (teamIndex > listSize) {
            teamIndex = currentIndex + (teamIndex - listSize);
        }
        return teamIndex;
    }

    private int getIndexToStopAt(int originalIndex, MiniGame.TeamSetting setting) {
        switch (setting) {
            case DUOS:
                return originalIndex + 2;
            case TRIOS:
                return originalIndex + 3;
            case SQUADS:
                return originalIndex + 4;
            default:
                return originalIndex;
        }
    }

    public enum TeamSetting {
        SOLOS,
        DUOS,
        TRIOS,
        SQUADS,
        RED_VS_BLUE,
        CUSTOM,
        NONE
    }

    public enum MinigameType {
        UHC,
        SPLEEF,
        BED_WARS,
        SKY_WARS,
        OTHER
    }

    @Getter
    public static class Team {
        public static List<ChatColor> colors = Arrays.asList(ChatColor.RED, ChatColor.BLUE, ChatColor.LIGHT_PURPLE, ChatColor.YELLOW, ChatColor.GREEN, ChatColor.DARK_RED, ChatColor.GRAY);
        private final String name;
        private final ConcurrentSet<Player> members;
        @Setter
        private ChatColor teamColor;

        public Team(Player... players) {
            members = new ConcurrentSet<>();
            Collections.addAll(members, players);
            this.name = String.format("Team%s", serverWideTeams.size() + 1);
            initColor();
            serverWideTeams.add(this);
        }

        public Team(String name, Player... players) {
            members = new ConcurrentSet<>();
            Collections.addAll(members, players);
            this.name = name;
            initColor();
            serverWideTeams.add(this);
        }

        public Team(Player creator, String name, Player... players) {
            this(name, players);
            PlayerTeamEvent.TeamCreate event = new PlayerTeamEvent.TeamCreate(creator, this, true);
            Bukkit.getPluginManager().callEvent(event);
        }

        public void remove(Player player, boolean announce) {
            this.members.remove(player);
            PlayerTeamEvent.TeamLeave event = new PlayerTeamEvent.TeamLeave(player, this, announce);
            Bukkit.getPluginManager().callEvent(event);
        }

        public void add(Player player, boolean announce) {
            this.members.add(player);
            PlayerTeamEvent.TeamJoin event = new PlayerTeamEvent.TeamJoin(player, this, announce);
            Bukkit.getPluginManager().callEvent(event);
        }

        public Player get(Player player) {
            return this.members.stream().filter(member -> Objects.equals(member, player)).findAny().orElse(null);
        }

        public Player getAny() {
            return this.members.stream().findAny().orElse(null);
        }

        public void clear() {
            this.members.clear();
        }

        public boolean contains(Player player) {
            return this.members.contains(player);
        }

        private void initColor() {
            ChatColor color = null;
            if (serverWideTeams.size() == 0) color = colors.get(new Random().nextInt(colors.size()));
            if (serverWideTeams.size() > 0) {
                int attempts = 0;
                while (color == null) {
                    if (attempts == colors.size()) break;
                    color = colors.get(new Random().nextInt(colors.size()));
                    ChatColor finalColor = color;
                    if (serverWideTeams.stream().anyMatch(team -> team.getTeamColor() == finalColor)) color = null;
                    attempts++;
                }
            }
            this.teamColor = color;
        }

        public String getDisplayName() {
            return teamColor + name;
        }
    }
}
