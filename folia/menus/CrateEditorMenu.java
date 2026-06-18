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
        super(plugin, "&8ᴇᴅɪᴛɪɴɢ ᴄʀᴀᴛᴇ: " + crateId, resolveSize(plugin, crateId));
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
                "&cᴄʟᴏѕᴇ ᴇᴅɪᴛᴏʀ",
                List.of(
                        "&7ᴄʟɪᴄᴋ ᴛᴏ ᴄʟᴏѕᴇ ᴛʜɪѕ ᴇᴅɪᴛᴏʀ.",
                        "&7ᴄʜᴀɴɢᴇѕ ᴀʀᴇ ѕᴀᴠᴇᴅ ɪɴѕᴛᴀɴᴛʟʏ."
                )
        ));

        if (!instructionsSent) {
            instructionsSent = true;
            player.sendMessage(ColorUtils.toComponent("&8[&bᴄʀᴀᴛᴇѕ&8] &7ᴄʟɪᴄᴋ ᴀɴ ɪᴛᴇᴍ ɪɴ ʏᴏᴜʀ ɪɴᴠᴇɴᴛᴏʀʏ ᴛᴏ ѕᴇʟᴇᴄᴛ ɪᴛ ᴀѕ ᴀ ᴛᴇᴍᴘʟᴀᴛᴇ, ᴛʜᴇɴ ᴄʟɪᴄᴋ ᴀ ᴄʀᴀᴛᴇ ѕʟᴏᴛ ᴛᴏ ᴘʟᴀᴄᴇ ᴏʀ ʀᴇᴘʟᴀᴄᴇ ɪᴛ."));
            player.sendMessage(ColorUtils.toComponent("&8[&bᴄʀᴀᴛᴇѕ&8] &7ᴄʟɪᴄᴋ ᴀ ʀᴇᴡᴀʀᴅ ѕʟᴏᴛ ᴡɪᴛʜ ɴᴏ ѕᴇʟᴇᴄᴛᴇᴅ ᴛᴇᴍᴘʟᴀᴛᴇ ᴛᴏ ʀᴇᴍᴏᴠᴇ ᴛʜᴇ ɪᴛᴇᴍ ʀᴇᴡᴀʀᴅ ꜰʀᴏᴍ ᴛʜᴀᴛ ѕʟᴏᴛ."));
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
            player.sendMessage(ColorUtils.toComponent("&cᴛʜᴀᴛ ᴄʀᴀᴛᴇ ɴᴏ ʟᴏɴɢᴇʀ ᴇxɪѕᴛѕ."));
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
                player.sendMessage(ColorUtils.toComponent("&cᴛʜᴀᴛ ѕʟᴏᴛ ᴄᴏɴᴛᴀɪɴѕ ᴀ ɴᴏɴ-ɪᴛᴇᴍ ʀᴇᴡᴀʀᴅ. ᴇᴅɪᴛ ɪᴛ ɪɴ ᴄʀᴀᴛᴇѕ.ʏᴍʟ ɪꜰ ɴᴇᴇᴅᴇᴅ."));
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
                player.sendMessage(ColorUtils.toComponent("&cѕᴇʟᴇᴄᴛ ᴀɴ ɪᴛᴇᴍ ꜰʀᴏᴍ ʏᴏᴜʀ ɪɴᴠᴇɴᴛᴏʀʏ ꜰɪʀѕᴛ."));
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
        player.sendMessage(ColorUtils.toComponent("&aѕᴇʟᴇᴄᴛᴇᴅ &f" + readableItemName(selectedTemplate) + "&a. ᴄʟɪᴄᴋ ᴀ ᴄʀᴀᴛᴇ ѕʟᴏᴛ ᴛᴏ ᴘʟᴀᴄᴇ ɪᴛ."));
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
                        "&7ᴛʜɪѕ ѕʟᴏᴛ ᴜѕᴇѕ ᴀ ɴᴏɴ-ɪᴛᴇᴍ ʀᴇᴡᴀʀᴅ.",
                        "&7ɢᴜɪ ᴇᴅɪᴛᴏʀ ᴏɴʟʏ ѕᴜᴘᴘᴏʀᴛѕ ɪᴛᴇᴍ ʀᴇᴡᴀʀᴅѕ."
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
