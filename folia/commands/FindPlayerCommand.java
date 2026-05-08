package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Locale;

public class FindPlayerCommand implements CommandExecutor {

    private static final String DEFAULT_AFK = "&7{player}'s in the &#A303F9afk";
    private static final String DEFAULT_RTP_ZONE = "&7{player}'s in the &crtpzone";
    private static final String DEFAULT_SPAWN = "&7{player}'s in the &bspawn";
    private static final String DEFAULT_OVERWORLD = "&7{player}'s in the &boverworld &7(&b{biome}&7)";
    private static final String DEFAULT_NETHER = "&7{player}'s in the &bnether &7(&b{biome}&7)";
    private static final String DEFAULT_THE_END = "&7{player}'s in the &bthe end &7(&b{biome}&7)";
    private static final String DEFAULT_UNKNOWN = "&7{player}'s in the &b{world}";

    private final UltimateDonutSmp plugin;

    public FindPlayerCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("Player only."); return true; }
        if (args.length == 0) { player.sendMessage(ColorUtils.toComponent("&cUsage: /findplayer <player>")); return true; }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) { player.sendMessage(ColorUtils.toComponent("&cPlayer not online.")); return true; }

        LocationMessage locationMessage = resolveLocationMessage(target);
        String msg = plugin.getConfigManager().getMessageOrDefault(
                locationMessage.key(),
                locationMessage.fallback(),
                "{player}", target.getName(),
                "{world}", friendlyWorldName(target.getWorld()),
                "{biome}", formatBiome(target.getLocation())
        );
        player.sendMessage(ColorUtils.toComponent(msg));
        return true;
    }

    private LocationMessage resolveLocationMessage(Player target) {
        if (plugin.getAFKManager().isAfk(target.getUniqueId())) {
            return new LocationMessage("FINDPLAYER.AFK", DEFAULT_AFK);
        }
        if (plugin.getRtpZoneManager().isInZone(target)) {
            return new LocationMessage("FINDPLAYER.RTP_ZONE", DEFAULT_RTP_ZONE);
        }
        if (plugin.getAFKManager().isInSpawnCuboid(target)) {
            return new LocationMessage("FINDPLAYER.SPAWN", DEFAULT_SPAWN);
        }

        World world = target.getWorld();
        if (world == null) {
            return new LocationMessage("FINDPLAYER.UNKNOWN", DEFAULT_UNKNOWN);
        }

        return switch (world.getEnvironment()) {
            case NORMAL -> new LocationMessage("FINDPLAYER.OVERWORLD", DEFAULT_OVERWORLD);
            case NETHER -> new LocationMessage("FINDPLAYER.NETHER", DEFAULT_NETHER);
            case THE_END -> new LocationMessage("FINDPLAYER.THE_END", DEFAULT_THE_END);
            case CUSTOM -> new LocationMessage("FINDPLAYER.UNKNOWN", DEFAULT_UNKNOWN);
        };
    }

    private String friendlyWorldName(World world) {
        if (world == null) {
            return "Unknown";
        }

        return switch (world.getEnvironment()) {
            case NORMAL -> "Overworld";
            case NETHER -> "Nether";
            case THE_END -> "The End";
            case CUSTOM -> formatIdentifier(world.getName());
        };
    }

    private String formatBiome(Location location) {
        if (location == null || location.getWorld() == null) {
            return "Unknown";
        }

        Biome biome = location.getBlock().getBiome();
        if (biome == null) {
            return "Unknown";
        }

        return formatIdentifier(biome.getKey().getKey());
    }

    private String formatIdentifier(String value) {
        if (value == null || value.isBlank()) {
            return "Unknown";
        }

        String normalized = value;
        int namespaceSeparator = normalized.indexOf(':');
        if (namespaceSeparator >= 0 && namespaceSeparator + 1 < normalized.length()) {
            normalized = normalized.substring(namespaceSeparator + 1);
        }

        normalized = normalized.replace('-', '_').replace(' ', '_').toLowerCase(Locale.ROOT);
        String[] parts = normalized.split("_+");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                builder.append(part.substring(1));
            }
        }

        return builder.isEmpty() ? "Unknown" : builder.toString();
    }

    private record LocationMessage(String key, String fallback) {
    }
}
