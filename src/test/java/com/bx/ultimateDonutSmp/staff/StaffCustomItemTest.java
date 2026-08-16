package com.bx.ultimateDonutSmp.staff;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StaffCustomItemTest {

    private static final Set<Integer> BUILT_IN_SLOTS = Set.of(0, 1, 4, 7, 8);

    @Test
    void parsesAFullDefinition() throws Exception {
        List<String> warnings = new ArrayList<>();
        List<StaffCustomItem> items = parse("""
                CUSTOM-ITEMS:
                  invsee:
                    SLOT: 2
                    MATERIAL: CHEST
                    NAME: '&eInventory'
                    LORE:
                    - '&7Right-click a player'
                    EXECUTE-AS: CONSOLE
                    PERMISSION: ultimatedonutsmp.staff.mode.custom.invsee
                    REQUIRE-TARGET: true
                    COMMANDS:
                    - '/invsee {target}'
                """, warnings);

        assertEquals(1, items.size());
        StaffCustomItem item = items.get(0);
        assertEquals("INVSEE", item.id());
        assertEquals(2, item.slot());
        assertEquals(Material.CHEST, item.material());
        assertEquals(List.of("&7Right-click a player"), item.lore());
        assertEquals(StaffCustomItem.ExecuteAs.CONSOLE, item.executeAs());
        assertTrue(item.hasPermission());
        assertTrue(item.requireTarget());
        assertEquals(List.of("invsee {target}"), item.commands(), "leading slashes must be stripped");
        assertTrue(warnings.isEmpty(), () -> "unexpected warnings: " + warnings);
    }

    @Test
    void appliesDefaultsForOptionalKeys() throws Exception {
        List<StaffCustomItem> items = parse("""
                CUSTOM-ITEMS:
                  minimal:
                    SLOT: 3
                    COMMANDS:
                    - 'spawn'
                """, new ArrayList<>());

        assertEquals(1, items.size());
        StaffCustomItem item = items.get(0);
        assertEquals(Material.STONE, item.material());
        assertEquals(StaffCustomItem.ExecuteAs.PLAYER, item.executeAs(), "commands must default to the staff member");
        assertFalse(item.hasPermission());
        assertFalse(item.requireTarget());
    }

    @Test
    void acceptsASingleStringCommand() throws Exception {
        List<StaffCustomItem> items = parse("""
                CUSTOM-ITEMS:
                  single:
                    SLOT: 3
                    COMMANDS: 'heal {player}'
                """, new ArrayList<>());

        assertEquals(List.of("heal {player}"), items.get(0).commands());
    }

    @Test
    void skipsDisabledEntriesWithoutWarning() throws Exception {
        List<String> warnings = new ArrayList<>();
        List<StaffCustomItem> items = parse("""
                CUSTOM-ITEMS:
                  off:
                    ENABLED: false
                    SLOT: 3
                    COMMANDS:
                    - 'spawn'
                """, warnings);

        assertTrue(items.isEmpty());
        assertTrue(warnings.isEmpty());
    }

    @Test
    void rejectsSlotsOutsideTheHotbar() throws Exception {
        List<String> warnings = new ArrayList<>();
        List<StaffCustomItem> items = parse("""
                CUSTOM-ITEMS:
                  missing-slot:
                    COMMANDS:
                    - 'spawn'
                  too-high:
                    SLOT: 9
                    COMMANDS:
                    - 'spawn'
                """, warnings);

        assertTrue(items.isEmpty());
        assertEquals(2, warnings.size());
    }

    @Test
    void rejectsSlotsHeldByBuiltInToolsAndOtherCustomItems() throws Exception {
        List<String> warnings = new ArrayList<>();
        List<StaffCustomItem> items = parse("""
                CUSTOM-ITEMS:
                  clashes-with-vanish:
                    SLOT: 0
                    COMMANDS:
                    - 'spawn'
                  first:
                    SLOT: 2
                    COMMANDS:
                    - 'spawn'
                  second:
                    SLOT: 2
                    COMMANDS:
                    - 'spawn'
                """, warnings);

        assertEquals(1, items.size());
        assertEquals("FIRST", items.get(0).id());
        assertEquals(2, warnings.size());
    }

    @Test
    void rejectsEntriesWithoutRunnableCommands() throws Exception {
        List<String> warnings = new ArrayList<>();
        List<StaffCustomItem> items = parse("""
                CUSTOM-ITEMS:
                  no-commands:
                    SLOT: 2
                  blank-commands:
                    SLOT: 3
                    COMMANDS:
                    - '   '
                    - '/'
                """, warnings);

        assertTrue(items.isEmpty());
        assertEquals(2, warnings.size());
    }

    @Test
    void rejectsAnUnknownExecuteAsInsteadOfGuessing() throws Exception {
        // Falling back to PLAYER would be surprising, and falling back to CONSOLE would be unsafe,
        // so a typo must drop the item rather than run it with the wrong rights.
        List<String> warnings = new ArrayList<>();
        List<StaffCustomItem> items = parse("""
                CUSTOM-ITEMS:
                  typo:
                    SLOT: 2
                    EXECUTE-AS: SERVER
                    COMMANDS:
                    - 'spawn'
                """, warnings);

        assertTrue(items.isEmpty());
        assertEquals(1, warnings.size());
    }

    @Test
    void keepsAnUnknownMaterialUsableWithAWarning() throws Exception {
        List<String> warnings = new ArrayList<>();
        List<StaffCustomItem> items = parse("""
                CUSTOM-ITEMS:
                  bad-material:
                    SLOT: 2
                    MATERIAL: NOT_A_REAL_BLOCK
                    COMMANDS:
                    - 'spawn'
                """, warnings);

        assertEquals(1, items.size());
        assertEquals(Material.STONE, items.get(0).material());
        assertEquals(1, warnings.size());
    }

    @Test
    void bundledExampleIsValidAndShipsDisabled() throws Exception {
        YamlConfiguration config = new YamlConfiguration();
        config.load(Path.of("src/main/resources", "staff-mode.yml").toFile());

        assertNotNull(config.getConfigurationSection("CUSTOM-ITEMS"),
                "staff-mode.yml must document the CUSTOM-ITEMS section");
        assertFalse(config.getBoolean("CUSTOM-ITEMS.EXAMPLE.ENABLED", true),
                "the bundled example must ship disabled so a config restore never arms a command item");
        assertNotNull(config.getString("MESSAGES.CUSTOM-ITEM-NO-TARGET"),
                "the require-target rejection needs a configurable message");

        List<String> warnings = new ArrayList<>();
        StaffCustomItem.parseAll(config.getConfigurationSection("CUSTOM-ITEMS"), BUILT_IN_SLOTS, warnings::add);
        assertTrue(warnings.isEmpty(), () -> "bundled example is misconfigured: " + warnings);

        // The example is only useful if it loads once an admin flips ENABLED.
        config.set("CUSTOM-ITEMS.EXAMPLE.ENABLED", true);
        List<StaffCustomItem> enabled = StaffCustomItem.parseAll(
                config.getConfigurationSection("CUSTOM-ITEMS"), BUILT_IN_SLOTS, warnings::add);
        assertEquals(1, enabled.size(), () -> "bundled example does not load when enabled: " + warnings);
        assertEquals(StaffCustomItem.ExecuteAs.PLAYER, enabled.get(0).executeAs());
    }

    @Test
    void aRenamedEnabledEntryOnAFreeSlotLoads() throws Exception {
        // The shape reported in #145: the bundled example renamed, armed, and moved to a free slot.
        List<String> warnings = new ArrayList<>();
        List<StaffCustomItem> items = parse("""
                CUSTOM-ITEMS:
                  SUS:
                    ENABLED: true
                    SLOT: 6
                    MATERIAL: TNT
                    NAME: '&cS&cU&cS'
                    LORE:
                    - '&7Click to open /sus'
                    EXECUTE-AS: PLAYER
                    PERMISSION: ''
                    REQUIRE-TARGET: false
                    COMMANDS:
                    - 'sus'
                """, warnings);

        assertEquals(1, items.size(), () -> "the entry was rejected: " + warnings);
        StaffCustomItem item = items.get(0);
        assertEquals("SUS", item.id());
        assertEquals(6, item.slot());
        assertEquals(Material.TNT, item.material());
        assertEquals(StaffCustomItem.ExecuteAs.PLAYER, item.executeAs());
        assertFalse(item.hasPermission(), "an empty PERMISSION must not gate the item");
        assertEquals(List.of("sus"), item.commands());
        assertTrue(warnings.isEmpty(), () -> "unexpected warnings: " + warnings);
    }

    private static List<StaffCustomItem> parse(String yaml, List<String> warnings) throws Exception {
        YamlConfiguration config = new YamlConfiguration();
        config.loadFromString(yaml);
        return StaffCustomItem.parseAll(config.getConfigurationSection("CUSTOM-ITEMS"), BUILT_IN_SLOTS, warnings::add);
    }
}
