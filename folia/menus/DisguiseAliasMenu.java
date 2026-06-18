package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.HideManager;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DisguiseAliasMenu extends BaseMenu {

    private static final int PAGE_SIZE = 45;

    private final int page;
    private final Map<Integer, String> aliasesBySlot = new HashMap<>();

    public DisguiseAliasMenu(UltimateDonutSmp plugin, int page) {
        super(plugin, title(plugin, page), 54);
        this.page = Math.max(0, page);
    }

    @Override
    public void build(Player player) {
        clear();
        aliasesBySlot.clear();
        List<HideManager.AliasOption> aliases = new ArrayList<>(plugin.getHideManager().aliases().values());
        int start = page * PAGE_SIZE;
        int end = Math.min(aliases.size(), start + PAGE_SIZE);
        for (int index = start; index < end; index++) {
            HideManager.AliasOption option = aliases.get(index);
            int slot = index - start;
            HideManager.HeadTexture texture = plugin.getHideManager().cachedHeadTexture(option.skinUsername());
            set(slot, createAliasHead(option, texture));
            aliasesBySlot.put(slot, option.key());
            if (texture == null) {
                refreshHeadAsync(player, slot, option);
            }
        }
        renderNavigation(aliases.size());
    }

    private ItemStack createAliasHead(HideManager.AliasOption option, HideManager.HeadTexture texture) {
        return ItemUtils.createPlayerHead(
                Bukkit.getOfflinePlayer(option.skinUsername()),
                texture == null ? null : texture.value(),
                "&d" + option.name(),
                List.of(
                        "&7Key: &f" + option.key(),
                        "&7Preview skin: &f" + option.skinUsername(),
                        "",
                        "&eClick to select this name."
                )
        );
    }

    private void refreshHeadAsync(Player player, int slot, HideManager.AliasOption option) {
        plugin.getHideManager().resolveHeadTextureAsync(option.skinUsername()).thenAccept(texture -> {
            if (texture == null || !texture.isValid()) {
                return;
            }
            plugin.getFoliaScheduler().runEntity(player, () -> {
                if (!player.isOnline()
                        || player.getOpenInventory().getTopInventory().getHolder() != this
                        || !option.key().equals(aliasesBySlot.get(slot))) {
                    return;
                }
                set(slot, createAliasHead(option, texture));
            });
        });
    }

    @Override
    public void handleClick(int slot, Player player, ClickType clickType) {
        String aliasKey = aliasesBySlot.get(slot);
        if (aliasKey != null) {
            new DisguiseSkinMenu(plugin, aliasKey, 0).open(player);
            return;
        }
        if (slot == 45 && page > 0) {
            new DisguiseAliasMenu(plugin, page - 1).open(player);
        } else if (slot == 53 && (page + 1) * PAGE_SIZE < plugin.getHideManager().aliases().size()) {
            new DisguiseAliasMenu(plugin, page + 1).open(player);
        } else if (slot == 49) {
            new HideMenu(plugin).open(player);
        }
    }

    private void renderNavigation(int total) {
        fillNavigation();
        if (page > 0) {
            set(45, ItemUtils.createItem(Material.ARROW, "&bPrevious Page", List.of()));
        }
        set(49, ItemUtils.createItem(Material.BARRIER, "&cBack", List.of()));
        if ((page + 1) * PAGE_SIZE < total) {
            set(53, ItemUtils.createItem(Material.ARROW, "&bNext Page", List.of()));
        }
    }

    private void fillNavigation() {
        for (int slot = 45; slot < 54; slot++) {
            set(slot, ItemUtils.createItem(Material.BLACK_STAINED_GLASS_PANE, " ", List.of()));
        }
    }

    private static String title(UltimateDonutSmp plugin, int page) {
        int total = Math.max(1, plugin.getHideManager().aliases().size());
        int pages = Math.max(1, (int) Math.ceil(total / (double) PAGE_SIZE));
        return plugin.getConfigManager().getHide()
                .getString("GUI.ALIASES.TITLE", "&8Select a Name - {page}/{pages}")
                .replace("{page}", String.valueOf(Math.min(page + 1, pages)))
                .replace("{pages}", String.valueOf(pages));
    }
}
