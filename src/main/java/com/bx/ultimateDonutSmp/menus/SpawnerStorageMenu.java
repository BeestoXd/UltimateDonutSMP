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
        super(plugin, "&8ѕᴘᴀᴡɴᴇʀ ѕᴛᴏʀᴀɢᴇ", plugin.getSpawnerManager().getStorageSize());
        this.spawnerId = spawnerId;
        this.page = Math.max(1, page);
    }

    @Override
    public void build(Player player) {
        SpawnerInstance instance = plugin.getSpawnerManager().getSpawner(spawnerId);
        if (instance == null) {
            inventory = Bukkit.createInventory(this, plugin.getSpawnerManager().getStorageSize(), ColorUtils.toComponent("&8ѕᴘᴀᴡɴᴇʀ ᴍɪѕѕɪɴɢ"));
            clear();
            fill(Material.GRAY_STAINED_GLASS_PANE);
            set(inventory.getSize() / 2, ItemUtils.createItem(Material.BARRIER, "&cѕᴘᴀᴡɴᴇʀ ɴᴏᴛ ꜰᴏᴜɴᴅ"));
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
                boolean isFiltered = instance.isLootDisabled(entry.getKey());
                meta.setDisplayName(ColorUtils.toComponent("&b" + plugin.getWorthManager().prettifyMaterial(entry.getMaterial())));
                meta.setLore(ColorUtils.toComponentList(List.of(
                        "&7ѕᴛᴏʀᴇᴅ: &f" + NumberUtils.format(entry.getAmount()),
                        "&7ꜰɪʟᴛᴇʀ ѕᴛᴀᴛᴜѕ: " + (isFiltered ? "&cᴅɪѕᴀʙʟᴇᴅ &7(ɴᴏᴛ ѕᴛᴏʀɪɴɢ)" : "&aᴇɴᴀʙʟᴇᴅ &7(ѕᴛᴏʀɪɴɢ)"),
                        "",
                        "&eʟᴇꜰᴛ-ᴄʟɪᴄᴋ &7ᴛᴏ ᴄᴏʟʟᴇᴄᴛ ᴏɴᴇ ѕᴛᴀᴄᴋ",
                        "&eѕʜɪꜰᴛ-ʟᴇꜰᴛ &7ᴛᴏ ᴄᴏʟʟᴇᴄᴛ ᴀʟʟ ᴏꜰ ᴛʜɪѕ ʟᴏᴏᴛ",
                        "&eʀɪɢʜᴛ-ᴄʟɪᴄᴋ &7ᴛᴏ ᴛᴏɢɢʟᴇ ꜰɪʟᴛᴇʀ"
                )));
                display.setItemMeta(meta);
            }
            set(slot, display);
        }

        int lastRow = inventory.getSize() - 9;
        // Slot 45 (lastRow + 0): Barrier (Back to menu)
        set(lastRow, ItemUtils.createItem(Material.BARRIER, "&cBACK", List.of("&7Return to spawner menu.")));

        // Slot 48 (lastRow + 3): Spectral Arrow (Golden arrow - collect loot)
        set(lastRow + 3, ItemUtils.createItem(
                Material.SPECTRAL_ARROW,
                "&eSPAWNER",
                List.of("&e• &fCollect your loot from the storage")
        ));

        // Slot 49 (lastRow + 4): Arrow (Next / Previous page)
        if (safePage < totalPages) {
            set(lastRow + 4, ItemUtils.createItem(Material.ARROW, "&aNEXT", List.of("&fClick to go forward a page")));
        } else if (safePage > 1) {
            set(lastRow + 4, ItemUtils.createItem(Material.ARROW, "&aPREVIOUS", List.of("&fClick to go back a page")));
        } else {
            set(lastRow + 4, ItemUtils.createPlaceholder(Material.BLACK_STAINED_GLASS_PANE));
        }

        // Slot 52 (lastRow + 7): Dropper (Drop loot)
        set(lastRow + 7, ItemUtils.createItem(
                Material.DROPPER,
                "&aDROP LOOT",
                List.of("&fClick to drop all loot on the page")
        ));

        // Slot 53 (lastRow + 8): Gold Ingot (Sell all with confirm sell)
        set(lastRow + 8, ItemUtils.createItem(
                Material.GOLD_INGOT,
                "&aSELL ALL",
                List.of("&fClick to sell all mob drops!")
        ));
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
        if (slot == lastRow) {
            // Barrier - Back to menu
            new SpawnerMainMenu(plugin, spawnerId).open(player);
            return;
        }
        if (slot == lastRow + 3) {
            // Golden arrow - Collect loot
            player.sendMessage(ColorUtils.toComponent(plugin.getSpawnerManager().collectAllLoot(player, instance).message()));
            new SpawnerStorageMenu(plugin, spawnerId, safePage).open(player);
            return;
        }
        if (slot == lastRow + 4) {
            // Arrow - Page navigation
            if (safePage < totalPages) {
                new SpawnerStorageMenu(plugin, spawnerId, safePage + 1).open(player);
            } else if (safePage > 1) {
                new SpawnerStorageMenu(plugin, spawnerId, safePage - 1).open(player);
            }
            return;
        }
        if (slot == lastRow + 7) {
            // Dropper - Drop loot
            player.sendMessage(ColorUtils.toComponent(plugin.getSpawnerManager().dropAllLoot(player, instance).message()));
            new SpawnerStorageMenu(plugin, spawnerId, safePage).open(player);
            return;
        }
        if (slot == lastRow + 8) {
            // Gold Ingot - Sell All (Open Confirm Sell GUI)
            plugin.getSpawnerManager().playSellConfirmOpenSound(player);
            new SpawnerSellConfirmMenu(plugin, spawnerId, safePage).open(player);
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
        if (clickType.isRightClick()) {
            boolean currentState = instance.isLootDisabled(entry.getKey());
            instance.setLootDisabled(entry.getKey(), !currentState);
            instance.setUpdatedAt(System.currentTimeMillis());
            plugin.getSpawnerManager().saveSpawnerAndLoot(instance);

            String statusMsg = !currentState ? "&cᴅɪѕᴀʙʟᴇᴅ &7(ɴᴏᴛ ѕᴛᴏʀɪɴɢ)" : "&aᴇɴᴀʙʟᴇᴅ &7(ѕᴛᴏʀɪɴɢ)";
            player.sendMessage(ColorUtils.toComponent("&aToggled filter for &f"
                    + plugin.getWorthManager().prettifyMaterial(entry.getMaterial())
                    + " &ato " + statusMsg + "&a."));

            new SpawnerStorageMenu(plugin, spawnerId, safePage).open(player);
            return;
        }

        boolean collectAll = clickType.isShiftClick();
        player.sendMessage(ColorUtils.toComponent(
                plugin.getSpawnerManager().collectLootEntry(player, instance, entry.getKey(), collectAll).message()
        ));
        new SpawnerStorageMenu(plugin, spawnerId, safePage).open(player);
    }
}
