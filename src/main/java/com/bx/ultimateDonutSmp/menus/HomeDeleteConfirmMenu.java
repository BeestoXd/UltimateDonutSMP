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
import java.util.function.Consumer;

public class HomeDeleteConfirmMenu extends BaseMenu {

    private final String subject;
    private final int returnPage;
    private final Consumer<Player> onConfirm;
    private final Map<Integer, Runnable> slotActions = new HashMap<>();

    public HomeDeleteConfirmMenu(UltimateDonutSmp plugin, Home home) {
        this(plugin, home, 0);
    }

    public HomeDeleteConfirmMenu(UltimateDonutSmp plugin, Home home, int returnPage) {
        this(
                plugin,
                "&8Delete Home: &c" + homeName(home) + "?",
                home == null ? null : "home &f" + home.getName(),
                returnPage,
                home == null ? null : player -> deleteHome(plugin, player, home)
        );
    }

    private HomeDeleteConfirmMenu(UltimateDonutSmp plugin, String title, String subject,
                                  int returnPage, Consumer<Player> onConfirm) {
        super(plugin, title, 27);
        this.subject = subject;
        this.returnPage = returnPage;
        this.onConfirm = onConfirm;
    }

    /**
     * There is no Home behind the shared team home, and deleting it needs the team permission
     * checks the caller already does, so the caller hands its own delete in.
     */
    public static HomeDeleteConfirmMenu forTeamHome(UltimateDonutSmp plugin, int returnPage,
                                                    Consumer<Player> onConfirm) {
        return new HomeDeleteConfirmMenu(
                plugin, "&8Delete Team Home?", "your team home", returnPage, onConfirm);
    }

    @Override
    public void build(Player player) {
        clear();
        slotActions.clear();
        fill(Material.LIGHT_GRAY_STAINED_GLASS_PANE);

        if (onConfirm == null) return;

        // Confirm Delete - Slot 11
        set(11, ItemUtils.createItem(
                Material.LIME_TERRACOTTA,
                "&aConfirm Delete",
                List.of("&7Permanently delete " + subject)
        ));
        slotActions.put(11, () -> {
            onConfirm.accept(player);
            new HomeMenu(plugin, returnPage).open(player);
        });

        // Cancel - Slot 15
        set(15, ItemUtils.createItem(
                Material.RED_TERRACOTTA,
                "&cCancel",
                List.of("&7Do not delete this home")
        ));
        slotActions.put(15, () -> new HomeMenu(plugin, returnPage).open(player));
    }

    @Override
    public void handleClick(int slot, Player player, ClickType clickType) {
        Runnable action = slotActions.get(slot);
        if (action != null) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            action.run();
        }
    }

    private static void deleteHome(UltimateDonutSmp plugin, Player player, Home home) {
        boolean removed = plugin.getHomeManager().deleteHome(player.getUniqueId(), home.getName());
        player.sendMessage(ColorUtils.toComponent(removed
                ? plugin.getConfigManager().getMessage("HOME.DELETED")
                : "&cHome not found."));
    }

    private static String homeName(Home home) {
        return home == null ? "" : home.getName();
    }
}
