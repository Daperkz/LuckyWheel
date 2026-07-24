package com.daperkz.luckywheel;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.ChatColor;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.daperkz.luckywheel.InventoryManager;

public class InventoryClickListener implements Listener {
    private final Main plugin;

    public InventoryClickListener(Main plugin)
    {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || !item.hasItemMeta())
            return;

        // Vérification du nom du ticket
        String displayName = item.getItemMeta().getDisplayName();
        
        // On boucle sur les roues pour trouver celle qui correspond à ce ticket
        for (String wheelName : plugin.getConfig().getConfigurationSection("wheels").getKeys(false)) {
            // comparaison avec la metadata
           if (InventoryManager.consumeTicket(player, wheelName)) {
                event.setCancelled(true);
                
                // Calculer le gagnant ici
                Map<String, Integer> prizesMap = new HashMap<>();
                ConfigurationSection section = plugin.getConfig().getConfigurationSection("wheels." + wheelName + ".prizes");
                if (section != null) {
                    for (String key : section.getKeys(false)) {
                        prizesMap.put(key, section.getInt(key + ".chance"));
                    }
                }
                String winnerKey = WheelManager.getPrize(prizesMap);
                
                // Lancer avec les 4 arguments
                new WheelAnimation(plugin, player, wheelName, winnerKey).runTaskTimer(plugin, 0L, 2L);
                return;
            }
        }
    }

    public void givePrize(Player player, String wheelName, String prizeKey)
    {
        ConfigurationSection prize = plugin.getConfig().getConfigurationSection("wheels." + wheelName + ".prizes." + prizeKey);
        if (prize == null) return;

        String material = prize.getString("material");
        int quantity = prize.getInt("quantity");
        List<String> commands = prize.getStringList("commands");

        for (String cmd : commands) {
            String finalCmd = cmd.replace("%player%", player.getName())
                                 .replace("{material}", material)
                                 .replace("{quantity}", String.valueOf(quantity));
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCmd);
        }
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event)
    {
        if (event.getView().getTitle().startsWith("Roue: ")) {
            event.setCancelled(true);
        }
    }

}
