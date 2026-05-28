package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.DuelManager;
import com.bx.ultimateDonutSmp.models.DuelMapSelection;
import com.bx.ultimateDonutSmp.models.DuelStats;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class DuelQueueMenu extends BaseMenu {

    private static final int QUEUE_SLOT = 20;
    private static final int STATS_SLOT = 22;
    private static final int SELECT_SLOT = 24;
    private static final int CLAIMS_SLOT = 31;

    private final DuelMapSelection selectedSelection;

    public DuelQueueMenu(UltimateDonutSmp plugin) {
        this(plugin, null);
    }

    public DuelQueueMenu(UltimateDonutSmp plugin, DuelMapSelection selectedSelection) {
        super(plugin, plugin.getDuelManager().getQueueTitle(), plugin.getDuelManager().getQueueSize());
        this.selectedSelection = selectedSelection;
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);

        List<DuelManager.DuelMapOption> options = plugin.getDuelManager().getSelectableMapOptions(true);
        DuelManager.DuelMapOption selectedOption = resolveSelectedOption(options);
        DuelStats stats = plugin.getDuelManager().getStats(player.getUniqueId());
        boolean queued = plugin.getDuelManager().isInQueue(player.getUniqueId());
        boolean showSelector = shouldShowSelector(options);
        int claimsSlot = showSelector ? CLAIMS_SLOT : SELECT_SLOT;

        if (queued) {
            set(QUEUE_SLOT, ItemUtils.createItem(
                    Material.PAPER,
                    "&cLeave queue",
                    List.of(
                            "&7Players queued: &f" + plugin.getDuelManager().getQueueSizeCount(),
                            "&7Click to leave the duel queue."
                    )
            ));
        } else if (selectedOption == null) {
            set(QUEUE_SLOT, ItemUtils.createItem(
                    Material.BARRIER,
                    "&cNo queue maps available",
                    List.of("&7Configure queue arenas or enable random biomes.")
            ));
        } else {
            set(QUEUE_SLOT, ItemUtils.createItem(
                    Material.PAPER,
                    "&aJoin casual queue",
                    queueLore(selectedOption, showSelector)
            ));
        }

        if (showSelector) {
            set(SELECT_SLOT, ItemUtils.createItem(
                    Material.COMPASS,
                    "&bSelect map",
                    List.of(
                            selectedOption == null
                                    ? "&7No map is selected."
                                    : "&7Selected: &f" + selectedOption.displayName(),
                            "&eClick to choose arena or biome."
                    )
            ));
        }

        set(STATS_SLOT, ItemUtils.createItem(
                Material.NETHERITE_SWORD,
                "&eYour duel stats",
                List.of(
                        "&7Wins: &f" + stats.getWins(),
                        "&7Losses: &f" + stats.getLosses(),
                        "&7Draws: &f" + stats.getDraws(),
                        "&7Streak: &f" + stats.getCurrentStreak(),
                        "&7Best streak: &f" + stats.getBestStreak()
                )
        ));

        set(claimsSlot, ItemUtils.createItem(
                Material.ENDER_CHEST,
                "&dClaims",
                List.of("&7Open duel loot claim packages.")
        ));
        set(inventory.getSize() - 1, ItemUtils.createItem(Material.BARRIER, "&cClose"));
    }

    @Override
    public void handleClick(int slot, Player player) {
        List<DuelManager.DuelMapOption> options = plugin.getDuelManager().getSelectableMapOptions(true);
        DuelManager.DuelMapOption selectedOption = resolveSelectedOption(options);
        boolean queued = plugin.getDuelManager().isInQueue(player.getUniqueId());
        boolean showSelector = shouldShowSelector(options);
        int claimsSlot = showSelector ? CLAIMS_SLOT : SELECT_SLOT;

        if (slot == QUEUE_SLOT) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("DUELS.CLICK"));
            if (queued) {
                plugin.getDuelManager().leaveState(player);
                new DuelQueueMenu(plugin, selectedOption == null ? selectedSelection : selectedOption.selection()).open(player);
                return;
            }
            if (selectedOption == null) {
                new DuelQueueMenu(plugin).open(player);
                return;
            }

            plugin.getDuelManager().joinQueue(player, selectedOption.selection());
            if (plugin.getDuelManager().isInDuel(player.getUniqueId())) {
                player.closeInventory();
            } else {
                new DuelQueueMenu(plugin, selectedOption.selection()).open(player);
            }
            return;
        }

        if (showSelector && slot == SELECT_SLOT) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("DUELS.CLICK"));
            new DuelQueueMapSelectMenu(plugin, selectedOption == null ? selectedSelection : selectedOption.selection()).open(player);
            return;
        }

        if (slot == claimsSlot) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("DUELS.CLICK"));
            new DuelClaimMenu(plugin, 1).open(player);
            return;
        }

        if (slot == inventory.getSize() - 1) {
            player.closeInventory();
        }
    }

    private DuelManager.DuelMapOption resolveSelectedOption(List<DuelManager.DuelMapOption> options) {
        if (options == null || options.isEmpty()) {
            return null;
        }
        if (selectedSelection != null) {
            for (DuelManager.DuelMapOption option : options) {
                if (option.selection().equals(selectedSelection)) {
                    return option;
                }
            }
        }
        for (DuelManager.DuelMapOption option : options) {
            if (option.selection().type() == DuelMapSelection.Type.RANDOM_STATIC) {
                return option;
            }
        }
        return options.get(0);
    }

    private boolean shouldShowSelector(List<DuelManager.DuelMapOption> options) {
        if (!plugin.getDuelManager().isVanillaBiomeTerrainMode() || options == null) {
            return false;
        }
        for (DuelManager.DuelMapOption option : options) {
            if (option.selection().usesGeneratedWorld()) {
                return true;
            }
        }
        return false;
    }

    private List<String> queueLore(DuelManager.DuelMapOption selectedOption, boolean showSelector) {
        List<String> lore = new ArrayList<>();
        lore.add("&7Players queued: &f" + plugin.getDuelManager().getQueueSizeCount());
        if (showSelector) {
            lore.add("&7Selected: &f" + selectedOption.displayName());
            if (selectedOption.selection().usesGeneratedWorld()
                    && plugin.getDuelManager().isVanillaBiomeTerrainMode()
                    && !plugin.getDuelManager().isVanillaRuntimeGenerationEnabled()) {
                lore.add("&7Mode: &fVanilla generation disabled");
                lore.add("&7Enable VANILLA_POOL.RUNTIME_GENERATION.");
            } else {
                lore.add("&7" + selectedOption.description());
            }
        } else if (selectedOption.selection().usesGeneratedWorld()) {
            lore.add("&7Mode: &fFlat biome arena");
            lore.add("&7Uses lightweight generated flat terrain.");
        } else if (selectedOption.selection().type() == DuelMapSelection.Type.STATIC_ARENA) {
            lore.add("&7Map: &f" + selectedOption.displayName());
            lore.add("&7Uses a configured custom duel map.");
        } else {
            lore.add("&7Mode: &fDefault queue arena");
            lore.add("&7Uses an available configured duel arena.");
        }
        lore.add("&eClick to join queue.");
        return lore;
    }
}
