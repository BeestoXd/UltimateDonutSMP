package com.bx.ultimateDonutSmp.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import com.bx.ultimateDonutSmp.UltimateDonutSmp;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class SignInputUtil {

    private static final Set<String> REGISTERED = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, JavaPlugin> PLUGINS = new ConcurrentHashMap<>();
    private static final Map<UUID, Location> SIGN_LOC = new ConcurrentHashMap<>();
    private static final Map<UUID, BlockData> OLD_DATA = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> INPUT_LINE = new ConcurrentHashMap<>();
    private static final Map<UUID, Consumer<String>> CALLBACK = new ConcurrentHashMap<>();
    private static final Map<UUID, org.bukkit.scheduler.BukkitTask> HIDE_TASK = new ConcurrentHashMap<>();
    private static final Map<UUID, String[]> EXPECTED_LINES = new ConcurrentHashMap<>();
    private static final Map<UUID, List<String>> ORIGINAL_LINES = new ConcurrentHashMap<>();

    private static com.bx.ultimateDonutSmp.utils.SpigotScheduler getScheduler(JavaPlugin plugin) {
        if (plugin instanceof UltimateDonutSmp uds) {
            return uds.getSpigotScheduler();
        }
        UltimateDonutSmp uds = (UltimateDonutSmp) Bukkit.getPluginManager().getPlugin("UltimateDonutSmp");
        if (uds != null) {
            return uds.getSpigotScheduler();
        }
        return null;
    }

    public static final String META_SIGN_INPUT = "donutorder-sign-input";

    private SignInputUtil() {}

    public static void openFromConfig(JavaPlugin plugin, Player player, org.bukkit.configuration.ConfigurationSection config, Consumer<String> callback) {
        if (config == null) {
            callback.accept(null);
            return;
        }
        List<String> lines = config.getStringList("lines");
        int inputLine = config.getInt("input-line", 1);
        open(plugin, player, lines, inputLine, callback);
    }

    public static void open(JavaPlugin plugin, Player player, List<String> lines, int inputLine, Consumer<String> callback) {
        cancel(player);
        if (!player.isOnline()) {
            if (callback != null) {
                callback.accept(null);
            }
            return;
        }

        UUID uuid = player.getUniqueId();
        int lineIndex = Math.max(0, Math.min(3, inputLine));

        List<String> list = new ArrayList<>();
        if (lines != null) {
            list.addAll(lines);
        }
        while (list.size() < 4) {
            list.add("");
        }
        if (list.size() > 4) {
            list = list.subList(0, 4);
        }

        String[] signLines = new String[4];
        for (int i = 0; i < 4; i++) {
            String cleanLine = list.get(i);
            if (cleanLine == null) {
                cleanLine = "";
            }
            cleanLine = org.bukkit.ChatColor.translateAlternateColorCodes('&', cleanLine);
            signLines[i] = org.bukkit.ChatColor.stripColor(cleanLine);
        }

        Placement placement = findGroundPlacement(player);
        if (placement == null) {
            if (callback != null) {
                callback.accept(null);
            }
            return;
        }

        ensureRegistered(plugin);

        PLUGINS.put(uuid, plugin);
        SIGN_LOC.put(uuid, placement.loc);
        OLD_DATA.put(uuid, placement.oldData);
        INPUT_LINE.put(uuid, lineIndex);
        EXPECTED_LINES.put(uuid, signLines);
        ORIGINAL_LINES.put(uuid, list);
        if (callback != null) {
            CALLBACK.put(uuid, callback);
        }

        player.setMetadata(META_SIGN_INPUT, new FixedMetadataValue(plugin, true));

        var scheduler = getScheduler(plugin);

        // Close inventory immediately so player is ready for sign input
        try {
            player.closeInventory();
        } catch (Throwable ignored) {}

        // Schedule timeout (45 seconds)
        if (scheduler != null) {
            org.bukkit.scheduler.BukkitTask task = scheduler.runEntityLater(player, () -> {
                if (PLUGINS.containsKey(uuid)) {
                    cancel(player);
                    player.sendMessage(org.bukkit.ChatColor.RED + "ѕɪɢɴ ɪɴᴘᴜᴛ ᴛɪᴍᴇᴅ ᴏᴜᴛ.");
                }
            }, 900L);
            HIDE_TASK.put(uuid, task);
        }

        Location loc = placement.loc.clone();
        BlockData oldData = placement.oldData;

        Runnable openAction = () -> {
            Block block = loc.getBlock();
            block.setType(Material.OAK_SIGN, false);

            if (!(block.getState() instanceof Sign sign)) {
                finish(player, null);
                return;
            }

            // Set sign lines (supporting 1.20+ Side API via reflection)
            try {
                Class<?> sideClass = Class.forName("org.bukkit.block.sign.Side");
                java.lang.reflect.Method getSideMethod = sign.getClass().getMethod("getSide", sideClass);
                Object frontSide = sideClass.getEnumConstants()[0]; // FRONT is index 0
                Object sideObject = getSideMethod.invoke(sign, frontSide);
                java.lang.reflect.Method setLineMethod = sideObject.getClass().getMethod("setLine", int.class, String.class);
                for (int i = 0; i < 4; i++) {
                    setLineMethod.invoke(sideObject, i, signLines[i]);
                }
            } catch (Throwable e) {
                // Fallback to pre-1.20
                for (int i = 0; i < 4; i++) {
                    sign.setLine(i, signLines[i]);
                }
            }

            // Set editable, unwaxed and allowed editor UUID
            try {
                java.lang.reflect.Method setEditable = sign.getClass().getMethod("setEditable", boolean.class);
                setEditable.invoke(sign, true);
            } catch (Throwable ignored) {}

            try {
                java.lang.reflect.Method setWaxed = sign.getClass().getMethod("setWaxed", boolean.class);
                setWaxed.invoke(sign, false);
            } catch (Throwable ignored) {}

            try {
                java.lang.reflect.Method setEditor = sign.getClass().getMethod("setAllowedEditorUniqueId", UUID.class);
                setEditor.invoke(sign, uuid);
            } catch (Throwable ignored) {}

            sign.update(true, false);

            // Open sign for player
            player.sendBlockChange(loc, sign.getBlockData());
            try {
                player.sendSignChange(loc, signLines);
            } catch (Throwable ignored) {}

            try {
                Class<?> sideClass = Class.forName("org.bukkit.block.sign.Side");
                Object frontSide = sideClass.getEnumConstants()[0];
                java.lang.reflect.Method openSignMethod = player.getClass().getMethod("openSign", Sign.class, sideClass);
                openSignMethod.invoke(player, sign, frontSide);
            } catch (Throwable t) {
                try {
                    player.openSign(sign);
                } catch (Throwable t2) {
                    finish(player, null);
                }
            }

            // Hide from others immediately
            startHideFromOthers(plugin, player, loc, oldData);
        };

        if (scheduler != null) {
            scheduler.runRegion(loc, openAction);
        } else {
            openAction.run();
        }
    }

    public static void cancel(Player player) {
        finish(player, null);
    }

    private static void finish(Player player, String text) {
        if (player == null) return;
        UUID uuid = player.getUniqueId();

        JavaPlugin plugin = PLUGINS.remove(uuid);
        Location loc = SIGN_LOC.remove(uuid);
        BlockData oldData = OLD_DATA.remove(uuid);
        INPUT_LINE.remove(uuid);
        EXPECTED_LINES.remove(uuid);
        ORIGINAL_LINES.remove(uuid);
        Consumer<String> callback = CALLBACK.remove(uuid);
        org.bukkit.scheduler.BukkitTask task = HIDE_TASK.remove(uuid);

        if (task != null) {
            task.cancel();
        }

        if (player.hasMetadata(META_SIGN_INPUT)) {
            player.removeMetadata(META_SIGN_INPUT, plugin);
        }

        var scheduler = getScheduler(plugin);

        if (loc != null && oldData != null && plugin != null && scheduler != null) {
            // Restore block in the world
            scheduler.runRegion(loc, () -> {
                Block block = loc.getBlock();
                block.setBlockData(oldData, false);
                player.sendBlockChange(loc, oldData);
                sendOriginalToOthers(plugin, player, loc, oldData);
            });
        }

        if (callback != null) {
            if (scheduler != null) {
                scheduler.runEntity(player, () -> callback.accept(text));
            } else {
                callback.accept(text);
            }
        }
    }

    private static Placement findGroundPlacement(Player player) {
        Location loc = player.getLocation().clone().add(0, 2.0, 0);
        int maxHeight = loc.getWorld().getMaxHeight() - 2;
        if (loc.getY() > maxHeight) {
            loc.setY(maxHeight);
        }
        Block block = loc.getBlock();
        return new Placement(block.getLocation(), block.getBlockData());
    }

    private static void startHideFromOthers(JavaPlugin plugin, Player player, Location loc, BlockData oldData) {
        for (Player other : loc.getWorld().getPlayers()) {
            if (!other.getUniqueId().equals(player.getUniqueId()) && other.getLocation().distanceSquared(loc) < 2500) {
                other.sendBlockChange(loc, oldData);
            }
        }
    }

    private static void sendOriginalToOthers(JavaPlugin plugin, Player player, Location loc, BlockData oldData) {
        startHideFromOthers(plugin, player, loc, oldData);
    }

    private static void ensureRegistered(JavaPlugin plugin) {
        String name = plugin.getName();
        if (REGISTERED.add(name)) {
            Bukkit.getPluginManager().registerEvents(new InternalListener(), plugin);
        }
    }

    private record Placement(Location loc, BlockData oldData) {}

    private static final class InternalListener implements Listener {

        @EventHandler
        public void onSignChange(SignChangeEvent event) {
            Player player = event.getPlayer();
            UUID uuid = player.getUniqueId();
            Location loc = SIGN_LOC.get(uuid);
            if (loc == null || !loc.getBlock().getLocation().equals(event.getBlock().getLocation())) {
                return;
            }

            event.setCancelled(true);
            int lineIdx = INPUT_LINE.getOrDefault(uuid, 1);

            String[] expected = EXPECTED_LINES.get(uuid);
            if (expected != null) {
                for (int i = 0; i < 4; i++) {
                    if (i == lineIdx) continue;
                    String currentLine = event.getLine(i);
                    if (currentLine == null) currentLine = "";
                    currentLine = org.bukkit.ChatColor.stripColor(currentLine).trim();
                    String expectedLine = expected[i];
                    if (expectedLine == null) expectedLine = "";
                    expectedLine = expectedLine.trim();
                    if (!currentLine.equals(expectedLine)) {
                        JavaPlugin plugin = PLUGINS.get(uuid);
                        String errorMsg = "&cyou cannot delete or modify the helper lines on the sign!";
                        if (plugin instanceof com.bx.ultimateDonutSmp.UltimateDonutSmp uds) {
                            errorMsg = uds.getConfigManager().getMessageOrDefault("ORDERS.SIGN_INPUT_HELPER_CANNOT_DELETE", errorMsg);
                        }
                        player.sendMessage(ColorUtils.toComponent(errorMsg));

                        List<String> origLines = ORIGINAL_LINES.get(uuid);
                        int inputIdx = INPUT_LINE.getOrDefault(uuid, 1);
                        Consumer<String> cb = CALLBACK.remove(uuid);

                        finish(player, null);

                        if (plugin != null && origLines != null && cb != null) {
                            var scheduler = getScheduler(plugin);
                            if (scheduler != null) {
                                scheduler.runEntity(player, () -> {
                                    open(plugin, player, origLines, inputIdx, cb);
                                });
                            }
                        }
                        return;
                    }
                }
            }

            String text = event.getLine(lineIdx);
            finish(player, text);
        }

        @EventHandler
        public void onQuit(PlayerQuitEvent event) {
            cancel(event.getPlayer());
        }
    }
}
