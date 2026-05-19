package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.OrdersManager;
import com.bx.ultimateDonutSmp.models.Order;
import com.bx.ultimateDonutSmp.models.OrderCollectionClaim;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

final class OrdersMenuSupport {

    private OrdersMenuSupport() {
    }

    static ItemStack createOrderDisplay(
            UltimateDonutSmp plugin,
            OrdersManager manager,
            Order order,
            boolean ownedByViewer
    ) {
        List<String> extraLore = new ArrayList<>();
        extraLore.add("");
        extraLore.add("&7ᴏᴡɴᴇʀ: &f" + order.ownerName());
        extraLore.add("&7ѕᴛᴀᴛᴜѕ: &f" + order.status().name());
        extraLore.add("&7ᴘʀᴏɢʀᴇѕѕ: &e" + order.deliveredQuantity() + "&7/&e" + order.requestedQuantity());
        extraLore.add("&7ᴘʀɪᴄᴇ ᴇᴀᴄʜ: " + plugin.getCurrencyManager().formatMoney(order.priceEach()));
        extraLore.add("&7ᴘᴀɪᴅ ѕᴏ ꜰᴀʀ: " + plugin.getCurrencyManager().formatMoney(order.paidAmount()));
        extraLore.add("&7ᴇѕᴄʀᴏᴡ ʟᴇꜰᴛ: " + plugin.getCurrencyManager().formatMoney(order.escrowRemaining()));
        extraLore.add("&7ᴛɪᴍᴇ ʟᴇꜰᴛ: &f" + manager.formatRemaining(order.secondsRemaining(System.currentTimeMillis())));
        extraLore.add("&7ᴏʀᴅᴇʀ ID: &f#" + order.id());
        extraLore.add("");
        extraLore.add(ownedByViewer ? "&eᴄʟɪᴄᴋ ᴛᴏ ᴍᴀɴᴀɢᴇ ᴏʀᴅᴇʀ" : "&eᴄʟɪᴄᴋ ᴛᴏ ᴠɪᴇᴡ ᴅᴇʟɪᴠᴇʀʏ ᴏᴘᴛɪᴏɴѕ");
        return decorateItem(plugin, order.requestedItem(), manager.describeItem(order.requestedItem()), extraLore);
    }

    static ItemStack createClaimDisplay(
            UltimateDonutSmp plugin,
            OrdersManager manager,
            OrderCollectionClaim claim
    ) {
        if (claim.refundClaim()) {
            return ItemUtils.createItem(
                    Material.SUNFLOWER,
                    "&aᴇѕᴄʀᴏᴡ ʀᴇꜰᴜɴᴅ",
                    List.of(
                            "&7ᴀᴍᴏᴜɴᴛ: " + plugin.getCurrencyManager().formatMoney(claim.moneyAmount()),
                            "&7ᴏʀᴅᴇʀ: &f#" + claim.orderId(),
                            "",
                            "&eᴄʟɪᴄᴋ ᴛᴏ ᴄʟᴀɪᴍ"
                    )
            );
        }

        List<String> extraLore = new ArrayList<>();
        extraLore.add("");
        extraLore.add("&7ᴄʟᴀɪᴍ ᴛʏᴘᴇ: &fᴅᴇʟɪᴠᴇʀᴇᴅ ɪᴛᴇᴍ");
        extraLore.add("&7ᴏʀᴅᴇʀ: &f#" + claim.orderId());
        extraLore.add("&7ᴄʀᴇᴀᴛᴇᴅ: &f" + NumberUtils.formatTimeLong(Math.max(0L,
                (System.currentTimeMillis() - claim.createdAt()) / 1000L)));
        extraLore.add("");
        extraLore.add("&eᴄʟɪᴄᴋ ᴛᴏ ᴄʟᴀɪᴍ");
        return decorateItem(plugin, claim.item(), manager.describeItem(claim.item()), extraLore);
    }

    static ItemStack decorateItem(
            UltimateDonutSmp plugin,
            ItemStack source,
            String fallbackDisplayName,
            List<String> extraLore
    ) {
        if (source == null || source.getType().isAir()) {
            return ItemUtils.createItem(Material.BARRIER, "&cᴍɪѕѕɪɴɢ ɪᴛᴇᴍ", List.of("&7ᴛʜɪѕ ᴇɴᴛʀʏ ʜᴀѕ ɴᴏ ɪᴛᴇᴍ ᴅᴀᴛᴀ."));
        }

        ItemStack display = source.clone();
        ItemMeta meta = display.getItemMeta();
        if (meta == null) {
            return display;
        }

        List<String> combinedLore = new ArrayList<>();
        if (meta.hasLore() && meta.getLore() != null) {
            for (String line : meta.getLore()) {
                combinedLore.add(ColorUtils.toLegacyString(line));
            }
        }
        combinedLore.addAll(extraLore);

        if (!meta.hasDisplayName() && fallbackDisplayName != null && !fallbackDisplayName.isBlank()) {
            meta.setDisplayName(ColorUtils.toComponent("&b" + fallbackDisplayName));
        }
        meta.setLore(ColorUtils.toComponentList(combinedLore));
        display.setItemMeta(meta);
        return display;
    }
}
