package org.AH1_1.acaciaHoppers.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PlayerData {

    private final UUID uuid;
    private String ign; // Can be null
    private final List<SuperChest> superChests;
    private final List<Hopper> hoppers;

    // Store settings per hopper location -> setting key -> value
    private final Map<String, Map<String, Boolean>> hopperSettings = new HashMap<>();

    public PlayerData(@NotNull UUID uuid) {
        this.uuid = uuid;
        this.superChests = new ArrayList<>();
        this.hoppers = new ArrayList<>();
    }

    @NotNull
    public UUID getUuid() {
        return uuid;
    }

    @Nullable
    public String getIgn() {
        return ign;
    }

    public void setIgn(@Nullable String ign) {
        this.ign = ign;
    }

    @NotNull
    public List<SuperChest> getSuperChests() {
        return superChests;
    }

    public void addSuperChest(@NotNull String location, @NotNull List<Map<String, Object>> contents) {
        superChests.add(new SuperChest(location, contents));
    }

    @NotNull
    public List<Hopper> getHoppers() {
        return hoppers;
    }

    public void addHopper(@NotNull String location, @NotNull String hopperType,
                          @NotNull List<String> filter, @NotNull List<Map<String, Object>> contents) {
        hoppers.add(new Hopper(location, hopperType, filter, contents));
    }

    // ----------------------------
    // Hopper Settings Methods
    // ----------------------------
    public void setHopperSetting(@NotNull String hopperLocation, @NotNull String key, boolean value) {
        hopperSettings.computeIfAbsent(hopperLocation, k -> new HashMap<>()).put(key, value);
    }

    public boolean getHopperSetting(@NotNull String hopperLocation, @NotNull String key, boolean defaultValue) {
        return hopperSettings
                .getOrDefault(hopperLocation, new HashMap<>())
                .getOrDefault(key, defaultValue);
    }

    @NotNull
    public Map<String, Map<String, Boolean>> getAllHopperSettings() {
        return Collections.unmodifiableMap(hopperSettings);
    }

    // ----------------------------
    // Inner Classes
    // ----------------------------
    public static class SuperChest {
        private final String location;
        private final List<Map<String, Object>> contents;

        public SuperChest(@NotNull String location, @NotNull List<Map<String, Object>> contents) {
            this.location = location;
            this.contents = contents;
        }

        @NotNull
        public String getLocation() {
            return location;
        }

        @NotNull
        public List<Map<String, Object>> getContents() {
            return contents;
        }
    }

    public static class Hopper {
        private final String location;
        private final String hopperType;
        private final List<String> filter;
        private final List<Map<String, Object>> contents;

        public Hopper(@NotNull String location, @NotNull String hopperType,
                      @NotNull List<String> filter, @NotNull List<Map<String, Object>> contents) {
            this.location = location;
            this.hopperType = hopperType;
            this.filter = filter;
            this.contents = contents;
        }

        @NotNull
        public String getLocation() {
            return location;
        }

        @NotNull
        public String getHopperType() {
            return hopperType;
        }

        @NotNull
        public List<String> getFilter() {
            return filter;
        }

        @NotNull
        public List<Map<String, Object>> getContents() {
            return contents;
        }
    }
}
