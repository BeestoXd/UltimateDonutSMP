package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.CurrencyManager;
import com.bx.ultimateDonutSmp.managers.ShopManager;
import com.bx.ultimateDonutSmp.models.SellCategory;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public class SellMenu extends BaseMenu {

    private static final int SIZE = 54;
    private static final int SELLABLE_SLOT_END = 45;
    private static final long PROCESS_DELAY_TICKS = 2L;
    private static final List<SellCategory> BUTTON_ORDER = List.of(
            SellCategory.CROPS,
            SellCategory.ORES,
            SellCategory.MOBS,
            SellCategory.NATURAL,
            SellCategory.ARMOR_AND_TOOLS,
            SellCategory.FISH,
            SellCategory.BOOK,
            SellCategory.POTIONS,
            SellCategory.BLOCKS
    );

    private boolean processScheduled;
    private String mode = "instant";
    private boolean confirmMode;
    private Player player;

    public SellMenu(UltimateDonutSmp plugin) {
        super(plugin, plugin.getConfigManager().getMenus().getString(
                "SELL-MENU.TITLE",
                "&8Place items in here to sell"
        ), SIZE);
    }

    @Override
    public void build(Player player) {
        this.player = player;
        FileConfiguration menus = plugin.getConfigManager().getMenus();
        this.mode = menus.getString("SELL-MENU.MODE", "instant");
        this.confirmMode = isConfirmMode(mode);

        clear();
        if (confirmMode) {
            buildConfirmSellGUI(player);
        } else {
            refreshMultiplierButtons(player);
        }
    }

    private void buildConfirmSellGUI(Player player) {
        FileConfiguration menus = plugin.getConfigManager().getMenus();
        double total = plugin.getShopManager().previewInventoryContents(player, inventory, 0, getSellableSlotEnd());
        String compactPrice = plugin.getCurrencyManager().formatCompactAmount(CurrencyManager.CurrencyType.MONEY, total);
        String formattedPrice = plugin.getCurrencyManager().formatMoney(total);

        Material material = ItemUtils.parseMaterial(menus.getString("SELL-MENU.CONFIRM-BUTTON.MATERIAL", "LIME_STAINED_GLASS_PANE"));
        String title = applyPricePlaceholders(
                menus.getString("SELL-MENU.CONFIRM-BUTTON.TITLE", "&a&lConfirm sell"),
                compactPrice,
                formattedPrice
        );
        List<String> lore = applyPricePlaceholders(
                resolveConfirmLore(menus.getStringList("SELL-MENU.CONFIRM-BUTTON.LORE")),
                compactPrice,
                formattedPrice
        );
        set(53, ItemUtils.createItem(material, title, lore));
    }

    @Override
    public void handleClick(int slot, Player player, ClickType clickType) {
        if (confirmMode && slot == 53) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            ShopManager.SellResult result = plugin.getShopManager().sellInventoryContents(player, inventory, 0, getSellableSlotEnd());
            if (result.status() == ShopManager.SellStatus.NO_SELLABLE_ITEMS) {
                player.sendMessage(com.bx.ultimateDonutSmp.utils.ColorUtils.toComponent(
                        plugin.getConfigManager().getMessage("WORTH.NO-SELLABLE", "&cThis item is not sellable.")
                ));
            }
            buildConfirmSellGUI(player);
            return;
        }

        SellCategory category = getCategoryForSlot(slot);
        if (category == null) {
            return;
        }

        SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
        if (clickType.isRightClick()) {
            new WorthMenu(plugin, 1, WorthMenu.SortMode.CATEGORY, category, this).open(player);
        } else {
            new SellProgressMenu(plugin, category).open(player);
        }
    }

    public void handleInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (event.isShiftClick()) {
            plugin.getSpigotScheduler().runEntityLater(player, player::updateInventory, 1L);
        }

        boolean autoSell = isAutoSellEnabled();
        int rawSlot = event.getRawSlot();
        if (rawSlot >= 0 && rawSlot < inventory.getSize()) {
            if (isSellableSlot(rawSlot)) {
                event.setCancelled(false);
                if (autoSell || confirmMode) {
                    scheduleProcessing(player);
                }
                return;
            }

            event.setCancelled(true);
            handleClick(rawSlot, player, event.getClick());
            return;
        }

        if (event.getClickedInventory() == null) {
            return;
        }

        event.setCancelled(false);
        if (autoSell || confirmMode) {
            scheduleProcessing(player);
        }
    }

    public void handleInventoryDrag(InventoryDragEvent event) {
        for (int rawSlot : event.getRawSlots()) {
            if (rawSlot < inventory.getSize() && !isSellableSlot(rawSlot)) {
                event.setCancelled(true);
                return;
            }
        }

        event.setCancelled(false);
        if ((isAutoSellEnabled() || confirmMode) && event.getWhoClicked() instanceof Player player) {
            scheduleProcessing(player);
        }
    }

    @Override
    public void onClose(Player player) {
        int sellableEnd = getSellableSlotEnd();
        if (sellsOnClose(mode)) {
            plugin.getShopManager().sellInventoryContents(player, inventory, 0, sellableEnd);
        }

        for (int slot = 0; slot < sellableEnd; slot++) {
            ItemStack item = inventory.getItem(slot);
            if (item == null || item.getType().isAir()) {
                continue;
            }

            player.getInventory().addItem(item).values()
                    .forEach(leftover -> player.getWorld().dropItemNaturally(player.getLocation(), leftover));
            inventory.setItem(slot, null);
        }
    }

    private void scheduleProcessing(Player player) {
        if (processScheduled) {
            return;
        }

        processScheduled = true;
        plugin.getSpigotScheduler().runEntityLater(player, () -> {
            processScheduled = false;
            if (!player.isOnline()) {
                return;
            }
            if (!inventory.equals(player.getOpenInventory().getTopInventory())) {
                return;
            }

            if (isAutoSellEnabled()) {
                plugin.getShopManager().sellInventoryContents(player, inventory, 0, getSellableSlotEnd());
                refreshMultiplierButtons(player);
            } else if (confirmMode) {
                buildConfirmSellGUI(player);
            }
            player.updateInventory();
        }, PROCESS_DELAY_TICKS);
    }

    private void refreshMultiplierButtons(Player player) {
        FileConfiguration menus = plugin.getConfigManager().getMenus();
        Map<SellCategory, Double> progress = plugin.getShopManager().getSellProgress(player.getUniqueId());

        for (int index = 0; index < BUTTON_ORDER.size(); index++) {
            SellCategory category = BUTTON_ORDER.get(index);
            ShopManager.SellProgressInfo info = plugin.getShopManager().getSellProgressInfo(progress, category);
            String path = "SELL-MENU." + category.getSellMenuButtonKey();

            Material material = ItemUtils.parseMaterial(menus.getString(path + ".MATERIAL", "STONE"));
            String title = menus.getString(path + ".TITLE", "&f" + category.name());
            List<String> lore = applyProgressPlaceholders(menus.getStringList(path + ".LORE"), info);

            set(45 + index, ItemUtils.createItem(material, title, lore));
        }
    }

    private List<String> applyProgressPlaceholders(List<String> lore, ShopManager.SellProgressInfo info) {
        return lore.stream()
                .map(line -> line
                        .replace("{next_multiplier}", info.nextMultiplierDisplay())
                        .replace("{porcentage_level}", info.progressBar())
                        .replace("{porcentage}", String.valueOf(info.percentage()))
                        .replace("{current_earned}", plugin.getCurrencyManager().formatCompactAmount(CurrencyManager.CurrencyType.MONEY, info.earned()))
                        .replace("{next_goal}", plugin.getCurrencyManager().formatCompactAmount(CurrencyManager.CurrencyType.MONEY, info.nextGoal())))
                .toList();
    }

    private SellCategory getCategoryForSlot(int slot) {
        if (confirmMode) {
            return null;
        }
        int index = slot - 45;
        if (index < 0 || index >= BUTTON_ORDER.size()) {
            return null;
        }
        return BUTTON_ORDER.get(index);
    }

    private int getSellableSlotEnd() {
        return confirmMode ? 53 : 45;
    }

    private boolean isSellableSlot(int slot) {
        return slot >= 0 && slot < getSellableSlotEnd();
    }

    private boolean isAutoSellEnabled() {
        return sellsWhileOpen(
                mode,
                plugin.getConfigManager().getMenus().getBoolean("SELL-MENU.AUTO-SELL", true)
        );
    }

    static List<String> resolveConfirmLore(List<String> configured) {
        if (configured == null || configured.isEmpty() || isLegacyConfirmLore(configured)) {
            return List.of("&a{price_formatted}");
        }
        return configured;
    }

    static boolean isLegacyConfirmLore(List<String> lore) {
        return lore.size() == 1 && "&7Click to sell all items in the menu.".equals(lore.get(0));
    }

    static String applyPricePlaceholders(String text, String compactPrice, String formattedPrice) {
        return ShopManager.applySellPricePlaceholders(text, compactPrice, formattedPrice);
    }

    static List<String> applyPricePlaceholders(List<String> lines, String compactPrice, String formattedPrice) {
        return lines.stream()
                .map(line -> applyPricePlaceholders(line, compactPrice, formattedPrice))
                .toList();
    }

    static boolean isConfirmMode(String mode) {
        return "confirm".equalsIgnoreCase(mode);
    }

    static boolean isCloseMode(String mode) {
        return "close".equalsIgnoreCase(mode);
    }

    // Close mode ignores AUTO-SELL outright: waiting for the close is the whole point of it.
    static boolean sellsWhileOpen(String mode, boolean autoSell) {
        return !isConfirmMode(mode) && !isCloseMode(mode) && autoSell;
    }

    // AUTO-SELL only governs selling while the menu is open, so closing it still settles the grid.
    // Confirm mode is the exception: it has its own button, so closing cancels instead.
    static boolean sellsOnClose(String mode) {
        return !isConfirmMode(mode);
    }
}
