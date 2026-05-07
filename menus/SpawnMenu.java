package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.SpawnManager;

public class SpawnMenu extends TeleportAreaMenu {

    public SpawnMenu(UltimateDonutSmp plugin) {
        super(
                plugin,
                plugin.getConfigManager().getMenus().getString("SPAWN-MENU.TITLE", "&8Spawn Areas"),
                plugin.getConfigManager().getMenus().getInt("SPAWN-MENU.SIZE", 54)
        );
    }

    @Override
    protected SpawnManager.AreaType getAreaType() {
        return SpawnManager.AreaType.SPAWN;
    }

    @Override
    protected String getMenuPath() {
        return "SPAWN-MENU";
    }

    @Override
    protected String getTeleportType() {
        return "SPAWN";
    }

    @Override
    protected String getEmptyTitle() {
        return "&cNo spawn areas";
    }

    @Override
    protected String getEmptyLore() {
        return "&7There are no valid cuboid-based spawn areas yet.";
    }
}
