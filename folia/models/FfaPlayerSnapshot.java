package com.bx.ultimateDonutSmp.models;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FfaPlayerSnapshot {

    private final ItemStack[] storageContents;
    private final ItemStack[] armorContents;
    private final ItemStack offHand;
    private final double health;
    private final double absorptionAmount;
    private final int foodLevel;
    private final float saturation;
    private final float exhaustion;
    private final int level;
    private final float experienceProgress;
    private final int totalExperience;
    private final List<PotionEffect> potionEffects;
    private final Location returnLocation;
    private final GameMode gameMode;
    private final boolean allowFlight;
    private final boolean flying;
    private final int fireTicks;

    public FfaPlayerSnapshot(ItemStack[] storageContents,
                             ItemStack[] armorContents,
                             ItemStack offHand,
                             double health,
                             double absorptionAmount,
                             int foodLevel,
                             float saturation,
                             float exhaustion,
                             int level,
                             float experienceProgress,
                             int totalExperience,
                             Collection<PotionEffect> potionEffects,
                             Location returnLocation,
                             GameMode gameMode,
                             boolean allowFlight,
                             boolean flying,
                             int fireTicks) {
        this.storageContents = cloneItems(storageContents);
        this.armorContents = cloneItems(armorContents);
        this.offHand = offHand == null ? null : offHand.clone();
        this.health = Math.max(0D, health);
        this.absorptionAmount = Math.max(0D, absorptionAmount);
        this.foodLevel = Math.max(0, foodLevel);
        this.saturation = Math.max(0F, saturation);
        this.exhaustion = Math.max(0F, exhaustion);
        this.level = Math.max(0, level);
        this.experienceProgress = Math.max(0F, experienceProgress);
        this.totalExperience = Math.max(0, totalExperience);
        this.potionEffects = potionEffects == null ? List.of() : new ArrayList<>(potionEffects);
        this.returnLocation = returnLocation == null ? null : returnLocation.clone();
        this.gameMode = gameMode == null ? GameMode.SURVIVAL : gameMode;
        this.allowFlight = allowFlight;
        this.flying = flying;
        this.fireTicks = Math.max(0, fireTicks);
    }

    public static FfaPlayerSnapshot capture(Player player) {
        if (player == null) {
            return null;
        }

        PlayerInventory inventory = player.getInventory();
        double maxHealth = player.getAttribute(Attribute.MAX_HEALTH) == null
                ? 20D
                : player.getAttribute(Attribute.MAX_HEALTH).getValue();

        return new FfaPlayerSnapshot(
                inventory.getStorageContents(),
                inventory.getArmorContents(),
                inventory.getItemInOffHand(),
                Math.min(player.getHealth(), maxHealth),
                player.getAbsorptionAmount(),
                player.getFoodLevel(),
                player.getSaturation(),
                player.getExhaustion(),
                player.getLevel(),
                player.getExp(),
                player.getTotalExperience(),
                player.getActivePotionEffects(),
                player.getLocation(),
                player.getGameMode(),
                player.getAllowFlight(),
                player.isFlying(),
                player.getFireTicks()
        );
    }

    public ItemStack[] getStorageContents() {
        return cloneItems(storageContents);
    }

    public ItemStack[] getArmorContents() {
        return cloneItems(armorContents);
    }

    public ItemStack getOffHand() {
        return offHand == null ? null : offHand.clone();
    }

    public double getHealth() {
        return health;
    }

    public double getAbsorptionAmount() {
        return absorptionAmount;
    }

    public int getFoodLevel() {
        return foodLevel;
    }

    public float getSaturation() {
        return saturation;
    }

    public float getExhaustion() {
        return exhaustion;
    }

    public int getLevel() {
        return level;
    }

    public float getExperienceProgress() {
        return experienceProgress;
    }

    public int getTotalExperience() {
        return totalExperience;
    }

    public Collection<PotionEffect> getPotionEffects() {
        return new ArrayList<>(potionEffects);
    }

    public Location getReturnLocation() {
        return returnLocation == null ? null : returnLocation.clone();
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public boolean isAllowFlight() {
        return allowFlight;
    }

    public boolean isFlying() {
        return flying;
    }

    public int getFireTicks() {
        return fireTicks;
    }

    private static ItemStack[] cloneItems(ItemStack[] items) {
        if (items == null) {
            return new ItemStack[0];
        }

        ItemStack[] clone = new ItemStack[items.length];
        for (int i = 0; i < items.length; i++) {
            clone[i] = items[i] == null ? null : items[i].clone();
        }
        return clone;
    }
}
