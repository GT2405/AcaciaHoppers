package org.AH1_1.acaciaHoppers.managers;

import org.AH1_1.acaciaHoppers.AcaciaHoppers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InventoryManager {

    private final AcaciaHoppers plugin;
    private final Map<UUID, String> inventoryTitles = new HashMap<>();

    public InventoryManager(AcaciaHoppers plugin) {
        this.plugin = plugin;
    }

    // ------------------------
    // Inventory title management
    // ------------------------

    public void setInventoryTitle(UUID playerId, String title) {
        inventoryTitles.put(playerId, title);
        if (plugin.getConfig().getBoolean("debug", false)) {
            plugin.getLogger().info("[InventoryManager] Set title '" + title + "' for player " + playerId);
        }
    }

    public String getInventoryTitle(UUID playerId) {
        return inventoryTitles.get(playerId);
    }

    public void removeInventoryTitle(UUID playerId) {
        inventoryTitles.remove(playerId);
        if (plugin.getConfig().getBoolean("debug", false)) {
            plugin.getLogger().info("[InventoryManager] Removed title for player " + playerId);
        }
    }

    @SuppressWarnings("unused")
    public void clearAll() {
        inventoryTitles.clear();
        if (plugin.getConfig().getBoolean("debug", false)) {
            plugin.getLogger().info("[InventoryManager] Cleared all inventory titles.");
        }
    }

    // ------------------------
    // Inventory type checks
    // ------------------------

    public boolean isSuperChestInventory(String title) {
        return title != null && (title.contains("Super Chest") || title.startsWith("Super Chest"));
    }

    public boolean isHopperFilterInventory(String title) {
        return title != null && (title.contains("Hopper Filter") || title.endsWith("Filter"));
    }
}
