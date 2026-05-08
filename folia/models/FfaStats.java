package com.bx.ultimateDonutSmp.models;

public class FfaStats {

    private final int wins;
    private final int losses;
    private final int draws;
    private final int matchesPlayed;
    private final int currentStreak;
    private final int bestStreak;

    public FfaStats(int wins, int losses, int draws, int matchesPlayed, int currentStreak, int bestStreak) {
        this.wins = Math.max(0, wins);
        this.losses = Math.max(0, losses);
        this.draws = Math.max(0, draws);
        this.matchesPlayed = Math.max(0, matchesPlayed);
        this.currentStreak = Math.max(0, currentStreak);
        this.bestStreak = Math.max(0, bestStreak);
    }

    public static FfaStats empty() {
        return new FfaStats(0, 0, 0, 0, 0, 0);
    }

    public int getWins() {
        return wins;
    }

    public int getLosses() {
        return losses;
    }

    public int getDraws() {
        return draws;
    }

    public int getMatchesPlayed() {
        return matchesPlayed;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public int getBestStreak() {
        return bestStreak;
    }

    public FfaStats recordWin() {
        int newStreak = currentStreak + 1;
        return new FfaStats(wins + 1, losses, draws, matchesPlayed + 1, newStreak, Math.max(bestStreak, newStreak));
    }

    public FfaStats recordLoss() {
        return new FfaStats(wins, losses + 1, draws, matchesPlayed + 1, 0, bestStreak);
    }

    public FfaStats recordDraw() {
        return new FfaStats(wins, losses, draws + 1, matchesPlayed + 1, 0, bestStreak);
    }
}
