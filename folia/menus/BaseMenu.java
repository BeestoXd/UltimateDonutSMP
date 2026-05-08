package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public abstract class BaseMenu implements InventoryHolder {

    protected final UltimateDonutSmp plugin;
    protected Inventory inventory;

    public BaseMenu(UltimateDonutSmp plugin, String title, int size) {
        this.plugin = plugin;
        this.inventory = Bukkit.createInventory(this, size, ColorUtils.toComponent(title));
    }

    public abstract void build(Player player);

    /** Override this to handle clicks with click-type awareness. */
    public void handleClick(int slot, Player player, ClickType clickType) {
        handleClick(slot, player); // default: delegate to simple version
    }

    /** Legacy simple click handler — override if you don't need click type. */
    public void handleClick(int slot, Player player) {}

    public void onClose(Player player) {}

    public void open(Player player) {
        build(player);
        player.openInventory(inventory);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    protected void fill(Material material) {
        ItemUtils.fillInventory(inventory, material);
    }

    protected void set(int slot, ItemStack item) {
        if (slot >= 0 && slot < inventory.getSize()) {
            inventory.setItem(slot, item);
        }
    }

    protected void clear() {
        inventory.clear();
    }

    protected boolean isPlaceholder(ItemStack item) {
        if (item == null) return true;
        return item.getType() == Material.GRAY_STAINED_GLASS_PANE
                || item.getType() == Material.BLACK_STAINED_GLASS_PANE;
    }
}
