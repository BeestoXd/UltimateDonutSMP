package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.EnumSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerVisibilityManager {

    public enum Reason {
        PLAYER_SETTING,
        DUEL,
        FFA,
        STAFF_VANISH
    }

    private final UltimateDonutSmp plugin;
    private final Map<VisibilityKey, EnumSet<Reason>> hiddenReasons = new ConcurrentHashMap<>();

    public PlayerVisibilityManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public void hide(Player viewer, Player target, Reason reason) {
        if (!valid(viewer, target) || reason == null) {
            return;
        }
        VisibilityKey key = new VisibilityKey(viewer.getUniqueId(), target.getUniqueId());
        hiddenReasons.compute(key, (ignored, current) -> {
            EnumSet<Reason> updated = current == null
                    ? EnumSet.noneOf(Reason.class)
                    : EnumSet.copyOf(current);
            updated.add(reason);
            return updated;
        });
        apply(viewer, target);
    }

    public void show(Player viewer, Player target, Reason reason) {
        if (!valid(viewer, target) || reason == null) {
            return;
        }
        VisibilityKey key = new VisibilityKey(viewer.getUniqueId(), target.getUniqueId());
        hiddenReasons.computeIfPresent(key, (ignored, current) -> {
            EnumSet<Reason> updated = EnumSet.copyOf(current);
            updated.remove(reason);
            return updated.isEmpty() ? null : updated;
        });
        apply(viewer, target);
    }

    public void applyViewerPreference(Player viewer) {
        if (viewer == null || !viewer.isOnline()) {
            return;
        }
        PlayerData data = plugin.getPlayerDataManager().get(viewer);
        boolean hidden = data != null && data.isHideAllPlayersEnabled();
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (viewer.getUniqueId().equals(target.getUniqueId())) {
                continue;
            }
            if (hidden) {
                hide(viewer, target, Reason.PLAYER_SETTING);
            } else {
                show(viewer, target, Reason.PLAYER_SETTING);
            }
        }
    }

    public void handleJoin(Player joined) {
        if (joined == null) {
            return;
        }
        applyViewerPreference(joined);
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (viewer.getUniqueId().equals(joined.getUniqueId())) {
                continue;
            }
            PlayerData data = plugin.getPlayerDataManager().get(viewer);
            if (data != null && data.isHideAllPlayersEnabled()) {
                hide(viewer, joined, Reason.PLAYER_SETTING);
            }
        }
    }

    public void refresh(Player viewer, Player target) {
        if (valid(viewer, target)) {
            apply(viewer, target);
        }
    }

    public boolean isHidden(Player viewer, Player target) {
        if (!valid(viewer, target)) {
            return false;
        }
        EnumSet<Reason> reasons = hiddenReasons.get(
                new VisibilityKey(viewer.getUniqueId(), target.getUniqueId())
        );
        return reasons != null && !reasons.isEmpty();
    }

    public void clearPlayer(UUID playerId) {
        if (playerId == null) {
            return;
        }
        hiddenReasons.keySet().removeIf(key ->
                key.viewerId().equals(playerId) || key.targetId().equals(playerId));
    }

    public void clear() {
        hiddenReasons.clear();
    }

    private void apply(Player viewer, Player target) {
        plugin.getSpigotScheduler().runEntity(viewer, () -> {
            if (!valid(viewer, target)) {
                return;
            }
            if (isHidden(viewer, target)) {
                viewer.hidePlayer(plugin, target);
            } else {
                viewer.showPlayer(plugin, target);
            }
        });
    }

    private boolean valid(Player viewer, Player target) {
        return viewer != null
                && target != null
                && viewer.isOnline()
                && target.isOnline()
                && !viewer.getUniqueId().equals(target.getUniqueId());
    }

    private record VisibilityKey(UUID viewerId, UUID targetId) {
    }
}
