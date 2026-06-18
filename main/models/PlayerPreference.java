package com.bx.ultimateDonutSmp.models;

import java.util.UUID;

public class PlayerPreference {
    private final UUID playerId;
    private boolean fastBuyEnabled;
    private boolean fastSellEnabled;
    private int lastDurationHours;
    private String lastCategory;
    private double lastPrice;

    public PlayerPreference(UUID playerId) {
        this.playerId = playerId;
        this.fastBuyEnabled = false;
        this.fastSellEnabled = false;
        this.lastDurationHours = 48;
        this.lastCategory = "ALL";
        this.lastPrice = 0.0;
    }

    public PlayerPreference(UUID playerId, boolean fastBuyEnabled, boolean fastSellEnabled, int lastDurationHours, String lastCategory, double lastPrice) {
        this.playerId = playerId;
        this.fastBuyEnabled = fastBuyEnabled;
        this.fastSellEnabled = fastSellEnabled;
        this.lastDurationHours = lastDurationHours;
        this.lastCategory = lastCategory == null ? "ALL" : lastCategory;
        this.lastPrice = lastPrice;
    }

    public UUID playerId() {
        return playerId;
    }

    public boolean fastBuyEnabled() {
        return fastBuyEnabled;
    }

    public void fastBuyEnabled(boolean fastBuyEnabled) {
        this.fastBuyEnabled = fastBuyEnabled;
    }

    public boolean fastSellEnabled() {
        return fastSellEnabled;
    }

    public void fastSellEnabled(boolean fastSellEnabled) {
        this.fastSellEnabled = fastSellEnabled;
    }

    public int lastDurationHours() {
        return lastDurationHours;
    }

    public void lastDurationHours(int lastDurationHours) {
        this.lastDurationHours = lastDurationHours;
    }

    public String lastCategory() {
        return lastCategory;
    }

    public void lastCategory(String lastCategory) {
        this.lastCategory = lastCategory == null ? "ALL" : lastCategory;
    }

    public double lastPrice() {
        return lastPrice;
    }

    public void lastPrice(double lastPrice) {
        this.lastPrice = lastPrice;
    }
}
