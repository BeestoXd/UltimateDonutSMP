package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PvpKit;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The ranked queue menu: pick the kit you want to fight with, then confirm to join.
 *
 * <p>The kit is chosen before queueing rather than after being matched, so two players who are
 * paired arrive already agreed on the loadout and the fight can start on a countdown instead of on
 * a second round of menus.</p>
 */
public class PvpQueueMenu extends BaseMenu {

    private static final String PATH = "MENUS.QUEUE";
    private static final List<Integer> KIT_SLOTS = List.of(12, 13, 14, 11, 15);
    private static final int CANCEL_SLOT = 10;
    private static final int CONFIRM_SLOT = 16;

    private final Map<Integer, String> slotKits = new HashMap<>();
    private String selectedKit;

    public PvpQueueMenu(UltimateDonutSmp plugin) {
        super(plugin, configuredTitle(plugin), configuredSize(plugin));
    }

    @Override
    public void build(Player player) {
        clear();
        slotKits.clear();
        fill(Material.BLACK_STAINED_GLASS_PANE);

        List<PvpKit> kits = plugin.getPvpManager().getAvailableKits(player);
        if (kits.isEmpty()) {
            set(13, ItemUtils.createItem(Material.BARRIER, "&cNo kits",
                    List.of("&7There are no PvP kits to choose from yet.")));
            return;
        }

        if (selectedKit == null || plugin.getPvpManager().getKit(selectedKit) == null) {
            String queued = plugin.getPvpMatchManager().getQueuedKit(player.getUniqueId());
            selectedKit = queued != null ? queued : kits.get(0).getId();
        }

        for (int index = 0; index < kits.size() && index < KIT_SLOTS.size(); index++) {
            PvpKit kit = kits.get(index);
            int slot = KIT_SLOTS.get(index);
            boolean chosen = kit.getId().equals(selectedKit);

            List<String> lore = new ArrayList<>();
            lore.add(chosen ? "&aSelected" : "&7Click to select this kit.");
            set(slot, ItemUtils.setGlint(
                    ItemUtils.createItem(kit.getIcon(), kit.getDisplayName(), lore),
                    chosen
            ));
            slotKits.put(slot, kit.getId());
        }

        boolean queued = plugin.getPvpMatchManager().isQueued(player.getUniqueId());
        set(CONFIRM_SLOT, ItemUtils.createItem(
                ItemUtils.parseMaterial(config().getString(PATH + ".CONFIRM.MATERIAL", "LIME_STAINED_GLASS_PANE")),
                config().getString(PATH + ".CONFIRM.DISPLAY-NAME", "&aCONFIRM"),
                queued
                        ? List.of("&7Already in the queue.", "&7Waiting: &f" + waiting(player) + "s")
                        : List.of("&7Click to join the queue.", "&7In queue: &f"
                        + plugin.getPvpMatchManager().getQueueSize())
        ));
        set(CANCEL_SLOT, ItemUtils.createItem(
                ItemUtils.parseMaterial(config().getString(PATH + ".CANCEL.MATERIAL", "RED_STAINED_GLASS_PANE")),
                config().getString(PATH + ".CANCEL.DISPLAY-NAME", "&cLEAVE"),
                List.of(queued ? "&7Click to leave the queue." : "&7Click to close this menu.")
        ));
    }

    private long waiting(Player player) {
        return Math.max(0L, plugin.getPvpMatchManager().getQueuedSeconds(player.getUniqueId()));
    }

    @Override
    public void handleClick(int slot, Player player, ClickType clickType) {
        String kitId = slotKits.get(slot);
        if (kitId != null) {
            selectedKit = kitId;
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            build(player);
            return;
        }

        if (slot == CANCEL_SLOT) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            if (plugin.getPvpMatchManager().leaveQueue(player.getUniqueId())) {
                player.sendMessage(ColorUtils.toComponent(plugin.getPvpManager()
                        .message("QUEUE_LEFT", "&aYou left the ranked queue.")));
            }
            player.closeInventory();
            return;
        }

        if (slot == CONFIRM_SLOT) {
            PvpKit kit = plugin.getPvpManager().getKit(selectedKit);
            if (kit == null) {
                return;
            }
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            player.closeInventory();
            plugin.getPvpMatchManager().joinQueue(player, kit);
        }
    }

    private FileConfiguration config() {
        return plugin.getConfigManager().getPvp();
    }

    private static String configuredTitle(UltimateDonutSmp plugin) {
        return plugin.getConfigManager().getPvp().getString(PATH + ".TITLE", "&8PvP queue");
    }

    private static int configuredSize(UltimateDonutSmp plugin) {
        int size = plugin.getConfigManager().getPvp().getInt(PATH + ".SIZE", 27);
        return size >= 27 && size <= 54 && size % 9 == 0 ? size : 27;
    }
}
