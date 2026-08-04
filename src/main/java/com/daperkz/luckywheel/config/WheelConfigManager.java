package com.daperkz.luckywheel.config;

import com.daperkz.luckywheel.LuckyWheelPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class WheelConfigManager {
    private final LuckyWheelPlugin plugin;
    private final File wheelsFolder;
    private final Map<String, YamlConfiguration> loadedWheels = new HashMap<>();

    public WheelConfigManager(LuckyWheelPlugin plugin) {
        this.plugin = plugin;
        this.wheelsFolder = new File(plugin.getDataFolder(), "wheels");
    }

    public void loadWheels() {
        loadedWheels.clear();

        if (!wheelsFolder.exists()) {
            if (wheelsFolder.mkdirs()) {
                plugin.saveResource("wheels/Daily.yml", false);
            }
        }

        File[] files = wheelsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;

        for (File file : files) {
            String wheelId = file.getName().substring(0, file.getName().length() - 4);
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            loadedWheels.put(wheelId.toLowerCase(), config);
            plugin.getLogger().info("Roue chargée avec succès : " + file.getName());
        }
    }

    public Optional<YamlConfiguration> getWheelConfig(String wheelId) {
        return Optional.ofNullable(loadedWheels.get(wheelId.toLowerCase()));
    }

    public boolean exists(String wheelId) {
        return loadedWheels.containsKey(wheelId.toLowerCase());
    }

    public Set<String> getWheelNames() {
        return Collections.unmodifiableSet(loadedWheels.keySet());
    }
}
