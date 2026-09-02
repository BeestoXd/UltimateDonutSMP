package com.bx.ultimateDonutSmp.utils;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.permissions.Permissible;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Private messages and staff chat colour the whole line after the typed message has gone into it,
 * so anyone who could use them could colour their own text, hide it with {@code &k}, or clear the
 * format's colours with {@code &r}. Public chat never did, because it leaves the typed text alone.
 */
class TypedColorPolicyTest {

    @Test
    void aSenderWithoutThePermissionLosesTheColourCodesTheyTyped() {
        assertEquals("hello", TypedColorPolicy.apply(sender(), "&chello"));
    }

    @Test
    void obfuscatedAndResetCodesComeOutToo() {
        assertEquals("hello", TypedColorPolicy.apply(sender(), "&khello"));
        assertEquals("hello", TypedColorPolicy.apply(sender(), "&rhello"));
    }

    @Test
    void gradientAndMiniMessageTagsComeOutAsWell() {
        assertEquals("hello", TypedColorPolicy.apply(
                sender(), "<gradient:#FF0000:#0000FF>hello</gradient>"));
        assertEquals("hello", TypedColorPolicy.apply(sender(), "<red>hello"));
        assertEquals("hello", TypedColorPolicy.apply(sender(), "&#FF0000hello"));
    }

    @Test
    void aSenderHoldingThePermissionKeepsEveryCharacterTheyTyped() {
        String typed = "<gradient:#FF0000:#0000FF>hey</gradient> &kthere &rok";

        assertEquals(typed, TypedColorPolicy.apply(sender(TypedColorPolicy.PERMISSION), typed));
    }

    @Test
    void ordinaryTextIsLeftAloneEitherWay() {
        assertEquals("anyone on?", TypedColorPolicy.apply(sender(), "anyone on?"));
        assertEquals("anyone on?", TypedColorPolicy.apply(sender(TypedColorPolicy.PERMISSION), "anyone on?"));
    }

    @Test
    void nullAndEmptyMessagesSurviveUntouched() {
        assertEquals(null, TypedColorPolicy.apply(sender(), null));
        assertEquals("", TypedColorPolicy.apply(sender(), ""));
    }

    @Test
    void aMissingSenderIsNotAllowedToColour() {
        assertFalse(TypedColorPolicy.mayColour(null));
        assertEquals("hello", TypedColorPolicy.apply(null, "&chello"));
    }

    @Test
    void thePermissionIsTheOneDeclaredInPluginYml() {
        assertEquals("ultimatedonutsmp.chat.color", TypedColorPolicy.PERMISSION);
        assertTrue(TypedColorPolicy.mayColour(sender(TypedColorPolicy.PERMISSION)));
        assertFalse(TypedColorPolicy.mayColour(sender()));
    }

    @Test
    void pluginYmlDeclaresTheNodeAndKeepsItOffByDefault() throws Exception {
        YamlConfiguration plugin = new YamlConfiguration();
        plugin.load(new File("src/main/resources/plugin.yml"));

        String node = "permissions." + TypedColorPolicy.PERMISSION;
        assertTrue(plugin.isConfigurationSection(node), node);
        assertEquals("op", plugin.getString(node + ".default"));

        // Staff pick it up through the bundles they already hold, so staff chat keeps working.
        assertTrue(plugin.getBoolean("permissions.ultimatedonutsmp.admin.children."
                + TypedColorPolicy.PERMISSION));
        assertTrue(plugin.getBoolean("permissions.ultimatedonutsmp.staff.mode.children."
                + TypedColorPolicy.PERMISSION));
    }

    private static Permissible sender(String... permissions) {
        Set<String> held = new HashSet<>(Set.of(permissions));
        return (Permissible) Proxy.newProxyInstance(
                Permissible.class.getClassLoader(),
                new Class<?>[]{Permissible.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "hasPermission" -> held.contains(String.valueOf(args[0]));
                    case "getEffectivePermissions" -> new HashSet<>();
                    default -> null;
                }
        );
    }
}
