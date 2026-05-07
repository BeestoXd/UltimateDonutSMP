package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;

public class TpaConfirmMenu extends BaseMenu {

    private final String requesterName;
    private final boolean tpaHere;

    public TpaConfirmMenu(UltimateDonutSmp plugin, String requesterName, boolean tpaHere) {
        super(
                plugin,
                plugin.getConfigManager().getMenus()
                        .getString("TPA-CONFIRM-MENU.TITLE", "&8Confirm TPA {here}")
                        .replace("{here}", tpaHere ? "Here" : ""),
                plugin.getConfigManager().getMenus().getInt("TPA-CONFIRM-MENU.SIZE", 27)
        );
        this.requesterName = requesterName;
        this.tpaHere = tpaHere;
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);

        set(11, ItemUtils.createItem(Material.RED_STAINED_GLASS_PANE, "&cCancel",
                List.of("&7Deny this teleport request.")));

        set(15, ItemUtils.createItem(Material.LIME_STAINED_GLASS_PANE, "&aConfirm",
                List.of("&7Accept this teleport request.")));

        String requestText = tpaHere
                ? "&7" + requesterName + " wants you to teleport to them."
                : "&7" + requesterName + " wants to teleport to you.";
        set(13, createRequesterItem(List.of(requestText)));
    }

    @Override
    public void handleClick(int slot, Player player) {
        if (slot != 11 && slot != 15) return;

        SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
        player.closeInventory();

        if (slot == 11) {
            player.performCommand("tpadeny " + requesterName);
            return;
        }

        player.performCommand("tpaccept " + requesterName);
    }

    private ItemStack createRequesterItem(List<String> lore) {
        ItemStack item = ItemUtils.createItem(Material.PLAYER_HEAD, "&a" + requesterName, lore);
        if (!(item.getItemMeta() instanceof SkullMeta meta)) {
            return item;
        }

        OfflinePlayer requester = Bukkit.getOfflinePlayer(requesterName);
        meta.setOwningPlayer(requester);
        item.setItemMeta(meta);
        return item;
    }
}
