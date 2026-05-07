package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import io.papermc.paper.scoreboard.numbers.NumberFormat;
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
import java.util.UUID;

public class ScoreboardManager {

    private static final int MAX_LINES = 15;

    // Unique invisible entries, one per line slot, so updates stay flicker free.
    private static final String[] ENTRIES = new String[MAX_LINES];
    static {
        String[] codes = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e"};
        for (int i = 0; i < MAX_LINES; i++) {
            ENTRIES[i] = "\u00A7" + codes[i] + "\u00A7r";
        }
    }

    private final UltimateDonutSmp plugin;
    private final Map<UUID, Scoreboard> playerBoards = new HashMap<>();
    private int titleIndex = 0;

    public ScoreboardManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public boolean isEnabled() {
        return plugin.getConfigManager().getScoreboard().getBoolean("SCOREBOARD.ENABLED", true);
    }

    public void applyVisibility(Player player) {
        if (!isEnabled() || !isVisibleFor(player)) {
            hidePlayer(player);
            return;
        }

        if (!playerBoards.containsKey(player.getUniqueId())) {
            setupPlayer(player);
            return;
        }

        update(player);
    }

    /** Called once on player join, creates the board structure. */
    public void setupPlayer(Player player) {
        if (!isEnabled() || !isVisibleFor(player)) {
            hidePlayer(player);
            return;
        }

        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective("sidebar", Criteria.DUMMY,
                ColorUtils.toComponent("EconomySMP"));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        obj.numberFormat(NumberFormat.blank());

        playerBoards.put(player.getUniqueId(), board);
        player.setScoreboard(board);
        updateText(player, board, obj);
    }

    public void removePlayer(UUID uuid) {
        playerBoards.remove(uuid);
    }

    /** Called every tick, only updates text without recreating entries. */
    public void update(Player player) {
        if (!isEnabled() || !isVisibleFor(player)) {
            hidePlayer(player);
            return;
        }

        Scoreboard board = playerBoards.get(player.getUniqueId());
        if (board == null) {
            setupPlayer(player);
            return;
        }

        Objective obj = board.getObjective("sidebar");
        if (obj == null) return;
        updateText(player, board, obj);
    }

    private void updateText(Player player, Scoreboard board, Objective obj) {
        List<String> titles = plugin.getConfigManager().getScoreboard()
                .getStringList("SCOREBOARD.TITLE");
        if (!titles.isEmpty()) {
            String title = titles.get(titleIndex % titles.size());
            obj.displayName(ColorUtils.toComponent(title, player));
        }

        List<String> lines = getLines(player);
        int count = Math.min(lines.size(), MAX_LINES);
        syncLineSlots(board, obj, count);

        for (int i = 0; i < count; i++) {
            Team team = board.getTeam("sb_" + i);
            if (team == null) continue;
            String text = ColorUtils.colorize(lines.get(i), player);
            applyLine(team, text);
        }

        for (int i = count; i < MAX_LINES; i++) {
            Team team = board.getTeam("sb_" + i);
            if (team != null) applyLine(team, "");
        }
    }

    private List<String> getLines(Player player) {
        FileConfiguration scoreboard = plugin.getConfigManager().getScoreboard();
        List<String> lines = new ArrayList<>();
        String teamLine = scoreboard.getString("SCOREBOARD.TEAM");
        String boosterLine = scoreboard.getString("SCOREBOARD.SHARD-BOOSTER");
        String shardCuboidLine = scoreboard.getString("SCOREBOARD.SHARD-CUBOID");
        boolean hasBooster = plugin.getShardManager().hasBooster(player.getUniqueId());
        boolean showShardCuboid = plugin.getShardManager().shouldShowShardCuboidLine(player.getUniqueId());

        for (String line : scoreboard.getStringList("SCOREBOARD.LINES")) {
            String resolved = resolveConfiguredLine(
                    line,
                    teamLine,
                    boosterLine,
                    shardCuboidLine,
                    hasBooster,
                    showShardCuboid
            );
            if (resolved != null) {
                lines.add(resolved);
            }
        }

        return lines;
    }

    private String resolveConfiguredLine(
            String line,
            String teamLine,
            String boosterLine,
            String shardCuboidLine,
            boolean hasBooster,
            boolean showShardCuboid
    ) {
        if (line == null) {
            return null;
        }

        String trimmed = line.trim();
        if ("{team}".equalsIgnoreCase(trimmed)) {
            return teamLine;
        }
        if ("{shard_booster}".equalsIgnoreCase(trimmed)) {
            return hasBooster ? boosterLine : null;
        }
        if ("{shard_cuboid}".equalsIgnoreCase(trimmed)) {
            return showShardCuboid ? shardCuboidLine : null;
        }
        return line;
    }

    private void syncLineSlots(Scoreboard board, Objective obj, int count) {
        for (int i = 0; i < count; i++) {
            Team team = board.getTeam("sb_" + i);
            if (team == null) {
                team = board.registerNewTeam("sb_" + i);
                team.addEntry(ENTRIES[i]);
            }
            obj.getScore(ENTRIES[i]).setScore(count - i);
        }

        for (int i = count; i < MAX_LINES; i++) {
            board.resetScores(ENTRIES[i]);
        }
    }

    private void applyLine(Team team, String text) {
        if (text.length() <= 64) {
            team.prefix(ColorUtils.toComponent(text));
            team.suffix(ColorUtils.toComponent(""));
            return;
        }

        int split = findSafeSplit(text, 64);
        team.prefix(ColorUtils.toComponent(text.substring(0, split)));
        team.suffix(ColorUtils.toComponent(text.substring(split, Math.min(text.length(), split + 64))));
    }

    private int findSafeSplit(String text, int max) {
        if (max >= text.length()) return text.length();
        int split = max;
        if (split > 0 && text.charAt(split - 1) == '\u00A7') split--;
        return split;
    }

    public void updateAll() {
        List<String> titles = plugin.getConfigManager().getScoreboard()
                .getStringList("SCOREBOARD.TITLE");
        if (!titles.isEmpty()) titleIndex = (titleIndex + 1) % titles.size();

        for (Player player : Bukkit.getOnlinePlayers()) {
            update(player);
        }
    }

    private boolean isVisibleFor(Player player) {
        PlayerData data = plugin.getPlayerDataManager().get(player);
        return data == null || data.isScoreboardVisible();
    }

    private void hidePlayer(Player player) {
        playerBoards.remove(player.getUniqueId());
        if (Bukkit.getScoreboardManager() != null) {
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
    }
}

