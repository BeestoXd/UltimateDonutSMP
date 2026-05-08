package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.SpawnManager;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class TeleportAreaMenu extends BaseMenu {

    private final Map<Integer, SpawnManager.TeleportArea> slotAreas = new HashMap<>();
    private boolean randomEnabled;

    protected TeleportAreaMenu(UltimateDonutSmp plugin, String title, int size) {
        super(plugin, title, normalizeSize(size));
    }

    protected abstract SpawnManager.AreaType getAreaType();

    protected abstract String getMenuPath();

    protected abstract String getTeleportType();

    protected abstract String getEmptyTitle();

    protected abstract String getEmptyLore();

    @Override
    public void build(Player player) {
        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);
        slotAreas.clear();
        randomEnabled = false;

        List<SpawnManager.TeleportArea> areas = plugin.getSpawnManager().getValidAreas(getAreaType());
        if (areas.isEmpty()) {
            set(inventory.getSize() / 2, ItemUtils.createItem(
                    Material.BARRIER,
                    getEmptyTitle(),
                    List.of(getEmptyLore())
            ));
            return;
        }

        for (SpawnManager.TeleportArea area : areas) {
            set(area.slot(), ItemUtils.createItem(
                    area.material(),
                    replaceAreaPlaceholders(area.displayName(), area),
                    replaceAreaPlaceholders(area.lore(), area)
            ));
            slotAreas.put(area.slot(), area);
        }

        if (areas.size() > 1) {
            buildRandomButton(areas.size());
        }
    }

    @Override
    public void handleClick(int slot, Player player) {
        if (randomEnabled && slot == getRandomSlot()) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            SpawnManager.TeleportArea randomArea = plugin.getSpawnManager().getRandomArea(getAreaType());
            if (randomArea == null) {
                player.sendMessage(ColorUtils.toComponent("&cNo valid area is available right now."));
                return;
            }
            queueTeleport(player, randomArea);
            return;
        }

        SpawnManager.TeleportArea area = slotAreas.get(slot);
        if (area == null) {
            return;
        }

        SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
        queueTeleport(player, area);
    }

    private void buildRandomButton(int areaCount) {
        int slot = getRandomSlot();
        if (slot < 0 || slot >= inventory.getSize()) {
            return;
        }

        FileConfiguration menus = plugin.getConfigManager().getMenus();
        String path = getMenuPath() + ".RANDOM-BUTTON";
        randomEnabled = true;

        set(slot, ItemUtils.createItem(
                ItemUtils.parseMaterial(menus.getString(path + ".MATERIAL", "COMPASS")),
                replaceGlobalPlaceholders(menus.getString(path + ".DISPLAY-NAME", "&fRandom"), areaCount),
                replaceGlobalPlaceholders(menus.getStringList(path + ".LORE"), areaCount)
        ));
    }

    private void queueTeleport(Player player, SpawnManager.TeleportArea area) {
        Location destination = plugin.getSpawnManager().resolveDestination(area);
        if (destination == null) {
            player.sendMessage(ColorUtils.toComponent("&cThis area does not have a valid teleport destination."));
            return;
        }

        player.closeInventory();
        plugin.getTeleportManager().queue(player, destination, getTeleportType(), null);
    }

    private int getRandomSlot() {
        return plugin.getConfigManager().getMenus().getInt(getMenuPath() + ".RANDOM-BUTTON.SLOT", -1);
    }

    private List<String> replaceAreaPlaceholders(List<String> lines, SpawnManager.TeleportArea area) {
        List<String> replaced = new ArrayList<>();
        for (String line : lines) {
            replaced.add(replaceAreaPlaceholders(line, area));
        }
        return replaced;
    }

    private String replaceAreaPlaceholders(String text, SpawnManager.TeleportArea area) {
        return text.replace("{players}", String.valueOf(plugin.getSpawnManager().countPlayersInArea(area)))
                .replace("{capacity}", String.valueOf(area.capacity()))
                .replace("{cuboid}", area.cuboidName())
                .replace("{id}", area.id());
    }

    private List<String> replaceGlobalPlaceholders(List<String> lines, int areaCount) {
        List<String> replaced = new ArrayList<>();
        for (String line : lines) {
            replaced.add(replaceGlobalPlaceholders(line, areaCount));
        }
        return replaced;
    }

    private String replaceGlobalPlaceholders(String text, int areaCount) {
        return text.replace("{areas}", String.valueOf(areaCount));
    }

    private static int normalizeSize(int size) {
        int normalized = Math.max(9, ((size + 8) / 9) * 9);
        return Math.min(54, normalized);
    }
}
