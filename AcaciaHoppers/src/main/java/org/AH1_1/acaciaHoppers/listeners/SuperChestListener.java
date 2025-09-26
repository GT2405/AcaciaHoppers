package org.AH1_1.acaciaHoppers.listeners;

import org.AH1_1.acaciaHoppers.AcaciaHoppers;
import org.AH1_1.acaciaHoppers.managers.SuperChestHologramManager;
import org.AH1_1.acaciaHoppers.utils.SuperChest;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.List;
import java.util.UUID;

public class SuperChestListener implements Listener {

    private final AcaciaHoppers plugin;
    private final SuperChestHologramManager hologramManager;

    public SuperChestListener(AcaciaHoppers plugin) {
        this.plugin = plugin;
        this.hologramManager = plugin.getSuperChestHologramManager();
    }

    @EventHandler
    public void onChestPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlockPlaced();

        if (block.getType() == Material.CHEST && plugin.isSuperChestItem(player.getInventory().getItemInMainHand())) {

            String chestName = player.getName() + "_SuperChest_" + System.currentTimeMillis();
            UUID ownerUUID = player.getUniqueId();

            SuperChest superChest = new SuperChest(ownerUUID, chestName, block.getLocation(), List.of());
            plugin.getSuperChestManager().addSuperChest(superChest);
            plugin.getSuperChestManager().saveAll();
            hologramManager.createHologram(block.getLocation(), ownerUUID);

            String message = plugin.getMessage("superchest.placed");
            player.sendMessage(message);
        }
    }

    @EventHandler
    public void onChestBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Location loc = block.getLocation();

        SuperChest chest = plugin.getSuperChestManager().getSuperChestByLocation(loc);
        if (chest != null) {
            plugin.getSuperChestManager().removeSuperChest(chest.getOwnerUUID());
            plugin.getSuperChestManager().saveAll();
            hologramManager.removeHologram(loc);

            String message = plugin.getMessage("superchest.mined");
            event.getPlayer().sendMessage(message);
        }
    }
}
