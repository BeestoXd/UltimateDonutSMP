package com.bx.ultimateDonutSmp.managers;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CrateHologramSpacingTest {

    private static final double EPSILON = 1.0e-9;

    @Test
    void cratesShipTheSameLineSpacingThePortalHologramUses() throws Exception {
        YamlConfiguration crates = load("crates.yml");
        YamlConfiguration config = load("config.yml");

        double crateSpacing = crates.getDouble("SETTINGS.HOLOGRAM.LINE-SPACING", -1);
        double portalSpacing = config.getDouble("PORTAL-SYSTEM.HOLOGRAM.LINE-SPACING", -1);

        assertEquals(
                CrateVisualManager.DEFAULT_HOLOGRAM_LINE_SPACING,
                crateSpacing,
                EPSILON,
                "crates.yml should ship the documented default"
        );
        assertEquals(
                portalSpacing,
                crateSpacing,
                EPSILON,
                "the two holograms should read the same out of the box"
        );
    }

    @Test
    void theBundledSpacingIsInsideTheRangeTheClampAllows() {
        assertEquals(
                CrateVisualManager.DEFAULT_HOLOGRAM_LINE_SPACING,
                CrateVisualManager.clampLineSpacing(CrateVisualManager.DEFAULT_HOLOGRAM_LINE_SPACING),
                EPSILON,
                "shipping a default the clamp moves would change every server's holograms"
        );
    }

    @Test
    void aSpacingThatWouldStackEveryLineOnOneSpotIsPulledUp() {
        assertEquals(
                CrateVisualManager.MIN_HOLOGRAM_LINE_SPACING,
                CrateVisualManager.clampLineSpacing(0),
                EPSILON
        );
        assertEquals(
                CrateVisualManager.MIN_HOLOGRAM_LINE_SPACING,
                CrateVisualManager.clampLineSpacing(-5),
                EPSILON,
                "a negative gap would build the stack upside down"
        );
    }

    @Test
    void aSpacingWiderThanTheSweepsReachIsPulledDown() {
        assertEquals(
                CrateVisualManager.MAX_HOLOGRAM_LINE_SPACING,
                CrateVisualManager.clampLineSpacing(40),
                EPSILON,
                "text spread past the cleanup sweep would be stranded when a crate is unbound"
        );
    }

    @Test
    void aSpacingInsideTheRangeIsLeftAlone() {
        assertEquals(0.4D, CrateVisualManager.clampLineSpacing(0.4D), EPSILON);
        assertEquals(
                CrateVisualManager.DEFAULT_HOLOGRAM_LINE_SPACING,
                CrateVisualManager.clampLineSpacing(Double.NaN),
                EPSILON,
                "an unreadable value should fall back rather than poison the maths"
        );
    }

    @Test
    void everyStackTheClampAllowsStaysInsideTheNarrowestSweep() throws Exception {
        // The disable-time purge is the shallowest of the sweeps at 2.5 blocks, and the personal
        // key line hangs a full gap below the last hologram line.
        int bundledLines = load("crates.yml").getStringList("SETTINGS.HOLOGRAM.LINES").size();
        double deepest = (bundledLines + 1) * CrateVisualManager.MAX_HOLOGRAM_LINE_SPACING;

        assertTrue(
                deepest <= 2.5D,
                "the bundled line count at the widest allowed gap reaches " + deepest
                        + " blocks, past the 2.5 the shallowest sweep covers"
        );
    }

    private static YamlConfiguration load(String resource) throws Exception {
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.load(Path.of("src/main/resources", resource).toFile());
        return configuration;
    }
}
