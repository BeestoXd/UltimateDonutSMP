package com.bx.ultimateDonutSmp.managers;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShardAfkDifferentWorldTest {

    private World createMockWorld(String name) {
        return (World) Proxy.newProxyInstance(
                World.class.getClassLoader(),
                new Class<?>[]{World.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getName" -> name;
                    default -> null;
                }
        );
    }

    private Player createMockPlayer(Location location) {
        return (Player) Proxy.newProxyInstance(
                Player.class.getClassLoader(),
                new Class<?>[]{Player.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getUniqueId" -> UUID.randomUUID();
                    case "getLocation" -> location;
                    case "getWorld" -> location == null ? null : location.getWorld();
                    default -> null;
                }
        );
    }

    @Test
    void matchesPlayerInAfkZoneEvenWhenRegionWorldIsDifferent() {
        World lobbyWorld = createMockWorld("lobby");
        World afkWorld = createMockWorld("afk_world");

        Location afkLocation = new Location(afkWorld, 100.0, 65.0, 100.0);
        Location spawnRewardLocation = new Location(lobbyWorld, 0.0, 70.0, 0.0);

        ShardManager.ShardCuboidConfig config = new ShardManager.ShardCuboidConfig(
                "spawn",
                "spawn",
                "lobby",
                spawnRewardLocation,
                16.0D,
                100,
                60,
                1L,
                "countdown",
                "reward",
                "boosted",
                "leave",
                180,
                null,
                afkLocation,
                "afk message",
                true,
                true,
                15,
                5,
                "paused",
                "afk paused",
                "excluded",
                Set.of()
        );

        // Player standing at the AFK point in afk_world
        Player playerInAfk = createMockPlayer(new Location(afkWorld, 105.0, 65.0, 100.0));
        assertTrue(config.matches(playerInAfk, null, null),
                "Player in AFK zone in afk_world should match even when region world is lobby");

        // Player far away in afk_world
        Player playerFarInAfkWorld = createMockPlayer(new Location(afkWorld, 500.0, 65.0, 500.0));
        assertFalse(config.matches(playerFarInAfkWorld, null, null),
                "Player outside AFK zone in afk_world should not match");

        // Player in lobby at spawn
        Player playerInLobby = createMockPlayer(new Location(lobbyWorld, 2.0, 70.0, 2.0));
        assertTrue(config.matches(playerInLobby, null, null),
                "Player in lobby within reward radius should match");

        // Player in some other world
        World wilderness = createMockWorld("wilderness");
        Player playerInWilderness = createMockPlayer(new Location(wilderness, 100.0, 65.0, 100.0));
        assertFalse(config.matches(playerInWilderness, null, null),
                "Player in wilderness should not match");
    }
}
