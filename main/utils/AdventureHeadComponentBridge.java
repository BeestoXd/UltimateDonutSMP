package com.bx.ultimateDonutSmp.utils;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class AdventureHeadComponentBridge {

    private final Class<?> objectContentsType;
    private final Class<?> playerHeadContentsType;
    private final Method componentObjectMethod;
    private final Method playerHeadMethod;
    private final Method profilePropertyTwoMethod;
    private final Method profilePropertyThreeMethod;

    public AdventureHeadComponentBridge() {
        objectContentsType = findClass("net.kyori.adventure.text.object.ObjectContents");
        playerHeadContentsType = findClass("net.kyori.adventure.text.object.PlayerHeadObjectContents");
        componentObjectMethod = objectContentsType == null
                ? null
                : findStaticMethod(Component.class, "object", Component.class, objectContentsType);
        playerHeadMethod = objectContentsType == null
                ? null
                : findStaticMethod(objectContentsType, "playerHead", null);
        profilePropertyTwoMethod = playerHeadContentsType == null
                ? null
                : findStaticMethod(playerHeadContentsType, "property", null, String.class, String.class);
        profilePropertyThreeMethod = playerHeadContentsType == null
                ? null
                : findStaticMethod(playerHeadContentsType, "property", null, String.class, String.class, String.class);
    }

    public HeadBuilder newPlayerHeadBuilder() {
        if (!isAvailable()) {
            return null;
        }

        try {
            Object builder = playerHeadMethod.invoke(null);
            HeadBuilder headBuilder = new HeadBuilder(this, builder);
            headBuilder.hat(true);
            return headBuilder;
        } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
            return null;
        }
    }

    private boolean isAvailable() {
        return objectContentsType != null
                && playerHeadContentsType != null
                && componentObjectMethod != null
                && playerHeadMethod != null;
    }

    private Component buildComponent(Object builder) {
        Object contents = buildContents(builder);
        if (contents == null) {
            return Component.empty();
        }

        try {
            Object component = componentObjectMethod.invoke(null, contents);
            return component instanceof Component adventureComponent ? adventureComponent : Component.empty();
        } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
            return Component.empty();
        }
    }

    private Object buildContents(Object builder) {
        try {
            return invokeCompatible(builder, "build");
        } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
            return null;
        }
    }

    private Object createProfileProperty(String value, String signature) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            if (signature != null && !signature.isBlank() && profilePropertyThreeMethod != null) {
                return profilePropertyThreeMethod.invoke(null, "textures", value, signature);
            }
            if (profilePropertyTwoMethod != null) {
                return profilePropertyTwoMethod.invoke(null, "textures", value);
            }
            if (profilePropertyThreeMethod != null) {
                return profilePropertyThreeMethod.invoke(null, "textures", value, signature);
            }
        } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
        }
        return null;
    }

    private boolean applyPaperSkin(Player player, Object builder) {
        if (player == null || builder == null) {
            return false;
        }

        for (Method method : player.getClass().getMethods()) {
            if (isApplySkinMethod(method, builder) && invokeBooleanMethod(method, player, builder)) {
                return true;
            }
        }
        for (Method method : player.getClass().getDeclaredMethods()) {
            if (isApplySkinMethod(method, builder) && invokeBooleanMethod(method, player, builder)) {
                return true;
            }
        }
        return false;
    }

    private boolean isApplySkinMethod(Method method, Object builder) {
        return method.getName().equals("applySkinToPlayerHeadContents")
                && method.getParameterCount() == 1
                && method.getParameterTypes()[0].isAssignableFrom(builder.getClass());
    }

    private boolean invokeBooleanMethod(Method method, Object target, Object arg) {
        try {
            method.setAccessible(true);
            method.invoke(target, arg);
            return true;
        } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
            return false;
        }
    }

    private static Object invokeCompatible(Object target, String methodName, Object... args)
            throws ReflectiveOperationException {
        if (target == null) {
            return null;
        }

        for (Method method : target.getClass().getMethods()) {
            if (isCompatibleMethod(method, methodName, args)) {
                method.setAccessible(true);
                return method.invoke(target, args);
            }
        }
        for (Class<?> current = target.getClass(); current != null; current = current.getSuperclass()) {
            for (Method method : current.getDeclaredMethods()) {
                if (isCompatibleMethod(method, methodName, args)) {
                    method.setAccessible(true);
                    return method.invoke(target, args);
                }
            }
        }
        return null;
    }

    private static boolean isCompatibleMethod(Method method, String methodName, Object[] args) {
        return method.getName().equals(methodName)
                && method.getParameterCount() == args.length
                && canAccept(method.getParameterTypes(), args);
    }

    private static Method findStaticMethod(
            Class<?> owner,
            String methodName,
            Class<?> returnType,
            Class<?>... parameterTypes
    ) {
        for (Method method : owner.getMethods()) {
            if (isStaticMethod(method, methodName, returnType, parameterTypes)) {
                method.setAccessible(true);
                return method;
            }
        }
        for (Method method : owner.getDeclaredMethods()) {
            if (isStaticMethod(method, methodName, returnType, parameterTypes)) {
                method.setAccessible(true);
                return method;
            }
        }
        return null;
    }

    private static boolean isStaticMethod(
            Method method,
            String methodName,
            Class<?> returnType,
            Class<?>[] parameterTypes
    ) {
        if (!Modifier.isStatic(method.getModifiers())
                || !method.getName().equals(methodName)
                || method.getParameterCount() != parameterTypes.length) {
            return false;
        }
        if (returnType != null && !returnType.isAssignableFrom(method.getReturnType())) {
            return false;
        }

        Class<?>[] actualParameterTypes = method.getParameterTypes();
        for (int index = 0; index < actualParameterTypes.length; index++) {
            if (!actualParameterTypes[index].isAssignableFrom(parameterTypes[index])) {
                return false;
            }
        }
        return true;
    }

    private static boolean canAccept(Class<?>[] parameterTypes, Object[] args) {
        for (int index = 0; index < parameterTypes.length; index++) {
            Object arg = args[index];
            if (arg == null) {
                continue;
            }

            Class<?> parameterType = wrapPrimitive(parameterTypes[index]);
            if (!parameterType.isAssignableFrom(arg.getClass())) {
                return false;
            }
        }
        return true;
    }

    private static Class<?> wrapPrimitive(Class<?> type) {
        if (!type.isPrimitive()) {
            return type;
        }
        if (type == boolean.class) {
            return Boolean.class;
        }
        if (type == int.class) {
            return Integer.class;
        }
        if (type == long.class) {
            return Long.class;
        }
        if (type == double.class) {
            return Double.class;
        }
        if (type == float.class) {
            return Float.class;
        }
        if (type == byte.class) {
            return Byte.class;
        }
        if (type == short.class) {
            return Short.class;
        }
        if (type == char.class) {
            return Character.class;
        }
        return type;
    }

    private static Class<?> findClass(String className) {
        List<ClassLoader> loaders = new ArrayList<>();
        addLoader(loaders, Component.class.getClassLoader());
        addLoader(loaders, Thread.currentThread().getContextClassLoader());
        addLoader(loaders, AdventureHeadComponentBridge.class.getClassLoader());

        for (ClassLoader loader : loaders) {
            try {
                return Class.forName(className, false, loader);
            } catch (ClassNotFoundException ignored) {
            }
        }

        try {
            return Class.forName(className);
        } catch (ClassNotFoundException ignored) {
            return null;
        }
    }

    private static void addLoader(List<ClassLoader> loaders, ClassLoader loader) {
        if (loader != null && !loaders.contains(loader)) {
            loaders.add(loader);
        }
    }

    public static final class HeadBuilder {
        private final AdventureHeadComponentBridge bridge;
        private final Object builder;

        private HeadBuilder(AdventureHeadComponentBridge bridge, Object builder) {
            this.bridge = bridge;
            this.builder = builder;
        }

        public void hat(boolean value) {
            invokeQuietly("hat", value);
        }

        public void name(String value) {
            if (value != null && !value.isBlank()) {
                invokeQuietly("name", value);
            }
        }

        public void id(UUID value) {
            if (value != null) {
                invokeQuietly("id", value);
            }
        }

        public void profileProperty(String value, String signature) {
            Object property = bridge.createProfileProperty(value, signature);
            if (property != null) {
                invokeQuietly("profileProperty", property);
            }
        }

        public boolean applyPaperSkin(Player player) {
            return bridge.applyPaperSkin(player, builder);
        }

        public Component buildComponent() {
            return bridge.buildComponent(builder);
        }

        public Object buildContents() {
            return bridge.buildContents(builder);
        }

        private void invokeQuietly(String methodName, Object... args) {
            try {
                invokeCompatible(builder, methodName, args);
            } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
            }
        }
    }
}
