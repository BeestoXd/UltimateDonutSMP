package com.bx.ultimateDonutSmp.listeners;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CombatListenerTest {

    @Test
    void playerDamageAlwaysTagsCombat() {
        assertTrue(CombatListener.shouldTagVictim(
                CombatListener.DamageSource.PLAYER,
                false,
                false
        ));
    }

    @Test
    void mobAndCrystalDamageRespectTheirToggles() {
        assertFalse(CombatListener.shouldTagVictim(
                CombatListener.DamageSource.MOB,
                false,
                true
        ));
        assertTrue(CombatListener.shouldTagVictim(
                CombatListener.DamageSource.MOB,
                true,
                false
        ));
        assertFalse(CombatListener.shouldTagVictim(
                CombatListener.DamageSource.ENDER_CRYSTAL,
                true,
                false
        ));
        assertTrue(CombatListener.shouldTagVictim(
                CombatListener.DamageSource.ENDER_CRYSTAL,
                false,
                true
        ));
    }

    @Test
    void unknownEntityDamageDoesNotTagCombat() {
        assertFalse(CombatListener.shouldTagVictim(
                CombatListener.DamageSource.OTHER,
                true,
                true
        ));
    }

    /**
     * Every path CombatManager and CombatListener read off COMBAT-MANAGER. A key the code reads
     * but the bundled config never ships is invisible to admins and silently keeps its java
     * fallback, so the feature behind it can never be switched on.
     */
    private static final List<String> COMBAT_PATHS = List.of(
            "COMBAT-MANAGER.ENABLED",
            "COMBAT-MANAGER.COOLDOWN",
            "COMBAT-MANAGER.KILL-ON-LOGOUT",
            "COMBAT-MANAGER.ACTION-BAR",
            "COMBAT-MANAGER.MOBS",
            "COMBAT-MANAGER.ENDER-CRYSTAL",
            "COMBAT-MANAGER.ENDER-PEARL",
            "COMBAT-MANAGER.RESPAWN-ANCHOR",
            "COMBAT-MANAGER.BLOCK-MESSAGE",
            "COMBAT-MANAGER.BLOCK-COMMANDS",
            "COMBAT-MANAGER.EXCLUDED-WORLDS"
    );

    @Test
    void bundledConfigShipsEveryCombatPathTheCodeReads() {
        YamlConfiguration config = bundledConfig();
        for (String path : COMBAT_PATHS) {
            assertTrue(
                    config.contains(path),
                    "config.yml must ship " + path + " or admins cannot find the setting"
            );
        }
    }

    @Test
    void bundledConfigDisablesMobCombatByDefault() {
        YamlConfiguration config = bundledConfig();
        assertTrue(config.isBoolean("COMBAT-MANAGER.MOBS"));
        assertFalse(config.getBoolean("COMBAT-MANAGER.MOBS"));
    }

    @Test
    void bundledConfigDisablesKillOnLogoutByDefault() {
        YamlConfiguration config = bundledConfig();
        assertTrue(config.isBoolean("COMBAT-MANAGER.KILL-ON-LOGOUT"));
        assertFalse(config.getBoolean("COMBAT-MANAGER.KILL-ON-LOGOUT"));
    }

    private static YamlConfiguration bundledConfig() {
        var stream = CombatListenerTest.class.getClassLoader().getResourceAsStream("config.yml");
        assertNotNull(stream);

        return YamlConfiguration.loadConfiguration(
                new InputStreamReader(stream, StandardCharsets.UTF_8)
        );
    }
}
