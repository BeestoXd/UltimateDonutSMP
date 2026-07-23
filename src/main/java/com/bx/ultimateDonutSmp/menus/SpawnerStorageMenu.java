package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.SpawnerInstance;
import com.bx.ultimateDonutSmp.models.SpawnerLootEntry;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;

public class SpawnerStorageMenu extends BaseMenu {

    private final long spawnerId;
    private final int page;
    private long lastInteractionTime = 0L;

    public SpawnerStorageMenu(UltimateDonutSmp plugin, long spawnerId, int page) {
        super(plugin, "&8ѕᴘᴀᴡɴᴇʀ ѕᴛᴏʀᴀɢᴇ", plugin.getSpawnerManager().getStorageSize());
        this.spawnerId = spawnerId;
        this.page = Math.max(1, page);
    }

    public long getSpawnerId() {
        return spawnerId;
    }

    public int getPage() {
        return page;
    }

    public void refresh(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        // Skip background auto-refresh if player clicked/interacted in the last 2 seconds
        if (System.currentTimeMillis() - lastInteractionTime < 2000L) {
            return;
        }

        SpawnerInstance instance = plugin.getSpawnerManager().getSpawner(spawnerId);
        if (instance == null) {
            return;
        }

        Inventory topInventory = player.getOpenInventory().getTopInventory();
        if (topInventory == null || !(topInventory.getHolder() instanceof SpawnerStorageMenu storageMenu)) {
            return;
        }

        if (storageMenu.spawnerId != spawnerId) {
            return;
        }

        int itemsPerPage = plugin.getSpawnerManager().getStorageItemsPerPage();
        int contentSlots = Math.min(itemsPerPage, topInventory.getSize() - 9);
        int pageOffset = (page - 1) * itemsPerPage;

        for (int slot = 0; slot < contentSlots; slot++) {
            int slotIndex = pageOffset + slot;
            SpawnerLootEntry entry = instance.getSlotLoot(slotIndex);

            if (entry != null && entry.getAmount() > 0L) {
                ItemStack current = topInventory.getItem(slot);
                if (current == null || current.getType() != entry.getMaterial() || current.getAmount() != (int) entry.getAmount()) {
                    topInventory.setItem(slot, applyStorageMeta(plugin, instance, entry.getMaterial(), (int) entry.getAmount()));
                }
            } else {
                ItemStack current = topInventory.getItem(slot);
                if (current != null && !current.getType().isAir()) {
                    topInventory.setItem(slot, null);
                }
            }
        }
        player.updateInventory();
    }

    public static ItemStack applyStorageMeta(UltimateDonutSmp plugin, SpawnerInstance instance, Material material, int amount) {
        ItemStack item = new ItemStack(material, Math.max(1, Math.min(amount, material.getMaxStackSize())));
        var meta = item.getItemMeta();
        if (meta != null) {
            boolean isFiltered = instance.isLootDisabled(material.name());
            meta.setDisplayName(ColorUtils.toComponent("&b" + plugin.getWorthManager().prettifyMaterial(material)));
            meta.setLore(ColorUtils.toComponentList(List.of(
                    "&7ꜰɪʟᴛᴇʀ ѕᴛᴀᴛᴜѕ: " + (isFiltered ? "&cᴅɪѕᴀʙʟᴇᴅ &7(ɴᴏᴛ ѕᴛᴏʀɪɴɢ)" : "&aᴇɴᴀʙʟᴇᴅ &7(ѕᴛᴏʀɪɴɢ)"),
                    "",
                    "&eʟᴇꜰᴛ-ᴄʟɪᴄᴋ &7ᴛᴏ ᴘɪᴄᴋ ᴜᴘ",
                    "&eʀɪɢʜᴛ-ᴄʟɪᴄᴋ &7ᴛᴏ ᴘɪᴄᴋ ᴜᴘ ʜᴀʟꜰ / ᴘʟᴀᴄᴇ 1",
                    "&eѕʜɪꜰᴛ-ʟᴇꜰᴛ &7ᴛᴏ ᴄᴏʟʟᴇᴄᴛ ᴛᴏ ɪɴᴠᴇɴᴛᴏʀʏ",
                    "&eѕʜɪꜰᴛ-ʀɪɢʜᴛ &7ᴛᴏ ᴛᴏɢɢʟᴇ ꜰɪʟᴛᴇʀ"
            )));
            item.setItemMeta(meta);
        }
        return item;
    }

    public static ItemStack stripStorageMeta(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return item;
        }
        ItemStack copy = item.clone();
        var meta = copy.getItemMeta();
        if (meta != null) {
            meta.setLore(null);
            copy.setItemMeta(meta);
        }
        return copy;
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

        int itemsPerPage = plugin.getSpawnerManager().getStorageItemsPerPage();
        int maxSlotIndex = 0;
        for (SpawnerLootEntry entry : instance.getStoredLootEntries()) {
            if (entry.getKey().startsWith("SLOT_")) {
                try {
                    int idx = Integer.parseInt(entry.getKey().substring(5));
                    maxSlotIndex = Math.max(maxSlotIndex, idx);
                } catch (NumberFormatException ignored) {}
            }
        }
        int totalPages = Math.max(1, (int) Math.ceil((maxSlotIndex + 1) / (double) itemsPerPage));
        int safePage = Math.min(page, totalPages);

        inventory = Bukkit.createInventory(
                this,
                plugin.getSpawnerManager().getStorageSize(),
                ColorUtils.toComponent(plugin.getSpawnerManager().getStorageTitle(instance, safePage, totalPages))
        );

        clear();
        int lastRow = inventory.getSize() - 9;
        for (int r = lastRow; r < inventory.getSize(); r++) {
            set(r, ItemUtils.createPlaceholder(Material.GRAY_STAINED_GLASS_PANE));
        }

        int contentSlots = Math.min(itemsPerPage, inventory.getSize() - 9);
        int pageOffset = (safePage - 1) * itemsPerPage;

        for (int slot = 0; slot < contentSlots; slot++) {
            int slotIndex = pageOffset + slot;
            SpawnerLootEntry entry = instance.getSlotLoot(slotIndex);
            if (entry != null && entry.getAmount() > 0L) {
                set(slot, applyStorageMeta(plugin, instance, entry.getMaterial(), (int) entry.getAmount()));
            }
        }

        // Slot 45 (lastRow + 0): Barrier (Back to menu)
        set(lastRow, ItemUtils.createItem(Material.BARRIER, "&cBACK", List.of("&7Return to spawner menu.")));

        // Slot 48 (lastRow + 3): Spectral Arrow (Golden arrow - collect loot)
        set(lastRow + 3, ItemUtils.createItem(
                Material.SPECTRAL_ARROW,
                "&eCOLLECT ALL",
                List.of("&e• &fCollect all loot from storage into inventory")
        ));

        // Slot 49 (lastRow + 4): Previous Page Arrow
        if (safePage > 1) {
            set(lastRow + 4, ItemUtils.createItem(Material.ARROW, "&aPREVIOUS PAGE", List.of("&fClick to go back to page " + (safePage - 1))));
        } else {
            set(lastRow + 4, ItemUtils.createPlaceholder(Material.GRAY_STAINED_GLASS_PANE));
        }

        // Slot 50 (lastRow + 5): Background Glass Pane
        set(lastRow + 5, ItemUtils.createPlaceholder(Material.GRAY_STAINED_GLASS_PANE));

        // Slot 51 (lastRow + 6): Next Page Arrow
        if (safePage < totalPages) {
            set(lastRow + 6, ItemUtils.createItem(Material.ARROW, "&aNEXT PAGE", List.of("&fClick to go to page " + (safePage + 1))));
        } else {
            set(lastRow + 6, ItemUtils.createPlaceholder(Material.GRAY_STAINED_GLASS_PANE));
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

    public void handleInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        this.lastInteractionTime = System.currentTimeMillis();

        SpawnerInstance instance = plugin.getSpawnerManager().getSpawner(spawnerId);
        if (instance == null) {
            event.setCancelled(true);
            player.closeInventory();
            return;
        }

        Inventory topInventory = event.getView().getTopInventory();
        Inventory clickedInventory = event.getClickedInventory();
        int rawSlot = event.getRawSlot();
        ClickType clickType = event.getClick();
        ItemStack cursorItem = event.getCursor();

        int itemsPerPage = plugin.getSpawnerManager().getStorageItemsPerPage();
        int lastRow = topInventory.getSize() - 9;
        int pageOffset = (page - 1) * itemsPerPage;

        // 1. Click in player's bottom inventory
        if (clickedInventory != null && !clickedInventory.equals(topInventory)) {
            if (clickType.isShiftClick()) {
                ItemStack current = event.getCurrentItem();
                if (current != null && !current.getType().isAir()) {
                    event.setCancelled(true);
                    Material mat = current.getType();
                    int remainingToAdd = current.getAmount();
                    int maxStack = mat.getMaxStackSize();
                    if (maxStack <= 0) maxStack = 64;

                    // Step A: Top up existing slots of same material in top inventory (0..44)
                    for (int s = 0; s < lastRow && remainingToAdd > 0; s++) {
                        ItemStack inSlot = topInventory.getItem(s);
                        if (inSlot != null && inSlot.getType() == mat && inSlot.getAmount() < maxStack) {
                            int space = maxStack - inSlot.getAmount();
                            int add = Math.min(space, remainingToAdd);
                            int newAmount = inSlot.getAmount() + add;
                            int slotIndex = pageOffset + s;

                            topInventory.setItem(s, applyStorageMeta(plugin, instance, mat, newAmount));
                            instance.setSlotLoot(slotIndex, mat, newAmount);
                            remainingToAdd -= add;
                        }
                    }

                    // Step B: Fill first available empty slots if remainder > 0
                    for (int s = 0; s < lastRow && remainingToAdd > 0; s++) {
                        ItemStack inSlot = topInventory.getItem(s);
                        if (inSlot == null || inSlot.getType().isAir()) {
                            int add = Math.min(maxStack, remainingToAdd);
                            int slotIndex = pageOffset + s;

                            topInventory.setItem(s, applyStorageMeta(plugin, instance, mat, add));
                            instance.setSlotLoot(slotIndex, mat, add);
                            remainingToAdd -= add;
                        }
                    }

                    instance.setUpdatedAt(System.currentTimeMillis());
                    plugin.getSpawnerManager().saveLoot(instance);

                    if (remainingToAdd <= 0) {
                        event.setCurrentItem(null);
                    } else {
                        current.setAmount(remainingToAdd);
                    }
                    player.updateInventory();
                }
            }
            // For normal (non-shift) clicks inside player inventory, let Bukkit handle it natively!
            return;
        }

        // 2. Click in top inventory control bar (Slots 45 to 53)
        if (rawSlot >= lastRow) {
            event.setCancelled(true);
            if (rawSlot == lastRow) {
                new SpawnerMainMenu(plugin, spawnerId).open(player);
            } else if (rawSlot == lastRow + 3) {
                player.sendMessage(ColorUtils.toComponent(plugin.getSpawnerManager().collectAllLoot(player, instance).message()));
                new SpawnerStorageMenu(plugin, spawnerId, page).open(player);
            } else if (rawSlot == lastRow + 4 && page > 1) {
                new SpawnerStorageMenu(plugin, spawnerId, page - 1).open(player);
            } else if (rawSlot == lastRow + 6) {
                new SpawnerStorageMenu(plugin, spawnerId, page + 1).open(player);
            } else if (rawSlot == lastRow + 7) {
                player.sendMessage(ColorUtils.toComponent(plugin.getSpawnerManager().dropAllLoot(player, instance).message()));
                new SpawnerStorageMenu(plugin, spawnerId, page).open(player);
            } else if (rawSlot == lastRow + 8) {
                plugin.getSpawnerManager().playSellConfirmOpenSound(player);
                new SpawnerSellConfirmMenu(plugin, spawnerId, page).open(player);
            }
            player.updateInventory();
            return;
        }

        // 3. Click in storage content slots (Slots 0 to 44)
        if (rawSlot >= 0 && rawSlot < lastRow) {
            event.setCancelled(true);
            int slotIndex = pageOffset + rawSlot;
            ItemStack slotItem = topInventory.getItem(rawSlot);

            // Shift + Right Click (or Middle Click) -> Toggle Filter Status
            if (clickType == ClickType.SHIFT_RIGHT || clickType == ClickType.MIDDLE) {
                if (slotItem != null && !slotItem.getType().isAir()) {
                    boolean currentState = instance.isLootDisabled(slotItem.getType().name());
                    instance.setLootDisabled(slotItem.getType().name(), !currentState);
                    instance.setUpdatedAt(System.currentTimeMillis());
                    plugin.getSpawnerManager().saveSpawnerAndLoot(instance);

                    String statusMsg = !currentState ? "&cᴅɪѕᴀʙʟᴇᴅ &7(ɴᴏᴛ ѕᴛᴏʀɪɴɢ)" : "&aᴇɴᴀʙʟᴇᴅ &7(ѕᴛᴏʀɪɴɢ)";
                    player.sendMessage(ColorUtils.toComponent("&aToggled filter for &f"
                            + plugin.getWorthManager().prettifyMaterial(slotItem.getType())
                            + " &ato " + statusMsg + "&a."));

                    topInventory.setItem(rawSlot, applyStorageMeta(plugin, instance, slotItem.getType(), slotItem.getAmount()));
                }
                player.updateInventory();
                return;
            }

            // Case A: Holding Item on Cursor
            if (cursorItem != null && !cursorItem.getType().isAir()) {
                if (clickType.isRightClick()) {
                    // Right Click -> Place 1 item from cursor into slot (1-by-1 split or merge 1)
                    if (slotItem == null || slotItem.getType().isAir()) {
                        topInventory.setItem(rawSlot, applyStorageMeta(plugin, instance, cursorItem.getType(), 1));
                        instance.setSlotLoot(slotIndex, cursorItem.getType(), 1);
                        instance.setUpdatedAt(System.currentTimeMillis());
                        plugin.getSpawnerManager().saveLoot(instance);

                        cursorItem.setAmount(cursorItem.getAmount() - 1);
                        event.setCursor(cursorItem.getAmount() <= 0 ? null : cursorItem);
                    } else if (slotItem.getType() == cursorItem.getType()) {
                        int maxStack = slotItem.getType().getMaxStackSize();
                        if (slotItem.getAmount() < maxStack) {
                            int newAmount = slotItem.getAmount() + 1;
                            topInventory.setItem(rawSlot, applyStorageMeta(plugin, instance, slotItem.getType(), newAmount));
                            instance.setSlotLoot(slotIndex, slotItem.getType(), newAmount);
                            instance.setUpdatedAt(System.currentTimeMillis());
                            plugin.getSpawnerManager().saveLoot(instance);

                            cursorItem.setAmount(cursorItem.getAmount() - 1);
                            event.setCursor(cursorItem.getAmount() <= 0 ? null : cursorItem);
                        }
                    }
                    player.updateInventory();
                    return;
                }

                // Left Click -> Place entire cursor stack into slot (or merge cursor stack into slot)
                if (slotItem == null || slotItem.getType().isAir()) {
                    topInventory.setItem(rawSlot, applyStorageMeta(plugin, instance, cursorItem.getType(), cursorItem.getAmount()));
                    instance.setSlotLoot(slotIndex, cursorItem.getType(), cursorItem.getAmount());
                    instance.setUpdatedAt(System.currentTimeMillis());
                    plugin.getSpawnerManager().saveLoot(instance);
                    event.setCursor(null);
                } else if (slotItem.getType() == cursorItem.getType()) {
                    int maxStack = slotItem.getType().getMaxStackSize();
                    int space = maxStack - slotItem.getAmount();
                    if (space > 0) {
                        int add = Math.min(space, cursorItem.getAmount());
                        int newAmount = slotItem.getAmount() + add;
                        topInventory.setItem(rawSlot, applyStorageMeta(plugin, instance, slotItem.getType(), newAmount));
                        instance.setSlotLoot(slotIndex, slotItem.getType(), newAmount);
                        instance.setUpdatedAt(System.currentTimeMillis());
                        plugin.getSpawnerManager().saveLoot(instance);

                        cursorItem.setAmount(cursorItem.getAmount() - add);
                        event.setCursor(cursorItem.getAmount() <= 0 ? null : cursorItem);
                    }
                }
                player.updateInventory();
                return;
            }

            // Case B: Cursor is Empty
            if (slotItem != null && !slotItem.getType().isAir()) {
                if (clickType.isShiftClick()) {
                    // Shift + Left Click -> Collect stack to player inventory
                    ItemStack cleanStack = stripStorageMeta(slotItem);
                    HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(cleanStack);
                    if (leftover.isEmpty()) {
                        topInventory.setItem(rawSlot, null);
                        instance.removeSlotLoot(slotIndex);
                    } else {
                        ItemStack unadded = leftover.values().iterator().next();
                        topInventory.setItem(rawSlot, applyStorageMeta(plugin, instance, slotItem.getType(), unadded.getAmount()));
                        instance.setSlotLoot(slotIndex, slotItem.getType(), unadded.getAmount());
                    }
                    instance.setUpdatedAt(System.currentTimeMillis());
                    plugin.getSpawnerManager().saveLoot(instance);
                    player.updateInventory();
                    return;
                }

                if (clickType.isRightClick()) {
                    // Right Click with empty cursor -> Pick up HALF stack onto cursor
                    int totalAmount = slotItem.getAmount();
                    int half = (int) Math.ceil(totalAmount / 2.0);
                    int rem = totalAmount - half;

                    ItemStack pickedUpHalf = stripStorageMeta(slotItem);
                    pickedUpHalf.setAmount(half);

                    if (rem <= 0) {
                        topInventory.setItem(rawSlot, null);
                        instance.removeSlotLoot(slotIndex);
                    } else {
                        topInventory.setItem(rawSlot, applyStorageMeta(plugin, instance, slotItem.getType(), rem));
                        instance.setSlotLoot(slotIndex, slotItem.getType(), rem);
                    }
                    instance.setUpdatedAt(System.currentTimeMillis());
                    plugin.getSpawnerManager().saveLoot(instance);
                    event.setCursor(pickedUpHalf);
                    player.updateInventory();
                    return;
                }

                // Normal Left Click -> Pick up FULL stack onto cursor
                ItemStack pickedUp = stripStorageMeta(slotItem);
                topInventory.setItem(rawSlot, null);
                instance.removeSlotLoot(slotIndex);
                instance.setUpdatedAt(System.currentTimeMillis());
                plugin.getSpawnerManager().saveLoot(instance);
                event.setCursor(pickedUp);
                player.updateInventory();
            }
        }
    }

    public void handleInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        this.lastInteractionTime = System.currentTimeMillis();

        SpawnerInstance instance = plugin.getSpawnerManager().getSpawner(spawnerId);
        if (instance == null) {
            event.setCancelled(true);
            return;
        }

        int lastRow = plugin.getSpawnerManager().getStorageSize() - 9;
        for (int rawSlot : event.getRawSlots()) {
            if (rawSlot >= lastRow && rawSlot < plugin.getSpawnerManager().getStorageSize()) {
                event.setCancelled(true);
                player.updateInventory();
                return;
            }
        }
    }
}
