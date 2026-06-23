package com.bx.ultimateDonutSmp.models;

public record OrderBatchClaimResult(
        int itemClaims,
        int refundClaims,
        int failedClaims,
        int itemAmount,
        double refundAmount
) {
}
