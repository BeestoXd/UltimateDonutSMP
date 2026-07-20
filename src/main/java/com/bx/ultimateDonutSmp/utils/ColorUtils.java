package com.bx.ultimateDonutSmp.utils;

import com.bx.ultimateDonutSmp.managers.LanguageManager;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtils {

    private static final char SECTION_CHAR = '\u00A7';
    private static final Pattern HEX_PATTERN = Pattern.compile("(?:&#|\\{#|&x#|<#|#)([A-Fa-f0-9]{6})\\}?>?");
    private static final Pattern TAGGED_GRADIENT_PATTERN = Pattern.compile(
            "<#([A-Fa-f0-9]{6})>(.*?)</#([A-Fa-f0-9]{6})>",
            Pattern.DOTALL
    );
    private static final Pattern TAGGED_HEX_PATTERN = Pattern.compile("<#([A-Fa-f0-9]{6})>");
    private static final Pattern TAGGED_HEX_CLOSE_PATTERN = Pattern.compile("</#([A-Fa-f0-9]{6})>");

    private static boolean hasPAPI = false;

    public static void init() {
        hasPAPI = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
    }

    public static String translateHex(String text) {
        if (text == null) {
            return "";
        }

        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuilder builder = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(builder, Matcher.quoteReplacement(toLegacyHex(matcher.group(1))));
        }
        matcher.appendTail(builder);
        return builder.toString();
    }

    public static String colorize(String text) {
        if (text == null) {
            return "";
        }

        Player target = PlayerContext.get();
        if (target != null) {
            return colorize(text, target);
        }

        String result = LanguageManager.translateBuiltInText(text);
        if (hasPAPI) {
            try {
                result = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders((Player) null, result);
            } catch (Exception ignored) {
            }
        }
        return applyColors(result);
    }

    private static String applyColors(String text) {
        String result = normalizeText(text);
        result = translateTaggedGradients(result);
        result = translateTaggedHex(result);
        return translateHex(result).replace('&', SECTION_CHAR);
    }

    public static String colorize(String text, Player player) {
        if (text == null) {
            return "";
        }

        String result = LanguageManager.translateBuiltInText(text);
        if (hasPAPI && player != null) {
            try {
                result = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, result);
            } catch (Exception ignored) {
            }
        }
        return applyColors(result);
    }

    public static String colorizeOffline(String text, OfflinePlayer player) {
        if (text == null) {
            return "";
        }

        String result = LanguageManager.translateBuiltInText(text);
        if (hasPAPI && player != null) {
            try {
                result = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, result);
            } catch (Exception ignored) {
            }
        }
        return applyColors(result);
    }

    public static String toComponent(String text) {
        return colorize(text);
    }

    public static String toComponent(String text, Player player) {
        return colorize(text, player);
    }

    public static String toLegacyString(String component) {
        return component == null ? "" : component;
    }

    public static List<String> toComponentList(List<String> lines) {
        return colorizeList(lines);
    }

    public static BaseComponent[] toBaseComponents(String text) {
        return TextComponent.fromLegacyText(colorize(text));
    }

    public static BaseComponent[] toBaseComponents(String text, Player player) {
        return TextComponent.fromLegacyText(colorize(text, player));
    }

    public static TextComponent toBaseComponent(String text) {
        TextComponent component = new TextComponent();
        for (BaseComponent part : toBaseComponents(text)) {
            component.addExtra(part);
        }
        return component;
    }

    public static TextComponent toBaseComponent(String text, Player player) {
        TextComponent component = new TextComponent();
        for (BaseComponent part : toBaseComponents(text, player)) {
            component.addExtra(part);
        }
        return component;
    }

    public static List<String> toComponentList(List<String> lines, Player player) {
        List<String> list = new ArrayList<>();
        for (String line : lines) {
            list.add(toComponent(line, player));
        }
        return list;
    }

    public static List<String> colorizeList(List<String> lines) {
        List<String> list = new ArrayList<>();
        for (String line : lines) {
            list.add(colorize(line));
        }
        return list;
    }

    public static List<String> colorizeList(List<String> lines, Player player) {
        List<String> list = new ArrayList<>();
        for (String line : lines) {
            list.add(colorize(line, player));
        }
        return list;
    }

    public static String strip(String text) {
        if (text == null) {
            return "";
        }

        return text.replaceAll("&#[A-Fa-f0-9]{6}", "")
                .replaceAll("\\{#[A-Fa-f0-9]{6}\\}", "")
                .replaceAll("<#?[A-Fa-f0-9]{6}>", "")
                .replaceAll("</#?[A-Fa-f0-9]{6}>", "")
                .replaceAll("&x#[A-Fa-f0-9]{6}", "")
                .replaceAll("#[A-Fa-f0-9]{6}", "")
                .replaceAll("(?i)\\u00A7x(?:\\u00A7[0-9A-F]){6}", "")
                .replaceAll("[\\u00A7&][0-9A-FK-ORa-fk-or]", "");
    }

    public static boolean hasPAPI() {
        return hasPAPI;
    }

    private static String translateTaggedGradients(String text) {
        Matcher matcher = TAGGED_GRADIENT_PATTERN.matcher(text);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String replacement = applyGradient(matcher.group(2), matcher.group(1), matcher.group(3));
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private static String translateTaggedHex(String text) {
        Matcher openMatcher = TAGGED_HEX_PATTERN.matcher(text);
        StringBuffer buffer = new StringBuffer();
        while (openMatcher.find()) {
            openMatcher.appendReplacement(buffer, Matcher.quoteReplacement("&#" + openMatcher.group(1)));
        }
        openMatcher.appendTail(buffer);

        return TAGGED_HEX_CLOSE_PATTERN.matcher(buffer.toString()).replaceAll("&r");
    }

    private static String applyGradient(String text, String startHex, String endHex) {
        int visibleCharacters = countVisibleCharacters(text);
        if (visibleCharacters <= 0) {
            return text;
        }

        int startRed = Integer.parseInt(startHex.substring(0, 2), 16);
        int startGreen = Integer.parseInt(startHex.substring(2, 4), 16);
        int startBlue = Integer.parseInt(startHex.substring(4, 6), 16);
        int endRed = Integer.parseInt(endHex.substring(0, 2), 16);
        int endGreen = Integer.parseInt(endHex.substring(2, 4), 16);
        int endBlue = Integer.parseInt(endHex.substring(4, 6), 16);

        StringBuilder output = new StringBuilder();
        String activeFormats = "";
        int visibleIndex = 0;

        for (int i = 0; i < text.length(); i++) {
            char current = text.charAt(i);
            if ((current == '&' || current == SECTION_CHAR) && i + 1 < text.length()) {
                char code = Character.toLowerCase(text.charAt(++i));
                if (code == 'r' || "0123456789abcdef".indexOf(code) >= 0) {
                    activeFormats = "";
                } else if (isFormatCode(code)) {
                    activeFormats = addFormatCode(activeFormats, code);
                }
                continue;
            }

            double ratio = visibleCharacters == 1 ? 0.0D : (double) visibleIndex / (visibleCharacters - 1);
            int red = interpolate(startRed, endRed, ratio);
            int green = interpolate(startGreen, endGreen, ratio);
            int blue = interpolate(startBlue, endBlue, ratio);

            output.append(toLegacyHex(String.format("%02X%02X%02X", red, green, blue)));
            output.append(activeFormats);
            output.append(current);
            visibleIndex++;
        }

        return output.toString();
    }

    private static int interpolate(int start, int end, double ratio) {
        return (int) Math.round(start + (end - start) * ratio);
    }

    private static int countVisibleCharacters(String text) {
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            char current = text.charAt(i);
            if ((current == '&' || current == SECTION_CHAR) && i + 1 < text.length()) {
                i++;
                continue;
            }
            count++;
        }
        return count;
    }

    private static boolean isFormatCode(char code) {
        return code == 'k' || code == 'l' || code == 'm' || code == 'n' || code == 'o';
    }

    private static String addFormatCode(String activeFormats, char code) {
        String marker = String.valueOf(SECTION_CHAR) + code;
        return activeFormats.contains(marker) ? activeFormats : activeFormats + marker;
    }

    private static String toLegacyHex(String hex) {
        return String.valueOf(SECTION_CHAR) + "x"
                + SECTION_CHAR + String.valueOf(hex.charAt(0))
                + SECTION_CHAR + String.valueOf(hex.charAt(1))
                + SECTION_CHAR + String.valueOf(hex.charAt(2))
                + SECTION_CHAR + String.valueOf(hex.charAt(3))
                + SECTION_CHAR + String.valueOf(hex.charAt(4))
                + SECTION_CHAR + String.valueOf(hex.charAt(5));
    }

    private static final Pattern UNICODE_ESCAPE_PATTERN = Pattern.compile("\\\\u([0-9A-Fa-f]{4})");
    private static final java.util.Map<Character, Character> SMALL_CAPS_MAP = new java.util.HashMap<>();
    static {
        String smallCaps = "ᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴘǫʀѕᴛᴜᴠᴡxʏᴢ";
        String normalCaps = "abcdefghijklmnopqrstuvwxyz";
        for (int i = 0; i < smallCaps.length(); i++) {
            SMALL_CAPS_MAP.put(smallCaps.charAt(i), normalCaps.charAt(i));
        }
        // Add other lookalikes
        SMALL_CAPS_MAP.put('ѕ', 's'); // Cyrillic s
        SMALL_CAPS_MAP.put('ꜱ', 's'); // Latin small capital s
        SMALL_CAPS_MAP.put('q', 'q');
    }

    private static String decodeUnicodeEscapes(String text) {
        if (text == null || !text.contains("\\u")) {
            return text;
        }
        Matcher matcher = UNICODE_ESCAPE_PATTERN.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            try {
                char ch = (char) Integer.parseInt(matcher.group(1), 16);
                matcher.appendReplacement(sb, Matcher.quoteReplacement(String.valueOf(ch)));
            } catch (NumberFormatException e) {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group(0)));
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static String normalizeText(String text) {
        if (text == null) {
            return "";
        }
        String result = decodeUnicodeEscapes(text);
        // Fix known translation/spelling typos from old configs
        result = result.replace("PEARH", "PEARL").replace("pearh", "pearl").replace("Pearh", "Pearl");
        result = result.replace("DONT+", "DONUT+").replace("dont+", "donut+").replace("Dont+", "Donut+");
        result = result.replace("RANDOMIVED", "RANDOMIZED").replace("randomived", "randomized").replace("Randomived", "Randomized");
        result = result.replace("MDWVST", "MESSAGES").replace("mdwvst", "messages").replace("Mdwvst", "Messages");
        result = result.replace("MWVST", "MESSAGES").replace("mwvst", "messages").replace("Mwvst", "Messages");
        result = result.replace("CURRENTLY", "Currently").replace("currently", "Currently");
        result = result.replace("STATUS", "Status").replace("status", "Status");
        result = result.replace("{Status}", "{status}");
        result = result.replace("{State}", "{state}");
        return result;
    }

    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("&#[A-Fa-f0-9]{6}|\\{#[A-Fa-f0-9]{6}\\}|<#?[A-Fa-f0-9]{6}>|</#?[A-Fa-f0-9]{6}>|&x#[A-Fa-f0-9]{6}|#[A-Fa-f0-9]{6}");
    private static final Pattern LEGACY_COLOR_PATTERN = Pattern.compile("&[0-9a-fk-orA-FK-OR]");
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{.*?\\}|%.*?%");

    public static String stripColorCodesAndPlaceholders(String text) {
        if (text == null) return "";
        String s = HEX_COLOR_PATTERN.matcher(text).replaceAll("");
        s = LEGACY_COLOR_PATTERN.matcher(s).replaceAll("");
        s = PLACEHOLDER_PATTERN.matcher(s).replaceAll("");
        s = s.replaceAll("(?i)\\bcurrently\\b", "");
        s = s.replaceAll("(?i)\\bstatus\\b", "");
        return s;
    }

    public static boolean isAllCaps(String text) {
        boolean hasUppercase = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isLowerCase(c)) {
                return false;
            }
            if (Character.isUpperCase(c)) {
                hasUppercase = true;
            }
        }
        return hasUppercase;
    }

    public static String transformAllCaps(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        String stripped = stripColorCodesAndPlaceholders(text);
        if (!isAllCaps(stripped)) {
            return text;
        }

        StringBuilder result = new StringBuilder();
        int i = 0;
        int len = text.length();

        while (i < len) {
            // Check braced hex color {#ffffff}
            if (text.charAt(i) == '{' && i + 9 <= len && text.charAt(i + 8) == '}') {
                String sub = text.substring(i, i + 9);
                if (sub.matches("\\{#[A-Fa-f0-9]{6}\\}")) {
                    result.append(sub);
                    i += 9;
                    continue;
                }
            }
            // Check ampersand hex color &#ffffff
            if (i + 7 < len && text.charAt(i) == '&' && text.charAt(i + 1) == '#' && isHexColor(text, i + 2)) {
                result.append(text, i, i + 8);
                i += 8;
                continue;
            }
            // Check ampersand x hex color &x#ffffff
            if (i + 8 < len && text.charAt(i) == '&' && text.charAt(i + 1) == 'x' && text.charAt(i + 2) == '#' && isHexColor(text, i + 3)) {
                result.append(text, i, i + 9);
                i += 9;
                continue;
            }
            // Check legacy color
            if (i + 1 < len && (text.charAt(i) == '&' || text.charAt(i) == '\u00A7') && "0123456789abcdefklmnorx".indexOf(Character.toLowerCase(text.charAt(i + 1))) >= 0) {
                result.append(text, i, i + 2);
                i += 2;
                continue;
            }
            // Check tagged hex color
            if (text.charAt(i) == '<' && i + 8 <= len && text.charAt(i + 7) == '>') {
                String sub = text.substring(i, i + 8);
                if (sub.matches("<#[A-Fa-f0-9]{6}>")) {
                    result.append(sub);
                    i += 8;
                    continue;
                }
            }
            // Check tagged close color
            if (text.charAt(i) == '<' && i + 9 <= len && text.charAt(i + 8) == '>') {
                String sub = text.substring(i, i + 9);
                if (sub.matches("</#[A-Fa-f0-9]{6}>")) {
                    result.append(sub);
                    i += 9;
                    continue;
                }
            }
            // Check standalone hex color #ffffff
            if (text.charAt(i) == '#' && isHexColor(text, i + 1)) {
                result.append(text, i, i + 7);
                i += 7;
                continue;
            }
            // Check brace placeholder
            if (text.charAt(i) == '{') {
                int end = text.indexOf('}', i);
                if (end != -1) {
                    result.append(text, i, end + 1);
                    i = end + 1;
                    continue;
                }
            }
            // Check percent placeholder
            if (text.charAt(i) == '%') {
                int end = text.indexOf('%', i + 1);
                if (end != -1) {
                    result.append(text, i, end + 1);
                    i = end + 1;
                    continue;
                }
            }

            // Plain text token
            StringBuilder textToken = new StringBuilder();
            while (i < len) {
                char current = text.charAt(i);
                if (current == '&' || current == '\u00A7' || current == '{' || current == '%' || current == '<' || current == '#') {
                    if (current == '&' || current == '\u00A7') {
                        if (i + 1 < len && "0123456789abcdefklmnorx#".indexOf(Character.toLowerCase(text.charAt(i + 1))) >= 0) {
                            break;
                        }
                    } else if (current == '{') {
                        if (i + 9 <= len && text.substring(i, i + 9).matches("\\{#[A-Fa-f0-9]{6}\\}")) break;
                        if (text.indexOf('}', i) != -1) break;
                    } else if (current == '%') {
                        if (text.indexOf('%', i + 1) != -1) break;
                    } else if (current == '<') {
                        if (i + 8 <= len && text.substring(i, i + 8).matches("<#[A-Fa-f0-9]{6}>")) break;
                        if (i + 9 <= len && text.substring(i, i + 9).matches("</#[A-Fa-f0-9]{6}>")) break;
                    } else if (current == '#') {
                        if (isHexColor(text, i + 1)) break;
                    }
                }
                textToken.append(current);
                i++;
            }

            result.append(toTitleCase(textToken.toString()));
        }
        return result.toString();
    }

    private static String toTitleCase(String text) {
        StringBuilder sb = new StringBuilder();
        boolean capitalizeNext = true;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isLetter(c)) {
                if (capitalizeNext) {
                    sb.append(Character.toUpperCase(c));
                    capitalizeNext = false;
                } else {
                    sb.append(Character.toLowerCase(c));
                }
            } else {
                sb.append(c);
                if (Character.isWhitespace(c) || c == '-' || c == '/' || c == '_') {
                    capitalizeNext = true;
                }
            }
        }
        return sb.toString();
    }

    private static boolean isHexColor(String text, int start) {
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

    public static String toTitleCaseSmart(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        StringBuilder result = new StringBuilder();
        int i = 0;
        int len = text.length();

        while (i < len) {
            // Check hex color &#ffffff
            if (i + 7 < len && text.charAt(i) == '&' && text.charAt(i + 1) == '#' && isHexColor(text, i + 2)) {
                result.append(text, i, i + 8);
                i += 8;
                continue;
            }
            // Check legacy color &c or §c
            if (i + 1 < len && (text.charAt(i) == '&' || text.charAt(i) == '\u00A7') && "0123456789abcdefklmnorx".indexOf(Character.toLowerCase(text.charAt(i + 1))) >= 0) {
                result.append(text, i, i + 2);
                i += 2;
                continue;
            }
            // Check tagged hex color <#ffffff>
            if (text.charAt(i) == '<' && i + 8 <= len && text.charAt(i + 7) == '>') {
                String sub = text.substring(i, i + 8);
                if (sub.matches("<#[A-Fa-f0-9]{6}>")) {
                    result.append(sub);
                    i += 8;
                    continue;
                }
            }
            // Check tagged close color </#ffffff>
            if (text.charAt(i) == '<' && i + 9 <= len && text.charAt(i + 8) == '>') {
                String sub = text.substring(i, i + 9);
                if (sub.matches("</#[A-Fa-f0-9]{6}>")) {
                    result.append(sub);
                    i += 9;
                    continue;
                }
            }
            // Check brace placeholder {placeholder}
            if (text.charAt(i) == '{') {
                int end = text.indexOf('}', i);
                if (end != -1) {
                    result.append(text, i, end + 1);
                    i = end + 1;
                    continue;
                }
            }
            // Check percent placeholder %placeholder%
            if (text.charAt(i) == '%') {
                int end = text.indexOf('%', i + 1);
                if (end != -1) {
                    result.append(text, i, end + 1);
                    i = end + 1;
                    continue;
                }
            }

            // Plain text token
            StringBuilder textToken = new StringBuilder();
            while (i < len) {
                char current = text.charAt(i);
                if (current == '&' || current == '\u00A7' || current == '{' || current == '%' || current == '<') {
                    if (current == '&' || current == '\u00A7') {
                        if (i + 1 < len && "0123456789abcdefklmnorx#".indexOf(Character.toLowerCase(text.charAt(i + 1))) >= 0) {
                            break;
                        }
                    } else if (current == '{') {
                        if (text.indexOf('}', i) != -1) break;
                    } else if (current == '%') {
                        if (text.indexOf('%', i + 1) != -1) break;
                    } else if (current == '<') {
                        if (i + 8 <= len && text.substring(i, i + 8).matches("<#[A-Fa-f0-9]{6}>")) break;
                        if (i + 9 <= len && text.substring(i, i + 9).matches("</#[A-Fa-f0-9]{6}>")) break;
                    }
                }
                textToken.append(current);
                i++;
            }

            result.append(formatTitleCaseSmart(textToken.toString()));
        }
        return result.toString();
    }

    private static String formatTitleCaseSmart(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        StringBuilder sb = new StringBuilder();
        int len = text.length();
        int i = 0;
        while (i < len) {
            // Find non-word characters and append them
            while (i < len && !isWordChar(text.charAt(i))) {
                sb.append(text.charAt(i));
                i++;
            }
            if (i >= len) {
                break;
            }
            int start = i;
            // Find word characters
            while (i < len && isWordChar(text.charAt(i))) {
                i++;
            }
            String word = text.substring(start, i);
            sb.append(formatWord(word));
        }
        return sb.toString();
    }

    private static boolean isWordChar(char c) {
        return Character.isLetterOrDigit(c) || c == '\'' || c == '`';
    }

    private static String formatWord(String word) {
        if (word.isEmpty()) {
            return word;
        }
        char first = word.charAt(0);
        if (Character.isLetter(first)) {
            StringBuilder sb = new StringBuilder();
            sb.append(Character.toUpperCase(first));
            for (int i = 1; i < word.length(); i++) {
                sb.append(Character.toLowerCase(word.charAt(i)));
            }
            return sb.toString();
        } else {
            return word;
        }
    }
}
