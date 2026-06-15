package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.PacketSidebarRenderer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ScoreboardManager {

    private static final int MAX_LINES = 15;

    private final UltimateDonutSmp plugin;
    private final PacketSidebarRenderer sidebarRenderer;
    private final Set<UUID> visiblePlayers = new HashSet<>();
    private int titleIndex = 0;

    public ScoreboardManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
        this.sidebarRenderer = new PacketSidebarRenderer(plugin);
    }

    public boolean isEnabled() {
        return plugin.getFeatureManager().isEnabled(FeatureManager.Feature.SCOREBOARD)
                && plugin.getConfigManager().getScoreboard().getBoolean("SCOREBOARD.ENABLED", true);
    }

    public void applyVisibility(Player player) {
        if (!isEnabled()) {
            releasePlayer(player);
            return;
        }
        if (!isVisibleFor(player)) {
            hidePlayer(player);
            return;
        }

        update(player);
    }

    /** Called once on player join, creates the client-side sidebar. */
    public void setupPlayer(Player player) {
        if (!isEnabled()) {
            releasePlayer(player);
            return;
        }
        if (!isVisibleFor(player)) {
            hidePlayer(player);
            return;
        }

        renderPlayer(player);
    }

    public void removePlayer(UUID uuid) {
        visiblePlayers.remove(uuid);
        sidebarRenderer.remove(uuid);
    }

    /** Called every tick, only updates changed sidebar packets. */
    public void update(Player player) {
        if (!isEnabled()) {
            releasePlayer(player);
            return;
        }
        if (!isVisibleFor(player)) {
            hidePlayer(player);
            return;
        }

        renderPlayer(player);
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
                lines.add(applySidebarEconomyPlaceholders(resolved, player));
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

    private String applySidebarEconomyPlaceholders(String line, Player player) {
        if (line == null || line.isEmpty()) {
            return line == null ? "" : line;
        }

        PlayerData data = plugin.getPlayerDataManager().get(player);
        double money = data != null ? data.getMoney() : 0D;
        long shards = data != null ? data.getShards() : 0L;
        CurrencyManager currencyManager = plugin.getCurrencyManager();
        String moneyShort = currencyManager.formatCompactAmount(CurrencyManager.CurrencyType.MONEY, money);
        String shardsShort = currencyManager.formatCompactAmount(CurrencyManager.CurrencyType.SHARDS, shards);

        return line
                .replace("%economy_nicestMoney%", moneyShort)
                .replace("%economy_money_short%", moneyShort)
                .replace("%economy_money_amount_short%", moneyShort)
                .replace("%economy_nicestShards%", shardsShort)
                .replace("%economy_shards_short%", shardsShort)
                .replace("%economy_shards_amount_short%", shardsShort)
                .replace("%economy_shards%", shardsShort);
    }

    private void renderPlayer(Player player) {
        sidebarRenderer.show(player, getTitle(player), getRenderedLines(player));
        visiblePlayers.add(player.getUniqueId());
    }

    private String getTitle(Player player) {
        List<String> titles = plugin.getConfigManager().getScoreboard()
                .getStringList("SCOREBOARD.TITLE");
        if (titles.isEmpty()) {
            return ColorUtils.colorize("EconomySMP", player);
        }
        return ColorUtils.colorize(titles.get(titleIndex % titles.size()), player);
    }

    private List<String> getRenderedLines(Player player) {
        List<String> lines = getLines(player);
        List<String> rendered = new ArrayList<>(Math.min(lines.size(), MAX_LINES));
        for (String line : lines) {
            if (rendered.size() >= MAX_LINES) {
                break;
            }
            rendered.add(ColorUtils.colorize(line, player));
        }
        return rendered;
    }

    public void updateAll() {
        if (!isEnabled()) {
            releaseAll();
            return;
        }

        List<String> titles = plugin.getConfigManager().getScoreboard()
                .getStringList("SCOREBOARD.TITLE");
        if (!titles.isEmpty()) titleIndex = (titleIndex + 1) % titles.size();

        plugin.getFoliaScheduler().forEachOnlinePlayer(this::update);
    }

    private boolean isVisibleFor(Player player) {
        PlayerData data = plugin.getPlayerDataManager().get(player);
        return data == null || data.isScoreboardVisible();
    }

    private void hidePlayer(Player player) {
        releasePlayer(player);
    }

    public void releaseAll() {
        if (visiblePlayers.isEmpty()) {
            return;
        }

        Set<UUID> uuids = new HashSet<>(visiblePlayers);
        for (UUID uuid : uuids) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null && player.isOnline()) {
                sidebarRenderer.hide(player);
            } else {
                sidebarRenderer.remove(uuid);
            }
        }
        visiblePlayers.clear();
    }

    private void releasePlayer(Player player) {
        if (player == null || !visiblePlayers.remove(player.getUniqueId())) {
            return;
        }
        sidebarRenderer.hide(player);
    }
}
