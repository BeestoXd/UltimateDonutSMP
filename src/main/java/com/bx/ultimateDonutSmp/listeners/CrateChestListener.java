package com.bx.ultimateDonutSmp.listeners;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.CrateManager;
import com.bx.ultimateDonutSmp.managers.FeatureManager;
import com.bx.ultimateDonutSmp.menus.CrateGachaMenu;
import com.bx.ultimateDonutSmp.menus.CrateRewardMenu;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Iterator;

public class CrateChestListener implements Listener {

    private final UltimateDonutSmp plugin;

    public CrateChestListener(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!plugin.getFeatureManager().isEnabled(FeatureManager.Feature.CRATES)) {
            return;
        }
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Player player = event.getPlayer();
        Block clickedBlock = event.getClickedBlock();
        String pendingBindCrateId = plugin.getCrateManager().getPendingBindCrateId(player.getUniqueId());

        if (pendingBindCrateId != null && event.getAction() == Action.LEFT_CLICK_BLOCK && clickedBlock != null) {
            event.setCancelled(true);

            if (!plugin.getCrateManager().isBindableBlock(clickedBlock.getType())) {
                player.sendMessage(ColorUtils.toComponent("&cɪɴᴠᴀʟɪᴅ ʙʟᴏᴄᴋ. ʟᴇꜰᴛ-ᴄʟɪᴄᴋ ᴀ ᴄʜᴇѕᴛ, ᴛʀᴀᴘᴘᴇᴅ ᴄʜᴇѕᴛ, ʙᴀʀʀᴇʟ, ᴇɴᴅᴇʀ ᴄʜᴇѕᴛ, ᴏʀ ѕʜᴜʟᴋᴇʀ ʙᴏx."));
                return;
            }

            String previousCrateId = plugin.getCrateManager().getBoundCrateId(clickedBlock);
            if (!plugin.getCrateManager().bindCrateBlock(clickedBlock, pendingBindCrateId)) {
                player.sendMessage(ColorUtils.toComponent("&cꜰᴀɪʟᴇᴅ ᴛᴏ ʙɪɴᴅ ᴛʜᴀᴛ ᴄʀᴀᴛᴇ ᴄʜᴇѕᴛ."));
                return;
            }

            plugin.getCrateManager().clearPendingBind(player.getUniqueId());
            plugin.getCrateVisualManager().refreshHologram(clickedBlock);
            String location = formatBlockLocation(clickedBlock);
            if (previousCrateId != null && !previousCrateId.equalsIgnoreCase(pendingBindCrateId)) {
                player.sendMessage(ColorUtils.toComponent("&aᴜᴘᴅᴀᴛᴇᴅ ᴄʀᴀᴛᴇ ᴄʜᴇѕᴛ ᴀᴛ &f" + location
                        + "&a ꜰʀᴏᴍ &f" + previousCrateId + "&a ᴛᴏ &f" + pendingBindCrateId + "&a."));
                return;
            }

            player.sendMessage(ColorUtils.toComponent("&aʙᴏᴜɴᴅ ᴄʀᴀᴛᴇ &f" + pendingBindCrateId + "&a ᴛᴏ ᴄʜᴇѕᴛ ᴀᴛ &f" + location + "&a."));
            return;
        }

        if (pendingBindCrateId != null && event.getAction() == Action.RIGHT_CLICK_BLOCK && clickedBlock != null) {
            event.setCancelled(true);
            player.sendMessage(ColorUtils.toComponent("&eʙɪɴᴅ ᴍᴏᴅᴇ ɪѕ ᴀᴄᴛɪᴠᴇ. ʟᴇꜰᴛ-ᴄʟɪᴄᴋ ᴛʜᴇ ᴛᴀʀɢᴇᴛ ᴄʜᴇѕᴛ ᴛᴏ ꜰɪɴɪѕʜ ʙɪɴᴅɪɴɢ, ᴏʀ ᴜѕᴇ &f/crate ʙɪɴᴅ ᴄᴀɴᴄᴇʟ&e."));
            return;
        }

        if (clickedBlock == null) {
            return;
        }

        String crateId = plugin.getCrateManager().getBoundCrateId(clickedBlock);
        if (crateId == null) {
            return;
        }

        CrateManager.CrateDefinition crate = plugin.getCrateManager().getCrate(crateId);
        if (crate == null) {
            event.setCancelled(true);
            player.sendMessage(ColorUtils.toComponent("&cᴛʜɪѕ ᴄʀᴀᴛᴇ ᴄʜᴇѕᴛ ɪѕ ʙᴏᴜɴᴅ ᴛᴏ ᴀɴ ɪɴᴠᴀʟɪᴅ ᴄʀᴀᴛᴇ."));
            return;
        }

        if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        // both clicks open the same rewards menu; per-reward copy and the claim click
        // tell the player whether they have a key (no separate preview mode)
        event.setCancelled(true);
        plugin.getSpigotScheduler().runEntity(player, () -> openCrate(player, clickedBlock, crate));
    }

    private void openPreview(Player player, CrateManager.CrateDefinition crate) {
        if (!player.isOnline()) {
            return;
        }

        if (!crate.enabled()) {
            player.sendMessage(ColorUtils.toComponent("&cᴛʜɪѕ ᴄʀᴀᴛᴇ ɪѕ ᴄᴜʀʀᴇɴᴛʟʏ ᴅɪѕᴀʙʟᴇᴅ."));
            return;
        }

        if (!plugin.getCrateManager().hasAccess(player, crate)) {
            player.sendMessage(ColorUtils.toComponent("&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ ᴛᴏ ᴠɪᴇᴡ ᴛʜɪѕ ᴄʀᴀᴛᴇ."));
            return;
        }

        if (crate.rewards().isEmpty()) {
            player.sendMessage(ColorUtils.toComponent("&cᴛʜɪѕ ᴄʀᴀᴛᴇ ʜᴀѕ ɴᴏ ᴠᴀʟɪᴅ ʀᴇᴡᴀʀᴅѕ ᴄᴏɴꜰɪɢᴜʀᴇᴅ."));
            return;
        }

        new CrateRewardMenu(plugin, crate, CrateRewardMenu.OpenContext.PREVIEW).open(player);
    }

    private void openCrate(Player player, Block block, CrateManager.CrateDefinition crate) {
        if (!player.isOnline()) {
            return;
        }

        // a burst of interact events can schedule several opens in one tick; once the
        // first one opened a crate menu, silently drop the rest
        var holder = player.getOpenInventory().getTopInventory().getHolder();
        if (holder instanceof CrateRewardMenu || holder instanceof CrateGachaMenu) {
            return;
        }

        // gacha rolls a reward immediately, so it still needs the key up front;
        // the choose-one menu opens keyless and gates the key at the claim click
        boolean gacha = crate.openType() == CrateManager.OpenType.GACHA;
        CrateManager.OpenResult result = plugin.getCrateManager().startOpening(player, crate.id(), gacha);
        if (!result.success()) {
            player.sendMessage(ColorUtils.toComponent(result.message()));
            if (result.reason() == CrateManager.FailureReason.NO_KEYS) {
                plugin.getCrateVisualManager().playNoKeyEffects(player);
                // keyless gacha cannot roll; show the rewards preview instead
                openPreview(player, crate);
            }
            return;
        }

        plugin.getCrateVisualManager().playOpenEffects(player, block, crate);
        if (gacha) {
            new CrateGachaMenu(plugin, crate).open(player);
            return;
        }

        new CrateRewardMenu(plugin, crate, CrateRewardMenu.OpenContext.CHEST).open(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!plugin.getFeatureManager().isEnabled(FeatureManager.Feature.CRATES)) {
            return;
        }
        Block block = event.getBlock();
        if (plugin.getCrateManager().getBoundCrateId(block) == null) {
            return;
        }

        event.setCancelled(true);
        event.getPlayer().sendMessage(ColorUtils.toComponent("&cᴛʜᴀᴛ ᴄʀᴀᴛᴇ ᴄʜᴇѕᴛ ɪѕ ʙᴏᴜɴᴅ ᴀɴᴅ ᴄᴀɴɴᴏᴛ ʙᴇ ʙʀᴏᴋᴇɴ ᴜɴᴛɪʟ ɪᴛ ɪѕ ᴜɴʙᴏᴜɴᴅ."));
        plugin.getCrateVisualManager().playNoKeyEffects(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        if (!plugin.getFeatureManager().isEnabled(FeatureManager.Feature.CRATES)) {
            return;
        }
        filterBoundCrates(event.blockList().iterator());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (!plugin.getFeatureManager().isEnabled(FeatureManager.Feature.CRATES)) {
            return;
        }
        filterBoundCrates(event.blockList().iterator());
    }

    private void filterBoundCrates(Iterator<Block> iterator) {
        while (iterator.hasNext()) {
            Block block = iterator.next();
            if (plugin.getCrateManager().getBoundCrateId(block) != null) {
                iterator.remove();
            }
        }
    }

    private String formatBlockLocation(Block block) {
        return block.getWorld().getName() + " "
                + block.getX() + ","
                + block.getY() + ","
                + block.getZ();
    }
}
