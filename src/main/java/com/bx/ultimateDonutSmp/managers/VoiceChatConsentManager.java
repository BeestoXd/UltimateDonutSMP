package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.hooks.SimpleVoiceChatHook;
import com.bx.ultimateDonutSmp.menus.VoiceChatConsentMenu;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.models.VoiceChatConsent;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class VoiceChatConsentManager {

    private static final String VOICECHAT_PLUGIN = "voicechat";

    private final UltimateDonutSmp plugin;

    /**
     * Simple Voice Chat delivers microphone packets off the main thread, so the answer has to be
     * readable without touching PlayerData or the Bukkit API.
     */
    private final Map<UUID, VoiceChatConsent> consentCache = new ConcurrentHashMap<>();

    private volatile boolean enforcing;
    private boolean hookRegistered;

    public VoiceChatConsentManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
        refreshSettings();
    }

    public boolean isEnabled() {
        return plugin.getFeatureManager().isEnabled(FeatureManager.Feature.VOICE_CHAT);
    }

    public void refreshSettings() {
        enforcing = isEnabled()
                && plugin.getConfigManager().getConfig().getBoolean("VOICE-CHAT.MUTE-UNTIL-ACCEPTED", true);
    }

    /**
     * Simple Voice Chat is a soft dependency, so the hook class stays untouched until the plugin is
     * known to be installed.
     */
    public void registerVoicechatHook() {
        if (hookRegistered || Bukkit.getPluginManager().getPlugin(VOICECHAT_PLUGIN) == null) {
            return;
        }
        try {
            hookRegistered = SimpleVoiceChatHook.register(plugin);
        } catch (Throwable throwable) {
            plugin.getLogger().log(Level.WARNING, "Could not hook into Simple Voice Chat.", throwable);
        }
    }

    public boolean isVoicechatInstalled() {
        return Bukkit.getPluginManager().getPlugin(VOICECHAT_PLUGIN) != null;
    }

    public VoiceChatConsent getConsent(Player player) {
        if (player == null) {
            return VoiceChatConsent.UNDECIDED;
        }
        PlayerData data = plugin.getPlayerDataManager().get(player);
        return data == null ? VoiceChatConsent.UNDECIDED : data.getVoiceChatConsent();
    }

    /** Called from the microphone packet handler, so it must stay off the Bukkit API. */
    public boolean mayTalk(UUID uuid) {
        if (!enforcing) {
            return true;
        }
        return consentCache.getOrDefault(uuid, VoiceChatConsent.UNDECIDED) == VoiceChatConsent.ACCEPTED;
    }

    public void handleJoin(Player player) {
        if (player == null) {
            return;
        }
        VoiceChatConsent consent = getConsent(player);
        consentCache.put(player.getUniqueId(), consent);

        if (!isEnabled() || consent != VoiceChatConsent.UNDECIDED) {
            return;
        }
        if (!plugin.getConfigManager().getConfig().getBoolean("VOICE-CHAT.PROMPT-ON-JOIN", true)) {
            return;
        }

        long delay = Math.max(1L,
                plugin.getConfigManager().getConfig().getLong("VOICE-CHAT.PROMPT-DELAY-TICKS", 40L));
        plugin.getSpigotScheduler().runEntityLater(player, () -> {
            if (player.isOnline() && getConsent(player) == VoiceChatConsent.UNDECIDED) {
                openMenu(player);
            }
        }, delay);
    }

    public void handleQuit(UUID uuid) {
        if (uuid != null) {
            consentCache.remove(uuid);
        }
    }

    public void openMenu(Player player) {
        if (player == null) {
            return;
        }
        new VoiceChatConsentMenu(plugin).open(player);
    }

    public void accept(Player player) {
        apply(player, VoiceChatConsent.ACCEPTED,
                "VOICE-CHAT.ACCEPTED", "&aVoice chat is now enabled for you.");
    }

    public void decline(Player player) {
        apply(player, VoiceChatConsent.DECLINED,
                "VOICE-CHAT.DECLINED", "&cVoice chat will stay disabled.");
    }

    public void revoke(Player player) {
        apply(player, VoiceChatConsent.UNDECIDED,
                "VOICE-CHAT.REVOKED", "&cYou withdrew your voice chat consent. Voice chat is disabled again.");
    }

    private void apply(Player player, VoiceChatConsent consent, String messagePath, String fallback) {
        if (player == null) {
            return;
        }
        PlayerData data = plugin.getPlayerDataManager().get(player);
        if (data != null) {
            data.setVoiceChatConsent(consent);
        }
        consentCache.put(player.getUniqueId(), consent);
        player.sendMessage(ColorUtils.toComponent(
                plugin.getLanguageManager().message(messagePath, fallback), player));
    }
}
