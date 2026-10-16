package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.TitleUtils;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Watches outgoing title packets so a later restore of the default timings does not land on an
 * animation another plugin is already showing.
 */
public final class TitleProtocolLibBridge {

    private final UltimateDonutSmp plugin;

    public TitleProtocolLibBridge(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        if (!plugin.getServer().getPluginManager().isPluginEnabled("ProtocolLib")) {
            return;
        }

        PacketType[] types = supportedTitlePackets();
        if (types.length == 0) {
            return;
        }

        try {
            ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(
                    plugin,
                    ListenerPriority.MONITOR,
                    types
            ) {
                @Override
                public void onPacketSending(PacketEvent event) {
                    if (event.isCancelled()) {
                        return;
                    }
                    Player player;
                    try {
                        player = event.getPlayer();
                    } catch (Throwable ignored) {
                        return;
                    }
                    if (player == null) {
                        return;
                    }
                    TitleUtils.noteForeignTitlePacket(player.getUniqueId());
                }
            });
            TitleUtils.watchForeignTitles();
        } catch (Throwable throwable) {
            plugin.getLogger().log(Level.WARNING, "Failed to register the title timing listener", throwable);
        }
    }

    private static PacketType[] supportedTitlePackets() {
        List<PacketType> types = new ArrayList<>();
        addIfSupported(types, PacketType.Play.Server.SET_TITLE_TEXT);
        addIfSupported(types, PacketType.Play.Server.SET_SUBTITLE_TEXT);
        addIfSupported(types, PacketType.Play.Server.SET_TITLES_ANIMATION);
        addIfSupported(types, PacketType.Play.Server.CLEAR_TITLES);
        addIfSupported(types, PacketType.Play.Server.TITLE);
        return types.toArray(PacketType[]::new);
    }

    private static void addIfSupported(List<PacketType> types, PacketType type) {
        try {
            if (type != null && type.isSupported()) {
                types.add(type);
            }
        } catch (Throwable ignored) {
        }
    }
}
