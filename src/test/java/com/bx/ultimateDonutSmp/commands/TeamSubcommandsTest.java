package com.bx.ultimateDonutSmp.commands;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The usage line in plugin.yml is what players read when they get /team wrong, so a subcommand
 * listed there and nowhere else reads as a command that exists when it does not.
 */
class TeamSubcommandsTest {

    private static final Pattern USAGE_ARGUMENTS = Pattern.compile("<([^>]+)>");

    @Test
    void pluginYamlUsageAndTabCompletionListTheSameSubcommands() throws Exception {
        assertEquals(
                new TreeSet<>(usageSubcommands()),
                new TreeSet<>(UniversalCommandTabCompleter.TEAM_SUBCOMMANDS)
        );
    }

    @Test
    void usageAdvertisesTheTeamLookup() throws Exception {
        assertTrue(usageSubcommands().contains("info"));
    }

    private static List<String> usageSubcommands() throws Exception {
        YamlConfiguration plugin = new YamlConfiguration();
        plugin.load(new File("src/main/resources/plugin.yml"));

        String usage = plugin.getString("commands.team.usage", "");
        Matcher matcher = USAGE_ARGUMENTS.matcher(usage);
        assertTrue(matcher.find(), "commands.team.usage must list its subcommands: " + usage);

        List<String> subcommands = new ArrayList<>();
        for (String option : matcher.group(1).split("\\|")) {
            subcommands.add(option.trim());
        }
        return subcommands;
    }
}
