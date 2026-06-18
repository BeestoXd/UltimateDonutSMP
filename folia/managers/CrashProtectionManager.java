package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemSerializationUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;

public class CrashProtectionManager {

    private static final String CONFIG_ROOT = "CRASH-PROTECTION";

    private final UltimateDonutSmp plugin;

    public CrashProtectionManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public enum Context {
        AUCTION_HOUSE("Auction House"),
        ORDERS("Orders"),
        ENDER_CHEST("Ender Chest"),
        CRATES("Crates"),
        DUELS("Duels"),
        DATABASE_LOAD("Database Load");

        private final String displayName;

        Context(String displayName) {
            this.displayName = displayName;
        }

        public String displayName() {
            return displayName;
        }
    }

    public record ValidationResult(boolean allowed, String reason) {
        public static ValidationResult permit() {
            return new ValidationResult(true, "");
        }

        public static ValidationResult blocked(String reason) {
            return new ValidationResult(false, reason == null || reason.isBlank() ? "unsafe item data" : reason);
        }
    }

    private record ContainerStats(int itemCount) {}

    public void reload() {
        validateConfiguration();
    }

    public boolean isEnabled() {
        return config().getBoolean(CONFIG_ROOT + ".ENABLED", true);
    }

    public ValidationResult validateForStorage(ItemStack item, Context context) {
        if (!isEnabled() || isMissing(item)) {
            return ValidationResult.permit();
        }

        try {
            int serializedSize = ItemSerializationUtils.serializedByteSize(item);
            int maxSerializedBytes = maxSerializedBytes();
            if (serializedSize > maxSerializedBytes) {
                return ValidationResult.blocked("item data is too large (" + serializedSize + "/" + maxSerializedBytes + " bytes)");
            }
        } catch (IOException exception) {
            return ValidationResult.blocked("item data could not be serialized");
        }

        ValidationResult metaResult = validateMeta(item);
        if (!metaResult.allowed()) {
            return metaResult;
        }

        return validateContainers(item, 0).result();
    }

    public ValidationResult validateOrNotify(Player player, ItemStack item, Context context) {
        ValidationResult result = validateForStorage(item, context);
        if (!result.allowed()) {
            notifyBlocked(player, item, context, result);
        }
        return result;
    }

    public boolean isAllowedForStorage(ItemStack item, Context context) {
        return validateForStorage(item, context).allowed();
    }

    public void logBlockedItem(String source, ItemStack item, Context context, ValidationResult result) {
        if (result == null || result.allowed()) {
            return;
        }

        plugin.getLogger().warning("Crash protection blocked " + describeItem(item)
                + " in " + displayContext(context)
                + (source == null || source.isBlank() ? "" : " from " + source)
                + ": " + result.reason());
    }

    private void notifyBlocked(Player player, ItemStack item, Context context, ValidationResult result) {
        logBlockedItem(player == null ? "" : player.getName() + "/" + player.getUniqueId(), item, context, result);
        if (player == null || !player.isOnline()) {
            return;
        }

        String message = plugin.getConfigManager().getMessageOrDefault(
                "CRASH_PROTECTION.ITEM_BLOCKED",
                "&cThat item cannot be used here because its data looks unsafe. &7Context: &f{context}&7. Reason: &f{reason}",
                "{context}", displayContext(context),
                "{reason}", result.reason()
        );
        player.sendMessage(ColorUtils.toComponent(message));
    }

    private ValidationResult validateMeta(ItemStack item) {
        if (!item.hasItemMeta()) {
            return ValidationResult.permit();
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return ValidationResult.permit();
        }

        if (meta.hasDisplayName() && meta.getDisplayName() != null) {
            int length = meta.getDisplayName().length();
            int max = maxDisplayNameLength();
            if (length > max) {
                return ValidationResult.blocked("display name is too long (" + length + "/" + max + ")");
            }
        }

        if (meta.hasLore() && meta.getLore() != null) {
            int maxLines = maxLoreLines();
            if (meta.getLore().size() > maxLines) {
                return ValidationResult.blocked("lore has too many lines (" + meta.getLore().size() + "/" + maxLines + ")");
            }

            int maxLineLength = maxLoreLineLength();
            for (String line : meta.getLore()) {
                int length = line == null ? 0 : line.length();
                if (length > maxLineLength) {
                    return ValidationResult.blocked("lore line is too long (" + length + "/" + maxLineLength + ")");
                }
            }
        }

        int pdcKeys = meta.getPersistentDataContainer().getKeys().size();
        int maxPdcKeys = maxPersistentDataKeys();
        if (pdcKeys > maxPdcKeys) {
            return ValidationResult.blocked("too many persistent data keys (" + pdcKeys + "/" + maxPdcKeys + ")");
        }

        if (meta instanceof BookMeta bookMeta) {
            ValidationResult bookResult = validateBook(bookMeta);
            if (!bookResult.allowed()) {
                return bookResult;
            }
        }

        return ValidationResult.permit();
    }

    private ValidationResult validateBook(BookMeta meta) {
        int pages = meta.getPageCount();
        int maxPages = maxBookPages();
        if (pages > maxPages) {
            return ValidationResult.blocked("book has too many pages (" + pages + "/" + maxPages + ")");
        }

        int maxPageLength = maxBookPageLength();
        int maxTotalChars = maxBookTotalChars();
        int totalChars = 0;
        for (String page : meta.getPages()) {
            int pageLength = page == null ? 0 : page.length();
            if (pageLength > maxPageLength) {
                return ValidationResult.blocked("book page is too long (" + pageLength + "/" + maxPageLength + ")");
            }
            totalChars += pageLength;
            if (totalChars > maxTotalChars) {
                return ValidationResult.blocked("book has too much text (" + totalChars + "/" + maxTotalChars + ")");
            }
        }

        return ValidationResult.permit();
    }

    private ContainerValidation validateContainers(ItemStack item, int depth) {
        if (isMissing(item) || !(item.getItemMeta() instanceof BlockStateMeta blockStateMeta)) {
            return ContainerValidation.allowed(0);
        }

        BlockState blockState = blockStateMeta.getBlockState();
        if (!(blockState instanceof Container container)) {
            return ContainerValidation.allowed(0);
        }

        int maxDepth = maxContainerDepth();
        if (depth > maxDepth) {
            return ContainerValidation.blocked("container nesting is too deep (" + depth + "/" + maxDepth + ")");
        }

        int nestedItems = 0;
        int maxNestedItems = maxNestedContainerItems();
        for (ItemStack content : container.getInventory().getContents()) {
            if (isMissing(content)) {
                continue;
            }

            nestedItems++;
            if (nestedItems > maxNestedItems) {
                return ContainerValidation.blocked("container has too many stored items (" + nestedItems + "/" + maxNestedItems + ")");
            }

            if (content.getItemMeta() instanceof BlockStateMeta nestedMeta
                    && nestedMeta.getBlockState() instanceof Container) {
                if (blockNestedContainers()) {
                    return ContainerValidation.blocked("nested containers are not allowed");
                }
            }

            ValidationResult metaResult = validateMeta(content);
            if (!metaResult.allowed()) {
                return ContainerValidation.blocked("stored item is unsafe: " + metaResult.reason());
            }

            ContainerValidation nestedResult = validateContainers(content, depth + 1);
            if (!nestedResult.result().allowed()) {
                return nestedResult;
            }
            nestedItems += nestedResult.stats().itemCount();
            if (nestedItems > maxNestedItems) {
                return ContainerValidation.blocked("container has too many stored items (" + nestedItems + "/" + maxNestedItems + ")");
            }
        }

        return ContainerValidation.allowed(nestedItems);
    }

    private record ContainerValidation(ValidationResult result, ContainerStats stats) {
        private static ContainerValidation allowed(int itemCount) {
            return new ContainerValidation(ValidationResult.permit(), new ContainerStats(itemCount));
        }

        private static ContainerValidation blocked(String reason) {
            return new ContainerValidation(ValidationResult.blocked(reason), new ContainerStats(0));
        }
    }

    private void validateConfiguration() {
        if (maxSerializedBytes() < 1024) {
            plugin.getLogger().warning("CRASH-PROTECTION.MAX-SERIALIZED-BYTES is very low and may block normal items.");
        }
        if (maxContainerDepth() < 0) {
            plugin.getLogger().warning("CRASH-PROTECTION.MAX-CONTAINER-DEPTH is below 0; containers will be blocked.");
        }
    }

    private boolean isMissing(ItemStack item) {
        return item == null || item.getType().isAir() || item.getAmount() <= 0;
    }

    private String describeItem(ItemStack item) {
        if (item == null) {
            return "null item";
        }
        return item.getType().name().toLowerCase(Locale.US) + " x" + Math.max(0, item.getAmount());
    }

    private String displayContext(Context context) {
        return context == null ? Context.DATABASE_LOAD.displayName() : context.displayName();
    }

    private FileConfiguration config() {
        return plugin.getConfigManager().getConfig();
    }

    private int maxSerializedBytes() {
        return Math.max(1, config().getInt(CONFIG_ROOT + ".MAX-SERIALIZED-BYTES", 65_536));
    }

    private int maxDisplayNameLength() {
        return Math.max(1, config().getInt(CONFIG_ROOT + ".MAX-DISPLAY-NAME-LENGTH", 256));
    }

    private int maxLoreLines() {
        return Math.max(0, config().getInt(CONFIG_ROOT + ".MAX-LORE-LINES", 80));
    }

    private int maxLoreLineLength() {
        return Math.max(1, config().getInt(CONFIG_ROOT + ".MAX-LORE-LINE-LENGTH", 512));
    }

    private int maxBookPages() {
        return Math.max(0, config().getInt(CONFIG_ROOT + ".MAX-BOOK-PAGES", 50));
    }

    private int maxBookPageLength() {
        return Math.max(1, config().getInt(CONFIG_ROOT + ".MAX-BOOK-PAGE-LENGTH", 1024));
    }

    private int maxBookTotalChars() {
        return Math.max(1, config().getInt(CONFIG_ROOT + ".MAX-BOOK-TOTAL-CHARS", 16_384));
    }

    private int maxPersistentDataKeys() {
        return Math.max(0, config().getInt(CONFIG_ROOT + ".MAX-PERSISTENT-DATA-KEYS", 64));
    }

    private int maxContainerDepth() {
        return Math.max(0, config().getInt(CONFIG_ROOT + ".MAX-CONTAINER-DEPTH", 1));
    }

    private int maxNestedContainerItems() {
        return Math.max(0, config().getInt(CONFIG_ROOT + ".MAX-NESTED-CONTAINER-ITEMS", 54));
    }

    private boolean blockNestedContainers() {
        return config().getBoolean(CONFIG_ROOT + ".BLOCK-NESTED-CONTAINERS", true);
    }
}
