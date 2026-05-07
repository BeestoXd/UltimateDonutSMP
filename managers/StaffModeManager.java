package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.menus.FrozenPlayersMenu;
import com.bx.ultimateDonutSmp.menus.StaffListMenu;
import com.bx.ultimateDonutSmp.models.FreezeState;
import com.bx.ultimateDonutSmp.models.StaffModeState;
import com.bx.ultimateDonutSmp.staff.StaffInventorySnapshot;
import com.bx.ultimateDonutSmp.staff.StaffModeSession;
import com.bx.ultimateDonutSmp.staff.StaffToolType;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.PlayerSettingUtils;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class StaffModeManager {

    private static final StaffToolType[] TOOL_ORDER = new StaffToolType[]{
            StaffToolType.VANISH,
            StaffToolType.FREEZE,
            StaffToolType.STAFF_LIST,
            StaffToolType.BETTER_VIEW,
            StaffToolType.RANDOM_TELEPORT
    };

    private final UltimateDonutSmp plugin;
    private final Map<UUID, StaffModeState> activeStates = new HashMap<>();
    private final Map<UUID, StaffModeSession> runtimeSessions = new HashMap<>();
    private final Set<UUID> restartRecoveryOnly = new HashSet<>();
    private ScheduledTask vanishActionBarTask;

    public StaffModeManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
        reloadInternal(true);
        restartVanishActionBarTask();
    }

    public void reload() {
        reloadInternal(false);
        restartVanishActionBarTask();
    }

    public void shutdown() {
        stopVanishActionBarTask();
        if (!shouldPersistOnRestart()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (isInStaffMode(player.getUniqueId())) {
                    disableSilently(player, true);
                }
            }
        }

        runtimeSessions.clear();
    }

    public boolean isEnabled() {
        return getConfig().getBoolean("STAFF-MODE.ENABLED", true);
    }

    public boolean shouldPersistOnQuit() {
        return getConfig().getBoolean("STAFF-MODE.PERSIST-ON-QUIT", true);
    }

    public boolean shouldPersistOnRestart() {
        return getConfig().getBoolean("STAFF-MODE.PERSIST-ON-RESTART", true);
    }

    public boolean shouldAutoVanishOnEnable() {
        return getConfig().getBoolean("STAFF-MODE.AUTO-VANISH-ON-ENABLE", true);
    }

    public boolean shouldLockTools() {
        return getConfig().getBoolean("STAFF-MODE.LOCK-TOOLS", true);
    }

    public boolean shouldShowVanishActionBar() {
        return getConfig().getBoolean("STAFF-MODE.VANISH-ACTIONBAR.ENABLED", true);
    }

    public boolean shouldRestoreInventoryOnDisable() {
        return getConfig().getBoolean("STAFF-MODE.RESTORE-INVENTORY-ON-DISABLE", true);
    }

    public String getStaffPermission() {
        return getConfig().getString("STAFF-MODE.STAFF-PERMISSION", "ultimatedonutsmp.staff.mode");
    }

    public String getAdminPermission() {
        return getConfig().getString("STAFF-MODE.ADMIN-PERMISSION", "ultimatedonutsmp.admin.staffmode");
    }

    public String getVanishPermission() {
        return getConfig().getString("STAFF-MODE.VANISH-PERMISSION", "ultimatedonutsmp.staff.mode.vanish");
    }

    public String getBetterViewPermission() {
        return getConfig().getString("STAFF-MODE.BETTER-VIEW-PERMISSION", "ultimatedonutsmp.staff.mode.betterview");
    }

    public String getStaffListPermission() {
        return getConfig().getString("STAFF-MODE.STAFF-LIST-PERMISSION", "ultimatedonutsmp.staff.mode.stafflist");
    }

    public String getRandomTeleportPermission() {
        return getConfig().getString("STAFF-MODE.RANDOM-TELEPORT-PERMISSION", "ultimatedonutsmp.staff.mode.randomtp");
    }

    public String getSeeVanishedPermission() {
        return getConfig().getString("STAFF-MODE.SEE-VANISHED-PERMISSION", "ultimatedonutsmp.staff.mode.seevanished");
    }

    public String getOthersPermission() {
        return getConfig().getString("STAFF-MODE.OTHERS-PERMISSION", "ultimatedonutsmp.staff.mode.others");
    }

    public boolean canUse(CommandSender sender) {
        return !(sender instanceof Player player) || player.hasPermission(getStaffPermission());
    }

    public boolean canAdmin(CommandSender sender) {
        return !(sender instanceof Player player) || player.hasPermission(getAdminPermission());
    }

    public boolean canUseVanish(Player player) {
        return player != null && player.hasPermission(getVanishPermission());
    }

    public boolean canUseBetterView(Player player) {
        return player != null && player.hasPermission(getBetterViewPermission());
    }

    public boolean canOpenStaffList(Player player) {
        return player != null && player.hasPermission(getStaffListPermission());
    }

    public boolean canUseRandomTeleport(Player player) {
        return player != null && player.hasPermission(getRandomTeleportPermission());
    }

    public boolean canManageOthers(CommandSender sender) {
        return !(sender instanceof Player player)
                || player.hasPermission(getOthersPermission())
                || player.hasPermission(getAdminPermission());
    }

    public boolean isInStaffMode(UUID uuid) {
        return uuid != null && activeStates.containsKey(uuid);
    }

    public boolean isVanished(UUID uuid) {
        StaffModeState state = getState(uuid);
        return state != null && state.isVanishActive();
    }

    public boolean isBetterViewEnabled(UUID uuid) {
        StaffModeState state = getState(uuid);
        return state != null && state.isBetterViewActive();
    }

    public StaffModeState getState(UUID uuid) {
        return uuid == null ? null : activeStates.get(uuid);
    }

    public String getMessage(String path, String fallback) {
        return getConfig().getString("MESSAGES." + path, fallback);
    }

    public String getStaffMessage(String path, String fallback) {
        return plugin.getConfigManager().getMessages().getString("STAFF." + path, fallback);
    }

    public String getRandomTeleportMessage(String path, String fallback) {
        return plugin.getConfigManager().getMessages().getString("RANDOMTP." + path, fallback);
    }

    public String formatMessage(String path, String fallback, String... placeholders) {
        String message = getMessage(path, fallback);
        for (int index = 0; index + 1 < placeholders.length; index += 2) {
            message = message.replace(placeholders[index], placeholders[index + 1]);
        }
        return message;
    }

    public StaffModeToggleResult toggle(Player player) {
        if (player == null) {
            return new StaffModeToggleResult(false, false, getMessage("PLAYER-ONLY", "&cOnly players can use this command."));
        }

        if (isInStaffMode(player.getUniqueId())) {
            return disable(player);
        }

        return enable(player);
    }

    public StaffModeToggleResult enable(Player player) {
        return enable(player, true);
    }

    public StaffModeToggleResult enable(Player player, boolean sendMessages) {
        if (player == null) {
            return new StaffModeToggleResult(false, false, getMessage("PLAYER-ONLY", "&cOnly players can use this command."));
        }
        if (!isEnabled()) {
            return new StaffModeToggleResult(false, false, getMessage("FEATURE-DISABLED", "&cStaff mode is currently disabled."));
        }
        if (!canUse(player)) {
            return new StaffModeToggleResult(false, false, getMessage("NO-PERMISSION", "&cYou do not have permission."));
        }
        if (isInStaffMode(player.getUniqueId())) {
            return new StaffModeToggleResult(true, true, getMessage("ENABLED", "&aStaff mode enabled."));
        }

        player.closeInventory();
        StaffInventorySnapshot snapshot = captureSnapshot(player);
        if (!plugin.getDatabaseManager().saveStaffModeSnapshot(player.getUniqueId(), snapshot)) {
            return new StaffModeToggleResult(false, false, getMessage("RESTORE-FAILED", "&cStaff mode restore failed. Contact an admin."));
        }

        StaffModeState state = new StaffModeState(
                player.getUniqueId(),
                player.getName(),
                System.currentTimeMillis(),
                resolveSourceServer(),
                false,
                false,
                true,
                player.getAllowFlight(),
                player.isFlying(),
                player.getInventory().getHeldItemSlot(),
                false,
                player.getGameMode()
        );
        activeStates.put(player.getUniqueId(), state);
        plugin.getDatabaseManager().saveStaffModeState(state);
        runtimeSessions.put(player.getUniqueId(), new StaffModeSession(player.getUniqueId()));
        restartRecoveryOnly.remove(player.getUniqueId());

        player.setGameMode(GameMode.CREATIVE);
        player.setAllowFlight(true);
        rebuildTools(player);
        if (shouldAutoVanishOnEnable() && canUseVanish(player)) {
            setVanish(player, true, false);
        } else {
            refreshViewerVisibility(player);
        }

        if (sendMessages) {
            sendSelfToggleMessages(player, true);
        }
        return new StaffModeToggleResult(true, true, localizeStatus(true));
    }

    public StaffModeToggleResult disable(Player player) {
        return disable(player, true);
    }

    public StaffModeToggleResult disable(Player player, boolean sendMessages) {
        if (player == null) {
            return new StaffModeToggleResult(false, false, getMessage("PLAYER-ONLY", "&cOnly players can use this command."));
        }

        boolean restored = disableSilently(player, true);
        if (!restored) {
            return new StaffModeToggleResult(false, false, getMessage("RESTORE-FAILED", "&cStaff mode restore failed. Contact an admin."));
        }

        if (sendMessages) {
            sendSelfToggleMessages(player, false);
        }
        return new StaffModeToggleResult(true, false, localizeStatus(false));
    }

    public boolean toggleVanish(Player player) {
        if (player == null || !isInStaffMode(player.getUniqueId()) || !canUseVanish(player)) {
            return false;
        }

        return setVanish(player, !isVanished(player.getUniqueId()), true);
    }

    public boolean toggleBetterView(Player player) {
        if (player == null || !isInStaffMode(player.getUniqueId()) || !canUseBetterView(player)) {
            return false;
        }

        StaffModeState state = getState(player.getUniqueId());
        if (state == null) {
            return false;
        }

        boolean nextActive = !state.isBetterViewActive();
        if (nextActive) {
            boolean nightVisionOwned = false;
            if (isBetterViewNightVisionEnabled() && !player.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false));
                nightVisionOwned = true;
            }
            if (isBetterViewFlightEnabled() && isBetterViewAutoFlyEnabled()) {
                player.setFlying(true);
            }

            StaffModeState updated = state.withBetterView(true, nightVisionOwned);
            activeStates.put(player.getUniqueId(), updated);
            plugin.getDatabaseManager().saveStaffModeState(updated);
            refreshTool(player, StaffToolType.BETTER_VIEW);
            player.sendMessage(ColorUtils.toComponent(getMessage("BETTER-VIEW-ON", "&aBetter View enabled."), player));
            PlayerSettingUtils.sendActionBar(plugin, player, getMessage("BETTER-VIEW-ON", "&aBetter View enabled."));
            return true;
        }

        clearBetterView(player, state);
        StaffModeState updated = state.withBetterView(false, false);
        activeStates.put(player.getUniqueId(), updated);
        plugin.getDatabaseManager().saveStaffModeState(updated);
        refreshTool(player, StaffToolType.BETTER_VIEW);
        player.sendMessage(ColorUtils.toComponent(getMessage("BETTER-VIEW-OFF", "&cBetter View disabled."), player));
        PlayerSettingUtils.sendActionBar(plugin, player, getMessage("BETTER-VIEW-OFF", "&cBetter View disabled."));
        return true;
    }

    public Player teleportToRandomPlayer(Player viewer) {
        Player target = findRandomTeleportTarget(viewer);
        if (viewer == null || target == null) {
            return null;
        }

        StaffModeSession session = runtimeSessions.computeIfAbsent(viewer.getUniqueId(), StaffModeSession::new);
        session.setLastRandomTeleportTarget(target.getUniqueId());
        String message = formatLegacyMessage(
                getRandomTeleportMessage("SUCCESS", "&8[&dRTP&8] &7You were teleported to &e%player%"),
                target.getName(),
                null,
                null,
                target.getName(),
                null
        );
        String viewerName = viewer.getName();
        plugin.getPaperScheduler().teleport(viewer, target.getLocation()).thenAccept(success -> {
            if (!Boolean.TRUE.equals(success)) {
                return;
            }
            plugin.getPaperScheduler().runEntity(viewer, () -> {
                if (viewer.isOnline()) {
                    viewer.sendMessage(ColorUtils.toComponent(message, viewer));
                    PlayerSettingUtils.sendActionBar(plugin, viewer, message);
                }
            });
            plugin.getPaperScheduler().runEntity(target, () -> {
                if (target.isOnline()) {
                    target.sendMessage(ColorUtils.toComponent(
                            formatLegacyMessage(
                                    getRandomTeleportMessage("NOTIFY", "&8[&dRTP&8] &e%player% &7appeared near you!"),
                                    viewerName,
                                    null,
                                    null,
                                    viewerName,
                                    null
                            ),
                            target
                    ));
                }
            });
        });
        return target;
    }

    public Player findRandomTeleportTarget(Player viewer) {
        if (viewer == null) {
            return null;
        }

        List<Player> candidates = new ArrayList<>();
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (isEligibleRandomTeleportTarget(viewer, target)) {
                candidates.add(target);
            }
        }

        if (candidates.isEmpty()) {
            return null;
        }

        StaffModeSession session = runtimeSessions.get(viewer.getUniqueId());
        UUID previousTarget = session == null ? null : session.getLastRandomTeleportTarget();
        if (previousTarget != null && candidates.size() > 1) {
            candidates.removeIf(player -> previousTarget.equals(player.getUniqueId()));
        }

        return candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
    }

    public void openStaffList(Player viewer) {
        if (viewer == null) {
            return;
        }
        new StaffListMenu(plugin).open(viewer);
    }

    public void openFrozenPlayers(Player viewer) {
        if (viewer == null) {
            return;
        }
        new FrozenPlayersMenu(plugin).open(viewer);
    }

    public boolean isStaffTool(ItemStack item) {
        return resolveTool(item) != null;
    }

    public StaffToolType resolveTool(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }

        PersistentDataContainer data = meta.getPersistentDataContainer();
        String raw = data.get(toolKey(), PersistentDataType.STRING);
        return StaffToolType.fromPersistentId(raw);
    }

    public void sendToolLockedMessage(Player player) {
        if (player == null) {
            return;
        }

        player.sendMessage(ColorUtils.toComponent(getMessage("TOOL-LOCKED", "&cYour staff tools are locked while Staff Mode is active."), player));
    }

    public void handleJoin(Player player) {
        if (player == null) {
            return;
        }

        StaffModeState state = activeStates.get(player.getUniqueId());
        if (state == null) {
            refreshViewerVisibility(player);
            return;
        }

        if (!player.getName().equals(state.getStaffNameSnapshot())) {
            state = state.withStaffNameSnapshot(player.getName());
            activeStates.put(player.getUniqueId(), state);
            plugin.getDatabaseManager().saveStaffModeState(state);
        }

        if (restartRecoveryOnly.remove(player.getUniqueId())) {
            if (disableSilently(player, true)) {
                player.sendMessage(ColorUtils.toComponent(getMessage(
                        "RECOVERED-AFTER-RESTART",
                        "&eStaff mode was disabled because the server restarted. Your inventory was restored."
                ), player));
            } else {
                player.sendMessage(ColorUtils.toComponent(getMessage("RESTORE-FAILED", "&cStaff mode restore failed. Contact an admin."), player));
            }
            refreshViewerVisibility(player);
            return;
        }

        runtimeSessions.put(player.getUniqueId(), new StaffModeSession(player.getUniqueId()));
        player.setGameMode(GameMode.CREATIVE);
        player.setAllowFlight(true);
        rebuildTools(player);

        if (state.isBetterViewActive()) {
            reapplyBetterView(player, state);
        }

        refreshViewerVisibility(player);
        if (state.isVanishActive()) {
            refreshTargetVisibility(player);
        }
    }

    public void handleQuit(Player player) {
        if (player == null) {
            return;
        }

        if (!isInStaffMode(player.getUniqueId())) {
            runtimeSessions.remove(player.getUniqueId());
            return;
        }

        if (!shouldPersistOnQuit()) {
            disableSilently(player, true);
            return;
        }

        runtimeSessions.remove(player.getUniqueId());
    }

    public void handleRespawn(Player player) {
        if (player == null || !isInStaffMode(player.getUniqueId())) {
            return;
        }

        plugin.getPaperScheduler().runEntityLater(player, () -> {
            if (!player.isOnline() || !isInStaffMode(player.getUniqueId())) {
                return;
            }
            player.setGameMode(GameMode.CREATIVE);
            player.setAllowFlight(true);
            rebuildTools(player);
            StaffModeState state = getState(player.getUniqueId());
            if (state != null && state.isBetterViewActive()) {
                reapplyBetterView(player, state);
            }
            refreshViewerVisibility(player);
            if (state != null && state.isVanishActive()) {
                refreshTargetVisibility(player);
            }
        }, 2L);
    }

    public void handleDeath(PlayerDeathEvent event) {
        if (event == null || !isInStaffMode(event.getEntity().getUniqueId())) {
            return;
        }

        event.setKeepInventory(true);
        event.getDrops().clear();
    }

    public List<Player> getOnlineStaffMembers() {
        List<Player> staffMembers = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isStaffMember(player)) {
                staffMembers.add(player);
            }
        }
        staffMembers.sort(Comparator.comparing(Player::getName, String.CASE_INSENSITIVE_ORDER));
        return staffMembers;
    }

    public List<FreezeState> getFrozenStates() {
        return plugin.getFreezeManager().getActiveStates();
    }

    public String getLocalServerDisplayName() {
        return plugin.getConfigManager().getNetwork().getString("NETWORK-STATUS.LOCAL-DISPLAY-NAME", "Local");
    }

    public String getPlayerStatusSummary(Player player) {
        if (player == null) {
            return "Offline";
        }
        if (plugin.getAFKManager().isAfk(player.getUniqueId())) {
            return "Online (AFK)";
        }
        return "Online";
    }

    public int getMenuRefreshSlot(String menuKey) {
        return menuSection(menuKey).getInt("REFRESH-SLOT", 49);
    }

    public int getMenuSize(String menuKey) {
        int configured = menuSection(menuKey).getInt("SIZE", 54);
        if (configured < 9 || configured > 54 || configured % 9 != 0) {
            return 54;
        }
        return configured;
    }

    public String getMenuTitle(String menuKey, String fallback) {
        return menuSection(menuKey).getString("TITLE", fallback);
    }

    public Material getMenuPlaceholderMaterial(String menuKey, Material fallback) {
        return parseMaterial(menuSection(menuKey).getString("PLACEHOLDER-MATERIAL", fallback.name()), fallback);
    }

    public List<Integer> getMenuContentSlots(String menuKey, int inventorySize) {
        List<Integer> configured = menuSection(menuKey).getIntegerList("CONTENT-SLOTS");
        List<Integer> slots = new ArrayList<>();
        for (int slot : configured) {
            if (slot >= 0 && slot < inventorySize) {
                slots.add(slot);
            }
        }
        if (!slots.isEmpty()) {
            return slots;
        }

        for (int slot = 0; slot < inventorySize; slot++) {
            slots.add(slot);
        }
        return slots;
    }

    public ItemStack createRefreshItem(String menuKey) {
        return ItemUtils.createItem(Material.CLOCK, "&eRefresh", List.of("&7Click to refresh this view."));
    }

    public ItemStack createMenuEmptyItem(String menuKey) {
        ConfigurationSection section = menuSection(menuKey);
        return ItemUtils.createItem(
                parseMaterial(section.getString("EMPTY-MATERIAL", "BARRIER"), Material.BARRIER),
                section.getString("EMPTY-NAME", "&cNothing here"),
                section.getStringList("EMPTY-LORE")
        );
    }

    public String localizeState(boolean active) {
        return getStaffMessage(active ? "STATE_ACTIVE" : "STATE_INACTIVE", active ? "&aActive" : "&cInactive");
    }

    private String localizeStatus(boolean enabled) {
        return getStaffMessage(enabled ? "STATUS_ENABLED" : "STATUS_DISABLED", enabled ? "&aEnabled" : "&cDisabled");
    }

    private String localizeIcon(boolean enabled) {
        return getStaffMessage(enabled ? "ICON_ENABLED" : "ICON_DISABLED", enabled ? "&a✓" : "&c✗");
    }

    private void sendSelfToggleMessages(Player player, boolean enabled) {
        String status = localizeStatus(enabled);
        String icon = localizeIcon(enabled);

        player.sendMessage(ColorUtils.toComponent(
                formatLegacyMessage(
                        getStaffMessage("SELF_TOGGLE", "&6&lSTAFF MODE &7» %status%&7!"),
                        player.getName(),
                        player.getName(),
                        status,
                        null,
                        icon
                ),
                player
        ));
        player.sendMessage(ColorUtils.toComponent(
                formatLegacyMessage(
                        getStaffMessage("SELF_STATUS", "&7Status: %icon% %status%"),
                        player.getName(),
                        player.getName(),
                        status,
                        null,
                        icon
                ),
                player
        ));
        PlayerSettingUtils.sendActionBar(plugin, player, status);

        if (!enabled) {
            return;
        }

        List<String> lines = plugin.getConfigManager().getMessages().getStringList("STAFF.ACTIVATION_MESSAGES");
        for (String line : lines) {
            player.sendMessage(ColorUtils.toComponent(line, player));
        }
    }

    public void notifyExternalToggle(CommandSender actor, Player target, boolean enabled) {
        String status = localizeStatus(enabled);
        String icon = localizeIcon(enabled);
        String actorName = actor instanceof Player player ? player.getName() : actor.getName();

        if (actor instanceof Player player) {
            player.sendMessage(ColorUtils.toComponent(
                    formatLegacyMessage(
                            getStaffMessage("OTHER_TOGGLE", "&6&lSTAFF MODE &7» %status% &7staff mode for &e%target%&7!"),
                            actorName,
                            target.getName(),
                            status,
                            target.getName(),
                            icon
                    ),
                    player
            ));
        }

        target.sendMessage(ColorUtils.toComponent(
                formatLegacyMessage(
                        getStaffMessage("TARGET_NOTIFY", "&6&lSTAFF MODE &7» Your staff mode has been %status% &7by &e%staff%&7!"),
                        actorName,
                        target.getName(),
                        status,
                        target.getName(),
                        icon
                ),
                target
        ));

        String broadcast = formatLegacyMessage(
                getStaffMessage("BROADCAST", "&8[&6STAFF&8] &e%staff% &7has %status% &7staff mode for &e%target%"),
                actorName,
                target.getName(),
                status,
                target.getName(),
                icon
        );
        UUID actorUuid = actor instanceof Player player ? player.getUniqueId() : null;
        broadcastToStaff(broadcast, actorUuid, target.getUniqueId());
    }

    private void broadcastToStaff(String message, UUID... excludedUuids) {
        if (message == null || message.isBlank()) {
            return;
        }

        Set<UUID> excluded = new HashSet<>();
        if (excludedUuids != null) {
            for (UUID excludedUuid : excludedUuids) {
                if (excludedUuid != null) {
                    excluded.add(excludedUuid);
                }
            }
        }

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!isStaffMember(online)) {
                continue;
            }
            if (excluded.contains(online.getUniqueId())) {
                continue;
            }
            online.sendMessage(ColorUtils.toComponent(message, online));
        }
        plugin.getLogger().info(ColorUtils.colorize(message));
    }

    private String formatLegacyMessage(String template,
                                       String staffName,
                                       String targetName,
                                       String status,
                                       String playerName,
                                       String icon) {
        return template
                .replace("%staff%", staffName == null ? "" : staffName)
                .replace("%sender%", staffName == null ? "" : staffName)
                .replace("%target%", targetName == null ? "" : targetName)
                .replace("%player%", playerName == null ? (targetName == null ? "" : targetName) : playerName)
                .replace("%status%", status == null ? "" : status)
                .replace("%icon%", icon == null ? "" : icon);
    }

    private void reloadInternal(boolean startup) {
        Set<UUID> preservedRecoveryOnly = new HashSet<>(restartRecoveryOnly);
        runtimeSessions.clear();
        activeStates.clear();
        restartRecoveryOnly.clear();

        for (StaffModeState state : plugin.getDatabaseManager().loadActiveStaffModeStates()) {
            if (state.getStaffUuid() != null) {
                activeStates.put(state.getStaffUuid(), state);
                if (startup && !shouldPersistOnRestart()) {
                    restartRecoveryOnly.add(state.getStaffUuid());
                } else if (!startup && preservedRecoveryOnly.contains(state.getStaffUuid())) {
                    restartRecoveryOnly.add(state.getStaffUuid());
                }
            }
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!isInStaffMode(player.getUniqueId())) {
                continue;
            }

            if (restartRecoveryOnly.remove(player.getUniqueId())) {
                disableSilently(player, true);
                player.sendMessage(ColorUtils.toComponent(getMessage(
                        "RECOVERED-AFTER-RESTART",
                        "&eStaff mode was disabled because the server restarted. Your inventory was restored."
                ), player));
                continue;
            }

            runtimeSessions.put(player.getUniqueId(), new StaffModeSession(player.getUniqueId()));
            player.setGameMode(GameMode.CREATIVE);
            player.setAllowFlight(true);
            rebuildTools(player);
            StaffModeState state = getState(player.getUniqueId());
            if (state != null && state.isBetterViewActive()) {
                reapplyBetterView(player, state);
            }
        }

        refreshAllVanishVisibility();
    }

    private void restartVanishActionBarTask() {
        stopVanishActionBarTask();
        if (!shouldShowVanishActionBar()) {
            return;
        }

        long intervalTicks = Math.max(20L, getConfig().getLong("STAFF-MODE.VANISH-ACTIONBAR.INTERVAL-TICKS", 40L));
        vanishActionBarTask = plugin.getPaperScheduler().runGlobalTimer(
                this::sendVanishActionBars,
                20L,
                intervalTicks
        );
    }

    private void stopVanishActionBarTask() {
        if (vanishActionBarTask != null) {
            vanishActionBarTask.cancel();
            vanishActionBarTask = null;
        }
    }

    private void sendVanishActionBars() {
        if (!isEnabled() || activeStates.isEmpty()) {
            return;
        }

        String message = getConfig().getString(
                "STAFF-MODE.VANISH-ACTIONBAR.MESSAGE",
                "&aVANISHED &7>> &fYou are hidden from regular players"
        );
        if (message == null || message.isBlank()) {
            return;
        }

        plugin.getPaperScheduler().forEachOnlinePlayer(player -> {
            if (isVanished(player.getUniqueId())) {
                PlayerSettingUtils.sendActionBar(plugin, player, message);
            }
        });
    }

    private boolean disableSilently(Player player, boolean restoreInventory) {
        StaffModeState state = getState(player.getUniqueId());
        if (state == null) {
            return true;
        }

        player.closeInventory();

        if (state.isVanishActive()) {
            applyVanish(player, false);
        } else {
            refreshViewerVisibility(player);
        }

        clearBetterView(player, state);

        boolean restored = true;
        if (restoreInventory) {
            restored = restoreInventorySnapshot(player, state);
        }

        if (!restored) {
            return false;
        }

        runtimeSessions.remove(player.getUniqueId());
        activeStates.remove(player.getUniqueId());
        restartRecoveryOnly.remove(player.getUniqueId());
        plugin.getDatabaseManager().deleteStaffModeState(player.getUniqueId());
        plugin.getDatabaseManager().deleteStaffModeSnapshot(player.getUniqueId());
        player.updateInventory();
        return true;
    }

    private StaffInventorySnapshot captureSnapshot(Player player) {
        PlayerInventory inventory = player.getInventory();
        return new StaffInventorySnapshot(
                inventory.getStorageContents(),
                inventory.getArmorContents(),
                inventory.getItemInOffHand()
        );
    }

    private boolean restoreInventorySnapshot(Player player, StaffModeState state) {
        StaffInventorySnapshot snapshot = plugin.getDatabaseManager().loadStaffModeSnapshot(player.getUniqueId());
        if (snapshot == null) {
            return false;
        }

        PlayerInventory inventory = player.getInventory();
        inventory.setStorageContents(snapshot.getStorageContents());
        inventory.setArmorContents(snapshot.getArmorContents());
        inventory.setItemInOffHand(snapshot.getOffhandItem());
        inventory.setHeldItemSlot(state.getPreviousSelectedSlot());
        player.setGameMode(state.getPreviousGameMode());
        player.setAllowFlight(state.isPreviousAllowFlight());
        player.setFlying(state.isPreviousAllowFlight() && state.isPreviousFlying());
        if (state.isNightVisionOwned()) {
            player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        }
        return true;
    }

    private boolean setVanish(Player player, boolean active, boolean notify) {
        if (player == null || !isInStaffMode(player.getUniqueId())) {
            return false;
        }

        StaffModeState currentState = getState(player.getUniqueId());
        if (currentState == null) {
            return false;
        }

        StaffModeState updatedState = currentState.withVanishActive(active);
        activeStates.put(player.getUniqueId(), updatedState);
        plugin.getDatabaseManager().saveStaffModeState(updatedState);
        applyVanish(player, active);
        refreshTool(player, StaffToolType.VANISH);

        if (notify) {
            String path = active ? "VANISH-ON" : "VANISH-OFF";
            String fallback = active ? "&aVanish enabled." : "&cVanish disabled.";
            player.sendMessage(ColorUtils.toComponent(getMessage(path, fallback), player));
            PlayerSettingUtils.sendActionBar(plugin, player, getMessage(path, fallback));
        }

        return active;
    }

    private void applyVanish(Player player, boolean active) {
        if (player == null) {
            return;
        }

        if (active) {
            refreshTargetVisibility(player);
            return;
        }

        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (viewer.getUniqueId().equals(player.getUniqueId())) {
                continue;
            }
            viewer.showPlayer(plugin, player);
        }
    }

    private void clearBetterView(Player player, StaffModeState state) {
        if (player == null || state == null) {
            return;
        }

        if (state.isNightVisionOwned()) {
            player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        }
        if (isBetterViewFlightEnabled() && isBetterViewAutoFlyEnabled()) {
            player.setFlying(false);
        }
    }

    private void reapplyBetterView(Player player, StaffModeState state) {
        if (player == null || state == null) {
            return;
        }

        if (isBetterViewNightVisionEnabled() && state.isNightVisionOwned()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false));
        }
        if (isBetterViewFlightEnabled() && isBetterViewAutoFlyEnabled()) {
            player.setFlying(true);
        }
    }

    private void refreshAllVanishVisibility() {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            refreshViewerVisibility(viewer);
        }

        for (Player target : Bukkit.getOnlinePlayers()) {
            if (isVanished(target.getUniqueId())) {
                refreshTargetVisibility(target);
            }
        }
    }

    private void refreshViewerVisibility(Player viewer) {
        if (viewer == null) {
            return;
        }

        for (Player target : Bukkit.getOnlinePlayers()) {
            if (target.getUniqueId().equals(viewer.getUniqueId())) {
                continue;
            }
            if (isVanished(target.getUniqueId()) && !canSeeVanished(viewer, target)) {
                viewer.hidePlayer(plugin, target);
            }
        }
    }

    private void refreshTargetVisibility(Player target) {
        if (target == null) {
            return;
        }

        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (viewer.getUniqueId().equals(target.getUniqueId())) {
                continue;
            }
            if (isVanished(target.getUniqueId()) && !canSeeVanished(viewer, target)) {
                viewer.hidePlayer(plugin, target);
            } else {
                viewer.showPlayer(plugin, target);
            }
        }
    }

    private boolean canSeeVanished(Player viewer, Player vanishedTarget) {
        return viewer != null
                && vanishedTarget != null
                && (viewer.getUniqueId().equals(vanishedTarget.getUniqueId())
                || viewer.hasPermission(getSeeVanishedPermission()));
    }

    private void rebuildTools(Player player) {
        if (player == null || !isInStaffMode(player.getUniqueId())) {
            return;
        }

        PlayerInventory inventory = player.getInventory();
        inventory.setStorageContents(new ItemStack[36]);
        inventory.setArmorContents(new ItemStack[4]);
        inventory.setItemInOffHand(null);

        for (StaffToolType toolType : TOOL_ORDER) {
            int slot = getToolSlot(toolType);
            if (slot < 0 || slot >= 9) {
                continue;
            }
            inventory.setItem(slot, buildToolItem(player, toolType));
        }

        inventory.setHeldItemSlot(Math.max(0, Math.min(8, getToolSlot(StaffToolType.VANISH))));
        player.updateInventory();
    }

    private void refreshTool(Player player, StaffToolType toolType) {
        if (player == null || toolType == null || !isInStaffMode(player.getUniqueId())) {
            return;
        }

        int slot = getToolSlot(toolType);
        if (slot < 0 || slot >= player.getInventory().getSize()) {
            return;
        }
        player.getInventory().setItem(slot, buildToolItem(player, toolType));
        player.updateInventory();
    }

    private ItemStack buildToolItem(Player player, StaffToolType toolType) {
        String path = "ITEMS." + toolType.getConfigKey();
        Material material;
        String displayName;
        List<String> lore;

        if (toolType == StaffToolType.VANISH) {
            String statePath = isVanished(player.getUniqueId()) ? "ENABLED" : "DISABLED";
            path = path + "." + statePath;
            material = parseMaterial(getConfig().getString(path + ".MATERIAL", "GRAY_DYE"), Material.GRAY_DYE);
            displayName = getConfig().getString(path + ".NAME", "&7Unvanished");
            lore = getConfig().getStringList(path + ".LORE");
        } else {
            material = parseMaterial(getConfig().getString(path + ".MATERIAL", "STONE"), Material.STONE);
            displayName = getConfig().getString(path + ".NAME", "&fTool");
            lore = getConfig().getStringList(path + ".LORE");
        }

        ItemStack item = material == Material.PLAYER_HEAD
                ? ItemUtils.createPlayerHead(player, displayName, lore)
                : ItemUtils.createItem(material, displayName, lore);
        tagToolItem(item, toolType);
        return item;
    }

    private void tagToolItem(ItemStack item, StaffToolType toolType) {
        if (item == null || toolType == null) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.getPersistentDataContainer().set(toolKey(), PersistentDataType.STRING, toolType.name());
        item.setItemMeta(meta);
    }

    private boolean isEligibleRandomTeleportTarget(Player viewer, Player target) {
        if (viewer == null || target == null || !target.isOnline()) {
            return false;
        }
        if (viewer.getUniqueId().equals(target.getUniqueId())) {
            return false;
        }
        if (getConfig().getBoolean("STAFF-MODE.RANDOM-TELEPORT.EXCLUDE-VANISHED", true)
                && isVanished(target.getUniqueId())) {
            return false;
        }
        if (getConfig().getBoolean("STAFF-MODE.RANDOM-TELEPORT.EXCLUDE-STAFF", true)
                && isStaffMember(target)) {
            return false;
        }
        if (getConfig().getBoolean("STAFF-MODE.RANDOM-TELEPORT.EXCLUDE-FROZEN", false)
                && plugin.getFreezeManager().isFrozen(target.getUniqueId())) {
            return false;
        }
        if (getConfig().getBoolean("STAFF-MODE.RANDOM-TELEPORT.EXCLUDE-DUELS", true)
                && (plugin.getDuelManager().isInDuel(target.getUniqueId())
                || plugin.getDuelManager().isInQueue(target.getUniqueId()))) {
            return false;
        }
        if (getConfig().getBoolean("STAFF-MODE.RANDOM-TELEPORT.EXCLUDE-FFA", true)
                && (plugin.getFfaManager().isInMatch(target.getUniqueId())
                || plugin.getFfaManager().isInQueue(target.getUniqueId()))) {
            return false;
        }
        return true;
    }

    public boolean isStaffMember(Player player) {
        if (player == null) {
            return false;
        }
        return player.hasPermission(getStaffPermission())
                || player.hasPermission(plugin.getFreezeManager().getStaffPermission())
                || player.hasPermission("ultimatedonutsmp.staff.invsee")
                || player.hasPermission("ultimatedonutsmp.staff.profileviewer")
                || player.hasPermission("ultimatedonutsmp.staff.punishments.view")
                || player.hasPermission("ultimatedonutsmp.staff.punishments.create")
                || player.hasPermission("ultimatedonutsmp.staff.punishments.remove")
                || player.hasPermission("ultimatedonutsmp.staff.punishments.delete");
    }

    private boolean isBetterViewNightVisionEnabled() {
        return getConfig().getBoolean("STAFF-MODE.BETTER-VIEW.ENABLE-NIGHT-VISION", true);
    }

    private boolean isBetterViewFlightEnabled() {
        return getConfig().getBoolean("STAFF-MODE.BETTER-VIEW.ENABLE-FLIGHT", true);
    }

    private boolean isBetterViewAutoFlyEnabled() {
        return getConfig().getBoolean("STAFF-MODE.BETTER-VIEW.AUTO-FLY", true);
    }

    private int getToolSlot(StaffToolType toolType) {
        return Math.max(0, Math.min(8, getConfig().getInt(
                "STAFF-MODE.HOTBAR-SLOTS." + toolType.getConfigKey(),
                defaultSlot(toolType)
        )));
    }

    private int defaultSlot(StaffToolType toolType) {
        return switch (toolType) {
            case VANISH -> 0;
            case FREEZE -> 1;
            case STAFF_LIST -> 4;
            case BETTER_VIEW -> 7;
            case RANDOM_TELEPORT -> 8;
        };
    }

    private NamespacedKey toolKey() {
        return plugin.getKey("staff_tool_type");
    }

    private String resolveSourceServer() {
        String configured = plugin.getConfigManager().getNetwork().getString("NETWORK-STATUS.LOCAL-SERVER-ID", "local");
        return configured == null || configured.isBlank() ? "local" : configured.trim().toLowerCase(Locale.ROOT);
    }

    private ConfigurationSection menuSection(String menuKey) {
        ConfigurationSection section = getConfig().getConfigurationSection("MENUS." + menuKey);
        return section == null ? new MemoryConfiguration() : section;
    }

    private Material parseMaterial(String raw, Material fallback) {
        Material material = Material.matchMaterial(raw == null ? "" : raw.trim().toUpperCase(Locale.ROOT));
        return material == null ? fallback : material;
    }

    private FileConfiguration getConfig() {
        return plugin.getConfigManager().getStaffMode();
    }

    public record StaffModeToggleResult(boolean success, boolean active, String message) {
    }
}
