package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.menus.TeamDisbandConfirmMenu;
import com.bx.ultimateDonutSmp.menus.TeamMenu;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.models.Team;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class TeamCommand implements CommandExecutor {

    private final UltimateDonutSmp plugin;

    public TeamCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Player only.");
            return true;
        }

        if (args.length == 0) {
            new TeamMenu(plugin).open(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create" -> handleCreate(player, args);
            case "disband" -> handleDisband(player);
            case "invite" -> handleInvite(player, args);
            case "join" -> handleJoin(player, args);
            case "leave" -> handleLeave(player);
            case "kick" -> handleKick(player, args);
            case "home" -> handleHome(player);
            case "sethome" -> handleSetHome(player);
            case "delhome" -> handleDeleteHome(player);
            case "chat" -> handleChat(player);
            case "pvp" -> handlePvp(player);
            default -> new TeamMenu(plugin).open(player);
        }
        return true;
    }

    private void handleCreate(Player player, String[] args) {
        if (args.length < 2) {
            send(player, "&cUsage: /team create <name>");
            return;
        }

        String name = args[1];
        if (!plugin.getTeamManager().isValidName(name)) {
            send(player, "&cTeam name must be 3-5 alphanumeric characters.");
            return;
        }
        if (plugin.getTeamManager().isInTeam(player)) {
            send(player, plugin.getConfigManager().getMessage("TEAM.ALREADY-IN-TEAM"));
            return;
        }
        if (plugin.getTeamManager().teamExists(name)) {
            send(player, plugin.getConfigManager().getMessage("TEAM.ALREADY-EXISTS"));
            return;
        }

        plugin.getTeamManager().createTeam(player, name);
        send(player, plugin.getConfigManager().getMessage("TEAM.TEAM-CREATED"));
    }

    private void handleDisband(Player player) {
        Team team = plugin.getTeamManager().getTeam(player);
        if (team == null) {
            send(player, plugin.getConfigManager().getMessage("TEAM.NO-TEAM"));
            return;
        }
        if (!team.isLeader(player.getUniqueId())) {
            send(player, plugin.getConfigManager().getMessage("TEAM.NOT-LEADER"));
            return;
        }

        new TeamDisbandConfirmMenu(plugin).open(player);
    }

    private void handleInvite(Player player, String[] args) {
        if (args.length < 2) {
            send(player, "&cUsage: /team invite <player>");
            return;
        }

        Team team = plugin.getTeamManager().getTeam(player);
        if (team == null) {
            send(player, plugin.getConfigManager().getMessage("TEAM.NO-TEAM"));
            return;
        }
        if (!plugin.getTeamManager().canManageTeammates(team, player.getUniqueId())) {
            send(player, plugin.getConfigManager().getMessage("TEAM.NO-MANAGE-PERMISSION"));
            return;
        }
        if (args[1].equalsIgnoreCase(player.getName())) {
            send(player, "&cYou cannot invite yourself!");
            return;
        }

        int maxMembers = plugin.getConfigManager().getConfig().getInt("TEAM.LIMIT-MEMBERS", 10);
        if (team.getMemberCount() >= maxMembers) {
            send(player, plugin.getConfigManager().getMessage("TEAM.TEAM-FULL"));
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            send(player, "&cPlayer not online.");
            return;
        }

        PlayerData targetData = plugin.getPlayerDataManager().get(target);
        if (targetData != null && !targetData.isTeamInvitesEnabled()) {
            send(player, plugin.getConfigManager().getMessage("TEAM.PLAYER-NO-INVITES"));
            return;
        }
        if (plugin.getTeamManager().isInTeam(target)) {
            send(player, plugin.getConfigManager().getMessage("TEAM.PLAYER-IN-TEAM", "{player}", target.getName()));
            return;
        }

        plugin.getTeamManager().sendInvite(player, target);
        send(player, plugin.getConfigManager().getMessage("TEAM.INVITE-SENT", "{player}", target.getName()));

        String joinCommand = "/team join " + team.getName();
        Component inviteMessage = ColorUtils.toComponent(
                plugin.getConfigManager().getMessage("TEAM.INVITED-TO-JOIN", "{team}", team.getName()))
                .append(Component.text(" "))
                .append(ColorUtils.toComponent(plugin.getConfigManager().getMessage("TEAM.CLICK-TO-JOIN"))
                        .clickEvent(ClickEvent.runCommand(joinCommand))
                        .hoverEvent(HoverEvent.showText(ColorUtils.toComponent(
                                plugin.getConfigManager().getMessage("TEAM.HOVER-JOIN", "{team}", team.getName())))));
        target.sendMessage(inviteMessage);
    }

    private void handleJoin(Player player, String[] args) {
        if (args.length < 2) {
            send(player, "&cUsage: /team join <team>");
            return;
        }
        if (plugin.getTeamManager().isInTeam(player)) {
            send(player, plugin.getConfigManager().getMessage("TEAM.ALREADY-IN-TEAM"));
            return;
        }

        String teamName = args[1];
        if (!plugin.getTeamManager().hasInviteFrom(player, teamName)) {
            send(player, plugin.getConfigManager().getMessage("TEAM.NO-PENDING-INVITES", "{team}", teamName));
            return;
        }

        boolean joined = plugin.getTeamManager().joinTeam(player, teamName);
        if (!joined) {
            send(player, plugin.getConfigManager().getMessage("TEAM.TEAM-FULL"));
            return;
        }

        Team team = plugin.getTeamManager().getTeam(player);
        String resolvedTeamName = team != null ? team.getName() : teamName;
        send(player, plugin.getConfigManager().getMessage("TEAM.JOIN-SUCCESS", "{team}", resolvedTeamName));

        if (team != null) {
            String broadcast = plugin.getConfigManager().getMessage("TEAM.JOINED-BROADCAST", "{player}", player.getName());
            for (UUID memberUuid : team.getMemberUuids()) {
                Player member = Bukkit.getPlayer(memberUuid);
                if (member != null && !member.getUniqueId().equals(player.getUniqueId())) {
                    member.sendMessage(ColorUtils.toComponent(broadcast));
                }
            }
        }
    }

    private void handleLeave(Player player) {
        if (!plugin.getTeamManager().isInTeam(player)) {
            send(player, plugin.getConfigManager().getMessage("TEAM.NO-TEAM"));
            return;
        }

        Team team = plugin.getTeamManager().getTeam(player);
        if (team != null && team.isLeader(player.getUniqueId())) {
            new TeamDisbandConfirmMenu(plugin).open(player);
            return;
        }

        plugin.getTeamManager().leaveTeam(player);
        send(player, "&7You left the team.");
    }

    private void handleKick(Player player, String[] args) {
        if (args.length < 2) {
            send(player, "&cUsage: /team kick <player>");
            return;
        }

        Team team = plugin.getTeamManager().getTeam(player);
        if (team == null) {
            send(player, plugin.getConfigManager().getMessage("TEAM.NO-TEAM"));
            return;
        }
        if (!plugin.getTeamManager().canManageTeammates(team, player.getUniqueId())) {
            send(player, plugin.getConfigManager().getMessage("TEAM.NO-MANAGE-PERMISSION"));
            return;
        }
        if (args[1].equalsIgnoreCase(player.getName())) {
            send(player, plugin.getConfigManager().getMessage("TEAM.CANT-KICK-SELF"));
            return;
        }

        UUID targetUuid = resolvePlayerUuid(args[1]);
        if (targetUuid == null) {
            send(player, plugin.getConfigManager().getMessage("TEAM.TEAM-NOT-EXIST"));
            return;
        }

        boolean kicked = plugin.getTeamManager().kickMember(team, targetUuid);
        send(player, kicked
                ? plugin.getConfigManager().getMessage("TEAM.KICK-SUCCESS", "{player}", args[1])
                : plugin.getConfigManager().getMessage("TEAM.PLAYER-NOT-IN-TEAM", "{player}", args[1]));
    }

    private void handleHome(Player player) {
        Team team = plugin.getTeamManager().getTeam(player);
        if (team == null) {
            send(player, plugin.getConfigManager().getMessage("TEAM.NO-TEAM"));
            return;
        }
        if (!plugin.getTeamManager().canVisitHome(team, player.getUniqueId())) {
            send(player, plugin.getConfigManager().getMessage("TEAM.NO-VISIT-HOME-PERMISSION"));
            return;
        }
        if (!team.hasHome()) {
            send(player, plugin.getConfigManager().getMessage("TEAM.NO-TEAM-HOME"));
            return;
        }

        plugin.getTeleportManager().queue(player, team.getHome(), "TEAM-HOME", null);
    }

    private void handleSetHome(Player player) {
        Team team = plugin.getTeamManager().getTeam(player);
        if (team == null) {
            send(player, plugin.getConfigManager().getMessage("TEAM.NO-TEAM"));
            return;
        }
        if (!plugin.getTeamManager().canEditHome(team, player.getUniqueId())) {
            send(player, plugin.getConfigManager().getMessage("TEAM.NO-EDIT-HOME-PERMISSION"));
            return;
        }

        team.setHome(player.getLocation());
        plugin.getTeamManager().save(team);
        send(player, plugin.getConfigManager().getMessage("TEAM.TEAM-HOME-SET"));
    }

    private void handleDeleteHome(Player player) {
        Team team = plugin.getTeamManager().getTeam(player);
        if (team == null) {
            send(player, plugin.getConfigManager().getMessage("TEAM.NO-TEAM"));
            return;
        }
        if (!plugin.getTeamManager().canEditHome(team, player.getUniqueId())) {
            send(player, plugin.getConfigManager().getMessage("TEAM.NO-EDIT-HOME-PERMISSION"));
            return;
        }
        if (!team.hasHome()) {
            send(player, plugin.getConfigManager().getMessage("TEAM.NO-TEAM-HOME"));
            return;
        }

        team.setHome(null);
        plugin.getTeamManager().save(team);
        send(player, plugin.getConfigManager().getMessage("TEAM.TEAM-HOME-DELETED"));
    }

    private void handleChat(Player player) {
        Team team = plugin.getTeamManager().getTeam(player);
        if (team == null) {
            send(player, plugin.getConfigManager().getMessage("TEAM.NO-TEAM"));
            return;
        }
        if (!plugin.getTeamManager().canUseTeamChat(team, player.getUniqueId())) {
            send(player, plugin.getConfigManager().getMessage("TEAM.NO-TEAM-CHAT-PERMISSION"));
            return;
        }

        plugin.getTeamManager().toggleTeamChat(player.getUniqueId());
        boolean enabled = plugin.getTeamManager().isTeamChatEnabled(player.getUniqueId());
        send(player, enabled
                ? plugin.getConfigManager().getMessage("TEAM.TEAM-CHAT-ENABLED")
                : plugin.getConfigManager().getMessage("TEAM.TEAM-CHAT-DISABLED"));
    }

    private void handlePvp(Player player) {
        Team team = plugin.getTeamManager().getTeam(player);
        if (team == null) {
            send(player, plugin.getConfigManager().getMessage("TEAM.NO-TEAM"));
            return;
        }
        if (!plugin.getTeamManager().toggleFriendlyFire(team, player.getUniqueId())) {
            send(player, plugin.getConfigManager().getMessage("TEAM.NO-PVP-PERMISSION"));
            return;
        }

        send(player, team.isFriendlyFireEnabled()
                ? plugin.getConfigManager().getMessage("TEAM.TEAM-PVP-ENABLED")
                : plugin.getConfigManager().getMessage("TEAM.TEAM-PVP-DISABLED"));
    }

    private UUID resolvePlayerUuid(String name) {
        Player online = Bukkit.getPlayerExact(name);
        if (online != null) {
            return online.getUniqueId();
        }

        UUID storedUuid = plugin.getDatabaseManager().findPlayerUuidByUsername(name);
        if (storedUuid != null) {
            return storedUuid;
        }

        return Bukkit.getOfflinePlayer(name).hasPlayedBefore()
                ? Bukkit.getOfflinePlayer(name).getUniqueId()
                : null;
    }

    private void send(Player player, String message) {
        player.sendMessage(ColorUtils.toComponent(message));
    }
}
