package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.DuelClaim;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class DuelClaimPreviewMenu extends BaseMenu {

    private final int returnPage;
    private final long matchId;

    public DuelClaimPreviewMenu(UltimateDonutSmp plugin, int returnPage, long matchId) {
        super(plugin, "&8ᴅᴜᴇʟ ʟᴏᴏᴛ ᴘʀᴇᴠɪᴇᴡ", 54);
        this.returnPage = Math.max(1, returnPage);
        this.matchId = matchId;
    }

    @Override
    public void build(Player player) {
        clear();

        DuelClaim claim = plugin.getDuelManager().getClaim(player.getUniqueId(), matchId);
        for (int slot = 45; slot < inventory.getSize(); slot++) {
            set(slot, ItemUtils.createPlaceholder(Material.BLACK_STAINED_GLASS_PANE));
        }

        if (claim == null || claim.items() == null || claim.items().isEmpty()) {
            set(22, ItemUtils.createItem(
                    Material.BARRIER,
                    "&cᴄʟᴀɪᴍ ɴᴏᴛ ꜰᴏᴜɴᴅ",
                    List.of("&7ᴛʜɪѕ ᴅᴜᴇʟ ʟᴏᴏᴛ ᴘᴀᴄᴋᴀɢᴇ ɴᴏ ʟᴏɴɢᴇʀ ᴇxɪѕᴛѕ.")
            ));
            set(45, ItemUtils.createItem(Material.ARROW, "&aʙᴀᴄᴋ"));
            set(53, ItemUtils.createItem(Material.BARRIER, "&cʙᴀᴄᴋ"));
            return;
        }

        int slot = 0;
        for (ItemStack item : claim.items()) {
            if (item == null || item.getType().isAir() || item.getAmount() <= 0) {
                continue;
            }
            if (slot >= 45) {
                break;
            }
            set(slot, item.clone());
            slot++;
        }

        String defeatedName = claim.defeatedName() == null || claim.defeatedName().isBlank()
                ? "unknown"
                : claim.defeatedName();

        set(45, ItemUtils.createItem(Material.ARROW, "&aʙᴀᴄᴋ"));
        set(47, ItemUtils.createItem(
                Material.CHEST,
                "&eʟᴏᴏᴛ ѕᴜᴍᴍᴀʀʏ",
                List.of(
                        "&7ᴅᴇꜰᴇᴀᴛᴇᴅ ᴘʟᴀʏᴇʀ: &f" + defeatedName,
                        "&7ᴍᴀᴛᴄʜ: &f#" + claim.matchId(),
                        "&7ѕᴛᴏʀᴇᴅ ɪᴛᴇᴍѕ: &f" + claim.itemCount()
                )
        ));
        set(49, ItemUtils.createItem(
                Material.LIME_STAINED_GLASS_PANE,
                "&aᴄʟᴀɪᴍ ᴀʟʟ",
                List.of(
                        "&7ᴍᴏᴠᴇ ᴀʟʟ ꜰɪᴛᴛɪɴɢ ɪᴛᴇᴍѕ ɪɴᴛᴏ ʏᴏᴜʀ ɪɴᴠᴇɴᴛᴏʀʏ.",
                        "&7ɪꜰ ѕᴏᴍᴇ ᴅᴏ ɴᴏᴛ ꜰɪᴛ, ᴛʜᴇʏ ѕᴛᴀʏ ɪɴ ᴄʟᴀɪᴍѕ."
                )
        ));
        set(51, ItemUtils.createItem(
                Material.RED_STAINED_GLASS_PANE,
                "&cᴅᴇʟᴇᴛᴇ ᴄʟᴀɪᴍ",
                List.of(
                        "&7ᴅᴇʟᴇᴛᴇ ᴛʜɪѕ ᴇɴᴛɪʀᴇ ʟᴏᴏᴛ ᴘᴀᴄᴋᴀɢᴇ.",
                        "&7ᴛʜɪѕ ᴀᴄᴛɪᴏɴ ᴄᴀɴɴᴏᴛ ʙᴇ ᴜɴᴅᴏɴᴇ."
                )
        ));
        set(53, ItemUtils.createItem(Material.BARRIER, "&cʙᴀᴄᴋ"));
    }

    @Override
    public void handleClick(int slot, Player player) {
        if (slot == 45) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("DUELS.CLICK"));
            new DuelClaimMenu(plugin, returnPage).open(player);
            return;
        }
        if (slot == 49) {
            if (plugin.getDuelManager().claim(player, matchId)) {
                SoundUtils.play(player, plugin.getConfigManager().getSound("DUELS.CLAIM"));
            }

            if (plugin.getDuelManager().getClaim(player.getUniqueId(), matchId) == null) {
                new DuelClaimMenu(plugin, returnPage).open(player);
            } else {
                new DuelClaimPreviewMenu(plugin, returnPage, matchId).open(player);
            }
            return;
        }
        if (slot == 51) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("DUELS.CLICK"));
            plugin.getDuelManager().deleteClaim(player, matchId);
            new DuelClaimMenu(plugin, returnPage).open(player);
            return;
        }
        if (slot == 53) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("DUELS.CLICK"));
            new DuelClaimMenu(plugin, returnPage).open(player);
        }
    }
}
