/*
* ==============================================================================
* LuckyWheel - Minecraft Plugin
* Copyright (c) 2026 Daperkz
*
* WheelManager
* ==============================================================================
*/
package com.daperkz.luckywheel.manager;

import java.util.Map;
import java.util.Random;

public final class WheelManager {
    private static final Random RANDOM = new Random();

    private WheelManager() {
    }

    public static String getPrize(Map<String, Double> prizes) {
        if (prizes == null || prizes.isEmpty()) {
            return null;
        }

        double totalWeight = 0.0;
        for (double chance : prizes.values()) {
            totalWeight += Math.max(0.0, chance);
        }

        if (totalWeight <= 0.0) {
            return null;
        }

        double randomValue = RANDOM.nextDouble() * totalWeight;
        double currentWeight = 0.0;

        for (Map.Entry<String, Double> entry : prizes.entrySet()) {
            currentWeight += Math.max(0.0, entry.getValue());
            if (randomValue <= currentWeight) {
                return entry.getKey();
            }
        }

        return prizes.entrySet().stream()
                .reduce((first, second) -> second)
                .map(Map.Entry::getKey)
                .orElse(null);
    }
}
