package com.bx.ultimateDonutSmp.managers;

import org.bukkit.scoreboard.Team;
import org.junit.jupiter.api.Test;

import sun.reflect.ReflectionFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScoreboardLineSplitTest {

    // The dagger the default scoreboard ships with. Outside the BMP, so Java holds it as a surrogate pair.
    private static final String DAGGER = Character.toString(0x1F5E1);

    private ScoreboardManager manager() throws Exception {
        // The real constructor needs a live server; the line splitting only reads its arguments.
        Constructor<Object> objectConstructor = Object.class.getConstructor();
        Constructor<?> managerConstructor = ReflectionFactory.getReflectionFactory()
                .newConstructorForSerialization(ScoreboardManager.class, objectConstructor);
        return (ScoreboardManager) managerConstructor.newInstance();
    }

    private int findSafeSplit(String text, int max) throws Exception {
        Method findSafeSplit = ScoreboardManager.class.getDeclaredMethod("findSafeSplit", String.class, int.class);
        findSafeSplit.setAccessible(true);
        return (int) findSafeSplit.invoke(manager(), text, max);
    }

    private String[] halvesOf(String text) throws Exception {
        String[] halves = new String[2];
        Team team = (Team) Proxy.newProxyInstance(
                Team.class.getClassLoader(),
                new Class<?>[]{Team.class},
                (proxy, method, args) -> {
                    if ("setPrefix".equals(method.getName())) halves[0] = (String) args[0];
                    if ("setSuffix".equals(method.getName())) halves[1] = (String) args[0];
                    return null;
                });

        Method applyLineSpigot = ScoreboardManager.class.getDeclaredMethod("applyLineSpigot", Team.class, String.class);
        applyLineSpigot.setAccessible(true);
        applyLineSpigot.invoke(manager(), team, text);
        return halves;
    }

    private boolean hasUnpairedSurrogate(String text) {
        for (int i = 0; i < text.length(); i++) {
            char current = text.charAt(i);
            if (Character.isHighSurrogate(current)) {
                if (i + 1 >= text.length() || !Character.isLowSurrogate(text.charAt(i + 1))) return true;
            } else if (Character.isLowSurrogate(current)) {
                if (i == 0 || !Character.isHighSurrogate(text.charAt(i - 1))) return true;
            }
        }
        return false;
    }

    @Test
    void keepsAnEmojiWholeWhenItStraddlesTheSplit() throws Exception {
        // The dagger starts at index 63, so cutting at 64 would land between its two halves.
        String line = "a".repeat(63) + DAGGER + "b".repeat(20);
        assertEquals(63, findSafeSplit(line, 64));

        String[] halves = halvesOf(line);
        assertFalse(hasUnpairedSurrogate(halves[0]));
        assertFalse(hasUnpairedSurrogate(halves[1]));
        assertTrue(halves[1].startsWith(DAGGER));
        assertEquals(line, halves[0] + halves[1]);
    }

    @Test
    void keepsAnEmojiWholeAtTheEndOfTheSuffix() throws Exception {
        // Nothing straddles the first cut, so the dagger lands on the second one instead.
        String line = "a".repeat(127) + DAGGER + "b".repeat(20);
        assertEquals(64, findSafeSplit(line, 64));

        String[] halves = halvesOf(line);
        assertFalse(hasUnpairedSurrogate(halves[0]));
        assertFalse(hasUnpairedSurrogate(halves[1]));
    }

    @Test
    void stillKeepsAColourCodeWithItsSectionSign() throws Exception {
        String line = "a".repeat(63) + "\u00A7c" + "b".repeat(20);
        assertEquals(63, findSafeSplit(line, 64));
    }

    @Test
    void backsOffPastASectionSignSittingRightBeforeTheEmoji() throws Exception {
        String line = "a".repeat(62) + "\u00A7" + DAGGER + "b".repeat(20);
        assertEquals(62, findSafeSplit(line, 64));
    }

    @Test
    void cutsAtTheLimitWhenNothingStraddlesIt() throws Exception {
        assertEquals(64, findSafeSplit("a".repeat(100), 64));
    }

    @Test
    void leavesALineShorterThanTheLimitAlone() throws Exception {
        String[] halves = halvesOf("a".repeat(64));
        assertEquals("a".repeat(64), halves[0]);
        assertEquals("", halves[1]);
    }
}
