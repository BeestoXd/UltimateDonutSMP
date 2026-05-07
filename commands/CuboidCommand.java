package com.bx.ultimateDonutSmp.commands;

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
        if (!sender.hasPermission("ultimatedonutsmp.admin.cuboid")) {
            sender.sendMessage(ColorUtils.toComponent("&cNo permission."));
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
            sender.sendMessage("Player only.");
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
                        "&7Step 1: Left click a block to set &aPosition 1",
                        "&7Step 2: Right click a block to set &bPosition 2",
                        "&7Step 3: Use &f/cuboid create <name> &7to save",
                        "&8The wand disappears after both positions are set"
                )
        );

        var leftovers = player.getInventory().addItem(wand);
        if (!leftovers.isEmpty()) {
            leftovers.values().forEach(item -> player.getWorld().dropItemNaturally(player.getLocation(), item));
        }

        player.sendMessage(ColorUtils.toComponent("&aYou received the &6Cuboid Wand&a. &7Set both positions to continue."));
    }

    private void createCuboid(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ColorUtils.toComponent("&cUsage: /cuboid create <name>"));
            return;
        }
        if (!plugin.getCuboidManager().hasFullSelection(player.getUniqueId())) {
            player.sendMessage(ColorUtils.toComponent("&cSelect both positions first using &f/cuboid wand&c."));
            return;
        }

        Location[] selection = plugin.getCuboidManager().getSelection(player.getUniqueId());
        plugin.getCuboidManager().addCuboid(args[1], selection[0], selection[1]);
        player.sendMessage(ColorUtils.toComponent("&aCuboid &b" + args[1] + " &ahas been created."));
    }

    private void deleteCuboid(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ColorUtils.toComponent("&cUsage: /cuboid delete <name>"));
            return;
        }
        plugin.getCuboidManager().removeCuboid(args[1]);
        player.sendMessage(ColorUtils.toComponent("&aCuboid &b" + args[1] + " &ahas been deleted."));
    }

    private void listCuboids(Player player) {
        Set<String> names = plugin.getCuboidManager().getCuboidNames();
        if (names.isEmpty()) {
            player.sendMessage(ColorUtils.toComponent("&7No cuboids have been created yet."));
            return;
        }
        player.sendMessage(ColorUtils.toComponent("&7Cuboids: &b" + String.join("&7, &b", names)));
    }

    private void bindCuboidSystem(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage(ColorUtils.toComponent(
                    "&cUsage: /cuboid bind <cuboid> <spawn|shard|rtp-zone> <true|false>"
            ));
            return;
        }

        String cuboidName = args[1].toLowerCase();
        if (plugin.getCuboidManager().getCuboid(cuboidName) == null) {
            player.sendMessage(ColorUtils.toComponent("&cCuboid not found: &f" + args[1]));
            return;
        }

        String role = normalizeRole(args[2]);
        if (role == null) {
            player.sendMessage(ColorUtils.toComponent("&cUnknown role. Use &fspawn&c, &fshard&c, or &frtp-zone&c."));
            return;
        }

        boolean enabled;
        if ("true".equalsIgnoreCase(args[3]) || "on".equalsIgnoreCase(args[3])) {
            enabled = true;
        } else if ("false".equalsIgnoreCase(args[3]) || "off".equalsIgnoreCase(args[3])) {
            enabled = false;
        } else {
            player.sendMessage(ColorUtils.toComponent("&cToggle must be &ftrue &cor &ffalse&c."));
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
                config.set("SHARDS.CUBOIDS.REGIONS.spawn.ENABLED", enabled);
                config.set("SHARDS.CUBOIDS.REGIONS.spawn.BOUND", enabled);
                updateBindList(config, "CUBOID-BINDS.AFK", cuboidName, enabled);
                List<String> afkBinds = config.getStringList("CUBOID-BINDS.AFK");
                if (enabled) {
                    config.set("SHARDS.CUBOIDS.REGIONS.spawn.CUBOID", cuboidName);
                } else {
                    config.set("SHARDS.CUBOIDS.REGIONS.spawn.CUBOID", "");
                }
                config.set("AFK-SYSTEM.AFK-CUBOID-NAME", afkBinds.isEmpty() ? "" : afkBinds.get(0));
            }
            case "rtp-zone" -> config.set("RTP-ZONE.CUBOID", enabled ? cuboidName : "");
            default -> {
                player.sendMessage(ColorUtils.toComponent("&cUnknown role."));
                return;
            }
        }

        plugin.saveConfig();
        plugin.reloadAllPluginConfigurations();

        String state = enabled ? "&atrue" : "&cfalse";
        player.sendMessage(ColorUtils.toComponent(
                "&aCuboid &b" + cuboidName + " &aset for &f" + role + " &a= " + state
        ));
    }

    private void reloadAllConfigs(CommandSender sender) {
        plugin.reloadAllPluginConfigurations();
        sender.sendMessage(ColorUtils.toComponent("&aAll configuration files have been reloaded."));
    }

    private void updateBindList(FileConfiguration config, String path, String cuboidName, boolean enabled) {
        List<String> current = new ArrayList<>(config.getStringList(path));
        current.removeIf(entry -> entry.equalsIgnoreCase(cuboidName));
        if (enabled) {
            current.add(cuboidName);
        }
        config.set(path, current);
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
        sender.sendMessage(ColorUtils.toComponent("&cUsage: /cuboid <wand|create <name>|delete <name>|list|bind <cuboid> <spawn|shard|rtp-zone> <true|false>|reload>"));
    }
}
