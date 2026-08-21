package com.bx.ultimateDonutSmp.managers;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServerNotificationConfigurationTest {

    private static final List<String> ANNOUNCEMENTS = List.of(
            "JOIN",
            "LEAVE",
            "FIRST-JOIN",
            "AUCTION-HOUSE.LISTING",
            "AUCTION-HOUSE.PURCHASE",
            "ORDERS.CREATE",
            "ORDERS.COMPLETE"
    );

    @Test
    void everyAnnouncementShipsSwitchedOffWithTextReadyToUse() throws Exception {
        ConfigurationSection section = notifications();

        for (String announcement : ANNOUNCEMENTS) {
            assertFalse(
                    section.getBoolean(announcement + ".ENABLED"),
                    announcement + " must ship off so updating the jar leaves chat alone"
            );
            String message = section.getString(announcement + ".MESSAGE");
            assertNotNull(message, announcement + " has no message");
            assertFalse(message.isBlank(), announcement + " has a blank message");
            assertTrue(message.contains("{player}"), announcement + " never names the player");
        }
    }

    @Test
    void theMarketplaceParentsAreOnSoOnlyTheIndividualLinesNeedTurningOn() throws Exception {
        ConfigurationSection section = notifications();

        assertTrue(section.getBoolean("AUCTION-HOUSE.ENABLED"));
        assertTrue(section.getBoolean("ORDERS.ENABLED"));
    }

    @Test
    void marketplaceMessagesCarryTheirItemPlaceholders() throws Exception {
        ConfigurationSection section = notifications();

        for (String announcement : List.of(
                "AUCTION-HOUSE.LISTING",
                "AUCTION-HOUSE.PURCHASE",
                "ORDERS.CREATE",
                "ORDERS.COMPLETE"
        )) {
            String message = section.getString(announcement + ".MESSAGE", "");
            assertTrue(message.contains("{item}"), announcement + " never names the item");
            assertTrue(message.contains("{amount}"), announcement + " never gives the amount");
        }

        assertTrue(section.getString("ORDERS.COMPLETE.MESSAGE", "").contains("{owner}"));
    }

    @Test
    void placeholdersAreFilledInAndMissingValuesFallAway() {
        assertEquals(
                "&aWelcome &eSteve &ato the server!",
                ServerNotificationManager.format(
                        "&aWelcome &e{player} &ato the server!",
                        "{player}", "Steve"
                )
        );
        assertEquals(
                "Alex listed 3x Diamond Sword",
                ServerNotificationManager.format(
                        "{player} listed {amount}x {item}",
                        "{player}", "Alex",
                        "{amount}", "3",
                        "{item}", "Diamond Sword"
                )
        );
        assertEquals(
                "Alex listed an item from ",
                ServerNotificationManager.format(
                        "{player} listed an item from {category}",
                        "{player}", "Alex",
                        "{category}", null
                )
        );
    }

    @Test
    void anEmptyMessageProducesNothingToBroadcast() {
        assertNull(ServerNotificationManager.format(null, "{player}", "Steve"));
        assertNull(ServerNotificationManager.format("", "{player}", "Steve"));
        assertNull(ServerNotificationManager.format("   ", "{player}", "Steve"));
    }

    private static ConfigurationSection notifications() throws Exception {
        YamlConfiguration config = new YamlConfiguration();
        config.load(Path.of("src/main/resources", "config.yml").toFile());
        ConfigurationSection section = config.getConfigurationSection("SERVER-NOTIFICATIONS");
        assertNotNull(section, "config.yml has no SERVER-NOTIFICATIONS section");
        return section;
    }
}
