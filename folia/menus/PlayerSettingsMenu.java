package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.MobSpawnPolicy;
import com.bx.ultimateDonutSmp.utils.PermissionUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class PlayerSettingsMenu extends BaseMenu {

    private static final String MENU_PATH = "SETTINGS-MENU";
    private static final Map<String, Integer> LAYOUT_SLOTS = Map.ofEntries(
            Map.entry("PUBLIC_CHAT", 1),
            Map.entry("PRIVATE_MESSAGES", 2),
            Map.entry("SERVER_BROADCASTS", 3),
            Map.entry("AUCTION_NOTIFICATIONS", 4),
            Map.entry("EXPLOSION_PARTICLES", 5),
            Map.entry("QUICK_AUCTION_PURCHASE", 6),
            Map.entry("HIDE_ALL_PLAYERS", 7),
            Map.entry("NOTIFICATION_SOUNDS", 10),
            Map.entry("RTP_COORDINATES", 11),
            Map.entry("PAY_ALERTS", 12),
            Map.entry("HOTBAR_MESSAGES", 13),
            Map.entry("CLEAR_ENTITIES_MESSAGES", 14),
            Map.entry("BOUNTY_ALERTS", 15),
            Map.entry("AMETHYST_BREAK_MESSAGES", 16),
            Map.entry("KEY_ALL_NOTIFICATIONS", 19),
            Map.entry("CHAINMAIL_ON_RESPAWN", 20),
            Map.entry("SCOREBOARD_VISIBILITY", 21),
            Map.entry("TPA_CONFIRM_MENUS", 22),
            Map.entry("LUNAR_TEAMMATES", 23),
            Map.entry("TPA_REQUESTS", 24),
            Map.entry("TPA_HERE_REQUESTS", 25),
            Map.entry("TEAM_INVITES", 28),
            Map.entry("PAYMENTS", 29),
            Map.entry("TEAM_CHAT", 30),
            Map.entry("DISABLE_MOB_SPAWN", 31),
            Map.entry("DISABLE_PHANTOM_SPAWN", 32),
            Map.entry("PAY_CONFIRM_MENUS", 33),
            Map.entry("AUTO_CONFIRM_TPAS", 34),
            Map.entry("FAST_CRYSTALS", 37),
            Map.entry("WORTH_DISPLAY", 38),
            Map.entry("TOTEM_PARTICLES", 39),
            Map.entry("DUEL_REQUESTS", 40),
            Map.entry("ORDER_NOTIFICATIONS", 41),
            Map.entry("TEAM_CHAT_VISIBILITY", 42),
            Map.entry("DUEL_MUSIC", 43),
            Map.entry("QUIET_SPAWN", 46)
    );
    private final Map<Integer, String> clickableButtons = new HashMap<>();
    private UUID quickPurchasePlayerId;
    private Boolean quickPurchaseEnabled;
    private boolean quickPurchaseLoading;

    public PlayerSettingsMenu(UltimateDonutSmp plugin) {
        super(plugin,
                plugin.getConfigManager().getMenus().getString(MENU_PATH + ".TITLE", "&8ѕᴇᴛᴛɪɴɢѕ"),
                plugin.getConfigManager().getMenus().getInt(MENU_PATH + ".SIZE", 54));
    }

    @Override
    public void build(Player player) {
        clear();
        clickableButtons.clear();
        PlayerData data = plugin.getPlayerDataManager().get(player);
        if (data == null) return;

        ConfigurationSection buttons = plugin.getConfigManager().getMenus()
                .getConfigurationSection(MENU_PATH + ".BUTTONS");
        if (buttons == null) return;
        if (buttons.contains("QUICK_AUCTION_PURCHASE")) loadQuickPurchase(player);

        for (String key : buttons.getKeys(false)) {
            if (!shouldRenderButton(key)) continue;
            ConfigurationSection section = buttons.getConfigurationSection(key);
            if (section != null) renderButton(player, data, key, section);
        }
    }

    @Override
    public void handleClick(int slot, Player player) {
        String key = clickableButtons.get(slot);
        if (key == null) return;
        PlayerData data = plugin.getPlayerDataManager().get(player);
        if (data == null) return;

        SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
        switch (key) {
            case "PUBLIC_CHAT" -> toggle(player, "ᴘᴜʙʟɪᴄ ᴄʜᴀᴛ", !data.isPublicChatEnabled(), data::setPublicChatEnabled);
            case "PRIVATE_MESSAGES" -> toggle(player, "ᴘʀɪᴠᴀᴛᴇ ᴍᴇѕѕᴀɢᴇѕ", !data.isPrivateMessagesEnabled(), data::setPrivateMessagesEnabled);
            case "SERVER_BROADCASTS" -> toggle(player, "ѕᴇʀᴠᴇʀ ʙʀᴏᴀᴅᴄᴀѕᴛѕ", !data.isServerBroadcastsEnabled(), data::setServerBroadcastsEnabled);
            case "HOTBAR_MESSAGES" -> toggle(player, "ʜᴏᴛʙᴀʀ ɴᴏᴛɪꜰɪᴄᴀᴛɪᴏɴѕ", !data.isHotbarMessagesEnabled(), data::setHotbarMessagesEnabled);
            case "PAY_ALERTS" -> toggle(player, "ᴘᴀʏ ᴀʟᴇʀᴛѕ", !data.isPayAlertsEnabled(), data::setPayAlertsEnabled);
            case "BOUNTY_ALERTS" -> toggle(player, "ʙᴏᴜɴᴛʏ ᴀʟᴇʀᴛѕ", !data.isBountyAlertsEnabled(), data::setBountyAlertsEnabled);
            case "AUCTION_NOTIFICATIONS" -> toggle(player, "ᴀᴜᴄᴛɪᴏɴ ɴᴏᴛɪꜰɪᴄᴀᴛɪᴏɴѕ", !data.isAuctionNotificationsEnabled(), data::setAuctionNotificationsEnabled);
            case "FAST_CRYSTALS" -> {
                data.setFastCrystalsEnabled(!data.isFastCrystalsEnabled());
                plugin.getFastCrystalManager().applyCrystalCooldown(player);
                sendToggleMessage(player, "ꜰᴀѕᴛ ᴄʀʏѕᴛᴀʟѕ", data.isFastCrystalsEnabled());
            }
            case "TOTEM_PARTICLES" -> toggle(player, "ᴛᴏᴛᴇᴍ ᴘᴀʀᴛɪᴄʟᴇѕ", !data.isTotemParticlesEnabled(), data::setTotemParticlesEnabled);
            case "EXPLOSION_PARTICLES" -> toggle(player, "ᴇxᴘʟᴏѕɪᴏɴ ᴘᴀʀᴛɪᴄʟᴇѕ", !data.isExplosionParticlesEnabled(), data::setExplosionParticlesEnabled);
            case "QUICK_AUCTION_PURCHASE" -> toggleQuickPurchase(player);
            case "CHAINMAIL_ON_RESPAWN" -> toggle(player, "ᴀᴜᴛᴏᴍᴀᴛɪᴄ ʀᴇѕᴘᴀᴡɴ ᴋɪᴛ", !data.isChainmailOnRespawnEnabled(), data::setChainmailOnRespawnEnabled);
            case "DISABLE_MOB_SPAWN" -> {
                data.setMobSpawnEnabled(!data.isMobSpawnEnabled());
                if (!data.isMobSpawnEnabled()) clearNearbyHostileMobs(player);
                sendToggleMessage(player, "ɴᴇᴀʀʙʏ ᴍᴏʙ ѕᴘᴀᴡɴ ᴘʀᴇᴠᴇɴᴛɪᴏɴ", !data.isMobSpawnEnabled());
            }
            case "HIDE_ALL_PLAYERS" -> {
                data.setHideAllPlayersEnabled(!data.isHideAllPlayersEnabled());
                plugin.getPlayerVisibilityManager().applyViewerPreference(player);
                sendToggleMessage(player, "ʜɪᴅᴇ ᴀʟʟ ᴘʟᴀʏᴇʀѕ", data.isHideAllPlayersEnabled());
            }
            case "SCOREBOARD_VISIBILITY" -> {
                data.setScoreboardVisible(!data.isScoreboardVisible());
                plugin.getScoreboardManager().applyVisibility(player);
                sendToggleMessage(player, "ѕᴄᴏʀᴇʙᴏᴀʀᴅ ᴠɪѕɪʙɪʟɪᴛʏ", data.isScoreboardVisible());
            }
            case "AUTO_CONFIRM_TPAS" -> {
                boolean enabled = !(data.isTpauto() && data.isAutoTpaHereEnabled());
                data.setTpauto(enabled);
                data.setAutoTpaHereEnabled(enabled);
                if (enabled) plugin.getTPAManager().processQueuedAutoRequests(player.getUniqueId());
                sendToggleMessage(player, "ᴀᴜᴛᴏ-ᴄᴏɴꜰɪʀᴍ ᴛᴘᴀѕ", enabled);
            }
            case "NOTIFICATION_SOUNDS" -> toggle(player, "ɴᴏᴛɪꜰɪᴄᴀᴛɪᴏɴ ѕᴏᴜɴᴅѕ", !data.isNotificationSoundsEnabled(), data::setNotificationSoundsEnabled);
            case "RTP_COORDINATES" -> toggle(player, "ʀᴛᴘ ᴄᴏᴏʀᴅɪɴᴀᴛᴇѕ", !data.isRtpCoordinatesEnabled(), data::setRtpCoordinatesEnabled);
            case "ORDER_NOTIFICATIONS" -> toggle(player, "ᴏʀᴅᴇʀ ɴᴏᴛɪꜰɪᴄᴀᴛɪᴏɴѕ", !data.isOrderNotificationsEnabled(), data::setOrderNotificationsEnabled);
            case "DUEL_REQUESTS" -> toggle(player, "ᴅᴜᴇʟ ʀᴇǫᴜᴇѕᴛѕ", !data.isDuelRequestsEnabled(), data::setDuelRequestsEnabled);
            case "TPA_REQUESTS" -> toggle(player, "ᴛᴘᴀ ʀᴇǫᴜᴇѕᴛѕ", !data.isTpaRequestsEnabled(), data::setTpaRequestsEnabled);
            case "TEAM_INVITES" -> toggle(player, "ᴛᴇᴀᴍ ɪɴᴠɪᴛᴇѕ", !data.isTeamInvitesEnabled(), data::setTeamInvitesEnabled);
            case "PAYMENTS" -> toggle(player, "Payments", !data.isPaymentsEnabled(), data::setPaymentsEnabled);
            case "TEAM_CHAT_VISIBILITY" -> toggle(player, "ᴛᴇᴀᴍ ᴄʜᴀᴛ ᴠɪѕɪʙɪʟɪᴛʏ", !data.isTeamChatVisible(), data::setTeamChatVisible);
            case "WORTH_DISPLAY" -> {
                data.setWorthDisplayEnabled(!data.isWorthDisplayEnabled());
                if (data.isWorthDisplayEnabled()) plugin.getWorthManager().syncWorthDisplay(player);
                else plugin.getWorthManager().clearWorthDisplay(player);
                sendToggleMessage(player, "ᴡᴏʀᴛʜ ᴅɪѕᴘʟᴀʏ", data.isWorthDisplayEnabled());
            }
            case "DUEL_MUSIC" -> toggle(player, "ᴅᴜᴇʟ ᴍᴜѕɪᴄ", !data.isDuelMusicEnabled(), data::setDuelMusicEnabled);
            case "QUIET_SPAWN" -> toggle(player, "ǫᴜɪᴇᴛ ѕᴘᴀᴡɴ ᴛᴇʟᴇᴘᴏʀᴛᴀᴛɪᴏɴ", !data.isQuietSpawnEnabled(), data::setQuietSpawnEnabled);
            case "CLEAR_ENTITIES_MESSAGES" -> toggle(player, "ᴄʟᴇᴀʀ ᴇɴᴛɪᴛɪᴇѕ ᴍᴇѕѕᴀɢᴇѕ", !data.isClearEntitiesMessagesEnabled(), data::setClearEntitiesMessagesEnabled);
            case "AMETHYST_BREAK_MESSAGES" -> toggle(player, "ᴀᴍᴇᴛʜʏѕᴛ ʙʀᴇᴀᴋ ᴍᴇѕѕᴀɢᴇѕ", !data.isAmethystBreakMessagesEnabled(), data::setAmethystBreakMessagesEnabled);
            case "KEY_ALL_NOTIFICATIONS" -> toggle(player, "ᴋᴇʏ-ᴀʟʟ ɴᴏᴛɪꜰɪᴄᴀᴛɪᴏɴѕ", !data.isKeyAllNotificationsEnabled(), data::setKeyAllNotificationsEnabled);
            case "TPA_CONFIRM_MENUS" -> toggle(player, "ᴛᴘᴀ ᴄᴏɴꜰɪʀᴍᴀᴛɪᴏɴ ᴍᴇɴᴜѕ", !data.isTpaConfirmMenuEnabled(), data::setTpaConfirmMenuEnabled);
            case "LUNAR_TEAMMATES" -> toggle(player, "ʟᴜɴᴀʀ ᴛᴇᴀᴍᴍᴀᴛᴇѕ", !data.isLunarTeammatesEnabled(), data::setLunarTeammatesEnabled);
            case "TPA_HERE_REQUESTS" -> toggle(player, "ᴛᴘᴀ-ʜᴇʀᴇ ʀᴇǫᴜᴇѕᴛѕ", !data.isTpaHereRequestsEnabled(), data::setTpaHereRequestsEnabled);
            case "DISABLE_PHANTOM_SPAWN" -> {
                data.setPhantomEnabled(!data.isPhantomEnabled());
                sendToggleMessage(player, "ᴘʜᴀɴᴛᴏᴍ ѕᴘᴀᴡɴ ᴘʀᴇᴠᴇɴᴛɪᴏɴ", !data.isPhantomEnabled());
            }
            case "PAY_CONFIRM_MENUS" -> toggle(player, "ᴘᴀʏ ᴄᴏɴꜰɪʀᴍᴀᴛɪᴏɴ ᴍᴇɴᴜѕ", !data.isPayConfirmMenuEnabled(), data::setPayConfirmMenuEnabled);
            case "TEAM_CHAT" -> toggleTeamChat(player);
            default -> { return; }
        }
        build(player);
    }

    private void renderButton(Player player, PlayerData data, String key, ConfigurationSection section) {
        int slot = LAYOUT_SLOTS.getOrDefault(key, -1);
        if (slot < 0 || slot >= inventory.getSize()) return;
        ButtonState state = buttonState(player, data, key);
        List<String> lore = new ArrayList<>();
        for (String line : section.getStringList("LORE")) {
            lore.add(line.replace("{status}", state.status()));
        }
        Material material = ItemUtils.parseMaterial(section.getString("MATERIAL", "STONE"));
        ItemStack item = ItemUtils.createItem(material,
                section.getString("DISPLAY-NAME", "&fѕᴇᴛᴛɪɴɢ"), lore);
        set(slot, item);
        if (state.clickable()) clickableButtons.put(slot, key);
    }

    private ButtonState buttonState(Player player, PlayerData data, String key) {
        return switch (key) {
            case "PUBLIC_CHAT" -> state(data.isPublicChatEnabled());
            case "PRIVATE_MESSAGES" -> state(data.isPrivateMessagesEnabled());
            case "SERVER_BROADCASTS" -> state(data.isServerBroadcastsEnabled());
            case "HOTBAR_MESSAGES" -> state(data.isHotbarMessagesEnabled());
            case "PAY_ALERTS" -> state(data.isPayAlertsEnabled());
            case "BOUNTY_ALERTS" -> state(data.isBountyAlertsEnabled());
            case "AUCTION_NOTIFICATIONS" -> state(data.isAuctionNotificationsEnabled());
            case "FAST_CRYSTALS" -> state(data.isFastCrystalsEnabled());
            case "TOTEM_PARTICLES" -> state(data.isTotemParticlesEnabled());
            case "EXPLOSION_PARTICLES" -> explosionState(data);
            case "QUICK_AUCTION_PURCHASE" -> quickPurchaseState(player);
            case "CHAINMAIL_ON_RESPAWN" -> state(data.isChainmailOnRespawnEnabled());
            case "DISABLE_MOB_SPAWN" -> state(!data.isMobSpawnEnabled());
            case "HIDE_ALL_PLAYERS" -> state(data.isHideAllPlayersEnabled());
            case "SCOREBOARD_VISIBILITY" -> state(data.isScoreboardVisible());
            case "AUTO_CONFIRM_TPAS" -> state(data.isTpauto() && data.isAutoTpaHereEnabled());
            case "NOTIFICATION_SOUNDS" -> state(data.isNotificationSoundsEnabled());
            case "RTP_COORDINATES" -> state(data.isRtpCoordinatesEnabled());
            case "ORDER_NOTIFICATIONS" -> state(data.isOrderNotificationsEnabled());
            case "DUEL_REQUESTS" -> state(data.isDuelRequestsEnabled());
            case "TPA_REQUESTS" -> state(data.isTpaRequestsEnabled());
            case "TEAM_INVITES" -> state(data.isTeamInvitesEnabled());
            case "PAYMENTS" -> state(data.isPaymentsEnabled());
            case "TEAM_CHAT_VISIBILITY" -> state(data.isTeamChatVisible());
            case "WORTH_DISPLAY" -> state(data.isWorthDisplayEnabled());
            case "DUEL_MUSIC" -> state(data.isDuelMusicEnabled());
            case "QUIET_SPAWN" -> state(data.isQuietSpawnEnabled());
            case "CLEAR_ENTITIES_MESSAGES" -> state(data.isClearEntitiesMessagesEnabled());
            case "AMETHYST_BREAK_MESSAGES" -> state(data.isAmethystBreakMessagesEnabled());
            case "KEY_ALL_NOTIFICATIONS" -> state(data.isKeyAllNotificationsEnabled());
            case "TPA_CONFIRM_MENUS" -> state(data.isTpaConfirmMenuEnabled());
            case "LUNAR_TEAMMATES" -> state(data.isLunarTeammatesEnabled());
            case "TPA_HERE_REQUESTS" -> state(data.isTpaHereRequestsEnabled());
            case "DISABLE_PHANTOM_SPAWN" -> state(!data.isPhantomEnabled());
            case "PAY_CONFIRM_MENUS" -> state(data.isPayConfirmMenuEnabled());
            case "TEAM_CHAT" -> state(plugin.getTeamManager().isTeamChatEnabled(player.getUniqueId()));
            default -> new ButtonState("", false);
        };
    }

    private ButtonState explosionState(PlayerData data) {
        return plugin.getExplosionParticleFilter() != null
                && plugin.getExplosionParticleFilter().isAvailable()
                ? state(data.isExplosionParticlesEnabled())
                : new ButtonState("&cᴜɴᴀᴠᴀɪʟᴀʙʟᴇ", false);
    }

    private ButtonState quickPurchaseState(Player player) {
        if (!PermissionUtils.has(player, "ultimatedonutsmp.auctionhouse.fastbuy")
                && !PermissionUtils.has(player, "donutauction.fastbuy")) {
            return new ButtonState("&cɴᴏ ᴘᴇʀᴍɪѕѕɪᴏɴ", false);
        }
        if (quickPurchaseLoading || quickPurchaseEnabled == null) {
            return new ButtonState("&eʟᴏᴀᴅɪɴɢ...", false);
        }
        return state(quickPurchaseEnabled);
    }

    private void loadQuickPurchase(Player player) {
        UUID playerId = player.getUniqueId();
        if (!playerId.equals(quickPurchasePlayerId)) {
            quickPurchasePlayerId = playerId;
            quickPurchaseEnabled = null;
            quickPurchaseLoading = false;
        }
        if (quickPurchaseEnabled != null || quickPurchaseLoading
                || plugin.getAuctionHouseManager() == null) return;
        quickPurchaseLoading = true;
        plugin.getAuctionHouseManager().getPreferenceAsync(playerId).whenComplete((preference, error) ->
                runPlayer(player, () -> {
                    quickPurchaseLoading = false;
                    quickPurchaseEnabled = error == null && preference != null
                            ? preference.fastBuyEnabled() : Boolean.FALSE;
                    rebuildIfOpen(player);
                }));
    }

    private void toggleQuickPurchase(Player player) {
        if (quickPurchaseEnabled == null || plugin.getAuctionHouseManager() == null) return;
        boolean previous = quickPurchaseEnabled;
        boolean enabled = !previous;
        quickPurchaseEnabled = enabled;
        plugin.getAuctionHouseManager().getPreferenceAsync(player.getUniqueId())
                .thenCompose(preference -> {
                    preference.fastBuyEnabled(enabled);
                    return plugin.getAuctionHouseManager().savePreference(preference);
                })
                .whenComplete((ignored, error) -> runPlayer(player, () -> {
                    if (error != null) {
                        quickPurchaseEnabled = previous;
                        player.sendMessage(ColorUtils.toComponent(
                                "&cᴜɴᴀʙʟᴇ ᴛᴏ ѕᴀᴠᴇ ǫᴜɪᴄᴋ ᴀᴜᴄᴛɪᴏɴ ᴘᴜʀᴄʜᴀѕᴇ ѕᴇᴛᴛɪɴɢ."));
                    } else {
                        sendToggleMessage(player, "ǫᴜɪᴄᴋ ᴀᴜᴄᴛɪᴏɴ ᴘᴜʀᴄʜᴀѕᴇѕ", enabled);
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
                    plugin.getConfigManager().getMessage("TEAM.NO-TEAM-CHAT-PERMISSION")));
            return;
        }
        plugin.getTeamManager().toggleTeamChat(player.getUniqueId());
        sendToggleMessage(player, "ᴛᴇᴀᴍ ᴄʜᴀᴛ ѕᴇɴᴅɪɴɢ ᴍᴏᴅᴇ",
                plugin.getTeamManager().isTeamChatEnabled(player.getUniqueId()));
    }

    private void clearNearbyHostileMobs(Player player) {
        double radius = plugin.getConfigManager().getConfig().getDouble("SETTINGS.MOB-SPAWN-RADIUS", 50);
        double radiusSquared = radius * radius;
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (!(entity instanceof Monster monster)
                    || monster.getType() == EntityType.PHANTOM
                    || MobSpawnPolicy.isVanillaSpawnerMob(plugin, monster)
                    || monster.getLocation().distanceSquared(player.getLocation()) > radiusSquared) continue;
            monster.remove();
        }
    }

    private void toggle(Player player, String label, boolean enabled, BooleanSetter setter) {
        setter.set(enabled);
        sendToggleMessage(player, label, enabled);
    }

    private void sendToggleMessage(Player player, String label, boolean enabled) {
        player.sendMessage(ColorUtils.toComponent(
                "&7" + label + " ɪѕ ɴᴏᴡ " + (enabled ? "&aᴇɴᴀʙʟᴇᴅ" : "&cᴅɪѕᴀʙʟᴇᴅ") + "&7."));
    }

    private ButtonState state(boolean enabled) {
        return new ButtonState(enabled ? "&aᴇɴᴀʙʟᴇᴅ" : "&cᴅɪѕᴀʙʟᴇᴅ", true);
    }

    private boolean shouldRenderButton(String key) {
        return LAYOUT_SLOTS.containsKey(key)
                && (!"DUEL_REQUESTS".equals(key)
                || (plugin.getDuelManager() != null && plugin.getDuelManager().isEnabled()));
    }

    private void rebuildIfOpen(Player player) {
        if (player.isOnline() && player.getOpenInventory().getTopInventory().getHolder() == this) build(player);
    }

    private void runPlayer(Player player, Runnable action) {
        plugin.getFoliaScheduler().runEntity(player, action);
    }

    @FunctionalInterface
    private interface BooleanSetter { void set(boolean value); }
    private record ButtonState(String status, boolean clickable) {}
}
