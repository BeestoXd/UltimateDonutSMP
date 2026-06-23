package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.enderchest.EnderChestHolder;
import com.bx.ultimateDonutSmp.enderchest.EnderChestInspectionHolder;
import com.bx.ultimateDonutSmp.enderchest.EnderChestInspectionSession;
import com.bx.ultimateDonutSmp.enderchest.EnderChestSession;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.PermissionUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Lidded;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

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

    private final UltimateDonutSmp plugin;
    private final Map<UUID, EnderChestSession> activeSessions = new HashMap<>();
    private final Map<UUID, EnderChestInspectionSession> inspectionSessionsByViewer = new ConcurrentHashMap<>();
    private final Map<UUID, Set<UUID>> inspectionViewersByTarget = new ConcurrentHashMap<>();
    private final Map<UUID, EnderChestBlockKey> activeVisualViewers = new HashMap<>();
    private final Map<EnderChestBlockKey, Integer> visualViewerCounts = new HashMap<>();
    private ScheduledTask autoSaveTask;
    private ScheduledTask inspectionRefreshTask;

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
            int rows = clampRows(plugin.getDatabaseManager().loadEnderChestRows(uuid, getDefaultRows()));
            EnderChestHolder holder = new EnderChestHolder(uuid, rows);
            Inventory inventory = Bukkit.createInventory(
                    holder,
                    rows * 9,
                    ColorUtils.toComponent(getTitle(), player)
            );
            holder.bind(inventory);
            inventory.setContents(sanitizeLoadedContents(
                    uuid,
                    plugin.getDatabaseManager().loadEnderChestContents(uuid, inventory.getSize())
            ));

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
                    getMessage("OPEN-FAILED", "&cꜰᴀɪʟᴇᴅ ᴛᴏ ᴏᴘᴇɴ ʏᴏᴜʀ ᴇɴᴅᴇʀ ᴄʜᴇѕᴛ. ᴘʟᴇᴀѕᴇ ᴛʀʏ ᴀɢᴀɪɴ.")
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
            int rows = activeTargetSession == null
                    ? clampRows(plugin.getDatabaseManager().loadEnderChestRows(targetUuid, getDefaultRows()))
                    : activeTargetSession.getRows();
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

            ItemStack[] sourceContents = activeTargetSession == null
                    ? sanitizeLoadedContents(
                            targetUuid,
                            plugin.getDatabaseManager().loadEnderChestContents(targetUuid, inventory.getSize())
                    )
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
                    "&cꜰᴀɪʟᴇᴅ ᴛᴏ ᴏᴘᴇɴ {target}'ѕ ᴇɴᴅᴇʀ ᴄʜᴇѕᴛ. ᴘʟᴇᴀѕᴇ ᴛʀʏ ᴀɢᴀɪɴ.",
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
                    getMessage("SAVE-FAILED", "&cꜰᴀɪʟᴇᴅ ᴛᴏ ѕᴀᴠᴇ ʏᴏᴜʀ ᴇɴᴅᴇʀ ᴄʜᴇѕᴛ. ᴄᴏɴᴛᴀᴄᴛ ѕᴛᴀꜰꜰ.")
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
        removeInspectionSession(session);
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

        autoSaveTask = plugin.getFoliaScheduler().runGlobalTimer(
                () -> {
                    for (EnderChestSession session : List.copyOf(activeSessions.values())) {
                        if (!session.isDirty()) {
                            continue;
                        }

                        Player player = plugin.getServer().getPlayer(session.getOwnerUuid());
                        if (player != null && player.isOnline()) {
                            plugin.getFoliaScheduler().runEntity(player, () -> autoSaveSession(session));
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

        inspectionRefreshTask = plugin.getFoliaScheduler().runGlobalTimer(
                () -> {
                    for (UUID targetUuid : List.copyOf(inspectionViewersByTarget.keySet())) {
                        Player target = Bukkit.getPlayer(targetUuid);
                        if (target == null || !target.isOnline()) {
                            continue;
                        }
                        plugin.getFoliaScheduler().runEntity(
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
            plugin.getFoliaScheduler().runEntity(
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

        plugin.getFoliaScheduler().runRegion(blockLocation, () -> {
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

    private int getDefaultRows() {
        return clampRows(getConfig().getInt("ENDER-CHEST.DEFAULT-ROWS", 6));
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
