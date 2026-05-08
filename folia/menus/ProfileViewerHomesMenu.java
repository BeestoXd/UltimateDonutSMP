package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.Home;
import com.bx.ultimateDonutSmp.models.ProfileSnapshot;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ProfileViewerHomesMenu extends BaseMenu {

    private static final String MENU_PATH = "PROFILE-VIEWER-HOMES-MENU";

    private static final int FIRST_PAGE_SLOT = 45;
    private static final int PREVIOUS_PAGE_SLOT = 46;
    private static final int BACK_SLOT = 48;
    private static final int REFRESH_SLOT = 49;
    private static final int PAGE_INFO_SLOT = 50;
    private static final int NEXT_PAGE_SLOT = 52;
    private static final int LAST_PAGE_SLOT = 53;

    private final UUID targetUuid;
    private final Map<Integer, Home> slotHomes = new HashMap<>();

    private ProfileSnapshot snapshot;
    private int page;
    private int totalPages = 1;
    private boolean hasPreviousPage;
    private boolean hasNextPage;

    public ProfileViewerHomesMenu(UltimateDonutSmp plugin, UUID targetUuid) {
        super(plugin, configuredTitle(plugin, targetUuid), configuredSize(plugin));
        this.targetUuid = targetUuid;
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);
        slotHomes.clear();

        snapshot = plugin.getProfileViewerManager().resolveProfile(targetUuid).orElse(null);
        if (snapshot == null) {
            set(inventory.getSize() / 2, ItemUtils.createItem(
                    Material.BARRIER,
                    "&cProfile Not Found",
                    List.of("&7This player no longer has profile data.")
            ));
            return;
        }

        List<Home> homes = new ArrayList<>(snapshot.getHomes());
        homes.sort(Comparator.comparing(Home::getName, String.CASE_INSENSITIVE_ORDER));

        int maxItems = Math.max(1, Math.min(45, menus().getInt(MENU_PATH + ".MAX-ITEMS-PER-PAGE", 45)));
        totalPages = Math.max(1, (int) Math.ceil(homes.size() / (double) maxItems));
        if (page >= totalPages) {
            page = totalPages - 1;
        }

        int startIndex = page * maxItems;
        int endIndex = Math.min(startIndex + maxItems, homes.size());
        hasPreviousPage = page > 0;
        hasNextPage = endIndex < homes.size();

        if (homes.isEmpty()) {
            buildEmptyState();
        } else {
            for (int index = startIndex; index < endIndex; index++) {
                Home home = homes.get(index);
                int slot = index - startIndex;
                set(slot, buildHomeItem(home));
                if (isTeleportable(home)) {
                    slotHomes.put(slot, home);
                }
            }
        }

        buildBackButton();
        buildRefreshButton();
        buildPageButtons(homes.size());
    }

    @Override
    public void handleClick(int slot, Player player) {
        if (slot == BACK_SLOT) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            new ProfileViewerMenu(plugin, targetUuid).open(player);
            return;
        }

        if (slot == REFRESH_SLOT) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            build(player);
            return;
        }

        if (slot == FIRST_PAGE_SLOT && hasPreviousPage) {
            page = 0;
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.PAGE-TURN"));
            build(player);
            return;
        }

        if (slot == PREVIOUS_PAGE_SLOT && hasPreviousPage) {
            page--;
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.PAGE-TURN"));
            build(player);
            return;
        }

        if (slot == NEXT_PAGE_SLOT && hasNextPage) {
            page++;
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.PAGE-TURN"));
            build(player);
            return;
        }

        if (slot == LAST_PAGE_SLOT && hasNextPage) {
            page = totalPages - 1;
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.PAGE-TURN"));
            build(player);
            return;
        }

        Home home = slotHomes.get(slot);
        if (home == null) {
            return;
        }

        SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
        player.closeInventory();
        plugin.getTeleportManager().queue(player, home.getLocation(), "PROFILE", null);
    }

    private void buildEmptyState() {
        String name = menus().getString(MENU_PATH + ".EMPTY-BUTTON.DISPLAY-NAME", "&cNo Homes");
        List<String> lore = menus().getStringList(MENU_PATH + ".EMPTY-BUTTON.LORE");
        if (lore.isEmpty()) {
            lore = List.of("&7This player has no homes saved.");
        }

        set(inventory.getSize() / 2, ItemUtils.createItem(
                ItemUtils.parseMaterial(menus().getString(MENU_PATH + ".EMPTY-BUTTON.MATERIAL", "BARRIER")),
                replaceSnapshotPlaceholders(name),
                replaceSnapshotPlaceholders(lore)
        ));
    }

    private ItemStack buildHomeItem(Home home) {
        boolean valid = isTeleportable(home);
        String materialPath = valid ? MENU_PATH + ".HOME-BUTTON.MATERIAL" : MENU_PATH + ".INVALID-HOME-BUTTON.MATERIAL";
        String namePath = valid ? MENU_PATH + ".HOME-BUTTON.DISPLAY-NAME" : MENU_PATH + ".INVALID-HOME-BUTTON.DISPLAY-NAME";
        String lorePath = valid ? MENU_PATH + ".HOME-BUTTON.LORE" : MENU_PATH + ".INVALID-HOME-BUTTON.LORE";

        Material material = ItemUtils.parseMaterial(menus().getString(materialPath, valid ? "LIGHT_BLUE_BED" : "BARRIER"));
        String displayName = menus().getString(namePath, valid ? "&b{name}" : "&c{name}");
        List<String> lore = menus().getStringList(lorePath);
        if (lore.isEmpty()) {
            lore = valid
                    ? List.of("&7World: &f{world}", "&7X: &f{x} &7Y: &f{y} &7Z: &f{z}", "&aClick to teleport")
                    : List.of("&7This home points to an unavailable world.");
        }

        return ItemUtils.createItem(
                material,
                replaceHomePlaceholders(displayName, home),
                replaceHomePlaceholders(lore, home)
        );
    }

    private void buildBackButton() {
        set(BACK_SLOT, ItemUtils.createItem(
                ItemUtils.parseMaterial(menus().getString(MENU_PATH + ".BACK-BUTTON.MATERIAL", "RED_STAINED_GLASS_PANE")),
                menus().getString(MENU_PATH + ".BACK-BUTTON.DISPLAY-NAME", "&cBack"),
                menus().getStringList(MENU_PATH + ".BACK-BUTTON.LORE").isEmpty()
                        ? List.of("&7Return to the main profile viewer.")
                        : menus().getStringList(MENU_PATH + ".BACK-BUTTON.LORE")
        ));
    }

    private void buildRefreshButton() {
        set(REFRESH_SLOT, ItemUtils.createItem(
                ItemUtils.parseMaterial(menus().getString(MENU_PATH + ".REFRESH-BUTTON.MATERIAL", "CLOCK")),
                replaceSnapshotPlaceholders(menus().getString(MENU_PATH + ".REFRESH-BUTTON.DISPLAY-NAME", "&bRefresh")),
                replaceSnapshotPlaceholders(defaultIfEmpty(
                        menus().getStringList(MENU_PATH + ".REFRESH-BUTTON.LORE"),
                        List.of("&7Reload this player's homes.")
                ))
        ));
    }

    private void buildPageButtons(int totalHomes) {
        Material material = ItemUtils.parseMaterial(menus().getString("GLOBAL.PAGE-MENU.MATERIAL", "ARROW"));

        if (hasPreviousPage) {
            set(FIRST_PAGE_SLOT, ItemUtils.createItem(
                    material,
                    menus().getString("GLOBAL.PAGE-MENU.FIRST-PAGE-BUTTON", "&aFirst Page"),
                    menus().getStringList("GLOBAL.PAGE-MENU.FIRST-PAGE-LORE")
            ));
            set(PREVIOUS_PAGE_SLOT, ItemUtils.createItem(
                    material,
                    menus().getString("GLOBAL.PAGE-MENU.BACK-BUTTON", "&aBack"),
                    menus().getStringList("GLOBAL.PAGE-MENU.BACK-LORE")
            ));
        }

        set(PAGE_INFO_SLOT, ItemUtils.createItem(
                Material.BOOK,
                "&ePage " + (page + 1) + "&7/&e" + totalPages,
                List.of("&fHomes: &7" + NumberUtils.format(totalHomes))
        ));

        if (hasNextPage) {
            set(NEXT_PAGE_SLOT, ItemUtils.createItem(
                    material,
                    menus().getString("GLOBAL.PAGE-MENU.NEXT-BUTTON", "&aNext"),
                    menus().getStringList("GLOBAL.PAGE-MENU.NEXT-LORE")
            ));
            set(LAST_PAGE_SLOT, ItemUtils.createItem(
                    material,
                    menus().getString("GLOBAL.PAGE-MENU.LAST-PAGE-BUTTON", "&aLast Page"),
                    menus().getStringList("GLOBAL.PAGE-MENU.LAST-PAGE-LORE")
            ));
        }
    }

    private boolean isTeleportable(Home home) {
        Location location = home.getLocation();
        return location != null && location.getWorld() != null;
    }

    private String replaceSnapshotPlaceholders(String value) {
        if (snapshot == null || value == null) {
            return value == null ? "" : value;
        }

        return value
                .replace("{username}", snapshot.getUsername())
                .replace("{homes}", String.valueOf(snapshot.getHomeCount()));
    }

    private List<String> replaceSnapshotPlaceholders(List<String> lines) {
        List<String> replaced = new ArrayList<>();
        for (String line : lines) {
            replaced.add(replaceSnapshotPlaceholders(line));
        }
        return replaced;
    }

    private String replaceHomePlaceholders(String value, Home home) {
        if (value == null) {
            return "";
        }

        Location location = home.getLocation();
        String world = "Unavailable";
        int x = 0;
        int y = 0;
        int z = 0;

        if (location != null) {
            x = location.getBlockX();
            y = location.getBlockY();
            z = location.getBlockZ();
            if (location.getWorld() != null) {
                world = friendlyWorldName(location);
            }
        }

        return value
                .replace("{username}", snapshot == null ? "Unknown" : snapshot.getUsername())
                .replace("{name}", home.getName())
                .replace("{world}", world)
                .replace("{x}", String.valueOf(x))
                .replace("{y}", String.valueOf(y))
                .replace("{z}", String.valueOf(z));
    }

    private List<String> replaceHomePlaceholders(List<String> lines, Home home) {
        List<String> replaced = new ArrayList<>();
        for (String line : lines) {
            replaced.add(replaceHomePlaceholders(line, home));
        }
        return replaced;
    }

    private String friendlyWorldName(Location location) {
        if (location == null || location.getWorld() == null) {
            return "Unavailable";
        }

        World.Environment environment = location.getWorld().getEnvironment();
        return switch (environment) {
            case NETHER -> "Nether";
            case THE_END -> "End";
            default -> "Overworld";
        };
    }

    private List<String> defaultIfEmpty(List<String> configured, List<String> fallback) {
        return configured == null || configured.isEmpty() ? fallback : configured;
    }

    private FileConfiguration menus() {
        return plugin.getConfigManager().getMenus();
    }

    private static String configuredTitle(UltimateDonutSmp plugin, UUID uuid) {
        String template = plugin.getConfigManager().getMenus().getString(MENU_PATH + ".TITLE", "&8{username}'s Homes");
        String username = plugin.getProfileViewerManager().resolveProfile(uuid)
                .map(ProfileSnapshot::getUsername)
                .orElse("Unknown");
        return template.replace("{username}", username);
    }

    private static int configuredSize(UltimateDonutSmp plugin) {
        int size = plugin.getConfigManager().getMenus().getInt(MENU_PATH + ".SIZE", 54);
        return size >= 27 && size % 9 == 0 ? size : 54;
    }
}
