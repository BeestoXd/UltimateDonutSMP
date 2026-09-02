package com.bx.ultimateDonutSmp.menus;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Right-clicking a button in the /home menu deleted on the spot, so missing the left-click cost a
 * player a home with nothing to undo it. Both deletes the menu offers now open a confirmation,
 * which is how every other destructive click in the plugin already behaves.
 */
class HomeDeleteConfirmationTest {

    private static final Path HOME_MENU =
            Path.of("src", "main", "java", "com", "bx", "ultimateDonutSmp", "menus", "HomeMenu.java");

    @Test
    void everyRightClickBranchOpensAConfirmationFirst() throws Exception {
        List<String> lines = Files.readAllLines(HOME_MENU, StandardCharsets.UTF_8);
        List<String> offenders = new ArrayList<>();
        int branches = 0;

        for (int i = 0; i < lines.size(); i++) {
            if (!lines.get(i).contains("isRightClick()")) {
                continue;
            }
            branches++;
            String next = i + 1 < lines.size() ? lines.get(i + 1) : "";
            if (!next.contains("confirm")) {
                offenders.add("HomeMenu.java:" + (i + 2) + " -> " + next.trim());
            }
        }

        assertEquals(2, branches,
                "the menu right-click deletes a personal home and the team home, so both branches"
                        + " have to stay covered by this test");
        assertEquals(List.of(), offenders,
                "a right-click here destroys something, so it has to open a confirmation instead");
    }

    @Test
    void theMenuNoLongerDeletesAHomeOnItsOwn() throws Exception {
        String source = Files.readString(HOME_MENU, StandardCharsets.UTF_8);

        assertFalse(
                source.contains("getHomeManager().deleteHome("),
                "HomeDeleteConfirmMenu owns the delete now; a call here would be a route around it"
        );
    }

    @Test
    void aRememberedPageIsPulledBackToOneThePlayerStillHas() {
        assertEquals(0, HomeMenu.clampPage(0, 1));
        assertEquals(2, HomeMenu.clampPage(2, 3));
        assertEquals(2, HomeMenu.clampPage(7, 3), "a page past the last one lands on the last one");
        assertEquals(0, HomeMenu.clampPage(-4, 3), "cancelling can never scroll backwards off the menu");
        assertEquals(0, HomeMenu.clampPage(3, 0), "with no pages counted there is still a first page to draw");
    }
}
