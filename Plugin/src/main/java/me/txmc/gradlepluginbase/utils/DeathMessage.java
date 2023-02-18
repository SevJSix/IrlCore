package me.txmc.gradlepluginbase.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.txmc.gradlepluginbase.common.CommonUtils;
import me.txmc.gradlepluginbase.utils.Utils;
import net.md_5.bungee.api.chat.*;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

@Getter
@RequiredArgsConstructor
public class DeathMessage {

    private final DamageSource source;
    private final Player eliminated;
    private LivingEntity killer;

    public BaseComponent toComponent() {
        BaseComponent component = null;
        if (source instanceof EntityDamageSource) {
            Entity entity = source.getEntity().getBukkitEntity();
            if (entity instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity) entity;
                this.killer = livingEntity;
                component = new TextComponent(Utils.translateChars("&3" + eliminated.getName()));
                component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("&6Click to &3.msg " + eliminated.getName()).create()));
                component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, ".msg " + eliminated.getName()));
                component.addExtra(new TextComponent(Utils.translateChars(" &4died to " + (livingEntity instanceof Player ? "&3" + livingEntity.getName() + "&r" : "a &3" + livingEntity.getType().getEntityClass().getSimpleName()) + (((CraftLivingEntity) livingEntity).getHandle().hasCustomName() && !(livingEntity instanceof Player) ? " &4named &3" + livingEntity.getCustomName() + "&r" : ""))));
                if (livingEntity.getEquipment().getItemInHand() != null && livingEntity.getEquipment().getItemInHand().getType() != Material.AIR) {
                    ItemStack inHand = livingEntity.getEquipment().getItemInHand();
                    ItemMeta meta = inHand.getItemMeta();
                    String colorCode = "§6";
                    BaseComponent weaponComponent = new TextComponent(getDisplayName(inHand.getType() == Material.AIR ? "&6Fists" : (CraftItemStack.asNMSCopy(inHand).getTag() == null || !CraftItemStack.asNMSCopy(inHand).getTag().hasKey("display")) ? CraftItemStack.asNMSCopy(inHand).getName() : meta.getDisplayName(), colorCode));
                    weaponComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new BaseComponent[]{new TextComponent(convertItemStackToJson(inHand))}));
                    BaseComponent componentToAdd = new TextComponent(Utils.translateChars(component.toPlainText() + " &4using "));
                    componentToAdd.addExtra(weaponComponent);
                    component = componentToAdd;
                }
            }
        } else {
            component = new TextComponent(source.getLocalizedDeathMessage(CommonUtils.getNMSPlayer(eliminated)).getText().replace(eliminated.getName(), String.format(ChatColor.translateAlternateColorCodes('&', "&r&3%s&r&4"), eliminated.getName())));
            killer = null;
        }
        return component;
    }

    public String getAltColorCodes(String display) {
        StringBuilder builder = new StringBuilder();
        if (display == null) return "§6";
        for (int i = 0; i < display.toCharArray().length; i++) {
            char c = display.charAt(i);
            if (c == '§' || c == '&') {
                builder.append(c).append(display.charAt(i + 1));
            }
        }
        return builder.toString();
    }

    public String getDisplayName(String display, String altCode) {
        StringBuilder builder = new StringBuilder();
        display = ChatColor.stripColor(display);
        for (char c : display.toCharArray()) {
            builder.append(altCode).append(c);
        }
        builder.append("§r");
        return builder.toString();
    }

    public String convertItemStackToJson(ItemStack itemStack) {
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound compound = new NBTTagCompound();
        return nmsStack.save(compound).toString();
    }
}
