package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.CrateManager;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CrateEditorMenu extends BaseMenu {

    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection();

    private final String crateId;
    private final Set<Integer> lockedSlots = new HashSet<>();
    private ItemStack selectedTemplate;
    private boolean instructionsSent;

    public CrateEditorMenu(UltimateDonutSmp plugin, String crateId) {
        super(plugin, "&8Editing Crate: " + crateId, resolveSize(plugin, crateId));
        this.crateId = crateId;
    }

    @Override
    public void build(Player player) {
        CrateManager.CrateDefinition crate = getCrate();
        clear();
        lockedSlots.clear();

        if (crate == null) {
            return;
        }

        for (CrateManager.CrateReward reward : crate.rewards()) {
            if (reward.slot() < 0 || reward.slot() >= inventory.getSize() || reward.slot() == crate.menuSettings().backSlot()) {
                continue;
            }

            if (reward.grant().type() == CrateManager.GrantType.ITEM) {
                set(reward.slot(), createEditorItem(reward));
                continue;
            }

            set(reward.slot(), createLockedPreviewItem(reward));
            lockedSlots.add(reward.slot());
        }

        set(crate.menuSettings().backSlot(), ItemUtils.createItem(
                Material.BARRIER,
                "&cClose Editor",
                List.of(
                        "&7Click to close this editor.",
                        "&7Changes are saved instantly."
                )
        ));

        if (!instructionsSent) {
            instructionsSent = true;
            player.sendMessage(ColorUtils.toComponent("&8[&bCrates&8] &7Click item di inventory kamu untuk memilih template, lalu klik slot crate untuk menaruh atau menggantinya."));
            player.sendMessage(ColorUtils.toComponent("&8[&bCrates&8] &7Klik slot reward tanpa template terpilih untuk menghapus reward item dari slot itu."));
        }
    }

    public void handleInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        CrateManager.CrateDefinition crate = getCrate();
        if (crate == null) {
            event.setCancelled(true);
            player.closeInventory();
            player.sendMessage(ColorUtils.toComponent("&cThat crate no longer exists."));
            return;
        }

        int rawSlot = event.getRawSlot();
        if (rawSlot >= 0 && rawSlot < inventory.getSize()) {
            event.setCancelled(true);

            if (rawSlot == crate.menuSettings().backSlot()) {
                SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
                player.closeInventory();
                return;
            }

            if (lockedSlots.contains(rawSlot)) {
                player.sendMessage(ColorUtils.toComponent("&cThat slot contains a non-item reward. Edit it in crates.yml if needed."));
                return;
            }

            if (selectedTemplate != null) {
                CrateManager.ActionResult result = plugin.getCrateManager().upsertItemReward(crateId, rawSlot, selectedTemplate);
                player.sendMessage(ColorUtils.toComponent(result.message()));
                if (result.success()) {
                    SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
                    build(player);
                }
                return;
            }

            if (inventory.getItem(rawSlot) == null || inventory.getItem(rawSlot).getType().isAir()) {
                player.sendMessage(ColorUtils.toComponent("&cSelect an item from your inventory first."));
                return;
            }

            CrateManager.ActionResult result = plugin.getCrateManager().removeReward(crateId, rawSlot);
            player.sendMessage(ColorUtils.toComponent(result.message()));
            if (result.success()) {
                SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
                build(player);
            }
            return;
        }

        if (event.getClickedInventory() == null || event.getCurrentItem() == null || event.getCurrentItem().getType().isAir()) {
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);
        selectedTemplate = event.getCurrentItem().clone();
        player.sendMessage(ColorUtils.toComponent("&aSelected &f" + readableItemName(selectedTemplate) + "&a. Click a crate slot to place it."));
        SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
    }

    public void handleInventoryDrag(InventoryDragEvent event) {
        event.setCancelled(true);
    }

    @Override
    public void onClose(Player player) {
        selectedTemplate = null;
    }

    private CrateManager.CrateDefinition getCrate() {
        return plugin.getCrateManager().getCrate(crateId);
    }

    private ItemStack createEditorItem(CrateManager.CrateReward reward) {
        ItemStack item = ItemUtils.createItem(
                reward.grant().item().material(),
                reward.grant().item().displayName(),
                reward.grant().item().lore()
        );
        item.setAmount(Math.max(1, Math.min(reward.grant().item().amount(), item.getMaxStackSize())));
        ItemUtils.addEnchantments(item, reward.grant().item().enchantments());
        return item;
    }

    private ItemStack createLockedPreviewItem(CrateManager.CrateReward reward) {
        ItemStack item = ItemUtils.createItem(
                reward.display().material(),
                reward.display().displayName(),
                List.of(
                        "&7This slot uses a non-item reward.",
                        "&7GUI editor only supports item rewards."
                )
        );
        item.setAmount(1);
        return item;
    }

    private String readableItemName(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta() != null && item.getItemMeta().displayName() != null) {
            return ColorUtils.strip(LEGACY_SERIALIZER.serialize(item.getItemMeta().displayName()).replace('\u00A7', '&'));
        }
        return prettyMaterial(item.getType());
    }

    private String prettyMaterial(Material material) {
        String[] parts = material.name().toLowerCase().split("_");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return builder.toString();
    }

    private static int resolveSize(UltimateDonutSmp plugin, String crateId) {
        CrateManager.CrateDefinition crate = plugin.getCrateManager().getCrate(crateId);
        return crate == null ? 27 : crate.menuSettings().size();
    }
}
