package com.bx.ultimateDonutSmp.managers;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import org.bukkit.entity.Player;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

final class NativeGameProfileFactory {

    private NativeGameProfileFactory() {
    }

    static WrappedGameProfile create(
            UUID profileId,
            String profileName,
            WrappedGameProfile sourceProfile,
            String textureValue,
            String textureSignature
    ) {
        try {
            Object sourceHandle = sourceProfile == null ? null : sourceProfile.getHandle();
            Object textureProperty = textureValue == null || textureValue.isBlank()
                    ? findTextureProperty(sourceHandle)
                    : createProperty(
                            sourceHandle == null ? null : sourceHandle.getClass().getClassLoader(),
                            textureValue,
                            textureSignature
                    );
            Object nativeProfile = instantiateProfile(
                    sourceHandle == null ? null : sourceHandle.getClass().getClassLoader(),
                    profileId,
                    profileName,
                    textureProperty
            );
            if (nativeProfile == null) {
                return fallbackProfile(profileId, profileName, textureValue, textureSignature);
            }

            if (textureProperty != null) {
                addTexture(nativeProfile, textureProperty);
            }
            WrappedGameProfile wrapped = WrappedGameProfile.fromHandle(nativeProfile);
            cacheWrappedTexture(wrapped, textureValue, textureSignature);
            return wrapped;
        } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
            return fallbackProfile(profileId, profileName, textureValue, textureSignature);
        }
    }

    static boolean applyTexture(WrappedGameProfile profile, String textureValue, String textureSignature) {
        if (profile == null || textureValue == null || textureValue.isBlank()) {
            return false;
        }
        try {
            Object handle = profile.getHandle();
            Object property = createProperty(
                    handle == null ? null : handle.getClass().getClassLoader(),
                    textureValue,
                    textureSignature
            );
            return property != null && addTexture(handle, property);
        } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
            return false;
        }
    }

    private static Object instantiateProfile(
            ClassLoader preferredLoader,
            UUID uuid,
            String name,
            Object textureProperty
    )
            throws ReflectiveOperationException {
        Class<?> profileClass = findClass(
                preferredLoader,
                "com.mojang.authlib.GameProfile",
                "net.minecraft.util.com.mojang.authlib.GameProfile"
        );
        if (profileClass == null) {
            return null;
        }

        if (textureProperty != null) {
            for (Constructor<?> constructor : profileClass.getDeclaredConstructors()) {
                Class<?>[] parameters = constructor.getParameterTypes();
                if (parameters.length != 3
                        || parameters[0] != UUID.class
                        || parameters[1] != String.class) {
                    continue;
                }
                Object propertyMap = createPropertyMap(parameters[2], textureProperty);
                if (propertyMap == null) {
                    continue;
                }
                constructor.setAccessible(true);
                return constructor.newInstance(uuid, name, propertyMap);
            }
        }

        for (Constructor<?> constructor : profileClass.getDeclaredConstructors()) {
            Class<?>[] parameters = constructor.getParameterTypes();
            constructor.setAccessible(true);
            if (parameters.length == 2 && parameters[0] == UUID.class && parameters[1] == String.class) {
                return constructor.newInstance(uuid, name);
            }
            if (parameters.length == 2 && parameters[0] == String.class && parameters[1] == String.class) {
                return constructor.newInstance(uuid.toString(), name);
            }
        }
        return null;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Object createPropertyMap(Class<?> propertyMapType, Object textureProperty) {
        if (propertyMapType == null || textureProperty == null) {
            return null;
        }

        Multimap properties = HashMultimap.create();
        properties.put("textures", textureProperty);
        for (Constructor<?> constructor : propertyMapType.getDeclaredConstructors()) {
            Class<?>[] parameters = constructor.getParameterTypes();
            try {
                constructor.setAccessible(true);
                if (parameters.length == 1
                        && parameters[0].isAssignableFrom(properties.getClass())) {
                    return constructor.newInstance(properties);
                }
                if (parameters.length == 1
                        && Multimap.class.isAssignableFrom(parameters[0])) {
                    return constructor.newInstance(properties);
                }
                if (parameters.length == 0) {
                    Object propertyMap = constructor.newInstance();
                    if (mutateContainer(propertyMap, textureProperty)) {
                        return propertyMap;
                    }
                }
            } catch (ReflectiveOperationException | RuntimeException ignored) {
            }
        }
        return null;
    }

    private static WrappedGameProfile fallbackProfile(
            UUID profileId,
            String profileName,
            String textureValue,
            String textureSignature
    ) {
        WrappedGameProfile profile = new WrappedGameProfile(profileId, profileName);
        cacheWrappedTexture(profile, textureValue, textureSignature);
        return profile;
    }

    private static void cacheWrappedTexture(
            WrappedGameProfile profile,
            String textureValue,
            String textureSignature
    ) {
        if (profile == null || textureValue == null || textureValue.isBlank()) {
            return;
        }
        try {
            Multimap<String, WrappedSignedProperty> propertyMap = HashMultimap.create();
            propertyMap.put(
                    "textures",
                    new WrappedSignedProperty("textures", textureValue, blankToNull(textureSignature))
            );
            Field field = WrappedGameProfile.class.getDeclaredField("propertyMap");
            field.setAccessible(true);
            field.set(profile, propertyMap);
        } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
        }
    }

    private static Object createProperty(ClassLoader preferredLoader, String value, String signature)
            throws ReflectiveOperationException {
        Class<?> propertyClass = findClass(
                preferredLoader,
                "com.mojang.authlib.properties.Property",
                "net.minecraft.util.com.mojang.authlib.properties.Property"
        );
        if (propertyClass == null) {
            return null;
        }

        for (Constructor<?> constructor : propertyClass.getDeclaredConstructors()) {
            Class<?>[] parameters = constructor.getParameterTypes();
            constructor.setAccessible(true);
            if (parameters.length == 3
                    && parameters[0] == String.class
                    && parameters[1] == String.class
                    && parameters[2] == String.class) {
                return constructor.newInstance("textures", value, blankToNull(signature));
            }
        }
        for (Constructor<?> constructor : propertyClass.getDeclaredConstructors()) {
            Class<?>[] parameters = constructor.getParameterTypes();
            constructor.setAccessible(true);
            if (parameters.length == 2
                    && parameters[0] == String.class
                    && parameters[1] == String.class) {
                return constructor.newInstance("textures", value);
            }
        }
        return null;
    }

    private static Object findTextureProperty(Object nativeProfile) {
        if (nativeProfile == null) {
            return null;
        }

        for (String methodName : List.of("getProperties", "properties")) {
            for (Method method : methods(nativeProfile.getClass(), methodName, 0)) {
                try {
                    method.setAccessible(true);
                    Object texture = findTextureInContainer(method.invoke(nativeProfile));
                    if (texture != null) {
                        return texture;
                    }
                } catch (ReflectiveOperationException | RuntimeException ignored) {
                }
            }
        }

        for (Class<?> current = nativeProfile.getClass(); current != null; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                if (!isPropertyContainer(field)) {
                    continue;
                }
                try {
                    field.setAccessible(true);
                    Object texture = findTextureInContainer(field.get(nativeProfile));
                    if (texture != null) {
                        return texture;
                    }
                } catch (IllegalAccessException | RuntimeException ignored) {
                }
            }
        }
        return null;
    }

    private static Object findTextureInContainer(Object source) {
        if (source == null) {
            return null;
        }
        if (source instanceof Optional<?> optional) {
            return optional.map(NativeGameProfileFactory::findTextureInContainer).orElse(null);
        }
        if (source instanceof Map<?, ?> map) {
            Object texture = findTextureInContainer(map.get("textures"));
            return texture != null ? texture : findTextureInContainer(map.values());
        }
        if (source instanceof Iterable<?> iterable) {
            for (Object item : iterable) {
                Object texture = findTextureInContainer(item);
                if (texture != null) {
                    return texture;
                }
            }
            return null;
        }
        if (source.getClass().isArray()) {
            for (int index = 0; index < Array.getLength(source); index++) {
                Object texture = findTextureInContainer(Array.get(source, index));
                if (texture != null) {
                    return texture;
                }
            }
            return null;
        }

        String propertyName = readString(source, "getName", "name");
        String propertyValue = readString(source, "getValue", "value");
        if (propertyValue != null
                && !propertyValue.isBlank()
                && (propertyName == null || propertyName.equalsIgnoreCase("textures"))) {
            return source;
        }

        Object keyedTextures = invokeForValue(source, "get", "textures");
        if (keyedTextures != null && keyedTextures != source) {
            return findTextureInContainer(keyedTextures);
        }
        return null;
    }

    private static boolean addTexture(Object nativeProfile, Object property) {
        if (nativeProfile == null || property == null) {
            return false;
        }

        for (String methodName : List.of("getProperties", "properties")) {
            for (Method method : methods(nativeProfile.getClass(), methodName, 0)) {
                try {
                    method.setAccessible(true);
                    if (mutateContainer(method.invoke(nativeProfile), property)) {
                        return true;
                    }
                } catch (ReflectiveOperationException | RuntimeException ignored) {
                }
            }
        }

        for (Class<?> current = nativeProfile.getClass(); current != null; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                if (!isPropertyContainer(field)) {
                    continue;
                }
                try {
                    field.setAccessible(true);
                    if (mutateContainer(field.get(nativeProfile), property)) {
                        return true;
                    }
                } catch (IllegalAccessException | RuntimeException ignored) {
                }
            }
        }
        return false;
    }

    private static boolean mutateContainer(Object container, Object property) {
        if (container == null) {
            return false;
        }
        invoke(container, "removeAll", "textures");
        invoke(container, "remove", "textures");
        return invoke(container, "put", "textures", property)
                || invoke(container, "add", property);
    }

    private static boolean invoke(Object target, String name, Object... args) {
        if (target == null) {
            return false;
        }
        for (Method method : methods(target.getClass(), name, args.length)) {
            if (!accepts(method.getParameterTypes(), args)) {
                continue;
            }
            try {
                method.setAccessible(true);
                method.invoke(target, args);
                return true;
            } catch (ReflectiveOperationException | RuntimeException ignored) {
            }
        }
        return false;
    }

    private static Object invokeForValue(Object target, String name, Object... args) {
        if (target == null) {
            return null;
        }
        for (Method method : methods(target.getClass(), name, args.length)) {
            if (!accepts(method.getParameterTypes(), args)) {
                continue;
            }
            try {
                method.setAccessible(true);
                return method.invoke(target, args);
            } catch (ReflectiveOperationException | RuntimeException ignored) {
            }
        }
        return null;
    }

    private static String readString(Object source, String... names) {
        for (String name : names) {
            Object value = invokeForValue(source, name);
            if (value instanceof String string) {
                return string;
            }
        }
        return null;
    }

    private static List<Method> methods(Class<?> type, String name, int parameterCount) {
        List<Method> result = new ArrayList<>();
        for (Method method : type.getMethods()) {
            if (method.getName().equals(name) && method.getParameterCount() == parameterCount) {
                result.add(method);
            }
        }
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            for (Method method : current.getDeclaredMethods()) {
                if (method.getName().equals(name)
                        && method.getParameterCount() == parameterCount
                        && !result.contains(method)) {
                    result.add(method);
                }
            }
        }
        return result;
    }

    private static boolean accepts(Class<?>[] parameterTypes, Object[] args) {
        for (int index = 0; index < parameterTypes.length; index++) {
            Object arg = args[index];
            if (arg != null && !box(parameterTypes[index]).isInstance(arg)) {
                return false;
            }
        }
        return true;
    }

    private static Class<?> box(Class<?> type) {
        if (!type.isPrimitive()) {
            return type;
        }
        if (type == boolean.class) return Boolean.class;
        if (type == int.class) return Integer.class;
        if (type == long.class) return Long.class;
        if (type == double.class) return Double.class;
        if (type == float.class) return Float.class;
        if (type == byte.class) return Byte.class;
        if (type == short.class) return Short.class;
        if (type == char.class) return Character.class;
        return type;
    }

    private static boolean isPropertyContainer(Field field) {
        String fieldName = field.getName().toLowerCase(java.util.Locale.ROOT);
        String typeName = field.getType().getName().toLowerCase(java.util.Locale.ROOT);
        return fieldName.contains("properties")
                || fieldName.contains("property")
                || typeName.contains("propertymap")
                || typeName.contains("multimap");
    }

    private static Class<?> findClass(ClassLoader preferredLoader, String... names) {
        List<ClassLoader> loaders = new ArrayList<>();
        if (preferredLoader != null) {
            loaders.add(preferredLoader);
        }
        if (Thread.currentThread().getContextClassLoader() != null) {
            loaders.add(Thread.currentThread().getContextClassLoader());
        }
        loaders.add(NativeGameProfileFactory.class.getClassLoader());
        loaders.add(Player.class.getClassLoader());

        for (ClassLoader loader : loaders) {
            for (String name : names) {
                try {
                    return Class.forName(name, false, loader);
                } catch (ClassNotFoundException ignored) {
                }
            }
        }
        return null;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
