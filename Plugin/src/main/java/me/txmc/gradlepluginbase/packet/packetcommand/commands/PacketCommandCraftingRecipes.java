package me.txmc.gradlepluginbase.packet.packetcommand.commands;

import me.txmc.gradlepluginbase.packet.packetcommand.PacketCommand;
import me.txmc.gradlepluginbase.packet.packetcommand.PacketCommandExecutor;
import me.txmc.gradlepluginbase.utils.CustomCrafting;
import org.bukkit.entity.Player;

public class PacketCommandCraftingRecipes extends PacketCommandExecutor {
    public PacketCommandCraftingRecipes() {
        super("recipes", "shows all custom crafting recipes");
    }

    @Override
    public void onPacketCommand(PacketCommand command) {
        Player player = command.getSender();
        player.openInventory(CustomCrafting.generateRecipeOptionsInventory());
    }
}
