package com.bx.ultimateDonutSmp.listeners;

import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.models.TwoChoice;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * PlayerJoinQuitListener clears the server's own join and leave message and resends it per player,
 * so whatever decides that audience decides whether anybody sees a join at all. These pin it to
 * "everyone who did not mute it", the same rule the death feed follows.
 */
class JoinLeaveMessageAudienceTest {

    @Test
    void aPlayerWhoNeverTouchedTheSettingSeesJoinAndLeaveLines() {
        assertTrue(newPlayer().isJoinLeaveMessagesEnabled());
    }

    @Test
    void mutingTheSettingStopsThem() {
        PlayerData data = newPlayer();
        data.setJoinLeaveMessagesChoice(TwoChoice.OFF);
        assertFalse(data.isJoinLeaveMessagesEnabled());
    }

    /**
     * FRIENDS_FOLLOWED is the stored on value, not a request to be shown only the people you
     * follow. Reading it the other way emptied the feed, because a server where nobody has
     * followed anybody has no receiver that passes a follow check.
     */
    @Test
    void theOnValueDoesNotNarrowTheFeedToFollowedPlayers() {
        PlayerData data = newPlayer();
        data.setJoinLeaveMessagesChoice(TwoChoice.FRIENDS_FOLLOWED);
        assertTrue(data.isJoinLeaveMessagesEnabled());
    }

    private static PlayerData newPlayer() {
        return new PlayerData(UUID.randomUUID(), "Tester");
    }
}
