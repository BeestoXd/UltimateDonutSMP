package com.bx.ultimateDonutSmp.models;

public record WorthResult(
        boolean sellable,
        boolean container,
        double unitWorth,
        double totalWorth,
        double baseWorth,
        double containerContentsWorth,
        String resolutionType,
        String sourceKey,
        String categoryKey
) {

    public static WorthResult unsellable() {
        return new WorthResult(false, false, -1, -1, 0, 0, "UNSELLABLE", "", "");
    }

    public boolean hasContainerContentsWorth() {
        return containerContentsWorth > 0;
    }
}
