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
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.*;
import java.util.Collections;

public class WheelTabCompleter implements TabCompleter {
    private final Main plugin;

    public WheelTabCompleter(Main plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
    {
        List<String> completions = new ArrayList<>();

        // 1. Racine : choix de la commande
        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], Arrays.asList("give", "spin"), completions);
        } 
        // 2. Branche "give"
        else if (args[0].equalsIgnoreCase("give")) {
            completeGive(args, completions);
        }
        // 3. Branche "spin"
        else if (args[0].equalsIgnoreCase("spin")) {
            completeSpin(args, completions);
        }

        Collections.sort(completions);
        return completions;
    }

    private void completeGive(String[] args, List<String> completions)
    {
        // args[1] = Roue
        if (args.length == 2) {
            if (plugin.getConfig().getConfigurationSection("wheels") != null) {
                StringUtil.copyPartialMatches(args[1], plugin.getConfig().getConfigurationSection("wheels").getKeys(false), completions);
            }
        }
        // args[2] = Joueur
        else if (args.length == 3) {
            List<String> players = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) players.add(p.getName());
            StringUtil.copyPartialMatches(args[2], players, completions);
        }
        // args[3] = Quantité
        else if (args.length == 4) {
            StringUtil.copyPartialMatches(args[3], Arrays.asList("1", "16", "32", "64"), completions);
        }
        // args[4] = ID du prix
        else if (args.length == 5) {
            String wheelName = args[1];
            if (plugin.getConfig().contains("wheels." + wheelName + ".prizes")) {
                StringUtil.copyPartialMatches(args[4], plugin.getConfig().getConfigurationSection("wheels." + wheelName + ".prizes").getKeys(false), completions);
            }
        }
    }

    private void completeSpin(String[] args, List<String> completions)
    {
        // args[1] = Roue
        if (args.length == 2) {
            if (plugin.getConfig().getConfigurationSection("wheels") != null) {
                StringUtil.copyPartialMatches(args[1], plugin.getConfig().getConfigurationSection("wheels").getKeys(false), completions);
            }
        }
    }
}
