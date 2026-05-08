package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import com.bx.ultimateDonutSmp.utils.PaymentUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;

public class PayConfirmMenu extends BaseMenu {

    public enum PaymentType {
        MONEY,
        SHARDS
    }

    private final String targetName;
    private final PaymentType paymentType;
    private final double moneyAmount;
    private final long shardAmount;

    public PayConfirmMenu(UltimateDonutSmp plugin, String targetName, double amount) {
        super(
                plugin,
                plugin.getConfigManager().getMenus().getString("PAY-CONFIRM-MENU.TITLE", "&8Confirm Payment"),
                plugin.getConfigManager().getMenus().getInt("PAY-CONFIRM-MENU.SIZE", 27)
        );
        this.targetName = targetName;
        this.paymentType = PaymentType.MONEY;
        this.moneyAmount = amount;
        this.shardAmount = 0L;
    }

    public PayConfirmMenu(UltimateDonutSmp plugin, String targetName, long amount) {
        super(
                plugin,
                plugin.getConfigManager().getMenus().getString("PAY-CONFIRM-MENU.TITLE", "&8Confirm Payment"),
                plugin.getConfigManager().getMenus().getInt("PAY-CONFIRM-MENU.SIZE", 27)
        );
        this.targetName = targetName;
        this.paymentType = PaymentType.SHARDS;
        this.moneyAmount = 0.0;
        this.shardAmount = amount;
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);

        set(11, ItemUtils.createItem(Material.RED_STAINED_GLASS_PANE, "&cCancel",
                List.of("&7Click to cancel this payment.")));

        set(15, ItemUtils.createItem(Material.LIME_STAINED_GLASS_PANE, "&aConfirm",
                List.of("&7Click to confirm this payment.")));

        String amountText = paymentType == PaymentType.MONEY
                ? "&a$" + NumberUtils.format(moneyAmount)
                : "&#A303F9" + shardAmount + " Shards";

        set(13, createTargetItem(List.of(
                "&7Target: &f" + targetName,
                "&7Amount: " + amountText
        )));
    }

    @Override
    public void handleClick(int slot, Player player) {
        if (slot != 11 && slot != 15) return;

        SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
        player.closeInventory();

        if (slot == 11) {
            return;
        }

        if (paymentType == PaymentType.MONEY) {
            PaymentUtils.transferMoney(plugin, player, targetName, moneyAmount);
        } else {
            PaymentUtils.transferShards(plugin, player, targetName, shardAmount);
        }
    }

    private ItemStack createTargetItem(List<String> lore) {
        ItemStack item = ItemUtils.createItem(Material.PLAYER_HEAD, "&a" + targetName, lore);
        if (!(item.getItemMeta() instanceof SkullMeta meta)) {
            return item;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        meta.setOwningPlayer(target);
        item.setItemMeta(meta);
        return item;
    }
}
