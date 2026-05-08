package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.ShopManager;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class PurchaseShopMenu extends BaseMenu {

    private final ShopManager.ShopItem item;
    private final String originMenuSection;
    private final int originPage;
    private int quantity;

    public PurchaseShopMenu(
            UltimateDonutSmp plugin,
            ShopManager.ShopItem item,
            String originMenuSection,
            int originPage
    ) {
        super(
                plugin,
                plugin.getConfigManager().getMenus().getString("PURCHASE-SHOP-MENU.TITLE", "&8Confirmation Menu"),
                plugin.getConfigManager().getMenus().getInt("PURCHASE-SHOP-MENU.SIZE", 27)
        );
        this.item = item;
        this.originMenuSection = originMenuSection;
        this.originPage = Math.max(0, originPage);
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);

        ShopManager.ShopRestriction restriction = plugin.getShopManager().getPurchaseRestriction(item);
        if (quantity <= 0) {
            quantity = restriction.defaultQuantity();
        }
        quantity = restriction.clamp(quantity);

        buildPreviewItem(restriction);
        buildCancelButton();
        buildConfirmButton();

        if (restriction.adjustable()) {
            buildQuantityButtons();
        }
    }

    @Override
    public void handleClick(int slot, Player player) {
        ShopManager.ShopRestriction restriction = plugin.getShopManager().getPurchaseRestriction(item);

        if (slot == getCancelSlot()) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            new ShopMenu(plugin, originMenuSection, originPage).open(player);
            return;
        }

        if (slot == getConfirmSlot()) {
            ShopManager.PurchaseResult result = plugin.getShopManager().purchase(player, item, quantity);
            if (result.success()) {
                playSuccessSound(player);
                player.sendMessage(ColorUtils.toComponent(resolveSuccessMessage(result)));
                new ShopMenu(plugin, originMenuSection, originPage).open(player);
            } else {
                playErrorSound(player);
                player.sendMessage(ColorUtils.toComponent(resolveErrorMessage(result)));
                quantity = restriction.clamp(quantity);
                build(player);
            }
            return;
        }

        int updatedQuantity = quantity;
        updatedQuantity = applyAddButtons(slot, updatedQuantity);
        updatedQuantity = applyRemoveButtons(slot, updatedQuantity);
        updatedQuantity = restriction.clamp(updatedQuantity);

        if (updatedQuantity != quantity) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            quantity = updatedQuantity;
            build(player);
        }
    }

    private void buildPreviewItem(ShopManager.ShopRestriction restriction) {
        List<String> lore = new ArrayList<>();
        for (String line : item.lore()) {
            if (!isRedundantPriceLore(line)) {
                lore.add(line);
            }
        }
        if (!lore.isEmpty()) {
            lore.add("");
        }

        String priceLine = getCurrencyPreviewLore();
        if (!priceLine.isBlank()) {
            lore.add(replaceCommonPlaceholders(priceLine));
        }
        lore.add("&7Quantity: &f" + quantity);
        lore.add("&7Allowed: &f" + restriction.minQuantity() + "&7 - &f" + restriction.maxQuantity());
        lore.add("&7Currency: &f" + (item.currency() == ShopManager.Currency.SHARD ? "Shards" : "Money"));

        ItemStack preview = ItemUtils.createItem(item.material(), item.displayName(), lore);
        preview.setAmount(Math.min(quantity, preview.getMaxStackSize()));
        set(getPreviewSlot(), preview);
    }

    private boolean isRedundantPriceLore(String line) {
        String plain = ColorUtils.strip(line).toLowerCase();
        return plain.contains("buy price")
                || plain.contains("buyprice")
                || plain.contains("harga beli");
    }

    private void buildCancelButton() {
        set(getCancelSlot(), ItemUtils.createItem(
                ItemUtils.parseMaterial(getMenus().getString("PURCHASE-SHOP-MENU.BUTTONS.CANCEL.MATERIAL", "RED_STAINED_GLASS_PANE")),
                getMenus().getString("PURCHASE-SHOP-MENU.BUTTONS.CANCEL.NAME", "&cCancel"),
                replaceCommonPlaceholders(readLines("PURCHASE-SHOP-MENU.BUTTONS.CANCEL.LORE"))
        ));
    }

    private void buildConfirmButton() {
        set(getConfirmSlot(), ItemUtils.createItem(
                ItemUtils.parseMaterial(getMenus().getString("PURCHASE-SHOP-MENU.BUTTONS.CONFIRM.MATERIAL", "LIME_STAINED_GLASS_PANE")),
                replaceCommonPlaceholders(getMenus().getString("PURCHASE-SHOP-MENU.BUTTONS.CONFIRM.NAME", "&aConfirm")),
                replaceCommonPlaceholders(readLines("PURCHASE-SHOP-MENU.BUTTONS.CONFIRM.LORE"))
        ));
    }

    private void buildQuantityButtons() {
        Material addMaterial = ItemUtils.parseMaterial(
                getMenus().getString("PURCHASE-SHOP-MENU.BUTTONS.QUANTITY_ADJUST.ADD.MATERIAL", "LIME_STAINED_GLASS_PANE")
        );
        Material removeMaterial = ItemUtils.parseMaterial(
                getMenus().getString("PURCHASE-SHOP-MENU.BUTTONS.QUANTITY_ADJUST.REMOVE.MATERIAL", "RED_STAINED_GLASS_PANE")
        );

        buildQuantityButton(
                "PURCHASE-SHOP-MENU.BUTTONS.QUANTITY_ADJUST.REMOVE.REMOVE_64",
                removeMaterial
        );
        buildQuantityButton(
                "PURCHASE-SHOP-MENU.BUTTONS.QUANTITY_ADJUST.REMOVE.REMOVE_10",
                removeMaterial
        );
        buildQuantityButton(
                "PURCHASE-SHOP-MENU.BUTTONS.QUANTITY_ADJUST.REMOVE.REMOVE_1",
                removeMaterial
        );
        buildQuantityButton(
                "PURCHASE-SHOP-MENU.BUTTONS.QUANTITY_ADJUST.ADD.ADD_1",
                addMaterial
        );
        buildQuantityButton(
                "PURCHASE-SHOP-MENU.BUTTONS.QUANTITY_ADJUST.ADD.ADD_10",
                addMaterial
        );
        buildQuantityButton(
                "PURCHASE-SHOP-MENU.BUTTONS.QUANTITY_ADJUST.ADD.SET_64",
                addMaterial
        );
    }

    private void buildQuantityButton(String path, Material material) {
        int slot = getMenus().getInt(path + ".SLOT", -1);
        if (slot < 0) {
            return;
        }

        List<String> lore = List.of(
                "&7Current quantity: &f" + quantity,
                "&eClick to adjust the quantity"
        );
        set(slot, ItemUtils.createItem(
                material,
                replaceCommonPlaceholders(getMenus().getString(path + ".NAME", "&fAdjust")),
                lore
        ));
    }

    private int applyAddButtons(int slot, int currentQuantity) {
        if (slot == getMenus().getInt("PURCHASE-SHOP-MENU.BUTTONS.QUANTITY_ADJUST.ADD.ADD_1.SLOT", -1)) {
            return currentQuantity + getMenus().getInt("PURCHASE-SHOP-MENU.BUTTONS.QUANTITY_ADJUST.ADD.ADD_1.INCREMENT", 1);
        }
        if (slot == getMenus().getInt("PURCHASE-SHOP-MENU.BUTTONS.QUANTITY_ADJUST.ADD.ADD_10.SLOT", -1)) {
            return currentQuantity + getMenus().getInt("PURCHASE-SHOP-MENU.BUTTONS.QUANTITY_ADJUST.ADD.ADD_10.INCREMENT", 10);
        }
        if (slot == getMenus().getInt("PURCHASE-SHOP-MENU.BUTTONS.QUANTITY_ADJUST.ADD.SET_64.SLOT", -1)) {
            return getMenus().getInt("PURCHASE-SHOP-MENU.BUTTONS.QUANTITY_ADJUST.ADD.SET_64.INCREMENT", 64);
        }
        return currentQuantity;
    }

    private int applyRemoveButtons(int slot, int currentQuantity) {
        if (slot == getMenus().getInt("PURCHASE-SHOP-MENU.BUTTONS.QUANTITY_ADJUST.REMOVE.REMOVE_1.SLOT", -1)) {
            return currentQuantity - getMenus().getInt("PURCHASE-SHOP-MENU.BUTTONS.QUANTITY_ADJUST.REMOVE.REMOVE_1.DECREMENT", 1);
        }
        if (slot == getMenus().getInt("PURCHASE-SHOP-MENU.BUTTONS.QUANTITY_ADJUST.REMOVE.REMOVE_10.SLOT", -1)) {
            return currentQuantity - getMenus().getInt("PURCHASE-SHOP-MENU.BUTTONS.QUANTITY_ADJUST.REMOVE.REMOVE_10.DECREMENT", 10);
        }
        if (slot == getMenus().getInt("PURCHASE-SHOP-MENU.BUTTONS.QUANTITY_ADJUST.REMOVE.REMOVE_64.SLOT", -1)) {
            return currentQuantity - getMenus().getInt("PURCHASE-SHOP-MENU.BUTTONS.QUANTITY_ADJUST.REMOVE.REMOVE_64.DECREMENT", 64);
        }
        return currentQuantity;
    }

    private String resolveSuccessMessage(ShopManager.PurchaseResult result) {
        String path = result.currency() == ShopManager.Currency.SHARD
                ? "PURCHASE-SHOP-MENU.MESSAGES.SUCCESS.SHARDS"
                : "PURCHASE-SHOP-MENU.MESSAGES.SUCCESS.MONEY";
        String fallback = result.currency() == ShopManager.Currency.SHARD
                ? "&7You bought &e{quantity} {item-name}&7 for &5{amount} shards"
                : "&7You bought &e{quantity} {item-name}&7 for &a${amount}";
        return replaceMessagePlaceholders(getMenus().getString(path, fallback));
    }

    private String resolveErrorMessage(ShopManager.PurchaseResult result) {
        return switch (result.reason()) {
            case NO_MONEY -> getMenus().getString(
                    "PURCHASE-SHOP-MENU.MESSAGES.ERROR.NO_MONEY",
                    "&cYou don't have enough money."
            );
            case NO_SHARDS -> getMenus().getString(
                    "PURCHASE-SHOP-MENU.MESSAGES.ERROR.NO_SHARDS",
                    "&cYou don't have enough shards."
            );
            case INVENTORY_FULL -> getMenus().getString(
                    "PURCHASE-SHOP-MENU.MESSAGES.ERROR.FULL_INVENTORY",
                    "&cYour inventory is full."
            );
            case NO_PERMISSION -> "&cYou do not have permission to buy this item.";
            case INVALID_QUANTITY -> "&cThe selected quantity is not allowed for this item.";
            case INVALID_ITEM -> "&cThis item cannot be purchased right now.";
            case NO_PLAYER_DATA -> "&cYour player data could not be loaded. Try again.";
        };
    }

    private String getCurrencyPreviewLore() {
        String path = "PURCHASE-SHOP-MENU.BUTTONS.MAIN.LORE.";
        String currencyKey = item.currency() == ShopManager.Currency.SHARD ? "SHARD" : "MONEY";
        return getMenus().getString(path + currencyKey, getMenus().getString(path + "DEFAULT", ""));
    }

    private String replaceMessagePlaceholders(String text) {
        String amount = item.currency() == ShopManager.Currency.SHARD
                ? NumberUtils.format(Math.round(item.pricePerUnit() * quantity))
                : NumberUtils.format(item.pricePerUnit() * quantity);
        return replaceCommonPlaceholders(text)
                .replace("{amount}", amount)
                .replace("{item-name}", resolveItemName())
                .replace("{quantity}", String.valueOf(quantity));
    }

    private String replaceCommonPlaceholders(String text) {
        if (text == null) {
            return "";
        }

        String amount = item.currency() == ShopManager.Currency.SHARD
                ? NumberUtils.format(Math.round(item.pricePerUnit() * quantity))
                : NumberUtils.format(item.pricePerUnit() * quantity);
        String formattedPrice = item.currency() == ShopManager.Currency.SHARD ? amount : "$" + amount;
        return text
                .replace("${price}", formattedPrice)
                .replace("%price%", amount)
                .replace("{price}", amount)
                .replace("%quantity%", String.valueOf(quantity))
                .replace("{quantity}", String.valueOf(quantity))
                .replace("{item-name}", resolveItemName())
                .replace("{item_name}", resolveItemName());
    }

    private List<String> replaceCommonPlaceholders(List<String> lines) {
        List<String> replaced = new ArrayList<>();
        for (String line : lines) {
            replaced.add(replaceCommonPlaceholders(line));
        }
        return replaced;
    }

    private String resolveItemName() {
        if (item.displayName() != null && !item.displayName().isBlank()) {
            return ColorUtils.strip(item.displayName());
        }
        return plugin.getWorthManager().prettifyMaterial(item.material());
    }

    private List<String> readLines(String path) {
        if (getMenus().isList(path)) {
            return getMenus().getStringList(path);
        }

        String singleLine = getMenus().getString(path);
        if (singleLine == null || singleLine.isBlank()) {
            return List.of();
        }
        return List.of(singleLine);
    }

    private void playSuccessSound(Player player) {
        String sound = getMenus().getString(
                "PURCHASE-SHOP-MENU.SOUNDS.SUCCESS",
                plugin.getConfigManager().getSound("SHOP.BUY-SUCCESS")
        );
        SoundUtils.play(player, sound);
    }

    private void playErrorSound(Player player) {
        String sound = getMenus().getString(
                "PURCHASE-SHOP-MENU.SOUNDS.ERROR",
                plugin.getConfigManager().getSound("SHOP.NO-MONEY")
        );
        SoundUtils.play(player, sound);
    }

    private int getPreviewSlot() {
        return getMenus().getInt("PURCHASE-SHOP-MENU.BUTTONS.MAIN.SLOT", 13);
    }

    private int getCancelSlot() {
        return getMenus().getInt("PURCHASE-SHOP-MENU.BUTTONS.CANCEL.SLOT", 21);
    }

    private int getConfirmSlot() {
        return getMenus().getInt("PURCHASE-SHOP-MENU.BUTTONS.CONFIRM.SLOT", 23);
    }

    private FileConfiguration getMenus() {
        return plugin.getConfigManager().getMenus();
    }
}
