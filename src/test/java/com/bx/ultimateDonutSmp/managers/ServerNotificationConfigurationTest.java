package com.bx.ultimateDonutSmp.managers;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    @Test
    void everyJoinAndLeaveLineOffersPerRankWordingKeyedByPermission() throws Exception {
        ConfigurationSection section = notifications();

        for (String announcement : List.of("JOIN", "LEAVE", "FIRST-JOIN")) {
            ConfigurationSection byPermission =
                    section.getConfigurationSection(announcement + ".BY-PERMISSION");
            assertNotNull(byPermission, announcement + " has no BY-PERMISSION section");

            Map<String, String> wordingByNode = wordingByNode(byPermission);
            assertFalse(wordingByNode.isEmpty(), announcement + " ships no rank examples");
            wordingByNode.forEach((permission, wording) -> {
                assertFalse(wording.isBlank(), permission + " has blank wording");
                assertTrue(wording.contains("{player}"), permission + " never names the player");
                assertTrue(
                        permission.startsWith("ultimatedonutsmp.notifications."),
                        permission + " is not under the plugin's own permission root"
                );
            });
        }
    }

    @Test
    void theBundledRanksAreListedHighestFirstSoTheFirstMatchIsTheBestOne() throws Exception {
        ConfigurationSection section = notifications();

        for (String announcement : List.of("JOIN", "LEAVE", "FIRST-JOIN")) {
            List<String> permissions = List.copyOf(
                    wordingByNode(section.getConfigurationSection(announcement + ".BY-PERMISSION"))
                            .keySet()
            );
            assertEquals(3, permissions.size(), announcement + " should ship three rank examples");
            assertTrue(permissions.get(0).endsWith("vip++"), announcement + " does not lead with vip++");
            assertTrue(permissions.get(1).endsWith("vip+"), announcement + " has vip+ out of order");
            assertTrue(permissions.get(2).endsWith("vip"), announcement + " has vip out of order");
        }
    }

    /**
     * The rank wording, keyed by the whole permission node. A node carries dots and Bukkit reads
     * those as a path, so the bundled keys arrive as a tree and only a deep read puts them back
     * together.
     */
    private static Map<String, String> wordingByNode(ConfigurationSection byPermission) {
        Map<String, String> wording = new LinkedHashMap<>();
        byPermission.getValues(true).forEach((node, value) -> {
            if (value instanceof String text) {
                wording.put(node, text);
            }
        });
        return wording;
    }

    @Test
    void theFirstRankThePlayerHoldsDecidesTheWording() {
        ConfigurationSection ranks = ranks();

        assertEquals(
                "top",
                ServerNotificationManager.resolveByPermission(ranks, "plain", permission -> true),
                "holding everything should take the first entry, not the last"
        );
        assertEquals(
                "middle",
                ServerNotificationManager.resolveByPermission(
                        ranks, "plain", permission -> !permission.endsWith("vip++")
                )
        );
        assertEquals(
                "bottom",
                ServerNotificationManager.resolveByPermission(
                        ranks, "plain", permission -> permission.endsWith(".vip")
                )
        );
    }

    @Test
    void aPlayerMatchingNoRankKeepsThePlainWording() {
        assertEquals(
                "plain",
                ServerNotificationManager.resolveByPermission(ranks(), "plain", permission -> false)
        );
        assertEquals(
                "plain",
                ServerNotificationManager.resolveByPermission(null, "plain", permission -> true),
                "an announcement without a BY-PERMISSION section still announces"
        );
        assertEquals(
                "plain",
                ServerNotificationManager.resolveByPermission(
                        new YamlConfiguration().createSection("BY-PERMISSION"),
                        "plain",
                        permission -> true
                ),
                "an emptied BY-PERMISSION section still announces"
        );
    }

    @Test
    void blankWordingIsSkippedRatherThanAnnouncedAsAnEmptyLine() {
        YamlConfiguration config = new YamlConfiguration();
        ConfigurationSection ranks = config.createSection("BY-PERMISSION");
        ranks.set("ultimatedonutsmp.notifications.join.vip++", "   ");
        ranks.set("ultimatedonutsmp.notifications.join.vip", "bottom");

        assertEquals(
                "bottom",
                ServerNotificationManager.resolveByPermission(ranks, "plain", permission -> true),
                "a rank left blank should fall through to the next one"
        );
    }

    private static ConfigurationSection ranks() {
        YamlConfiguration config = new YamlConfiguration();
        ConfigurationSection ranks = config.createSection("BY-PERMISSION");
        ranks.set("ultimatedonutsmp.notifications.join.vip++", "top");
        ranks.set("ultimatedonutsmp.notifications.join.vip+", "middle");
        ranks.set("ultimatedonutsmp.notifications.join.vip", "bottom");
        return ranks;
    }

    private static ConfigurationSection notifications() throws Exception {
        YamlConfiguration config = new YamlConfiguration();
        config.load(Path.of("src/main/resources", "config.yml").toFile());
        ConfigurationSection section = config.getConfigurationSection("SERVER-NOTIFICATIONS");
        assertNotNull(section, "config.yml has no SERVER-NOTIFICATIONS section");
        return section;
    }
}
