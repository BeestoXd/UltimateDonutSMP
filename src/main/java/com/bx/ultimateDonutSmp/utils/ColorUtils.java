package com.bx.ultimateDonutSmp.utils;

import com.bx.ultimateDonutSmp.managers.LanguageManager;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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

        String result = text;
        if (hasPAPI) {
            try {
                result = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders((Player) null, result);
            } catch (Exception ignored) {
            }
        }
        return applyColors(result);
    }

    private static String applyColors(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        if (text.indexOf('&') < 0 && text.indexOf('#') < 0 && text.indexOf('<') < 0
                && text.indexOf('{') < 0 && text.indexOf('%') < 0 && text.indexOf('\u00A7') < 0) {
            return text;
        }
        String result = normalizeText(text);
        if (result.indexOf('<') >= 0) {
            result = translateMiniMessage(result);
        }
        result = transformAllCaps(result);
        // Every pattern below needs a literal marker character, so a missing marker rules the pass
        // out without building a Matcher. Scoreboard lines run this ten times a second per player.
        if (result.indexOf("</#") >= 0) {
            result = translateTaggedGradients(result);
        }
        if (result.indexOf('<') >= 0) {
            result = translateTaggedHex(result);
        }
        if (result.indexOf('#') >= 0) {
            result = translateHex(result);
        }
        return result.replace('&', SECTION_CHAR);
    }

    public static String colorize(String text, Player player) {
        if (text == null) {
            return "";
        }

        String result = text;
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

        String result = text;
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

        String base = text.indexOf('<') >= 0 ? stripMiniMessageTags(text) : text;
        return base.replaceAll("&#[A-Fa-f0-9]{6}", "")
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
        // The result is always fourteen characters. Chaining concatenations built a dozen throwaway
        // strings per colour code, and the sidebar runs this for every line of every player.
        char[] out = new char[14];
        out[0] = SECTION_CHAR;
        out[1] = 'x';
        for (int i = 0; i < 6; i++) {
            out[2 + i * 2] = SECTION_CHAR;
            out[3 + i * 2] = hex.charAt(i);
        }
        return new String(out);
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

    public static String unSmallCaps(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        boolean hasSmallCaps = false;
        for (int i = 0; i < text.length(); i++) {
            if (SMALL_CAPS_MAP.containsKey(text.charAt(i))) {
                hasSmallCaps = true;
                break;
            }
        }
        if (!hasSmallCaps) {
            return text;
        }
        StringBuilder sb = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            Character mapped = SMALL_CAPS_MAP.get(c);
            sb.append(mapped != null ? mapped : c);
        }
        return capitalizeFirstTextChar(sb.toString());
    }

    private static String capitalizeFirstTextChar(String text) {
        if (text == null || text.isEmpty()) return text;
        int i = 0;
        int len = text.length();
        while (i < len) {
            if (text.charAt(i) == '&' || text.charAt(i) == '\u00A7') {
                if (i + 1 < len) {
                    i += 2;
                    continue;
                }
            }
            if (Character.isLowerCase(text.charAt(i))) {
                return text.substring(0, i) + Character.toUpperCase(text.charAt(i)) + text.substring(i + 1);
            }
            if (Character.isLetter(text.charAt(i))) {
                break;
            }
            i++;
        }
        return text;
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
        result = result.replace("{Status}", "{status}");
        result = result.replace("{State}", "{state}");
        return result;
    }

    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("&#[A-Fa-f0-9]{6}|\\{#[A-Fa-f0-9]{6}\\}|<#?[A-Fa-f0-9]{6}>|</#?[A-Fa-f0-9]{6}>|&x#[A-Fa-f0-9]{6}|#[A-Fa-f0-9]{6}");
    private static final Pattern LEGACY_COLOR_PATTERN = Pattern.compile("&[0-9a-fk-orA-FK-OR]");
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{.*?\\}|%.*?%");
    private static final Pattern CURRENTLY_PATTERN = Pattern.compile("(?i)\\bcurrently\\b");
    private static final Pattern STATUS_PATTERN = Pattern.compile("(?i)\\bstatus\\b");

    public static String stripColorCodesAndPlaceholders(String text) {
        if (text == null || text.isEmpty()) return "";
        String s = HEX_COLOR_PATTERN.matcher(text).replaceAll("");
        s = LEGACY_COLOR_PATTERN.matcher(s).replaceAll("");
        s = PLACEHOLDER_PATTERN.matcher(s).replaceAll("");
        s = CURRENTLY_PATTERN.matcher(s).replaceAll("");
        s = STATUS_PATTERN.matcher(s).replaceAll("");
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
        boolean hasUpper = false;
        for (int i = 0; i < text.length(); i++) {
            if (Character.isUpperCase(text.charAt(i))) {
                hasUpper = true;
                break;
            }
        }
        if (!hasUpper) {
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

    // MiniMessage support. Config text stays a legacy string the whole way to the client, so the
    // tags are translated into the codes below rather than handed to the Adventure parser. That
    // keeps the plugin's own <#RRGGBB>text</#RRGGBB> gradients working, and it leaves anything the
    // translator does not recognise -- <player>, <amount>, <type> and the rest of the usage
    // strings -- exactly as the admin typed it.

    private static final int MAX_MINI_TAG_LENGTH = 96;
    private static final Pattern MINI_HEX_PATTERN = Pattern.compile("[A-Fa-f0-9]{6}");
    private static final java.util.Map<String, Character> MINI_COLOR_CODES = new java.util.HashMap<>();
    private static final java.util.Map<String, String> MINI_COLOR_HEXES = new java.util.HashMap<>();

    static {
        registerMiniColor("black", '0', "000000");
        registerMiniColor("dark_blue", '1', "0000AA");
        registerMiniColor("dark_green", '2', "00AA00");
        registerMiniColor("dark_aqua", '3', "00AAAA");
        registerMiniColor("dark_red", '4', "AA0000");
        registerMiniColor("dark_purple", '5', "AA00AA");
        registerMiniColor("gold", '6', "FFAA00");
        registerMiniColor("gray", '7', "AAAAAA");
        registerMiniColor("grey", '7', "AAAAAA");
        registerMiniColor("dark_gray", '8', "555555");
        registerMiniColor("dark_grey", '8', "555555");
        registerMiniColor("blue", '9', "5555FF");
        registerMiniColor("green", 'a', "55FF55");
        registerMiniColor("aqua", 'b', "55FFFF");
        registerMiniColor("red", 'c', "FF5555");
        registerMiniColor("light_purple", 'd', "FF55FF");
        registerMiniColor("yellow", 'e', "FFFF55");
        registerMiniColor("white", 'f', "FFFFFF");
    }

    private static void registerMiniColor(String name, char code, String hex) {
        MINI_COLOR_CODES.put(name, code);
        MINI_COLOR_HEXES.put(name, hex);
    }

    public static String translateMiniMessage(String text) {
        if (text == null) {
            return "";
        }
        if (text.indexOf('<') < 0) {
            return text;
        }
        return renderMiniMessage(text, null, new ArrayList<>());
    }

    private static String renderMiniMessage(String text, String inheritedColor, List<Character> inheritedDecorations) {
        StringBuilder out = new StringBuilder(text.length() + 16);
        List<Character> decorations = new ArrayList<>(inheritedDecorations);
        String color = inheritedColor;
        int i = 0;
        int length = text.length();

        while (i < length) {
            char current = text.charAt(i);
            if (current != '<') {
                out.append(current);
                i++;
                continue;
            }

            int close = text.indexOf('>', i + 1);
            if (close < 0 || close - i > MAX_MINI_TAG_LENGTH) {
                out.append(current);
                i++;
                continue;
            }

            String raw = text.substring(i + 1, close);
            // <#RRGGBB> and </#RRGGBB> belong to the gradient pass further down applyColors.
            if (raw.startsWith("#") || raw.startsWith("/#")) {
                out.append(text, i, close + 1);
                i = close + 1;
                continue;
            }

            boolean closing = raw.startsWith("/");
            String body = closing ? raw.substring(1) : raw;
            int separator = body.indexOf(':');
            String name = (separator < 0 ? body : body.substring(0, separator)).toLowerCase(Locale.ROOT);
            String argument = separator < 0 ? "" : body.substring(separator + 1);
            boolean gradientTag = name.equals("gradient") || name.equals("rainbow");

            if (gradientTag && !closing) {
                int contentStart = close + 1;
                int contentEnd = findMiniClosingTag(text, contentStart, name);
                if (contentEnd < 0) {
                    out.append(text, i, close + 1);
                    i = close + 1;
                    continue;
                }

                StringBuilder content = new StringBuilder();
                appendActiveStyle(content, null, decorations);
                content.append(renderMiniMessage(text.substring(contentStart, contentEnd), null, decorations));
                String inner = content.toString();

                out.append(name.equals("gradient")
                        ? expandGradient(inner, gradientStops(argument))
                        : expandRainbow(inner, argument));
                out.append("&r");
                appendActiveStyle(out, color, decorations);
                i = text.indexOf('>', contentEnd) + 1;
                continue;
            }

            if (gradientTag) {
                out.append("&r");
                appendActiveStyle(out, color, decorations);
                i = close + 1;
                continue;
            }

            if (closing && isMiniColorTag(name)) {
                color = null;
                out.append("&r");
                appendActiveStyle(out, null, decorations);
                i = close + 1;
                continue;
            }

            Character decoration = miniDecorationCode(name);
            if (decoration != null) {
                if (closing || argument.equalsIgnoreCase("false")) {
                    decorations.remove(decoration);
                    out.append("&r");
                    appendActiveStyle(out, color, decorations);
                } else if (!decorations.contains(decoration)) {
                    decorations.add(decoration);
                    out.append('&').append(decoration.charValue());
                }
                i = close + 1;
                continue;
            }

            if (!closing && name.equals("reset")) {
                color = null;
                decorations.clear();
                out.append("&r");
                i = close + 1;
                continue;
            }

            if (!closing && (name.equals("newline") || name.equals("br"))) {
                out.append('\n');
                i = close + 1;
                continue;
            }

            if (!closing) {
                String resolved = resolveMiniColor(name, argument);
                if (resolved != null) {
                    color = resolved;
                    out.append(resolved);
                    appendActiveStyle(out, null, decorations);
                    i = close + 1;
                    continue;
                }
            }

            out.append(text, i, close + 1);
            i = close + 1;
        }

        return out.toString();
    }

    // A legacy colour code clears every decoration with it, so anything still open has to be
    // written again behind the colour.
    private static void appendActiveStyle(StringBuilder out, String color, List<Character> decorations) {
        if (color != null) {
            out.append(color);
        }
        for (char decoration : decorations) {
            out.append('&').append(decoration);
        }
    }

    private static int findMiniClosingTag(String text, int from, String name) {
        int depth = 0;
        int i = from;
        while (i < text.length()) {
            int open = text.indexOf('<', i);
            if (open < 0) {
                return -1;
            }
            int close = text.indexOf('>', open + 1);
            if (close < 0) {
                return -1;
            }
            String raw = text.substring(open + 1, close);
            boolean closing = raw.startsWith("/");
            String body = closing ? raw.substring(1) : raw;
            int separator = body.indexOf(':');
            String tag = (separator < 0 ? body : body.substring(0, separator)).toLowerCase(Locale.ROOT);
            if (tag.equals(name)) {
                if (!closing) {
                    depth++;
                } else if (depth == 0) {
                    return open;
                } else {
                    depth--;
                }
            }
            i = close + 1;
        }
        return -1;
    }

    private static boolean isMiniColorTag(String name) {
        return name.equals("color") || name.equals("colour") || name.equals("c")
                || MINI_COLOR_CODES.containsKey(name);
    }

    private static String resolveMiniColor(String name, String argument) {
        if (name.equals("color") || name.equals("colour") || name.equals("c")) {
            return resolveMiniColorValue(argument);
        }
        return argument.isEmpty() ? resolveMiniColorValue(name) : null;
    }

    private static String resolveMiniColorValue(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        Character code = MINI_COLOR_CODES.get(value.toLowerCase(Locale.ROOT));
        if (code != null) {
            return "&" + code;
        }
        String hex = miniColorHex(value);
        return hex == null ? null : "&#" + hex;
    }

    private static String miniColorHex(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.startsWith("#")) {
            String hex = trimmed.substring(1);
            return MINI_HEX_PATTERN.matcher(hex).matches() ? hex.toUpperCase(Locale.ROOT) : null;
        }
        return MINI_COLOR_HEXES.get(trimmed.toLowerCase(Locale.ROOT));
    }

    private static Character miniDecorationCode(String name) {
        return switch (name) {
            case "bold", "b" -> 'l';
            case "italic", "i", "em" -> 'o';
            case "underlined", "underline", "u" -> 'n';
            case "strikethrough", "st" -> 'm';
            case "obfuscated", "obf" -> 'k';
            default -> null;
        };
    }

    private static List<String> gradientStops(String argument) {
        List<String> stops = new ArrayList<>();
        if (argument != null && !argument.isEmpty()) {
            for (String part : argument.split(":")) {
                String hex = miniColorHex(part);
                if (hex != null) {
                    stops.add(hex);
                }
            }
        }
        if (stops.size() < 2) {
            stops.clear();
            stops.add("FFFFFF");
            stops.add("000000");
        }
        return stops;
    }

    // Every pair of stops becomes one of the plugin's own <#start>text</#end> gradients, so the
    // colour maths stays in applyGradient instead of gaining a second implementation here.
    private static String expandGradient(String content, List<String> stops) {
        int segments = stops.size() - 1;
        if (segments == 1) {
            return "<#" + stops.get(0) + ">" + content + "</#" + stops.get(1) + ">";
        }

        int visible = countVisibleCharacters(content);
        if (visible <= 0) {
            return content;
        }

        StringBuilder out = new StringBuilder(content.length() + segments * 20);
        int consumed = 0;
        int cursor = 0;
        for (int segment = 0; segment < segments; segment++) {
            int target = (int) Math.round((double) visible * (segment + 1) / segments);
            StringBuilder chunk = new StringBuilder();
            while (cursor < content.length() && consumed < target) {
                char current = content.charAt(cursor);
                if ((current == '&' || current == SECTION_CHAR) && cursor + 1 < content.length()) {
                    chunk.append(current).append(content.charAt(cursor + 1));
                    cursor += 2;
                    continue;
                }
                chunk.append(current);
                cursor++;
                consumed++;
            }
            if (chunk.length() == 0) {
                continue;
            }
            out.append("<#").append(stops.get(segment)).append('>')
                    .append(chunk)
                    .append("</#").append(stops.get(segment + 1)).append('>');
        }
        if (cursor < content.length()) {
            out.append(content, cursor, content.length());
        }
        return out.toString();
    }

    private static String expandRainbow(String content, String argument) {
        int visible = countVisibleCharacters(content);
        if (visible <= 0) {
            return content;
        }

        boolean reversed = argument.startsWith("!");
        String phaseText = reversed ? argument.substring(1) : argument;
        double phase = 0.0D;
        if (!phaseText.isEmpty()) {
            try {
                phase = Double.parseDouble(phaseText);
            } catch (NumberFormatException ignored) {
            }
        }

        List<String> stops = new ArrayList<>(visible + 1);
        for (int index = 0; index <= visible; index++) {
            double hue = (double) index / visible + phase;
            stops.add(hueToHex(hue - Math.floor(hue)));
        }
        if (reversed) {
            java.util.Collections.reverse(stops);
        }
        return expandGradient(content, stops);
    }

    private static String hueToHex(double hue) {
        double scaled = hue * 6.0D;
        int sector = (int) Math.floor(scaled) % 6;
        if (sector < 0) {
            sector += 6;
        }
        int rising = (int) Math.round((scaled - Math.floor(scaled)) * 255.0D);
        int falling = 255 - rising;
        int red;
        int green;
        int blue;
        switch (sector) {
            case 0 -> {
                red = 255;
                green = rising;
                blue = 0;
            }
            case 1 -> {
                red = falling;
                green = 255;
                blue = 0;
            }
            case 2 -> {
                red = 0;
                green = 255;
                blue = rising;
            }
            case 3 -> {
                red = 0;
                green = falling;
                blue = 255;
            }
            case 4 -> {
                red = rising;
                green = 0;
                blue = 255;
            }
            default -> {
                red = 255;
                green = 0;
                blue = falling;
            }
        }
        return String.format("%02X%02X%02X", red, green, blue);
    }

    private static String stripMiniMessageTags(String text) {
        StringBuilder out = new StringBuilder(text.length());
        int i = 0;
        int length = text.length();
        while (i < length) {
            char current = text.charAt(i);
            if (current != '<') {
                out.append(current);
                i++;
                continue;
            }

            int close = text.indexOf('>', i + 1);
            if (close < 0 || close - i > MAX_MINI_TAG_LENGTH) {
                out.append(current);
                i++;
                continue;
            }

            String raw = text.substring(i + 1, close);
            // The hex and gradient tags are taken out by the regexes in strip() instead.
            if (!raw.startsWith("#") && !raw.startsWith("/#") && isRecognisedMiniTag(raw)) {
                i = close + 1;
                continue;
            }

            out.append(text, i, close + 1);
            i = close + 1;
        }
        return out.toString();
    }

    private static boolean isRecognisedMiniTag(String raw) {
        boolean closing = raw.startsWith("/");
        String body = closing ? raw.substring(1) : raw;
        int separator = body.indexOf(':');
        String name = (separator < 0 ? body : body.substring(0, separator)).toLowerCase(Locale.ROOT);
        String argument = separator < 0 ? "" : body.substring(separator + 1);
        return switch (name) {
            case "gradient", "rainbow", "reset", "newline", "br" -> true;
            case "color", "colour", "c" -> closing || miniColorHex(argument) != null;
            default -> miniDecorationCode(name) != null || MINI_COLOR_CODES.containsKey(name);
        };
    }
}
