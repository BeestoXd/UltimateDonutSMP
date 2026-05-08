package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.ShopManager;
import com.bx.ultimateDonutSmp.models.SellCategory;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SellProgressMenu extends BaseMenu {

    private static final int SIZE = 54;
    private static final int TYPE_BUTTON_SLOT = 4;
    private static final int BACK_BUTTON_SLOT = 49;
    private static final List<Integer> PROGRESS_SLOTS = List.of(
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33
    );

    private final SellCategory category;

    public SellProgressMenu(UltimateDonutSmp plugin, SellCategory category) {
        super(plugin, plugin.getConfigManager().getMenus().getString(
                "PROGRESS-MENU.TITLE." + category.getConfigKey(),
                "&8sell progress"
        ), SIZE);
        this.category = category;
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);

        FileConfiguration menus = plugin.getConfigManager().getMenus();
        buildTypeButton(menus);
        buildProgressButtons(player, menus);
        buildBackButton(menus);
    }

    @Override
    public void handleClick(int slot, Player player) {
        if (slot != BACK_BUTTON_SLOT) {
            return;
        }

        SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
        new SellMenu(plugin).open(player);
    }

    private void buildTypeButton(FileConfiguration menus) {
        String path = "PROGRESS-MENU.TYPE-BUTTON";
        Material material = ItemUtils.parseMaterial(
                menus.getString(path + ".MATERIAL." + category.getConfigKey(), "STONE")
        );
        String title = menus.getString(path + ".TITLE." + category.getConfigKey(), "&f" + category.name());
        List<String> lore = menus.getStringList(path + ".LORE." + category.getConfigKey());
        set(TYPE_BUTTON_SLOT, ItemUtils.createItem(material, title, lore));
    }

    private void buildProgressButtons(Player player, FileConfiguration menus) {
        Map<SellCategory, Double> progress = plugin.getShopManager().getSellProgress(player.getUniqueId());
        ShopManager.SellProgressInfo info = plugin.getShopManager().getSellProgressInfo(progress, category);
        List<Long> levels = menus.getLongList("PROGRESS-MENU.LEVEL");

        for (int index = 0; index < Math.min(levels.size(), PROGRESS_SLOTS.size()); index++) {
            int slot = PROGRESS_SLOTS.get(index);
            long targetGoal = levels.get(index);

            if (index < info.completedLevels()) {
                String title = menus.getString("PROGRESS-MENU.COMPLETED-BUTTON.TITLE", "&aCompleted");
                Material material = ItemUtils.parseMaterial(
                        menus.getString("PROGRESS-MENU.COMPLETED-BUTTON.MATERIAL", "LIME_STAINED_GLASS_PANE")
                );
                List<String> lore = applyProgressPlaceholders(
                        menus.getStringList("PROGRESS-MENU.COMPLETED-BUTTON.LORE"),
                        getTierMultiplier(index),
                        100,
                        targetGoal,
                        targetGoal,
                        buildCompletedBar(menus)
                );
                set(slot, ItemUtils.createItem(material, title, lore));
                continue;
            }

            if (index == info.completedLevels() && !info.maxed()) {
                String title = menus.getString("PROGRESS-MENU.WORKING-BUTTON.TITLE", "&eWorking");
                Material material = ItemUtils.parseMaterial(
                        menus.getString("PROGRESS-MENU.WORKING-BUTTON.MATERIAL", "YELLOW_STAINED_GLASS_PANE")
                );
                List<String> lore = applyProgressPlaceholders(
                        menus.getStringList("PROGRESS-MENU.WORKING-BUTTON.LORE"),
                        info.nextMultiplierDisplay(),
                        info.percentage(),
                        info.earned(),
                        info.nextGoal(),
                        info.progressBar()
                );
                set(slot, ItemUtils.createItem(material, title, lore));
            }
        }
    }

    private void buildBackButton(FileConfiguration menus) {
        Material material = ItemUtils.parseMaterial(
                menus.getString("GLOBAL.PAGE-MENU.MATERIAL", "ARROW")
        );
        String title = menus.getString("GLOBAL.PAGE-MENU.BACK-BUTTON", "&cBack");
        List<String> lore = menus.getStringList("GLOBAL.PAGE-MENU.BACK-LORE");
        set(BACK_BUTTON_SLOT, ItemUtils.createItem(material, title, lore));
    }

    private List<String> applyProgressPlaceholders(
            List<String> lore,
            String nextMultiplier,
            int percentage,
            double currentEarned,
            double nextGoal,
            String progressBar
    ) {
        return lore.stream()
                .map(line -> line
                        .replace("{next_multiplier}", nextMultiplier)
                        .replace("{porcentage}", String.valueOf(percentage))
                        .replace("{current_earned}", NumberUtils.formatNice(currentEarned))
                        .replace("{next_goal}", NumberUtils.formatNice(nextGoal))
                        .replace("{porcentage_level}", progressBar))
                .toList();
    }

    private String getTierMultiplier(int index) {
        return String.format(Locale.US, "%.1fx", 1.0 + ((index + 1) * 0.1));
    }

    private String buildCompletedBar(FileConfiguration menus) {
        String symbol = menus.getString("PROGRESS-MENU.PROGRESS-BAR", "\u25A0");
        return "&#6BF18D" + symbol.repeat(10);
    }
}
