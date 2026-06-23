package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.AuctionHouseManager;
import org.bukkit.entity.Player;

public final class AuctionHouseMyListingsMenu extends BaseMenu {

    private final int page;

    public AuctionHouseMyListingsMenu(
            UltimateDonutSmp plugin,
            int page,
            AuctionHouseManager.AuctionSort ignoredSort
    ) {
        super(plugin, plugin.getAuctionHouseManager().getMyListingsTitle(), 54);
        this.page = Math.max(1, page);
    }

    @Override
    public void build(Player player) {
    }

    @Override
    public void open(Player player) {
        new PlayerAuctionGui(plugin, page).open(player);
    }
}
