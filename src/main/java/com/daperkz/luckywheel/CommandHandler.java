/*
* ==============================================================================
* LuckyWheelCustom - Minecraft Plugin
* Copyright (c) 2026 Daperkz
*
* CommandHandler
* ==============================================================================
*/
package com.daperkz.luckywheel;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.daperkz.luckywheel.commands.SubCommand;
import com.daperkz.luckywheel.commands.GiveSubCommand;
import com.daperkz.luckywheel.commands.SpinSubCommand;

import java.util.*;

public class CommandHandler implements CommandExecutor {
    private final Main plugin;
    private final Map<String, SubCommand> subCommands = new HashMap<>();

    public CommandHandler(Main plugin) {
        this.plugin = plugin;
        subCommands.put("give", new GiveSubCommand(plugin));
        subCommands.put("spin", new SpinSubCommand(plugin));
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0)
            return false;

        SubCommand sub = subCommands.get(args[0].toLowerCase());
        if (sub != null) {
            sub.execute(sender, args);
        } else {
            sender.sendMessage("§cCommande inconnue. Utilisez /luckywheel <give|spin> ...");
        }
        return true;
    }
}
