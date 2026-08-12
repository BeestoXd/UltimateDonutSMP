package com.bx.ultimateDonutSmp.utils;

import com.bx.ultimateDonutSmp.models.PlayerData;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MobSpawnPolicyTest {

    @Test
    void onlyVanillaSpawnerReasonReceivesPersistentExemption() {
        assertTrue(MobSpawnPolicy.isVanillaSpawnerSpawn(CreatureSpawnEvent.SpawnReason.SPAWNER));
        assertFalse(MobSpawnPolicy.isVanillaSpawnerSpawn(CreatureSpawnEvent.SpawnReason.NATURAL));
        assertFalse(MobSpawnPolicy.isVanillaSpawnerSpawn(CreatureSpawnEvent.SpawnReason.CUSTOM));
    }

    @Test
    void periodicCleanupPreservesSpawnerMobsAndExistingExcludedTypes() {
        assertTrue(MobSpawnPolicy.shouldRemoveFromPeriodicCleanup(true, EntityType.ZOMBIE, false));
        assertFalse(MobSpawnPolicy.shouldRemoveFromPeriodicCleanup(true, EntityType.ZOMBIE, true));
        assertFalse(MobSpawnPolicy.shouldRemoveFromPeriodicCleanup(true, EntityType.ZOMBIE, false, true));
        assertFalse(MobSpawnPolicy.shouldRemoveFromPeriodicCleanup(false, EntityType.ZOMBIE, false));

        assertFalse(MobSpawnPolicy.shouldRemoveFromPeriodicCleanup(true, EntityType.PHANTOM, false));
        assertFalse(MobSpawnPolicy.shouldRemoveFromPeriodicCleanup(true, EntityType.WITHER, false));
        assertFalse(MobSpawnPolicy.shouldRemoveFromPeriodicCleanup(true, EntityType.ENDER_DRAGON, false));
        assertFalse(MobSpawnPolicy.shouldRemoveFromPeriodicCleanup(true, EntityType.ELDER_GUARDIAN, false));
        assertFalse(MobSpawnPolicy.shouldRemoveFromPeriodicCleanup(true, EntityType.WARDEN, false));

        assertTrue(MobSpawnPolicy.shouldRemoveFromPeriodicCleanup(true, EntityType.SLIME, false));
        assertTrue(MobSpawnPolicy.shouldRemoveFromPeriodicCleanup(true, EntityType.GHAST, false));
        assertTrue(MobSpawnPolicy.shouldRemoveFromPeriodicCleanup(true, EntityType.HOGLIN, false));
        assertTrue(MobSpawnPolicy.shouldRemoveFromPeriodicCleanup(true, EntityType.SPIDER, false));
        assertTrue(MobSpawnPolicy.shouldRemoveFromPeriodicCleanup(true, EntityType.CAVE_SPIDER, false));
    }

    @Test
    void nearestPlayerMobSpawnOnButFartherPlayerMobSpawnOffCancelsSpawn() {
        Location spawnLoc = new Location(null, 0, 0, 0);

        Player playerA = createMockPlayer(new Location(null, 10, 0, 0));
        Player playerB = createMockPlayer(new Location(null, 30, 0, 0));

        PlayerData dataA = new PlayerData(UUID.randomUUID(), "PlayerA");
        dataA.setMobSpawnEnabled(true);

        PlayerData dataB = new PlayerData(UUID.randomUUID(), "PlayerB");
        dataB.setMobSpawnEnabled(false);

        Map<Player, PlayerData> dataMap = Map.of(playerA, dataA, playerB, dataB);

        // Player A is closer (10m) & ON, but Player B is within 50m & OFF.
        // Spawn MUST be cancelled!
        assertTrue(MobSpawnPolicy.shouldCancelMobSpawn(spawnLoc, List.of(playerA, playerB), 50.0, dataMap::get));
    }

    @Test
    void allPlayersMobSpawnOnAllowsSpawn() {
        Location spawnLoc = new Location(null, 0, 0, 0);

        Player playerA = createMockPlayer(new Location(null, 10, 0, 0));
        PlayerData dataA = new PlayerData(UUID.randomUUID(), "PlayerA");
        dataA.setMobSpawnEnabled(true);

        Map<Player, PlayerData> dataMap = Map.of(playerA, dataA);

        assertFalse(MobSpawnPolicy.shouldCancelMobSpawn(spawnLoc, List.of(playerA), 50.0, dataMap::get));
    }

    @Test
    void playerMobSpawnOffOutsideRadiusAllowsSpawn() {
        Location spawnLoc = new Location(null, 0, 0, 0);

        Player playerB = createMockPlayer(new Location(null, 60, 0, 0));
        PlayerData dataB = new PlayerData(UUID.randomUUID(), "PlayerB");
        dataB.setMobSpawnEnabled(false);

        Map<Player, PlayerData> dataMap = Map.of(playerB, dataB);

        assertFalse(MobSpawnPolicy.shouldCancelMobSpawn(spawnLoc, List.of(playerB), 50.0, dataMap::get));
    }

    private Player createMockPlayer(Location location) {
        return (Player) Proxy.newProxyInstance(
                Player.class.getClassLoader(),
                new Class<?>[]{Player.class},
                (proxy, method, args) -> {
                    if ("getLocation".equals(method.getName())) {
                        return location;
                    }
                    if ("equals".equals(method.getName()) && args != null && args.length == 1) {
                        return proxy == args[0];
                    }
                    if ("hashCode".equals(method.getName())) {
                        return System.identityHashCode(proxy);
                    }
                    return null;
                }
        );
    }
}
