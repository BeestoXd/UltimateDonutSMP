package com.bx.ultimateDonutSmp.listeners;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

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

    @Test
    void bundledConfigDisablesMobCombatByDefault() {
        var stream = CombatListenerTest.class.getClassLoader().getResourceAsStream("config.yml");
        assertNotNull(stream);

        YamlConfiguration config = YamlConfiguration.loadConfiguration(
                new InputStreamReader(stream, StandardCharsets.UTF_8)
        );
        assertFalse(config.getBoolean("COMBAT-MANAGER.MOBS"));
    }
}
