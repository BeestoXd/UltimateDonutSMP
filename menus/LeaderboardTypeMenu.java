package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.LeaderboardManager;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LeaderboardTypeMenu extends BaseMenu {

    private static final String MENU_PATH = "LEADERBOARDS-MENU.TYPE-MENU";
    private static final int[] ENTRY_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    private final LeaderboardManager.LeaderboardType type;
    private int page;

    public LeaderboardTypeMenu(UltimateDonutSmp plugin, LeaderboardManager.LeaderboardType type) {
        this(plugin, type, 0);
    }

    public LeaderboardTypeMenu(UltimateDonutSmp plugin, LeaderboardManager.LeaderboardType type, int page) {
        super(
                plugin,
                plugin.getConfigManager().getMenus()
                        .getString(MENU_PATH + ".TITLE", "&8{type}")
                        .replace("{type}", plugin.getLeaderboardManager().getDisplayName(type)),
                plugin.getConfigManager().getMenus().getInt(MENU_PATH + ".SIZE", 54)
        );
        this.type = type;
        this.page = Math.max(0, page);
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);

        int offset = page * ENTRY_SLOTS.length;
        List<LeaderboardManager.LeaderboardEntry> entries =
                plugin.getLeaderboardManager().getEntries(type, offset, ENTRY_SLOTS.length);

        for (int i = 0; i < entries.size() && i < ENTRY_SLOTS.length; i++) {
            set(ENTRY_SLOTS[i], createEntryItem(entries.get(i)));
        }

        if (entries.isEmpty()) {
            set(22, ItemUtils.createItem(
                    Material.BARRIER,
                    "&cNo leaderboard data",
                    List.of("&7Belum ada pemain yang punya data di kategori ini.")
            ));
        }

        set(45, ItemUtils.createItem(
                Material.RED_STAINED_GLASS_PANE,
                "&cBack",
                List.of("&7Kembali ke menu leaderboard.")
        ));

        if (page > 0) {
            set(48, ItemUtils.createItem(
                    Material.ARROW,
                    previousPageTitle(),
                    previousPageLore()
            ));
        }

        if ((page + 1) * ENTRY_SLOTS.length < plugin.getLeaderboardManager().getTotalEntries(type)) {
            set(50, ItemUtils.createItem(
                    Material.ARROW,
                    nextPageTitle(),
                    nextPageLore()
            ));
        }

        set(49, ItemUtils.createItem(
                currentTypeMaterial(),
                "&6" + plugin.getLeaderboardManager().getDisplayName(type),
                List.of(
                        "&7Page: &f" + (page + 1),
                        "&7Entries shown: &f" + entries.size()
                )
        ));

        set(53, createPlayerRankItem(player));
    }

    @Override
    public void handleClick(int slot, Player player) {
        if (slot == 45) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            new LeaderboardMenu(plugin).open(player);
            return;
        }

        if (slot == 48 && page > 0) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            page--;
            build(player);
            return;
        }

        if (slot == 50 && (page + 1) * ENTRY_SLOTS.length < plugin.getLeaderboardManager().getTotalEntries(type)) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            page++;
            build(player);
        }
    }

    private ItemStack createEntryItem(LeaderboardManager.LeaderboardEntry entry) {
        ConfigurationSection buttonSection = plugin.getConfigManager().getMenus().getConfigurationSection(MENU_PATH + ".BUTTON");
        String playerName = entry.playerData().getUsername();
        String typeName = plugin.getLeaderboardManager().getDisplayName(type);
        String value = plugin.getLeaderboardManager().formatValue(type, entry.playerData());

        Map<String, String> placeholders = new LinkedHashMap<>();
        placeholders.put("player", playerName);
        placeholders.put("type", typeName);
        placeholders.put("value", value);
        placeholders.put("position", String.valueOf(entry.position()));

        Material material = ItemUtils.parseMaterial(valueOrDefault(buttonSection, "MATERIAL", "PLAYER_HEAD"));
        ItemStack item = ItemUtils.createItem(
                material,
                applyPlaceholders(valueOrDefault(buttonSection, "DISPLAY-NAME", "&b{player}"), placeholders),
                applyPlaceholders(getLore(buttonSection, "LORE"), placeholders)
        );

        if (material == Material.PLAYER_HEAD && item.getItemMeta() instanceof SkullMeta meta) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(entry.playerData().getUuid());
            meta.setOwningPlayer(offlinePlayer);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createPlayerRankItem(Player player) {
        LeaderboardManager.LeaderboardEntry entry = plugin.getLeaderboardManager()
                .getPlayerEntry(player.getUniqueId(), type);
        if (entry == null) {
            return ItemUtils.createItem(
                    Material.PLAYER_HEAD,
                    "&eYour Rank",
                    List.of("&7Belum ada data untuk kategori ini.")
            );
        }

        PlayerData data = entry.playerData();
        ItemStack item = ItemUtils.createItem(
                Material.PLAYER_HEAD,
                "&eYour Rank",
                List.of(
                        "&7Player: &f" + data.getUsername(),
                        "&7Position: &f#" + entry.position(),
                        "&7Value: &f" + plugin.getLeaderboardManager().formatValue(type, data)
                )
        );

        if (item.getItemMeta() instanceof SkullMeta meta) {
            meta.setOwningPlayer(player);
            item.setItemMeta(meta);
        }
        return item;
    }

    private Material currentTypeMaterial() {
        ConfigurationSection buttons = plugin.getConfigManager().getMenus().getConfigurationSection("LEADERBOARDS-MENU.BUTTONS");
        if (buttons == null) {
            return Material.NETHER_STAR;
        }

        for (String key : buttons.getKeys(false)) {
            ConfigurationSection section = buttons.getConfigurationSection(key);
            if (section == null) {
                continue;
            }

            LeaderboardManager.LeaderboardType configuredType = plugin.getLeaderboardManager()
                    .parseType(section.getString("TYPE"))
                    .orElse(null);
            if (configuredType == type) {
                return ItemUtils.parseMaterial(section.getString("MATERIAL", "NETHER_STAR"));
            }
        }
        return Material.NETHER_STAR;
    }

    private String previousPageTitle() {
        return plugin.getConfigManager().getMenus()
                .getString("GLOBAL.PAGE-MENU.BACK-BUTTON", "&aBack");
    }

    private List<String> previousPageLore() {
        return getLore(plugin.getConfigManager().getMenus().getConfigurationSection("GLOBAL.PAGE-MENU"), "BACK-LORE");
    }

    private String nextPageTitle() {
        return plugin.getConfigManager().getMenus()
                .getString("GLOBAL.PAGE-MENU.NEXT-BUTTON", "&aNext");
    }

    private List<String> nextPageLore() {
        return getLore(plugin.getConfigManager().getMenus().getConfigurationSection("GLOBAL.PAGE-MENU"), "NEXT-LORE");
    }

    private String valueOrDefault(ConfigurationSection section, String key, String fallback) {
        if (section == null) {
            return fallback;
        }
        return section.getString(key, fallback);
    }

    private List<String> getLore(ConfigurationSection section, String key) {
        if (section == null) {
            return List.of();
        }
        if (section.isList(key)) {
            return new ArrayList<>(section.getStringList(key));
        }

        String singleLine = section.getString(key);
        if (singleLine == null || singleLine.isBlank()) {
            return List.of();
        }
        return List.of(singleLine);
    }

    private List<String> applyPlaceholders(List<String> lines, Map<String, String> placeholders) {
        List<String> replaced = new ArrayList<>();
        for (String line : lines) {
            replaced.add(applyPlaceholders(line, placeholders));
        }
        return replaced;
    }

    private String applyPlaceholders(String text, Map<String, String> placeholders) {
        String resolved = text;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            resolved = resolved.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return resolved;
    }
}
