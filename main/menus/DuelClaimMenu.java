package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.DuelClaim;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class DuelClaimMenu extends BaseMenu {

    private final int page;

    public DuelClaimMenu(UltimateDonutSmp plugin, int page) {
        super(plugin, plugin.getDuelManager().getClaimsTitle(), plugin.getDuelManager().getClaimsSize());
        this.page = Math.max(1, page);
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);

        List<DuelClaim> claims = plugin.getDuelManager().getClaims(player.getUniqueId());
        int itemsPerPage = plugin.getDuelManager().getClaimsItemsPerPage();
        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(claims.size(), startIndex + itemsPerPage);

        for (int slot = 0; slot < itemsPerPage && slot < inventory.getSize() - 9; slot++) {
            int claimIndex = startIndex + slot;
            if (claimIndex >= endIndex) {
                break;
            }

            DuelClaim claim = claims.get(claimIndex);
            set(slot, createClaimItem(claim));
        }

        int lastRow = inventory.getSize() - 9;
        set(lastRow, page > 1
                ? ItemUtils.createItem(Material.ARROW, "&aᴘʀᴇᴠɪᴏᴜѕ ᴘᴀɢᴇ")
                : ItemUtils.createPlaceholder(Material.BLACK_STAINED_GLASS_PANE));
        set(lastRow + 4, ItemUtils.createItem(Material.CLOCK, "&eʀᴇꜰʀᴇѕʜ"));
        set(lastRow + 7, hasNextPage(claims.size(), itemsPerPage)
                ? ItemUtils.createItem(Material.ARROW, "&aɴᴇxᴛ ᴘᴀɢᴇ")
                : ItemUtils.createPlaceholder(Material.BLACK_STAINED_GLASS_PANE));
        set(lastRow + 8, ItemUtils.createItem(Material.BARRIER, "&cᴄʟᴏѕᴇ"));

        if (claims.isEmpty()) {
            set(inventory.getSize() / 2, ItemUtils.createItem(
                    Material.BARRIER,
                    "&cɴᴏ ᴘᴇɴᴅɪɴɢ ᴄʟᴀɪᴍѕ",
                    List.of("&7ʟᴏᴏᴛ ꜰʀᴏᴍ ᴅᴜᴇʟ ᴡɪɴѕ ᴡɪʟʟ ѕʜᴏᴡ ᴜᴘ ʜᴇʀᴇ.")
            ));
        }
    }

    private ItemStack createClaimItem(DuelClaim claim) {
        String defeatedName = claim.defeatedName() == null || claim.defeatedName().isBlank()
                ? "ᴜɴᴋɴᴏᴡɴ"
                : claim.defeatedName();
        return ItemUtils.createItem(
                Material.CHEST,
                "&eʟᴏᴏᴛ ꜰʀᴏᴍ &f" + defeatedName,
                List.of(
                        "&7ᴍᴀᴛᴄʜ: &f#" + claim.matchId(),
                        "&7ѕᴛᴏʀᴇᴅ ɪᴛᴇᴍѕ: &f" + claim.itemCount(),
                        "&7ᴄʟɪᴄᴋ ᴛᴏ ᴘʀᴇᴠɪᴇᴡ ᴛʜɪѕ ʟᴏᴏᴛ ᴘᴀᴄᴋᴀɢᴇ.",
                        "&8ᴅᴇʟᴇᴛᴇ ɪѕ ᴀᴠᴀɪʟᴀʙʟᴇ ɪɴѕɪᴅᴇ ᴛʜᴇ ᴘʀᴇᴠɪᴇᴡ."
                )
        );
    }

    @Override
    public void handleClick(int slot, Player player) {
        List<DuelClaim> claims = plugin.getDuelManager().getClaims(player.getUniqueId());
        int itemsPerPage = plugin.getDuelManager().getClaimsItemsPerPage();
        int lastRow = inventory.getSize() - 9;

        if (slot == lastRow) {
            if (page > 1) {
                SoundUtils.play(player, plugin.getConfigManager().getSound("DUELS.CLICK"));
                new DuelClaimMenu(plugin, page - 1).open(player);
            }
            return;
        }
        if (slot == lastRow + 4) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("DUELS.CLICK"));
            new DuelClaimMenu(plugin, page).open(player);
            return;
        }
        if (slot == lastRow + 7) {
            if (hasNextPage(claims.size(), itemsPerPage)) {
                SoundUtils.play(player, plugin.getConfigManager().getSound("DUELS.CLICK"));
                new DuelClaimMenu(plugin, page + 1).open(player);
            }
            return;
        }
        if (slot == lastRow + 8) {
            player.closeInventory();
            return;
        }

        int claimIndex = ((page - 1) * itemsPerPage) + slot;
        if (slot < 0 || slot >= itemsPerPage || claimIndex >= claims.size()) {
            return;
        }

        SoundUtils.play(player, plugin.getConfigManager().getSound("DUELS.CLICK"));
        new DuelClaimPreviewMenu(plugin, page, claims.get(claimIndex).matchId()).open(player);
    }

    private boolean hasNextPage(int totalItems, int itemsPerPage) {
        return page < Math.max(1, (int) Math.ceil(totalItems / (double) itemsPerPage));
    }
}
