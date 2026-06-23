package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class FeatureManager {

    private static final String ROOT = "FEATURES";

    public enum Feature {
        CHAT("CHAT", "chat", "global chat commands and moderation controls.", "WRITABLE_BOOK", "CHAT"),
        IGNORE("IGNORE", "ignore", "player ignore and unignore commands.", "BARRIER", "IGNORE"),
        MESSAGING("MESSAGING", "messaging", "private messages, replies, and pm toggles.", "PAPER", "MESSAGE"),
        BOUNTY("BOUNTY", "bounty", "bounty command and menus.", "TARGET", "BOUNTY"),
        CUBOIDS("CUBOIDS", "cuboids", "cuboid region management and bound region helpers.", "WOODEN_AXE", "CUBOID"),
        AFK("AFK", "afk", "afk command, menus, and afk movement task.", "CLOCK", "AFK"),
        SHARDS("SHARDS", "shards", "shard balances, shard pay, passive rewards, and shard cuboids.", "AMETHYST_SHARD", "SHARDS"),
        WARPS("WARPS", "warps", "warp commands and warp manager commands.", "ENDER_PEARL", "WARP"),
        TEAMS("TEAMS", "teams", "team command, team homes, and team menus.", "IRON_HELMET", "TEAM"),
        BILLFORD("BILLFORD", "billford", "billford trade menu and rotation task.", "EMERALD", "BILLFORD"),
        HOMES("HOMES", "homes", "home commands and home menu.", "LIGHT_BLUE_BED", "HOME"),
        LEADERBOARDS("LEADERBOARDS", "leaderboards", "leaderboard commands and leaderboard menus.", "GOLD_INGOT", "LEADERBOARDS"),
        NIGHT_VISION("NIGHT_VISION", "night vision", "night vision player toggle command.", "GOLDEN_CARROT", "NIGHT-VISION"),
        PHANTOM("PHANTOM", "phantom toggle", "phantom spawning toggle command.", "PHANTOM_MEMBRANE", "PHANTOM"),
        RTP("RTP", "rtp", "random teleport command and rtp menu.", "COMPASS", "RTP"),
        RTP_ZONE("RTP_ZONE", "rtp zone", "cuboid-triggered rtp countdown zone.", "ENDER_EYE", null),
        SELL("SELL", "sell", "sell commands and sell menus.", "HOPPER", "SELL"),
        WORTH("WORTH", "worth", "worth browser and worth display helpers.", "EMERALD", "SELL"),
        SETTINGS("SETTINGS", "settings", "player settings menu.", "COMPARATOR", "SETTINGS"),
        SHOP("SHOP", "shop", "shop command and purchase menus.", "CHEST", "SHOP"),
        ENDER_CHEST("ENDER_CHEST", "ender chest", "custom ender chest command and listener.", "ENDER_CHEST", "ENDERCHEST"),
        GAMEMODE("GAMEMODE", "gamemode", "staff gamemode commands.", "GRASS_BLOCK", "GAMEMODE"),
        SOCIAL("SOCIAL", "social", "discord, twitter/x, store, and media commands.", "BOOK", "SOCIAL"),
        SPAWN("SPAWN", "spawn", "spawn command and spawn menu.", "BEACON", "SPAWN"),
        STATS("STATS", "stats", "stats, ping, and playtime commands.", "PLAYER_HEAD", "STATS"),
        TPA("TPA", "tpa", "teleport request commands and confirm menu.", "ENDER_PEARL", "TPA"),
        TPA_AUTO("TPA_AUTO", "tpa auto", "tpa auto-accept commands.", "REDSTONE_TORCH", "TPAUTO"),
        FIND_PLAYER("FIND_PLAYER", "find player", "staff find player command.", "SPYGLASS", "FINDPLAYER"),
        CRATES("CRATES", "crates", "crate commands, menus, key-all, and visual effects.", "TRIPWIRE_HOOK", "CRATE"),
        RULES("RULES", "rules", "rules command and rules menu.", "BOOKSHELF", "RULES"),
        HELP("HELP", "help", "help command and server info menu.", "KNOWLEDGE_BOOK", "HELP"),
        NETWORK_SERVERS("NETWORK_SERVERS", "network servers", "network server status command and menu.", "NETHER_STAR", "SERVERS"),
        SCOREBOARD("SCOREBOARD", "scoreboard", "sidebar scoreboard task and display.", "MAP", null),
        TABLIST("TABLIST", "tablist", "tablist header, footer, and player list names.", "NAME_TAG", null),
        AUCTION_HOUSE("AUCTION_HOUSE", "auction house", "auction house commands, listings, claims, and expiry task.", "GOLD_INGOT", null),
        ORDERS("ORDERS", "orders", "orders board commands, menus, and expiry task.", "WRITABLE_BOOK", null),
        DUELS("DUELS", "duels", "duel commands, queues, arenas, matches, and tasks.", "DIAMOND_SWORD", null),
        FFA("FFA", "ffa", "ffa commands, arenas, matches, and tasks.", "IRON_SWORD", null),
        STAFF_MODE("STAFF_MODE", "staff mode", "staff mode command, hotbar, vanish, and staff tools.", "NETHERITE_CHESTPLATE", null),
        STAFF_CHAT("STAFF_CHAT", "staff chat", "staff chat command and network staff chat.", "ECHO_SHARD", null),
        STAFF_ALERTS("STAFF_ALERTS", "staff alerts", "helpop, reports, and network staff alerts.", "BELL", null),
        SPAWN_STASH("SPAWN_STASH", "SpawnStash", "Staff bait stash spawning, alerts, and rollback cleanup.", "CHEST", "SPAWN-STASH"),
        FREEZE("FREEZE", "freeze", "freeze command, listeners, and freeze state enforcement.", "PACKED_ICE", null),
        INVSEE("INVSEE", "invsee", "inventory inspection command and sessions.", "CHEST_MINECART", null),
        PROFILE_VIEWER("PROFILE_VIEWER", "profile viewer", "profile viewer command and homes browser.", "PLAYER_HEAD", null),
        PUNISHMENTS("PUNISHMENTS", "punishments", "punishment commands, aliases, and history menus.", "IRON_AXE", null),
        SPAWNERS("SPAWNERS", "spawners", "managed spawner commands, listeners, visibility, and generation.", "SPAWNER", null),
        CLEAR_LAG("CLEAR_LAG", "clearlag", "clearlag command and cleanup task.", "LAVA_BUCKET", null),
        PORTALS("PORTALS", "portals", "portal triggers, manager command, and portal holograms.", "END_PORTAL_FRAME", null),
        AMETHYST_TOOLS("AMETHYST_TOOLS", "amethyst tools", "amethyst tool command, listener, and expiry task.", "AMETHYST_SHARD", null),
        COMBAT("COMBAT", "combat", "combat tagging listener and command blocking.", "SHIELD", null),
        FAST_CRYSTALS("FAST_CRYSTALS", "fast crystals", "fast crystal placement/breaking behavior.", "END_CRYSTAL", null),
        KEY_ALL("KEY_ALL", "key-all", "automatic crate key-all rewards.", "TRIPWIRE_HOOK", null),
        LUNAR_RICH_PRESENCE("LUNAR_RICH_PRESENCE", "lunar rich presence", "lunar client rich presence integration.", "ENDER_EYE", null),
        LUNAR_TEAM_VIEW("LUNAR_TEAM_VIEW", "lunar team view", "lunar teammate overlay integration.", "LEATHER_HELMET", null),
        OPTIMIZATION("OPTIMIZATION", "optimization", "runtime optimization monitor and adaptive task skipping.", "REDSTONE", null),
        MAINTENANCE("MAINTENANCE", "maintenance", "seamless maintenance system with lobby redirection.", "REDSTONE_LAMP", "MAINTENANCE"),
        HIDE("HIDE", "Hide", "Persistent player identity scrambling and configured disguises.", "NAME_TAG", "HIDE"),
        FRIENDS("FRIENDS", "friends", "player friends/follows system.", "PLAYER_HEAD", "FRIEND"),
        SAFETY("SAFETY", "safety", "safety command and info.", "BOOK", "SAFETY");

        private final String configKey;
        private final String displayName;
        private final String description;
        private final String iconMaterial;
        private final String legacyCommandKey;

        Feature(String configKey, String displayName, String description, String iconMaterial, String legacyCommandKey) {
            this.configKey = configKey;
            this.displayName = displayName;
            this.description = description;
            this.iconMaterial = iconMaterial;
            this.legacyCommandKey = legacyCommandKey;
        }

        public String configKey() {
            return configKey;
        }

        public String displayName() {
            return displayName;
        }

        public String description() {
            return description;
        }

        public String iconMaterial() {
            return iconMaterial;
        }

        public String legacyCommandKey() {
            return legacyCommandKey;
        }

        public static Optional<Feature> fromInput(String input) {
            String normalized = normalize(input);
            return Arrays.stream(values())
                    .filter(feature -> normalize(feature.configKey).equals(normalized)
                            || normalize(feature.name()).equals(normalized)
                            || normalize(feature.displayName).equals(normalized)
                            || (feature.legacyCommandKey != null
                            && normalize(feature.legacyCommandKey).equals(normalized)))
                    .findFirst();
        }

        public static Feature fromLegacyCommandKey(String key) {
            String normalized = normalize(key);
            return Arrays.stream(values())
                    .filter(feature -> normalize(feature.configKey).equals(normalized)
                            || (feature.legacyCommandKey != null
                            && normalize(feature.legacyCommandKey).equals(normalized)))
                    .findFirst()
                    .orElse(null);
        }

        private static String normalize(String value) {
            return value == null ? "" : value.trim()
                    .replace('-', '_')
                    .replace(' ', '_')
                    .toUpperCase(Locale.ROOT);
        }
    }

    private final UltimateDonutSmp plugin;

    public FeatureManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public List<Feature> getFeatures() {
        return List.of(Feature.values());
    }

    public boolean isEnabled(Feature feature) {
        return isEnabled(plugin.getConfigManager().getConfig(), feature);
    }

    public boolean areEnabled(Feature... features) {
        for (Feature feature : features) {
            if (feature != null && !isEnabled(feature)) {
                return false;
            }
        }
        return true;
    }

    public boolean isCommandFeatureEnabled(String commandName) {
        return areEnabled(featuresForCommand(commandName));
    }

    public static Feature[] featuresForCommand(String commandName) {
        String key = commandName == null ? "" : commandName.trim().toLowerCase(Locale.ROOT);
        return switch (key) {
            case "team" -> new Feature[]{Feature.TEAMS};
            case "chat" -> new Feature[]{Feature.CHAT};
            case "ignore", "unignore" -> new Feature[]{Feature.IGNORE};
            case "msg", "reply", "pm" -> new Feature[]{Feature.MESSAGING};
            case "home", "homes", "sethome", "delhome", "renamehome" -> new Feature[]{Feature.HOMES};
            case "spawn" -> new Feature[]{Feature.SPAWN};
            case "afk" -> new Feature[]{Feature.AFK};
            case "tpa", "tpahere", "tpaccept", "tpadeny", "tpacancel" -> new Feature[]{Feature.TPA};
            case "tpauto", "tpahereauto" -> new Feature[]{Feature.TPA, Feature.TPA_AUTO};
            case "shards", "shardpay", "addshards", "removeshards", "setshards" -> new Feature[]{Feature.SHARDS};
            case "crate", "crates", "keys" -> new Feature[]{Feature.CRATES};
            case "shop" -> new Feature[]{Feature.SHOP};
            case "shardshop" -> new Feature[]{Feature.SHOP, Feature.SHARDS};
            case "orders" -> new Feature[]{Feature.ORDERS};
            case "duel", "create", "queue", "draw", "arena" -> new Feature[]{Feature.DUELS};
            case "ffa", "ffastats", "ffaarena" -> new Feature[]{Feature.FFA};
            case "auctionhouse" -> new Feature[]{Feature.AUCTION_HOUSE};
            case "enderchest", "ecsee" -> new Feature[]{Feature.ENDER_CHEST};
            case "sell", "sellhand", "sellall", "sellhistory" -> new Feature[]{Feature.SELL};
            case "worth" -> new Feature[]{Feature.SELL, Feature.WORTH};
            case "rtp" -> new Feature[]{Feature.RTP};
            case "stats", "ping", "playtime" -> new Feature[]{Feature.STATS};
            case "leaderboard" -> new Feature[]{Feature.LEADERBOARDS};
            case "freeze" -> new Feature[]{Feature.FREEZE};
            case "gamemode" -> new Feature[]{Feature.GAMEMODE};
            case "staffmode", "stafflist", "vanish", "fakeplayer", "fplayer" -> new Feature[]{Feature.STAFF_MODE};
            case "staffchat" -> new Feature[]{Feature.STAFF_CHAT};
            case "helpop", "report" -> new Feature[]{Feature.STAFF_ALERTS};
            case "spawnstash", "stash" -> new Feature[]{Feature.SPAWN_STASH};
            case "invsee" -> new Feature[]{Feature.INVSEE};
            case "profileviewer" -> new Feature[]{Feature.PROFILE_VIEWER};
            case "punishments", "ban", "tempban", "mute", "tempmute", "warn", "kick", "blacklist",
                    "unban", "unmute", "unblacklist" -> new Feature[]{Feature.PUNISHMENTS};
            case "bounty" -> new Feature[]{Feature.BOUNTY};
            case "warp", "warpmanager", "setwarp", "delwarp" -> new Feature[]{Feature.WARPS};
            case "portalmanager" -> new Feature[]{Feature.PORTALS};
            case "nightvision" -> new Feature[]{Feature.NIGHT_VISION};
            case "phantom" -> new Feature[]{Feature.PHANTOM};
            case "findplayer" -> new Feature[]{Feature.FIND_PLAYER};
            case "settings" -> new Feature[]{Feature.SETTINGS};
            case "discord", "twitter", "store", "social" -> new Feature[]{Feature.SOCIAL};
            case "rules" -> new Feature[]{Feature.RULES};
            case "help" -> new Feature[]{Feature.HELP};
            case "servers" -> new Feature[]{Feature.NETWORK_SERVERS};
            case "billford" -> new Feature[]{Feature.BILLFORD};
            case "spawner" -> new Feature[]{Feature.SPAWNERS};
            case "clearlag" -> new Feature[]{Feature.CLEAR_LAG};
            case "hide", "disguise" -> new Feature[]{Feature.HIDE};
            case "cuboid" -> new Feature[]{Feature.CUBOIDS};
            case "amethysttool" -> new Feature[]{Feature.AMETHYST_TOOLS};
            case "friends", "friend" -> new Feature[]{Feature.FRIENDS};
            case "safety" -> new Feature[]{Feature.SAFETY};
            default -> new Feature[0];
        };
    }

    public static boolean isEnabled(FileConfiguration config, Feature feature) {
        if (config == null || feature == null) {
            return true;
        }

        String featurePath = path(feature);
        if (config.contains(featurePath)) {
            return config.getBoolean(featurePath, true);
        }

        String legacyKey = feature.legacyCommandKey();
        if (legacyKey != null && config.contains("COMMANDS." + legacyKey)) {
            return config.getBoolean("COMMANDS." + legacyKey, true);
        }

        return true;
    }

    public static boolean isCommandEnabled(FileConfiguration config, String commandKey) {
        Feature feature = Feature.fromLegacyCommandKey(commandKey);
        if (feature != null) {
            return isEnabled(config, feature);
        }
        return config == null || config.getBoolean("COMMANDS." + commandKey, true);
    }

    public boolean setEnabled(Feature feature, boolean enabled) {
        if (feature == null) {
            return false;
        }

        plugin.getConfigManager().getConfig().set(path(feature), enabled);
        if (!plugin.getConfigManager().saveConfig()) {
            return false;
        }
        applyRuntimeState(feature);
        return true;
    }

    public boolean toggle(Feature feature) {
        return setEnabled(feature, !isEnabled(feature));
    }

    public String statusText(Feature feature) {
        return isEnabled(feature)
                ? plugin.getConfigManager().getMessageOrDefault("FEATURES.STATUS-ENABLED", "&aᴇɴᴀʙʟᴇᴅ")
                : plugin.getConfigManager().getMessageOrDefault("FEATURES.STATUS-DISABLED", "&cᴅɪѕᴀʙʟᴇᴅ");
    }

    public void sendDisabledMessage(CommandSender sender, Feature feature, String commandLabel) {
        String message = plugin.getConfigManager().getMessageOrDefault(
                "FEATURES.DISABLED",
                "&cᴛʜᴇ {feature} ꜰᴇᴀᴛᴜʀᴇ ɪѕ ᴄᴜʀʀᴇɴᴛʟʏ ᴅɪѕᴀʙʟᴇᴅ.",
                "{feature}", feature.displayName(),
                "{feature_key}", feature.configKey(),
                "{command}", commandLabel == null ? "" : commandLabel
        );
        sender.sendMessage(ColorUtils.toComponent(message));
    }

    public void applyRuntimeState(Feature feature) {
        if (feature == null) {
            return;
        }

        switch (feature) {
            case SCOREBOARD -> {
                if (plugin.getScoreboardManager() != null) {
                    plugin.getScoreboardManager().updateAll();
                }
            }
            case TABLIST -> {
                if (plugin.getTablistManager() != null) {
                    plugin.getTablistManager().updateAll();
                    plugin.getTablistManager().updateNamesAll();
                }
            }
            case SHARDS -> {
                if (plugin.getShardManager() != null) {
                    plugin.getShardManager().reloadSettings();
                }
            }
            case RTP_ZONE -> {
                if (plugin.getRtpZoneManager() != null) {
                    plugin.getRtpZoneManager().reloadSettings();
                    for (Player player : plugin.getServer().getOnlinePlayers()) {
                        plugin.getRtpZoneManager().clearState(player);
                    }
                }
            }
            case RTP -> {
                if (plugin.getRtpManager() != null) {
                    plugin.getRtpManager().reload();
                }
                if (plugin.getRtpZoneManager() != null) {
                    plugin.getRtpZoneManager().reloadSettings();
                    for (Player player : plugin.getServer().getOnlinePlayers()) {
                        plugin.getRtpZoneManager().clearState(player);
                    }
                }
            }
            case CRATES -> {
                if (plugin.getCrateManager() != null) {
                    plugin.getCrateManager().reload();
                    plugin.getCrateManager().clearAllSessions();
                }
                if (plugin.getCrateVisualManager() != null) {
                    plugin.getCrateVisualManager().reload();
                }
            }
            case ENDER_CHEST -> {
                if (plugin.getEnderChestManager() != null) {
                    plugin.getEnderChestManager().reload();
                }
            }
            case STAFF_MODE -> {
                if (!isEnabled(feature) && plugin.getStaffModeManager() != null) {
                    plugin.getStaffModeManager().shutdown();
                }
            }
            case FREEZE -> {
                if (!isEnabled(feature) && plugin.getFreezeManager() != null) {
                    plugin.getFreezeManager().shutdown();
                }
            }
            case INVSEE -> {
                if (!isEnabled(feature) && plugin.getInvseeManager() != null) {
                    plugin.getInvseeManager().shutdown();
                }
            }
            case SPAWNERS -> {
                if (plugin.getSpawnerManager() != null) {
                    plugin.getSpawnerManager().reload();
                }
                if (plugin.getAntiEspManager() != null) {
                    plugin.getAntiEspManager().refreshAllPlayers();
                }
            }
            case AUCTION_HOUSE -> {
                if (plugin.getAuctionHouseManager() != null) {
                    plugin.getAuctionHouseManager().reload();
                }
            }
            case ORDERS -> {
                if (plugin.getOrdersManager() != null) {
                    plugin.getOrdersManager().reload();
                }
            }
            case DUELS -> {
                if (plugin.getDuelManager() != null) {
                    plugin.getDuelManager().reload();
                }
            }
            case FFA -> {
                if (plugin.getFfaManager() != null) {
                    plugin.getFfaManager().reload();
                }
            }
            case NETWORK_SERVERS -> {
                if (plugin.getNetworkStatusManager() != null) {
                    plugin.getNetworkStatusManager().reload();
                }
            }
            case STAFF_CHAT -> {
                if (plugin.getNetworkStaffChatManager() != null) {
                    plugin.getNetworkStaffChatManager().reload();
                }
            }
            case STAFF_ALERTS -> {
                if (plugin.getNetworkStaffAlertManager() != null) {
                    plugin.getNetworkStaffAlertManager().reload();
                }
            }
            case SPAWN_STASH -> {
                if (plugin.getSpawnStashManager() != null) {
                    if (isEnabled(feature)) {
                        plugin.getSpawnStashManager().reload();
                    } else {
                        plugin.getSpawnStashManager().shutdown();
                    }
                }
            }
            case LUNAR_RICH_PRESENCE -> {
                if (!isEnabled(feature) && plugin.getLunarRichPresenceManager() != null) {
                    plugin.getLunarRichPresenceManager().shutdown();
                } else if (plugin.getLunarRichPresenceManager() != null) {
                    plugin.getLunarRichPresenceManager().reload();
                } else {
                    plugin.initializeLunarRichPresenceManager();
                }
            }
            case OPTIMIZATION -> {
                if (plugin.getOptimizationManager() != null) {
                    plugin.getOptimizationManager().reload();
                }
            }
            default -> {
            }
        }
    }

    private static String path(Feature feature) {
        return ROOT + "." + feature.configKey() + ".ENABLED";
    }
}
