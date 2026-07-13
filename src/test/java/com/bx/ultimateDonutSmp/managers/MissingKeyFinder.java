package com.bx.ultimateDonutSmp.managers;

import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;

public class MissingKeyFinder {
    @org.junit.jupiter.api.Test
    public void testMissingKey() throws Exception {
        YamlConfiguration config = new YamlConfiguration();
        config.load(new File("src/main/resources/languages/en_US.yml"));
        
        java.lang.reflect.Method m = LanguageManagerTest.class.getDeclaredMethod("mergeCurrentResourceText", YamlConfiguration.class);
        m.setAccessible(true);
        int added = (int) m.invoke(null, config);
        
        // Let's do a diff between original en_US and new en_US!
        YamlConfiguration original = new YamlConfiguration();
        original.load(new File("src/main/resources/languages/en_US.yml"));
        
        for (String key : config.getKeys(true)) {
            if (!config.isConfigurationSection(key)) {
                if (!original.contains(key)) {
                    org.junit.jupiter.api.Assertions.fail("MISSING KEY: " + key);
                }
            }
        }
    }
}
