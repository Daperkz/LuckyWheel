/*
* ==============================================================================
* LuckyWheelCustom - Minecraft Plugin
* Copyright (c) 2026 Daperkz
*
* LuckyWheelPlugin
* ==============================================================================
*/
package com.daperkz.luckywheel;

import com.daperkz.luckywheel.command.CommandHandler;
import com.daperkz.luckywheel.command.WheelTabCompleter;
import com.daperkz.luckywheel.listener.InventoryClickListener;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public class LuckyWheelPlugin extends JavaPlugin {
    public static NamespacedKey TICKET_KEY;
    public static NamespacedKey OWNER_KEY;

    @Override
    public void onEnable() {

        // Crée le dossier de config et copie le config.yml par défaut s'il n'existe pas
        saveDefaultConfig();

        TICKET_KEY = new NamespacedKey(this, "wheel_ticket");
        OWNER_KEY = new NamespacedKey(JavaPlugin.getPlugin(LuckyWheelPlugin.class), "owner");

        if (getCommand("luckywheel") != null) {
            getCommand("luckywheel").setExecutor(new CommandHandler(this));
            getCommand("luckywheel").setTabCompleter(new WheelTabCompleter(this));
        }

        getServer().getPluginManager().registerEvents(new InventoryClickListener(this), this);
        getLogger().info("LuckyWheelCustom a été activé avec succès !");

    }

    public void reloadPluginConfig() {
        reloadConfig();
        getServer().getScheduler().cancelTasks(this);
        getLogger().info("Configuration rechargée et tâches résiduelles annulées !");
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
        getLogger().info("LuckyWheelCustom a été désactivé.");
    }
}
