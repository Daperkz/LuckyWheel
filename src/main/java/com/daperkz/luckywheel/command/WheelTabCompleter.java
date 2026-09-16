/*
* ==============================================================================
* LuckyWheel - Minecraft Plugin
* Copyright (c) 2026 Daperkz
*
* WheelTabCompleter
* ==============================================================================
*/
package com.daperkz.luckywheel.command;

import com.daperkz.luckywheel.LuckyWheelPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class WheelTabCompleter implements TabCompleter {
    private final LuckyWheelPlugin plugin;

    public WheelTabCompleter(LuckyWheelPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subCommands = new ArrayList<>(List.of("claim", "spin"));
            if (sender.hasPermission("Daperkz.luckywheel.admin")) {
                subCommands.add("give");
                subCommands.add("reload");
            }
            StringUtil.copyPartialMatches(args[0], subCommands, completions);
        } else if (args[0].equalsIgnoreCase("give") && sender.hasPermission("Daperkz.luckywheel.admin")) {
            completeGive(args, completions);
        } else if (args[0].equalsIgnoreCase("spin")) {
            completeSpin(args, completions);
        } else if (args[0].equalsIgnoreCase("claim")) {
            completeClaim(args, completions);
        }

        Collections.sort(completions);
        return completions;
    }

    private void completeGive(String[] args, List<String> completions) {
        if (args.length == 2) {
            StringUtil.copyPartialMatches(args[1], plugin.getWheelConfigManager().getWheelNames(), completions);
        } else if (args.length == 3) {
            List<String> players = Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
            StringUtil.copyPartialMatches(args[2], players, completions);
        } else if (args.length == 4) {
            StringUtil.copyPartialMatches(args[3], List.of("1", "16", "32", "64"), completions);
        } else if (args.length == 5) {
            String wheelName = args[1];
            Optional<YamlConfiguration> configOpt = plugin.getWheelConfigManager().getWheelConfig(wheelName);
            if (configOpt.isPresent()) {
                ConfigurationSection prizes = configOpt.get().getConfigurationSection("prizes");
                if (prizes != null) {
                    StringUtil.copyPartialMatches(args[4], prizes.getKeys(false), completions);
                }
            }
        }
    }

    private void completeSpin(String[] args, List<String> completions) {
        if (args.length == 2) {
            StringUtil.copyPartialMatches(args[1], plugin.getWheelConfigManager().getWheelNames(), completions);
        }
    }

    private void completeClaim(String[] args, List<String> completions) {
        if (args.length == 2) {
            StringUtil.copyPartialMatches(args[1], plugin.getWheelConfigManager().getWheelNames(), completions);
        }
    }
}
