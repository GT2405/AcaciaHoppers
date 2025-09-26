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

import java.util.ArrayList;
import java.util.UUID;

public class SuperChestPlaceListener implements Listener {

    private final AcaciaHoppers plugin;

    public SuperChestPlaceListener(AcaciaHoppers plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSuperChestPlace(BlockPlaceEvent event) {
        Block block = event.getBlockPlaced();
        Player player = event.getPlayer();

        // Only handle placing a chest
        if (block.getType() != Material.CHEST) return;

        // Use centralized null-safe method
        String displayName = plugin.getItemDisplayName(player.getInventory().getItemInMainHand());
        if (!"§6Super Chest".equals(displayName)) return;

        // Generate unique chest name and owner
        String chestName = player.getName() + "_SuperChest_" + System.currentTimeMillis();
        UUID ownerUUID = player.getUniqueId();

        // Create and register SuperChest
        SuperChest chest = new SuperChest(ownerUUID, chestName, block.getLocation(), new ArrayList<>());
        plugin.getSuperChestManager().addSuperChest(chest);
        plugin.getSuperChestManager().saveAll();

        String message = plugin.getMessage("superchest.placed");
        player.sendMessage(message);
    }

    @EventHandler
    public void onSuperChestBreak(BlockBreakEvent event) {
        Block block = event.getBlock();

        plugin.getSuperChestManager().getSuperChests().values().stream()
                .filter(chest -> chest.getLocation().equals(block.getLocation()))
                .findFirst()
                .ifPresent(chest -> {
                    plugin.getSuperChestManager().removeSuperChest(chest.getOwnerUUID());
                    plugin.getSuperChestManager().saveAll();

                    String message = plugin.getMessage("superchest.mined");
                    event.getPlayer().sendMessage(message);
                });
    }
}
