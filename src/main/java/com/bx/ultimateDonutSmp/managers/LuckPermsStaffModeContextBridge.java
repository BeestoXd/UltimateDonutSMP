package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.context.ContextCalculator;
import net.luckperms.api.context.ContextConsumer;
import net.luckperms.api.context.ContextSet;
import net.luckperms.api.context.ImmutableContextSet;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public final class LuckPermsStaffModeContextBridge {

    private final UltimateDonutSmp plugin;
    private StaffModeContextCalculator calculator;
    private boolean active = false;

    public LuckPermsStaffModeContextBridge(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public void start() {
        shutdown();

        if (Bukkit.getPluginManager().getPlugin("LuckPerms") == null) {
            return;
        }

        boolean enabled = plugin.getConfigManager().getStaffMode()
                .getBoolean("STAFF-MODE.LUCKPERMS-CONTEXT.ENABLED", true);
        if (!enabled) {
            return;
        }

        try {
            LuckPerms luckPerms = LuckPermsProvider.get();
            calculator = new StaffModeContextCalculator();
            luckPerms.getContextManager().registerCalculator(calculator);
            active = true;
            plugin.getLogger().info("LuckPerms Staff Mode context provider registered successfully.");
        } catch (Throwable error) {
            active = false;
            calculator = null;
            plugin.getLogger().log(Level.WARNING, "Failed to register LuckPerms Staff Mode context calculator.", error);
        }
    }

    public void shutdown() {
        if (active && calculator != null) {
            try {
                if (Bukkit.getPluginManager().getPlugin("LuckPerms") != null) {
                    LuckPermsProvider.get().getContextManager().unregisterCalculator(calculator);
                }
            } catch (Throwable ignored) {
            }
        }
        active = false;
        calculator = null;
    }

    public void signalContextUpdate(Player player) {
        if (!active || player == null) {
            return;
        }

        try {
            LuckPermsProvider.get().getContextManager().signalContextUpdate(player);
        } catch (Throwable error) {
            plugin.getLogger().log(Level.FINE, "Failed to signal LuckPerms context update for player: " + player.getName(), error);
        }
    }

    public boolean isActive() {
        return active;
    }

    private String getContextKey() {
        String key = plugin.getConfigManager().getStaffMode()
                .getString("STAFF-MODE.LUCKPERMS-CONTEXT.KEY", "staffmode");
        return (key == null || key.isBlank()) ? "staffmode" : key.trim().toLowerCase();
    }

    private final class StaffModeContextCalculator implements ContextCalculator<Player> {

        @Override
        public void calculate(Player target, ContextConsumer consumer) {
            if (plugin.getStaffModeManager() == null || target == null) {
                return;
            }

            boolean inStaffMode = plugin.getStaffModeManager().isInStaffMode(target.getUniqueId());
            String key = getContextKey();
            consumer.accept(key, inStaffMode ? "true" : "false");
        }

        @Override
        public ContextSet estimatePotentialContexts() {
            String key = getContextKey();
            return ImmutableContextSet.builder()
                    .add(key, "true")
                    .add(key, "false")
                    .build();
        }
    }
}
