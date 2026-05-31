package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.utils.PermissionUtils;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.FeatureManager;
import com.bx.ultimateDonutSmp.models.AuctionListing;
import com.bx.ultimateDonutSmp.models.DuelArena;
import com.bx.ultimateDonutSmp.models.FfaArena;
import com.bx.ultimateDonutSmp.models.Home;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.models.SpawnerTypeDefinition;
import com.bx.ultimateDonutSmp.models.Team;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class UniversalCommandTabCompleter implements TabCompleter {

    private static final List<String> AMOUNTS = List.of(
            "1", "10", "100", "1K", "10K", "100K", "1M", "10M", "100M", "1B"
    );
    private static final List<String> DURATIONS = List.of("30s", "15m", "1h", "2h", "1d", "7d");
    private static final List<String> TOGGLES = List.of("true", "false", "on", "off");
    private static final List<String> TEAM_SUBCOMMANDS = List.of(
            "create", "disband", "invite", "join", "leave", "kick", "home", "sethome", "delhome", "chat", "pvp"
    );
    private static final List<String> ARENA_SUBCOMMANDS = List.of(
            "create", "delete", "setpos1", "setpos2", "setreturn", "setdisplay", "enable", "disable", "queue", "list", "reload"
    );
    private static final List<String> FFA_ARENA_SUBCOMMANDS = List.of(
            "create", "delete", "setpos", "setdisplay", "settings", "enable", "disable", "list", "reload"
    );
    private static final List<String> FFA_SETTINGS = List.of("nohunger", "noweather", "alwaysmorning", "nofalldamage");
    private static final List<String> CUBOID_SUBCOMMANDS = List.of(
            "wand", "create", "save", "delete", "list", "bind", "system", "reload"
    );
    private static final List<String> CUBOID_ROLES = List.of("spawn", "shard", "rtp-zone");
    private static final List<String> TELEPORT_ROOTS = List.of("here", "all", "top");

    private final UltimateDonutSmp plugin;

    public UniversalCommandTabCompleter(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!command.testPermissionSilent(sender) || !isFeatureEnabled(command.getName())) {
            return List.of();
        }

        String commandName = normalize(command.getName());
        String label = normalize(alias);
        return switch (commandName) {
            case "team" -> completeTeam(sender, args);
            case "home", "homes", "sethome", "delhome", "renamehome" -> completeHome(sender, commandName, args);
            case "rtp" -> singleArg(args, plugin.getRtpManager().getPortalSelectorSuggestions());
            case "shop" -> completeReloadOnly(sender, args, "ultimatedonutsmp.admin.shop");
            case "orders" -> completeOrders(sender, args);
            case "duel" -> completeDuel(sender, args);
            case "create" -> completeCreate(sender, args);
            case "queue" -> completeQueue(args);
            case "arena" -> completeArena(sender, args);
            case "ffa" -> completeFfa(sender, args);
            case "ffastats" -> completeKnownPlayer(args, sender, true);
            case "ffaarena" -> completeFfaArena(sender, args, 0);
            case "auctionhouse" -> completeAuctionHouse(sender, args);
            case "enderchest" -> completeReloadOnly(sender, args, "ultimatedonutsmp.admin.enderchest");
            case "sellhand" -> singleArg(args, AMOUNTS);
            case "worth" -> completeWorth(sender, args);
            case "balance", "stats", "playtime", "alts", "profileviewer", "punishments" ->
                    completeKnownPlayer(args, sender, true);
            case "ping", "findplayer" -> completeOnlinePlayer(args, sender, true);
            case "pay", "shardpay" -> completePayment(sender, args);
            case "addmoney", "removemoney", "setmoney", "addshards", "removeshards", "setshards" ->
                    completeMoneyAdmin(sender, args);
            case "shards" -> completeShards(sender, args);
            case "freeze" -> completePlayerOrReload(sender, args, "ultimatedonutsmp.admin.freeze", false);
            case "fly", "heal", "feed" -> completeOnlinePlayer(args, sender, true);
            case "staffmode" -> completePlayerOrReload(sender, args, "ultimatedonutsmp.admin.staffmode", true);
            case "helpop", "staffchat" -> List.of();
            case "rename" -> singleArg(args, List.of("reset", "clear", "remove"));
            case "randomteleport", "leave", "draw", "pm", "spawn", "afk", "sell", "sellall", "sellhistory",
                    "stafflist", "vanish", "tpauto", "tpahereauto", "nightvision", "phantom", "settings",
                    "discord", "twitter", "store", "social", "rules", "help", "servers", "billford",
                    "clearlag", "crates", "keys" -> List.of();
            case "teleport" -> completeTeleport(sender, label, args);
            case "invsee" -> completePlayerOrReload(sender, args, "ultimatedonutsmp.admin.invsee", false);
            case "ban", "tempban", "mute", "tempmute", "warn", "kick", "blacklist", "unban", "unmute",
                    "unblacklist" -> completePunishment(sender, commandName, args);
            case "bounty" -> completeBounty(sender, args);
            case "tpa", "tpahere" -> completeOnlinePlayer(args, sender, false);
            case "tpaccept", "tpadeny" -> completeOnlinePlayer(args, sender, true);
            case "leaderboard" -> singleArg(args, leaderboardTypes());
            case "spawner" -> completeSpawner(sender, args);
            case "cuboid" -> completeCuboid(sender, args);
            default -> List.of();
        };
    }

    private List<String> completeTeam(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            return List.of();
        }
        if (args.length == 1) {
            return partial(args[0], TEAM_SUBCOMMANDS);
        }
        if (args.length != 2) {
            return List.of();
        }

        return switch (normalize(args[0])) {
            case "invite" -> partial(args[1], onlinePlayerNames(sender, false));
            case "join" -> partial(args[1], plugin.getTeamManager().getPendingInvites(player.getUniqueId()));
            case "kick" -> partial(args[1], teamMemberNames(player, false));
            default -> List.of();
        };
    }

    private List<String> completeHome(CommandSender sender, String commandName, String[] args) {
        if (!(sender instanceof Player player)) {
            return List.of();
        }
        if (args.length != 1) {
            return List.of();
        }
        return switch (commandName) {
            case "home", "delhome", "renamehome" -> partial(args[0], homeNames(player));
            default -> List.of();
        };
    }

    private List<String> completeOrders(CommandSender sender, String[] args) {
        if (args.length != 1) {
            return List.of();
        }
        List<String> options = new ArrayList<>(List.of("my", "collect"));
        if (has(sender, "ultimatedonutsmp.admin.orders")) {
            options.add("reload");
        }
        return partial(args[0], options);
    }

    private List<String> completeDuel(CommandSender sender, String[] args) {
        if (args.length == 1) {
            List<String> options = new ArrayList<>(onlinePlayerNames(sender, false));
            options.addAll(List.of("accept", "deny", "claims"));
            if (has(sender, "ultimatedonutsmp.admin.duels")) {
                options.add("reload");
            }
            return partial(args[0], options);
        }
        if (args.length == 2 && Set.of("accept", "deny").contains(normalize(args[0]))) {
            return partial(args[1], onlinePlayerNames(sender, true));
        }
        return List.of();
    }

    private List<String> completeCreate(CommandSender sender, String[] args) {
        if (args.length == 1) {
            List<String> options = new ArrayList<>(List.of("invite", "friends"));
            options.addAll(onlinePlayerNames(sender, false));
            return partial(args[0], options);
        }

        String first = normalize(args[0]);
        boolean hasMode = first.equals("invite") || first.equals("friends");
        if (hasMode && args.length == 2) {
            return partial(args[1], onlinePlayerNames(sender, false));
        }
        if ((hasMode && args.length == 3) || (!hasMode && args.length == 2)) {
            return partial(args[args.length - 1], plugin.getDuelManager().getMapSelectionSuggestions(false));
        }
        return List.of();
    }

    private List<String> completeQueue(String[] args) {
        if (args.length == 1) {
            return partial(args[0], List.of("join", "leave"));
        }
        if (args.length == 2 && normalize(args[0]).equals("join")) {
            return partial(args[1], plugin.getDuelManager().getMapSelectionSuggestions(true));
        }
        return List.of();
    }

    private List<String> completeArena(CommandSender sender, String[] args) {
        if (!has(sender, "ultimatedonutsmp.admin.duels")) {
            return List.of();
        }
        if (args.length == 1) {
            return partial(args[0], ARENA_SUBCOMMANDS);
        }
        if (args.length == 2) {
            String sub = normalize(args[0]);
            if (Set.of("delete", "setpos1", "setpos2", "setreturn", "setdisplay", "enable", "disable", "queue").contains(sub)) {
                return partial(args[1], duelArenaIds());
            }
        }
        if (args.length == 3 && normalize(args[0]).equals("queue")) {
            return partial(args[2], TOGGLES);
        }
        return List.of();
    }

    private List<String> completeFfa(CommandSender sender, String[] args) {
        if (args.length == 1) {
            List<String> options = new ArrayList<>(List.of("join", "help"));
            if (has(sender, "ultimatedonutsmp.admin.ffa")) {
                options.add("arena");
                options.add("reload");
            }
            return partial(args[0], options);
        }
        if (args.length > 1 && normalize(args[0]).equals("arena")) {
            return completeFfaArena(sender, args, 1);
        }
        return List.of();
    }

    private List<String> completeFfaArena(CommandSender sender, String[] args, int offset) {
        if (!has(sender, "ultimatedonutsmp.admin.ffa")) {
            return List.of();
        }
        if (args.length == offset + 1) {
            return partial(args[offset], FFA_ARENA_SUBCOMMANDS);
        }

        String sub = normalize(args[offset]);
        if (args.length == offset + 2
                && Set.of("delete", "setpos", "setdisplay", "settings", "enable", "disable").contains(sub)) {
            return partial(args[offset + 1], ffaArenaIds());
        }
        if (args.length == offset + 3 && sub.equals("settings")) {
            return partial(args[offset + 2], FFA_SETTINGS);
        }
        if (args.length == offset + 4 && sub.equals("settings")) {
            return partial(args[offset + 3], TOGGLES);
        }
        return List.of();
    }

    private List<String> completeAuctionHouse(CommandSender sender, String[] args) {
        if (args.length == 1) {
            List<String> options = new ArrayList<>(List.of("sell", "my", "claims", "cancel"));
            if (has(sender, "ultimatedonutsmp.admin.auctionhouse")) {
                options.add("reload");
            }
            return partial(args[0], options);
        }
        if (args.length == 2 && normalize(args[0]).equals("sell")) {
            return partial(args[1], AMOUNTS);
        }
        if (args.length == 2 && normalize(args[0]).equals("cancel") && sender instanceof Player player) {
            return partial(args[1], ownAuctionListingIds(player));
        }
        return List.of();
    }

    private List<String> completeWorth(CommandSender sender, String[] args) {
        if (args.length != 1) {
            return List.of();
        }
        List<String> options = new ArrayList<>(List.of("hand", "held", "item", "check", "browse", "prices"));
        if (has(sender, "ultimatedonutsmp.admin.worth")) {
            options.add("reload");
        }
        return partial(args[0], options);
    }

    private List<String> completePayment(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return partial(args[0], knownPlayerNames(sender, false));
        }
        if (args.length == 2) {
            return partial(args[1], AMOUNTS);
        }
        return List.of();
    }

    private List<String> completeMoneyAdmin(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return partial(args[0], knownPlayerNames(sender, true));
        }
        if (args.length == 2) {
            return partial(args[1], AMOUNTS);
        }
        return List.of();
    }

    private List<String> completeShards(CommandSender sender, String[] args) {
        if (args.length == 1) {
            List<String> options = new ArrayList<>(knownPlayerNames(sender, true));
            if (has(sender, "ultimatedonutsmp.admin.shards")) {
                options.add("everywhere");
            }
            return partial(args[0], options);
        }
        if (!normalize(args[0]).equals("everywhere") || !has(sender, "ultimatedonutsmp.admin.shards")) {
            return List.of();
        }
        if (args.length == 2) {
            return partial(args[1], List.of("status", "debug"));
        }
        if (args.length == 3) {
            return partial(args[2], onlinePlayerNames(sender, true));
        }
        return List.of();
    }

    private List<String> completePlayerOrReload(CommandSender sender, String[] args, String adminPermission, boolean includeSelf) {
        if (args.length != 1) {
            return List.of();
        }
        List<String> options = new ArrayList<>(onlinePlayerNames(sender, includeSelf));
        if (has(sender, adminPermission)) {
            options.add("reload");
        }
        return partial(args[0], options);
    }

    private List<String> completeTeleport(CommandSender sender, String label, String[] args) {
        if (label.equals("tphere")) {
            return completeOnlinePlayer(args, sender, false);
        }
        if (label.equals("tpall")) {
            return List.of();
        }
        if (args.length == 1) {
            List<String> options = new ArrayList<>(onlinePlayerNames(sender, true));
            options.addAll(TELEPORT_ROOTS);
            return partial(args[0], options);
        }
        if (args.length == 2 && normalize(args[0]).equals("here")) {
            return partial(args[1], onlinePlayerNames(sender, false));
        }
        if (args.length == 4 && !normalize(args[0]).equals("here")) {
            return partial(args[3], worldNames());
        }
        return List.of();
    }

    private List<String> completePunishment(CommandSender sender, String commandName, String[] args) {
        if (args.length == 1) {
            boolean onlineOnly = commandName.equals("kick");
            return onlineOnly
                    ? partial(args[0], onlinePlayerNames(sender, true))
                    : partial(args[0], knownPlayerNames(sender, true));
        }
        if (args.length == 2 && (commandName.equals("tempban") || commandName.equals("tempmute"))) {
            return partial(args[1], DURATIONS);
        }
        return List.of();
    }

    private List<String> completeBounty(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return partial(args[0], List.of("add", "set", "info", "list"));
        }
        if (args.length == 2 && Set.of("add", "set", "info").contains(normalize(args[0]))) {
            return partial(args[1], knownPlayerNames(sender, false));
        }
        if (args.length == 3 && Set.of("add", "set").contains(normalize(args[0]))) {
            return partial(args[2], AMOUNTS);
        }
        return List.of();
    }

    private List<String> completeSpawner(CommandSender sender, String[] args) {
        boolean admin = has(sender, "ultimatedonutsmp.admin.spawner");
        if (args.length == 1) {
            List<String> options = new ArrayList<>(List.of("info"));
            if (admin) {
                options.addAll(List.of("give", "panel", "remove", "forcebreak", "reload"));
            }
            return partial(args[0], options);
        }
        if (!admin || !normalize(args[0]).equals("give")) {
            return List.of();
        }
        if (args.length == 2) {
            return partial(args[1], onlinePlayerNames(sender, true));
        }
        if (args.length == 3) {
            return partial(args[2], spawnerTypeKeys());
        }
        if (args.length == 4) {
            return partial(args[3], AMOUNTS);
        }
        return List.of();
    }

    private List<String> completeCuboid(CommandSender sender, String[] args) {
        if (!has(sender, "ultimatedonutsmp.admin.cuboid")) {
            return List.of();
        }
        if (args.length == 1) {
            return partial(args[0], CUBOID_SUBCOMMANDS);
        }
        if (args.length == 2 && Set.of("delete", "bind", "system").contains(normalize(args[0]))) {
            return partial(args[1], cuboidNames());
        }
        if (args.length == 3 && Set.of("bind", "system").contains(normalize(args[0]))) {
            return partial(args[2], CUBOID_ROLES);
        }
        if (args.length == 4 && Set.of("bind", "system").contains(normalize(args[0]))) {
            return partial(args[3], TOGGLES);
        }
        return List.of();
    }

    private List<String> completeReloadOnly(CommandSender sender, String[] args, String permission) {
        return args.length == 1 && has(sender, permission) ? partial(args[0], List.of("reload")) : List.of();
    }

    private List<String> completeOnlinePlayer(String[] args, CommandSender sender, boolean includeSelf) {
        return args.length == 1 ? partial(args[0], onlinePlayerNames(sender, includeSelf)) : List.of();
    }

    private List<String> completeKnownPlayer(String[] args, CommandSender sender, boolean includeSelf) {
        return args.length == 1 ? partial(args[0], knownPlayerNames(sender, includeSelf)) : List.of();
    }

    private List<String> singleArg(String[] args, Collection<String> options) {
        return args.length == 1 ? partial(args[0], options) : List.of();
    }

    private List<String> onlinePlayerNames(CommandSender sender, boolean includeSelf) {
        UUID senderUuid = sender instanceof Player player ? player.getUniqueId() : null;
        List<String> names = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!includeSelf && senderUuid != null && senderUuid.equals(player.getUniqueId())) {
                continue;
            }
            names.add(player.getName());
        }
        return names;
    }

    private List<String> knownPlayerNames(CommandSender sender, boolean includeSelf) {
        UUID senderUuid = sender instanceof Player player ? player.getUniqueId() : null;
        Set<String> names = new LinkedHashSet<>(onlinePlayerNames(sender, includeSelf));
        if (plugin.getPlayerDataManager() == null) {
            return new ArrayList<>(names);
        }
        for (PlayerData data : plugin.getPlayerDataManager().getAll()) {
            if (data == null || data.getUsername() == null || data.getUsername().isBlank()) {
                continue;
            }
            if (!includeSelf && senderUuid != null && senderUuid.equals(data.getUuid())) {
                continue;
            }
            names.add(data.getUsername());
        }
        return new ArrayList<>(names);
    }

    private List<String> homeNames(Player player) {
        if (plugin.getHomeManager() == null) {
            return List.of();
        }
        return plugin.getHomeManager().getHomes(player.getUniqueId()).stream()
                .map(Home::getName)
                .toList();
    }

    private List<String> teamMemberNames(Player player, boolean includeSelf) {
        Team team = plugin.getTeamManager().getTeam(player);
        if (team == null) {
            return List.of();
        }
        List<String> names = new ArrayList<>();
        for (UUID uuid : team.getMemberUuids()) {
            if (!includeSelf && player.getUniqueId().equals(uuid)) {
                continue;
            }
            Player online = Bukkit.getPlayer(uuid);
            if (online != null) {
                names.add(online.getName());
                continue;
            }
            String knownName = plugin.getDatabaseManager().getLastKnownUsername(uuid);
            if (knownName != null && !knownName.isBlank()) {
                names.add(knownName);
            }
        }
        return names;
    }

    private List<String> duelArenaIds() {
        return plugin.getDuelManager().getArenas().stream()
                .map(DuelArena::getId)
                .toList();
    }

    private List<String> ffaArenaIds() {
        return plugin.getFfaManager().getArenas().stream()
                .map(FfaArena::getId)
                .toList();
    }

    private List<String> ownAuctionListingIds(Player player) {
        return plugin.getAuctionHouseManager()
                .getActiveListingsForSeller(player.getUniqueId(), plugin.getAuctionHouseManager().getDefaultSort())
                .stream()
                .map(AuctionListing::id)
                .map(String::valueOf)
                .toList();
    }

    private List<String> leaderboardTypes() {
        return plugin.getLeaderboardManager().getTypes().stream()
                .map(type -> type.getConfigKey().toLowerCase(Locale.ROOT))
                .toList();
    }

    private List<String> spawnerTypeKeys() {
        return plugin.getSpawnerManager().getTypeDefinitions().stream()
                .map(SpawnerTypeDefinition::key)
                .toList();
    }

    private List<String> cuboidNames() {
        return new ArrayList<>(plugin.getCuboidManager().getCuboidNames());
    }

    private List<String> worldNames() {
        return Bukkit.getWorlds().stream()
                .map(World::getName)
                .toList();
    }

    private boolean isFeatureEnabled(String commandName) {
        if (plugin.getFeatureManager() == null) {
            return true;
        }
        for (FeatureManager.Feature feature : FeatureManager.featuresForCommand(commandName)) {
            if (feature != null && !plugin.getFeatureManager().isEnabled(feature)) {
                return false;
            }
        }
        return true;
    }

    private boolean has(CommandSender sender, String permission) {
        return permission == null || permission.isBlank() || PermissionUtils.has(sender, permission);
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private List<String> partial(String input, Collection<String> options) {
        if (options == null || options.isEmpty()) {
            return List.of();
        }
        String prefix = input == null ? "" : input.toLowerCase(Locale.ROOT);
        return options.stream()
                .filter(option -> option != null && !option.isBlank())
                .distinct()
                .filter(option -> option.toLowerCase(Locale.ROOT).startsWith(prefix))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }
}
