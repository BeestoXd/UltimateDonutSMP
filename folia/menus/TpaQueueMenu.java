package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.TPAManager;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TpaQueueMenu extends BaseMenu {

    private static final String MENU_PATH = "TPA-QUEUE-MENU";
    private static final int[] DEFAULT_CONTENT_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    private final boolean tpaHere;
    private final Map<Integer, UUID> queuedRequestSlots = new HashMap<>();
    private int page;
    private boolean hasPreviousPage;
    private boolean hasNextPage;

    public TpaQueueMenu(UltimateDonutSmp plugin, boolean tpaHere) {
        this(plugin, tpaHere, 0);
    }

    public TpaQueueMenu(UltimateDonutSmp plugin, boolean tpaHere, int page) {
        super(plugin, configuredTitle(plugin, tpaHere), configuredSize(plugin));
        this.tpaHere = tpaHere;
        this.page = Math.max(0, page);
    }

    @Override
    public void build(Player player) {
        clear();
        queuedRequestSlots.clear();

        FileConfiguration menus = menus();
        if (menus.getBoolean(MENU_PATH + ".PLACEHOLDER", true)) {
            fill(ItemUtils.parseMaterial(menus.getString(MENU_PATH + ".PLACEHOLDER-MATERIAL", "GRAY_STAINED_GLASS_PANE")));
        }

        List<Integer> contentSlots = contentSlots();
        List<TPAManager.TpaQueueEntry> entries = plugin.getTPAManager()
                .getQueuedRequests(player.getUniqueId(), tpaHere);
        int itemsPerPage = Math.max(1, contentSlots.size());
        int totalPages = Math.max(1, (int) Math.ceil(entries.size() / (double) itemsPerPage));
        page = Math.max(0, Math.min(page, totalPages - 1));

        int start = page * itemsPerPage;
        int end = Math.min(entries.size(), start + itemsPerPage);
        for (int index = start; index < end; index++) {
            TPAManager.TpaQueueEntry entry = entries.get(index);
            int slot = contentSlots.get(index - start);
            set(slot, createQueueItem(entry));
            queuedRequestSlots.put(slot, entry.requester());
        }

        if (entries.isEmpty()) {
            buildEmptyState();
        }

        hasPreviousPage = page > 0;
        hasNextPage = end < entries.size();
        buildNavigation(entries.size(), totalPages);
    }

    @Override
    public void handleClick(int slot, Player player) {
        UUID requesterUuid = queuedRequestSlots.get(slot);
        if (requesterUuid != null) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            if (!plugin.getTPAManager().acceptQueuedRequest(player, requesterUuid, tpaHere)) {
                player.sendMessage(com.bx.ultimateDonutSmp.utils.ColorUtils.toComponent(
                        menus().getString(MENU_PATH + ".MESSAGES.REQUEST-MISSING", "&cThat queued request is no longer available.")
                ));
            }
            build(player);
            return;
        }

        int previousSlot = menus().getInt(MENU_PATH + ".BUTTONS.PREVIOUS.SLOT", 45);
        int randomSlot = menus().getInt(MENU_PATH + ".BUTTONS.RANDOM.SLOT", 49);
        int nextSlot = menus().getInt(MENU_PATH + ".BUTTONS.NEXT.SLOT", 53);

        if (slot == previousSlot && hasPreviousPage) {
            page--;
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.PAGE-TURN"));
            build(player);
            return;
        }

        if (slot == nextSlot && hasNextPage) {
            page++;
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.PAGE-TURN"));
            build(player);
            return;
        }

        if (slot == randomSlot) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            if (!plugin.getTPAManager().acceptRandomQueuedRequest(player, tpaHere)) {
                player.sendMessage(com.bx.ultimateDonutSmp.utils.ColorUtils.toComponent(
                        menus().getString(MENU_PATH + ".MESSAGES.EMPTY", "&cThere are no queued requests to accept.")
                ));
            }
            build(player);
        }
    }

    private void buildEmptyState() {
        String path = MENU_PATH + ".EMPTY-BUTTON";
        set(menus().getInt(path + ".SLOT", 22), ItemUtils.createItem(
                ItemUtils.parseMaterial(menus().getString(path + ".MATERIAL", "BARRIER")),
                replaceStaticPlaceholders(menus().getString(path + ".DISPLAY-NAME", "&cNo queued requests")),
                replaceStaticPlaceholders(defaultIfEmpty(
                        menus().getStringList(path + ".LORE"),
                        List.of("&7Requests sent while this setting was disabled will appear here.")
                ))
        ));
    }

    private void buildNavigation(int totalItems, int totalPages) {
        Map<String, String> placeholders = Map.of(
                "page", String.valueOf(page + 1),
                "previous_page", String.valueOf(Math.max(1, page)),
                "next_page", String.valueOf(Math.min(totalPages, page + 2)),
                "total_pages", String.valueOf(totalPages),
                "total", String.valueOf(totalItems)
        );
        buildButton("PREVIOUS", hasPreviousPage, placeholders);
        buildButton("RANDOM", totalItems > 0, placeholders);
        buildButton("NEXT", hasNextPage, placeholders);
    }

    private void buildButton(String key, boolean active, Map<String, String> placeholders) {
        String path = MENU_PATH + ".BUTTONS." + key;
        int slot = menus().getInt(path + ".SLOT", switch (key) {
            case "PREVIOUS" -> 45;
            case "RANDOM" -> 49;
            case "NEXT" -> 53;
            default -> -1;
        });
        if (slot < 0 || slot >= inventory.getSize()) {
            return;
        }

        Material material = ItemUtils.parseMaterial(menus().getString(path + ".MATERIAL", active ? "ARROW" : "BARRIER"));
        if (!active && !"RANDOM".equals(key)) {
            material = ItemUtils.parseMaterial(menus().getString(path + ".DISABLED-MATERIAL", "BARRIER"));
        }
        String displayName = menus().getString(path + ".DISPLAY-NAME", "&a" + key.toLowerCase());
        List<String> lore = menus().getStringList(path + ".LORE");
        set(slot, ItemUtils.createItem(material, replace(displayName, placeholders), replace(lore, placeholders)));
    }

    private ItemStack createQueueItem(TPAManager.TpaQueueEntry entry) {
        String playerName = resolveName(entry.requester());
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", playerName);
        placeholders.put("position", String.valueOf(entry.position()));
        placeholders.put("expires", formatRemaining(entry.expiresAtMillis()));
        placeholders.put("type", requestType());

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(entry.requester());
        return ItemUtils.createPlayerHead(
                offlinePlayer,
                replace(menus().getString(MENU_PATH + ".REQUEST-BUTTON.DISPLAY-NAME", "&b{player}"), placeholders),
                replace(defaultIfEmpty(
                        menus().getStringList(MENU_PATH + ".REQUEST-BUTTON.LORE"),
                        List.of(
                                "&7Nickname: &f{player}",
                                "&7Queue: &f#{position}",
                                "&7Expires: &f{expires}",
                                "",
                                "&eClick to accept."
                        )
                ), placeholders)
        );
    }

    private List<Integer> contentSlots() {
        List<Integer> configured = menus().getIntegerList(MENU_PATH + ".CONTENT-SLOTS");
        if (!configured.isEmpty()) {
            List<Integer> validConfigured = configured.stream()
                    .filter(slot -> slot >= 0 && slot < inventory.getSize())
                    .toList();
            if (!validConfigured.isEmpty()) {
                return validConfigured;
            }
        }

        List<Integer> fallback = new ArrayList<>();
        for (int slot : DEFAULT_CONTENT_SLOTS) {
            if (slot >= 0 && slot < inventory.getSize()) {
                fallback.add(slot);
            }
        }
        return fallback;
    }

    private String resolveName(UUID uuid) {
        Player online = Bukkit.getPlayer(uuid);
        if (online != null) {
            return online.getName();
        }

        OfflinePlayer offline = Bukkit.getOfflinePlayer(uuid);
        String name = offline.getName();
        return name == null || name.isBlank() ? uuid.toString().substring(0, 8) : name;
    }

    private String formatRemaining(long expiresAtMillis) {
        long remainingSeconds = Math.max(0L, (expiresAtMillis - System.currentTimeMillis()) / 1000L);
        return NumberUtils.formatCountdown(remainingSeconds);
    }

    private String replaceStaticPlaceholders(String value) {
        return value == null ? "" : value
                .replace("{type}", requestType())
                .replace("{page}", String.valueOf(page + 1));
    }

    private List<String> replaceStaticPlaceholders(List<String> lines) {
        List<String> replaced = new ArrayList<>();
        for (String line : lines) {
            replaced.add(replaceStaticPlaceholders(line));
        }
        return replaced;
    }

    private String replace(String value, Map<String, String> placeholders) {
        String output = replaceStaticPlaceholders(value);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            output = output.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return output;
    }

    private List<String> replace(List<String> values, Map<String, String> placeholders) {
        List<String> replaced = new ArrayList<>();
        for (String value : values) {
            replaced.add(replace(value, placeholders));
        }
        return replaced;
    }

    private List<String> defaultIfEmpty(List<String> configured, List<String> fallback) {
        return configured == null || configured.isEmpty() ? fallback : configured;
    }

    private String requestType() {
        return tpaHere ? "/tpahere" : "/tpa";
    }

    private FileConfiguration menus() {
        return plugin.getConfigManager().getMenus();
    }

    private static String configuredTitle(UltimateDonutSmp plugin, boolean tpaHere) {
        String type = tpaHere ? "/tpahere" : "/tpa";
        return plugin.getConfigManager().getMenus()
                .getString(MENU_PATH + ".TITLE", "&8{type} queue")
                .replace("{type}", type);
    }

    private static int configuredSize(UltimateDonutSmp plugin) {
        int size = plugin.getConfigManager().getMenus().getInt(MENU_PATH + ".SIZE", 54);
        return size >= 27 && size <= 54 && size % 9 == 0 ? size : 54;
    }
}
