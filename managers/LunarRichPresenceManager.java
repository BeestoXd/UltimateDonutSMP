package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.models.Team;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import com.lunarclient.apollo.Apollo;
import com.lunarclient.apollo.module.richpresence.RichPresenceModule;
import com.lunarclient.apollo.module.richpresence.ServerRichPresence;
import com.lunarclient.apollo.player.ApolloPlayer;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class LunarRichPresenceManager {

    private static final String PATH = "LUNAR-CLIENT.RICH-PRESENCE";
    private static final String SECTION_MARK = String.valueOf((char) 0x00A7);
    private static final Pattern LEGACY_HEX_PATTERN = Pattern.compile(
            "(?i)" + Pattern.quote(SECTION_MARK + "x") + "(" + Pattern.quote(SECTION_MARK) + "[0-9a-f]){6}"
    );
    private static final Pattern LEGACY_COLOR_PATTERN = Pattern.compile(
            "(?i)(?:&|" + Pattern.quote(SECTION_MARK) + ")[0-9a-fk-or]"
    );

    private final UltimateDonutSmp plugin;
    private final Map<UUID, String> lastPresenceFingerprints = new ConcurrentHashMap<>();

    private ScheduledTask updateTask;
    private RichPresenceModule richPresenceModule;

    public LunarRichPresenceManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        stopUpdateTask();

        if (!isConfiguredEnabled()) {
            resetAll();
            richPresenceModule = null;
            return;
        }

        if (!loadModule()) {
            return;
        }

        long periodTicks = getUpdatePeriodTicks();
        updateAll();
        updateTask = plugin.getPaperScheduler().runGlobalTimer(this::updateAll, periodTicks, periodTicks);
        plugin.getLogger().info("Lunar Client Rich Presence enabled via Apollo.");
    }

    public void shutdown() {
        stopUpdateTask();
        resetAll();
        richPresenceModule = null;
    }

    public void handleJoin(Player player) {
        if (player == null || !isConfiguredEnabled()) {
            return;
        }

        plugin.getPaperScheduler().runEntityLater(player, () -> updatePlayer(player), 100L);
    }

    public void handleQuit(Player player) {
        if (player == null) {
            return;
        }

        resetPlayer(player);
    }

    public void refreshPlayer(UUID uuid) {
        if (uuid == null || !isConfiguredEnabled()) {
            return;
        }

        Player player = Bukkit.getPlayer(uuid);
        if (player == null || !player.isOnline()) {
            lastPresenceFingerprints.remove(uuid);
            return;
        }

        plugin.getPaperScheduler().runEntity(player, () -> updatePlayer(player));
    }

    public void refreshPlayers(Collection<UUID> uuids) {
        if (uuids == null || uuids.isEmpty()) {
            return;
        }

        for (UUID uuid : uuids) {
            refreshPlayer(uuid);
        }
    }

    private void updateAll() {
        if (!isConfiguredEnabled() || richPresenceModule == null) {
            return;
        }

        plugin.getPaperScheduler().forEachOnlinePlayer(this::updatePlayer);
    }

    private void updatePlayer(Player player) {
        if (player == null || !player.isOnline() || !isConfiguredEnabled() || richPresenceModule == null) {
            return;
        }

        try {
            Optional<ApolloPlayer> apolloPlayer = Apollo.getPlayerManager().getPlayer(player.getUniqueId());
            if (apolloPlayer.isEmpty()) {
                lastPresenceFingerprints.remove(player.getUniqueId());
                return;
            }

            ServerRichPresence presence = buildPresence(player);
            String fingerprint = fingerprint(presence);
            if (fingerprint.equals(lastPresenceFingerprints.get(player.getUniqueId()))) {
                return;
            }

            richPresenceModule.overrideServerRichPresence(apolloPlayer.get(), presence);
            lastPresenceFingerprints.put(player.getUniqueId(), fingerprint);
        } catch (Throwable error) {
            plugin.getLogger().log(Level.FINE, "Failed to update Lunar Rich Presence for " + player.getName(), error);
        }
    }

    private ServerRichPresence buildPresence(Player player) {
        int fallbackTeamSize = getTeamSize(player);
        int fallbackTeamMaxSize = Math.max(
                fallbackTeamSize,
                plugin.getConfigManager().getConfig().getInt("TEAM.LIMIT-MEMBERS", 10)
        );
        int teamCurrentSize = Math.max(0, resolvePresenceInt(player, "TEAM-CURRENT-SIZE", fallbackTeamSize));
        int teamMaxSize = Math.max(teamCurrentSize, resolvePresenceInt(player, "TEAM-MAX-SIZE", fallbackTeamMaxSize));

        return ServerRichPresence.builder()
                .gameName(resolvePresenceText(player, "GAME-NAME", "SMP"))
                .gameVariantName(resolvePresenceText(player, "VARIANT", player.getName()))
                .gameState(resolvePresenceText(player, "GAME-STATE", "Playing"))
                .playerState(resolvePresenceText(player, "PLAYER-STATE", "Playing"))
                .mapName(resolvePresenceText(player, "WORLD-NAME", player.getWorld().getName()))
                .subServerName(resolvePresenceText(player, "SUB-SERVER-NAME", "SMP"))
                .teamCurrentSize(teamCurrentSize)
                .teamMaxSize(teamMaxSize)
                .build();
    }

    private String resolvePresenceText(Player player, String key, String fallback) {
        String template = getConfig().getString(PATH + "." + key, fallback);
        return sanitizePresenceText(resolveTemplate(player, template));
    }

    private int resolvePresenceInt(Player player, String key, int fallback) {
        String path = PATH + "." + key;
        if (!getConfig().isSet(path)) {
            return fallback;
        }

        String resolved = sanitizePresenceText(resolveTemplate(player, getConfig().getString(path, String.valueOf(fallback))));
        if (resolved == null || resolved.isBlank()) {
            return fallback;
        }

        try {
            return Integer.parseInt(resolved.replace(",", "").trim());
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private String resolveTemplate(Player player, String template) {
        String resolved = template == null ? "" : template;

        if (ColorUtils.hasPAPI()) {
            try {
                resolved = PlaceholderAPI.setPlaceholders(player, resolved);
            } catch (Throwable ignored) {
            }
        }

        return applyInternalPlaceholders(player, resolved);
    }

    private String applyInternalPlaceholders(Player player, String text) {
        PlayerData data = plugin.getPlayerDataManager().get(player);
        if (data == null) {
            data = plugin.getPlayerDataManager().get(player.getUniqueId());
        }

        Team team = plugin.getTeamManager().getTeam(player);
        String teamName = team != null ? team.getName().toUpperCase() : "None";
        int teamSize = getTeamSize(player);
        int teamMaxSize = Math.max(teamSize, plugin.getConfigManager().getConfig().getInt("TEAM.LIMIT-MEMBERS", 10));

        String money = data != null ? NumberUtils.format(data.getMoney()) : "0";
        String moneyNice = data != null ? NumberUtils.formatNice(data.getMoney()) : "0";
        String shards = data != null ? String.valueOf(data.getShards()) : "0";
        String kills = data != null ? String.valueOf(data.getKills()) : "0";
        String deaths = data != null ? String.valueOf(data.getDeaths()) : "0";
        String playtime = data != null ? NumberUtils.formatTimeLong(data.getTotalPlaytimeSeconds()) : "0s";

        String result = text;
        result = replaceToken(result, "player", player.getName());
        result = replaceToken(result, "username", player.getName());
        result = replaceToken(result, "world", player.getWorld().getName());
        result = replaceToken(result, "online", String.valueOf(Bukkit.getOnlinePlayers().size()));
        result = replaceToken(result, "max_players", String.valueOf(Bukkit.getMaxPlayers()));
        result = replaceToken(result, "team", teamName);
        result = replaceToken(result, "team_size", String.valueOf(teamSize));
        result = replaceToken(result, "team_max_size", String.valueOf(teamMaxSize));
        result = replaceToken(result, "ping", String.valueOf(player.getPing()));
        result = result.replace("%economy_username%", player.getName());
        result = result.replace("%economy_team%", teamName);
        result = result.replace("%economy_money%", money);
        result = result.replace("%economy_nicestMoney%", moneyNice);
        result = result.replace("%economy_shards%", shards);
        result = result.replace("%economy_kills%", kills);
        result = result.replace("%economy_deaths%", deaths);
        result = result.replace("%economy_playtime%", playtime);
        result = result.replace("%economy_ping%", String.valueOf(player.getPing()));
        return result;
    }

    private String replaceToken(String text, String key, String value) {
        String safeValue = value == null ? "" : value;
        return text
                .replace("{" + key + "}", safeValue)
                .replace("%" + key + "%", safeValue);
    }

    private String sanitizePresenceText(String text) {
        String sanitized = ColorUtils.colorize(text);
        sanitized = LEGACY_HEX_PATTERN.matcher(sanitized).replaceAll("");
        sanitized = LEGACY_COLOR_PATTERN.matcher(sanitized).replaceAll("");
        sanitized = sanitized.replace(SECTION_MARK, "");
        sanitized = sanitized.replace('\r', ' ').replace('\n', ' ').trim();

        int maxLength = getConfig().getInt(PATH + ".MAX-FIELD-LENGTH", 128);
        if (maxLength > 0 && sanitized.length() > maxLength) {
            return sanitized.substring(0, maxLength);
        }
        return sanitized;
    }

    private int getTeamSize(Player player) {
        Team team = plugin.getTeamManager().getTeam(player);
        return team != null ? Math.max(1, team.getMemberCount()) : 1;
    }

    private String fingerprint(ServerRichPresence presence) {
        return presence.getGameName()
                + "|" + presence.getGameVariantName()
                + "|" + presence.getGameState()
                + "|" + presence.getPlayerState()
                + "|" + presence.getMapName()
                + "|" + presence.getSubServerName()
                + "|" + presence.getTeamCurrentSize()
                + "|" + presence.getTeamMaxSize();
    }

    private void resetAll() {
        lastPresenceFingerprints.clear();

        if (richPresenceModule == null && !loadModule()) {
            return;
        }

        try {
            for (ApolloPlayer apolloPlayer : Apollo.getPlayerManager().getPlayers()) {
                richPresenceModule.resetServerRichPresence(apolloPlayer);
            }
        } catch (Throwable error) {
            plugin.getLogger().log(Level.FINE, "Failed to reset Lunar Rich Presence for online players.", error);
        }
    }

    private void resetPlayer(Player player) {
        lastPresenceFingerprints.remove(player.getUniqueId());

        if (richPresenceModule == null) {
            return;
        }

        try {
            Apollo.getPlayerManager()
                    .getPlayer(player.getUniqueId())
                    .ifPresent(richPresenceModule::resetServerRichPresence);
        } catch (Throwable error) {
            plugin.getLogger().log(Level.FINE, "Failed to reset Lunar Rich Presence for " + player.getName(), error);
        }
    }

    private boolean loadModule() {
        try {
            if (!Apollo.getModuleManager().isEnabled(RichPresenceModule.class)) {
                plugin.getLogger().warning("Lunar Rich Presence is enabled, but the Apollo rich_presence module is disabled.");
                richPresenceModule = null;
                return false;
            }

            richPresenceModule = Apollo.getModuleManager().getModule(RichPresenceModule.class);
            return richPresenceModule != null;
        } catch (Throwable error) {
            plugin.getLogger().log(Level.WARNING, "Lunar Rich Presence is enabled, but Apollo is not ready.", error);
            richPresenceModule = null;
            return false;
        }
    }

    private long getUpdatePeriodTicks() {
        double minutes = Math.max(0.05D, getConfig().getDouble(PATH + ".UPDATE", 1.0D));
        return Math.max(20L, Math.round(minutes * 60.0D * 20.0D));
    }

    private boolean isConfiguredEnabled() {
        return getConfig().getBoolean(PATH + ".ENABLED", true);
    }

    private FileConfiguration getConfig() {
        return plugin.getConfigManager().getConfig();
    }

    private void stopUpdateTask() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
    }
}
