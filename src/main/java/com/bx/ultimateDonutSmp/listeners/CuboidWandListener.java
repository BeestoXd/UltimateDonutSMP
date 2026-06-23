package com.bx.ultimateDonutSmp.listeners;

import com.bx.ultimateDonutSmp.utils.PermissionUtils;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class CuboidWandListener implements Listener {

    private static final String WAND_NAME = "&6cuboid wand";

    private static final String WAND_KEY = "cuboid_wand";

    private final UltimateDonutSmp plugin;

    public CuboidWandListener(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Action action = event.getAction();
        if (action != Action.LEFT_CLICK_BLOCK && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block clicked = event.getClickedBlock();
        if (clicked == null) {
            return;
        }

        Player player = event.getPlayer();
        if (!PermissionUtils.has(player, "ultimatedonutsmp.admin.cuboid")) {
            return;
        }
        if (!isCuboidWand(event.getItem())) {
            return;
        }

        event.setCancelled(true);
        event.setUseInteractedBlock(Event.Result.DENY);
        event.setUseItemInHand(Event.Result.DENY);

        if (action == Action.LEFT_CLICK_BLOCK) {
            plugin.getCuboidManager().setPos1(player.getUniqueId(), clicked.getLocation());
            player.sendMessage(ColorUtils.toComponent("&aꜰɪʀѕᴛ ᴘᴏѕɪᴛɪᴏɴ ѕᴇᴛ ᴀᴛ &f" + formatLoc(clicked)));
            handleSelectionCompleted(player);
            return;
        }

        plugin.getCuboidManager().setPos2(player.getUniqueId(), clicked.getLocation());
        player.sendMessage(ColorUtils.toComponent("&aѕᴇᴄᴏɴᴅ ᴘᴏѕɪᴛɪᴏɴ ѕᴇᴛ ᴀᴛ &f" + formatLoc(clicked)));
        handleSelectionCompleted(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockDamage(BlockDamageEvent event) {
        if (isCuboidWand(event.getPlayer().getInventory().getItemInMainHand())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (isCuboidWand(event.getPlayer().getInventory().getItemInMainHand())) {
            event.setCancelled(true);
        }
    }

    public static boolean isCuboidWand(ItemStack item) {
        if (item == null || item.getType() != Material.GOLDEN_SHOVEL) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        if (hasWandMarker(meta)) {
            return true;
        }
        if (!meta.hasDisplayName()) {
            return false;
        }

        String displayName = ColorUtils.strip(meta.getDisplayName()).trim();
        String wandName = ColorUtils.strip(WAND_NAME).trim();
        return displayName.equalsIgnoreCase(wandName)
                || displayName.equalsIgnoreCase("Cuboid Wand");
    }

    public static String getWandName() {
        return WAND_NAME;
    }

    public static void markAsCuboidWand(UltimateDonutSmp plugin, ItemStack item) {
        if (plugin == null || item == null) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }

        meta.getPersistentDataContainer().set(plugin.getKey(WAND_KEY), PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
    }

    private static boolean hasWandMarker(ItemMeta meta) {
        UltimateDonutSmp instance = UltimateDonutSmp.getInstance();
        return instance != null
                && meta.getPersistentDataContainer().has(instance.getKey(WAND_KEY), PersistentDataType.BYTE);
    }

    private void handleSelectionCompleted(Player player) {
        if (!plugin.getCuboidManager().hasFullSelection(player.getUniqueId())) {
            return;
        }

        removeWandFromInventory(player);
        player.sendMessage(ColorUtils.toComponent("&aѕᴇʟᴇᴄᴛɪᴏɴ ᴄᴏᴍᴘʟᴇᴛᴇ. &7ᴄᴜʙᴏɪᴅ ᴡᴀɴᴅ ʀᴇᴍᴏᴠᴇᴅ ꜰʀᴏᴍ ʏᴏᴜʀ ɪɴᴠᴇɴᴛᴏʀʏ."));

        TextComponent guideMessage = ColorUtils.toBaseComponent("&estep 3/3 &7type ");
        TextComponent commandPart = ColorUtils.toBaseComponent("&f/cuboid create <name>");
        commandPart.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/cuboid create "));
        commandPart.setHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                ColorUtils.toBaseComponents("&7click to autofill the create command.")
        ));
        guideMessage.addExtra(commandPart);
        player.spigot().sendMessage(guideMessage);
    }

    private void removeWandFromInventory(Player player) {
        for (int slot = 0; slot < player.getInventory().getSize(); slot++) {
            ItemStack item = player.getInventory().getItem(slot);
            if (isCuboidWand(item)) {
                player.getInventory().setItem(slot, null);
            }
        }
    }

    private String formatLoc(Block block) {
        return "X:" + block.getX() + " Y:" + block.getY() + " Z:" + block.getZ();
    }
}
