package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.SpawnerInstance;
import com.bx.ultimateDonutSmp.models.SpawnerTypeDefinition;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
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
        super(plugin, plugin.getSpawnerManager().getPanelTitle(worldName), plugin.getSpawnerManager().getPanelSize());
        this.worldName = worldName;
        this.page = Math.max(1, page);
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);

        List<SpawnerInstance> spawners = plugin.getSpawnerManager().getSpawnersInWorld(worldName);
        String worldLabel = plugin.getSpawnerManager().describeWorld(worldName);
        int totalPages = Math.max(1, (int) Math.ceil(spawners.size() / (double) ITEMS_PER_PAGE));
        int safePage = Math.min(page, totalPages);
        int startIndex = (safePage - 1) * ITEMS_PER_PAGE;
        int endIndex = Math.min(spawners.size(), startIndex + ITEMS_PER_PAGE);

        for (int slot = 0; slot < ITEMS_PER_PAGE && startIndex + slot < endIndex; slot++) {
            SpawnerInstance instance = spawners.get(startIndex + slot);
            SpawnerTypeDefinition definition = plugin.getSpawnerManager().getTypeDefinition(instance.getMobTypeKey());
            Material icon = definition == null ? Material.SPAWNER : definition.iconMaterial();
            set(slot, ItemUtils.createItem(
                    icon,
                    "&b" + plugin.getSpawnerManager().getPlainTypeDisplayName(instance.getMobTypeKey()) + " &fx" + NumberUtils.format(instance.getStackAmount()),
                    List.of(
                            "&7ᴏᴡɴᴇʀ: &f" + instance.getOwnerNameSnapshot(),
                            "&7ᴡᴏʀʟᴅ: &f" + worldLabel,
                            "&7ᴄᴏᴏʀᴅѕ: &f" + instance.getX() + ", " + instance.getY() + ", " + instance.getZ(),
                            "&7ѕᴛᴏʀᴇᴅ ʟᴏᴏᴛ: &f" + NumberUtils.format(instance.getTotalStoredItems()),
                            "",
                            "&eᴄʟɪᴄᴋ ᴛᴏ ᴛᴇʟᴇᴘᴏʀᴛ ᴛᴏ ᴛʜɪѕ ѕᴘᴀᴡɴᴇʀ"
                    )
            ));
        }

        int lastRow = inventory.getSize() - 9;
        set(lastRow, ItemUtils.createItem(Material.COMPASS, "&b" + worldLabel, List.of(
                "&7ᴘᴀɢᴇ: &f" + safePage + "&7/&f" + totalPages,
                "&7ѕᴘᴀᴡɴᴇʀѕ ɪɴ ᴡᴏʀʟᴅ: &f" + spawners.size()
        )));
        set(lastRow + 1, safePage > 1
                ? ItemUtils.createItem(Material.ARROW, "&aᴘʀᴇᴠɪᴏᴜѕ ᴘᴀɢᴇ", List.of("&7ɢᴏ ᴛᴏ ᴘᴀɢᴇ &f" + (safePage - 1)))
                : ItemUtils.createPlaceholder(Material.BLACK_STAINED_GLASS_PANE));
        set(lastRow + 7, safePage < totalPages
                ? ItemUtils.createItem(Material.ARROW, "&aɴᴇxᴛ ᴘᴀɢᴇ", List.of("&7ɢᴏ ᴛᴏ ᴘᴀɢᴇ &f" + (safePage + 1)))
                : ItemUtils.createPlaceholder(Material.BLACK_STAINED_GLASS_PANE));
        set(lastRow + 8, ItemUtils.createItem(Material.OAK_DOOR, "&cʙᴀᴄᴋ", List.of("&7ʀᴇᴛᴜʀɴ ᴛᴏ ᴡᴏʀʟᴅ ʟɪѕᴛ.")));
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
            player.sendMessage(ColorUtils.toComponent("&cᴛʜᴀᴛ ѕᴘᴀᴡɴᴇʀ'ѕ ᴡᴏʀʟᴅ ɪѕ ɴᴏᴛ ᴄᴜʀʀᴇɴᴛʟʏ ʟᴏᴀᴅᴇᴅ."));
            return;
        }

        String worldLabel = plugin.getSpawnerManager().describeWorld(worldName);
        plugin.getFoliaScheduler().teleport(player, destination).thenAccept(success ->
                plugin.getFoliaScheduler().runEntity(player, () -> {
                    if (!Boolean.TRUE.equals(success) || !player.isOnline()) {
                        return;
                    }
                    player.sendMessage(ColorUtils.toComponent("&aᴛᴇʟᴇᴘᴏʀᴛᴇᴅ ᴛᴏ ѕᴘᴀᴡɴᴇʀ ᴀᴛ &f"
                            + instance.getX() + ", " + instance.getY() + ", " + instance.getZ() + "&a ɪɴ &f" + worldLabel + "&a."));
                    plugin.getAntiEspManager().updatePlayer(player);
                }));
    }
}
