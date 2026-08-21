package com.bx.ultimateDonutSmp.managers;

import org.junit.jupiter.api.Test;

import sun.reflect.ReflectionFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ScoreboardTeamLineTest {

    private static final String TEAM_LINE = "&#00A4FC &fTeam &#00A4FC%economy_team%     ";
    private static final String BOOSTER_LINE = "&fBooster &#A303F9%economy_booster_countdown%";
    private static final String SHARD_CUBOID_LINE = "&fShards &#A303F9%economy_shard_cuboid_display%";

    private String resolve(String line, boolean inTeam) throws Exception {
        // The real constructor needs a live server; resolveConfiguredLine only reads its arguments.
        Constructor<Object> objectConstructor = Object.class.getConstructor();
        Constructor<?> managerConstructor = ReflectionFactory.getReflectionFactory()
                .newConstructorForSerialization(ScoreboardManager.class, objectConstructor);
        ScoreboardManager manager = (ScoreboardManager) managerConstructor.newInstance();

        Method resolveConfiguredLine = ScoreboardManager.class.getDeclaredMethod(
                "resolveConfiguredLine",
                String.class,
                String.class,
                String.class,
                String.class,
                boolean.class,
                boolean.class,
                boolean.class
        );
        resolveConfiguredLine.setAccessible(true);

        return (String) resolveConfiguredLine.invoke(
                manager,
                line,
                TEAM_LINE,
                BOOSTER_LINE,
                SHARD_CUBOID_LINE,
                inTeam,
                false,
                false
        );
    }

    @Test
    void dropsTheTeamLineForPlayersWithoutATeam() throws Exception {
        assertNull(resolve("{team}", false));
    }

    @Test
    void showsTheTeamLineOnceThePlayerHasATeam() throws Exception {
        assertEquals(TEAM_LINE, resolve("{team}", true));
    }

    @Test
    void ignoresSurroundingWhitespaceAndCaseOnThePlaceholder() throws Exception {
        assertNull(resolve("  {TEAM}  ", false));
        assertEquals(TEAM_LINE, resolve("  {TEAM}  ", true));
    }

    @Test
    void keepsARawTeamLineVisibleWithoutATeam() throws Exception {
        // Servers that still want the line can drop {team} and inline the text themselves.
        String raw = "&fTeam &#00A4FC%economy_team%";
        assertEquals(raw, resolve(raw, false));
    }
}
