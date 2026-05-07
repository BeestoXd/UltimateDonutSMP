package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.BillfordManager;
import com.bx.ultimateDonutSmp.models.EconomyReason;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class BillfordMenu extends BaseMenu {

    private static final String MENU_PATH = "BILLFORD-MENU";

    private final int rewardSlot;
    private final int clockSlot;
    private final int infoSlot;
    private final int confirmSlot;
    private final Material fillerMaterial;
    private final boolean closeOnSuccess;
    private final boolean reopenOnTradeChange;
    private final Set<ClickType> allowedClickTypes;

    private BillfordManager.BillfordTrade currentTrade;
    private int openedTradeId;

    public BillfordMenu(UltimateDonutSmp plugin) {
        super(plugin, resolveTitle(plugin), resolveSize(plugin));
        FileConfiguration config = plugin.getConfigManager().getBillford();
        rewardSlot = config.getInt("GUI.REWARD_SLOT", 8);
        clockSlot = config.getInt("GUI.COUNTDOWN_SLOT", 13);
        infoSlot = config.getInt("GUI.INFO_SLOT", 22);
        confirmSlot = config.getInt("GUI.CONFIRM_SLOT", 26);
        fillerMaterial = ItemUtils.parseMaterial(config.getString("GUI.FILLER_MATERIAL", "GRAY_STAINED_GLASS_PANE"));
        closeOnSuccess = config.getBoolean("SETTINGS.CLOSE_MENU_ON_SUCCESS", true);
        reopenOnTradeChange = config.getBoolean("SETTINGS.REOPEN_ON_TRADE_CHANGE", true);
        allowedClickTypes = resolveAllowedClickTypes(config.getStringList("SETTINGS.ALLOWED_CLICK_TYPES"));
    }

    private static String resolveTitle(UltimateDonutSmp plugin) {
        return plugin.getConfigManager().getMenus().getString(
                MENU_PATH + ".TITLE",
                plugin.getConfigManager().getBillford().getString("GUI.TITLE", "&8&lBillford &7Trade")
        );
    }

    private static int resolveSize(UltimateDonutSmp plugin) {
        int configured = plugin.getConfigManager().getMenus().getInt(
                MENU_PATH + ".SIZE",
                plugin.getConfigManager().getBillford().getInt("GUI.SIZE", 27)
        );
        int normalized = Math.max(9, Math.min(54, configured));
        int remainder = normalized % 9;
        return remainder == 0 ? normalized : normalized + (9 - remainder);
    }

    private Set<ClickType> resolveAllowedClickTypes(List<String> configured) {
        Set<ClickType> defaults = EnumSet.of(ClickType.LEFT, ClickType.RIGHT);
        if (configured == null || configured.isEmpty()) {
            return defaults;
        }

        Set<ClickType> result = EnumSet.noneOf(ClickType.class);
        for (String raw : configured) {
            if (raw == null || raw.isBlank()) {
                continue;
            }
            try {
                result.add(ClickType.valueOf(raw.trim().toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return result.isEmpty() ? defaults : result;
    }

    @Override
    public void build(Player player) {
        clear();
        fill(fillerMaterial);

        BillfordManager manager = plugin.getBillfordManager();
        currentTrade = manager.getCurrentTrade();
        if (currentTrade == null) {
            set(inventory.getSize() / 2, ItemUtils.createItem(
                    Material.BARRIER,
                    "&cNo Billford trade configured",
                    List.of("&7Add entries inside &fbillford.yml &7to enable the system.")
            ));
            return;
        }

        openedTradeId = currentTrade.id();
        int playerCount = manager.getPlayerTradeCount(player.getUniqueId());
        boolean limitReached = manager.hasReachedLimit(player.getUniqueId());

        for (BillfordManager.BillfordSlot requiredSlot : currentTrade.requiredItems()) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("{item}", formatName(requiredSlot.material().name()));
            placeholders.put("{amount}", String.valueOf(requiredSlot.quantity()));
            placeholders.put("{slot}", String.valueOf(requiredSlot.slot()));

            ItemStack item = ItemUtils.createItem(
                    requiredSlot.material(),
                    replacePlaceholders(
                            getMenuString(
                                    "TRADE-BUTTON.MATERIAL.NAME",
                                    getConfigString("GUI.INPUT_NAME", "&f{item}")
                            ),
                            placeholders
                    ),
                    replaceLore(
                            getMenuStringList(
                                    "TRADE-BUTTON.MATERIAL.LORE",
                                    getConfigStringList(
                                            "GUI.INPUT_LORE",
                                            List.of(
                                                    "&7Cost: &f{amount}x &e{item}",
                                                    "",
                                                    "&8Must be in your inventory."
                                            )
                                    )
                            ),
                            placeholders
                    )
            );
            item.setAmount(Math.min(requiredSlot.quantity(), 64));
            set(requiredSlot.slot() - 1, item);
        }

        Map<String, String> rewardPlaceholders = new HashMap<>();
        rewardPlaceholders.put("{item}", formatName(currentTrade.rewardMaterial().name()));
        rewardPlaceholders.put("{amount}", String.valueOf(currentTrade.rewardQuantity()));
        rewardPlaceholders.put(
                "{money_line}",
                currentTrade.moneyBonus() > 0
                        ? "&7Money Bonus: &a$" + NumberUtils.format(currentTrade.moneyBonus())
                        : ""
        );
        rewardPlaceholders.put(
                "{shard_line}",
                currentTrade.shardBonus() > 0
                        ? "&7Shard Bonus: &#A303F9+" + currentTrade.shardBonus()
                        : ""
        );

        ItemStack rewardDisplay = ItemUtils.createItem(
                currentTrade.rewardMaterial(),
                replacePlaceholders(getConfigString("GUI.REWARD_NAME", "&aReward"), rewardPlaceholders),
                replaceLore(
                        getConfigStringList(
                                "GUI.REWARD_LORE",
                                List.of(
                                        "&7Reward: &a{amount}x &f{item}",
                                        "{money_line}",
                                        "{shard_line}",
                                        "",
                                        "&8Awarded upon successful trade."
                                )
                        ),
                        rewardPlaceholders
                )
        );
        rewardDisplay.setAmount(Math.min(currentTrade.rewardQuantity(), 64));
        set(rewardSlot, rewardDisplay);

        Map<String, String> timerPlaceholders = new HashMap<>();
        timerPlaceholders.put("{trade_name}", currentTrade.displayName());
        timerPlaceholders.put("{countdown}", manager.getFormattedCountdown());
        timerPlaceholders.put("{trade_id}", String.valueOf(manager.getCurrentTradeId()));
        timerPlaceholders.put("{trade_count}", String.valueOf(manager.getTradeCount()));
        set(clockSlot, ItemUtils.createItem(
                Material.CLOCK,
                replacePlaceholders(getConfigString("GUI.COUNTDOWN_NAME", "&e{trade_name}"), timerPlaceholders),
                replaceLore(
                        getConfigStringList(
                                "GUI.COUNTDOWN_LORE",
                                List.of(
                                        "&7Next rotation: &b{countdown}",
                                        "",
                                        "&8Trade &f{trade_id} &8of &f{trade_count}"
                                )
                        ),
                        timerPlaceholders
                )
        ));

        Map<String, String> infoPlaceholders = new HashMap<>();
        infoPlaceholders.put("{used}", String.valueOf(playerCount));
        infoPlaceholders.put("{limit}", String.valueOf(currentTrade.tradeLimit()));
        infoPlaceholders.put("{remaining}", String.valueOf(Math.max(0, currentTrade.tradeLimit() - playerCount)));
        infoPlaceholders.put(
                "{status_line}",
                limitReached
                        ? "&c&lLimit reached this rotation."
                        : "&aYou can trade &f" + Math.max(0, currentTrade.tradeLimit() - playerCount) + " &amore time(s)."
        );

        List<String> infoLore = currentTrade.tradeLimit() > 0
                ? replaceLore(
                getConfigStringList(
                        "GUI.INFO_LIMITED_LORE",
                        List.of(
                                "&7Trades: &f{used} &7/ &f{limit}",
                                "{status_line}"
                        )
                ),
                infoPlaceholders
        )
                : replaceLore(
                getConfigStringList(
                        "GUI.INFO_UNLIMITED_LORE",
                        List.of(
                                "&7Trades done: &f{used}",
                                "&aUnlimited trades this rotation."
                        )
                ),
                infoPlaceholders
        );

        set(infoSlot, ItemUtils.createItem(
                ItemUtils.parseMaterial(getConfigString("GUI.INFO_MATERIAL", "BOOK")),
                getConfigString("GUI.INFO_NAME", "&6Trade Info"),
                infoLore
        ));

        if (limitReached) {
            set(confirmSlot, ItemUtils.createItem(
                    ItemUtils.parseMaterial(getConfigString("GUI.LIMIT_BUTTON.MATERIAL", "BARRIER")),
                    getConfigString("GUI.LIMIT_BUTTON.NAME", "&c&lLimit Reached"),
                    getConfigStringList(
                            "GUI.LIMIT_BUTTON.LORE",
                            List.of(
                                    "&cTrade limit reached for this rotation.",
                                    "&7Check back after the next rotation."
                            )
                    )
            ));
        } else {
            set(confirmSlot, ItemUtils.createItem(
                    ItemUtils.parseMaterial(getMenuString(
                            "CONFIRM-TRADE-BUTTON.MATERIAL",
                            getConfigString("GUI.CONFIRM_BUTTON.MATERIAL", "EMERALD")
                    )),
                    getMenuString(
                            "CONFIRM-TRADE-BUTTON.NAME",
                            getConfigString("GUI.CONFIRM_BUTTON.NAME", "&a&lConfirm Trade")
                    ),
                    getMenuStringList(
                            "CONFIRM-TRADE-BUTTON.LORE",
                            getConfigStringList(
                                    "GUI.CONFIRM_BUTTON.LORE",
                                    List.of(
                                            "&7Have the required items in your",
                                            "&7inventory, then click to trade.",
                                            "",
                                            "&a&lClick to Confirm"
                                    )
                            )
                    )
            ));
        }
    }

    @Override
    public void handleClick(int slot, Player player, ClickType clickType) {
        if (slot != confirmSlot || currentTrade == null) {
            return;
        }

        if (!allowedClickTypes.contains(clickType)) {
            return;
        }

        SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));

        BillfordManager manager = plugin.getBillfordManager();
        if (!manager.beginTrade(player.getUniqueId())) {
            fail(player, "BILLFORD.BUSY", "&cBillford is already processing your last click.");
            return;
        }

        try {
            if (manager.isOnCooldown(player.getUniqueId())) {
                fail(player, "BILLFORD.CLICK-COOLDOWN", "&cSlow down. Billford is still checking your last trade.");
                return;
            }
            manager.updateCooldown(player.getUniqueId());

            if (openedTradeId != manager.getCurrentTradeId()) {
                player.sendMessage(ColorUtils.toComponent(
                        plugin.getConfigManager().getMessage(
                                "BILLFORD.TRADE-CHANGED",
                                "{countdown}",
                                manager.getFormattedCountdown()
                        )
                ));
                SoundUtils.play(player, plugin.getConfigManager().getSound("BILLFORD.FAIL"));
                if (reopenOnTradeChange) {
                    new BillfordMenu(plugin).open(player);
                } else {
                    player.closeInventory();
                }
                return;
            }

            BillfordManager.BillfordTrade liveTrade = manager.getCurrentTrade();
            if (liveTrade == null) {
                fail(player, "BILLFORD.NOT-CONFIGURED", "&cBillford trade is not configured right now.");
                return;
            }

            if (manager.hasReachedLimit(player.getUniqueId())) {
                fail(player, "BILLFORD.LIMIT-REACHED", "&cYou've reached the trade limit for this rotation.");
                return;
            }

            if (!hasRequiredItems(player, liveTrade)) {
                fail(player, "BILLFORD.REQUIRED_CONTENTS", "&cYou don't have all the required items for this trade.");
                return;
            }

            ItemStack reward = new ItemStack(liveTrade.rewardMaterial(), liveTrade.rewardQuantity());
            if (!canFitReward(player, reward)) {
                fail(player, "BILLFORD.FULL-INVENTORY", "&cYour inventory is too full to receive the reward.");
                return;
            }

            PlayerInventory playerInventory = player.getInventory();
            ItemStack[] originalStorage = cloneStorage(playerInventory.getStorageContents());
            ItemStack[] updatedStorage = cloneStorage(originalStorage);
            if (!consumeRequiredItems(updatedStorage, liveTrade)) {
                fail(player, "BILLFORD.REQUIRED_CONTENTS", "&cYou don't have all the required items for this trade.");
                return;
            }

            playerInventory.setStorageContents(updatedStorage);
            Map<Integer, ItemStack> leftovers = playerInventory.addItem(reward.clone());
            if (!leftovers.isEmpty()) {
                playerInventory.setStorageContents(originalStorage);
                fail(player, "BILLFORD.FULL-INVENTORY", "&cYour inventory is too full to receive the reward.");
                return;
            }

            if (liveTrade.shardBonus() > 0) {
                plugin.getShardManager().giveShards(player, liveTrade.shardBonus(), true);
            }

            if (liveTrade.moneyBonus() > 0) {
                PlayerData data = plugin.getPlayerDataManager().get(player);
                if (data == null) {
                    data = plugin.getPlayerDataManager().loadOrCreate(player);
                }
                var depositResult = plugin.getEconomyManager().deposit(player, liveTrade.moneyBonus(), EconomyReason.BILLFORD_REWARD);
                if (!depositResult.success()) {
                    fail(player, "BILLFORD.BUSY", "&cBillford could not complete the money reward.");
                    return;
                }
            }

            manager.incrementPlayerTradeCount(player.getUniqueId());

            String success = plugin.getConfigManager().getMessage(
                    "BILLFORD.TRADE-COMPLETED",
                    "{reward}", liveTrade.rewardQuantity() + "x " + formatName(liveTrade.rewardMaterial().name()),
                    "{money_bonus}", NumberUtils.format(liveTrade.moneyBonus()),
                    "{shard_bonus}",
                    liveTrade.shardBonus() > 0 ? String.valueOf(liveTrade.shardBonus()) : "0",
                    "{next_rotation}", manager.getFormattedCountdown()
            );
            player.sendMessage(ColorUtils.toComponent(success));
            SoundUtils.play(player, plugin.getConfigManager().getSound("BILLFORD.SUCCESS"));
            playSuccessParticle(player);
            player.updateInventory();

            if (closeOnSuccess) {
                player.closeInventory();
            } else {
                build(player);
            }
        } finally {
            manager.endTrade(player.getUniqueId());
        }
    }

    private void fail(Player player, String messagePath, String fallbackMessage) {
        String message = plugin.getConfigManager().getMessage(messagePath);
        if (message.startsWith("&cMessage not found")) {
            message = fallbackMessage;
        }
        player.sendMessage(ColorUtils.toComponent(message));
        SoundUtils.play(player, plugin.getConfigManager().getSound("BILLFORD.FAIL"));
    }

    private boolean hasRequiredItems(Player player, BillfordManager.BillfordTrade trade) {
        for (BillfordManager.BillfordSlot requiredSlot : trade.requiredItems()) {
            int found = 0;
            for (ItemStack item : player.getInventory().getStorageContents()) {
                if (item != null && item.getType() == requiredSlot.material()) {
                    found += item.getAmount();
                }
            }
            if (found < requiredSlot.quantity()) {
                return false;
            }
        }
        return true;
    }

    private boolean canFitReward(Player player, ItemStack reward) {
        int remaining = reward.getAmount();
        int maxStack = reward.getMaxStackSize();

        for (ItemStack item : player.getInventory().getStorageContents()) {
            if (item == null || item.getType().isAir()) {
                remaining -= maxStack;
            } else if (item.isSimilar(reward)) {
                remaining -= Math.max(0, maxStack - item.getAmount());
            }

            if (remaining <= 0) {
                return true;
            }
        }

        return remaining <= 0;
    }

    private ItemStack[] cloneStorage(ItemStack[] contents) {
        ItemStack[] clone = new ItemStack[contents.length];
        for (int i = 0; i < contents.length; i++) {
            clone[i] = contents[i] == null ? null : contents[i].clone();
        }
        return clone;
    }

    private boolean consumeRequiredItems(ItemStack[] contents, BillfordManager.BillfordTrade trade) {
        for (BillfordManager.BillfordSlot requiredSlot : trade.requiredItems()) {
            int remaining = requiredSlot.quantity();
            for (int i = 0; i < contents.length && remaining > 0; i++) {
                ItemStack item = contents[i];
                if (item == null || item.getType() != requiredSlot.material()) {
                    continue;
                }

                int taken = Math.min(item.getAmount(), remaining);
                item.setAmount(item.getAmount() - taken);
                remaining -= taken;
                if (item.getAmount() <= 0) {
                    contents[i] = null;
                }
            }

            if (remaining > 0) {
                return false;
            }
        }
        return true;
    }

    private void playSuccessParticle(Player player) {
        FileConfiguration config = plugin.getConfigManager().getBillford();
        boolean enabled = config.getBoolean("FEEDBACK.SUCCESS_PARTICLE.ENABLED", true);
        if (!enabled || player.getWorld() == null) {
            return;
        }

        String particleName = config.getString("FEEDBACK.SUCCESS_PARTICLE.TYPE", "TOTEM_OF_UNDYING");
        int count = Math.max(1, config.getInt("FEEDBACK.SUCCESS_PARTICLE.COUNT", 24));

        try {
            Particle particle = Particle.valueOf(particleName.toUpperCase(Locale.ROOT));
            player.getWorld().spawnParticle(
                    particle,
                    player.getLocation().add(0, 1.0, 0),
                    count,
                    0.35,
                    0.45,
                    0.35,
                    0.02
            );
        } catch (IllegalArgumentException ignored) {
        }
    }

    private String getConfigString(String path, String fallback) {
        return plugin.getConfigManager().getBillford().getString(path, fallback);
    }

    private List<String> getConfigStringList(String path, List<String> fallback) {
        List<String> configured = plugin.getConfigManager().getBillford().getStringList(path);
        return configured == null || configured.isEmpty() ? new ArrayList<>(fallback) : new ArrayList<>(configured);
    }

    private String getMenuString(String path, String fallback) {
        return plugin.getConfigManager().getMenus().getString(MENU_PATH + "." + path, fallback);
    }

    private List<String> getMenuStringList(String path, List<String> fallback) {
        List<String> configured = plugin.getConfigManager().getMenus().getStringList(MENU_PATH + "." + path);
        return configured == null || configured.isEmpty() ? new ArrayList<>(fallback) : new ArrayList<>(configured);
    }

    private List<String> replaceLore(List<String> template, Map<String, String> placeholders) {
        List<String> lore = new ArrayList<>();
        for (String line : template) {
            String replaced = replacePlaceholders(line, placeholders);
            if (replaced.isBlank() && line.contains("{") && line.contains("}")) {
                continue;
            }
            lore.add(replaced);
        }
        return lore;
    }

    private String replacePlaceholders(String input, Map<String, String> placeholders) {
        String result = input;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        return result;
    }

    private String formatName(String materialName) {
        String lower = materialName.replace("_", " ").toLowerCase(Locale.ROOT);
        if (lower.isEmpty()) {
            return lower;
        }
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}
