package me.txmc.gradlepluginbase.mixin.mixins;

import me.txmc.gradlepluginbase.common.CommonUtils;
import me.txmc.rtmixin.CallbackInfo;
import me.txmc.rtmixin.mixin.At;
import me.txmc.rtmixin.mixin.Inject;
import me.txmc.rtmixin.mixin.MethodInfo;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.Blocks;
import net.minecraft.server.v1_8_R3.EntityProjectile;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.material.MaterialData;

public class MixinProjectile {


    @Inject(info = @MethodInfo(_class = EntityProjectile.class, name = "t_", sig = {}, rtype = void.class), at = @At(pos = At.Position.TAIL))
    public static void onMove(CallbackInfo ci) {
        EntityProjectile projectile = (EntityProjectile) ci.getSelf();
        try {
            BlockPosition position = new BlockPosition(projectile.locX, projectile.locY, projectile.locZ);
            if (projectile.world.getType(position).getBlock() == Blocks.TNT) {
                projectile.world.setAir(position);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
