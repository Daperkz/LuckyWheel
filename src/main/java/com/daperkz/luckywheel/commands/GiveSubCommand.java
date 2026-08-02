/*
* ==============================================================================
* LuckyWheelCustom - Minecraft Plugin
* Copyright (c) 2026 Daperkz
*
* GiveSubCommand
* ==============================================================================
*/
package com.daperkz.luckywheel.commands;

import com.daperkz.luckywheel.InventoryManager;
import com.daperkz.luckywheel.Main;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class GiveSubCommand implements SubCommand {
    private final Main plugin;

    public GiveSubCommand(Main plugin) {
        this.plugin = plugin;
    }

    private ItemStack createItem(String wheelName, String prizeId, int amount, String playerUUID) {
        ConfigurationSection section;
        if (prizeId != null && plugin.getConfig().contains("wheels." + wheelName + ".prizes." + prizeId)) {
            section = plugin.getConfig().getConfigurationSection("wheels." + wheelName + ".prizes." + prizeId);
        } else {
            section = plugin.getConfig().getConfigurationSection("wheels." + wheelName + ".ticket");
        }

        if (section == null) {
            plugin.getLogger().warning("Section manquante dans config.yml pour la roue: " + wheelName);
            return new ItemStack(Material.PAPER);
        }

        ItemStack item = InventoryManager.createItemFromConfig(section);
        item.setAmount(amount);

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(Main.TICKET_KEY, PersistentDataType.STRING, wheelName);
            meta.getPersistentDataContainer().set(Main.OWNER_KEY, PersistentDataType.STRING, playerUUID);
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Usage: /luckywheel give <roue> [joueur] [quantité] [id_prix]"));
            return;
        }

        String wheelName = args[1];
        if (!plugin.getConfig().contains("wheels." + wheelName)) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Cette roue n'existe pas."));
            return;
        }

        Player target = (args.length >= 3) ? Bukkit.getPlayer(args[2]) : (sender instanceof Player p ? p : null);
        if (target == null) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Joueur introuvable ou non spécifié."));
            return;
        }

        int amount = 1;
        if (args.length >= 4) {
            try {
                amount = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>La quantité doit être un nombre entier."));
                return;
            }
        }

        String prizeId = (args.length >= 5) ? args[4] : null;

        ItemStack item = createItem(wheelName, prizeId, amount, target.getUniqueId().toString());
        target.getInventory().addItem(item);
        sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>Donné " + amount + " ticket(s) pour '" + wheelName + "' à " + target.getName()));
    }

    @Override
    public String getName() {
        return "give";
    }

    @Override
    public String getPermission() {
        return "Daperkz.luckywheel.admin";
    }
}
