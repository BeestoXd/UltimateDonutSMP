package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PvpKit;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * The kit editor. An admin drops the real items in, and closing the menu stores them.
 *
 * <p>Editing by hand beats a form of dropdowns here: whatever the item is - enchantments, custom
 * name, lore, potion, stack size, a plugin's own custom item - it round-trips exactly, because
 * what gets saved is the {@link ItemStack} itself rather than a description of it.</p>
 */
public class PvpKitEditMenu extends BaseMenu {

    /** Chest slots 0-35 map straight onto the kit's 36 inventory slots. */
    private static final int CONTENT_END = 36;
    private static final int DIVIDER_START = 36;
    private static final int DIVIDER_END = 45;
    private static final int ARMOR_START = 45;
    private static final int LABEL_SLOT = 49;
    private static final int OFFHAND_SLOT = 50;
    private static final int[] FILLER_SLOTS = {51, 52, 53};

    private final PvpKit kit;
    private boolean saved;

    public PvpKitEditMenu(UltimateDonutSmp plugin, PvpKit kit) {
        super(plugin, "&8Editing kit: &f" + kit.getId(), 54);
        this.kit = kit;
    }

    @Override
    public void build(Player player) {
        clear();

        for (int slot = 0; slot < CONTENT_END; slot++) {
            inventory.setItem(slot, kit.getContents()[slot]);
        }
        for (int slot = DIVIDER_START; slot < DIVIDER_END; slot++) {
            inventory.setItem(slot, ItemUtils.createItem(
                    Material.GRAY_STAINED_GLASS_PANE,
                    "&8-",
                    List.of("&7Rows above: the kit inventory.",
                            "&7Row below: armour and offhand.",
                            "&7Close the menu to save.")
            ));
        }
        for (int slot = 0; slot < 4; slot++) {
            inventory.setItem(ARMOR_START + slot, kit.getArmor()[slot]);
        }
        inventory.setItem(OFFHAND_SLOT, kit.getOffhand());

        inventory.setItem(LABEL_SLOT, ItemUtils.createItem(
                Material.PAPER,
                "&fArmour and offhand",
                List.of(
                        "&7The four slots to the left are",
                        "&7boots, leggings, chestplate, helmet.",
                        "&7The slot to the right is the offhand."
                )
        ));
        for (int slot : FILLER_SLOTS) {
            inventory.setItem(slot, ItemUtils.createItem(Material.BLACK_STAINED_GLASS_PANE, "&8-", null));
        }
    }

    /** Free placement everywhere except the labelled slots, which stay put. */
    public void handleInventoryClick(InventoryClickEvent event) {
        int raw = event.getRawSlot();
        if (raw >= 0 && raw < inventory.getSize() && isLocked(raw)) {
            event.setCancelled(true);
        }
    }

    public void handleInventoryDrag(InventoryDragEvent event) {
        for (int raw : event.getRawSlots()) {
            if (raw >= 0 && raw < inventory.getSize() && isLocked(raw)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    private boolean isLocked(int slot) {
        if (slot >= DIVIDER_START && slot < DIVIDER_END) {
            return true;
        }
        if (slot == LABEL_SLOT) {
            return true;
        }
        for (int filler : FILLER_SLOTS) {
            if (slot == filler) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onClose(Player player) {
        if (saved) {
            return;
        }
        saved = true;

        for (int slot = 0; slot < CONTENT_END; slot++) {
            kit.getContents()[slot] = inventory.getItem(slot);
        }
        for (int slot = 0; slot < 4; slot++) {
            kit.getArmor()[slot] = inventory.getItem(ARMOR_START + slot);
        }
        kit.setOffhand(inventory.getItem(OFFHAND_SLOT));

        plugin.getPvpManager().saveKit(kit);
        player.sendMessage(ColorUtils.toComponent("&aSaved the &f" + kit.getId() + " &akit."));
    }
}
