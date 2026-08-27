package com.bx.ultimateDonutSmp.managers;

import org.junit.jupiter.api.Test;

import sun.reflect.ReflectionFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

class ShardHudStateFallbackTest {

    @SuppressWarnings("unchecked")
    private ShardManager managerWith(Map<UUID, ShardManager.ShardCuboidHudState> states) throws Exception {
        // The real constructor needs a live server; getHudState only reads the map.
        Constructor<Object> objectConstructor = Object.class.getConstructor();
        Constructor<?> managerConstructor = ReflectionFactory.getReflectionFactory()
                .newConstructorForSerialization(ShardManager.class, objectConstructor);
        ShardManager manager = (ShardManager) managerConstructor.newInstance();

        Field hudStates = ShardManager.class.getDeclaredField("hudStates");
        hudStates.setAccessible(true);
        hudStates.set(manager, states);
        return manager;
    }

    @Test
    void reusesOneFallbackForPlayersWithoutState() throws Exception {
        ShardManager manager = managerWith(new HashMap<>());

        ShardManager.ShardCuboidHudState first = manager.getHudState(UUID.randomUUID());
        ShardManager.ShardCuboidHudState second = manager.getHudState(UUID.randomUUID());

        // The sidebar asks once per player per pass, so the fallback must not be rebuilt each time.
        assertSame(first, second);
    }

    @Test
    void fallbackKeepsItsValues() throws Exception {
        ShardManager manager = managerWith(new HashMap<>());
        ShardManager.ShardCuboidHudState state = manager.getHudState(UUID.randomUUID());

        assertEquals("none", state.cuboidName());
        assertEquals("outside", state.status());
        assertEquals("-", state.display());
        assertEquals(0, state.remainingSeconds());
        assertFalse(state.visible());
    }

    @Test
    void stillReturnsTheStoredStateWhenThereIsOne() throws Exception {
        UUID uuid = UUID.randomUUID();
        ShardManager.ShardCuboidHudState stored =
                new ShardManager.ShardCuboidHudState("mine", "inside", "42s", 42, true);
        Map<UUID, ShardManager.ShardCuboidHudState> states = new HashMap<>();
        states.put(uuid, stored);

        ShardManager manager = managerWith(states);

        assertSame(stored, manager.getHudState(uuid));
        assertEquals(true, manager.shouldShowShardCuboidLine(uuid));
        assertFalse(manager.shouldShowShardCuboidLine(UUID.randomUUID()));
    }
}
