package com.bx.ultimateDonutSmp.models;

import java.util.UUID;

public record FollowEntry(
        UUID followerUuid,
        UUID followedUuid,
        String followedNameSnapshot,
        boolean transactionsEnabled,
        boolean messagesEnabled,
        boolean paymentsEnabled,
        boolean activityEnabled,
        boolean tpaAutoAcceptEnabled,
        boolean teleportRequestsEnabled,
        long createdAt
) {}
