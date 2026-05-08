package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.DuelArena;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class DuelCreateMenu extends BaseMenu {

    private final UUID targetUuid;

    public DuelCreateMenu(UltimateDonutSmp plugin, UUID targetUuid) {
        super(plugin, plugin.getDuelManager().getCreateTitle(Bukkit.getPlayer(targetUuid)), plugin.getDuelManager().getCreateSize());
        this.targetUuid = targetUuid;
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);

        Player target = Bukkit.getPlayer(targetUuid);
        if (target == null) {
            set(13, ItemUtils.createItem(Material.BARRIER, "&cTarget Offline", List.of("&7This player is no longer online.")));
            set(inventory.getSize() - 1, ItemUtils.createItem(Material.BARRIER, "&cClose"));
            return;
        }

        List<DuelArena> arenas = plugin.getDuelManager().getReadyEnabledArenas();
        int slot = 10;
        for (DuelArena arena : arenas) {
            if (slot >= inventory.getSize() - 9) {
                break;
            }

            set(slot++, ItemUtils.createItem(
                    Material.IRON_SWORD,
                    "&a" + arena.getDisplayName(),
                    List.of(
                            "&7Arena ID: &f" + arena.getId(),
                            "&7Click to challenge &f" + target.getName(),
                            "&7using this arena."
                    )
            ));
        }

        if (arenas.isEmpty()) {
            set(13, ItemUtils.createItem(Material.BARRIER, "&cNo Ready Arena", List.of("&7Set duel arenas first with &f/arena&7.")));
        }

        int lastRow = inventory.getSize() - 9;
        set(lastRow + 3, ItemUtils.createPlayerHead(target, "&eTarget: &f" + target.getName(), List.of("&7Choose an arena to send a duel request.")));
        set(lastRow + 4, ItemUtils.createItem(Material.COMPASS, "&bRandom Arena", List.of("&7Send a duel request using any available arena.")));
        set(lastRow + 8, ItemUtils.createItem(Material.BARRIER, "&cClose"));
    }

    @Override
    public void handleClick(int slot, Player player) {
        Player target = Bukkit.getPlayer(targetUuid);
        if (target == null) {
            player.closeInventory();
            return;
        }

        List<DuelArena> arenas = plugin.getDuelManager().getReadyEnabledArenas();
        int index = slot - 10;
        if (index >= 0 && index < arenas.size()) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("DUELS.CLICK"));
            plugin.getDuelManager().sendChallenge(player, target, arenas.get(index).getId());
            player.closeInventory();
            return;
        }

        int lastRow = inventory.getSize() - 9;
        if (slot == lastRow + 4) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("DUELS.CLICK"));
            plugin.getDuelManager().sendChallenge(player, target, null);
            player.closeInventory();
            return;
        }
        if (slot == lastRow + 8) {
            player.closeInventory();
        }
    }
}
