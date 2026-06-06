package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.FakePlayerManager;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.PermissionUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FakePlayerCommand implements CommandExecutor {

    private final UltimateDonutSmp plugin;

    public FakePlayerCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        FakePlayerManager manager = plugin.getFakePlayerManager();
        if (!PermissionUtils.has(sender, FakePlayerManager.USE_PERMISSION)) {
            send(sender, manager == null
                    ? "&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ."
                    : manager.publicMessage("NO-PERMISSION", "&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ."));
            return true;
        }

        if (!(sender instanceof Player player)) {
            send(sender, manager == null
                    ? "&cᴏɴʟʏ ᴘʟᴀʏᴇʀѕ ᴄᴀɴ ᴜѕᴇ ᴛʜɪѕ ᴄᴏᴍᴍᴀɴᴅ."
                    : manager.publicMessage("PLAYER-ONLY", "&cᴏɴʟʏ ᴘʟᴀʏᴇʀѕ ᴄᴀɴ ᴜѕᴇ ᴛʜɪѕ ᴄᴏᴍᴍᴀɴᴅ."));
            return true;
        }

        if (manager == null || !manager.isAvailable()) {
            send(sender, manager == null
                    ? "&cᴘʀᴏᴛᴏᴄᴏʟʟɪʙ ɪѕ ʀᴇǫᴜɪʀᴇᴅ ꜰᴏʀ /fakeplayer. ɪɴѕᴛᴀʟʟ ᴘʀᴏᴛᴏᴄᴏʟʟɪʙ ᴀɴᴅ ʀᴇѕᴛᴀʀᴛ ᴛʜᴇ ѕᴇʀᴠᴇʀ."
                    : manager.publicMessage(
                            "DEPENDENCY-MISSING",
                            "&cᴘʀᴏᴛᴏᴄᴏʟʟɪʙ ɪѕ ʀᴇǫᴜɪʀᴇᴅ ꜰᴏʀ /fakeplayer. ɪɴѕᴛᴀʟʟ ᴘʀᴏᴛᴏᴄᴏʟʟɪʙ ᴀɴᴅ ʀᴇѕᴛᴀʀᴛ ᴛʜᴇ ѕᴇʀᴠᴇʀ."
                    ));
            return true;
        }

        send(sender, manager.publicMessage(
                "SKIN-LOOKUP",
                "&7ᴄʜᴇᴄᴋɪɴɢ ѕᴋɪɴ ᴅᴀᴛᴀ..."
        ));
        manager.spawnAsync(player, result -> send(sender, result.message()));
        return true;
    }

    private void send(CommandSender sender, String message) {
        sender.sendMessage(ColorUtils.toComponent(message));
    }
}
