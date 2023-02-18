package me.txmc.gradlepluginbase.common.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.txmc.gradlepluginbase.common.CommonUtils;
import net.minecraft.server.v1_8_R3.EntityVillager;
import net.minecraft.server.v1_8_R3.ItemStack;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagList;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@RequiredArgsConstructor
@Getter
public class VillagerCreateEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final EntityVillager villager;

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public void setTradeOffer(TradeOffer... offers) {
        NBTTagCompound compound = new NBTTagCompound();
        compound.set("Offers", new NBTTagCompound());
        NBTTagCompound offersTag = compound.getCompound("Offers");
        NBTTagList recipes = new NBTTagList();
        for (TradeOffer offer : offers) {
            recipes.add(offer.toNBT());
        }
        offersTag.set("Recipes", recipes);
        this.villager.a(compound);
    }
}
