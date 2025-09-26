package org.AH1_1.acaciaHoppers.listeners;

import org.AH1_1.acaciaHoppers.AcaciaHoppers;
import org.AH1_1.acaciaHoppers.utils.SuperChest;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;


import java.util.List;
import java.util.UUID;

public class HopperEventListener implements Listener {

    private final AcaciaHoppers plugin;

    public HopperEventListener(AcaciaHoppers plugin) {
        this.plugin = plugin;
    }

    // Handle placing a SuperChest
    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        Block block = event.getBlockPlaced();
        Player player = event.getPlayer();

        if (block.getType() == Material.CHEST && plugin.isSuperChestItem(player.getInventory().getItemInMainHand())) {
            String chestName = player.getName() + "_SuperChest_" + System.currentTimeMillis();
            UUID ownerUUID = player.getUniqueId();

            SuperChest chest = new SuperChest(ownerUUID, chestName, block.getLocation(), List.of());
            plugin.getSuperChestManager().addSuperChest(chest);
            plugin.getSuperChestManager().saveAll();

            player.sendMessage("§aSuperChest placed! Hologram created above the chest.");
        }
    }

    // Handle breaking a SuperChest
    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Block block = event.getBlock();

        plugin.getSuperChestManager().getSuperChests().values().stream()
                .filter(chest -> chest.getLocation().equals(block.getLocation()))
                .findFirst()
                .ifPresent(chest -> {
                    plugin.getSuperChestManager().removeSuperChest(chest.getOwnerUUID());
                    plugin.getSuperChestManager().saveAll();
                    event.getPlayer().sendMessage("§cSuperChest removed! Hologram deleted.");
                });
    }

    // Handle interacting with a SuperChest
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (block != null) {
            plugin.getSuperChestManager().getSuperChests().values().stream()
                    .filter(chest -> chest.getLocation().equals(block.getLocation()))
                    .findFirst()
                    .ifPresent(chest -> plugin.getSuperChestGUI().openSuperChestGUI(player, chest.getOwnerUUID()));
        }
    }

    // Handle clicks in SuperChest GUI
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        String chestUUIDStr = plugin.getInventoryManager().getInventoryTitle(player.getUniqueId());
        if (chestUUIDStr == null) return;

        event.setCancelled(true);
        plugin.getSuperChestGUI().handleItemClick(event);
    }

    // Handle closing SuperChest GUI
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        plugin.getSuperChestGUI().handleInventoryClose(event);
    }

}
