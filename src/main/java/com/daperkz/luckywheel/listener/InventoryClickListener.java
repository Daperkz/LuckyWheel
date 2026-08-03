/*
* ==============================================================================
* LuckyWheelCustom - Minecraft Plugin
* Copyright (c) 2026 Daperkz
*
* InventoryClickListener
* ==============================================================================
*/
package com.daperkz.luckywheel.listener;

import com.daperkz.luckywheel.LuckyWheelPlugin;
import com.daperkz.luckywheel.manager.InventoryManager;
import com.daperkz.luckywheel.manager.WheelManager;
import com.daperkz.luckywheel.wheel.WheelAnimation;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InventoryClickListener implements Listener {
    private final LuckyWheelPlugin plugin;

    public InventoryClickListener(LuckyWheelPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        // Only process main hand interactions to prevent double-firing off-hand triggers
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        // Guard clause: check if the item is valid ticket
        Optional<String> ticketWheelOpt = InventoryManager.getTicketWheel(item);
        if (ticketWheelOpt.isEmpty()) {
            return;
        }

        String wheelName = ticketWheelOpt.get();

        // Check if wheel actually exists in configuration
        if (!plugin.getConfig().contains("wheels." + wheelName)) {
            return;
        }

        event.setCancelled(true);

        // Consume ticket and start wheel
        if (InventoryManager.tryConsumeTicket(player, EquipmentSlot.HAND, wheelName)) {
            Map<String, Integer> prizesMap = new HashMap<>();
            ConfigurationSection section = plugin.getConfig().getConfigurationSection("wheels." + wheelName + ".prizes");
            if (section != null) {
                for (String key : section.getKeys(false)) {
                    prizesMap.put(key, section.getInt(key + ".chance"));
                }
            }
            String winnerKey = WheelManager.getPrize(prizesMap);
            new WheelAnimation(plugin, player, wheelName, winnerKey).runTaskTimer(plugin, 0L, 2L);
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
