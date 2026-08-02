/*
* ==============================================================================
* LuckyWheelCustom - Minecraft Plugin
* Copyright (c) 2026 Daperkz
*
* SubCommand
* ==============================================================================
*/
package com.daperkz.luckywheel.commands;

import org.bukkit.command.CommandSender;
import java.util.List;

public interface SubCommand {
    void execute(CommandSender sender, String[] args);
    String getName();
}
