package com.bx.ultimateDonutSmp.utils;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class PacketSidebarRenderer {

    private static final int MAX_LINES = 15;
    private static final String OBJECTIVE_PREFIX = "uds";
    private static final String[] ENTRIES = new String[MAX_LINES];

    static {
        String[] codes = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e"};
        for (int i = 0; i < MAX_LINES; i++) {
            ENTRIES[i] = "\u00A7" + codes[i] + "\u00A7r";
        }
    }

    private final UltimateDonutSmp plugin;
    private final Map<UUID, SidebarState> states = new HashMap<>();

    private boolean disabled;
    private boolean warned;
    private ClassLoader classLoader;
    private Class<?> setObjectivePacketClass;
    private Class<?> displayObjectivePacketClass;
    private Class<?> setScorePacketClass;
    private Class<?> resetScorePacketClass;
    private Class<?> setPlayerTeamPacketClass;
    private Class<?> componentClass;
    private Class<?> renderTypeClass;
    private Class<?> displaySlotClass;
    private Class<?> scoreboardClass;
    private Class<?> objectiveClass;
    private Class<?> playerTeamClass;
    private Class<?> objectiveCriteriaClass;
    private Object integerRenderType;
    private Object sidebarDisplaySlot;
    private Object dummyCriteria;
    private Object blankFormat;

    public PacketSidebarRenderer(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public void show(Player player, String title, List<String> lines) {
        if (disabled || player == null || !player.isOnline()) {
            return;
        }

        try {
            ensureInitialized();
            SidebarState state = states.get(player.getUniqueId());
            if (state == null) {
                state = new SidebarState(objectiveName(player.getUniqueId()));
                sendQuietly(player, createObjectivePacket(state.objectiveName, title, 1));
                for (int i = 0; i < MAX_LINES; i++) {
                    sendQuietly(player, createRemoveTeamPacket(state.objectiveName, i));
                }
                sendPacket(player, createObjectivePacket(state.objectiveName, title, 0));
                sendPacket(player, createDisplayObjectivePacket(state.objectiveName));
                state.title = title;
                state.created = true;
                states.put(player.getUniqueId(), state);
            }

            if (!state.created) {
                sendPacket(player, createObjectivePacket(state.objectiveName, title, 0));
                sendPacket(player, createDisplayObjectivePacket(state.objectiveName));
                state.created = true;
            }

            if (!title.equals(state.title)) {
                sendPacket(player, createObjectivePacket(state.objectiveName, title, 2));
                state.title = title;
            }

            List<String> normalized = normalizeLines(lines);
            for (int i = 0; i < normalized.size(); i++) {
                String line = normalized.get(i);
                if (i >= state.lines.size() || !line.equals(state.lines.get(i))) {
                    sendPacket(player, createTeamPacket(state.objectiveName, i, ENTRIES[i], line, i >= state.lines.size()));
                }
                sendPacket(player, createSetScorePacket(ENTRIES[i], state.objectiveName, normalized.size() - i));
            }

            for (int i = normalized.size(); i < state.lines.size(); i++) {
                Object resetPacket = createResetScorePacket(ENTRIES[i], state.objectiveName);
                if (resetPacket != null) {
                    sendPacket(player, resetPacket);
                }
                sendPacket(player, createRemoveTeamPacket(state.objectiveName, i));
            }

            state.lines.clear();
            state.lines.addAll(normalized);
        } catch (ReflectiveOperationException | RuntimeException exception) {
            disableWithwarning(exception);
        }
    }

    public void hide(Player player) {
        if (player == null) {
            return;
        }

        SidebarState state = states.remove(player.getUniqueId());
        if (disabled || state == null || !player.isOnline()) {
            return;
        }

        try {
            ensureInitialized();
            for (int i = 0; i < state.lines.size(); i++) {
                sendPacket(player, createRemoveTeamPacket(state.objectiveName, i));
            }
            sendPacket(player, createObjectivePacket(state.objectiveName, state.title, 1));
        } catch (ReflectiveOperationException | RuntimeException exception) {
            disableWithwarning(exception);
        }
    }

    public void remove(UUID uuid) {
        if (uuid == null) {
            return;
        }

        Player player = Bukkit.getPlayer(uuid);
        if (player != null && player.isOnline()) {
            hide(player);
            return;
        }

        states.remove(uuid);
    }

    private List<String> normalizeLines(List<String> lines) {
        List<String> normalized = new ArrayList<>();
        if (lines == null) {
            return normalized;
        }

        for (String line : lines) {
            if (normalized.size() >= MAX_LINES) {
                break;
            }
            normalized.add(line == null ? "" : line);
        }
        return normalized;
    }

    private void ensureInitialized() throws ReflectiveOperationException {
        if (setObjectivePacketClass != null) {
            return;
        }

        classLoader = plugin.getServer().getClass().getClassLoader();
        setObjectivePacketClass = loadClass(
                "net.minecraft.network.protocol.game.ClientboundSetObjectivePacket",
                "net.minecraft.network.protocol.game.PacketPlayOutScoreboardObjective"
        );
        displayObjectivePacketClass = loadClass(
                "net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket",
                "net.minecraft.network.protocol.game.PacketPlayOutScoreboardDisplayObjective"
        );
        setScorePacketClass = loadClass(
                "net.minecraft.network.protocol.game.ClientboundSetScorePacket",
                "net.minecraft.network.protocol.game.PacketPlayOutScoreboardScore"
        );
        resetScorePacketClass = loadOptionalClass(
                "net.minecraft.network.protocol.game.ClientboundResetScorePacket"
        );
        setPlayerTeamPacketClass = loadClass(
                "net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket",
                "net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam"
        );
        componentClass = loadClass(
                "net.minecraft.network.chat.Component",
                "net.minecraft.network.chat.IChatBaseComponent"
        );
        renderTypeClass = loadOptionalClass("net.minecraft.world.scores.criteria.ObjectiveCriteria$RenderType");
        displaySlotClass = loadOptionalClass("net.minecraft.world.scores.DisplaySlot");
        scoreboardClass = loadOptionalClass("net.minecraft.world.scores.Scoreboard");
        objectiveClass = loadOptionalClass("net.minecraft.world.scores.Objective");
        playerTeamClass = loadOptionalClass("net.minecraft.world.scores.PlayerTeam");
        objectiveCriteriaClass = loadOptionalClass("net.minecraft.world.scores.criteria.ObjectiveCriteria");
        integerRenderType = readEnumConstant(renderTypeClass, "INTEGER");
        sidebarDisplaySlot = readEnumConstant(displaySlotClass, "SIDEBAR");
        dummyCriteria = readStaticObject(objectiveCriteriaClass, "DUMMY");
        blankFormat = findBlankFormat();
    }

    private Object createObjectivePacket(String objectiveName, String title, int mode) throws ReflectiveOperationException {
        Object component = component(title);
        Optional<?> numberFormat = optionalBlankFormat();
        Object objective = null;

        for (Constructor<?> constructor : setObjectivePacketClass.getDeclaredConstructors()) {
            Class<?>[] parameters = constructor.getParameterTypes();
            Object[] args = new Object[parameters.length];
            boolean seenName = false;
            boolean seenObjective = false;
            boolean seenMode = false;
            boolean supported = true;

            for (int i = 0; i < parameters.length; i++) {
                Class<?> parameter = parameters[i];
                if (parameter == String.class) {
                    args[i] = objectiveName;
                    seenName = true;
                } else if (parameter == int.class || parameter == Integer.class) {
                    args[i] = mode;
                    seenMode = true;
                } else if (acceptsObjective(parameter)) {
                    if (objective == null) {
                        objective = createNmsObjective(objectiveName, title);
                    }
                    args[i] = objective;
                    seenObjective = true;
                } else if (acceptsComponent(parameter, component)) {
                    args[i] = component;
                } else if (acceptsValue(parameter, integerRenderType)) {
                    args[i] = integerRenderType;
                } else if (parameter == Optional.class) {
                    args[i] = numberFormat;
                } else {
                    supported = false;
                    break;
                }
            }

            if (supported && (seenName || seenObjective) && seenMode) {
                constructor.setAccessible(true);
                return constructor.newInstance(args);
            }
        }

        throw new NoSuchMethodException(setObjectivePacketClass.getName() + " supported constructor");
    }

    private Object createDisplayObjectivePacket(String objectiveName) throws ReflectiveOperationException {
        Object objective = null;
        for (Constructor<?> constructor : displayObjectivePacketClass.getDeclaredConstructors()) {
            Class<?>[] parameters = constructor.getParameterTypes();
            Object[] args = new Object[parameters.length];
            boolean seenName = false;
            boolean seenObjective = false;
            boolean seenSlot = false;
            boolean supported = true;

            for (int i = 0; i < parameters.length; i++) {
                Class<?> parameter = parameters[i];
                if (parameter == String.class) {
                    args[i] = objectiveName;
                    seenName = true;
                } else if ((parameter == int.class || parameter == Integer.class)) {
                    args[i] = 1;
                    seenSlot = true;
                } else if (acceptsObjective(parameter)) {
                    if (objective == null) {
                        objective = createNmsObjective(objectiveName, "");
                    }
                    args[i] = objective;
                    seenObjective = true;
                } else if (acceptsValue(parameter, sidebarDisplaySlot)) {
                    args[i] = sidebarDisplaySlot;
                    seenSlot = true;
                } else {
                    supported = false;
                    break;
                }
            }

            if (supported && (seenName || seenObjective) && seenSlot) {
                constructor.setAccessible(true);
                return constructor.newInstance(args);
            }
        }

        throw new NoSuchMethodException(displayObjectivePacketClass.getName() + " constructor");
    }

    private Object createNmsObjective(String objectiveName, String title) throws ReflectiveOperationException {
        if (scoreboardClass == null || objectiveClass == null || dummyCriteria == null) {
            throw new NoSuchMethodException("NMS scoreboard objective classes");
        }

        Object scoreboard = scoreboardClass.getDeclaredConstructor().newInstance();
        Object component = component(title);

        Object objective = createObjectiveViaScoreboard(scoreboard, objectiveName, component);
        if (objective != null) {
            return objective;
        }

        for (Constructor<?> constructor : objectiveClass.getDeclaredConstructors()) {
            Object[] args = createObjectiveArgs(
                    constructor.getParameterTypes(),
                    scoreboard,
                    objectiveName,
                    component
            );
            if (args == null) {
                continue;
            }

            constructor.setAccessible(true);
            return constructor.newInstance(args);
        }

        throw new NoSuchMethodException(objectiveClass.getName() + " constructor");
    }

    private Object createObjectiveViaScoreboard(Object scoreboard, String objectiveName, Object component)
            throws ReflectiveOperationException {
        for (Method method : scoreboardClass.getDeclaredMethods()) {
            if (!method.getName().equals("addObjective") || !objectiveClass.isAssignableFrom(method.getReturnType())) {
                continue;
            }

            Object[] args = createObjectiveArgs(
                    method.getParameterTypes(),
                    scoreboard,
                    objectiveName,
                    component
            );
            if (args == null) {
                continue;
            }

            method.setAccessible(true);
            return method.invoke(scoreboard, args);
        }

        return null;
    }

    private Object[] createObjectiveArgs(
            Class<?>[] parameters,
            Object scoreboard,
            String objectiveName,
            Object component
    ) {
        Object[] args = new Object[parameters.length];
        boolean seenName = false;
        boolean seenCriteria = false;
        boolean seenComponent = false;
        boolean seenRenderType = false;

        for (int i = 0; i < parameters.length; i++) {
            Class<?> parameter = parameters[i];
            if (acceptsValue(parameter, scoreboard)) {
                args[i] = scoreboard;
            } else if (parameter == String.class) {
                args[i] = objectiveName;
                seenName = true;
            } else if (acceptsValue(parameter, dummyCriteria)) {
                args[i] = dummyCriteria;
                seenCriteria = true;
            } else if (acceptsComponent(parameter, component)) {
                args[i] = component;
                seenComponent = true;
            } else if (acceptsValue(parameter, integerRenderType)) {
                args[i] = integerRenderType;
                seenRenderType = true;
            } else if (parameter == boolean.class || parameter == Boolean.class) {
                args[i] = false;
            } else if (parameter == Optional.class) {
                args[i] = optionalBlankFormat();
            } else if (acceptsValue(parameter, blankFormat) || isNumberFormat(parameter)) {
                args[i] = blankFormat;
            } else {
                return null;
            }
        }

        return seenName && seenCriteria && seenComponent && seenRenderType ? args : null;
    }

    private Object createTeamPacket(String objectiveName, int lineIndex, String entry, String text, boolean add)
            throws ReflectiveOperationException {
        Object team = createNmsPlayerTeam(teamName(objectiveName, lineIndex), entry, text);
        for (Method method : setPlayerTeamPacketClass.getDeclaredMethods()) {
            if (!Modifier.isStatic(method.getModifiers())
                    || !method.getName().equals("createAddOrModifyPacket")
                    || method.getParameterCount() != 2) {
                continue;
            }

            Class<?>[] parameters = method.getParameterTypes();
            if (!parameters[0].isAssignableFrom(playerTeamClass)
                    || (parameters[1] != boolean.class && parameters[1] != Boolean.class)) {
                continue;
            }

            method.setAccessible(true);
            return method.invoke(null, team, add);
        }

        throw new NoSuchMethodException(setPlayerTeamPacketClass.getName() + " createAddOrModifyPacket");
    }

    private Object createRemoveTeamPacket(String objectiveName, int lineIndex) throws ReflectiveOperationException {
        Object team = createNmsPlayerTeam(teamName(objectiveName, lineIndex), ENTRIES[lineIndex], "");
        for (Method method : setPlayerTeamPacketClass.getDeclaredMethods()) {
            if (!Modifier.isStatic(method.getModifiers())
                    || !method.getName().equals("createRemovePacket")
                    || method.getParameterCount() != 1) {
                continue;
            }

            if (!method.getParameterTypes()[0].isAssignableFrom(playerTeamClass)) {
                continue;
            }

            method.setAccessible(true);
            return method.invoke(null, team);
        }

        throw new NoSuchMethodException(setPlayerTeamPacketClass.getName() + " createRemovePacket");
    }

    @SuppressWarnings("unchecked")
    private Object createNmsPlayerTeam(String teamName, String entry, String text) throws ReflectiveOperationException {
        if (scoreboardClass == null || playerTeamClass == null) {
            throw new NoSuchMethodException("NMS player team classes");
        }

        Object scoreboard = scoreboardClass.getDeclaredConstructor().newInstance();
        Constructor<?> constructor = playerTeamClass.getDeclaredConstructor(scoreboardClass, String.class);
        constructor.setAccessible(true);
        Object team = constructor.newInstance(scoreboard, teamName);

        invokeOneArg(team, "setDisplayName", component(teamName));
        invokeOneArg(team, "setPlayerPrefix", component(text));
        invokeOneArg(team, "setPlayerSuffix", component(""));

        Method getPlayers = findNoArgMethod(playerTeamClass, "getPlayers");
        if (getPlayers != null) {
            getPlayers.setAccessible(true);
            Object players = getPlayers.invoke(team);
            if (players instanceof java.util.Collection<?> collection) {
                ((java.util.Collection<String>) collection).add(entry);
            }
        }

        return team;
    }

    private Object createSetScorePacket(String entry, String objectiveName, int score)
            throws ReflectiveOperationException {
        Optional<?> componentOptional = Optional.empty();
        Optional<?> numberFormat = optionalBlankFormat();

        for (Constructor<?> constructor : setScorePacketClass.getDeclaredConstructors()) {
            Class<?>[] parameters = constructor.getParameterTypes();
            Object[] args = new Object[parameters.length];
            int stringIndex = 0;
            int optionalIndex = 0;
            boolean seenScore = false;
            boolean supported = true;

            for (int i = 0; i < parameters.length; i++) {
                Class<?> parameter = parameters[i];
                if (parameter == String.class) {
                    args[i] = stringIndex++ == 0 ? entry : objectiveName;
                } else if (parameter == int.class || parameter == Integer.class) {
                    args[i] = score;
                    seenScore = true;
                } else if (parameter == Optional.class) {
                    args[i] = optionalIndex++ == 0 ? componentOptional : numberFormat;
                } else {
                    supported = false;
                    break;
                }
            }

            if (supported && stringIndex >= 2 && seenScore && optionalIndex >= 1) {
                constructor.setAccessible(true);
                return constructor.newInstance(args);
            }
        }

        throw new NoSuchMethodException(setScorePacketClass.getName() + " supported display-name constructor");
    }

    private Object createResetScorePacket(String entry, String objectiveName) throws ReflectiveOperationException {
        if (resetScorePacketClass == null) {
            return null;
        }

        for (Constructor<?> constructor : resetScorePacketClass.getDeclaredConstructors()) {
            Class<?>[] parameters = constructor.getParameterTypes();
            Object[] args = new Object[parameters.length];
            int stringIndex = 0;
            boolean supported = true;

            for (int i = 0; i < parameters.length; i++) {
                Class<?> parameter = parameters[i];
                if (parameter == String.class) {
                    args[i] = stringIndex++ == 0 ? entry : objectiveName;
                } else if (parameter == Optional.class) {
                    args[i] = Optional.of(objectiveName);
                } else {
                    supported = false;
                    break;
                }
            }

            if (supported && stringIndex >= 1) {
                constructor.setAccessible(true);
                return constructor.newInstance(args);
            }
        }

        return null;
    }

    private Object component(String legacyText) throws ReflectiveOperationException {
        String text = legacyText == null ? "" : legacyText;
        Class<?> craftChatMessage = loadOptionalClass("org.bukkit.craftbukkit.util.CraftChatMessage");
        if (craftChatMessage != null) {
            for (Method method : craftChatMessage.getDeclaredMethods()) {
                if (!Modifier.isStatic(method.getModifiers()) || !method.getName().equals("fromStringOrNull")) {
                    continue;
                }
                Class<?>[] parameters = method.getParameterTypes();
                if (parameters.length == 1 && parameters[0] == String.class) {
                    method.setAccessible(true);
                    Object component = method.invoke(null, text);
                    if (component != null) {
                        return component;
                    }
                }
                if (parameters.length == 2 && parameters[0] == String.class && parameters[1] == boolean.class) {
                    method.setAccessible(true);
                    Object component = method.invoke(null, text, true);
                    if (component != null) {
                        return component;
                    }
                }
            }
        }

        Method literal = componentClass.getMethod("literal", String.class);
        return literal.invoke(null, text);
    }

    private Optional<?> optionalBlankFormat() {
        return blankFormat == null ? Optional.empty() : Optional.of(blankFormat);
    }

    private Object findBlankFormat() {
        Class<?> type = loadOptionalClass("net.minecraft.network.chat.numbers.BlankFormat");
        if (type == null) {
            return null;
        }

        try {
            Object singleton = readSingleton(type);
            if (singleton != null) {
                return singleton;
            }
        } catch (ReflectiveOperationException ignored) {
        }
        return null;
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

        Constructor<?> constructor = type.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    private Object readStaticObject(Class<?> type, String name) throws ReflectiveOperationException {
        if (type == null) {
            return null;
        }

        for (Field field : type.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) || !field.getName().equals(name)) {
                continue;
            }
            field.setAccessible(true);
            return field.get(null);
        }
        return null;
    }

    private Object readEnumConstant(Class<?> enumType, String name) {
        if (enumType == null || !enumType.isEnum()) {
            return null;
        }

        for (Object constant : enumType.getEnumConstants()) {
            if (constant instanceof Enum<?> enumConstant && enumConstant.name().equalsIgnoreCase(name)) {
                return constant;
            }
        }
        return enumType.getEnumConstants().length == 0 ? null : enumType.getEnumConstants()[0];
    }

    private boolean acceptsComponent(Class<?> parameter, Object component) {
        return component != null
                && (parameter.isAssignableFrom(component.getClass())
                || parameter.getName().equals("net.minecraft.network.chat.Component")
                || parameter.getName().equals("net.minecraft.network.chat.IChatBaseComponent"));
    }

    private boolean acceptsObjective(Class<?> parameter) {
        return objectiveClass != null && parameter.isAssignableFrom(objectiveClass);
    }

    private boolean acceptsValue(Class<?> parameter, Object value) {
        return value != null && parameter.isAssignableFrom(value.getClass());
    }

    private boolean isNumberFormat(Class<?> parameter) {
        return "net.minecraft.network.chat.numbers.NumberFormat".equals(parameter.getName());
    }

    private void sendQuietly(Player player, Object packet) {
        try {
            sendPacket(player, packet);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
        }
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
        Object connection = findPlayerConnection(root);
        if (connection != null) {
            PacketSender sender = findNamedSenderOn(connection, packet);
            if (sender != null) {
                return sender;
            }
        }

        for (Class<?> current = root.getClass(); current != null; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                field.setAccessible(true);
                Object value = field.get(root);
                if (value == null) {
                    continue;
                }
                PacketSender sender = findNamedSenderOn(value, packet);
                if (sender != null) {
                    return sender;
                }
            }
        }

        return findNamedSenderOn(root, packet);
    }

    private Object findPlayerConnection(Object handle) throws ReflectiveOperationException {
        Field namedConnection = findField(handle.getClass(), "connection");
        if (namedConnection != null) {
            namedConnection.setAccessible(true);
            Object value = namedConnection.get(handle);
            if (value != null) {
                return value;
            }
        }

        for (Class<?> current = handle.getClass(); current != null; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                if (!field.getType().getName().startsWith("net.minecraft.server.network.")) {
                    continue;
                }
                field.setAccessible(true);
                Object value = field.get(handle);
                if (value != null && value.getClass().getSimpleName().contains("PacketListener")) {
                    return value;
                }
            }
        }

        return null;
    }

    private Field findField(Class<?> type, String name) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
            }
        }
        return null;
    }

    private PacketSender findNamedSenderOn(Object target, Object packet) {
        PacketSender twoArgumentSend = null;
        PacketSender legacySend = null;

        for (Class<?> current = target.getClass(); current != null; current = current.getSuperclass()) {
            for (Method method : current.getDeclaredMethods()) {
                if (!isSendMethodName(method)) {
                    continue;
                }

                Class<?>[] parameters = method.getParameterTypes();
                if (parameters.length == 1 && acceptsPacket(parameters[0], packet)) {
                    return new PacketSender(target, method, false);
                }
                if (parameters.length == 2 && acceptsPacket(parameters[0], packet)) {
                    PacketSender sender = new PacketSender(target, method, true);
                    if ("send".equalsIgnoreCase(method.getName())) {
                        twoArgumentSend = sender;
                    } else {
                        legacySend = sender;
                    }
                }
            }
        }

        return twoArgumentSend != null ? twoArgumentSend : legacySend;
    }

    private boolean acceptsPacket(Class<?> parameterType, Object packet) {
        return parameterType.isAssignableFrom(packet.getClass())
                || "net.minecraft.network.protocol.Packet".equals(parameterType.getName())
                || "Packet".equals(parameterType.getSimpleName());
    }

    private boolean isSendMethodName(Method method) {
        String name = method.getName().toLowerCase();
        return "send".equals(name) || "sendpacket".equals(name);
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

    private void invokeOneArg(Object target, String name, Object argument) throws ReflectiveOperationException {
        for (Class<?> current = target.getClass(); current != null; current = current.getSuperclass()) {
            for (Method method : current.getDeclaredMethods()) {
                if (!method.getName().equals(name) || method.getParameterCount() != 1) {
                    continue;
                }
                Class<?> parameter = method.getParameterTypes()[0];
                if (!parameter.isAssignableFrom(argument.getClass())) {
                    continue;
                }
                method.setAccessible(true);
                method.invoke(target, argument);
                return;
            }
        }

        throw new NoSuchMethodException(target.getClass().getName() + "#" + name);
    }

    private Class<?> loadClass(String... names) throws ClassNotFoundException {
        Class<?> type = loadOptionalClass(names);
        if (type != null) {
            return type;
        }
        throw new ClassNotFoundException(String.join(", ", names));
    }

    private Class<?> loadOptionalClass(String... names) {
        ClassLoader loader = classLoader == null ? plugin.getServer().getClass().getClassLoader() : classLoader;
        for (String name : names) {
            try {
                return Class.forName(name, false, loader);
            } catch (ClassNotFoundException ignored) {
            }
        }
        return null;
    }

    private String objectiveName(UUID uuid) {
        return OBJECTIVE_PREFIX + uuid.toString().replace("-", "").substring(0, 12);
    }

    private String teamName(String objectiveName, int index) {
        String base = objectiveName.length() > 12 ? objectiveName.substring(0, 12) : objectiveName;
        return base + Integer.toHexString(index);
    }

    private void disableWithwarning(Exception exception) {
        disabled = true;
        states.clear();
        if (warned) {
            return;
        }
        warned = true;
        plugin.getLogger().warning("Folia packet sidebar is unavailable on this server build: "
                + exception.getClass().getSimpleName() + ": " + exception.getMessage());
    }

    private static final class SidebarState {
        private final String objectiveName;
        private final List<String> lines = new ArrayList<>();
        private String title = "";
        private boolean created;

        private SidebarState(String objectiveName) {
            this.objectiveName = objectiveName;
        }
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
                return;
            }
            method.invoke(target, packet);
        }
    }
}
