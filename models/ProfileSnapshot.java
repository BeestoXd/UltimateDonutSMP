package com.bx.ultimateDonutSmp.models;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ProfileSnapshot {

    private final UUID uuid;
    private final String username;
    private final boolean online;
    private final PlayerData playerData;
    private final List<Home> homes;
    private final String teamName;
    private final Location currentLocation;
    private final boolean afk;

    public ProfileSnapshot(UUID uuid,
                           String username,
                           boolean online,
                           PlayerData playerData,
                           List<Home> homes,
                           String teamName,
                           Location currentLocation,
                           boolean afk) {
        this.uuid = uuid;
        this.username = username;
        this.online = online;
        this.playerData = playerData;
        this.homes = List.copyOf(homes == null ? List.of() : new ArrayList<>(homes));
        this.teamName = teamName;
        this.currentLocation = currentLocation == null ? null : currentLocation.clone();
        this.afk = afk;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }

    public boolean isOnline() {
        return online;
    }

    public PlayerData getPlayerData() {
        return playerData;
    }

    public List<Home> getHomes() {
        return homes;
    }

    public int getHomeCount() {
        return homes.size();
    }

    public String getTeamName() {
        return teamName;
    }

    public Location getCurrentLocation() {
        return currentLocation == null ? null : currentLocation.clone();
    }

    public boolean isAfk() {
        return afk;
    }

    public boolean hasCurrentLocation() {
        return currentLocation != null;
    }

    public boolean hasPlayerData() {
        return playerData != null;
    }
}
