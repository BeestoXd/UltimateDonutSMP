package com.bx.ultimateDonutSmp.managers;

import net.skinsrestorer.api.SkinsRestorer;
import net.skinsrestorer.api.SkinsRestorerProvider;
import net.skinsrestorer.api.property.SkinIdentifier;
import net.skinsrestorer.api.property.SkinProperty;
import net.skinsrestorer.api.storage.PlayerStorage;
import net.skinsrestorer.api.storage.SkinStorage;

import java.util.Optional;
import java.util.UUID;

final class SkinsRestorerSkinLookup {

    private SkinsRestorerSkinLookup() {
    }

    static TablistManager.SkinTexture resolve(UUID playerId, String playerName) {
        if (playerId == null || playerName == null || playerName.isBlank()) {
            return null;
        }

        try {
            SkinsRestorer skinsRestorer = SkinsRestorerProvider.get();
            PlayerStorage playerStorage = skinsRestorer.getPlayerStorage();
            SkinStorage skinStorage = skinsRestorer.getSkinStorage();

            TablistManager.SkinTexture texture = fromProperty(getSkinOfPlayer(playerStorage, playerId));
            if (texture != null && texture.isValid()) {
                return texture;
            }

            texture = fromStoredIdentifier(playerStorage, skinStorage, playerId);
            if (texture != null && texture.isValid()) {
                return texture;
            }

            texture = fromIdentifier(playerStorage, skinStorage, playerId, playerName, false);
            if (texture != null && texture.isValid()) {
                return texture;
            }

            texture = fromIdentifier(playerStorage, skinStorage, playerId, playerName, true);
            if (texture != null && texture.isValid()) {
                return texture;
            }

            texture = fromIdentifier(playerStorage, skinStorage, playerId, playerName, null);
            if (texture != null && texture.isValid()) {
                return texture;
            }

            texture = fromProperty(getSkinForPlayer(playerStorage, playerId, playerName, false));
            if (texture != null && texture.isValid()) {
                return texture;
            }

            texture = fromProperty(getSkinForPlayer(playerStorage, playerId, playerName, true));
            if (texture != null && texture.isValid()) {
                return texture;
            }

            return fromProperty(getSkinForPlayer(playerStorage, playerId, playerName));
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static SkinProperty getSkinForPlayer(
            PlayerStorage playerStorage,
            UUID playerId,
            String playerName,
            boolean onlineMode
    ) {
        try {
            return playerStorage.getSkinForPlayer(playerId, playerName, onlineMode).orElse(null);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static SkinProperty getSkinForPlayer(PlayerStorage playerStorage, UUID playerId, String playerName) {
        try {
            return playerStorage.getSkinForPlayer(playerId, playerName).orElse(null);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static SkinProperty getSkinOfPlayer(PlayerStorage playerStorage, UUID playerId) {
        try {
            return playerStorage.getSkinOfPlayer(playerId).orElse(null);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static TablistManager.SkinTexture fromIdentifier(
            PlayerStorage playerStorage,
            SkinStorage skinStorage,
            UUID playerId,
            String playerName,
            Boolean onlineMode
    ) {
        try {
            Optional<SkinIdentifier> identifier = onlineMode == null
                    ? playerStorage.getSkinIdForPlayer(playerId, playerName)
                    : playerStorage.getSkinIdForPlayer(playerId, playerName, onlineMode);
            if (identifier.isEmpty()) {
                return null;
            }
            return fromProperty(skinStorage.getSkinDataByIdentifier(identifier.get()).orElse(null));
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static TablistManager.SkinTexture fromStoredIdentifier(
            PlayerStorage playerStorage,
            SkinStorage skinStorage,
            UUID playerId
    ) {
        try {
            Optional<SkinIdentifier> identifier = playerStorage.getSkinIdOfPlayer(playerId);
            if (identifier.isEmpty()) {
                return null;
            }
            return fromProperty(skinStorage.getSkinDataByIdentifier(identifier.get()).orElse(null));
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static TablistManager.SkinTexture fromProperty(SkinProperty property) {
        if (property == null || property.getValue() == null || property.getValue().isBlank()) {
            return null;
        }
        return new TablistManager.SkinTexture(property.getValue(), property.getSignature());
    }
}
