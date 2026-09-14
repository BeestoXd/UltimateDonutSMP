package com.bx.ultimateDonutSmp.managers;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CrateMessageKeyTest {

    private static final List<String> EXPECTED_KEYS = List.of(
            "REWARD-CLAIM-HINT",
            "REWARD-NO-KEY",
            "CLAIM-SUCCESS",
            "CLAIM-BROADCAST",
            "NO-KEYS",
            "INVENTORY-FULL",
            "NO-PLAYER-DATA",
            "REWARD-GRANT-FAILED",
            "INVALID-REWARD",
            "REWARD-SELECT-FAILED"
    );

    private static final List<String> BUNDLED_LOCALES = List.of(
            "en_US", "es_ES", "id_ID", "pt_BR", "de_DE", "fr_FR", "ru_RU", "zh_CN"
    );

    private static YamlConfiguration load(String path) {
        return YamlConfiguration.loadConfiguration(new File(path));
    }

    @Test
    void messagesYmlContainsAllCrateMessageKeys() {
        YamlConfiguration messages = load("src/main/resources/messages.yml");
        for (String key : EXPECTED_KEYS) {
            assertTrue(
                    messages.contains("CRATES." + key),
                    "messages.yml is missing CRATES." + key
            );
        }
    }

    @Test
    void allBundledLanguagesContainCrateMessageKeys() {
        for (String locale : BUNDLED_LOCALES) {
            YamlConfiguration language = load("src/main/resources/languages/" + locale + ".yml");
            for (String key : EXPECTED_KEYS) {
                assertTrue(
                        language.contains("MESSAGES.CRATES." + key),
                        locale + ".yml is missing MESSAGES.CRATES." + key
                );
            }
        }
    }
}
