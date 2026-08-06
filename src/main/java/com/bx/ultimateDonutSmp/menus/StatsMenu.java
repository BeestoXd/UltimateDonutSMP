package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class StatsMenu extends BaseMenu {

    private static final String MENU_PATH = "STATS-MENU";

    private final UUID targetUuid;
    private final String targetName;

    public StatsMenu(UltimateDonutSmp plugin, UUID targetUuid, String targetName) {
        super(
                plugin,
                configuredTitle(plugin, targetName),
                configuredSize(plugin)
        );
        this.targetUuid = targetUuid;
        this.targetName = targetName;
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);

        PlayerData data = plugin.getPlayerDataManager().get(targetUuid);
        if (data == null) {
            data = plugin.getDatabaseManager().loadPlayer(targetUuid);
        }

        if (data == null) {
            set(inventory.getSize() / 2, ItemUtils.createItem(
                    Material.BARRIER,
                    "&cPlayer Not Found",
                    List.of("&7This player has no recorded stats.")
            ));
            return;
        }

        ConfigurationSection buttons = plugin.getConfigManager().getMenus().getConfigurationSection(MENU_PATH + ".BUTTONS");
        if (buttons == null) {
            return;
        }

        for (String key : buttons.getKeys(false)) {
            ConfigurationSection section = buttons.getConfigurationSection(key);
            if (section == null) {
                continue;
            }

            int slot = section.getInt("SLOT", -1);
            if (slot < 0 || slot >= inventory.getSize()) {
                continue;
            }

            Material material = ItemUtils.parseMaterial(section.getString("MATERIAL", "STONE"));
            String rawName = section.getString("DISPLAY-NAME", "&b" + key);
            String displayName = replacePlaceholders(rawName, data);

            String val = resolveStatValue(key, data);

            List<String> rawLore = section.getStringList("LORE");
            List<String> lore = new ArrayList<>();
            for (String line : rawLore) {
                lore.add(replacePlaceholders(line.replace("{value}", val), data));
            }

            ItemStack item = createItem(material, displayName, lore);
            set(slot, item);
        }
    }

    @Override
    public void handleClick(int slot, Player player) {
        if (slot >= 0 && slot < inventory.getSize()) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
        }
    }

    private ItemStack createItem(Material material, String displayName, List<String> lore) {
        if (material != Material.PLAYER_HEAD) {
            return ItemUtils.createItem(material, displayName, lore);
        }

        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta itemMeta = item.getItemMeta();
        if (!(itemMeta instanceof SkullMeta meta)) {
            return ItemUtils.createItem(Material.PLAYER_HEAD, displayName, lore);
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(targetUuid);
        meta.setOwningPlayer(offlinePlayer);
        meta.setDisplayName(ColorUtils.toComponent(displayName));
        meta.setLore(ColorUtils.toComponentList(lore));
        item.setItemMeta(meta);
        return item;
    }

    private String resolveStatValue(String key, PlayerData data) {
        if (data == null) {
            return "0";
        }

        return switch (key.toUpperCase(Locale.ROOT)) {
            case "MONEY" -> plugin.getCurrencyManager().formatMoneyCompact(data.getMoney());
            case "SHARDS" -> plugin.getCurrencyManager().formatShardsCompact(data.getShards());
            case "KILLS" -> NumberUtils.format(data.getKills());
            case "DEATHS" -> NumberUtils.format(data.getDeaths());
            case "PLAYTIME" -> NumberUtils.formatTimeLong(data.getTotalPlaytimeSeconds());
            case "BLOCKS_PLACED" -> NumberUtils.format(data.getBlocksPlaced());
            case "BLOCKS_BROKEN" -> NumberUtils.format(data.getBlocksBroken());
            case "MOBS_KILLED" -> NumberUtils.format(data.getMobsKilled());
            case "KILL_STREAK" -> NumberUtils.format(data.getKillStreak());
            case "HIGHEST_KILL_STREAK" -> NumberUtils.format(data.getHighestKillStreak());
            case "MONEY_SPENT" -> plugin.getCurrencyManager().formatMoneyCompact(data.getMoneySpent());
            case "MONEY_MADE" -> plugin.getCurrencyManager().formatMoneyCompact(data.getMoneyMade());
            case "TEAM" -> {
                var team = plugin.getTeamManager().getTeam(data.getUuid());
                yield team != null ? team.getName().toUpperCase() : "None";
            }
            default -> "0";
        };
    }

    private String replacePlaceholders(String text, PlayerData data) {
        if (text == null) {
            return "";
        }
        String result = plugin.getCurrencyManager().applyStaticPlaceholders(text)
                .replace("{username}", targetName);

        if (data != null) {
            result = result
                    .replace("{kills}", NumberUtils.format(data.getKills()))
                    .replace("{deaths}", NumberUtils.format(data.getDeaths()))
                    .replace("{money}", plugin.getCurrencyManager().formatMoneyCompact(data.getMoney()))
                    .replace("{shards}", plugin.getCurrencyManager().formatShardsCompact(data.getShards()))
                    .replace("{playtime}", NumberUtils.formatTimeLong(data.getTotalPlaytimeSeconds()))
                    .replace("{blocks_placed}", NumberUtils.format(data.getBlocksPlaced()))
                    .replace("{blocks_broken}", NumberUtils.format(data.getBlocksBroken()))
                    .replace("{mobs_killed}", NumberUtils.format(data.getMobsKilled()))
                    .replace("{kill_streak}", NumberUtils.format(data.getKillStreak()))
                    .replace("{highest_kill_streak}", NumberUtils.format(data.getHighestKillStreak()))
                    .replace("{money_spent}", plugin.getCurrencyManager().formatMoneyCompact(data.getMoneySpent()))
                    .replace("{money_made}", plugin.getCurrencyManager().formatMoneyCompact(data.getMoneyMade()));
        }
        return result;
    }

    private static String configuredTitle(UltimateDonutSmp plugin, String targetName) {
        String template = plugin.getConfigManager().getMenus().getString(MENU_PATH + ".TITLE", "&8{username} Stats");
        return template.replace("{username}", targetName);
    }

    private static int configuredSize(UltimateDonutSmp plugin) {
        int size = plugin.getConfigManager().getMenus().getInt(MENU_PATH + ".SIZE", 36);
        return size >= 9 && size <= 54 && size % 9 == 0 ? size : 36;
    }
}
