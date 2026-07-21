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
                            Object soundObj = event.getPacket().getSoundEffects().readSafely(0);
                            if (soundObj instanceof org.bukkit.Sound sound) {
                                soundName = sound.name();
                            } else if (soundObj != null) {
                                soundName = soundObj.toString();
                            }
                            if (soundName.isEmpty() || "null".equalsIgnoreCase(soundName)) {
                                soundName = event.getPacket().getStrings().readSafely(0);
                            }
                            if (soundName != null) {
                                String soundUpper = soundName.toUpperCase();
                                if (soundUpper.contains("EXPLODE") || soundUpper.contains("EXPLOSION")) {
                                    event.setCancelled(true);
                                }
                            }
                        }
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
}
