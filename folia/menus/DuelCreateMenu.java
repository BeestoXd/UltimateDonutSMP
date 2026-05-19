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
            set(13, ItemUtils.createItem(Material.BARRIER, "&cᴛᴀʀɢᴇᴛ ᴏꜰꜰʟɪɴᴇ", List.of("&7ᴛʜɪѕ ᴘʟᴀʏᴇʀ ɪѕ ɴᴏ ʟᴏɴɢᴇʀ ᴏɴʟɪɴᴇ.")));
            set(inventory.getSize() - 1, ItemUtils.createItem(Material.BARRIER, "&cᴄʟᴏѕᴇ"));
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
                            "&7ᴀʀᴇɴᴀ ɪᴅ: &f" + arena.getId(),
                            "&7ᴄʟɪᴄᴋ ᴛᴏ ᴄʜᴀʟʟᴇɴɢᴇ &f" + target.getName(),
                            "&7ᴜѕɪɴɢ ᴛʜɪѕ ᴀʀᴇɴᴀ."
                    )
            ));
        }

        if (arenas.isEmpty()) {
            set(13, ItemUtils.createItem(Material.BARRIER, "&cɴᴏ ʀᴇᴀᴅʏ ᴀʀᴇɴᴀ", List.of("&7ѕᴇᴛ ᴅᴜᴇʟ ᴀʀᴇɴᴀѕ ꜰɪʀѕᴛ ᴡɪᴛʜ &f/arena&7.")));
        }

        int lastRow = inventory.getSize() - 9;
        set(lastRow + 3, ItemUtils.createPlayerHead(target, "&eᴛᴀʀɢᴇᴛ: &f" + target.getName(), List.of("&7ᴄʜᴏᴏѕᴇ ᴀɴ ᴀʀᴇɴᴀ ᴛᴏ ѕᴇɴᴅ ᴀ ᴅᴜᴇʟ ʀᴇǫᴜᴇѕᴛ.")));
        set(lastRow + 4, ItemUtils.createItem(Material.COMPASS, "&bʀᴀɴᴅᴏᴍ ᴀʀᴇɴᴀ", List.of("&7ѕᴇɴᴅ ᴀ ᴅᴜᴇʟ ʀᴇǫᴜᴇѕᴛ ᴜѕɪɴɢ ᴀɴʏ ᴀᴠᴀɪʟᴀʙʟᴇ ᴀʀᴇɴᴀ.")));
        set(lastRow + 8, ItemUtils.createItem(Material.BARRIER, "&cᴄʟᴏѕᴇ"));
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
