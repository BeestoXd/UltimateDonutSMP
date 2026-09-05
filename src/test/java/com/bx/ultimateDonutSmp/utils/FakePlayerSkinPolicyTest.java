package com.bx.ultimateDonutSmp.utils;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FakePlayerSkinPolicyTest {

    @Test
    void stockSettingsStillCopyTheStaffMember() {
        assertTrue(FakePlayerSkinPolicy.shouldCopyCreatorSkin(false));
        assertTrue(FakePlayerSkinPolicy.requireCreatorSkinTexture(false, true));
    }

    @Test
    void forcingTheDefaultSkinSkipsTheCreatorAndTheTextureGate() {
        assertFalse(FakePlayerSkinPolicy.shouldCopyCreatorSkin(true));
        assertFalse(FakePlayerSkinPolicy.requireCreatorSkinTexture(true, true));
        assertFalse(FakePlayerSkinPolicy.requireCreatorSkinTexture(true, false));
    }

    @Test
    void theTextureGateStillAppliesWhenTheCreatorSkinIsWanted() {
        assertFalse(FakePlayerSkinPolicy.requireCreatorSkinTexture(false, false));
        assertTrue(FakePlayerSkinPolicy.requireCreatorSkinTexture(false, true));
    }

    @Test
    void bundledStaffModeLeavesCreatorSkinsOn() throws Exception {
        YamlConfiguration config = new YamlConfiguration();
        config.load(Path.of("src/main/resources", "staff-mode.yml").toFile());

        assertFalse(config.getBoolean("FAKE-PLAYER.USE-DEFAULT-SKIN", true),
                "staff-mode.yml must ship USE-DEFAULT-SKIN as false so a config restore keeps copying the staff member");
    }
}
