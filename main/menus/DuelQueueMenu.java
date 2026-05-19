package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.DuelStats;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public class DuelQueueMenu extends BaseMenu {

    public DuelQueueMenu(UltimateDonutSmp plugin) {
        super(plugin, plugin.getDuelManager().getQueueTitle(), plugin.getDuelManager().getQueueSize());
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);

        DuelStats stats = plugin.getDuelManager().getStats(player.getUniqueId());
        boolean queued = plugin.getDuelManager().isInQueue(player.getUniqueId());

        set(11, ItemUtils.createItem(
                queued ? Material.RED_STAINED_GLASS_PANE : Material.LIME_STAINED_GLASS_PANE,
                queued ? "&cʟᴇᴀᴠᴇ ǫᴜᴇᴜᴇ" : "&aᴊᴏɪɴ ᴄᴀѕᴜᴀʟ ǫᴜᴇᴜᴇ",
                List.of(
                        "&7ᴘʟᴀʏᴇʀѕ ǫᴜᴇᴜᴇᴅ: &f" + plugin.getDuelManager().getQueueSizeCount(),
                        queued ? "&7ᴄʟɪᴄᴋ ᴛᴏ ʟᴇᴀᴠᴇ ᴛʜᴇ ᴅᴜᴇʟ ǫᴜᴇᴜᴇ." : "&7ᴄʟɪᴄᴋ ᴛᴏ ᴊᴏɪɴ ᴛʜᴇ ᴅᴜᴇʟ ǫᴜᴇᴜᴇ."
                )
        ));
        set(13, ItemUtils.createItem(
                Material.NETHERITE_SWORD,
                "&eʏᴏᴜʀ ᴅᴜᴇʟ ѕᴛᴀᴛѕ",
                List.of(
                        "&7ᴡɪɴѕ: &f" + stats.getWins(),
                        "&7ʟᴏѕѕᴇѕ: &f" + stats.getLosses(),
                        "&7ᴅʀᴀᴡѕ: &f" + stats.getDraws(),
                        "&7ѕᴛʀᴇᴀᴋ: &f" + stats.getCurrentStreak(),
                        "&7ʙᴇѕᴛ ѕᴛʀᴇᴀᴋ: &f" + stats.getBestStreak()
                )
        ));
        set(15, ItemUtils.createItem(
                Material.ENDER_CHEST,
                "&dᴄʟᴀɪᴍѕ",
                List.of("&7ᴏᴘᴇɴ ᴅᴜᴇʟ ʟᴏᴏᴛ ᴄʟᴀɪᴍ ᴘᴀᴄᴋᴀɢᴇѕ.")
        ));
        set(26, ItemUtils.createItem(Material.BARRIER, "&cᴄʟᴏѕᴇ"));
    }

    @Override
    public void handleClick(int slot, Player player) {
        if (slot == 11) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("DUELS.CLICK"));
            if (plugin.getDuelManager().isInQueue(player.getUniqueId())) {
                plugin.getDuelManager().leaveState(player);
            } else {
                plugin.getDuelManager().joinQueue(player);
            }
            if (plugin.getDuelManager().isInDuel(player.getUniqueId())) {
                player.closeInventory();
            } else {
                new DuelQueueMenu(plugin).open(player);
            }
            return;
        }
        if (slot == 15) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("DUELS.CLICK"));
            new DuelClaimMenu(plugin, 1).open(player);
            return;
        }
        if (slot == 26) {
            player.closeInventory();
        }
    }
}
