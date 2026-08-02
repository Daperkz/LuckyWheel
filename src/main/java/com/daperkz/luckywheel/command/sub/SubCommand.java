/*
* ==============================================================================
* LuckyWheelCustom - Minecraft Plugin
* Copyright (c) 2026 Daperkz
*
* SubCommand
* ==============================================================================
*/
package com.daperkz.luckywheel.command.sub;

import org.bukkit.command.CommandSender;

public interface SubCommand {
    void execute(CommandSender sender, String[] args);
    String getName();
    default String getPermission() {
        return "Daperkz.luckywheel";
    }
}
