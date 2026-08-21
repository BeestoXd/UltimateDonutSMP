package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.DatabaseManager;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SellStatsAdminMenu extends BaseMenu {

    public enum Mode {
        TOP_REVENUE,
        TOP_VOLUME,
        TOP_SELLERS,
        RECENT_LOGS
    }

    private static final int TAB_REVENUE_SLOT = 45;
    private static final int TAB_VOLUME_SLOT = 46;
    private static final int TAB_SELLERS_SLOT = 47;
    private static final int TAB_LOGS_SLOT = 48;
    private static final int EXPORT_SLOT = 49;
    private static final int PREVIOUS_PAGE_SLOT = 51;
    private static final int PAGE_INFO_SLOT = 52;
    private static final int NEXT_PAGE_SLOT = 53;

    private Mode mode = Mode.TOP_REVENUE;
    private int page = 0;
    private int totalPages = 1;

    public SellStatsAdminMenu(UltimateDonutSmp plugin) {
        super(plugin, "&8Sell statistics &7(admin)", 54);
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);

        int maxItemsPerPage = 45;
        double totalRevenue = plugin.getDatabaseManager().getTotalSellRevenue();
        long totalVolume = plugin.getDatabaseManager().getTotalItemsSold();
        int totalTransactions = plugin.getDatabaseManager().countGlobalSellHistory();

        switch (mode) {
            case TOP_REVENUE -> {
                List<DatabaseManager.TopSoldItemEntry> items = plugin.getDatabaseManager().getTopSoldItemsByRevenue(100);
                totalPages = Math.max(1, (int) Math.ceil(items.size() / (double) maxItemsPerPage));
                page = Math.max(0, Math.min(page, totalPages - 1));

                int start = page * maxItemsPerPage;
                int end = Math.min(items.size(), start + maxItemsPerPage);

                for (int i = start; i < end; i++) {
                    int slot = i - start;
                    DatabaseManager.TopSoldItemEntry entry = items.get(i);
                    set(slot, createTopItemByRevenueStack(entry, i + 1, totalRevenue));
                }
            }
            case TOP_VOLUME -> {
                List<DatabaseManager.TopSoldItemEntry> items = plugin.getDatabaseManager().getTopSoldItemsByVolume(100);
                totalPages = Math.max(1, (int) Math.ceil(items.size() / (double) maxItemsPerPage));
                page = Math.max(0, Math.min(page, totalPages - 1));

                int start = page * maxItemsPerPage;
                int end = Math.min(items.size(), start + maxItemsPerPage);

                for (int i = start; i < end; i++) {
                    int slot = i - start;
                    DatabaseManager.TopSoldItemEntry entry = items.get(i);
                    set(slot, createTopItemByVolumeStack(entry, i + 1, totalVolume));
                }
            }
            case TOP_SELLERS -> {
                List<DatabaseManager.TopSellerEntry> sellers = plugin.getDatabaseManager().getTopSellers(100);
                totalPages = Math.max(1, (int) Math.ceil(sellers.size() / (double) maxItemsPerPage));
                page = Math.max(0, Math.min(page, totalPages - 1));

                int start = page * maxItemsPerPage;
                int end = Math.min(sellers.size(), start + maxItemsPerPage);

                for (int i = start; i < end; i++) {
                    int slot = i - start;
                    DatabaseManager.TopSellerEntry entry = sellers.get(i);
                    set(slot, createTopSellerStack(entry, i + 1, totalRevenue));
                }
            }
            case RECENT_LOGS -> {
                totalPages = Math.max(1, (int) Math.ceil(totalTransactions / (double) maxItemsPerPage));
                page = Math.max(0, Math.min(page, totalPages - 1));

                int offset = page * maxItemsPerPage;
                List<DatabaseManager.GlobalSellHistoryEntry> logs = plugin.getDatabaseManager().getGlobalSellHistoryEntries(maxItemsPerPage, offset);

                for (int i = 0; i < logs.size(); i++) {
                    DatabaseManager.GlobalSellHistoryEntry log = logs.get(i);
                    set(i, createGlobalLogStack(log));
                }
            }
        }

        buildTabsAndControls(totalRevenue, totalVolume, totalTransactions);
    }

    private void buildTabsAndControls(double totalRevenue, long totalVolume, int totalTransactions) {
        // Tab 1: Top Revenue (Anvil)
        set(TAB_REVENUE_SLOT, ItemUtils.createItem(
                mode == Mode.TOP_REVENUE ? Material.GOLD_BLOCK : Material.GOLD_INGOT,
                (mode == Mode.TOP_REVENUE ? "&a&l" : "&e") + "Top Revenue Items",
                List.of(
                        "&7View items generating the most money.",
                        "&7Use this to &cnerf over-rewarding items&7!",
                        "",
                        mode == Mode.TOP_REVENUE ? "&a> Currently Selected" : "&eClick to view top revenue"
                )
        ));

        // Tab 2: Top Volume (Hopper)
        set(TAB_VOLUME_SLOT, ItemUtils.createItem(
                mode == Mode.TOP_VOLUME ? Material.HOPPER : Material.CHEST,
                (mode == Mode.TOP_VOLUME ? "&a&l" : "&e") + "Top Volume Items",
                List.of(
                        "&7View items sold in largest quantities.",
                        "",
                        mode == Mode.TOP_VOLUME ? "&a> Currently Selected" : "&eClick to view top volume"
                )
        ));

        // Tab 3: Top Sellers (Player Head)
        set(TAB_SELLERS_SLOT, ItemUtils.createItem(
                Material.PLAYER_HEAD,
                (mode == Mode.TOP_SELLERS ? "&a&l" : "&e") + "Top Sellers (Players)",
                List.of(
                        "&7View top players making money from /sell.",
                        "",
                        mode == Mode.TOP_SELLERS ? "&a> Currently Selected" : "&eClick to view top sellers"
                )
        ));

        // Tab 4: Recent Logs (Clock)
        set(TAB_LOGS_SLOT, ItemUtils.createItem(
                mode == Mode.RECENT_LOGS ? Material.CLOCK : Material.COMPASS,
                (mode == Mode.RECENT_LOGS ? "&a&l" : "&e") + "Recent Sales Log",
                List.of(
                        "&7View live global sales transaction log.",
                        "",
                        mode == Mode.RECENT_LOGS ? "&a> Currently Selected" : "&eClick to view recent logs"
                )
        ));

        // Export Button (Book & Quill)
        set(EXPORT_SLOT, ItemUtils.createItem(
                Material.WRITABLE_BOOK,
                "&b&lExport Sell Report",
                List.of(
                        "&7Generate a complete sales report file",
                        "&7saved to &fplugins/UltimateDonutSMP/sell-stats-report.txt",
                        "",
                        "&fTotal Revenue: &a" + plugin.getCurrencyManager().formatMoney(totalRevenue),
                        "&fTotal Items Sold: &e" + NumberUtils.format(totalVolume),
                        "&fTotal Sales Count: &b" + NumberUtils.format(totalTransactions),
                        "",
                        "&eClick to export report file"
                )
        ));

        // Page controls
        if (page > 0) {
            set(PREVIOUS_PAGE_SLOT, ItemUtils.createItem(
                    Material.ARROW,
                    "&aPrevious Page",
                    List.of("&7Go to page " + page)
            ));
        }

        set(PAGE_INFO_SLOT, ItemUtils.createItem(
                Material.BOOK,
                "&ePage " + (page + 1) + " / " + totalPages,
                List.of(
                        "&fMode: &7" + mode.name(),
                        "&fTotal Revenue: &a" + plugin.getCurrencyManager().formatMoney(totalRevenue),
                        "&fTotal Volume: &e" + NumberUtils.format(totalVolume)
                )
        ));

        if (page + 1 < totalPages) {
            set(NEXT_PAGE_SLOT, ItemUtils.createItem(
                    Material.ARROW,
                    "&aNext Page",
                    List.of("&7Go to page " + (page + 2))
            ));
        }
    }

    private ItemStack createTopItemByRevenueStack(DatabaseManager.TopSoldItemEntry entry, int rank, double grandTotalRevenue) {
        Material mat = ItemUtils.parseMaterial(entry.itemName());
        String displayName = "&e#" + rank + " &f" + prettifyMaterial(entry.itemName());
        double percent = grandTotalRevenue > 0 ? (entry.totalRevenue() / grandTotalRevenue) * 100.0 : 0.0;
        double avgPricePerUnit = entry.totalAmount() > 0 ? entry.totalRevenue() / entry.totalAmount() : 0.0;

        List<String> lore = new ArrayList<>();
        lore.add("&fTotal Revenue: &a" + plugin.getCurrencyManager().formatMoney(entry.totalRevenue()));
        lore.add("&fShare of Server Economy: &b" + String.format(Locale.US, "%.2f%%", percent));
        lore.add("&fTotal Units Sold: &e" + NumberUtils.format(entry.totalAmount()));
        lore.add("&fAverage Price/Unit: &a" + plugin.getCurrencyManager().formatMoney(avgPricePerUnit));
        lore.add("&fTransaction Count: &7" + NumberUtils.format(entry.count()));
        lore.add("");
        if (percent >= 10.0) {
            lore.add("&c&l[HIGH REVENUE - RECOMMENDED TO NERF PRICE]");
        } else if (percent >= 5.0) {
            lore.add("&e[MEDIUM REVENUE - MONITOR CLOSELY]");
        } else {
            lore.add("&7[BALANCED REVENUE]");
        }

        return ItemUtils.createItem(mat, displayName, lore);
    }

    private ItemStack createTopItemByVolumeStack(DatabaseManager.TopSoldItemEntry entry, int rank, long grandTotalVolume) {
        Material mat = ItemUtils.parseMaterial(entry.itemName());
        String displayName = "&e#" + rank + " &f" + prettifyMaterial(entry.itemName());
        double percent = grandTotalVolume > 0 ? (entry.totalAmount() / (double) grandTotalVolume) * 100.0 : 0.0;

        List<String> lore = List.of(
                "&fTotal Volume Sold: &e" + NumberUtils.format(entry.totalAmount()),
                "&fShare of Total Items: &b" + String.format(Locale.US, "%.2f%%", percent),
                "&fTotal Revenue Generated: &a" + plugin.getCurrencyManager().formatMoney(entry.totalRevenue()),
                "&fTransaction Count: &7" + NumberUtils.format(entry.count())
        );

        return ItemUtils.createItem(mat, displayName, lore);
    }

    private ItemStack createTopSellerStack(DatabaseManager.TopSellerEntry entry, int rank, double grandTotalRevenue) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            if (entry.playerName() != null && !entry.playerName().equals("Unknown")) {
                meta.setOwner(entry.playerName());
            }
            meta.setDisplayName(ColorUtils.toComponent("&e#" + rank + " &f" + entry.playerName()));

            double percent = grandTotalRevenue > 0 ? (entry.totalEarned() / grandTotalRevenue) * 100.0 : 0.0;
            List<String> lore = List.of(
                    "&fTotal Earned from Sell: &a" + plugin.getCurrencyManager().formatMoney(entry.totalEarned()),
                    "&fShare of Server Money: &b" + String.format(Locale.US, "%.2f%%", percent),
                    "&fTotal Items Sold: &e" + NumberUtils.format(entry.totalAmountSold()),
                    "&fTransactions: &7" + NumberUtils.format(entry.count())
            );

            meta.setLore(ColorUtils.toComponentList(lore));
            head.setItemMeta(meta);
        }
        return head;
    }

    private ItemStack createGlobalLogStack(DatabaseManager.GlobalSellHistoryEntry entry) {
        Material mat = ItemUtils.parseMaterial(entry.itemName());
        String displayName = "&f" + entry.playerName() + " &7sold &f" + prettifyMaterial(entry.itemName());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateStr = sdf.format(new Date(entry.timestamp()));

        List<String> lore = List.of(
                "&fPlayer: &b" + entry.playerName(),
                "&fItem: &e" + prettifyMaterial(entry.itemName()),
                "&fAmount: &e" + NumberUtils.format(entry.amount()),
                "&fPayout Earned: &a" + plugin.getCurrencyManager().formatMoney(entry.price()),
                "&fTime: &7" + dateStr
        );

        return ItemUtils.createItem(mat, displayName, lore);
    }

    @Override
    public void handleClick(int slot, Player player) {
        if (slot == TAB_REVENUE_SLOT) {
            mode = Mode.TOP_REVENUE;
            page = 0;
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            build(player);
            return;
        }

        if (slot == TAB_VOLUME_SLOT) {
            mode = Mode.TOP_VOLUME;
            page = 0;
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            build(player);
            return;
        }

        if (slot == TAB_SELLERS_SLOT) {
            mode = Mode.TOP_SELLERS;
            page = 0;
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            build(player);
            return;
        }

        if (slot == TAB_LOGS_SLOT) {
            mode = Mode.RECENT_LOGS;
            page = 0;
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            build(player);
            return;
        }

        if (slot == EXPORT_SLOT) {
            exportReport(player);
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            return;
        }

        if (slot == PREVIOUS_PAGE_SLOT && page > 0) {
            page--;
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.PAGE-TURN"));
            build(player);
            return;
        }

        if (slot == NEXT_PAGE_SLOT && page + 1 < totalPages) {
            page++;
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.PAGE-TURN"));
            build(player);
        }
    }

    public void exportReport(Player player) {
        plugin.getSpigotScheduler().runAsync(() -> {
            try {
                File reportFile = new File(plugin.getDataFolder(), "sell-stats-report.txt");
                double totalRevenue = plugin.getDatabaseManager().getTotalSellRevenue();
                long totalVolume = plugin.getDatabaseManager().getTotalItemsSold();
                int totalTransactions = plugin.getDatabaseManager().countGlobalSellHistory();
                List<DatabaseManager.TopSoldItemEntry> topRevenue = plugin.getDatabaseManager().getTopSoldItemsByRevenue(20);
                List<DatabaseManager.TopSoldItemEntry> topVolume = plugin.getDatabaseManager().getTopSoldItemsByVolume(20);
                List<DatabaseManager.TopSellerEntry> topSellers = plugin.getDatabaseManager().getTopSellers(20);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String dateStr = sdf.format(new Date());

                try (PrintWriter writer = new PrintWriter(new FileWriter(reportFile, StandardCharsets.UTF_8))) {
                    writer.println("=================================================");
                    writer.println("   ULTIMATEDONUTSMP - ADMIN SELL STATISTICS REPORT");
                    writer.println("   Generated At: " + dateStr);
                    writer.println("=================================================");
                    writer.println("Global Summary:");
                    writer.println(" - Total Revenue Generated: " + String.format(Locale.US, "%.2f", totalRevenue));
                    writer.println(" - Total Items Sold: " + totalVolume);
                    writer.println(" - Total Transactions: " + totalTransactions);
                    writer.println();
                    writer.println("-------------------------------------------------");
                    writer.println("TOP 20 ITEMS BY REVENUE (CANDIDATES FOR PRICE NERF)");
                    writer.println("-------------------------------------------------");
                    for (int i = 0; i < topRevenue.size(); i++) {
                        DatabaseManager.TopSoldItemEntry entry = topRevenue.get(i);
                        double pct = totalRevenue > 0 ? (entry.totalRevenue() / totalRevenue) * 100.0 : 0.0;
                        writer.printf(Locale.US, "%2d. %-24s | Revenue: $%12.2f (%5.2f%%) | Units: %10d | Count: %6d %s%n",
                                (i + 1), entry.itemName(), entry.totalRevenue(), pct, entry.totalAmount(), entry.count(),
                                (pct >= 10.0 ? "[HIGH REVENUE - NERF RECOMMENDED]" : ""));
                    }
                    writer.println();
                    writer.println("-------------------------------------------------");
                    writer.println("TOP 20 ITEMS BY VOLUME SOLD");
                    writer.println("-------------------------------------------------");
                    for (int i = 0; i < topVolume.size(); i++) {
                        DatabaseManager.TopSoldItemEntry entry = topVolume.get(i);
                        writer.printf(Locale.US, "%2d. %-24s | Units: %10d | Revenue: $%12.2f | Count: %6d%n",
                                (i + 1), entry.itemName(), entry.totalAmount(), entry.totalRevenue(), entry.count());
                    }
                    writer.println();
                    writer.println("-------------------------------------------------");
                    writer.println("TOP 20 PLAYERS BY EARNED MONEY");
                    writer.println("-------------------------------------------------");
                    for (int i = 0; i < topSellers.size(); i++) {
                        DatabaseManager.TopSellerEntry entry = topSellers.get(i);
                        double pct = totalRevenue > 0 ? (entry.totalEarned() / totalRevenue) * 100.0 : 0.0;
                        writer.printf(Locale.US, "%2d. %-20s | Earned: $%12.2f (%5.2f%%) | Units: %10d | Count: %6d%n",
                                (i + 1), entry.playerName(), entry.totalEarned(), pct, entry.totalAmountSold(), entry.count());
                    }
                    writer.println("=================================================");
                }

                if (player != null && player.isOnline()) {
                    player.sendMessage(ColorUtils.toComponent("&a&l[Sell Stats] &fExport complete! Report saved to &eplugins/UltimateDonutSMP/sell-stats-report.txt"));
                }
                plugin.getLogger().info("Sell statistics report exported to " + reportFile.getAbsolutePath());
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to export sell statistics report: " + e.getMessage());
                if (player != null && player.isOnline()) {
                    player.sendMessage(ColorUtils.toComponent("&c[Sell Stats] Failed to export report file. Check console."));
                }
            }
        });
    }

    private String prettifyMaterial(String raw) {
        String[] words = raw.toLowerCase(Locale.US).split("_");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (w.isEmpty()) continue;
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1));
        }
        return sb.toString();
    }
}
