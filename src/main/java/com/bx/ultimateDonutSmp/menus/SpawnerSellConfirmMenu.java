package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.SpawnerInstance;
import com.bx.ultimateDonutSmp.models.SpawnerTypeDefinition;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.List;

public class SpawnerSellConfirmMenu extends BaseMenu {

    private final long spawnerId;
    private final int returnPage;

    public SpawnerSellConfirmMenu(UltimateDonutSmp plugin, long spawnerId, int returnPage) {
        super(plugin, "&8Confirm Sell", 27);
        this.spawnerId = spawnerId;
        this.returnPage = Math.max(1, returnPage);
    }

    @Override
    public void build(Player player) {
        SpawnerInstance instance = plugin.getSpawnerManager().getSpawner(spawnerId);
        if (instance == null) {
            inventory = Bukkit.createInventory(this, 27, ColorUtils.toComponent("&8ѕᴘᴀᴡɴᴇʀ ᴍɪѕѕɪɴɢ"));
            clear();
            fill(Material.GRAY_STAINED_GLASS_PANE);
            set(13, ItemUtils.createItem(Material.BARRIER, "&cѕᴘᴀᴡɴᴇʀ ɴᴏᴛ ꜰᴏᴜɴᴅ"));
            return;
        }

        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);

        String mobLabel = instance.getMobTypeKey().replace('_', ' ').toUpperCase();
        long totalItems = instance.getTotalStoredItems();

        // Red Glass Pane (Cancel) - Slot 11
        set(11, ItemUtils.createItem(
                Material.RED_STAINED_GLASS_PANE,
                "&cCANCEL",
                List.of("&7Click to cancel sell and return to storage.")
        ));

        // Head Skin Mob - Slot 13
        SpawnerTypeDefinition def = plugin.getSpawnerManager().getTypeDefinition(instance.getMobTypeKey());
        String customTexture = def != null ? def.headTexture() : null;

        set(13, ItemUtils.createMobHead(
                instance.getMobTypeKey(),
                customTexture,
                "&e" + mobLabel + " LOOT SALE",
                List.of(
                        "&7Stored Items: &f" + NumberUtils.format(totalItems),
                        "",
                        "&eConfirm selling all stored loot?"
                )
        ));

        // Green Glass Pane (Confirm) - Slot 15
        set(15, ItemUtils.createItem(
                Material.GREEN_STAINED_GLASS_PANE,
                "&aCONFIRM SELL",
                List.of("&7Click to confirm and sell all mob drops.")
        ));
    }

    @Override
    public void handleClick(int slot, Player player, ClickType clickType) {
        SpawnerInstance instance = plugin.getSpawnerManager().getSpawner(spawnerId);
        if (instance == null) {
            player.closeInventory();
            return;
        }

        if (slot == 11) {
            plugin.getSpawnerManager().playSellCancelSound(player);
            new SpawnerStorageMenu(plugin, spawnerId, returnPage).open(player);
            return;
        }

        if (slot == 15) {
            var sellResult = plugin.getSpawnerManager().sellAllLoot(player, instance);
            player.sendMessage(ColorUtils.toComponent(sellResult.message()));
            new SpawnerStorageMenu(plugin, spawnerId, returnPage).open(player);
            return;
        }
    }
}
