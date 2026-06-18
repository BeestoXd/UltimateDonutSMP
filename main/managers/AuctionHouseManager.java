package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.utils.PermissionUtils;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.AuctionClaim;
import com.bx.ultimateDonutSmp.models.AuctionListing;
import com.bx.ultimateDonutSmp.models.EconomyReason;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.models.PlayerPreference;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemSerializationUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class AuctionHouseManager {

    private static final int MAX_PERMISSION_VALUE = 100;

    public enum AuctionSort {
        NEWEST,
        OLDEST,
        PRICE_LOWEST,
        PRICE_HIGHEST,
        EXPIRING_SOON;

        public static AuctionSort fromConfig(String rawValue) {
            if (rawValue == null || rawValue.isBlank()) {
                return NEWEST;
            }

            try {
                return AuctionSort.valueOf(rawValue.trim().toUpperCase(Locale.US));
            } catch (IllegalArgumentException ignored) {
                return NEWEST;
            }
        }
    }

    public enum CreateFailureReason {
        DISABLED,
        NO_PLAYER_DATA,
        NO_ITEM,
        INVALID_ITEM,
        UNSAFE_ITEM,
        INVALID_PRICE,
        NO_MONEY,
        MAX_LISTINGS_REACHED,
        DATABASE_ERROR
    }

    public enum PurchaseFailureReason {
        DISABLED,
        NO_PLAYER_DATA,
        LISTING_NOT_FOUND,
        NOT_ACTIVE,
        OWN_LISTING,
        NO_MONEY,
        INVENTORY_FULL,
        DATABASE_ERROR
    }

    public enum CancelFailureReason {
        DISABLED,
        LISTING_NOT_FOUND,
        NOT_OWNER,
        NOT_ACTIVE,
        DATABASE_ERROR
    }

    public enum ClaimFailureReason {
        DISABLED,
        CLAIM_NOT_FOUND,
        NOT_OWNER,
        ALREADY_CLAIMED,
        INVENTORY_FULL,
        NO_PLAYER_DATA,
        DATABASE_ERROR
    }

    public record CreateListingResult(
            boolean success,
            CreateFailureReason reason,
            AuctionListing listing,
            double listingFee,
            CrashProtectionManager.ValidationResult safetyResult
    ) {
        public CreateListingResult(boolean success, CreateFailureReason reason, AuctionListing listing, double listingFee) {
            this(success, reason, listing, listingFee, null);
        }
    }

    public record PurchaseListingResult(
            boolean success,
            PurchaseFailureReason reason,
            AuctionListing listing
    ) {}

    public record CancelListingResult(
            boolean success,
            CancelFailureReason reason,
            AuctionListing listing
    ) {}

    public record ClaimResult(
            boolean success,
            ClaimFailureReason reason,
            AuctionClaim claim
    ) {}

    private final UltimateDonutSmp plugin;
    private final Set<UUID> activeTransactions = new HashSet<>();
    private final java.util.Map<UUID, Long> lastClickTimes = new java.util.HashMap<>();
    private final java.util.Map<UUID, PlayerPreference> preferenceCache = new java.util.concurrent.ConcurrentHashMap<>();
    private final Set<UUID> awaitingSearch = new HashSet<>();
    private final java.util.Map<UUID, String> searchQueries = new java.util.HashMap<>();
    private final Set<UUID> navigating = java.util.concurrent.ConcurrentHashMap.newKeySet();

    public AuctionHouseManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
        ensureTables();
        reload();
    }

    public void reload() {
        validateConfiguration();
        preferenceCache.clear();
        awaitingSearch.clear();
        searchQueries.clear();
    }

    public void prepareForServerWipe() {
        activeTransactions.clear();
        lastClickTimes.clear();
    }

    public boolean isEnabled() {
        return plugin.getFeatureManager().isEnabled(FeatureManager.Feature.AUCTION_HOUSE)
                && config().getBoolean("SETTINGS.ENABLED", true);
    }

    public String getBrowseTitle() {
        return config().getString("GUI.BROWSE.TITLE", "&8ᴀᴜᴄᴛɪᴏɴ ʜᴏᴜѕᴇ");
    }

    public int getBrowseSize() {
        return normalizeSize(config().getInt("GUI.BROWSE.SIZE", 54));
    }

    public int getBrowseItemsPerPage() {
        return Math.max(1, Math.min(45, config().getInt("GUI.BROWSE.ITEMS_PER_PAGE", 45)));
    }

    public String getMyListingsTitle() {
        return config().getString("GUI.MY_LISTINGS.TITLE", "&8ᴍʏ ᴀᴜᴄᴛɪᴏɴѕ");
    }

    public int getMyListingsSize() {
        return normalizeSize(config().getInt("GUI.MY_LISTINGS.SIZE", 54));
    }

    public int getMyListingsItemsPerPage() {
        return Math.max(1, Math.min(45, config().getInt("GUI.MY_LISTINGS.ITEMS_PER_PAGE", 45)));
    }

    public String getClaimsTitle() {
        return config().getString("GUI.CLAIMS.TITLE", "&8ᴀᴜᴄᴛɪᴏɴ ᴄʟᴀɪᴍѕ");
    }

    public int getClaimsSize() {
        return normalizeSize(config().getInt("GUI.CLAIMS.SIZE", 54));
    }

    public int getClaimsItemsPerPage() {
        return Math.max(1, Math.min(45, config().getInt("GUI.CLAIMS.ITEMS_PER_PAGE", 45)));
    }

    public AuctionSort getDefaultSort() {
        return AuctionSort.fromConfig(config().getString("SORTING.DEFAULT", "NEWEST"));
    }

    public List<AuctionSort> getAllowedSorts() {
        List<AuctionSort> sorts = new ArrayList<>();
        for (String rawValue : config().getStringList("SORTING.ALLOWED")) {
            AuctionSort sort = AuctionSort.fromConfig(rawValue);
            if (!sorts.contains(sort)) {
                sorts.add(sort);
            }
        }
        if (sorts.isEmpty()) {
            sorts.addAll(List.of(AuctionSort.NEWEST, AuctionSort.PRICE_LOWEST, AuctionSort.PRICE_HIGHEST));
        }
        return List.copyOf(sorts);
    }

    public PlayerPreference getPreference(UUID uuid) {
        return preferenceCache.computeIfAbsent(uuid, k -> loadPreference(k));
    }

    public synchronized PlayerPreference loadPreference(UUID uuid) {
        try (PreparedStatement ps = connection().prepareStatement(
                "SELECT * FROM player_auction_preferences WHERE player_uuid = ? LIMIT 1")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new PlayerPreference(
                            uuid,
                            rs.getInt("fast_buy_enabled") == 1,
                            rs.getInt("fast_sell_enabled") == 1,
                            rs.getInt("last_duration_hours"),
                            rs.getString("last_category"),
                            rs.getDouble("last_price")
                    );
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load player preferences for " + uuid, e);
        }
        return new PlayerPreference(uuid);
    }

    public synchronized void savePreference(PlayerPreference pref) {
        preferenceCache.put(pref.playerId(), pref);
        plugin.getSpigotScheduler().runAsync(() -> {
            try (PreparedStatement ps = connection().prepareStatement(
                    "REPLACE INTO player_auction_preferences (player_uuid, fast_buy_enabled, fast_sell_enabled, last_duration_hours, last_category, last_price) " +
                    "VALUES (?,?,?,?,?,?)")) {
                ps.setString(1, pref.playerId().toString());
                ps.setInt(2, pref.fastBuyEnabled() ? 1 : 0);
                ps.setInt(3, pref.fastSellEnabled() ? 1 : 0);
                ps.setInt(4, pref.lastDurationHours());
                ps.setString(5, pref.lastCategory());
                ps.setDouble(6, pref.lastPrice());
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to save player preferences for " + pref.playerId(), e);
            }
        });
    }

    public boolean hasPendingSearchInput(UUID uuid) {
        return awaitingSearch.contains(uuid);
    }

    public void beginSearch(Player player) {
        awaitingSearch.add(player.getUniqueId());
        player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                "AUCTION_HOUSE.SEARCH_PROMPT",
                "&6&lAuctionHouse &8» &eType an item name in chat to search the auction house."
        )));
        player.closeInventory();
    }

    public void handlePendingSearchInput(Player player, String query) {
        awaitingSearch.remove(player.getUniqueId());
        if (query == null || query.isBlank() || query.equalsIgnoreCase("cancel")) {
            searchQueries.remove(player.getUniqueId());
        } else {
            searchQueries.put(player.getUniqueId(), query);
        }
        plugin.getSpigotScheduler().runEntity(player, () -> {
            new com.bx.ultimateDonutSmp.menus.AuctionHouseBrowseMenu(plugin, 1, getDefaultSort()).open(player);
        });
    }

    public String getSearchQuery(UUID uuid) {
        return searchQueries.getOrDefault(uuid, "");
    }

    public void clearSearchQuery(UUID uuid) {
        searchQueries.remove(uuid);
    }

    public void setSearchQuery(UUID uuid, String query) {
        if (query == null || query.isBlank() || query.equalsIgnoreCase("cancel")) {
            searchQueries.remove(uuid);
        } else {
            searchQueries.put(uuid, query);
        }
    }

    public void startNavigating(UUID uuid) {
        navigating.add(uuid);
    }

    public boolean stopNavigating(UUID uuid) {
        return navigating.remove(uuid);
    }

    public List<AuctionListing> getActiveListings(AuctionSort sort, String categoryFilter, String searchQuery) {
        List<AuctionListing> listings = getActiveListings(sort);
        if (categoryFilter != null && !categoryFilter.equalsIgnoreCase("ALL")) {
            listings.removeIf(listing -> !matchesCategory(listing, categoryFilter));
        }
        if (searchQuery != null && !searchQuery.isBlank()) {
            String cleanQuery = searchQuery.toLowerCase().trim();
            listings.removeIf(listing -> {
                String displayName = describeItem(listing.item()).toLowerCase();
                String typeName = listing.item().getType().name().toLowerCase().replace('_', ' ');
                return !displayName.contains(cleanQuery) && !typeName.contains(cleanQuery);
            });
        }
        return listings;
    }

    private boolean matchesCategory(AuctionListing listing, String category) {
        Material type = listing.item().getType();
        String name = type.name();

        return switch (category.toUpperCase(Locale.US)) {
            case "ALL" -> true;
            case "BLOCKS" -> type.isBlock();
            case "TOOLS" -> name.endsWith("_AXE") || name.endsWith("_PICKAXE") || name.endsWith("_SHOVEL")
                    || name.endsWith("_HOE") || type == Material.SHEARS || type == Material.FLINT_AND_STEEL
                    || type == Material.FISHING_ROD;
            case "FOOD" -> type.isEdible();
            case "COMBAT" -> name.endsWith("_SWORD") || name.endsWith("_AXE") || name.endsWith("_HELMET")
                    || name.endsWith("_CHESTPLATE") || name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS")
                    || name.endsWith("_BOW") || type == Material.CROSSBOW || type == Material.TRIDENT
                    || type == Material.SHIELD;
            case "POTIONS" -> type == Material.POTION || type == Material.SPLASH_POTION
                    || type == Material.LINGERING_POTION || type == Material.TIPPED_ARROW;
            case "BOOKS" -> type == Material.BOOK || type == Material.WRITABLE_BOOK
                    || type == Material.WRITTEN_BOOK || type == Material.ENCHANTED_BOOK
                    || type == Material.KNOWLEDGE_BOOK;
            case "INGREDIENTS" -> type == Material.BLAZE_POWDER || type == Material.BLAZE_ROD
                    || type == Material.GUNPOWDER || type == Material.STRING || type == Material.SPIDER_EYE
                    || type == Material.FERMENTED_SPIDER_EYE || type == Material.GLISTERING_MELON_SLICE
                    || type == Material.GHAST_TEAR || type == Material.MAGMA_CREAM || type == Material.RABBIT_FOOT
                    || type == Material.PHANTOM_MEMBRANE || type == Material.SUGAR || type == Material.REDSTONE
                    || type == Material.GLOWSTONE_DUST || type == Material.NETHER_WART;
            case "UTILITIES" -> type == Material.ENDER_CHEST || type == Material.CHEST || type == Material.BARREL
                    || type == Material.SHULKER_BOX || name.endsWith("_SHULKER_BOX") || type == Material.ELYTRA
                    || type == Material.LEAD || type == Material.NAME_TAG || type == Material.COMPASS
                    || type == Material.RECOVERY_COMPASS || type == Material.CLOCK;
            default -> false;
        };
    }

    public boolean beginAction(UUID uuid) {
        return activeTransactions.add(uuid);
    }

    public void endAction(UUID uuid) {
        activeTransactions.remove(uuid);
    }

    public boolean isOnClickCooldown(UUID uuid) {
        long lastClick = lastClickTimes.getOrDefault(uuid, 0L);
        return System.currentTimeMillis() - lastClick < getClickCooldownMillis();
    }

    public void updateClickCooldown(UUID uuid) {
        lastClickTimes.put(uuid, System.currentTimeMillis());
    }

    public List<AuctionListing> getActiveListings(AuctionSort sort) {
        List<AuctionListing> listings = new ArrayList<>();
        long now = System.currentTimeMillis();

        try (PreparedStatement ps = connection().prepareStatement(
                "SELECT * FROM auction_listings WHERE status = ? AND expires_at > ?")) {
            ps.setString(1, AuctionListing.Status.ACTIVE.name());
            ps.setLong(2, now);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AuctionListing listing = mapListing(rs);
                    if (listing != null) {
                        listings.add(listing);
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load active auction listings", e);
        }

        listings.sort(resolveComparator(sort));
        return listings;
    }

    public List<AuctionListing> getActiveListingsForSeller(UUID sellerUuid, AuctionSort sort) {
        List<AuctionListing> listings = new ArrayList<>();
        long now = System.currentTimeMillis();

        try (PreparedStatement ps = connection().prepareStatement(
                "SELECT * FROM auction_listings WHERE status = ? AND seller_uuid = ? AND expires_at > ?")) {
            ps.setString(1, AuctionListing.Status.ACTIVE.name());
            ps.setString(2, sellerUuid.toString());
            ps.setLong(3, now);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AuctionListing listing = mapListing(rs);
                    if (listing != null) {
                        listings.add(listing);
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load seller auction listings", e);
        }

        listings.sort(resolveComparator(sort));
        return listings;
    }

    public List<AuctionClaim> getUnclaimedClaims(UUID ownerUuid) {
        List<AuctionClaim> claims = new ArrayList<>();
        try (PreparedStatement ps = connection().prepareStatement(
                "SELECT * FROM auction_claims WHERE owner_uuid = ? AND claimed_at = 0 ORDER BY created_at DESC, id DESC")) {
            ps.setString(1, ownerUuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AuctionClaim claim = mapClaim(rs);
                    if (claim != null) {
                        claims.add(claim);
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load auction claims", e);
        }
        return claims;
    }

    public AuctionListing getListing(long listingId) {
        try (PreparedStatement ps = connection().prepareStatement(
                "SELECT * FROM auction_listings WHERE id = ? LIMIT 1")) {
            ps.setLong(1, listingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapListing(rs);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load listing " + listingId, e);
        }
        return null;
    }

    public AuctionClaim getClaim(long claimId) {
        try (PreparedStatement ps = connection().prepareStatement(
                "SELECT * FROM auction_claims WHERE id = ? LIMIT 1")) {
            ps.setLong(1, claimId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapClaim(rs);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load claim " + claimId, e);
        }
        return null;
    }

    public String describeItem(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return "ᴜɴᴋɴᴏᴡɴ ɪᴛᴇᴍ";
        }

        if (item.hasItemMeta()
                && item.getItemMeta() != null
                && item.getItemMeta().hasDisplayName()) {
            return ColorUtils.strip(ColorUtils.toLegacyString(item.getItemMeta().getDisplayName()));
        }

        return plugin.getWorthManager().prettifyMaterial(item.getType());
    }

    public String formatRemaining(long seconds) {
        if (seconds <= 0) {
            return "Expired";
        }
        return NumberUtils.formatTimeLong(seconds);
    }

    private FileConfiguration config() {
        return plugin.getConfigManager().getAuctionHouse();
    }

    private Connection connection() {
        return plugin.getDatabaseManager().getConnection();
    }

    private int normalizeSize(int configured) {
        int bounded = Math.max(9, Math.min(54, configured));
        int remainder = bounded % 9;
        return remainder == 0 ? bounded : bounded + (9 - remainder);
    }

    private long getListingDurationMillis() {
        long hours = Math.max(1L, config().getLong("SETTINGS.LISTING_DURATION_HOURS", 48L));
        return hours * 60L * 60L * 1000L;
    }

    public int getMaxActiveListings(Player player) {
        int defaultValue = Math.max(1, config().getInt("SETTINGS.MAX_ACTIVE_LISTINGS_DEFAULT", 5));
        if (player == null) {
            return defaultValue;
        }

        int resolvedByPermission = 0;
        ConfigurationSection limitsSection = config().getConfigurationSection("SETTINGS.MAX_ACTIVE_LISTINGS_BY_PERMISSION");
        if (limitsSection != null) {
            for (String permission : limitsSection.getKeys(false)) {
                if (!PermissionUtils.hasExact(player, permission)) {
                    continue;
                }
                resolvedByPermission = Math.max(resolvedByPermission, Math.max(1, limitsSection.getInt(permission, defaultValue)));
            }
        }

        resolvedByPermission = Math.max(resolvedByPermission, PermissionUtils.resolveHighestExactNumberedPermission(
                player, "ultimatedonutsmp.auctionhouse.", MAX_PERMISSION_VALUE));
        resolvedByPermission = Math.max(resolvedByPermission, PermissionUtils.resolveHighestExactNumberedPermission(
                player, "ultimatedonutsmp.auctionhouse.limit.", MAX_PERMISSION_VALUE));

        return resolvedByPermission > 0 ? resolvedByPermission : defaultValue;
    }

    private long getClickCooldownMillis() {
        return Math.max(250L, config().getLong("SETTINGS.CLICK_COOLDOWN_MS", 750L));
    }

    private double getMinPrice() {
        return Math.max(0.01D, config().getDouble("PRICING.MIN_PRICE", 100D));
    }

    private double getMaxPrice() {
        return Math.max(getMinPrice(), config().getDouble("PRICING.MAX_PRICE", 100_000_000D));
    }

    private double getListingFee() {
        return Math.max(0D, config().getDouble("PRICING.LISTING_FEE", 0D));
    }

    private double calculateTax(double price) {
        double taxPercent = Math.max(0D, config().getDouble("PRICING.TAX_PERCENT", 5D));
        return Math.max(0D, price * (taxPercent / 100D));
    }

    public synchronized CreateListingResult createListing(Player seller, double price) {
        return createListing(seller, price, "ALL", (int) (getListingDurationMillis() / 3600000L));
    }

    public synchronized CreateListingResult createListing(Player seller, double price, String category, int durationHours) {
        if (!isEnabled()) {
            return new CreateListingResult(false, CreateFailureReason.DISABLED, null, 0D);
        }

        PlayerData sellerData = getPlayerData(seller);
        if (sellerData == null) {
            return new CreateListingResult(false, CreateFailureReason.NO_PLAYER_DATA, null, 0D);
        }

        ItemStack handItem = seller.getInventory().getItemInMainHand();
        if (handItem == null || handItem.getType().isAir()) {
            return new CreateListingResult(false, CreateFailureReason.NO_ITEM, null, 0D);
        }

        if (price < getMinPrice() || price > getMaxPrice()) {
            return new CreateListingResult(false, CreateFailureReason.INVALID_PRICE, null, 0D);
        }

        if (countActiveListings(seller.getUniqueId()) >= getMaxActiveListings(seller)) {
            return new CreateListingResult(false, CreateFailureReason.MAX_LISTINGS_REACHED, null, 0D);
        }

        ItemStack listedItem = sanitizeListedItem(handItem);
        if (!isListable(listedItem)) {
            return new CreateListingResult(false, CreateFailureReason.INVALID_ITEM, null, 0D);
        }
        CrashProtectionManager.ValidationResult safetyResult = plugin.getCrashProtectionManager()
                .validateForStorage(listedItem, CrashProtectionManager.Context.AUCTION_HOUSE);
        if (!safetyResult.allowed()) {
            plugin.getCrashProtectionManager().logBlockedItem(
                    seller.getName() + "/" + seller.getUniqueId(),
                    listedItem,
                    CrashProtectionManager.Context.AUCTION_HOUSE,
                    safetyResult
            );
            return new CreateListingResult(false, CreateFailureReason.UNSAFE_ITEM, null, 0D, safetyResult);
        }

        double listingFee = getListingFee();
        if (listingFee > 0 && !plugin.getEconomyManager().has(seller, listingFee)) {
            return new CreateListingResult(false, CreateFailureReason.NO_MONEY, null, listingFee);
        }

        long now = System.currentTimeMillis();
        long expiresAt = now + ((long) durationHours * 60L * 60L * 1000L);
        double tax = calculateTax(price);

        long listingId = insertListing(
                seller.getUniqueId(),
                seller.getName(),
                price,
                tax,
                listedItem,
                now,
                expiresAt,
                category
        );
        if (listingId <= 0L) {
            return new CreateListingResult(false, CreateFailureReason.DATABASE_ERROR, null, listingFee);
        }

        seller.getInventory().setItemInMainHand(null);
        if (listingFee > 0) {
            var withdrawResult = plugin.getEconomyManager().withdraw(seller, listingFee, EconomyReason.AUCTION_LISTING_FEE);
            if (!withdrawResult.success()) {
                seller.getInventory().setItemInMainHand(listedItem);
                deleteListing(listingId);
                return new CreateListingResult(false, CreateFailureReason.NO_MONEY, null, listingFee);
            }
            sellerData.addMoneySpent(listingFee);
            plugin.getDatabaseManager().savePlayer(sellerData);
        }
        seller.updateInventory();

        return new CreateListingResult(true, null, getListing(listingId), listingFee);
    }

    public synchronized PurchaseListingResult purchaseListing(Player buyer, long listingId) {
        if (!isEnabled()) {
            return new PurchaseListingResult(false, PurchaseFailureReason.DISABLED, null);
        }

        PlayerData buyerData = getPlayerData(buyer);
        if (buyerData == null) {
            return new PurchaseListingResult(false, PurchaseFailureReason.NO_PLAYER_DATA, null);
        }

        AuctionListing listing = getListing(listingId);
        if (listing == null) {
            return new PurchaseListingResult(false, PurchaseFailureReason.LISTING_NOT_FOUND, null);
        }

        if (!listing.active() || listing.expiresAt() <= System.currentTimeMillis()) {
            if (listing.active() && listing.expiresAt() <= System.currentTimeMillis()) {
                expireListing(listing);
            }
            return new PurchaseListingResult(false, PurchaseFailureReason.NOT_ACTIVE, listing);
        }

        if (listing.sellerUuid().equals(buyer.getUniqueId())) {
            return new PurchaseListingResult(false, PurchaseFailureReason.OWN_LISTING, listing);
        }

        if (!plugin.getEconomyManager().has(buyer, listing.price())) {
            return new PurchaseListingResult(false, PurchaseFailureReason.NO_MONEY, listing);
        }

        if (!canFitItem(buyer, listing.item())) {
            return new PurchaseListingResult(false, PurchaseFailureReason.INVENTORY_FULL, listing);
        }

        boolean sold = markListingSoldAndCreateClaim(
                listing.id(),
                buyer.getUniqueId(),
                listing.sellerUuid(),
                listing.sellerPayout(),
                System.currentTimeMillis()
        );
        if (!sold) {
            return new PurchaseListingResult(false, PurchaseFailureReason.NOT_ACTIVE, getListing(listingId));
        }

        var withdrawResult = plugin.getEconomyManager().withdraw(buyer, listing.price(), EconomyReason.AUCTION_PURCHASE);
        if (!withdrawResult.success()) {
            reopenPurchasedListing(listing.id());
            return new PurchaseListingResult(false, PurchaseFailureReason.NO_MONEY, listing);
        }
        buyerData.addMoneySpent(listing.price());
        plugin.getDatabaseManager().savePlayer(buyerData);

        java.util.Map<Integer, ItemStack> leftovers = buyer.getInventory().addItem(listing.item().clone());
        if (!leftovers.isEmpty()) {
            plugin.getEconomyManager().deposit(buyer, listing.price(), EconomyReason.AUCTION_REFUND);
            buyerData.setMoneySpent(Math.max(0D, buyerData.getMoneySpent() - listing.price()));
            plugin.getDatabaseManager().savePlayer(buyerData);
            reopenPurchasedListing(listing.id());
            return new PurchaseListingResult(false, PurchaseFailureReason.INVENTORY_FULL, listing);
        }

        buyer.updateInventory();
        notifySellerOfSale(listing, buyer);
        return new PurchaseListingResult(true, null, getListing(listing.id()));
    }

    public synchronized CancelListingResult cancelListing(Player owner, long listingId) {
        if (!isEnabled()) {
            return new CancelListingResult(false, CancelFailureReason.DISABLED, null);
        }

        AuctionListing listing = getListing(listingId);
        if (listing == null) {
            return new CancelListingResult(false, CancelFailureReason.LISTING_NOT_FOUND, null);
        }

        if (!listing.sellerUuid().equals(owner.getUniqueId())) {
            return new CancelListingResult(false, CancelFailureReason.NOT_OWNER, listing);
        }

        if (!listing.active()) {
            return new CancelListingResult(false, CancelFailureReason.NOT_ACTIVE, listing);
        }

        boolean success = moveListingToItemClaim(listing, AuctionListing.Status.CANCELLED, System.currentTimeMillis());
        if (!success) {
            return new CancelListingResult(false, CancelFailureReason.DATABASE_ERROR, listing);
        }

        return new CancelListingResult(true, null, getListing(listing.id()));
    }

    public synchronized ClaimResult claim(Player player, long claimId) {
        if (!isEnabled()) {
            return new ClaimResult(false, ClaimFailureReason.DISABLED, null);
        }

        AuctionClaim claim = getClaim(claimId);
        if (claim == null) {
            return new ClaimResult(false, ClaimFailureReason.CLAIM_NOT_FOUND, null);
        }

        if (!claim.ownerUuid().equals(player.getUniqueId())) {
            return new ClaimResult(false, ClaimFailureReason.NOT_OWNER, claim);
        }

        if (claim.claimed()) {
            return new ClaimResult(false, ClaimFailureReason.ALREADY_CLAIMED, claim);
        }

        if (claim.moneyClaim()) {
            PlayerData data = getPlayerData(player);
            if (data == null) {
                return new ClaimResult(false, ClaimFailureReason.NO_PLAYER_DATA, claim);
            }

            boolean updated = markMoneyClaimClaimed(claim.id(), player.getUniqueId(), System.currentTimeMillis());
            if (!updated) {
                return new ClaimResult(false, ClaimFailureReason.DATABASE_ERROR, claim);
            }

            var depositResult = plugin.getEconomyManager().deposit(player, claim.moneyAmount(), EconomyReason.AUCTION_CLAIM);
            if (!depositResult.success()) {
                reopenClaim(claim.id());
                return new ClaimResult(false, ClaimFailureReason.NO_PLAYER_DATA, claim);
            }
            data.addMoneyMade(claim.moneyAmount());
            plugin.getDatabaseManager().savePlayer(data);
            return new ClaimResult(true, null, getClaim(claim.id()));
        }

        if (!canFitItem(player, claim.item())) {
            return new ClaimResult(false, ClaimFailureReason.INVENTORY_FULL, claim);
        }

        boolean claimed = markItemClaimClaimed(claim.id(), player.getUniqueId(), System.currentTimeMillis());
        if (!claimed) {
            return new ClaimResult(false, ClaimFailureReason.DATABASE_ERROR, claim);
        }

        java.util.Map<Integer, ItemStack> leftovers = player.getInventory().addItem(claim.item().clone());
        if (!leftovers.isEmpty()) {
            reopenClaim(claim.id());
            return new ClaimResult(false, ClaimFailureReason.INVENTORY_FULL, claim);
        }

        player.updateInventory();
        return new ClaimResult(true, null, getClaim(claim.id()));
    }

    public synchronized int expireListings() {
        int expired = 0;
        long now = System.currentTimeMillis();
        List<AuctionListing> listings = new ArrayList<>();

        try (PreparedStatement ps = connection().prepareStatement(
                "SELECT * FROM auction_listings WHERE status = ? AND expires_at <= ?")) {
            ps.setString(1, AuctionListing.Status.ACTIVE.name());
            ps.setLong(2, now);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AuctionListing listing = mapListing(rs);
                    if (listing != null) {
                        listings.add(listing);
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to scan expired auction listings", e);
        }

        for (AuctionListing listing : listings) {
            if (expireListing(listing)) {
                expired++;
            }
        }
        return expired;
    }

    private ItemStack sanitizeListedItem(ItemStack rawItem) {
        ItemStack sanitized = rawItem.clone();
        sanitized = plugin.getWorthManager().stripWorthDisplay(sanitized);
        sanitized.setAmount(rawItem.getAmount());
        return sanitized;
    }

    private boolean isListable(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return false;
        }

        Set<Material> blockedMaterials = EnumSet.noneOf(Material.class);
        for (String rawMaterial : config().getStringList("RESTRICTIONS.BLOCKED_MATERIALS")) {
            try {
                blockedMaterials.add(Material.valueOf(rawMaterial.trim().toUpperCase(Locale.US)));
            } catch (IllegalArgumentException ignored) {
            }
        }
        if (blockedMaterials.contains(item.getType())) {
            return false;
        }

        if (item.hasItemMeta()
                && item.getItemMeta() != null
                && item.getItemMeta().hasLore()) {
            List<String> blockedLoreTerms = config().getStringList("RESTRICTIONS.BLOCKED_IF_HAS_LORE_CONTAINS");
            List<String> lore = item.getItemMeta().getLore();
            if (lore != null) {
                for (String line : lore) {
                    String plainLine = ColorUtils.strip(ColorUtils.toLegacyString(line)).toLowerCase(Locale.US);
                    for (String blocked : blockedLoreTerms) {
                        if (blocked != null && !blocked.isBlank()
                                && plainLine.contains(blocked.toLowerCase(Locale.US))) {
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    public int countActiveListings(UUID sellerUuid) {
        try (PreparedStatement ps = connection().prepareStatement(
                "SELECT COUNT(*) FROM auction_listings WHERE seller_uuid = ? AND status = ? AND expires_at > ?")) {
            ps.setString(1, sellerUuid.toString());
            ps.setString(2, AuctionListing.Status.ACTIVE.name());
            ps.setLong(3, System.currentTimeMillis());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to count active auction listings", e);
        }
        return 0;
    }

    private long insertListing(
            UUID sellerUuid,
            String sellerName,
            double price,
            double tax,
            ItemStack item,
            long createdAt,
            long expiresAt,
            String category
    ) {
        try (PreparedStatement ps = connection().prepareStatement(
                "INSERT INTO auction_listings " +
                        "(seller_uuid, seller_name, buyer_uuid, status, price, tax, item_data, created_at, expires_at, sold_at, cancelled_at, expired_at, category) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS
        )) {
            ps.setString(1, sellerUuid.toString());
            ps.setString(2, sellerName);
            ps.setNull(3, Types.VARCHAR);
            ps.setString(4, AuctionListing.Status.ACTIVE.name());
            ps.setDouble(5, price);
            ps.setDouble(6, tax);
            ps.setString(7, serializeItem(item));
            ps.setLong(8, createdAt);
            ps.setLong(9, expiresAt);
            ps.setLong(10, 0L);
            ps.setLong(11, 0L);
            ps.setLong(12, 0L);
            ps.setString(13, category);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to insert auction listing", e);
        }
        return -1L;
    }

    private boolean markListingSoldAndCreateClaim(
            long listingId,
            UUID buyerUuid,
            UUID sellerUuid,
            double sellerPayout,
            long soldAt
    ) {
        try {
            Connection conn = connection();
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                int updatedRows;
                try (PreparedStatement updateListing = conn.prepareStatement(
                        "UPDATE auction_listings SET status = ?, buyer_uuid = ?, sold_at = ? " +
                                "WHERE id = ? AND status = ?")) {
                    updateListing.setString(1, AuctionListing.Status.SOLD.name());
                    updateListing.setString(2, buyerUuid.toString());
                    updateListing.setLong(3, soldAt);
                    updateListing.setLong(4, listingId);
                    updateListing.setString(5, AuctionListing.Status.ACTIVE.name());
                    updatedRows = updateListing.executeUpdate();
                }

                if (updatedRows != 1) {
                    conn.rollback();
                    conn.setAutoCommit(originalAutoCommit);
                    return false;
                }

                try (PreparedStatement insertClaim = conn.prepareStatement(
                        "INSERT INTO auction_claims " +
                                "(owner_uuid, claim_type, source_listing_id, money_amount, item_data, created_at, claimed_at) " +
                                "VALUES (?,?,?,?,?,?,?)")) {
                    insertClaim.setString(1, sellerUuid.toString());
                    insertClaim.setString(2, AuctionClaim.ClaimType.MONEY.name());
                    insertClaim.setLong(3, listingId);
                    insertClaim.setDouble(4, sellerPayout);
                    insertClaim.setNull(5, Types.VARCHAR);
                    insertClaim.setLong(6, soldAt);
                    insertClaim.setLong(7, 0L);
                    insertClaim.executeUpdate();
                }

                conn.commit();
                conn.setAutoCommit(originalAutoCommit);
                return true;
            } catch (SQLException e) {
                conn.rollback();
                conn.setAutoCommit(originalAutoCommit);
                throw e;
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to mark listing as sold", e);
            return false;
        }
    }

    private boolean moveListingToItemClaim(AuctionListing listing, AuctionListing.Status nextStatus, long timestamp) {
        try {
            Connection conn = connection();
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                String updateSql = switch (nextStatus) {
                    case CANCELLED -> "UPDATE auction_listings SET status = ?, cancelled_at = ? WHERE id = ? AND status = ?";
                    case EXPIRED -> "UPDATE auction_listings SET status = ?, expired_at = ? WHERE id = ? AND status = ?";
                    default -> throw new SQLException("Unsupported listing transition: " + nextStatus);
                };

                int updatedRows;
                try (PreparedStatement updateListing = conn.prepareStatement(updateSql)) {
                    updateListing.setString(1, nextStatus.name());
                    updateListing.setLong(2, timestamp);
                    updateListing.setLong(3, listing.id());
                    updateListing.setString(4, AuctionListing.Status.ACTIVE.name());
                    updatedRows = updateListing.executeUpdate();
                }

                if (updatedRows != 1) {
                    conn.rollback();
                    conn.setAutoCommit(originalAutoCommit);
                    return false;
                }

                try (PreparedStatement insertClaim = conn.prepareStatement(
                        "INSERT INTO auction_claims " +
                                "(owner_uuid, claim_type, source_listing_id, money_amount, item_data, created_at, claimed_at) " +
                                "VALUES (?,?,?,?,?,?,?)")) {
                    insertClaim.setString(1, listing.sellerUuid().toString());
                    insertClaim.setString(2, AuctionClaim.ClaimType.ITEM.name());
                    insertClaim.setLong(3, listing.id());
                    insertClaim.setDouble(4, 0D);
                    insertClaim.setString(5, serializeItem(listing.item()));
                    insertClaim.setLong(6, timestamp);
                    insertClaim.setLong(7, 0L);
                    insertClaim.executeUpdate();
                }

                conn.commit();
                conn.setAutoCommit(originalAutoCommit);
                return true;
            } catch (SQLException e) {
                conn.rollback();
                conn.setAutoCommit(originalAutoCommit);
                throw e;
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to move listing to claim queue", e);
            return false;
        }
    }

    private boolean markMoneyClaimClaimed(long claimId, UUID ownerUuid, long claimedAt) {
        try (PreparedStatement ps = connection().prepareStatement(
                "UPDATE auction_claims SET claimed_at = ? WHERE id = ? AND owner_uuid = ? AND claimed_at = 0")) {
            ps.setLong(1, claimedAt);
            ps.setLong(2, claimId);
            ps.setString(3, ownerUuid.toString());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to mark money claim as claimed", e);
            return false;
        }
    }

    private boolean markItemClaimClaimed(long claimId, UUID ownerUuid, long claimedAt) {
        try (PreparedStatement ps = connection().prepareStatement(
                "UPDATE auction_claims SET claimed_at = ? WHERE id = ? AND owner_uuid = ? AND claim_type = ? AND claimed_at = 0")) {
            ps.setLong(1, claimedAt);
            ps.setLong(2, claimId);
            ps.setString(3, ownerUuid.toString());
            ps.setString(4, AuctionClaim.ClaimType.ITEM.name());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to mark item claim as claimed", e);
            return false;
        }
    }

    private void reopenPurchasedListing(long listingId) {
        try {
            Connection conn = connection();
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement updateListing = conn.prepareStatement(
                        "UPDATE auction_listings SET status = ?, buyer_uuid = NULL, sold_at = 0 WHERE id = ? AND status = ?")) {
                    updateListing.setString(1, AuctionListing.Status.ACTIVE.name());
                    updateListing.setLong(2, listingId);
                    updateListing.setString(3, AuctionListing.Status.SOLD.name());
                    updateListing.executeUpdate();
                }

                try (PreparedStatement deleteClaims = conn.prepareStatement(
                        "DELETE FROM auction_claims WHERE source_listing_id = ? AND claim_type = ? AND claimed_at = 0")) {
                    deleteClaims.setLong(1, listingId);
                    deleteClaims.setString(2, AuctionClaim.ClaimType.MONEY.name());
                    deleteClaims.executeUpdate();
                }

                conn.commit();
                conn.setAutoCommit(originalAutoCommit);
            } catch (SQLException e) {
                conn.rollback();
                conn.setAutoCommit(originalAutoCommit);
                throw e;
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to reopen sold listing " + listingId, e);
        }
    }

    private void deleteListing(long listingId) {
        try (PreparedStatement ps = connection().prepareStatement(
                "DELETE FROM auction_listings WHERE id = ?")) {
            ps.setLong(1, listingId);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to delete auction listing " + listingId, e);
        }
    }

    private void reopenClaim(long claimId) {
        try (PreparedStatement ps = connection().prepareStatement(
                "UPDATE auction_claims SET claimed_at = 0 WHERE id = ?")) {
            ps.setLong(1, claimId);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to reopen auction claim " + claimId, e);
        }
    }

    private boolean expireListing(AuctionListing listing) {
        return moveListingToItemClaim(listing, AuctionListing.Status.EXPIRED, System.currentTimeMillis());
    }

    private Comparator<AuctionListing> resolveComparator(AuctionSort sort) {
        AuctionSort effectiveSort = sort == null ? AuctionSort.NEWEST : sort;
        return switch (effectiveSort) {
            case OLDEST -> Comparator.comparingLong(AuctionListing::createdAt)
                    .thenComparingLong(AuctionListing::id);
            case PRICE_LOWEST -> Comparator.comparingDouble(AuctionListing::price)
                    .thenComparing(Comparator.comparingLong(AuctionListing::createdAt).reversed());
            case PRICE_HIGHEST -> Comparator.comparingDouble(AuctionListing::price).reversed()
                    .thenComparing(Comparator.comparingLong(AuctionListing::createdAt).reversed());
            case EXPIRING_SOON -> Comparator.comparingLong(AuctionListing::expiresAt)
                    .thenComparing(Comparator.comparingLong(AuctionListing::createdAt).reversed());
            case NEWEST -> Comparator.comparingLong(AuctionListing::createdAt).reversed()
                    .thenComparing(Comparator.comparingLong(AuctionListing::id).reversed());
        };
    }

    private PlayerData getPlayerData(Player player) {
        PlayerData data = plugin.getPlayerDataManager().get(player);
        if (data == null) {
            data = plugin.getPlayerDataManager().loadOrCreate(player);
        }
        return data;
    }

    private boolean canFitItem(Player player, ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return true;
        }

        int remaining = item.getAmount();
        int maxStack = item.getMaxStackSize();
        ItemStack comparison = item.clone();
        comparison.setAmount(1);

        for (ItemStack current : player.getInventory().getStorageContents()) {
            if (current == null || current.getType().isAir()) {
                remaining -= maxStack;
            } else if (current.isSimilar(comparison) && current.getAmount() < current.getMaxStackSize()) {
                remaining -= Math.max(0, current.getMaxStackSize() - current.getAmount());
            }

            if (remaining <= 0) {
                return true;
            }
        }
        return remaining <= 0;
    }

    private void notifySellerOfSale(AuctionListing listing, Player buyer) {
        Player seller = plugin.getServer().getPlayer(listing.sellerUuid());
        if (seller == null) {
            return;
        }

        if (plugin.getFriendsManager() != null && plugin.getFriendsManager().isTransactionMessageBlocked(buyer.getUniqueId(), seller.getUniqueId())) {
            return;
        }

        String message = plugin.getConfigManager().getMessage(
                "AUCTION_HOUSE.ITEM_SOLD",
                "{buyer}", plugin.getHideManager().publicName(buyer),
                "{item}", describeItem(listing.item()),
                "{price}", NumberUtils.format(listing.price()),
                "{price_formatted}", plugin.getCurrencyManager().formatMoney(listing.price()),
                "{payout}", NumberUtils.format(listing.sellerPayout()),
                "{payout_formatted}", plugin.getCurrencyManager().formatMoney(listing.sellerPayout())
        );
        seller.sendMessage(ColorUtils.toComponent(message));
    }

    private void ensureTables() {
        try (Statement statement = connection().createStatement()) {
            plugin.getDatabaseManager().executeSchema(statement, """
                    CREATE TABLE IF NOT EXISTS auction_listings (
                      id INTEGER PRIMARY KEY AUTOINCREMENT,
                      seller_uuid TEXT NOT NULL,
                      seller_name TEXT NOT NULL,
                      buyer_uuid TEXT,
                      status TEXT NOT NULL,
                      price REAL NOT NULL,
                      tax REAL DEFAULT 0,
                      item_data TEXT NOT NULL,
                      created_at INTEGER NOT NULL,
                      expires_at INTEGER NOT NULL,
                      sold_at INTEGER DEFAULT 0,
                      cancelled_at INTEGER DEFAULT 0,
                      expired_at INTEGER DEFAULT 0,
                      category TEXT DEFAULT 'ALL'
                    )
                    """);
            try {
                plugin.getDatabaseManager().executeSchema(statement, "ALTER TABLE auction_listings ADD COLUMN category TEXT DEFAULT 'ALL'");
            } catch (Exception ignored) {
            }
            plugin.getDatabaseManager().executeSchema(statement, """
                    CREATE TABLE IF NOT EXISTS player_auction_preferences (
                      player_uuid TEXT PRIMARY KEY,
                      fast_buy_enabled INTEGER DEFAULT 0,
                      fast_sell_enabled INTEGER DEFAULT 0,
                      last_duration_hours INTEGER DEFAULT 48,
                      last_category TEXT DEFAULT 'ALL',
                      last_price REAL DEFAULT 0.0
                    )
                    """);
            plugin.getDatabaseManager().executeSchema(statement, """
                    CREATE TABLE IF NOT EXISTS auction_claims (
                      id INTEGER PRIMARY KEY AUTOINCREMENT,
                      owner_uuid TEXT NOT NULL,
                      claim_type TEXT NOT NULL,
                      source_listing_id INTEGER DEFAULT 0,
                      money_amount REAL DEFAULT 0,
                      item_data TEXT,
                      created_at INTEGER NOT NULL,
                      claimed_at INTEGER DEFAULT 0
                    )
                    """);
            plugin.getDatabaseManager().executeSchema(statement, "CREATE INDEX IF NOT EXISTS idx_auction_listings_status_expires ON auction_listings(status, expires_at)");
            plugin.getDatabaseManager().executeSchema(statement, "CREATE INDEX IF NOT EXISTS idx_auction_listings_seller_status ON auction_listings(seller_uuid, status)");
            plugin.getDatabaseManager().executeSchema(statement, "CREATE INDEX IF NOT EXISTS idx_auction_claims_owner_claimed ON auction_claims(owner_uuid, claimed_at)");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to create auction house tables", e);
        }
    }

    private AuctionListing mapListing(ResultSet rs) throws SQLException {
        ItemStack item = deserializeItem(rs.getString("item_data"));
        if (item == null) {
            return null;
        }
        CrashProtectionManager.ValidationResult safetyResult = plugin.getCrashProtectionManager()
                .validateForStorage(item, CrashProtectionManager.Context.DATABASE_LOAD);
        if (!safetyResult.allowed()) {
            plugin.getCrashProtectionManager().logBlockedItem(
                    "auction listing #" + rs.getLong("id"),
                    item,
                    CrashProtectionManager.Context.DATABASE_LOAD,
                    safetyResult
            );
            return null;
        }

        String buyerUuid = rs.getString("buyer_uuid");
        String category = "ALL";
        try {
            category = rs.getString("category");
            if (category == null) {
                category = "ALL";
            }
        } catch (SQLException ignored) {
        }
        return new AuctionListing(
                rs.getLong("id"),
                UUID.fromString(rs.getString("seller_uuid")),
                rs.getString("seller_name"),
                buyerUuid == null || buyerUuid.isBlank() ? null : UUID.fromString(buyerUuid),
                AuctionListing.Status.fromDatabase(rs.getString("status")),
                rs.getDouble("price"),
                rs.getDouble("tax"),
                item,
                rs.getLong("created_at"),
                rs.getLong("expires_at"),
                rs.getLong("sold_at"),
                rs.getLong("cancelled_at"),
                rs.getLong("expired_at"),
                category
        );
    }

    private AuctionClaim mapClaim(ResultSet rs) throws SQLException {
        AuctionClaim.ClaimType claimType = AuctionClaim.ClaimType.fromDatabase(rs.getString("claim_type"));
        ItemStack item = deserializeItem(rs.getString("item_data"));
        if (claimType == AuctionClaim.ClaimType.ITEM && item == null) {
            return null;
        }
        if (item != null) {
            CrashProtectionManager.ValidationResult safetyResult = plugin.getCrashProtectionManager()
                    .validateForStorage(item, CrashProtectionManager.Context.DATABASE_LOAD);
            if (!safetyResult.allowed()) {
                plugin.getCrashProtectionManager().logBlockedItem(
                        "auction claim #" + rs.getLong("id"),
                        item,
                        CrashProtectionManager.Context.DATABASE_LOAD,
                        safetyResult
                );
                return null;
            }
        }
        return new AuctionClaim(
                rs.getLong("id"),
                UUID.fromString(rs.getString("owner_uuid")),
                claimType,
                rs.getLong("source_listing_id"),
                rs.getDouble("money_amount"),
                item,
                rs.getLong("created_at"),
                rs.getLong("claimed_at")
        );
    }

    private String serializeItem(ItemStack item) {
        try {
            return ItemSerializationUtils.serialize(item);
        } catch (java.io.IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to serialize auction item", e);
            return "";
        }
    }

    private ItemStack deserializeItem(String encoded) {
        if (encoded == null || encoded.isBlank()) {
            return null;
        }

        try {
            return ItemSerializationUtils.deserialize(encoded);
        } catch (IllegalArgumentException | java.io.IOException | ClassNotFoundException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to deserialize auction item", e);
            return null;
        }
    }

    private void validateConfiguration() {
        if (!isEnabled()) {
            return;
        }

        if (getMinPrice() > getMaxPrice()) {
            plugin.getLogger().warning("auction-house.yml has PRICING.MIN_PRICE greater than PRICING.MAX_PRICE.");
        }

        for (String rawMaterial : config().getStringList("RESTRICTIONS.BLOCKED_MATERIALS")) {
            if (rawMaterial == null || rawMaterial.isBlank()) {
                continue;
            }
            try {
                Material.valueOf(rawMaterial.trim().toUpperCase(Locale.US));
            } catch (IllegalArgumentException exception) {
                plugin.getLogger().warning("Invalid Auction House blocked material: " + rawMaterial);
            }
        }
    }
}
