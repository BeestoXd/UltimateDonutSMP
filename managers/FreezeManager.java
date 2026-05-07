package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.freeze.FreezeSession;
import com.bx.ultimateDonutSmp.models.FreezeState;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

public class FreezeManager {

    private final UltimateDonutSmp plugin;
    private final Map<UUID, FreezeState> activeStates = new HashMap<>();
    private final Map<UUID, FreezeSession> runtimeSessions = new HashMap<>();

    public FreezeManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        activeStates.clear();
        runtimeSessions.clear();

        for (FreezeState state : plugin.getDatabaseManager().loadActiveFreezeStates()) {
            if (state.getTargetUuid() != null) {
                activeStates.put(state.getTargetUuid(), state);
            }
        }

        if (!isEnabled()) {
            return;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (hasActiveFreeze(player.getUniqueId())) {
                createOrUpdateSession(player, player.getLocation());
            }
        }
    }

    public void shutdown() {
        runtimeSessions.clear();

        if (shouldPersistOnRestart()) {
            return;
        }

        for (UUID targetUuid : List.copyOf(activeStates.keySet())) {
            plugin.getDatabaseManager().deleteFreezeState(targetUuid);
        }
        activeStates.clear();
    }

    public boolean isEnabled() {
        return getConfig().getBoolean("FREEZE.ENABLED", true);
    }

    public boolean shouldPersistOnQuit() {
        return getConfig().getBoolean("FREEZE.PERSIST-ON-QUIT", true);
    }

    public boolean shouldPersistOnRestart() {
        return getConfig().getBoolean("FREEZE.PERSIST-ON-RESTART", true);
    }

    public boolean allowLook() {
        return getConfig().getBoolean("FREEZE.ALLOW-LOOK", true);
    }

    public boolean shouldLogUsage() {
        return getConfig().getBoolean("FREEZE.LOG-USAGE", true);
    }

    public String getStaffPermission() {
        return getConfig().getString("FREEZE.STAFF-PERMISSION", "ultimatedonutsmp.staff.freeze");
    }

    public String getAlertPermission() {
        return getConfig().getString("FREEZE.ALERT-PERMISSION", "ultimatedonutsmp.staff.freeze.alert");
    }

    public String getExemptPermission() {
        return getConfig().getString("FREEZE.EXEMPT-PERMISSION", "ultimatedonutsmp.staff.freeze.exempt");
    }

    public String getAdminPermission() {
        return getConfig().getString("FREEZE.ADMIN-PERMISSION", "ultimatedonutsmp.admin.freeze");
    }

    public boolean canUse(CommandSender sender) {
        return !(sender instanceof Player player) || player.hasPermission(getStaffPermission());
    }

    public boolean canAdmin(CommandSender sender) {
        return !(sender instanceof Player player) || player.hasPermission(getAdminPermission());
    }

    public boolean isSelfTarget(CommandSender sender, Player target) {
        return sender instanceof Player player && target != null
                && player.getUniqueId().equals(target.getUniqueId());
    }

    public boolean canFreeze(CommandSender sender, Player target) {
        if (target == null) {
            return false;
        }
        if (!canUse(sender)) {
            return false;
        }
        if (isSelfTarget(sender, target)) {
            return false;
        }
        if (!target.hasPermission(getExemptPermission())) {
            return true;
        }
        return canAdmin(sender);
    }

    public boolean hasActiveFreeze(UUID targetUuid) {
        return targetUuid != null && activeStates.containsKey(targetUuid);
    }

    public boolean isFrozen(UUID targetUuid) {
        return isEnabled() && hasActiveFreeze(targetUuid);
    }

    public FreezeState getActiveState(UUID targetUuid) {
        return targetUuid == null ? null : activeStates.get(targetUuid);
    }

    public List<FreezeState> getActiveStates() {
        List<FreezeState> states = new ArrayList<>(activeStates.values());
        states.sort(Comparator.comparing(FreezeState::getTargetNameSnapshot, String.CASE_INSENSITIVE_ORDER));
        return states;
    }

    public FreezeState findActiveState(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }

        for (FreezeState state : activeStates.values()) {
            if (input.equalsIgnoreCase(state.getTargetNameSnapshot())) {
                return state;
            }
        }
        return null;
    }

    public Player findOnlineTarget(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }

        Player exact = Bukkit.getPlayerExact(username);
        if (exact != null) {
            return exact;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getName().equalsIgnoreCase(username)) {
                return player;
            }
        }
        return null;
    }

    public boolean hasKnownPlayer(String username) {
        return username != null
                && !username.isBlank()
                && plugin.getDatabaseManager().findPlayerUuidByUsername(username) != null;
    }

    public FreezeToggleResult freeze(CommandSender actor, Player target) {
        if (target == null) {
            return null;
        }

        FreezeState state = new FreezeState(
                target.getUniqueId(),
                target.getName(),
                actor instanceof Player player ? player.getUniqueId() : null,
                actor instanceof Player player ? player.getName() : "Console",
                System.currentTimeMillis(),
                getServerName()
        );

        activeStates.put(target.getUniqueId(), state);
        plugin.getDatabaseManager().saveFreezeState(state);
        createOrUpdateSession(target, target.getLocation());
        sendAlert(target, true);

        if (shouldLogUsage()) {
            plugin.getLogger().info("Freeze enabled: target=" + target.getName() + " actor=" + state.getFrozenByNameSnapshot());
        }

        return new FreezeToggleResult(true, state);
    }

    public FreezeToggleResult unfreeze(CommandSender actor, UUID targetUuid) {
        if (targetUuid == null) {
            return null;
        }

        FreezeState removed = activeStates.remove(targetUuid);
        if (removed == null) {
            return null;
        }

        runtimeSessions.remove(targetUuid);
        plugin.getDatabaseManager().deleteFreezeState(targetUuid);

        Player target = Bukkit.getPlayer(targetUuid);
        if (target != null && target.isOnline()) {
            target.sendMessage(ColorUtils.toComponent(getMessage(
                    "UNFROZEN",
                    "&aYou are no longer frozen."
            )));
        }

        if (shouldLogUsage()) {
            String actorName = actor instanceof Player player ? player.getName() : "Console";
            plugin.getLogger().info("Freeze disabled: target=" + removed.getTargetNameSnapshot() + " actor=" + actorName);
        }

        return new FreezeToggleResult(false, removed);
    }

    public void handleJoin(Player player) {
        if (player == null || !isFrozen(player.getUniqueId())) {
            return;
        }

        FreezeState state = getActiveState(player.getUniqueId());
        if (state != null && !player.getName().equals(state.getTargetNameSnapshot())) {
            FreezeState refreshed = new FreezeState(
                    state.getTargetUuid(),
                    player.getName(),
                    state.getFrozenByUuid(),
                    state.getFrozenByNameSnapshot(),
                    state.getFrozenAt(),
                    state.getSourceServer()
            );
            activeStates.put(player.getUniqueId(), refreshed);
            plugin.getDatabaseManager().saveFreezeState(refreshed);
        }

        createOrUpdateSession(player, player.getLocation());
        sendAlert(player, true);
    }

    public void handleQuit(Player player) {
        if (player == null || !isFrozen(player.getUniqueId())) {
            runtimeSessions.remove(player == null ? null : player.getUniqueId());
            return;
        }

        FreezeState state = getActiveState(player.getUniqueId());
        runtimeSessions.remove(player.getUniqueId());

        if (state != null) {
            broadcastStaffMessage(formatText(
                    getConfig().getString("FREEZE.QUIT_MESSAGE", "&c[Freeze] &4%player% &cleft while frozen on &4%server%"),
                    state,
                    null
            ));
        }

        if (!shouldPersistOnQuit()) {
            activeStates.remove(player.getUniqueId());
            plugin.getDatabaseManager().deleteFreezeState(player.getUniqueId());
        }
    }

    public void handleRespawn(Player player) {
        if (player == null || !isFrozen(player.getUniqueId())) {
            return;
        }

        plugin.getPaperScheduler().runEntity(player, () -> {
            updateAnchor(player, player.getLocation());
            sendAlert(player, true);
        });
    }

    public void updateAnchor(Player player, Location location) {
        if (player == null || location == null || !isFrozen(player.getUniqueId())) {
            return;
        }

        createOrUpdateSession(player, location);
    }

    public Location getAnchor(Player player) {
        if (player == null) {
            return null;
        }

        FreezeSession session = runtimeSessions.get(player.getUniqueId());
        Location anchor = session == null ? null : session.getAnchorLocation();
        return anchor == null ? player.getLocation().clone() : anchor;
    }

    public boolean sendAlert(Player player, boolean force) {
        if (player == null || !isFrozen(player.getUniqueId())) {
            return false;
        }

        FreezeSession session = createOrUpdateSession(player, player.getLocation());
        long cooldownMillis = getAlertCooldownMillis();
        long now = System.currentTimeMillis();
        if (!force && !session.shouldSendAlert(now, cooldownMillis)) {
            return false;
        }

        session.markAlertSent(now);
        session.markReminderSent(now);
        FreezeState state = getActiveState(player.getUniqueId());
        List<String> lines = getConfig().getStringList("FREEZE.ALERT");
        if (lines.isEmpty()) {
            lines = List.of(
                    "",
                    "&c&lYou're currently frozen!",
                    "&7- You cannot move or interact",
                    ""
            );
        }

        for (String line : lines) {
            player.sendMessage(ColorUtils.toComponent(formatText(line, state, null), player));
        }
        return true;
    }

    public boolean sendStillFrozenReminder(Player player) {
        if (player == null || !isFrozen(player.getUniqueId())) {
            return false;
        }

        FreezeSession session = createOrUpdateSession(player, player.getLocation());
        long cooldownMillis = getAlertCooldownMillis();
        long now = System.currentTimeMillis();
        if (!session.shouldSendReminder(now, cooldownMillis)) {
            return false;
        }

        session.markReminderSent(now);
        player.sendMessage(ColorUtils.toComponent(getMessage(
                "STILL-FROZEN",
                "&cYou are still frozen. Wait for staff instructions."
        )));
        return true;
    }

    public void sendBlockedCommandMessage(Player player) {
        if (player == null) {
            return;
        }

        player.sendMessage(ColorUtils.toComponent(getMessage(
                "COMMAND-BLOCKED",
                "&cYou cannot use commands while frozen."
        )));
    }

    public boolean isAllowedCommand(String rawCommand) {
        String normalized = normalizeCommand(rawCommand);
        if (normalized.isEmpty()) {
            return true;
        }

        for (String allowed : getConfig().getStringList("FREEZE.ALLOWED-COMMANDS")) {
            if (normalized.equals(normalizeCommand(allowed))) {
                return true;
            }
        }
        return false;
    }

    public String buildToggleMessage(FreezeToggleResult result) {
        if (result == null || result.state() == null) {
            return "";
        }

        String status = getConfig().getString(
                result.active() ? "FREEZE.STATUS_ON" : "FREEZE.STATUS_OFF",
                result.active() ? "&a&lON" : "&c&lOFF"
        );
        String template = getConfig().getString(
                "FREEZE.MESSAGE",
                "&bFreeze &a%player% &7is now %status%"
        );
        return formatText(template, result.state(), status);
    }

    public String getMessage(String path, String fallback) {
        return getConfig().getString("MESSAGES." + path, fallback);
    }

    private FreezeSession createOrUpdateSession(Player player, Location anchorLocation) {
        return runtimeSessions.compute(player.getUniqueId(), (ignored, existing) -> {
            if (existing == null) {
                return new FreezeSession(player.getUniqueId(), anchorLocation);
            }
            existing.setAnchorLocation(anchorLocation);
            return existing;
        });
    }

    private void broadcastStaffMessage(String message) {
        if (message == null || message.isBlank()) {
            return;
        }

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.hasPermission(getAlertPermission())) {
                online.sendMessage(ColorUtils.toComponent(message, online));
            }
        }
    }

    private long getAlertCooldownMillis() {
        return Math.max(0L, getConfig().getLong("FREEZE.ALERT-INTERVAL-TICKS", 100L)) * 50L;
    }

    private String formatText(String template, FreezeState state, String status) {
        String targetName = state == null ? "" : state.getTargetNameSnapshot();
        String serverName = state == null ? getServerName() : state.getSourceServer();
        String formattedStatus = status == null ? "" : status;
        return template
                .replace("%player%", targetName)
                .replace("%status%", formattedStatus)
                .replace("%server%", serverName)
                .replace("{player}", targetName)
                .replace("{status}", formattedStatus)
                .replace("{server}", serverName);
    }

    private String getServerName() {
        String configured = getConfig().getString("FREEZE.SERVER-NAME", "local");
        return configured == null || configured.isBlank() ? "local" : configured;
    }

    private String normalizeCommand(String rawCommand) {
        if (rawCommand == null || rawCommand.isBlank()) {
            return "";
        }

        String token = rawCommand.trim().split("\\s+")[0].toLowerCase(Locale.ROOT);
        if (!token.startsWith("/")) {
            token = "/" + token;
        }

        int separator = token.indexOf(':');
        if (separator >= 0 && separator + 1 < token.length()) {
            token = "/" + token.substring(separator + 1);
        }
        return token;
    }

    private FileConfiguration getConfig() {
        return plugin.getConfigManager().getFreeze();
    }

    public record FreezeToggleResult(boolean active, FreezeState state) {
    }
}
