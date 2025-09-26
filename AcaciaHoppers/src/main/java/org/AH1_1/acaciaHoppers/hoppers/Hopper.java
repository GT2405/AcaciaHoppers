package org.AH1_1.acaciaHoppers.hoppers;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.AH1_1.acaciaHoppers.data.PlayerData;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class Hopper {

    private final HopperType type;
    private final List<ItemStack> items;

    private final double itemsPerTick;
    private final double maxItemsPerSecond;
    private final BlockFace facing;

    private boolean filterActive;
    private boolean enabled;
    private boolean automaticMode;

    public Hopper(HopperType type, double itemsPerTick, double maxItemsPerSecond, BlockFace facing) {
        this.type = type;
        this.itemsPerTick = itemsPerTick;
        this.maxItemsPerSecond = maxItemsPerSecond;
        this.items = new ArrayList<>();
        this.facing = facing;

        this.filterActive = true;
        this.enabled = true;
        this.automaticMode = false;
    }

    public boolean isFilterActive() { return filterActive; }
    public void setFilterActive(boolean filterActive) { this.filterActive = filterActive; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public boolean isAutomaticMode() { return automaticMode; }
    public void setAutomaticMode(boolean automaticMode) { this.automaticMode = automaticMode; }

    public HopperType getHopperType() { return type; }
    public double getItemsPerTick() { return itemsPerTick; }
    public double getMaxItemsPerSecond() { return maxItemsPerSecond; }
    public BlockFace getFacing() { return facing; }

    public void addItem(ItemStack item) {
        if (item != null) items.add(item.clone());
    }

    public List<ItemStack> getItems() {
        List<ItemStack> copy = new ArrayList<>();
        for (ItemStack i : items) copy.add(i.clone());
        return copy;
    }

    public void removeItems(List<ItemStack> toRemove) {
        for (ItemStack rem : toRemove) items.removeIf(i -> i.isSimilar(rem));
    }

    public Inventory getInventory() {
        Inventory inv = Bukkit.createInventory(null, 54, Component.text(type.name() + " Hopper"));
        inv.setContents(items.toArray(new ItemStack[0]));
        return inv;
    }

    public void updateFromInventory(Inventory inv) {
        items.clear();
        for (ItemStack i : inv.getContents()) if (i != null) items.add(i.clone());
    }

    public void saveToConfig(PlayerData playerData, String hopperLocation) {
        playerData.setHopperSetting(hopperLocation, "filterActive", this.filterActive);
        playerData.setHopperSetting(hopperLocation, "enabled", this.enabled);
        playerData.setHopperSetting(hopperLocation, "automaticMode", this.automaticMode);
    }

    public static Hopper loadFromConfig(PlayerData playerData, String hopperLocation, HopperType type,
                                        double itemsPerTick, double maxItemsPerSecond, BlockFace facing) {
        boolean filterActive = playerData.getHopperSetting(hopperLocation, "filterActive", true);
        boolean enabled = playerData.getHopperSetting(hopperLocation, "enabled", true);
        boolean automaticMode = playerData.getHopperSetting(hopperLocation, "automaticMode", false);

        Hopper hopper = new Hopper(type, itemsPerTick, maxItemsPerSecond, facing);
        hopper.setFilterActive(filterActive);
        hopper.setEnabled(enabled);
        hopper.setAutomaticMode(automaticMode);
        return hopper;
    }
}
