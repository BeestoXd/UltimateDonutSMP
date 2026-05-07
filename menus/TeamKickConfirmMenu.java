package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.Team;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.*;

public class TeamKickConfirmMenu extends BaseMenu {

    private static final String MENU_PATH = "TEAM-MENUS.TEAM-KICK-MEMBER";

    private final UUID targetUuid;
    private final int returnPage;
    private final TeamMenu.SortMode returnSortMode;
    private final String returnSearchQuery;

    public TeamKickConfirmMenu(UltimateDonutSmp plugin, UUID targetUuid, int returnPage, TeamMenu.SortMode returnSortMode, String returnSearchQuery) {
        super(plugin, configuredTitle(plugin, targetUuid), configuredSize(plugin));
        this.targetUuid = targetUuid;
        this.returnPage = returnPage;
        this.returnSortMode = returnSortMode;
        this.returnSearchQuery = returnSearchQuery;
    }

    @Override
    public void build(Player player) {
        clear();
        if (menus().getBoolean(MENU_PATH + ".PLACEHOLDER", true)) {
            fill(Material.BLACK_STAINED_GLASS_PANE);
        }

        String targetName = resolveTargetName();

        String cancelPath = MENU_PATH + ".CANCEL-BUTTON";
        set(
                menus().getInt(cancelPath + ".SLOT", 11),
                ItemUtils.createItem(
                        material(cancelPath + ".MATERIAL", Material.RED_STAINED_GLASS_PANE),
                        menus().getString(cancelPath + ".TITLE", "&cCancel"),
                        replace(menus().getStringList(cancelPath + ".LORE"), Map.of("player", targetName))
                )
        );

        String confirmPath = MENU_PATH + ".CONFIRM-BUTTON";
        set(
                menus().getInt(confirmPath + ".SLOT", 15),
                ItemUtils.createItem(
                        material(confirmPath + ".MATERIAL", Material.LIME_STAINED_GLASS_PANE),
                        menus().getString(confirmPath + ".TITLE", "&aConfirm"),
                        replace(menus().getStringList(confirmPath + ".LORE"), Map.of("player", targetName))
                )
        );
    }

    @Override
    public void handleClick(int slot, Player player, ClickType clickType) {
        SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));

        int cancelSlot = menus().getInt(MENU_PATH + ".CANCEL-BUTTON.SLOT", 11);
        int confirmSlot = menus().getInt(MENU_PATH + ".CONFIRM-BUTTON.SLOT", 15);

        if (slot == cancelSlot) {
            new TeamEditMenu(plugin, targetUuid, returnPage, returnSortMode, returnSearchQuery).open(player);
            return;
        }
        if (slot != confirmSlot) {
            return;
        }

        Team team = plugin.getTeamManager().getTeam(player);
        if (team == null || !team.isLeader(player.getUniqueId())) {
            player.sendMessage(ColorUtils.toComponent("&cYou don't have permissions to do this."));
            player.closeInventory();
            return;
        }

        String targetName = resolveTargetName();
        boolean kicked = plugin.getTeamManager().kickMember(team, targetUuid);
        player.sendMessage(ColorUtils.toComponent(kicked
                ? plugin.getConfigManager().getMessage("TEAM.KICK-SUCCESS", "{player}", targetName)
                : plugin.getConfigManager().getMessage("TEAM.PLAYER-NOT-IN-TEAM", "{player}", targetName)));
        new TeamMenu(plugin).withState(returnPage, returnSortMode, returnSearchQuery).open(player);
    }

    private String resolveTargetName() {
        String onlineName = Bukkit.getOfflinePlayer(targetUuid).getName();
        if (onlineName != null) {
            return onlineName;
        }
        String storedName = plugin.getDatabaseManager().getLastKnownUsername(targetUuid);
        return storedName != null ? storedName : "player";
    }

    private FileConfiguration menus() {
        return plugin.getConfigManager().getMenus();
    }

    private Material material(String path, Material fallback) {
        return ItemUtils.parseMaterial(menus().getString(path, fallback.name()));
    }

    private List<String> replace(List<String> values, Map<String, String> placeholders) {
        List<String> output = new ArrayList<>();
        for (String value : values) {
            String line = value;
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                line = line.replace("{" + entry.getKey() + "}", entry.getValue());
            }
            output.add(line);
        }
        return output;
    }

    private static String configuredTitle(UltimateDonutSmp plugin, UUID targetUuid) {
        String name = Bukkit.getOfflinePlayer(targetUuid).getName();
        if (name == null) {
            String stored = plugin.getDatabaseManager().getLastKnownUsername(targetUuid);
            name = stored != null ? stored : "player";
        }
        return plugin.getConfigManager().getMenus()
                .getString(MENU_PATH + ".TITLE", "&8Confirm kicking {player}")
                .replace("{player}", name);
    }

    private static int configuredSize(UltimateDonutSmp plugin) {
        int size = plugin.getConfigManager().getMenus().getInt(MENU_PATH + ".SIZE", 27);
        return size >= 9 && size % 9 == 0 ? size : 27;
    }
}
