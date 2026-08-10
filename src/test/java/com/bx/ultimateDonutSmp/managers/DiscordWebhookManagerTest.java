package com.bx.ultimateDonutSmp.managers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DiscordWebhookManagerTest {

    @Test
    void sanitizeDescriptionStripsCorruptedCommentGeneratorLines() {
        DiscordWebhookManager manager = new DiscordWebhookManager(null);

        String corruptedInput = ":hammer: **Punishment Type:** Ban\n"
                + "\n"
                + "# The text or value for # The Text Or Mode For **Player. Available Options. Available options: Any valid string text\n"
                + "# The text or mode for **Player. Available options: Any string text **Player:**\n"
                + "%player%\n"
                + "\n"
                + "# The text or value for # The Text Or Mode For **Staff. Available Options. Available options: Any valid string text\n"
                + "# The text or mode for **Staff. Available options: Any string text **Staff:**\n"
                + "%staff%\n"
                + "\n"
                + "**Reason:**\n"
                + "||%reason%||";

        String cleaned = manager.sanitizeDescription(corruptedInput);

        assertFalse(cleaned.contains("Available options:"));
        assertFalse(cleaned.contains("# The text or"));
        assertTrue(cleaned.contains(":hammer: **Punishment Type:** Ban"));
        assertTrue(cleaned.contains("%player%"));
        assertTrue(cleaned.contains("%staff%"));
        assertTrue(cleaned.contains("||%reason%||"));
    }
}
