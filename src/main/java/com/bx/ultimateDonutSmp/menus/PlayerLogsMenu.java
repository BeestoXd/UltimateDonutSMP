package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PlayerLogEntry;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class PlayerLogsMenu extends BaseMenu {

    private static final int PREVIOUS_PAGE_SLOT = 45;
    private static final int CLOSE_MENU_SLOT = 49;
    private static final int NEXT_PAGE_SLOT = 53;
    private static final int MAX_ITEMS_PER_PAGE = 45;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final UUID targetUuid;
    private final String targetName;
    private int page = 0;
    private int totalItems;
    private int totalPages = 1;

    public PlayerLogsMenu(UltimateDonutSmp plugin, UUID targetUuid, String targetName) {
        super(
                plugin,
                "&8Logs: " + targetName,
                54
        );
        this.targetUuid = targetUuid;
        this.targetName = targetName;
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);

        totalItems = plugin.getDatabaseManager().getPlayerLogsCount(targetUuid);
        totalPages = Math.max(1, (int) Math.ceil(totalItems / (double) MAX_ITEMS_PER_PAGE));
        if (page >= totalPages) {
            page = totalPages - 1;
        }
        if (page < 0) {
            page = 0;
        }

        int offset = page * MAX_ITEMS_PER_PAGE;
        List<PlayerLogEntry> logs = plugin.getDatabaseManager().getPlayerLogs(targetUuid, MAX_ITEMS_PER_PAGE, offset);

        for (int index = 0; index < logs.size() && index < MAX_ITEMS_PER_PAGE; index++) {
            PlayerLogEntry entry = logs.get(index);
            set(index, createLogItem(entry));
        }

        buildNavigation();
    }

    @Override
    public void handleClick(int slot, Player player) {
        if (slot == PREVIOUS_PAGE_SLOT && page > 0) {
            page--;
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.PAGE-TURN"));
            build(player);
            return;
        }

        if (slot == CLOSE_MENU_SLOT) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            player.closeInventory();
            return;
        }

        if (slot == NEXT_PAGE_SLOT && (page + 1) * MAX_ITEMS_PER_PAGE < totalItems) {
            page++;
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.PAGE-TURN"));
            build(player);
        }
    }

    private void buildNavigation() {
        Material material = Material.ARROW;

        // Close Menu Button
        set(CLOSE_MENU_SLOT, ItemUtils.createItem(
                Material.BARRIER,
                "&cClose Menu",
                List.of("&7Click to close this log view.")
        ));

        // Previous Page Button
        if (page > 0) {
            set(PREVIOUS_PAGE_SLOT, ItemUtils.createItem(
                    material,
                    "&aPrevious Page",
                    List.of("&7Go to page " + page + ".")
            ));
        } else {
            set(PREVIOUS_PAGE_SLOT, ItemUtils.createPlaceholder(Material.GRAY_STAINED_GLASS_PANE));
        }

        // Next Page Button
        if ((page + 1) * MAX_ITEMS_PER_PAGE < totalItems) {
            set(NEXT_PAGE_SLOT, ItemUtils.createItem(
                    material,
                    "&aNext Page",
                    List.of("&7Go to page " + (page + 2) + ".")
            ));
        } else {
            set(NEXT_PAGE_SLOT, ItemUtils.createPlaceholder(Material.GRAY_STAINED_GLASS_PANE));
        }
    }

    private ItemStack createLogItem(PlayerLogEntry entry) {
        Material material;
        switch (entry.category().toLowerCase()) {
            case "shop" -> material = Material.CHEST;
            case "auctions" -> material = Material.DIAMOND;
            case "economy" -> material = Material.GOLD_INGOT;
            case "crates" -> material = Material.TRIPWIRE_HOOK;
            case "spawners" -> material = Material.SPAWNER;
            default -> material = Material.PAPER;
        }

        String formattedTime = DATE_FORMAT.format(new Date(entry.timestamp()));
        List<String> lore = List.of(
                "&7Time: &f" + formattedTime,
                "&7Details: &f" + entry.details(),
                "&8(Category: " + entry.category() + ")"
        );

        return ItemUtils.createItem(material, "&e&l" + entry.logType(), lore);
    }
}
