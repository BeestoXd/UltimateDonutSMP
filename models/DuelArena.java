package com.bx.ultimateDonutSmp.models;

import org.bukkit.Location;

public class DuelArena {

    private final String id;
    private String displayName;
    private Location spawn1;
    private Location spawn2;
    private Location returnLocation;
    private Location regionPos1;
    private Location regionPos2;
    private boolean enabled;
    private boolean queueEnabled;
    private boolean noHunger;
    private boolean noWeather;
    private boolean alwaysMorning;
    private boolean noFallDamage;

    public DuelArena(String id,
                     String displayName,
                     Location spawn1,
                     Location spawn2,
                     Location returnLocation,
                     Location regionPos1,
                     Location regionPos2,
                     boolean enabled,
                     boolean queueEnabled,
                     boolean noHunger,
                     boolean noWeather,
                     boolean alwaysMorning,
                     boolean noFallDamage) {
        this.id = id;
        this.displayName = displayName;
        this.spawn1 = cloneLocation(spawn1);
        this.spawn2 = cloneLocation(spawn2);
        this.returnLocation = cloneLocation(returnLocation);
        this.regionPos1 = cloneLocation(regionPos1);
        this.regionPos2 = cloneLocation(regionPos2);
        this.enabled = enabled;
        this.queueEnabled = queueEnabled;
        this.noHunger = noHunger;
        this.noWeather = noWeather;
        this.alwaysMorning = alwaysMorning;
        this.noFallDamage = noFallDamage;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Location getSpawn1() {
        return cloneLocation(spawn1);
    }

    public void setSpawn1(Location spawn1) {
        this.spawn1 = cloneLocation(spawn1);
    }

    public Location getSpawn2() {
        return cloneLocation(spawn2);
    }

    public void setSpawn2(Location spawn2) {
        this.spawn2 = cloneLocation(spawn2);
    }

    public Location getReturnLocation() {
        return cloneLocation(returnLocation);
    }

    public void setReturnLocation(Location returnLocation) {
        this.returnLocation = cloneLocation(returnLocation);
    }

    public Location getRegionPos1() {
        return cloneLocation(regionPos1);
    }

    public void setRegionPos1(Location regionPos1) {
        this.regionPos1 = cloneLocation(regionPos1);
    }

    public Location getRegionPos2() {
        return cloneLocation(regionPos2);
    }

    public void setRegionPos2(Location regionPos2) {
        this.regionPos2 = cloneLocation(regionPos2);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isQueueEnabled() {
        return queueEnabled;
    }

    public void setQueueEnabled(boolean queueEnabled) {
        this.queueEnabled = queueEnabled;
    }

    public boolean isNoHunger() {
        return noHunger;
    }

    public void setNoHunger(boolean noHunger) {
        this.noHunger = noHunger;
    }

    public boolean isNoWeather() {
        return noWeather;
    }

    public void setNoWeather(boolean noWeather) {
        this.noWeather = noWeather;
    }

    public boolean isAlwaysMorning() {
        return alwaysMorning;
    }

    public void setAlwaysMorning(boolean alwaysMorning) {
        this.alwaysMorning = alwaysMorning;
    }

    public boolean isNoFallDamage() {
        return noFallDamage;
    }

    public void setNoFallDamage(boolean noFallDamage) {
        this.noFallDamage = noFallDamage;
    }

    public boolean isReady() {
        return isValid(spawn1) && isValid(spawn2);
    }

    public boolean hasRollbackRegion() {
        return isValid(regionPos1)
                && isValid(regionPos2)
                && regionPos1.getWorld().getName().equalsIgnoreCase(regionPos2.getWorld().getName());
    }

    private boolean isValid(Location location) {
        return location != null && location.getWorld() != null;
    }

    private Location cloneLocation(Location location) {
        return location == null ? null : location.clone();
    }
}
