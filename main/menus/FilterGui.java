package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.AuctionHouseManager;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.List;

public class FilterGui extends BaseMenu {

    private final AuctionHouseManager.AuctionSort sortMode;
    private final String currentCategory;

    private static final String[] CATEGORIES = {
            "BLOCKS", "TOOLS", "FOOD", "COMBAT", "POTIONS", "BOOKS", "INGREDIENTS", "UTILITIES"
    };

    private static final Material[] ICONS = {
            Material.GRASS_BLOCK, Material.DIAMOND_PICKAXE, Material.GOLDEN_CARROT,
            Material.DIAMOND_SWORD, Material.POTION, Material.ENCHANTED_BOOK,
            Material.BLAZE_POWDER, Material.ENDER_CHEST
    };

    public FilterGui(UltimateDonutSmp plugin, AuctionHouseManager.AuctionSort sortMode, String currentCategory) {
        super(plugin, "&6Category Filter", 27);
        this.sortMode = sortMode;
        this.currentCategory = currentCategory == null ? "ALL" : currentCategory.toUpperCase();
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);

        for (int i = 0; i < CATEGORIES.length; i++) {
            String category = CATEGORIES[i];
            Material icon = ICONS[i];
            boolean selected = category.equals(currentCategory);

            String displayName = "&f" + category.substring(0, 1) + category.substring(1).toLowerCase();
            List<String> lore = selected
                    ? List.of("&aSelected")
                    : List.of("&7Click to select");

            set(i, ItemUtils.createItem(
                    selected ? Material.LIME_STAINED_GLASS_PANE : icon,
                    displayName,
                    lore
            ));
        }

        // Slot 22: Clear Filter
        set(22, ItemUtils.createItem(
                Material.BARRIER,
                "&cClear Filter",
                List.of("&7Show all listings")
        ));
    }

    @Override
    public void handleClick(int slot, Player player, ClickType clickType) {
        if (slot >= 0 && slot < CATEGORIES.length) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            String selected = CATEGORIES[slot];
            plugin.getAuctionHouseManager().startNavigating(player.getUniqueId());
            new AuctionHouseBrowseMenu(plugin, 1, sortMode, selected).open(player);
        } else if (slot == 22) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            plugin.getAuctionHouseManager().startNavigating(player.getUniqueId());
            new AuctionHouseBrowseMenu(plugin, 1, sortMode, "ALL").open(player);
        }
    }

    @Override
    public void onClose(Player player) {
        if (!plugin.getAuctionHouseManager().stopNavigating(player.getUniqueId())) {
            plugin.getAuctionHouseManager().clearSearchQuery(player.getUniqueId());
        }
    }
}
