# Detailed Configuration & Setup Guide: `duels.yml`

This is the official, 100% complete technical setup guide for `duels.yml` in **UltimateDonutSMP**.
Each section details the exact commented setup code block, allowed option values, data types, default values, and in-depth functional behavior.

---

## Section: `SETTINGS`

### 1. Commented Setup Code Example

```yaml
SETTINGS:
  # Enable or disable the duels system globally (true / false)
  ENABLED: true
  # Countdown duration before match starts (in seconds)
  COUNTDOWN_SECONDS: 5
  # Maximum allowed match duration before forcing a draw (in seconds)
  MATCH_DURATION_SECONDS: 900
  # Time before an outgoing duel request expires (in seconds)
  REQUEST_TIMEOUT_SECONDS: 30
  # Time before a draw offer expires (in seconds)
  DRAW_REQUEST_TIMEOUT_SECONDS: 15
  # Delay before teleporting players back after match ends (in seconds)
  RETURN_DELAY_SECONDS: 3
  # Delay before returning winner (in seconds)
  WINNER_RETURN_DELAY_SECONDS: 3
  # Extra horizontal padding blocks preserved around duel arena during arena rollback
  ROLLBACK_PADDING_HORIZONTAL: 8
  # Extra vertical padding blocks preserved around duel arena during arena rollback
  ROLLBACK_PADDING_VERTICAL: 6

# Countdown titles and sound notifications
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `SETTINGS.ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `SETTINGS` system. Set to `true` to enable, `false` to disable. |
| `SETTINGS.COUNTDOWN_SECONDS` | `int` | Any valid integer number | `'5'` | Configures the technical `COUNTDOWN_SECONDS` parameter for `SETTINGS.COUNTDOWN_SECONDS` in `duels.yml`. |
| `SETTINGS.MATCH_DURATION_SECONDS` | `int` | Any valid integer number | `'900'` | Configures the technical `MATCH_DURATION_SECONDS` parameter for `SETTINGS.MATCH_DURATION_SECONDS` in `duels.yml`. |
| `SETTINGS.REQUEST_TIMEOUT_SECONDS` | `int` | Any valid integer number | `'30'` | Configures the technical `REQUEST_TIMEOUT_SECONDS` parameter for `SETTINGS.REQUEST_TIMEOUT_SECONDS` in `duels.yml`. |
| `SETTINGS.DRAW_REQUEST_TIMEOUT_SECONDS` | `int` | Any valid integer number | `'15'` | Configures the technical `DRAW_REQUEST_TIMEOUT_SECONDS` parameter for `SETTINGS.DRAW_REQUEST_TIMEOUT_SECONDS` in `duels.yml`. |
| `SETTINGS.RETURN_DELAY_SECONDS` | `int` | Any valid integer number | `'3'` | Configures the technical `RETURN_DELAY_SECONDS` parameter for `SETTINGS.RETURN_DELAY_SECONDS` in `duels.yml`. |
| `SETTINGS.WINNER_RETURN_DELAY_SECONDS` | `int` | Any valid integer number | `'3'` | Configures the technical `WINNER_RETURN_DELAY_SECONDS` parameter for `SETTINGS.WINNER_RETURN_DELAY_SECONDS` in `duels.yml`. |
| `SETTINGS.ROLLBACK_PADDING_HORIZONTAL` | `int` | Any valid integer number | `'8'` | Configures the technical `ROLLBACK_PADDING_HORIZONTAL` parameter for `SETTINGS.ROLLBACK_PADDING_HORIZONTAL` in `duels.yml`. |
| `SETTINGS.ROLLBACK_PADDING_VERTICAL` | `int` | Any valid integer number | `'6'` | Configures the technical `ROLLBACK_PADDING_VERTICAL` parameter for `SETTINGS.ROLLBACK_PADDING_VERTICAL` in `duels.yml`. |

### 3. Practical Setup Example

```yaml
SETTINGS:
  # Enable or disable the duels system globally (true / false)
  ENABLED: true
  # Countdown duration before match starts (in seconds)
  COUNTDOWN_SECONDS: 5
  # Maximum allowed match duration before forcing a draw (in seconds)
  MATCH_DURATION_SECONDS: 900
  # Time before an outgoing duel request expires (in seconds)
  REQUEST_TIMEOUT_SECONDS: 30
  # Time before a draw offer expires (in seconds)
  DRAW_REQUEST_TIMEOUT_SECONDS: 15
  # Delay before teleporting players back after match ends (in seconds)
  RETURN_DELAY_SECONDS: 3
  # Delay before returning winner (in seconds)
  WINNER_RETURN_DELAY_SECONDS: 3
  # Extra horizontal padding blocks preserved around duel arena during arena rollback
  ROLLBACK_PADDING_HORIZONTAL: 8
  # Extra vertical padding blocks preserved around duel arena during arena rollback
  ROLLBACK_PADDING_VERTICAL: 6

# Countdown titles and sound notifications
```

---

## Section: `START-COUNTDOWN`

### 1. Commented Setup Code Example

```yaml
START-COUNTDOWN:
  # Enable countdown messages and titles
  ENABLED: true
  SOUNDS:
    # Play tick sound effects during countdown
    ENABLED: true
  TITLES:
    6: ''
    5: '&e5'
    4: '&e4'
    3: '&c3'
    2: '&c2'
    1: '&c1'
    0: '&a&lFight!'
  MESSAGES:
    '5': '&a5'
    '4': '&a4'
    '3': '&a3'
    '2': '&a2'
    '1': '&a1'
  START-MESSAGE: '&aMatch Started!'

# End match screen titles and subtitles
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `START-COUNTDOWN.ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `START-COUNTDOWN` system. Set to `true` to enable, `false` to disable. |
| `START-COUNTDOWN.SOUNDS.ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `START-COUNTDOWN` system. Set to `true` to enable, `false` to disable. |
| `START-COUNTDOWN.TITLES.6` | `str` | Any string text | `''` | Configures the technical `6` parameter for `START-COUNTDOWN.TITLES.6` in `duels.yml`. |
| `START-COUNTDOWN.TITLES.5` | `str` | Any string text | `'&e5'` | Configures the technical `5` parameter for `START-COUNTDOWN.TITLES.5` in `duels.yml`. |
| `START-COUNTDOWN.TITLES.4` | `str` | Any string text | `'&e4'` | Configures the technical `4` parameter for `START-COUNTDOWN.TITLES.4` in `duels.yml`. |
| `START-COUNTDOWN.TITLES.3` | `str` | Any string text | `'&c3'` | Configures the technical `3` parameter for `START-COUNTDOWN.TITLES.3` in `duels.yml`. |
| `START-COUNTDOWN.TITLES.2` | `str` | Any string text | `'&c2'` | Configures the technical `2` parameter for `START-COUNTDOWN.TITLES.2` in `duels.yml`. |
| `START-COUNTDOWN.TITLES.1` | `str` | Any string text | `'&c1'` | Configures the technical `1` parameter for `START-COUNTDOWN.TITLES.1` in `duels.yml`. |
| `START-COUNTDOWN.TITLES.0` | `str` | Any string text | `'&a&lFight!'` | Configures the technical `0` parameter for `START-COUNTDOWN.TITLES.0` in `duels.yml`. |
| `START-COUNTDOWN.MESSAGES.5` | `str` | Any string text | `'&a5'` | Configures the technical `5` parameter for `START-COUNTDOWN.MESSAGES.5` in `duels.yml`. |
| `START-COUNTDOWN.MESSAGES.4` | `str` | Any string text | `'&a4'` | Configures the technical `4` parameter for `START-COUNTDOWN.MESSAGES.4` in `duels.yml`. |
| `START-COUNTDOWN.MESSAGES.3` | `str` | Any string text | `'&a3'` | Configures the technical `3` parameter for `START-COUNTDOWN.MESSAGES.3` in `duels.yml`. |
| `START-COUNTDOWN.MESSAGES.2` | `str` | Any string text | `'&a2'` | Configures the technical `2` parameter for `START-COUNTDOWN.MESSAGES.2` in `duels.yml`. |
| `START-COUNTDOWN.MESSAGES.1` | `str` | Any string text | `'&a1'` | Configures the technical `1` parameter for `START-COUNTDOWN.MESSAGES.1` in `duels.yml`. |
| `START-COUNTDOWN.START-MESSAGE` | `str` | Any string text | `'&aMatch Started!'` | Configures the technical `START-MESSAGE` parameter for `START-COUNTDOWN.START-MESSAGE` in `duels.yml`. |

### 3. Practical Setup Example

```yaml
START-COUNTDOWN:
  # Enable countdown messages and titles
  ENABLED: true
  SOUNDS:
    # Play tick sound effects during countdown
    ENABLED: true
  TITLES:
    6: ''
    5: '&e5'
    4: '&e4'
    3: '&c3'
    2: '&c2'
    1: '&c1'
    0: '&a&lFight!'
  MESSAGES:
    '5': '&a5'
    '4': '&a4'
    '3': '&a3'
    '2': '&a2'
    '1': '&a1'
  START-MESSAGE: '&aMatch Started!'

# End match screen titles and subtitles
```

---

## Section: `RESULT-TITLES`

### 1. Commented Setup Code Example

```yaml
RESULT-TITLES:
  victory:
    title: '&e&lVICTORY!'
    subtitle: '&e<player> &fwon the Match!'
  defeat:
    title: '&c&lDEFEAT!'
    subtitle: '&c<opponent> &fwon this Match!'
  draw:
    title: '&e&lDRAW!'
    subtitle: '&fTime''s up - no winner.'
    message: '&e[Timer] &fTime limit reached! Match ended as a &eDRAW &f- streaks unchanged.'

# Command restrictions during a duel
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `RESULT-TITLES.victory.title` | `str` | Any string text | `'&e&lVICTORY!'` | Configures the technical `title` parameter for `RESULT-TITLES.victory.title` in `duels.yml`. |
| `RESULT-TITLES.victory.subtitle` | `str` | Any string text | `'&e<player> &fwon the Match!'` | Configures the technical `subtitle` parameter for `RESULT-TITLES.victory.subtitle` in `duels.yml`. |
| `RESULT-TITLES.defeat.title` | `str` | Any string text | `'&c&lDEFEAT!'` | Configures the technical `title` parameter for `RESULT-TITLES.defeat.title` in `duels.yml`. |
| `RESULT-TITLES.defeat.subtitle` | `str` | Any string text | `'&c<opponent> &fwon this Match!'` | Configures the technical `subtitle` parameter for `RESULT-TITLES.defeat.subtitle` in `duels.yml`. |
| `RESULT-TITLES.draw.title` | `str` | Any string text | `'&e&lDRAW!'` | Configures the technical `title` parameter for `RESULT-TITLES.draw.title` in `duels.yml`. |
| `RESULT-TITLES.draw.subtitle` | `str` | Any string text | `'&fTime's up - no winner.'` | Configures the technical `subtitle` parameter for `RESULT-TITLES.draw.subtitle` in `duels.yml`. |
| `RESULT-TITLES.draw.message` | `str` | Any string text | `'&e[Timer] &fTime limit reached! Mat...'` | Configures the technical `message` parameter for `RESULT-TITLES.draw.message` in `duels.yml`. |

### 3. Practical Setup Example

```yaml
RESULT-TITLES:
  victory:
    title: '&e&lVICTORY!'
    subtitle: '&e<player> &fwon the Match!'
  defeat:
    title: '&c&lDEFEAT!'
    subtitle: '&c<opponent> &fwon this Match!'
  draw:
    title: '&e&lDRAW!'
    subtitle: '&fTime''s up - no winner.'
    message: '&e[Timer] &fTime limit reached! Match ended as a &eDRAW &f- streaks unchanged.'

# Command restrictions during a duel
```

---

## Section: `COMMAND_BLOCK`

### 1. Commented Setup Code Example

```yaml
COMMAND_BLOCK:
  # Enable command blocking during a duel match (true / false)
  ENABLED: true
  # Filtering mode: ALLOWLIST (only specified commands allowed) or BLOCKLIST (specified commands blocked)
  MODE: ALLOWLIST
  # List of commands allowed (or blocked depending on MODE) during a duel match
  COMMANDS:
    - "/duel"
    - "/draw"
    - "/leave"
    - "/queue"
  # Message shown to players attempting blocked commands
  MESSAGE: "&cYou cannot use that command during a duel."

# World Border settings applied during dynamic random biome duel matches
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `COMMAND_BLOCK.ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `COMMAND_BLOCK` system. Set to `true` to enable, `false` to disable. |
| `COMMAND_BLOCK.MODE` | `str` | Any string text | `'ALLOWLIST'` | Configures the technical `MODE` parameter for `COMMAND_BLOCK.MODE` in `duels.yml`. |
| `COMMAND_BLOCK.COMMANDS` | `list` | List of configured items/strings | `[/duel, /draw, /leave...]` | Configures the technical `COMMANDS` parameter for `COMMAND_BLOCK.COMMANDS` in `duels.yml`. |
| `COMMAND_BLOCK.MESSAGE` | `str` | Any string text | `'&cYou cannot use that command durin...'` | Configures the technical `MESSAGE` parameter for `COMMAND_BLOCK.MESSAGE` in `duels.yml`. |

### 3. Practical Setup Example

```yaml
COMMAND_BLOCK:
  # Enable command blocking during a duel match (true / false)
  ENABLED: true
  # Filtering mode: ALLOWLIST (only specified commands allowed) or BLOCKLIST (specified commands blocked)
  MODE: ALLOWLIST
  # List of commands allowed (or blocked depending on MODE) during a duel match
  COMMANDS:
    - "/duel"
    - "/draw"
    - "/leave"
    - "/queue"
  # Message shown to players attempting blocked commands
  MESSAGE: "&cYou cannot use that command during a duel."

# World Border settings applied during dynamic random biome duel matches
```

---

## Section: `WORLDBORDER`

### 1. Commented Setup Code Example

```yaml
WORLDBORDER:
  # Enable world border restrictions during duel matches (true / false)
  ENABLED: true
  # Size/diameter of the world border in blocks
  SIZE: 96.0
  # Safe buffer zone size in blocks before border damage is applied
  DAMAGE_BUFFER: 0.0
  # Distance in blocks from border to display border warning vignette
  WARNING_DISTANCE: 4
  # Time in seconds for border warning pulse animation
  WARNING_TIME: 5
  # Grace period in ticks before penalizing a player outside the border
  ESCAPE_GRACE_TICKS: 40
  # Action to take when a player steps outside: PUSH_BACK, TELEPORT, or DAMAGE
  ACTION: PUSH_BACK
  # Fallback action if push back fails: FORFEIT or KILL
  FALLBACK_ACTION: FORFEIT

# Arena and World Sources configuration
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `WORLDBORDER.ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `WORLDBORDER` system. Set to `true` to enable, `false` to disable. |
| `WORLDBORDER.SIZE` | `float` | Any decimal number | `'96.0'` | Configures the technical `SIZE` parameter for `WORLDBORDER.SIZE` in `duels.yml`. |
| `WORLDBORDER.DAMAGE_BUFFER` | `float` | Any decimal number | `'0.0'` | Configures the technical `DAMAGE_BUFFER` parameter for `WORLDBORDER.DAMAGE_BUFFER` in `duels.yml`. |
| `WORLDBORDER.WARNING_DISTANCE` | `int` | Any valid integer number | `'4'` | Configures the technical `WARNING_DISTANCE` parameter for `WORLDBORDER.WARNING_DISTANCE` in `duels.yml`. |
| `WORLDBORDER.WARNING_TIME` | `int` | Any valid integer number | `'5'` | Configures the technical `WARNING_TIME` parameter for `WORLDBORDER.WARNING_TIME` in `duels.yml`. |
| `WORLDBORDER.ESCAPE_GRACE_TICKS` | `int` | Any valid integer number | `'40'` | Configures the technical `ESCAPE_GRACE_TICKS` parameter for `WORLDBORDER.ESCAPE_GRACE_TICKS` in `duels.yml`. |
| `WORLDBORDER.ACTION` | `str` | Any string text | `'PUSH_BACK'` | Configures the technical `ACTION` parameter for `WORLDBORDER.ACTION` in `duels.yml`. |
| `WORLDBORDER.FALLBACK_ACTION` | `str` | Any string text | `'FORFEIT'` | Configures the technical `FALLBACK_ACTION` parameter for `WORLDBORDER.FALLBACK_ACTION` in `duels.yml`. |

### 3. Practical Setup Example

```yaml
WORLDBORDER:
  # Enable world border restrictions during duel matches (true / false)
  ENABLED: true
  # Size/diameter of the world border in blocks
  SIZE: 96.0
  # Safe buffer zone size in blocks before border damage is applied
  DAMAGE_BUFFER: 0.0
  # Distance in blocks from border to display border warning vignette
  WARNING_DISTANCE: 4
  # Time in seconds for border warning pulse animation
  WARNING_TIME: 5
  # Grace period in ticks before penalizing a player outside the border
  ESCAPE_GRACE_TICKS: 40
  # Action to take when a player steps outside: PUSH_BACK, TELEPORT, or DAMAGE
  ACTION: PUSH_BACK
  # Fallback action if push back fails: FORFEIT or KILL
  FALLBACK_ACTION: FORFEIT

# Arena and World Sources configuration
```

---

## Section: `MAP_SOURCES`

### 1. Commented Setup Code Example

```yaml
MAP_SOURCES:
  # Configuration for pre-built static arenas in existing server worlds
  STATIC_WORLDS:
    # Enable static world duel arenas (true / false)
    ENABLED: true
    # Automatically load configured static duel worlds on server startup (true / false)
    AUTO_LOAD: true
    # List of world names containing static duel arenas (e.g., ["world_duels"])
    WORLDS: []

  # Configuration for dynamic auto-generated random biome duel worlds
  RANDOM_BIOMES:
    # Enable auto-generating random biome duel worlds for matches (true / false)
    ENABLED: true
    # Terrain generation mode: FLAT (superflat with biome theme) or VANILLA (natural terrain)
    TERRAIN_MODE: FLAT
    # Whether to generate structures (villages, fortresses) in duel worlds (true / false)
    GENERATE_STRUCTURES: false
    # Automatically unload and delete generated duel worlds after match ends (true / false)
    CLEANUP_AFTER_MATCH: true
    # Radius of arena play zone in blocks
    ARENA_RADIUS: 48
    # Distance between player spawn points in blocks
    SPAWN_DISTANCE: 16
    # Search radius when scanning for safe spawn points on vanilla terrain
    SPAWN_SEARCH_RADIUS: 16
    # World name prefix for auto-generated duel worlds
    WORLD_PREFIX: duel_biome_
    # Subfolder name for generated duel world files
    WORLD_FOLDER: duel
    # Whitelist of biome keys allowed for selection (empty [] = all biomes allowed)
    ALLOWLIST: []
    # Blacklist of biome keys excluded from selection
    EXCLUDE: []

    # Pre-prepared world pool settings for FLAT terrain mode
    FLAT_POOL:
      # Enable pre-generating flat duel worlds in advance (true / false)
      ENABLED: true
      # Recycle and reuse clean flat worlds for subsequent matches (true / false)
      REUSE_WORLDS: true
      # Number of pre-prepared flat worlds to keep ready in pool
      SIZE: 2
      # Interval in ticks between pool preparation checks
      PREPARE_INTERVAL_TICKS: 20

    # Pre-prepared world pool settings for VANILLA terrain mode
    VANILLA_POOL:
      # Enable pre-generating vanilla terrain duel worlds (true / false)
      ENABLED: true
      # Allow background chunk generation for vanilla terrain (true / false)
      RUNTIME_GENERATION: true
      # Number of pre-prepared vanilla worlds to keep ready in pool
      SIZE: 2
      # Chunks generated per tick to prevent server lag
      CHUNKS_PER_TICK: 1
      # Interval in ticks between vanilla pool preparation ticks
      PREPARE_INTERVAL_TICKS: 1
      # Maximum allowed time in milliseconds per sync preparation step before pausing
      MAX_SYNC_STEP_MS: 2000
      # Pause background chunk generation if a step exceeds MAX_SYNC_STEP_MS (true / false)
      PAUSE_ON_SLOW_STEP: true
      # Maximum percentage of the arena allowed to be water before it is regenerated (100 = allow any)
      MAX_WATER_PERCENT: 40
      # How many times a too-watery arena is regenerated before it is used anyway
      MAX_TERRAIN_ATTEMPTS: 5

# Cross-server BungeeCord / Velocity Redis sync settings
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `MAP_SOURCES.STATIC_WORLDS.ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `MAP_SOURCES` system. Set to `true` to enable, `false` to disable. |
| `MAP_SOURCES.STATIC_WORLDS.AUTO_LOAD` | `bool` | `true`, `false` | `true` | Configures the technical `AUTO_LOAD` parameter for `MAP_SOURCES.STATIC_WORLDS.AUTO_LOAD` in `duels.yml`. |
| `MAP_SOURCES.STATIC_WORLDS.WORLDS` | `list` | List of configured items/strings | `[]` | Configures the technical `WORLDS` parameter for `MAP_SOURCES.STATIC_WORLDS.WORLDS` in `duels.yml`. |
| `MAP_SOURCES.RANDOM_BIOMES.ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `MAP_SOURCES` system. Set to `true` to enable, `false` to disable. |
| `MAP_SOURCES.RANDOM_BIOMES.TERRAIN_MODE` | `str` | Any string text | `'FLAT'` | Configures the technical `TERRAIN_MODE` parameter for `MAP_SOURCES.RANDOM_BIOMES.TERRAIN_MODE` in `duels.yml`. |
| `MAP_SOURCES.RANDOM_BIOMES.GENERATE_STRUCTURES` | `bool` | `true`, `false` | `false` | Configures the technical `GENERATE_STRUCTURES` parameter for `MAP_SOURCES.RANDOM_BIOMES.GENERATE_STRUCTURES` in `duels.yml`. |
| `MAP_SOURCES.RANDOM_BIOMES.CLEANUP_AFTER_MATCH` | `bool` | `true`, `false` | `true` | Configures the technical `CLEANUP_AFTER_MATCH` parameter for `MAP_SOURCES.RANDOM_BIOMES.CLEANUP_AFTER_MATCH` in `duels.yml`. |
| `MAP_SOURCES.RANDOM_BIOMES.ARENA_RADIUS` | `int` | Any valid integer number | `'48'` | Configures the technical `ARENA_RADIUS` parameter for `MAP_SOURCES.RANDOM_BIOMES.ARENA_RADIUS` in `duels.yml`. |
| `MAP_SOURCES.RANDOM_BIOMES.SPAWN_DISTANCE` | `int` | Any valid integer number | `'16'` | Configures the technical `SPAWN_DISTANCE` parameter for `MAP_SOURCES.RANDOM_BIOMES.SPAWN_DISTANCE` in `duels.yml`. |
| `MAP_SOURCES.RANDOM_BIOMES.SPAWN_SEARCH_RADIUS` | `int` | Any valid integer number | `'16'` | Configures the technical `SPAWN_SEARCH_RADIUS` parameter for `MAP_SOURCES.RANDOM_BIOMES.SPAWN_SEARCH_RADIUS` in `duels.yml`. |
| `MAP_SOURCES.RANDOM_BIOMES.WORLD_PREFIX` | `str` | Any string text | `'duel_biome_'` | Configures the technical `WORLD_PREFIX` parameter for `MAP_SOURCES.RANDOM_BIOMES.WORLD_PREFIX` in `duels.yml`. |
| `MAP_SOURCES.RANDOM_BIOMES.WORLD_FOLDER` | `str` | Any string text | `'duel'` | Configures the technical `WORLD_FOLDER` parameter for `MAP_SOURCES.RANDOM_BIOMES.WORLD_FOLDER` in `duels.yml`. |
| `MAP_SOURCES.RANDOM_BIOMES.ALLOWLIST` | `list` | List of configured items/strings | `[]` | Configures the technical `ALLOWLIST` parameter for `MAP_SOURCES.RANDOM_BIOMES.ALLOWLIST` in `duels.yml`. |
| `MAP_SOURCES.RANDOM_BIOMES.EXCLUDE` | `list` | List of configured items/strings | `[]` | Configures the technical `EXCLUDE` parameter for `MAP_SOURCES.RANDOM_BIOMES.EXCLUDE` in `duels.yml`. |
| `MAP_SOURCES.RANDOM_BIOMES.FLAT_POOL.ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `MAP_SOURCES` system. Set to `true` to enable, `false` to disable. |
| `MAP_SOURCES.RANDOM_BIOMES.FLAT_POOL.REUSE_WORLDS` | `bool` | `true`, `false` | `true` | Configures the technical `REUSE_WORLDS` parameter for `MAP_SOURCES.RANDOM_BIOMES.FLAT_POOL.REUSE_WORLDS` in `duels.yml`. |
| `MAP_SOURCES.RANDOM_BIOMES.FLAT_POOL.SIZE` | `int` | Any valid integer number | `'2'` | Configures the technical `SIZE` parameter for `MAP_SOURCES.RANDOM_BIOMES.FLAT_POOL.SIZE` in `duels.yml`. |
| `MAP_SOURCES.RANDOM_BIOMES.FLAT_POOL.PREPARE_INTERVAL_TICKS` | `int` | Any valid integer number | `'20'` | Configures the technical `PREPARE_INTERVAL_TICKS` parameter for `MAP_SOURCES.RANDOM_BIOMES.FLAT_POOL.PREPARE_INTERVAL_TICKS` in `duels.yml`. |
| `MAP_SOURCES.RANDOM_BIOMES.VANILLA_POOL.ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `MAP_SOURCES` system. Set to `true` to enable, `false` to disable. |
| `MAP_SOURCES.RANDOM_BIOMES.VANILLA_POOL.RUNTIME_GENERATION` | `bool` | `true`, `false` | `true` | Configures the technical `RUNTIME_GENERATION` parameter for `MAP_SOURCES.RANDOM_BIOMES.VANILLA_POOL.RUNTIME_GENERATION` in `duels.yml`. |
| `MAP_SOURCES.RANDOM_BIOMES.VANILLA_POOL.SIZE` | `int` | Any valid integer number | `'2'` | Configures the technical `SIZE` parameter for `MAP_SOURCES.RANDOM_BIOMES.VANILLA_POOL.SIZE` in `duels.yml`. |
| `MAP_SOURCES.RANDOM_BIOMES.VANILLA_POOL.CHUNKS_PER_TICK` | `int` | Any valid integer number | `'1'` | Configures the technical `CHUNKS_PER_TICK` parameter for `MAP_SOURCES.RANDOM_BIOMES.VANILLA_POOL.CHUNKS_PER_TICK` in `duels.yml`. |
| `MAP_SOURCES.RANDOM_BIOMES.VANILLA_POOL.PREPARE_INTERVAL_TICKS` | `int` | Any valid integer number | `'1'` | Configures the technical `PREPARE_INTERVAL_TICKS` parameter for `MAP_SOURCES.RANDOM_BIOMES.VANILLA_POOL.PREPARE_INTERVAL_TICKS` in `duels.yml`. |
| `MAP_SOURCES.RANDOM_BIOMES.VANILLA_POOL.MAX_SYNC_STEP_MS` | `int` | Any valid integer number | `'2000'` | Configures the technical `MAX_SYNC_STEP_MS` parameter for `MAP_SOURCES.RANDOM_BIOMES.VANILLA_POOL.MAX_SYNC_STEP_MS` in `duels.yml`. |
| `MAP_SOURCES.RANDOM_BIOMES.VANILLA_POOL.PAUSE_ON_SLOW_STEP` | `bool` | `true`, `false` | `true` | Configures the technical `PAUSE_ON_SLOW_STEP` parameter for `MAP_SOURCES.RANDOM_BIOMES.VANILLA_POOL.PAUSE_ON_SLOW_STEP` in `duels.yml`. |
| `MAP_SOURCES.RANDOM_BIOMES.VANILLA_POOL.MAX_WATER_PERCENT` | `int` | `0` - `100` | `'40'` | Share of the arena that is allowed to be water. A freshly generated arena above this share is thrown away and generated again on a new seed, which is what keeps duels off the middle of an ocean. Set it to `100` to accept whatever the terrain gives you. |
| `MAP_SOURCES.RANDOM_BIOMES.VANILLA_POOL.MAX_TERRAIN_ATTEMPTS` | `int` | `1` - `20` | `'5'` | How many times a too-watery arena is regenerated before the last one is used regardless. Stops a narrow biome pool from generating chunks forever, and logs a warning when it happens. |

### 3. Practical Setup Example

```yaml
MAP_SOURCES:
  # Configuration for pre-built static arenas in existing server worlds
  STATIC_WORLDS:
    # Enable static world duel arenas (true / false)
    ENABLED: true
    # Automatically load configured static duel worlds on server startup (true / false)
    AUTO_LOAD: true
    # List of world names containing static duel arenas (e.g., ["world_duels"])
    WORLDS: []

  # Configuration for dynamic auto-generated random biome duel worlds
  RANDOM_BIOMES:
    # Enable auto-generating random biome duel worlds for matches (true / false)
    ENABLED: true
    # Terrain generation mode: FLAT (superflat with biome theme) or VANILLA (natural terrain)
    TERRAIN_MODE: FLAT
    # Whether to generate structures (villages, fortresses) in duel worlds (true / false)
    GENERATE_STRUCTURES: false
    # Automatically unload and delete generated duel worlds after match ends (true / false)
    CLEANUP_AFTER_MATCH: true
    # Radius of arena play zone in blocks
    ARENA_RADIUS: 48
    # Distance
```

---

## Section: `CROSS_SERVER`

### 1. Commented Setup Code Example

```yaml
CROSS_SERVER:
  # Enable cross-server duel matchmaking (true / false)
  ENABLED: false
  # Unique identifier for this local server instance
  LOCAL_SERVER_ID: ""
  # Redis channel for duel match communications
  REDIS_CHANNEL: "ultimatedonutsmp:duels"
  # Redis key prefix for duel data
  KEY_PREFIX: "uds:duels:"
  # Stale queue request timeout (in seconds)
  STALE_QUEUE_TIMEOUT_SECONDS: 45
  # Player proxy transfer timeout (in seconds)
  TRANSFER_TIMEOUT_SECONDS: 20
  # Proxy server target name
  PROXY_SERVER_NAME: ""
  # List of server IDs allowed for duel matchmaking queues
  ALLOWED_QUEUE_SERVERS: []
  # List of server IDs allowed to host duel matches
  ALLOWED_MATCH_SERVERS: []

# Configuration for static arena definitions (managed via /arena commands)
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `CROSS_SERVER.ENABLED` | `bool` | `true`, `false` | `false` | Global toggle for `CROSS_SERVER` system. Set to `true` to enable, `false` to disable. |
| `CROSS_SERVER.LOCAL_SERVER_ID` | `str` | Any string text | `''` | Configures the technical `LOCAL_SERVER_ID` parameter for `CROSS_SERVER.LOCAL_SERVER_ID` in `duels.yml`. |
| `CROSS_SERVER.REDIS_CHANNEL` | `str` | Any string text | `'ultimatedonutsmp:duels'` | Configures the technical `REDIS_CHANNEL` parameter for `CROSS_SERVER.REDIS_CHANNEL` in `duels.yml`. |
| `CROSS_SERVER.KEY_PREFIX` | `str` | Any string text | `'uds:duels:'` | Configures the technical `KEY_PREFIX` parameter for `CROSS_SERVER.KEY_PREFIX` in `duels.yml`. |
| `CROSS_SERVER.STALE_QUEUE_TIMEOUT_SECONDS` | `int` | Any valid integer number | `'45'` | Configures the technical `STALE_QUEUE_TIMEOUT_SECONDS` parameter for `CROSS_SERVER.STALE_QUEUE_TIMEOUT_SECONDS` in `duels.yml`. |
| `CROSS_SERVER.TRANSFER_TIMEOUT_SECONDS` | `int` | Any valid integer number | `'20'` | Configures the technical `TRANSFER_TIMEOUT_SECONDS` parameter for `CROSS_SERVER.TRANSFER_TIMEOUT_SECONDS` in `duels.yml`. |
| `CROSS_SERVER.PROXY_SERVER_NAME` | `str` | Any string text | `''` | Configures the technical `PROXY_SERVER_NAME` parameter for `CROSS_SERVER.PROXY_SERVER_NAME` in `duels.yml`. |
| `CROSS_SERVER.ALLOWED_QUEUE_SERVERS` | `list` | List of configured items/strings | `[]` | Configures the technical `ALLOWED_QUEUE_SERVERS` parameter for `CROSS_SERVER.ALLOWED_QUEUE_SERVERS` in `duels.yml`. |
| `CROSS_SERVER.ALLOWED_MATCH_SERVERS` | `list` | List of configured items/strings | `[]` | Configures the technical `ALLOWED_MATCH_SERVERS` parameter for `CROSS_SERVER.ALLOWED_MATCH_SERVERS` in `duels.yml`. |

### 3. Practical Setup Example

```yaml
CROSS_SERVER:
  # Enable cross-server duel matchmaking (true / false)
  ENABLED: false
  # Unique identifier for this local server instance
  LOCAL_SERVER_ID: ""
  # Redis channel for duel match communications
  REDIS_CHANNEL: "ultimatedonutsmp:duels"
  # Redis key prefix for duel data
  KEY_PREFIX: "uds:duels:"
  # Stale queue request timeout (in seconds)
  STALE_QUEUE_TIMEOUT_SECONDS: 45
  # Player proxy transfer timeout (in seconds)
  TRANSFER_TIMEOUT_SECONDS: 20
  # Proxy server target name
  PROXY_SERVER_NAME: ""
  # List of server IDs allowed for duel matchmaking queues
  ALLOWED_QUEUE_SERVERS: []
  # List of server IDs allowed to host duel matches
  ALLOWED_MATCH_SERVERS: []

# Configuration for static arena definitions (managed via /arena commands)
```

---

## Section: `ARENA_SETTINGS`

### 1. Commented Setup Code Example

```yaml
ARENA_SETTINGS: {}

# GUI Inventory Titles and Sizes
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |

### 3. Practical Setup Example

```yaml
ARENA_SETTINGS: {}

# GUI Inventory Titles and Sizes
```

---

## Section: `GUI`

### 1. Commented Setup Code Example

```yaml
GUI:
  QUEUE:
    TITLE: '&8Casual Queue'
    SIZE: 27
  CREATE:
    TITLE: '&8Create Duel -> {player}'
    SIZE: 27
  CLAIMS:
    TITLE: '&8Duel Claims'
    SIZE: 54
    ITEMS_PER_PAGE: 45
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `GUI.QUEUE.TITLE` | `str` | Any string text | `'&8Casual Queue'` | Configures the technical `TITLE` parameter for `GUI.QUEUE.TITLE` in `duels.yml`. |
| `GUI.QUEUE.SIZE` | `int` | Any valid integer number | `'27'` | Configures the technical `SIZE` parameter for `GUI.QUEUE.SIZE` in `duels.yml`. |
| `GUI.CREATE.TITLE` | `str` | Any string text | `'&8Create Duel -> {player}'` | Configures the technical `TITLE` parameter for `GUI.CREATE.TITLE` in `duels.yml`. |
| `GUI.CREATE.SIZE` | `int` | Any valid integer number | `'27'` | Configures the technical `SIZE` parameter for `GUI.CREATE.SIZE` in `duels.yml`. |
| `GUI.CLAIMS.TITLE` | `str` | Any string text | `'&8Duel Claims'` | Configures the technical `TITLE` parameter for `GUI.CLAIMS.TITLE` in `duels.yml`. |
| `GUI.CLAIMS.SIZE` | `int` | Any valid integer number | `'54'` | Configures the technical `SIZE` parameter for `GUI.CLAIMS.SIZE` in `duels.yml`. |
| `GUI.CLAIMS.ITEMS_PER_PAGE` | `int` | Any valid integer number | `'45'` | Configures the technical `ITEMS_PER_PAGE` parameter for `GUI.CLAIMS.ITEMS_PER_PAGE` in `duels.yml`. |

### 3. Practical Setup Example

```yaml
GUI:
  QUEUE:
    TITLE: '&8Casual Queue'
    SIZE: 27
  CREATE:
    TITLE: '&8Create Duel -> {player}'
    SIZE: 27
  CLAIMS:
    TITLE: '&8Duel Claims'
    SIZE: 54
    ITEMS_PER_PAGE: 45
```

---

