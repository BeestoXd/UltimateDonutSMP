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

public class TeamEditMenu extends BaseMenu {

    private static final String MENU_PATH = "TEAM-MENUS.TEAM-EDIT-MEMBER";

    private final UUID targetUuid;
    private final int returnPage;
    private final TeamMenu.SortMode returnSortMode;
    private final String returnSearchQuery;

    public TeamEditMenu(UltimateDonutSmp plugin, UUID targetUuid) {
        this(plugin, targetUuid, 0, TeamMenu.SortMode.DEFAULT, null);
    }

    public TeamEditMenu(UltimateDonutSmp plugin, UUID targetUuid, int returnPage, TeamMenu.SortMode returnSortMode) {
        this(plugin, targetUuid, returnPage, returnSortMode, null);
    }

    public TeamEditMenu(UltimateDonutSmp plugin, UUID targetUuid, int returnPage, TeamMenu.SortMode returnSortMode, String returnSearchQuery) {
        super(plugin, configuredTitle(plugin, targetUuid), configuredSize(plugin));
        this.targetUuid = targetUuid;
        this.returnPage = returnPage;
        this.returnSortMode = returnSortMode;
        this.returnSearchQuery = returnSearchQuery;
    }

    @Override
    public void build(Player player) {
        clear();

        if (menus().getBoolean(MENU_PATH + ".PLACEHOLDER", false)) {
            fill(material(MENU_PATH + ".PLACEHOLDER-MATERIAL", Material.BLACK_STAINED_GLASS_PANE));
        }

        Team team = plugin.getTeamManager().getTeam(player);
        if (team == null || !team.isLeader(player.getUniqueId())) {
            set(inventory.getSize() / 2, ItemUtils.createItem(Material.BARRIER, "&cYou don't have permissions to do this.", null));
            return;
        }

        Team.TeamMember member = team.getMember(targetUuid);
        if (member == null || team.isLeader(targetUuid)) {
            set(inventory.getSize() / 2, ItemUtils.createItem(Material.BARRIER, "&cPlayer is not editable.", null));
            return;
        }

        String targetName = resolveTargetName();

        renderToggleButton(
                "EDIT-HOME-BUTTON",
                Material.WHITE_BANNER,
                targetName,
                member.canEditHome()
        );
        renderToggleButton(
                "MANAGE-TEAMMATES-BUTTON",
                Material.IRON_HELMET,
                targetName,
                member.canManageTeammates()
        );
        renderToggleButton(
                "PVP-BUTTON",
                Material.IRON_SWORD,
                targetName,
                member.canTogglePvp()
        );
        renderToggleButton(
                "VISIT-HOME-BUTTON",
                Material.ENDER_PEARL,
                targetName,
                member.canVisitHome()
        );
        renderToggleButton(
                "TEAM-CHAT-BUTTON",
                Material.FEATHER,
                targetName,
                member.canUseTeamChat()
        );

        String kickPath = MENU_PATH + ".KICK-BUTTON";
        set(
                menus().getInt(kickPath + ".SLOT", 11),
                ItemUtils.createItem(
                        material(kickPath + ".MATERIAL", Material.OAK_DOOR),
                        replace(menus().getString(kickPath + ".TITLE", "&cKick"), Map.of("player", targetName)),
                        replace(menus().getStringList(kickPath + ".LORE"), Map.of("player", targetName))
                )
        );

        String backPath = MENU_PATH + ".BACK-BUTTON";
        set(
                menus().getInt(backPath + ".SLOT", 18),
                ItemUtils.createItem(
                        material(backPath + ".MATERIAL", Material.RED_STAINED_GLASS_PANE),
                        menus().getString(backPath + ".TITLE", "&cBack"),
                        menus().getStringList(backPath + ".LORE")
                )
        );
    }

    @Override
    public void handleClick(int slot, Player player, ClickType clickType) {
        SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));

        Team team = plugin.getTeamManager().getTeam(player);
        if (team == null || !team.isLeader(player.getUniqueId())) {
            player.sendMessage(ColorUtils.toComponent("&cYou don't have permissions to do this."));
            return;
        }

        Team.TeamMember member = team.getMember(targetUuid);
        if (member == null || team.isLeader(targetUuid)) {
            player.sendMessage(ColorUtils.toComponent("&cPlayer is not editable."));
            return;
        }

        if (slot == menus().getInt(MENU_PATH + ".EDIT-HOME-BUTTON.SLOT", 10)) {
            member.setCanEditHome(!member.canEditHome());
            plugin.getTeamManager().save(team);
            build(player);
            return;
        }
        if (slot == menus().getInt(MENU_PATH + ".KICK-BUTTON.SLOT", 11)) {
            new TeamKickConfirmMenu(plugin, targetUuid, returnPage, returnSortMode, returnSearchQuery).open(player);
            return;
        }
        if (slot == menus().getInt(MENU_PATH + ".MANAGE-TEAMMATES-BUTTON.SLOT", 12)) {
            member.setCanManageTeammates(!member.canManageTeammates());
            plugin.getTeamManager().save(team);
            build(player);
            return;
        }
        if (slot == menus().getInt(MENU_PATH + ".PVP-BUTTON.SLOT", 13)) {
            member.setCanTogglePvp(!member.canTogglePvp());
            plugin.getTeamManager().save(team);
            build(player);
            return;
        }
        if (slot == menus().getInt(MENU_PATH + ".VISIT-HOME-BUTTON.SLOT", 14)) {
            member.setCanVisitHome(!member.canVisitHome());
            plugin.getTeamManager().save(team);
            build(player);
            return;
        }
        if (slot == menus().getInt(MENU_PATH + ".TEAM-CHAT-BUTTON.SLOT", 15)) {
            member.setCanUseTeamChat(!member.canUseTeamChat());
            if (!member.canUseTeamChat()) {
                plugin.getTeamManager().setTeamChat(targetUuid, false);
            }
            plugin.getTeamManager().save(team);
            build(player);
            return;
        }
        if (slot == menus().getInt(MENU_PATH + ".BACK-BUTTON.SLOT", 18)) {
            new TeamMenu(plugin).withState(returnPage, returnSortMode, returnSearchQuery).open(player);
        }
    }

    private void renderToggleButton(String key, Material fallbackMaterial, String targetName, boolean enabled) {
        String path = MENU_PATH + "." + key;
        String state = enabled
                ? menus().getString(path + ".ON-STATE", "&a&lON")
                : menus().getString(path + ".OFF-STATE", "&c&lOFF");
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", targetName);
        placeholders.put("state", state);

        set(
                menus().getInt(path + ".SLOT", 10),
                ItemUtils.createItem(
                        material(path + ".MATERIAL", fallbackMaterial),
                        replace(menus().getString(path + ".TITLE", "&fToggle"), placeholders),
                        replace(menus().getStringList(path + ".LORE"), placeholders)
                )
        );
    }

    private String resolveTargetName() {
        String targetName = Bukkit.getOfflinePlayer(targetUuid).getName();
        if (targetName != null) {
            return targetName;
        }

        String storedName = plugin.getDatabaseManager().getLastKnownUsername(targetUuid);
        return storedName != null ? storedName : "Unknown";
    }

    private FileConfiguration menus() {
        return plugin.getConfigManager().getMenus();
    }

    private Material material(String path, Material fallback) {
        return ItemUtils.parseMaterial(menus().getString(path, fallback.name()));
    }

    private String replace(String value, Map<String, String> placeholders) {
        String output = value == null ? "" : value;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            output = output.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return output;
    }

    private List<String> replace(List<String> values, Map<String, String> placeholders) {
        List<String> output = new ArrayList<>();
        for (String value : values) {
            output.add(replace(value, placeholders));
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
                .getString(MENU_PATH + ".TITLE", "&8Edit {player}")
                .replace("{player}", name);
    }

    private static int configuredSize(UltimateDonutSmp plugin) {
        int size = plugin.getConfigManager().getMenus().getInt(MENU_PATH + ".SIZE", 27);
        return size >= 9 && size % 9 == 0 ? size : 27;
    }
}
