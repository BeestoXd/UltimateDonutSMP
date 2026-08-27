package com.bx.ultimateDonutSmp.managers;

import org.junit.jupiter.api.Test;

import sun.reflect.ReflectionFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class ScoreboardEconomyPlaceholderTest {

    private static final String MONEY = "1,2K";
    private static final String SHARDS = "340";

    private String apply(String line) throws Exception {
        // The real constructor needs a live server; the substitution only reads its arguments.
        Constructor<Object> objectConstructor = Object.class.getConstructor();
        Constructor<?> managerConstructor = ReflectionFactory.getReflectionFactory()
                .newConstructorForSerialization(ScoreboardManager.class, objectConstructor);
        ScoreboardManager manager = (ScoreboardManager) managerConstructor.newInstance();

        Method apply = ScoreboardManager.class.getDeclaredMethod(
                "applySidebarEconomyPlaceholders", String.class, String.class, String.class);
        apply.setAccessible(true);

        return (String) apply.invoke(manager, line, MONEY, SHARDS);
    }

    @Test
    void substitutesEveryMoneyToken() throws Exception {
        assertEquals("&f" + MONEY, apply("&f%economy_nicestMoney%"));
        assertEquals("&f" + MONEY, apply("&f%economy_money_short%"));
        assertEquals("&f" + MONEY, apply("&f%economy_money_amount_short%"));
    }

    @Test
    void substitutesEveryShardToken() throws Exception {
        assertEquals("&f" + SHARDS, apply("&f%economy_nicestShards%"));
        assertEquals("&f" + SHARDS, apply("&f%economy_shards_short%"));
        assertEquals("&f" + SHARDS, apply("&f%economy_shards_amount_short%"));
        assertEquals("&f" + SHARDS, apply("&f%economy_shards%"));
    }

    @Test
    void substitutesBothOnOneLine() throws Exception {
        assertEquals(
                "&aMoney " + MONEY + " &dShards " + SHARDS,
                apply("&aMoney %economy_money_short% &dShards %economy_shards%")
        );
    }

    @Test
    void leavesLinesWithoutEconomyTokensUntouched() throws Exception {
        // The early-out skips seven replace scans, so it has to return the line exactly as given,
        // including lines that carry other placeholders.
        String plain = "&#00A4FC &fKills &#00A4FC%statistic_player_kills%";
        assertSame(plain, apply(plain));

        String noPlaceholder = "&7ᴘɪɴɢ: &f25ms";
        assertSame(noPlaceholder, apply(noPlaceholder));
    }

    @Test
    void handlesEmptyAndNullLines() throws Exception {
        assertEquals("", apply(""));
        assertEquals("", apply(null));
    }
}
