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
        extraLore.add("&7Owner: &f" + order.ownerName());
        extraLore.add("&7Status: &f" + order.status().name());
        extraLore.add("&7Progress: &e" + order.deliveredQuantity() + "&7/&e" + order.requestedQuantity());
        extraLore.add("&7Price Each: &a$" + NumberUtils.format(order.priceEach()));
        extraLore.add("&7Paid So Far: &a$" + NumberUtils.format(order.paidAmount()));
        extraLore.add("&7Escrow Left: &a$" + NumberUtils.format(order.escrowRemaining()));
        extraLore.add("&7Time Left: &f" + manager.formatRemaining(order.secondsRemaining(System.currentTimeMillis())));
        extraLore.add("&7Order ID: &f#" + order.id());
        extraLore.add("");
        extraLore.add(ownedByViewer ? "&eClick to manage order" : "&eClick to view delivery options");
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
                    "&aEscrow Refund",
                    List.of(
                            "&7Amount: &a$" + NumberUtils.format(claim.moneyAmount()),
                            "&7Order: &f#" + claim.orderId(),
                            "",
                            "&eClick to claim"
                    )
            );
        }

        List<String> extraLore = new ArrayList<>();
        extraLore.add("");
        extraLore.add("&7Claim Type: &fDelivered Item");
        extraLore.add("&7Order: &f#" + claim.orderId());
        extraLore.add("&7Created: &f" + NumberUtils.formatTimeLong(Math.max(0L,
                (System.currentTimeMillis() - claim.createdAt()) / 1000L)));
        extraLore.add("");
        extraLore.add("&eClick to claim");
        return decorateItem(plugin, claim.item(), manager.describeItem(claim.item()), extraLore);
    }

    static ItemStack decorateItem(
            UltimateDonutSmp plugin,
            ItemStack source,
            String fallbackDisplayName,
            List<String> extraLore
    ) {
        if (source == null || source.getType().isAir()) {
            return ItemUtils.createItem(Material.BARRIER, "&cMissing Item", List.of("&7This entry has no item data."));
        }

        ItemStack display = source.clone();
        ItemMeta meta = display.getItemMeta();
        if (meta == null) {
            return display;
        }

        List<String> combinedLore = new ArrayList<>();
        if (meta.hasLore() && meta.lore() != null) {
            for (net.kyori.adventure.text.Component line : meta.lore()) {
                combinedLore.add(ColorUtils.toLegacyString(line));
            }
        }
        combinedLore.addAll(extraLore);

        if (!meta.hasDisplayName() && fallbackDisplayName != null && !fallbackDisplayName.isBlank()) {
            meta.displayName(ColorUtils.toComponent("&b" + fallbackDisplayName));
        }
        meta.lore(ColorUtils.toComponentList(combinedLore));
        display.setItemMeta(meta);
        return display;
    }
}
