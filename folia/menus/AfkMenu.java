package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.SpawnManager;

public class AfkMenu extends TeleportAreaMenu {

    public AfkMenu(UltimateDonutSmp plugin) {
        super(
                plugin,
                plugin.getConfigManager().getMenus().getString("AFK-MENU.TITLE", "&8AFK Areas"),
                plugin.getConfigManager().getMenus().getInt("AFK-MENU.SIZE", 54)
        );
    }

    @Override
    protected SpawnManager.AreaType getAreaType() {
        return SpawnManager.AreaType.AFK;
    }

    @Override
    protected String getMenuPath() {
        return "AFK-MENU";
    }

    @Override
    protected String getTeleportType() {
        return "AFK";
    }

    @Override
    protected String getEmptyTitle() {
        return "&cNo AFK areas";
    }

    @Override
    protected String getEmptyLore() {
        return "&7There are no valid cuboid-based AFK areas yet.";
    }
}
