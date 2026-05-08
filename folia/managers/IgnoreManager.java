package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.IgnoreEntry;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class IgnoreManager {

    public enum ToggleAction {
        ADDED,
        REMOVED,
        FAILED
    }

    public record ToggleResult(ToggleAction action, IgnoreEntry entry) {
    }

    private static final String BYPASS_PERMISSION = "ultimatedonutsmp.ignore.bypass";

    private final UltimateDonutSmp plugin;
    private final Map<UUID, Map<UUID, IgnoreEntry>> ignoredByOwner = new ConcurrentHashMap<>();

    public IgnoreManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public void loadPlayer(UUID ownerUuid) {
        if (ownerUuid == null) {
            return;
        }
        ignoredByOwner.put(ownerUuid, loadIgnoreMap(ownerUuid));
    }

    public void unloadPlayer(UUID ownerUuid) {
        if (ownerUuid != null) {
            ignoredByOwner.remove(ownerUuid);
        }
    }

    public void clear() {
        ignoredByOwner.clear();
    }

    public ToggleResult toggleIgnore(Player owner, UUID ignoredUuid, String ignoredName) {
        if (owner == null || ignoredUuid == null) {
            return new ToggleResult(ToggleAction.FAILED, null);
        }

        UUID ownerUuid = owner.getUniqueId();
        Map<UUID, IgnoreEntry> ignoredPlayers = getOrLoad(ownerUuid);
        IgnoreEntry existing = ignoredPlayers.get(ignoredUuid);
        if (existing != null) {
            boolean removed = removeIgnore(ownerUuid, ignoredUuid);
            return new ToggleResult(removed ? ToggleAction.REMOVED : ToggleAction.FAILED, existing);
        }

        IgnoreEntry entry = new IgnoreEntry(
                ownerUuid,
                ignoredUuid,
                normalizeName(ignoredName, ignoredUuid),
                System.currentTimeMillis()
        );
        boolean added = addIgnore(entry);
        return new ToggleResult(added ? ToggleAction.ADDED : ToggleAction.FAILED, entry);
    }

    public boolean addIgnore(IgnoreEntry entry) {
        if (entry == null || entry.ownerUuid() == null || entry.ignoredUuid() == null) {
            return false;
        }

        boolean saved = plugin.getDatabaseManager().addIgnoredPlayer(
                entry.ownerUuid(),
                entry.ignoredUuid(),
                normalizeName(entry.ignoredNameSnapshot(), entry.ignoredUuid()),
                entry.createdAt()
        );
        if (!saved) {
            return false;
        }

        getOrLoad(entry.ownerUuid()).put(entry.ignoredUuid(), entry);
        return true;
    }

    public boolean removeIgnore(UUID ownerUuid, UUID ignoredUuid) {
        if (ownerUuid == null || ignoredUuid == null) {
            return false;
        }

        boolean removed = plugin.getDatabaseManager().removeIgnoredPlayer(ownerUuid, ignoredUuid);
        if (!removed) {
            return false;
        }

        getOrLoad(ownerUuid).remove(ignoredUuid);
        return true;
    }

    public boolean isIgnoring(UUID ownerUuid, UUID senderUuid) {
        if (ownerUuid == null || senderUuid == null) {
            return false;
        }
        return getOrLoad(ownerUuid).containsKey(senderUuid);
    }

    public boolean canBypassIgnore(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            return true;
        }
        return player.hasPermission(BYPASS_PERMISSION);
    }

    public List<IgnoreEntry> getIgnoredPlayers(UUID ownerUuid) {
        if (ownerUuid == null) {
            return List.of();
        }

        List<IgnoreEntry> entries = new ArrayList<>(getOrLoad(ownerUuid).values());
        entries.sort(Comparator
                .comparing(IgnoreEntry::ignoredNameSnapshot, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(IgnoreEntry::createdAt));
        return entries;
    }

    private Map<UUID, IgnoreEntry> getOrLoad(UUID ownerUuid) {
        return ignoredByOwner.computeIfAbsent(ownerUuid, this::loadIgnoreMap);
    }

    private Map<UUID, IgnoreEntry> loadIgnoreMap(UUID ownerUuid) {
        Map<UUID, IgnoreEntry> ignoredPlayers = new ConcurrentHashMap<>();
        for (IgnoreEntry entry : plugin.getDatabaseManager().loadIgnoredPlayers(ownerUuid)) {
            ignoredPlayers.put(entry.ignoredUuid(), entry);
        }
        return ignoredPlayers;
    }

    private String normalizeName(String ignoredName, UUID ignoredUuid) {
        if (ignoredName != null && !ignoredName.isBlank()) {
            return ignoredName.trim();
        }

        String lastKnownName = plugin.getDatabaseManager().getLastKnownUsername(ignoredUuid);
        if (lastKnownName != null && !lastKnownName.isBlank()) {
            return lastKnownName;
        }

        return ignoredUuid.toString();
    }
}
