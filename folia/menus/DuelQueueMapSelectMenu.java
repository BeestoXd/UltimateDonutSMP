package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.DuelManager;
import com.bx.ultimateDonutSmp.models.DuelMapSelection;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class DuelQueueMapSelectMenu extends BaseMenu {

    private final DuelMapSelection selectedSelection;

    public DuelQueueMapSelectMenu(UltimateDonutSmp plugin, DuelMapSelection selectedSelection) {
        super(plugin, "&8ѕᴇʟᴇᴄᴛ ᴅᴜᴇʟ ᴍᴀᴘ", plugin.getDuelManager().getQueueSize());
        this.selectedSelection = selectedSelection;
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);

        List<DuelManager.DuelMapOption> options = plugin.getDuelManager().getSelectableMapOptions(true);
        int[] slots = contentSlots();
        for (int i = 0; i < Math.min(options.size(), slots.length); i++) {
            DuelManager.DuelMapOption option = options.get(i);
            boolean selected = option.selection().equals(selectedSelection);
            List<String> lore = new ArrayList<>();
            lore.add("&7" + option.description());
            if (selected) {
                lore.add("&aᴄᴜʀʀᴇɴᴛʟʏ ѕᴇʟᴇᴄᴛᴇᴅ.");
            } else {
                lore.add("&eᴄʟɪᴄᴋ ᴛᴏ ѕᴇʟᴇᴄᴛ.");
            }
            set(slots[i], ItemUtils.createItem(
                    materialFor(option.selection()),
                    (selected ? "&a" : "&e") + option.displayName(),
                    lore
            ));
        }

        if (options.isEmpty()) {
            set(13, ItemUtils.createItem(Material.BARRIER, "&cɴᴏ ǫᴜᴇᴜᴇ ᴍᴀᴘѕ ᴀᴠᴀɪʟᴀʙʟᴇ", List.of("&7ᴄᴏɴꜰɪɢᴜʀᴇ ǫᴜᴇᴜᴇ ᴀʀᴇɴᴀѕ ᴏʀ ᴇɴᴀʙʟᴇ ʀᴀɴᴅᴏᴍ ʙɪᴏᴍᴇѕ.")));
        }

        int lastRow = inventory.getSize() - 9;
        set(lastRow + 4, ItemUtils.createItem(Material.ARROW, "&eʙᴀᴄᴋ", List.of("&7ʀᴇᴛᴜʀɴ ᴛᴏ ǫᴜᴇᴜᴇ ᴍᴇɴᴜ.")));
        set(lastRow + 8, ItemUtils.createItem(Material.BARRIER, "&cᴄʟᴏѕᴇ"));
    }

    @Override
    public void handleClick(int slot, Player player) {
        int lastRow = inventory.getSize() - 9;
        if (slot == lastRow + 4) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("DUELS.CLICK"));
            new DuelQueueMenu(plugin, selectedSelection).open(player);
            return;
        }
        if (slot == inventory.getSize() - 1) {
            player.closeInventory();
            return;
        }

        List<DuelManager.DuelMapOption> options = plugin.getDuelManager().getSelectableMapOptions(true);
        int index = optionIndex(slot);
        if (index >= 0 && index < options.size()) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("DUELS.CLICK"));
            new DuelQueueMenu(plugin, options.get(index).selection()).open(player);
        }
    }

    private int[] contentSlots() {
        int rows = inventory.getSize() / 9;
        List<Integer> slots = new ArrayList<>();
        for (int row = 0; row < rows - 1; row++) {
            for (int column = 0; column < 9; column++) {
                slots.add(row * 9 + column);
            }
        }
        return slots.stream().mapToInt(Integer::intValue).toArray();
    }

    private int optionIndex(int clickedSlot) {
        int[] slots = contentSlots();
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] == clickedSlot) {
                return i;
            }
        }
        return -1;
    }

    private Material materialFor(DuelMapSelection selection) {
        return switch (selection.type()) {
            case STATIC_ARENA -> Material.IRON_SWORD;
            case RANDOM_STATIC -> Material.COMPASS;
            case BIOME -> Material.GRASS_BLOCK;
            case RANDOM_BIOME -> Material.MAP;
        };
    }
}
