package com.bx.ultimateDonutSmp.models;

import java.util.UUID;

/**
 * One ranked 1v1 match: who fought, how it went, and what it cost them.
 *
 * <p>The per-player counters live here rather than in {@link PvpStats} because they describe a
 * single fight. The history menu reads them straight back out, so a match that has ended is a
 * complete record on its own and never has to be recomputed from the players' totals.</p>
 */
public class PvpMatch {

    /** How a match finished. A match that is still running has no result yet. */
    public enum Result {
        DECIDED,
        DRAW,
        ABORTED
    }

    private final long id;
    private final UUID firstUuid;
    private final UUID secondUuid;
    private final String firstName;
    private final String secondName;
    private final String kitId;
    private final long startedAt;

    private long endedAt;
    private long countdownEndsAt;
    private UUID winnerUuid;
    private Result result;

    private int firstHits;
    private int secondHits;
    private int firstCrystals;
    private int secondCrystals;
    private int firstEloBefore;
    private int secondEloBefore;
    private int firstEloDelta;
    private int secondEloDelta;

    public PvpMatch(
            long id,
            UUID firstUuid,
            String firstName,
            UUID secondUuid,
            String secondName,
            String kitId,
            long startedAt
    ) {
        this.id = id;
        this.firstUuid = firstUuid;
        this.firstName = firstName;
        this.secondUuid = secondUuid;
        this.secondName = secondName;
        this.kitId = kitId;
        this.startedAt = startedAt;
    }

    public long getId() {
        return id;
    }

    public UUID getFirstUuid() {
        return firstUuid;
    }

    public UUID getSecondUuid() {
        return secondUuid;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getSecondName() {
        return secondName;
    }

    public String getKitId() {
        return kitId;
    }

    public long getStartedAt() {
        return startedAt;
    }

    public long getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(long endedAt) {
        this.endedAt = endedAt;
    }

    /** Epoch millis the opening countdown ends. Until then neither player can be damaged. */
    public long getCountdownEndsAt() {
        return countdownEndsAt;
    }

    public void setCountdownEndsAt(long countdownEndsAt) {
        this.countdownEndsAt = countdownEndsAt;
    }

    public boolean isCountingDown(long now) {
        return countdownEndsAt > 0 && now < countdownEndsAt;
    }

    public UUID getWinnerUuid() {
        return winnerUuid;
    }

    public void setWinnerUuid(UUID winnerUuid) {
        this.winnerUuid = winnerUuid;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    /** Seconds the match ran for, counted to whichever end time it has so far. */
    public long getDurationSeconds(long now) {
        long end = endedAt > 0 ? endedAt : now;
        return Math.max(0L, (end - startedAt) / 1000L);
    }

    public boolean involves(UUID uuid) {
        return uuid != null && (uuid.equals(firstUuid) || uuid.equals(secondUuid));
    }

    /** The other player in the match, or null when the uuid is not one of the two. */
    public UUID opponentOf(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        if (uuid.equals(firstUuid)) {
            return secondUuid;
        }
        return uuid.equals(secondUuid) ? firstUuid : null;
    }

    public boolean isFirst(UUID uuid) {
        return firstUuid.equals(uuid);
    }

    public void addHit(UUID uuid) {
        if (isFirst(uuid)) {
            firstHits++;
        } else if (secondUuid.equals(uuid)) {
            secondHits++;
        }
    }

    public void addCrystal(UUID uuid) {
        if (isFirst(uuid)) {
            firstCrystals++;
        } else if (secondUuid.equals(uuid)) {
            secondCrystals++;
        }
    }

    public int getHits(UUID uuid) {
        return isFirst(uuid) ? firstHits : secondHits;
    }

    public int getCrystals(UUID uuid) {
        return isFirst(uuid) ? firstCrystals : secondCrystals;
    }

    public int getFirstHits() {
        return firstHits;
    }

    public int getSecondHits() {
        return secondHits;
    }

    public int getFirstCrystals() {
        return firstCrystals;
    }

    public int getSecondCrystals() {
        return secondCrystals;
    }

    public void setHits(int first, int second) {
        this.firstHits = first;
        this.secondHits = second;
    }

    public void setCrystals(int first, int second) {
        this.firstCrystals = first;
        this.secondCrystals = second;
    }

    public int getEloBefore(UUID uuid) {
        return isFirst(uuid) ? firstEloBefore : secondEloBefore;
    }

    public int getEloDelta(UUID uuid) {
        return isFirst(uuid) ? firstEloDelta : secondEloDelta;
    }

    public int getFirstEloBefore() {
        return firstEloBefore;
    }

    public int getSecondEloBefore() {
        return secondEloBefore;
    }

    public int getFirstEloDelta() {
        return firstEloDelta;
    }

    public int getSecondEloDelta() {
        return secondEloDelta;
    }

    public void setEloBefore(int first, int second) {
        this.firstEloBefore = first;
        this.secondEloBefore = second;
    }

    public void setEloDelta(int first, int second) {
        this.firstEloDelta = first;
        this.secondEloDelta = second;
    }
}
