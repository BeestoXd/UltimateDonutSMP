package com.bx.ultimateDonutSmp.models;

import org.bukkit.Location;

import java.util.UUID;

public class Home {

    private final UUID ownerUuid;
    private String name;
    private Location location;

    public Home(UUID ownerUuid, String name, Location location) {
        this.ownerUuid = ownerUuid;
        this.name = name;
        this.location = location;
    }

    public UUID getOwnerUuid() { return ownerUuid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }
}
