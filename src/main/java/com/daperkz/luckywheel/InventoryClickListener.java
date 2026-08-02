/*
* ==============================================================================
* LuckyWheelCustom - Minecraft Plugin
* Copyright (c) 2026 Daperkz
*
* InventoryClickListener
* ==============================================================================
*/
package com.daperkz.luckywheel;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class InventoryClickListener implements Listener {
    private final Main plugin;

    public InventoryClickListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir() || !item.hasItemMeta()) return;

        ConfigurationSection wheels = plugin.getConfig().getConfigurationSection("wheels");
        if (wheels == null) return;

        for (String wheelName : wheels.getKeys(false)) {
            if (InventoryManager.consumeTicket(player, wheelName)) {
                event.setCancelled(true);

                Map<String, Integer> prizesMap = new HashMap<>();
                ConfigurationSection section = plugin.getConfig().getConfigurationSection("wheels." + wheelName + ".prizes");
                if (section != null) {
                    for (String key : section.getKeys(false)) {
                        prizesMap.put(key, section.getInt(key + ".chance"));
                    }
                }
                String winnerKey = WheelManager.getPrize(prizesMap);
                new WheelAnimation(plugin, player, wheelName, winnerKey).runTaskTimer(plugin, 0L, 2L);
                return;
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().title() == null) return;
        String rawTitle = PlainTextComponentSerializer.plainText().serialize(event.getView().title());
        if (rawTitle.startsWith("Roue: ")) {
            event.setCancelled(true);
        }
    }
}
