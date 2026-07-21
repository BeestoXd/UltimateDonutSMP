package com.bx.ultimateDonutSmp.models;

import org.bukkit.Location;

import java.util.UUID;

public class Home {

    private final UUID ownerUuid;
    private String name;
    private Location location;
    private long createdAt;

    public Home(UUID ownerUuid, String name, Location location) {
        this(ownerUuid, name, location, System.currentTimeMillis());
    }

    public Home(UUID ownerUuid, String name, Location location, long createdAt) {
        this.ownerUuid = ownerUuid;
        this.name = name;
        this.location = location;
        this.createdAt = createdAt;
    }

    public UUID getOwnerUuid() { return ownerUuid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
