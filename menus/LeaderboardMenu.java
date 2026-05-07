package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.LeaderboardManager;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeaderboardMenu extends BaseMenu {

    private static final String MENU_PATH = "LEADERBOARDS-MENU";

    private final Map<Integer, LeaderboardManager.LeaderboardType> clickableTypes = new HashMap<>();

    public LeaderboardMenu(UltimateDonutSmp plugin) {
        super(
                plugin,
                plugin.getConfigManager().getMenus().getString(MENU_PATH + ".TITLE", "&8Leaderboards"),
                plugin.getConfigManager().getMenus().getInt(MENU_PATH + ".SIZE", 36)
        );
    }

    @Override
    public void build(Player player) {
        clear();
        clickableTypes.clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);

        ConfigurationSection buttons = plugin.getConfigManager().getMenus().getConfigurationSection(MENU_PATH + ".BUTTONS");
        if (buttons == null) {
            return;
        }

        for (String key : buttons.getKeys(false)) {
            ConfigurationSection section = buttons.getConfigurationSection(key);
            if (section == null) {
                continue;
            }

            String rawType = section.getString("TYPE");
            LeaderboardManager.LeaderboardType type = plugin.getLeaderboardManager().parseType(rawType).orElse(null);
            int slot = section.getInt("SLOT", -1);
            if (type == null || slot < 0 || slot >= inventory.getSize()) {
                continue;
            }

            Material material = ItemUtils.parseMaterial(section.getString("MATERIAL", "STONE"));
            String displayName = section.getString("DISPLAY-NAME", "&b" + plugin.getLeaderboardManager().getDisplayName(type));
            List<String> lore = section.getStringList("LORE");

            set(slot, ItemUtils.createItem(material, displayName, lore));
            clickableTypes.put(slot, type);
        }
    }

    @Override
    public void handleClick(int slot, Player player) {
        LeaderboardManager.LeaderboardType type = clickableTypes.get(slot);
        if (type == null) {
            return;
        }

        SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
        new LeaderboardTypeMenu(plugin, type).open(player);
    }
}
