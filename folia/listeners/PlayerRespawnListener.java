package com.bx.ultimateDonutSmp.listeners;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PlayerRespawnListener implements Listener {

    private final UltimateDonutSmp plugin;

    public PlayerRespawnListener(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        boolean duelRespawnHandled = plugin.getDuelManager() != null
                && plugin.getDuelManager().consumeRespawn(player, event);
        boolean ffaRespawnHandled = !duelRespawnHandled
                && plugin.getFfaManager() != null
                && plugin.getFfaManager().consumeRespawn(player, event);

        if (!duelRespawnHandled && !ffaRespawnHandled) {
            Location respawnLocation = plugin.getSpawnManager().getSpawnLocation();
            if (respawnLocation == null) {
                respawnLocation = plugin.getSpawnManager().makeSafeDestination(event.getRespawnLocation());
            }
            if (respawnLocation != null) {
                Location finalRespawnLocation = respawnLocation.clone();
                event.setRespawnLocation(finalRespawnLocation);
                plugin.getFoliaScheduler().runEntityLater(player, () -> {
                    if (player.isOnline() && shouldSnapToRespawnLocation(player.getLocation(), finalRespawnLocation)) {
                        plugin.getFoliaScheduler().teleport(player, finalRespawnLocation);
                    }
                }, 1L);
            }
        }

        plugin.getStaffModeManager().handleRespawn(player);

        if (!ffaRespawnHandled) {
            if (plugin.getStaffModeManager().isInStaffMode(player.getUniqueId())) {
                return;
            }
            scheduleChainmailKit(plugin, player, 2L);
        }
    }

    private boolean shouldSnapToRespawnLocation(Location current, Location expected) {
        if (current == null || expected == null || current.getWorld() == null || expected.getWorld() == null) {
            return false;
        }
        if (!current.getWorld().equals(expected.getWorld())) {
            return true;
        }
        return current.distanceSquared(expected) > 0.36D;
    }

    public static void scheduleChainmailKit(UltimateDonutSmp plugin, Player player, long delayTicks) {
        if (plugin == null || player == null) {
            return;
        }

        boolean chainmailEnabled = plugin.getConfigManager().getConfig()
                .getBoolean("SETTINGS.CHAINMAIL-ON-RESPAWN", true);
        PlayerData data = plugin.getPlayerDataManager().get(player);
        boolean playerEnabled = data == null || data.isChainmailOnRespawnEnabled();
        if (!chainmailEnabled || !playerEnabled) {
            return;
        }

        plugin.getFoliaScheduler().runEntityLater(player, () -> {
            if (player.isOnline()) {
                giveChainmailKit(plugin, player);
            }
        }, Math.max(0L, delayTicks));
    }

    private static void giveChainmailKit(UltimateDonutSmp plugin, Player player) {
        List<?> itemList = plugin.getConfigManager().getConfig()
                .getList("SETTINGS.CHAINMAIL-RESPAWN-ITEMS");
        Set<Material> grantedMaterials = new HashSet<>();
        if (itemList != null) {
            for (Object obj : itemList) {
                if (!(obj instanceof ConfigurationSection section)) continue;
                Material mat = ItemUtils.parseMaterial(section.getString("MATERIAL", "STONE"));
                int amount = section.getInt("AMOUNT", 1);
                String name = section.getString("NAME");

                ItemStack item;
                if (name != null) {
                    item = ItemUtils.createItem(mat, name);
                    item.setAmount(amount);
                } else {
                    item = new ItemStack(mat, amount);
                }

                grantedMaterials.add(mat);
                giveRespawnItem(player, item);
            }
        }

        ensureDefaultItem(player, grantedMaterials, Material.CHAINMAIL_HELMET);
        ensureDefaultItem(player, grantedMaterials, Material.CHAINMAIL_CHESTPLATE);
        ensureDefaultItem(player, grantedMaterials, Material.CHAINMAIL_LEGGINGS);
        ensureDefaultItem(player, grantedMaterials, Material.CHAINMAIL_BOOTS);
        ensureDefaultItem(player, grantedMaterials, Material.STONE_SWORD);
        ensureDefaultItem(player, grantedMaterials, Material.STONE_PICKAXE);
        ensureDefaultItem(player, grantedMaterials, Material.STONE_AXE);
        ensureDefaultItem(player, grantedMaterials, Material.STONE_SHOVEL);

        player.updateInventory();
    }

    private static void ensureDefaultItem(Player player, Set<Material> grantedMaterials, Material material) {
        if (grantedMaterials.contains(material)) {
            return;
        }

        giveRespawnItem(player, new ItemStack(material));
    }

    private static void giveRespawnItem(Player player, ItemStack item) {
        if (equipArmorIfApplicable(player, item)) {
            return;
        }

        placeInPreferredSlot(player, item);
    }

    private static boolean equipArmorIfApplicable(Player player, ItemStack item) {
        PlayerInventory inventory = player.getInventory();

        switch (item.getType()) {
            case CHAINMAIL_HELMET -> {
                if (isEmpty(inventory.getHelmet())) {
                    inventory.setHelmet(item);
                } else {
                    inventory.addItem(item);
                }
                return true;
            }
            case CHAINMAIL_CHESTPLATE -> {
                if (isEmpty(inventory.getChestplate())) {
                    inventory.setChestplate(item);
                } else {
                    inventory.addItem(item);
                }
                return true;
            }
            case CHAINMAIL_LEGGINGS -> {
                if (isEmpty(inventory.getLeggings())) {
                    inventory.setLeggings(item);
                } else {
                    inventory.addItem(item);
                }
                return true;
            }
            case CHAINMAIL_BOOTS -> {
                if (isEmpty(inventory.getBoots())) {
                    inventory.setBoots(item);
                } else {
                    inventory.addItem(item);
                }
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    private static void placeInPreferredSlot(Player player, ItemStack item) {
        PlayerInventory inventory = player.getInventory();
        int preferredSlot = getPreferredHotbarSlot(item.getType());

        if (preferredSlot >= 0 && isEmpty(inventory.getItem(preferredSlot))) {
            inventory.setItem(preferredSlot, item);
            return;
        }

        inventory.addItem(item);
    }

    private static int getPreferredHotbarSlot(Material material) {
        return switch (material) {
            case STONE_SWORD -> 0;
            case STONE_PICKAXE -> 1;
            case STONE_AXE -> 2;
            case STONE_SHOVEL -> 3;
            default -> -1;
        };
    }

    private static boolean isEmpty(ItemStack item) {
        return item == null || item.getType().isAir();
    }
}
