package me.txmc.gradlepluginbase.mixin.mixins;

import co.aikar.timings.Timing;
import co.aikar.timings.Timings;
import me.txmc.gradlepluginbase.common.events.*;
import me.txmc.rtmixin.CallbackInfo;
import me.txmc.rtmixin.mixin.At;
import me.txmc.rtmixin.mixin.Inject;
import me.txmc.rtmixin.mixin.MethodInfo;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftVillager;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.plugin.SimplePluginManager;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

public class Mixins {

    @Inject(info = @MethodInfo(_class = MinecraftServer.class, name = "A", rtype = void.class), at = @At(pos = At.Position.HEAD))
    public static void onTick(CallbackInfo ci) {
        MinecraftServer mc = (MinecraftServer) ci.getSelf();
        Timing timing = Timings.of(Bukkit.getPluginManager().getPlugins()[0], "Tick Event");
        timing.startTiming();
        ServerTickEvent event = new ServerTickEvent(mc.at(), mc);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) ci.cancel();
        timing.stopTiming();
    }

    @Inject(info = @MethodInfo(_class = EntityPlayer.class, name = "die", sig = DamageSource.class, rtype = void.class), at = @At(pos = At.Position.HEAD))
    public static void onDie(CallbackInfo ci) {
        EntityPlayer player = (EntityPlayer) ci.getSelf();
        DamageSource source = (DamageSource) ci.getParameters()[0];
        PlayerPreDeathEvent event = new PlayerPreDeathEvent(player, source);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            player.setHealth(player.getMaxHealth());
            ci.cancel();
        }
    }


    @Inject(info = @MethodInfo(_class = WorldBorder.class, name = "setSize", sig = double.class, rtype = void.class), at = @At(pos = At.Position.TAIL))
    public static void onWorldBorder(CallbackInfo ci) {
        WorldBorder worldBorder = (WorldBorder) ci.getSelf();
        int trueSize = (int) (worldBorder.getSize() / 2);
        BorderUpdateEvent event = new BorderUpdateEvent(worldBorder, trueSize);
        Bukkit.getPluginManager().callEvent(event);
    }

    @Inject(info = @MethodInfo(_class = EntityPlayer.class, name = "damageEntity", sig = {DamageSource.class, float.class}, rtype = boolean.class), at = @At(pos = At.Position.HEAD))
    public static void onDamage(CallbackInfo ci) {
        EntityPlayer player = (EntityPlayer) ci.getSelf();
        DamageSource source = (DamageSource) ci.getParameters()[0];
        PreDamageEvent event = new PreDamageEvent(player, source);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) ci.cancel();
    }

    @Inject(info = @MethodInfo(_class = EntityVillager.class, name = "<init>", sig = {World.class, int.class}, rtype = void.class), at = @At(pos = At.Position.TAIL))
    public static void onVillager(CallbackInfo ci) {
        EntityVillager villager = (EntityVillager) ci.getSelf();
        VillagerCreateEvent event = new VillagerCreateEvent(villager);
        Bukkit.getPluginManager().callEvent(event);
    }
}
