package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Renders a player's balance on a floating line under their username.
 *
 * <p>The line is a {@link TextDisplay} standing on its own rather than riding its owner. A ride
 * would pin it perfectly, but Minecraft refuses to draw a username on any player carrying a
 * passenger, so the ride costs the very name the balance is meant to sit under. Nothing here
 * touches the username: it keeps its normal place and the balance is parked below it.</p>
 *
 * <p>Standing on its own means the line has to be moved onto its owner, which happens every tick so
 * the server broadcasts it in the same breath as the player's own movement. The two then carry
 * matching interpolation and travel together instead of one chasing the other.</p>
 *
 * <p>Every player decides for themselves whether they see the line through
 * {@code /settings > Money Nametags}, so a line only exists while somebody online has that setting
 * switched on, and it is only sent to the players who asked for it.</p>
 */
public class MoneyNametagManager {

    private static final String DISPLAY_TAG = "uds_money_nametag";
    private static final String BALANCE_PLACEHOLDER = "{balance}";

    /** Vanilla hangs a username half a block above the player's height. */
    private static final double NAME_TAG_BASE = 0.5D;
    /** The username's own glyphs drop about a fifth of a block below where they hang from. */
    private static final double NAME_TAG_TEXT_HEIGHT = 0.2D;
    /** Display text is drawn at the same scale as a username, centred on the entity. */
    private static final double LINE_HALF_HEIGHT = 0.1D;
    /** Matches the three tick smoothing a client gives any other entity that moves. */
    private static final int LERP_TICKS = 3;

    private final UltimateDonutSmp plugin;
    private final Map<UUID, UUID> displays = new ConcurrentHashMap<>();
    private final Map<UUID, String> renderedText = new ConcurrentHashMap<>();
    private long tick;

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

    /** How often the balance written on a line is re-read, in ticks. */
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

    /**
     * Runs every tick. Moving each line onto its owner is cheap and has to happen at the rate the
     * player moves, while re-reading balances and re-checking who can see what is not, so that part
     * waits for the configured interval.
     */
    public void tick() {
        if (!isEnabled()) {
            clearAll();
            return;
        }
        Set<UUID> viewers = viewers();
        if (viewers.isEmpty()) {
            clearAll();
            return;
        }
        boolean full = tick++ % getUpdateIntervalTicks() == 0L;
        plugin.getSpigotScheduler().forEachOnlinePlayer(owner -> refresh(owner, viewers, full));
    }

    /** Brings one player's own line up to date, spawning it when somebody wants to see it. */
    public void update(Player owner) {
        if (!isEnabled()) {
            return;
        }
        Set<UUID> viewers = viewers();
        if (!viewers.isEmpty()) {
            refresh(owner, viewers, true);
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

    private void refresh(Player owner, Set<UUID> viewers, boolean full) {
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
            full = true;
        }

        follow(display, owner);
        if (!full) {
            return;
        }

        String text = ColorUtils.colorize(currentText(owner), owner);
        if (!text.equals(renderedText.get(owner.getUniqueId()))) {
            display.setText(text);
            renderedText.put(owner.getUniqueId(), text);
        }
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
                textDisplay.setTeleportDuration(LERP_TICKS);
            });
            renderedText.remove(owner.getUniqueId());
            displays.put(owner.getUniqueId(), display.getUniqueId());
            return display;
        } catch (RuntimeException error) {
            plugin.getLogger().warning("Unable to spawn a money nametag for " + owner.getName() + ".");
            return null;
        }
    }

    /** Moves the line onto its owner, skipping the work while neither of them has moved. */
    private void follow(TextDisplay display, Player owner) {
        Location anchor = anchor(owner);
        if (display.getLocation().distanceSquared(anchor) < 0.0001D) {
            return;
        }
        if (plugin.getSpigotScheduler().isFolia()) {
            plugin.getSpigotScheduler().teleport(display, anchor);
            return;
        }
        display.teleport(anchor);
    }

    /**
     * Sits the line directly under the username. The name hangs half a block above the player's
     * height and its glyphs drop from there, so the balance goes below all of that, its own half
     * height and the configured gap lower again.
     */
    private Location anchor(Player owner) {
        Location location = owner.getLocation();
        double gap = config().getDouble("MONEY-NAMETAGS.LINE-GAP", 0.05D);
        double height = location.getY() + owner.getHeight()
                + NAME_TAG_BASE - NAME_TAG_TEXT_HEIGHT - gap - LINE_HALF_HEIGHT;
        return new Location(owner.getWorld(), location.getX(), height, location.getZ());
    }

    /**
     * Only the players who asked for balances get the line sent to them, and only while they can
     * see its owner in the first place.
     */
    private void applyVisibility(TextDisplay display, Player owner, Set<UUID> viewers) {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (viewers.contains(viewer.getUniqueId()) && viewer.canSee(owner)) {
                viewer.showEntity(plugin, display);
            } else {
                viewer.hideEntity(plugin, display);
            }
        }
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
