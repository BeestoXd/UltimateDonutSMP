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
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
            "enchantments.yml",
            "filter.yml",
            "duels.yml",
            "ffa.yml",
            "crates.yml",
            "spawners.yml",
            "spawn-stash.yml",
            "network.yml",
            "staff-mode.yml",
            "hide.yml",
            "database.yml",
            "server-wipe.yml",
            "discord.yml",
            "anvil-moderation.yml"
    );

    private static final DateTimeFormatter BACKUP_TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");
    private static final String SETUP_COMMENT_PREFIX = "# UDS setup:";

    private final UltimateDonutSmp plugin;
    private final Set<String> invalidConfigurations = new HashSet<>();

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
    private FileConfiguration hide;
    private FileConfiguration database;
    private FileConfiguration serverWipe;
    private FileConfiguration discord;
    private FileConfiguration anvilModeration;
    private FileConfiguration enchantments;
    private FileConfiguration filter;

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
        config       = load("config.yml", config);
        messages     = load("messages.yml", messages);
        deathMessages= load("death-messages.yml", deathMessages);
        menus        = load("menus.yml", menus);
        scoreboard   = load("scoreboard.yml", scoreboard);
        shop         = load("shop.yml", shop);
        sounds       = load("sounds.yml", sounds);
        billford     = load("billford.yml", billford);
        rtp          = load("rtp.yml", rtp);
        worth        = load("worth.yml", worth);
        amethystTools = load("amethyst-tools.yml", amethystTools);
        enderChest   = load("ender-chest.yml", enderChest);
        invsee       = load("invsee.yml", invsee);
        freeze       = load("freeze.yml", freeze);
        auctionHouse = load("auction-house.yml", auctionHouse);
        orders       = load("orders.yml", orders);
        duels        = load("duels.yml", duels);
        ffa          = load("ffa.yml", ffa);
        crates       = load("crates.yml", crates);
        spawners     = load("spawners.yml", spawners);
        spawnStash   = load("spawn-stash.yml", spawnStash);
        network      = load("network.yml", network);
        staffMode    = load("staff-mode.yml", staffMode);
        hide         = load("hide.yml", hide);
        database     = load("database.yml", database);
        serverWipe   = load("server-wipe.yml", serverWipe);
        discord      = load("discord.yml", discord);
        anvilModeration = load("anvil-moderation.yml", anvilModeration);
        enchantments = load("enchantments.yml", enchantments);
        filter       = load("filter.yml", filter);
    }

    private void syncBundledConfigurations() {
        File backupDirectory = new File(
                new File(plugin.getDataFolder(), "config-backups"),
                LocalDateTime.now().format(BACKUP_TIMESTAMP_FORMAT)
        );

        int created = 0;
        int updated = 0;
        int skipped = 0;

        for (String name : CONFIGURATION_RESOURCES) {
            SyncResult result = syncBundledConfiguration(name, backupDirectory);
            if (result.created) {
                created++;
            }
            if (result.updated) {
                updated++;
            }
            if (result.skipped) {
                skipped++;
            }
        }

        plugin.getLogger().info("Configuration sync complete: "
                + created + " created, "
                + updated + " updated, "
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
            } else {
                result.skipped = true;
            }
            return result;
        }

        YamlConfiguration current;
        try {
            current = loadYamlFile(targetFile);
        } catch (IOException | InvalidConfigurationException e) {
            invalidConfigurations.add(name);
            result.skipped = true;
            plugin.getLogger().log(Level.SEVERE,
                    "Skipping configuration sync for invalid YAML without replacing the original file: "
                            + targetFile.getPath(),
                    e);
            backupExistingFile(targetFile, backupDirectory);
            return result;
        }

        TextFileContent currentText;
        try {
            currentText = readTextFile(targetFile);
        } catch (IOException e) {
            result.skipped = true;
            plugin.getLogger().log(Level.WARNING, "Failed to read configuration for line-preserving sync: "
                    + targetFile.getPath(), e);
            return result;
        }

        int mergedPaths = mergeBundledDefaults(name, currentText.lines(), current, bundledDefault);
        if (mergedPaths == 0) {
            return result;
        }

        try {
            validateYamlLines(currentText.lines());
            if (!backupExistingFile(targetFile, backupDirectory)) {
                result.skipped = true;
                plugin.getLogger().warning("Skipped configuration sync because backup creation failed: "
                        + targetFile.getPath());
                return result;
            }
            writeTextFileAtomically(targetFile, currentText);
            result.updated = true;
            plugin.getLogger().info("Added " + mergedPaths + " missing bundled default path(s) to " + name + ".");
        } catch (IOException | InvalidConfigurationException e) {
            result.skipped = true;
            plugin.getLogger().log(Level.WARNING, "Failed to save synced configuration " + targetFile.getPath(), e);
        }
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

    private boolean syncBundledInlineComments(String resourceName, File targetFile, File backupDirectory, boolean alreadyBackedUp) {
        try {
            List<String> bundledLines = readBundledResourceLines(resourceName);
            List<String> lines = Files.readAllLines(targetFile.toPath(), StandardCharsets.UTF_8);
            boolean changed = syncBundledInlineComments(lines, bundledLines, resourceName);

            if (!changed) {
                return false;
            }

            validateYamlLines(lines);
            if (!alreadyBackedUp) {
                backupExistingFile(targetFile, backupDirectory);
            }
            Files.write(targetFile.toPath(), lines, StandardCharsets.UTF_8);
            plugin.getLogger().info("Updated " + resourceName + " inline option comment tags.");
            return true;
        } catch (IOException | InvalidConfigurationException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to sync inline option comments for " + resourceName + ".", e);
            return false;
        }
    }

    private boolean syncBundledInlineComments(List<String> lines, List<String> bundledLines, String resourceName) {
        Map<String, YamlPathLine> bundledLineIndex = indexYamlPathLines(bundledLines);
        Map<String, YamlPathLine> currentLineIndex = indexYamlPathLines(lines);
        boolean changed = false;

        for (Map.Entry<String, YamlPathLine> entry : bundledLineIndex.entrySet()) {
            String path = entry.getKey();
            YamlPathLine bundledNode = entry.getValue();
            if (bundledNode.sectionSyntax || isUserManagedBundledPath(resourceName, path)) {
                continue;
            }

            YamlPathLine currentNode = currentLineIndex.get(path);
            if (currentNode == null || currentNode.sectionSyntax) {
                continue;
            }

            String bundledComment = yamlInlineComment(bundledLines.get(bundledNode.lineIndex));
            if (bundledComment.isBlank()) {
                continue;
            }

            String currentLine = lines.get(currentNode.lineIndex);
            if (!canPlaceYamlInlineComment(currentLine)) {
                String cleanedLine = removeManagedInlineCommentFromQuotedValue(currentLine, bundledComment);
                if (!cleanedLine.equals(currentLine)) {
                    lines.set(currentNode.lineIndex, cleanedLine);
                    changed = true;
                }
                continue;
            }

            String updatedLine = replaceYamlInlineComment(currentLine, bundledComment);
            if (!updatedLine.equals(currentLine)) {
                lines.set(currentNode.lineIndex, updatedLine);
                changed = true;
            }
        }

        return changed;
    }

    private boolean syncBundledTopLevelOrder(String resourceName, File targetFile, File backupDirectory, boolean alreadyBackedUp) {
        try {
            List<String> bundledLines = readBundledResourceLines(resourceName);
            List<String> lines = Files.readAllLines(targetFile.toPath(), StandardCharsets.UTF_8);
            boolean changed = syncBundledTopLevelOrder(lines, bundledLines);

            if (!changed) {
                return false;
            }

            validateYamlLines(lines);
            if (!alreadyBackedUp) {
                backupExistingFile(targetFile, backupDirectory);
            }
            Files.write(targetFile.toPath(), lines, StandardCharsets.UTF_8);
            plugin.getLogger().info("Updated " + resourceName + " top-level section order.");
            return true;
        } catch (IOException | InvalidConfigurationException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to sync top-level section order for " + resourceName + ".", e);
            return false;
        }
    }

    private boolean syncBundledTopLevelOrder(List<String> lines, List<String> bundledLines) {
        Map<String, YamlPathLine> bundledIndex = indexYamlPathLines(bundledLines);
        Map<String, YamlPathLine> currentIndex = indexYamlPathLines(lines);
        List<String> bundledTopLevel = bundledIndex.values().stream()
                .filter(node -> parentPath(node.path).isEmpty())
                .map(YamlPathLine::path)
                .toList();
        if (bundledTopLevel.isEmpty()) {
            return false;
        }

        List<YamlPathLine> currentTopLevel = currentIndex.values().stream()
                .filter(node -> parentPath(node.path).isEmpty())
                .sorted((first, second) -> Integer.compare(first.lineIndex, second.lineIndex))
                .toList();
        if (currentTopLevel.isEmpty()) {
            return false;
        }

        int firstBlockStart = attachedCommentStart(lines, currentTopLevel.get(0).lineIndex);
        List<String> reordered = new ArrayList<>(lines.subList(0, firstBlockStart));
        Set<String> appended = new HashSet<>();

        for (String path : bundledTopLevel) {
            YamlPathLine node = currentIndex.get(path);
            if (node != null && parentPath(node.path).isEmpty()) {
                appendYamlTopLevelBlock(reordered, extractYamlNodeBlock(lines, node, true));
                appended.add(path);
            }
        }

        for (YamlPathLine node : currentTopLevel) {
            if (!appended.contains(node.path)) {
                appendYamlTopLevelBlock(reordered, extractYamlNodeBlock(lines, node, true));
            }
        }

        List<String> trimmed = trimTrailingBlankLines(reordered);
        if (trimmed.equals(lines)) {
            return false;
        }

        lines.clear();
        lines.addAll(trimmed);
        return true;
    }

    private List<String> readBundledResourceLines(String name) throws IOException {
        String content = new String(readBundledResourceBytes(name), StandardCharsets.UTF_8);
        return new ArrayList<>(Arrays.asList(content.split("\\R", -1)));
    }

    private TextFileContent readTextFile(File file) throws IOException {
        String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        String lineSeparator = detectLineSeparator(content);
        boolean trailingLineSeparator = content.endsWith("\r\n")
                || content.endsWith("\n")
                || content.endsWith("\r");
        List<String> lines = new ArrayList<>(Arrays.asList(content.split("\\r\\n|\\n|\\r", -1)));
        if (trailingLineSeparator && !lines.isEmpty()) {
            lines.remove(lines.size() - 1);
        }
        return new TextFileContent(lines, lineSeparator, trailingLineSeparator);
    }

    private String detectLineSeparator(String content) {
        int crlf = content.indexOf("\r\n");
        int lf = content.indexOf('\n');
        int cr = content.indexOf('\r');
        if (crlf >= 0 && (lf < 0 || crlf <= lf) && (cr < 0 || crlf <= cr)) {
            return "\r\n";
        }
        if (lf >= 0 && (cr < 0 || lf < cr)) {
            return "\n";
        }
        if (cr >= 0) {
            return "\r";
        }
        return System.lineSeparator();
    }

    private void writeTextFileAtomically(File file, TextFileContent content) throws IOException {
        Path target = file.toPath();
        Path parent = target.getParent();
        Files.createDirectories(parent);
        Path temporary = Files.createTempFile(parent, "." + file.getName() + ".", ".tmp");
        try {
            Files.writeString(temporary, content.serialize(), StandardCharsets.UTF_8);
            Files.move(
                    temporary,
                    target,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING
            );
        } finally {
            Files.deleteIfExists(temporary);
        }
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
                    + "MAX-ATTEMPTS: \"&ccould not find a safe rtp location. &7attempts: &f{attempts}/{max_attempts} &8| &7samples: &f{samples}/{max_samples}\"";
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
                || comment.contains("MAXIMUM PRICE")
                || comment.contains("MAXIMUM TOTAL ESCROW")
                || comment.contains("total escrow cap")
                || comment.contains("quantity x price"));
    }

    private boolean isRtpSearchComment(String line) {
        String comment = line.trim();
        return comment.startsWith("#")
                && (comment.contains("RTP COUNTDOWN")
                || comment.contains("SEARCHING OR WAITING FOR RTP")
                || comment.contains("MAX PLAYERS")
                || comment.contains("MAX TRIES")
                || comment.contains("MAX CHUNK SAMPLES")
                || comment.contains("TICKS BETWEEN CHUNK SAMPLES")
                || comment.contains("GENERATE NEW CHUNKS")
                || comment.contains("limited number of chunks")
                || comment.contains("limited fallback generation")
                || comment.contains("fallback chunks")
                || comment.contains("chunk generation is disabled")
                || comment.contains("already-generated chunks")
                || comment.contains("already-loaded chunks")
                || comment.contains("PRELOAD GENERATED CHUNKS")
                || comment.contains("CHUNK RADIUS TO PRELOAD")
                || comment.contains("destination chunks")
                || comment.contains("MAXIMUM TICKS TO SPEND PRELOADING")
                || comment.contains("player chunk send distance")
                || comment.contains("per-player view distance")
                || comment.contains("per-player simulation distance")
                || comment.contains("original view/simulation distance")
                || comment.contains("VALUES BELOW")
                || comment.contains("safe default")
                || comment.contains("SET 0"));
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
                && (comment.contains("CRASH PROTECTION")
                || comment.contains("unsafe item metadata")
                || comment.contains("UDS STORAGE")
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
                || comment.contains("INVALID VALUES")
                || comment.contains("VALID VALUES"));
    }

    private boolean isSelectItemSourceComment(String line) {
        String comment = line.trim();
        return comment.startsWith("#")
                && (comment.contains("SELECT_ITEM_SOURCE")
                || comment.contains("CATEGORY_FILTERS")
                || comment.contains("SERVER_MATERIALS")
                || comment.contains("SERVER.JAR")
                || comment.contains("catalog source")
                || comment.contains("SELECT_ITEM MENU CONTENTS")
                || comment.contains("VALID VALUES"));
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
            List<String> currentLines,
            YamlConfiguration current,
            YamlConfiguration bundledDefault
    ) {
        List<String> bundledLines;
        try {
            bundledLines = readBundledResourceLines(resourceName);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to read bundled configuration lines for " + resourceName, e);
            return 0;
        }

        return mergeBundledDefaults(resourceName, currentLines, bundledLines, current, bundledDefault);
    }

    private int mergeBundledDefaults(
            String resourceName,
            List<String> currentLines,
            List<String> bundledLines,
            YamlConfiguration current,
            YamlConfiguration bundledDefault
    ) {
        int changes = 0;
        Map<String, YamlPathLine> bundledLineIndex = indexYamlPathLines(bundledLines);
        Set<String> insertedSubtrees = new HashSet<>();

        for (String path : bundledDefault.getKeys(true)) {
            if (isUserManagedBundledPath(resourceName, path) || isUnderAnyPath(insertedSubtrees, path)) {
                continue;
            }

            if (bundledDefault.isConfigurationSection(path)) {
                if (!current.contains(path, true) && !hasScalarParent(current, path)) {
                    if (insertBundledPathBlock(currentLines, bundledLines, bundledLineIndex, path)) {
                        insertedSubtrees.add(path);
                        changes += countBundledDefaultPaths(resourceName, bundledDefault, path);
                    }
                }
                continue;
            }

            if (!current.contains(path, true)) {
                if (!hasScalarParent(current, path)) {
                    if (insertBundledPathBlock(currentLines, bundledLines, bundledLineIndex, path)) {
                        changes++;
                    }
                }
            }
        }

        return changes;
    }

    private boolean insertBundledPathBlock(
            List<String> currentLines,
            List<String> bundledLines,
            Map<String, YamlPathLine> bundledLineIndex,
            String path
    ) {
        YamlPathLine bundledNode = bundledLineIndex.get(path);
        if (bundledNode == null) {
            return false;
        }

        Map<String, YamlPathLine> currentLineIndex = indexYamlPathLines(currentLines);
        int insertAt = findBundledOrderInsertionIndex(currentLines, currentLineIndex, bundledLineIndex, path);
        List<String> block = extractYamlNodeBlock(bundledLines, bundledNode, true);
        insertYamlBlock(currentLines, insertAt, block);
        return true;
    }

    private int findBundledOrderInsertionIndex(
            List<String> currentLines,
            Map<String, YamlPathLine> currentLineIndex,
            Map<String, YamlPathLine> bundledLineIndex,
            String path
    ) {
        List<YamlPathLine> siblings = bundledLineIndex.values().stream()
                .filter(node -> parentPath(node.path).equals(parentPath(path)))
                .toList();

        int siblingIndex = -1;
        for (int index = 0; index < siblings.size(); index++) {
            if (siblings.get(index).path.equals(path)) {
                siblingIndex = index;
                break;
            }
        }

        for (int index = siblingIndex - 1; index >= 0; index--) {
            YamlPathLine currentSibling = currentLineIndex.get(siblings.get(index).path);
            if (currentSibling != null) {
                return findYamlNodeEnd(currentLines, currentSibling);
            }
        }

        for (int index = siblingIndex + 1; index < siblings.size(); index++) {
            YamlPathLine currentSibling = currentLineIndex.get(siblings.get(index).path);
            if (currentSibling != null) {
                return attachedCommentStart(currentLines, currentSibling.lineIndex);
            }
        }

        YamlPathLine parent = currentLineIndex.get(parentPath(path));
        if (parent != null) {
            return findYamlNodeEnd(currentLines, parent);
        }
        return currentLines.size();
    }

    private Map<String, YamlPathLine> indexYamlPathLines(List<String> lines) {
        Map<String, YamlPathLine> index = new LinkedHashMap<>();
        List<YamlStackEntry> stack = new ArrayList<>();
        List<Integer> listItemIndents = new ArrayList<>();
        int blockScalarBaseIndent = -1;

        for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
            String line = lines.get(lineIndex);
            String trimmed = line.trim();
            int indent = leadingWhitespace(line).length();

            if (blockScalarBaseIndent >= 0) {
                if (trimmed.isEmpty() || indent > blockScalarBaseIndent) {
                    continue;
                }
                blockScalarBaseIndent = -1;
            }

            if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("-")) {
                if (trimmed.startsWith("-")) {
                    while (!listItemIndents.isEmpty()
                            && indent <= listItemIndents.get(listItemIndents.size() - 1)) {
                        listItemIndents.remove(listItemIndents.size() - 1);
                    }
                    listItemIndents.add(indent);
                }
                continue;
            }

            while (!listItemIndents.isEmpty()
                    && indent <= listItemIndents.get(listItemIndents.size() - 1)) {
                listItemIndents.remove(listItemIndents.size() - 1);
            }
            if (!listItemIndents.isEmpty()) {
                continue;
            }

            int colonIndex = yamlKeyColonIndex(line, indent);
            if (colonIndex < 0) {
                continue;
            }

            while (!stack.isEmpty() && stack.get(stack.size() - 1).indent >= indent) {
                stack.remove(stack.size() - 1);
            }

            String key = line.substring(indent, colonIndex).trim();
            if (key.isEmpty()) {
                continue;
            }

            String path = stack.isEmpty() ? key : stack.get(stack.size() - 1).path + "." + key;
            String rawValue = line.substring(colonIndex + 1);
            String value = yamlValueWithoutInlineComment(rawValue).trim();
            boolean blockScalar = isBlockScalarValue(value) || isUnclosedQuotedScalarValue(rawValue);
            boolean sectionSyntax = value.isEmpty();

            index.putIfAbsent(path, new YamlPathLine(path, lineIndex, indent, sectionSyntax, blockScalar));
            if (sectionSyntax) {
                stack.add(new YamlStackEntry(indent, path));
            }
            if (blockScalar) {
                blockScalarBaseIndent = indent;
            }
        }

        return index;
    }

    private int yamlKeyColonIndex(String line, int indent) {
        int colonIndex = line.indexOf(':', indent);
        if (colonIndex <= indent) {
            return -1;
        }

        String key = line.substring(indent, colonIndex).trim();
        if (key.isEmpty() || key.startsWith("-") || key.startsWith("#")) {
            return -1;
        }
        return colonIndex;
    }

    private String yamlValueWithoutInlineComment(String rawValue) {
        boolean singleQuoted = false;
        boolean doubleQuoted = false;
        boolean escaped = false;
        for (int index = 0; index < rawValue.length(); index++) {
            char current = rawValue.charAt(index);
            if (current == '"' && !singleQuoted && !escaped) {
                doubleQuoted = !doubleQuoted;
            } else if (current == '\'' && !doubleQuoted) {
                singleQuoted = !singleQuoted;
            } else if (current == '#'
                    && !singleQuoted
                    && !doubleQuoted
                    && (index == 0 || Character.isWhitespace(rawValue.charAt(index - 1)))) {
                return rawValue.substring(0, index);
            }
            escaped = current == '\\' && doubleQuoted && !escaped;
            if (current != '\\') {
                escaped = false;
            }
        }
        return rawValue;
    }

    private boolean isUnclosedQuotedScalarValue(String rawValue) {
        String trimmed = rawValue.stripLeading();
        if (!trimmed.startsWith("\"") && !trimmed.startsWith("'")) {
            return false;
        }

        boolean singleQuoted = false;
        boolean doubleQuoted = false;
        boolean escaped = false;
        for (int index = 0; index < rawValue.length(); index++) {
            char current = rawValue.charAt(index);
            if (current == '"' && !singleQuoted && !escaped) {
                doubleQuoted = !doubleQuoted;
            } else if (current == '\'' && !doubleQuoted) {
                if (singleQuoted && index + 1 < rawValue.length() && rawValue.charAt(index + 1) == '\'') {
                    index++;
                } else {
                    singleQuoted = !singleQuoted;
                }
            }
            escaped = current == '\\' && doubleQuoted && !escaped;
            if (current != '\\') {
                escaped = false;
            }
        }
        return singleQuoted || doubleQuoted;
    }

    private boolean canPlaceYamlInlineComment(String line) {
        int colonIndex = yamlKeyColonIndex(line, leadingWhitespace(line).length());
        if (colonIndex < 0) {
            return false;
        }
        return !isUnclosedQuotedScalarValue(line.substring(colonIndex + 1));
    }

    private String removeManagedInlineCommentFromQuotedValue(String line, String bundledComment) {
        int colonIndex = yamlKeyColonIndex(line, leadingWhitespace(line).length());
        if (colonIndex < 0 || bundledComment.isBlank()) {
            return line;
        }
        String marker = " " + bundledComment.trim();
        int markerIndex = line.indexOf(marker, colonIndex + 1);
        if (markerIndex < 0) {
            return line;
        }
        return line.substring(0, markerIndex) + line.substring(markerIndex + marker.length());
    }

    private boolean containsManagedInlineCommentText(String line) {
        int commentIndex = line.indexOf('#');
        while (commentIndex >= 0) {
            if (isManagedInlineComment(line.substring(commentIndex).trim())) {
                return true;
            }
            commentIndex = line.indexOf('#', commentIndex + 1);
        }
        return false;
    }

    private boolean isManagedInlineComment(String comment) {
        return comment.startsWith("# Enables ")
                || comment.startsWith("# Sets ")
                || comment.startsWith("# Configures ")
                || comment.startsWith("# Selects ")
                || comment.startsWith("# Removes ")
                || comment.startsWith("# Uses ")
                || comment.startsWith("# Requires ")
                || comment.startsWith("# Plays ")
                || comment.startsWith("# Applies ")
                || comment.startsWith("# Resets ");
    }

    private String yamlInlineComment(String line) {
        int colonIndex = yamlKeyColonIndex(line, leadingWhitespace(line).length());
        if (colonIndex < 0) {
            return "";
        }

        String rawValue = line.substring(colonIndex + 1);
        boolean singleQuoted = false;
        boolean doubleQuoted = false;
        boolean escaped = false;
        for (int index = 0; index < rawValue.length(); index++) {
            char current = rawValue.charAt(index);
            if (current == '"' && !singleQuoted && !escaped) {
                doubleQuoted = !doubleQuoted;
            } else if (current == '\'' && !doubleQuoted) {
                singleQuoted = !singleQuoted;
            } else if (current == '#'
                    && !singleQuoted
                    && !doubleQuoted
                    && (index == 0 || Character.isWhitespace(rawValue.charAt(index - 1)))) {
                return rawValue.substring(index).trim();
            }
            escaped = current == '\\' && doubleQuoted && !escaped;
            if (current != '\\') {
                escaped = false;
            }
        }
        return "";
    }

    private String replaceYamlInlineComment(String line, String desiredComment) {
        int colonIndex = yamlKeyColonIndex(line, leadingWhitespace(line).length());
        if (colonIndex < 0 || desiredComment.isBlank()) {
            return line;
        }

        String rawValue = line.substring(colonIndex + 1);
        int commentIndex = yamlInlineCommentIndex(rawValue);
        String valuePart = commentIndex >= 0 ? rawValue.substring(0, commentIndex) : rawValue;
        return line.substring(0, colonIndex + 1)
                + valuePart.stripTrailing()
                + " "
                + desiredComment.trim();
    }

    private int yamlInlineCommentIndex(String rawValue) {
        boolean singleQuoted = false;
        boolean doubleQuoted = false;
        boolean escaped = false;
        for (int index = 0; index < rawValue.length(); index++) {
            char current = rawValue.charAt(index);
            if (current == '"' && !singleQuoted && !escaped) {
                doubleQuoted = !doubleQuoted;
            } else if (current == '\'' && !doubleQuoted) {
                singleQuoted = !singleQuoted;
            } else if (current == '#'
                    && !singleQuoted
                    && !doubleQuoted
                    && (index == 0 || Character.isWhitespace(rawValue.charAt(index - 1)))) {
                return index;
            }
            escaped = current == '\\' && doubleQuoted && !escaped;
            if (current != '\\') {
                escaped = false;
            }
        }
        return -1;
    }

    private boolean isBlockScalarValue(String value) {
        return value.startsWith("|") || value.startsWith(">");
    }

    private List<String> extractYamlNodeBlock(List<String> lines, YamlPathLine node, boolean includeAttachedComments) {
        int start = includeAttachedComments ? attachedCommentStart(lines, node.lineIndex) : node.lineIndex;
        int end = findYamlNodeEnd(lines, node);
        return new ArrayList<>(lines.subList(start, end));
    }

    private int attachedCommentStart(List<String> lines, int lineIndex) {
        int start = lineIndex;
        while (start > 0 && lines.get(start - 1).trim().startsWith("#")) {
            start--;
        }
        return start;
    }

    private int findYamlNodeEnd(List<String> lines, YamlPathLine node) {
        if (!node.sectionSyntax && !node.blockScalar) {
            return Math.min(node.lineIndex + 1, lines.size());
        }

        int index = node.lineIndex + 1;
        while (index < lines.size()) {
            String line = lines.get(index);
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                index++;
                continue;
            }

            int indent = leadingWhitespace(line).length();
            if (indent < node.indent || (indent == node.indent && !trimmed.startsWith("-"))) {
                break;
            }
            index++;
        }

        while (index > node.lineIndex + 1 && lines.get(index - 1).trim().isEmpty()) {
            index--;
        }
        return index;
    }

    private void removeYamlNodeBlock(List<String> lines, YamlPathLine node) {
        int start = attachedCommentStart(lines, node.lineIndex);
        int end = findYamlNodeEnd(lines, node);
        lines.subList(start, end).clear();
        collapseBlankLinesAt(lines, Math.max(0, start - 1));
    }

    private void collapseBlankLinesAt(List<String> lines, int aroundIndex) {
        int index = Math.max(1, Math.min(aroundIndex + 1, lines.size() - 1));
        while (index < lines.size()
                && index > 0
                && lines.get(index).trim().isEmpty()
                && lines.get(index - 1).trim().isEmpty()) {
            lines.remove(index);
        }
    }

    private void appendYamlTopLevelBlock(List<String> target, List<String> block) {
        List<String> cleanBlock = trimYamlBlock(block);
        if (cleanBlock.isEmpty()) {
            return;
        }
        if (!target.isEmpty() && !target.get(target.size() - 1).trim().isEmpty()) {
            target.add("");
        }
        target.addAll(cleanBlock);
    }

    private List<String> trimTrailingBlankLines(List<String> lines) {
        List<String> trimmed = new ArrayList<>(lines);
        while (!trimmed.isEmpty() && trimmed.get(trimmed.size() - 1).trim().isEmpty()) {
            trimmed.remove(trimmed.size() - 1);
        }
        return trimmed;
    }

    private void insertYamlBlock(List<String> lines, int insertAt, List<String> block) {
        List<String> cleanBlock = trimYamlBlock(block);
        if (cleanBlock.isEmpty()) {
            return;
        }

        int firstIndent = leadingWhitespace(cleanBlock.get(0)).length();
        boolean topLevelBlock = firstIndent == 0;
        if (topLevelBlock
                && insertAt > 0
                && !lines.get(insertAt - 1).trim().isEmpty()
                && !cleanBlock.get(0).trim().isEmpty()) {
            cleanBlock.add(0, "");
        }

        lines.addAll(insertAt, cleanBlock);

        int afterInsert = insertAt + cleanBlock.size();
        if (topLevelBlock
                && afterInsert < lines.size()
                && !lines.get(afterInsert - 1).trim().isEmpty()
                && !lines.get(afterInsert).trim().isEmpty()) {
            lines.add(afterInsert, "");
        }
    }

    private List<String> trimYamlBlock(List<String> block) {
        int start = 0;
        int end = block.size();
        while (start < end && block.get(start).trim().isEmpty()) {
            start++;
        }
        while (end > start && block.get(end - 1).trim().isEmpty()) {
            end--;
        }
        return new ArrayList<>(block.subList(start, end));
    }

    private List<String> reindentYamlBlock(List<String> block, int fromIndent, int toIndent) {
        if (fromIndent == toIndent) {
            return new ArrayList<>(block);
        }

        String targetPrefix = " ".repeat(Math.max(0, toIndent));
        List<String> reindented = new ArrayList<>(block.size());
        for (String line : block) {
            if (line.trim().isEmpty()) {
                reindented.add(line);
                continue;
            }
            String sourcePrefix = " ".repeat(Math.min(fromIndent, leadingWhitespace(line).length()));
            if (line.startsWith(sourcePrefix)) {
                reindented.add(targetPrefix + line.substring(sourcePrefix.length()));
            } else {
                reindented.add(line);
            }
        }
        return reindented;
    }

    private boolean isUnderAnyPath(Set<String> parentPaths, String path) {
        for (String parentPath : parentPaths) {
            if (path.startsWith(parentPath + ".")) {
                return true;
            }
        }
        return false;
    }

    private boolean hasAncestorPath(List<String> parentPaths, String path) {
        for (String parentPath : parentPaths) {
            if (path.startsWith(parentPath + ".")) {
                return true;
            }
        }
        return false;
    }

    private String parentPath(String path) {
        int dotIndex = path.lastIndexOf('.');
        return dotIndex < 0 ? "" : path.substring(0, dotIndex);
    }

    private int countBundledDefaultPaths(String resourceName, YamlConfiguration bundledDefault, String rootPath) {
        int count = 0;
        for (String path : bundledDefault.getKeys(true)) {
            if (isUserManagedBundledPath(resourceName, path)) {
                continue;
            }
            if (path.equals(rootPath) || path.startsWith(rootPath + ".")) {
                count++;
            }
        }
        return Math.max(1, count);
    }

    private void validateYamlLines(List<String> lines) throws InvalidConfigurationException {
        loadYamlLines(lines);
    }

    private YamlConfiguration loadYamlLines(List<String> lines) throws InvalidConfigurationException {
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.options().parseComments(true);
        configuration.loadFromString(String.join("\n", lines) + "\n");
        return configuration;
    }

    private boolean isUserManagedBundledPath(String resourceName, String path) {
        // Crate definitions are live server content. The bundled CRATES tree is only
        // an initial example and must not be merged back after admins edit/delete it.
        if ("crates.yml".equals(resourceName)
                && (path.equals("CRATES") || path.startsWith("CRATES."))) {
            return true;
        }

        // Arena sections are written by admin commands and store live map/region data.
        if (("duels.yml".equals(resourceName) || "ffa.yml".equals(resourceName))
                && (path.equals("ARENA_SETTINGS") || path.startsWith("ARENA_SETTINGS."))) {
            return true;
        }

        // Bot settings and item definitions are customized by server admins.
        if (("orders.yml".equals(resourceName) || "auction-house.yml".equals(resourceName))
                && (path.equals("BOTS") || path.startsWith("BOTS.") || path.equals("ITEMS") || path.startsWith("ITEMS."))) {
            return true;
        }

        // Network server entries can be expanded per deployment.
        return "network.yml".equals(resourceName)
                && path.startsWith("NETWORK-STATUS.SERVERS.");
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
        try (Reader reader = new InputStreamReader(new java.io.FileInputStream(file), StandardCharsets.UTF_8)) {
            configuration.load(reader);
        }
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

    private boolean backupExistingFile(File file, File backupDirectory) {
        if (!file.exists()) {
            return true;
        }

        File backup = new File(backupDirectory, file.getName());
        try {
            Files.createDirectories(backupDirectory.toPath());
            Files.copy(file.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to back up " + file.getPath(), e);
            return false;
        }
    }

    private FileConfiguration load(String name, FileConfiguration previousConfiguration) {
        File file = new File(plugin.getDataFolder(), name);
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.options().parseComments(true);

        try {
            try (Reader reader = new InputStreamReader(new java.io.FileInputStream(file), StandardCharsets.UTF_8)) {
                configuration.load(reader);
            }
            invalidConfigurations.remove(name);
            if ("menus.yml".equals(name) && (configuration.contains("SETTINGS-MENU-LEGACY") || hasLegacyButtons(configuration))) {
                plugin.getLogger().warning("Detected legacy menus.yml format. Resetting to default for new slot layout...");
                backupInvalidFile(file);
                file.delete();
                copyBundledResource(name, file, true);
                try (Reader reader = new InputStreamReader(new java.io.FileInputStream(file), StandardCharsets.UTF_8)) {
                    configuration.load(reader);
                }
            }
            return configuration;
        } catch (IOException | InvalidConfigurationException e) {
            boolean firstInvalidLoad = invalidConfigurations.add(name);
            plugin.getLogger().log(Level.SEVERE,
                    "Failed to load " + file.getPath() + "; the original file will not be replaced.",
                    e);
            if (firstInvalidLoad) {
                backupInvalidFile(file);
            }
            if (previousConfiguration != null) {
                plugin.getLogger().warning("Keeping the previously loaded in-memory configuration for " + name + ".");
                return previousConfiguration;
            }
            try {
                plugin.getLogger().warning("Using bundled defaults for " + name
                        + " in memory only until the YAML file is fixed and reloaded.");
                return loadBundledYaml(name);
            } catch (IOException | InvalidConfigurationException | IllegalArgumentException fallbackException) {
                plugin.getLogger().log(Level.SEVERE,
                        "Failed to load bundled in-memory fallback for " + name,
                        fallbackException);
                return configuration;
            }
        }
    }

    private boolean hasLegacyButtons(YamlConfiguration config) {
        ConfigurationSection buttons = config.getConfigurationSection("SETTINGS-MENU.BUTTONS");
        if (buttons == null) {
            return false;
        }
        for (String key : buttons.getKeys(false)) {
            if (key.startsWith("HEADER_") || "TOTEM_PARTICLES".equals(key) || "TP_AUTO".equals(key)) {
                return true;
            }
        }
        return false;
    }

    private void backupInvalidFile(File file) {
        if (!file.exists()) {
            return;
        }
        File backupDirectory = new File(
                new File(plugin.getDataFolder(), "config-backups"),
                LocalDateTime.now().format(BACKUP_TIMESTAMP_FORMAT)
        );
        backupExistingFile(file, backupDirectory);
    }

    private void backupBrokenFile(File file) {
        if (!file.exists()) {
            return;
        }

        File backup = new File(file.getParentFile(), file.getName() + ".broken");
        try {
            Files.copy(file.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "failed to back up broken file " + file.getPath(), e);
        }
    }

    // ── Getters ────────────────────────────────────────────────────────────────

    public FileConfiguration getConfig()        { return config; }
    public FileConfiguration getMessages()      { return localized("MESSAGES", messages); }
    public FileConfiguration getDeathMessages() { return localized("DEATH_MESSAGES", deathMessages); }
    public FileConfiguration getMenus()         { return localized("MENUS", menus); }
    public FileConfiguration getScoreboard()    { return scoreboard; }
    public FileConfiguration getShop()          { return localized("CONFIG.SHOP", shop); }
    public FileConfiguration getSounds()        { return sounds; }
    public FileConfiguration getBillford()      { return localized("CONFIG.BILLFORD", billford); }
    public FileConfiguration getRtp()           { return localized("CONFIG.RTP", rtp); }
    public FileConfiguration getWorth()         { return localized("CONFIG.WORTH", worth); }
    public FileConfiguration getAmethystTools() { return localized("CONFIG.AMETHYST_TOOLS", amethystTools); }
    public FileConfiguration getEnderChest()    { return localized("CONFIG.ENDER_CHEST", enderChest); }
    public FileConfiguration getInvsee()        { return localized("CONFIG.INVSEE", invsee); }
    public FileConfiguration getFreeze()        { return localized("CONFIG.FREEZE", freeze); }
    public FileConfiguration getAuctionHouse()  { return localized("CONFIG.AUCTION_HOUSE", auctionHouse); }
    public FileConfiguration getOrders()        { return localized("CONFIG.ORDERS", orders); }
    public FileConfiguration getOrdersConfig()  { return getOrders(); }
    public FileConfiguration getDuels()         { return localized("CONFIG.DUELS", duels); }
    public FileConfiguration getFfa()           { return localized("CONFIG.FFA", ffa); }
    public FileConfiguration getCrates()        { return localized("CONFIG.CRATES", crates); }
    public FileConfiguration getOriginalCrates() { return crates; }
    public FileConfiguration getOriginalDuels() { return duels; }
    public FileConfiguration getOriginalFfa() { return ffa; }
    public FileConfiguration getOriginalMenus() { return menus; }
    public FileConfiguration getSpawners()      { return localized("CONFIG.SPAWNERS", spawners); }
    public FileConfiguration getSpawnStash()    { return localized("CONFIG.SPAWN_STASH", spawnStash); }
    public FileConfiguration getNetwork()       { return localized("CONFIG.NETWORK", network); }
    public FileConfiguration getStaffMode()     { return localized("CONFIG.STAFF_MODE", staffMode); }
    public FileConfiguration getHide()          { return hide; }
    public FileConfiguration getDatabase()      { return database; }
    public FileConfiguration getServerWipe()    { return localized("CONFIG.SERVER_WIPE", serverWipe); }
    public FileConfiguration getDiscord()       { return discord; }
    public FileConfiguration getAnvilModeration() { return anvilModeration; }
    public FileConfiguration getEnchantments()  { return enchantments; }
    public FileConfiguration getFilter()        { return filter; }

    public FileConfiguration getLegacyMessages() { return messages; }
    public FileConfiguration getLegacyDeathMessages() { return deathMessages; }
    public FileConfiguration getLegacyMenus() { return menus; }
    public FileConfiguration getLegacyShop() { return shop; }
    public FileConfiguration getLegacyAuctionHouse() { return auctionHouse; }

    public void reloadShop() { shop = load("shop.yml", shop); }
    public void reloadMenus() { menus = load("menus.yml", menus); }
    public void reloadSounds() { sounds = load("sounds.yml", sounds); }
    public void reloadWorth() { worth = load("worth.yml", worth); }
    public void reloadAmethystTools() { amethystTools = load("amethyst-tools.yml", amethystTools); }
    public void reloadEnderChest() { enderChest = load("ender-chest.yml", enderChest); }
    public void reloadInvsee() { invsee = load("invsee.yml", invsee); }
    public void reloadFreeze() { freeze = load("freeze.yml", freeze); }
    public void reloadAuctionHouse() { auctionHouse = load("auction-house.yml", auctionHouse); }
    public void reloadOrders() { orders = load("orders.yml", orders); }
    public void reloadDuels() { duels = load("duels.yml", duels); }
    public void reloadFfa() { ffa = load("ffa.yml", ffa); }
    public void reloadCrates() { crates = load("crates.yml", crates); }
    public void reloadSpawners() { spawners = load("spawners.yml", spawners); }
    public void reloadSpawnStash() { spawnStash = load("spawn-stash.yml", spawnStash); }
    public void reloadNetwork() { network = load("network.yml", network); }
    public void reloadStaffMode() { staffMode = load("staff-mode.yml", staffMode); }
    public void reloadHide() { hide = load("hide.yml", hide); }
    public void reloadDatabase() { database = load("database.yml", database); }
    public void reloadDiscord() { discord = load("discord.yml", discord); }
    public void reloadAnvilModeration() { anvilModeration = load("anvil-moderation.yml", anvilModeration); }
    public void reloadEnchantments() { enchantments = load("enchantments.yml", enchantments); }
    public void reloadFilter() { filter = load("filter.yml", filter); }
    public boolean saveConfig() { return save("config.yml", config); }
    public boolean saveDuels() { return save("duels.yml", duels); }
    public boolean saveFfa() { return save("ffa.yml", ffa); }
    public boolean saveCrates() { return save("crates.yml", crates); }
    public boolean saveMenus() { return save("menus.yml", menus); }
    public boolean saveAuctionHouse() { return save("auction-house.yml", auctionHouse); }
    public boolean saveDatabase() { return save("database.yml", database); }
    public boolean saveNetwork() { return save("network.yml", network); }
    public boolean saveDiscord() { return save("discord.yml", discord); }
    public boolean saveAnvilModeration() { return save("anvil-moderation.yml", anvilModeration); }

    // ── Convenience helpers ────────────────────────────────────────────────────

    public String getMessage(String path) {
        String legacy = messages.getString(path);
        LanguageManager languageManager = plugin == null ? null : plugin.getLanguageManager();
        if (languageManager != null) {
            return languageManager.message(path, legacy);
        }
        return legacy == null ? "&cMissing message: " + path : legacy;
    }

    public String getMessage(String path, String... placeholders) {
        String msg = getMessage(path);
        for (int i = 0; i + 1 < placeholders.length; i += 2) {
            msg = msg.replace(placeholders[i], placeholders[i + 1]);
        }
        return msg;
    }

    public String getMessageOrDefault(String path, String fallback) {
        String legacy = messages.getString(path);
        LanguageManager languageManager = plugin == null ? null : plugin.getLanguageManager();
        if (languageManager != null) {
            return languageManager.text("MESSAGES." + path, legacy, fallback);
        }
        return legacy == null ? fallback : legacy;
    }

    public String getMessageOrDefault(String path, String fallback, String... placeholders) {
        String msg = getMessageOrDefault(path, fallback);
        for (int i = 0; i + 1 < placeholders.length; i += 2) {
            String key = placeholders[i];
            String val = placeholders[i + 1];
            msg = msg.replace(key, val);
            if (key.equals("{quantity}")) {
                msg = msg.replace("{Quantity}", val);
            }
        }
        return msg;
    }

    public String getSound(String path) {
        return sounds.getString(path, "");
    }

    public boolean isCommandEnabled(String key) {
        return FeatureManager.isCommandEnabled(config, key);
    }

    private FileConfiguration localized(String rootPath, FileConfiguration legacy) {
        if (plugin == null || plugin.getLanguageManager() == null) {
            return legacy;
        }
        return plugin.getLanguageManager().localize(rootPath, legacy);
    }

    private boolean save(String name, FileConfiguration configuration) {
        if (configuration == null) {
            return false;
        }
        if (invalidConfigurations.contains(name)) {
            plugin.getLogger().warning("Refusing to save " + name
                    + " because its on-disk YAML is invalid. Fix the file and reload it first.");
            return false;
        }

        File file = new File(plugin.getDataFolder(), name);
        try {
            configuration.save(file);
            if (plugin != null && plugin.getLanguageManager() != null) {
                plugin.getLanguageManager().clearLocalizedConfigurations();
            }
            return true;
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save " + file.getPath(), e);
        }
        return false;
    }

    public boolean syncResource(String name, File targetFile) {
        File backupDirectory = new File(
                new File(plugin.getDataFolder(), "config-backups"),
                LocalDateTime.now().format(BACKUP_TIMESTAMP_FORMAT)
        );

        YamlConfiguration bundledDefault;
        try {
            bundledDefault = loadBundledYaml(name);
        } catch (IOException | InvalidConfigurationException | IllegalArgumentException e) {
            plugin.getLogger().log(Level.WARNING, "Skipping configuration sync for missing or invalid bundled resource: " + name, e);
            return false;
        }

        if (!targetFile.exists()) {
            return copyBundledResource(name, targetFile, false);
        }

        YamlConfiguration current;
        try {
            current = loadYamlFile(targetFile);
        } catch (IOException | InvalidConfigurationException e) {
            invalidConfigurations.add(name);
            plugin.getLogger().log(Level.SEVERE,
                    "Skipping configuration sync for invalid YAML without replacing the original file: "
                            + targetFile.getPath(),
                    e);
            backupExistingFile(targetFile, backupDirectory);
            return false;
        }

        TextFileContent currentText;
        try {
            currentText = readTextFile(targetFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to read configuration for line-preserving sync: "
                    + targetFile.getPath(), e);
            return false;
        }

        int mergedPaths = mergeBundledDefaults(name, currentText.lines(), current, bundledDefault);
        if (mergedPaths == 0) {
            return true;
        }

        try {
            validateYamlLines(currentText.lines());
            if (!backupExistingFile(targetFile, backupDirectory)) {
                plugin.getLogger().warning("Skipped configuration sync because backup creation failed: "
                        + targetFile.getPath());
                return false;
            }
            writeTextFileAtomically(targetFile, currentText);
            plugin.getLogger().info("Added " + mergedPaths + " missing bundled default path(s) to " + name + ".");
            return true;
        } catch (IOException | InvalidConfigurationException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save synced configuration " + targetFile.getPath(), e);
            return false;
        }
    }

    public boolean syncGeneratedDefaults(
            String name,
            File targetFile,
            YamlConfiguration defaults,
            String backupFolderName
    ) {
        if (defaults == null) {
            return false;
        }

        File backupDirectory = new File(
                new File(plugin.getDataFolder(), backupFolderName),
                LocalDateTime.now().format(BACKUP_TIMESTAMP_FORMAT)
        );
        TextFileContent defaultText = textContent(defaults.saveToString(), "\n");

        if (!targetFile.exists()) {
            try {
                writeTextFileAtomically(targetFile, defaultText);
                return true;
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING,
                        "Failed to create generated configuration " + targetFile.getPath(), e);
                return false;
            }
        }

        YamlConfiguration current;
        try {
            current = loadYamlFile(targetFile);
        } catch (IOException | InvalidConfigurationException e) {
            plugin.getLogger().log(Level.SEVERE,
                    "Skipping generated configuration sync for invalid YAML without replacing the original file: "
                            + targetFile.getPath(),
                    e);
            backupExistingFile(targetFile, backupDirectory);
            return false;
        }

        TextFileContent currentText;
        try {
            currentText = readTextFile(targetFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING,
                    "Failed to read generated configuration for line-preserving sync: "
                            + targetFile.getPath(),
                    e);
            return false;
        }

        int mergedPaths = mergeBundledDefaults(
                name,
                currentText.lines(),
                defaultText.lines(),
                current,
                defaults
        );
        if (mergedPaths == 0) {
            return true;
        }

        try {
            validateYamlLines(currentText.lines());
            if (!backupExistingFile(targetFile, backupDirectory)) {
                plugin.getLogger().warning("Skipped generated configuration sync because backup creation failed: "
                        + targetFile.getPath());
                return false;
            }
            writeTextFileAtomically(targetFile, currentText);
            plugin.getLogger().info("Added " + mergedPaths + " missing generated default path(s) to " + name + ".");
            return true;
        } catch (IOException | InvalidConfigurationException e) {
            plugin.getLogger().log(Level.WARNING,
                    "Failed to save synced generated configuration " + targetFile.getPath(),
                    e);
            return false;
        }
    }

    private TextFileContent textContent(String content, String defaultLineSeparator) {
        String lineSeparator = detectLineSeparator(content);
        if (lineSeparator.equals(System.lineSeparator()) && !content.contains("\n") && !content.contains("\r")) {
            lineSeparator = defaultLineSeparator;
        }
        boolean trailingLineSeparator = content.endsWith("\r\n")
                || content.endsWith("\n")
                || content.endsWith("\r");
        List<String> lines = new ArrayList<>(Arrays.asList(content.split("\\r\\n|\\n|\\r", -1)));
        if (trailingLineSeparator && !lines.isEmpty()) {
            lines.remove(lines.size() - 1);
        }
        return new TextFileContent(lines, lineSeparator, trailingLineSeparator);
    }

    private static final class SyncResult {
        private boolean created;
        private boolean updated;
        private boolean skipped;
    }

    private record YamlPathLine(
            String path,
            int lineIndex,
            int indent,
            boolean sectionSyntax,
            boolean blockScalar
    ) {
    }

    private record YamlStackEntry(int indent, String path) {
    }

    private record TextFileContent(
            List<String> lines,
            String lineSeparator,
            boolean trailingLineSeparator
    ) {
        private String serialize() {
            String joined = String.join(lineSeparator, lines);
            return trailingLineSeparator ? joined + lineSeparator : joined;
        }
    }
}
