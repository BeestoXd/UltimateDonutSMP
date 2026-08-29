package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PvpKit;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The kit picker shown when a player joins the arena and again after every respawn.
 *
 * <p>A kit with a configured slot keeps it; the rest fill the middle row outwards, so a server
 * with three kits gets them centred without anyone having to pick slot numbers.</p>
 */
public class PvpKitMenu extends BaseMenu {

    private static final List<Integer> CENTERED_SLOTS = List.of(
            13, 12, 14, 11, 15, 10, 16, 9, 17,
            4, 22, 3, 5, 21, 23, 2, 6, 20, 24, 1, 7, 19, 25, 0, 8, 18, 26
    );

    private final Map<Integer, String> slotKits = new HashMap<>();

    public PvpKitMenu(UltimateDonutSmp plugin) {
        super(plugin, "&8PvP kit", 27);
    }

    @Override
    public void build(Player player) {
        clear();
        slotKits.clear();
        fill(Material.BLACK_STAINED_GLASS_PANE);

        List<PvpKit> kits = plugin.getPvpManager().getAvailableKits(player);
        if (kits.isEmpty()) {
            set(13, ItemUtils.createItem(
                    Material.BARRIER,
                    "&cNo kits",
                    List.of("&7There are no PvP kits to choose from yet.")
            ));
            return;
        }

        List<PvpKit> unplaced = new ArrayList<>();
        for (PvpKit kit : kits) {
            int slot = kit.getMenuSlot();
            if (slot >= 0 && slot < inventory.getSize() && !slotKits.containsKey(slot)) {
                place(slot, kit);
            } else {
                unplaced.add(kit);
            }
        }

        int cursor = 0;
        for (PvpKit kit : unplaced) {
            while (cursor < CENTERED_SLOTS.size() && slotKits.containsKey(CENTERED_SLOTS.get(cursor))) {
                cursor++;
            }
            if (cursor >= CENTERED_SLOTS.size()) {
                break;
            }
            place(CENTERED_SLOTS.get(cursor), kit);
        }
    }

    private void place(int slot, PvpKit kit) {
        ItemStack icon = ItemUtils.createItem(
                kit.getIcon(),
                kit.getDisplayName(),
                List.of("&7Click to take this kit.")
        );
        set(slot, icon);
        slotKits.put(slot, kit.getId());
    }

    @Override
    public void handleClick(int slot, Player player, ClickType clickType) {
        String kitId = slotKits.get(slot);
        if (kitId == null) {
            return;
        }

        PvpKit kit = plugin.getPvpManager().getKit(kitId);
        if (kit == null) {
            return;
        }

        player.closeInventory();
        plugin.getPvpManager().selectKit(player, kit);
    }

    @Override
    public void onClose(Player player) {
        plugin.getPvpManager().handleKitMenuClosed(player);
    }
}
