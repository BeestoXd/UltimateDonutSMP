package com.bx.ultimateDonutSmp.utils;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;

public final class AttributeUtils {

    private static final Attribute MAX_HEALTH = findAttribute("MAX_HEALTH", "GENERIC_MAX_HEALTH");

    private AttributeUtils() {
    }

    public static AttributeInstance getMaxHealthAttribute(Player player) {
        return player == null || MAX_HEALTH == null ? null : player.getAttribute(MAX_HEALTH);
    }

    public static double getMaxHealth(Player player) {
        AttributeInstance attribute = getMaxHealthAttribute(player);
        return attribute == null ? 20D : attribute.getValue();
    }

    private static Attribute findAttribute(String... names) {
        for (String name : names) {
            Attribute attribute = findAttribute(name);
            if (attribute != null) {
                return attribute;
            }
        }
        return null;
    }

    private static Attribute findAttribute(String name) {
        try {
            Method valueOf = Attribute.class.getMethod("valueOf", String.class);
            Object value = valueOf.invoke(null, name);
            if (value instanceof Attribute attribute) {
                return attribute;
            }
        } catch (ReflectiveOperationException | RuntimeException ignored) {
        }

        try {
            Object value = Attribute.class.getField(name).get(null);
            if (value instanceof Attribute attribute) {
                return attribute;
            }
        } catch (ReflectiveOperationException | RuntimeException ignored) {
        }

        return null;
    }
}
