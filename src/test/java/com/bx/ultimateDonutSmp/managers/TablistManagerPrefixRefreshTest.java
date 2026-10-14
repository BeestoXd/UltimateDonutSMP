package com.bx.ultimateDonutSmp.managers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TablistManagerPrefixRefreshTest {

    private static final String FORMAT = "<media_badge>%luckperms_prefix%&r&fSteve";

    @Test
    void luckPermsPrefixTokenIsPartOfTheResolvedName() {
        assertEquals("<media_badge>&r&fSteve", TablistManager.applyResolvedPrefix(FORMAT, ""));
        assertEquals(
                "<media_badge>&6[Daddy] &f&r&fSteve",
                TablistManager.applyResolvedPrefix(FORMAT, "&6[Daddy] &f")
        );
    }

    @Test
    void pluginPrefixTokenStillResolves() {
        assertEquals("&6[Daddy] &fSteve", TablistManager.applyResolvedPrefix("%prefix%Steve", "&6[Daddy] &f"));
    }

    @Test
    void emptyPrefixAndMissingFormatStayEmpty() {
        assertEquals("", TablistManager.applyResolvedPrefix(null, "&6[Daddy] &f"));
        assertEquals("Steve", TablistManager.applyResolvedPrefix("Steve", null));
        assertEquals("", TablistManager.applyResolvedPrefix("", "&6[Daddy] &f"));
    }

    @Test
    void sameTemplateRepaintsOnceThePrefixArrives() {
        String beforeRestartSettles = TablistManager.applyResolvedPrefix(FORMAT, "");
        String afterPrefixLoads = TablistManager.applyResolvedPrefix(FORMAT, "&6[Daddy] &f");

        assertTrue(FORMAT.contains("%luckperms_prefix%"));
        assertFalse(afterPrefixLoads.contains("%luckperms_prefix%"));
        assertTrue(TablistManager.tablistRenderChanged(beforeRestartSettles, afterPrefixLoads));
        assertFalse(TablistManager.tablistRenderChanged(afterPrefixLoads, afterPrefixLoads));
        assertTrue(TablistManager.tablistRenderChanged(null, afterPrefixLoads));
    }
}
