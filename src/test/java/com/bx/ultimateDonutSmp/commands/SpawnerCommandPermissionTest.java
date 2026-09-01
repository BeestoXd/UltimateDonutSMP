package com.bx.ultimateDonutSmp.commands;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpawnerCommandPermissionTest {

    @Test
    void spawnerCommandIsReachableByPlayersWhileAdminWorkStaysOp() throws Exception {
        YamlConfiguration plugin = new YamlConfiguration();
        // Permission nodes are dotted keys, so path traversal has to walk on something else.
        plugin.options().pathSeparator('/');
        plugin.load(new File("src/main/resources/plugin.yml"));

        // /spawner info and /spawner split carry no admin check of their own, so the command
        // node has to reach ordinary players or both of them are unusable.
        assertEquals("true", plugin.getString("permissions/ultimatedonutsmp.command.spawner/default"));
        assertEquals("op", plugin.getString("permissions/ultimatedonutsmp.admin.spawner/default"));
    }
}
