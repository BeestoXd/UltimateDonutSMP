package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatManager {

    private static final String CHAT_ROOT = "CHAT";
    private static final String LEGACY_FORMAT_ENABLED_PATH = "SETTINGS.CHAT-FORMAT";
    private static final String LEGACY_FORMAT_PATH = "CHAT-FORMAT.CHAT";
    private static final String LEGACY_HOVER_ENABLED_PATH = "CHAT-FORMAT.HOVER-STATS.ENABLED";
    private static final String LEGACY_HOVER_LINES_PATH = "CHAT-FORMAT.HOVER-STATS.LINES";
    private static final String LEGACY_HOVER_LORE_PATH = "CHAT-FORMAT.HOVER-LORE";
    private static final String LEGACY_CLICK_RUN_PATH = "CHAT-FORMAT.HOVER-STATS.CLICK-RUN-COMMAND";
    private static final String LEGACY_CLICK_COMMAND_PATH = "CHAT-FORMAT.HOVER-STATS.CLICK-COMMAND";
    private static final String DEFAULT_CHAT_FORMAT = "%player%: %message%";
    private static final Pattern LINK_PATTERN = Pattern.compile(
            "(?i)\\b(?:https?://)?(?:www\\.)?([a-z0-9-]+(?:\\.[a-z0-9-]+)+)(?::\\d+)?(?:/\\S*)?"
    );

    private final UltimateDonutSmp plugin;
    private final Map<UUID, Long> nextAllowedGlobalChatAtByPlayer = new ConcurrentHashMap<>();
    private final Map<UUID, String> lastAcceptedGlobalMessageByPlayer = new ConcurrentHashMap<>();

    public ChatManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public boolean isFormatEnabled() {
        FileConfiguration config = config();
        if (config.contains(CHAT_ROOT + ".FORMAT-ENABLED")) {
            return config.getBoolean(CHAT_ROOT + ".FORMAT-ENABLED", true);
        }
        return config.getBoolean(LEGACY_FORMAT_ENABLED_PATH, true);
    }

    public String getChatFormat() {
        return config().getString(CHAT_ROOT + ".FORMAT",
                config().getString(LEGACY_FORMAT_PATH, DEFAULT_CHAT_FORMAT));
    }

    public boolean isClickableNameEnabled() {
        FileConfiguration config = config();
        if (config.contains(CHAT_ROOT + ".CLICKABLE-NAME.ENABLED")) {
            return config.getBoolean(CHAT_ROOT + ".CLICKABLE-NAME.ENABLED", true);
        }
        if (config.contains(LEGACY_HOVER_ENABLED_PATH)) {
            return config.getBoolean(LEGACY_HOVER_ENABLED_PATH);
        }
        return !config.getStringList(LEGACY_HOVER_LORE_PATH).isEmpty();
    }

    public List<String> getClickableHoverText() {
        FileConfiguration config = config();
        List<String> lines = config.getStringList(CHAT_ROOT + ".CLICKABLE-NAME.HOVER-TEXT");
        if (!lines.isEmpty()) {
            return lines;
        }

        lines = config.getStringList(LEGACY_HOVER_LINES_PATH);
        if (!lines.isEmpty()) {
            return lines;
        }

        return config.getStringList(LEGACY_HOVER_LORE_PATH);
    }

    public ClickAction getClickableNameAction(Player player) {
        FileConfiguration config = config();

        String suggestCommand = config.getString(CHAT_ROOT + ".CLICKABLE-NAME.SUGGEST-COMMAND", "").trim();
        if (!suggestCommand.isEmpty()) {
            return new ClickAction(ClickActionType.SUGGEST_COMMAND, resolveCommandTemplate(suggestCommand, player));
        }

        if (config.getBoolean(LEGACY_CLICK_RUN_PATH, true)) {
            String command = config.getString(LEGACY_CLICK_COMMAND_PATH, "/stats %player%");
            if (command != null && !command.isBlank()) {
                return new ClickAction(ClickActionType.RUN_COMMAND, resolveCommandTemplate(command, player));
            }
        }

        return null;
    }

    public String resolveMessageColor(Player player) {
        ConfigurationSection section = config().getConfigurationSection(CHAT_ROOT + ".MESSAGE-COLORS");
        if (section == null) {
            return "&f";
        }

        String fallback = section.getString("default", "&f");
        String primaryGroup = resolvePrimaryGroup(player);
        if (primaryGroup == null || primaryGroup.isBlank()) {
            return fallback;
        }

        for (String key : section.getKeys(false)) {
            if (key.equalsIgnoreCase(primaryGroup)) {
                return section.getString(key, fallback);
            }
        }
        return fallback;
    }

    public boolean isGlobalChatMuted() {
        return config().getBoolean(CHAT_ROOT + ".GLOBAL-CHAT-MUTED", false);
    }

    public void setGlobalChatMuted(boolean muted, boolean persist) {
        config().set(CHAT_ROOT + ".GLOBAL-CHAT-MUTED", muted);
        if (persist) {
            plugin.saveConfig();
        }
    }

    public boolean isGlobalDelayEnabled() {
        return config().getBoolean(CHAT_ROOT + ".GLOBAL-CHAT-DELAY-ENABLED", false)
                && getGlobalDelaySeconds() > 0;
    }

    public int getGlobalDelaySeconds() {
        return Math.max(0, config().getInt(CHAT_ROOT + ".GLOBAL-CHAT-DELAY", 0));
    }

    public int getMaxDelaySeconds() {
        return Math.max(0, config().getInt(CHAT_ROOT + ".MAX-DELAY-SECONDS", 30));
    }

    public void setGlobalDelay(int seconds, boolean enabled, boolean persist) {
        int safeSeconds = Math.max(0, seconds);
        boolean safeEnabled = enabled && safeSeconds > 0;

        config().set(CHAT_ROOT + ".GLOBAL-CHAT-DELAY", safeSeconds);
        config().set(CHAT_ROOT + ".GLOBAL-CHAT-DELAY-ENABLED", safeEnabled);

        if (!safeEnabled) {
            nextAllowedGlobalChatAtByPlayer.clear();
        }

        if (persist) {
            plugin.saveConfig();
        }
    }

    public boolean isMuteBypassed(Player player) {
        return player != null && player.hasPermission("ultimatedonutsmp.staff.chat.bypass.mute");
    }

    public boolean isDelayBypassed(Player player) {
        return player != null && player.hasPermission("ultimatedonutsmp.staff.chat.bypass.delay");
    }

    public boolean isFilterBypassed(Player player) {
        return player != null && player.hasPermission("ultimatedonutsmp.staff.chat.bypass.filter");
    }

    public FilterResult validateGlobalMessage(Player player, String rawMessage) {
        String trimmed = rawMessage == null ? "" : rawMessage.trim();
        String normalized = normalizeMessage(trimmed);

        if (player == null || !isFilterEnabled() || isFilterBypassed(player)) {
            return FilterResult.allowed(normalized);
        }

        if (isMinLengthEnabled()) {
            int min = getMinLength();
            if (trimmed.length() < min) {
                return FilterResult.blocked(message(CHAT_ROOT + ".FILTER.LENGTH.MIN.BLOCK-MESSAGE",
                        "&cYour message is too short! (Min: %min%)")
                        .replace("%min%", String.valueOf(min)));
            }
        }

        if (isMaxLengthEnabled()) {
            int max = getMaxLength();
            if (trimmed.length() > max) {
                return FilterResult.blocked(message(CHAT_ROOT + ".FILTER.LENGTH.MAX.BLOCK-MESSAGE",
                        "&cYour message is too long! (Max: %max%)")
                        .replace("%max%", String.valueOf(max)));
            }
        }

        if (isLanguageFilterEnabled() && !containsOnlyAllowedCharacters(trimmed)) {
            return FilterResult.blocked(message(CHAT_ROOT + ".FILTER.LANGUAGE.BLOCK-MESSAGE",
                    "&cYour message contains characters that are not allowed on this server."));
        }

        if (containsBlockedWord(trimmed)) {
            return FilterResult.blocked(message(CHAT_ROOT + ".FILTER.BLOCK-MESSAGE",
                    "&7Please avoid using inappropriate words."));
        }

        if (isCapsFilterTriggered(trimmed)) {
            return FilterResult.blocked(message(CHAT_ROOT + ".FILTER.CAPS.BLOCK-MESSAGE",
                    "&cPlease avoid using too many capital letters."));
        }

        if (containsDisallowedLink(trimmed)) {
            return FilterResult.blocked(message(CHAT_ROOT + ".FILTER.ANTI-LINK.BLOCK-MESSAGE",
                    "&cLinks are not allowed in the chat!"));
        }

        if (isAntiRepeatEnabled() && player != null) {
            String previous = lastAcceptedGlobalMessageByPlayer.get(player.getUniqueId());
            if (previous != null && previous.equals(normalized)) {
                return FilterResult.blocked(message(CHAT_ROOT + ".FILTER.ANTI-REPEAT.BLOCK-MESSAGE",
                        "&cYou cannot repeat the same message!"));
            }
        }

        return FilterResult.allowed(normalized);
    }

    public DelayResult checkAndTrackDelay(Player player) {
        if (player == null || !isGlobalDelayEnabled() || isDelayBypassed(player)) {
            return DelayResult.pass();
        }

        long now = System.currentTimeMillis();
        Long nextAllowed = nextAllowedGlobalChatAtByPlayer.get(player.getUniqueId());
        if (nextAllowed != null && nextAllowed > now) {
            long remainingSeconds = Math.max(1L, (nextAllowed - now + 999L) / 1000L);
            return DelayResult.blocked(remainingSeconds);
        }
        return DelayResult.pass();
    }

    public void trackAcceptedGlobalMessage(Player player, String rawMessage) {
        if (player == null) {
            return;
        }

        String normalized = normalizeMessage(rawMessage);
        if (!normalized.isBlank()) {
            lastAcceptedGlobalMessageByPlayer.put(player.getUniqueId(), normalized);
        }

        if (!isGlobalDelayEnabled() || isDelayBypassed(player)) {
            return;
        }

        long nextAllowed = System.currentTimeMillis() + (getGlobalDelaySeconds() * 1000L);
        nextAllowedGlobalChatAtByPlayer.put(player.getUniqueId(), nextAllowed);
    }

    public void clearChatForAllPlayers() {
        int clearLines = Math.max(1, config().getInt(CHAT_ROOT + ".CLEAR-LINES", 150));
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (int i = 0; i < clearLines; i++) {
                player.sendMessage(Component.empty());
            }
        }
    }

    public void clearPlayerState(UUID uuid) {
        if (uuid == null) {
            return;
        }
        nextAllowedGlobalChatAtByPlayer.remove(uuid);
        lastAcceptedGlobalMessageByPlayer.remove(uuid);
    }

    public String message(String path, String fallback) {
        return config().getString(path, fallback);
    }

    private FileConfiguration config() {
        return plugin.getConfigManager().getConfig();
    }

    private boolean isFilterEnabled() {
        return config().getBoolean(CHAT_ROOT + ".FILTER.ENABLED", false);
    }

    private boolean isMinLengthEnabled() {
        return config().getBoolean(CHAT_ROOT + ".FILTER.LENGTH.MIN.ENABLED", false);
    }

    private int getMinLength() {
        return Math.max(0, config().getInt(CHAT_ROOT + ".FILTER.LENGTH.MIN.VALUE", 1));
    }

    private boolean isMaxLengthEnabled() {
        return config().getBoolean(CHAT_ROOT + ".FILTER.LENGTH.MAX.ENABLED", false);
    }

    private int getMaxLength() {
        return Math.max(0, config().getInt(CHAT_ROOT + ".FILTER.LENGTH.MAX.VALUE", 100));
    }

    private boolean isLanguageFilterEnabled() {
        return config().getBoolean(CHAT_ROOT + ".FILTER.LANGUAGE.ENABLED", false);
    }

    private boolean containsBlockedWord(String message) {
        if (message == null || message.isBlank()) {
            return false;
        }

        String normalized = message.toLowerCase(Locale.ROOT);
        for (String blockedWord : config().getStringList(CHAT_ROOT + ".FILTER.WORDS")) {
            if (blockedWord != null && !blockedWord.isBlank()
                    && normalized.contains(blockedWord.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private boolean isCapsFilterTriggered(String message) {
        if (!config().getBoolean(CHAT_ROOT + ".FILTER.CAPS.ENABLED", false)) {
            return false;
        }

        String trimmed = message == null ? "" : message.trim();
        int minLength = Math.max(0, config().getInt(CHAT_ROOT + ".FILTER.CAPS.MIN-LENGTH", 5));
        if (trimmed.length() < minLength) {
            return false;
        }

        int letters = 0;
        int uppercaseLetters = 0;
        for (int i = 0; i < trimmed.length(); i++) {
            char character = trimmed.charAt(i);
            if (!Character.isLetter(character)) {
                continue;
            }

            letters++;
            if (Character.isUpperCase(character)) {
                uppercaseLetters++;
            }
        }

        if (letters == 0) {
            return false;
        }

        double uppercasePercentage = (uppercaseLetters * 100.0D) / letters;
        return uppercasePercentage >= config().getDouble(CHAT_ROOT + ".FILTER.CAPS.PERCENTAGE", 70.0D);
    }

    private boolean isAntiRepeatEnabled() {
        return config().getBoolean(CHAT_ROOT + ".FILTER.ANTI-REPEAT.ENABLED", false);
    }

    private boolean containsDisallowedLink(String message) {
        if (!config().getBoolean(CHAT_ROOT + ".FILTER.ANTI-LINK.ENABLED", false)) {
            return false;
        }

        Matcher matcher = LINK_PATTERN.matcher(message == null ? "" : message);
        while (matcher.find()) {
            String host = normalizeHost(matcher.group(1));
            if (host != null && !isAllowedLinkHost(host)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAllowedLinkHost(String host) {
        List<String> allowedHosts = config().getStringList(CHAT_ROOT + ".FILTER.ANTI-LINK.ALLOWED");
        if (allowedHosts.isEmpty()) {
            return false;
        }

        for (String allowedHost : allowedHosts) {
            String normalizedAllowed = normalizeHost(allowedHost);
            if (normalizedAllowed == null) {
                continue;
            }
            if (host.equals(normalizedAllowed) || host.endsWith("." + normalizedAllowed)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsOnlyAllowedCharacters(String message) {
        Set<AllowedAlphabet> allowedAlphabets = getAllowedAlphabets();
        if (allowedAlphabets.isEmpty()) {
            return true;
        }

        for (int offset = 0; offset < message.length(); ) {
            int codePoint = message.codePointAt(offset);
            if (!isAllowedCodePoint(codePoint, allowedAlphabets)) {
                return false;
            }
            offset += Character.charCount(codePoint);
        }
        return true;
    }

    private Set<AllowedAlphabet> getAllowedAlphabets() {
        List<String> configured = config().getStringList(CHAT_ROOT + ".FILTER.LANGUAGE.ALLOWED-ALPHABETS");
        if (configured.isEmpty()) {
            return Collections.emptySet();
        }

        EnumSet<AllowedAlphabet> allowed = EnumSet.noneOf(AllowedAlphabet.class);
        for (String value : configured) {
            try {
                allowed.add(AllowedAlphabet.valueOf(value.toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return allowed;
    }

    private boolean isAllowedCodePoint(int codePoint, Set<AllowedAlphabet> allowedAlphabets) {
        if (Character.isWhitespace(codePoint)) {
            return allowedAlphabets.contains(AllowedAlphabet.SYMBOLS);
        }

        if (allowedAlphabets.contains(AllowedAlphabet.LATIN)
                && Character.isLetter(codePoint)
                && Character.UnicodeScript.of(codePoint) == Character.UnicodeScript.LATIN) {
            return true;
        }

        if (allowedAlphabets.contains(AllowedAlphabet.NUMBERS) && Character.isDigit(codePoint)) {
            return true;
        }

        return allowedAlphabets.contains(AllowedAlphabet.SYMBOLS) && isSupportedSymbol(codePoint);
    }

    private boolean isSupportedSymbol(int codePoint) {
        if (codePoint <= 0x7E && !Character.isLetterOrDigit(codePoint)) {
            return true;
        }

        return switch (Character.getType(codePoint)) {
            case Character.CONNECTOR_PUNCTUATION,
                    Character.DASH_PUNCTUATION,
                    Character.START_PUNCTUATION,
                    Character.END_PUNCTUATION,
                    Character.INITIAL_QUOTE_PUNCTUATION,
                    Character.FINAL_QUOTE_PUNCTUATION,
                    Character.OTHER_PUNCTUATION,
                    Character.MATH_SYMBOL,
                    Character.CURRENCY_SYMBOL,
                    Character.MODIFIER_SYMBOL -> codePoint <= 0x2BFF;
            default -> false;
        };
    }

    private String resolvePrimaryGroup(Player player) {
        if (player == null || !ColorUtils.hasPAPI()) {
            return null;
        }

        try {
            String group = me.clip.placeholderapi.PlaceholderAPI
                    .setPlaceholders(player, "%luckperms_primary_group%");
            if (group == null || group.isBlank() || group.startsWith("%")) {
                return null;
            }
            return group.trim();
        } catch (Exception ignored) {
            return null;
        }
    }

    private String resolveCommandTemplate(String template, Player player) {
        if (template == null || template.isBlank() || player == null) {
            return template == null ? "" : template;
        }

        return template
                .replace("<player>", player.getName())
                .replace("%player%", player.getName())
                .replace("%uuid%", player.getUniqueId().toString());
    }

    private String normalizeMessage(String message) {
        if (message == null) {
            return "";
        }
        return message.trim()
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);
    }

    private String normalizeHost(String host) {
        if (host == null || host.isBlank()) {
            return null;
        }

        String normalized = host.toLowerCase(Locale.ROOT).trim();
        if (normalized.startsWith("http://")) {
            normalized = normalized.substring("http://".length());
        } else if (normalized.startsWith("https://")) {
            normalized = normalized.substring("https://".length());
        }

        if (normalized.startsWith("www.")) {
            normalized = normalized.substring(4);
        }

        int slashIndex = normalized.indexOf('/');
        if (slashIndex >= 0) {
            normalized = normalized.substring(0, slashIndex);
        }

        int colonIndex = normalized.indexOf(':');
        if (colonIndex >= 0) {
            normalized = normalized.substring(0, colonIndex);
        }

        return normalized;
    }

    private enum AllowedAlphabet {
        LATIN,
        NUMBERS,
        SYMBOLS
    }

    public enum ClickActionType {
        RUN_COMMAND,
        SUGGEST_COMMAND
    }

    public record ClickAction(ClickActionType type, String command) {
    }

    public record FilterResult(boolean allowed, String blockMessage, String normalizedMessage) {
        public static FilterResult allowed(String normalizedMessage) {
            return new FilterResult(true, null, normalizedMessage);
        }

        public static FilterResult blocked(String blockMessage) {
            return new FilterResult(false, blockMessage, null);
        }
    }

    public record DelayResult(boolean allowed, long remainingSeconds) {
        public static DelayResult pass() {
            return new DelayResult(true, 0L);
        }

        public static DelayResult blocked(long remainingSeconds) {
            return new DelayResult(false, remainingSeconds);
        }
    }
}
