package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.AuctionBrowseRequest;
import com.bx.ultimateDonutSmp.models.AuctionBrowseEngine;
import com.bx.ultimateDonutSmp.models.AuctionCategory;
import com.bx.ultimateDonutSmp.models.AuctionClaim;
import com.bx.ultimateDonutSmp.models.AuctionListing;
import com.bx.ultimateDonutSmp.models.AuctionPage;
import com.bx.ultimateDonutSmp.models.EconomyReason;
import com.bx.ultimateDonutSmp.models.PlayerAuctionSession;
import com.bx.ultimateDonutSmp.models.PlayerPreference;
import com.bx.ultimateDonutSmp.storage.AuctionHouseRepository;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import com.bx.ultimateDonutSmp.utils.PermissionUtils;
import com.bx.ultimateDonutSmp.utils.PlayerSettingUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;

public final class AuctionHouseManager {

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
                return valueOf(rawValue.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ignored) {
                return NEWEST;
            }
        }
    }

    public enum CreateFailureReason {
        DISABLED,
        NO_PERMISSION,
        NO_PLAYER_DATA,
        NO_ITEM,
        INVALID_ITEM,
        UNSAFE_ITEM,
        INVALID_PRICE,
        INVALID_DURATION,
        NO_MONEY,
        MAX_LISTINGS_REACHED,
        DATABASE_ERROR
    }

    public enum PurchaseFailureReason {
        DISABLED,
        NO_PERMISSION,
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
        NO_PERMISSION,
        LISTING_NOT_FOUND,
        NOT_OWNER,
        NOT_ACTIVE,
        DATABASE_ERROR
    }

    public enum ClaimFailureReason {
        DISABLED,
        NO_PERMISSION,
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
            AuctionListing listing,
            boolean deliveryPending
    ) {
        public PurchaseListingResult(boolean success, PurchaseFailureReason reason, AuctionListing listing) {
            this(success, reason, listing, false);
        }
    }

    public record CancelListingResult(
            boolean success,
            CancelFailureReason reason,
            AuctionListing listing
    ) {
    }

    public record ClaimResult(
            boolean success,
            ClaimFailureReason reason,
            AuctionClaim claim
    ) {
    }

    private final UltimateDonutSmp plugin;
    private final AuctionHouseRepository repository;
    private final AtomicReference<List<AuctionListing>> listingCache = new AtomicReference<>(List.of());
    private final AtomicReference<List<AuctionClaim>> claimCache = new AtomicReference<>(List.of());
    private final Map<Long, AuctionListing> listingsById = new ConcurrentHashMap<>();
    private final Map<Long, AuctionClaim> claimsById = new ConcurrentHashMap<>();
    private final Map<UUID, PlayerPreference> preferenceCache = new ConcurrentHashMap<>();
    private final Map<UUID, PlayerAuctionSession> sessions = new ConcurrentHashMap<>();
    private final Set<UUID> activeTransactions = ConcurrentHashMap.newKeySet();
    private final Set<UUID> navigating = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Long> lastClickTimes = new ConcurrentHashMap<>();

    public AuctionHouseManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
        this.repository = new AuctionHouseRepository(plugin);
        repository.initialize().join();
        refreshCache().join();
        validateConfiguration();
    }

    public void reload() {
        validateConfiguration();
        preferenceCache.clear();
        refreshCache();
    }

    public void shutdown() {
        repository.shutdown();
        activeTransactions.clear();
        sessions.clear();
        navigating.clear();
    }

    public void prepareForServerWipe() {
        activeTransactions.clear();
        lastClickTimes.clear();
        sessions.clear();
        navigating.clear();
    }

    public void cleanupPlayer(UUID playerId) {
        activeTransactions.remove(playerId);
        lastClickTimes.remove(playerId);
        sessions.remove(playerId);
        navigating.remove(playerId);
    }

    public boolean isEnabled() {
        return plugin.getFeatureManager().isEnabled(FeatureManager.Feature.AUCTION_HOUSE)
                && config().getBoolean("SETTINGS.ENABLED", true);
    }

    public boolean isClaimsEnabled() {
        return config().getBoolean("CLAIMS.ENABLED", true);
    }

    public String getBrowseTitle() {
        return config().getString("GUI.BROWSE.TITLE", "&8ᴀᴜᴄᴛɪᴏɴ ʜᴏᴜѕᴇ");
    }

    public int getBrowseSize() {
        return 54;
    }

    public int getBrowseItemsPerPage() {
        return Math.max(1, Math.min(45, config().getInt("GUI.BROWSE.ITEMS_PER_PAGE", 45)));
    }

    public String getCategoryDisplayName(AuctionCategory category) {
        AuctionCategory effective = category == null ? AuctionCategory.ALL : category;
        return config().getString(
                "GUI.CATEGORIES." + effective.name() + ".NAME",
                effective.defaultDisplayName()
        );
    }

    public Material getCategoryIcon(AuctionCategory category) {
        AuctionCategory effective = category == null ? AuctionCategory.ALL : category;
        String configured = config().getString(
                "GUI.CATEGORIES." + effective.name() + ".MATERIAL",
                effective.defaultIcon().name()
        );
        try {
            return Material.valueOf(configured.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return effective.defaultIcon();
        }
    }

    public String getSortDisplayName(AuctionSort sort) {
        AuctionSort effective = sort == null ? getDefaultSort() : sort;
        String fallback = effective.name().toLowerCase(Locale.ROOT).replace('_', ' ');
        fallback = Character.toUpperCase(fallback.charAt(0)) + fallback.substring(1);
        return config().getString("GUI.SORT_NAMES." + effective.name() + ".NAME", fallback);
    }

    public String getText(String key, String fallback) {
        return config().getString("GUI.TEXT." + key + ".NAME", fallback);
    }

    public String getMyListingsTitle() {
        return config().getString("GUI.MY_LISTINGS.TITLE", "&8ʏᴏᴜʀ ɪᴛᴇᴍѕ");
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
        List<AuctionSort> sorts = config().getStringList("SORTING.ALLOWED").stream()
                .map(AuctionSort::fromConfig)
                .distinct()
                .collect(Collectors.toCollection(ArrayList::new));
        if (sorts.isEmpty()) {
            sorts.addAll(List.of(AuctionSort.NEWEST, AuctionSort.PRICE_LOWEST, AuctionSort.PRICE_HIGHEST));
        }
        return List.copyOf(sorts);
    }

    public List<Integer> getAllowedDurations() {
        List<Integer> values = config().getIntegerList("SETTINGS.LISTING_DURATIONS_HOURS").stream()
                .filter(value -> value != null && value > 0)
                .distinct()
                .sorted()
                .toList();
        return values.isEmpty() ? List.of(6, 12, 24, 48, 72, 168) : values;
    }

    public PlayerAuctionSession session(UUID playerId) {
        return sessions.computeIfAbsent(playerId, id -> new PlayerAuctionSession(
                id,
                new AuctionBrowseRequest(1, getDefaultSort(), AuctionCategory.ALL, "")
        ));
    }

    public void updateSession(UUID playerId, AuctionBrowseRequest request) {
        session(playerId).request(request);
    }

    public String getSearchQuery(UUID playerId) {
        return session(playerId).request().search();
    }

    public void setSearchQuery(UUID playerId, String query) {
        PlayerAuctionSession session = session(playerId);
        session.request(session.request().withSearch(query));
    }

    public void clearSearchQuery(UUID playerId) {
        setSearchQuery(playerId, "");
    }

    public void startNavigating(UUID playerId) {
        navigating.add(playerId);
    }

    public boolean stopNavigating(UUID playerId) {
        return navigating.remove(playerId);
    }

    public boolean beginAction(UUID playerId) {
        return activeTransactions.add(playerId);
    }

    public void endAction(UUID playerId) {
        activeTransactions.remove(playerId);
    }

    public boolean isOnClickCooldown(UUID playerId) {
        return System.currentTimeMillis() - lastClickTimes.getOrDefault(playerId, 0L) < getClickCooldownMillis();
    }

    public void updateClickCooldown(UUID playerId) {
        lastClickTimes.put(playerId, System.currentTimeMillis());
    }

    public CompletableFuture<Void> refreshCache() {
        return repository.loadSnapshot().thenAccept(snapshot -> {
            listingCache.set(snapshot.listings());
            claimCache.set(snapshot.claims());
            listingsById.clear();
            snapshot.listings().forEach(listing -> listingsById.put(listing.id(), listing));
            claimsById.clear();
            snapshot.claims().forEach(claim -> claimsById.put(claim.id(), claim));
        }).exceptionally(throwable -> {
            plugin.getLogger().log(Level.WARNING, "Failed to refresh Auction House cache", unwrap(throwable));
            return null;
        });
    }

    public AuctionPage browse(AuctionBrowseRequest request) {
        return AuctionBrowseEngine.page(
                listingCache.get(),
                request,
                getBrowseItemsPerPage(),
                System.currentTimeMillis(),
                listing -> describeItem(listing.item())
        );
    }

    public List<AuctionListing> getActiveListings(AuctionSort sort) {
        return getActiveListings(sort, "ALL", "");
    }

    public List<AuctionListing> getActiveListings(AuctionSort sort, String category, String search) {
        AuctionBrowseRequest request = new AuctionBrowseRequest(
                1,
                sort,
                AuctionCategory.from(category),
                search
        );
        return AuctionBrowseEngine.filter(
                listingCache.get(),
                request,
                System.currentTimeMillis(),
                listing -> describeItem(listing.item())
        );
    }

    public List<AuctionListing> getActiveListingsForSeller(UUID sellerId, AuctionSort sort) {
        long now = System.currentTimeMillis();
        return listingCache.get().stream()
                .filter(listing -> listing.sellerUuid().equals(sellerId))
                .filter(listing -> listing.active() && listing.expiresAt() > now)
                .sorted(AuctionBrowseEngine.comparator(sort))
                .toList();
    }

    public List<AuctionListing> getPlayerListings(UUID sellerId) {
        return listingCache.get().stream()
                .filter(listing -> listing.sellerUuid().equals(sellerId))
                .sorted(Comparator.comparingLong(AuctionListing::createdAt).reversed()
                        .thenComparing(Comparator.comparingLong(AuctionListing::id).reversed()))
                .toList();
    }

    public List<AuctionClaim> getUnclaimedClaims(UUID ownerId) {
        return claimCache.get().stream()
                .filter(claim -> claim.ownerUuid().equals(ownerId) && !claim.claimed())
                .toList();
    }

    public AuctionListing getListing(long listingId) {
        return listingsById.get(listingId);
    }

    public AuctionClaim getClaim(long claimId) {
        return claimsById.get(claimId);
    }

    public int countActiveListings(UUID sellerId) {
        return (int) getActiveListingsForSeller(sellerId, AuctionSort.NEWEST).size();
    }

    public CompletableFuture<PlayerPreference> getPreferenceAsync(UUID playerId) {
        PlayerPreference cached = preferenceCache.get(playerId);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }
        return repository.loadPreference(playerId).thenApply(preference -> {
            preferenceCache.put(playerId, preference);
            return preference;
        });
    }

    public PlayerPreference getPreference(UUID playerId) {
        PlayerPreference cached = preferenceCache.get(playerId);
        if (cached != null) {
            return cached;
        }
        repository.loadPreference(playerId).thenAccept(preference -> preferenceCache.put(playerId, preference));
        return new PlayerPreference(playerId);
    }

    public CompletableFuture<Void> savePreference(PlayerPreference preference) {
        preferenceCache.put(preference.playerId(), preference);
        return repository.savePreference(preference).exceptionally(throwable -> {
            plugin.getLogger().log(Level.WARNING, "Failed to save Auction House preference", unwrap(throwable));
            return null;
        });
    }

    public CompletableFuture<CreateListingResult> createListing(Player seller, double price) {
        return createListing(
                seller,
                price,
                AuctionCategory.ALL.name(),
                defaultDurationHours()
        );
    }

    public CompletableFuture<CreateListingResult> createListing(
            Player seller,
            double price,
            String category,
            int durationHours
    ) {
        ItemStack hand = seller.getInventory().getItemInMainHand();
        if (hand == null || hand.getType().isAir()) {
            return CompletableFuture.completedFuture(
                    new CreateListingResult(false, CreateFailureReason.NO_ITEM, null, 0D)
            );
        }
        ItemStack escrow = hand.clone();
        seller.getInventory().setItemInMainHand(null);
        seller.updateInventory();
        return createListingFromItem(seller, escrow, price, durationHours, AuctionCategory.from(category));
    }

    public CompletableFuture<CreateListingResult> createListingFromItem(
            Player seller,
            ItemStack escrowItem,
            double price,
            int durationHours,
            AuctionCategory category
    ) {
        if (!beginAction(seller.getUniqueId())) {
            restoreEscrow(seller, escrowItem);
            return CompletableFuture.completedFuture(
                    new CreateListingResult(false, CreateFailureReason.DATABASE_ERROR, null, 0D)
            );
        }

        CreateListingResult validation = validateListing(seller, escrowItem, price, durationHours);
        if (validation != null) {
            endAction(seller.getUniqueId());
            restoreEscrow(seller, escrowItem);
            return CompletableFuture.completedFuture(validation);
        }

        ItemStack storedItem = sanitizeListedItem(escrowItem);
        double listingFee = getListingFee();
        if (listingFee > 0D) {
            var withdrawal = plugin.getEconomyManager().withdraw(
                    seller,
                    listingFee,
                    EconomyReason.AUCTION_LISTING_FEE
            );
            if (!withdrawal.success()) {
                endAction(seller.getUniqueId());
                restoreEscrow(seller, escrowItem);
                return CompletableFuture.completedFuture(
                        new CreateListingResult(false, CreateFailureReason.NO_MONEY, null, listingFee)
                );
            }
        }

        long now = System.currentTimeMillis();
        long expiresAt = now + TimeUnitHours.toMillis(durationHours);
        CompletableFuture<CreateListingResult> result = new CompletableFuture<>();
        repository.createListing(
                seller.getUniqueId(),
                seller.getName(),
                price,
                calculateTax(price),
                storedItem,
                now,
                expiresAt,
                category.name(),
                getMaxActiveListings(seller)
        ).whenComplete((created, throwable) -> plugin.getSpigotScheduler().runEntity(seller, () -> {
            try {
                if (throwable != null || created == null || !created.created()) {
                    if (listingFee > 0D) {
                        plugin.getEconomyManager().deposit(seller, listingFee, EconomyReason.AUCTION_REFUND);
                    }
                    restoreEscrow(seller, escrowItem);
                    CreateFailureReason reason = created != null && created.limitReached()
                            ? CreateFailureReason.MAX_LISTINGS_REACHED
                            : CreateFailureReason.DATABASE_ERROR;
                    result.complete(new CreateListingResult(false, reason, null, listingFee));
                    return;
                }
                refreshCache().thenRun(() -> {
                    plugin.getPlayerLogsManager().log(
                            seller.getUniqueId(),
                            seller.getName(),
                            "Auctions",
                            "AUCTION_LIST",
                            "Listed " + describeItem(created.listing().item()) + " for " + plugin.getCurrencyManager().formatMoney(price)
                    );
                    result.complete(new CreateListingResult(true, null, created.listing(), listingFee));
                });
            } finally {
                endAction(seller.getUniqueId());
            }
        }));
        return result;
    }

    public CompletableFuture<PurchaseListingResult> purchaseListing(Player buyer, long listingId) {
        if (!isEnabled()) {
            return CompletableFuture.completedFuture(
                    new PurchaseListingResult(false, PurchaseFailureReason.DISABLED, null)
            );
        }
        if (!hasPermission(buyer, "buy")) {
            return CompletableFuture.completedFuture(
                    new PurchaseListingResult(false, PurchaseFailureReason.NO_PERMISSION, null)
            );
        }
        AuctionListing listing = getListing(listingId);
        if (listing == null) {
            return CompletableFuture.completedFuture(
                    new PurchaseListingResult(false, PurchaseFailureReason.LISTING_NOT_FOUND, null)
            );
        }
        if (!listing.active() || listing.expiresAt() <= System.currentTimeMillis()) {
            return CompletableFuture.completedFuture(
                    new PurchaseListingResult(false, PurchaseFailureReason.NOT_ACTIVE, listing)
            );
        }
        if (listing.sellerUuid().equals(buyer.getUniqueId())) {
            return CompletableFuture.completedFuture(
                    new PurchaseListingResult(false, PurchaseFailureReason.OWN_LISTING, listing)
            );
        }
        if (!canFitItem(buyer, listing.item())) {
            return CompletableFuture.completedFuture(
                    new PurchaseListingResult(false, PurchaseFailureReason.INVENTORY_FULL, listing)
            );
        }
        if (!beginAction(buyer.getUniqueId())) {
            return CompletableFuture.completedFuture(
                    new PurchaseListingResult(false, PurchaseFailureReason.DATABASE_ERROR, listing)
            );
        }

        var withdrawal = plugin.getEconomyManager().withdraw(
                buyer,
                listing.price(),
                EconomyReason.AUCTION_PURCHASE
        );
        if (!withdrawal.success()) {
            endAction(buyer.getUniqueId());
            return CompletableFuture.completedFuture(
                    new PurchaseListingResult(false, PurchaseFailureReason.NO_MONEY, listing)
            );
        }

        CompletableFuture<PurchaseListingResult> result = new CompletableFuture<>();
        repository.markSold(listingId, buyer.getUniqueId(), System.currentTimeMillis())
                .whenComplete((sold, throwable) -> plugin.getSpigotScheduler().runEntity(buyer, () -> {
                    if (throwable != null || sold == null || sold.isEmpty()) {
                        plugin.getEconomyManager().deposit(buyer, listing.price(), EconomyReason.AUCTION_REFUND);
                        endAction(buyer.getUniqueId());
                        refreshCache();
                        result.complete(new PurchaseListingResult(
                                false,
                                throwable == null ? PurchaseFailureReason.NOT_ACTIVE : PurchaseFailureReason.DATABASE_ERROR,
                                listing
                        ));
                        return;
                    }

                    AuctionHouseRepository.PurchaseCommit commit = sold.get();
                    AuctionListing purchased = commit.listing();
                    repository.makeSellerClaimReady(purchased.id())
                            .thenCompose(ignored -> refreshCache())
                            .thenCompose(ignored -> collectClaim(buyer, commit.buyerClaimId()))
                            .whenComplete((deliveryResult, deliveryError) -> {
                                plugin.getSpigotScheduler().runEntity(buyer, () -> {
                                    endAction(buyer.getUniqueId());
                                    notifySellerOfSale(purchased, buyer);
                                    Player seller = plugin.getServer().getPlayer(purchased.sellerUuid());
                                    if (seller != null && !isClaimsEnabled()) {
                                        processAutoClaims(seller);
                                    }
                                    boolean delivered = deliveryError == null
                                            && deliveryResult != null
                                            && deliveryResult.success();
                                    plugin.getPlayerLogsManager().log(
                                            buyer.getUniqueId(),
                                            buyer.getName(),
                                            "Auctions",
                                            "AUCTION_BUY",
                                            "Bought " + describeItem(purchased.item()) + " from " + purchased.sellerName() + " for " + plugin.getCurrencyManager().formatMoney(purchased.price())
                                    );
                                    result.complete(new PurchaseListingResult(
                                            deliveryError == null,
                                            deliveryError == null ? null : PurchaseFailureReason.DATABASE_ERROR,
                                            purchased,
                                            !delivered
                                    ));
                                });
                            });
                }));
        return result;
    }

    public CompletableFuture<CancelListingResult> cancelListing(Player owner, long listingId) {
        if (!isEnabled()) {
            return CompletableFuture.completedFuture(
                    new CancelListingResult(false, CancelFailureReason.DISABLED, null)
            );
        }
        if (!hasPermission(owner, "cancel")) {
            return CompletableFuture.completedFuture(
                    new CancelListingResult(false, CancelFailureReason.NO_PERMISSION, null)
            );
        }
        AuctionListing listing = getListing(listingId);
        if (listing == null) {
            return CompletableFuture.completedFuture(
                    new CancelListingResult(false, CancelFailureReason.LISTING_NOT_FOUND, null)
            );
        }
        if (!listing.sellerUuid().equals(owner.getUniqueId())) {
            return CompletableFuture.completedFuture(
                    new CancelListingResult(false, CancelFailureReason.NOT_OWNER, listing)
            );
        }
        if (!listing.active()) {
            return CompletableFuture.completedFuture(
                    new CancelListingResult(false, CancelFailureReason.NOT_ACTIVE, listing)
            );
        }
        if (!beginAction(owner.getUniqueId())) {
            return CompletableFuture.completedFuture(
                    new CancelListingResult(false, CancelFailureReason.DATABASE_ERROR, listing)
            );
        }

        return repository.cancelListing(listingId, owner.getUniqueId(), System.currentTimeMillis())
                .thenCompose(cancelled -> refreshCache().thenApply(ignored -> cancelled))
                .handle((cancelled, throwable) -> {
                    endAction(owner.getUniqueId());
                    if (throwable != null) {
                        return new CancelListingResult(false, CancelFailureReason.DATABASE_ERROR, listing);
                    }
                    if (cancelled.isEmpty()) {
                        return new CancelListingResult(false, CancelFailureReason.NOT_ACTIVE, listing);
                    }
                    if (!isClaimsEnabled()) {
                        plugin.getSpigotScheduler().runEntity(owner, () -> processAutoClaims(owner));
                    }
                    AuctionListing cancelledListing = cancelled.get();
                    plugin.getPlayerLogsManager().log(
                            owner.getUniqueId(),
                            owner.getName(),
                            "Auctions",
                            "AUCTION_CANCEL",
                            "Cancelled listing for " + describeItem(cancelledListing.item())
                    );
                    return new CancelListingResult(true, null, cancelledListing);
                });
    }

    public CompletableFuture<ClaimResult> claim(Player player, long claimId) {
        if (!isClaimsEnabled()) {
            return CompletableFuture.completedFuture(
                    new ClaimResult(false, ClaimFailureReason.DISABLED, null)
            );
        }
        if (!hasPermission(player, "claims")) {
            return CompletableFuture.completedFuture(
                    new ClaimResult(false, ClaimFailureReason.NO_PERMISSION, null)
            );
        }
        return collectClaim(player, claimId);
    }

    public CompletableFuture<Void> processAutoClaims(Player player) {
        if (player == null || !player.isOnline() || isClaimsEnabled() || !isEnabled()) {
            return CompletableFuture.completedFuture(null);
        }
        return refreshCache().thenCompose(ignored -> {
            List<AuctionClaim> pending = getUnclaimedClaims(player.getUniqueId());
            CompletableFuture<Integer> moneyClaims = CompletableFuture.completedFuture(0);
            CompletableFuture<Integer> itemClaims = CompletableFuture.completedFuture(0);
            for (AuctionClaim claim : pending) {
                if (claim.moneyClaim()) {
                    moneyClaims = moneyClaims.thenCompose(count -> collectClaim(player, claim.id())
                            .thenApply(result -> count + (result.success() ? 1 : 0)));
                } else {
                    itemClaims = itemClaims.thenCompose(count -> collectClaim(player, claim.id())
                            .thenApply(result -> count + (result.success() ? 1 : 0)));
                }
            }
            return moneyClaims.thenCombine(itemClaims, (money, items) -> {
                if (money + items > 0
                        && player.isOnline()
                        && PlayerSettingUtils.notificationEnabled(
                        plugin,
                        player,
                        PlayerSettingUtils.NotificationChannel.AUCTION
                )) {
                    player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                            "AUCTION_HOUSE.AUTO_COLLECTED",
                            "&aᴀᴜᴛᴏᴍᴀᴛɪᴄᴀʟʟʏ ᴄᴏʟʟᴇᴄᴛᴇᴅ &f{money} &aᴘᴀʏᴏᴜᴛ(ѕ) ᴀɴᴅ &f{items} &aʀᴇᴛᴜʀɴᴇᴅ ɪᴛᴇᴍ(ѕ).",
                            "{money}", String.valueOf(money),
                            "{items}", String.valueOf(items)
                    )));
                }
                return null;
            });
        });
    }

    public CompletableFuture<Integer> expireListings() {
        if (!isEnabled()) {
            return CompletableFuture.completedFuture(0);
        }
        return repository.expireListings(System.currentTimeMillis())
                .thenCompose(count -> refreshCache().thenApply(ignored -> count))
                .whenComplete((count, throwable) -> {
                    if (throwable == null && !isClaimsEnabled()) {
                        plugin.getSpigotScheduler().forEachOnlinePlayer(this::processAutoClaims);
                    }
                });
    }

    public int getMaxActiveListings(Player player) {
        int defaultValue = Math.max(1, config().getInt("SETTINGS.MAX_ACTIVE_LISTINGS_DEFAULT", 5));
        if (player == null) {
            return defaultValue;
        }

        int resolved = 0;
        ConfigurationSection limits = config().getConfigurationSection("SETTINGS.MAX_ACTIVE_LISTINGS_BY_PERMISSION");
        if (limits != null) {
            for (String permission : limits.getKeys(true)) {
                if (!limits.isInt(permission)) {
                    continue;
                }
                if (!PermissionUtils.hasExact(player, permission)) {
                    continue;
                }
                int value = limits.getInt(permission, defaultValue);
                if (value < 0) {
                    return Integer.MAX_VALUE;
                }
                resolved = Math.max(resolved, value);
            }
        }
        resolved = Math.max(resolved, PermissionUtils.resolveHighestExactNumberedPermission(
                player,
                "ultimatedonutsmp.auctionhouse.",
                MAX_PERMISSION_VALUE
        ));
        resolved = Math.max(resolved, PermissionUtils.resolveHighestExactNumberedPermission(
                player,
                "ultimatedonutsmp.auctionhouse.limit.",
                MAX_PERMISSION_VALUE
        ));
        resolved = Math.max(resolved, PermissionUtils.resolveHighestExactNumberedPermission(
                player,
                "donutauction.limit.",
                MAX_PERMISSION_VALUE
        ));
        return resolved > 0 ? resolved : defaultValue;
    }

    public String describeItem(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return "Unknown Item";
        }
        if (item.hasItemMeta() && item.getItemMeta() != null && item.getItemMeta().hasDisplayName()) {
            return ColorUtils.strip(ColorUtils.toLegacyString(item.getItemMeta().getDisplayName()));
        }
        return plugin.getWorthManager().prettifyMaterial(item.getType());
    }

    public String formatRemaining(long seconds) {
        return seconds <= 0
                ? config().getString("GUI.TEXT.EXPIRED.NAME", "ᴇxᴘɪʀᴇᴅ")
                : NumberUtils.formatTimeLong(seconds);
    }

    public String formatDuration(int hours) {
        if (hours % 168 == 0) {
            int weeks = hours / 168;
            String path = weeks == 1 ? "DURATION_WEEK" : "DURATION_WEEKS";
            return config().getString(
                    "GUI.TEXT." + path + ".NAME",
                    weeks == 1 ? "{value} ᴡᴇᴇᴋ" : "{value} ᴡᴇᴇᴋѕ"
            ).replace("{value}", String.valueOf(weeks));
        }
        if (hours % 24 == 0) {
            int days = hours / 24;
            String path = days == 1 ? "DURATION_DAY" : "DURATION_DAYS";
            return config().getString(
                    "GUI.TEXT." + path + ".NAME",
                    days == 1 ? "{value} ᴅᴀʏ" : "{value} ᴅᴀʏѕ"
            ).replace("{value}", String.valueOf(days));
        }
        String path = hours == 1 ? "DURATION_HOUR" : "DURATION_HOURS";
        return config().getString(
                "GUI.TEXT." + path + ".NAME",
                hours == 1 ? "{value} ʜᴏᴜʀ" : "{value} ʜᴏᴜʀѕ"
        ).replace("{value}", String.valueOf(hours));
    }

    public void returnEscrow(Player player, ItemStack item) {
        restoreEscrow(player, item);
    }

    private CompletableFuture<ClaimResult> collectClaim(Player player, long claimId) {
        AuctionClaim cached = getClaim(claimId);
        if (cached == null) {
            return CompletableFuture.completedFuture(
                    new ClaimResult(false, ClaimFailureReason.CLAIM_NOT_FOUND, null)
            );
        }
        if (!cached.ownerUuid().equals(player.getUniqueId())) {
            return CompletableFuture.completedFuture(
                    new ClaimResult(false, ClaimFailureReason.NOT_OWNER, cached)
            );
        }
        if (cached.itemClaim() && !canFitItem(player, cached.item())) {
            return CompletableFuture.completedFuture(
                    new ClaimResult(false, ClaimFailureReason.INVENTORY_FULL, cached)
            );
        }

        CompletableFuture<ClaimResult> result = new CompletableFuture<>();
        repository.acquireClaim(claimId, player.getUniqueId()).whenComplete((lease, throwable) -> {
            if (throwable != null || lease == null || lease.isEmpty()) {
                result.complete(new ClaimResult(
                        false,
                        throwable == null ? ClaimFailureReason.ALREADY_CLAIMED : ClaimFailureReason.DATABASE_ERROR,
                        cached
                ));
                return;
            }
            AuctionHouseRepository.ClaimLease acquired = lease.get();
            plugin.getSpigotScheduler().runEntity(player, () -> deliverClaim(player, acquired, result));
        });
        return result;
    }

    private void deliverClaim(
            Player player,
            AuctionHouseRepository.ClaimLease lease,
            CompletableFuture<ClaimResult> result
    ) {
        AuctionClaim claim = lease.claim();
        if (!player.isOnline()) {
            repository.releaseClaim(lease);
            result.complete(new ClaimResult(false, ClaimFailureReason.NO_PLAYER_DATA, claim));
            return;
        }

        if (claim.moneyClaim()) {
            var deposit = plugin.getEconomyManager().deposit(
                    player,
                    claim.moneyAmount(),
                    EconomyReason.AUCTION_CLAIM
            );
            if (!deposit.success()) {
                repository.releaseClaim(lease);
                result.complete(new ClaimResult(false, ClaimFailureReason.NO_PLAYER_DATA, claim));
                return;
            }
            repository.completeClaim(lease)
                    .whenComplete((completed, throwable) -> {
                        if (throwable == null && Boolean.TRUE.equals(completed)) {
                            refreshCache().whenComplete((ignored, refreshError) -> result.complete(new ClaimResult(
                                    refreshError == null,
                                    refreshError == null ? null : ClaimFailureReason.DATABASE_ERROR,
                                    claim
                            )));
                            return;
                        }

                        var compensation = plugin.getEconomyManager().withdraw(
                                player,
                                claim.moneyAmount(),
                                EconomyReason.AUCTION_REFUND
                        );
                        if (compensation.success()) {
                            repository.restoreClaim(lease)
                                    .thenCompose(ignored -> refreshCache())
                                    .whenComplete((ignored, recoveryError) -> result.complete(new ClaimResult(
                                            false,
                                            ClaimFailureReason.DATABASE_ERROR,
                                            claim
                                    )));
                            return;
                        }

                        plugin.getLogger().severe(
                                "Auction claim #" + claim.id()
                                        + " was paid but could not be finalized or compensated; "
                                        + "keeping the database lease to prevent a duplicate payout."
                        );
                        result.complete(new ClaimResult(false, ClaimFailureReason.DATABASE_ERROR, claim));
                    });
            return;
        }

        if (!canFitItem(player, claim.item())) {
            repository.releaseClaim(lease);
            result.complete(new ClaimResult(false, ClaimFailureReason.INVENTORY_FULL, claim));
            return;
        }

        repository.completeClaim(lease).whenComplete((completed, throwable) -> plugin.getSpigotScheduler().runEntity(player, () -> {
            if (throwable != null || !Boolean.TRUE.equals(completed)) {
                result.complete(new ClaimResult(false, ClaimFailureReason.DATABASE_ERROR, claim));
                return;
            }
            Map<Integer, ItemStack> leftovers = player.getInventory().addItem(claim.item().clone());
            player.updateInventory();
            CompletableFuture<Void> recovery = CompletableFuture.completedFuture(null);
            for (ItemStack leftover : leftovers.values()) {
                recovery = recovery.thenCompose(ignored -> repository.createPendingItemClaim(
                        player.getUniqueId(),
                        claim.sourceListingId(),
                        leftover,
                        System.currentTimeMillis()
                ));
            }
            recovery.thenCompose(ignored -> refreshCache())
                    .whenComplete((ignored, deliveryError) -> result.complete(new ClaimResult(
                            deliveryError == null,
                            deliveryError == null ? null : ClaimFailureReason.DATABASE_ERROR,
                            claim
                    )));
        }));
    }

    private CreateListingResult validateListing(
            Player seller,
            ItemStack item,
            double price,
            int durationHours
    ) {
        if (!isEnabled()) {
            return new CreateListingResult(false, CreateFailureReason.DISABLED, null, 0D);
        }
        if (!hasPermission(seller, "sell")) {
            return new CreateListingResult(false, CreateFailureReason.NO_PERMISSION, null, 0D);
        }
        if (item == null || item.getType().isAir()) {
            return new CreateListingResult(false, CreateFailureReason.NO_ITEM, null, 0D);
        }
        if (!isPriceAllowed(price, getMinPrice(), getMaxPrice())) {
            return new CreateListingResult(false, CreateFailureReason.INVALID_PRICE, null, 0D);
        }
        if (!isDurationAllowed(durationHours, getAllowedDurations())) {
            return new CreateListingResult(false, CreateFailureReason.INVALID_DURATION, null, 0D);
        }

        ItemStack sanitized = sanitizeListedItem(item);
        if (!isListable(sanitized)) {
            return new CreateListingResult(false, CreateFailureReason.INVALID_ITEM, null, 0D);
        }
        CrashProtectionManager.ValidationResult safety = plugin.getCrashProtectionManager()
                .validateForStorage(sanitized, CrashProtectionManager.Context.AUCTION_HOUSE);
        if (!safety.allowed()) {
            plugin.getCrashProtectionManager().logBlockedItem(
                    seller.getName() + "/" + seller.getUniqueId(),
                    sanitized,
                    CrashProtectionManager.Context.AUCTION_HOUSE,
                    safety
            );
            return new CreateListingResult(false, CreateFailureReason.UNSAFE_ITEM, null, 0D, safety);
        }
        return null;
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
        Set<Material> blocked = EnumSet.noneOf(Material.class);
        for (String raw : config().getStringList("RESTRICTIONS.BLOCKED_MATERIALS")) {
            try {
                blocked.add(Material.valueOf(raw.trim().toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException ignored) {
            }
        }
        if (blocked.contains(item.getType())) {
            return false;
        }

        if (item.hasItemMeta() && item.getItemMeta() != null && item.getItemMeta().hasLore()) {
            List<String> blockedTerms = config().getStringList("RESTRICTIONS.BLOCKED_IF_HAS_LORE_CONTAINS");
            List<String> lore = item.getItemMeta().getLore();
            if (lore != null) {
                for (String line : lore) {
                    String plain = ColorUtils.strip(ColorUtils.toLegacyString(line)).toLowerCase(Locale.ROOT);
                    for (String term : blockedTerms) {
                        if (term != null && !term.isBlank() && plain.contains(term.toLowerCase(Locale.ROOT))) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
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
                remaining -= current.getMaxStackSize() - current.getAmount();
            }
            if (remaining <= 0) {
                return true;
            }
        }
        return false;
    }

    private void restoreEscrow(Player player, ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return;
        }
        if (!player.isOnline()) {
            repository.createPendingItemClaim(
                    player.getUniqueId(),
                    0L,
                    item,
                    System.currentTimeMillis()
            ).thenCompose(ignored -> refreshCache());
            return;
        }
        Map<Integer, ItemStack> leftovers = player.getInventory().addItem(item.clone());
        player.updateInventory();
        for (ItemStack leftover : leftovers.values()) {
            repository.createPendingItemClaim(
                    player.getUniqueId(),
                    0L,
                    leftover,
                    System.currentTimeMillis()
            ).thenCompose(ignored -> refreshCache());
        }
    }

    private void notifySellerOfSale(AuctionListing listing, Player buyer) {
        Player seller = plugin.getServer().getPlayer(listing.sellerUuid());
        if (seller == null) {
            return;
        }
        if (!PlayerSettingUtils.notificationEnabled(
                plugin,
                seller,
                PlayerSettingUtils.NotificationChannel.AUCTION
        )) {
            return;
        }
        if (plugin.getFriendsManager() != null
                && plugin.getFriendsManager().isTransactionMessageBlocked(
                buyer.getUniqueId(),
                seller.getUniqueId()
        )) {
            return;
        }
        seller.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage(
                "AUCTION_HOUSE.ITEM_SOLD",
                "{buyer}", plugin.getHideManager().publicName(buyer),
                "{item}", describeItem(listing.item()),
                "{price}", NumberUtils.format(listing.price()),
                "{price_formatted}", plugin.getCurrencyManager().formatMoney(listing.price()),
                "{payout}", NumberUtils.format(listing.sellerPayout()),
                "{payout_formatted}", plugin.getCurrencyManager().formatMoney(listing.sellerPayout())
        )));
    }

    private FileConfiguration config() {
        return plugin.getConfigManager().getAuctionHouse();
    }

    private int normalizeSize(int configured) {
        int bounded = Math.max(9, Math.min(54, configured));
        int remainder = bounded % 9;
        return remainder == 0 ? bounded : bounded + (9 - remainder);
    }

    private long getClickCooldownMillis() {
        return Math.max(250L, config().getLong("SETTINGS.CLICK_COOLDOWN_MS", 750L));
    }

    private int defaultDurationHours() {
        int configured = Math.max(1, config().getInt("SETTINGS.LISTING_DURATION_HOURS", 48));
        return getAllowedDurations().contains(configured) ? configured : getAllowedDurations().get(0);
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
        double percent = Math.max(0D, config().getDouble("PRICING.TAX_PERCENT", 5D));
        return Math.max(0D, price * (percent / 100D));
    }

    private void validateConfiguration() {
        FileConfiguration rawConfiguration = plugin.getConfigManager().getLegacyAuctionHouse();
        if (migrateMyAuctionControlSlots(rawConfiguration)) {
            plugin.getConfigManager().saveAuctionHouse();
            plugin.getLogger().info(
                    "Aligned My Auction controls to slots 48-50: refresh anvil, back chest, then page book."
            );
        }
        if (getMinPrice() > getMaxPrice()) {
            plugin.getLogger().warning("auction-house.yml has PRICING.MIN_PRICE greater than PRICING.MAX_PRICE.");
        }
        for (String material : config().getStringList("RESTRICTIONS.BLOCKED_MATERIALS")) {
            try {
                Material.valueOf(material.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException exception) {
                plugin.getLogger().warning("Invalid Auction House blocked material: " + material);
            }
        }
    }

    public static boolean migrateMyAuctionControlSlots(FileConfiguration configuration) {
        if (configuration == null) {
            return false;
        }
        String root = "GUI.PLAYER_ITEMS.CONTROLS.";
        int backSlot = configuration.getInt(root + "BACK.SLOT", 49);
        int refreshSlot = configuration.getInt(root + "REFRESH.SLOT", 48);
        int pageSlot = configuration.getInt(root + "PAGE.SLOT", 50);
        if (backSlot == 49 && refreshSlot == 48 && pageSlot == 50) {
            return false;
        }
        configuration.set(root + "BACK.SLOT", 49);
        configuration.set(root + "REFRESH.SLOT", 48);
        configuration.set(root + "PAGE.SLOT", 50);
        return true;
    }

    private boolean hasPermission(Player player, String action) {
        if (PermissionUtils.has(player, "ultimatedonutsmp.admin.auctionhouse")) {
            return true;
        }
        if (PermissionUtils.has(player, "ultimatedonutsmp.auctionhouse." + action)
                || PermissionUtils.has(player, "donutauction." + action)) {
            return true;
        }
        if ("buy".equalsIgnoreCase(action)
                && (PermissionUtils.has(player, "ultimatedonutsmp.auctionhouse.use")
                || PermissionUtils.has(player, "donutauction.use"))) {
            return !isExplicitlyDenied(player, "ultimatedonutsmp.auctionhouse.buy")
                    && !isExplicitlyDenied(player, "donutauction.buy");
        }
        return false;
    }

    private boolean isExplicitlyDenied(Player player, String permission) {
        if (player == null || permission == null) {
            return false;
        }
        String normalized = PermissionUtils.normalizePermissionNode(permission);
        for (org.bukkit.permissions.PermissionAttachmentInfo info : player.getEffectivePermissions()) {
            if (PermissionUtils.normalizePermissionNode(info.getPermission()).equals(normalized) && !info.getValue()) {
                return true;
            }
        }
        return false;
    }

    private Throwable unwrap(Throwable throwable) {
        return throwable.getCause() == null ? throwable : throwable.getCause();
    }

    public static boolean isPriceAllowed(double price, double minimum, double maximum) {
        return Double.isFinite(price)
                && Double.isFinite(minimum)
                && Double.isFinite(maximum)
                && minimum <= maximum
                && price >= minimum
                && price <= maximum;
    }

    public static boolean isDurationAllowed(int durationHours, List<Integer> allowedDurations) {
        return durationHours > 0
                && allowedDurations != null
                && allowedDurations.contains(durationHours);
    }

    private static final class TimeUnitHours {
        private static long toMillis(int hours) {
            return hours * 60L * 60L * 1000L;
        }
    }

    public int countActiveBotListings(java.util.Collection<String> botNames) {
        long now = System.currentTimeMillis();
        return (int) listingCache.get().stream()
                .filter(listing -> listing.active() && listing.expiresAt() > now)
                .filter(listing -> botNames.contains(listing.sellerName()))
                .count();
    }

    public CompletableFuture<AuctionListing> createBotListingDirect(
            UUID botUuid,
            String botName,
            ItemStack item,
            double price,
            int durationHours,
            String categoryKey
    ) {
        long now = System.currentTimeMillis();
        long expiresAt = now + TimeUnitHours.toMillis(durationHours);
        return repository.createListing(
                botUuid,
                botName,
                price,
                0D,
                item,
                now,
                expiresAt,
                categoryKey,
                Integer.MAX_VALUE
        ).thenCompose(result -> {
            if (result.created()) {
                return refreshCache().thenApply(ignored -> result.listing());
            }
            return CompletableFuture.completedFuture(null);
        });
    }
}
