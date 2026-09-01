package com.bx.ultimateDonutSmp.listeners;

import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.models.TwoChoice;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * PlayerDeathListener clears the vanilla death message and posts its own line to each player it
 * decides may see it, so anything that narrows that audience silently removes the death feed from
 * chat and from the server log at once. These pin the audience to "everyone who did not mute it".
 */
class DeathMessageAudienceTest {

    @Test
    void aPlayerWhoNeverTouchedTheSettingSeesDeathMessages() {
        assertTrue(PlayerDeathListener.shouldReceiveDeathMessage(newPlayer()));
    }

    @Test
    void mutingTheSettingStopsThem() {
        PlayerData data = newPlayer();
        data.setDeathMessagesChoice(TwoChoice.OFF);
        assertFalse(PlayerDeathListener.shouldReceiveDeathMessage(data));
    }

    /**
     * The stored value doubles as the join and leave choice, where FRIENDS_FOLLOWED means "only
     * players I follow". Reading it that way for deaths left the feed empty, because a server
     * where nobody has followed anybody has no receiver that passes the check.
     */
    @Test
    void theOnValueDoesNotNarrowTheFeedToFollowedPlayers() {
        PlayerData data = newPlayer();
        data.setDeathMessagesChoice(TwoChoice.FRIENDS_FOLLOWED);
        assertTrue(PlayerDeathListener.shouldReceiveDeathMessage(data));
    }

    @Test
    void aPlayerWithNoStoredRowStillSeesThem() {
        assertTrue(PlayerDeathListener.shouldReceiveDeathMessage(null));
    }

    @Test
    void bundledDeathMessagesShipEnabled() {
        YamlConfiguration config = bundledDeathMessages();
        assertTrue(config.isBoolean("MESSAGES.ENABLED"));
        assertTrue(config.getBoolean("MESSAGES.ENABLED"));
    }

    /**
     * The file used to ship a SETTINGS block promising a radius the plugin never read, so an owner
     * could set it either way and watch nothing happen. Only MESSAGES is wired up; a key here that
     * nothing reads is a setting admins waste an evening on.
     */
    @Test
    void bundledDeathMessagesShipNothingTheCodeIgnores() {
        assertEquals(Set.of("MESSAGES"), bundledDeathMessages().getKeys(false));
    }

    private static YamlConfiguration bundledDeathMessages() {
        var stream = DeathMessageAudienceTest.class.getClassLoader()
                .getResourceAsStream("death-messages.yml");
        assertNotNull(stream);

        return YamlConfiguration.loadConfiguration(
                new InputStreamReader(stream, StandardCharsets.UTF_8)
        );
    }

    private static PlayerData newPlayer() {
        return new PlayerData(UUID.randomUUID(), "Tester");
    }
}
