package com.bx.ultimateDonutSmp.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class NumberUtils {

    private static final DecimalFormat COMMA_FMT;
    private static final DecimalFormat SHORT_FMT;
    private static final String[] SHORT_SUFFIXES = {"", "K", "M", "B", "T"};

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        COMMA_FMT = new DecimalFormat("#,##0.##", symbols);
        SHORT_FMT = new DecimalFormat("#,##0.##", symbols);
    }

    /** Format with commas: 1234567 → 1,234,567 */
    public static String format(double number) {
        return COMMA_FMT.format(number);
    }

    /** Format with suffix: 1500 → 1.5K */
    public static String formatNice(double number) {
        if (!Double.isFinite(number)) {
            return "0";
        }

        double absolute = Math.abs(number);
        int suffixIndex = 0;

        while (absolute >= 1_000D && suffixIndex < SHORT_SUFFIXES.length - 1) {
            absolute /= 1_000D;
            suffixIndex++;
        }

        if (absolute >= 999.995D && suffixIndex < SHORT_SUFFIXES.length - 1) {
            absolute /= 1_000D;
            suffixIndex++;
        }

        String sign = number < 0D ? "-" : "";
        if (suffixIndex == 0) {
            return sign + COMMA_FMT.format(absolute);
        }

        return sign + SHORT_FMT.format(absolute) + SHORT_SUFFIXES[suffixIndex];
    }

    /** Parse a number string with optional K/M/B/T suffix */
    public static double parse(String input) {
        if (input == null || input.isBlank()) throw new NumberFormatException("Empty input");
        String clean = input.trim().toUpperCase(Locale.US);
        double multiplier = 1;
        if (clean.endsWith("T")) { multiplier = 1_000_000_000_000D; clean = clean.substring(0, clean.length() - 1); }
        else if (clean.endsWith("B")) { multiplier = 1_000_000_000; clean = clean.substring(0, clean.length() - 1); }
        else if (clean.endsWith("M")) { multiplier = 1_000_000; clean = clean.substring(0, clean.length() - 1); }
        else if (clean.endsWith("K")) { multiplier = 1_000;    clean = clean.substring(0, clean.length() - 1); }
        return Double.parseDouble(clean) * multiplier;
    }

    public static boolean isValidPositiveAmount(String input) {
        try {
            return parse(input) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /** Format seconds as readable time: 3665 → "1h 1m" */
    public static String formatTime(long totalSeconds) {
        long h = totalSeconds / 3600;
        long m = (totalSeconds % 3600) / 60;
        long s = totalSeconds % 60;
        if (h > 0) return h + "h " + m + "m";
        if (m > 0) return m + "m " + s + "s";
        return s + "s";
    }

    /** Format seconds with days: 90061 → "1d 1h 1m" */
    public static String formatTimeLong(long totalSeconds) {
        long d = totalSeconds / 86400;
        long h = (totalSeconds % 86400) / 3600;
        long m = (totalSeconds % 3600) / 60;
        if (d > 0) return d + "d " + h + "h " + m + "m";
        return formatTime(totalSeconds);
    }

    /** Format remaining seconds for countdown display */
    public static String formatCountdown(long totalSeconds) {
        long m = totalSeconds / 60;
        long s = totalSeconds % 60;
        if (m > 0) return m + "m " + s + "s";
        return s + "s";
    }

    public static long parseLong(String input) {
        return (long) parse(input);
    }
}
