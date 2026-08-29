package com.bx.ultimateDonutSmp.models;

import java.util.List;

/**
 * One configured PvP rank: an id, the text shown for it, and the Elo a player needs to hold it.
 *
 * <p>Ranks are ordered by their requirement rather than by their name, so a server owner can rename
 * or reorder the whole ladder in pvp.yml without the code caring what the tiers are called.</p>
 */
public class PvpRank {

    private final String id;
    private final String display;
    private final int eloRequirement;

    public PvpRank(String id, String display, int eloRequirement) {
        this.id = id == null ? "" : id;
        this.display = display == null || display.isBlank() ? this.id : display;
        this.eloRequirement = eloRequirement;
    }

    public String getId() {
        return id;
    }

    public String getDisplay() {
        return display;
    }

    public int getEloRequirement() {
        return eloRequirement;
    }

    /**
     * Picks the rank a player holds at the given Elo: the highest one they still meet.
     *
     * <p>The list is expected to be sorted by requirement ascending, which is how
     * {@code PvpManager} loads it. Below the cheapest rank the player keeps that cheapest rank
     * rather than having none, so the ladder always has a floor.</p>
     */
    public static PvpRank resolve(List<PvpRank> ranks, int elo) {
        if (ranks == null || ranks.isEmpty()) {
            return null;
        }

        PvpRank current = ranks.get(0);
        for (PvpRank rank : ranks) {
            if (elo >= rank.getEloRequirement()) {
                current = rank;
            }
        }
        return current;
    }
}
