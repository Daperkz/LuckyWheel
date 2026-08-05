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
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public class LuckyWheelPlugin extends JavaPlugin {
    public static NamespacedKey TICKET_KEY;
    public static NamespacedKey OWNER_KEY;

    private WheelConfigManager wheelConfigManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        TICKET_KEY = new NamespacedKey(this, "wheel_ticket");
        OWNER_KEY = new NamespacedKey(this, "owner");

        this.wheelConfigManager = new WheelConfigManager(this);
        this.wheelConfigManager.loadWheels();

        if (getCommand("luckywheel") != null) {
            getCommand("luckywheel").setExecutor(new CommandHandler(this));
            getCommand("luckywheel").setTabCompleter(new WheelTabCompleter(this));
        }

        getServer().getPluginManager().registerEvents(new InventoryClickListener(this), this);
        getLogger().info("LuckyWheel a été activé avec succès !");

    }

    public void reloadPluginConfig() {
        reloadConfig();
        wheelConfigManager.loadWheels();
        getServer().getScheduler().cancelTasks(this);
        getLogger().info("Configuration et roues rechargées !");
    }

    public WheelConfigManager getWheelConfigManager() {
        return wheelConfigManager;
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
        getLogger().info("LuckyWheel a été désactivé.");
    }
}
