package com.bx.ultimateDonutSmp.listeners;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.menus.BaseMenu;
import com.bx.ultimateDonutSmp.menus.SellMenu;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WorthDisplayListener implements Listener {

    private static final long AMETHYST_REFRESH_DELAY_TICKS = 20L;
    private final UltimateDonutSmp plugin;
    private final Set<UUID> dirtyPlayers = ConcurrentHashMap.newKeySet();
    private final Set<UUID> pendingRefreshes = ConcurrentHashMap.newKeySet();
    private final Set<UUID> forceUpdatePlayers = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Long> lastMiningTimes = new ConcurrentHashMap<>();

    public WorthDisplayListener(UltimateDonutSmp plugin) {
        this.plugin = plugin;
        startDirtySyncTask();
        registerAttemptPickupListener();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.getWorthManager().clearWorthDisplay(event.getPlayer());
        queueRefresh(event.getPlayer(), 2L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        pendingRefreshes.remove(uuid);
        dirtyPlayers.remove(uuid);
        forceUpdatePlayers.remove(uuid);
        lastMiningTimes.remove(uuid);
        plugin.getWorthManager().clearWorthDisplay(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        if (event.getNewGameMode() == GameMode.CREATIVE) {
            queueClear(event.getPlayer(), 1L);
            return;
        }

        queueRefresh(event.getPlayer(), 2L);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        if (isPlayerInventoryView(event.getInventory())) {
            return;
        }

        if (plugin.getInvseeManager().isInvseeInventory(event.getInventory())) {
            return;
        }

        if (!(event.getInventory().getHolder() instanceof SellMenu)
                && !(event.getInventory().getHolder() instanceof BaseMenu)) {
            plugin.getWorthManager().sanitizeInventory(event.getInventory());
        }

        if (isShulkerInventory(event.getInventory())) {
            queueRefresh(player, 1L);
            return;
        }

        queueRefresh(player, 1L);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        if (isAmethystItem(event.getCurrentItem()) || isAmethystItem(event.getCursor())) {
            return;
        }

        Inventory topInventory = event.getView().getTopInventory();
        if (topInventory.getHolder() instanceof SellMenu
                || topInventory.getHolder() instanceof BaseMenu
                || plugin.getInvseeManager().isInvseeInventory(topInventory)) {
            return;
        }

        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();

        boolean isStacking = cursor != null && !cursor.getType().isAir()
                && current != null && !current.getType().isAir()
                && cursor.getType() == current.getType();

        boolean strippedAny = false;

        if (isStacking) {
            ClickType click = event.getClick();
            if (click == ClickType.LEFT || click == ClickType.RIGHT) {
                int maxStack = current.getMaxStackSize();
                int currentAmount = current.getAmount();
                if (currentAmount < maxStack) {
                    event.setCancelled(true);

                    int cursorAmount = cursor.getAmount();
                    int transfer = (click == ClickType.LEFT)
                            ? Math.min(cursorAmount, maxStack - currentAmount)
                            : 1;

                    ItemStack newCurrent = current.clone();
                    newCurrent.setAmount(currentAmount + transfer);
                    newCurrent = plugin.getWorthManager().stripWorthDisplay(newCurrent);

                    ItemStack newCursor = cursor.clone();
                    newCursor.setAmount(cursorAmount - transfer);
                    if (newCursor.getAmount() <= 0) {
                        newCursor = null;
                    } else {
                        newCursor = plugin.getWorthManager().stripWorthDisplay(newCursor);
                    }

                    event.getClickedInventory().setItem(event.getSlot(), newCurrent);
                    player.setItemOnCursor(newCursor);

                    queueRefresh(player, 1L, true);
                    return;
                }
            }
        } else if (event.getClick().isShiftClick() && current != null && !current.getType().isAir()) {
            if (plugin.getWorthManager().stripStorageWorthDisplayForNativePickup(player, current)) {
                strippedAny = true;
            }

            ItemStack strippedCurrent = plugin.getWorthManager().stripWorthDisplay(current);
            if (strippedCurrent != current) {
                event.setCurrentItem(strippedCurrent);
                strippedAny = true;
            }
        }

        if (isShulkerInventory(topInventory)) {
            plugin.getWorthManager().sanitizeInventory(topInventory);
        }

        queueRefresh(player, 1L, strippedAny);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (isAmethystItem(event.getOldCursor())) {
            return;
        }

        if (isShulkerInventory(event.getView().getTopInventory())) {
            queueRefresh(player, 1L);
            return;
        }

        queueRefresh(player, 1L);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (isPlayerInventoryView(event.getInventory())) {
            return;
        }

        if (plugin.getInvseeManager().isInvseeInventory(event.getInventory())) {
            return;
        }

        if (!(event.getInventory().getHolder() instanceof SellMenu)
                && !(event.getInventory().getHolder() instanceof BaseMenu)) {
            plugin.getWorthManager().sanitizeInventory(event.getInventory());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        event.getItemDrop().setItemStack(
                plugin.getWorthManager().stripWorthDisplay(event.getItemDrop().getItemStack())
        );
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (player.getGameMode() == GameMode.CREATIVE) {
                return;
            }
            ItemStack current = event.getItem().getItemStack();
            if (isAmethystItem(current)) {
                return;
            }

            ItemStack stripped = plugin.getWorthManager().stripWorthDisplay(current);
            if (stripped != current) {
                event.getItem().setItemStack(stripped);
            }

            plugin.getWorthManager().stripStorageWorthDisplayForNativePickup(player, current);
            queueRefresh(player, 1L);
        }
    }

    private void registerAttemptPickupListener() {
        try {
            @SuppressWarnings("unchecked")
            Class<? extends org.bukkit.event.Event> eventClass = 
                (Class<? extends org.bukkit.event.Event>) Class.forName("org.bukkit.event.player.PlayerAttemptPickupItemEvent");
            
            plugin.getServer().getPluginManager().registerEvent(
                eventClass,
                this,
                EventPriority.LOWEST,
                (listener, event) -> {
                    try {
                        Player player = (Player) event.getClass().getMethod("getPlayer").invoke(event);
                        org.bukkit.entity.Item itemEntity = (org.bukkit.entity.Item) event.getClass().getMethod("getItem").invoke(event);
                        ItemStack current = itemEntity.getItemStack();
                        
                        if (player.getGameMode() != GameMode.CREATIVE && !isAmethystItem(current)) {
                            plugin.getWorthManager().stripStorageWorthDisplayForNativePickup(player, current);
                            queueRefresh(player, 1L);
                        }
                    } catch (Exception e) {
                        // ignore
                    }
                },
                plugin,
                true
            );
        } catch (ClassNotFoundException e) {
            // Not on Paper/Folia, ignore
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        List<org.bukkit.inventory.ItemStack> drops = event.getDrops();
        for (int index = 0; index < drops.size(); index++) {
            drops.set(index, plugin.getWorthManager().stripWorthDisplay(drops.get(index)));
        }

        UUID uuid = event.getEntity().getUniqueId();
        pendingRefreshes.remove(uuid);
        dirtyPlayers.remove(uuid);
        plugin.getWorthManager().clearWorthDisplay(event.getEntity());
    }

    private void queueRefresh(Player player, long delayTicks) {
        queueRefresh(player, delayTicks, false);
    }

    private void queueRefresh(Player player, long delayTicks, boolean forceUpdate) {
        long effectiveDelay = delayTicks;
        if (plugin.getAmethystToolsManager().isVisualSyncSuppressed(player.getUniqueId())) {
            effectiveDelay = Math.max(effectiveDelay, AMETHYST_REFRESH_DELAY_TICKS);
        }

        UUID uuid = player.getUniqueId();
        if (forceUpdate) {
            forceUpdatePlayers.add(uuid);
        }

        if (!pendingRefreshes.add(uuid)) {
            return;
        }

        plugin.getSpigotScheduler().runEntityLater(player, () -> {
            pendingRefreshes.remove(uuid);
            if (!player.isOnline()) {
                forceUpdatePlayers.remove(uuid);
                return;
            }
            dirtyPlayers.add(uuid);
        }, effectiveDelay);
    }

    private void queueClear(Player player, long delayTicks) {
        plugin.getSpigotScheduler().runEntityLater(player, () -> {
            if (!player.isOnline()) {
                return;
            }

            pendingRefreshes.remove(player.getUniqueId());
            dirtyPlayers.remove(player.getUniqueId());
            plugin.getWorthManager().clearWorthDisplay(player);
        }, delayTicks);
    }

    private void startDirtySyncTask() {
        plugin.getSpigotScheduler().runGlobalTimer(this::syncDirtyPlayers, 1L, 1L);
    }

    private void syncDirtyPlayers() {
        if (dirtyPlayers.isEmpty()) {
            return;
        }

        for (UUID uuid : Set.copyOf(dirtyPlayers)) {
            Player player = plugin.getServer().getPlayer(uuid);

            boolean forceUpdate = forceUpdatePlayers.contains(uuid);
            if (!forceUpdate && player != null && player.isOnline()) {
                long lastMining = lastMiningTimes.getOrDefault(uuid, 0L);
                if (System.currentTimeMillis() - lastMining < 1500L) {
                    continue;
                }
            }

            dirtyPlayers.remove(uuid);

            if (player == null || !player.isOnline()) {
                forceUpdatePlayers.remove(uuid);
                lastMiningTimes.remove(uuid);
                continue;
            }

            forceUpdatePlayers.remove(uuid);

            plugin.getSpigotScheduler().runEntity(player, () -> {
                if (!player.isOnline()) {
                    return;
                }

                if (player.getGameMode() == GameMode.CREATIVE) {
                    plugin.getWorthManager().clearWorthDisplay(player);
                    sanitizeOpenShulkerInventory(player);
                    return;
                }

                plugin.getWorthManager().syncWorthDisplay(player, forceUpdate);
                syncOpenShulkerInventory(player);

                Inventory topInventory = player.getOpenInventory().getTopInventory();
                if (topInventory != null && !isPlayerInventoryView(topInventory) && !isShulkerInventory(topInventory)) {
                    plugin.getWorthManager().sanitizeInventory(topInventory);
                }
            });
        }
    }

    private boolean isPlayerInventoryView(Inventory inventory) {
        return inventory != null
                && (inventory.getHolder() instanceof Player
                || inventory.getType() == InventoryType.CRAFTING
                || inventory.getType() == InventoryType.CREATIVE);
    }

    private void syncOpenShulkerInventory(Player player) {
        Inventory inventory = player.getOpenInventory().getTopInventory();
        if (!isShulkerInventory(inventory)) {
            return;
        }

        plugin.getWorthManager().syncWorthDisplay(player, inventory);
    }

    private void sanitizeOpenShulkerInventory(Player player) {
        Inventory inventory = player.getOpenInventory().getTopInventory();
        if (!isShulkerInventory(inventory)) {
            return;
        }

        plugin.getWorthManager().sanitizeInventory(inventory);
    }

    private boolean isShulkerInventory(Inventory inventory) {
        return inventory != null && inventory.getType() == InventoryType.SHULKER_BOX;
    }

    private boolean isAmethystItem(ItemStack item) {
        return plugin.getAmethystToolsManager().isAmethystTool(item);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onBlockDamage(BlockDamageEvent event) {
        lastMiningTimes.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            lastMiningTimes.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        lastMiningTimes.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
    }
}
