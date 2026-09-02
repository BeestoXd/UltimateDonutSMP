package com.bx.ultimateDonutSmp.managers;

import org.bukkit.GameMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpawnerCreativeBreakTest {

    @Test
    void everyGameModeThatPaysForASpawnerIsTheOneThatGetsItBack() {
        for (GameMode gameMode : GameMode.values()) {
            assertEquals(
                    SpawnerManager.consumesSpawnerItemOnPlace(gameMode),
                    SpawnerManager.returnsSpawnerItemOnBreak(gameMode),
                    gameMode + " pays for a spawner and gets one back on different terms"
            );
        }
    }

    @Test
    void creativeNeitherPaysForASpawnerNorGetsOneBack() {
        assertFalse(SpawnerManager.consumesSpawnerItemOnPlace(GameMode.CREATIVE));
        assertFalse(SpawnerManager.returnsSpawnerItemOnBreak(GameMode.CREATIVE));
    }

    @Test
    void survivalStillPaysForASpawnerAndStillGetsItBack() {
        assertTrue(SpawnerManager.consumesSpawnerItemOnPlace(GameMode.SURVIVAL));
        assertTrue(SpawnerManager.returnsSpawnerItemOnBreak(GameMode.SURVIVAL));
    }
}
