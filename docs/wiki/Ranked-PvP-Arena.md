# Ranked PvP Arena

The ranked arena is a persistent free-for-all area with kits, an Elo rating, a rank ladder, a
separate level track, and an optional scheduled reset that pastes a schematic back over the map. On
top of that sits a ranked 1v1 queue with its own match history, leaderboards and an assign menu for
testers. All of it is configured through `pvp.yml` and the `/pvp` command, and it ships switched
off.

It is not a second FFA system. [Instanced FFA](Duels-and-FFA) matches two players in a throwaway
copy of an arena and rolls the blocks back afterwards. The ranked arena is one permanent place that
everyone fights in at once, and what carries over between visits is the rating rather than the map.

---

## What it does not do

Three things are deliberately left to the plugins that already do them.

- **Combat tagging.** The arena never tags anyone. Whatever combat plugin the server runs stays in
  charge of tagging, logout punishment and command blocking during a fight.
- **Inventory separation.** The arena does not snapshot or restore a survival inventory. Give it its
  own world and let Multiverse-Inventories, or whatever else you already use, keep the two apart.
  The arena only clears what it handed out when a player leaves.
- **Nametags.** The arena publishes `%pvp_rank%` and the rest as PlaceholderAPI placeholders. TAB
  keeps drawing the nametag from them.

---

## Setting one up

Give yourself `ultimatedonutsmp.admin.pvp` and work through this in the world the arena lives in.

```
/pvp create Arena
/pvp setspawn
/pvp setlobby
/pvp wand
/pvp setboundary
```

`/pvp setspawn` is the only step the arena cannot open without. Run it where players should appear.
`/pvp setlobby` records where `/pvp leave`, a boundary exit and a reconnect send people; without it
the arena falls back to the server spawn. `/pvp setspawn2` marks where the second player in a ranked
1v1 starts; leave it unset and both fighters spawn on the same point.

`/pvp wand` hands you a golden axe. Left click one corner of the arena, right click the opposite
one, then `/pvp setboundary` stores them. Anyone who steps outside that box - plus the
`ARENA.BOUNDARY_PADDING` slack - is pulled out of the fight. Skip the boundary entirely and the
whole arena world counts as inside, which is usually what you want for a dedicated world.

Finally switch it on:

```yaml
SETTINGS:
  ENABLED: true
```

---

## Kits

A player picks a kit when they join and again after every respawn, so a kit has to exist before the
arena will let anyone in.

```
/pvp kit create warrior
```

That opens the editor. The top four rows are the player's inventory - the first row is the hotbar -
the four slots at the bottom left are boots, leggings, chestplate and helmet in that order, and the
slot after the label is the offhand. Put the real items in. Enchantments, custom names, lore, potion
types, stack sizes and items from other plugins all survive, because what gets stored is the item
itself rather than a description of it. Closing the menu saves.

The rest of a kit is set from the command line:

```
/pvp kit icon warrior NETHERITE_SWORD
/pvp kit display warrior &cWarrior
/pvp kit permission warrior ultimatedonutsmp.pvp.kit.warrior
/pvp kit slot warrior 11
```

A kit with no permission is open to everyone. A kit with no slot is placed automatically, centred in
the menu alongside the others. `/pvp kit list` shows what exists and `/pvp kit delete <name>`
removes one.

---

## Elo, ranks and levels

Every kill moves two numbers. Elo decides the rank, and XP decides the level; they are independent
on purpose, so a long-serving player can be highly levelled and still rank badly.

```yaml
ELO:
  GAIN_PER_KILL: 25
  LOSS_PER_DEATH: 20

LEVELS:
  XP_PER_KILL: 50
  BASE_XP: 100
  XP_INCREASE_PER_LEVEL: 50
```

Ranks are whatever you define. The bundled ladder is the usual LT5 to HT1, but the ids, the names
and the Elo each one costs are all yours:

```yaml
RANKS:
  HT5:
    DISPLAY: '&eHT5'
    ELO: 600
```

A player holds the highest rank they still meet the Elo for, checked after every kill, so ranks go
down as well as up with no separate demotion setting to keep in sync.

Levels come off a straight line: level 2 costs `BASE_XP`, and every level after that costs one more
`XP_INCREASE_PER_LEVEL` than the one before it. With the defaults that is 100, 150, 200 and so on.

Rank and level changes can be announced to the whole server:

```yaml
BROADCASTS:
  LEVEL_UP:
    GLOBAL: true
    MESSAGE: '&8[&cPVP&8] &f%player_name% &7has reached PvP Level &c%pvp_level%&7!'
```

---

## Ranked 1v1 matches

Alongside the open arena there is a ranked queue. `/pvp queue` opens a menu, the player picks the kit
they want to fight with, and confirming puts them in line. As soon as a second person is waiting the
two are paired, dropped on `ARENA.SPAWN` and `ARENA.SPAWN_2`, handed the kit, and held for a short
countdown during which neither can be hurt.

Pairing is by wait time, not by rating. On a survival server the queue is rarely more than a handful
of people, and holding a high rated player back to look for a closer opponent mostly means nobody
gets a fight at all.

The match ends when somebody dies, disconnects, leaves the arena boundary, or `MATCH.MAX_DURATION`
runs out, which is a draw. The winner gains `MATCH.ELO_WIN`, the loser drops `MATCH.ELO_LOSS`, and
both are sent back to the lobby. Ranked deaths never trigger the open arena's per-kill rewards or its
respawn countdown, so a match scores itself once and only once.

```yaml
MATCH:
  ENABLED: true
  COUNTDOWN_SECONDS: 5
  MAX_DURATION: '5m'
  ELO_WIN: 20
  ELO_LOSS: 15
```

---

## The menus

Four menus cover the ranked side.

`/pvp queue` is the queue itself: the available kits in the middle, a confirm button on the right, a
leave button on the left. The kit is chosen before queueing rather than after being paired, so two
matched players arrive already agreed on the loadout.

`/pvp leaderboard` shows one icon per board, with its ranking in the icon's lore. Elo, level, kills,
deaths, best streak and arena joins each get their own, and the icon materials and the row format are
config. Clicking an icon refreshes it.

`/pvp history [player]` lists past matches newest first, one item each. The lore carries the date,
how long the match ran, who won, and both fighters' hits, crystals and Elo change. Those numbers come
from the stored match row rather than from the players' current totals, so an old entry still reads
correctly long after the ladder has moved on.

`/pvp assign` is the tester's menu. Click the two player slots to cycle through everyone online,
click the middle slot to pick the kit, then confirm to start the match without either player having
queued. `/pvp assign <player> <player> [kit]` does the same thing from the command line, which is
what you want when running the same fixture repeatedly. Both need `ultimatedonutsmp.admin.pvp`.

Hits and crystals are counted separately during a match. A crystal explosion names the crystal as
the damager rather than whoever set it off, so in a ranked match it is credited to the opponent,
which is exact because a match only ever has two people in it.

---

## Kill farming

Killing the same player over and over stops paying:

```yaml
ANTI_KILL_FARMING:
  ENABLED: true
  MAX_REWARDED_KILLS: 3
  COOLDOWN: '5m'
  REDUCED_ELO: 0
  REDUCED_XP: 0
```

The counter is per killer and victim pair and forgets a victim once the cooldown passes without
another kill. Unrewarded kills still count toward K/D and the streak - they happened - they simply
stop paying Elo and XP, and by default they stop costing the victim Elo too.

---

## The scoreboard

While a player is in the arena the sidebar comes from `pvp.yml` instead of `scoreboard.yml`. It
takes any PlaceholderAPI placeholder, including this feature's own.

```yaml
SCOREBOARD:
  ENABLED: true
  TITLE:
  - '&c&lPVP ARENA'
  LINES:
  - '&fRANK: %pvp_rank%'
  - '&fLEVEL: &c%pvp_level%'
  - '&fK/D: &c%pvp_kills%&7/&c%pvp_deaths%'
  - '&fARENA RESET'
  - '&c%pvp_arena_reset%'
  RESET_FORMAT: '{d}D:{h}H:{m}M:{s}S'
```

`RESET_FORMAT` takes `{d} {h} {m} {s}` padded to two digits and `{D} {H} {M} {S}` unpadded.

---

## Scheduled resets

The arena can paste a schematic back over itself on a timer instead of regenerating the world.

```yaml
RESET:
  ENABLED: true
  INTERVAL: '24h'
  WARNING_SECONDS: 30
  EVACUATE: true
  SCHEMATIC: 'arena'
  COMMANDS:
  - '//schematic load {schematic}'
  - '//paste -o -a'
  PASTE_DELAY_SECONDS: 5
```

Save the arena as a schematic with WorldEdit first, then point `SCHEMATIC` at it - the name only,
no file extension. `/pvp schematic load <name>` sets it from in game, and `/pvp schematic location`
records a paste position for configs that want one.

The reset runs the commands from console, in order, waiting `PASTE_DELAY_SECONDS` between each. That
gap matters: a large schematic is still loading when an immediate paste would fire. Because the work
is done through commands rather than a compiled dependency, the same two lines drive WorldEdit and
FastAsyncWorldEdit, and FAWE is worth installing for a big arena - it is far faster and much easier
on the tick.

`INTERVAL` accepts `1d10h15m`, `24h`, `30m`, `90s` and combinations of those. `/pvp reset` runs one
immediately.

---

## Blocked commands

Survival commands are switched off inside the arena.

```yaml
BLOCKED_COMMANDS:
  ENABLED: true
  COMMANDS:
  - 'shop'
  - 'sell'
  - 'ah'
  - 'ec'
  - 'enderchest'
  - 'orders'
  BLOCK_ENDER_CHEST_BLOCK: true
```

A blocked entry covers its subcommands, so `shop` also blocks `/shop sell`, and a plugin-qualified
form like `/otherplugin:shop` is caught too. `BLOCK_ENDER_CHEST_BLOCK` closes the other door into
the ender chest, the block itself. Holders of `ultimatedonutsmp.admin.pvp` are exempt from all of
it.

---

## Player commands

| Command | What it does |
| :--- | :--- |
| `/pvp` | Join the arena and pick a kit |
| `/pvp leave` | Leave and return to the lobby |
| `/pvp kit` | Reopen the kit menu |
| `/pvp stats [player]` | Elo, rank, level, XP, K/D and streaks |
| `/pvp top [amount]` | The Elo ladder, offline players included |
| `/pvp queue` | Open the ranked 1v1 queue |
| `/pvp queue leave` | Leave the queue |
| `/pvp leaderboard` | Open the leaderboards |
| `/pvp history [player]` | Browse ranked match history |

See [Config-pvp.yml](Config-pvp.yml) for every option, and
[Placeholders & Integrations](Placeholders-and-Integrations) for the `%pvp_*%` catalog.
