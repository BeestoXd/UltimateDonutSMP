package com.bx.ultimateDonutSmp.models;

import com.bx.ultimateDonutSmp.managers.AuctionHouseManager;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

public final class AuctionBrowseEngine {

    private AuctionBrowseEngine() {
    }

    public static AuctionPage page(
            List<AuctionListing> source,
            AuctionBrowseRequest request,
            int pageSize,
            long now,
            Function<AuctionListing, String> itemDescription
    ) {
        List<AuctionListing> filtered = filter(source, request, now, itemDescription);
        int normalizedPageSize = Math.max(1, pageSize);
        int totalPages = Math.max(1, (int) Math.ceil(filtered.size() / (double) normalizedPageSize));
        int page = Math.min(Math.max(1, request.page()), totalPages);
        int from = Math.min(filtered.size(), (page - 1) * normalizedPageSize);
        int to = Math.min(filtered.size(), from + normalizedPageSize);
        return new AuctionPage(filtered.subList(from, to), page, totalPages, filtered.size());
    }

    public static List<AuctionListing> filter(
            List<AuctionListing> source,
            AuctionBrowseRequest request,
            long now,
            Function<AuctionListing, String> itemDescription
    ) {
        String search = request.search().trim().toLowerCase(Locale.ROOT);
        return source.stream()
                .filter(listing -> listing.active() && listing.expiresAt() > now)
                .filter(listing -> request.category().matches(listing.item()))
                .filter(listing -> matchesSearch(listing, search, itemDescription))
                .sorted(comparator(request.sort()))
                .toList();
    }

    public static Comparator<AuctionListing> comparator(AuctionHouseManager.AuctionSort sort) {
        AuctionHouseManager.AuctionSort effective = sort == null
                ? AuctionHouseManager.AuctionSort.NEWEST
                : sort;
        return switch (effective) {
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

    private static boolean matchesSearch(
            AuctionListing listing,
            String normalizedSearch,
            Function<AuctionListing, String> itemDescription
    ) {
        if (normalizedSearch.isBlank()) {
            return true;
        }
        String display = itemDescription.apply(listing);
        String normalizedDisplay = display == null ? "" : display.toLowerCase(Locale.ROOT);
        String material = listing.item().getType().name().toLowerCase(Locale.ROOT).replace('_', ' ');
        return normalizedDisplay.contains(normalizedSearch) || material.contains(normalizedSearch);
    }
}
