package com.daperkz.luckywheel;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.persistence.PersistentDataContainer;

import java.util.ArrayList;
import java.util.List;

public class InventoryManager {

    private static String parsePlaceholders(String text, String material, int quantity) {
        return text.replace("{material}", material.toLowerCase())
                   .replace("{quantity}", String.valueOf(quantity));
    }

    public static Inventory createGUI(Main plugin, String wheelName) {
        Inventory inv = Bukkit.createInventory(null, 9, "Roue: " + wheelName);
        ConfigurationSection prizes = plugin.getConfig().getConfigurationSection("wheels." + wheelName + ".prizes");

        if (prizes == null)
            return inv;
        int slot = 0;
        for (String key : prizes.getKeys(false)) {
            if (slot >= inv.getSize())
                break;
            ConfigurationSection prize = prizes.getConfigurationSection(key);
            inv.setItem(slot++, createItemFromConfig(prize));
        }
        return inv;
    }

    public static ItemStack getRandomPrize(Main plugin, String wheelName) {
        ConfigurationSection prizes = plugin.getConfig().getConfigurationSection("wheels." + wheelName + ".prizes");
        if (prizes == null)
            return new ItemStack(Material.STONE); // Sécurité

        double totalWeight = 0.0;
        // 1. Calculer le total des chances
        for (String key : prizes.getKeys(false)) {
            totalWeight += prizes.getDouble(key + ".chance", 1.0);
        }

        // 2. Tirer un nombre aléatoire
        double random = Math.random() * totalWeight;
        double currentWeight = 0.0;

        // 3. Trouver l'objet gagnant
        for (String key : prizes.getKeys(false)) {
            currentWeight += prizes.getDouble(key + ".chance", 1.0);
            
            if (currentWeight >= random) {
                ConfigurationSection prize = prizes.getConfigurationSection(key);
                if (prize == null)
                    continue;
                return createItemFromConfig(prize);
            }
        }
        return new ItemStack(Material.STONE);
    }

    public static boolean consumeTicket(Player player, String wheelName) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta())
            return false;

        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        // Vérifie si l'item a notre clé de données et si c'est la bonne roue
        String data = item.getItemMeta().getPersistentDataContainer().get(Main.TICKET_KEY, PersistentDataType.STRING);
        if (data == null || !data.equals(wheelName))
            return false;

        // verification du owner
        String owner = pdc.get(Main.OWNER_KEY, PersistentDataType.STRING);
        if (owner == null || !player.getUniqueId().toString().equals(owner)) {
            player.sendMessage(ChatColor.RED + "Ce ticket ne vous appartient pas !");
            return false;
        }
        
        // Retrait ou réduction du ticket
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }
        return true;
    }

    public static ItemStack createItemFromConfig(ConfigurationSection prize) {
        String matName = prize.getString("material", "PAPER");
        int qty = Math.max(1, prize.getInt("quantity", 1));
        Material mat = Material.getMaterial(matName.toUpperCase());
        if (mat == null) mat = Material.PAPER;

        ItemStack item = new ItemStack(mat, qty);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            List<String> hologram = prize.getStringList("hologram");
            if (!hologram.isEmpty()) {
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', hologram.get(0)));
                List<String> lore = new ArrayList<>();
                for (int i = 1; i < hologram.size(); i++) {
                    // C'est ici qu'on remplace les placeholders, identique pour les deux méthodes
                    lore.add(ChatColor.translateAlternateColorCodes('&', hologram.get(i)
                            .replace("{quantity}", String.valueOf(qty))
                            .replace("{material}", matName)));
                }
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}
