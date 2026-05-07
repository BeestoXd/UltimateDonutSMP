package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.menus.HomeMenu;
import com.bx.ultimateDonutSmp.models.Home;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

public class HomeManager {

    private static final int HOMES_PER_PAGE = 5;
    private static final int MAX_PERMISSION_VALUE = 100;

    private final UltimateDonutSmp plugin;
    /** UUID → list of homes */
    private final Map<UUID, List<Home>> cache = new HashMap<>();
    private final Map<UUID, PendingHomeInput> pendingInputs = new HashMap<>();

    public HomeManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public void loadHomes(Player player) {
        List<Home> homes = plugin.getDatabaseManager().loadHomes(player.getUniqueId());
        cache.put(player.getUniqueId(), homes);
    }

    public void unloadHomes(UUID uuid) {
        cache.remove(uuid);
    }

    public void clearAllCaches() {
        cache.clear();
        pendingInputs.clear();
    }

    public List<Home> getHomes(UUID uuid) {
        return cache.getOrDefault(uuid, Collections.emptyList());
    }

    public Home getHome(UUID uuid, String name) {
        for (Home h : getHomes(uuid)) {
            if (h.getName().equalsIgnoreCase(name)) return h;
        }
        return null;
    }

    public int getHomeCount(UUID uuid) {
        return getHomes(uuid).size();
    }

    public int getMaxHomes(Player player) {
        int defaultHomes = plugin.getConfigManager().getConfig().getInt("SETTINGS.HOME-DEFAULT", 2);
        int homesByAmountPermission = resolveHighestPermissionValue(player, "ultimatedonutsmp.homes.");
        int pagesByPermission = resolveHighestPermissionValue(player, "ultimatedonutsmp.homes.page.");
        int homesByPagePermission = pagesByPermission * HOMES_PER_PAGE;

        return Math.max(defaultHomes, Math.max(homesByAmountPermission, homesByPagePermission));
    }

    public int getMaxHomePages(Player player) {
        int maxHomes = getMaxHomes(player);
        int pagesByHomes = Math.max(1, (int) Math.ceil(maxHomes / (double) HOMES_PER_PAGE));
        int pagesByPermission = resolveHighestPermissionValue(player, "ultimatedonutsmp.homes.page.");

        return Math.max(1, Math.max(pagesByHomes, pagesByPermission));
    }

    public boolean canSetHome(Player player) {
        return getHomeCount(player.getUniqueId()) < getMaxHomes(player);
    }

    public boolean setHome(Player player, String name) {
        return setHome(player.getUniqueId(), name, player.getLocation());
    }

    public boolean setHome(UUID uuid, String name, Location location) {
        List<Home> homes = cache.computeIfAbsent(uuid, k -> new ArrayList<>());
        // Update existing
        for (Home h : homes) {
            if (h.getName().equalsIgnoreCase(name)) {
                h.setLocation(location.clone());
                plugin.getDatabaseManager().saveHome(h);
                return true;
            }
        }
        // Create new
        Player player = plugin.getServer().getPlayer(uuid);
        if (player != null && homes.size() >= getMaxHomes(player)) return false;
        Home home = new Home(uuid, name, location.clone());
        homes.add(home);
        plugin.getDatabaseManager().saveHome(home);
        return true;
    }

    public boolean deleteHome(UUID uuid, String name) {
        List<Home> homes = cache.get(uuid);
        if (homes == null) return false;
        boolean removed = homes.removeIf(h -> h.getName().equalsIgnoreCase(name));
        if (removed) plugin.getDatabaseManager().deleteHome(uuid, name);
        return removed;
    }

    public boolean renameHome(UUID uuid, String oldName, String newName) {
        // Check newName not already taken
        if (getHome(uuid, newName) != null) return false;
        Home home = getHome(uuid, oldName);
        if (home == null) return false;
        plugin.getDatabaseManager().deleteHome(uuid, oldName);
        home.setName(newName);
        plugin.getDatabaseManager().saveHome(home);
        return true;
    }

    public void promptCreateHome(Player player, Location location, String suggestedName) {
        pendingInputs.put(player.getUniqueId(), PendingHomeInput.create(location, suggestedName));
        player.closeInventory();
        player.sendMessage(ColorUtils.toComponent(
                plugin.getConfigManager().getMessage("HOME.NAME-PROMPT", "{name}", suggestedName)));
    }

    public void promptRenameHome(Player player, String oldName) {
        pendingInputs.put(player.getUniqueId(), PendingHomeInput.rename(oldName));
        player.closeInventory();
        player.sendMessage(ColorUtils.toComponent(
                plugin.getConfigManager().getMessage("HOME.RENAME-PROMPT", "{name}", oldName)));
    }

    public boolean hasPendingInput(UUID uuid) {
        return pendingInputs.containsKey(uuid);
    }

    public void handlePendingInput(Player player, String rawInput) {
        PendingHomeInput pending = pendingInputs.get(player.getUniqueId());
        if (pending == null) return;

        String input = rawInput == null ? "" : rawInput.trim();
        if (input.equalsIgnoreCase("cancel")) {
            pendingInputs.remove(player.getUniqueId());
            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage("HOME.CANCELLED")));
            return;
        }

        if (!isValidHomeName(input)) {
            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage("HOME.INVALID-NAME")));
            resendPrompt(player, pending);
            return;
        }

        if (pending.type == PendingHomeInput.Type.CREATE) {
            if (getHome(player.getUniqueId(), input) != null) {
                player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage("HOME.ALREADY-EXISTS")));
                resendPrompt(player, pending);
                return;
            }
            if (!setHome(player.getUniqueId(), input, pending.location)) {
                pendingInputs.remove(player.getUniqueId());
                player.sendMessage(ColorUtils.toComponent("&cYou cannot create another home right now."));
                return;
            }

            pendingInputs.remove(player.getUniqueId());
            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage("HOME.SET")));
            new HomeMenu(plugin).open(player);
            return;
        }

        if (!renameHome(player.getUniqueId(), pending.oldName, input)) {
            player.sendMessage(ColorUtils.toComponent("&cFailed to rename home. Try another name."));
            resendPrompt(player, pending);
            return;
        }

        pendingInputs.remove(player.getUniqueId());
        player.sendMessage(ColorUtils.toComponent(
                plugin.getConfigManager().getMessage("HOME.RENAME-SUCCESS", "{name}", input)));
        new HomeMenu(plugin).open(player);
    }

    private int resolveHighestPermissionValue(Player player, String prefix) {
        for (int i = MAX_PERMISSION_VALUE; i >= 1; i--) {
            if (player.hasPermission(prefix + i)) return i;
        }
        return 0;
    }

    private boolean isValidHomeName(String input) {
        return !input.isBlank() && !input.contains(" ");
    }

    private void resendPrompt(Player player, PendingHomeInput pending) {
        if (pending.type == PendingHomeInput.Type.CREATE) {
            player.sendMessage(ColorUtils.toComponent(
                    plugin.getConfigManager().getMessage("HOME.NAME-PROMPT", "{name}", pending.suggestedName)));
            return;
        }

        player.sendMessage(ColorUtils.toComponent(
                plugin.getConfigManager().getMessage("HOME.RENAME-PROMPT", "{name}", pending.oldName)));
    }

    private static final class PendingHomeInput {
        private final Type type;
        private final String oldName;
        private final String suggestedName;
        private final Location location;

        private PendingHomeInput(Type type, String oldName, String suggestedName, Location location) {
            this.type = type;
            this.oldName = oldName;
            this.suggestedName = suggestedName;
            this.location = location == null ? null : location.clone();
        }

        private static PendingHomeInput create(Location location, String suggestedName) {
            return new PendingHomeInput(Type.CREATE, null, suggestedName, location);
        }

        private static PendingHomeInput rename(String oldName) {
            return new PendingHomeInput(Type.RENAME, oldName, null, null);
        }

        private enum Type {
            CREATE,
            RENAME
        }
    }
}
