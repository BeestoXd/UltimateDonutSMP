package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PlayerData;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FastCrystalManager {

    private final UltimateDonutSmp plugin;
    private final Map<UUID, Long> lastPlaceAttempts = new HashMap<>();

    public FastCrystalManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public boolean isEnabled() {
        return plugin.getConfigManager().getConfig()
                .getBoolean("FAST-CRYSTALS.ENABLED", true);
    }

    public boolean isExcludedWorld(Player player) {
        return isExcludedWorld(player.getWorld().getName());
    }

    public boolean isExcludedWorld(String worldName) {
        for (String excluded : plugin.getConfigManager().getConfig()
                .getStringList("FAST-CRYSTALS.EXCLUDED-WORLDS")) {
            if (excluded.equalsIgnoreCase(worldName)) {
                return true;
            }
        }
        return false;
    }

    public boolean isPlayerToggleEnabled(Player player) {
        PlayerData data = plugin.getPlayerDataManager().get(player);
        if (data != null) {
            return data.isFastCrystalsEnabled();
        }

        return plugin.getConfigManager().getConfig()
                .getBoolean("FAST-CRYSTALS.DEFAULT-PLAYER-STATE", true);
    }

    public boolean isEnabledFor(Player player) {
        return isEnabled() && !isExcludedWorld(player) && isPlayerToggleEnabled(player);
    }

    public int getEnabledCooldownTicks() {
        return Math.max(0, plugin.getConfigManager().getConfig()
                .getInt("FAST-CRYSTALS.PLACE.ENABLED-COOLDOWN-TICKS", 0));
    }

    public int getDisabledCooldownTicks() {
        return Math.max(0, plugin.getConfigManager().getConfig()
                .getInt("FAST-CRYSTALS.PLACE.DISABLED-COOLDOWN-TICKS", 8));
    }

    public long getDebounceMillis() {
        return Math.max(0L, plugin.getConfigManager().getConfig()
                .getLong("FAST-CRYSTALS.PLACE.DEBOUNCE-MS", 40L));
    }

    public boolean shouldRequireValidBase() {
        return plugin.getConfigManager().getConfig()
                .getBoolean("FAST-CRYSTALS.PLACE.REQUIRE-VALID-BASE", true);
    }

    public boolean shouldRequireAirAbove() {
        return plugin.getConfigManager().getConfig()
                .getBoolean("FAST-CRYSTALS.PLACE.REQUIRE-AIR-ABOVE", true);
    }

    public boolean shouldRequireAirTwoAbove() {
        return plugin.getConfigManager().getConfig()
                .getBoolean("FAST-CRYSTALS.PLACE.REQUIRE-AIR-TWO-ABOVE", true);
    }

    public boolean shouldClearCooldownAfterHit() {
        return plugin.getConfigManager().getConfig()
                .getBoolean("FAST-CRYSTALS.BREAK.CLEAR-COOLDOWN-AFTER-HIT", true);
    }

    public int getCooldownTicksFor(Player player) {
        return isEnabledFor(player) ? getEnabledCooldownTicks() : getDisabledCooldownTicks();
    }

    public void applyCrystalCooldown(Player player) {
        player.setCooldown(Material.END_CRYSTAL, getCooldownTicksFor(player));
    }

    public void clearCrystalCooldown(Player player) {
        player.setCooldown(Material.END_CRYSTAL, 0);
    }

    public boolean tryBeginPlace(Player player, Block clickedBlock) {
        if (!isEnabledFor(player) || clickedBlock == null) {
            return false;
        }
        if (!hasValidPlacementContext(clickedBlock)) {
            return false;
        }

        long now = System.currentTimeMillis();
        Long lastAttempt = lastPlaceAttempts.get(player.getUniqueId());
        if (lastAttempt != null && now - lastAttempt < getDebounceMillis()) {
            return false;
        }

        lastPlaceAttempts.put(player.getUniqueId(), now);
        return true;
    }

    public boolean hasValidPlacementContext(Block clickedBlock) {
        return hasValidPlacementBase(clickedBlock) && hasRequiredPlacementSpace(clickedBlock);
    }

    public boolean hasValidPlacementBase(Block clickedBlock) {
        if (!shouldRequireValidBase()) {
            return true;
        }

        String blockName = clickedBlock.getType().name();
        for (String validBase : plugin.getConfigManager().getConfig()
                .getStringList("FAST-CRYSTALS.PLACE.VALID-BASES")) {
            if (validBase.equalsIgnoreCase(blockName)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasRequiredPlacementSpace(Block clickedBlock) {
        if (shouldRequireAirAbove() && !clickedBlock.getRelative(0, 1, 0).getType().isAir()) {
            return false;
        }
        if (shouldRequireAirTwoAbove() && !clickedBlock.getRelative(0, 2, 0).getType().isAir()) {
            return false;
        }
        return true;
    }

    public void clearState(UUID uuid) {
        lastPlaceAttempts.remove(uuid);
    }
}
