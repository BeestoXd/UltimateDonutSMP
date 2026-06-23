package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.AuctionHouseManager;
import com.bx.ultimateDonutSmp.models.AuctionBrowseRequest;
import com.bx.ultimateDonutSmp.models.AuctionCategory;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class FilterGui extends BaseMenu {

    private final AuctionBrowseRequest request;
    private final Map<AuctionCategory, Integer> categorySlots = new EnumMap<>(AuctionCategory.class);

    public FilterGui(UltimateDonutSmp plugin, AuctionBrowseRequest request) {
        super(plugin, plugin.getConfigManager().getAuctionHouse()
                .getString("GUI.FILTER.TITLE", "&8ᴄᴀᴛᴇɢᴏʀʏ ꜰɪʟᴛᴇʀ"), 27);
        this.request = request;
    }

    public FilterGui(
            UltimateDonutSmp plugin,
            AuctionHouseManager.AuctionSort sort,
            String category
    ) {
        this(plugin, new AuctionBrowseRequest(1, sort, AuctionCategory.from(category), ""));
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);
        categorySlots.clear();

        AuctionCategory[] categories = {
                AuctionCategory.BLOCKS,
                AuctionCategory.TOOLS,
                AuctionCategory.FOOD,
                AuctionCategory.COMBAT,
                AuctionCategory.POTIONS,
                AuctionCategory.BOOKS,
                AuctionCategory.INGREDIENTS,
                AuctionCategory.UTILITIES
        };
        int firstSlot = plugin.getConfigManager().getAuctionHouse().getInt("GUI.FILTER.FIRST_CATEGORY_SLOT", 9);
        for (int index = 0; index < categories.length; index++) {
            AuctionCategory category = categories[index];
            int slot = firstSlot + index;
            categorySlots.put(category, slot);
            boolean selected = category == request.category();
            set(slot, AuctionHouseMenuSupport.control(
                    plugin,
                    "GUI.FILTER.CATEGORY",
                    selected
                            ? Material.LIME_STAINED_GLASS_PANE
                            : plugin.getAuctionHouseManager().getCategoryIcon(category),
                    (selected ? "&a" : "&f")
                            + plugin.getAuctionHouseManager().getCategoryDisplayName(category),
                    List.of(selected ? "&aѕᴇʟᴇᴄᴛᴇᴅ" : "&7ᴄʟɪᴄᴋ ᴛᴏ ѕᴇʟᴇᴄᴛ"),
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

        set(AuctionHouseMenuSupport.slot(plugin, "GUI.FILTER.CLEAR", 22),
                AuctionHouseMenuSupport.control(
                        plugin,
                        "GUI.FILTER.CLEAR",
                        Material.BARRIER,
                        "&cᴄʟᴇᴀʀ ꜰɪʟᴛᴇʀ",
                        List.of("&7ѕʜᴏᴡ ᴀʟʟ ʟɪѕᴛɪɴɢѕ")
                ));
    }

    @Override
    public void handleClick(int slot, Player player, ClickType clickType) {
        AuctionCategory selected = categorySlots.entrySet().stream()
                .filter(entry -> entry.getValue() == slot)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
        if (slot == AuctionHouseMenuSupport.slot(plugin, "GUI.FILTER.CLEAR", 22)) {
            selected = AuctionCategory.ALL;
        }
        if (selected == null) {
            return;
        }
        SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
        AuctionBrowseRequest next = request.withCategory(selected);
        plugin.getAuctionHouseManager().updateSession(player.getUniqueId(), next);
        plugin.getAuctionHouseManager().startNavigating(player.getUniqueId());
        new AuctionHouseBrowseMenu(plugin, 1, next.sort(), next.category().name()).open(player);
    }

    @Override
    public void onClose(Player player) {
        plugin.getAuctionHouseManager().stopNavigating(player.getUniqueId());
    }
}
