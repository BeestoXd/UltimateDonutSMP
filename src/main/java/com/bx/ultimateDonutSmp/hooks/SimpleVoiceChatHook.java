package com.bx.ultimateDonutSmp.hooks;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import de.maxhenkel.voicechat.api.BukkitVoicechatService;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;

import java.util.UUID;

/**
 * Blocks microphone audio from players who have not accepted the voice chat policy.
 *
 * Simple Voice Chat is optional, so nothing here may be touched before
 * {@code Bukkit.getPluginManager().getPlugin("voicechat")} is known to exist — loading this class
 * without the API on the classpath throws.
 */
public class SimpleVoiceChatHook implements VoicechatPlugin {

    private final UltimateDonutSmp plugin;

    public SimpleVoiceChatHook(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    /** Returns true when Simple Voice Chat accepted the registration. */
    public static boolean register(UltimateDonutSmp plugin) {
        BukkitVoicechatService service = plugin.getServer().getServicesManager().load(BukkitVoicechatService.class);
        if (service == null) {
            return false;
        }
        service.registerPlugin(new SimpleVoiceChatHook(plugin));
        return true;
    }

    @Override
    public String getPluginId() {
        return "ultimatedonutsmp";
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(MicrophonePacketEvent.class, this::onMicrophonePacket);
    }

    private void onMicrophonePacket(MicrophonePacketEvent event) {
        if (!event.isCancellable() || event.isCancelled()) {
            return;
        }
        UUID speaker = speakerUuid(event.getSenderConnection());
        if (speaker == null) {
            return;
        }
        if (!plugin.getVoiceChatConsentManager().mayTalk(speaker)) {
            event.cancel();
        }
    }

    private UUID speakerUuid(VoicechatConnection connection) {
        if (connection == null || connection.getPlayer() == null) {
            return null;
        }
        return connection.getPlayer().getUuid();
    }
}
