package com.bx.ultimateDonutSmp.models;

import java.util.UUID;

public record EconomyTransactionResult(
        boolean success,
        EconomyFailureReason failureReason,
        EconomyReason reason,
        UUID targetUuid,
        String displayName,
        double amount,
        double beforeBalance,
        double afterBalance
) {

    public boolean invalidAmount() {
        return failureReason == EconomyFailureReason.INVALID_AMOUNT;
    }

    public boolean playerNotFound() {
        return failureReason == EconomyFailureReason.PLAYER_NOT_FOUND;
    }

    public boolean noPlayerData() {
        return failureReason == EconomyFailureReason.NO_PLAYER_DATA;
    }

    public boolean insufficientFunds() {
        return failureReason == EconomyFailureReason.INSUFFICIENT_FUNDS;
    }
}
