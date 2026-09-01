package com.bx.ultimateDonutSmp.managers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SetupShardRegionOwnershipTest {

    private static final String SPAWN_BUILD = "worlds_spawn1,13.0,-3.0,-31.0,45.0,-5.0";
    private static final String WORLD_ORIGIN = "worlds_spawn1,0.5,0.0,0.5,90.0,0.0";
    private static final String SETUP_CUBOID = "spawn1";
    private static final String OWNER_CUBOID = "shardzone";

    @Test
    void aRegionWithNoLocationYetIsBootstrapped() {
        assertTrue(SpawnManager.setupOwnsShardRegion(null, ""));
        assertTrue(SpawnManager.setupOwnsShardRegion("", ""));
        assertTrue(SpawnManager.setupOwnsShardRegion("   ", SPAWN_BUILD));
    }

    @Test
    void aRegionStillFollowingSetupMovesWithTheNewLocation() {
        assertTrue(SpawnManager.setupOwnsShardRegion(SPAWN_BUILD, SPAWN_BUILD));
        assertTrue(SpawnManager.setupOwnsShardRegion("  " + SPAWN_BUILD + "  ", SPAWN_BUILD));
        assertTrue(SpawnManager.setupOwnsShardRegion(SPAWN_BUILD.toUpperCase(), SPAWN_BUILD));
    }

    @Test
    void aRegionTheOwnerMovedIsLeftAlone() {
        assertFalse(SpawnManager.setupOwnsShardRegion(SPAWN_BUILD, WORLD_ORIGIN));
    }

    @Test
    void aRegionConfiguredBeforeAnySetupRunIsLeftAlone() {
        assertFalse(SpawnManager.setupOwnsShardRegion(SPAWN_BUILD, ""));
        assertFalse(SpawnManager.setupOwnsShardRegion(SPAWN_BUILD, null));
    }

    @Test
    void aRegionWithNoCuboidYetIsBootstrapped() {
        assertTrue(SpawnManager.setupOwnsShardCuboid(null, SETUP_CUBOID));
        assertTrue(SpawnManager.setupOwnsShardCuboid("", SETUP_CUBOID));
        assertTrue(SpawnManager.setupOwnsShardCuboid("   ", SETUP_CUBOID));
    }

    @Test
    void aRegionStillNamingTheSetupCuboidMovesWithTheNewLocation() {
        assertTrue(SpawnManager.setupOwnsShardCuboid(SETUP_CUBOID, SETUP_CUBOID));
        assertTrue(SpawnManager.setupOwnsShardCuboid("  " + SETUP_CUBOID + "  ", SETUP_CUBOID));
        assertTrue(SpawnManager.setupOwnsShardCuboid(SETUP_CUBOID.toUpperCase(), SETUP_CUBOID));
    }

    @Test
    void aCuboidTheOwnerBoundIsLeftAlone() {
        assertFalse(SpawnManager.setupOwnsShardCuboid(OWNER_CUBOID, SETUP_CUBOID));
        assertFalse(SpawnManager.setupOwnsShardCuboid(OWNER_CUBOID, ""));
        assertFalse(SpawnManager.setupOwnsShardCuboid(OWNER_CUBOID, null));
    }

    @Test
    void aShardZoneBoundToACuboidSurvivesTheNextSetupRun() {
        // /cuboid bind <name> shard true names the cuboid and never writes a LOCATION, so the
        // location check on its own still hands the region back to setup.
        assertTrue(SpawnManager.setupOwnsShardRegion("", SPAWN_BUILD));
        assertFalse(SpawnManager.setupOwnsShardCuboid(OWNER_CUBOID, SETUP_CUBOID));
    }
}
