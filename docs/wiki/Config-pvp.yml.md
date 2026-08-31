# Detailed Configuration & Setup Guide: `pvp.yml`

This is the official, 100% complete technical setup guide for `pvp.yml` in **UltimateDonutSMP**.
Each section details the exact commented setup code block, allowed option values, data types, default values, and in-depth functional behavior.

For the walkthrough that puts these settings together, see [Ranked PvP Arena](Ranked-PvP-Arena).

---

## Section: `SETTINGS`

### 1. Commented Setup Code Example

```yaml
SETTINGS:
  # Enable or disable the ranked PvP arena globally (true / false)
  ENABLED: false
  # Seconds a player stays in spectator after dying before the kit menu reopens
  RESPAWN_DELAY_SECONDS: 3
  # Drop the kit items when a player dies in the arena (true / false)
  DROP_KIT_ON_DEATH: false
  # Restore full health, hunger and remove potion effects on every arena spawn (true / false)
  HEAL_ON_SPAWN: true
  # Kill and remove a player who leaves the arena boundary (true / false)
  KILL_OUTSIDE_BOUNDARY: true
  # Seconds of protection after spawning, during which the player cannot deal or take damage
  SPAWN_PROTECTION_SECONDS: 3
  # Send players who disconnect inside the arena to the lobby when they rejoin (true / false)
  LOBBY_ON_REJOIN: true
  # Take the kit back when a player leaves the arena (true / false)
  CLEAR_KIT_ON_LEAVE: true
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `SETTINGS.ENABLED` | `bool` | `true`, `false` | `false` | Global toggle. Ships off, because nothing works until `ARENA.SPAWN` is set and at least one kit exists. |
| `SETTINGS.RESPAWN_DELAY_SECONDS` | `int` | `0` and above | `3` | Seconds in spectator between dying and the kit menu reopening. The remaining count is sent to the player each second. |
| `SETTINGS.DROP_KIT_ON_DEATH` | `bool` | `true`, `false` | `false` | Leave this off and kit items never hit the floor, which is what keeps arena gear out of survival. |
| `SETTINGS.HEAL_ON_SPAWN` | `bool` | `true`, `false` | `true` | Restores health and hunger and clears potion effects each time a kit is taken. |
| `SETTINGS.KILL_OUTSIDE_BOUNDARY` | `bool` | `true`, `false` | `true` | Pulls a player out of the fight when they leave the boundary. Only has an effect once both wand corners are stored. |
| `SETTINGS.SPAWN_PROTECTION_SECONDS` | `int` | `0` and above | `3` | Window after spawning where the player can neither deal nor take damage. `0` disables it. |
| `SETTINGS.LOBBY_ON_REJOIN` | `bool` | `true`, `false` | `true` | A player who disconnects inside the arena is sent to the lobby on their next login instead of reappearing mid-fight. |
| `SETTINGS.CLEAR_KIT_ON_LEAVE` | `bool` | `true`, `false` | `true` | Empties the kit out on the way back to survival. Turn it off when Multiverse-Inventories already owns the arena world - clearing an inventory that plugin manages would undo its work rather than help it. |

---

## Section: `ARENA`

Written by `/pvp create`, `/pvp setspawn`, `/pvp setlobby` and `/pvp setboundary`. Editing it by hand
is supported but rarely needed, and the config updater never overwrites it.

### 1. Commented Setup Code Example

```yaml
ARENA:
  # Display name shown in messages and on the scoreboard
  NAME: 'Arena'
  # World the arena lives in. Empty means the world of the spawn point below.
  WORLD: ''
  # Where players are put when they join or respawn (world,x,y,z,yaw,pitch)
  SPAWN: ''
  # Where the second player in a ranked 1v1 match starts
  SPAWN_2: ''
  # Where players are sent on /pvp leave and after a disconnect (world,x,y,z,yaw,pitch)
  LOBBY: ''
  # The two wand corners of the arena boundary (world,x,y,z,yaw,pitch)
  BOUNDARY_POS1: ''
  BOUNDARY_POS2: ''
  # Extra blocks of slack around the boundary before a player counts as outside
  BOUNDARY_PADDING: 2
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `ARENA.NAME` | `string` | Any text | `'Arena'` | Shown by `%pvp_arena%`. Set with `/pvp create <name>`. |
| `ARENA.WORLD` | `string` | A loaded world name, or empty | `''` | Left empty the arena uses the world of `ARENA.SPAWN`, which is almost always right. |
| `ARENA.SPAWN` | `string` | `world,x,y,z,yaw,pitch` | `''` | The one required setting. Without it `/pvp` refuses to let anyone in. |
| `ARENA.SPAWN_2` | `string` | `world,x,y,z,yaw,pitch` | `''` | Where the second fighter in a ranked match starts. Empty puts both of them on `ARENA.SPAWN`. Set with `/pvp setspawn2`. |
| `ARENA.LOBBY` | `string` | `world,x,y,z,yaw,pitch` | `''` | Falls back to the configured server spawn when empty. |
| `ARENA.BOUNDARY_POS1` | `string` | `world,x,y,z,yaw,pitch` | `''` | First wand corner. With no corners the whole arena world counts as inside. |
| `ARENA.BOUNDARY_POS2` | `string` | `world,x,y,z,yaw,pitch` | `''` | Second wand corner. |
| `ARENA.BOUNDARY_PADDING` | `int` | `0` and above | `2` | Slack added around the box on every axis, so a player brushing the wall is not thrown out. |

---

## Section: `RESET`

### 1. Commented Setup Code Example

```yaml
RESET:
  # Enable the scheduled arena reset (true / false)
  ENABLED: false
  # How often the arena resets. Accepts 1d10h15m, 24h, 30m, 90s and combinations.
  INTERVAL: '24h'
  # Seconds of warning broadcast before the reset runs
  WARNING_SECONDS: 30
  # Teleport everyone in the arena to the lobby before pasting (true / false)
  EVACUATE: true
  # Schematic name as WorldEdit/FastAsyncWorldEdit knows it, without the file extension
  SCHEMATIC: 'arena'
  # Where the schematic is pasted. Empty pastes at the origin stored in the schematic itself.
  PASTE_LOCATION: ''
  COMMANDS:
  - '//schematic load {schematic}'
  - '//paste -o -a'
  # Seconds to wait between the load command and the paste command
  PASTE_DELAY_SECONDS: 5
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `RESET.ENABLED` | `bool` | `true`, `false` | `false` | Turns the timer on. `/pvp reset` still works while this is off. |
| `RESET.INTERVAL` | `string` | `1d10h15m`, `24h`, `30m`, `90s`, or a bare number of seconds | `'24h'` | Time between resets. The clock restarts when the plugin loads and after every reset. |
| `RESET.WARNING_SECONDS` | `int` | `0` and above | `30` | Broadcasts one warning this long before the reset. `0` sends none. |
| `RESET.EVACUATE` | `bool` | `true`, `false` | `true` | Sends everyone in the arena to the lobby before pasting, so nobody is buried by the schematic. |
| `RESET.SCHEMATIC` | `string` | A schematic name without its extension | `'arena'` | Substituted into `{schematic}` in the commands below. Set from in game with `/pvp schematic load <name>`. |
| `RESET.PASTE_LOCATION` | `string` | `world,x,y,z,yaw,pitch`, or empty | `''` | Fills `{world} {x} {y} {z}` in the commands. Empty relies on `-o`, which pastes at the origin stored in the schematic. |
| `RESET.COMMANDS` | `list` | Console commands | load + paste | Run in order from console. WorldEdit and FastAsyncWorldEdit answer the same two commands, so either works and neither needs to be listed as a dependency. |
| `RESET.PASTE_DELAY_SECONDS` | `int` | `1` and above | `5` | Wait between one command and the next. A large schematic is still loading when an immediate paste would fire. |

---

## Section: `MATCH`

### 1. Commented Setup Code Example

```yaml
MATCH:
  # Enable the ranked queue, /pvp assign and the match history (true / false)
  ENABLED: true
  # Seconds both players stand protected at the start of a match
  COUNTDOWN_SECONDS: 5
  # Longest a match may run before it is called a draw
  MAX_DURATION: '5m'
  # Elo the winner gains
  ELO_WIN: 20
  # Elo the loser drops
  ELO_LOSS: 15
  # Elo both players take on a draw or an aborted match
  ELO_DRAW: 0
  # Heal both players and re-hand the kit when a match starts (true / false)
  HEAL_ON_START: true
  # How the date is written in the match history
  DATE_FORMAT: 'dd/MM/yyyy HH:mm'
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `MATCH.ENABLED` | `bool` | `true`, `false` | `true` | Covers the queue, `/pvp assign` and the history menu. The open arena keeps working with this off. |
| `MATCH.COUNTDOWN_SECONDS` | `int` | `0` and above | `5` | Neither fighter can deal or take damage until it runs out. |
| `MATCH.MAX_DURATION` | `string` | Same format as `RESET.INTERVAL` | `'5m'` | A match still running at this point is called a draw. `0` lets it run forever. |
| `MATCH.ELO_WIN` | `int` | `0` and above | `20` | Added to the winner. |
| `MATCH.ELO_LOSS` | `int` | `0` and above | `15` | Taken from the loser. `ELO.MINIMUM` still applies, and the history stores the change that actually landed. |
| `MATCH.ELO_DRAW` | `int` | Any integer | `0` | Applied to both on a draw or an aborted match. |
| `MATCH.HEAL_ON_START` | `bool` | `true`, `false` | `true` | Full health and a fresh kit at the opening countdown. |
| `MATCH.DATE_FORMAT` | `string` | A Java date pattern | `'dd/MM/yyyy HH:mm'` | Used by the history menu. An unreadable pattern falls back to the default rather than breaking the menu. |

---

## Section: `MENUS`

Titles and sizes are yours; the slot layouts are fixed so a size change cannot leave a button with
nowhere to sit.

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `MENUS.QUEUE.TITLE` | `string` | Any text | `'&8PvP queue'` | Title of `/pvp queue`. |
| `MENUS.QUEUE.SIZE` | `int` | 27, 36, 45, 54 | `27` | Anything else falls back to 27. |
| `MENUS.QUEUE.CONFIRM.MATERIAL` | `string` | A material name | `LIME_STAINED_GLASS_PANE` | The join button. |
| `MENUS.QUEUE.CONFIRM.DISPLAY-NAME` | `string` | Any text | `'&aCONFIRM'` | |
| `MENUS.QUEUE.CANCEL.MATERIAL` | `string` | A material name | `RED_STAINED_GLASS_PANE` | Leaves the queue, or closes the menu when not queued. |
| `MENUS.QUEUE.CANCEL.DISPLAY-NAME` | `string` | Any text | `'&cLEAVE'` | |
| `MENUS.LEADERBOARD.TITLE` | `string` | Any text | `'&8Leaderboards'` | |
| `MENUS.LEADERBOARD.SIZE` | `int` | 27, 36, 45, 54 | `27` | |
| `MENUS.LEADERBOARD.ENTRIES` | `int` | 1-15 | `10` | How many players each board lists in its lore. |
| `MENUS.LEADERBOARD.LINE` | `string` | `{position}`, `{player}`, `{value}` | `'&7#{position} &f{player} &8- &c{value}'` | One ranking row. |
| `MENUS.LEADERBOARD.ICONS.<board>` | `string` | A material name | Varies | One per board: `ELO`, `LEVEL`, `KILLS`, `DEATHS`, `STREAK`, `JOINS`. |
| `MENUS.HISTORY.TITLE` | `string` | `{player}` | `'&8Match history &7- &f{player}'` | The history menu pages 45 matches at a time. |
| `MENUS.ASSIGN.TITLE` | `string` | Any text | `'&8Assign match'` | |
| `MENUS.ASSIGN.SIZE` | `int` | 27, 36, 45, 54 | `27` | |
| `MENUS.ASSIGN.CONFIRM.MATERIAL` | `string` | A material name | `LIME_STAINED_GLASS_PANE` | Shown once two different players and a kit are picked. |
| `MENUS.ASSIGN.CONFIRM.DISPLAY-NAME` | `string` | Any text | `'&aSTART MATCH'` | |
| `MENUS.ASSIGN.BLOCKED.MATERIAL` | `string` | A material name | `RED_STAINED_GLASS_PANE` | Shown while the selection is incomplete. |

---

## Section: `SYNC`

One-time codes players use to link their Minecraft account to the Discord bot. The bot reads this
table, checks the code and stores the link on its own side, so the plugin never has to know anybody's
Discord id and the bot never has to write to the server database.

### 1. Commented Setup Code Example

```yaml
SYNC:
  # Enable /pvp sync (true / false)
  ENABLED: true
  # How many characters a code has
  CODE_LENGTH: 6
  # How long a code stays valid
  EXPIRES: '10m'
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `SYNC.ENABLED` | `bool` | `true`, `false` | `true` | Turns `/pvp sync` on. Off, the command answers with `MESSAGES.SYNC_DISABLED`. |
| `SYNC.CODE_LENGTH` | `int` | `4`-`16` | `6` | Characters per code. The alphabet leaves out characters that read as each other, so a player copying one out of chat by eye does not get it wrong. |
| `SYNC.EXPIRES` | `string` | Same format as `RESET.INTERVAL` | `'10m'` | How long a code stays claimable. Asking for another replaces the one before it. |

---

## Section: `BLOCKED_COMMANDS`

### 1. Commented Setup Code Example

```yaml
BLOCKED_COMMANDS:
  # Enable command blocking inside the arena (true / false)
  ENABLED: true
  COMMANDS:
  - 'shop'
  - 'sell'
  - 'ah'
  - 'auctionhouse'
  - 'ec'
  - 'enderchest'
  - 'orders'
  # Also block the ender chest when it is opened by clicking the block (true / false)
  BLOCK_ENDER_CHEST_BLOCK: true
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `BLOCKED_COMMANDS.ENABLED` | `bool` | `true`, `false` | `true` | Master switch. `/pvp` itself is never blocked. |
| `BLOCKED_COMMANDS.COMMANDS` | `list` | Command labels, with or without the leading slash | shop, sell, ah, auctionhouse, ec, enderchest, orders | An entry covers its subcommands and its plugin-qualified form, so `shop` catches `/shop sell` and `/otherplugin:shop`. |
| `BLOCKED_COMMANDS.BLOCK_ENDER_CHEST_BLOCK` | `bool` | `true`, `false` | `true` | Closes the second route into the ender chest: right clicking the block. Holders of `ultimatedonutsmp.admin.pvp` bypass both. |

---

## Section: `ELO`

### 1. Commented Setup Code Example

```yaml
ELO:
  # Elo every player starts on
  STARTING: 0
  # Elo the killer gains
  GAIN_PER_KILL: 25
  # Elo the victim loses
  LOSS_PER_DEATH: 20
  # Elo can never fall below this
  MINIMUM: 0
  # Elo can never rise above this. 0 removes the cap.
  MAXIMUM: 0
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `ELO.STARTING` | `int` | `0` and above | `0` | Elo a player who has never fought is treated as having. |
| `ELO.GAIN_PER_KILL` | `int` | `0` and above | `25` | Added to the killer on a rewarded kill. |
| `ELO.LOSS_PER_DEATH` | `int` | `0` and above | `20` | Taken from the victim. Kill-farm protection can waive this; see `ANTI_KILL_FARMING.APPLY_ELO_LOSS`. |
| `ELO.MINIMUM` | `int` | `0` and above | `0` | Floor. Elo never drops below it. |
| `ELO.MAXIMUM` | `int` | `0` and above | `0` | Ceiling. `0` means no ceiling. |

---

## Section: `RANKS`

### 1. Commented Setup Code Example

```yaml
RANKS:
  LT5:
    DISPLAY: '&7LT5'
    ELO: 0
  HT5:
    DISPLAY: '&eHT5'
    ELO: 600
  HT1:
    DISPLAY: '&c&lHT1'
    ELO: 1500
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `RANKS.<id>` | `section` | Any id | LT5 through HT1 | Rank ids are free-form. Rename them, add tiers or remove some; nothing in the code expects the LT/HT naming. |
| `RANKS.<id>.DISPLAY` | `string` | Any text with colour codes | Varies | What `%pvp_rank%` renders. TAB reads this for the nametag. |
| `RANKS.<id>.ELO` | `int` | `0` and above | Varies | Elo needed to hold the rank. A player takes the highest rank they meet, so they drop back down on their own when Elo falls. |

---

## Section: `LEVELS`

### 1. Commented Setup Code Example

```yaml
LEVELS:
  # XP a rewarded kill is worth
  XP_PER_KILL: 50
  # XP needed to reach level 2
  BASE_XP: 100
  # Extra XP each further level costs on top of the previous one
  XP_INCREASE_PER_LEVEL: 50
  # Highest level a player can reach. 0 removes the cap.
  MAX_LEVEL: 100
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `LEVELS.XP_PER_KILL` | `int` | `0` and above | `50` | XP a rewarded kill hands out. Independent of Elo. |
| `LEVELS.BASE_XP` | `int` | `1` and above | `100` | Cost of level 2. |
| `LEVELS.XP_INCREASE_PER_LEVEL` | `int` | `0` and above | `50` | Added to the cost of each level after that, giving 100, 150, 200 with the defaults. `0` makes every level cost the same. |
| `LEVELS.MAX_LEVEL` | `int` | `0` and above | `100` | Ceiling. XP stops accumulating at the top. `0` removes the cap. |

---

## Section: `ANTI_KILL_FARMING`

### 1. Commented Setup Code Example

```yaml
ANTI_KILL_FARMING:
  # Enable kill-farm protection (true / false)
  ENABLED: true
  # Rewarded kills allowed against the same player before rewards drop
  MAX_REWARDED_KILLS: 3
  # How long the counter remembers a victim
  COOLDOWN: '5m'
  # Elo given for a kill past the limit
  REDUCED_ELO: 0
  # XP given for a kill past the limit
  REDUCED_XP: 0
  # Take Elo from the victim on an unrewarded kill (true / false)
  APPLY_ELO_LOSS: false
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `ANTI_KILL_FARMING.ENABLED` | `bool` | `true`, `false` | `true` | Master switch. Off, every kill pays in full. |
| `ANTI_KILL_FARMING.MAX_REWARDED_KILLS` | `int` | `0` and above | `3` | Rewarded kills allowed per victim inside the cooldown window. |
| `ANTI_KILL_FARMING.COOLDOWN` | `string` | Same format as `RESET.INTERVAL` | `'5m'` | How long the counter remembers a victim. A pair who go this long without a kill start fresh. |
| `ANTI_KILL_FARMING.REDUCED_ELO` | `int` | `0` and above | `0` | Elo paid once the limit is passed. |
| `ANTI_KILL_FARMING.REDUCED_XP` | `int` | `0` and above | `0` | XP paid once the limit is passed. |
| `ANTI_KILL_FARMING.APPLY_ELO_LOSS` | `bool` | `true`, `false` | `false` | Whether an unrewarded kill still costs the victim Elo. Off by default so a farmed player is not punished for it. |

> Tracking is per killer and victim UUID pair. Unrewarded kills still count toward kills, deaths, K/D and streaks, because they did happen - only the Elo and XP change.

---

## Section: `BROADCASTS`

### 1. Commented Setup Code Example

```yaml
BROADCASTS:
  LEVEL_UP:
    GLOBAL: true
    MESSAGE: '&8[&cPVP&8] &f%player_name% &7has reached PvP Level &c%pvp_level%&7!'
  RANK_UP:
    GLOBAL: true
    MESSAGE: '&8[&cPVP&8] &f%player_name% &7ranked up to %pvp_rank% &7with &c%pvp_elo% &7elo!'
  RANK_DOWN:
    GLOBAL: false
    MESSAGE: '&8[&cPVP&8] &f%player_name% &7dropped to %pvp_rank% &7with &c%pvp_elo% &7elo!'
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `BROADCASTS.LEVEL_UP.GLOBAL` | `bool` | `true`, `false` | `true` | `true` announces to the server, `false` only to the player. |
| `BROADCASTS.LEVEL_UP.MESSAGE` | `string` | Any text; empty disables | See above | Supports `%player_name%`, `%pvp_level%`, `%pvp_old_level%`, `%pvp_rank%`, `%pvp_old_rank%`, `%pvp_elo%`, `%pvp_xp%`. |
| `BROADCASTS.RANK_UP.GLOBAL` | `bool` | `true`, `false` | `true` | Rank promotions. |
| `BROADCASTS.RANK_UP.MESSAGE` | `string` | Any text; empty disables | See above | Same placeholder set. |
| `BROADCASTS.RANK_DOWN.GLOBAL` | `bool` | `true`, `false` | `false` | Demotions are personal by default. |
| `BROADCASTS.RANK_DOWN.MESSAGE` | `string` | Any text; empty disables | See above | Same placeholder set. |

> These placeholders are filled in by the arena itself, not by PlaceholderAPI, so a broadcast describes the player it is about rather than whoever is reading it - and it still reads correctly on a server with no PlaceholderAPI installed.

---

## Section: `SCOREBOARD`

### 1. Commented Setup Code Example

```yaml
SCOREBOARD:
  # Show the arena sidebar instead of the survival one (true / false)
  ENABLED: true
  TITLE:
  - '&c&lPVP ARENA'
  LINES:
  - ''
  - '&fPLAYER: &c%player_name%'
  - '&fRANK: %pvp_rank%'
  - '&fLEVEL: &c%pvp_level%'
  - '&fK/D: &c%pvp_kills%&7/&c%pvp_deaths%'
  - ''
  - '&fARENA RESET'
  - '&c%pvp_arena_reset%'
  # How the reset countdown is written
  RESET_FORMAT: '{d}D:{h}H:{m}M:{s}S'
  # Shown by %pvp_arena_reset% when no reset is scheduled
  RESET_DISABLED_TEXT: '&7-'
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `SCOREBOARD.ENABLED` | `bool` | `true`, `false` | `true` | Off, arena players keep the normal `scoreboard.yml` sidebar. |
| `SCOREBOARD.TITLE` | `list` | Colour-coded text | `'&c&lPVP ARENA'` | Animated frames, cycled at the survival sidebar's speed. |
| `SCOREBOARD.LINES` | `list` | Colour-coded text with any placeholders | See above | Rendered top to bottom. Empty strings are spacer lines. |
| `SCOREBOARD.RESET_FORMAT` | `string` | `{d} {h} {m} {s}` padded, `{D} {H} {M} {S}` unpadded | `'{d}D:{h}H:{m}M:{s}S'` | How `%pvp_arena_reset%` writes the countdown. |
| `SCOREBOARD.RESET_DISABLED_TEXT` | `string` | Any text | `'&7-'` | Shown when no reset is scheduled. |

---

## Section: `KITS`

Written by `/pvp kit create`, `/pvp kit edit` and the `/pvp kit` property commands, and excluded
from the config updater so a plugin update never restores the empty default over your kits.

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `KITS.<id>.DISPLAY` | `string` | Any text | `&f<id>` | Menu name. Set with `/pvp kit display <name> <text>`. |
| `KITS.<id>.ICON` | `string` | A material name | `IRON_SWORD` | Menu icon. Set with `/pvp kit icon <name> <material>`. |
| `KITS.<id>.PERMISSION` | `string` | A permission node, or empty | `''` | Empty leaves the kit open to everyone. `/pvp kit permission <name> none` clears it. |
| `KITS.<id>.SLOT` | `int` | `0`-`25`, or `-1` | `-1` | Fixed menu slot. `-1` places the kit automatically, centred with the others. Slot 26 belongs to the leave button, and a kit pointed at it is placed automatically instead. |
| `KITS.<id>.CONTENTS` | `section` | Serialized items by slot | – | The 36 inventory slots. Written by the editor. |
| `KITS.<id>.ARMOR` | `section` | Serialized items by slot | – | Boots, leggings, chestplate, helmet, in that order. |
| `KITS.<id>.OFFHAND` | `string` | A serialized item | – | Offhand item. |
| `KITS.<id>.EFFECTS` | `list` | `TYPE:amplifier:duration` | empty | Potion effects applied on spawn, for example `SPEED:1:600`. |

---

## Section: `MESSAGES`

Every player-facing string the arena sends. All of them go through the plugin's colour handling, so
`&a` and `&#RRGGBB` both work.

| Option / Key Path | Placeholders | Technical Function |
| :--- | :--- | :--- |
| `MESSAGES.DISABLED` | – | Sent when `SETTINGS.ENABLED` is off. |
| `MESSAGES.NOT_SET_UP` | – | Sent when `ARENA.SPAWN` is missing. |
| `MESSAGES.NO_KITS` | – | Sent when no kit is available to that player. |
| `MESSAGES.JOINED` / `MESSAGES.LEFT` | – | Entering and leaving. |
| `MESSAGES.ALREADY_IN` / `MESSAGES.NOT_IN` | – | Wrong-state responses. |
| `MESSAGES.KIT_GIVEN` | `{kit}` | Confirms a kit choice. |
| `MESSAGES.KIT_NO_PERMISSION` | – | Kit locked behind a permission. |
| `MESSAGES.RESPAWN_COUNTDOWN` | `{seconds}` | Sent once a second while dead. |
| `MESSAGES.BLOCKED_COMMAND` | – | Also used for a blocked ender chest. |
| `MESSAGES.LEFT_BOUNDARY` | – | Sent when a player is removed for leaving the arena. |
| `MESSAGES.KILL_REWARD` | `{victim}`, `{elo}`, `{xp}` | Sent to the killer. |
| `MESSAGES.DEATH_PENALTY` | `{killer}`, `{elo}` | Sent to the victim. |
| `MESSAGES.KILL_FARM_LIMIT` | `{victim}` | Sent instead of the reward line on an unrewarded kill. |
| `MESSAGES.LEVEL_UP` | `{level}` | Personal level-up line. |
| `MESSAGES.RANK_UP` / `MESSAGES.RANK_DOWN` | `{rank}` | Personal rank change lines. |
| `MESSAGES.RESET_WARNING` | `{time}` | Broadcast before a reset. |
| `MESSAGES.RESET_DONE` | – | Broadcast after a reset. |
| `MESSAGES.RESET_NO_SCHEMATIC` | – | `/pvp reset` with nothing configured. |
| `MESSAGES.STATS_HEADER` | `{player}` | `/pvp stats` header. |
| `MESSAGES.STATS_LINE` | `{label}`, `{value}` | Each `/pvp stats` row. |
| `MESSAGES.TOP_HEADER` | – | `/pvp top` header. |
| `MESSAGES.TOP_LINE` | `{position}`, `{player}`, `{elo}`, `{rank}` | Each `/pvp top` row. |
| `MESSAGES.TOP_EMPTY` | – | `/pvp top` with no records yet. |
| `MESSAGES.QUEUE_JOINED` | `{kit}` | Confirms a place in the ranked queue. |
| `MESSAGES.QUEUE_LEFT` | – | Left the queue. |
| `MESSAGES.QUEUE_ALREADY_IN` / `MESSAGES.QUEUE_NOT_IN` | – | Wrong-state queue responses. |
| `MESSAGES.MATCH_ALREADY_IN` | – | Already fighting a ranked match. |
| `MESSAGES.MATCH_LEAVE_ARENA_FIRST` | – | Queueing while inside the open arena. |
| `MESSAGES.MATCH_NEEDS_TWO` | – | `/pvp assign` with one player, or the same player twice. |
| `MESSAGES.MATCH_BUSY` | `{player}` | `/pvp assign` naming somebody who is already fighting in the open arena. |
| `MESSAGES.SYNC_DISABLED` | – | `/pvp sync` while `SYNC.ENABLED` is off. |
| `MESSAGES.SYNC_FAILED` | – | `/pvp sync` when the code could not be stored. |
| `MESSAGES.SYNC_HEADER` | – | First line of the sync code message. |
| `MESSAGES.SYNC_CODE` | `{code}` | The code itself. |
| `MESSAGES.SYNC_HINT` | `{code}` | What to do with it on Discord. |
| `MESSAGES.MATCH_STARTED` | `{first}`, `{second}` | Sent to both fighters when a match opens. |
| `MESSAGES.MATCH_COUNTDOWN` | `{seconds}` | Sent once a second during the opening countdown. |
| `MESSAGES.MATCH_WIN` / `MESSAGES.MATCH_LOSS` | `{opponent}`, `{elo}`, `{hits}`, `{crystals}` | The result lines. |
| `MESSAGES.MATCH_DRAW` | `{opponent}` | Sent when the duration cap runs out. |
