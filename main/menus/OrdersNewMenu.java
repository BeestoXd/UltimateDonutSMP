package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.OrdersManager;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public class OrdersNewMenu extends BaseMenu {

    public OrdersNewMenu(UltimateDonutSmp plugin) {
        super(plugin, plugin.getOrdersManager().getNewOrderTitle(), plugin.getOrdersManager().getNewOrderSize());
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);

        OrdersManager.PendingOrderCreationSnapshot pending = plugin.getOrdersManager().getPendingCreation(player.getUniqueId());
        set(18, ItemUtils.createItem(Material.RED_STAINED_GLASS_PANE, "&cʙᴀᴄᴋ", List.of("&7ʀᴇᴛᴜʀɴ ᴛᴏ ɪᴛᴇᴍ ѕᴇʟᴇᴄᴛɪᴏɴ")));

        if (pending == null) {
            set(13, ItemUtils.createItem(
                    Material.BARRIER,
                    "&cɴᴏ ᴘᴇɴᴅɪɴɢ ᴏʀᴅᴇʀ",
                    List.of("&7ᴘɪᴄᴋ ᴀɴ ɪᴛᴇᴍ ꜰɪʀѕᴛ ᴛᴏ ᴄʀᴇᴀᴛᴇ ᴀ ɴᴇᴡ ᴏʀᴅᴇʀ.")
            ));
            return;
        }

        set(11, ItemUtils.createItem(
                Material.PAPER,
                "&bᴏʀᴅᴇʀ ᴅᴇᴛᴀɪʟѕ",
                List.of(
                        "&7ɪᴛᴇᴍ: &f" + plugin.getOrdersManager().describeMaterial(pending.entry().material()),
                        "&7ᴄᴀᴛᴇɢᴏʀʏ: &f" + plugin.getOrdersManager().prettifyCategory(pending.entry().categoryKey()),
                        "&7ǫᴜᴀɴᴛɪᴛʏ: &e" + pending.quantity(),
                        "&7ᴘʀɪᴄᴇ ᴇᴀᴄʜ: " + plugin.getCurrencyManager().formatMoney(pending.priceEach()),
                        "&7ᴛᴏᴛᴀʟ ʙᴜᴅɢᴇᴛ: " + plugin.getCurrencyManager().formatMoney(pending.totalBudget())
                )
        ));
        set(13, ItemUtils.createItem(
                pending.entry().material(),
                "&b" + plugin.getOrdersManager().describeMaterial(pending.entry().material()),
                List.of("&7ᴛʜɪѕ ɪѕ ᴛʜᴇ ɪᴛᴇᴍ ᴏᴛʜᴇʀ ᴘʟᴀʏᴇʀѕ ᴡɪʟʟ ᴅᴇʟɪᴠᴇʀ.")
        ));
        set(15, ItemUtils.createItem(
                Material.SUNFLOWER,
                "&eʙᴀʟᴀɴᴄᴇ ᴄʜᴇᴄᴋ",
                List.of(
                        "&7ᴄᴜʀʀᴇɴᴛ ʙᴀʟᴀɴᴄᴇ: " + plugin.getCurrencyManager().formatMoney(plugin.getEconomyManager().getBalance(player)),
                        "&7ᴄʀᴇᴀᴛɪᴏɴ ꜰᴇᴇ: " + plugin.getCurrencyManager().formatMoney(plugin.getConfigManager().getOrders().getDouble("PRICING.ORDER_CREATION_FEE", 0D)),
                        "&7ʀᴇǫᴜɪʀᴇᴅ: " + plugin.getCurrencyManager().formatMoney(
                                pending.totalBudget() + plugin.getConfigManager().getOrders().getDouble("PRICING.ORDER_CREATION_FEE", 0D)
                        )
                )
        ));
        set(23, ItemUtils.createItem(
                Material.LIME_DYE,
                "&aᴄᴏɴꜰɪʀᴍ ᴏʀᴅᴇʀ",
                List.of(
                        "&7ᴛʜɪѕ ᴡɪʟʟ ʟᴏᴄᴋ ʏᴏᴜʀ ʙᴜᴅɢᴇᴛ ɪɴ ᴇѕᴄʀᴏᴡ.",
                        "",
                        "&eᴄʟɪᴄᴋ ᴛᴏ ᴄʀᴇᴀᴛᴇ"
                )
        ));
    }

    @Override
    public void handleClick(int slot, Player player) {
        if (slot == 18) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            new OrdersSelectItemMenu(plugin, 1, "ALL").open(player);
            return;
        }

        if (slot != 23) {
            return;
        }

        OrdersManager manager = plugin.getOrdersManager();
        if (!manager.beginAction(player.getUniqueId())) {
            player.sendMessage(ColorUtils.toComponent("&cᴏʀᴅᴇʀѕ ɪѕ ѕᴛɪʟʟ ᴘʀᴏᴄᴇѕѕɪɴɢ ʏᴏᴜʀ ᴘʀᴇᴠɪᴏᴜѕ ᴀᴄᴛɪᴏɴ."));
            return;
        }

        try {
            if (manager.isOnClickCooldown(player.getUniqueId())) {
                player.sendMessage(ColorUtils.toComponent("&cѕʟᴏᴡ ᴅᴏᴡɴ ꜰᴏʀ ᴀ ᴍᴏᴍᴇɴᴛ."));
                return;
            }
            manager.updateClickCooldown(player.getUniqueId());

            OrdersManager.CreateOrderResult result = manager.createOrder(player);
            if (!result.success()) {
                player.sendMessage(ColorUtils.toComponent(resolveFailureMessage(result)));
                SoundUtils.play(player, plugin.getConfigManager().getSound("ORDERS.FAIL"));
                return;
            }

            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                    "ORDERS.CREATED",
                    "&aᴏʀᴅᴇʀ ᴄʀᴇᴀᴛᴇᴅ! &7#{order_id} &fꜰᴏʀ &e{quantity} {item}&7 ᴀᴛ {price_each_formatted} &7ᴇᴀᴄʜ. ʙᴜᴅɢᴇᴛ ʟᴏᴄᴋᴇᴅ: {budget_formatted}&7.",
                    "{order_id}", String.valueOf(result.order().id()),
                    "{quantity}", String.valueOf(result.order().requestedQuantity()),
                    "{item}", manager.describeItem(result.order().requestedItem()),
                    "{price_each}", NumberUtils.format(result.order().priceEach()),
                    "{price_each_formatted}", plugin.getCurrencyManager().formatMoney(result.order().priceEach()),
                    "{budget}", NumberUtils.format(result.order().totalBudget()),
                    "{budget_formatted}", plugin.getCurrencyManager().formatMoney(result.order().totalBudget())
            )));
            SoundUtils.play(player, plugin.getConfigManager().getSound("ORDERS.SUCCESS"));
            new OrdersMyOrdersMenu(plugin, 1, manager.getDefaultSort()).open(player);
        } finally {
            manager.endAction(player.getUniqueId());
        }
    }

    private String resolveFailureMessage(OrdersManager.CreateOrderResult result) {
        return switch (result.reason()) {
            case DISABLED -> plugin.getConfigManager().getMessageOrDefault("ORDERS.DISABLED", "&cᴏʀᴅᴇʀѕ ɪѕ ᴄᴜʀʀᴇɴᴛʟʏ ᴅɪѕᴀʙʟᴇᴅ.");
            case NO_PENDING_ORDER -> plugin.getConfigManager().getMessageOrDefault("ORDERS.NO_PENDING_ORDER", "&cᴛʜᴇʀᴇ ɪѕ ɴᴏ ᴘᴇɴᴅɪɴɢ ᴏʀᴅᴇʀ ᴅʀᴀꜰᴛ ᴛᴏ ᴄᴏɴꜰɪʀᴍ.");
            case NO_PLAYER_DATA -> "&cʏᴏᴜʀ ᴘʟᴀʏᴇʀ ᴅᴀᴛᴀ ᴄᴏᴜʟᴅ ɴᴏᴛ ʙᴇ ʟᴏᴀᴅᴇᴅ.";
            case INVALID_ITEM -> plugin.getConfigManager().getMessageOrDefault("ORDERS.ITEM_BLOCKED", "&cᴛʜᴀᴛ ɪᴛᴇᴍ ᴄᴀɴɴᴏᴛ ʙᴇ ᴏʀᴅᴇʀᴇᴅ.");
            case INVALID_QUANTITY -> plugin.getConfigManager().getMessageOrDefault("ORDERS.INVALID_QUANTITY", "&cɪɴᴠᴀʟɪᴅ ǫᴜᴀɴᴛɪᴛʏ.");
            case INVALID_PRICE -> plugin.getConfigManager().getMessageOrDefault("ORDERS.INVALID_PRICE", "&cɪɴᴠᴀʟɪᴅ ᴘʀɪᴄᴇ.");
            case TOTAL_TOO_HIGH -> plugin.getConfigManager().getMessageOrDefault(
                    "ORDERS.TOTAL_TOO_HIGH",
                    "&cᴛᴏᴛᴀʟ ᴏʀᴅᴇʀ ʙᴜᴅɢᴇᴛ ᴄᴀɴɴᴏᴛ ᴇxᴄᴇᴇᴅ &f{max_formatted}&c.",
                    "{max_formatted}", plugin.getCurrencyManager().formatMoney(
                            plugin.getConfigManager().getOrders().getDouble("PRICING.MAX_TOTAL_BUDGET", 250_000_000D)
                    )
            );
            case NO_MONEY -> plugin.getConfigManager().getMessageOrDefault(
                    "ORDERS.NOT_ENOUGH_MONEY",
                    "&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴇɴᴏᴜɢʜ "
                            + plugin.getCurrencyManager().plural(com.bx.ultimateDonutSmp.managers.CurrencyManager.CurrencyType.MONEY)
                            + " ꜰᴏʀ ᴛʜᴀᴛ ᴏʀᴅᴇʀ."
            );
            case MAX_ORDERS_REACHED -> plugin.getConfigManager().getMessageOrDefault("ORDERS.MAX_ACTIVE_REACHED", "&cʏᴏᴜ ʜᴀᴠᴇ ʀᴇᴀᴄʜᴇᴅ ʏᴏᴜʀ ᴀᴄᴛɪᴠᴇ ᴏʀᴅᴇʀ ʟɪᴍɪᴛ.");
            case DATABASE_ERROR -> "&cᴏʀᴅᴇʀѕ ᴄᴏᴜʟᴅ ɴᴏᴛ ѕᴀᴠᴇ ʏᴏᴜʀ ᴏʀᴅᴇʀ ʀɪɢʜᴛ ɴᴏᴡ. ᴛʀʏ ᴀɢᴀɪɴ.";
        };
    }
}
