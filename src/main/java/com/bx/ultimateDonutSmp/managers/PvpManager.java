package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.menus.PvpKitMenu;
import com.bx.ultimateDonutSmp.models.PvpKit;
import com.bx.ultimateDonutSmp.models.PvpRank;
import com.bx.ultimateDonutSmp.models.PvpSession;
import com.bx.ultimateDonutSmp.models.PvpStats;
import com.bx.ultimateDonutSmp.utils.AttributeUtils;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemSerializationUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.LocationUtils;
import com.bx.ultimateDonutSmp.utils.PermissionUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The ranked PvP arena: sessions, kits, Elo, ranks, levels, and the scheduled schematic reset.
 *
 * <p>It deliberately owns no combat logic of its own. Damage, tagging and logout punishment stay
 * with whatever combat plugin the server already runs, and this class only reacts to the deaths
 * that come out of it.</p>
 */
public class PvpManager {

    private static final Pattern DURATION_PATTERN = Pattern.compile("(\\d+)\\s*([dhms])", Pattern.CASE_INSENSITIVE);
    private static final String WAND_NAME = "&cPvP Arena Wand";
    private static final String WAND_KEY = "pvp_arena_wand";

    private final UltimateDonutSmp plugin;

    private final Map<UUID, PvpSession> sessions = new ConcurrentHashMap<>();
    private final Map<UUID, PvpStats> statsCache = new ConcurrentHashMap<>();
    private final Map<UUID, Map<UUID, KillRecord>> killHistory = new ConcurrentHashMap<>();
    private final Map<UUID, Location[]> wandSelections = new ConcurrentHashMap<>();
    private final Map<String, PvpKit> kits = new LinkedHashMap<>();
    private final List<PvpRank> ranks = new ArrayList<>();
    private final java.util.Set<UUID> lobbyOnRejoin = ConcurrentHashMap.newKeySet();
    private final java.util.Set<UUID> lobbyOnRespawn = ConcurrentHashMap.newKeySet();

    private long nextResetAt;
    private boolean resetWarningSent;

    public PvpManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
        ensureTables();
        loadRanks();
        loadKits();
        scheduleNextReset();
    }

    // ── Configuration ─────────────────────────────────────────────────────────

    private FileConfiguration config() {
        return plugin.getConfigManager().getPvp();
    }

    public boolean isEnabled() {
        return config().getBoolean("SETTINGS.ENABLED", false);
    }

    /** True once the arena has at least a spawn point, which is the minimum to let players in. */
    public boolean isConfigured() {
        return getSpawn() != null;
    }

    public void reload() {
        loadRanks();
        loadKits();
        scheduleNextReset();
    }

    public void shutdown() {
        for (UUID uuid : new ArrayList<>(sessions.keySet())) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                removeFromArena(player, true);
            }
        }
        sessions.clear();
        for (Map.Entry<UUID, PvpStats> entry : statsCache.entrySet()) {
            saveStats(entry.getKey(), entry.getValue());
        }
    }

    private void loadRanks() {
        ranks.clear();
        ConfigurationSection section = config().getConfigurationSection("RANKS");
        if (section == null) {
            return;
        }

        for (String id : section.getKeys(false)) {
            ranks.add(new PvpRank(
                    id,
                    section.getString(id + ".DISPLAY", id),
                    section.getInt(id + ".ELO", 0)
            ));
        }
        ranks.sort(Comparator.comparingInt(PvpRank::getEloRequirement));
    }

    // ── Ranks, levels and Elo ─────────────────────────────────────────────────

    public List<PvpRank> getRanks() {
        return List.copyOf(ranks);
    }

    public PvpRank getRankFor(int elo) {
        return PvpRank.resolve(ranks, elo);
    }

    public PvpRank getRank(UUID uuid) {
        return getRankFor(getStats(uuid).getElo());
    }

    public String getRankDisplay(UUID uuid) {
        PvpRank rank = getRank(uuid);
        return rank == null ? "" : rank.getDisplay();
    }

    /**
     * XP needed to move from {@code level} to the next one.
     *
     * <p>Level 1 costs the configured base, and every level after it costs one more increment,
     * so the curve stays a straight line an owner can reason about from two numbers.</p>
     */
    public static int xpForLevel(int level, int baseXp, int increasePerLevel) {
        int normalized = Math.max(1, level);
        return Math.max(1, baseXp + (normalized - 1) * increasePerLevel);
    }

    public int getXpForNextLevel(int level) {
        return xpForLevel(
                level,
                config().getInt("LEVELS.BASE_XP", 100),
                config().getInt("LEVELS.XP_INCREASE_PER_LEVEL", 50)
        );
    }

    // ── Stats ─────────────────────────────────────────────────────────────────

    public PvpStats getStats(UUID uuid) {
        if (uuid == null) {
            return PvpStats.starting(config().getInt("ELO.STARTING", 0));
        }
        return statsCache.computeIfAbsent(uuid, this::loadStats);
    }

    private void updateStats(UUID uuid, PvpStats stats) {
        if (uuid == null || stats == null) {
            return;
        }
        statsCache.put(uuid, stats);
        saveStats(uuid, stats);
    }

    /** The Elo ladder, best first. Reads straight from the database so offline players count. */
    public List<TopEntry> getTop(int limit) {
        return getTop(TopCategory.ELO, limit);
    }

    /**
     * One leaderboard, best first.
     *
     * <p>The column comes from {@link TopCategory} rather than from a caller-supplied string, so
     * the ordering can be chosen freely without ever building SQL out of user input.</p>
     */
    public List<TopEntry> getTop(TopCategory category, int limit) {
        List<TopEntry> top = new ArrayList<>();
        Connection connection = connection();
        if (connection == null || category == null) {
            return top;
        }

        try (PreparedStatement ps = connection.prepareStatement(
                "select player_uuid, " + category.column() + " as value from pvp_stats"
                        + " order by value desc limit ?")) {
            ps.setInt(1, Math.max(1, limit));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UUID uuid = parseUuid(rs.getString("player_uuid"));
                    if (uuid == null) {
                        continue;
                    }
                    String name = plugin.getDatabaseManager().getLastKnownUsername(uuid);
                    if (name == null || name.isBlank()) {
                        name = Bukkit.getOfflinePlayer(uuid).getName();
                    }
                    top.add(new TopEntry(uuid, name == null ? uuid.toString() : name, rs.getInt("value")));
                }
            }
        } catch (SQLException exception) {
            plugin.getLogger().log(Level.WARNING, "Failed to read the PvP leaderboard", exception);
        }
        return top;
    }

    // ── Arena geometry ────────────────────────────────────────────────────────

    public String getArenaName() {
        return config().getString("ARENA.NAME", "Arena");
    }

    public Location getSpawn() {
        return LocationUtils.parse(config().getString("ARENA.SPAWN", ""));
    }

    /** Where the second player in a ranked match starts. Falls back to the single arena spawn. */
    public Location getSpawn2() {
        Location second = LocationUtils.parse(config().getString("ARENA.SPAWN_2", ""));
        return second != null ? second : getSpawn();
    }

    public Location getLobby() {
        Location lobby = LocationUtils.parse(config().getString("ARENA.LOBBY", ""));
        if (lobby != null) {
            return lobby;
        }
        return plugin.getSpawnManager() == null ? null : plugin.getSpawnManager().getSpawnLocation();
    }

    public World getArenaWorld() {
        String configured = config().getString("ARENA.WORLD", "");
        if (configured != null && !configured.isBlank()) {
            World world = Bukkit.getWorld(configured);
            if (world != null) {
                return world;
            }
        }
        Location spawn = getSpawn();
        return spawn == null ? null : spawn.getWorld();
    }

    public void setSpawn(Location location) {
        writeConfig("ARENA.SPAWN", LocationUtils.serialize(location));
        if (location != null && location.getWorld() != null) {
            writeConfig("ARENA.WORLD", location.getWorld().getName());
        }
    }

    public void setSpawn2(Location location) {
        writeConfig("ARENA.SPAWN_2", LocationUtils.serialize(location));
    }

    public void setLobby(Location location) {
        writeConfig("ARENA.LOBBY", LocationUtils.serialize(location));
    }

    public void setArenaName(String name) {
        writeConfig("ARENA.NAME", name);
    }

    /** Stores one of the two boundary corners. {@code index} is 1 or 2. */
    public void setBoundaryCorner(int index, Location location) {
        writeConfig(index <= 1 ? "ARENA.BOUNDARY_POS1" : "ARENA.BOUNDARY_POS2", LocationUtils.serialize(location));
    }

    public boolean hasBoundary() {
        return LocationUtils.parse(config().getString("ARENA.BOUNDARY_POS1", "")) != null
                && LocationUtils.parse(config().getString("ARENA.BOUNDARY_POS2", "")) != null;
    }

    /**
     * Whether a location still counts as inside the arena.
     *
     * <p>With both wand corners set this is the cuboid between them plus the configured padding.
     * With no corners set the whole arena world counts, which is the sane reading for a server
     * that dedicates a world to the arena.</p>
     */
    public boolean isInsideArena(Location location) {
        if (location == null || location.getWorld() == null) {
            return false;
        }

        World arenaWorld = getArenaWorld();
        if (arenaWorld == null || !arenaWorld.getName().equals(location.getWorld().getName())) {
            return false;
        }

        Location first = LocationUtils.parse(config().getString("ARENA.BOUNDARY_POS1", ""));
        Location second = LocationUtils.parse(config().getString("ARENA.BOUNDARY_POS2", ""));
        if (first == null || second == null) {
            return true;
        }

        double padding = Math.max(0, config().getInt("ARENA.BOUNDARY_PADDING", 2));
        return location.getX() >= Math.min(first.getX(), second.getX()) - padding
                && location.getX() <= Math.max(first.getX(), second.getX()) + padding
                && location.getY() >= Math.min(first.getY(), second.getY()) - padding
                && location.getY() <= Math.max(first.getY(), second.getY()) + padding
                && location.getZ() >= Math.min(first.getZ(), second.getZ()) - padding
                && location.getZ() <= Math.max(first.getZ(), second.getZ()) + padding;
    }

    // ── Wand ──────────────────────────────────────────────────────────────────

    public ItemStack createWand() {
        ItemStack wand = ItemUtils.createItem(Material.GOLDEN_AXE, WAND_NAME, List.of(
                "&7Left click a block to set corner &f1",
                "&7Right click a block to set corner &f2",
                "&7Then run &f/pvp setboundary"
        ));
        ItemMeta meta = wand.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(plugin.getKey(WAND_KEY), PersistentDataType.BYTE, (byte) 1);
            wand.setItemMeta(meta);
        }
        return wand;
    }

    /** Identified by a data tag rather than its name, so renaming it on an anvil cannot break it. */
    public boolean isWand(ItemStack item) {
        if (item == null || item.getType() != Material.GOLDEN_AXE || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        return meta != null
                && meta.getPersistentDataContainer().has(plugin.getKey(WAND_KEY), PersistentDataType.BYTE);
    }

    public void setWandSelection(UUID uuid, int index, Location location) {
        Location[] selection = wandSelections.computeIfAbsent(uuid, key -> new Location[2]);
        selection[index <= 1 ? 0 : 1] = location;
    }

    public Location getWandSelection(UUID uuid, int index) {
        Location[] selection = wandSelections.get(uuid);
        return selection == null ? null : selection[index <= 1 ? 0 : 1];
    }

    // ── Kits ──────────────────────────────────────────────────────────────────

    public List<PvpKit> getKits() {
        return List.copyOf(kits.values());
    }

    public PvpKit getKit(String id) {
        return id == null ? null : kits.get(id.toLowerCase(Locale.ROOT));
    }

    public boolean canUseKit(Player player, PvpKit kit) {
        if (player == null || kit == null) {
            return false;
        }
        String permission = kit.getPermission();
        return permission == null || permission.isBlank() || PermissionUtils.has(player, permission);
    }

    /** The kits a player is actually allowed to pick, in menu order. */
    public List<PvpKit> getAvailableKits(Player player) {
        List<PvpKit> available = new ArrayList<>();
        for (PvpKit kit : kits.values()) {
            if (canUseKit(player, kit)) {
                available.add(kit);
            }
        }
        return available;
    }

    public PvpKit createKit(String id) {
        String normalized = id.toLowerCase(Locale.ROOT);
        PvpKit kit = new PvpKit(normalized);
        kits.put(normalized, kit);
        saveKit(kit);
        return kit;
    }

    public boolean deleteKit(String id) {
        if (id == null) {
            return false;
        }
        String normalized = id.toLowerCase(Locale.ROOT);
        if (kits.remove(normalized) == null) {
            return false;
        }
        writeConfig("KITS." + normalized, null);
        return true;
    }

    public void giveKit(Player player, PvpKit kit) {
        if (player == null || kit == null) {
            return;
        }

        PlayerInventory inventory = player.getInventory();
        inventory.clear();
        for (int slot = 0; slot < PvpKit.CONTENT_SIZE; slot++) {
            ItemStack item = kit.getContents()[slot];
            inventory.setItem(slot, item == null ? null : item.clone());
        }
        ItemStack[] armor = new ItemStack[4];
        for (int slot = 0; slot < 4; slot++) {
            ItemStack piece = kit.getArmor()[slot];
            armor[slot] = piece == null ? null : piece.clone();
        }
        inventory.setArmorContents(armor);
        inventory.setItemInOffHand(kit.getOffhand() == null ? null : kit.getOffhand().clone());

        for (PotionEffect effect : kit.getEffects()) {
            player.addPotionEffect(effect);
        }
        player.updateInventory();
    }

    private void loadKits() {
        kits.clear();
        ConfigurationSection section = config().getConfigurationSection("KITS");
        if (section == null) {
            return;
        }

        for (String id : section.getKeys(false)) {
            PvpKit kit = new PvpKit(id.toLowerCase(Locale.ROOT));
            kit.setDisplayName(section.getString(id + ".DISPLAY", "&f" + id));
            kit.setIcon(ItemUtils.parseMaterial(section.getString(id + ".ICON", "IRON_SWORD")));
            kit.setPermission(section.getString(id + ".PERMISSION", ""));
            kit.setMenuSlot(section.getInt(id + ".SLOT", -1));

            readItems(section.getConfigurationSection(id + ".CONTENTS"), kit.getContents());
            readItems(section.getConfigurationSection(id + ".ARMOR"), kit.getArmor());
            kit.setOffhand(readItem(section.getString(id + ".OFFHAND", "")));

            List<PotionEffect> effects = new ArrayList<>();
            for (String raw : section.getStringList(id + ".EFFECTS")) {
                PotionEffect effect = parseEffect(raw);
                if (effect != null) {
                    effects.add(effect);
                }
            }
            kit.setEffects(effects);
            kits.put(kit.getId(), kit);
        }
    }

    public void saveKit(PvpKit kit) {
        FileConfiguration pvp = plugin.getConfigManager().getOriginalPvp();
        if (pvp == null || kit == null) {
            return;
        }

        String root = "KITS." + kit.getId();
        pvp.set(root, null);
        pvp.set(root + ".DISPLAY", kit.getDisplayName());
        pvp.set(root + ".ICON", kit.getIcon().name());
        pvp.set(root + ".PERMISSION", kit.getPermission());
        pvp.set(root + ".SLOT", kit.getMenuSlot());
        writeItems(pvp, root + ".CONTENTS", kit.getContents());
        writeItems(pvp, root + ".ARMOR", kit.getArmor());
        pvp.set(root + ".OFFHAND", writeItem(kit.getOffhand()));

        List<String> effects = new ArrayList<>();
        for (PotionEffect effect : kit.getEffects()) {
            effects.add(effect.getType().getName() + ":" + effect.getAmplifier() + ":" + effect.getDuration());
        }
        pvp.set(root + ".EFFECTS", effects);
        plugin.getConfigManager().savePvp();
    }

    private void readItems(ConfigurationSection section, ItemStack[] target) {
        if (section == null) {
            return;
        }
        for (String key : section.getKeys(false)) {
            int slot = parseInt(key, -1);
            if (slot < 0 || slot >= target.length) {
                continue;
            }
            target[slot] = readItem(section.getString(key, ""));
        }
    }

    private ItemStack readItem(String encoded) {
        if (encoded == null || encoded.isBlank()) {
            return null;
        }
        try {
            return ItemSerializationUtils.deserialize(encoded);
        } catch (IOException | ClassNotFoundException exception) {
            plugin.getLogger().log(Level.WARNING, "Failed to read a stored PvP kit item", exception);
            return null;
        }
    }

    private void writeItems(FileConfiguration pvp, String root, ItemStack[] items) {
        for (int slot = 0; slot < items.length; slot++) {
            ItemStack item = items[slot];
            if (item == null || item.getType() == Material.AIR) {
                continue;
            }
            pvp.set(root + "." + slot, writeItem(item));
        }
    }

    private String writeItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return null;
        }
        try {
            return ItemSerializationUtils.serialize(item);
        } catch (IOException exception) {
            plugin.getLogger().log(Level.WARNING, "Failed to store a PvP kit item", exception);
            return null;
        }
    }

    private PotionEffect parseEffect(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String[] parts = raw.split(":");
        org.bukkit.potion.PotionEffectType type = org.bukkit.potion.PotionEffectType.getByName(parts[0].trim());
        if (type == null) {
            return null;
        }
        int amplifier = parts.length > 1 ? parseInt(parts[1], 0) : 0;
        int duration = parts.length > 2 ? parseInt(parts[2], 20 * 60) : 20 * 60;
        return new PotionEffect(type, duration, amplifier, false, false);
    }

    // ── Sessions ──────────────────────────────────────────────────────────────

    public boolean isInArena(UUID uuid) {
        return uuid != null && sessions.containsKey(uuid);
    }

    public PvpSession getSession(UUID uuid) {
        return uuid == null ? null : sessions.get(uuid);
    }

    public int getArenaPlayerCount() {
        return sessions.size();
    }

    /**
     * Puts a player into the arena and asks them for a kit.
     *
     * @return false when the arena is off, unconfigured, has no kits, or the player is already in
     */
    public boolean join(Player player) {
        if (player == null) {
            return false;
        }

        if (!isEnabled()) {
            send(player, message("DISABLED", "&cThe PvP arena is not enabled."));
            return false;
        }
        if (!isConfigured()) {
            send(player, message("NOT_SET_UP", "&cThe PvP arena has not been set up yet."));
            return false;
        }
        if (isInArena(player.getUniqueId())) {
            send(player, message("ALREADY_IN", "&cYou are already in the PvP arena."));
            return false;
        }
        if (getAvailableKits(player).isEmpty()) {
            send(player, message("NO_KITS", "&cThere are no PvP kits to choose from yet."));
            return false;
        }
        // Waiting for a ranked match and fighting in the open arena cannot both be true: the match
        // would drop on them mid-fight and take the session out from under them.
        if (plugin.getPvpMatchManager() != null && plugin.getPvpMatchManager().isQueued(player.getUniqueId())) {
            send(player, message("QUEUE_ALREADY_IN", "&cYou are already in the queue."));
            return false;
        }

        PvpSession session = new PvpSession(player.getUniqueId(), System.currentTimeMillis());
        sessions.put(player.getUniqueId(), session);
        updateStats(player.getUniqueId(), getStats(player.getUniqueId()).recordArenaJoin());

        Location spawn = getSpawn();
        plugin.getSpigotScheduler().teleport(player, spawn).thenRun(() ->
                plugin.getSpigotScheduler().runEntity(player, () -> openKitMenu(player)));
        send(player, message("JOINED", "&aYou joined the PvP arena."));
        return true;
    }

    public void leave(Player player) {
        if (player == null || !isInArena(player.getUniqueId())) {
            if (player != null) {
                send(player, message("NOT_IN", "&cYou are not in the PvP arena."));
            }
            return;
        }
        removeFromArena(player, false);
        send(player, message("LEFT", "&aYou left the PvP arena."));
    }

    /**
     * Takes a player out of the arena and sends them to the lobby.
     *
     * @param silent true during shutdown, where messages would never reach the client anyway
     */
    public void removeFromArena(Player player, boolean silent) {
        if (player == null) {
            return;
        }

        sessions.remove(player.getUniqueId());
        killHistory.remove(player.getUniqueId());
        takeKitBack(player);
        if (player.getGameMode() == GameMode.SPECTATOR) {
            player.setGameMode(GameMode.SURVIVAL);
        }

        Location lobby = getLobby();
        if (lobby != null && !silent) {
            // Losing a ranked match removes the player while they are still on the death screen,
            // and a teleport there does not survive the respawn. Hand it to the respawn instead.
            if (player.isDead()) {
                lobbyOnRespawn.add(player.getUniqueId());
            } else {
                plugin.getSpigotScheduler().teleport(player, lobby);
            }
        }
        if (plugin.getScoreboardManager() != null) {
            plugin.getScoreboardManager().invalidatePlayer(player);
        }
    }

    /**
     * Empties the kit out of a player on their way back to survival.
     *
     * <p>Switched off, nothing is touched on the way out. That is the right setting on a server
     * where Multiverse-Inventories - or anything else that separates inventories per world -
     * already owns this, since the arena clearing an inventory it does not own would undo that
     * plugin's work rather than help it.</p>
     */
    private void takeKitBack(Player player) {
        if (!config().getBoolean("SETTINGS.CLEAR_KIT_ON_LEAVE", true)) {
            return;
        }

        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[4]);
        player.getInventory().setItemInOffHand(null);
        for (PotionEffect effect : new ArrayList<>(player.getActivePotionEffects())) {
            player.removePotionEffect(effect.getType());
        }
    }

    /**
     * Opens a session for a player the ranked queue is about to drop into a match.
     *
     * <p>Unlike {@link #join(Player)} there is no kit menu: the match already decided the kit. It
     * still arrives later than the session does, because the match waits for the teleport into the
     * arena before handing it over, so the session opens awaiting a kit like any other.</p>
     */
    public void startSession(Player player) {
        if (player == null) {
            return;
        }

        PvpSession session = new PvpSession(player.getUniqueId(), System.currentTimeMillis());
        sessions.put(player.getUniqueId(), session);
        updateStats(player.getUniqueId(), getStats(player.getUniqueId()).recordArenaJoin());
        player.setGameMode(GameMode.SURVIVAL);
    }

    /**
     * Records that a ranked fighter has landed in the arena holding the match kit.
     *
     * <p>Kept apart from {@link #startSession(Player)} because the two no longer happen together.
     * Between them the player is still on their way in, and a session that says it is awaiting a
     * kit is what keeps them out of the damage and boundary checks until they arrive.</p>
     */
    public void markKitGiven(Player player, PvpKit kit) {
        PvpSession session = player == null ? null : getSession(player.getUniqueId());
        if (session == null || kit == null) {
            return;
        }

        session.setKitId(kit.getId());
        session.setAwaitingKit(false);
        session.setSpawnedAt(System.currentTimeMillis());
    }

    public void openKitMenu(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }
        new PvpKitMenu(plugin).open(player);
    }

    /**
     * Reopens the picker when it is closed with no kit taken.
     *
     * <p>A player in the arena with no kit cannot fight and cannot be fought, so closing the menu
     * to escape it would leave them a permanent spectator. The check runs a tick later because
     * taking a kit closes the menu too, and by then the session says a kit was chosen.</p>
     */
    public void handleKitMenuClosed(Player player) {
        if (player == null) {
            return;
        }
        plugin.getSpigotScheduler().runEntityLater(player, () -> {
            PvpSession session = getSession(player.getUniqueId());
            if (player.isOnline() && session != null && session.isAwaitingKit() && session.getRespawnAt() <= 0) {
                openKitMenu(player);
            }
        }, 1L);
    }

    /** Hands a player their chosen kit and drops them back onto the arena spawn. */
    public void selectKit(Player player, PvpKit kit) {
        PvpSession session = getSession(player.getUniqueId());
        if (session == null || kit == null) {
            return;
        }
        if (!canUseKit(player, kit)) {
            send(player, message("KIT_NO_PERMISSION", "&cYou do not have access to that kit."));
            return;
        }

        session.setKitId(kit.getId());
        session.setAwaitingKit(false);
        session.setRespawnAt(0L);
        session.setSpawnedAt(System.currentTimeMillis());

        player.setGameMode(GameMode.SURVIVAL);
        if (config().getBoolean("SETTINGS.HEAL_ON_SPAWN", true)) {
            heal(player);
        }
        giveKit(player, kit);

        Location spawn = getSpawn();
        if (spawn != null) {
            plugin.getSpigotScheduler().teleport(player, spawn);
        }
        send(player, message("KIT_GIVEN", "&aYou picked the &f{kit} &akit.")
                .replace("{kit}", ColorUtils.strip(ColorUtils.colorize(kit.getDisplayName()))));
    }

    public void healPlayer(Player player) {
        heal(player);
    }

    private void heal(Player player) {
        player.setHealth(Math.max(1.0D, AttributeUtils.getMaxHealth(player)));
        player.setFoodLevel(20);
        player.setSaturation(20F);
        player.setFireTicks(0);
        for (PotionEffect effect : new ArrayList<>(player.getActivePotionEffects())) {
            player.removePotionEffect(effect.getType());
        }
    }

    // ── Death, respawn, connection ────────────────────────────────────────────

    /** Called from the death event once the arena has been confirmed as the place it happened. */
    public void handleDeath(Player victim, Player killer) {
        PvpSession session = getSession(victim.getUniqueId());
        if (session == null) {
            return;
        }

        // A ranked match scores itself: the win and the Elo come from the match result, so the
        // open arena's per-kill rewards and respawn countdown must not also fire here.
        if (plugin.getPvpMatchManager() != null && plugin.getPvpMatchManager().isInMatch(victim.getUniqueId())) {
            plugin.getPvpMatchManager().handleDeath(victim);
            return;
        }

        session.setAwaitingKit(true);
        session.setKitId(null);
        session.setSpawnedAt(0L);
        session.setRespawnAt(System.currentTimeMillis()
                + Math.max(0, config().getInt("SETTINGS.RESPAWN_DELAY_SECONDS", 3)) * 1000L);

        updateStats(victim.getUniqueId(), getStats(victim.getUniqueId()).recordDeath());

        if (killer == null || killer.getUniqueId().equals(victim.getUniqueId())) {
            return;
        }
        if (!isInArena(killer.getUniqueId())) {
            return;
        }
        awardKill(killer, victim);
    }

    /**
     * Applies the Elo, XP, rank and level changes for one arena kill.
     *
     * <p>Kill-farm protection only reduces the reward, it never refuses to count the kill, so the
     * K/D and streak on the scoreboard still describe what actually happened in the arena.</p>
     */
    private void awardKill(Player killer, Player victim) {
        boolean rewarded = registerKill(killer.getUniqueId(), victim.getUniqueId());

        int eloGain = rewarded
                ? config().getInt("ELO.GAIN_PER_KILL", 25)
                : config().getInt("ANTI_KILL_FARMING.REDUCED_ELO", 0);
        int xpGain = rewarded
                ? config().getInt("LEVELS.XP_PER_KILL", 50)
                : config().getInt("ANTI_KILL_FARMING.REDUCED_XP", 0);
        int eloLoss = rewarded || config().getBoolean("ANTI_KILL_FARMING.APPLY_ELO_LOSS", false)
                ? config().getInt("ELO.LOSS_PER_DEATH", 20)
                : 0;

        PvpStats killerStats = getStats(killer.getUniqueId()).recordKill();
        PvpRank oldRank = getRankFor(killerStats.getElo());
        int oldLevel = killerStats.getLevel();

        killerStats = killerStats.withElo(clampElo(killerStats.getElo() + eloGain));
        killerStats = applyXp(killerStats, xpGain);
        updateStats(killer.getUniqueId(), killerStats);

        PvpStats victimBefore = getStats(victim.getUniqueId());
        PvpRank victimOldRank = getRankFor(victimBefore.getElo());
        PvpStats victimStats = victimBefore.withElo(clampElo(victimBefore.getElo() - eloLoss));
        updateStats(victim.getUniqueId(), victimStats);

        if (rewarded) {
            send(killer, message("KILL_REWARD", "&8[&cPVP&8] &7You killed &f{victim} &8(&c+{elo} elo&8, &c+{xp} xp&8)")
                    .replace("{victim}", victim.getName())
                    .replace("{elo}", String.valueOf(eloGain))
                    .replace("{xp}", String.valueOf(xpGain)));
        } else {
            send(killer, message("KILL_FARM_LIMIT", "&7No reward - you have already killed &f{victim} &7too recently.")
                    .replace("{victim}", victim.getName()));
        }
        send(victim, message("DEATH_PENALTY", "&8[&cPVP&8] &f{killer} &7killed you &8(&c-{elo} elo&8)")
                .replace("{killer}", killer.getName())
                .replace("{elo}", String.valueOf(eloLoss)));

        announceLevel(killer, oldLevel, killerStats);
        announceRank(killer, oldRank, getRankFor(killerStats.getElo()), killerStats);
        announceRank(victim, victimOldRank, getRankFor(victimStats.getElo()), victimStats);
    }

    private int clampElo(int elo) {
        int minimum = config().getInt("ELO.MINIMUM", 0);
        int maximum = config().getInt("ELO.MAXIMUM", 0);
        int clamped = Math.max(minimum, elo);
        return maximum > 0 ? Math.min(maximum, clamped) : clamped;
    }

    /** Adds XP and rolls the player up as many levels as it pays for. */
    private PvpStats applyXp(PvpStats stats, int xpGain) {
        if (xpGain <= 0) {
            return stats;
        }

        int maxLevel = config().getInt("LEVELS.MAX_LEVEL", 100);
        int level = stats.getLevel();
        int xp = stats.getXp() + xpGain;

        while (maxLevel <= 0 || level < maxLevel) {
            int needed = getXpForNextLevel(level);
            if (xp < needed) {
                break;
            }
            xp -= needed;
            level++;
        }

        if (maxLevel > 0 && level >= maxLevel) {
            level = maxLevel;
            xp = 0;
        }
        return stats.withProgress(level, xp);
    }

    public void handleQuit(Player player) {
        if (player == null || !isInArena(player.getUniqueId())) {
            return;
        }

        sessions.remove(player.getUniqueId());
        killHistory.remove(player.getUniqueId());
        if (config().getBoolean("SETTINGS.LOBBY_ON_REJOIN", true)) {
            lobbyOnRejoin.add(player.getUniqueId());
        }
        takeKitBack(player);

        // Spectator is only ever a respawn countdown here, and it outlives the disconnect. Undo it
        // now rather than on the next login: the login path is skipped entirely when the lobby
        // return is switched off, which would leave the player a spectator for good.
        if (player.getGameMode() == GameMode.SPECTATOR) {
            player.setGameMode(GameMode.SURVIVAL);
        }
    }

    /**
     * Whether this player still owes the arena a trip to the lobby when they respawn.
     *
     * <p>Reading it clears it, so the respawn that acts on it is the only one that can.</p>
     */
    public boolean consumeLobbyOnRespawn(UUID uuid) {
        return uuid != null && lobbyOnRespawn.remove(uuid);
    }

    /** Sends a player who logged out inside the arena back to the lobby on their next join. */
    public void handleJoin(Player player) {
        if (player == null || !lobbyOnRejoin.remove(player.getUniqueId())) {
            return;
        }

        Location lobby = getLobby();
        if (lobby != null) {
            plugin.getSpigotScheduler().runEntityLater(player, () -> {
                if (player.isOnline()) {
                    plugin.getSpigotScheduler().teleport(player, lobby);
                    if (player.getGameMode() == GameMode.SPECTATOR) {
                        player.setGameMode(GameMode.SURVIVAL);
                    }
                }
            }, 10L);
        }
    }

    /**
     * Moves a player's Elo by a ranked match result and announces any rank change.
     *
     * @return the change that actually landed, which the floor or ceiling may have cut short
     */
    public int applyMatchElo(UUID uuid, int delta) {
        if (uuid == null) {
            return 0;
        }

        PvpStats before = getStats(uuid);
        PvpRank oldRank = getRankFor(before.getElo());
        PvpStats after = before.withElo(clampElo(before.getElo() + delta));
        updateStats(uuid, after);

        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            announceRank(player, oldRank, getRankFor(after.getElo()), after);
        }
        return after.getElo() - before.getElo();
    }

    /** Books a ranked match win and loss against the same kill and death counters the arena uses. */
    public void recordMatchOutcome(UUID winner, UUID loser) {
        if (winner != null) {
            updateStats(winner, getStats(winner).recordKill());
        }
        if (loser != null) {
            updateStats(loser, getStats(loser).recordDeath());
        }
    }

    // ── Anti kill-farming ─────────────────────────────────────────────────────

    /**
     * Records a kill and says whether it earns full rewards.
     *
     * <p>The counter for a victim expires after the configured cooldown, so a pair who keep
     * meeting over a long session never permanently lock each other out of rewards.</p>
     */
    public boolean registerKill(UUID killer, UUID victim) {
        if (!config().getBoolean("ANTI_KILL_FARMING.ENABLED", true)) {
            return true;
        }

        long now = System.currentTimeMillis();
        long cooldown = parseDuration(config().getString("ANTI_KILL_FARMING.COOLDOWN", "5m"));
        int limit = Math.max(0, config().getInt("ANTI_KILL_FARMING.MAX_REWARDED_KILLS", 3));

        Map<UUID, KillRecord> victims = killHistory.computeIfAbsent(killer, key -> new ConcurrentHashMap<>());
        KillRecord record = victims.get(victim);
        if (record == null || (cooldown > 0 && now - record.lastKillAt() > cooldown)) {
            victims.put(victim, new KillRecord(1, now));
            return limit > 0;
        }

        int count = record.count() + 1;
        victims.put(victim, new KillRecord(count, now));
        return count <= limit;
    }

    // ── Broadcasts ────────────────────────────────────────────────────────────

    private void announceLevel(Player player, int oldLevel, PvpStats stats) {
        if (stats.getLevel() <= oldLevel) {
            return;
        }

        send(player, message("LEVEL_UP", "&aYou reached PvP Level &f{level}&a!")
                .replace("{level}", String.valueOf(stats.getLevel())));

        String template = config().getString("BROADCASTS.LEVEL_UP.MESSAGE", "");
        if (template == null || template.isBlank()) {
            return;
        }
        broadcast(player, stats, oldLevel, null,
                config().getBoolean("BROADCASTS.LEVEL_UP.GLOBAL", true), template);
    }

    private void announceRank(Player player, PvpRank oldRank, PvpRank newRank, PvpStats stats) {
        if (oldRank == null || newRank == null || oldRank.getId().equals(newRank.getId())) {
            return;
        }

        boolean up = newRank.getEloRequirement() > oldRank.getEloRequirement();
        send(player, message(up ? "RANK_UP" : "RANK_DOWN",
                up ? "&aYou ranked up to {rank}&a!" : "&cYou dropped to {rank}&c.")
                .replace("{rank}", newRank.getDisplay()));

        String root = up ? "BROADCASTS.RANK_UP" : "BROADCASTS.RANK_DOWN";
        String template = config().getString(root + ".MESSAGE", "");
        if (template == null || template.isBlank()) {
            return;
        }
        broadcast(player, stats, stats.getLevel(), oldRank,
                config().getBoolean(root + ".GLOBAL", up), template);
    }

    /**
     * Sends a progression announcement.
     *
     * <p>The placeholders are filled in here rather than left to PlaceholderAPI, so the message
     * still reads correctly on a server that does not run PlaceholderAPI at all, and so a global
     * broadcast describes the player it is about instead of whoever is reading it.</p>
     */
    private void broadcast(Player player, PvpStats stats, int oldLevel, PvpRank oldRank, boolean global, String template) {
        PvpRank rank = getRankFor(stats.getElo());
        String text = template
                .replace("%player_name%", player.getName())
                .replace("%pvp_level%", String.valueOf(stats.getLevel()))
                .replace("%pvp_old_level%", String.valueOf(oldLevel))
                .replace("%pvp_rank%", rank == null ? "" : rank.getDisplay())
                .replace("%pvp_old_rank%", oldRank == null ? "" : oldRank.getDisplay())
                .replace("%pvp_elo%", String.valueOf(stats.getElo()))
                .replace("%pvp_xp%", String.valueOf(stats.getXp()));

        if (global) {
            broadcastToServer(text);
        } else {
            send(player, text);
        }
    }

    // ── Blocked commands ──────────────────────────────────────────────────────

    public boolean shouldBlockCommands() {
        return config().getBoolean("BLOCKED_COMMANDS.ENABLED", true);
    }

    public boolean shouldBlockEnderChestBlock() {
        return config().getBoolean("BLOCKED_COMMANDS.BLOCK_ENDER_CHEST_BLOCK", true);
    }

    /** Matches a typed command against the blocked list, ignoring the slash and any arguments. */
    public boolean isBlockedCommand(String raw) {
        if (raw == null || raw.isBlank()) {
            return false;
        }

        String typed = raw.trim().toLowerCase(Locale.ROOT);
        if (typed.startsWith("/")) {
            typed = typed.substring(1);
        }
        int space = typed.indexOf(' ');
        String label = space < 0 ? typed : typed.substring(0, space);

        for (String blocked : config().getStringList("BLOCKED_COMMANDS.COMMANDS")) {
            if (blocked == null || blocked.isBlank()) {
                continue;
            }
            String normalized = blocked.trim().toLowerCase(Locale.ROOT);
            if (normalized.startsWith("/")) {
                normalized = normalized.substring(1);
            }
            if (label.equals(normalized) || label.endsWith(":" + normalized)) {
                return true;
            }
        }
        return false;
    }

    // ── Scoreboard ────────────────────────────────────────────────────────────

    public boolean hasArenaScoreboard(Player player) {
        return player != null
                && isEnabled()
                && isInArena(player.getUniqueId())
                && config().getBoolean("SCOREBOARD.ENABLED", true);
    }

    public List<String> getScoreboardTitles() {
        return config().getStringList("SCOREBOARD.TITLE");
    }

    public List<String> getScoreboardLines() {
        return config().getStringList("SCOREBOARD.LINES");
    }

    // ── Arena reset ───────────────────────────────────────────────────────────

    public boolean isResetEnabled() {
        return config().getBoolean("RESET.ENABLED", false)
                && parseDuration(config().getString("RESET.INTERVAL", "24h")) > 0;
    }

    /** Milliseconds until the next scheduled reset, or -1 when resets are switched off. */
    public long getMillisUntilReset() {
        if (!isResetEnabled() || nextResetAt <= 0) {
            return -1L;
        }
        return Math.max(0L, nextResetAt - System.currentTimeMillis());
    }

    public String getFormattedReset() {
        long remaining = getMillisUntilReset();
        if (remaining < 0) {
            return config().getString("SCOREBOARD.RESET_DISABLED_TEXT", "&7-");
        }
        return formatDuration(remaining, config().getString("SCOREBOARD.RESET_FORMAT", "{d}D:{h}H:{m}M:{s}S"));
    }

    public void setResetSchematic(String schematic) {
        writeConfig("RESET.SCHEMATIC", schematic == null ? "" : schematic.trim());
    }

    public void setPasteLocation(Location location) {
        writeConfig("RESET.PASTE_LOCATION", LocationUtils.serialize(location));
    }

    private void scheduleNextReset() {
        long interval = parseDuration(config().getString("RESET.INTERVAL", "24h"));
        nextResetAt = isResetEnabled() ? System.currentTimeMillis() + interval : 0L;
        resetWarningSent = false;
    }

    /**
     * Runs the configured reset commands from console.
     *
     * <p>Going through the commands instead of a compiled WorldEdit dependency is deliberate: the
     * same two lines drive WorldEdit and FastAsyncWorldEdit, so a server can swap between them
     * without the plugin caring, and one that runs neither simply has the feature switched off.</p>
     */
    public void resetArena(CommandSender initiator) {
        String schematic = config().getString("RESET.SCHEMATIC", "");
        List<String> commands = config().getStringList("RESET.COMMANDS");
        if (schematic == null || schematic.isBlank() || commands.isEmpty()) {
            if (initiator != null) {
                send(initiator, message("RESET_NO_SCHEMATIC", "&cNo schematic is configured for the arena reset."));
            }
            return;
        }

        if (config().getBoolean("RESET.EVACUATE", true)) {
            for (UUID uuid : new ArrayList<>(sessions.keySet())) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    removeFromArena(player, false);
                }
            }
        }

        Location paste = LocationUtils.parse(config().getString("RESET.PASTE_LOCATION", ""));
        long delay = Math.max(0, config().getInt("RESET.PASTE_DELAY_SECONDS", 5)) * 20L;

        // The first command loads the schematic into the console's WorldEdit session and the rest
        // paste it. They cannot run back to back: a large schematic is still loading when the paste
        // would fire, which is exactly the manual wait the reporter described.
        dispatch(commands.get(0), schematic, paste);
        for (int index = 1; index < commands.size(); index++) {
            String command = commands.get(index);
            plugin.getSpigotScheduler().runGlobalLater(() -> dispatch(command, schematic, paste), delay * index);
        }

        plugin.getSpigotScheduler().runGlobalLater(() -> {
            String done = message("RESET_DONE", "&8[&cPVP&8] &7The arena has been reset.");
            if (!done.isBlank()) {
                broadcastToServer(done);
            }
        }, delay * Math.max(1, commands.size() - 1) + 20L);

        scheduleNextReset();
    }

    private void dispatch(String command, String schematic, Location paste) {
        String resolved = command.replace("{schematic}", schematic);
        if (paste != null && paste.getWorld() != null) {
            resolved = resolved
                    .replace("{world}", paste.getWorld().getName())
                    .replace("{x}", String.valueOf(paste.getBlockX()))
                    .replace("{y}", String.valueOf(paste.getBlockY()))
                    .replace("{z}", String.valueOf(paste.getBlockZ()));
        }
        plugin.getSpigotScheduler().dispatchConsoleCommand(resolved);
    }

    // ── Tick ──────────────────────────────────────────────────────────────────

    /** Runs once a second: respawn countdowns, boundary checks, and the reset schedule. */
    public void tick() {
        long now = System.currentTimeMillis();

        for (UUID uuid : new ArrayList<>(sessions.keySet())) {
            Player player = Bukkit.getPlayer(uuid);
            PvpSession session = sessions.get(uuid);
            if (player == null || session == null) {
                sessions.remove(uuid);
                continue;
            }

            if (session.getRespawnAt() > 0 && now >= session.getRespawnAt()) {
                session.setRespawnAt(0L);
                plugin.getSpigotScheduler().runEntity(player, () -> openKitMenu(player));
                continue;
            }

            if (session.getRespawnAt() > 0) {
                long seconds = Math.max(1L, (session.getRespawnAt() - now + 999L) / 1000L);
                send(player, message("RESPAWN_COUNTDOWN", "&fRespawning in &c{seconds}&f...")
                        .replace("{seconds}", String.valueOf(seconds)));
                continue;
            }

            if (!session.isAwaitingKit()
                    && config().getBoolean("SETTINGS.KILL_OUTSIDE_BOUNDARY", true)
                    && !isInsideArena(player.getLocation())) {
                send(player, message("LEFT_BOUNDARY", "&cYou left the arena and were removed from the fight."));
                if (plugin.getPvpMatchManager() != null && plugin.getPvpMatchManager().isInMatch(uuid)) {
                    plugin.getPvpMatchManager().handleBoundaryExit(player);
                } else {
                    removeFromArena(player, false);
                }
            }
        }

        tickReset(now);
    }

    private void tickReset(long now) {
        if (!isResetEnabled()) {
            return;
        }
        if (nextResetAt <= 0) {
            scheduleNextReset();
            return;
        }

        long warning = Math.max(0, config().getInt("RESET.WARNING_SECONDS", 30)) * 1000L;
        if (!resetWarningSent && warning > 0 && nextResetAt - now <= warning) {
            resetWarningSent = true;
            String text = message("RESET_WARNING", "&8[&cPVP&8] &7The arena resets in &c{time}&7.")
                    .replace("{time}", formatDuration(Math.max(0L, nextResetAt - now), "{D}m {S}s"));
            if (!text.isBlank()) {
                broadcastToServer(text);
            }
        }

        if (now >= nextResetAt) {
            resetArena(null);
        }
    }

    // ── Duration helpers ──────────────────────────────────────────────────────

    /**
     * Reads a duration written the way the config documents it: {@code 1d10h15m}, {@code 24h},
     * {@code 30m}, {@code 90s}, or a bare number of seconds.
     *
     * @return the duration in milliseconds, or 0 when nothing could be read
     */
    public static long parseDuration(String input) {
        if (input == null || input.isBlank()) {
            return 0L;
        }

        String trimmed = input.trim();
        Matcher matcher = DURATION_PATTERN.matcher(trimmed);
        long total = 0L;
        boolean matched = false;
        while (matcher.find()) {
            matched = true;
            long amount = Long.parseLong(matcher.group(1));
            total += switch (matcher.group(2).toLowerCase(Locale.ROOT)) {
                case "d" -> amount * 86_400_000L;
                case "h" -> amount * 3_600_000L;
                case "m" -> amount * 60_000L;
                default -> amount * 1_000L;
            };
        }

        if (matched) {
            return total;
        }
        try {
            return Long.parseLong(trimmed) * 1_000L;
        } catch (NumberFormatException exception) {
            return 0L;
        }
    }

    /**
     * Renders a duration into a configured template.
     *
     * <p>{@code {d} {h} {m} {s}} are zero padded to two digits and {@code {D} {H} {M} {S}} are
     * not, so an owner can write either {@code 01D:10H} or {@code 1d 10h} without the code
     * choosing for them.</p>
     */
    public static String formatDuration(long millis, String format) {
        String template = format == null || format.isBlank() ? "{d}D:{h}H:{m}M:{s}S" : format;
        long totalSeconds = Math.max(0L, millis) / 1000L;
        long days = totalSeconds / 86_400L;
        long hours = (totalSeconds % 86_400L) / 3_600L;
        long minutes = (totalSeconds % 3_600L) / 60L;
        long seconds = totalSeconds % 60L;

        return template
                .replace("{d}", pad(days))
                .replace("{h}", pad(hours))
                .replace("{m}", pad(minutes))
                .replace("{s}", pad(seconds))
                .replace("{D}", String.valueOf(days))
                .replace("{H}", String.valueOf(hours))
                .replace("{M}", String.valueOf(minutes))
                .replace("{S}", String.valueOf(seconds));
    }

    private static String pad(long value) {
        return value < 10 ? "0" + value : String.valueOf(value);
    }

    // ── Messages and small helpers ────────────────────────────────────────────

    public String message(String key, String fallback) {
        String configured = config().getString("MESSAGES." + key, fallback);
        return configured == null ? "" : configured;
    }

    /** Sends one already-resolved line to everyone online. */
    private void broadcastToServer(String text) {
        if (text == null || text.isBlank()) {
            return;
        }
        String rendered = ColorUtils.toComponent(text);
        plugin.getSpigotScheduler().forEachOnlinePlayer(online -> online.sendMessage(rendered));
    }

    private void send(CommandSender target, String text) {
        if (target != null && text != null && !text.isBlank()) {
            target.sendMessage(ColorUtils.toComponent(text));
        }
    }

    private void writeConfig(String path, Object value) {
        FileConfiguration pvp = plugin.getConfigManager().getOriginalPvp();
        if (pvp == null) {
            return;
        }
        pvp.set(path, value);
        plugin.getConfigManager().savePvp();
    }

    private static int parseInt(String input, int fallback) {
        try {
            return Integer.parseInt(input.trim());
        } catch (NumberFormatException | NullPointerException exception) {
            return fallback;
        }
    }

    private static UUID parseUuid(String input) {
        try {
            return UUID.fromString(input);
        } catch (IllegalArgumentException | NullPointerException exception) {
            return null;
        }
    }

    // ── Persistence ───────────────────────────────────────────────────────────

    private Connection connection() {
        return plugin.getDatabaseManager() == null ? null : plugin.getDatabaseManager().getConnection();
    }

    private void ensureTables() {
        Connection connection = connection();
        if (connection == null) {
            return;
        }

        try (Statement st = connection.createStatement()) {
            plugin.getDatabaseManager().executeSchema(st, """
                    CREATE TABLE IF NOT EXISTS pvp_stats (
                      player_uuid TEXT PRIMARY KEY,
                      elo INTEGER DEFAULT 0,
                      pvp_level INTEGER DEFAULT 1,
                      pvp_xp INTEGER DEFAULT 0,
                      kills INTEGER DEFAULT 0,
                      deaths INTEGER DEFAULT 0,
                      streak INTEGER DEFAULT 0,
                      best_streak INTEGER DEFAULT 0,
                      arena_joins INTEGER DEFAULT 0,
                      updated_at INTEGER DEFAULT 0
                    )
                    """);
        } catch (SQLException exception) {
            plugin.getLogger().log(Level.WARNING, "Failed to create the PvP arena tables", exception);
        }
    }

    private PvpStats loadStats(UUID uuid) {
        PvpStats empty = PvpStats.starting(config().getInt("ELO.STARTING", 0));
        Connection connection = connection();
        if (uuid == null || connection == null) {
            return empty;
        }

        try (PreparedStatement ps = connection.prepareStatement(
                "select elo, pvp_level, pvp_xp, kills, deaths, streak, best_streak, arena_joins"
                        + " from pvp_stats where player_uuid = ?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new PvpStats(
                            rs.getInt("elo"),
                            rs.getInt("pvp_level"),
                            rs.getInt("pvp_xp"),
                            rs.getInt("kills"),
                            rs.getInt("deaths"),
                            rs.getInt("streak"),
                            rs.getInt("best_streak"),
                            rs.getInt("arena_joins")
                    );
                }
            }
        } catch (SQLException exception) {
            plugin.getLogger().log(Level.WARNING, "Failed to load PvP stats for " + uuid, exception);
        }
        return empty;
    }

    private void saveStats(UUID uuid, PvpStats stats) {
        Connection connection = connection();
        if (uuid == null || stats == null || connection == null) {
            return;
        }

        try (PreparedStatement ps = connection.prepareStatement(
                "replace into pvp_stats (player_uuid, elo, pvp_level, pvp_xp, kills, deaths, streak,"
                        + " best_streak, arena_joins, updated_at) values (?,?,?,?,?,?,?,?,?,?)")) {
            ps.setString(1, uuid.toString());
            ps.setInt(2, stats.getElo());
            ps.setInt(3, stats.getLevel());
            ps.setInt(4, stats.getXp());
            ps.setInt(5, stats.getKills());
            ps.setInt(6, stats.getDeaths());
            ps.setInt(7, stats.getStreak());
            ps.setInt(8, stats.getBestStreak());
            ps.setInt(9, stats.getArenaJoins());
            ps.setLong(10, System.currentTimeMillis());
            ps.executeUpdate();
        } catch (SQLException exception) {
            plugin.getLogger().log(Level.WARNING, "Failed to save PvP stats for " + uuid, exception);
        }
    }

    /** How often a killer has been rewarded for one particular victim, and when that last happened. */
    private record KillRecord(int count, long lastKillAt) {
    }

    /** One row of a leaderboard: who, and their score in whichever category was asked for. */
    public record TopEntry(UUID uuid, String name, int value) {
    }

    /** The leaderboards the menu offers, each naming the column it sorts on. */
    public enum TopCategory {
        ELO("elo", "Elo", "DIAMOND"),
        LEVEL("pvp_level", "Level", "EXPERIENCE_BOTTLE"),
        KILLS("kills", "Kills", "DIAMOND_SWORD"),
        DEATHS("deaths", "Deaths", "SKELETON_SKULL"),
        STREAK("best_streak", "Best streak", "BLAZE_POWDER"),
        JOINS("arena_joins", "Arena joins", "IRON_DOOR");

        private final String column;
        private final String displayName;
        private final String icon;

        TopCategory(String column, String displayName, String icon) {
            this.column = column;
            this.displayName = displayName;
            this.icon = icon;
        }

        public String column() {
            return column;
        }

        public String displayName() {
            return displayName;
        }

        public String icon() {
            return icon;
        }
    }
}
