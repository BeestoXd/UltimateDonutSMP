package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SettingsMenu extends BaseMenu {

    private static final String MENU_PATH = "SETTINGS-MENU";

    private final Map<Integer, String> clickableButtons = new HashMap<>();

    public SettingsMenu(UltimateDonutSmp plugin) {
        super(
                plugin,
                plugin.getConfigManager().getMenus().getString(MENU_PATH + ".TITLE", "&8ѕᴇᴛᴛɪɴɢѕ"),
                plugin.getConfigManager().getMenus().getInt(MENU_PATH + ".SIZE", 45)
        );
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

        for (String key : buttons.getKeys(false)) {
            if (!shouldRenderButton(key)) {
                continue;
            }
            ConfigurationSection section = buttons.getConfigurationSection(key);
            if (section == null) continue;
            renderButton(player, data, key, section);
        }
    }

    @Override
    public void handleClick(int slot, Player player) {
        String key = clickableButtons.get(slot);
        if (key == null) return;
        if (!shouldRenderButton(key)) return;

        PlayerData data = plugin.getPlayerDataManager().get(player);
        if (data == null) return;

        SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));

        switch (key) {
            case "PAY_ALERTS" -> {
                data.setPayAlertsEnabled(!data.isPayAlertsEnabled());
                sendToggleMessage(player, "ᴘᴀʏ ᴀʟᴇʀᴛѕ", data.isPayAlertsEnabled());
            }
            case "HOTBAR_MESSAGES" -> {
                data.setHotbarMessagesEnabled(!data.isHotbarMessagesEnabled());
                sendToggleMessage(player, "ʜᴏᴛʙᴀʀ ᴍᴇѕѕᴀɢᴇѕ", data.isHotbarMessagesEnabled());
            }
            case "CLEAR_ENTITIES_MESSAGES" -> {
                data.setClearEntitiesMessagesEnabled(!data.isClearEntitiesMessagesEnabled());
                sendToggleMessage(player, "ᴄʟᴇᴀʀ ᴇɴᴛɪᴛɪᴇѕ ᴍᴇѕѕᴀɢᴇѕ", data.isClearEntitiesMessagesEnabled());
            }
            case "WORTH_DISPLAY" -> {
                data.setWorthDisplayEnabled(!data.isWorthDisplayEnabled());
                if (data.isWorthDisplayEnabled()) {
                    plugin.getWorthManager().syncWorthDisplay(player);
                } else {
                    plugin.getWorthManager().clearWorthDisplay(player);
                }
                sendToggleMessage(player, "ᴡᴏʀᴛʜ ᴅɪѕᴘʟᴀʏ", data.isWorthDisplayEnabled());
            }
            case "BOUNTY_ALERTS" -> {
                data.setBountyAlertsEnabled(!data.isBountyAlertsEnabled());
                sendToggleMessage(player, "ʙᴏᴜɴᴛʏ ᴀʟᴇʀᴛѕ", data.isBountyAlertsEnabled());
            }
            case "AMETHYST_BREAK_MESSAGES" -> {
                data.setAmethystBreakMessagesEnabled(!data.isAmethystBreakMessagesEnabled());
                sendToggleMessage(player, "ᴀᴍᴇᴛʜʏѕᴛ ʙʀᴇᴀᴋ ᴍᴇѕѕᴀɢᴇѕ", data.isAmethystBreakMessagesEnabled());
            }
            case "KEY_ALL_NOTIFICATIONS" -> {
                data.setKeyAllNotificationsEnabled(!data.isKeyAllNotificationsEnabled());
                sendToggleMessage(player, "ᴋᴇʏ-ᴀʟʟ ɴᴏᴛɪꜰɪᴄᴀᴛɪᴏɴѕ", data.isKeyAllNotificationsEnabled());
            }
            case "DUEL_REQUESTS" -> {
                data.setDuelRequestsEnabled(!data.isDuelRequestsEnabled());
                sendToggleMessage(player, "Duel requests", data.isDuelRequestsEnabled());
            }
            case "TPA_CONFIRM_MENUS" -> {
                data.setTpaConfirmMenuEnabled(!data.isTpaConfirmMenuEnabled());
                sendToggleMessage(player, "ᴛᴘᴀ ᴄᴏɴꜰɪʀᴍ ᴍᴇɴᴜ", data.isTpaConfirmMenuEnabled());
            }
            case "CHAINMAIL_ON_RESPAWN" -> {
                data.setChainmailOnRespawnEnabled(!data.isChainmailOnRespawnEnabled());
                sendToggleMessage(player, "ʀᴇѕᴘᴀᴡɴ ᴀʀᴍᴏʀ", data.isChainmailOnRespawnEnabled());
            }
            case "LUNAR_TEAMMATES" -> {
                data.setLunarTeammatesEnabled(!data.isLunarTeammatesEnabled());
                sendToggleMessage(player, "ʟᴜɴᴀʀ ᴛᴇᴀᴍᴍᴀᴛᴇѕ", data.isLunarTeammatesEnabled());
            }
            case "TPA_REQUESTS" -> {
                data.setTpaRequestsEnabled(!data.isTpaRequestsEnabled());
                sendToggleMessage(player, "ᴛᴘᴀ ʀᴇǫᴜᴇѕᴛѕ", data.isTpaRequestsEnabled());
            }
            case "TP_AUTO" -> {
                data.setTpauto(!data.isTpauto());
                if (data.isTpauto()) {
                    plugin.getTPAManager().processQueuedAutoRequests(player.getUniqueId());
                }
                String msg = data.isTpauto()
                        ? plugin.getConfigManager().getMessage("TPAUTO.ENABLED")
                        : plugin.getConfigManager().getMessage("TPAUTO.DISABLED");
                player.sendMessage(ColorUtils.toComponent(msg));
            }
            case "TPA_HERE_AUTO_ACCEPT" -> {
                data.setAutoTpaHereEnabled(!data.isAutoTpaHereEnabled());
                if (data.isAutoTpaHereEnabled()) {
                    plugin.getTPAManager().processQueuedAutoRequests(player.getUniqueId());
                }
                sendToggleMessage(player, "ᴛᴘᴀ ʜᴇʀᴇ ᴀᴜᴛᴏ-ᴀᴄᴄᴇᴘᴛ", data.isAutoTpaHereEnabled());
            }
            case "TPA_HERE_REQUESTS" -> {
                data.setTpaHereRequestsEnabled(!data.isTpaHereRequestsEnabled());
                sendToggleMessage(player, "ᴛᴘᴀ ʜᴇʀᴇ ʀᴇǫᴜᴇѕᴛѕ", data.isTpaHereRequestsEnabled());
            }
            case "TEAM_INVITES" -> {
                data.setTeamInvitesEnabled(!data.isTeamInvitesEnabled());
                sendToggleMessage(player, "ᴛᴇᴀᴍ ɪɴᴠɪᴛᴇѕ", data.isTeamInvitesEnabled());
            }
            case "DISABLE_PHANTOM_SPAWN" -> {
                data.setPhantomEnabled(!data.isPhantomEnabled());
                String msg = data.isPhantomEnabled()
                        ? plugin.getConfigManager().getMessage("PHANTOM.DISABLED")
                        : plugin.getConfigManager().getMessage("PHANTOM.ENABLED");
                player.sendMessage(ColorUtils.toComponent(msg));
            }
            case "DISABLE_MOB_SPAWN" -> {
                data.setMobSpawnEnabled(!data.isMobSpawnEnabled());
                if (!data.isMobSpawnEnabled()) {
                    clearNearbyHostileMobs(player);
                }
                sendToggleMessage(player, "ɴᴇᴀʀʙʏ ʜᴏѕᴛɪʟᴇ ᴍᴏʙ ѕᴘᴀᴡɴѕ", data.isMobSpawnEnabled());
            }
            case "PAYMENTS" -> {
                data.setPaymentsEnabled(!data.isPaymentsEnabled());
                String msg = data.isPaymentsEnabled()
                        ? "&7ᴘᴀʏᴍᴇɴᴛѕ ᴀʀᴇ ɴᴏᴡ &aᴇɴᴀʙʟᴇᴅ&7."
                        : "&7ᴘᴀʏᴍᴇɴᴛѕ ᴀʀᴇ ɴᴏᴡ &cᴅɪѕᴀʙʟᴇᴅ&7.";
                player.sendMessage(ColorUtils.toComponent(msg));
            }
            case "SCOREBOARD_VISIBILITY" -> {
                data.setScoreboardVisible(!data.isScoreboardVisible());
                plugin.getScoreboardManager().applyVisibility(player);
                String msg = data.isScoreboardVisible()
                        ? "&7ѕᴄᴏʀᴇʙᴏᴀʀᴅ ɪѕ ɴᴏᴡ &aᴇɴᴀʙʟᴇᴅ&7."
                        : "&7ѕᴄᴏʀᴇʙᴏᴀʀᴅ ɪѕ ɴᴏᴡ &cᴅɪѕᴀʙʟᴇᴅ&7.";
                player.sendMessage(ColorUtils.toComponent(msg));
            }
            case "TEAM_CHAT" -> {
                var team = plugin.getTeamManager().getTeam(player);
                if (team == null) {
                    player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage("TEAM.NO-TEAM")));
                    break;
                }
                if (!plugin.getTeamManager().canUseTeamChat(team, player.getUniqueId())) {
                    player.sendMessage(ColorUtils.toComponent(
                            plugin.getConfigManager().getMessage("TEAM.NO-TEAM-CHAT-PERMISSION")));
                    break;
                }
                plugin.getTeamManager().toggleTeamChat(player.getUniqueId());
                boolean enabled = plugin.getTeamManager().isTeamChatEnabled(player.getUniqueId());
                String msg = enabled
                        ? plugin.getConfigManager().getMessage("TEAM.TEAM-CHAT-ENABLED")
                        : plugin.getConfigManager().getMessage("TEAM.TEAM-CHAT-DISABLED");
                player.sendMessage(ColorUtils.toComponent(msg));
            }
            case "PAY_CONFIRM_MENUS" -> {
                data.setPayConfirmMenuEnabled(!data.isPayConfirmMenuEnabled());
                sendToggleMessage(player, "ᴘᴀʏ ᴄᴏɴꜰɪʀᴍ ᴍᴇɴᴜ", data.isPayConfirmMenuEnabled());
            }
            case "TOTEM_PARTICLES" -> {
                data.setTotemParticlesEnabled(!data.isTotemParticlesEnabled());
                sendToggleMessage(player, "ᴛᴏᴛᴇᴍ ᴘᴀʀᴛɪᴄʟᴇѕ", data.isTotemParticlesEnabled());
            }
            case "FAST_CRYSTALS" -> {
                data.setFastCrystalsEnabled(!data.isFastCrystalsEnabled());
                plugin.getFastCrystalManager().applyCrystalCooldown(player);
                sendToggleMessage(player, "ꜰᴀѕᴛ ᴄʀʏѕᴛᴀʟѕ", data.isFastCrystalsEnabled());
            }
            case "NIGHT_VISION" -> toggleNightVision(player);
            default -> {
                return;
            }
        }

        build(player);
    }

    private boolean shouldRenderButton(String key) {
        if (!"DUEL_REQUESTS".equals(key)) {
            return true;
        }
        return plugin.getDuelManager() != null && plugin.getDuelManager().isEnabled();
    }

    private void renderButton(Player player, PlayerData data, String key, ConfigurationSection section) {
        int slot = section.getInt("SLOT", -1);
        if (slot < 0 || slot >= inventory.getSize()) return;

        ButtonState state = getButtonState(player, data, key);

        List<String> lore = new ArrayList<>();
        for (String line : section.getStringList("LORE")) {
            lore.add(line.replace("{status}", state.statusText()));
        }

        Material material = ItemUtils.parseMaterial(section.getString("MATERIAL", "STONE"));
        ItemStack item = ItemUtils.createItem(material, section.getString("DISPLAY-NAME", "&fѕᴇᴛᴛɪɴɢ"), lore);
        if ("NIGHT_VISION".equals(key)) {
            item = toNightVisionPotion(item);
        }

        set(slot, item);
        if (state.clickable()) {
            clickableButtons.put(slot, key);
        }
    }

    private ButtonState getButtonState(Player player, PlayerData data, String key) {
        return switch (key) {
            case "PAY_ALERTS" -> new ButtonState(formatStatus(data.isPayAlertsEnabled()), true);
            case "HOTBAR_MESSAGES" -> new ButtonState(formatStatus(data.isHotbarMessagesEnabled()), true);
            case "CLEAR_ENTITIES_MESSAGES" -> new ButtonState(formatStatus(data.isClearEntitiesMessagesEnabled()), true);
            case "WORTH_DISPLAY" -> new ButtonState(formatStatus(data.isWorthDisplayEnabled()), true);
            case "BOUNTY_ALERTS" -> new ButtonState(formatStatus(data.isBountyAlertsEnabled()), true);
            case "AMETHYST_BREAK_MESSAGES" -> new ButtonState(formatStatus(data.isAmethystBreakMessagesEnabled()), true);
            case "KEY_ALL_NOTIFICATIONS" -> new ButtonState(formatStatus(data.isKeyAllNotificationsEnabled()), true);
            case "DUEL_REQUESTS" -> new ButtonState(formatStatus(data.isDuelRequestsEnabled()), true);
            case "TPA_CONFIRM_MENUS" -> new ButtonState(formatStatus(data.isTpaConfirmMenuEnabled()), true);
            case "CHAINMAIL_ON_RESPAWN" -> new ButtonState(formatStatus(data.isChainmailOnRespawnEnabled()), true);
            case "SCOREBOARD_VISIBILITY" -> new ButtonState(formatStatus(data.isScoreboardVisible()), true);
            case "TEAM_CHAT" -> new ButtonState(
                    formatStatus(plugin.getTeamManager().isTeamChatEnabled(player.getUniqueId())),
                    true
            );
            case "TPA_REQUESTS" -> new ButtonState(formatStatus(data.isTpaRequestsEnabled()), true);
            case "TP_AUTO" -> new ButtonState(formatStatus(data.isTpauto()), true);
            case "TPA_HERE_AUTO_ACCEPT" -> new ButtonState(formatStatus(data.isAutoTpaHereEnabled()), true);
            case "TPA_HERE_REQUESTS" -> new ButtonState(formatStatus(data.isTpaHereRequestsEnabled()), true);
            case "LUNAR_TEAMMATES" -> new ButtonState(formatStatus(data.isLunarTeammatesEnabled()), true);
            case "TEAM_INVITES" -> new ButtonState(formatStatus(data.isTeamInvitesEnabled()), true);
            case "PAYMENTS" -> new ButtonState(formatStatus(data.isPaymentsEnabled()), true);
            case "DISABLE_MOB_SPAWN" -> new ButtonState(formatStatus(!data.isMobSpawnEnabled()), true);
            case "DISABLE_PHANTOM_SPAWN" -> new ButtonState(formatStatus(!data.isPhantomEnabled()), true);
            case "PAY_CONFIRM_MENUS" -> new ButtonState(formatStatus(data.isPayConfirmMenuEnabled()), true);
            case "TOTEM_PARTICLES" -> new ButtonState(formatStatus(data.isTotemParticlesEnabled()), true);
            case "FAST_CRYSTALS" -> new ButtonState(formatStatus(data.isFastCrystalsEnabled()), true);
            case "NIGHT_VISION" -> new ButtonState(
                    formatStatus(player.hasPotionEffect(PotionEffectType.NIGHT_VISION)),
                    true
            );
            default -> new ButtonState("&8ᴘʀᴇᴠɪᴇᴡ", false);
        };
    }

    private String formatStatus(boolean enabled) {
        return enabled ? "&aᴇɴᴀʙʟᴇᴅ" : "&cᴅɪѕᴀʙʟᴇᴅ";
    }

    private void sendToggleMessage(Player player, String label, boolean enabled) {
        String state = enabled ? "&aᴇɴᴀʙʟᴇᴅ" : "&cᴅɪѕᴀʙʟᴇᴅ";
        player.sendMessage(ColorUtils.toComponent("&7" + label + " ɪѕ ɴᴏᴡ " + state + "&7."));
    }

    private void toggleNightVision(Player player) {
        if (player.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
            player.removePotionEffect(PotionEffectType.NIGHT_VISION);
            player.sendMessage(ColorUtils.toComponent("&7ɴɪɢʜᴛ ᴠɪѕɪᴏɴ &cᴅɪѕᴀʙʟᴇᴅ&7."));
            return;
        }

        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false));
        player.sendMessage(ColorUtils.toComponent("&7ɴɪɢʜᴛ ᴠɪѕɪᴏɴ &aᴇɴᴀʙʟᴇᴅ&7."));
    }

    private void clearNearbyHostileMobs(Player player) {
        double radius = plugin.getConfigManager().getConfig().getDouble("SETTINGS.MOB-SPAWN-RADIUS", 50);
        double radiusSquared = radius * radius;

        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (!(entity instanceof Monster monster)) {
                continue;
            }
            if (monster.getType() == EntityType.PHANTOM) {
                continue;
            }
            if (monster.getLocation().distanceSquared(player.getLocation()) > radiusSquared) {
                continue;
            }

            monster.remove();
        }
    }

    private ItemStack toNightVisionPotion(ItemStack item) {
        if (!(item.getItemMeta() instanceof PotionMeta meta)) {
            return item;
        }

        meta.setBasePotionType(PotionType.NIGHT_VISION);
        item.setItemMeta(meta);
        return item;
    }

    private record ButtonState(String statusText, boolean clickable) {
    }
}
