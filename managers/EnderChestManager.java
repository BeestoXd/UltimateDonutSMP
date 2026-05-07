package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.enderchest.EnderChestHolder;
import com.bx.ultimateDonutSmp.enderchest.EnderChestSession;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class EnderChestManager {

    private static final int MIN_ROWS = 1;
    private static final int MAX_ROWS = 6;

    private final UltimateDonutSmp plugin;
    private final Map<UUID, EnderChestSession> activeSessions = new HashMap<>();
    private ScheduledTask autoSaveTask;

    public EnderChestManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        restartAutoSaveTask();
    }

    public void shutdown() {
        saveAllOpenSessions();
        cancelAutoSaveTask();
    }

    public boolean isEnabled() {
        return getConfig().getBoolean(
                "ENDER-CHEST.ENABLED",
                plugin.getConfigManager().getConfig().getBoolean("ENDER-CHEST.SIX-ROW", false)
        );
    }

    public boolean shouldInterceptVanillaOpen() {
        return isEnabled() && getConfig().getBoolean("ENDER-CHEST.INTERCEPT-VANILLA-OPEN", true);
    }

    public boolean isCommandAllowed() {
        return isEnabled()
                && plugin.getConfigManager().isCommandEnabled("ENDERCHEST")
                && getConfig().getBoolean("ENDER-CHEST.ALLOW-COMMAND", true);
    }

    public boolean commandRequiresPermission() {
        return getConfig().getBoolean("ENDER-CHEST.COMMAND-REQUIRES-PERMISSION", false);
    }

    public String getCommandPermission() {
        return getConfig().getString("ENDER-CHEST.PERMISSION", "ultimatedonutsmp.enderchest");
    }

    public String getMessage(String path, String fallback) {
        return getConfig().getString("MESSAGES." + path, fallback);
    }

    public boolean isCustomEnderChest(Inventory inventory) {
        return inventory != null && inventory.getHolder() instanceof EnderChestHolder;
    }

    public boolean isCustomEnderChestView(InventoryView view) {
        return view != null && isCustomEnderChest(view.getTopInventory());
    }

    public void open(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        UUID uuid = player.getUniqueId();
        EnderChestSession existingSession = activeSessions.get(uuid);
        if (existingSession != null) {
            if (player.getOpenInventory().getTopInventory().equals(existingSession.getInventory())) {
                return;
            }
            player.openInventory(existingSession.getInventory());
            return;
        }

        try {
            int rows = clampRows(plugin.getDatabaseManager().loadEnderChestRows(uuid, getDefaultRows()));
            EnderChestHolder holder = new EnderChestHolder(uuid, rows);
            Inventory inventory = Bukkit.createInventory(
                    holder,
                    rows * 9,
                    ColorUtils.toComponent(getTitle(), player)
            );
            holder.bind(inventory);
            inventory.setContents(plugin.getDatabaseManager().loadEnderChestContents(uuid, inventory.getSize()));

            EnderChestSession session = new EnderChestSession(uuid, inventory, rows);
            activeSessions.put(uuid, session);
            player.openInventory(inventory);
        } catch (Exception exception) {
            plugin.getLogger().log(
                    Level.SEVERE,
                    "Failed to open custom Ender Chest for " + player.getUniqueId(),
                    exception
            );
            player.sendMessage(ColorUtils.toComponent(
                    getMessage("OPEN-FAILED", "&cFailed to open your Ender Chest. Please try again.")
            ));
        }
    }

    public void markDirty(InventoryView view) {
        if (!isCustomEnderChestView(view)) {
            return;
        }

        Inventory inventory = view.getTopInventory();
        EnderChestHolder holder = (EnderChestHolder) inventory.getHolder();
        EnderChestSession session = activeSessions.get(holder.getOwnerUuid());
        if (session == null || session.getInventory() != inventory) {
            return;
        }

        session.markDirty();
    }

    public void handleClose(Player player, Inventory inventory) {
        if (!isCustomEnderChest(inventory)) {
            return;
        }

        EnderChestHolder holder = (EnderChestHolder) inventory.getHolder();
        EnderChestSession session = activeSessions.get(holder.getOwnerUuid());
        if (session == null || session.getInventory() != inventory) {
            return;
        }

        if (saveSession(session)) {
            activeSessions.remove(holder.getOwnerUuid());
            return;
        }

        if (player != null && player.isOnline()) {
            player.sendMessage(ColorUtils.toComponent(
                    getMessage("SAVE-FAILED", "&cFailed to save your Ender Chest. Contact staff.")
            ));
        }
    }

    public void handleQuit(Player player) {
        if (player == null) {
            return;
        }

        UUID uuid = player.getUniqueId();
        EnderChestSession session = activeSessions.get(uuid);
        if (session == null) {
            return;
        }

        if (saveSession(session)) {
            activeSessions.remove(uuid);
        } else {
            plugin.getLogger().warning("Failed to flush Ender Chest session for quitting player " + uuid + ".");
        }
    }

    public void saveAllOpenSessions() {
        for (EnderChestSession session : List.copyOf(activeSessions.values())) {
            if (saveSession(session)) {
                activeSessions.remove(session.getOwnerUuid());
            }
        }
    }

    private boolean saveSession(EnderChestSession session) {
        ItemStack[] sanitizedContents = sanitizeContents(session.getInventory().getContents());
        boolean saved = plugin.getDatabaseManager().saveEnderChest(
                session.getOwnerUuid(),
                session.getRows(),
                sanitizedContents
        );
        if (saved) {
            session.markSaved();
        }
        return saved;
    }

    private ItemStack[] sanitizeContents(ItemStack[] contents) {
        ItemStack[] sanitized = new ItemStack[contents.length];
        for (int slot = 0; slot < contents.length; slot++) {
            ItemStack item = contents[slot];
            if (item == null || item.getType().isAir()) {
                sanitized[slot] = null;
                continue;
            }

            sanitized[slot] = plugin.getWorthManager().stripWorthDisplay(item);
        }
        return sanitized;
    }

    private void restartAutoSaveTask() {
        cancelAutoSaveTask();

        long periodTicks = getConfig().getLong("ENDER-CHEST.AUTO-SAVE-TICKS", 1200L);
        if (periodTicks <= 0L) {
            return;
        }

        autoSaveTask = plugin.getPaperScheduler().runGlobalTimer(
                () -> {
                    for (EnderChestSession session : List.copyOf(activeSessions.values())) {
                        if (!session.isDirty()) {
                            continue;
                        }

                        Player player = plugin.getServer().getPlayer(session.getOwnerUuid());
                        if (player != null && player.isOnline()) {
                            plugin.getPaperScheduler().runEntity(player, () -> autoSaveSession(session));
                        } else {
                            autoSaveSession(session);
                        }
                    }
                },
                periodTicks,
                periodTicks
        );
    }

    private void autoSaveSession(EnderChestSession session) {
        if (!saveSession(session)) {
            plugin.getLogger().warning(
                    "Auto-save failed for Ender Chest session " + session.getOwnerUuid() + "."
            );
        }
    }

    private void cancelAutoSaveTask() {
        if (autoSaveTask != null) {
            autoSaveTask.cancel();
            autoSaveTask = null;
        }
    }

    private int getDefaultRows() {
        return clampRows(getConfig().getInt("ENDER-CHEST.DEFAULT-ROWS", 6));
    }

    private String getTitle() {
        return getConfig().getString("ENDER-CHEST.TITLE", "&5Ender Chest");
    }

    private int clampRows(int rows) {
        return Math.max(MIN_ROWS, Math.min(MAX_ROWS, rows));
    }

    private FileConfiguration getConfig() {
        return plugin.getConfigManager().getEnderChest();
    }
}
