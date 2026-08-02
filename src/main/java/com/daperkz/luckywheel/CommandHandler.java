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
import net.kyori.adventure.text.minimessage.MiniMessage;

import com.daperkz.luckywheel.commands.SubCommand;
import com.daperkz.luckywheel.commands.GiveSubCommand;
import com.daperkz.luckywheel.commands.SpinSubCommand;
import com.daperkz.luckywheel.commands.ReloadSubCommand;

import java.util.HashMap;
import java.util.Map;

public class CommandHandler implements CommandExecutor {
    private final Map<String, SubCommand> subCommands = new HashMap<>();

    public CommandHandler(Main plugin) {
        registerSubCommand(new GiveSubCommand(plugin));
        registerSubCommand(new SpinSubCommand(plugin));
        registerSubCommand(new ReloadSubCommand(plugin));
    }

    private void registerSubCommand(SubCommand sub) {
        subCommands.put(sub.getName().toLowerCase(), sub);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Utilisation: /luckywheel <spin|give|reload> ..."));
            return true;
        }

        SubCommand sub = subCommands.get(args[0].toLowerCase());
        if (sub != null) {
            if (sub.getPermission() != null && !sender.hasPermission(sub.getPermission())) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Vous n'avez pas la permission."));
                return true;
            }
            sub.execute(sender, args);
        } else {
            sender.sendMessage("§cCommande inconnue. Utilisez /luckywheel <give|spin|reload> ...");
        }
        return true;
    }
}
