package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.PacketSidebarRenderer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScoreboardManager {

    private static final int MAX_LINES = 15;
    private static final Pattern SIDEBAR_ICON_PATTERN = Pattern.compile("\\{sb_icon:([^}]*)\\}");
    private static final char SECTION_CHAR = '\u00A7';

    private final UltimateDonutSmp plugin;
    private final PacketSidebarRenderer sidebarRenderer;
    private final Set<UUID> visiblePlayers = ConcurrentHashMap.newKeySet();
    private volatile int titleIndex = 0;

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

    /** Called every update cycle, only sends changed sidebar packets. */
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
                resolved = applySidebarEconomyPlaceholders(resolved, player);
                lines.add(applySidebarLayoutPlaceholders(resolved));
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

    private String applySidebarLayoutPlaceholders(String line) {
        if (line == null || line.isEmpty()) {
            return "";
        }

        String result = line
                .replace("{money_icon}", paddedSidebarIcon(
                        plugin.getCurrencyManager().symbolColor(CurrencyManager.CurrencyType.MONEY)
                                + "&l"
                                + plugin.getCurrencyManager().symbol(CurrencyManager.CurrencyType.MONEY)))
                .replace("{shards_icon}", paddedSidebarIcon(
                        plugin.getCurrencyManager().symbolColor(CurrencyManager.CurrencyType.SHARDS)
                                + "&l"
                                + plugin.getCurrencyManager().symbol(CurrencyManager.CurrencyType.SHARDS)));

        Matcher matcher = SIDEBAR_ICON_PATTERN.matcher(result);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(paddedSidebarIcon(matcher.group(1))));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private String paddedSidebarIcon(String icon) {
        int columnWidth = Math.max(0, plugin.getConfigManager().getScoreboard()
                .getInt("SCOREBOARD.ICON-COLUMN-WIDTH", 10));
        int iconWidth = minecraftTextWidth(icon);
        int missingWidth = Math.max(0, columnWidth - iconWidth);
        int spaces = Math.max(1, Math.round(missingWidth / 4F));
        return icon + " ".repeat(spaces);
    }

    private String alignSidebarIconColumn(String text) {
        if (text == null || text.isEmpty() || !plugin.getConfigManager().getScoreboard()
                .getBoolean("SCOREBOARD.ALIGN-ICON-COLUMN", true)) {
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
        int columnWidth = Math.max(0, plugin.getConfigManager().getScoreboard()
                .getInt("SCOREBOARD.ICON-COLUMN-WIDTH", 10));
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

    private void renderPlayer(Player player) {
        sidebarRenderer.show(player, getTitle(player), getRenderedLines(player));
        visiblePlayers.add(player.getUniqueId());
    }

    private String getTitle(Player player) {
        List<String> titles = plugin.getConfigManager().getScoreboard().getStringList("SCOREBOARD.TITLE");
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
            rendered.add(alignSidebarIconColumn(ColorUtils.colorize(line, player)));
        }
        return rendered;
    }

    public void updateAll() {
        if (!isEnabled()) {
            releaseAll();
            return;
        }

        List<String> titles = plugin.getConfigManager().getScoreboard().getStringList("SCOREBOARD.TITLE");
        if (!titles.isEmpty()) {
            titleIndex = (titleIndex + 1) % titles.size();
        }

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

        Set<UUID> uuids = Set.copyOf(visiblePlayers);
        visiblePlayers.clear();
        for (UUID uuid : uuids) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null && player.isOnline()) {
                plugin.getFoliaScheduler().runEntity(player, () -> sidebarRenderer.hide(player));
            } else {
                sidebarRenderer.remove(uuid);
            }
        }
    }

    private void releasePlayer(Player player) {
        if (player == null || !visiblePlayers.remove(player.getUniqueId())) {
            return;
        }
        sidebarRenderer.hide(player);
    }
}
