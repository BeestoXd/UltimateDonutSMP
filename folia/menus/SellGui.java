package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.AuctionCategory;
import com.bx.ultimateDonutSmp.models.PlayerPreference;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class SellGui extends BaseMenu {

    private final ItemStack escrowItem;
    private final double price;
    private final PlayerPreference preference;
    private int selectedDurationIndex;
    private AuctionCategory selectedCategory;
    private boolean submitted;

    public SellGui(UltimateDonutSmp plugin, ItemStack item, double price) {
        this(plugin, item, price, new PlayerPreference(new java.util.UUID(0L, 0L)));
    }

    public SellGui(
            UltimateDonutSmp plugin,
            ItemStack item,
            double price,
            PlayerPreference preference
    ) {
        super(plugin, plugin.getConfigManager().getAuctionHouse()
                .getString("GUI.SELL.TITLE", "&8ѕᴇʟʟ ɪᴛᴇᴍ"), 45);
        this.escrowItem = item.clone();
        this.price = price;
        this.preference = preference;
        List<Integer> durations = plugin.getAuctionHouseManager().getAllowedDurations();
        int preferredIndex = durations.indexOf(preference.lastDurationHours());
        this.selectedDurationIndex = preferredIndex < 0 ? 0 : preferredIndex;
        this.selectedCategory = AuctionCategory.from(preference.lastCategory());
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);
        List<Integer> durations = plugin.getAuctionHouseManager().getAllowedDurations();

        set(AuctionHouseMenuSupport.slot(plugin, "GUI.SELL.ITEM", 4), escrowItem.clone());
        set(AuctionHouseMenuSupport.slot(plugin, "GUI.SELL.SUMMARY", 13),
                AuctionHouseMenuSupport.control(
                        plugin,
                        "GUI.SELL.SUMMARY",
                        Material.GOLD_INGOT,
                        "&6ʟɪѕᴛɪɴɢ ѕᴜᴍᴍᴀʀʏ",
                        List.of(
                                "&7ᴘʀɪᴄᴇ: {price}",
                                "&7ᴅᴜʀᴀᴛɪᴏɴ: &f{duration}",
                                "&7ᴄᴀᴛᴇɢᴏʀʏ: &f{category}"
                        ),
                        "{price}", plugin.getCurrencyManager().formatMoney(price),
                        "{duration}", plugin.getAuctionHouseManager().formatDuration(durations.get(selectedDurationIndex)),
                        "{category}", plugin.getAuctionHouseManager().getCategoryDisplayName(selectedCategory)
                ));

        int durationStart = plugin.getConfigManager().getAuctionHouse()
                .getInt("GUI.SELL.DURATION_START_SLOT", 18);
        for (int index = 0; index < durations.size() && index < 6; index++) {
            int hours = durations.get(index);
            boolean selected = index == selectedDurationIndex;
            set(durationStart + index, AuctionHouseMenuSupport.control(
                    plugin,
                    "GUI.SELL.DURATION",
                    selected ? Material.LIME_STAINED_GLASS_PANE : Material.GRAY_STAINED_GLASS_PANE,
                    (selected ? "&a" : "&f") + "{duration}",
                    List.of("{state}"),
                    "{duration}", plugin.getAuctionHouseManager().formatDuration(hours),
                    "{state}", selected
                            ? AuctionHouseMenuSupport.configText(
                                    plugin,
                                    "GUI.TEXT.SELECTED.NAME",
                                    "&aѕᴇʟᴇᴄᴛᴇᴅ"
                            )
                            : AuctionHouseMenuSupport.configText(
                                    plugin,
                                    "GUI.TEXT.CLICK_TO_SELECT.NAME",
                                    "&7ᴄʟɪᴄᴋ ᴛᴏ ѕᴇʟᴇᴄᴛ"
                            )
            ));
        }

        AuctionCategory[] categories = AuctionCategory.values();
        int categoryStart = plugin.getConfigManager().getAuctionHouse()
                .getInt("GUI.SELL.CATEGORY_START_SLOT", 27);
        for (int index = 0; index < categories.length; index++) {
            AuctionCategory category = categories[index];
            boolean selected = category == selectedCategory;
            set(categoryStart + index, AuctionHouseMenuSupport.control(
                    plugin,
                    "GUI.SELL.CATEGORY",
                    selected
                            ? Material.LIME_STAINED_GLASS_PANE
                            : plugin.getAuctionHouseManager().getCategoryIcon(category),
                    (selected ? "&a" : "&f") + "{category}",
                    List.of("{state}"),
                    "{category}", plugin.getAuctionHouseManager().getCategoryDisplayName(category),
                    "{state}", selected
                            ? AuctionHouseMenuSupport.configText(
                                    plugin,
                                    "GUI.TEXT.SELECTED.NAME",
                                    "&aѕᴇʟᴇᴄᴛᴇᴅ"
                            )
                            : AuctionHouseMenuSupport.configText(
                                    plugin,
                                    "GUI.TEXT.CLICK_TO_SELECT.NAME",
                                    "&7ᴄʟɪᴄᴋ ᴛᴏ ѕᴇʟᴇᴄᴛ"
                            )
            ));
        }

        set(AuctionHouseMenuSupport.slot(plugin, "GUI.SELL.CONFIRM", 40),
                AuctionHouseMenuSupport.control(
                        plugin,
                        "GUI.SELL.CONFIRM",
                        Material.LIME_STAINED_GLASS_PANE,
                        "&aᴄᴏɴꜰɪʀᴍ ʟɪѕᴛɪɴɢ",
                        List.of("&7ᴘʀɪᴄᴇ: {price}", "&7ᴅᴜʀᴀᴛɪᴏɴ: &f{duration}", "&eᴄʟɪᴄᴋ ᴛᴏ ʟɪѕᴛ"),
                        "{price}", plugin.getCurrencyManager().formatMoney(price),
                        "{duration}", plugin.getAuctionHouseManager().formatDuration(durations.get(selectedDurationIndex))
                ));
        set(AuctionHouseMenuSupport.slot(plugin, "GUI.SELL.CANCEL", 44),
                AuctionHouseMenuSupport.control(
                        plugin,
                        "GUI.SELL.CANCEL",
                        Material.RED_STAINED_GLASS_PANE,
                        "&cᴄᴀɴᴄᴇʟ",
                        List.of("&7ʀᴇᴛᴜʀɴ ᴛʜᴇ ɪᴛᴇᴍ ᴡɪᴛʜᴏᴜᴛ ʟɪѕᴛɪɴɢ")
                ));
    }

    @Override
    public void handleClick(int slot, Player player, ClickType clickType) {
        List<Integer> durations = plugin.getAuctionHouseManager().getAllowedDurations();
        int durationStart = plugin.getConfigManager().getAuctionHouse()
                .getInt("GUI.SELL.DURATION_START_SLOT", 18);
        int categoryStart = plugin.getConfigManager().getAuctionHouse()
                .getInt("GUI.SELL.CATEGORY_START_SLOT", 27);

        if (slot >= durationStart && slot < durationStart + Math.min(6, durations.size())) {
            selectedDurationIndex = slot - durationStart;
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            build(player);
            return;
        }
        if (slot >= categoryStart && slot < categoryStart + AuctionCategory.values().length) {
            selectedCategory = AuctionCategory.values()[slot - categoryStart];
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            build(player);
            return;
        }
        if (slot == AuctionHouseMenuSupport.slot(plugin, "GUI.SELL.CANCEL", 44)) {
            submitted = true;
            plugin.getAuctionHouseManager().returnEscrow(player, escrowItem);
            plugin.getAuctionHouseManager().startNavigating(player.getUniqueId());
            new AuctionHouseBrowseMenu(
                    plugin,
                    1,
                    plugin.getAuctionHouseManager().getDefaultSort()
            ).open(player);
            return;
        }
        if (slot != AuctionHouseMenuSupport.slot(plugin, "GUI.SELL.CONFIRM", 40) || submitted) {
            return;
        }

        submitted = true;
        int duration = durations.get(selectedDurationIndex);
        preference.lastDurationHours(duration);
        preference.lastCategory(selectedCategory.name());
        preference.lastPrice(price);
        plugin.getAuctionHouseManager().savePreference(preference);
        SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));

        plugin.getAuctionHouseManager().createListingFromItem(
                player,
                escrowItem,
                price,
                duration,
                selectedCategory
        ).thenAccept(result -> plugin.getFoliaScheduler().runEntity(player, () -> {
            if (result.success()) {
                player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage(
                        "AUCTION_HOUSE.LISTING_CREATED",
                        "{listing_id}", String.valueOf(result.listing().id()),
                        "{item}", plugin.getAuctionHouseManager().describeItem(result.listing().item()),
                        "{price}", NumberUtils.format(result.listing().price()),
                        "{price_formatted}", plugin.getCurrencyManager().formatMoney(result.listing().price()),
                        "{fee}", NumberUtils.format(result.listingFee()),
                        "{fee_formatted}", plugin.getCurrencyManager().formatMoney(result.listingFee()),
                        "{expires}", plugin.getAuctionHouseManager().formatRemaining(
                                result.listing().secondsRemaining(System.currentTimeMillis())
                        )
                )));
                SoundUtils.play(player, plugin.getConfigManager().getSound("AUCTION_HOUSE.SUCCESS"));
                plugin.getAuctionHouseManager().startNavigating(player.getUniqueId());
                new PlayerAuctionGui(plugin, 1).open(player);
                return;
            }

            String key = switch (result.reason()) {
                case DISABLED -> "AUCTION_HOUSE.DISABLED";
                case NO_PERMISSION -> "AUCTION_HOUSE.NO_PERMISSION";
                case NO_ITEM -> "AUCTION_HOUSE.NO_ITEM_IN_HAND";
                case INVALID_ITEM, UNSAFE_ITEM -> "AUCTION_HOUSE.ITEM_BLOCKED";
                case INVALID_PRICE -> "AUCTION_HOUSE.PRICE_OUT_OF_RANGE";
                case INVALID_DURATION -> "AUCTION_HOUSE.INVALID_DURATION";
                case NO_MONEY -> "AUCTION_HOUSE.NO_MONEY_FOR_FEE";
                case MAX_LISTINGS_REACHED -> "AUCTION_HOUSE.MAX_LISTINGS_REACHED";
                case NO_PLAYER_DATA, DATABASE_ERROR -> "AUCTION_HOUSE.CREATE_DATABASE_ERROR";
            };
            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage(key)));
            SoundUtils.play(player, plugin.getConfigManager().getSound("AUCTION_HOUSE.FAIL"));
            plugin.getAuctionHouseManager().startNavigating(player.getUniqueId());
            new AuctionHouseBrowseMenu(
                    plugin,
                    1,
                    plugin.getAuctionHouseManager().getDefaultSort()
            ).open(player);
        }));
    }

    @Override
    public void onClose(Player player) {
        if (!submitted) {
            submitted = true;
            plugin.getAuctionHouseManager().returnEscrow(player, escrowItem);
        }
        plugin.getAuctionHouseManager().stopNavigating(player.getUniqueId());
    }
}
