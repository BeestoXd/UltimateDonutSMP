package com.bx.ultimateDonutSmp.models;

import java.util.UUID;

public class PlayerData {

    private final UUID uuid;
    private String username;
    private double money;
    private long shards;
    private int kills;
    private int deaths;
    private long playtimeSeconds;
    private long blocksPlaced;
    private long blocksBroken;
    private long mobsKilled;
    private int killStreak;
    private int highestKillStreak;
    private double moneySpent;
    private double moneyMade;
    private long shardBoosterExpiryMillis;
    private long sessionStartMillis;
    private boolean tpauto;
    private boolean phantomEnabled;
    private boolean paymentsEnabled;
    private boolean scoreboardVisible;
    private boolean payAlertsEnabled;
    private boolean hotbarMessagesEnabled;
    private boolean worthDisplayEnabled;
    private boolean clearEntitiesMessagesEnabled;
    private boolean bountyAlertsEnabled;
    private boolean tpaConfirmMenuEnabled;
    private boolean chainmailOnRespawnEnabled;
    private boolean lunarTeammatesEnabled;
    private boolean tpaRequestsEnabled;
    private boolean autoTpaHereEnabled;
    private boolean tpaHereRequestsEnabled;
    private boolean teamInvitesEnabled;
    private boolean mobSpawnEnabled;
    private boolean payConfirmMenuEnabled;
    private boolean totemParticlesEnabled;
    private boolean fastCrystalsEnabled;
    private boolean amethystBreakMessagesEnabled;
    private boolean privateMessagesEnabled;
    private boolean keyAllNotificationsEnabled;
    private long keyAllRemainingSeconds;
    private boolean dirty;

    public PlayerData(UUID uuid, String username) {
        this.uuid = uuid;
        this.username = username;
        this.money = 0;
        this.shards = 0;
        this.kills = 0;
        this.deaths = 0;
        this.playtimeSeconds = 0;
        this.blocksPlaced = 0;
        this.blocksBroken = 0;
        this.mobsKilled = 0;
        this.killStreak = 0;
        this.highestKillStreak = 0;
        this.moneySpent = 0;
        this.moneyMade = 0;
        this.shardBoosterExpiryMillis = 0;
        this.sessionStartMillis = System.currentTimeMillis();
        this.tpauto = false;
        this.phantomEnabled = true;
        this.paymentsEnabled = true;
        this.scoreboardVisible = true;
        this.payAlertsEnabled = true;
        this.hotbarMessagesEnabled = true;
        this.worthDisplayEnabled = true;
        this.clearEntitiesMessagesEnabled = true;
        this.bountyAlertsEnabled = true;
        this.tpaConfirmMenuEnabled = true;
        this.chainmailOnRespawnEnabled = true;
        this.lunarTeammatesEnabled = true;
        this.tpaRequestsEnabled = true;
        this.autoTpaHereEnabled = false;
        this.tpaHereRequestsEnabled = true;
        this.teamInvitesEnabled = true;
        this.mobSpawnEnabled = true;
        this.payConfirmMenuEnabled = true;
        this.totemParticlesEnabled = true;
        this.fastCrystalsEnabled = true;
        this.amethystBreakMessagesEnabled = true;
        this.privateMessagesEnabled = true;
        this.keyAllNotificationsEnabled = true;
        this.keyAllRemainingSeconds = -1L;
        this.dirty = false;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
        dirty = true;
    }

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = Math.max(0, money);
        dirty = true;
    }

    public void addMoney(double amount) {
        setMoney(this.money + amount);
    }

    public boolean removeMoney(double amount) {
        if (this.money < amount) {
            return false;
        }
        setMoney(this.money - amount);
        return true;
    }

    public boolean hasMoney(double amount) {
        return this.money >= amount;
    }

    public long getShards() {
        return shards;
    }

    public void setShards(long shards) {
        this.shards = Math.max(0, shards);
        dirty = true;
    }

    public void addShards(long amount) {
        setShards(this.shards + amount);
    }

    public boolean removeShards(long amount) {
        if (this.shards < amount) {
            return false;
        }
        setShards(this.shards - amount);
        return true;
    }

    public boolean hasShards(long amount) {
        return this.shards >= amount;
    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kills) {
        this.kills = Math.max(0, kills);
        dirty = true;
    }

    public void addKill() {
        this.kills++;
        dirty = true;
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = Math.max(0, deaths);
        dirty = true;
    }

    public void addDeath() {
        this.deaths++;
        dirty = true;
    }

    public long getPlaytimeSeconds() {
        return playtimeSeconds;
    }

    public void setPlaytimeSeconds(long playtimeSeconds) {
        this.playtimeSeconds = Math.max(0, playtimeSeconds);
        dirty = true;
    }

    public long getSessionStartMillis() {
        return sessionStartMillis;
    }

    public void setSessionStartMillis(long sessionStartMillis) {
        this.sessionStartMillis = sessionStartMillis;
    }

    public long getTotalPlaytimeSeconds() {
        return playtimeSeconds + (System.currentTimeMillis() - sessionStartMillis) / 1000L;
    }

    public void commitSession() {
        long now = System.currentTimeMillis();
        playtimeSeconds += (now - sessionStartMillis) / 1000L;
        sessionStartMillis = now;
        dirty = true;
    }

    public long getBlocksPlaced() {
        return blocksPlaced;
    }

    public void setBlocksPlaced(long blocksPlaced) {
        this.blocksPlaced = Math.max(0, blocksPlaced);
        dirty = true;
    }

    public void addBlocksPlaced(long amount) {
        setBlocksPlaced(this.blocksPlaced + amount);
    }

    public long getBlocksBroken() {
        return blocksBroken;
    }

    public void setBlocksBroken(long blocksBroken) {
        this.blocksBroken = Math.max(0, blocksBroken);
        dirty = true;
    }

    public void addBlocksBroken(long amount) {
        setBlocksBroken(this.blocksBroken + amount);
    }

    public long getMobsKilled() {
        return mobsKilled;
    }

    public void setMobsKilled(long mobsKilled) {
        this.mobsKilled = Math.max(0, mobsKilled);
        dirty = true;
    }

    public void addMobKill() {
        setMobsKilled(this.mobsKilled + 1);
    }

    public int getKillStreak() {
        return killStreak;
    }

    public void setKillStreak(int killStreak) {
        this.killStreak = Math.max(0, killStreak);
        dirty = true;
    }

    public int getHighestKillStreak() {
        return highestKillStreak;
    }

    public void setHighestKillStreak(int highestKillStreak) {
        this.highestKillStreak = Math.max(0, highestKillStreak);
        dirty = true;
    }

    public void addKillStreak() {
        this.killStreak++;
        if (this.killStreak > this.highestKillStreak) {
            this.highestKillStreak = this.killStreak;
        }
        dirty = true;
    }

    public void resetKillStreak() {
        this.killStreak = 0;
        dirty = true;
    }

    public double getMoneySpent() {
        return moneySpent;
    }

    public void setMoneySpent(double moneySpent) {
        this.moneySpent = Math.max(0, moneySpent);
        dirty = true;
    }

    public void addMoneySpent(double amount) {
        setMoneySpent(this.moneySpent + amount);
    }

    public double getMoneyMade() {
        return moneyMade;
    }

    public void setMoneyMade(double moneyMade) {
        this.moneyMade = Math.max(0, moneyMade);
        dirty = true;
    }

    public void addMoneyMade(double amount) {
        setMoneyMade(this.moneyMade + amount);
    }

    public long getShardBoosterExpiryMillis() {
        return shardBoosterExpiryMillis;
    }

    public void setShardBoosterExpiryMillis(long shardBoosterExpiryMillis) {
        this.shardBoosterExpiryMillis = Math.max(0L, shardBoosterExpiryMillis);
        dirty = true;
    }

    public boolean hasActiveShardBooster() {
        return shardBoosterExpiryMillis > System.currentTimeMillis();
    }

    public boolean isTpauto() {
        return tpauto;
    }

    public void setTpauto(boolean tpauto) {
        this.tpauto = tpauto;
        dirty = true;
    }

    public boolean isPhantomEnabled() {
        return phantomEnabled;
    }

    public void setPhantomEnabled(boolean phantomEnabled) {
        this.phantomEnabled = phantomEnabled;
        dirty = true;
    }

    public boolean isPaymentsEnabled() {
        return paymentsEnabled;
    }

    public void setPaymentsEnabled(boolean paymentsEnabled) {
        this.paymentsEnabled = paymentsEnabled;
        dirty = true;
    }

    public boolean isScoreboardVisible() {
        return scoreboardVisible;
    }

    public void setScoreboardVisible(boolean scoreboardVisible) {
        this.scoreboardVisible = scoreboardVisible;
        dirty = true;
    }

    public boolean isPayAlertsEnabled() {
        return payAlertsEnabled;
    }

    public void setPayAlertsEnabled(boolean payAlertsEnabled) {
        this.payAlertsEnabled = payAlertsEnabled;
        dirty = true;
    }

    public boolean isHotbarMessagesEnabled() {
        return hotbarMessagesEnabled;
    }

    public void setHotbarMessagesEnabled(boolean hotbarMessagesEnabled) {
        this.hotbarMessagesEnabled = hotbarMessagesEnabled;
        dirty = true;
    }

    public boolean isWorthDisplayEnabled() {
        return worthDisplayEnabled;
    }

    public void setWorthDisplayEnabled(boolean worthDisplayEnabled) {
        this.worthDisplayEnabled = worthDisplayEnabled;
        dirty = true;
    }

    public boolean isClearEntitiesMessagesEnabled() {
        return clearEntitiesMessagesEnabled;
    }

    public void setClearEntitiesMessagesEnabled(boolean clearEntitiesMessagesEnabled) {
        this.clearEntitiesMessagesEnabled = clearEntitiesMessagesEnabled;
        dirty = true;
    }

    public boolean isBountyAlertsEnabled() {
        return bountyAlertsEnabled;
    }

    public void setBountyAlertsEnabled(boolean bountyAlertsEnabled) {
        this.bountyAlertsEnabled = bountyAlertsEnabled;
        dirty = true;
    }

    public boolean isTpaConfirmMenuEnabled() {
        return tpaConfirmMenuEnabled;
    }

    public void setTpaConfirmMenuEnabled(boolean tpaConfirmMenuEnabled) {
        this.tpaConfirmMenuEnabled = tpaConfirmMenuEnabled;
        dirty = true;
    }

    public boolean isChainmailOnRespawnEnabled() {
        return chainmailOnRespawnEnabled;
    }

    public void setChainmailOnRespawnEnabled(boolean chainmailOnRespawnEnabled) {
        this.chainmailOnRespawnEnabled = chainmailOnRespawnEnabled;
        dirty = true;
    }

    public boolean isLunarTeammatesEnabled() {
        return lunarTeammatesEnabled;
    }

    public void setLunarTeammatesEnabled(boolean lunarTeammatesEnabled) {
        this.lunarTeammatesEnabled = lunarTeammatesEnabled;
        dirty = true;
    }

    public boolean isTpaRequestsEnabled() {
        return tpaRequestsEnabled;
    }

    public void setTpaRequestsEnabled(boolean tpaRequestsEnabled) {
        this.tpaRequestsEnabled = tpaRequestsEnabled;
        dirty = true;
    }

    public boolean isAutoTpaHereEnabled() {
        return autoTpaHereEnabled;
    }

    public void setAutoTpaHereEnabled(boolean autoTpaHereEnabled) {
        this.autoTpaHereEnabled = autoTpaHereEnabled;
        dirty = true;
    }

    public boolean isTpaHereRequestsEnabled() {
        return tpaHereRequestsEnabled;
    }

    public void setTpaHereRequestsEnabled(boolean tpaHereRequestsEnabled) {
        this.tpaHereRequestsEnabled = tpaHereRequestsEnabled;
        dirty = true;
    }

    public boolean isTeamInvitesEnabled() {
        return teamInvitesEnabled;
    }

    public void setTeamInvitesEnabled(boolean teamInvitesEnabled) {
        this.teamInvitesEnabled = teamInvitesEnabled;
        dirty = true;
    }

    public boolean isMobSpawnEnabled() {
        return mobSpawnEnabled;
    }

    public void setMobSpawnEnabled(boolean mobSpawnEnabled) {
        this.mobSpawnEnabled = mobSpawnEnabled;
        dirty = true;
    }

    public boolean isPayConfirmMenuEnabled() {
        return payConfirmMenuEnabled;
    }

    public void setPayConfirmMenuEnabled(boolean payConfirmMenuEnabled) {
        this.payConfirmMenuEnabled = payConfirmMenuEnabled;
        dirty = true;
    }

    public boolean isTotemParticlesEnabled() {
        return totemParticlesEnabled;
    }

    public void setTotemParticlesEnabled(boolean totemParticlesEnabled) {
        this.totemParticlesEnabled = totemParticlesEnabled;
        dirty = true;
    }

    public boolean isFastCrystalsEnabled() {
        return fastCrystalsEnabled;
    }

    public void setFastCrystalsEnabled(boolean fastCrystalsEnabled) {
        this.fastCrystalsEnabled = fastCrystalsEnabled;
        dirty = true;
    }

    public boolean isAmethystBreakMessagesEnabled() {
        return amethystBreakMessagesEnabled;
    }

    public void setAmethystBreakMessagesEnabled(boolean amethystBreakMessagesEnabled) {
        this.amethystBreakMessagesEnabled = amethystBreakMessagesEnabled;
        dirty = true;
    }

    public boolean isPrivateMessagesEnabled() {
        return privateMessagesEnabled;
    }

    public void setPrivateMessagesEnabled(boolean privateMessagesEnabled) {
        this.privateMessagesEnabled = privateMessagesEnabled;
        dirty = true;
    }

    public boolean isKeyAllNotificationsEnabled() {
        return keyAllNotificationsEnabled;
    }

    public void setKeyAllNotificationsEnabled(boolean keyAllNotificationsEnabled) {
        this.keyAllNotificationsEnabled = keyAllNotificationsEnabled;
        dirty = true;
    }

    public long getKeyAllRemainingSeconds() {
        return keyAllRemainingSeconds;
    }

    public void setKeyAllRemainingSeconds(long keyAllRemainingSeconds) {
        this.keyAllRemainingSeconds = Math.max(-1L, keyAllRemainingSeconds);
        dirty = true;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public void markDirty() {
        this.dirty = true;
    }

    public void resetTrackedStats(long newSessionStartMillis) {
        this.kills = 0;
        this.deaths = 0;
        this.playtimeSeconds = 0L;
        this.blocksPlaced = 0L;
        this.blocksBroken = 0L;
        this.mobsKilled = 0L;
        this.killStreak = 0;
        this.highestKillStreak = 0;
        this.moneySpent = 0D;
        this.moneyMade = 0D;
        this.sessionStartMillis = newSessionStartMillis;
        this.dirty = true;
    }
}
