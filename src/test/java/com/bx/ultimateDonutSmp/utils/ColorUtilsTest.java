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
    void testAllCapsWithoutAnyMarkerIsLeftAsTyped() {
        String input = "WELCOME TO THE SERVER";
        assertEquals(input, ColorUtils.colorize(input));
    }

    @Test
    void testAllCapsIsRetypedWhenOnlyAPlaceholderCarriesTheLine() {
        String input = "PLAIN CAPS %player%";
        assertEquals("Plain Caps %player%", ColorUtils.colorize(input));
    }

    @Test
    void testOneLowercaseWordKeepsTheCapitalsIntact() {
        String input = "&cWELCOME to the server";
        assertEquals("§cWELCOME to the server", ColorUtils.colorize(input));
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
                "plain text with no codes",
                "<red>Kills</red> &710",
                "<bold>one<red>two</red>three",
                "<gradient:#FF0000:#0000FF>Kills</gradient>",
                "<rainbow>kills</rainbow>",
                "&7give <player>"
        };

        for (String line : lines) {
            String once = ColorUtils.colorize(line);
            assertEquals(once, ColorUtils.colorize(once), "second pass changed: " + line);
        }
    }

    @Test
    void testMiniMessageNamedColor() {
        assertEquals("§cHello World", ColorUtils.colorize("<red>Hello World"));
        assertEquals("§7Hello World", ColorUtils.colorize("<gray>Hello World"));
        assertEquals("§7Hello World", ColorUtils.colorize("<grey>Hello World"));
    }

    @Test
    void testMiniMessageHexColorTag() {
        assertEquals(legacyHex("FF0000") + "Hello World", ColorUtils.colorize("<color:#FF0000>Hello World"));
        assertEquals("§cHello World", ColorUtils.colorize("<c:red>Hello World"));
    }

    @Test
    void testMiniMessageDecorationSurvivesAColorChange() {
        // A legacy colour code wipes bold, so the translator has to write it again behind the colour.
        assertEquals("§l§c§lhi", ColorUtils.colorize("<bold><red>hi"));
        assertEquals("§l§c§lhi", ColorUtils.colorize("<b><red>hi"));
    }

    @Test
    void testMiniMessageClosingTagRestoresTheOuterStyle() {
        assertEquals("§lone§c§ltwo§r§lthree",
                ColorUtils.colorize("<bold>one<red>two</red>three"));
        assertEquals("§cone§ltwo§r§cthree",
                ColorUtils.colorize("<red>one<bold>two</bold>three"));
    }

    @Test
    void testMiniMessageResetDropsEverything() {
        assertEquals("§c§lone§rtwo", ColorUtils.colorize("<red><bold>one<reset>two"));
    }

    @Test
    void testMiniMessageGradientUsesTheExistingGradientRenderer() {
        assertEquals(legacyHex("FF0000") + "A" + legacyHex("0000FF") + "b" + "§r",
                ColorUtils.colorize("<gradient:#FF0000:#0000FF>Ab</gradient>"));
    }

    @Test
    void testMiniMessageGradientAcceptsNamedStops() {
        assertEquals(legacyHex("FF5555") + "A" + legacyHex("5555FF") + "b" + "§r",
                ColorUtils.colorize("<gradient:red:blue>Ab</gradient>"));
    }

    @Test
    void testMiniMessageGradientSpansThreeStops() {
        assertEquals(legacyHex("FF0000") + "a" + legacyHex("00FF00") + "b"
                        + legacyHex("00FF00") + "c" + legacyHex("0000FF") + "d" + "§r",
                ColorUtils.colorize("<gradient:#FF0000:#00FF00:#0000FF>abcd</gradient>"));
    }

    @Test
    void testMiniMessageRainbowColorsEveryCharacter() {
        assertEquals(legacyHex("FF0000") + "a" + legacyHex("00FF00") + "b"
                        + legacyHex("0000FF") + "c" + "§r",
                ColorUtils.colorize("<rainbow>abc</rainbow>"));
    }

    @Test
    void testMiniMessageGradientKeepsBoldFromOutside() {
        assertEquals("§l" + legacyHex("FF0000") + "§la" + legacyHex("0000FF") + "§lb"
                        + "§r§l",
                ColorUtils.colorize("<bold><gradient:#FF0000:#0000FF>ab</gradient>"));
    }

    @Test
    void testUnknownTagsAreLeftAlone() {
        // menus.yml and messages.yml are full of these; swallowing them would gut every usage line.
        assertEquals("§7Usage: §f/amethysttool give <player> <type>",
                ColorUtils.colorize("&7Usage: &f/amethysttool give <player> <type>"));
        assertEquals("§fSold §a<amount>§f items",
                ColorUtils.colorize("&fSold &a<amount>&f items"));
    }

    @Test
    void testPluginGradientSyntaxStillWins() {
        // <#RRGGBB>text</#RRGGBB> is the plugin's own syntax, not MiniMessage, and it has to keep
        // rendering the way every existing config expects.
        assertEquals(legacyHex("FF0000") + "A" + legacyHex("0000FF") + "b",
                ColorUtils.colorize("<#FF0000>Ab</#0000FF>"));
    }

    @Test
    void testStripRemovesMiniMessageTags() {
        assertEquals("Shop", ColorUtils.strip("<red><bold>Shop</bold></red>"));
        assertEquals("Shop", ColorUtils.strip("<gradient:#FF0000:#0000FF>Shop</gradient>"));
        assertEquals("Shop", ColorUtils.strip("<color:#FF0000>Shop"));
        assertEquals("give <player>", ColorUtils.strip("&7give <player>"));
    }

    @Test
    void testNormalizeLabelFoldsSmallCaps() {
        assertTrue(ColorUtils.normalizeLabel("&fʙᴜʏ ᴘʀɪᴄᴇ: &a$250").contains("buy price:"));
        assertTrue(ColorUtils.normalizeLabel("&fʜᴀʀɢᴀ ʙᴇʟɪ: &a$250").contains("harga beli:"));
        assertTrue(ColorUtils.normalizeLabel("&fBuy Price: &a$250").contains("buy price:"));
        assertEquals("", ColorUtils.normalizeLabel(null));
        assertEquals("&5250x shards", ColorUtils.normalizeLabel("&5250x Shards"));
    }
}
