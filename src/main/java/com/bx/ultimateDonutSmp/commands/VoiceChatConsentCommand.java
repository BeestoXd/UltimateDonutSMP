package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.VoiceChatConsent;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;

public class VoiceChatConsentCommand implements CommandExecutor, TabCompleter {

    private final UltimateDonutSmp plugin;

    public VoiceChatConsentCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtils.toComponent(plugin.getLanguageManager().message(
                    "VOICE-CHAT.PLAYERS-ONLY", "&cOnly players can use this command.")));
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("revoke")) {
            if (plugin.getVoiceChatConsentManager().getConsent(player) == VoiceChatConsent.UNDECIDED) {
                player.sendMessage(ColorUtils.toComponent(plugin.getLanguageManager().message(
                        "VOICE-CHAT.NOTHING-TO-REVOKE",
                        "&cYou have not agreed to the voice chat policy yet."), player));
                return true;
            }
            plugin.getVoiceChatConsentManager().revoke(player);
            return true;
        }

        plugin.getVoiceChatConsentManager().openMenu(player);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1 && "revoke".startsWith(args[0].toLowerCase(Locale.ROOT))) {
            return List.of("revoke");
        }
        return List.of();
    }
}
