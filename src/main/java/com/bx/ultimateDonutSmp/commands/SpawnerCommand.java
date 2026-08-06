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
                sender.sendMessage("Use /" + label + " give <player> <type> [amount]");
                return true;
            }
            if (!PermissionUtils.has(sender, ADMIN_PERMISSION)) {
                sender.sendMessage(ColorUtils.toComponent("&cYou do not have permission to open the spawner admin panel."));
                return true;
            }

            plugin.getSpawnerManager().openPanel(player);
            return true;
        }

        return switch (args[0].toLowerCase(Locale.US)) {
            case "give" -> handleGive(sender, args);
            case "reload" -> handleReload(sender);
            case "panel" -> handlePanel(sender);
            case "info" -> handleInfo(sender);
            case "split" -> handleSplit(sender, args);
            case "remove", "forcebreak" -> handleRemove(sender);
            default -> sendUsage(sender, label);
        };
    }

    private boolean handleGive(CommandSender sender, String[] args) {
        if (!PermissionUtils.has(sender, ADMIN_PERMISSION)) {
            sender.sendMessage(ColorUtils.toComponent("&cYou do not have permission to give spawners."));
            return true;
        }
        if (args.length < 3) {
            sender.sendMessage(ColorUtils.toComponent("&cUsage: /spawner give <player> <type> [amount]"));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(ColorUtils.toComponent("&cPlayer '&f" + args[1] + "&c' must be online."));
            return true;
        }

        long amount;
        try {
            amount = args.length >= 4 ? NumberUtils.parseLong(args[3]) : 1L;
        } catch (NumberFormatException exception) {
            sender.sendMessage(ColorUtils.toComponent("&cAmount must be a valid positive number."));
            return true;
        }

        if (amount <= 0L) {
            sender.sendMessage(ColorUtils.toComponent("&cAmount must be greater than zero."));
            return true;
        }

        var result = plugin.getSpawnerManager().giveSpawner(target, args[2], amount);
        sender.sendMessage(ColorUtils.toComponent(result.message()));
        if (!sender.equals(target)) {
            target.sendMessage(ColorUtils.toComponent("&aYou received &f" + NumberUtils.format(amount)
                    + "x " + plugin.getSpawnerManager().getPlainTypeDisplayName(args[2]) + "&a."));
        }
        return true;
    }

    private boolean handleReload(CommandSender sender) {
        if (!PermissionUtils.has(sender, ADMIN_PERMISSION)) {
            sender.sendMessage(ColorUtils.toComponent("&cYou do not have permission to reload spawners."));
            return true;
        }

        plugin.getConfigManager().reloadSpawners();
        plugin.getSpawnerManager().reload();
        plugin.getAntiEspManager().reload();
        plugin.getAntiEspManager().refreshAllPlayers();
        sender.sendMessage(ColorUtils.toComponent("&aSpawner settings reloaded."));
        return true;
    }

    private boolean handlePanel(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Player only.");
            return true;
        }
        if (!PermissionUtils.has(sender, ADMIN_PERMISSION)) {
            sender.sendMessage(ColorUtils.toComponent("&cYou do not have permission to open the spawner panel."));
            return true;
        }

        plugin.getSpawnerManager().openPanel(player);
        return true;
    }

    private boolean handleInfo(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Player only.");
            return true;
        }

        Block target = player.getTargetBlockExact(6);
        SpawnerInstance instance = target == null ? null : plugin.getSpawnerManager().getSpawner(target);
        if (instance == null) {
            player.sendMessage(ColorUtils.toComponent("&cLook at a managed spawner to inspect it."));
            return true;
        }

        player.sendMessage(ColorUtils.toComponent("&8&m----------- &bSpawner Info &8&m-----------"));
        player.sendMessage(ColorUtils.toComponent("&7Type: &f" + plugin.getSpawnerManager().getPlainTypeDisplayName(instance.getMobTypeKey())));
        player.sendMessage(ColorUtils.toComponent("&7Owner: &f" + instance.getOwnerNameSnapshot()));
        player.sendMessage(ColorUtils.toComponent("&7Stack: &f" + NumberUtils.format(instance.getStackAmount())));
        player.sendMessage(ColorUtils.toComponent("&7Stored Loot: &f" + NumberUtils.format(instance.getTotalStoredItems())));
        player.sendMessage(ColorUtils.toComponent("&7Location: &f" + instance.getWorld() + " "
                + instance.getX() + ", " + instance.getY() + ", " + instance.getZ()));
        return true;
    }

    private boolean handleRemove(CommandSender sender) {
        if (!PermissionUtils.has(sender, ADMIN_PERMISSION)) {
            sender.sendMessage(ColorUtils.toComponent("&cYou do not have permission to remove spawners."));
            return true;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Player only.");
            return true;
        }

        Block target = player.getTargetBlockExact(6);
        SpawnerInstance instance = target == null ? null : plugin.getSpawnerManager().getSpawner(target);
        if (instance == null) {
            player.sendMessage(ColorUtils.toComponent("&cLook at a managed spawner to remove it."));
            return true;
        }

        target.setType(Material.AIR, false);
        player.sendMessage(ColorUtils.toComponent(plugin.getSpawnerManager().removeSpawner(instance, false, player).message()));
        return true;
    }

    private boolean handleSplit(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Player only.");
            return true;
        }

        ItemStack hand = player.getInventory().getItemInMainHand();
        if (!plugin.getSpawnerManager().isSpawnerItem(hand)) {
            player.sendMessage(ColorUtils.toComponent("&cYou must be holding a managed spawner item."));
            return true;
        }

        long currentAmount = plugin.getSpawnerManager().getSpawnerItemAmount(hand);
        if (currentAmount <= 1L) {
            player.sendMessage(ColorUtils.toComponent("&cThis spawner item cannot be split (amount is 1)."));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(ColorUtils.toComponent("&cUsage: /spawner split <amount>"));
            return true;
        }

        long splitAmount;
        try {
            splitAmount = NumberUtils.parseLong(args[1]);
        } catch (NumberFormatException exception) {
            player.sendMessage(ColorUtils.toComponent("&cInvalid split amount."));
            return true;
        }

        if (splitAmount <= 0L) {
            player.sendMessage(ColorUtils.toComponent("&cSplit amount must be greater than zero."));
            return true;
        }

        if (splitAmount >= currentAmount) {
            player.sendMessage(ColorUtils.toComponent("&cSplit amount must be less than the current stack size (&f" + NumberUtils.format(currentAmount) + "&c)."));
            return true;
        }

        String typeKey = plugin.getSpawnerManager().getSpawnerItemType(hand);
        ItemStack splitItem = plugin.getSpawnerManager().createSpawnerItem(typeKey, splitAmount);
        if (splitItem == null) {
            player.sendMessage(ColorUtils.toComponent("&cFailed to create split spawner item."));
            return true;
        }

        long remainingAmount = currentAmount - splitAmount;
        plugin.getSpawnerManager().updateSpawnerItemAmount(hand, remainingAmount);

        java.util.Map<Integer, ItemStack> leftovers = player.getInventory().addItem(splitItem);
        leftovers.values().forEach(leftover -> player.getWorld().dropItemNaturally(player.getLocation(), leftover));

        player.sendMessage(ColorUtils.toComponent("&aSplit &f" + NumberUtils.format(splitAmount) + "x &aspawners. &7Remaining in hand: &f" + NumberUtils.format(remainingAmount) + "&7."));
        return true;
    }

    private boolean sendUsage(CommandSender sender, String label) {
        sender.sendMessage(ColorUtils.toComponent("&8&m----------- &dSpawner &8&m-----------"));
        sender.sendMessage(ColorUtils.toComponent("&f/" + label + " &7- Open the spawner panel"));
        sender.sendMessage(ColorUtils.toComponent("&f/" + label + " info &7- Inspect the looked-at spawner"));
        sender.sendMessage(ColorUtils.toComponent("&f/" + label + " panel &7- Open the spawner admin panel"));
        sender.sendMessage(ColorUtils.toComponent("&f/" + label + " give <player> <type> [amount] &7- Give a spawner item"));
        sender.sendMessage(ColorUtils.toComponent("&f/" + label + " split <amount> &7- Split the held spawner item"));
        sender.sendMessage(ColorUtils.toComponent("&f/" + label + " reload &7- Reload spawner settings"));
        sender.sendMessage(ColorUtils.toComponent("&f/" + label + " remove &7- Remove the looked-at spawner"));
        return true;
    }
}
