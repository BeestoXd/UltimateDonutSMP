package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.Home;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeActionMenu extends BaseMenu {

    private final Home home;
    private final Map<Integer, Runnable> slotActions = new HashMap<>();

    public HomeActionMenu(UltimateDonutSmp plugin, Home home) {
        super(plugin, "&8Manage Home: &f" + (home != null ? home.getName() : ""), 27);
        this.home = home;
    }

    @Override
    public void build(Player player) {
        clear();
        slotActions.clear();
        fill(Material.LIGHT_GRAY_STAINED_GLASS_PANE);

        if (home == null) return;

        // Teleport - Slot 11
        set(11, ItemUtils.createItem(
                Material.ENDER_PEARL,
                "&aTeleport to Home",
                List.of("&7Click to teleport to &f" + home.getName())
        ));
        slotActions.put(11, () -> {
            player.closeInventory();
            plugin.getTeleportManager().queue(player, home.getLocation(), "HOME", null);
        });

        // Rename - Slot 13
        set(13, ItemUtils.createItem(
                Material.NAME_TAG,
                "&eRename Home",
                List.of("&7Click to rename &f" + home.getName())
        ));
        slotActions.put(13, () -> plugin.getHomeManager().promptRenameHome(player, home.getName()));

        // Delete - Slot 15
        set(15, ItemUtils.createItem(
                Material.RED_DYE,
                "&cDelete Home",
                List.of("&7Click to delete &f" + home.getName())
        ));
        slotActions.put(15, () -> new HomeDeleteConfirmMenu(plugin, home).open(player));

        // Back - Slot 26
        set(26, ItemUtils.createItem(
                Material.BARRIER,
                "&cBack",
                List.of("&7Go back to homes menu")
        ));
        slotActions.put(26, () -> new HomeMenu(plugin).open(player));
    }

    @Override
    public void handleClick(int slot, Player player, ClickType clickType) {
        Runnable action = slotActions.get(slot);
        if (action != null) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            action.run();
        }
    }
}
