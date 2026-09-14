/*
* ==============================================================================
* LuckyWheel - Minecraft Plugin
* Copyright (c) 2026 Daperkz
*
* SpinSubCommand
* ==============================================================================
*/
package com.daperkz.luckywheel.command.sub;

import com.daperkz.luckywheel.LuckyWheelPlugin;
import com.daperkz.luckywheel.manager.InventoryManager;
import com.daperkz.luckywheel.manager.WheelManager;
import com.daperkz.luckywheel.wheel.WheelAnimation;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class SpinSubCommand implements SubCommand {
    private final LuckyWheelPlugin plugin;

    public SpinSubCommand(LuckyWheelPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Seul un joueur peut exécuter cette commande."));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Usage: /luckywheel spin <roue>"));
            return;
        }

        String wheelName = args[1];
        if (!plugin.getWheelConfigManager().exists(wheelName)) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Cette roue n'existe pas."));
            return;
        }

        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand.getType() == Material.AIR || !hand.hasItemMeta()) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Vous devez tenir un ticket de '" + wheelName + "' en main !"));
            return;
        }

        if (!InventoryManager.tryConsumeTicket(player, EquipmentSlot.HAND, wheelName)) {
            return;
        }

        Map<String, Double> prizesMap = new LinkedHashMap<>();
        Optional<YamlConfiguration> configOpt = plugin.getWheelConfigManager().getWheelConfig(wheelName);
        if (configOpt.isPresent()) {
            ConfigurationSection section = configOpt.get().getConfigurationSection("prizes");
            if (section != null) {
                for (String key : section.getKeys(false)) {
                    ConfigurationSection prizeSection = section.getConfigurationSection(key);
                    if (prizeSection != null) {
                        prizesMap.put(key, com.daperkz.luckywheel.config.PrizeChance.fromConfig(prizeSection));
                    }
                }
            }
        }

        String winnerKey = WheelManager.getPrize(prizesMap);
        WheelAnimation animation = new WheelAnimation(plugin, player, wheelName, winnerKey);
        animation.start();
    }

    @Override
    public String getName() {
        return "spin";
    }
}
