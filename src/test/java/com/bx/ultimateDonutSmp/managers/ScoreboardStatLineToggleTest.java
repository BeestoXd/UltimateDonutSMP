package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.models.PlayerData;
import org.junit.jupiter.api.Test;

import sun.reflect.ReflectionFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScoreboardStatLineToggleTest {

    private static final String MONEY_LINE = "&#00FC00&l$ &fMoney &#00FC00%economy_nicestMoney%     ";
    private static final String SHARDS_LINE = "&#A303F9 &fShards &#A303F9%economy_shards%     ";
    private static final String KILLS_LINE = "&#FC0000 &fKills &#FC0000%economy_kills%      ";
    private static final String DEATHS_LINE = "&#F97603 &fDeaths &#F97603%economy_deaths%   ";
    private static final String PLAYTIME_LINE = "&#FCE300 &fPlaytime &#FCE300%economy_playtime%   ";
    private static final String KEYALL_LINE = "&#00A4FC &fKeyall &#00A4FC%economy_keyall_countdown%";
    private static final String SHARD_CUBOID_LINE = "&fShards &#A303F9%economy_shard_cuboid_display%";

    private boolean hidden(String line, PlayerData data) throws Exception {
        // The real constructor needs a live server; isHiddenStatLine only reads its arguments.
        Constructor<Object> objectConstructor = Object.class.getConstructor();
        Constructor<?> managerConstructor = ReflectionFactory.getReflectionFactory()
                .newConstructorForSerialization(ScoreboardManager.class, objectConstructor);
        ScoreboardManager manager = (ScoreboardManager) managerConstructor.newInstance();

        Method isHiddenStatLine = ScoreboardManager.class.getDeclaredMethod(
                "isHiddenStatLine", String.class, PlayerData.class);
        isHiddenStatLine.setAccessible(true);
        return (boolean) isHiddenStatLine.invoke(manager, line, data);
    }

    private PlayerData player() {
        return new PlayerData(UUID.randomUUID(), "Tester");
    }

    @Test
    void everyStatLineShowsUntilThePlayerTurnsItOff() throws Exception {
        PlayerData data = player();

        for (String line : new String[]{
                MONEY_LINE, SHARDS_LINE, KILLS_LINE, DEATHS_LINE, PLAYTIME_LINE}) {
            assertFalse(hidden(line, data), line);
        }
    }

    @Test
    void eachToggleOnlyHidesItsOwnLine() throws Exception {
        PlayerData data = player();
        data.setShowKillsLine(false);

        assertTrue(hidden(KILLS_LINE, data));
        assertFalse(hidden(DEATHS_LINE, data));
        assertFalse(hidden(MONEY_LINE, data));
    }

    @Test
    void turningOffShardsLeavesTheCuboidLineAlone() throws Exception {
        // The shard cuboid line has its own {shard_cuboid} switch and reads a different placeholder.
        PlayerData data = player();
        data.setShowShardsLine(false);

        assertTrue(hidden(SHARDS_LINE, data));
        assertFalse(hidden(SHARD_CUBOID_LINE, data));
    }

    @Test
    void theMoneyToggleCoversEveryMoneyPlaceholder() throws Exception {
        PlayerData data = player();
        data.setShowMoneyLine(false);

        assertTrue(hidden("&fMoney %economy_money%", data));
        assertTrue(hidden("&fMoney %economy_money_short%", data));
        assertTrue(hidden("&fMoney %economy_nicestMoney%", data));
        assertTrue(hidden("&fMoney %economy_money_formatted%", data));
    }

    @Test
    void linesWithoutAStatPlaceholderAreNeverHidden() throws Exception {
        PlayerData data = player();
        data.setShowMoneyLine(false);
        data.setShowShardsLine(false);
        data.setShowKillsLine(false);
        data.setShowDeathsLine(false);
        data.setShowPlaytimeLine(false);

        assertFalse(hidden(KEYALL_LINE, data));
        assertFalse(hidden("", data));
        assertFalse(hidden("&7NA East", data));
    }

    @Test
    void aMissingProfileHidesNothing() throws Exception {
        assertFalse(hidden(MONEY_LINE, null));
    }
}
