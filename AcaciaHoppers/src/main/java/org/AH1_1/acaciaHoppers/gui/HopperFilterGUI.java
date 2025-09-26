package org.AH1_1.acaciaHoppers.gui;

import net.kyori.adventure.text.Component;
import org.AH1_1.acaciaHoppers.AcaciaHoppers;
import org.AH1_1.acaciaHoppers.data.PlayerData;
import org.AH1_1.acaciaHoppers.hoppers.HopperFilter;
import org.AH1_1.acaciaHoppers.hoppers.HopperType;
import org.AH1_1.acaciaHoppers.managers.HopperManager;
import org.AH1_1.acaciaHoppers.managers.PlayerDataManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class HopperFilterGUI {

    private final AcaciaHoppers plugin;
    private final HopperManager hopperManager;
    private final PlayerDataManager playerDataManager;
    private final Map<UUID, Integer> playerPages = new HashMap<>();

    public HopperFilterGUI(AcaciaHoppers plugin) {
        this.plugin = plugin;
        this.hopperManager = plugin.getHopperManager();
        this.playerDataManager = plugin.getPlayerDataManager();
    }

    @SuppressWarnings("unused")
    public void openFilterGUI(Player player, HopperType hopperType, String hopperLocation) {
        openFilterGUI(player, hopperType, hopperLocation, 0);
    }

    private void openFilterGUI(Player player, HopperType hopperType, String hopperLocation, int page) {
        HopperFilter filter = hopperManager.getHopperFilter(hopperType);
        if (filter == null) {
            player.sendMessage(plugin.getMessage("hopper.noFilter"));
            return;
        }

        List<Material> allowedItems = filter.getAllowedItems();
        int maxPage = (allowedItems.size() - 1) / 27;
        page = Math.max(0, Math.min(page, maxPage));
        playerPages.put(player.getUniqueId(), page);

        Component guiTitle = Component.text(hopperType.name() + " Filter");
        Inventory gui = Bukkit.createInventory(null, 54, guiTitle);

        fillRow(gui, 0, 8, Material.LIGHT_BLUE_STAINED_GLASS_PANE);

        PlayerData data = playerDataManager.getPlayerData(player);
        int slot = 10;
        int startIndex = page * 27;
        int endIndex = Math.min(startIndex + 27, allowedItems.size());
        for (int i = startIndex; i < endIndex; i++) {
            Material mat = allowedItems.get(i);
            boolean enabled = data.getHopperSetting(hopperLocation, mat.name(), true);
            if (slot == 17 || slot == 26 || slot == 35 || slot == 44) slot++;
            gui.setItem(slot, createItemStack(mat, mat.name(), enabled));
            slot++;
        }

        addBottomRow(gui);
        player.openInventory(gui);
        plugin.getInventoryManager().setInventoryTitle(player.getUniqueId(), hopperType.name() + ":" + hopperLocation);
    }

    private void fillRow(Inventory inv, int start, int end, Material mat) {
        ItemStack pane = new ItemStack(mat);
        for (int i = start; i <= end; i++) inv.setItem(i, pane);
    }

    private ItemStack createItemStack(Material mat, String name, boolean glow) {
        if (mat == null) mat = Material.BARRIER;
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (name != null && !name.isEmpty()) meta.displayName(Component.text(name));
            if (glow) meta.addEnchant(Enchantment.MENDING, 1, true);
            item.setItemMeta(meta);
        }
        return item;
    }

    private void addBottomRow(Inventory inv) {
        inv.setItem(45, createButton("PREVIOUS_PAGE"));
        inv.setItem(49, createButton("CLOSE_GUI"));
        inv.setItem(53, createButton("NEXT_PAGE"));
    }

    private ItemStack createButton(String key) {
        String matName = plugin.getConfig().getString("gui_items." + key);
        Material mat = (matName != null) ? Material.matchMaterial(matName) : Material.BARRIER;
        ItemStack item = new ItemStack(mat != null ? mat : Material.BARRIER);
        String display = plugin.getConfig().getString("gui_labels." + key, "");
        ItemMeta meta = item.getItemMeta();
        if (meta != null && !display.isEmpty()) {
            meta.displayName(Component.text(display));
            item.setItemMeta(meta);
        }
        return item;
    }

    public void handleItemClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Component titleFull = event.getView().title();
        String titleString = titleFull.toString();
        if (!titleString.endsWith(" Filter")) return;

        String[] parts = titleString.split(":");
        String hopperLocation = (parts.length > 1) ? parts[1] : "unknown";

        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        int slot = event.getSlot();
        switch (slot) {
            case 45 -> openFilterGUI(player, HopperType.valueOf(parts[0]), hopperLocation, playerPages.get(player.getUniqueId()) - 1);
            case 53 -> openFilterGUI(player, HopperType.valueOf(parts[0]), hopperLocation, playerPages.get(player.getUniqueId()) + 1);
            case 49 -> {
                player.closeInventory();
                player.sendMessage(plugin.getMessage("hopper.closed"));
            }
            default -> toggleItem(player, clicked, slot, hopperLocation);
        }
    }

    private void toggleItem(Player player, ItemStack clicked, int slot, String hopperLocation) {
        Material mat = clicked.getType();
        PlayerData data = playerDataManager.getPlayerData(player);
        boolean current = data.getHopperSetting(hopperLocation, mat.name(), true);
        data.setHopperSetting(hopperLocation, mat.name(), !current);

        boolean nowEnabled = !current;
        ItemMeta meta = clicked.getItemMeta();
        if (meta != null) {
            if (nowEnabled) meta.addEnchant(Enchantment.MENDING, 1, true);
            else meta.getEnchants().clear();
            clicked.setItemMeta(meta);
        }

        player.playSound(player.getLocation(),
                nowEnabled ? Sound.BLOCK_WOODEN_DOOR_OPEN : Sound.BLOCK_WOODEN_DOOR_CLOSE,
                1.0f, 1.0f);

        player.sendMessage(plugin.getMessage("hopper.filterUpdated", Map.of("item", mat.name())));
        player.getOpenInventory().getTopInventory().setItem(slot, clicked);
        playerDataManager.savePlayerData(player);
    }
}
