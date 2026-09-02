package com.bx.ultimateDonutSmp.utils;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BossSoundRangePolicyTest {

    private static final String OVERWORLD = "world";
    private static final String NETHER = "world_nether";

    private static BossSoundRangePolicy policy(int radius) {
        return new BossSoundRangePolicy(true, radius, true, true);
    }

    private static BossSoundRangePolicy.Position at(String world, double x, double z) {
        return new BossSoundRangePolicy.Position(world, x, 64.0D, z);
    }

    @Test
    void onlyTheTwoGlobalBossEventsAreFiltered() {
        BossSoundRangePolicy policy = policy(1600);

        assertTrue(policy.filters(BossSoundRangePolicy.WITHER_SPAWN_EFFECT_ID));
        assertTrue(policy.filters(BossSoundRangePolicy.ENDER_DRAGON_DEATH_EFFECT_ID));

        // Other level events ride the same packet and must be left alone: 1024 is a wither shooting,
        // 1038 is the end portal spawning, 2001 is a block breaking.
        assertFalse(policy.filters(1024));
        assertFalse(policy.filters(1038));
        assertFalse(policy.filters(2001));
    }

    @Test
    void eachSoundCanBeLeftGlobalOnItsOwn() {
        BossSoundRangePolicy witherOnly = new BossSoundRangePolicy(true, 1600, true, false);
        assertTrue(witherOnly.filters(BossSoundRangePolicy.WITHER_SPAWN_EFFECT_ID));
        assertFalse(witherOnly.filters(BossSoundRangePolicy.ENDER_DRAGON_DEATH_EFFECT_ID));

        BossSoundRangePolicy dragonOnly = new BossSoundRangePolicy(true, 1600, false, true);
        assertFalse(dragonOnly.filters(BossSoundRangePolicy.WITHER_SPAWN_EFFECT_ID));
        assertTrue(dragonOnly.filters(BossSoundRangePolicy.ENDER_DRAGON_DEATH_EFFECT_ID));
    }

    @Test
    void turningItOffLeavesEveryEventAlone() {
        BossSoundRangePolicy disabled = new BossSoundRangePolicy(false, 1600, true, true);
        assertFalse(disabled.isActive());
        assertFalse(disabled.filters(BossSoundRangePolicy.WITHER_SPAWN_EFFECT_ID));
        assertFalse(disabled.filters(BossSoundRangePolicy.ENDER_DRAGON_DEATH_EFFECT_ID));
    }

    @Test
    void aRadiusOfZeroOrLessTurnsTheLimitOffRatherThanSilencingEveryone() {
        // Reading it the other way round would let one mistyped number mute both sounds server-wide.
        for (int radius : new int[]{0, -1, -1600}) {
            BossSoundRangePolicy policy = policy(radius);
            assertFalse(policy.isActive(), "radius " + radius);
            assertFalse(policy.filters(BossSoundRangePolicy.WITHER_SPAWN_EFFECT_ID), "radius " + radius);
        }
    }

    @Test
    void playersInsideTheRadiusStillHearIt() {
        BossSoundRangePolicy policy = policy(1600);

        assertTrue(policy.canHear(at(OVERWORLD, 0, 0), at(OVERWORLD, 0, 0)));
        assertTrue(policy.canHear(at(OVERWORLD, 0, 0), at(OVERWORLD, 1500, 0)));
        assertTrue(policy.canHear(at(OVERWORLD, 0, 0), at(OVERWORLD, 1600, 0)));
        assertTrue(policy.canHear(at(OVERWORLD, 2000, -3000), at(OVERWORLD, 2900, -3200)));
    }

    @Test
    void playersOutsideTheRadiusDoNot() {
        BossSoundRangePolicy policy = policy(1600);

        assertFalse(policy.canHear(at(OVERWORLD, 0, 0), at(OVERWORLD, 1601, 0)));
        assertFalse(policy.canHear(at(OVERWORLD, 0, 0), at(OVERWORLD, 1200, 1200)));
        assertFalse(policy.canHear(at(OVERWORLD, 0, 0), at(OVERWORLD, 30000, 30000)));
    }

    @Test
    void heightCountsTowardsTheDistance() {
        BossSoundRangePolicy policy = policy(100);

        assertTrue(policy.canHear(
                new BossSoundRangePolicy.Position(OVERWORLD, 0, 0, 0),
                new BossSoundRangePolicy.Position(OVERWORLD, 0, 100, 0)));
        assertFalse(policy.canHear(
                new BossSoundRangePolicy.Position(OVERWORLD, 0, -64, 0),
                new BossSoundRangePolicy.Position(OVERWORLD, 0, 320, 0)));
    }

    @Test
    void anotherWorldNeverHearsIt() {
        BossSoundRangePolicy policy = policy(1600);

        // Nether coordinates sit right on top of overworld ones, so distance alone would let a
        // player standing next to a nether portal hear a wither charging up in the overworld.
        assertFalse(policy.canHear(at(OVERWORLD, 0, 0), at(NETHER, 0, 0)));
        assertFalse(policy.canHear(at(OVERWORLD, 100, 100), at(NETHER, 100, 100)));
    }

    @Test
    void distanceAloneDecidesWhenTheWorldIsUnknown() {
        BossSoundRangePolicy policy = policy(1600);

        assertTrue(policy.canHear(at(null, 0, 0), at(OVERWORLD, 800, 0)));
        assertFalse(policy.canHear(at(null, 0, 0), at(OVERWORLD, 4000, 0)));
        assertTrue(policy.canHear(at(OVERWORLD, 0, 0), at(null, 800, 0)));
        assertFalse(policy.canHear(at(OVERWORLD, 0, 0), at(null, 4000, 0)));
    }

    @Test
    void aMissingPositionLeavesTheSoundAlone() {
        BossSoundRangePolicy policy = policy(1600);

        assertTrue(policy.canHear(null, at(OVERWORLD, 30000, 30000)));
        assertTrue(policy.canHear(at(OVERWORLD, 0, 0), null));
    }

    @Test
    void theShippedConfigLimitsBothSoundsToAHundredChunks() throws Exception {
        YamlConfiguration config = new YamlConfiguration();
        config.loadFromString(Files.readString(
                new File("src/main/resources/config.yml").toPath(), StandardCharsets.UTF_8));

        BossSoundRangePolicy policy = BossSoundRangePolicy.fromConfig(config);

        assertTrue(policy.isActive());
        assertEquals(1600, policy.getRadius());
        assertTrue(policy.filters(BossSoundRangePolicy.WITHER_SPAWN_EFFECT_ID));
        assertTrue(policy.filters(BossSoundRangePolicy.ENDER_DRAGON_DEATH_EFFECT_ID));
    }

    @Test
    void configValuesAreReadBackOffTheKeysServerOwnersEdit() throws Exception {
        YamlConfiguration config = new YamlConfiguration();
        config.loadFromString(String.join("\n",
                "BOSS-SOUNDS:",
                "  ENABLED: true",
                "  RADIUS: 320",
                "  WITHER-SPAWN: false",
                "  ENDER-DRAGON-DEATH: true"));

        BossSoundRangePolicy policy = BossSoundRangePolicy.fromConfig(config);

        assertEquals(320, policy.getRadius());
        assertFalse(policy.filters(BossSoundRangePolicy.WITHER_SPAWN_EFFECT_ID));
        assertTrue(policy.filters(BossSoundRangePolicy.ENDER_DRAGON_DEATH_EFFECT_ID));
        assertTrue(policy.canHear(at(OVERWORLD, 0, 0), at(OVERWORLD, 300, 0)));
        assertFalse(policy.canHear(at(OVERWORLD, 0, 0), at(OVERWORLD, 400, 0)));
    }

    @Test
    void aMissingSectionFallsBackToTheShippedDefaults() {
        BossSoundRangePolicy policy = BossSoundRangePolicy.fromConfig(new YamlConfiguration());

        assertTrue(policy.isActive());
        assertEquals(1600, policy.getRadius());
    }
}
