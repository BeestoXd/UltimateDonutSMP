package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedNumberFormat;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Renders a player's balance on the line under their username.
 *
 * <p>This is the line Minecraft draws for a scoreboard objective in the {@code BELOW_NAME} slot, so
 * the client places it itself as part of the nametag. It cannot drift, lag behind a sprint or cover
 * the username, because it is drawn in the same pass as the name and always one line beneath it.</p>
 *
 * <p>The slot normally shows a raw score, which is an integer and no use for a balance in the
 * billions, so each score carries a fixed number format holding the text to draw instead. That is a
 * packet level feature with no Bukkit API, which is why everything here is sent through
 * ProtocolLib.</p>
 *
 * <p>Nothing is written to anybody's real scoreboard. Every packet goes to one viewer, so a player
 * who left the setting off never hears about the objective at all, and the balances a viewer sees
 * are decided entirely by which scores were sent to them.</p>
 *
 * <p>Two things about the slot are the client's rules rather than ours: it only draws within about
 * ten blocks, and a server can only show one objective there at a time.</p>
 */
public class MoneyNametagManager {

    private static final String OBJECTIVE = "uds_money";
    private static final String LEGACY_DISPLAY_TAG = "uds_money_nametag";
    private static final String BALANCE_PLACEHOLDER = "{balance}";

    private static final int OBJECTIVE_CREATE = 0;
    private static final int OBJECTIVE_REMOVE = 1;

    private final UltimateDonutSmp plugin;
    /** Viewer to the balance text each player was last sent, so unchanged lines are not resent. */
    private final Map<UUID, Map<UUID, String>> sentText = new ConcurrentHashMap<>();
    /** Viewers whose client has been told the objective exists. */
    private final Set<UUID> installed = ConcurrentHashMap.newKeySet();
    private ProtocolManager protocolManager;
    private boolean warnedUnsupported;
    private boolean warnedPacketFailure;

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

    /** Whether {@code viewer} asked to see balances under other players. */
    public boolean isEnabledFor(Player viewer) {
        if (viewer == null) {
            return false;
        }
        PlayerData data = plugin.getPlayerDataManager().get(viewer);
        return data != null && data.isMoneyNametagsEnabled();
    }

    /** Sends every viewer who wants balances the ones that have changed since their last update. */
    public void updateAll() {
        if (!isEnabled() || !isNumberFormatSupported()) {
            clearAll();
            return;
        }
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (isEnabledFor(viewer)) {
                push(viewer);
            } else {
                clearViewer(viewer);
            }
        }
    }

    /** Pushes {@code player}'s balance out again, and gives them the objective if they want one. */
    public void update(Player player) {
        if (player == null || !isEnabled() || !isNumberFormatSupported()) {
            return;
        }
        for (Map<UUID, String> known : sentText.values()) {
            known.remove(player.getUniqueId());
        }
        refreshViewer(player);
    }

    /** Installs or removes the objective on one player's client to match their own choice. */
    public void refreshViewer(Player viewer) {
        if (viewer == null) {
            return;
        }
        if (!isEnabled() || !isNumberFormatSupported() || !isEnabledFor(viewer)) {
            clearViewer(viewer);
            return;
        }
        push(viewer);
    }

    /** Drops everything remembered about a player, as a viewer and as somebody being viewed. */
    public void remove(UUID playerUuid) {
        if (playerUuid == null) {
            return;
        }
        installed.remove(playerUuid);
        sentText.remove(playerUuid);
        for (Map<UUID, String> known : sentText.values()) {
            known.remove(playerUuid);
        }
    }

    public void clearAll() {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            clearViewer(viewer);
        }
        installed.clear();
        sentText.clear();
    }

    public void reload() {
        clearAll();
        purgeOrphanedDisplays();
    }

    public void shutdown() {
        clearAll();
    }

    /**
     * Removes the floating text entities earlier versions used for this feature. They were spawned
     * non-persistent so a restart clears them on its own, but a plugin reload leaves the previous
     * run's entities behind.
     */
    public void purgeOrphanedDisplays() {
        for (World world : Bukkit.getWorlds()) {
            for (TextDisplay display : world.getEntitiesByClass(TextDisplay.class)) {
                if (display.getScoreboardTags().contains(LEGACY_DISPLAY_TAG)) {
                    display.remove();
                }
            }
        }
    }

    private void push(Player viewer) {
        if (!installed.contains(viewer.getUniqueId())) {
            if (!sendObjective(viewer, OBJECTIVE_CREATE) || !sendDisplaySlot(viewer)) {
                return;
            }
            installed.add(viewer.getUniqueId());
        }

        Map<UUID, String> known = sentText.computeIfAbsent(viewer.getUniqueId(), key -> new ConcurrentHashMap<>());
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (!shouldDisplayFor(target) || !viewer.canSee(target)) {
                known.remove(target.getUniqueId());
                continue;
            }
            String text = ColorUtils.colorize(currentText(target), target);
            if (!text.equals(known.get(target.getUniqueId())) && sendScore(viewer, target.getName(), text)) {
                known.put(target.getUniqueId(), text);
            }
        }
    }

    private void clearViewer(Player viewer) {
        sentText.remove(viewer.getUniqueId());
        if (installed.remove(viewer.getUniqueId())) {
            sendObjective(viewer, OBJECTIVE_REMOVE);
        }
    }

    private String currentText(Player target) {
        return render(
                config().getString("MONEY-NAMETAGS.FORMAT", "&a${balance}"),
                plugin.getEconomyManager().getBalance(target),
                config().getBoolean("MONEY-NAMETAGS.SHORT-FORMAT", true));
    }

    /** Hidden players keep their balance to themselves. */
    private boolean shouldDisplayFor(Player target) {
        HideManager hideManager = plugin.getHideManager();
        return hideManager == null || !hideManager.isHidden(target.getUniqueId());
    }

    private boolean sendObjective(Player viewer, int method) {
        ProtocolManager manager = protocolManager();
        if (manager == null) {
            return false;
        }
        try {
            PacketContainer packet = manager.createPacket(PacketType.Play.Server.SCOREBOARD_OBJECTIVE);
            packet.getStrings().write(0, OBJECTIVE);
            packet.getChatComponents().writeSafely(0, WrappedChatComponent.fromText(""));
            packet.getRenderTypes().writeSafely(0, EnumWrappers.RenderType.INTEGER);
            packet.getIntegers().write(0, method);
            emptyEveryOptional(packet);
            return send(viewer, packet);
        } catch (RuntimeException | LinkageError error) {
            warnPacketFailure("Unable to set up the money nametag objective.", error);
            return false;
        }
    }

    private boolean sendDisplaySlot(Player viewer) {
        ProtocolManager manager = protocolManager();
        if (manager == null) {
            return false;
        }
        try {
            PacketContainer packet = manager.createPacket(PacketType.Play.Server.SCOREBOARD_DISPLAY_OBJECTIVE);
            packet.getDisplaySlots().write(0, EnumWrappers.DisplaySlot.BELOW_NAME);
            packet.getStrings().write(0, OBJECTIVE);
            return send(viewer, packet);
        } catch (RuntimeException | LinkageError error) {
            warnPacketFailure("Unable to place the money nametag under the name.", error);
            return false;
        }
    }

    /**
     * The score itself stays at zero and never reaches the screen. What the client draws is the
     * fixed number format carried alongside it, which is where the formatted balance lives.
     */
    private boolean sendScore(Player viewer, String targetName, String text) {
        ProtocolManager manager = protocolManager();
        if (manager == null) {
            return false;
        }
        try {
            PacketContainer packet = manager.createPacket(PacketType.Play.Server.SCOREBOARD_SCORE);
            packet.getStrings().write(0, targetName);
            packet.getStrings().write(1, OBJECTIVE);
            packet.getIntegers().write(0, 0);
            emptyEveryOptional(packet);
            writeNumberFormat(packet, WrappedNumberFormat.fixed(WrappedChatComponent.fromLegacyText(text)));
            return send(viewer, packet);
        } catch (RuntimeException | LinkageError error) {
            warnPacketFailure("Unable to send a money nametag balance.", error);
            return false;
        }
    }

    /**
     * Empties every optional field on a freshly built packet. ProtocolLib fills the ones it has no
     * default for with a bare {@link Object}, which survives right up to the moment Minecraft tries
     * to encode it as whatever the field really holds and throws instead.
     */
    private void emptyEveryOptional(PacketContainer packet) {
        StructureModifier<Object> optionals = packet.getModifier().withType(Optional.class);
        for (int index = 0; index < optionals.size(); index++) {
            optionals.writeSafely(index, Optional.empty());
        }
    }

    /**
     * Writes the format into whichever optional field actually holds one. The scoreboard packets
     * carry more than one optional and their order is Minecraft's business, so the field is picked
     * by what it declares rather than by counting.
     */
    private void writeNumberFormat(PacketContainer packet, WrappedNumberFormat format) {
        StructureModifier<Optional<WrappedNumberFormat>> optionals =
                packet.getOptionals(BukkitConverters.getWrappedNumberFormatConverter());
        List<FieldAccessor> fields = optionals.getFields();
        for (int index = 0; index < fields.size(); index++) {
            if (holdsNumberFormat(fields.get(index))) {
                optionals.write(index, Optional.of(format));
                return;
            }
        }
        throw new IllegalStateException("This server's score packet has nowhere to put a number format.");
    }

    private boolean holdsNumberFormat(FieldAccessor accessor) {
        return accessor.getField().getGenericType().getTypeName().contains("NumberFormat");
    }

    /**
     * The first packet that will not build or send is worth shouting about, since the line simply
     * fails to appear and there is nothing else to go on. Everything after it stays quiet.
     */
    private void warnPacketFailure(String message, Throwable error) {
        if (warnedPacketFailure) {
            plugin.getLogger().log(Level.FINE, message, error);
            return;
        }
        warnedPacketFailure = true;
        plugin.getLogger().log(Level.WARNING, message + " Money nametags may not show up.", error);
    }

    private boolean send(Player viewer, PacketContainer packet) {
        ProtocolManager manager = protocolManager();
        if (manager == null || viewer == null || !viewer.isOnline()) {
            return false;
        }
        try {
            manager.sendServerPacket(viewer, packet, false);
            return true;
        } catch (RuntimeException | LinkageError error) {
            warnPacketFailure("Unable to send a money nametag packet.", error);
            return false;
        }
    }

    /**
     * Without fixed number formats the slot can only draw a raw integer, which no balance worth
     * showing fits into, so the feature stays off rather than printing a wrong number.
     */
    private boolean isNumberFormatSupported() {
        try {
            if (WrappedNumberFormat.isSupported()) {
                return true;
            }
        } catch (RuntimeException | LinkageError ignored) {
            // Treated the same as an unsupported server below.
        }
        if (!warnedUnsupported) {
            warnedUnsupported = true;
            plugin.getLogger().warning("Money nametags need a server that supports scoreboard number"
                    + " formats (Minecraft 1.20.3 or newer). The feature will stay off.");
        }
        return false;
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

    private FileConfiguration config() {
        return plugin.getConfigManager().getConfig();
    }
}
