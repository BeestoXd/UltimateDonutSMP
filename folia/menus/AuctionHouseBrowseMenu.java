package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.AuctionHouseManager;
import com.bx.ultimateDonutSmp.models.AuctionBrowseRequest;
import com.bx.ultimateDonutSmp.models.AuctionCategory;
import com.bx.ultimateDonutSmp.models.AuctionListing;
import com.bx.ultimateDonutSmp.models.AuctionPage;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import com.bx.ultimateDonutSmp.utils.PermissionUtils;
import com.bx.ultimateDonutSmp.utils.ShulkerBoxSupport;
import com.bx.ultimateDonutSmp.utils.SignInputUtil;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class AuctionHouseBrowseMenu extends BaseMenu {

    private final AuctionBrowseRequest request;
    private final Map<Integer, AuctionListing> slotMappings = new HashMap<>();
    private AuctionPage renderedPage = new AuctionPage(List.of(), 1, 1, 0);

    public AuctionHouseBrowseMenu(
            UltimateDonutSmp plugin,
            int page,
            AuctionHouseManager.AuctionSort sort
    ) {
        this(plugin, page, sort, "ALL");
    }

    public AuctionHouseBrowseMenu(
            UltimateDonutSmp plugin,
            int page,
            AuctionHouseManager.AuctionSort sort,
            String category
    ) {
        super(plugin, plugin.getAuctionHouseManager().getBrowseTitle(), 54);
        this.request = new AuctionBrowseRequest(page, sort, AuctionCategory.from(category), "");
    }

    private AuctionHouseBrowseMenu(UltimateDonutSmp plugin, AuctionBrowseRequest request) {
        super(plugin, plugin.getAuctionHouseManager().getBrowseTitle(), 54);
        this.request = request;
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);
        slotMappings.clear();

        AuctionHouseManager manager = plugin.getAuctionHouseManager();
        AuctionBrowseRequest effective = new AuctionBrowseRequest(
                request.page(),
                request.sort(),
                request.category(),
                manager.getSearchQuery(player.getUniqueId())
        );
        manager.updateSession(player.getUniqueId(), effective);
        renderedPage = manager.browse(effective);

        int slot = 0;
        for (AuctionListing listing : renderedPage.listings()) {
            ItemStack display = AuctionHouseMenuSupport.createListingDisplay(
                    plugin,
                    manager,
                    listing,
                    listing.sellerUuid().equals(player.getUniqueId())
            );
            if (ShulkerBoxSupport.isShulkerBox(display)) {
                ItemMeta meta = display.getItemMeta();
                if (meta != null) {
                    List<String> lore = meta.hasLore() && meta.getLore() != null
                            ? new ArrayList<>(meta.getLore())
                            : new ArrayList<>();
                    lore.add(ColorUtils.colorize(AuctionHouseMenuSupport.configText(
                            plugin,
                            "GUI.BROWSE.SHULKER.ITEM_COUNT_LORE",
                            "&bɪᴛᴇᴍѕ ɪɴѕɪᴅᴇ: &3{count}",
                            "{count}", String.valueOf(ShulkerBoxSupport.getItemCount(display))
                    )));
                    lore.add(ColorUtils.colorize(AuctionHouseMenuSupport.configText(
                            plugin,
                            "GUI.BROWSE.SHULKER.PREVIEW_LORE",
                            "&8ʀɪɢʜᴛ-ᴄʟɪᴄᴋ ᴛᴏ ᴘʀᴇᴠɪᴇᴡ"
                    )));
                    meta.setLore(lore);
                    display.setItemMeta(meta);
                }
            }
            set(slot, display);
            slotMappings.put(slot, listing);
            slot++;
        }

        set(controlSlot("PREVIOUS", 45), renderedPage.hasPrevious()
                ? control("PREVIOUS", Material.ARROW, "&fᴘʀᴇᴠɪᴏᴜѕ ᴘᴀɢᴇ",
                List.of("&7ɢᴏ ᴛᴏ ᴘᴀɢᴇ &f{page}"), "{page}", String.valueOf(renderedPage.page() - 1))
                : control("FILLER", Material.BLACK_STAINED_GLASS_PANE, "&7 ", List.of()));
        set(controlSlot("SORT", 47), control(
                "SORT",
                Material.CAULDRON,
                "&fᴘʀɪᴄᴇ ѕᴏʀᴛ",
                List.of("&7ᴄᴜʀʀᴇɴᴛ: &e{sort}", "&8ᴄʟɪᴄᴋ ᴛᴏ ᴄʏᴄʟᴇ ѕᴏʀᴛɪɴɢ"),
                "{sort}", manager.getSortDisplayName(effective.sort())
        ));
        set(controlSlot("FILTER", 48), control(
                "FILTER",
                Material.HOPPER,
                "&fꜰɪʟᴛᴇʀ",
                List.of("&7ᴄᴀᴛᴇɢᴏʀʏ: &e{category}", "&8ᴄʟɪᴄᴋ ᴛᴏ ᴄʜᴀɴɢᴇ ᴄᴀᴛᴇɢᴏʀʏ"),
                "{category}", manager.getCategoryDisplayName(effective.category())
        ));
        set(controlSlot("REFRESH", 49), control(
                "REFRESH",
                Material.ANVIL,
                "&fᴀᴜᴄᴛɪᴏɴ",
                List.of("&7ʀᴇꜰʀᴇѕʜ ᴛʜᴇ ᴀᴜᴄᴛɪᴏɴ ʜᴏᴜѕᴇ")
        ));
        set(controlSlot("SEARCH", 50), control(
                "SEARCH",
                Material.OAK_SIGN,
                "&fѕᴇᴀʀᴄʜ",
                List.of("&7ᴄᴜʀʀᴇɴᴛ: &e{search}", "&8ʟᴇꜰᴛ-ᴄʟɪᴄᴋ ᴛᴏ ѕᴇᴀʀᴄʜ", "&8ʀɪɢʜᴛ-ᴄʟɪᴄᴋ ᴛᴏ ᴄʟᴇᴀʀ"),
                "{search}", effective.search().isBlank()
                        ? AuctionHouseMenuSupport.configText(
                                plugin,
                                "GUI.TEXT.NONE.NAME",
                                "none"
                        )
                        : effective.search()
        ));
        set(controlSlot("PLAYER_ITEMS", 51), control(
                "PLAYER_ITEMS",
                Material.CHEST,
                "&fʏᴏᴜʀ ɪᴛᴇᴍѕ",
                List.of("&7ᴠɪᴇᴡ ᴀᴄᴛɪᴠᴇ, ѕᴏʟᴅ, ᴇxᴘɪʀᴇᴅ, ᴀɴᴅ ᴄᴀɴᴄᴇʟʟᴇᴅ ʟɪѕᴛɪɴɢѕ")
        ));
        set(controlSlot("NEXT", 53), renderedPage.hasNext()
                ? control("NEXT", Material.ARROW, "&fɴᴇxᴛ ᴘᴀɢᴇ",
                List.of("&7ɢᴏ ᴛᴏ ᴘᴀɢᴇ &f{page}"), "{page}", String.valueOf(renderedPage.page() + 1))
                : control("FILLER", Material.BLACK_STAINED_GLASS_PANE, "&7 ", List.of()));
    }

    @Override
    public void handleClick(int slot, Player player, ClickType clickType) {
        AuctionHouseManager manager = plugin.getAuctionHouseManager();
        AuctionBrowseRequest current = manager.session(player.getUniqueId()).request();
        AuctionListing listing = slotMappings.get(slot);
        if (listing != null) {
            if (clickType.isRightClick() && ShulkerBoxSupport.isShulkerBox(listing.item())) {
                navigate(player, () -> new ShulkerPreviewGui(plugin, listing.item(), current).open(player));
                return;
            }
            manager.getPreferenceAsync(player.getUniqueId()).thenAccept(preference ->
                    plugin.getFoliaScheduler().runEntity(player, () -> {
                        boolean fastBuy = preference.fastBuyEnabled()
                                && (PermissionUtils.has(player, "ultimatedonutsmp.auctionhouse.fastbuy")
                                || PermissionUtils.has(player, "donutauction.fastbuy"));
                        if (fastBuy) {
                            purchase(player, listing, current);
                        } else {
                            navigate(player, () -> new ConfirmPurchaseGui(plugin, listing, current).open(player));
                        }
                    })
            );
            return;
        }

        if (slot == controlSlot("PREVIOUS", 45) && renderedPage.hasPrevious()) {
            open(player, current.withPage(renderedPage.page() - 1), true);
        } else if (slot == controlSlot("SORT", 47)) {
            open(player, current.withSort(nextSort(current.sort())), true);
        } else if (slot == controlSlot("FILTER", 48)) {
            navigate(player, () -> new FilterGui(plugin, current).open(player));
        } else if (slot == controlSlot("REFRESH", 49)) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            manager.refreshCache().thenRun(() -> plugin.getFoliaScheduler().runEntity(
                    player,
                    () -> open(player, current, true)
            ));
        } else if (slot == controlSlot("SEARCH", 50)) {
            if (clickType.isRightClick()) {
                manager.clearSearchQuery(player.getUniqueId());
                open(player, current.withSearch(""), true);
                return;
            }
            var signConfig = plugin.getConfigManager().getAuctionHouse()
                    .getConfigurationSection("GUI.BROWSE.SEARCH_SIGN");
            manager.startNavigating(player.getUniqueId());
            SignInputUtil.openFromConfig(plugin, player, signConfig, text -> {
                String search = text == null || text.isBlank() || text.equalsIgnoreCase("cancel")
                        ? current.search()
                        : text.trim();
                if (search.length() > 64) {
                    search = search.substring(0, 64);
                }
                manager.setSearchQuery(player.getUniqueId(), search);
                open(player, current.withSearch(search), false);
            });
        } else if (slot == controlSlot("PLAYER_ITEMS", 51)) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            navigate(player, () -> new PlayerAuctionGui(plugin, 1).open(player));
        } else if (slot == controlSlot("NEXT", 53) && renderedPage.hasNext()) {
            open(player, current.withPage(renderedPage.page() + 1), true);
        }
    }

    private void purchase(Player player, AuctionListing listing, AuctionBrowseRequest current) {
        plugin.getAuctionHouseManager().purchaseListing(player, listing.id())
                .thenAccept(result -> plugin.getFoliaScheduler().runEntity(player, () -> {
                    if (result.success()) {
                        player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage(
                                "AUCTION_HOUSE.PURCHASE_SUCCESS",
                                "{item}", plugin.getAuctionHouseManager().describeItem(listing.item()),
                                "{price}", NumberUtils.format(listing.price()),
                                "{price_formatted}", plugin.getCurrencyManager().formatMoney(listing.price()),
                                "{seller}", listing.sellerName()
                        )));
                        SoundUtils.play(player, plugin.getConfigManager().getSound("AUCTION_HOUSE.SUCCESS"));
                    } else {
                        String key = switch (result.reason()) {
                            case DISABLED -> "AUCTION_HOUSE.DISABLED";
                            case NO_PERMISSION -> "AUCTION_HOUSE.NO_PERMISSION";
                            case LISTING_NOT_FOUND -> "AUCTION_HOUSE.LISTING_NOT_FOUND";
                            case NOT_ACTIVE -> "AUCTION_HOUSE.LISTING_NOT_ACTIVE";
                            case OWN_LISTING -> "AUCTION_HOUSE.CANNOT_BUY_OWN";
                            case NO_MONEY -> "AUCTION_HOUSE.NOT_ENOUGH_MONEY";
                            case INVENTORY_FULL -> "AUCTION_HOUSE.FULL_INVENTORY";
                            case NO_PLAYER_DATA, DATABASE_ERROR -> "AUCTION_HOUSE.PURCHASE_DATABASE_ERROR";
                        };
                        player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage(key)));
                        SoundUtils.play(player, plugin.getConfigManager().getSound("AUCTION_HOUSE.FAIL"));
                    }
                    open(player, current, true);
                }));
    }

    private void open(Player player, AuctionBrowseRequest next, boolean sound) {
        if (sound) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
        }
        plugin.getAuctionHouseManager().updateSession(player.getUniqueId(), next);
        navigate(player, () -> new AuctionHouseBrowseMenu(plugin, next).open(player));
    }

    private void navigate(Player player, Runnable action) {
        plugin.getAuctionHouseManager().startNavigating(player.getUniqueId());
        action.run();
    }

    private AuctionHouseManager.AuctionSort nextSort(AuctionHouseManager.AuctionSort current) {
        List<AuctionHouseManager.AuctionSort> sorts = plugin.getAuctionHouseManager().getAllowedSorts();
        int index = sorts.indexOf(current);
        return index < 0 ? plugin.getAuctionHouseManager().getDefaultSort() : sorts.get((index + 1) % sorts.size());
    }

    private int controlSlot(String key, int fallback) {
        return AuctionHouseMenuSupport.slot(plugin, "GUI.BROWSE.CONTROLS." + key, fallback);
    }

    private ItemStack control(
            String key,
            Material material,
            String name,
            List<String> lore,
            String... replacements
    ) {
        return AuctionHouseMenuSupport.control(
                plugin,
                "GUI.BROWSE.CONTROLS." + key,
                material,
                name,
                lore,
                replacements
        );
    }

    @Override
    public void onClose(Player player) {
        plugin.getAuctionHouseManager().stopNavigating(player.getUniqueId());
    }
}
