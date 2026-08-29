# Duels & Instanced FFA Arena Setup Guide

UltimateDonutSMP provides built-in, production-grade 1v1 Duels and instanced Free-For-All (FFA) PvP systems with automatic map rollbacks, crystal speed optimizations, custom kits, and queue management.

> Looking for a permanent arena where everyone fights at once and keeps an Elo rating between visits? That is the [Ranked PvP Arena](Ranked-PvP-Arena), a separate system. The FFA below is instanced: it pairs players in a throwaway copy of a map and rolls the blocks back afterwards.

---

## 1v1 Duels System Setup

### 1. Creating a Duel Arena (`/arena`)
Administrators can configure duel arenas using `/arena`:

1. Select Arena Boundary 1 & 2 using selection mode or position setting:
   ```bash
   /arena setpos1 <arena_name>
   /arena setpos2 <arena_name>
   ```
2. Set Return Spawn Point (where players return after match ends):
   ```bash
   /arena setreturn <arena_name>
   ```
3. Enable Arena:
   ```bash
   /arena enable <arena_name>
   ```

### Full `/arena` Command Syntax:
- `/arena create <name>` – Initialize arena
- `/arena delete <name>` – Delete arena
- `/arena setpos1 <name>` – Set player 1 spawn location
- `/arena setpos2 <name>` – Set player 2 spawn location
- `/arena setreturn <name>` – Set match completion return location
- `/arena setdisplay <name>` – Set icon item for arena selector menu
- `/arena enable <name>` – Enable arena for matchmaking
- `/arena disable <name>` – Disable arena for maintenance
- `/arena list` – Display all duel arenas & status

---

## Player Duel Workflow

Players can challenge each other or join match queues:

- **Challenge Player**: `/duel <player>`
- **Accept Challenge**: `/duel accept <player>`
- **Deny Challenge**: `/duel deny <player>`
- **Match Queue**: `/queue` (opens GUI to join Ranked / Unranked queues)
- **Request Draw**: `/draw` (both players must type `/draw` to end in tie)
- **Forfeit / Leave**: `/leave`

---

## Instanced FFA Arena Setup

The Instanced FFA system supports unlimited players fighting inside dedicated FFA arenas with automatic killstreak tracking and stats.

### 1. Creating FFA Arena (`/ffaarena`)
1. Create FFA arena:
   ```bash
   /ffaarena create <name>
   ```
2. Set spawn point inside FFA arena:
   ```bash
   /ffaarena setpos <name>
   ```
3. Enable arena:
   ```bash
   /ffaarena enable <name>
   ```

### Full `/ffaarena` Command Syntax:
- `/ffaarena create <name>` – Create new FFA arena
- `/ffaarena delete <name>` – Delete FFA arena
- `/ffaarena setpos <name>` – Set spawn location inside arena
- `/ffaarena setdisplay <name>` – Set GUI icon
- `/ffaarena enable <name>` – Enable arena
- `/ffaarena disable <name>` – Disable arena
- `/ffaarena list` – List all FFA arenas

### Player FFA Commands:
- Join FFA: `/ffa` or `/ffa join`
- Leave FFA: `/leave`
- Check FFA Statistics: `/ffastats [player]` (Kills, Deaths, KDR, Current Streak, Best Streak)

---

## Fast Crystal & Combat Tweaks

In `duels.yml`, administrators can adjust crystal placing and obsidian placing speeds to simulate DonutSMP-style high-speed End Crystal PvP:

```yaml
DUELS:
  FAST-CRYSTAL:
    ENABLED: true
    PLACEMENT-DELAY-TICKS: 0
  MAP-ROLLBACK:
    AUTO-RESTORE-OBSIDIAN: true
    RESTORE-DELAY-SECONDS: 3
```
