package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.models.Team;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HoverStatsManager {

    private static final String DEFAULT_CHAT_FORMAT = "&f%prefix%%player%&7: &f%message%";
    private static final String NAME_TOKEN = "__UDS_HOVER_NAME__";
    private static final String SENDER_TOKEN = "__UDS_HOVER_SENDER__";
    private static final String MESSAGE_TOKEN = "__UDS_HOVER_MESSAGE__";

    private final UltimateDonutSmp plugin;

    public HoverStatsManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public boolean isEnabled() {
        return plugin.getChatManager().isClickableNameEnabled();
    }

    public Component buildChatComponent(Player speaker, String prefix, String rawMessage, String chatFormat) {
        String format = (chatFormat == null || chatFormat.isBlank()) ? DEFAULT_CHAT_FORMAT : chatFormat;
        format = format.replace("%nick%", "%player%").replace("<nick>", "%player%");
        String displayName = resolveDisplayName(speaker);
        HoverEvent<Component> hover = buildHover(speaker, prefix);
        ClickEvent click = buildClick(speaker);
        Component messageComponent = buildMessageComponent(speaker, rawMessage);

        String resolvedFormat = format.replace("%prefix%", prefix == null ? "" : prefix);
        int playerIndex = resolvedFormat.indexOf("%player%");
        int messageIndex = resolvedFormat.indexOf("%message%");

        if (playerIndex < 0 || messageIndex < 0 || playerIndex > messageIndex) {
            String legacyTemplate = resolvedFormat
                    .replace("%player%", displayName)
                    .replace("%message%", MESSAGE_TOKEN);
            Component component = ColorUtils.toComponent(legacyTemplate);
            component = replaceMessageToken(component, messageComponent);
            if (isEnabled()) {
                component = applyEvents(component, hover, click);
            }
            return component;
        }

        String beforePlayer = resolvedFormat.substring(0, playerIndex);
        String betweenPlayerAndMessage = resolvedFormat.substring(playerIndex + "%player%".length(), messageIndex);
        String afterMessage = resolvedFormat.substring(messageIndex + "%message%".length())
                .replace("%player%", displayName);

        if (!isEnabled()) {
            String template = beforePlayer + displayName + betweenPlayerAndMessage + MESSAGE_TOKEN + afterMessage;
            return replaceMessageToken(ColorUtils.toComponent(template), messageComponent);
        }

        String applyTo = resolveApplyTo();

        Component component;
        if ("SENDER".equalsIgnoreCase(applyTo)) {
            String template = SENDER_TOKEN + betweenPlayerAndMessage + MESSAGE_TOKEN + afterMessage;
            component = ColorUtils.toComponent(template);

            String senderText = beforePlayer + displayName;
            Component senderComponent = applyEvents(ColorUtils.toComponent(senderText), hover, click);

            component = component.replaceText(TextReplacementConfig.builder()
                    .matchLiteral(SENDER_TOKEN)
                    .replacement(senderComponent)
                    .build());
        } else {
            String template = beforePlayer + NAME_TOKEN + betweenPlayerAndMessage + MESSAGE_TOKEN + afterMessage;
            component = ColorUtils.toComponent(template);
            Component nameComponent = ColorUtils.toComponent(displayName);
            if (hover != null) {
                nameComponent = nameComponent.hoverEvent(hover);
            }
            if (click != null) {
                nameComponent = nameComponent.clickEvent(click);
            }

            component = component.replaceText(TextReplacementConfig.builder()
                    .matchLiteral(NAME_TOKEN)
                    .replacement(nameComponent)
                    .build());
        }

        return replaceMessageToken(component, messageComponent);
    }

    public HoverEvent<Component> buildHover(Player speaker, String prefix) {
        List<String> hoverLines = getHoverLines();
        if (hoverLines.isEmpty()) {
            return null;
        }

        FileConfiguration config = plugin.getConfigManager().getConfig();
        PlayerData data = plugin.getPlayerDataManager().get(speaker);
        if (data == null && !config.getBoolean("CHAT-FORMAT.HOVER-STATS.FALLBACK-IF-NO-DATA", true)) {
            return null;
        }

        String resolvedPrefix = resolvePrefix(speaker, prefix);
        Map<String, String> placeholders = buildPlaceholders(speaker, resolvedPrefix, data);
        StringBuilder hoverText = new StringBuilder();
        for (String line : hoverLines) {
            if (!hoverText.isEmpty()) {
                hoverText.append("\n");
            }
            hoverText.append(ColorUtils.colorize(replacePlaceholders(line, placeholders), speaker));
        }
        return HoverEvent.showText(ColorUtils.toComponent(hoverText.toString()));
    }

    public ClickEvent buildClick(Player speaker) {
        ChatManager.ClickAction clickAction = plugin.getChatManager().getClickableNameAction(speaker);
        if (clickAction == null || clickAction.command() == null || clickAction.command().isBlank()) {
            return null;
        }
        return clickAction.type() == ChatManager.ClickActionType.SUGGEST_COMMAND
                ? ClickEvent.suggestCommand(clickAction.command())
                : ClickEvent.runCommand(clickAction.command());
    }

    private List<String> getHoverLines() {
        List<String> lines = plugin.getChatManager().getClickableHoverText();
        if (!lines.isEmpty()) {
            return lines;
        }

        return List.of(
                "%prefix%%player%",
                "&7&m----------",
                "&aMoney: &f%money%",
                "&cKills: &f%kills%",
                "&ePlaytime: &f%playtime%",
                "&6Deaths: &f%deaths%",
                "&dShards: &f%shards%",
                "&7&m----------",
                "&7Click to view stats"
        );
    }

    private Map<String, String> buildPlaceholders(Player speaker, String prefix, PlayerData data) {
        Map<String, String> placeholders = new LinkedHashMap<>();
        Team team = plugin.getTeamManager().getTeam(speaker.getUniqueId());
        String teamName = team != null ? team.getName().toUpperCase() : "None";
        String displayName = resolveDisplayName(speaker);

        placeholders.put("%player%", displayName);
        placeholders.put("%nick%", displayName);
        placeholders.put("%real_player%", speaker.getName());
        placeholders.put("%realname%", speaker.getName());
        placeholders.put("%prefix%", prefix == null ? "" : prefix);
        placeholders.put("%luckperms_prefix%", prefix == null ? "" : prefix);
        placeholders.put("%team%", teamName);
        placeholders.put("%money%", data != null ? NumberUtils.formatNice(data.getMoney()) : "0");
        placeholders.put("%money_raw%", data != null ? NumberUtils.format(data.getMoney()) : "0");
        placeholders.put("%kills%", String.valueOf(data != null ? data.getKills() : 0));
        placeholders.put("%deaths%", String.valueOf(data != null ? data.getDeaths() : 0));
        placeholders.put("%playtime%", data != null ? NumberUtils.formatTimeLong(data.getTotalPlaytimeSeconds()) : "0s");
        placeholders.put("%shards%", String.valueOf(data != null ? data.getShards() : 0));
        placeholders.put("%blocks_placed%", String.valueOf(data != null ? data.getBlocksPlaced() : 0));
        placeholders.put("%blocks_broken%", String.valueOf(data != null ? data.getBlocksBroken() : 0));
        placeholders.put("%mobs_killed%", String.valueOf(data != null ? data.getMobsKilled() : 0));
        placeholders.put("%killstreak%", String.valueOf(data != null ? data.getKillStreak() : 0));
        placeholders.put("%highest_killstreak%", String.valueOf(data != null ? data.getHighestKillStreak() : 0));
        placeholders.put("%money_made%", data != null ? NumberUtils.formatNice(data.getMoneyMade()) : "0");
        placeholders.put("%money_spent%", data != null ? NumberUtils.formatNice(data.getMoneySpent()) : "0");
        return placeholders;
    }

    private String replacePlaceholders(String text, Map<String, String> placeholders) {
        String replaced = text;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            replaced = replaced.replace(entry.getKey(), entry.getValue());
        }
        return replaced;
    }

    private String resolveDisplayName(Player player) {
        if (!ColorUtils.hasPAPI()) {
            return player.getName();
        }

        try {
            String nickname = me.clip.placeholderapi.PlaceholderAPI
                    .setPlaceholders(player, "%nickplus_nick%");
            if (nickname == null || nickname.isBlank() || nickname.startsWith("%")) {
                return player.getName();
            }
            return nickname;
        } catch (Exception ignored) {
            return player.getName();
        }
    }

    private Component replaceMessageToken(Component component, Component messageComponent) {
        return component.replaceText(TextReplacementConfig.builder()
                .matchLiteral(MESSAGE_TOKEN)
                .replacement(messageComponent)
                .build());
    }

    private Component applyEvents(Component component, HoverEvent<Component> hover, ClickEvent click) {
        if (hover != null) {
            component = component.hoverEvent(hover);
        }
        if (click != null) {
            component = component.clickEvent(click);
        }
        return component;
    }

    private String resolveApplyTo() {
        FileConfiguration config = plugin.getConfigManager().getConfig();
        if (config.contains("CHAT.CLICKABLE-NAME.ENABLED")) {
            return "NAME";
        }
        return config.getString("CHAT-FORMAT.HOVER-STATS.APPLY-TO", "NAME");
    }

    private Component buildMessageComponent(Player speaker, String rawMessage) {
        String messageColor = plugin.getChatManager().resolveMessageColor(speaker);
        Component probe = ColorUtils.toComponent((messageColor == null || messageColor.isBlank() ? "&f" : messageColor) + "x");
        return Component.text(rawMessage == null ? "" : rawMessage).style(probe.style());
    }

    private String resolvePrefix(Player player, String fallbackPrefix) {
        if (fallbackPrefix != null && !fallbackPrefix.isBlank()) {
            return fallbackPrefix;
        }
        if (!ColorUtils.hasPAPI()) {
            return "";
        }

        try {
            String prefix = me.clip.placeholderapi.PlaceholderAPI
                    .setPlaceholders(player, "%luckperms_prefix%");
            if (prefix == null || prefix.isBlank() || prefix.startsWith("%")) {
                return "";
            }
            return prefix;
        } catch (Exception ignored) {
            return "";
        }
    }
}
