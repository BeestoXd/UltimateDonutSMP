package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedServerPing;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Dresses the status ping while maintenance mode is active, so a closed server says so in the
 * multiplayer list instead of looking open right up until someone tries to join.
 *
 * <p>The MOTD is the part Bukkit could do on its own. The label that replaces the player count
 * cannot: it is the version string, and the client only draws it once the protocol number fails to
 * match its own, so both are set together here.
 */
public final class MaintenanceProtocolLibBridge {

    /** Token replaced in any configured line with the time left before maintenance lifts. */
    static final String TIME_PLACEHOLDER = "%time%";

    /**
     * No client speaks this protocol, which is what makes the version string surface in place of
     * the player count instead of being hidden behind a version match.
     */
    private static final int UNMATCHABLE_PROTOCOL = -1;

    private static final List<String> DEFAULT_LINES =
            List.of("&cCurrently under maintenance", "&bCome back in: &d%time%");
    private static final List<String> DEFAULT_LINES_WITHOUT_TIMER =
            List.of("&cCurrently under maintenance", "&7come back later");

    private final UltimateDonutSmp plugin;
    private boolean loggedFailure;

    public MaintenanceProtocolLibBridge(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        if (!plugin.getServer().getPluginManager().isPluginEnabled("ProtocolLib")) {
            return;
        }

        try {
            ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
            protocolManager.addPacketListener(new PacketAdapter(
                    plugin,
                    ListenerPriority.HIGH,
                    PacketType.Status.Server.SERVER_INFO
            ) {
                @Override
                public void onPacketSending(PacketEvent event) {
                    onServerInfo(event);
                }
            });
        } catch (Throwable throwable) {
            plugin.getLogger().log(Level.WARNING, "Failed to register the maintenance server list listener", throwable);
        }
    }

    private void onServerInfo(PacketEvent event) {
        if (event.isCancelled()) {
            return;
        }

        MaintenanceManager maintenanceManager = plugin.getMaintenanceManager();
        if (maintenanceManager == null || !maintenanceManager.isMaintenanceActive()) {
            return;
        }

        FileConfiguration network = plugin.getConfigManager().getNetwork();
        if (!network.getBoolean("MAINTENANCE.SERVER_LIST.ENABLED", true)) {
            return;
        }

        try {
            WrappedServerPing ping = event.getPacket().getServerPings().read(0);
            if (ping == null) {
                return;
            }
            apply(ping, network, maintenanceManager.getRemainingSeconds());
            // Recent server versions hand out an immutable status object, so the wrapper collects
            // the edits and only the write-back puts them on the packet.
            event.getPacket().getServerPings().write(0, ping);
        } catch (Throwable throwable) {
            logOnce("Could not rewrite the server list ping for maintenance mode", throwable);
        }
    }

    /**
     * A ping is answered for every client that opens its server list, so a fault that repeats
     * would fill the console with the same stack trace. One copy is enough to act on.
     */
    private void logOnce(String message, Throwable throwable) {
        if (loggedFailure) {
            return;
        }
        loggedFailure = true;
        plugin.getLogger().log(Level.WARNING, message, throwable);
    }

    private void apply(WrappedServerPing ping, FileConfiguration network, long remainingSeconds) {
        boolean counting = remainingSeconds != MaintenanceManager.NO_DEADLINE;
        List<String> lines = network.getStringList(
                counting ? "MAINTENANCE.SERVER_LIST.LINES" : "MAINTENANCE.SERVER_LIST.LINES_NO_TIMER");
        if (lines.isEmpty()) {
            // A network.yml written before this block existed still gets the entry rather than an
            // empty MOTD.
            lines = counting ? DEFAULT_LINES : DEFAULT_LINES_WITHOUT_TIMER;
        }
        ping.setMotD(ColorUtils.colorize(renderMotd(lines, remainingSeconds)));

        String versionLabel = network.getString("MAINTENANCE.SERVER_LIST.VERSION_LABEL", "&cMaintenance");
        if (versionLabel != null && !versionLabel.isBlank()) {
            ping.setVersionName(ColorUtils.colorize(applyCountdown(versionLabel, remainingSeconds)));
            ping.setVersionProtocol(UNMATCHABLE_PROTOCOL);
        }

        List<String> hoverLines = network.getStringList("MAINTENANCE.SERVER_LIST.HOVER");
        if (hoverLines.isEmpty()) {
            return;
        }

        // The hover box is carried by fake player entries, and how much a server accepts in one of
        // those names varies with its authlib. Losing the hover is a fair price; losing the MOTD
        // and the label with it would not be, so this part fails on its own.
        try {
            List<WrappedGameProfile> sample = new ArrayList<>(hoverLines.size());
            for (String line : hoverLines) {
                sample.add(new WrappedGameProfile(
                        UUID.randomUUID(),
                        ColorUtils.colorize(applyCountdown(line, remainingSeconds))
                ));
            }
            // Replacing the sample also keeps whoever is still on the server, staff working
            // through the maintenance included, out of the hover box.
            ping.setPlayers(sample);
        } catch (Throwable throwable) {
            logOnce("Could not put the maintenance text on the server list hover", throwable);
        }
    }

    /**
     * Joins the configured lines into the MOTD. The client draws the first two of them, so a
     * longer list costs nothing but is not shown either.
     */
    static String renderMotd(List<String> lines, long remainingSeconds) {
        if (lines == null || lines.isEmpty()) {
            return "";
        }
        StringBuilder motd = new StringBuilder();
        for (String line : lines) {
            if (motd.length() > 0) {
                motd.append('\n');
            }
            motd.append(applyCountdown(line, remainingSeconds));
        }
        return motd.toString();
    }

    static String applyCountdown(String line, long remainingSeconds) {
        if (line == null || line.isEmpty()) {
            return "";
        }
        if (!line.contains(TIME_PLACEHOLDER)) {
            return line;
        }
        String rendered = remainingSeconds == MaintenanceManager.NO_DEADLINE
                ? ""
                : MaintenanceManager.formatCountdown(remainingSeconds);
        return line.replace(TIME_PLACEHOLDER, rendered);
    }
}
