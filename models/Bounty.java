package com.bx.ultimateDonutSmp.models;

import java.util.UUID;

public class Bounty {

    private final UUID targetUuid;
    private double amount;
    private UUID placerUuid;

    public Bounty(UUID targetUuid, double amount, UUID placerUuid) {
        this.targetUuid = targetUuid;
        this.amount = amount;
        this.placerUuid = placerUuid;
    }

    public UUID getTargetUuid() { return targetUuid; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = Math.max(0, amount); }
    public void addAmount(double extra) { this.amount += extra; }

    public UUID getPlacerUuid() { return placerUuid; }
    public void setPlacerUuid(UUID placerUuid) { this.placerUuid = placerUuid; }
}
