package org.AH1_1.acaciaHoppers.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SuperChest {

    private final UUID ownerUUID;
    private final String name;
    private final Location location;
    private final List<ItemStack> items;
    private boolean hologramEnabled;

    public SuperChest(UUID ownerUUID, String name, Location location, List<ItemStack> initialItems) {
        this.ownerUUID = ownerUUID;
        this.name = name;
        this.location = location;
        this.items = new ArrayList<>();
        this.hologramEnabled = true;

        if (initialItems != null) {
            for (ItemStack i : initialItems) {
                if (i != null) items.add(i.clone());
            }
        }
    }

    public UUID getOwnerUUID() { return ownerUUID; }
    public String getName() { return name; }
    public Location getLocation() { return location; }
    public boolean isHologramEnabled() { return hologramEnabled; }
    public void setHologramEnabled(boolean hologramEnabled) { this.hologramEnabled = hologramEnabled; }

    @SuppressWarnings("unused")
    public void addItem(ItemStack item) { if (item != null) items.add(item.clone()); }

    public void removeItem(ItemStack item) {
        if (item != null) items.removeIf(i -> i.isSimilar(item));
    }

    public List<ItemStack> getItems() {
        List<ItemStack> copy = new ArrayList<>();
        for (ItemStack i : items) copy.add(i.clone());
        return copy;
    }

    public List<ItemStack> getInventory() { return getItems(); }

    /** Serialize for YAML storage */
    public ConfigurationSection serialize() {
        YamlConfiguration tempConfig = new YamlConfiguration();
        ConfigurationSection section = tempConfig.createSection(ownerUUID.toString());

        section.set("name", name);
        section.set("world", location.getWorld().getName());
        section.set("x", location.getBlockX());
        section.set("y", location.getBlockY());
        section.set("z", location.getBlockZ());
        section.set("items", serializeItems());
        section.set("hologramEnabled", hologramEnabled);

        return section;
    }

    private List<String> serializeItems() {
        List<String> serialized = new ArrayList<>();
        for (ItemStack item : items) {
            if (item != null && item.getType() != Material.AIR) {
                serialized.add(item.getType().name() + ":" + item.getAmount());
            }
        }
        return serialized;
    }

    /** Deserialize from YAML storage */
    public static SuperChest deserialize(ConfigurationSection section) {
        UUID ownerUUID = UUID.fromString(section.getName());
        String name = section.getString("name");
        String worldName = section.getString("world");

        // Fallback in case worldName is null
        Location loc;
        if (worldName != null && Bukkit.getWorld(worldName) != null) {
            loc = new Location(Bukkit.getWorld(worldName), section.getInt("x"), section.getInt("y"), section.getInt("z"));
        } else if (!Bukkit.getWorlds().isEmpty()) {
            loc = new Location(Bukkit.getWorlds().get(0), section.getInt("x"), section.getInt("y"), section.getInt("z"));
        } else {
            loc = new Location(Bukkit.getWorlds().get(0), 0, 0, 0); // Safe fallback
        }

        List<String> serializedItems = section.getStringList("items");
        List<ItemStack> items = new ArrayList<>();
        for (String s : serializedItems) {
            try {
                String[] parts = s.split(":");
                Material mat = Material.valueOf(parts[0]);
                int amount = Integer.parseInt(parts[1]);
                items.add(new ItemStack(mat, amount));
            } catch (Exception ignored) {}
        }

        SuperChest chest = new SuperChest(ownerUUID, name, loc, items);
        chest.setHologramEnabled(section.getBoolean("hologramEnabled", true));
        return chest;
    }
}
