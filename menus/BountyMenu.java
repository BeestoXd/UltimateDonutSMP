package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.Bounty;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class BountyMenu extends BaseMenu {

    private static final int FIRST_PAGE_SLOT = 45;
    private static final int PREVIOUS_PAGE_SLOT = 46;
    private static final int SORT_SLOT = 48;
    private static final int PAGE_INFO_SLOT = 50;
    private static final int NEXT_PAGE_SLOT = 52;
    private static final int LAST_PAGE_SLOT = 53;

    private final List<Bounty> displayedBounties = new ArrayList<>();
    private int page;
    private int totalPages = 1;
    private boolean hasPreviousPage;
    private boolean hasNextPage;
    private boolean descending = true;

    public BountyMenu(UltimateDonutSmp plugin) {
        super(
                plugin,
                plugin.getConfigManager().getMenus().getString("BOUNTIES-MENU.TITLE", "&8bounties"),
                plugin.getConfigManager().getMenus().getInt("BOUNTIES-MENU.SIZE", 54)
        );
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);
        displayedBounties.clear();

        FileConfiguration menus = plugin.getConfigManager().getMenus();
        int maxItems = menus.getInt("BOUNTIES-MENU.MAX-ITEMS-PER-PAGE", 45);

        List<Bounty> allBounties = new ArrayList<>(plugin.getBountyManager().getAllBounties());
        Comparator<Bounty> comparator = Comparator.comparingDouble(Bounty::getAmount);
        if (descending) {
            comparator = comparator.reversed();
        }
        allBounties.sort(comparator.thenComparing(bounty ->
                plugin.getBountyManager().getDisplayName(bounty.getTargetUuid()), String.CASE_INSENSITIVE_ORDER));

        totalPages = Math.max(1, (int) Math.ceil(allBounties.size() / (double) maxItems));
        if (page >= totalPages) {
            page = totalPages - 1;
        }

        int startIndex = page * maxItems;
        int endIndex = Math.min(startIndex + maxItems, allBounties.size());
        hasPreviousPage = page > 0;
        hasNextPage = endIndex < allBounties.size();

        for (int index = startIndex; index < endIndex; index++) {
            Bounty bounty = allBounties.get(index);
            int slot = index - startIndex;
            set(slot, createBountyItem(menus, bounty));
            displayedBounties.add(bounty);
        }

        buildRefreshButton(menus);
        buildSortButton();
        buildPageButtons(menus, allBounties.size());
    }

    @Override
    public void handleClick(int slot, Player player) {
        int refreshSlot = plugin.getConfigManager().getMenus().getInt("BOUNTIES-MENU.REFRESH-BUTTON.SLOT", 49);

        if (slot == refreshSlot) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            build(player);
            return;
        }

        if (slot == SORT_SLOT) {
            descending = !descending;
            page = 0;
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            build(player);
            return;
        }

        if (slot == FIRST_PAGE_SLOT && hasPreviousPage) {
            page = 0;
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.PAGE-TURN"));
            build(player);
            return;
        }

        if (slot == PREVIOUS_PAGE_SLOT && hasPreviousPage) {
            page--;
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.PAGE-TURN"));
            build(player);
            return;
        }

        if (slot == NEXT_PAGE_SLOT && hasNextPage) {
            page++;
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.PAGE-TURN"));
            build(player);
            return;
        }

        if (slot == LAST_PAGE_SLOT && hasNextPage) {
            page = totalPages - 1;
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.PAGE-TURN"));
            build(player);
            return;
        }

        if (slot < 0 || slot >= displayedBounties.size()) {
            return;
        }

        SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
        Bounty bounty = displayedBounties.get(slot);
        String msg = plugin.getConfigManager().getMessage("BOUNTY.PLAYER-HAS-BOUNTY",
                "{player}", plugin.getBountyManager().getDisplayName(bounty.getTargetUuid()),
                "{amount}", NumberUtils.format(bounty.getAmount()));
        player.sendMessage(ColorUtils.toComponent(msg));
    }

    private ItemStack createBountyItem(FileConfiguration menus, Bounty bounty) {
        String playerName = plugin.getBountyManager().getDisplayName(bounty.getTargetUuid());
        String displayName = menus.getString("BOUNTIES-MENU.BOUNTY-BUTTON.NAME", "&#6BF18D{player}")
                .replace("{player}", playerName);
        List<String> lore = menus.getStringList("BOUNTIES-MENU.BOUNTY-BUTTON.LORE").stream()
                .map(line -> line.replace("{player}", playerName)
                        .replace("{price}", NumberUtils.format(bounty.getAmount())))
                .toList();

        Material material = ItemUtils.parseMaterial(
                menus.getString("BOUNTIES-MENU.BOUNTY-BUTTON.MATERIAL", "PLAYER_HEAD")
        );

        if (material != Material.PLAYER_HEAD) {
            return ItemUtils.createItem(material, displayName, lore);
        }

        ItemStack item = ItemUtils.createItem(material, displayName, lore);
        if (!(item.getItemMeta() instanceof SkullMeta meta)) {
            return item;
        }

        meta.setOwningPlayer(resolveOfflinePlayer(bounty.getTargetUuid()));
        item.setItemMeta(meta);
        return item;
    }

    private void buildRefreshButton(FileConfiguration menus) {
        String path = "BOUNTIES-MENU.REFRESH-BUTTON";
        Material material = ItemUtils.parseMaterial(menus.getString(path + ".MATERIAL", "SKELETON_SKULL"));
        String name = menus.getString(path + ".NAME", "&#6BF18Dbounties");
        List<String> lore = menus.getStringList(path + ".LORE");
        set(menus.getInt(path + ".SLOT", 49), ItemUtils.createItem(material, name, lore));
    }

    private void buildSortButton() {
        String sortState = descending ? "Highest Bounty" : "Lowest Bounty";
        set(SORT_SLOT, ItemUtils.createItem(
                Material.HOPPER,
                "&aSort",
                List.of("&fCurrently: &7" + sortState)
        ));
    }

    private void buildPageButtons(FileConfiguration menus, int totalItems) {
        Material material = ItemUtils.parseMaterial(menus.getString("GLOBAL.PAGE-MENU.MATERIAL", "ARROW"));

        if (hasPreviousPage) {
            set(FIRST_PAGE_SLOT, ItemUtils.createItem(
                    material,
                    menus.getString("GLOBAL.PAGE-MENU.FIRST-PAGE-BUTTON", "&aFirst Page"),
                    menus.getStringList("GLOBAL.PAGE-MENU.FIRST-PAGE-LORE")
            ));
            set(PREVIOUS_PAGE_SLOT, ItemUtils.createItem(
                    material,
                    menus.getString("GLOBAL.PAGE-MENU.BACK-BUTTON", "&aBack"),
                    menus.getStringList("GLOBAL.PAGE-MENU.BACK-LORE")
            ));
        }

        set(PAGE_INFO_SLOT, ItemUtils.createItem(
                Material.BOOK,
                "&ePage " + (page + 1) + "&7/&e" + totalPages,
                List.of("&fActive bounties: &7" + NumberUtils.format(totalItems))
        ));

        if (hasNextPage) {
            set(NEXT_PAGE_SLOT, ItemUtils.createItem(
                    material,
                    menus.getString("GLOBAL.PAGE-MENU.NEXT-BUTTON", "&aNext"),
                    menus.getStringList("GLOBAL.PAGE-MENU.NEXT-LORE")
            ));
            set(LAST_PAGE_SLOT, ItemUtils.createItem(
                    material,
                    menus.getString("GLOBAL.PAGE-MENU.LAST-PAGE-BUTTON", "&aLast Page"),
                    menus.getStringList("GLOBAL.PAGE-MENU.LAST-PAGE-LORE")
            ));
        }
    }

    private OfflinePlayer resolveOfflinePlayer(UUID targetUuid) {
        Player online = Bukkit.getPlayer(targetUuid);
        if (online != null) {
            return online;
        }
        return Bukkit.getOfflinePlayer(targetUuid);
    }
}
