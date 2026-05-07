package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.SpawnerManager;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpawnerWorldListMenu extends BaseMenu {

    private static final int[] CENTER_PRIORITY_SLOTS = {11, 13, 15, 2, 4, 6, 20, 22, 24};

    private final List<SpawnerManager.WorldSummary> summaries;
    private final Map<Integer, SpawnerManager.WorldSummary> slotBindings = new HashMap<>();

    public SpawnerWorldListMenu(UltimateDonutSmp plugin) {
        super(plugin, plugin.getSpawnerManager().getWorldListTitle(), plugin.getSpawnerManager().getWorldListSize());
        this.summaries = plugin.getSpawnerManager().getWorldSummaries();
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);
        slotBindings.clear();

        List<Integer> layoutSlots = resolveLayoutSlots();
        int contentSlots = Math.min(summaries.size(), layoutSlots.size());
        for (int index = 0; index < contentSlots; index++) {
            int slot = layoutSlots.get(index);
            SpawnerManager.WorldSummary summary = summaries.get(index);
            World world = Bukkit.getWorld(summary.worldName());
            String worldLabel = plugin.getSpawnerManager().describeWorld(summary.worldName());
            set(slot, ItemUtils.createItem(
                    plugin.getSpawnerManager().getWorldIcon(world),
                    "&b" + worldLabel,
                    List.of(
                            "&7Managed spawners: &f" + summary.count(),
                            "",
                            "&eClick to browse " + worldLabel + "&e spawners"
                    )
            ));
            slotBindings.put(slot, summary);
        }
    }

    @Override
    public void handleClick(int slot, Player player, ClickType clickType) {
        SpawnerManager.WorldSummary summary = slotBindings.get(slot);
        if (summary == null) {
            return;
        }

        plugin.getSpawnerManager().openWorldPanel(player, summary.worldName(), 1);
    }

    private List<Integer> resolveLayoutSlots() {
        List<Integer> slots = new ArrayList<>();
        for (int slot : CENTER_PRIORITY_SLOTS) {
            if (slot >= 0 && slot < inventory.getSize()) {
                slots.add(slot);
            }
        }
        return slots;
    }
}
