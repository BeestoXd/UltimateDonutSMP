package com.bx.ultimateDonutSmp.utils;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public final class TablistComponentUpdater {

    private static final String[] PLAYER_INFO_UPDATE_PACKET_CLASS_NAMES = {
            "net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket",
            "net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo"
    };
    private static final String[] PLAYER_INFO_REMOVE_PACKET_CLASS_NAMES = {
            "net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket",
            "net.minecraft.network.protocol.game.PacketPlayOutPlayerInfoRemove"
    };

    private final UltimateDonutSmp plugin;
    private boolean warned;
    private boolean disabled;
    private boolean avatarWarned;
    private boolean avatarDisabled;
    private boolean objectComponentWarned;

    public TablistComponentUpdater(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public boolean updateName(Player target, net.kyori.adventure.text.Component adventureComponent) {
        if (disabled || target == null || !target.isOnline() || adventureComponent == null) {
            return false;
        }

        if (updateNameWithPaperApi(target, adventureComponent)) {
            return true;
        }

        try {
            Object handle = invokeNoArg(target, "getHandle");
            Object component = toNativeComponent(adventureComponent);
            if (component == null) {
                return false;
            }
            setTabListDisplayName(handle, component);
            Object packet = createDisplayNamePacket(handle, component);
            for (Player viewer : Bukkit.getOnlinePlayers()) {
                if (viewer != null && viewer.isOnline()) {
                    sendPacket(viewer, packet);
                }
            }
            return true;
        } catch (ReflectiveOperationException | RuntimeException exception) {
            disableWithWarning(exception);
            return false;
        }
    }

    private boolean updateNameWithPaperApi(Player target, net.kyori.adventure.text.Component adventureComponent) {
        try {
            Method method = target.getClass().getMethod("playerListName", net.kyori.adventure.text.Component.class);
            method.setAccessible(true);
            method.invoke(target, adventureComponent);
            return true;
        } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
            return false;
        }
    }

    private Object toNativeComponent(net.kyori.adventure.text.Component adventureComponent) {
        try {
            Class<?> paperAdventureClass = Class.forName("io.papermc.paper.adventure.PaperAdventure");
            Method asVanillaMethod = paperAdventureClass.getMethod("asVanilla", net.kyori.adventure.text.Component.class);
            asVanillaMethod.setAccessible(true);
            return asVanillaMethod.invoke(null, adventureComponent);
        } catch (ReflectiveOperationException | LinkageError ignored) {
        }

        boolean hasObjectComponent = containsObjectComponent(adventureComponent);
        if (hasObjectComponent) {
            Object direct = toNativeComponentDirect(adventureComponent);
            if (direct != null) {
                return direct;
            }
        }

        try {
            String json = net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson().serialize(adventureComponent);
            Object component = parseNativeComponent(json);
            if (component != null) {
                return component;
            }
        } catch (RuntimeException ignored) {
        }

        Object direct = hasObjectComponent ? null : toNativeComponentDirect(adventureComponent);
        if (direct != null) {
            return direct;
        }

        if (hasObjectComponent) {
            warnObjectComponentUnsupported();
        }
        return null;
    }

    private boolean containsObjectComponent(net.kyori.adventure.text.Component component) {
        if (isAdventureObjectComponent(component)) {
            return true;
        }

        for (net.kyori.adventure.text.Component child : component.children()) {
            if (containsObjectComponent(child)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAdventureObjectComponent(Object component) {
        return isNamedClassInstance("net.kyori.adventure.text.ObjectComponent", component);
    }

    private boolean isPlayerHeadObjectContents(Object contents) {
        return isNamedClassInstance("net.kyori.adventure.text.object.PlayerHeadObjectContents", contents);
    }

    private boolean isNamedClassInstance(String className, Object value) {
        if (value == null) {
            return false;
        }

        try {
            Class<?> type = Class.forName(className, false, value.getClass().getClassLoader());
            return type.isInstance(value);
        } catch (ClassNotFoundException ignored) {
        }

        try {
            Class<?> type = Class.forName(className);
            return type.isInstance(value);
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    private Object toNativeComponentDirect(net.kyori.adventure.text.Component adventureComponent) {
        try {
            ClassLoader loader = plugin.getServer().getClass().getClassLoader();
            Class<?> nativeComponentType = getFirstAvailableClassOrNull(
                    loader,
                    "net.minecraft.network.chat.Component",
                    "net.minecraft.network.chat.IChatBaseComponent"
            );
            if (nativeComponentType == null) {
                return null;
            }

            return createNativeComponent(adventureComponent, loader, nativeComponentType);
        } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
            return null;
        }
    }

    private void warnObjectComponentUnsupported() {
        if (objectComponentWarned) {
            return;
        }
        objectComponentWarned = true;
        plugin.getLogger().warning("[Tablist] This Spigot build did not expose native object text components "
                + "for inline player heads. <head:...> requires a Minecraft/Spigot build with "
                + "net.minecraft.network.chat.contents.objects.PlayerSprite support. Server: "
                + Bukkit.getBukkitVersion());
    }

    private Object createNativeComponent(
            net.kyori.adventure.text.Component adventureComponent,
            ClassLoader loader,
            Class<?> nativeComponentType
    ) throws ReflectiveOperationException {
        Object nativeComponent = createNativeBaseComponent(adventureComponent, loader, nativeComponentType);
        if (nativeComponent == null) {
            nativeComponent = createNativeLiteralComponent("", nativeComponentType);
        }

        applyNativeColor(nativeComponent, adventureComponent.color());
        for (net.kyori.adventure.text.Component child : adventureComponent.children()) {
            Object nativeChild = createNativeComponent(child, loader, nativeComponentType);
            appendNativeComponent(nativeComponent, nativeChild, nativeComponentType);
        }
        return nativeComponent;
    }

    private Object createNativeBaseComponent(
            net.kyori.adventure.text.Component adventureComponent,
            ClassLoader loader,
            Class<?> nativeComponentType
    ) throws ReflectiveOperationException {
        if (adventureComponent instanceof TextComponent textComponent) {
            return createNativeLiteralComponent(textComponent.content(), nativeComponentType);
        }

        if (isAdventureObjectComponent(adventureComponent)) {
            Object object = createNativeObjectInfo(invokeNoArg(adventureComponent, "contents"), loader);
            if (object != null) {
                return createNativeObjectComponent(object, loader, nativeComponentType);
            }
        }

        return createNativeLiteralComponent("", nativeComponentType);
    }

    private Object createNativeLiteralComponent(String text, Class<?> nativeComponentType)
            throws ReflectiveOperationException {
        List<String> preferredNames = List.of("literal", "b", "m_237113_", "method_43470");
        for (String preferredName : preferredNames) {
            Object component = invokeStaticStringComponentFactory(nativeComponentType, preferredName, text);
            if (component != null) {
                return component;
            }
        }

        for (Method method : nativeComponentType.getMethods()) {
            Object component = invokeStringComponentFactory(nativeComponentType, method, text);
            if (component != null) {
                return component;
            }
        }
        for (Method method : nativeComponentType.getDeclaredMethods()) {
            Object component = invokeStringComponentFactory(nativeComponentType, method, text);
            if (component != null) {
                return component;
            }
        }

        throw new NoSuchMethodException("literal component factory");
    }

    private Object invokeStaticStringComponentFactory(Class<?> nativeComponentType, String methodName, String text) {
        for (Method method : nativeComponentType.getMethods()) {
            if (method.getName().equals(methodName)) {
                Object component = invokeStringComponentFactory(nativeComponentType, method, text);
                if (component != null) {
                    return component;
                }
            }
        }
        for (Method method : nativeComponentType.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                Object component = invokeStringComponentFactory(nativeComponentType, method, text);
                if (component != null) {
                    return component;
                }
            }
        }
        return null;
    }

    private Object invokeStringComponentFactory(Class<?> nativeComponentType, Method method, String text) {
        if (!Modifier.isStatic(method.getModifiers())
                || method.getParameterCount() != 1
                || method.getParameterTypes()[0] != String.class
                || !nativeComponentType.isAssignableFrom(method.getReturnType())) {
            return null;
        }

        try {
            method.setAccessible(true);
            return method.invoke(null, text == null ? "" : text);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return null;
        }
    }

    private Object createNativeObjectInfo(Object contents, ClassLoader loader) throws ReflectiveOperationException {
        if (isPlayerHeadObjectContents(contents)) {
            return createNativePlayerHeadObjectInfo(contents, loader);
        }
        return null;
    }

    private Object createNativePlayerHeadObjectInfo(Object playerHead, ClassLoader loader)
            throws ReflectiveOperationException {
        Class<?> playerSpriteType = Class.forName(
                "net.minecraft.network.chat.contents.objects.PlayerSprite",
                false,
                loader
        );
        Class<?> resolvableProfileType = Class.forName(
                "net.minecraft.world.item.component.ResolvableProfile",
                false,
                loader
        );
        Object gameProfile = createGameProfile(playerHead);
        Object resolvableProfile = createResolvableProfile(resolvableProfileType, gameProfile);
        boolean hat = getBooleanValue(playerHead, "hat");

        for (Constructor<?> constructor : playerSpriteType.getDeclaredConstructors()) {
            Class<?>[] parameters = constructor.getParameterTypes();
            if (parameters.length == 2
                    && parameters[0].isAssignableFrom(resolvableProfile.getClass())
                    && (parameters[1] == boolean.class || parameters[1] == Boolean.class)) {
                constructor.setAccessible(true);
                return constructor.newInstance(resolvableProfile, hat);
            }
        }

        throw new NoSuchMethodException("PlayerSprite(ResolvableProfile, boolean)");
    }

    private Object createGameProfile(Object playerHead) throws ReflectiveOperationException {
        ClassLoader loader = plugin.getServer().getClass().getClassLoader();
        Class<?> gameProfileType = getFirstAvailableClassOrNull(
                loader,
                "com.mojang.authlib.GameProfile"
        );
        if (gameProfileType == null) {
            gameProfileType = Class.forName("com.mojang.authlib.GameProfile");
        }

        UUID id = getUuidValue(playerHead, "id");
        String name = getStringValue(playerHead, "name");
        if ((name == null || name.isBlank()) && id != null) {
            name = id.toString().replace("-", "").substring(0, 16);
        }
        if (name == null || name.isBlank()) {
            name = "Player";
        }

        Constructor<?> constructor = gameProfileType.getConstructor(UUID.class, String.class);
        Object profile = constructor.newInstance(id, name);
        Object propertyMap = gameProfileType.getMethod("getProperties").invoke(profile);
        Object profileProperties = unwrapOptional(invokeNoArg(playerHead, "profileProperties"));
        if (!(profileProperties instanceof Iterable<?> iterableProperties)) {
            return profile;
        }

        for (Object profileProperty : iterableProperties) {
            String propertyName = getStringValue(profileProperty, "name");
            String propertyValue = getStringValue(profileProperty, "value");
            if (profileProperty == null || propertyValue == null || propertyValue.isBlank()) {
                continue;
            }
            if (propertyName == null || propertyName.isBlank()) {
                propertyName = "textures";
            }
            Object property = createMojangProfileProperty(profileProperty, gameProfileType.getClassLoader());
            invokeCompatibleIfPresent(propertyMap, "put", propertyName, property);
        }
        return profile;
    }

    private Object createMojangProfileProperty(
            Object profileProperty,
            ClassLoader preferredLoader
    ) throws ReflectiveOperationException {
        String propertyName = getStringValue(profileProperty, "name");
        String propertyValue = getStringValue(profileProperty, "value");
        String propertySignature = getStringValue(profileProperty, "signature");
        if (propertyName == null || propertyName.isBlank()) {
            propertyName = "textures";
        }

        List<ClassLoader> loaders = new ArrayList<>();
        if (preferredLoader != null) {
            loaders.add(preferredLoader);
        }
        ClassLoader serverLoader = plugin.getServer().getClass().getClassLoader();
        if (serverLoader != null && !loaders.contains(serverLoader)) {
            loaders.add(serverLoader);
        }
        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        if (contextLoader != null && !loaders.contains(contextLoader)) {
            loaders.add(contextLoader);
        }

        ClassNotFoundException missing = null;
        for (ClassLoader loader : loaders) {
            try {
                Class<?> propertyType = Class.forName("com.mojang.authlib.properties.Property", false, loader);
                for (Constructor<?> constructor : propertyType.getDeclaredConstructors()) {
                    Class<?>[] parameters = constructor.getParameterTypes();
                    if (parameters.length == 3
                            && parameters[0] == String.class
                            && parameters[1] == String.class
                            && parameters[2] == String.class) {
                        constructor.setAccessible(true);
                        return constructor.newInstance(
                                propertyName,
                                propertyValue,
                                propertySignature
                        );
                    }
                    if (parameters.length == 2
                            && parameters[0] == String.class
                            && parameters[1] == String.class) {
                        constructor.setAccessible(true);
                        return constructor.newInstance(propertyName, propertyValue);
                    }
                }
            } catch (ClassNotFoundException exception) {
                missing = exception;
            }
        }

        throw missing == null
                ? new ClassNotFoundException("com.mojang.authlib.properties.Property")
                : missing;
    }

    private Object createResolvableProfile(Class<?> resolvableProfileType, Object gameProfile)
            throws ReflectiveOperationException {
        List<String> preferredNames = List.of(
                "createResolved",
                "ofStatic",
                "a",
                "m_416870_",
                "method_73307"
        );
        for (String preferredName : preferredNames) {
            Object profile = invokeStaticSingleArgFactory(resolvableProfileType, preferredName, gameProfile);
            if (profile != null) {
                return profile;
            }
        }

        for (Method method : resolvableProfileType.getMethods()) {
            Object profile = invokeSingleArgFactory(resolvableProfileType, method, gameProfile);
            if (profile != null) {
                return profile;
            }
        }
        for (Method method : resolvableProfileType.getDeclaredMethods()) {
            Object profile = invokeSingleArgFactory(resolvableProfileType, method, gameProfile);
            if (profile != null) {
                return profile;
            }
        }

        throw new NoSuchMethodException("ResolvableProfile.createResolved(GameProfile)");
    }

    private Object createNativeObjectComponent(
            Object objectInfo,
            ClassLoader loader,
            Class<?> nativeComponentType
    ) throws ReflectiveOperationException {
        Object component = invokeStaticObjectComponentFactory(nativeComponentType, objectInfo);
        if (component != null) {
            return component;
        }

        Class<?> objectContentsType = Class.forName(
                "net.minecraft.network.chat.contents.ObjectContents",
                false,
                loader
        );
        Object objectContents = null;
        for (Constructor<?> constructor : objectContentsType.getDeclaredConstructors()) {
            Class<?>[] parameters = constructor.getParameterTypes();
            if (parameters.length == 1 && parameters[0].isAssignableFrom(objectInfo.getClass())) {
                constructor.setAccessible(true);
                objectContents = constructor.newInstance(objectInfo);
                break;
            }
        }
        if (objectContents == null) {
            throw new NoSuchMethodException("ObjectContents(ObjectInfo)");
        }

        Class<?> mutableComponentType = getFirstAvailableClassOrNull(
                loader,
                "net.minecraft.network.chat.MutableComponent",
                "net.minecraft.network.chat.IChatMutableComponent"
        );
        if (mutableComponentType == null) {
            throw new ClassNotFoundException("MutableComponent");
        }

        for (Method method : mutableComponentType.getMethods()) {
            component = invokeSingleArgFactory(mutableComponentType, method, objectContents);
            if (component != null) {
                return component;
            }
        }
        for (Method method : mutableComponentType.getDeclaredMethods()) {
            component = invokeSingleArgFactory(mutableComponentType, method, objectContents);
            if (component != null) {
                return component;
            }
        }

        throw new NoSuchMethodException("MutableComponent.create(ObjectContents)");
    }

    private Object invokeStaticObjectComponentFactory(Class<?> nativeComponentType, Object objectInfo) {
        List<String> preferredNames = List.of("object", "a", "m_418787_", "method_74062");
        for (String preferredName : preferredNames) {
            for (Method method : nativeComponentType.getMethods()) {
                if (method.getName().equals(preferredName)) {
                    Object component = invokeSingleArgFactory(nativeComponentType, method, objectInfo);
                    if (component != null) {
                        return component;
                    }
                }
            }
            for (Method method : nativeComponentType.getDeclaredMethods()) {
                if (method.getName().equals(preferredName)) {
                    Object component = invokeSingleArgFactory(nativeComponentType, method, objectInfo);
                    if (component != null) {
                        return component;
                    }
                }
            }
        }
        return null;
    }

    private Object invokeStaticSingleArgFactory(Class<?> returnType, String methodName, Object arg) {
        for (Method method : returnType.getMethods()) {
            if (method.getName().equals(methodName)) {
                Object value = invokeSingleArgFactory(returnType, method, arg);
                if (value != null) {
                    return value;
                }
            }
        }
        for (Method method : returnType.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                Object value = invokeSingleArgFactory(returnType, method, arg);
                if (value != null) {
                    return value;
                }
            }
        }
        return null;
    }

    private Object invokeSingleArgFactory(Class<?> returnType, Method method, Object arg) {
        if (!Modifier.isStatic(method.getModifiers())
                || method.getParameterCount() != 1
                || arg == null
                || !method.getParameterTypes()[0].isAssignableFrom(arg.getClass())
                || !returnType.isAssignableFrom(method.getReturnType())) {
            return null;
        }

        try {
            method.setAccessible(true);
            return method.invoke(null, arg);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return null;
        }
    }

    private void appendNativeComponent(
            Object nativeComponent,
            Object nativeChild,
            Class<?> nativeComponentType
    ) throws ReflectiveOperationException {
        if (nativeComponent == null || nativeChild == null) {
            return;
        }

        List<String> preferredNames = List.of("append", "b", "m_7220_", "method_10852");
        for (String preferredName : preferredNames) {
            if (invokeAppendNativeComponent(nativeComponent, nativeChild, nativeComponentType, preferredName)) {
                return;
            }
        }

        for (Method method : nativeComponent.getClass().getMethods()) {
            if (invokeAppendNativeComponent(nativeComponent, nativeChild, nativeComponentType, method)) {
                return;
            }
        }
        for (Method method : nativeComponent.getClass().getDeclaredMethods()) {
            if (invokeAppendNativeComponent(nativeComponent, nativeChild, nativeComponentType, method)) {
                return;
            }
        }

        throw new NoSuchMethodException("MutableComponent.append(Component)");
    }

    private boolean invokeAppendNativeComponent(
            Object nativeComponent,
            Object nativeChild,
            Class<?> nativeComponentType,
            String methodName
    ) {
        for (Method method : nativeComponent.getClass().getMethods()) {
            if (method.getName().equals(methodName)
                    && invokeAppendNativeComponent(nativeComponent, nativeChild, nativeComponentType, method)) {
                return true;
            }
        }
        for (Method method : nativeComponent.getClass().getDeclaredMethods()) {
            if (method.getName().equals(methodName)
                    && invokeAppendNativeComponent(nativeComponent, nativeChild, nativeComponentType, method)) {
                return true;
            }
        }
        return false;
    }

    private boolean invokeAppendNativeComponent(
            Object nativeComponent,
            Object nativeChild,
            Class<?> nativeComponentType,
            Method method
    ) {
        if (method.getParameterCount() != 1
                || !method.getParameterTypes()[0].isAssignableFrom(nativeChild.getClass())
                || !nativeComponentType.isAssignableFrom(method.getParameterTypes()[0])) {
            return false;
        }

        try {
            method.setAccessible(true);
            method.invoke(nativeComponent, nativeChild);
            return true;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return false;
        }
    }

    private void applyNativeColor(Object nativeComponent, TextColor color) {
        if (nativeComponent == null || color == null) {
            return;
        }

        List<String> preferredNames = List.of("withColor", "b", "m_306658_", "method_54663");
        for (String preferredName : preferredNames) {
            if (invokeNativeColor(nativeComponent, color.value(), preferredName)) {
                return;
            }
        }
    }

    private boolean invokeNativeColor(Object nativeComponent, int color, String methodName) {
        for (Method method : nativeComponent.getClass().getMethods()) {
            if (method.getName().equals(methodName) && invokeNativeColor(nativeComponent, color, method)) {
                return true;
            }
        }
        for (Method method : nativeComponent.getClass().getDeclaredMethods()) {
            if (method.getName().equals(methodName) && invokeNativeColor(nativeComponent, color, method)) {
                return true;
            }
        }
        return false;
    }

    private boolean invokeNativeColor(Object nativeComponent, int color, Method method) {
        if (method.getParameterCount() != 1
                || !(method.getParameterTypes()[0] == int.class || method.getParameterTypes()[0] == Integer.class)
                || !method.getReturnType().isAssignableFrom(nativeComponent.getClass())) {
            return false;
        }

        try {
            method.setAccessible(true);
            method.invoke(nativeComponent, color);
            return true;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return false;
        }
    }

    private boolean invokeCompatibleIfPresent(Object target, String methodName, Object... args)
            throws ReflectiveOperationException {
        if (target == null) {
            return false;
        }

        for (Method method : target.getClass().getMethods()) {
            if (!method.getName().equals(methodName)
                    || method.getParameterCount() != args.length
                    || !canAccept(method.getParameterTypes(), args)) {
                continue;
            }

            method.setAccessible(true);
            method.invoke(target, args);
            return true;
        }

        for (Class<?> current = target.getClass(); current != null; current = current.getSuperclass()) {
            for (Method method : current.getDeclaredMethods()) {
                if (!method.getName().equals(methodName)
                        || method.getParameterCount() != args.length
                        || !canAccept(method.getParameterTypes(), args)) {
                    continue;
                }

                method.setAccessible(true);
                method.invoke(target, args);
                return true;
            }
        }

        return false;
    }

    private boolean canAccept(Class<?>[] parameterTypes, Object[] args) {
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

    private Class<?> wrapPrimitive(Class<?> type) {
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

    public boolean refreshAvatar(Player target) {
        if (avatarDisabled || target == null || !target.isOnline()) {
            return false;
        }

        try {
            Object handle = invokeNoArg(target, "getHandle");
            Object removePacket = createRemovePlayerPacket(target.getUniqueId(), handle);
            for (Player viewer : Bukkit.getOnlinePlayers()) {
                if (viewer != null && viewer.isOnline()) {
                    sendPacket(viewer, removePacket);
                }
            }
            scheduleAvatarAdd(target.getUniqueId(), 2L);
            scheduleAvatarAdd(target.getUniqueId(), 6L);
            return true;
        } catch (ReflectiveOperationException | RuntimeException exception) {
            disableAvatarWithWarning(exception);
            return false;
        }
    }

    public boolean refreshEntry(Player target, net.kyori.adventure.text.Component adventureComponent) {
        if (avatarDisabled || target == null || !target.isOnline() || adventureComponent == null) {
            return false;
        }

        try {
            Object handle = invokeNoArg(target, "getHandle");
            Object displayName = toNativeComponent(adventureComponent);
            if (displayName == null) {
                return false;
            }

            setTabListDisplayName(handle, displayName);
            Object removePacket = createRemovePlayerPacket(target.getUniqueId(), handle);
            for (Player viewer : Bukkit.getOnlinePlayers()) {
                if (viewer != null && viewer.isOnline()) {
                    sendPacket(viewer, removePacket);
                }
            }
            scheduleEntryAdd(target.getUniqueId(), adventureComponent, 2L);
            scheduleEntryAdd(target.getUniqueId(), adventureComponent, 8L);
            return true;
        } catch (ReflectiveOperationException | RuntimeException exception) {
            disableAvatarWithWarning(exception);
            return false;
        }
    }

    private void scheduleAvatarAdd(UUID targetId, long delayTicks) {
        plugin.getFoliaScheduler().runGlobalLater(() -> {
            Player target = Bukkit.getPlayer(targetId);
            if (target == null || !target.isOnline()) {
                return;
            }

            try {
                Object handle = invokeNoArg(target, "getHandle");
                Object addPacket = createAddPlayerPacket(handle);
                Object displayNamePacket = createDisplayNamePacket(handle);
                for (Player viewer : Bukkit.getOnlinePlayers()) {
                    if (viewer != null && viewer.isOnline()) {
                        sendPacket(viewer, addPacket);
                        sendPacket(viewer, displayNamePacket);
                    }
                }
            } catch (ReflectiveOperationException | RuntimeException exception) {
                disableAvatarWithWarning(exception);
            }
        }, delayTicks);
    }

    private void scheduleEntryAdd(
            UUID targetId,
            net.kyori.adventure.text.Component adventureComponent,
            long delayTicks
    ) {
        plugin.getFoliaScheduler().runGlobalLater(() -> {
            Player target = Bukkit.getPlayer(targetId);
            if (target == null || !target.isOnline()) {
                return;
            }

            try {
                Object handle = invokeNoArg(target, "getHandle");
                Object displayName = toNativeComponent(adventureComponent);
                if (displayName == null) {
                    return;
                }
                setTabListDisplayName(handle, displayName);
                Object addPacket = createAddPlayerPacket(handle, displayName);
                Object displayNamePacket = createDisplayNamePacket(handle, displayName);
                for (Player viewer : Bukkit.getOnlinePlayers()) {
                    if (viewer != null && viewer.isOnline()) {
                        sendPacket(viewer, addPacket);
                        sendPacket(viewer, displayNamePacket);
                    }
                }
            } catch (ReflectiveOperationException | RuntimeException exception) {
                disableAvatarWithWarning(exception);
            }
        }, delayTicks);
    }

    private Object parseNativeComponent(String json) {
        try {
            Object component = parseWithCraftChatMessage(json);
            if (component != null) {
                return component;
            }
        } catch (ReflectiveOperationException | RuntimeException ignored) {
        }

        try {
            return parseWithMinecraftSerializer(json);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return null;
        }
    }

    private Object parseWithCraftChatMessage(String json) throws ReflectiveOperationException {
        ClassLoader loader = plugin.getServer().getClass().getClassLoader();
        for (String className : craftChatMessageClassNames()) {
            Class<?> type;
            try {
                type = Class.forName(className, false, loader);
            } catch (ClassNotFoundException ignored) {
                continue;
            }

            for (String name : List.of("fromJSON", "fromJson", "fromJSONOrNull", "fromJsonOrNull")) {
                for (Method method : type.getDeclaredMethods()) {
                    if (!Modifier.isStatic(method.getModifiers())
                            || !method.getName().equals(name)
                            || method.getParameterCount() != 1
                            || method.getParameterTypes()[0] != String.class) {
                        continue;
                    }
                    method.setAccessible(true);
                    Object value = unwrapOptional(method.invoke(null, json));
                    if (value != null) {
                        return value;
                    }
                }
            }
        }
        return null;
    }

    private Object parseWithMinecraftSerializer(String json) throws ReflectiveOperationException {
        ClassLoader loader = plugin.getServer().getClass().getClassLoader();
        Class<?> componentType = getFirstAvailableClassOrNull(
                loader,
                "net.minecraft.network.chat.Component",
                "net.minecraft.network.chat.IChatBaseComponent"
        );
        if (componentType == null) {
            return null;
        }

        Class<?> serializerType = getFirstAvailableClassOrNull(
                loader,
                componentType.getName() + "$Serializer",
                componentType.getName() + "$ChatSerializer"
        );
        if (serializerType == null) {
            serializerType = findNestedSerializerType(componentType);
        }
        if (serializerType == null) {
            return null;
        }

        for (Method method : serializerType.getDeclaredMethods()) {
            Object component = parseWithSerializerMethod(componentType, method, json);
            if (component != null) {
                return component;
            }
        }
        for (Method method : serializerType.getMethods()) {
            Object component = parseWithSerializerMethod(componentType, method, json);
            if (component != null) {
                return component;
            }
        }

        return null;
    }

    private List<String> craftChatMessageClassNames() {
        LinkedHashSet<String> names = new LinkedHashSet<>();
        String serverPackage = plugin.getServer().getClass().getPackage().getName();
        if (serverPackage.startsWith("org.bukkit.craftbukkit")) {
            names.add(serverPackage + ".util.CraftChatMessage");
        }
        names.add("org.bukkit.craftbukkit.util.CraftChatMessage");
        return new ArrayList<>(names);
    }

    private Class<?> findNestedSerializerType(Class<?> componentType) {
        for (Class<?> nested : componentType.getDeclaredClasses()) {
            String simpleName = nested.getSimpleName().toLowerCase(Locale.ROOT);
            if (simpleName.contains("serializer") || simpleName.contains("chatserializer")) {
                return nested;
            }
        }
        return null;
    }

    private Object parseWithSerializerMethod(Class<?> componentType, Method method, String json) {
        if (!Modifier.isStatic(method.getModifiers())
                || method.getParameterCount() != 1
                || method.getParameterTypes()[0] != String.class) {
            return null;
        }

        String name = method.getName().toLowerCase(Locale.ROOT);
        if (!name.contains("json") && !name.equals("a") && !name.contains("deserialize")) {
            return null;
        }

        try {
            method.setAccessible(true);
            Object value = unwrapOptional(method.invoke(null, json));
            return componentType.isInstance(value) ? value : null;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return null;
        }
    }

    private void setTabListDisplayName(Object handle, Object component) throws ReflectiveOperationException {
        Method method = findDisplayNameSetter(handle.getClass(), component);
        if (method != null) {
            method.setAccessible(true);
            method.invoke(handle, component);
            return;
        }

        Field field = findDisplayNameField(handle, component);
        if (field != null) {
            field.setAccessible(true);
            field.set(handle, component);
            return;
        }

        throw new NoSuchFieldException("ServerPlayer tablist display name");
    }

    private Method findDisplayNameSetter(Class<?> type, Object component) {
        Method fallback = null;
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            for (Method method : current.getDeclaredMethods()) {
                if (method.getParameterCount() != 1
                        || !isVoidLike(method.getReturnType())
                        || !method.getParameterTypes()[0].isAssignableFrom(component.getClass())) {
                    continue;
                }
                String name = method.getName().toLowerCase(Locale.ROOT);
                if (name.contains("tab") && name.contains("list") && name.contains("name")) {
                    return method;
                }
                if ((name.contains("display") && name.contains("name"))
                        || (name.contains("list") && name.contains("name"))) {
                    fallback = method;
                }
            }
        }
        return fallback;
    }

    private Field findDisplayNameField(Object handle, Object component) {
        Field fallback = null;
        Field nullableFallback = null;
        Field componentFallback = null;
        Class<?> type = handle.getClass();
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                if (Modifier.isFinal(field.getModifiers())
                        || !field.getType().isAssignableFrom(component.getClass())) {
                    continue;
                }
                if (componentFallback == null) {
                    componentFallback = field;
                }
                String name = field.getName().toLowerCase(Locale.ROOT);
                if (name.contains("tab") && name.contains("list") && name.contains("name")) {
                    return field;
                }
                if ((name.contains("display") && name.contains("name"))
                        || (name.contains("list") && name.contains("name"))) {
                    fallback = field;
                    continue;
                }
                if (nullableFallback == null && isNullField(handle, field)) {
                    nullableFallback = field;
                }
            }
        }
        if (fallback != null) {
            return fallback;
        }
        return nullableFallback != null ? nullableFallback : componentFallback;
    }

    private boolean isNullField(Object handle, Field field) {
        try {
            field.setAccessible(true);
            return field.get(handle) == null;
        } catch (RuntimeException exception) {
            return false;
        } catch (IllegalAccessException exception) {
            return false;
        }
    }

    private Object createDisplayNamePacket(Object handle) throws ReflectiveOperationException {
        return createDisplayNamePacket(handle, null);
    }

    private Object createDisplayNamePacket(Object handle, Object displayName) throws ReflectiveOperationException {
        Class<?> packetClass = getFirstAvailableClass(PLAYER_INFO_UPDATE_PACKET_CLASS_NAMES);
        Object action = findAction(packetClass, "UPDATE_DISPLAY_NAME", "UPDATE_DISPLAY");
        if (action == null) {
            throw new NoSuchFieldException("UPDATE_DISPLAY_NAME action");
        }
        Object packet = instantiateActionPacket(packetClass, action, handle);
        if (displayName != null) {
            replacePlayerInfoDisplayName(packet, displayName);
        }
        return packet;
    }

    private Object createAddPlayerPacket(Object handle) throws ReflectiveOperationException {
        Class<?> packetClass = getFirstAvailableClass(PLAYER_INFO_UPDATE_PACKET_CLASS_NAMES);
        List<Object> actions = findActions(
                packetClass,
                "ADD_PLAYER",
                "INITIALIZE_CHAT",
                "UPDATE_LISTED",
                "UPDATE_GAME_MODE",
                "UPDATE_LATENCY",
                "UPDATE_DISPLAY_NAME"
        );
        if (actions.isEmpty()) {
            throw new NoSuchFieldException("ADD_PLAYER action");
        }
        return instantiateActionPacket(packetClass, actions.toArray(), handle);
    }

    private Object createAddPlayerPacket(Object handle, Object displayName) throws ReflectiveOperationException {
        Object packet = createAddPlayerPacket(handle);
        if (displayName != null) {
            replacePlayerInfoDisplayName(packet, displayName);
        }
        return packet;
    }

    private Object createRemovePlayerPacket(UUID playerId, Object handle) throws ReflectiveOperationException {
        try {
            Class<?> packetClass = getFirstAvailableClass(PLAYER_INFO_REMOVE_PACKET_CLASS_NAMES);
            List<UUID> playerIds = List.of(playerId);
            for (Constructor<?> constructor : packetClass.getDeclaredConstructors()) {
                Class<?>[] parameters = constructor.getParameterTypes();
                constructor.setAccessible(true);
                if (parameters.length == 1 && Collection.class.isAssignableFrom(parameters[0])) {
                    return constructor.newInstance(playerIds);
                }
                if (parameters.length == 1 && Iterable.class.isAssignableFrom(parameters[0])) {
                    return constructor.newInstance(playerIds);
                }
                if (parameters.length == 1 && parameters[0].isAssignableFrom(UUID.class)) {
                    return constructor.newInstance(playerId);
                }
            }
        } catch (ClassNotFoundException ignored) {
        }

        Class<?> packetClass = getFirstAvailableClass(PLAYER_INFO_UPDATE_PACKET_CLASS_NAMES);
        Object action = findAction(packetClass, "REMOVE_PLAYER");
        if (action == null) {
            throw new NoSuchFieldException("REMOVE_PLAYER action");
        }
        return instantiateActionPacket(packetClass, action, handle);
    }

    private void replacePlayerInfoDisplayName(Object packet, Object displayName) throws ReflectiveOperationException {
        Object entriesObject = readPlayerInfoEntries(packet);
        if (!(entriesObject instanceof List<?> entries) || entries.isEmpty()) {
            return;
        }

        List<Object> replaced = new ArrayList<>(entries.size());
        boolean changed = false;
        for (Object entry : entries) {
            Object newEntry = replacePlayerInfoEntryDisplayName(entry, displayName);
            replaced.add(newEntry);
            changed |= newEntry != entry;
        }
        if (!changed) {
            return;
        }

        if (replaceListContents(entriesObject, replaced)) {
            return;
        }

        setPlayerInfoEntries(packet, replaced);
    }

    private Object readPlayerInfoEntries(Object packet) throws ReflectiveOperationException {
        for (Method method : packet.getClass().getMethods()) {
            Object entries = invokeEntriesAccessor(packet, method);
            if (entries != null) {
                return entries;
            }
        }
        for (Method method : packet.getClass().getDeclaredMethods()) {
            Object entries = invokeEntriesAccessor(packet, method);
            if (entries != null) {
                return entries;
            }
        }

        for (Class<?> current = packet.getClass(); current != null; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                if (!List.class.isAssignableFrom(field.getType())) {
                    continue;
                }
                field.setAccessible(true);
                Object value = field.get(packet);
                if (value instanceof List<?> list && !list.isEmpty()) {
                    return list;
                }
            }
        }
        return null;
    }

    private Object invokeEntriesAccessor(Object packet, Method method) {
        if (method.getParameterCount() != 0 || !List.class.isAssignableFrom(method.getReturnType())) {
            return null;
        }

        String name = method.getName().toLowerCase(Locale.ROOT);
        if (!name.contains("entr") && !name.equals("e") && !name.equals("f") && !name.equals("c")) {
            return null;
        }

        try {
            method.setAccessible(true);
            Object value = method.invoke(packet);
            if (value instanceof List<?> list && !list.isEmpty()) {
                return value;
            }
        } catch (ReflectiveOperationException | RuntimeException ignored) {
        }
        return null;
    }

    private Object replacePlayerInfoEntryDisplayName(Object entry, Object displayName)
            throws ReflectiveOperationException {
        if (entry == null || displayName == null) {
            return entry;
        }

        Object byRecord = replaceRecordDisplayName(entry, displayName);
        if (byRecord != null) {
            return byRecord;
        }

        if (setDisplayNameField(entry, displayName)) {
            return entry;
        }
        return entry;
    }

    private Object replaceRecordDisplayName(Object entry, Object displayName)
            throws ReflectiveOperationException {
        Class<?> entryClass = entry.getClass();
        if (!entryClass.isRecord()) {
            return null;
        }

        RecordComponent[] components = entryClass.getRecordComponents();
        if (components == null || components.length == 0) {
            return null;
        }

        Constructor<?> constructor = null;
        for (Constructor<?> candidate : entryClass.getDeclaredConstructors()) {
            if (candidate.getParameterCount() == components.length) {
                constructor = candidate;
                break;
            }
        }
        if (constructor == null) {
            return null;
        }

        Object[] args = new Object[components.length];
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        boolean replaced = false;
        for (int index = 0; index < components.length; index++) {
            Method accessor = components[index].getAccessor();
            accessor.setAccessible(true);
            Object value = accessor.invoke(entry);
            if (!replaced && parameterTypes[index].isAssignableFrom(displayName.getClass())) {
                value = displayName;
                replaced = true;
            }
            args[index] = value;
        }

        if (!replaced) {
            return null;
        }

        constructor.setAccessible(true);
        return constructor.newInstance(args);
    }

    private boolean setDisplayNameField(Object entry, Object displayName) {
        for (Class<?> current = entry.getClass(); current != null; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                if (!field.getType().isAssignableFrom(displayName.getClass())) {
                    continue;
                }

                String name = field.getName().toLowerCase(Locale.ROOT);
                if (!name.contains("display") && !name.contains("name") && !name.equals("f")) {
                    continue;
                }

                try {
                    field.setAccessible(true);
                    field.set(entry, displayName);
                    return true;
                } catch (RuntimeException | IllegalAccessException ignored) {
                }
            }
        }
        return false;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private boolean replaceListContents(Object entriesObject, List<Object> replaced) {
        if (!(entriesObject instanceof List entries)) {
            return false;
        }

        try {
            entries.clear();
            entries.addAll(replaced);
            return true;
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private void setPlayerInfoEntries(Object packet, List<Object> replaced) throws ReflectiveOperationException {
        for (Class<?> current = packet.getClass(); current != null; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                if (!List.class.isAssignableFrom(field.getType())) {
                    continue;
                }

                field.setAccessible(true);
                Object value = field.get(packet);
                if (value instanceof List<?> list && !list.isEmpty()) {
                    field.set(packet, replaced);
                    return;
                }
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Object instantiateActionPacket(Class<?> packetClass, Object action, Object handle)
            throws ReflectiveOperationException {
        return instantiateActionPacket(packetClass, new Object[]{action}, handle);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Object instantiateActionPacket(Class<?> packetClass, Object[] actions, Object handle)
            throws ReflectiveOperationException {
        EnumSet actionSet = null;
        Object action = actions.length == 0 ? null : actions[0];
        if (action instanceof Enum enumAction) {
            actionSet = EnumSet.noneOf(enumAction.getDeclaringClass());
            for (Object candidate : actions) {
                if (candidate instanceof Enum candidateAction
                        && candidateAction.getDeclaringClass() == enumAction.getDeclaringClass()) {
                    actionSet.add(candidateAction);
                }
            }
        }
        List<Object> handles = List.of(handle);

        for (Constructor<?> constructor : packetClass.getDeclaredConstructors()) {
            Class<?>[] parameters = constructor.getParameterTypes();
            if (parameters.length != 2) {
                continue;
            }
            constructor.setAccessible(true);

            if (actionSet != null
                    && EnumSet.class.isAssignableFrom(parameters[0])
                    && (Collection.class.isAssignableFrom(parameters[1])
                    || Iterable.class.isAssignableFrom(parameters[1]))) {
                return constructor.newInstance(actionSet, handles);
            }
            if (actionSet != null
                    && EnumSet.class.isAssignableFrom(parameters[1])
                    && (Collection.class.isAssignableFrom(parameters[0])
                    || Iterable.class.isAssignableFrom(parameters[0]))) {
                return constructor.newInstance(handles, actionSet);
            }
            if (parameters[0].isAssignableFrom(action.getClass()) && parameters[1].isArray()) {
                Object array = Array.newInstance(parameters[1].getComponentType(), 1);
                Array.set(array, 0, handle);
                return constructor.newInstance(action, array);
            }
            if (parameters[1].isAssignableFrom(action.getClass()) && parameters[0].isArray()) {
                Object array = Array.newInstance(parameters[0].getComponentType(), 1);
                Array.set(array, 0, handle);
                return constructor.newInstance(array, action);
            }
            if (parameters[0].isAssignableFrom(action.getClass())
                    && (Collection.class.isAssignableFrom(parameters[1])
                    || Iterable.class.isAssignableFrom(parameters[1]))) {
                return constructor.newInstance(action, handles);
            }
            if (parameters[1].isAssignableFrom(action.getClass())
                    && (Collection.class.isAssignableFrom(parameters[0])
                    || Iterable.class.isAssignableFrom(parameters[0]))) {
                return constructor.newInstance(handles, action);
            }
        }

        throw new NoSuchMethodException(packetClass.getName() + "(UPDATE_DISPLAY_NAME,ServerPlayer)");
    }

    private Object findAction(Class<?> packetClass, String... names) {
        List<Object> actions = findActions(packetClass, names);
        return actions.isEmpty() ? null : actions.getFirst();
    }

    private List<Object> findActions(Class<?> packetClass, String... names) {
        java.util.ArrayList<Object> actions = new java.util.ArrayList<>();
        for (Class<?> nested : packetClass.getDeclaredClasses()) {
            if (!nested.isEnum()) {
                continue;
            }
            Object[] constants = nested.getEnumConstants();
            if (constants == null) {
                continue;
            }
            for (String name : names) {
                for (Object constant : constants) {
                    if (((Enum<?>) constant).name().equalsIgnoreCase(name) && !actions.contains(constant)) {
                        actions.add(constant);
                    }
                }
            }
        }
        return actions;
    }

    private void sendPacket(Player player, Object packet) throws ReflectiveOperationException {
        Object handle = invokeNoArg(player, "getHandle");
        PacketSender sender = findPacketSender(handle, packet);
        if (sender == null) {
            throw new NoSuchMethodException("packet sender");
        }
        sender.send(packet);
    }

    private PacketSender findPacketSender(Object root, Object packet) throws ReflectiveOperationException {
        PacketSender sender = findSenderOn(root, packet);
        if (sender != null) {
            return sender;
        }

        for (Class<?> current = root.getClass(); current != null; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                field.setAccessible(true);
                Object value = field.get(root);
                if (value == null) {
                    continue;
                }
                sender = findSenderOn(value, packet);
                if (sender != null) {
                    return sender;
                }
            }
        }

        return null;
    }

    private PacketSender findSenderOn(Object target, Object packet) {
        PacketSender fallback = null;
        for (Class<?> current = target.getClass(); current != null; current = current.getSuperclass()) {
            for (Method method : current.getDeclaredMethods()) {
                Class<?>[] parameters = method.getParameterTypes();
                if (parameters.length == 1 && acceptsPacket(parameters[0], packet)) {
                    PacketSender sender = new PacketSender(target, method, false);
                    if (isPreferredSendMethod(method)) {
                        return sender;
                    }
                    fallback = sender;
                }
                if (parameters.length == 2 && acceptsPacket(parameters[0], packet)) {
                    PacketSender sender = new PacketSender(target, method, true);
                    if (isPreferredSendMethod(method)) {
                        return sender;
                    }
                    fallback = sender;
                }
            }
        }
        return fallback;
    }

    private boolean acceptsPacket(Class<?> parameterType, Object packet) {
        return parameterType.isAssignableFrom(packet.getClass())
                || "net.minecraft.network.protocol.Packet".equals(parameterType.getName())
                || "Packet".equals(parameterType.getSimpleName());
    }

    private boolean isPreferredSendMethod(Method method) {
        String name = method.getName().toLowerCase(Locale.ROOT);
        return "send".equals(name) || "sendpacket".equals(name) || "a".equals(name);
    }

    private String getStringValue(Object target, String methodName) throws ReflectiveOperationException {
        if (target == null) {
            return null;
        }

        Object value = unwrapOptional(invokeNoArg(target, methodName));
        return value instanceof String string ? string : null;
    }

    private UUID getUuidValue(Object target, String methodName) throws ReflectiveOperationException {
        if (target == null) {
            return null;
        }

        Object value = unwrapOptional(invokeNoArg(target, methodName));
        return value instanceof UUID uuid ? uuid : null;
    }

    private boolean getBooleanValue(Object target, String methodName) throws ReflectiveOperationException {
        if (target == null) {
            return false;
        }

        Object value = unwrapOptional(invokeNoArg(target, methodName));
        return value instanceof Boolean bool && bool;
    }

    private Object invokeNoArg(Object target, String name) throws ReflectiveOperationException {
        Method method = findNoArgMethod(target.getClass(), name);
        if (method == null) {
            throw new NoSuchMethodException(name);
        }
        method.setAccessible(true);
        return method.invoke(target);
    }

    private Method findNoArgMethod(Class<?> type, String name) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            for (Method method : current.getDeclaredMethods()) {
                if (method.getParameterCount() == 0 && method.getName().equals(name)) {
                    return method;
                }
            }
        }
        return null;
    }

    private Class<?> getFirstAvailableClass(String[] classNames) throws ClassNotFoundException {
        ClassLoader loader = plugin.getServer().getClass().getClassLoader();
        for (String className : classNames) {
            try {
                return Class.forName(className, false, loader);
            } catch (ClassNotFoundException ignored) {
            }
        }
        throw new ClassNotFoundException(String.join(", ", classNames));
    }

    private Class<?> getFirstAvailableClassOrNull(ClassLoader loader, String... classNames) {
        for (String className : classNames) {
            try {
                return Class.forName(className, false, loader);
            } catch (ClassNotFoundException ignored) {
            }
        }
        return null;
    }

    private Object unwrapOptional(Object value) {
        if (value instanceof Optional<?> optional) {
            return optional.orElse(null);
        }
        return value;
    }

    private boolean isVoidLike(Class<?> type) {
        return type == Void.TYPE || type == Void.class;
    }

    private void disableWithWarning(Exception exception) {
        if (warned) {
            return;
        }
        warned = true;
        Throwable cause = exception.getCause() == null ? exception : exception.getCause();
        plugin.getLogger().warning("[Tablist] Unable to send Adventure tablist name components on this Spigot build: "
                + cause.getClass().getSimpleName() + ": " + cause.getMessage()
                + ". Future tablist updates will retry component rendering.");
    }

    private void disableAvatarWithWarning(Exception exception) {
        if (avatarWarned) {
            return;
        }
        avatarWarned = true;
        Throwable cause = exception.getCause() == null ? exception : exception.getCause();
        plugin.getLogger().warning("[Tablist] Unable to refresh tablist skin avatars on this Spigot build: "
                + cause.getClass().getSimpleName() + ": " + cause.getMessage()
                + ". Future skin refreshes will retry avatar packets.");
    }

    private static final class PacketSender {

        private final Object target;
        private final Method method;
        private final boolean trailingNull;

        private PacketSender(Object target, Method method, boolean trailingNull) {
            this.target = target;
            this.method = method;
            this.trailingNull = trailingNull;
        }

        private void send(Object packet) throws ReflectiveOperationException {
            method.setAccessible(true);
            if (trailingNull) {
                method.invoke(target, packet, null);
            } else {
                method.invoke(target, packet);
            }
        }
    }
}
