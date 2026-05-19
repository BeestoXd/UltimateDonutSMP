package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.utils.PermissionUtils;

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
            sender.sendMessage("ᴘʟᴀʏᴇʀ ᴏɴʟʏ.");
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
            if (data == null) { player.sendMessage(ColorUtils.toComponent("&cᴘʟᴀʏᴇʀ ɴᴏᴛ ꜰᴏᴜɴᴅ.")); return true; }
            String msg = plugin.getConfigManager().getMessage("BALANCE.OTHER-SHARDS",
                    "{player}", target.getName() != null ? target.getName() : args[0],
                    "{amount}", String.valueOf(data.getShards()));
            player.sendMessage(ColorUtils.toComponent(msg));
        }
        return true;
    }

    private boolean handleEverywhere(CommandSender sender, String label, String[] args) {
        if (!PermissionUtils.has(sender, ADMIN_PERMISSION)) {
            sender.sendMessage(ColorUtils.toComponent("&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ ᴛᴏ ɪɴѕᴘᴇᴄᴛ ѕʜᴀʀᴅѕ ᴇᴠᴇʀʏᴡʜᴇʀᴇ."));
            return true;
        }

        if (args.length < 2 || (!args[1].equalsIgnoreCase("status") && !args[1].equalsIgnoreCase("debug"))) {
            sender.sendMessage(ColorUtils.toComponent("&cᴜѕᴀɢᴇ: /" + label + " ᴇᴠᴇʀʏᴡʜᴇʀᴇ <status|debug> [player]"));
            return true;
        }

        Player target;
        if (args.length >= 3) {
            target = Bukkit.getPlayerExact(args[2]);
        } else if (sender instanceof Player player) {
            target = player;
        } else {
            sender.sendMessage(ColorUtils.toComponent("&cᴜѕᴀɢᴇ: /" + label + " ᴇᴠᴇʀʏᴡʜᴇʀᴇ <status|debug> <player>"));
            return true;
        }

        if (target == null || !target.isOnline()) {
            sender.sendMessage(ColorUtils.toComponent("&cᴛᴀʀɢᴇᴛ ᴘʟᴀʏᴇʀ ᴍᴜѕᴛ ʙᴇ ᴏɴʟɪɴᴇ ꜰᴏʀ ѕʜᴀʀᴅѕ ᴇᴠᴇʀʏᴡʜᴇʀᴇ ᴄʜᴇᴄᴋѕ."));
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
        boolean disableWhileInShardCuboid = shardManager.isEverywheredisabledWhileInShardCuboid();
        boolean booster = shardManager.hasBooster(target.getUniqueId());
        int multiplier = shardManager.getMultiplier(target.getUniqueId());
        ShardManager.EverywhereEligibilityResult eligibility = shardManager.getEverywhereEligibility(target);

        sender.sendMessage(ColorUtils.toComponent("&8&m--------------------------------"));
        sender.sendMessage(ColorUtils.toComponent("&#A303F9ѕʜᴀʀᴅѕ ᴇᴠᴇʀʏᴡʜᴇʀᴇ &7ꜰᴏʀ &b" + target.getName()));
        sender.sendMessage(ColorUtils.toComponent("&7ᴇɴᴀʙʟᴇᴅ: " + yesNo(shardManager.isEverywhereEnabled())));
        sender.sendMessage(ColorUtils.toComponent("&7ᴇʟɪɢɪʙʟᴇ ɴᴏᴡ: " + eligibilityColor(eligibility) + formatEligibility(eligibility)));
        sender.sendMessage(ColorUtils.toComponent("&7ʀᴇǫᴜɪʀᴇᴅ ᴘᴇʀᴍɪѕѕɪᴏɴ: &f" + (requiredPermission != null ? requiredPermission : "<none>")));
        sender.sendMessage(ColorUtils.toComponent("&7ʜᴀѕ ᴘᴇʀᴍɪѕѕɪᴏɴ: " + yesNo(hasPermission)));
        sender.sendMessage(ColorUtils.toComponent("&7ᴡᴏʀʟᴅ: &f" + target.getWorld().getName()));
        sender.sendMessage(ColorUtils.toComponent("&7ᴡᴏʀʟᴅ ᴇxᴄʟᴜᴅᴇᴅ: " + yesNo(excludedWorld)));
        sender.sendMessage(ColorUtils.toComponent("&7ᴀꜰᴋ: " + yesNo(afk)));
        sender.sendMessage(ColorUtils.toComponent("&7ʀᴇᴄᴇɴᴛ ᴍᴏᴠᴇᴍᴇɴᴛ: " + yesNo(recentMovement)));
        sender.sendMessage(ColorUtils.toComponent("&7ᴅɪѕᴀʙʟᴇ ɪɴ ѕʜᴀʀᴅ ᴄᴜʙᴏɪᴅ: " + yesNo(disableWhileInShardCuboid)));
        sender.sendMessage(ColorUtils.toComponent("&7ɪɴ ѕʜᴀʀᴅ ᴄᴜʙᴏɪᴅ: " + yesNo(inShardCuboid)));
        sender.sendMessage(ColorUtils.toComponent("&7ɪɴᴛᴇʀᴠᴀʟ: &f" + shardManager.getEverywhereEveryMinutes() + " ᴍɪɴᴜᴛᴇ(ѕ)"));
        sender.sendMessage(ColorUtils.toComponent("&7ᴀᴍᴏᴜɴᴛ: &#A303F9" + shardManager.getEverywhereAmount()));
        sender.sendMessage(ColorUtils.toComponent("&7ʙᴏᴏѕᴛᴇʀ ᴀᴄᴛɪᴠᴇ: " + yesNo(booster)));
        sender.sendMessage(ColorUtils.toComponent("&7ᴍᴜʟᴛɪᴘʟɪᴇʀ: &f" + multiplier + "x"));

        if (debug) {
            long secondsSinceMovement = plugin.getAFKManager().getSecondsSinceLastMovement(target.getUniqueId());
            sender.sendMessage(ColorUtils.toComponent("&7ᴍᴏᴠᴇᴍᴇɴᴛ ᴡɪɴᴅᴏᴡ: &f" + shardManager.getEverywhereRecentMovementWindowSeconds() + "s"));
            sender.sendMessage(ColorUtils.toComponent("&7ѕᴇᴄᴏɴᴅѕ ѕɪɴᴄᴇ ᴍᴏᴠᴇᴍᴇɴᴛ: &f" + secondsSinceMovement));
            sender.sendMessage(ColorUtils.toComponent("&7ᴄᴜʀʀᴇɴᴛ ѕʜᴀʀᴅѕ: &#A303F9" + getCurrentShards(target)));
        }

        sender.sendMessage(ColorUtils.toComponent("&8&m--------------------------------"));
    }

    private long getCurrentShards(Player target) {
        PlayerData data = plugin.getPlayerDataManager().get(target);
        return data != null ? data.getShards() : 0L;
    }

    private String yesNo(boolean value) {
        return value ? "&aʏᴇѕ" : "&cɴᴏ";
    }

    private String eligibilityColor(ShardManager.EverywhereEligibilityResult result) {
        return result == ShardManager.EverywhereEligibilityResult.ELIGIBLE ? "&a" : "&c";
    }

    private String formatEligibility(ShardManager.EverywhereEligibilityResult result) {
        return switch (result) {
            case ELIGIBLE -> "Eligible";
            case DISABLED -> "ᴅɪѕᴀʙʟᴇᴅ";
            case NO_PERMISSION -> "ɴᴏ ᴘᴇʀᴍɪѕѕɪᴏɴ";
            case EXCLUDED_WORLD -> "ᴇxᴄʟᴜᴅᴇᴅ ᴡᴏʀʟᴅ";
            case AFK -> "AFK";
            case NO_RECENT_MOVEMENT -> "ɴᴏ ʀᴇᴄᴇɴᴛ ᴍᴏᴠᴇᴍᴇɴᴛ";
            case IN_SHARD_CUBOID -> "ɪɴ ѕʜᴀʀᴅ ᴄᴜʙᴏɪᴅ";
        };
    }
}
