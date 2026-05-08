package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.ShardManager;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShardsCommand implements CommandExecutor {

    private static final String ADMIN_PERMISSION = "ultimatedonutsmp.admin.shards";

    private final UltimateDonutSmp plugin;

    public ShardsCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("everywhere")) {
            return handleEverywhere(sender, label, args);
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Player only.");
            return true;
        }

        if (args.length == 0) {
            PlayerData data = plugin.getPlayerDataManager().get(player);
            if (data == null) return true;
            String msg = plugin.getConfigManager().getMessage("BALANCE.YOUR-SHARDS",
                    "{amount}", String.valueOf(data.getShards()));
            player.sendMessage(ColorUtils.toComponent(msg));
        } else {
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
            PlayerData data = plugin.getPlayerDataManager().get(target.getUniqueId());
            if (data == null) data = plugin.getDatabaseManager().loadPlayer(target.getUniqueId());
            if (data == null) { player.sendMessage(ColorUtils.toComponent("&cPlayer not found.")); return true; }
            String msg = plugin.getConfigManager().getMessage("BALANCE.OTHER-SHARDS",
                    "{player}", target.getName() != null ? target.getName() : args[0],
                    "{amount}", String.valueOf(data.getShards()));
            player.sendMessage(ColorUtils.toComponent(msg));
        }
        return true;
    }

    private boolean handleEverywhere(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission(ADMIN_PERMISSION)) {
            sender.sendMessage(ColorUtils.toComponent("&cYou do not have permission to inspect Shards Everywhere."));
            return true;
        }

        if (args.length < 2 || (!args[1].equalsIgnoreCase("status") && !args[1].equalsIgnoreCase("debug"))) {
            sender.sendMessage(ColorUtils.toComponent("&cUsage: /" + label + " everywhere <status|debug> [player]"));
            return true;
        }

        Player target;
        if (args.length >= 3) {
            target = Bukkit.getPlayerExact(args[2]);
        } else if (sender instanceof Player player) {
            target = player;
        } else {
            sender.sendMessage(ColorUtils.toComponent("&cUsage: /" + label + " everywhere <status|debug> <player>"));
            return true;
        }

        if (target == null || !target.isOnline()) {
            sender.sendMessage(ColorUtils.toComponent("&cTarget player must be online for Shards Everywhere checks."));
            return true;
        }

        boolean debug = args[1].equalsIgnoreCase("debug");
        sendEverywhereStatus(sender, target, debug);
        return true;
    }

    private void sendEverywhereStatus(CommandSender sender, Player target, boolean debug) {
        ShardManager shardManager = plugin.getShardManager();
        String requiredPermission = shardManager.getEverywhereRequiredPermission();
        boolean hasPermission = shardManager.hasEverywherePermission(target);
        boolean excludedWorld = shardManager.isEverywhereExcludedWorld(target.getWorld().getName());
        boolean afk = plugin.getAFKManager().isAfk(target.getUniqueId());
        boolean recentMovement = plugin.getAFKManager().hasRecentMovement(
                target.getUniqueId(),
                shardManager.getEverywhereRecentMovementWindowSeconds()
        );
        boolean inShardCuboid = shardManager.isInShardCuboid(target);
        boolean disableWhileInShardCuboid = shardManager.isEverywhereDisabledWhileInShardCuboid();
        boolean booster = shardManager.hasBooster(target.getUniqueId());
        int multiplier = shardManager.getMultiplier(target.getUniqueId());
        ShardManager.EverywhereEligibilityResult eligibility = shardManager.getEverywhereEligibility(target);

        sender.sendMessage(ColorUtils.toComponent("&8&m--------------------------------"));
        sender.sendMessage(ColorUtils.toComponent("&#A303F9Shards Everywhere &7for &b" + target.getName()));
        sender.sendMessage(ColorUtils.toComponent("&7Enabled: " + yesNo(shardManager.isEverywhereEnabled())));
        sender.sendMessage(ColorUtils.toComponent("&7Eligible now: " + eligibilityColor(eligibility) + formatEligibility(eligibility)));
        sender.sendMessage(ColorUtils.toComponent("&7Required permission: &f" + (requiredPermission != null ? requiredPermission : "<none>")));
        sender.sendMessage(ColorUtils.toComponent("&7Has permission: " + yesNo(hasPermission)));
        sender.sendMessage(ColorUtils.toComponent("&7World: &f" + target.getWorld().getName()));
        sender.sendMessage(ColorUtils.toComponent("&7World excluded: " + yesNo(excludedWorld)));
        sender.sendMessage(ColorUtils.toComponent("&7AFK: " + yesNo(afk)));
        sender.sendMessage(ColorUtils.toComponent("&7Recent movement: " + yesNo(recentMovement)));
        sender.sendMessage(ColorUtils.toComponent("&7Disable in shard cuboid: " + yesNo(disableWhileInShardCuboid)));
        sender.sendMessage(ColorUtils.toComponent("&7In shard cuboid: " + yesNo(inShardCuboid)));
        sender.sendMessage(ColorUtils.toComponent("&7Interval: &f" + shardManager.getEverywhereEveryMinutes() + " minute(s)"));
        sender.sendMessage(ColorUtils.toComponent("&7Amount: &#A303F9" + shardManager.getEverywhereAmount()));
        sender.sendMessage(ColorUtils.toComponent("&7Booster active: " + yesNo(booster)));
        sender.sendMessage(ColorUtils.toComponent("&7Multiplier: &f" + multiplier + "x"));

        if (debug) {
            long secondsSinceMovement = plugin.getAFKManager().getSecondsSinceLastMovement(target.getUniqueId());
            sender.sendMessage(ColorUtils.toComponent("&7Movement window: &f" + shardManager.getEverywhereRecentMovementWindowSeconds() + "s"));
            sender.sendMessage(ColorUtils.toComponent("&7Seconds since movement: &f" + secondsSinceMovement));
            sender.sendMessage(ColorUtils.toComponent("&7Current shards: &#A303F9" + getCurrentShards(target)));
        }

        sender.sendMessage(ColorUtils.toComponent("&8&m--------------------------------"));
    }

    private long getCurrentShards(Player target) {
        PlayerData data = plugin.getPlayerDataManager().get(target);
        return data != null ? data.getShards() : 0L;
    }

    private String yesNo(boolean value) {
        return value ? "&aYes" : "&cNo";
    }

    private String eligibilityColor(ShardManager.EverywhereEligibilityResult result) {
        return result == ShardManager.EverywhereEligibilityResult.ELIGIBLE ? "&a" : "&c";
    }

    private String formatEligibility(ShardManager.EverywhereEligibilityResult result) {
        return switch (result) {
            case ELIGIBLE -> "Eligible";
            case DISABLED -> "Disabled";
            case NO_PERMISSION -> "No permission";
            case EXCLUDED_WORLD -> "Excluded world";
            case AFK -> "AFK";
            case NO_RECENT_MOVEMENT -> "No recent movement";
            case IN_SHARD_CUBOID -> "In shard cuboid";
        };
    }
}
