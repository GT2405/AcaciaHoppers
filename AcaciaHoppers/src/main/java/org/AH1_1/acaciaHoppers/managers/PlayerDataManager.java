package org.AH1_1.acaciaHoppers.managers;

import org.AH1_1.acaciaHoppers.AcaciaHoppers;
import org.AH1_1.acaciaHoppers.data.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PlayerDataManager {

    private final AcaciaHoppers plugin;
    private final Map<UUID, PlayerData> playerDataMap = new HashMap<>();
    private final File dataFile;
    private final YamlConfiguration dataConfig;

    public PlayerDataManager(AcaciaHoppers plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "players.yml");
        this.dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    // ----------------------------
    // PlayerData Access
    // ----------------------------
    public PlayerData getPlayerData(UUID uuid) {
        return playerDataMap.computeIfAbsent(uuid, PlayerData::new);
    }

    public PlayerData getPlayerData(Player player) {
        return getPlayerData(player.getUniqueId());
    }

    // ----------------------------
    // Load/Save Single Player
    // ----------------------------
    @SuppressWarnings("unused")
    public void loadPlayerData(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerData data = new PlayerData(uuid);
        data.setIgn(player.getName());

        loadHopperSettingsFromConfig(data, "players." + uuid + ".hopperSettings");
        playerDataMap.put(uuid, data);
    }

    public void savePlayerData(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerData data = playerDataMap.get(uuid);
        if (data == null) return;

        saveHopperSettingsToConfig(data, "players." + uuid + ".hopperSettings");
        dataConfig.set("players." + uuid + ".ign", data.getIgn());

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save player data for " + player.getName() + ": " + e.getMessage());
        }
    }

    // ----------------------------
    // Load/Save All Players
    // ----------------------------
    @SuppressWarnings("unused")
    public void loadAll() {
        var playersSection = dataConfig.getConfigurationSection("players");
        if (playersSection == null) return;

        for (String key : playersSection.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                PlayerData data = new PlayerData(uuid);

                String ign = dataConfig.getString("players." + key + ".ign");
                data.setIgn(ign);

                loadHopperSettingsFromConfig(data, "players." + key + ".hopperSettings");

                playerDataMap.put(uuid, data);
            } catch (IllegalArgumentException ignored) {}
        }

        plugin.getLogger().info("[PlayerDataManager] Loaded " + playerDataMap.size() + " players.");
    }

    public void saveAll() {
        for (Map.Entry<UUID, PlayerData> entry : playerDataMap.entrySet()) {
            UUID uuid = entry.getKey();
            PlayerData data = entry.getValue();

            saveHopperSettingsToConfig(data, "players." + uuid + ".hopperSettings");
            dataConfig.set("players." + uuid + ".ign", data.getIgn());
        }

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save all player data: " + e.getMessage());
        }

        if (plugin.getConfig().getBoolean("debug", false)) {
            plugin.getLogger().info("[PlayerDataManager] Saved " + playerDataMap.size() + " players.");
        }
    }

    // ----------------------------
    // Unload Player Data
    // ----------------------------
    @SuppressWarnings("unused")
    public void unloadPlayer(Player player) {
        playerDataMap.remove(player.getUniqueId());
    }

    // ----------------------------
    // Private Helpers
    // ----------------------------
    private void loadHopperSettingsFromConfig(PlayerData data, String basePath) {
        var hopperSection = dataConfig.getConfigurationSection(basePath);
        if (hopperSection == null) return;

        for (String hopperLoc : hopperSection.getKeys(false)) {
            var settingsSection = hopperSection.getConfigurationSection(hopperLoc);
            if (settingsSection == null) continue;

            for (String key : settingsSection.getKeys(false)) {
                boolean value = settingsSection.getBoolean(key, true);
                data.setHopperSetting(hopperLoc, key, value);
            }
        }
    }

    private void saveHopperSettingsToConfig(PlayerData data, String basePath) {
        for (var hopperEntry : data.getAllHopperSettings().entrySet()) {
            String hopperLoc = hopperEntry.getKey();
            for (var settingEntry : hopperEntry.getValue().entrySet()) {
                dataConfig.set(basePath + "." + hopperLoc + "." + settingEntry.getKey(), settingEntry.getValue());
            }
        }
    }
}
