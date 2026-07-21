package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.models.Team;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HoverStatsManager {

    private static final String DEFAULT_CHAT_FORMAT = "&f%prefix%%player%&7: &f%message%";

    private final UltimateDonutSmp plugin;

    public HoverStatsManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public boolean isEnabled() {
        return plugin.getChatManager().isClickableNameEnabled();
    }

    public BaseComponent[] buildChatComponent(Player speaker, String prefix, String rawMessage, String chatFormat) {
        String format = (chatFormat == null || chatFormat.isBlank()) ? DEFAULT_CHAT_FORMAT : chatFormat;
        format = format.replace("%nick%", "%player%").replace("<nick>", "%player%");

        String displayName = resolveDisplayName(speaker);
        String resolvedFormat = format.replace("%prefix%", prefix == null ? "" : prefix);
        int playerIndex = resolvedFormat.indexOf("%player%");
        int messageIndex = resolvedFormat.indexOf("%message%");

        if (playerIndex < 0 || messageIndex < 0 || playerIndex > messageIndex) {
            String fallback = resolvedFormat
                    .replace("%player%", displayName)
                    .replace("%message%", rawMessage == null ? "" : rawMessage);
            return ColorUtils.toBaseComponents(fallback, speaker);
        }

        String beforePlayer = resolvedFormat.substring(0, playerIndex);
        String betweenPlayerAndMessage = resolvedFormat.substring(playerIndex + "%player%".length(), messageIndex);
        String afterMessage = resolvedFormat.substring(messageIndex + "%message%".length())
                .replace("%player%", displayName);

        TextComponent root = new TextComponent();
        HoverEvent hover = isEnabled() ? buildHover(speaker, prefix) : null;
        ClickEvent click = isEnabled() ? buildClick(speaker) : null;

        if (isEnabled() && "SENDER".equalsIgnoreCase(resolveApplyTo())) {
            TextComponent senderComponent = ColorUtils.toBaseComponent(beforePlayer + displayName, speaker);
            applyEvents(senderComponent, hover, click);
            root.addExtra(senderComponent);
        } else {
            append(root, ColorUtils.toBaseComponents(beforePlayer, speaker));
            TextComponent nameComponent = ColorUtils.toBaseComponent(displayName, speaker);
            if (isEnabled()) {
                applyEvents(nameComponent, hover, click);
            }
            root.addExtra(nameComponent);
        }

        append(root, ColorUtils.toBaseComponents(betweenPlayerAndMessage, speaker));
        append(root, buildMessageComponent(speaker, rawMessage));
        append(root, ColorUtils.toBaseComponents(afterMessage, speaker));
        return new BaseComponent[]{root};
    }

    public HoverEvent buildHover(Player speaker, String prefix) {
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
            hoverText.append(replacePlaceholders(line, placeholders));
        }
        return new HoverEvent(HoverEvent.Action.SHOW_TEXT, ColorUtils.toBaseComponents(hoverText.toString(), speaker));
    }

    public ClickEvent buildClick(Player speaker) {
        ChatManager.ClickAction clickAction = plugin.getChatManager().getClickableNameAction(speaker);
        if (clickAction == null || clickAction.command() == null || clickAction.command().isBlank()) {
            return null;
        }
        return clickAction.type() == ChatManager.ClickActionType.SUGGEST_COMMAND
                ? new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, clickAction.command())
                : new ClickEvent(ClickEvent.Action.RUN_COMMAND, clickAction.command());
    }

    private List<String> getHoverLines() {
        List<String> lines = plugin.getChatManager().getClickableHoverText();
        if (!lines.isEmpty()) {
            return lines;
        }

        return List.of(
                "%prefix%%player%",
                "&7&m----------",
                "{money_color}{money_name_plural}: &f%money_formatted%",
                "&cᴋɪʟʟѕ: &f%kills%",
                "&eᴘʟᴀʏᴛɪᴍᴇ: &f%playtime%",
                "&6ᴅᴇᴀᴛʜѕ: &f%deaths%",
                "{shards_color}{shards_name_plural}: &f%shards_formatted%",
                "&7&m----------",
                "&7ᴄʟɪᴄᴋ ᴛᴏ ᴠɪᴇᴡ ѕᴛᴀᴛѕ"
        );
    }

    private Map<String, String> buildPlaceholders(Player speaker, String prefix, PlayerData data) {
        Map<String, String> placeholders = new LinkedHashMap<>();
        Team team = plugin.getTeamManager().getTeam(speaker.getUniqueId());
        String teamName = team != null ? team.getName().toUpperCase() : "none";
        String displayName = resolveDisplayName(speaker);

        placeholders.put("%player%", displayName);
        placeholders.put("%nick%", displayName);
        placeholders.put("%real_player%", speaker.getName());
        placeholders.put("%realname%", speaker.getName());
        placeholders.put("%prefix%", prefix == null ? "" : prefix);
        placeholders.put("%luckperms_prefix%", prefix == null ? "" : prefix);
        placeholders.put("%team%", teamName);
        double money = data != null ? data.getMoney() : 0D;
        long shards = data != null ? data.getShards() : 0L;
        placeholders.put("%money%", plugin.getCurrencyManager().formatCompactAmount(CurrencyManager.CurrencyType.MONEY, money));
        placeholders.put("%money_raw%", data != null ? NumberUtils.format(data.getMoney()) : "0");
        placeholders.put("%money_formatted%", plugin.getCurrencyManager().formatMoneyCompact(money));
        placeholders.put("{money_color}", plugin.getCurrencyManager().color(CurrencyManager.CurrencyType.MONEY));
        placeholders.put("{money_symbol}", plugin.getCurrencyManager().symbol(CurrencyManager.CurrencyType.MONEY));
        placeholders.put("{money_name}", plugin.getCurrencyManager().name(CurrencyManager.CurrencyType.MONEY, money));
        placeholders.put("{money_name_plural}", plugin.getCurrencyManager().plural(CurrencyManager.CurrencyType.MONEY));
        placeholders.put("%kills%", String.valueOf(data != null ? data.getKills() : 0));
        placeholders.put("%deaths%", String.valueOf(data != null ? data.getDeaths() : 0));
        placeholders.put("%playtime%", data != null ? NumberUtils.formatTimeLong(data.getTotalPlaytimeSeconds()) : "0s");
        placeholders.put("%shards%", String.valueOf(shards));
        placeholders.put("%shards_formatted%", plugin.getCurrencyManager().formatShards(shards));
        placeholders.put("{shards_color}", plugin.getCurrencyManager().color(CurrencyManager.CurrencyType.SHARDS));
        placeholders.put("{shards_symbol}", plugin.getCurrencyManager().symbol(CurrencyManager.CurrencyType.SHARDS));
        placeholders.put("{shards_name}", plugin.getCurrencyManager().name(CurrencyManager.CurrencyType.SHARDS, shards));
        placeholders.put("{shards_name_plural}", plugin.getCurrencyManager().plural(CurrencyManager.CurrencyType.SHARDS));
        placeholders.put("%blocks_placed%", String.valueOf(data != null ? data.getBlocksPlaced() : 0));
        placeholders.put("%blocks_broken%", String.valueOf(data != null ? data.getBlocksBroken() : 0));
        placeholders.put("%mobs_killed%", String.valueOf(data != null ? data.getMobsKilled() : 0));
        placeholders.put("%killstreak%", String.valueOf(data != null ? data.getKillStreak() : 0));
        placeholders.put("%highest_killstreak%", String.valueOf(data != null ? data.getHighestKillStreak() : 0));
        placeholders.put("%money_made%", plugin.getCurrencyManager().formatCompactAmount(
                CurrencyManager.CurrencyType.MONEY,
                data != null ? data.getMoneyMade() : 0D
        ));
        placeholders.put("%money_made_formatted%", plugin.getCurrencyManager().formatMoneyCompact(data != null ? data.getMoneyMade() : 0D));
        placeholders.put("%money_spent%", plugin.getCurrencyManager().formatCompactAmount(
                CurrencyManager.CurrencyType.MONEY,
                data != null ? data.getMoneySpent() : 0D
        ));
        placeholders.put("%money_spent_formatted%", plugin.getCurrencyManager().formatMoneyCompact(data != null ? data.getMoneySpent() : 0D));
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
        if (plugin.getHideManager() != null && plugin.getHideManager().isHidden(player.getUniqueId())) {
            return plugin.getHideManager().publicName(player);
        }
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

    private void applyEvents(TextComponent component, HoverEvent hover, ClickEvent click) {
        if (hover != null) {
            component.setHoverEvent(hover);
        }
        if (click != null) {
            component.setClickEvent(click);
        }
    }

    private String resolveApplyTo() {
        FileConfiguration config = plugin.getConfigManager().getConfig();
        if (config.contains("CHAT.CLICKABLE-NAME.ENABLED")) {
            return "NAME";
        }
        return config.getString("CHAT-FORMAT.HOVER-STATS.APPLY-TO", "NAME");
    }

    private BaseComponent[] buildMessageComponent(Player speaker, String rawMessage) {
        String messageColor = plugin.getChatManager().resolveMessageColor(speaker);
        String prefix = messageColor == null || messageColor.isBlank() ? "&f" : messageColor;
        return TextComponent.fromLegacyText(ColorUtils.colorize(prefix) + (rawMessage == null ? "" : rawMessage));
    }

    private String resolvePrefix(Player player, String fallbackPrefix) {
        if (fallbackPrefix != null && !fallbackPrefix.isBlank()) {
            return fallbackPrefix;
        }
        if (ColorUtils.hasPAPI()) {
            try {
                String prefix = me.clip.placeholderapi.PlaceholderAPI
                        .setPlaceholders(player, "%luckperms_prefix%");
                if (prefix != null && !prefix.isBlank() && !prefix.startsWith("%")) {
                    return prefix;
                }
                prefix = me.clip.placeholderapi.PlaceholderAPI
                        .setPlaceholders(player, "%vault_prefix%");
                if (prefix != null && !prefix.isBlank() && !prefix.startsWith("%")) {
                    return prefix;
                }
                prefix = me.clip.placeholderapi.PlaceholderAPI
                        .setPlaceholders(player, "%prefix%");
                if (prefix != null && !prefix.isBlank() && !prefix.startsWith("%")) {
                    return prefix;
                }
            } catch (Exception ignored) {
            }
        }
        if (org.bukkit.Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            try {
                org.bukkit.plugin.RegisteredServiceProvider<net.milkbowl.vault.chat.Chat> rsp =
                        org.bukkit.Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
                if (rsp != null && rsp.getProvider() != null) {
                    String prefix = rsp.getProvider().getPlayerPrefix(player);
                    if (prefix != null && !prefix.isBlank()) {
                        return prefix;
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return "";
    }

    private void append(TextComponent root, BaseComponent[] components) {
        for (BaseComponent component : components) {
            root.addExtra(component);
        }
    }
}
