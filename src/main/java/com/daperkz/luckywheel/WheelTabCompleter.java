/*
* ==============================================================================
* LuckyWheelCustom - Minecraft Plugin
* Copyright (c) 2026 Daperkz
*
* WheelTabCompleter
* ==============================================================================
*/
package com.daperkz.luckywheel;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;

public class WheelTabCompleter implements TabCompleter {
    private final Main plugin;

    public WheelTabCompleter(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subCommands = new ArrayList<>(List.of("spin"));
            if (sender.hasPermission("Daperkz.luckywheel.admin")) {
                subCommands.add("give");
                subCommands.add("reload");
            }
            StringUtil.copyPartialMatches(args[0], subCommands, completions);
        } else if (args[0].equalsIgnoreCase("give") && sender.hasPermission("Daperkz.luckywheel.admin")) {
            completeGive(args, completions);
        } else if (args[0].equalsIgnoreCase("spin")) {
            completeSpin(args, completions);
        }

        Collections.sort(completions);
        return completions;
    }

    private void completeGive(String[] args, List<String> completions) {
        ConfigurationSection wheels = plugin.getConfig().getConfigurationSection("wheels");

        if (args.length == 2 && wheels != null) {
            StringUtil.copyPartialMatches(args[1], wheels.getKeys(false), completions);
        } else if (args.length == 3) {
            List<String> players = Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
            StringUtil.copyPartialMatches(args[2], players, completions);
        } else if (args.length == 4) {
            StringUtil.copyPartialMatches(args[3], List.of("1", "16", "32", "64"), completions);
        } else if (args.length == 5 && wheels != null) {
            String wheelName = args[1];
            ConfigurationSection prizes = plugin.getConfig().getConfigurationSection("wheels." + wheelName + ".prizes");
            if (prizes != null) {
                StringUtil.copyPartialMatches(args[4], prizes.getKeys(false), completions);
            }
        }
    }

    private void completeSpin(String[] args, List<String> completions) {
        ConfigurationSection wheels = plugin.getConfig().getConfigurationSection("wheels");
        if (args.length == 2 && wheels != null) {
            StringUtil.copyPartialMatches(args[1], wheels.getKeys(false), completions);
        }
    }
}
