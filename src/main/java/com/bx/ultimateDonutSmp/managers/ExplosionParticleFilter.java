package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.wrappers.WrappedParticle;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.Locale;

public final class ExplosionParticleFilter {

    private final UltimateDonutSmp plugin;
    private ProtocolManager protocolManager;
    private PacketListener listener;
    private boolean available;

    public ExplosionParticleFilter(UltimateDonutSmp plugin) {
        this.plugin = plugin;
        initialize();
    }

    public boolean isAvailable() {
        return available;
    }

    public void shutdown() {
        if (protocolManager != null && listener != null) {
            protocolManager.removePacketListener(listener);
        }
        listener = null;
        protocolManager = null;
        available = false;
    }

    private void initialize() {
        if (!plugin.getServer().getPluginManager().isPluginEnabled("ProtocolLib")) {
            return;
        }
        try {
            protocolManager = ProtocolLibrary.getProtocolManager();
            listener = new PacketAdapter(
                    plugin,
                    ListenerPriority.NORMAL,
                    PacketType.Play.Server.WORLD_PARTICLES,
                    PacketType.Play.Server.NAMED_SOUND_EFFECT
            ) {
                @Override
                public void onPacketSending(PacketEvent event) {
                    try {
                        Player viewer = event.getPlayer();
                        PlayerData data = ExplosionParticleFilter.this.plugin
                                .getPlayerDataManager().get(viewer);
                        if (data == null) {
                            return;
                        }
                        PacketType type = event.getPacketType();
                        if (type == PacketType.Play.Server.WORLD_PARTICLES) {
                            if (!data.isExplosionParticlesEnabled()) {
                                WrappedParticle<?> wrapped = event.getPacket().getNewParticles().readSafely(0);
                                Particle particle = wrapped == null ? null : wrapped.getParticle();
                                if (particle == Particle.EXPLOSION || particle == Particle.EXPLOSION_EMITTER) {
                                    event.setCancelled(true);
                                }
                            }
                        } else if (type == PacketType.Play.Server.NAMED_SOUND_EFFECT) {
                            if (!data.isExplosionSoundsEnabled()) {
                                String soundName = "";
                                try {
                                    Object soundObj = event.getPacket().getSoundEffects().readSafely(0);
                                    soundName = extractSoundName(soundObj);
                                } catch (Throwable ignored) {
                                }
                                if (soundName.isEmpty() || "null".equalsIgnoreCase(soundName)) {
                                    try {
                                        soundName = extractSoundName(event.getPacket().getModifier().readSafely(0));
                                    } catch (Throwable ignored) {
                                    }
                                }
                                if (soundName.isEmpty() || "null".equalsIgnoreCase(soundName)) {
                                    try {
                                        soundName = event.getPacket().getStrings().readSafely(0);
                                    } catch (Throwable ignored) {
                                    }
                                }
                                if (isExplosionSound(soundName)) {
                                    event.setCancelled(true);
                                }
                            }
                        }
                    } catch (Throwable ignored) {
                    }
                }
            };
            protocolManager.addPacketListener(listener);
            available = true;
        } catch (Throwable error) {
            plugin.getLogger().warning("Explosion particle/sound setting is unavailable: " + error.getMessage());
            shutdown();
        }
    }

    public static boolean isExplosionSound(String soundName) {
        if (soundName == null || soundName.isBlank()) {
            return false;
        }
        String upper = soundName.toUpperCase(Locale.ROOT);
        return upper.contains("EXPLODE") || upper.contains("EXPLOSION");
    }

    public static String extractSoundName(Object soundObj) {
        if (soundObj == null) {
            return "";
        }
        if (soundObj instanceof org.bukkit.Sound sound) {
            try {
                String name = sound.name();
                if (name != null && !name.isBlank()) {
                    return name;
                }
            } catch (Throwable ignored) {
                // Paper throws IllegalStateException on unregistered / direct sound holders
            }
            try {
                org.bukkit.NamespacedKey key = sound.getKey();
                if (key != null) {
                    return key.toString();
                }
            } catch (Throwable ignored) {
            }
        }
        try {
            String str = soundObj.toString();
            if (str != null && !str.isBlank() && !"null".equalsIgnoreCase(str)) {
                return str;
            }
        } catch (Throwable ignored) {
        }
        return "";
    }
}
