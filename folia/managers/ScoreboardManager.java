package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.PacketSidebarRenderer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScoreboardManager {

    private static final int MAX_LINES = 15;
    private static final Pattern SIDEBAR_ICON_PATTERN = Pattern.compile("\\{sb_icon:([^}]*)\\}");
    private static final char SECTION_CHAR = '\u00A7';

    private final UltimateDonutSmp plugin;
    private final PacketSidebarRenderer sidebarRenderer;
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
        if (!isEnabled() || !isVisibleFor(player)) {
            hidePlayer(player);
            return;
        }

        update(player);
    }

    public void setupPlayer(Player player) {
        update(player);
    }

    public void removePlayer(UUID uuid) {
        sidebarRenderer.remove(uuid);
    }

    public void update(Player player) {
        if (!isEnabled() || !isVisibleFor(player)) {
            hidePlayer(player);
            return;
        }

        sidebarRenderer.show(player, currentTitle(player), visibleLines(player));
    }

    private String currentTitle(Player player) {
        List<String> titles = plugin.getConfigManager().getScoreboard()
                .getStringList("SCOREBOARD.TITLE");
        if (titles.isEmpty()) {
            return ColorUtils.colorize("EconomySMP", player);
        }

        return ColorUtils.colorize(titles.get(titleIndex % titles.size()), player);
    }

    private List<String> visibleLines(Player player) {
        List<String> configuredLines = getLines(player);
        List<String> visible = new ArrayList<>();
        for (String line : configuredLines) {
            if (visible.size() >= MAX_LINES) {
                break;
            }
            String text = ColorUtils.colorize(line, player);
            visible.add(alignSidebarIconColumn(text));
        }
        return visible;
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

    public void updateAll() {
        List<String> titles = plugin.getConfigManager().getScoreboard()
                .getStringList("SCOREBOARD.TITLE");
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
        sidebarRenderer.hide(player);
    }
}
