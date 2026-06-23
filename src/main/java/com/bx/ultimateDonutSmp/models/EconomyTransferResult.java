package com.bx.ultimateDonutSmp.models;

import java.util.UUID;

public record EconomyTransferResult(
        boolean success,
        EconomyFailureReason failureReason,
        EconomyReason reason,
        UUID senderUuid,
        String senderName,
        UUID recipientUuid,
        String recipientName,
        double amount,
        double senderBeforeBalance,
        double senderAfterBalance,
        double recipientBeforeBalance,
        double recipientAfterBalance
) {

    public boolean invalidAmount() {
        return failureReason == EconomyFailureReason.INVALID_AMOUNT;
    }

    public boolean insufficientFunds() {
        return failureReason == EconomyFailureReason.INSUFFICIENT_FUNDS;
    }

    public boolean sameAccount() {
        return failureReason == EconomyFailureReason.SAME_ACCOUNT;
    }

    public boolean playerNotFound() {
        return failureReason == EconomyFailureReason.PLAYER_NOT_FOUND;
    }

    public boolean noPlayerData() {
        return failureReason == EconomyFailureReason.NO_PLAYER_DATA;
    }
}
