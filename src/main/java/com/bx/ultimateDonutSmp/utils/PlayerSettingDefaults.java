package com.bx.ultimateDonutSmp.utils;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.models.ThreeChoice;
import com.bx.ultimateDonutSmp.models.TwoChoice;
import org.bukkit.configuration.ConfigurationSection;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Reads the optional {@code DEFAULT} and {@code ENABLED} keys admins may place under each
 * {@code SETTINGS-MENU.BUTTONS} entry in menus.yml.
 *
 * <p>{@code DEFAULT} decides what a setting looks like for players who have never touched it,
 * {@code ENABLED: false} removes the option from /settings entirely and pins every player to the
 * configured default.</p>
 */
public final class PlayerSettingDefaults {

    public static final String BUTTONS_PATH = "SETTINGS-MENU.BUTTONS";

    private static final String DEFAULT_KEY = "DEFAULT";
    private static final String ENABLED_KEY = "ENABLED";

    private static final Map<String, Binding> BINDINGS = createBindings();

    private PlayerSettingDefaults() {
    }

    /** Whether the button described by {@code buttonSection} should still be offered to players. */
    public static boolean isOptionEnabled(ConfigurationSection buttonSection) {
        return buttonSection == null || buttonSection.getBoolean(ENABLED_KEY, true);
    }

    public static boolean isOptionEnabled(ConfigurationSection buttons, String key) {
        return buttons == null || isOptionEnabled(buttons.getConfigurationSection(key));
    }

    public static boolean isOptionEnabled(UltimateDonutSmp plugin, String key) {
        return isOptionEnabled(buttons(plugin), key);
    }

    /** Applies every configured {@code DEFAULT} to a player who has no stored settings yet. */
    public static void applyDefaults(UltimateDonutSmp plugin, PlayerData data) {
        applyDefaults(buttons(plugin), data, warningSink(plugin));
    }

    public static void applyDefaults(ConfigurationSection buttons, PlayerData data) {
        applyDefaults(buttons, data, null);
    }

    public static void applyDefaults(ConfigurationSection buttons, PlayerData data, Consumer<String> warnings) {
        if (buttons == null || data == null) {
            return;
        }
        for (String key : buttons.getKeys(false)) {
            ConfigurationSection section = buttons.getConfigurationSection(key);
            if (section == null || !section.contains(DEFAULT_KEY)) {
                continue;
            }
            applyConfiguredDefault(key, section, data, warnings);
        }
    }

    /**
     * Pins every option turned off with {@code ENABLED: false} back to its configured default, so a
     * removed option behaves the same for players who had already toggled it.
     *
     * <p>The previous dirty flag is restored afterwards: the override lives in memory and never
     * schedules a database write on its own.</p>
     */
    public static void applyDisabledOptions(UltimateDonutSmp plugin, PlayerData data) {
        applyDisabledOptions(buttons(plugin), data, warningSink(plugin));
    }

    public static void applyDisabledOptions(ConfigurationSection buttons, PlayerData data) {
        applyDisabledOptions(buttons, data, null);
    }

    public static void applyDisabledOptions(ConfigurationSection buttons, PlayerData data, Consumer<String> warnings) {
        if (buttons == null || data == null) {
            return;
        }

        boolean wasDirty = data.isDirty();
        PlayerData pristine = null;
        for (String key : buttons.getKeys(false)) {
            ConfigurationSection section = buttons.getConfigurationSection(key);
            if (section == null || isOptionEnabled(section)) {
                continue;
            }
            if (section.contains(DEFAULT_KEY)) {
                applyConfiguredDefault(key, section, data, warnings);
                continue;
            }
            Binding binding = BINDINGS.get(key);
            if (binding == null) {
                continue;
            }
            if (pristine == null) {
                pristine = new PlayerData(data.getUuid(), data.getUsername());
            }
            binding.copier().copy(data, pristine);
        }
        data.setDirty(wasDirty);
    }

    private static void applyConfiguredDefault(
            String key,
            ConfigurationSection section,
            PlayerData data,
            Consumer<String> warnings
    ) {
        Binding binding = BINDINGS.get(key);
        Object configured = section.get(DEFAULT_KEY);
        if (binding == null) {
            warn(warnings, "Ignoring " + BUTTONS_PATH + "." + key + "." + DEFAULT_KEY
                    + ": this setting does not support a configurable default.");
            return;
        }
        String raw = configured == null ? "" : String.valueOf(configured).trim().toLowerCase(Locale.ROOT);
        if (!binding.applier().apply(data, raw)) {
            warn(warnings, "Ignoring " + BUTTONS_PATH + "." + key + "." + DEFAULT_KEY
                    + ": '" + configured + "' is not a valid value for this setting.");
        }
    }

    private static ConfigurationSection buttons(UltimateDonutSmp plugin) {
        if (plugin == null || plugin.getConfigManager() == null) {
            return null;
        }
        return plugin.getConfigManager().getMenus().getConfigurationSection(BUTTONS_PATH);
    }

    private static Consumer<String> warningSink(UltimateDonutSmp plugin) {
        return plugin == null ? null : message -> plugin.getLogger().warning(message);
    }

    private static void warn(Consumer<String> warnings, String message) {
        if (warnings != null) {
            warnings.accept(message);
        }
    }

    private static Map<String, Binding> createBindings() {
        Map<String, Binding> bindings = new LinkedHashMap<>();

        bindings.put("PUBLIC_CHAT", bool(PlayerData::isPublicChatEnabled, PlayerData::setPublicChatEnabled));
        bindings.put("PRIVATE_MESSAGES", threeChoice(
                PlayerData::getPrivateMessagesChoice, PlayerData::setPrivateMessagesChoice));
        bindings.put("SERVER_BROADCASTS", bool(
                PlayerData::isServerBroadcastsEnabled, PlayerData::setServerBroadcastsEnabled));
        bindings.put("TEAM_CHAT_VISIBILITY", bool(PlayerData::isTeamChatVisible, PlayerData::setTeamChatVisible));
        bindings.put("TPA_CONFIRM_MENUS", bool(
                PlayerData::isTpaConfirmMenuEnabled, PlayerData::setTpaConfirmMenuEnabled));
        bindings.put("DESTROY_PEARL_ON_DEATH", bool(
                PlayerData::isDestroyPearlOnDeath, PlayerData::setDestroyPearlOnDeath));
        bindings.put("PAY_CONFIRM_MENUS", bool(
                PlayerData::isPayConfirmMenuEnabled, PlayerData::setPayConfirmMenuEnabled));
        bindings.put("AUTO_CONFIRM_TPAS", new Binding(
                (data, raw) -> {
                    Boolean value = parseBoolean(raw);
                    if (value == null) {
                        return false;
                    }
                    data.setTpauto(value);
                    data.setAutoTpaHereEnabled(value);
                    return true;
                },
                (target, source) -> {
                    target.setTpauto(source.isTpauto());
                    target.setAutoTpaHereEnabled(source.isAutoTpaHereEnabled());
                }));
        bindings.put("HOTBAR_MESSAGES", bool(
                PlayerData::isHotbarMessagesEnabled, PlayerData::setHotbarMessagesEnabled));
        bindings.put("NOTIFICATION_SOUNDS", bool(
                PlayerData::isNotificationSoundsEnabled, PlayerData::setNotificationSoundsEnabled));
        bindings.put("FOLLOW_ALERT_SETTINGS", bool(
                PlayerData::isFollowAlertsEnabled, PlayerData::setFollowAlertsEnabled));
        bindings.put("DISPLAY_DONUT_PLUS", bool(
                PlayerData::isDisplayDonutPlusEnabled, PlayerData::setDisplayDonutPlusEnabled));
        bindings.put("CHAINMAIL_ON_RESPAWN", bool(
                PlayerData::isChainmailOnRespawnEnabled, PlayerData::setChainmailOnRespawnEnabled));
        bindings.put("EXPLOSION_PARTICLES", bool(
                PlayerData::isExplosionParticlesEnabled, PlayerData::setExplosionParticlesEnabled));
        bindings.put("EXPLOSION_SOUNDS", bool(
                PlayerData::isExplosionSoundsEnabled, PlayerData::setExplosionSoundsEnabled));
        bindings.put("TELEPORT_ALERTS", bool(
                PlayerData::isTeleportAlertsEnabled, PlayerData::setTeleportAlertsEnabled));
        bindings.put("RTP_COORDINATES", bool(
                PlayerData::isRtpCoordinatesEnabled, PlayerData::setRtpCoordinatesEnabled));
        bindings.put("FAST_CRYSTALS", bool(PlayerData::isFastCrystalsEnabled, PlayerData::setFastCrystalsEnabled));
        bindings.put("RANDOMIZED_COORDS", bool(PlayerData::isRandomizedCoords, PlayerData::setRandomizedCoords));
        bindings.put("TPA_REQUESTS", threeChoice(
                PlayerData::getTpaRequestsChoice, PlayerData::setTpaRequestsChoice));
        bindings.put("TPA_HERE_REQUESTS", threeChoice(
                PlayerData::getTpaHereRequestsChoice, PlayerData::setTpaHereRequestsChoice));
        bindings.put("PAYMENTS", threeChoice(PlayerData::getPaymentsChoice, PlayerData::setPaymentsChoice));
        bindings.put("WORTH_DISPLAY", bool(PlayerData::isWorthDisplayEnabled, PlayerData::setWorthDisplayEnabled));
        bindings.put("MONEY_NAMETAGS", bool(
                PlayerData::isMoneyNametagsEnabled, PlayerData::setMoneyNametagsEnabled));
        bindings.put("JOIN_LEAVE_MESSAGES", twoChoice(
                PlayerData::getJoinLeaveMessagesChoice, PlayerData::setJoinLeaveMessagesChoice));
        bindings.put("PAY_ALERTS", bool(PlayerData::isPayAlertsEnabled, PlayerData::setPayAlertsEnabled));
        bindings.put("ADVANCEMENT_MESSAGES", bool(
                PlayerData::isAdvancementMessagesEnabled, PlayerData::setAdvancementMessagesEnabled));
        bindings.put("AUCTION_NOTIFICATIONS", bool(
                PlayerData::isAuctionNotificationsEnabled, PlayerData::setAuctionNotificationsEnabled));
        bindings.put("AMETHYST_BREAK_MESSAGES", bool(
                PlayerData::isAmethystBreakMessagesEnabled, PlayerData::setAmethystBreakMessagesEnabled));
        bindings.put("DUEL_REQUESTS", bool(PlayerData::isDuelRequestsEnabled, PlayerData::setDuelRequestsEnabled));
        bindings.put("DEATH_MESSAGES", twoChoice(
                PlayerData::getDeathMessagesChoice, PlayerData::setDeathMessagesChoice));
        bindings.put("KEY_ALL_NOTIFICATIONS", bool(
                PlayerData::isKeyAllNotificationsEnabled, PlayerData::setKeyAllNotificationsEnabled));
        bindings.put("ORDER_NOTIFICATIONS", bool(
                PlayerData::isOrderNotificationsEnabled, PlayerData::setOrderNotificationsEnabled));
        // The two spawn buttons read as "prevention is on", so the stored flag is the inverse.
        bindings.put("DISABLE_MOB_SPAWN", new Binding(
                (data, raw) -> {
                    Boolean value = parseBoolean(raw);
                    if (value == null) {
                        return false;
                    }
                    data.setMobSpawnEnabled(!value);
                    data.setMobSpawnDisabledUntil(0L);
                    return true;
                },
                (target, source) -> {
                    target.setMobSpawnEnabled(source.isMobSpawnEnabled());
                    target.setMobSpawnDisabledUntil(source.getMobSpawnDisabledUntil());
                }));
        bindings.put("DISABLE_PHANTOM_SPAWN", new Binding(
                (data, raw) -> {
                    Boolean value = parseBoolean(raw);
                    if (value == null) {
                        return false;
                    }
                    data.setPhantomEnabled(!value);
                    data.setPhantomDisabledUntil(0L);
                    return true;
                },
                (target, source) -> {
                    target.setPhantomEnabled(source.isPhantomEnabled());
                    target.setPhantomDisabledUntil(source.getPhantomDisabledUntil());
                }));
        bindings.put("NIGHT_VISION", bool(PlayerData::isNightVisionEnabled, PlayerData::setNightVisionEnabled));
        bindings.put("BOUNTY_ALERTS", bool(PlayerData::isBountyAlertsEnabled, PlayerData::setBountyAlertsEnabled));
        bindings.put("SCOREBOARD_VISIBILITY", bool(PlayerData::isScoreboardVisible, PlayerData::setScoreboardVisible));
        bindings.put("SHOW_MONEY", bool(PlayerData::isShowMoneyLine, PlayerData::setShowMoneyLine));
        bindings.put("SHOW_SHARDS", bool(PlayerData::isShowShardsLine, PlayerData::setShowShardsLine));
        bindings.put("SHOW_KILLS", bool(PlayerData::isShowKillsLine, PlayerData::setShowKillsLine));
        bindings.put("SHOW_DEATHS", bool(PlayerData::isShowDeathsLine, PlayerData::setShowDeathsLine));
        bindings.put("SHOW_PLAYTIME", bool(PlayerData::isShowPlaytimeLine, PlayerData::setShowPlaytimeLine));
        bindings.put("COMBAT_TIMER", bool(PlayerData::isCombatTimerEnabled, PlayerData::setCombatTimerEnabled));

        return Map.copyOf(bindings);
    }

    private static Binding bool(Predicate<PlayerData> getter, BooleanSetter setter) {
        return new Binding(
                (data, raw) -> {
                    Boolean value = parseBoolean(raw);
                    if (value == null) {
                        return false;
                    }
                    setter.set(data, value);
                    return true;
                },
                (target, source) -> setter.set(target, getter.test(source)));
    }

    private static Binding threeChoice(
            Function<PlayerData, ThreeChoice> getter,
            BiConsumer<PlayerData, ThreeChoice> setter
    ) {
        return new Binding(
                (data, raw) -> {
                    ThreeChoice value = parseThreeChoice(raw);
                    if (value == null) {
                        return false;
                    }
                    setter.accept(data, value);
                    return true;
                },
                (target, source) -> setter.accept(target, getter.apply(source)));
    }

    private static Binding twoChoice(
            Function<PlayerData, TwoChoice> getter,
            BiConsumer<PlayerData, TwoChoice> setter
    ) {
        return new Binding(
                (data, raw) -> {
                    TwoChoice value = parseTwoChoice(raw);
                    if (value == null) {
                        return false;
                    }
                    setter.accept(data, value);
                    return true;
                },
                (target, source) -> setter.accept(target, getter.apply(source)));
    }

    private static Boolean parseBoolean(String raw) {
        return switch (raw) {
            case "true", "yes", "on", "enable", "enabled", "1" -> Boolean.TRUE;
            case "false", "no", "off", "disable", "disabled", "0" -> Boolean.FALSE;
            default -> null;
        };
    }

    private static ThreeChoice parseThreeChoice(String raw) {
        return switch (raw) {
            case "anyone", "everyone", "all", "true", "yes", "on", "enable", "enabled", "1" -> ThreeChoice.ANYONE;
            case "friends_followed", "friends-followed", "friends/followed", "friends", "followed"
                    -> ThreeChoice.FRIENDS_FOLLOWED;
            case "off", "none", "false", "no", "disable", "disabled", "0" -> ThreeChoice.OFF;
            default -> null;
        };
    }

    private static TwoChoice parseTwoChoice(String raw) {
        return switch (raw) {
            case "friends_followed", "friends-followed", "friends/followed", "friends", "followed",
                 "true", "yes", "on", "enable", "enabled", "1" -> TwoChoice.FRIENDS_FOLLOWED;
            case "off", "none", "false", "no", "disable", "disabled", "0" -> TwoChoice.OFF;
            default -> null;
        };
    }

    @FunctionalInterface
    private interface BooleanSetter {
        void set(PlayerData data, boolean value);
    }

    @FunctionalInterface
    private interface ValueApplier {
        /** @return false when the configured text is not a valid value for this setting */
        boolean apply(PlayerData data, String rawValue);
    }

    @FunctionalInterface
    private interface ValueCopier {
        void copy(PlayerData target, PlayerData source);
    }

    private record Binding(ValueApplier applier, ValueCopier copier) {
    }
}
