package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.PunishmentManager;
import com.bx.ultimateDonutSmp.models.PunishmentQuery;
import com.bx.ultimateDonutSmp.models.PunishmentRecord;
import com.bx.ultimateDonutSmp.models.PunishmentState;
import com.bx.ultimateDonutSmp.models.PunishmentType;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class PunishmentHistoryMenu extends BaseMenu {

    private static final String MENU_PATH = "PUNISHMENT-HISTORY-MENU";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("ᴍᴍᴍ ᴅ ʏʏʏʏ, ʜʜ:ᴍᴍ:ѕѕ", Locale.US);

    private static final int BACK_SLOT = 45;
    private static final int FILTER_STATE_SLOT = 46;
    private static final int FILTER_TYPE_SLOT = 47;
    private static final int PREVIOUS_PAGE_SLOT = 48;
    private static final int REFRESH_SLOT = 49;
    private static final int PAGE_INFO_SLOT = 50;
    private static final int NEXT_PAGE_SLOT = 52;

    private final UUID targetUuid;
    private final boolean returnToProfileViewer;

    private PunishmentQuery query = PunishmentQuery.defaultQuery();
    private int page;
    private int totalPages = 1;
    private int totalItems;
    private boolean hasPreviousPage;
    private boolean hasNextPage;
    private final Map<Integer, Long> visibleRecordIds = new HashMap<>();

    public PunishmentHistoryMenu(UltimateDonutSmp plugin, UUID targetUuid, boolean returnToProfileViewer) {
        super(plugin, configuredTitle(plugin, targetUuid), configuredSize(plugin));
        this.targetUuid = targetUuid;
        this.returnToProfileViewer = returnToProfileViewer;
    }

    @Override
    public void build(Player player) {
        clear();
        visibleRecordIds.clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);

        int maxItems = Math.max(1, Math.min(45, menus().getInt(MENU_PATH + ".MAX-ITEMS-PER-PAGE", 45)));
        totalItems = plugin.getPunishmentManager().countHistory(targetUuid, query);
        totalPages = Math.max(1, (int) Math.ceil(totalItems / (double) maxItems));
        if (page >= totalPages) {
            page = totalPages - 1;
        }

        int offset = page * maxItems;
        hasPreviousPage = page > 0;
        hasNextPage = offset + maxItems < totalItems;

        List<PunishmentRecord> records = plugin.getPunishmentManager().getHistory(targetUuid, maxItems, offset, query);
        if (records.isEmpty()) {
            buildEmptyState();
        } else {
            for (int index = 0; index < records.size() && index < 45; index++) {
                PunishmentRecord record = records.get(index);
                visibleRecordIds.put(index, record.getId());
                set(index, createPunishmentItem(record));
            }
        }

        buildBackButton();
        buildFilterStateButton();
        buildFilterTypeButton();
        buildRefreshButton();
        buildPageButtons();
    }

    @Override
    public void handleClick(int slot, Player player, ClickType clickType) {
        Long recordId = visibleRecordIds.get(slot);
        if (recordId != null) {
            handlePunishmentRecordClick(recordId, player, clickType);
            return;
        }

        if (slot == BACK_SLOT) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            if (returnToProfileViewer && plugin.getProfileViewerManager().canView(player)) {
                new ProfileViewerMenu(plugin, targetUuid).open(player);
            } else {
                player.closeInventory();
            }
            return;
        }

        if (slot == FILTER_STATE_SLOT) {
            query = query.nextStateFilter();
            page = 0;
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            build(player);
            return;
        }

        if (slot == FILTER_TYPE_SLOT) {
            query = query.nextTypeFilter();
            page = 0;
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            build(player);
            return;
        }

        if (slot == REFRESH_SLOT) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
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
        }
    }

    private void handlePunishmentRecordClick(long recordId, Player player, ClickType clickType) {
        if (clickType != ClickType.SHIFT_RIGHT) {
            return;
        }

        if (!player.hasPermission(PunishmentManager.DELETE_PERMISSION)) {
            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                    "PUNISHMENTS.NO-DELETE-PERMISSION",
                    "&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ ᴛᴏ ᴅᴇʟᴇᴛᴇ ᴘᴜɴɪѕʜᴍᴇɴᴛ ʜɪѕᴛᴏʀʏ ʀᴇᴄᴏʀᴅѕ."
            )));
            return;
        }

        boolean deleted = plugin.getPunishmentManager().deleteRecord(recordId);
        if (!deleted) {
            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                    "PUNISHMENTS.DELETE-FAILED",
                    "&cꜰᴀɪʟᴇᴅ ᴛᴏ ᴅᴇʟᴇᴛᴇ ᴘᴜɴɪѕʜᴍᴇɴᴛ ʀᴇᴄᴏʀᴅ #{id}.",
                    "{id}", String.valueOf(recordId)
            )));
            return;
        }

        player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                "PUNISHMENTS.DELETED-RECORD",
                "&aᴅᴇʟᴇᴛᴇᴅ ᴘᴜɴɪѕʜᴍᴇɴᴛ ʜɪѕᴛᴏʀʏ ʀᴇᴄᴏʀᴅ &f#{id}&a.",
                "{id}", String.valueOf(recordId)
        )));
        SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
        build(player);
    }

    private void buildEmptyState() {
        String name = menus().getString(MENU_PATH + ".EMPTY-BUTTON.DISPLAY-NAME", "&cɴᴏ ᴘᴜɴɪѕʜᴍᴇɴᴛ ʜɪѕᴛᴏʀʏ");
        List<String> lore = menus().getStringList(MENU_PATH + ".EMPTY-BUTTON.LORE");
        if (lore.isEmpty()) {
            lore = List.of("&7ᴛʜɪѕ ᴘʟᴀʏᴇʀ ʜᴀѕ ɴᴏ ᴘᴜɴɪѕʜᴍᴇɴᴛ ʀᴇᴄᴏʀᴅѕ.");
        }

        set(inventory.getSize() / 2, ItemUtils.createItem(
                ItemUtils.parseMaterial(menus().getString(MENU_PATH + ".EMPTY-BUTTON.MATERIAL", "BARRIER")),
                replaceMenuPlaceholders(name),
                replaceMenuPlaceholders(lore)
        ));
    }

    private void buildBackButton() {
        String fallbackName = returnToProfileViewer ? "&cʙᴀᴄᴋ" : "&cᴄʟᴏѕᴇ";
        List<String> fallbackLore = returnToProfileViewer
                ? List.of("&7ʀᴇᴛᴜʀɴ ᴛᴏ ᴛʜᴇ ᴘʀᴏꜰɪʟᴇ ᴠɪᴇᴡᴇʀ.")
                : List.of("&7ᴄʟᴏѕᴇ ᴛʜɪѕ ʜɪѕᴛᴏʀʏ ᴍᴇɴᴜ.");

        set(BACK_SLOT, ItemUtils.createItem(
                ItemUtils.parseMaterial(menus().getString(MENU_PATH + ".BUTTONS.BACK.MATERIAL", "ARROW")),
                replaceMenuPlaceholders(menus().getString(MENU_PATH + ".BUTTONS.BACK.DISPLAY-NAME", fallbackName)),
                replaceMenuPlaceholders(defaultIfEmpty(menus().getStringList(MENU_PATH + ".BUTTONS.BACK.LORE"), fallbackLore))
        ));
    }

    private void buildFilterStateButton() {
        set(FILTER_STATE_SLOT, ItemUtils.createItem(
                ItemUtils.parseMaterial(menus().getString(MENU_PATH + ".BUTTONS.FILTER-STATE.MATERIAL", "HOPPER")),
                replaceMenuPlaceholders(menus().getString(MENU_PATH + ".BUTTONS.FILTER-STATE.DISPLAY-NAME", "&dѕᴛᴀᴛᴇ ꜰɪʟᴛᴇʀ")),
                replaceMenuPlaceholders(defaultIfEmpty(
                        menus().getStringList(MENU_PATH + ".BUTTONS.FILTER-STATE.LORE"),
                        List.of("&7ᴄᴜʀʀᴇɴᴛ: &f{state_filter}", "&aᴄʟɪᴄᴋ ᴛᴏ ᴄʜᴀɴɢᴇ")
                ))
        ));
    }

    private void buildFilterTypeButton() {
        set(FILTER_TYPE_SLOT, ItemUtils.createItem(
                ItemUtils.parseMaterial(menus().getString(MENU_PATH + ".BUTTONS.FILTER-TYPE.MATERIAL", "BOOK")),
                replaceMenuPlaceholders(menus().getString(MENU_PATH + ".BUTTONS.FILTER-TYPE.DISPLAY-NAME", "&dᴛʏᴘᴇ ꜰɪʟᴛᴇʀ")),
                replaceMenuPlaceholders(defaultIfEmpty(
                        menus().getStringList(MENU_PATH + ".BUTTONS.FILTER-TYPE.LORE"),
                        List.of("&7ᴄᴜʀʀᴇɴᴛ: &f{type_filter}", "&aᴄʟɪᴄᴋ ᴛᴏ ᴄʜᴀɴɢᴇ")
                ))
        ));
    }

    private void buildRefreshButton() {
        set(REFRESH_SLOT, ItemUtils.createItem(
                ItemUtils.parseMaterial(menus().getString(MENU_PATH + ".BUTTONS.REFRESH.MATERIAL", "CLOCK")),
                replaceMenuPlaceholders(menus().getString(MENU_PATH + ".BUTTONS.REFRESH.DISPLAY-NAME", "&dʀᴇꜰʀᴇѕʜ")),
                replaceMenuPlaceholders(defaultIfEmpty(
                        menus().getStringList(MENU_PATH + ".BUTTONS.REFRESH.LORE"),
                        List.of("&7ʀᴇʟᴏᴀᴅ ᴛʜɪѕ ᴘʟᴀʏᴇʀ'ѕ ᴘᴜɴɪѕʜᴍᴇɴᴛ ʜɪѕᴛᴏʀʏ.")
                ))
        ));
    }

    private void buildPageButtons() {
        Material material = ItemUtils.parseMaterial(menus().getString("GLOBAL.PAGE-MENU.MATERIAL", "ARROW"));

        if (hasPreviousPage) {
            set(PREVIOUS_PAGE_SLOT, ItemUtils.createItem(
                    material,
                    menus().getString("GLOBAL.PAGE-MENU.BACK-BUTTON", "&aʙᴀᴄᴋ"),
                    menus().getStringList("GLOBAL.PAGE-MENU.BACK-LORE")
            ));
        }

        set(PAGE_INFO_SLOT, ItemUtils.createItem(
                Material.BOOK,
                "&eᴘᴀɢᴇ " + (page + 1) + "&7/&e" + totalPages,
                List.of(
                        "&fʀᴇᴄᴏʀᴅѕ: &7" + NumberUtils.format(totalItems),
                        "&fᴛʏᴘᴇ: &7" + currentTypeFilterLabel(),
                        "&fѕᴛᴀᴛᴇ: &7" + query.stateFilter().getDisplayName()
                )
        ));

        if (hasNextPage) {
            set(NEXT_PAGE_SLOT, ItemUtils.createItem(
                    material,
                    menus().getString("GLOBAL.PAGE-MENU.NEXT-BUTTON", "&aɴᴇxᴛ"),
                    menus().getStringList("GLOBAL.PAGE-MENU.NEXT-LORE")
            ));
        }
    }

    private ItemStack createPunishmentItem(PunishmentRecord record) {
        PunishmentState state = plugin.getPunishmentManager().getState(record);
        String materialPath = MENU_PATH + ".PUNISHMENT-ITEM.MATERIALS." + record.getType().name();
        Material material = ItemUtils.parseMaterial(menus().getString(materialPath, defaultMaterial(record.getType())));
        String displayNameTemplate = menus().getString(MENU_PATH + ".PUNISHMENT-ITEM.DISPLAY-NAME", "{status_color}{type}");
        List<String> loreTemplate = defaultIfEmpty(
                menus().getStringList(MENU_PATH + ".PUNISHMENT-ITEM.LORE"),
                List.of(
                        "&7ʀᴇᴀѕᴏɴ: &f{reason}",
                        "&7ɪѕѕᴜᴇᴅ ʙʏ: &f{issuer}",
                        "&7ᴅᴀᴛᴇ: &f{issued_at}",
                        "&7ᴇxᴘɪʀᴇѕ: &f{expires_at}",
                        "&7ѕᴛᴀᴛᴜѕ: {status_color}{status}",
                        "&7ʀᴇᴍᴏᴠᴇᴅ ʙʏ: &f{removed_by}",
                        "&7ʀᴇᴍᴏᴠᴀʟ ʀᴇᴀѕᴏɴ: &f{removal_reason}",
                        "&7ʀᴇᴍᴏᴠᴇᴅ ᴀᴛ: &f{removed_at}",
                        "&7ɪᴅ: &f#{id}"
                )
        );

        return ItemUtils.createItem(
                material,
                replacePunishmentPlaceholders(displayNameTemplate, record, state),
                replacePunishmentPlaceholders(loreTemplate, record, state)
        );
    }

    private String replaceMenuPlaceholders(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("{player}", plugin.getPunishmentManager().resolveTargetName(targetUuid))
                .replace("{type_filter}", currentTypeFilterLabel())
                .replace("{state_filter}", query.stateFilter().getDisplayName())
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

    private String replacePunishmentPlaceholders(String value, PunishmentRecord record, PunishmentState state) {
        if (value == null) {
            return "";
        }

        String removedBy = safeText(record.getRemovedByNameSnapshot());
        String removalReason = safeText(record.getRemovalReason());
        String removedAt = formatOptionalTimestamp(record.getRemovedAt(), "ɴ/ᴀ");

        if (state == PunishmentState.EXPIRED) {
            if (removedBy.equals("ɴ/ᴀ")) {
                removedBy = "System";
            }
            if (removalReason.equals("ɴ/ᴀ")) {
                removalReason = "Expired";
            }
            if (removedAt.equals("ɴ/ᴀ")) {
                removedAt = formatOptionalTimestamp(record.getExpiresAt(), "ɴ/ᴀ");
            }
        }

        return replaceMenuPlaceholders(value)
                .replace("{status_color}", statusColor(record, state))
                .replace("{type}", plugin.getPunishmentManager().getDisplayType(record))
                .replace("{reason}", record.getReason())
                .replace("{issuer}", safeText(record.getIssuerNameSnapshot()))
                .replace("{issued_at}", formatOptionalTimestamp(record.getIssuedAt(), "ᴜɴᴋɴᴏᴡɴ"))
                .replace("{expires_at}", formatOptionalTimestamp(record.getExpiresAt(), "Never"))
                .replace("{status}", state.getDisplayName())
                .replace("{removed_by}", removedBy)
                .replace("{removal_reason}", removalReason)
                .replace("{removed_at}", removedAt)
                .replace("{id}", String.valueOf(record.getId()))
                .replace("{scope}", record.getScope().name())
                .replace("{source_server}", record.getSourceServer());
    }

    private List<String> replacePunishmentPlaceholders(List<String> lines, PunishmentRecord record, PunishmentState state) {
        List<String> replaced = new ArrayList<>();
        for (String line : lines) {
            replaced.add(replacePunishmentPlaceholders(line, record, state));
        }
        return replaced;
    }

    private String currentTypeFilterLabel() {
        return query.typeFilter() == null ? "All" : query.typeFilter().name();
    }

    private String defaultMaterial(PunishmentType type) {
        return switch (type) {
            case BAN -> "IRON_BARS";
            case MUTE -> "PAPER";
            case WARN -> "YELLOW_DYE";
            case KICK -> "LEATHER_BOOTS";
            case BLACKLIST -> "BARRIER";
        };
    }

    private String statusColor(PunishmentRecord record, PunishmentState state) {
        if (state == PunishmentState.EXPIRED) {
            return "&6";
        }
        if (state == PunishmentState.REMOVED) {
            return "&7";
        }

        return switch (record.getType()) {
            case BAN, BLACKLIST -> "&c";
            case MUTE -> "&d";
            case WARN -> "&e";
            case KICK -> "&6";
        };
    }

    private String formatOptionalTimestamp(Long timestamp, String fallback) {
        if (timestamp == null || timestamp <= 0L) {
            return fallback;
        }
        return DATE_FORMATTER.format(Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()));
    }

    private String safeText(String value) {
        return value == null || value.isBlank() ? "ɴ/ᴀ" : value;
    }

    private List<String> defaultIfEmpty(List<String> configured, List<String> fallback) {
        return configured == null || configured.isEmpty() ? fallback : configured;
    }

    private FileConfiguration menus() {
        return plugin.getConfigManager().getMenus();
    }

    private static String configuredTitle(UltimateDonutSmp plugin, UUID targetUuid) {
        String template = plugin.getConfigManager().getMenus().getString(MENU_PATH + ".TITLE", "&8ᴘᴜɴɪѕʜᴍᴇɴᴛѕ ({player})");
        return template.replace("{player}", plugin.getPunishmentManager().resolveTargetName(targetUuid));
    }

    private static int configuredSize(UltimateDonutSmp plugin) {
        int size = plugin.getConfigManager().getMenus().getInt(MENU_PATH + ".SIZE", 54);
        return size >= 27 && size % 9 == 0 ? size : 54;
    }
}
