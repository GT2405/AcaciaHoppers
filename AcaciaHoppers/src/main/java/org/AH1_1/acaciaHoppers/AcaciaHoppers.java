package org.AH1_1.acaciaHoppers;

import org.AH1_1.acaciaHoppers.commands.AcaciaHoppersTabCompleter;
import org.AH1_1.acaciaHoppers.managers.*;
import org.AH1_1.acaciaHoppers.gui.SuperChestGUI;
import org.AH1_1.acaciaHoppers.gui.HopperFilterGUI;
import org.AH1_1.acaciaHoppers.hoppers.HopperFilter;
import org.AH1_1.acaciaHoppers.commands.AcaciaHoppersCommand;
import org.AH1_1.acaciaHoppers.listeners.SuperChestListener;
import org.AH1_1.acaciaHoppers.listeners.UnifiedGUIListener;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AcaciaHoppers extends JavaPlugin {

    private SuperChestManager superChestManager;
    private SuperChestHologramManager superChestHologramManager;
    private SuperChestGUI superChestGUI;
    private HopperManager hopperManager;
    private HopperFilterGUI hopperFilterGUI;
    private InventoryManager inventoryManager;
    private PlayerDataManager playerDataManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        inventoryManager = new InventoryManager(this);
        superChestManager = new SuperChestManager(this);
        superChestHologramManager = new SuperChestHologramManager(this);
        hopperManager = new HopperManager(this);
        playerDataManager = new PlayerDataManager(this);

        superChestGUI = new SuperChestGUI(this);
        hopperFilterGUI = new HopperFilterGUI(this);

        initializeHopperFilters();
        registerListeners();
        registerCommands();
        runDebugLogs();
        startAutoSave();

        getLogger().info("[AcaciaHoppers] Plugin enabled successfully!");
    }

    @Override
    public void onDisable() {
        if (superChestManager != null) superChestManager.saveAll();
        if (hopperManager != null) hopperManager.saveAll();
        getLogger().info("[AcaciaHoppers] Plugin disabled. Data saved.");
    }

    private void initializeHopperFilters() {
        List<String> hopperGUIs = List.of(
                "Crop Hopper Filter",
                "Mob Hopper Filter",
                "Fish Hopper Filter",
                "Ore Hopper Filter",
                "Tree Hopper Filter"
        );
        for (String guiTitle : hopperGUIs) {
            HopperFilter filter = new HopperFilter(List.of());
            hopperManager.registerHopperFilter(guiTitle, filter);
        }
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new SuperChestListener(this), this);
        getServer().getPluginManager().registerEvents(new UnifiedGUIListener(this), this);
    }

    private void registerCommands() {
        var command = getCommand("acaciahoppers");
        if (command != null) {
            command.setExecutor(new AcaciaHoppersCommand(this));
            command.setTabCompleter(new AcaciaHoppersTabCompleter());
        } else {
            getLogger().warning("The command 'acaciahoppers' is not defined in plugin.yml!");
        }
    }

    private void runDebugLogs() {
        if (getConfig().getBoolean("debug.hopper")) {
            getLogger().info("[DEBUG] Loaded " + hopperManager.getAllHoppers().size() + " hoppers.");
            hopperManager.getLoadErrors().forEach(err -> getLogger().warning("[DEBUG] Hopper load error: " + err));
        }
        if (getConfig().getBoolean("debug.superchest")) {
            getLogger().info("[DEBUG] Loaded " + superChestManager.getSuperChests().size() + " super chests.");
            superChestManager.getLoadErrors().forEach(err -> getLogger().warning("[DEBUG] SuperChest load error: " + err));
        }
        if (getConfig().getBoolean("debug.holograms")) {
            getLogger().info("[DEBUG] Loaded " + superChestHologramManager.getHologramCount() + " holograms.");
        }
        if (getConfig().getBoolean("debug.commands")) getLogger().info("[DEBUG] Commands registered successfully.");
        if (getConfig().getBoolean("debug.config")) getLogger().info("[DEBUG] Config loaded and applied successfully.");
    }

    private void startAutoSave() {
        int autosaveMinutes = getConfig().getInt("debug.autosaveInterval", 5);
        if (autosaveMinutes <= 0) return;

        long ticks = autosaveMinutes * 60L * 20L;
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            try {
                hopperManager.saveAll();
                superChestManager.saveAll();
                getLogger().info("[AUTOSAVE] Hopper and Super Chest data saved.");
            } catch (Exception e) {
                getLogger().warning("[AUTOSAVE] Error during auto-save: " + e.getMessage());
            }
        }, ticks, ticks);
    }

    public String getMessage(String path) {
        return getMessage(path, null);
    }

    public String getMessage(String path, Map<String, String> placeholders) {
        String msg = getConfig().getString("messages." + path, "&cMissing message: " + path);

        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                msg = msg.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }

        return msg.replace("&", "§");
    }

    public boolean isSuperChestItem(ItemStack item) {
        if (item == null) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        var nameComponent = meta.displayName();
        if (nameComponent == null) return false;

        String displayName = PlainTextComponentSerializer.plainText().serialize(nameComponent);
        String configuredName = getMessage("names.superChest");
        return Objects.equals(displayName, configuredName);
    }

    public String getItemDisplayName(ItemStack item) {
        if (item == null) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;

        var nameComponent = meta.displayName();
        if (nameComponent == null) return null;

        return PlainTextComponentSerializer.plainText().serialize(nameComponent);
    }

    // Getters
    public SuperChestManager getSuperChestManager() { return superChestManager; }
    public SuperChestHologramManager getSuperChestHologramManager() { return superChestHologramManager; }
    public SuperChestGUI getSuperChestGUI() { return superChestGUI; }
    public HopperManager getHopperManager() { return hopperManager; }
    public HopperFilterGUI getHopperFilterGUI() { return hopperFilterGUI; }
    public InventoryManager getInventoryManager() { return inventoryManager; }
    public PlayerDataManager getPlayerDataManager() { return playerDataManager; }
}
