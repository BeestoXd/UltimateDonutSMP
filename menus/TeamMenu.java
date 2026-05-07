package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
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

import java.util.*;

public class TeamMenu extends BaseMenu {

    private static final String MENU_PATH = "TEAM-MENUS.TEAM";

    private final Map<Integer, UUID> slotMembers = new HashMap<>();
    private int page = 0;
    private SortMode sortMode = SortMode.DEFAULT;
    private String searchQuery;

    public TeamMenu(UltimateDonutSmp plugin) {
        super(plugin, configuredTitle(plugin), configuredSize(plugin));
    }

    public TeamMenu withState(int page, SortMode sortMode) {
        return withState(page, sortMode, this.searchQuery);
    }

    public TeamMenu withState(int page, SortMode sortMode, String searchQuery) {
        this.page = Math.max(0, page);
        this.sortMode = sortMode == null ? SortMode.DEFAULT : sortMode;
        this.searchQuery = searchQuery == null || searchQuery.isBlank() ? null : searchQuery.trim();
        return this;
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);
        slotMembers.clear();

        Team team = plugin.getTeamManager().getTeam(player);
        if (team == null) {
            set(inventory.getSize() / 2, ItemUtils.createItem(
                    Material.BARRIER,
                    message("MESSAGES.NOT-IN-TEAM", "&cYou are not part of the team."),
                    null
            ));
            return;
        }

        if (searchQuery == null || searchQuery.isBlank()) {
            searchQuery = plugin.getTeamManager().getActiveSearchQuery(player.getUniqueId());
        }

        List<UUID> members = new ArrayList<>(team.getMemberUuids());
        members.removeIf(memberUuid -> !matchesSearch(memberUuid));
        members.sort(sortMode.comparator(team));

        int maxItemsPerPage = Math.max(1, menus().getInt(MENU_PATH + ".MAX-ITEMS-PER-PAGE", 45));
        int totalPages = Math.max(1, (int) Math.ceil(members.size() / (double) maxItemsPerPage));
        page = Math.max(0, Math.min(page, totalPages - 1));

        int startIndex = page * maxItemsPerPage;
        int endIndex = Math.min(members.size(), startIndex + maxItemsPerPage);
        for (int index = startIndex; index < endIndex; index++) {
            int slot = index - startIndex;
            UUID memberUuid = members.get(index);
            OfflinePlayer member = Bukkit.getOfflinePlayer(memberUuid);
            set(slot, createMemberItem(team, memberUuid, member));
            slotMembers.put(slot, memberUuid);
        }

        if (members.isEmpty()) {
            set(22, ItemUtils.createItem(
                    Material.PAPER,
                    "&cNo members found",
                    searchQuery == null
                            ? List.of("&7Your team has no members to display.")
                            : List.of("&7No team member matched &f" + searchQuery, "&7Use search again or right-click search to clear.")
            ));
        }

        renderSearchButton();
        renderSortButton();
        renderRefreshButton(team);
        renderPageButtons(totalPages);
        renderHomeButton(player, team);
        renderPvpButton(team);
    }

    @Override
    public void handleClick(int slot, Player player, ClickType clickType) {
        SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));

        Team team = plugin.getTeamManager().getTeam(player);
        if (team == null) {
            return;
        }

        int searchSlot = menus().getInt(MENU_PATH + ".SEARCH-BUTTON.SLOT", 45);
        int sortSlot = menus().getInt(MENU_PATH + ".SORT-BUTTON.SLOT", 46);
        int refreshSlot = menus().getInt(MENU_PATH + ".REFRESH-BUTTON.SLOT", 49);
        int homeSlot = menus().getInt(MENU_PATH + ".HOME-BUTTON.SLOT", 52);
        int pvpSlot = menus().getInt(MENU_PATH + ".PVP-BUTTON.SLOT", 53);

        if (slot == searchSlot) {
            if (clickType.isRightClick()) {
                plugin.getTeamManager().clearSearchState(player.getUniqueId());
                searchQuery = null;
                page = 0;
                build(player);
                player.sendMessage(com.bx.ultimateDonutSmp.utils.ColorUtils.toComponent("&7Team member search cleared."));
                return;
            }
            plugin.getTeamManager().promptTeamSearch(player, page, sortMode);
            return;
        }
        if (slot == sortSlot) {
            sortMode = sortMode.next();
            build(player);
            return;
        }
        if (slot == refreshSlot) {
            build(player);
            return;
        }
        if (slot == 47 && hasPreviousPage(team)) {
            page--;
            build(player);
            return;
        }
        if (slot == 51 && hasNextPage(team)) {
            page++;
            build(player);
            return;
        }
        if (slot == homeSlot) {
            if (!plugin.getTeamManager().canVisitHome(team, player.getUniqueId())) {
                player.sendMessage(com.bx.ultimateDonutSmp.utils.ColorUtils.toComponent(
                        plugin.getConfigManager().getMessage("TEAM.NO-VISIT-HOME-PERMISSION")));
                return;
            }
            if (!team.hasHome()) {
                player.sendMessage(com.bx.ultimateDonutSmp.utils.ColorUtils.toComponent(
                        plugin.getConfigManager().getMessage("TEAM.NO-TEAM-HOME")));
                return;
            }
            player.closeInventory();
            plugin.getTeleportManager().queue(player, team.getHome(), "TEAM-HOME", null);
            return;
        }
        if (slot == pvpSlot) {
            if (!plugin.getTeamManager().toggleFriendlyFire(team, player.getUniqueId())) {
                player.sendMessage(com.bx.ultimateDonutSmp.utils.ColorUtils.toComponent(
                        message("MESSAGES.NO-PERMISSION", "&cYou don't have permissions to do this.")));
                return;
            }
            player.sendMessage(com.bx.ultimateDonutSmp.utils.ColorUtils.toComponent(
                    team.isFriendlyFireEnabled()
                            ? plugin.getConfigManager().getMessage("TEAM.TEAM-PVP-ENABLED")
                            : plugin.getConfigManager().getMessage("TEAM.TEAM-PVP-DISABLED")));
            build(player);
            return;
        }

        UUID targetUuid = slotMembers.get(slot);
        if (targetUuid == null) {
            return;
        }
        if (targetUuid.equals(player.getUniqueId())) {
            player.sendMessage(com.bx.ultimateDonutSmp.utils.ColorUtils.toComponent(
                    message("MESSAGES.CANT-EDIT-SELF", "&cYou can't do this yourself!")));
            return;
        }
        if (!team.isLeader(player.getUniqueId())) {
            player.sendMessage(com.bx.ultimateDonutSmp.utils.ColorUtils.toComponent(
                    message("MESSAGES.NO-PERMISSION", "&cYou don't have permissions to do this.")));
            return;
        }

        new TeamEditMenu(plugin, targetUuid, page, sortMode, searchQuery).open(player);
    }

    private ItemStack createMemberItem(Team team, UUID memberUuid, OfflinePlayer member) {
        String name = member.getName() != null ? member.getName() : "Unknown";
        boolean online = member.isOnline();

        List<String> lore = new ArrayList<>();
        lore.add((online
                ? menus().getString(MENU_PATH + ".PLAYER-BUTTON.ONLINE-SYMBOL", "&a■")
                : menus().getString(MENU_PATH + ".PLAYER-BUTTON.OFFLINE-SYMBOL", "&4■"))
                + "&7 " + (online ? "Online" : "Offline"));
        if (team.isLeader(memberUuid)) {
            lore.add("&6Leader");
        }

        String configuredLore = menus().getString(MENU_PATH + ".PLAYER-BUTTON.LORE", "&fClick to edit");
        if (configuredLore != null && !configuredLore.isBlank()) {
            lore.add(configuredLore);
        }

        return ItemUtils.createPlayerHead(member, "&f" + name, lore);
    }

    private void renderSearchButton() {
        String path = MENU_PATH + ".SEARCH-BUTTON";
        List<String> lore = new ArrayList<>(menus().getStringList(path + ".LORE"));
        if (searchQuery == null || searchQuery.isBlank()) {
            lore.removeIf(line -> line.toLowerCase().contains("in development"));
            lore.add("&7Current: &fNone");
            lore.add("&7Left-click to type a search.");
        } else {
            lore.removeIf(line -> line.toLowerCase().contains("in development"));
            lore.add("&7Current: &f" + searchQuery);
            lore.add("&7Left-click to change search.");
            lore.add("&7Right-click to clear search.");
        }
        set(
                menus().getInt(path + ".SLOT", 45),
                ItemUtils.createItem(
                        material(path + ".MATERIAL", Material.OAK_SIGN),
                        menus().getString(path + ".TITLE", "&aSearch"),
                        lore
                )
        );
    }

    private void renderSortButton() {
        String path = MENU_PATH + ".SORT-BUTTON";
        String selectedPrefix = menus().getString(path + ".SELECTED-PREFIX", "&a");
        String unselectedPrefix = menus().getString(path + ".UNSELECTED-PREFIX", "&f");
        String symbol = menus().getString(path + ".SYMBOL", "▪");

        List<String> lore = new ArrayList<>();
        for (SortMode mode : SortMode.values()) {
            boolean selected = mode == sortMode;
            lore.add((selected ? selectedPrefix : unselectedPrefix) + symbol + " " + mode.displayName);
        }
        lore.add("&7Click to change sorting.");

        set(
                menus().getInt(path + ".SLOT", 46),
                ItemUtils.createItem(
                        material(path + ".MATERIAL", Material.HOPPER),
                        menus().getString(path + ".TITLE", "&aSort"),
                        lore
                )
        );
    }

    private void renderRefreshButton(Team team) {
        String path = MENU_PATH + ".REFRESH-BUTTON";
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("team_name", team.getName());
        placeholders.put("max_members", String.valueOf(plugin.getConfigManager().getConfig().getInt("TEAM.LIMIT-MEMBERS", 10)));

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

        set(49, inventory.getItem(49));
        set(50, ItemUtils.createItem(
                Material.PAPER,
                "&fPage " + (page + 1) + "/" + totalPages,
                List.of("&7Browse team members.")
        ));

        if (page < totalPages - 1) {
            set(51, ItemUtils.createItem(
                    material,
                    menus.getString(globalPath + ".NEXT-BUTTON", "&aNext"),
                    menus.getStringList(globalPath + ".NEXT-LORE")
            ));
        }
    }

    private void renderHomeButton(Player player, Team team) {
        String path = MENU_PATH + ".HOME-BUTTON";
        String loreLine;
        if (!plugin.getTeamManager().canVisitHome(team, player.getUniqueId())) {
            loreLine = plugin.getConfigManager().getMessage("TEAM.NO-VISIT-HOME-PERMISSION");
        } else if (team.hasHome()) {
            loreLine = menus().getString(path + ".HOME-LORE", "&fClick to teleport to your team's home");
        } else {
            loreLine = menus().getString(path + ".NO-HOME-LORE", "&fSet the team home with /home");
        }

        set(
                menus().getInt(path + ".SLOT", 52),
                ItemUtils.createItem(
                        material(path + ".MATERIAL", Material.WHITE_BANNER),
                        menus().getString(path + ".TITLE", "&#6BF18DTeam Home"),
                        List.of(loreLine)
                )
        );
    }

    private void renderPvpButton(Team team) {
        String path = MENU_PATH + ".PVP-BUTTON";
        String state = team.isFriendlyFireEnabled()
                ? menus().getString(path + ".ON-STATE", "&a&lON")
                : menus().getString(path + ".OFF-STATE", "&c&lOFF");

        set(
                menus().getInt(path + ".SLOT", 53),
                ItemUtils.createItem(
                        material(path + ".MATERIAL", Material.IRON_SWORD),
                        menus().getString(path + ".TITLE", "&#6BF18DPvP"),
                        List.of(replace(menus().getString(path + ".LORE", "&fCurrently: {state}"), Map.of("state", state)))
                )
        );
    }

    private boolean hasPreviousPage(Team team) {
        return page > 0 && team != null;
    }

    private boolean hasNextPage(Team team) {
        if (team == null) return false;
        int maxItemsPerPage = Math.max(1, menus().getInt(MENU_PATH + ".MAX-ITEMS-PER-PAGE", 45));
        long visibleMembers = team.getMemberUuids().stream().filter(this::matchesSearch).count();
        return (page + 1) * maxItemsPerPage < visibleMembers;
    }

    private boolean matchesSearch(UUID memberUuid) {
        if (searchQuery == null || searchQuery.isBlank()) {
            return true;
        }

        String needle = searchQuery.toLowerCase(Locale.ROOT);
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(memberUuid);
        String name = offlinePlayer.getName();
        if (name == null) {
            name = plugin.getDatabaseManager().getLastKnownUsername(memberUuid);
        }
        return name != null && name.toLowerCase(Locale.ROOT).contains(needle);
    }

    private FileConfiguration menus() {
        return plugin.getConfigManager().getMenus();
    }

    private Material material(String path, Material fallback) {
        return ItemUtils.parseMaterial(menus().getString(path, fallback.name()));
    }

    private String message(String suffix, String fallback) {
        return menus().getString(MENU_PATH + "." + suffix, fallback);
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

    private static String configuredTitle(UltimateDonutSmp plugin) {
        return plugin.getConfigManager().getMenus().getString(MENU_PATH + ".TITLE", "&8Team");
    }

    private static int configuredSize(UltimateDonutSmp plugin) {
        int size = plugin.getConfigManager().getMenus().getInt(MENU_PATH + ".SIZE", 54);
        return size >= 9 && size % 9 == 0 ? size : 54;
    }

    public enum SortMode {
        DEFAULT("Default") {
            @Override
            Comparator<UUID> comparator(Team team) {
                return Comparator.comparingInt(uuid -> {
                    int index = 0;
                    for (UUID memberUuid : team.getMemberUuids()) {
                        if (memberUuid.equals(uuid)) {
                            return index;
                        }
                        index++;
                    }
                    return Integer.MAX_VALUE;
                });
            }
        },
        ONLINE_FIRST("Online First") {
            @Override
            Comparator<UUID> comparator(Team team) {
                return Comparator
                        .comparing((UUID uuid) -> !Bukkit.getOfflinePlayer(uuid).isOnline())
                        .thenComparing(uuid -> {
                            String name = Bukkit.getOfflinePlayer(uuid).getName();
                            return name == null ? "zzzz" : name.toLowerCase();
                        });
            }
        },
        ALPHABETICAL("Alphabetical") {
            @Override
            Comparator<UUID> comparator(Team team) {
                return Comparator.comparing(uuid -> {
                    String name = Bukkit.getOfflinePlayer(uuid).getName();
                    return name == null ? "zzzz" : name.toLowerCase();
                });
            }
        };

        private final String displayName;

        SortMode(String displayName) {
            this.displayName = displayName;
        }

        abstract Comparator<UUID> comparator(Team team);

        SortMode next() {
            SortMode[] values = values();
            return values[(ordinal() + 1) % values.length];
        }
    }
}
