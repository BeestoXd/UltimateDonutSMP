package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.OrdersManager;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import com.bx.ultimateDonutSmp.utils.SignInputUtil;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class OrdersNewMenu extends BaseMenu {

    public OrdersNewMenu(UltimateDonutSmp plugin) {
        super(plugin, plugin.getOrdersManager().getNewOrderTitle(), plugin.getOrdersManager().getNewOrderSize());
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);

        OrdersManager manager = plugin.getOrdersManager();
        OrdersManager.NewOrderSession session = manager.getOrCreateNewOrderSession(player.getUniqueId());

        // Slot 10: Cancel
        set(10, ItemUtils.createItem(
                Material.RED_STAINED_GLASS_PANE,
                "&cᴄᴀɴᴄᴇʟ",
                List.of("&7ᴄʟɪᴄᴋ ᴛᴏ ᴄᴀɴᴄᴇʟ ᴀɴ ɪᴛᴇᴍ ᴀɴᴅ ʀᴇᴛᴜʀɴ")
        ));

        // Slot 12: Item
        ItemStack itemDisplay;
        if (session.getChosenItem() == null) {
            itemDisplay = ItemUtils.createItem(
                    Material.BARRIER,
                    "&bᴄʜᴏᴏѕᴇ ɪᴛᴇᴍ",
                    List.of("&7ᴄʟɪᴄᴋ ᴛᴏ ѕᴇʟᴇᴄᴛ ᴛʜᴇ ɪᴛᴇᴍ ʏᴏᴜ ᴡᴀɴᴛ ᴛᴏ ᴏʀᴅᴇʀ.")
            );
        } else {
            itemDisplay = OrdersMenuSupport.decorateItem(
                    plugin,
                    session.getChosenItem(),
                    manager.describeItem(session.getChosenItem()),
                    List.of(
                            "",
                            "&7ᴄᴀᴛᴇɢᴏʀʏ: &f" + manager.prettifyCategory(session.getCategoryKey()),
                            "",
                            "&eᴄʟɪᴄᴋ ᴛᴏ ᴄʜᴀɴɢᴇ ɪᴛᴇᴍ"
                    )
            );
        }
        set(12, itemDisplay);

        // Slot 13: Amount
        int amount = session.getAmount();
        ItemStack amountDisplay = ItemUtils.createItem(
                Material.PAPER,
                "&bᴏʀᴅᴇʀ ǫᴜᴀɴᴛɪᴛʏ",
                List.of(
                        "&7ᴄᴜʀʀᴇɴᴛ ǫᴜᴀɴᴛɪᴛʏ: &e" + (amount <= 0 ? "ɴᴏᴛ ѕᴇᴛ" : amount),
                        "",
                        "&eᴄʟɪᴄᴋ ᴛᴏ ѕᴇᴛ ǫᴜᴀɴᴛɪᴛʏ"
                )
        );
        if (amount > 0) {
            amountDisplay.setAmount(Math.max(1, Math.min(64, amount)));
        }
        set(13, amountDisplay);

        // Slot 14: Price
        double priceEach = session.getPriceEach();
        ItemStack priceDisplay = ItemUtils.createItem(
                Material.SUNFLOWER,
                "&bᴘʀɪᴄᴇ ᴇᴀᴄʜ",
                List.of(
                        "&7ᴄᴜʀʀᴇɴᴛ ᴘʀɪᴄᴇ: &e" + (priceEach <= 0D ? "ɴᴏᴛ ѕᴇᴛ" : plugin.getCurrencyManager().formatMoney(priceEach)),
                        "",
                        "&eᴄʟɪᴄᴋ ᴛᴏ ѕ..."
                )
        );
        // Let's refine description
        List<String> priceLore = new ArrayList<>();
        priceLore.add("&7ᴄᴜʀʀᴇɴᴛ ᴘʀɪᴄᴇ: &e" + (priceEach <= 0D ? "ɴᴏᴛ ѕᴇᴛ" : plugin.getCurrencyManager().formatMoney(priceEach)));
        priceLore.add("");
        priceLore.add("&eᴄʟɪᴄᴋ ᴛᴏ ѕᴇᴛ ᴘʀɪᴄᴇ");
        priceDisplay = ItemUtils.createItem(Material.SUNFLOWER, "&bᴘʀɪᴄᴇ ᴇᴀᴄʜ", priceLore);
        set(14, priceDisplay);

        // Slot 16: Confirm
        double totalBudget = manager.roundCurrency(amount * priceEach);
        double creationFee = plugin.getConfigManager().getOrders().getDouble("PRICING.ORDER_CREATION_FEE", 0D);
        double requiredTotal = totalBudget + creationFee;
        boolean canConfirm = session.getChosenItem() != null && amount > 0 && priceEach > 0D;

        List<String> confirmLore = new ArrayList<>();
        if (!canConfirm) {
            confirmLore.add("&cᴘʟᴇᴀѕᴇ ѕᴇᴛ ɪᴛᴇᴍ, ǫᴜᴀɴᴛɪᴛʏ, ᴀɴᴅ ᴘʀɪᴄᴇ ꜰɪʀѕᴛ.");
        } else {
            confirmLore.add("&7ᴛᴏᴛᴀʟ ʙᴜᴅɢᴇᴛ: &e" + plugin.getCurrencyManager().formatMoney(totalBudget));
            confirmLore.add("&7ᴄʀᴇᴀᴛɪᴏɴ ꜰᴇᴇ: &e" + plugin.getCurrencyManager().formatMoney(creationFee));
            confirmLore.add("&7ʀᴇǫᴜɪʀᴇᴅ ᴛᴏᴛᴀʟ: &e" + plugin.getCurrencyManager().formatMoney(requiredTotal));
            confirmLore.add("");
            confirmLore.add("&7ᴄᴜʀʀᴇɴᴛ ʙᴀʟᴀɴᴄᴇ: " + plugin.getCurrencyManager().formatMoney(plugin.getEconomyManager().getBalance(player)));
            confirmLore.add("");
            confirmLore.add("&aᴄʟɪᴄᴋ ᴛᴏ ᴄᴏɴꜰɪʀᴍ &7(ʟᴏᴄᴋѕ ʙᴜᴅɢᴇᴛ ɪɴ ᴇѕᴄʀᴏᴡ)");
        }

        ItemStack confirmDisplay = ItemUtils.createItem(
                canConfirm ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE,
                "&aᴄᴏɴꜰɪʀᴍ ᴏʀᴅᴇʀ",
                confirmLore
        );
        set(16, confirmDisplay);
    }

    @Override
    public void handleClick(int slot, Player player) {
        OrdersManager manager = plugin.getOrdersManager();
        OrdersManager.NewOrderSession session = manager.getOrCreateNewOrderSession(player.getUniqueId());

        if (slot == 10) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            manager.clearPendingCreation(player.getUniqueId());
            new OrdersBrowseMenu(plugin, 1, manager.getDefaultSort(), "ALL").open(player);
            return;
        }

        if (slot == 12) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            manager.openNewOrderItemSelection(player);
            return;
        }

        if (slot == 13) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            ConfigurationSection config = plugin.getConfigManager().getOrdersConfig().getConfigurationSection("AMOUNT_SIGN");
            SignInputUtil.openFromConfig(plugin, player, config, text -> {
                if (text != null && !text.isBlank()) {
                    try {
                        int quantity = Math.toIntExact(NumberUtils.parseLong(text));
                        if (quantity > 0 && quantity <= manager.getMaxQuantityPerOrder()) {
                            session.setAmount(quantity);
                        } else {
                            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                                    "ORDERS.QUANTITY_OUT_OF_RANGE",
                                    "&cǫᴜᴀɴᴛɪᴛʏ ᴍᴜѕᴛ ʙᴇ ʙᴇᴛᴡᴇᴇɴ 1 ᴀɴᴅ {max}.",
                                    "{max}", String.valueOf(manager.getMaxQuantityPerOrder())
                            )));
                        }
                    } catch (Exception e) {
                        player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                                "ORDERS.INVALID_QUANTITY",
                                "&cɪɴᴠᴀʟɪᴅ ǫᴜᴀɴᴛɪᴛʏ. ᴜѕᴇ ᴀ ᴡʜᴏʟᴇ ɴᴜᴍʙᴇʀ ɢʀᴇᴀᴛᴇʀ ᴛʜᴀɴ 0."
                        )));
                    }
                }
                new OrdersNewMenu(plugin).open(player);
            });
            return;
        }

        if (slot == 14) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            ConfigurationSection config = plugin.getConfigManager().getOrdersConfig().getConfigurationSection("PRICE_SIGN");
            SignInputUtil.openFromConfig(plugin, player, config, text -> {
                if (text != null && !text.isBlank()) {
                    try {
                        double priceEach = NumberUtils.parse(text);
                        double normalizedPrice = manager.roundCurrency(priceEach);
                        if (normalizedPrice >= manager.getMinPriceEach() && normalizedPrice <= manager.getMaxPriceEach()) {
                            session.setPriceEach(normalizedPrice);
                        } else {
                            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                                    "ORDERS.PRICE_OUT_OF_RANGE",
                                    "&cᴘʀɪᴄᴇ ᴇᴀᴄʜ ᴍᴜѕᴛ ʙᴇ ʙᴇᴛᴡᴇᴇɴ &f{min_formatted}&c ᴀɴᴅ &f{max_formatted}&c.",
                                    "{min_formatted}", plugin.getCurrencyManager().formatMoney(manager.getMinPriceEach()),
                                    "{max_formatted}", plugin.getCurrencyManager().formatMoney(manager.getMaxPriceEach())
                            )));
                        }
                    } catch (Exception e) {
                        player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                                "ORDERS.INVALID_PRICE",
                                "&cɪɴᴠᴀʟɪᴅ ᴘʀɪᴄᴇ ꜰᴏʀᴍᴀᴛ. ᴜѕᴇ ɴᴜᴍʙᴇʀѕ ʟɪᴋᴇ 100, 5ᴋ, ᴏʀ 1.5ᴍ."
                        )));
                    }
                }
                new OrdersNewMenu(plugin).open(player);
            });
            return;
        }

        if (slot == 16) {
            boolean canConfirm = session.getChosenItem() != null && session.getAmount() > 0 && session.getPriceEach() > 0D;
            if (!canConfirm) {
                return;
            }

            if (!manager.beginAction(player.getUniqueId())) {
                player.sendMessage(ColorUtils.toComponent("&c..."));
                return;
            }

            try {
                if (manager.isOnClickCooldown(player.getUniqueId())) {
                    player.sendMessage(ColorUtils.toComponent("&c..."));
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
    }

    private String resolveFailureMessage(OrdersManager.CreateOrderResult result) {
        return switch (result.reason()) {
            case DISABLED -> plugin.getConfigManager().getMessageOrDefault("ORDERS.DISABLED", "&cᴏʀᴅᴇʀѕ ɪѕ ᴄᴜʀʀᴇɴᴛʟʏ ᴅɪѕᴀʙʟᴇᴅ.");
            case NO_PENDING_ORDER -> plugin.getConfigManager().getMessageOrDefault("ORDERS.NO_PENDING_ORDER", "&cᴛʜᴇʀᴇ ɪѕ ɴᴏ ᴘᴇɴᴅɪɴɢ ᴏʀᴅᴇʀ ᴅʀᴀꜰᴛ ᴛᴏ ᴄᴏɴꜰɪʀᴍ.");
            case NO_PLAYER_DATA -> "&cʏᴏᴜʀ ᴘʟᴀʏᴇʀ ᴅᴀᴛᴀ ᴄᴏᴜʟᴅ ɴᴏᴛ ʙᴇ ʟᴏᴀᴅᴇᴅ.";
            case INVALID_ITEM -> plugin.getConfigManager().getMessageOrDefault("ORDERS.ITEM_BLOCKED", "&cᴛʜᴀᴛ ɪᴛᴇᴍ ᴄᴀɴɴᴏᴛ ʙᴇ ᴏʀᴅᴇʀᴇᴅ.");
            case UNSAFE_ITEM -> plugin.getConfigManager().getMessageOrDefault(
                    "CRASH_PROTECTION.ITEM_BLOCKED",
                    "&cᴛʜᴀᴛ ɪᴛᴇᴍ ᴄᴀɴɴᴏᴛ ʙᴇ ᴜѕᴇᴅ ʜᴇʀᴇ ʙᴇᴄᴀᴜѕᴇ ɪᴛѕ ᴅᴀᴛᴀ ʟᴏᴏᴋѕ ᴜɴѕᴀꜰᴇ. &7ᴄᴏɴᴛᴇxᴛ: &f{context}&7. ʀᴇᴀѕᴏɴ: &f{reason}",
                    "{context}", "ᴏʀᴅᴇʀѕ",
                    "{reason}", result.safetyResult() == null ? "ᴜɴѕᴀꜰᴇ ɪᴛᴇᴍ ᴅᴀᴛᴀ" : result.safetyResult().reason()
            );
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
                    "&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴇɴᴏᴜɢʜ ᴍᴏɴᴇʏ ꜰᴏʀ ᴛʜᴀᴛ ᴏʀᴅᴇʀ."
            );
            case MAX_ORDERS_REACHED -> plugin.getConfigManager().getMessageOrDefault("ORDERS.MAX_ACTIVE_REACHED", "&cʏᴏᴜ ʜᴀᴠᴇ ʀᴇᴀᴄʜᴇᴅ ʏᴏᴜʀ ᴀᴄᴛɪᴠᴇ ᴏʀᴅᴇʀ ʟɪᴍɪᴛ.");
            case DATABASE_ERROR -> "&cᴏʀᴅᴇʀѕ ᴄᴏᴜʟᴅ ɴᴏᴛ ѕᴀᴠᴇ ʏᴏᴜʀ ᴏʀᴅᴇʀ ʀɪɢʜᴛ ɴᴏᴡ. ᴛʀʏ ᴀɢᴀɪɴ.";
        };
    }
}
