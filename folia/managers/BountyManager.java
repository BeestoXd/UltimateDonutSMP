package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.Bounty;
import com.bx.ultimateDonutSmp.models.EconomyReason;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BountyManager {

    public enum PlacementResult {
        NEW,
        INCREASED,
        FAILED_SELF,
        FAILED_FUNDS
    }

    private final UltimateDonutSmp plugin;
    private final Map<UUID, Bounty> bounties = new HashMap<>();

    public BountyManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public void loadAll() {
        bounties.clear();
        for (Bounty bounty : plugin.getDatabaseManager().loadAllBounties()) {
            bounties.put(bounty.getTargetUuid(), bounty);
        }
    }

    public Bounty getBounty(UUID targetUuid) {
        return bounties.get(targetUuid);
    }

    public boolean hasBounty(UUID targetUuid) {
        return bounties.containsKey(targetUuid);
    }

    public Collection<Bounty> getAllBounties() {
        return bounties.values();
    }

    public UUID resolvePlayerUuid(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }

        Player online = Bukkit.getPlayerExact(input);
        if (online != null) {
            return online.getUniqueId();
        }

        UUID storedUuid = plugin.getDatabaseManager().findPlayerUuidByUsername(input);
        if (storedUuid != null) {
            return storedUuid;
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(input);
        if (offlinePlayer.isOnline() || offlinePlayer.hasPlayedBefore()) {
            return offlinePlayer.getUniqueId();
        }

        return null;
    }

    public String getDisplayName(UUID playerUuid) {
        if (playerUuid == null) {
            return "Unknown";
        }

        Player online = Bukkit.getPlayer(playerUuid);
        if (online != null) {
            return online.getName();
        }

        String offlineName = Bukkit.getOfflinePlayer(playerUuid).getName();
        if (offlineName != null && !offlineName.isBlank()) {
            return offlineName;
        }

        String storedName = plugin.getDatabaseManager().getLastKnownUsername(playerUuid);
        if (storedName != null && !storedName.isBlank()) {
            return storedName;
        }

        return "Unknown";
    }

    public PlacementResult placeBounty(Player placer, UUID targetUuid, double amount) {
        if (placer.getUniqueId().equals(targetUuid)) {
            return PlacementResult.FAILED_SELF;
        }

        PlayerData placerData = plugin.getPlayerDataManager().get(placer);
        if (placerData == null || !plugin.getEconomyManager().has(placer, amount)) {
            return PlacementResult.FAILED_FUNDS;
        }

        var withdrawResult = plugin.getEconomyManager().withdraw(placer, amount, EconomyReason.BOUNTY_PLACE);
        if (!withdrawResult.success()) {
            return PlacementResult.FAILED_FUNDS;
        }

        Bounty existing = bounties.get(targetUuid);
        PlacementResult result;
        if (existing != null) {
            existing.addAmount(amount);
            existing.setPlacerUuid(placer.getUniqueId());
            plugin.getDatabaseManager().saveBounty(existing);
            result = PlacementResult.INCREASED;
        } else {
            Bounty bounty = new Bounty(targetUuid, amount, placer.getUniqueId());
            bounties.put(targetUuid, bounty);
            plugin.getDatabaseManager().saveBounty(bounty);
            result = PlacementResult.NEW;
        }

        notifyTarget(placer, targetUuid, amount);
        broadcastPlacement(targetUuid, amount, result);
        return result;
    }

    private void notifyTarget(Player placer, UUID targetUuid, double amount) {
        Player target = Bukkit.getPlayer(targetUuid);
        if (target == null) {
            return;
        }

        PlayerData targetData = plugin.getPlayerDataManager().get(target);
        if (targetData == null || targetData.isBountyAlertsEnabled()) {
            String msg = plugin.getConfigManager().getMessage("BOUNTY.ALERT",
                    "{who}", placer.getName(),
                    "{price}", NumberUtils.format(amount));
            target.sendMessage(ColorUtils.toComponent(msg));
        }
    }

    private void broadcastPlacement(UUID targetUuid, double amount, PlacementResult result) {
        String messageKey = result == PlacementResult.NEW ? "BOUNTY.NEW" : "BOUNTY.INCREASED";
        String message = plugin.getConfigManager().getMessage(messageKey,
                "{player}", getDisplayName(targetUuid),
                "{price}", NumberUtils.format(amount));

        for (Player online : Bukkit.getOnlinePlayers()) {
            PlayerData data = plugin.getPlayerDataManager().get(online);
            if (data == null || data.isBountyAlertsEnabled()) {
                online.sendMessage(ColorUtils.toComponent(message));
            }
        }
    }

    public double claimBounty(Player killer, UUID targetUuid) {
        Bounty bounty = bounties.get(targetUuid);
        if (bounty == null) {
            return 0;
        }

        var depositResult = plugin.getEconomyManager().deposit(killer, bounty.getAmount(), EconomyReason.BOUNTY_REWARD);
        if (!depositResult.success()) {
            return 0;
        }

        bounties.remove(targetUuid);
        plugin.getDatabaseManager().deleteBounty(targetUuid);
        return bounty.getAmount();
    }

    public void removeBounty(UUID targetUuid) {
        bounties.remove(targetUuid);
        plugin.getDatabaseManager().deleteBounty(targetUuid);
    }

    public boolean isExcludedWorld(String worldName) {
        return plugin.getConfigManager().getConfig()
                .getStringList("BOUNTY.EXCLUDED-WORLDS")
                .contains(worldName);
    }

    public void clearAll() {
        bounties.clear();
    }
}
