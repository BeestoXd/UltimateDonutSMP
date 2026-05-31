package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.EconomyManager;
import com.bx.ultimateDonutSmp.managers.LeaderboardManager;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import com.bx.ultimateDonutSmp.utils.PermissionUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShardAdminCommand implements CommandExecutor {

    private static final String PERMISSION = "ultimatedonutsmp.admin.shards";

    private final UltimateDonutSmp plugin;

    public ShardAdminCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!PermissionUtils.has(sender, PERMISSION)) {
            sender.sendMessage(ColorUtils.toComponent("&cNo permission."));
            return true;
        }

        Mode mode = Mode.fromCommand(command.getName());
        if (mode == null) {
            sender.sendMessage(ColorUtils.toComponent("&cUnknown shard admin command."));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ColorUtils.toComponent("&cUsage: /" + label + " <player> <amount>"));
            return true;
        }

        long amount;
        try {
            amount = NumberUtils.parseLong(args[1]);
        } catch (NumberFormatException exception) {
            sender.sendMessage(ColorUtils.toComponent("&cAmount must be a valid number."));
            return true;
        }

        if (mode == Mode.SET) {
            if (amount < 0L) {
                sender.sendMessage(ColorUtils.toComponent("&cAmount must be zero or greater."));
                return true;
            }
        } else if (amount <= 0L) {
            sender.sendMessage(ColorUtils.toComponent("&cAmount must be greater than zero."));
            return true;
        }

        EconomyManager.AccountReference account = plugin.getEconomyManager().resolveAccount(args[0]);
        if (account == null) {
            sender.sendMessage(ColorUtils.toComponent("&cPlayer not found."));
            return true;
        }

        PlayerData data = loadData(account);
        if (data == null) {
            sender.sendMessage(ColorUtils.toComponent("&cPlayer data could not be loaded."));
            return true;
        }

        long before = data.getShards();
        if (mode == Mode.REMOVE && before < amount) {
            sender.sendMessage(ColorUtils.toComponent("&c" + account.displayName()
                    + " only has " + formatShards(before) + "."));
            return true;
        }

        switch (mode) {
            case ADD -> data.addShards(amount);
            case REMOVE -> data.removeShards(amount);
            case SET -> data.setShards(amount);
        }

        plugin.getDatabaseManager().savePlayer(data);
        if (plugin.getLeaderboardManager() != null) {
            plugin.getLeaderboardManager().invalidate(LeaderboardManager.LeaderboardType.SHARDS);
        }

        long after = data.getShards();
        sender.sendMessage(ColorUtils.toComponent("&a" + mode.pastTense() + " shards for &f"
                + account.displayName() + "&a. &7Before: &f" + formatShards(before)
                + " &7After: &f" + formatShards(after)));

        Player target = Bukkit.getPlayer(account.uuid());
        if (target != null && !target.equals(sender)) {
            target.sendMessage(ColorUtils.toComponent("&aYour shards were updated by &f"
                    + sender.getName() + "&a. &7Balance: &f" + formatShards(after)));
        }

        return true;
    }

    private PlayerData loadData(EconomyManager.AccountReference account) {
        if (account.onlinePlayer() != null) {
            PlayerData data = plugin.getPlayerDataManager().get(account.onlinePlayer());
            return data == null ? plugin.getPlayerDataManager().loadOrCreate(account.onlinePlayer()) : data;
        }
        return plugin.getDatabaseManager().loadPlayer(account.uuid());
    }

    private String formatShards(long amount) {
        return plugin.getCurrencyManager().formatShards(amount);
    }

    private enum Mode {
        ADD("addshards", "Added"),
        REMOVE("removeshards", "Removed"),
        SET("setshards", "Set");

        private final String commandName;
        private final String pastTense;

        Mode(String commandName, String pastTense) {
            this.commandName = commandName;
            this.pastTense = pastTense;
        }

        static Mode fromCommand(String commandName) {
            for (Mode mode : values()) {
                if (mode.commandName.equalsIgnoreCase(commandName)) {
                    return mode;
                }
            }
            return null;
        }

        String pastTense() {
            return pastTense;
        }
    }
}
