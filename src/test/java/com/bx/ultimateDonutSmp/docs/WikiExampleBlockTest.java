package com.bx.ultimateDonutSmp.docs;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Catches config wiki examples that have been cut off part way through.
 *
 * <p>The pages under {@code docs/wiki/Config-*.md} are generated outside this repository by a
 * tool that stops writing an example after 1000 characters. In September 2026 that had quietly
 * truncated 66 blocks across 24 pages, and only 8 of them ended mid-word, so the rest read as
 * complete while missing everything below the cut. The generator cannot be fixed from here, so
 * the next batch of generated pages will arrive the same way, and this is what notices.</p>
 *
 * <p>Only the generated {@code Config-*.md} pages are checked. Hand written pages such as
 * {@code FAQ.md} put illustrative snippets in yaml fences that are deliberately not loadable on
 * their own, and they are not produced by the tool this guards against.</p>
 */
class WikiExampleBlockTest {

    private static final Path WIKI = Path.of("docs", "wiki");
    private static final String FENCE = "```";

    /**
     * Where the generator stops. Of the blocks found truncated in September 2026, 63 were exactly
     * this long, which is what identified the cause in the first place.
     */
    private static final int GENERATOR_LIMIT = 1000;

    @Test
    void everyGeneratedConfigExampleIsComplete() throws IOException {
        List<Path> pages = configPages();
        assertFalse(pages.isEmpty(),
                "found no " + WIKI + "/Config-*.md pages to check; is the working directory the module root?");

        List<String> problems = new ArrayList<>();
        int checked = 0;
        for (Path page : pages) {
            List<String> lines = Files.readString(page, StandardCharsets.UTF_8).lines().toList();
            int openedAt = -1;
            boolean yaml = false;
            for (int i = 0; i < lines.size(); i++) {
                String trimmed = lines.get(i).trim();
                if (!trimmed.startsWith(FENCE)) {
                    continue;
                }
                if (openedAt < 0) {
                    openedAt = i;
                    yaml = trimmed.substring(FENCE.length()).trim().equalsIgnoreCase("yaml");
                    continue;
                }
                if (yaml) {
                    String body = String.join("\n", lines.subList(openedAt + 1, i));
                    if (!body.isBlank()) {
                        checked++;
                        String problem = inspect(body);
                        if (problem != null) {
                            problems.add(describe(page, openedAt + 2, body, problem));
                        }
                    }
                }
                openedAt = -1;
            }
        }

        assertTrue(checked > 100, "only " + checked + " example blocks were found, so the scan is not working");
        assertTrue(problems.isEmpty(), () -> problems.size() + " wiki example block(s) look truncated:\n\n"
                + String.join("\n\n", problems)
                + "\n\nEach one is missing the rest of its config. Copy the complete version from the"
                + "\n'Commented Setup Code Example' block in the same section of the same page.");
    }

    /**
     * Why a block looks cut off, or null when it looks whole.
     *
     * <p>Three signatures, and all three earn their place. The length catches a cut that landed on
     * a line boundary, which is the common case and leaves nothing visibly wrong. Failing to parse
     * catches a cut inside a key or a quoted value, which happens at any length: two blocks that
     * survived the first sweep were 1023 and 1036 characters and stopped halfway through a display
     * name. An empty trailing list entry is valid YAML of the right length, so only looking for it
     * finds it.</p>
     */
    private static String inspect(String body) {
        if (lastContentLine(body).equals("-")) {
            return "it ends on an empty list entry";
        }
        if (body.length() == GENERATOR_LIMIT) {
            return "it is exactly " + GENERATOR_LIMIT + " characters, which is where the generator stops writing";
        }
        try {
            new YamlConfiguration().loadFromString(body);
        } catch (InvalidConfigurationException exception) {
            return "it does not parse as YAML: " + firstLineOf(exception);
        }
        return null;
    }

    private static String describe(Path page, int firstContentLine, String body, String problem) {
        return page + " line " + firstContentLine + ": " + problem
                + "\n    the block ends on: " + lastContentLine(body);
    }

    private static String lastContentLine(String body) {
        String[] lines = body.split("\\n");
        for (int i = lines.length - 1; i >= 0; i--) {
            if (!lines[i].isBlank()) {
                return lines[i].strip();
            }
        }
        return "";
    }

    private static String firstLineOf(Throwable throwable) {
        String message = throwable.getMessage();
        if (message == null || message.isBlank()) {
            return throwable.getClass().getSimpleName();
        }
        return message.lines().findFirst().orElse(message).strip();
    }

    private static List<Path> configPages() throws IOException {
        try (Stream<Path> pages = Files.list(WIKI)) {
            return pages
                    .filter(path -> path.getFileName().toString().startsWith("Config-"))
                    .filter(path -> path.getFileName().toString().endsWith(".md"))
                    .sorted()
                    .toList();
        }
    }
}
