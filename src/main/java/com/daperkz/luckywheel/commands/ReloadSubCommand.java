/*
* ==============================================================================
* LuckyWheelCustom - Minecraft Plugin
* Copyright (c) 2026 Daperkz
*
* ReloadSubCommand
* ==============================================================================
*/
package com.daperkz.luckywheel.commands;

import com.daperkz.luckywheel.Main;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;

public class ReloadSubCommand implements SubCommand {
    private final Main plugin;

    public ReloadSubCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        plugin.reloadPluginConfig();
        sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>[LuckyWheel] Configuration rechargée avec succès !"));
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getPermission() {
        return "Daperkz.luckywheel.admin";
    }
}
