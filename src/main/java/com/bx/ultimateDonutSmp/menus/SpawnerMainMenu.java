package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.SpawnerInstance;
import com.bx.ultimateDonutSmp.models.SpawnerLootEntry;
import com.bx.ultimateDonutSmp.models.SpawnerTypeDefinition;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class SpawnerMainMenu extends BaseMenu {

    private final long spawnerId;

    public SpawnerMainMenu(UltimateDonutSmp plugin, long spawnerId) {
        super(plugin, "&8ѕᴘᴀᴡɴᴇʀ", plugin.getSpawnerManager().getMainMenuSize());
        this.spawnerId = spawnerId;
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

        String titleStr = plugin.getSpawnerManager().getMainMenuTitle(instance);
        inventory = Bukkit.createInventory(this, plugin.getSpawnerManager().getMainMenuSize(), ColorUtils.toComponent(titleStr));

        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);

        // Slot 11: Spawner Storage (Chest)
        List<SpawnerLootEntry> lootEntries = instance.getStoredLootEntries();
        List<String> storageLore = new ArrayList<>();
        if (lootEntries.isEmpty()) {
            storageLore.add("&7No items stored.");
        } else {
            for (SpawnerLootEntry entry : lootEntries) {
                if (entry.getAmount() > 0) {
                    storageLore.add("&6" + NumberUtils.format(entry.getAmount()) + " &f" + plugin.getWorthManager().prettifyMaterial(entry.getMaterial()));
                }
            }
            if (storageLore.isEmpty()) {
                storageLore.add("&7No items stored.");
            }
        }
        storageLore.add("");
        storageLore.add("&eClick to view & collect spawner storage");

        set(11, ItemUtils.createItem(
                Material.CHEST,
                "&6ѕᴘᴀᴡɴᴇʀ ѕᴛᴏʀᴀɢᴇ",
                storageLore
        ));

        // Slot 13: Spawner Count (Mob Head)
        long totalCapacity = plugin.getSpawnerManager().getStorageCapPerLootKey();
        long currentTotal = instance.getTotalStoredItems();
        double fillPercentage = totalCapacity > 0 ? Math.min(100.0, (currentTotal / (double) totalCapacity) * 100.0) : 0.0;
        String mobLabel = instance.getMobTypeKey().replace('_', ' ').toUpperCase();

        SpawnerTypeDefinition def = plugin.getSpawnerManager().getTypeDefinition(instance.getMobTypeKey());
        String customTexture = def != null ? def.headTexture() : null;

        ItemStack headItem = ItemUtils.createMobHead(
                instance.getMobTypeKey(),
                customTexture,
                "&e" + NumberUtils.format(instance.getStackAmount()) + " " + mobLabel + " SPAWNERS",
                List.of(
                        "&e• &fClick to sell items and collect xp",
                        "&7Storage: &e" + String.format(java.util.Locale.US, "%.1f", fillPercentage) + "% Filled."
                )
        );
        set(13, headItem);

        // Slot 15: Collect XP (Experience Bottle)
        double storedXp = instance.getStoredXp();
        set(15, ItemUtils.createItem(
                Material.EXPERIENCE_BOTTLE,
                "&aᴄᴏʟʟᴇᴄᴛ xᴘ",
                List.of(
                        "&a" + String.format("%.1f", storedXp) + " &fXP Points",
                        "",
                        "&eClick to claim XP points"
                )
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
            // Open Spawner Storage Menu
            new SpawnerStorageMenu(plugin, spawnerId, 1).open(player);
            return;
        }

        if (slot == 13) {
            // Sell items & Collect XP in one click
            var result = plugin.getSpawnerManager().sellAndCollectXp(player, instance);
            player.sendMessage(ColorUtils.toComponent(result.message()));
            new SpawnerMainMenu(plugin, spawnerId).open(player);
            return;
        }

        if (slot == 15) {
            // Collect XP
            var result = plugin.getSpawnerManager().collectXp(player, instance);
            player.sendMessage(ColorUtils.toComponent(result.message()));
            new SpawnerMainMenu(plugin, spawnerId).open(player);
            return;
        }
    }
}
