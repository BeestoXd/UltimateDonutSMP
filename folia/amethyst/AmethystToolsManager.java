package com.bx.ultimateDonutSmp.amethyst;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class AmethystToolsManager {

    public final NamespacedKey KEY_TYPE;
    public final NamespacedKey KEY_EXPIRY;
    public final NamespacedKey KEY_OWNER;
    public final NamespacedKey KEY_ID;

    private static final long DEFAULT_USE_COOLDOWN_MS = 250L;
    private static final long DEFAULT_VISUAL_SYNC_SUPPRESSION_MS = 1200L;

    private final UltimateDonutSmp plugin;
    private final Map<UUID, Long> useCooldowns = new java.util.HashMap<>();
    private final Map<UUID, Long> visualSyncSuppressions = new java.util.HashMap<>();

    public AmethystToolsManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
        KEY_TYPE = new NamespacedKey(plugin, "amethyst_tool_type");
        KEY_EXPIRY = new NamespacedKey(plugin, "amethyst_tool_expiry");
        KEY_OWNER = new NamespacedKey(plugin, "amethyst_tool_owner");
        KEY_ID = new NamespacedKey(plugin, "amethyst_tool_id");
    }

    public ItemStack createTool(AmethystToolType type, UUID ownerUuid, long durationSeconds) {
        ConfigurationSection cfg = getToolSection(type);
        if (cfg == null) {
            return null;
        }

        Material material = ItemUtils.parseMaterial(cfg.getString("MATERIAL", "IRON_PICKAXE"));
        long duration = durationSeconds > 0 ? durationSeconds : cfg.getLong("DURATION", 86400L);
        long expiryEpoch = (System.currentTimeMillis() / 1000L) + duration;

        List<String> resolvedLore = new ArrayList<>();
        for (String line : cfg.getStringList("LORE")) {
            resolvedLore.add(line.replace("{time}", NumberUtils.formatTimeLong(duration)));
        }

        ItemStack item = ItemUtils.createItem(material, cfg.getString("NAME", "&d&lAmethyst Tool"), resolvedLore);
        item.setAmount(1);

        List<String> enchants = cfg.getStringList("ENCHANTMENTS");
        if (!enchants.isEmpty()) {
            ItemUtils.addEnchantments(item, enchants);
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }

        if (type == AmethystToolType.SHARD_BOOSTER && meta instanceof PotionMeta potionMeta) {
            potionMeta.setBasePotionType(PotionType.WATER);
            meta = potionMeta;
        }

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);
        meta.setUnbreakable(true);

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(KEY_TYPE, PersistentDataType.STRING, type.name());
        pdc.set(KEY_EXPIRY, PersistentDataType.LONG, expiryEpoch);
        pdc.set(KEY_ID, PersistentDataType.STRING, UUID.randomUUID().toString());
        if (ownerUuid != null) {
            pdc.set(KEY_OWNER, PersistentDataType.STRING, ownerUuid.toString());
        }

        item.setItemMeta(meta);
        return item;
    }

    public boolean isAmethystTool(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(KEY_TYPE, PersistentDataType.STRING);
    }

    public boolean hasValidSignature(ItemStack item) {
        if (!isAmethystTool(item)) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (!pdc.has(KEY_EXPIRY, PersistentDataType.LONG)) {
            return false;
        }
        if (requiresItemId() && !pdc.has(KEY_ID, PersistentDataType.STRING)) {
            return false;
        }

        AmethystToolType type = getToolType(item);
        if (type == null) {
            return false;
        }

        ConfigurationSection cfg = getToolSection(type);
        if (cfg == null) {
            return false;
        }

        Material expected = ItemUtils.parseMaterial(cfg.getString("MATERIAL", item.getType().name()));
        return item.getType() == expected;
    }

    public AmethystToolType getToolType(ItemStack item) {
        if (!isAmethystTool(item)) {
            return null;
        }
        String raw = item.getItemMeta().getPersistentDataContainer().get(KEY_TYPE, PersistentDataType.STRING);
        return AmethystToolType.fromString(raw);
    }

    public String getItemId(ItemStack item) {
        if (!isAmethystTool(item)) {
            return null;
        }
        return item.getItemMeta().getPersistentDataContainer().get(KEY_ID, PersistentDataType.STRING);
    }

    public long getExpiryEpoch(ItemStack item) {
        if (!isAmethystTool(item)) {
            return 0L;
        }
        return item.getItemMeta().getPersistentDataContainer().getOrDefault(KEY_EXPIRY, PersistentDataType.LONG, 0L);
    }

    public long getRemainingSeconds(ItemStack item) {
        long expiry = getExpiryEpoch(item);
        if (expiry <= 0L) {
            return 0L;
        }
        return expiry - (System.currentTimeMillis() / 1000L);
    }

    public boolean isExpired(ItemStack item) {
        return getRemainingSeconds(item) <= 0L;
    }

    public boolean ensureIdentity(ItemStack item, UUID defaultOwner, boolean forceNewId) {
        if (!isAmethystTool(item)) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        AmethystToolType type = getToolType(item);
        if (type == null) {
            return false;
        }

        boolean changed = false;

        if (!pdc.has(KEY_EXPIRY, PersistentDataType.LONG)) {
            long defaultDuration = getToolSection(type) != null
                    ? getToolSection(type).getLong("DURATION", 86400L)
                    : 86400L;
            pdc.set(KEY_EXPIRY, PersistentDataType.LONG, (System.currentTimeMillis() / 1000L) + defaultDuration);
            changed = true;
        }

        if (forceNewId || !pdc.has(KEY_ID, PersistentDataType.STRING)) {
            pdc.set(KEY_ID, PersistentDataType.STRING, UUID.randomUUID().toString());
            changed = true;
        }

        if (defaultOwner != null && !pdc.has(KEY_OWNER, PersistentDataType.STRING)) {
            pdc.set(KEY_OWNER, PersistentDataType.STRING, defaultOwner.toString());
            changed = true;
        }

        if (changed) {
            item.setItemMeta(meta);
        }

        return changed;
    }

    public boolean isOwnedBy(Player player, ItemStack item) {
        if (!isAmethystTool(item) || !isOwnerBindingEnabled()) {
            return true;
        }

        String owner = item.getItemMeta().getPersistentDataContainer().get(KEY_OWNER, PersistentDataType.STRING);
        return owner == null || owner.equalsIgnoreCase(player.getUniqueId().toString());
    }

    public boolean isOnCooldown(UUID uuid) {
        Long last = useCooldowns.get(uuid);
        return last != null && System.currentTimeMillis() - last < getUseCooldownMs();
    }

    public void stampCooldown(UUID uuid) {
        useCooldowns.put(uuid, System.currentTimeMillis());
    }

    public void removeCooldown(UUID uuid) {
        useCooldowns.remove(uuid);
    }

    public boolean updateLoreCountdown(ItemStack item) {
        if (!isAmethystTool(item)) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        List<Component> lore = meta.lore();
        if (lore == null || lore.isEmpty()) {
            return false;
        }

        long remaining = getRemainingSeconds(item);
        String timeStr = remaining > 0 ? NumberUtils.formatTimeLong(remaining) : "&cEXPIRED";

        PlainTextComponentSerializer plain = PlainTextComponentSerializer.plainText();
        boolean foundSelfDestruct = false;
        List<Component> newLore = new ArrayList<>(lore);
        Component replacement = ColorUtils.toComponent("&#BDC3C7" + timeStr);
        String replacementPlain = plain.serialize(replacement);
        boolean changed = false;

        for (int i = 0; i < newLore.size(); i++) {
            String lineText = plain.serialize(newLore.get(i));
            if (lineText.contains("Self Destruct")) {
                foundSelfDestruct = true;
                continue;
            }

            if (foundSelfDestruct && !lineText.isBlank()) {
                if (lineText.equals(replacementPlain)) {
                    return false;
                }
                newLore.set(i, replacement);
                changed = true;
                break;
            }
        }

        if (!changed) {
            return false;
        }

        meta.lore(newLore);
        item.setItemMeta(meta);
        return true;
    }

    public void sanitizePlayerInventory(Player player, boolean notifyExpired) {
        PlayerInventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            sanitizeInventorySlot(player, slot, notifyExpired);
        }
    }

    public boolean sanitizeInventorySlot(Player player, int slot, boolean notifyExpired) {
        PlayerInventory inventory = player.getInventory();
        ItemStack item = inventory.getItem(slot);
        if (!isAmethystTool(item)) {
            return false;
        }

        boolean changed = ensureIdentity(item, player.getUniqueId(), false);

        if (item.getAmount() > 1) {
            splitStack(player, slot, item);
            changed = true;
            item = inventory.getItem(slot);
            if (item == null) {
                return true;
            }
        }

        if (!hasValidSignature(item)) {
            inventory.setItem(slot, null);
            return true;
        }

        if (isExpired(item)) {
            expireItem(player, slot, item, notifyExpired);
            return true;
        }

        return changed;
    }

    public boolean sanitizeHeldItem(Player player, boolean notifyExpired) {
        return sanitizeInventorySlot(player, player.getInventory().getHeldItemSlot(), notifyExpired);
    }

    public boolean sanitizeExternalInventorySlot(Player player, Inventory inventory, int slot, boolean notifyExpired) {
        if (inventory == null) {
            return false;
        }

        ItemStack item = inventory.getItem(slot);
        if (!isAmethystTool(item)) {
            return false;
        }

        boolean changed = ensureIdentity(item, null, false);

        if (item.getAmount() > 1) {
            splitExternalStack(player, inventory, slot, item);
            changed = true;
            item = inventory.getItem(slot);
            if (item == null) {
                return true;
            }
        }

        if (!hasValidSignature(item)) {
            inventory.setItem(slot, null);
            return true;
        }

        if (isExpired(item)) {
            AmethystToolType type = getToolType(item);
            inventory.setItem(slot, null);
            sendExpireFeedback(player, type, notifyExpired);
            return true;
        }

        return changed;
    }

    private void splitStack(Player player, int slot, ItemStack stack) {
        if (!isAmethystTool(stack) || stack.getAmount() <= 1) {
            return;
        }

        PlayerInventory inventory = player.getInventory();
        int amount = stack.getAmount();

        ItemStack base = stack.clone();
        base.setAmount(1);
        ensureIdentity(base, player.getUniqueId(), true);
        inventory.setItem(slot, base);

        for (int i = 1; i < amount; i++) {
            ItemStack extra = stack.clone();
            extra.setAmount(1);
            ensureIdentity(extra, player.getUniqueId(), true);
            Map<Integer, ItemStack> leftovers = inventory.addItem(extra);
            leftovers.values().forEach(leftover ->
                    player.getWorld().dropItemNaturally(player.getLocation(), leftover));
        }
    }

    public void expireHeldItem(Player player) {
        int slot = player.getInventory().getHeldItemSlot();
        ItemStack item = player.getInventory().getItem(slot);
        if (isAmethystTool(item)) {
            expireItem(player, slot, item, true);
        }
    }

    public boolean sanitizeCursorItem(Player player, boolean notifyExpired) {
        ItemStack cursor = player.getItemOnCursor();
        if (!isAmethystTool(cursor)) {
            return false;
        }

        boolean changed = ensureIdentity(cursor, player.getUniqueId(), false);

        if (cursor.getAmount() > 1) {
            splitCursorStack(player, cursor);
            changed = true;
            cursor = player.getItemOnCursor();
            if (cursor == null) {
                return true;
            }
        }

        if (!hasValidSignature(cursor)) {
            player.setItemOnCursor(null);
            return true;
        }

        if (isExpired(cursor)) {
            expireCursorItem(player, cursor, notifyExpired);
            return true;
        }

        return changed;
    }

    public void expireItem(Player player, int slot, ItemStack item, boolean sendFeedback) {
        if (!isAmethystTool(item)) {
            return;
        }

        AmethystToolType type = getToolType(item);
        player.getInventory().setItem(slot, null);

        sendExpireFeedback(player, type, sendFeedback);
    }

    private void splitCursorStack(Player player, ItemStack stack) {
        if (!isAmethystTool(stack) || stack.getAmount() <= 1) {
            return;
        }

        int amount = stack.getAmount();

        ItemStack base = stack.clone();
        base.setAmount(1);
        ensureIdentity(base, player.getUniqueId(), true);
        player.setItemOnCursor(base);

        for (int i = 1; i < amount; i++) {
            ItemStack extra = stack.clone();
            extra.setAmount(1);
            ensureIdentity(extra, player.getUniqueId(), true);
            Map<Integer, ItemStack> leftovers = player.getInventory().addItem(extra);
            leftovers.values().forEach(leftover ->
                    player.getWorld().dropItemNaturally(player.getLocation(), leftover));
        }
    }

    private void splitExternalStack(Player player, Inventory inventory, int slot, ItemStack stack) {
        if (!isAmethystTool(stack) || stack.getAmount() <= 1) {
            return;
        }

        int amount = stack.getAmount();

        ItemStack base = stack.clone();
        base.setAmount(1);
        ensureIdentity(base, null, true);
        inventory.setItem(slot, base);

        for (int i = 1; i < amount; i++) {
            ItemStack extra = stack.clone();
            extra.setAmount(1);
            ensureIdentity(extra, null, true);
            player.getWorld().dropItemNaturally(player.getLocation(), extra);
        }
    }

    private void expireCursorItem(Player player, ItemStack item, boolean sendFeedback) {
        if (!isAmethystTool(item)) {
            return;
        }

        AmethystToolType type = getToolType(item);
        player.setItemOnCursor(null);
        sendExpireFeedback(player, type, sendFeedback);
    }

    private void sendExpireFeedback(Player player, AmethystToolType type, boolean sendFeedback) {
        if (!sendFeedback) {
            return;
        }

        String toolName = type != null ? type.getDisplayName() : "Amethyst Tool";

        SoundUtils.play(player, getSound("EXPIRE"));
        spawnAmethystParticles(player.getLocation().add(0, 1, 0));
        String msg = getMessage("EXPIRED", "{tool}", toolName);
        player.sendMessage(ColorUtils.toComponent(msg));
    }

    public ConfigurationSection getToolSection(AmethystToolType type) {
        ConfigurationSection root = plugin.getConfigManager().getAmethystTools()
                .getConfigurationSection("AMETHYST-TOOLS");
        if (root == null) {
            return null;
        }
        return root.getConfigurationSection(type.getConfigKey());
    }

    public List<String> getExcludedWorlds() {
        ConfigurationSection root = plugin.getConfigManager().getAmethystTools()
                .getConfigurationSection("AMETHYST-TOOLS");
        if (root == null) {
            return Collections.emptyList();
        }
        return root.getStringList("EXCLUDED-WORLDS");
    }

    public boolean isExcludedWorld(String worldName) {
        return getExcludedWorlds().stream().anyMatch(excluded -> excluded.equalsIgnoreCase(worldName));
    }

    public String getMessage(String key) {
        ConfigurationSection msgs = plugin.getConfigManager().getAmethystTools()
                .getConfigurationSection("AMETHYST-MESSAGES");
        if (msgs == null) {
            return key;
        }
        String prefix = msgs.getString("PREFIX", "&#9B59B6[Amethyst] &r");
        String raw = msgs.getString(key, key);
        return raw.replace("{prefix}", prefix);
    }

    public String getMessage(String key, String... replacements) {
        String msg = getMessage(key);
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            msg = msg.replace(replacements[i], replacements[i + 1]);
        }
        return msg;
    }

    public String getSound(String key) {
        ConfigurationSection root = plugin.getConfigManager().getAmethystTools()
                .getConfigurationSection("AMETHYST-TOOLS.SOUNDS");
        if (root == null) {
            return "";
        }
        return root.getString(key, "");
    }

    public String getToolPermission(AmethystToolType type) {
        ConfigurationSection section = getToolSection(type);
        if (section == null) {
            return "";
        }
        return section.getString("PERMISSION", "").trim();
    }

    public boolean isOwnerBindingEnabled() {
        return getSecuritySection().getBoolean("BIND-TO-OWNER", false);
    }

    public boolean requiresItemId() {
        return getSecuritySection().getBoolean("REQUIRE-ITEM-ID", true);
    }

    public boolean shouldBlockAutomationPickup() {
        return getSecuritySection().getBoolean("BLOCK-HOPPER-PICKUP", true);
    }

    public long getUseCooldownMs() {
        return Math.max(0L, getSecuritySection().getLong("CLICK-COOLDOWN-MS", DEFAULT_USE_COOLDOWN_MS));
    }

    public void suppressVisualSync(UUID uuid) {
        suppressVisualSync(uuid, DEFAULT_VISUAL_SYNC_SUPPRESSION_MS);
    }

    public void suppressVisualSync(UUID uuid, long durationMs) {
        if (uuid == null || durationMs <= 0L) {
            return;
        }
        visualSyncSuppressions.put(uuid, System.currentTimeMillis() + durationMs);
    }

    public boolean isVisualSyncSuppressed(UUID uuid) {
        if (uuid == null) {
            return false;
        }

        Long until = visualSyncSuppressions.get(uuid);
        if (until == null) {
            return false;
        }

        if (until <= System.currentTimeMillis()) {
            visualSyncSuppressions.remove(uuid);
            return false;
        }

        return true;
    }

    public long getShardBoosterDurationSeconds() {
        ConfigurationSection section = getToolSection(AmethystToolType.SHARD_BOOSTER);
        if (section == null) {
            return 86400L;
        }
        return Math.max(1L, section.getLong("BOOSTER-DURATION", 86400L));
    }

    public Set<Material> getDisabledBlocks() {
        return parseMaterialSet(getToolSection(AmethystToolType.DRILL), "DISABLED-BLOCKS");
    }

    public Set<Material> getAllowedBlocks() {
        return parseMaterialSet(getToolSection(AmethystToolType.SHOVEL), "ALLOWED-BLOCKS");
    }

    public Set<Material> getLogBlocks() {
        return parseMaterialSet(getToolSection(AmethystToolType.CHOPPER), "LOG-BLOCKS");
    }

    private Set<Material> parseMaterialSet(ConfigurationSection section, String key) {
        if (section == null) {
            return EnumSet.noneOf(Material.class);
        }

        Set<Material> set = EnumSet.noneOf(Material.class);
        for (String name : section.getStringList(key)) {
            try {
                set.add(Material.valueOf(name.toUpperCase(Locale.ROOT).trim()));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return set;
    }

    public void spawnAmethystParticles(Location location) {
        ConfigurationSection root = plugin.getConfigManager().getAmethystTools()
                .getConfigurationSection("AMETHYST-TOOLS.PARTICLES");
        if (root == null || !root.getBoolean("ENABLED", true) || location.getWorld() == null) {
            return;
        }

        int count = root.getInt("COUNT", 12);
        double spread = root.getDouble("SPREAD", 0.4D);
        String particleName = root.getString("TYPE", "BLOCK").toUpperCase(Locale.ROOT);
        Material blockMaterial = ItemUtils.parseMaterial(root.getString("BLOCK-MATERIAL", "PURPLE_CONCRETE_POWDER"));

        try {
            Particle particle = Particle.valueOf(particleName);
            if (particle.getDataType() == BlockData.class) {
                BlockData blockData = blockMaterial.createBlockData();
                location.getWorld().spawnParticle(
                        particle,
                        location.clone().add(0.5, 0.5, 0.5),
                        count,
                        spread,
                        spread,
                        spread,
                        0.0D,
                        blockData
                );
                return;
            }
        } catch (IllegalArgumentException ignored) {
        }

        Particle.DustOptions dust = new Particle.DustOptions(Color.fromRGB(0x9B, 0x59, 0xB6), 1.2f);
        location.getWorld().spawnParticle(
                Particle.DUST,
                location.clone().add(0.5, 0.5, 0.5),
                count,
                spread,
                spread,
                spread,
                0.0D,
                dust
        );
    }

    private ConfigurationSection getSecuritySection() {
        ConfigurationSection root = plugin.getConfigManager().getAmethystTools()
                .getConfigurationSection("AMETHYST-TOOLS.SECURITY");
        if (root != null) {
            return root;
        }
        return plugin.getConfigManager().getAmethystTools().createSection("AMETHYST-TOOLS.SECURITY");
    }
}
