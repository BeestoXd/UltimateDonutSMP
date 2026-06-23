package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.models.HideMode;
import com.bx.ultimateDonutSmp.models.HideState;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HideIdentityPolicyTest {

    @Test
    void bundledConfigurationIsValid() {
        var stream = getClass().getClassLoader().getResourceAsStream("hide.yml");
        var config = YamlConfiguration.loadConfiguration(
                new InputStreamReader(stream, StandardCharsets.UTF_8)
        );

        assertTrue(HideIdentityPolicy.validate(config).isEmpty());
    }

    @Test
    void scrambleGeneratorUsesOnlyValidConfiguredCharacters() {
        String alias = HideIdentityPolicy.generateAlias(new Random(7L), "AB_-$A", 12, 16);

        assertEquals(12, alias.length());
        assertTrue(alias.matches("[AB_]+"));
        assertTrue(HideIdentityPolicy.isValidProfileName(alias, 16));
        assertFalse(HideIdentityPolicy.isValidProfileName("bad-name", 16));
        assertFalse(HideIdentityPolicy.isValidProfileName("12345678901234567", 16));
    }

    @Test
    void cooldownRoundsUpAndExpires() {
        assertEquals(30L, HideIdentityPolicy.cooldownRemaining(1_000L, 1_001L, 30L));
        assertEquals(1L, HideIdentityPolicy.cooldownRemaining(1_000L, 30_001L, 30L));
        assertEquals(0L, HideIdentityPolicy.cooldownRemaining(1_000L, 31_000L, 30L));
    }

    @Test
    void realNameLookupRequiresBypass() {
        HideState state = state(UUID.randomUUID(), "RealPlayer", "HiddenAlias");

        assertTrue(HideIdentityPolicy.matchesState(state, "HiddenAlias", false));
        assertFalse(HideIdentityPolicy.matchesState(state, "RealPlayer", false));
        assertTrue(HideIdentityPolicy.matchesState(state, "RealPlayer", true));
    }

    @Test
    void scrambleDisplayNameUsesObfuscatedEffectWithoutChangingAlias() {
        HideState scramble = state(UUID.randomUUID(), "RealPlayer", "HiddenAlias");
        HideState disguise = new HideState(
                UUID.randomUUID(), "RealPlayer", HideMode.DISGUISE,
                "Dream", HideManager.normalize("Dream"),
                "dream", "Dream", "", "", 1L, 1L
        );
        HideState urlOnlyDisguise = new HideState(
                UUID.randomUUID(), "RealPlayer", HideMode.DISGUISE,
                "RandomAlias", HideManager.normalize("RandomAlias"),
                "obfuscated_custom_url", "https://example.com/skin.png", "", "", 1L, 1L
        );

        assertEquals("&kHiddenAlias&r", HideIdentityPolicy.formatPublicName(scramble, "", true));
        assertEquals("HiddenAlias", HideIdentityPolicy.formatPublicName(scramble, "", false));
        assertEquals("Dream", HideIdentityPolicy.formatPublicName(disguise, "", true));
        assertEquals("&kRandomAlias&r", HideIdentityPolicy.formatPublicName(urlOnlyDisguise, "", true));
        assertEquals("Fallback", HideIdentityPolicy.formatPublicName(null, "Fallback", true));
        assertEquals("HiddenAlias", scramble.alias());
    }

    @Test
    void skinUrlDetectionOnlyAcceptsHttpUrls() {
        assertTrue(HideManager.isSkinUrl("https://example.com/skin.png"));
        assertTrue(HideManager.isSkinUrl("HTTP://example.com/skin.png"));
        assertFalse(HideManager.isSkinUrl("Dream"));
        assertFalse(HideManager.isSkinUrl(null));
    }

    @Test
    void permissionCombatAndDependencyChecksHaveStablePriority() {
        assertEquals(HideManager.ResultType.DISABLED,
                HideManager.evaluateChange(false, false, false, true, true, false, false));
        assertEquals(HideManager.ResultType.DEPENDENCY_MISSING,
                HideManager.evaluateChange(true, false, true, false, false, false, false));
        assertEquals(HideManager.ResultType.NO_PERMISSION,
                HideManager.evaluateChange(true, true, false, true, false, false, false));
        assertEquals(HideManager.ResultType.IN_COMBAT,
                HideManager.evaluateChange(true, true, true, true, true, false, false));
        assertEquals(HideManager.ResultType.COOLDOWN,
                HideManager.evaluateChange(true, true, true, false, true, false, false));
        assertEquals(HideManager.ResultType.NOT_HIDDEN,
                HideManager.evaluateChange(true, true, true, false, false, true, false));
        assertNull(HideManager.evaluateChange(true, true, true, false, false, true, true));
    }

    private HideState state(UUID uuid, String realName, String alias) {
        return new HideState(
                uuid,
                realName,
                HideMode.SCRAMBLE,
                alias,
                HideManager.normalize(alias),
                "",
                "",
                "",
                "",
                1L,
                1L
        );
    }
}
