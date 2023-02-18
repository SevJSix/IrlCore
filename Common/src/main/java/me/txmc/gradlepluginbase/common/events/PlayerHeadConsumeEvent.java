package me.txmc.gradlepluginbase.common.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.ItemStack;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@Getter
public class PlayerHeadConsumeEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final EntityPlayer player;
    private final ItemStack item;
    @Setter
    private List<PotionEffect> potionEffects = Arrays.asList(
            new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 300, 0, true, true),
            new PotionEffect(PotionEffectType.REGENERATION, 400, 0, true, true),
            new PotionEffect(PotionEffectType.SPEED, 200, 1, true, true)
    );
    private boolean cancel;


    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancel = b;
    }
}
