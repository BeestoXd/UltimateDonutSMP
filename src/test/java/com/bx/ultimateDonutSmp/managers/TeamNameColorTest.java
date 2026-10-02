package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.models.Team;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TeamNameColorTest {

    private TeamManager teamManager;

    @BeforeEach
    void setUp() {
        teamManager = new TeamManager(null);
    }

    private Player createMockPlayer(UUID uuid) {
        return (Player) Proxy.newProxyInstance(
                Player.class.getClassLoader(),
                new Class<?>[]{Player.class},
                (proxy, method, args) -> {
                    if ("getUniqueId".equals(method.getName())) {
                        return uuid;
                    }
                    if ("getName".equals(method.getName())) {
                        return "Player_" + uuid.toString().substring(0, 8);
                    }
                    return null;
                }
        );
    }

    @Test
    void testCleanNameAndContainsColorCodes() {
        assertEquals("TEAM", TeamManager.cleanName("&7TEAM"));
        assertEquals("ELITE", TeamManager.cleanName("&c&lELITE"));
        assertEquals("BOSS", TeamManager.cleanName("&#FF0000BOSS"));
        assertEquals("TEAM", TeamManager.cleanName("TEAM"));
        assertEquals("", TeamManager.cleanName(null));
        assertEquals("", TeamManager.cleanName(""));

        assertTrue(ColorUtils.containsColorCodes("&7TEAM"));
        assertTrue(ColorUtils.containsColorCodes("&c&lELITE"));
        assertTrue(ColorUtils.containsColorCodes("&#FF0000BOSS"));
        assertFalse(ColorUtils.containsColorCodes("TEAM"));
        assertFalse(ColorUtils.containsColorCodes(null));
        assertFalse(ColorUtils.containsColorCodes(""));
    }

    @Test
    void testValidTeamNames() {
        assertTrue(teamManager.isValidName("TEAM"));
        assertTrue(teamManager.isValidName("ABC"));
        assertTrue(teamManager.isValidName("ELITE"));
        assertTrue(teamManager.isValidName("A1_B"));

        assertTrue(teamManager.isValidName("&7TEAM"));
        assertTrue(teamManager.isValidName("&c&lELITE"));
        assertTrue(teamManager.isValidName("&#FF0000BOSS"));
        assertTrue(teamManager.isValidName("&a&oPRO_1"));
    }

    @Test
    void testInvalidTeamNames() {
        // Too short visible length (< 3)
        assertFalse(teamManager.isValidName("&7A"));
        assertFalse(teamManager.isValidName("AB"));
        assertFalse(teamManager.isValidName("&c&l12"));

        // Too long visible length (> 5)
        assertFalse(teamManager.isValidName("&7TOOLONG"));
        assertFalse(teamManager.isValidName("TOOLONG"));
        assertFalse(teamManager.isValidName("&c&lSIXLET"));

        // Illegal characters in visible name
        assertFalse(teamManager.isValidName("&7TEAM!"));
        assertFalse(teamManager.isValidName("TEA M"));
        assertFalse(teamManager.isValidName("&7TEA-M"));

        // Invalid color codes (e.g. &z is not a valid Minecraft color code)
        assertFalse(teamManager.isValidName("&zTEAM"));

        // Color codes only (no visible characters)
        assertFalse(teamManager.isValidName("&7&c"));
        assertFalse(teamManager.isValidName("&#FF0000"));

        // Null or blank
        assertFalse(teamManager.isValidName(null));
        assertFalse(teamManager.isValidName(""));
        assertFalse(teamManager.isValidName("   "));
    }

    @Test
    void testTeamModelCleanName() {
        UUID leaderUuid = UUID.randomUUID();
        Team team = new Team("&7TEAM", leaderUuid);
        assertEquals("&7TEAM", team.getName());
        assertEquals("TEAM", team.getCleanName());
    }

    @Test
    void testTeamCreationAndLookupWithColorCodes() {
        UUID leaderUuid = UUID.randomUUID();
        Player leader = createMockPlayer(leaderUuid);

        boolean created = teamManager.createTeam(leader, "&7TEAM");
        assertTrue(created);

        // Lookup with exact raw name
        assertTrue(teamManager.teamExists("&7TEAM"));
        assertNotNull(teamManager.getTeam("&7TEAM"));

        // Lookup with clean name and case-insensitivity
        assertTrue(teamManager.teamExists("TEAM"));
        assertTrue(teamManager.teamExists("team"));
        assertTrue(teamManager.teamExists("&7team"));
        assertNotNull(teamManager.getTeam("TEAM"));
        assertNotNull(teamManager.getTeam("team"));
        assertEquals("&7TEAM", teamManager.getTeam("team").getName());

        // Collision check: cannot create team with same clean name even if colors differ
        assertTrue(teamManager.teamExists("&cTEAM"));

        // Invite and join checks
        UUID targetUuid = UUID.randomUUID();
        Player target = createMockPlayer(targetUuid);

        teamManager.sendInvite(leader, target);
        assertTrue(teamManager.hasInviteFrom(target, "&7TEAM"));
        assertTrue(teamManager.hasInviteFrom(target, "TEAM"));
        assertTrue(teamManager.hasInviteFrom(target, "team"));

        boolean joined = teamManager.joinTeam(target, "team");
        assertTrue(joined);
        assertTrue(teamManager.isInTeam(target));
        assertEquals("&7TEAM", teamManager.getTeam(target).getName());

        // Disband cleans up lookups
        teamManager.disbandTeam(teamManager.getTeam("&7TEAM"));
        assertFalse(teamManager.teamExists("&7TEAM"));
        assertFalse(teamManager.teamExists("TEAM"));
        assertNull(teamManager.getTeam("team"));
    }
}
