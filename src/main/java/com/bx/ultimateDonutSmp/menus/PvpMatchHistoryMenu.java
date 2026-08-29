package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.PvpMatchManager;
import com.bx.ultimateDonutSmp.models.PvpMatch;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A player's ranked matches, newest first, one item per match.
 *
 * <p>Everything about a match is in the item's lore rather than behind another click: the date, how
 * long it ran, who won, and both fighters' hits, crystals and Elo change. The numbers come from the
 * stored match row, so an old entry still reads correctly after the players' totals have moved on.</p>
 */
public class PvpMatchHistoryMenu extends BaseMenu {

    private static final String PATH = "MENUS.HISTORY";
    private static final int ITEMS_PER_PAGE = 45;
    private static final int PREVIOUS_PAGE_SLOT = 48;
    private static final int PAGE_INFO_SLOT = 49;
    private static final int NEXT_PAGE_SLOT = 50;

    private final UUID targetUuid;

    private int page;
    private int totalPages = 1;
    private boolean hasPreviousPage;
    private boolean hasNextPage;

    public PvpMatchHistoryMenu(UltimateDonutSmp plugin, UUID targetUuid) {
        super(plugin, configuredTitle(plugin, targetUuid), 54);
        this.targetUuid = targetUuid;
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.BLACK_STAINED_GLASS_PANE);

        PvpMatchManager matches = plugin.getPvpMatchManager();
        int total = matches.countHistory(targetUuid);
        totalPages = Math.max(1, (int) Math.ceil(total / (double) ITEMS_PER_PAGE));
        if (page >= totalPages) {
            page = totalPages - 1;
        }

        int offset = page * ITEMS_PER_PAGE;
        hasPreviousPage = page > 0;
        hasNextPage = offset + ITEMS_PER_PAGE < total;

        List<PvpMatch> history = matches.getHistory(targetUuid, ITEMS_PER_PAGE, offset);
        if (history.isEmpty()) {
            set(22, ItemUtils.createItem(Material.BARRIER, "&cNo matches",
                    List.of("&7This player has not fought a ranked match yet.")));
        } else {
            for (int index = 0; index < history.size(); index++) {
                set(index, createMatchItem(history.get(index), offset + index + 1));
            }
        }

        buildPageButtons(total);
    }

    private ItemStack createMatchItem(PvpMatch match, int number) {
        PvpMatchManager matches = plugin.getPvpMatchManager();
        UUID opponent = match.opponentOf(targetUuid);
        boolean won = match.getWinnerUuid() != null && match.getWinnerUuid().equals(targetUuid);
        boolean decided = match.getResult() == PvpMatch.Result.DECIDED;

        String result = decided
                ? (won ? "&aWIN" : "&cLOSS")
                : (match.getResult() == PvpMatch.Result.DRAW ? "&eDRAW" : "&7ABORTED");

        List<String> lore = new ArrayList<>();
        lore.add("&7Date: &f" + matches.formatDate(match.getEndedAt()));
        lore.add("&7Duration: &f"
                + PvpMatchManager.formatMatchDuration(match.getDurationSeconds(match.getEndedAt())));
        lore.add("&7Result: " + result);
        lore.add("");
        lore.add("&f" + matches.resolveName(match.getFirstUuid())
                + " &7vs &f" + matches.resolveName(match.getSecondUuid()));
        lore.add("");
        appendPlayerLines(lore, match, match.getFirstUuid());
        lore.add("");
        appendPlayerLines(lore, match, match.getSecondUuid());

        return ItemUtils.createPlayerHead(
                opponent == null ? null : Bukkit.getOfflinePlayer(opponent),
                (won ? "&a" : "&c") + "Match #" + number,
                lore
        );
    }

    private void appendPlayerLines(List<String> lore, PvpMatch match, UUID uuid) {
        PvpMatchManager matches = plugin.getPvpMatchManager();
        int after = match.getEloBefore(uuid) + match.getEloDelta(uuid);
        lore.add("&a" + matches.resolveName(uuid));
        lore.add("&7Hits: &f" + match.getHits(uuid));
        lore.add("&7Crystals: &f" + match.getCrystals(uuid));
        lore.add("&7ELO: &f" + after + " &8(" + deltaColor(match.getEloDelta(uuid))
                + PvpMatchManager.formatDelta(match.getEloDelta(uuid)) + "&8)");
    }

    private String deltaColor(int delta) {
        if (delta > 0) {
            return "&a";
        }
        return delta < 0 ? "&c" : "&7";
    }

    private void buildPageButtons(int total) {
        Material arrow = ItemUtils.parseMaterial(
                plugin.getConfigManager().getMenus().getString("GLOBAL.PAGE-MENU.MATERIAL", "ARROW"));

        if (hasPreviousPage) {
            set(PREVIOUS_PAGE_SLOT, ItemUtils.createItem(arrow, "&ePrevious page", List.of("&7Page " + page)));
        }
        if (hasNextPage) {
            set(NEXT_PAGE_SLOT, ItemUtils.createItem(arrow, "&eNext page", List.of("&7Page " + (page + 2))));
        }
        set(PAGE_INFO_SLOT, ItemUtils.createItem(Material.PAPER,
                "&ePage &f" + (page + 1) + "&7/&f" + totalPages,
                List.of("&7Matches: &f" + total)));
    }

    @Override
    public void handleClick(int slot, Player player, ClickType clickType) {
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

    private static String configuredTitle(UltimateDonutSmp plugin, UUID targetUuid) {
        FileConfiguration pvp = plugin.getConfigManager().getPvp();
        String template = pvp.getString(PATH + ".TITLE", "&8Match history &7- &f{player}");
        return template.replace("{player}", plugin.getPvpMatchManager().resolveName(targetUuid));
    }
}
