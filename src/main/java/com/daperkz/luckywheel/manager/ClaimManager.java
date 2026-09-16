/*
* ============================================================================
* LuckyWheel - Minecraft Plugin
* Copyright (c) 2026 Daperkz
*
* ClaimManager
* ============================================================================
*/
package com.daperkz.luckywheel.manager;

import com.daperkz.luckywheel.LuckyWheelPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ClaimManager {
    private static final Pattern DURATION_PATTERN = Pattern.compile("^(\\d+)(s|m|h|d|w)$", Pattern.CASE_INSENSITIVE);

    private final LuckyWheelPlugin plugin;
    private final File claimsFile;
    private final File sqliteFile;
    private final Map<String, Long> nextClaims = new ConcurrentHashMap<>();
    private final ExecutorService storageExecutor = Executors.newSingleThreadExecutor(task -> {
        Thread thread = new Thread(task, "LuckyWheel-Claims");
        thread.setDaemon(true);
        return thread;
    });
    private final boolean sqlite;
    private YamlConfiguration yamlClaims;

    public ClaimManager(LuckyWheelPlugin plugin) {
        this.plugin = plugin;
        this.claimsFile = new File(plugin.getDataFolder(), "claims.yml");
        this.sqliteFile = new File(plugin.getDataFolder(), plugin.getConfig().getString("claims.sqlite-file", "claims.db"));
        this.sqlite = !plugin.getConfig().getString("claims.storage", "sqlite").equalsIgnoreCase("yaml");
        loadStorage();
    }

    public long getNextClaimAt(UUID playerId, String wheelName) {
        return nextClaims.getOrDefault(key(playerId, wheelName), 0L);
    }

    public boolean isAvailable(UUID playerId, String wheelName, long now) {
        return getNextClaimAt(playerId, wheelName) <= now;
    }

    public synchronized boolean claim(UUID playerId, String wheelName, long cooldownMillis, long now) {
        String claimKey = key(playerId, wheelName);
        if (nextClaims.getOrDefault(claimKey, 0L) > now) {
            return false;
        }

        long nextClaimAt = Math.addExact(now, cooldownMillis);
        nextClaims.put(claimKey, nextClaimAt);
        enqueueSave(playerId, wheelName, nextClaimAt);
        return true;
    }

    public synchronized void reload() {
        flushStorage();
        nextClaims.clear();
        loadStorage();
    }

    public void shutdown() {
        flushStorage();
        storageExecutor.shutdown();
        try {
            if (!storageExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                storageExecutor.shutdownNow();
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            storageExecutor.shutdownNow();
        }
    }

    public static long parseDurationMillis(String value) {
        if (value == null) {
            return -1L;
        }

        Matcher matcher = DURATION_PATTERN.matcher(value.trim());
        if (!matcher.matches()) {
            return -1L;
        }

        try {
            long amount = Long.parseLong(matcher.group(1));
            Duration duration = switch (matcher.group(2).toLowerCase()) {
                case "s" -> Duration.ofSeconds(amount);
                case "m" -> Duration.ofMinutes(amount);
                case "h" -> Duration.ofHours(amount);
                case "d" -> Duration.ofDays(amount);
                case "w" -> Duration.ofDays(Math.multiplyExact(amount, 7L));
                default -> null;
            };
            return duration == null ? -1L : duration.toMillis();
        } catch (ArithmeticException | NumberFormatException exception) {
            return -1L;
        }
    }

    private void loadStorage() {
        if (sqlite) {
            loadSqlite();
        } else {
            loadYaml();
        }
    }

    private void loadSqlite() {
        try (Connection connection = openSqliteConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS claims ("
                    + "player_uuid TEXT NOT NULL, wheel_name TEXT NOT NULL, next_claim_at INTEGER NOT NULL, "
                    + "PRIMARY KEY (player_uuid, wheel_name))");

            try (ResultSet resultSet = statement.executeQuery("SELECT player_uuid, wheel_name, next_claim_at FROM claims")) {
                while (resultSet.next()) {
                    nextClaims.put(key(UUID.fromString(resultSet.getString("player_uuid")), resultSet.getString("wheel_name")),
                            resultSet.getLong("next_claim_at"));
                }
            }

            if (nextClaims.isEmpty() && claimsFile.isFile()) {
                migrateYamlToSqlite(connection);
            }
        } catch (SQLException | IllegalArgumentException exception) {
            throw new IllegalStateException("Impossible de charger la base SQLite des claims.", exception);
        }
    }

    private void migrateYamlToSqlite(Connection connection) throws SQLException {
        YamlConfiguration legacyClaims = YamlConfiguration.loadConfiguration(claimsFile);
        if (legacyClaims.getConfigurationSection("players") == null) {
            return;
        }

        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT OR IGNORE INTO claims (player_uuid, wheel_name, next_claim_at) VALUES (?, ?, ?)")) {
            for (String playerId : legacyClaims.getConfigurationSection("players").getKeys(false)) {
                try {
                    UUID playerUuid = UUID.fromString(playerId);
                    if (legacyClaims.getConfigurationSection("players." + playerId) == null) {
                        continue;
                    }
                    for (String wheelName : legacyClaims.getConfigurationSection("players." + playerId).getKeys(false)) {
                        long nextClaimAt = legacyClaims.getLong(
                                "players." + playerId + "." + wheelName + ".next-claim-at", 0L);
                        if (nextClaimAt <= 0L) {
                            continue;
                        }
                        nextClaims.put(key(playerUuid, wheelName), nextClaimAt);
                        statement.setString(1, playerUuid.toString());
                        statement.setString(2, wheelName.toLowerCase());
                        statement.setLong(3, nextClaimAt);
                        statement.addBatch();
                    }
                } catch (IllegalArgumentException exception) {
                    plugin.getLogger().warning("UUID invalide ignoré dans claims.yml : " + playerId);
                }
            }
            statement.executeBatch();
        }

        plugin.getLogger().info("Anciens claims importés depuis claims.yml vers SQLite.");
    }

    private void loadYaml() {
        yamlClaims = YamlConfiguration.loadConfiguration(claimsFile);
        if (yamlClaims.getConfigurationSection("players") == null) {
            return;
        }

        for (String playerId : yamlClaims.getConfigurationSection("players").getKeys(false)) {
            if (yamlClaims.getConfigurationSection("players." + playerId) == null) {
                continue;
            }
            for (String wheelName : yamlClaims.getConfigurationSection("players." + playerId).getKeys(false)) {
                long nextClaimAt = yamlClaims.getLong("players." + playerId + "." + wheelName + ".next-claim-at", 0L);
                if (nextClaimAt > 0L) {
                    nextClaims.put(playerId.toLowerCase() + ":" + wheelName.toLowerCase(), nextClaimAt);
                }
            }
        }
    }

    private void enqueueSave(UUID playerId, String wheelName, long nextClaimAt) {
        if (sqlite) {
            storageExecutor.execute(() -> saveSqlite(playerId, wheelName, nextClaimAt));
        } else {
            storageExecutor.execute(() -> saveYaml(playerId, wheelName, nextClaimAt));
        }
    }

    private void saveSqlite(UUID playerId, String wheelName, long nextClaimAt) {
        try (Connection connection = openSqliteConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO claims (player_uuid, wheel_name, next_claim_at) VALUES (?, ?, ?) "
                             + "ON CONFLICT(player_uuid, wheel_name) DO UPDATE SET next_claim_at = excluded.next_claim_at")) {
            statement.setString(1, playerId.toString());
            statement.setString(2, wheelName.toLowerCase());
            statement.setLong(3, nextClaimAt);
            statement.executeUpdate();
        } catch (SQLException exception) {
            plugin.getLogger().severe("Impossible de sauvegarder un claim SQLite : " + exception.getMessage());
        }
    }

    private synchronized void saveYaml(UUID playerId, String wheelName, long nextClaimAt) {
        yamlClaims.set("players." + playerId + "." + wheelName.toLowerCase() + ".next-claim-at", nextClaimAt);
        try {
            yamlClaims.save(claimsFile);
        } catch (IOException exception) {
            plugin.getLogger().severe("Impossible de sauvegarder les claims YAML : " + exception.getMessage());
        }
    }

    private void flushStorage() {
        try {
            storageExecutor.submit(() -> { }).get(5, TimeUnit.SECONDS);
        } catch (Exception exception) {
            plugin.getLogger().warning("Les sauvegardes de claims n'ont pas pu être vidées proprement.");
        }
    }

    private Connection openSqliteConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + sqliteFile.getAbsolutePath());
    }

    private String key(UUID playerId, String wheelName) {
        return playerId.toString().toLowerCase() + ":" + wheelName.toLowerCase();
    }
}
