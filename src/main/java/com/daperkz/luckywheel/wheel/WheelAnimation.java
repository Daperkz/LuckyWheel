/*
* ==============================================================================
* LuckyWheelCustom - Minecraft Plugin
* Copyright (c) 2026 Daperkz
*
* WheelAnimation
* ==============================================================================
*/
package com.daperkz.luckywheel.wheel;

import com.daperkz.luckywheel.LuckyWheelPlugin;
import com.daperkz.luckywheel.manager.InventoryManager;
import com.daperkz.luckywheel.manager.SoundManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Optional;

public class WheelAnimation extends BukkitRunnable {
    private final Player player;
    private final String wheelName;
    private final LuckyWheelPlugin plugin;
    private final Inventory inv;
    private final String winnerKey;

    private int ticks = 0;
    private final int totalTicks;
    private int delay;
    private int currentDelayCount = 0;
    private final int increment;
    private final int startDelay;
    private final int winningSlot;
    private final ItemStack winningItem;
    private int totalCycles = 0;
    private int currentCycle = 0;

    public WheelAnimation(LuckyWheelPlugin plugin, Player player, String wheelName, String winnerKey) {
        this.plugin = plugin;
        this.player = player;
        this.wheelName = wheelName;
        this.winnerKey = winnerKey;

        YamlConfiguration wheelConfig = plugin.getWheelConfigManager()
                .getWheelConfig(wheelName)
                .orElseThrow(() -> new IllegalArgumentException("Roue introuvable: " + wheelName));

        ConfigurationSection settings = wheelConfig.getConfigurationSection("settings");
        this.totalTicks = settings != null ? settings.getInt("total-ticks", 55) : 55;
        this.delay = settings != null ? settings.getInt("animation-speed", 1) : 1;
        this.increment = settings != null ? settings.getInt("slowdown-ticks", 2) : 2;
        this.startDelay = settings != null ? settings.getInt("start-delay-ticks", 30) : 30;
        this.winningSlot = settings != null ? settings.getInt("winning-slot", 5) : 5;

        ConfigurationSection prize = wheelConfig.getConfigurationSection("prizes." + winnerKey);
        this.winningItem = prize != null ? InventoryManager.createItemFromConfig(prize) : new ItemStack(Material.STONE);

        this.totalCycles = calculateTotalCycles();
        this.inv = Bukkit.createInventory(null, 9, MiniMessage.miniMessage().deserialize("<dark_gray>Roue: <gold>" + wheelName));

        SoundManager.playConfigSound(player, plugin, wheelName, "open");
        player.openInventory(inv);
    }

    private int calculateTotalCycles() {
        int simulatedTicks = 0;
        int simulatedDelay = this.delay;
        int simulatedCurrentDelay = 0;
        int cycles = 0;

        while (simulatedTicks < this.totalTicks) {
            if (simulatedTicks >= this.startDelay) {
                simulatedDelay += this.increment;
            }
            if (simulatedCurrentDelay >= simulatedDelay) {
                cycles++;
                simulatedCurrentDelay = 0;
            }
            simulatedCurrentDelay++;
            simulatedTicks++;
        }
        return cycles;
    }

    @Override
    public void run() {
        if (!player.isOnline()) {
            this.cancel();
            return;
        }

        if (ticks >= this.totalTicks) {
            this.cancel();
            finish();
            return;
        }

        if (ticks >= this.startDelay) {
            this.delay += this.increment;
        }

        if (currentDelayCount >= this.delay) {
            this.currentCycle++;
            updateInventoryVisuals();
            SoundManager.playConfigSound(player, plugin, wheelName, "spin");
            currentDelayCount = 0;
        }
        currentDelayCount++;
        ticks++;
    }

    private void updateInventoryVisuals() {
        ItemStack[] items = inv.getContents();
        for (int i = 8; i > 0; i--) {
            items[i] = items[i - 1];
        }

        if (currentCycle == (totalCycles - (this.winningSlot - 1))) {
            items[0] = winningItem;
        } else {
            items[0] = InventoryManager.getRandomPrize(plugin, wheelName);
        }
        inv.setContents(items);
    }

    private void finish() {
        Optional<YamlConfiguration> configOpt = plugin.getWheelConfigManager().getWheelConfig(wheelName);
        if (configOpt.isEmpty())
            return;

        ConfigurationSection prize = configOpt.get().getConfigurationSection("prizes." + winnerKey);
        if (prize == null)
            return;

        String customSound = prize.getString("sound");
        if (customSound != null) {
            SoundManager.playDirectSound(player, customSound);
        } else {
            SoundManager.playConfigSound(player, plugin, wheelName, "win_default");
        }

        List<String> commands = prize.getStringList("commands");
        if (!commands.isEmpty()) {
            for (String cmd : commands) {
                String formattedCmd = cmd.replace("%player%", player.getName());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), formattedCmd);
            }
        } else if (winningItem.getType() != Material.BARRIER) {
            player.getInventory().addItem(winningItem);
        }

        player.sendMessage(MiniMessage.miniMessage().deserialize("<green>La roue s'est arrêtée ! Vous remportez votre prix."));
    }
}
