package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PortalDefinition;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class PortalManagerCommand implements CommandExecutor {

    private static final String PORTAL_MANAGER_PERMISSION = "ultimatedonutsmp.admin.portalmanager";

    private final UltimateDonutSmp plugin;

    public PortalManagerCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(PORTAL_MANAGER_PERMISSION)) {
            sendMessage(sender, message("PORTALMANAGER.NO-PERMISSION",
                    "&cYou do not have permission to manage portals."));
            return true;
        }

        if (args.length == 0) {
            sendMessage(sender, usage(label));
            return true;
        }

        String subcommand = args[0].toLowerCase(Locale.ROOT);
        switch (subcommand) {
            case "list" -> handleList(sender);
            case "info" -> handleInfo(sender, label, args);
            case "create" -> handleCreate(sender, label, args);
            case "delete" -> handleDelete(sender, label, args);
            case "setcuboid" -> handleSetCuboid(sender, label, args);
            case "setdestination" -> handleSetDestination(sender, label, args);
            case "setdisplay" -> handleSetDisplay(sender, label, args);
            case "toggle" -> handleToggle(sender, label, args);
            case "setpriority" -> handleSetPriority(sender, label, args);
            case "sethologramhere" -> handleSetHologramHere(sender, label, args);
            default -> sendMessage(sender, usage(label));
        }
        return true;
    }

    private void handleList(CommandSender sender) {
        List<PortalDefinition> portals = plugin.getPortalManager().getPortals();
        if (portals.isEmpty()) {
            sendMessage(sender, message("PORTAL.LIST-EMPTY", "&cNo portals have been configured yet."));
            return;
        }

        sendMessage(sender, message("PORTALMANAGER.LIST-HEADER",
                "&8&m---------------- &dPortals &7({count}) &8&m----------------")
                .replace("{count}", String.valueOf(portals.size())));

        for (PortalDefinition portal : portals) {
            String state = describeState(portal);
            String destination = plugin.getPortalManager().describeDestination(portal);
            sendMessage(sender, message("PORTALMANAGER.LIST-ENTRY",
                    "&7- &d{id} &8[&f{state}&8] &7cuboid=&f{cuboid} &7destination=&f{destination}")
                    .replace("{id}", portal.id())
                    .replace("{state}", state)
                    .replace("{cuboid}", portal.cuboidName())
                    .replace("{destination}", destination));
        }
    }

    private void handleInfo(CommandSender sender, String label, String[] args) {
        if (args.length != 2) {
            sendMessage(sender, message("PORTALMANAGER.INFO-USAGE",
                    "&cUsage: /" + label + " info <id>"));
            return;
        }

        PortalDefinition portal = plugin.getPortalManager().getPortal(args[1]);
        if (portal == null) {
            sendMessage(sender, portalNotFound(args[1]));
            return;
        }

        String worldName = plugin.getPortalManager().resolveDestinationWorld(portal);
        String worldLabel = worldName == null ? "Unknown" : plugin.getRtpManager().describeWorld(worldName);

        sendMessage(sender, message("PORTALMANAGER.INFO-HEADER",
                "&8&m---------------- &dPortal: &f{id} &8&m----------------")
                .replace("{id}", portal.id()));
        sendMessage(sender, message("PORTALMANAGER.INFO-DISPLAY",
                "&7Display: &f{display}")
                .replace("{display}", portal.effectiveDisplayName()));
        sendMessage(sender, message("PORTALMANAGER.INFO-STATE",
                "&7State: &f{state}")
                .replace("{state}", describeState(portal)));
        sendMessage(sender, message("PORTALMANAGER.INFO-CUBOID",
                "&7Cuboid: &f{cuboid}")
                .replace("{cuboid}", portal.cuboidName()));
        sendMessage(sender, message("PORTALMANAGER.INFO-DESTINATION",
                "&7Destination: &f{destination}")
                .replace("{destination}", portal.destinationValue()));
        sendMessage(sender, message("PORTALMANAGER.INFO-WORLD",
                "&7Resolved World: &f{world}")
                .replace("{world}", worldLabel));
        sendMessage(sender, message("PORTALMANAGER.INFO-PRIORITY",
                "&7Priority: &f{priority}")
                .replace("{priority}", String.valueOf(portal.priority())));
        sendMessage(sender, message("PORTALMANAGER.INFO-COOLDOWN",
                "&7Trigger Cooldown: &f{cooldown}ms")
                .replace("{cooldown}", String.valueOf(portal.triggerCooldownMillis())));
        sendMessage(sender, message("PORTALMANAGER.INFO-PERMISSION",
                "&7Permission: &f{permission}")
                .replace("{permission}", portal.permission().isBlank() ? "-" : portal.permission()));
        sendMessage(sender, message("PORTALMANAGER.INFO-HOLOGRAM",
                "&7Hologram: &f{hologram}")
                .replace("{hologram}", formatHologramLocation(portal)));
    }

    private void handleCreate(CommandSender sender, String label, String[] args) {
        if (args.length != 4) {
            sendMessage(sender, message("PORTALMANAGER.CREATE-USAGE",
                    "&cUsage: /" + label + " create <id> <cuboid> <rtp_selector>"));
            return;
        }

        String portalId = args[1];
        String cuboidName = args[2];
        String selector = args[3];

        if (!plugin.getPortalManager().isValidPortalId(portalId)) {
            sendMessage(sender, message("PORTALMANAGER.INVALID-ID",
                    "&cInvalid portal id. Use only letters, numbers, dashes, and underscores."));
            return;
        }

        if (!plugin.getCuboidManager().exists(cuboidName)) {
            sendMessage(sender, message("PORTALMANAGER.INVALID-CUBOID",
                    "&cCuboid '&e{cuboid}&c' does not exist.")
                    .replace("{cuboid}", cuboidName));
            return;
        }

        if (!plugin.getRtpManager().isPortalDestinationAvailable(selector)) {
            sendMessage(sender, message("PORTALMANAGER.INVALID-DESTINATION",
                    "&cRTP destination '&e{destination}&c' is unavailable.")
                    .replace("{destination}", selector));
            return;
        }

        if (!plugin.getPortalManager().createPortal(portalId, cuboidName, selector)) {
            sendMessage(sender, message("PORTALMANAGER.ALREADY-EXISTS",
                    "&cPortal '&e{id}&c' already exists.")
                    .replace("{id}", portalId));
            return;
        }

        sendMessage(sender, message("PORTALMANAGER.CREATED",
                "&aPortal &d{id} &ahas been created.")
                .replace("{id}", plugin.getPortalManager().normalizeId(portalId)));
    }

    private void handleDelete(CommandSender sender, String label, String[] args) {
        if (args.length != 2) {
            sendMessage(sender, message("PORTALMANAGER.DELETE-USAGE",
                    "&cUsage: /" + label + " delete <id>"));
            return;
        }

        if (!plugin.getPortalManager().deletePortal(args[1])) {
            sendMessage(sender, portalNotFound(args[1]));
            return;
        }

        sendMessage(sender, message("PORTALMANAGER.DELETED",
                "&aPortal &d{id} &ahas been deleted.")
                .replace("{id}", plugin.getPortalManager().normalizeId(args[1])));
    }

    private void handleSetCuboid(CommandSender sender, String label, String[] args) {
        if (args.length != 3) {
            sendMessage(sender, message("PORTALMANAGER.SETCUBOID-USAGE",
                    "&cUsage: /" + label + " setcuboid <id> <cuboid>"));
            return;
        }

        if (plugin.getPortalManager().getPortal(args[1]) == null) {
            sendMessage(sender, portalNotFound(args[1]));
            return;
        }

        if (!plugin.getCuboidManager().exists(args[2])) {
            sendMessage(sender, message("PORTALMANAGER.INVALID-CUBOID",
                    "&cCuboid '&e{cuboid}&c' does not exist.")
                    .replace("{cuboid}", args[2]));
            return;
        }

        plugin.getPortalManager().setPortalCuboid(args[1], args[2]);
        sendMessage(sender, message("PORTALMANAGER.UPDATED",
                "&aPortal &d{id} &ahas been updated.")
                .replace("{id}", plugin.getPortalManager().normalizeId(args[1])));
    }

    private void handleSetDestination(CommandSender sender, String label, String[] args) {
        if (args.length != 3) {
            sendMessage(sender, message("PORTALMANAGER.SETDESTINATION-USAGE",
                    "&cUsage: /" + label + " setdestination <id> <rtp_selector>"));
            return;
        }

        if (plugin.getPortalManager().getPortal(args[1]) == null) {
            sendMessage(sender, portalNotFound(args[1]));
            return;
        }

        if (!plugin.getRtpManager().isPortalDestinationAvailable(args[2])) {
            sendMessage(sender, message("PORTALMANAGER.INVALID-DESTINATION",
                    "&cRTP destination '&e{destination}&c' is unavailable.")
                    .replace("{destination}", args[2]));
            return;
        }

        plugin.getPortalManager().setPortalDestination(args[1], args[2]);
        sendMessage(sender, message("PORTALMANAGER.UPDATED",
                "&aPortal &d{id} &ahas been updated.")
                .replace("{id}", plugin.getPortalManager().normalizeId(args[1])));
    }

    private void handleSetDisplay(CommandSender sender, String label, String[] args) {
        if (args.length < 3) {
            sendMessage(sender, message("PORTALMANAGER.SETDISPLAY-USAGE",
                    "&cUsage: /" + label + " setdisplay <id> <display name...>"));
            return;
        }

        if (plugin.getPortalManager().getPortal(args[1]) == null) {
            sendMessage(sender, portalNotFound(args[1]));
            return;
        }

        String displayName = String.join(" ", Arrays.copyOfRange(args, 2, args.length)).trim();
        if (displayName.isBlank()) {
            sendMessage(sender, message("PORTALMANAGER.SETDISPLAY-USAGE",
                    "&cUsage: /" + label + " setdisplay <id> <display name...>"));
            return;
        }

        plugin.getPortalManager().setPortalDisplayName(args[1], displayName);
        sendMessage(sender, message("PORTALMANAGER.UPDATED",
                "&aPortal &d{id} &ahas been updated.")
                .replace("{id}", plugin.getPortalManager().normalizeId(args[1])));
    }

    private void handleToggle(CommandSender sender, String label, String[] args) {
        if (args.length != 2) {
            sendMessage(sender, message("PORTALMANAGER.TOGGLE-USAGE",
                    "&cUsage: /" + label + " toggle <id>"));
            return;
        }

        PortalDefinition portal = plugin.getPortalManager().getPortal(args[1]);
        if (portal == null) {
            sendMessage(sender, portalNotFound(args[1]));
            return;
        }

        boolean nextState = !portal.enabled();
        plugin.getPortalManager().setPortalEnabled(portal.id(), nextState);
        sendMessage(sender, message("PORTALMANAGER.TOGGLED",
                "&aPortal &d{id} &ais now &f{state}&a.")
                .replace("{id}", portal.id())
                .replace("{state}", nextState ? "enabled" : "disabled"));
    }

    private void handleSetPriority(CommandSender sender, String label, String[] args) {
        if (args.length != 3) {
            sendMessage(sender, message("PORTALMANAGER.SETPRIORITY-USAGE",
                    "&cUsage: /" + label + " setpriority <id> <number>"));
            return;
        }

        if (plugin.getPortalManager().getPortal(args[1]) == null) {
            sendMessage(sender, portalNotFound(args[1]));
            return;
        }

        int priority;
        try {
            priority = Integer.parseInt(args[2]);
        } catch (NumberFormatException exception) {
            sendMessage(sender, message("PORTALMANAGER.INVALID-PRIORITY",
                    "&cPriority must be a whole number."));
            return;
        }

        plugin.getPortalManager().setPortalPriority(args[1], priority);
        sendMessage(sender, message("PORTALMANAGER.UPDATED",
                "&aPortal &d{id} &ahas been updated.")
                .replace("{id}", plugin.getPortalManager().normalizeId(args[1])));
    }

    private void handleSetHologramHere(CommandSender sender, String label, String[] args) {
        if (args.length != 2) {
            sendMessage(sender, message("PORTALMANAGER.SETHOLOGRAMHERE-USAGE",
                    "&cUsage: /" + label + " sethologramhere <id>"));
            return;
        }

        if (!(sender instanceof Player player)) {
            sendMessage(sender, message("PORTALMANAGER.PLAYER-ONLY",
                    "&cOnly players can use this command."));
            return;
        }

        if (plugin.getPortalManager().getPortal(args[1]) == null) {
            sendMessage(sender, portalNotFound(args[1]));
            return;
        }

        Location hologramLocation = player.getLocation().clone().add(0D, getSetHereOffsetY(), 0D);
        plugin.getPortalManager().setPortalHologramLocation(args[1], hologramLocation);
        sendMessage(sender, message("PORTALMANAGER.HOLOGRAM-UPDATED",
                "&aPortal &d{id} &ahologram has been moved to your location.")
                .replace("{id}", plugin.getPortalManager().normalizeId(args[1])));
    }

    private String usage(String label) {
        return message("PORTALMANAGER.USAGE",
                "&cUsage: /" + label + " <list|info|create|delete|setcuboid|setdestination|setdisplay|toggle|setpriority|sethologramhere>");
    }

    private String portalNotFound(String id) {
        return message("PORTALMANAGER.NOT-FOUND",
                "&cPortal '&e{id}&c' not found.")
                .replace("{id}", id);
    }

    private String describeState(PortalDefinition portal) {
        String stateKey = plugin.getPortalManager().getPortalStateKey(portal);
        return switch (stateKey) {
            case "READY" -> message("PORTAL.STATUS-READY", "&aready");
            case "DISABLED" -> message("PORTAL.STATUS-DISABLED", "&cdisabled");
            case "INVALID_CUBOID" -> message("PORTAL.STATUS-INVALID-CUBOID", "&einvalid cuboid");
            case "INVALID_DESTINATION" -> message("PORTAL.STATUS-INVALID-DESTINATION", "&einvalid destination");
            default -> "&7unknown";
        };
    }

    private String formatHologramLocation(PortalDefinition portal) {
        if (!portal.hasCustomHologramLocation()) {
            return "auto";
        }

        return portal.hologramWorld()
                + " "
                + formatCoordinate(portal.hologramX())
                + ", "
                + formatCoordinate(portal.hologramY())
                + ", "
                + formatCoordinate(portal.hologramZ());
    }

    private String formatCoordinate(double value) {
        return String.format(Locale.US, "%.2f", value);
    }

    private double getSetHereOffsetY() {
        return plugin.getConfigManager().getConfig()
                .getDouble("PORTAL-SYSTEM.HOLOGRAM.SET-HERE-OFFSET-Y", 1.6D);
    }

    private void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(ColorUtils.colorize(message));
    }

    private String message(String path, String fallback) {
        return plugin.getConfigManager().getMessageOrDefault(path, fallback);
    }
}
