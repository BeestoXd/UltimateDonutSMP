package com.bx.ultimateDonutSmp.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtils {

    private static final char SECTION_CHAR = '\u00A7';
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
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

        String result = translateTaggedGradients(text);
        result = translateTaggedHex(result);
        return translateHex(result).replace('&', SECTION_CHAR);
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
        return colorize(result);
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
        return colorize(result);
    }

    public static Component toComponent(String text) {
        return LegacyComponentSerializer.legacySection().deserialize(colorize(text));
    }

    public static Component toComponent(String text, Player player) {
        return LegacyComponentSerializer.legacySection().deserialize(colorize(text, player));
    }

    public static String toLegacyString(Component component) {
        return component == null ? "" : LegacyComponentSerializer.legacySection().serialize(component);
    }

    public static List<Component> toComponentList(List<String> lines) {
        List<Component> list = new ArrayList<>();
        for (String line : lines) {
            list.add(toComponent(line));
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
}
