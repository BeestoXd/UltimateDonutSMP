package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.logging.Level;

public class SellStatsExporter {

    private static HttpServer activeHttpServer = null;
    private static int actualBoundPort = 8080;

    private static volatile String cachedHtml = null;
    private static volatile long lastCacheTime = 0;
    private static final long CACHE_TTL_MS = 15000L;

    public static void invalidateCache() {
        cachedHtml = null;
    }

    private final UltimateDonutSmp plugin;

    public SellStatsExporter(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public String generateDashboardHtmlCached() {
        long now = System.currentTimeMillis();
        String current = cachedHtml;
        if (current != null && (now - lastCacheTime < CACHE_TTL_MS)) {
            return current;
        }
        synchronized (SellStatsExporter.class) {
            now = System.currentTimeMillis();
            if (cachedHtml != null && (now - lastCacheTime < CACHE_TTL_MS)) {
                return cachedHtml;
            }
            String fresh = generateDashboardHtml();
            cachedHtml = fresh;
            lastCacheTime = now;
            return fresh;
        }
    }

    public static synchronized void startEmbeddedHttpServer(UltimateDonutSmp plugin) {
        if (activeHttpServer != null) {
            return;
        }

        boolean enabled = plugin.getConfigManager().getShop().getBoolean("SHOP-GUI.WEB-SERVER.ENABLED", true);
        if (!enabled) {
            return;
        }

        int targetPort = plugin.getConfigManager().getShop().getInt("SHOP-GUI.WEB-SERVER.PORT", 8080);
        int mcPort = plugin.getServer().getPort();

        if (targetPort == mcPort) {
            plugin.getLogger().warning("Web Server port (" + targetPort + ") conflicts with Minecraft game port! Switching to dedicated web port (8080/25580).");
            targetPort = (mcPort == 8080) ? 8081 : 8080;
        }

        int[] tryPorts = new int[]{targetPort, 8080, 8081, 25580, 8888};
        for (int p : tryPorts) {
            try {
                activeHttpServer = HttpServer.create(new InetSocketAddress(p), 0);
                activeHttpServer.createContext("/stats", exchange -> {
                    try {
                        String path = exchange.getRequestURI().getPath();
                        if ("/stats/reset".equals(path) || "/stats/reset/".equals(path)) {
                            plugin.getDatabaseManager().clearShopAnalyticsData();
                            invalidateCache();
                            byte[] response = "{\"success\":true}".getBytes(StandardCharsets.UTF_8);
                            exchange.getResponseHeaders().set("Content-Type", "application/json");
                            exchange.sendResponseHeaders(200, response.length);
                            try (OutputStream os = exchange.getResponseBody()) {
                                os.write(response);
                            }
                            return;
                        }

                        String html = new SellStatsExporter(plugin).generateDashboardHtmlCached();
                        byte[] response = html.getBytes(StandardCharsets.UTF_8);
                        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                        exchange.sendResponseHeaders(200, response.length);
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(response);
                        }
                    } catch (Exception e) {
                        plugin.getLogger().log(Level.WARNING, "Error serving stats web page", e);
                    }
                });
                activeHttpServer.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
                activeHttpServer.start();
                actualBoundPort = p;
                plugin.getLogger().info("Shop Analytics Web Server started at http://localhost:" + p + "/stats");
                return;
            } catch (Exception ignored) {}
        }
        plugin.getLogger().warning("Could not bind Shop Analytics Web Server to any open port.");
    }

    public static synchronized void stopEmbeddedHttpServer() {
        if (activeHttpServer != null) {
            try {
                activeHttpServer.stop(0);
            } catch (Exception ignored) {}
            activeHttpServer = null;
        }
    }

    public void exportHtml(CommandSender sender) {
        plugin.getSpigotScheduler().runAsync(() -> {
            try {
                File htmlFile = new File(plugin.getDataFolder(), "sell-stats.html");
                String html = generateDashboardHtmlCached();
                try (PrintWriter writer = new PrintWriter(new FileWriter(htmlFile, StandardCharsets.UTF_8))) {
                    writer.print(html);
                }

                if (sender != null) {
                    sender.sendMessage(ColorUtils.toComponent("&a&l[Sell Stats Site] &fHTML web dashboard generated at: &eplugins/UltimateDonutSMP/sell-stats.html"));
                }
                plugin.getLogger().info("Sell stats HTML dashboard exported to " + htmlFile.getAbsolutePath());
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to generate sell stats HTML report: " + e.getMessage());
                if (sender != null) {
                    sender.sendMessage(ColorUtils.toComponent("&cFailed to generate HTML stats site file. Check console."));
                }
            }
        });
    }

    public void uploadToWebPaste(CommandSender sender) {
        // First make sure HTML file is generated
        exportHtml(null);

        int port = actualBoundPort > 0 ? actualBoundPort : plugin.getConfigManager().getShop().getInt("SHOP-GUI.WEB-SERVER.PORT", 8080);
        String configuredUrl = plugin.getConfigManager().getShop().getString("SHOP-GUI.WEB-SERVER.PUBLIC-URL", "");
        if (configuredUrl != null && (configuredUrl.contains("example.com") || configuredUrl.isBlank())) {
            configuredUrl = "";
        }

        String localWebUrl = (configuredUrl != null && !configuredUrl.isBlank())
                ? configuredUrl
                : "http://localhost:" + port + "/stats";

        if (sender instanceof Player player && player.isOnline()) {
            TextComponent linkMsg = new TextComponent(ColorUtils.toComponent("&a&l[Sell Stats Web] &fOpen live Shop Analytics site: &e&n" + localWebUrl));
            linkMsg.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, localWebUrl));
            linkMsg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ColorUtils.toComponent("&7Click to open &e" + localWebUrl + "\n&7File: plugins/UltimateDonutSMP/sell-stats.html"))));
            player.spigot().sendMessage(linkMsg);
        } else if (sender != null) {
            sender.sendMessage(ColorUtils.toComponent("&a&l[Sell Stats Web] &fLive web site: &e" + localWebUrl + " &7(or open plugins/UltimateDonutSMP/sell-stats.html)"));
        }
    }

    public String generateDashboardHtml() {
        double totalSellRevenue = plugin.getDatabaseManager().getTotalSellRevenue();
        long totalSellVolume = plugin.getDatabaseManager().getTotalItemsSold();
        int salesCount = plugin.getDatabaseManager().countGlobalSellHistory();

        int purchasesCount = plugin.getDatabaseManager().getTotalShopBuyCount();
        double totalPurchaseSpend = plugin.getDatabaseManager().getTotalShopBuySpend();

        int totalTransactions = salesCount + purchasesCount;
        double totalVolumeMoney = totalSellRevenue + totalPurchaseSpend;

        double buyRatio = totalTransactions > 0 ? ((double) purchasesCount / totalTransactions) * 100.0 : 0.0;
        double netFlow = totalSellRevenue - totalPurchaseSpend;
        double avgTransaction = totalTransactions > 0 ? totalVolumeMoney / totalTransactions : 0.0;

        long since24h = System.currentTimeMillis() - 86400000L;
        int activeTraders = plugin.getDatabaseManager().getActiveTradersCount(since24h);

        List<DatabaseManager.TopSoldItemEntry> topRevenue = plugin.getDatabaseManager().getTopSoldItemsByRevenue(20);
        List<DatabaseManager.TopSellerEntry> topSellers = plugin.getDatabaseManager().getTopSellers(10);
        List<DatabaseManager.TopBuyerEntry> topBuyers = plugin.getDatabaseManager().getTopBuyers(10);

        List<DatabaseManager.HourlyActivityEntry> act1H = plugin.getDatabaseManager().getMinuteActivityStats(6);
        List<DatabaseManager.HourlyActivityEntry> act24H = plugin.getDatabaseManager().getHourlyActivityStats(24);
        List<DatabaseManager.HourlyActivityEntry> act7D = plugin.getDatabaseManager().getDailyActivityStats(7);
        List<DatabaseManager.HourlyActivityEntry> actAll = plugin.getDatabaseManager().getHourlyActivityStats(12);

        String[] set1H = buildJsonDatasets(act1H);
        String[] set24H = buildJsonDatasets(act24H);
        String[] set7D = buildJsonDatasets(act7D);
        String[] setAll = buildJsonDatasets(actAll);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateStr = sdf.format(new Date());

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n");
        html.append("<meta charset=\"UTF-8\">\n");
        html.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("<title>Shop Analytics — UltimateDonutSMP</title>\n");
        html.append("<script src=\"https://cdn.jsdelivr.net/npm/chart.js\"></script>\n");
        html.append("<style>\n");
        html.append(":root {\n");
        html.append("  --bg: #0b0f19;\n");
        html.append("  --card-bg: #111827;\n");
        html.append("  --box-bg: #1f2937;\n");
        html.append("  --border: #1e293b;\n");
        html.append("  --primary: #3b82f6;\n");
        html.append("  --accent-blue: #60a5fa;\n");
        html.append("  --green: #10b981;\n");
        html.append("  --red: #ef4444;\n");
        html.append("  --purple: #8b5cf6;\n");
        html.append("  --text: #f9fafb;\n");
        html.append("  --muted: #9ca3af;\n");
        html.append("}\n");
        html.append("* { box-sizing: border-box; margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; }\n");
        html.append("body { background-color: var(--bg); color: var(--text); padding: 2.5rem 1.5rem; line-height: 1.5; min-height: 100vh; }\n");
        html.append(".container { max-width: 1240px; margin: 0 auto; display: flex; flex-direction: column; gap: 1.75rem; }\n");

        // Header styles
        html.append(".page-header { text-align: center; margin-bottom: 0.5rem; }\n");
        html.append(".page-title { font-size: 2.25rem; font-weight: 800; color: #60a5fa; background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%); -webkit-background-clip: text; -webkit-text-fill-color: transparent; letter-spacing: -0.02em; }\n");
        html.append(".page-subtitle { color: var(--muted); font-size: 0.95rem; margin-top: 0.35rem; font-weight: 400; }\n");

        // KPI Top Grid
        html.append(".kpi-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 1.25rem; }\n");
        html.append(".kpi-card { background: var(--card-bg); border: 1px solid var(--border); border-radius: 14px; padding: 1.25rem 1.5rem; display: flex; align-items: center; gap: 1.25rem; box-shadow: 0 4px 20px -2px rgba(0,0,0,0.3); transition: transform 0.2s ease, border-color 0.2s ease; }\n");
        html.append(".kpi-card:hover { transform: translateY(-2px); border-color: #374151; }\n");
        html.append(".kpi-icon { width: 48px; height: 48px; border-radius: 10px; background: var(--box-bg); display: flex; align-items: center; justify-content: center; font-size: 1.4rem; flex-shrink: 0; }\n");
        html.append(".kpi-content { display: flex; flex-direction: column; }\n");
        html.append(".kpi-label { font-size: 0.85rem; color: var(--muted); font-weight: 500; text-transform: uppercase; letter-spacing: 0.04em; }\n");
        html.append(".kpi-value { font-size: 1.75rem; font-weight: 700; color: #fff; margin-top: 0.2rem; }\n");

        // Section Cards
        html.append(".section-card { background: var(--card-bg); border: 1px solid var(--border); border-radius: 14px; padding: 1.5rem; box-shadow: 0 4px 20px -2px rgba(0,0,0,0.3); display: flex; flex-direction: column; gap: 1.25rem; }\n");
        html.append(".section-header { display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid rgba(255,255,255,0.05); padding-bottom: 1rem; }\n");
        html.append(".section-title-group { display: flex; align-items: center; gap: 0.6rem; }\n");
        html.append(".section-title { font-size: 1.15rem; font-weight: 700; color: #fff; letter-spacing: -0.01em; }\n");

        // Economy Health Inner Grid
        html.append(".health-grid { display: grid; grid-template-columns: repeat(6, 1fr); gap: 1rem; }\n");
        html.append(".health-box { background: var(--box-bg); border-radius: 10px; padding: 1rem 0.75rem; text-align: center; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 0.35rem; }\n");
        html.append(".health-label { font-size: 0.75rem; color: var(--muted); text-transform: uppercase; font-weight: 600; letter-spacing: 0.05em; }\n");
        html.append(".health-value { font-size: 1.25rem; font-weight: 700; color: #fff; }\n");
        html.append(".mini-progress { width: 80%; height: 5px; background: rgba(255,255,255,0.1); border-radius: 99px; overflow: hidden; margin-top: 0.25rem; }\n");
        html.append(".mini-progress-fill { height: 100%; background: var(--primary); border-radius: 99px; }\n");

        // Filter Pills
        html.append(".filter-pills { display: flex; gap: 0.4rem; background: var(--box-bg); padding: 0.25rem; border-radius: 8px; }\n");
        html.append(".pill { border: none; background: transparent; color: var(--muted); padding: 0.35rem 0.85rem; font-size: 0.8rem; font-weight: 600; border-radius: 6px; cursor: pointer; transition: all 0.15s ease; }\n");
        html.append(".pill.active { background: var(--purple); color: #fff; shadow: 0 2px 8px rgba(139,92,246,0.4); }\n");

        // Split Grid (Bottom row)
        html.append(".split-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 1.5rem; }\n");

        // Table Styling
        html.append(".data-table { width: 100%; border-collapse: collapse; text-align: left; font-size: 0.9rem; }\n");
        html.append(".data-table th { color: var(--muted); font-size: 0.75rem; text-transform: uppercase; font-weight: 600; padding: 0.75rem 1rem; border-bottom: 1px solid var(--border); }\n");
        html.append(".data-table td { padding: 0.85rem 1rem; border-bottom: 1px solid rgba(255,255,255,0.04); color: #e5e7eb; }\n");
        html.append(".data-table tr:last-child td { border-bottom: none; }\n");
        html.append(".badge { display: inline-block; padding: 0.25rem 0.55rem; border-radius: 6px; font-size: 0.7rem; font-weight: 700; text-transform: uppercase; }\n");
        html.append(".badge-red { background: rgba(239,68,68,0.15); color: #fca5a5; border: 1px solid rgba(239,68,68,0.3); }\n");
        html.append(".badge-yellow { background: rgba(245,158,11,0.15); color: #fde68a; border: 1px solid rgba(245,158,11,0.3); }\n");
        html.append(".badge-green { background: rgba(16,185,129,0.15); color: #6ee7b7; border: 1px solid rgba(16,185,129,0.3); }\n");

        // Chart container
        html.append(".chart-container { position: relative; height: 280px; width: 100%; }\n");

        // Responsive Media Queries
        html.append("@media (max-width: 1024px) {\n");
        html.append("  .kpi-grid { grid-template-columns: repeat(2, 1fr); }\n");
        html.append("  .health-grid { grid-template-columns: repeat(3, 1fr); }\n");
        html.append("  .split-grid { grid-template-columns: 1fr; }\n");
        html.append("}\n");
        html.append("@media (max-width: 640px) {\n");
        html.append("  .kpi-grid { grid-template-columns: 1fr; }\n");
        html.append("  .health-grid { grid-template-columns: repeat(2, 1fr); }\n");
        html.append("}\n");
        html.append("</style>\n</head>\n<body>\n");

        html.append("<div class=\"container\">\n");

        // Page Header
        html.append("<div class=\"page-header\">\n");
        html.append("<h1 class=\"page-title\">Shop Analytics</h1>\n");
        html.append("<div style=\"display:flex; align-items:center; justify-content:center; gap:0.8rem; margin-top:0.35rem; flex-wrap:wrap;\">\n");
        html.append("  <p class=\"page-subtitle\" style=\"margin-top:0;\">Real-time economy monitoring and market insights — Updated ").append(dateStr).append("</p>\n");
        html.append("  <button id=\"autoRefreshBtn\" style=\"background: rgba(255,255,255,0.06); border: 1px solid rgba(255,255,255,0.12); color: #e5e7eb; padding: 4px 12px; border-radius: 20px; font-size: 0.8rem; font-weight: 500; cursor: pointer; display: inline-flex; align-items: center; gap: 0.45rem; transition: all 0.2s ease;\">\n");
        html.append("    <span id=\"refreshDot\" style=\"width: 7px; height: 7px; border-radius: 50%; background: #10b981; display: inline-block;\"></span>\n");
        html.append("    <span id=\"refreshText\">Auto-Refresh (30s)</span>\n");
        html.append("  </button>\n");
        html.append("  <button id=\"resetDataBtn\" style=\"background: rgba(239,68,68,0.12); border: 1px solid rgba(239,68,68,0.3); color: #fca5a5; padding: 4px 12px; border-radius: 20px; font-size: 0.8rem; font-weight: 600; cursor: pointer; display: inline-flex; align-items: center; gap: 0.35rem; transition: all 0.2s ease;\">\n");
        html.append("    🗑️ Reset Data\n");
        html.append("  </button>\n");
        html.append("</div>\n");
        html.append("</div>\n");

        // Top 4 KPI Cards
        html.append("<div class=\"kpi-grid\">\n");

        // Card 1: Total Transactions
        html.append("<div class=\"kpi-card\">\n");
        html.append("<div class=\"kpi-icon\">📦</div>\n");
        html.append("<div class=\"kpi-content\">\n");
        html.append("<span class=\"kpi-label\">Total Transactions</span>\n");
        html.append("<span class=\"kpi-value\">").append(String.format(Locale.US, "%,d", totalTransactions)).append("</span>\n");
        html.append("</div>\n</div>\n");

        // Card 2: Total Volume
        html.append("<div class=\"kpi-card\">\n");
        html.append("<div class=\"kpi-icon\">💰</div>\n");
        html.append("<div class=\"kpi-content\">\n");
        html.append("<span class=\"kpi-label\">Total Volume</span>\n");
        html.append("<span class=\"kpi-value\">$").append(String.format(Locale.US, "%,.2f", totalVolumeMoney)).append("</span>\n");
        html.append("</div>\n</div>\n");

        // Card 3: Purchases
        html.append("<div class=\"kpi-card\">\n");
        html.append("<div class=\"kpi-icon\">📈</div>\n");
        html.append("<div class=\"kpi-content\">\n");
        html.append("<span class=\"kpi-label\">Purchases</span>\n");
        html.append("<span class=\"kpi-value\">").append(String.format(Locale.US, "%,d", purchasesCount)).append("</span>\n");
        html.append("</div>\n</div>\n");

        // Card 4: Sales
        html.append("<div class=\"kpi-card\">\n");
        html.append("<div class=\"kpi-icon\">📉</div>\n");
        html.append("<div class=\"kpi-content\">\n");
        html.append("<span class=\"kpi-label\">Sales</span>\n");
        html.append("<span class=\"kpi-value\">").append(String.format(Locale.US, "%,d", salesCount)).append("</span>\n");
        html.append("</div>\n</div>\n");

        html.append("</div>\n");

        // Economy Health Panel
        html.append("<div class=\"section-card\">\n");
        html.append("<div class=\"section-header\">\n");
        html.append("<div class=\"section-title-group\">\n");
        html.append("<span>💊</span>\n");
        html.append("<h2 class=\"section-title\">Economy Health</h2>\n");
        html.append("</div>\n</div>\n");

        html.append("<div class=\"health-grid\">\n");

        // Health 1: Buy Ratio
        html.append("<div class=\"health-box\">\n");
        html.append("<span class=\"health-label\">Buy Ratio</span>\n");
        html.append("<span class=\"health-value\">").append(String.format(Locale.US, "%.1f%%", buyRatio)).append("</span>\n");
        html.append("<div class=\"mini-progress\"><div class=\"mini-progress-fill\" style=\"width: ").append(Math.min(100, buyRatio)).append("%;\"></div></div>\n");
        html.append("</div>\n");

        // Health 2: Activity
        int avgTxsHr = Math.max(1, totalTransactions / 24);
        html.append("<div class=\"health-box\">\n");
        html.append("<span class=\"health-label\">Activity</span>\n");
        html.append("<span class=\"health-value\">").append(avgTxsHr).append(" txs/hr</span>\n");
        html.append("</div>\n");

        // Health 3: Net Flow
        String netFlowColor = netFlow >= 0 ? "#10b981" : "#ef4444";
        String netFlowPrefix = netFlow >= 0 ? "+$" : "-$";
        html.append("<div class=\"health-box\">\n");
        html.append("<span class=\"health-label\">Net Flow</span>\n");
        html.append("<span class=\"health-value\" style=\"color: ").append(netFlowColor).append(";\">").append(netFlowPrefix).append(String.format(Locale.US, "%,.2f", Math.abs(netFlow))).append("</span>\n");
        html.append("</div>\n");

        // Health 4: Avg Transaction
        html.append("<div class=\"health-box\">\n");
        html.append("<span class=\"health-label\">Avg Transaction</span>\n");
        html.append("<span class=\"health-value\">$").append(String.format(Locale.US, "%.2f", avgTransaction)).append("</span>\n");
        html.append("</div>\n");

        // Health 5: Unique Items
        html.append("<div class=\"health-box\">\n");
        html.append("<span class=\"health-label\">Unique Items</span>\n");
        html.append("<span class=\"health-value\">").append(topRevenue.size()).append("</span>\n");
        html.append("</div>\n");

        // Health 6: Active Traders
        html.append("<div class=\"health-box\">\n");
        html.append("<span class=\"health-label\">Active Traders</span>\n");
        html.append("<span class=\"health-value\">").append(activeTraders).append("</span>\n");
        html.append("</div>\n");

        html.append("</div>\n</div>\n");

        // Transaction Activity Chart Panel
        html.append("<div class=\"section-card\">\n");
        html.append("<div class=\"section-header\">\n");
        html.append("<h2 class=\"section-title\">Transaction Activity</h2>\n");
        html.append("<div class=\"filter-pills\">\n");
        html.append("<button class=\"pill\">1H</button>\n");
        html.append("<button class=\"pill\">24H</button>\n");
        html.append("<button class=\"pill\">7D</button>\n");
        html.append("<button class=\"pill active\">All</button>\n");
        html.append("</div>\n</div>\n");

        html.append("<div class=\"chart-container\">\n");
        html.append("<canvas id=\"activityChart\"></canvas>\n");
        html.append("</div>\n</div>\n");

        // Split Grid: Leaderboards & Market Trends
        html.append("<div class=\"split-grid\">\n");

        // Left Column: Leaderboards
        html.append("<div class=\"section-card\">\n");
        html.append("<div class=\"section-header\">\n");
        html.append("<div class=\"section-title-group\">\n");
        html.append("<span>🏆</span>\n");
        html.append("<h2 class=\"section-title\">Leaderboards (Top Sellers)</h2>\n");
        html.append("</div>\n</div>\n");

        html.append("<table class=\"data-table\">\n");
        html.append("<thead>\n<tr>\n<th>Rank</th>\n<th>Player</th>\n<th>Total Earned</th>\n<th>Units Sold</th>\n</tr>\n</thead>\n<tbody>\n");

        for (int i = 0; i < Math.min(8, topSellers.size()); i++) {
            DatabaseManager.TopSellerEntry entry = topSellers.get(i);
            String primaryHeadUrl = resolvePlayerSkinHeadUrl(entry.playerUuid(), entry.playerName());
            String fallbackHeadUrl = "https://minotar.net/helm/" + entry.playerName() + "/24";

            html.append("<tr>\n");
            html.append("<td><strong>#").append(i + 1).append("</strong></td>\n");
            html.append("<td><div style=\"display:flex; align-items:center; gap:0.6rem;\">");
            html.append("<img src=\"").append(primaryHeadUrl).append("\" alt=\"").append(entry.playerName())
                .append("\" style=\"width:24px; height:24px; border-radius:4px; border:1px solid rgba(255,255,255,0.15); flex-shrink:0;\" ")
                .append("onerror=\"this.onerror=null; this.src='").append(fallbackHeadUrl).append("';\">");
            html.append("<strong>").append(entry.playerName()).append("</strong></div></td>\n");
            html.append("<td style=\"color:#10b981; font-weight:700;\">$").append(String.format(Locale.US, "%,.2f", entry.totalEarned())).append("</td>\n");
            html.append("<td>").append(String.format(Locale.US, "%,d", entry.totalAmountSold())).append("</td>\n");
            html.append("</tr>\n");
        }
        if (topSellers.isEmpty()) {
            html.append("<tr><td colspan=\"4\" style=\"text-align:center; color:var(--muted);\">No sales data recorded yet</td></tr>\n");
        }
        html.append("</tbody>\n</table>\n</div>\n");

        // Right Column: Market Trends
        html.append("<div class=\"section-card\">\n");
        html.append("<div class=\"section-header\">\n");
        html.append("<div class=\"section-title-group\">\n");
        html.append("<span>📊</span>\n");
        html.append("<h2 class=\"section-title\">Market Trends (Top Revenue Items)</h2>\n");
        html.append("</div>\n</div>\n");

        html.append("<table class=\"data-table\">\n");
        html.append("<thead>\n<tr>\n<th>Item</th>\n<th>Total Revenue</th>\n<th>Units Sold</th>\n<th>Share</th>\n<th>Status</th>\n</tr>\n</thead>\n<tbody>\n");

        for (int i = 0; i < Math.min(8, topRevenue.size()); i++) {
            DatabaseManager.TopSoldItemEntry entry = topRevenue.get(i);
            double pct = totalSellRevenue > 0 ? (entry.totalRevenue() / totalSellRevenue) * 100.0 : 0.0;

            String itemTextureUrl = resolveMinecraftItemTextureUrl(entry.itemName());
            String blockTextureUrl = "https://raw.githubusercontent.com/InventivetalentDev/minecraft-assets/1.20.4/assets/minecraft/textures/block/" + entry.itemName().toLowerCase(Locale.US) + ".png";

            String badgeHtml;
            String barColor;
            if (pct >= 10.0) {
                badgeHtml = "<span class=\"badge badge-red\">Nerf Target</span>";
                barColor = "#ef4444";
            } else if (pct >= 5.0) {
                badgeHtml = "<span class=\"badge badge-yellow\">Medium</span>";
                barColor = "#f59e0b";
            } else {
                badgeHtml = "<span class=\"badge badge-green\">Balanced</span>";
                barColor = "#3b82f6";
            }

            html.append("<tr>\n");
            html.append("<td><div style=\"display:flex; align-items:center; gap:0.6rem;\">");
            html.append("<img src=\"").append(itemTextureUrl).append("\" alt=\"").append(entry.itemName())
                .append("\" style=\"width:22px; height:22px; object-fit:contain; flex-shrink:0;\" ")
                .append("onerror=\"this.onerror=null; this.src='").append(blockTextureUrl).append("'; this.onerror=function(){this.style.display='none';};\">");
            html.append("<strong>").append(prettify(entry.itemName())).append("</strong></div></td>\n");
            html.append("<td style=\"color:#10b981; font-weight:700;\">$").append(String.format(Locale.US, "%,.2f", entry.totalRevenue())).append("</td>\n");
            html.append("<td>").append(NumberUtils.format(entry.totalAmount())).append("</td>\n");
            html.append("<td>").append(String.format(Locale.US, "%.1f%%", pct));
            html.append("<div class=\"mini-progress\" style=\"width:100%; margin-top:4px;\"><div class=\"mini-progress-fill\" style=\"width: ").append(Math.min(100, Math.max(4, pct))).append("%; background: ").append(barColor).append(";\"></div></div>");
            html.append("</td>\n");
            html.append("<td>").append(badgeHtml).append("</td>\n");
            html.append("</tr>\n");
        }
        if (topRevenue.isEmpty()) {
            html.append("<tr><td colspan=\"5\" style=\"text-align:center; color:var(--muted);\">No market items recorded yet</td></tr>\n");
        }
        html.append("</tbody>\n</table>\n</div>\n");

        html.append("</div>\n"); // End split grid

        html.append("</div>\n"); // End container

        // Reset Confirmation Modal HTML
        html.append("<div id=\"resetModal\" style=\"display:none; position:fixed; top:0; left:0; width:100%; height:100%; background:rgba(0,0,0,0.75); backdrop-filter:blur(5px); z-index:9999; align-items:center; justify-content:center;\">\n");
        html.append("  <div style=\"background:#1e293b; border:1px solid rgba(239,68,68,0.4); border-radius:14px; padding:1.75rem; max-width:420px; width:90%; text-align:center; box-shadow:0 20px 25px -5px rgba(0,0,0,0.6);\">\n");
        html.append("    <div style=\"font-size:2.2rem; margin-bottom:0.4rem;\">⚠️</div>\n");
        html.append("    <h3 style=\"color:#ef4444; font-size:1.2rem; font-weight:700; margin-bottom:0.5rem;\">Reset Shop Analytics?</h3>\n");
        html.append("    <p style=\"color:#9ca3af; font-size:0.85rem; line-height:1.4; margin-bottom:1.5rem;\">This will permanently clear all shop purchases, sales history, and leaderboard statistics. This action cannot be undone.</p>\n");
        html.append("    <div style=\"display:flex; gap:0.75rem; justify-content:center;\">\n");
        html.append("      <button id=\"cancelResetModal\" style=\"background:rgba(255,255,255,0.08); border:1px solid rgba(255,255,255,0.15); color:#e5e7eb; padding:8px 16px; border-radius:8px; font-weight:600; cursor:pointer;\">Cancel</button>\n");
        html.append("      <button id=\"confirmResetModal\" style=\"background:#ef4444; border:none; color:#fff; padding:8px 18px; border-radius:8px; font-weight:700; cursor:pointer;\">Yes, Reset All</button>\n");
        html.append("    </div>\n");
        html.append("  </div>\n");
        html.append("</div>\n");

        // Single Consolidated JavaScript Script
        html.append("<script>\n");
        html.append("const timeframes = {\n");
        html.append("  '1H': { labels: ").append(set1H[0]).append(", sales: ").append(set1H[1]).append(", purchases: ").append(set1H[2]).append(" },\n");
        html.append("  '24H': { labels: ").append(set24H[0]).append(", sales: ").append(set24H[1]).append(", purchases: ").append(set24H[2]).append(" },\n");
        html.append("  '7D': { labels: ").append(set7D[0]).append(", sales: ").append(set7D[1]).append(", purchases: ").append(set7D[2]).append(" },\n");
        html.append("  'All': { labels: ").append(setAll[0]).append(", sales: ").append(setAll[1]).append(", purchases: ").append(setAll[2]).append(" }\n");
        html.append("};\n");

        html.append("const ctx = document.getElementById('activityChart').getContext('2d');\n");
        html.append("const initial = timeframes['All'];\n");
        html.append("const chart = new Chart(ctx, {\n");
        html.append("  type: 'bar',\n");
        html.append("  data: {\n");
        html.append("    labels: initial.labels,\n");
        html.append("    datasets: [\n");
        html.append("      { label: 'Purchases', data: initial.purchases, backgroundColor: '#10b981', borderRadius: 4 },\n");
        html.append("      { label: 'Sales', data: initial.sales, backgroundColor: '#ef4444', borderRadius: 4 }\n");
        html.append("    ]\n");
        html.append("  },\n");
        html.append("  options: {\n");
        html.append("    responsive: true,\n");
        html.append("    maintainAspectRatio: false,\n");
        html.append("    scales: {\n");
        html.append("      x: { stacked: true, grid: { color: 'rgba(255,255,255,0.05)' }, ticks: { color: '#9ca3af' } },\n");
        html.append("      y: { stacked: true, grid: { color: 'rgba(255,255,255,0.05)' }, ticks: { color: '#9ca3af' } }\n");
        html.append("    },\n");
        html.append("    plugins: {\n");
        html.append("      legend: { labels: { color: '#e5e7eb' } }\n");
        html.append("    }\n");
        html.append("  }\n");
        html.append("});\n");

        html.append("document.querySelectorAll('.filter-pills .pill').forEach(button => {\n");
        html.append("  button.addEventListener('click', () => {\n");
        html.append("    document.querySelectorAll('.filter-pills .pill').forEach(b => b.classList.remove('active'));\n");
        html.append("    button.classList.add('active');\n");
        html.append("    const key = button.innerText.trim();\n");
        html.append("    const target = timeframes[key] || timeframes['All'];\n");
        html.append("    chart.data.labels = target.labels;\n");
        html.append("    chart.data.datasets[0].data = target.purchases;\n");
        html.append("    chart.data.datasets[1].data = target.sales;\n");
        html.append("    chart.update();\n");
        html.append("  });\n");
        html.append("});\n");

        html.append("let autoRefresh = true;\n");
        html.append("let timer = 30;\n");
        html.append("const rText = document.getElementById('refreshText');\n");
        html.append("const rDot = document.getElementById('refreshDot');\n");
        html.append("const rBtn = document.getElementById('autoRefreshBtn');\n");

        html.append("if (rBtn) {\n");
        html.append("  rBtn.addEventListener('click', () => {\n");
        html.append("    autoRefresh = !autoRefresh;\n");
        html.append("    if (autoRefresh) {\n");
        html.append("      rDot.style.background = '#10b981';\n");
        html.append("      rText.innerText = 'Auto-Refresh (' + timer + 's)';\n");
        html.append("      rBtn.style.borderColor = 'rgba(255,255,255,0.12)';\n");
        html.append("    } else {\n");
        html.append("      rDot.style.background = '#ef4444';\n");
        html.append("      rText.innerText = 'Auto-Refresh (OFF)';\n");
        html.append("      rBtn.style.borderColor = 'rgba(239,68,68,0.3)';\n");
        html.append("    }\n");
        html.append("  });\n");

        html.append("  setInterval(() => {\n");
        html.append("    if (!autoRefresh) return;\n");
        html.append("    timer--;\n");
        html.append("    if (timer <= 0) {\n");
        html.append("      window.location.reload();\n");
        html.append("    } else {\n");
        html.append("      rText.innerText = 'Auto-Refresh (' + timer + 's)';\n");
        html.append("    }\n");
        html.append("  }, 1000);\n");
        html.append("}\n");

        html.append("const resetBtn = document.getElementById('resetDataBtn');\n");
        html.append("const resetModal = document.getElementById('resetModal');\n");
        html.append("const cancelModal = document.getElementById('cancelResetModal');\n");
        html.append("const confirmModal = document.getElementById('confirmResetModal');\n");
        html.append("if (resetBtn && resetModal) {\n");
        html.append("  resetBtn.addEventListener('click', () => { resetModal.style.display = 'flex'; });\n");
        html.append("  cancelModal.addEventListener('click', () => { resetModal.style.display = 'none'; });\n");
        html.append("  confirmModal.addEventListener('click', () => {\n");
        html.append("    confirmModal.innerText = 'Resetting...';\n");
        html.append("    fetch('/stats/reset', { method: 'POST' }).then(() => { window.location.reload(); });\n");
        html.append("  });\n");
        html.append("}\n");
        html.append("</script>\n");

        html.append("</body>\n</html>");

        return html.toString();
    }

    private String[] buildJsonDatasets(List<DatabaseManager.HourlyActivityEntry> entries) {
        StringBuilder labels = new StringBuilder("[");
        StringBuilder sales = new StringBuilder("[");
        StringBuilder purchases = new StringBuilder("[");

        for (int i = 0; i < entries.size(); i++) {
            DatabaseManager.HourlyActivityEntry entry = entries.get(i);
            if (i > 0) {
                labels.append(",");
                sales.append(",");
                purchases.append(",");
            }
            labels.append("\"").append(entry.hourLabel()).append("\"");
            sales.append(entry.salesCount());
            purchases.append(entry.purchaseCount());
        }
        labels.append("]");
        sales.append("]");
        purchases.append("]");

        return new String[]{ labels.toString(), sales.toString(), purchases.toString() };
    }

    private String resolvePlayerSkinHeadUrl(java.util.UUID uuid, String playerName) {
        String textureHash = null;

        // 1. Try SkinsRestorer API to get actual skin property Base64 value
        if (plugin.getServer().getPluginManager().isPluginEnabled("SkinsRestorer")) {
            try {
                TablistManager.SkinTexture texture = SkinsRestorerSkinLookup.resolve(uuid, playerName);
                if (texture != null && texture.value() != null && !texture.value().isBlank()) {
                    textureHash = parseTextureHashFromBase64(texture.value());
                }
            } catch (Throwable ignored) {}
        }

        // 2. Try online player profile if online
        if (textureHash == null && uuid != null) {
            Player onlinePlayer = plugin.getServer().getPlayer(uuid);
            if (onlinePlayer != null) {
                try {
                    Object profile = onlinePlayer.getClass().getMethod("getPlayerProfile").invoke(onlinePlayer);
                    if (profile != null) {
                        java.util.Collection<?> properties = (java.util.Collection<?>) profile.getClass().getMethod("getProperties").invoke(profile);
                        for (Object prop : properties) {
                            String name = (String) prop.getClass().getMethod("getName").invoke(prop);
                            if ("textures".equals(name)) {
                                String val = (String) prop.getClass().getMethod("getValue").invoke(prop);
                                textureHash = parseTextureHashFromBase64(val);
                                break;
                            }
                        }
                    }
                } catch (Throwable ignored) {}
            }
        }

        // 3. If texture hash resolved, mc-heads renders exact texture head directly
        if (textureHash != null && !textureHash.isBlank()) {
            return "https://mc-heads.net/avatar/" + textureHash + "/24";
        }

        // Fallback to name-based lookup
        return "https://mc-heads.net/avatar/" + (playerName != null ? playerName : "Steve") + "/24";
    }

    private String parseTextureHashFromBase64(String base64) {
        try {
            byte[] decoded = java.util.Base64.getDecoder().decode(base64);
            String json = new String(decoded, StandardCharsets.UTF_8);
            int idx = json.indexOf("/texture/");
            if (idx != -1) {
                int start = idx + "/texture/".length();
                int end = json.indexOf("\"", start);
                if (end != -1) {
                    return json.substring(start, end);
                }
            }
        } catch (Throwable ignored) {}
        return null;
    }

    private String resolveMinecraftItemTextureUrl(String rawMaterial) {
        if (rawMaterial == null || rawMaterial.isBlank()) {
            return "https://raw.githubusercontent.com/InventivetalentDev/minecraft-assets/1.20.4/assets/minecraft/textures/item/chest.png";
        }
        String name = rawMaterial.toLowerCase(Locale.US);
        return "https://raw.githubusercontent.com/InventivetalentDev/minecraft-assets/1.20.4/assets/minecraft/textures/item/" + name + ".png";
    }

    private String prettify(String raw) {
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
