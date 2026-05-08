package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.Home;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.models.ProfileSnapshot;
import com.bx.ultimateDonutSmp.models.Team;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ProfileViewerManager {

    public static final String VIEW_PERMISSION = "ultimatedonutsmp.staff.profileviewer";

    private final UltimateDonutSmp plugin;

    public ProfileViewerManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public boolean canView(Player viewer) {
        return viewer != null && viewer.hasPermission(VIEW_PERMISSION);
    }

    public Optional<ProfileSnapshot> resolveProfile(String username) {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }

        Player online = findOnlinePlayer(username);
        if (online != null) {
            return resolveProfile(online.getUniqueId());
        }

        UUID uuid = plugin.getDatabaseManager().findPlayerUuidByUsername(username);
        if (uuid == null) {
            return Optional.empty();
        }

        return resolveProfile(uuid);
    }

    public Optional<ProfileSnapshot> resolveProfile(UUID uuid) {
        if (uuid == null) {
            return Optional.empty();
        }

        Player online = Bukkit.getPlayer(uuid);
        boolean isOnline = online != null;

        PlayerData playerData = isOnline ? plugin.getPlayerDataManager().get(uuid) : null;
        if (playerData == null) {
            playerData = plugin.getDatabaseManager().loadPlayer(uuid);
        }

        String username = isOnline ? online.getName() : null;
        if ((username == null || username.isBlank()) && playerData != null) {
            username = playerData.getUsername();
        }
        if (username == null || username.isBlank()) {
            username = plugin.getDatabaseManager().getLastKnownUsername(uuid);
        }

        List<Home> homes = isOnline
                ? new ArrayList<>(plugin.getHomeManager().getHomes(uuid))
                : new ArrayList<>(plugin.getDatabaseManager().loadHomes(uuid));
        if (isOnline && homes.isEmpty()) {
            homes = new ArrayList<>(plugin.getDatabaseManager().loadHomes(uuid));
        }

        Team team = plugin.getTeamManager().getTeam(uuid);
        String teamName = team == null ? null : team.getName();

        boolean afk = isOnline && plugin.getAFKManager().isAfk(uuid);

        if ((username == null || username.isBlank())
                && playerData == null
                && homes.isEmpty()
                && teamName == null
                && online == null) {
            return Optional.empty();
        }

        if (username == null || username.isBlank()) {
            username = uuid.toString().substring(0, 8);
        }

        return Optional.of(new ProfileSnapshot(
                uuid,
                username,
                isOnline,
                playerData,
                homes,
                teamName,
                isOnline ? online.getLocation() : null,
                afk
        ));
    }

    public List<Home> getHomes(UUID uuid) {
        if (uuid == null) {
            return List.of();
        }

        Player online = Bukkit.getPlayer(uuid);
        if (online != null) {
            List<Home> cached = new ArrayList<>(plugin.getHomeManager().getHomes(uuid));
            if (!cached.isEmpty()) {
                return List.copyOf(cached);
            }
        }

        return List.copyOf(plugin.getDatabaseManager().loadHomes(uuid));
    }

    private Player findOnlinePlayer(String username) {
        Player exact = Bukkit.getPlayerExact(username);
        if (exact != null) {
            return exact;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getName().equalsIgnoreCase(username)) {
                return player;
            }
        }
        return null;
    }
}
