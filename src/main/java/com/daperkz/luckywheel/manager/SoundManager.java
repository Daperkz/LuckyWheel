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
            String soundPath = "sounds." + configPath;
            String soundName;
            float volume = 1.0f;
            float pitch = 1.0f;

            if (config.isString(soundPath)) {
                soundName = config.getString(soundPath);
            } else {
                soundName = config.getString(soundPath + ".sound");
                volume = (float) config.getDouble(soundPath + ".volume", 1.0);
                pitch = (float) config.getDouble(soundPath + ".pitch", 1.0);
            }

            if (soundName != null) {
                playDirectSound(player, soundName, volume, pitch);
            }
        });
    }

    public static void playDirectSound(Player player, String soundName) {
        playDirectSound(player, soundName, 1.0f, 1.0f);
    }

    public static void playDirectSound(Player player, String soundName, float volume, float pitch) {
        if (soundName != null && !soundName.isEmpty()) {
            try {
                player.playSound(player.getLocation(), Sound.valueOf(soundName.toUpperCase()), volume, pitch);
            } catch (IllegalArgumentException e) {
                Bukkit.getLogger().warning("Son invalide dans la config: " + soundName);
            }
        }
    }
}
