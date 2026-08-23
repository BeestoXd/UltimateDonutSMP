package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.HideManager;
import com.bx.ultimateDonutSmp.models.Team;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * Read-only view of a team anyone can open with /team info, so the roster of a team you are not in
 * is visible without joining it. Nothing in here edits the team.
 */
public class TeamInfoMenu extends BaseMenu {

    private static final String MENU_PATH = "TEAM-MENUS.TEAM-INFO";

    private final Team team;
    private int page;

    public TeamInfoMenu(UltimateDonutSmp plugin, Team team) {
        super(plugin, configuredTitle(plugin, team), configuredSize(plugin));
        this.team = team;
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);

        List<UUID> members = orderMembers(team, memberUuid -> realName(memberUuid, null));
        int perPage = Math.max(1, menus().getInt(MENU_PATH + ".MAX-ITEMS-PER-PAGE", 45));
        int totalPages = pageCount(members.size(), perPage);
        page = Math.max(0, Math.min(page, totalPages - 1));

        int startIndex = page * perPage;
        int endIndex = Math.min(members.size(), startIndex + perPage);
        for (int index = startIndex; index < endIndex; index++) {
            set(index - startIndex, createMemberItem(player, members.get(index)));
        }

        renderSummaryButton(player);
        renderPageButtons(totalPages);
    }

    @Override
    public void handleClick(int slot, Player player, ClickType clickType) {
        SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));

        int perPage = Math.max(1, menus().getInt(MENU_PATH + ".MAX-ITEMS-PER-PAGE", 45));
        int totalPages = pageCount(team.getMemberCount(), perPage);

        if (slot == 47 && page > 0) {
            page--;
            build(player);
            return;
        }
        if (slot == 51 && page < totalPages - 1) {
            page++;
            build(player);
        }
    }

    /** Leader first, then everyone else by name, so the roster reads the same on every page load. */
    static List<UUID> orderMembers(Team team, Function<UUID, String> nameResolver) {
        List<UUID> ordered = new ArrayList<>(team.getMemberUuids());
        ordered.sort(Comparator
                .comparing((UUID uuid) -> !team.isLeader(uuid))
                .thenComparing(uuid -> nameResolver.apply(uuid) == null)
                .thenComparing(uuid -> {
                    String name = nameResolver.apply(uuid);
                    return name == null ? "" : name.toLowerCase(Locale.ROOT);
                }));
        return ordered;
    }

    static int pageCount(int memberCount, int perPage) {
        int size = Math.max(1, perPage);
        return Math.max(1, (int) Math.ceil(memberCount / (double) size));
    }

    private ItemStack createMemberItem(Player viewer, UUID memberUuid) {
        HideManager hideManager = plugin.getHideManager();
        boolean disguised = hideManager.isHidden(memberUuid) && !hideManager.canSeeRealIdentity(viewer);
        OfflinePlayer member = Bukkit.getOfflinePlayer(memberUuid);
        boolean online = member.isOnline();

        List<String> lore = new ArrayList<>();
        lore.add((online
                ? menus().getString(MENU_PATH + ".PLAYER-BUTTON.ONLINE-SYMBOL", "&a■")
                : menus().getString(MENU_PATH + ".PLAYER-BUTTON.OFFLINE-SYMBOL", "&4■"))
                + "&7 " + (online ? "online" : "offline"));
        if (team.isLeader(memberUuid)) {
            lore.add(menus().getString(MENU_PATH + ".PLAYER-BUTTON.LEADER-LORE", "&6Leader"));
        }

        String title = "&f" + displayName(viewer, memberUuid);
        // A disguised member gets a blank head, otherwise the menu hands out the skin behind the alias.
        return disguised
                ? ItemUtils.createItem(Material.PLAYER_HEAD, title, lore)
                : ItemUtils.createPlayerHead(member, title, lore);
    }

    private void renderSummaryButton(Player viewer) {
        String path = MENU_PATH + ".SUMMARY-BUTTON";
        String pvpState = team.isFriendlyFireEnabled()
                ? menus().getString(path + ".ON-STATE", "&a&lOn")
                : menus().getString(path + ".OFF-STATE", "&c&lOff");

        Map<String, String> placeholders = Map.of(
                "team_name", team.getName(),
                "leader", displayName(viewer, team.getLeaderUuid()),
                "members", String.valueOf(team.getMemberCount()),
                "max_members", String.valueOf(plugin.getConfigManager().getConfig().getInt("TEAM.LIMIT-MEMBERS", 10)),
                "state", pvpState
        );

        set(
                menus().getInt(path + ".SLOT", 49),
                ItemUtils.createItem(
                        material(path + ".MATERIAL", Material.IRON_HELMET),
                        replace(menus().getString(path + ".TITLE", "&#6BF18DTeam {team_name}"), placeholders),
                        replace(menus().getStringList(path + ".LORE"), placeholders)
                )
        );
    }

    private void renderPageButtons(int totalPages) {
        if (totalPages <= 1) {
            return;
        }

        FileConfiguration menus = menus();
        String globalPath = "GLOBAL.PAGE-MENU";
        Material material = material(globalPath + ".MATERIAL", Material.ARROW);

        if (page > 0) {
            set(47, ItemUtils.createItem(
                    material,
                    menus.getString(globalPath + ".BACK-BUTTON", "&aBack"),
                    menus.getStringList(globalPath + ".BACK-LORE")
            ));
        }

        String pagePath = MENU_PATH + ".PAGE-BUTTON";
        Map<String, String> placeholders = Map.of(
                "page", String.valueOf(page + 1),
                "total_pages", String.valueOf(totalPages)
        );
        set(
                menus.getInt(pagePath + ".SLOT", 50),
                ItemUtils.createItem(
                        material(pagePath + ".MATERIAL", Material.PAPER),
                        replace(menus.getString(pagePath + ".TITLE", "&fPage {page}&7/&f{total_pages}"), placeholders),
                        replace(menus.getStringList(pagePath + ".LORE"), placeholders)
                )
        );

        if (page < totalPages - 1) {
            set(51, ItemUtils.createItem(
                    material,
                    menus.getString(globalPath + ".NEXT-BUTTON", "&aNext"),
                    menus.getStringList(globalPath + ".NEXT-LORE")
            ));
        }
    }

    /** Mirrors HideManager#visibleName, except it also has to work for members who are offline. */
    private String displayName(Player viewer, UUID memberUuid) {
        HideManager hideManager = plugin.getHideManager();
        String realName = realName(memberUuid, "unknown");
        if (!hideManager.isHidden(memberUuid)) {
            return realName;
        }
        return hideManager.canSeeRealIdentity(viewer)
                ? hideManager.staffMarker() + realName
                : hideManager.publicName(memberUuid, realName);
    }

    private String realName(UUID memberUuid, String fallback) {
        String name = Bukkit.getOfflinePlayer(memberUuid).getName();
        if (name == null) {
            name = plugin.getDatabaseManager().getLastKnownUsername(memberUuid);
        }
        return name == null || name.isBlank() ? fallback : name;
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

    private static String configuredTitle(UltimateDonutSmp plugin, Team team) {
        String title = plugin.getConfigManager().getMenus().getString(MENU_PATH + ".TITLE", "&8Team {team_name}");
        return title.replace("{team_name}", team.getName());
    }

    private static int configuredSize(UltimateDonutSmp plugin) {
        int size = plugin.getConfigManager().getMenus().getInt(MENU_PATH + ".SIZE", 54);
        return size >= 9 && size % 9 == 0 ? size : 54;
    }
}
