package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.SpawnManager;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Asked before an area is removed from the spawn or AFK menu. Right-clicking an area icon used to
 * delete it outright, which is a heavy thing to do by accident: the same click also blanks the point
 * /setspawn or /setafk saved, so a stray right-click could leave the whole server with no spawn.
 */
public class TeleportAreaDeleteConfirmMenu extends BaseMenu {

    private static final int CONFIRM_SLOT = 11;
    private static final int CANCEL_SLOT = 15;

    private final SpawnManager.TeleportArea area;
    private final Map<Integer, Runnable> slotActions = new HashMap<>();

    public TeleportAreaDeleteConfirmMenu(UltimateDonutSmp plugin, SpawnManager.TeleportArea area) {
        super(plugin, "&8Delete " + label(area) + " area: &c" + (area == null ? "" : area.id()) + "?", 27);
        this.area = area;
    }

    @Override
    public void build(Player player) {
        clear();
        slotActions.clear();
        fill(Material.LIGHT_GRAY_STAINED_GLASS_PANE);

        if (area == null) {
            return;
        }

        String label = label(area);

        set(CONFIRM_SLOT, ItemUtils.createItem(
                Material.LIME_TERRACOTTA,
                "&aConfirm Delete",
                List.of(
                        "&7Permanently delete " + label + " area &f" + area.id(),
                        "&7from slot &f" + area.slot() + "&7.",
                        "&cIf this is the point /set" + label + " saved, that is cleared too."
                )
        ));
        slotActions.put(CONFIRM_SLOT, () -> {
            SpawnManager.AreaDeleteResult result = plugin.getSpawnManager().deleteMenuArea(area, player.getName());
            player.sendMessage(ColorUtils.toComponent(result.success()
                    ? "&a" + result.message()
                    : "&cCould not delete this " + label + " Area: &f" + result.message()));
            parentMenu().open(player);
        });

        set(CANCEL_SLOT, ItemUtils.createItem(
                Material.RED_TERRACOTTA,
                "&cCancel",
                List.of("&7Keep this " + label + " area")
        ));
        slotActions.put(CANCEL_SLOT, () -> parentMenu().open(player));
    }

    @Override
    public void handleClick(int slot, Player player, ClickType clickType) {
        Runnable action = slotActions.get(slot);
        if (action != null) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            action.run();
        }
    }

    private TeleportAreaMenu parentMenu() {
        return area != null && area.type() == SpawnManager.AreaType.AFK
                ? new AfkMenu(plugin)
                : new SpawnMenu(plugin);
    }

    private static String label(SpawnManager.TeleportArea area) {
        return area != null && area.type() == SpawnManager.AreaType.AFK ? "afk" : "spawn";
    }
}
