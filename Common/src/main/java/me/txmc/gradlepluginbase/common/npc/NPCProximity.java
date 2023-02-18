package me.txmc.gradlepluginbase.common.npc;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface NPCProximity {

    void enterProximity(Player player);

    void exitProximity(Player player);

    void handleEntityLook(NPC npc, Player player);

    boolean isInProximity(NPC npc, Player player);
}
