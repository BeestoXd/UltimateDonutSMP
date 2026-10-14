package com.bx.ultimateDonutSmp.managers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TablistManagerPrefixRefreshTest {

    private static final String FORMAT = "<media_badge>%luckperms_prefix%&r&fSteve";

    @Test
    void luckPermsPlaceholderIsLeftForPlaceholderApi() {
        assertEquals(FORMAT, TablistManager.applyResolvedPrefix(FORMAT, ""));
        assertEquals(FORMAT, TablistManager.applyResolvedPrefix(FORMAT, "&6[Daddy] &f"));
    }

    @Test
    void pluginPrefixTokenStillResolves() {
        assertEquals("&6[Daddy] &fSteve", TablistManager.applyResolvedPrefix("%prefix%Steve", "&6[Daddy] &f"));
        assertEquals("%luckperms_prefix%Steve", TablistManager.applyResolvedPrefix("%luckperms_prefix%%prefix%Steve", ""));
    }

    @Test
    void emptyPrefixAndMissingFormatStayEmpty() {
        assertEquals("", TablistManager.applyResolvedPrefix(null, "&6[Daddy] &f"));
        assertEquals("Steve", TablistManager.applyResolvedPrefix("Steve", null));
        assertEquals("", TablistManager.applyResolvedPrefix("", "&6[Daddy] &f"));
    }

    @Test
    void placeholderResultIsKeptWhenTheApiPrefixDiffers() {
        String fromPapi = "<media_badge>&6[Daddy] &f&r&fSteve";
        assertEquals(fromPapi, TablistManager.fillUnresolvedLuckPermsPrefix(fromPapi, "&7"));
    }

    @Test
    void unresolvedLuckPermsTokenFallsBackToTheApiPrefix() {
        assertEquals(
                "<media_badge>&6[Daddy] &f&r&fSteve",
                TablistManager.fillUnresolvedLuckPermsPrefix(FORMAT, "&6[Daddy] &f")
        );
        assertEquals(
                "<media_badge>&r&fSteve",
                TablistManager.fillUnresolvedLuckPermsPrefix(FORMAT, "")
        );
    }

    @Test
    void expandedPlaceholderRepaintsWhenTheRankArrives() {
        String before = "<media_badge>&r&fSteve";
        String after = "<media_badge>&6[Daddy] &f&r&fSteve";
        assertTrue(TablistManager.tablistRenderChanged(before, after));
        assertFalse(TablistManager.tablistRenderChanged(after, after));
        assertTrue(TablistManager.tablistRenderChanged(null, after));
    }
}
