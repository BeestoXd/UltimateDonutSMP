package com.bx.ultimateDonutSmp.utils;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.logging.Level;

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
        if (encoded == null || encoded.isEmpty()) {
            return null;
        }
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
        ClassLoader targetLoader = ItemSerializationUtils.class.getClassLoader();
        try (BukkitObjectInputStream input = new ClassLoaderObjectInputStream(new ByteArrayInputStream(bytes), targetLoader)) {
            Object value = input.readObject();
            return value instanceof ItemStack item ? item : null;
        }
    }

    private static int getDataVersion() {
        try {
            Object unsafe = Bukkit.getUnsafe();
            if (unsafe != null) {
                Method m = unsafe.getClass().getMethod("getDataVersion");
                Object val = m.invoke(unsafe);
                if (val instanceof Integer ver) {
                    return ver;
                }
            }
        } catch (Exception ignored) {
        }
        return 0;
    }

    private static byte[] serializeAsBytes(ItemStack item) throws IOException {
        try {
            Method method = ItemStack.class.getMethod("serializeAsBytes");
            Object value = method.invoke(item);
            if (value instanceof byte[] bytes) {
                return bytes;
            }
        } catch (NoSuchMethodException ignored) {
        } catch (IllegalAccessException | InvocationTargetException exception) {
            throw new IOException("Failed to serialize item using byte serialization", exception);
        }

        try {
            Object unsafe = Bukkit.getUnsafe();
            if (unsafe != null) {
                int dataVersion = getDataVersion();
                for (Method m : unsafe.getClass().getMethods()) {
                    if (m.getName().equals("serializeItem")) {
                        try {
                            m.setAccessible(true);
                            Object value = null;
                            if (m.getParameterCount() == 1 && m.getParameterTypes()[0] == byte[].class) {
                                value = m.invoke(unsafe, item);
                            } else if (m.getParameterCount() == 1 && m.getParameterTypes()[0] == ItemStack.class) {
                                value = m.invoke(unsafe, item);
                            } else if (m.getParameterCount() == 2 && m.getParameterTypes()[0] == ItemStack.class) {
                                value = m.invoke(unsafe, item, dataVersion);
                            }
                            if (value instanceof byte[] bytes) {
                                return bytes;
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        } catch (Throwable ignored) {
        }

        return null;
    }

    private static ItemStack deserializeBytes(byte[] bytes) throws IOException {
        // 1. Try Paper ItemStack.deserializeBytes
        try {
            Method method = ItemStack.class.getMethod("deserializeBytes", byte[].class);
            Object value = method.invoke(null, bytes);
            if (value instanceof ItemStack item) {
                return item;
            }
        } catch (NoSuchMethodException ignored) {
        } catch (IllegalAccessException | InvocationTargetException exception) {
            throw new IOException("Failed to deserialize item using byte serialization", exception);
        }

        // 2. Scan all UnsafeValues and ItemFactory methods taking byte[] / (byte[], int)
        ItemStack unsafeItem = tryUnsafeOrFactoryDeserialize(bytes);
        if (unsafeItem != null) {
            return unsafeItem;
        }

        // 3. Try CraftBukkit / NMS reflection fallback for Spigot (supports MC 1.16 - 26.x)
        ItemStack cbNbtItem = deserializeCraftBukkitNbt(bytes);
        if (cbNbtItem != null) {
            return cbNbtItem;
        }

        // 4. Try Bukkit Object Stream if bytes happen to be legacy format
        try {
            return deserializeLegacyBytes(bytes);
        } catch (Exception ignored) {
        }

        // 5. Try Yaml deserialization fallback
        ItemStack yamlItem = deserializeYamlItem(bytes);
        if (yamlItem != null) {
            return yamlItem;
        }

        return null;
    }

    private static ItemStack tryUnsafeOrFactoryDeserialize(byte[] bytes) {
        Object unsafe = null;
        try {
            unsafe = Bukkit.getUnsafe();
        } catch (Throwable ignored) {
        }

        Object factory = null;
        try {
            factory = Bukkit.getItemFactory();
        } catch (Throwable ignored) {
        }

        Object[] targets = new Object[] { unsafe, factory };
        int dataVersion = getDataVersion();

        for (Object target : targets) {
            if (target == null) continue;
            for (Method m : target.getClass().getMethods()) {
                if (m.getReturnType().equals(void.class) || m.getReturnType().isPrimitive()) {
                    continue;
                }
                Class<?>[] params = m.getParameterTypes();
                if (params.length == 1 && params[0].isAssignableFrom(byte[].class)) {
                    try {
                        m.setAccessible(true);
                        Object val = m.invoke(target, (Object) bytes);
                        if (val instanceof ItemStack item) {
                            return item;
                        }
                    } catch (Throwable ignored) {}
                }
                if (params.length == 2 && params[0].isAssignableFrom(byte[].class)) {
                    try {
                        m.setAccessible(true);
                        Object val = m.invoke(target, bytes, dataVersion);
                        if (val instanceof ItemStack item) {
                            return item;
                        }
                        val = m.invoke(target, bytes, 0);
                        if (val instanceof ItemStack item) {
                            return item;
                        }
                    } catch (Throwable ignored) {}
                }
            }
        }
        return null;
    }

    private static ItemStack deserializeYamlItem(byte[] bytes) {
        try {
            String str = new String(bytes, StandardCharsets.UTF_8);
            if (str.contains("==") || str.contains("type:")) {
                YamlConfiguration config = new YamlConfiguration();
                config.loadFromString("item: " + str);
                if (config.isItemStack("item")) {
                    return config.getItemStack("item");
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static ItemStack deserializeCraftBukkitNbt(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            Object compoundTag = readNbtCompound(bytes);
            if (compoundTag != null) {
                return createItemStackFromNbt(compoundTag);
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    private static class ClassLoaderObjectInputStream extends BukkitObjectInputStream {
        private final ClassLoader classLoader;

        public ClassLoaderObjectInputStream(InputStream in, ClassLoader classLoader) throws IOException {
            super(in);
            this.classLoader = classLoader;
        }

        @Override
        protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
            try {
                if (classLoader != null) {
                    return Class.forName(desc.getName(), false, classLoader);
                }
            } catch (ClassNotFoundException ignored) {
            }
            return super.resolveClass(desc);
        }
    }

    private static Object getNbtAccounter() {
        String[] candidateNames = new String[] {
            "net.minecraft.nbt.NbtAccounter",
            "net.minecraft.nbt.NBTReadLimiter"
        };
        for (String name : candidateNames) {
            try {
                Class<?> clazz = Class.forName(name);
                for (Method m : clazz.getDeclaredMethods()) {
                    if (Modifier.isStatic(m.getModifiers()) && m.getParameterCount() == 0 && clazz.isAssignableFrom(m.getReturnType())) {
                        try {
                            m.setAccessible(true);
                            return m.invoke(null);
                        } catch (Exception ignored) {}
                    }
                }
            } catch (ClassNotFoundException ignored) {}
        }
        return null;
    }

    private static Object readNbtCompound(byte[] bytes) {
        Class<?> nbtIoClass = getNbtIoClass();
        if (nbtIoClass == null) {
            return null;
        }

        Object accounter = getNbtAccounter();
        boolean isGzip = bytes.length >= 2 && (bytes[0] & 0xFF) == 0x1F && (bytes[1] & 0xFF) == 0x8B;

        Object result = tryReadNbtFromBytes(nbtIoClass, accounter, bytes, isGzip);
        if (result != null) return result;

        if (isGzip) {
            result = tryReadNbtFromBytes(nbtIoClass, accounter, bytes, false);
            if (result != null) return result;
        }

        return null;
    }

    private static Object tryReadNbtFromBytes(Class<?> nbtIoClass, Object accounter, byte[] bytes, boolean useGzip) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
             java.io.InputStream input = useGzip ? new java.util.zip.GZIPInputStream(bais) : bais) {

            for (Method m : nbtIoClass.getDeclaredMethods()) {
                if (!Modifier.isStatic(m.getModifiers())) continue;

                if (m.getParameterCount() == 1 && java.io.InputStream.class.isAssignableFrom(m.getParameterTypes()[0])) {
                    try {
                        m.setAccessible(true);
                        Object res = m.invoke(null, input);
                        if (res != null) return res;
                    } catch (Exception ignored) {}
                }
                if (m.getParameterCount() == 2 && accounter != null && java.io.InputStream.class.isAssignableFrom(m.getParameterTypes()[0])) {
                    try {
                        m.setAccessible(true);
                        Object res = m.invoke(null, input, accounter);
                        if (res != null) return res;
                    } catch (Exception ignored) {}
                }
            }
        } catch (Exception ignored) {
        }

        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
             java.io.InputStream input = useGzip ? new java.util.zip.GZIPInputStream(bais) : bais;
             java.io.DataInputStream dis = new java.io.DataInputStream(input)) {

            for (Method m : nbtIoClass.getDeclaredMethods()) {
                if (!Modifier.isStatic(m.getModifiers())) continue;

                if (m.getParameterCount() == 1 && (java.io.DataInput.class.isAssignableFrom(m.getParameterTypes()[0]) || java.io.DataInputStream.class.isAssignableFrom(m.getParameterTypes()[0]))) {
                    try {
                        m.setAccessible(true);
                        Object res = m.invoke(null, dis);
                        if (res != null) return res;
                    } catch (Exception ignored) {}
                }
                if (m.getParameterCount() == 2 && accounter != null && (java.io.DataInput.class.isAssignableFrom(m.getParameterTypes()[0]) || java.io.DataInputStream.class.isAssignableFrom(m.getParameterTypes()[0]))) {
                    try {
                        m.setAccessible(true);
                        Object res = m.invoke(null, dis, accounter);
                        if (res != null) return res;
                    } catch (Exception ignored) {}
                }
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    private static Object getRegistryAccess() {
        try {
            Object server = Bukkit.getServer().getClass().getMethod("getServer").invoke(Bukkit.getServer());
            if (server != null) {
                for (Method m : server.getClass().getMethods()) {
                    if (m.getParameterCount() == 0 && (m.getName().equals("registryAccess") || m.getReturnType().getSimpleName().contains("Registry"))) {
                        try {
                            Object reg = m.invoke(server);
                            if (reg != null) return reg;
                        } catch (Exception ignored) {}
                    }
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static ItemStack createItemStackFromNbt(Object compoundTag) {
        if (compoundTag == null) {
            return null;
        }

        Class<?> craftItemStackClass = getCraftItemStackClass();

        if (craftItemStackClass != null) {
            for (Method m : craftItemStackClass.getDeclaredMethods()) {
                if (Modifier.isStatic(m.getModifiers()) && m.getParameterCount() == 1) {
                    if (m.getParameterTypes()[0].isAssignableFrom(compoundTag.getClass())) {
                        try {
                            m.setAccessible(true);
                            Object res = m.invoke(null, compoundTag);
                            if (res instanceof ItemStack item) {
                                return item;
                            }
                        } catch (Exception ignored) {}
                    }
                }
            }
        }

        Object nmsItemStack = createNmsItemStack(compoundTag);
        if (nmsItemStack != null && craftItemStackClass != null) {
            for (Method m : craftItemStackClass.getDeclaredMethods()) {
                if (Modifier.isStatic(m.getModifiers()) && m.getParameterCount() == 1
                        && m.getReturnType().equals(ItemStack.class)
                        && m.getParameterTypes()[0].isAssignableFrom(nmsItemStack.getClass())) {
                    try {
                        m.setAccessible(true);
                        Object res = m.invoke(null, nmsItemStack);
                        if (res instanceof ItemStack item) {
                            return item;
                        }
                    } catch (Exception ignored) {}
                }
            }
        }

        return null;
    }

    private static Object createNmsItemStack(Object compoundTag) {
        Class<?> nmsItemStackClass = getNmsItemStackClass();
        if (nmsItemStackClass == null) {
            return null;
        }

        for (Method m : nmsItemStackClass.getDeclaredMethods()) {
            if (Modifier.isStatic(m.getModifiers()) && m.getParameterCount() == 1) {
                if (m.getParameterTypes()[0].isAssignableFrom(compoundTag.getClass())) {
                    try {
                        m.setAccessible(true);
                        Object res = m.invoke(null, compoundTag);
                        if (res != null && nmsItemStackClass.isAssignableFrom(res.getClass())) {
                            return res;
                        }
                    } catch (Exception ignored) {}
                }
            }
        }

        Object registryAccess = getRegistryAccess();
        if (registryAccess != null) {
            for (Method m : nmsItemStackClass.getDeclaredMethods()) {
                if (Modifier.isStatic(m.getModifiers()) && m.getParameterCount() == 2) {
                    Class<?>[] params = m.getParameterTypes();
                    if (params[0].isAssignableFrom(registryAccess.getClass()) && params[1].isAssignableFrom(compoundTag.getClass())) {
                        try {
                            m.setAccessible(true);
                            Object res = m.invoke(null, registryAccess, compoundTag);
                            if (res instanceof java.util.Optional<?> opt) {
                                return opt.orElse(null);
                            }
                            if (res != null && nmsItemStackClass.isAssignableFrom(res.getClass())) {
                                return res;
                            }
                        } catch (Exception ignored) {}
                    }
                }
            }
        }

        try {
            Constructor<?> cons = nmsItemStackClass.getDeclaredConstructor(compoundTag.getClass());
            cons.setAccessible(true);
            return cons.newInstance(compoundTag);
        } catch (Exception ignored) {}

        return null;
    }

    private static Class<?> getNmsItemStackClass() {
        String[] candidates = new String[] {
            "net.minecraft.world.item.ItemStack",
            "net.minecraft.server.v1_16_R3.ItemStack"
        };
        for (String c : candidates) {
            try {
                return Class.forName(c);
            } catch (ClassNotFoundException ignored) {}
        }
        try {
            String serverPackage = Bukkit.getServer().getClass().getPackage().getName();
            if (serverPackage.startsWith("org.bukkit.craftbukkit.")) {
                String version = serverPackage.substring("org.bukkit.craftbukkit.".length());
                int dot = version.indexOf(".");
                if (dot > 0) {
                    version = version.substring(0, dot);
                }
                if (!version.isEmpty()) {
                    return Class.forName("net.minecraft.server." + version + ".ItemStack");
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static Class<?> getCraftItemStackClass() {
        try {
            return Class.forName("org.bukkit.craftbukkit.inventory.CraftItemStack");
        } catch (ClassNotFoundException ignored) {}
        try {
            String serverPackage = Bukkit.getServer().getClass().getPackage().getName();
            return Class.forName(serverPackage + ".inventory.CraftItemStack");
        } catch (ClassNotFoundException ignored) {}
        return null;
    }

    private static Class<?> getNbtIoClass() {
        String[] candidateNames = new String[] {
            "net.minecraft.nbt.NbtIo",
            "net.minecraft.nbt.NBTCompressedStreamTools"
        };
        for (String name : candidateNames) {
            try {
                return Class.forName(name);
            } catch (ClassNotFoundException ignored) {}
        }
        try {
            String serverPackage = Bukkit.getServer().getClass().getPackage().getName();
            if (serverPackage.startsWith("org.bukkit.craftbukkit.")) {
                String version = serverPackage.substring("org.bukkit.craftbukkit.".length());
                int dot = version.indexOf(".");
                if (dot > 0) {
                    version = version.substring(0, dot);
                }
                if (!version.isEmpty()) {
                    try {
                        return Class.forName("net.minecraft.server." + version + ".NBTCompressedStreamTools");
                    } catch (ClassNotFoundException ignored) {}
                    try {
                        return Class.forName("net.minecraft.server." + version + ".NbtIo");
                    } catch (ClassNotFoundException ignored) {}
                }
            }
        } catch (Exception ignored) {}
        return null;
    }
}
