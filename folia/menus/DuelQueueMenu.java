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
                queued ? "&cLeave Queue" : "&aJoin Casual Queue",
                List.of(
                        "&7Players queued: &f" + plugin.getDuelManager().getQueueSizeCount(),
                        queued ? "&7Click to leave the duel queue." : "&7Click to join the duel queue."
                )
        ));
        set(13, ItemUtils.createItem(
                Material.NETHERITE_SWORD,
                "&eYour Duel Stats",
                List.of(
                        "&7Wins: &f" + stats.getWins(),
                        "&7Losses: &f" + stats.getLosses(),
                        "&7Draws: &f" + stats.getDraws(),
                        "&7Streak: &f" + stats.getCurrentStreak(),
                        "&7Best Streak: &f" + stats.getBestStreak()
                )
        ));
        set(15, ItemUtils.createItem(
                Material.ENDER_CHEST,
                "&dClaims",
                List.of("&7Open duel loot claim packages.")
        ));
        set(26, ItemUtils.createItem(Material.BARRIER, "&cClose"));
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
