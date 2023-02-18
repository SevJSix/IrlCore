package me.txmc.gradlepluginbase.packet.packetcommand.commands;

import me.txmc.gradlepluginbase.packet.packetcommand.PacketCommand;
import me.txmc.gradlepluginbase.packet.packetcommand.PacketCommandExecutor;
import me.txmc.gradlepluginbase.utils.ItemUtils;
import me.txmc.gradlepluginbase.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Objects;

public class PacketCommandCustomItems extends PacketCommandExecutor {

    public PacketCommandCustomItems() {
        super("customitems", "shows all custom items and their abilities");
    }

    @Override
    public void onPacketCommand(PacketCommand command) {
        Player player = command.getSender();
        if (player.isOp() && command.getArgs().length > 0 && Objects.equals(command.getArgs()[0], "give")) {
            player.getInventory().addItem(ItemUtils.genExplosiveBow());
            player.getInventory().addItem(ItemUtils.genGrenade(1));
            player.getInventory().addItem(ItemUtils.genVelocityPearl(1));
            player.getInventory().addItem(ItemUtils.genZombieHelmet());
            player.getInventory().addItem(ItemUtils.genZombieAxe());
            player.getInventory().addItem(ItemUtils.genArkMouse());
            player.getInventory().addItem(ItemUtils.genRodolfoBook());
            player.getInventory().addItem(ItemUtils.genLightningStick());
            player.getInventory().addItem(ItemUtils.genLumberAxe());
            player.getInventory().addItem(ItemUtils.genTowerBuilderBlock());
            return;
        }
        Inventory inventory = Bukkit.createInventory(null, 9, Utils.translateChars("&9&lCustom Items"));
        inventory.setItem(0, ItemUtils.genExplosiveBow());
        inventory.setItem(1, ItemUtils.genGrenade(1));
        inventory.setItem(2, ItemUtils.genVelocityPearl(1));
        inventory.setItem(3, ItemUtils.genArkMouse());
        inventory.setItem(4, ItemUtils.genRodolfoBook());
        inventory.setItem(5, ItemUtils.genLightningStick());
        inventory.setItem(6, ItemUtils.genLumberAxe());
        inventory.setItem(7, ItemUtils.genTowerBuilderBlock());
        player.openInventory(inventory);
    }
}
