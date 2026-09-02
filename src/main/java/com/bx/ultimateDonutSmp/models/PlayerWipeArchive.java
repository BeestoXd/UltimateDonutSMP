package com.bx.ultimateDonutSmp.models;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Everything a player wipe removed, kept on disk so {@code /playerunwipe} can put it back. The rows
 * are stored exactly as the database handed them over — column names beside the values, one entry
 * per row — so restoring is an insert rather than a guess at what the old state looked like.
 */
public final class PlayerWipeArchive {

    /** The archive layout, so an older file is rejected rather than half-read. */
    public static final int FORMAT = 1;

    public record TableRows(List<String> columns, List<List<Object>> rows) {

        public TableRows {
            columns = List.copyOf(columns);
            List<List<Object>> copied = new ArrayList<>(rows.size());
            for (List<Object> row : rows) {
                copied.add(Collections.unmodifiableList(new ArrayList<>(row)));
            }
            rows = Collections.unmodifiableList(copied);
        }
    }

    private final UUID playerUuid;
    private final String playerName;
    private final long wipedAt;
    private final String wipedBy;
    private final List<String> statColumns;
    private final List<Object> statValues;
    private final Map<String, TableRows> tables;

    public PlayerWipeArchive(
            UUID playerUuid,
            String playerName,
            long wipedAt,
            String wipedBy,
            List<String> statColumns,
            List<Object> statValues,
            Map<String, TableRows> tables
    ) {
        this.playerUuid = playerUuid;
        this.playerName = playerName;
        this.wipedAt = wipedAt;
        this.wipedBy = wipedBy;
        this.statColumns = List.copyOf(statColumns);
        this.statValues = Collections.unmodifiableList(new ArrayList<>(statValues));
        this.tables = Collections.unmodifiableMap(new LinkedHashMap<>(tables));
    }

    public UUID playerUuid() {
        return playerUuid;
    }

    public String playerName() {
        return playerName;
    }

    public long wipedAt() {
        return wipedAt;
    }

    public String wipedBy() {
        return wipedBy;
    }

    public List<String> statColumns() {
        return statColumns;
    }

    public List<Object> statValues() {
        return statValues;
    }

    public Map<String, TableRows> tables() {
        return tables;
    }

    /** True when the wipe found a {@code players} row to reset, so there is one to restore. */
    public boolean hasStats() {
        return !statColumns.isEmpty() && statColumns.size() == statValues.size();
    }

    public int rowCount() {
        int total = hasStats() ? 1 : 0;
        for (TableRows table : tables.values()) {
            total += table.rows().size();
        }
        return total;
    }

    public void save(File file) throws IOException {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("format", FORMAT);
        yaml.set("player-uuid", playerUuid.toString());
        yaml.set("player-name", playerName);
        yaml.set("wiped-at", wipedAt);
        yaml.set("wiped-by", wipedBy);
        if (hasStats()) {
            yaml.set("stats.columns", new ArrayList<>(statColumns));
            yaml.set("stats.values", new ArrayList<>(statValues));
        }
        for (Map.Entry<String, TableRows> entry : tables.entrySet()) {
            List<List<Object>> rows = new ArrayList<>();
            for (List<Object> row : entry.getValue().rows()) {
                rows.add(new ArrayList<>(row));
            }
            yaml.set("tables." + entry.getKey() + ".columns", new ArrayList<>(entry.getValue().columns()));
            yaml.set("tables." + entry.getKey() + ".rows", rows);
        }

        File parent = file.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
        yaml.save(file);
    }

    /**
     * Reads an archive back. Anything malformed throws rather than restoring part of a player, so a
     * hand-edited or truncated file cannot leave them half-way between two states.
     */
    public static PlayerWipeArchive load(File file) throws IOException {
        YamlConfiguration yaml = new YamlConfiguration();
        try {
            yaml.load(file);
        } catch (org.bukkit.configuration.InvalidConfigurationException exception) {
            throw new IOException("Wipe backup " + file.getName() + " is not readable.", exception);
        }

        int format = yaml.getInt("format", 0);
        if (format != FORMAT) {
            throw new IOException("Wipe backup " + file.getName() + " uses unsupported format " + format + ".");
        }

        UUID uuid;
        try {
            uuid = UUID.fromString(String.valueOf(yaml.getString("player-uuid")));
        } catch (IllegalArgumentException exception) {
            throw new IOException("Wipe backup " + file.getName() + " has no valid player uuid.", exception);
        }

        List<String> statColumns = yaml.getStringList("stats.columns");
        List<Object> statValues = new ArrayList<>(yaml.getList("stats.values", List.of()));
        if (statColumns.size() != statValues.size()) {
            throw new IOException("Wipe backup " + file.getName() + " has mismatched stat columns.");
        }

        Map<String, TableRows> tables = new LinkedHashMap<>();
        ConfigurationSection tablesSection = yaml.getConfigurationSection("tables");
        if (tablesSection != null) {
            for (String table : tablesSection.getKeys(false)) {
                ConfigurationSection tableSection = tablesSection.getConfigurationSection(table);
                if (tableSection == null) {
                    continue;
                }
                List<String> columns = tableSection.getStringList("columns");
                List<List<Object>> rows = new ArrayList<>();
                for (Object row : tableSection.getList("rows", List.of())) {
                    if (!(row instanceof List<?> values)) {
                        throw new IOException("Wipe backup " + file.getName() + " has a malformed row in " + table + ".");
                    }
                    if (values.size() != columns.size()) {
                        throw new IOException("Wipe backup " + file.getName() + " has a short row in " + table + ".");
                    }
                    rows.add(new ArrayList<>(values));
                }
                tables.put(table, new TableRows(columns, rows));
            }
        }

        return new PlayerWipeArchive(
                uuid,
                yaml.getString("player-name", uuid.toString()),
                yaml.getLong("wiped-at"),
                yaml.getString("wiped-by", "unknown"),
                statColumns,
                statValues,
                tables
        );
    }
}
