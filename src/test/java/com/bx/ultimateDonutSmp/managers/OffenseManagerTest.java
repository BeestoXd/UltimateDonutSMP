package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.models.PunishmentType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OffenseManagerTest {

    @Test
    void parsesNameTypeAndDurations() throws Exception {
        OffenseManager.OffenseRule rule = parse("""
                duping:
                  name: "Item Duping"
                  type: BAN
                  durations:
                    - "3d"
                    - "perm"
                """, "duping");

        assertEquals("duping", rule.key());
        assertEquals("Item Duping", rule.name());
        assertEquals(PunishmentType.BAN, rule.type());
        assertEquals("3d", rule.getDurationForTier(0));
        assertEquals("perm", rule.getDurationForTier(7));
    }

    @Test
    void offenseWithoutWipeKeyDoesNotWipe() throws Exception {
        assertFalse(parse("""
                duping:
                  name: "Item Duping"
                  type: BAN
                  durations:
                    - "3d"
                """, "duping").wipe());
    }

    @Test
    void wipeFlagIsReadUnderAnyCasing() throws Exception {
        for (String key : new String[]{"wipe", "WIPE", "Wipe"}) {
            assertTrue(parse("""
                    botting:
                      name: "Botting"
                      type: BAN
                      %s: true
                      durations:
                        - "perm"
                    """.formatted(key), "botting").wipe(), key + " should enable the wipe");
        }
    }

    @Test
    void quotedWipeValueStillCounts() throws Exception {
        assertTrue(parse("""
                botting:
                  name: "Botting"
                  type: BAN
                  wipe: "true"
                  durations:
                    - "perm"
                """, "botting").wipe());

        assertFalse(parse("""
                botting:
                  name: "Botting"
                  type: BAN
                  wipe: "false"
                  durations:
                    - "perm"
                """, "botting").wipe());
    }

    @Test
    void unknownTypeFallsBackToBan() throws Exception {
        assertEquals(PunishmentType.BAN, parse("""
                mystery:
                  name: "Mystery"
                  type: SOMETHING-ELSE
                  durations:
                    - "1d"
                """, "mystery").type());
    }

    @Test
    void bundledOffensesParseAndNoneWipeByDefault() {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(new File("src/main/resources/offenses.yml"));
        ConfigurationSection offenses = config.getConfigurationSection("offenses");
        assertNotNull(offenses, "offenses.yml should contain an offenses section");

        for (String key : offenses.getKeys(false)) {
            ConfigurationSection section = offenses.getConfigurationSection(key);
            assertNotNull(section, key + " should be a section");
            OffenseManager.OffenseRule rule = OffenseManager.parseRule(key, section);
            assertFalse(rule.wipe(), key + " must not wipe out of the box");
            assertNotNull(rule.type(), key + " should resolve a punishment type");
        }
    }

    private OffenseManager.OffenseRule parse(String yaml, String key) throws Exception {
        YamlConfiguration config = new YamlConfiguration();
        config.load(new StringReader(yaml));
        ConfigurationSection section = config.getConfigurationSection(key);
        assertNotNull(section, "test yaml should define " + key);
        return OffenseManager.parseRule(key, section);
    }
}
