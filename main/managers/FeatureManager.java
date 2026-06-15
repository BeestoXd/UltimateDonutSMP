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
        CHAT("CHAT", "ᴄʜᴀᴛ", "ɢʟᴏʙᴀʟ ᴄʜᴀᴛ ᴄᴏᴍᴍᴀɴᴅѕ ᴀɴᴅ ᴍᴏᴅᴇʀᴀᴛɪᴏɴ ᴄᴏɴᴛʀᴏʟѕ.", "WRITABLE_BOOK", "CHAT"),
        IGNORE("IGNORE", "ɪɢɴᴏʀᴇ", "ᴘʟᴀʏᴇʀ ɪɢɴᴏʀᴇ ᴀɴᴅ ᴜɴɪɢɴᴏʀᴇ ᴄᴏᴍᴍᴀɴᴅѕ.", "BARRIER", "IGNORE"),
        MESSAGING("MESSAGING", "ᴍᴇѕѕᴀɢɪɴɢ", "ᴘʀɪᴠᴀᴛᴇ ᴍᴇѕѕᴀɢᴇѕ, ʀᴇᴘʟɪᴇѕ, ᴀɴᴅ ᴘᴍ ᴛᴏɢɢʟᴇѕ.", "PAPER", "MESSAGE"),
        BOUNTY("BOUNTY", "ʙᴏᴜɴᴛʏ", "ʙᴏᴜɴᴛʏ ᴄᴏᴍᴍᴀɴᴅ ᴀɴᴅ ᴍᴇɴᴜѕ.", "TARGET", "BOUNTY"),
        CUBOIDS("CUBOIDS", "ᴄᴜʙᴏɪᴅѕ", "ᴄᴜʙᴏɪᴅ ʀᴇɢɪᴏɴ ᴍᴀɴᴀɢᴇᴍᴇɴᴛ ᴀɴᴅ ʙᴏᴜɴᴅ ʀᴇɢɪᴏɴ ʜᴇʟᴘᴇʀѕ.", "WOODEN_AXE", "CUBOID"),
        AFK("AFK", "ᴀꜰᴋ", "ᴀꜰᴋ ᴄᴏᴍᴍᴀɴᴅ, ᴍᴇɴᴜѕ, ᴀɴᴅ ᴀꜰᴋ ᴍᴏᴠᴇᴍᴇɴᴛ ᴛᴀѕᴋ.", "CLOCK", "AFK"),
        SHARDS("SHARDS", "ѕʜᴀʀᴅѕ", "ѕʜᴀʀᴅ ʙᴀʟᴀɴᴄᴇѕ, ѕʜᴀʀᴅ ᴘᴀʏ, ᴘᴀѕѕɪᴠᴇ ʀᴇᴡᴀʀᴅѕ, ᴀɴᴅ ѕʜᴀʀᴅ ᴄᴜʙᴏɪᴅѕ.", "AMETHYST_SHARD", "SHARDS"),
        WARPS("WARPS", "ᴡᴀʀᴘѕ", "ᴡᴀʀᴘ ᴄᴏᴍᴍᴀɴᴅѕ ᴀɴᴅ ᴡᴀʀᴘ ᴍᴀɴᴀɢᴇʀ ᴄᴏᴍᴍᴀɴᴅѕ.", "ENDER_PEARL", "WARP"),
        TEAMS("TEAMS", "ᴛᴇᴀᴍѕ", "ᴛᴇᴀᴍ ᴄᴏᴍᴍᴀɴᴅ, ᴛᴇᴀᴍ ʜᴏᴍᴇѕ, ᴀɴᴅ ᴛᴇᴀᴍ ᴍᴇɴᴜѕ.", "IRON_HELMET", "TEAM"),
        BILLFORD("BILLFORD", "ʙɪʟʟꜰᴏʀᴅ", "ʙɪʟʟꜰᴏʀᴅ ᴛʀᴀᴅᴇ ᴍᴇɴᴜ ᴀɴᴅ ʀᴏᴛᴀᴛɪᴏɴ ᴛᴀѕᴋ.", "EMERALD", "BILLFORD"),
        HOMES("HOMES", "ʜᴏᴍᴇѕ", "ʜᴏᴍᴇ ᴄᴏᴍᴍᴀɴᴅѕ ᴀɴᴅ ʜᴏᴍᴇ ᴍᴇɴᴜ.", "LIGHT_BLUE_BED", "HOME"),
        LEADERBOARDS("LEADERBOARDS", "ʟᴇᴀᴅᴇʀʙᴏᴀʀᴅѕ", "ʟᴇᴀᴅᴇʀʙᴏᴀʀᴅ ᴄᴏᴍᴍᴀɴᴅѕ ᴀɴᴅ ʟᴇᴀᴅᴇʀʙᴏᴀʀᴅ ᴍᴇɴᴜѕ.", "GOLD_INGOT", "LEADERBOARDS"),
        NIGHT_VISION("NIGHT_VISION", "ɴɪɢʜᴛ ᴠɪѕɪᴏɴ", "ɴɪɢʜᴛ ᴠɪѕɪᴏɴ ᴘʟᴀʏᴇʀ ᴛᴏɢɢʟᴇ ᴄᴏᴍᴍᴀɴᴅ.", "GOLDEN_CARROT", "NIGHT-VISION"),
        PHANTOM("PHANTOM", "ᴘʜᴀɴᴛᴏᴍ ᴛᴏɢɢʟᴇ", "ᴘʜᴀɴᴛᴏᴍ ѕᴘᴀᴡɴɪɴɢ ᴛᴏɢɢʟᴇ ᴄᴏᴍᴍᴀɴᴅ.", "PHANTOM_MEMBRANE", "PHANTOM"),
        RTP("RTP", "ʀᴛᴘ", "ʀᴀɴᴅᴏᴍ ᴛᴇʟᴇᴘᴏʀᴛ ᴄᴏᴍᴍᴀɴᴅ ᴀɴᴅ ʀᴛᴘ ᴍᴇɴᴜ.", "COMPASS", "RTP"),
        RTP_ZONE("RTP_ZONE", "ʀᴛᴘ ᴢᴏɴᴇ", "ᴄᴜʙᴏɪᴅ-ᴛʀɪɢɢᴇʀᴇᴅ ʀᴛᴘ ᴄᴏᴜɴᴛᴅᴏᴡɴ ᴢᴏɴᴇ.", "ENDER_EYE", null),
        SELL("SELL", "ѕᴇʟʟ", "ѕᴇʟʟ ᴄᴏᴍᴍᴀɴᴅѕ ᴀɴᴅ ѕᴇʟʟ ᴍᴇɴᴜѕ.", "HOPPER", "SELL"),
        WORTH("WORTH", "ᴡᴏʀᴛʜ", "ᴡᴏʀᴛʜ ʙʀᴏᴡѕᴇʀ ᴀɴᴅ ᴡᴏʀᴛʜ ᴅɪѕᴘʟᴀʏ ʜᴇʟᴘᴇʀѕ.", "EMERALD", "SELL"),
        SETTINGS("SETTINGS", "ѕᴇᴛᴛɪɴɢѕ", "ᴘʟᴀʏᴇʀ ѕᴇᴛᴛɪɴɢѕ ᴍᴇɴᴜ.", "COMPARATOR", "SETTINGS"),
        SHOP("SHOP", "ѕʜᴏᴘ", "ѕʜᴏᴘ ᴄᴏᴍᴍᴀɴᴅ ᴀɴᴅ ᴘᴜʀᴄʜᴀѕᴇ ᴍᴇɴᴜѕ.", "CHEST", "SHOP"),
        ENDER_CHEST("ENDER_CHEST", "ᴇɴᴅᴇʀ ᴄʜᴇѕᴛ", "ᴄᴜѕᴛᴏᴍ ᴇɴᴅᴇʀ ᴄʜᴇѕᴛ ᴄᴏᴍᴍᴀɴᴅ ᴀɴᴅ ʟɪѕᴛᴇɴᴇʀ.", "ENDER_CHEST", "ENDERCHEST"),
        GAMEMODE("GAMEMODE", "ɢᴀᴍᴇᴍᴏᴅᴇ", "ѕᴛᴀꜰꜰ ɢᴀᴍᴇᴍᴏᴅᴇ ᴄᴏᴍᴍᴀɴᴅѕ.", "GRASS_BLOCK", "GAMEMODE"),
        SOCIAL("SOCIAL", "ѕᴏᴄɪᴀʟ", "ᴅɪѕᴄᴏʀᴅ, ᴛᴡɪᴛᴛᴇʀ/x, ѕᴛᴏʀᴇ, ᴀɴᴅ ᴍᴇᴅɪᴀ ᴄᴏᴍᴍᴀɴᴅѕ.", "BOOK", "SOCIAL"),
        SPAWN("SPAWN", "ѕᴘᴀᴡɴ", "ѕᴘᴀᴡɴ ᴄᴏᴍᴍᴀɴᴅ ᴀɴᴅ ѕᴘᴀᴡɴ ᴍᴇɴᴜ.", "BEACON", "SPAWN"),
        STATS("STATS", "ѕᴛᴀᴛѕ", "ѕᴛᴀᴛѕ, ᴘɪɴɢ, ᴀɴᴅ ᴘʟᴀʏᴛɪᴍᴇ ᴄᴏᴍᴍᴀɴᴅѕ.", "PLAYER_HEAD", "STATS"),
        TPA("TPA", "ᴛᴘᴀ", "ᴛᴇʟᴇᴘᴏʀᴛ ʀᴇǫᴜᴇѕᴛ ᴄᴏᴍᴍᴀɴᴅѕ ᴀɴᴅ ᴄᴏɴꜰɪʀᴍ ᴍᴇɴᴜ.", "ENDER_PEARL", "TPA"),
        TPA_AUTO("TPA_AUTO", "ᴛᴘᴀ ᴀᴜᴛᴏ", "ᴛᴘᴀ ᴀᴜᴛᴏ-ᴀᴄᴄᴇᴘᴛ ᴄᴏᴍᴍᴀɴᴅѕ.", "REDSTONE_TORCH", "TPAUTO"),
        FIND_PLAYER("FIND_PLAYER", "ꜰɪɴᴅ ᴘʟᴀʏᴇʀ", "ѕᴛᴀꜰꜰ ꜰɪɴᴅ ᴘʟᴀʏᴇʀ ᴄᴏᴍᴍᴀɴᴅ.", "SPYGLASS", "FINDPLAYER"),
        CRATES("CRATES", "ᴄʀᴀᴛᴇѕ", "ᴄʀᴀᴛᴇ ᴄᴏᴍᴍᴀɴᴅѕ, ᴍᴇɴᴜѕ, ᴋᴇʏ-ᴀʟʟ, ᴀɴᴅ ᴠɪѕᴜᴀʟ ᴇꜰꜰᴇᴄᴛѕ.", "TRIPWIRE_HOOK", "CRATE"),
        RULES("RULES", "ʀᴜʟᴇѕ", "ʀᴜʟᴇѕ ᴄᴏᴍᴍᴀɴᴅ ᴀɴᴅ ʀᴜʟᴇѕ ᴍᴇɴᴜ.", "BOOKSHELF", "RULES"),
        HELP("HELP", "ʜᴇʟᴘ", "ʜᴇʟᴘ ᴄᴏᴍᴍᴀɴᴅ ᴀɴᴅ ѕᴇʀᴠᴇʀ ɪɴꜰᴏ ᴍᴇɴᴜ.", "KNOWLEDGE_BOOK", "HELP"),
        NETWORK_SERVERS("NETWORK_SERVERS", "ɴᴇᴛᴡᴏʀᴋ ѕᴇʀᴠᴇʀѕ", "ɴᴇᴛᴡᴏʀᴋ ѕᴇʀᴠᴇʀ ѕᴛᴀᴛᴜѕ ᴄᴏᴍᴍᴀɴᴅ ᴀɴᴅ ᴍᴇɴᴜ.", "NETHER_STAR", "SERVERS"),
        SCOREBOARD("SCOREBOARD", "ѕᴄᴏʀᴇʙᴏᴀʀᴅ", "ѕɪᴅᴇʙᴀʀ ѕᴄᴏʀᴇʙᴏᴀʀᴅ ᴛᴀѕᴋ ᴀɴᴅ ᴅɪѕᴘʟᴀʏ.", "MAP", null),
        TABLIST("TABLIST", "ᴛᴀʙʟɪѕᴛ", "ᴛᴀʙʟɪѕᴛ ʜᴇᴀᴅᴇʀ, ꜰᴏᴏᴛᴇʀ, ᴀɴᴅ ᴘʟᴀʏᴇʀ ʟɪѕᴛ ɴᴀᴍᴇѕ.", "NAME_TAG", null),
        AUCTION_HOUSE("AUCTION_HOUSE", "ᴀᴜᴄᴛɪᴏɴ ʜᴏᴜѕᴇ", "ᴀᴜᴄᴛɪᴏɴ ʜᴏᴜѕᴇ ᴄᴏᴍᴍᴀɴᴅѕ, ʟɪѕᴛɪɴɢѕ, ᴄʟᴀɪᴍѕ, ᴀɴᴅ ᴇxᴘɪʀʏ ᴛᴀѕᴋ.", "GOLD_INGOT", null),
        ORDERS("ORDERS", "ᴏʀᴅᴇʀѕ", "ᴏʀᴅᴇʀѕ ʙᴏᴀʀᴅ ᴄᴏᴍᴍᴀɴᴅѕ, ᴍᴇɴᴜѕ, ᴀɴᴅ ᴇxᴘɪʀʏ ᴛᴀѕᴋ.", "WRITABLE_BOOK", null),
        DUELS("DUELS", "ᴅᴜᴇʟѕ", "ᴅᴜᴇʟ ᴄᴏᴍᴍᴀɴᴅѕ, ǫᴜᴇᴜᴇѕ, ᴀʀᴇɴᴀѕ, ᴍᴀᴛᴄʜᴇѕ, ᴀɴᴅ ᴛᴀѕᴋѕ.", "DIAMOND_SWORD", null),
        FFA("FFA", "ꜰꜰᴀ", "ꜰꜰᴀ ᴄᴏᴍᴍᴀɴᴅѕ, ᴀʀᴇɴᴀѕ, ᴍᴀᴛᴄʜᴇѕ, ᴀɴᴅ ᴛᴀѕᴋѕ.", "IRON_SWORD", null),
        STAFF_MODE("STAFF_MODE", "ѕᴛᴀꜰꜰ ᴍᴏᴅᴇ", "ѕᴛᴀꜰꜰ ᴍᴏᴅᴇ ᴄᴏᴍᴍᴀɴᴅ, ʜᴏᴛʙᴀʀ, ᴠᴀɴɪѕʜ, ᴀɴᴅ ѕᴛᴀꜰꜰ ᴛᴏᴏʟѕ.", "NETHERITE_CHESTPLATE", null),
        STAFF_CHAT("STAFF_CHAT", "ѕᴛᴀꜰꜰ ᴄʜᴀᴛ", "ѕᴛᴀꜰꜰ ᴄʜᴀᴛ ᴄᴏᴍᴍᴀɴᴅ ᴀɴᴅ ɴᴇᴛᴡᴏʀᴋ ѕᴛᴀꜰꜰ ᴄʜᴀᴛ.", "ECHO_SHARD", null),
        STAFF_ALERTS("STAFF_ALERTS", "ѕᴛᴀꜰꜰ ᴀʟᴇʀᴛѕ", "ʜᴇʟᴘᴏᴘ, ʀᴇᴘᴏʀᴛѕ, ᴀɴᴅ ɴᴇᴛᴡᴏʀᴋ ѕᴛᴀꜰꜰ ᴀʟᴇʀᴛѕ.", "BELL", null),
        SPAWN_STASH("SPAWN_STASH", "SpawnStash", "Staff bait stash spawning, alerts, and rollback cleanup.", "CHEST", "SPAWN-STASH"),
        FREEZE("FREEZE", "ꜰʀᴇᴇᴢᴇ", "ꜰʀᴇᴇᴢᴇ ᴄᴏᴍᴍᴀɴᴅ, ʟɪѕᴛᴇɴᴇʀѕ, ᴀɴᴅ ꜰʀᴇᴇᴢᴇ ѕᴛᴀᴛᴇ ᴇɴꜰᴏʀᴄᴇᴍᴇɴᴛ.", "PACKED_ICE", null),
        INVSEE("INVSEE", "ɪɴᴠѕᴇᴇ", "ɪɴᴠᴇɴᴛᴏʀʏ ɪɴѕᴘᴇᴄᴛɪᴏɴ ᴄᴏᴍᴍᴀɴᴅ ᴀɴᴅ ѕᴇѕѕɪᴏɴѕ.", "CHEST_MINECART", null),
        PROFILE_VIEWER("PROFILE_VIEWER", "ᴘʀᴏꜰɪʟᴇ ᴠɪᴇᴡᴇʀ", "ᴘʀᴏꜰɪʟᴇ ᴠɪᴇᴡᴇʀ ᴄᴏᴍᴍᴀɴᴅ ᴀɴᴅ ʜᴏᴍᴇѕ ʙʀᴏᴡѕᴇʀ.", "PLAYER_HEAD", null),
        PUNISHMENTS("PUNISHMENTS", "ᴘᴜɴɪѕʜᴍᴇɴᴛѕ", "ᴘᴜɴɪѕʜᴍᴇɴᴛ ᴄᴏᴍᴍᴀɴᴅѕ, ᴀʟɪᴀѕᴇѕ, ᴀɴᴅ ʜɪѕᴛᴏʀʏ ᴍᴇɴᴜѕ.", "IRON_AXE", null),
        SPAWNERS("SPAWNERS", "ѕᴘᴀᴡɴᴇʀѕ", "ᴍᴀɴᴀɢᴇᴅ ѕᴘᴀᴡɴᴇʀ ᴄᴏᴍᴍᴀɴᴅѕ, ʟɪѕᴛᴇɴᴇʀѕ, ᴠɪѕɪʙɪʟɪᴛʏ, ᴀɴᴅ ɢᴇɴᴇʀᴀᴛɪᴏɴ.", "SPAWNER", null),
        CLEAR_LAG("CLEAR_LAG", "ᴄʟᴇᴀʀʟᴀɢ", "ᴄʟᴇᴀʀʟᴀɢ ᴄᴏᴍᴍᴀɴᴅ ᴀɴᴅ ᴄʟᴇᴀɴᴜᴘ ᴛᴀѕᴋ.", "LAVA_BUCKET", null),
        PORTALS("PORTALS", "ᴘᴏʀᴛᴀʟѕ", "ᴘᴏʀᴛᴀʟ ᴛʀɪɢɢᴇʀѕ, ᴍᴀɴᴀɢᴇʀ ᴄᴏᴍᴍᴀɴᴅ, ᴀɴᴅ ᴘᴏʀᴛᴀʟ ʜᴏʟᴏɢʀᴀᴍѕ.", "END_PORTAL_FRAME", null),
        AMETHYST_TOOLS("AMETHYST_TOOLS", "ᴀᴍᴇᴛʜʏѕᴛ ᴛᴏᴏʟѕ", "ᴀᴍᴇᴛʜʏѕᴛ ᴛᴏᴏʟ ᴄᴏᴍᴍᴀɴᴅ, ʟɪѕᴛᴇɴᴇʀ, ᴀɴᴅ ᴇxᴘɪʀʏ ᴛᴀѕᴋ.", "AMETHYST_SHARD", null),
        COMBAT("COMBAT", "ᴄᴏᴍʙᴀᴛ", "ᴄᴏᴍʙᴀᴛ ᴛᴀɢɢɪɴɢ ʟɪѕᴛᴇɴᴇʀ ᴀɴᴅ ᴄᴏᴍᴍᴀɴᴅ ʙʟᴏᴄᴋɪɴɢ.", "SHIELD", null),
        FAST_CRYSTALS("FAST_CRYSTALS", "ꜰᴀѕᴛ ᴄʀʏѕᴛᴀʟѕ", "ꜰᴀѕᴛ ᴄʀʏѕᴛᴀʟ ᴘʟᴀᴄᴇᴍᴇɴᴛ/ʙʀᴇᴀᴋɪɴɢ ʙᴇʜᴀᴠɪᴏʀ.", "END_CRYSTAL", null),
        KEY_ALL("KEY_ALL", "ᴋᴇʏ-ᴀʟʟ", "ᴀᴜᴛᴏᴍᴀᴛɪᴄ ᴄʀᴀᴛᴇ ᴋᴇʏ-ᴀʟʟ ʀᴇᴡᴀʀᴅѕ.", "TRIPWIRE_HOOK", null),
        LUNAR_RICH_PRESENCE("LUNAR_RICH_PRESENCE", "ʟᴜɴᴀʀ ʀɪᴄʜ ᴘʀᴇѕᴇɴᴄᴇ", "ʟᴜɴᴀʀ ᴄʟɪᴇɴᴛ ʀɪᴄʜ ᴘʀᴇѕᴇɴᴄᴇ ɪɴᴛᴇɢʀᴀᴛɪᴏɴ.", "ENDER_EYE", null),
        LUNAR_TEAM_VIEW("LUNAR_TEAM_VIEW", "ʟᴜɴᴀʀ ᴛᴇᴀᴍ ᴠɪᴇᴡ", "ʟᴜɴᴀʀ ᴛᴇᴀᴍᴍᴀᴛᴇ ᴏᴠᴇʀʟᴀʏ ɪɴᴛᴇɢʀᴀᴛɪᴏɴ.", "LEATHER_HELMET", null),
        OPTIMIZATION("OPTIMIZATION", "ᴏᴘᴛɪᴍɪᴢᴀᴛɪᴏɴ", "ʀᴜɴᴛɪᴍᴇ ᴏᴘᴛɪᴍɪᴢᴀᴛɪᴏɴ ᴍᴏɴɪᴛᴏʀ ᴀɴᴅ ᴀᴅᴀᴘᴛɪᴠᴇ ᴛᴀѕᴋ ѕᴋɪᴘᴘɪɴɢ.", "REDSTONE", null),
        MAINTENANCE("MAINTENANCE", "ᴍᴀɪɴᴛᴇɴᴀɴᴄᴇ", "ѕᴇᴀᴍʟᴇѕѕ ᴍᴀɪɴᴛᴇɴᴀɴᴄᴇ ѕʏѕᴛᴇᴍ ᴡɪᴛʜ ʟᴏʙʙʏ ʀᴇᴅɪʀᴇᴄᴛɪᴏɴ.", "REDSTONE_LAMP", "MAINTENANCE"),
        HIDE("HIDE", "Hide", "Persistent player identity scrambling and configured disguises.", "NAME_TAG", "HIDE"),
        FRIENDS("FRIENDS", "ꜰʀɪᴇɴᴅѕ", "ᴘʟᴀʏᴇʀ ꜰʀɪᴇɴᴅѕ/ꜰᴏʟʟᴏᴡѕ ѕʏѕᴛᴇᴍ.", "PLAYER_HEAD", "FRIEND");

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
