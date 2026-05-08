package com.bx.ultimateDonutSmp.amethyst;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.PlayerDataManager;
import com.bx.ultimateDonutSmp.managers.ShardManager;
import com.bx.ultimateDonutSmp.managers.ShopManager;
import com.bx.ultimateDonutSmp.models.EconomyReason;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class AmethystToolsListener implements Listener {

    private final UltimateDonutSmp plugin;
    private final AmethystToolsManager manager;

    public AmethystToolsListener(UltimateDonutSmp plugin) {
        this.plugin = plugin;
        this.manager = plugin.getAmethystToolsManager();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!manager.isAmethystTool(item)) {
            return;
        }

        if (item.getAmount() > 1) {
            manager.sanitizeHeldItem(player, false);
            return;
        }

        manager.ensureIdentity(item, player.getUniqueId(), false);
        AmethystToolType type = manager.getToolType(item);
        if (type != AmethystToolType.DRILL && type != AmethystToolType.SHOVEL && type != AmethystToolType.CHOPPER) {
            return;
        }

        if (!canUseTool(player, item, type, true, true)) {
            event.setCancelled(true);
            return;
        }

        switch (type) {
            case DRILL -> handleDrill(event, player, item);
            case SHOVEL -> handleShovel(event, player, item);
            case CHOPPER -> handleChopper(event, player, item);
            default -> {
            }
        }
    }

    private void handleDrill(BlockBreakEvent event, Player player, ItemStack item) {
        manager.suppressVisualSync(player.getUniqueId());
        Block origin = event.getBlock();
        ConfigurationSection cfg = manager.getToolSection(AmethystToolType.DRILL);
        int radius = cfg != null ? cfg.getInt("RADIUS", 1) : 1;

        Set<Material> disabled = manager.getDisabledBlocks();
        if (disabled.contains(origin.getType())) {
            return;
        }

        List<Block> toBreak = getAoeBlocks(origin, player, radius);
        toBreak.remove(origin);

        int count = 1;
        manager.spawnAmethystParticles(origin.getLocation());
        for (Block block : toBreak) {
            if (disabled.contains(block.getType()) || block.getType().isAir()) {
                continue;
            }
            block.breakNaturally(item);
            manager.spawnAmethystParticles(block.getLocation());
            count++;
        }

        SoundUtils.play(player, manager.getSound("BREAK"));
        if (count > 1 && shouldSendBreakMessages(player)) {
            player.sendMessage(ColorUtils.toComponent(
                    manager.getMessage("DRILL-BREAK", "{count}", String.valueOf(count))));
        }
    }

    private void handleShovel(BlockBreakEvent event, Player player, ItemStack item) {
        manager.suppressVisualSync(player.getUniqueId());
        Block origin = event.getBlock();
        ConfigurationSection cfg = manager.getToolSection(AmethystToolType.SHOVEL);
        int radius = cfg != null ? cfg.getInt("RADIUS", 1) : 1;

        Set<Material> allowed = manager.getAllowedBlocks();
        if (!allowed.contains(origin.getType())) {
            return;
        }

        List<Block> toBreak = getAoeBlocks(origin, player, radius);
        toBreak.remove(origin);

        int count = 1;
        manager.spawnAmethystParticles(origin.getLocation());
        for (Block block : toBreak) {
            if (!allowed.contains(block.getType()) || block.getType().isAir()) {
                continue;
            }
            block.breakNaturally(item);
            manager.spawnAmethystParticles(block.getLocation());
            count++;
        }

        SoundUtils.play(player, manager.getSound("BREAK"));
        if (count > 1 && shouldSendBreakMessages(player)) {
            player.sendMessage(ColorUtils.toComponent(
                    manager.getMessage("DRILL-BREAK", "{count}", String.valueOf(count))));
        }
    }

    private void handleChopper(BlockBreakEvent event, Player player, ItemStack item) {
        manager.suppressVisualSync(player.getUniqueId());
        Block origin = event.getBlock();
        Set<Material> logBlocks = manager.getLogBlocks();
        if (!logBlocks.contains(origin.getType())) {
            return;
        }

        ConfigurationSection cfg = manager.getToolSection(AmethystToolType.CHOPPER);
        int maxLogs = cfg != null ? cfg.getInt("MAX-LOGS", 128) : 128;

        List<Block> logs = bfsLogs(origin, logBlocks, maxLogs);
        logs.remove(origin);

        manager.spawnAmethystParticles(origin.getLocation());
        for (Block log : logs) {
            log.breakNaturally(item);
            manager.spawnAmethystParticles(log.getLocation());
        }

        SoundUtils.play(player, manager.getSound("BREAK"));
        if (shouldSendBreakMessages(player)) {
            player.sendMessage(ColorUtils.toComponent(
                    manager.getMessage("CHOP-BREAK", "{count}", String.valueOf(logs.size() + 1))));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!manager.isAmethystTool(item)) {
            return;
        }

        if (item.getAmount() > 1) {
            manager.sanitizeHeldItem(player, false);
            event.setCancelled(true);
            return;
        }

        manager.ensureIdentity(item, player.getUniqueId(), false);
        AmethystToolType type = manager.getToolType(item);
        if (type == null) {
            event.setCancelled(true);
            return;
        }

        switch (type) {
            case SELL_AXE -> {
                if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
                    return;
                }
                if (!canUseTool(player, item, type, true, true)) {
                    event.setCancelled(true);
                    return;
                }
                event.setCancelled(true);
                handleSellAxe(event, player);
            }
            case BUCKET -> {
                if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
                    return;
                }
                if (!canUseTool(player, item, type, true, true)) {
                    event.setCancelled(true);
                    return;
                }
                event.setCancelled(true);
                handleBucket(event, player);
            }
            default -> {
            }
        }
    }

    private void handleSellAxe(PlayerInteractEvent event, Player player) {
        manager.suppressVisualSync(player.getUniqueId());
        Block clicked = event.getClickedBlock();
        if (clicked == null) {
            player.sendMessage(ColorUtils.toComponent(manager.getMessage("SELL-NO-CHEST")));
            return;
        }

        if (clicked.getType() != Material.CHEST
                && clicked.getType() != Material.TRAPPED_CHEST
                && clicked.getType() != Material.BARREL) {
            player.sendMessage(ColorUtils.toComponent(manager.getMessage("SELL-NO-CHEST")));
            return;
        }

        Inventory containerInventory;
        try {
            org.bukkit.block.Container container = (org.bukkit.block.Container) clicked.getState();
            containerInventory = container.getInventory();
        } catch (ClassCastException e) {
            player.sendMessage(ColorUtils.toComponent(manager.getMessage("SELL-NO-CHEST")));
            return;
        }

        ShopManager shopManager = plugin.getShopManager();
        PlayerDataManager playerDataManager = plugin.getPlayerDataManager();
        PlayerData data = playerDataManager.get(player);
        if (data == null) {
            return;
        }

        double total = 0D;
        int soldSlots = 0;

        for (int i = 0; i < containerInventory.getSize(); i++) {
            ItemStack slotItem = containerInventory.getItem(i);
            if (slotItem == null || slotItem.getType().isAir()) {
                continue;
            }

            double unitWorth = shopManager.getWorth(slotItem);
            if (unitWorth <= 0D) {
                continue;
            }

            total += unitWorth * slotItem.getAmount();
            containerInventory.setItem(i, null);
            soldSlots++;
        }

        if (soldSlots == 0) {
            player.sendMessage(ColorUtils.toComponent(manager.getMessage("SELL-EMPTY")));
            return;
        }

        plugin.getEconomyManager().deposit(player, total, EconomyReason.AMETHYST_SELL);
        manager.spawnAmethystParticles(clicked.getLocation().add(0.5, 1, 0.5));
        SoundUtils.play(player, manager.getSound("USE"));
        player.sendMessage(ColorUtils.toComponent(
                manager.getMessage("SELL-SUCCESS", "{amount}", NumberUtils.formatNice(total))));
    }

    private void handleBucket(PlayerInteractEvent event, Player player) {
        manager.suppressVisualSync(player.getUniqueId());
        Block clicked = event.getClickedBlock();
        if (clicked == null || clicked.getType() != Material.WATER) {
            player.sendMessage(ColorUtils.toComponent(manager.getMessage("BUCKET-NO-WATER")));
            return;
        }

        ConfigurationSection cfg = manager.getToolSection(AmethystToolType.BUCKET);
        int radius = cfg != null ? cfg.getInt("DRAIN-RADIUS", 1) : 1;
        int maxDrain = cfg != null ? cfg.getInt("MAX-DRAIN", 27) : 27;

        List<Block> waterBlocks = bfsWater(clicked, radius, maxDrain);
        if (waterBlocks.isEmpty()) {
            player.sendMessage(ColorUtils.toComponent(manager.getMessage("BUCKET-NO-WATER")));
            return;
        }

        for (Block water : waterBlocks) {
            water.setType(Material.AIR);
            manager.spawnAmethystParticles(water.getLocation());
        }

        SoundUtils.play(player, manager.getSound("USE"));
        player.sendMessage(ColorUtils.toComponent(
                manager.getMessage("BUCKET-DRAIN", "{count}", String.valueOf(waterBlocks.size()))));
    }

    private void handleShardBooster(Player player) {
        manager.suppressVisualSync(player.getUniqueId());
        ShardManager shardManager = plugin.getShardManager();
        long durationMillis = manager.getShardBoosterDurationSeconds() * 1000L;
        boolean activated = shardManager.activateBooster(player, durationMillis);
        if (!activated) {
            player.sendMessage(ColorUtils.toComponent(manager.getMessage("BOOSTER-ALREADY")));
            return;
        }

        int slot = player.getInventory().getHeldItemSlot();
        player.getInventory().setItem(slot, null);
        SoundUtils.play(player, manager.getSound("ACTIVATE"));
        manager.spawnAmethystParticles(player.getLocation().add(0, 1, 0));
        player.sendMessage(ColorUtils.toComponent(manager.getMessage("BOOSTER-ACTIVATED")));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (!manager.isAmethystTool(item)) {
            return;
        }

        manager.ensureIdentity(item, player.getUniqueId(), false);
        AmethystToolType type = manager.getToolType(item);
        if (type != AmethystToolType.SHARD_BOOSTER) {
            return;
        }

        if (!canUseTool(player, item, type, false, true)) {
            event.setCancelled(true);
            return;
        }

        handleShardBooster(player);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        ItemStack current = event.getCurrentItem();
        ItemStack cursor = event.getCursor();
        if (!manager.isAmethystTool(current) && !manager.isAmethystTool(cursor)) {
            return;
        }

        if (manager.isAmethystTool(current)) {
            manager.ensureIdentity(current, event.getClickedInventory() == player.getInventory() ? player.getUniqueId() : null, false);
            if (current.getAmount() > 1) {
                if (event.getClickedInventory() == player.getInventory()) {
                    manager.sanitizeInventorySlot(player, event.getSlot(), false);
                } else {
                    manager.sanitizeExternalInventorySlot(player, event.getClickedInventory(), event.getSlot(), false);
                }
                event.setCancelled(true);
                return;
            }
            if (!manager.hasValidSignature(current)) {
                event.setCurrentItem(null);
                event.setCancelled(true);
                return;
            }
            if (manager.isExpired(current)) {
                event.setCurrentItem(null);
                event.setCancelled(true);
                return;
            }
        }

        if (manager.isAmethystTool(cursor)) {
            manager.ensureIdentity(cursor, player.getUniqueId(), false);
            if (cursor.getAmount() > 1) {
                manager.sanitizeCursorItem(player, false);
                event.setCancelled(true);
                return;
            }
            if (!manager.hasValidSignature(cursor) || manager.isExpired(cursor)) {
                player.setItemOnCursor(null);
                event.setCancelled(true);
                return;
            }
        }

        if (event.getAction() == InventoryAction.COLLECT_TO_CURSOR) {
            if (manager.isAmethystTool(current) || manager.isAmethystTool(cursor)) {
                event.setCancelled(true);
                return;
            }
        }

        if (manager.isAmethystTool(current) && manager.isAmethystTool(cursor)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        ItemStack oldCursor = event.getOldCursor();
        if (!manager.isAmethystTool(oldCursor)) {
            return;
        }

        manager.ensureIdentity(oldCursor, player.getUniqueId(), false);
        if (!manager.hasValidSignature(oldCursor) || manager.isExpired(oldCursor) || oldCursor.getAmount() > 1) {
            manager.sanitizeCursorItem(player, false);
        }
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        if (manager.isAmethystTool(event.getInventory().getItem(0))
                || manager.isAmethystTool(event.getInventory().getItem(1))) {
            event.setResult(null);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        if (!manager.shouldBlockAutomationPickup()) {
            return;
        }
        if (manager.isAmethystTool(event.getItem())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryPickupItem(InventoryPickupItemEvent event) {
        if (!manager.shouldBlockAutomationPickup()) {
            return;
        }
        if (manager.isAmethystTool(event.getItem().getItemStack())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Item droppedEntity = event.getItemDrop();
        ItemStack item = droppedEntity.getItemStack();
        if (!manager.isAmethystTool(item)) {
            return;
        }

        manager.ensureIdentity(item, player.getUniqueId(), false);
        if (!manager.hasValidSignature(item) || item.getAmount() > 1) {
            event.setCancelled(true);
            manager.sanitizeHeldItem(player, false);
            return;
        }

        if (manager.isExpired(item)) {
            event.setCancelled(true);
            manager.sanitizeHeldItem(player, true);
            return;
        }

        droppedEntity.setItemStack(item);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        Item itemEntity = event.getItem();
        ItemStack stack = itemEntity.getItemStack();
        if (!manager.isAmethystTool(stack)) {
            return;
        }

        manager.ensureIdentity(stack, player.getUniqueId(), false);
        if (!manager.hasValidSignature(stack) || stack.getAmount() > 1) {
            itemEntity.remove();
            event.setCancelled(true);
            return;
        }

        if (manager.isExpired(stack)) {
            itemEntity.remove();
            event.setCancelled(true);
            return;
        }

        if (!manager.isOwnedBy(player, stack)) {
            player.sendMessage(ColorUtils.toComponent(manager.getMessage("WRONG-OWNER")));
            event.setCancelled(true);
            return;
        }

        itemEntity.setItemStack(stack);
    }

    private boolean canUseTool(Player player, ItemStack item, AmethystToolType type, boolean checkCooldown, boolean sendFeedback) {
        if (!manager.hasValidSignature(item)) {
            return false;
        }

        if (manager.isExcludedWorld(player.getWorld().getName())) {
            if (sendFeedback) {
                player.sendMessage(ColorUtils.toComponent(manager.getMessage("EXCLUDED-WORLD")));
            }
            return false;
        }

        if (manager.isExpired(item)) {
            manager.expireHeldItem(player);
            return false;
        }

        String permission = manager.getToolPermission(type);
        if (!permission.isBlank() && !player.hasPermission(permission)) {
            if (sendFeedback) {
                player.sendMessage(ColorUtils.toComponent(manager.getMessage("NO-PERMISSION")));
            }
            return false;
        }

        if (!manager.isOwnedBy(player, item)) {
            if (sendFeedback) {
                player.sendMessage(ColorUtils.toComponent(manager.getMessage("WRONG-OWNER")));
            }
            return false;
        }

        if (checkCooldown && manager.isOnCooldown(player.getUniqueId())) {
            return false;
        }

        if (checkCooldown) {
            manager.stampCooldown(player.getUniqueId());
        }

        return true;
    }

    private boolean shouldSendBreakMessages(Player player) {
        PlayerData data = plugin.getPlayerDataManager().get(player);
        return data == null || data.isAmethystBreakMessagesEnabled();
    }

    private List<Block> getAoeBlocks(Block origin, Player player, int radius) {
        List<Block> blocks = new ArrayList<>();
        BlockFace face = getPlayerFace(player);
        int ox = origin.getX();
        int oy = origin.getY();
        int oz = origin.getZ();

        for (int a = -radius; a <= radius; a++) {
            for (int b = -radius; b <= radius; b++) {
                Block block;
                if (face == BlockFace.UP || face == BlockFace.DOWN) {
                    block = origin.getWorld().getBlockAt(ox + a, oy, oz + b);
                } else if (face == BlockFace.NORTH || face == BlockFace.SOUTH) {
                    block = origin.getWorld().getBlockAt(ox + a, oy + b, oz);
                } else {
                    block = origin.getWorld().getBlockAt(ox, oy + b, oz + a);
                }
                blocks.add(block);
            }
        }

        return blocks;
    }

    private BlockFace getPlayerFace(Player player) {
        float yaw = player.getLocation().getYaw();
        float pitch = player.getLocation().getPitch();
        if (pitch < -45) {
            return BlockFace.UP;
        }
        if (pitch > 45) {
            return BlockFace.DOWN;
        }
        yaw = ((yaw % 360) + 360) % 360;
        if (yaw < 45 || yaw >= 315) {
            return BlockFace.SOUTH;
        }
        if (yaw < 135) {
            return BlockFace.WEST;
        }
        if (yaw < 225) {
            return BlockFace.NORTH;
        }
        return BlockFace.EAST;
    }

    private List<Block> bfsLogs(Block start, Set<Material> logMaterials, int maxLogs) {
        List<Block> result = new ArrayList<>();
        Set<org.bukkit.Location> visited = new java.util.HashSet<>();
        Queue<Block> queue = new LinkedList<>();
        queue.add(start);
        visited.add(start.getLocation());

        BlockFace[] faces = {
                BlockFace.UP, BlockFace.DOWN,
                BlockFace.NORTH, BlockFace.SOUTH,
                BlockFace.EAST, BlockFace.WEST
        };

        while (!queue.isEmpty() && result.size() < maxLogs) {
            Block current = queue.poll();
            if (!logMaterials.contains(current.getType())) {
                continue;
            }
            result.add(current);

            for (BlockFace face : faces) {
                Block neighbor = current.getRelative(face);
                if (!visited.contains(neighbor.getLocation()) && logMaterials.contains(neighbor.getType())) {
                    visited.add(neighbor.getLocation());
                    queue.add(neighbor);
                }
            }
        }

        return result;
    }

    private List<Block> bfsWater(Block start, int radius, int max) {
        List<Block> result = new ArrayList<>();
        Set<org.bukkit.Location> visited = new java.util.HashSet<>();
        Queue<Block> queue = new LinkedList<>();

        int sx = start.getX();
        int sy = start.getY();
        int sz = start.getZ();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    Block block = start.getWorld().getBlockAt(sx + dx, sy + dy, sz + dz);
                    if (block.getType() == Material.WATER && visited.add(block.getLocation())) {
                        queue.add(block);
                    }
                }
            }
        }

        BlockFace[] faces = {
                BlockFace.UP, BlockFace.DOWN,
                BlockFace.NORTH, BlockFace.SOUTH,
                BlockFace.EAST, BlockFace.WEST
        };

        while (!queue.isEmpty() && result.size() < max) {
            Block current = queue.poll();
            if (current.getType() != Material.WATER) {
                continue;
            }
            result.add(current);

            for (BlockFace face : faces) {
                Block neighbor = current.getRelative(face);
                if (result.size() + queue.size() >= max) {
                    break;
                }
                if (neighbor.getType() == Material.WATER && visited.add(neighbor.getLocation())) {
                    queue.add(neighbor);
                }
            }
        }

        return result;
    }
}
