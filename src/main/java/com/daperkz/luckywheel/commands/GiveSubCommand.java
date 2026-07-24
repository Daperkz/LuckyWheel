package com.daperkz.luckywheel.commands;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.StringUtil;
import java.util.*;

import com.daperkz.luckywheel.Main;
import com.daperkz.luckywheel.InventoryManager;

public class GiveSubCommand implements SubCommand {
    private final Main plugin;

    public GiveSubCommand(Main plugin)
    {
        this.plugin = plugin;
    }

    private ItemStack createItem(String wheelName, String prizeId, int amount, String playerUUID)
    {
        ConfigurationSection section;
        
        // Si un ID est fourni et existe, on prend le prix, sinon le ticket par défaut
        if (prizeId != null && plugin.getConfig().contains("wheels." + wheelName + ".prizes." + prizeId)) {
            section = plugin.getConfig().getConfigurationSection("wheels." + wheelName + ".prizes." + prizeId);
        } else {
            section = plugin.getConfig().getConfigurationSection("wheels." + wheelName + ".ticket");
        }
        if (section == null) {
            Bukkit.getLogger().warning("Section manquante dans config.yml pour la roue: " + wheelName);
            return new ItemStack(Material.PAPER);
        }
        // Utilisation de la méthode unifiée créée précédemment
        ItemStack item = InventoryManager.createItemFromConfig(section);
        item.setAmount(amount);

       ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            // 1. Appliquer le tag persistant (IMPORTANT)
            meta.getPersistentDataContainer().set(Main.TICKET_KEY, PersistentDataType.STRING, wheelName);
            meta.getPersistentDataContainer().set(Main.OWNER_KEY, PersistentDataType.STRING, playerUUID);
            if (!meta.hasDisplayName() && section.contains("name")) {
                String name = ChatColor.translateAlternateColorCodes('&', section.getString("name"));
                meta.setDisplayName(name);
            }

            // 3. Sécurité : Si la lore est vide, on la lit ici
            if (!meta.hasLore() && section.contains("lore")) {
                List<String> lore = new ArrayList<>();
                for (String line : section.getStringList("lore")) {
                    lore.add(ChatColor.translateAlternateColorCodes('&', line));
                }
                meta.setLore(lore);
            }

            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        if (!sender.hasPermission("luckywheel.admin")) {
            sender.sendMessage(ChatColor.RED + "Vous n'avez pas la permission.");
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /luckywheel give <roue> [joueur] <amount> <id>");
            return;
        }
        String wheelName = args[1];
        if (!plugin.getConfig().contains("wheels." + wheelName)) {
            sender.sendMessage(ChatColor.RED + "Cette roue n'existe pas.");
            return;
        }
        Player target = (args.length >= 3) ? Bukkit.getPlayer(args[2]) : (sender instanceof Player ? (Player) sender : null);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Joueur introuvable ou non spécifié.");
            return;
        }
        int amount = 1;
        if (args.length >= 4) {
            try {
                amount = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "La quantité doit être un nombre.");
                return;
            }
        }

        String prizeId = (args.length >= 5) ? args[4] : null;

        ItemStack item = createItem(wheelName, prizeId, amount, target.getUniqueId().toString());
        target.getInventory().addItem(item);
        sender.sendMessage(ChatColor.GREEN + "Donné " + amount + " exemplaire(s) pour '" + wheelName + "' à " + target.getName());
    }

    @Override
    public String getName()
    {
        return "give";
    }
}
