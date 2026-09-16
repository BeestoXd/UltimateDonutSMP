package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

class ExplosionParticleFilterSoundTest {

    private static Server originalServer;
    private static final Map<NamespacedKey, Object> SOUND_MAP = new ConcurrentHashMap<>();

    static {
        try {
            originalServer = Bukkit.getServer();
            Server mockServer = (Server) Proxy.newProxyInstance(
                    Server.class.getClassLoader(),
                    new Class<?>[]{Server.class},
                    (proxy, method, args) -> {
                        if ("getRegistry".equals(method.getName()) && args != null && args.length == 1) {
                            Class<?> regType = (Class<?>) args[0];
                            return Proxy.newProxyInstance(
                                    Server.class.getClassLoader(),
                                    new Class<?>[]{method.getReturnType()},
                                    (rProxy, rMethod, rArgs) -> {
                                        if ("getOrThrow".equals(rMethod.getName()) && rArgs != null && rArgs.length == 1) {
                                            NamespacedKey rawKey = (NamespacedKey) rArgs[0];
                                            NamespacedKey key = NamespacedKey.minecraft(rawKey.getKey().replace('_', '.').toLowerCase());
                                            return SOUND_MAP.computeIfAbsent(key, k -> Proxy.newProxyInstance(
                                                    Server.class.getClassLoader(),
                                                    new Class<?>[]{regType},
                                                    (sProxy, sMethod, sArgs) -> {
                                                        if ("getKey".equals(sMethod.getName())) return k;
                                                        if ("name".equals(sMethod.getName())) {
                                                            return k.getKey().replace('.', '_').toUpperCase();
                                                        }
                                                        if ("toString".equals(sMethod.getName())) return k.toString();
                                                        if ("equals".equals(sMethod.getName()) && sArgs != null && sArgs.length == 1) {
                                                            return sProxy == sArgs[0];
                                                        }
                                                        if ("hashCode".equals(sMethod.getName())) return k.hashCode();
                                                        return null;
                                                    }
                                            ));
                                        }
                                        if ("get".equals(rMethod.getName()) && rArgs != null && rArgs.length == 1) {
                                            NamespacedKey rawKey = (NamespacedKey) rArgs[0];
                                            NamespacedKey key = NamespacedKey.minecraft(rawKey.getKey().replace('_', '.').toLowerCase());
                                            return SOUND_MAP.get(key);
                                        }
                                        return null;
                                    }
                            );
                        }
                        return null;
                    }
            );

            Field field = Bukkit.class.getDeclaredField("server");
            field.setAccessible(true);
            field.set(null, mockServer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @AfterAll
    static void tearDownAll() throws Exception {
        Field field = Bukkit.class.getDeclaredField("server");
        field.setAccessible(true);
        field.set(null, originalServer);
    }

    @Test
    void directHolderThrowingIllegalStateExceptionDoesNotEscapeExtractSoundName() {
        // Simulates Paper's OldEnumHolderable throwing IllegalStateException on unregistered / direct holders
        Sound unregisteredSound = (Sound) Proxy.newProxyInstance(
                Sound.class.getClassLoader(),
                new Class<?>[]{Sound.class},
                (proxy, method, args) -> {
                    if ("name".equals(method.getName())) {
                        throw new IllegalStateException("Cannot call method for this registry item, because it is not registered.");
                    }
                    if ("getKey".equals(method.getName())) {
                        return NamespacedKey.minecraft("entity.generic.explode");
                    }
                    if ("toString".equals(method.getName())) {
                        throw new IllegalStateException("Cannot call method for this registry item, because it is not registered.");
                    }
                    return null;
                }
        );

        String soundName = ExplosionParticleFilter.extractSoundName(unregisteredSound);
        assertEquals("minecraft:entity.generic.explode", soundName);
        assertTrue(ExplosionParticleFilter.isExplosionSound(soundName));
    }

    @Test
    void completelyUnregisteredSoundWithoutKeyReturnsEmptySafely() {
        Sound brokenSound = (Sound) Proxy.newProxyInstance(
                Sound.class.getClassLoader(),
                new Class<?>[]{Sound.class},
                (proxy, method, args) -> {
                    if ("name".equals(method.getName())) {
                        throw new IllegalStateException("Cannot call method for this registry item, because it is not registered.");
                    }
                    if ("getKey".equals(method.getName())) {
                        throw new IllegalStateException("Cannot call method for this registry item, because it is not registered.");
                    }
                    if ("toString".equals(method.getName())) {
                        throw new IllegalStateException("Cannot call method for this registry item, because it is not registered.");
                    }
                    return null;
                }
        );

        assertDoesNotThrow(() -> {
            String soundName = ExplosionParticleFilter.extractSoundName(brokenSound);
            assertEquals("", soundName);
            assertFalse(ExplosionParticleFilter.isExplosionSound(soundName));
        });
    }

    @Test
    void directHolderStringRepresentationIdentifiesExplosions() {
        String directExplosion = "Direct{SoundEvent[location=minecraft:entity.generic.explode, fixedRange=Optional.empty]}";
        String directButtonClick = "Direct{SoundEvent[location=minecraft:ui.button.click, fixedRange=Optional.empty]}";
        String directTeleport = "Direct{SoundEvent[location=minecraft:entity.enderman.teleport, fixedRange=Optional.empty]}";

        assertTrue(ExplosionParticleFilter.isExplosionSound(directExplosion));
        assertFalse(ExplosionParticleFilter.isExplosionSound(directButtonClick));
        assertFalse(ExplosionParticleFilter.isExplosionSound(directTeleport));
        assertFalse(ExplosionParticleFilter.isExplosionSound(""));
        assertFalse(ExplosionParticleFilter.isExplosionSound(null));
    }

    @Test
    void parseSoundResolvesVanillaSoundKeys() {
        assertEquals(Sound.UI_BUTTON_CLICK, SoundUtils.parseSound("minecraft:ui.button.click"));
        assertEquals(Sound.ENTITY_SLIME_JUMP, SoundUtils.parseSound("minecraft:entity.slime.jump"));
        assertEquals(Sound.ENTITY_ENDERMAN_TELEPORT, SoundUtils.parseSound("minecraft:entity.enderman.teleport"));
        assertEquals(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundUtils.parseSound("minecraft:entity.experience_orb.pickup"));
        assertEquals(Sound.UI_BUTTON_CLICK, SoundUtils.parseSound("UI_BUTTON_CLICK"));
        assertNull(SoundUtils.parseSound("custom:custom_pack_sound"));
        assertNull(SoundUtils.parseSound(""));
        assertNull(SoundUtils.parseSound(null));
    }
}
