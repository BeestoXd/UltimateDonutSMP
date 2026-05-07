package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.models.SellCategory;
import com.bx.ultimateDonutSmp.models.WorthResult;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionType;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class WorthManager {

    private static final String NULL_LORE = "__NULL__";
    private static final String LORE_SEPARATOR = ";";
    private static final Set<Material> FISH_CATEGORY_OVERRIDES = EnumSet.of(
            Material.COD,
            Material.COOKED_COD,
            Material.SALMON,
            Material.COOKED_SALMON,
            Material.TROPICAL_FISH,
            Material.PUFFERFISH,
            Material.COD_BUCKET,
            Material.SALMON_BUCKET,
            Material.TROPICAL_FISH_BUCKET,
            Material.PUFFERFISH_BUCKET,
            Material.AXOLOTL_BUCKET,
            Material.TADPOLE_BUCKET,
            Material.FISHING_ROD,
            Material.NAME_TAG,
            Material.NAUTILUS_SHELL,
            Material.LILY_PAD,
            Material.HEART_OF_THE_SEA
    );
    private static final Set<Material> POTION_CATEGORY_OVERRIDES = EnumSet.of(
            Material.POTION,
            Material.SPLASH_POTION,
            Material.LINGERING_POTION,
            Material.TIPPED_ARROW,
            Material.BREWING_STAND,
            Material.BLAZE_POWDER,
            Material.BLAZE_ROD,
            Material.FERMENTED_SPIDER_EYE,
            Material.GLASS_BOTTLE,
            Material.GLISTERING_MELON_SLICE,
            Material.GHAST_TEAR,
            Material.MAGMA_CREAM,
            Material.RABBIT_FOOT,
            Material.SPIDER_EYE,
            Material.SUGAR,
            Material.GOLDEN_CARROT,
            Material.PHANTOM_MEMBRANE
    );

    public record WorthBrowserEntry(
            Material material,
            String categoryKey,
            double unitWorth,
            String sourceKey
    ) {}

    private record DirectWorthData(
            double worth,
            String sourceKey,
            String categoryKey,
            String resolutionType
    ) {}

    private final UltimateDonutSmp plugin;
    private final NamespacedKey worthDisplayAppliedKey;
    private final NamespacedKey worthDisplayOriginalLoreKey;

    private List<WorthBrowserEntry> browserEntriesCache = Collections.emptyList();
    private boolean browserEntriesLoaded;

    public WorthManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
        this.worthDisplayAppliedKey = new NamespacedKey(plugin, "worth_display_applied");
        this.worthDisplayOriginalLoreKey = new NamespacedKey(plugin, "worth_display_original_lore");
    }

    public void reload() {
        browserEntriesCache = Collections.emptyList();
        browserEntriesLoaded = false;
    }

    public double getWorth(Material material) {
        if (material == null || material.isAir()) {
            return -1;
        }

        return getWorth(new ItemStack(material));
    }

    public double getWorth(ItemStack item) {
        DirectWorthData data = resolveDirectWorth(item);
        return data == null ? -1 : data.worth();
    }

    public WorthResult resolveWorth(ItemStack item) {
        return resolveWorth(item, 0, true, new HashSet<>());
    }

    public SellCategory getSellCategory(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return null;
        }

        Material material = item.getType();
        if (FISH_CATEGORY_OVERRIDES.contains(material)) {
            return SellCategory.FISH;
        }
        if (POTION_CATEGORY_OVERRIDES.contains(material)) {
            return SellCategory.POTIONS;
        }

        ConfigurationSection typedValues = plugin.getConfigManager().getWorth().getConfigurationSection("TYPE");
        if (typedValues == null) {
            return null;
        }

        for (SellCategory category : SellCategory.values()) {
            ConfigurationSection categorySection = typedValues.getConfigurationSection(category.getWorthSectionKey());
            if (categorySection == null) {
                continue;
            }
            DirectWorthData directWorth = findWorthRecursively(categorySection, item, category.getWorthSectionKey());
            if (directWorth != null) {
                return category;
            }
        }

        return null;
    }

    public List<WorthBrowserEntry> getBrowserEntries() {
        if (browserEntriesLoaded) {
            return browserEntriesCache;
        }

        FileConfiguration worthConfig = plugin.getConfigManager().getWorth();
        ConfigurationSection typedValues = worthConfig.getConfigurationSection("TYPE");
        if (typedValues == null) {
            browserEntriesCache = List.of();
            browserEntriesLoaded = true;
            return browserEntriesCache;
        }

        List<String> configuredOrder = worthConfig.getStringList("BROWSER.CATEGORY-SORT");
        List<String> categoryOrder = new ArrayList<>();
        if (!configuredOrder.isEmpty()) {
            categoryOrder.addAll(configuredOrder);
        }
        for (String categoryKey : typedValues.getKeys(false)) {
            if (!categoryOrder.contains(categoryKey)) {
                categoryOrder.add(categoryKey);
            }
        }

        List<WorthBrowserEntry> entries = new ArrayList<>();
        Set<Material> seenMaterials = new HashSet<>();
        for (String categoryKey : categoryOrder) {
            ConfigurationSection categorySection = typedValues.getConfigurationSection(categoryKey);
            if (categorySection == null) {
                continue;
            }

            for (String key : categorySection.getKeys(false)) {
                if (categorySection.isConfigurationSection(key) || key.contains(":")) {
                    continue;
                }

                Material material = matchMaterial(key);
                if (material == null || material.isAir() || !seenMaterials.add(material)) {
                    continue;
                }

                double unitWorth = categorySection.getDouble(key, -1);
                if (unitWorth < 0) {
                    continue;
                }

                entries.add(new WorthBrowserEntry(material, categoryKey, unitWorth, key));
            }
        }

        browserEntriesCache = List.copyOf(entries);
        browserEntriesLoaded = true;
        return browserEntriesCache;
    }

    public String getBrowserTitle() {
        return plugin.getConfigManager().getWorth().getString("BROWSER.TITLE", "&8Item Prices");
    }

    public int getBrowserSize() {
        int configured = plugin.getConfigManager().getWorth().getInt("BROWSER.SIZE", 54);
        if (configured < 18 || configured > 54 || configured % 9 != 0) {
            return 54;
        }
        return configured;
    }

    public int getBrowserItemsPerPage() {
        int inventorySize = getBrowserSize();
        int configured = plugin.getConfigManager().getWorth().getInt("BROWSER.ITEMS-PER-PAGE", inventorySize - 9);
        int maxAllowed = Math.max(9, inventorySize - 9);
        return Math.max(9, Math.min(maxAllowed, configured));
    }

    public void syncWorthDisplay(Player player) {
        boolean enabled = isWorthDisplayEnabled(player);

        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack current = inventory.getItem(slot);
            ItemStack updated = updateWorthDisplay(current, enabled);
            if (updated != current) {
                inventory.setItem(slot, updated);
            }
        }

        ItemStack cursor = player.getItemOnCursor();
        ItemStack updatedCursor = updateWorthDisplay(cursor, enabled);
        if (updatedCursor != cursor) {
            player.setItemOnCursor(updatedCursor);
        }

        mergePlayerStorageStacks(player);
    }

    public ItemStack applyWorthDisplayForPlayer(Player player, ItemStack item) {
        if (player == null) {
            return item;
        }
        return updateWorthDisplay(item, isWorthDisplayEnabled(player));
    }

    public void mergeStorageStacksForNativeBehavior(Player player) {
        Inventory inventory = player.getInventory();
        ItemStack[] storage = inventory.getStorageContents();

        for (int slot = 0; slot < storage.length; slot++) {
            ItemStack current = storage[slot];
            ItemStack stripped = stripWorthDisplay(current);
            if (stripped != current) {
                storage[slot] = stripped;
                inventory.setItem(slot, stripped);
            }
        }

        mergePlayerStorageStacks(player);
    }

    public void clearWorthDisplay(Player player) {
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack current = inventory.getItem(slot);
            ItemStack stripped = stripWorthDisplay(current);
            if (stripped != current) {
                inventory.setItem(slot, stripped);
            }
        }

        ItemStack cursor = player.getItemOnCursor();
        ItemStack strippedCursor = stripWorthDisplay(cursor);
        if (strippedCursor != cursor) {
            player.setItemOnCursor(strippedCursor);
        }
    }

    public void sanitizeInventory(Inventory inventory) {
        if (inventory == null) {
            return;
        }

        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack current = inventory.getItem(slot);
            ItemStack stripped = stripWorthDisplay(current);
            if (stripped != current) {
                inventory.setItem(slot, stripped);
            }
        }
    }

    public ItemStack stripWorthDisplay(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return item;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (!container.has(worthDisplayAppliedKey, PersistentDataType.BYTE)) {
            return item;
        }

        ItemStack updated = item.clone();
        ItemMeta updatedMeta = updated.getItemMeta();
        if (updatedMeta == null) {
            return item;
        }

        PersistentDataContainer updatedContainer = updatedMeta.getPersistentDataContainer();
        List<Component> originalLore = readOriginalLore(updatedMeta);
        updatedContainer.remove(worthDisplayAppliedKey);
        updatedContainer.remove(worthDisplayOriginalLoreKey);
        updatedMeta.lore(originalLore);
        updated.setItemMeta(updatedMeta);
        return updated;
    }

    public String getWorthLoreLine(ItemStack item) {
        WorthResult worthResult = resolveWorth(item);
        if (!worthResult.sellable()) {
            return null;
        }

        String itemName = prettifyMaterial(item == null ? null : item.getType());
        double displayWorth = shouldUseUnitWorthDisplay(item) ? worthResult.unitWorth() : worthResult.totalWorth();
        return replaceWorthPlaceholders(
                getWorthLoreFormat(),
                NumberUtils.formatNice(displayWorth),
                NumberUtils.formatNice(worthResult.unitWorth()),
                String.valueOf(item == null ? 0 : item.getAmount()),
                itemName
        );
    }

    public String prettifyMaterial(Material material) {
        if (material == null) {
            return "unknown";
        }

        String[] tokens = material.name().toLowerCase(Locale.US).split("_");
        StringBuilder builder = new StringBuilder();
        for (String token : tokens) {
            if (token.isEmpty()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(token.charAt(0))).append(token.substring(1));
        }
        return builder.toString();
    }

    private WorthResult resolveWorth(ItemStack item, int depth, boolean allowNestedExpansion, Set<Integer> visitedContainers) {
        if (item == null || item.getType().isAir()) {
            return WorthResult.unsellable();
        }

        DirectWorthData directWorth = resolveDirectWorth(item);
        if (isContainerWorthEnabled()
                && isSupportedContainer(item)
                && depth < getMaxContainerDepth()) {
            double contentsWorth = resolveContainerContentsWorth(item, depth + 1, allowNestedExpansion, visitedContainers);
            if (contentsWorth > 0) {
                double baseWorth = directWorth == null ? 0 : directWorth.worth();
                double totalUnitWorth = contentsWorth + (shouldIncludeContainerBasePrice() ? baseWorth : 0);
                if (totalUnitWorth > 0) {
                    return new WorthResult(
                            true,
                            true,
                            totalUnitWorth,
                            totalUnitWorth * item.getAmount(),
                            baseWorth,
                            contentsWorth,
                            "CONTAINER",
                            item.getType().name(),
                            directWorth == null ? "" : directWorth.categoryKey()
                    );
                }
            }
        }

        if (directWorth == null) {
            return WorthResult.unsellable();
        }

        return new WorthResult(
                true,
                false,
                directWorth.worth(),
                directWorth.worth() * item.getAmount(),
                directWorth.worth(),
                0,
                directWorth.resolutionType(),
                directWorth.sourceKey(),
                directWorth.categoryKey()
        );
    }

    private double resolveContainerContentsWorth(
            ItemStack item,
            int depth,
            boolean allowNestedExpansion,
            Set<Integer> visitedContainers
    ) {
        if (!(item.getItemMeta() instanceof BlockStateMeta blockStateMeta)) {
            return 0;
        }

        BlockState blockState = blockStateMeta.getBlockState();
        if (!(blockState instanceof Container container)) {
            return 0;
        }

        int containerIdentity = buildContainerIdentity(item);
        if (!visitedContainers.add(containerIdentity)) {
            return 0;
        }

        try {
            double total = 0;
            for (ItemStack content : container.getInventory().getContents()) {
                if (content == null || content.getType().isAir()) {
                    continue;
                }

                WorthResult contentWorth = resolveWorth(
                        content,
                        depth,
                        allowNestedExpansion && allowNestedContainers(),
                        visitedContainers
                );
                if (contentWorth.sellable()) {
                    total += contentWorth.totalWorth();
                }
            }
            return total;
        } finally {
            visitedContainers.remove(containerIdentity);
        }
    }

    private int buildContainerIdentity(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        return Objects.hash(item.getType(), meta == null ? null : meta.getAsString());
    }

    private boolean isSupportedContainer(ItemStack item) {
        return item != null
                && item.getItemMeta() instanceof BlockStateMeta blockStateMeta
                && blockStateMeta.getBlockState() instanceof Container;
    }

    private boolean isContainerWorthEnabled() {
        return plugin.getConfigManager().getWorth().getBoolean("CONTAINER.ENABLED", true);
    }

    private boolean shouldIncludeContainerBasePrice() {
        return plugin.getConfigManager().getWorth().getBoolean("CONTAINER.INCLUDE-CONTAINER-BASE-PRICE", true);
    }

    private boolean allowNestedContainers() {
        return plugin.getConfigManager().getWorth().getBoolean("CONTAINER.ALLOW-NESTED-CONTAINERS", false);
    }

    private int getMaxContainerDepth() {
        return Math.max(1, plugin.getConfigManager().getWorth().getInt("CONTAINER.MAX-CONTAINER-DEPTH", 1));
    }

    private DirectWorthData resolveDirectWorth(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return null;
        }

        FileConfiguration worthConfig = plugin.getConfigManager().getWorth();
        ConfigurationSection typedValues = worthConfig.getConfigurationSection("TYPE");
        if (typedValues != null) {
            for (String categoryKey : typedValues.getKeys(false)) {
                ConfigurationSection categorySection = typedValues.getConfigurationSection(categoryKey);
                DirectWorthData typedWorth = findWorthRecursively(categorySection, item, categoryKey);
                if (typedWorth != null) {
                    return typedWorth;
                }
            }
        }

        return findWorthRecursively(worthConfig, item, "");
    }

    private DirectWorthData findWorthRecursively(ConfigurationSection section, ItemStack item, String categoryKey) {
        if (section == null || item == null || item.getType().isAir()) {
            return null;
        }

        DirectWorthData directWorth = getDirectWorth(section, item, categoryKey);
        if (directWorth != null) {
            return directWorth;
        }

        for (String key : section.getKeys(false)) {
            if (!section.isConfigurationSection(key)) {
                continue;
            }

            DirectWorthData nestedWorth = findWorthRecursively(
                    section.getConfigurationSection(key),
                    item,
                    categoryKey
            );
            if (nestedWorth != null) {
                return nestedWorth;
            }
        }

        return null;
    }

    private DirectWorthData getDirectWorth(ConfigurationSection section, ItemStack item, String categoryKey) {
        DirectWorthData specificWorth = resolveSpecificItemWorth(section, item, categoryKey);
        if (specificWorth != null) {
            return specificWorth;
        }

        String materialKey = item.getType().name();
        if (section.contains(materialKey) && !section.isConfigurationSection(materialKey)) {
            return new DirectWorthData(
                    section.getDouble(materialKey, -1),
                    materialKey,
                    categoryKey,
                    "DIRECT"
            );
        }
        return null;
    }

    private DirectWorthData resolveSpecificItemWorth(ConfigurationSection section, ItemStack item, String categoryKey) {
        if (item.getType() == Material.ENCHANTED_BOOK) {
            return resolveEnchantedBookWorth(section, item, categoryKey);
        }

        if (item.getItemMeta() instanceof PotionMeta meta) {
            return resolvePotionWorth(section, item.getType(), meta, categoryKey);
        }

        return null;
    }

    private DirectWorthData resolveEnchantedBookWorth(ConfigurationSection section, ItemStack item, String categoryKey) {
        if (!(item.getItemMeta() instanceof EnchantmentStorageMeta meta) || meta.getStoredEnchants().isEmpty()) {
            return null;
        }

        double total = 0;
        List<String> matchedKeys = new ArrayList<>();
        for (var enchantmentEntry : meta.getStoredEnchants().entrySet()) {
            Enchantment enchantment = enchantmentEntry.getKey();
            String enchantmentKey = enchantment.getKey().getKey()
                    .toUpperCase(Locale.US)
                    .replace('-', '_');
            String worthKey = item.getType().name() + ":" + enchantmentKey + ":" + enchantmentEntry.getValue();
            if (section.contains(worthKey) && !section.isConfigurationSection(worthKey)) {
                total += section.getDouble(worthKey, 0);
                matchedKeys.add(worthKey);
            }
        }

        if (matchedKeys.isEmpty()) {
            return null;
        }

        return new DirectWorthData(
                total,
                String.join(",", matchedKeys),
                categoryKey,
                "ENCHANTED_BOOK"
        );
    }

    private DirectWorthData resolvePotionWorth(
            ConfigurationSection section,
            Material material,
            PotionMeta meta,
            String categoryKey
    ) {
        PotionType potionType = meta.getBasePotionType();
        if (potionType == null) {
            return null;
        }

        String potionKey = buildPotionWorthKey(material, potionType);
        if (potionKey == null || !section.contains(potionKey) || section.isConfigurationSection(potionKey)) {
            return null;
        }

        return new DirectWorthData(
                section.getDouble(potionKey, -1),
                potionKey,
                categoryKey,
                "POTION"
        );
    }

    private String buildPotionWorthKey(Material material, PotionType potionType) {
        String potionName = potionType.name().toUpperCase(Locale.US);
        boolean upgraded = false;

        if (potionName.startsWith("STRONG_")) {
            potionName = potionName.substring("STRONG_".length());
            upgraded = true;
        } else if (potionName.startsWith("LONG_")) {
            potionName = potionName.substring("LONG_".length());
        }

        StringBuilder builder = new StringBuilder(material.name()).append(':').append(potionName);
        if (upgraded) {
            builder.append(":2");
        }
        return builder.toString();
    }

    private boolean isWorthDisplayEnabled(Player player) {
        PlayerData data = plugin.getPlayerDataManager().get(player);
        return data != null && data.isWorthDisplayEnabled();
    }

    private boolean shouldUseUnitWorthDisplay(ItemStack item) {
        return item != null && item.getMaxStackSize() > 1;
    }

    private boolean mergePlayerStorageStacks(Player player) {
        ItemStack[] storage = player.getInventory().getStorageContents();
        boolean changed = false;

        for (int i = 0; i < storage.length; i++) {
            ItemStack base = storage[i];
            if (base == null || base.getType().isAir()) {
                continue;
            }

            int maxStackSize = base.getMaxStackSize();
            if (base.getAmount() >= maxStackSize) {
                continue;
            }

            for (int j = i + 1; j < storage.length; j++) {
                ItemStack candidate = storage[j];
                if (candidate == null || candidate.getType().isAir()) {
                    continue;
                }

                if (!base.isSimilar(candidate)) {
                    continue;
                }

                int transferable = Math.min(maxStackSize - base.getAmount(), candidate.getAmount());
                if (transferable <= 0) {
                    continue;
                }

                base.setAmount(base.getAmount() + transferable);
                candidate.setAmount(candidate.getAmount() - transferable);
                storage[i] = base;
                storage[j] = candidate.getAmount() <= 0 ? null : candidate;
                player.getInventory().setItem(i, storage[i]);
                player.getInventory().setItem(j, storage[j]);
                changed = true;

                if (base.getAmount() >= maxStackSize) {
                    break;
                }
            }
        }
        return changed;
    }

    private ItemStack updateWorthDisplay(ItemStack item, boolean enabled) {
        if (item == null || item.getType().isAir()) {
            return item;
        }

        if (isWorthDisplayExcluded(item)) {
            return item;
        }

        String loreLine = enabled ? getWorthLoreLine(item) : null;
        if (loreLine == null || loreLine.isBlank()) {
            return stripWorthDisplay(item);
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }

        List<Component> currentLore = meta.lore();
        List<Component> originalLore = readOriginalLore(meta);
        if (originalLore == null) {
            originalLore = currentLore;
        }
        originalLore = stripExistingWorthLore(originalLore);

        List<Component> desiredLore = originalLore == null ? new ArrayList<>() : new ArrayList<>(originalLore);
        desiredLore.add(ColorUtils.toComponent(loreLine));

        PersistentDataContainer container = meta.getPersistentDataContainer();
        String serializedOriginalLore = serializeLore(originalLore);
        String storedOriginalLore = container.get(worthDisplayOriginalLoreKey, PersistentDataType.STRING);
        boolean alreadyApplied = container.has(worthDisplayAppliedKey, PersistentDataType.BYTE);
        if (alreadyApplied
                && Objects.equals(storedOriginalLore, serializedOriginalLore)
                && Objects.equals(currentLore, desiredLore)) {
            return item;
        }

        ItemStack updated = item.clone();
        ItemMeta updatedMeta = updated.getItemMeta();
        if (updatedMeta == null) {
            return item;
        }

        PersistentDataContainer updatedContainer = updatedMeta.getPersistentDataContainer();
        updatedContainer.set(worthDisplayAppliedKey, PersistentDataType.BYTE, (byte) 1);
        updatedContainer.set(worthDisplayOriginalLoreKey, PersistentDataType.STRING, serializedOriginalLore);
        updatedMeta.lore(desiredLore);
        updated.setItemMeta(updatedMeta);
        return updated;
    }

    private boolean isWorthDisplayExcluded(ItemStack item) {
        return item != null && plugin.getAmethystToolsManager().isAmethystTool(item);
    }

    private List<Component> readOriginalLore(ItemMeta meta) {
        PersistentDataContainer container = meta.getPersistentDataContainer();
        String stored = container.get(worthDisplayOriginalLoreKey, PersistentDataType.STRING);
        if (stored != null) {
            return deserializeLore(stored);
        }

        if (!container.has(worthDisplayAppliedKey, PersistentDataType.BYTE)) {
            return meta.lore();
        }

        List<Component> currentLore = meta.lore();
        if (currentLore == null || currentLore.isEmpty()) {
            return null;
        }

        return new ArrayList<>(currentLore.subList(0, currentLore.size() - 1));
    }

    private String serializeLore(List<Component> lore) {
        if (lore == null) {
            return NULL_LORE;
        }
        if (lore.isEmpty()) {
            return "";
        }

        List<String> encoded = new ArrayList<>();
        for (Component component : lore) {
            String json = GsonComponentSerializer.gson().serialize(component);
            encoded.add(Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8)));
        }
        return String.join(LORE_SEPARATOR, encoded);
    }

    private List<Component> deserializeLore(String serialized) {
        if (serialized == null) {
            return null;
        }
        if (NULL_LORE.equals(serialized)) {
            return null;
        }
        if (serialized.isEmpty()) {
            return new ArrayList<>();
        }

        List<Component> lore = new ArrayList<>();
        for (String token : serialized.split(LORE_SEPARATOR, -1)) {
            String json = new String(Base64.getDecoder().decode(token), StandardCharsets.UTF_8);
            lore.add(GsonComponentSerializer.gson().deserialize(json));
        }
        return lore;
    }

    private List<Component> stripExistingWorthLore(List<Component> lore) {
        if (lore == null || lore.isEmpty()) {
            return lore;
        }

        Pattern worthLorePattern = buildWorthLorePattern();
        List<Component> sanitized = new ArrayList<>();
        boolean changed = false;
        for (Component line : lore) {
            if (isWorthLoreLine(line, worthLorePattern)) {
                changed = true;
                continue;
            }
            sanitized.add(line);
        }
        return changed ? sanitized : lore;
    }

    private boolean isWorthLoreLine(Component line, Pattern worthLorePattern) {
        String plain = PlainTextComponentSerializer.plainText().serialize(line);
        if (containsBrokenWorthPlaceholder(plain)) {
            return true;
        }

        return worthLorePattern.matcher(plain).matches();
    }

    private boolean containsBrokenWorthPlaceholder(String plain) {
        return plain.contains("${price}")
                || plain.contains("%price%")
                || plain.contains("{price}")
                || plain.contains("${unit_price}")
                || plain.contains("%unit_price%")
                || plain.contains("{unit_price}");
    }

    private String replaceWorthPlaceholders(
            String format,
            String totalPrice,
            String unitPrice,
            String amount,
            String itemName
    ) {
        String resolved = replacePlaceholderVariants(format, "price", totalPrice);
        resolved = replacePlaceholderVariants(resolved, "unit_price", unitPrice);
        resolved = replacePlaceholderVariants(resolved, "amount", amount);
        return replacePlaceholderVariants(resolved, "item", itemName);
    }

    private String replacePlaceholderVariants(String text, String key, String value) {
        return text
                .replace("%" + key + "%", value)
                .replace("${" + key + "}", value)
                .replace("{" + key + "}", value);
    }

    private String getWorthLoreFormat() {
        FileConfiguration worthConfig = plugin.getConfigManager().getWorth();
        String format = firstNonBlank(
                worthConfig.getString("WORTH-LORE.FORMAT"),
                worthConfig.getString("DISPLAY.FORMAT"),
                worthConfig.getString("FORMAT")
        );
        if (format != null) {
            return normalizeWorthLoreFormat(format);
        }

        return normalizeWorthLoreFormat(
                plugin.getConfigManager().getConfig()
                        .getString("WORTH-LORE.FORMAT", "&7Worth: &a$%price%")
        );
    }

    private String firstNonBlank(String... candidates) {
        for (String candidate : candidates) {
            if (candidate != null && !candidate.isBlank()) {
                return candidate;
            }
        }
        return null;
    }

    private String normalizeWorthLoreFormat(String format) {
        if (format == null || format.isBlank()) {
            return "&7Worth: &a$%price%";
        }
        if (format.contains("$")) {
            return format;
        }

        for (String placeholder : List.of("%price%", "${price}", "{price}")) {
            int index = format.indexOf(placeholder);
            if (index >= 0) {
                return format.substring(0, index) + "$" + format.substring(index);
            }
        }

        return format;
    }

    private Pattern buildWorthLorePattern() {
        String format = stripColorCodes(getWorthLoreFormat());
        List<String> placeholders = List.of(
                "%unit_price%", "${unit_price}", "{unit_price}",
                "%price%", "${price}", "{price}",
                "%amount%", "${amount}", "{amount}",
                "%item%", "${item}", "{item}"
        );

        StringBuilder regex = new StringBuilder("^");
        int index = 0;
        while (index < format.length()) {
            int nextPlaceholderIndex = -1;
            String nextPlaceholder = null;
            for (String placeholder : placeholders) {
                int candidateIndex = format.indexOf(placeholder, index);
                if (candidateIndex < 0) {
                    continue;
                }
                if (nextPlaceholderIndex < 0 || candidateIndex < nextPlaceholderIndex) {
                    nextPlaceholderIndex = candidateIndex;
                    nextPlaceholder = placeholder;
                }
            }

            if (nextPlaceholderIndex < 0) {
                regex.append(Pattern.quote(format.substring(index)));
                break;
            }

            String staticPart = format.substring(index, nextPlaceholderIndex);
            if (isPricePlaceholder(nextPlaceholder) && staticPart.endsWith("$")) {
                staticPart = staticPart.substring(0, staticPart.length() - 1);
                regex.append(Pattern.quote(staticPart));
                regex.append("\\$?");
            } else {
                regex.append(Pattern.quote(staticPart));
            }

            regex.append(".+?");
            index = nextPlaceholderIndex + nextPlaceholder.length();
        }

        regex.append("$");
        try {
            return Pattern.compile(regex.toString());
        } catch (PatternSyntaxException exception) {
            return Pattern.compile("^$");
        }
    }

    private String stripColorCodes(String text) {
        return text
                .replaceAll("(?i)&#[0-9a-f]{6}", "")
                .replaceAll("(?i)&[0-9a-fk-or]", "");
    }

    private boolean isPricePlaceholder(String placeholder) {
        return placeholder.contains("price");
    }

    private Material matchMaterial(String key) {
        Material material = Material.matchMaterial(key);
        if (material != null) {
            return material;
        }
        return Registry.MATERIAL.get(NamespacedKey.minecraft(key.toLowerCase(Locale.US)));
    }
}
