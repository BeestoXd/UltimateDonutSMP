package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.CrateManager;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CrateEditorMenu extends BaseMenu {

    private final String crateId;
    private final Set<Integer> lockedSlots = new HashSet<>();
    private ItemStack selectedTemplate;
    private boolean instructionsSent;

    public CrateEditorMenu(UltimateDonutSmp plugin, String crateId) {
        super(plugin, "&8Editing crate: " + crateId, resolveSize(plugin, crateId));
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
            player.sendMessage(ColorUtils.toComponent("&8[&bCrates&8] &7Click an item in your inventory to select it as a template, then click a crate slot to place or replace it."));
            player.sendMessage(ColorUtils.toComponent("&8[&bCrates&8] &7Click a reward slot with no selected template to remove the item reward from that slot."));
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
                String display = "";
                if (selectedTemplate.hasItemMeta() && selectedTemplate.getItemMeta() != null && selectedTemplate.getItemMeta().hasDisplayName()) {
                    display = selectedTemplate.getItemMeta().getDisplayName();
                }
                // Template shorthand: use display name tags to create non-item rewards from the GUI.
                // Supported tags (place on a held item as the display name):
                // [CMD] <console command...>
                // [MONEY] <amount>
                // [SHARDS] <amount>
                if (display.startsWith("[CMD] ")) {
                    String consoleCommand = display.substring(6).strip();
                    CrateManager.ActionResult result = plugin.getCrateManager().addCommandReward(crateId, rawSlot, List.of(consoleCommand));
                    player.sendMessage(ColorUtils.toComponent(result.message()));
                    if (result.success()) {
                        SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
                        build(player);
                    }
                    return;
                } else if (display.startsWith("[MONEY] ")) {
                    String amountStr = display.substring(8).strip();
                    Double parsed = null;
                    try {
                        parsed = Double.parseDouble(amountStr);
                    } catch (NumberFormatException ignored) { }
                    if (parsed == null) {
                        player.sendMessage(ColorUtils.toComponent("&cInvalid money amount in template display name. Use '[MONEY] 10.5'."));
                        return;
                    }
                    CrateManager.ActionResult result = plugin.getCrateManager().addMoneyReward(crateId, rawSlot, parsed);
                    player.sendMessage(ColorUtils.toComponent(result.message()));
                    if (result.success()) {
                        SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
                        build(player);
                    }
                    return;
                } else if (display.startsWith("[SHARDS] ")) {
                    String amountStr = display.substring(9).strip();
                    Long parsed = null;
                    try {
                        parsed = Long.parseLong(amountStr);
                    } catch (NumberFormatException ignored) { }
                    if (parsed == null) {
                        player.sendMessage(ColorUtils.toComponent("&cInvalid shards amount in template display name. Use '[SHARDS] 100'."));
                        return;
                    }
                    CrateManager.ActionResult result = plugin.getCrateManager().addShardsReward(crateId, rawSlot, parsed);
                    player.sendMessage(ColorUtils.toComponent(result.message()));
                    if (result.success()) {
                        SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
                        build(player);
                    }
                    return;
                }

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
        ItemStack item = null;
        if (reward.grant().serializedItemData() != null && !reward.grant().serializedItemData().isBlank()) {
            try {
                item = com.bx.ultimateDonutSmp.utils.ItemSerializationUtils.deserialize(reward.grant().serializedItemData());
            } catch (Exception ignored) {
            }
        }
        if (item == null) {
            item = ItemUtils.createItem(
                    reward.grant().item().material(),
                    reward.grant().item().displayName(),
                    reward.grant().item().lore()
            );
            item.setAmount(Math.max(1, Math.min(reward.grant().item().amount(), item.getMaxStackSize())));
            ItemUtils.addEnchantments(item, reward.grant().item().enchantments());
        } else {
            item = item.clone();
            item.setAmount(Math.max(1, Math.min(reward.grant().item().amount(), item.getMaxStackSize())));
        }
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
        if (item.hasItemMeta() && item.getItemMeta() != null && item.getItemMeta().hasDisplayName()) {
            return ColorUtils.strip(item.getItemMeta().getDisplayName().replace('\u00A7', '&'));
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
