package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.PunishmentManager;
import com.bx.ultimateDonutSmp.models.PunishmentQuery;
import com.bx.ultimateDonutSmp.models.PunishmentRecord;
import com.bx.ultimateDonutSmp.models.PunishmentState;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import com.bx.ultimateDonutSmp.utils.PermissionUtils;
import com.bx.ultimateDonutSmp.utils.SignInputUtil;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Server-wide punishment browser. Unlike {@link PunishmentHistoryMenu} this is not scoped to one
 * target, so every page is read off the main thread and rendered once the results land.
 */
public class PunishmentsListMenu extends BaseMenu {

    private static final String MENU_PATH = "PUNISHMENTS-LIST-MENU";
    private static final int MAX_SEARCH_LENGTH = 32;

    private static final int BACK_SLOT = 45;
    private static final int FILTER_STATE_SLOT = 46;
    private static final int FILTER_TYPE_SLOT = 47;
    private static final int PREVIOUS_PAGE_SLOT = 48;
    private static final int REFRESH_SLOT = 49;
    private static final int PAGE_INFO_SLOT = 50;
    private static final int SEARCH_SLOT = 51;
    private static final int NEXT_PAGE_SLOT = 52;
    private static final int SORT_SLOT = 53;

    private PunishmentQuery query = PunishmentQuery.defaultQuery();
    private String search = "";
    private int page;
    private int totalPages = 1;
    private int totalItems;
    private boolean loading = true;
    private boolean hasPreviousPage;
    private boolean hasNextPage;

    private List<PunishmentRecord> records = new ArrayList<>();
    private final Map<Integer, PunishmentRecord> visibleRecords = new HashMap<>();

    public PunishmentsListMenu(UltimateDonutSmp plugin) {
        super(plugin, configuredTitle(plugin), configuredSize(plugin));
    }

    @Override
    public void open(Player player) {
        loading = true;
        build(player);
        player.openInventory(inventory);
        reload(player);
    }

    /** Fetches the current page in the background and repaints the open inventory when it arrives. */
    private void reload(Player player) {
        loading = true;
        build(player);

        int maxItems = maxItemsPerPage();
        plugin.getPunishmentManager()
                .getAllAsync(query, search, maxItems, page * maxItems)
                .thenAccept(result -> plugin.getSpigotScheduler().runEntity(player, () -> {
                    if (!isViewing(player)) {
                        return;
                    }
                    records = result.records();
                    totalItems = result.total();
                    page = result.offset() / maxItems;
                    totalPages = Math.max(1, (int) Math.ceil(totalItems / (double) maxItems));
                    hasPreviousPage = page > 0;
                    hasNextPage = (page + 1) * maxItems < totalItems;
                    loading = false;
                    build(player);
                }));
    }

    private boolean isViewing(Player player) {
        return player.isOnline() && player.getOpenInventory().getTopInventory().getHolder() == this;
    }

    @Override
    public void build(Player player) {
        clear();
        visibleRecords.clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);

        if (loading) {
            buildLoadingState();
        } else if (records.isEmpty()) {
            buildEmptyState();
        } else {
            int maxItems = maxItemsPerPage();
            for (int index = 0; index < records.size() && index < maxItems; index++) {
                PunishmentRecord record = records.get(index);
                visibleRecords.put(index, record);
                set(index, createPunishmentItem(record));
            }
        }

        buildButton(BACK_SLOT, "BACK", "ARROW", "&cClose", List.of("&7Close this menu."));
        buildButton(FILTER_STATE_SLOT, "FILTER-STATE", "HOPPER", "&dState filter",
                List.of("&7Current: &f{state_filter}", "&aClick to change"));
        buildButton(FILTER_TYPE_SLOT, "FILTER-TYPE", "BOOK", "&dType filter",
                List.of("&7Current: &f{type_filter}", "&aClick to change"));
        buildButton(REFRESH_SLOT, "REFRESH", "CLOCK", "&dRefresh",
                List.of("&7Reload the punishment list."));
        buildButton(SEARCH_SLOT, "SEARCH", "NAME_TAG", "&dSearch",
                List.of("&7Current: &f{search}", "&aLeft-click to search a player", "&cRight-click to clear"));
        buildButton(SORT_SLOT, "SORT", "COMPARATOR", "&dSort",
                List.of("&7Current: &f{sort_order}", "&aClick to change"));
        buildPageButtons();
    }

    @Override
    public void handleClick(int slot, Player player, ClickType clickType) {
        PunishmentRecord record = visibleRecords.get(slot);
        if (record != null) {
            handleRecordClick(record, player, clickType);
            return;
        }

        if (slot == BACK_SLOT) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            player.closeInventory();
            return;
        }

        if (loading) {
            return;
        }

        if (slot == FILTER_STATE_SLOT) {
            query = query.nextStateFilter();
            page = 0;
            clickFeedback(player);
            reload(player);
            return;
        }

        if (slot == FILTER_TYPE_SLOT) {
            query = query.nextTypeFilter();
            page = 0;
            clickFeedback(player);
            reload(player);
            return;
        }

        if (slot == SORT_SLOT) {
            query = query.nextSortOrder();
            page = 0;
            clickFeedback(player);
            reload(player);
            return;
        }

        if (slot == SEARCH_SLOT) {
            handleSearchClick(player, clickType);
            return;
        }

        if (slot == REFRESH_SLOT) {
            clickFeedback(player);
            reload(player);
            return;
        }

        if (slot == PREVIOUS_PAGE_SLOT && hasPreviousPage) {
            page--;
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.PAGE-TURN"));
            reload(player);
            return;
        }

        if (slot == NEXT_PAGE_SLOT && hasNextPage) {
            page++;
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.PAGE-TURN"));
            reload(player);
        }
    }

    private void handleSearchClick(Player player, ClickType clickType) {
        if (clickType.isRightClick()) {
            if (search.isEmpty()) {
                return;
            }
            search = "";
            page = 0;
            clickFeedback(player);
            reload(player);
            return;
        }

        List<String> signLines = defaultIfEmpty(
                menus().getStringList(MENU_PATH + ".SEARCH-SIGN.LINES"),
                List.of("", "^^^^^^^^^^^^^^", "Player Name", "")
        );
        int inputLine = menus().getInt(MENU_PATH + ".SEARCH-SIGN.INPUT-LINE", 0);

        SignInputUtil.open(plugin, player, signLines, inputLine, text -> {
            if (text != null && !text.isBlank() && !text.equalsIgnoreCase("cancel")) {
                String trimmed = text.trim();
                search = trimmed.length() > MAX_SEARCH_LENGTH ? trimmed.substring(0, MAX_SEARCH_LENGTH) : trimmed;
                page = 0;
            }
            open(player);
        });
    }

    private void handleRecordClick(PunishmentRecord record, Player player, ClickType clickType) {
        if (clickType == ClickType.SHIFT_RIGHT) {
            deleteRecord(record, player);
            return;
        }

        if (clickType.isLeftClick() && record.getTargetUuid() != null) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            new PunishmentHistoryMenu(plugin, record.getTargetUuid(), false).open(player);
        }
    }

    private void deleteRecord(PunishmentRecord record, Player player) {
        if (!PermissionUtils.has(player, PunishmentManager.DELETE_PERMISSION)) {
            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                    "PUNISHMENTS.NO-DELETE-PERMISSION",
                    "&cYou do not have permission to delete punishment history records."
            )));
            return;
        }

        long recordId = record.getId();
        if (!plugin.getPunishmentManager().deleteRecord(recordId)) {
            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                    "PUNISHMENTS.DELETE-FAILED",
                    "&cFailed to delete punishment record #{id}.",
                    "{id}", String.valueOf(recordId)
            )));
            return;
        }

        player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                "PUNISHMENTS.DELETED-RECORD",
                "&aDeleted punishment history record &f#{id}&a.",
                "{id}", String.valueOf(recordId)
        )));
        clickFeedback(player);
        reload(player);
    }

    private void clickFeedback(Player player) {
        SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
    }

    private void buildLoadingState() {
        set(inventory.getSize() / 2, ItemUtils.createItem(
                ItemUtils.parseMaterial(menus().getString(MENU_PATH + ".LOADING-BUTTON.MATERIAL", "CLOCK")),
                replaceMenuPlaceholders(menus().getString(MENU_PATH + ".LOADING-BUTTON.DISPLAY-NAME", "&eLoading punishments")),
                replaceMenuPlaceholders(defaultIfEmpty(
                        menus().getStringList(MENU_PATH + ".LOADING-BUTTON.LORE"),
                        List.of("&7Reading the punishment table.")
                ))
        ));
    }

    private void buildEmptyState() {
        List<String> fallbackLore = search.isEmpty()
                ? List.of("&7No punishments match the current filters.")
                : List.of("&7No punishments match &f{search}&7.");

        set(inventory.getSize() / 2, ItemUtils.createItem(
                ItemUtils.parseMaterial(menus().getString(MENU_PATH + ".EMPTY-BUTTON.MATERIAL", "BARRIER")),
                replaceMenuPlaceholders(menus().getString(MENU_PATH + ".EMPTY-BUTTON.DISPLAY-NAME", "&cNo punishments found")),
                replaceMenuPlaceholders(defaultIfEmpty(menus().getStringList(MENU_PATH + ".EMPTY-BUTTON.LORE"), fallbackLore))
        ));
    }

    private void buildButton(int slot, String key, String fallbackMaterial, String fallbackName, List<String> fallbackLore) {
        set(slot, ItemUtils.createItem(
                ItemUtils.parseMaterial(menus().getString(MENU_PATH + ".BUTTONS." + key + ".MATERIAL", fallbackMaterial)),
                replaceMenuPlaceholders(menus().getString(MENU_PATH + ".BUTTONS." + key + ".DISPLAY-NAME", fallbackName)),
                replaceMenuPlaceholders(defaultIfEmpty(menus().getStringList(MENU_PATH + ".BUTTONS." + key + ".LORE"), fallbackLore))
        ));
    }

    private void buildPageButtons() {
        Material material = ItemUtils.parseMaterial(menus().getString("GLOBAL.PAGE-MENU.MATERIAL", "ARROW"));

        if (hasPreviousPage) {
            set(PREVIOUS_PAGE_SLOT, ItemUtils.createItem(
                    material,
                    menus().getString("GLOBAL.PAGE-MENU.BACK-BUTTON", "&aBack"),
                    menus().getStringList("GLOBAL.PAGE-MENU.BACK-LORE")
            ));
        }

        set(PAGE_INFO_SLOT, ItemUtils.createItem(
                Material.BOOK,
                "&ePage " + (page + 1) + "&7/&e" + totalPages,
                List.of(
                        "&fRecords: &7" + NumberUtils.format(totalItems),
                        "&fType: &7" + currentTypeFilterLabel(),
                        "&fState: &7" + query.stateFilter().getDisplayName(),
                        "&fSort: &7" + query.sortOrder().getDisplayName(),
                        "&fSearch: &7" + currentSearchLabel()
                )
        ));

        if (hasNextPage) {
            set(NEXT_PAGE_SLOT, ItemUtils.createItem(
                    material,
                    menus().getString("GLOBAL.PAGE-MENU.NEXT-BUTTON", "&aNext"),
                    menus().getStringList("GLOBAL.PAGE-MENU.NEXT-LORE")
            ));
        }
    }

    private ItemStack createPunishmentItem(PunishmentRecord record) {
        PunishmentState state = plugin.getPunishmentManager().getState(record);
        String materialPath = MENU_PATH + ".PUNISHMENT-ITEM.MATERIALS." + record.getType().name();
        Material material = ItemUtils.parseMaterial(
                menus().getString(materialPath, PunishmentItemRenderer.defaultMaterial(record.getType())));

        String displayName = menus().getString(
                MENU_PATH + ".PUNISHMENT-ITEM.DISPLAY-NAME", "{status_color}{player} &8- &f{type}");
        List<String> lore = defaultIfEmpty(
                menus().getStringList(MENU_PATH + ".PUNISHMENT-ITEM.LORE"),
                List.of(
                        "&7Reason: &f{reason}",
                        "&7Issued by: &f{issuer}",
                        "&7Date: &f{issued_at}",
                        "&7Expires: &f{expires_at}",
                        "&7Status: {status_color}{status}",
                        "&7ID: &f#{id}",
                        "",
                        "&aLeft-click to view full history",
                        "&cShift-right-click to delete this record"
                )
        );

        String targetName = displayNameFor(record);
        String displayType = plugin.getPunishmentManager().getDisplayType(record);

        return ItemUtils.createItem(
                material,
                PunishmentItemRenderer.applyRecord(replaceMenuPlaceholders(displayName), record, state, displayType, targetName),
                PunishmentItemRenderer.applyRecord(replaceMenuPlaceholders(lore), record, state, displayType, targetName)
        );
    }

    /**
     * Uses the name stored with the record rather than a fresh lookup: this list renders up to a full
     * page at a time and a per-row name resolve would be a database round trip each.
     */
    private String displayNameFor(PunishmentRecord record) {
        String snapshot = record.getTargetNameSnapshot();
        if (snapshot != null && !snapshot.isBlank()) {
            return snapshot;
        }
        UUID uuid = record.getTargetUuid();
        return uuid == null ? "unknown" : uuid.toString().substring(0, 8);
    }

    private String replaceMenuPlaceholders(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("{type_filter}", currentTypeFilterLabel())
                .replace("{state_filter}", query.stateFilter().getDisplayName())
                .replace("{sort_order}", query.sortOrder().getDisplayName())
                .replace("{search}", currentSearchLabel())
                .replace("{page}", String.valueOf(page + 1))
                .replace("{pages}", String.valueOf(totalPages))
                .replace("{total}", NumberUtils.format(totalItems));
    }

    private List<String> replaceMenuPlaceholders(List<String> lines) {
        List<String> replaced = new ArrayList<>();
        for (String line : lines) {
            replaced.add(replaceMenuPlaceholders(line));
        }
        return replaced;
    }

    private String currentTypeFilterLabel() {
        return query.typeFilter() == null ? "All" : query.typeFilter().name();
    }

    private String currentSearchLabel() {
        return search.isEmpty() ? "None" : search;
    }

    private int maxItemsPerPage() {
        return Math.max(1, Math.min(45, menus().getInt(MENU_PATH + ".MAX-ITEMS-PER-PAGE", 45)));
    }

    private List<String> defaultIfEmpty(List<String> configured, List<String> fallback) {
        return configured == null || configured.isEmpty() ? fallback : configured;
    }

    private FileConfiguration menus() {
        return plugin.getConfigManager().getMenus();
    }

    private static String configuredTitle(UltimateDonutSmp plugin) {
        return plugin.getConfigManager().getMenus().getString(MENU_PATH + ".TITLE", "&8All punishments");
    }

    private static int configuredSize(UltimateDonutSmp plugin) {
        int size = plugin.getConfigManager().getMenus().getInt(MENU_PATH + ".SIZE", 54);
        return size >= 27 && size % 9 == 0 ? size : 54;
    }
}
