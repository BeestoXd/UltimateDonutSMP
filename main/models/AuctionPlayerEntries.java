package com.bx.ultimateDonutSmp.models;

import java.util.ArrayList;
import java.util.List;

public final class AuctionPlayerEntries {

    private AuctionPlayerEntries() {
    }

    public static List<Object> combine(
            List<AuctionListing> listings,
            List<AuctionClaim> claims,
            boolean claimsEnabled
    ) {
        List<Object> entries = new ArrayList<>(listings == null ? List.of() : listings);
        if (claimsEnabled && claims != null) {
            entries.addAll(claims);
        }
        return List.copyOf(entries);
    }
}
