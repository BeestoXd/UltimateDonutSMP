package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.TPAManager;
import com.bx.ultimateDonutSmp.menus.TpaConfirmMenu;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TPACommand implements CommandExecutor {

    private final UltimateDonutSmp plugin;

    public TPACommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("Player only."); return true; }

        String sub = label.toLowerCase();

        switch (sub) {
            case "tpa" -> {
                if (args.length == 0) { send(player, "&cUsage: /tpa <player>"); return true; }
                Player target = Bukkit.getPlayerExact(args[0]);
                if (target == null || target.equals(player)) {
                    send(player, target == null ? "&cPlayer not online."
                            : plugin.getConfigManager().getMessage("TPA.CANNOT-INVITE-YOURSELF"));
                    return true;
                }
                PlayerData targetData = plugin.getPlayerDataManager().get(target);
                if (targetData != null && (targetData.isTpauto() || !targetData.isTpaRequestsEnabled())) {
                    int queuePosition = plugin.getTPAManager().queueAutoTPA(
                            player,
                            target,
                            !targetData.isTpaRequestsEnabled()
                    );
                    if (queuePosition == 0) {
                        send(player, plugin.getConfigManager().getMessage("TPA.ALREADY-SENT",
                                "{player}", target.getName()));
                        return true;
                    }

                    send(player, plugin.getConfigManager().getMessage("TPA.INVITE-SENT",
                            "{player}", target.getName()));
                    if (!targetData.isTpaRequestsEnabled() || queuePosition > 1 || !targetData.isTpauto()) {
                        send(player, "&7Your /tpa request was stored in &b" + target.getName()
                                + "&7's queue &8(#" + queuePosition + "&8).");
                    }
                    SoundUtils.play(player, plugin.getConfigManager().getSound("TPA.REQUEST-SENT"));
                    plugin.getTPAManager().processQueuedAutoRequests(target.getUniqueId());
                    return true;
                }

                if (!plugin.getTPAManager().sendTPA(player, target)) {
                    send(player, plugin.getConfigManager().getMessage("TPA.ALREADY-SENT",
                            "{player}", target.getName()));
                    return true;
                }
                send(player, plugin.getConfigManager().getMessage("TPA.INVITE-SENT",
                        "{player}", target.getName()));
                SoundUtils.play(player, plugin.getConfigManager().getSound("TPA.REQUEST-SENT"));
                sendIncomingRequest(player, target, false);
            }
            case "tpahere" -> {
                if (args.length == 0) { send(player, "&cUsage: /tpahere <player>"); return true; }
                Player target = Bukkit.getPlayerExact(args[0]);
                if (target == null || target.equals(player)) {
                    send(player, target == null ? "&cPlayer not online."
                            : plugin.getConfigManager().getMessage("TPA.CANNOT-INVITE-YOURSELF"));
                    return true;
                }
                PlayerData targetData = plugin.getPlayerDataManager().get(target);
                if (targetData != null && (targetData.isAutoTpaHereEnabled() || !targetData.isTpaHereRequestsEnabled())) {
                    int queuePosition = plugin.getTPAManager().queueAutoTPAHere(
                            player,
                            target,
                            !targetData.isTpaHereRequestsEnabled()
                    );
                    if (queuePosition == 0) {
                        send(player, plugin.getConfigManager().getMessage("TPA.ALREADY-SENT",
                                "{player}", target.getName()));
                        return true;
                    }

                    send(player, plugin.getConfigManager().getMessage("TPA.INVITE-HERE-SENT",
                            "{player}", target.getName()));
                    if (!targetData.isTpaHereRequestsEnabled() || queuePosition > 1 || !targetData.isAutoTpaHereEnabled()) {
                        send(player, "&7Your /tpahere request was added to &b" + target.getName()
                                + "&7's queue &8(#" + queuePosition + "&8).");
                    }
                    SoundUtils.play(player, plugin.getConfigManager().getSound("TPA.REQUEST-SENT"));
                    plugin.getTPAManager().processQueuedAutoRequests(target.getUniqueId());
                    return true;
                }

                if (!plugin.getTPAManager().sendTPAHere(player, target)) {
                    send(player, plugin.getConfigManager().getMessage("TPA.ALREADY-SENT",
                            "{player}", target.getName()));
                    return true;
                }
                send(player, plugin.getConfigManager().getMessage("TPA.INVITE-HERE-SENT",
                        "{player}", target.getName()));
                SoundUtils.play(player, plugin.getConfigManager().getSound("TPA.REQUEST-SENT"));
                sendIncomingRequest(player, target, true);
            }
            case "tpaccept" -> {
                TPAManager.TpaRequest req = plugin.getTPAManager().getRequest(player.getUniqueId());
                if (req == null) {
                    send(player, plugin.getConfigManager().getMessage("TPA.NO-REQUEST",
                            "{player}", args.length > 0 ? args[0] : ""));
                    return true;
                }
                plugin.getTPAManager().removeRequest(player.getUniqueId());
                Player requester = Bukkit.getPlayer(req.requester());
                if (requester == null) { send(player, "&cRequester is no longer online."); return true; }

                if (req.tpaHere()) {
                    // tpahere: player (target) teleports to requester
                    plugin.getTeleportManager().queue(player, requester.getLocation(), "TPA", null);
                    send(player, plugin.getConfigManager().getMessage("TPA.ACCEPTED-HERE",
                            "{player}", requester.getName()));
                    requester.sendMessage(ColorUtils.toComponent(
                            plugin.getConfigManager().getMessage("TPA.YOUR-REQUEST-HERE-ACCEPTED",
                                    "{player}", player.getName())));
                } else {
                    // tpa: requester teleports to player (target)
                    plugin.getTeleportManager().queue(requester, player.getLocation(), "TPA", null);
                    send(player, plugin.getConfigManager().getMessage("TPA.ACCEPTED",
                            "{player}", requester.getName()));
                    requester.sendMessage(ColorUtils.toComponent(
                            plugin.getConfigManager().getMessage("TPA.YOUR-REQUEST-ACCEPTED",
                                    "{player}", player.getName())));
                }
                SoundUtils.play(player, plugin.getConfigManager().getSound("TPA.CONFIRM"));
                SoundUtils.play(requester, plugin.getConfigManager().getSound("TPA.CONFIRM"));
            }
            case "tpadeny" -> {
                if (!plugin.getTPAManager().hasRequest(player.getUniqueId())) {
                    send(player, plugin.getConfigManager().getMessage("TPA.NO-REQUEST", "{player}", ""));
                    return true;
                }
                TPAManager.TpaRequest req = plugin.getTPAManager().getRequest(player.getUniqueId());
                plugin.getTPAManager().removeRequest(player.getUniqueId());
                send(player, "&7TPA request denied.");
                if (req != null) {
                    Player requester = Bukkit.getPlayer(req.requester());
                    if (requester != null) {
                        requester.sendMessage(ColorUtils.toComponent("&7Your teleport request was denied."));
                    }
                }
            }
            case "tpacancel" -> {
                plugin.getTPAManager().cancelRequestsByRequester(player.getUniqueId());
                send(player, plugin.getConfigManager().getMessage("TPA.CANCELLED-REQUESTS"));
            }
        }
        return true;
    }

    private void send(Player p, String msg) {
        p.sendMessage(ColorUtils.toComponent(msg));
    }

    private void sendIncomingRequest(Player requester, Player target, boolean tpaHere) {
        String requesterName = requester.getName();

        plugin.getPaperScheduler().runEntity(target, () -> {
            if (!target.isOnline()) {
                return;
            }

            SoundUtils.play(target, plugin.getConfigManager().getSound("TPA.REQUEST-RECEIVED"));

            PlayerData currentTargetData = plugin.getPlayerDataManager().get(target);
            if (currentTargetData != null && currentTargetData.isTpaConfirmMenuEnabled()) {
                new TpaConfirmMenu(plugin, requesterName, tpaHere).open(target);
                return;
            }

            String messagePath = tpaHere ? "TPA.REQUEST-HERE-RECEIVED" : "TPA.REQUEST-RECEIVED";
            Component requestMsg = ColorUtils.toComponent(
                    plugin.getConfigManager().getMessage(messagePath, "{player}", requesterName))
                    .clickEvent(ClickEvent.runCommand("/tpaccept " + requesterName));
            target.sendMessage(requestMsg);
        });
    }
}
