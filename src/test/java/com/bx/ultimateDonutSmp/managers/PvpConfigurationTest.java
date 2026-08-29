package com.bx.ultimateDonutSmp.managers;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Guards the shape of the bundled ranked arena config and its command registration. */
class PvpConfigurationTest {

    private static YamlConfiguration pvp() throws Exception {
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.load(new File("src/main/resources/pvp.yml"));
        return configuration;
    }

    @Test
    void theArenaShipsSwitchedOffWithAnEmptyArenaAndNoKits() throws Exception {
        YamlConfiguration pvp = pvp();

        // Nothing about this feature can work before an admin has run the setup commands, so it
        // must not start doing anything on a server that merely updated the plugin.
        assertFalse(pvp.getBoolean("SETTINGS.ENABLED"));
        assertFalse(pvp.getBoolean("RESET.ENABLED"));
        assertEquals("", pvp.getString("ARENA.SPAWN"));
        assertTrue(pvp.getConfigurationSection("KITS").getKeys(false).isEmpty());
    }

    @Test
    void theDefaultRankLadderRisesWithoutGaps() throws Exception {
        YamlConfiguration pvp = pvp();
        assertNotNull(pvp.getConfigurationSection("RANKS"));

        int previous = Integer.MIN_VALUE;
        for (String id : pvp.getConfigurationSection("RANKS").getKeys(false)) {
            int elo = pvp.getInt("RANKS." + id + ".ELO");
            assertTrue(elo > previous, id + " must need more elo than the rank before it");
            assertFalse(pvp.getString("RANKS." + id + ".DISPLAY", "").isBlank());
            previous = elo;
        }
        assertEquals(0, pvp.getInt("RANKS.LT5.ELO"), "the bottom rank has to be reachable from zero elo");
    }

    @Test
    void theResetIsDrivenByCommandsThatWorkOnWorldEditAndFawe() throws Exception {
        List<String> commands = pvp().getStringList("RESET.COMMANDS");

        assertEquals(2, commands.size());
        assertTrue(commands.get(0).contains("{schematic}"), "the load command has to name the schematic");
        assertTrue(commands.get(1).startsWith("//paste"));
        assertTrue(pvp().getInt("RESET.PASTE_DELAY_SECONDS") > 0, "a paste must not race the load");
    }

    @Test
    void theSurvivalCommandsTheReporterListedAreBlockedByDefault() throws Exception {
        List<String> blocked = pvp().getStringList("BLOCKED_COMMANDS.COMMANDS");

        assertTrue(pvp().getBoolean("BLOCKED_COMMANDS.ENABLED"));
        assertTrue(pvp().getBoolean("BLOCKED_COMMANDS.BLOCK_ENDER_CHEST_BLOCK"));
        for (String command : List.of("shop", "sell", "ah", "ec", "enderchest", "orders")) {
            assertTrue(blocked.contains(command), "expected /" + command + " to be blocked by default");
        }
    }

    @Test
    void theCommandAndItsPermissionsAreRegistered() throws Exception {
        YamlConfiguration plugin = new YamlConfiguration();
        plugin.load(new File("src/main/resources/plugin.yml"));

        assertTrue(plugin.isConfigurationSection("commands.pvp"));
        assertEquals("ultimatedonutsmp.command.pvp", plugin.getString("commands.pvp.permission"));
        assertEquals("true", plugin.getString("permissions.ultimatedonutsmp.command.pvp.default"));
        assertEquals("op", plugin.getString("permissions.ultimatedonutsmp.admin.pvp.default"));
        assertTrue(plugin.getBoolean(
                "permissions.ultimatedonutsmp.admin.children.ultimatedonutsmp.admin.pvp"
        ));
    }
}
