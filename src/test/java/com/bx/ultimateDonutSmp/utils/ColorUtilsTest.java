package com.bx.ultimateDonutSmp.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ColorUtilsTest {

    @Test
    void testAmpersandHexColor() {
        String input = "&#FF0000Hello World";
        String colorized = ColorUtils.colorize(input);
        assertEquals("\u00A7x\u00A7F\u00A7F\u00A70\u00A70\u00A70\u00A70Hello World", colorized);
    }

    @Test
    void testStandaloneHexColor() {
        String input = "#FF0000Hello World";
        String colorized = ColorUtils.colorize(input);
        assertEquals("\u00A7x\u00A7F\u00A7F\u00A70\u00A70\u00A70\u00A70Hello World", colorized);
    }

    @Test
    void testBracedHexColor() {
        String input = "{#FF0000}Hello World";
        String colorized = ColorUtils.colorize(input);
        assertEquals("\u00A7x\u00A7F\u00A7F\u00A70\u00A70\u00A70\u00A70Hello World", colorized);
    }

    @Test
    void testTaggedHexColor() {
        String input = "<#FF0000>Hello World";
        String colorized = ColorUtils.colorize(input);
        assertEquals("\u00A7x\u00A7F\u00A7F\u00A70\u00A70\u00A70\u00A70Hello World", colorized);
    }

    @Test
    void testAmpersandXHexColor() {
        String input = "&x#FF0000Hello World";
        String colorized = ColorUtils.colorize(input);
        assertEquals("\u00A7x\u00A7F\u00A7F\u00A70\u00A70\u00A70\u00A70Hello World", colorized);
    }

    @Test
    void testAllCapsMessageWithHex() {
        String input = "#FF0000YOU DO NOT HAVE PERMISSION!";
        String colorized = ColorUtils.colorize(input);
        assertEquals("\u00A7x\u00A7F\u00A7F\u00A70\u00A70\u00A70\u00A70You Do Not Have Permission!", colorized);
    }

    @Test
    void testSmallCapsPreservation() {
        String input = "&fᴏᴡɴᴇʀ";
        String colorized = ColorUtils.colorize(input);
        assertEquals("\u00A7fᴏᴡɴᴇʀ", colorized);
    }

    @Test
    void testSmallCapsScoreboardLinePreserved() {
        String input = "&7ᴘɪɴɢ: &f25ms";
        String colorized = ColorUtils.colorize(input);
        assertEquals("\u00A77ᴘɪɴɢ: \u00A7f25ms", colorized);
    }

    @Test
    void testSmallCapsWithHexColorPreserved() {
        String input = "&#FF0000ʀᴇɢɪᴏɴ: &f25ᴍꜱ";
        String colorized = ColorUtils.colorize(input);
        assertEquals("\u00A7x\u00A7F\u00A7F\u00A70\u00A70\u00A70\u00A70ʀᴇɢɪᴏɴ: \u00A7f25ᴍꜱ", colorized);
    }

    @Test
    void testEmojiAndSymbolsPreserved() {
        String input = "&#FF0000🗡 &fKills &7★";
        String colorized = ColorUtils.colorize(input);
        assertEquals("\u00A7x\u00A7F\u00A7F\u00A70\u00A70\u00A70\u00A70🗡 \u00A7fKills \u00A77★", colorized);
    }

    @Test
    void testUnicodeEscapesStillDecode() {
        String input = "&f\\u1D18\\u026A\\u0274\\u0262";
        String colorized = ColorUtils.colorize(input);
        assertEquals("\u00A7fᴘɪɴɢ", colorized);
    }

    @Test
    void testStripHexColors() {
        String stripped1 = ColorUtils.strip("#FF0000Hello");
        assertEquals("Hello", stripped1);

        String stripped2 = ColorUtils.strip("{#FF0000}Hello");
        assertEquals("Hello", stripped2);

        String stripped3 = ColorUtils.strip("&#FF0000Hello");
        assertEquals("Hello", stripped3);

        String stripped4 = ColorUtils.strip("<#FF0000>Hello");
        assertEquals("Hello", stripped4);
    }

    private static String legacyHex(String hex) {
        StringBuilder out = new StringBuilder("§x");
        for (char digit : hex.toCharArray()) {
            out.append('§').append(digit);
        }
        return out.toString();
    }

    @Test
    void testGradientStillExpandsBetweenTags() {
        String colorized = ColorUtils.colorize("<#FF0000>Ab</#0000FF>");
        assertEquals(legacyHex("FF0000") + "A" + legacyHex("0000FF") + "b", colorized);
    }

    @Test
    void testColorizingTwiceChangesNothing() {
        // The sidebar hands finished text straight to the team prefix, so a second pass over an
        // already-colorized line has to be a no-op.
        String[] lines = {
                "&#00A4FC §fTeam &#00A4FCAlpha     ",
                "&f&lBALANCE: &a1,234",
                "&7ᴘɪɴɢ: &f25ms",
                "<#FF0000>Kills</#0000FF> &710",
                "{#FF0000}Shards &f42",
                "&f\\u1D18\\u026A\\u0274\\u0262",
                "🗡 &fKills &7★",
                "plain text with no codes"
        };

        for (String line : lines) {
            String once = ColorUtils.colorize(line);
            assertEquals(once, ColorUtils.colorize(once), "second pass changed: " + line);
        }
    }
}
