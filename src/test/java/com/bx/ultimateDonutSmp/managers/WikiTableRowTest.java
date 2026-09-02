package com.bx.ultimateDonutSmp.managers;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A "|" inside a code span still splits a markdown table cell: GitHub cuts the row into cells before
 * it parses inline code, so `a|b` in a value silently turns one cell into two and pushes the last
 * cell, usually the whole description, off the end of the row. It has to be written as `a\|b`.
 *
 * <p>Rows are checked against their own table's header rather than a fixed column count, because
 * these pages carry two, three and four column tables beside the five column option tables, and a
 * fixed count reports every short table as broken.
 */
class WikiTableRowTest {

    private static final Path WIKI = Path.of("docs", "wiki");

    /** Splits on the pipes that actually separate cells, leaving escaped ones alone. */
    private static final Pattern CELL_SEPARATOR = Pattern.compile("(?<!\\\\)\\|");

    private static final Pattern SEPARATOR_ROW = Pattern.compile("^\\|[\\s:\\-|]+\\|$");

    private static int cellCount(String row) {
        // "|a|b|" splits to ["", "a", "b", ""], so two of the pieces are the outer edges.
        return CELL_SEPARATOR.split(row, -1).length - 2;
    }

    private static List<String> mismatchedRows(Path page) throws IOException {
        List<String> problems = new ArrayList<>();
        List<String> lines = Files.readAllLines(page, StandardCharsets.UTF_8);

        boolean insideFence = false;
        Integer headerCells = null;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.startsWith("```")) {
                insideFence = !insideFence;
                continue;
            }
            if (insideFence) {
                continue;
            }

            String trimmed = line.trim();
            if (!trimmed.startsWith("|")) {
                headerCells = null;
                continue;
            }
            if (SEPARATOR_ROW.matcher(trimmed).matches()) {
                continue;
            }
            if (headerCells == null) {
                headerCells = cellCount(trimmed);
                continue;
            }

            int cells = cellCount(trimmed);
            if (cells != headerCells) {
                problems.add(page.getFileName() + ":" + (i + 1)
                        + " has " + cells + " cells but its table header has " + headerCells
                        + ". An unescaped | inside a code span is the usual cause. Line: "
                        + trimmed.substring(0, Math.min(120, trimmed.length())));
            }
        }
        return problems;
    }

    @Test
    void everyWikiTableRowMatchesItsHeader() throws IOException {
        assertTrue(Files.isDirectory(WIKI), "docs/wiki is missing, so this test is checking nothing");

        List<String> problems = new ArrayList<>();
        List<Path> pages;
        try (Stream<Path> walk = Files.list(WIKI)) {
            pages = walk.filter(p -> p.toString().endsWith(".md")).sorted().toList();
        }

        assertTrue(pages.size() > 30, "expected the wiki pages to be present, found " + pages.size());

        for (Path page : pages) {
            problems.addAll(mismatchedRows(page));
        }

        assertTrue(problems.isEmpty(), "wiki table rows lose cells:\n" + String.join("\n", problems));
    }
}
