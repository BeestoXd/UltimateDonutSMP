package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.PlayerLogsManager;
import com.bx.ultimateDonutSmp.models.PlayerLogEntry;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class ChatLogsMenu extends BaseMenu {

    private static final int PREVIOUS_PAGE_SLOT = 45;
    private static final int CLOSE_MENU_SLOT = 49;
    private static final int NEXT_PAGE_SLOT = 53;
    private static final int MAX_ITEMS_PER_PAGE = 45;
    private static final int LORE_LINE_LENGTH = 40;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final UUID targetUuid;
    private final String targetName;
    private int page = 0;
    private int totalItems;
    private int totalPages = 1;

    /** Pass a null uuid to browse every player's public chat instead of one player's. */
    public ChatLogsMenu(UltimateDonutSmp plugin, UUID targetUuid, String targetName) {
        super(
                plugin,
                targetUuid == null ? "&8Chat Log" : "&8Chat Log: " + targetName,
                54
        );
        this.targetUuid = targetUuid;
        this.targetName = targetName;
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);

        totalItems = plugin.getDatabaseManager()
                .getLogsByTypeCount(targetUuid, PlayerLogsManager.PUBLIC_CHAT_TYPE);
        totalPages = Math.max(1, (int) Math.ceil(totalItems / (double) MAX_ITEMS_PER_PAGE));
        if (page >= totalPages) {
            page = totalPages - 1;
        }
        if (page < 0) {
            page = 0;
        }

        int offset = page * MAX_ITEMS_PER_PAGE;
        List<PlayerLogEntry> logs = plugin.getDatabaseManager()
                .getLogsByType(targetUuid, PlayerLogsManager.PUBLIC_CHAT_TYPE, MAX_ITEMS_PER_PAGE, offset);

        if (logs.isEmpty()) {
            set(22, ItemUtils.createItem(
                    Material.BARRIER,
                    "&cNo chat messages",
                    List.of(targetUuid == null
                            ? "&7Nobody has said anything in public chat yet."
                            : "&7" + targetName + " has not said anything in public chat yet.")
            ));
        }

        for (int index = 0; index < logs.size() && index < MAX_ITEMS_PER_PAGE; index++) {
            set(index, createChatItem(logs.get(index)));
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
                List.of("&7Click to close this chat log.")
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

    private ItemStack createChatItem(PlayerLogEntry entry) {
        String name = entry.playerName() == null || entry.playerName().isBlank()
                ? "Unknown"
                : entry.playerName();

        List<String> lore = new ArrayList<>();
        lore.add("&7Time: &f" + DATE_FORMAT.format(new Date(entry.timestamp())));
        lore.add("&7Message:");
        for (String line : wrap(entry.details())) {
            lore.add("&f" + line);
        }

        return ItemUtils.createPlayerHead(
                Bukkit.getOfflinePlayer(entry.playerUuid()),
                "&e&l" + name,
                lore
        );
    }

    /** Breaks a chat message over several lore lines so long messages stay readable. */
    static List<String> wrap(String message) {
        List<String> lines = new ArrayList<>();
        if (message == null || message.isBlank()) {
            lines.add("");
            return lines;
        }

        StringBuilder line = new StringBuilder();
        for (String word : message.trim().split("\s+")) {
            while (word.length() > LORE_LINE_LENGTH) {
                if (line.length() > 0) {
                    lines.add(line.toString());
                    line.setLength(0);
                }
                lines.add(word.substring(0, LORE_LINE_LENGTH));
                word = word.substring(LORE_LINE_LENGTH);
            }
            if (line.length() > 0 && line.length() + 1 + word.length() > LORE_LINE_LENGTH) {
                lines.add(line.toString());
                line.setLength(0);
            }
            if (line.length() > 0) {
                line.append(' ');
            }
            line.append(word);
        }
        if (line.length() > 0) {
            lines.add(line.toString());
        }
        return lines;
    }
}
