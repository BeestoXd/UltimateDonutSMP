package com.bx.ultimateDonutSmp.managers;

import org.bukkit.entity.Player;

import java.util.UUID;

interface FakePlayerPacketBridge {

    Object createProfile(Player source, UUID fakeUuid, String profileName);

    Object createProfile(Player source, UUID fakeUuid, String profileName, TablistManager.SkinTexture texture);

    boolean hasSkinTexture(Object profile);

    void spawn(Player viewer, FakePlayerSession fakePlayer);

    void refreshPosition(Player viewer, FakePlayerSession fakePlayer);

    void playHitReaction(Player attacker, FakePlayerSession fakePlayer);

    void removeFromTablist(Player viewer, FakePlayerSession fakePlayer);

    void destroy(Player viewer, FakePlayerSession fakePlayer);

    default void shutdown() {
    }
}
