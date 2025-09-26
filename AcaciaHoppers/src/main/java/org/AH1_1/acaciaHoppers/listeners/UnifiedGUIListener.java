package org.AH1_1.acaciaHoppers.listeners;

import org.AH1_1.acaciaHoppers.AcaciaHoppers;
import org.AH1_1.acaciaHoppers.gui.HopperFilterGUI;
import org.AH1_1.acaciaHoppers.gui.SuperChestGUI;
import org.AH1_1.acaciaHoppers.managers.HopperManager;
import org.AH1_1.acaciaHoppers.utils.SuperChest;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Arrays;
import java.util.List;

public class UnifiedGUIListener implements Listener {

    private final AcaciaHoppers plugin;
    private final SuperChestGUI superChestGUI;
    private final HopperFilterGUI hopperFilterGUI;
    private final HopperManager hopperManager;

    private final List<String> HOPPER_FILTER_GUIS = Arrays.asList(
            "Crop Hopper Filter",
            "Mob Hopper Filter",
            "Fish Hopper Filter",
            "Ore Hopper Filter",
            "Tree Hopper Filter"
    );

    private final PlainTextComponentSerializer serializer = PlainTextComponentSerializer.plainText();

    public UnifiedGUIListener(AcaciaHoppers plugin) {
        this.plugin = plugin;
        this.superChestGUI = plugin.getSuperChestGUI();
        this.hopperFilterGUI = plugin.getHopperFilterGUI();
        this.hopperManager = plugin.getHopperManager();
    }

    // --------------------
    // Block break handling
    // --------------------
    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        SuperChest chest = plugin.getSuperChestManager().getSuperChestByLocation(event.getBlock().getLocation());
        if (chest != null) {
            plugin.getSuperChestManager().removeSuperChest(chest.getOwnerUUID());
            plugin.getSuperChestManager().saveAll();

            String message = plugin.getMessage("superchest.mined");
            event.getPlayer().sendMessage(message);
        }
    }

    // --------------------
    // Optional interactions
    // --------------------
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        // Optional: handle shift-click opening of hopper filters, etc.
    }

    // --------------------
    // Inventory click handling
    // --------------------
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        HumanEntity human = event.getWhoClicked();
        if (!(human instanceof Player)) return; // no pattern variable needed
        if (event.getClickedInventory() == null) return;

        String title = serializer.serialize(event.getView().title());

        // Hopper Filter GUI
        if (HOPPER_FILTER_GUIS.contains(title)) {
            hopperFilterGUI.handleItemClick(event);
            return;
        }

        // SuperChest GUI
        if (plugin.getInventoryManager().isSuperChestInventory(title)) {
            handleSuperChestClick(event);
        }
    }

    private void handleSuperChestClick(InventoryClickEvent event) {
        int slot = event.getSlot();
        int inventorySize = event.getInventory().getSize();
        int bottomRowStart = inventorySize - 9;

        if (slot >= bottomRowStart) {
            superChestGUI.handleItemClick(event);
        } else {
            event.setCancelled(false);
        }
    }

    // --------------------
    // Inventory drag handling
    // --------------------
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        String title = serializer.serialize(event.getView().title());
        int bottomRowStart = event.getInventory().getSize() - 9;

        if (plugin.getInventoryManager().isSuperChestInventory(title)) {
            for (int slot : event.getRawSlots()) {
                if (slot >= bottomRowStart) {
                    event.setCancelled(true);
                    return;
                }
            }
        } else if (plugin.getInventoryManager().isHopperFilterInventory(title)) {
            event.setCancelled(true);
        }
    }

    // --------------------
    // Inventory close handling
    // --------------------
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return; // no pattern variable needed

        String title = serializer.serialize(event.getView().title());

        if (HOPPER_FILTER_GUIS.contains(title)) {
            hopperManager.handleFilterClose(title, (Player) event.getPlayer());
        } else if (plugin.getInventoryManager().isSuperChestInventory(title)) {
            superChestGUI.handleInventoryClose(event);
        }
    }
}
