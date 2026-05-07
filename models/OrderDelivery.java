package com.bx.ultimateDonutSmp.models;

import java.util.UUID;

public record OrderDelivery(
        long id,
        long orderId,
        UUID delivererUuid,
        String delivererName,
        int quantity,
        double payout,
        long createdAt
) {
}
