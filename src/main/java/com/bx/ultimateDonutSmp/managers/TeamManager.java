package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.Team;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TeamManager {

    private final UltimateDonutSmp plugin;
    private final Map<String, Team> teams = new ConcurrentHashMap<>();
    private final Map<UUID, String> playerTeamMap = new ConcurrentHashMap<>();
    private final Map<UUID, List<String>> pendingInvites = new HashMap<>();
    private final Set<UUID> teamChatEnabled = ConcurrentHashMap.newKeySet();
    private final Map<UUID, String> activeSearchQueries = new HashMap<>();

    public TeamManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public static String cleanName(String name) {
        if (name == null) return "";
        return ColorUtils.stripColorCodes(name).trim();
    }

    public void loadAll() {
        resetRuntimeState();

        List<Team> loaded = plugin.getDatabaseManager().loadAllTeams();
        for (Team team : loaded) {
            String internalName = cleanName(team.getName()).toLowerCase(Locale.ROOT);
            teams.put(internalName, team);
            for (UUID uuid : team.getMemberUuids()) {
                playerTeamMap.put(uuid, internalName);
            }
        }
    }

    public Team getTeam(String name) {
        if (name == null || name.isBlank()) return null;
        Team team = teams.get(name.toLowerCase(Locale.ROOT));
        if (team != null) return team;
        String clean = cleanName(name).toLowerCase(Locale.ROOT);
        team = teams.get(clean);
        if (team != null) return team;
        for (Team candidate : teams.values()) {
            if (cleanName(candidate.getName()).equalsIgnoreCase(clean)) {
                return candidate;
            }
        }
        return null;
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
        if (name == null || name.isBlank()) return false;
        if (teams.containsKey(name.toLowerCase(Locale.ROOT))) return true;
        String clean = cleanName(name).toLowerCase(Locale.ROOT);
        if (teams.containsKey(clean)) return true;
        for (Team team : teams.values()) {
            if (cleanName(team.getName()).equalsIgnoreCase(clean)) {
                return true;
            }
        }
        return false;
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

    public boolean isWorldExcluded(String worldName) {
        if (worldName == null || worldName.isBlank()) {
            return false;
        }
        FileConfiguration config = plugin != null && plugin.getConfigManager() != null
                ? plugin.getConfigManager().getConfig()
                : null;
        if (config == null) {
            return false;
        }
        List<String> excluded = config.getStringList("TEAM.EXCLUDED-WORLDS");
        if (excluded.isEmpty()) {
            excluded = config.getStringList("TEAM.HOME-EXCLUDED-WORLDS");
        }
        if (excluded.isEmpty() && plugin.getHomeManager() != null) {
            return plugin.getHomeManager().isWorldExcluded(worldName);
        }
        for (String w : excluded) {
            if (w != null && w.trim().equalsIgnoreCase(worldName.trim())) {
                return true;
            }
        }
        return false;
    }

    public boolean isWorldExcluded(World world) {
        return world != null && isWorldExcluded(world.getName());
    }

    public boolean isValidName(String name) {
        if (name == null || name.isBlank()) {
            return false;
        }
        int min = plugin != null && plugin.getConfigManager() != null && plugin.getConfigManager().getConfig() != null
                ? plugin.getConfigManager().getConfig().getInt("TEAM.NAME-MIN-LENGTH", 3)
                : 3;
        int max = plugin != null && plugin.getConfigManager() != null && plugin.getConfigManager().getConfig() != null
                ? plugin.getConfigManager().getConfig().getInt("TEAM.NAME-MAX-LENGTH", 5)
                : 5;
        String stripped = cleanName(name);
        if (stripped.length() < min || stripped.length() > max || !stripped.matches("[a-zA-Z0-9_]+")) {
            return false;
        }
        String check = ColorUtils.stripColorCodes(name);
        return check.equals(stripped);
    }

    public boolean createTeam(Player leader, String name) {
        if (leader == null || teamExists(name) || isInTeam(leader)) {
            return false;
        }

        Team team = new Team(name, leader.getUniqueId());
        team.addMember(leader.getUniqueId());
        String internalName = cleanName(name).toLowerCase(Locale.ROOT);
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

            if (Bukkit.getServer() != null) {
                Player member = Bukkit.getPlayer(uuid);
                if (member != null && plugin != null && plugin.getConfigManager() != null) {
                    member.sendMessage(ColorUtils.toComponent(
                            plugin.getConfigManager().getMessage("TEAM.TEAM-DISBANDED")));
                }
            }
        }

        teams.remove(cleanName(team.getName()).toLowerCase(Locale.ROOT));
        teams.remove(team.getName().toLowerCase(Locale.ROOT));
        if (plugin != null && plugin.getDatabaseManager() != null) {
            plugin.getDatabaseManager().deleteTeam(team.getName());
        }
        refreshTablist(affectedMembers);
        refreshRichPresence(affectedMembers);
    }

    public void sendInvite(Player inviter, Player target) {
        Team team = getTeam(inviter);
        if (team == null || target == null) return;

        String internalName = cleanName(team.getName()).toLowerCase(Locale.ROOT);
        List<String> invites = pendingInvites.computeIfAbsent(target.getUniqueId(), ignored -> new ArrayList<>());
        if (!invites.contains(internalName)) {
            invites.add(internalName);
        }

        if (plugin != null && plugin.getSpigotScheduler() != null) {
            plugin.getSpigotScheduler().runEntityLater(target, () -> {
                List<String> pending = pendingInvites.get(target.getUniqueId());
                if (pending == null) return;

                pending.remove(internalName);
                if (pending.isEmpty()) {
                    pendingInvites.remove(target.getUniqueId());
                }
            }, 60 * 20L);
        }
    }

    public boolean hasInviteFrom(Player target, String teamName) {
        if (target == null || teamName == null) return false;
        List<String> invites = pendingInvites.get(target.getUniqueId());
        if (invites == null) return false;
        String clean = cleanName(teamName).toLowerCase(Locale.ROOT);
        return invites.contains(clean) || invites.contains(teamName.toLowerCase(Locale.ROOT));
    }

    public List<String> getPendingInvites(UUID uuid) {
        List<String> invites = pendingInvites.get(uuid);
        return invites == null ? Collections.emptyList() : List.copyOf(invites);
    }

    public void removeInvite(UUID uuid, String teamName) {
        if (uuid == null || teamName == null) return;

        List<String> invites = pendingInvites.get(uuid);
        if (invites == null) return;

        invites.remove(teamName.toLowerCase(Locale.ROOT));
        invites.remove(cleanName(teamName).toLowerCase(Locale.ROOT));
        if (invites.isEmpty()) {
            pendingInvites.remove(uuid);
        }
    }

    public boolean joinTeam(Player player, String teamName) {
        Team team = getTeam(teamName);
        if (player == null || team == null || isInTeam(player)) {
            return false;
        }

        int maxMembers = plugin != null && plugin.getConfigManager() != null && plugin.getConfigManager().getConfig() != null
                ? plugin.getConfigManager().getConfig().getInt("TEAM.LIMIT-MEMBERS", 10)
                : 10;
        if (team.getMemberCount() >= maxMembers) {
            return false;
        }

        team.addMember(player.getUniqueId());
        String internalName = cleanName(team.getName()).toLowerCase(Locale.ROOT);
        playerTeamMap.put(player.getUniqueId(), internalName);
        removeInvite(player.getUniqueId(), team.getName());
        removeInvite(player.getUniqueId(), internalName);
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

        if (Bukkit.getServer() != null) {
            Player target = Bukkit.getPlayer(targetUuid);
            if (target != null && plugin != null && plugin.getConfigManager() != null) {
                target.sendMessage(ColorUtils.toComponent(
                        plugin.getConfigManager().getMessage("TEAM.KICKED-FROM-TEAM")));
            }
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
        if (team == null || plugin == null || plugin.getDatabaseManager() == null) return;
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

    public void setSearchQuery(UUID uuid, String query) {
        if (uuid == null) return;
        if (query == null || query.isBlank()) {
            activeSearchQueries.remove(uuid);
            return;
        }
        String normalized = query.trim();
        if (normalized.length() > 64) {
            normalized = normalized.substring(0, 64);
        }
        activeSearchQueries.put(uuid, normalized);
    }

    public String getActiveSearchQuery(UUID uuid) {
        String query = activeSearchQueries.get(uuid);
        return query == null || query.isBlank() ? null : query;
    }

    public void clearSearchState(UUID uuid) {
        if (uuid == null) return;
        activeSearchQueries.remove(uuid);
    }

    public void resetRuntimeState() {
        teams.clear();
        playerTeamMap.clear();
        pendingInvites.clear();
        teamChatEnabled.clear();
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
        if (uuid == null || plugin == null || plugin.getTablistManager() == null || Bukkit.getServer() == null) return;
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
        if (plugin == null) return;
        LunarRichPresenceManager richPresenceManager = plugin.getLunarRichPresenceManager();
        if (richPresenceManager != null) {
            richPresenceManager.refreshPlayer(uuid);
        }
    }

    private void refreshRichPresence(Collection<UUID> uuids) {
        if (plugin == null) return;
        LunarRichPresenceManager richPresenceManager = plugin.getLunarRichPresenceManager();
        if (richPresenceManager != null) {
            richPresenceManager.refreshPlayers(new ArrayList<>(uuids));
        }
    }

    @FunctionalInterface
    private interface PermissionAccessor {
        boolean hasPermission(Team.TeamMember member);
    }
}
