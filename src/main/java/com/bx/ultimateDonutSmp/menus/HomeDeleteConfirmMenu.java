package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.Home;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeDeleteConfirmMenu extends BaseMenu {

    private final Home home;
    private final Map<Integer, Runnable> slotActions = new HashMap<>();

    public HomeDeleteConfirmMenu(UltimateDonutSmp plugin, Home home) {
        super(plugin, "&8Delete Home: &c" + (home != null ? home.getName() : "") + "?", 27);
        this.home = home;
    }

    @Override
    public void build(Player player) {
        clear();
        slotActions.clear();
        fill(Material.LIGHT_GRAY_STAINED_GLASS_PANE);

        if (home == null) return;

        // Confirm Delete - Slot 11
        set(11, ItemUtils.createItem(
                Material.LIME_TERRACOTTA,
                "&aConfirm Delete",
                List.of("&7Permanently delete home &f" + home.getName())
        ));
        slotActions.put(11, () -> {
            boolean removed = plugin.getHomeManager().deleteHome(player.getUniqueId(), home.getName());
            player.sendMessage(ColorUtils.toComponent(removed
                    ? plugin.getConfigManager().getMessage("HOME.DELETED")
                    : "&cHome not found."));
            new HomeMenu(plugin).open(player);
        });

        // Cancel - Slot 15
        set(15, ItemUtils.createItem(
                Material.RED_TERRACOTTA,
                "&cCancel",
                List.of("&7Do not delete this home")
        ));
        slotActions.put(15, () -> new HomeMenu(plugin).open(player));
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
