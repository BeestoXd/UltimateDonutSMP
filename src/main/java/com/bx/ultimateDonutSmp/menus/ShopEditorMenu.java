package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.ShopManager;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ShopEditorMenu extends BaseMenu {

    private final String menuSection;
    private final Set<Integer> reservedSlots;
    private final int closeSlot;
    private ItemStack selectedTemplate;
    private boolean instructionsSent;

    public ShopEditorMenu(UltimateDonutSmp plugin, String menuSection) {
        super(plugin, "&8Editing shop: " + friendlyName(menuSection), plugin.getShopManager().getMenuSize(menuSection));
        this.menuSection = menuSection;
        this.reservedSlots = plugin.getShopManager().getReservedSlots(menuSection);
        this.closeSlot = resolveCloseSlot(plugin, menuSection, inventory.getSize());
    }

    @Override
    public void build(Player player) {
        clear();

        for (ShopManager.ShopItem item : plugin.getShopManager().loadMenuItems(menuSection)) {
            if (item.slot() < 0 || item.slot() >= inventory.getSize() || item.slot() == closeSlot) {
                continue;
            }
            set(item.slot(), createEditorItem(item));
        }

        for (int reserved : reservedSlots) {
            if (reserved == closeSlot || reserved < 0 || reserved >= inventory.getSize()) {
                continue;
            }
            set(reserved, ItemUtils.createItem(
                    Material.GRAY_STAINED_GLASS_PANE,
                    "&7Menu button slot",
                    List.of("&7The shop uses this slot for its own buttons.")
            ));
        }

        set(closeSlot, ItemUtils.createItem(
                Material.BARRIER,
                "&cClose Editor",
                List.of(
                        "&7Click to close this editor.",
                        "&7Changes are saved instantly."
                )
        ));

        if (!instructionsSent) {
            instructionsSent = true;
            player.sendMessage(ColorUtils.toComponent("&8[&bShop&8] &7Click an item in your inventory to select it, then click a shop slot to place or replace it."));
            player.sendMessage(ColorUtils.toComponent("&8[&bShop&8] &7Rename it to &f[PRICE] 250 &7first to set the price, otherwise its worth is used."));
            player.sendMessage(ColorUtils.toComponent("&8[&bShop&8] &7Click a filled slot with nothing selected to clear it."));
        }
    }

    public void handleInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        int rawSlot = event.getRawSlot();
        if (rawSlot >= 0 && rawSlot < inventory.getSize()) {
            event.setCancelled(true);

            if (rawSlot == closeSlot) {
                SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
                player.closeInventory();
                return;
            }

            if (reservedSlots.contains(rawSlot)) {
                player.sendMessage(ColorUtils.toComponent("&cThat slot belongs to the menu buttons. Pick another one."));
                return;
            }

            if (selectedTemplate != null) {
                ShopManager.PricedItem priced = plugin.getShopManager().readPriceTag(selectedTemplate);
                ShopManager.EditResult result = plugin.getShopManager()
                        .upsertMenuItem(menuSection, rawSlot, priced.item(), priced.price());
                player.sendMessage(ColorUtils.toComponent(result.message()));
                if (result.success()) {
                    SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
                    build(player);
                }
                return;
            }

            ItemStack current = inventory.getItem(rawSlot);
            if (current == null || current.getType().isAir()) {
                player.sendMessage(ColorUtils.toComponent("&cSelect an item from your inventory first."));
                return;
            }

            ShopManager.EditResult result = plugin.getShopManager().removeMenuItem(menuSection, rawSlot);
            player.sendMessage(ColorUtils.toComponent(result.message()));
            if (result.success()) {
                SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
                build(player);
            }
            return;
        }

        event.setCancelled(true);
        if (event.getClickedInventory() == null || event.getCurrentItem() == null || event.getCurrentItem().getType().isAir()) {
            return;
        }

        selectedTemplate = event.getCurrentItem().clone();
        player.sendMessage(ColorUtils.toComponent("&aSelected &f" + readableItemName(selectedTemplate) + "&a. Click a shop slot to place it."));
        SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
    }

    public void handleInventoryDrag(InventoryDragEvent event) {
        event.setCancelled(true);
    }

    @Override
    public void onClose(Player player) {
        selectedTemplate = null;
    }

    private ItemStack createEditorItem(ShopManager.ShopItem item) {
        List<String> lore = new ArrayList<>();
        lore.add("&7Price: &a" + NumberUtils.format(item.pricePerUnit()));
        lore.add("&7Currency: &f" + item.currency().name());
        if (item.hasCustomItemData()) {
            lore.add("&7Sold exactly as stored.");
        }
        lore.add("");
        lore.add("&7Click with an item selected to replace.");
        lore.add("&7Click with nothing selected to clear.");

        ItemStack custom = plugin.getShopManager().createCustomItem(item);
        if (custom != null) {
            ItemStack preview = custom.clone();
            preview.setAmount(1);
            ItemMeta meta = preview.getItemMeta();
            if (meta != null) {
                meta.setLore(ColorUtils.colorizeList(lore));
                preview.setItemMeta(meta);
            }
            return preview;
        }

        ItemStack preview = ItemUtils.createItem(item.material(), item.displayName(), lore);
        if (item.enchantments() != null && !item.enchantments().isEmpty()) {
            ItemUtils.addEnchantments(preview, item.enchantments());
        }
        if (item.glint() != null) {
            ItemUtils.setGlint(preview, item.glint());
        }
        return preview;
    }

    private String readableItemName(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta() != null && item.getItemMeta().hasDisplayName()) {
            return ColorUtils.strip(item.getItemMeta().getDisplayName());
        }
        return plugin.getWorthManager().prettifyMaterial(item.getType());
    }

    private static int resolveCloseSlot(UltimateDonutSmp plugin, String menuSection, int size) {
        int backSlot = plugin.getShopManager().getBackButtonSlot(menuSection);
        return backSlot >= 0 && backSlot < size ? backSlot : size - 1;
    }

    private static String friendlyName(String menuSection) {
        String trimmed = menuSection.endsWith("-MENU")
                ? menuSection.substring(0, menuSection.length() - "-MENU".length())
                : menuSection;
        return trimmed.toLowerCase(Locale.US).replace('-', ' ');
    }
}
