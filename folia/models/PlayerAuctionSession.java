package com.bx.ultimateDonutSmp.models;

import java.util.UUID;

public final class PlayerAuctionSession {
    private final UUID playerId;
    private AuctionBrowseRequest request;

    public PlayerAuctionSession(UUID playerId, AuctionBrowseRequest request) {
        this.playerId = playerId;
        this.request = request;
    }

    public UUID playerId() {
        return playerId;
    }

    public AuctionBrowseRequest request() {
        return request;
    }

    public void request(AuctionBrowseRequest request) {
        this.request = request;
    }
}
