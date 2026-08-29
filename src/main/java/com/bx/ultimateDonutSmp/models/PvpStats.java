package com.bx.ultimateDonutSmp.models;

/**
 * A player's ranked arena record. Immutable, so a kill produces a new instance the way
 * {@link FfaStats} does and nothing can hold a stale half-updated copy.
 */
public class PvpStats {

    private final int elo;
    private final int level;
    private final int xp;
    private final int kills;
    private final int deaths;
    private final int streak;
    private final int bestStreak;
    private final int arenaJoins;

    public PvpStats(int elo, int level, int xp, int kills, int deaths, int streak, int bestStreak, int arenaJoins) {
        this.elo = Math.max(0, elo);
        this.level = Math.max(1, level);
        this.xp = Math.max(0, xp);
        this.kills = Math.max(0, kills);
        this.deaths = Math.max(0, deaths);
        this.streak = Math.max(0, streak);
        this.bestStreak = Math.max(0, bestStreak);
        this.arenaJoins = Math.max(0, arenaJoins);
    }

    public static PvpStats starting(int startingElo) {
        return new PvpStats(startingElo, 1, 0, 0, 0, 0, 0, 0);
    }

    public int getElo() {
        return elo;
    }

    public int getLevel() {
        return level;
    }

    public int getXp() {
        return xp;
    }

    public int getKills() {
        return kills;
    }

    public int getDeaths() {
        return deaths;
    }

    public int getStreak() {
        return streak;
    }

    public int getBestStreak() {
        return bestStreak;
    }

    public int getArenaJoins() {
        return arenaJoins;
    }

    /** Kills over deaths, counting a death-free record as its own kill count. */
    public double getKillDeathRatio() {
        return deaths <= 0 ? kills : (double) kills / (double) deaths;
    }

    public PvpStats withElo(int newElo) {
        return new PvpStats(newElo, level, xp, kills, deaths, streak, bestStreak, arenaJoins);
    }

    public PvpStats withProgress(int newLevel, int newXp) {
        return new PvpStats(elo, newLevel, newXp, kills, deaths, streak, bestStreak, arenaJoins);
    }

    public PvpStats recordKill() {
        int newStreak = streak + 1;
        return new PvpStats(elo, level, xp, kills + 1, deaths, newStreak,
                Math.max(bestStreak, newStreak), arenaJoins);
    }

    public PvpStats recordDeath() {
        return new PvpStats(elo, level, xp, kills, deaths + 1, 0, bestStreak, arenaJoins);
    }

    public PvpStats recordArenaJoin() {
        return new PvpStats(elo, level, xp, kills, deaths, streak, bestStreak, arenaJoins + 1);
    }
}
