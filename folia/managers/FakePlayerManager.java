package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.PermissionUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FakePlayerManager {

    public static final String USE_PERMISSION = "ultimatedonutsmp.staff.fakeplayer";
    public static final String ALERT_PERMISSION = "ultimatedonutsmp.staff.fakeplayer.alert";
    public static final String BYPASS_PERMISSION = "ultimatedonutsmp.staff.fakeplayer.bypass";

    private static final int ENTITY_ID_BASE = 2_000_000_000;
    private static final long HIT_RESPONSE_COOLDOWN_MILLIS = 180L;
    private static final Pattern TEXTURE_PROFILE_ID_PATTERN =
            Pattern.compile("\"profileId\"\\s*:\\s*\"([0-9a-fA-F-]{32,36})\"");

    private final UltimateDonutSmp plugin;
    private final AtomicLong nextId = new AtomicLong(1L);
    private final AtomicInteger nextEntityId = new AtomicInteger(ENTITY_ID_BASE + ThreadLocalRandom.current().nextInt(100_000));
    private final Map<Long, FakePlayerSession> activeFakePlayers = new LinkedHashMap<>();

    private FakePlayerPacketBridge packetBridge;
    private ScheduledTask monitorTask;
    private ScheduledTask positionLockTask;
    private boolean enabled;
    private long ttlSeconds;
    private double viewRadius;
    private double detectionRadius;
    private double detectionAngleCos;
    private int blockedCheckThreshold;
    private long checkIntervalTicks;
    private long alertCooldownMillis;
    private long tablistRemoveDelayTicks;
    private boolean logToConsole;
    private boolean hideFromTablist;
    private SkinSourceMode skinSourceMode;
    private boolean lockAirPosition;
    private boolean simulatePhysics;
    private boolean hitResponseEnabled;
    private double physicsGravity;
    private double physicsVerticalDrag;
    private double physicsAirHorizontalDrag;
    private double physicsGroundFriction;
    private double physicsMaxFallSpeed;

    public FakePlayerManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
        this.packetBridge = createBridge();
        reload();
    }

    public synchronized void reload() {
        removeAllInternal("reload");
        loadSettings();
        restartMonitor();
        if (packetBridge == null) {
            packetBridge = createBridge();
        }
    }

    public synchronized void shutdown() {
        cancelMonitor();
        cancelPositionLock();
        removeAllInternal("shutdown");
        if (packetBridge != null) {
            packetBridge.shutdown();
        }
    }

    public boolean isAvailable() {
        return packetBridge != null;
    }

    public boolean isEnabled() {
        return enabled && (plugin.getFeatureManager() == null
                || plugin.getFeatureManager().isEnabled(FeatureManager.Feature.STAFF_MODE));
    }

    public SpawnResult spawn(Player creator) {
        return spawnWithTexture(creator, null);
    }

    private SpawnResult spawnWithTexture(Player creator, TablistManager.SkinTexture skinTexture) {
        return spawnWithTexture(creator, skinTexture, configBoolean("REQUIRE-SKIN-TEXTURE", true));
    }

    private SpawnResult spawnWithTexture(
            Player creator,
            TablistManager.SkinTexture skinTexture,
            boolean requireSkinTexture
    ) {
        if (creator == null) {
            return SpawnResult.fail(message("PLAYER-ONLY", "&cᴏɴʟʏ ᴘʟᴀʏᴇʀѕ ᴄᴀɴ ᴜѕᴇ ᴛʜɪѕ ᴄᴏᴍᴍᴀɴᴅ."));
        }
        if (!isAvailable()) {
            return SpawnResult.fail(message(
                    "DEPENDENCY-MISSING",
                    "&cᴘʀᴏᴛᴏᴄᴏʟʟɪʙ ɪѕ ʀᴇǫᴜɪʀᴇᴅ ꜰᴏʀ /fakeplayer. ɪɴѕᴛᴀʟʟ ᴘʀᴏᴛᴏᴄᴏʟʟɪʙ ᴀɴᴅ ʀᴇѕᴛᴀʀᴛ ᴛʜᴇ ѕᴇʀᴠᴇʀ."
            ));
        }
        if (!isEnabled()) {
            return SpawnResult.fail(message("DISABLED", "&cꜰᴀᴋᴇᴘʟᴀʏᴇʀ ɪѕ ᴄᴜʀʀᴇɴᴛʟʏ ᴅɪѕᴀʙʟᴇᴅ."));
        }

        Location location = creator.getLocation().clone();
        if (location.getWorld() == null) {
            return SpawnResult.fail(message("INVALID-LOCATION", "&cᴜɴᴀʙʟᴇ ᴛᴏ ѕᴘᴀᴡɴ ᴀ ꜰᴀᴋᴇᴘʟᴀʏᴇʀ ᴀᴛ ʏᴏᴜʀ ᴄᴜʀʀᴇɴᴛ ʟᴏᴄᴀᴛɪᴏɴ."));
        }

        UUID textureProfileUuid = extractTextureProfileUuid(skinTexture);
        UUID fakeUuid = resolveFakeUuid(textureProfileUuid);
        String displayName = resolveDisplayName(creator);
        String profileName = resolveProfileName(creator, displayName);
        Object profile = skinTexture != null && skinTexture.isValid()
                ? packetBridge.createProfile(creator, fakeUuid, profileName, skinTexture)
                : packetBridge.createProfile(requireSkinTexture ? creator : null, fakeUuid, profileName);
        if (requireSkinTexture && !packetBridge.hasSkinTexture(profile)) {
            return SpawnResult.fail(message(
                    "SKIN-NOT-READY",
                    "&cѕᴋɪɴ ᴅᴀᴛᴀ ꜰᴏʀ &f{player}&c ɪѕ ɴᴏᴛ ʀᴇᴀᴅʏ. ʀᴇᴀᴘᴘʟʏ ʏᴏᴜʀ ѕᴋɪɴ ᴏʀ ʀᴇᴊᴏɪɴ, ᴛʜᴇɴ ᴛʀʏ ᴀɢᴀɪɴ.",
                    "{player}", creator.getName()
            ));
        }
        if (skinTexture != null && skinTexture.isValid()) {
            logPreparedSkinProfile(creator.getName(), fakeUuid, textureProfileUuid);
        }
        long now = System.currentTimeMillis();
        FakePlayerSession fakePlayer = new FakePlayerSession(
                nextId.getAndIncrement(),
                nextEntityId.getAndIncrement(),
                fakeUuid,
                profile,
                creator.getUniqueId(),
                creator.getName(),
                profileName,
                displayName,
                location,
                now,
                now + Math.max(1L, ttlSeconds) * 1000L
        );

        synchronized (this) {
            activeFakePlayers.put(fakePlayer.id(), fakePlayer);
        }
        updateViewers(fakePlayer);
        return SpawnResult.success(message(
                "SPAWNED",
                "&aꜰᴀᴋᴇᴘʟᴀʏᴇʀ &f{fakeplayer}&a ѕᴘᴀᴡɴᴇᴅ ꜰᴏʀ &f{ttl}ѕ&a.",
                "{fakeplayer}", fakePlayer.displayName(),
                "{ttl}", String.valueOf(ttlSeconds)
        ), fakePlayer);
    }

    public void spawnAsync(Player creator, Consumer<SpawnResult> callback) {
        if (callback == null) {
            return;
        }

        boolean requireSkinTexture = configBoolean("REQUIRE-SKIN-TEXTURE", true);

        if (creator == null || !isAvailable() || !isEnabled()) {
            callback.accept(spawn(creator));
            return;
        }

        UUID creatorId = creator.getUniqueId();
        String creatorName = creator.getName();
        TablistManager tablistManager = plugin.getTablistManager();

        plugin.getFoliaScheduler().runAsync(() -> {
            TablistManager.SkinTexture resolvedTexture = null;
            String resolvedSource = null;
            try {
                if (tablistManager != null && skinSourceMode != SkinSourceMode.GAMEPROFILE) {
                    resolvedTexture = tablistManager.resolveSkinTextureForFakePlayer(creatorId, creatorName);
                    if (resolvedTexture != null && resolvedTexture.isValid()) {
                        resolvedSource = "SkinsRestorer";
                    }
                }
            } catch (RuntimeException | LinkageError error) {
                plugin.getLogger().log(Level.FINE,
                        "Unable to resolve fakeplayer SkinsRestorer skin for " + creatorName + ".", error);
            }

            if ((resolvedTexture == null || !resolvedTexture.isValid())
                    && tablistManager != null
                    && skinSourceMode != SkinSourceMode.SKINSRESTORER) {
                try {
                    resolvedTexture = tablistManager.resolveOriginalGameProfileSkinTexture(creatorId, creatorName);
                    if (resolvedTexture != null && resolvedTexture.isValid()) {
                        resolvedSource = "GameProfile";
                    }
                } catch (RuntimeException | LinkageError error) {
                    plugin.getLogger().log(Level.FINE,
                            "Unable to resolve fakeplayer original GameProfile skin for " + creatorName + ".", error);
                }
            }

            TablistManager.SkinTexture finalTexture = resolvedTexture;
            String finalSource = resolvedSource;
            plugin.getFoliaScheduler().runEntity(creator, () -> {
                if (finalTexture != null && finalTexture.isValid()) {
                    if (tablistManager != null && "SkinsRestorer".equals(finalSource)) {
                        tablistManager.updateSkinTexture(creatorId, finalTexture.value(), finalTexture.signature());
                    }
                    logSkinTextureResolved(creatorName, finalSource == null ? skinSourceMode.displayName() : finalSource,
                            finalTexture);
                    callback.accept(spawnWithTexture(creator, finalTexture, requireSkinTexture));
                    return;
                }

                if (tablistManager != null && skinSourceMode == SkinSourceMode.GAMEPROFILE) {
                    TablistManager.SkinTexture liveTexture = tablistManager.resolveLiveGameProfileSkinTexture(creator);
                    if (liveTexture != null && liveTexture.isValid()) {
                        logSkinTextureResolved(creatorName, "GameProfileLive", liveTexture);
                        callback.accept(spawnWithTexture(creator, liveTexture, requireSkinTexture));
                        return;
                    }
                }

                if (!requireSkinTexture) {
                    callback.accept(spawnWithTexture(creator, null, false));
                    return;
                }

                if (creator.isOnline()) {
                    plugin.getLogger().warning("[FakePlayer] " + skinSourceMode.displayName()
                            + " texture was not available for "
                            + creatorName + "; refusing to spawn a default-skin fakeplayer.");
                }

                callback.accept(SpawnResult.fail(message(
                        "SKIN-NOT-READY",
                        "&cѕᴋɪɴ ᴅᴀᴛᴀ ꜰᴏʀ &f{player}&c ɪѕ ɴᴏᴛ ʀᴇᴀᴅʏ. ʀᴇᴀᴘᴘʟʏ ʏᴏᴜʀ ѕᴋɪɴ ᴏʀ ʀᴇᴊᴏɪɴ, ᴛʜᴇɴ ᴛʀʏ ᴀɢᴀɪɴ.",
                        "{player}", creatorName
                )));
            });
        });
    }

    public synchronized Collection<FakePlayerSession> getActiveFakePlayers() {
        return List.copyOf(activeFakePlayers.values());
    }

    public String publicMessage(String path, String fallback, String... placeholders) {
        return message(path, fallback, placeholders);
    }

    private void logSkinTextureResolved(String playerName, String source, TablistManager.SkinTexture texture) {
        if (texture == null || !texture.isValid()) {
            return;
        }

        String resolver = source.equalsIgnoreCase("SkinsRestorer")
                ? "skinsrestorer-only-v3"
                : source.toLowerCase(Locale.ROOT);
        plugin.getLogger().info("[FakePlayer] Using " + source + " skin texture for " + playerName
                + " (resolver=" + resolver
                + ", valueLength=" + texture.value().length()
                + ", signed=" + (texture.signature() != null && !texture.signature().isBlank()) + ").");
    }

    private FakePlayerPacketBridge createBridge() {
        if (!isClassAvailable("com.comphenix.protocol.ProtocolLibrary")
                || plugin.getServer().getPluginManager().getPlugin("ProtocolLib") == null
                || !plugin.getServer().getPluginManager().isPluginEnabled("ProtocolLib")) {
            return null;
        }

        try {
            return new FakePlayerProtocolLibBridge(plugin, this);
        } catch (Throwable error) {
            plugin.getLogger().log(Level.WARNING, "Unable to initialize fakeplayer ProtocolLib bridge.", error);
            return null;
        }
    }

    private void loadSettings() {
        enabled = configBoolean("ENABLED", true);
        ttlSeconds = Math.max(1L, configLong("TTL-SECONDS", 10L));
        viewRadius = Math.max(1.0D, configDouble("VIEW-RADIUS", 64.0D));
        detectionRadius = Math.max(1.0D, configDouble("DETECTION-RADIUS", viewRadius));
        double detectionAngle = Math.max(1.0D, Math.min(45.0D, configDouble("DETECTION-ANGLE-DEGREES", 8.0D)));
        detectionAngleCos = Math.cos(Math.toRadians(detectionAngle));
        blockedCheckThreshold = Math.max(1, configInt("BLOCKED-CHECK-THRESHOLD", 3));
        checkIntervalTicks = Math.max(1L, configLong("CHECK-INTERVAL-TICKS", 5L));
        alertCooldownMillis = Math.max(1L, configLong("ALERT-COOLDOWN-SECONDS", 30L)) * 1000L;
        hideFromTablist = configBoolean("HIDE-FROM-TABLIST", true);
        tablistRemoveDelayTicks = Math.max(0L, configLong("TABLIST-REMOVE-DELAY-TICKS", hideFromTablist ? 5L : 200L));
        if (hideFromTablist) {
            tablistRemoveDelayTicks = Math.min(tablistRemoveDelayTicks, 5L);
        }
        logToConsole = configBoolean("LOG-TO-CONSOLE", true);
        skinSourceMode = SkinSourceMode.fromConfig(configString("SKIN-SOURCE-MODE", "AUTO"));
        simulatePhysics = configBoolean("SIMULATE-PHYSICS", true);
        lockAirPosition = configBoolean("LOCK-AIR-POSITION", false) && !simulatePhysics;
        hitResponseEnabled = configBoolean("HIT-RESPONSE.ENABLED", true);
        physicsGravity = Math.max(0.0D, configDouble("PHYSICS.GRAVITY", 0.08D));
        physicsVerticalDrag = clamp(configDouble("PHYSICS.VERTICAL-DRAG", 0.98D), 0.0D, 1.0D);
        physicsAirHorizontalDrag = clamp(configDouble("PHYSICS.AIR-HORIZONTAL-DRAG", 0.91D), 0.0D, 1.0D);
        physicsGroundFriction = clamp(configDouble("PHYSICS.GROUND-FRICTION", 0.6D), 0.0D, 1.0D);
        physicsMaxFallSpeed = Math.min(-0.1D, configDouble("PHYSICS.MAX-FALL-SPEED", -3.92D));
    }

    private void restartMonitor() {
        cancelMonitor();
        cancelPositionLock();
        if (!enabled) {
            return;
        }
        monitorTask = plugin.getFoliaScheduler().runGlobalTimer(this::tick, checkIntervalTicks, checkIntervalTicks);
        if (lockAirPosition || simulatePhysics) {
            positionLockTask = plugin.getFoliaScheduler().runGlobalTimer(this::tickPositionLock, 1L, 1L);
        }
    }

    private void cancelMonitor() {
        if (monitorTask != null) {
            monitorTask.cancel();
            monitorTask = null;
        }
    }

    private void cancelPositionLock() {
        if (positionLockTask != null) {
            positionLockTask.cancel();
            positionLockTask = null;
        }
    }

    private void tick() {
        List<FakePlayerSession> snapshot = List.copyOf(getActiveFakePlayers());
        long now = System.currentTimeMillis();
        for (FakePlayerSession fakePlayer : snapshot) {
            if (now >= fakePlayer.expiresAtMillis()) {
                synchronized (this) {
                    if (activeFakePlayers.containsKey(fakePlayer.id())) {
                        removeInternal(fakePlayer.id(), "expired");
                    }
                }
                continue;
            }
            updateViewers(fakePlayer);
            checkDetection(fakePlayer);
        }
    }

    private void tickPositionLock() {
        if ((!lockAirPosition && !simulatePhysics) || !isEnabled() || packetBridge == null) {
            return;
        }

        long now = System.currentTimeMillis();
        for (FakePlayerSession fakePlayer : List.copyOf(getActiveFakePlayers())) {
            if (now >= fakePlayer.expiresAtMillis()) {
                continue;
            }
            if (simulatePhysics) {
                tickPhysics(fakePlayer);
            } else if (lockAirPosition && !fakePlayer.isPositionLockPaused(now)) {
                refreshPosition(fakePlayer);
            }
        }
    }

    private void tickPhysics(FakePlayerSession fakePlayer) {
        Location current = fakePlayer.visualLocation();
        World world = current.getWorld();
        if (world == null) {
            return;
        }

        Vector velocity = fakePlayer.visualVelocity();
        boolean onGround = hasGroundSupport(current);
        if (onGround && velocity.getY() < 0D) {
            velocity.setY(0D);
        }

        if (!onGround) {
            velocity.setY(Math.max(physicsMaxFallSpeed, (velocity.getY() - physicsGravity) * physicsVerticalDrag));
            velocity.setX(velocity.getX() * physicsAirHorizontalDrag);
            velocity.setZ(velocity.getZ() * physicsAirHorizontalDrag);
        } else {
            velocity.setX(velocity.getX() * physicsGroundFriction);
            velocity.setZ(velocity.getZ() * physicsGroundFriction);
        }

        if (Math.abs(velocity.getX()) < 0.003D) {
            velocity.setX(0D);
        }
        if (Math.abs(velocity.getZ()) < 0.003D) {
            velocity.setZ(0D);
        }

        Location next = current.clone();
        moveVertical(next, velocity);
        moveHorizontal(next, velocity);

        boolean nextOnGround = hasGroundSupport(next);
        if (nextOnGround && velocity.getY() < 0D) {
            velocity.setY(0D);
        }

        fakePlayer.setVisualLocation(next);
        fakePlayer.setVisualVelocity(velocity);
        fakePlayer.setVisualOnGround(nextOnGround);

        if (!sameBlockPrecise(current, next)
                || velocity.lengthSquared() > 0.000001D
                || fakePlayer.visualOnGround() != onGround) {
            refreshPosition(fakePlayer);
        }
    }

    private void moveVertical(Location location, Vector velocity) {
        double yVelocity = velocity.getY();
        if (Math.abs(yVelocity) < 0.00001D) {
            return;
        }

        if (yVelocity < 0D) {
            Location hit = findGroundCollision(location, -yVelocity + 0.05D);
            if (hit != null && hit.getY() >= location.getY() + yVelocity - 0.001D) {
                location.setY(hit.getY());
                velocity.setY(0D);
                return;
            }
        }

        location.add(0D, yVelocity, 0D);
    }

    private Location findGroundCollision(Location location, double distance) {
        World world = location.getWorld();
        if (world == null || distance <= 0D) {
            return null;
        }

        RayTraceResult rayTrace = world.rayTraceBlocks(
                location.clone().add(0D, 0.05D, 0D),
                new Vector(0D, -1D, 0D),
                distance,
                FluidCollisionMode.NEVER,
                true
        );
        if (rayTrace == null || rayTrace.getHitBlock() == null || rayTrace.getHitBlock().isPassable()) {
            return null;
        }

        BoundingBox box = rayTrace.getHitBlock().getBoundingBox();
        double groundY = box == null ? rayTrace.getHitBlock().getY() + 1.0D : box.getMaxY();
        Location ground = location.clone();
        ground.setY(groundY);
        return ground;
    }

    private void moveHorizontal(Location location, Vector velocity) {
        double xVelocity = velocity.getX();
        if (Math.abs(xVelocity) >= 0.00001D) {
            Location movedX = location.clone().add(xVelocity, 0D, 0D);
            if (intersectsSolid(movedX)) {
                velocity.setX(0D);
            } else {
                location.setX(movedX.getX());
            }
        }

        double zVelocity = velocity.getZ();
        if (Math.abs(zVelocity) >= 0.00001D) {
            Location movedZ = location.clone().add(0D, 0D, zVelocity);
            if (intersectsSolid(movedZ)) {
                velocity.setZ(0D);
            } else {
                location.setZ(movedZ.getZ());
            }
        }
    }

    private boolean hasGroundSupport(Location location) {
        World world = location.getWorld();
        if (world == null) {
            return false;
        }
        if (location.getY() <= world.getMinHeight()) {
            return true;
        }

        double y = location.getY() - 0.05D;
        return hasSolidBlockAt(world, location.getX() - 0.3D, y, location.getZ() - 0.3D)
                || hasSolidBlockAt(world, location.getX() - 0.3D, y, location.getZ() + 0.3D)
                || hasSolidBlockAt(world, location.getX() + 0.3D, y, location.getZ() - 0.3D)
                || hasSolidBlockAt(world, location.getX() + 0.3D, y, location.getZ() + 0.3D)
                || hasSolidBlockAt(world, location.getX(), y, location.getZ());
    }

    private boolean intersectsSolid(Location location) {
        World world = location.getWorld();
        if (world == null) {
            return false;
        }

        double[] xs = {location.getX() - 0.3D, location.getX(), location.getX() + 0.3D};
        double[] ys = {location.getY() + 0.1D, location.getY() + 0.9D, location.getY() + 1.75D};
        double[] zs = {location.getZ() - 0.3D, location.getZ(), location.getZ() + 0.3D};
        for (double x : xs) {
            for (double y : ys) {
                for (double z : zs) {
                    if (hasSolidBlockAt(world, x, y, z)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean hasSolidBlockAt(World world, double x, double y, double z) {
        if (y < world.getMinHeight() || y > world.getMaxHeight()) {
            return false;
        }
        return !world.getBlockAt(floor(x), floor(y), floor(z)).isPassable();
    }

    private int floor(double value) {
        return (int) Math.floor(value);
    }

    private boolean sameBlockPrecise(Location first, Location second) {
        return first.getWorld() == second.getWorld()
                && Math.abs(first.getX() - second.getX()) < 0.0001D
                && Math.abs(first.getY() - second.getY()) < 0.0001D
                && Math.abs(first.getZ() - second.getZ()) < 0.0001D
                && Math.abs(first.getYaw() - second.getYaw()) < 0.0001D
                && Math.abs(first.getPitch() - second.getPitch()) < 0.0001D;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private void refreshPosition(FakePlayerSession fakePlayer) {
        for (UUID viewerId : new HashSet<>(fakePlayer.viewers())) {
            Player viewer = Bukkit.getPlayer(viewerId);
            if (viewer != null && viewer.isOnline()) {
                packetBridge.refreshPosition(viewer, fakePlayer);
            }
        }
    }

    void refreshVisualPosition(FakePlayerSession fakePlayer) {
        if (fakePlayer != null && packetBridge != null) {
            refreshPosition(fakePlayer);
        }
    }

    private void updateViewers(FakePlayerSession fakePlayer) {
        Set<UUID> wantedViewers = new HashSet<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (shouldSee(player, fakePlayer)) {
                wantedViewers.add(player.getUniqueId());
            }
        }

        for (UUID previousViewer : new HashSet<>(fakePlayer.viewers())) {
            if (wantedViewers.contains(previousViewer)) {
                continue;
            }
            Player viewer = Bukkit.getPlayer(previousViewer);
            if (viewer != null && viewer.isOnline()) {
                packetBridge.destroy(viewer, fakePlayer);
            }
            fakePlayer.viewers().remove(previousViewer);
            fakePlayer.aimCounts().remove(previousViewer);
        }

        for (UUID wantedViewer : wantedViewers) {
            if (fakePlayer.viewers().contains(wantedViewer)) {
                continue;
            }
            Player viewer = Bukkit.getPlayer(wantedViewer);
            if (viewer == null || !viewer.isOnline()) {
                continue;
            }
            packetBridge.spawn(viewer, fakePlayer);
            fakePlayer.viewers().add(wantedViewer);
            if (hideFromTablist) {
                scheduleTablistRemoval(wantedViewer, fakePlayer.id());
            }
        }
    }

    private boolean shouldSee(Player player, FakePlayerSession fakePlayer) {
        if (player == null || !player.isOnline()) {
            return false;
        }
        Location location = fakePlayer.location();
        World world = location.getWorld();
        if (world == null || !player.getWorld().equals(world)) {
            return false;
        }
        return player.getLocation().distanceSquared(location) <= viewRadius * viewRadius;
    }

    private void scheduleTablistRemoval(UUID viewerId, long fakePlayerId) {
        long spawnDelayTicks = Math.max(0L, configLong("SPAWN-DELAY-TICKS", 20L));
        long delayTicks = spawnDelayTicks + tablistRemoveDelayTicks;
        plugin.getFoliaScheduler().runGlobalLater(() -> {
            FakePlayerSession fakePlayer;
            synchronized (this) {
                fakePlayer = activeFakePlayers.get(fakePlayerId);
            }
            Player viewer = Bukkit.getPlayer(viewerId);
            if (fakePlayer != null && viewer != null && viewer.isOnline() && fakePlayer.viewers().contains(viewerId)) {
                packetBridge.removeFromTablist(viewer, fakePlayer);
            }
        }, delayTicks);
    }

    private void checkDetection(FakePlayerSession fakePlayer) {
        Location location = fakePlayer.location();
        World world = location.getWorld();
        if (world == null) {
            return;
        }

        double radiusSquared = detectionRadius * detectionRadius;
        for (UUID viewerId : new HashSet<>(fakePlayer.viewers())) {
            Player player = Bukkit.getPlayer(viewerId);
            if (player == null || !player.isOnline()
                    || player.getUniqueId().equals(fakePlayer.creatorUuid())
                    || PermissionUtils.has(player, BYPASS_PERMISSION)) {
                fakePlayer.aimCounts().remove(viewerId);
                continue;
            }
            if (!player.getWorld().equals(world)
                    || player.getEyeLocation().distanceSquared(fakePlayer.eyeLocation()) > radiusSquared) {
                fakePlayer.aimCounts().remove(viewerId);
                continue;
            }

            if (isAimingThroughBlock(player, fakePlayer)) {
                int count = fakePlayer.aimCounts().merge(viewerId, 1, Integer::sum);
                if (count >= blockedCheckThreshold) {
                    broadcastAlert(fakePlayer, player);
                    fakePlayer.aimCounts().put(viewerId, 0);
                }
            } else {
                fakePlayer.aimCounts().remove(viewerId);
            }
        }
    }

    private boolean isAimingThroughBlock(Player player, FakePlayerSession fakePlayer) {
        Location eye = player.getEyeLocation();
        Location target = fakePlayer.eyeLocation();
        Vector toTarget = target.toVector().subtract(eye.toVector());
        double distance = toTarget.length();
        if (distance <= 0.01D) {
            return false;
        }

        Vector direction = toTarget.clone().normalize();
        double dot = eye.getDirection().normalize().dot(direction);
        if (dot < detectionAngleCos) {
            return false;
        }

        RayTraceResult rayTrace = eye.getWorld().rayTraceBlocks(
                eye,
                direction,
                distance,
                FluidCollisionMode.NEVER,
                true
        );
        if (rayTrace == null || rayTrace.getHitBlock() == null) {
            return false;
        }

        Block hitBlock = rayTrace.getHitBlock();
        if (hitBlock.isPassable()) {
            return false;
        }

        Location hitPosition = rayTrace.getHitPosition().toLocation(eye.getWorld());
        return hitPosition.distanceSquared(eye) < Math.max(0.0D, distance - 0.35D) * Math.max(0.0D, distance - 0.35D);
    }

    private synchronized int removeAllInternal(String reason) {
        List<Long> ids = new ArrayList<>(activeFakePlayers.keySet());
        int removed = 0;
        for (Long id : ids) {
            if (removeInternal(id, reason)) {
                removed++;
            }
        }
        return removed;
    }

    private synchronized boolean removeInternal(long id, String reason) {
        FakePlayerSession fakePlayer = activeFakePlayers.remove(id);
        if (fakePlayer == null) {
            return false;
        }

        for (UUID viewerId : new HashSet<>(fakePlayer.viewers())) {
            Player viewer = Bukkit.getPlayer(viewerId);
            if (viewer != null && viewer.isOnline()) {
                packetBridge.destroy(viewer, fakePlayer);
            }
        }
        fakePlayer.viewers().clear();
        fakePlayer.aimCounts().clear();
        fakePlayer.alertCooldowns().clear();
        fakePlayer.hitCooldowns().clear();
        fakePlayer.hardPositionLockTimes().clear();

        if (logToConsole) {
            plugin.getLogger().info("Removed fakeplayer #" + fakePlayer.id() + " (" + reason + ").");
        }
        return true;
    }

    private void broadcastAlert(FakePlayerSession fakePlayer, Player suspected) {
        long now = System.currentTimeMillis();
        Long previous = fakePlayer.alertCooldowns().get(suspected.getUniqueId());
        if (previous != null && now - previous < alertCooldownMillis) {
            return;
        }
        fakePlayer.alertCooldowns().put(suspected.getUniqueId(), now);

        Location location = suspected.getLocation();
        List<String> lines = messageList("ALERT", List.of(
                "&8[&cꜰᴀᴋᴇᴘʟᴀʏᴇʀ&8] &f{player} &7ᴛʀᴀᴄᴋᴇᴅ ʙᴀɪᴛ &f{fakeplayer}&7 ᴛʜʀᴏᴜɢʜ ʙʟᴏᴄᴋѕ.",
                "&7ʟᴏᴄᴀᴛɪᴏɴ: &f{world} {x}, {y}, {z} &8| &7ᴄʀᴇᴀᴛᴇᴅ ʙʏ: &f{creator}"
        ),
                "{player}", suspected.getName(),
                "{fakeplayer}", fakePlayer.displayName(),
                "{creator}", fakePlayer.creatorName(),
                "{world}", location.getWorld() == null ? "unknown" : location.getWorld().getName(),
                "{x}", String.valueOf(location.getBlockX()),
                "{y}", String.valueOf(location.getBlockY()),
                "{z}", String.valueOf(location.getBlockZ())
        );

        Set<UUID> notified = new HashSet<>();
        for (Player staff : Bukkit.getOnlinePlayers()) {
            boolean isCreator = staff.getUniqueId().equals(fakePlayer.creatorUuid());
            boolean canReceive = PermissionUtils.has(staff, ALERT_PERMISSION)
                    || PermissionUtils.has(staff, "ultimatedonutsmp.staff.alerts.receive");
            if (!isCreator && !canReceive) {
                continue;
            }
            if (notified.add(staff.getUniqueId())) {
                sendClickableAlert(staff, suspected, lines);
            }
        }

        if (logToConsole) {
            plugin.getLogger().warning(ColorUtils.strip(String.join(" ", lines)));
        }
    }

    private void sendClickableAlert(Player recipient, Player target, List<String> lines) {
        String command = "/tp " + target.getName();
        String hover = message("ALERT-HOVER", "&eᴄʟɪᴄᴋ ᴛᴏ ᴛᴇʟᴇᴘᴏʀᴛ ᴛᴏ &f{player}", "{player}", target.getName());
        for (String line : lines) {
            Component component = ColorUtils.toComponent(line)
                    .clickEvent(ClickEvent.runCommand(command))
                    .hoverEvent(HoverEvent.showText(ColorUtils.toComponent(hover)));
            recipient.sendMessage(component);
        }
    }

    boolean handleAttackPacket(Player attacker, int entityId) {
        if (attacker == null) {
            return false;
        }

        FakePlayerSession fakePlayer = getByEntityId(entityId);
        if (fakePlayer == null) {
            return false;
        }

        UUID attackerId = attacker.getUniqueId();
        long fakePlayerId = fakePlayer.id();
        plugin.getFoliaScheduler().runEntity(attacker, () -> handleAttackSync(attackerId, fakePlayerId));
        return true;
    }

    boolean handleSwingPacket(Player attacker) {
        if (attacker == null || !attacker.isOnline()) {
            return false;
        }

        FakePlayerSession fakePlayer = findTargetedFakePlayer(attacker);
        if (fakePlayer == null) {
            return false;
        }

        UUID attackerId = attacker.getUniqueId();
        long fakePlayerId = fakePlayer.id();
        plugin.getFoliaScheduler().runEntity(attacker, () -> handleAttackSync(attackerId, fakePlayerId));
        return true;
    }

    private FakePlayerSession findTargetedFakePlayer(Player attacker) {
        Location eye = attacker.getEyeLocation();
        Vector direction = eye.getDirection().normalize();
        double reach = Math.max(3.0D, configDouble("HIT-RESPONSE.REACH", 4.5D));

        FakePlayerSession nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (FakePlayerSession fakePlayer : getActiveFakePlayers()) {
            if (!fakePlayer.viewers().contains(attacker.getUniqueId())) {
                continue;
            }

            Location location = fakePlayer.visualLocation();
            if (location.getWorld() == null || !location.getWorld().equals(attacker.getWorld())) {
                continue;
            }
            if (eye.distanceSquared(fakePlayer.visualEyeLocation()) > (reach + 1.0D) * (reach + 1.0D)) {
                continue;
            }

            BoundingBox hitbox = BoundingBox.of(location.clone().add(0D, 0.9D, 0D), 0.45D, 0.9D, 0.45D);
            RayTraceResult hit = hitbox.rayTrace(eye.toVector(), direction, reach);
            if (hit == null) {
                continue;
            }

            double distance = hit.getHitPosition().distanceSquared(eye.toVector());
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = fakePlayer;
            }
        }
        return nearest;
    }

    private synchronized FakePlayerSession getByEntityId(int entityId) {
        for (FakePlayerSession fakePlayer : activeFakePlayers.values()) {
            if (fakePlayer.entityId() == entityId) {
                return fakePlayer;
            }
        }
        return null;
    }

    private void handleAttackSync(UUID attackerId, long fakePlayerId) {
        if (!hitResponseEnabled || packetBridge == null) {
            return;
        }

        Player attacker = Bukkit.getPlayer(attackerId);
        if (attacker == null || !attacker.isOnline()) {
            return;
        }

        FakePlayerSession fakePlayer;
        synchronized (this) {
            fakePlayer = activeFakePlayers.get(fakePlayerId);
        }
        if (fakePlayer == null || !fakePlayer.viewers().contains(attackerId)) {
            return;
        }

        long now = System.currentTimeMillis();
        Long lastHit = fakePlayer.hitCooldowns().get(attackerId);
        if (lastHit != null && now - lastHit < HIT_RESPONSE_COOLDOWN_MILLIS) {
            return;
        }
        fakePlayer.hitCooldowns().put(attackerId, now);

        packetBridge.playHitReaction(attacker, fakePlayer);
    }

    private String resolveDisplayName(Player player) {
        String displayName = ColorUtils.strip(player.getDisplayName()).trim();
        return displayName.isBlank() ? player.getName() : displayName;
    }

    private String resolveProfileName(Player player, String displayName) {
        String name = sanitizeProfileName(displayName);
        if (name.isBlank()) {
            name = sanitizeProfileName(player.getName());
        }
        if (name.isBlank()) {
            name = "FakePlayer";
        }
        return name.length() > 16 ? name.substring(0, 16) : name;
    }

    private UUID resolveFakeUuid(UUID textureProfileUuid) {
        if (!configBoolean("USE-SKIN-PROFILE-UUID", true)) {
            return UUID.randomUUID();
        }

        if (textureProfileUuid == null || isOnlinePlayerUuid(textureProfileUuid) || isFakeUuidInUse(textureProfileUuid)) {
            return UUID.randomUUID();
        }
        return textureProfileUuid;
    }

    private boolean isOnlinePlayerUuid(UUID uuid) {
        Player online = uuid == null ? null : Bukkit.getPlayer(uuid);
        return online != null && online.isOnline();
    }

    private synchronized boolean isFakeUuidInUse(UUID uuid) {
        if (uuid == null) {
            return false;
        }
        for (FakePlayerSession fakePlayer : activeFakePlayers.values()) {
            if (uuid.equals(fakePlayer.fakeUuid())) {
                return true;
            }
        }
        return false;
    }

    private UUID extractTextureProfileUuid(TablistManager.SkinTexture skinTexture) {
        if (skinTexture == null || !skinTexture.isValid()) {
            return null;
        }

        try {
            String decoded = new String(Base64.getDecoder().decode(skinTexture.value()), StandardCharsets.UTF_8);
            Matcher matcher = TEXTURE_PROFILE_ID_PATTERN.matcher(decoded);
            if (!matcher.find()) {
                return null;
            }
            return parseTextureUuid(matcher.group(1));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private UUID parseTextureUuid(String rawUuid) {
        if (rawUuid == null || rawUuid.isBlank()) {
            return null;
        }

        String compact = rawUuid.replace("-", "");
        if (compact.length() != 32) {
            return null;
        }

        return UUID.fromString(compact.substring(0, 8)
                + "-"
                + compact.substring(8, 12)
                + "-"
                + compact.substring(12, 16)
                + "-"
                + compact.substring(16, 20)
                + "-"
                + compact.substring(20));
    }

    private void logPreparedSkinProfile(String playerName, UUID fakeUuid, UUID textureProfileUuid) {
        if (!logToConsole) {
            return;
        }

        String uuidSource = fakeUuid != null && fakeUuid.equals(textureProfileUuid) ? "texture-profile" : "generated";
        plugin.getLogger().info("[FakePlayer] Prepared skinned GameProfile for " + playerName
                + " (fakeUuid=" + fakeUuid
                + ", textureProfileUuid=" + (textureProfileUuid == null ? "none" : textureProfileUuid)
                + ", uuidSource=" + uuidSource + ").");
    }

    private String sanitizeProfileName(String input) {
        if (input == null) {
            return "";
        }
        return input.chars()
                .mapToObj(codePoint -> String.valueOf((char) codePoint))
                .filter(character -> character.matches("[A-Za-z0-9_]"))
                .collect(java.util.stream.Collectors.joining());
    }

    private boolean configBoolean(String path, boolean fallback) {
        return plugin.getConfigManager().getStaffMode().getBoolean("FAKE-PLAYER." + path, fallback);
    }

    private String configString(String path, String fallback) {
        return plugin.getConfigManager().getStaffMode().getString("FAKE-PLAYER." + path, fallback);
    }

    private int configInt(String path, int fallback) {
        return plugin.getConfigManager().getStaffMode().getInt("FAKE-PLAYER." + path, fallback);
    }

    private long configLong(String path, long fallback) {
        return plugin.getConfigManager().getStaffMode().getLong("FAKE-PLAYER." + path, fallback);
    }

    private double configDouble(String path, double fallback) {
        return plugin.getConfigManager().getStaffMode().getDouble("FAKE-PLAYER." + path, fallback);
    }

    private String message(String path, String fallback, String... placeholders) {
        String message = plugin.getConfigManager().getStaffMode()
                .getString("FAKE-PLAYER.MESSAGES." + path, fallback);
        return replace(message, placeholders);
    }

    private List<String> messageList(String path, List<String> fallback, String... placeholders) {
        List<String> lines = plugin.getConfigManager().getStaffMode().getStringList("FAKE-PLAYER.MESSAGES." + path);
        if (lines == null || lines.isEmpty()) {
            lines = fallback;
        }
        return lines.stream().map(line -> replace(line, placeholders)).toList();
    }

    private String replace(String text, String... placeholders) {
        String output = text == null ? "" : text;
        for (int index = 0; index + 1 < placeholders.length; index += 2) {
            output = output.replace(placeholders[index], placeholders[index + 1] == null ? "" : placeholders[index + 1]);
        }
        return output;
    }

    private boolean isClassAvailable(String className) {
        try {
            Class.forName(className, false, getClass().getClassLoader());
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    public record SpawnResult(boolean success, String message, FakePlayerSession fakePlayer) {
        public static SpawnResult success(String message, FakePlayerSession fakePlayer) {
            return new SpawnResult(true, message, fakePlayer);
        }

        public static SpawnResult fail(String message) {
            return new SpawnResult(false, message, null);
        }
    }

    private enum SkinSourceMode {
        AUTO("auto"),
        SKINSRESTORER("SkinsRestorer"),
        GAMEPROFILE("GameProfile");

        private final String displayName;

        SkinSourceMode(String displayName) {
            this.displayName = displayName;
        }

        String displayName() {
            return displayName;
        }

        static SkinSourceMode fromConfig(String value) {
            if (value == null || value.isBlank()) {
                return AUTO;
            }

            String normalized = value.trim().replace("-", "_").toUpperCase(Locale.ROOT);
            for (SkinSourceMode mode : values()) {
                if (mode.name().equals(normalized)) {
                    return mode;
                }
            }
            return AUTO;
        }
    }
}
