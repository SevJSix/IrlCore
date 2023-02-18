package me.txmc.gradlepluginbase.packet.packetcommand.commands;

import me.txmc.gradlepluginbase.packet.packetcommand.PacketCommand;
import me.txmc.gradlepluginbase.packet.packetcommand.PacketCommandExecutor;
import me.txmc.gradlepluginbase.utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PacketCommandItemLore extends PacketCommandExecutor {
    public PacketCommandItemLore() {
        super("lore", "ADMIN ONLY COMMAND");
    }

    @Override
    public void onPacketCommand(PacketCommand command) {
        Player player = command.getSender();
        if (!player.isOp()) return;
        String[] args = command.getArgs();
        if (args.length > 0) {
            String joined = String.join(" ", Arrays.copyOfRange(args, 0, args.length));
            String toLore = Utils.translateChars(joined);
            ItemStack stackInHand = player.getItemInHand();
            if (stackInHand != null) {
                ItemMeta meta = stackInHand.getItemMeta();
                List<String> lore = meta.getLore();
                if (lore == null) lore = new ArrayList<>();
                lore.add(toLore);
                meta.setLore(lore);
                stackInHand.setItemMeta(meta);
                player.updateInventory();
            }
        }
    }
}
