package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.Team;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

public class TeamDisbandConfirmMenu extends BaseMenu {

    private static final String MENU_PATH = "TEAM-MENUS.TEAM-DISBAND";

    public TeamDisbandConfirmMenu(UltimateDonutSmp plugin) {
        super(plugin, configuredTitle(plugin), configuredSize(plugin));
    }

    @Override
    public void build(Player player) {
        clear();
        if (menus().getBoolean(MENU_PATH + ".PLACEHOLDER", true)) {
            fill(Material.BLACK_STAINED_GLASS_PANE);
        }

        String cancelPath = MENU_PATH + ".CANCEL-BUTTON";
        set(
                menus().getInt(cancelPath + ".SLOT", 11),
                ItemUtils.createItem(
                        material(cancelPath + ".MATERIAL", Material.RED_STAINED_GLASS_PANE),
                        menus().getString(cancelPath + ".TITLE", "&cCancel"),
                        menus().getStringList(cancelPath + ".LORE")
                )
        );

        String confirmPath = MENU_PATH + ".CONFIRM-BUTTON";
        set(
                menus().getInt(confirmPath + ".SLOT", 15),
                ItemUtils.createItem(
                        material(confirmPath + ".MATERIAL", Material.LIME_STAINED_GLASS_PANE),
                        menus().getString(confirmPath + ".TITLE", "&aConfirm"),
                        menus().getStringList(confirmPath + ".LORE")
                )
        );
    }

    @Override
    public void handleClick(int slot, Player player, ClickType clickType) {
        SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));

        int cancelSlot = menus().getInt(MENU_PATH + ".CANCEL-BUTTON.SLOT", 11);
        int confirmSlot = menus().getInt(MENU_PATH + ".CONFIRM-BUTTON.SLOT", 15);

        if (slot == cancelSlot) {
            new TeamMenu(plugin).open(player);
            return;
        }
        if (slot != confirmSlot) {
            return;
        }

        Team team = plugin.getTeamManager().getTeam(player);
        if (team == null) {
            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage("TEAM.NO-TEAM")));
            player.closeInventory();
            return;
        }
        if (!team.isLeader(player.getUniqueId())) {
            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage("TEAM.NOT-LEADER")));
            player.closeInventory();
            return;
        }

        plugin.getTeamManager().disbandTeam(team);
        player.closeInventory();
    }

    private FileConfiguration menus() {
        return plugin.getConfigManager().getMenus();
    }

    private Material material(String path, Material fallback) {
        return ItemUtils.parseMaterial(menus().getString(path, fallback.name()));
    }

    private static String configuredTitle(UltimateDonutSmp plugin) {
        return plugin.getConfigManager().getMenus().getString(MENU_PATH + ".TITLE", "&8Confirm disbanding team");
    }

    private static int configuredSize(UltimateDonutSmp plugin) {
        int size = plugin.getConfigManager().getMenus().getInt(MENU_PATH + ".SIZE", 27);
        return size >= 9 && size % 9 == 0 ? size : 27;
    }
}
