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
import java.util.Arrays;

public class CrateCommand implements CommandExecutor, TabCompleter {

    private static final String ADMIN_PERMISSION = "ultimatedonutsmp.admin.crate";
    private static final String RELOAD_PERMISSION = "ultimatedonutsmp.admin.crate.reload";
    private static final String KEYALL_PERMISSION = "ultimatedonutsmp.admin.crate.keyall";
    private static final String CRATES_USE_PERMISSION = "ultimatedonutsmp.command.crates";
    private static final int TARGET_BLOCK_DISTANCE = 6;
    private static final List<String> PLAYER_SUBCOMMANDS = List.of("keys", "open");
    private static final List<String> ADMIN_SUBCOMMANDS = List.of(
            "create", "delete", "type", "key", "take", "set", "add", "edit", "remove", "bind", "unbind", "listbound", "info"
    );
    private static final List<String> OPEN_TYPE_COMPLETIONS = List.of("choose_one", "choose-one", "gacha");
    private static final List<String> AMOUNT_COMPLETIONS = List.of("1", "5", "10", "25", "64");
    private static final List<String> SLOT_COMPLETIONS = List.of("1", "2", "3", "4", "5", "6", "7", "8", "9");

    private final UltimateDonutSmp plugin;

    public CrateCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getConfigManager().isCommandEnabled("CRATE")) {
            sender.sendMessage(ColorUtils.toComponent("&cCrate commands are currently disabled."));
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
            case "unbind" -> handleUnbind(sender, args);
            case "listbound" -> handleListBound(sender);
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
        if (subcommand.equals("unbind") && hasAdminPermission(sender)) {
            if (args.length == 2) {
                return partialMatches(args[1], unbindWorldSuggestions());
            }
            if (args.length == 3) {
                return partialMatches(args[2], unbindXSuggestions(args[1]));
            }
            if (args.length == 4) {
                return partialMatches(args[3], unbindYSuggestions(args[1], args[2]));
            }
            if (args.length == 5) {
                return partialMatches(args[4], unbindZSuggestions(args[1], args[2], args[3]));
            }
        }

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
        if (!PermissionUtils.has(sender, CRATES_USE_PERMISSION)) {
            sender.sendMessage(ColorUtils.toComponent("&cYou do not have permission to use /crates."));
            return true;
        }

        if (args.length > 0) {
            sender.sendMessage(ColorUtils.toComponent("&cUsage: /" + label));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtils.toComponent("&cOnly players can open the crates menu."));
            return true;
        }

        new CratesMenu(plugin).open(player);
        return true;
    }

    private boolean handleKeysCommand(CommandSender sender, String label, String[] args) {
        if (args.length > 0) {
            sender.sendMessage(ColorUtils.toComponent("&cUsage: /" + label));
            return true;
        }

        return openKeysMenu(sender);
    }

    private boolean sendCrateUsage(CommandSender sender, String label) {
        if (!PermissionUtils.has(sender, ADMIN_PERMISSION)) {
            sender.sendMessage(ColorUtils.toComponent("&c/" + label + " is an admin crate command."));
            sender.sendMessage(ColorUtils.toComponent("&7Use &f/crates &7to open crates and &f/keys &7to view your keys."));
            return true;
        }

        sender.sendMessage(ColorUtils.toComponent("&8&m----------- &bCrate admin &8&m-----------"));
        sender.sendMessage(ColorUtils.toComponent("&f/" + label + " create <crate> &7- create a crate"));
        sender.sendMessage(ColorUtils.toComponent("&f/" + label + " delete <crate> &7- delete a crate"));
        sender.sendMessage(ColorUtils.toComponent("&f/" + label + " type <crate> <choose_one|gacha> &7- set crate type"));
        sender.sendMessage(ColorUtils.toComponent("&f/" + label + " open <crate> &7- open a crate directly"));
        sender.sendMessage(ColorUtils.toComponent("&f/" + label + " key <player> <crate> <amount> &7- give keys"));
        sender.sendMessage(ColorUtils.toComponent("&f/" + label + " take <player> <crate> <amount> &7- remove keys"));
        sender.sendMessage(ColorUtils.toComponent("&f/" + label + " set <player> <crate> <amount> &7- set key balance"));
        sender.sendMessage(ColorUtils.toComponent("&f/" + label + " keyall <crate> <amount> &7- grant keys to online players"));
        sender.sendMessage(ColorUtils.toComponent("&f/" + label + " add <crate> [slot] &7- add reward by gui or hand"));
        sender.sendMessage(ColorUtils.toComponent("&f/" + label + " edit <crate> [slot] &7- edit reward by gui or hand"));
        sender.sendMessage(ColorUtils.toComponent("&f/" + label + " remove <crate> <slot> &7- remove a reward"));
        sender.sendMessage(ColorUtils.toComponent("&f/" + label + " bind <crate|cancel> &7- bind a crate chest"));
        sender.sendMessage(ColorUtils.toComponent("&f/" + label + " unbind [world x y z] &7- unbind by look-at or coords"));
        sender.sendMessage(ColorUtils.toComponent("&f/" + label + " listbound &7- list all bound crates and locations"));
        sender.sendMessage(ColorUtils.toComponent("&f/" + label + " info &7- inspect the looked-at crate chest"));
        sender.sendMessage(ColorUtils.toComponent("&f/" + label + " reload &7- reload crate settings"));
        sender.sendMessage(ColorUtils.toComponent("&7Player commands: &f/crates &7and &f/keys"));
        sender.sendMessage(ColorUtils.toComponent("&8&m----------------------------------"));
        return true;
    }

    private boolean handleOpen(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtils.toComponent("&cOnly players can open crates."));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ColorUtils.toComponent("&cUsage: /" + label + " open <crate>"));
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
            sender.sendMessage(ColorUtils.toComponent("&cYou do not have permission to create crates."));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ColorUtils.toComponent("&cUsage: /" + label + " create <crate>"));
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
            sender.sendMessage(ColorUtils.toComponent("&cYou do not have permission to delete crates."));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ColorUtils.toComponent("&cUsage: /" + label + " delete <crate>"));
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
            sender.sendMessage(ColorUtils.toComponent("&cYou do not have permission to change crate types."));
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(ColorUtils.toComponent("&cUsage: /" + label + " type <crate> <choose_one|gacha>"));
            return true;
        }

        CrateManager.OpenType openType;
        try {
            openType = CrateManager.OpenType.valueOf(args[2].trim().toUpperCase().replace('-', '_'));
        } catch (IllegalArgumentException exception) {
            sender.sendMessage(ColorUtils.toComponent("&ctype must be &fchoose_one &c(or &fchoose-one&c) or &fgacha&c."));
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
            sender.sendMessage(ColorUtils.toComponent("&cYou do not have permission to reload crate settings."));
            return true;
        }

        plugin.getConfigManager().reloadCrates();
        plugin.getCrateManager().reload();
        plugin.getCrateVisualManager().reload();
        sender.sendMessage(ColorUtils.toComponent("&aCrate settings reloaded."));
        return true;
    }

    private boolean handleKeyMutation(CommandSender sender, String[] args, MutationMode mode) {
        if (!PermissionUtils.has(sender, ADMIN_PERMISSION)) {
            sender.sendMessage(ColorUtils.toComponent("&cYou do not have permission to modify crate keys."));
            return true;
        }

        if (args.length < 4) {
            sender.sendMessage(ColorUtils.toComponent("&cUsage: /crate " + mode.commandName + " <player> <crate> <amount>"));
            return true;
        }

        ResolvedTarget target = resolveTarget(args[1]);
        if (target == null) {
            sender.sendMessage(ColorUtils.toComponent("&cPlayer '&f" + args[1] + "&c' was not found."));
            return true;
        }

        CrateManager.CrateDefinition crate = plugin.getCrateManager().getCrate(args[2]);
        if (crate == null) {
            sender.sendMessage(ColorUtils.toComponent("&cCrate '&f" + args[2] + "&c' was not found."));
            return true;
        }

        Integer amount = parsePositiveInt(args[3]);
        if ((amount == null || amount <= 0) && mode != MutationMode.SET) {
            sender.sendMessage(ColorUtils.toComponent("&cAmount must be a positive integer."));
            return true;
        }
        if (mode == MutationMode.SET && (amount == null || amount < 0)) {
            sender.sendMessage(ColorUtils.toComponent("&cAmount must be zero or a positive integer."));
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
            sender.sendMessage(ColorUtils.toComponent("&c" + target.name() + " does not have enough keys to remove " + amount + "."));
            return true;
        }

        sender.sendMessage(ColorUtils.toComponent("&a" + mode.successPrefix + " &f" + amount + "x "
                + plugin.getCrateManager().getReadableCrateName(crate)
                + "&a for &f" + target.name() + "&a. balance: &f" + balance));

        Player online = Bukkit.getPlayer(target.uuid());
        if (online != null && online.isOnline()) {
            online.sendMessage(ColorUtils.toComponent("&7Your &b" + plugin.getCrateManager().getReadableCrateName(crate)
                    + "&7 key balance is now &f" + balance + "&7."));
        }
        return true;
    }

    private boolean handleKeyAll(CommandSender sender, String label, String[] args) {
        if (!PermissionUtils.has(sender, KEYALL_PERMISSION) && !PermissionUtils.has(sender, ADMIN_PERMISSION)) {
            sender.sendMessage(ColorUtils.toComponent("&cYou do not have permission to run crate key-all."));
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(ColorUtils.toComponent("&cUsage: /" + label + " keyall <crate> <amount>"));
            return true;
        }

        CrateManager.CrateDefinition crate = plugin.getCrateManager().getCrate(args[1]);
        if (crate == null) {
            sender.sendMessage(ColorUtils.toComponent("&cCrate '&f" + args[1] + "&c' was not found."));
            return true;
        }

        Integer amount = parsePositiveInt(args[2]);
        if (amount == null) {
            sender.sendMessage(ColorUtils.toComponent("&cAmount must be a positive integer."));
            return true;
        }

        int granted = plugin.getKeyAllManager().grantCrateKeys(crate.id(), amount, false);
        sender.sendMessage(ColorUtils.toComponent("&aGranted &f" + amount + "x "
                + plugin.getCrateManager().getReadableCrateName(crate)
                + "&a key(s) to &f" + granted + "&a online player(s)."));
        return true;
    }

    private boolean handleRewardMutation(CommandSender sender, String label, String[] args, RewardMutationMode mode) {
        if (!PermissionUtils.has(sender, ADMIN_PERMISSION)) {
            sender.sendMessage(ColorUtils.toComponent("&cYou do not have permission to modify crate rewards."));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtils.toComponent("&cOnly players can use /crate " + mode.commandName + "."));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ColorUtils.toComponent("&cUsage: /" + label + " " + mode.commandName + " <crate> [slot]"));
            return true;
        }

        CrateManager.CrateDefinition crate = plugin.getCrateManager().getCrate(args[1]);
        if (crate == null) {
            sender.sendMessage(ColorUtils.toComponent("&cCrate '&f" + args[1] + "&c' was not found."));
            return true;
        }

        if ((mode == RewardMutationMode.ADD || mode == RewardMutationMode.EDIT) && args.length == 2) {
            new CrateEditorMenu(plugin, crate.id()).open(player);
            return true;
        }

        if (mode == RewardMutationMode.REMOVE && args.length == 2) {
            sender.sendMessage(ColorUtils.toComponent("&cUsage: /" + label + " remove <crate> <slot>"));
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(ColorUtils.toComponent("&cUsage: /" + label + " " + mode.commandName + " <crate> <slot>"));
            return true;
        }

        Integer slot = parsePositiveInt(args[2]);
        if (slot == null || slot < 0) {
            sender.sendMessage(ColorUtils.toComponent("&cSlot must be a valid number, for example &f10&c."));
            return true;
        }

        CrateManager.ActionResult result;
        // Support: /crate add <crate> <slot> command <console command...>
        if (mode == RewardMutationMode.ADD && args.length >= 4) {
            String verb = args[3].toLowerCase(Locale.ROOT);
            if (verb.equals("command")) {
                if (args.length < 5) {
                    sender.sendMessage(ColorUtils.toComponent("&cUsage: /crate add <crate> <slot> command <console command...>"));
                    return true;
                }
                String consoleCommand = String.join(" ", Arrays.copyOfRange(args, 4, args.length));
                result = plugin.getCrateManager().addCommandReward(crate.id(), slot, List.of(consoleCommand));
            } else if (verb.equals("money")) {
                if (args.length < 5) {
                    sender.sendMessage(ColorUtils.toComponent("&cUsage: /crate add <crate> <slot> money <amount>"));
                    return true;
                }
                Double parsed = parseDouble(args[4]);
                if (parsed == null) {
                    sender.sendMessage(ColorUtils.toComponent("&camount must be a number."));
                    return true;
                }
                result = plugin.getCrateManager().addMoneyReward(crate.id(), slot, parsed);
            } else if (verb.equals("shards")) {
                if (args.length < 5) {
                    sender.sendMessage(ColorUtils.toComponent("&cUsage: /crate add <crate> <slot> shards <amount>"));
                    return true;
                }
                Long parsed = parseLong(args[4]);
                if (parsed == null) {
                    sender.sendMessage(ColorUtils.toComponent("&camount must be an integer."));
                    return true;
                }
                result = plugin.getCrateManager().addShardsReward(crate.id(), slot, parsed);
            } else {
                // fallback to item-handling
                result = plugin.getCrateManager().addItemReward(crate.id(), slot, player.getInventory().getItemInMainHand());
            }
        } else {
            result = switch (mode) {
                case ADD -> plugin.getCrateManager().addItemReward(crate.id(), slot, player.getInventory().getItemInMainHand());
                case EDIT -> plugin.getCrateManager().editItemReward(crate.id(), slot, player.getInventory().getItemInMainHand());
                case REMOVE -> plugin.getCrateManager().removeReward(crate.id(), slot);
            };
        }

        sender.sendMessage(ColorUtils.toComponent(result.message()));
        return true;
    }

    private boolean handleBind(CommandSender sender, String label, String[] args) {
        if (!PermissionUtils.has(sender, ADMIN_PERMISSION)) {
            sender.sendMessage(ColorUtils.toComponent("&cYou do not have permission to bind crate chests."));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtils.toComponent("&cOnly players can bind crate chests."));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ColorUtils.toComponent("&cUsage: /" + label + " bind <crate|cancel>"));
            return true;
        }

        if (args[1].equalsIgnoreCase("cancel")) {
            plugin.getCrateManager().clearPendingBind(player.getUniqueId());
            player.sendMessage(ColorUtils.toComponent("&aCrate bind mode cancelled."));
            return true;
        }

        CrateManager.CrateDefinition crate = plugin.getCrateManager().getCrate(args[1]);
        if (crate == null) {
            sender.sendMessage(ColorUtils.toComponent("&cCrate '&f" + args[1] + "&c' was not found."));
            return true;
        }

        plugin.getCrateManager().startPendingBind(player.getUniqueId(), crate.id());
        player.sendMessage(ColorUtils.toComponent("&aBind mode enabled for &f" + crate.id() + "&a."));
        player.sendMessage(ColorUtils.toComponent("&7Left-click a chest, trapped chest, barrel, ender chest, or shulker box to bind it."));
        return true;
    }

    private boolean handleUnbind(CommandSender sender, String[] args) {
        if (!PermissionUtils.has(sender, ADMIN_PERMISSION)) {
            sender.sendMessage(ColorUtils.toComponent("&cYou do not have permission to unbind crate chests."));
            return true;
        }

        if (args.length >= 5) {
            String worldName = args[1];
            Integer x = parseInteger(args[2]);
            Integer y = parseInteger(args[3]);
            Integer z = parseInteger(args[4]);
            if (x == null || y == null || z == null) {
                sender.sendMessage(ColorUtils.toComponent("&cInvalid coordinates. usage: /crate unbind <world> <x> <y> <z>"));
                return true;
            }

            if (!plugin.getCrateManager().unbindCrateBlock(worldName, x, y, z)) {
                sender.sendMessage(ColorUtils.toComponent("&cFailed to unbind that crate chest (or it was not bound)."));
                return true;
            }

            plugin.getCrateVisualManager().removeHologram(worldName, x, y, z);
            sender.sendMessage(ColorUtils.toComponent("&aRemoved crate binding at &f" + worldName + " " + x + "," + y + "," + z + "&a."));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtils.toComponent("&cOnly players can unbind crate chests by looking at them. use: /crate unbind <world> <x> <y> <z>"));
            return true;
        }

        Block target = getTargetBlock(player);
        if (target == null) {
            player.sendMessage(ColorUtils.toComponent("&cLook at a bound crate chest first."));
            return true;
        }

        String crateId = plugin.getCrateManager().getBoundCrateId(target);
        if (crateId == null) {
            player.sendMessage(ColorUtils.toComponent("&cThat block is not bound to any crate."));
            return true;
        }

        if (!plugin.getCrateManager().unbindCrateBlock(target)) {
            player.sendMessage(ColorUtils.toComponent("&cFailed to unbind that crate chest."));
            return true;
        }

        plugin.getCrateVisualManager().removeHologram(target);
        player.sendMessage(ColorUtils.toComponent("&aRemoved crate binding from &f" + formatBlockLocation(target) + "&a."));
        return true;
    }

    private boolean handleListBound(CommandSender sender) {
        if (!PermissionUtils.has(sender, ADMIN_PERMISSION)) {
            sender.sendMessage(ColorUtils.toComponent("&cYou do not have permission to list bound crates."));
            return true;
        }

        var bound = plugin.getCrateManager().getBoundBlockIds();
        if (bound.isEmpty()) {
            sender.sendMessage(ColorUtils.toComponent("&cNo crates are currently bound."));
            return true;
        }

        sender.sendMessage(ColorUtils.toComponent("&8&m-------- &bBound crates &8&m--------"));
        for (var entry : bound.entrySet()) {
            var key = entry.getKey();
            sender.sendMessage(ColorUtils.toComponent("&7- &f" + key.world() + " &7(&f" + key.x() + "," + key.y() + "," + key.z() + "&7) -> &b" + entry.getValue()));
        }
        sender.sendMessage(ColorUtils.toComponent("&8&m--------------------------------"));
        return true;
    }

    private Integer parseInteger(String input) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private boolean handleInfo(CommandSender sender) {
        if (!PermissionUtils.has(sender, ADMIN_PERMISSION)) {
            sender.sendMessage(ColorUtils.toComponent("&cYou do not have permission to inspect crate chests."));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtils.toComponent("&cOnly players can inspect crate chests."));
            return true;
        }

        Block target = getTargetBlock(player);
        if (target == null) {
            player.sendMessage(ColorUtils.toComponent("&cLook at a crate chest first."));
            return true;
        }

        String crateId = plugin.getCrateManager().getBoundCrateId(target);
        if (crateId == null) {
            player.sendMessage(ColorUtils.toComponent("&cThat block is not bound to any crate."));
            return true;
        }

        CrateManager.CrateDefinition crate = plugin.getCrateManager().getCrate(crateId);
        player.sendMessage(ColorUtils.toComponent("&8&m-------- &bCrate chest &8&m--------"));
        player.sendMessage(ColorUtils.toComponent("&7Location: &f" + formatBlockLocation(target)));
        player.sendMessage(ColorUtils.toComponent("&7Crate id: &f" + crateId));
        player.sendMessage(ColorUtils.toComponent("&7Display: &f" + plugin.getCrateManager().getReadableCrateName(crate)));
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

    private Double parseDouble(String input) {
        try {
            return Double.parseDouble(input);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private Long parseLong(String input) {
        try {
            return Long.parseLong(input);
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
            sender.sendMessage(ColorUtils.toComponent("&cOnly players can view crate keys."));
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

    private List<String> unbindWorldSuggestions() {
        return plugin.getCrateManager().getBoundBlockIds().keySet().stream()
                .map(CrateManager.CrateBlockKey::world)
                .distinct()
                .toList();
    }

    private List<String> unbindXSuggestions(String world) {
        return plugin.getCrateManager().getBoundBlockIds().keySet().stream()
                .filter(key -> key.world().equalsIgnoreCase(world))
                .map(key -> String.valueOf(key.x()))
                .distinct()
                .toList();
    }

    private List<String> unbindYSuggestions(String world, String xStr) {
        Integer x = parseInteger(xStr);
        if (x == null) return Collections.emptyList();
        return plugin.getCrateManager().getBoundBlockIds().keySet().stream()
                .filter(key -> key.world().equalsIgnoreCase(world) && key.x() == x)
                .map(key -> String.valueOf(key.y()))
                .distinct()
                .toList();
    }

    private List<String> unbindZSuggestions(String world, String xStr, String yStr) {
        Integer x = parseInteger(xStr);
        Integer y = parseInteger(yStr);
        if (x == null || y == null) return Collections.emptyList();
        return plugin.getCrateManager().getBoundBlockIds().keySet().stream()
                .filter(key -> key.world().equalsIgnoreCase(world) && key.x() == x && key.y() == y)
                .map(key -> String.valueOf(key.z()))
                .distinct()
                .toList();
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
