package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.models.ThreeChoice;
import com.bx.ultimateDonutSmp.models.TwoChoice;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.PermissionUtils;
import com.bx.ultimateDonutSmp.utils.PlayerSettingDefaults;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
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
            "TPA_CONFIRM_MENUS", "QUICK_AUCTION_PURCHASE", "DESTROY_PEARL_ON_DEATH",
            "PAY_CONFIRM_MENUS", "AUTO_CONFIRM_TPAS", "HOTBAR_MESSAGES", "NOTIFICATION_SOUNDS",
            "FOLLOW_ALERT_SETTINGS", "DISPLAY_DONUT_PLUS", "CHAINMAIL_ON_RESPAWN", "EXPLOSION_PARTICLES",
            "EXPLOSION_SOUNDS", "TELEPORT_ALERTS", "RTP_COORDINATES", "FAST_CRYSTALS", "RANDOMIZED_COORDS",
            "TOTEM_PARTICLES", "HIDE_ALL_PLAYERS", "QUIET_SPAWN", "DUEL_MUSIC", "TEAM_INVITES",
            "CLEAR_ENTITIES_MESSAGES",
            "TPA_REQUESTS", "TPA_HERE_REQUESTS", "PAYMENTS", "WORTH_DISPLAY", "MONEY_NAMETAGS",
            "JOIN_LEAVE_MESSAGES", "PAY_ALERTS", "ADVANCEMENT_MESSAGES", "AUCTION_NOTIFICATIONS",
            "AMETHYST_BREAK_MESSAGES", "DUEL_REQUESTS", "DEATH_MESSAGES", "KEY_ALL_NOTIFICATIONS",
            "QUICK_AUCTION_SELL", "ORDER_NOTIFICATIONS", "DISABLE_MOB_SPAWN", "DISABLE_PHANTOM_SPAWN",
            "NIGHT_VISION", "BOUNTY_ALERTS", "SCOREBOARD_VISIBILITY", "SHOW_MONEY", "SHOW_SHARDS",
            "SHOW_KILLS", "SHOW_DEATHS", "SHOW_PLAYTIME", "COMBAT_TIMER"
    );

    private static final Map<String, String> BUNDLED_DEFAULT_DISPLAY_NAMES = Map.ofEntries(
            Map.entry("PUBLIC_CHAT", "Public Chat"),
            Map.entry("PRIVATE_MESSAGES", "Private Messages"),
            Map.entry("SERVER_BROADCASTS", "Server Broadcasts"),
            Map.entry("HOTBAR_MESSAGES", "Hotbar Messages"),
            Map.entry("DEATH_MESSAGES", "Death Messages"),
            Map.entry("ADVANCEMENT_MESSAGES", "Advancement Messages"),
            Map.entry("JOIN_LEAVE_MESSAGES", "Join/Leave Messages"),
            Map.entry("TEAM_CHAT_VISIBILITY", "Team Chat Visibility"),
            Map.entry("AMETHYST_BREAK_MESSAGES", "Amethyst Break Messages"),
            Map.entry("PAY_ALERTS", "Pay Alerts"),
            Map.entry("TELEPORT_ALERTS", "Teleport Alerts"),
            Map.entry("BOUNTY_ALERTS", "Bounty Alerts"),
            Map.entry("AUCTION_NOTIFICATIONS", "Auction Alerts"),
            Map.entry("ORDER_NOTIFICATIONS", "Order Alerts"),
            Map.entry("NOTIFICATION_SOUNDS", "Notification Sounds"),
            Map.entry("FOLLOW_ALERT_SETTINGS", "Follow Alerts"),
            Map.entry("KEY_ALL_NOTIFICATIONS", "Key All Notifications"),
            Map.entry("RTP_COORDINATES", "RTP Coordinates"),
            Map.entry("FAST_CRYSTALS", "Fast Crystals"),
            Map.entry("CHAINMAIL_ON_RESPAWN", "Automatic Respawn Kit"),
            Map.entry("EXPLOSION_PARTICLES", "Explosion Particles"),
            Map.entry("EXPLOSION_SOUNDS", "Explosion Sounds"),
            Map.entry("COMBAT_TIMER", "Combat Timer"),
            Map.entry("DISPLAY_DONUT_PLUS", "Display Donut+"),
            Map.entry("MONEY_NAMETAGS", "Money Nametags"),
            Map.entry("WORTH_DISPLAY", "Worth Display"),
            Map.entry("TPA_CONFIRM_MENUS", "TPA Confirmation Menus"),
            Map.entry("TPA_REQUESTS", "TPA Requests"),
            Map.entry("TPA_HERE_REQUESTS", "TPA Here Requests"),
            Map.entry("PAYMENTS", "Payments"),
            Map.entry("RANDOMIZED_COORDS", "Randomized Coordinates"),
            Map.entry("DUEL_REQUESTS", "Duel Requests"),
            Map.entry("PAY_CONFIRM_MENUS", "Pay Confirmation Menus"),
            Map.entry("AUTO_CONFIRM_TPAS", "Auto-Confirm TPAs"),
            Map.entry("TEAM_INVITES", "Team Invites"),
            Map.entry("DUEL_MUSIC", "Duel Music"),
            Map.entry("SCOREBOARD_VISIBILITY", "Scoreboard Visibility"),
            Map.entry("SHOW_MONEY", "Show Money"),
            Map.entry("SHOW_SHARDS", "Show Shards"),
            Map.entry("SHOW_KILLS", "Show Kills"),
            Map.entry("SHOW_DEATHS", "Show Deaths"),
            Map.entry("SHOW_PLAYTIME", "Show Playtime"),
            Map.entry("CLEAR_ENTITIES_MESSAGES", "Clear Entities Messages"),
            Map.entry("QUICK_AUCTION_PURCHASE", "Quick Buy"),
            Map.entry("QUICK_AUCTION_SELL", "Quick Sell"),
            Map.entry("DISABLE_MOB_SPAWN", "Disable Mob Spawning"),
            Map.entry("DISABLE_PHANTOM_SPAWN", "Disable Phantom Spawning"),
            Map.entry("NIGHT_VISION", "Night Vision"),
            Map.entry("DESTROY_PEARL_ON_DEATH", "Destroy Ender Pearl On Death"),
            Map.entry("HIDE_ALL_PLAYERS", "Hide All Players"),
            Map.entry("TOTEM_PARTICLES", "Totem Particles"),
            Map.entry("QUIET_SPAWN", "Quiet Spawn Teleportation")
    );

    private final Map<Integer, String> clickableButtons = new HashMap<>();
    private UUID preferencePlayerId;
    private Boolean quickBuyEnabled;
    private Boolean quickSellEnabled;
    private boolean preferenceLoading;

    public PlayerSettingsMenu(UltimateDonutSmp plugin) {
        super(
                plugin,
                plugin.getConfigManager().getMenus().getString(MENU_PATH + ".TITLE", "&8Settings"),
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
        if (containsEnabledButton(buttons, "QUICK_AUCTION_PURCHASE")
                || containsEnabledButton(buttons, "QUICK_AUCTION_SELL")) {
            loadPreference(player);
        }

        for (String key : buttons.getKeys(false)) {
            ConfigurationSection section = buttons.getConfigurationSection(key);
            if (section == null || !shouldRenderButton(key, section)) {
                continue;
            }
            renderButton(player, data, key, section);
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

        ConfigurationSection section = plugin.getConfigManager().getMenus()
                .getConfigurationSection(MENU_PATH + ".BUTTONS." + key);
        if (!PlayerSettingDefaults.isOptionEnabled(section)) {
            return;
        }

        SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));

        if (section != null && section.contains("COMMAND")) {
            String commandStr = section.getString("COMMAND");
            if (commandStr != null && !commandStr.isBlank()) {
                commandStr = commandStr.replace("{player}", player.getName()).replace("%player%", player.getName());
                if (commandStr.toLowerCase(java.util.Locale.ROOT).startsWith("[console] ")) {
                    String cmd = commandStr.substring(10).trim();
                    plugin.getSpigotScheduler().dispatchConsoleCommand(cmd);
                } else if (commandStr.toLowerCase(java.util.Locale.ROOT).startsWith("[player] ")) {
                    String cmd = commandStr.substring(9).trim();
                    if (cmd.startsWith("/")) cmd = cmd.substring(1);
                    plugin.getSpigotScheduler().dispatchPlayerCommand(player, cmd);
                } else {
                    String cmd = commandStr.startsWith("/") ? commandStr.substring(1) : commandStr;
                    plugin.getSpigotScheduler().dispatchPlayerCommand(player, cmd);
                }
                plugin.getSpigotScheduler().runEntity(player, () -> {
                    if (player.isOnline()) {
                        build(player);
                    }
                });
                return;
            }
        }

        switch (key) {
            case "PUBLIC_CHAT" -> toggle(player, section, key, "Public Chat",
                    !data.isPublicChatEnabled(), data::setPublicChatEnabled);
            case "PRIVATE_MESSAGES" -> {
                data.setPrivateMessagesChoice(nextThreeChoice(data.getPrivateMessagesChoice()));
                sendChoiceMessage(player, section, key, "Private Messages", data.getPrivateMessagesChoice());
            }
            case "SERVER_BROADCASTS" -> toggle(player, section, key, "Server Broadcasts",
                    !data.isServerBroadcastsEnabled(), data::setServerBroadcastsEnabled);
            case "HOTBAR_MESSAGES" -> toggle(player, section, key, "Hotbar Notifications",
                    !data.isHotbarMessagesEnabled(), data::setHotbarMessagesEnabled);
            case "PAY_ALERTS" -> toggle(player, section, key, "Pay Alerts",
                    !data.isPayAlertsEnabled(), data::setPayAlertsEnabled);
            case "BOUNTY_ALERTS" -> toggle(player, section, key, "Bounty Alerts",
                    !data.isBountyAlertsEnabled(), data::setBountyAlertsEnabled);
            case "AUCTION_NOTIFICATIONS" -> toggle(player, section, key, "Auction Notifications",
                    !data.isAuctionNotificationsEnabled(), data::setAuctionNotificationsEnabled);
            case "FAST_CRYSTALS" -> {
                data.setFastCrystalsEnabled(!data.isFastCrystalsEnabled());
                plugin.getFastCrystalManager().applyCrystalCooldown(player);
                sendToggleMessage(player, section, key, "Fast Crystals", data.isFastCrystalsEnabled());
            }
            case "TOTEM_PARTICLES" -> toggle(player, section, key, "Totem Particles",
                    !data.isTotemParticlesEnabled(), data::setTotemParticlesEnabled);
            case "EXPLOSION_PARTICLES" -> toggle(player, section, key, "Explosion Particles",
                    !data.isExplosionParticlesEnabled(), data::setExplosionParticlesEnabled);
            case "QUICK_AUCTION_PURCHASE" -> toggleQuickBuy(player, section, key);
            case "QUICK_AUCTION_SELL" -> toggleQuickSell(player, section, key);
            case "CHAINMAIL_ON_RESPAWN" -> toggle(player, section, key, "Automatic Respawn Kit",
                    !data.isChainmailOnRespawnEnabled(), data::setChainmailOnRespawnEnabled);
            case "DISABLE_MOB_SPAWN" -> {
                data.setMobSpawnEnabled(!data.isMobSpawnEnabled());
                if (!data.isMobSpawnEnabled()) {
                    long limitSeconds = plugin.getConfigManager().getConfig().getLong("SETTINGS.DISABLE-MOB-SPAWN-LIMIT-SECONDS", -1L);
                    if (limitSeconds > 0) {
                        data.setMobSpawnDisabledUntil(System.currentTimeMillis() + (limitSeconds * 1000L));
                    } else {
                        data.setMobSpawnDisabledUntil(0L);
                    }
                } else {
                    data.setMobSpawnDisabledUntil(0L);
                }
                sendToggleMessage(player, section, key, "Nearby Mob Spawn Prevention", !data.isMobSpawnEnabled());
            }
            case "HIDE_ALL_PLAYERS" -> {
                data.setHideAllPlayersEnabled(!data.isHideAllPlayersEnabled());
                plugin.getPlayerVisibilityManager().applyViewerPreference(player);
                sendToggleMessage(player, section, key, "Hide All Players", data.isHideAllPlayersEnabled());
            }
            case "SCOREBOARD_VISIBILITY" -> {
                data.setScoreboardVisible(!data.isScoreboardVisible());
                plugin.getScoreboardManager().applyVisibility(player);
                sendToggleMessage(player, section, key, "Scoreboard Visibility", data.isScoreboardVisible());
            }
            case "SHOW_MONEY" -> toggleSidebarLine(player, section, key, "Show Money",
                    !data.isShowMoneyLine(), data::setShowMoneyLine);
            case "SHOW_SHARDS" -> toggleSidebarLine(player, section, key, "Show Shards",
                    !data.isShowShardsLine(), data::setShowShardsLine);
            case "SHOW_KILLS" -> toggleSidebarLine(player, section, key, "Show Kills",
                    !data.isShowKillsLine(), data::setShowKillsLine);
            case "SHOW_DEATHS" -> toggleSidebarLine(player, section, key, "Show Deaths",
                    !data.isShowDeathsLine(), data::setShowDeathsLine);
            case "SHOW_PLAYTIME" -> toggleSidebarLine(player, section, key, "Show Playtime",
                    !data.isShowPlaytimeLine(), data::setShowPlaytimeLine);
            case "COMBAT_TIMER" -> toggle(player, section, key, "Combat Timer",
                    !data.isCombatTimerEnabled(), data::setCombatTimerEnabled);
            case "AUTO_CONFIRM_TPAS" -> {
                boolean enabled = !(data.isTpauto() && data.isAutoTpaHereEnabled());
                data.setTpauto(enabled);
                data.setAutoTpaHereEnabled(enabled);
                if (enabled) {
                    plugin.getTPAManager().processQueuedAutoRequests(player.getUniqueId());
                }
                sendToggleMessage(player, section, key, "Auto-Confirm TPAs", enabled);
            }
            case "NOTIFICATION_SOUNDS" -> toggle(player, section, key, "Notification Sounds",
                    !data.isNotificationSoundsEnabled(), data::setNotificationSoundsEnabled);
            case "RTP_COORDINATES" -> toggle(player, section, key, "RTP Coordinates",
                    !data.isRtpCoordinatesEnabled(), data::setRtpCoordinatesEnabled);
            case "ORDER_NOTIFICATIONS" -> toggle(player, section, key, "Order Notifications",
                    !data.isOrderNotificationsEnabled(), data::setOrderNotificationsEnabled);
            case "DUEL_REQUESTS" -> toggle(player, section, key, "Duel Requests",
                    !data.isDuelRequestsEnabled(), data::setDuelRequestsEnabled);
            case "TPA_REQUESTS" -> {
                data.setTpaRequestsChoice(nextThreeChoice(data.getTpaRequestsChoice()));
                sendChoiceMessage(player, section, key, "TPA Requests", data.getTpaRequestsChoice());
            }
            case "TEAM_INVITES" -> toggle(player, section, key, "Team Invites",
                    !data.isTeamInvitesEnabled(), data::setTeamInvitesEnabled);
            case "PAYMENTS" -> {
                data.setPaymentsChoice(nextThreeChoice(data.getPaymentsChoice()));
                sendChoiceMessage(player, section, key, "Payments", data.getPaymentsChoice());
            }
            case "TEAM_CHAT_VISIBILITY" -> toggle(player, section, key, "Team Chat Visibility",
                    !data.isTeamChatVisible(), data::setTeamChatVisible);
            case "WORTH_DISPLAY" -> {
                data.setWorthDisplayEnabled(!data.isWorthDisplayEnabled());
                if (data.isWorthDisplayEnabled()) {
                    plugin.getWorthManager().syncWorthDisplay(player);
                } else {
                    plugin.getWorthManager().clearWorthDisplay(player);
                }
                sendToggleMessage(player, section, key, "Worth Display", data.isWorthDisplayEnabled());
            }
            case "MONEY_NAMETAGS" -> {
                data.setMoneyNametagsEnabled(!data.isMoneyNametagsEnabled());
                plugin.getMoneyNametagManager().refreshViewer(player);
                sendToggleMessage(player, section, key, "Money Nametags", data.isMoneyNametagsEnabled());
            }
            case "DUEL_MUSIC" -> toggle(player, section, key, "Duel Music",
                    !data.isDuelMusicEnabled(), data::setDuelMusicEnabled);
            case "QUIET_SPAWN" -> toggle(player, section, key, "Quiet Spawn Teleportation",
                    !data.isQuietSpawnEnabled(), data::setQuietSpawnEnabled);
            case "CLEAR_ENTITIES_MESSAGES" -> toggle(player, section, key, "Clear Entities Messages",
                    !data.isClearEntitiesMessagesEnabled(), data::setClearEntitiesMessagesEnabled);
            case "AMETHYST_BREAK_MESSAGES" -> toggle(player, section, key, "Amethyst Break Messages",
                    !data.isAmethystBreakMessagesEnabled(), data::setAmethystBreakMessagesEnabled);
            case "KEY_ALL_NOTIFICATIONS" -> toggle(player, section, key, "Key-All Notifications",
                    !data.isKeyAllNotificationsEnabled(), data::setKeyAllNotificationsEnabled);
            case "TPA_CONFIRM_MENUS" -> toggle(player, section, key, "TPA Confirmation Menus",
                    !data.isTpaConfirmMenuEnabled(), data::setTpaConfirmMenuEnabled);
            case "TPA_HERE_REQUESTS" -> {
                data.setTpaHereRequestsChoice(nextThreeChoice(data.getTpaHereRequestsChoice()));
                sendChoiceMessage(player, section, key, "TPA Here Requests", data.getTpaHereRequestsChoice());
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
                sendToggleMessage(player, section, key, "Phantom Spawn Prevention", !data.isPhantomEnabled());
            }
            case "PAY_CONFIRM_MENUS" -> toggle(player, section, key, "Pay Confirmation Menus",
                    !data.isPayConfirmMenuEnabled(), data::setPayConfirmMenuEnabled);
            case "DESTROY_PEARL_ON_DEATH" -> toggle(player, section, key, "Destroy Pearl on Death",
                    !data.isDestroyPearlOnDeath(), data::setDestroyPearlOnDeath);
            case "RANDOMIZED_COORDS" -> {
                boolean nextVal = !data.isRandomizedCoords();
                data.setRandomizedCoords(nextVal);
                plugin.getDatabaseManager().savePlayer(data);
                player.kickPlayer(ColorUtils.colorize("&cThe setting has been changed. Please rejoin."));
            }
            case "DEATH_MESSAGES" -> {
                data.setDeathMessagesChoice(nextTwoChoice(data.getDeathMessagesChoice()));
                sendToggleMessage(player, section, key, "Death Messages", data.isDeathMessagesEnabled());
            }
            case "ADVANCEMENT_MESSAGES" -> toggle(player, section, key, "Advancement Messages",
                    !data.isAdvancementMessagesEnabled(), data::setAdvancementMessagesEnabled);
            case "JOIN_LEAVE_MESSAGES" -> {
                data.setJoinLeaveMessagesChoice(nextTwoChoice(data.getJoinLeaveMessagesChoice()));
                sendToggleMessage(player, section, key, "Join/Leave Messages", data.isJoinLeaveMessagesEnabled());
            }
            case "TELEPORT_ALERTS" -> toggle(player, section, key, "Teleport Alerts",
                    !data.isTeleportAlertsEnabled(), data::setTeleportAlertsEnabled);
            case "FOLLOW_ALERT_SETTINGS" -> toggle(player, section, key, "Follow Alerts",
                    !data.isFollowAlertsEnabled(), data::setFollowAlertsEnabled);
            case "EXPLOSION_SOUNDS" -> toggle(player, section, key, "Explosion Sounds",
                    !data.isExplosionSoundsEnabled(), data::setExplosionSoundsEnabled);
            case "DISPLAY_DONUT_PLUS" -> toggle(player, section, key, "Display Donut+",
                    !data.isDisplayDonutPlusEnabled(), data::setDisplayDonutPlusEnabled);
            case "NIGHT_VISION" -> {
                boolean enabled = com.bx.ultimateDonutSmp.utils.NightVisionUtils.toggle(plugin, player);
                sendToggleMessage(player, section, key, "Night Vision", enabled);
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

        ButtonState state = buttonState(player, data, key, section);
        List<String> lore = new ArrayList<>();
        for (String line : section.getStringList("LORE")) {
            lore.add(ColorUtils.colorize(line.replace("{status}", state.status()), player));
        }
        Material material = ItemUtils.parseMaterial(section.getString("MATERIAL", "STONE"));
        String displayName = ColorUtils.colorize(section.getString("DISPLAY-NAME", "&fSetting"), player);
        ItemStack item = ItemUtils.createItem(
                material,
                displayName,
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

    private ButtonState buttonState(Player player, PlayerData data, String key, ConfigurationSection section) {
        if (section != null && section.contains("STATUS-PLACEHOLDER")) {
            String placeholder = section.getString("STATUS-PLACEHOLDER");
            if (placeholder != null && !placeholder.isBlank()) {
                String evaluated = ColorUtils.colorize(placeholder, player).trim();
                if (evaluated.startsWith("%") && evaluated.endsWith("%")) {
                    if (placeholder.equalsIgnoreCase("%player_is_flying%") || placeholder.equalsIgnoreCase("%player_flying%")) {
                        boolean flying = player.isFlying() || player.getAllowFlight();
                        return new ButtonState(flying ? "&aEnabled" : "&cDisabled", true);
                    }
                    if (placeholder.equalsIgnoreCase("%player_is_op%")) {
                        return new ButtonState(player.isOp() ? "&aEnabled" : "&cDisabled", true);
                    }
                    if (placeholder.equalsIgnoreCase("%player_is_sneaking%")) {
                        return new ButtonState(player.isSneaking() ? "&aEnabled" : "&cDisabled", true);
                    }
                    return new ButtonState("&cDisabled", true);
                }
                String lower = evaluated.toLowerCase(java.util.Locale.ROOT);
                if (lower.equals("true") || lower.equals("enabled") || lower.equals("yes") || lower.equals("on") || lower.equals("1")) {
                    return new ButtonState("&aEnabled", true);
                } else if (lower.equals("false") || lower.equals("disabled") || lower.equals("no") || lower.equals("off") || lower.equals("0")) {
                    return new ButtonState("&cDisabled", true);
                } else {
                    return new ButtonState(evaluated, true);
                }
            }
        }
        if (section != null && section.contains("COMMAND")) {
            String cmd = section.getString("COMMAND", "").toLowerCase(java.util.Locale.ROOT);
            if (cmd.contains("fly")) {
                boolean flying = player.isFlying() || player.getAllowFlight();
                return new ButtonState(flying ? "&aEnabled" : "&cDisabled", true);
            }
            return new ButtonState("&aEnabled", true);
        }
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
            case "SHOW_MONEY" -> state(data.isShowMoneyLine());
            case "SHOW_SHARDS" -> state(data.isShowShardsLine());
            case "SHOW_KILLS" -> state(data.isShowKillsLine());
            case "SHOW_DEATHS" -> state(data.isShowDeathsLine());
            case "SHOW_PLAYTIME" -> state(data.isShowPlaytimeLine());
            case "COMBAT_TIMER" -> state(data.isCombatTimerEnabled());
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
            case "MONEY_NAMETAGS" -> state(data.isMoneyNametagsEnabled());
            case "DUEL_MUSIC" -> state(data.isDuelMusicEnabled());
            case "QUIET_SPAWN" -> state(data.isQuietSpawnEnabled());
            case "CLEAR_ENTITIES_MESSAGES" -> state(data.isClearEntitiesMessagesEnabled());
            case "AMETHYST_BREAK_MESSAGES" -> state(data.isAmethystBreakMessagesEnabled());
            case "KEY_ALL_NOTIFICATIONS" -> state(data.isKeyAllNotificationsEnabled());
            case "TPA_CONFIRM_MENUS" -> state(data.isTpaConfirmMenuEnabled());
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
            case "DESTROY_PEARL_ON_DEATH" -> state(data.isDestroyPearlOnDeath());
            case "RANDOMIZED_COORDS" -> state(data.isRandomizedCoords());
            case "DEATH_MESSAGES" -> state(data.isDeathMessagesEnabled());
            case "ADVANCEMENT_MESSAGES" -> state(data.isAdvancementMessagesEnabled());
            case "JOIN_LEAVE_MESSAGES" -> state(data.isJoinLeaveMessagesEnabled());
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
                : new ButtonState("&cUnavailable", false);
    }

    private ButtonState quickBuyState(Player player) {
        if (!PermissionUtils.has(player, "ultimatedonutsmp.auctionhouse.fastbuy")
                && !PermissionUtils.has(player, "donutauction.fastbuy")) {
            return new ButtonState("&cNo permission", false);
        }
        if (preferenceLoading || quickBuyEnabled == null) {
            return new ButtonState("&eLoading...", false);
        }
        return state(quickBuyEnabled);
    }

    private ButtonState quickSellState(Player player) {
        if (!PermissionUtils.has(player, "ultimatedonutsmp.auctionhouse.fastsell")
                && !PermissionUtils.has(player, "donutauction.fastsell")) {
            return new ButtonState("&cNo permission", false);
        }
        if (preferenceLoading || quickSellEnabled == null) {
            return new ButtonState("&eLoading...", false);
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

    private void toggleQuickBuy(Player player, ConfigurationSection section, String key) {
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
                                "&cUnable to save quick auction purchase setting."
                        ));
                    } else {
                        sendToggleMessage(player, section, key, "Quick auction purchases", enabled);
                    }
                    rebuildIfOpen(player);
                }));
    }

    private void toggleQuickBuy(Player player) {
        ConfigurationSection section = plugin.getConfigManager().getMenus()
                .getConfigurationSection(MENU_PATH + ".BUTTONS.QUICK_AUCTION_PURCHASE");
        toggleQuickBuy(player, section, "QUICK_AUCTION_PURCHASE");
    }

    private void toggleQuickSell(Player player, ConfigurationSection section, String key) {
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
                                "&cUnable to save quick auction sell setting."
                        ));
                    } else {
                        sendToggleMessage(player, section, key, "Quick auction sells", enabled);
                    }
                    rebuildIfOpen(player);
                }));
    }

    private void toggleQuickSell(Player player) {
        ConfigurationSection section = plugin.getConfigManager().getMenus()
                .getConfigurationSection(MENU_PATH + ".BUTTONS.QUICK_AUCTION_SELL");
        toggleQuickSell(player, section, "QUICK_AUCTION_SELL");
    }

    private void toggle(Player player, ConfigurationSection section, String key, String label, boolean enabled, BooleanSetter setter) {
        setter.set(enabled);
        sendToggleMessage(player, section, key, label, enabled);
    }

    private void toggle(Player player, String label, boolean enabled, BooleanSetter setter) {
        toggle(player, null, null, label, enabled, setter);
    }

    /** Toggles one sidebar line and redraws the scoreboard so the change shows without a relog. */
    private void toggleSidebarLine(Player player, ConfigurationSection section, String key, String label, boolean enabled, BooleanSetter setter) {
        setter.set(enabled);
        plugin.getScoreboardManager().applyVisibility(player);
        sendToggleMessage(player, section, key, label, enabled);
    }

    private void toggleSidebarLine(Player player, String label, boolean enabled, BooleanSetter setter) {
        toggleSidebarLine(player, null, null, label, enabled, setter);
    }

    static String formatToggleFeedback(
            ConfigurationSection feedbackSection,
            ConfigurationSection section,
            String key,
            String label,
            boolean enabled
    ) {
        String template;
        if (section != null && section.contains("FEEDBACK-MESSAGE")) {
            template = section.getString("FEEDBACK-MESSAGE");
        } else if (feedbackSection != null && feedbackSection.contains("TOGGLE-MESSAGE")) {
            template = feedbackSection.getString("TOGGLE-MESSAGE");
        } else {
            template = "&7{setting} is now {state}&7.";
        }

        if (template == null || template.isBlank() || template.equalsIgnoreCase("none")) {
            return null;
        }

        String stateText = enabled
                ? (feedbackSection != null ? feedbackSection.getString("STATE-ENABLED", "&aEnabled") : "&aEnabled")
                : (feedbackSection != null ? feedbackSection.getString("STATE-DISABLED", "&cDisabled") : "&cDisabled");

        String settingName = resolveSettingName(feedbackSection, section, key, label);
        String settingDisplay = resolveSettingDisplayName(section, label);

        return template
                .replace("{setting}", settingName)
                .replace("{setting_display}", settingDisplay)
                .replace("{state}", stateText)
                .replace("{status}", stateText);
    }

    private void sendToggleMessage(Player player, ConfigurationSection section, String key, String label, boolean enabled) {
        ConfigurationSection feedbackSection = plugin.getConfigManager().getMenus()
                .getConfigurationSection(MENU_PATH + ".FEEDBACK");
        String message = formatToggleFeedback(feedbackSection, section, key, label, enabled);
        if (message != null) {
            player.sendMessage(ColorUtils.toComponent(message, player));
        }
    }

    private void sendToggleMessage(Player player, String label, boolean enabled) {
        sendToggleMessage(player, null, null, label, enabled);
    }

    static String formatChoiceFeedback(
            ConfigurationSection feedbackSection,
            ConfigurationSection section,
            String key,
            String label,
            String choiceText
    ) {
        String template;
        if (section != null && section.contains("FEEDBACK-MESSAGE")) {
            template = section.getString("FEEDBACK-MESSAGE");
        } else if (feedbackSection != null && feedbackSection.contains("CHOICE-MESSAGE")) {
            template = feedbackSection.getString("CHOICE-MESSAGE");
        } else {
            template = "&7{setting} is now set to {choice}&7.";
        }

        if (template == null || template.isBlank() || template.equalsIgnoreCase("none")) {
            return null;
        }

        String settingName = resolveSettingName(feedbackSection, section, key, label);
        String settingDisplay = resolveSettingDisplayName(section, label);

        return template
                .replace("{setting}", settingName)
                .replace("{setting_display}", settingDisplay)
                .replace("{choice}", choiceText)
                .replace("{state}", choiceText)
                .replace("{status}", choiceText);
    }

    private void sendChoiceMessage(Player player, ConfigurationSection section, String key, String label, ThreeChoice choice) {
        ConfigurationSection feedbackSection = plugin.getConfigManager().getMenus()
                .getConfigurationSection(MENU_PATH + ".FEEDBACK");
        String choiceText = formatFeedbackChoice(feedbackSection, choice);
        sendChoiceMessage(player, feedbackSection, section, key, label, choiceText);
    }

    private void sendChoiceMessage(Player player, String label, String choiceText) {
        ConfigurationSection feedbackSection = plugin.getConfigManager().getMenus()
                .getConfigurationSection(MENU_PATH + ".FEEDBACK");
        sendChoiceMessage(player, feedbackSection, null, null, label, choiceText);
    }

    private void sendChoiceMessage(
            Player player,
            ConfigurationSection feedbackSection,
            ConfigurationSection section,
            String key,
            String label,
            String choiceText
    ) {
        String message = formatChoiceFeedback(feedbackSection, section, key, label, choiceText);
        if (message != null) {
            player.sendMessage(ColorUtils.toComponent(message, player));
        }
    }

    static String formatFeedbackChoice(ConfigurationSection feedbackSection, ThreeChoice choice) {
        if (choice == null) {
            return "";
        }
        return switch (choice) {
            case OFF -> feedbackSection != null
                    ? feedbackSection.getString("CHOICE-OFF-TEXT", "&cOff")
                    : "&cOff";
            case ANYONE -> feedbackSection != null
                    ? feedbackSection.getString("CHOICE-ANYONE-TEXT", "&aAnyone")
                    : "&aAnyone";
            case FRIENDS_FOLLOWED -> feedbackSection != null
                    ? feedbackSection.getString("CHOICE-FRIENDS-FOLLOWED-TEXT", "&dFriends/Followed")
                    : "&dFriends/Followed";
        };
    }

    static String resolveSettingName(
            ConfigurationSection feedbackSection,
            ConfigurationSection section,
            String key,
            String defaultLabel
    ) {
        if (section != null) {
            if (section.contains("FEEDBACK-NAME")) {
                return section.getString("FEEDBACK-NAME");
            }
            if (section.contains("FEEDBACK-LABEL")) {
                return section.getString("FEEDBACK-LABEL");
            }
            if (section.contains("DISPLAY-NAME")) {
                boolean useDisplayName = feedbackSection != null && feedbackSection.getBoolean("USE-DISPLAY-NAME", false);
                String rawDisplay = section.getString("DISPLAY-NAME", "");
                if (useDisplayName || isDisplayNameCustomized(key, rawDisplay)) {
                    String stripped = ChatColor.stripColor(ColorUtils.colorize(rawDisplay)).trim();
                    if (!stripped.isEmpty()) {
                        return stripped;
                    }
                }
            }
        }
        return defaultLabel;
    }

    static String resolveSettingDisplayName(ConfigurationSection section, String defaultLabel) {
        if (section != null && section.contains("DISPLAY-NAME")) {
            return section.getString("DISPLAY-NAME");
        }
        return defaultLabel;
    }

    static boolean isDisplayNameCustomized(String key, String rawDisplayName) {
        if (key == null || rawDisplayName == null) {
            return false;
        }
        String bundledDefault = BUNDLED_DEFAULT_DISPLAY_NAMES.get(key);
        if (bundledDefault == null) {
            return false;
        }
        String stripped = ChatColor.stripColor(ColorUtils.colorize(rawDisplayName)).trim();
        return !bundledDefault.equalsIgnoreCase(stripped);
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

    private ThreeChoice nextThreeChoice(ThreeChoice current) {
        int nextOrdinal = (current.ordinal() + 1) % ThreeChoice.values().length;
        return ThreeChoice.values()[nextOrdinal];
    }

    private TwoChoice nextTwoChoice(TwoChoice current) {
        int nextOrdinal = (current.ordinal() + 1) % TwoChoice.values().length;
        return TwoChoice.values()[nextOrdinal];
    }

    private boolean containsEnabledButton(ConfigurationSection buttons, String key) {
        return buttons.contains(key) && PlayerSettingDefaults.isOptionEnabled(buttons, key);
    }

    private boolean shouldRenderButton(String key, ConfigurationSection section) {
        if (!PlayerSettingDefaults.isOptionEnabled(section)) {
            return false;
        }
        if (section != null && (section.contains("COMMAND") || section.contains("STATUS-PLACEHOLDER"))) {
            return true;
        }
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
