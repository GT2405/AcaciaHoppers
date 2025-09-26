package org.AH1_1.acaciaHoppers.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.AH1_1.acaciaHoppers.AcaciaHoppers;
import org.AH1_1.acaciaHoppers.hoppers.Hopper;
import org.AH1_1.acaciaHoppers.hoppers.HopperFilter;
import org.AH1_1.acaciaHoppers.hoppers.HopperType;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.Location;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class HopperManager {

    private final AcaciaHoppers plugin;
    private final Map<HopperType, Hopper> hoppers = new HashMap<>();
    private final Map<Material, HopperType> itemToHopperTypeCache = new HashMap<>();
    private final Map<Location, Hopper> customHopperLocations = new HashMap<>();
    private final Map<String, HopperFilter> hopperFilters = new HashMap<>();
    private final List<String> loadErrors = new ArrayList<>();

    public HopperManager(AcaciaHoppers plugin) {
        this.plugin = plugin;
        loadHopperSettings();
    }

    private void loadHopperSettings() {
        var config = plugin.getConfig();

        for (HopperType type : HopperType.values()) {
            try {
                String path = "hoppers." + type.name().toLowerCase();

                double itemsPerTick = config.getDouble(path + ".itemsPerTick", 1.0);
                double maxItemsPerSecond = config.getDouble(path + ".maxItemsPerSecond", 64.0);

                Hopper hopper = new Hopper(type, itemsPerTick, maxItemsPerSecond, BlockFace.DOWN);
                hoppers.put(type, hopper);

                List<String> allowed = config.getStringList(path + ".items");
                for (String matName : allowed) {
                    try {
                        itemToHopperTypeCache.put(Material.valueOf(matName.toUpperCase()), type);
                    } catch (IllegalArgumentException e) {
                        loadErrors.add("Invalid material '" + matName + "' for hopper type " + type.name());
                    }
                }

            } catch (Exception e) {
                loadErrors.add("Error loading hopper type " + type.name() + ": " + e.getMessage());
            }
        }
    }

    // --- Hopper Filter Accessors ---
    public HopperFilter getHopperFilter(HopperType type) {
        String guiTitle = plugin.getConfig().getString("gui_labels." + type.name().toUpperCase() + "_FILTER", type.name() + " Filter");
        return hopperFilters.get(guiTitle);
    }
    @SuppressWarnings("unused")
    public HopperFilter getHopperFilterByGuiTitle(String title) {
        return hopperFilters.get(title);
    }

    public void registerHopperFilter(String guiTitle, HopperFilter filter) {
        hopperFilters.put(guiTitle, filter);
    }
    @SuppressWarnings("unused")
    public Inventory getCustomHopperInventoryAt(Location loc) {
        Hopper hopper = customHopperLocations.get(loc);
        return hopper != null ? hopper.getInventory() : null;
    }

    public Hopper getCustomHopperAt(Location loc) {
        return customHopperLocations.get(loc);
    }

    public void handleFilterClose(String title, org.bukkit.entity.Player player) {
        if (plugin.getConfig().getBoolean("debug.hopper", false))
            plugin.getLogger().info("[HopperManager] Filter GUI closed for: " + title + " by " + player.getName());
    }

    public void saveAll() {
        var config = plugin.getConfig();

        for (HopperType type : HopperType.values()) {
            List<String> materials = new ArrayList<>();
            for (var entry : itemToHopperTypeCache.entrySet())
                if (entry.getValue() == type) materials.add(entry.getKey().name());

            config.set("hoppers." + type.name().toLowerCase() + ".items", materials);
        }

        plugin.saveConfig();
    }
    @SuppressWarnings("unused")
    public void registerCustomHopper(Location loc, Hopper hopper) {
        customHopperLocations.put(loc, hopper);
    }
    @SuppressWarnings("unused")
    public void unregisterCustomHopper(Location loc) {
        customHopperLocations.remove(loc);
    }

    // ================================
    // 🔹 Debug / Accessors
    // ================================
    public List<Hopper> getAllHoppers() {
        return new ArrayList<>(hoppers.values());
    }

    public List<String> getLoadErrors() {
        return loadErrors;
    }

    // ================================
    // 🔹 Give Hopper Item
    // ================================
    public void giveHopperItem(org.bukkit.entity.Player player, HopperType type, int quantity) {
        if (quantity <= 0) return;

        ItemStack hopperItem = new ItemStack(Material.HOPPER, quantity);
        ItemMeta meta = hopperItem.getItemMeta();
        if (meta != null) {
            // Get name from config
            String name = plugin.getConfig().getString("names." + type.name().toLowerCase(), type.name());

            // Parse gradient/hex with MiniMessage
            Component componentName = MiniMessage.miniMessage().deserialize(name);

            // Apply to item safely
            meta.displayName(componentName);
            hopperItem.setItemMeta(meta);
        }

        player.getInventory().addItem(hopperItem);
        player.sendMessage(plugin.getMessage("hopper.placed", Map.of("item", type.name())));
    }
}
