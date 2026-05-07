package com.bx.ultimateDonutSmp;

import com.bx.ultimateDonutSmp.amethyst.*;
import com.bx.ultimateDonutSmp.api.EconomyExpansion;
import com.bx.ultimateDonutSmp.commands.*;
import com.bx.ultimateDonutSmp.hooks.VaultEconomyHook;
import com.bx.ultimateDonutSmp.listeners.*;
import com.bx.ultimateDonutSmp.managers.*;
import com.bx.ultimateDonutSmp.tasks.*;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.PaperScheduler;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public final class UltimateDonutSmp extends JavaPlugin {

    private static UltimateDonutSmp instance;
    private PaperScheduler paperScheduler;

    // ── Managers ──────────────────────────────────────────────────────────────
    private ConfigManager      configManager;
    private DatabaseManager    databaseManager;
    private PlayerDataManager  playerDataManager;
    private EconomyManager     economyManager;
    private ChatManager        chatManager;
    private IgnoreManager      ignoreManager;
    private PrivateMessageManager privateMessageManager;
    private TeamManager        teamManager;
    private HomeManager        homeManager;
    private BountyManager      bountyManager;
    private WarpManager        warpManager;
    private CuboidManager      cuboidManager;
    private SpawnManager       spawnManager;
    private CombatManager      combatManager;
    private FastCrystalManager fastCrystalManager;
    private TPAManager         tpaManager;
    private ShardManager       shardManager;
    private ClearLagManager    clearLagManager;
    private CrateManager       crateManager;
    private CrateVisualManager crateVisualManager;
    private KeyAllManager      keyAllManager;
    private AFKManager         afkManager;
    private HoverStatsManager  hoverStatsManager;
    private WorthManager       worthManager;
    private ShopManager        shopManager;
    private OrdersManager      ordersManager;
    private DuelManager        duelManager;
    private FfaManager         ffaManager;
    private AuctionHouseManager auctionHouseManager;
    private BillfordManager    billfordManager;
    private LeaderboardManager leaderboardManager;
    private ScoreboardManager  scoreboardManager;
    private TablistManager     tablistManager;
    private TeleportManager    teleportManager;
    private RTPManager         rtpManager;
    private RTPZoneManager     rtpZoneManager;
    private PortalManager      portalManager;
    private AmethystToolsManager amethystToolsManager;
    private EnderChestManager  enderChestManager;
    private FreezeManager      freezeManager;
    private InvseeManager      invseeManager;
    private ProfileViewerManager profileViewerManager;
    private PunishmentManager  punishmentManager;
    private StatsWipeManager  statsWipeManager;
    private SpawnerManager    spawnerManager;
    private AntiEspManager    antiEspManager;
    private NetworkStatusManager networkStatusManager;
    private RedisManager redisManager;
    private NetworkStaffChatManager networkStaffChatManager;
    private NetworkStaffAlertManager networkStaffAlertManager;
    private StaffModeManager staffModeManager;
    private DiscordWebhookManager discordWebhookManager;
    private LunarRichPresenceManager lunarRichPresenceManager;
    private OptimizationManager optimizationManager;

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    public void onEnable() {
        instance = this;
        paperScheduler = new PaperScheduler(this);

        // 1. Config & database (no dependencies)
        ColorUtils.init();

        configManager   = new ConfigManager(this);
        configManager.loadAll();
        optimizationManager = new OptimizationManager(this);
        optimizationManager.start();

        databaseManager = new DatabaseManager(this);
        databaseManager.initialize();

        // 2. Data managers (depend on DB / config)
        playerDataManager = new PlayerDataManager(this);
        economyManager    = new EconomyManager(this);
        chatManager       = new ChatManager(this);
        ignoreManager     = new IgnoreManager(this);
        privateMessageManager = new PrivateMessageManager(this);
        teamManager       = new TeamManager(this);
        teamManager.loadAll();
        homeManager       = new HomeManager(this);
        bountyManager     = new BountyManager(this);
        bountyManager.loadAll();
        warpManager       = new WarpManager(this);
        warpManager.loadAll();
        cuboidManager     = new CuboidManager(this);
        cuboidManager.loadAll();

        spawnManager = new SpawnManager(this);
        spawnManager.load();

        // 3. Gameplay managers
        combatManager   = new CombatManager(this);
        fastCrystalManager = new FastCrystalManager(this);
        tpaManager      = new TPAManager(this);
        shardManager    = new ShardManager(this);
        amethystToolsManager = new AmethystToolsManager(this);
        clearLagManager = new ClearLagManager(this);
        crateManager    = new CrateManager(this);
        crateVisualManager = new CrateVisualManager(this);
        keyAllManager   = new KeyAllManager(this);
        afkManager      = new AFKManager(this);
        hoverStatsManager = new HoverStatsManager(this);
        worthManager    = new WorthManager(this);
        shopManager     = new ShopManager(this);
        ordersManager   = new OrdersManager(this);
        duelManager     = new DuelManager(this);
        ffaManager      = new FfaManager(this);
        auctionHouseManager = new AuctionHouseManager(this);
        billfordManager = new BillfordManager(this);
        billfordManager.load();
        leaderboardManager = new LeaderboardManager(this);
        enderChestManager = new EnderChestManager(this);
        freezeManager = new FreezeManager(this);
        staffModeManager = new StaffModeManager(this);
        invseeManager = new InvseeManager(this);
        profileViewerManager = new ProfileViewerManager(this);
        punishmentManager = new PunishmentManager(this);
        statsWipeManager = new StatsWipeManager(this);
        spawnerManager = new SpawnerManager(this);
        antiEspManager = new AntiEspManager(this);
        redisManager = new RedisManager(this);
        networkStatusManager = new NetworkStatusManager(this);
        networkStaffChatManager = new NetworkStaffChatManager(this);
        networkStaffAlertManager = new NetworkStaffAlertManager(this);
        discordWebhookManager = new DiscordWebhookManager(this);
        initializeLunarRichPresenceManager();

        // 4. Display managers
        scoreboardManager = new ScoreboardManager(this);
        tablistManager    = new TablistManager(this);
        teleportManager   = new TeleportManager(this);
        rtpManager        = new RTPManager(this);
        rtpZoneManager    = new RTPZoneManager(this);
        portalManager     = new PortalManager(this);
        portalManager.loadAll();

        // 5. Listeners
        registerListeners();

        // 6. Commands
        registerCommands();

        // 6.5 Optional integrations
        registerVaultEconomyProvider();

        // 7. Background tasks
        ScoreboardTask.start(this);
        TablistTask.start(this);
        ShardTask.start(this);        // passive "everywhere" shards (per minute)
        ShardCuboidTask.start(this);  // spawn cuboid countdown + reward (per second)
        RTPZoneTask.start(this);
        ClearLagTask.start(this);
        KeyAllTask.start(this);
        AutoSaveTask.start(this);
        AFKCheckTask.start(this);
        LunarTeammatesTask.start(this);
        BillfordTask.start(this);     // Billford trade rotation check (every 30 s)
        OrdersExpiryTask.start(this);
        AuctionHouseExpiryTask.start(this);
        AmethystToolsTask.start(this);
        SpawnerGenerationTask.start(this);
        DuelMatchTask.start(this);
        FfaMatchTask.start(this);

        // 8. PlaceholderAPI expansion
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new EconomyExpansion(this).register();
            getLogger().info("PlaceholderAPI expansion registered.");
        }

        getLogger().info("UltimateDonutSmp enabled successfully.");
    }

    @Override
    public void onDisable() {
        if (worthManager != null) {
            getServer().getOnlinePlayers().forEach(worthManager::clearWorthDisplay);
        }

        if (enderChestManager != null) {
            enderChestManager.shutdown();
        }
        if (freezeManager != null) {
            freezeManager.shutdown();
        }
        if (staffModeManager != null) {
            staffModeManager.shutdown();
        }
        if (invseeManager != null) {
            invseeManager.shutdown();
        }
        if (antiEspManager != null) {
            antiEspManager.shutdown();
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
        if (spawnerManager != null) {
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
        if (playerDataManager != null) {
            playerDataManager.saveAll();
        }
        if (databaseManager != null) {
            databaseManager.close();
        }
        getServer().getServicesManager().unregisterAll(this);
        getLogger().info("UltimateDonutSmp disabled.");
    }

    // ── Registration helpers ──────────────────────────────────────────────────

    private void registerListeners() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerJoinQuitListener(this), this);
        pm.registerEvents(new PlayerDeathListener(this), this);
        pm.registerEvents(new ChatListener(this), this);
        pm.registerEvents(new CombatListener(this), this);
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
        pm.registerEvents(new PunishmentCommandAliasListener(this), this);
    }

    private void registerCommands() {
        // Team
        TeamCommand teamCmd = new TeamCommand(this);
        getCommand("team").setExecutor(teamCmd);
        ChatCommand chatCommand = new ChatCommand(this);
        ChatTabCompleter chatTabCompleter = new ChatTabCompleter();
        getCommand("chat").setExecutor(chatCommand);
        getCommand("chat").setTabCompleter(chatTabCompleter);
        IgnoreCommand ignoreCommand = new IgnoreCommand(this);
        IgnoreTabCompleter ignoreTabCompleter = new IgnoreTabCompleter(this);
        getCommand("ignore").setExecutor(ignoreCommand);
        getCommand("ignore").setTabCompleter(ignoreTabCompleter);
        getCommand("unignore").setExecutor(ignoreCommand);
        getCommand("unignore").setTabCompleter(ignoreTabCompleter);
        MessageCommand messageCommand = new MessageCommand(this);
        MessageTabCompleter messageTabCompleter = new MessageTabCompleter();
        getCommand("msg").setExecutor(messageCommand);
        getCommand("msg").setTabCompleter(messageTabCompleter);
        getCommand("reply").setExecutor(messageCommand);
        getCommand("reply").setTabCompleter(messageTabCompleter);
        getCommand("pm").setExecutor(new PrivateMessageToggleCommand(this));

        // Homes
        HomeCommand homeCmd = new HomeCommand(this);
        getCommand("home").setExecutor(homeCmd);
        getCommand("homes").setExecutor(homeCmd);
        getCommand("sethome").setExecutor(homeCmd);
        getCommand("delhome").setExecutor(homeCmd);
        getCommand("renamehome").setExecutor(homeCmd);

        // Spawn / AFK
        getCommand("spawn").setExecutor(new SpawnCommand(this));
        getCommand("afk").setExecutor(new AFKCommand(this));

        // Teleport
        TPACommand tpaCmd = new TPACommand(this);
        getCommand("tpa").setExecutor(tpaCmd);
        getCommand("tpahere").setExecutor(tpaCmd);
        getCommand("tpaccept").setExecutor(tpaCmd);
        getCommand("tpadeny").setExecutor(tpaCmd);
        getCommand("tpacancel").setExecutor(tpaCmd);
        getCommand("tpauto").setExecutor(new TPAutoCommand(this));
        getCommand("tpahereauto").setExecutor(new TPAHereAutoCommand(this));

        // Economy
        BalanceCommand balCmd = new BalanceCommand(this);
        getCommand("balance").setExecutor(balCmd);
        getCommand("pay").setExecutor(new PayCommand(this));
        getCommand("addmoney").setExecutor(new AddMoneyCommand(this));
        getCommand("removemoney").setExecutor(new RemoveMoneyCommand(this));
        getCommand("setmoney").setExecutor(new SetMoneyCommand(this));

        ShardsCommand shardsCmd = new ShardsCommand(this);
        getCommand("shards").setExecutor(shardsCmd);
        getCommand("shardpay").setExecutor(new ShardPayCommand(this));
        CrateCommand crateCmd = new CrateCommand(this);
        getCommand("crate").setExecutor(crateCmd);
        getCommand("crates").setExecutor(crateCmd);
        getCommand("keys").setExecutor(crateCmd);

        // Shop / Sell / Worth
        getCommand("shop").setExecutor(new ShopCommand(this));
        getCommand("orders").setExecutor(new OrdersCommand(this));
        getCommand("duel").setExecutor(new DuelCommand(this));
        getCommand("queue").setExecutor(new QueueCommand(this));
        getCommand("leave").setExecutor(new LeaveCommand(this));
        getCommand("draw").setExecutor(new DrawCommand(this));
        getCommand("arena").setExecutor(new ArenaCommand(this));
        getCommand("ffa").setExecutor(new FfaCommand(this));
        getCommand("ffastats").setExecutor(new FfaStatsCommand(this));
        getCommand("ffaarena").setExecutor(new FfaArenaCommand(this));
        getCommand("auctionhouse").setExecutor(new AuctionHouseCommand(this));
        getCommand("enderchest").setExecutor(new EnderChestCommand(this));
        SellCommand sellCmd = new SellCommand(this);
        getCommand("sell").setExecutor(sellCmd);
        getCommand("sellhand").setExecutor(sellCmd);
        getCommand("sellall").setExecutor(sellCmd);
        getCommand("sellhistory").setExecutor(sellCmd);
        getCommand("worth").setExecutor(new WorthCommand(this));

        // RTP
        getCommand("rtp").setExecutor(new RTPCommand(this));

        // Stats / Leaderboard
        getCommand("stats").setExecutor(new StatsCommand(this));
        getCommand("ping").setExecutor(new PingCommand(this));
        getCommand("playtime").setExecutor(new PlaytimeCommand(this));
        LeaderboardCommand lbCmd = new LeaderboardCommand(this);
        getCommand("leaderboard").setExecutor(lbCmd);
        getCommand("freeze").setExecutor(new FreezeCommand(this));
        getCommand("fly").setExecutor(new FlyCommand(this));
        getCommand("heal").setExecutor(new HealCommand(this));
        getCommand("feed").setExecutor(new FeedCommand(this));
        GamemodeCommand gamemodeCommand = new GamemodeCommand(this);
        getCommand("gamemode").setExecutor(gamemodeCommand);
        getCommand("gamemode").setTabCompleter(gamemodeCommand);
        getCommand("staffmode").setExecutor(new StaffModeCommand(this));
        getCommand("stafflist").setExecutor(new StaffListCommand(this));
        getCommand("staffchat").setExecutor(new StaffChatCommand(this));
        getCommand("helpop").setExecutor(new HelpopCommand(this));
        getCommand("report").setExecutor(new ReportCommand(this));
        getCommand("rename").setExecutor(new RenameCommand(this));
        getCommand("randomteleport").setExecutor(new RandomTeleportCommand(this));
        getCommand("teleport").setExecutor(new TeleportCommand(this));
        getCommand("alts").setExecutor(new AltsCommand(this));
        getCommand("vanish").setExecutor(new VanishCommand(this));
        getCommand("invsee").setExecutor(new InvseeCommand(this));
        getCommand("profileviewer").setExecutor(new ProfileViewerCommand(this));
        getCommand("punishments").setExecutor(new PunishmentHistoryCommand(this));
        PunishmentCommand punishmentCommand = new PunishmentCommand(this);
        getCommand("ban").setExecutor(punishmentCommand);
        getCommand("tempban").setExecutor(punishmentCommand);
        getCommand("mute").setExecutor(punishmentCommand);
        getCommand("tempmute").setExecutor(punishmentCommand);
        getCommand("warn").setExecutor(punishmentCommand);
        getCommand("kick").setExecutor(punishmentCommand);
        getCommand("blacklist").setExecutor(punishmentCommand);
        getCommand("unban").setExecutor(punishmentCommand);
        getCommand("unmute").setExecutor(punishmentCommand);
        getCommand("unblacklist").setExecutor(punishmentCommand);

        // Bounty
        getCommand("bounty").setExecutor(new BountyCommand(this));

        // Warps
        WarpCommand warpCmd = new WarpCommand(this);
        WarpManagerCommand warpManagerCmd = new WarpManagerCommand(this);
        WarpTabCompleter warpTabCompleter = new WarpTabCompleter(this);
        getCommand("warp").setExecutor(warpCmd);
        getCommand("warp").setTabCompleter(warpTabCompleter);
        getCommand("warpmanager").setExecutor(warpManagerCmd);
        getCommand("warpmanager").setTabCompleter(warpTabCompleter);
        getCommand("setwarp").setExecutor(warpManagerCmd);
        getCommand("setwarp").setTabCompleter(warpTabCompleter);
        getCommand("delwarp").setExecutor(warpManagerCmd);
        getCommand("delwarp").setTabCompleter(warpTabCompleter);

        PortalManagerCommand portalManagerCmd = new PortalManagerCommand(this);
        PortalTabCompleter portalTabCompleter = new PortalTabCompleter(this);
        getCommand("portalmanager").setExecutor(portalManagerCmd);
        getCommand("portalmanager").setTabCompleter(portalTabCompleter);

        // Misc toggles
        getCommand("nightvision").setExecutor(new NightVisionCommand(this));
        getCommand("phantom").setExecutor(new PhantomCommand(this));
        getCommand("findplayer").setExecutor(new FindPlayerCommand(this));
        getCommand("settings").setExecutor(new SettingsCommand(this));

        // Social / info
        SocialCommand socialCmd = new SocialCommand(this);
        getCommand("discord").setExecutor(socialCmd);
        getCommand("twitter").setExecutor(socialCmd);
        getCommand("store").setExecutor(socialCmd);
        getCommand("social").setExecutor(socialCmd);

        getCommand("rules").setExecutor(new RulesCommand(this));
        getCommand("help").setExecutor(new HelpCommand(this));
        getCommand("servers").setExecutor(new ServersCommand(this));

        // Billford
        getCommand("billford").setExecutor(new BillfordCommand(this));
        getCommand("spawner").setExecutor(new SpawnerCommand(this));

        // Admin
        getCommand("clearlag").setExecutor(new ClearLagCommand(this));
        getCommand("cuboid").setExecutor(new CuboidCommand(this));
        AmethystToolCommand amethystToolCommand = new AmethystToolCommand(this);
        getCommand("amethysttool").setExecutor(amethystToolCommand);
        getCommand("amethysttool").setTabCompleter(amethystToolCommand);
        getCommand("ultimatedonutsmp").setExecutor(new UltimateDonutSmpCommand(this));
    }

    private void registerVaultEconomyProvider() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return;
        }

        getServer().getServicesManager().register(
                net.milkbowl.vault.economy.Economy.class,
                new VaultEconomyHook(this),
                this,
                ServicePriority.Highest
        );
        getLogger().info("Vault economy provider registered.");
    }

    private void initializeLunarRichPresenceManager() {
        if (!configManager.getConfig().getBoolean("LUNAR-CLIENT.RICH-PRESENCE.ENABLED", true)) {
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
        configManager.reload();
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
        configManager.reloadNetwork();
        configManager.reloadDatabase();
        configManager.reloadDiscord();
        spawnerManager.reload();
        antiEspManager.reload();
        antiEspManager.refreshAllPlayers();
        networkStatusManager.reload();
        networkStaffChatManager.reload();
        networkStaffAlertManager.reload();
        discordWebhookManager.reload();
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

    public static UltimateDonutSmp getInstance() { return instance; }

    public NamespacedKey getKey(String key) { return new NamespacedKey(this, key); }
    public PaperScheduler getPaperScheduler() { return paperScheduler; }

    // ── Getters ───────────────────────────────────────────────────────────────

    public ConfigManager      getConfigManager()      { return configManager; }
    public DatabaseManager    getDatabaseManager()    { return databaseManager; }
    public PlayerDataManager  getPlayerDataManager()  { return playerDataManager; }
    public EconomyManager     getEconomyManager()     { return economyManager; }
    public ChatManager        getChatManager()        { return chatManager; }
    public IgnoreManager      getIgnoreManager()      { return ignoreManager; }
    public PrivateMessageManager getPrivateMessageManager() { return privateMessageManager; }
    public TeamManager        getTeamManager()        { return teamManager; }
    public HomeManager        getHomeManager()        { return homeManager; }
    public BountyManager      getBountyManager()      { return bountyManager; }
    public WarpManager        getWarpManager()        { return warpManager; }
    public CuboidManager      getCuboidManager()      { return cuboidManager; }
    public SpawnManager       getSpawnManager()       { return spawnManager; }
    public CombatManager      getCombatManager()      { return combatManager; }
    public FastCrystalManager getFastCrystalManager() { return fastCrystalManager; }
    public TPAManager         getTPAManager()         { return tpaManager; }
    public ShardManager       getShardManager()       { return shardManager; }
    public ClearLagManager    getClearLagManager()    { return clearLagManager; }
    public CrateManager       getCrateManager()       { return crateManager; }
    public CrateVisualManager getCrateVisualManager() { return crateVisualManager; }
    public KeyAllManager      getKeyAllManager()      { return keyAllManager; }
    public AFKManager         getAFKManager()         { return afkManager; }
    public HoverStatsManager  getHoverStatsManager()  { return hoverStatsManager; }
    public WorthManager       getWorthManager()       { return worthManager; }
    public ShopManager        getShopManager()        { return shopManager; }
    public OrdersManager      getOrdersManager()      { return ordersManager; }
    public DuelManager        getDuelManager()        { return duelManager; }
    public FfaManager         getFfaManager()         { return ffaManager; }
    public AuctionHouseManager getAuctionHouseManager() { return auctionHouseManager; }
    public BillfordManager    getBillfordManager()    { return billfordManager; }
    public LeaderboardManager getLeaderboardManager() { return leaderboardManager; }
    public ScoreboardManager  getScoreboardManager()  { return scoreboardManager; }
    public TablistManager     getTablistManager()     { return tablistManager; }
    public TeleportManager    getTeleportManager()    { return teleportManager; }
    public RTPManager         getRtpManager()         { return rtpManager; }
    public RTPZoneManager     getRtpZoneManager()     { return rtpZoneManager; }
    public PortalManager      getPortalManager()      { return portalManager; }
    public AmethystToolsManager getAmethystToolsManager() { return amethystToolsManager; }
    public EnderChestManager  getEnderChestManager()  { return enderChestManager; }
    public FreezeManager getFreezeManager() { return freezeManager; }
    public StaffModeManager getStaffModeManager() { return staffModeManager; }
    public InvseeManager getInvseeManager() { return invseeManager; }
    public ProfileViewerManager getProfileViewerManager() { return profileViewerManager; }
    public PunishmentManager getPunishmentManager() { return punishmentManager; }
    public StatsWipeManager  getStatsWipeManager() { return statsWipeManager; }
    public SpawnerManager    getSpawnerManager()    { return spawnerManager; }
    public AntiEspManager    getAntiEspManager()    { return antiEspManager; }
    public NetworkStatusManager getNetworkStatusManager() { return networkStatusManager; }
    public RedisManager getRedisManager() { return redisManager; }
    public NetworkStaffChatManager getNetworkStaffChatManager() { return networkStaffChatManager; }
    public NetworkStaffAlertManager getNetworkStaffAlertManager() { return networkStaffAlertManager; }
    public DiscordWebhookManager getDiscordWebhookManager() { return discordWebhookManager; }
    public LunarRichPresenceManager getLunarRichPresenceManager() { return lunarRichPresenceManager; }
    public OptimizationManager getOptimizationManager() { return optimizationManager; }
}
