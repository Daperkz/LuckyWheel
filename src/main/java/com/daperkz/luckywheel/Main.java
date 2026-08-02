/*
* ==============================================================================
* LuckyWheelCustom - Minecraft Plugin
* Copyright (c) 2026 Daperkz
*
* Main
* ==============================================================================
*/
package com.daperkz.luckywheel;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.NamespacedKey;

public class Main extends JavaPlugin {
    public static NamespacedKey TICKET_KEY;
    public static NamespacedKey OWNER_KEY;

    @Override
    public void onEnable() {

        // Crée le dossier de config et copie le config.yml par défaut s'il n'existe pas
        saveDefaultConfig();

        TICKET_KEY = new NamespacedKey(this, "wheel_ticket");
        OWNER_KEY = new NamespacedKey(JavaPlugin.getPlugin(Main.class), "owner");

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
