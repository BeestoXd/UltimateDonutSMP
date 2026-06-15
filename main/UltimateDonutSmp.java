package com.bx.ultimateDonutSmp;

import com.bx.ultimateDonutSmp.amethyst.*;
import com.bx.ultimateDonutSmp.api.EconomyExpansion;
import com.bx.ultimateDonutSmp.api.HideExpansion;
import com.bx.ultimateDonutSmp.commands.*;
import com.bx.ultimateDonutSmp.hooks.VaultEconomyHook;
import com.bx.ultimateDonutSmp.listeners.*;
import com.bx.ultimateDonutSmp.managers.*;
import com.bx.ultimateDonutSmp.tasks.*;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.SpigotScheduler;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public final class UltimateDonutSmp extends JavaPlugin {

    private static UltimateDonutSmp instance;
    private SpigotScheduler SpigotScheduler;

    // ── Managers ──────────────────────────────────────────────────────────────
    private ConfigManager configManager;
    private LanguageManager languageManager;
    private CurrencyManager currencyManager;
    private FeatureManager featureManager;
    private DatabaseManager databaseManager;
    private PlayerDataManager playerDataManager;
    private EconomyManager economyManager;
    private ChatManager chatManager;
    private IgnoreManager ignoreManager;
    private PrivateMessageManager privateMessageManager;
    private TeamManager teamManager;
    private HomeManager homeManager;
    private BountyManager bountyManager;
    private WarpManager warpManager;
    private CuboidManager cuboidManager;
    private SpawnManager spawnManager;
    private CombatManager combatManager;
    private FastCrystalManager fastCrystalManager;
    private TPAManager tpaManager;
    private ShardManager shardManager;
    private ClearLagManager clearLagManager;
    private CrateManager crateManager;
    private CrateVisualManager crateVisualManager;
    private KeyAllManager keyAllManager;
    private AFKManager afkManager;
    private HoverStatsManager hoverStatsManager;
    private WorthManager worthManager;
    private ShopManager shopManager;
    private OrdersManager ordersManager;
    private DuelManager duelManager;
    private FfaManager ffaManager;
    private AuctionHouseManager auctionHouseManager;
    private BillfordManager billfordManager;
    private LeaderboardManager leaderboardManager;
    private ScoreboardManager scoreboardManager;
    private TablistManager tablistManager;
    private TeleportManager teleportManager;
    private RTPManager rtpManager;
    private RTPZoneManager rtpZoneManager;
    private PortalManager portalManager;
    private AmethystToolsManager amethystToolsManager;
    private EnderChestManager enderChestManager;
    private FreezeManager freezeManager;
    private GodModeManager godModeManager;
    private InvseeManager invseeManager;
    private ProfileViewerManager profileViewerManager;
    private PunishmentManager punishmentManager;
    private StatsWipeManager statsWipeManager;
    private ServerWipeManager serverWipeManager;
    private SpawnerManager spawnerManager;
    private AntiEspManager antiEspManager;
    private SpawnStashManager spawnStashManager;
    private FakePlayerManager fakePlayerManager;
    private HideManager hideManager;
    private NetworkStatusManager networkStatusManager;
    private RedisManager redisManager;
    private MaintenanceManager maintenanceManager;
    private NetworkStaffChatManager networkStaffChatManager;
    private NetworkStaffAlertManager networkStaffAlertManager;
    private StaffModeManager staffModeManager;
    private DiscordWebhookManager discordWebhookManager;
    private LunarRichPresenceManager lunarRichPresenceManager;
    private LuckPermsTablistRefreshBridge luckPermsTablistRefreshBridge;
    private SkinsRestorerTablistRefreshBridge skinsRestorerTablistRefreshBridge;
    private OptimizationManager optimizationManager;
    private CrashProtectionManager crashProtectionManager;

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    public void onEnable() {
        instance = this;

        printStartupBanner();

        if (!requirePlaceholderApi()) {
            return;
        }

        SpigotScheduler = new SpigotScheduler(this);

        // 1. Config & database (no dependencies)
        ColorUtils.init();

        configManager = new ConfigManager(this);
        configManager.loadAll();
        languageManager = new LanguageManager(this);
        languageManager.load();
        crashProtectionManager = new CrashProtectionManager(this);
        currencyManager = new CurrencyManager(this);
        featureManager = new FeatureManager(this);
        optimizationManager = new OptimizationManager(this);
        optimizationManager.start();

        databaseManager = new DatabaseManager(this);
        databaseManager.initialize();
        serverWipeManager = new ServerWipeManager(this);
        serverWipeManager.recoverOrRecreatePendingWorlds();

        // 2. Data managers (depend on DB / config)
        playerDataManager = new PlayerDataManager(this);
        economyManager = new EconomyManager(this);
        chatManager = new ChatManager(this);
        ignoreManager = new IgnoreManager(this);
        privateMessageManager = new PrivateMessageManager(this);
        teamManager = new TeamManager(this);
        teamManager.loadAll();
        homeManager = new HomeManager(this);
        bountyManager = new BountyManager(this);
        bountyManager.loadAll();
        warpManager = new WarpManager(this);
        warpManager.loadAll();
        cuboidManager = new CuboidManager(this);
        cuboidManager.loadAll();

        spawnManager = new SpawnManager(this);
        spawnManager.load();

        // 3. Gameplay managers
        combatManager = new CombatManager(this);
        fastCrystalManager = new FastCrystalManager(this);
        tpaManager = new TPAManager(this);
        shardManager = new ShardManager(this);
        amethystToolsManager = new AmethystToolsManager(this);
        clearLagManager = new ClearLagManager(this);
        crateManager = new CrateManager(this);
        crateVisualManager = new CrateVisualManager(this);
        keyAllManager = new KeyAllManager(this);
        afkManager = new AFKManager(this);
        hoverStatsManager = new HoverStatsManager(this);
        worthManager = new WorthManager(this);
        shopManager = new ShopManager(this);
        ordersManager = new OrdersManager(this);
        duelManager = new DuelManager(this);
        ffaManager = new FfaManager(this);
        auctionHouseManager = new AuctionHouseManager(this);
        billfordManager = new BillfordManager(this);
        billfordManager.load();
        leaderboardManager = new LeaderboardManager(this);
        enderChestManager = new EnderChestManager(this);
        freezeManager = new FreezeManager(this);
        godModeManager = new GodModeManager();
        staffModeManager = new StaffModeManager(this);
        invseeManager = new InvseeManager(this);
        profileViewerManager = new ProfileViewerManager(this);
        punishmentManager = new PunishmentManager(this);
        statsWipeManager = new StatsWipeManager(this);
        spawnerManager = new SpawnerManager(this);
        antiEspManager = new AntiEspManager(this);
        spawnStashManager = new SpawnStashManager(this);
        fakePlayerManager = new FakePlayerManager(this);
        redisManager = new RedisManager(this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        networkStatusManager = new NetworkStatusManager(this);
        networkStaffChatManager = new NetworkStaffChatManager(this);
        networkStaffAlertManager = new NetworkStaffAlertManager(this);
        maintenanceManager = new MaintenanceManager(this);
        maintenanceManager.initializeRedisListener();
        duelManager.initializeCrossServer();
        discordWebhookManager = new DiscordWebhookManager(this);
        initializeLunarRichPresenceManager();

        // 4. Display managers
        scoreboardManager = new ScoreboardManager(this);
        tablistManager = new TablistManager(this);
        hideManager = new HideManager(this);
        hideManager.loadAll();
        teleportManager = new TeleportManager(this);
        rtpManager = new RTPManager(this);
        rtpZoneManager = new RTPZoneManager(this);
        portalManager = new PortalManager(this);
        portalManager.loadAll();
        initializeLuckPermsTablistRefreshBridge();
        initializeSkinsRestorerTablistRefreshBridge();

        // 5. Listeners
        registerListeners();

        // 6. Commands
        registerCommands();

        // 6.5 Optional integrations
        registerVaultEconomyProvider();

        // 7. Background tasks
        ScoreboardTask.start(this);
        TablistTask.start(this);
        ShardTask.start(this); // passive "everywhere" shards (per minute)
        ShardCuboidTask.start(this); // spawn cuboid countdown + reward (per second)
        RTPZoneTask.start(this);
        ClearLagTask.start(this);
        KeyAllTask.start(this);
        AutoSaveTask.start(this);
        AFKCheckTask.start(this);
        LunarTeammatesTask.start(this);
        BillfordTask.start(this); // Billford trade rotation check (every 30 s)
        OrdersExpiryTask.start(this);
        AuctionHouseExpiryTask.start(this);
        AmethystToolsTask.start(this);
        SpawnerGenerationTask.start(this);
        DuelMatchTask.start(this);
        FfaMatchTask.start(this);

        // 8. PlaceholderAPI expansion
        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new EconomyExpansion(this).register();
            new HideExpansion(this).register();
            getLogger().info("PlaceholderAPI expansion registered.");
        }

        if (maintenanceManager != null && !maintenanceManager.isMaintenanceActive()) {
            maintenanceManager.broadcastOnline();
        }

        getLogger().info("UltimateDonutSmp enabled successfully.");
    }

    @Override
    public void onDisable() {
        boolean suppressWipeSaves = serverWipeManager != null
                && serverWipeManager.shouldSuppressShutdownSaves();
        if (teleportManager != null) {
            teleportManager.restoreAllRtpChunkThrottles();
        }
        if (worthManager != null) {
            getServer().getOnlinePlayers().forEach(worthManager::clearWorthDisplay);
        }

        if (enderChestManager != null && !suppressWipeSaves) {
            enderChestManager.shutdown();
        }
        if (freezeManager != null) {
            freezeManager.shutdown();
        }
        if (staffModeManager != null) {
            staffModeManager.shutdown();
        }
        if (godModeManager != null) {
            godModeManager.clearAll();
        }
        if (invseeManager != null) {
            invseeManager.shutdown();
        }
        if (antiEspManager != null) {
            antiEspManager.shutdown();
        }
        if (spawnStashManager != null) {
            spawnStashManager.shutdown();
        }
        if (fakePlayerManager != null) {
            fakePlayerManager.shutdown();
        }
        if (hideManager != null) {
            hideManager.shutdown();
        }
        if (networkStatusManager != null) {
            networkStatusManager.shutdown();
        }
        if (networkStaffChatManager != null) {
            networkStaffChatManager.shutdown();
        }
        if (networkStaffAlertManager != null) {
            networkStaffAlertManager.shutdown();
        }
        if (luckPermsTablistRefreshBridge != null) {
            luckPermsTablistRefreshBridge.shutdown();
        }
        if (skinsRestorerTablistRefreshBridge != null) {
            skinsRestorerTablistRefreshBridge.shutdown();
        }
        if (lunarRichPresenceManager != null) {
            lunarRichPresenceManager.shutdown();
        }
        if (optimizationManager != null) {
            optimizationManager.shutdown();
        }
        if (redisManager != null) {
            redisManager.shutdown();
        }
        if (duelManager != null) {
            duelManager.shutdown();
        }
        if (ffaManager != null) {
            ffaManager.shutdown();
        }
        if (spawnerManager != null && !suppressWipeSaves) {
            spawnerManager.shutdown();
        }
        if (crateManager != null) {
            crateManager.clearAllSessions();
        }
        if (crateVisualManager != null) {
            crateVisualManager.shutdown();
        }
        if (privateMessageManager != null) {
            privateMessageManager.clear();
        }
        if (ignoreManager != null) {
            ignoreManager.clear();
        }
        if (portalManager != null) {
            portalManager.shutdown();
        }

        // Save all online players and close DB
        if (playerDataManager != null && !suppressWipeSaves) {
            playerDataManager.saveAll();
        }
        if (databaseManager != null) {
            databaseManager.close();
        }
        getServer().getServicesManager().unregisterAll(this);
        getLogger().info("UltimateDonutSmp disabled.");
    }

    // ── Startup Banner ─────────────────────────────────────────────────────────

    private void printStartupBanner() {
        var console = getServer().getConsoleSender();
        String v = getDescription().getVersion();

        console.sendMessage("");
        console.sendMessage("§6§l  ██╗   ██╗██╗  ████████╗██╗███╗   ███╗ █████╗ ████████╗███████╗");
        console.sendMessage("§6§l  ██║   ██║██║  ╚══██╔══╝██║████╗ ████║██╔══██╗╚══██╔══╝██╔════╝");
        console.sendMessage("§6§l  ██║   ██║██║     ██║   ██║██╔████╔██║███████║   ██║   █████╗  ");
        console.sendMessage("§6§l  ██║   ██║██║     ██║   ██║██║╚██╔╝██║██╔══██║   ██║   ██╔══╝  ");
        console.sendMessage("§6§l  ╚██████╔╝███████╗██║   ██║██║ ╚═╝ ██║██║  ██║   ██║   ███████╗");
        console.sendMessage("§6§l   ╚═════╝ ╚══════╝╚═╝   ╚═╝╚═╝     ╚═╝╚═╝  ╚═╝   ╚═╝   ╚══════╝");
        console.sendMessage("");
        console.sendMessage("§e§l  ██████╗  ██████╗ ███╗   ██╗██╗   ██╗████████╗    ███████╗███╗   ███╗██████╗ ");
        console.sendMessage("§e§l  ██╔══██╗██╔═══██╗████╗  ██║██║   ██║╚══██╔══╝    ██╔════╝████╗ ████║██╔══██╗");
        console.sendMessage("§e§l  ██║  ██║██║   ██║██╔██╗ ██║██║   ██║   ██║       ███████╗██╔████╔██║██████╔╝");
        console.sendMessage("§e§l  ██║  ██║██║   ██║██║╚██╗██║██║   ██║   ██║       ╚════██║██║╚██╔╝██║██╔═══╝ ");
        console.sendMessage("§e§l  ██████╔╝╚██████╔╝██║ ╚████║╚██████╔╝   ██║       ███████║██║ ╚═╝ ██║██║     ");
        console.sendMessage("§e§l  ╚═════╝  ╚═════╝ ╚═╝  ╚═══╝ ╚═════╝    ╚═╝       ╚══════╝╚═╝     ╚═╝╚═╝     ");
        console.sendMessage("");
        console.sendMessage("§8  ═══════════════════════════════════════════════════════════════════════════════");
        console.sendMessage("§7                       §fMade by §b§lBeestoXd §8| §fVersion §a§l" + v);
        console.sendMessage("§7                   §fDiscord: §9§nhttps://dsc.gg/hellstarr");
        console.sendMessage("§8  ═══════════════════════════════════════════════════════════════════════════════");
        console.sendMessage("");
        console.sendMessage("§8  ╔═══════════════════════════════════════════════════════════════════════════╗");
        console.sendMessage("§8  ║                                                                         ║");
        console.sendMessage("§8  ║  §e§l⚠ §6§lNOTE                                                              §8║");
        console.sendMessage("§8  ║                                                                         ║");
        console.sendMessage("§8  ║  §fGuys, please donate to this project or this plugin if you really      §8║");
        console.sendMessage("§8  ║  §flike this plugin, for the donation link just DM me on Discord,        §8║");
        console.sendMessage("§8  ║  §fand for those who have donated to me, may God bless you and may       §8║");
        console.sendMessage("§8  ║  §fyou always be healthy and I am very grateful for the donations        §8║");
        console.sendMessage("§8  ║  §fthat have been given to me §e:)                                       §8║");
        console.sendMessage("§8  ║                                                                         ║");
        console.sendMessage("§8  ╚═══════════════════════════════════════════════════════════════════════════╝");
        console.sendMessage("");
    }

    // ── Registration helpers ──────────────────────────────────────────────────

    private boolean requirePlaceholderApi() {
        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            return true;
        }

        getLogger().severe("==================================================");
        getLogger().severe("UltimateDonutSmp requires PlaceholderAPI to be installed and enabled.");
        getLogger().severe("Scoreboard/sidebar placeholders will not resolve without PlaceholderAPI.");
        getLogger().severe("Install PlaceholderAPI, then restart the server.");
        getLogger().severe("UltimateDonutSmp is disabling itself to prevent broken sidebar output.");
        getLogger().severe("==================================================");
        getServer().getPluginManager().disablePlugin(this);
        return false;
    }

    private void registerListeners() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerJoinQuitListener(this), this);
        pm.registerEvents(new PlayerDeathListener(this), this);
        pm.registerEvents(new ChatListener(this), this);
        pm.registerEvents(new CombatListener(this), this);
        pm.registerEvents(new GodModeListener(this), this);
        pm.registerEvents(new FastCrystalListener(this), this);
        pm.registerEvents(new PlayerRespawnListener(this), this);
        pm.registerEvents(new PlayerMoveListener(this), this);
        pm.registerEvents(new PortalListener(this), this);
        pm.registerEvents(new CuboidWandListener(this), this);
        pm.registerEvents(new InventoryClickListener(this), this);
        pm.registerEvents(new CrateChestListener(this), this);
        pm.registerEvents(new EnderChestListener(this), this);
        pm.registerEvents(new FreezeListener(this), this);
        pm.registerEvents(new StaffModeListener(this), this);
        pm.registerEvents(new InvseeListener(this), this);
        pm.registerEvents(new PlayerStatsListener(this), this);
        pm.registerEvents(new PhantomListener(this), this);
        pm.registerEvents(new MobSpawnListener(this), this);
        pm.registerEvents(new PlayerSettingEffectsListener(this), this);
        pm.registerEvents(new WorthDisplayListener(this), this);
        pm.registerEvents(new DuelListener(this), this);
        pm.registerEvents(new FfaListener(this), this);
        pm.registerEvents(new AmethystToolsListener(this), this);
        pm.registerEvents(new SpawnerBlockListener(this), this);
        pm.registerEvents(new SpawnerInteractListener(this), this);
        pm.registerEvents(new SpawnerVisibilityListener(this), this);
        pm.registerEvents(new SpawnStashListener(this), this);
        pm.registerEvents(new PunishmentCommandAliasListener(this), this);
    }

    private void registerCommands() {
        UniversalCommandTabCompleter universalCommandTabCompleter = new UniversalCommandTabCompleter(this);
        registerFallbackTabCompleters(universalCommandTabCompleter);

        // Team
        TeamCommand teamCmd = new TeamCommand(this);
        setExecutor("team", teamCmd, FeatureManager.Feature.TEAMS);
        ChatCommand chatCommand = new ChatCommand(this);
        ChatTabCompleter chatTabCompleter = new ChatTabCompleter();
        setExecutor("chat", chatCommand, FeatureManager.Feature.CHAT);
        setTabCompleter("chat", chatTabCompleter);
        IgnoreCommand ignoreCommand = new IgnoreCommand(this);
        IgnoreTabCompleter ignoreTabCompleter = new IgnoreTabCompleter(this);
        setExecutor("ignore", ignoreCommand, FeatureManager.Feature.IGNORE);
        setTabCompleter("ignore", ignoreTabCompleter);
        setExecutor("unignore", ignoreCommand, FeatureManager.Feature.IGNORE);
        setTabCompleter("unignore", ignoreTabCompleter);
        MessageCommand messageCommand = new MessageCommand(this);
        MessageTabCompleter messageTabCompleter = new MessageTabCompleter(this);
        setExecutor("msg", messageCommand, FeatureManager.Feature.MESSAGING);
        setTabCompleter("msg", messageTabCompleter);
        setExecutor("reply", messageCommand, FeatureManager.Feature.MESSAGING);
        setTabCompleter("reply", messageTabCompleter);
        setExecutor("pm", new PrivateMessageToggleCommand(this), FeatureManager.Feature.MESSAGING);

        // Homes
        HomeCommand homeCmd = new HomeCommand(this);
        setExecutor("home", homeCmd, FeatureManager.Feature.HOMES);
        setExecutor("homes", homeCmd, FeatureManager.Feature.HOMES);
        setExecutor("sethome", homeCmd, FeatureManager.Feature.HOMES);
        setExecutor("delhome", homeCmd, FeatureManager.Feature.HOMES);
        setExecutor("renamehome", homeCmd, FeatureManager.Feature.HOMES);

        // Spawn / AFK
        setExecutor("spawn", new SpawnCommand(this), FeatureManager.Feature.SPAWN);
        setExecutor("afk", new AFKCommand(this), FeatureManager.Feature.AFK);

        // Teleport
        TPACommand tpaCmd = new TPACommand(this);
        setExecutor("tpa", tpaCmd, FeatureManager.Feature.TPA);
        setExecutor("tpahere", tpaCmd, FeatureManager.Feature.TPA);
        setExecutor("tpaccept", tpaCmd, FeatureManager.Feature.TPA);
        setExecutor("tpadeny", tpaCmd, FeatureManager.Feature.TPA);
        setExecutor("tpacancel", tpaCmd, FeatureManager.Feature.TPA);
        setExecutor("tpauto", new TPAutoCommand(this), FeatureManager.Feature.TPA, FeatureManager.Feature.TPA_AUTO);
        setExecutor("tpahereauto", new TPAHereAutoCommand(this), FeatureManager.Feature.TPA,
                FeatureManager.Feature.TPA_AUTO);

        // Economy
        BalanceCommand balCmd = new BalanceCommand(this);
        setExecutor("balance", balCmd);
        setExecutor("pay", new PayCommand(this));
        setExecutor("addmoney", new AddMoneyCommand(this));
        setExecutor("removemoney", new RemoveMoneyCommand(this));
        setExecutor("setmoney", new SetMoneyCommand(this));

        ShardsCommand shardsCmd = new ShardsCommand(this);
        setExecutor("shards", shardsCmd, FeatureManager.Feature.SHARDS);
        setExecutor("shardpay", new ShardPayCommand(this), FeatureManager.Feature.SHARDS);
        ShardAdminCommand shardAdminCommand = new ShardAdminCommand(this);
        setExecutor("addshards", shardAdminCommand, FeatureManager.Feature.SHARDS);
        setExecutor("removeshards", shardAdminCommand, FeatureManager.Feature.SHARDS);
        setExecutor("setshards", shardAdminCommand, FeatureManager.Feature.SHARDS);
        CrateCommand crateCmd = new CrateCommand(this);
        setExecutor("crate", crateCmd, FeatureManager.Feature.CRATES);
        setExecutor("crates", crateCmd, FeatureManager.Feature.CRATES);
        setExecutor("keys", crateCmd, FeatureManager.Feature.CRATES);
        setTabCompleter("crate", crateCmd);
        setTabCompleter("crates", crateCmd);
        setTabCompleter("keys", crateCmd);

        // Shop / Sell / Worth
        setExecutor("shop", new ShopCommand(this), FeatureManager.Feature.SHOP);
        setExecutor("orders", new OrdersCommand(this), FeatureManager.Feature.ORDERS);
        setExecutor("duel", new DuelCommand(this), FeatureManager.Feature.DUELS);
        setExecutor("create", new CreateCommand(this), FeatureManager.Feature.DUELS);
        setExecutor("queue", new QueueCommand(this), FeatureManager.Feature.DUELS);
        setExecutor("leave", new LeaveCommand(this));
        setExecutor("draw", new DrawCommand(this), FeatureManager.Feature.DUELS);
        setExecutor("arena", new ArenaCommand(this), FeatureManager.Feature.DUELS);
        setExecutor("ffa", new FfaCommand(this), FeatureManager.Feature.FFA);
        setExecutor("ffastats", new FfaStatsCommand(this), FeatureManager.Feature.FFA);
        setExecutor("ffaarena", new FfaArenaCommand(this), FeatureManager.Feature.FFA);
        setExecutor("auctionhouse", new AuctionHouseCommand(this), FeatureManager.Feature.AUCTION_HOUSE);
        setExecutor("enderchest", new EnderChestCommand(this), FeatureManager.Feature.ENDER_CHEST);
        setExecutor("ecsee", new EcseeCommand(this), FeatureManager.Feature.ENDER_CHEST);
        SellCommand sellCmd = new SellCommand(this);
        setExecutor("sell", sellCmd, FeatureManager.Feature.SELL);
        setExecutor("sellhand", sellCmd, FeatureManager.Feature.SELL);
        setExecutor("sellall", sellCmd, FeatureManager.Feature.SELL);
        setExecutor("sellhistory", sellCmd, FeatureManager.Feature.SELL);
        setExecutor("worth", new WorthCommand(this), FeatureManager.Feature.SELL, FeatureManager.Feature.WORTH);

        // RTP
        setExecutor("rtp", new RTPCommand(this), FeatureManager.Feature.RTP);

        // Stats / Leaderboard
        setExecutor("stats", new StatsCommand(this), FeatureManager.Feature.STATS);
        setExecutor("ping", new PingCommand(this), FeatureManager.Feature.STATS);
        setExecutor("playtime", new PlaytimeCommand(this), FeatureManager.Feature.STATS);
        LeaderboardCommand lbCmd = new LeaderboardCommand(this);
        setExecutor("leaderboard", lbCmd, FeatureManager.Feature.LEADERBOARDS);
        setExecutor("freeze", new FreezeCommand(this));
        setExecutor("fly", new FlyCommand(this));
        setExecutor("heal", new HealCommand(this));
        setExecutor("feed", new FeedCommand(this));
        registerGodModeCommand(new GodModeCommand(this));
        GamemodeCommand gamemodeCommand = new GamemodeCommand(this);
        setExecutor("gamemode", gamemodeCommand, FeatureManager.Feature.GAMEMODE);
        setTabCompleter("gamemode", gamemodeCommand);
        setExecutor("staffmode", new StaffModeCommand(this));
        setExecutor("stafflist", new StaffListCommand(this), FeatureManager.Feature.STAFF_MODE);
        setExecutor("staffchat", new StaffChatCommand(this), FeatureManager.Feature.STAFF_CHAT);
        setExecutor("helpop", new HelpopCommand(this), FeatureManager.Feature.STAFF_ALERTS);
        setExecutor("report", new ReportCommand(this), FeatureManager.Feature.STAFF_ALERTS);
        setExecutor("rename", new RenameCommand(this));
        setExecutor("randomteleport", new RandomTeleportCommand(this));
        setExecutor("teleport", new TeleportCommand(this));
        setExecutor("alts", new AltsCommand(this));
        setExecutor("vanish", new VanishCommand(this), FeatureManager.Feature.STAFF_MODE);
        setExecutor("invsee", new InvseeCommand(this));
        setExecutor("profileviewer", new ProfileViewerCommand(this), FeatureManager.Feature.PROFILE_VIEWER);
        setExecutor("punishments", new PunishmentHistoryCommand(this), FeatureManager.Feature.PUNISHMENTS);
        PunishmentCommand punishmentCommand = new PunishmentCommand(this);
        setExecutor("ban", punishmentCommand, FeatureManager.Feature.PUNISHMENTS);
        setExecutor("tempban", punishmentCommand, FeatureManager.Feature.PUNISHMENTS);
        setExecutor("mute", punishmentCommand, FeatureManager.Feature.PUNISHMENTS);
        setExecutor("tempmute", punishmentCommand, FeatureManager.Feature.PUNISHMENTS);
        setExecutor("warn", punishmentCommand, FeatureManager.Feature.PUNISHMENTS);
        setExecutor("kick", punishmentCommand, FeatureManager.Feature.PUNISHMENTS);
        setExecutor("blacklist", punishmentCommand, FeatureManager.Feature.PUNISHMENTS);
        setExecutor("unban", punishmentCommand, FeatureManager.Feature.PUNISHMENTS);
        setExecutor("unmute", punishmentCommand, FeatureManager.Feature.PUNISHMENTS);
        setExecutor("unblacklist", punishmentCommand, FeatureManager.Feature.PUNISHMENTS);

        // Bounty
        setExecutor("bounty", new BountyCommand(this), FeatureManager.Feature.BOUNTY);

        // Warps
        WarpCommand warpCmd = new WarpCommand(this);
        WarpManagerCommand warpManagerCmd = new WarpManagerCommand(this);
        WarpTabCompleter warpTabCompleter = new WarpTabCompleter(this);
        setExecutor("warp", warpCmd, FeatureManager.Feature.WARPS);
        setTabCompleter("warp", warpTabCompleter);
        setExecutor("warpmanager", warpManagerCmd, FeatureManager.Feature.WARPS);
        setTabCompleter("warpmanager", warpTabCompleter);
        setExecutor("setwarp", warpManagerCmd, FeatureManager.Feature.WARPS);
        setTabCompleter("setwarp", warpTabCompleter);
        setExecutor("delwarp", warpManagerCmd, FeatureManager.Feature.WARPS);
        setTabCompleter("delwarp", warpTabCompleter);

        PortalManagerCommand portalManagerCmd = new PortalManagerCommand(this);
        PortalTabCompleter portalTabCompleter = new PortalTabCompleter(this);
        setExecutor("portalmanager", portalManagerCmd, FeatureManager.Feature.PORTALS);
        setTabCompleter("portalmanager", portalTabCompleter);

        // Misc toggles
        setExecutor("nightvision", new NightVisionCommand(this), FeatureManager.Feature.NIGHT_VISION);
        setExecutor("phantom", new PhantomCommand(this), FeatureManager.Feature.PHANTOM);
        setExecutor("findplayer", new FindPlayerCommand(this), FeatureManager.Feature.FIND_PLAYER);
        setExecutor("settings", new SettingsCommand(this), FeatureManager.Feature.SETTINGS);

        // Social / info
        SocialCommand socialCmd = new SocialCommand(this);
        setExecutor("discord", socialCmd, FeatureManager.Feature.SOCIAL);
        setExecutor("twitter", socialCmd, FeatureManager.Feature.SOCIAL);
        setExecutor("store", socialCmd, FeatureManager.Feature.SOCIAL);
        setExecutor("social", socialCmd, FeatureManager.Feature.SOCIAL);

        setExecutor("rules", new RulesCommand(this), FeatureManager.Feature.RULES);
        setExecutor("help", new HelpCommand(this), FeatureManager.Feature.HELP);
        setExecutor("servers", new ServersCommand(this), FeatureManager.Feature.NETWORK_SERVERS);

        // Billford
        setExecutor("billford", new BillfordCommand(this), FeatureManager.Feature.BILLFORD);
        setExecutor("spawner", new SpawnerCommand(this), FeatureManager.Feature.SPAWNERS);
        setExecutor("spawnstash", new SpawnStashCommand(this), FeatureManager.Feature.SPAWN_STASH);
        setExecutor("fakeplayer", new FakePlayerCommand(this), FeatureManager.Feature.STAFF_MODE);
        HideCommand hideCommand = new HideCommand(this);
        setExecutor("hide", hideCommand, FeatureManager.Feature.HIDE);
        setExecutor("disguise", hideCommand, FeatureManager.Feature.HIDE);
        setTabCompleter("hide", hideCommand);
        setTabCompleter("disguise", hideCommand);

        // Admin
        setExecutor("clearlag", new ClearLagCommand(this), FeatureManager.Feature.CLEAR_LAG);
        setExecutor("cuboid", new CuboidCommand(this), FeatureManager.Feature.CUBOIDS);
        AmethystToolCommand amethystToolCommand = new AmethystToolCommand(this);
        setExecutor("amethysttool", amethystToolCommand, FeatureManager.Feature.AMETHYST_TOOLS);
        setTabCompleter("amethysttool", amethystToolCommand);
        UltimateDonutSmpCommand ultimateDonutSmpCommand = new UltimateDonutSmpCommand(this);
        setExecutor("ultimatedonutsmp", ultimateDonutSmpCommand);
        setTabCompleter("ultimatedonutsmp", ultimateDonutSmpCommand);
        MaintenanceCommand maintenanceCommand = new MaintenanceCommand(this);
        setExecutor("maintenance", maintenanceCommand, FeatureManager.Feature.MAINTENANCE);
        setTabCompleter("maintenance", maintenanceCommand);
        ServerWipeCommand serverWipeCommand = new ServerWipeCommand(this);
        setExecutor("serverwipe", serverWipeCommand);
        setTabCompleter("serverwipe", serverWipeCommand);
    }

    private void setExecutor(String commandName, CommandExecutor executor, FeatureManager.Feature... requiredFeatures) {
        PluginCommand command = getCommand(commandName);
        if (command == null) {
            getLogger().warning("Command missing from plugin.yml: " + commandName);
            return;
        }

        if (requiredFeatures == null || requiredFeatures.length == 0) {
            command.setExecutor(executor);
            return;
        }

        FeatureCommandExecutor featureExecutor = new FeatureCommandExecutor(this, executor, requiredFeatures);
        command.setExecutor(featureExecutor);
        if (executor instanceof TabCompleter) {
            command.setTabCompleter(featureExecutor);
        }
    }

    private void registerGodModeCommand(GodModeCommand executor) {
        PluginCommand command = getCommand("god");
        if (command != null) {
            command.setExecutor(executor);
            return;
        }

        if (!executor.registerDynamically()) {
            getLogger().warning("Command missing from plugin.yml and dynamic registration failed: god");
        }
    }

    @SuppressWarnings("unused")
    private void setTabCompleter(String commandName, TabCompleter tabCompleter) {
        PluginCommand pluginCommand = getCommand(commandName);
        if (pluginCommand == null) {
            getLogger().warning("Command missing from plugin.yml: " + commandName);
            return;
        }
        pluginCommand.setTabCompleter((sender, tabCommand, alias, args) -> {
            if (!isTabCompletionFeatureEnabled(tabCommand.getName())) {
                return java.util.Collections.emptyList();
            }
            var completions = tabCompleter.onTabComplete(sender, tabCommand, alias, args);
            return completions == null ? java.util.Collections.emptyList() : completions;
        });
    }

    private void registerFallbackTabCompleters(TabCompleter tabCompleter) {
        for (String commandName : getDescription().getCommands().keySet()) {
            PluginCommand command = getCommand(commandName);
            if (command == null) {
                getLogger().warning("Command missing from plugin.yml: " + commandName);
                continue;
            }
            command.setTabCompleter(tabCompleter);
        }
    }

    private boolean isTabCompletionFeatureEnabled(String commandName) {
        if (featureManager == null) {
            return true;
        }
        for (FeatureManager.Feature feature : FeatureManager.featuresForCommand(commandName)) {
            if (feature != null && !featureManager.isEnabled(feature)) {
                return false;
            }
        }
        return true;
    }

    private void registerVaultEconomyProvider() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return;
        }

        getServer().getServicesManager().register(
                net.milkbowl.vault.economy.Economy.class,
                new VaultEconomyHook(this),
                this,
                ServicePriority.Highest);
        getLogger().info("Vault economy provider registered.");
    }

    public void initializeLunarRichPresenceManager() {
        if (!featureManager.isEnabled(FeatureManager.Feature.LUNAR_RICH_PRESENCE)
                || !configManager.getConfig().getBoolean("LUNAR-CLIENT.RICH-PRESENCE.ENABLED", true)) {
            return;
        }

        if (!isClassAvailable("com.lunarclient.apollo.Apollo")) {
            getLogger().warning("Lunar Rich Presence is enabled, but the Apollo plugin/API is not installed.");
            return;
        }

        try {
            lunarRichPresenceManager = new LunarRichPresenceManager(this);
        } catch (Throwable error) {
            lunarRichPresenceManager = null;
            getLogger().log(java.util.logging.Level.WARNING, "Failed to initialize Lunar Rich Presence.", error);
        }
    }

    private void initializeLuckPermsTablistRefreshBridge() {
        if (getServer().getPluginManager().getPlugin("LuckPerms") == null
                && !isClassAvailable("net.luckperms.api.LuckPermsProvider")) {
            return;
        }

        try {
            luckPermsTablistRefreshBridge = new LuckPermsTablistRefreshBridge(this);
            luckPermsTablistRefreshBridge.start();
        } catch (Throwable error) {
            luckPermsTablistRefreshBridge = null;
            getLogger().log(java.util.logging.Level.WARNING, "Failed to initialize LuckPerms tablist refresh bridge.",
                    error);
        }
    }

    private void initializeSkinsRestorerTablistRefreshBridge() {
        if (!isClassAvailable("net.skinsrestorer.api.SkinsRestorerProvider")) {
            return;
        }

        try {
            skinsRestorerTablistRefreshBridge = new SkinsRestorerTablistRefreshBridge(this);
            skinsRestorerTablistRefreshBridge.start();
        } catch (Throwable error) {
            skinsRestorerTablistRefreshBridge = null;
            getLogger().log(java.util.logging.Level.WARNING, "Failed to initialize SkinsRestorer tablist refresh bridge.",
                    error);
        }
    }

    private boolean isClassAvailable(String className) {
        try {
            Class.forName(className, false, getClassLoader());
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    // ── Static accessor ───────────────────────────────────────────────────────

    public void reloadAllPluginConfigurations() {
        if (teleportManager != null) {
            teleportManager.restoreAllRtpChunkThrottles();
        }
        configManager.reload();
        languageManager.reload();
        crashProtectionManager.reload();
        currencyManager.reload();
        optimizationManager.reload();
        warpManager.loadAll();
        spawnManager.load();
        worthManager.reload();
        shopManager.reload();
        ordersManager.reload();
        duelManager.reload();
        ffaManager.reload();
        auctionHouseManager.reload();
        billfordManager.load();
        rtpManager.reload();
        portalManager.loadAll();
        crateManager.reload();
        crateVisualManager.reload();
        keyAllManager.reload();
        shardManager.reloadSettings();
        rtpZoneManager.reloadSettings();
        enderChestManager.reload();
        freezeManager.reload();
        staffModeManager.reload();
        invseeManager.reload();
        configManager.reloadSpawners();
        configManager.reloadSpawnStash();
        configManager.reloadNetwork();
        configManager.reloadDatabase();
        configManager.reloadDiscord();
        spawnerManager.reload();
        antiEspManager.reload();
        antiEspManager.refreshAllPlayers();
        spawnStashManager.reload();
        fakePlayerManager.reload();
        hideManager.reload();
        networkStatusManager.reload();
        networkStaffChatManager.reload();
        networkStaffAlertManager.reload();
        discordWebhookManager.reload();
        if (maintenanceManager != null) {
            maintenanceManager.load();
        }
        leaderboardManager.invalidateAll();
        scoreboardManager.updateAll();
        tablistManager.updateAll();
        if (lunarRichPresenceManager != null) {
            lunarRichPresenceManager.reload();
        } else {
            initializeLunarRichPresenceManager();
        }

        for (Player player : getServer().getOnlinePlayers()) {
            tablistManager.updateTablistName(player);
            worthManager.clearWorthDisplay(player);
            worthManager.syncWorthDisplay(player);
        }
    }

    public static UltimateDonutSmp getInstance() {
        return instance;
    }

    public NamespacedKey getKey(String key) {
        return new NamespacedKey(this, key);
    }

    public SpigotScheduler getSpigotScheduler() {
        return SpigotScheduler;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    public CurrencyManager getCurrencyManager() {
        return currencyManager;
    }

    public FeatureManager getFeatureManager() {
        return featureManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public ChatManager getChatManager() {
        return chatManager;
    }

    public IgnoreManager getIgnoreManager() {
        return ignoreManager;
    }

    public PrivateMessageManager getPrivateMessageManager() {
        return privateMessageManager;
    }

    public TeamManager getTeamManager() {
        return teamManager;
    }

    public HomeManager getHomeManager() {
        return homeManager;
    }

    public BountyManager getBountyManager() {
        return bountyManager;
    }

    public WarpManager getWarpManager() {
        return warpManager;
    }

    public CuboidManager getCuboidManager() {
        return cuboidManager;
    }

    public SpawnManager getSpawnManager() {
        return spawnManager;
    }

    public SpawnStashManager getSpawnStashManager() {
        return spawnStashManager;
    }

    public FakePlayerManager getFakePlayerManager() {
        return fakePlayerManager;
    }

    public HideManager getHideManager() {
        return hideManager;
    }

    public CombatManager getCombatManager() {
        return combatManager;
    }

    public FastCrystalManager getFastCrystalManager() {
        return fastCrystalManager;
    }

    public TPAManager getTPAManager() {
        return tpaManager;
    }

    public ShardManager getShardManager() {
        return shardManager;
    }

    public ClearLagManager getClearLagManager() {
        return clearLagManager;
    }

    public CrateManager getCrateManager() {
        return crateManager;
    }

    public CrateVisualManager getCrateVisualManager() {
        return crateVisualManager;
    }

    public KeyAllManager getKeyAllManager() {
        return keyAllManager;
    }

    public AFKManager getAFKManager() {
        return afkManager;
    }

    public HoverStatsManager getHoverStatsManager() {
        return hoverStatsManager;
    }

    public WorthManager getWorthManager() {
        return worthManager;
    }

    public ShopManager getShopManager() {
        return shopManager;
    }

    public OrdersManager getOrdersManager() {
        return ordersManager;
    }

    public DuelManager getDuelManager() {
        return duelManager;
    }

    public FfaManager getFfaManager() {
        return ffaManager;
    }

    public AuctionHouseManager getAuctionHouseManager() {
        return auctionHouseManager;
    }

    public BillfordManager getBillfordManager() {
        return billfordManager;
    }

    public LeaderboardManager getLeaderboardManager() {
        return leaderboardManager;
    }

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

    public TablistManager getTablistManager() {
        return tablistManager;
    }

    public TeleportManager getTeleportManager() {
        return teleportManager;
    }

    public RTPManager getRtpManager() {
        return rtpManager;
    }

    public RTPZoneManager getRtpZoneManager() {
        return rtpZoneManager;
    }

    public PortalManager getPortalManager() {
        return portalManager;
    }

    public AmethystToolsManager getAmethystToolsManager() {
        return amethystToolsManager;
    }

    public EnderChestManager getEnderChestManager() {
        return enderChestManager;
    }

    public FreezeManager getFreezeManager() {
        return freezeManager;
    }

    public GodModeManager getGodModeManager() {
        return godModeManager;
    }

    public StaffModeManager getStaffModeManager() {
        return staffModeManager;
    }

    public InvseeManager getInvseeManager() {
        return invseeManager;
    }

    public ProfileViewerManager getProfileViewerManager() {
        return profileViewerManager;
    }

    public PunishmentManager getPunishmentManager() {
        return punishmentManager;
    }

    public StatsWipeManager getStatsWipeManager() {
        return statsWipeManager;
    }

    public ServerWipeManager getServerWipeManager() {
        return serverWipeManager;
    }

    public SpawnerManager getSpawnerManager() {
        return spawnerManager;
    }

    public AntiEspManager getAntiEspManager() {
        return antiEspManager;
    }

    public NetworkStatusManager getNetworkStatusManager() {
        return networkStatusManager;
    }

    public RedisManager getRedisManager() {
        return redisManager;
    }

    public MaintenanceManager getMaintenanceManager() {
        return maintenanceManager;
    }

    public NetworkStaffChatManager getNetworkStaffChatManager() {
        return networkStaffChatManager;
    }

    public NetworkStaffAlertManager getNetworkStaffAlertManager() {
        return networkStaffAlertManager;
    }

    public DiscordWebhookManager getDiscordWebhookManager() {
        return discordWebhookManager;
    }

    public LunarRichPresenceManager getLunarRichPresenceManager() {
        return lunarRichPresenceManager;
    }

    public OptimizationManager getOptimizationManager() {
        return optimizationManager;
    }

    public CrashProtectionManager getCrashProtectionManager() {
        return crashProtectionManager;
    }
}
