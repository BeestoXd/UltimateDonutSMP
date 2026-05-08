package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.CrateManager;
import com.bx.ultimateDonutSmp.menus.CrateRewardMenu.OpenContext;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CratesMenu extends BaseMenu {

    private final Map<Integer, String> crateSlots = new HashMap<>();
    private final int closeSlot;

    public CratesMenu(UltimateDonutSmp plugin) {
        super(
                plugin,
                plugin.getCrateManager().getListMenuSettings().title(),
                plugin.getCrateManager().getListMenuSettings().size()
        );
        this.closeSlot = plugin.getCrateManager().getListMenuSettings().closeSlot();
    }

    @Override
    public void build(Player player) {
        clear();
        crateSlots.clear();

        CrateManager.ListMenuSettings settings = plugin.getCrateManager().getListMenuSettings();
        fill(settings.filler());

        List<CrateManager.CrateDefinition> crates = plugin.getCrateManager().getAccessibleCrates(player);
        List<Integer> contentSlots = settings.contentSlots();

        int rendered = 0;
        for (int i = 0; i < contentSlots.size() && rendered < crates.size(); i++) {
            int slot = contentSlots.get(i);
            CrateManager.CrateDefinition crate = crates.get(rendered++);
            set(slot, plugin.getCrateManager().createCrateListItem(player, crate));
            crateSlots.put(slot, crate.id());
        }

        if (crates.isEmpty()) {
            set(settings.emptySlot(), createSimpleItem(settings.emptyItem(), player, null, null));
        }

        set(settings.closeSlot(), createSimpleItem(settings.closeItem(), player, null, null));
    }

    @Override
    public void handleClick(int slot, Player player) {
        if (slot == closeSlot) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            player.closeInventory();
            return;
        }

        String crateId = crateSlots.get(slot);
        if (crateId == null) {
            return;
        }

        SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
        CrateManager.OpenResult result = plugin.getCrateManager().startOpening(player, crateId);
        if (!result.success()) {
            player.sendMessage(ColorUtils.toComponent(result.message()));
            build(player);
            return;
        }

        if (result.crate().openType() == CrateManager.OpenType.GACHA) {
            new CrateGachaMenu(plugin, result.crate()).open(player);
            return;
        }

        new CrateRewardMenu(plugin, result.crate(), OpenContext.COMMAND).open(player);
    }

    private org.bukkit.inventory.ItemStack createSimpleItem(
            CrateManager.DisplayItem display,
            Player player,
            CrateManager.CrateDefinition crate,
            CrateManager.CrateReward reward
    ) {
        org.bukkit.inventory.ItemStack item = com.bx.ultimateDonutSmp.utils.ItemUtils.createItem(
                display.material(),
                plugin.getCrateManager().applyPlaceholders(display.displayName(), player, crate, reward),
                plugin.getCrateManager().applyPlaceholders(display.lore(), player, crate, reward)
        );
        com.bx.ultimateDonutSmp.utils.ItemUtils.addEnchantments(item, display.enchantments());
        item.setAmount(Math.max(1, Math.min(display.amount(), item.getMaxStackSize())));
        return item;
    }
}
