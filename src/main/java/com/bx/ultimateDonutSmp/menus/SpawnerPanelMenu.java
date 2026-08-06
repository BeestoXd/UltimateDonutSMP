package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.SpawnerInstance;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.List;

public class SpawnerPanelMenu extends BaseMenu {

    private static final int ITEMS_PER_PAGE = 45;
    private final String worldName;
    private final int page;

    public SpawnerPanelMenu(UltimateDonutSmp plugin, String worldName, int page) {
        super(plugin, "&8Spawners", plugin.getSpawnerManager().getPanelSize());
        this.worldName = worldName;
        this.page = Math.max(1, page);
    }

    @Override
    public void build(Player player) {
        List<SpawnerInstance> spawners = plugin.getSpawnerManager().getSpawnersInWorld(worldName);
        int totalPages = Math.max(1, (int) Math.ceil(spawners.size() / (double) ITEMS_PER_PAGE));
        int safePage = Math.min(page, totalPages);
        String worldLabel = plugin.getSpawnerManager().describeWorld(worldName);

        inventory = Bukkit.createInventory(
                this,
                plugin.getSpawnerManager().getPanelSize(),
                ColorUtils.toComponent(plugin.getSpawnerManager().getPanelTitle(worldName))
        );
        clear();

        for (int r = inventory.getSize() - 9; r < inventory.getSize(); r++) {
            set(r, ItemUtils.createPlaceholder(Material.GRAY_STAINED_GLASS_PANE));
        }

        int startIndex = (safePage - 1) * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, spawners.size());

        for (int i = startIndex; i < endIndex; i++) {
            SpawnerInstance instance = spawners.get(i);
            int slot = i - startIndex;
            Material icon = plugin.getSpawnerManager().getTypeIcon(instance.getMobTypeKey());

            set(slot, ItemUtils.createItem(
                    icon,
                    "&b" + plugin.getSpawnerManager().getPlainTypeDisplayName(instance.getMobTypeKey()) + " &fx" + NumberUtils.format(instance.getStackAmount()),
                    List.of(
                            "&7Owner: &f" + instance.getOwnerNameSnapshot(),
                            "&7World: &f" + worldLabel,
                            "&7Coords: &f" + instance.getX() + ", " + instance.getY() + ", " + instance.getZ(),
                            "&7Stored Loot: &f" + NumberUtils.format(instance.getTotalStoredItems()),
                            "",
                            "&eClick to teleport to this spawner"
                    )
            ));
        }

        int lastRow = inventory.getSize() - 9;
        set(lastRow, ItemUtils.createItem(Material.COMPASS, "&b" + worldLabel, List.of(
                "&7Page: &f" + safePage + "&7/&f" + totalPages,
                "&7Spawners in World: &f" + spawners.size()
        )));
        set(lastRow + 1, safePage > 1
                ? ItemUtils.createItem(Material.ARROW, "&aPrevious Page", List.of("&7Go to page &f" + (safePage - 1)))
                : ItemUtils.createPlaceholder(Material.BLACK_STAINED_GLASS_PANE));
        set(lastRow + 7, safePage < totalPages
                ? ItemUtils.createItem(Material.ARROW, "&aNext Page", List.of("&7Go to page &f" + (safePage + 1)))
                : ItemUtils.createPlaceholder(Material.BLACK_STAINED_GLASS_PANE));
        set(lastRow + 8, ItemUtils.createItem(Material.OAK_DOOR, "&cBack", List.of("&7Return to world list.")));
    }

    @Override
    public void handleClick(int slot, Player player, ClickType clickType) {
        List<SpawnerInstance> spawners = plugin.getSpawnerManager().getSpawnersInWorld(worldName);
        int totalPages = Math.max(1, (int) Math.ceil(spawners.size() / (double) ITEMS_PER_PAGE));
        int safePage = Math.min(page, totalPages);
        int lastRow = inventory.getSize() - 9;

        if (slot == lastRow + 1 && safePage > 1) {
            new SpawnerPanelMenu(plugin, worldName, safePage - 1).open(player);
            return;
        }
        if (slot == lastRow + 7 && safePage < totalPages) {
            new SpawnerPanelMenu(plugin, worldName, safePage + 1).open(player);
            return;
        }
        if (slot == lastRow + 8) {
            new SpawnerWorldListMenu(plugin).open(player);
            return;
        }
        if (slot < 0 || slot >= ITEMS_PER_PAGE) {
            return;
        }

        int entryIndex = (safePage - 1) * ITEMS_PER_PAGE + slot;
        if (entryIndex < 0 || entryIndex >= spawners.size()) {
            return;
        }

        SpawnerInstance instance = spawners.get(entryIndex);
        Location destination = plugin.getSpawnerManager().getSpawnerCenter(instance).add(0, 1, 0);
        if (destination.getWorld() == null) {
            player.sendMessage(ColorUtils.toComponent("&cThat spawner's world is not currently loaded."));
            return;
        }

        String worldLabel = plugin.getSpawnerManager().describeWorld(worldName);
        plugin.getSpigotScheduler().teleport(player, destination).thenAccept(success ->
                plugin.getSpigotScheduler().runEntity(player, () -> {
                    if (!Boolean.TRUE.equals(success) || !player.isOnline()) {
                        return;
                    }
                    player.sendMessage(ColorUtils.toComponent("&aTeleported to spawner at &f"
                            + instance.getX() + ", " + instance.getY() + ", " + instance.getZ() + "&a in &f" + worldLabel + "&a."));
                    plugin.getAntiEspManager().updatePlayer(player);
                }));
    }
}
