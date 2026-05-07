package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.CrateManager;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class KeysMenu extends BaseMenu {

    private static final List<Integer> CENTERED_SLOTS = List.of(
            13, 12, 14, 11, 15, 10, 16,
            4, 22, 3, 5, 21, 23, 2, 6,
            20, 24, 1, 7, 19, 25, 0, 8, 18, 26
    );

    public KeysMenu(UltimateDonutSmp plugin) {
        super(plugin, "&8Your Keys", 27);
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.BLACK_STAINED_GLASS_PANE);

        List<CrateManager.CrateDefinition> crates = new ArrayList<>(plugin.getCrateManager().getCrates());
        if (crates.isEmpty()) {
            set(13, ItemUtils.createItem(
                    Material.BARRIER,
                    "&cNo Crates",
                    List.of("&7No crate keys are available right now.")
            ));
            return;
        }

        int rendered = 0;
        for (CrateManager.CrateDefinition crate : crates) {
            if (rendered >= CENTERED_SLOTS.size()) {
                break;
            }

            set(CENTERED_SLOTS.get(rendered++), createKeyItem(player, crate));
        }
    }

    private ItemStack createKeyItem(Player player, CrateManager.CrateDefinition crate) {
        int keyCount = plugin.getCrateManager().getKeyBalance(player, crate.id());
        CrateManager.DisplayItem keyItem = crate.keyItem();
        ItemStack item = ItemUtils.createItem(
                keyItem.material(),
                plugin.getCrateManager().applyPlaceholders(keyItem.displayName(), player, crate, null),
                List.of("&7Keys: &f" + keyCount)
        );
        ItemUtils.addEnchantments(item, keyItem.enchantments());

        int maxAmount = Math.max(1, item.getMaxStackSize());
        item.setAmount(Math.max(1, Math.min(keyCount <= 0 ? 1 : keyCount, maxAmount)));
        return item;
    }
}
