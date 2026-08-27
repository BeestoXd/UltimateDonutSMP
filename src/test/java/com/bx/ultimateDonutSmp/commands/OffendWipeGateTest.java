package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.models.PunishmentType;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The wipe flag in offenses.yml is only meant to fire on a ban, so a preset that mutes or warns
 * keeps the account even with the flag turned on.
 */
class OffendWipeGateTest {

    @Test
    void bansAndBlacklistsWipe() throws Exception {
        assertTrue(isBan(PunishmentType.BAN));
        assertTrue(isBan(PunishmentType.BLACKLIST));
    }

    @Test
    void lesserPunishmentsDoNotWipe() throws Exception {
        assertFalse(isBan(PunishmentType.MUTE));
        assertFalse(isBan(PunishmentType.KICK));
        assertFalse(isBan(PunishmentType.WARN), "a 0s tier is issued as a warning and must keep the account");
    }

    private boolean isBan(PunishmentType type) throws Exception {
        Method method = OffendCommand.class.getDeclaredMethod("isBan", PunishmentType.class);
        method.setAccessible(true);
        return (boolean) method.invoke(null, type);
    }
}
