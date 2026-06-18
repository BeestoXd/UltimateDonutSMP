package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.utils.PermissionUtils;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.CrateManager;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.menus.CrateEditorMenu;
import com.bx.ultimateDonutSmp.menus.CrateGachaMenu;
import com.bx.ultimateDonutSmp.menus.CrateRewardMenu;
import com.bx.ultimateDonutSmp.menus.CratesMenu;
import com.bx.ultimateDonutSmp.menus.KeysMenu;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class CrateCommand implements CommandExecutor, TabCompleter {

    private static final String ADMIN_PERMISSION = "ultimatedonutsmp.admin.crate";
    private static final String RELOAD_PERMISSION = "ultimatedonutsmp.admin.crate.reload";
    private static final String KEYALL_PERMISSION = "ultimatedonutsmp.admin.crate.keyall";
    private static final int TARGET_BLOCK_DISTANCE = 6;
    private static final List<String> PLAYER_SUBCOMMANDS = List.of("keys", "open");
    private static final List<String> ADMIN_SUBCOMMANDS = List.of(
            "create", "delete", "type", "key", "take", "set", "add", "edit", "remove", "bind", "unbind", "info"
    );
    private static final List<String> OPEN_TYPE_COMPLETIONS = List.of("choose_one", "gacha");
    private static final List<String> AMOUNT_COMPLETIONS = List.of("1", "5", "10", "25", "64");
    private static final List<String> SLOT_COMPLETIONS = List.of("1", "2", "3", "4", "5", "6", "7", "8", "9");

    private final UltimateDonutSmp plugin;

    public CrateCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getConfigManager().isCommandEnabled("CRATE")) {
            sender.sendMessage(ColorUtils.toComponent("&cᴄʀᴀᴛᴇ ᴄᴏᴍᴍᴀɴᴅѕ ᴀʀᴇ ᴄᴜʀʀᴇɴᴛʟʏ ᴅɪѕᴀʙʟᴇᴅ."));
            return true;
        }

        String commandName = command.getName().toLowerCase(Locale.ROOT);
        if (commandName.equals("crates")) {
            return handleCratesCommand(sender, label, args);
        }
        if (commandName.equals("keys")) {
            return handleKeysCommand(sender, label, args);
        }

        if (args.length == 0) {
            return sendCrateUsage(sender, label);
        }

        return switch (args[0].toLowerCase()) {
            case "create" -> handleCreate(sender, label, args);
            case "delete" -> handleDelete(sender, label, args);
            case "type" -> handleType(sender, label, args);
            case "open" -> handleOpen(sender, label, args);
            case "keys" -> handleKeys(sender, args);
            case "reload" -> handleReload(sender);
            case "key" -> handleKeyMutation(sender, args, MutationMode.ADD);
            case "take" -> handleKeyMutation(sender, args, MutationMode.TAKE);
            case "set" -> handleKeyMutation(sender, args, MutationMode.SET);
            case "keyall" -> handleKeyAll(sender, label, args);
            case "add" -> handleRewardMutation(sender, label, args, RewardMutationMode.ADD);
            case "edit" -> handleRewardMutation(sender, label, args, RewardMutationMode.EDIT);
            case "remove" -> handleRewardMutation(sender, label, args, RewardMutationMode.REMOVE);
            case "bind" -> handleBind(sender, label, args);
            case "unbind" -> handleUnbind(sender);
            case "info" -> handleInfo(sender);
            default -> sendCrateUsage(sender, label);
        };
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!command.getName().equalsIgnoreCase("crate")
                || !plugin.getConfigManager().isCommandEnabled("CRATE")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return partialMatches(args[0], availableSubcommands(sender));
        }

        String subcommand = args[0].toLowerCase(Locale.ROOT);
        if (args.length == 2) {
            return switch (subcommand) {
                case "delete", "type", "add", "edit", "remove" -> hasAdminPermission(sender)
                        ? partialMatches(args[1], crateIds())
                        : Collections.emptyList();
                case "open" -> partialMatches(args[1], crateIds());
                case "keyall" -> hasKeyAllPermission(sender)
                        ? partialMatches(args[1], crateIds())
                        : Collections.emptyList();
                case "key", "take", "set" -> hasAdminPermission(sender)
                        ? partialMatches(args[1], targetNames())
                        : Collections.emptyList();
                case "bind" -> hasAdminPermission(sender)
                        ? partialMatches(args[1], bindTargets())
                        : Collections.emptyList();
                default -> Collections.emptyList();
            };
        }

        if (args.length == 3) {
            return switch (subcommand) {
                case "type" -> hasAdminPermission(sender)
                        ? partialMatches(args[2], OPEN_TYPE_COMPLETIONS)
                        : Collections.emptyList();
                case "key", "take", "set" -> hasAdminPermission(sender)
                        ? partialMatches(args[2], crateIds())
                        : Collections.emptyList();
                case "keyall" -> hasKeyAllPermission(sender)
                        ? partialMatches(args[2], AMOUNT_COMPLETIONS)
                        : Collections.emptyList();
                case "add", "edit", "remove" -> hasAdminPermission(sender)
                        ? partialMatches(args[2], SLOT_COMPLETIONS)
                        : Collections.emptyList();
                default -> Collections.emptyList();
            };
        }

        if (args.length == 4 && List.of("key", "take", "set").contains(subcommand)) {
            return hasAdminPermission(sender)
                    ? partialMatches(args[3], AMOUNT_COMPLETIONS)
                    : Collections.emptyList();
        }

        return Collections.emptyList();
    }

    private boolean handleCratesCommand(CommandSender sender, String label, String[] args) {
        if (args.length > 0) {
            sender.sendMessage(ColorUtils.toComponent("&cᴜѕᴀɢᴇ: /" + label));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtils.toComponent("&cᴏɴʟʏ ᴘʟᴀʏᴇʀѕ ᴄᴀɴ ᴏᴘᴇɴ ᴛʜᴇ ᴄʀᴀᴛᴇѕ ᴍᴇɴᴜ."));
            return true;
        }

        new CratesMenu(plugin).open(player);
        return true;
    }

    private boolean handleKeysCommand(CommandSender sender, String label, String[] args) {
        if (args.length > 0) {
            sender.sendMessage(ColorUtils.toComponent("&cᴜѕᴀɢᴇ: /" + label));
            return true;
        }

        return openKeysMenu(sender);
    }

    private boolean sendCrateUsage(CommandSender sender, String label) {
        if (!PermissionUtils.has(sender, ADMIN_PERMISSION)) {
            sender.sendMessage(ColorUtils.toComponent("&c/" + label + " ɪѕ ᴀɴ ᴀᴅᴍɪɴ ᴄʀᴀᴛᴇ ᴄᴏᴍᴍᴀɴᴅ."));
            sender.sendMessage(ColorUtils.toComponent("&7ᴜѕᴇ &f/crates &7ᴛᴏ ᴏᴘᴇɴ ᴄʀᴀᴛᴇѕ ᴀɴᴅ &f/keys &7ᴛᴏ ᴠɪᴇᴡ ʏᴏᴜʀ ᴋᴇʏѕ."));
            return true;
        }

        sender.sendMessage(ColorUtils.toComponent("&8&m----------- &bᴄʀᴀᴛᴇ ᴀᴅᴍɪɴ &8&m-----------"));
        sender.sendMessage(ColorUtils.toComponent("&f/" + label + " create <crate> &7- ᴄʀᴇᴀᴛᴇ ᴀ ᴄʀᴀᴛᴇ"));
        sender.sendMessage(ColorUtils.toComponent("&f/" + label + " delete <crate> &7- ᴅᴇʟᴇᴛᴇ ᴀ ᴄʀᴀᴛᴇ"));
        sender.sendMessage(ColorUtils.toComponent("&f/" + label + " ᴛʏᴘᴇ <crate> <choose_one|gacha> &7- ѕᴇᴛ ᴄʀᴀᴛᴇ ᴛʏᴘᴇ"));
        sender.sendMessage(ColorUtils.toComponent("&f/" + label + " ᴏᴘᴇɴ <crate> &7- ᴏᴘᴇɴ ᴀ ᴄʀᴀᴛᴇ ᴅɪʀᴇᴄᴛʟʏ"));
        sender.sendMessage(ColorUtils.toComponent("&f/" + label + " ᴋᴇʏ <player> <crate> <amount> &7- ɢɪᴠᴇ ᴋᴇʏѕ"));
        sender.sendMessage(ColorUtils.toComponent("&f/" + label + " ᴛᴀᴋᴇ <player> <crate> <amount> &7- ʀᴇᴍᴏᴠᴇ ᴋᴇʏѕ"));
        sender.sendMessage(ColorUtils.toComponent("&f/" + label + " ѕᴇᴛ <player> <crate> <amount> &7- ѕᴇᴛ ᴋᴇʏ ʙᴀʟᴀɴᴄᴇ"));
        sender.sendMessage(ColorUtils.toComponent("&f/" + label + " ᴋᴇʏᴀʟʟ <crate> <amount> &7- ɢʀᴀɴᴛ ᴋᴇʏѕ ᴛᴏ ᴏɴʟɪɴᴇ ᴘʟᴀʏᴇʀѕ"));
        sender.sendMessage(ColorUtils.toComponent("&f/" + label + " ᴀᴅᴅ <crate> [slot] &7- ᴀᴅᴅ ʀᴇᴡᴀʀᴅ ʙʏ ɢᴜɪ ᴏʀ ʜᴀɴᴅ"));
        sender.sendMessage(ColorUtils.toComponent("&f/" + label + " ᴇᴅɪᴛ <crate> [slot] &7- ᴇᴅɪᴛ ʀᴇᴡᴀʀᴅ ʙʏ ɢᴜɪ ᴏʀ ʜᴀɴᴅ"));
        sender.sendMessage(ColorUtils.toComponent("&f/" + label + " ʀᴇᴍᴏᴠᴇ <crate> <slot> &7- ʀᴇᴍᴏᴠᴇ ᴀ ʀᴇᴡᴀʀᴅ"));
        sender.sendMessage(ColorUtils.toComponent("&f/" + label + " ʙɪɴᴅ <crate|cancel> &7- ʙɪɴᴅ ᴀ ᴄʀᴀᴛᴇ ᴄʜᴇѕᴛ"));
        sender.sendMessage(ColorUtils.toComponent("&f/" + label + " ᴜɴʙɪɴᴅ &7- ᴜɴʙɪɴᴅ ᴛʜᴇ ʟᴏᴏᴋᴇᴅ-ᴀᴛ ᴄʀᴀᴛᴇ ᴄʜᴇѕᴛ"));
        sender.sendMessage(ColorUtils.toComponent("&f/" + label + " ɪɴꜰᴏ &7- ɪɴѕᴘᴇᴄᴛ ᴛʜᴇ ʟᴏᴏᴋᴇᴅ-ᴀᴛ ᴄʀᴀᴛᴇ ᴄʜᴇѕᴛ"));
        sender.sendMessage(ColorUtils.toComponent("&f/" + label + " ʀᴇʟᴏᴀᴅ &7- ʀᴇʟᴏᴀᴅ ᴄʀᴀᴛᴇ ѕᴇᴛᴛɪɴɢѕ"));
        sender.sendMessage(ColorUtils.toComponent("&7ᴘʟᴀʏᴇʀ ᴄᴏᴍᴍᴀɴᴅѕ: &f/crates &7ᴀɴᴅ &f/keys"));
        sender.sendMessage(ColorUtils.toComponent("&8&m----------------------------------"));
        return true;
    }

    private boolean handleOpen(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtils.toComponent("&cᴏɴʟʏ ᴘʟᴀʏᴇʀѕ ᴄᴀɴ ᴏᴘᴇɴ ᴄʀᴀᴛᴇѕ."));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ColorUtils.toComponent("&cᴜѕᴀɢᴇ: /" + label + " ᴏᴘᴇɴ <crate>"));
            return true;
        }

        CrateManager.OpenResult result = plugin.getCrateManager().startOpening(player, args[1]);
        if (!result.success()) {
            player.sendMessage(ColorUtils.toComponent(result.message()));
            return true;
        }

        openCrateMenu(player, result.crate(), CrateRewardMenu.OpenContext.COMMAND);
        return true;
    }

    private boolean handleCreate(CommandSender sender, String label, String[] args) {
        if (!PermissionUtils.has(sender, ADMIN_PERMISSION)) {
            sender.sendMessage(ColorUtils.toComponent("&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ ᴛᴏ ᴄʀᴇᴀᴛᴇ ᴄʀᴀᴛᴇѕ."));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ColorUtils.toComponent("&cᴜѕᴀɢᴇ: /" + label + " create <crate>"));
            return true;
        }

        CrateManager.ActionResult result = plugin.getCrateManager().createCrate(args[1]);
        sender.sendMessage(ColorUtils.toComponent(result.message()));
        if (result.success()) {
            plugin.getCrateVisualManager().reload();
        }
        return true;
    }

    private boolean handleDelete(CommandSender sender, String label, String[] args) {
        if (!PermissionUtils.has(sender, ADMIN_PERMISSION)) {
            sender.sendMessage(ColorUtils.toComponent("&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ ᴛᴏ ᴅᴇʟᴇᴛᴇ ᴄʀᴀᴛᴇѕ."));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ColorUtils.toComponent("&cᴜѕᴀɢᴇ: /" + label + " delete <crate>"));
            return true;
        }

        CrateManager.ActionResult result = plugin.getCrateManager().deleteCrate(args[1]);
        sender.sendMessage(ColorUtils.toComponent(result.message()));
        if (result.success()) {
            plugin.getCrateVisualManager().reload();
        }
        return true;
    }

    private boolean handleType(CommandSender sender, String label, String[] args) {
        if (!PermissionUtils.has(sender, ADMIN_PERMISSION)) {
            sender.sendMessage(ColorUtils.toComponent("&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ ᴛᴏ ᴄʜᴀɴɢᴇ ᴄʀᴀᴛᴇ ᴛʏᴘᴇѕ."));
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(ColorUtils.toComponent("&cᴜѕᴀɢᴇ: /" + label + " ᴛʏᴘᴇ <crate> <choose_one|gacha>"));
            return true;
        }

        CrateManager.OpenType openType;
        try {
            openType = CrateManager.OpenType.valueOf(args[2].trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            sender.sendMessage(ColorUtils.toComponent("&cᴛʏᴘᴇ ᴍᴜѕᴛ ʙᴇ &fᴄʜᴏᴏѕᴇ_ᴏɴᴇ &cᴏʀ &fɢᴀᴄʜᴀ&c."));
            return true;
        }

        CrateManager.ActionResult result = plugin.getCrateManager().setOpenType(args[1], openType);
        sender.sendMessage(ColorUtils.toComponent(result.message()));
        if (result.success()) {
            plugin.getCrateVisualManager().reload();
        }
        return true;
    }

    private boolean handleKeys(CommandSender sender, String[] args) {
        return openKeysMenu(sender);
    }

    private boolean handleReload(CommandSender sender) {
        if (!PermissionUtils.has(sender, RELOAD_PERMISSION) && !PermissionUtils.has(sender, ADMIN_PERMISSION)) {
            sender.sendMessage(ColorUtils.toComponent("&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ ᴛᴏ ʀᴇʟᴏᴀᴅ ᴄʀᴀᴛᴇ ѕᴇᴛᴛɪɴɢѕ."));
            return true;
        }

        plugin.getConfigManager().reloadCrates();
        plugin.getCrateManager().reload();
        plugin.getCrateVisualManager().reload();
        sender.sendMessage(ColorUtils.toComponent("&aᴄʀᴀᴛᴇ ѕᴇᴛᴛɪɴɢѕ ʀᴇʟᴏᴀᴅᴇᴅ."));
        return true;
    }

    private boolean handleKeyMutation(CommandSender sender, String[] args, MutationMode mode) {
        if (!PermissionUtils.has(sender, ADMIN_PERMISSION)) {
            sender.sendMessage(ColorUtils.toComponent("&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ ᴛᴏ ᴍᴏᴅɪꜰʏ ᴄʀᴀᴛᴇ ᴋᴇʏѕ."));
            return true;
        }

        if (args.length < 4) {
            sender.sendMessage(ColorUtils.toComponent("&cᴜѕᴀɢᴇ: /crate " + mode.commandName + " <player> <crate> <amount>"));
            return true;
        }

        ResolvedTarget target = resolveTarget(args[1]);
        if (target == null) {
            sender.sendMessage(ColorUtils.toComponent("&cᴘʟᴀʏᴇʀ '&f" + args[1] + "&c' ᴡᴀѕ ɴᴏᴛ ꜰᴏᴜɴᴅ."));
            return true;
        }

        CrateManager.CrateDefinition crate = plugin.getCrateManager().getCrate(args[2]);
        if (crate == null) {
            sender.sendMessage(ColorUtils.toComponent("&cᴄʀᴀᴛᴇ '&f" + args[2] + "&c' ᴡᴀѕ ɴᴏᴛ ꜰᴏᴜɴᴅ."));
            return true;
        }

        Integer amount = parsePositiveInt(args[3]);
        if ((amount == null || amount <= 0) && mode != MutationMode.SET) {
            sender.sendMessage(ColorUtils.toComponent("&cᴀᴍᴏᴜɴᴛ ᴍᴜѕᴛ ʙᴇ ᴀ ᴘᴏѕɪᴛɪᴠᴇ ɪɴᴛᴇɢᴇʀ."));
            return true;
        }
        if (mode == MutationMode.SET && (amount == null || amount < 0)) {
            sender.sendMessage(ColorUtils.toComponent("&cᴀᴍᴏᴜɴᴛ ᴍᴜѕᴛ ʙᴇ ᴢᴇʀᴏ ᴏʀ ᴀ ᴘᴏѕɪᴛɪᴠᴇ ɪɴᴛᴇɢᴇʀ."));
            return true;
        }

        int balance;
        boolean success = true;
        switch (mode) {
            case ADD -> balance = plugin.getCrateManager().addKeys(target.uuid(), crate.id(), amount);
            case TAKE -> {
                success = plugin.getCrateManager().takeKeys(target.uuid(), crate.id(), amount);
                balance = plugin.getCrateManager().getKeyBalance(target.uuid(), crate.id());
            }
            case SET -> balance = plugin.getCrateManager().setKeys(target.uuid(), crate.id(), amount);
            default -> throw new IllegalStateException("Unexpected value: " + mode);
        }

        if (!success) {
            sender.sendMessage(ColorUtils.toComponent("&c" + target.name() + " ᴅᴏᴇѕ ɴᴏᴛ ʜᴀᴠᴇ ᴇɴᴏᴜɢʜ ᴋᴇʏѕ ᴛᴏ ʀᴇᴍᴏᴠᴇ " + amount + "."));
            return true;
        }

        sender.sendMessage(ColorUtils.toComponent("&a" + mode.successPrefix + " &f" + amount + "x "
                + plugin.getCrateManager().getReadableCrateName(crate)
                + "&a ꜰᴏʀ &f" + target.name() + "&a. ʙᴀʟᴀɴᴄᴇ: &f" + balance));

        Player online = Bukkit.getPlayer(target.uuid());
        if (online != null && online.isOnline()) {
            online.sendMessage(ColorUtils.toComponent("&7ʏᴏᴜʀ &b" + plugin.getCrateManager().getReadableCrateName(crate)
                    + "&7 ᴋᴇʏ ʙᴀʟᴀɴᴄᴇ ɪѕ ɴᴏᴡ &f" + balance + "&7."));
        }
        return true;
    }

    private boolean handleKeyAll(CommandSender sender, String label, String[] args) {
        if (!PermissionUtils.has(sender, KEYALL_PERMISSION) && !PermissionUtils.has(sender, ADMIN_PERMISSION)) {
            sender.sendMessage(ColorUtils.toComponent("&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ ᴛᴏ ʀᴜɴ ᴄʀᴀᴛᴇ ᴋᴇʏ-ᴀʟʟ."));
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(ColorUtils.toComponent("&cᴜѕᴀɢᴇ: /" + label + " ᴋᴇʏᴀʟʟ <crate> <amount>"));
            return true;
        }

        CrateManager.CrateDefinition crate = plugin.getCrateManager().getCrate(args[1]);
        if (crate == null) {
            sender.sendMessage(ColorUtils.toComponent("&cᴄʀᴀᴛᴇ '&f" + args[1] + "&c' ᴡᴀѕ ɴᴏᴛ ꜰᴏᴜɴᴅ."));
            return true;
        }

        Integer amount = parsePositiveInt(args[2]);
        if (amount == null) {
            sender.sendMessage(ColorUtils.toComponent("&cᴀᴍᴏᴜɴᴛ ᴍᴜѕᴛ ʙᴇ ᴀ ᴘᴏѕɪᴛɪᴠᴇ ɪɴᴛᴇɢᴇʀ."));
            return true;
        }

        int granted = plugin.getKeyAllManager().grantCrateKeys(crate.id(), amount, false);
        sender.sendMessage(ColorUtils.toComponent("&aɢʀᴀɴᴛᴇᴅ &f" + amount + "x "
                + plugin.getCrateManager().getReadableCrateName(crate)
                + "&a ᴋᴇʏ(s) ᴛᴏ &f" + granted + "&a ᴏɴʟɪɴᴇ ᴘʟᴀʏᴇʀ(s)."));
        return true;
    }

    private boolean handleRewardMutation(CommandSender sender, String label, String[] args, RewardMutationMode mode) {
        if (!PermissionUtils.has(sender, ADMIN_PERMISSION)) {
            sender.sendMessage(ColorUtils.toComponent("&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ ᴛᴏ ᴍᴏᴅɪꜰʏ ᴄʀᴀᴛᴇ ʀᴇᴡᴀʀᴅѕ."));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtils.toComponent("&cᴏɴʟʏ ᴘʟᴀʏᴇʀѕ ᴄᴀɴ ᴜѕᴇ /crate " + mode.commandName + "."));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ColorUtils.toComponent("&cᴜѕᴀɢᴇ: /" + label + " " + mode.commandName + " <crate> [slot]"));
            return true;
        }

        CrateManager.CrateDefinition crate = plugin.getCrateManager().getCrate(args[1]);
        if (crate == null) {
            sender.sendMessage(ColorUtils.toComponent("&cᴄʀᴀᴛᴇ '&f" + args[1] + "&c' ᴡᴀѕ ɴᴏᴛ ꜰᴏᴜɴᴅ."));
            return true;
        }

        if ((mode == RewardMutationMode.ADD || mode == RewardMutationMode.EDIT) && args.length == 2) {
            new CrateEditorMenu(plugin, crate.id()).open(player);
            return true;
        }

        if (mode == RewardMutationMode.REMOVE && args.length == 2) {
            sender.sendMessage(ColorUtils.toComponent("&cᴜѕᴀɢᴇ: /" + label + " ʀᴇᴍᴏᴠᴇ <crate> <slot>"));
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(ColorUtils.toComponent("&cᴜѕᴀɢᴇ: /" + label + " " + mode.commandName + " <crate> <slot>"));
            return true;
        }

        Integer slot = parsePositiveInt(args[2]);
        if (slot == null || slot < 0) {
            sender.sendMessage(ColorUtils.toComponent("&cѕʟᴏᴛ ᴍᴜѕᴛ ʙᴇ ᴀ ᴠᴀʟɪᴅ ɴᴜᴍʙᴇʀ, ꜰᴏʀ ᴇxᴀᴍᴘʟᴇ &f10&c."));
            return true;
        }

        CrateManager.ActionResult result = switch (mode) {
            case ADD -> plugin.getCrateManager().addItemReward(crate.id(), slot, player.getInventory().getItemInMainHand());
            case EDIT -> plugin.getCrateManager().editItemReward(crate.id(), slot, player.getInventory().getItemInMainHand());
            case REMOVE -> plugin.getCrateManager().removeReward(crate.id(), slot);
        };

        sender.sendMessage(ColorUtils.toComponent(result.message()));
        return true;
    }

    private boolean handleBind(CommandSender sender, String label, String[] args) {
        if (!PermissionUtils.has(sender, ADMIN_PERMISSION)) {
            sender.sendMessage(ColorUtils.toComponent("&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ ᴛᴏ ʙɪɴᴅ ᴄʀᴀᴛᴇ ᴄʜᴇѕᴛѕ."));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtils.toComponent("&cᴏɴʟʏ ᴘʟᴀʏᴇʀѕ ᴄᴀɴ ʙɪɴᴅ ᴄʀᴀᴛᴇ ᴄʜᴇѕᴛѕ."));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ColorUtils.toComponent("&cᴜѕᴀɢᴇ: /" + label + " ʙɪɴᴅ <crate|cancel>"));
            return true;
        }

        if (args[1].equalsIgnoreCase("cancel")) {
            plugin.getCrateManager().clearPendingBind(player.getUniqueId());
            player.sendMessage(ColorUtils.toComponent("&aᴄʀᴀᴛᴇ ʙɪɴᴅ ᴍᴏᴅᴇ ᴄᴀɴᴄᴇʟʟᴇᴅ."));
            return true;
        }

        CrateManager.CrateDefinition crate = plugin.getCrateManager().getCrate(args[1]);
        if (crate == null) {
            sender.sendMessage(ColorUtils.toComponent("&cᴄʀᴀᴛᴇ '&f" + args[1] + "&c' ᴡᴀѕ ɴᴏᴛ ꜰᴏᴜɴᴅ."));
            return true;
        }

        plugin.getCrateManager().startPendingBind(player.getUniqueId(), crate.id());
        player.sendMessage(ColorUtils.toComponent("&aʙɪɴᴅ ᴍᴏᴅᴇ ᴇɴᴀʙʟᴇᴅ ꜰᴏʀ &f" + crate.id() + "&a."));
        player.sendMessage(ColorUtils.toComponent("&7ʟᴇꜰᴛ-ᴄʟɪᴄᴋ ᴀ ᴄʜᴇѕᴛ, ᴛʀᴀᴘᴘᴇᴅ ᴄʜᴇѕᴛ, ʙᴀʀʀᴇʟ, ᴇɴᴅᴇʀ ᴄʜᴇѕᴛ, ᴏʀ ѕʜᴜʟᴋᴇʀ ʙᴏx ᴛᴏ ʙɪɴᴅ ɪᴛ."));
        return true;
    }

    private boolean handleUnbind(CommandSender sender) {
        if (!PermissionUtils.has(sender, ADMIN_PERMISSION)) {
            sender.sendMessage(ColorUtils.toComponent("&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ ᴛᴏ ᴜɴʙɪɴᴅ ᴄʀᴀᴛᴇ ᴄʜᴇѕᴛѕ."));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtils.toComponent("&cᴏɴʟʏ ᴘʟᴀʏᴇʀѕ ᴄᴀɴ ᴜɴʙɪɴᴅ ᴄʀᴀᴛᴇ ᴄʜᴇѕᴛѕ."));
            return true;
        }

        Block target = getTargetBlock(player);
        if (target == null) {
            player.sendMessage(ColorUtils.toComponent("&cʟᴏᴏᴋ ᴀᴛ ᴀ ʙᴏᴜɴᴅ ᴄʀᴀᴛᴇ ᴄʜᴇѕᴛ ꜰɪʀѕᴛ."));
            return true;
        }

        String crateId = plugin.getCrateManager().getBoundCrateId(target);
        if (crateId == null) {
            player.sendMessage(ColorUtils.toComponent("&cᴛʜᴀᴛ ʙʟᴏᴄᴋ ɪѕ ɴᴏᴛ ʙᴏᴜɴᴅ ᴛᴏ ᴀɴʏ ᴄʀᴀᴛᴇ."));
            return true;
        }

        if (!plugin.getCrateManager().unbindCrateBlock(target)) {
            player.sendMessage(ColorUtils.toComponent("&cꜰᴀɪʟᴇᴅ ᴛᴏ ᴜɴʙɪɴᴅ ᴛʜᴀᴛ ᴄʀᴀᴛᴇ ᴄʜᴇѕᴛ."));
            return true;
        }

        plugin.getCrateVisualManager().removeHologram(target);
        player.sendMessage(ColorUtils.toComponent("&aʀᴇᴍᴏᴠᴇᴅ ᴄʀᴀᴛᴇ ʙɪɴᴅɪɴɢ ꜰʀᴏᴍ &f" + formatBlockLocation(target) + "&a."));
        return true;
    }

    private boolean handleInfo(CommandSender sender) {
        if (!PermissionUtils.has(sender, ADMIN_PERMISSION)) {
            sender.sendMessage(ColorUtils.toComponent("&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ ᴛᴏ ɪɴѕᴘᴇᴄᴛ ᴄʀᴀᴛᴇ ᴄʜᴇѕᴛѕ."));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtils.toComponent("&cᴏɴʟʏ ᴘʟᴀʏᴇʀѕ ᴄᴀɴ ɪɴѕᴘᴇᴄᴛ ᴄʀᴀᴛᴇ ᴄʜᴇѕᴛѕ."));
            return true;
        }

        Block target = getTargetBlock(player);
        if (target == null) {
            player.sendMessage(ColorUtils.toComponent("&cʟᴏᴏᴋ ᴀᴛ ᴀ ᴄʀᴀᴛᴇ ᴄʜᴇѕᴛ ꜰɪʀѕᴛ."));
            return true;
        }

        String crateId = plugin.getCrateManager().getBoundCrateId(target);
        if (crateId == null) {
            player.sendMessage(ColorUtils.toComponent("&cᴛʜᴀᴛ ʙʟᴏᴄᴋ ɪѕ ɴᴏᴛ ʙᴏᴜɴᴅ ᴛᴏ ᴀɴʏ ᴄʀᴀᴛᴇ."));
            return true;
        }

        CrateManager.CrateDefinition crate = plugin.getCrateManager().getCrate(crateId);
        player.sendMessage(ColorUtils.toComponent("&8&m-------- &bᴄʀᴀᴛᴇ ᴄʜᴇѕᴛ &8&m--------"));
        player.sendMessage(ColorUtils.toComponent("&7ʟᴏᴄᴀᴛɪᴏɴ: &f" + formatBlockLocation(target)));
        player.sendMessage(ColorUtils.toComponent("&7ᴄʀᴀᴛᴇ ID: &f" + crateId));
        player.sendMessage(ColorUtils.toComponent("&7ᴅɪѕᴘʟᴀʏ: &f" + plugin.getCrateManager().getReadableCrateName(crate)));
        player.sendMessage(ColorUtils.toComponent("&8&m-------------------------------"));
        return true;
    }

    private ResolvedTarget resolveTarget(String input) {
        Player online = Bukkit.getPlayerExact(input);
        if (online != null) {
            return new ResolvedTarget(online.getUniqueId(), online.getName());
        }

        UUID uuid = plugin.getDatabaseManager().findPlayerUuidByUsername(input);
        if (uuid == null) {
            return null;
        }

        String name = plugin.getDatabaseManager().getLastKnownUsername(uuid);
        return new ResolvedTarget(uuid, name == null || name.isBlank() ? input : name);
    }

    private Integer parsePositiveInt(String input) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private Block getTargetBlock(Player player) {
        Block block = player.getTargetBlockExact(TARGET_BLOCK_DISTANCE);
        if (block == null || block.getType().isAir()) {
            return null;
        }
        return block;
    }

    private String formatBlockLocation(Block block) {
        return block.getWorld().getName() + " "
                + block.getX() + ","
                + block.getY() + ","
                + block.getZ();
    }

    private void openCrateMenu(Player player, CrateManager.CrateDefinition crate, CrateRewardMenu.OpenContext openContext) {
        if (crate.openType() == CrateManager.OpenType.GACHA) {
            new CrateGachaMenu(plugin, crate).open(player);
            return;
        }

        new CrateRewardMenu(plugin, crate, openContext).open(player);
    }

    private boolean openKeysMenu(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtils.toComponent("&cᴏɴʟʏ ᴘʟᴀʏᴇʀѕ ᴄᴀɴ ᴠɪᴇᴡ ᴄʀᴀᴛᴇ ᴋᴇʏѕ."));
            return true;
        }

        new KeysMenu(plugin).open(player);
        return true;
    }

    private List<String> availableSubcommands(CommandSender sender) {
        List<String> completions = new ArrayList<>(PLAYER_SUBCOMMANDS);
        if (hasAdminPermission(sender)) {
            completions.addAll(ADMIN_SUBCOMMANDS);
        }
        if (hasReloadPermission(sender)) {
            completions.add("reload");
        }
        if (hasKeyAllPermission(sender)) {
            completions.add("keyall");
        }
        return completions;
    }

    private boolean hasAdminPermission(CommandSender sender) {
        return PermissionUtils.has(sender, ADMIN_PERMISSION);
    }

    private boolean hasReloadPermission(CommandSender sender) {
        return hasAdminPermission(sender) || PermissionUtils.has(sender, RELOAD_PERMISSION);
    }

    private boolean hasKeyAllPermission(CommandSender sender) {
        return hasAdminPermission(sender) || PermissionUtils.has(sender, KEYALL_PERMISSION);
    }

    private List<String> crateIds() {
        List<String> ids = new ArrayList<>();
        for (CrateManager.CrateDefinition crate : plugin.getCrateManager().getCrates()) {
            ids.add(crate.id());
        }
        ids.sort(String.CASE_INSENSITIVE_ORDER);
        return ids;
    }

    private List<String> bindTargets() {
        List<String> completions = crateIds();
        completions.add("cancel");
        return completions;
    }

    private List<String> targetNames() {
        List<String> names = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            names.add(player.getName());
        }
        for (PlayerData data : plugin.getPlayerDataManager().getAll()) {
            if (data.getUsername() != null && !data.getUsername().isBlank()) {
                names.add(data.getUsername());
            }
        }
        return names.stream()
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    private List<String> partialMatches(String token, List<String> completions) {
        List<String> matches = new ArrayList<>();
        StringUtil.copyPartialMatches(token, completions, matches);
        matches.sort(String.CASE_INSENSITIVE_ORDER);
        return matches;
    }

    private record ResolvedTarget(UUID uuid, String name) {
    }

    private enum MutationMode {
        ADD("key", "Granted"),
        TAKE("take", "Removed"),
        SET("set", "Set");

        private final String commandName;
        private final String successPrefix;

        MutationMode(String commandName, String successPrefix) {
            this.commandName = commandName;
            this.successPrefix = successPrefix;
        }
    }

    private enum RewardMutationMode {
        ADD("add"),
        EDIT("edit"),
        REMOVE("remove");

        private final String commandName;

        RewardMutationMode(String commandName) {
            this.commandName = commandName;
        }
    }
}
