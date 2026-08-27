package com.bx.ultimateDonutSmp.utils;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.Optional;
import java.util.logging.Level;

/**
 * Builds the three below-name scoreboard packets out of the server's own classes.
 *
 * <p>ProtocolLib assembles these everywhere else in the plugin and is still the first thing tried,
 * but it can only build a packet for a Minecraft build it has already been taught about. On a
 * release it has not caught up with, the scoreboard packets refuse to instantiate at all and money
 * nametags vanish even though nothing about the packets themselves changed. Reading the
 * constructors off the running server instead survives a version bump on its own.</p>
 *
 * <p>The objective these packets carry is registered through the ordinary Bukkit scoreboard API, on
 * a board no player is ever given, so the server hands over its own class rather than the plugin
 * guessing at a name that differs between Spigot and Paper. Folia does not allow that API, so there
 * the objective is built straight from the Mojang-mapped classes Folia always ships.</p>
 */
public final class PacketBelowNameRenderer {

    /** The slot index the pre-1.20.2 display packet used for the line under the name. */
    private static final int LEGACY_BELOW_NAME_SLOT = 2;

    private final UltimateDonutSmp plugin;
    private final String objectiveName;

    private boolean resolved;
    private volatile boolean unavailable;

    private Object nmsObjective;
    private Object belowNameSlot;
    private Constructor<?> objectivePacket;
    private Constructor<?> displayPacket;
    private Constructor<?> scorePacket;
    private Constructor<?> fixedFormat;
    private int numberFormatIndex;

    public PacketBelowNameRenderer(UltimateDonutSmp plugin, String objectiveName) {
        this.plugin = plugin;
        this.objectiveName = objectiveName;
    }

    /** Whether this server exposes everything the three packets need. Resolved once, then cached. */
    public synchronized boolean isUsable() {
        if (!resolved) {
            resolved = true;
            try {
                resolve();
            } catch (ReflectiveOperationException | RuntimeException | LinkageError failure) {
                unavailable = true;
                plugin.getLogger().log(Level.FINE, "Unable to read this server's scoreboard packets.", failure);
            }
        }
        return !unavailable;
    }

    public boolean sendObjective(Player viewer, int method) {
        return isUsable() && send(viewer, () -> build(objectivePacket, method, null));
    }

    public boolean sendDisplaySlot(Player viewer) {
        return isUsable() && send(viewer, () -> build(displayPacket, LEGACY_BELOW_NAME_SLOT, belowNameSlot));
    }

    /** Sends one player's balance as the fixed number format attached to their score. */
    public boolean sendScore(Player viewer, String owner, String legacyText) {
        return isUsable() && send(viewer, () -> buildScore(owner, legacyText));
    }

    /**
     * Fills a constructor from the handful of values these packets are made of. Which parameter
     * takes what is decided by its declared type, because the order the objective, the slot and the
     * mode appear in has changed between releases.
     */
    private Object build(Constructor<?> constructor, int number, Object slot) throws ReflectiveOperationException {
        Class<?>[] parameters = constructor.getParameterTypes();
        Object[] arguments = new Object[parameters.length];
        for (int index = 0; index < parameters.length; index++) {
            Class<?> parameter = parameters[index];
            if (parameter == String.class) {
                arguments[index] = objectiveName;
            } else if (parameter == int.class || parameter == Integer.class) {
                arguments[index] = number;
            } else if (slot != null && parameter.isAssignableFrom(slot.getClass())) {
                arguments[index] = slot;
            } else if (parameter.isAssignableFrom(nmsObjective.getClass())) {
                arguments[index] = nmsObjective;
            } else {
                throw new NoSuchMethodException(constructor.getDeclaringClass().getName());
            }
        }
        return constructor.newInstance(arguments);
    }

    private Object buildScore(String owner, String legacyText) throws ReflectiveOperationException {
        Class<?>[] parameters = scorePacket.getParameterTypes();
        Object[] arguments = new Object[parameters.length];
        int strings = 0;
        int optionals = 0;
        for (int index = 0; index < parameters.length; index++) {
            Class<?> parameter = parameters[index];
            if (parameter == String.class) {
                arguments[index] = strings++ == 0 ? owner : objectiveName;
            } else if (parameter == int.class || parameter == Integer.class) {
                arguments[index] = 0;
            } else {
                arguments[index] = optionals++ == numberFormatIndex
                        ? Optional.of(fixedFormat.newInstance(component(legacyText)))
                        : Optional.empty();
            }
        }
        return scorePacket.newInstance(arguments);
    }

    private boolean send(Player viewer, PacketSupplier supplier) {
        if (viewer == null || !viewer.isOnline()) {
            return false;
        }
        try {
            Object packet = supplier.get();
            Object connection = playerConnection(invoke(viewer, "getHandle"));
            findSender(connection.getClass(), packet).invoke(connection, packet);
            return true;
        } catch (ReflectiveOperationException | RuntimeException | LinkageError failure) {
            unavailable = true;
            plugin.getLogger().log(Level.FINE, "Unable to send a money nametag packet directly.", failure);
            return false;
        }
    }

    // ── Working out what this server calls things ──────────────────────────────

    private void resolve() throws ReflectiveOperationException {
        nmsObjective = createObjective();
        objectivePacket = matchConstructor(
                "net.minecraft.network.protocol.game.ClientboundSetObjectivePacket",
                "net.minecraft.network.protocol.game.PacketPlayOutScoreboardObjective",
                parameters -> takesObjectiveOrName(parameters) && takesNumber(parameters));
        displayPacket = matchConstructor(
                "net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket",
                "net.minecraft.network.protocol.game.PacketPlayOutScoreboardDisplayObjective",
                this::takesObjectiveOrName);
        belowNameSlot = readBelowNameSlot(displayPacket);
        resolveScorePacket();
    }

    private void resolveScorePacket() throws ReflectiveOperationException {
        Class<?> type = loadClass(
                "net.minecraft.network.protocol.game.ClientboundSetScorePacket",
                "net.minecraft.network.protocol.game.PacketPlayOutScoreboardScore");
        for (Constructor<?> candidate : type.getDeclaredConstructors()) {
            int strings = 0;
            int optionals = 0;
            boolean number = false;
            boolean usable = true;
            for (Class<?> parameter : candidate.getParameterTypes()) {
                if (parameter == String.class) {
                    strings++;
                } else if (parameter == int.class || parameter == Integer.class) {
                    number = true;
                } else if (parameter == Optional.class) {
                    optionals++;
                } else {
                    usable = false;
                    break;
                }
            }
            if (usable && strings >= 2 && number && optionals >= 1) {
                candidate.setAccessible(true);
                scorePacket = candidate;
                numberFormatIndex = numberFormatOptional(type, optionals);
                fixedFormat = fixedFormatConstructor();
                return;
            }
        }
        throw new NoSuchMethodException(type.getName());
    }

    /**
     * Which of the packet's optional fields holds the number format. The packet carries an optional
     * display name as well, both are plain {@link Optional} to the constructor, and only the generic
     * type tells them apart, so the field is picked by what it declares rather than by counting on an
     * order that is Minecraft's business. Visible for tests, which stand a record in for the packet.
     */
    static int numberFormatOptional(Class<?> type, int optionals) {
        RecordComponent[] components = type.getRecordComponents();
        if (components != null) {
            int seen = 0;
            for (RecordComponent component : components) {
                if (component.getType() != Optional.class) {
                    continue;
                }
                if (component.getGenericType().getTypeName().contains("NumberFormat")) {
                    return seen;
                }
                seen++;
            }
        }
        return optionals - 1;
    }

    private Constructor<?> fixedFormatConstructor() throws ReflectiveOperationException {
        Class<?> type = loadClass("net.minecraft.network.chat.numbers.FixedFormat");
        Object sample = component("");
        for (Constructor<?> candidate : type.getDeclaredConstructors()) {
            Class<?>[] parameters = candidate.getParameterTypes();
            if (parameters.length == 1 && parameters[0].isAssignableFrom(sample.getClass())) {
                candidate.setAccessible(true);
                return candidate;
            }
        }
        throw new NoSuchMethodException(type.getName());
    }

    /**
     * The enum constant for the line under the name, read off whichever parameter of the display
     * packet carries the slot. Nothing is guessed here: an enum with no {@code BELOW_NAME} in it is
     * not the slot, and a packet with no such parameter falls back to the older integer form.
     */
    private Object readBelowNameSlot(Constructor<?> constructor) {
        for (Class<?> parameter : constructor.getParameterTypes()) {
            if (!parameter.isEnum()) {
                continue;
            }
            for (Object constant : parameter.getEnumConstants()) {
                if (constant instanceof Enum<?> value && value.name().equalsIgnoreCase("BELOW_NAME")) {
                    return constant;
                }
            }
        }
        return null;
    }

    private Constructor<?> matchConstructor(String mojang, String spigot, ParameterTest test)
            throws ReflectiveOperationException {
        Class<?> type = loadClass(mojang, spigot);
        for (Constructor<?> candidate : type.getDeclaredConstructors()) {
            Class<?>[] parameters = candidate.getParameterTypes();
            if (isBuildable(parameters) && test.matches(parameters)) {
                candidate.setAccessible(true);
                return candidate;
            }
        }
        throw new NoSuchMethodException(type.getName());
    }

    private boolean isBuildable(Class<?>[] parameters) {
        if (parameters.length == 0) {
            return false;
        }
        for (Class<?> parameter : parameters) {
            boolean known = parameter == String.class
                    || parameter == int.class
                    || parameter == Integer.class
                    || parameter.isEnum()
                    || parameter.isAssignableFrom(nmsObjective.getClass());
            if (!known) {
                return false;
            }
        }
        return true;
    }

    private boolean takesObjectiveOrName(Class<?>[] parameters) {
        for (Class<?> parameter : parameters) {
            if (parameter == String.class
                    || (!parameter.isEnum() && parameter.isAssignableFrom(nmsObjective.getClass()))) {
                return true;
            }
        }
        return false;
    }

    private boolean takesNumber(Class<?>[] parameters) {
        for (Class<?> parameter : parameters) {
            if (parameter == int.class || parameter == Integer.class) {
                return true;
            }
        }
        return false;
    }

    // ── The objective the packets are built around ─────────────────────────────

    /**
     * Registers the objective on a throwaway board and unwraps the server's own object out of it.
     * That board is never handed to a player, so nothing about it reaches a client except through
     * the packets above.
     */
    private Object createObjective() throws ReflectiveOperationException {
        try {
            org.bukkit.scoreboard.ScoreboardManager manager = Bukkit.getScoreboardManager();
            if (manager != null) {
                Scoreboard board = manager.getNewScoreboard();
                Objective objective = board.registerNewObjective(objectiveName, Criteria.DUMMY, "");
                Object handle = unwrap(objective);
                if (handle != null) {
                    return handle;
                }
            }
        } catch (RuntimeException | LinkageError foliaOrOlderServer) {
            plugin.getLogger().log(Level.FINE,
                    "Building the money nametag objective directly instead.", foliaOrOlderServer);
        }
        return createObjectiveDirectly();
    }

    private Object unwrap(Objective objective) throws ReflectiveOperationException {
        for (Class<?> type = objective.getClass(); type != null; type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
                if (!field.getType().getName().startsWith("net.minecraft")) {
                    continue;
                }
                field.setAccessible(true);
                Object value = field.get(objective);
                if (value != null) {
                    return value;
                }
            }
        }
        return null;
    }

    /** The Folia path: no Bukkit scoreboards there, so the objective is put together by hand. */
    private Object createObjectiveDirectly() throws ReflectiveOperationException {
        Class<?> scoreboardType = loadClass("net.minecraft.world.scores.Scoreboard");
        Class<?> objectiveType = loadClass("net.minecraft.world.scores.Objective");
        Object scoreboard = scoreboardType.getDeclaredConstructor().newInstance();
        Object criteria = staticField("net.minecraft.world.scores.criteria.ObjectiveCriteria", "DUMMY");
        Object renderType = enumConstant(
                "net.minecraft.world.scores.criteria.ObjectiveCriteria$RenderType", "INTEGER");
        Object displayName = component("");

        for (Constructor<?> candidate : objectiveType.getDeclaredConstructors()) {
            Object[] arguments = objectiveArguments(
                    candidate.getParameterTypes(), scoreboard, criteria, renderType, displayName);
            if (arguments != null) {
                candidate.setAccessible(true);
                return candidate.newInstance(arguments);
            }
        }
        throw new NoSuchMethodException(objectiveType.getName());
    }

    private Object[] objectiveArguments(
            Class<?>[] parameters, Object scoreboard, Object criteria, Object renderType, Object displayName) {
        Object[] arguments = new Object[parameters.length];
        boolean named = false;
        for (int index = 0; index < parameters.length; index++) {
            Class<?> parameter = parameters[index];
            if (parameter == String.class) {
                arguments[index] = objectiveName;
                named = true;
            } else if (parameter.isAssignableFrom(scoreboard.getClass())) {
                arguments[index] = scoreboard;
            } else if (criteria != null && parameter.isAssignableFrom(criteria.getClass())) {
                arguments[index] = criteria;
            } else if (renderType != null && parameter.isAssignableFrom(renderType.getClass())) {
                arguments[index] = renderType;
            } else if (parameter.isAssignableFrom(displayName.getClass())) {
                arguments[index] = displayName;
            } else if (parameter == boolean.class || parameter == Boolean.class) {
                arguments[index] = false;
            } else if (parameter == Optional.class) {
                arguments[index] = Optional.empty();
            } else if (parameter.isPrimitive()) {
                return null;
            } else {
                arguments[index] = null;
            }
        }
        return named ? arguments : null;
    }

    // ── Small reflective helpers ───────────────────────────────────────────────

    /** Turns legacy colour codes into whatever this server's chat component class is. */
    private Object component(String legacyText) throws ReflectiveOperationException {
        String text = legacyText == null ? "" : legacyText;
        Class<?> chatMessage = optionalClass(
                plugin.getServer().getClass().getPackage().getName() + ".util.CraftChatMessage",
                "org.bukkit.craftbukkit.util.CraftChatMessage");
        if (chatMessage != null) {
            for (Method method : chatMessage.getDeclaredMethods()) {
                if (!method.getName().equals("fromStringOrNull") || method.getParameterCount() != 1) {
                    continue;
                }
                method.setAccessible(true);
                Object component = method.invoke(null, text);
                if (component != null) {
                    return component;
                }
            }
        }
        Class<?> componentType = loadClass(
                "net.minecraft.network.chat.Component", "net.minecraft.network.chat.IChatBaseComponent");
        return componentType.getMethod("literal", String.class).invoke(null, text);
    }

    private Object playerConnection(Object handle) throws ReflectiveOperationException {
        for (Class<?> type = handle.getClass(); type != null; type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
                if (!field.getType().getName().startsWith("net.minecraft.server.network.")) {
                    continue;
                }
                field.setAccessible(true);
                Object value = field.get(handle);
                if (value != null && findSenderOrNull(value.getClass()) != null) {
                    return value;
                }
            }
        }
        throw new NoSuchFieldException("player connection");
    }

    private Method findSender(Class<?> type, Object packet) throws ReflectiveOperationException {
        Method sender = findSenderOrNull(type);
        if (sender == null || !sender.getParameterTypes()[0].isAssignableFrom(packet.getClass())) {
            throw new NoSuchMethodException("send");
        }
        return sender;
    }

    private Method findSenderOrNull(Class<?> type) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            for (Method method : current.getDeclaredMethods()) {
                String name = method.getName();
                if (method.getParameterCount() != 1 || (!name.equals("send") && !name.equals("sendPacket"))) {
                    continue;
                }
                if (method.getParameterTypes()[0].getName().endsWith("Packet")) {
                    method.setAccessible(true);
                    return method;
                }
            }
        }
        return null;
    }

    private Object invoke(Object target, String name) throws ReflectiveOperationException {
        for (Class<?> type = target.getClass(); type != null; type = type.getSuperclass()) {
            for (Method method : type.getDeclaredMethods()) {
                if (method.getParameterCount() == 0 && method.getName().equals(name)) {
                    method.setAccessible(true);
                    return method.invoke(target);
                }
            }
        }
        throw new NoSuchMethodException(name);
    }

    private Object staticField(String className, String name) throws ReflectiveOperationException {
        Class<?> type = optionalClass(className);
        if (type == null) {
            return null;
        }
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(null);
    }

    private Object enumConstant(String className, String name) {
        Class<?> type = optionalClass(className);
        if (type == null || !type.isEnum()) {
            return null;
        }
        for (Object constant : type.getEnumConstants()) {
            if (constant instanceof Enum<?> value && value.name().equalsIgnoreCase(name)) {
                return constant;
            }
        }
        return null;
    }

    private Class<?> loadClass(String... names) throws ClassNotFoundException {
        Class<?> type = optionalClass(names);
        if (type == null) {
            throw new ClassNotFoundException(String.join(", ", names));
        }
        return type;
    }

    private Class<?> optionalClass(String... names) {
        ClassLoader loader = plugin.getServer().getClass().getClassLoader();
        for (String name : names) {
            try {
                return Class.forName(name, false, loader);
            } catch (ClassNotFoundException | LinkageError ignored) {
                // Tried again under the next name this server might use.
            }
        }
        return null;
    }

    @FunctionalInterface
    private interface PacketSupplier {
        Object get() throws ReflectiveOperationException;
    }

    @FunctionalInterface
    private interface ParameterTest {
        boolean matches(Class<?>[] parameters);
    }
}
