package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.SpawnerInstance;
import com.bx.ultimateDonutSmp.models.SpawnerTypeDefinition;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SpawnerFilterMenu extends BaseMenu {

    private final long spawnerId;
    private final int returnPage;
    private final Map<Integer, SpawnerTypeDefinition.DropDefinition> dropSlotMap = new HashMap<>();

    public SpawnerFilterMenu(UltimateDonutSmp plugin, long spawnerId, int returnPage) {
        super(plugin, "&8Filter Settings", 27);
        this.spawnerId = spawnerId;
        this.returnPage = Math.max(1, returnPage);
    }

    @Override
    public void build(Player player) {
        SpawnerInstance instance = plugin.getSpawnerManager().getSpawner(spawnerId);
        if (instance == null) {
            inventory = Bukkit.createInventory(this, 27, ColorUtils.toComponent("&8Spawner Missing"));
            clear();
            fill(Material.GRAY_STAINED_GLASS_PANE);
            set(13, ItemUtils.createItem(Material.BARRIER, "&cSpawner Not Found"));
            return;
        }

        SpawnerTypeDefinition def = plugin.getSpawnerManager().getTypeDefinition(instance.getMobTypeKey());
        FileConfiguration config = plugin.getConfigManager().getMenus();

        String mobLabel = plugin.getSpawnerManager().prettifyKey(instance.getMobTypeKey());
        String titlePattern = config.getString("SPAWNER-MENUS.FILTER-MENU.TITLE", "&8{mob} Filter Settings");
        String titleStr = titlePattern.replace("{mob}", mobLabel);
        int menuSize = plugin.getSpawnerManager().normalizeSize(config.getInt("SPAWNER-MENUS.FILTER-MENU.SIZE", 27));

        inventory = Bukkit.createInventory(this, menuSize, ColorUtils.toComponent(titleStr));
        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);

        dropSlotMap.clear();

        List<SpawnerTypeDefinition.DropDefinition> drops = def != null ? def.drops() : List.of();
        int contentSlots = menuSize - 9;

        String enabledTxt = config.getString("SPAWNER-MENUS.FILTER-MENU.ITEM-META.FILTER-ENABLED",
                config.getString("SPAWNER-MENUS.STORAGE-MENU.ITEM-META.FILTER-ENABLED", "&aEnabled &7(Storing)"));
        String disabledTxt = config.getString("SPAWNER-MENUS.FILTER-MENU.ITEM-META.FILTER-DISABLED",
                config.getString("SPAWNER-MENUS.STORAGE-MENU.ITEM-META.FILTER-DISABLED", "&cDisabled &7(Not Storing)"));

        String titleFormat = config.getString("SPAWNER-MENUS.FILTER-MENU.ITEM-META.TITLE", "&b{material}");
        List<String> rawLore = config.getStringList("SPAWNER-MENUS.FILTER-MENU.ITEM-META.LORE");

        for (int i = 0; i < drops.size() && i < contentSlots; i++) {
            SpawnerTypeDefinition.DropDefinition drop = drops.get(i);
            int slot = i;
            dropSlotMap.put(slot, drop);

            Material mat = drop.material();
            boolean isFiltered = instance.isLootDisabled(drop.key()) || instance.isLootDisabled(mat.name());
            String filterStatus = isFiltered ? disabledTxt : enabledTxt;

            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ColorUtils.toComponent(titleFormat.replace("{material}", plugin.getWorthManager().prettifyMaterial(mat))));
                List<String> lore = new ArrayList<>();
                if (rawLore.isEmpty()) {
                    lore.add("&7Filter Status: " + filterStatus);
                    lore.add("&7Drop Chance: &e" + String.format(Locale.US, "%.1f%%", drop.chance() * 100.0));
                    lore.add("");
                    lore.add("&eClick &7to toggle filter");
                } else {
                    for (String line : rawLore) {
                        lore.add(line.replace("{filter_status}", filterStatus)
                                .replace("{material}", plugin.getWorthManager().prettifyMaterial(mat))
                                .replace("{chance}", String.format(Locale.US, "%.1f", drop.chance() * 100.0)));
                    }
                }
                meta.setLore(ColorUtils.toComponentList(lore));
                item.setItemMeta(meta);
            }
            set(slot, item);
        }

        int lastRow = inventory.getSize() - 9;

        // 1. Back Button
        int backSlot = config.getInt("SPAWNER-MENUS.FILTER-MENU.BACK-BUTTON.SLOT", lastRow);
        String backMatName = config.getString("SPAWNER-MENUS.FILTER-MENU.BACK-BUTTON.MATERIAL", "BARRIER");
        Material backMat = Material.matchMaterial(backMatName);
        if (backMat == null) backMat = Material.BARRIER;
        String backTitle = config.getString("SPAWNER-MENUS.FILTER-MENU.BACK-BUTTON.TITLE", "&cBACK");
        List<String> backLore = config.getStringList("SPAWNER-MENUS.FILTER-MENU.BACK-BUTTON.LORE");
        if (backLore.isEmpty()) backLore = List.of("&7Return to storage menu.");
        set(backSlot, ItemUtils.createItem(backMat, backTitle, backLore));

        // 2. Enable All Button
        int enableAllSlot = config.getInt("SPAWNER-MENUS.FILTER-MENU.ENABLE-ALL-BUTTON.SLOT", lastRow + 7);
        String enableAllMatName = config.getString("SPAWNER-MENUS.FILTER-MENU.ENABLE-ALL-BUTTON.MATERIAL", "LIME_DYE");
        Material enableAllMat = Material.matchMaterial(enableAllMatName);
        if (enableAllMat == null) enableAllMat = Material.LIME_DYE;
        String enableAllTitle = config.getString("SPAWNER-MENUS.FILTER-MENU.ENABLE-ALL-BUTTON.TITLE", "&aENABLE ALL");
        List<String> enableAllLore = config.getStringList("SPAWNER-MENUS.FILTER-MENU.ENABLE-ALL-BUTTON.LORE");
        if (enableAllLore.isEmpty()) enableAllLore = List.of("&7Enable storing all drops.");
        set(enableAllSlot, ItemUtils.createItem(enableAllMat, enableAllTitle, enableAllLore));

        // 3. Disable All Button
        int disableAllSlot = config.getInt("SPAWNER-MENUS.FILTER-MENU.DISABLE-ALL-BUTTON.SLOT", lastRow + 8);
        String disableAllMatName = config.getString("SPAWNER-MENUS.FILTER-MENU.DISABLE-ALL-BUTTON.MATERIAL", "RED_DYE");
        Material disableAllMat = Material.matchMaterial(disableAllMatName);
        if (disableAllMat == null) disableAllMat = Material.RED_DYE;
        String disableAllTitle = config.getString("SPAWNER-MENUS.FILTER-MENU.DISABLE-ALL-BUTTON.TITLE", "&cDISABLE ALL");
        List<String> disableAllLore = config.getStringList("SPAWNER-MENUS.FILTER-MENU.DISABLE-ALL-BUTTON.LORE");
        if (disableAllLore.isEmpty()) disableAllLore = List.of("&7Disable storing all drops.");
        set(disableAllSlot, ItemUtils.createItem(disableAllMat, disableAllTitle, disableAllLore));
    }

    @Override
    public void handleClick(int slot, Player player, ClickType clickType) {
        SpawnerInstance instance = plugin.getSpawnerManager().getSpawner(spawnerId);
        if (instance == null) {
            player.closeInventory();
            return;
        }

        FileConfiguration config = plugin.getConfigManager().getMenus();
        int lastRow = inventory.getSize() - 9;
        int backSlot = config.getInt("SPAWNER-MENUS.FILTER-MENU.BACK-BUTTON.SLOT", lastRow);
        int enableAllSlot = config.getInt("SPAWNER-MENUS.FILTER-MENU.ENABLE-ALL-BUTTON.SLOT", lastRow + 7);
        int disableAllSlot = config.getInt("SPAWNER-MENUS.FILTER-MENU.DISABLE-ALL-BUTTON.SLOT", lastRow + 8);

        if (slot == backSlot) {
            new SpawnerStorageMenu(plugin, spawnerId, returnPage).open(player);
            return;
        }

        SpawnerTypeDefinition def = plugin.getSpawnerManager().getTypeDefinition(instance.getMobTypeKey());

        if (slot == enableAllSlot) {
            if (def != null) {
                for (SpawnerTypeDefinition.DropDefinition drop : def.drops()) {
                    instance.setLootDisabled(drop.key(), false);
                    instance.setLootDisabled(drop.material().name(), false);
                }
                instance.setUpdatedAt(System.currentTimeMillis());
                plugin.getSpawnerManager().saveSpawnerAndLoot(instance);
                player.sendMessage(ColorUtils.toComponent("&aEnabled storing for all drops on this spawner."));
            }
            plugin.getSpawnerManager().playFilterToggleSound(player);
            new SpawnerFilterMenu(plugin, spawnerId, returnPage).open(player);
            return;
        }

        if (slot == disableAllSlot) {
            if (def != null) {
                for (SpawnerTypeDefinition.DropDefinition drop : def.drops()) {
                    instance.setLootDisabled(drop.key(), true);
                    instance.setLootDisabled(drop.material().name(), true);
                }
                instance.setUpdatedAt(System.currentTimeMillis());
                plugin.getSpawnerManager().saveSpawnerAndLoot(instance);
                player.sendMessage(ColorUtils.toComponent("&cDisabled storing for all drops on this spawner."));
            }
            plugin.getSpawnerManager().playFilterToggleSound(player);
            new SpawnerFilterMenu(plugin, spawnerId, returnPage).open(player);
            return;
        }

        if (dropSlotMap.containsKey(slot)) {
            SpawnerTypeDefinition.DropDefinition drop = dropSlotMap.get(slot);
            boolean currentlyDisabled = instance.isLootDisabled(drop.key()) || instance.isLootDisabled(drop.material().name());
            boolean newState = !currentlyDisabled;

            instance.setLootDisabled(drop.key(), newState);
            instance.setLootDisabled(drop.material().name(), newState);
            instance.setUpdatedAt(System.currentTimeMillis());
            plugin.getSpawnerManager().saveSpawnerAndLoot(instance);

            String statusMsg = newState ? "&cDisabled &7(Not Storing)" : "&aEnabled &7(Storing)";
            player.sendMessage(ColorUtils.toComponent("&aToggled filter for &f"
                    + plugin.getWorthManager().prettifyMaterial(drop.material())
                    + " &ato " + statusMsg + "&a."));

            plugin.getSpawnerManager().playFilterToggleSound(player);
            new SpawnerFilterMenu(plugin, spawnerId, returnPage).open(player);
        }
    }
}
