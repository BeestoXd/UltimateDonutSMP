package com.bx.ultimateDonutSmp.managers;

import org.bukkit.Location;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FakePlayerLookAndSneakTest {

    @Test
    void sneakingShortensTheEyeAndHitbox() {
        Location feet = new Location(null, 3.0D, 64.0D, 7.0D);
        FakePlayerSession standing = session(feet, false);
        FakePlayerSession sneaking = session(feet, true);

        assertFalse(standing.sneaking());
        assertTrue(sneaking.sneaking());
        assertEquals(1.62D, standing.eyeHeight(), 0.0001D);
        assertEquals(1.27D, sneaking.eyeHeight(), 0.0001D);
        assertEquals(65.62D, standing.eyeLocation().getY(), 0.0001D);
        assertEquals(65.27D, sneaking.eyeLocation().getY(), 0.0001D);
        assertEquals(0.9D, standing.hitboxHalfHeight(), 0.0001D);
        assertEquals(0.75D, sneaking.hitboxHalfHeight(), 0.0001D);
    }

    private static FakePlayerSession session(Location feet, boolean sneaking) {
        return new FakePlayerSession(
                1L,
                2,
                UUID.fromString("12345678-1234-1234-1234-123456789abc"),
                null,
                UUID.fromString("abcdefab-cdef-abcd-efab-cdefabcdefab"),
                "staff",
                "fp123456789abcde",
                "staff",
                feet,
                sneaking,
                0L,
                1L
        );
    }
}
