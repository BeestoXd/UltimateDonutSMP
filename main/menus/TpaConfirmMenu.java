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
                        .getString("TPA-CONFIRM-MENU.TITLE", "&8ᴄᴏɴꜰɪʀᴍ ᴛᴘᴀ {here}")
                        .replace("{here}", tpaHere ? "ʜᴇʀᴇ" : ""),
                plugin.getConfigManager().getMenus().getInt("TPA-CONFIRM-MENU.SIZE", 27)
        );
        this.requesterName = requesterName;
        this.tpaHere = tpaHere;
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);

        set(11, ItemUtils.createItem(Material.RED_STAINED_GLASS_PANE, "&cᴄᴀɴᴄᴇʟ",
                List.of("&7ᴅᴇɴʏ ᴛʜɪѕ ᴛᴇʟᴇᴘᴏʀᴛ ʀᴇǫᴜᴇѕᴛ.")));

        set(15, ItemUtils.createItem(Material.LIME_STAINED_GLASS_PANE, "&aᴄᴏɴꜰɪʀᴍ",
                List.of("&7ᴀᴄᴄᴇᴘᴛ ᴛʜɪѕ ᴛᴇʟᴇᴘᴏʀᴛ ʀᴇǫᴜᴇѕᴛ.")));

        String requestText = tpaHere
                ? "&7" + requesterName + " ᴡᴀɴᴛѕ ʏᴏᴜ ᴛᴏ ᴛᴇʟᴇᴘᴏʀᴛ ᴛᴏ ᴛʜᴇᴍ."
                : "&7" + requesterName + " ᴡᴀɴᴛѕ ᴛᴏ ᴛᴇʟᴇᴘᴏʀᴛ ᴛᴏ ʏᴏᴜ.";
        set(13, createRequesterItem(List.of(requestText)));
    }

    @Override
    public void handleClick(int slot, Player player) {
        if (slot != 11 && slot != 15) return;

        SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
        player.closeInventory();

        if (slot == 11) {
            player.performCommand("ᴛᴘᴀᴅᴇɴʏ " + requesterName);
            return;
        }

        player.performCommand("ᴛᴘᴀᴄᴄᴇᴘᴛ " + requesterName);
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
