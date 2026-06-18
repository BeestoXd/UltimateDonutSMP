package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import com.bx.ultimateDonutSmp.utils.ShulkerBoxSupport;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ShulkerPreviewGui extends BaseMenu {

    private final ItemStack shulkerItem;

    public ShulkerPreviewGui(UltimateDonutSmp plugin, ItemStack shulkerItem) {
        super(plugin, getTitleForShulker(shulkerItem), 54);
        this.shulkerItem = shulkerItem.clone();
    }

    private static String getTitleForShulker(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasDisplayName()) {
            return "Preview: " + meta.getDisplayName();
        }
        String name = item.getType().name().replace('_', ' ').toLowerCase();
        if (!name.isEmpty()) {
            name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
        }
        return "Preview: " + name;
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);

        List<ItemStack> contents = ShulkerBoxSupport.getContents(shulkerItem);
        for (int i = 0; i < 27 && i < contents.size(); i++) {
            ItemStack current = contents.get(i);
            if (current != null) {
                set(i, current.clone());
            }
        }

        // Slot 49: Back to Auction
        set(49, ItemUtils.createItem(
                Material.ARROW,
                "&fBack to Auction",
                List.of("&7Return to the auction browser")
        ));
    }

    @Override
    public void handleClick(int slot, Player player, ClickType clickType) {
        if (slot == 49) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            plugin.getAuctionHouseManager().startNavigating(player.getUniqueId());
            new AuctionHouseBrowseMenu(plugin, 1, plugin.getAuctionHouseManager().getDefaultSort()).open(player);
        }
    }

    @Override
    public void onClose(Player player) {
        if (!plugin.getAuctionHouseManager().stopNavigating(player.getUniqueId())) {
            plugin.getAuctionHouseManager().clearSearchQuery(player.getUniqueId());
        }
    }
}
