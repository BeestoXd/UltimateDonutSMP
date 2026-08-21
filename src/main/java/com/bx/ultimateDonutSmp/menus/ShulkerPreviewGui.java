package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.AuctionBrowseRequest;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import com.bx.ultimateDonutSmp.utils.ShulkerBoxSupport;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ShulkerPreviewGui extends BaseMenu {

    private final ItemStack shulkerItem;
    private final AuctionBrowseRequest returnRequest;

    public ShulkerPreviewGui(UltimateDonutSmp plugin, ItemStack shulkerItem) {
        this(plugin, shulkerItem, null);
    }

    public ShulkerPreviewGui(
            UltimateDonutSmp plugin,
            ItemStack shulkerItem,
            AuctionBrowseRequest returnRequest
    ) {
        super(plugin, getTitle(plugin, shulkerItem), 54);
        this.shulkerItem = shulkerItem.clone();
        this.returnRequest = returnRequest;
    }

    private static String getTitle(UltimateDonutSmp plugin, ItemStack item) {
        return AuctionHouseMenuSupport.configText(
                plugin,
                "GUI.SHULKER_PREVIEW.TITLE",
                "&8Preview: {item}",
                "{item}", plugin.getAuctionHouseManager().describeItem(item)
        );
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);

        List<ItemStack> contents = ShulkerBoxSupport.getContents(shulkerItem);
        for (int i = 0; i < 27 && i < contents.size(); i++) {
            ItemStack current = contents.get(i);
            if (current != null) {
                current = current.clone();
                if (plugin.getAmethystToolsManager().isAmethystTool(current)) {
                    plugin.getAmethystToolsManager().updateLoreCountdown(current);
                }
                set(i, current);
            }
        }

        set(AuctionHouseMenuSupport.slot(plugin, "GUI.SHULKER_PREVIEW.BACK", 49),
                AuctionHouseMenuSupport.control(
                        plugin,
                        "GUI.SHULKER_PREVIEW.BACK",
                        Material.ARROW,
                        "&fBack to auction",
                        List.of("&7Return to the auction browser")
                ));
    }

    @Override
    public void handleClick(int slot, Player player, ClickType clickType) {
        if (slot == AuctionHouseMenuSupport.slot(plugin, "GUI.SHULKER_PREVIEW.BACK", 49)) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            plugin.getAuctionHouseManager().startNavigating(player.getUniqueId());
            AuctionBrowseRequest request = returnRequest == null
                    ? plugin.getAuctionHouseManager().session(player.getUniqueId()).request()
                    : returnRequest;
            new AuctionHouseBrowseMenu(
                    plugin,
                    request.page(),
                    request.sort(),
                    request.category().name()
            ).open(player);
        }
    }

    @Override
    public void onClose(Player player) {
        if (!plugin.getAuctionHouseManager().stopNavigating(player.getUniqueId())) {
            plugin.getAuctionHouseManager().clearSearchQuery(player.getUniqueId());
        }
    }
}
