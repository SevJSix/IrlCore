package me.txmc.gradlepluginbase.mixin.mixins;

import me.txmc.gradlepluginbase.common.events.PlayerHeadConsumeEvent;
import me.txmc.rtmixin.CallbackInfo;
import me.txmc.rtmixin.mixin.At;
import me.txmc.rtmixin.mixin.Inject;
import me.txmc.rtmixin.mixin.MethodInfo;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.ItemStack;
import net.minecraft.server.v1_8_R3.PacketPlayInBlockPlace;
import net.minecraft.server.v1_8_R3.PlayerConnection;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class MixinPlayerConnection {

    @Inject(info = @MethodInfo(_class = PlayerConnection.class, name = "a", sig = PacketPlayInBlockPlace.class, rtype = void.class), at = @At(pos = At.Position.HEAD))
    public static void mixinInteract(CallbackInfo ci) {
        PlayerConnection connection = (PlayerConnection) ci.getSelf();
        EntityPlayer player = connection.player;
        ItemStack item = player.inventory.getItemInHand();
        if (item == null) return;
        if (item.getTag() != null) {
            if (item.getTag().hasKey("SkullOwner")) {
                if (player.getBukkitEntity().getGameMode() == GameMode.SURVIVAL) {
                    PlayerHeadConsumeEvent event = new PlayerHeadConsumeEvent(player, item);
                    Bukkit.getPluginManager().callEvent(event);
                    ci.cancel();
                }
            }
        }
    }
}
