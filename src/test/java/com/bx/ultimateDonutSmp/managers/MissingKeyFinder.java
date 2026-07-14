package com.bx.ultimateDonutSmp.managers;

import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.lang.reflect.Method;

public class MissingKeyFinder {
    @org.junit.jupiter.api.Test
    public void testMissingKey() throws Exception {
        File file = new File("src/main/resources/languages/en_US.yml");
        YamlConfiguration config = new YamlConfiguration();
        config.options().parseComments(true);
        config.load(file);
        
        Method m = LanguageManagerTest.class.getDeclaredMethod("mergeCurrentResourceText", YamlConfiguration.class);
        m.setAccessible(true);
        int added = (int) m.invoke(null, config);
        
        if (added > 0) {
            System.out.println("Adding " + added + " missing keys to en_US.yml");
            config.save(file);
        }
    }
}
