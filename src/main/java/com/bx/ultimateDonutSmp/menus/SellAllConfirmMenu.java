package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

public class SellAllConfirmMenu extends BaseMenu {

    private static final String MENU_PATH = "SELLALL-CONFIRM-MENU";

    public SellAllConfirmMenu(UltimateDonutSmp plugin) {
        super(
                plugin,
                plugin.getConfigManager().getMenus().getString(MENU_PATH + ".TITLE", "&8Confirm sell all"),
                plugin.getConfigManager().getMenus().getInt(MENU_PATH + ".SIZE", 27)
        );
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);

        FileConfiguration menus = plugin.getConfigManager().getMenus();

        String cancelPath = MENU_PATH + ".CANCEL-BUTTON";
        set(
                menus.getInt(cancelPath + ".SLOT", 11),
                ItemUtils.createItem(
                        ItemUtils.parseMaterial(menus.getString(cancelPath + ".MATERIAL", "RED_STAINED_GLASS_PANE")),
                        menus.getString(cancelPath + ".TITLE", "&cCancel"),
                        menus.getStringList(cancelPath + ".LORE")
                )
        );

        String infoPath = MENU_PATH + ".INFO-BUTTON";
        set(
                menus.getInt(infoPath + ".SLOT", 13),
                ItemUtils.createItem(
                        ItemUtils.parseMaterial(menus.getString(infoPath + ".MATERIAL", "CHEST")),
                        menus.getString(infoPath + ".TITLE", "&eSell all items"),
                        menus.getStringList(infoPath + ".LORE")
                )
        );

        String confirmPath = MENU_PATH + ".CONFIRM-BUTTON";
        set(
                menus.getInt(confirmPath + ".SLOT", 15),
                ItemUtils.createItem(
                        ItemUtils.parseMaterial(menus.getString(confirmPath + ".MATERIAL", "LIME_STAINED_GLASS_PANE")),
                        menus.getString(confirmPath + ".TITLE", "&aConfirm"),
                        menus.getStringList(confirmPath + ".LORE")
                )
        );
    }

    @Override
    public void handleClick(int slot, Player player, ClickType clickType) {
        FileConfiguration menus = plugin.getConfigManager().getMenus();
        int cancelSlot = menus.getInt(MENU_PATH + ".CANCEL-BUTTON.SLOT", 11);
        int confirmSlot = menus.getInt(MENU_PATH + ".CONFIRM-BUTTON.SLOT", 15);

        if (slot != cancelSlot && slot != confirmSlot) {
            return;
        }

        SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
        player.closeInventory();

        if (slot == confirmSlot) {
            double total = plugin.getShopManager().sellInventory(player, false);
            if (total <= 0) {
                player.sendMessage(ColorUtils.toComponent(
                        plugin.getConfigManager().getMessage("WORTH.NO-SELLABLE", "&cThis item is not sellable.")));
            }
        }
    }
}
