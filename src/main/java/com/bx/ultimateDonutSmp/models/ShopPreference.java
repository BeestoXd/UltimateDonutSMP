package com.bx.ultimateDonutSmp.models;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public record ShopPreference(
        UUID playerId,
        Set<String> favorites
) {

    public ShopPreference {
        favorites = favorites == null ? Set.of() : Set.copyOf(new LinkedHashSet<>(favorites));
    }

    public ShopPreference withFavorite(String favoriteId, boolean favorite) {
        LinkedHashSet<String> updated = new LinkedHashSet<>(favorites);
        if (favorite) {
            updated.add(favoriteId);
        } else {
            updated.remove(favoriteId);
        }
        return new ShopPreference(playerId, updated);
    }
}
