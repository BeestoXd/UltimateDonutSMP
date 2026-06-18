package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PlayerPreference;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class SellGui extends BaseMenu {

    private final ItemStack itemToSell;
    private final double price;

    private int selectedDurationIndex = 3; // default: 48 Hours
    private String selectedCategory = "ALL";
    private boolean preferenceLoaded = false;
    private boolean confirmed = false;

    private static final int[] DURATION_OPTIONS = { 6, 12, 24, 48, 72, 168 };
    private static final String[] DURATION_LABELS = {
            "6 Hours", "12 Hours", "1 Day", "2 Days", "3 Days", "1 Week"
    };

    private static final String[] CATEGORIES = {
            "ALL", "BLOCKS", "TOOLS", "FOOD", "COMBAT", "POTIONS", "BOOKS", "INGREDIENTS", "UTILITIES"
    };

    private static final Material[] ICONS = {
            Material.COMPASS, Material.GRASS_BLOCK, Material.DIAMOND_PICKAXE,
            Material.GOLDEN_CARROT, Material.DIAMOND_SWORD, Material.POTION,
            Material.ENCHANTED_BOOK, Material.BLAZE_POWDER, Material.ENDER_CHEST
    };

    public SellGui(UltimateDonutSmp plugin, ItemStack itemToSell, double price) {
        super(plugin, "&6Sell Item", 45);
        this.itemToSell = itemToSell.clone();
        this.price = price;
    }

    @Override
    public void build(Player player) {
        if (!preferenceLoaded) {
            PlayerPreference pref = plugin.getAuctionHouseManager().getPreference(player.getUniqueId());
            if (pref != null) {
                int lastHours = pref.lastDurationHours();
                for (int i = 0; i < DURATION_OPTIONS.length; i++) {
                    if (DURATION_OPTIONS[i] == lastHours) {
                        selectedDurationIndex = i;
                        break;
                    }
                }
                selectedCategory = pref.lastCategory();
            }
            preferenceLoaded = true;
        }

        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);

        // Slot 4: Item to sell
        set(4, itemToSell.clone());

        // Slots 18-23: Durations
        for (int i = 0; i < DURATION_OPTIONS.length; i++) {
            boolean isSelected = (i == selectedDurationIndex);
            set(18 + i, ItemUtils.createItem(
                    isSelected ? Material.LIME_STAINED_GLASS_PANE : Material.GRAY_STAINED_GLASS_PANE,
                    (isSelected ? "&a" : "&f") + DURATION_LABELS[i],
                    List.of(isSelected ? "&aSelected" : "&7Click to select")
            ));
        }

        // Slots 27-35: Categories
        for (int i = 0; i < CATEGORIES.length; i++) {
            String cat = CATEGORIES[i];
            boolean isSelected = cat.equalsIgnoreCase(selectedCategory);
            set(27 + i, ItemUtils.createItem(
                    isSelected ? Material.LIME_STAINED_GLASS_PANE : ICONS[i],
                    (isSelected ? "&a" : "&f") + cat.substring(0, 1) + cat.substring(1).toLowerCase(),
                    List.of(isSelected ? "&aSelected" : "&7Click to select")
            ));
        }

        // Slot 13: Summary Ingot
        set(13, ItemUtils.createItem(
                Material.GOLD_INGOT,
                "&6Price: &e$" + NumberUtils.format(price),
                List.of("&7Duration: &f" + DURATION_LABELS[selectedDurationIndex])
        ));

        // Slot 40: Confirm Listing
        set(40, ItemUtils.createItem(
                Material.LIME_STAINED_GLASS_PANE,
                "&aConfirm Listing",
                List.of(
                        "&7Price: &a$" + NumberUtils.format(price),
                        "&7Duration: &f" + DURATION_LABELS[selectedDurationIndex]
                )
        ));

        // Slot 44: Cancel Listing
        set(44, ItemUtils.createItem(
                Material.RED_STAINED_GLASS_PANE,
                "&cCancel",
                List.of("&7Go back without listing")
        ));
    }

    @Override
    public void handleClick(int slot, Player player, ClickType clickType) {
        if (slot >= 18 && slot < 18 + DURATION_OPTIONS.length) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            selectedDurationIndex = slot - 18;
            build(player);
        } else if (slot >= 27 && slot < 27 + CATEGORIES.length) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            selectedCategory = CATEGORIES[slot - 27];
            build(player);
        } else if (slot == 40) {
            // Confirm
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            confirmed = true;

            // Update player preferences
            PlayerPreference pref = plugin.getAuctionHouseManager().getPreference(player.getUniqueId());
            if (pref != null) {
                pref.lastDurationHours(DURATION_OPTIONS[selectedDurationIndex]);
                pref.lastCategory(selectedCategory);
                pref.lastPrice(price);
                plugin.getAuctionHouseManager().savePreference(pref);
            }

            // Option A: Put item back in main hand temporarily
            player.getInventory().setItemInMainHand(itemToSell.clone());

            var result = plugin.getAuctionHouseManager().createListing(
                    player,
                    price,
                    selectedCategory,
                    DURATION_OPTIONS[selectedDurationIndex]
            );

            if (result.success()) {
                String successMsg = plugin.getConfigManager().getMessage(
                        "AUCTION_HOUSE.LISTING_CREATED",
                        "{listing_id}", String.valueOf(result.listing().id()),
                        "{item}", plugin.getAuctionHouseManager().describeItem(itemToSell),
                        "{price}", NumberUtils.format(price),
                        "{fee}", NumberUtils.format(result.listingFee()),
                        "{expires}", plugin.getAuctionHouseManager().formatRemaining(
                                result.listing().secondsRemaining(System.currentTimeMillis()) * 1000L
                        )
                );
                player.sendMessage(ColorUtils.toComponent(successMsg));
                plugin.getAuctionHouseManager().startNavigating(player.getUniqueId());
                new PlayerAuctionGui(plugin, 1).open(player);
            } else {
                // Return item back to hand/inventory since listing failed
                player.getInventory().setItemInMainHand(itemToSell.clone());
                String errorMsgKey = switch (result.reason()) {
                    case DISABLED -> "AUCTION_HOUSE.DISABLED";
                    case INVALID_PRICE -> "AUCTION_HOUSE.PRICE_OUT_OF_RANGE";
                    case NO_MONEY -> "AUCTION_HOUSE.NO_MONEY_FOR_FEE";
                    case MAX_LISTINGS_REACHED -> "AUCTION_HOUSE.MAX_LISTINGS_REACHED";
                    default -> "AUCTION_HOUSE.ITEM_BLOCKED";
                };
                player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage(errorMsgKey)));
                plugin.getAuctionHouseManager().startNavigating(player.getUniqueId());
                new AuctionHouseBrowseMenu(plugin, 1, plugin.getAuctionHouseManager().getDefaultSort()).open(player);
            }
        } else if (slot == 44) {
            // Cancel
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            confirmed = true;
            returnItem(player);
            plugin.getAuctionHouseManager().startNavigating(player.getUniqueId());
            new AuctionHouseBrowseMenu(plugin, 1, plugin.getAuctionHouseManager().getDefaultSort()).open(player);
        }
    }

    private void returnItem(Player player) {
        java.util.Map<Integer, ItemStack> leftovers = player.getInventory().addItem(itemToSell.clone());
        if (!leftovers.isEmpty()) {
            for (ItemStack leftover : leftovers.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), leftover);
            }
        }
        player.updateInventory();
    }

    @Override
    public void onClose(Player player) {
        if (!confirmed) {
            confirmed = true;
            returnItem(player);
        }
        if (!plugin.getAuctionHouseManager().stopNavigating(player.getUniqueId())) {
            plugin.getAuctionHouseManager().clearSearchQuery(player.getUniqueId());
        }
    }
}
