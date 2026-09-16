/*
* ============================================================================
* LuckyWheel - Minecraft Plugin
* Copyright (c) 2026 Daperkz
*
* ClaimSubCommand
* ============================================================================
*/
package com.daperkz.luckywheel.command.sub;

import com.daperkz.luckywheel.LuckyWheelPlugin;
import com.daperkz.luckywheel.manager.ClaimManager;
import com.daperkz.luckywheel.manager.InventoryManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

public class ClaimSubCommand implements SubCommand {
    private final LuckyWheelPlugin plugin;

    public ClaimSubCommand(LuckyWheelPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Seul un joueur peut exécuter cette commande."));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Usage: /luckywheel claim <roue>"));
            return;
        }

        String wheelName = args[1];
        Optional<YamlConfiguration> configOpt = plugin.getWheelConfigManager().getWheelConfig(wheelName);
        if (configOpt.isEmpty()) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Cette roue n'existe pas."));
            return;
        }

        YamlConfiguration config = configOpt.get();
        if (!config.getBoolean("claim.enabled", false)) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Cette roue ne permet pas de réclamer un ticket."));
            return;
        }

        long cooldown = ClaimManager.parseDurationMillis(config.getString("claim.cooldown", "24h"));
        if (cooldown <= 0L) {
            plugin.getLogger().warning("Cooldown invalide pour la roue " + wheelName + ". Utilisez une durée comme 24h ou 7d.");
            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Le cooldown de cette roue est mal configuré."));
            return;
        }

        long now = System.currentTimeMillis();
        ClaimManager claimManager = plugin.getClaimManager();
        if (!claimManager.claim(player.getUniqueId(), wheelName, cooldown, now)) {
            long remaining = claimManager.getNextClaimAt(player.getUniqueId(), wheelName) - now;
            player.sendMessage(MiniMessage.miniMessage().deserialize(
                    "<red>Vous pourrez réclamer un nouveau ticket dans <yellow>" + formatRemaining(remaining) + "<red>."));
            return;
        }

        InventoryManager.giveTicket(plugin, player, wheelName, 1);
        player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Vous avez reçu un ticket pour la roue <gold>" + wheelName + "<green> !"));
    }

    private String formatRemaining(long milliseconds) {
        long seconds = Math.max(1L, (milliseconds + 999L) / 1000L);
        long days = seconds / 86400L;
        seconds %= 86400L;
        long hours = seconds / 3600L;
        seconds %= 3600L;
        long minutes = seconds / 60L;
        seconds %= 60L;

        if (days > 0) return days + "j " + hours + "h";
        if (hours > 0) return hours + "h " + minutes + "min";
        if (minutes > 0) return minutes + "min " + seconds + "s";
        return seconds + "s";
    }

    @Override
    public String getName() {
        return "claim";
    }
}
