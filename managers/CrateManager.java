package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.block.Block;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

public class CrateManager {

    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection();

    private final UltimateDonutSmp plugin;
    private final Map<String, CrateDefinition> crates = new LinkedHashMap<>();
    private final Map<UUID, CrateOpenSession> activeSessions = new HashMap<>();
    private final Map<CrateBlockKey, String> boundBlocks = new HashMap<>();
    private final Map<UUID, String> pendingBindCrates = new HashMap<>();

    private ListMenuSettings listMenuSettings = ListMenuSettings.defaults();
    private ConfirmMenuSettings confirmMenuSettings = ConfirmMenuSettings.defaults();
    private GachaSettings gachaDefaults = GachaSettings.defaults();

    public CrateManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        crates.clear();
        activeSessions.clear();
        boundBlocks.clear();
        pendingBindCrates.clear();
        listMenuSettings = loadListMenuSettings();
        confirmMenuSettings = loadConfirmMenuSettings();
        gachaDefaults = loadGachaDefaults();
        loadCrates();
        loadBoundBlocks();
    }

    public Collection<CrateDefinition> getCrates() {
        return Collections.unmodifiableCollection(crates.values());
    }

    public List<CrateDefinition> getAccessibleCrates(Player player) {
        List<CrateDefinition> accessible = new ArrayList<>();
        for (CrateDefinition crate : crates.values()) {
            if (hasAccess(player, crate)) {
                accessible.add(crate);
            }
        }
        return accessible;
    }

    public CrateDefinition getCrate(String crateId) {
        return crates.get(normalizeCrateId(crateId));
    }

    public ActionResult createCrate(String crateId) {
        String normalized = normalizeCrateId(crateId);
        if (normalized == null || !normalized.matches("[a-z0-9_-]+")) {
            return new ActionResult(false, "&cCrate ID may only contain lowercase letters, numbers, '-' and '_'.");
        }
        if (crates.containsKey(normalized)) {
            return new ActionResult(false, "&cA crate with ID '&f" + normalized + "&c' already exists.");
        }

        FileConfiguration cratesConfig = plugin.getConfigManager().getCrates();
        String path = "CRATES." + normalized;
        cratesConfig.set(path + ".ENABLED", true);
        cratesConfig.set(path + ".DISPLAY.MATERIAL", Material.CHEST.name());
        cratesConfig.set(path + ".DISPLAY.DISPLAY-NAME", "&f" + prettifyId(normalized) + " Crate");
        cratesConfig.set(path + ".DISPLAY.LORE", List.of(
                "&7Keys: &f{keys}",
                "&aClick to open and choose 1 reward."
        ));
        cratesConfig.set(path + ".KEY-ITEM.MATERIAL", Material.TRIPWIRE_HOOK.name());
        cratesConfig.set(path + ".KEY-ITEM.DISPLAY-NAME", "&f" + prettifyId(normalized) + " Key");
        cratesConfig.set(path + ".KEY-ITEM.LORE", List.of(
                "&7Opens the &f" + prettifyId(normalized) + " Crate&7."
        ));
        cratesConfig.set(path + ".OPEN-TYPE", OpenType.CHOOSE_ONE.name());
        cratesConfig.set(path + ".PERMISSION", "");
        cratesConfig.set(path + ".BROADCAST-ON-CLAIM", false);
        cratesConfig.set(path + ".MENU.OPEN-TITLE", "&8" + prettifyId(normalized) + " Crate");
        cratesConfig.set(path + ".MENU.CONFIRM-TITLE", "&8Confirm Reward");
        cratesConfig.set(path + ".MENU.SIZE", 27);
        cratesConfig.set(path + ".MENU.FILLER", Material.BLACK_STAINED_GLASS_PANE.name());
        cratesConfig.set(path + ".MENU.BACK-SLOT", 26);
        cratesConfig.set(path + ".MENU.BACK-BUTTON.MATERIAL", Material.BARRIER.name());
        cratesConfig.set(path + ".MENU.BACK-BUTTON.DISPLAY-NAME", "&cBack");
        cratesConfig.set(path + ".MENU.BACK-BUTTON.LORE", List.of("&7Return to the crate list."));
        cratesConfig.createSection(path + ".REWARDS");

        if (!plugin.getConfigManager().saveCrates()) {
            return new ActionResult(false, "&cFailed to save crates.yml while creating that crate.");
        }

        reload();
        return new ActionResult(true, "&aCreated crate &f" + normalized + "&a with a default key config.");
    }

    public ActionResult deleteCrate(String crateId) {
        CrateDefinition crate = getCrate(crateId);
        if (crate == null) {
            return new ActionResult(false, "&cCrate '&f" + crateId + "&c' was not found.");
        }

        FileConfiguration cratesConfig = plugin.getConfigManager().getCrates();
        cratesConfig.set("CRATES." + crate.id(), null);
        if (!plugin.getConfigManager().saveCrates()) {
            return new ActionResult(false, "&cFailed to save crates.yml while deleting that crate.");
        }

        plugin.getDatabaseManager().deleteCrateKeyBalances(crate.id());
        plugin.getDatabaseManager().deleteCrateBlocksByCrateId(crate.id());
        reload();
        return new ActionResult(true, "&aDeleted crate &f" + crate.id() + "&a, its key balances, and bound crate chests.");
    }

    public ActionResult setOpenType(String crateId, OpenType openType) {
        CrateDefinition crate = getCrate(crateId);
        if (crate == null) {
            return new ActionResult(false, "&cCrate '&f" + crateId + "&c' was not found.");
        }
        if (openType == null) {
            return new ActionResult(false, "&cOpen type is invalid.");
        }

        FileConfiguration cratesConfig = plugin.getConfigManager().getCrates();
        String path = "CRATES." + crate.id();
        cratesConfig.set(path + ".OPEN-TYPE", openType.name());

        if (!plugin.getConfigManager().saveCrates()) {
            return new ActionResult(false, "&cFailed to save crates.yml while updating crate type.");
        }

        reload();
        return new ActionResult(true, "&aSet crate &f" + crate.id() + "&a to &f" + openType.name() + "&a.");
    }

    public ActionResult addItemReward(String crateId, int slot, ItemStack item) {
        CrateDefinition crate = getCrate(crateId);
        if (crate == null) {
            return new ActionResult(false, "&cCrate '&f" + crateId + "&c' was not found.");
        }
        if (!isValidRewardSlot(crate, slot)) {
            return new ActionResult(false, "&cSlot &f" + slot + "&c is not valid for this crate menu.");
        }
        if (crate.findRewardBySlot(slot) != null) {
            return new ActionResult(false, "&cThat slot already has a reward. Use &f/crate edit " + crate.id() + " " + slot + "&c.");
        }

        return saveItemReward(crate, slot, item, false);
    }

    public ActionResult editItemReward(String crateId, int slot, ItemStack item) {
        CrateDefinition crate = getCrate(crateId);
        if (crate == null) {
            return new ActionResult(false, "&cCrate '&f" + crateId + "&c' was not found.");
        }
        if (!isValidRewardSlot(crate, slot)) {
            return new ActionResult(false, "&cSlot &f" + slot + "&c is not valid for this crate menu.");
        }
        if (crate.findRewardBySlot(slot) == null) {
            return new ActionResult(false, "&cThat slot does not have a reward yet. Use &f/crate add " + crate.id() + " " + slot + "&c.");
        }

        return saveItemReward(crate, slot, item, true);
    }

    public ActionResult upsertItemReward(String crateId, int slot, ItemStack item) {
        CrateDefinition crate = getCrate(crateId);
        if (crate == null) {
            return new ActionResult(false, "&cCrate '&f" + crateId + "&c' was not found.");
        }
        if (!isValidRewardSlot(crate, slot)) {
            return new ActionResult(false, "&cSlot &f" + slot + "&c is not valid for this crate menu.");
        }

        CrateReward existingReward = crate.findRewardBySlot(slot);
        if (existingReward != null && existingReward.grant().type() != GrantType.ITEM) {
            return new ActionResult(false, "&cThat slot contains a non-item reward and cannot be edited from the GUI.");
        }

        return saveItemReward(crate, slot, item, existingReward != null);
    }

    public ActionResult removeReward(String crateId, int slot) {
        CrateDefinition crate = getCrate(crateId);
        if (crate == null) {
            return new ActionResult(false, "&cCrate '&f" + crateId + "&c' was not found.");
        }
        if (!isValidRewardSlot(crate, slot)) {
            return new ActionResult(false, "&cSlot &f" + slot + "&c is not valid for this crate menu.");
        }

        ConfigurationSection rewardsSection = plugin.getConfigManager().getCrates()
                .getConfigurationSection("CRATES." + crate.id() + ".REWARDS");
        if (rewardsSection == null) {
            return new ActionResult(false, "&cThat crate does not have any rewards configured.");
        }

        String rewardKey = findRewardKeyBySlot(rewardsSection, slot);
        if (rewardKey == null) {
            return new ActionResult(false, "&cNo reward was found in slot &f" + slot + "&c.");
        }

        rewardsSection.set(rewardKey, null);
        if (!plugin.getConfigManager().saveCrates()) {
            return new ActionResult(false, "&cFailed to save crates.yml while removing that reward.");
        }

        reload();
        return new ActionResult(true, "&aRemoved reward in slot &f" + slot + "&a from crate &f" + crate.id() + "&a.");
    }

    public boolean isBindableBlock(Material material) {
        return material == Material.CHEST
                || material == Material.TRAPPED_CHEST
                || material == Material.BARREL
                || material == Material.ENDER_CHEST;
    }

    public void startPendingBind(UUID uuid, String crateId) {
        String normalized = normalizeCrateId(crateId);
        if (uuid == null || normalized == null || !crates.containsKey(normalized)) {
            return;
        }
        pendingBindCrates.put(uuid, normalized);
    }

    public String getPendingBindCrateId(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        return pendingBindCrates.get(uuid);
    }

    public void clearPendingBind(UUID uuid) {
        if (uuid != null) {
            pendingBindCrates.remove(uuid);
        }
    }

    public String getBoundCrateId(Block block) {
        if (block == null || block.getWorld() == null) {
            return null;
        }
        return boundBlocks.get(new CrateBlockKey(
                block.getWorld().getName(),
                block.getX(),
                block.getY(),
                block.getZ()
        ));
    }

    public CrateDefinition getBoundCrate(Block block) {
        return getCrate(getBoundCrateId(block));
    }

    public Map<CrateBlockKey, String> getBoundBlockIds() {
        return Collections.unmodifiableMap(boundBlocks);
    }

    public boolean bindCrateBlock(Block block, String crateId) {
        if (block == null || block.getWorld() == null || !isBindableBlock(block.getType())) {
            return false;
        }

        CrateDefinition crate = getCrate(crateId);
        if (crate == null) {
            return false;
        }

        boolean saved = plugin.getDatabaseManager().saveCrateBlock(
                block.getWorld().getName(),
                block.getX(),
                block.getY(),
                block.getZ(),
                crate.id()
        );
        if (saved) {
            boundBlocks.put(new CrateBlockKey(block.getWorld().getName(), block.getX(), block.getY(), block.getZ()), crate.id());
        }
        return saved;
    }

    public boolean unbindCrateBlock(Block block) {
        if (block == null || block.getWorld() == null) {
            return false;
        }

        boolean deleted = plugin.getDatabaseManager().deleteCrateBlock(
                block.getWorld().getName(),
                block.getX(),
                block.getY(),
                block.getZ()
        );
        boundBlocks.remove(new CrateBlockKey(block.getWorld().getName(), block.getX(), block.getY(), block.getZ()));
        return deleted;
    }

    public boolean hasAccess(Player player, CrateDefinition crate) {
        if (player == null || crate == null || !crate.enabled()) {
            return false;
        }
        return crate.permission() == null
                || crate.permission().isBlank()
                || player.hasPermission(crate.permission());
    }

    public int getKeyBalance(Player player, String crateId) {
        return player == null ? 0 : getKeyBalance(player.getUniqueId(), crateId);
    }

    public int getKeyBalance(UUID uuid, String crateId) {
        String normalized = normalizeCrateId(crateId);
        if (uuid == null || normalized == null) {
            return 0;
        }
        return plugin.getDatabaseManager().getCrateKeyAmount(uuid, normalized);
    }

    public int addKeys(UUID uuid, String crateId, int amount) {
        String normalized = normalizeCrateId(crateId);
        CrateDefinition crate = getCrate(normalized);
        if (uuid == null || crate == null || amount <= 0) {
            return getKeyBalance(uuid, normalized);
        }
        return plugin.getDatabaseManager().addCrateKeys(uuid, crate.id(), amount);
    }

    public int setKeys(UUID uuid, String crateId, int amount) {
        String normalized = normalizeCrateId(crateId);
        CrateDefinition crate = getCrate(normalized);
        if (uuid == null || crate == null) {
            return 0;
        }
        return plugin.getDatabaseManager().setCrateKeyAmount(uuid, crate.id(), amount);
    }

    public boolean takeKeys(UUID uuid, String crateId, int amount) {
        String normalized = normalizeCrateId(crateId);
        CrateDefinition crate = getCrate(normalized);
        if (uuid == null || crate == null) {
            return false;
        }
        return plugin.getDatabaseManager().removeCrateKeys(uuid, crate.id(), amount);
    }

    public Map<CrateDefinition, Integer> getKeySummary(UUID uuid) {
        Map<CrateDefinition, Integer> summary = new LinkedHashMap<>();
        for (CrateDefinition crate : crates.values()) {
            summary.put(crate, getKeyBalance(uuid, crate.id()));
        }
        return summary;
    }

    public OpenResult startOpening(Player player, String crateId) {
        CrateDefinition crate = getCrate(crateId);
        if (crate == null) {
            return new OpenResult(false, FailureReason.CRATE_NOT_FOUND,
                    "&cCrate '&f" + crateId + "&c' was not found.", null);
        }
        if (!crate.enabled()) {
            return new OpenResult(false, FailureReason.CRATE_DISABLED,
                    "&cThis crate is currently disabled.", crate);
        }
        if (!hasAccess(player, crate)) {
            return new OpenResult(false, FailureReason.NO_PERMISSION,
                    "&cYou do not have permission to open this crate.", crate);
        }
        if (crate.rewards().isEmpty()) {
            return new OpenResult(false, FailureReason.INVALID_CRATE,
                    "&cThis crate has no valid rewards configured.", crate);
        }
        if (getKeyBalance(player, crate.id()) <= 0) {
            return new OpenResult(false, FailureReason.NO_KEYS,
                    "&cYou do not have any " + getReadableCrateName(crate) + " keys.", crate);
        }

        activeSessions.put(player.getUniqueId(), new CrateOpenSession(crate, null));
        return new OpenResult(true, null, "", crate);
    }

    public boolean selectReward(Player player, String rewardId) {
        if (player == null) {
            return false;
        }

        CrateOpenSession session = activeSessions.get(player.getUniqueId());
        if (session == null) {
            return false;
        }

        CrateReward reward = session.crate().findReward(rewardId);
        if (reward == null) {
            return false;
        }

        activeSessions.put(player.getUniqueId(), session.withSelectedReward(reward));
        return true;
    }

    public CrateOpenSession getSession(UUID uuid) {
        return uuid == null ? null : activeSessions.get(uuid);
    }

    public void clearSession(UUID uuid) {
        if (uuid != null) {
            activeSessions.remove(uuid);
        }
    }

    public void clearAllSessions() {
        activeSessions.clear();
        pendingBindCrates.clear();
    }

    public ClaimResult claimSelectedReward(Player player) {
        if (player == null) {
            return new ClaimResult(false, FailureReason.NO_PLAYER_DATA, "&cPlayer is not available.", null, null, 0);
        }

        CrateOpenSession session = activeSessions.get(player.getUniqueId());
        if (session == null) {
            return new ClaimResult(false, FailureReason.NO_SESSION, "&cNo active crate session was found.", null, null, 0);
        }

        CrateDefinition crate = session.crate();
        CrateReward reward = session.selectedReward();
        if (crate == null) {
            clearSession(player.getUniqueId());
            return new ClaimResult(false, FailureReason.INVALID_CRATE, "&cThis crate session is no longer valid.", null, null, 0);
        }
        if (reward == null) {
            return new ClaimResult(false, FailureReason.NO_REWARD_SELECTED, "&cSelect a reward first.", crate, null,
                    getKeyBalance(player, crate.id()));
        }
        if (getKeyBalance(player, crate.id()) <= 0) {
            clearSession(player.getUniqueId());
            return new ClaimResult(false, FailureReason.NO_KEYS,
                    "&cYou no longer have a key for " + getReadableCrateName(crate) + ".", crate, reward, 0);
        }

        if (reward.grant().type() == GrantType.ITEM) {
            ItemStack rewardItem = createGrantItem(reward.grant());
            if (rewardItem == null || rewardItem.getType().isAir()) {
                return new ClaimResult(false, FailureReason.INVALID_REWARD,
                        "&cThat reward is no longer valid.", crate, reward, getKeyBalance(player, crate.id()));
            }
            if (reward.grant().requiresInventorySpace() && !canFitItem(player, rewardItem)) {
                return new ClaimResult(false, FailureReason.INVENTORY_FULL,
                        "&cYour inventory is full. Clear space before claiming this reward.", crate, reward,
                        getKeyBalance(player, crate.id()));
            }
        }

        if ((reward.grant().type() == GrantType.MONEY || reward.grant().type() == GrantType.SHARDS)
                && plugin.getPlayerDataManager().get(player) == null) {
            return new ClaimResult(false, FailureReason.NO_PLAYER_DATA,
                    "&cYour player data could not be loaded. Try again in a moment.", crate, reward,
                    getKeyBalance(player, crate.id()));
        }

        if (!plugin.getDatabaseManager().removeCrateKeys(player.getUniqueId(), crate.id(), 1)) {
            clearSession(player.getUniqueId());
            return new ClaimResult(false, FailureReason.NO_KEYS,
                    "&cYou no longer have a key for " + getReadableCrateName(crate) + ".", crate, reward, 0);
        }

        boolean granted = false;
        try {
            granted = grantReward(player, crate, reward);
        } catch (Exception exception) {
            plugin.getLogger().log(Level.WARNING, "Failed to grant crate reward " + reward.id()
                    + " from crate " + crate.id() + " to " + player.getName(), exception);
        }

        if (!granted) {
            plugin.getDatabaseManager().addCrateKeys(player.getUniqueId(), crate.id(), 1);
            return new ClaimResult(false, FailureReason.REWARD_GRANT_FAILED,
                    "&cFailed to grant that reward. Your key has been returned.", crate, reward,
                    getKeyBalance(player, crate.id()));
        }

        clearSession(player.getUniqueId());
        if (shouldBroadcastClaim(crate, reward)) {
            Bukkit.broadcast(ColorUtils.toComponent(buildClaimBroadcast(player, crate, reward)));
        }

        return new ClaimResult(true, null,
                "&7You claimed &f" + getReadableRewardName(reward)
                        + "&7 from &b" + getReadableCrateName(crate) + "&7.",
                crate,
                reward,
                getKeyBalance(player, crate.id()));
    }

    public ClaimResult claimReward(Player player, CrateReward reward) {
        if (reward == null) {
            return new ClaimResult(false, FailureReason.INVALID_REWARD, "&cThat reward is no longer valid.", null, null, 0);
        }
        if (!selectReward(player, reward.id())) {
            return new ClaimResult(false, FailureReason.INVALID_REWARD, "&cThat reward is no longer valid.", null, reward, 0);
        }
        return claimSelectedReward(player);
    }

    public CrateReward rollReward(CrateDefinition crate) {
        if (crate == null || crate.rewards().isEmpty()) {
            return null;
        }

        long totalWeight = 0L;
        for (CrateReward reward : crate.rewards()) {
            totalWeight += Math.max(1, reward.weight());
        }

        if (totalWeight <= 0L) {
            return crate.rewards().get(ThreadLocalRandom.current().nextInt(crate.rewards().size()));
        }

        long roll = ThreadLocalRandom.current().nextLong(totalWeight);
        long cursor = 0L;
        for (CrateReward reward : crate.rewards()) {
            cursor += Math.max(1, reward.weight());
            if (roll < cursor) {
                return reward;
            }
        }
        return crate.rewards().get(crate.rewards().size() - 1);
    }

    public List<Player> grantKeysToPlayers(Collection<? extends Player> players, String crateId, int amount) {
        CrateDefinition crate = getCrate(crateId);
        if (crate == null || !crate.enabled() || amount <= 0) {
            return List.of();
        }

        List<Player> granted = new ArrayList<>();
        for (Player player : players) {
            if (!hasAccess(player, crate)) {
                continue;
            }
            addKeys(player.getUniqueId(), crate.id(), amount);
            granted.add(player);
        }
        return granted;
    }

    public int grantKeysToOnlinePlayers(String crateId, int amount) {
        return grantKeysToPlayers(Bukkit.getOnlinePlayers(), crateId, amount).size();
    }

    public String getReadableCrateName(String crateId) {
        return getReadableCrateName(getCrate(crateId));
    }

    public String getReadableCrateName(CrateDefinition crate) {
        if (crate == null) {
            return "crate";
        }

        String stripped = ColorUtils.strip(crate.display().displayName());
        return stripped == null || stripped.isBlank() ? prettifyId(crate.id()) : stripped;
    }

    public String getReadableRewardName(CrateReward reward) {
        if (reward == null) {
            return "reward";
        }

        String stripped = ColorUtils.strip(reward.display().displayName());
        if (stripped == null || stripped.isBlank()) {
            stripped = ColorUtils.strip(reward.grant().item().displayName());
        }
        return stripped == null || stripped.isBlank() ? reward.id() : stripped;
    }

    public ItemStack createCrateListItem(Player player, CrateDefinition crate) {
        int keyCount = getKeyBalance(player, crate.id());
        DisplayItem display = crate.display();
        ItemStack item = createDisplayItem(display, Math.max(1, Math.min(display.amount(), display.material().getMaxStackSize())));
        if (keyCount > 1) {
            item.setAmount(Math.min(keyCount, item.getMaxStackSize()));
        }

        var meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(ColorUtils.toComponent(applyPlaceholders(display.displayName(), player, crate, null)));
            meta.lore(ColorUtils.toComponentList(applyPlaceholders(display.lore(), player, crate, null)));
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createRewardDisplayItem(Player player, CrateDefinition crate, CrateReward reward) {
        ItemStack item = createDisplayItem(reward.display(), reward.display().amount());
        var meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(ColorUtils.toComponent(applyPlaceholders(reward.display().displayName(), player, crate, reward)));
            meta.lore(ColorUtils.toComponentList(applyPlaceholders(reward.display().lore(), player, crate, reward)));
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createRewardPreviewItem(Player player, CrateDefinition crate, CrateReward reward) {
        return createRewardDisplayItem(player, crate, reward);
    }

    public ListMenuSettings getListMenuSettings() {
        return listMenuSettings;
    }

    public ConfirmMenuSettings getConfirmMenuSettings() {
        return confirmMenuSettings;
    }

    public GachaSettings getGachaDefaults() {
        return gachaDefaults;
    }

    public String applyPlaceholders(String text, Player player, CrateDefinition crate, CrateReward reward) {
        if (text == null) {
            return "";
        }

        int keys = player == null || crate == null ? 0 : getKeyBalance(player, crate.id());
        return text
                .replace("{crate}", crate == null ? "crate" : getReadableCrateName(crate))
                .replace("{crate_id}", crate == null ? "" : crate.id())
                .replace("{reward}", reward == null ? "reward" : getReadableRewardName(reward))
                .replace("{reward_id}", reward == null ? "" : reward.id())
                .replace("{keys}", String.valueOf(keys))
                .replace("{player}", player == null ? "" : player.getName());
    }

    public List<String> applyPlaceholders(List<String> lines, Player player, CrateDefinition crate, CrateReward reward) {
        List<String> replaced = new ArrayList<>();
        for (String line : lines) {
            replaced.add(applyPlaceholders(line, player, crate, reward));
        }
        return replaced;
    }

    private boolean grantReward(Player player, CrateDefinition crate, CrateReward reward) {
        GrantDefinition grant = reward.grant();
        return switch (grant.type()) {
            case ITEM -> grantItemReward(player, grant);
            case MONEY -> grantMoneyReward(player, grant);
            case SHARDS -> grantShardReward(player, grant);
            case COMMAND -> grantCommandReward(player, crate, reward, grant);
        };
    }

    private boolean grantItemReward(Player player, GrantDefinition grant) {
        ItemStack rewardItem = createGrantItem(grant);
        if (rewardItem == null || rewardItem.getType().isAir()) {
            return false;
        }

        if (grant.requiresInventorySpace() && !canFitItem(player, rewardItem)) {
            return false;
        }

        ItemStack[] snapshot = cloneContents(player.getInventory().getStorageContents());
        Map<Integer, ItemStack> leftovers = player.getInventory().addItem(rewardItem);
        if (!leftovers.isEmpty()) {
            player.getInventory().setStorageContents(snapshot);
            player.updateInventory();
            return false;
        }

        plugin.getPaperScheduler().runEntity(player, () -> {
            if (!player.isOnline()) {
                return;
            }

            plugin.getWorthManager().syncWorthDisplay(player);
            player.updateInventory();
        });
        return true;
    }

    private boolean grantMoneyReward(Player player, GrantDefinition grant) {
        PlayerData data = plugin.getPlayerDataManager().get(player);
        if (data == null) {
            return false;
        }
        data.addMoney(grant.moneyAmount());
        return true;
    }

    private boolean grantShardReward(Player player, GrantDefinition grant) {
        PlayerData data = plugin.getPlayerDataManager().get(player);
        if (data == null) {
            return false;
        }
        data.addShards(grant.shardAmount());
        return true;
    }

    private boolean grantCommandReward(Player player, CrateDefinition crate, CrateReward reward, GrantDefinition grant) {
        for (String command : grant.commands()) {
            if (command == null || command.isBlank()) {
                continue;
            }

            String resolved = command
                    .replace("{player}", player.getName())
                    .replace("{username}", player.getName())
                    .replace("{crate}", crate.id())
                    .replace("{crate_name}", getReadableCrateName(crate))
                    .replace("{reward}", reward.id())
                    .replace("{reward_name}", getReadableRewardName(reward))
                    .replace("{amount}", String.valueOf(Math.max(1, grant.item().amount())));
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), resolved);
        }
        return true;
    }

    private ItemStack createGrantItem(GrantDefinition grant) {
        DisplayItem item = grant.item();
        if (item == null || item.material() == null || item.material().isAir()) {
            return null;
        }

        ItemStack grantedItem = createDisplayItem(item, item.amount());
        stripPreviewLore(grantedItem);
        return grantedItem;
    }

    private ItemStack createDisplayItem(DisplayItem display, int amount) {
        ItemStack item = ItemUtils.createItem(
                display.material(),
                display.displayName(),
                display.lore()
        );
        item.setAmount(Math.max(1, Math.min(amount, item.getMaxStackSize())));
        ItemUtils.addEnchantments(item, display.enchantments());
        return item;
    }

    private boolean canFitItem(Player player, ItemStack item) {
        ItemStack[] storage = player.getInventory().getStorageContents();
        int remaining = item.getAmount();
        ItemStack probe = item.clone();
        probe.setAmount(1);

        for (ItemStack current : storage) {
            if (current == null || current.getType().isAir()) {
                remaining -= item.getMaxStackSize();
            } else if (current.isSimilar(probe) && current.getAmount() < current.getMaxStackSize()) {
                remaining -= Math.max(0, current.getMaxStackSize() - current.getAmount());
            }

            if (remaining <= 0) {
                return true;
            }
        }
        return remaining <= 0;
    }

    private ItemStack[] cloneContents(ItemStack[] contents) {
        ItemStack[] clone = new ItemStack[contents.length];
        for (int i = 0; i < contents.length; i++) {
            clone[i] = contents[i] == null ? null : contents[i].clone();
        }
        return clone;
    }

    private String buildClaimBroadcast(Player player, CrateDefinition crate, CrateReward reward) {
        return "&8[&bCrates&8] &f" + player.getName()
                + " &7claimed &f" + getReadableRewardName(reward)
                + " &7from &b" + getReadableCrateName(crate) + "&7.";
    }

    private boolean shouldBroadcastClaim(CrateDefinition crate, CrateReward reward) {
        if (reward != null && reward.broadcast() != null) {
            return reward.broadcast();
        }
        return crate != null && crate.broadcastOnClaim();
    }

    private ActionResult saveItemReward(CrateDefinition crate, int slot, ItemStack item, boolean editing) {
        if (item == null || item.getType().isAir()) {
            return new ActionResult(false, "&cHold the item you want to save in your main hand first.");
        }

        FileConfiguration cratesConfig = plugin.getConfigManager().getCrates();
        ConfigurationSection rewardsSection = cratesConfig.getConfigurationSection("CRATES." + crate.id() + ".REWARDS");
        if (rewardsSection == null) {
            rewardsSection = cratesConfig.createSection("CRATES." + crate.id() + ".REWARDS");
        }

        String rewardKey = findRewardKeyBySlot(rewardsSection, slot);
        if (rewardKey == null) {
            rewardKey = "reward_" + slot;
        }

        String basePath = "CRATES." + crate.id() + ".REWARDS." + rewardKey;
        writeItemReward(cratesConfig, basePath, slot, item);

        if (!plugin.getConfigManager().saveCrates()) {
            return new ActionResult(false, "&cFailed to save crates.yml while updating that reward.");
        }

        reload();
        return new ActionResult(true, "&a" + (editing ? "Updated" : "Added")
                + " reward in slot &f" + slot + "&a for crate &f" + crate.id() + "&a.");
    }

    private void writeItemReward(FileConfiguration cratesConfig, String basePath, int slot, ItemStack item) {
        ItemStack clonedItem = item.clone();
        ItemMeta meta = clonedItem.getItemMeta();

        cratesConfig.set(basePath + ".SLOT", slot);
        cratesConfig.set(basePath + ".DISPLAY.MATERIAL", clonedItem.getType().name());
        cratesConfig.set(basePath + ".DISPLAY.DISPLAY-NAME", serializeDisplayName(meta, clonedItem.getType()));
        cratesConfig.set(basePath + ".DISPLAY.LORE", serializeDisplayLore(meta));
        cratesConfig.set(basePath + ".DISPLAY.AMOUNT", Math.max(1, clonedItem.getAmount()));
        cratesConfig.set(basePath + ".DISPLAY.ENCHANTMENTS", serializeEnchantments(clonedItem));

        cratesConfig.set(basePath + ".GRANT.TYPE", "ITEM");
        cratesConfig.set(basePath + ".GRANT.MATERIAL", clonedItem.getType().name());
        cratesConfig.set(basePath + ".GRANT.DISPLAY-NAME", serializeDisplayName(meta, clonedItem.getType()));
        cratesConfig.set(basePath + ".GRANT.LORE", serializeGrantLore(meta));
        cratesConfig.set(basePath + ".GRANT.AMOUNT", Math.max(1, clonedItem.getAmount()));
        cratesConfig.set(basePath + ".GRANT.ENCHANTMENTS", serializeEnchantments(clonedItem));
        cratesConfig.set(basePath + ".GRANT.REQUIRES-INVENTORY-SPACE", true);
    }

    private boolean isValidRewardSlot(CrateDefinition crate, int slot) {
        return slot >= 0
                && slot < crate.menuSettings().size()
                && slot != crate.menuSettings().backSlot();
    }

    private String findRewardKeyBySlot(ConfigurationSection rewardsSection, int slot) {
        for (String key : rewardsSection.getKeys(false)) {
            if (rewardsSection.getInt(key + ".SLOT", -1) == slot) {
                return key;
            }
        }
        return null;
    }

    private String serializeDisplayName(ItemMeta meta, Material material) {
        if (meta == null || meta.displayName() == null) {
            return "&f" + prettifyId(material.name());
        }
        return serializeComponent(meta.displayName());
    }

    private List<String> serializeDisplayLore(ItemMeta meta) {
        if (meta == null || meta.lore() == null || meta.lore().isEmpty()) {
            return List.of("&7Choose this reward.");
        }

        List<String> lore = serializeActualLore(meta);
        return lore.isEmpty() ? List.of("&7Choose this reward.") : lore;
    }

    private List<String> serializeGrantLore(ItemMeta meta) {
        return serializeActualLore(meta);
    }

    private List<String> serializeActualLore(ItemMeta meta) {
        if (meta == null || meta.lore() == null || meta.lore().isEmpty()) {
            return List.of();
        }

        List<String> lore = new ArrayList<>();
        for (Component component : meta.lore()) {
            String serialized = serializeComponent(component);
            if (isPreviewLoreLine(serialized)) {
                continue;
            }
            lore.add(serialized);
        }
        return lore;
    }

    private List<String> serializeEnchantments(ItemStack item) {
        if (item == null || item.getEnchantments().isEmpty()) {
            return List.of();
        }

        List<String> enchantments = new ArrayList<>();
        for (Map.Entry<org.bukkit.enchantments.Enchantment, Integer> entry : item.getEnchantments().entrySet()) {
            enchantments.add(entry.getKey().getKey().getKey() + ":" + entry.getValue());
        }
        return enchantments;
    }

    private String serializeComponent(Component component) {
        if (component == null) {
            return "";
        }
        return LEGACY_SERIALIZER.serialize(component).replace('\u00A7', '&');
    }

    private void stripPreviewLore(ItemStack item) {
        if (item == null) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null || meta.lore() == null || meta.lore().isEmpty()) {
            return;
        }

        List<Component> filteredLore = new ArrayList<>();
        for (Component component : meta.lore()) {
            if (component == null) {
                continue;
            }

            if (isPreviewLoreLine(serializeComponent(component))) {
                continue;
            }
            filteredLore.add(component);
        }

        meta.lore(filteredLore.isEmpty() ? null : filteredLore);
        item.setItemMeta(meta);
    }

    private boolean isPreviewLoreLine(String line) {
        String stripped = ColorUtils.strip(line).trim();
        if (stripped.regionMatches(true, 0, "Worth:", 0, "Worth:".length())) {
            return false;
        }
        return stripped.equalsIgnoreCase("Choose this reward.");
    }

    private void loadCrates() {
        FileConfiguration cratesConfig = plugin.getConfigManager().getCrates();
        ConfigurationSection cratesSection = cratesConfig.getConfigurationSection("CRATES");
        if (cratesSection == null) {
            plugin.getLogger().warning("crates.yml is missing the CRATES section.");
            return;
        }

        for (String rawId : cratesSection.getKeys(false)) {
            ConfigurationSection section = cratesSection.getConfigurationSection(rawId);
            if (section == null) {
                continue;
            }

            String crateId = normalizeCrateId(rawId);
            DisplayItem display = parseDisplayItem(
                    section.getConfigurationSection("DISPLAY"),
                    "crate " + crateId,
                    Material.CHEST
            );
            DisplayItem keyItem = parseDisplayItem(
                    section.getConfigurationSection("KEY-ITEM"),
                    "key item for crate " + crateId,
                    Material.TRIPWIRE_HOOK
            );
            OpenType openType = parseOpenType(section.getString("OPEN-TYPE", OpenType.CHOOSE_ONE.name()));
            CrateMenuSettings menuSettings = parseCrateMenuSettings(section.getConfigurationSection("MENU"));
            GachaSettings gachaSettings = parseGachaSettings(section.getConfigurationSection("GACHA"));
            List<CrateReward> rewards = parseRewards(crateId, section.getConfigurationSection("REWARDS"));

            CrateDefinition crate = new CrateDefinition(
                    crateId,
                    section.getBoolean("ENABLED", true),
                    display,
                    openType,
                    section.getString("PERMISSION", ""),
                    section.getBoolean("BROADCAST-ON-CLAIM", false),
                    menuSettings,
                    gachaSettings,
                    keyItem,
                    rewards
            );
            crates.put(crate.id(), crate);
        }
    }

    private void loadBoundBlocks() {
        for (DatabaseManager.CrateBlockData blockData : plugin.getDatabaseManager().loadCrateBlocks()) {
            String crateId = normalizeCrateId(blockData.crateId());
            if (crateId == null || !crates.containsKey(crateId)) {
                plugin.getLogger().warning("Skipping crate block binding at "
                        + blockData.world() + " " + blockData.x() + "," + blockData.y() + "," + blockData.z()
                        + " because crate '" + blockData.crateId() + "' does not exist.");
                continue;
            }

            boundBlocks.put(
                    new CrateBlockKey(blockData.world(), blockData.x(), blockData.y(), blockData.z()),
                    crateId
            );
        }
    }

    private List<CrateReward> parseRewards(String crateId, ConfigurationSection rewardsSection) {
        List<CrateReward> rewards = new ArrayList<>();
        if (rewardsSection == null) {
            plugin.getLogger().warning("Crate '" + crateId + "' is missing the REWARDS section.");
            return rewards;
        }

        Set<Integer> usedSlots = new HashSet<>();
        for (String rewardKey : rewardsSection.getKeys(false)) {
            ConfigurationSection rewardSection = rewardsSection.getConfigurationSection(rewardKey);
            if (rewardSection == null) {
                continue;
            }

            int slot = rewardSection.getInt("SLOT", -1);
            if (slot < 0) {
                plugin.getLogger().warning("Skipping reward '" + rewardKey + "' in crate '" + crateId
                        + "' because it is missing a valid SLOT.");
                continue;
            }
            if (!usedSlots.add(slot)) {
                plugin.getLogger().warning("Duplicate reward slot " + slot + " detected in crate '" + crateId + "'.");
            }

            ConfigurationSection displaySection = rewardSection.getConfigurationSection("DISPLAY");
            ConfigurationSection grantSection = rewardSection.getConfigurationSection("GRANT");
            if (grantSection == null) {
                plugin.getLogger().warning("Skipping reward '" + rewardKey + "' in crate '" + crateId
                        + "' because it is missing a GRANT section.");
                continue;
            }

            GrantDefinition grant = parseGrantDefinition(crateId, rewardKey, grantSection);
            if (grant == null) {
                continue;
            }

            DisplayItem display = parseDisplayItem(displaySection, "display reward " + rewardKey + " in crate " + crateId,
                    grant.item().material());

            rewards.add(new CrateReward(
                    rewardKey,
                    slot,
                    display,
                    grant,
                    rewardSection.contains("BROADCAST") ? rewardSection.getBoolean("BROADCAST") : null,
                    Math.max(1, rewardSection.getInt("WEIGHT", 1))
            ));
        }

        rewards.sort((left, right) -> Integer.compare(left.slot(), right.slot()));
        return rewards;
    }

    private GrantDefinition parseGrantDefinition(String crateId, String rewardId, ConfigurationSection section) {
        String typeName = section.getString("TYPE", "ITEM");
        GrantType type;
        try {
            type = GrantType.valueOf(typeName.toUpperCase(Locale.US).trim());
        } catch (IllegalArgumentException exception) {
            plugin.getLogger().warning("Skipping reward '" + rewardId + "' in crate '" + crateId
                    + "' because grant type '" + typeName + "' is invalid.");
            return null;
        }

        return switch (type) {
            case ITEM -> {
                DisplayItem item = parseDisplayItem(section, "grant reward " + rewardId + " in crate " + crateId, Material.STONE);
                yield new GrantDefinition(type, item, 0D, 0L, List.of(),
                        section.getBoolean("REQUIRES-INVENTORY-SPACE", true));
            }
            case COMMAND -> new GrantDefinition(
                    type,
                    new DisplayItem(Material.PAPER, "&fReward", List.of(), 1, List.of()),
                    0D,
                    0L,
                    readStringList(section, "COMMANDS"),
                    false
            );
            case MONEY -> new GrantDefinition(
                    type,
                    new DisplayItem(Material.SUNFLOWER, "&eMoney Reward", List.of(), 1, List.of()),
                    section.getDouble("AMOUNT", 0D),
                    0L,
                    List.of(),
                    false
            );
            case SHARDS -> new GrantDefinition(
                    type,
                    new DisplayItem(Material.AMETHYST_SHARD, "&#A303F9Shard Reward", List.of(), 1, List.of()),
                    0D,
                    Math.max(0L, section.getLong("AMOUNT", 0L)),
                    List.of(),
                    false
            );
        };
    }

    private DisplayItem parseDisplayItem(ConfigurationSection section, String context, Material fallbackMaterial) {
        if (section == null) {
            return new DisplayItem(
                    fallbackMaterial == null ? Material.STONE : fallbackMaterial,
                    "&f" + prettifyId(context),
                    List.of(),
                    1,
                    List.of()
            );
        }

        Material material = parseMaterial(
                section.getString("MATERIAL"),
                fallbackMaterial == null ? Material.STONE : fallbackMaterial,
                context
        );
        String name = section.getString("DISPLAY-NAME",
                section.getString("NAME", "&f" + prettifyId(context)));
        List<String> lore = readStringList(section, "LORE");
        int amount = Math.max(1, section.getInt("AMOUNT", 1));
        List<String> enchantments = readStringList(section, "ENCHANTMENTS");
        return new DisplayItem(material, name, lore, amount, enchantments);
    }

    private ListMenuSettings loadListMenuSettings() {
        ConfigurationSection section = plugin.getConfigManager().getCrates().getConfigurationSection("SETTINGS.LIST-MENU");
        if (section == null) {
            return ListMenuSettings.defaults();
        }

        List<Integer> contentSlots = section.getIntegerList("CONTENT-SLOTS");
        if (contentSlots.isEmpty()) {
            contentSlots = List.of(10, 11, 12, 13, 14, 15, 16);
        }

        return new ListMenuSettings(
                section.getString("TITLE", "&8Crates"),
                sanitizeSize(section.getInt("SIZE", 27), 27),
                parseMaterial(section.getString("FILLER"), Material.GRAY_STAINED_GLASS_PANE, "crate list filler"),
                contentSlots,
                section.getInt("EMPTY-SLOT", 13),
                parseDisplayItem(section.getConfigurationSection("EMPTY"), "empty crate list button", Material.BARRIER),
                section.getInt("CLOSE-BUTTON.SLOT", 26),
                parseDisplayItem(section.getConfigurationSection("CLOSE-BUTTON"), "crate list close button", Material.BARRIER)
        );
    }

    private ConfirmMenuSettings loadConfirmMenuSettings() {
        ConfigurationSection section = plugin.getConfigManager().getCrates().getConfigurationSection("SETTINGS.CONFIRM-MENU");
        if (section == null) {
            return ConfirmMenuSettings.defaults();
        }

        return new ConfirmMenuSettings(
                sanitizeSize(section.getInt("SIZE", 27), 27),
                parseMaterial(section.getString("FILLER"), Material.GRAY_STAINED_GLASS_PANE, "crate confirm filler"),
                section.getInt("PREVIEW-SLOT", 13),
                section.getInt("CONFIRM-SLOT", 15),
                parseDisplayItem(section.getConfigurationSection("CONFIRM-BUTTON"), "crate confirm button", Material.LIME_STAINED_GLASS_PANE),
                section.getInt("CANCEL-SLOT", 11),
                parseDisplayItem(section.getConfigurationSection("CANCEL-BUTTON"), "crate cancel button", Material.RED_STAINED_GLASS_PANE)
        );
    }

    private GachaSettings loadGachaDefaults() {
        ConfigurationSection section = plugin.getConfigManager().getCrates().getConfigurationSection("SETTINGS.GACHA");
        if (section == null) {
            return GachaSettings.defaults();
        }

        List<Integer> previewSlots = section.getIntegerList("PREVIEW-SLOTS");
        if (previewSlots.isEmpty()) {
            previewSlots = List.of(10, 11, 12, 13, 14, 15, 16);
        }

        return new GachaSettings(
                section.getString("TITLE", "&8Rolling Reward"),
                parseMaterial(section.getString("FILLER"), Material.BLACK_STAINED_GLASS_PANE, "crate gacha filler"),
                previewSlots,
                section.getInt("POINTER-SLOT", 13),
                Math.max(12, section.getInt("TOTAL-STEPS", 38)),
                Math.max(1, section.getInt("TICK-INTERVAL", 2)),
                parseSpinDirection(section.getString("SPIN-DIRECTION", SpinDirection.RANDOM.name()))
        );
    }

    private CrateMenuSettings parseCrateMenuSettings(ConfigurationSection section) {
        if (section == null) {
            return CrateMenuSettings.defaults();
        }

        int size = sanitizeSize(section.getInt("SIZE", 27), 27);
        return new CrateMenuSettings(
                section.getString("OPEN-TITLE", "&8Choose 1 Reward"),
                section.getString("CONFIRM-TITLE", "&8Confirm Reward"),
                size,
                parseMaterial(section.getString("FILLER"), Material.BLACK_STAINED_GLASS_PANE, "crate menu filler"),
                section.getInt("BACK-SLOT", size - 1),
                parseDisplayItem(section.getConfigurationSection("BACK-BUTTON"), "crate back button", Material.BARRIER)
        );
    }

    private GachaSettings parseGachaSettings(ConfigurationSection section) {
        if (section == null) {
            return gachaDefaults;
        }

        List<Integer> previewSlots = section.getIntegerList("PREVIEW-SLOTS");
        if (previewSlots.isEmpty()) {
            previewSlots = gachaDefaults.previewSlots();
        }

        return new GachaSettings(
                section.getString("TITLE", gachaDefaults.title()),
                parseMaterial(section.getString("FILLER"), gachaDefaults.filler(), "crate gacha filler"),
                previewSlots,
                section.getInt("POINTER-SLOT", gachaDefaults.pointerSlot()),
                Math.max(12, section.getInt("TOTAL-STEPS", gachaDefaults.totalSteps())),
                Math.max(1, section.getInt("TICK-INTERVAL", gachaDefaults.tickInterval())),
                parseSpinDirection(section.getString("SPIN-DIRECTION", gachaDefaults.direction().name()))
        );
    }

    private List<String> readStringList(ConfigurationSection section, String path) {
        if (section == null) {
            return List.of();
        }
        if (section.isList(path)) {
            return section.getStringList(path);
        }

        String singleLine = section.getString(path);
        if (singleLine == null || singleLine.isBlank()) {
            return List.of();
        }
        return List.of(singleLine);
    }

    private Material parseMaterial(String rawName, Material fallback, String context) {
        if (rawName == null || rawName.isBlank()) {
            return fallback;
        }

        try {
            return Material.valueOf(rawName.trim().toUpperCase(Locale.US));
        } catch (IllegalArgumentException exception) {
            plugin.getLogger().warning("Invalid material '" + rawName + "' configured for " + context
                    + ". Falling back to " + fallback + ".");
            return fallback;
        }
    }

    private OpenType parseOpenType(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return OpenType.CHOOSE_ONE;
        }

        try {
            return OpenType.valueOf(rawValue.trim().toUpperCase(Locale.US));
        } catch (IllegalArgumentException exception) {
            return OpenType.CHOOSE_ONE;
        }
    }

    private SpinDirection parseSpinDirection(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return SpinDirection.RANDOM;
        }

        try {
            return SpinDirection.valueOf(rawValue.trim().toUpperCase(Locale.US));
        } catch (IllegalArgumentException exception) {
            return SpinDirection.RANDOM;
        }
    }

    private int sanitizeSize(int size, int fallback) {
        if (size < 9 || size > 54 || size % 9 != 0) {
            return fallback;
        }
        return size;
    }

    private String normalizeCrateId(String crateId) {
        if (crateId == null || crateId.isBlank()) {
            return null;
        }
        return crateId.trim().toLowerCase(Locale.US);
    }

    private String prettifyId(String raw) {
        if (raw == null || raw.isBlank()) {
            return "Crate";
        }

        String normalized = raw.replace('-', ' ').replace('_', ' ');
        String[] parts = normalized.split("\\s+");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                builder.append(part.substring(1).toLowerCase(Locale.US));
            }
        }
        return builder.isEmpty() ? "Crate" : builder.toString();
    }

    public enum GrantType {
        ITEM,
        COMMAND,
        MONEY,
        SHARDS
    }

    public enum FailureReason {
        CRATE_NOT_FOUND,
        CRATE_DISABLED,
        NO_PERMISSION,
        NO_KEYS,
        INVALID_CRATE,
        INVALID_REWARD,
        NO_SESSION,
        NO_REWARD_SELECTED,
        INVENTORY_FULL,
        NO_PLAYER_DATA,
        REWARD_GRANT_FAILED
    }

    public record DisplayItem(
            Material material,
            String displayName,
            List<String> lore,
            int amount,
            List<String> enchantments
    ) {
    }

    public record GrantDefinition(
            GrantType type,
            DisplayItem item,
            double moneyAmount,
            long shardAmount,
            List<String> commands,
            boolean requiresInventorySpace
    ) {
    }

    public record CrateReward(
            String id,
            int slot,
            DisplayItem display,
            GrantDefinition grant,
            Boolean broadcast,
            int weight
    ) {
    }

    public record CrateDefinition(
            String id,
            boolean enabled,
            DisplayItem display,
            OpenType openType,
            String permission,
            boolean broadcastOnClaim,
            CrateMenuSettings menuSettings,
            GachaSettings gachaSettings,
            DisplayItem keyItem,
            List<CrateReward> rewards
    ) {
        public CrateReward findReward(String rewardId) {
            if (rewardId == null || rewardId.isBlank()) {
                return null;
            }
            for (CrateReward reward : rewards) {
                if (reward.id().equalsIgnoreCase(rewardId)) {
                    return reward;
                }
            }
            return null;
        }

        public CrateReward findRewardBySlot(int slot) {
            for (CrateReward reward : rewards) {
                if (reward.slot() == slot) {
                    return reward;
                }
            }
            return null;
        }
    }

    public record CrateOpenSession(CrateDefinition crate, CrateReward selectedReward) {
        public CrateOpenSession withSelectedReward(CrateReward reward) {
            return new CrateOpenSession(crate, reward);
        }
    }

    public record CrateBlockKey(String world, int x, int y, int z) {
    }

    public record ActionResult(boolean success, String message) {
    }

    public record OpenResult(boolean success, FailureReason reason, String message, CrateDefinition crate) {
    }

    public record ClaimResult(
            boolean success,
            FailureReason reason,
            String message,
            CrateDefinition crate,
            CrateReward reward,
            int remainingKeys
    ) {
    }

    public record ListMenuSettings(
            String title,
            int size,
            Material filler,
            List<Integer> contentSlots,
            int emptySlot,
            DisplayItem emptyItem,
            int closeSlot,
            DisplayItem closeItem
    ) {
        public static ListMenuSettings defaults() {
            return new ListMenuSettings(
                    "&8Crates",
                    27,
                    Material.GRAY_STAINED_GLASS_PANE,
                    List.of(10, 11, 12, 13, 14, 15, 16),
                    13,
                    new DisplayItem(Material.BARRIER, "&cNo Crates", List.of("&7No crates are available right now."), 1, List.of()),
                    26,
                    new DisplayItem(Material.BARRIER, "&cClose", List.of("&7Close this menu."), 1, List.of())
            );
        }
    }

    public record ConfirmMenuSettings(
            int size,
            Material filler,
            int previewSlot,
            int confirmSlot,
            DisplayItem confirmButton,
            int cancelSlot,
            DisplayItem cancelButton
    ) {
        public static ConfirmMenuSettings defaults() {
            return new ConfirmMenuSettings(
                    27,
                    Material.GRAY_STAINED_GLASS_PANE,
                    13,
                    15,
                    new DisplayItem(Material.LIME_STAINED_GLASS_PANE, "&aConfirm", List.of("&7Click to claim {reward}."), 1, List.of()),
                    11,
                    new DisplayItem(Material.RED_STAINED_GLASS_PANE, "&cCancel", List.of("&7Return to the reward list."), 1, List.of())
            );
        }
    }

    public record CrateMenuSettings(
            String openTitle,
            String confirmTitle,
            int size,
            Material filler,
            int backSlot,
            DisplayItem backButton
    ) {
        public static CrateMenuSettings defaults() {
            return new CrateMenuSettings(
                    "&8Choose 1 Reward",
                    "&8Confirm Reward",
                    27,
                    Material.BLACK_STAINED_GLASS_PANE,
                    26,
                    new DisplayItem(Material.BARRIER, "&cBack", List.of("&7Return to the crate list."), 1, List.of())
            );
        }
    }

    public record GachaSettings(
            String title,
            Material filler,
            List<Integer> previewSlots,
            int pointerSlot,
            int totalSteps,
            int tickInterval,
            SpinDirection direction
    ) {
        public static GachaSettings defaults() {
            return new GachaSettings(
                    "&8Rolling Reward",
                    Material.BLACK_STAINED_GLASS_PANE,
                    List.of(10, 11, 12, 13, 14, 15, 16),
                    13,
                    38,
                    2,
                    SpinDirection.RANDOM
            );
        }
    }

    public enum OpenType {
        CHOOSE_ONE,
        GACHA
    }

    public enum SpinDirection {
        LEFT,
        RIGHT,
        RANDOM
    }
}
