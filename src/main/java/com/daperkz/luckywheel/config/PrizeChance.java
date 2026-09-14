/*
* ==============================================================================
* LuckyWheel - Minecraft Plugin
* Copyright (c) 2026 Daperkz
*
* PrizeChance
* ==============================================================================
*/
package com.daperkz.luckywheel.config;

import org.bukkit.configuration.ConfigurationSection;

public final class PrizeChance {
    private static final double PERCENT_TO_WEIGHT_SCALE = 1000.0;

    private PrizeChance() {
    }

    public static double fromConfig(ConfigurationSection section) {
        if (section == null) {
            return 1.0;
        }

        if (section.contains("weight")) {
            return parse(section.get("weight"));
        }

        if (section.contains("chance")) {
            return parse(section.get("chance"));
        }

        return 1.0;
    }

    public static double parse(Object rawValue) {
        if (rawValue == null) {
            return 1.0;
        }

        if (rawValue instanceof Number number) {
            return Math.max(0.0, number.doubleValue());
        }

        String value = rawValue.toString().trim();
        if (value.isEmpty()) {
            return 1.0;
        }

        String normalized = value.replace("_", "").replace(" ", "");
        if (normalized.endsWith("%")) {
            try {
                double percentage = Double.parseDouble(normalized.substring(0, normalized.length() - 1));
                return percentage * PERCENT_TO_WEIGHT_SCALE;
            } catch (NumberFormatException exception) {
                return 1.0;
            }
        }

        try {
            return Math.max(0.0, Double.parseDouble(normalized));
        } catch (NumberFormatException exception) {
            return 1.0;
        }
    }

    public static double toPercentage(double weight) {
        return weight / PERCENT_TO_WEIGHT_SCALE;
    }
}
