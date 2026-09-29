package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import sun.reflect.ReflectionFactory;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HomeWorldBlacklistTest {

    private record PluginSetup(UltimateDonutSmp plugin, HomeManager homeManager, TeamManager teamManager) {}

    private PluginSetup createSetup(YamlConfiguration mainConfig) throws Exception {
        Constructor<Object> objectConstructor = Object.class.getConstructor();
        ReflectionFactory reflectionFactory = ReflectionFactory.getReflectionFactory();

        Constructor<?> pluginConstructor = reflectionFactory
                .newConstructorForSerialization(UltimateDonutSmp.class, objectConstructor);
        UltimateDonutSmp plugin = (UltimateDonutSmp) pluginConstructor.newInstance();

        ConfigManager configManager = new ConfigManager(plugin);
        Field configField = ConfigManager.class.getDeclaredField("config");
        configField.setAccessible(true);
        configField.set(configManager, mainConfig);

        Field configManagerField = UltimateDonutSmp.class.getDeclaredField("configManager");
        configManagerField.setAccessible(true);
        configManagerField.set(plugin, configManager);

        HomeManager homeManager = new HomeManager(plugin);
        Field homeManagerField = UltimateDonutSmp.class.getDeclaredField("homeManager");
        homeManagerField.setAccessible(true);
        homeManagerField.set(plugin, homeManager);

        TeamManager teamManager = new TeamManager(plugin);
        Field teamManagerField = UltimateDonutSmp.class.getDeclaredField("teamManager");
        teamManagerField.setAccessible(true);
        teamManagerField.set(plugin, teamManager);

        return new PluginSetup(plugin, homeManager, teamManager);
    }

    private World createMockWorld(String name) {
        return (World) Proxy.newProxyInstance(
                World.class.getClassLoader(),
                new Class<?>[]{World.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("getName")) {
                        return name;
                    }
                    return null;
                }
        );
    }

    @Test
    void homeManagerExcludesConfiguredWorlds() throws Exception {
        YamlConfiguration config = new YamlConfiguration();
        config.set("SETTINGS.HOME-EXCLUDED-WORLDS", List.of("spawn", "afk_zone", "event_world"));

        PluginSetup setup = createSetup(config);
        HomeManager manager = setup.homeManager();

        assertTrue(manager.isWorldExcluded("spawn"));
        assertTrue(manager.isWorldExcluded("SPAWN"));
        assertTrue(manager.isWorldExcluded("  spawn  "));
        assertTrue(manager.isWorldExcluded("afk_zone"));
        assertTrue(manager.isWorldExcluded("event_world"));

        assertFalse(manager.isWorldExcluded("world"));
        assertFalse(manager.isWorldExcluded("world_nether"));
        assertFalse(manager.isWorldExcluded(""));
        assertFalse(manager.isWorldExcluded((String) null));

        World spawnWorld = createMockWorld("spawn");
        World survivalWorld = createMockWorld("world");
        assertTrue(manager.isWorldExcluded(spawnWorld));
        assertFalse(manager.isWorldExcluded(survivalWorld));
        assertFalse(manager.isWorldExcluded((World) null));
    }

    @Test
    void teamManagerExcludesConfiguredWorldsExplicitly() throws Exception {
        YamlConfiguration config = new YamlConfiguration();
        config.set("SETTINGS.HOME-EXCLUDED-WORLDS", List.of("spawn"));
        config.set("TEAM.EXCLUDED-WORLDS", List.of("team_spawn", "arena_world"));

        PluginSetup setup = createSetup(config);
        TeamManager manager = setup.teamManager();

        assertTrue(manager.isWorldExcluded("team_spawn"));
        assertTrue(manager.isWorldExcluded("TEAM_SPAWN"));
        assertTrue(manager.isWorldExcluded("arena_world"));

        // When TEAM.EXCLUDED-WORLDS is set, it overrides the home list
        assertFalse(manager.isWorldExcluded("spawn"));
        assertFalse(manager.isWorldExcluded("world"));
    }

    @Test
    void teamManagerFallsBackToHomeExcludedWorldsWhenEmpty() throws Exception {
        YamlConfiguration config = new YamlConfiguration();
        config.set("SETTINGS.HOME-EXCLUDED-WORLDS", List.of("spawn", "hub"));

        PluginSetup setup = createSetup(config);
        TeamManager manager = setup.teamManager();

        assertTrue(manager.isWorldExcluded("spawn"));
        assertTrue(manager.isWorldExcluded("hub"));
        assertFalse(manager.isWorldExcluded("world"));
    }

    @Test
    void bundledConfigContainsWorldBlacklistSettings() throws Exception {
        YamlConfiguration bundled = new YamlConfiguration();
        bundled.load(new File("src/main/resources/config.yml"));

        assertTrue(bundled.isList("SETTINGS.HOME-EXCLUDED-WORLDS"));
        assertTrue(bundled.isList("TEAM.EXCLUDED-WORLDS"));
    }

    @Test
    void bundledMessagesContainWorldBlacklistKeys() throws Exception {
        YamlConfiguration bundled = new YamlConfiguration();
        bundled.load(new File("src/main/resources/messages.yml"));

        assertTrue(bundled.contains("HOME.EXCLUDED-WORLD"));
        assertTrue(bundled.contains("TEAM.EXCLUDED-WORLD"));
    }
}
