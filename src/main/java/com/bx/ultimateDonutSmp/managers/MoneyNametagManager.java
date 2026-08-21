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
 * <p>The line is a {@link TextDisplay} that follows its owner around rather than a passenger riding
 * them: Spigot refuses to teleport a player who is carrying passengers, so mounting the line would
 * quietly break every warp, home and teleport request on the server.</p>
 *
 * <p>Every player decides for themselves whether they see the line through
 * {@code /settings > Money Nametags}, so a line only exists while somebody online has that setting
 * switched on, and it is only sent to the players who asked for it.</p>
 */
public class MoneyNametagManager {

    private static final String DISPLAY_TAG = "uds_money_nametag";
    private static final String BALANCE_PLACEHOLDER = "{balance}";

    private final UltimateDonutSmp plugin;
    private final Map<UUID, UUID> displays = new ConcurrentHashMap<>();
    private final Map<UUID, String> renderedText = new ConcurrentHashMap<>();

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
        return Math.max(1L, config().getLong("MONEY-NAMETAGS.UPDATE-INTERVAL-TICKS", 2L));
    }

    /** Whether {@code viewer} asked to see balances above other players. */
    public boolean isEnabledFor(Player viewer) {
        if (viewer == null) {
            return false;
        }
        PlayerData data = plugin.getPlayerDataManager().get(viewer);
        return data != null && data.isMoneyNametagsEnabled();
    }

    /** Moves every line to its owner and refreshes the balance written on it. */
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
        move(display, anchor(owner));
        applyVisibility(display, owner, viewers);
    }

    private String currentText(Player owner) {
        return render(
                config().getString("MONEY-NAMETAGS.FORMAT", "&a${balance}"),
                plugin.getEconomyManager().getBalance(owner),
                config().getBoolean("MONEY-NAMETAGS.SHORT-FORMAT", false));
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
                textDisplay.setTeleportDuration(teleportDuration());
            });
            renderedText.remove(owner.getUniqueId());
            displays.put(owner.getUniqueId(), display.getUniqueId());
            return display;
        } catch (RuntimeException error) {
            plugin.getLogger().warning("Unable to spawn a money nametag for " + owner.getName() + ".");
            return null;
        }
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

    private void move(TextDisplay display, Location location) {
        if (plugin.getSpigotScheduler().isFolia()) {
            plugin.getSpigotScheduler().teleport(display, location);
            return;
        }
        display.teleport(location);
    }

    private Location anchor(Player owner) {
        Location location = owner.getLocation();
        return new Location(
                owner.getWorld(),
                location.getX(),
                location.getY() + config().getDouble("MONEY-NAMETAGS.Y-OFFSET", 1.85D),
                location.getZ());
    }

    /** The Bukkit view range is a multiplier of the 64 block default rather than a distance. */
    private float viewRange() {
        double blocks = Math.max(1.0D, config().getDouble("MONEY-NAMETAGS.VIEW-RANGE", 32.0D));
        return (float) (blocks / 64.0D);
    }

    /**
     * Interpolating the move over the gap between updates is what keeps the line glued to a running
     * player instead of snapping along behind them.
     */
    private int teleportDuration() {
        return (int) Math.min(59L, getUpdateIntervalTicks());
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
