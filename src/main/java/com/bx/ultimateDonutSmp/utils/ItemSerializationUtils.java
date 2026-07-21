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
        return Base64.getEncoder().encodeToString(serializeToLegacyBytes(item));
    }

    public static byte[] serializeToBytes(ItemStack item) throws IOException {
        byte[] serializedBytes = serializeAsBytes(item);
        if (serializedBytes != null) {
            return serializedBytes;
        }
        return serializeToLegacyBytes(item);
    }

    public static int serializedByteSize(ItemStack item) throws IOException {
        return serializeToBytes(item).length;
    }

    private static byte[] serializeToLegacyBytes(ItemStack item) throws IOException {
        ByteArrayOutputStream outputBytes = new ByteArrayOutputStream();
        try (BukkitObjectOutputStream output = new BukkitObjectOutputStream(outputBytes)) {
            output.writeObject(item);
        }
        return outputBytes.toByteArray();
    }

    public static ItemStack deserialize(String encoded) throws IOException, ClassNotFoundException {
        if (isByteSerialized(encoded)) {
            byte[] bytes = Base64.getDecoder().decode(encoded.substring(BYTE_SERIALIZATION_PREFIX.length()));
            if (isLegacyBytes(bytes)) {
                return deserializeLegacyBytes(bytes);
            }
            return deserializeBytes(bytes);
        }

        byte[] bytes = Base64.getDecoder().decode(encoded);
        return deserializeLegacyBytes(bytes);
    }

    public static boolean isLegacyBytes(byte[] bytes) {
        return bytes != null && bytes.length >= 2 && bytes[0] == (byte) 0xAC && bytes[1] == (byte) 0xED;
    }

    private static ItemStack deserializeLegacyBytes(byte[] bytes) throws IOException, ClassNotFoundException {
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
