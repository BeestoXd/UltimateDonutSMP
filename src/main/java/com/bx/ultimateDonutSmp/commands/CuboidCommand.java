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
            case "setspawn" -> setCuboidSpawn(player, args);
            case "delspawn" -> deleteCuboidSpawn(player, args);
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
                        "&7Step 1: left click a block to set &aposition 1",
                        "&7Step 2: right click a block to set &bposition 2",
                        "&7Step 3: use &f/cuboid create <name> &7to save",
                        "&8The wand disappears after both positions are set"
                )
        );

        CuboidWandListener.markAsCuboidWand(plugin, wand);

        var leftovers = player.getInventory().addItem(wand);
        if (!leftovers.isEmpty()) {
            leftovers.values().forEach(item -> player.getWorld().dropItemNaturally(player.getLocation(), item));
        }

        player.sendMessage(ColorUtils.toComponent("&aYou received the &6cuboid wand&a. &7Set both positions to continue."));
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

        if (plugin.getCuboidManager().dropSpawnOutsideBounds(args[1]) && plugin.getConfigManager().saveConfig()) {
            plugin.reloadAllPluginConfigurations();
            player.sendMessage(ColorUtils.toComponent(
                    "&7Its old spawn point was outside the new corners, so it has been cleared."
            ));
        }
    }

    private void setCuboidSpawn(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ColorUtils.toComponent("&cUsage: /cuboid setspawn <name>"));
            return;
        }

        String cuboidName = args[1].toLowerCase();
        var cuboid = plugin.getCuboidManager().getCuboid(cuboidName);
        if (cuboid == null) {
            player.sendMessage(ColorUtils.toComponent("&cCuboid not found: &f" + args[1]));
            return;
        }

        Location location = player.getLocation();
        if (!cuboid.contains(location)) {
            player.sendMessage(ColorUtils.toComponent(
                    "&cStand inside &b" + cuboidName + " &cfirst. &7Its spawn point has to be within the region."
            ));
            return;
        }

        plugin.getCuboidManager().setCuboidSpawn(cuboidName, location);
        if (!plugin.getConfigManager().saveConfig()) {
            player.sendMessage(ColorUtils.toComponent("&cFailed to save config.yml."));
            return;
        }
        plugin.reloadAllPluginConfigurations();
        player.sendMessage(ColorUtils.toComponent(
                "&aCuboid &b" + cuboidName + " &anow spawns players where you are standing. &7(X: &f"
                        + String.format("%.1f", location.getX()) + "&7, Y: &f"
                        + String.format("%.1f", location.getY()) + "&7, Z: &f"
                        + String.format("%.1f", location.getZ()) + "&7)"
        ));
    }

    private void deleteCuboidSpawn(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ColorUtils.toComponent("&cUsage: /cuboid delspawn <name>"));
            return;
        }

        String cuboidName = args[1].toLowerCase();
        if (!plugin.getCuboidManager().clearCuboidSpawn(cuboidName)) {
            player.sendMessage(ColorUtils.toComponent("&cCuboid &b" + args[1] + " &chas no spawn point saved."));
            return;
        }

        if (!plugin.getConfigManager().saveConfig()) {
            player.sendMessage(ColorUtils.toComponent("&cFailed to save config.yml."));
            return;
        }
        plugin.reloadAllPluginConfigurations();
        player.sendMessage(ColorUtils.toComponent(
                "&aSpawn point for &b" + cuboidName + " &aremoved. &7Players land near the middle of the region again."
        ));
    }

    private void deleteCuboid(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ColorUtils.toComponent("&cUsage: /cuboid delete <name>"));
            return;
        }
        String cuboidName = args[1].toLowerCase();
        plugin.getCuboidManager().removeCuboid(cuboidName);
        clearDeletedCuboidReferences(cuboidName);
        if (!plugin.getConfigManager().saveConfig()) {
            player.sendMessage(ColorUtils.toComponent("&cFailed to save config.yml."));
            return;
        }
        plugin.reloadAllPluginConfigurations();
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
        var cuboid = plugin.getCuboidManager().getCuboid(cuboidName);
        if (cuboid == null) {
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
                    config.set("SHARDS.CUBOIDS.REGIONS.spawn.WORLD", cuboid.world());
                } else {
                    config.set("SHARDS.CUBOIDS.REGIONS.spawn.CUBOID", "");
                    config.set("SHARDS.CUBOIDS.REGIONS.spawn.WORLD", "");
                    if (isBlank(config.getString("SHARDS.CUBOIDS.REGIONS.spawn.LOCATION"))) {
                        config.set("SHARDS.CUBOIDS.REGIONS.spawn.ENABLED", false);
                        config.set("SHARDS.CUBOIDS.REGIONS.spawn.BOUND", false);
                    } else {
                        config.set("SHARDS.CUBOIDS.REGIONS.spawn.ENABLED", true);
                        config.set("SHARDS.CUBOIDS.REGIONS.spawn.BOUND", false);
                    }
                }
                config.set("AFK-SYSTEM.AFK-CUBOID-NAME", afkBinds.isEmpty() ? "" : afkBinds.get(0));
            }
            case "rtp-zone" -> config.set("RTP-ZONE.CUBOID", enabled ? cuboidName : "");
            default -> {
                player.sendMessage(ColorUtils.toComponent("&cUnknown role."));
                return;
            }
        }

        if (!plugin.getConfigManager().saveConfig()) {
            player.sendMessage(ColorUtils.toComponent("&cFailed to save config.yml."));
            return;
        }
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

    private void clearDeletedCuboidReferences(String cuboidName) {
        FileConfiguration config = plugin.getConfigManager().getConfig();
        plugin.getCuboidManager().clearCuboidSpawn(cuboidName);
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

        if (cuboidName.equalsIgnoreCase(config.getString("SHARDS.CUBOIDS.REGIONS.spawn.CUBOID", ""))) {
            config.set("SHARDS.CUBOIDS.REGIONS.spawn.CUBOID", "");
            config.set("SHARDS.CUBOIDS.REGIONS.spawn.WORLD", "");
            if (isBlank(config.getString("SHARDS.CUBOIDS.REGIONS.spawn.LOCATION"))) {
                config.set("SHARDS.CUBOIDS.REGIONS.spawn.ENABLED", false);
                config.set("SHARDS.CUBOIDS.REGIONS.spawn.BOUND", false);
            } else {
                config.set("SHARDS.CUBOIDS.REGIONS.spawn.ENABLED", true);
                config.set("SHARDS.CUBOIDS.REGIONS.spawn.BOUND", false);
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
        sender.sendMessage(ColorUtils.toComponent("&cUsage: /cuboid <wand|create <name>|delete <name>|list|setspawn <name>|delspawn <name>|bind <cuboid> <spawn|shard|rtp-zone> <true|false>|reload>"));
    }
}
