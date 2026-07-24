package com.daperkz.luckywheel.commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.configuration.ConfigurationSection;
import java.util.Map;
import java.util.HashMap;

import com.daperkz.luckywheel.InventoryManager;
import com.daperkz.luckywheel.WheelManager;
import com.daperkz.luckywheel.WheelAnimation;
import com.daperkz.luckywheel.Main;

public class SpinSubCommand implements SubCommand {
    private final Main plugin;
    public SpinSubCommand(Main plugin) { this.plugin = plugin; }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player))
            return;
        Player player = (Player) sender;

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /luckywheel spin <roue>");
            return;
        }

        String wheelName = args[1];
        if (!plugin.getConfig().contains("wheels." + wheelName)) {
            player.sendMessage(ChatColor.RED + "Cette roue n'existe pas.");
            return;
        }

        ItemStack hand = player.getInventory().getItemInMainHand();
        String expectedName = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("wheels." + wheelName + ".ticket.name", ""));

        // SÉCURITÉ : On vérifie que la main n'est pas vide et que l'objet a bien un nom
        if (hand == null || hand.getType() == Material.AIR || !hand.hasItemMeta() || !hand.getItemMeta().hasDisplayName()) {
            player.sendMessage(ChatColor.RED + "Vous devez tenir un ticket de '" + wheelName + "' en main !");
            return;
        }

        // Comparaison de la metadata
        if (!InventoryManager.consumeTicket(player, wheelName)) {
            player.sendMessage(ChatColor.RED + "Vous devez tenir le bon ticket de '" + wheelName + "' en main !");
            return;
        }

        Map<String, Integer> prizesMap = new HashMap<>();
        
        // 2. Remplissage de la map
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("wheels." + wheelName + ".prizes");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                prizesMap.put(key, section.getInt(key + ".chance"));
            }
        }

        String winnerKey = WheelManager.getPrize(prizesMap);
        // Si tout est bon, on ouvre le menu
        WheelAnimation animation = new WheelAnimation(plugin, player, wheelName, winnerKey);
        player.openInventory(animation.getInventory());
        animation.runTaskTimer(plugin, 0L, 2L);
    }

    @Override
    public String getName()
    {
        return "spin";
    }
}
