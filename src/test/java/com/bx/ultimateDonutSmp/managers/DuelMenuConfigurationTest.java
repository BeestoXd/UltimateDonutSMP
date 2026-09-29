package com.bx.ultimateDonutSmp.managers;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DuelMenuConfigurationTest {

    private YamlConfiguration loadDuels() {
        return YamlConfiguration.loadConfiguration(new File("src/main/resources/duels.yml"));
    }

    @Test
    void duelsConfigContainsAllGuiItemSections() {
        YamlConfiguration config = loadDuels();

        assertTrue(config.isConfigurationSection("GUI.QUEUE.ITEMS"), "GUI.QUEUE.ITEMS section missing");
        assertTrue(config.isString("GUI.QUEUE.ITEMS.JOIN_QUEUE.NAME"));
        assertTrue(config.isString("GUI.QUEUE.ITEMS.LEAVE_QUEUE.NAME"));
        assertTrue(config.isString("GUI.QUEUE.ITEMS.SELECT_MAP.NAME"));
        assertTrue(config.isString("GUI.QUEUE.ITEMS.STATS.NAME"));
        assertTrue(config.isList("GUI.QUEUE.ITEMS.STATS.LORE"));
        assertTrue(config.isString("GUI.QUEUE.ITEMS.CLAIMS.NAME"));
        assertTrue(config.isString("GUI.QUEUE.ITEMS.CLOSE.NAME"));

        assertTrue(config.isConfigurationSection("GUI.QUEUE_MAP_SELECT.ITEMS"), "GUI.QUEUE_MAP_SELECT.ITEMS section missing");
        assertTrue(config.isString("GUI.QUEUE_MAP_SELECT.TITLE"));
        assertTrue(config.isString("GUI.QUEUE_MAP_SELECT.ITEMS.SELECTED_PREFIX"));
        assertTrue(config.isString("GUI.QUEUE_MAP_SELECT.ITEMS.UNSELECTED_PREFIX"));
        assertTrue(config.isString("GUI.QUEUE_MAP_SELECT.ITEMS.CURRENTLY_SELECTED_LORE"));
        assertTrue(config.isString("GUI.QUEUE_MAP_SELECT.ITEMS.CLICK_TO_SELECT_LORE"));
        assertTrue(config.isString("GUI.QUEUE_MAP_SELECT.ITEMS.NO_MAPS.NAME"));
        assertTrue(config.isString("GUI.QUEUE_MAP_SELECT.ITEMS.BACK.NAME"));
        assertTrue(config.isString("GUI.QUEUE_MAP_SELECT.ITEMS.CLOSE.NAME"));

        assertTrue(config.isConfigurationSection("GUI.CREATE.ITEMS"), "GUI.CREATE.ITEMS section missing");
        assertTrue(config.isString("GUI.CREATE.ITEMS.TARGET_OFFLINE.NAME"));
        assertTrue(config.isString("GUI.CREATE.ITEMS.NO_MAPS.NAME"));
        assertTrue(config.isString("GUI.CREATE.ITEMS.MAP_OPTION.NAME"));
        assertTrue(config.isList("GUI.CREATE.ITEMS.MAP_OPTION.LORE"));
        assertTrue(config.isString("GUI.CREATE.ITEMS.TARGET_HEAD.NAME"));
        assertTrue(config.isString("GUI.CREATE.ITEMS.PRIVACY_BUTTON.NAME"));
        assertTrue(config.isString("GUI.CREATE.ITEMS.CLOSE.NAME"));

        assertTrue(config.isConfigurationSection("GUI.CLAIMS.ITEMS"), "GUI.CLAIMS.ITEMS section missing");
        assertTrue(config.isString("GUI.CLAIMS.ITEMS.PREVIOUS_PAGE.NAME"));
        assertTrue(config.isString("GUI.CLAIMS.ITEMS.REFRESH.NAME"));
        assertTrue(config.isString("GUI.CLAIMS.ITEMS.NEXT_PAGE.NAME"));
        assertTrue(config.isString("GUI.CLAIMS.ITEMS.BACK.NAME"));
        assertTrue(config.isString("GUI.CLAIMS.ITEMS.NO_CLAIMS.NAME"));
        assertTrue(config.isString("GUI.CLAIMS.ITEMS.CLAIM_ENTRY.NAME"));
        assertTrue(config.isList("GUI.CLAIMS.ITEMS.CLAIM_ENTRY.LORE"));

        assertTrue(config.isConfigurationSection("GUI.CLAIM_PREVIEW.ITEMS"), "GUI.CLAIM_PREVIEW.ITEMS section missing");
        assertTrue(config.isString("GUI.CLAIM_PREVIEW.TITLE"));
        assertEquals(54, config.getInt("GUI.CLAIM_PREVIEW.SIZE"));
        assertTrue(config.isString("GUI.CLAIM_PREVIEW.ITEMS.CLAIM_NOT_FOUND.NAME"));
        assertTrue(config.isString("GUI.CLAIM_PREVIEW.ITEMS.BACK_ARROW.NAME"));
        assertTrue(config.isString("GUI.CLAIM_PREVIEW.ITEMS.BACK_BARRIER.NAME"));
        assertTrue(config.isString("GUI.CLAIM_PREVIEW.ITEMS.SUMMARY.NAME"));
        assertTrue(config.isList("GUI.CLAIM_PREVIEW.ITEMS.SUMMARY.LORE"));
        assertTrue(config.isString("GUI.CLAIM_PREVIEW.ITEMS.CLAIM_ALL.NAME"));
        assertTrue(config.isString("GUI.CLAIM_PREVIEW.ITEMS.DELETE_CLAIM.NAME"));
    }

    @Test
    void duelsConfigContainsMessagesSection() {
        YamlConfiguration config = loadDuels();

        assertTrue(config.isConfigurationSection("MESSAGES"), "MESSAGES section missing from duels.yml");
        assertTrue(config.isString("MESSAGES.QUEUE_JOINED"));
        assertTrue(config.isString("MESSAGES.QUEUE_LEFT"));
        assertTrue(config.isString("MESSAGES.QUEUE_ALREADY_IN"));
        assertTrue(config.isString("MESSAGES.DRAW_SENT"));
        assertTrue(config.isString("MESSAGES.DRAW_RECEIVED"));
        assertTrue(config.isString("MESSAGES.DEFEATED_PLAYER"));
        assertTrue(config.isString("MESSAGES.LOST_DUEL"));
        assertTrue(config.isString("MESSAGES.DUEL_STARTED"));
        assertTrue(config.isString("MESSAGES.CLAIM_SUCCESS"));
        assertTrue(config.isString("MESSAGES.CLAIM_DELETED"));
        assertTrue(config.isString("MESSAGES.QUEUE_ARENAS_NOT_READY"));
        assertTrue(config.isString("MESSAGES.QUEUE_NO_READY_ARENAS"));
    }
}
