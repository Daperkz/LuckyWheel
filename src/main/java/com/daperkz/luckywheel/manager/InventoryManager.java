/*
* ==============================================================================
* LuckyWheelCustom - Minecraft Plugin
* Copyright (c) 2026 Daperkz
*
* InventoryManager
* ==============================================================================
*/
package com.daperkz.luckywheel.manager;

import com.daperkz.luckywheel.LuckyWheelPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class InventoryManager {

    private static Component parseText(String input) {
        if (input == null) return Component.empty();
        if (input.contains("&") || input.contains("§")) {
            return LegacyComponentSerializer.legacyAmpersand().deserialize(input);
        }
        return MiniMessage.miniMessage().deserialize(input);
    }

    public static ItemStack getRandomPrize(LuckyWheelPlugin plugin, String wheelName) {
        ConfigurationSection prizes = plugin.getConfig().getConfigurationSection("wheels." + wheelName + ".prizes");
        if (prizes == null) return new ItemStack(Material.STONE);

        double totalWeight = 0.0;
        for (String key : prizes.getKeys(false)) {
            totalWeight += prizes.getDouble(key + ".chance", 1.0);
        }

        double random = Math.random() * totalWeight;
        double currentWeight = 0.0;

        for (String key : prizes.getKeys(false)) {
            currentWeight += prizes.getDouble(key + ".chance", 1.0);
            if (currentWeight >= random) {
                ConfigurationSection prize = prizes.getConfigurationSection(key);
                return prize != null ? createItemFromConfig(prize) : new ItemStack(Material.STONE);
            }
        }
        return new ItemStack(Material.STONE);
    }

    public static boolean consumeTicket(Player player, String wheelName) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR || !item.hasItemMeta()) return false;

        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        String ticketWheel = pdc.get(LuckyWheelPlugin.TICKET_KEY, PersistentDataType.STRING);

        if (ticketWheel == null || !ticketWheel.equalsIgnoreCase(wheelName)) {
            player.sendMessage(parseText("<red>Vous devez tenir le bon ticket de '" + wheelName + "' en main !"));
            return false;
        }

        String owner = pdc.get(LuckyWheelPlugin.OWNER_KEY, PersistentDataType.STRING);
        if (owner == null || !player.getUniqueId().toString().equals(owner)) {
            player.sendMessage(parseText("<red>Ce ticket ne vous appartient pas !"));
            return false;
        }

        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }
        return true;
    }

    public static ItemStack createItemFromConfig(ConfigurationSection section) {
        String matName = section.getString("material", "PAPER");
        int qty = Math.max(1, section.getInt("quantity", 1));
        Material mat = Material.getMaterial(matName.toUpperCase());
        if (mat == null) mat = Material.PAPER;

        ItemStack item = new ItemStack(mat, qty);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            if (section.contains("name")) {
                meta.displayName(parseText(section.getString("name")));
            }

            List<String> hologram = section.getStringList("hologram");
            if (!hologram.isEmpty()) {
                meta.displayName(parseText(hologram.get(0)));
                List<Component> lore = new ArrayList<>();
                for (int i = 1; i < hologram.size(); i++) {
                    lore.add(parseText(hologram.get(i)
                            .replace("{quantity}", String.valueOf(qty))
                            .replace("{material}", matName)));
                }
                meta.lore(lore);
            } else if (section.contains("lore")) {
                List<Component> lore = new ArrayList<>();
                for (String line : section.getStringList("lore")) {
                    lore.add(parseText(line));
                }
                meta.lore(lore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}
