package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.FreezeManager;
import com.bx.ultimateDonutSmp.models.FreezeState;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FreezeCommand implements CommandExecutor {

    private final UltimateDonutSmp plugin;

    public FreezeCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        FreezeManager freezeManager = plugin.getFreezeManager();

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!freezeManager.canAdmin(sender)) {
                sender.sendMessage(ColorUtils.toComponent(
                        freezeManager.getMessage("NO-PERMISSION", "&cYou do not have permission.")
                ));
                return true;
            }

            plugin.getConfigManager().reloadFreeze();
            freezeManager.reload();
            sender.sendMessage(ColorUtils.toComponent(
                    freezeManager.getMessage("RELOAD-SUCCESS", "&aFreeze config reloaded.")
            ));
            return true;
        }

        if (!freezeManager.isEnabled()) {
            sender.sendMessage(ColorUtils.toComponent(
                    freezeManager.getMessage("FEATURE-DISABLED", "&cThe Freeze system is disabled.")
            ));
            return true;
        }

        if (!freezeManager.canUse(sender)) {
            sender.sendMessage(ColorUtils.toComponent(
                    freezeManager.getMessage("NO-PERMISSION", "&cYou do not have permission.")
            ));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ColorUtils.toComponent("&cUsage: /" + label + " <player>"));
            return true;
        }

        Player target = freezeManager.findOnlineTarget(args[0]);
        if (target != null) {
            if (freezeManager.hasActiveFreeze(target.getUniqueId())) {
                FreezeManager.FreezeToggleResult result = freezeManager.unfreeze(sender, target.getUniqueId());
                if (result != null) {
                    sender.sendMessage(ColorUtils.toComponent(freezeManager.buildToggleMessage(result)));
                }
                return true;
            }

            if (freezeManager.isSelfTarget(sender, target)) {
                sender.sendMessage(ColorUtils.toComponent(
                        freezeManager.getMessage("SELF-TARGET", "&cYou cannot freeze yourself.")
                ));
                return true;
            }

            if (!freezeManager.canFreeze(sender, target)) {
                sender.sendMessage(ColorUtils.toComponent(
                        freezeManager.getMessage("TARGET-EXEMPT", "&cYou cannot freeze that player.")
                ));
                return true;
            }

            FreezeManager.FreezeToggleResult result = freezeManager.freeze(sender, target);
            if (result != null) {
                sender.sendMessage(ColorUtils.toComponent(freezeManager.buildToggleMessage(result)));
            }
            return true;
        }

        FreezeState frozenState = freezeManager.findActiveState(args[0]);
        if (frozenState != null) {
            FreezeManager.FreezeToggleResult result = freezeManager.unfreeze(sender, frozenState.getTargetUuid());
            if (result != null) {
                sender.sendMessage(ColorUtils.toComponent(freezeManager.buildToggleMessage(result)));
            }
            return true;
        }

        String path = freezeManager.hasKnownPlayer(args[0]) ? "TARGET-OFFLINE" : "PLAYER-NOT-FOUND";
        String fallback = freezeManager.hasKnownPlayer(args[0])
                ? "&cThat player must be online."
                : "&cPlayer not found.";
        sender.sendMessage(ColorUtils.toComponent(freezeManager.getMessage(path, fallback)));
        return true;
    }
}
