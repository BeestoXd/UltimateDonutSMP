package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.menus.TeamMenu;
import com.bx.ultimateDonutSmp.models.Team;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class TeamManager {

    private final UltimateDonutSmp plugin;
    private final Map<String, Team> teams = new HashMap<>();
    private final Map<UUID, String> playerTeamMap = new HashMap<>();
    private final Map<UUID, List<String>> pendingInvites = new HashMap<>();
    private final Set<UUID> teamChatEnabled = new HashSet<>();
    private final Map<UUID, PendingTeamSearch> pendingSearchInputs = new HashMap<>();
    private final Map<UUID, String> activeSearchQueries = new HashMap<>();

    public TeamManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public void loadAll() {
        resetRuntimeState();

        List<Team> loaded = plugin.getDatabaseManager().loadAllTeams();
        for (Team team : loaded) {
            String internalName = team.getName().toLowerCase();
            teams.put(internalName, team);
            for (UUID uuid : team.getMemberUuids()) {
                playerTeamMap.put(uuid, internalName);
            }
        }
    }

    public Team getTeam(String name) {
        if (name == null) return null;
        return teams.get(name.toLowerCase());
    }

    public Team getTeam(Player player) {
        if (player == null) return null;
        return getTeam(player.getUniqueId());
    }

    public Team getTeam(UUID uuid) {
        if (uuid == null) return null;
        String internalName = playerTeamMap.get(uuid);
        return internalName != null ? teams.get(internalName) : null;
    }

    public String getTeamName(Player player) {
        Team team = getTeam(player);
        return team != null ? team.getName() : null;
    }

    public boolean isInTeam(Player player) {
        return player != null && isInTeam(player.getUniqueId());
    }

    public boolean isInTeam(UUID uuid) {
        return uuid != null && playerTeamMap.containsKey(uuid);
    }

    public boolean teamExists(String name) {
        return name != null && teams.containsKey(name.toLowerCase());
    }

    public Collection<Team> getAllTeams() {
        return teams.values();
    }

    public boolean areTeammates(UUID first, UUID second) {
        if (first == null || second == null) return false;
        if (first.equals(second)) return true;

        String firstTeam = playerTeamMap.get(first);
        String secondTeam = playerTeamMap.get(second);
        return firstTeam != null && firstTeam.equals(secondTeam);
    }

    public boolean isValidName(String name) {
        int min = plugin.getConfigManager().getConfig().getInt("TEAM.NAME-MIN-LENGTH", 3);
        int max = plugin.getConfigManager().getConfig().getInt("TEAM.NAME-MAX-LENGTH", 5);
        return name != null
                && name.length() >= min
                && name.length() <= max
                && name.matches("[a-zA-Z0-9_]+");
    }

    public boolean createTeam(Player leader, String name) {
        if (leader == null || teamExists(name) || isInTeam(leader)) {
            return false;
        }

        Team team = new Team(name, leader.getUniqueId());
        team.addMember(leader.getUniqueId());
        String internalName = name.toLowerCase();
        teams.put(internalName, team);
        playerTeamMap.put(leader.getUniqueId(), internalName);
        save(team);
        refreshTablist(leader.getUniqueId());
        refreshRichPresence(leader.getUniqueId());
        return true;
    }

    public void disbandTeam(Team team) {
        if (team == null) return;

        List<UUID> affectedMembers = new ArrayList<>(team.getMemberUuids());
        for (UUID uuid : affectedMembers) {
            playerTeamMap.remove(uuid);
            setTeamChat(uuid, false);
            clearSearchState(uuid);

            Player member = Bukkit.getPlayer(uuid);
            if (member != null) {
                member.sendMessage(ColorUtils.toComponent(
                        plugin.getConfigManager().getMessage("TEAM.TEAM-DISBANDED")));
            }
        }

        teams.remove(team.getName().toLowerCase());
        plugin.getDatabaseManager().deleteTeam(team.getName());
        refreshTablist(affectedMembers);
        refreshRichPresence(affectedMembers);
    }

    public void sendInvite(Player inviter, Player target) {
        Team team = getTeam(inviter);
        if (team == null || target == null) return;

        String internalName = team.getName().toLowerCase();
        List<String> invites = pendingInvites.computeIfAbsent(target.getUniqueId(), ignored -> new ArrayList<>());
        if (!invites.contains(internalName)) {
            invites.add(internalName);
        }

        plugin.getSpigotScheduler().runEntityLater(target, () -> {
            List<String> pending = pendingInvites.get(target.getUniqueId());
            if (pending == null) return;

            pending.remove(internalName);
            if (pending.isEmpty()) {
                pendingInvites.remove(target.getUniqueId());
            }
        }, 60 * 20L);
    }

    public boolean hasInviteFrom(Player target, String teamName) {
        if (target == null || teamName == null) return false;
        List<String> invites = pendingInvites.get(target.getUniqueId());
        return invites != null && invites.contains(teamName.toLowerCase());
    }

    public List<String> getPendingInvites(UUID uuid) {
        List<String> invites = pendingInvites.get(uuid);
        return invites == null ? Collections.emptyList() : List.copyOf(invites);
    }

    public void removeInvite(UUID uuid, String teamName) {
        if (uuid == null || teamName == null) return;

        List<String> invites = pendingInvites.get(uuid);
        if (invites == null) return;

        invites.remove(teamName.toLowerCase());
        if (invites.isEmpty()) {
            pendingInvites.remove(uuid);
        }
    }

    public boolean joinTeam(Player player, String teamName) {
        Team team = getTeam(teamName);
        if (player == null || team == null || isInTeam(player)) {
            return false;
        }

        int maxMembers = plugin.getConfigManager().getConfig().getInt("TEAM.LIMIT-MEMBERS", 10);
        if (team.getMemberCount() >= maxMembers) {
            return false;
        }

        team.addMember(player.getUniqueId());
        playerTeamMap.put(player.getUniqueId(), team.getName().toLowerCase());
        removeInvite(player.getUniqueId(), team.getName());
        save(team);
        refreshTablist(player.getUniqueId());
        refreshRichPresence(team.getMemberUuids());
        return true;
    }

    public void leaveTeam(Player player) {
        Team team = getTeam(player);
        if (team == null) return;

        if (team.isLeader(player.getUniqueId())) {
            disbandTeam(team);
            return;
        }

        List<UUID> affectedMembers = new ArrayList<>(team.getMemberUuids());
        team.removeMember(player.getUniqueId());
        playerTeamMap.remove(player.getUniqueId());
        setTeamChat(player.getUniqueId(), false);
        clearSearchState(player.getUniqueId());
        save(team);
        refreshTablist(player.getUniqueId());
        refreshRichPresence(affectedMembers);
    }

    public boolean kickMember(Team team, UUID targetUuid) {
        if (team == null || targetUuid == null) return false;
        if (!team.isMember(targetUuid) || team.isLeader(targetUuid)) return false;

        List<UUID> affectedMembers = new ArrayList<>(team.getMemberUuids());
        team.removeMember(targetUuid);
        playerTeamMap.remove(targetUuid);
        setTeamChat(targetUuid, false);
        clearSearchState(targetUuid);

        Player target = Bukkit.getPlayer(targetUuid);
        if (target != null) {
            target.sendMessage(ColorUtils.toComponent(
                    plugin.getConfigManager().getMessage("TEAM.KICKED-FROM-TEAM")));
        }

        save(team);
        refreshTablist(targetUuid);
        refreshRichPresence(affectedMembers);
        return true;
    }

    public boolean canManageTeammates(Team team, UUID uuid) {
        return hasPermission(team, uuid, Team.TeamMember::canManageTeammates);
    }

    public boolean canEditHome(Team team, UUID uuid) {
        return hasPermission(team, uuid, Team.TeamMember::canEditHome);
    }

    public boolean canVisitHome(Team team, UUID uuid) {
        return hasPermission(team, uuid, Team.TeamMember::canVisitHome);
    }

    public boolean canUseTeamChat(Team team, UUID uuid) {
        return hasPermission(team, uuid, Team.TeamMember::canUseTeamChat);
    }

    public boolean canTogglePvp(Team team, UUID uuid) {
        return hasPermission(team, uuid, Team.TeamMember::canTogglePvp);
    }

    public boolean toggleFriendlyFire(Team team, UUID actorUuid) {
        if (!canTogglePvp(team, actorUuid)) {
            return false;
        }

        team.setFriendlyFireEnabled(!team.isFriendlyFireEnabled());
        save(team);
        return true;
    }

    public void setFriendlyFire(Team team, boolean enabled) {
        if (team == null) return;
        team.setFriendlyFireEnabled(enabled);
        save(team);
    }

    public void save(Team team) {
        if (team == null) return;
        plugin.getDatabaseManager().saveTeam(team);
    }

    public boolean isTeamChatEnabled(UUID uuid) {
        return teamChatEnabled.contains(uuid);
    }

    public void setTeamChat(UUID uuid, boolean enabled) {
        if (uuid == null) return;
        if (enabled) {
            teamChatEnabled.add(uuid);
        } else {
            teamChatEnabled.remove(uuid);
        }
    }

    public void toggleTeamChat(UUID uuid) {
        if (uuid == null) return;
        if (!teamChatEnabled.remove(uuid)) {
            teamChatEnabled.add(uuid);
        }
    }

    public void promptTeamSearch(Player player, int currentPage, TeamMenu.SortMode sortMode) {
        if (player == null) return;

        pendingSearchInputs.put(player.getUniqueId(), new PendingTeamSearch(currentPage, sortMode));
        player.closeInventory();

        String currentQuery = getActiveSearchQuery(player.getUniqueId());
        if (currentQuery == null || currentQuery.isBlank()) {
            player.sendMessage(ColorUtils.toComponent(
                    "&7Type a team member name in chat to search. Type &ccancel &7to abort."));
            return;
        }

        player.sendMessage(ColorUtils.toComponent(
                "&7Type a team member name in chat to search. Type &ccancel &7to abort or &cclear &7to reset the filter."));
        player.sendMessage(ColorUtils.toComponent("&7Current filter: &f" + currentQuery));
    }

    public boolean hasPendingSearchInput(UUID uuid) {
        return pendingSearchInputs.containsKey(uuid);
    }

    public void handlePendingSearchInput(Player player, String rawInput) {
        if (player == null) return;

        PendingTeamSearch pending = pendingSearchInputs.get(player.getUniqueId());
        if (pending == null) return;

        String input = rawInput == null ? "" : rawInput.trim();
        if (input.equalsIgnoreCase("cancel")) {
            pendingSearchInputs.remove(player.getUniqueId());
            player.sendMessage(ColorUtils.toComponent("&7Team member search cancelled."));
            new TeamMenu(plugin).withState(pending.page(), pending.sortMode(), getActiveSearchQuery(player.getUniqueId())).open(player);
            return;
        }

        if (input.equalsIgnoreCase("clear")) {
            pendingSearchInputs.remove(player.getUniqueId());
            activeSearchQueries.remove(player.getUniqueId());
            player.sendMessage(ColorUtils.toComponent("&7Team member search cleared."));
            new TeamMenu(plugin).withState(0, pending.sortMode(), null).open(player);
            return;
        }

        if (input.isBlank()) {
            player.sendMessage(ColorUtils.toComponent("&cSearch query cannot be empty."));
            player.sendMessage(ColorUtils.toComponent("&7Type a team member name or &ccancel&7."));
            return;
        }

        pendingSearchInputs.remove(player.getUniqueId());
        activeSearchQueries.put(player.getUniqueId(), input);
        player.sendMessage(ColorUtils.toComponent("&7Searching team members for &f" + input + "&7."));
        new TeamMenu(plugin).withState(0, pending.sortMode(), input).open(player);
    }

    public String getActiveSearchQuery(UUID uuid) {
        String query = activeSearchQueries.get(uuid);
        return query == null || query.isBlank() ? null : query;
    }

    public void clearSearchState(UUID uuid) {
        if (uuid == null) return;
        pendingSearchInputs.remove(uuid);
        activeSearchQueries.remove(uuid);
    }

    public void resetRuntimeState() {
        teams.clear();
        playerTeamMap.clear();
        pendingInvites.clear();
        teamChatEnabled.clear();
        pendingSearchInputs.clear();
        activeSearchQueries.clear();
    }

    private boolean hasPermission(Team team, UUID uuid, PermissionAccessor accessor) {
        if (team == null || uuid == null || !team.isMember(uuid)) {
            return false;
        }
        if (team.isLeader(uuid)) {
            return true;
        }

        Team.TeamMember member = team.getMember(uuid);
        return member != null && accessor.hasPermission(member);
    }

    private void refreshTablist(UUID uuid) {
        if (uuid == null) return;
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            plugin.getTablistManager().updateTablistName(player);
        }
    }

    private void refreshTablist(Collection<UUID> uuids) {
        for (UUID uuid : uuids) {
            refreshTablist(uuid);
        }
    }

    private void refreshRichPresence(UUID uuid) {
        LunarRichPresenceManager richPresenceManager = plugin.getLunarRichPresenceManager();
        if (richPresenceManager != null) {
            richPresenceManager.refreshPlayer(uuid);
        }
    }

    private void refreshRichPresence(Collection<UUID> uuids) {
        LunarRichPresenceManager richPresenceManager = plugin.getLunarRichPresenceManager();
        if (richPresenceManager != null) {
            richPresenceManager.refreshPlayers(new ArrayList<>(uuids));
        }
    }

    @FunctionalInterface
    private interface PermissionAccessor {
        boolean hasPermission(Team.TeamMember member);
    }

    private record PendingTeamSearch(int page, TeamMenu.SortMode sortMode) {
    }
}
