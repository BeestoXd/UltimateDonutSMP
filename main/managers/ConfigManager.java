package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

public class ConfigManager {

    private static final List<String> CONFIGURATION_RESOURCES = List.of(
            "config.yml",
            "messages.yml",
            "death-messages.yml",
            "menus.yml",
            "scoreboard.yml",
            "shop.yml",
            "sounds.yml",
            "billford.yml",
            "rtp.yml",
            "worth.yml",
            "amethyst-tools.yml",
            "ender-chest.yml",
            "invsee.yml",
            "freeze.yml",
            "auction-house.yml",
            "orders.yml",
            "duels.yml",
            "ffa.yml",
            "crates.yml",
            "spawners.yml",
            "spawn-stash.yml",
            "network.yml",
            "staff-mode.yml",
            "database.yml",
            "discord.yml"
    );

    private static final DateTimeFormatter BACKUP_TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");
    private static final String SETUP_COMMENT_PREFIX = "# UDS setup:";

    private final UltimateDonutSmp plugin;

    private FileConfiguration config;
    private FileConfiguration messages;
    private FileConfiguration deathMessages;
    private FileConfiguration menus;
    private FileConfiguration scoreboard;
    private FileConfiguration shop;
    private FileConfiguration sounds;
    private FileConfiguration billford;
    private FileConfiguration rtp;
    private FileConfiguration worth;
    private FileConfiguration amethystTools;
    private FileConfiguration enderChest;
    private FileConfiguration invsee;
    private FileConfiguration freeze;
    private FileConfiguration auctionHouse;
    private FileConfiguration orders;
    private FileConfiguration duels;
    private FileConfiguration ffa;
    private FileConfiguration crates;
    private FileConfiguration spawners;
    private FileConfiguration spawnStash;
    private FileConfiguration network;
    private FileConfiguration staffMode;
    private FileConfiguration database;
    private FileConfiguration discord;

    public ConfigManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public void loadAll() {
        syncBundledConfigurations();
        reloadLoadedConfigurations();
    }

    public void reload() {
        syncBundledConfigurations();
        reloadLoadedConfigurations();
    }

    private void reloadLoadedConfigurations() {
        plugin.reloadConfig();
        config       = plugin.getConfig();
        messages     = load("messages.yml");
        deathMessages= load("death-messages.yml");
        menus        = load("menus.yml");
        scoreboard   = load("scoreboard.yml");
        shop         = load("shop.yml");
        sounds       = load("sounds.yml");
        billford     = load("billford.yml");
        rtp          = load("rtp.yml");
        worth        = load("worth.yml");
        amethystTools = load("amethyst-tools.yml");
        enderChest   = load("ender-chest.yml");
        invsee       = load("invsee.yml");
        freeze       = load("freeze.yml");
        auctionHouse = load("auction-house.yml");
        orders       = load("orders.yml");
        duels        = load("duels.yml");
        ffa          = load("ffa.yml");
        crates       = load("crates.yml");
        spawners     = load("spawners.yml");
        spawnStash   = load("spawn-stash.yml");
        network      = load("network.yml");
        staffMode    = load("staff-mode.yml");
        database     = load("database.yml");
        discord      = load("discord.yml");
    }

    private void syncBundledConfigurations() {
        File backupDirectory = new File(
                new File(plugin.getDataFolder(), "config-backups"),
                LocalDateTime.now().format(BACKUP_TIMESTAMP_FORMAT)
        );

        int created = 0;
        int updated = 0;
        int restored = 0;
        int skipped = 0;
        int snapshots = 0;

        for (String name : CONFIGURATION_RESOURCES) {
            SyncResult result = syncBundledConfiguration(name, backupDirectory);
            if (result.created) {
                created++;
            }
            if (result.updated) {
                updated++;
            }
            if (result.restored) {
                restored++;
            }
            if (result.skipped) {
                skipped++;
            }
            if (result.snapshotUpdated) {
                snapshots++;
            }
        }

        plugin.getLogger().info("Configuration sync complete: "
                + created + " created, "
                + updated + " updated, "
                + restored + " restored, "
                + snapshots + " default snapshots refreshed"
                + (skipped > 0 ? ", " + skipped + " skipped" : "")
                + ".");
    }

    private SyncResult syncBundledConfiguration(String name, File backupDirectory) {
        SyncResult result = new SyncResult();
        File targetFile = new File(plugin.getDataFolder(), name);

        YamlConfiguration bundledDefault;
        try {
            bundledDefault = loadBundledYaml(name);
        } catch (IOException | InvalidConfigurationException | IllegalArgumentException e) {
            result.skipped = true;
            plugin.getLogger().log(Level.WARNING, "Skipping configuration sync for missing or invalid bundled resource: " + name, e);
            return result;
        }

        if (!targetFile.exists()) {
            if (copyBundledResource(name, targetFile, false)) {
                result.created = true;
                result.snapshotUpdated = refreshDefaultSnapshot(name);
            } else {
                result.skipped = true;
            }
            return result;
        }

        YamlConfiguration current;
        try {
            current = loadYamlFile(targetFile);
        } catch (IOException | InvalidConfigurationException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load " + targetFile.getPath() + ", restoring default copy.", e);
            backupExistingFile(targetFile, backupDirectory);
            if (copyBundledResource(name, targetFile, true)) {
                result.restored = true;
                result.snapshotUpdated = refreshDefaultSnapshot(name);
            } else {
                result.skipped = true;
            }
            return result;
        }

        YamlConfiguration previousDefault = loadPreviousDefaultSnapshot(name);
        int mergedPaths = mergeBundledDefaults(name, current, bundledDefault, previousDefault);
        if (mergedPaths > 0) {
            backupExistingFile(targetFile, backupDirectory);
            try {
                current.save(targetFile);
                result.updated = true;
                syncRtpSearchDefaultsAndComments(name, targetFile, backupDirectory, true);
                syncBundledCommentTags(name, targetFile, backupDirectory, true);
                syncOrdersPricingDefaultsAndComments(name, targetFile, backupDirectory, true);
                syncCrashProtectionPlacement(name, targetFile, backupDirectory, true);
                syncBundledSetupComments(name, targetFile, backupDirectory, true);
                result.snapshotUpdated = refreshDefaultSnapshot(name);
                plugin.getLogger().info("Updated " + name + " with " + mergedPaths + " bundled default path(s).");
            } catch (IOException e) {
                result.skipped = true;
                plugin.getLogger().log(Level.WARNING, "Failed to save synced configuration " + targetFile.getPath(), e);
            }
            return result;
        }

        boolean rtpUpdated = syncRtpSearchDefaultsAndComments(name, targetFile, backupDirectory, false);
        boolean commentsUpdated = syncBundledCommentTags(name, targetFile, backupDirectory, rtpUpdated);
        boolean pricingUpdated = syncOrdersPricingDefaultsAndComments(name, targetFile, backupDirectory, rtpUpdated || commentsUpdated);
        boolean crashProtectionUpdated = syncCrashProtectionPlacement(
                name,
                targetFile,
                backupDirectory,
                rtpUpdated || commentsUpdated || pricingUpdated
        );
        boolean setupCommentsUpdated = syncBundledSetupComments(
                name,
                targetFile,
                backupDirectory,
                rtpUpdated || commentsUpdated || pricingUpdated || crashProtectionUpdated
        );
        if (rtpUpdated || commentsUpdated || pricingUpdated || crashProtectionUpdated || setupCommentsUpdated) {
            result.updated = true;
        }
        result.snapshotUpdated = refreshDefaultSnapshot(name);
        return result;
    }

    private boolean syncCrashProtectionPlacement(String resourceName, File targetFile, File backupDirectory, boolean alreadyBackedUp) {
        if (!"config.yml".equals(resourceName)) {
            return false;
        }

        try {
            List<String> lines = Files.readAllLines(targetFile.toPath(), StandardCharsets.UTF_8);
            int crashProtectionLine = findTopLevelConfigLine(lines, "CRASH-PROTECTION:");
            if (crashProtectionLine < 0) {
                return false;
            }

            int commentStart = crashProtectionLine;
            while (commentStart > 0 && isCrashProtectionComment(lines.get(commentStart - 1))) {
                commentStart--;
            }

            int blockEnd = findTopLevelBlockEnd(lines, crashProtectionLine);
            List<String> crashProtectionBlock = new ArrayList<>();
            crashProtectionBlock.add(SETUP_COMMENT_PREFIX
                    + " Blocks unsafe item metadata before it is saved in UDS storage.");
            crashProtectionBlock.addAll(lines.subList(crashProtectionLine, blockEnd));
            while (!crashProtectionBlock.isEmpty()
                    && crashProtectionBlock.get(crashProtectionBlock.size() - 1).trim().isEmpty()) {
                crashProtectionBlock.remove(crashProtectionBlock.size() - 1);
            }

            List<String> remainingLines = new ArrayList<>(lines.size() + 3);
            remainingLines.addAll(lines.subList(0, commentStart));
            remainingLines.addAll(lines.subList(blockEnd, lines.size()));

            int insertAt = crashProtectionInsertIndex(remainingLines);
            normalizeBlankLinesAroundInsertion(remainingLines, insertAt);
            insertAt = crashProtectionInsertIndex(remainingLines);

            List<String> updatedLines = new ArrayList<>(remainingLines.size() + crashProtectionBlock.size() + 2);
            updatedLines.addAll(remainingLines.subList(0, insertAt));
            if (!updatedLines.isEmpty() && !updatedLines.get(updatedLines.size() - 1).trim().isEmpty()) {
                updatedLines.add("");
            }
            updatedLines.addAll(crashProtectionBlock);
            if (insertAt < remainingLines.size()) {
                updatedLines.add("");
            }
            updatedLines.addAll(remainingLines.subList(insertAt, remainingLines.size()));

            if (updatedLines.equals(lines)) {
                return false;
            }

            if (!alreadyBackedUp) {
                backupExistingFile(targetFile, backupDirectory);
            }
            Files.write(targetFile.toPath(), updatedLines, StandardCharsets.UTF_8);
            plugin.getLogger().info("Updated config.yml crash protection placement/comment tags.");
            return true;
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to sync config.yml crash protection placement.", e);
            return false;
        }
    }

    private int crashProtectionInsertIndex(List<String> lines) {
        int settingsLine = findTopLevelConfigLine(lines, "SETTINGS:");
        if (settingsLine >= 0) {
            return findTopLevelBlockEnd(lines, settingsLine);
        }

        int chatLine = findTopLevelConfigLine(lines, "CHAT:");
        if (chatLine >= 0) {
            return chatLine;
        }

        int commandsLine = findTopLevelConfigLine(lines, "COMMANDS:");
        if (commandsLine >= 0) {
            return commandsLine;
        }

        return lines.size();
    }

    private void normalizeBlankLinesAroundInsertion(List<String> lines, int insertAt) {
        while (insertAt > 0 && lines.get(insertAt - 1).trim().isEmpty()) {
            lines.remove(insertAt - 1);
            insertAt--;
        }
        while (insertAt < lines.size() && lines.get(insertAt).trim().isEmpty()) {
            lines.remove(insertAt);
        }
    }

    private boolean syncBundledSetupComments(String resourceName, File targetFile, File backupDirectory, boolean alreadyBackedUp) {
        try {
            List<String> bundledLines = readBundledResourceLines(resourceName);
            List<String> lines = Files.readAllLines(targetFile.toPath(), StandardCharsets.UTF_8);
            boolean changed = false;

            changed |= syncManagedHeader(lines, extractManagedHeader(bundledLines));
            Map<String, List<String>> commentsByKey = collectTopLevelSetupComments(bundledLines);
            for (Map.Entry<String, List<String>> entry : commentsByKey.entrySet()) {
                changed |= syncTopLevelSetupComment(lines, entry.getKey(), entry.getValue());
            }

            if (!changed) {
                return false;
            }

            if (!alreadyBackedUp) {
                backupExistingFile(targetFile, backupDirectory);
            }
            Files.write(targetFile.toPath(), lines, StandardCharsets.UTF_8);
            plugin.getLogger().info("Updated " + resourceName + " setup comment tags.");
            return true;
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to sync setup comments for " + resourceName + ".", e);
            return false;
        }
    }

    private List<String> readBundledResourceLines(String name) throws IOException {
        String content = new String(readBundledResourceBytes(name), StandardCharsets.UTF_8);
        return new ArrayList<>(Arrays.asList(content.split("\\R", -1)));
    }

    private List<String> extractManagedHeader(List<String> lines) {
        List<String> header = new ArrayList<>();
        for (String line : lines) {
            if (!isSetupComment(line)) {
                break;
            }
            header.add(line);
        }
        return header;
    }

    private Map<String, List<String>> collectTopLevelSetupComments(List<String> lines) {
        Map<String, List<String>> commentsByKey = new LinkedHashMap<>();
        for (int index = 0; index < lines.size(); index++) {
            String keyPrefix = topLevelKeyPrefix(lines.get(index));
            if (keyPrefix == null) {
                continue;
            }

            int commentStart = index;
            while (commentStart > 0 && isSetupComment(lines.get(commentStart - 1))) {
                commentStart--;
            }
            if (commentStart < index) {
                commentsByKey.put(keyPrefix, new ArrayList<>(lines.subList(commentStart, index)));
            }
        }
        return commentsByKey;
    }

    private boolean syncManagedHeader(List<String> lines, List<String> desiredHeader) {
        if (desiredHeader.isEmpty()) {
            return false;
        }

        int removeEnd = findManagedHeaderEnd(lines);
        List<String> currentHeader = new ArrayList<>(lines.subList(0, removeEnd));
        if (currentHeader.equals(desiredHeader)) {
            return false;
        }

        lines.subList(0, removeEnd).clear();
        lines.addAll(0, desiredHeader);
        if (lines.size() > desiredHeader.size() && !lines.get(desiredHeader.size()).trim().isEmpty()) {
            lines.add(desiredHeader.size(), "");
        }
        return true;
    }

    private boolean syncTopLevelSetupComment(List<String> lines, String keyPrefix, List<String> desiredComments) {
        int keyLine = findTopLevelConfigLine(lines, keyPrefix);
        if (keyLine < 0) {
            return false;
        }

        int commentStart = findSetupCommentBlockStart(lines, keyLine, desiredComments);

        List<String> currentComments = new ArrayList<>(lines.subList(commentStart, keyLine));
        if (currentComments.equals(desiredComments)) {
            return false;
        }

        lines.subList(commentStart, keyLine).clear();
        lines.addAll(commentStart, desiredComments);
        return true;
    }

    private int findManagedHeaderEnd(List<String> lines) {
        int index = 0;
        while (index < lines.size() && isSetupComment(lines.get(index))) {
            index++;
        }

        while (index < lines.size()) {
            int blankStart = index;
            while (index < lines.size() && lines.get(index).trim().isEmpty()) {
                index++;
            }
            if (index == blankStart || index >= lines.size() || !isSetupComment(lines.get(index))) {
                return blankStart;
            }

            int setupBlockEnd = index;
            while (setupBlockEnd < lines.size() && isSetupComment(lines.get(setupBlockEnd))) {
                setupBlockEnd++;
            }

            int nextContent = nextNonBlankIndex(lines, setupBlockEnd);
            if (nextContent >= 0 && topLevelKeyPrefix(lines.get(nextContent)) != null) {
                return blankStart;
            }
            index = setupBlockEnd;
        }

        return index;
    }

    private int findSetupCommentBlockStart(List<String> lines, int keyLine, List<String> desiredComments) {
        int commentStart = keyLine;
        while (commentStart > 0) {
            String previousLine = lines.get(commentStart - 1);
            if (isSetupComment(previousLine)) {
                commentStart--;
                continue;
            }
            if (previousLine.trim().isEmpty()
                    && isSetupCommentBeforeManagedBlank(lines, commentStart - 1, desiredComments)) {
                commentStart--;
                continue;
            }
            break;
        }
        return commentStart;
    }

    private boolean isSetupCommentBeforeManagedBlank(
            List<String> lines,
            int blankLineIndex,
            List<String> desiredComments
    ) {
        int previousContent = previousNonBlankIndex(lines, blankLineIndex - 1);
        if (previousContent < 0 || !isSetupComment(lines.get(previousContent))) {
            return false;
        }
        return desiredComments.contains(lines.get(previousContent))
                || hasTopLevelKeyBefore(lines, previousContent);
    }

    private int previousNonBlankIndex(List<String> lines, int startIndex) {
        for (int index = startIndex; index >= 0; index--) {
            if (!lines.get(index).trim().isEmpty()) {
                return index;
            }
        }
        return -1;
    }

    private int nextNonBlankIndex(List<String> lines, int startIndex) {
        for (int index = startIndex; index < lines.size(); index++) {
            if (!lines.get(index).trim().isEmpty()) {
                return index;
            }
        }
        return -1;
    }

    private boolean hasTopLevelKeyBefore(List<String> lines, int beforeIndex) {
        for (int index = 0; index < beforeIndex; index++) {
            if (topLevelKeyPrefix(lines.get(index)) != null) {
                return true;
            }
        }
        return false;
    }

    private String topLevelKeyPrefix(String line) {
        if (line == null || !leadingWhitespace(line).isEmpty()) {
            return null;
        }

        String trimmed = line.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("-")) {
            return null;
        }

        int colonIndex = trimmed.indexOf(':');
        if (colonIndex <= 0) {
            return null;
        }
        return trimmed.substring(0, colonIndex + 1);
    }

    private boolean syncBundledCommentTags(String resourceName, File targetFile, File backupDirectory, boolean alreadyBackedUp) {
        if (!"orders.yml".equals(resourceName)) {
            return false;
        }

        try {
            List<String> lines = Files.readAllLines(targetFile.toPath(), StandardCharsets.UTF_8);
            boolean changed = false;

            int modeLineIndex = findConfigLine(lines, "ITEM_SELECTION_MODE:");
            if (modeLineIndex >= 0) {
                String indent = leadingWhitespace(lines.get(modeLineIndex));
                changed |= syncCommentBlockBeforeLine(
                        lines,
                        "ITEM_SELECTION_MODE:",
                        List.of(
                                indent + "# SELECT_ITEM = opens the configured order catalog menu. This is the default.",
                                indent + "# INVENTORY_ITEM = lets players click an item from their inventory as the exact order template.",
                                indent + "# SEARCH_ITEM = asks players to type an item/category search, then opens matching results.",
                                indent + "# Valid values: SELECT_ITEM, INVENTORY_ITEM, SEARCH_ITEM. Invalid values fall back to SELECT_ITEM."
                        ),
                        this::isItemSelectionModeComment
                );
            }

            int sourceLineIndex = findConfigLine(lines, "SELECT_ITEM_SOURCE:");
            if (sourceLineIndex >= 0) {
                String indent = leadingWhitespace(lines.get(sourceLineIndex));
                changed |= syncCommentBlockBeforeLine(
                        lines,
                        "SELECT_ITEM_SOURCE:",
                        List.of(
                                indent + "# SELECT_ITEM_SOURCE only changes the SELECT_ITEM menu contents.",
                                indent + "# CATEGORY_FILTERS = use the curated CATEGORY_FILTERS list below. This is the default.",
                                indent + "# SERVER_MATERIALS = generate all orderable materials from the running server.jar.",
                                indent + "# Valid values: CATEGORY_FILTERS, SERVER_MATERIALS. Invalid values fall back to CATEGORY_FILTERS."
                        ),
                        this::isSelectItemSourceComment
                );
            }

            if (!changed) {
                return false;
            }

            if (!alreadyBackedUp) {
                backupExistingFile(targetFile, backupDirectory);
            }
            Files.write(targetFile.toPath(), lines, StandardCharsets.UTF_8);
            plugin.getLogger().info("Updated orders.yml item selection comment tags.");
            return true;
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to sync orders.yml item selection comment tags.", e);
            return false;
        }
    }

    private boolean syncOrdersPricingDefaultsAndComments(String resourceName, File targetFile, File backupDirectory, boolean alreadyBackedUp) {
        if (!"orders.yml".equals(resourceName)) {
            return false;
        }

        try {
            List<String> lines = Files.readAllLines(targetFile.toPath(), StandardCharsets.UTF_8);
            boolean changed = false;

            int maxPriceLineIndex = findConfigLine(lines, "MAX_PRICE_EACH:");
            if (maxPriceLineIndex >= 0) {
                String indent = leadingWhitespace(lines.get(maxPriceLineIndex));
                changed |= syncCommentBlockBeforeLine(
                        lines,
                        "MAX_PRICE_EACH:",
                        List.of(indent + "# Maximum price for one requested item. Default matches MAX_TOTAL_BUDGET so the total escrow cap is the main limit."),
                        this::isOrdersPricingComment
                );

                maxPriceLineIndex = findConfigLine(lines, "MAX_PRICE_EACH:");
                if (maxPriceLineIndex >= 0 && isOldDefaultMaxPrice(lines.get(maxPriceLineIndex))) {
                    lines.set(maxPriceLineIndex, leadingWhitespace(lines.get(maxPriceLineIndex)) + "MAX_PRICE_EACH: 250000000");
                    changed = true;
                }
            }

            int maxTotalLineIndex = findConfigLine(lines, "MAX_TOTAL_BUDGET:");
            if (maxTotalLineIndex >= 0) {
                String indent = leadingWhitespace(lines.get(maxTotalLineIndex));
                changed |= syncCommentBlockBeforeLine(
                        lines,
                        "MAX_TOTAL_BUDGET:",
                        List.of(indent + "# Maximum total escrow budget for one order after quantity x price each."),
                        this::isOrdersPricingComment
                );
            }

            if (!changed) {
                return false;
            }

            if (!alreadyBackedUp) {
                backupExistingFile(targetFile, backupDirectory);
            }
            Files.write(targetFile.toPath(), lines, StandardCharsets.UTF_8);
            plugin.getLogger().info("Updated orders.yml pricing defaults/comment tags.");
            return true;
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to sync orders.yml pricing defaults/comment tags.", e);
            return false;
        }
    }

    private boolean syncRtpSearchDefaultsAndComments(String resourceName, File targetFile, File backupDirectory, boolean alreadyBackedUp) {
        if (!"rtp.yml".equals(resourceName)) {
            return false;
        }

        try {
            List<String> lines = Files.readAllLines(targetFile.toPath(), StandardCharsets.UTF_8);
            boolean changed = syncRtpSearchDefaultsAndComments(lines);

            if (!changed) {
                return false;
            }

            if (!alreadyBackedUp) {
                backupExistingFile(targetFile, backupDirectory);
            }
            Files.write(targetFile.toPath(), lines, StandardCharsets.UTF_8);
            plugin.getLogger().info("Updated rtp.yml search defaults/comment tags.");
            return true;
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to sync rtp.yml search defaults/comment tags.", e);
            return false;
        }
    }

    private boolean syncRtpSearchDefaultsAndComments(List<String> lines) {
        boolean changed = false;

        changed |= syncRtpComment(lines, "PLAYERS-IN-RTP:",
                "# Max players searching or waiting for RTP simultaneously. Values below 1 are treated as 1.");
        changed |= syncLegacyScalarDefault(lines, "PLAYERS-IN-RTP:", "1", List.of("0"));

        changed |= syncRtpComment(lines, "MAX-ATTEMPTS:",
                "# Max tries to find a valid RTP location before failing. Values below 32 use 32.");
        changed |= syncLegacyScalarDefault(lines, "MAX-ATTEMPTS:", "64", List.of("0", "1", "16"));

        changed |= syncRtpComment(lines, "MAX-CHUNK-SAMPLES:",
                "# Max chunk samples to inspect while looking for a valid location. Values below 64 use 64.");
        changed |= syncLegacyScalarDefault(lines, "MAX-CHUNK-SAMPLES:", "128", List.of("0", "1"));

        changed |= syncRtpComment(lines, "ATTEMPT-INTERVAL-TICKS:",
                "# Ticks between chunk samples. Higher values reduce load but make RTP slower. Values below 8 use 8.");
        changed |= syncLegacyScalarDefault(lines, "ATTEMPT-INTERVAL-TICKS:", "8", List.of("1", "2", "4"));

        changed |= syncRtpComment(lines, "GENERATE-CHUNKS:",
                "# Generate new chunks while searching. Keep false for pregenerated RTP worlds to protect TPS.");
        changed |= syncRtpSettingDefaultAndComment(
                lines,
                "GENERATE-FALLBACK-CHUNKS:",
                "true",
                "# Generate a limited number of chunks only after pregenerated/loaded RTP search cannot find a safe spot.",
                "GENERATE-CHUNKS:"
        );
        changed |= syncRtpSettingDefaultAndComment(
                lines,
                "GENERATE-FALLBACK-AFTER-SAMPLES:",
                "48",
                "# Chunk samples to try before limited fallback generation starts.",
                "GENERATE-FALLBACK-CHUNKS:"
        );
        changed |= syncRtpSettingDefaultAndComment(
                lines,
                "MAX-GENERATE-FALLBACK-SAMPLES:",
                "32",
                "# Maximum fallback chunks allowed to generate during one RTP search. Set 0 to disable generation fallback.",
                "GENERATE-FALLBACK-AFTER-SAMPLES:"
        );

        changed |= syncRtpComment(lines, "LOAD-GENERATED-CHUNKS:",
                "# If chunk generation is disabled, allow loading already-generated chunks from disk.");
        changed |= syncRtpComment(lines, "FALLBACK-TO-LOADED-CHUNKS:",
                "# If random samples cannot be prepared, try already-loaded chunks as a fallback.");
        changed |= syncRtpComment(lines, "PRELOAD-TELEPORT-CHUNKS:",
                "# Preload generated chunks around the RTP destination before teleporting to reduce post-teleport ping spikes.");
        changed |= syncRtpComment(lines, "PRELOAD-RADIUS:",
                "# Chunk radius to preload around the destination. Values are clamped between 0 and 3.");
        changed |= syncRtpComment(lines, "PRELOAD-CHUNKS-PER-TICK:",
                "# How many destination chunks to preload per tick.");
        changed |= syncRtpComment(lines, "PRELOAD-MAX-TICKS:",
                "# Maximum ticks to spend preloading before teleport continues anyway.");
        changed |= syncRtpComment(lines, "POST-TELEPORT-CHUNK-THROTTLE:",
                "# Temporarily lower player chunk send distance after RTP to avoid a large client chunk burst.");
        changed |= syncRtpComment(lines, "POST-TELEPORT-VIEW-DISTANCE:",
                "# Temporary per-player view distance after RTP. Values below 2 use 2.");
        changed |= syncRtpComment(lines, "POST-TELEPORT-SIMULATION-DISTANCE:",
                "# Temporary per-player simulation distance after RTP. Values below 2 use 2.");
        changed |= syncRtpComment(lines, "POST-TELEPORT-THROTTLE-TICKS:",
                "# Ticks before the player's original view/simulation distance is restored after RTP.");

        int maxAttemptsMessageIndex = findConfigLineInSection(lines, "MESSAGES:", "MAX-ATTEMPTS:");
        if (maxAttemptsMessageIndex >= 0) {
            String indent = leadingWhitespace(lines.get(maxAttemptsMessageIndex));
            String desiredLine = indent
                    + "MAX-ATTEMPTS: \"&cCould not find a safe RTP location. &7Attempts: &f{attempts}/{max_attempts} &8| &7Samples: &f{samples}/{max_samples}\"";
            String existingBlock = collectYamlScalarBlock(lines, maxAttemptsMessageIndex);
            if (!existingBlock.contains("{samples}") || !lines.get(maxAttemptsMessageIndex).equals(desiredLine)) {
                setConfigLineAndRemoveContinuations(lines, maxAttemptsMessageIndex, desiredLine);
                changed = true;
            }
        }

        return changed;
    }

    private boolean syncCommentBlockBeforeLine(
            List<String> lines,
            String keyPrefix,
            List<String> desiredComments,
            java.util.function.Predicate<String> managedCommentPredicate
    ) {
        int keyLineIndex = findConfigLine(lines, keyPrefix);
        if (keyLineIndex < 0) {
            return false;
        }

        int commentStart = keyLineIndex;
        while (commentStart > 0 && lines.get(commentStart - 1).trim().startsWith("#")) {
            commentStart--;
        }

        List<String> existingComments = new ArrayList<>(lines.subList(commentStart, keyLineIndex));
        if (existingComments.equals(desiredComments)) {
            return false;
        }

        boolean replaceExistingComments = existingComments.isEmpty()
                || existingComments.stream().anyMatch(managedCommentPredicate);
        int copyUntil = replaceExistingComments ? commentStart : keyLineIndex;

        List<String> updatedLines = new ArrayList<>(lines.size() + desiredComments.size());
        updatedLines.addAll(lines.subList(0, copyUntil));
        updatedLines.addAll(desiredComments);
        updatedLines.addAll(lines.subList(keyLineIndex, lines.size()));

        lines.clear();
        lines.addAll(updatedLines);
        return true;
    }

    private boolean syncRtpComment(List<String> lines, String keyPrefix, String comment) {
        int keyLineIndex = findConfigLine(lines, keyPrefix);
        if (keyLineIndex < 0) {
            return false;
        }
        String indent = leadingWhitespace(lines.get(keyLineIndex));
        return syncCommentBlockBeforeLine(
                lines,
                keyPrefix,
                List.of(indent + comment),
                this::isRtpSearchComment
        );
    }

    private boolean syncRtpSettingDefaultAndComment(
            List<String> lines,
            String keyPrefix,
            String defaultValue,
            String comment,
            String insertAfterKeyPrefix
    ) {
        int keyLineIndex = findConfigLine(lines, keyPrefix);
        if (keyLineIndex >= 0) {
            return syncRtpComment(lines, keyPrefix, comment);
        }

        int anchorLineIndex = findConfigLine(lines, insertAfterKeyPrefix);
        if (anchorLineIndex < 0) {
            return false;
        }

        String indent = leadingWhitespace(lines.get(anchorLineIndex));
        int insertAt = anchorLineIndex + 1;
        lines.add(insertAt, indent + comment);
        lines.add(insertAt + 1, indent + keyPrefix + " " + defaultValue);
        return true;
    }

    private boolean syncLegacyScalarDefault(
            List<String> lines,
            String keyPrefix,
            String desiredValue,
            List<String> legacyValues
    ) {
        int keyLineIndex = findConfigLine(lines, keyPrefix);
        if (keyLineIndex < 0) {
            return false;
        }

        String currentValue = normalizeYamlScalarValue(lines.get(keyLineIndex), keyPrefix);
        if (!legacyValues.contains(currentValue) || desiredValue.equals(currentValue)) {
            return false;
        }

        lines.set(keyLineIndex, leadingWhitespace(lines.get(keyLineIndex)) + keyPrefix + " " + desiredValue);
        return true;
    }

    private String normalizeYamlScalarValue(String line, String keyPrefix) {
        String trimmed = line == null ? "" : line.trim();
        if (!trimmed.startsWith(keyPrefix)) {
            return "";
        }

        String value = trimmed.substring(keyPrefix.length()).trim();
        int commentIndex = value.indexOf('#');
        if (commentIndex >= 0) {
            value = value.substring(0, commentIndex).trim();
        }
        if ((value.startsWith("\"") && value.endsWith("\""))
                || (value.startsWith("'") && value.endsWith("'"))) {
            value = value.substring(1, value.length() - 1);
        }
        return value.trim().toLowerCase(java.util.Locale.ROOT);
    }

    private int findConfigLineInSection(List<String> lines, String sectionPrefix, String keyPrefix) {
        int sectionLineIndex = findConfigLine(lines, sectionPrefix);
        if (sectionLineIndex < 0) {
            return -1;
        }

        int sectionIndent = leadingWhitespace(lines.get(sectionLineIndex)).length();
        for (int index = sectionLineIndex + 1; index < lines.size(); index++) {
            String line = lines.get(index);
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }

            int indent = leadingWhitespace(line).length();
            if (indent <= sectionIndent) {
                return -1;
            }
            if (trimmed.startsWith(keyPrefix)) {
                return index;
            }
        }
        return -1;
    }

    private String collectYamlScalarBlock(List<String> lines, int keyLineIndex) {
        StringBuilder block = new StringBuilder(lines.get(keyLineIndex));
        int baseIndent = leadingWhitespace(lines.get(keyLineIndex)).length();
        for (int index = keyLineIndex + 1; index < lines.size(); index++) {
            String line = lines.get(index);
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#") || leadingWhitespace(line).length() <= baseIndent) {
                break;
            }
            block.append('\n').append(line);
        }
        return block.toString();
    }

    private void setConfigLineAndRemoveContinuations(List<String> lines, int keyLineIndex, String desiredLine) {
        lines.set(keyLineIndex, desiredLine);
        int baseIndent = leadingWhitespace(desiredLine).length();
        int index = keyLineIndex + 1;
        while (index < lines.size()) {
            String line = lines.get(index);
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#") || leadingWhitespace(line).length() <= baseIndent) {
                break;
            }
            lines.remove(index);
        }
    }

    private boolean isOldDefaultMaxPrice(String line) {
        String trimmed = line == null ? "" : line.trim();
        if (!trimmed.startsWith("MAX_PRICE_EACH:")) {
            return false;
        }

        String value = trimmed.substring("MAX_PRICE_EACH:".length()).trim();
        int commentIndex = value.indexOf('#');
        if (commentIndex >= 0) {
            value = value.substring(0, commentIndex).trim();
        }
        value = value.replace("_", "").replace(",", "");
        return "1000000".equals(value) || "1000000.0".equals(value);
    }

    private boolean isOrdersPricingComment(String line) {
        String comment = line.trim();
        return comment.startsWith("#")
                && (comment.contains("MAX_PRICE_EACH")
                || comment.contains("MAX_TOTAL_BUDGET")
                || comment.contains("Maximum price")
                || comment.contains("Maximum total escrow")
                || comment.contains("total escrow cap")
                || comment.contains("quantity x price"));
    }

    private boolean isRtpSearchComment(String line) {
        String comment = line.trim();
        return comment.startsWith("#")
                && (comment.contains("RTP countdown")
                || comment.contains("searching or waiting for RTP")
                || comment.contains("Max players")
                || comment.contains("Max tries")
                || comment.contains("Max chunk samples")
                || comment.contains("Ticks between chunk samples")
                || comment.contains("Generate new chunks")
                || comment.contains("limited number of chunks")
                || comment.contains("limited fallback generation")
                || comment.contains("fallback chunks")
                || comment.contains("chunk generation is disabled")
                || comment.contains("already-generated chunks")
                || comment.contains("already-loaded chunks")
                || comment.contains("Preload generated chunks")
                || comment.contains("Chunk radius to preload")
                || comment.contains("destination chunks")
                || comment.contains("Maximum ticks to spend preloading")
                || comment.contains("player chunk send distance")
                || comment.contains("per-player view distance")
                || comment.contains("per-player simulation distance")
                || comment.contains("original view/simulation distance")
                || comment.contains("Values below")
                || comment.contains("safe default")
                || comment.contains("Set 0"));
    }

    private int findConfigLine(List<String> lines, String keyPrefix) {
        for (int index = 0; index < lines.size(); index++) {
            if (lines.get(index).trim().startsWith(keyPrefix)) {
                return index;
            }
        }
        return -1;
    }

    private int findTopLevelConfigLine(List<String> lines, String keyPrefix) {
        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index);
            String trimmed = line.trim();
            if (leadingWhitespace(line).isEmpty()
                    && (trimmed.equals(keyPrefix) || trimmed.startsWith(keyPrefix + " "))) {
                return index;
            }
        }
        return -1;
    }

    private int findTopLevelBlockEnd(List<String> lines, int startIndex) {
        for (int index = startIndex + 1; index < lines.size(); index++) {
            String line = lines.get(index);
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }
            if (leadingWhitespace(line).isEmpty() && trimmed.contains(":")) {
                return index;
            }
        }
        return lines.size();
    }

    private boolean isCrashProtectionComment(String line) {
        String comment = line.trim();
        return isSetupComment(line)
                || comment.startsWith("#")
                && (comment.contains("Crash protection")
                || comment.contains("unsafe item metadata")
                || comment.contains("UDS storage")
                || comment.contains("MAX-SERIALIZED-BYTES")
                || comment.contains("production use"));
    }

    private boolean isSetupComment(String line) {
        return line != null && line.trim().startsWith(SETUP_COMMENT_PREFIX);
    }

    private boolean isItemSelectionModeComment(String line) {
        String comment = line.trim();
        return comment.startsWith("#")
                && (comment.contains("SELECT_ITEM")
                || comment.contains("INVENTORY_ITEM")
                || comment.contains("SEARCH_ITEM")
                || comment.contains("ITEM_SELECTION_MODE")
                || comment.contains("Invalid values")
                || comment.contains("Valid values"));
    }

    private boolean isSelectItemSourceComment(String line) {
        String comment = line.trim();
        return comment.startsWith("#")
                && (comment.contains("SELECT_ITEM_SOURCE")
                || comment.contains("CATEGORY_FILTERS")
                || comment.contains("SERVER_MATERIALS")
                || comment.contains("server.jar")
                || comment.contains("catalog source")
                || comment.contains("SELECT_ITEM menu contents")
                || comment.contains("Valid values"));
    }

    private String leadingWhitespace(String line) {
        int index = 0;
        while (index < line.length() && Character.isWhitespace(line.charAt(index))) {
            index++;
        }
        return line.substring(0, index);
    }

    private int mergeBundledDefaults(
            String resourceName,
            YamlConfiguration current,
            YamlConfiguration bundledDefault,
            YamlConfiguration previousDefault
    ) {
        int changes = 0;

        for (String path : bundledDefault.getKeys(true)) {
            if (isUserManagedBundledPath(resourceName, path)) {
                continue;
            }

            if (bundledDefault.isConfigurationSection(path)) {
                if (!current.contains(path, true) && !hasScalarParent(current, path)) {
                    current.createSection(path);
                    changes++;
                }
                continue;
            }

            if (!current.contains(path, true)) {
                if (!hasScalarParent(current, path)) {
                    current.set(path, copyConfigValue(bundledDefault.get(path)));
                    changes++;
                }
                continue;
            }

            if (previousDefault == null
                    || !previousDefault.contains(path, true)
                    || previousDefault.isConfigurationSection(path)) {
                continue;
            }

            Object currentValue = current.get(path);
            Object previousValue = previousDefault.get(path);
            Object bundledValue = bundledDefault.get(path);

            if (valuesEquivalent(currentValue, previousValue)
                    && !valuesEquivalent(currentValue, bundledValue)) {
                current.set(path, copyConfigValue(bundledValue));
                changes++;
            }
        }

        return changes;
    }

    private boolean isUserManagedBundledPath(String resourceName, String path) {
        // Crate definitions are live server content. The bundled CRATES tree is only
        // an initial example and must not be merged back after admins edit/delete it.
        return "crates.yml".equals(resourceName) && path.startsWith("CRATES.");
    }

    private boolean hasScalarParent(ConfigurationSection configuration, String path) {
        int dotIndex = path.indexOf('.');
        while (dotIndex > 0) {
            String parentPath = path.substring(0, dotIndex);
            if (configuration.contains(parentPath, true)
                    && !configuration.isConfigurationSection(parentPath)) {
                return true;
            }
            dotIndex = path.indexOf('.', dotIndex + 1);
        }
        return false;
    }

    private YamlConfiguration loadPreviousDefaultSnapshot(String name) {
        File snapshot = new File(defaultSnapshotsFolder(), name);
        if (!snapshot.exists()) {
            return null;
        }

        try {
            return loadYamlFile(snapshot);
        } catch (IOException | InvalidConfigurationException e) {
            plugin.getLogger().log(Level.WARNING, "Ignoring invalid default configuration snapshot: " + snapshot.getPath(), e);
            return null;
        }
    }

    private boolean refreshDefaultSnapshot(String name) {
        byte[] bundledBytes;
        try {
            bundledBytes = readBundledResourceBytes(name);
        } catch (IOException | IllegalArgumentException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to read bundled configuration snapshot for " + name, e);
            return false;
        }

        File snapshot = new File(defaultSnapshotsFolder(), name);
        try {
            if (snapshot.exists()) {
                byte[] existingBytes = Files.readAllBytes(snapshot.toPath());
                if (Arrays.equals(existingBytes, bundledBytes)) {
                    return false;
                }
            }

            Files.createDirectories(snapshot.getParentFile().toPath());
            Files.write(snapshot.toPath(), bundledBytes);
            return true;
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to refresh default configuration snapshot: " + snapshot.getPath(), e);
            return false;
        }
    }

    private File defaultSnapshotsFolder() {
        return new File(plugin.getDataFolder(), ".default-configs");
    }

    private YamlConfiguration loadBundledYaml(String name) throws IOException, InvalidConfigurationException {
        try (InputStream input = plugin.getResource(name)) {
            if (input == null) {
                throw new IllegalArgumentException("Resource not found in jar: " + name);
            }

            try (Reader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
                YamlConfiguration configuration = new YamlConfiguration();
                configuration.options().parseComments(true);
                configuration.load(reader);
                return configuration;
            }
        }
    }

    private YamlConfiguration loadYamlFile(File file) throws IOException, InvalidConfigurationException {
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.options().parseComments(true);
        configuration.load(file);
        return configuration;
    }

    private boolean copyBundledResource(String name, File target, boolean replace) {
        try (InputStream input = plugin.getResource(name)) {
            if (input == null) {
                plugin.getLogger().warning("Resource not found in jar: " + name);
                return false;
            }

            Files.createDirectories(target.getParentFile().toPath());
            if (replace) {
                Files.copy(input, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } else {
                Files.copy(input, target.toPath());
            }
            return true;
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to copy bundled resource " + name + " to " + target.getPath(), e);
            return false;
        }
    }

    private byte[] readBundledResourceBytes(String name) throws IOException {
        try (InputStream input = plugin.getResource(name)) {
            if (input == null) {
                throw new IllegalArgumentException("Resource not found in jar: " + name);
            }
            return input.readAllBytes();
        }
    }

    private void backupExistingFile(File file, File backupDirectory) {
        if (!file.exists()) {
            return;
        }

        File backup = new File(backupDirectory, file.getName());
        try {
            Files.createDirectories(backupDirectory.toPath());
            Files.copy(file.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to back up " + file.getPath(), e);
        }
    }

    private Object copyConfigValue(Object value) {
        if (value instanceof List<?> list) {
            List<Object> copy = new ArrayList<>(list.size());
            for (Object entry : list) {
                copy.add(copyConfigValue(entry));
            }
            return copy;
        }

        if (value instanceof Map<?, ?> map) {
            Map<Object, Object> copy = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                copy.put(entry.getKey(), copyConfigValue(entry.getValue()));
            }
            return copy;
        }

        return value;
    }

    private boolean valuesEquivalent(Object first, Object second) {
        return Objects.equals(first, second);
    }

    private FileConfiguration load(String name) {
        File file = new File(plugin.getDataFolder(), name);
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.options().parseComments(true);

        try {
            configuration.load(file);
            return configuration;
        } catch (IOException | InvalidConfigurationException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load " + file.getPath() + ", restoring default copy.", e);
            backupBrokenFile(file);

            try {
                copyBundledResource(name, file, true);
                configuration.load(file);
            } catch (IOException | InvalidConfigurationException | IllegalArgumentException restoreException) {
                plugin.getLogger().log(Level.SEVERE, "Failed to restore default resource " + name, restoreException);
            }
            return configuration;
        }
    }

    private void backupBrokenFile(File file) {
        if (!file.exists()) {
            return;
        }

        File backup = new File(file.getParentFile(), file.getName() + ".broken");
        try {
            Files.copy(file.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "ꜰᴀɪʟᴇᴅ ᴛᴏ ʙᴀᴄᴋ ᴜᴘ ʙʀᴏᴋᴇɴ ꜰɪʟᴇ " + file.getPath(), e);
        }
    }

    // ── Getters ────────────────────────────────────────────────────────────────

    public FileConfiguration getConfig()        { return config; }
    public FileConfiguration getMessages()      { return messages; }
    public FileConfiguration getDeathMessages() { return deathMessages; }
    public FileConfiguration getMenus()         { return menus; }
    public FileConfiguration getScoreboard()    { return scoreboard; }
    public FileConfiguration getShop()          { return shop; }
    public FileConfiguration getSounds()        { return sounds; }
    public FileConfiguration getBillford()      { return billford; }
    public FileConfiguration getRtp()           { return rtp; }
    public FileConfiguration getWorth()         { return worth; }
    public FileConfiguration getAmethystTools() { return amethystTools; }
    public FileConfiguration getEnderChest()    { return enderChest; }
    public FileConfiguration getInvsee()        { return invsee; }
    public FileConfiguration getFreeze()        { return freeze; }
    public FileConfiguration getAuctionHouse()  { return auctionHouse; }
    public FileConfiguration getOrders()        { return orders; }
    public FileConfiguration getDuels()         { return duels; }
    public FileConfiguration getFfa()           { return ffa; }
    public FileConfiguration getCrates()        { return crates; }
    public FileConfiguration getSpawners()      { return spawners; }
    public FileConfiguration getSpawnStash()    { return spawnStash; }
    public FileConfiguration getNetwork()       { return network; }
    public FileConfiguration getStaffMode()     { return staffMode; }
    public FileConfiguration getDatabase()      { return database; }
    public FileConfiguration getDiscord()       { return discord; }

    public void reloadShop() { shop = load("shop.yml"); }
    public void reloadMenus() { menus = load("menus.yml"); }
    public void reloadSounds() { sounds = load("sounds.yml"); }
    public void reloadWorth() { worth = load("worth.yml"); }
    public void reloadAmethystTools() { amethystTools = load("amethyst-tools.yml"); }
    public void reloadEnderChest() { enderChest = load("ender-chest.yml"); }
    public void reloadInvsee() { invsee = load("invsee.yml"); }
    public void reloadFreeze() { freeze = load("freeze.yml"); }
    public void reloadAuctionHouse() { auctionHouse = load("auction-house.yml"); }
    public void reloadOrders() { orders = load("orders.yml"); }
    public void reloadDuels() { duels = load("duels.yml"); }
    public void reloadFfa() { ffa = load("ffa.yml"); }
    public void reloadCrates() { crates = load("crates.yml"); }
    public void reloadSpawners() { spawners = load("spawners.yml"); }
    public void reloadSpawnStash() { spawnStash = load("spawn-stash.yml"); }
    public void reloadNetwork() { network = load("network.yml"); }
    public void reloadStaffMode() { staffMode = load("staff-mode.yml"); }
    public void reloadDatabase() { database = load("database.yml"); }
    public void reloadDiscord() { discord = load("discord.yml"); }
    public boolean saveDuels() { return save("duels.yml", duels); }
    public boolean saveFfa() { return save("ffa.yml", ffa); }
    public boolean saveCrates() { return save("crates.yml", crates); }
    public boolean saveMenus() { return save("menus.yml", menus); }
    public boolean saveDatabase() { return save("database.yml", database); }
    public boolean saveNetwork() { return save("network.yml", network); }
    public boolean saveDiscord() { return save("discord.yml", discord); }

    // ── Convenience helpers ────────────────────────────────────────────────────

    public String getMessage(String path) {
        return messages.getString(path, "&cᴍᴇѕѕᴀɢᴇ ɴᴏᴛ ꜰᴏᴜɴᴅ: " + path);
    }

    public String getMessage(String path, String... placeholders) {
        String msg = getMessage(path);
        for (int i = 0; i + 1 < placeholders.length; i += 2) {
            msg = msg.replace(placeholders[i], placeholders[i + 1]);
        }
        return msg;
    }

    public String getMessageOrDefault(String path, String fallback) {
        return messages.getString(path, fallback);
    }

    public String getMessageOrDefault(String path, String fallback, String... placeholders) {
        String msg = getMessageOrDefault(path, fallback);
        for (int i = 0; i + 1 < placeholders.length; i += 2) {
            msg = msg.replace(placeholders[i], placeholders[i + 1]);
        }
        return msg;
    }

    public String getSound(String path) {
        return sounds.getString(path, "");
    }

    public boolean isCommandEnabled(String key) {
        return FeatureManager.isCommandEnabled(config, key);
    }

    private boolean save(String name, FileConfiguration configuration) {
        if (configuration == null) {
            return false;
        }

        File file = new File(plugin.getDataFolder(), name);
        try {
            configuration.save(file);
            return true;
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save " + file.getPath(), e);
        }
        return false;
    }

    private static final class SyncResult {
        private boolean created;
        private boolean updated;
        private boolean restored;
        private boolean skipped;
        private boolean snapshotUpdated;
    }
}
