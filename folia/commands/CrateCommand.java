package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.CrateManager;
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
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Locale;
import java.util.UUID;

public class CrateCommand implements CommandExecutor {

    private static final String ADMIN_PERMISSION = "ultimatedonutsmp.admin.crate";
    private static final String RELOAD_PERMISSION = "ultimatedonutsmp.admin.crate.reload";
    private static final String KEYALL_PERMISSION = "ultimatedonutsmp.admin.crate.keyall";
    private static final int TARGET_BLOCK_DISTANCE = 6;

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
            case "unbind" -> handleUnbind(sender);
            case "info" -> handleInfo(sender);
            default -> sendCrateUsage(sender, label);
        };
    }

    private boolean handleCratesCommand(CommandSender sender, String label, String[] args) {
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
        if (!sender.hasPermission(ADMIN_PERMISSION)) {
            sender.sendMessage(ColorUtils.toComponent("&c/" + label + " is an admin crate command."));
            sender.sendMessage(ColorUtils.toComponent("&7Use &f/crates &7to open crates and &f/keys &7to view your keys."));
            return true;
        }

        sender.sendMessage(ColorUtils.toComponent("&8&m----------- &bCrate Admin &8&m-----------"));
        sender.sendMessage(ColorUtils.toComponent("&f/" + label + " create <crate> &7- Create a crate"));
        sender.sendMessage(ColorUtils.toComponent("&f/" + label + " delete <crate> &7- Delete a crate"));
        sender.sendMessage(ColorUtils.toComponent("&f/" + label + " type <crate> <choose_one|gacha> &7- Set crate type"));
        sender.sendMessage(ColorUtils.toComponent("&f/" + label + " open <crate> &7- Open a crate directly"));
        sender.sendMessage(ColorUtils.toComponent("&f/" + label + " key <player> <crate> <amount> &7- Give keys"));
        sender.sendMessage(ColorUtils.toComponent("&f/" + label + " take <player> <crate> <amount> &7- Remove keys"));
        sender.sendMessage(ColorUtils.toComponent("&f/" + label + " set <player> <crate> <amount> &7- Set key balance"));
        sender.sendMessage(ColorUtils.toComponent("&f/" + label + " keyall <crate> <amount> &7- Grant keys to online players"));
        sender.sendMessage(ColorUtils.toComponent("&f/" + label + " add <crate> [slot] &7- Add reward by GUI or hand"));
        sender.sendMessage(ColorUtils.toComponent("&f/" + label + " edit <crate> [slot] &7- Edit reward by GUI or hand"));
        sender.sendMessage(ColorUtils.toComponent("&f/" + label + " remove <crate> <slot> &7- Remove a reward"));
        sender.sendMessage(ColorUtils.toComponent("&f/" + label + " bind <crate|cancel> &7- Bind a crate chest"));
        sender.sendMessage(ColorUtils.toComponent("&f/" + label + " unbind &7- Unbind the looked-at crate chest"));
        sender.sendMessage(ColorUtils.toComponent("&f/" + label + " info &7- Inspect the looked-at crate chest"));
        sender.sendMessage(ColorUtils.toComponent("&f/" + label + " reload &7- Reload crate settings"));
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
        if (!sender.hasPermission(ADMIN_PERMISSION)) {
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
        if (!sender.hasPermission(ADMIN_PERMISSION)) {
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
        if (!sender.hasPermission(ADMIN_PERMISSION)) {
            sender.sendMessage(ColorUtils.toComponent("&cYou do not have permission to change crate types."));
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(ColorUtils.toComponent("&cUsage: /" + label + " type <crate> <choose_one|gacha>"));
            return true;
        }

        CrateManager.OpenType openType;
        try {
            openType = CrateManager.OpenType.valueOf(args[2].trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            sender.sendMessage(ColorUtils.toComponent("&cType must be &fchoose_one &cor &fgacha&c."));
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
        if (!sender.hasPermission(RELOAD_PERMISSION) && !sender.hasPermission(ADMIN_PERMISSION)) {
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
        if (!sender.hasPermission(ADMIN_PERMISSION)) {
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
                + "&a for &f" + target.name() + "&a. Balance: &f" + balance));

        Player online = Bukkit.getPlayer(target.uuid());
        if (online != null && online.isOnline()) {
            online.sendMessage(ColorUtils.toComponent("&7Your &b" + plugin.getCrateManager().getReadableCrateName(crate)
                    + "&7 key balance is now &f" + balance + "&7."));
        }
        return true;
    }

    private boolean handleKeyAll(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission(KEYALL_PERMISSION) && !sender.hasPermission(ADMIN_PERMISSION)) {
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
        if (!sender.hasPermission(ADMIN_PERMISSION)) {
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

        CrateManager.ActionResult result = switch (mode) {
            case ADD -> plugin.getCrateManager().addItemReward(crate.id(), slot, player.getInventory().getItemInMainHand());
            case EDIT -> plugin.getCrateManager().editItemReward(crate.id(), slot, player.getInventory().getItemInMainHand());
            case REMOVE -> plugin.getCrateManager().removeReward(crate.id(), slot);
        };

        sender.sendMessage(ColorUtils.toComponent(result.message()));
        return true;
    }

    private boolean handleBind(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission(ADMIN_PERMISSION)) {
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
        player.sendMessage(ColorUtils.toComponent("&7Left-click a chest, trapped chest, barrel, or ender chest to bind it."));
        return true;
    }

    private boolean handleUnbind(CommandSender sender) {
        if (!sender.hasPermission(ADMIN_PERMISSION)) {
            sender.sendMessage(ColorUtils.toComponent("&cYou do not have permission to unbind crate chests."));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtils.toComponent("&cOnly players can unbind crate chests."));
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

    private boolean handleInfo(CommandSender sender) {
        if (!sender.hasPermission(ADMIN_PERMISSION)) {
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
        player.sendMessage(ColorUtils.toComponent("&8&m-------- &bCrate Chest &8&m--------"));
        player.sendMessage(ColorUtils.toComponent("&7Location: &f" + formatBlockLocation(target)));
        player.sendMessage(ColorUtils.toComponent("&7Crate ID: &f" + crateId));
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
