package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.CrateManager;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CrateConfirmMenu extends BaseMenu {

    private final CrateManager.CrateDefinition crate;
    private final CrateManager.CrateReward reward;
    private final CrateRewardMenu.OpenContext openContext;

    public CrateConfirmMenu(
            UltimateDonutSmp plugin,
            CrateManager.CrateDefinition crate,
            CrateManager.CrateReward reward
    ) {
        this(plugin, crate, reward, CrateRewardMenu.OpenContext.COMMAND);
    }

    public CrateConfirmMenu(
            UltimateDonutSmp plugin,
            CrateManager.CrateDefinition crate,
            CrateManager.CrateReward reward,
            CrateRewardMenu.OpenContext openContext
    ) {
        super(
                plugin,
                crate.menuSettings().confirmTitle(),
                plugin.getCrateManager().getConfirmMenuSettings().size()
        );
        this.crate = crate;
        this.reward = reward;
        this.openContext = openContext;
    }

    @Override
    public void build(Player player) {
        CrateManager.ConfirmMenuSettings settings = plugin.getCrateManager().getConfirmMenuSettings();
        clear();
        fill(settings.filler());

        set(settings.previewSlot(), plugin.getCrateManager().createRewardPreviewItem(player, crate, reward));
        set(settings.confirmSlot(), createSimpleItem(settings.confirmButton(), player));
        set(settings.cancelSlot(), createSimpleItem(settings.cancelButton(), player));
    }

    @Override
    public void handleClick(int slot, Player player) {
        CrateManager.ConfirmMenuSettings settings = plugin.getCrateManager().getConfirmMenuSettings();

        if (slot == settings.cancelSlot()) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            new CrateRewardMenu(plugin, crate, openContext).open(player);
            return;
        }

        if (slot != settings.confirmSlot()) {
            return;
        }

        SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
        CrateManager.ClaimResult result = plugin.getCrateManager().claimSelectedReward(player);
        player.sendMessage(ColorUtils.toComponent(result.message()));

        if (!result.success()) {
            if (result.reason() == CrateManager.FailureReason.NO_SESSION
                    || result.reason() == CrateManager.FailureReason.INVALID_CRATE
                    || result.reason() == CrateManager.FailureReason.NO_KEYS) {
                if (openContext == CrateRewardMenu.OpenContext.CHEST) {
                    player.closeInventory();
                } else {
                    new CratesMenu(plugin).open(player);
                }
                return;
            }

            new CrateRewardMenu(plugin, crate, openContext).open(player);
            return;
        }

        plugin.getCrateVisualManager().playClaimEffects(player, crate);
        player.closeInventory();
    }

    private ItemStack createSimpleItem(CrateManager.DisplayItem display, Player player) {
        ItemStack item = ItemUtils.createItem(
                display.material(),
                plugin.getCrateManager().applyPlaceholders(display.displayName(), player, crate, reward),
                plugin.getCrateManager().applyPlaceholders(display.lore(), player, crate, reward)
        );
        ItemUtils.addEnchantments(item, display.enchantments());
        item.setAmount(Math.max(1, Math.min(display.amount(), item.getMaxStackSize())));
        return item;
    }
}
