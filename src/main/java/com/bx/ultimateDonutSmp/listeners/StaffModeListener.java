package com.bx.ultimateDonutSmp.listeners;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.FreezeManager;
import com.bx.ultimateDonutSmp.staff.StaffToolType;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class StaffModeListener implements Listener {

    private static final long INTERACT_COOLDOWN_MS = 200L;

    private final UltimateDonutSmp plugin;
    private final Map<UUID, Long> lastInteractTimes = new ConcurrentHashMap<>();

    public StaffModeListener(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (!plugin.getStaffModeManager().isInStaffMode(player.getUniqueId())
                || !plugin.getStaffModeManager().shouldLockTools()) {
            return;
        }

        ItemStack currentItem = event.getCurrentItem();
        ItemStack cursorItem = event.getCursor();
        boolean clickOwnInventory = event.getClickedInventory() != null
                && event.getClickedInventory().equals(player.getInventory());
        boolean movingBetweenInventories = event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY
                || event.getClick().isKeyboardClick()
                || event.getAction() == InventoryAction.COLLECT_TO_CURSOR;
        boolean isDropAction = event.getAction() == InventoryAction.DROP_ALL_CURSOR
                || event.getAction() == InventoryAction.DROP_ONE_CURSOR
                || event.getAction() == InventoryAction.DROP_ALL_SLOT
                || event.getAction() == InventoryAction.DROP_ONE_SLOT;
        boolean isOutsideClick = event.getSlotType() == org.bukkit.event.inventory.InventoryType.SlotType.OUTSIDE
                || event.getClickedInventory() == null;

        if (!clickOwnInventory
                && !movingBetweenInventories
                && !isDropAction
                && !isOutsideClick
                && !plugin.getStaffModeManager().isStaffTool(currentItem)
                && !plugin.getStaffModeManager().isStaffTool(cursorItem)) {
            return;
        }

        event.setCancelled(true);
        event.setResult(org.bukkit.event.Event.Result.DENY);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (!plugin.getStaffModeManager().isInStaffMode(player.getUniqueId())
                || !plugin.getStaffModeManager().shouldLockTools()) {
            return;
        }

        event.setCancelled(true);
        event.setResult(org.bukkit.event.Event.Result.DENY);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getStaffModeManager().isInStaffMode(player.getUniqueId())
                || !plugin.getStaffModeManager().shouldLockTools()) {
            return;
        }

        event.setCancelled(true);
        org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
            if (player.isOnline()) {
                player.updateInventory();
            }
        });
        plugin.getStaffModeManager().sendToolLockedMessage(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSwap(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getStaffModeManager().isInStaffMode(player.getUniqueId())
                || !plugin.getStaffModeManager().shouldLockTools()) {
            return;
        }

        event.setCancelled(true);
        plugin.getStaffModeManager().sendToolLockedMessage(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (!plugin.getStaffModeManager().isInStaffMode(player.getUniqueId())
                || !plugin.getStaffModeManager().shouldLockTools()) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getStaffModeManager().isInStaffMode(player.getUniqueId())) {
            return;
        }
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        StaffToolType toolType = plugin.getStaffModeManager().resolveTool(event.getItem());
        if (toolType == null) {
            return;
        }

        event.setCancelled(true);
        event.setUseItemInHand(org.bukkit.event.Event.Result.DENY);
        event.setUseInteractedBlock(org.bukkit.event.Event.Result.DENY);

        Action action = event.getAction();
        boolean isLeftClickFreeze = (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK)
                && toolType == StaffToolType.FREEZE;
        boolean isRightClick = (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK);

        if (!isLeftClickFreeze && !isRightClick) {
            return;
        }

        if (isOnInteractCooldown(player)) {
            return;
        }

        if (isLeftClickFreeze) {
            plugin.getStaffModeManager().openFrozenPlayers(player);
            return;
        }

        switch (toolType) {
            case VANISH -> {
                if (!plugin.getStaffModeManager().canUseVanish(player)) {
                    player.sendMessage(ColorUtils.toComponent(
                            plugin.getStaffModeManager().getMessage("NO-PERMISSION", "&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ.")
                    ));
                    return;
                }
                plugin.getStaffModeManager().toggleVanish(player);
            }
            case STAFF_LIST -> {
                if (!plugin.getStaffModeManager().canOpenStaffList(player)) {
                    player.sendMessage(ColorUtils.toComponent(
                            plugin.getStaffModeManager().getMessage("NO-PERMISSION", "&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ.")
                    ));
                    return;
                }
                plugin.getStaffModeManager().openStaffList(player);
            }
            case BETTER_VIEW -> {
                if (!plugin.getStaffModeManager().canUseBetterView(player)) {
                    player.sendMessage(ColorUtils.toComponent(
                            plugin.getStaffModeManager().getMessage("NO-PERMISSION", "&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ.")
                    ));
                    return;
                }
                plugin.getStaffModeManager().toggleBetterView(player);
            }
            case RANDOM_TELEPORT -> {
                if (!plugin.getStaffModeManager().canUseRandomTeleport(player)) {
                    player.sendMessage(ColorUtils.toComponent(
                            plugin.getStaffModeManager().getMessage("NO-PERMISSION", "&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ.")
                    ));
                    return;
                }
                if (plugin.getStaffModeManager().teleportToRandomPlayer(player) == null) {
                    player.sendMessage(ColorUtils.toComponent(
                            plugin.getStaffModeManager().getRandomTeleportMessage("NO_PLAYERS", "&cɴᴏ ᴏᴛʜᴇʀ ᴘʟᴀʏᴇʀѕ ᴀᴠᴀɪʟᴀʙʟᴇ ꜰᴏʀ ʀᴀɴᴅᴏᴍ ᴛᴇʟᴇᴘᴏʀᴛ")
                    ));
                }
            }
            case CUSTOM -> plugin.getStaffModeManager().useCustomItem(player, event.getItem(), null);
            default -> {
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        lastInteractTimes.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getStaffModeManager().isInStaffMode(player.getUniqueId())
                || event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        StaffToolType toolType = plugin.getStaffModeManager().resolveTool(player.getInventory().getItemInMainHand());
        if (toolType == null) {
            return;
        }

        event.setCancelled(true);
        if (!(event.getRightClicked() instanceof Player target)) {
            return;
        }

        if (toolType == StaffToolType.CUSTOM) {
            if (isOnInteractCooldown(player)) {
                return;
            }
            plugin.getStaffModeManager().useCustomItem(
                    player, player.getInventory().getItemInMainHand(), target);
            return;
        }

        if (toolType != StaffToolType.FREEZE) {
            return;
        }

        handleFreezeInteraction(player, target);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        Player damager = resolveDamager(event.getDamager());
        if (damager == null || !plugin.getStaffModeManager().isInStaffMode(damager.getUniqueId())) {
            return;
        }

        StaffToolType toolType = plugin.getStaffModeManager().resolveTool(damager.getInventory().getItemInMainHand());
        if (toolType == null) {
            return;
        }

        event.setCancelled(true);
    }

    private void handleFreezeInteraction(Player staff, Player target) {
        FreezeManager freezeManager = plugin.getFreezeManager();
        if (!freezeManager.isEnabled()) {
            staff.sendMessage(ColorUtils.toComponent(
                    freezeManager.getMessage("FEATURE-DISABLED", "&cᴛʜᴇ ꜰʀᴇᴇᴢᴇ ѕʏѕᴛᴇᴍ ɪѕ ᴅɪѕᴀʙʟᴇᴅ.")
            ));
            return;
        }
        if (!freezeManager.canUse(staff)) {
            staff.sendMessage(ColorUtils.toComponent(
                    freezeManager.getMessage("NO-PERMISSION", "&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ.")
            ));
            return;
        }
        if (freezeManager.isSelfTarget(staff, target)) {
            staff.sendMessage(ColorUtils.toComponent(
                    freezeManager.getMessage("SELF-TARGET", "&cʏᴏᴜ ᴄᴀɴɴᴏᴛ ꜰʀᴇᴇᴢᴇ ʏᴏᴜʀѕᴇʟꜰ.")
            ));
            return;
        }

        FreezeManager.FreezeToggleResult result;
        if (freezeManager.hasActiveFreeze(target.getUniqueId())) {
            result = freezeManager.unfreeze(staff, target.getUniqueId());
        } else {
            if (!freezeManager.canFreeze(staff, target)) {
                staff.sendMessage(ColorUtils.toComponent(
                        freezeManager.getMessage("TARGET-EXEMPT", "&cʏᴏᴜ ᴄᴀɴɴᴏᴛ ꜰʀᴇᴇᴢᴇ ᴛʜᴀᴛ ᴘʟᴀʏᴇʀ.")
                ));
                return;
            }
            result = freezeManager.freeze(staff, target);
        }

        if (result != null) {
            staff.sendMessage(ColorUtils.toComponent(freezeManager.buildToggleMessage(result)));
        }
    }

    private boolean isOnInteractCooldown(Player player) {
        long now = System.currentTimeMillis();
        Long last = lastInteractTimes.get(player.getUniqueId());
        if (last != null && (now - last) < INTERACT_COOLDOWN_MS) {
            return true;
        }
        lastInteractTimes.put(player.getUniqueId(), now);
        return false;
    }

    private Player resolveDamager(Entity entity) {
        if (entity instanceof Player player) {
            return player;
        }
        return null;
    }
}
