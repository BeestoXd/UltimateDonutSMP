package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.utils.PermissionUtils;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.listeners.CuboidWandListener;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CuboidCommand implements CommandExecutor {

    private final UltimateDonutSmp plugin;

    public CuboidCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!PermissionUtils.has(sender, "ultimatedonutsmp.admin.cuboid")) {
            sender.sendMessage(ColorUtils.toComponent("&cɴᴏ ᴘᴇʀᴍɪѕѕɪᴏɴ."));
            return true;
        }

        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        String sub = args[0].toLowerCase();
        if (sub.equals("reload")) {
            reloadAllConfigs(sender);
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("ᴘʟᴀʏᴇʀ ᴏɴʟʏ.");
            return true;
        }

        switch (sub) {
            case "wand" -> giveWand(player);
            case "create", "save" -> createCuboid(player, args);
            case "delete" -> deleteCuboid(player, args);
            case "list" -> listCuboids(player);
            case "bind", "system" -> bindCuboidSystem(player, args);
            default -> sendUsage(player);
        }
        return true;
    }

    private void giveWand(Player player) {
        plugin.getCuboidManager().clearSelection(player.getUniqueId());

        ItemStack wand = ItemUtils.createItem(
                Material.GOLDEN_SHOVEL,
                CuboidWandListener.getWandName(),
                List.of(
                        "&7ѕᴛᴇᴘ 1: ʟᴇꜰᴛ ᴄʟɪᴄᴋ ᴀ ʙʟᴏᴄᴋ ᴛᴏ ѕᴇᴛ &aᴘᴏѕɪᴛɪᴏɴ 1",
                        "&7ѕᴛᴇᴘ 2: ʀɪɢʜᴛ ᴄʟɪᴄᴋ ᴀ ʙʟᴏᴄᴋ ᴛᴏ ѕᴇᴛ &bᴘᴏѕɪᴛɪᴏɴ 2",
                        "&7ѕᴛᴇᴘ 3: ᴜѕᴇ &f/cuboid ᴄʀᴇᴀᴛᴇ <name> &7ᴛᴏ ѕᴀᴠᴇ",
                        "&8ᴛʜᴇ ᴡᴀɴᴅ ᴅɪѕᴀᴘᴘᴇᴀʀѕ ᴀꜰᴛᴇʀ ʙᴏᴛʜ ᴘᴏѕɪᴛɪᴏɴѕ ᴀʀᴇ ѕᴇᴛ"
                )
        );

        CuboidWandListener.markAsCuboidWand(plugin, wand);

        var leftovers = player.getInventory().addItem(wand);
        if (!leftovers.isEmpty()) {
            leftovers.values().forEach(item -> player.getWorld().dropItemNaturally(player.getLocation(), item));
        }

        player.sendMessage(ColorUtils.toComponent("&aʏᴏᴜ ʀᴇᴄᴇɪᴠᴇᴅ ᴛʜᴇ &6ᴄᴜʙᴏɪᴅ ᴡᴀɴᴅ&a. &7ѕᴇᴛ ʙᴏᴛʜ ᴘᴏѕɪᴛɪᴏɴѕ ᴛᴏ ᴄᴏɴᴛɪɴᴜᴇ."));
    }

    private void createCuboid(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ColorUtils.toComponent("&cᴜѕᴀɢᴇ: /cuboid ᴄʀᴇᴀᴛᴇ <name>"));
            return;
        }
        if (!plugin.getCuboidManager().hasFullSelection(player.getUniqueId())) {
            player.sendMessage(ColorUtils.toComponent("&cѕᴇʟᴇᴄᴛ ʙᴏᴛʜ ᴘᴏѕɪᴛɪᴏɴѕ ꜰɪʀѕᴛ ᴜѕɪɴɢ &f/cuboid ᴡᴀɴᴅ&c."));
            return;
        }

        Location[] selection = plugin.getCuboidManager().getSelection(player.getUniqueId());
        plugin.getCuboidManager().addCuboid(args[1], selection[0], selection[1]);
        player.sendMessage(ColorUtils.toComponent("&aᴄᴜʙᴏɪᴅ &b" + args[1] + " &aʜᴀѕ ʙᴇᴇɴ ᴄʀᴇᴀᴛᴇᴅ."));
    }

    private void deleteCuboid(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ColorUtils.toComponent("&cᴜѕᴀɢᴇ: /cuboid ᴅᴇʟᴇᴛᴇ <name>"));
            return;
        }
        String cuboidName = args[1].toLowerCase();
        plugin.getCuboidManager().removeCuboid(cuboidName);
        clearDeletedCuboidReferences(cuboidName);
        if (!plugin.getConfigManager().saveConfig()) {
            player.sendMessage(ColorUtils.toComponent("&cꜰᴀɪʟᴇᴅ ᴛᴏ ѕᴀᴠᴇ config.yml."));
            return;
        }
        plugin.reloadAllPluginConfigurations();
        player.sendMessage(ColorUtils.toComponent("&aᴄᴜʙᴏɪᴅ &b" + args[1] + " &aʜᴀѕ ʙᴇᴇɴ ᴅᴇʟᴇᴛᴇᴅ."));
    }

    private void listCuboids(Player player) {
        Set<String> names = plugin.getCuboidManager().getCuboidNames();
        if (names.isEmpty()) {
            player.sendMessage(ColorUtils.toComponent("&7ɴᴏ ᴄᴜʙᴏɪᴅѕ ʜᴀᴠᴇ ʙᴇᴇɴ ᴄʀᴇᴀᴛᴇᴅ ʏᴇᴛ."));
            return;
        }
        player.sendMessage(ColorUtils.toComponent("&7ᴄᴜʙᴏɪᴅѕ: &b" + String.join("&7, &b", names)));
    }

    private void bindCuboidSystem(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage(ColorUtils.toComponent(
                    "&cᴜѕᴀɢᴇ: /cuboid ʙɪɴᴅ <cuboid> <spawn|shard|rtp-zone> <true|false>"
            ));
            return;
        }

        String cuboidName = args[1].toLowerCase();
        var cuboid = plugin.getCuboidManager().getCuboid(cuboidName);
        if (cuboid == null) {
            player.sendMessage(ColorUtils.toComponent("&cᴄᴜʙᴏɪᴅ ɴᴏᴛ ꜰᴏᴜɴᴅ: &f" + args[1]));
            return;
        }

        String role = normalizeRole(args[2]);
        if (role == null) {
            player.sendMessage(ColorUtils.toComponent("&cᴜɴᴋɴᴏᴡɴ ʀᴏʟᴇ. ᴜѕᴇ &fѕᴘᴀᴡɴ&c, &fѕʜᴀʀᴅ&c, ᴏʀ &fʀᴛᴘ-ᴢᴏɴᴇ&c."));
            return;
        }

        boolean enabled;
        if ("true".equalsIgnoreCase(args[3]) || "on".equalsIgnoreCase(args[3])) {
            enabled = true;
        } else if ("false".equalsIgnoreCase(args[3]) || "off".equalsIgnoreCase(args[3])) {
            enabled = false;
        } else {
            player.sendMessage(ColorUtils.toComponent("&cᴛᴏɢɢʟᴇ ᴍᴜѕᴛ ʙᴇ &fᴛʀᴜᴇ &cᴏʀ &fꜰᴀʟѕᴇ&c."));
            return;
        }

        FileConfiguration config = plugin.getConfigManager().getConfig();
        switch (role) {
            case "spawn" -> {
                updateBindList(config, "CUBOID-BINDS.SPAWN", cuboidName, enabled);
                List<String> spawnBinds = config.getStringList("CUBOID-BINDS.SPAWN");
                config.set("AFK-SYSTEM.SPAWN-CUBOID-NAME", spawnBinds.isEmpty() ? "" : spawnBinds.get(0));
            }
            case "shard" -> {
                config.set("SHARDS.CUBOIDS.REGIONS.SPAWN.ENABLED", enabled);
                config.set("SHARDS.CUBOIDS.REGIONS.SPAWN.BOUND", enabled);
                updateBindList(config, "CUBOID-BINDS.AFK", cuboidName, enabled);
                List<String> afkBinds = config.getStringList("CUBOID-BINDS.AFK");
                if (enabled) {
                    config.set("SHARDS.CUBOIDS.REGIONS.SPAWN.CUBOID", cuboidName);
                    config.set("SHARDS.CUBOIDS.REGIONS.SPAWN.WORLD", cuboid.world());
                } else {
                    config.set("SHARDS.CUBOIDS.REGIONS.SPAWN.CUBOID", "");
                    config.set("SHARDS.CUBOIDS.REGIONS.SPAWN.WORLD", "");
                    if (isBlank(config.getString("SHARDS.CUBOIDS.REGIONS.SPAWN.LOCATION"))) {
                        config.set("SHARDS.CUBOIDS.REGIONS.SPAWN.ENABLED", false);
                        config.set("SHARDS.CUBOIDS.REGIONS.SPAWN.BOUND", false);
                    } else {
                        config.set("SHARDS.CUBOIDS.REGIONS.SPAWN.ENABLED", true);
                        config.set("SHARDS.CUBOIDS.REGIONS.SPAWN.BOUND", false);
                    }
                }
                config.set("AFK-SYSTEM.AFK-CUBOID-NAME", afkBinds.isEmpty() ? "" : afkBinds.get(0));
            }
            case "rtp-zone" -> config.set("RTP-ZONE.CUBOID", enabled ? cuboidName : "");
            default -> {
                player.sendMessage(ColorUtils.toComponent("&cᴜɴᴋɴᴏᴡɴ ʀᴏʟᴇ."));
                return;
            }
        }

        if (!plugin.getConfigManager().saveConfig()) {
            player.sendMessage(ColorUtils.toComponent("&cꜰᴀɪʟᴇᴅ ᴛᴏ ѕᴀᴠᴇ config.yml."));
            return;
        }
        plugin.reloadAllPluginConfigurations();

        String state = enabled ? "&atrue" : "&cfalse";
        player.sendMessage(ColorUtils.toComponent(
                "&aᴄᴜʙᴏɪᴅ &b" + cuboidName + " &aѕᴇᴛ ꜰᴏʀ &f" + role + " &a= " + state
        ));
    }

    private void reloadAllConfigs(CommandSender sender) {
        plugin.reloadAllPluginConfigurations();
        sender.sendMessage(ColorUtils.toComponent("&aᴀʟʟ ᴄᴏɴꜰɪɢᴜʀᴀᴛɪᴏɴ ꜰɪʟᴇѕ ʜᴀᴠᴇ ʙᴇᴇɴ ʀᴇʟᴏᴀᴅᴇᴅ."));
    }

    private void clearDeletedCuboidReferences(String cuboidName) {
        FileConfiguration config = plugin.getConfigManager().getConfig();
        updateBindList(config, "CUBOID-BINDS.SPAWN", cuboidName, false);
        updateBindList(config, "CUBOID-BINDS.AFK", cuboidName, false);

        List<String> spawnBinds = config.getStringList("CUBOID-BINDS.SPAWN");
        if (cuboidName.equalsIgnoreCase(config.getString("AFK-SYSTEM.SPAWN-CUBOID-NAME", ""))) {
            config.set("AFK-SYSTEM.SPAWN-CUBOID-NAME", spawnBinds.isEmpty() ? "" : spawnBinds.get(0));
        }

        List<String> afkBinds = config.getStringList("CUBOID-BINDS.AFK");
        if (cuboidName.equalsIgnoreCase(config.getString("AFK-SYSTEM.AFK-CUBOID-NAME", ""))) {
            config.set("AFK-SYSTEM.AFK-CUBOID-NAME", afkBinds.isEmpty() ? "" : afkBinds.get(0));
        }

        if (cuboidName.equalsIgnoreCase(config.getString("SHARDS.CUBOIDS.REGIONS.SPAWN.CUBOID", ""))) {
            config.set("SHARDS.CUBOIDS.REGIONS.SPAWN.CUBOID", "");
            config.set("SHARDS.CUBOIDS.REGIONS.SPAWN.WORLD", "");
            if (isBlank(config.getString("SHARDS.CUBOIDS.REGIONS.SPAWN.LOCATION"))) {
                config.set("SHARDS.CUBOIDS.REGIONS.SPAWN.ENABLED", false);
                config.set("SHARDS.CUBOIDS.REGIONS.SPAWN.BOUND", false);
            } else {
                config.set("SHARDS.CUBOIDS.REGIONS.SPAWN.ENABLED", true);
                config.set("SHARDS.CUBOIDS.REGIONS.SPAWN.BOUND", false);
            }
        }
    }

    private void updateBindList(FileConfiguration config, String path, String cuboidName, boolean enabled) {
        List<String> current = new ArrayList<>(config.getStringList(path));
        current.removeIf(entry -> entry.equalsIgnoreCase(cuboidName));
        if (enabled) {
            current.add(cuboidName);
        }
        config.set(path, current);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String normalizeRole(String raw) {
        return switch (raw.toLowerCase()) {
            case "spawn" -> "spawn";
            case "shard", "shards" -> "shard";
            case "rtp-zone", "rtpzone", "rtp_zone" -> "rtp-zone";
            default -> null;
        };
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(ColorUtils.toComponent("&cᴜѕᴀɢᴇ: /cuboid <wand|create <name>|ᴅᴇʟᴇᴛᴇ <name>|ʟɪѕᴛ|ʙɪɴᴅ <cuboid> <spawn|shard|rtp-zone> <true|false>|ʀᴇʟᴏᴀᴅ>"));
    }
}
