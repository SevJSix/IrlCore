package me.txmc.gradlepluginbase.packet.packetcommand.commands;

import me.txmc.gradlepluginbase.common.npc.NPC;
import me.txmc.gradlepluginbase.common.npc.NPCManager;
import me.txmc.gradlepluginbase.packet.packetcommand.PacketCommand;
import me.txmc.gradlepluginbase.packet.packetcommand.PacketCommandExecutor;
import me.txmc.gradlepluginbase.game.games.uhc.HerobrineListener;
import me.txmc.gradlepluginbase.utils.Utils;
import org.bukkit.Location;

public class PacketCommandHerobrine extends PacketCommandExecutor {

    public PacketCommandHerobrine() {
        super("herobrine", "spawn herobrine");
    }

    @Override
    public void onPacketCommand(PacketCommand command) {
        Location location = Utils.getRandomHerobrineLocation(command.getSender().getLocation(), 15);
       NPC npc = NPCManager.createNPC(location, "", "ewogICJ0aW1lc3RhbXAiIDogMTY3MTk4NzA1Nzk2MiwKICAicHJvZmlsZUlkIiA6ICIwMzk1NzAyMjZiOTc0ZWE5ODJhNjM1NDJmOGIwNjc4MSIsCiAgInByb2ZpbGVOYW1lIiA6ICJMYUhtMzQiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQxZDFjM2VkN2M5YzUwMzczMjAyMjcxYTQxY2ZiNTExNWQ5NzJlYTUxOWMxN2YzMjQ4MmY5Y2I5MmM4N2MxMSIKICAgIH0KICB9Cn0=", "AM2vYb9Q2vcU+8CmgF9kdfT68a7a7dMfBfzgk2eYs9Ig05IXTIpm/65toEgIgPI8tXF8Z/+UqQXQGTX9NsfhGZvs2qSSfZYS2xMQf7yZzGMbgm6594hJqEE1B3wwmUA+HfD7DXDY6Ap7gjNCYv85R07SV9n1lhMeoNEHkJsSJVIK7RI2PQWRkvBVOnQCNrua4nQxVVs2RKGGHpQhxH9OW4qtWiz5/7pQPtAJIvy1PVlWIQWICTqReO0XPJLCWFC5faqlMQPVU041fPfN/0xzxNkGgCp4AJQ3o0X8NAav3X/U/FxHQUa8jgJc51GGzC3tt37B8Hol4dsBSyP1g1atWNqn9tqRW9LEt7GTaEFxzMKbcyZpeKL6CBsmmr9cMjBRupu8Ofoi6Brcol1nOYX3zyH+K80IRp1BIAjPsVQ4wyL8tK9Wv36tJ3mnUmvi8BBZHahZruDggyI/h/MkjXksHRmR6s1TOaKxphYlEQVmpKrF3FAXNCdKtOaOnx1pK1zeNpybTpiMmIUU0CED5TEUAghQ7dkyiHBxpa3kZDVKAVJLUqWYJPruPvV2o2aZ9NqHFQnV8qfJbAaCXA09yBTXUEDIzy5tbxmXIKHhuJHQJhPr9OBqjiciNpHvu5PDVNtT1gmuf+p6TX2ZwGFvfviS1WcdCvH7w4Pkq4I9G2oWXow=");
       npc.setNameInvisible();

        HerobrineListener.herobrineSpawnLocation = location;
    }
}
