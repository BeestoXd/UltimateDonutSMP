package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import com.bx.ultimateDonutSmp.utils.PacketBelowNameRenderer;
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
 * packet level feature with no Bukkit API behind it, which is why everything here is a packet.</p>
 *
 * <p>Nothing is written to anybody's real scoreboard. Every packet goes to one viewer, so a player
 * who left the setting off never hears about the objective at all, and the balances a viewer sees
 * are decided entirely by which scores were sent to them.</p>
 *
 * <p>Two things about the slot are the client's rules rather than ours: it only draws within about
 * ten blocks, and a server can only show one objective there at a time.</p>
 *
 * <p>ProtocolLib only knows how to build a packet for a Minecraft build it has been taught about,
 * and on a newer one it cannot assemble these at all. Rather than lose the feature until it catches
 * up, the first refusal moves everything onto {@link PacketBelowNameRenderer}, which reads the same
 * packets off the running server.</p>
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
    /** The standby path, used from the moment ProtocolLib cannot build one of these packets. */
    private final PacketBelowNameRenderer directPackets;
    private ProtocolManager protocolManager;
    private boolean warnedUnsupported;
    /** Set once ProtocolLib proves it cannot build this server's scoreboard packets. */
    private volatile boolean protocolLibUnusable;
    /** Set once neither path can build them, which is the only case that loses the feature. */
    private volatile boolean packetsUnsupported;

    public MoneyNametagManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
        this.directPackets = new PacketBelowNameRenderer(plugin, OBJECTIVE);
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
        if (!isEnabled() || !isPacketPathUsable()) {
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
        if (player == null || !isEnabled() || !isPacketPathUsable()) {
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
        if (!isEnabled() || !isPacketPathUsable() || !isEnabledFor(viewer)) {
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
                config().getString("MONEY-NAMETAGS.FORMAT", "&a$ &f{balance}"),
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
        if (manager != null && !protocolLibUnusable) {
            try {
                PacketContainer packet = manager.createPacket(PacketType.Play.Server.SCOREBOARD_OBJECTIVE);
                packet.getStrings().write(0, OBJECTIVE);
                packet.getChatComponents().writeSafely(0, WrappedChatComponent.fromText(""));
                packet.getRenderTypes().writeSafely(0, EnumWrappers.RenderType.INTEGER);
                packet.getIntegers().write(0, method);
                emptyEveryOptional(packet);
                return send(viewer, packet);
            } catch (RuntimeException | LinkageError error) {
                switchToDirectPackets("Unable to set up the money nametag objective.", error);
            }
        }
        return directPackets.sendObjective(viewer, method);
    }

    private boolean sendDisplaySlot(Player viewer) {
        ProtocolManager manager = protocolManager();
        if (manager != null && !protocolLibUnusable) {
            try {
                PacketContainer packet = manager.createPacket(PacketType.Play.Server.SCOREBOARD_DISPLAY_OBJECTIVE);
                packet.getDisplaySlots().write(0, EnumWrappers.DisplaySlot.BELOW_NAME);
                packet.getStrings().write(0, OBJECTIVE);
                return send(viewer, packet);
            } catch (RuntimeException | LinkageError error) {
                switchToDirectPackets("Unable to place the money nametag under the name.", error);
            }
        }
        return directPackets.sendDisplaySlot(viewer);
    }

    /**
     * The score itself stays at zero and never reaches the screen. What the client draws is the
     * fixed number format carried alongside it, which is where the formatted balance lives.
     */
    private boolean sendScore(Player viewer, String targetName, String text) {
        ProtocolManager manager = protocolManager();
        if (manager != null && !protocolLibUnusable) {
            try {
                PacketContainer packet = manager.createPacket(PacketType.Play.Server.SCOREBOARD_SCORE);
                packet.getStrings().write(0, targetName);
                packet.getStrings().write(1, OBJECTIVE);
                packet.getIntegers().write(0, 0);
                emptyEveryOptional(packet);
                writeNumberFormat(packet, WrappedNumberFormat.fixed(WrappedChatComponent.fromLegacyText(text)));
                return send(viewer, packet);
            } catch (RuntimeException | LinkageError error) {
                switchToDirectPackets("Unable to send a money nametag balance.", error);
            }
        }
        return directPackets.sendScore(viewer, targetName, text);
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
     * A packet that will not build or send means ProtocolLib does not recognise this server's
     * scoreboard packets, which nothing short of a ProtocolLib update will change. Everything moves
     * to the direct path from here, once, and the trace is kept at FINE for anyone debugging it.
     * Only a server where that path cannot run either loses the feature.
     */
    private void switchToDirectPackets(String message, Throwable error) {
        plugin.getLogger().log(Level.FINE, message, error);
        if (protocolLibUnusable) {
            return;
        }
        protocolLibUnusable = true;
        if (directPackets.isUsable()) {
            plugin.getLogger().info("ProtocolLib cannot build this server's scoreboard packets, so money"
                    + " nametags will be sent to players directly instead.");
            return;
        }
        packetsUnsupported = true;
        plugin.getLogger().warning("Money nametags need scoreboard packets that neither ProtocolLib nor"
                + " this server itself can build. The feature will stay off.");
    }

    /**
     * Every reason the packet path can be unusable, checked together everywhere the feature starts
     * work. Once these are known the answer cannot change while the server is running.
     */
    private boolean isPacketPathUsable() {
        if (packetsUnsupported || (protocolLibUnusable && !directPackets.isUsable())) {
            return false;
        }
        return isNumberFormatSupported();
    }

    private boolean send(Player viewer, PacketContainer packet) {
        ProtocolManager manager = protocolManager();
        if (manager == null || viewer == null || !viewer.isOnline()) {
            return false;
        }
        manager.sendServerPacket(viewer, packet, false);
        return true;
    }

    /**
     * Without fixed number formats the slot can only draw a raw integer, which no balance worth
     * showing fits into, so the feature stays off rather than printing a wrong number.
     */
    private boolean isNumberFormatSupported() {
        if (protocolLibNumberFormats() || directPackets.isUsable()) {
            return true;
        }
        if (!warnedUnsupported) {
            warnedUnsupported = true;
            plugin.getLogger().warning("Money nametags need a server that supports scoreboard number"
                    + " formats (Minecraft 1.20.3 or newer). The feature will stay off.");
        }
        return false;
    }

    private boolean protocolLibNumberFormats() {
        try {
            return !protocolLibUnusable && WrappedNumberFormat.isSupported();
        } catch (RuntimeException | LinkageError unsupportedServer) {
            return false;
        }
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
