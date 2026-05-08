package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.SpawnerInstance;
import com.bx.ultimateDonutSmp.models.SpawnerLootEntry;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class SpawnerStorageMenu extends BaseMenu {

    private final long spawnerId;
    private final int page;

    public SpawnerStorageMenu(UltimateDonutSmp plugin, long spawnerId, int page) {
        super(plugin, "&8Spawner Storage", plugin.getSpawnerManager().getStorageSize());
        this.spawnerId = spawnerId;
        this.page = Math.max(1, page);
    }

    @Override
    public void build(Player player) {
        SpawnerInstance instance = plugin.getSpawnerManager().getSpawner(spawnerId);
        if (instance == null) {
            inventory = Bukkit.createInventory(this, plugin.getSpawnerManager().getStorageSize(), ColorUtils.toComponent("&8Spawner Missing"));
            clear();
            fill(Material.GRAY_STAINED_GLASS_PANE);
            set(inventory.getSize() / 2, ItemUtils.createItem(Material.BARRIER, "&cSpawner not found"));
            return;
        }

        List<SpawnerLootEntry> entries = plugin.getSpawnerManager().getSortedLootEntries(instance);
        int itemsPerPage = plugin.getSpawnerManager().getStorageItemsPerPage();
        int totalPages = Math.max(1, (int) Math.ceil(entries.size() / (double) itemsPerPage));
        int safePage = Math.min(page, totalPages);
        inventory = Bukkit.createInventory(
                this,
                plugin.getSpawnerManager().getStorageSize(),
                ColorUtils.toComponent(plugin.getSpawnerManager().getStorageTitle(instance, safePage, totalPages))
        );

        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);

        int startIndex = (safePage - 1) * itemsPerPage;
        int lastIndexExclusive = Math.min(entries.size(), startIndex + itemsPerPage);
        int contentSlots = Math.min(itemsPerPage, inventory.getSize() - 9);

        for (int slot = 0; slot < contentSlots; slot++) {
            int entryIndex = startIndex + slot;
            if (entryIndex >= lastIndexExclusive) {
                break;
            }

            SpawnerLootEntry entry = entries.get(entryIndex);
            ItemStack display = new ItemStack(entry.getMaterial(), (int) Math.max(1, Math.min(entry.getAmount(), entry.getMaterial().getMaxStackSize())));
            var meta = display.getItemMeta();
            if (meta != null) {
                meta.displayName(ColorUtils.toComponent("&b" + plugin.getWorthManager().prettifyMaterial(entry.getMaterial())));
                meta.lore(ColorUtils.toComponentList(List.of(
                        "&7Stored: &f" + NumberUtils.format(entry.getAmount()),
                        "",
                        "&eLeft-click &7to collect one stack",
                        "&eShift-left &7to collect all of this loot"
                )));
                display.setItemMeta(meta);
            }
            set(slot, display);
        }

        int lastRow = inventory.getSize() - 9;
        set(lastRow, safePage > 1
                ? ItemUtils.createItem(Material.ARROW, "&aPrevious Page", List.of("&7Go to page &f" + (safePage - 1)))
                : ItemUtils.createPlaceholder(Material.BLACK_STAINED_GLASS_PANE));
        set(lastRow + 2, ItemUtils.createItem(Material.HOPPER, "&aCollect All", List.of("&7Move all stored loot to your inventory.")));
        set(lastRow + 3, ItemUtils.createItem(Material.DISPENSER, "&6Drop Loot", List.of("&7Drop all stored loot on the ground.")));
        set(lastRow + 4, ItemUtils.createItem(Material.GOLD_INGOT, "&eSell All", List.of("&7Sell all sellable loot for money.")));
        set(lastRow + 5, ItemUtils.createItem(Material.SPAWNER, "&bSpawner Info", List.of(
                "&7Type: &f" + plugin.getSpawnerManager().getPlainTypeDisplayName(instance.getMobTypeKey()),
                "&7Stack: &f" + NumberUtils.format(instance.getStackAmount()),
                "&7Stored items: &f" + NumberUtils.format(instance.getTotalStoredItems())
        )));
        set(lastRow + 7, safePage < totalPages
                ? ItemUtils.createItem(Material.ARROW, "&aNext Page", List.of("&7Go to page &f" + (safePage + 1)))
                : ItemUtils.createPlaceholder(Material.BLACK_STAINED_GLASS_PANE));
        set(lastRow + 8, ItemUtils.createItem(Material.BARRIER, "&cClose", List.of("&7Close this menu.")));
    }

    @Override
    public void handleClick(int slot, Player player, ClickType clickType) {
        SpawnerInstance instance = plugin.getSpawnerManager().getSpawner(spawnerId);
        if (instance == null) {
            player.closeInventory();
            return;
        }

        int itemsPerPage = plugin.getSpawnerManager().getStorageItemsPerPage();
        List<SpawnerLootEntry> entries = plugin.getSpawnerManager().getSortedLootEntries(instance);
        int totalPages = Math.max(1, (int) Math.ceil(entries.size() / (double) itemsPerPage));
        int safePage = Math.min(page, totalPages);

        int lastRow = inventory.getSize() - 9;
        if (slot == lastRow && safePage > 1) {
            new SpawnerStorageMenu(plugin, spawnerId, safePage - 1).open(player);
            return;
        }
        if (slot == lastRow + 2) {
            player.sendMessage(ColorUtils.toComponent(plugin.getSpawnerManager().collectAllLoot(player, instance).message()));
            new SpawnerStorageMenu(plugin, spawnerId, safePage).open(player);
            return;
        }
        if (slot == lastRow + 3) {
            player.sendMessage(ColorUtils.toComponent(plugin.getSpawnerManager().dropAllLoot(player, instance).message()));
            new SpawnerStorageMenu(plugin, spawnerId, safePage).open(player);
            return;
        }
        if (slot == lastRow + 4) {
            player.sendMessage(ColorUtils.toComponent(plugin.getSpawnerManager().sellAllLoot(player, instance).message()));
            new SpawnerStorageMenu(plugin, spawnerId, safePage).open(player);
            return;
        }
        if (slot == lastRow + 7 && safePage < totalPages) {
            new SpawnerStorageMenu(plugin, spawnerId, safePage + 1).open(player);
            return;
        }
        if (slot == lastRow + 8) {
            player.closeInventory();
            return;
        }

        if (slot < 0 || slot >= Math.min(itemsPerPage, inventory.getSize() - 9)) {
            return;
        }

        int entryIndex = (safePage - 1) * itemsPerPage + slot;
        if (entryIndex < 0 || entryIndex >= entries.size()) {
            return;
        }

        SpawnerLootEntry entry = entries.get(entryIndex);
        boolean collectAll = clickType.isShiftClick();
        player.sendMessage(ColorUtils.toComponent(
                plugin.getSpawnerManager().collectLootEntry(player, instance, entry.getKey(), collectAll).message()
        ));
        new SpawnerStorageMenu(plugin, spawnerId, safePage).open(player);
    }
}
