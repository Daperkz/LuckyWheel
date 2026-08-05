/*
* ==============================================================================
* LuckyWheel - Minecraft Plugin
* Copyright (c) 2026 Daperkz
*
* SoundManager
* ==============================================================================
*/
package com.daperkz.luckywheel.manager;

import com.daperkz.luckywheel.LuckyWheelPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SoundManager {

    public static void playConfigSound(Player player, LuckyWheelPlugin plugin, String wheelName, String configPath) {
        plugin.getWheelConfigManager().getWheelConfig(wheelName).ifPresent(config -> {
            String soundName = config.getString("sounds." + configPath);
            if (soundName != null) {
                playDirectSound(player, soundName);
            }
        });
    }

    public static void playDirectSound(Player player, String soundName) {
        if (soundName != null && !soundName.isEmpty()) {
            try {
                player.playSound(player.getLocation(), Sound.valueOf(soundName.toUpperCase()), 1.0f, 1.0f);
            } catch (IllegalArgumentException e) {
                Bukkit.getLogger().warning("Son invalide dans la config: " + soundName);
            }
        }
    }
}
