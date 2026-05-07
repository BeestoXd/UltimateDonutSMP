package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.FreezeState;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FrozenPlayersMenu extends BaseMenu {

    private static final String MENU_KEY = "FROZEN-PLAYERS";

    private final Map<Integer, UUID> slotTargets = new HashMap<>();
    private ScheduledTask refreshTask;

    public FrozenPlayersMenu(UltimateDonutSmp plugin) {
        super(
                plugin,
                plugin.getStaffModeManager().getMenuTitle(MENU_KEY, "&8Frozen Players"),
                plugin.getStaffModeManager().getMenuSize(MENU_KEY)
        );
    }

    @Override
    public void open(Player player) {
        build(player);
        player.openInventory(inventory);
        startRefresh(player);
    }

    @Override
    public void build(Player player) {
        cancelRefresh();
        render();
    }

    @Override
    public void handleClick(int slot, Player player, ClickType clickType) {
        int refreshSlot = plugin.getStaffModeManager().getMenuRefreshSlot(MENU_KEY);
        if (slot == refreshSlot) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            render();
            return;
        }

        UUID targetUuid = slotTargets.get(slot);
        if (targetUuid == null) {
            return;
        }

        Player target = plugin.getServer().getPlayer(targetUuid);
        if (target == null || !target.isOnline()) {
            player.sendMessage(ColorUtils.toComponent("&cThat frozen player is currently offline."));
            render();
            return;
        }

        SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
        player.closeInventory();
        plugin.getPaperScheduler().teleport(player, target.getLocation()).thenAccept(success ->
                plugin.getPaperScheduler().runEntity(player, () -> {
                    if (Boolean.TRUE.equals(success) && player.isOnline()) {
                        player.sendMessage(ColorUtils.toComponent("&eTeleported to frozen player &f" + target.getName() + "&e."));
                    }
                }));
    }

    @Override
    public void onClose(Player player) {
        cancelRefresh();
    }

    private void render() {
        clear();
        fill(plugin.getStaffModeManager().getMenuPlaceholderMaterial(MENU_KEY, Material.LIGHT_BLUE_STAINED_GLASS_PANE));
        slotTargets.clear();

        int refreshSlot = plugin.getStaffModeManager().getMenuRefreshSlot(MENU_KEY);
        set(refreshSlot, plugin.getStaffModeManager().createRefreshItem(MENU_KEY));

        List<Integer> contentSlots = plugin.getStaffModeManager().getMenuContentSlots(MENU_KEY, inventory.getSize());
        List<FreezeState> states = plugin.getStaffModeManager().getFrozenStates();
        states.sort(Comparator
                .comparing((FreezeState state) -> Bukkit.getPlayer(state.getTargetUuid()) == null)
                .thenComparing(FreezeState::getTargetNameSnapshot, String.CASE_INSENSITIVE_ORDER));

        int rendered = 0;
        for (FreezeState state : states) {
            if (rendered >= contentSlots.size()) {
                break;
            }

            int slot = contentSlots.get(rendered++);
            set(slot, createFrozenItem(state));
            slotTargets.put(slot, state.getTargetUuid());
        }

        if (rendered == 0) {
            set(inventory.getSize() / 2, plugin.getStaffModeManager().createMenuEmptyItem(MENU_KEY));
        }
    }

    private ItemStack createFrozenItem(FreezeState state) {
        OfflinePlayer player = state.getTargetUuid() == null
                ? null
                : Bukkit.getOfflinePlayer(state.getTargetUuid());
        Player online = state.getTargetUuid() == null ? null : Bukkit.getPlayer(state.getTargetUuid());
        long frozenSeconds = Math.max(0L, (System.currentTimeMillis() - state.getFrozenAt()) / 1000L);

        List<String> lore = List.of(
                "&7Status: " + (online == null ? "&cOffline" : "&aOnline"),
                "&7Frozen By: &f" + state.getFrozenByNameSnapshot(),
                "&7Duration: &f" + NumberUtils.formatTimeLong(frozenSeconds),
                "&7Server: &f" + state.getSourceServer(),
                online == null
                        ? "&7Target is offline."
                        : "&eClick to teleport"
        );

        if (player == null) {
            return ItemUtils.createItem(Material.PLAYER_HEAD, "&b" + state.getTargetNameSnapshot(), lore);
        }
        return ItemUtils.createPlayerHead(player, "&b" + state.getTargetNameSnapshot(), lore);
    }

    private void startRefresh(Player player) {
        refreshTask = plugin.getPaperScheduler().runEntityTimer(player, () -> {
            if (!player.isOnline() || player.getOpenInventory().getTopInventory() != inventory) {
                cancelRefresh();
                return;
            }
            render();
        }, 40L, 40L);
    }

    private void cancelRefresh() {
        if (refreshTask != null) {
            refreshTask.cancel();
            refreshTask = null;
        }
    }
}
