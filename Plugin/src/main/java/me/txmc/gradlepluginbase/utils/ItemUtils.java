package me.txmc.gradlepluginbase.utils;

import lombok.Getter;
import me.txmc.gradlepluginbase.common.ItemStacks;
import me.txmc.gradlepluginbase.common.events.TradeOffer;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ItemUtils {
    private static final List<String> names = Arrays.asList("Clifford", "Mary", "Paul", "Patton Dad", "Wendy", "Big Brandon", "MickyB", "Rodolfo");
    private static final List<String> axeNames = Arrays.asList("Rodolfo's Weapon of Choice", "Gabe's Hacksaw", "Ian Sevier's 12yo Slayer", "Verdugo's Bane", "Brandon's Xbox Controller", "Wendy's Heroin Needle", "Patton-Dad's Baseball Bat", "Cliffords \"No Employment!\" Reminiscence", "Mary's Lard Churner", "Josh Ball's Lightsaber", "Ferris's \"Extra Feminine\" Submissive Toy", "Clifford's Shit to Knock Off", "A Flathead's Worst Nightmare", "Mr Myers Life Sentence", "Ian Sevier's Antisemitic Reaver", "Carlos Medina's Class Action Lawsuit", "Ian Sevier's Swastika Painter", "Annabelle's Unfaithfulness");

    @Getter
    private static final TradeOffer[][] offers;
    private static final List<Enchantment> protEnchants = Arrays.asList(Enchantment.PROTECTION_ENVIRONMENTAL, Enchantment.PROTECTION_EXPLOSIONS, Enchantment.PROTECTION_FIRE, Enchantment.PROTECTION_PROJECTILE);
    private static final List<Enchantment> damageEnchants = Arrays.asList(Enchantment.KNOCKBACK, Enchantment.DAMAGE_ALL, Enchantment.FIRE_ASPECT, Enchantment.DAMAGE_UNDEAD, Enchantment.LOOT_BONUS_MOBS);

    public void addItemAttributeModifier(ItemStack item, String attributeName, String modifierName, double amount, int operation) {
        // Get the NMS representation of the item stack
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);

        // Get the NBTTagCompound of the item stack, creating it if necessary
        NBTTagCompound compound = nmsItem.hasTag() ? nmsItem.getTag() : new NBTTagCompound();

        // Get the modifiers NBTTagList, creating it if necessary
        NBTTagList modifiers = compound.hasKey("AttributeModifiers") ? compound.getList("AttributeModifiers", 10) : new NBTTagList();

        // Create a new NBTTagCompound to represent the attribute modifier
        NBTTagCompound modifier = new NBTTagCompound();

        // Set the name and UUID of the modifier
        modifier.setString("AttributeName", attributeName);
        modifier.setString("Name", modifierName);
        modifier.setLong("UUIDMost", 1234L); // Use any long value here
        modifier.setLong("UUIDLeast", 5678L); // Use any long value here

        // Set the amount and operation of the modifier
        modifier.setDouble("Amount", amount);
        modifier.setByte("Operation", (byte) operation);

        // Add the modifier to the list of modifiers
        modifiers.add(modifier);

        // Set the modifiers NBTTagList on the item stack's NBTTagCompound
        compound.set("AttributeModifiers", modifiers);

        // Set the NBTTagCompound on the item stack
        nmsItem.setTag(compound);

        // Convert the NMS ItemStack back to a Bukkit ItemStack
        ItemStack modifiedItem = CraftItemStack.asBukkitCopy(nmsItem);

        // Update the original ItemStack with the modified ItemStack
        item.setItemMeta(modifiedItem.getItemMeta());
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static void lore(ItemStack itemStack, String... lore) {
        if (itemStack != null) {
            ItemMeta meta = itemStack.getItemMeta();
            List<String> loreToAdd = new ArrayList<>();
            for (String s : lore) {
                loreToAdd.add(Utils.translateChars(s));
            }
            meta.setLore(loreToAdd);
            itemStack.setItemMeta(meta);
        }
    }

    public static void name(ItemStack itemStack, String name) {
        if (itemStack != null) {
            ItemMeta meta = itemStack.getItemMeta();
            meta.setDisplayName(Utils.translateChars(name));
            itemStack.setItemMeta(meta);
        }
    }

    public static void enchant(ItemStack itemStack, Enchantment enchantment, int level) {
        if (itemStack != null) {
            itemStack.addUnsafeEnchantment(enchantment, level);
        }
    }

    public static net.minecraft.server.v1_8_R3.ItemStack genEnchantedStack(Material material, int count, Enchantment enchantment, int level) {
        ItemStack stack = CraftItemStack.asBukkitCopy(genStack(material, count));
        enchant(stack, enchantment, level);
        return CraftItemStack.asNMSCopy(stack);
    }

    public static void enchantRandomHelmet(ItemStack itemStack) {
        Enchantment enchantment = protEnchants.get(ThreadLocalRandom.current().nextInt(0, protEnchants.size()));
        Enchantment enchantment1 = null;
        while (enchantment1 == null) {
            enchantment1 = protEnchants.get(ThreadLocalRandom.current().nextInt(0, protEnchants.size()));
            if (enchantment1 == enchantment) enchantment1 = null;
        }
        enchant(itemStack, enchantment, ThreadLocalRandom.current().nextInt(1, 3));
        enchant(itemStack, enchantment1, ThreadLocalRandom.current().nextInt(1, 3));
        if (ThreadLocalRandom.current().nextInt(0, 3) == 0) {
            enchant(itemStack, Enchantment.DURABILITY, ThreadLocalRandom.current().nextInt(1, 2));
        }
    }

    public static void enchantRandomSword(ItemStack itemStack) {
        Enchantment enchantment = damageEnchants.get(ThreadLocalRandom.current().nextInt(0, damageEnchants.size()));
        Enchantment enchantment1 = null;
        while (enchantment1 == null) {
            enchantment1 = damageEnchants.get(ThreadLocalRandom.current().nextInt(0, damageEnchants.size()));
            if (enchantment1 == enchantment) enchantment1 = null;
        }
        enchant(itemStack, enchantment, ThreadLocalRandom.current().nextInt(1, 3));
        enchant(itemStack, enchantment1, ThreadLocalRandom.current().nextInt(1, 3));
        if (ThreadLocalRandom.current().nextInt(0, 3) == 0) {
            enchant(itemStack, Enchantment.DURABILITY, ThreadLocalRandom.current().nextInt(1, 2));
        }
    }

    public static ItemStack genZombieHelmet() {
        ItemStack helmet = new ItemStack(Material.IRON_HELMET);
        name(helmet, String.format("&a%s's &3Helmet", names.get(ThreadLocalRandom.current().nextInt(0, names.size()))));
        enchantRandomHelmet(helmet);
        return helmet;
    }

    public static ItemStack genZombieAxe() {
        ItemStack axe = new ItemStack(Material.STONE_AXE);
        name(axe, ChatColor.values()[ThreadLocalRandom.current().nextInt(0, ChatColor.values().length)] + axeNames.get(ThreadLocalRandom.current().nextInt(0, axeNames.size())));
        enchantRandomSword(axe);
        return axe;
    }

    public static ItemStack genExplosiveBow() {
        ItemStack stack = genStack(Items.BOW, "TNTBow");
        name(stack, "&4Explosive TNT Bow");
        lore(stack, " ", "&9Item Feature:", "&7Shoots Exploding TNT instead of arrows", " ", "&cDebuff:", "&7Each shot damages bow 50x than normal");
        return stack;
    }

    public static ItemStack genGrenade(int count) {
        ItemStack stack = genStack(Items.SNOWBALL, "Grenade");
        name(stack, "&7&lMK6 Frag Grenade");
        lore(stack, " ", "&9Item Feature:", "&7Throws an object that explodes upon impact");
        stack.setAmount(count);
        return stack;
    }

    public static ItemStack genVelocityPearl(int count) {
        ItemStack stack = genStack(Items.ENDER_PEARL, "VelocityPearl");
        name(stack, "&5Velocity Pearl");
        lore(stack, " ", "&9Item Feature:", "&7Multiplies your velocity, launching you far", " ", "&bBuff:", "&7Grants temporary fall damage invincibility");
        stack.setAmount(count);
        return stack;
    }

    public static ItemStack genArkMouse() {
        ItemStack stack = genStack(CraftMagicNumbers.getItem(Material.STONE_BUTTON), "ArkMouse");
        name(stack, "&4Ark Survival Mouse");
        lore(stack, " ", "&9Item Feature:", "&7When placed, blocks in front of you will disappear!", " ", "&eWarning: Use with Caution");
        return stack;
    }

    public static ItemStack genStack(Item item, String nbtTag) {
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = new net.minecraft.server.v1_8_R3.ItemStack(item);
        NBTTagCompound comp = new NBTTagCompound();
        comp.setBoolean(nbtTag, true);
        nmsStack.setTag(comp);
        return CraftItemStack.asBukkitCopy(nmsStack);
    }

    public static net.minecraft.server.v1_8_R3.ItemStack genStack(Material material, int count, int data) {
        return new net.minecraft.server.v1_8_R3.ItemStack(CraftMagicNumbers.getItem(material), count, data);
    }

    public static net.minecraft.server.v1_8_R3.ItemStack genStack(Material material, int count) {
        return new net.minecraft.server.v1_8_R3.ItemStack(CraftMagicNumbers.getItem(material), count);
    }

    public static net.minecraft.server.v1_8_R3.ItemStack genStack(Material material) {
        return new net.minecraft.server.v1_8_R3.ItemStack(CraftMagicNumbers.getItem(material));
    }

    public static ItemStack genLumberAxe() {
        ItemStack axe = genStack(Items.GOLDEN_AXE, "LumberAxe");
        name(axe, "&eClifford's Lumberjack Axe");
        lore(axe, " ", "&9Item Feature:", "&7This axe will break entire trees at once");
        enchant(axe, Enchantment.DURABILITY, 4);
        return axe;
    }

    public static ItemStack genLightningStick() {
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = new net.minecraft.server.v1_8_R3.ItemStack(Items.STICK);
        NBTTagCompound comp = new NBTTagCompound();
        comp.setInt("LightningUses", 0);
        nmsStack.setTag(comp);
        ItemStack stack = CraftItemStack.asBukkitCopy(nmsStack);
        name(stack, "&c&lHerobrine's Lightning Rod");
        enchant(stack, Enchantment.DAMAGE_UNDEAD, 5);
        lore(stack, "&7Lightning III", "&cCurse of Herobrine", " ", "&9Item Feature:", "&7Right clicking will summon a lightning bolt wherever you are looking", " ", "&cDebuff:", "&7Only has 20 total uses");
        return stack;
    }

    public static ItemStack genRodolfoBook() {
        ItemStack stack = genStack(Items.WRITABLE_BOOK, "RodolfoRat");
        ItemMeta meta = stack.getItemMeta();
        BookMeta bookMeta = (BookMeta) meta;
        bookMeta.setPages(Utils.translateChars("&a&lHACK PLAYER&r:"));
        stack.setItemMeta(bookMeta);
        name(stack, "&2Rodolfo's RAT");
        lore(stack, " ", "&9Item Feature:", "&7Type in the name of a player you want to swap locations with", " ", "&eNote: press &a'done' &eand NOT &c'sign'");
        return stack;
    }

    public static ItemStack genTowerBuilderBlock() {
        ItemStack stack = genStack(Item.getItemOf(Blocks.BEDROCK), "TowerBuilder");
        name(stack, "&9Tower Builder Block");
        lore(stack, " ", "&9Item Feature:", "&7Builds a tower instantly for you");
        return stack;
    }

    public static ItemStack genPetrifiedWood(boolean setAmount, int amount) {
        ItemStack stack = genStack(CraftMagicNumbers.getItem(Material.LOG), "PetrifiedWood");
        if (setAmount && amount != -1) {
            stack.setAmount(amount);
        }
        name(stack, "&aPetrified Wood");
        return stack;
    }

    public static net.minecraft.server.v1_8_R3.ItemStack genPotion(PotionType type) {
        Potion potion = new Potion(type);
        return CraftItemStack.asNMSCopy(potion.toItemStack(1));
    }

    public static net.minecraft.server.v1_8_R3.ItemStack genPotion(PotionType type, Potion.Tier tier, boolean splash) {
        Potion potion = new Potion(type);
        potion.setSplash(splash);
        potion.setTier(tier);
        return CraftItemStack.asNMSCopy(potion.toItemStack(1));
    }

    static {
        offers = new TradeOffer[][]{
                new TradeOffer[]{
                        new TradeOffer(ItemStacks.DIRT, null, ItemStacks.GRAVEL),
                        new TradeOffer(ItemStacks.GRAVEL, null, ItemStacks.SAND),
                        new TradeOffer(ItemStacks.GRAVEL, ItemStacks.SAND, ItemStacks.FLINT),
                        new TradeOffer(ItemStacks.FLINT, ItemStacks.COAL, ItemStacks.GUNPOWDER),
                        new TradeOffer(genStack(Material.IRON_INGOT, 2), ItemStacks.FLINT, ItemStacks.EMERALD),
                        new TradeOffer(genStack(Material.IRON_INGOT, 2), ItemStacks.EMERALD, genStack(Material.GOLD_INGOT, 6)),
                        new TradeOffer(ItemStacks.GOLD_INGOT, ItemStacks.GUNPOWDER, ItemStacks.BLAZE_ROD),
                        new TradeOffer(genStack(Material.BLAZE_POWDER, 4), ItemStacks.EMERALD, genStack(Material.PRISMARINE_CRYSTALS, 16)),
                        new TradeOffer(genStack(Material.PRISMARINE_CRYSTALS, 32), genStack(Material.EMERALD, 3), ItemStacks.NETHER_STAR)
                },

                new TradeOffer[]{
                        new TradeOffer(genStack(Material.GOLDEN_APPLE, 6), genStack(Material.EMERALD, 10), ItemStacks.GOD_APPLE),
                        new TradeOffer(genStack(Material.TNT, 1), null, CraftItemStack.asNMSCopy(genGrenade(4))),
                        new TradeOffer(ItemStacks.BOW, genStack(Material.TNT, 3), CraftItemStack.asNMSCopy(genExplosiveBow())),
                        new TradeOffer(ItemStacks.IRON_PICKAXE, ItemStacks.EMERALD, CraftItemStack.asNMSCopy(genArkMouse())),
                        new TradeOffer(genStack(Material.SULPHUR, 3), genStack(Material.EMERALD, 2), ItemStacks.ENDER_PEARL),
                        new TradeOffer(genStack(Material.ENDER_PEARL, 2), genStack(Material.TNT, 2), CraftItemStack.asNMSCopy(genVelocityPearl(4))),
                        new TradeOffer(genStack(Material.FEATHER, 2), genStack(Material.FLINT, 2), genStack(Material.ARROW, 8)),
                        new TradeOffer(genStack(Material.IRON_BLOCK, 3), null, ItemStacks.ANVIL)
                },
                new TradeOffer[]{
                        new TradeOffer(ItemStacks.NETHER_STAR, genStack(Material.PRISMARINE_CRYSTALS, 8), genPotion(PotionType.REGEN)),
                        new TradeOffer(ItemStacks.NETHER_STAR, genStack(Material.BLAZE_POWDER, 12), genPotion(PotionType.FIRE_RESISTANCE)),
                        new TradeOffer(ItemStacks.NETHER_STAR, genStack(Material.GOLD_NUGGET, 36), genPotion(PotionType.SPEED)),
                        new TradeOffer(ItemStacks.NETHER_STAR, genStack(Material.IRON_SWORD), genPotion(PotionType.STRENGTH)),
                        new TradeOffer(ItemStacks.NETHER_STAR, genStack(Material.REDSTONE, 16), genPotion(PotionType.POISON))
                },
                new TradeOffer[]{
                        new TradeOffer(ItemStacks.IRON_INGOT, null, ItemStacks.GOLD_INGOT),
                        new TradeOffer(genStack(Material.IRON_INGOT, 8), genStack(Material.GOLD_INGOT, 8), ItemStacks.DIAMOND),
                        new TradeOffer(genStack(Material.OBSIDIAN, 3), genStack(Material.BOOK), genStack(Material.ENCHANTMENT_TABLE)),
                        new TradeOffer(genStack(Material.EMERALD, 1), genStack(Material.GLASS_BOTTLE), genStack(Material.EXP_BOTTLE)),
                        new TradeOffer(genStack(Material.SEEDS, 16), genStack(Material.WATER_BUCKET), genStack(Material.WHEAT, 64)),
                        new TradeOffer(genStack(Material.WHEAT, 3), null, genStack(Material.BREAD, 3)),
                        new TradeOffer(genStack(Material.WOOD, 16), genStack(Material.IRON_INGOT, 12), CraftItemStack.asNMSCopy(genPetrifiedWood(false, -1))),
                        new TradeOffer(CraftItemStack.asNMSCopy(genPetrifiedWood(true, 2)), genStack(Material.IRON_AXE), CraftItemStack.asNMSCopy(genLumberAxe()))
                },
        };
    }
}
