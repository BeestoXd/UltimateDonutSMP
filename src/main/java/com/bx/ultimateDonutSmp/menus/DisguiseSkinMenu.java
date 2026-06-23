package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.commands.HideCommand;
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

public class DisguiseSkinMenu extends BaseMenu {

    private static final int PAGE_SIZE = 45;

    private final String aliasKey;
    private final int page;
    private final Map<Integer, String> skinsBySlot = new HashMap<>();

    public DisguiseSkinMenu(UltimateDonutSmp plugin, String aliasKey, int page) {
        super(plugin, title(plugin, page), 54);
        this.aliasKey = aliasKey;
        this.page = Math.max(0, page);
    }

    @Override
    public void build(Player player) {
        clear();
        skinsBySlot.clear();
        List<HideManager.SkinOption> skins = new ArrayList<>(plugin.getHideManager().skins().values());
        int start = page * PAGE_SIZE;
        int end = Math.min(skins.size(), start + PAGE_SIZE);
        for (int index = start; index < end; index++) {
            HideManager.SkinOption option = skins.get(index);
            int slot = index - start;
            HideManager.HeadTexture texture = plugin.getHideManager().cachedHeadTexture(option.username());
            set(slot, createSkinHead(option, texture));
            skinsBySlot.put(slot, option.key());
            if (texture == null) {
                refreshHeadAsync(player, slot, option);
            }
        }
        renderNavigation(skins.size());
    }

    private ItemStack createSkinHead(HideManager.SkinOption option, HideManager.HeadTexture texture) {
        return ItemUtils.createPlayerHead(
                Bukkit.getOfflinePlayer(option.username()),
                texture == null ? null : texture.value(),
                "&d" + option.displayName(),
                List.of(
                        "&7ᴜѕᴇʀɴᴀᴍᴇ: &f" + option.username(),
                        "&7ᴋᴇʏ: &f" + option.key(),
                        "",
                        "&eᴄʟɪᴄᴋ ᴛᴏ ѕᴇʟᴇᴄᴛ ᴛʜɪѕ ѕᴋɪɴ."
                )
        );
    }

    private void refreshHeadAsync(Player player, int slot, HideManager.SkinOption option) {
        plugin.getHideManager().resolveHeadTextureAsync(option.username()).thenAccept(texture -> {
            if (texture == null || !texture.isValid()) {
                return;
            }
            plugin.getSpigotScheduler().runEntity(player, () -> {
                if (!player.isOnline()
                        || player.getOpenInventory().getTopInventory().getHolder() != this
                        || !option.key().equals(skinsBySlot.get(slot))) {
                    return;
                }
                set(slot, createSkinHead(option, texture));
            });
        });
    }

    @Override
    public void handleClick(int slot, Player player, ClickType clickType) {
        String skinKey = skinsBySlot.get(slot);
        if (skinKey != null) {
            player.closeInventory();
            new HideCommand(plugin).sendResult(
                    player,
                    plugin.getHideManager().disguise(player, aliasKey, skinKey)
            );
            return;
        }
        if (slot == 45 && page > 0) {
            new DisguiseSkinMenu(plugin, aliasKey, page - 1).open(player);
        } else if (slot == 53 && (page + 1) * PAGE_SIZE < plugin.getHideManager().skins().size()) {
            new DisguiseSkinMenu(plugin, aliasKey, page + 1).open(player);
        } else if (slot == 49) {
            new DisguiseAliasMenu(plugin, 0).open(player);
        }
    }

    private void renderNavigation(int total) {
        for (int slot = 45; slot < 54; slot++) {
            set(slot, ItemUtils.createItem(Material.BLACK_STAINED_GLASS_PANE, " ", List.of()));
        }
        if (page > 0) {
            set(45, ItemUtils.createItem(Material.ARROW, "&bᴘʀᴇᴠɪᴏᴜѕ ᴘᴀɢᴇ", List.of()));
        }
        set(49, ItemUtils.createItem(Material.BARRIER, "&cʙᴀᴄᴋ", List.of()));
        if ((page + 1) * PAGE_SIZE < total) {
            set(53, ItemUtils.createItem(Material.ARROW, "&bɴᴇxᴛ ᴘᴀɢᴇ", List.of()));
        }
    }

    private static String title(UltimateDonutSmp plugin, int page) {
        int total = Math.max(1, plugin.getHideManager().skins().size());
        int pages = Math.max(1, (int) Math.ceil(total / (double) PAGE_SIZE));
        return plugin.getConfigManager().getHide()
                .getString("GUI.SKINS.TITLE", "&8ѕᴇʟᴇᴄᴛ ᴀ ѕᴋɪɴ - {page}/{pages}")
                .replace("{page}", String.valueOf(Math.min(page + 1, pages)))
                .replace("{pages}", String.valueOf(pages));
    }
}
