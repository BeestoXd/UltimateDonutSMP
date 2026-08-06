package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.*;

import java.util.List;

public class ClearLagManager {

    private final UltimateDonutSmp plugin;
    private int countdown;
    private boolean running = false;

    public ClearLagManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public boolean isEnabled() {
        return plugin.getFeatureManager().isEnabled(FeatureManager.Feature.CLEAR_LAG)
                && plugin.getConfigManager().getConfig().getBoolean("CLEAR-LAG.ENABLED", true);
    }

    public int getIntervalMinutes() {
        return plugin.getConfigManager().getConfig().getInt("CLEAR-LAG.EVERY", 5);
    }

    public boolean clearAnimals() {
        return plugin.getConfigManager().getConfig().getBoolean("CLEAR-LAG.ANIMALS", false);
    }

    public boolean clearMonsters() {
        return plugin.getConfigManager().getConfig().getBoolean("CLEAR-LAG.MONSTERS", false);
    }

    public boolean clearDroppedItems() {
        return plugin.getConfigManager().getConfig().getBoolean("CLEAR-LAG.DROPPED-ITEMS", true);
    }

    public List<String> getExcludedWorlds() {
        return plugin.getConfigManager().getConfig().getStringList("CLEAR-LAG.EXCLUDED-WORLDS");
    }

    public boolean excludeNamed() {
        return plugin.getConfigManager().getConfig().getBoolean("CLEAR-LAG.EXCLUDE-NAMED", true);
    }

    public boolean excludeTamed() {
        return plugin.getConfigManager().getConfig().getBoolean("CLEAR-LAG.EXCLUDE-TAMED", true);
    }

    public boolean excludeVillagers() {
        return plugin.getConfigManager().getConfig().getBoolean("CLEAR-LAG.EXCLUDE-VILLAGERS", true);
    }

    public List<String> getExcludedEntityTypes() {
        return plugin.getConfigManager().getConfig().getStringList("CLEAR-LAG.EXCLUDED-ENTITY-TYPES");
    }

    public List<String> getExcludedItemMaterials() {
        return plugin.getConfigManager().getConfig().getStringList("CLEAR-LAG.EXCLUDED-ITEM-MATERIALS");
    }

    public int clearEntities() {
        List<String> excludedWorlds = getExcludedWorlds();
        boolean checkNamed = excludeNamed();
        boolean checkTamed = excludeTamed();
        boolean checkVillagers = excludeVillagers();
        List<String> excludedTypes = getExcludedEntityTypes();
        List<String> excludedMaterials = getExcludedItemMaterials();

        if (plugin.getSpigotScheduler().isFolia()) {
            java.util.concurrent.atomic.AtomicInteger count = new java.util.concurrent.atomic.AtomicInteger(0);
            java.util.concurrent.atomic.AtomicInteger pending = new java.util.concurrent.atomic.AtomicInteger(0);

            for (World world : Bukkit.getWorlds()) {
                if (excludedWorlds.contains(world.getName())) continue;
                for (Entity entity : world.getEntities()) {
                    if (entity instanceof Player) continue;
                    pending.incrementAndGet();
                    plugin.getSpigotScheduler().runEntity(entity, () -> {
                        try {
                            if (!entity.isValid()) return;

                            if (checkNamed && entity.getCustomName() != null) return;
                            if (checkTamed && entity instanceof Tameable tameable && tameable.isTamed()) return;
                            if (checkVillagers && (entity instanceof AbstractVillager || entity instanceof NPC)) return;

                            boolean typeExcluded = false;
                            String typeName = entity.getType().name();
                            for (String t : excludedTypes) {
                                if (typeName.equalsIgnoreCase(t)) {
                                    typeExcluded = true;
                                    break;
                                }
                            }
                            if (typeExcluded) return;

                            boolean remove = false;
                            if (entity instanceof Item item) {
                                if (clearDroppedItems()) {
                                    String materialName = item.getItemStack().getType().name();
                                    boolean materialExcluded = false;
                                    for (String mat : excludedMaterials) {
                                        if (materialName.equalsIgnoreCase(mat)) {
                                            materialExcluded = true;
                                            break;
                                        }
                                    }
                                    if (!materialExcluded) {
                                        remove = true;
                                    }
                                }
                            } else if (entity instanceof Animals) {
                                if (clearAnimals()) {
                                    remove = true;
                                }
                            } else if (entity instanceof Monster || entity instanceof Enemy || entity instanceof Slime || entity instanceof Flying) {
                                if (clearMonsters()) {
                                    remove = true;
                                }
                            }

                            if (remove) {
                                entity.remove();
                                count.incrementAndGet();
                            }
                        } finally {
                            if (pending.decrementAndGet() == 0) {
                                plugin.getSpigotScheduler().runGlobal(() -> broadcastSuccess(count.get()));
                            }
                        }
                    });
                }
            }
            if (pending.get() == 0) {
                broadcastSuccess(0);
            }
            return 0;
        }

        int count = 0;
        for (World world : Bukkit.getWorlds()) {
            if (excludedWorlds.contains(world.getName())) continue;
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Player) continue;

                // Check named exclusion
                if (checkNamed && entity.getCustomName() != null) continue;

                // Check tamed exclusion
                if (checkTamed && entity instanceof Tameable tameable && tameable.isTamed()) continue;

                // Check villagers/NPCs exclusion
                if (checkVillagers && (entity instanceof AbstractVillager || entity instanceof NPC)) continue;

                // Check excluded entity type
                boolean typeExcluded = false;
                String typeName = entity.getType().name();
                for (String t : excludedTypes) {
                    if (typeName.equalsIgnoreCase(t)) {
                        typeExcluded = true;
                        break;
                    }
                }
                if (typeExcluded) continue;

                boolean remove = false;
                if (entity instanceof Item item) {
                    if (clearDroppedItems()) {
                        String materialName = item.getItemStack().getType().name();
                        boolean materialExcluded = false;
                        for (String mat : excludedMaterials) {
                            if (materialName.equalsIgnoreCase(mat)) {
                                materialExcluded = true;
                                break;
                            }
                        }
                        if (!materialExcluded) {
                            remove = true;
                        }
                    }
                } else if (entity instanceof Animals) {
                    if (clearAnimals()) {
                        remove = true;
                    }
                } else if (entity instanceof Monster || entity instanceof Enemy || entity instanceof Slime || entity instanceof Flying) {
                    if (clearMonsters()) {
                        remove = true;
                    }
                }

                if (remove) {
                    entity.remove();
                    count++;
                }
            }
        }
        broadcastSuccess(count);
        return count;
    }

    public void broadcastCountdown(int seconds) {
        String msg = plugin.getConfigManager().getMessage("CLEAR-LAG.COUNTDOWN",
                "{seconds}", String.valueOf(seconds));
        broadcastToSubscribedPlayers(msg);
    }

    public void broadcastSuccess(int total) {
        String msg = plugin.getConfigManager().getMessage("CLEAR-LAG.SUCCESS",
                "{total}", String.valueOf(total));
        broadcastToSubscribedPlayers(msg);
    }

    private void broadcastToSubscribedPlayers(String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData data = plugin.getPlayerDataManager().get(player);
            if (data != null && !data.isClearEntitiesMessagesEnabled()) {
                continue;
            }

            player.sendMessage(ColorUtils.toComponent(message));
        }
    }
}
