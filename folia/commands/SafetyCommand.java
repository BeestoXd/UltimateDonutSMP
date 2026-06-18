package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.PermissionUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class SafetyCommand implements CommandExecutor {

    private final UltimateDonutSmp plugin;

    public SafetyCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // Handle reload subcommand
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!PermissionUtils.has(sender, "safety.reload")) {
                sender.sendMessage(ColorUtils.toComponent(
                        plugin.getLanguageManager().text("MESSAGES.SAFETY.NO-PERMISSION", "&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ.")
                ));
                return true;
            }

            try {
                plugin.reloadAllPluginConfigurations();
                sender.sendMessage(ColorUtils.toComponent(
                        plugin.getLanguageManager().text("MESSAGES.SAFETY.RELOAD-SUCCESS", "&aѕᴀꜰᴇᴛʏ ᴄᴏɴꜰɪɢ ʀᴇʟᴏᴀᴅᴇᴅ.")
                ));
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to reload safety configurations.", e);
                sender.sendMessage(ColorUtils.toComponent("&cꜰᴀɪʟᴇᴅ ᴛᴏ ʀᴇʟᴏᴀᴅ ᴄᴏɴꜰɪɢᴜʀᴀᴛɪᴏɴ. ᴄʜᴇᴄᴋ ᴄᴏɴѕᴏʟᴇ ꜰᴏʀ ᴅᴇᴛᴀɪʟѕ."));
            }
            return true;
        }

        // Handle add/give subcommand
        if (args.length > 0 && (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("give"))) {
            if (!PermissionUtils.has(sender, "safety.add")) {
                sender.sendMessage(ColorUtils.toComponent(
                        plugin.getLanguageManager().text("MESSAGES.SAFETY.NO-PERMISSION", "&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ.")
                ));
                return true;
            }

            Player target;
            if (args.length > 1) {
                target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(ColorUtils.toComponent(
                            plugin.getLanguageManager().text("MESSAGES.SAFETY.PLAYER-NOT-FOUND", "&cᴘʟᴀʏᴇʀ ɴᴏᴛ ꜰᴏᴜɴᴅ.")
                    ));
                    return true;
                }
            } else {
                if (sender instanceof Player player) {
                    target = player;
                } else {
                    sender.sendMessage(ColorUtils.toComponent(
                            plugin.getLanguageManager().text("MESSAGES.SAFETY.USAGE", "&cᴜѕᴀɢᴇ: /safety [reload|add <player>|give <player>]")
                    ));
                    return true;
                }
            }

            givePhysicalSafetyBook(sender, target);
            return true;
        }

        // Must be player to view the book directly on screen
        if (!(sender instanceof Player player)) {
            sender.sendMessage("ᴘʟᴀʏᴇʀ ᴏɴʟʏ.");
            return true;
        }

        // Check view permission
        if (!PermissionUtils.has(player, "safety.use")) {
            player.sendMessage(ColorUtils.toComponent(
                    plugin.getLanguageManager().text("MESSAGES.SAFETY.NO-PERMISSION", "&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ.")
            ));
            return true;
        }

        // Open safety book
        openSafetyBook(player);
        return true;
    }

    private ItemStack createSafetyBook(Player viewer) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        if (meta == null) {
            return null;
        }

        String title = plugin.getLanguageManager().text("MESSAGES.SAFETY.BOOK-TITLE", "Safety Guide");
        String author = plugin.getLanguageManager().text("MESSAGES.SAFETY.BOOK-AUTHOR", "Server");

        meta.setTitle(ColorUtils.colorize(title, viewer));
        meta.setAuthor(ColorUtils.colorize(author, viewer));

        List<String> defaultPages = List.of(
                "&0⚠ Watch out!\n\n" +
                "Fake discords or\n" +
                "mods can steal your\n" +
                "account.\n\n" +
                "The only official\n" +
                "discord can be found\n" +
                "using &1/discord&0.\n\n" +
                "Never download files\n" +
                "or mods from random\n" +
                "people."
        );

        List<String> rawPages = plugin.getLanguageManager().list("MESSAGES.SAFETY.BOOK-PAGES", defaultPages);
        List<String> coloredPages = new ArrayList<>();
        for (String rawPage : rawPages) {
            coloredPages.add(ColorUtils.colorize(rawPage, viewer));
        }
        meta.setPages(coloredPages);

        book.setItemMeta(meta);
        return book;
    }

    private void openSafetyBook(Player player) {
        try {
            ItemStack book = createSafetyBook(player);
            if (book == null) {
                player.sendMessage(ColorUtils.toComponent("&cAn error occurred: BookMeta is null."));
                return;
            }
            player.openBook(book);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to open safety book for " + player.getName(), e);
            player.sendMessage(ColorUtils.toComponent("&cAn error occurred while opening the safety book."));
        }
    }

    private void givePhysicalSafetyBook(CommandSender sender, Player target) {
        try {
            ItemStack book = createSafetyBook(target);
            if (book == null) {
                sender.sendMessage(ColorUtils.toComponent("&cAn error occurred: BookMeta is null."));
                return;
            }

            Map<Integer, ItemStack> leftovers = target.getInventory().addItem(book);
            if (!leftovers.isEmpty()) {
                leftovers.values().forEach(leftover -> target.getWorld().dropItemNaturally(target.getLocation(), leftover));
            }

            // Notify receiver
            target.sendMessage(ColorUtils.toComponent(
                    plugin.getLanguageManager().text("MESSAGES.SAFETY.GIVE-SUCCESS-RECEIVER", "&aʏᴏᴜ ʀᴇᴄᴇɪᴠᴇᴅ ᴛʜᴇ ѕᴀꜰᴇᴛʏ ʙᴏᴏᴋ.")
            ));

            // Notify sender if it is a different entity
            if (!(sender instanceof Player senderPlayer) || !senderPlayer.getUniqueId().equals(target.getUniqueId())) {
                sender.sendMessage(ColorUtils.toComponent(
                        plugin.getLanguageManager().text("MESSAGES.SAFETY.GIVE-SUCCESS-SENDER", "&aѕᴜᴄᴄᴇѕѕꜰᴜʟʟʏ ɢᴀᴠᴇ ѕᴀꜰᴇᴛʏ ʙᴏᴏᴋ ᴛᴏ {player}.", "{player}", target.getName())
                ));
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to give safety book to " + target.getName(), e);
            sender.sendMessage(ColorUtils.toComponent("&cAn error occurred while giving the safety book."));
        }
    }
}
