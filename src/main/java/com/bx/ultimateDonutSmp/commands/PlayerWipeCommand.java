package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.DatabaseManager;
import com.bx.ultimateDonutSmp.managers.PlayerWipeManager;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.PermissionUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class PlayerWipeCommand implements CommandExecutor, TabCompleter {

    private static final String PERMISSION = "ultimatedonutsmp.admin.playerwipe";
    private static final long STORED_NAMES_TTL_MS = 60_000L;

    private final UltimateDonutSmp plugin;
    private final AtomicBoolean storedNamesLoading = new AtomicBoolean();

    private volatile List<String> storedNames = List.of();
    private volatile long storedNamesLoadedAt;

    public PlayerWipeCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!PermissionUtils.has(sender, PERMISSION)) {
            send(sender, "&cYou do not have permission to wipe player data.");
            return true;
        }
        if (args.length == 0) {
            send(sender, "&cUsage: /" + label + " <player> [confirm]");
            return true;
        }

        PlayerWipeManager wipeManager = plugin.getPlayerWipeManager();
        PlayerWipeManager.Target target = wipeManager.resolveTarget(args[0]);
        if (target == null) {
            send(sender, "&cNo player named &f" + args[0] + " &chas ever joined this server.");
            return true;
        }

        if (args.length < 2 || !args[1].equalsIgnoreCase("confirm")) {
            sendPreview(sender, label, target);
            return true;
        }

        PlayerWipeManager.WipeResult result = wipeManager.wipe(target, sender.getName());
        if (result.busy()) {
            send(sender, "&cA player wipe is already running.");
            return true;
        }
        if (!result.success()) {
            String error = result.errorMessage() == null || result.errorMessage().isBlank()
                    ? "unknown error"
                    : result.errorMessage();
            send(sender, "&cPlayer wipe failed: &f" + error);
            return true;
        }

        send(sender, "&aWiped &f" + target.name() + "&a. Records removed: &f" + result.counts().total() + "&a.");
        for (String key : PlayerWipeManager.COUNT_KEYS) {
            int affected = result.counts().affected(key);
            if (affected > 0) {
                send(sender, "&8- &7" + PlayerWipeManager.label(key) + ": &f" + affected);
            }
        }
        return true;
    }

    private void sendPreview(CommandSender sender, String label, PlayerWipeManager.Target target) {
        DatabaseManager.PlayerWipePreview preview = plugin.getPlayerWipeManager().preview(target.uuid());
        send(sender, "&6Player wipe preview &8- &f" + target.name());

        if (preview.total() == 0) {
            send(sender, "&7This plugin has nothing stored for them.");
            return;
        }

        for (String key : PlayerWipeManager.COUNT_KEYS) {
            int count = preview.count(key);
            if (count > 0) {
                send(sender, "&8- &7" + PlayerWipeManager.label(key) + ": &f" + count);
            }
        }
        send(sender, "&7Punishments, IP history and their placed spawners are kept.");
        send(sender, "&cThis cannot be undone. Run &f/" + label + " " + target.name() + " confirm &cto wipe them.");
    }

    private void send(CommandSender sender, String message) {
        sender.sendMessage(ColorUtils.toComponent(message));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!PermissionUtils.has(sender, PERMISSION)) {
            return List.of();
        }
        if (args.length == 1) {
            return matches(knownPlayerNames(), args[0]);
        }
        if (args.length == 2) {
            return matches(List.of("confirm"), args[1]);
        }
        return List.of();
    }

    private List<String> knownPlayerNames() {
        Set<String> names = new LinkedHashSet<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            names.add(player.getName());
        }
        names.addAll(storedNames());
        return names.stream().sorted(String.CASE_INSENSITIVE_ORDER).toList();
    }

    /**
     * Tab completion runs on the main thread and fires on every keystroke, so the stored names come
     * from a snapshot that is refreshed off-thread rather than from a query the server waits on.
     */
    private List<String> storedNames() {
        long now = System.currentTimeMillis();
        if (now - storedNamesLoadedAt >= STORED_NAMES_TTL_MS && storedNamesLoading.compareAndSet(false, true)) {
            plugin.getSpigotScheduler().runAsync(() -> {
                try {
                    storedNames = List.copyOf(plugin.getDatabaseManager().loadKnownPlayerNames());
                    storedNamesLoadedAt = System.currentTimeMillis();
                } finally {
                    storedNamesLoading.set(false);
                }
            });
        }
        return storedNames;
    }

    private List<String> matches(List<String> candidates, String input) {
        String normalized = input.toLowerCase(Locale.ROOT);
        List<String> matches = new ArrayList<>();
        for (String candidate : candidates) {
            if (candidate.toLowerCase(Locale.ROOT).startsWith(normalized)) {
                matches.add(candidate);
            }
        }
        return matches;
    }
}
