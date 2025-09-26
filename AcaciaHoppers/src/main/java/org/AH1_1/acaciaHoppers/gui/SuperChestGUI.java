package org.AH1_1.acaciaHoppers.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.AH1_1.acaciaHoppers.AcaciaHoppers;
import org.AH1_1.acaciaHoppers.managers.SuperChestManager;
import org.AH1_1.acaciaHoppers.managers.SuperChestHologramManager;
import org.AH1_1.acaciaHoppers.utils.SuperChest;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class SuperChestGUI {

    private final AcaciaHoppers plugin;
    private final SuperChestManager superChestManager;
    private final SuperChestHologramManager hologramManager;
    private final Map<UUID, Integer> playerPages = new HashMap<>();

    public SuperChestGUI(AcaciaHoppers plugin) {
        this.plugin = plugin;
        this.superChestManager = plugin.getSuperChestManager();
        this.hologramManager = plugin.getSuperChestHologramManager();
    }

    public void openSuperChestGUI(Player player, UUID chestUUID) {
        openSuperChestGUI(player, chestUUID, 1);
    }

    private void openSuperChestGUI(Player player, UUID chestUUID, int page) {
        SuperChest chest = superChestManager.getSuperChestByUUID(chestUUID);
        if (chest == null) {
            player.sendMessage(plugin.getMessage("superchest.notFound"));
            return;
        }

        int storageRows = plugin.getConfig().getInt("superChest.storageRows", 5);
        int maxItemsPerPage = storageRows * 9 - 9;
        List<ItemStack> items = chest.getItems();

        int maxPages = (items.size() + maxItemsPerPage - 1) / maxItemsPerPage;
        page = Math.max(1, Math.min(page, maxPages));
        playerPages.put(player.getUniqueId(), page);

        int size = storageRows * 9;
        Component title = Component.text(plugin.getConfig().getString("names.superChest", "Super Chest") + " - " + chest.getName(), NamedTextColor.GOLD);
        Inventory inv = Bukkit.createInventory(player, size, title);
        plugin.getInventoryManager().setInventoryTitle(player.getUniqueId(), chest.getOwnerUUID().toString());

        addItemsToInventory(chest, inv, page);
        player.openInventory(inv);
    }

    private void addItemsToInventory(SuperChest chest, Inventory inv, int page) {
        int storageRows = plugin.getConfig().getInt("superChest.storageRows", 5);
        int maxItemsPerPage = storageRows * 9 - 9;
        List<ItemStack> items = chest.getItems();
        int startIndex = (page - 1) * maxItemsPerPage;
        int endIndex = Math.min(startIndex + maxItemsPerPage, items.size());

        int slot = 0;
        for (int i = startIndex; i < endIndex; i++) {
            while (slot / 9 == storageRows - 1) slot++;
            ItemStack item = items.get(i);
            if (item != null) inv.setItem(slot, item.clone());
            slot++;
        }

        int lastRowStart = storageRows * 9 - 9;
        inv.setItem(lastRowStart, createButton("PREVIOUS_PAGE"));
        inv.setItem(lastRowStart + 1, createButton("HOLOGRAM_TOGGLE"));
        inv.setItem(lastRowStart + 4, createButton("CLOSE_GUI"));
        inv.setItem(lastRowStart + 8, createButton("NEXT_PAGE"));
    }

    private ItemStack createButton(String key) {
        String matName = plugin.getConfig().getString("gui_items." + key);
        Material mat = (matName != null) ? Material.matchMaterial(matName) : Material.BARRIER;
        if (mat == null) mat = Material.BARRIER;

        ItemStack item = new ItemStack(mat);
        String displayStr = plugin.getConfig().getString("gui_labels." + key, key);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(displayStr, NamedTextColor.YELLOW));
            item.setItemMeta(meta);
        }
        return item;
    }

    public void handleItemClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        String chestStr = plugin.getInventoryManager().getInventoryTitle(player.getUniqueId());
        if (chestStr == null) return;

        UUID chestUUID;
        try {
            chestUUID = UUID.fromString(chestStr);
        } catch (IllegalArgumentException e) {
            player.sendMessage(plugin.getMessage("superchest.notFound"));
            return;
        }

        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        int currentPage = playerPages.getOrDefault(player.getUniqueId(), 1);

        // ✅ Null-safe display name
        String displayName = plugin.getItemDisplayName(clicked);
        if (displayName == null) return;

        switch (displayName) {
            case "Close GUI", "§cClose GUI" -> {
                player.closeInventory();
                player.sendMessage(plugin.getMessage("superchest.closed"));
            }
            case "Previous Page", "§ePrevious Page" -> openSuperChestGUI(player, chestUUID, currentPage - 1);
            case "Next Page", "§eNext Page" -> openSuperChestGUI(player, chestUUID, currentPage + 1);
            case "Toggle Hologram", "§bToggle Hologram" -> {
                SuperChest chest = plugin.getSuperChestManager().getSuperChestByUUID(chestUUID);
                if (chest == null) return;
                Location loc = chest.getLocation();
                if (loc != null) {
                    chest.setHologramEnabled(!chest.isHologramEnabled());
                    if (chest.isHologramEnabled()) {
                        hologramManager.createHologram(loc, chest.getOwnerUUID());
                        player.sendMessage(plugin.getMessage("superchest.hologramEnabled"));
                    } else {
                        hologramManager.removeHologram(loc);
                        player.sendMessage(plugin.getMessage("superchest.hologramDisabled"));
                    }
                    superChestManager.saveAll();
                }
            }
            default -> {
                SuperChest chest = plugin.getSuperChestManager().getSuperChestByUUID(chestUUID);
                if (chest == null) return;
                chest.removeItem(clicked);
                player.sendMessage(plugin.getMessage("superchest.itemRemoved", Map.of("item", clicked.getType().name())));
                openSuperChestGUI(player, chestUUID, currentPage);
            }
        }
    }

    public void handleInventoryClose(InventoryCloseEvent event) {
        playerPages.remove(event.getPlayer().getUniqueId());
        plugin.getInventoryManager().removeInventoryTitle(event.getPlayer().getUniqueId());
    }
}
