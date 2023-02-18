package me.txmc.gradlepluginbase.common.npc;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

public class NPCManager {

    @Getter
    @Setter
    private static NPC herobrine = null;

    public static NPC createNPC(Location location, String name, String texture, String signature) {
        NPC npc = new NPC(location.add(0.5, 0, 0.5), name, texture, signature);
        npc.spawnIn();
        herobrine = npc;
        npc.getEntityPlayer().getBukkitEntity().setMaxHealth(60);
        npc.getEntityPlayer().getBukkitEntity().setMaxHealth(60);
        return npc;
    }

    public static void removeHerobrine() {
        herobrine.remove();
        herobrine = null;
    }
}
