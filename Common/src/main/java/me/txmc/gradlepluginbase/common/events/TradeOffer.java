package me.txmc.gradlepluginbase.common.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.server.v1_8_R3.ItemStack;
import net.minecraft.server.v1_8_R3.NBTTagCompound;

@Getter
@RequiredArgsConstructor
public class TradeOffer {

    private final ItemStack itemBuy1;
    private final ItemStack itemBuy2;
    private final ItemStack itemSell;

    public NBTTagCompound toNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInt("maxUses", Integer.MAX_VALUE);
        compound.setInt("uses", 0);
        compound.setByte("rewardExp", (byte) 500);
        compound.set("buy", itemBuy1.save(new NBTTagCompound()));
        if (itemBuy2 != null) compound.set("buyB", itemBuy2.save(new NBTTagCompound()));
        compound.set("sell", itemSell.save(new NBTTagCompound()));
        return compound;
    }
}
