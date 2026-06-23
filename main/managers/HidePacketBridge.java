package com.bx.ultimateDonutSmp.managers;

import org.bukkit.entity.Player;

import java.util.UUID;

interface HidePacketBridge {

    void refresh(Player target);

    default void refreshNametag(Player target) {
        refresh(target);
    }

    default void clear(UUID targetUuid) {
    }

    default void shutdown() {
    }
}
