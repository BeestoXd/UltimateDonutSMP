package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.HideManager;
import com.bx.ultimateDonutSmp.models.HideState;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.PermissionUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HideListMenu extends BaseMenu {

    private static final int PAGE_SIZE = 45;

    private final int page;
    private final Map<Integer, UUID> targets = new HashMap<>();

    public HideListMenu(UltimateDonutSmp plugin, int page) {
        super(plugin, title(plugin, page), 54);
        this.page = Math.max(0, page);
    }

    @Override
    public void build(Player player) {
        clear();
        targets.clear();
        if (!PermissionUtils.has(player, HideManager.ADMIN_PERMISSION)) {
            player.closeInventory();
            return;
        }
        List<HideState> states = plugin.getHideManager().getStates().stream().toList();
        int start = page * PAGE_SIZE;
        int end = Math.min(states.size(), start + PAGE_SIZE);
        for (int index = start; index < end; index++) {
            HideState state = states.get(index);
            Player online = Bukkit.getPlayer(state.playerUuid());
            int slot = index - start;
            set(slot, ItemUtils.createPlayerHead(
                    Bukkit.getOfflinePlayer(state.playerUuid()),
                    "&b" + state.alias(),
                    List.of(
                            "&7Real Name: &f" + state.realNameSnapshot(),
                            "&7Mode: &f" + state.mode().name(),
                            "&7Skin: &f" + (state.skinUsername().isBlank() ? "Original" : state.skinUsername()),
                            "&7Status: " + (online == null ? "&cOffline" : "&aOnline"),
                            "",
                            online == null ? "&7Left-click unavailable." : "&aLeft-click to teleport.",
                            "&cRight-click to remove."
                    )
            ));
            targets.put(slot, state.playerUuid());
        }
        renderNavigation(states.size());
    }

    @Override
    public void handleClick(int slot, Player player, ClickType clickType) {
        UUID targetUuid = targets.get(slot);
        if (targetUuid != null) {
            HideState state = plugin.getHideManager().getState(targetUuid);
            if (state == null) {
                build(player);
                return;
            }
            if (clickType.isRightClick()) {
                plugin.getHideManager().remove(targetUuid);
                player.sendMessage(ColorUtils.toComponent(plugin.getHideManager().message(
                        "ADMIN-REMOVED",
                        "&aSuccessfully removed Hide from &f{player}&a.",
                        "{player}", state.realNameSnapshot()
                ), player));
                Player target = Bukkit.getPlayer(targetUuid);
                if (target != null) {
                    target.sendMessage(ColorUtils.toComponent(plugin.getHideManager().message(
                            "REMOVED-BY-ADMIN",
                            "&cYour Hide state has been removed by an administrator."
                    ), target));
                }
                build(player);
                return;
            }
            Player target = Bukkit.getPlayer(targetUuid);
            if (target != null) {
                player.closeInventory();
                plugin.getSpigotScheduler().teleport(player, target.getLocation());
            }
            return;
        }
        if (slot == 45 && page > 0) {
            new HideListMenu(plugin, page - 1).open(player);
        } else if (slot == 53 && (page + 1) * PAGE_SIZE < plugin.getHideManager().getStates().size()) {
            new HideListMenu(plugin, page + 1).open(player);
        } else if (slot == 49) {
            new HideMenu(plugin).open(player);
        }
    }

    private void renderNavigation(int total) {
        for (int slot = 45; slot < 54; slot++) {
            set(slot, ItemUtils.createItem(Material.BLACK_STAINED_GLASS_PANE, " ", List.of()));
        }
        if (page > 0) {
            set(45, ItemUtils.createItem(Material.ARROW, "&bPrevious Page", List.of()));
        }
        set(49, ItemUtils.createItem(Material.BARRIER, "&cBack", List.of()));
        if ((page + 1) * PAGE_SIZE < total) {
            set(53, ItemUtils.createItem(Material.ARROW, "&bNext Page", List.of()));
        }
    }

    private static String title(UltimateDonutSmp plugin, int page) {
        int total = Math.max(1, plugin.getHideManager().getStates().size());
        int pages = Math.max(1, (int) Math.ceil(total / (double) PAGE_SIZE));
        return plugin.getConfigManager().getHide()
                .getString("GUI.LIST.TITLE", "&8Hidden Players - {page}/{pages}")
                .replace("{page}", String.valueOf(Math.min(page + 1, pages)))
                .replace("{pages}", String.valueOf(pages));
    }
}
