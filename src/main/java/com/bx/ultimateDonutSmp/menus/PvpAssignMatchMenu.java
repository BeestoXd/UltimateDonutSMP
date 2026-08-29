package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PvpKit;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Lets a tester put two named players into a ranked match without either of them queueing.
 *
 * <p>The two player slots cycle through everyone online instead of opening a picker, which keeps
 * the whole assignment on one screen the way the queue menu is.</p>
 */
public class PvpAssignMatchMenu extends BaseMenu {

    private static final String PATH = "MENUS.ASSIGN";
    private static final int FIRST_SLOT = 11;
    private static final int KIT_SLOT = 13;
    private static final int SECOND_SLOT = 15;
    private static final int CONFIRM_SLOT = 22;

    private UUID firstUuid;
    private UUID secondUuid;
    private String kitId;

    public PvpAssignMatchMenu(UltimateDonutSmp plugin, UUID firstUuid, UUID secondUuid) {
        super(plugin, configuredTitle(plugin), configuredSize(plugin));
        this.firstUuid = firstUuid;
        this.secondUuid = secondUuid;
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.BLACK_STAINED_GLASS_PANE);

        List<PvpKit> kits = plugin.getPvpManager().getKits();
        if (kitId == null && !kits.isEmpty()) {
            kitId = kits.get(0).getId();
        }

        set(FIRST_SLOT, playerIcon(firstUuid, "&aPlayer one"));
        set(SECOND_SLOT, playerIcon(secondUuid, "&aPlayer two"));

        PvpKit kit = plugin.getPvpManager().getKit(kitId);
        set(KIT_SLOT, ItemUtils.createItem(
                kit == null ? Material.BARRIER : kit.getIcon(),
                kit == null ? "&cNo kit" : kit.getDisplayName(),
                kit == null
                        ? List.of("&7Create a kit before assigning a match.")
                        : List.of("&7Kit for this match.", "&eClick &7to pick the next one.")
        ));

        boolean ready = firstUuid != null && secondUuid != null && !firstUuid.equals(secondUuid) && kit != null;
        set(CONFIRM_SLOT, ItemUtils.createItem(
                ItemUtils.parseMaterial(config().getString(PATH + "."
                        + (ready ? "CONFIRM" : "BLOCKED") + ".MATERIAL",
                        ready ? "LIME_STAINED_GLASS_PANE" : "RED_STAINED_GLASS_PANE")),
                config().getString(PATH + ".CONFIRM.DISPLAY-NAME", ready ? "&aSTART MATCH" : "&cNOT READY"),
                ready
                        ? List.of("&7Click to start the match.")
                        : List.of("&7Pick two different players and a kit first.")
        ));
    }

    private ItemStack playerIcon(UUID uuid, String title) {
        List<String> lore = new ArrayList<>();
        if (uuid == null) {
            lore.add("&7Nobody selected.");
        } else {
            lore.add("&7Selected: &f" + plugin.getPvpMatchManager().resolveName(uuid));
        }
        lore.add("&eClick &7for the next player online.");
        lore.add("&eShift click &7to clear.");

        if (uuid == null) {
            return ItemUtils.createItem(Material.PLAYER_HEAD, title, lore);
        }
        return ItemUtils.createPlayerHead(Bukkit.getOfflinePlayer(uuid), title, lore);
    }

    @Override
    public void handleClick(int slot, Player player, ClickType clickType) {
        if (slot == FIRST_SLOT || slot == SECOND_SLOT) {
            boolean first = slot == FIRST_SLOT;
            if (clickType.isShiftClick()) {
                if (first) {
                    firstUuid = null;
                } else {
                    secondUuid = null;
                }
            } else if (first) {
                firstUuid = nextPlayer(firstUuid, secondUuid);
            } else {
                secondUuid = nextPlayer(secondUuid, firstUuid);
            }
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            build(player);
            return;
        }

        if (slot == KIT_SLOT) {
            kitId = nextKit();
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            build(player);
            return;
        }

        if (slot != CONFIRM_SLOT || firstUuid == null || secondUuid == null) {
            return;
        }

        Player first = Bukkit.getPlayer(firstUuid);
        Player second = Bukkit.getPlayer(secondUuid);
        if (first == null || second == null) {
            build(player);
            return;
        }

        SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
        player.closeInventory();
        plugin.getPvpMatchManager().startMatch(first, second, kitId, player);
    }

    /** The next online player after the current one, skipping whoever is already in the other slot. */
    private UUID nextPlayer(UUID current, UUID excluded) {
        List<UUID> online = new ArrayList<>();
        for (Player candidate : Bukkit.getOnlinePlayers()) {
            if (excluded == null || !candidate.getUniqueId().equals(excluded)) {
                online.add(candidate.getUniqueId());
            }
        }
        if (online.isEmpty()) {
            return null;
        }

        int index = current == null ? -1 : online.indexOf(current);
        return online.get((index + 1) % online.size());
    }

    private String nextKit() {
        List<PvpKit> kits = plugin.getPvpManager().getKits();
        if (kits.isEmpty()) {
            return null;
        }

        int index = -1;
        for (int position = 0; position < kits.size(); position++) {
            if (kits.get(position).getId().equals(kitId)) {
                index = position;
                break;
            }
        }
        return kits.get((index + 1) % kits.size()).getId();
    }

    private FileConfiguration config() {
        return plugin.getConfigManager().getPvp();
    }

    private static String configuredTitle(UltimateDonutSmp plugin) {
        return plugin.getConfigManager().getPvp().getString(PATH + ".TITLE", "&8Assign match");
    }

    private static int configuredSize(UltimateDonutSmp plugin) {
        int size = plugin.getConfigManager().getPvp().getInt(PATH + ".SIZE", 27);
        return size >= 27 && size <= 54 && size % 9 == 0 ? size : 27;
    }
}
