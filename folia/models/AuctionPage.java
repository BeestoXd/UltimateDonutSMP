package com.bx.ultimateDonutSmp.models;

import java.util.List;

public record AuctionPage(
        List<AuctionListing> listings,
        int page,
        int totalPages,
        int totalListings
) {
    public AuctionPage {
        listings = listings == null ? List.of() : List.copyOf(listings);
        page = Math.max(1, page);
        totalPages = Math.max(1, totalPages);
        totalListings = Math.max(0, totalListings);
    }

    public boolean hasPrevious() {
        return page > 1;
    }

    public boolean hasNext() {
        return page < totalPages;
    }
}
