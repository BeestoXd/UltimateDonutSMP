package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.PacketSidebarRenderer;
import com.bx.ultimateDonutSmp.utils.ScoreboardNumberHider;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScoreboardManager {

    private static final int MAX_LINES = 15;
    private static final Pattern SIDEBAR_ICON_PATTERN = Pattern.compile("\\{sb_icon:([^}]*)\\}");
    private static final char SECTION_CHAR = '\u00A7';

    // The sidebar statistics a player can switch off one at a time in /settings. A line belongs to a
    // statistic when it carries one of that statistic's placeholders. %economy_shard_cuboid_display%
    // is deliberately absent from the shards list: the cuboid line has its own {shard_cuboid} switch.
    private static final StatLine[] STAT_LINES = {
            new StatLine(PlayerData::isShowMoneyLine, new String[]{
                    "%economy_money%", "%economy_nicestMoney%", "%economy_nicestmoney%",
                    "%economy_money_short%", "%economy_money_amount_short%", "%economy_money_formatted%",
                    "%economy_money_short_formatted%", "%economy_nicestMoney_formatted%",
                    "%economy_nicestmoney_formatted%"}),
            new StatLine(PlayerData::isShowShardsLine, new String[]{
                    "%economy_shards%", "%economy_nicestShards%", "%economy_nicestshards%",
                    "%economy_shards_short%", "%economy_shards_amount_short%"}),
            new StatLine(PlayerData::isShowKillsLine, new String[]{"%economy_kills%"}),
            new StatLine(PlayerData::isShowDeathsLine, new String[]{"%economy_deaths%"}),
            new StatLine(PlayerData::isShowPlaytimeLine, new String[]{"%economy_playtime%"}),
    };

    // Unique invisible entries, one per line slot, so updates stay flicker free.
    private static final String[] ENTRIES = new String[MAX_LINES];
    static {
        String[] codes = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e"};
        for (int i = 0; i < MAX_LINES; i++) {
            ENTRIES[i] = "\u00A7" + codes[i] + "\u00A7r";
        }
    }

    private final UltimateDonutSmp plugin;
    private final boolean folia;

    // Folia implementation fields
    private final PacketSidebarRenderer sidebarRenderer;
    private final Set<UUID> visiblePlayers;

    // Spigot/Paper implementation fields
    private final ScoreboardNumberHider numberHider;
    private final Map<UUID, Scoreboard> playerBoards;

    // Line and title caching to avoid redundant Bukkit/Packet updates
    private final Map<UUID, String[]> playerLastLines = new ConcurrentHashMap<>();
    private final Map<UUID, String> playerLastTitle = new ConcurrentHashMap<>();
    private final Map<UUID, String> playerLastRawTitle = new ConcurrentHashMap<>();
    private final Map<UUID, String[]> foliaLastLines = new ConcurrentHashMap<>();
    private final Map<UUID, String> foliaLastTitle = new ConcurrentHashMap<>();

    // Bukkit handles for the board a player already owns. Looking these up per frame allocates a
    // fresh CraftObjective every time and costs three name lookups per line.
    private final Map<UUID, Objective> playerObjectives = new ConcurrentHashMap<>();
    private final Map<UUID, Team[]> playerTeams = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> playerLineCounts = new ConcurrentHashMap<>();

    // Formatted balances, and the two sidebar currency icons, held until the value behind them moves.
    private final Map<UUID, FormattedBalances> balanceCache = new ConcurrentHashMap<>();
    private Object iconMoneyToken;
    private Object iconShardsToken;
    private int iconWidth = -1;
    private String cachedMoneyIcon;
    private String cachedShardsIcon;

    private int titleIndex = 0;

    public ScoreboardManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
        this.folia = plugin.getSpigotScheduler().isFolia();
        if (folia) {
            this.sidebarRenderer = new PacketSidebarRenderer(plugin);
            this.visiblePlayers = ConcurrentHashMap.newKeySet();
            this.numberHider = null;
            this.playerBoards = null;
        } else {
            this.sidebarRenderer = null;
            this.visiblePlayers = null;
            this.numberHider = new ScoreboardNumberHider(plugin);
            this.playerBoards = new HashMap<>();
        }
    }

    public long getUpdateIntervalTicks() {
        FileConfiguration scoreboard = plugin.getConfigManager().getScoreboard();
        long legacy = scoreboard.getLong("SCOREBOARD.UPDATE-INTERVAL-TICKS", 0L);
        long ticks = scoreboard.getLong("SCOREBOARD.TITLE-UPDATE-TICKS", legacy > 0L ? legacy : 2L);
        return Math.max(1L, ticks);
    }

    public boolean isEnabled() {
        return plugin.getFeatureManager().isEnabled(FeatureManager.Feature.SCOREBOARD)
                && plugin.getConfigManager().getScoreboard().getBoolean("SCOREBOARD.ENABLED", true);
    }

    public boolean isRuntimeSupported() {
        return true;
    }

    public void applyVisibility(Player player) {
        SidebarSettings settings = readSettings();
        if (folia) {
            if (!settings.enabled()) {
                releasePlayerFolia(player);
                return;
            }
            if (!isVisibleFor(player)) {
                hidePlayerFolia(player);
                return;
            }
            updateFolia(player, settings);
        } else {
            if (!settings.enabled()) {
                releaseOwnedBoardSpigot(player);
                return;
            }
            if (!isVisibleFor(player)) {
                hidePlayerSpigot(player);
                return;
            }
            if (!playerBoards.containsKey(player.getUniqueId())) {
                setupPlayerSpigot(player, settings);
                return;
            }
            updateSpigot(player, settings);
        }
    }

    /** Called once on player join. */
    public void setupPlayer(Player player) {
        SidebarSettings settings = readSettings();
        if (folia) {
            setupPlayerFolia(player, settings);
        } else {
            setupPlayerSpigot(player, settings);
        }
    }

    public void removePlayer(UUID uuid) {
        balanceCache.remove(uuid);
        if (folia) {
            removePlayerFolia(uuid);
        } else {
            removePlayerSpigot(uuid);
        }
    }

    public void update(Player player) {
        SidebarSettings settings = readSettings();
        if (folia) {
            updateFolia(player, settings);
        } else {
            updateSpigot(player, settings);
        }
    }

    public void updateAll() {
        if (!isEnabled()) {
            releaseAll();
            return;
        }

        // One read of the sidebar config for the whole pass. Reading it per player, and again per
        // line, was the bulk of this task's cost with a full server online.
        SidebarSettings settings = readSettings();
        if (!settings.titles().isEmpty()) {
            titleIndex = (titleIndex + 1) % settings.titles().size();
        }

        if (folia) {
            plugin.getSpigotScheduler().forEachOnlinePlayer(player -> updateFolia(player, settings));
        } else {
            for (Player player : Bukkit.getOnlinePlayers()) {
                updateSpigot(player, settings);
            }
        }
    }

    public void releaseAll() {
        if (folia) {
            releaseAllFolia();
        } else {
            releaseAllSpigot();
        }
    }

    public void invalidateAll() {
        if (!folia) {
            playerBoards.clear();
            playerLastLines.clear();
            playerLastTitle.clear();
            playerLastRawTitle.clear();
            playerObjectives.clear();
            playerTeams.clear();
            playerLineCounts.clear();
            balanceCache.clear();
        }
    }

    public void invalidatePlayer(Player player) {
        if (!folia && player != null) {
            UUID uuid = player.getUniqueId();
            playerBoards.remove(uuid);
            playerLastLines.remove(uuid);
            playerLastTitle.remove(uuid);
            playerLastRawTitle.remove(uuid);
            playerObjectives.remove(uuid);
            playerTeams.remove(uuid);
            playerLineCounts.remove(uuid);
            balanceCache.remove(uuid);
        }
    }

    // ── Folia Implementations ──────────────────────────────────────────────────

    private void setupPlayerFolia(Player player, SidebarSettings settings) {
        if (!settings.enabled()) {
            releasePlayerFolia(player);
            return;
        }
        if (!isVisibleFor(player)) {
            hidePlayerFolia(player);
            return;
        }
        renderPlayerFolia(player, settings);
    }

    private void removePlayerFolia(UUID uuid) {
        visiblePlayers.remove(uuid);
        sidebarRenderer.remove(uuid);
    }

    private void updateFolia(Player player, SidebarSettings settings) {
        if (!settings.enabled()) {
            releasePlayerFolia(player);
            return;
        }
        if (!isVisibleFor(player)) {
            hidePlayerFolia(player);
            return;
        }
        renderPlayerFolia(player, settings);
    }

    private void renderPlayerFolia(Player player, SidebarSettings settings) {
        UUID uuid = player.getUniqueId();
        String title = getTitle(player, settings);
        List<String> renderedLines = getRenderedLines(player, settings);
        String oldTitle = foliaLastTitle.get(uuid);
        String[] oldLines = foliaLastLines.get(uuid);
        boolean changed = oldTitle == null || !oldTitle.equals(title) || oldLines == null || oldLines.length != renderedLines.size();
        if (!changed) {
            for (int i = 0; i < renderedLines.size(); i++) {
                if (!renderedLines.get(i).equals(oldLines[i])) {
                    changed = true;
                    break;
                }
            }
        }
        if (changed) {
            sidebarRenderer.show(player, title, renderedLines);
            foliaLastTitle.put(uuid, title);
            foliaLastLines.put(uuid, renderedLines.toArray(new String[0]));
        }
        visiblePlayers.add(uuid);
    }

    private void hidePlayerFolia(Player player) {
        releasePlayerFolia(player);
    }

    private void releaseAllFolia() {
        foliaLastTitle.clear();
        foliaLastLines.clear();
        if (visiblePlayers.isEmpty()) {
            return;
        }
        Set<UUID> uuids = Set.copyOf(visiblePlayers);
        visiblePlayers.clear();
        for (UUID uuid : uuids) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null && player.isOnline()) {
                plugin.getSpigotScheduler().runEntity(player, () -> sidebarRenderer.hide(player));
            } else {
                sidebarRenderer.remove(uuid);
            }
        }
    }

    private void releasePlayerFolia(Player player) {
        if (player == null) {
            return;
        }
        UUID uuid = player.getUniqueId();
        foliaLastTitle.remove(uuid);
        foliaLastLines.remove(uuid);
        if (!visiblePlayers.remove(uuid)) {
            return;
        }
        sidebarRenderer.hide(player);
    }

    // ── Spigot/Paper Implementations ───────────────────────────────────────────

    private void setupPlayerSpigot(Player player, SidebarSettings settings) {
        if (!settings.enabled()) {
            releaseOwnedBoardSpigot(player);
            return;
        }
        if (!isVisibleFor(player)) {
            hidePlayerSpigot(player);
            return;
        }

        // A fresh board starts with empty teams, so any cached line/title state is stale.
        clearCacheSpigot(player.getUniqueId());

        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective("sidebar", Criteria.DUMMY, getTitle(player, settings));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        playerBoards.put(player.getUniqueId(), board);
        playerObjectives.put(player.getUniqueId(), obj);
        player.setScoreboard(board);
        updateTextSpigot(player, board, obj, settings);
    }

    private void removePlayerSpigot(UUID uuid) {
        playerBoards.remove(uuid);
        clearCacheSpigot(uuid);
    }

    private void updateSpigot(Player player, SidebarSettings settings) {
        if (!settings.enabled()) {
            releaseOwnedBoardSpigot(player);
            return;
        }
        if (!isVisibleFor(player)) {
            hidePlayerSpigot(player);
            return;
        }

        UUID uuid = player.getUniqueId();
        Scoreboard board = playerBoards.get(uuid);
        if (board == null) {
            setupPlayerSpigot(player, settings);
            return;
        }

        Objective obj = playerObjectives.get(uuid);
        if (obj == null) {
            obj = board.getObjective("sidebar");
            if (obj == null) return;
            playerObjectives.put(uuid, obj);
        }

        try {
            updateTextSpigot(player, board, obj, settings);
        } catch (IllegalStateException unregistered) {
            // Something unregistered the objective or a team out from under us. Drop the handles and
            // rebuild the board rather than leaving the player with a half-drawn sidebar.
            playerBoards.remove(uuid);
            clearCacheSpigot(uuid);
            setupPlayerSpigot(player, settings);
        }
    }

    private void updateTextSpigot(Player player, Scoreboard board, Objective obj, SidebarSettings settings) {
        UUID uuid = player.getUniqueId();
        List<String> titles = settings.titles();
        if (!titles.isEmpty()) {
            String title = titles.get(titleIndex % titles.size());
            // A title with no placeholder in it renders to the same string every pass, so the render
            // that is already on screen still stands and there is nothing to format.
            if (hasPlaceholder(title) || !title.equals(playerLastRawTitle.get(uuid))) {
                String formattedTitle = ColorUtils.toComponent(title, player);
                String oldTitle = playerLastTitle.get(uuid);
                if (oldTitle == null || !oldTitle.equals(formattedTitle)) {
                    obj.setDisplayName(formattedTitle);
                    playerLastTitle.put(uuid, formattedTitle);
                }
                playerLastRawTitle.put(uuid, title);
            }
        }

        List<String> lines = getLines(player, settings);
        int count = Math.min(lines.size(), MAX_LINES);
        Team[] teams = syncLineSlotsSpigot(board, obj, count, uuid);

        String[] oldLines = playerLastLines.get(uuid);
        if (oldLines == null || oldLines.length != MAX_LINES) {
            oldLines = new String[MAX_LINES];
            playerLastLines.put(uuid, oldLines);
        }

        for (int i = 0; i < count; i++) {
            Team team = teams[i];
            if (team == null) continue;
            String text = ColorUtils.colorize(lines.get(i), player);
            text = alignSidebarIconColumn(text, settings);
            if (!text.equals(oldLines[i])) {
                applyLineSpigot(team, text);
                oldLines[i] = text;
            }
        }

        for (int i = count; i < MAX_LINES; i++) {
            Team team = teams[i];
            if (team != null && !"".equals(oldLines[i])) {
                applyLineSpigot(team, "");
                oldLines[i] = "";
            }
        }

        numberHider.hide(player, obj, settings.hideNumbers());
    }

    /**
     * Resolves the team handle for every slot and returns them. Scores are only rewritten when the
     * line count moves, since that is the only thing they depend on.
     */
    private Team[] syncLineSlotsSpigot(Scoreboard board, Objective obj, int count, UUID uuid) {
        Team[] teams = playerTeams.get(uuid);
        if (teams == null) {
            teams = new Team[MAX_LINES];
            playerTeams.put(uuid, teams);
        }

        boolean missing = false;
        for (int i = 0; i < MAX_LINES; i++) {
            if (teams[i] == null) {
                teams[i] = board.getTeam("sb_" + i);
            }
            if (teams[i] == null && i < count) {
                missing = true;
            }
        }

        Integer lastCount = playerLineCounts.get(uuid);
        if (!missing && lastCount != null && lastCount == count) {
            return teams;
        }

        for (int i = 0; i < count; i++) {
            if (teams[i] == null) {
                teams[i] = board.registerNewTeam("sb_" + i);
                teams[i].addEntry(ENTRIES[i]);
            }
            obj.getScore(ENTRIES[i]).setScore(count - i);
        }

        for (int i = count; i < MAX_LINES; i++) {
            board.resetScores(ENTRIES[i]);
        }

        playerLineCounts.put(uuid, count);
        return teams;
    }

    private void applyLineSpigot(Team team, String text) {
        // The caller has already run the text through ColorUtils; a second pass would redo the whole
        // placeholder and colour pipeline on a string that is finished.
        if (text.length() <= 64) {
            team.setPrefix(text);
            // Prefix and suffix each broadcast a team update of their own, and a short line leaves
            // the suffix empty every pass after the first.
            applySuffixSpigot(team, "");
            return;
        }

        int split = findSafeSplit(text, 64);
        int end = findSafeSplit(text, split + 64);
        team.setPrefix(text.substring(0, split));
        applySuffixSpigot(team, text.substring(split, end));
    }

    private void applySuffixSpigot(Team team, String suffix) {
        if (!suffix.equals(team.getSuffix())) {
            team.setSuffix(suffix);
        }
    }

    private void hidePlayerSpigot(Player player) {
        releaseOwnedBoardSpigot(player);
    }

    private void releaseAllSpigot() {
        if (playerBoards.isEmpty()) {
            return;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            releaseOwnedBoardSpigot(player);
        }
        playerBoards.clear();
        playerLastLines.clear();
        playerLastTitle.clear();
        playerLastRawTitle.clear();
        playerObjectives.clear();
        playerTeams.clear();
        playerLineCounts.clear();
        balanceCache.clear();
    }

    private void releaseOwnedBoardSpigot(Player player) {
        if (player == null) {
            return;
        }

        UUID uuid = player.getUniqueId();
        Scoreboard board = playerBoards.remove(uuid);
        clearCacheSpigot(uuid);
        if (board == null || Bukkit.getScoreboardManager() == null) {
            return;
        }
        if (player.getScoreboard() == board) {
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
    }

    /** Drops the diff cache so the next render rewrites every line of a rebuilt board. */
    private void clearCacheSpigot(UUID uuid) {
        playerLastLines.remove(uuid);
        playerLastTitle.remove(uuid);
        playerLastRawTitle.remove(uuid);
        playerObjectives.remove(uuid);
        playerTeams.remove(uuid);
        playerLineCounts.remove(uuid);
        balanceCache.remove(uuid);
        if (numberHider != null) {
            numberHider.forget(uuid);
        }
    }

    // ── Common Shared Utilities ────────────────────────────────────────────────

    /** Reads every sidebar config value the render needs, once, for a whole update pass. */
    private SidebarSettings readSettings() {
        FileConfiguration scoreboard = plugin.getConfigManager().getScoreboard();
        int iconColumnWidth = Math.max(0, scoreboard.getInt("SCOREBOARD.ICON-COLUMN-WIDTH", 10));
        CurrencyManager currency = plugin.getCurrencyManager();
        Object moneyToken = currency.definitionToken(CurrencyManager.CurrencyType.MONEY);
        Object shardsToken = currency.definitionToken(CurrencyManager.CurrencyType.SHARDS);

        // An icon only moves when the currency symbol, its colour, or the column width changes.
        // Rebuilding it every pass meant walking the string for its pixel width every pass.
        if (moneyToken != iconMoneyToken || shardsToken != iconShardsToken || iconColumnWidth != iconWidth) {
            cachedMoneyIcon = sidebarCurrencyIcon(currency, CurrencyManager.CurrencyType.MONEY, iconColumnWidth);
            cachedShardsIcon = sidebarCurrencyIcon(currency, CurrencyManager.CurrencyType.SHARDS, iconColumnWidth);
            iconMoneyToken = moneyToken;
            iconShardsToken = shardsToken;
            iconWidth = iconColumnWidth;
        }

        return new SidebarSettings(
                isEnabled(),
                scoreboard.getStringList("SCOREBOARD.TITLE"),
                scoreboard.getStringList("SCOREBOARD.LINES"),
                scoreboard.getString("SCOREBOARD.TEAM"),
                scoreboard.getString("SCOREBOARD.SHARD-BOOSTER"),
                scoreboard.getString("SCOREBOARD.SHARD-CUBOID"),
                iconColumnWidth,
                scoreboard.getBoolean("SCOREBOARD.ALIGN-ICON-COLUMN", true),
                scoreboard.getBoolean("SCOREBOARD.HIDE-NUMBERS", true),
                cachedMoneyIcon,
                cachedShardsIcon,
                moneyToken,
                shardsToken
        );
    }

    private String sidebarCurrencyIcon(CurrencyManager currency, CurrencyManager.CurrencyType type, int columnWidth) {
        return paddedSidebarIcon(currency.symbolColor(type) + "&l" + currency.symbol(type), columnWidth);
    }

    private static boolean hasPlaceholder(String text) {
        return text != null && (text.indexOf('%') >= 0 || text.indexOf('{') >= 0);
    }

    private List<String> getLines(Player player, SidebarSettings settings) {
        List<String> lines = new ArrayList<>();
        String teamLine = settings.teamLine();
        String boosterLine = settings.boosterLine();
        String shardCuboidLine = settings.shardCuboidLine();
        boolean inTeam = plugin.getTeamManager().isInTeam(player.getUniqueId());
        boolean hasBooster = plugin.getShardManager().hasBooster(player.getUniqueId());
        boolean showShardCuboid = plugin.getShardManager().shouldShowShardCuboidLine(player.getUniqueId());

        // Formatting a balance runs it through DecimalFormat, and the result only moves when the
        // balance does. Hold the last one per player and reuse it until the amount or the currency
        // config changes underneath it.
        PlayerData data = plugin.getPlayerDataManager().get(player);
        double money = data != null ? data.getMoney() : 0D;
        long shards = data != null ? data.getShards() : 0L;

        UUID uuid = player.getUniqueId();
        FormattedBalances balances = balanceCache.get(uuid);
        if (balances == null
                || balances.moneyToken() != settings.moneyToken()
                || balances.shardsToken() != settings.shardsToken()
                || Double.compare(balances.money(), money) != 0
                || balances.shards() != shards) {
            CurrencyManager currencyManager = plugin.getCurrencyManager();
            balances = new FormattedBalances(
                    settings.moneyToken(),
                    settings.shardsToken(),
                    money,
                    shards,
                    currencyManager.formatCompactAmount(CurrencyManager.CurrencyType.MONEY, money),
                    currencyManager.formatCompactAmount(CurrencyManager.CurrencyType.SHARDS, shards)
            );
            balanceCache.put(uuid, balances);
        }
        String moneyShort = balances.moneyText();
        String shardsShort = balances.shardsText();

        for (String line : settings.lines()) {
            String resolved = resolveConfiguredLine(
                    line,
                    teamLine,
                    boosterLine,
                    shardCuboidLine,
                    inTeam,
                    hasBooster,
                    showShardCuboid
            );
            if (resolved != null && !isHiddenStatLine(resolved, data)) {
                resolved = applySidebarEconomyPlaceholders(resolved, moneyShort, shardsShort);
                lines.add(applySidebarLayoutPlaceholders(resolved, settings));
            }
        }

        return lines;
    }

    private String resolveConfiguredLine(
            String line,
            String teamLine,
            String boosterLine,
            String shardCuboidLine,
            boolean inTeam,
            boolean hasBooster,
            boolean showShardCuboid
    ) {
        if (line == null) {
            return null;
        }

        String trimmed = line.trim();
        if ("{team}".equalsIgnoreCase(trimmed)) {
            return inTeam ? teamLine : null;
        }
        if ("{shard_booster}".equalsIgnoreCase(trimmed)) {
            return hasBooster ? boosterLine : null;
        }
        if ("{shard_cuboid}".equalsIgnoreCase(trimmed)) {
            return showShardCuboid ? shardCuboidLine : null;
        }
        return line;
    }

    /**
     * Drops the sidebar lines a player switched off in /settings.
     *
     * <p>The lines are free-form templates, so the placeholder inside one is what says which
     * statistic it reports. An admin who rewords a line keeps its toggle as long as the placeholder
     * survives, and a line carrying none of them is never hidden.</p>
     */
    private boolean isHiddenStatLine(String line, PlayerData data) {
        if (data == null || line == null || line.indexOf("%economy_") < 0) {
            return false;
        }
        for (StatLine stat : STAT_LINES) {
            if (!stat.visible().test(data) && containsAnyPlaceholder(line, stat.placeholders())) {
                return true;
            }
        }
        return false;
    }

    private boolean containsAnyPlaceholder(String line, String[] placeholders) {
        for (String placeholder : placeholders) {
            if (line.contains(placeholder)) {
                return true;
            }
        }
        return false;
    }

    private String applySidebarEconomyPlaceholders(String line, String moneyShort, String shardsShort) {
        if (line == null || line.isEmpty()) {
            return line == null ? "" : line;
        }
        // One scan rules out all seven replacements below, and most sidebar lines carry none of them.
        if (line.indexOf("%economy_") < 0) {
            return line;
        }

        return line
                .replace("%economy_nicestMoney%", moneyShort)
                .replace("%economy_money_short%", moneyShort)
                .replace("%economy_money_amount_short%", moneyShort)
                .replace("%economy_nicestShards%", shardsShort)
                .replace("%economy_shards_short%", shardsShort)
                .replace("%economy_shards_amount_short%", shardsShort)
                .replace("%economy_shards%", shardsShort);
    }

    private String applySidebarLayoutPlaceholders(String line, SidebarSettings settings) {
        if (line == null || line.isEmpty()) {
            return "";
        }

        String result = line
                .replace("{money_icon}", settings.moneyIcon())
                .replace("{shards_icon}", settings.shardsIcon());

        if (result.indexOf("{sb_icon:") < 0) {
            return result;
        }

        Matcher matcher = SIDEBAR_ICON_PATTERN.matcher(result);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(buffer,
                    Matcher.quoteReplacement(paddedSidebarIcon(matcher.group(1), settings.iconColumnWidth())));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private String paddedSidebarIcon(String icon, int columnWidth) {
        int iconWidth = minecraftTextWidth(icon);
        int missingWidth = Math.max(0, columnWidth - iconWidth);
        int spaces = Math.max(1, Math.round(missingWidth / 4F));
        return icon + " ".repeat(spaces);
    }

    private String alignSidebarIconColumn(String text, SidebarSettings settings) {
        if (text == null || text.isEmpty() || !settings.alignIconColumn()) {
            return text == null ? "" : text;
        }

        int iconStart = firstVisibleIndex(text, 0);
        if (iconStart < 0) {
            return text;
        }

        int iconEnd = iconStart + Character.charCount(text.codePointAt(iconStart));
        int cursor = iconEnd;
        while (cursor < text.length()) {
            int formattingEnd = formattingEnd(text, cursor);
            if (formattingEnd <= cursor) {
                break;
            }
            cursor = formattingEnd;
        }

        int spacesStart = cursor;
        while (cursor < text.length() && text.charAt(cursor) == ' ') {
            cursor++;
        }
        if (spacesStart == cursor) {
            return text;
        }

        int nextVisible = firstVisibleIndex(text, cursor);
        if (nextVisible < 0) {
            return text;
        }

        String iconText = text.substring(0, iconEnd);
        int columnWidth = settings.iconColumnWidth();
        int iconWidth = minecraftTextWidth(iconText);
        int missingWidth = Math.max(0, columnWidth - iconWidth);
        int spaces = Math.max(1, Math.round(missingWidth / 4F));
        return text.substring(0, spacesStart) + " ".repeat(spaces) + text.substring(cursor);
    }

    private int firstVisibleIndex(String text, int start) {
        int index = Math.max(0, start);
        while (index < text.length()) {
            int formattingEnd = formattingEnd(text, index);
            if (formattingEnd > index) {
                index = formattingEnd;
                continue;
            }
            return index;
        }
        return -1;
    }

    private int formattingEnd(String text, int index) {
        if (index < 0 || index + 1 >= text.length() || text.charAt(index) != SECTION_CHAR) {
            return index;
        }

        char code = Character.toLowerCase(text.charAt(index + 1));
        if (code == 'x' && index + 13 < text.length()) {
            return index + 14;
        }
        return index + 2;
    }

    private int minecraftTextWidth(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }

        int width = 0;
        boolean bold = false;
        for (int i = 0; i < text.length(); ) {
            char current = text.charAt(i);
            if (current == '&' && i + 7 < text.length() && text.charAt(i + 1) == '#'
                    && isHexColor(text, i + 2)) {
                bold = false;
                i += 8;
                continue;
            }
            if ((current == '&' || current == SECTION_CHAR) && i + 1 < text.length()) {
                char code = Character.toLowerCase(text.charAt(i + 1));
                if (code == 'x' && current == SECTION_CHAR && i + 13 < text.length()) {
                    bold = false;
                    i += 14;
                    continue;
                }
                if ("0123456789abcdefr".indexOf(code) >= 0) {
                    bold = false;
                } else if (code == 'l') {
                    bold = true;
                }
                i += 2;
                continue;
            }

            int codePoint = text.codePointAt(i);
            int charWidth = minecraftCharWidth(codePoint);
            width += bold && charWidth > 0 ? charWidth + 1 : charWidth;
            i += Character.charCount(codePoint);
        }
        return width;
    }

    private boolean isHexColor(String text, int start) {
        if (start + 6 > text.length()) {
            return false;
        }
        for (int i = start; i < start + 6; i++) {
            char c = text.charAt(i);
            if (!((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F'))) {
                return false;
            }
        }
        return true;
    }

    private int minecraftCharWidth(int codePoint) {
        return switch (codePoint) {
            case ' ', '\u00A0' -> 4;
            case '!', '.', ',', ':', ';', '|', 'i', '\'', '`' -> 2;
            case 'l', 'I', '[', ']', 't' -> 3;
            case '"', '(', ')', '*', '<', '>', '{', '}', 'f', 'k' -> 5;
            case '@', '~' -> 7;
            default -> codePoint > 127 ? 7 : 6;
        };
    }

    private String getTitle(Player player, SidebarSettings settings) {
        List<String> titles = settings.titles();
        if (titles.isEmpty()) {
            return ColorUtils.colorize("EconomySMP", player);
        }
        return ColorUtils.colorize(titles.get(titleIndex % titles.size()), player);
    }

    private List<String> getRenderedLines(Player player, SidebarSettings settings) {
        List<String> lines = getLines(player, settings);
        List<String> rendered = new ArrayList<>(Math.min(lines.size(), MAX_LINES));
        for (String line : lines) {
            if (rendered.size() >= MAX_LINES) {
                break;
            }
            String text = ColorUtils.colorize(line, player);
            rendered.add(alignSidebarIconColumn(text, settings));
        }
        return rendered;
    }

    private boolean isVisibleFor(Player player) {
        PlayerData data = plugin.getPlayerDataManager().get(player);
        return data == null || data.isScoreboardVisible();
    }

    private int findSafeSplit(String text, int max) {
        if (max >= text.length()) return text.length();
        int split = max;
        // A surrogate pair has to stay in one half, or the client draws two broken glyphs.
        if (split > 0 && Character.isHighSurrogate(text.charAt(split - 1))) split--;
        if (split > 0 && text.charAt(split - 1) == '\u00A7') split--;
        return split;
    }

    /** One toggleable sidebar statistic: how to read the player's choice, and what marks its line. */
    private record StatLine(java.util.function.Predicate<PlayerData> visible, String[] placeholders) {
    }

    /** The sidebar config as it stood at the start of one update pass. */
    private record SidebarSettings(
            boolean enabled,
            List<String> titles,
            List<String> lines,
            String teamLine,
            String boosterLine,
            String shardCuboidLine,
            int iconColumnWidth,
            boolean alignIconColumn,
            boolean hideNumbers,
            String moneyIcon,
            String shardsIcon,
            Object moneyToken,
            Object shardsToken
    ) {
    }

    /** A player's balances as they were last formatted, with the currency definitions behind them. */
    private record FormattedBalances(
            Object moneyToken,
            Object shardsToken,
            double money,
            long shards,
            String moneyText,
            String shardsText
    ) {
    }
}
