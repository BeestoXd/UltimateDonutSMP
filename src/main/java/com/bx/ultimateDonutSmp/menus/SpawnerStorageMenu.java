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
                meta.setDisplayName(ColorUtils.toComponent("&b" + plugin.getWorthManager().prettifyMaterial(entry.getMaterial())));
                meta.setLore(ColorUtils.toComponentList(List.of(
                        "&7ѕᴛᴏʀᴇᴅ: &f" + NumberUtils.format(entry.getAmount()),
                        "",
                        "&eʟᴇꜰᴛ-ᴄʟɪᴄᴋ &7ᴛᴏ ᴄᴏʟʟᴇᴄᴛ ᴏɴᴇ ѕᴛᴀᴄᴋ",
                        "&eѕʜɪꜰᴛ-ʟᴇꜰᴛ &7ᴛᴏ ᴄᴏʟʟᴇᴄᴛ ᴀʟʟ ᴏꜰ ᴛʜɪѕ ʟᴏᴏᴛ"
                )));
                display.setItemMeta(meta);
            }
            set(slot, display);
        }

        int lastRow = inventory.getSize() - 9;
        set(lastRow, safePage > 1
                ? ItemUtils.createItem(Material.ARROW, "&aᴘʀᴇᴠɪᴏᴜѕ ᴘᴀɢᴇ", List.of("&7ɢᴏ ᴛᴏ ᴘᴀɢᴇ &f" + (safePage - 1)))
                : ItemUtils.createPlaceholder(Material.BLACK_STAINED_GLASS_PANE));
        set(lastRow + 2, ItemUtils.createItem(Material.HOPPER, "&aᴄᴏʟʟᴇᴄᴛ ᴀʟʟ", List.of("&7ᴍᴏᴠᴇ ᴀʟʟ ѕᴛᴏʀᴇᴅ ʟᴏᴏᴛ ᴛᴏ ʏᴏᴜʀ ɪɴᴠᴇɴᴛᴏʀʏ.")));
        set(lastRow + 3, ItemUtils.createItem(Material.DISPENSER, "&6ᴅʀᴏᴘ ʟᴏᴏᴛ", List.of("&7ᴅʀᴏᴘ ᴀʟʟ ѕᴛᴏʀᴇᴅ ʟᴏᴏᴛ ᴏɴ ᴛʜᴇ ɢʀᴏᴜɴᴅ.")));
        set(lastRow + 4, ItemUtils.createItem(
                Material.GOLD_INGOT,
                "&eѕᴇʟʟ ᴀʟʟ",
                List.of("&7ѕᴇʟʟ ᴀʟʟ ѕᴇʟʟᴀʙʟᴇ ʟᴏᴏᴛ ꜰᴏʀ "
                        + plugin.getCurrencyManager().plural(com.bx.ultimateDonutSmp.managers.CurrencyManager.CurrencyType.MONEY)
                        + ".")
        ));
        set(lastRow + 5, ItemUtils.createItem(Material.SPAWNER, "&bѕᴘᴀᴡɴᴇʀ ɪɴꜰᴏ", List.of(
                "&7ᴛʏᴘᴇ: &f" + plugin.getSpawnerManager().getPlainTypeDisplayName(instance.getMobTypeKey()),
                "&7ѕᴛᴀᴄᴋ: &f" + NumberUtils.format(instance.getStackAmount()),
                "&7ѕᴛᴏʀᴇᴅ ɪᴛᴇᴍѕ: &f" + NumberUtils.format(instance.getTotalStoredItems())
        )));
        set(lastRow + 7, safePage < totalPages
                ? ItemUtils.createItem(Material.ARROW, "&aɴᴇxᴛ ᴘᴀɢᴇ", List.of("&7ɢᴏ ᴛᴏ ᴘᴀɢᴇ &f" + (safePage + 1)))
                : ItemUtils.createPlaceholder(Material.BLACK_STAINED_GLASS_PANE));
        set(lastRow + 8, ItemUtils.createItem(Material.BARRIER, "&cᴄʟᴏѕᴇ", List.of("&7ᴄʟᴏѕᴇ ᴛʜɪѕ ᴍᴇɴᴜ.")));
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
