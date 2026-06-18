package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.HideMode;
import com.bx.ultimateDonutSmp.models.HideState;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedRemoteChatSessionData;
import com.comphenix.protocol.wrappers.WrappedTeamParameters;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

final class HideProtocolLibBridge implements HidePacketBridge {

    private final UltimateDonutSmp plugin;
    private final HideManager hideManager;
    private final ProtocolManager protocolManager;
    private final Map<UUID, UUID> nametagDisplays = new ConcurrentHashMap<>();
    private PacketListener playerInfoListener;

    HideProtocolLibBridge(UltimateDonutSmp plugin, HideManager hideManager) {
        this.plugin = plugin;
        this.hideManager = hideManager;
        this.protocolManager = ProtocolLibrary.getProtocolManager();
        registerPlayerInfoListener();
    }

    private void registerPlayerInfoListener() {
        playerInfoListener = new PacketAdapter(
                plugin,
                ListenerPriority.MONITOR,
                PacketType.Play.Server.PLAYER_INFO,
                PacketType.Play.Server.SCOREBOARD_TEAM
        ) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if (event.getPacketType() == PacketType.Play.Server.SCOREBOARD_TEAM) {
                    rewriteTeamEntries(event);
                    return;
                }
                Player viewer = event.getPlayer();
                if (viewer == null || event.getPacket().getPlayerInfoDataLists().size() == 0) {
                    return;
                }

                try {
                    PlayerInfoListField playerInfoField = readPlayerInfoData(event.getPacket());
                    List<PlayerInfoData> source = playerInfoField == null ? null : playerInfoField.data();
                    if (source == null || source.isEmpty() || source.stream().anyMatch(data -> data == null)) {
                        return;
                    }

                    boolean changed = false;
                    Set<UUID> obfuscatedProfiles = new HashSet<>();
                    List<PlayerInfoData> rewritten = new ArrayList<>(source.size());
                    for (PlayerInfoData data : source) {
                        PlayerInfoData replacement = rewrite(viewer, data);
                        rewritten.add(replacement);
                        changed |= replacement != data;
                        HideState state = hideManager.getState(data.getProfileId());
                        if (shouldObfuscateFor(viewer, state)) {
                            obfuscatedProfiles.add(state.playerUuid());
                        }
                    }
                    if (changed) {
                        event.getPacket().getPlayerInfoDataLists().writeSafely(playerInfoField.index(), rewritten);
                    }
                    if (addsPlayers(event.getPacket())) {
                        enforceNametagTeamsLater(viewer, obfuscatedProfiles);
                    }
                } catch (RuntimeException error) {
                    plugin.getLogger().log(Level.FINE,
                            "Unable to rewrite player-info packet for Hide; leaving the packet unchanged.", error);
                }
            }
        };
        protocolManager.addPacketListener(playerInfoListener);
    }

    private PlayerInfoListField readPlayerInfoData(PacketContainer packet) {
        StructureModifier<List<PlayerInfoData>> modifier = packet.getPlayerInfoDataLists();
        for (int index = 0; index < modifier.size(); index++) {
            List<PlayerInfoData> data = modifier.readSafely(index);
            if (data != null) {
                return new PlayerInfoListField(index, data);
            }
        }
        return null;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void rewriteTeamEntries(PacketEvent event) {
        Player viewer = event.getPlayer();
        if (viewer == null || hideManager.canSeeRealIdentity(viewer)) {
            return;
        }
        Set<UUID> obfuscatedProfiles = new HashSet<>();
        try {
            var collections = event.getPacket().getSpecificModifier(Collection.class);
            for (int index = 0; index < collections.size(); index++) {
                Collection<?> source = collections.readSafely(index);
                if (source == null || source.isEmpty()
                        || source.stream().anyMatch(value -> !(value instanceof String))) {
                    continue;
                }
                boolean changed = false;
                List<String> rewritten = new ArrayList<>(source.size());
                for (Object value : source) {
                    String entry = (String) value;
                    HideState state = hideManager.findState(entry);
                    if (hideManager.usesObfuscatedText(state)) {
                        obfuscatedProfiles.add(state.playerUuid());
                    }
                    if (state != null && state.realNameSnapshot().equalsIgnoreCase(entry)) {
                        rewritten.add(state.alias());
                        changed = true;
                    } else {
                        rewritten.add(entry);
                    }
                }
                if (changed) {
                    collections.writeSafely(index, rewritten);
                }
            }
        } catch (RuntimeException error) {
            plugin.getLogger().log(Level.FINE, "Unable to rewrite scoreboard team entries for Hide.", error);
        }
        enforceNametagTeamsLater(viewer, obfuscatedProfiles);
    }

    private PlayerInfoData rewrite(Player viewer, PlayerInfoData data) {
        if (data == null) {
            return null;
        }
        UUID profileId = data.getProfileId();
        if (profileId == null) {
            return data;
        }
        HideState state = hideManager.getState(profileId);
        if (state == null) {
            return data;
        }

        boolean bypass = !profileId.equals(viewer.getUniqueId())
                && hideManager.canSeeRealIdentity(viewer);
        String profileName = bypass ? state.realNameSnapshot() : state.alias();
        HideManager.HeadTexture texture = profileTexture(state, profileId, bypass);
        WrappedGameProfile profile = NativeGameProfileFactory.create(
                profileId,
                profileName,
                data.getProfile(),
                texture == null ? null : texture.value(),
                texture == null ? null : texture.signature()
        );

        WrappedChatComponent displayName = data.getDisplayName();
        if (bypass) {
            displayName = WrappedChatComponent.fromLegacyText(
                    ColorUtils.colorize(hideManager.staffMarker() + state.realNameSnapshot())
            );
        } else if (hideManager.usesObfuscatedText(state) && displayName == null) {
            displayName = WrappedChatComponent.fromLegacyText(
                    ColorUtils.colorize(hideManager.publicName(state))
            );
        }

        return new PlayerInfoData(
                profileId,
                data.getLatency(),
                data.isListed(),
                data.getGameMode(),
                profile,
                displayName,
                data.getRemoteChatSessionData()
        );
    }

    @Override
    public void refresh(Player target) {
        if (target == null || !target.isOnline()) {
            return;
        }

        plugin.getSpigotScheduler().runEntity(target, () -> syncNametagDisplay(target));
        List<Player> viewersToRetrack = new ArrayList<>();
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            plugin.getSpigotScheduler().runEntity(viewer, () -> {
                if (!viewer.isOnline() || !target.isOnline()) {
                    return;
                }
                removeNametagTeam(viewer, target.getUniqueId());
                if (viewer.getUniqueId().equals(target.getUniqueId())) {
                    refreshPlayerInfo(viewer, target);
                    return;
                }
                viewer.hidePlayer(plugin, target);
                send(viewer, createPlayerInfoRemove(target));
                plugin.getSpigotScheduler().runEntityLater(viewer, () -> {
                    if (viewer.isOnline() && target.isOnline()) {
                        send(viewer, createPlayerInfoAdd(viewer, target));
                        refreshNametagTeam(viewer, target.getUniqueId());
                        viewer.showPlayer(plugin, target);
                    }
                }, 2L);
            });
            if (!viewer.getUniqueId().equals(target.getUniqueId())) {
                viewersToRetrack.add(viewer);
            }
        }

        if (!viewersToRetrack.isEmpty()) {
            plugin.getSpigotScheduler().runEntityLater(target, () -> {
                if (!target.isOnline()) {
                    return;
                }
                List<Player> onlineViewers = viewersToRetrack.stream()
                        .filter(Player::isOnline)
                        .toList();
                if (!onlineViewers.isEmpty()) {
                    try {
                        protocolManager.updateEntity(target, onlineViewers);
                    } catch (RuntimeException error) {
                        plugin.getLogger().log(Level.FINE,
                                "Unable to retrack disguised player entity after refreshing its profile.", error);
                    }
                }
            }, 4L);
        }

        plugin.getSpigotScheduler().runEntity(target, () -> {
            if (target.isOnline() && plugin.getTablistManager() != null) {
                plugin.getTablistManager().updateTablistName(target);
                plugin.getTablistManager().update(target);
            }
        });
    }

    @Override
    public void refreshNametag(Player target) {
        if (target == null || !target.isOnline()) {
            return;
        }
        plugin.getSpigotScheduler().runEntityLater(target, () -> {
            if (target.isOnline()) {
                syncNametagDisplay(target);
            }
        }, 1L);
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            plugin.getSpigotScheduler().runEntityLater(viewer, () -> {
                if (viewer.isOnline() && target.isOnline()) {
                    refreshNametagTeam(viewer, target.getUniqueId());
                }
            }, 2L);
        }
    }

    private void refreshPlayerInfo(Player viewer, Player target) {
        send(viewer, createPlayerInfoRemove(target));
        plugin.getSpigotScheduler().runEntityLater(viewer, () -> {
            if (viewer.isOnline() && target.isOnline()) {
                send(viewer, createPlayerInfoAdd(viewer, target));
                refreshNametagTeam(viewer, target.getUniqueId());
            }
        }, 2L);
    }

    private boolean addsPlayers(PacketContainer packet) {
        try {
            if (packet.getPlayerInfoActions().size() > 0) {
                Set<EnumWrappers.PlayerInfoAction> actions = packet.getPlayerInfoActions().readSafely(0);
                return actions != null && actions.contains(EnumWrappers.PlayerInfoAction.ADD_PLAYER);
            }
            if (packet.getPlayerInfoAction().size() > 0) {
                return packet.getPlayerInfoAction().readSafely(0) == EnumWrappers.PlayerInfoAction.ADD_PLAYER;
            }
        } catch (RuntimeException ignored) {
        }
        return false;
    }

    private boolean shouldObfuscateFor(Player viewer, HideState state) {
        if (viewer == null || state == null || !hideManager.usesObfuscatedText(state)) {
            return false;
        }
        return state.playerUuid().equals(viewer.getUniqueId())
                || !hideManager.canSeeRealIdentity(viewer);
    }

    private void enforceNametagTeamsLater(Player viewer, Collection<UUID> profileIds) {
        if (viewer == null || profileIds == null || profileIds.isEmpty()) {
            return;
        }
        Set<UUID> profiles = Set.copyOf(profileIds);
        plugin.getSpigotScheduler().runEntity(viewer, () -> {
            if (!viewer.isOnline()) {
                return;
            }
            for (UUID profileId : profiles) {
                refreshNametagTeam(viewer, profileId);
            }
        });
    }

    private void refreshNametagTeam(Player viewer, UUID profileId) {
        try {
            removeNametagTeam(viewer, profileId);
            HideState state = hideManager.getState(profileId);
            TextDisplay display = nametagDisplay(profileId);
            if (!shouldObfuscateFor(viewer, state)) {
                if (display != null) {
                    viewer.hideEntity(plugin, display);
                }
                return;
            }
            if (display != null && !profileId.equals(viewer.getUniqueId())) {
                viewer.showEntity(plugin, display);
            } else if (display != null) {
                viewer.hideEntity(plugin, display);
            }
            send(viewer, createObfuscatedNametagTeam(state));
        } catch (RuntimeException | LinkageError error) {
            plugin.getLogger().log(Level.FINE, "Unable to refresh an obfuscated world nametag.", error);
        }
    }

    private PacketContainer createNametagTeamRemove(UUID profileId) {
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.SCOREBOARD_TEAM);
        packet.getStrings().writeSafely(0, nametagTeamName(profileId));
        packet.getIntegers().writeSafely(0, 1);
        return packet;
    }

    private void removeNametagTeam(Player viewer, UUID profileId) {
        try {
            send(viewer, createNametagTeamRemove(profileId));
        } catch (RuntimeException | LinkageError error) {
            plugin.getLogger().log(Level.FINE, "Unable to remove an obfuscated world nametag team.", error);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private PacketContainer createObfuscatedNametagTeam(HideState state) {
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.SCOREBOARD_TEAM);
        String teamName = nametagTeamName(state.playerUuid());
        packet.getStrings().writeSafely(0, teamName);
        packet.getIntegers().writeSafely(0, 0);

        WrappedTeamParameters parameters = WrappedTeamParameters.newBuilder()
                .displayName(WrappedChatComponent.fromText(teamName))
                .prefix(WrappedChatComponent.fromText(""))
                .suffix(WrappedChatComponent.fromText(""))
                .nametagVisibility("never")
                .collisionRule("always")
                .color(EnumWrappers.ChatFormatting.WHITE)
                .options(0)
                .build();
        packet.getOptionalTeamParameters().writeSafely(0, Optional.of(parameters));

        StructureModifier<Collection> entries = packet.getSpecificModifier(Collection.class);
        entries.writeSafely(0, List.of(state.alias(), state.realNameSnapshot()));
        return packet;
    }

    private String nametagTeamName(UUID profileId) {
        String compactUuid = profileId.toString().replace("-", "");
        return "udsh_" + compactUuid.substring(0, 11);
    }

    private void syncNametagDisplay(Player target) {
        HideState state = hideManager.getState(target.getUniqueId());
        if (state == null || !hideManager.usesObfuscatedText(state)) {
            removeNametagDisplay(target.getUniqueId());
            return;
        }

        TextDisplay display = nametagDisplay(target.getUniqueId());
        if (display != null && !display.getWorld().equals(target.getWorld())) {
            removeNametagDisplay(target.getUniqueId());
            display = null;
        }
        if (display == null) {
            display = target.getWorld().spawn(target.getLocation(), TextDisplay.class, textDisplay -> {
                textDisplay.setBillboard(Display.Billboard.CENTER);
                textDisplay.setDefaultBackground(false);
                textDisplay.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
                textDisplay.setSeeThrough(true);
                textDisplay.setShadowed(true);
                textDisplay.setViewRange(64.0F);
                textDisplay.setLineWidth(200);
                textDisplay.setPersistent(false);
                textDisplay.setInvulnerable(true);
                textDisplay.setGravity(false);
                textDisplay.setVisibleByDefault(true);
            });
            nametagDisplays.put(target.getUniqueId(), display.getUniqueId());
        }

        display.setText(ColorUtils.colorize(hideManager.publicName(state)));
        if (!target.getPassengers().contains(display)) {
            target.addPassenger(display);
        }
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (viewer.getUniqueId().equals(target.getUniqueId())
                    || hideManager.canSeeRealIdentity(viewer)) {
                viewer.hideEntity(plugin, display);
            } else {
                viewer.showEntity(plugin, display);
            }
        }
    }

    private TextDisplay nametagDisplay(UUID targetUuid) {
        UUID displayUuid = nametagDisplays.get(targetUuid);
        if (displayUuid == null) {
            return null;
        }
        Entity entity = Bukkit.getEntity(displayUuid);
        if (entity instanceof TextDisplay display && display.isValid()) {
            return display;
        }
        nametagDisplays.remove(targetUuid, displayUuid);
        return null;
    }

    private void removeNametagDisplay(UUID targetUuid) {
        UUID displayUuid = nametagDisplays.remove(targetUuid);
        if (displayUuid == null) {
            return;
        }
        Entity entity = Bukkit.getEntity(displayUuid);
        if (entity != null) {
            try {
                entity.remove();
            } catch (RuntimeException ignored) {
            }
        }
    }

    private PacketContainer createPlayerInfoRemove(Player target) {
        if (PacketType.Play.Server.PLAYER_INFO_REMOVE.isSupported()) {
            PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.PLAYER_INFO_REMOVE);
            if (packet.getUUIDLists().size() > 0) {
                packet.getUUIDLists().write(0, List.of(target.getUniqueId()));
            } else if (packet.getUUIDs().size() > 0) {
                packet.getUUIDs().write(0, target.getUniqueId());
            }
            return packet;
        }

        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.PLAYER_INFO);
        if (packet.getPlayerInfoActions().size() > 0) {
            packet.getPlayerInfoActions().write(
                    0,
                    EnumSet.of(EnumWrappers.PlayerInfoAction.REMOVE_PLAYER)
            );
        } else if (packet.getPlayerInfoAction().size() > 0) {
            packet.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.REMOVE_PLAYER);
        }
        writePlayerInfoData(packet, List.of(createPlayerInfoData(target, originalProfile(target), null)));
        return packet;
    }

    private PacketContainer createPlayerInfoAdd(Player viewer, Player target) {
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.PLAYER_INFO);
        WrappedRemoteChatSessionData chatSession = remoteChatSession(target);
        if (packet.getPlayerInfoActions().size() > 0) {
            EnumSet<EnumWrappers.PlayerInfoAction> actions = EnumSet.of(
                    EnumWrappers.PlayerInfoAction.ADD_PLAYER,
                    EnumWrappers.PlayerInfoAction.UPDATE_LISTED,
                    EnumWrappers.PlayerInfoAction.UPDATE_GAME_MODE,
                    EnumWrappers.PlayerInfoAction.UPDATE_LATENCY,
                    EnumWrappers.PlayerInfoAction.UPDATE_DISPLAY_NAME
            );
            if (chatSession != null) {
                actions.add(EnumWrappers.PlayerInfoAction.INITIALIZE_CHAT);
            }
            packet.getPlayerInfoActions().write(0, actions);
        } else if (packet.getPlayerInfoAction().size() > 0) {
            packet.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.ADD_PLAYER);
        }

        WrappedGameProfile profile = profileFor(viewer, target);
        writePlayerInfoData(packet, List.of(createPlayerInfoData(target, profile, chatSession)));
        return packet;
    }

    private PlayerInfoData createPlayerInfoData(
            Player target,
            WrappedGameProfile profile,
            WrappedRemoteChatSessionData chatSession
    ) {
        return new PlayerInfoData(
                target.getUniqueId(),
                target.getPing(),
                true,
                EnumWrappers.NativeGameMode.fromBukkit(target.getGameMode()),
                profile,
                null,
                chatSession
        );
    }

    private WrappedRemoteChatSessionData remoteChatSession(Player target) {
        try {
            return WrappedRemoteChatSessionData.fromPlayer(target);
        } catch (RuntimeException | LinkageError ignored) {
            return null;
        }
    }

    private WrappedGameProfile profileFor(Player viewer, Player target) {
        WrappedGameProfile original = originalProfile(target);
        HideState state = hideManager.getState(target.getUniqueId());
        if (state == null) {
            return original;
        }

        boolean bypass = !viewer.getUniqueId().equals(target.getUniqueId())
                && hideManager.canSeeRealIdentity(viewer);
        HideManager.HeadTexture texture = profileTexture(
                state,
                target.getUniqueId(),
                bypass
        );
        return NativeGameProfileFactory.create(
                target.getUniqueId(),
                bypass ? state.realNameSnapshot() : state.alias(),
                original,
                texture == null ? null : texture.value(),
                texture == null ? null : texture.signature()
        );
    }

    private HideManager.HeadTexture profileTexture(
            HideState state,
            UUID profileId,
            boolean bypass
    ) {
        if (state == null) {
            return null;
        }
        if (bypass) {
            HideManager.HeadTexture original = hideManager.originalSkinTexture(profileId);
            if (original != null && original.isValid()) {
                return original;
            }
            if (state.mode() == HideMode.SCRAMBLE && state.hasTexture()) {
                return new HideManager.HeadTexture(
                        state.textureValue(),
                        state.textureSignature()
                );
            }
            return null;
        }
        if (state.hasTexture()) {
            return new HideManager.HeadTexture(
                    state.textureValue(),
                    state.textureSignature()
            );
        }
        return null;
    }

    private WrappedGameProfile originalProfile(Player target) {
        try {
            return WrappedGameProfile.fromPlayer(target);
        } catch (RuntimeException | LinkageError error) {
            return new WrappedGameProfile(target.getUniqueId(), target.getName());
        }
    }

    private void writePlayerInfoData(PacketContainer packet, List<PlayerInfoData> data) {
        StructureModifier<List<PlayerInfoData>> modifier = packet.getPlayerInfoDataLists();
        if (modifier.size() == 0) {
            throw new IllegalStateException("PLAYER_INFO packet has no PlayerInfoData list fields.");
        }
        modifier.write(modifier.size() > 1 ? 1 : 0, data);
    }

    private void send(Player viewer, PacketContainer packet) {
        if (viewer == null || !viewer.isOnline() || packet == null) {
            return;
        }
        try {
            protocolManager.sendServerPacket(viewer, packet, false);
        } catch (RuntimeException error) {
            plugin.getLogger().log(Level.FINE, "Unable to send Hide profile refresh packet.", error);
        }
    }

    @Override
    public void shutdown() {
        if (playerInfoListener != null) {
            protocolManager.removePacketListener(playerInfoListener);
            playerInfoListener = null;
        }
        for (UUID targetUuid : Set.copyOf(nametagDisplays.keySet())) {
            removeNametagDisplay(targetUuid);
        }
    }

    @Override
    public void clear(UUID targetUuid) {
        removeNametagDisplay(targetUuid);
    }

    private record PlayerInfoListField(int index, List<PlayerInfoData> data) {
    }
}
