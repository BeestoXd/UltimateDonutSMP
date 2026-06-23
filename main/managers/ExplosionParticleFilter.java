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
                    PacketType.Play.Server.WORLD_PARTICLES
            ) {
                @Override
                public void onPacketSending(PacketEvent event) {
                    Player viewer = event.getPlayer();
                    PlayerData data = ExplosionParticleFilter.this.plugin
                            .getPlayerDataManager().get(viewer);
                    if (data == null || data.isExplosionParticlesEnabled()) {
                        return;
                    }
                    WrappedParticle<?> wrapped = event.getPacket().getNewParticles().readSafely(0);
                    Particle particle = wrapped == null ? null : wrapped.getParticle();
                    if (particle == Particle.EXPLOSION || particle == Particle.EXPLOSION_EMITTER) {
                        event.setCancelled(true);
                    }
                }
            };
            protocolManager.addPacketListener(listener);
            available = true;
        } catch (Throwable error) {
            plugin.getLogger().warning("Explosion particle setting is unavailable: " + error.getMessage());
            shutdown();
        }
    }
}
