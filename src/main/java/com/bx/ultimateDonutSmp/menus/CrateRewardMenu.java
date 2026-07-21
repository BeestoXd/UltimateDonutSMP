package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.CrateManager;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CrateRewardMenu extends BaseMenu {

    private final CrateManager.CrateDefinition crate;
    private final OpenContext openContext;
    private final Map<Integer, String> rewardSlots = new HashMap<>();

    public CrateRewardMenu(UltimateDonutSmp plugin, CrateManager.CrateDefinition crate) {
        this(plugin, crate, OpenContext.COMMAND);
    }

    public CrateRewardMenu(UltimateDonutSmp plugin, CrateManager.CrateDefinition crate, OpenContext openContext) {
        super(plugin, crate.menuSettings().openTitle(), crate.menuSettings().size());
        this.crate = crate;
        this.openContext = openContext;
    }

    @Override
    public void build(Player player) {
        clear();
        rewardSlots.clear();
        fill(crate.menuSettings().filler());

        List<Integer> centeredSlots = openContext.centerRewards()
                ? buildCenteredRewardSlots(crate.rewards().size(), inventory.getSize(), openContext.showBackButton() ? crate.menuSettings().backSlot() : -1)
                : List.of();

        int rewardIndex = 0;
        for (CrateManager.CrateReward reward : crate.rewards()) {
            int targetSlot = openContext.centerRewards()
                    ? rewardIndex < centeredSlots.size() ? centeredSlots.get(rewardIndex) : -1
                    : reward.slot();
            rewardIndex++;

            if (targetSlot < 0 || targetSlot >= inventory.getSize()) {
                continue;
            }

            ItemStack display = plugin.getCrateManager().createRewardDisplayItem(player, crate, reward);
            if (openContext.interactive()) {
                appendKeyHint(display, player);
            }
            set(targetSlot, display);
            rewardSlots.put(targetSlot, reward.id());
        }

        if (openContext.showBackButton()) {
            set(crate.menuSettings().backSlot(), createSimpleItem(crate.menuSettings().backButton(), player, null));
        }
    }

    @Override
    public void handleClick(int slot, Player player) {
        if (!openContext.interactive()) {
            return;
        }

        if (openContext.showBackButton() && slot == crate.menuSettings().backSlot()) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            plugin.getCrateManager().clearSession(player.getUniqueId());
            new CratesMenu(plugin).open(player);
            return;
        }

        String rewardId = rewardSlots.get(slot);
        if (rewardId == null) {
            return;
        }

        if (plugin.getCrateManager().getKeyBalance(player, crate.id()) <= 0) {
            plugin.getCrateVisualManager().playNoKeyEffects(player);
            player.sendMessage(ColorUtils.toComponent(keyHintLine(player, false)));
            return;
        }

        SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
        if (!plugin.getCrateManager().selectReward(player, rewardId)) {
            player.sendMessage(ColorUtils.toComponent("&cᴛʜᴀᴛ ʀᴇᴡᴀʀᴅ ᴄᴏᴜʟᴅ ɴᴏᴛ ʙᴇ ѕᴇʟᴇᴄᴛᴇᴅ."));
            build(player);
            return;
        }

        CrateManager.CrateOpenSession session = plugin.getCrateManager().getSession(player.getUniqueId());
        if (session == null || session.selectedReward() == null) {
            player.sendMessage(ColorUtils.toComponent("&cᴛʜᴀᴛ ʀᴇᴡᴀʀᴅ ᴄᴏᴜʟᴅ ɴᴏᴛ ʙᴇ ѕᴇʟᴇᴄᴛᴇᴅ."));
            return;
        }

        new CrateConfirmMenu(plugin, crate, session.selectedReward(), openContext).open(player);
    }

    @Override
    public void onClose(Player player) {
        plugin.getSpigotScheduler().runEntity(player, () -> {
            if (!player.isOnline()) {
                plugin.getCrateManager().clearSession(player.getUniqueId());
                return;
            }
            org.bukkit.inventory.Inventory top = player.getOpenInventory().getTopInventory();
            if (!(top.getHolder() instanceof BaseMenu)) {
                plugin.getCrateManager().clearSession(player.getUniqueId());
            }
        });
    }

    public OpenContext openContext() {
        return openContext;
    }

    // one unified menu instead of preview/open modes: the reward copy says whether the
    // player can claim (keys are a per-player balance, not an inventory item), and a
    // keyless claim click explains itself in place
    private void appendKeyHint(ItemStack display, Player player) {
        var meta = display.getItemMeta();
        if (meta == null) {
            return;
        }
        List<String> lore = meta.getLore() == null ? new ArrayList<>() : new ArrayList<>(meta.getLore());
        lore.add("");
        boolean hasKey = plugin.getCrateManager().getKeyBalance(player, crate.id()) > 0;
        lore.add(ColorUtils.toComponent(keyHintLine(player, hasKey)));
        meta.setLore(lore);
        display.setItemMeta(meta);
    }

    private String keyHintLine(Player player, boolean hasKey) {
        String raw = hasKey
                ? plugin.getConfigManager().getMessageOrDefault("CRATES.REWARD-CLAIM-HINT", "&aClick to claim this reward!")
                : plugin.getConfigManager().getMessageOrDefault("CRATES.REWARD-NO-KEY", "&cYou don't have a {crate} key!");
        return plugin.getCrateManager().applyPlaceholders(raw, player, crate, null);
    }

    private List<Integer> buildCenteredRewardSlots(int rewardCount, int inventorySize, int excludedSlot) {
        List<Integer> centered = new ArrayList<>();
        if (rewardCount <= 0) {
            return centered;
        }

        int totalRows = inventorySize / 9;
        List<Integer> rowOrder = new ArrayList<>();
        int middleRow = totalRows / 2;
        rowOrder.add(middleRow);
        for (int offset = 1; offset < totalRows; offset++) {
            int lower = middleRow - offset;
            int upper = middleRow + offset;
            if (lower >= 0) {
                rowOrder.add(lower);
            }
            if (upper < totalRows) {
                rowOrder.add(upper);
            }
        }

        for (int row : rowOrder) {
            for (int column : centeredColumnsForRow()) {
                int slot = (row * 9) + column;
                if (slot == excludedSlot) {
                    continue;
                }
                centered.add(slot);
                if (centered.size() >= rewardCount) {
                    return centered;
                }
            }
        }

        return centered;
    }

    private List<Integer> centeredColumnsForRow() {
        return List.of(4, 3, 5, 2, 6, 1, 7, 0, 8);
    }

    private ItemStack createSimpleItem(
            CrateManager.DisplayItem display,
            Player player,
            CrateManager.CrateReward reward
    ) {
        ItemStack item = ItemUtils.createItem(
                display.material(),
                plugin.getCrateManager().applyPlaceholders(display.displayName(), player, crate, reward),
                plugin.getCrateManager().applyPlaceholders(display.lore(), player, crate, reward)
        );
        ItemUtils.addEnchantments(item, display.enchantments());
        item.setAmount(Math.max(1, Math.min(display.amount(), item.getMaxStackSize())));
        return item;
    }

    public enum OpenContext {
        COMMAND(false, true, true),
        CHEST(false, false, true),
        PREVIEW(false, false, false);

        private final boolean centerRewards;
        private final boolean showBackButton;
        private final boolean interactive;

        OpenContext(boolean centerRewards, boolean showBackButton, boolean interactive) {
            this.centerRewards = centerRewards;
            this.showBackButton = showBackButton;
            this.interactive = interactive;
        }

        public boolean centerRewards() {
            return centerRewards;
        }

        public boolean showBackButton() {
            return showBackButton;
        }

        public boolean interactive() {
            return interactive;
        }
    }
}
