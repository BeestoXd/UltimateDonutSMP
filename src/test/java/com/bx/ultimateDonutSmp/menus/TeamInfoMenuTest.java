package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.models.Team;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TeamInfoMenuTest {

    private final Map<UUID, String> names = new HashMap<>();

    @Test
    void leaderComesFirstAndTheRestAreAlphabetical() {
        UUID leader = named("Marcus");
        UUID zara = named("zara");
        UUID alice = named("Alice");
        UUID bob = named("bob");

        Team team = new Team("Dawn", leader);
        team.addMember(zara);
        team.addMember(leader);
        team.addMember(bob);
        team.addMember(alice);

        assertEquals(
                List.of(leader, alice, bob, zara),
                TeamInfoMenu.orderMembers(team, names::get)
        );
    }

    @Test
    void membersWithNoKnownNameSortLastWithoutBreakingTheOrder() {
        UUID leader = named("Marcus");
        UUID unknown = UUID.randomUUID();
        UUID alice = named("Alice");

        Team team = new Team("Dawn", leader);
        team.addMember(unknown);
        team.addMember(leader);
        team.addMember(alice);

        assertEquals(
                List.of(leader, alice, unknown),
                TeamInfoMenu.orderMembers(team, names::get)
        );
    }

    @Test
    void pagingCoversEveryMemberAndNeverDropsBelowOnePage() {
        assertEquals(1, TeamInfoMenu.pageCount(0, 45));
        assertEquals(1, TeamInfoMenu.pageCount(45, 45));
        assertEquals(2, TeamInfoMenu.pageCount(46, 45));
        assertEquals(2, TeamInfoMenu.pageCount(90, 45));
        assertEquals(3, TeamInfoMenu.pageCount(91, 45));
    }

    @Test
    void anUnusablePageSizeFallsBackToOneMemberPerPage() {
        assertEquals(4, TeamInfoMenu.pageCount(4, 0));
        assertEquals(4, TeamInfoMenu.pageCount(4, -10));
    }

    private UUID named(String name) {
        UUID uuid = UUID.randomUUID();
        names.put(uuid, name);
        return uuid;
    }
}
