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
    private ThreeChoice paymentsChoice;
    private boolean scoreboardVisible;
    private boolean payAlertsEnabled;
    private boolean hotbarMessagesEnabled;
    private boolean worthDisplayEnabled;
    private boolean clearEntitiesMessagesEnabled;
    private boolean bountyAlertsEnabled;
    private boolean tpaConfirmMenuEnabled;
    private boolean chainmailOnRespawnEnabled;
    private boolean lunarTeammatesEnabled;
    private ThreeChoice tpaRequestsChoice;
    private boolean autoTpaHereEnabled;
    private ThreeChoice tpaHereRequestsChoice;
    private boolean teamInvitesEnabled;
    private boolean mobSpawnEnabled;
    private boolean payConfirmMenuEnabled;
    private boolean totemParticlesEnabled;
    private boolean fastCrystalsEnabled;
    private boolean amethystBreakMessagesEnabled;
    private ThreeChoice privateMessagesChoice;
    private boolean keyAllNotificationsEnabled;
    private boolean duelRequestsEnabled;
    private boolean publicChatEnabled;
    private boolean serverBroadcastsEnabled;
    private boolean auctionNotificationsEnabled;
    private boolean explosionParticlesEnabled;
    private boolean hideAllPlayersEnabled;
    private boolean notificationSoundsEnabled;
    private boolean rtpCoordinatesEnabled;
    private boolean orderNotificationsEnabled;
    private boolean teamChatVisible;
    private boolean duelMusicEnabled;
    private boolean quietSpawnEnabled;
    private boolean nightVisionEnabled;
    private long keyAllRemainingSeconds;
    private long keyAllAnchorPlaytimeSeconds;
    private boolean dirty;

    // New Settings
    private boolean destroyPearlOnDeath;
    private boolean randomizedCoords;
    private TwoChoice deathMessagesChoice;
    private ThreeChoice advancementMessagesChoice;
    private ThreeChoice joinLeaveMessagesChoice;
    private boolean teleportAlertsEnabled;
    private boolean followAlertsEnabled;
    private boolean explosionSoundsEnabled;
    private boolean displayDonutPlusEnabled;
    private long mobSpawnDisabledUntil;
    private long phantomDisabledUntil;

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
        this.paymentsChoice = ThreeChoice.ANYONE;
        this.scoreboardVisible = true;
        this.payAlertsEnabled = true;
        this.hotbarMessagesEnabled = true;
        this.worthDisplayEnabled = true;
        this.clearEntitiesMessagesEnabled = true;
        this.bountyAlertsEnabled = true;
        this.tpaConfirmMenuEnabled = true;
        this.chainmailOnRespawnEnabled = true;
        this.lunarTeammatesEnabled = true;
        this.tpaRequestsChoice = ThreeChoice.ANYONE;
        this.autoTpaHereEnabled = false;
        this.tpaHereRequestsChoice = ThreeChoice.ANYONE;
        this.teamInvitesEnabled = true;
        this.mobSpawnEnabled = true;
        this.payConfirmMenuEnabled = true;
        this.totemParticlesEnabled = true;
        this.fastCrystalsEnabled = true;
        this.amethystBreakMessagesEnabled = true;
        this.privateMessagesChoice = ThreeChoice.ANYONE;
        this.keyAllNotificationsEnabled = true;
        this.duelRequestsEnabled = true;
        this.publicChatEnabled = true;
        this.serverBroadcastsEnabled = true;
        this.auctionNotificationsEnabled = true;
        this.explosionParticlesEnabled = true;
        this.hideAllPlayersEnabled = false;
        this.notificationSoundsEnabled = true;
        this.rtpCoordinatesEnabled = true;
        this.orderNotificationsEnabled = true;
        this.teamChatVisible = true;
        this.duelMusicEnabled = true;
        this.quietSpawnEnabled = false;
        this.nightVisionEnabled = false;
        this.keyAllRemainingSeconds = -1L;
        this.keyAllAnchorPlaytimeSeconds = 0L;
        this.dirty = false;

        // New Settings Defaults
        this.destroyPearlOnDeath = true;
        this.randomizedCoords = false;
        this.deathMessagesChoice = TwoChoice.FRIENDS_FOLLOWED;
        this.advancementMessagesChoice = ThreeChoice.ANYONE;
        this.joinLeaveMessagesChoice = ThreeChoice.ANYONE;
        this.teleportAlertsEnabled = true;
        this.followAlertsEnabled = true;
        this.explosionSoundsEnabled = true;
        this.displayDonutPlusEnabled = true;
        this.mobSpawnDisabledUntil = 0L;
        this.phantomDisabledUntil = 0L;
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
        if (!phantomEnabled) {
            if (phantomDisabledUntil > 0 && System.currentTimeMillis() > phantomDisabledUntil) {
                phantomEnabled = true;
                phantomDisabledUntil = 0L;
                dirty = true;
            }
        }
        return phantomEnabled;
    }

    public void setPhantomEnabled(boolean phantomEnabled) {
        this.phantomEnabled = phantomEnabled;
        dirty = true;
    }

    public long getPhantomDisabledUntil() {
        return phantomDisabledUntil;
    }

    public void setPhantomDisabledUntil(long phantomDisabledUntil) {
        this.phantomDisabledUntil = phantomDisabledUntil;
        dirty = true;
    }

    public boolean isPaymentsEnabled() {
        return paymentsChoice != ThreeChoice.OFF;
    }

    public void setPaymentsEnabled(boolean paymentsEnabled) {
        this.paymentsChoice = paymentsEnabled ? ThreeChoice.ANYONE : ThreeChoice.OFF;
        dirty = true;
    }

    public ThreeChoice getPaymentsChoice() {
        return paymentsChoice;
    }

    public void setPaymentsChoice(ThreeChoice paymentsChoice) {
        this.paymentsChoice = paymentsChoice;
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
        return tpaRequestsChoice != ThreeChoice.OFF;
    }

    public void setTpaRequestsEnabled(boolean tpaRequestsEnabled) {
        this.tpaRequestsChoice = tpaRequestsEnabled ? ThreeChoice.ANYONE : ThreeChoice.OFF;
        dirty = true;
    }

    public ThreeChoice getTpaRequestsChoice() {
        return tpaRequestsChoice;
    }

    public void setTpaRequestsChoice(ThreeChoice tpaRequestsChoice) {
        this.tpaRequestsChoice = tpaRequestsChoice;
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
        return tpaHereRequestsChoice != ThreeChoice.OFF;
    }

    public void setTpaHereRequestsEnabled(boolean tpaHereRequestsEnabled) {
        this.tpaHereRequestsChoice = tpaHereRequestsEnabled ? ThreeChoice.ANYONE : ThreeChoice.OFF;
        dirty = true;
    }

    public ThreeChoice getTpaHereRequestsChoice() {
        return tpaHereRequestsChoice;
    }

    public void setTpaHereRequestsChoice(ThreeChoice tpaHereRequestsChoice) {
        this.tpaHereRequestsChoice = tpaHereRequestsChoice;
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
        if (!mobSpawnEnabled) {
            if (mobSpawnDisabledUntil > 0 && System.currentTimeMillis() > mobSpawnDisabledUntil) {
                mobSpawnEnabled = true;
                mobSpawnDisabledUntil = 0L;
                dirty = true;
            }
        }
        return mobSpawnEnabled;
    }

    public void setMobSpawnEnabled(boolean mobSpawnEnabled) {
        this.mobSpawnEnabled = mobSpawnEnabled;
        dirty = true;
    }

    public long getMobSpawnDisabledUntil() {
        return mobSpawnDisabledUntil;
    }

    public void setMobSpawnDisabledUntil(long mobSpawnDisabledUntil) {
        this.mobSpawnDisabledUntil = mobSpawnDisabledUntil;
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
        return privateMessagesChoice != ThreeChoice.OFF;
    }

    public void setPrivateMessagesEnabled(boolean privateMessagesEnabled) {
        this.privateMessagesChoice = privateMessagesEnabled ? ThreeChoice.ANYONE : ThreeChoice.OFF;
        dirty = true;
    }

    public ThreeChoice getPrivateMessagesChoice() {
        return privateMessagesChoice;
    }

    public void setPrivateMessagesChoice(ThreeChoice privateMessagesChoice) {
        this.privateMessagesChoice = privateMessagesChoice;
        dirty = true;
    }

    public boolean isKeyAllNotificationsEnabled() {
        return keyAllNotificationsEnabled;
    }

    public void setKeyAllNotificationsEnabled(boolean keyAllNotificationsEnabled) {
        this.keyAllNotificationsEnabled = keyAllNotificationsEnabled;
        dirty = true;
    }

    public boolean isDuelRequestsEnabled() {
        return duelRequestsEnabled;
    }

    public void setDuelRequestsEnabled(boolean duelRequestsEnabled) {
        this.duelRequestsEnabled = duelRequestsEnabled;
        dirty = true;
    }

    public boolean isPublicChatEnabled() {
        return publicChatEnabled;
    }

    public void setPublicChatEnabled(boolean publicChatEnabled) {
        this.publicChatEnabled = publicChatEnabled;
        dirty = true;
    }

    public boolean isServerBroadcastsEnabled() {
        return serverBroadcastsEnabled;
    }

    public void setServerBroadcastsEnabled(boolean serverBroadcastsEnabled) {
        this.serverBroadcastsEnabled = serverBroadcastsEnabled;
        dirty = true;
    }

    public boolean isAuctionNotificationsEnabled() {
        return auctionNotificationsEnabled;
    }

    public void setAuctionNotificationsEnabled(boolean auctionNotificationsEnabled) {
        this.auctionNotificationsEnabled = auctionNotificationsEnabled;
        dirty = true;
    }

    public boolean isExplosionParticlesEnabled() {
        return explosionParticlesEnabled;
    }

    public void setExplosionParticlesEnabled(boolean explosionParticlesEnabled) {
        this.explosionParticlesEnabled = explosionParticlesEnabled;
        dirty = true;
    }

    public boolean isHideAllPlayersEnabled() {
        return hideAllPlayersEnabled;
    }

    public void setHideAllPlayersEnabled(boolean hideAllPlayersEnabled) {
        this.hideAllPlayersEnabled = hideAllPlayersEnabled;
        dirty = true;
    }

    public boolean isNotificationSoundsEnabled() {
        return notificationSoundsEnabled;
    }

    public void setNotificationSoundsEnabled(boolean notificationSoundsEnabled) {
        this.notificationSoundsEnabled = notificationSoundsEnabled;
        dirty = true;
    }

    public boolean isRtpCoordinatesEnabled() {
        return rtpCoordinatesEnabled;
    }

    public void setRtpCoordinatesEnabled(boolean rtpCoordinatesEnabled) {
        this.rtpCoordinatesEnabled = rtpCoordinatesEnabled;
        dirty = true;
    }

    public boolean isOrderNotificationsEnabled() {
        return orderNotificationsEnabled;
    }

    public void setOrderNotificationsEnabled(boolean orderNotificationsEnabled) {
        this.orderNotificationsEnabled = orderNotificationsEnabled;
        dirty = true;
    }

    public boolean isTeamChatVisible() {
        return teamChatVisible;
    }

    public void setTeamChatVisible(boolean teamChatVisible) {
        this.teamChatVisible = teamChatVisible;
        dirty = true;
    }

    public boolean isDuelMusicEnabled() {
        return duelMusicEnabled;
    }

    public void setDuelMusicEnabled(boolean duelMusicEnabled) {
        this.duelMusicEnabled = duelMusicEnabled;
        dirty = true;
    }

    public boolean isQuietSpawnEnabled() {
        return quietSpawnEnabled;
    }

    public void setQuietSpawnEnabled(boolean quietSpawnEnabled) {
        this.quietSpawnEnabled = quietSpawnEnabled;
        dirty = true;
    }

    public boolean isNightVisionEnabled() {
        return nightVisionEnabled;
    }

    public void setNightVisionEnabled(boolean nightVisionEnabled) {
        this.nightVisionEnabled = nightVisionEnabled;
        dirty = true;
    }

    public long getKeyAllRemainingSeconds() {
        if (keyAllRemainingSeconds < 0L) {
            return keyAllRemainingSeconds;
        }
        // count down off the same clock as playtime so the scoreboard lines stay in sync
        long elapsed = getTotalPlaytimeSeconds() - keyAllAnchorPlaytimeSeconds;
        if (elapsed < 0L) {
            elapsed = 0L;
        }
        long remaining = keyAllRemainingSeconds - elapsed;
        return remaining < 0L ? 0L : remaining;
    }

    public void setKeyAllRemainingSeconds(long keyAllRemainingSeconds) {
        this.keyAllRemainingSeconds = Math.max(-1L, keyAllRemainingSeconds);
        this.keyAllAnchorPlaytimeSeconds = getTotalPlaytimeSeconds();
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
        this.keyAllAnchorPlaytimeSeconds = 0L;
        this.dirty = true;
    }

    public boolean isDestroyPearlOnDeath() {
        return destroyPearlOnDeath;
    }

    public void setDestroyPearlOnDeath(boolean destroyPearlOnDeath) {
        this.destroyPearlOnDeath = destroyPearlOnDeath;
        dirty = true;
    }

    public boolean isRandomizedCoords() {
        return randomizedCoords;
    }

    public void setRandomizedCoords(boolean randomizedCoords) {
        this.randomizedCoords = randomizedCoords;
        dirty = true;
    }

    private transient int randomOffsetX = 0;
    private transient int randomOffsetZ = 0;
    private transient boolean randomOffsetInitialized = false;

    private synchronized void ensureRandomOffset() {
        if (!randomOffsetInitialized) {
            java.util.concurrent.ThreadLocalRandom rng = java.util.concurrent.ThreadLocalRandom.current();
            randomOffsetX = (rng.nextBoolean() ? 1 : -1) * rng.nextInt(1000, 50000);
            randomOffsetZ = (rng.nextBoolean() ? 1 : -1) * rng.nextInt(1000, 50000);
            randomOffsetInitialized = true;
        }
    }

    public void setRandomOffsetForTesting(int offsetX, int offsetZ) {
        this.randomOffsetX = offsetX;
        this.randomOffsetZ = offsetZ;
        this.randomOffsetInitialized = true;
    }

    public int getDisplayX(int actualX) {
        if (!randomizedCoords) return actualX;
        ensureRandomOffset();
        return actualX + randomOffsetX;
    }

    public int getDisplayY(int actualY) {
        return actualY;
    }

    public int getDisplayZ(int actualZ) {
        if (!randomizedCoords) return actualZ;
        ensureRandomOffset();
        return actualZ + randomOffsetZ;
    }

    public String getDisplayCoords(org.bukkit.Location loc) {
        if (loc == null) return "0, 0, 0";
        int x = getDisplayX(loc.getBlockX());
        int y = getDisplayY(loc.getBlockY());
        int z = getDisplayZ(loc.getBlockZ());
        return x + ", " + y + ", " + z;
    }

    public TwoChoice getDeathMessagesChoice() {
        return deathMessagesChoice;
    }

    public void setDeathMessagesChoice(TwoChoice deathMessagesChoice) {
        this.deathMessagesChoice = deathMessagesChoice;
        dirty = true;
    }

    public ThreeChoice getAdvancementMessagesChoice() {
        return advancementMessagesChoice;
    }

    public void setAdvancementMessagesChoice(ThreeChoice advancementMessagesChoice) {
        this.advancementMessagesChoice = advancementMessagesChoice;
        dirty = true;
    }

    public ThreeChoice getJoinLeaveMessagesChoice() {
        return joinLeaveMessagesChoice;
    }

    public void setJoinLeaveMessagesChoice(ThreeChoice joinLeaveMessagesChoice) {
        this.joinLeaveMessagesChoice = joinLeaveMessagesChoice;
        dirty = true;
    }

    public boolean isTeleportAlertsEnabled() {
        return teleportAlertsEnabled;
    }

    public void setTeleportAlertsEnabled(boolean teleportAlertsEnabled) {
        this.teleportAlertsEnabled = teleportAlertsEnabled;
        dirty = true;
    }

    public boolean isFollowAlertsEnabled() {
        return followAlertsEnabled;
    }

    public void setFollowAlertsEnabled(boolean followAlertsEnabled) {
        this.followAlertsEnabled = followAlertsEnabled;
        dirty = true;
    }

    public boolean isExplosionSoundsEnabled() {
        return explosionSoundsEnabled;
    }

    public void setExplosionSoundsEnabled(boolean explosionSoundsEnabled) {
        this.explosionSoundsEnabled = explosionSoundsEnabled;
        dirty = true;
    }

    public boolean isDisplayDonutPlusEnabled() {
        return displayDonutPlusEnabled;
    }

    public void setDisplayDonutPlusEnabled(boolean displayDonutPlusEnabled) {
        this.displayDonutPlusEnabled = displayDonutPlusEnabled;
        dirty = true;
    }
}
