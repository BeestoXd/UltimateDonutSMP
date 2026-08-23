package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.enderchest.EnderChestHolder;
import com.bx.ultimateDonutSmp.enderchest.EnderChestInspectionHolder;
import com.bx.ultimateDonutSmp.enderchest.EnderChestInspectionSession;
import com.bx.ultimateDonutSmp.enderchest.EnderChestSession;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.PermissionUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Lidded;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class EnderChestManager {

    private static final int MIN_ROWS = 1;
    private static final int MAX_ROWS = 6;
    private static final String ROWS_PERMISSION_PREFIX = "ultimatedonutsmp.enderchest.rows.";
    // Scanned well past MAX_ROWS so a node like rows.9 clamps to the biggest chest instead of
    // silently falling through to DEFAULT-ROWS.
    private static final int MAX_ROWS_PERMISSION_VALUE = 54;
    private static final String RETURN_ITEMS_MODE = "RETURN-ITEMS";

    private final UltimateDonutSmp plugin;
    private final Map<UUID, EnderChestSession> activeSessions = new HashMap<>();
    private final Map<UUID, EnderChestInspectionSession> inspectionSessionsByViewer = new ConcurrentHashMap<>();
    private final Map<UUID, Set<UUID>> inspectionViewersByTarget = new ConcurrentHashMap<>();
    private final Map<UUID, EnderChestBlockKey> activeVisualViewers = new HashMap<>();
    private final Map<EnderChestBlockKey, Integer> visualViewerCounts = new HashMap<>();
    private BukkitTask autoSaveTask;
    private BukkitTask inspectionRefreshTask;

    public EnderChestManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        closeAllInspections(true);
        cancelInspectionRefreshTask();
        if (!isEnabled()) {
            cancelAutoSaveTask();
            closeAllVisuals();
            return;
        }
        if (!areVanillaEffectsEnabled()) {
            closeAllVisuals();
        }
        restartAutoSaveTask();
        if (isInspectionEnabled()) {
            restartInspectionRefreshTask();
        } else {
            cancelInspectionRefreshTask();
        }
    }

    public void shutdown() {
        closeAllInspections(true);
        cancelInspectionRefreshTask();
        saveAllOpenSessions();
        closeAllVisuals();
        cancelAutoSaveTask();
    }

    public boolean isEnabled() {
        return plugin.getFeatureManager().isEnabled(FeatureManager.Feature.ENDER_CHEST)
                && getConfig().getBoolean(
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
        return getConfig().getString("ENDER-CHEST.PERMISSION", "ULTIMATEDONUTSMP.ENDERCHEST");
    }

    public boolean isInspectionEnabled() {
        return isEnabled() && getConfig().getBoolean("ENDER-CHEST.ECSEE.ENABLED", true);
    }

    public String getInspectionPermission() {
        return getConfig().getString("ENDER-CHEST.ECSEE.PERMISSION", "ULTIMATEDONUTSMP.ADMIN.ECSEE");
    }

    public boolean canInspect(CommandSender sender) {
        return sender != null && PermissionUtils.has(sender, getInspectionPermission());
    }

    public boolean isEcseeEditable(Player viewer) {
        return isInspectionEnabled()
                && getConfig().getBoolean("ENDER-CHEST.ECSEE.EDITABLE", false)
                && viewer != null
                && PermissionUtils.has(viewer, "ultimatedonutsmp.admin.ecsee.edit");
    }

    public List<String> getInspectionTargetSuggestions() {
        Set<String> names = new LinkedHashSet<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            names.add(player.getName());
        }
        names.addAll(plugin.getDatabaseManager().loadKnownPlayerNames());
        return names.stream().sorted(String.CASE_INSENSITIVE_ORDER).toList();
    }

    public String getMessage(String path, String fallback) {
        return getConfig().getString("MESSAGES." + path, fallback);
    }

    public String formatMessage(String path, String fallback, String... placeholders) {
        String message = getMessage(path, fallback);
        for (int index = 0; index + 1 < placeholders.length; index += 2) {
            message = message.replace(placeholders[index], placeholders[index + 1]);
        }
        return message;
    }

    public boolean isCustomEnderChest(Inventory inventory) {
        return inventory != null && inventory.getHolder() instanceof EnderChestHolder;
    }

    public boolean isCustomEnderChestView(InventoryView view) {
        return view != null && isCustomEnderChest(view.getTopInventory());
    }

    public boolean isInspectionInventory(Inventory inventory) {
        return inventory != null && inventory.getHolder() instanceof EnderChestInspectionHolder;
    }

    public boolean isInspectionView(InventoryView view) {
        return view != null && isInspectionInventory(view.getTopInventory());
    }

    public void open(Player player) {
        open(player, null);
    }

    public void open(Player player, Location sourceLocation) {
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
            registerVisualOpenIfViewing(player, existingSession.getInventory(), sourceLocation);
            return;
        }

        try {
            int dbRows = plugin.getDatabaseManager().loadEnderChestRows(uuid, -1);
            if (dbRows == -1) {
                throw new java.sql.SQLException("Database load error when reading Ender Chest rows");
            }
            int rows = resolveOpenRows(player, dbRows);
            EnderChestHolder holder = new EnderChestHolder(uuid, rows);
            Inventory inventory = Bukkit.createInventory(
                    holder,
                    rows * 9,
                    ColorUtils.toComponent(getTitle(), player)
            );
            holder.bind(inventory);
            ItemStack[] rawContents = plugin.getDatabaseManager().loadEnderChestContents(uuid, inventory.getSize());
            if (rawContents == null) {
                throw new java.sql.SQLException("Database load error when reading Ender Chest contents");
            }
            inventory.setContents(sanitizeLoadedContents(uuid, rawContents));

            EnderChestSession session = new EnderChestSession(uuid, inventory, rows);
            activeSessions.put(uuid, session);
            player.openInventory(inventory);
            registerVisualOpenIfViewing(player, inventory, sourceLocation);
        } catch (Exception exception) {
            plugin.getLogger().log(
                    Level.SEVERE,
                    "failed to open custom ender chest for " + player.getUniqueId(),
                    exception
            );
            player.sendMessage(ColorUtils.toComponent(
                    getMessage("OPEN-FAILED", "&cFailed to open your ender chest. Please try again.")
            ));
        }
    }

    public void openInspection(Player viewer, UUID targetUuid, String targetName) {
        if (viewer == null || !viewer.isOnline() || targetUuid == null) {
            return;
        }

        removeInspectionSession(inspectionSessionsByViewer.get(viewer.getUniqueId()));
        if (viewer.getUniqueId().equals(targetUuid)
                && isCustomEnderChestView(viewer.getOpenInventory())) {
            viewer.closeInventory();
        }

        try {
            EnderChestSession activeTargetSession = activeSessions.get(targetUuid);
            int dbRows = activeTargetSession == null
                    ? plugin.getDatabaseManager().loadEnderChestRows(targetUuid, -1)
                    : activeTargetSession.getRows();
            if (dbRows == -1) {
                throw new java.sql.SQLException("Database load error when reading Ender Chest rows for inspection");
            }
            int defaultRows = getDefaultRows();
            int rows = clampRows(dbRows <= 0 ? defaultRows : Math.max(dbRows, defaultRows));
            EnderChestInspectionHolder holder = new EnderChestInspectionHolder(
                    viewer.getUniqueId(),
                    targetUuid
            );
            Inventory inventory = Bukkit.createInventory(
                    holder,
                    rows * 9,
                    ColorUtils.toComponent(getInspectionTitle(targetName), viewer)
            );
            holder.bind(inventory);

            ItemStack[] rawContents = null;
            if (activeTargetSession == null) {
                rawContents = plugin.getDatabaseManager().loadEnderChestContents(targetUuid, inventory.getSize());
                if (rawContents == null) {
                    throw new java.sql.SQLException("Database load error when reading Ender Chest contents for inspection");
                }
            }

            ItemStack[] sourceContents = activeTargetSession == null
                    ? sanitizeLoadedContents(targetUuid, rawContents)
                    : activeTargetSession.getInventory().getContents();
            inventory.setContents(copyInspectionContents(sourceContents, inventory.getSize()));

            EnderChestInspectionSession inspectionSession = new EnderChestInspectionSession(
                    viewer.getUniqueId(),
                    targetUuid,
                    targetName,
                    inventory
            );
            inspectionSessionsByViewer.put(viewer.getUniqueId(), inspectionSession);
            inspectionViewersByTarget
                    .computeIfAbsent(targetUuid, ignored -> ConcurrentHashMap.newKeySet())
                    .add(viewer.getUniqueId());
            viewer.openInventory(inventory);

            if (getConfig().getBoolean("ENDER-CHEST.ECSEE.LOG-USAGE", true)) {
                plugin.getLogger().info(
                        "ecsee opened: viewer=" + viewer.getName()
                                + " target=" + targetName
                                + " targetuuid=" + targetUuid
                );
            }
        } catch (Exception exception) {
            removeInspectionSession(inspectionSessionsByViewer.get(viewer.getUniqueId()));
            plugin.getLogger().log(
                    Level.SEVERE,
                    "failed to inspect ender chest for " + targetUuid,
                    exception
            );
            viewer.sendMessage(ColorUtils.toComponent(formatMessage(
                    "ECSEE-OPEN-FAILED",
                    "&cFailed to open {target}'s ender chest. Please try again.",
                    "{player}", targetName,
                    "{target}", targetName
            )));
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

        if (player != null) {
            releaseVisualViewer(player.getUniqueId());
        }

        EnderChestHolder holder = (EnderChestHolder) inventory.getHolder();
        EnderChestSession session = activeSessions.get(holder.getOwnerUuid());
        if (session == null || session.getInventory() != inventory) {
            return;
        }

        syncInspectionsForTarget(holder.getOwnerUuid());
        if (saveSession(session)) {
            activeSessions.remove(holder.getOwnerUuid());
            return;
        }

        if (player != null && player.isOnline()) {
            player.sendMessage(ColorUtils.toComponent(
                    getMessage("SAVE-FAILED", "&cFailed to save your ender chest. Contact staff.")
            ));
        }
    }

    public void handleQuit(Player player) {
        if (player == null) {
            return;
        }

        UUID uuid = player.getUniqueId();
        releaseVisualViewer(uuid);
        EnderChestSession session = activeSessions.get(uuid);
        if (session == null) {
            return;
        }

        syncInspectionsForTarget(uuid);
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

    public boolean flushAndDiscardForServerWipe() {
        closeAllInspections(true);
        cancelInspectionRefreshTask();
        for (EnderChestSession session : List.copyOf(activeSessions.values())) {
            if (!saveSession(session)) {
                return false;
            }
        }

        cancelAutoSaveTask();
        activeSessions.clear();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isCustomEnderChestView(player.getOpenInventory())) {
                player.closeInventory();
            }
        }
        closeAllVisuals();
        return true;
    }

    /**
     * Drops one player's Ender Chest state without flushing it, so a wipe of their stored items is
     * not undone by an open session saving itself back afterwards.
     */
    public void discardForPlayerWipe(UUID ownerUuid) {
        if (ownerUuid == null) {
            return;
        }

        closeInspectionsForTarget(ownerUuid);
        releaseVisualViewer(ownerUuid);
        activeSessions.remove(ownerUuid);

        Player owner = Bukkit.getPlayer(ownerUuid);
        if (owner != null && owner.isOnline() && isCustomEnderChestView(owner.getOpenInventory())) {
            owner.closeInventory();
        }
    }

    private void closeInspectionsForTarget(UUID targetUuid) {
        Set<UUID> viewerUuids = inspectionViewersByTarget.get(targetUuid);
        if (viewerUuids == null || viewerUuids.isEmpty()) {
            return;
        }

        for (UUID viewerUuid : List.copyOf(viewerUuids)) {
            EnderChestInspectionSession session = inspectionSessionsByViewer.get(viewerUuid);
            if (session == null) {
                continue;
            }

            removeInspectionSession(session);
            Player viewer = Bukkit.getPlayer(viewerUuid);
            if (viewer != null && viewer.isOnline()
                    && viewer.getOpenInventory().getTopInventory() == session.getInventory()) {
                viewer.closeInventory();
            }
        }
    }

    public void discardAllForServerWipe() {
        closeAllInspections(true);
        cancelInspectionRefreshTask();
        cancelAutoSaveTask();
        activeSessions.clear();
        closeAllVisuals();
    }

    public void handleInspectionClose(Player viewer, Inventory inventory) {
        if (!isInspectionInventory(inventory)) {
            return;
        }

        EnderChestInspectionHolder holder = (EnderChestInspectionHolder) inventory.getHolder();
        EnderChestInspectionSession session = inspectionSessionsByViewer.get(holder.getViewerUuid());
        if (session == null || session.getInventory() != inventory) {
            return;
        }
        if (viewer != null && isEcseeEditable(viewer)) {
            saveInspectionSession(session);
        }
        removeInspectionSession(session);
    }

    public void syncInspectionBackToTarget(Inventory inspectionInventory) {
        if (inspectionInventory == null || !(inspectionInventory.getHolder() instanceof EnderChestInspectionHolder holder)) {
            return;
        }

        UUID targetUuid = holder.getTargetUuid();
        EnderChestSession targetSession = activeSessions.get(targetUuid);
        ItemStack[] currentContents = inspectionInventory.getContents();

        if (targetSession != null) {
            targetSession.getInventory().setContents(copyInspectionContents(currentContents, targetSession.getInventory().getSize()));
            targetSession.markDirty();
        }
    }

    private void saveInspectionSession(EnderChestInspectionSession session) {
        UUID targetUuid = session.getTargetUuid();
        EnderChestSession activeTargetSession = activeSessions.get(targetUuid);

        if (activeTargetSession != null) {
            activeTargetSession.getInventory().setContents(copyInspectionContents(
                    session.getInventory().getContents(),
                    activeTargetSession.getInventory().getSize()
            ));
            saveSession(activeTargetSession);
        } else {
            ItemStack[] sanitizedContents = sanitizeContents(session.getInventory().getContents());
            int rows = clampRows(session.getInventory().getSize() / 9);
            plugin.getDatabaseManager().saveEnderChest(targetUuid, rows, sanitizedContents);
        }
    }

    public void handleInspectionViewerQuit(Player viewer) {
        if (viewer != null) {
            removeInspectionSession(inspectionSessionsByViewer.get(viewer.getUniqueId()));
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
            CrashProtectionManager.ValidationResult safetyResult = plugin.getCrashProtectionManager()
                    .validateForStorage(sanitized[slot], CrashProtectionManager.Context.ENDER_CHEST);
            if (!safetyResult.allowed()) {
                plugin.getCrashProtectionManager().logBlockedItem(
                        "ender chest save " + slot,
                        sanitized[slot],
                        CrashProtectionManager.Context.ENDER_CHEST,
                        safetyResult
                );
                sanitized[slot] = null;
            }
        }
        return sanitized;
    }

    private ItemStack[] sanitizeLoadedContents(UUID uuid, ItemStack[] contents) {
        ItemStack[] sanitized = new ItemStack[contents.length];
        for (int slot = 0; slot < contents.length; slot++) {
            ItemStack item = contents[slot];
            if (item == null || item.getType().isAir()) {
                sanitized[slot] = null;
                continue;
            }

            CrashProtectionManager.ValidationResult safetyResult = plugin.getCrashProtectionManager()
                    .validateForStorage(item, CrashProtectionManager.Context.DATABASE_LOAD);
            if (!safetyResult.allowed()) {
                plugin.getCrashProtectionManager().logBlockedItem(
                        "ender chest load " + uuid + " slot " + slot,
                        item,
                        CrashProtectionManager.Context.DATABASE_LOAD,
                        safetyResult
                );
                sanitized[slot] = null;
                continue;
            }

            sanitized[slot] = item;
        }
        return sanitized;
    }

    private void restartAutoSaveTask() {
        cancelAutoSaveTask();

        long periodTicks = getConfig().getLong("ENDER-CHEST.AUTO-SAVE-TICKS", 1200L);
        if (periodTicks <= 0L) {
            return;
        }

        autoSaveTask = plugin.getSpigotScheduler().runGlobalTimer(
                () -> {
                    for (EnderChestSession session : List.copyOf(activeSessions.values())) {
                        if (!session.isDirty()) {
                            continue;
                        }

                        Player player = plugin.getServer().getPlayer(session.getOwnerUuid());
                        if (player != null && player.isOnline()) {
                            plugin.getSpigotScheduler().runEntity(player, () -> autoSaveSession(session));
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
                    "auto-save failed for ender chest session " + session.getOwnerUuid() + "."
            );
        }
    }

    private void cancelAutoSaveTask() {
        if (autoSaveTask != null) {
            autoSaveTask.cancel();
            autoSaveTask = null;
        }
    }

    private void restartInspectionRefreshTask() {
        cancelInspectionRefreshTask();

        long periodTicks = getConfig().getLong("ENDER-CHEST.ECSEE.AUTO-REFRESH-TICKS", 10L);
        if (periodTicks <= 0L) {
            return;
        }

        inspectionRefreshTask = plugin.getSpigotScheduler().runGlobalTimer(
                () -> {
                    for (UUID targetUuid : List.copyOf(inspectionViewersByTarget.keySet())) {
                        Player target = Bukkit.getPlayer(targetUuid);
                        if (target == null || !target.isOnline()) {
                            continue;
                        }
                        plugin.getSpigotScheduler().runEntity(
                                target,
                                () -> syncInspectionsForTarget(targetUuid)
                        );
                    }
                },
                periodTicks,
                periodTicks
        );
    }

    private void cancelInspectionRefreshTask() {
        if (inspectionRefreshTask != null) {
            inspectionRefreshTask.cancel();
            inspectionRefreshTask = null;
        }
    }

    private void syncInspectionsForTarget(UUID targetUuid) {
        EnderChestSession targetSession = activeSessions.get(targetUuid);
        if (targetSession == null) {
            return;
        }

        Set<UUID> viewerUuids = inspectionViewersByTarget.get(targetUuid);
        if (viewerUuids == null || viewerUuids.isEmpty()) {
            return;
        }

        ItemStack[] snapshot = copyInspectionContents(
                targetSession.getInventory().getContents(),
                targetSession.getInventory().getSize()
        );
        for (UUID viewerUuid : List.copyOf(viewerUuids)) {
            Player viewer = Bukkit.getPlayer(viewerUuid);
            if (viewer == null || !viewer.isOnline()) {
                removeInspectionSession(inspectionSessionsByViewer.get(viewerUuid));
                continue;
            }
            plugin.getSpigotScheduler().runEntity(
                    viewer,
                    () -> applyInspectionSnapshot(viewerUuid, snapshot)
            );
        }
    }

    private void applyInspectionSnapshot(UUID viewerUuid, ItemStack[] snapshot) {
        EnderChestInspectionSession session = inspectionSessionsByViewer.get(viewerUuid);
        if (session == null) {
            return;
        }

        Player viewer = Bukkit.getPlayer(viewerUuid);
        if (viewer == null || !viewer.isOnline()
                || viewer.getOpenInventory().getTopInventory() != session.getInventory()) {
            removeInspectionSession(session);
            return;
        }

        session.getInventory().setContents(copyInspectionContents(
                snapshot,
                session.getInventory().getSize()
        ));
        session.markSynced();
    }

    private ItemStack[] copyInspectionContents(ItemStack[] source, int size) {
        ItemStack[] copy = new ItemStack[Math.max(9, size)];
        if (source == null) {
            return copy;
        }

        int limit = Math.min(copy.length, source.length);
        for (int slot = 0; slot < limit; slot++) {
            ItemStack item = source[slot];
            if (item == null || item.getType().isAir()) {
                continue;
            }
            ItemStack sanitized = plugin.getWorthManager().stripWorthDisplay(item);
            copy[slot] = sanitized == null ? null : sanitized.clone();
        }
        return copy;
    }

    private void removeInspectionSession(EnderChestInspectionSession session) {
        if (session == null) {
            return;
        }

        inspectionSessionsByViewer.remove(session.getViewerUuid(), session);
        Set<UUID> viewerUuids = inspectionViewersByTarget.get(session.getTargetUuid());
        if (viewerUuids != null) {
            viewerUuids.remove(session.getViewerUuid());
            if (viewerUuids.isEmpty()) {
                inspectionViewersByTarget.remove(session.getTargetUuid());
            }
        }
    }

    private void closeAllInspections(boolean closeInventories) {
        for (EnderChestInspectionSession session : List.copyOf(inspectionSessionsByViewer.values())) {
            removeInspectionSession(session);
            if (!closeInventories) {
                continue;
            }

            Player viewer = Bukkit.getPlayer(session.getViewerUuid());
            if (viewer != null && viewer.isOnline()
                    && viewer.getOpenInventory().getTopInventory() == session.getInventory()) {
                viewer.closeInventory();
            }
        }
        inspectionSessionsByViewer.clear();
        inspectionViewersByTarget.clear();
    }

    private void registerVisualOpenIfViewing(Player player, Inventory inventory, Location sourceLocation) {
        if (sourceLocation == null || !areVanillaEffectsEnabled()) {
            return;
        }

        if (!player.getOpenInventory().getTopInventory().equals(inventory)) {
            return;
        }

        registerVisualViewer(player.getUniqueId(), sourceLocation);
    }

    private synchronized void registerVisualViewer(UUID playerUuid, Location sourceLocation) {
        if (sourceLocation.getWorld() == null) {
            return;
        }

        EnderChestBlockKey key = EnderChestBlockKey.from(sourceLocation);
        EnderChestBlockKey previousKey = activeVisualViewers.get(playerUuid);
        if (key.equals(previousKey)) {
            return;
        }

        if (previousKey != null) {
            releaseVisualViewer(playerUuid);
        }

        activeVisualViewers.put(playerUuid, key);
        int viewers = visualViewerCounts.merge(key, 1, Integer::sum);
        if (viewers == 1) {
            runVisualBlockAction(key, true);
        }
    }

    private synchronized void releaseVisualViewer(UUID playerUuid) {
        EnderChestBlockKey key = activeVisualViewers.remove(playerUuid);
        if (key == null) {
            return;
        }

        Integer currentViewers = visualViewerCounts.get(key);
        if (currentViewers == null || currentViewers <= 1) {
            visualViewerCounts.remove(key);
            runVisualBlockAction(key, false);
            return;
        }

        visualViewerCounts.put(key, currentViewers - 1);
    }

    private synchronized void closeAllVisuals() {
        for (EnderChestBlockKey key : List.copyOf(visualViewerCounts.keySet())) {
            runVisualBlockAction(key, false);
        }
        activeVisualViewers.clear();
        visualViewerCounts.clear();
    }

    private void runVisualBlockAction(EnderChestBlockKey key, boolean open) {
        Location blockLocation = key.toLocation();
        if (blockLocation == null) {
            return;
        }

        plugin.getSpigotScheduler().runRegion(blockLocation, () -> {
            if (blockLocation.getBlock().getType() != Material.ENDER_CHEST) {
                return;
            }

            BlockState state = blockLocation.getBlock().getState();
            if (state instanceof Lidded lidded) {
                if (open) {
                    lidded.open();
                } else {
                    lidded.close();
                }
            }

            if (shouldPlayManualSounds()) {
                SoundUtils.play(
                        blockLocation.clone().add(0.5D, 0.5D, 0.5D),
                        open ? getOpenSound() : getCloseSound()
                );
            }
        });
    }

    private boolean areVanillaEffectsEnabled() {
        return getConfig().getBoolean("ENDER-CHEST.VANILLA-EFFECTS", true);
    }

    private boolean shouldPlayManualSounds() {
        return getConfig().getBoolean("ENDER-CHEST.MANUAL-SOUNDS", false);
    }

    private String getOpenSound() {
        return getConfig().getString("ENDER-CHEST.OPEN-SOUND", "minecraft:block.ender_chest.open|1.0|1.0");
    }

    private String getCloseSound() {
        return getConfig().getString("ENDER-CHEST.CLOSE-SOUND", "minecraft:block.ender_chest.close|1.0|1.0");
    }

    public boolean isRowPermissionsEnabled() {
        FileConfiguration config = getConfig();
        return config == null || config.getBoolean("ENDER-CHEST.ROW-PERMISSIONS.ENABLED", true);
    }

    public boolean returnsOverflowOnDowngrade() {
        FileConfiguration config = getConfig();
        if (config == null) {
            return false;
        }
        return RETURN_ITEMS_MODE.equalsIgnoreCase(
                config.getString("ENDER-CHEST.ROW-PERMISSIONS.ON-DOWNGRADE", "KEEP-SIZE").trim()
        );
    }

    /**
     * Highest row count the player is entitled to by permission, or 0 when no row permission applies.
     */
    public int getPermissionRows(Player player) {
        if (player == null || !isRowPermissionsEnabled()) {
            return 0;
        }

        int resolved = clampToTier(PermissionUtils.resolveHighestExactNumberedPermission(
                player, ROWS_PERMISSION_PREFIX, MAX_ROWS_PERMISSION_VALUE));

        FileConfiguration config = getConfig();
        ConfigurationSection section = config == null
                ? null
                : config.getConfigurationSection("ENDER-CHEST.ROW-PERMISSIONS.PERMISSIONS");
        if (section != null) {
            for (Map.Entry<String, Object> entry : section.getValues(true).entrySet()) {
                if (!(entry.getValue() instanceof Number number)) {
                    continue;
                }
                if (!PermissionUtils.hasExact(player, entry.getKey())) {
                    continue;
                }
                resolved = Math.max(resolved, clampToTier(number.intValue()));
            }
        }

        return resolved;
    }

    /**
     * Rows the player should get right now: their permission tier, or the configured default when
     * they hold no row permission at all.
     */
    public int getEntitledRows(Player player) {
        int permissionRows = getPermissionRows(player);
        return permissionRows > 0 ? clampRows(permissionRows) : getDefaultRows();
    }

    public int getDefaultRows() {
        FileConfiguration ecConfig = getConfig();
        if (ecConfig != null) {
            if (ecConfig.contains("ENDER-CHEST.DEFAULT-ROWS")) {
                return clampRows(ecConfig.getInt("ENDER-CHEST.DEFAULT-ROWS", 6));
            }
            if (ecConfig.contains("ENDER-CHEST.ROWS")) {
                return clampRows(ecConfig.getInt("ENDER-CHEST.ROWS", 6));
            }
            if (ecConfig.contains("ENDER-CHEST.SIX-ROWS")) {
                return ecConfig.getBoolean("ENDER-CHEST.SIX-ROWS", true) ? 6 : 1;
            }
            if (ecConfig.contains("ENDER-CHEST.SIX-ROW")) {
                return ecConfig.getBoolean("ENDER-CHEST.SIX-ROW", true) ? 6 : 1;
            }
        }
        FileConfiguration mainConfig = plugin != null && plugin.getConfigManager() != null ? plugin.getConfigManager().getConfig() : null;
        if (mainConfig != null) {
            if (mainConfig.contains("ENDER-CHEST.DEFAULT-ROWS")) {
                return clampRows(mainConfig.getInt("ENDER-CHEST.DEFAULT-ROWS", 6));
            }
            if (mainConfig.contains("ENDER-CHEST.ROWS")) {
                return clampRows(mainConfig.getInt("ENDER-CHEST.ROWS", 6));
            }
            if (mainConfig.contains("ENDER-CHEST.SIX-ROWS")) {
                return mainConfig.getBoolean("ENDER-CHEST.SIX-ROWS", true) ? 6 : 1;
            }
            if (mainConfig.contains("ENDER-CHEST.SIX-ROW")) {
                return mainConfig.getBoolean("ENDER-CHEST.SIX-ROW", true) ? 6 : 1;
            }
        }
        return 6;
    }

    private String getTitle() {
        return getConfig().getString("ENDER-CHEST.TITLE", "&5ender chest");
    }

    private String getInspectionTitle(String targetName) {
        String resolvedTargetName = targetName == null || targetName.isBlank() ? "Unknown" : targetName;
        return getConfig()
                .getString("ENDER-CHEST.ECSEE.TITLE", "&8ender chest of {player}")
                .replace("{player}", resolvedTargetName)
                .replace("{target}", resolvedTargetName);
    }

    /**
     * Picks the size an ender chest opens at. Stored rows are the size the chest was last saved at,
     * so honouring them keeps items reachable when nothing entitles the player to that size any more.
     * Only ON-DOWNGRADE: RETURN-ITEMS shrinks the chest, and only after the overflow has been handed
     * back and the smaller layout has actually reached the database.
     */
    private int resolveOpenRows(Player player, int dbRows) {
        int defaultRows = getDefaultRows();
        int storedRows = clampRows(dbRows <= 0 ? defaultRows : dbRows);
        int entitledRows = clampRows(getEntitledRows(player));

        if (entitledRows < storedRows
                && returnsOverflowOnDowngrade()
                && applyRowDowngrade(player, entitledRows)) {
            return entitledRows;
        }

        return clampRows(Math.max(entitledRows, storedRows));
    }

    /**
     * Trims a chest down to {@code newRows}, returning anything that no longer fits. The smaller
     * layout is written first so a failed hand-back cannot duplicate items; a failed write leaves the
     * chest at its old size instead.
     */
    private boolean applyRowDowngrade(Player player, int newRows) {
        UUID uuid = player.getUniqueId();
        int keptSize = clampRows(newRows) * 9;

        ItemStack[] rawContents = plugin.getDatabaseManager().loadEnderChestContents(uuid, MAX_ROWS * 9);
        if (rawContents == null) {
            return false;
        }

        ItemStack[] storedContents = sanitizeLoadedContents(uuid, rawContents);
        ItemStack[] kept = Arrays.copyOf(storedContents, keptSize);
        List<ItemStack> overflow = new ArrayList<>();
        for (int slot = keptSize; slot < storedContents.length; slot++) {
            ItemStack item = storedContents[slot];
            if (item != null && !item.getType().isAir()) {
                overflow.add(item);
            }
        }

        if (!plugin.getDatabaseManager().saveEnderChest(uuid, clampRows(newRows), kept)) {
            return false;
        }

        if (!overflow.isEmpty()) {
            giveBackOverflow(player, overflow);
            player.sendMessage(ColorUtils.toComponent(formatMessage(
                    "ROWS-DOWNGRADED",
                    "&eYour ender chest is now {rows} rows. {amount} item(s) that no longer fit were returned to you.",
                    "{rows}", String.valueOf(clampRows(newRows)),
                    "{amount}", String.valueOf(overflow.size())
            )));
        }

        return true;
    }

    private void giveBackOverflow(Player player, List<ItemStack> overflow) {
        for (ItemStack item : overflow) {
            Map<Integer, ItemStack> leftovers = player.getInventory().addItem(item);
            for (ItemStack leftover : leftovers.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), leftover);
            }
        }
        player.updateInventory();
    }

    private int clampToTier(int rows) {
        return rows <= 0 ? 0 : clampRows(rows);
    }

    private int clampRows(int rows) {
        return Math.max(MIN_ROWS, Math.min(MAX_ROWS, rows));
    }

    private FileConfiguration getConfig() {
        return plugin.getConfigManager().getEnderChest();
    }

    private record EnderChestBlockKey(UUID worldUuid, int x, int y, int z) {

        private static EnderChestBlockKey from(Location location) {
            return new EnderChestBlockKey(
                    location.getWorld().getUID(),
                    location.getBlockX(),
                    location.getBlockY(),
                    location.getBlockZ()
            );
        }

        private Location toLocation() {
            World world = Bukkit.getWorld(worldUuid);
            return world == null ? null : new Location(world, x, y, z);
        }
    }
}
