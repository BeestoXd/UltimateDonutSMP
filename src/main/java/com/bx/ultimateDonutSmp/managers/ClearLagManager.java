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
        return plugin.getConfigManager().getConfig().getBoolean("CLEAR-LAG.ANIMALS", true);
    }

    public boolean clearMonsters() {
        return plugin.getConfigManager().getConfig().getBoolean("CLEAR-LAG.MONSTERS", true);
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
        List<String> excludedTypes = getExcludedEntityTypes();
        List<String> excludedMaterials = getExcludedItemMaterials();

        int count = 0;
        for (World world : Bukkit.getWorlds()) {
            if (excludedWorlds.contains(world.getName())) continue;
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Player) continue;

                // Check named exclusion
                if (checkNamed && entity.getCustomName() != null) continue;

                // Check tamed exclusion
                if (checkTamed && entity instanceof Tameable && ((Tameable) entity).isTamed()) continue;

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
                if (entity instanceof Item) {
                    if (clearDroppedItems()) {
                        Item item = (Item) entity;
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
                } else if (entity instanceof Monster) {
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
