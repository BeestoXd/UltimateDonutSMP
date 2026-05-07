package com.bx.ultimateDonutSmp.models;

import org.bukkit.Location;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class Team {

    private final String name;
    private UUID leaderUuid;
    private Location home;
    private boolean friendlyFireEnabled;
    private final Map<UUID, TeamMember> members = new LinkedHashMap<>();

    public Team(String name, UUID leaderUuid) {
        this.name = name;
        this.leaderUuid = leaderUuid;
        this.friendlyFireEnabled = false;
    }

    // ── Basics ─────────────────────────────────────────────────────────────────

    public String getName() { return name; }

    public UUID getLeaderUuid() { return leaderUuid; }
    public void setLeaderUuid(UUID leaderUuid) { this.leaderUuid = leaderUuid; }

    public Location getHome() { return home; }
    public void setHome(Location home) { this.home = home; }
    public boolean hasHome() { return home != null; }

    public boolean isFriendlyFireEnabled() { return friendlyFireEnabled; }
    public void setFriendlyFireEnabled(boolean friendlyFireEnabled) {
        this.friendlyFireEnabled = friendlyFireEnabled;
    }

    // ── Members ────────────────────────────────────────────────────────────────

    public Map<UUID, TeamMember> getMembers() { return members; }

    public Collection<UUID> getMemberUuids() { return members.keySet(); }

    public int getMemberCount() { return members.size(); }

    public void addMember(UUID uuid) {
        members.put(uuid, new TeamMember(uuid));
    }

    public void removeMember(UUID uuid) {
        members.remove(uuid);
    }

    public boolean isMember(UUID uuid) {
        return members.containsKey(uuid);
    }

    public boolean isLeader(UUID uuid) {
        return leaderUuid != null && leaderUuid.equals(uuid);
    }

    public TeamMember getMember(UUID uuid) {
        return members.get(uuid);
    }

    // ── Inner class ────────────────────────────────────────────────────────────

    public static class TeamMember {
        private final UUID uuid;
        private boolean canEditHome;
        private boolean canManageTeammates;
        private boolean canTogglePvp;
        private boolean canVisitHome;
        private boolean canUseTeamChat;

        public TeamMember(UUID uuid) {
            this.uuid = uuid;
            this.canEditHome = false;
            this.canManageTeammates = false;
            this.canTogglePvp = false;
            this.canVisitHome = true;
            this.canUseTeamChat = true;
        }

        public UUID getUuid() { return uuid; }

        public boolean canEditHome() { return canEditHome; }
        public void setCanEditHome(boolean b) { this.canEditHome = b; }

        public boolean canManageTeammates() { return canManageTeammates; }
        public void setCanManageTeammates(boolean b) { this.canManageTeammates = b; }

        public boolean canTogglePvp() { return canTogglePvp; }
        public void setCanTogglePvp(boolean canTogglePvp) { this.canTogglePvp = canTogglePvp; }

        public boolean canVisitHome() { return canVisitHome; }
        public void setCanVisitHome(boolean canVisitHome) { this.canVisitHome = canVisitHome; }

        public boolean canUseTeamChat() { return canUseTeamChat; }
        public void setCanUseTeamChat(boolean canUseTeamChat) { this.canUseTeamChat = canUseTeamChat; }
    }
}
