package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.SpawnerInstance;
import com.bx.ultimateDonutSmp.models.SpawnerLootEntry;
import com.bx.ultimateDonutSmp.models.SpawnerTypeDefinition;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SpawnerMainMenu extends BaseMenu {

    private final long spawnerId;

    public SpawnerMainMenu(UltimateDonutSmp plugin, long spawnerId) {
        super(plugin, "&8Spawner", plugin.getSpawnerManager().getMainMenuSize());
        this.spawnerId = spawnerId;
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

        FileConfiguration config = plugin.getConfigManager().getMenus();
        String titleStr = plugin.getSpawnerManager().getMainMenuTitle(instance);
        inventory = Bukkit.createInventory(this, plugin.getSpawnerManager().getMainMenuSize(), ColorUtils.toComponent(titleStr));

        clear();
        String fillerMatName = config.getString("SPAWNER-MENUS.MAIN-MENU.FILLER-MATERIAL", "GRAY_STAINED_GLASS_PANE");
        Material fillerMat = Material.matchMaterial(fillerMatName);
        if (fillerMat == null) fillerMat = Material.GRAY_STAINED_GLASS_PANE;
        fill(fillerMat);

        // 1. Spawner Storage Button
        int storageSlot = config.getInt("SPAWNER-MENUS.MAIN-MENU.STORAGE-BUTTON.SLOT", 11);
        String storageMatName = config.getString("SPAWNER-MENUS.MAIN-MENU.STORAGE-BUTTON.MATERIAL", "CHEST");
        Material storageMat = Material.matchMaterial(storageMatName);
        if (storageMat == null) storageMat = Material.CHEST;
        String storageTitle = config.getString("SPAWNER-MENUS.MAIN-MENU.STORAGE-BUTTON.TITLE", "&6Spawner Storage");

        List<SpawnerLootEntry> lootEntries = instance.getStoredLootEntries();
        Map<Material, Long> aggregated = new LinkedHashMap<>();
        for (SpawnerLootEntry entry : lootEntries) {
            if (entry != null && entry.getAmount() > 0) {
                aggregated.merge(entry.getMaterial(), entry.getAmount(), Long::sum);
            }
        }

        List<String> storageLore = new ArrayList<>();
        if (aggregated.isEmpty()) {
            List<String> noItemsLore = config.getStringList("SPAWNER-MENUS.MAIN-MENU.STORAGE-BUTTON.NO-ITEMS-LORE");
            if (noItemsLore.isEmpty()) {
                storageLore.add("&7No items stored.");
            } else {
                storageLore.addAll(noItemsLore);
            }
        } else {
            String itemFormat = config.getString("SPAWNER-MENUS.MAIN-MENU.STORAGE-BUTTON.ITEM-SUMMARY-FORMAT", "&6{amount} &f{material}");
            String moreFormat = config.getString("SPAWNER-MENUS.MAIN-MENU.STORAGE-BUTTON.MORE-ITEMS-FORMAT", "&7+ {count} more item types...");

            int count = 0;
            for (Map.Entry<Material, Long> entry : aggregated.entrySet()) {
                if (count < 6) {
                    storageLore.add(itemFormat
                            .replace("{amount}", NumberUtils.format(entry.getValue()))
                            .replace("{material}", plugin.getWorthManager().prettifyMaterial(entry.getKey())));
                    count++;
                } else {
                    int remainingTypes = aggregated.size() - count;
                    storageLore.add(moreFormat.replace("{count}", String.valueOf(remainingTypes)));
                    break;
                }
            }
        }

        List<String> footerLore = config.getStringList("SPAWNER-MENUS.MAIN-MENU.STORAGE-BUTTON.FOOTER-LORE");
        if (!footerLore.isEmpty()) {
            storageLore.addAll(footerLore);
        } else {
            storageLore.add("");
            storageLore.add("&eClick to view & collect spawner storage");
        }

        set(storageSlot, ItemUtils.createItem(storageMat, storageTitle, storageLore));

        // 2. Mob Head Button (Spawner Count & Quick Sell)
        int headSlot = config.getInt("SPAWNER-MENUS.MAIN-MENU.MOB-HEAD-BUTTON.SLOT", 13);
        long totalCapacity = plugin.getSpawnerManager().getStorageCapPerLootKey();
        long currentTotal = instance.getTotalStoredItems();
        double fillPercentage = totalCapacity > 0 ? Math.min(100.0, (currentTotal / (double) totalCapacity) * 100.0) : 0.0;
        String mobLabel = instance.getMobTypeKey().replace('_', ' ').toUpperCase();

        SpawnerTypeDefinition def = plugin.getSpawnerManager().getTypeDefinition(instance.getMobTypeKey());
        String customTexture = def != null ? def.headTexture() : null;

        String headTitle = config.getString("SPAWNER-MENUS.MAIN-MENU.MOB-HEAD-BUTTON.TITLE", "&e{stack} {mob} SPAWNERS")
                .replace("{stack}", NumberUtils.format(instance.getStackAmount()))
                .replace("{mob}", mobLabel);

        List<String> rawHeadLore = config.getStringList("SPAWNER-MENUS.MAIN-MENU.MOB-HEAD-BUTTON.LORE");
        List<String> headLore = new ArrayList<>();
        if (rawHeadLore.isEmpty()) {
            headLore.add("&e• &fClick to sell items and collect xp");
            headLore.add("&7Storage: &e" + String.format(java.util.Locale.US, "%.1f", fillPercentage) + "% Filled.");
        } else {
            for (String line : rawHeadLore) {
                headLore.add(line
                        .replace("{percentage}", String.format(java.util.Locale.US, "%.1f", fillPercentage))
                        .replace("{stack}", NumberUtils.format(instance.getStackAmount()))
                        .replace("{mob}", mobLabel));
            }
        }

        ItemStack headItem = ItemUtils.createMobHead(
                instance.getMobTypeKey(),
                customTexture,
                headTitle,
                headLore
        );
        set(headSlot, headItem);

        // 3. Collect XP Button
        int xpSlot = config.getInt("SPAWNER-MENUS.MAIN-MENU.COLLECT-XP-BUTTON.SLOT", 15);
        String xpMatName = config.getString("SPAWNER-MENUS.MAIN-MENU.COLLECT-XP-BUTTON.MATERIAL", "EXPERIENCE_BOTTLE");
        Material xpMat = Material.matchMaterial(xpMatName);
        if (xpMat == null) xpMat = Material.EXPERIENCE_BOTTLE;

        String xpTitle = config.getString("SPAWNER-MENUS.MAIN-MENU.COLLECT-XP-BUTTON.TITLE", "&aCollect XP");
        double storedXp = instance.getStoredXp();
        List<String> rawXpLore = config.getStringList("SPAWNER-MENUS.MAIN-MENU.COLLECT-XP-BUTTON.LORE");
        List<String> xpLore = new ArrayList<>();
        if (rawXpLore.isEmpty()) {
            xpLore.add("&a" + String.format(java.util.Locale.US, "%.1f", storedXp) + " &fXP Points");
            xpLore.add("");
            xpLore.add("&eClick to claim XP points");
        } else {
            for (String line : rawXpLore) {
                xpLore.add(line.replace("{xp}", String.format(java.util.Locale.US, "%.1f", storedXp)));
            }
        }

        set(xpSlot, ItemUtils.createItem(xpMat, xpTitle, xpLore));
    }

    @Override
    public void handleClick(int slot, Player player, ClickType clickType) {
        SpawnerInstance instance = plugin.getSpawnerManager().getSpawner(spawnerId);
        if (instance == null) {
            player.closeInventory();
            return;
        }

        FileConfiguration config = plugin.getConfigManager().getMenus();
        int storageSlot = config.getInt("SPAWNER-MENUS.MAIN-MENU.STORAGE-BUTTON.SLOT", 11);
        int headSlot = config.getInt("SPAWNER-MENUS.MAIN-MENU.MOB-HEAD-BUTTON.SLOT", 13);
        int xpSlot = config.getInt("SPAWNER-MENUS.MAIN-MENU.COLLECT-XP-BUTTON.SLOT", 15);

        if (slot == storageSlot) {
            // Open Spawner Storage Menu
            new SpawnerStorageMenu(plugin, spawnerId, 1).open(player);
            return;
        }

        if (slot == headSlot) {
            // Sell items & Collect XP in one click
            var result = plugin.getSpawnerManager().sellAndCollectXp(player, instance);
            player.sendMessage(ColorUtils.toComponent(result.message()));
            new SpawnerMainMenu(plugin, spawnerId).open(player);
            return;
        }

        if (slot == xpSlot) {
            // Collect XP
            var result = plugin.getSpawnerManager().collectXp(player, instance);
            player.sendMessage(ColorUtils.toComponent(result.message()));
            new SpawnerMainMenu(plugin, spawnerId).open(player);
            return;
        }
    }
}
