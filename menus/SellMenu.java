package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.ShopManager;
import com.bx.ultimateDonutSmp.models.SellCategory;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
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

    public SellMenu(UltimateDonutSmp plugin) {
        super(plugin, plugin.getConfigManager().getMenus().getString(
                "SELL-MENU.TITLE",
                "&8place items in here to sell"
        ), SIZE);
    }

    @Override
    public void build(Player player) {
        clear();
        refreshMultiplierButtons(player);
    }

    @Override
    public void handleClick(int slot, Player player, ClickType clickType) {
        SellCategory category = getCategoryForSlot(slot);
        if (category == null) {
            return;
        }

        SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
        new SellProgressMenu(plugin, category).open(player);
    }

    public void handleInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        int rawSlot = event.getRawSlot();
        if (rawSlot >= 0 && rawSlot < inventory.getSize()) {
            if (isSellableSlot(rawSlot)) {
                event.setCancelled(false);
                scheduleProcessing(player);
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
        scheduleProcessing(player);
    }

    public void handleInventoryDrag(InventoryDragEvent event) {
        for (int rawSlot : event.getRawSlots()) {
            if (rawSlot < inventory.getSize() && !isSellableSlot(rawSlot)) {
                event.setCancelled(true);
                return;
            }
        }

        event.setCancelled(false);
        if (event.getWhoClicked() instanceof Player player) {
            scheduleProcessing(player);
        }
    }

    @Override
    public void onClose(Player player) {
        plugin.getShopManager().sellInventoryContents(player, inventory, 0, SELLABLE_SLOT_END);

        for (int slot = 0; slot < SELLABLE_SLOT_END; slot++) {
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
        plugin.getPaperScheduler().runEntity(player, () -> {
            processScheduled = false;
            if (!player.isOnline()) {
                return;
            }

            plugin.getShopManager().sellInventoryContents(player, inventory, 0, SELLABLE_SLOT_END);
            refreshMultiplierButtons(player);
            player.updateInventory();
        });
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
                        .replace("{current_earned}", NumberUtils.formatNice(info.earned()))
                        .replace("{next_goal}", NumberUtils.formatNice(info.nextGoal())))
                .toList();
    }

    private SellCategory getCategoryForSlot(int slot) {
        int index = slot - 45;
        if (index < 0 || index >= BUTTON_ORDER.size()) {
            return null;
        }
        return BUTTON_ORDER.get(index);
    }

    private boolean isSellableSlot(int slot) {
        return slot >= 0 && slot < SELLABLE_SLOT_END;
    }
}
