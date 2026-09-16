package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.models.ThreeChoice;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class PlayerSettingsFeedbackTest {

    @Test
    void bundledMenusHasCompleteFeedbackSection() {
        YamlConfiguration config = loadMenusYaml();
        ConfigurationSection feedback = config.getConfigurationSection("SETTINGS-MENU.FEEDBACK");
        assertNotNull(feedback, "SETTINGS-MENU.FEEDBACK must be defined in menus.yml");

        assertEquals("&7{setting} is now {state}&7.", feedback.getString("TOGGLE-MESSAGE"));
        assertEquals("&7{setting} is now set to {choice}&7.", feedback.getString("CHOICE-MESSAGE"));
        assertEquals("&aEnabled", feedback.getString("STATE-ENABLED"));
        assertEquals("&cDisabled", feedback.getString("STATE-DISABLED"));
        assertEquals("&cOff", feedback.getString("CHOICE-OFF-TEXT"));
        assertEquals("&aAnyone", feedback.getString("CHOICE-ANYONE-TEXT"));
        assertEquals("&dFriends/Followed", feedback.getString("CHOICE-FRIENDS-FOLLOWED-TEXT"));
    }

    @Test
    void defaultToggleFeedbackUsesBundledTemplates() {
        YamlConfiguration config = loadMenusYaml();
        ConfigurationSection feedback = config.getConfigurationSection("SETTINGS-MENU.FEEDBACK");
        ConfigurationSection button = config.getConfigurationSection("SETTINGS-MENU.BUTTONS.NOTIFICATION_SOUNDS");

        String enabledMsg = PlayerSettingsMenu.formatToggleFeedback(
                feedback, button, "NOTIFICATION_SOUNDS", "Notification Sounds", true
        );
        assertEquals("&7Notification Sounds is now &aEnabled&7.", enabledMsg);

        String disabledMsg = PlayerSettingsMenu.formatToggleFeedback(
                feedback, button, "NOTIFICATION_SOUNDS", "Notification Sounds", false
        );
        assertEquals("&7Notification Sounds is now &cDisabled&7.", disabledMsg);
    }

    @Test
    void customFontAndSmallCapsInGlobalFeedbackTemplate() {
        YamlConfiguration config = new YamlConfiguration();
        ConfigurationSection feedback = config.createSection("FEEDBACK");
        feedback.set("TOGGLE-MESSAGE", ">>> {setting} ɪꜱ ɴᴏᴡ {state}");
        feedback.set("STATE-ENABLED", "&aᴇɴᴀʙʟᴇᴅ");
        feedback.set("STATE-DISABLED", "&cᴅɪꜱᴀʙʟᴇᴅ");

        String msg = PlayerSettingsMenu.formatToggleFeedback(
                feedback, null, "NOTIFICATION_SOUNDS", "ɴᴏᴛɪꜰɪᴄᴀᴛɪᴏɴ ꜱᴏᴜɴᴅꜱ", true
        );
        assertEquals(">>> ɴᴏᴛɪꜰɪᴄᴀᴛɪᴏɴ ꜱᴏᴜɴᴅꜱ ɪꜱ ɴᴏᴡ &aᴇɴᴀʙʟᴇᴅ", msg);
    }

    @Test
    void perButtonFeedbackNameOverridesDefaultLabel() {
        YamlConfiguration config = loadMenusYaml();
        ConfigurationSection feedback = config.getConfigurationSection("SETTINGS-MENU.FEEDBACK");

        YamlConfiguration buttonConfig = new YamlConfiguration();
        ConfigurationSection button = buttonConfig.createSection("NOTIFICATION_SOUNDS");
        button.set("FEEDBACK-NAME", "ɴᴏᴛɪꜰɪᴄᴀᴛɪᴏɴ ꜱᴏᴜɴᴅꜱ");

        String msg = PlayerSettingsMenu.formatToggleFeedback(
                feedback, button, "NOTIFICATION_SOUNDS", "Notification Sounds", true
        );
        assertEquals("&7ɴᴏᴛɪꜰɪᴄᴀᴛɪᴏɴ ꜱᴏᴜɴᴅꜱ is now &aEnabled&7.", msg);
    }

    @Test
    void perButtonFeedbackMessageOverridesGlobalTemplate() {
        YamlConfiguration config = loadMenusYaml();
        ConfigurationSection feedback = config.getConfigurationSection("SETTINGS-MENU.FEEDBACK");

        YamlConfiguration buttonConfig = new YamlConfiguration();
        ConfigurationSection button = buttonConfig.createSection("NOTIFICATION_SOUNDS");
        button.set("FEEDBACK-MESSAGE", "&e[Audio] {setting} state: {state}");

        String msg = PlayerSettingsMenu.formatToggleFeedback(
                feedback, button, "NOTIFICATION_SOUNDS", "Notification Sounds", true
        );
        assertEquals("&e[Audio] Notification Sounds state: &aEnabled", msg);
    }

    @Test
    void customDisplayNameAutomaticallyUsedWhenCustomized() {
        YamlConfiguration config = loadMenusYaml();
        ConfigurationSection feedback = config.getConfigurationSection("SETTINGS-MENU.FEEDBACK");

        YamlConfiguration buttonConfig = new YamlConfiguration();
        ConfigurationSection button = buttonConfig.createSection("NOTIFICATION_SOUNDS");
        button.set("DISPLAY-NAME", "&6&lɴᴏᴛɪꜰɪᴄᴀᴛɪᴏɴ ꜱᴏᴜɴᴅꜱ");

        String msg = PlayerSettingsMenu.formatToggleFeedback(
                feedback, button, "NOTIFICATION_SOUNDS", "Notification Sounds", true
        );
        assertEquals("&7ɴᴏᴛɪꜰɪᴄᴀᴛɪᴏɴ ꜱᴏᴜɴᴅꜱ is now &aEnabled&7.", msg);
    }

    @Test
    void settingDisplayPlaceholderIncludesColorCodes() {
        YamlConfiguration config = new YamlConfiguration();
        ConfigurationSection feedback = config.createSection("FEEDBACK");
        feedback.set("TOGGLE-MESSAGE", "{setting_display} &7-> {state}");
        feedback.set("STATE-ENABLED", "&aON");

        YamlConfiguration buttonConfig = new YamlConfiguration();
        ConfigurationSection button = buttonConfig.createSection("FAST_CRYSTALS");
        button.set("DISPLAY-NAME", "&bFast Crystals");

        String msg = PlayerSettingsMenu.formatToggleFeedback(
                feedback, button, "FAST_CRYSTALS", "Fast Crystals", true
        );
        assertEquals("&bFast Crystals &7-> &aON", msg);
    }

    @Test
    void choiceFeedbackFormattingWithCustomChoiceTexts() {
        YamlConfiguration config = new YamlConfiguration();
        ConfigurationSection feedback = config.createSection("FEEDBACK");
        feedback.set("CHOICE-MESSAGE", "&8[{setting}] &7Choice changed to {choice}&7.");
        feedback.set("CHOICE-OFF-TEXT", "&cᴏꜰꜰ");
        feedback.set("CHOICE-ANYONE-TEXT", "&aᴇᴠᴇʀʏᴏɴᴇ");
        feedback.set("CHOICE-FRIENDS-FOLLOWED-TEXT", "&dꜰʀɪᴇɴᴅꜱ");

        String choiceTextOff = PlayerSettingsMenu.formatFeedbackChoice(feedback, ThreeChoice.OFF);
        assertEquals("&cᴏꜰꜰ", choiceTextOff);

        String msgOff = PlayerSettingsMenu.formatChoiceFeedback(
                feedback, null, "PRIVATE_MESSAGES", "Private Messages", choiceTextOff
        );
        assertEquals("&8[Private Messages] &7Choice changed to &cᴏꜰꜰ&7.", msgOff);

        String choiceTextAnyone = PlayerSettingsMenu.formatFeedbackChoice(feedback, ThreeChoice.ANYONE);
        assertEquals("&aᴇᴠᴇʀʏᴏɴᴇ", choiceTextAnyone);

        String msgAnyone = PlayerSettingsMenu.formatChoiceFeedback(
                feedback, null, "PRIVATE_MESSAGES", "Private Messages", choiceTextAnyone
        );
        assertEquals("&8[Private Messages] &7Choice changed to &aᴇᴠᴇʀʏᴏɴᴇ&7.", msgAnyone);
    }

    @Test
    void feedbackCanBeSilencedWithNoneOrBlank() {
        YamlConfiguration config = new YamlConfiguration();
        ConfigurationSection feedback = config.createSection("FEEDBACK");
        feedback.set("TOGGLE-MESSAGE", "none");

        String msg = PlayerSettingsMenu.formatToggleFeedback(
                feedback, null, "HOTBAR_MESSAGES", "Hotbar Notifications", true
        );
        assertNull(msg, "Template 'none' should silence feedback");

        feedback.set("TOGGLE-MESSAGE", "   ");
        String msgBlank = PlayerSettingsMenu.formatToggleFeedback(
                feedback, null, "HOTBAR_MESSAGES", "Hotbar Notifications", true
        );
        assertNull(msgBlank, "Blank template should silence feedback");
    }

    private static YamlConfiguration loadMenusYaml() {
        try (InputStream in = PlayerSettingsFeedbackTest.class.getClassLoader().getResourceAsStream("menus.yml")) {
            Objects.requireNonNull(in, "menus.yml resource not found");
            YamlConfiguration config = new YamlConfiguration();
            config.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            return config;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load menus.yml", e);
        }
    }
}
