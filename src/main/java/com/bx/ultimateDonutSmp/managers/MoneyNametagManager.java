package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Transformation;
import org.joml.Vector3f;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Renders a player's balance on a floating line under their username.
 *
 * <p>The line is a {@link TextDisplay} that every viewer's client is told to draw riding its owner,
 * so it is pinned to the player the same way the username is and never trails behind a sprint. The
 * ride only exists in the packet: server side the display stays a free entity, because Spigot
 * refuses to teleport a player who is carrying real passengers and mounting it for real would
 * quietly break every warp, home and teleport request on the server.</p>
 *
 * <p>The vanilla username is left exactly as it is. Passengers hang from the same anchor the
 * username is measured against, half a block below the name itself, so the line is pushed back up
 * to sit directly underneath it and the two read as one two line nametag.</p>
 *
 * <p>Because the ride is a fiction the server does not share, the server keeps broadcasting the
 * display's own movement. A client that hears both puts the line on the player one tick and back on
 * the entity the next, which reads as a line bobbing up and down, so those movement packets are
 * dropped on the way out and the vehicle is left as the only thing placing it.</p>
 *
 * <p>Every player decides for themselves whether they see the line through
 * {@code /settings > Money Nametags}, so a line only exists while somebody online has that setting
 * switched on, and it is only sent to the players who asked for it.</p>
 */
public class MoneyNametagManager {

    private static final String DISPLAY_TAG = "uds_money_nametag";
    private static final String BALANCE_PLACEHOLDER = "{balance}";

    /** Vanilla draws a username half a block above the point a passenger hangs from. */
    private static final float NAME_TAG_HEIGHT = 0.5F;
    /** A line of display text is the same height as a username, both drawn at 0.025 scale. */
    private static final float LINE_HEIGHT = 0.25F;

    private final UltimateDonutSmp plugin;
    private final Map<UUID, UUID> displays = new ConcurrentHashMap<>();
    private final Map<UUID, String> renderedText = new ConcurrentHashMap<>();
    private final Set<Integer> displayEntityIds = ConcurrentHashMap.newKeySet();
    private ProtocolManager protocolManager;
    private PacketListener movementListener;

    public MoneyNametagManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    /** Renders {@code format} for {@code balance}; visible for tests without a running server. */
    public static String render(String format, double balance, boolean shortFormat) {
        String amount = shortFormat ? NumberUtils.formatNice(balance) : NumberUtils.format(balance);
        return format == null ? amount : format.replace(BALANCE_PLACEHOLDER, amount);
    }

    public boolean isEnabled() {
        return config().getBoolean("MONEY-NAMETAGS.ENABLED", true);
    }

    public long getUpdateIntervalTicks() {
        return Math.max(1L, config().getLong("MONEY-NAMETAGS.UPDATE-INTERVAL-TICKS", 10L));
    }

    /** Whether {@code viewer} asked to see balances above other players. */
    public boolean isEnabledFor(Player viewer) {
        if (viewer == null) {
            return false;
        }
        PlayerData data = plugin.getPlayerDataManager().get(viewer);
        return data != null && data.isMoneyNametagsEnabled();
    }

    /** Refreshes the balance on every line and keeps each one riding its owner. */
    public void updateAll() {
        if (!isEnabled()) {
            clearAll();
            return;
        }
        Set<UUID> viewers = viewers();
        if (viewers.isEmpty()) {
            clearAll();
            return;
        }
        plugin.getSpigotScheduler().forEachOnlinePlayer(owner -> refresh(owner, viewers));
    }

    /** Brings one player's own line up to date, spawning it when somebody wants to see it. */
    public void update(Player owner) {
        if (!isEnabled()) {
            return;
        }
        Set<UUID> viewers = viewers();
        if (!viewers.isEmpty()) {
            refresh(owner, viewers);
        }
    }

    /** Re-applies one player's own choice to every line currently in the world. */
    public void refreshViewer(Player viewer) {
        if (viewer == null) {
            return;
        }
        if (!isEnabled()) {
            clearAll();
            return;
        }

        boolean wanted = isEnabledFor(viewer);
        for (UUID ownerUuid : Set.copyOf(displays.keySet())) {
            TextDisplay display = display(ownerUuid);
            if (display == null) {
                continue;
            }
            Player owner = Bukkit.getPlayer(ownerUuid);
            if (wanted && owner != null && viewer.canSee(owner)) {
                viewer.showEntity(plugin, display);
                mount(viewer, owner, display);
            } else {
                viewer.hideEntity(plugin, display);
            }
        }
        if (!wanted && viewers().isEmpty()) {
            clearAll();
        }
    }

    public void remove(UUID ownerUuid) {
        renderedText.remove(ownerUuid);
        UUID displayUuid = displays.remove(ownerUuid);
        if (displayUuid == null) {
            return;
        }
        Entity entity = Bukkit.getEntity(displayUuid);
        if (entity != null) {
            displayEntityIds.remove(entity.getEntityId());
            entity.remove();
        }
    }

    public void clearAll() {
        for (UUID ownerUuid : Set.copyOf(displays.keySet())) {
            remove(ownerUuid);
        }
        renderedText.clear();
    }

    public void reload() {
        clearAll();
        purgeOrphanedDisplays();
    }

    public void shutdown() {
        clearAll();
        displayEntityIds.clear();
        ProtocolManager manager = protocolManager();
        if (movementListener != null && manager != null) {
            try {
                manager.removePacketListener(movementListener);
            } catch (RuntimeException | LinkageError ignored) {
                // The server is going down anyway, a listener left behind costs nothing.
            }
        }
        movementListener = null;
    }

    /**
     * Deletes lines a crash or a reload left behind. They are spawned non-persistent so they never
     * reach the region files, but a plugin reload leaves the previous run's entities loaded.
     */
    public void purgeOrphanedDisplays() {
        for (World world : Bukkit.getWorlds()) {
            for (TextDisplay display : world.getEntitiesByClass(TextDisplay.class)) {
                if (display.getScoreboardTags().contains(DISPLAY_TAG)
                        && !displays.containsValue(display.getUniqueId())) {
                    display.remove();
                }
            }
        }
    }

    private void refresh(Player owner, Set<UUID> viewers) {
        if (owner == null || !owner.isOnline()) {
            return;
        }
        if (!shouldDisplayFor(owner)) {
            remove(owner.getUniqueId());
            return;
        }

        TextDisplay display = display(owner.getUniqueId());
        if (display != null && !owner.getWorld().equals(display.getWorld())) {
            remove(owner.getUniqueId());
            display = null;
        }
        if (display == null) {
            display = spawn(owner);
            if (display == null) {
                return;
            }
        }

        String text = ColorUtils.colorize(currentText(owner), owner);
        if (!text.equals(renderedText.get(owner.getUniqueId()))) {
            display.setText(text);
            renderedText.put(owner.getUniqueId(), text);
        }
        keepInTrackingRange(display, owner);
        applyVisibility(display, owner, viewers);
    }

    private String currentText(Player owner) {
        return render(
                config().getString("MONEY-NAMETAGS.FORMAT", "&a${balance}"),
                plugin.getEconomyManager().getBalance(owner),
                config().getBoolean("MONEY-NAMETAGS.SHORT-FORMAT", true));
    }

    /**
     * Hidden players keep their balance to themselves, and a sneaking player loses the line the
     * same way they lose their username.
     */
    private boolean shouldDisplayFor(Player owner) {
        if (owner.isSneaking() && config().getBoolean("MONEY-NAMETAGS.HIDE-WHILE-SNEAKING", true)) {
            return false;
        }
        HideManager hideManager = plugin.getHideManager();
        return hideManager == null || !hideManager.isHidden(owner.getUniqueId());
    }

    private TextDisplay spawn(Player owner) {
        try {
            TextDisplay display = owner.getWorld().spawn(anchor(owner), TextDisplay.class, textDisplay -> {
                textDisplay.addScoreboardTag(DISPLAY_TAG);
                textDisplay.setBillboard(Display.Billboard.CENTER);
                textDisplay.setDefaultBackground(false);
                textDisplay.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
                textDisplay.setShadowed(true);
                textDisplay.setViewRange(viewRange());
                textDisplay.setLineWidth(200);
                textDisplay.setPersistent(false);
                textDisplay.setInvulnerable(true);
                textDisplay.setGravity(false);
                textDisplay.setSilent(true);
                textDisplay.setVisibleByDefault(false);
                applyLinePlacement(textDisplay);
            });
            renderedText.remove(owner.getUniqueId());
            displays.put(owner.getUniqueId(), display.getUniqueId());
            displayEntityIds.add(display.getEntityId());
            registerMovementListener();
            return display;
        } catch (RuntimeException error) {
            plugin.getLogger().warning("Unable to spawn a money nametag for " + owner.getName() + ".");
            return null;
        }
    }

    /**
     * Lifts the text from the anchor a passenger hangs from up to just under the username, leaving
     * the configured gap between the bottom of the name and the top of the balance.
     */
    private void applyLinePlacement(TextDisplay display) {
        Transformation transformation = display.getTransformation();
        float gap = (float) config().getDouble("MONEY-NAMETAGS.LINE-GAP", 0.05D);
        Vector3f translation = new Vector3f(0.0F, NAME_TAG_HEIGHT - LINE_HEIGHT - gap, 0.0F);
        display.setTransformation(new Transformation(
                translation,
                transformation.getLeftRotation(),
                transformation.getScale(),
                transformation.getRightRotation()));
    }

    /**
     * The client draws the line on its owner and never hears about these moves, so the entity's own
     * position only decides whether a viewer is sent it at all. Keeping it on top of the player
     * keeps that decision matching the player's own, and leaves the entity somewhere sensible for
     * the moment between a client being sent the line and being told what it rides.
     */
    private void keepInTrackingRange(TextDisplay display, Player owner) {
        Location anchor = anchor(owner);
        if (display.getLocation().distanceSquared(anchor) < 0.01D) {
            return;
        }
        if (plugin.getSpigotScheduler().isFolia()) {
            plugin.getSpigotScheduler().teleport(display, anchor);
            return;
        }
        display.teleport(anchor);
    }

    private Location anchor(Player owner) {
        Location location = owner.getLocation();
        return new Location(
                owner.getWorld(),
                location.getX(),
                location.getY() + owner.getHeight(),
                location.getZ());
    }

    /**
     * Only the players who asked for balances get the line sent to them, and only while they can
     * see its owner in the first place.
     */
    private void applyVisibility(TextDisplay display, Player owner, Set<UUID> viewers) {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (viewers.contains(viewer.getUniqueId()) && viewer.canSee(owner)) {
                viewer.showEntity(plugin, display);
                mount(viewer, owner, display);
            } else {
                viewer.hideEntity(plugin, display);
            }
        }
    }

    /**
     * Tells one viewer's client that the line rides its owner. Anything genuinely riding the player
     * is listed alongside it, otherwise the client would drop those the moment this packet lands.
     */
    private void mount(Player viewer, Player owner, TextDisplay display) {
        ProtocolManager manager = protocolManager();
        if (manager == null || viewer == null || !viewer.isOnline() || !sharesWorld(viewer, owner)) {
            return;
        }
        try {
            PacketContainer packet = manager.createPacket(PacketType.Play.Server.MOUNT);
            packet.getIntegers().write(0, owner.getEntityId());
            packet.getIntegerArrays().write(0, passengerIds(owner, display));
            manager.sendServerPacket(viewer, packet, false);
        } catch (RuntimeException | LinkageError error) {
            plugin.getLogger().log(Level.FINE, "Unable to pin a money nametag to its player.", error);
        }
    }

    /**
     * Drops the display's own movement packets on the way to the client. The client already places
     * it on the player it rides, and letting the server's idea of where the entity is arrive as well
     * makes the line flick between the two positions every tick.
     */
    private void registerMovementListener() {
        if (movementListener != null) {
            return;
        }
        ProtocolManager manager = protocolManager();
        if (manager == null) {
            return;
        }
        try {
            movementListener = new PacketAdapter(
                    plugin,
                    ListenerPriority.NORMAL,
                    PacketType.Play.Server.ENTITY_TELEPORT,
                    PacketType.Play.Server.REL_ENTITY_MOVE,
                    PacketType.Play.Server.REL_ENTITY_MOVE_LOOK,
                    PacketType.Play.Server.ENTITY_LOOK,
                    PacketType.Play.Server.ENTITY_HEAD_ROTATION,
                    PacketType.Play.Server.ENTITY_VELOCITY
            ) {
                @Override
                public void onPacketSending(PacketEvent event) {
                    if (displayEntityIds.isEmpty()) {
                        return;
                    }
                    Integer entityId = event.getPacket().getIntegers().readSafely(0);
                    if (entityId != null && displayEntityIds.contains(entityId)) {
                        event.setCancelled(true);
                    }
                }
            };
            manager.addPacketListener(movementListener);
        } catch (RuntimeException | LinkageError error) {
            movementListener = null;
            plugin.getLogger().log(Level.FINE, "Unable to silence money nametag movement.", error);
        }
    }

    private int[] passengerIds(Player owner, TextDisplay display) {
        java.util.List<Entity> riders = owner.getPassengers();
        int[] ids = new int[riders.size() + 1];
        for (int index = 0; index < riders.size(); index++) {
            ids[index] = riders.get(index).getEntityId();
        }
        ids[riders.size()] = display.getEntityId();
        return ids;
    }

    private boolean sharesWorld(Player viewer, Player owner) {
        return viewer.getWorld().equals(owner.getWorld());
    }

    private ProtocolManager protocolManager() {
        if (protocolManager == null) {
            try {
                protocolManager = ProtocolLibrary.getProtocolManager();
            } catch (RuntimeException | LinkageError ignored) {
                return null;
            }
        }
        return protocolManager;
    }

    /** The players who currently want to see balances, resolved once per pass. */
    private Set<UUID> viewers() {
        Set<UUID> viewers = new HashSet<>();
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (isEnabledFor(viewer)) {
                viewers.add(viewer.getUniqueId());
            }
        }
        return viewers;
    }

    /** The Bukkit view range is a multiplier of the 64 block default rather than a distance. */
    private float viewRange() {
        double blocks = Math.max(1.0D, config().getDouble("MONEY-NAMETAGS.VIEW-RANGE", 32.0D));
        return (float) (blocks / 64.0D);
    }

    private TextDisplay display(UUID ownerUuid) {
        UUID displayUuid = displays.get(ownerUuid);
        if (displayUuid == null) {
            return null;
        }
        Entity entity = Bukkit.getEntity(displayUuid);
        if (entity instanceof TextDisplay display && display.isValid()) {
            return display;
        }
        displays.remove(ownerUuid, displayUuid);
        return null;
    }

    private FileConfiguration config() {
        return plugin.getConfigManager().getConfig();
    }
}
