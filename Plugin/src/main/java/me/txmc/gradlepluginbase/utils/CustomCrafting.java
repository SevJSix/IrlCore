package me.txmc.gradlepluginbase.utils;

import me.txmc.gradlepluginbase.Main;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class CustomCrafting implements Listener {

    public static HashMap<Recipe, String> recipeMap = new HashMap<>();
    private final List<InventoryType> exempt = Arrays.asList(InventoryType.CRAFTING, InventoryType.ANVIL, InventoryType.BEACON, InventoryType.BREWING, InventoryType.DISPENSER, InventoryType.ENCHANTING, InventoryType.MERCHANT, InventoryType.WORKBENCH);

    public static Inventory generateRecipeOptionsInventory() {
        HashMap<Recipe, String> recipeMap = CustomCrafting.recipeMap;
        Inventory inventory = Bukkit.createInventory(null, recipeMap.size() <= 9 ? 9 : recipeMap.size() <= 18 ? 18 : recipeMap.size() <= 27 ? 27 : recipeMap.size() <= 36 ? 36 : 54, Utils.translateChars("&4Custom Crafting Recipes"));
        recipeMap.forEach((rp, name) -> {
            ShapedRecipe recipe = (ShapedRecipe) rp;
            net.minecraft.server.v1_8_R3.ItemStack item = CraftItemStack.asNMSCopy(recipe.getResult());
            NBTTagCompound compound = item.getTag();
            if (compound == null) compound = new NBTTagCompound();
            compound.set("display", new NBTTagCompound());
            compound.getCompound("display").setString("Name", Utils.translateChars("&9" + name));
            compound.setString("CustomRecipe", name);
            item.setTag(compound);
            inventory.addItem(CraftItemStack.asBukkitCopy(item));
        });
        return inventory;
    }

    public static Inventory generateRecipeInventory(ShapedRecipe recipe, String name) {
        Inventory inventory = Bukkit.createInventory(null, 45, Utils.translateChars("&9" + name + " Crafting Recipe"));
        for (int i = 0; i < inventory.getSize(); i++)
            inventory.setItem(i, new org.bukkit.inventory.ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7));
        String[] shape = recipe.getShape();
        Map<Character, org.bukkit.inventory.ItemStack> map = recipe.getIngredientMap();
        int[] slots = new int[]{11, 12, 13, 20, 21, 22, 29, 30, 31, 24};
        for (int slot : slots) inventory.setItem(slot, null);
        int index = 11;
        for (String s : shape) {
            char[] charArr = s.toCharArray();
            for (char c : charArr) {
                if (map.containsKey(c)) {
                    org.bukkit.inventory.ItemStack item = map.get(c);
                    inventory.setItem(index, item);
                    index++;
                }
            }
            index = index + 6;
        }
        inventory.setItem(24, recipe.getResult());
        net.minecraft.server.v1_8_R3.ItemStack button = new net.minecraft.server.v1_8_R3.ItemStack(CraftMagicNumbers.getItem(Material.STONE_BUTTON));
        button.setTag(new NBTTagCompound());
        NBTTagCompound compound = button.getTag();
        compound.set("display", new NBTTagCompound());
        compound.getCompound("display").setString("Name", Utils.translateChars("&cSee Other Recipes"));
        compound.setString("GoBack", "go back button");
        inventory.setItem(36, CraftItemStack.asBukkitCopy(button));
        return inventory;
    }

    public static Recipe getRecipeByName(String name) {
        return recipeMap.entrySet().stream().filter(entry -> entry.getValue().equals(name)).map(Map.Entry::getKey).findAny().orElse(null);
    }

    public static void addRecipe(Recipe recipe, String name) {
        recipeMap.put(recipe, name);
        Bukkit.addRecipe(recipe);
        Main.getInstance().getLogger().log(Level.INFO, String.format("Added a custom recipe by the name of %s", name));
    }

    public void init() {
        ShapedRecipe GOLDEN_APPLE = new ShapedRecipe(new org.bukkit.inventory.ItemStack(Material.GOLDEN_APPLE, 1)).shape("###", "#G#", "###").setIngredient('#', Material.GOLD_NUGGET).setIngredient('G', Material.APPLE);
        ShapedRecipe ENCH_TABLE = new ShapedRecipe(new org.bukkit.inventory.ItemStack(Material.ENCHANTMENT_TABLE, 1)).shape("$$$", "$B$", "###").setIngredient('B', Material.BOOK).setIngredient('#', Material.OBSIDIAN);
        ShapedRecipe EXP_BOTTLE = new ShapedRecipe(new org.bukkit.inventory.ItemStack(Material.EXP_BOTTLE, 1)).shape("$#$", "#B#", "$#$").setIngredient('#', Material.REDSTONE).setIngredient('B', Material.GLASS_BOTTLE);
        ShapedRecipe EMERALD = new ShapedRecipe(new org.bukkit.inventory.ItemStack(Material.EMERALD, 1)).shape("#$").setIngredient('#', Material.REDSTONE).setIngredient('$', Material.IRON_INGOT);
        Potion instantHealthPotion = new Potion(PotionType.INSTANT_HEAL, 2);
        instantHealthPotion.setSplash(true);
        ShapedRecipe POTION = new ShapedRecipe(instantHealthPotion.toItemStack(1)).shape("$E$", "#B#", "$E$").setIngredient('$', Material.REDSTONE).setIngredient('#', Material.GOLD_INGOT).setIngredient('B', Material.GLASS_BOTTLE).setIngredient('E', Material.EMERALD);
        recipeMap.put(GOLDEN_APPLE, "Golden Apple");
        recipeMap.put(ENCH_TABLE, "Enchantment Table");
        recipeMap.put(EXP_BOTTLE, "Bottle Of Enchanting");
        recipeMap.put(EMERALD, "Emerald");
        recipeMap.put(POTION, "Instant Health Potion");
        recipeMap.forEach((recipe, name) -> {
            Bukkit.addRecipe(recipe);
            Main.getInstance().getLogger().log(Level.INFO, String.format(Utils.translateChars("&3Added a custom recipe with the name &a%s"), name));
        });
        Bukkit.getPluginManager().registerEvents(this, Main.getInstance());
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Inventory inventory = event.getClickedInventory();
        if (inventory == null) return;
        if (exempt.contains(inventory.getType())) return;
        if (inventory.getHolder() == null) {
            event.setCancelled(true);
            if (event.getSlot() > inventory.getSize() || event.getSlot() < 0) return;
            net.minecraft.server.v1_8_R3.ItemStack item = ((CraftInventory) inventory).getInventory().getItem(event.getSlot());
            Player player = (Player) event.getWhoClicked();
            if (item != null && item.hasTag()) {
                NBTTagCompound compound = item.getTag();
                if (compound.hasKey("CustomRecipe")) {
                    String recipeName = compound.getString("CustomRecipe");
                    ShapedRecipe recipe = (ShapedRecipe) CustomCrafting.getRecipeByName(recipeName);
                    Inventory inv = generateRecipeInventory(recipe, recipeName);
                    player.openInventory(inv);
                } else if (compound.hasKey("GoBack")) {
                    player.openInventory(generateRecipeOptionsInventory());
                }
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory() == null) event.setCancelled(true);
    }
}
