package org.AH1_1.acaciaHoppers.hoppers;

import org.bukkit.Material;
import java.util.ArrayList;
import java.util.List;

public class HopperFilter {

    private final List<Material> allowedItems;

    public HopperFilter(List<Material> allowedItems) {
        this.allowedItems = new ArrayList<>(allowedItems);
    }

    public List<Material> getAllowedItems() {
        return allowedItems;
    }

    @SuppressWarnings("unused")
    public boolean isItemAllowed(Material mat) {
        return allowedItems.contains(mat);
    }

    @SuppressWarnings("unused")
    public void toggleItem(Material mat) {
        if (allowedItems.contains(mat)) {
            allowedItems.remove(mat);
        } else {
            allowedItems.add(mat);
        }
    }
}
