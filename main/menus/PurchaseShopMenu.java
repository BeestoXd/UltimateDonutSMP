package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.CurrencyManager;
import com.bx.ultimateDonutSmp.managers.ShopManager;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
                plugin.getConfigManager().getMenus().getString("PURCHASE-SHOP-MENU.TITLE", "&8ᴄᴏɴꜰɪʀᴍᴀᴛɪᴏɴ ᴍᴇɴᴜ"),
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
                quantity = restriction.clamp(quantity);
                build(player);
                player.updateInventory();
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
        lore.add("&7ǫᴜᴀɴᴛɪᴛʏ: &f" + quantity);
        lore.add("&7ᴀʟʟᴏᴡᴇᴅ: &f" + restriction.minQuantity() + "&7 - &f" + restriction.maxQuantity());
        lore.add("&7ᴄᴜʀʀᴇɴᴄʏ: &f" + plugin.getCurrencyManager().plural(currencyType()));

        ItemStack preview = ItemUtils.createItem(item.material(), item.displayName(), lore);
        preview.setAmount(Math.min(quantity, preview.getMaxStackSize()));
        set(getPreviewSlot(), preview);
    }

    private boolean isRedundantPriceLore(String line) {
        String plain = normalizePriceLabel(ColorUtils.strip(line));
        return plain.contains("buy price")
                || plain.contains("buyprice")
                || plain.contains("harga beli");
    }

    private String normalizePriceLabel(String value) {
        return (value == null ? "" : value.toLowerCase(Locale.ROOT))
                .replace('ʙ', 'b')
                .replace('ᴜ', 'u')
                .replace('ʏ', 'y')
                .replace('ᴘ', 'p')
                .replace('ʀ', 'r')
                .replace('ɪ', 'i')
                .replace('ᴄ', 'c')
                .replace('ᴇ', 'e')
                .replace('ʜ', 'h')
                .replace('ᴀ', 'a')
                .replace('ɢ', 'g')
                .replace('ʟ', 'l');
    }

    private void buildCancelButton() {
        set(getCancelSlot(), ItemUtils.createItem(
                ItemUtils.parseMaterial(getMenus().getString("PURCHASE-SHOP-MENU.BUTTONS.CANCEL.MATERIAL", "RED_STAINED_GLASS_PANE")),
                getMenus().getString("PURCHASE-SHOP-MENU.BUTTONS.CANCEL.NAME", "&cᴄᴀɴᴄᴇʟ"),
                replaceCommonPlaceholders(readLines("PURCHASE-SHOP-MENU.BUTTONS.CANCEL.LORE"))
        ));
    }

    private void buildConfirmButton() {
        set(getConfirmSlot(), ItemUtils.createItem(
                ItemUtils.parseMaterial(getMenus().getString("PURCHASE-SHOP-MENU.BUTTONS.CONFIRM.MATERIAL", "LIME_STAINED_GLASS_PANE")),
                replaceCommonPlaceholders(getMenus().getString("PURCHASE-SHOP-MENU.BUTTONS.CONFIRM.NAME", "&aᴄᴏɴꜰɪʀᴍ")),
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
                "&7ᴄᴜʀʀᴇɴᴛ ǫᴜᴀɴᴛɪᴛʏ: &f" + quantity,
                "&eᴄʟɪᴄᴋ ᴛᴏ ᴀᴅᴊᴜѕᴛ ᴛʜᴇ ǫᴜᴀɴᴛɪᴛʏ"
        );
        set(slot, ItemUtils.createItem(
                material,
                replaceCommonPlaceholders(getMenus().getString(path + ".NAME", "&fᴀᴅᴊᴜѕᴛ")),
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
                ? "&7ʏᴏᴜ ʙᴏᴜɢʜᴛ &e{quantity} {item-name}&7 ꜰᴏʀ {price_formatted}"
                : "&7ʏᴏᴜ ʙᴏᴜɢʜᴛ &e{quantity} {item-name}&7 ꜰᴏʀ {price_formatted}";
        return replaceMessagePlaceholders(getMenus().getString(path, fallback));
    }

    private String resolveErrorMessage(ShopManager.PurchaseResult result) {
        return switch (result.reason()) {
            case NO_MONEY -> getMenus().getString(
                    "PURCHASE-SHOP-MENU.MESSAGES.ERROR.NO_MONEY",
                    "&cʏᴏᴜ ᴅᴏɴ'ᴛ ʜᴀᴠᴇ ᴇɴᴏᴜɢʜ "
                            + plugin.getCurrencyManager().plural(CurrencyManager.CurrencyType.MONEY)
                            + "."
            );
            case NO_SHARDS -> getMenus().getString(
                    "PURCHASE-SHOP-MENU.MESSAGES.ERROR.NO_SHARDS",
                    "&cʏᴏᴜ ᴅᴏɴ'ᴛ ʜᴀᴠᴇ ᴇɴᴏᴜɢʜ "
                            + plugin.getCurrencyManager().plural(CurrencyManager.CurrencyType.SHARDS)
                            + "."
            );
            case INVENTORY_FULL -> getMenus().getString(
                    "PURCHASE-SHOP-MENU.MESSAGES.ERROR.FULL_INVENTORY",
                    "&cʏᴏᴜʀ ɪɴᴠᴇɴᴛᴏʀʏ ɪѕ ꜰᴜʟʟ."
            );
            case NO_PERMISSION -> "&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ ᴛᴏ ʙᴜʏ ᴛʜɪѕ ɪᴛᴇᴍ.";
            case INVALID_QUANTITY -> "&cᴛʜᴇ ѕᴇʟᴇᴄᴛᴇᴅ ǫᴜᴀɴᴛɪᴛʏ ɪѕ ɴᴏᴛ ᴀʟʟᴏᴡᴇᴅ ꜰᴏʀ ᴛʜɪѕ ɪᴛᴇᴍ.";
            case INVALID_ITEM -> "&cᴛʜɪѕ ɪᴛᴇᴍ ᴄᴀɴɴᴏᴛ ʙᴇ ᴘᴜʀᴄʜᴀѕᴇᴅ ʀɪɢʜᴛ ɴᴏᴡ.";
            case NO_PLAYER_DATA -> "&cʏᴏᴜʀ ᴘʟᴀʏᴇʀ ᴅᴀᴛᴀ ᴄᴏᴜʟᴅ ɴᴏᴛ ʙᴇ ʟᴏᴀᴅᴇᴅ. ᴛʀʏ ᴀɢᴀɪɴ.";
        };
    }

    private String getCurrencyPreviewLore() {
        String path = "PURCHASE-SHOP-MENU.BUTTONS.MAIN.LORE.";
        String currencyKey = item.currency() == ShopManager.Currency.SHARD ? "SHARD" : "MONEY";
        return getMenus().getString(path + currencyKey, getMenus().getString(path + "DEFAULT", ""));
    }

    private String replaceMessagePlaceholders(String text) {
        CurrencyManager.CurrencyType currencyType = currencyType();
        double totalPrice = item.currency() == ShopManager.Currency.SHARD
                ? Math.round(item.pricePerUnit() * quantity)
                : item.pricePerUnit() * quantity;
        String amount = plugin.getCurrencyManager().formatAmount(currencyType, totalPrice);
        String formattedPrice = plugin.getCurrencyManager().format(currencyType, totalPrice);
        String resolved = replaceCommonPlaceholders(text);
        if (currencyType == CurrencyManager.CurrencyType.SHARDS) {
            resolved = resolved
                    .replace("{amount} ѕʜᴀʀᴅѕ", "{price_formatted}")
                    .replace("{amount} ѕʜᴀʀᴅѕ", "{price_formatted}")
                    .replace("%amount% ѕʜᴀʀᴅѕ", "{price_formatted}")
                    .replace("${amount} ѕʜᴀʀᴅѕ", "{price_formatted}");
        }
        return resolved
                .replace("{amount}", amount)
                .replace("${amount}", formattedPrice)
                .replace("%amount%", amount)
                .replace("{price_formatted}", formattedPrice)
                .replace("{currency}", formattedPrice)
                .replace("{currency_name}", plugin.getCurrencyManager().name(currencyType, totalPrice))
                .replace("{currency_name_singular}", plugin.getCurrencyManager().singular(currencyType))
                .replace("{currency_name_plural}", plugin.getCurrencyManager().plural(currencyType))
                .replace("{item-name}", resolveItemName())
                .replace("{quantity}", String.valueOf(quantity));
    }

    private String replaceCommonPlaceholders(String text) {
        if (text == null) {
            return "";
        }

        CurrencyManager.CurrencyType currencyType = currencyType();
        double totalPrice = item.currency() == ShopManager.Currency.SHARD
                ? Math.round(item.pricePerUnit() * quantity)
                : item.pricePerUnit() * quantity;
        String amount = plugin.getCurrencyManager().formatAmount(currencyType, totalPrice);
        String formattedPrice = plugin.getCurrencyManager().format(currencyType, totalPrice);
        String resolved = text;
        if (currencyType == CurrencyManager.CurrencyType.SHARDS) {
            resolved = resolved
                    .replace("${price}x &lѕʜᴀʀᴅѕ", "{price_formatted}")
                    .replace("${price}x ѕʜᴀʀᴅѕ", "{price_formatted}")
                    .replace("${price} ѕʜᴀʀᴅѕ", "{price_formatted}")
                    .replace("{price} ѕʜᴀʀᴅѕ", "{price_formatted}")
                    .replace("%price% ѕʜᴀʀᴅѕ", "{price_formatted}");
        }
        return resolved
                .replace("${price}", formattedPrice)
                .replace("%price%", amount)
                .replace("{price}", amount)
                .replace("{price_formatted}", formattedPrice)
                .replace("{currency}", formattedPrice)
                .replace("{currency_name}", plugin.getCurrencyManager().name(currencyType, totalPrice))
                .replace("{currency_name_singular}", plugin.getCurrencyManager().singular(currencyType))
                .replace("{currency_name_plural}", plugin.getCurrencyManager().plural(currencyType))
                .replace("%quantity%", String.valueOf(quantity))
                .replace("{quantity}", String.valueOf(quantity))
                .replace("{item-name}", resolveItemName())
                .replace("{item_name}", resolveItemName());
    }

    private CurrencyManager.CurrencyType currencyType() {
        return item.currency() == ShopManager.Currency.SHARD
                ? CurrencyManager.CurrencyType.SHARDS
                : CurrencyManager.CurrencyType.MONEY;
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
