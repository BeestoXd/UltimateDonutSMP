package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class RenameCommand implements CommandExecutor {

    private static final String PERMISSION = "ultimatedonutsmp.staff.rename";

    private final UltimateDonutSmp plugin;

    public RenameCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtils.toComponent("&cOnly players can use this command."));
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(ColorUtils.toComponent("&cYou do not have permission."));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ColorUtils.toComponent("&cUsage: /" + label + " <name...|reset>"));
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            player.sendMessage(ColorUtils.toComponent(
                    plugin.getConfigManager().getMessageOrDefault("RENAME.NO_ITEM", "&cYou must hold an item to rename it")
            ));
            return true;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            player.sendMessage(ColorUtils.toComponent(
                    plugin.getConfigManager().getMessageOrDefault("RENAME.META_ERROR", "&cThis item cannot be renamed")
            ));
            return true;
        }

        if (isStaffModeItemBlocked(player, item)) {
            player.sendMessage(ColorUtils.toComponent(
                    plugin.getConfigManager().getMessageOrDefault(
                            "RENAME.STAFFMODE_BLOCKED",
                            "&cYou cannot rename staff mode items."
                    ),
                    player
            ));
            return true;
        }

        String newName = String.join(" ", args);
        if (isResetRequest(args)) {
            meta.displayName(null);
            item.setItemMeta(meta);
            player.sendMessage(ColorUtils.toComponent(
                    plugin.getConfigManager().getMessageOrDefault(
                            "RENAME.RESET_SUCCESS",
                            "&7Item name has been reset."
                    ),
                    player
            ));
            return true;
        }

        meta.displayName(ColorUtils.toComponent(newName, player));
        item.setItemMeta(meta);
        player.sendMessage(ColorUtils.toComponent(
                plugin.getConfigManager().getMessageOrDefault("RENAME.SUCCESS", "&7New name: &f%name%", "%name%", newName),
                player
        ));
        return true;
    }

    private boolean isResetRequest(String[] args) {
        if (args.length != 1) {
            return false;
        }

        String value = args[0];
        return value.equalsIgnoreCase("reset")
                || value.equalsIgnoreCase("clear")
                || value.equalsIgnoreCase("remove");
    }

    private boolean isStaffModeItemBlocked(Player player, ItemStack item) {
        if (plugin.getStaffModeManager() == null) {
            return false;
        }

        return plugin.getStaffModeManager().isInStaffMode(player.getUniqueId())
                || plugin.getStaffModeManager().isStaffTool(item);
    }
}
