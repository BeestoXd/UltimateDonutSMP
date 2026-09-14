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

    private final DuelMapSelection selectedSelection;

    public DuelQueueMenu(UltimateDonutSmp plugin) {
        this(plugin, null);
    }

    public DuelQueueMenu(UltimateDonutSmp plugin, DuelMapSelection selectedSelection) {
        super(plugin, plugin.getDuelManager().getQueueTitle(), plugin.getDuelManager().getQueueSize());
        this.selectedSelection = selectedSelection;
    }

    public static int resolveQueueSlot(int size) {
        int rows = Math.max(1, size / 9);
        if (rows >= 4) {
            return 20;
        } else if (rows == 3) {
            return 11;
        } else if (rows == 2) {
            return 2;
        } else {
            return 1;
        }
    }

    public static int resolveStatsSlot(int size) {
        int rows = Math.max(1, size / 9);
        if (rows >= 4) {
            return 22;
        } else if (rows == 3) {
            return 13;
        } else if (rows == 2) {
            return 4;
        } else {
            return 3;
        }
    }

    public static int resolveSelectSlot(int size) {
        int rows = Math.max(1, size / 9);
        if (rows >= 4) {
            return 24;
        } else if (rows == 3) {
            return 15;
        } else if (rows == 2) {
            return 6;
        } else {
            return 5;
        }
    }

    public static int resolveClaimsSlot(int size, boolean showSelector) {
        if (!showSelector) {
            return resolveSelectSlot(size);
        }
        int rows = Math.max(1, size / 9);
        if (rows >= 4) {
            return 31;
        } else if (rows == 3) {
            return 22;
        } else if (rows == 2) {
            return 13;
        } else {
            return 7;
        }
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

        int queueSlot = resolveQueueSlot(inventory.getSize());
        int statsSlot = resolveStatsSlot(inventory.getSize());
        int selectSlot = resolveSelectSlot(inventory.getSize());
        int claimsSlot = resolveClaimsSlot(inventory.getSize(), showSelector);

        if (queued) {
            set(queueSlot, ItemUtils.createItem(
                    Material.PAPER,
                    "&cleave queue",
                    List.of(
                            "&7players queued: &f" + plugin.getDuelManager().getQueueSizeCount(),
                            "&7click to leave the duel queue."
                    )
            ));
        } else if (selectedOption == null) {
            set(queueSlot, ItemUtils.createItem(
                    Material.BARRIER,
                    "&cno queue maps available",
                    List.of("&7configure queue arenas or enable random biomes.")
            ));
        } else {
            set(queueSlot, ItemUtils.createItem(
                    Material.PAPER,
                    "&ajoin casual queue",
                    queueLore(selectedOption, showSelector)
            ));
        }

        if (showSelector) {
            set(selectSlot, ItemUtils.createItem(
                    Material.COMPASS,
                    "&bselect map",
                    List.of(
                            selectedOption == null
                                    ? "&7no map is selected."
                                    : "&7selected: &f" + selectedOption.displayName(),
                            "&eclick to choose arena or biome."
                    )
            ));
        }

        set(statsSlot, ItemUtils.createItem(
                Material.NETHERITE_SWORD,
                "&eyour duel stats",
                List.of(
                        "&7wins: &f" + stats.getWins(),
                        "&7losses: &f" + stats.getLosses(),
                        "&7draws: &f" + stats.getDraws(),
                        "&7streak: &f" + stats.getCurrentStreak(),
                        "&7best streak: &f" + stats.getBestStreak()
                )
        ));

        set(claimsSlot, ItemUtils.createItem(
                Material.ENDER_CHEST,
                "&dclaims",
                List.of("&7open duel loot claim packages.")
        ));
        set(inventory.getSize() - 1, ItemUtils.createItem(Material.BARRIER, "&cclose"));
    }

    @Override
    public void handleClick(int slot, Player player) {
        List<DuelManager.DuelMapOption> options = plugin.getDuelManager().getSelectableMapOptions(true);
        DuelManager.DuelMapOption selectedOption = resolveSelectedOption(options);
        boolean queued = plugin.getDuelManager().isInQueue(player.getUniqueId());
        boolean showSelector = shouldShowSelector(options);

        int queueSlot = resolveQueueSlot(inventory.getSize());
        int selectSlot = resolveSelectSlot(inventory.getSize());
        int claimsSlot = resolveClaimsSlot(inventory.getSize(), showSelector);

        if (slot == queueSlot) {
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

        if (showSelector && slot == selectSlot) {
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
        lore.add("&7players queued: &f" + plugin.getDuelManager().getQueueSizeCount());
        if (showSelector) {
            lore.add("&7selected: &f" + selectedOption.displayName());
            if (selectedOption.selection().usesGeneratedWorld()
                    && plugin.getDuelManager().isVanillaBiomeTerrainMode()
                    && !plugin.getDuelManager().isVanillaRuntimeGenerationEnabled()) {
                lore.add("&7mode: &fvanilla generation disabled");
                lore.add("&7enable vanilla_pool.runtime_generation.");
            } else {
                lore.add("&7" + selectedOption.description());
            }
        } else if (selectedOption.selection().usesGeneratedWorld()) {
            lore.add("&7mode: &fflat biome arena");
            lore.add("&7uses lightweight generated flat terrain.");
        } else if (selectedOption.selection().type() == DuelMapSelection.Type.STATIC_ARENA) {
            lore.add("&7map: &f" + selectedOption.displayName());
            lore.add("&7uses a configured custom duel map.");
        } else {
            lore.add("&7mode: &fdefault queue arena");
            lore.add("&7uses an available configured duel arena.");
        }
        lore.add("&eclick to join queue.");
        return lore;
    }
}
