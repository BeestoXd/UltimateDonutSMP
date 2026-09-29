package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.utils.PermissionUtils;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.SpawnerInstance;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Locale;

public class SpawnerCommand implements CommandExecutor {

    private static final String ADMIN_PERMISSION = "ultimatedonutsmp.admin.spawner";

    private final UltimateDonutSmp plugin;

    public SpawnerCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getSpawnerManager().getMessage("GIVE-USAGE", "Use /{label} give <player> <type> [amount]", "{label}", label));
                return true;
            }
            if (!PermissionUtils.has(sender, ADMIN_PERMISSION)) {
                sender.sendMessage(ColorUtils.toComponent(plugin.getSpawnerManager().getMessage("NO-PANEL-PERMISSION", "&cYou do not have permission to open the spawner admin panel.")));
                return true;
            }

            plugin.getSpawnerManager().openPanel(player);
            return true;
        }

        return switch (args[0].toLowerCase(Locale.US)) {
            case "give" -> handleGive(sender, args, label);
            case "reload" -> handleReload(sender);
            case "panel" -> handlePanel(sender);
            case "info" -> handleInfo(sender);
            case "split" -> handleSplit(sender, args);
            case "remove", "forcebreak" -> handleRemove(sender);
            default -> sendUsage(sender, label);
        };
    }

    private boolean handleGive(CommandSender sender, String[] args, String label) {
        if (!PermissionUtils.has(sender, ADMIN_PERMISSION)) {
            sender.sendMessage(ColorUtils.toComponent(plugin.getSpawnerManager().getMessage("NO-GIVE-PERMISSION", "&cYou do not have permission to give spawners.")));
            return true;
        }
        if (args.length < 3) {
            sender.sendMessage(ColorUtils.toComponent(plugin.getSpawnerManager().getMessage("GIVE-USAGE-COMMAND", "&cUsage: /spawner give <player> <type> [amount]", "{label}", label)));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(ColorUtils.toComponent(plugin.getSpawnerManager().getMessage("PLAYER-NOT-ONLINE", "&cPlayer '&f{player}&c' must be online.", "{player}", args[1])));
            return true;
        }

        long amount;
        try {
            amount = args.length >= 4 ? NumberUtils.parseLong(args[3]) : 1L;
        } catch (NumberFormatException exception) {
            sender.sendMessage(ColorUtils.toComponent(plugin.getSpawnerManager().getMessage("AMOUNT-INVALID", "&cAmount must be a valid positive number.")));
            return true;
        }

        if (amount <= 0L) {
            sender.sendMessage(ColorUtils.toComponent(plugin.getSpawnerManager().getMessage("AMOUNT-GREATER-THAN-ZERO", "&cAmount must be greater than zero.")));
            return true;
        }

        var result = plugin.getSpawnerManager().giveSpawner(target, args[2], amount);
        sender.sendMessage(ColorUtils.toComponent(result.message()));
        if (!sender.equals(target)) {
            target.sendMessage(ColorUtils.toComponent(plugin.getSpawnerManager().getMessage("RECEIVED-SPAWNER", "&aYou received &f{amount}x {type}&a.",
                    "{amount}", NumberUtils.format(amount),
                    "{type}", plugin.getSpawnerManager().getPlainTypeDisplayName(args[2]))));
        }
        return true;
    }

    private boolean handleReload(CommandSender sender) {
        if (!PermissionUtils.has(sender, ADMIN_PERMISSION)) {
            sender.sendMessage(ColorUtils.toComponent(plugin.getSpawnerManager().getMessage("NO-RELOAD-PERMISSION", "&cYou do not have permission to reload spawners.")));
            return true;
        }

        plugin.getConfigManager().reloadSpawners();
        plugin.getSpawnerManager().reload();
        sender.sendMessage(ColorUtils.toComponent(plugin.getSpawnerManager().getMessage("RELOADED", "&aSpawner settings reloaded.")));
        return true;
    }

    private boolean handlePanel(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getSpawnerManager().getMessage("PLAYER-ONLY", "Player only."));
            return true;
        }
        if (!PermissionUtils.has(sender, ADMIN_PERMISSION)) {
            sender.sendMessage(ColorUtils.toComponent(plugin.getSpawnerManager().getMessage("NO-PANEL-PERMISSION", "&cYou do not have permission to open the spawner panel.")));
            return true;
        }

        plugin.getSpawnerManager().openPanel(player);
        return true;
    }

    private boolean handleInfo(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getSpawnerManager().getMessage("PLAYER-ONLY", "Player only."));
            return true;
        }

        Block target = player.getTargetBlockExact(6);
        SpawnerInstance instance = target == null ? null : plugin.getSpawnerManager().getSpawner(target);
        if (instance == null) {
            player.sendMessage(ColorUtils.toComponent(plugin.getSpawnerManager().getMessage("LOOK-AT-SPAWNER-INSPECT", "&cLook at a managed spawner to inspect it.")));
            return true;
        }

        player.sendMessage(ColorUtils.toComponent(plugin.getSpawnerManager().getMessage("INFO-HEADER", "&8&m----------- &bSpawner Info &8&m-----------")));
        player.sendMessage(ColorUtils.toComponent(plugin.getSpawnerManager().getMessage("INFO-TYPE", "&7Type: &f{type}",
                "{type}", plugin.getSpawnerManager().getPlainTypeDisplayName(instance.getMobTypeKey()))));
        if (!plugin.getSpawnerManager().canOpen(player, instance)) {
            player.sendMessage(ColorUtils.toComponent(plugin.getSpawnerManager().getMessage("INFO-REST-HIDDEN", "&7The rest is hidden on spawners you cannot access.")));
            return true;
        }
        player.sendMessage(ColorUtils.toComponent(plugin.getSpawnerManager().getMessage("INFO-OWNER", "&7Owner: &f{owner}",
                "{owner}", instance.getOwnerNameSnapshot())));
        player.sendMessage(ColorUtils.toComponent(plugin.getSpawnerManager().getMessage("INFO-STACK", "&7Stack: &f{amount}",
                "{amount}", NumberUtils.format(instance.getStackAmount()))));
        player.sendMessage(ColorUtils.toComponent(plugin.getSpawnerManager().getMessage("INFO-STORED-LOOT", "&7Stored Loot: &f{amount}",
                "{amount}", NumberUtils.format(instance.getTotalStoredItems()))));
        player.sendMessage(ColorUtils.toComponent(plugin.getSpawnerManager().getMessage("INFO-LOCATION", "&7Location: &f{world} {x}, {y}, {z}",
                "{world}", instance.getWorld(),
                "{x}", String.valueOf(instance.getX()),
                "{y}", String.valueOf(instance.getY()),
                "{z}", String.valueOf(instance.getZ()))));
        return true;
    }

    private boolean handleRemove(CommandSender sender) {
        if (!PermissionUtils.has(sender, ADMIN_PERMISSION)) {
            sender.sendMessage(ColorUtils.toComponent(plugin.getSpawnerManager().getMessage("NO-REMOVE-PERMISSION", "&cYou do not have permission to remove spawners.")));
            return true;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getSpawnerManager().getMessage("PLAYER-ONLY", "Player only."));
            return true;
        }

        Block target = player.getTargetBlockExact(6);
        SpawnerInstance instance = target == null ? null : plugin.getSpawnerManager().getSpawner(target);
        if (instance == null) {
            player.sendMessage(ColorUtils.toComponent(plugin.getSpawnerManager().getMessage("LOOK-AT-SPAWNER-REMOVE", "&cLook at a managed spawner to remove it.")));
            return true;
        }

        target.setType(Material.AIR, false);
        player.sendMessage(ColorUtils.toComponent(plugin.getSpawnerManager().removeSpawner(instance, false, player).message()));
        return true;
    }

    private boolean handleSplit(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getSpawnerManager().getMessage("PLAYER-ONLY", "Player only."));
            return true;
        }

        ItemStack hand = player.getInventory().getItemInMainHand();
        if (!plugin.getSpawnerManager().isSpawnerItem(hand)) {
            player.sendMessage(ColorUtils.toComponent(plugin.getSpawnerManager().getMessage("HOLD-SPAWNER-TO-SPLIT", "&cYou must be holding a managed spawner item.")));
            return true;
        }

        long currentAmount = plugin.getSpawnerManager().getSpawnerItemAmount(hand);
        if (currentAmount <= 1L) {
            player.sendMessage(ColorUtils.toComponent(plugin.getSpawnerManager().getMessage("CANNOT-SPLIT-ONE", "&cThis spawner item cannot be split (amount is 1).")));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(ColorUtils.toComponent(plugin.getSpawnerManager().getMessage("SPLIT-USAGE", "&cUsage: /spawner split <amount>")));
            return true;
        }

        long splitAmount;
        try {
            splitAmount = NumberUtils.parseLong(args[1]);
        } catch (NumberFormatException exception) {
            player.sendMessage(ColorUtils.toComponent(plugin.getSpawnerManager().getMessage("INVALID-SPLIT-AMOUNT", "&cInvalid split amount.")));
            return true;
        }

        if (splitAmount <= 0L) {
            player.sendMessage(ColorUtils.toComponent(plugin.getSpawnerManager().getMessage("SPLIT-GREATER-THAN-ZERO", "&cSplit amount must be greater than zero.")));
            return true;
        }

        if (splitAmount >= currentAmount) {
            player.sendMessage(ColorUtils.toComponent(plugin.getSpawnerManager().getMessage("SPLIT-LESS-THAN-CURRENT", "&cSplit amount must be less than the current stack size (&f{max}&c).",
                    "{max}", NumberUtils.format(currentAmount))));
            return true;
        }

        String typeKey = plugin.getSpawnerManager().getSpawnerItemType(hand);
        ItemStack splitItem = plugin.getSpawnerManager().createSpawnerItem(typeKey, splitAmount);
        if (splitItem == null) {
            player.sendMessage(ColorUtils.toComponent(plugin.getSpawnerManager().getMessage("SPLIT-FAILED", "&cFailed to create split spawner item.")));
            return true;
        }

        long remainingAmount = currentAmount - splitAmount;
        plugin.getSpawnerManager().updateSpawnerItemAmount(hand, remainingAmount);

        java.util.Map<Integer, ItemStack> leftovers = player.getInventory().addItem(splitItem);
        leftovers.values().forEach(leftover -> player.getWorld().dropItemNaturally(player.getLocation(), leftover));

        player.sendMessage(ColorUtils.toComponent(plugin.getSpawnerManager().getMessage("SPLIT-SUCCESS", "&aSplit &f{amount}x &aspawners. &7Remaining in hand: &f{remaining}&7.",
                "{amount}", NumberUtils.format(splitAmount),
                "{remaining}", NumberUtils.format(remainingAmount))));
        return true;
    }

    private boolean sendUsage(CommandSender sender, String label) {
        List<String> defaultUsage = List.of(
                "&8&m----------- &dSpawner &8&m-----------",
                "&f/" + label + " &7- Open the spawner panel",
                "&f/" + label + " info &7- Inspect the looked-at spawner",
                "&f/" + label + " panel &7- Open the spawner admin panel",
                "&f/" + label + " give <player> <type> [amount] &7- Give a spawner item",
                "&f/" + label + " split <amount> &7- Split the held spawner item",
                "&f/" + label + " reload &7- Reload spawner settings",
                "&f/" + label + " remove &7- Remove the looked-at spawner"
        );
        List<String> lines = plugin.getSpawnerManager().getMessageList("USAGE", defaultUsage, "{label}", label);
        for (String line : lines) {
            sender.sendMessage(ColorUtils.toComponent(line));
        }
        return true;
    }
}
