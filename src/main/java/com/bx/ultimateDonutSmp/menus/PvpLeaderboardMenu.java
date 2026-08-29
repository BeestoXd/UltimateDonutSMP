package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.PvpManager;
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

/**
 * One icon per leaderboard, each carrying its own top ten in the lore.
 *
 * <p>Holding the ranking in the lore rather than behind a second menu means the whole board is
 * readable by pointing at an icon, and a click only has to refresh it.</p>
 */
public class PvpLeaderboardMenu extends BaseMenu {

    private static final String PATH = "MENUS.LEADERBOARD";
    private static final List<Integer> CATEGORY_SLOTS = List.of(10, 11, 12, 14, 15, 16);

    private final Map<Integer, PvpManager.TopCategory> slotCategories = new HashMap<>();

    public PvpLeaderboardMenu(UltimateDonutSmp plugin) {
        super(plugin, configuredTitle(plugin), configuredSize(plugin));
    }

    @Override
    public void build(Player player) {
        clear();
        slotCategories.clear();
        fill(Material.BLACK_STAINED_GLASS_PANE);

        int shown = Math.max(1, Math.min(15, config().getInt(PATH + ".ENTRIES", 10)));
        PvpManager.TopCategory[] categories = PvpManager.TopCategory.values();

        for (int index = 0; index < categories.length && index < CATEGORY_SLOTS.size(); index++) {
            PvpManager.TopCategory category = categories[index];
            int slot = CATEGORY_SLOTS.get(index);
            set(slot, ItemUtils.createItem(
                    ItemUtils.parseMaterial(config().getString(
                            PATH + ".ICONS." + category.name(), category.icon())),
                    "&c&l" + category.displayName(),
                    buildLore(category, shown)
            ));
            slotCategories.put(slot, category);
        }
    }

    private List<String> buildLore(PvpManager.TopCategory category, int shown) {
        List<PvpManager.TopEntry> top = plugin.getPvpManager().getTop(category, shown);
        List<String> lore = new ArrayList<>();
        if (top.isEmpty()) {
            lore.add("&7Nobody has fought in the arena yet.");
            return lore;
        }

        String template = config().getString(PATH + ".LINE", "&7#{position} &f{player} &8- &c{value}");
        int position = 1;
        for (PvpManager.TopEntry entry : top) {
            lore.add(template
                    .replace("{position}", String.valueOf(position++))
                    .replace("{player}", entry.name())
                    .replace("{value}", String.valueOf(entry.value())));
        }
        return lore;
    }

    @Override
    public void handleClick(int slot, Player player, ClickType clickType) {
        if (slotCategories.containsKey(slot)) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            build(player);
        }
    }

    private FileConfiguration config() {
        return plugin.getConfigManager().getPvp();
    }

    private static String configuredTitle(UltimateDonutSmp plugin) {
        return plugin.getConfigManager().getPvp().getString(PATH + ".TITLE", "&8Leaderboards");
    }

    private static int configuredSize(UltimateDonutSmp plugin) {
        int size = plugin.getConfigManager().getPvp().getInt(PATH + ".SIZE", 27);
        return size >= 27 && size <= 54 && size % 9 == 0 ? size : 27;
    }
}
