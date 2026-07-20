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
}
