package com.daperkz.luckywheel;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import java.util.*;

import com.daperkz.luckywheel.SoundManager;


public class WheelAnimation extends BukkitRunnable {
    private final Player player;
    private final String wheelName;
    private final Main plugin;
    private final Inventory inv;
    private final String winnerKey;
    private int ticks = 0;
    private int totalTicks = 0;
    private int delay = 0;
    private int currentDelayCount = 0;
    private int increment = 0;
    private int startDelay = 0;
    private int winningSlot = 0;
    private final ItemStack winningItem;
    private int totalCycles = 0;
    private int currentCycle = 0;

    public WheelAnimation(Main plugin, Player player, String wheelName, String winnerKey) {
        this.plugin = plugin;
        this.player = player;
        this.wheelName = wheelName;
        this.winnerKey = winnerKey;

        ConfigurationSection settings = plugin.getConfig().getConfigurationSection("wheels." + wheelName + ".settings");
        this.totalTicks = settings.getInt("total-ticks", 50);
        this.delay = settings.getInt("animation-speed", 1);
        this.increment = settings.getInt("slowdown-ticks", 2);
        this.startDelay = settings.getInt("start-delay-ticks", 30);
        this.totalCycles = getTotalCycles();
        
        this.winningSlot = settings.getInt("winning-slot", 5);
        ConfigurationSection prize = plugin.getConfig().getConfigurationSection("wheels." + wheelName + ".prizes." + winnerKey);
        this.winningItem = InventoryManager.createItemFromConfig(prize);

        this.inv = Bukkit.createInventory(null, 9, "Roue: " + wheelName);

        SoundManager.playConfigSound(player, plugin, wheelName, "open");
        player.openInventory(inv);
    }

    private int getTotalCycles()
    {
        int simulatedTicks = 0;
        int simulatedDelay = this.delay;
        int simulatedCurrentDelay = 0;

        while (simulatedTicks < this.totalTicks) {
            if (simulatedTicks >= this.startDelay) {
                simulatedDelay += this.increment;
            }
            if (simulatedCurrentDelay >= simulatedDelay) {
                this.totalCycles++;
                simulatedCurrentDelay = 0;
            }
            simulatedCurrentDelay++;
            simulatedTicks++;
        }
        return totalCycles;
    }

    public Inventory getInventory() {
        return this.inv;
    }

    @Override
    public void run() {
        if (ticks >= this.totalTicks) {
            this.cancel();
            finish();
            return;
        }

        // Accélération du délai pour simuler le freinage (ralentissement)
        if (ticks >= this.startDelay) {
            this.delay += this.increment;
        }

        if (currentDelayCount >= this.delay) {
            // Déplacement des items
            this.currentCycle++;
            updateInventoryVisuals();
            SoundManager.playConfigSound(player, plugin, wheelName, "spin");
            currentDelayCount = 0;
        }
        currentDelayCount++;
        ticks++;
    }

    private void updateInventoryVisuals() {
        // Crée un effet de défilement en décalant les items
        ItemStack[] items = inv.getContents();
        for (int i = 8; i > 0; i--) {
            items[i] = items[i - 1];
        }
        int ticksRemaining = (totalTicks - ticks);
    
        if (currentCycle == (totalCycles - (this.winningSlot - 1))) {
            // ou au slot 0 selon ton design
            items[0] = winningItem;
        } else {
            items[0] = InventoryManager.getRandomPrize(plugin, wheelName);
        }
        inv.setContents(items);
    }

    private void finish() {
       ConfigurationSection prizesSection = plugin.getConfig().getConfigurationSection("wheels." + wheelName + ".prizes");
        
        if (prizesSection == null || winnerKey == null)
            return;

        ConfigurationSection prize = prizesSection.getConfigurationSection(winnerKey);
        if (prize == null)
            return;
        
        String customSound = prize.getString("sound");
        if (customSound != null) {
            SoundManager.playDirectSound(player, customSound);
        } else {
            SoundManager.playConfigSound(player, plugin, wheelName, "win_default");
        }

        // 4. GESTION DES COMMANDES (Priorité)
        List<String> commands = prize.getStringList("commands");
        if (commands != null && !commands.isEmpty()) {
            for (String cmd : commands) {
                String formattedCmd = cmd.replace("%player%", player.getName());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), formattedCmd);
            }
        } 
        // 5. GESTION DU DON D'ITEM (Si pas de commandes et pas une Barrière)
        else {
            if (winningItem.getType() != Material.BARRIER) {
                player.getInventory().addItem(winningItem);
            }
        }
        // Petit message sympa avec le nom de l'objet gagnant (hologram[0] si possible, sinon la clé)
        player.sendMessage(ChatColor.GREEN + "La roue s'est arrêtée ! Vous remportez votre prix.");
    }
}
