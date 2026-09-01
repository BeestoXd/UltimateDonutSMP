package com.bx.ultimateDonutSmp.listeners;

import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.models.TwoChoice;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

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
        var stream = DeathMessageAudienceTest.class.getClassLoader()
                .getResourceAsStream("death-messages.yml");
        assertNotNull(stream);

        YamlConfiguration config = YamlConfiguration.loadConfiguration(
                new InputStreamReader(stream, StandardCharsets.UTF_8)
        );
        assertTrue(config.isBoolean("MESSAGES.ENABLED"));
        assertTrue(config.getBoolean("MESSAGES.ENABLED"));
    }

    private static PlayerData newPlayer() {
        return new PlayerData(UUID.randomUUID(), "Tester");
    }
}
