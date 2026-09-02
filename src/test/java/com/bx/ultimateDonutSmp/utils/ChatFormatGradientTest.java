package com.bx.ultimateDonutSmp.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The chat line is coloured in pieces so the player's name can carry a hover event of its own, and a
 * gradient opened before the name used to be cut in half by that split and printed as plain text.
 * These cover the pieces the line is built from.
 */
class ChatFormatGradientTest {

    private static final char SECTION = '§';

    @Test
    void anUnclosedGradientColoursTheRestOfTheTextInsteadOfPrintingItsTag() {
        String colorized = ColorUtils.colorize("<gradient:#FF0000:#0000FF>Notch");

        assertFalse(colorized.contains("gradient"), colorized);
        assertFalse(colorized.contains("<"), colorized);
        assertTrue(colorized.startsWith(hex("FF0000")), colorized);
        assertTrue(colorized.contains(hex("0000FF")), colorized);
        assertEquals("Notch", ColorUtils.strip(colorized));
    }

    @Test
    void anUnclosedRainbowColoursTheRestOfTheTextToo() {
        String colorized = ColorUtils.colorize("<rainbow>Notch");

        assertFalse(colorized.contains("rainbow"), colorized);
        assertFalse(colorized.contains("<"), colorized);
        assertEquals("Notch", ColorUtils.strip(colorized));
    }

    @Test
    void aClosingTagLeftOnItsOwnPrintsNothing() {
        String colorized = ColorUtils.colorize("</gradient>");

        assertFalse(colorized.contains("gradient"), colorized);
        assertEquals("", ColorUtils.strip(colorized));
    }

    @Test
    void aGradientOpenedBeforeThePlayerNameStillReachesIt() {
        String beforePlayer = "<gradient:#FF0000:#0000FF>[VIP] ";
        String displayName = "Notch";

        String head = ColorUtils.colorize(beforePlayer + displayName);
        String[] split = ColorUtils.splitTrailingVisible(
                head, ColorUtils.visibleLength(ColorUtils.colorize(displayName)));

        assertEquals("[VIP] ", ColorUtils.strip(split[0]));
        assertEquals("Notch", ColorUtils.strip(split[1]));
        assertTrue(split[0].startsWith(hex("FF0000")), split[0]);
        assertTrue(split[1].startsWith(String.valueOf(SECTION) + 'x'), split[1]);
        assertTrue(split[1].contains(hex("0000FF")), split[1]);
    }

    @Test
    void noPieceOfTheChatLineShowsARawTag() {
        String resolved = "<gradient:#FF0000:#0000FF>%prefix%%player%</gradient>&7: &f%message%"
                .replace("%prefix%", "");
        int playerIndex = resolved.indexOf("%player%");
        int messageIndex = resolved.indexOf("%message%");
        String beforePlayer = resolved.substring(0, playerIndex);
        String between = resolved.substring(playerIndex + "%player%".length(), messageIndex);
        String after = resolved.substring(messageIndex + "%message%".length());

        String rendered = ColorUtils.colorize(beforePlayer + "Notch")
                + ColorUtils.colorize(between)
                + ColorUtils.colorize(after);

        assertFalse(rendered.contains("<"), rendered);
        assertFalse(rendered.contains("gradient"), rendered);
        assertEquals("Notch: ", ColorUtils.strip(rendered));
    }

    @Test
    void theSplitCarriesTheColourActiveAtTheCut() {
        String[] split = ColorUtils.splitTrailingVisible(ColorUtils.colorize("&a[VIP] Notch"), 5);

        assertEquals(SECTION + "a[VIP] ", split[0]);
        assertEquals(SECTION + "aNotch", split[1]);
    }

    @Test
    void theSplitCarriesAFormatCodeThatIsStillOpen() {
        String[] split = ColorUtils.splitTrailingVisible(ColorUtils.colorize("&c&lAlex Steve"), 5);

        assertEquals(SECTION + "c" + SECTION + "lAlex ", split[0]);
        assertEquals(SECTION + "c" + SECTION + "lSteve", split[1]);
    }

    @Test
    void theSplitReadsAHexColourRunWhole() {
        String[] split = ColorUtils.splitTrailingVisible(ColorUtils.colorize("&#FF0000ab"), 1);

        assertEquals(hex("FF0000") + "a", split[0]);
        assertEquals(hex("FF0000") + "b", split[1]);
    }

    @Test
    void askingForMoreThanIsThereLeavesTheWholeLineAsTheName() {
        String[] split = ColorUtils.splitTrailingVisible(ColorUtils.colorize("&aNotch"), 20);

        assertEquals("", split[0]);
        assertEquals(SECTION + "aNotch", split[1]);
    }

    @Test
    void aStaffChatLineIsUnaffectedBecauseItIsColouredInOnePass() {
        String colorized = ColorUtils.colorize(
                "<gradient:#FF0000:#0000FF>[SC] Notch: hello</gradient>");

        assertFalse(colorized.contains("gradient"), colorized);
        assertEquals("[SC] Notch: hello", ColorUtils.strip(colorized));
    }

    private static String hex(String value) {
        StringBuilder out = new StringBuilder().append(SECTION).append('x');
        for (int i = 0; i < value.length(); i++) {
            out.append(SECTION).append(value.charAt(i));
        }
        return out.toString();
    }
}
