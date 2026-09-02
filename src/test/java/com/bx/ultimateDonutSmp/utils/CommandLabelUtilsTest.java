package com.bx.ultimateDonutSmp.utils;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The two overloads differ only in what an absent label falls back to, which is the whole reason
 * GamemodeCommand and PunishmentCommand used to carry their own copies of this.
 */
class CommandLabelUtilsTest {

    private static final Command GMS = new StubCommand("gms");

    @Test
    void theNamespaceIsStrippedAndTheRestLowercased() {
        assertEquals("gms", CommandLabelUtils.normalizeLabel("ultimatedonutsmp:GMS", GMS));
        assertEquals("gms", CommandLabelUtils.normalizeLabel("ultimatedonutsmp:GMS"));
        assertEquals("unban", CommandLabelUtils.normalizeLabel("Unban", GMS));
    }

    @Test
    void withACommandAnAbsentLabelFallsBackToItsRegisteredName() {
        assertEquals("gms", CommandLabelUtils.normalizeLabel(null, GMS));
        assertEquals("gms", CommandLabelUtils.normalizeLabel("", GMS));
        assertEquals("gms", CommandLabelUtils.normalizeLabel("   ", GMS));
    }

    @Test
    void withoutACommandAnAbsentLabelNormalizesToAnEmptyString() {
        assertEquals("", CommandLabelUtils.normalizeLabel(null));
        assertEquals("", CommandLabelUtils.normalizeLabel(""));
        assertEquals("", CommandLabelUtils.normalizeLabel("   "));
        assertEquals("", CommandLabelUtils.normalizeLabel(null, null));
    }

    @Test
    void aTrailingColonIsLeftAloneRatherThanEmptyingTheLabel() {
        // The substring only runs when something follows the separator.
        assertEquals("gms:", CommandLabelUtils.normalizeLabel("gms:", GMS));
    }

    private static final class StubCommand extends Command {
        private StubCommand(String name) {
            super(name);
        }

        @Override
        public boolean execute(CommandSender sender, String label, String[] args) {
            return true;
        }
    }
}
