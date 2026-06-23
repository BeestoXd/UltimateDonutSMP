package com.bx.ultimateDonutSmp.models;

public class DuelStats {

    private final int wins;
    private final int losses;
    private final int draws;
    private final int currentStreak;
    private final int bestStreak;

    public DuelStats(int wins, int losses, int draws, int currentStreak, int bestStreak) {
        this.wins = Math.max(0, wins);
        this.losses = Math.max(0, losses);
        this.draws = Math.max(0, draws);
        this.currentStreak = Math.max(0, currentStreak);
        this.bestStreak = Math.max(0, bestStreak);
    }

    public static DuelStats empty() {
        return new DuelStats(0, 0, 0, 0, 0);
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

    public int getCurrentStreak() {
        return currentStreak;
    }

    public int getBestStreak() {
        return bestStreak;
    }

    public DuelStats recordWin() {
        int newStreak = currentStreak + 1;
        return new DuelStats(wins + 1, losses, draws, newStreak, Math.max(bestStreak, newStreak));
    }

    public DuelStats recordLoss() {
        return new DuelStats(wins, losses + 1, draws, 0, bestStreak);
    }

    public DuelStats recordDraw() {
        return new DuelStats(wins, losses, draws + 1, 0, bestStreak);
    }
}
