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
                sender.sendMessage("ᴜѕᴇ /" + label + " ɢɪᴠᴇ <player> <type> [amount]");
                return true;
            }
            if (!PermissionUtils.has(sender, ADMIN_PERMISSION)) {
                sender.sendMessage(ColorUtils.toComponent("&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ ᴛᴏ ᴏᴘᴇɴ ᴛʜᴇ ѕᴘᴀᴡɴᴇʀ ᴀᴅᴍɪɴ ᴘᴀɴᴇʟ."));
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
            sender.sendMessage(ColorUtils.toComponent("&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ ᴛᴏ ɢɪᴠᴇ ѕᴘᴀᴡɴᴇʀѕ."));
            return true;
        }
        if (args.length < 3) {
            sender.sendMessage(ColorUtils.toComponent("&cᴜѕᴀɢᴇ: /spawner give <player> <type> [amount]"));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(ColorUtils.toComponent("&cᴘʟᴀʏᴇʀ '&f" + args[1] + "&c' ᴍᴜѕᴛ ʙᴇ ᴏɴʟɪɴᴇ."));
            return true;
        }

        long amount;
        try {
            amount = args.length >= 4 ? NumberUtils.parseLong(args[3]) : 1L;
        } catch (NumberFormatException exception) {
            sender.sendMessage(ColorUtils.toComponent("&cᴀᴍᴏᴜɴᴛ ᴍᴜѕᴛ ʙᴇ ᴀ ᴠᴀʟɪᴅ ᴘᴏѕɪᴛɪᴠᴇ ɴᴜᴍʙᴇʀ."));
            return true;
        }

        if (amount <= 0L) {
            sender.sendMessage(ColorUtils.toComponent("&cᴀᴍᴏᴜɴᴛ ᴍᴜѕᴛ ʙᴇ ɢʀᴇᴀᴛᴇʀ ᴛʜᴀɴ ᴢᴇʀᴏ."));
            return true;
        }

        var result = plugin.getSpawnerManager().giveSpawner(target, args[2], amount);
        sender.sendMessage(ColorUtils.toComponent(result.message()));
        if (!sender.equals(target)) {
            target.sendMessage(ColorUtils.toComponent("&aʏᴏᴜ ʀᴇᴄᴇɪᴠᴇᴅ &f" + NumberUtils.format(amount)
                    + "x " + plugin.getSpawnerManager().getPlainTypeDisplayName(args[2]) + "&a."));
        }
        return true;
    }

    private boolean handleReload(CommandSender sender) {
        if (!PermissionUtils.has(sender, ADMIN_PERMISSION)) {
            sender.sendMessage(ColorUtils.toComponent("&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ ᴛᴏ ʀᴇʟᴏᴀᴅ ѕᴘᴀᴡɴᴇʀѕ."));
            return true;
        }

        plugin.getConfigManager().reloadSpawners();
        plugin.getSpawnerManager().reload();
        plugin.getAntiEspManager().reload();
        plugin.getAntiEspManager().refreshAllPlayers();
        sender.sendMessage(ColorUtils.toComponent("&aѕᴘᴀᴡɴᴇʀ ѕᴇᴛᴛɪɴɢѕ ʀᴇʟᴏᴀᴅᴇᴅ."));
        return true;
    }

    private boolean handlePanel(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("ᴘʟᴀʏᴇʀ ᴏɴʟʏ.");
            return true;
        }
        if (!PermissionUtils.has(sender, ADMIN_PERMISSION)) {
            sender.sendMessage(ColorUtils.toComponent("&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ ᴛᴏ ᴏᴘᴇɴ ᴛʜᴇ ѕᴘᴀᴡɴᴇʀ ᴘᴀɴᴇʟ."));
            return true;
        }

        plugin.getSpawnerManager().openPanel(player);
        return true;
    }

    private boolean handleInfo(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("ᴘʟᴀʏᴇʀ ᴏɴʟʏ.");
            return true;
        }

        Block target = player.getTargetBlockExact(6);
        SpawnerInstance instance = target == null ? null : plugin.getSpawnerManager().getSpawner(target);
        if (instance == null) {
            player.sendMessage(ColorUtils.toComponent("&cʟᴏᴏᴋ ᴀᴛ ᴀ ᴍᴀɴᴀɢᴇᴅ ѕᴘᴀᴡɴᴇʀ ᴛᴏ ɪɴѕᴘᴇᴄᴛ ɪᴛ."));
            return true;
        }

        player.sendMessage(ColorUtils.toComponent("&8&m----------- &bѕᴘᴀᴡɴᴇʀ ɪɴꜰᴏ &8&m-----------"));
        player.sendMessage(ColorUtils.toComponent("&7ᴛʏᴘᴇ: &f" + plugin.getSpawnerManager().getPlainTypeDisplayName(instance.getMobTypeKey())));
        player.sendMessage(ColorUtils.toComponent("&7ᴏᴡɴᴇʀ: &f" + instance.getOwnerNameSnapshot()));
        player.sendMessage(ColorUtils.toComponent("&7ѕᴛᴀᴄᴋ: &f" + NumberUtils.format(instance.getStackAmount())));
        player.sendMessage(ColorUtils.toComponent("&7ѕᴛᴏʀᴇᴅ ʟᴏᴏᴛ: &f" + NumberUtils.format(instance.getTotalStoredItems())));
        player.sendMessage(ColorUtils.toComponent("&7ʟᴏᴄᴀᴛɪᴏɴ: &f" + instance.getWorld() + " "
                + instance.getX() + ", " + instance.getY() + ", " + instance.getZ()));
        return true;
    }

    private boolean handleRemove(CommandSender sender) {
        if (!PermissionUtils.has(sender, ADMIN_PERMISSION)) {
            sender.sendMessage(ColorUtils.toComponent("&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ ᴛᴏ ʀᴇᴍᴏᴠᴇ ѕᴘᴀᴡɴᴇʀѕ."));
            return true;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage("ᴘʟᴀʏᴇʀ ᴏɴʟʏ.");
            return true;
        }

        Block target = player.getTargetBlockExact(6);
        SpawnerInstance instance = target == null ? null : plugin.getSpawnerManager().getSpawner(target);
        if (instance == null) {
            player.sendMessage(ColorUtils.toComponent("&cʟᴏᴏᴋ ᴀᴛ ᴀ ᴍᴀɴᴀɢᴇᴅ ѕᴘᴀᴡɴᴇʀ ᴛᴏ ʀᴇᴍᴏᴠᴇ ɪᴛ."));
            return true;
        }

        target.setType(Material.AIR, false);
        player.sendMessage(ColorUtils.toComponent(plugin.getSpawnerManager().removeSpawner(instance, false, player).message()));
        return true;
    }

    private boolean handleSplit(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("ᴘʟᴀʏᴇʀ ᴏɴʟʏ.");
            return true;
        }

        ItemStack hand = player.getInventory().getItemInMainHand();
        if (!plugin.getSpawnerManager().isSpawnerItem(hand)) {
            player.sendMessage(ColorUtils.toComponent("&cʏᴏᴜ ᴍᴜѕᴛ ʙᴇ ʜᴏʟᴅɪɴɢ ᴀ ᴍᴀɴᴀɢᴇᴅ ѕᴘᴀᴡɴᴇʀ ɪᴛᴇᴍ."));
            return true;
        }

        long currentAmount = plugin.getSpawnerManager().getSpawnerItemAmount(hand);
        if (currentAmount <= 1L) {
            player.sendMessage(ColorUtils.toComponent("&cᴛʜɪѕ ѕᴘᴀᴡɴᴇʀ ɪᴛᴇᴍ ᴄᴀɴɴᴏᴛ ʙᴇ ѕᴘʟɪᴛ (ᴀᴍᴏᴜɴᴛ ɪѕ 1)."));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(ColorUtils.toComponent("&cᴜѕᴀɢᴇ: /spawner split <amount>"));
            return true;
        }

        long splitAmount;
        try {
            splitAmount = NumberUtils.parseLong(args[1]);
        } catch (NumberFormatException exception) {
            player.sendMessage(ColorUtils.toComponent("&cɪɴᴠᴀʟɪᴅ ѕᴘʟɪᴛ ᴀᴍᴏᴜɴᴛ."));
            return true;
        }

        if (splitAmount <= 0L) {
            player.sendMessage(ColorUtils.toComponent("&cѕᴘʟɪᴛ ᴀᴍᴏᴜɴᴛ ᴍᴜѕᴛ ʙᴇ ɢʀᴇᴀᴛᴇʀ ᴛʜᴀɴ ᴢᴇʀᴏ."));
            return true;
        }

        if (splitAmount >= currentAmount) {
            player.sendMessage(ColorUtils.toComponent("&cѕᴘʟɪᴛ ᴀᴍᴏᴜɴᴛ ᴍᴜѕᴛ ʙᴇ ʟᴇѕѕ ᴛʜᴀɴ ᴛʜᴇ ᴄᴜʀʀᴇɴᴛ ѕᴛᴀᴄᴋ ѕɪᴢ (&f" + NumberUtils.format(currentAmount) + "&c)."));
            return true;
        }

        String typeKey = plugin.getSpawnerManager().getSpawnerItemType(hand);
        ItemStack splitItem = plugin.getSpawnerManager().createSpawnerItem(typeKey, splitAmount);
        if (splitItem == null) {
            player.sendMessage(ColorUtils.toComponent("&cꜰᴀɪʟᴇᴅ ᴛᴏ ᴄʀᴇᴀᴛᴇ ѕᴘʟɪᴛ ѕᴘᴀᴡɴᴇʀ ɪᴛᴇᴍ."));
            return true;
        }

        long remainingAmount = currentAmount - splitAmount;
        plugin.getSpawnerManager().updateSpawnerItemAmount(hand, remainingAmount);

        java.util.Map<Integer, ItemStack> leftovers = player.getInventory().addItem(splitItem);
        leftovers.values().forEach(leftover -> player.getWorld().dropItemNaturally(player.getLocation(), leftover));

        player.sendMessage(ColorUtils.toComponent("&aѕᴘʟɪᴛ &f" + NumberUtils.format(splitAmount) + "x &aѕᴘᴀᴡɴᴇʀѕ. &7ʀᴇᴍᴀɪɴɪɴɢ ɪɴ ʜᴀɴᴅ: &f" + NumberUtils.format(remainingAmount) + "&7."));
        return true;
    }

    private boolean sendUsage(CommandSender sender, String label) {
        sender.sendMessage(ColorUtils.toComponent("&8&m----------- &dѕᴘᴀᴡɴᴇʀ &8&m-----------"));
        sender.sendMessage(ColorUtils.toComponent("&f/" + label + " &7- ᴏᴘᴇɴ ᴛʜᴇ ѕᴘᴀᴡɴᴇʀ ᴘᴀɴᴇʟ"));
        sender.sendMessage(ColorUtils.toComponent("&f/" + label + " ɪɴꜰᴏ &7- ɪɴѕᴘᴇᴄᴛ ᴛʜᴇ ʟᴏᴏᴋᴇᴅ-ᴀᴛ ѕᴘᴀᴡɴᴇʀ"));
        sender.sendMessage(ColorUtils.toComponent("&f/" + label + " ᴘᴀɴᴇʟ &7- ᴏᴘᴇɴ ᴛʜᴇ ѕᴘᴀᴡɴᴇʀ ᴀᴅᴍɪɴ ᴘᴀɴᴇʟ"));
        sender.sendMessage(ColorUtils.toComponent("&f/" + label + " ɢɪᴠᴇ <player> <type> [amount] &7- ɢɪᴠᴇ ᴀ ѕᴘᴀᴡɴᴇʀ ɪᴛᴇᴍ"));
        sender.sendMessage(ColorUtils.toComponent("&f/" + label + " split <amount> &7- ѕᴘʟɪᴛ ᴛʜᴇ ʜᴇʟᴅ ѕᴘᴀᴡɴᴇʀ ɪᴛᴇᴍ"));
        sender.sendMessage(ColorUtils.toComponent("&f/" + label + " ʀᴇʟᴏᴀᴅ &7- ʀᴇʟᴏᴀᴅ ѕᴘᴀᴡɴᴇʀ ѕᴇᴛᴛɪɴɢѕ"));
        sender.sendMessage(ColorUtils.toComponent("&f/" + label + " ʀᴇᴍᴏᴠᴇ &7- ʀᴇᴍᴏᴠᴇ ᴛʜᴇ ʟᴏᴏᴋᴇᴅ-ᴀᴛ ѕᴘᴀᴡɴᴇʀ"));
        return true;
    }
}
