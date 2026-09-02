package com.bx.ultimateDonutSmp.managers;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HideNametagOffsetTest {

    private static final double BUNDLED_DEFAULT = 1.0D;

    @Test
    void liftedByRaisesTheTextAndKeepsTheRestOfTheTransformation() {
        Quaternionf left = new Quaternionf(0.0F, 0.5F, 0.0F, 0.5F);
        Quaternionf right = new Quaternionf(0.0F, 0.0F, 0.25F, 1.0F);
        Vector3f scale = new Vector3f(0.6F, 0.6F, 0.6F);
        Transformation current = new Transformation(new Vector3f(0.0F, 0.0F, 0.0F), left, scale, right);

        Transformation lifted = HideProtocolLibBridge.liftedBy(current, 1.25D);

        assertEquals(1.25F, lifted.getTranslation().y());
        assertEquals(0.0F, lifted.getTranslation().x());
        assertEquals(0.0F, lifted.getTranslation().z());
        assertEquals(scale, lifted.getScale());
        assertEquals(left, lifted.getLeftRotation());
        assertEquals(right, lifted.getRightRotation());
    }

    @Test
    void liftedByReplacesAnOffsetItHasAlreadyApplied() {
        Transformation once = HideProtocolLibBridge.liftedBy(identity(), BUNDLED_DEFAULT);
        Transformation twice = HideProtocolLibBridge.liftedBy(once, BUNDLED_DEFAULT);

        assertEquals((float) BUNDLED_DEFAULT, twice.getTranslation().y());
    }

    @Test
    void bundledHideConfigCarriesTheOffsetTheCodeFallsBackTo() throws Exception {
        YamlConfiguration hide = new YamlConfiguration();
        hide.load(new File("src/main/resources/hide.yml"));

        assertTrue(hide.contains("SCRAMBLE.NAMETAG-OFFSET-Y"));
        assertEquals(BUNDLED_DEFAULT, hide.getDouble("SCRAMBLE.NAMETAG-OFFSET-Y"));
        assertTrue(hide.getBoolean("SCRAMBLE.OBFUSCATED"));
    }

    private static Transformation identity() {
        return new Transformation(
                new Vector3f(0.0F, 0.0F, 0.0F),
                new Quaternionf(),
                new Vector3f(1.0F, 1.0F, 1.0F),
                new Quaternionf()
        );
    }
}
