package com.bx.ultimateDonutSmp.managers;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerSettingsConfigurationTest {

    private static final List<String> REQUIRED_SETTINGS = List.of(
            "PUBLIC_CHAT",
            "PRIVATE_MESSAGES",
            "SERVER_BROADCASTS",
            "HOTBAR_MESSAGES",
            "PAY_ALERTS",
            "BOUNTY_ALERTS",
            "AUCTION_NOTIFICATIONS",
            "FAST_CRYSTALS",
            "TOTEM_PARTICLES",
            "EXPLOSION_PARTICLES",
            "QUICK_AUCTION_PURCHASE",
            "CHAINMAIL_ON_RESPAWN",
            "DISABLE_MOB_SPAWN",
            "HIDE_ALL_PLAYERS",
            "SCOREBOARD_VISIBILITY",
            "AUTO_CONFIRM_TPAS",
            "NOTIFICATION_SOUNDS",
            "RTP_COORDINATES",
            "ORDER_NOTIFICATIONS",
            "DUEL_REQUESTS",
            "TPA_REQUESTS",
            "TEAM_INVITES",
            "PAYMENTS",
            "TEAM_CHAT_VISIBILITY",
            "WORTH_DISPLAY",
            "DUEL_MUSIC",
            "QUIET_SPAWN"
    );

    @Test
    void settingsMenuContainsEveryRequestedSettingInUniqueValidSlots() throws Exception {
        YamlConfiguration menus = load("menus.yml");
        assertEquals(54, menus.getInt("SETTINGS-MENU.SIZE"));

        ConfigurationSection buttons = menus.getConfigurationSection("SETTINGS-MENU.BUTTONS");
        assertNotNull(buttons);

        Set<Integer> usedSlots = new HashSet<>();
        for (String key : buttons.getKeys(false)) {
            assertFalse(key.startsWith("HEADER_"), key + " must not render a category header");
            int slot = buttons.getInt(key + ".SLOT", -1);
            assertTrue(slot >= 0 && slot < 54, key + " has invalid slot " + slot);
            assertTrue(slot % 9 >= 1 && slot % 9 <= 7, key + " is outside the packed layout at slot " + slot);
            assertTrue(usedSlots.add(slot), key + " duplicates slot " + slot);
        }

        for (String setting : REQUIRED_SETTINGS) {
            assertTrue(buttons.isConfigurationSection(setting), setting);
        }

        assertEquals(Set.of(
                1, 2, 3, 4, 5, 6, 7,
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43,
                46
        ), usedSlots);

        Map<String, Integer> centeredSlots = Map.ofEntries(
                Map.entry("PUBLIC_CHAT", 1),
                Map.entry("PRIVATE_MESSAGES", 2),
                Map.entry("SERVER_BROADCASTS", 3),
                Map.entry("AUCTION_NOTIFICATIONS", 4),
                Map.entry("EXPLOSION_PARTICLES", 5),
                Map.entry("QUICK_AUCTION_PURCHASE", 6),
                Map.entry("HIDE_ALL_PLAYERS", 7),
                Map.entry("NOTIFICATION_SOUNDS", 10),
                Map.entry("RTP_COORDINATES", 11),
                Map.entry("PAY_ALERTS", 12),
                Map.entry("HOTBAR_MESSAGES", 13),
                Map.entry("CLEAR_ENTITIES_MESSAGES", 14),
                Map.entry("BOUNTY_ALERTS", 15),
                Map.entry("AMETHYST_BREAK_MESSAGES", 16),
                Map.entry("KEY_ALL_NOTIFICATIONS", 19),
                Map.entry("CHAINMAIL_ON_RESPAWN", 20),
                Map.entry("SCOREBOARD_VISIBILITY", 21),
                Map.entry("TPA_CONFIRM_MENUS", 22),
                Map.entry("LUNAR_TEAMMATES", 23),
                Map.entry("TPA_REQUESTS", 24),
                Map.entry("TPA_HERE_REQUESTS", 25),
                Map.entry("TEAM_INVITES", 28),
                Map.entry("PAYMENTS", 29),
                Map.entry("TEAM_CHAT", 30),
                Map.entry("DISABLE_MOB_SPAWN", 31),
                Map.entry("DISABLE_PHANTOM_SPAWN", 32),
                Map.entry("PAY_CONFIRM_MENUS", 33),
                Map.entry("AUTO_CONFIRM_TPAS", 34),
                Map.entry("FAST_CRYSTALS", 37),
                Map.entry("WORTH_DISPLAY", 38),
                Map.entry("TOTEM_PARTICLES", 39),
                Map.entry("DUEL_REQUESTS", 40),
                Map.entry("ORDER_NOTIFICATIONS", 41),
                Map.entry("TEAM_CHAT_VISIBILITY", 42),
                Map.entry("DUEL_MUSIC", 43),
                Map.entry("QUIET_SPAWN", 46)
        );
        centeredSlots.forEach((key, slot) ->
                assertEquals(slot, buttons.getInt(key + ".SLOT"), key));
    }

    @Test
    void hiddenRtpTemplatesNeverExposeCoordinates() throws Exception {
        YamlConfiguration rtp = load("rtp.yml");

        for (String path : List.of(
                "MESSAGES.SAFE-LOCATION-FOUND-HIDDEN",
                "MESSAGES.SEARCH-FOUND-ACTIONBAR-HIDDEN"
        )) {
            String message = rtp.getString(path);
            assertNotNull(message, path);
            assertFalse(message.contains("{x}"), path);
            assertFalse(message.contains("{y}"), path);
            assertFalse(message.contains("{z}"), path);
        }
    }

    private static YamlConfiguration load(String fileName) throws Exception {
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.load(Path.of("src/main/resources", fileName).toFile());
        return configuration;
    }
}
