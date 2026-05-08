package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class WarpManager {

    private static final Pattern VALID_NAME_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{1,32}$");

    private final UltimateDonutSmp plugin;
    private Map<String, Location> warps = new LinkedHashMap<>();

    public WarpManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public void loadAll() {
        warps = new LinkedHashMap<>(plugin.getDatabaseManager().loadWarps());
    }

    public Location getWarp(String name) {
        String normalized = normalizeName(name);
        if (normalized == null) {
            return null;
        }

        Location location = warps.get(normalized);
        return location == null ? null : location.clone();
    }

    public boolean warpExists(String name) {
        String normalized = normalizeName(name);
        return normalized != null && warps.containsKey(normalized);
    }

    public boolean createWarp(String name, Location location) {
        String normalized = normalizeName(name);
        if (normalized == null || !isValidWarpName(name) || location == null || location.getWorld() == null) {
            return false;
        }
        if (warps.containsKey(normalized)) {
            return false;
        }

        Location storedLocation = location.clone();
        warps.put(normalized, storedLocation);
        plugin.getDatabaseManager().saveWarp(normalized, storedLocation);
        return true;
    }

    public boolean deleteWarp(String name) {
        String normalized = normalizeName(name);
        if (normalized == null || !warps.containsKey(normalized)) {
            return false;
        }

        warps.remove(normalized);
        plugin.getDatabaseManager().deleteWarp(normalized);
        return true;
    }

    public List<String> getSortedWarpNames() {
        List<String> names = new ArrayList<>(warps.keySet());
        names.sort(String.CASE_INSENSITIVE_ORDER);
        return names;
    }

    public List<String> findWarpSuggestions(String input) {
        String normalized = normalizeName(input);
        if (normalized == null) {
            return Collections.emptyList();
        }

        List<String> startsWith = new ArrayList<>();
        List<String> contains = new ArrayList<>();

        for (String name : getSortedWarpNames()) {
            if (name.startsWith(normalized)) {
                startsWith.add(name);
            } else if (name.contains(normalized)) {
                contains.add(name);
            }
        }

        List<String> suggestions = new ArrayList<>(startsWith);
        for (String name : contains) {
            if (suggestions.size() >= 5) {
                break;
            }
            suggestions.add(name);
        }
        return suggestions;
    }

    public int getWarpCount() {
        return warps.size();
    }

    public boolean isValidWarpName(String name) {
        if (name == null) {
            return false;
        }

        String trimmed = name.trim();
        return VALID_NAME_PATTERN.matcher(trimmed).matches();
    }

    public String normalizeName(String name) {
        if (name == null) {
            return null;
        }

        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        return trimmed.toLowerCase(Locale.ROOT);
    }

    public void setWarp(String name, Location location) {
        String normalized = normalizeName(name);
        if (normalized == null || location == null || location.getWorld() == null) {
            return;
        }

        Location storedLocation = location.clone();
        warps.put(normalized, storedLocation);
        plugin.getDatabaseManager().saveWarp(normalized, storedLocation);
    }

    public Set<String> getWarpNames() {
        return warps.keySet();
    }

    public Collection<Location> getWarpLocations() {
        return warps.values().stream().map(Location::clone).toList();
    }
}
