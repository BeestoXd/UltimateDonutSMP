package com.bx.ultimateDonutSmp.managers;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class GodModeManager {

    private final Set<UUID> activePlayers = new HashSet<>();

    public boolean isInGodMode(UUID uuid) {
        return uuid != null && activePlayers.contains(uuid);
    }

    public boolean toggle(UUID uuid) {
        if (uuid == null) {
            return false;
        }

        if (activePlayers.remove(uuid)) {
            return false;
        }

        activePlayers.add(uuid);
        return true;
    }

    public void clear(UUID uuid) {
        if (uuid != null) {
            activePlayers.remove(uuid);
        }
    }

    public void clearAll() {
        activePlayers.clear();
    }
}
