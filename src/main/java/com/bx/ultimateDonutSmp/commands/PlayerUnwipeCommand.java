package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.DatabaseManager;
import com.bx.ultimateDonutSmp.managers.PlayerUnwipeManager;
import com.bx.ultimateDonutSmp.managers.PlayerWipeManager;
import com.bx.ultimateDonutSmp.models.PlayerWipeArchive;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.PermissionUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/** Puts back the data a {@code /playerwipe} removed, from the backup that wipe left behind. */
public class PlayerUnwipeCommand implements CommandExecutor, TabCompleter {

    private static final String PERMISSION = "ultimatedonutsmp.admin.playerunwipe";
    private static final long BACKED_UP_NAMES_TTL_MS = 60_000L;

    private final UltimateDonutSmp plugin;
    private final AtomicBoolean backedUpNamesLoading = new AtomicBoolean();

    private volatile List<String> backedUpNames = List.of();
    private volatile long backedUpNamesLoadedAt;

    public PlayerUnwipeCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!PermissionUtils.has(sender, PERMISSION)) {
            send(sender, "&cYou do not have permission to restore player data.");
            return true;
        }
        if (args.length == 0) {
            send(sender, "&cUsage: /" + label + " <player> [confirm]");
            return true;
        }

        PlayerUnwipeManager unwipeManager = plugin.getPlayerUnwipeManager();
        UUID playerUuid = unwipeManager.resolveBackedUpPlayer(args[0]);
        PlayerWipeArchive archive = playerUuid == null ? null : unwipeManager.findLatest(playerUuid);
        if (archive == null) {
            send(sender, "&cThere is no wipe backup for &f" + args[0] + "&c.");
            send(sender, "&7Backups land in the &f" + unwipeManager.backupDirectory().getName()
                    + " &7folder the moment a wipe runs.");
            return true;
        }

        if (args.length < 2 || !args[1].equalsIgnoreCase("confirm")) {
            sendPreview(sender, label, archive);
            return true;
        }

        PlayerUnwipeManager.RestoreResult result = unwipeManager.restore(archive, sender.getName());
        if (result.busy()) {
            send(sender, "&cA player wipe or restore is already running.");
            return true;
        }
        if (!result.success()) {
            String error = result.errorMessage() == null || result.errorMessage().isBlank()
                    ? "unknown error"
                    : result.errorMessage();
            send(sender, "&cRestore failed: &f" + error);
            return true;
        }

        send(sender, "&aRestored &f" + archive.playerName() + "&a. Records put back: &f"
                + result.counts().total() + "&a.");
        for (String key : PlayerWipeManager.COUNT_KEYS) {
            int affected = result.counts().affected(key);
            if (affected > 0) {
                send(sender, "&8- &7" + PlayerWipeManager.label(key) + ": &f" + affected);
            }
        }
        return true;
    }

    private void sendPreview(CommandSender sender, String label, PlayerWipeArchive archive) {
        send(sender, "&6Restore preview &8- &f" + archive.playerName());
        send(sender, "&7Wiped by &f" + archive.wipedBy() + " &7on &f"
                + PlayerUnwipeManager.formatWipeTime(archive.wipedAt()) + "&7.");

        Map<String, Integer> counts = countsByGroup(archive);
        if (counts.isEmpty()) {
            send(sender, "&7That wipe found nothing to remove, so there is nothing to put back.");
            return;
        }

        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            send(sender, "&8- &7" + PlayerWipeManager.label(entry.getKey()) + ": &f" + entry.getValue());
        }
        send(sender, "&cAnything they have earned since that wipe is replaced. Run &f/" + label + " "
                + archive.playerName() + " confirm &cto restore them.");
    }

    /** Groups the backup's rows the way the wipe command counts them, so the two previews line up. */
    private Map<String, Integer> countsByGroup(PlayerWipeArchive archive) {
        Map<String, Integer> byGroup = new LinkedHashMap<>();
        if (archive.hasStats()) {
            byGroup.put("stats", 1);
        }
        for (Map.Entry<String, PlayerWipeArchive.TableRows> entry : archive.tables().entrySet()) {
            String group = DatabaseManager.playerWipeGroup(entry.getKey());
            if (group != null) {
                byGroup.merge(group, entry.getValue().rows().size(), Integer::sum);
            }
        }

        Map<String, Integer> ordered = new LinkedHashMap<>();
        for (String key : PlayerWipeManager.COUNT_KEYS) {
            Integer count = byGroup.get(key);
            if (count != null && count > 0) {
                ordered.put(key, count);
            }
        }
        return ordered;
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
            return matches(backedUpNames(), args[0]);
        }
        if (args.length == 2) {
            return matches(List.of("confirm"), args[1]);
        }
        return List.of();
    }

    /**
     * Tab completion fires on every keystroke, so the backup folder is listed off-thread and the
     * result reused for a minute rather than read from disk on the main thread each time.
     */
    private List<String> backedUpNames() {
        long now = System.currentTimeMillis();
        if (now - backedUpNamesLoadedAt >= BACKED_UP_NAMES_TTL_MS && backedUpNamesLoading.compareAndSet(false, true)) {
            plugin.getSpigotScheduler().runAsync(() -> {
                try {
                    backedUpNames = List.copyOf(plugin.getPlayerUnwipeManager().backedUpPlayerNames());
                    backedUpNamesLoadedAt = System.currentTimeMillis();
                } finally {
                    backedUpNamesLoading.set(false);
                }
            });
        }
        return backedUpNames;
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
