package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.SpawnerInstance;
import com.bx.ultimateDonutSmp.models.SpawnerTypeDefinition;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;
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
            inventory = Bukkit.createInventory(this, 27, ColorUtils.toComponent("&8Spawner Missing"));
            clear();
            fill(Material.GRAY_STAINED_GLASS_PANE);
            set(13, ItemUtils.createItem(Material.BARRIER, "&cSpawner Not Found"));
            return;
        }

        FileConfiguration config = plugin.getConfigManager().getMenus();
        String titleStr = config.getString("SPAWNER-MENUS.SELL-CONFIRM-MENU.TITLE", "&8Confirm Sell");
        int menuSize = plugin.getSpawnerManager().normalizeSize(config.getInt("SPAWNER-MENUS.SELL-CONFIRM-MENU.SIZE", 27));
        inventory = Bukkit.createInventory(this, menuSize, ColorUtils.toComponent(titleStr));

        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);

        String mobLabel = instance.getMobTypeKey().replace('_', ' ').toUpperCase();
        long totalItems = instance.getTotalStoredItems();

        // 1. Cancel Button
        int cancelSlot = config.getInt("SPAWNER-MENUS.SELL-CONFIRM-MENU.CANCEL-BUTTON.SLOT", 11);
        String cancelMatName = config.getString("SPAWNER-MENUS.SELL-CONFIRM-MENU.CANCEL-BUTTON.MATERIAL", "RED_STAINED_GLASS_PANE");
        Material cancelMat = Material.matchMaterial(cancelMatName);
        if (cancelMat == null) cancelMat = Material.RED_STAINED_GLASS_PANE;
        String cancelTitle = config.getString("SPAWNER-MENUS.SELL-CONFIRM-MENU.CANCEL-BUTTON.TITLE", "&cCancel");
        List<String> cancelLore = config.getStringList("SPAWNER-MENUS.SELL-CONFIRM-MENU.CANCEL-BUTTON.LORE");
        if (cancelLore.isEmpty()) cancelLore = List.of("&7Click to cancel sell and return to storage.");
        set(cancelSlot, ItemUtils.createItem(cancelMat, cancelTitle, cancelLore));

        // 2. Info / Mob Head Icon
        int infoSlot = config.getInt("SPAWNER-MENUS.SELL-CONFIRM-MENU.INFO-ICON.SLOT", 13);
        SpawnerTypeDefinition def = plugin.getSpawnerManager().getTypeDefinition(instance.getMobTypeKey());
        String customTexture = def != null ? def.headTexture() : null;

        set(infoSlot, ItemUtils.createMobHead(
                instance.getMobTypeKey(),
                customTexture,
                "&e" + mobLabel + " LOOT SALE",
                List.of(
                        "&7Stored Items: &f" + NumberUtils.format(totalItems),
                        "",
                        "&eConfirm selling all stored loot?"
                )
        ));

        // 3. Confirm Button
        int confirmSlot = config.getInt("SPAWNER-MENUS.SELL-CONFIRM-MENU.CONFIRM-BUTTON.SLOT", 15);
        String confirmMatName = config.getString("SPAWNER-MENUS.SELL-CONFIRM-MENU.CONFIRM-BUTTON.MATERIAL", "GREEN_STAINED_GLASS_PANE");
        Material confirmMat = Material.matchMaterial(confirmMatName);
        if (confirmMat == null) confirmMat = Material.GREEN_STAINED_GLASS_PANE;
        String confirmTitle = config.getString("SPAWNER-MENUS.SELL-CONFIRM-MENU.CONFIRM-BUTTON.TITLE", "&aConfirm Sell");
        List<String> confirmLore = config.getStringList("SPAWNER-MENUS.SELL-CONFIRM-MENU.CONFIRM-BUTTON.LORE");
        if (confirmLore.isEmpty()) confirmLore = List.of("&7Click to confirm and sell all mob drops.");
        set(confirmSlot, ItemUtils.createItem(confirmMat, confirmTitle, confirmLore));
    }

    @Override
    public void handleClick(int slot, Player player, ClickType clickType) {
        SpawnerInstance instance = plugin.getSpawnerManager().getSpawner(spawnerId);
        if (instance == null) {
            player.closeInventory();
            return;
        }

        FileConfiguration config = plugin.getConfigManager().getMenus();
        int cancelSlot = config.getInt("SPAWNER-MENUS.SELL-CONFIRM-MENU.CANCEL-BUTTON.SLOT", 11);
        int confirmSlot = config.getInt("SPAWNER-MENUS.SELL-CONFIRM-MENU.CONFIRM-BUTTON.SLOT", 15);

        if (slot == cancelSlot) {
            plugin.getSpawnerManager().playSellCancelSound(player);
            new SpawnerStorageMenu(plugin, spawnerId, returnPage).open(player);
            return;
        }

        if (slot == confirmSlot) {
            var sellResult = plugin.getSpawnerManager().sellAllLoot(player, instance);
            player.sendMessage(ColorUtils.toComponent(sellResult.message()));
            new SpawnerStorageMenu(plugin, spawnerId, returnPage).open(player);
            return;
        }
    }
}
