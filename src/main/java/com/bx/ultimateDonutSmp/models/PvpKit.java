package com.bx.ultimateDonutSmp.models;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * One arena kit: the hotbar and inventory contents, the four armour pieces, the offhand item,
 * any effects applied on spawn, and how the kit shows up in the selection menu.
 *
 * <p>The items are stored as real {@link ItemStack}s rather than a material/enchantment
 * description, so whatever an admin puts in the kit editor - custom names, lore, enchantments,
 * potions, stack sizes - comes back out exactly as they left it.</p>
 */
public class PvpKit {

    /** Main inventory slots a kit fills: the hotbar plus the three storage rows. */
    public static final int CONTENT_SIZE = 36;

    private final String id;
    private String displayName;
    private Material icon;
    private String permission;
    private int menuSlot;
    private final ItemStack[] contents;
    private final ItemStack[] armor;
    private ItemStack offhand;
    private final List<PotionEffect> effects;

    public PvpKit(String id) {
        this.id = id == null ? "" : id;
        this.displayName = "&f" + this.id;
        this.icon = Material.IRON_SWORD;
        this.permission = "";
        this.menuSlot = -1;
        this.contents = new ItemStack[CONTENT_SIZE];
        this.armor = new ItemStack[4];
        this.effects = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName == null || displayName.isBlank() ? "&f" + id : displayName;
    }

    public Material getIcon() {
        return icon;
    }

    public void setIcon(Material icon) {
        this.icon = icon == null ? Material.IRON_SWORD : icon;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission == null ? "" : permission.trim();
    }

    public int getMenuSlot() {
        return menuSlot;
    }

    public void setMenuSlot(int menuSlot) {
        this.menuSlot = menuSlot;
    }

    /** The live contents array. Callers write into it directly when saving an edited kit. */
    public ItemStack[] getContents() {
        return contents;
    }

    /** The live armour array, ordered the way Bukkit orders it: boots, leggings, chestplate, helmet. */
    public ItemStack[] getArmor() {
        return armor;
    }

    public ItemStack getOffhand() {
        return offhand;
    }

    public void setOffhand(ItemStack offhand) {
        this.offhand = offhand;
    }

    public List<PotionEffect> getEffects() {
        return Collections.unmodifiableList(effects);
    }

    public void setEffects(List<PotionEffect> newEffects) {
        effects.clear();
        if (newEffects != null) {
            effects.addAll(newEffects);
        }
    }

    /** True when the kit would hand a player nothing at all. */
    public boolean isEmpty() {
        for (ItemStack item : contents) {
            if (item != null && item.getType() != Material.AIR) {
                return false;
            }
        }
        for (ItemStack item : armor) {
            if (item != null && item.getType() != Material.AIR) {
                return false;
            }
        }
        return offhand == null || offhand.getType() == Material.AIR;
    }
}
