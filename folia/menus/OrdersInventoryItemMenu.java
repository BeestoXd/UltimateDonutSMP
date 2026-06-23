package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.OrdersManager;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class OrdersInventoryItemMenu extends BaseMenu {

    private final long editOrderId;
    private final OrdersManager.OrderEditNavigation editNavigation;

    public OrdersInventoryItemMenu(UltimateDonutSmp plugin) {
        super(plugin, plugin.getOrdersManager().getInventoryItemTitle(), plugin.getOrdersManager().getInventoryItemSize());
        this.editOrderId = 0L;
        this.editNavigation = null;
    }

    public OrdersInventoryItemMenu(UltimateDonutSmp plugin, long editOrderId, OrdersManager.OrderEditNavigation editNavigation) {
        super(plugin, plugin.getOrdersManager().getInventoryItemTitle(), plugin.getOrdersManager().getInventoryItemSize());
        this.editOrderId = editOrderId;
        this.editNavigation = editNavigation;
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);

        int lastRow = inventory.getSize() - 9;
        set(11, ItemUtils.createItem(
                Material.HOPPER,
                "&bᴄʜᴏᴏѕᴇ ꜰʀᴏᴍ ɪɴᴠᴇɴᴛᴏʀʏ",
                List.of(
                        "&7ᴄʟɪᴄᴋ ᴀɴ ɪᴛᴇᴍ ɪɴ ʏᴏᴜʀ ɪɴᴠᴇɴᴛᴏʀʏ.",
                        "&7ᴛʜᴇ ɪᴛᴇᴍ ɪѕ ᴄʟᴏɴᴇᴅ ᴀѕ ᴛʜᴇ ᴏʀᴅᴇʀ ᴛᴇᴍᴘʟᴀᴛᴇ.",
                        "&7ʏᴏᴜʀ ᴏʀɪɢɪɴᴀʟ ɪᴛᴇᴍ ѕᴛᴀʏѕ ɪɴ ʏᴏᴜʀ ɪɴᴠᴇɴᴛᴏʀʏ."
                )
        ));
        set(15, ItemUtils.createItem(
                Material.CHEST,
                "&eᴇxᴀᴄᴛ ɪᴛᴇᴍ ᴍᴀᴛᴄʜɪɴɢ",
                List.of(
                        "&7ᴄᴜѕᴛᴏᴍ ɴᴀᴍᴇѕ, ʟᴏʀᴇ, ᴇɴᴄʜᴀɴᴛᴍᴇɴᴛѕ,",
                        "&7ɴʙᴛ, ᴀɴᴅ ѕʜᴜʟᴋᴇʀ ᴄᴏɴᴛᴇɴᴛѕ ᴍᴜѕᴛ ᴍᴀᴛᴄʜ."
                )
        ));
        set(lastRow, ItemUtils.createItem(Material.COMPASS, "&bʙᴀᴄᴋ ᴛᴏ ᴏʀᴅᴇʀѕ", List.of("&7ʀᴇᴛᴜʀɴ ᴛᴏ ᴛʜᴇ ᴏʀᴅᴇʀ ʙᴏᴀʀᴅ")));
        set(lastRow + 8, ItemUtils.createPlaceholder(Material.GRAY_STAINED_GLASS_PANE));
    }

    public void handleInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        event.setCancelled(true);

        int rawSlot = event.getRawSlot();
        if (rawSlot >= 0 && rawSlot < inventory.getSize()) {
            handleTopClick(rawSlot, player);
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType().isAir()) {
            return;
        }

        boolean started = plugin.getOrdersManager().selectOrderItem(
                player,
                clicked,
                plugin.getOrdersManager().resolveCategoryForMaterial(clicked.getType()),
                editOrderId,
                editNavigation
        );
        SoundUtils.play(player, plugin.getConfigManager().getSound(started ? "MENUS.BUTTON-CLICK" : "ORDERS.FAIL"));
    }

    public void handleInventoryDrag(InventoryDragEvent event) {
        event.setCancelled(true);
    }

    private boolean isEditMode() {
        return editOrderId > 0L;
    }

    private void openEditMenu(Player player) {
        new OrdersEditMenu(
                plugin,
                editOrderId,
                editNavigation != null && editNavigation.backToMyOrders(),
                editNavigation == null ? 1 : editNavigation.originPage(),
                editNavigation == null ? plugin.getOrdersManager().getDefaultSort() : editNavigation.sortMode(),
                editNavigation == null ? "ALL" : editNavigation.categoryFilter()
        ).open(player);
    }

    private void handleTopClick(int slot, Player player) {
        int lastRow = inventory.getSize() - 9;
        if (slot == lastRow) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            if (isEditMode()) {
                openEditMenu(player);
            } else {
                new OrdersBrowseMenu(plugin, 1, plugin.getOrdersManager().getDefaultSort(), "ALL").open(player);
            }
            return;
        }

        if (slot == lastRow + 8) {
            return;
        }

        player.sendMessage(ColorUtils.toComponent("&7ᴄʟɪᴄᴋ ᴀɴ ɪᴛᴇᴍ ɪɴ ʏᴏᴜʀ ɪɴᴠᴇɴᴛᴏʀʏ ᴛᴏ ᴜѕᴇ ɪᴛ ᴀѕ ᴛʜᴇ ᴏʀᴅᴇʀ ᴛᴇᴍᴘʟᴀᴛᴇ."));
    }
}
