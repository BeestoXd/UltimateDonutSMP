package com.bx.ultimateDonutSmp.models;

import com.bx.ultimateDonutSmp.managers.AuctionHouseManager;

public record AuctionBrowseRequest(
        int page,
        AuctionHouseManager.AuctionSort sort,
        AuctionCategory category,
        String search
) {
    public AuctionBrowseRequest {
        page = Math.max(1, page);
        sort = sort == null ? AuctionHouseManager.AuctionSort.NEWEST : sort;
        category = category == null ? AuctionCategory.ALL : category;
        search = search == null ? "" : search.trim();
    }

    public AuctionBrowseRequest withPage(int nextPage) {
        return new AuctionBrowseRequest(nextPage, sort, category, search);
    }

    public AuctionBrowseRequest withSort(AuctionHouseManager.AuctionSort nextSort) {
        return new AuctionBrowseRequest(1, nextSort, category, search);
    }

    public AuctionBrowseRequest withCategory(AuctionCategory nextCategory) {
        return new AuctionBrowseRequest(1, sort, nextCategory, search);
    }

    public AuctionBrowseRequest withSearch(String nextSearch) {
        return new AuctionBrowseRequest(1, sort, category, nextSearch);
    }
}
