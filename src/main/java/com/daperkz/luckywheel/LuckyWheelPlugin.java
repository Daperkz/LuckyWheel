/*
* ==============================================================================
* LuckyWheel - Minecraft Plugin
* Copyright (c) 2026 Daperkz
*
* LuckyWheelPlugin
* ==============================================================================
*/
package com.daperkz.luckywheel;

import com.daperkz.luckywheel.command.CommandHandler;
import com.daperkz.luckywheel.command.WheelTabCompleter;
import com.daperkz.luckywheel.config.WheelConfigManager;
import com.daperkz.luckywheel.listener.InventoryClickListener;
import com.daperkz.luckywheel.listener.ClaimReminderListener;
import com.daperkz.luckywheel.manager.ClaimManager;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public class LuckyWheelPlugin extends JavaPlugin {
    public static NamespacedKey TICKET_KEY;
    public static NamespacedKey OWNER_KEY;

    private WheelConfigManager wheelConfigManager;
    private ClaimManager claimManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        TICKET_KEY = new NamespacedKey(this, "wheel_ticket");
        OWNER_KEY = new NamespacedKey(this, "owner");

        this.wheelConfigManager = new WheelConfigManager(this);
        this.wheelConfigManager.loadWheels();
        this.claimManager = new ClaimManager(this);

        if (getCommand("luckywheel") != null) {
            getCommand("luckywheel").setExecutor(new CommandHandler(this));
            getCommand("luckywheel").setTabCompleter(new WheelTabCompleter(this));
        }

        getServer().getPluginManager().registerEvents(new InventoryClickListener(this), this);
        getServer().getPluginManager().registerEvents(new ClaimReminderListener(this), this);
        getLogger().info("LuckyWheel a été activé avec succès !");

    }

    public void reloadPluginConfig() {
        getServer().getScheduler().cancelTasks(this);
        if (claimManager != null) {
            claimManager.shutdown();
        }
        reloadConfig();
        wheelConfigManager.loadWheels();
        claimManager = new ClaimManager(this);
        getLogger().info("Configuration et roues rechargées !");
    }

    public WheelConfigManager getWheelConfigManager() {
        return wheelConfigManager;
    }

    public ClaimManager getClaimManager() {
        return claimManager;
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
        if (claimManager != null) {
            claimManager.shutdown();
        }
        getLogger().info("LuckyWheel a été désactivé.");
    }
}
