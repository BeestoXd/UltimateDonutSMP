package com.bx.ultimateDonutSmp.utils;

import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.models.ThreeChoice;
import com.bx.ultimateDonutSmp.models.TwoChoice;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerSettingDefaultsTest {

    @Test
    void bundledMenusKeepEverySettingEnabledWithBuiltInDefaults() throws Exception {
        YamlConfiguration menus = new YamlConfiguration();
        menus.load(Path.of("src/main/resources", "menus.yml").toFile());

        ConfigurationSection buttons = menus.getConfigurationSection(PlayerSettingDefaults.BUTTONS_PATH);
        assertNotNull(buttons);
        for (String key : buttons.getKeys(false)) {
            assertTrue(PlayerSettingDefaults.isOptionEnabled(buttons, key), key);
        }

        PlayerData data = newPlayer();
        PlayerData untouched = newPlayer();
        List<String> warnings = new ArrayList<>();
        PlayerSettingDefaults.applyDefaults(buttons, data, warnings::add);

        assertEquals(List.of(), warnings);
        assertEquals(untouched.getAdvancementMessagesChoice(), data.getAdvancementMessagesChoice());
        assertEquals(untouched.isPublicChatEnabled(), data.isPublicChatEnabled());
    }

    @Test
    void configuredDefaultsApplyToBooleanAndChoiceSettings() {
        ConfigurationSection buttons = buttons();
        buttons.set("ADVANCEMENT_MESSAGES.DEFAULT", "OFF");
        buttons.set("JOIN_LEAVE_MESSAGES.DEFAULT", false);
        buttons.set("PRIVATE_MESSAGES.DEFAULT", "FRIENDS_FOLLOWED");
        buttons.set("DEATH_MESSAGES.DEFAULT", "off");
        buttons.set("PUBLIC_CHAT.DEFAULT", false);
        buttons.set("NIGHT_VISION.DEFAULT", "on");

        PlayerData data = newPlayer();
        PlayerSettingDefaults.applyDefaults(buttons, data);

        assertEquals(ThreeChoice.OFF, data.getAdvancementMessagesChoice());
        assertEquals(ThreeChoice.OFF, data.getJoinLeaveMessagesChoice());
        assertEquals(ThreeChoice.FRIENDS_FOLLOWED, data.getPrivateMessagesChoice());
        assertEquals(TwoChoice.OFF, data.getDeathMessagesChoice());
        assertFalse(data.isPublicChatEnabled());
        assertTrue(data.isNightVisionEnabled());
    }

    @Test
    void spawnPreventionDefaultsFollowTheButtonLabel() {
        ConfigurationSection buttons = buttons();
        buttons.set("DISABLE_MOB_SPAWN.DEFAULT", true);
        buttons.set("DISABLE_PHANTOM_SPAWN.DEFAULT", true);

        PlayerData data = newPlayer();
        PlayerSettingDefaults.applyDefaults(buttons, data);

        assertFalse(data.isMobSpawnEnabled());
        assertFalse(data.isPhantomEnabled());
        assertEquals(0L, data.getMobSpawnDisabledUntil());
        assertEquals(0L, data.getPhantomDisabledUntil());
    }

    @Test
    void autoConfirmTpasDefaultCoversBothStoredFlags() {
        ConfigurationSection buttons = buttons();
        buttons.set("AUTO_CONFIRM_TPAS.DEFAULT", true);

        PlayerData data = newPlayer();
        PlayerSettingDefaults.applyDefaults(buttons, data);

        assertTrue(data.isTpauto());
        assertTrue(data.isAutoTpaHereEnabled());
    }

    @Test
    void unusableDefaultsAreReportedAndLeaveTheSettingAlone() {
        ConfigurationSection buttons = buttons();
        buttons.set("ADVANCEMENT_MESSAGES.DEFAULT", "sometimes");
        buttons.set("QUICK_AUCTION_SELL.DEFAULT", true);

        PlayerData data = newPlayer();
        List<String> warnings = new ArrayList<>();
        PlayerSettingDefaults.applyDefaults(buttons, data, warnings::add);

        assertEquals(ThreeChoice.ANYONE, data.getAdvancementMessagesChoice());
        assertEquals(2, warnings.size());
    }

    @Test
    void disabledOptionsPinPlayersToTheConfiguredDefault() {
        ConfigurationSection buttons = buttons();
        buttons.set("ADVANCEMENT_MESSAGES.DEFAULT", "OFF");
        buttons.set("ADVANCEMENT_MESSAGES.ENABLED", false);
        buttons.set("PUBLIC_CHAT.ENABLED", false);

        PlayerData data = newPlayer();
        data.setAdvancementMessagesChoice(ThreeChoice.ANYONE);
        data.setPublicChatEnabled(false);
        data.setDirty(false);

        PlayerSettingDefaults.applyDisabledOptions(buttons, data);

        assertEquals(ThreeChoice.OFF, data.getAdvancementMessagesChoice());
        // No DEFAULT configured, so PUBLIC_CHAT falls back to the built-in value.
        assertTrue(data.isPublicChatEnabled());
        assertFalse(data.isDirty(), "pinning a removed option must not schedule a database write");
    }

    @Test
    void enabledOptionsKeepPlayerChoices() {
        ConfigurationSection buttons = buttons();
        buttons.set("ADVANCEMENT_MESSAGES.DEFAULT", "OFF");

        PlayerData data = newPlayer();
        data.setAdvancementMessagesChoice(ThreeChoice.FRIENDS_FOLLOWED);

        PlayerSettingDefaults.applyDisabledOptions(buttons, data);

        assertEquals(ThreeChoice.FRIENDS_FOLLOWED, data.getAdvancementMessagesChoice());
    }

    @Test
    void missingSectionsAndNullDataAreIgnored() {
        PlayerData data = newPlayer();
        PlayerSettingDefaults.applyDefaults((ConfigurationSection) null, data);
        PlayerSettingDefaults.applyDisabledOptions((ConfigurationSection) null, data);
        PlayerSettingDefaults.applyDefaults(buttons(), null);

        assertTrue(PlayerSettingDefaults.isOptionEnabled((ConfigurationSection) null));
        assertTrue(PlayerSettingDefaults.isOptionEnabled(buttons(), "ADVANCEMENT_MESSAGES"));
    }

    private static ConfigurationSection buttons() {
        return new YamlConfiguration().createSection("BUTTONS");
    }

    private static PlayerData newPlayer() {
        return new PlayerData(UUID.randomUUID(), "SettingsTester");
    }
}
