package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PortalDefinition;
import com.bx.ultimateDonutSmp.models.ServerStatusSnapshot;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

public class PortalManager {

    private static final Pattern VALID_ID_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{1,32}$");
    private static final String DESTINATION_TYPE_RTP = "RTP";
    private static final String HOLOGRAM_TAG = "uds_portal_hologram";

    private final UltimateDonutSmp plugin;
    private final Map<UUID, Long> entryDebounceUntil = new LinkedHashMap<>();
    private final Map<UUID, Long> postTeleportGraceUntil = new LinkedHashMap<>();
    private final Map<String, List<UUID>> portalHolograms = new LinkedHashMap<>();
    private Map<String, PortalDefinition> portals = new LinkedHashMap<>();
    private ScheduledTask hologramTask;

    public PortalManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public void loadAll() {
        Map<String, DatabaseManager.PortalData> loaded = plugin.getDatabaseManager().loadPortals();
        Map<String, PortalDefinition> rebuilt = new LinkedHashMap<>();

        loaded.forEach((id, data) -> {
            String normalizedId = normalizeId(id);
            if (normalizedId == null) {
                warn("Ignoring portal with invalid id '" + id + "'.");
                return;
            }

            PortalDefinition portal = new PortalDefinition(
                    normalizedId,
                    data.displayName(),
                    data.cuboidName(),
                    data.destinationType(),
                    data.destinationValue(),
                    data.enabled(),
                    data.permission(),
                    data.priority(),
                    data.triggerCooldownMs(),
                    data.enterMessage(),
                    data.hologramWorld(),
                    data.hologramX(),
                    data.hologramY(),
                    data.hologramZ()
            );
            rebuilt.put(normalizedId, portal);
        });

        portals = rebuilt;
        entryDebounceUntil.clear();
        postTeleportGraceUntil.clear();
        warnAboutInvalidPortals();
        reloadHolograms();
    }

    public List<PortalDefinition> getPortals() {
        List<PortalDefinition> list = new ArrayList<>(portals.values());
        list.sort(Comparator.comparing(PortalDefinition::id, String.CASE_INSENSITIVE_ORDER));
        return List.copyOf(list);
    }

    public List<String> getSortedPortalIds() {
        return getPortals().stream().map(PortalDefinition::id).toList();
    }

    public PortalDefinition getPortal(String id) {
        String normalized = normalizeId(id);
        return normalized == null ? null : portals.get(normalized);
    }

    public boolean portalExists(String id) {
        return getPortal(id) != null;
    }

    public boolean createPortal(String id, String cuboidName, String rtpSelector) {
        String normalizedId = normalizeId(id);
        if (normalizedId == null || !isValidPortalId(id) || portals.containsKey(normalizedId)) {
            return false;
        }
        if (!plugin.getCuboidManager().exists(cuboidName) || !plugin.getRtpManager().isPortalDestinationAvailable(rtpSelector)) {
            return false;
        }

        PortalDefinition portal = new PortalDefinition(
                normalizedId,
                normalizedId,
                cuboidName,
                DESTINATION_TYPE_RTP,
                rtpSelector,
                true,
                "",
                0,
                getDefaultTriggerCooldownMillis(),
                "",
                "",
                0D,
                0D,
                0D
        );
        portals.put(normalizedId, portal);
        savePortal(portal);
        reloadHolograms();
        return true;
    }

    public boolean deletePortal(String id) {
        String normalized = normalizeId(id);
        if (normalized == null || portals.remove(normalized) == null) {
            return false;
        }
        plugin.getDatabaseManager().deletePortal(normalized);
        removePortalHologram(normalized);
        return true;
    }

    public boolean setPortalCuboid(String id, String cuboidName) {
        PortalDefinition portal = getPortal(id);
        if (portal == null || !plugin.getCuboidManager().exists(cuboidName)) {
            return false;
        }

        PortalDefinition updated = portal.withCuboidName(cuboidName);
        portals.put(updated.id(), updated);
        savePortal(updated);
        reloadHolograms();
        return true;
    }

    public boolean setPortalDestination(String id, String rtpSelector) {
        PortalDefinition portal = getPortal(id);
        if (portal == null || !plugin.getRtpManager().isPortalDestinationAvailable(rtpSelector)) {
            return false;
        }

        PortalDefinition updated = portal.withDestination(DESTINATION_TYPE_RTP, rtpSelector);
        portals.put(updated.id(), updated);
        savePortal(updated);
        reloadHolograms();
        return true;
    }

    public boolean setPortalDisplayName(String id, String displayName) {
        PortalDefinition portal = getPortal(id);
        if (portal == null) {
            return false;
        }

        PortalDefinition updated = portal.withDisplayName(displayName);
        portals.put(updated.id(), updated);
        savePortal(updated);
        removePortalHologram(updated.id());
        updatePortalHologram(updated);
        return true;
    }

    public boolean setPortalEnabled(String id, boolean enabled) {
        PortalDefinition portal = getPortal(id);
        if (portal == null) {
            return false;
        }

        PortalDefinition updated = portal.withEnabled(enabled);
        portals.put(updated.id(), updated);
        savePortal(updated);
        reloadHolograms();
        return true;
    }

    public boolean setPortalPriority(String id, int priority) {
        PortalDefinition portal = getPortal(id);
        if (portal == null) {
            return false;
        }

        PortalDefinition updated = portal.withPriority(priority);
        portals.put(updated.id(), updated);
        savePortal(updated);
        updatePortalHologram(updated);
        return true;
    }

    public boolean setPortalHologramLocation(String id, Location location) {
        PortalDefinition portal = getPortal(id);
        if (portal == null || location == null || location.getWorld() == null) {
            return false;
        }

        PortalDefinition updated = portal.withHologramLocation(
                location.getWorld().getName(),
                location.getX(),
                location.getY(),
                location.getZ()
        );
        portals.put(updated.id(), updated);
        savePortal(updated);
        updatePortalHologram(updated);
        return true;
    }

    public PortalDefinition resolvePortalAt(Location location) {
        if (!isSystemEnabled() || location == null || location.getWorld() == null) {
            return null;
        }

        PortalDefinition bestMatch = null;
        for (PortalDefinition portal : portals.values()) {
            if (!portal.enabled() || !hasValidCuboid(portal)) {
                continue;
            }

            CuboidManager.Cuboid cuboid = plugin.getCuboidManager().getCuboid(portal.cuboidName());
            if (cuboid == null || !cuboid.contains(location)) {
                continue;
            }

            if (bestMatch == null
                    || portal.priority() > bestMatch.priority()
                    || (portal.priority() == bestMatch.priority()
                    && portal.id().compareToIgnoreCase(bestMatch.id()) < 0)) {
                bestMatch = portal;
            }
        }

        return bestMatch;
    }

    public void handlePlayerMovement(Player player, Location from, Location to, boolean teleported) {
        if (player == null || to == null) {
            return;
        }

        UUID playerId = player.getUniqueId();
        long now = System.currentTimeMillis();

        if (teleported) {
            postTeleportGraceUntil.put(playerId, now + getPostTeleportGraceMillis());
        }

        clearExpiredState(playerId, now);
        if (!isSystemEnabled() || portals.isEmpty()) {
            return;
        }

        PortalDefinition toPortal = resolvePortalAt(to);
        if (toPortal == null) {
            return;
        }

        PortalDefinition fromPortal = resolvePortalAt(from);
        if (fromPortal != null && fromPortal.id().equalsIgnoreCase(toPortal.id())) {
            return;
        }

        if (isWithinGrace(playerId, now)) {
            return;
        }

        handlePortalEntry(player, toPortal);
    }

    public boolean handlePortalEntry(Player player, PortalDefinition portal) {
        if (player == null || portal == null) {
            return false;
        }

        UUID playerId = player.getUniqueId();
        long now = System.currentTimeMillis();
        applyEntryDebounce(playerId, portal, now);

        if (!portal.enabled()) {
            return false;
        }

        if (!hasValidCuboid(portal)) {
            player.sendMessage(ColorUtils.toComponent(message("PORTAL.INVALID-CUBOID",
                    "&cThis portal is not configured correctly right now.")));
            return false;
        }

        if (!isDestinationUsable(portal)) {
            player.sendMessage(ColorUtils.toComponent(message("PORTAL.INVALID-DESTINATION",
                    "&cThis portal destination is currently unavailable.")));
            return false;
        }

        if (!portal.permission().isBlank() && !player.hasPermission(portal.permission())) {
            player.sendMessage(ColorUtils.toComponent(message("PORTAL.NO-PERMISSION",
                    "&cYou do not have permission to use this portal.")));
            return false;
        }

        if (shouldBlockInCombat() && plugin.getCombatManager().isInCombat(playerId)) {
            player.sendMessage(ColorUtils.toComponent(message("PORTAL.IN-COMBAT",
                    "&cYou cannot use portals while in combat.")));
            return false;
        }

        if (plugin.getTeleportManager().hasPending(playerId)
                && !plugin.getTeleportManager().hasPendingType(playerId, "RTP")) {
            player.sendMessage(ColorUtils.toComponent(message("PORTAL.TELEPORT-IN-PROGRESS",
                    "&cYou are already teleporting.")));
            return false;
        }

        boolean queued = plugin.getRtpManager().queueCommandTeleport(player, portal.destinationValue());
        if (!queued) {
            return false;
        }

        String enterMessage = portal.enterMessage().isBlank()
                ? message("PORTAL.ENTERED", "")
                : portal.enterMessage();
        if (!enterMessage.isBlank()) {
            String destinationLabel = describeDestination(portal);
            player.sendMessage(ColorUtils.toComponent(
                    enterMessage
                            .replace("{portal}", portal.effectiveDisplayName())
                            .replace("{destination}", destinationLabel)
            ));
        }

        return true;
    }

    public boolean hasValidCuboid(PortalDefinition portal) {
        return portal != null && plugin.getCuboidManager().exists(portal.cuboidName());
    }

    public boolean isDestinationUsable(PortalDefinition portal) {
        if (portal == null || !DESTINATION_TYPE_RTP.equalsIgnoreCase(portal.destinationType())) {
            return false;
        }
        return plugin.getRtpManager().isPortalDestinationAvailable(portal.destinationValue());
    }

    public String resolveDestinationWorld(PortalDefinition portal) {
        if (portal == null || !DESTINATION_TYPE_RTP.equalsIgnoreCase(portal.destinationType())) {
            return null;
        }
        return plugin.getRtpManager().resolveWorldSelector(portal.destinationValue());
    }

    public String describeDestination(PortalDefinition portal) {
        if (portal == null) {
            return "Unknown";
        }

        if (!DESTINATION_TYPE_RTP.equalsIgnoreCase(portal.destinationType())) {
            return portal.destinationType() + ":" + portal.destinationValue();
        }

        String worldName = resolveDestinationWorld(portal);
        if (worldName == null || worldName.isBlank()) {
            return "RTP:" + portal.destinationValue();
        }

        return plugin.getRtpManager().describeWorld(worldName) + " [" + portal.destinationValue() + "]";
    }

    public String getPortalStateKey(PortalDefinition portal) {
        if (portal == null) {
            return "UNKNOWN";
        }
        if (!portal.enabled()) {
            return "DISABLED";
        }
        if (!hasValidCuboid(portal)) {
            return "INVALID_CUBOID";
        }
        if (!isDestinationUsable(portal)) {
            return "INVALID_DESTINATION";
        }
        return "READY";
    }

    public boolean isValidPortalId(String id) {
        if (id == null) {
            return false;
        }
        return VALID_ID_PATTERN.matcher(id.trim()).matches();
    }

    public String normalizeId(String id) {
        if (id == null) {
            return null;
        }

        String trimmed = id.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        return trimmed.toLowerCase(Locale.ROOT);
    }

    public boolean isSystemEnabled() {
        return plugin.getConfigManager().getConfig().getBoolean("PORTAL-SYSTEM.ENABLED", true);
    }

    public void clearPlayerState(UUID playerId) {
        entryDebounceUntil.remove(playerId);
        postTeleportGraceUntil.remove(playerId);
    }

    public void refreshHologramsSoon() {
        if (!isSystemEnabled() || !isHologramEnabled()) {
            return;
        }

        if (plugin.getNetworkStatusManager() != null && plugin.getNetworkStatusManager().isEnabled()) {
            plugin.getNetworkStatusManager().requestImmediateRefreshAll();
        }

        for (long delayTicks : new long[]{1L, 20L, 60L, 100L, 200L}) {
            scheduleHologramRefresh(delayTicks);
        }
    }

    private void scheduleHologramRefresh(long delayTicks) {
        plugin.getPaperScheduler().runGlobalLater(this::ensureAllPortalHolograms, delayTicks);
    }

    public void shutdown() {
        if (hologramTask != null) {
            hologramTask.cancel();
            hologramTask = null;
        }
        clearPortalHolograms();
        purgeAllPortalHologramsInWorlds();
        entryDebounceUntil.clear();
        postTeleportGraceUntil.clear();
    }

    private void reloadHolograms() {
        if (hologramTask != null) {
            hologramTask.cancel();
            hologramTask = null;
        }

        clearPortalHolograms();
        purgeAllPortalHologramsInWorlds();

        if (!isSystemEnabled() || !isHologramEnabled()) {
            return;
        }

        ensureAllPortalHolograms();
        hologramTask = plugin.getPaperScheduler().runGlobalTimer(
                this::ensureAllPortalHolograms,
                getHologramUpdateTicks(),
                getHologramUpdateTicks()
        );
        refreshHologramsSoon();
    }

    private void ensureAllPortalHolograms() {
        if (!isSystemEnabled() || !isHologramEnabled()) {
            clearPortalHolograms();
            purgeAllPortalHologramsInWorlds();
            return;
        }

        Set<String> activeIds = new HashSet<>();
        for (PortalDefinition portal : portals.values()) {
            if (!shouldShowHologram(portal)) {
                removePortalHologram(portal.id());
                continue;
            }

            activeIds.add(portal.id());
            updatePortalHologram(portal);
        }

        for (String portalId : new HashSet<>(portalHolograms.keySet())) {
            if (!activeIds.contains(portalId)) {
                removePortalHologram(portalId);
            }
        }
    }

    private void updatePortalHologram(PortalDefinition portal) {
        if (!shouldShowHologram(portal)) {
            if (portal != null) {
                removePortalHologram(portal.id());
            }
            return;
        }

        List<String> lines = getHologramLines(portal);
        if (lines.isEmpty()) {
            removePortalHologram(portal.id());
            return;
        }

        Location baseLocation = getHologramLocation(portal);
        if (baseLocation == null || baseLocation.getWorld() == null) {
            removePortalHologram(portal.id());
            return;
        }

        removeLoadedPortalHologramOrphans(portal.id(), baseLocation, lines.size());

        if (!hasValidPortalHologram(portal.id(), lines.size(), baseLocation)) {
            spawnPortalHologram(portal, lines);
            return;
        }

        List<UUID> entityIds = portalHolograms.get(portal.id());
        for (int i = 0; i < entityIds.size(); i++) {
            Entity entity = Bukkit.getEntity(entityIds.get(i));
            if (entity instanceof TextDisplay display && entity.isValid()) {
                display.text(ColorUtils.toComponent(lines.get(i)));
            }
        }
    }

    private void spawnPortalHologram(PortalDefinition portal, List<String> lines) {
        Location baseLocation = getHologramLocation(portal);
        if (baseLocation == null || baseLocation.getWorld() == null) {
            removePortalHologram(portal.id());
            return;
        }

        removePortalHologram(portal.id());

        List<UUID> entityIds = new ArrayList<>();
        double lineSpacing = getHologramLineSpacing();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            Location lineLocation = baseLocation.clone().subtract(0D, i * lineSpacing, 0D);
            TextDisplay display = baseLocation.getWorld().spawn(lineLocation, TextDisplay.class, textDisplay -> {
                textDisplay.text(ColorUtils.toComponent(line));
                configureHologramDisplay(textDisplay);
                textDisplay.addScoreboardTag(HOLOGRAM_TAG);
                textDisplay.getPersistentDataContainer().set(
                        plugin.getKey("portal_hologram"),
                        PersistentDataType.STRING,
                        portal.id()
                );
            });
            entityIds.add(display.getUniqueId());
        }

        portalHolograms.put(portal.id(), entityIds);
    }

    private boolean hasValidPortalHologram(String portalId, int requiredLines, Location baseLocation) {
        List<UUID> trackedIds = portalHolograms.get(portalId);
        if (trackedIds == null || trackedIds.size() != requiredLines) {
            return false;
        }

        double lineSpacing = getHologramLineSpacing();
        for (int i = 0; i < trackedIds.size(); i++) {
            UUID entityId = trackedIds.get(i);
            Entity entity = Bukkit.getEntity(entityId);
            if (!(entity instanceof TextDisplay)
                    || !entity.isValid()
                    || !entity.getScoreboardTags().contains(HOLOGRAM_TAG)) {
                return false;
            }

            if (entity.getWorld() == null
                    || baseLocation.getWorld() == null
                    || !entity.getWorld().getName().equalsIgnoreCase(baseLocation.getWorld().getName())) {
                return false;
            }

            Location expected = baseLocation.clone().subtract(0D, i * lineSpacing, 0D);
            if (entity.getLocation().distanceSquared(expected) > 0.01D) {
                return false;
            }
        }

        return true;
    }

    private boolean shouldShowHologram(PortalDefinition portal) {
        return portal != null
                && isSystemEnabled()
                && isHologramEnabled()
                && portal.enabled()
                && hasValidCuboid(portal)
                && isDestinationUsable(portal);
    }

    private Location getHologramLocation(PortalDefinition portal) {
        if (portal.hasCustomHologramLocation()) {
            World hologramWorld = Bukkit.getWorld(portal.hologramWorld());
            if (hologramWorld != null) {
                return new Location(
                        hologramWorld,
                        portal.hologramX(),
                        portal.hologramY(),
                        portal.hologramZ()
                );
            }
        }

        CuboidManager.Cuboid cuboid = plugin.getCuboidManager().getCuboid(portal.cuboidName());
        if (cuboid == null) {
            return null;
        }

        World world = Bukkit.getWorld(cuboid.world());
        if (world == null) {
            return null;
        }

        int minX = Math.min(cuboid.x1(), cuboid.x2());
        int maxX = Math.max(cuboid.x1(), cuboid.x2());
        int maxY = Math.max(cuboid.y1(), cuboid.y2());
        int minZ = Math.min(cuboid.z1(), cuboid.z2());
        int maxZ = Math.max(cuboid.z1(), cuboid.z2());

        double x = (minX + maxX + 1) / 2.0D;
        double y = maxY + getHologramOffsetY();
        double z = (minZ + maxZ + 1) / 2.0D;
        return new Location(world, x, y, z);
    }

    private List<String> getHologramLines(PortalDefinition portal) {
        List<String> configured = plugin.getConfigManager().getConfig()
                .getStringList("PORTAL-SYSTEM.HOLOGRAM.PORTALS." + portal.id() + ".LINES");
        if (configured.isEmpty()) {
            configured = plugin.getConfigManager().getConfig()
                    .getStringList("PORTAL-SYSTEM.HOLOGRAM.LINES");
        }
        if (configured.isEmpty()) {
            configured = List.of(
                    "&f{portal}",
                    "&7Region {region}",
                    "",
                    "&f<total_player> Players"
            );
        }

        List<String> resolved = new ArrayList<>();
        for (String line : configured) {
            resolved.add(resolveHologramLine(line, portal));
        }
        return resolved;
    }

    private String resolveHologramLine(String line, PortalDefinition portal) {
        String worldName = resolveDestinationWorld(portal);
        String worldLabel = worldName == null ? "" : plugin.getRtpManager().describeWorld(worldName);
        String worldPlayers = worldName == null
                ? "0"
                : String.valueOf(plugin.getRtpManager().getPlayersInWorld(worldName));
        String serverId = getHologramServerId(portal);
        ServerStatusSnapshot serverSnapshot = getHologramServerSnapshot(serverId);
        boolean localServer = shouldUseLocalHologramPlayers(serverId);
        String totalPlayers = String.valueOf(localServer
                ? Bukkit.getOnlinePlayers().size()
                : serverSnapshot == null ? 0 : serverSnapshot.playerCount());
        String maxPlayers = String.valueOf(Bukkit.getMaxPlayers());
        String region = getHologramRegion(portal);
        String serverDisplay = localServer
                ? getLocalServerDisplayName()
                : serverSnapshot == null ? serverId : serverSnapshot.displayName();
        String serverStatus = localServer
                ? "online"
                : serverSnapshot != null && serverSnapshot.online() ? "online" : "offline";

        return line
                .replace("{id}", portal.id())
                .replace("{portal}", portal.effectiveDisplayName())
                .replace("{display}", portal.effectiveDisplayName())
                .replace("{destination}", describeDestination(portal))
                .replace("{selector}", portal.destinationValue())
                .replace("{world}", worldLabel)
                .replace("{world_name}", worldName == null ? "" : worldName)
                .replace("{region}", region)
                .replace("{server}", serverDisplay)
                .replace("{server_id}", serverId)
                .replace("{server_status}", serverStatus)
                .replace("{players}", worldPlayers)
                .replace("<players>", worldPlayers)
                .replace("{world_players}", worldPlayers)
                .replace("<world_players>", worldPlayers)
                .replace("{total_player}", totalPlayers)
                .replace("<total_player>", totalPlayers)
                .replace("{total_players}", totalPlayers)
                .replace("<total_players>", totalPlayers)
                .replace("{online}", totalPlayers)
                .replace("<online>", totalPlayers)
                .replace("{max_players}", maxPlayers)
                .replace("<max_players>", maxPlayers);
    }

    private String getHologramRegion(PortalDefinition portal) {
        String perPortalPath = "PORTAL-SYSTEM.HOLOGRAM.PORTALS." + portal.id() + ".REGION";
        String region = plugin.getConfigManager().getConfig().getString(perPortalPath, "").trim();
        if (!region.isBlank()) {
            return region;
        }

        region = plugin.getConfigManager().getConfig()
                .getString("PORTAL-SYSTEM.HOLOGRAM.DEFAULT-REGION", "").trim();
        if (!region.isBlank()) {
            return region;
        }

        return plugin.getConfigManager().getConfig()
                .getString("PORTAL-SYSTEM.HOLOGRAM.REGION", "NA East").trim();
    }

    private String getHologramServerId(PortalDefinition portal) {
        String perPortalPath = "PORTAL-SYSTEM.HOLOGRAM.PORTALS." + portal.id() + ".SERVER-ID";
        String serverId = plugin.getConfigManager().getConfig().getString(perPortalPath, "").trim();
        if (!serverId.isBlank()) {
            return serverId.toLowerCase(Locale.ROOT);
        }

        serverId = plugin.getConfigManager().getConfig()
                .getString("PORTAL-SYSTEM.HOLOGRAM.DEFAULT-SERVER-ID", "").trim();
        return serverId.toLowerCase(Locale.ROOT);
    }

    private ServerStatusSnapshot getHologramServerSnapshot(String serverId) {
        if (serverId == null || serverId.isBlank()) {
            return null;
        }
        if (shouldUseLocalHologramPlayers(serverId)) {
            return null;
        }
        if (plugin.getNetworkStatusManager() == null || !plugin.getNetworkStatusManager().isEnabled()) {
            return null;
        }
        return plugin.getNetworkStatusManager().getSnapshot(serverId);
    }

    private boolean shouldUseLocalHologramPlayers(String serverId) {
        if (isLocalHologramServerId(serverId)) {
            return true;
        }
        if (plugin.getNetworkStatusManager() == null || !plugin.getNetworkStatusManager().isEnabled()) {
            return true;
        }
        return !plugin.getNetworkStatusManager().isKnownServer(serverId);
    }

    private boolean isLocalHologramServerId(String serverId) {
        if (serverId == null || serverId.isBlank()) {
            return true;
        }

        String normalized = serverId.trim().toLowerCase(Locale.ROOT);
        if (normalized.equals(getNetworkString("NETWORK-STATUS.LOCAL-SERVER-ID", "local").toLowerCase(Locale.ROOT))) {
            return true;
        }
        if (normalized.equals(getNetworkString("NETWORK.LOCAL_SERVER_ID", "local").toLowerCase(Locale.ROOT))) {
            return true;
        }

        String sourceType = getNetworkString("NETWORK-STATUS.SERVERS." + normalized + ".SOURCE.TYPE", "");
        return sourceType.equalsIgnoreCase("LOCAL");
    }

    private String getLocalServerDisplayName() {
        String display = getNetworkString("NETWORK-STATUS.LOCAL-DISPLAY-NAME", "").trim();
        if (!display.isBlank()) {
            return display;
        }

        display = getNetworkString("NETWORK.LOCAL_DISPLAY_NAME", "").trim();
        return display.isBlank() ? "local" : display;
    }

    private String getNetworkString(String path, String fallback) {
        if (plugin.getConfigManager().getNetwork() == null) {
            return fallback;
        }
        return plugin.getConfigManager().getNetwork().getString(path, fallback);
    }

    private void configureHologramDisplay(TextDisplay textDisplay) {
        textDisplay.setBillboard(Display.Billboard.CENTER);
        textDisplay.setDefaultBackground(false);
        textDisplay.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
        textDisplay.setSeeThrough(true);
        textDisplay.setShadowed(false);
        textDisplay.setViewRange(1000.0F);
        textDisplay.setPersistent(false);
    }

    private void removeLoadedPortalHologramOrphans(String portalId, Location baseLocation, int lineCount) {
        List<UUID> trackedIds = portalHolograms.get(portalId);
        Set<UUID> tracked = trackedIds == null ? Set.of() : new HashSet<>(trackedIds);

        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntitiesByClass(TextDisplay.class)) {
                if (!isManagedPortalHologram(entity) || tracked.contains(entity.getUniqueId())) {
                    continue;
                }

                if (isAttachedToPortal(entity, portalId) || isNearHologramLocation(entity, baseLocation, lineCount)) {
                    entity.remove();
                }
            }
        }
    }

    private boolean isAttachedToPortal(Entity entity, String portalId) {
        String attachedPortalId = entity.getPersistentDataContainer()
                .get(plugin.getKey("portal_hologram"), PersistentDataType.STRING);
        return attachedPortalId != null && attachedPortalId.equalsIgnoreCase(portalId);
    }

    private boolean isNearHologramLocation(Entity entity, Location baseLocation, int lineCount) {
        if (baseLocation == null
                || baseLocation.getWorld() == null
                || entity.getWorld() == null
                || !entity.getWorld().getName().equalsIgnoreCase(baseLocation.getWorld().getName())) {
            return false;
        }

        Location entityLocation = entity.getLocation();
        double maxVerticalDistance = Math.max(1.0D, lineCount * getHologramLineSpacing() + 0.75D);
        return Math.abs(entityLocation.getX() - baseLocation.getX()) <= 1.0D
                && Math.abs(entityLocation.getZ() - baseLocation.getZ()) <= 1.0D
                && Math.abs(entityLocation.getY() - baseLocation.getY()) <= maxVerticalDistance;
    }

    private void removePortalHologram(String portalId) {
        removeTrackedPortalHologram(portalId);

        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntitiesByClass(TextDisplay.class)) {
                if (!isManagedPortalHologram(entity)) {
                    continue;
                }

                if (isAttachedToPortal(entity, portalId)) {
                    entity.remove();
                }
            }
        }
    }

    private void removeTrackedPortalHologram(String portalId) {
        List<UUID> entityIds = portalHolograms.remove(portalId);
        if (entityIds == null) {
            return;
        }

        for (UUID entityId : entityIds) {
            Entity entity = Bukkit.getEntity(entityId);
            if (entity != null && entity.isValid()) {
                entity.remove();
            }
        }
    }

    private void clearPortalHolograms() {
        for (String portalId : new HashSet<>(portalHolograms.keySet())) {
            removeTrackedPortalHologram(portalId);
        }
        portalHolograms.clear();
    }

    private void purgeAllPortalHologramsInWorlds() {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntitiesByClass(TextDisplay.class)) {
                if (isManagedPortalHologram(entity)) {
                    entity.remove();
                }
            }
        }
    }

    private boolean isManagedPortalHologram(Entity entity) {
        return entity instanceof TextDisplay
                && (entity.getScoreboardTags().contains(HOLOGRAM_TAG)
                || entity.getPersistentDataContainer().has(plugin.getKey("portal_hologram"), PersistentDataType.STRING));
    }

    private boolean isHologramEnabled() {
        return plugin.getConfigManager().getConfig().getBoolean("PORTAL-SYSTEM.HOLOGRAM.ENABLED", true);
    }

    private double getHologramOffsetY() {
        return plugin.getConfigManager().getConfig().getDouble("PORTAL-SYSTEM.HOLOGRAM.OFFSET-Y", 1.2D);
    }

    private double getHologramLineSpacing() {
        return Math.max(0.05D, plugin.getConfigManager().getConfig()
                .getDouble("PORTAL-SYSTEM.HOLOGRAM.LINE-SPACING", 0.27D));
    }

    private long getHologramUpdateTicks() {
        return Math.max(1L, plugin.getConfigManager().getConfig()
                .getLong("PORTAL-SYSTEM.HOLOGRAM.UPDATE-TICKS", 40L));
    }

    private boolean isWithinGrace(UUID playerId, long now) {
        Long entryDebounce = entryDebounceUntil.get(playerId);
        if (entryDebounce != null && entryDebounce > now) {
            return true;
        }

        Long teleportGrace = postTeleportGraceUntil.get(playerId);
        return teleportGrace != null && teleportGrace > now;
    }

    private void clearExpiredState(UUID playerId, long now) {
        Long entryDebounce = entryDebounceUntil.get(playerId);
        if (entryDebounce != null && entryDebounce <= now) {
            entryDebounceUntil.remove(playerId);
        }

        Long teleportGrace = postTeleportGraceUntil.get(playerId);
        if (teleportGrace != null && teleportGrace <= now) {
            postTeleportGraceUntil.remove(playerId);
        }
    }

    private void applyEntryDebounce(UUID playerId, PortalDefinition portal, long now) {
        long cooldown = Math.max(0L, portal.triggerCooldownMillis());
        if (cooldown <= 0L) {
            entryDebounceUntil.remove(playerId);
            return;
        }
        entryDebounceUntil.put(playerId, now + cooldown);
    }

    private long getDefaultTriggerCooldownMillis() {
        return Math.max(0L, plugin.getConfigManager().getConfig()
                .getLong("PORTAL-SYSTEM.DEFAULT-TRIGGER-COOLDOWN-MS", 1500L));
    }

    private long getPostTeleportGraceMillis() {
        return Math.max(0L, plugin.getConfigManager().getConfig()
                .getLong("PORTAL-SYSTEM.POST-TELEPORT-GRACE-MS", 2000L));
    }

    private boolean shouldBlockInCombat() {
        return plugin.getConfigManager().getConfig().getBoolean("PORTAL-SYSTEM.BLOCK-IN-COMBAT", true);
    }

    private void savePortal(PortalDefinition portal) {
        plugin.getDatabaseManager().savePortal(
                portal.id(),
                portal.displayName(),
                portal.cuboidName(),
                portal.destinationType(),
                portal.destinationValue(),
                portal.enabled(),
                portal.permission(),
                portal.priority(),
                portal.triggerCooldownMillis(),
                portal.enterMessage(),
                portal.hologramWorld(),
                portal.hologramX(),
                portal.hologramY(),
                portal.hologramZ()
        );
    }

    private void warnAboutInvalidPortals() {
        for (PortalDefinition portal : portals.values()) {
            if (!hasValidCuboid(portal)) {
                warn("Portal '" + portal.id() + "' references missing cuboid '" + portal.cuboidName() + "'.");
            }
            if (!isDestinationUsable(portal)) {
                warn("Portal '" + portal.id() + "' has unavailable destination '" + portal.destinationValue() + "'.");
            }
        }
    }

    private String message(String path, String fallback) {
        return plugin.getConfigManager().getMessageOrDefault(path, fallback);
    }

    private void warn(String message) {
        plugin.getLogger().warning("[PortalManager] " + message);
    }
}
