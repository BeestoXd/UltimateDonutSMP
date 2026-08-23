package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.ProfileViewerManager;
import com.bx.ultimateDonutSmp.menus.ProfileViewerHomesMenu;
import com.bx.ultimateDonutSmp.models.ProfileSnapshot;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.PermissionUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;

public class SeeHomesCommand implements CommandExecutor {

    public static final String PERMISSION = "ultimatedonutsmp.staff.seehomes";

    private final UltimateDonutSmp plugin;

    public SeeHomesCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    /**
     * The profile viewer already opens this exact menu, so anyone allowed to use it keeps working
     * without a second permission being handed out.
     */
    public static boolean canSeeHomes(Permissible permissible) {
        return PermissionUtils.hasAny(permissible, PERMISSION, ProfileViewerManager.VIEW_PERMISSION);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Player only.");
            return true;
        }

        if (!canSeeHomes(player)) {
            player.sendMessage(ColorUtils.toComponent("&cYou do not have permission to view other players' homes."));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ColorUtils.toComponent("&cUsage: /" + label + " <player>"));
            return true;
        }

        ProfileSnapshot snapshot = plugin.getProfileViewerManager().resolveProfile(args[0]).orElse(null);
        if (snapshot == null) {
            player.sendMessage(ColorUtils.toComponent("&cNo player named &f" + args[0] + " &chas ever joined this server."));
            return true;
        }

        new ProfileViewerHomesMenu(plugin, snapshot.getUuid(), false).open(player);
        return true;
    }
}
