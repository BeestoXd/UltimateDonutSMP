package com.bx.ultimateDonutSmp.utils;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class ScoreboardNumberHider {

    private static final long RESEND_INTERVAL_MS = 500L;
    private static final String[] PACKET_CLASS_NAMES = {
            "net.minecraft.network.protocol.game.ClientboundSetObjectivePacket",
            "net.minecraft.network.protocol.game.PacketPlayOutScoreboardObjective"
    };
    private static final String[] BLANK_FORMAT_CLASS_NAMES = {
            "net.minecraft.network.chat.numbers.BlankFormat"
    };

    private final UltimateDonutSmp plugin;
    private final Map<String, Long> lastSent = new HashMap<>();

    private boolean disabled;
    private boolean warned;
    private Object blankFormat;
    private Class<?> packetClass;
    private Constructor<?> packetConstructor;
    private Method objectiveHandleMethod;
    private Field objectiveHandleField;

    public ScoreboardNumberHider(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public boolean isEnabled() {
        return plugin.getConfigManager().getScoreboard().getBoolean("SCOREBOARD.HIDE-NUMBERS", true);
    }

    public void hide(Player player, Objective objective) {
        if (disabled || !isEnabled() || player == null || objective == null || !player.isOnline()) {
            return;
        }

        String key = player.getUniqueId() + ":" + objective.getName();
        long now = System.currentTimeMillis();
        Long last = lastSent.get(key);
        if (last != null && now - last < RESEND_INTERVAL_MS) {
            return;
        }

        try {
            Object nmsObjective = getObjectiveHandle(objective);
            Object format = getBlankFormat();
            boolean objectiveUpdated = applyNumberFormatToObjective(nmsObjective, format);
            Object packet = createObjectiveUpdatePacket(nmsObjective, format, objectiveUpdated);
            sendPacket(player, packet);
            lastSent.put(key, now);
        } catch (ReflectiveOperationException | IllegalArgumentException | SecurityException ex) {
            disableWithWarning(ex);
        }
    }

    private Object getObjectiveHandle(Objective objective) throws ReflectiveOperationException {
        if (objectiveHandleMethod != null) {
            return objectiveHandleMethod.invoke(objective);
        }
        if (objectiveHandleField != null) {
            return objectiveHandleField.get(objective);
        }

        Method method = findNoArgMethod(objective.getClass(), "getHandle");
        if (method != null) {
            method.setAccessible(true);
            objectiveHandleMethod = method;
            return method.invoke(objective);
        }

        Field field = findObjectiveField(objective.getClass());
        if (field != null) {
            field.setAccessible(true);
            objectiveHandleField = field;
            return field.get(objective);
        }

        throw new NoSuchFieldException("CraftObjective handle");
    }

    private Object getBlankFormat() throws ReflectiveOperationException {
        if (blankFormat != null) {
            return blankFormat;
        }

        ClassLoader loader = plugin.getServer().getClass().getClassLoader();
        for (String className : BLANK_FORMAT_CLASS_NAMES) {
            try {
                Class<?> type = Class.forName(className, false, loader);
                blankFormat = readSingleton(type);
                if (blankFormat != null) {
                    return blankFormat;
                }
            } catch (ClassNotFoundException ignored) {
            }
        }

        throw new ClassNotFoundException("Minecraft BlankFormat");
    }

    private Object readSingleton(Class<?> type) throws ReflectiveOperationException {
        for (Field field : type.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) || !type.isAssignableFrom(field.getType())) {
                continue;
            }
            field.setAccessible(true);
            Object value = field.get(null);
            if (value != null) {
                return value;
            }
        }

        Object[] constants = type.getEnumConstants();
        if (constants != null && constants.length > 0) {
            return constants[0];
        }

        for (Method method : type.getDeclaredMethods()) {
            if (!Modifier.isStatic(method.getModifiers()) || method.getParameterCount() != 0
                    || !type.isAssignableFrom(method.getReturnType())) {
                continue;
            }
            String name = method.getName().toLowerCase();
            if (!name.contains("instance") && !name.contains("blank")) {
                continue;
            }
            method.setAccessible(true);
            Object value = method.invoke(null);
            if (value != null) {
                return value;
            }
        }

        Constructor<?> constructor = type.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    private boolean applyNumberFormatToObjective(Object nmsObjective, Object format)
            throws ReflectiveOperationException {
        if (invokeNumberFormatSetter(nmsObjective, format)) {
            return true;
        }
        return setNumberFormatField(nmsObjective, format);
    }

    private boolean invokeNumberFormatSetter(Object target, Object format) throws ReflectiveOperationException {
        Method method = findNumberFormatSetter(target.getClass(), format);
        if (method == null) {
            return false;
        }

        method.setAccessible(true);
        Class<?> parameterType = method.getParameterTypes()[0];
        Object argument = parameterType == Optional.class ? Optional.of(format) : format;
        method.invoke(target, argument);
        return true;
    }

    private Method findNumberFormatSetter(Class<?> type, Object format) {
        Method fallback = null;
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            for (Method method : current.getDeclaredMethods()) {
                if (method.getParameterCount() != 1 || !isVoidLike(method.getReturnType())) {
                    continue;
                }
                Class<?> parameterType = method.getParameterTypes()[0];
                if (!acceptsNumberFormat(parameterType, format) && parameterType != Optional.class) {
                    continue;
                }

                String name = method.getName().toLowerCase();
                if ("setnumberformat".equals(name) || "numberformat".equals(name)) {
                    return method;
                }
                if (name.contains("number") || name.contains("format")) {
                    fallback = method;
                }
            }
        }
        return fallback;
    }

    private boolean setNumberFormatField(Object target, Object format) throws ReflectiveOperationException {
        for (Class<?> current = target.getClass(); current != null; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                if (!acceptsNumberFormat(field.getType(), format)) {
                    continue;
                }
                field.setAccessible(true);
                field.set(target, format);
                return true;
            }
        }
        return false;
    }

    private Object createObjectiveUpdatePacket(Object nmsObjective, Object format, boolean objectiveUpdated)
            throws ReflectiveOperationException {
        Constructor<?> constructor = getPacketConstructor(nmsObjective.getClass());
        Object packet = instantiatePacket(constructor, nmsObjective);
        if (!objectiveUpdated && !setPacketNumberFormat(packet, format)) {
            throw new NoSuchFieldException("packet number format");
        }
        return packet;
    }

    private Constructor<?> getPacketConstructor(Class<?> objectiveClass) throws ReflectiveOperationException {
        if (packetConstructor != null) {
            return packetConstructor;
        }

        Class<?> type = getPacketClass();
        for (Constructor<?> constructor : type.getDeclaredConstructors()) {
            Class<?>[] parameters = constructor.getParameterTypes();
            if (parameters.length != 2) {
                continue;
            }
            boolean hasObjective = parameters[0].isAssignableFrom(objectiveClass)
                    || parameters[1].isAssignableFrom(objectiveClass);
            boolean hasMode = parameters[0] == int.class || parameters[1] == int.class;
            if (hasObjective && hasMode) {
                constructor.setAccessible(true);
                packetConstructor = constructor;
                return constructor;
            }
        }

        throw new NoSuchMethodException(type.getName() + "(Objective,int)");
    }

    private Class<?> getPacketClass() throws ClassNotFoundException {
        if (packetClass != null) {
            return packetClass;
        }

        ClassLoader loader = plugin.getServer().getClass().getClassLoader();
        for (String className : PACKET_CLASS_NAMES) {
            try {
                packetClass = Class.forName(className, false, loader);
                return packetClass;
            } catch (ClassNotFoundException ignored) {
            }
        }

        throw new ClassNotFoundException("Minecraft set objective packet");
    }

    private Object instantiatePacket(Constructor<?> constructor, Object nmsObjective)
            throws ReflectiveOperationException {
        Class<?>[] parameters = constructor.getParameterTypes();
        if (parameters[0] == int.class) {
            return constructor.newInstance(2, nmsObjective);
        }
        return constructor.newInstance(nmsObjective, 2);
    }

    private boolean setPacketNumberFormat(Object packet, Object format) throws ReflectiveOperationException {
        for (Field field : packet.getClass().getDeclaredFields()) {
            if (field.getType() == Optional.class) {
                field.setAccessible(true);
                field.set(packet, Optional.of(format));
                return true;
            }
            if (acceptsNumberFormat(field.getType(), format)) {
                field.setAccessible(true);
                field.set(packet, format);
                return true;
            }
        }
        return false;
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
        String name = method.getName().toLowerCase();
        return "send".equals(name) || "sendpacket".equals(name) || "a".equals(name);
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

    private Field findObjectiveField(Class<?> type) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                String typeName = field.getType().getName();
                if ("objective".equalsIgnoreCase(field.getName())
                        || "net.minecraft.world.scores.Objective".equals(typeName)
                        || "net.minecraft.world.scores.ScoreboardObjective".equals(typeName)) {
                    return field;
                }
            }
        }
        return null;
    }

    private boolean acceptsNumberFormat(Class<?> type, Object format) {
        return !type.isPrimitive()
                && (type.isAssignableFrom(format.getClass())
                || "net.minecraft.network.chat.numbers.NumberFormat".equals(type.getName())
                || "NumberFormat".equals(type.getSimpleName()));
    }

    private boolean isVoidLike(Class<?> type) {
        return type == Void.TYPE || type == Void.class;
    }

    private void disableWithWarning(Exception ex) {
        disabled = true;
        if (warned) {
            return;
        }
        warned = true;
        plugin.getLogger().warning("Unable to hide scoreboard sidebar numbers on this Spigot build: "
                + ex.getClass().getSimpleName() + ": " + ex.getMessage());
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
