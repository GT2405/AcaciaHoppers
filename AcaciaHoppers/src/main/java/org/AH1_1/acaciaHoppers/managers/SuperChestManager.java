package org.AH1_1.acaciaHoppers.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.AH1_1.acaciaHoppers.AcaciaHoppers;
import org.AH1_1.acaciaHoppers.utils.SuperChest;
import org.AH1_1.acaciaHoppers.hoppers.Hopper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class SuperChestManager {

    private final AcaciaHoppers plugin;
    private final Map<UUID, SuperChest> superChests = new HashMap<>();
    private final File dataFile;
    private final YamlConfiguration dataConfig;
    private final List<String> loadErrors = new ArrayList<>();

    public SuperChestManager(AcaciaHoppers plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "superchests.yml");
        this.dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        loadAll();
        startExportTask();
    }

    public void addSuperChest(SuperChest chest) {
        try {
            superChests.put(chest.getOwnerUUID(), chest);
            if (chest.isHologramEnabled())
                plugin.getSuperChestHologramManager().createHologram(chest.getLocation(), chest.getOwnerUUID());
            saveAll();
        } catch (Exception e) {
            loadErrors.add("Error adding SuperChest for " + chest.getOwnerUUID() + ": " + e.getMessage());
        }
    }

    public SuperChest getSuperChestByUUID(UUID uuid) {
        return superChests.get(uuid);
    }

    public SuperChest getSuperChestByLocation(Location loc) {
        if (loc == null) return null;
        for (SuperChest chest : superChests.values()) {
            if (chest.getLocation() != null && chest.getLocation().equals(loc)) return chest;
        }
        return null;
    }

    public Map<UUID, SuperChest> getSuperChests() {
        return superChests;
    }

    @SuppressWarnings("unused")
    public Collection<SuperChest> getAllSuperChests() {
        return superChests.values();
    }

    public void removeSuperChest(UUID ownerUUID) {
        try {
            SuperChest chest = superChests.remove(ownerUUID);
            if (chest != null) {
                plugin.getSuperChestHologramManager().removeHologram(chest.getLocation());
                saveAll();
            }
        } catch (Exception e) {
            loadErrors.add("Error removing SuperChest for " + ownerUUID + ": " + e.getMessage());
        }
    }

    public void saveAll() {
        for (SuperChest chest : superChests.values()) {
            try {
                dataConfig.set(chest.getOwnerUUID().toString(), chest.serialize());
            } catch (Exception e) {
                loadErrors.add("Error serializing SuperChest for " + chest.getOwnerUUID() + ": " + e.getMessage());
            }
        }

        try {
            dataConfig.save(dataFile);
            if (plugin.getConfig().getBoolean("debug.superchest", false))
                plugin.getLogger().info("[SuperChestManager] Saved " + superChests.size() + " super chests.");
        } catch (IOException e) {
            loadErrors.add("Failed to save superchests.yml: " + e.getMessage());
        }
    }

    public void loadAll() {
        if (!dataFile.exists()) return;

        for (String key : dataConfig.getKeys(false)) {
            try {
                ConfigurationSection section = dataConfig.getConfigurationSection(key);
                if (section != null) {
                    SuperChest chest = SuperChest.deserialize(section);
                    superChests.put(chest.getOwnerUUID(), chest);

                    if (chest.isHologramEnabled())
                        plugin.getSuperChestHologramManager().createHologram(chest.getLocation(), chest.getOwnerUUID());
                }
            } catch (Exception e) {
                loadErrors.add("Error loading SuperChest from key " + key + ": " + e.getMessage());
            }
        }

        if (plugin.getConfig().getBoolean("debug.superchest", false) && !loadErrors.isEmpty()) {
            plugin.getLogger().info("[SuperChestManager] Load errors: ");
            loadErrors.forEach(err -> plugin.getLogger().warning(err));
        }
    }

    public void giveSuperChestItem(Player player) {
        try {
            ItemStack chestItem = new ItemStack(org.bukkit.Material.CHEST);
            ItemMeta meta = chestItem.getItemMeta();
            if (meta != null) {
                String name = plugin.getMessage("names.superChest");
                Component componentName = MiniMessage.miniMessage().deserialize(name);
                meta.displayName(componentName);
                chestItem.setItemMeta(meta);
            }
            player.getInventory().addItem(chestItem);
            player.sendMessage(plugin.getMessage("superchest.placed", Map.of("item", plugin.getMessage("names.superChest"))));
        } catch (Exception e) {
            loadErrors.add("Error giving SuperChest item to " + player.getName() + ": " + e.getMessage());
        }
    }

    private void startExportTask() {
        long interval = plugin.getConfig().getLong("superChest.autoSaveGUI", 20L);
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (SuperChest chest : superChests.values()) {
                try {
                    exportItemsBelow(chest);
                } catch (Exception e) {
                    loadErrors.add("Error exporting items for " + chest.getOwnerUUID() + ": " + e.getMessage());
                }
            }
        }, interval, interval);
    }

    private void exportItemsBelow(SuperChest chest) {
        if (chest.getLocation() == null || chest.getItems().isEmpty()) return;

        Block blockBelow = chest.getLocation().getBlock().getRelative(BlockFace.DOWN);
        Hopper hopperBelow = plugin.getHopperManager().getCustomHopperAt(blockBelow.getLocation());
        Inventory targetInventory = null;

        if (blockBelow.getState() instanceof Container container)
            targetInventory = container.getInventory();
        else if (hopperBelow != null)
            targetInventory = hopperBelow.getInventory();

        if (targetInventory == null) return;

        int itemsPerTick = 64; // fallback
        if (hopperBelow != null) itemsPerTick = (int) hopperBelow.getItemsPerTick();

        List<ItemStack> items = new ArrayList<>(chest.getItems());
        int processed = 0;

        for (ItemStack item : items) {
            if (item == null) continue;
            int toProcess = Math.min(item.getAmount(), itemsPerTick - processed);
            if (toProcess <= 0) break;

            ItemStack clone = item.clone();
            clone.setAmount(toProcess);

            HashMap<Integer, ItemStack> leftover = targetInventory.addItem(clone);

            int added = toProcess;
            if (!leftover.isEmpty())
                added -= leftover.values().iterator().next().getAmount();

            item.setAmount(item.getAmount() - added);
            if (item.getAmount() <= 0) chest.removeItem(item);

            processed += added;
        }
    }

    public List<String> getLoadErrors() {
        return new ArrayList<>(loadErrors);
    }
}
