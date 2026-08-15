package com.bx.ultimateDonutSmp.utils;

import com.bx.ultimateDonutSmp.models.PlayerData;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MobSpawnPolicyTest {

    @Test
    void bossesAreNeverBlockedByTheToggle() {
        assertTrue(MobSpawnPolicy.isBoss(EntityType.WITHER));
        assertTrue(MobSpawnPolicy.isBoss(EntityType.ENDER_DRAGON));
        assertTrue(MobSpawnPolicy.isBoss(EntityType.ELDER_GUARDIAN));
        assertTrue(MobSpawnPolicy.isBoss(EntityType.WARDEN));

        assertFalse(MobSpawnPolicy.isBoss(null));
        assertFalse(MobSpawnPolicy.isBoss(EntityType.ZOMBIE));
        assertFalse(MobSpawnPolicy.isBoss(EntityType.SLIME));
        assertFalse(MobSpawnPolicy.isBoss(EntityType.GHAST));
        assertFalse(MobSpawnPolicy.isBoss(EntityType.HOGLIN));
        assertFalse(MobSpawnPolicy.isBoss(EntityType.SPIDER));
        assertFalse(MobSpawnPolicy.isBoss(EntityType.CAVE_SPIDER));
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

    @Test
    void nonPositiveRadiusDisablesTheFilter() {
        Location spawnLoc = new Location(null, 0, 0, 0);

        Player playerB = createMockPlayer(new Location(null, 0, 0, 0));
        PlayerData dataB = new PlayerData(UUID.randomUUID(), "PlayerB");
        dataB.setMobSpawnEnabled(false);

        Map<Player, PlayerData> dataMap = Map.of(playerB, dataB);

        assertFalse(MobSpawnPolicy.shouldCancelMobSpawn(spawnLoc, List.of(playerB), 0.0, dataMap::get));
        assertFalse(MobSpawnPolicy.shouldCancelPhantomSpawn(spawnLoc, List.of(playerB), 0.0, dataMap::get));
    }

    @Test
    void chunkPreFilterNeverDropsAPlayerInsideTheRadius() {
        PlayerData disabled = new PlayerData(UUID.randomUUID(), "Disabled");
        disabled.setMobSpawnEnabled(false);

        // Walk the whole radius on each axis: the integer chunk pre-filter must not reject any
        // position that the squared-distance check would still consider in range.
        for (double radius : new double[]{1.0, 15.0, 16.0, 50.0, 300.0}) {
            for (double offset = 0.0; offset <= radius; offset += 0.5) {
                Player player = createMockPlayer(new Location(null, offset, 0, offset));
                Map<Player, PlayerData> dataMap = Map.of(player, disabled);

                boolean inRange = (offset * offset * 2) <= radius * radius;
                assertEquals(
                        inRange,
                        MobSpawnPolicy.shouldCancelMobSpawn(
                                new Location(null, 0, 0, 0), List.of(player), radius, dataMap::get),
                        "radius=" + radius + " offset=" + offset
                );
            }
        }
    }

    @Test
    void eachPlayerSettingIsEvaluatedIndependently() {
        Location spawnLoc = new Location(null, 0, 0, 0);

        Player near = createMockPlayer(new Location(null, 5, 0, 0));
        Player far = createMockPlayer(new Location(null, 500, 0, 0));

        PlayerData nearOn = new PlayerData(UUID.randomUUID(), "NearOn");
        nearOn.setMobSpawnEnabled(true);
        PlayerData farOff = new PlayerData(UUID.randomUUID(), "FarOff");
        farOff.setMobSpawnEnabled(false);

        Map<Player, PlayerData> dataMap = Map.of(near, nearOn, far, farOff);

        // The only player with the toggle OFF sits outside the radius, so the spawn is allowed even
        // though a player is standing right on top of the spawn location.
        assertFalse(MobSpawnPolicy.shouldCancelMobSpawn(spawnLoc, List.of(near, far), 50.0, dataMap::get));

        // Flip the near player OFF and the same spawn is now blocked by that player alone.
        nearOn.setMobSpawnEnabled(false);
        assertTrue(MobSpawnPolicy.shouldCancelMobSpawn(spawnLoc, List.of(near, far), 50.0, dataMap::get));
    }

    @Test
    void missingPlayerDataNeverBlocksASpawn() {
        Location spawnLoc = new Location(null, 0, 0, 0);
        Player player = createMockPlayer(new Location(null, 1, 0, 0));

        assertFalse(MobSpawnPolicy.shouldCancelMobSpawn(spawnLoc, List.of(player), 50.0, p -> null));
        assertFalse(MobSpawnPolicy.shouldCancelPhantomSpawn(spawnLoc, List.of(player), 40.0, p -> null));
    }

    @Test
    void phantomFilterIgnoresVerticalDistance() {
        Player player = createMockPlayer(new Location(null, 0, 200, 0));
        PlayerData data = new PlayerData(UUID.randomUUID(), "Player");
        data.setPhantomEnabled(false);

        Map<Player, PlayerData> dataMap = Map.of(player, data);

        assertTrue(MobSpawnPolicy.shouldCancelPhantomSpawn(
                new Location(null, 0, 0, 0), List.of(player), 40.0, dataMap::get));
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
