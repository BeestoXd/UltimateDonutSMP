package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.models.ThreeChoice;
import com.bx.ultimateDonutSmp.models.TwoChoice;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.MobSpawnPolicy;
import com.bx.ultimateDonutSmp.utils.PermissionUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class PlayerSettingsMenu extends BaseMenu {

    private static final String MENU_PATH = "SETTINGS-MENU";

    private static final Set<String> VALID_SETTINGS = Set.of(
            "PUBLIC_CHAT", "PRIVATE_MESSAGES", "SERVER_BROADCASTS", "TEAM_CHAT_VISIBILITY",
            "LUNAR_TEAMMATES", "TPA_CONFIRM_MENUS", "QUICK_AUCTION_PURCHASE", "DESTROY_PEARL_ON_DEATH",
            "PAY_CONFIRM_MENUS", "AUTO_CONFIRM_TPAS", "HOTBAR_MESSAGES", "NOTIFICATION_SOUNDS",
            "FOLLOW_ALERT_SETTINGS", "DISPLAY_DONUT_PLUS", "CHAINMAIL_ON_RESPAWN", "EXPLOSION_PARTICLES",
            "EXPLOSION_SOUNDS", "TELEPORT_ALERTS", "FAST_CRYSTALS", "RANDOMIZED_COORDS",
            "TPA_REQUESTS", "TPA_HERE_REQUESTS", "PAYMENTS", "WORTH_DISPLAY",
            "JOIN_LEAVE_MESSAGES", "PAY_ALERTS", "ADVANCEMENT_MESSAGES", "AUCTION_NOTIFICATIONS",
            "AMETHYST_BREAK_MESSAGES", "DUEL_REQUESTS", "DEATH_MESSAGES", "KEY_ALL_NOTIFICATIONS",
            "QUICK_AUCTION_SELL", "ORDER_NOTIFICATIONS", "DISABLE_MOB_SPAWN", "DISABLE_PHANTOM_SPAWN",
            "NIGHT_VISION", "BOUNTY_ALERTS"
    );

    private final Map<Integer, String> clickableButtons = new HashMap<>();
    private UUID preferencePlayerId;
    private Boolean quickBuyEnabled;
    private Boolean quickSellEnabled;
    private boolean preferenceLoading;

    public PlayerSettingsMenu(UltimateDonutSmp plugin) {
        super(
                plugin,
                plugin.getConfigManager().getMenus().getString(MENU_PATH + ".TITLE", "&8ѕᴇᴛᴛɪɴɢѕ"),
                plugin.getConfigManager().getMenus().getInt(MENU_PATH + ".SIZE", 54)
        );
    }

    @Override
    public void build(Player player) {
        clear();
        clickableButtons.clear();

        PlayerData data = plugin.getPlayerDataManager().get(player);
        if (data == null) {
            return;
        }

        ConfigurationSection buttons = plugin.getConfigManager().getMenus()
                .getConfigurationSection(MENU_PATH + ".BUTTONS");
        if (buttons == null) {
            return;
        }
        if (buttons.contains("QUICK_AUCTION_PURCHASE") || buttons.contains("QUICK_AUCTION_SELL")) {
            loadPreference(player);
        }

        for (String key : buttons.getKeys(false)) {
            if (!shouldRenderButton(key)) {
                continue;
            }
            ConfigurationSection section = buttons.getConfigurationSection(key);
            if (section != null) {
                renderButton(player, data, key, section);
            }
        }
    }

    @Override
    public void handleClick(int slot, Player player) {
        String key = clickableButtons.get(slot);
        if (key == null) {
            return;
        }
        PlayerData data = plugin.getPlayerDataManager().get(player);
        if (data == null) {
            return;
        }

        SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));

        switch (key) {
            case "PUBLIC_CHAT" -> toggle(player, "Public Chat",
                    !data.isPublicChatEnabled(), data::setPublicChatEnabled);
            case "PRIVATE_MESSAGES" -> {
                data.setPrivateMessagesChoice(nextThreeChoice(data.getPrivateMessagesChoice()));
                sendChoiceMessage(player, "Private Messages", formatThreeChoice(data.getPrivateMessagesChoice()));
            }
            case "SERVER_BROADCASTS" -> toggle(player, "Server Broadcasts",
                    !data.isServerBroadcastsEnabled(), data::setServerBroadcastsEnabled);
            case "HOTBAR_MESSAGES" -> toggle(player, "Hotbar Notifications",
                    !data.isHotbarMessagesEnabled(), data::setHotbarMessagesEnabled);
            case "PAY_ALERTS" -> toggle(player, "Pay Alerts",
                    !data.isPayAlertsEnabled(), data::setPayAlertsEnabled);
            case "BOUNTY_ALERTS" -> toggle(player, "Bounty Alerts",
                    !data.isBountyAlertsEnabled(), data::setBountyAlertsEnabled);
            case "AUCTION_NOTIFICATIONS" -> toggle(player, "Auction Notifications",
                    !data.isAuctionNotificationsEnabled(), data::setAuctionNotificationsEnabled);
            case "FAST_CRYSTALS" -> {
                data.setFastCrystalsEnabled(!data.isFastCrystalsEnabled());
                plugin.getFastCrystalManager().applyCrystalCooldown(player);
                sendToggleMessage(player, "Fast Crystals", data.isFastCrystalsEnabled());
            }
            case "TOTEM_PARTICLES" -> toggle(player, "Totem Particles",
                    !data.isTotemParticlesEnabled(), data::setTotemParticlesEnabled);
            case "EXPLOSION_PARTICLES" -> toggle(player, "Explosion Particles",
                    !data.isExplosionParticlesEnabled(), data::setExplosionParticlesEnabled);
            case "QUICK_AUCTION_PURCHASE" -> toggleQuickBuy(player);
            case "QUICK_AUCTION_SELL" -> toggleQuickSell(player);
            case "CHAINMAIL_ON_RESPAWN" -> toggle(player, "Automatic Respawn Kit",
                    !data.isChainmailOnRespawnEnabled(), data::setChainmailOnRespawnEnabled);
            case "DISABLE_MOB_SPAWN" -> {
                data.setMobSpawnEnabled(!data.isMobSpawnEnabled());
                if (!data.isMobSpawnEnabled()) {
                    clearNearbyHostileMobs(player);
                    long limitSeconds = plugin.getConfigManager().getConfig().getLong("SETTINGS.DISABLE-MOB-SPAWN-LIMIT-SECONDS", -1L);
                    if (limitSeconds > 0) {
                        data.setMobSpawnDisabledUntil(System.currentTimeMillis() + (limitSeconds * 1000L));
                    } else {
                        data.setMobSpawnDisabledUntil(0L);
                    }
                } else {
                    data.setMobSpawnDisabledUntil(0L);
                }
                sendToggleMessage(player, "Nearby Mob Spawn Prevention", !data.isMobSpawnEnabled());
            }
            case "HIDE_ALL_PLAYERS" -> {
                data.setHideAllPlayersEnabled(!data.isHideAllPlayersEnabled());
                plugin.getPlayerVisibilityManager().applyViewerPreference(player);
                sendToggleMessage(player, "Hide All Players", data.isHideAllPlayersEnabled());
            }
            case "SCOREBOARD_VISIBILITY" -> {
                data.setScoreboardVisible(!data.isScoreboardVisible());
                plugin.getScoreboardManager().applyVisibility(player);
                sendToggleMessage(player, "Scoreboard Visibility", data.isScoreboardVisible());
            }
            case "AUTO_CONFIRM_TPAS" -> {
                boolean enabled = !(data.isTpauto() && data.isAutoTpaHereEnabled());
                data.setTpauto(enabled);
                data.setAutoTpaHereEnabled(enabled);
                if (enabled) {
                    plugin.getTPAManager().processQueuedAutoRequests(player.getUniqueId());
                }
                sendToggleMessage(player, "Auto-Confirm TPAs", enabled);
            }
            case "NOTIFICATION_SOUNDS" -> toggle(player, "Notification Sounds",
                    !data.isNotificationSoundsEnabled(), data::setNotificationSoundsEnabled);
            case "RTP_COORDINATES" -> toggle(player, "RTP Coordinates",
                    !data.isRtpCoordinatesEnabled(), data::setRtpCoordinatesEnabled);
            case "ORDER_NOTIFICATIONS" -> toggle(player, "Order Notifications",
                    !data.isOrderNotificationsEnabled(), data::setOrderNotificationsEnabled);
            case "DUEL_REQUESTS" -> toggle(player, "Duel Requests",
                    !data.isDuelRequestsEnabled(), data::setDuelRequestsEnabled);
            case "TPA_REQUESTS" -> {
                data.setTpaRequestsChoice(nextThreeChoice(data.getTpaRequestsChoice()));
                sendChoiceMessage(player, "TPA Requests", formatThreeChoice(data.getTpaRequestsChoice()));
            }
            case "TEAM_INVITES" -> toggle(player, "Team Invites",
                    !data.isTeamInvitesEnabled(), data::setTeamInvitesEnabled);
            case "PAYMENTS" -> {
                data.setPaymentsChoice(nextThreeChoice(data.getPaymentsChoice()));
                sendChoiceMessage(player, "Payments", formatThreeChoice(data.getPaymentsChoice()));
            }
            case "TEAM_CHAT_VISIBILITY" -> toggle(player, "Team Chat Visibility",
                    !data.isTeamChatVisible(), data::setTeamChatVisible);
            case "WORTH_DISPLAY" -> {
                data.setWorthDisplayEnabled(!data.isWorthDisplayEnabled());
                if (data.isWorthDisplayEnabled()) {
                    plugin.getWorthManager().syncWorthDisplay(player);
                } else {
                    plugin.getWorthManager().clearWorthDisplay(player);
                }
                sendToggleMessage(player, "Worth Display", data.isWorthDisplayEnabled());
            }
            case "DUEL_MUSIC" -> toggle(player, "Duel Music",
                    !data.isDuelMusicEnabled(), data::setDuelMusicEnabled);
            case "QUIET_SPAWN" -> toggle(player, "Quiet Spawn Teleportation",
                    !data.isQuietSpawnEnabled(), data::setQuietSpawnEnabled);
            case "CLEAR_ENTITIES_MESSAGES" -> toggle(player, "Clear Entities Messages",
                    !data.isClearEntitiesMessagesEnabled(), data::setClearEntitiesMessagesEnabled);
            case "AMETHYST_BREAK_MESSAGES" -> toggle(player, "Amethyst Break Messages",
                    !data.isAmethystBreakMessagesEnabled(), data::setAmethystBreakMessagesEnabled);
            case "KEY_ALL_NOTIFICATIONS" -> toggle(player, "Key-All Notifications",
                    !data.isKeyAllNotificationsEnabled(), data::setKeyAllNotificationsEnabled);
            case "TPA_CONFIRM_MENUS" -> toggle(player, "TPA Confirmation Menus",
                    !data.isTpaConfirmMenuEnabled(), data::setTpaConfirmMenuEnabled);
            case "LUNAR_TEAMMATES" -> toggle(player, "Lunar Teammates",
                    !data.isLunarTeammatesEnabled(), data::setLunarTeammatesEnabled);
            case "TPA_HERE_REQUESTS" -> {
                data.setTpaHereRequestsChoice(nextThreeChoice(data.getTpaHereRequestsChoice()));
                sendChoiceMessage(player, "TPA Here Requests", formatThreeChoice(data.getTpaHereRequestsChoice()));
            }
            case "DISABLE_PHANTOM_SPAWN" -> {
                data.setPhantomEnabled(!data.isPhantomEnabled());
                if (!data.isPhantomEnabled()) {
                    long limitSeconds = plugin.getConfigManager().getConfig().getLong("SETTINGS.DISABLE-PHANTOM-SPAWN-LIMIT-SECONDS", -1L);
                    if (limitSeconds > 0) {
                        data.setPhantomDisabledUntil(System.currentTimeMillis() + (limitSeconds * 1000L));
                    } else {
                        data.setPhantomDisabledUntil(0L);
                    }
                } else {
                    data.setPhantomDisabledUntil(0L);
                }
                sendToggleMessage(player, "Phantom Spawn Prevention", !data.isPhantomEnabled());
            }
            case "PAY_CONFIRM_MENUS" -> toggle(player, "Pay Confirmation Menus",
                    !data.isPayConfirmMenuEnabled(), data::setPayConfirmMenuEnabled);
            case "TEAM_CHAT" -> toggleTeamChat(player);
            case "DESTROY_PEARL_ON_DEATH" -> toggle(player, "Destroy Pearl on Death",
                    !data.isDestroyPearlOnDeath(), data::setDestroyPearlOnDeath);
            case "RANDOMIZED_COORDS" -> {
                boolean nextVal = !data.isRandomizedCoords();
                data.setRandomizedCoords(nextVal);
                plugin.getDatabaseManager().savePlayer(data);
                player.kickPlayer(ColorUtils.colorize("&cThe setting has been changed. Please rejoin."));
            }
            case "DEATH_MESSAGES" -> {
                data.setDeathMessagesChoice(nextTwoChoice(data.getDeathMessagesChoice()));
                sendChoiceMessage(player, "Death Messages", formatTwoChoice(data.getDeathMessagesChoice()));
            }
            case "ADVANCEMENT_MESSAGES" -> {
                data.setAdvancementMessagesChoice(nextThreeChoice(data.getAdvancementMessagesChoice()));
                sendChoiceMessage(player, "Advancement Messages", formatThreeChoice(data.getAdvancementMessagesChoice()));
            }
            case "JOIN_LEAVE_MESSAGES" -> {
                data.setJoinLeaveMessagesChoice(nextThreeChoice(data.getJoinLeaveMessagesChoice()));
                sendChoiceMessage(player, "Join/Leave Messages", formatThreeChoice(data.getJoinLeaveMessagesChoice()));
            }
            case "TELEPORT_ALERTS" -> toggle(player, "Teleport Alerts",
                    !data.isTeleportAlertsEnabled(), data::setTeleportAlertsEnabled);
            case "FOLLOW_ALERT_SETTINGS" -> toggle(player, "Follow Alerts",
                    !data.isFollowAlertsEnabled(), data::setFollowAlertsEnabled);
            case "EXPLOSION_SOUNDS" -> toggle(player, "Explosion Sounds",
                    !data.isExplosionSoundsEnabled(), data::setExplosionSoundsEnabled);
            case "DISPLAY_DONUT_PLUS" -> toggle(player, "Display Donut+",
                    !data.isDisplayDonutPlusEnabled(), data::setDisplayDonutPlusEnabled);
            case "NIGHT_VISION" -> {
                boolean enabled = com.bx.ultimateDonutSmp.utils.NightVisionUtils.toggle(plugin, player);
                sendToggleMessage(player, "Night Vision", enabled);
            }
            default -> {
                return;
            }
        }

        build(player);
    }

    private void renderButton(Player player, PlayerData data, String key, ConfigurationSection section) {
        int slot = section.getInt("SLOT", -1);
        if (slot < 0 || slot >= inventory.getSize()) {
            return;
        }

        ButtonState state = buttonState(player, data, key);
        List<String> lore = new ArrayList<>();
        for (String line : section.getStringList("LORE")) {
            lore.add(line.replace("{status}", state.status()));
        }
        Material material = ItemUtils.parseMaterial(section.getString("MATERIAL", "STONE"));
        ItemStack item = ItemUtils.createItem(
                material,
                section.getString("DISPLAY-NAME", "&fѕᴇᴛᴛɪɴɢ"),
                lore
        );
        if ("NIGHT_VISION".equals(key)) {
            item = toNightVisionPotion(item);
        }
        set(slot, item);
        if (state.clickable()) {
            clickableButtons.put(slot, key);
        }
    }

    private ItemStack toNightVisionPotion(ItemStack item) {
        if (!(item.getItemMeta() instanceof org.bukkit.inventory.meta.PotionMeta meta)) {
            return item;
        }
        meta.setBasePotionType(org.bukkit.potion.PotionType.NIGHT_VISION);
        item.setItemMeta(meta);
        return item;
    }

    private ButtonState buttonState(Player player, PlayerData data, String key) {
        return switch (key) {
            case "PUBLIC_CHAT" -> state(data.isPublicChatEnabled());
            case "PRIVATE_MESSAGES" -> new ButtonState(formatThreeChoice(data.getPrivateMessagesChoice()), true);
            case "SERVER_BROADCASTS" -> state(data.isServerBroadcastsEnabled());
            case "HOTBAR_MESSAGES" -> state(data.isHotbarMessagesEnabled());
            case "PAY_ALERTS" -> state(data.isPayAlertsEnabled());
            case "BOUNTY_ALERTS" -> state(data.isBountyAlertsEnabled());
            case "AUCTION_NOTIFICATIONS" -> state(data.isAuctionNotificationsEnabled());
            case "FAST_CRYSTALS" -> state(data.isFastCrystalsEnabled());
            case "TOTEM_PARTICLES" -> state(data.isTotemParticlesEnabled());
            case "EXPLOSION_PARTICLES" -> explosionState(data);
            case "QUICK_AUCTION_PURCHASE" -> quickBuyState(player);
            case "QUICK_AUCTION_SELL" -> quickSellState(player);
            case "CHAINMAIL_ON_RESPAWN" -> state(data.isChainmailOnRespawnEnabled());
            case "DISABLE_MOB_SPAWN" -> {
                boolean disabled = !data.isMobSpawnEnabled();
                if (disabled && data.getMobSpawnDisabledUntil() > 0) {
                    long remainingSecs = (data.getMobSpawnDisabledUntil() - System.currentTimeMillis()) / 1000L;
                    if (remainingSecs > 0) {
                        yield new ButtonState("&aEnabled &7(" + com.bx.ultimateDonutSmp.utils.NumberUtils.formatTime(remainingSecs) + " left)", true);
                    }
                }
                yield state(disabled);
            }
            case "HIDE_ALL_PLAYERS" -> state(data.isHideAllPlayersEnabled());
            case "SCOREBOARD_VISIBILITY" -> state(data.isScoreboardVisible());
            case "AUTO_CONFIRM_TPAS" -> state(data.isTpauto() && data.isAutoTpaHereEnabled());
            case "NOTIFICATION_SOUNDS" -> state(data.isNotificationSoundsEnabled());
            case "RTP_COORDINATES" -> state(data.isRtpCoordinatesEnabled());
            case "ORDER_NOTIFICATIONS" -> state(data.isOrderNotificationsEnabled());
            case "DUEL_REQUESTS" -> state(data.isDuelRequestsEnabled());
            case "TPA_REQUESTS" -> new ButtonState(formatThreeChoice(data.getTpaRequestsChoice()), true);
            case "TEAM_INVITES" -> state(data.isTeamInvitesEnabled());
            case "PAYMENTS" -> new ButtonState(formatThreeChoice(data.getPaymentsChoice()), true);
            case "TEAM_CHAT_VISIBILITY" -> state(data.isTeamChatVisible());
            case "WORTH_DISPLAY" -> state(data.isWorthDisplayEnabled());
            case "DUEL_MUSIC" -> state(data.isDuelMusicEnabled());
            case "QUIET_SPAWN" -> state(data.isQuietSpawnEnabled());
            case "CLEAR_ENTITIES_MESSAGES" -> state(data.isClearEntitiesMessagesEnabled());
            case "AMETHYST_BREAK_MESSAGES" -> state(data.isAmethystBreakMessagesEnabled());
            case "KEY_ALL_NOTIFICATIONS" -> state(data.isKeyAllNotificationsEnabled());
            case "TPA_CONFIRM_MENUS" -> state(data.isTpaConfirmMenuEnabled());
            case "LUNAR_TEAMMATES" -> state(data.isLunarTeammatesEnabled());
            case "TPA_HERE_REQUESTS" -> new ButtonState(formatThreeChoice(data.getTpaHereRequestsChoice()), true);
            case "DISABLE_PHANTOM_SPAWN" -> {
                boolean disabled = !data.isPhantomEnabled();
                if (disabled && data.getPhantomDisabledUntil() > 0) {
                    long remainingSecs = (data.getPhantomDisabledUntil() - System.currentTimeMillis()) / 1000L;
                    if (remainingSecs > 0) {
                        yield new ButtonState("&aEnabled &7(" + com.bx.ultimateDonutSmp.utils.NumberUtils.formatTime(remainingSecs) + " left)", true);
                    }
                }
                yield state(disabled);
            }
            case "PAY_CONFIRM_MENUS" -> state(data.isPayConfirmMenuEnabled());
            case "TEAM_CHAT" -> state(plugin.getTeamManager().isTeamChatEnabled(player.getUniqueId()));
            case "DESTROY_PEARL_ON_DEATH" -> state(data.isDestroyPearlOnDeath());
            case "RANDOMIZED_COORDS" -> state(data.isRandomizedCoords());
            case "DEATH_MESSAGES" -> new ButtonState(formatTwoChoice(data.getDeathMessagesChoice()), true);
            case "ADVANCEMENT_MESSAGES" -> new ButtonState(formatThreeChoice(data.getAdvancementMessagesChoice()), true);
            case "JOIN_LEAVE_MESSAGES" -> new ButtonState(formatThreeChoice(data.getJoinLeaveMessagesChoice()), true);
            case "TELEPORT_ALERTS" -> state(data.isTeleportAlertsEnabled());
            case "FOLLOW_ALERT_SETTINGS" -> state(data.isFollowAlertsEnabled());
            case "EXPLOSION_SOUNDS" -> state(data.isExplosionSoundsEnabled());
            case "DISPLAY_DONUT_PLUS" -> state(data.isDisplayDonutPlusEnabled());
            case "NIGHT_VISION" -> state(com.bx.ultimateDonutSmp.utils.NightVisionUtils.isEnabled(plugin, player));
            default -> new ButtonState("", false);
        };
    }

    private ButtonState explosionState(PlayerData data) {
        return plugin.getExplosionParticleFilter() != null
                && plugin.getExplosionParticleFilter().isAvailable()
                ? state(data.isExplosionParticlesEnabled())
                : new ButtonState("&cᴜɴᴀᴠᴀɪʟᴀʙʟᴇ", false);
    }

    private ButtonState quickBuyState(Player player) {
        if (!PermissionUtils.has(player, "ultimatedonutsmp.auctionhouse.fastbuy")
                && !PermissionUtils.has(player, "donutauction.fastbuy")) {
            return new ButtonState("&cɴᴏ ᴘᴇʀᴍɪѕѕɪᴏɴ", false);
        }
        if (preferenceLoading || quickBuyEnabled == null) {
            return new ButtonState("&eʟᴏᴀᴅɪɴɢ...", false);
        }
        return state(quickBuyEnabled);
    }

    private ButtonState quickSellState(Player player) {
        if (!PermissionUtils.has(player, "ultimatedonutsmp.auctionhouse.fastsell")
                && !PermissionUtils.has(player, "donutauction.fastsell")) {
            return new ButtonState("&cɴᴏ ᴘᴇʀᴍɪѕѕɪᴏɴ", false);
        }
        if (preferenceLoading || quickSellEnabled == null) {
            return new ButtonState("&eʟᴏᴀᴅɪɴɢ...", false);
        }
        return state(quickSellEnabled);
    }

    private void loadPreference(Player player) {
        UUID playerId = player.getUniqueId();
        if (!playerId.equals(preferencePlayerId)) {
            preferencePlayerId = playerId;
            quickBuyEnabled = null;
            quickSellEnabled = null;
            preferenceLoading = false;
        }
        if (quickBuyEnabled != null || quickSellEnabled != null || preferenceLoading
                || plugin.getAuctionHouseManager() == null) {
            return;
        }
        preferenceLoading = true;
        plugin.getAuctionHouseManager().getPreferenceAsync(playerId).whenComplete((preference, error) ->
                runPlayer(player, () -> {
                    preferenceLoading = false;
                    quickBuyEnabled = error == null && preference != null
                            ? preference.fastBuyEnabled()
                            : Boolean.FALSE;
                    quickSellEnabled = error == null && preference != null
                            ? preference.fastSellEnabled()
                            : Boolean.FALSE;
                    rebuildIfOpen(player);
                }));
    }

    private void toggleQuickBuy(Player player) {
        if (quickBuyEnabled == null || plugin.getAuctionHouseManager() == null) {
            return;
        }
        boolean previous = quickBuyEnabled;
        boolean enabled = !previous;
        quickBuyEnabled = enabled;
        plugin.getAuctionHouseManager().getPreferenceAsync(player.getUniqueId())
                .thenCompose(preference -> {
                    preference.fastBuyEnabled(enabled);
                    return plugin.getAuctionHouseManager().savePreference(preference);
                })
                .whenComplete((ignored, error) -> runPlayer(player, () -> {
                    if (error != null) {
                        quickBuyEnabled = previous;
                        player.sendMessage(ColorUtils.toComponent(
                                "&cᴜɴᴀʙʟᴇ ᴛᴏ ѕᴀᴠᴇ ǫᴜɪᴄᴋ ᴀᴜᴄᴛɪᴏɴ ᴘᴜʀᴄʜᴀѕᴇ ѕᴇᴛᴛɪɴɢ."
                        ));
                    } else {
                        sendToggleMessage(player, "ǫᴜɪᴄᴋ ᴀᴜᴄᴛɪᴏɴ ᴘᴜʀᴄʜᴀѕᴇѕ", enabled);
                    }
                    rebuildIfOpen(player);
                }));
    }

    private void toggleQuickSell(Player player) {
        if (quickSellEnabled == null || plugin.getAuctionHouseManager() == null) {
            return;
        }
        boolean previous = quickSellEnabled;
        boolean enabled = !previous;
        quickSellEnabled = enabled;
        plugin.getAuctionHouseManager().getPreferenceAsync(player.getUniqueId())
                .thenCompose(preference -> {
                    preference.fastSellEnabled(enabled);
                    return plugin.getAuctionHouseManager().savePreference(preference);
                })
                .whenComplete((ignored, error) -> runPlayer(player, () -> {
                    if (error != null) {
                        quickSellEnabled = previous;
                        player.sendMessage(ColorUtils.toComponent(
                                "&cᴜɴᴀʙʟᴇ ᴛᴏ ѕᴀᴠᴇ ǫᴜɪᴄᴋ ᴀᴜᴄᴛɪᴏɴ ѕᴇʟʟ ѕᴇᴛᴛɪɴɢ."
                        ));
                    } else {
                        sendToggleMessage(player, "ǫᴜɪᴄᴋ ᴀᴜᴄᴛɪᴏɴ ѕᴇʟʟѕ", enabled);
                    }
                    rebuildIfOpen(player);
                }));
    }

    private void toggleTeamChat(Player player) {
        var team = plugin.getTeamManager().getTeam(player);
        if (team == null) {
            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage("TEAM.NO-TEAM")));
            return;
        }
        if (!plugin.getTeamManager().canUseTeamChat(team, player.getUniqueId())) {
            player.sendMessage(ColorUtils.toComponent(
                    plugin.getConfigManager().getMessage("TEAM.NO-TEAM-CHAT-PERMISSION")
            ));
            return;
        }
        plugin.getTeamManager().toggleTeamChat(player.getUniqueId());
        sendToggleMessage(
                player,
                "ᴛᴇᴀᴍ ᴄʜᴀᴛ ѕᴇɴᴅɪɴɢ ᴍᴏᴅᴇ",
                plugin.getTeamManager().isTeamChatEnabled(player.getUniqueId())
        );
    }

    private void clearNearbyHostileMobs(Player player) {
        double radius = plugin.getConfigManager().getConfig().getDouble("SETTINGS.MOB-SPAWN-RADIUS", 50);
        double radiusSquared = radius * radius;
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (!(entity instanceof LivingEntity living)) {
                continue;
            }
            if (!(living instanceof Monster || living instanceof org.bukkit.entity.Slime || living instanceof org.bukkit.entity.Ghast)) {
                continue;
            }
            if (living.getType() == EntityType.PHANTOM) {
                continue;
            }
            if (MobSpawnPolicy.isVanillaSpawnerMob(plugin, living)) {
                continue;
            }
            if (living.getLocation().distanceSquared(player.getLocation()) > radiusSquared) {
                continue;
            }
            living.remove();
        }
    }

    private void toggle(Player player, String label, boolean enabled, BooleanSetter setter) {
        setter.set(enabled);
        sendToggleMessage(player, label, enabled);
    }

    private void sendToggleMessage(Player player, String label, boolean enabled) {
        player.sendMessage(ColorUtils.toComponent(
                "&7" + label + " is now " + (enabled ? "&aEnabled" : "&cDisabled") + "&7."
        ));
    }

    private void sendChoiceMessage(Player player, String label, String choiceText) {
        player.sendMessage(ColorUtils.toComponent(
                "&7" + label + " is now set to " + choiceText + "&7."
        ));
    }

    private ButtonState state(boolean enabled) {
        return new ButtonState(enabled ? "&aEnabled" : "&cDisabled", true);
    }

    private String formatThreeChoice(ThreeChoice choice) {
        return switch (choice) {
            case OFF -> "&cOff";
            case ANYONE -> "&aAnyone";
            case FRIENDS_FOLLOWED -> "&dFriends/Followed";
        };
    }

    private String formatTwoChoice(TwoChoice choice) {
        return switch (choice) {
            case OFF -> "&cOff";
            case FRIENDS_FOLLOWED -> "&dFriends/Followed";
        };
    }

    private ThreeChoice nextThreeChoice(ThreeChoice current) {
        int nextOrdinal = (current.ordinal() + 1) % ThreeChoice.values().length;
        return ThreeChoice.values()[nextOrdinal];
    }

    private TwoChoice nextTwoChoice(TwoChoice current) {
        int nextOrdinal = (current.ordinal() + 1) % TwoChoice.values().length;
        return TwoChoice.values()[nextOrdinal];
    }

    private boolean shouldRenderButton(String key) {
        if (!VALID_SETTINGS.contains(key)) {
            return false;
        }
        return !"DUEL_REQUESTS".equals(key)
                || (plugin.getDuelManager() != null && plugin.getDuelManager().isEnabled());
    }

    private void rebuildIfOpen(Player player) {
        if (player.isOnline() && player.getOpenInventory().getTopInventory().getHolder() == this) {
            build(player);
        }
    }

    private void runPlayer(Player player, Runnable action) {
        plugin.getSpigotScheduler().runEntity(player, action);
    }

    @FunctionalInterface
    private interface BooleanSetter {
        void set(boolean value);
    }

    private record ButtonState(String status, boolean clickable) {
    }
}
