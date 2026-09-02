package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedServerPing;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.logging.Level;

/**
 * Puts the configured MOTD on the status ping, so what a server writes in config.yml is what the
 * multiplayer list shows. server.properties holds one line, takes no colour codes in the form the
 * rest of the plugin uses, and only changes on a restart.
 *
 * <p>Maintenance dresses the same entry from {@link MaintenanceProtocolLibBridge}, at a higher
 * listener priority. That alone would settle who wins, but the check here says so outright rather
 * than leaving it to the order the two happen to be registered in.
 */
public final class ServerListProtocolLibBridge {

    /** Token replaced in any configured line with the number of players on the server. */
    static final String ONLINE_PLACEHOLDER = "%online%";

    /** Token replaced with the slot count. Named as the tablist header names it. */
    static final String MAX_PLAYERS_PLACEHOLDER = "%max_players%";

    private final UltimateDonutSmp plugin;
    private boolean loggedFailure;

    public ServerListProtocolLibBridge(UltimateDonutSmp plugin) {
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
                    ListenerPriority.NORMAL,
                    PacketType.Status.Server.SERVER_INFO
            ) {
                @Override
                public void onPacketSending(PacketEvent event) {
                    onServerInfo(event);
                }
            });
        } catch (Throwable throwable) {
            plugin.getLogger().log(Level.WARNING, "Failed to register the server list MOTD listener", throwable);
        }
    }

    private void onServerInfo(PacketEvent event) {
        if (event.isCancelled()) {
            return;
        }

        FileConfiguration config = plugin.getConfigManager().getConfig();
        if (!config.getBoolean("SERVER-LIST.ENABLED", false)) {
            return;
        }

        if (maintenanceOwnsTheEntry()) {
            return;
        }

        List<String> lines = config.getStringList("SERVER-LIST.MOTD");
        if (lines.isEmpty()) {
            // Nothing written to show, so the server.properties line stays rather than the entry
            // going blank on a server that only emptied the list.
            return;
        }

        try {
            WrappedServerPing ping = event.getPacket().getServerPings().read(0);
            if (ping == null) {
                return;
            }
            String motd = renderMotd(lines, Bukkit.getOnlinePlayers().size(), Bukkit.getMaxPlayers());
            ping.setMotD(ColorUtils.colorize(motd));
            // Recent server versions hand out an immutable status object, so the wrapper collects
            // the edits and only the write-back puts them on the packet.
            event.getPacket().getServerPings().write(0, ping);
        } catch (Throwable throwable) {
            logOnce("Could not put the configured MOTD on the server list ping", throwable);
        }
    }

    /**
     * True only while maintenance is both active and rewriting the entry itself. With its
     * SERVER_LIST block switched off the server was asked to look untouched from the outside, and
     * on a server that wrote an MOTD, untouched is that MOTD.
     */
    private boolean maintenanceOwnsTheEntry() {
        MaintenanceManager maintenanceManager = plugin.getMaintenanceManager();
        if (maintenanceManager == null || !maintenanceManager.isMaintenanceActive()) {
            return false;
        }
        return plugin.getConfigManager().getNetwork().getBoolean("MAINTENANCE.SERVER_LIST.ENABLED", true);
    }

    /**
     * A ping is answered for every client that opens its server list, so a fault that repeats would
     * fill the console with the same stack trace. One copy is enough to act on.
     */
    private void logOnce(String message, Throwable throwable) {
        if (loggedFailure) {
            return;
        }
        loggedFailure = true;
        plugin.getLogger().log(Level.WARNING, message, throwable);
    }

    /**
     * Joins the configured lines into the MOTD. The client draws the first two of them, so a
     * longer list costs nothing but is not shown either.
     */
    static String renderMotd(List<String> lines, int online, int maxPlayers) {
        if (lines == null || lines.isEmpty()) {
            return "";
        }
        StringBuilder motd = new StringBuilder();
        for (String line : lines) {
            if (motd.length() > 0) {
                motd.append('\n');
            }
            motd.append(applyCounts(line, online, maxPlayers));
        }
        return motd.toString();
    }

    static String applyCounts(String line, int online, int maxPlayers) {
        if (line == null || line.isEmpty()) {
            return "";
        }
        return line
                .replace(ONLINE_PLACEHOLDER, Integer.toString(online))
                .replace(MAX_PLAYERS_PLACEHOLDER, Integer.toString(maxPlayers));
    }
}
