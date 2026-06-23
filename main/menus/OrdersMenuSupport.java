package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.OrdersManager;
import com.bx.ultimateDonutSmp.models.ItemKey;
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

    static int slot(UltimateDonutSmp plugin, String path, int fallback) {
        return plugin.getConfigManager().getOrders().getInt(path, fallback);
    }

    static Material material(UltimateDonutSmp plugin, String path, Material fallback) {
        String raw = plugin.getConfigManager().getOrders().getString(path, fallback.name());
        Material material = raw == null ? null : Material.matchMaterial(raw);
        return material == null ? fallback : material;
    }

    static String text(UltimateDonutSmp plugin, String path, String fallback, String... placeholders) {
        return plugin.getLanguageManager().text(path, null, fallback, placeholders);
    }

    static List<String> list(UltimateDonutSmp plugin, String path, List<String> fallback, String... placeholders) {
        return plugin.getLanguageManager().list(path, fallback, placeholders);
    }

    static ItemStack button(
            UltimateDonutSmp plugin,
            String configPath,
            String languagePath,
            Material fallbackMaterial,
            String fallbackName,
            List<String> fallbackLore,
            String... placeholders
    ) {
        return ItemUtils.createItem(
                material(plugin, configPath + ".MATERIAL", fallbackMaterial),
                text(plugin, languagePath + ".NAME", fallbackName, placeholders),
                list(plugin, languagePath + ".LORE", fallbackLore, placeholders)
        );
    }

    static ItemStack createOrderDisplay(
            UltimateDonutSmp plugin,
            OrdersManager manager,
            Order order,
            boolean ownedByViewer
    ) {
        List<String> lore = list(
                plugin,
                "ORDERS.GUI.ORDER_ITEM.LORE",
                List.of(
                        "&f{item}",
                        "&a{price_each} &fᴇᴀᴄʜ",
                        "",
                        "&e{delivered}&7/&a{requested} &7ᴅᴇʟɪᴠᴇʀᴇᴅ",
                        "&e{paid}&7/&a{total} &7ᴘᴀɪᴅ",
                        "",
                        ownedByViewer ? "&fᴄʟɪᴄᴋ ᴛᴏ ᴍᴀɴᴀɢᴇ ᴛʜɪѕ ᴏʀᴅᴇʀ" : "&fᴄʟɪᴄᴋ ᴛᴏ ᴅᴇʟɪᴠᴇʀ ᴛʜɪѕ ɪᴛᴇᴍ"
                ),
                "{item}", manager.describeItem(order.requestedItem()),
                "{owner}", order.ownerName(),
                "{price_each}", plugin.getCurrencyManager().formatMoney(order.priceEach()),
                "{delivered}", String.valueOf(order.deliveredQuantity()),
                "{requested}", String.valueOf(order.requestedQuantity()),
                "{paid}", plugin.getCurrencyManager().formatMoney(order.paidAmount()),
                "{total}", plugin.getCurrencyManager().formatMoney(order.totalBudget()),
                "{remaining}", String.valueOf(order.remainingQuantity()),
                "{time}", manager.formatRemaining(order.secondsRemaining(System.currentTimeMillis())),
                "{status}", plugin.getLanguageManager().display("ORDER_STATUSES", order.status().name(), order.status().name()),
                "{order_id}", String.valueOf(order.id())
        );
        String name = text(
                plugin,
                "ORDERS.GUI.ORDER_ITEM.NAME",
                "&a{owner}'ѕ ᴏʀᴅᴇʀ",
                "{owner}", order.ownerName(),
                "{item}", manager.describeItem(order.requestedItem())
        );
        return decorateItem(plugin, order.requestedItem(), name, lore, false);
    }

    static ItemStack createClaimDisplay(
            UltimateDonutSmp plugin,
            OrdersManager manager,
            OrderCollectionClaim claim
    ) {
        if (claim.refundClaim()) {
            return ItemUtils.createItem(
                    Material.SUNFLOWER,
                    text(plugin, "ORDERS.GUI.CLAIM.REFUND_NAME", "&aᴇѕᴄʀᴏᴡ ʀᴇꜰᴜɴᴅ"),
                    list(
                            plugin,
                            "ORDERS.GUI.CLAIM.REFUND_LORE",
                            List.of("&7ᴀᴍᴏᴜɴᴛ: &f{amount}", "&7ᴏʀᴅᴇʀ: &f#{order_id}", "", "&eᴄʟɪᴄᴋ ᴛᴏ ᴄᴏʟʟᴇᴄᴛ"),
                            "{amount}", plugin.getCurrencyManager().formatMoney(claim.moneyAmount()),
                            "{order_id}", String.valueOf(claim.orderId())
                    )
            );
        }

        List<String> lore = list(
                plugin,
                "ORDERS.GUI.CLAIM.ITEM_LORE",
                List.of(
                        "&7ᴏʀᴅᴇʀ: &f#{order_id}",
                        "&7ᴄʀᴇᴀᴛᴇᴅ: &f{age} ᴀɢᴏ",
                        "",
                        "&eᴄʟɪᴄᴋ ᴛᴏ ᴄᴏʟʟᴇᴄᴛ"
                ),
                "{order_id}", String.valueOf(claim.orderId()),
                "{age}", NumberUtils.formatTimeLong(Math.max(0L,
                        (System.currentTimeMillis() - claim.createdAt()) / 1000L))
        );
        return decorateItem(
                plugin,
                claim.item(),
                "&b" + manager.describeItem(claim.item()),
                lore,
                true
        );
    }

    static ItemStack decorateItem(
            UltimateDonutSmp plugin,
            ItemStack source,
            String displayName,
            List<String> extraLore,
            boolean preserveOriginalLore
    ) {
        if (source == null || source.getType().isAir()) {
            return ItemUtils.createItem(Material.BARRIER, "&cᴍɪѕѕɪɴɢ ɪᴛᴇᴍ", List.of("&7ѕᴛᴏʀᴇᴅ ɪᴛᴇᴍ ᴅᴀᴛᴀ ɪѕ ᴜɴᴀᴠᴀɪʟᴀʙʟᴇ."));
        }

        ItemStack display = source.clone();
        display.setAmount(Math.max(1, source.getAmount()));
        ItemMeta meta = display.getItemMeta();
        if (meta == null) {
            return display;
        }

        List<String> combinedLore = new ArrayList<>();
        List<String> enchantLines = ItemKey.fromStack(source).enchantLoreLines("&7- &d");
        if (!enchantLines.isEmpty()) {
            combinedLore.add(text(plugin, "ORDERS.GUI.REQUIRED_ENCHANTMENTS", "&bʀᴇǫᴜɪʀᴇᴅ ᴇɴᴄʜᴀɴᴛᴍᴇɴᴛѕ:"));
            combinedLore.addAll(enchantLines);
            combinedLore.add("");
        }
        if (preserveOriginalLore && meta.hasLore() && meta.getLore() != null) {
            for (String line : meta.getLore()) {
                combinedLore.add(ColorUtils.toLegacyString(line));
            }
        }
        combinedLore.addAll(extraLore);
        meta.setDisplayName(ColorUtils.toComponent(displayName));
        meta.setLore(ColorUtils.toComponentList(combinedLore));
        display.setItemMeta(meta);
        return display;
    }

    static ItemStack decorateItem(
            UltimateDonutSmp plugin,
            ItemStack source,
            String displayName,
            List<String> extraLore
    ) {
        return decorateItem(plugin, source, displayName, extraLore, true);
    }

    static String tr(String value) {
        return value;
    }
}
