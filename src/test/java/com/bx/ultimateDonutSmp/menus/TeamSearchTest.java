package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.managers.TeamManager;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TeamSearchTest {

    @Test
    void teamMenuShipsASearchSignLikeTheOtherBrowseMenus() throws Exception {
        YamlConfiguration menus = new YamlConfiguration();
        menus.load(Path.of("src/main/resources/menus.yml").toFile());

        assertTrue(menus.isConfigurationSection("TEAM-MENUS.TEAM.SEARCH_SIGN"));
        assertEquals(0, menus.getInt("TEAM-MENUS.TEAM.SEARCH_SIGN.input-line"));
        assertEquals(
                List.of("", "↑↑↑↑↑↑↑↑↑↑↑↑↑", "Search", ""),
                menus.getStringList("TEAM-MENUS.TEAM.SEARCH_SIGN.lines")
        );
    }

    @Test
    void cancelAndBlankKeepTheCurrentFilter() {
        assertEquals("Steve", TeamMenu.resolveSignSearch(null, "Steve"));
        assertEquals("Steve", TeamMenu.resolveSignSearch("  ", "Steve"));
        assertEquals("Steve", TeamMenu.resolveSignSearch("cancel", "Steve"));
        assertNull(TeamMenu.resolveSignSearch("cancel", null));
        assertEquals("Alex", TeamMenu.resolveSignSearch(" Alex ", "Steve"));
    }

    @Test
    void memberNamesMatchTheFilterWithoutCaringAboutCase() {
        assertTrue(TeamMenu.nameMatchesQuery("Steve", null));
        assertTrue(TeamMenu.nameMatchesQuery("Steve", "  "));
        assertTrue(TeamMenu.nameMatchesQuery("Steve", "ste"));
        assertTrue(TeamMenu.nameMatchesQuery("Alexandra", "LEX"));
        assertFalse(TeamMenu.nameMatchesQuery("Steve", "Alex"));
        assertFalse(TeamMenu.nameMatchesQuery(null, "Steve"));
    }

    @Test
    void teamManagerKeepsTheActiveFilterUntilItIsCleared() {
        TeamManager manager = new TeamManager(null);
        UUID playerId = UUID.randomUUID();

        assertNull(manager.getActiveSearchQuery(playerId));
        manager.setSearchQuery(playerId, "  Alex  ");
        assertEquals("Alex", manager.getActiveSearchQuery(playerId));
        manager.setSearchQuery(playerId, "   ");
        assertNull(manager.getActiveSearchQuery(playerId));
        manager.setSearchQuery(playerId, "a".repeat(80));
        assertEquals(64, manager.getActiveSearchQuery(playerId).length());
        manager.clearSearchState(playerId);
        assertNull(manager.getActiveSearchQuery(playerId));
    }
}
