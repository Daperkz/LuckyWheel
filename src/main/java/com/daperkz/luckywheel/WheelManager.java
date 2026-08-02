/*
* ==============================================================================
* LuckyWheelCustom - Minecraft Plugin
* Copyright (c) 2026 Daperkz
*
* WheelManager
* ==============================================================================
*/
package com.daperkz.luckywheel;

import java.util.Map;
import java.util.Random;

public class WheelManager {
    public static String getPrize(Map<String, Integer> prizes) {
        int totalWeight = 0;
        for (int chance : prizes.values()) {
            totalWeight += chance;
        }

        int randomValue = new Random().nextInt(totalWeight);
        int currentWeight = 0;

        for (Map.Entry<String, Integer> entry : prizes.entrySet()) {
            currentWeight += entry.getValue();
            if (randomValue < currentWeight) {
                return entry.getKey();
            }
        }
        return null;
    }
}
