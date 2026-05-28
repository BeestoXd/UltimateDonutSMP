package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.DuelStats;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public class DuelQueueMenu extends BaseMenu {

    private static final int QUEUE_SLOT = 20;
    private static final int STATS_SLOT = 22;
    private static final int CLAIMS_SLOT = 24;

    public DuelQueueMenu(UltimateDonutSmp plugin) {
        super(plugin, plugin.getDuelManager().getQueueTitle(), plugin.getDuelManager().getQueueSize());
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);

        DuelStats stats = plugin.getDuelManager().getStats(player.getUniqueId());
        boolean queued = plugin.getDuelManager().isInQueue(player.getUniqueId());

        set(QUEUE_SLOT, ItemUtils.createItem(
                Material.PAPER,
                queued ? "&cLeave queue" : "&aJoin casual queue",
                List.of(
                        "&7Players queued: &f" + plugin.getDuelManager().getQueueSizeCount(),
                        "&7Mode: &fDefault queue arena",
                        queued ? "&7Click to leave the duel queue." : "&7Click to join the duel queue."
                )
        ));
        set(STATS_SLOT, ItemUtils.createItem(
                Material.NETHERITE_SWORD,
                "&eYour duel stats",
                List.of(
                        "&7Wins: &f" + stats.getWins(),
                        "&7Losses: &f" + stats.getLosses(),
                        "&7Draws: &f" + stats.getDraws(),
                        "&7Streak: &f" + stats.getCurrentStreak(),
                        "&7Best streak: &f" + stats.getBestStreak()
                )
        ));
        set(CLAIMS_SLOT, ItemUtils.createItem(
                Material.ENDER_CHEST,
                "&dClaims",
                List.of("&7Open duel loot claim packages.")
        ));
        set(inventory.getSize() - 1, ItemUtils.createItem(Material.BARRIER, "&cClose"));
    }

    @Override
    public void handleClick(int slot, Player player) {
        if (slot == QUEUE_SLOT) {
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
        if (slot == CLAIMS_SLOT) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("DUELS.CLICK"));
            new DuelClaimMenu(plugin, 1).open(player);
            return;
        }
        if (slot == inventory.getSize() - 1) {
            player.closeInventory();
        }
    }
}
