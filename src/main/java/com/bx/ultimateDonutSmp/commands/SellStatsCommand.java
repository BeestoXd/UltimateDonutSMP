package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.DatabaseManager;
import com.bx.ultimateDonutSmp.menus.SellStatsAdminMenu;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import com.bx.ultimateDonutSmp.utils.PermissionUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SellStatsCommand implements CommandExecutor, TabCompleter {

    private static final String PERMISSION = "ultimatedonutsmp.admin.sellstats";

    private final UltimateDonutSmp plugin;

    public SellStatsCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!PermissionUtils.has(sender, PERMISSION) && !sender.hasPermission("ultimatedonutsmp.admin")) {
            sender.sendMessage(ColorUtils.toComponent("&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ ᴛᴏ ᴠɪᴇᴡ ᴀᴅᴍɪɴ ѕᴇʟʟ ѕᴛᴀᴛɪѕᴛɪᴄѕ."));
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("gui") || args[0].equalsIgnoreCase("menu")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(ColorUtils.toComponent("&cOnly players can open the Sell Stats GUI. Use /" + label + " export or /" + label + " items."));
                return true;
            }
            new SellStatsAdminMenu(plugin).open(player);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "items", "revenue", "topitems" -> handleTopItems(sender, args);
            case "volume", "topvolume" -> handleTopVolume(sender, args);
            case "sellers", "topsellers", "players" -> handleTopSellers(sender, args);
            case "export", "report" -> handleExport(sender);
            case "web", "site", "url", "paste" -> new com.bx.ultimateDonutSmp.managers.SellStatsExporter(plugin).uploadToWebPaste(sender);
            case "html" -> new com.bx.ultimateDonutSmp.managers.SellStatsExporter(plugin).exportHtml(sender);
            default -> sendUsage(sender, label);
        }

        return true;
    }

    private void handleTopItems(CommandSender sender, String[] args) {
        int limit = args.length >= 2 ? parseLimit(args[1], 10) : 10;
        double totalRevenue = plugin.getDatabaseManager().getTotalSellRevenue();
        List<DatabaseManager.TopSoldItemEntry> items = plugin.getDatabaseManager().getTopSoldItemsByRevenue(limit);

        sender.sendMessage(ColorUtils.toComponent("&8&m---------- &b&lTOP REVENUE SOLD ITEMS &8&m----------"));
        if (items.isEmpty()) {
            sender.sendMessage(ColorUtils.toComponent("&7No sell transaction history recorded yet."));
            return;
        }

        for (int i = 0; i < items.size(); i++) {
            DatabaseManager.TopSoldItemEntry entry = items.get(i);
            double pct = totalRevenue > 0 ? (entry.totalRevenue() / totalRevenue) * 100.0 : 0.0;
            String hint = pct >= 10.0 ? " &c[NERF RECOMMENDED]" : "";
            sender.sendMessage(ColorUtils.toComponent(
                    "&e#" + (i + 1) + " &f" + prettifyMaterial(entry.itemName())
                            + " &8- &a" + plugin.getCurrencyManager().formatMoney(entry.totalRevenue())
                            + " &7(" + String.format(Locale.US, "%.1f%%", pct) + ")"
                            + " &8| &e" + NumberUtils.format(entry.totalAmount()) + " sold" + hint
            ));
        }
        sender.sendMessage(ColorUtils.toComponent("&7Total Server Revenue: &a" + plugin.getCurrencyManager().formatMoney(totalRevenue)));
    }

    private void handleTopVolume(CommandSender sender, String[] args) {
        int limit = args.length >= 2 ? parseLimit(args[1], 10) : 10;
        long totalVolume = plugin.getDatabaseManager().getTotalItemsSold();
        List<DatabaseManager.TopSoldItemEntry> items = plugin.getDatabaseManager().getTopSoldItemsByVolume(limit);

        sender.sendMessage(ColorUtils.toComponent("&8&m---------- &b&lTOP VOLUME SOLD ITEMS &8&m----------"));
        if (items.isEmpty()) {
            sender.sendMessage(ColorUtils.toComponent("&7No sell transaction history recorded yet."));
            return;
        }

        for (int i = 0; i < items.size(); i++) {
            DatabaseManager.TopSoldItemEntry entry = items.get(i);
            sender.sendMessage(ColorUtils.toComponent(
                    "&e#" + (i + 1) + " &f" + prettifyMaterial(entry.itemName())
                            + " &8- &e" + NumberUtils.format(entry.totalAmount()) + " sold"
                            + " &8| &a" + plugin.getCurrencyManager().formatMoney(entry.totalRevenue())
            ));
        }
    }

    private void handleTopSellers(CommandSender sender, String[] args) {
        int limit = args.length >= 2 ? parseLimit(args[1], 10) : 10;
        double totalRevenue = plugin.getDatabaseManager().getTotalSellRevenue();
        List<DatabaseManager.TopSellerEntry> sellers = plugin.getDatabaseManager().getTopSellers(limit);

        sender.sendMessage(ColorUtils.toComponent("&8&m---------- &b&lTOP SELLING PLAYERS &8&m----------"));
        if (sellers.isEmpty()) {
            sender.sendMessage(ColorUtils.toComponent("&7No sell transaction history recorded yet."));
            return;
        }

        for (int i = 0; i < sellers.size(); i++) {
            DatabaseManager.TopSellerEntry entry = sellers.get(i);
            double pct = totalRevenue > 0 ? (entry.totalEarned() / totalRevenue) * 100.0 : 0.0;
            sender.sendMessage(ColorUtils.toComponent(
                    "&e#" + (i + 1) + " &f" + entry.playerName()
                            + " &8- &a" + plugin.getCurrencyManager().formatMoney(entry.totalEarned())
                            + " &7(" + String.format(Locale.US, "%.1f%%", pct) + ")"
                            + " &8| &e" + NumberUtils.format(entry.totalAmountSold()) + " items"
            ));
        }
    }

    private void handleExport(CommandSender sender) {
        sender.sendMessage(ColorUtils.toComponent("&7Exporting sell statistics report..."));
        Player player = sender instanceof Player p ? p : null;
        new SellStatsAdminMenu(plugin).exportReport(player);
    }

    private void sendUsage(CommandSender sender, String label) {
        sender.sendMessage(ColorUtils.toComponent("&cUsage: /" + label + " [gui|items|volume|sellers|export|web|html]"));
    }

    private int parseLimit(String input, int def) {
        try {
            return Math.max(1, Math.min(100, Integer.parseInt(input)));
        } catch (NumberFormatException ignored) {
            return def;
        }
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

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> options = List.of("gui", "items", "volume", "sellers", "export", "web", "site", "html");
            String filter = args[0].toLowerCase(Locale.ROOT);
            return options.stream().filter(o -> o.startsWith(filter)).toList();
        }
        return List.of();
    }
}
