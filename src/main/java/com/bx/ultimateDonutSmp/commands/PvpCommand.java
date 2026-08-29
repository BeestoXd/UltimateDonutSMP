package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.PvpManager;
import com.bx.ultimateDonutSmp.menus.PvpAssignMatchMenu;
import com.bx.ultimateDonutSmp.menus.PvpKitEditMenu;
import com.bx.ultimateDonutSmp.menus.PvpLeaderboardMenu;
import com.bx.ultimateDonutSmp.menus.PvpMatchHistoryMenu;
import com.bx.ultimateDonutSmp.menus.PvpQueueMenu;
import com.bx.ultimateDonutSmp.models.PvpKit;
import com.bx.ultimateDonutSmp.models.PvpRank;
import com.bx.ultimateDonutSmp.models.PvpStats;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.PermissionUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * {@code /pvp} - the player entrance to the arena and the whole admin setup tree behind it.
 */
public class PvpCommand implements CommandExecutor, TabCompleter {

    private static final DecimalFormat RATIO_FORMAT = new DecimalFormat("0.00");
    private static final List<String> PLAYER_SUBCOMMANDS = List.of(
            "join", "leave", "kit", "stats", "top", "queue", "leaderboard", "history"
    );
    private static final List<String> ADMIN_SUBCOMMANDS = List.of(
            "wand", "create", "setspawn", "setspawn2", "setlobby", "setboundary",
            "schematic", "assign", "reset", "reload"
    );

    private final UltimateDonutSmp plugin;

    public PvpCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        PvpManager pvp = plugin.getPvpManager();
        if (pvp == null) {
            sender.sendMessage(ColorUtils.toComponent("&cThe PvP arena is not available."));
            return true;
        }

        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sendUsage(sender, label, true);
                return true;
            }
            pvp.join(player);
            return true;
        }

        String subcommand = args[0].toLowerCase(Locale.ROOT);
        return switch (subcommand) {
            case "join" -> requirePlayer(sender, player -> pvp.join(player));
            case "leave" -> requirePlayer(sender, pvp::leave);
            case "kit" -> handleKit(sender, label, args);
            case "stats" -> handleStats(sender, args);
            case "top" -> handleTop(sender, args);
            case "queue" -> handleQueue(sender, args);
            case "leaderboard", "lb" -> requirePlayer(sender, p -> new PvpLeaderboardMenu(plugin).open(p));
            case "history" -> handleHistory(sender, args);
            case "assign" -> handleAssign(sender, args);
            case "wand" -> handleWand(sender);
            case "create" -> handleCreate(sender, args);
            case "setspawn" -> handleSetSpawn(sender);
            case "setspawn2" -> handleSetSpawn2(sender);
            case "setlobby" -> handleSetLobby(sender);
            case "setboundary" -> handleSetBoundary(sender);
            case "schematic" -> handleSchematic(sender, label, args);
            case "reset" -> handleReset(sender);
            case "reload" -> handleReload(sender);
            default -> {
                sendUsage(sender, label, isAdmin(sender));
                yield true;
            }
        };
    }

    // ── Player subcommands ────────────────────────────────────────────────────

    private boolean handleKit(CommandSender sender, String label, String[] args) {
        PvpManager pvp = plugin.getPvpManager();
        if (args.length == 1) {
            return requirePlayer(sender, player -> {
                if (!pvp.isInArena(player.getUniqueId())) {
                    send(player, pvp.message("NOT_IN", "&cYou are not in the PvP arena."));
                    return;
                }
                pvp.openKitMenu(player);
            });
        }

        String action = args[1].toLowerCase(Locale.ROOT);
        if (action.equals("list")) {
            if (pvp.getKits().isEmpty()) {
                send(sender, "&cThere are no PvP kits yet.");
                return true;
            }
            send(sender, "&ePvP kits:");
            for (PvpKit kit : pvp.getKits()) {
                send(sender, "&7- &f" + kit.getId()
                        + " &8(" + kit.getDisplayName() + "&8)"
                        + " &7icon=&f" + kit.getIcon().name()
                        + " &7permission=&f" + (kit.getPermission().isBlank() ? "-" : kit.getPermission())
                        + " &7empty=&f" + kit.isEmpty());
            }
            return true;
        }

        if (!requireAdmin(sender)) {
            return true;
        }
        if (args.length < 3) {
            send(sender, "&e/" + label + " kit <create|edit|delete|icon|permission|slot|display> <name> [value]");
            return true;
        }

        String name = args[2].toLowerCase(Locale.ROOT);
        switch (action) {
            case "create" -> {
                if (pvp.getKit(name) != null) {
                    send(sender, "&cA kit called &f" + name + " &calready exists.");
                    return true;
                }
                PvpKit created = pvp.createKit(name);
                send(sender, "&aCreated the &f" + name + " &akit. Fill it in with &f/" + label + " kit edit " + name);
                if (sender instanceof Player player) {
                    new PvpKitEditMenu(plugin, created).open(player);
                }
            }
            case "edit" -> {
                PvpKit kit = pvp.getKit(name);
                if (kit == null) {
                    send(sender, "&cNo kit called &f" + name + "&c.");
                    return true;
                }
                if (!(sender instanceof Player player)) {
                    send(sender, "&cOnly a player can edit a kit.");
                    return true;
                }
                new PvpKitEditMenu(plugin, kit).open(player);
            }
            case "delete" -> {
                if (!pvp.deleteKit(name)) {
                    send(sender, "&cNo kit called &f" + name + "&c.");
                    return true;
                }
                send(sender, "&aDeleted the &f" + name + " &akit.");
            }
            case "icon", "permission", "slot", "display" -> {
                PvpKit kit = pvp.getKit(name);
                if (kit == null) {
                    send(sender, "&cNo kit called &f" + name + "&c.");
                    return true;
                }
                if (args.length < 4) {
                    send(sender, "&e/" + label + " kit " + action + " " + name + " <value>");
                    return true;
                }
                String value = String.join(" ", java.util.Arrays.copyOfRange(args, 3, args.length));
                applyKitProperty(sender, pvp, kit, action, value);
            }
            default -> send(sender, "&e/" + label + " kit <create|edit|delete|list|icon|permission|slot|display>");
        }
        return true;
    }

    private void applyKitProperty(CommandSender sender, PvpManager pvp, PvpKit kit, String action, String value) {
        switch (action) {
            case "icon" -> {
                org.bukkit.Material material = com.bx.ultimateDonutSmp.utils.ItemUtils.parseMaterial(value);
                if (material == null) {
                    send(sender, "&cUnknown material: &f" + value);
                    return;
                }
                kit.setIcon(material);
                send(sender, "&aIcon for &f" + kit.getId() + " &aset to &f" + material.name());
            }
            case "permission" -> {
                kit.setPermission(value.equalsIgnoreCase("none") ? "" : value);
                send(sender, "&aPermission for &f" + kit.getId() + " &aset to &f"
                        + (kit.getPermission().isBlank() ? "none" : kit.getPermission()));
            }
            case "slot" -> {
                try {
                    kit.setMenuSlot(Integer.parseInt(value.trim()));
                } catch (NumberFormatException exception) {
                    send(sender, "&cThat is not a slot number: &f" + value);
                    return;
                }
                send(sender, "&aSlot for &f" + kit.getId() + " &aset to &f" + kit.getMenuSlot());
            }
            default -> {
                kit.setDisplayName(value);
                send(sender, "&aName for &f" + kit.getId() + " &aset to &r" + value);
            }
        }
        pvp.saveKit(kit);
    }

    private boolean handleStats(CommandSender sender, String[] args) {
        PvpManager pvp = plugin.getPvpManager();
        UUID target;
        String name;

        if (args.length > 1) {
            OfflinePlayer offline = Bukkit.getOfflinePlayer(args[1]);
            if (offline.getUniqueId() == null) {
                send(sender, "&cNo player called &f" + args[1] + "&c.");
                return true;
            }
            target = offline.getUniqueId();
            name = offline.getName() == null ? args[1] : offline.getName();
        } else if (sender instanceof Player player) {
            target = player.getUniqueId();
            name = player.getName();
        } else {
            send(sender, "&e/pvp stats <player>");
            return true;
        }

        PvpStats stats = pvp.getStats(target);
        PvpRank rank = pvp.getRankFor(stats.getElo());
        send(sender, pvp.message("STATS_HEADER", "&8&m--------&r &cPvP stats: &f{player} &8&m--------")
                .replace("{player}", name));
        statLine(sender, pvp, "Rank", rank == null ? "-" : rank.getDisplay());
        statLine(sender, pvp, "Elo", String.valueOf(stats.getElo()));
        statLine(sender, pvp, "Level", String.valueOf(stats.getLevel()));
        statLine(sender, pvp, "XP", stats.getXp() + " / " + pvp.getXpForNextLevel(stats.getLevel()));
        statLine(sender, pvp, "Kills", String.valueOf(stats.getKills()));
        statLine(sender, pvp, "Deaths", String.valueOf(stats.getDeaths()));
        statLine(sender, pvp, "K/D", RATIO_FORMAT.format(stats.getKillDeathRatio()));
        statLine(sender, pvp, "Streak", stats.getStreak() + " (best " + stats.getBestStreak() + ")");
        statLine(sender, pvp, "Arena joins", String.valueOf(stats.getArenaJoins()));
        return true;
    }

    private void statLine(CommandSender sender, PvpManager pvp, String label, String value) {
        send(sender, pvp.message("STATS_LINE", "&7{label}: &f{value}")
                .replace("{label}", label)
                .replace("{value}", value));
    }

    private boolean handleTop(CommandSender sender, String[] args) {
        PvpManager pvp = plugin.getPvpManager();
        int limit = 10;
        if (args.length > 1) {
            try {
                limit = Math.max(1, Math.min(50, Integer.parseInt(args[1].trim())));
            } catch (NumberFormatException ignored) {
                limit = 10;
            }
        }

        List<PvpManager.TopEntry> top = pvp.getTop(limit);
        if (top.isEmpty()) {
            send(sender, pvp.message("TOP_EMPTY", "&7Nobody has fought in the arena yet."));
            return true;
        }

        send(sender, pvp.message("TOP_HEADER", "&8&m--------&r &cTop PvP players &8&m--------"));
        int position = 1;
        for (PvpManager.TopEntry entry : top) {
            PvpRank rank = pvp.getRankFor(entry.value());
            send(sender, pvp.message("TOP_LINE", "&7#{position} &f{player} &8- &c{elo} elo &8(&7{rank}&8)")
                    .replace("{position}", String.valueOf(position++))
                    .replace("{player}", entry.name())
                    .replace("{elo}", String.valueOf(entry.value()))
                    .replace("{rank}", rank == null ? "-" : rank.getDisplay()));
        }
        return true;
    }

    private boolean handleQueue(CommandSender sender, String[] args) {
        return requirePlayer(sender, player -> {
            if (args.length > 1 && args[1].equalsIgnoreCase("leave")) {
                boolean left = plugin.getPvpMatchManager().leaveQueue(player.getUniqueId());
                send(player, left
                        ? plugin.getPvpManager().message("QUEUE_LEFT", "&aYou left the ranked queue.")
                        : plugin.getPvpManager().message("QUEUE_NOT_IN", "&cYou are not in the queue."));
                return;
            }
            new PvpQueueMenu(plugin).open(player);
        });
    }

    private boolean handleHistory(CommandSender sender, String[] args) {
        UUID target;
        if (args.length > 1) {
            OfflinePlayer offline = Bukkit.getOfflinePlayer(args[1]);
            target = offline.getUniqueId();
        } else if (sender instanceof Player player) {
            target = player.getUniqueId();
        } else {
            send(sender, "&e/pvp history <player>");
            return true;
        }

        UUID resolved = target;
        return requirePlayer(sender, player -> new PvpMatchHistoryMenu(plugin, resolved).open(player));
    }

    /**
     * Opens the assign menu, or starts the match straight away when both names are given.
     *
     * <p>Naming both players skips the menu entirely, which is what a tester running the same
     * fixture repeatedly actually wants.</p>
     */
    private boolean handleAssign(CommandSender sender, String[] args) {
        if (!requireAdmin(sender)) {
            return true;
        }

        if (args.length >= 3) {
            Player first = Bukkit.getPlayerExact(args[1]);
            Player second = Bukkit.getPlayerExact(args[2]);
            if (first == null || second == null) {
                send(sender, "&cBoth players have to be online.");
                return true;
            }
            String kitId = args.length >= 4 ? args[3] : null;
            plugin.getPvpMatchManager().startMatch(first, second, kitId, sender);
            return true;
        }

        UUID first = args.length >= 2 && Bukkit.getPlayerExact(args[1]) != null
                ? Bukkit.getPlayerExact(args[1]).getUniqueId()
                : null;
        return requirePlayer(sender, player -> new PvpAssignMatchMenu(plugin, first, null).open(player));
    }

    // ── Admin subcommands ─────────────────────────────────────────────────────

    private boolean handleWand(CommandSender sender) {
        if (!requireAdmin(sender)) {
            return true;
        }
        return requirePlayer(sender, player -> {
            player.getInventory().addItem(plugin.getPvpManager().createWand());
            send(player, "&aLeft click for corner 1, right click for corner 2, then run &f/pvp setboundary&a.");
        });
    }

    private boolean handleCreate(CommandSender sender, String[] args) {
        if (!requireAdmin(sender)) {
            return true;
        }
        if (args.length < 2) {
            send(sender, "&e/pvp create <name>");
            return true;
        }

        plugin.getPvpManager().setArenaName(String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length)));
        send(sender, "&aArena named &f" + plugin.getPvpManager().getArenaName()
                + "&a. Set the spawn with &f/pvp setspawn&a.");
        return true;
    }

    private boolean handleSetSpawn(CommandSender sender) {
        if (!requireAdmin(sender)) {
            return true;
        }
        return requirePlayer(sender, player -> {
            plugin.getPvpManager().setSpawn(player.getLocation());
            send(player, "&aArena spawn set to where you are standing.");
        });
    }

    private boolean handleSetSpawn2(CommandSender sender) {
        if (!requireAdmin(sender)) {
            return true;
        }
        return requirePlayer(sender, player -> {
            plugin.getPvpManager().setSpawn2(player.getLocation());
            send(player, "&aSecond ranked match spawn set to where you are standing.");
        });
    }

    private boolean handleSetLobby(CommandSender sender) {
        if (!requireAdmin(sender)) {
            return true;
        }
        return requirePlayer(sender, player -> {
            plugin.getPvpManager().setLobby(player.getLocation());
            send(player, "&aArena lobby set to where you are standing.");
        });
    }

    private boolean handleSetBoundary(CommandSender sender) {
        if (!requireAdmin(sender)) {
            return true;
        }
        return requirePlayer(sender, player -> {
            PvpManager pvp = plugin.getPvpManager();
            Location first = pvp.getWandSelection(player.getUniqueId(), 1);
            Location second = pvp.getWandSelection(player.getUniqueId(), 2);
            if (first == null || second == null) {
                send(player, "&cSelect both corners with &f/pvp wand &cfirst.");
                return;
            }
            pvp.setBoundaryCorner(1, first);
            pvp.setBoundaryCorner(2, second);
            send(player, "&aArena boundary saved.");
        });
    }

    private boolean handleSchematic(CommandSender sender, String label, String[] args) {
        if (!requireAdmin(sender)) {
            return true;
        }
        if (args.length < 2) {
            send(sender, "&e/" + label + " schematic <load <name>|location>");
            return true;
        }

        PvpManager pvp = plugin.getPvpManager();
        String action = args[1].toLowerCase(Locale.ROOT);
        if (action.equals("load")) {
            if (args.length < 3) {
                send(sender, "&e/" + label + " schematic load <name>");
                return true;
            }
            pvp.setResetSchematic(args[2]);
            send(sender, "&aReset schematic set to &f" + args[2] + "&a.");
            return true;
        }

        if (action.equals("location")) {
            return requirePlayer(sender, player -> {
                pvp.setPasteLocation(player.getLocation());
                send(player, "&aSchematic paste location set to where you are standing.");
            });
        }

        send(sender, "&e/" + label + " schematic <load <name>|location>");
        return true;
    }

    private boolean handleReset(CommandSender sender) {
        if (!requireAdmin(sender)) {
            return true;
        }
        plugin.getPvpManager().resetArena(sender);
        send(sender, "&aRunning the arena reset.");
        return true;
    }

    private boolean handleReload(CommandSender sender) {
        if (!requireAdmin(sender)) {
            return true;
        }
        plugin.getConfigManager().reloadPvp();
        plugin.getPvpManager().reload();
        send(sender, "&aReloaded pvp.yml.");
        return true;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private boolean isAdmin(CommandSender sender) {
        return PermissionUtils.has(sender, "ultimatedonutsmp.admin.pvp");
    }

    private boolean requireAdmin(CommandSender sender) {
        if (isAdmin(sender)) {
            return true;
        }
        send(sender, "&cYou do not have permission to manage the PvP arena.");
        return false;
    }

    private boolean requirePlayer(CommandSender sender, java.util.function.Consumer<Player> action) {
        if (sender instanceof Player player) {
            action.accept(player);
        } else {
            send(sender, "&cOnly a player can run that.");
        }
        return true;
    }

    private void send(CommandSender sender, String text) {
        if (sender != null && text != null && !text.isBlank()) {
            sender.sendMessage(ColorUtils.toComponent(text));
        }
    }

    private void sendUsage(CommandSender sender, String label, boolean admin) {
        send(sender, "&e/" + label + " &7- join the arena");
        send(sender, "&e/" + label + " leave &7- leave the arena");
        send(sender, "&e/" + label + " kit &7- pick a kit again");
        send(sender, "&e/" + label + " stats [player] &7- see a record");
        send(sender, "&e/" + label + " top [amount] &7- see the elo ladder");
        send(sender, "&e/" + label + " queue &7- join the ranked 1v1 queue");
        send(sender, "&e/" + label + " leaderboard &7- open the leaderboards");
        send(sender, "&e/" + label + " history [player] &7- browse ranked matches");
        if (!admin) {
            return;
        }
        send(sender, "&e/" + label + " wand &7- get the boundary wand");
        send(sender, "&e/" + label + " create <name> &7- name the arena");
        send(sender, "&e/" + label + " setspawn &8| &esetspawn2 &8| &esetlobby &8| &esetboundary");
        send(sender, "&e/" + label + " assign [player] [player] [kit] &7- put two players in a match");
        send(sender, "&e/" + label + " kit <create|edit|delete|list|icon|permission|slot|display>");
        send(sender, "&e/" + label + " schematic <load <name>|location>");
        send(sender, "&e/" + label + " reset &8| &ereload");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        PvpManager pvp = plugin.getPvpManager();
        List<String> options = new ArrayList<>();

        if (args.length == 1) {
            options.addAll(PLAYER_SUBCOMMANDS);
            if (isAdmin(sender)) {
                options.addAll(ADMIN_SUBCOMMANDS);
            }
            return filter(options, args[0]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("kit")) {
            options.add("list");
            if (isAdmin(sender)) {
                options.addAll(List.of("create", "edit", "delete", "icon", "permission", "slot", "display"));
            }
            return filter(options, args[1]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("schematic") && isAdmin(sender)) {
            return filter(List.of("load", "location"), args[1]);
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("kit") && pvp != null) {
            for (PvpKit kit : pvp.getKits()) {
                options.add(kit.getId());
            }
            return filter(options, args[2]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("queue")) {
            return filter(List.of("leave"), args[1]);
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("assign") && pvp != null && isAdmin(sender)) {
            for (PvpKit kit : pvp.getKits()) {
                options.add(kit.getId());
            }
            return filter(options, args[3]);
        }

        if ((args.length == 2 || args.length == 3)
                && (args[0].equalsIgnoreCase("history") || args[0].equalsIgnoreCase("assign"))) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                options.add(online.getName());
            }
            return filter(options, args[args.length - 1]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("stats")) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                options.add(online.getName());
            }
            return filter(options, args[1]);
        }

        return List.of();
    }

    private List<String> filter(List<String> options, String prefix) {
        String lower = prefix == null ? "" : prefix.toLowerCase(Locale.ROOT);
        List<String> matches = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase(Locale.ROOT).startsWith(lower)) {
                matches.add(option);
            }
        }
        return matches;
    }
}
