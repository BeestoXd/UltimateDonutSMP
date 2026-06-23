package com.bx.ultimateDonutSmp.models;

import java.util.UUID;

public final class OrderUiState {
    private final UUID playerUuid;
    private OrderSort sort;
    private String filter;
    private OrderAlphaSort itemSort;
    private int page;
    private int itemPage;
    private String search;
    private String itemSearch;

    public OrderUiState(UUID playerUuid) {
        this(playerUuid, OrderSort.MOST_PAID, "ALL", OrderAlphaSort.A_Z);
    }

    public OrderUiState(UUID playerUuid, OrderSort sort, String filter, OrderAlphaSort itemSort) {
        this.playerUuid = playerUuid;
        this.sort = sort == null ? OrderSort.MOST_PAID : sort;
        this.filter = normalizeFilter(filter);
        this.itemSort = itemSort == null ? OrderAlphaSort.A_Z : itemSort;
        this.search = "";
        this.itemSearch = "";
    }

    public UUID playerUuid() { return playerUuid; }
    public OrderSort sort() { return sort; }
    public void sort(OrderSort sort) { this.sort = sort == null ? OrderSort.MOST_PAID : sort; }
    public String filter() { return filter; }
    public void filter(String filter) { this.filter = normalizeFilter(filter); }
    public OrderAlphaSort itemSort() { return itemSort; }
    public void itemSort(OrderAlphaSort itemSort) { this.itemSort = itemSort == null ? OrderAlphaSort.A_Z : itemSort; }
    public int page() { return Math.max(0, page); }
    public void page(int page) { this.page = Math.max(0, page); }
    public int itemPage() { return Math.max(0, itemPage); }
    public void itemPage(int itemPage) { this.itemPage = Math.max(0, itemPage); }
    public String search() { return search; }
    public void search(String search) { this.search = search == null ? "" : search.trim(); }
    public String itemSearch() { return itemSearch; }
    public void itemSearch(String itemSearch) { this.itemSearch = itemSearch == null ? "" : itemSearch.trim(); }

    private static String normalizeFilter(String filter) {
        return filter == null || filter.isBlank() ? "ALL" : filter.trim().toUpperCase(java.util.Locale.ROOT);
    }
}
