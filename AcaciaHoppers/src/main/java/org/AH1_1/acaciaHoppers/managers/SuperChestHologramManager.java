package org.AH1_1.acaciaHoppers.managers;

import org.AH1_1.acaciaHoppers.AcaciaHoppers;
import org.AH1_1.acaciaHoppers.utils.SuperChest;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SuperChestHologramManager {

    private final AcaciaHoppers plugin;
    private final Map<UUID, Object> holograms = new HashMap<>(); // Replace Object with your hologram type
    private final List<String> loadErrors = new ArrayList<>();

    public SuperChestHologramManager(AcaciaHoppers plugin) {
        this.plugin = plugin;
    }

    public void createHologram(Location loc, UUID ownerUUID) {
        if (loc == null || ownerUUID == null) return;

        SuperChest chest = plugin.getSuperChestManager().getSuperChestByUUID(ownerUUID);
        if (chest == null) {
            loadErrors.add("Failed to create hologram: SuperChest not found for UUID " + ownerUUID);
            return;
        }

        boolean holoEnabledInConfig = plugin.getConfig().getBoolean("holograms.enabled.superchest", true);

        if (chest.isHologramEnabled() && holoEnabledInConfig) {
            try {
                String playerName = Bukkit.getOfflinePlayer(chest.getOwnerUUID()).getName();
                String text = plugin.getConfig()
                        .getString("holograms.ownerFormat", "&c&lOwned by: &b&l[playername]")
                        .replace("[playername]", playerName != null ? playerName : "Unknown");
                Object holo = spawnHologram(loc, text);
                holograms.put(ownerUUID, holo);
            } catch (Exception e) {
                loadErrors.add("Failed to spawn hologram for SuperChest UUID " + ownerUUID + ": " + e.getMessage());
            }
        }
    }

    @SuppressWarnings("unused")
    public void toggleHologram(Location loc) {
        if (loc == null) return;

        SuperChest chest = plugin.getSuperChestManager().getSuperChestByLocation(loc);
        if (chest == null) {
            loadErrors.add("Failed to toggle hologram: SuperChest not found at location " + loc);
            return;
        }

        boolean holoEnabledInConfig = plugin.getConfig().getBoolean("holograms.enabled.superchest", true);

        try {
            if (chest.isHologramEnabled()) {
                removeHologram(loc);
                chest.setHologramEnabled(false);
            } else if (holoEnabledInConfig) {
                String playerName = Bukkit.getOfflinePlayer(chest.getOwnerUUID()).getName();
                String text = plugin.getConfig()
                        .getString("holograms.ownerFormat", "&c&lOwned by: &b&l[playername]")
                        .replace("[playername]", playerName != null ? playerName : "Unknown");
                Object holo = spawnHologram(loc, text);
                holograms.put(chest.getOwnerUUID(), holo);
                chest.setHologramEnabled(true);
            }
            plugin.getSuperChestManager().saveAll();
        } catch (Exception e) {
            loadErrors.add("Error toggling hologram for SuperChest at " + loc + ": " + e.getMessage());
        }
    }

    public void removeHologram(Location loc) {
        SuperChest chest = plugin.getSuperChestManager().getSuperChestByLocation(loc);
        if (chest == null) {
            loadErrors.add("Failed to remove hologram: SuperChest not found at location " + loc);
            return;
        }

        UUID ownerUUID = chest.getOwnerUUID();
        Object holo = holograms.remove(ownerUUID);
        if (holo != null) {
            try {
                destroyHologram(holo);
            } catch (Exception e) {
                loadErrors.add("Error removing hologram for SuperChest at " + loc + ": " + e.getMessage());
            }
        }
    }

    private Object spawnHologram(Location loc, String text) {
        plugin.getLogger().info("[SuperChestHologramManager] Spawned hologram at " + loc + " with text: " + text);
        return new Object(); // placeholder
    }

    private void destroyHologram(@SuppressWarnings("unused") Object holo) {
        plugin.getLogger().info("[SuperChestHologramManager] Hologram removed.");
    }

    public int getHologramCount() {
        return holograms.size();
    }

    @SuppressWarnings("unused")
    public List<String> getLoadErrors() {
        return new ArrayList<>(loadErrors);
    }
}
