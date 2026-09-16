/*
* ============================================================================
* LuckyWheel - Minecraft Plugin
* Copyright (c) 2026 Daperkz
*
* ClaimReminderListener
* ============================================================================
*/
package com.daperkz.luckywheel.listener;

import com.daperkz.luckywheel.LuckyWheelPlugin;
import com.daperkz.luckywheel.manager.ClaimManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Optional;

public class ClaimReminderListener implements Listener {
    private final LuckyWheelPlugin plugin;

    public ClaimReminderListener(LuckyWheelPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.getScheduler().runAtFixedRate(plugin, task -> sendReminders(player), null, 1L, getReminderPeriodTicks());
    }

    private long getReminderPeriodTicks() {
        long shortestPeriod = 60L * 20L;
        for (String wheelName : plugin.getWheelConfigManager().getWheelNames()) {
            Optional<YamlConfiguration> configOpt = plugin.getWheelConfigManager().getWheelConfig(wheelName);
            if (configOpt.isEmpty() || !configOpt.get().getBoolean("claim.reminder.enabled", false)) {
                continue;
            }

            long period = ClaimManager.parseDurationMillis(
                    configOpt.get().getString("claim.reminder.interval", "60s"));
            if (period > 0L) {
                shortestPeriod = Math.min(shortestPeriod, Math.max(1000L, period) / 50L);
            }
        }
        return Math.max(20L, shortestPeriod);
    }

    private void sendReminders(Player player) {
        if (!player.isOnline()) {
            return;
        }

        long now = System.currentTimeMillis();
        for (String wheelName : plugin.getWheelConfigManager().getWheelNames()) {
            Optional<YamlConfiguration> configOpt = plugin.getWheelConfigManager().getWheelConfig(wheelName);
            if (configOpt.isEmpty() || !configOpt.get().getBoolean("claim.enabled", false)) {
                continue;
            }

            YamlConfiguration config = configOpt.get();
            if (!config.getBoolean("claim.reminder.enabled", false)
                    || !plugin.getClaimManager().isAvailable(player.getUniqueId(), wheelName, now)) {
                continue;
            }

            String message = config.getString("claim.reminder.message", "<gold>Votre ticket est disponible !");
            player.sendActionBar(MiniMessage.miniMessage().deserialize(message));
        }
    }
}