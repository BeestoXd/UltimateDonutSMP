package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.StatsWipeManager;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatsWipeConfirmMenu extends BaseMenu {

    private static final String MENU_PATH = "STATS-WIPE-CONFIRM-MENU";

    private final StatsWipeManager.WipeTarget target;

    public StatsWipeConfirmMenu(UltimateDonutSmp plugin, StatsWipeManager.WipeTarget target) {
        super(plugin, configuredTitle(plugin, target), configuredSize(plugin));
        this.target = target;
    }

    @Override
    public void build(Player player) {
        clear();

        Material placeholderMaterial = ItemUtils.parseMaterial(
                menus().getString(MENU_PATH + ".PLACEHOLDER-MATERIAL", "BLACK_STAINED_GLASS_PANE")
        );
        if (menus().getBoolean(MENU_PATH + ".PLACEHOLDER", true)) {
            fill(placeholderMaterial);
        }

        int previewCount = plugin.getStatsWipeManager().getPreviewCount(target);
        Map<String, String> placeholders = placeholders(previewCount);

        String infoPath = MENU_PATH + ".BUTTONS.TARGET";
        set(menus().getInt(infoPath + ".SLOT", 13), ItemUtils.createItem(
                ItemUtils.parseMaterial(menus().getString(infoPath + ".MATERIAL", "PAPER")),
                replace(menus().getString(infoPath + ".DISPLAY-NAME", "&cConfirm {target}"), placeholders),
                replace(menus().getStringList(infoPath + ".LORE"), placeholders)
        ));

        String cancelPath = MENU_PATH + ".BUTTONS.CANCEL";
        set(menus().getInt(cancelPath + ".SLOT", 11), ItemUtils.createItem(
                ItemUtils.parseMaterial(menus().getString(cancelPath + ".MATERIAL", "RED_STAINED_GLASS_PANE")),
                replace(menus().getString(cancelPath + ".DISPLAY-NAME", "&cCancel"), placeholders),
                replace(menus().getStringList(cancelPath + ".LORE"), placeholders)
        ));

        String confirmPath = MENU_PATH + ".BUTTONS.CONFIRM";
        set(menus().getInt(confirmPath + ".SLOT", 15), ItemUtils.createItem(
                ItemUtils.parseMaterial(menus().getString(confirmPath + ".MATERIAL", "LIME_STAINED_GLASS_PANE")),
                replace(menus().getString(confirmPath + ".DISPLAY-NAME", "&aConfirm"), placeholders),
                replace(menus().getStringList(confirmPath + ".LORE"), placeholders)
        ));
    }

    @Override
    public void handleClick(int slot, Player player, ClickType clickType) {
        SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));

        int cancelSlot = menus().getInt(MENU_PATH + ".BUTTONS.CANCEL.SLOT", 11);
        int confirmSlot = menus().getInt(MENU_PATH + ".BUTTONS.CONFIRM.SLOT", 15);

        if (slot == cancelSlot) {
            new StatsWipeMenu(plugin).open(player);
            return;
        }
        if (slot != confirmSlot) {
            return;
        }

        StatsWipeManager.WipeResult result = plugin.getStatsWipeManager().wipeTarget(target, player.getName());
        if (result.busy()) {
            player.sendMessage(ColorUtils.toComponent(message("BUSY", "&cA wipe is already in progress.")));
            new StatsWipeMenu(plugin).open(player);
            return;
        }
        if (!result.success()) {
            String error = result.errorMessage() == null || result.errorMessage().isBlank()
                    ? "Unknown error"
                    : result.errorMessage();
            player.sendMessage(ColorUtils.toComponent(message("FAILED", "&cStats Wipe failed: {error}")
                    .replace("{error}", error)));
            new StatsWipeMenu(plugin).open(player);
            return;
        }

        player.sendMessage(ColorUtils.toComponent(message("SUCCESS", "&aWipe complete: &f{target}&a. Affected records: &f{count}&a.")
                .replace("{target}", target.getDisplayName())
                .replace("{count}", String.valueOf(result.affectedCount(target)))));
        new StatsWipeMenu(plugin).open(player);
    }

    private Map<String, String> placeholders(int count) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("target", target.getDisplayName());
        placeholders.put("target_key", target.getConfigKey());
        placeholders.put("count", String.valueOf(count));
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

    private String message(String key, String fallback) {
        return plugin.getConfigManager().getMessages().getString("STATS-WIPE." + key, fallback);
    }

    private FileConfiguration menus() {
        return plugin.getConfigManager().getMenus();
    }

    private static String configuredTitle(UltimateDonutSmp plugin, StatsWipeManager.WipeTarget target) {
        String template = plugin.getConfigManager().getMenus().getString(MENU_PATH + ".TITLE", "&8Confirm {target}");
        return template.replace("{target}", target.getDisplayName());
    }

    private static int configuredSize(UltimateDonutSmp plugin) {
        int size = plugin.getConfigManager().getMenus().getInt(MENU_PATH + ".SIZE", 27);
        return size >= 9 && size % 9 == 0 ? size : 27;
    }
}
