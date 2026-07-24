package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.SpawnerInstance;
import com.bx.ultimateDonutSmp.models.SpawnerLootEntry;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SpawnerStorageMenu extends BaseMenu {

    private final long spawnerId;
    private final int page;
    private long lastInteractionTime = 0L;

    public SpawnerStorageMenu(UltimateDonutSmp plugin, long spawnerId, int page) {
        super(plugin, "&8Spawner Storage", plugin.getSpawnerManager().getStorageSize());
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
            FileConfiguration config = plugin.getConfigManager().getMenus();
            boolean isFiltered = instance.isLootDisabled(material.name());

            String enabledTxt = config.getString("SPAWNER-MENUS.STORAGE-MENU.ITEM-META.FILTER-ENABLED", "&aEnabled &7(Storing)");
            String disabledTxt = config.getString("SPAWNER-MENUS.STORAGE-MENU.ITEM-META.FILTER-DISABLED", "&cDisabled &7(Not Storing)");
            String filterStatus = isFiltered ? disabledTxt : enabledTxt;

            String titleFormat = config.getString("SPAWNER-MENUS.STORAGE-MENU.ITEM-META.TITLE", "&b{material}");
            meta.setDisplayName(ColorUtils.toComponent(titleFormat.replace("{material}", plugin.getWorthManager().prettifyMaterial(material))));

            List<String> rawLore = config.getStringList("SPAWNER-MENUS.STORAGE-MENU.ITEM-META.LORE");
            List<String> lore = new ArrayList<>();
            if (rawLore.isEmpty()) {
                lore.add("&7Filter Status: " + filterStatus);
                lore.add("");
                lore.add("&eLeft-Click &7to pick up");
                lore.add("&eRight-Click &7to pick up half / place 1");
                lore.add("&eShift-Left &7to collect to inventory");
                lore.add("&eShift-Right &7to toggle filter");
            } else {
                for (String line : rawLore) {
                    lore.add(line.replace("{filter_status}", filterStatus)
                            .replace("{material}", plugin.getWorthManager().prettifyMaterial(material)));
                }
            }
            meta.setLore(ColorUtils.toComponentList(lore));
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
            inventory = Bukkit.createInventory(this, plugin.getSpawnerManager().getStorageSize(), ColorUtils.toComponent("&8Spawner Missing"));
            clear();
            fill(Material.GRAY_STAINED_GLASS_PANE);
            set(inventory.getSize() / 2, ItemUtils.createItem(Material.BARRIER, "&cSpawner Not Found"));
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

        FileConfiguration config = plugin.getConfigManager().getMenus();

        // 1. Back Button
        int backSlot = config.getInt("SPAWNER-MENUS.STORAGE-MENU.BACK-BUTTON.SLOT", lastRow);
        String backMatName = config.getString("SPAWNER-MENUS.STORAGE-MENU.BACK-BUTTON.MATERIAL", "BARRIER");
        Material backMat = Material.matchMaterial(backMatName);
        if (backMat == null) backMat = Material.BARRIER;
        String backTitle = config.getString("SPAWNER-MENUS.STORAGE-MENU.BACK-BUTTON.TITLE", "&cBACK");
        List<String> backLore = config.getStringList("SPAWNER-MENUS.STORAGE-MENU.BACK-BUTTON.LORE");
        if (backLore.isEmpty()) backLore = List.of("&7Return to spawner menu.");
        set(backSlot, ItemUtils.createItem(backMat, backTitle, backLore));

        // 2. Collect All Button
        int collectSlot = config.getInt("SPAWNER-MENUS.STORAGE-MENU.COLLECT-ALL-BUTTON.SLOT", lastRow + 3);
        String collectMatName = config.getString("SPAWNER-MENUS.STORAGE-MENU.COLLECT-ALL-BUTTON.MATERIAL", "SPECTRAL_ARROW");
        Material collectMat = Material.matchMaterial(collectMatName);
        if (collectMat == null) collectMat = Material.SPECTRAL_ARROW;
        String collectTitle = config.getString("SPAWNER-MENUS.STORAGE-MENU.COLLECT-ALL-BUTTON.TITLE", "&eCOLLECT ALL");
        List<String> collectLore = config.getStringList("SPAWNER-MENUS.STORAGE-MENU.COLLECT-ALL-BUTTON.LORE");
        if (collectLore.isEmpty()) collectLore = List.of("&e• &fCollect all loot from storage into inventory");
        set(collectSlot, ItemUtils.createItem(collectMat, collectTitle, collectLore));

        // 3. Previous Page Button
        int prevSlot = config.getInt("SPAWNER-MENUS.STORAGE-MENU.PREVIOUS-PAGE-BUTTON.SLOT", lastRow + 4);
        if (safePage > 1) {
            String prevMatName = config.getString("SPAWNER-MENUS.STORAGE-MENU.PREVIOUS-PAGE-BUTTON.MATERIAL", "ARROW");
            Material prevMat = Material.matchMaterial(prevMatName);
            if (prevMat == null) prevMat = Material.ARROW;
            String prevTitle = config.getString("SPAWNER-MENUS.STORAGE-MENU.PREVIOUS-PAGE-BUTTON.TITLE", "&aPREVIOUS PAGE");
            List<String> rawPrevLore = config.getStringList("SPAWNER-MENUS.STORAGE-MENU.PREVIOUS-PAGE-BUTTON.LORE");
            List<String> prevLore = new ArrayList<>();
            if (rawPrevLore.isEmpty()) {
                prevLore.add("&fClick to go back to page " + (safePage - 1));
            } else {
                for (String line : rawPrevLore) {
                    prevLore.add(line.replace("{prev_page}", String.valueOf(safePage - 1)));
                }
            }
            set(prevSlot, ItemUtils.createItem(prevMat, prevTitle, prevLore));
        } else {
            set(prevSlot, ItemUtils.createPlaceholder(Material.GRAY_STAINED_GLASS_PANE));
        }

        // 4. Next Page Button
        int nextSlot = config.getInt("SPAWNER-MENUS.STORAGE-MENU.NEXT-PAGE-BUTTON.SLOT", lastRow + 6);
        if (safePage < totalPages) {
            String nextMatName = config.getString("SPAWNER-MENUS.STORAGE-MENU.NEXT-PAGE-BUTTON.MATERIAL", "ARROW");
            Material nextMat = Material.matchMaterial(nextMatName);
            if (nextMat == null) nextMat = Material.ARROW;
            String nextTitle = config.getString("SPAWNER-MENUS.STORAGE-MENU.NEXT-PAGE-BUTTON.TITLE", "&aNEXT PAGE");
            List<String> rawNextLore = config.getStringList("SPAWNER-MENUS.STORAGE-MENU.NEXT-PAGE-BUTTON.LORE");
            List<String> nextLore = new ArrayList<>();
            if (rawNextLore.isEmpty()) {
                nextLore.add("&fClick to go to page " + (safePage + 1));
            } else {
                for (String line : rawNextLore) {
                    nextLore.add(line.replace("{next_page}", String.valueOf(safePage + 1)));
                }
            }
            set(nextSlot, ItemUtils.createItem(nextMat, nextTitle, nextLore));
        } else {
            set(nextSlot, ItemUtils.createPlaceholder(Material.GRAY_STAINED_GLASS_PANE));
        }

        // 5. Drop Loot Button
        int dropSlot = config.getInt("SPAWNER-MENUS.STORAGE-MENU.DROP-LOOT-BUTTON.SLOT", lastRow + 7);
        String dropMatName = config.getString("SPAWNER-MENUS.STORAGE-MENU.DROP-LOOT-BUTTON.MATERIAL", "DROPPER");
        Material dropMat = Material.matchMaterial(dropMatName);
        if (dropMat == null) dropMat = Material.DROPPER;
        String dropTitle = config.getString("SPAWNER-MENUS.STORAGE-MENU.DROP-LOOT-BUTTON.TITLE", "&aDROP LOOT");
        List<String> dropLore = config.getStringList("SPAWNER-MENUS.STORAGE-MENU.DROP-LOOT-BUTTON.LORE");
        if (dropLore.isEmpty()) dropLore = List.of("&fClick to drop all loot on the page");
        set(dropSlot, ItemUtils.createItem(dropMat, dropTitle, dropLore));

        // 6. Sell All Button
        int sellSlot = config.getInt("SPAWNER-MENUS.STORAGE-MENU.SELL-ALL-BUTTON.SLOT", lastRow + 8);
        String sellMatName = config.getString("SPAWNER-MENUS.STORAGE-MENU.SELL-ALL-BUTTON.MATERIAL", "GOLD_INGOT");
        Material sellMat = Material.matchMaterial(sellMatName);
        if (sellMat == null) sellMat = Material.GOLD_INGOT;
        String sellTitle = config.getString("SPAWNER-MENUS.STORAGE-MENU.SELL-ALL-BUTTON.TITLE", "&aSELL ALL");
        List<String> sellLore = config.getStringList("SPAWNER-MENUS.STORAGE-MENU.SELL-ALL-BUTTON.LORE");
        if (sellLore.isEmpty()) sellLore = List.of("&fClick to sell all mob drops!");
        set(sellSlot, ItemUtils.createItem(sellMat, sellTitle, sellLore));
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
            return;
        }

        FileConfiguration config = plugin.getConfigManager().getMenus();
        int backSlot = config.getInt("SPAWNER-MENUS.STORAGE-MENU.BACK-BUTTON.SLOT", lastRow);
        int collectSlot = config.getInt("SPAWNER-MENUS.STORAGE-MENU.COLLECT-ALL-BUTTON.SLOT", lastRow + 3);
        int prevSlot = config.getInt("SPAWNER-MENUS.STORAGE-MENU.PREVIOUS-PAGE-BUTTON.SLOT", lastRow + 4);
        int nextSlot = config.getInt("SPAWNER-MENUS.STORAGE-MENU.NEXT-PAGE-BUTTON.SLOT", lastRow + 6);
        int dropSlot = config.getInt("SPAWNER-MENUS.STORAGE-MENU.DROP-LOOT-BUTTON.SLOT", lastRow + 7);
        int sellSlot = config.getInt("SPAWNER-MENUS.STORAGE-MENU.SELL-ALL-BUTTON.SLOT", lastRow + 8);

        // 2. Click in top inventory control bar / non-content slots
        if (rawSlot >= lastRow || rawSlot == backSlot || rawSlot == collectSlot || rawSlot == prevSlot || rawSlot == nextSlot || rawSlot == dropSlot || rawSlot == sellSlot) {
            event.setCancelled(true);
            if (rawSlot == backSlot) {
                new SpawnerMainMenu(plugin, spawnerId).open(player);
            } else if (rawSlot == collectSlot) {
                player.sendMessage(ColorUtils.toComponent(plugin.getSpawnerManager().collectAllLoot(player, instance).message()));
                new SpawnerStorageMenu(plugin, spawnerId, page).open(player);
            } else if (rawSlot == prevSlot && page > 1) {
                new SpawnerStorageMenu(plugin, spawnerId, page - 1).open(player);
            } else if (rawSlot == nextSlot) {
                new SpawnerStorageMenu(plugin, spawnerId, page + 1).open(player);
            } else if (rawSlot == dropSlot) {
                player.sendMessage(ColorUtils.toComponent(plugin.getSpawnerManager().dropAllLoot(player, instance).message()));
                new SpawnerStorageMenu(plugin, spawnerId, page).open(player);
            } else if (rawSlot == sellSlot) {
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

                    String statusMsg = !currentState ? "&cDisabled &7(Not Storing)" : "&aEnabled &7(Storing)";
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
