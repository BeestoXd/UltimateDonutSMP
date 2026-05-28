package com.bx.ultimateDonutSmp.utils;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Base64;

public final class ItemSerializationUtils {

    private static final String BYTE_SERIALIZATION_PREFIX = "ITEM_BYTES_V1:";

    private ItemSerializationUtils() {
    }

    public static boolean isByteSerialized(String encoded) {
        return encoded != null && encoded.startsWith(BYTE_SERIALIZATION_PREFIX);
    }

    public static String serialize(ItemStack item) throws IOException {
        byte[] serializedBytes = serializeAsBytes(item);
        if (serializedBytes != null) {
            return BYTE_SERIALIZATION_PREFIX + Base64.getEncoder().encodeToString(serializedBytes);
        }

        ByteArrayOutputStream outputBytes = new ByteArrayOutputStream();
        try (BukkitObjectOutputStream output = new BukkitObjectOutputStream(outputBytes)) {
            output.writeObject(item);
        }
        return Base64.getEncoder().encodeToString(outputBytes.toByteArray());
    }

    public static ItemStack deserialize(String encoded) throws IOException, ClassNotFoundException {
        if (isByteSerialized(encoded)) {
            byte[] bytes = Base64.getDecoder().decode(encoded.substring(BYTE_SERIALIZATION_PREFIX.length()));
            return deserializeBytes(bytes);
        }

        byte[] bytes = Base64.getDecoder().decode(encoded);
        try (BukkitObjectInputStream input = new BukkitObjectInputStream(new ByteArrayInputStream(bytes))) {
            Object value = input.readObject();
            return value instanceof ItemStack item ? item : null;
        }
    }

    private static byte[] serializeAsBytes(ItemStack item) throws IOException {
        try {
            Method method = ItemStack.class.getMethod("serializeAsBytes");
            Object value = method.invoke(item);
            return value instanceof byte[] bytes ? bytes : null;
        } catch (NoSuchMethodException ignored) {
            return null;
        } catch (IllegalAccessException | InvocationTargetException exception) {
            throw new IOException("Failed to serialize item using byte serialization", exception);
        }
    }

    private static ItemStack deserializeBytes(byte[] bytes) throws IOException {
        try {
            Method method = ItemStack.class.getMethod("deserializeBytes", byte[].class);
            Object value = method.invoke(null, bytes);
            return value instanceof ItemStack item ? item : null;
        } catch (NoSuchMethodException exception) {
            throw new IOException("Server does not support item byte deserialization", exception);
        } catch (IllegalAccessException | InvocationTargetException exception) {
            throw new IOException("Failed to deserialize item using byte serialization", exception);
        }
    }
}
