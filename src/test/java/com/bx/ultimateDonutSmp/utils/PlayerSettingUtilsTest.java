package com.bx.ultimateDonutSmp.utils;

import com.bx.ultimateDonutSmp.models.PlayerData;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerSettingUtilsTest {

    @Test
    void newPreferencesPreserveExistingPlayerExperienceByDefault() {
        PlayerData data = new PlayerData(UUID.randomUUID(), "SettingsTester");

        for (PlayerSettingUtils.NotificationChannel channel : PlayerSettingUtils.NotificationChannel.values()) {
            assertTrue(PlayerSettingUtils.notificationEnabled(data, channel), channel.name());
        }
        for (PlayerSettingUtils.SoundChannel channel : PlayerSettingUtils.SoundChannel.values()) {
            assertTrue(PlayerSettingUtils.soundEnabled(data, channel), channel.name());
        }

        assertTrue(data.isExplosionParticlesEnabled());
        assertTrue(data.isRtpCoordinatesEnabled());
        assertFalse(data.isHideAllPlayersEnabled());
        assertFalse(data.isQuietSpawnEnabled());
    }

    @Test
    void notificationChannelsAreIndependent() {
        PlayerData data = new PlayerData(UUID.randomUUID(), "SettingsTester");
        data.setPublicChatEnabled(false);
        data.setServerBroadcastsEnabled(false);
        data.setAuctionNotificationsEnabled(false);
        data.setOrderNotificationsEnabled(false);
        data.setTeamChatVisible(false);

        assertFalse(PlayerSettingUtils.notificationEnabled(
                data, PlayerSettingUtils.NotificationChannel.PUBLIC_CHAT));
        assertFalse(PlayerSettingUtils.notificationEnabled(
                data, PlayerSettingUtils.NotificationChannel.SERVER_BROADCAST));
        assertFalse(PlayerSettingUtils.notificationEnabled(
                data, PlayerSettingUtils.NotificationChannel.AUCTION));
        assertFalse(PlayerSettingUtils.notificationEnabled(
                data, PlayerSettingUtils.NotificationChannel.ORDER));
        assertFalse(PlayerSettingUtils.notificationEnabled(
                data, PlayerSettingUtils.NotificationChannel.TEAM_CHAT));
    }

    @Test
    void notificationAndDuelSoundsDoNotDisableGameplaySounds() {
        PlayerData data = new PlayerData(UUID.randomUUID(), "SettingsTester");
        data.setNotificationSoundsEnabled(false);
        data.setDuelMusicEnabled(false);

        assertFalse(PlayerSettingUtils.soundEnabled(
                data, PlayerSettingUtils.SoundChannel.NOTIFICATION));
        assertFalse(PlayerSettingUtils.soundEnabled(
                data, PlayerSettingUtils.SoundChannel.DUEL));
        assertTrue(PlayerSettingUtils.soundEnabled(
                data, PlayerSettingUtils.SoundChannel.GAMEPLAY));
    }
}
