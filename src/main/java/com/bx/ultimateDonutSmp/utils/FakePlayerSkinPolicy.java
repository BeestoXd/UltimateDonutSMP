package com.bx.ultimateDonutSmp.utils;

/**
 * Decides whether a fakeplayer copies the staff member who spawned it, or uses Minecraft's default
 * Steve/Alex skin.
 *
 * <p>{@code REQUIRE-SKIN-TEXTURE} is a gate on copying that skin. It has nothing to copy when the
 * server asked for the default look, so it must not refuse the spawn in that case.
 */
public final class FakePlayerSkinPolicy {

    private FakePlayerSkinPolicy() {
    }

    public static boolean shouldCopyCreatorSkin(boolean useDefaultSkin) {
        return !useDefaultSkin;
    }

    public static boolean requireCreatorSkinTexture(boolean useDefaultSkin, boolean requireSkinTexture) {
        return shouldCopyCreatorSkin(useDefaultSkin) && requireSkinTexture;
    }
}
