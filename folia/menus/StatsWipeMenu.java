package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.StatsWipeManager;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatsWipeMenu extends BaseMenu {

    private static final String MENU_PATH = "STATS-WIPE-MENU";

    private final Map<Integer, StatsWipeManager.WipeTarget> targetSlots = new HashMap<>();

    public StatsWipeMenu(UltimateDonutSmp plugin) {
        super(plugin, configuredTitle(plugin), configuredSize(plugin));
    }

    @Override
    public void build(Player player) {
        clear();
        targetSlots.clear();

        Material placeholderMaterial = ItemUtils.parseMaterial(
                menus().getString(MENU_PATH + ".PLACEHOLDER-MATERIAL", "BLACK_STAINED_GLASS_PANE")
        );
        if (menus().getBoolean(MENU_PATH + ".PLACEHOLDER", true)) {
            fill(placeholderMaterial);
        }

        Map<StatsWipeManager.WipeTarget, Integer> previewCounts = new EnumMap<>(plugin.getStatsWipeManager().buildPreviewCounts());
        ConfigurationSection buttons = menus().getConfigurationSection(MENU_PATH + ".BUTTONS");
        if (buttons != null) {
            for (StatsWipeManager.WipeTarget target : StatsWipeManager.WipeTarget.values()) {
                String targetPath = MENU_PATH + ".BUTTONS." + target.getConfigKey();
                if (!menus().contains(targetPath)) {
                    continue;
                }

                int slot = menus().getInt(targetPath + ".SLOT", -1);
                if (slot < 0 || slot >= inventory.getSize()) {
                    continue;
                }

                Map<String, String> placeholders = basePlaceholders(target, previewCounts.getOrDefault(target, 0));
                set(slot, ItemUtils.createItem(
                        ItemUtils.parseMaterial(menus().getString(targetPath + ".MATERIAL", "PAPER")),
                        replace(menus().getString(targetPath + ".DISPLAY-NAME", "&b" + target.getDisplayName()), placeholders),
                        replace(menus().getStringList(targetPath + ".LORE"), placeholders)
                ));
                targetSlots.put(slot, target);
            }
        }

        renderRefreshButton();
        renderCloseButton();

        if (plugin.getStatsWipeManager().isWipeInProgress()) {
            set(menus().getInt(MENU_PATH + ".STATUS.SLOT", 13), ItemUtils.createItem(
                    ItemUtils.parseMaterial(menus().getString(MENU_PATH + ".STATUS.MATERIAL", "BARRIER")),
                    menus().getString(MENU_PATH + ".STATUS.DISPLAY-NAME", "&cWipe In Progress"),
                    menus().getStringList(MENU_PATH + ".STATUS.LORE")
            ));
        }
    }

    @Override
    public void handleClick(int slot, Player player, ClickType clickType) {
        SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));

        int refreshSlot = menus().getInt(MENU_PATH + ".BUTTONS.REFRESH.SLOT", 22);
        int closeSlot = menus().getInt(MENU_PATH + ".BUTTONS.CLOSE.SLOT", 26);
        if (slot == refreshSlot) {
            build(player);
            return;
        }
        if (slot == closeSlot) {
            player.closeInventory();
            return;
        }

        StatsWipeManager.WipeTarget target = targetSlots.get(slot);
        if (target == null) {
            return;
        }

        if (plugin.getStatsWipeManager().isWipeInProgress()) {
            player.sendMessage(ColorUtils.toComponent(
                    plugin.getConfigManager().getMessages().getString("STATS-WIPE.BUSY", "&cA wipe is already in progress.")
            ));
            return;
        }

        new StatsWipeConfirmMenu(plugin, target).open(player);
    }

    private void renderRefreshButton() {
        String path = MENU_PATH + ".BUTTONS.REFRESH";
        if (!menus().contains(path)) {
            return;
        }

        set(menus().getInt(path + ".SLOT", 22), ItemUtils.createItem(
                ItemUtils.parseMaterial(menus().getString(path + ".MATERIAL", "CLOCK")),
                menus().getString(path + ".DISPLAY-NAME", "&bRefresh"),
                menus().getStringList(path + ".LORE")
        ));
    }

    private void renderCloseButton() {
        String path = MENU_PATH + ".BUTTONS.CLOSE";
        if (!menus().contains(path)) {
            return;
        }

        set(menus().getInt(path + ".SLOT", 26), ItemUtils.createItem(
                ItemUtils.parseMaterial(menus().getString(path + ".MATERIAL", "BARRIER")),
                menus().getString(path + ".DISPLAY-NAME", "&cClose"),
                menus().getStringList(path + ".LORE")
        ));
    }

    private Map<String, String> basePlaceholders(StatsWipeManager.WipeTarget target, int count) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("count", String.valueOf(count));
        placeholders.put("target", target.getDisplayName());
        placeholders.put("target_key", target.getConfigKey());
        return placeholders;
    }

    private String replace(String value, Map<String, String> placeholders) {
        String output = value == null ? "" : value;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            output = output.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return output;
    }

    private List<String> replace(List<String> lines, Map<String, String> placeholders) {
        List<String> replaced = new ArrayList<>();
        for (String line : lines) {
            replaced.add(replace(line, placeholders));
        }
        return replaced;
    }

    private FileConfiguration menus() {
        return plugin.getConfigManager().getMenus();
    }

    private static String configuredTitle(UltimateDonutSmp plugin) {
        return plugin.getConfigManager().getMenus().getString(MENU_PATH + ".TITLE", "&8Stats Wipe");
    }

    private static int configuredSize(UltimateDonutSmp plugin) {
        int size = plugin.getConfigManager().getMenus().getInt(MENU_PATH + ".SIZE", 27);
        return size >= 9 && size % 9 == 0 ? size : 27;
    }
}
