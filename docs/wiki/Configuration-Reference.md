# Exhaustive Technical Configuration Reference & Setup Manual

This document provides a **100% complete, fully commented reference guide** for every configuration file and option key in **UltimateDonutSMP**.
Each section details the exact YAML block with all comments, option keys, data types, allowed values, functional explanations, and step-by-step setup guides.

---

# duels.yml - 1v1 Duels & Fast Crystal Engine

## Section: `SETTINGS` - General Match Settings

### Fully Commented Setup Code Example
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

### Key Options & Setup Breakdown
| Key / Option Path | Data Type | Allowed Values / Options | Default | Functional Behavior & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `SETTINGS.ENABLED` | `bool` | true, false | `True` | Configures `ENABLED` for `SETTINGS`. |
| `SETTINGS.COUNTDOWN_SECONDS` | `int` | Any valid integer | `5` | Configures `COUNTDOWN_SECONDS` for `SETTINGS`. |
| `SETTINGS.MATCH_DURATION_SECONDS` | `int` | Any valid integer | `900` | Configures `MATCH_DURATION_SECONDS` for `SETTINGS`. |
| `SETTINGS.REQUEST_TIMEOUT_SECONDS` | `int` | Any valid integer | `30` | Configures `REQUEST_TIMEOUT_SECONDS` for `SETTINGS`. |
| `SETTINGS.DRAW_REQUEST_TIMEOUT_SECONDS` | `int` | Any valid integer | `15` | Configures `DRAW_REQUEST_TIMEOUT_SECONDS` for `SETTINGS`. |
| `SETTINGS.RETURN_DELAY_SECONDS` | `int` | Any valid integer | `3` | Configures `RETURN_DELAY_SECONDS` for `SETTINGS`. |
| `SETTINGS.WINNER_RETURN_DELAY_SECONDS` | `int` | Any valid integer | `3` | Configures `WINNER_RETURN_DELAY_SECONDS` for `SETTINGS`. |
| `SETTINGS.ROLLBACK_PADDING_HORIZONTAL` | `int` | Any valid integer | `8` | Configures `ROLLBACK_PADDING_HORIZONTAL` for `SETTINGS`. |
| `SETTINGS.ROLLBACK_PADDING_VERTICAL` | `int` | Any valid integer | `6` | Configures `ROLLBACK_PADDING_VERTICAL` for `SETTINGS`. |

---

## Section: `START-COUNTDOWN` - Match Start Countdown & Sound Effects

### Fully Commented Setup Code Example
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

### Key Options & Setup Breakdown
| Key / Option Path | Data Type | Allowed Values / Options | Default | Functional Behavior & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `START-COUNTDOWN.ENABLED` | `bool` | true, false | `True` | Configures `ENABLED` for `START-COUNTDOWN`. |
| `START-COUNTDOWN.SOUNDS.ENABLED` | `bool` | true, false | `True` | Configures `ENABLED` for `START-COUNTDOWN`. |
| `START-COUNTDOWN.TITLES.6` | `str` | Any string text | `` | Configures `6` for `START-COUNTDOWN`. |
| `START-COUNTDOWN.TITLES.5` | `str` | Any string text | `&e5` | Configures `5` for `START-COUNTDOWN`. |
| `START-COUNTDOWN.TITLES.4` | `str` | Any string text | `&e4` | Configures `4` for `START-COUNTDOWN`. |
| `START-COUNTDOWN.TITLES.3` | `str` | Any string text | `&c3` | Configures `3` for `START-COUNTDOWN`. |
| `START-COUNTDOWN.TITLES.2` | `str` | Any string text | `&c2` | Configures `2` for `START-COUNTDOWN`. |
| `START-COUNTDOWN.TITLES.1` | `str` | Any string text | `&c1` | Configures `1` for `START-COUNTDOWN`. |
| `START-COUNTDOWN.TITLES.0` | `str` | Any string text | `&a&lFight!` | Configures `0` for `START-COUNTDOWN`. |
| `START-COUNTDOWN.MESSAGES.5` | `str` | Any string text | `&a5` | Configures `5` for `START-COUNTDOWN`. |
| `START-COUNTDOWN.MESSAGES.4` | `str` | Any string text | `&a4` | Configures `4` for `START-COUNTDOWN`. |
| `START-COUNTDOWN.MESSAGES.3` | `str` | Any string text | `&a3` | Configures `3` for `START-COUNTDOWN`. |
| `START-COUNTDOWN.MESSAGES.2` | `str` | Any string text | `&a2` | Configures `2` for `START-COUNTDOWN`. |
| `START-COUNTDOWN.MESSAGES.1` | `str` | Any string text | `&a1` | Configures `1` for `START-COUNTDOWN`. |
| `START-COUNTDOWN.START-MESSAGE` | `str` | Any string text | `&aMatch Started!` | Configures `START-MESSAGE` for `START-COUNTDOWN`. |

---

## Section: `RESULT-TITLES` - Victory, Defeat & Draw Match Titles

### Fully Commented Setup Code Example
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

### Key Options & Setup Breakdown
| Key / Option Path | Data Type | Allowed Values / Options | Default | Functional Behavior & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `RESULT-TITLES.victory.title` | `str` | Any string text | `&e&lVICTORY!` | Configures `title` for `RESULT-TITLES`. |
| `RESULT-TITLES.victory.subtitle` | `str` | Any string text | `&e<player> &fwon the Match!` | Configures `subtitle` for `RESULT-TITLES`. |
| `RESULT-TITLES.defeat.title` | `str` | Any string text | `&c&lDEFEAT!` | Configures `title` for `RESULT-TITLES`. |
| `RESULT-TITLES.defeat.subtitle` | `str` | Any string text | `&c<opponent> &fwon this Match!` | Configures `subtitle` for `RESULT-TITLES`. |
| `RESULT-TITLES.draw.title` | `str` | Any string text | `&e&lDRAW!` | Configures `title` for `RESULT-TITLES`. |
| `RESULT-TITLES.draw.subtitle` | `str` | Any string text | `&fTime's up - no winner.` | Configures `subtitle` for `RESULT-TITLES`. |
| `RESULT-TITLES.draw.message` | `str` | Any string text | `&e[Timer] &fTime limit reached! Match...` | Configures `message` for `RESULT-TITLES`. |

---

## Section: `COMMAND_BLOCK` - In-Match Command Restriction & Allowlist

### Fully Commented Setup Code Example
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

### Key Options & Setup Breakdown
| Key / Option Path | Data Type | Allowed Values / Options | Default | Functional Behavior & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `COMMAND_BLOCK.ENABLED` | `bool` | true, false | `True` | Configures `ENABLED` for `COMMAND_BLOCK`. |
| `COMMAND_BLOCK.MODE` | `str` | Any string text | `ALLOWLIST` | Configures `MODE` for `COMMAND_BLOCK`. |
| `COMMAND_BLOCK.COMMANDS` | `list` | Configured values | `['/duel', '/draw', '/leave', '/queue']` | Configures `COMMANDS` for `COMMAND_BLOCK`. |
| `COMMAND_BLOCK.MESSAGE` | `str` | Any string text | `&cYou cannot use that command during ...` | Configures `MESSAGE` for `COMMAND_BLOCK`. |

---

## Section: `WORLDBORDER` - Dynamic World Border Rules

### Fully Commented Setup Code Example
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

### Key Options & Setup Breakdown
| Key / Option Path | Data Type | Allowed Values / Options | Default | Functional Behavior & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `WORLDBORDER.ENABLED` | `bool` | true, false | `True` | Configures `ENABLED` for `WORLDBORDER`. |
| `WORLDBORDER.SIZE` | `float` | Configured values | `96.0` | Configures `SIZE` for `WORLDBORDER`. |
| `WORLDBORDER.DAMAGE_BUFFER` | `float` | Configured values | `0.0` | Configures `DAMAGE_BUFFER` for `WORLDBORDER`. |
| `WORLDBORDER.WARNING_DISTANCE` | `int` | Any valid integer | `4` | Configures `WARNING_DISTANCE` for `WORLDBORDER`. |
| `WORLDBORDER.WARNING_TIME` | `int` | Any valid integer | `5` | Configures `WARNING_TIME` for `WORLDBORDER`. |
| `WORLDBORDER.ESCAPE_GRACE_TICKS` | `int` | Any valid integer | `40` | Configures `ESCAPE_GRACE_TICKS` for `WORLDBORDER`. |
| `WORLDBORDER.ACTION` | `str` | Any string text | `PUSH_BACK` | Configures `ACTION` for `WORLDBORDER`. |
| `WORLDBORDER.FALLBACK_ACTION` | `str` | Any string text | `FORFEIT` | Configures `FALLBACK_ACTION` for `WORLDBORDER`. |

---

## Section: `MAP_SOURCES` - Static Arenas & Random Biome Duel Worlds

### Fully Commented Setup Code Example
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

# Cross-server BungeeCord / Velocity Redis sync settings
```

### Key Options & Setup Breakdown
| Key / Option Path | Data Type | Allowed Values / Options | Default | Functional Behavior & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `MAP_SOURCES.STATIC_WORLDS.ENABLED` | `bool` | true, false | `True` | Configures `ENABLED` for `MAP_SOURCES`. |
| `MAP_SOURCES.STATIC_WORLDS.AUTO_LOAD` | `bool` | true, false | `True` | Configures `AUTO_LOAD` for `MAP_SOURCES`. |
| `MAP_SOURCES.STATIC_WORLDS.WORLDS` | `list` | Configured values | `[]` | Configures `WORLDS` for `MAP_SOURCES`. |
| `MAP_SOURCES.RANDOM_BIOMES.ENABLED` | `bool` | true, false | `True` | Configures `ENABLED` for `MAP_SOURCES`. |
| `MAP_SOURCES.RANDOM_BIOMES.TERRAIN_MODE` | `str` | Any string text | `FLAT` | Configures `TERRAIN_MODE` for `MAP_SOURCES`. |
| `MAP_SOURCES.RANDOM_BIOMES.GENERATE_STRUCTURES` | `bool` | true, false | `False` | Configures `GENERATE_STRUCTURES` for `MAP_SOURCES`. |
| `MAP_SOURCES.RANDOM_BIOMES.CLEANUP_AFTER_MATCH` | `bool` | true, false | `True` | Configures `CLEANUP_AFTER_MATCH` for `MAP_SOURCES`. |
| `MAP_SOURCES.RANDOM_BIOMES.ARENA_RADIUS` | `int` | Any valid integer | `48` | Configures `ARENA_RADIUS` for `MAP_SOURCES`. |
| `MAP_SOURCES.RANDOM_BIOMES.SPAWN_DISTANCE` | `int` | Any valid integer | `16` | Configures `SPAWN_DISTANCE` for `MAP_SOURCES`. |
| `MAP_SOURCES.RANDOM_BIOMES.SPAWN_SEARCH_RADIUS` | `int` | Any valid integer | `16` | Configures `SPAWN_SEARCH_RADIUS` for `MAP_SOURCES`. |
| `MAP_SOURCES.RANDOM_BIOMES.WORLD_PREFIX` | `str` | Any string text | `duel_biome_` | Configures `WORLD_PREFIX` for `MAP_SOURCES`. |
| `MAP_SOURCES.RANDOM_BIOMES.WORLD_FOLDER` | `str` | Any string text | `duel` | Configures `WORLD_FOLDER` for `MAP_SOURCES`. |
| `MAP_SOURCES.RANDOM_BIOMES.ALLOWLIST` | `list` | Configured values | `[]` | Configures `ALLOWLIST` for `MAP_SOURCES`. |
| `MAP_SOURCES.RANDOM_BIOMES.EXCLUDE` | `list` | Configured values | `[]` | Configures `EXCLUDE` for `MAP_SOURCES`. |
| `MAP_SOURCES.RANDOM_BIOMES.FLAT_POOL.ENABLED` | `bool` | true, false | `True` | Configures `ENABLED` for `MAP_SOURCES`. |
| `MAP_SOURCES.RANDOM_BIOMES.FLAT_POOL.REUSE_WORLDS` | `bool` | true, false | `True` | Configures `REUSE_WORLDS` for `MAP_SOURCES`. |
| `MAP_SOURCES.RANDOM_BIOMES.FLAT_POOL.SIZE` | `int` | Any valid integer | `2` | Configures `SIZE` for `MAP_SOURCES`. |
| `MAP_SOURCES.RANDOM_BIOMES.FLAT_POOL.PREPARE_INTERVAL_TICKS` | `int` | Any valid integer | `20` | Configures `PREPARE_INTERVAL_TICKS` for `MAP_SOURCES`. |
| `MAP_SOURCES.RANDOM_BIOMES.VANILLA_POOL.ENABLED` | `bool` | true, false | `True` | Configures `ENABLED` for `MAP_SOURCES`. |
| `MAP_SOURCES.RANDOM_BIOMES.VANILLA_POOL.RUNTIME_GENERATION` | `bool` | true, false | `True` | Configures `RUNTIME_GENERATION` for `MAP_SOURCES`. |
| `MAP_SOURCES.RANDOM_BIOMES.VANILLA_POOL.SIZE` | `int` | Any valid integer | `2` | Configures `SIZE` for `MAP_SOURCES`. |
| `MAP_SOURCES.RANDOM_BIOMES.VANILLA_POOL.CHUNKS_PER_TICK` | `int` | Any valid integer | `1` | Configures `CHUNKS_PER_TICK` for `MAP_SOURCES`. |
| `MAP_SOURCES.RANDOM_BIOMES.VANILLA_POOL.PREPARE_INTERVAL_TICKS` | `int` | Any valid integer | `1` | Configures `PREPARE_INTERVAL_TICKS` for `MAP_SOURCES`. |
| `MAP_SOURCES.RANDOM_BIOMES.VANILLA_POOL.MAX_SYNC_STEP_MS` | `int` | Any valid integer | `2000` | Configures `MAX_SYNC_STEP_MS` for `MAP_SOURCES`. |
| `MAP_SOURCES.RANDOM_BIOMES.VANILLA_POOL.PAUSE_ON_SLOW_STEP` | `bool` | true, false | `True` | Configures `PAUSE_ON_SLOW_STEP` for `MAP_SOURCES`. |

---

## Section: `CROSS_SERVER` - BungeeCord / Velocity Redis Matchmaking

### Fully Commented Setup Code Example
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

### Key Options & Setup Breakdown
| Key / Option Path | Data Type | Allowed Values / Options | Default | Functional Behavior & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `CROSS_SERVER.ENABLED` | `bool` | true, false | `False` | Configures `ENABLED` for `CROSS_SERVER`. |
| `CROSS_SERVER.LOCAL_SERVER_ID` | `str` | Any string text | `` | Configures `LOCAL_SERVER_ID` for `CROSS_SERVER`. |
| `CROSS_SERVER.REDIS_CHANNEL` | `str` | Any string text | `ultimatedonutsmp:duels` | Configures `REDIS_CHANNEL` for `CROSS_SERVER`. |
| `CROSS_SERVER.KEY_PREFIX` | `str` | Any string text | `uds:duels:` | Configures `KEY_PREFIX` for `CROSS_SERVER`. |
| `CROSS_SERVER.STALE_QUEUE_TIMEOUT_SECONDS` | `int` | Any valid integer | `45` | Configures `STALE_QUEUE_TIMEOUT_SECONDS` for `CROSS_SERVER`. |
| `CROSS_SERVER.TRANSFER_TIMEOUT_SECONDS` | `int` | Any valid integer | `20` | Configures `TRANSFER_TIMEOUT_SECONDS` for `CROSS_SERVER`. |
| `CROSS_SERVER.PROXY_SERVER_NAME` | `str` | Any string text | `` | Configures `PROXY_SERVER_NAME` for `CROSS_SERVER`. |
| `CROSS_SERVER.ALLOWED_QUEUE_SERVERS` | `list` | Configured values | `[]` | Configures `ALLOWED_QUEUE_SERVERS` for `CROSS_SERVER`. |
| `CROSS_SERVER.ALLOWED_MATCH_SERVERS` | `list` | Configured values | `[]` | Configures `ALLOWED_MATCH_SERVERS` for `CROSS_SERVER`. |

---

## Section: `GUI` - Duel Matchmaking GUI Layouts & Titles

### Fully Commented Setup Code Example
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

### Key Options & Setup Breakdown
| Key / Option Path | Data Type | Allowed Values / Options | Default | Functional Behavior & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `GUI.QUEUE.TITLE` | `str` | Any string text | `&8Casual Queue` | Configures `TITLE` for `GUI`. |
| `GUI.QUEUE.SIZE` | `int` | Any valid integer | `27` | Configures `SIZE` for `GUI`. |
| `GUI.CREATE.TITLE` | `str` | Any string text | `&8Create Duel -> {player}` | Configures `TITLE` for `GUI`. |
| `GUI.CREATE.SIZE` | `int` | Any valid integer | `27` | Configures `SIZE` for `GUI`. |
| `GUI.CLAIMS.TITLE` | `str` | Any string text | `&8Duel Claims` | Configures `TITLE` for `GUI`. |
| `GUI.CLAIMS.SIZE` | `int` | Any valid integer | `54` | Configures `SIZE` for `GUI`. |
| `GUI.CLAIMS.ITEMS_PER_PAGE` | `int` | Any valid integer | `45` | Configures `ITEMS_PER_PAGE` for `GUI`. |

---

# config.yml - Main Plugin Configuration

## Section: `LOCATIONS` - Global Spawn & AFK Locations

### Fully Commented Setup Code Example
```yaml
LOCATIONS:
  # The text or value for Spawn Location. Available options: Any valid string text
  SPAWN-LOCATION: ''
  # The text or value for Afk Location. Available options: Any valid string text
  AFK-LOCATION: ''
# Configuration section for Portal System.
```

### Key Options & Setup Breakdown
| Key / Option Path | Data Type | Allowed Values / Options | Default | Functional Behavior & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `LOCATIONS.SPAWN-LOCATION` | `str` | Any string text | `` | Configures `SPAWN-LOCATION` for `LOCATIONS`. |
| `LOCATIONS.AFK-LOCATION` | `str` | Any string text | `` | Configures `AFK-LOCATION` for `LOCATIONS`. |

---

## Section: `PORTAL-SYSTEM` - Cuboid Portal System & Floating Holograms

### Fully Commented Setup Code Example
```yaml
PORTAL-SYSTEM:
  # Determines whether Enabled is enabled or disabled. Available options: true, false
  ENABLED: true
  # Determines whether Block In Combat is enabled or disabled. Available options: true, false
  BLOCK-IN-COMBAT: true
  # The numerical value for Default Trigger Cooldown Ms. Available options: Any valid integer
  DEFAULT-TRIGGER-COOLDOWN-MS: 1500
  # The numerical value for Post Teleport Grace Ms. Available options: Any valid integer
  POST-TELEPORT-GRACE-MS: 2000
  # Configuration section for Hologram.
  HOLOGRAM:
    # Determines whether Enabled is enabled or disabled. Available options: true, false
    ENABLED: true
    # The text or value for Default Region. Available options: Any valid string text
    DEFAULT-REGION: NA East
    # The text or value for Default Server Id. Available options: Any valid string text
    DEFAULT-SERVER-ID: ''
    # The text or value for Portals. Available options: Any valid string text
    PORTALS: null
    # The decimal value for Offset Y. Available options: Any decimal number
    OFFSET-Y: 1.2
    # The decimal value for Set Here Offset Y. Available options: Any decimal number
    SET-HERE-OFFSET-Y: 1.6
    # The decimal value for Line Spacing. Available options: Any decimal number
    LINE-SPACING: 0.27
    # The numerical value for Update Ticks. Available options: Any valid integer
    UPDATE-TICKS: 40
    # Configuration section for Lines.
    LINES:
    - '&f{portal}'
    - '&7Region {region}'
    - ''
    - '&f<total_player> Players'
# Configuration section for Settings.
```

### Key Options & Setup Breakdown
| Key / Option Path | Data Type | Allowed Values / Options | Default | Functional Behavior & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `PORTAL-SYSTEM.ENABLED` | `bool` | true, false | `True` | Configures `ENABLED` for `PORTAL-SYSTEM`. |
| `PORTAL-SYSTEM.BLOCK-IN-COMBAT` | `bool` | true, false | `True` | Configures `BLOCK-IN-COMBAT` for `PORTAL-SYSTEM`. |
| `PORTAL-SYSTEM.DEFAULT-TRIGGER-COOLDOWN-MS` | `int` | Any valid integer | `1500` | Configures `DEFAULT-TRIGGER-COOLDOWN-MS` for `PORTAL-SYSTEM`. |
| `PORTAL-SYSTEM.POST-TELEPORT-GRACE-MS` | `int` | Any valid integer | `2000` | Configures `POST-TELEPORT-GRACE-MS` for `PORTAL-SYSTEM`. |
| `PORTAL-SYSTEM.HOLOGRAM.ENABLED` | `bool` | true, false | `True` | Configures `ENABLED` for `PORTAL-SYSTEM`. |
| `PORTAL-SYSTEM.HOLOGRAM.DEFAULT-REGION` | `str` | Any string text | `NA East` | Configures `DEFAULT-REGION` for `PORTAL-SYSTEM`. |
| `PORTAL-SYSTEM.HOLOGRAM.DEFAULT-SERVER-ID` | `str` | Any string text | `` | Configures `DEFAULT-SERVER-ID` for `PORTAL-SYSTEM`. |
| `PORTAL-SYSTEM.HOLOGRAM.PORTALS` | `NoneType` | Configured values | `None` | Configures `PORTALS` for `PORTAL-SYSTEM`. |
| `PORTAL-SYSTEM.HOLOGRAM.OFFSET-Y` | `float` | Configured values | `1.2` | Configures `OFFSET-Y` for `PORTAL-SYSTEM`. |
| `PORTAL-SYSTEM.HOLOGRAM.SET-HERE-OFFSET-Y` | `float` | Configured values | `1.6` | Configures `SET-HERE-OFFSET-Y` for `PORTAL-SYSTEM`. |
| `PORTAL-SYSTEM.HOLOGRAM.LINE-SPACING` | `float` | Configured values | `0.27` | Configures `LINE-SPACING` for `PORTAL-SYSTEM`. |
| `PORTAL-SYSTEM.HOLOGRAM.UPDATE-TICKS` | `int` | Any valid integer | `40` | Configures `UPDATE-TICKS` for `PORTAL-SYSTEM`. |
| `PORTAL-SYSTEM.HOLOGRAM.LINES` | `list` | Configured values | `['&f{portal}', '&7Region {region}', '...` | Configures `LINES` for `PORTAL-SYSTEM`. |

---

## Section: `SETTINGS` - Gameplay Rules, Respawn Gear & Limits

### Fully Commented Setup Code Example
```yaml
SETTINGS:
  # Determines whether Respawn On Bed is enabled or disabled. Available options: true, false
  RESPAWN-ON-BED: false
  # Determines whether Chainmail On Respawn is enabled or disabled. Available options: true, false
  CHAINMAIL-ON-RESPAWN: true
  # Configuration section for Chainmail Respawn Items.
  CHAINMAIL-RESPAWN-ITEMS:
  - MATERIAL: STONE_SWORD
    # The numerical value for Amount. Available options: Any valid integer
    AMOUNT: 1
  - MATERIAL: CHAINMAIL_HELMET
    NAME: '&eChainmail Helmet'
    # The numerical value for Amount. Available options: Any valid integer
    AMOUNT: 1
  - MATERIAL: CHAINMAIL_CHESTPLATE
    NAME: '&eChainmail Chestplate'
    # The numerical value for Amount. Available options: Any valid integer
    AMOUNT: 1
  - MATERIAL: CHAINMAIL_LEGGINGS
    NAME: '&eChainmail Leggings'
    # The numerical value for Amount. Available options: Any valid integer
    AMOUNT: 1
  - MATERIAL: CHAINMAIL_BOOTS
    NAME: '&eChainmail Boots'
    # The numerical value for Amount. Available options: Any valid integer
    AMOUNT: 1
  - MATERIAL: COOKED_BEEF
    # The numerical value for Amount. Available options: Any valid integer
    AMOUNT: 16
  # The numerical value for Home Default. Available options: Any valid integer
  HOME-DEFAULT: 2
  # The numerical value for Shards Per Kill. Available options: Any valid integer
  SHARDS-PER-KILL: 1
  # The text or value for Shards Kill Message. Available options: Any valid string text
  SHARDS-KILL-MESSAGE: '&#A303F9+{shards} Shard'
  # The text or value for Shards Kill Message Boosted, shown instead of Shards Kill
  # Message while a shard booster multiplies the kill reward. Supports {multiplier}.
  # Available options: Any valid string text
  SHARDS-KILL-MESSAGE-BOOSTED: '&#A303F9+{shards} Shards &7(&ax{multiplier}&7)'
  # The numerical value for Shards Kill Cooldown Seconds. Blocks repeated kill rewards
  # against the same victim until the cooldown expires. Set to 0 to disable.
  # Available options: Any valid integer
  SHARDS-KILL-COOLDOWN-SECONDS: 600
  # The text or value for Shards Kill Cooldown Message, shown when the kill reward is
  # skipped because the same victim was killed recently. Leave empty to stay silent.
  # Available options: Any valid string text
  SHARDS-KILL-COOLDOWN-MESSAGE: '&cNo Shard &7(killed recently, {time} left)'
  # The decimal value for Money Per Default. Available options: Any decimal number
  MONEY-PER-DEFAULT: 1000.0
  # The text or value for Sell Message. Available options: Any valid string text
  SELL-MESSAGE: '&a+$%price%'
  # Determines whether Spawn Menu is enabled or disabled. Available options: true, false
  SPAWN-MENU: true
  # Determines whether Afk Menu is enabled or disabled. Available options: true, false
  AFK-MENU: true
  # The decimal value for Worth Default Value. Available options: Any decimal number
  WORTH-DEFAULT-VALUE: 1.0
  # The numerical value for Mob Spawn Radius. Available options: Any valid integer
  MOB-SPAWN-RADIUS: 50
  # The numerical value for Phantom Spawn Radius. Available options: Any valid integer
  PHANTOM-SPAWN-RADIUS: 40
  # The numerical value for Disable Mob Spawn Limit Seconds. Set to -1 for no limit. Available options: Any valid integer
  DISABLE-MOB-SPAWN-LIMIT-SECONDS: -1
  # The numerical value for Disable Phantom Spawn Limit Seconds. Set to -1 for no limit. Available options: Any valid integer
  DISABLE-PHANTOM-SPAWN-LIMIT-SECONDS: 3600
# Configuration section for Features.
```

### Key Options & Setup Breakdown
| Key / Option Path | Data Type | Allowed Values / Options | Default | Functional Behavior & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `SETTINGS.RESPAWN-ON-BED` | `bool` | `true`, `false` | `False` | If `false`, forces player respawns to global Spawn. If `true`, permits Bed and Respawn Anchor respawns. |
| `SETTINGS.CHAINMAIL-ON-RESPAWN` | `bool` | `true`, `false` | `True` | If `true`, equips players with starter chainmail armor & stone sword upon death respawn. |
| `SETTINGS.CHAINMAIL-RESPAWN-ITEMS` | `list` | Configured values | `[{'MATERIAL': 'STONE_SWORD', 'AMOUNT'...` | Configures `CHAINMAIL-RESPAWN-ITEMS` for `SETTINGS`. |
| `SETTINGS.HOME-DEFAULT` | `int` | Any positive integer | `2` | Default maximum `/sethome` limit for non-donor players. |
| `SETTINGS.SHARDS-PER-KILL` | `int` | Any valid integer | `1` | Configures `SHARDS-PER-KILL` for `SETTINGS`. |
| `SETTINGS.SHARDS-KILL-MESSAGE` | `str` | Any string text | `&#A303F9+{shards} Shard` | Configures `SHARDS-KILL-MESSAGE` for `SETTINGS`. |
| `SETTINGS.SHARDS-KILL-MESSAGE-BOOSTED` | `str` | Any string text | `&#A303F9+{shards} Shards &7(&ax{multiplier}&7)` | Action bar shown instead of `SHARDS-KILL-MESSAGE` while a shard booster multiplies the kill reward. Supports `{multiplier}`. |
| `SETTINGS.SHARDS-KILL-COOLDOWN-SECONDS` | `int` | Any valid integer | `600` | Time a killer must wait before the same victim rewards shards again. Set to `0` to reward every kill. |
| `SETTINGS.SHARDS-KILL-COOLDOWN-MESSAGE` | `str` | Any string text | `&cNo Shard &7(killed recently, {time} left)` | Action bar shown when a kill reward is skipped by the cooldown. Supports `{time}` and `{seconds}`. Leave empty to stay silent. |
| `SETTINGS.MONEY-PER-DEFAULT` | `float` | Configured values | `1000.0` | Configures `MONEY-PER-DEFAULT` for `SETTINGS`. |
| `SETTINGS.SELL-MESSAGE` | `str` | Any string text | `&a+$%price%` | Configures `SELL-MESSAGE` for `SETTINGS`. |
| `SETTINGS.SPAWN-MENU` | `bool` | true, false | `True` | Configures `SPAWN-MENU` for `SETTINGS`. |
| `SETTINGS.AFK-MENU` | `bool` | true, false | `True` | Configures `AFK-MENU` for `SETTINGS`. |
| `SETTINGS.WORTH-DEFAULT-VALUE` | `float` | Configured values | `1.0` | Configures `WORTH-DEFAULT-VALUE` for `SETTINGS`. |
| `SETTINGS.MOB-SPAWN-RADIUS` | `int` | Any valid integer | `50` | Configures `MOB-SPAWN-RADIUS` for `SETTINGS`. |
| `SETTINGS.PHANTOM-SPAWN-RADIUS` | `int` | Any valid integer | `40` | Configures `PHANTOM-SPAWN-RADIUS` for `SETTINGS`. |
| `SETTINGS.DISABLE-MOB-SPAWN-LIMIT-SECONDS` | `int` | Any valid integer | `-1` | Configures `DISABLE-MOB-SPAWN-LIMIT-SECONDS` for `SETTINGS`. |
| `SETTINGS.DISABLE-PHANTOM-SPAWN-LIMIT-SECONDS` | `int` | Any valid integer | `3600` | Configures `DISABLE-PHANTOM-SPAWN-LIMIT-SECONDS` for `SETTINGS`. |

---

## Section: `FEATURES_SETTINGS` - Disabled Feature Command Handling

### Fully Commented Setup Code Example
```yaml
FEATURES_SETTINGS:
  # Action when executing a command linked to a disabled feature.
  # Options:
  # - "MESSAGE": Shows "The <feature> feature is currently disabled."
  # - "UNKNOWN": Shows default unknown command message.
  # - "UNREGISTER": Dynamically unregister command from Bukkit command map.
  DISABLED_COMMAND_ACTION: "MESSAGE"
# Configuration section for Chat.
```

### Key Options & Setup Breakdown
| Key / Option Path | Data Type | Allowed Values / Options | Default | Functional Behavior & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `FEATURES_SETTINGS.DISABLED_COMMAND_ACTION` | `str` | `"MESSAGE"`, `"UNKNOWN"`, `"UNREGISTER"` | `MESSAGE` | Action when executing a disabled feature's command:<br>- `"MESSAGE"`: Shows disabled notice.<br>- `"UNKNOWN"`: Shows unknown command message.<br>- `"UNREGISTER"`: Dynamically unregisters command from Bukkit. |

---

## Section: `CHAT` - Chat Formatting, Colors & Moderation Filter

### Fully Commented Setup Code Example
```yaml
CHAT:
  # Determines whether Format Enabled is enabled or disabled. Available options: true, false
  FORMAT-ENABLED: true
  # The text or value for Format. Available options: Any valid string text
  FORMAT: '&f%prefix%%player%&7: &f%message%'
  # Configuration section for Message Colors.
  MESSAGE-COLORS:
    # The text or value for Default. Available options: Any valid string text
    default: '&f'
    # The text or value for Owner. Available options: Any valid string text
    owner: '&#0000FF'
  # Configuration section for Clickable Name.
  CLICKABLE-NAME:
    # Determines whether Enabled is enabled or disabled. Available options: true, false
    ENABLED: true
    # Configuration section for Hover Text.
    HOVER-TEXT:
    - '%luckperms_prefix%%player%'
    - '&7&m----------'
    - '&#00FC00&l$ &fmoney &#00FC00%economy_money%'
    - '&#FC0000⚔ &fkills &#FC0000%economy_kills%'
    - '&#FCE300⌚ &fplaytime &#FCE300%economy_playtime%'
    - '&#F97603☠ &fdeaths &#F97603%economy_deaths%'
    - '&#A303F9★ &fshards &#A303F9%economy_shards%'
    - '&7&m----------'
    - '&7click to view stats'
    # The text or value for Suggest Command. Available options: Any valid string text
    SUGGEST-COMMAND: '/msg <player> '
  # Determines whether Global Chat Muted is enabled or disabled. Available options: true, false
  GLOBAL-CHAT-MUTED: false
  # Determines whether Global Chat Delay Enabled is enabled or disabled. Available options: true, false
  GLOBAL-CHAT-DELAY-ENABLED: false
  # The numerical value for Global Chat Delay. Available options: Any valid integer
  GLOBAL-CHAT-DELAY: 3
  # The numerical value for Max Delay Seconds. Available options: Any valid integer
  MAX-DELAY-SECONDS: 30
  # The numerical value for Clear Lines. Available options: Any valid integer
  CLEAR-LINES: 150
  # Configuration section for Filter.
  FILTER:
    # Determines whether Enabled is enabled or disabled. Available options: true, false
    ENABLED: true
    # The text or value for Block Message. Available options: Any valid string text
    BLOCK-MESSAGE: '&7Please avoid using inappropriate words.'
    # Configuration section for Words.
    WORDS:
    - fuck
    - shit
    - bitch
    # Configuration section for Language.
    LANGUAGE:
      # Determines whether Enabled is enabled or disabled. Available options: true, false
      ENABLED: false
      # Configuration section for Allowed Alphabets.
      ALLOWED-ALPHABETS:
      - LATIN
      - NUMBERS
      - SYMBOLS
      # The text or value for Block Message. Available options: Any valid string text
      BLOCK-MESSAGE: '&cYour message contains characters that are not allowed on this
        server.'
    # Configuration section for Caps.
    CAPS:
      # Determines whether Enabled is enabled or disabled. Available options: true, false
      ENABLED: false
      # The numerical value for Percentage. Available options: Any valid integer
      PERCENTAGE: 70
      # The numerical value for Min Length. Available options: Any valid integer
      MIN-LENGTH: 5
      # The text or value for Block Message. Available options: Any valid string text
      BLOCK-MESSAGE: '&cPlease avoid using too many capital letters.'
    # Configuration section for Anti Repeat.
    ANTI-REPEAT:
      # Determines whether Enabled is enabled or disabled. Available options: true, false
      ENABLED: false
      # The text or value for Block Message. Available options: Any valid string text
      BLOCK-MESSAGE: '&cYou cannot repeat the same message!'
    # Configuration section for Anti Link.
    ANTI-LINK:
      # Determines whether Enabled is enabled or disabled. Available options: true, false
      ENABLED: false
      # Configuration section for Allowed.
      ALLOWED:
      - google.com
      - youtube.com
      # The text or value for Block Message. Available options: Any valid string text
      BLOCK-MESSAGE: '&cLinks are not allowed in the chat!'
    # Configuration section for Length.
    LENGTH:
      # Configuration section for Min.
      MIN:
        # Determines whether Enabled is enabled or disabled. Available options: true, false
        ENABLED: false
        # The numerical value for Value. Available options: Any valid integer
        VALUE: 1
        # The text or value for Block Message. Available options: Any valid string text
        BLOCK-MESSAGE: '&cYour message is too short! (Min: %min%)'
      # Configuration section for Max.
      MAX:
        # Determines whether Enabled is enabled or disabled. Available options: true, false
        ENABLED: false
        # The numerical value for Value. Available options: Any valid integer
        VALUE: 100
        # The text or value for Block Message. Available options: Any valid string text
        BLOCK-MESSAGE: '&cYour message is too long! (Max: %max%)'
# Configuration section for Afk System.
```

### Key Options & Setup Breakdown
| Key / Option Path | Data Type | Allowed Values / Options | Default | Functional Behavior & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `CHAT.FORMAT-ENABLED` | `bool` | true, false | `True` | Configures `FORMAT-ENABLED` for `CHAT`. |
| `CHAT.FORMAT` | `str` | Any string text | `&f%prefix%%player%&7: &f%message%` | Configures `FORMAT` for `CHAT`. |
| `CHAT.MESSAGE-COLORS.default` | `str` | Any string text | `&f` | Configures `default` for `CHAT`. |
| `CHAT.MESSAGE-COLORS.owner` | `str` | Any string text | `&#0000FF` | Configures `owner` for `CHAT`. |
| `CHAT.CLICKABLE-NAME.ENABLED` | `bool` | true, false | `True` | Configures `ENABLED` for `CHAT`. |
| `CHAT.CLICKABLE-NAME.HOVER-TEXT` | `list` | Configured values | `['%luckperms_prefix%%player%', '&7&m-...` | Configures `HOVER-TEXT` for `CHAT`. |
| `CHAT.CLICKABLE-NAME.SUGGEST-COMMAND` | `str` | Any string text | `/msg <player> ` | Configures `SUGGEST-COMMAND` for `CHAT`. |
| `CHAT.GLOBAL-CHAT-MUTED` | `bool` | true, false | `False` | Configures `GLOBAL-CHAT-MUTED` for `CHAT`. |
| `CHAT.GLOBAL-CHAT-DELAY-ENABLED` | `bool` | true, false | `False` | Configures `GLOBAL-CHAT-DELAY-ENABLED` for `CHAT`. |
| `CHAT.GLOBAL-CHAT-DELAY` | `int` | Any valid integer | `3` | Configures `GLOBAL-CHAT-DELAY` for `CHAT`. |
| `CHAT.MAX-DELAY-SECONDS` | `int` | Any valid integer | `30` | Configures `MAX-DELAY-SECONDS` for `CHAT`. |
| `CHAT.CLEAR-LINES` | `int` | Any valid integer | `150` | Configures `CLEAR-LINES` for `CHAT`. |
| `CHAT.FILTER.ENABLED` | `bool` | true, false | `True` | Configures `ENABLED` for `CHAT`. |
| `CHAT.FILTER.BLOCK-MESSAGE` | `str` | Any string text | `&7Please avoid using inappropriate wo...` | Configures `BLOCK-MESSAGE` for `CHAT`. |
| `CHAT.FILTER.WORDS` | `list` | Configured values | `['fuck', 'shit', 'bitch']` | Configures `WORDS` for `CHAT`. |
| `CHAT.FILTER.LANGUAGE.ENABLED` | `bool` | true, false | `False` | Configures `ENABLED` for `CHAT`. |
| `CHAT.FILTER.LANGUAGE.ALLOWED-ALPHABETS` | `list` | Configured values | `['LATIN', 'NUMBERS', 'SYMBOLS']` | Configures `ALLOWED-ALPHABETS` for `CHAT`. |
| `CHAT.FILTER.LANGUAGE.BLOCK-MESSAGE` | `str` | Any string text | `&cYour message contains characters th...` | Configures `BLOCK-MESSAGE` for `CHAT`. |
| `CHAT.FILTER.CAPS.ENABLED` | `bool` | true, false | `False` | Configures `ENABLED` for `CHAT`. |
| `CHAT.FILTER.CAPS.PERCENTAGE` | `int` | Any valid integer | `70` | Configures `PERCENTAGE` for `CHAT`. |
| `CHAT.FILTER.CAPS.MIN-LENGTH` | `int` | Any valid integer | `5` | Configures `MIN-LENGTH` for `CHAT`. |
| `CHAT.FILTER.CAPS.BLOCK-MESSAGE` | `str` | Any string text | `&cPlease avoid using too many capital...` | Configures `BLOCK-MESSAGE` for `CHAT`. |
| `CHAT.FILTER.ANTI-REPEAT.ENABLED` | `bool` | true, false | `False` | Configures `ENABLED` for `CHAT`. |
| `CHAT.FILTER.ANTI-REPEAT.BLOCK-MESSAGE` | `str` | Any string text | `&cYou cannot repeat the same message!` | Configures `BLOCK-MESSAGE` for `CHAT`. |
| `CHAT.FILTER.ANTI-LINK.ENABLED` | `bool` | true, false | `False` | Configures `ENABLED` for `CHAT`. |
| `CHAT.FILTER.ANTI-LINK.ALLOWED` | `list` | Configured values | `['google.com', 'youtube.com']` | Configures `ALLOWED` for `CHAT`. |
| `CHAT.FILTER.ANTI-LINK.BLOCK-MESSAGE` | `str` | Any string text | `&cLinks are not allowed in the chat!` | Configures `BLOCK-MESSAGE` for `CHAT`. |
| `CHAT.FILTER.LENGTH.MIN.ENABLED` | `bool` | true, false | `False` | Configures `ENABLED` for `CHAT`. |
| `CHAT.FILTER.LENGTH.MIN.VALUE` | `int` | Any valid integer | `1` | Configures `VALUE` for `CHAT`. |
| `CHAT.FILTER.LENGTH.MIN.BLOCK-MESSAGE` | `str` | Any string text | `&cYour message is too short! (Min: %m...` | Configures `BLOCK-MESSAGE` for `CHAT`. |
| *(3 additional sub-keys configured in config.yml)* | | | | |

---

## Section: `AFK-SYSTEM` - AFK Reward Zone & Auto Teleport

### Fully Commented Setup Code Example
```yaml
AFK-SYSTEM:
  # Determines whether Enabled is enabled or disabled. Available options: true, false
  ENABLED: true
  # The numerical value for Time. Available options: Any valid integer
  TIME: 180
  # The text or value for Spawn Cuboid Name. Available options: Any valid string text
  SPAWN-CUBOID-NAME: spawn
  # The text or value for Afk Cuboid Name. Available options: Any valid string text
  AFK-CUBOID-NAME: ''
  # The text or value for Message. Available options: Any valid string text
  MESSAGE: '&7You have been moved to the AFK area for being inactive in the spawn.'
# Configuration section for Item Drop Prevention.
```

### Key Options & Setup Breakdown
| Key / Option Path | Data Type | Allowed Values / Options | Default | Functional Behavior & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `AFK-SYSTEM.ENABLED` | `bool` | true, false | `True` | Configures `ENABLED` for `AFK-SYSTEM`. |
| `AFK-SYSTEM.TIME` | `int` | Any valid integer | `180` | Configures `TIME` for `AFK-SYSTEM`. |
| `AFK-SYSTEM.SPAWN-CUBOID-NAME` | `str` | Any string text | `spawn` | Configures `SPAWN-CUBOID-NAME` for `AFK-SYSTEM`. |
| `AFK-SYSTEM.AFK-CUBOID-NAME` | `str` | Any string text | `` | Configures `AFK-CUBOID-NAME` for `AFK-SYSTEM`. |
| `AFK-SYSTEM.MESSAGE` | `str` | Any string text | `&7You have been moved to the AFK area...` | Configures `MESSAGE` for `AFK-SYSTEM`. |

---

## Section: `RTP-ZONE` - Random Wilderness Teleportation

### Fully Commented Setup Code Example
```yaml
RTP-ZONE:
  # Determines whether Enabled is enabled or disabled. Available options: true, false
  ENABLED: true
  # The text or value for Cuboid. Available options: Any valid string text
  CUBOID: ''
  # The numerical value for Every. Available options: Any valid integer
  EVERY: 30
  TITLE: '&c&lRTP Zone'
  # The text or value for Sub Title. Available options: Any valid string text
  SUB-TITLE: '&fTeleporting in %countdown%'
  # The text or value for Cancelled Message. Available options: Any valid string text
  CANCELLED-MESSAGE: '&cRTP cancelled because you left the zone.'
  # The text or value for Failed Message. Available options: Any valid string text
  FAILED-MESSAGE: '&cCould not find a safe RTP zone location.'
  # The text or value for Success Message. Available options: Any valid string text
  SUCCESS-MESSAGE: ''
  # Configuration section for World.
  WORLD:
    NAME: world
    # The numerical value for Center X. Available options: Any valid integer
    CENTER-X: 0
    # The numerical value for Center Z. Available options: Any valid integer
    CENTER-Z: 0
    # The numerical value for Min Radius. Available options: Any valid integer
    MIN-RADIUS: 500
    # The numerical value for Max Radius. Available options: Any valid integer
    MAX-RADIUS: 2000
  # The numerical value for Title Fade Out Ticks. Available options: Any valid integer
  TITLE-FADE-OUT-TICKS: 10
# Configuration section for Teleport Cooldown.
```

### Key Options & Setup Breakdown
| Key / Option Path | Data Type | Allowed Values / Options | Default | Functional Behavior & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `RTP-ZONE.ENABLED` | `bool` | true, false | `True` | Configures `ENABLED` for `RTP-ZONE`. |
| `RTP-ZONE.CUBOID` | `str` | Any string text | `` | Configures `CUBOID` for `RTP-ZONE`. |
| `RTP-ZONE.EVERY` | `int` | Any valid integer | `30` | Configures `EVERY` for `RTP-ZONE`. |
| `RTP-ZONE.TITLE` | `str` | Any string text | `&c&lRTP Zone` | Configures `TITLE` for `RTP-ZONE`. |
| `RTP-ZONE.SUB-TITLE` | `str` | Any string text | `&fTeleporting in %countdown%` | Configures `SUB-TITLE` for `RTP-ZONE`. |
| `RTP-ZONE.CANCELLED-MESSAGE` | `str` | Any string text | `&cRTP cancelled because you left the ...` | Configures `CANCELLED-MESSAGE` for `RTP-ZONE`. |
| `RTP-ZONE.FAILED-MESSAGE` | `str` | Any string text | `&cCould not find a safe RTP zone loca...` | Configures `FAILED-MESSAGE` for `RTP-ZONE`. |
| `RTP-ZONE.SUCCESS-MESSAGE` | `str` | Any string text | `` | Configures `SUCCESS-MESSAGE` for `RTP-ZONE`. |
| `RTP-ZONE.WORLD.NAME` | `str` | Any string text | `world` | Configures `NAME` for `RTP-ZONE`. |
| `RTP-ZONE.WORLD.CENTER-X` | `int` | Any valid integer | `0` | Configures `CENTER-X` for `RTP-ZONE`. |
| `RTP-ZONE.WORLD.CENTER-Z` | `int` | Any valid integer | `0` | Configures `CENTER-Z` for `RTP-ZONE`. |
| `RTP-ZONE.WORLD.MIN-RADIUS` | `int` | Any valid integer | `500` | Configures `MIN-RADIUS` for `RTP-ZONE`. |
| `RTP-ZONE.WORLD.MAX-RADIUS` | `int` | Any valid integer | `2000` | Configures `MAX-RADIUS` for `RTP-ZONE`. |
| `RTP-ZONE.TITLE-FADE-OUT-TICKS` | `int` | Any valid integer | `10` | Configures `TITLE-FADE-OUT-TICKS` for `RTP-ZONE`. |

---

## Section: `SHARDS` - Virtual Shards & Anti-AFK Movement Checks

### Fully Commented Setup Code Example
```yaml
SHARDS:
  # The numerical value for Every. Available options: Any valid integer
  EVERY: 1
  # The numerical value for Amount. Available options: Any valid integer
  AMOUNT: 1
  # The text or value for Countdown. Available options: Any valid string text
  COUNTDOWN: '&7Next shard in &#A303F9%time%'
  # The text or value for Received. Available options: Any valid string text
  RECEIVED: '&#A303F9You received %amount% Shard &7(Total: &#A303F9%total%&7)'
  # The text or value for Received Boosted. Available options: Any valid string text
  RECEIVED-BOOSTED: '&#A303F9You received %amount% Shards &7(&ax%multiplier%&7) &7(Total:
    &#A303F9%total%&7)'
  # The text or value for Cancelled Message. Available options: Any valid string text
  CANCELLED-MESSAGE: '&cShard reward cancelled &7(Left %cuboid% zone)'
  # Determines whether Reset On Leave is enabled or disabled. Available options: true, false
  RESET-ON-LEAVE: true
  # Configuration section for Cuboids.
  CUBOIDS:
    # Configuration section for Regions.
    REGIONS:
      # Configuration section for Spawn.
      spawn:
        # Determines whether Enabled is enabled or disabled. Available options: true, false
        ENABLED: false
        # Determines whether Bound is enabled or disabled. Available options: true, false
        BOUND: false
        # The numerical value for Priority. Available options: Any valid integer
        PRIORITY: 100
        # The text or value for Cuboid. Available options: Any valid string text
        CUBOID: ''
        # The text or value for World. Available options: Any valid string text
        WORLD: world
        # The numerical value for Interval. Available options: Any valid integer
        INTERVAL: 60
        # The numerical value for Amount. Available options: Any valid integer
        AMOUNT: 1
        # The text or value for Countdown Message. Available options: Any valid string text
        COUNTDOWN-MESSAGE: '&7Next shard in &#A303F9%time%'
        # The text or value for Reward Message. Available options: Any valid string text
        REWARD-MESSAGE: '&#A303F9You received %amount% Shard &7(Total: &#A303F9%total%&7)'
        # The text or value for Boosted Reward Message. Available options: Any valid string text
        BOOSTED-REWARD-MESSAGE: '&#A303F9You received %amount% Shards &7(&ax%multiplier%&7)
          # The text or value for # The Text Or Mode For &7(Total. Available Options. Available options: Any valid string text
          # The text or mode for &7(Total. Available options: Any string text &7(Total:
          &#A303F9%total%&7)'
        # The text or value for Leave Message. Available options: Any valid string text
        LEAVE-MESSAGE: '&cShard reward cancelled &7(Left %cuboid% zone)'
        # The numerical value for Afk Time. Available options: Any valid integer
        AFK-TIME: 120
        # The text or value for Afk Cuboid. Available options: Any valid string text
        AFK-CUBOID: ''
        # The text or value for Afk Location. Available options: Any valid string text
        AFK-LOCATION: ''
        # The text or value for Afk Message. Available options: Any valid string text
        AFK-MESSAGE: '&7You have been moved to the AFK area for being inactive in
          the shard zone.'
        # Determines whether Teleport On Afk is enabled or disabled. Available options: true, false
        TELEPORT-ON-AFK: true
        # Configuration section for Excluded Worlds.
        EXCLUDED-WORLDS:
        - duels
        # The numerical value for Recent Movement Window. Available options: Any valid integer
        RECENT-MOVEMENT-WINDOW: 15
        # The numerical value for Min Movement Blocks. Available options: Any valid integer
        MIN-MOVEMENT-BLOCKS: 5
        # Determines whether Reset On Leave is enabled or disabled. Available options: true, false
        RESET-ON-LEAVE: true
        # The text or value for Paused Message. Available options: Any valid string text
        PAUSED-MESSAGE: '&eMove to keep earning shards &7(%movement%/%required_movement%)'
        # The text or value for Afk Paused Message. Available options: Any valid string text
        AFK-PAUSED-MESSAGE: '&cYou are AFK. Move to resume shard gain'
        # The text or value for Excluded World Message. Available options: Any valid string text
        EXCLUDED-WORLD-MESSAGE: '&cShards are disabled in this world'
  # Configuration section for Everywhere.
  EVERYWHERE:
    # Determines whether Enabled is enabled or disabled. Available options: true, false
    ENABLED: false
    # The numerical value for Every. Available options: Any valid integer
    EVERY: 3
    # The numerical value for Amount. Available options: Any valid integer
    AMOUNT: 1
    # The text or value for Required Permission. Available options: Any valid string text
    REQUIRED-PERMISSION: ultimatedonutsmp.shards.everywhere
    # The numerical value for Recent Movement Window. Available options: Any valid integer
    RECENT-MOVEMENT-WINDOW: 15
    # Determines whether Disable While In Shard Cuboid is enabled or disabled. Available options: true, false
    DISABLE-WHILE-IN-SHARD-CUBOID: false
    # The text or value for Received. Available options: Any valid string text
    RECEIVED: '&#A303F9You received %amount% Shard &8[Everywhere] &7(Total: &#A303F9%total%&7)'
    # The text or value for Received Boosted. Available options: Any valid string text
    RECEIVED-BOOSTED: '&#A303F9You received %amount% Shards &7(&ax%multiplier%&7)
      # The text or value for # The Text Or Mode For &8[Everywhere] &7(Total. Available Options. Available options: Any valid string text
      # The text or mode for &8[Everywhere] &7(Total. Available options: Any string
      # The text or value for Text &8[Everywhere] &7(Total. Available options: Any valid string text
      text &8[Everywhere] &7(Total: &#A303F9%total%&7)'
    # Configuration section for Excluded Worlds.
    EXCLUDED-WORLDS:
    - duels
  # The numerical value for Booster Multiplier. Available options: Any valid integer
  BOOSTER-MULTIPLIER: 4
  # Determines whether the shard booster also multiplies player kill rewards.
  # Available options: true, false
  BOOSTER-APPLIES-TO-KILLS: true
  # The numerical value for Booster Kill Multiplier, used only for player kill rewards.
  # Set to 0 to reuse Booster Multiplier. Available options: Any valid integer
  BOOSTER-KILL-MULTIPLIER: 0
# Configuration section for Key All.
```

### Key Options & Setup Breakdown
| Key / Option Path | Data Type | Allowed Values / Options | Default | Functional Behavior & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `SHARDS.EVERY` | `int` | Any valid integer | `1` | Configures `EVERY` for `SHARDS`. |
| `SHARDS.AMOUNT` | `int` | Any valid integer | `1` | Configures `AMOUNT` for `SHARDS`. |
| `SHARDS.COUNTDOWN` | `str` | Any string text | `&7Next shard in &#A303F9%time%` | Configures `COUNTDOWN` for `SHARDS`. |
| `SHARDS.RECEIVED` | `str` | Any string text | `&#A303F9You received %amount% Shard &...` | Configures `RECEIVED` for `SHARDS`. |
| `SHARDS.RECEIVED-BOOSTED` | `str` | Any string text | `&#A303F9You received %amount% Shards ...` | Configures `RECEIVED-BOOSTED` for `SHARDS`. |
| `SHARDS.CANCELLED-MESSAGE` | `str` | Any string text | `&cShard reward cancelled &7(Left %cub...` | Configures `CANCELLED-MESSAGE` for `SHARDS`. |
| `SHARDS.RESET-ON-LEAVE` | `bool` | true, false | `True` | Configures `RESET-ON-LEAVE` for `SHARDS`. |
| `SHARDS.CUBOIDS.REGIONS.spawn.ENABLED` | `bool` | true, false | `False` | Configures `ENABLED` for `SHARDS`. |
| `SHARDS.CUBOIDS.REGIONS.spawn.BOUND` | `bool` | true, false | `False` | Configures `BOUND` for `SHARDS`. |
| `SHARDS.CUBOIDS.REGIONS.spawn.PRIORITY` | `int` | Any valid integer | `100` | Configures `PRIORITY` for `SHARDS`. |
| `SHARDS.CUBOIDS.REGIONS.spawn.CUBOID` | `str` | Any string text | `` | Configures `CUBOID` for `SHARDS`. |
| `SHARDS.CUBOIDS.REGIONS.spawn.WORLD` | `str` | Any string text | `world` | Configures `WORLD` for `SHARDS`. |
| `SHARDS.CUBOIDS.REGIONS.spawn.INTERVAL` | `int` | Any valid integer | `60` | Configures `INTERVAL` for `SHARDS`. |
| `SHARDS.CUBOIDS.REGIONS.spawn.AMOUNT` | `int` | Any valid integer | `1` | Configures `AMOUNT` for `SHARDS`. |
| `SHARDS.CUBOIDS.REGIONS.spawn.COUNTDOWN-MESSAGE` | `str` | Any string text | `&7Next shard in &#A303F9%time%` | Configures `COUNTDOWN-MESSAGE` for `SHARDS`. |
| `SHARDS.CUBOIDS.REGIONS.spawn.REWARD-MESSAGE` | `str` | Any string text | `&#A303F9You received %amount% Shard &...` | Configures `REWARD-MESSAGE` for `SHARDS`. |
| `SHARDS.CUBOIDS.REGIONS.spawn.BOOSTED-REWARD-MESSAGE` | `str` | Any string text | `&#A303F9You received %amount% Shards ...` | Configures `BOOSTED-REWARD-MESSAGE` for `SHARDS`. |
| `SHARDS.CUBOIDS.REGIONS.spawn.LEAVE-MESSAGE` | `str` | Any string text | `&cShard reward cancelled &7(Left %cub...` | Configures `LEAVE-MESSAGE` for `SHARDS`. |
| `SHARDS.CUBOIDS.REGIONS.spawn.AFK-TIME` | `int` | Any valid integer | `120` | Configures `AFK-TIME` for `SHARDS`. |
| `SHARDS.CUBOIDS.REGIONS.spawn.AFK-CUBOID` | `str` | Any string text | `` | Configures `AFK-CUBOID` for `SHARDS`. |
| `SHARDS.CUBOIDS.REGIONS.spawn.AFK-LOCATION` | `str` | Any string text | `` | Configures `AFK-LOCATION` for `SHARDS`. |
| `SHARDS.CUBOIDS.REGIONS.spawn.AFK-MESSAGE` | `str` | Any string text | `&7You have been moved to the AFK area...` | Configures `AFK-MESSAGE` for `SHARDS`. |
| `SHARDS.CUBOIDS.REGIONS.spawn.TELEPORT-ON-AFK` | `bool` | true, false | `True` | Configures `TELEPORT-ON-AFK` for `SHARDS`. |
| `SHARDS.CUBOIDS.REGIONS.spawn.EXCLUDED-WORLDS` | `list` | Configured values | `['duels']` | Configures `EXCLUDED-WORLDS` for `SHARDS`. |
| `SHARDS.CUBOIDS.REGIONS.spawn.RECENT-MOVEMENT-WINDOW` | `int` | Any valid integer | `15` | Configures `RECENT-MOVEMENT-WINDOW` for `SHARDS`. |
| `SHARDS.CUBOIDS.REGIONS.spawn.MIN-MOVEMENT-BLOCKS` | `int` | Any non-negative integer (e.g. `0`, `5`) | `5` | Minimum blocks player must move to keep earning shards. Set to `0` to completely disable movement requirement for passive AFK shard earning. |
| `SHARDS.CUBOIDS.REGIONS.spawn.RESET-ON-LEAVE` | `bool` | true, false | `True` | Configures `RESET-ON-LEAVE` for `SHARDS`. |
| `SHARDS.CUBOIDS.REGIONS.spawn.PAUSED-MESSAGE` | `str` | Any string text | `&eMove to keep earning shards &7(%mov...` | Configures `PAUSED-MESSAGE` for `SHARDS`. |
| `SHARDS.CUBOIDS.REGIONS.spawn.AFK-PAUSED-MESSAGE` | `str` | Any string text | `&cYou are AFK. Move to resume shard gain` | Configures `AFK-PAUSED-MESSAGE` for `SHARDS`. |
| `SHARDS.CUBOIDS.REGIONS.spawn.EXCLUDED-WORLD-MESSAGE` | `str` | Any string text | `&cShards are disabled in this world` | Configures `EXCLUDED-WORLD-MESSAGE` for `SHARDS`. |
| `SHARDS.BOOSTER-APPLIES-TO-KILLS` | `bool` | true, false | `True` | Whether an active shard booster also multiplies player kill rewards. Set to `false` to keep the booster on passive and cuboid shards only. |
| `SHARDS.BOOSTER-KILL-MULTIPLIER` | `int` | Any valid integer | `0` | Separate booster multiplier used only for kill rewards. Set to `0` to reuse `SHARDS.BOOSTER-MULTIPLIER`. |
| *(10 additional sub-keys configured in config.yml)* | | | | |

---

# database.yml - Database & Storage Engine

## Section: `DATABASE` - Database Backend Selection (SQLite, MySQL, MongoDB)

### Fully Commented Setup Code Example
```yaml
DATABASE:
  TYPE: SQLITE
  # Configuration section for Sqlite.
  SQLITE:
    # The text or value for File. Available options: Any valid string text
    FILE: data/data.db
  # Configuration section for Mysql.
  MYSQL:
    # The text or value for Host. Available options: Any valid string text
    HOST: localhost
    # The numerical value for Port. Available options: Any valid integer
    PORT: 3306
    # The text or value for Database. Available options: Any valid string text
    DATABASE: ultimatedonutsmp
    # The text or value for Username. Available options: Any valid string text
    USERNAME: root
    # The text or value for Password. Available options: Any valid string text
    PASSWORD: ''
    # Determines whether Create Database is enabled or disabled. Available options: true, false
    CREATE-DATABASE: true
    # The text or value for Parameters. Available options: Any valid string text
    PARAMETERS: useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=utf8
  # Configuration section for Mongodb.
  MONGODB:
    # The text or value for Uri. Available options: Any valid string text
    URI: mongodb://localhost:27017
    # The text or value for Database. Available options: Any valid string text
    DATABASE: ultimatedonutsmp
    # The text or value for Cache File. Available options: Any valid string text
    CACHE-FILE: data/mongodb-cache.db
    # Determines whether Sync On Autosave is enabled or disabled. Available options: true, false
    SYNC-ON-AUTOSAVE: true
# Configuration section for Redis.
```

### Key Options & Setup Breakdown
| Key / Option Path | Data Type | Allowed Values / Options | Default | Functional Behavior & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `DATABASE.TYPE` | `str` | `"SQLITE"`, `"MYSQL"`, `"MONGODB"` | `SQLITE` | Storage engine selection:<br>- `"SQLITE"`: Default zero-setup local database.<br>- `"MYSQL"`: Centralized database for multi-server networks.<br>- `"MONGODB"`: MongoDB document database. |
| `DATABASE.SQLITE.FILE` | `str` | Any string text | `data/data.db` | Configures `FILE` for `DATABASE`. |
| `DATABASE.MYSQL.HOST` | `str` | Any string text | `localhost` | Configures `HOST` for `DATABASE`. |
| `DATABASE.MYSQL.PORT` | `int` | Any valid integer | `3306` | Configures `PORT` for `DATABASE`. |
| `DATABASE.MYSQL.DATABASE` | `str` | Any string text | `ultimatedonutsmp` | Configures `DATABASE` for `DATABASE`. |
| `DATABASE.MYSQL.USERNAME` | `str` | Any string text | `root` | Configures `USERNAME` for `DATABASE`. |
| `DATABASE.MYSQL.PASSWORD` | `str` | Any string text | `` | Configures `PASSWORD` for `DATABASE`. |
| `DATABASE.MYSQL.CREATE-DATABASE` | `bool` | true, false | `True` | Configures `CREATE-DATABASE` for `DATABASE`. |
| `DATABASE.MYSQL.PARAMETERS` | `str` | Any string text | `useSSL=false&allowPublicKeyRetrieval=...` | Configures `PARAMETERS` for `DATABASE`. |
| `DATABASE.MONGODB.URI` | `str` | Any string text | `mongodb://localhost:27017` | Configures `URI` for `DATABASE`. |
| `DATABASE.MONGODB.DATABASE` | `str` | Any string text | `ultimatedonutsmp` | Configures `DATABASE` for `DATABASE`. |
| `DATABASE.MONGODB.CACHE-FILE` | `str` | Any string text | `data/mongodb-cache.db` | Configures `CACHE-FILE` for `DATABASE`. |
| `DATABASE.MONGODB.SYNC-ON-AUTOSAVE` | `bool` | true, false | `True` | Configures `SYNC-ON-AUTOSAVE` for `DATABASE`. |

---

## Section: `REDIS` - Redis Connection Pool & Pub/Sub Settings

### Fully Commented Setup Code Example
```yaml
REDIS:
  # Determines whether Enabled is enabled or disabled. Available options: true, false
  ENABLED: false
  # The text or value for Host. Available options: Any valid string text
  HOST: localhost
  # The numerical value for Port. Available options: Any valid integer
  PORT: 6379
  # The numerical value for Timeout. Available options: Any valid integer
  TIMEOUT: 2000
  # The text or value for Password. Available options: Any valid string text
  PASSWORD: ''
  # The numerical value for Database. Available options: Any valid integer
  DATABASE: 0
  # The numerical value for Max Total. Available options: Any valid integer
  MAX-TOTAL: 50
  # The numerical value for Max Idle. Available options: Any valid integer
  MAX-IDLE: 10
  # The numerical value for Min Idle. Available options: Any valid integer
  MIN-IDLE: 5
  # Determines whether Test On Borrow is enabled or disabled. Available options: true, false
  TEST-ON-BORROW: false
  # Determines whether Test On Return is enabled or disabled. Available options: true, false
  TEST-ON-RETURN: false
  # Determines whether Test While Idle is enabled or disabled. Available options: true, false
  TEST-WHILE-IDLE: false
  # The numerical value for Reconnect Delay Ms. Available options: Any valid integer
  RECONNECT-DELAY-MS: 5000
```

### Key Options & Setup Breakdown
| Key / Option Path | Data Type | Allowed Values / Options | Default | Functional Behavior & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `REDIS.ENABLED` | `bool` | true, false | `False` | Configures `ENABLED` for `REDIS`. |
| `REDIS.HOST` | `str` | Any string text | `localhost` | Configures `HOST` for `REDIS`. |
| `REDIS.PORT` | `int` | Any valid integer | `6379` | Configures `PORT` for `REDIS`. |
| `REDIS.TIMEOUT` | `int` | Any valid integer | `2000` | Configures `TIMEOUT` for `REDIS`. |
| `REDIS.PASSWORD` | `str` | Any string text | `` | Configures `PASSWORD` for `REDIS`. |
| `REDIS.DATABASE` | `int` | Any valid integer | `0` | Configures `DATABASE` for `REDIS`. |
| `REDIS.MAX-TOTAL` | `int` | Any valid integer | `50` | Configures `MAX-TOTAL` for `REDIS`. |
| `REDIS.MAX-IDLE` | `int` | Any valid integer | `10` | Configures `MAX-IDLE` for `REDIS`. |
| `REDIS.MIN-IDLE` | `int` | Any valid integer | `5` | Configures `MIN-IDLE` for `REDIS`. |
| `REDIS.TEST-ON-BORROW` | `bool` | true, false | `False` | Configures `TEST-ON-BORROW` for `REDIS`. |
| `REDIS.TEST-ON-RETURN` | `bool` | true, false | `False` | Configures `TEST-ON-RETURN` for `REDIS`. |
| `REDIS.TEST-WHILE-IDLE` | `bool` | true, false | `False` | Configures `TEST-WHILE-IDLE` for `REDIS`. |
| `REDIS.RECONNECT-DELAY-MS` | `int` | Any valid integer | `5000` | Configures `RECONNECT-DELAY-MS` for `REDIS`. |

---

# network.yml - Redis Multi-Server Networking

## Section: `NETWORK` - Cross-Server Staff Chat, Alerts & Status Sync

### Fully Commented Setup Code Example
```yaml
NETWORK:
  # Enable or disable the cross-server network system globally (true / false)
  ENABLED: true

  # Enable cross-server staff chat sync via Redis (true / false)
  STAFF_CHAT_ENABLED: true

  # Enable cross-server helpop notification sync (true / false)
  HELPOP_ENABLED: true

  # Enable cross-server report notification sync (true / false)
  REPORT_ENABLED: true

  # Enable cross-server staff join/leave notifications (true / false)
  STAFF_JOIN_LEAVE_ENABLED: true

  # Enable cross-server status heartbeat monitoring (true / false)
  SERVER_STATUS_ENABLED: true

  # Unique server identifier for this local server instance
  LOCAL_SERVER_ID: crystal

  # User-friendly server display name
  LOCAL_DISPLAY_NAME: Crystal

  # Redis pub/sub channel for staff chat messages
  REDIS_CHANNEL: ultimatedonutsmp:staff-chat

  # Redis pub/sub channel for helpop alerts
  HELPOP_REDIS_CHANNEL: ultimatedonutsmp:staff-alerts

  # Redis pub/sub channel for player reports
  REPORT_REDIS_CHANNEL: ultimatedonutsmp:staff-alerts

  # Broadcast staff chat locally if Redis connection fails (true / false)
  SEND_LOCAL_FALLBACK_ON_REDIS_ERROR: true

  # Broadcast staff alerts locally if Redis connection fails (true / false)
  STAFF_ALERTS_LOCAL_FALLBACK_ON_REDIS_ERROR: true

  # Warn sending player if staff alert Redis delivery fails (true / false)
  STAFF_ALERTS_WARN_SENDER_ON_REDIS_ERROR: false

  # Log staff chat messages to local server console (true / false)
  LOG_TO_CONSOLE: true

  # Log staff alerts to local server console (true / false)
  STAFF_ALERTS_LOG_TO_CONSOLE: true

  # Maximum allowed staff chat message length (in characters)
  MAX_MESSAGE_LENGTH: 512

  # Maximum allowed report/helpop reason text length (in characters)
  STAFF_ALERTS_MAX_REASON_LENGTH: 256

  # Cooldown between helpop submissions per player (in seconds)
  HELPOP_COOLDOWN_SECONDS: 30

  # Cooldown between report submissions per player (in seconds)
  REPORT_COOLDOWN_SECONDS: 60

  # Message format for server online/offline status broadcasts
  SERVER_STATUS: '&6%server% &eis now %status%&e.'

  # Message format for cross-server staff chat messages
  STAFF_CHAT: '&8[&dNetwork&8] &7[%server%] &e%player%&8: &f%message%'

  # Message format for staff member server join alert
  STAFF_JOIN: '&8[&a+&8] &a%player% &7joined &b%server%'

  # Message format for staff member server leave alert
  STAFF_LEAVE: '&8[&c-&8] &a%player% &7left &b%server%'

# Network status monitoring & HTTP endpoint configuration
```

### Key Options & Setup Breakdown
| Key / Option Path | Data Type | Allowed Values / Options | Default | Functional Behavior & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `NETWORK.ENABLED` | `bool` | true, false | `True` | Configures `ENABLED` for `NETWORK`. |
| `NETWORK.STAFF_CHAT_ENABLED` | `bool` | true, false | `True` | Configures `STAFF_CHAT_ENABLED` for `NETWORK`. |
| `NETWORK.HELPOP_ENABLED` | `bool` | true, false | `True` | Configures `HELPOP_ENABLED` for `NETWORK`. |
| `NETWORK.REPORT_ENABLED` | `bool` | true, false | `True` | Configures `REPORT_ENABLED` for `NETWORK`. |
| `NETWORK.STAFF_JOIN_LEAVE_ENABLED` | `bool` | true, false | `True` | Configures `STAFF_JOIN_LEAVE_ENABLED` for `NETWORK`. |
| `NETWORK.SERVER_STATUS_ENABLED` | `bool` | true, false | `True` | Configures `SERVER_STATUS_ENABLED` for `NETWORK`. |
| `NETWORK.LOCAL_SERVER_ID` | `str` | Any string text | `crystal` | Configures `LOCAL_SERVER_ID` for `NETWORK`. |
| `NETWORK.LOCAL_DISPLAY_NAME` | `str` | Any string text | `Crystal` | Configures `LOCAL_DISPLAY_NAME` for `NETWORK`. |
| `NETWORK.REDIS_CHANNEL` | `str` | Any string text | `ultimatedonutsmp:staff-chat` | Configures `REDIS_CHANNEL` for `NETWORK`. |
| `NETWORK.HELPOP_REDIS_CHANNEL` | `str` | Any string text | `ultimatedonutsmp:staff-alerts` | Configures `HELPOP_REDIS_CHANNEL` for `NETWORK`. |
| `NETWORK.REPORT_REDIS_CHANNEL` | `str` | Any string text | `ultimatedonutsmp:staff-alerts` | Configures `REPORT_REDIS_CHANNEL` for `NETWORK`. |
| `NETWORK.SEND_LOCAL_FALLBACK_ON_REDIS_ERROR` | `bool` | true, false | `True` | Configures `SEND_LOCAL_FALLBACK_ON_REDIS_ERROR` for `NETWORK`. |
| `NETWORK.STAFF_ALERTS_LOCAL_FALLBACK_ON_REDIS_ERROR` | `bool` | true, false | `True` | Configures `STAFF_ALERTS_LOCAL_FALLBACK_ON_REDIS_ERROR` for `NETWORK`. |
| `NETWORK.STAFF_ALERTS_WARN_SENDER_ON_REDIS_ERROR` | `bool` | true, false | `False` | Configures `STAFF_ALERTS_WARN_SENDER_ON_REDIS_ERROR` for `NETWORK`. |
| `NETWORK.LOG_TO_CONSOLE` | `bool` | true, false | `True` | Configures `LOG_TO_CONSOLE` for `NETWORK`. |
| `NETWORK.STAFF_ALERTS_LOG_TO_CONSOLE` | `bool` | true, false | `True` | Configures `STAFF_ALERTS_LOG_TO_CONSOLE` for `NETWORK`. |
| `NETWORK.MAX_MESSAGE_LENGTH` | `int` | Any valid integer | `512` | Configures `MAX_MESSAGE_LENGTH` for `NETWORK`. |
| `NETWORK.STAFF_ALERTS_MAX_REASON_LENGTH` | `int` | Any valid integer | `256` | Configures `STAFF_ALERTS_MAX_REASON_LENGTH` for `NETWORK`. |
| `NETWORK.HELPOP_COOLDOWN_SECONDS` | `int` | Any valid integer | `30` | Configures `HELPOP_COOLDOWN_SECONDS` for `NETWORK`. |
| `NETWORK.REPORT_COOLDOWN_SECONDS` | `int` | Any valid integer | `60` | Configures `REPORT_COOLDOWN_SECONDS` for `NETWORK`. |
| `NETWORK.SERVER_STATUS` | `str` | Any string text | `&6%server% &eis now %status%&e.` | Configures `SERVER_STATUS` for `NETWORK`. |
| `NETWORK.STAFF_CHAT` | `str` | Any string text | `&8[&dNetwork&8] &7[%server%] &e%playe...` | Configures `STAFF_CHAT` for `NETWORK`. |
| `NETWORK.STAFF_JOIN` | `str` | Any string text | `&8[&a+&8] &a%player% &7joined &b%server%` | Configures `STAFF_JOIN` for `NETWORK`. |
| `NETWORK.STAFF_LEAVE` | `str` | Any string text | `&8[&c-&8] &a%player% &7left &b%server%` | Configures `STAFF_LEAVE` for `NETWORK`. |

---

## Section: `NETWORK-STATUS` - Network Status Dashboard & HTTP Endpoint

### Fully Commented Setup Code Example
```yaml
NETWORK-STATUS:
  # Enable network status monitoring dashboard (true / false)
  ENABLED: true

  # Local server ID alias for status check
  LOCAL-SERVER-ID: crystal

  # Local display name alias for status check
  LOCAL-DISPLAY-NAME: Crystal

  # Interval in seconds between network heartbeat status refreshes
  REFRESH-SECONDS: 5

  # Timeout in milliseconds for server ping status checks
  TIMEOUT-MS: 1500

  # Internal REST API HTTP endpoint for external monitoring
  ENDPOINT:
    # Enable HTTP status endpoint server (true / false)
    ENABLED: false
    # Host IP address to bind HTTP endpoint server
    HOST: 0.0.0.0
    # Port number for HTTP status endpoint
    PORT: 8123
    # Endpoint URI path
    PATH: /status
    # Secret authorization token for HTTP status queries
    TOKEN: change-me

  # Configuration for remote network servers to monitor
  SERVERS:
    crystal:
      DISPLAY: Crystal
      SOURCE:
        TYPE: LOCAL
```

### Key Options & Setup Breakdown
| Key / Option Path | Data Type | Allowed Values / Options | Default | Functional Behavior & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `NETWORK-STATUS.ENABLED` | `bool` | true, false | `True` | Configures `ENABLED` for `NETWORK-STATUS`. |
| `NETWORK-STATUS.LOCAL-SERVER-ID` | `str` | Any string text | `crystal` | Configures `LOCAL-SERVER-ID` for `NETWORK-STATUS`. |
| `NETWORK-STATUS.LOCAL-DISPLAY-NAME` | `str` | Any string text | `Crystal` | Configures `LOCAL-DISPLAY-NAME` for `NETWORK-STATUS`. |
| `NETWORK-STATUS.REFRESH-SECONDS` | `int` | Any valid integer | `5` | Configures `REFRESH-SECONDS` for `NETWORK-STATUS`. |
| `NETWORK-STATUS.TIMEOUT-MS` | `int` | Any valid integer | `1500` | Configures `TIMEOUT-MS` for `NETWORK-STATUS`. |
| `NETWORK-STATUS.ENDPOINT.ENABLED` | `bool` | true, false | `False` | Configures `ENABLED` for `NETWORK-STATUS`. |
| `NETWORK-STATUS.ENDPOINT.HOST` | `str` | Any string text | `0.0.0.0` | Configures `HOST` for `NETWORK-STATUS`. |
| `NETWORK-STATUS.ENDPOINT.PORT` | `int` | Any valid integer | `8123` | Configures `PORT` for `NETWORK-STATUS`. |
| `NETWORK-STATUS.ENDPOINT.PATH` | `str` | Any string text | `/status` | Configures `PATH` for `NETWORK-STATUS`. |
| `NETWORK-STATUS.ENDPOINT.TOKEN` | `str` | Any string text | `change-me` | Configures `TOKEN` for `NETWORK-STATUS`. |
| `NETWORK-STATUS.SERVERS.crystal.DISPLAY` | `str` | Any string text | `Crystal` | Configures `DISPLAY` for `NETWORK-STATUS`. |
| `NETWORK-STATUS.SERVERS.crystal.SOURCE.TYPE` | `str` | Any string text | `LOCAL` | Configures `TYPE` for `NETWORK-STATUS`. |

---

# discord.yml - Discord Webhook Integration

## Section: `WEBHOOKS` - Discord Webhooks & Custom Event Embeds

### Fully Commented Setup Code Example
```yaml
WEBHOOKS:
  # Determines whether Enabled is enabled or disabled. Available options: true, false
  ENABLED: true
  # The text or value for Url. Available options: Any valid string text
  URL: https://discord.com/api/webhooks/your_webhook_here
  # The text or value for Avatar Api. Available options: Any valid string text
  AVATAR_API: https://visage.surgeplay.com/face/128/%uuid_no_dash%
  # The text or value for Model Api. Available options: Any valid string text
  MODEL_API: https://visage.surgeplay.com/full/384/%uuid_no_dash%
  # The text or value for Bust Api. Available options: Any valid string text
  BUST_API: https://visage.surgeplay.com/bust/384/%uuid_no_dash%
  MESSAGES:
    # Configuration section for Ban.
    BAN:
      # Determines whether Enabled is enabled or disabled. Available options: true, false
      ENABLED: true
      TITLE: Player Banned - %player%
      # The text or value for Color. Available options: Any valid string text
      COLOR: '#FF0000'
      # The text or value for Description. Available options: Any valid string text
      DESCRIPTION: ':hammer: **Punishment Type:** Ban

        # The text or value for # The Text Or Mode For **Player. Available Options. Available options: Any valid string text
        # The text or mode for **Player. Available options: Any string text **Player:**
        %player%

        # The text or value for # The Text Or Mode For **Staff. Available Options. Available options: Any valid string text
        # The text or mode for **Staff. Available options: Any string text **Staff:**
        %staff%

        # The text or value for # The Text Or Mode For **Reason. Available Options. Available options: Any valid string text
        # The text or mode for **Reason. Available options: Any string text **Reason:**
        ||%reason%||

        # The text or value for # The Text Or Mode For **Duration. Available Options. Available options: Any valid string text
        # The text or mode for **Duration. Available options: Any string text **Duration:**
        %duration%

        # The text or value for # The Text Or Mode For **Date. Available Options. Available options: Any valid string text
        # The text or mode for **Date. Available options: Any string text **Date:**
        %date%

        # The text or value for # The Text Or Mode For **Id. Available Options. Available options: Any valid string text
        # The text or mode for **Id. Available options: Any string text **ID:** `%id%`

        '
      # The text or value for Thumbnail. Available options: Any valid string text
      THUMBNAIL: '%skin_bust%'
      # The text or value for Author Name. Available options: Any valid string text
      AUTHOR_NAME: Ban System
      # The text or value for Footer. Available options: Any valid string text
      FOOTER: Punishment issued via server
    # Configuration section for Mute.
    MUTE:
      # Determines whether Enabled is enabled or disabled. Available options: true, false
      ENABLED: true
      TITLE: Player Muted - %player%
      # The text or value for Color. Available options: Any valid string text
      COLOR: '#FFFF00'
      # The text or value for Description. Available options: Any valid string text
      DESCRIPTION: ':mute: **Punishment Type:** Mute

        # The text or value for # The Text Or Mode For **Player. Available Options. Available options: Any valid string text
        # The text or mode for **Player. Available options: Any string text **Player:**
        %player%

        # The text or value for # The Text Or Mode For **Staff. Available Options. Available options: Any valid string text
        # The text or mode for **Staff. Available options: Any string text **Staff:**
        %staff%

        # The text or value for # The Text Or Mode For **Reason. Available Options. Available options: Any valid string text
        # The text or mode for **Reason. Available options: Any string text **Reason:**
        ||%reason%||

        # The text or value for # The Text Or Mode For **Duration. Available Options. Available options: Any valid string text
        # The text or mode for **Duration. Available options: Any string text **Duration:**
        %duration%

        # The text or value for # The Text Or Mode For **Date. Available Options. Available options: Any valid string text
        # The text or mode for **Date. Available options: Any string text **Date:**
        %date%

        # The text or value for # The Text Or Mode For **Id. Available Options. Available options: Any valid string text
        # The text or mode for **Id. Available options: Any string text **ID:** `%id%`

        '
      # The text or value for Thumbnail. Available options: Any valid string text
      THUMBNAIL: '%skin_bust%'
      # The text or value for Author Name. Available options: Any valid string text
      AUTHOR_NAME: Moderation System
      # The text or value for Footer. Available options: Any valid string text
      FOOTER: Chat restriction applied
    # Configuration section for Warn.
    WARN:
      # Determines whether Enabled is enabled or disabled. Available options: true, false
      ENABLED: true
      TITLE: Player Warned - %player%
      # The text or value for Color. Available options: Any valid string text
      COLOR: '#FFA500'
      # The text or value for Description. Available options: Any valid string text
      DESCRIPTION: ':warning: **Punishment Type:** Warning

        # The text or value for # The Text Or Mode For **Player. Available Options. Available options: Any valid string text
        # The text or mode for **Player. Available options: Any string text **Player:**
        %player%

        # The text or value for # The Text Or Mode For **Staff. Available Options. Available options: Any valid string text
        # The text or mode for **Staff. Available options: Any string text **Staff:**
        %staff%

        # The text or value for # The Text Or Mode For **Reason. Available Options. Available options: Any valid string text
        # The text or mode for **Reason. Available options: Any string text **Reason:**
        ||%reason%||

        # The text or value for # The Text Or Mode For **Date. Available Options. Available options: Any valid string text
        # The text or mode for **Date. Available options: Any string text **Date:**
        %date%

        # The text or value for # The Text Or Mode For **Id. Available Options. Available options: Any valid string text
        # The text or mode for **Id. Available options: Any string text **ID:** `%id%`

        '
      # The text or value for Thumbnail. Available options: Any valid string text
      THUMBNAIL: '%skin_bust%'
      # The text or value for Author Name. Available options: Any valid string text
      AUTHOR_NAME: Moderation System
      # The text or value for Footer. Available options: Any valid string text
      FOOTER: Warning issued
    # Configuration section for Kick.
    KICK:
      # Determines whether Enabled is enabled or disabled. Available options: true, false
      ENABLED: true
      TITLE: Player Kicked - %player%
      # The text or value for Color. Available options: Any valid string text
      COLOR: '#FF6347'
      # The text or value for Description. Available options: Any valid string text
      DESCRIPTION: ':boot: **Punishment Type:** Kick

        # The text or value for # The Text Or Mode For **Player. Available Options. Available options: Any valid string text
        # The text or mode for **Player. Available options: Any string text **Player:**
        %player%

        # The text or value for # The Text Or Mode For **Staff. Available Options. Available options: Any valid string text
        # The text or mode for **Staff. Available options: Any string text **Staff:**
        %staff%

        # The text or value for # The Text Or Mode For **Reason. Available Options. Available options: Any valid string text
        # The text or mode for **Reason. Available options: Any string text **Reason:**
        ||%reason%||

        # The text or value for # The Text Or Mode For **Date. Available Options. Available options: Any valid string text
        # The text or mode for **Date. Available options: Any string text **Date:**
        %date%

        '
      # The text or value for Thumbnail. Available options: Any valid string text
      THUMBNAIL: '%skin_bust%'
      # The text or value for Author Name. Available options: Any valid string text
      AUTHOR_NAME: Moderation System
      # The text or value for Footer. Available options: Any valid string text
      FOOTER: Player was removed from server
    # Configuration section for Blacklist.
    BLACKLIST:
      # Determines whether Enabled is enabled or disabled. Available options: true, false
      ENABLED: true
      TITLE: PLAYER BLACKLISTED - %player%
      # The text or value for Color. Available options: Any valid string text
      COLOR: '#000000'
      # The text or value for Description. Available options: Any valid string text
      DESCRIPTION: ':no_entry: **PERMANENT NETWORK BAN**

        # The text or value for # The Text Or Mode For **Player. Available Options. Available options: Any valid string text
        # The text or mode for **Player. Available options: Any string text **Player:**
        %player%

        # The text or value for # The Text Or Mode For **Staff. Available Options. Available options: Any valid string text
        # The text or mode for **Staff. Available options: Any string text **Staff:**
        %staff%

        # The text or value for # The Text Or Mode For **Reason. Available Options. Available options: Any valid string text
        # The text or mode for **Reason. Available options: Any string text **Reason:**
        ||%reason%||

        # The text or value for # The Text Or Mode For **Date. Available Options. Available options: Any valid string text
        # The text or mode for **Date. Available options: Any string text **Date:**
        %date%

        # The text or value for # The Text Or Mode For **Id. Available Options. Available options: Any valid string text
        # The text or mode for **Id. Available options: Any string text **ID:** `%id%`

        # The text or value for # The Text Or Mode For . Available Options. Available options: Any valid string text
        # The text or mode for . Available options: Any string text :exclamation:
        This is a permanent network-wide ban

        '
      # The text or value for Thumbnail. Available options: Any valid string text
      THUMBNAIL: '%skin_bust%'
      # The text or value for Author Name. Available options: Any valid string text
      AUTHOR_NAME: Security System
      # The text or value for Footer. Available options: Any valid string text
      FOOTER: Blacklist enforced
```

### Key Options & Setup Breakdown
| Key / Option Path | Data Type | Allowed Values / Options | Default | Functional Behavior & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `WEBHOOKS.ENABLED` | `bool` | true, false | `True` | Configures `ENABLED` for `WEBHOOKS`. |
| `WEBHOOKS.URL` | `str` | Any string text | `https://discord.com/api/webhooks/your...` | Configures `URL` for `WEBHOOKS`. |
| `WEBHOOKS.AVATAR_API` | `str` | Any string text | `https://visage.surgeplay.com/face/128...` | Configures `AVATAR_API` for `WEBHOOKS`. |
| `WEBHOOKS.MODEL_API` | `str` | Any string text | `https://visage.surgeplay.com/full/384...` | Configures `MODEL_API` for `WEBHOOKS`. |
| `WEBHOOKS.BUST_API` | `str` | Any string text | `https://visage.surgeplay.com/bust/384...` | Configures `BUST_API` for `WEBHOOKS`. |
| `WEBHOOKS.MESSAGES.BAN.ENABLED` | `bool` | true, false | `True` | Configures `ENABLED` for `WEBHOOKS`. |
| `WEBHOOKS.MESSAGES.BAN.TITLE` | `str` | Any string text | `Player Banned - %player%` | Configures `TITLE` for `WEBHOOKS`. |
| `WEBHOOKS.MESSAGES.BAN.COLOR` | `str` | Any string text | `#FF0000` | Configures `COLOR` for `WEBHOOKS`. |
| `WEBHOOKS.MESSAGES.BAN.DESCRIPTION` | `str` | Any string text | `:hammer: **Punishment Type:** Ban # T...` | Configures `DESCRIPTION` for `WEBHOOKS`. |
| `WEBHOOKS.MESSAGES.BAN.THUMBNAIL` | `str` | Any string text | `%skin_bust%` | Configures `THUMBNAIL` for `WEBHOOKS`. |
| `WEBHOOKS.MESSAGES.BAN.AUTHOR_NAME` | `str` | Any string text | `Ban System` | Configures `AUTHOR_NAME` for `WEBHOOKS`. |
| `WEBHOOKS.MESSAGES.BAN.FOOTER` | `str` | Any string text | `Punishment issued via server` | Configures `FOOTER` for `WEBHOOKS`. |
| `WEBHOOKS.MESSAGES.MUTE.ENABLED` | `bool` | true, false | `True` | Configures `ENABLED` for `WEBHOOKS`. |
| `WEBHOOKS.MESSAGES.MUTE.TITLE` | `str` | Any string text | `Player Muted - %player%` | Configures `TITLE` for `WEBHOOKS`. |
| `WEBHOOKS.MESSAGES.MUTE.COLOR` | `str` | Any string text | `#FFFF00` | Configures `COLOR` for `WEBHOOKS`. |
| `WEBHOOKS.MESSAGES.MUTE.DESCRIPTION` | `str` | Any string text | `:mute: **Punishment Type:** Mute # Th...` | Configures `DESCRIPTION` for `WEBHOOKS`. |
| `WEBHOOKS.MESSAGES.MUTE.THUMBNAIL` | `str` | Any string text | `%skin_bust%` | Configures `THUMBNAIL` for `WEBHOOKS`. |
| `WEBHOOKS.MESSAGES.MUTE.AUTHOR_NAME` | `str` | Any string text | `Moderation System` | Configures `AUTHOR_NAME` for `WEBHOOKS`. |
| `WEBHOOKS.MESSAGES.MUTE.FOOTER` | `str` | Any string text | `Chat restriction applied` | Configures `FOOTER` for `WEBHOOKS`. |
| `WEBHOOKS.MESSAGES.WARN.ENABLED` | `bool` | true, false | `True` | Configures `ENABLED` for `WEBHOOKS`. |
| `WEBHOOKS.MESSAGES.WARN.TITLE` | `str` | Any string text | `Player Warned - %player%` | Configures `TITLE` for `WEBHOOKS`. |
| `WEBHOOKS.MESSAGES.WARN.COLOR` | `str` | Any string text | `#FFA500` | Configures `COLOR` for `WEBHOOKS`. |
| `WEBHOOKS.MESSAGES.WARN.DESCRIPTION` | `str` | Any string text | `:warning: **Punishment Type:** Warnin...` | Configures `DESCRIPTION` for `WEBHOOKS`. |
| `WEBHOOKS.MESSAGES.WARN.THUMBNAIL` | `str` | Any string text | `%skin_bust%` | Configures `THUMBNAIL` for `WEBHOOKS`. |
| `WEBHOOKS.MESSAGES.WARN.AUTHOR_NAME` | `str` | Any string text | `Moderation System` | Configures `AUTHOR_NAME` for `WEBHOOKS`. |
| `WEBHOOKS.MESSAGES.WARN.FOOTER` | `str` | Any string text | `Warning issued` | Configures `FOOTER` for `WEBHOOKS`. |
| `WEBHOOKS.MESSAGES.KICK.ENABLED` | `bool` | true, false | `True` | Configures `ENABLED` for `WEBHOOKS`. |
| `WEBHOOKS.MESSAGES.KICK.TITLE` | `str` | Any string text | `Player Kicked - %player%` | Configures `TITLE` for `WEBHOOKS`. |
| `WEBHOOKS.MESSAGES.KICK.COLOR` | `str` | Any string text | `#FF6347` | Configures `COLOR` for `WEBHOOKS`. |
| `WEBHOOKS.MESSAGES.KICK.DESCRIPTION` | `str` | Any string text | `:boot: **Punishment Type:** Kick # Th...` | Configures `DESCRIPTION` for `WEBHOOKS`. |
| *(10 additional sub-keys configured in discord.yml)* | | | | |

---

# spawners.yml - Donut-Style Stacked Spawners

## Section: `SPAWNERS` - Mob Spawner Stacking, Upgrades & Silk Touch

### Fully Commented Setup Code Example
```yaml
# Configuration section for SPAWNERS
SPAWNERS:
  # See default options in spawners.yml
```

### Key Options & Setup Breakdown
| Key / Option Path | Data Type | Allowed Values / Options | Default | Functional Behavior & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `SPAWNERS` | `section` | Configuration Map | *YAML* | Configures `SPAWNERS` settings in `spawners.yml`. |

---

# death-messages.yml - Death Message Broadcasting

## Section: `SETTINGS` - Death Message Scope & Radius Controls

### Fully Commented Setup Code Example
```yaml
SETTINGS:
  # Determines whether Radius is enabled or disabled. Available options: true, false
  RADIUS: true
  # The numerical value for Chunks. Available options: Any valid integer
  CHUNKS: 5
```

### Key Options & Setup Breakdown
| Key / Option Path | Data Type | Allowed Values / Options | Default | Functional Behavior & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `SETTINGS.RADIUS` | `bool` | `true`, `false` | `True` | If `true`, death messages are restricted to nearby chunk radii. Set to `false` to broadcast death messages GLOBALLY server-wide. |
| `SETTINGS.CHUNKS` | `int` | Any valid integer | `5` | Configures `CHUNKS` for `SETTINGS`. |

---

## Section: `MESSAGES` - Custom Death Cause Message Templates

### Fully Commented Setup Code Example
```yaml
MESSAGES:
  # Determines whether Enabled is enabled or disabled. Available options: true, false
  ENABLED: true
  # The text or value for Prefix. Available options: Any valid string text
  PREFIX: '&c☠ '
  # The text or value for Block Explosion. Available options: Any valid string text
  BLOCK-EXPLOSION: '{player} got blown to pieces'
  # The text or value for Contact. Available options: Any valid string text
  CONTACT: '{player} was pricked to death'
  # Configuration section for Drowning.
  DROWNING:
    # The text or value for Normal. Available options: Any valid string text
    NORMAL: '{player} drowned!'
    # The text or value for Pvp. Available options: Any valid string text
    PVP: '{player} drowned whilst trying to escape {killer}'
  # The text or value for Entity Attack. Available options: Any valid string text
  ENTITY-ATTACK: '{player} was slain by {killer}'
  # Configuration section for Fall.
  FALL:
    # The text or value for Normal. Available options: Any valid string text
    NORMAL: '{player} hit the ground too hard'
    # The text or value for Pvp. Available options: Any valid string text
    PVP: '{player} was doomed to fall by {killer}'
  # The text or value for Falling Block. Available options: Any valid string text
  FALLING-BLOCK: '{player} got freaking squashed by a block'
  # Configuration section for Fire.
  FIRE:
    # The text or value for Normal. Available options: Any valid string text
    NORMAL: '{player} went up in flames'
    # The text or value for Pvp. Available options: Any valid string text
    PVP: '{player} walked into a fire whilst fighting {killer}'
  # Configuration section for Fire Tick.
  FIRE-TICK:
    # The text or value for Normal. Available options: Any valid string text
    NORMAL: '{player} burned to death'
    # The text or value for Pvp. Available options: Any valid string text
    PVP: '{player} was burnt to a crisp whilst fighting {killer}'
  # Configuration section for Lava.
  LAVA:
    # The text or value for Normal. Available options: Any valid string text
    NORMAL: '{player} tried to swim in lava'
    # The text or value for Pvp. Available options: Any valid string text
    PVP: '{player} tried to swim in lava while trying to escape {killer}'
  # The text or value for Lightning. Available options: Any valid string text
  LIGHTNING: '{player} got lit the hell up by a lightning'
  # The text or value for Poison. Available options: Any valid string text
  POISON: '{player} was poisoned'
  # Configuration section for Projectile.
  PROJECTILE:
    # The text or value for Normal. Available options: Any valid string text
    NORMAL: '{player} was shot'
    # The text or value for Pvp. Available options: Any valid string text
    PVP: '{player} was shot by {killer}'
  # The text or value for Starvation. Available options: Any valid string text
  STARVATION: '{player} starved to death'
  # The text or value for Suffocation. Available options: Any valid string text
  SUFFOCATION: '{player} suffocated in a wall'
  # The text or value for Suicide. Available options: Any valid string text
  SUICIDE: '{player} took his own life like a peasant'
  # The text or value for Thorns. Available options: Any valid string text
  THORNS: '{player} killed themself by trying to kill someone'
  # Configuration section for Void.
  VOID:
    # The text or value for Normal. Available options: Any valid string text
    NORMAL: '{player} fell out of the world'
    # The text or value for Pvp. Available options: Any valid string text
    PVP: '{player} was knocked into the void by {killer}'
  # The text or value for Wither. Available options: Any valid string text
  WITHER: '{player} withered away'
  # Configuration section for Entity Explosion.
  ENTITY-EXPLOSION:
    # The text or value for Normal. Available options: Any valid string text
    NORMAL: '{player} was blown up'
    # The text or value for Pvp. Available options: Any valid string text
    PVP: '{player} was blown up by {killer}'
  # The text or value for Default. Available options: Any valid string text
  DEFAULT: '{player} died'
```

### Key Options & Setup Breakdown
| Key / Option Path | Data Type | Allowed Values / Options | Default | Functional Behavior & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `MESSAGES.ENABLED` | `bool` | true, false | `True` | Configures `ENABLED` for `MESSAGES`. |
| `MESSAGES.PREFIX` | `str` | Any string text | `&c☠ ` | Configures `PREFIX` for `MESSAGES`. |
| `MESSAGES.BLOCK-EXPLOSION` | `str` | Any string text | `{player} got blown to pieces` | Configures `BLOCK-EXPLOSION` for `MESSAGES`. |
| `MESSAGES.CONTACT` | `str` | Any string text | `{player} was pricked to death` | Configures `CONTACT` for `MESSAGES`. |
| `MESSAGES.DROWNING.NORMAL` | `str` | Any string text | `{player} drowned!` | Configures `NORMAL` for `MESSAGES`. |
| `MESSAGES.DROWNING.PVP` | `str` | Any string text | `{player} drowned whilst trying to esc...` | Configures `PVP` for `MESSAGES`. |
| `MESSAGES.ENTITY-ATTACK` | `str` | Any string text | `{player} was slain by {killer}` | Configures `ENTITY-ATTACK` for `MESSAGES`. |
| `MESSAGES.FALL.NORMAL` | `str` | Any string text | `{player} hit the ground too hard` | Configures `NORMAL` for `MESSAGES`. |
| `MESSAGES.FALL.PVP` | `str` | Any string text | `{player} was doomed to fall by {killer}` | Configures `PVP` for `MESSAGES`. |
| `MESSAGES.FALLING-BLOCK` | `str` | Any string text | `{player} got freaking squashed by a b...` | Configures `FALLING-BLOCK` for `MESSAGES`. |
| `MESSAGES.FIRE.NORMAL` | `str` | Any string text | `{player} went up in flames` | Configures `NORMAL` for `MESSAGES`. |
| `MESSAGES.FIRE.PVP` | `str` | Any string text | `{player} walked into a fire whilst fi...` | Configures `PVP` for `MESSAGES`. |
| `MESSAGES.FIRE-TICK.NORMAL` | `str` | Any string text | `{player} burned to death` | Configures `NORMAL` for `MESSAGES`. |
| `MESSAGES.FIRE-TICK.PVP` | `str` | Any string text | `{player} was burnt to a crisp whilst ...` | Configures `PVP` for `MESSAGES`. |
| `MESSAGES.LAVA.NORMAL` | `str` | Any string text | `{player} tried to swim in lava` | Configures `NORMAL` for `MESSAGES`. |
| `MESSAGES.LAVA.PVP` | `str` | Any string text | `{player} tried to swim in lava while ...` | Configures `PVP` for `MESSAGES`. |
| `MESSAGES.LIGHTNING` | `str` | Any string text | `{player} got lit the hell up by a lig...` | Configures `LIGHTNING` for `MESSAGES`. |
| `MESSAGES.POISON` | `str` | Any string text | `{player} was poisoned` | Configures `POISON` for `MESSAGES`. |
| `MESSAGES.PROJECTILE.NORMAL` | `str` | Any string text | `{player} was shot` | Configures `NORMAL` for `MESSAGES`. |
| `MESSAGES.PROJECTILE.PVP` | `str` | Any string text | `{player} was shot by {killer}` | Configures `PVP` for `MESSAGES`. |
| `MESSAGES.STARVATION` | `str` | Any string text | `{player} starved to death` | Configures `STARVATION` for `MESSAGES`. |
| `MESSAGES.SUFFOCATION` | `str` | Any string text | `{player} suffocated in a wall` | Configures `SUFFOCATION` for `MESSAGES`. |
| `MESSAGES.SUICIDE` | `str` | Any string text | `{player} took his own life like a pea...` | Configures `SUICIDE` for `MESSAGES`. |
| `MESSAGES.THORNS` | `str` | Any string text | `{player} killed themself by trying to...` | Configures `THORNS` for `MESSAGES`. |
| `MESSAGES.VOID.NORMAL` | `str` | Any string text | `{player} fell out of the world` | Configures `NORMAL` for `MESSAGES`. |
| `MESSAGES.VOID.PVP` | `str` | Any string text | `{player} was knocked into the void by...` | Configures `PVP` for `MESSAGES`. |
| `MESSAGES.WITHER` | `str` | Any string text | `{player} withered away` | Configures `WITHER` for `MESSAGES`. |
| `MESSAGES.ENTITY-EXPLOSION.NORMAL` | `str` | Any string text | `{player} was blown up` | Configures `NORMAL` for `MESSAGES`. |
| `MESSAGES.ENTITY-EXPLOSION.PVP` | `str` | Any string text | `{player} was blown up by {killer}` | Configures `PVP` for `MESSAGES`. |
| `MESSAGES.DEFAULT` | `str` | Any string text | `{player} died` | Configures `DEFAULT` for `MESSAGES`. |

---

# staff-mode.yml - Staff Mode & Moderation Tools

## Section: `STAFF_MODE` - Staff Mode Hotbar Tools & Inventory Isolation

### Fully Commented Setup Code Example
```yaml
# Configuration section for STAFF_MODE
STAFF_MODE:
  # See default options in staff-mode.yml
```

### Key Options & Setup Breakdown
| Key / Option Path | Data Type | Allowed Values / Options | Default | Functional Behavior & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `STAFF_MODE` | `section` | Configuration Map | *YAML* | Configures `STAFF_MODE` settings in `staff-mode.yml`. |

---

# crates.yml - Crates & Virtual Keys

## Section: `CRATES` - Crate Reward Tables, Animations & Keys

### Fully Commented Setup Code Example
```yaml
CRATES:
  # Configuration section for Common.
  common:
    # Determines whether Enabled is enabled or disabled. Available options: true, false
    ENABLED: true
    # The text or value for Open Type. Available options: Any valid string text
    OPEN-TYPE: CHOOSE_ONE
    # Configuration section for Display.
    DISPLAY:
      MATERIAL: CHEST
      DISPLAY-NAME: '&fCommon Crate'
      LORE:
      - '&7Keys: &f{keys}'
      - '&aClick to open and choose 1 reward.'
    # Configuration section for Key Item.
    KEY-ITEM:
      MATERIAL: TRIPWIRE_HOOK
      DISPLAY-NAME: '&fCommon Key'
      LORE:
      - '&7Opens the &fCommon Crate&7.'
    # The text or value for Permission. Available options: Any valid string text
    PERMISSION: ''
    # Determines whether Broadcast On Claim is enabled or disabled. Available options: true, false
    BROADCAST-ON-CLAIM: false
    # Configuration section for Menu.
    MENU:
      # The text or value for Open Title. Available options: Any valid string text
      OPEN-TITLE: '&8Choose 1 Reward'
      # The text or value for Confirm Title. Available options: Any valid string text
      CONFIRM-TITLE: '&8Confirm Reward'
      SIZE: 27
      # The text or value for Filler. Available options: Any valid string text
      FILLER: BLACK_STAINED_GLASS_PANE
      # The numerical value for Back Slot. Available options: Any valid integer
      BACK-SLOT: 26
      # Configuration section for Back Button.
      BACK-BUTTON:
        MATERIAL: BARRIER
        DISPLAY-NAME: '&cBack'
        LORE:
        - '&7Return to the crate list.'
    # Configuration section for Rewards.
    REWARDS:
      # Configuration section for Iron Helmet.
      iron_helmet:
        SLOT: 10
        # Configuration section for Display.
        DISPLAY:
          MATERIAL: IRON_HELMET
          DISPLAY-NAME: '&fIron Helmet'
          LORE:
          - '&7Choose this reward.'
        # Configuration section for Grant.
        GRANT:
          TYPE: ITEM
          MATERIAL: IRON_HELMET
          # The numerical value for Amount. Available options: Any valid integer
          AMOUNT: 1
      # Configuration section for Iron Chestplate.
      iron_chestplate:
        SLOT: 11
        # Configuration section for Display.
        DISPLAY:
          MATERIAL: IRON_CHESTPLATE
          DISPLAY-NAME: '&fIron Chestplate'
          LORE:
          - '&7Choose this reward.'
        # Configuration section for Grant.
        GRANT:
          TYPE: ITEM
          MATERIAL: IRON_CHESTPLATE
          # The numerical value for Amount. Available options: Any valid integer
          AMOUNT: 1
      # Configuration section for Iron Leggings.
      iron_leggings:
        SLOT: 12
        # Configuration section for Display.
        DISPLAY:
          MATERIAL: IRON_LEGGINGS
          DISPLAY-NAME: '&fIron Leggings'
          LORE:
          - '&7Choose this reward.'
        # Configuration section for Grant.
        GRANT:
          TYPE: ITEM
          MATERIAL: IRON_LEGGINGS
          # The numerical value for Amount. Available options: Any valid integer
          AMOUNT: 1
      # Configuration section for Iron Boots.
      iron_boots:
        SLOT: 13
        # Configuration section for Display.
        DISPLAY:
          MATERIAL: IRON_BOOTS
          DISPLAY-NAME: '&fIron Boots'
          LORE:
          - '&7Choose this reward.'
        # Configuration section for Grant.
        GRANT:
          TYPE: ITEM
          MATERIAL: IRON_BOOTS
          # The numerical value for Amount. Available options: Any valid integer
          AMOUNT: 1
      # Configuration section for Iron Sword.
      iron_sword:
        SLOT: 14
        # Configuration section for Display.
        DISPLAY:
          MATERIAL: IRON_SWORD
          DISPLAY-NAME: '&fIron Sword'
          LORE:
          - '&7Choose this reward.'
        # Configuration section for Grant.
        GRANT:
          TYPE: ITEM
          MATERIAL: IRON_SWORD
          # The numerical value for Amount. Available options: Any valid integer
          AMOUNT: 1
      # Configuration section for Money.
      money:
        SLOT: 15
        # Configuration section for Display.
        DISPLAY:
          MATERIAL: SUNFLOWER
          DISPLAY-NAME: '&e$5,000'
          LORE:
          - '&7Choose this reward.'
        # Configuration section for Grant.
        GRANT:
          TYPE: COMMAND
          # Configuration section for Commands.
          COMMANDS:
          - addmoney {player} 5000
  # Configuration section for Rare.
  rare:
    # Determines whether Enabled is enabled or disabled. Available options: true, false
    ENABLED: true
    # The text or value for Open Type. Available options: Any valid string text
    OPEN-TYPE: CHOOSE_ONE
    # Configuration section for Display.
    DISPLAY:
      MATERIAL: BARREL
      DISPLAY-NAME: '&#37BFF9Rare Crate'
      LORE:
      - '&7Keys: &f{keys}'
      - '&aClick to open and choose 1 reward.'
    # Configuration section for Key Item.
    KEY-ITEM:
      MATERIAL: TRIPWIRE_HOOK
      DISPLAY-NAME: '&#37BFF9Rare Key'
      LORE:
      - '&7Opens the &bRare Crate&7.'
    # The text or value for Permission. Available options: Any valid string text
    PERMISSION: ''
    # Determines whether Broadcast On Claim is enabled or disabled. Available options: true, false
    BROADCAST-ON-CLAIM: false
    # Configuration section for Menu.
    MENU:
      # The text or value for Open Title. Available options: Any valid string text
      OPEN-TITLE: '&8Rare Crate'
      # The text or value for Confirm Title. Available options: Any valid string text
      CONFIRM-TITLE: '&8Confirm Rare Reward'
      SIZE: 27
      # The text or value for Filler. Available options: Any valid string text
      FILLER: BLACK_STAINED_GLASS_PANE
      # The numerical value for Back Slot. Available options: Any valid integer
      BACK-SLOT: 26
      # Configuration section for Back Button.
      BACK-BUTTON:
        MATERIAL: BARRIER
        DISPLAY-NAME: '&cBack'
        LORE:
        - '&7Return to the crate list.'
    # Configuration section for Rewards.
    REWARDS:
      # Configuration section for Diamond Helmet.
      diamond_helmet:
        SLOT: 10
        # Configuration section for Display.
        DISPLAY:
          MATERIAL: DIAMOND_HELMET
          DISPLAY-NAME: '&bDiamond Helmet'
          LORE:
          - '&7Choose this reward.'
        # Configuration section for Grant.
        GRANT:
          TYPE: ITEM
          MATERIAL: DIAMOND_HELMET
          # The numerical value for Amount. Available options: Any valid integer
          AMOUNT: 1
      # Configuration section for Diamond Chestplate.
      diamond_chestplate:
        SLOT: 11
        # Configuration section for Display.
        DISPLAY:
          MATERIAL: DIAMOND_CHESTPLATE
          DISPLAY-NAME: '&bDiamond Chestplate'
          LORE:
          - '&7Choose this reward.'
        # Configuration section for Grant.
        GRANT:
          TYPE: ITEM
          MATERIAL: DIAMOND_CHESTPLATE
          # The numerical value for Amount. Available options: Any valid integer
          AMOUNT: 1
      # Configuration section for Diamond Leggings.
      diamond_leggings:
        SLOT: 12
        # Configuration section for Display.
        DISPLAY:
          MATERIAL: DIAMOND_LEGGINGS
          DISPLAY-NAME: '&bDiamond Leggings'
          LORE:
          - '&7Choose this reward.'
        # Configuration section for Grant.
        GRANT:
          TYPE: ITEM
          MATERIAL: DIAMOND_LEGGINGS
          # The numerical value for Amount. Available options: Any valid integer
          AMOUNT: 1
      # Configuration section for Diamond Boots.
      diamond_boots:
        SLOT: 13
        # Configuration section for Display.
        DISPLAY:
          MATERIAL: DIAMOND_BOOTS
          DISPLAY-NAME: '&bDiamond Boots'
          LORE:
          - '&7Choose this reward.'
        # Configuration section for Grant.
        GRANT:
          TYPE: ITEM
          MATERIAL: DIAMOND_BOOTS
          # The numerical value for Amount. Available options: Any valid integer
          AMOUNT: 1
      # Configuration section for Diamond Sword.
      diamond_sword:
        SLOT: 14
        # Configuration section for Display.
        DISPLAY:
          MATERIAL: DIAMOND_SWORD
          DISPLAY-NAME: '&bDiamond Sword'
          LORE:
          - '&7Choose this reward.'
        # Configuration section for Grant.
        GRANT:
          TYPE: ITEM
          MATERIAL: DIAMOND_SWORD
          # The numerical value for Amount. Available options: Any valid integer
          AMOUNT: 1
      # Configuration section for Money.
      money:
        SLOT: 15
        # Configuration section for Display.
        DISPLAY:
          MATERIAL: SUNFLOWER
          DISPLAY-NAME: '&e$15,000'
          LORE:
          - '&7Choose this reward.'
        # Configuration section for Grant.
        GRANT:
          TYPE: COMMAND
          # Configuration section for Commands.
          COMMANDS:
          - addmoney {player} 15000
  # Configuration section for Epic.
  epic:
    # Determines whether Enabled is enabled or disabled. Available options: true, false
    ENABLED: true
    # The text or value for Open Type. Available options: Any valid string text
    OPEN-TYPE: CHOOSE_ONE
    # Configuration section for Display.
    DISPLAY:
      MATERIAL: ENDER_CHEST
      DISPLAY-NAME: '&#A303F9Epic Crate'
      LORE:
      - '&7Keys: &f{keys}'
      - '&aClick to open and choose 1 reward.'
    # Configuration section for Key Item.
    KEY-ITEM:
      MATERIAL: TRIPWIRE_HOOK
      DISPLAY-NAME: '&#A303F9Epic Key'
      LORE:
      - '&7Opens the &5Epic Crate&7.'
    # The text or value for Permission. Available options: Any valid string text
    PERMISSION: ''
    # Determines whether Broadcast On Claim is enabled or disabled. Available options: true, false
    BROADCAST-ON-CLAIM: true
    # Configuration section for Menu.
    MENU:
      # The text or value for Open Title. Available options: Any valid string text
      OPEN-TITLE: '&8Epic Crate'
      # The text or value for Confirm Title. Available options: Any valid string text
      CONFIRM-TITLE: '&8Confirm Epic Reward'
      SIZE: 27
      # The text or value for Filler. Available options: Any valid string text
      FILLER: BLACK_STAINED_GLASS_PANE
      # The numerical value for Back Slot. Available options: Any valid integer
      BACK-SLOT: 26
      # Configuration section for Back Button.
      BACK-BUTTON:
        MATERIAL: BARRIER
        DISPLAY-NAME: '&cBack'
        LORE:
        - '&7Return to the crate list.'
    # Configuration section for Rewards.
    REWARDS:
      # Configuration section for Netherite Helmet.
      netherite_helmet:
        SLOT: 10
        # Configuration section for Display.
        DISPLAY:
          MATERIAL: NETHERITE_HELMET
          DISPLAY-NAME: '&5Netherite Helmet'
          LORE:
          - '&7Choose this reward.'
        # Configuration section for Grant.
        GRANT:
          TYPE: ITEM
          MATERIAL: NETHERITE_HELMET
          # The numerical value for Amount. Available options: Any valid integer
          AMOUNT: 1
      # Configuration section for Netherite Chestplate.
      netherite_chestplate:
        SLOT: 11
        # Configuration section for Display.
        DISPLAY:
          MATERIAL: NETHERITE_CHESTPLATE
          DISPLAY-NAME: '&5Netherite Chestplate'
          LORE:
          - '&7Choose this reward.'
        # Configuration section for Grant.
        GRANT:
          TYPE: ITEM
          MATERIAL: NETHERITE_CHESTPLATE
          # The numerical value for Amount. Available options: Any valid integer
          AMOUNT: 1
      # Configuration section for Netherite Leggings.
      netherite_leggings:
        SLOT: 12
        # Configuration section for Display.
        DISPLAY:
          MATERIAL: NETHERITE_LEGGINGS
          DISPLAY-NAME: '&5Netherite Leggings'
          LORE:
          - '&7Choose this reward.'
        # Configuration section for Grant.
        GRANT:
          TYPE: ITEM
          MATERIAL: NETHERITE_LEGGINGS
          # The numerical value for Amount. Available options: Any valid integer
          AMOUNT: 1
      # Configuration section for Netherite Boots.
      netherite_boots:
        SLOT: 13
        # Configuration section for Display.
        DISPLAY:
          MATERIAL: NETHERITE_BOOTS
          DISPLAY-NAME: '&5Netherite Boots'
          LORE:
          - '&7Choose this reward.'
        # Configuration section for Grant.
        GRANT:
          TYPE: ITEM
          MATERIAL: NETHERITE_BOOTS
          # The numerical value for Amount. Available options: Any valid integer
          AMOUNT: 1
      # Configuration section for Netherite Sword.
      netherite_sword:
        SLOT: 14
        # Configuration section for Display.
        DISPLAY:
          MATERIAL: NETHERITE_SWORD
          DISPLAY-NAME: '&5Netherite Sword'
          LORE:
          - '&7Choose this reward.'
        # Configuration section for Grant.
        GRANT:
          TYPE: ITEM
          MATERIAL: NETHERITE_SWORD
          # The numerical value for Amount. Available options: Any valid integer
          AMOUNT: 1
      # Configuration section for Money.
      money:
        SLOT: 15
        # Configuration section for Display.
        DISPLAY:
          MATERIAL: SUNFLOWER
          DISPLAY-NAME: '&e$50,000'
          LORE:
          - '&7Choose this reward.'
        # Configuration section for Grant.
        GRANT:
          TYPE: COMMAND
          # Configuration section for Commands.
          COMMANDS:
          - addmoney {player} 50000
```

### Key Options & Setup Breakdown
| Key / Option Path | Data Type | Allowed Values / Options | Default | Functional Behavior & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `CRATES.common.ENABLED` | `bool` | true, false | `True` | Configures `ENABLED` for `CRATES`. |
| `CRATES.common.OPEN-TYPE` | `str` | Any string text | `CHOOSE_ONE` | Configures `OPEN-TYPE` for `CRATES`. |
| `CRATES.common.DISPLAY.MATERIAL` | `str` | Any string text | `CHEST` | Configures `MATERIAL` for `CRATES`. |
| `CRATES.common.DISPLAY.DISPLAY-NAME` | `str` | Any string text | `&fCommon Crate` | Configures `DISPLAY-NAME` for `CRATES`. |
| `CRATES.common.DISPLAY.LORE` | `list` | Configured values | `['&7Keys: &f{keys}', '&aClick to open...` | Configures `LORE` for `CRATES`. |
| `CRATES.common.KEY-ITEM.MATERIAL` | `str` | Any string text | `TRIPWIRE_HOOK` | Configures `MATERIAL` for `CRATES`. |
| `CRATES.common.KEY-ITEM.DISPLAY-NAME` | `str` | Any string text | `&fCommon Key` | Configures `DISPLAY-NAME` for `CRATES`. |
| `CRATES.common.KEY-ITEM.LORE` | `list` | Configured values | `['&7Opens the &fCommon Crate&7.']` | Configures `LORE` for `CRATES`. |
| `CRATES.common.PERMISSION` | `str` | Any string text | `` | Configures `PERMISSION` for `CRATES`. |
| `CRATES.common.BROADCAST-ON-CLAIM` | `bool` | true, false | `False` | Configures `BROADCAST-ON-CLAIM` for `CRATES`. |
| `CRATES.common.MENU.OPEN-TITLE` | `str` | Any string text | `&8Choose 1 Reward` | Configures `OPEN-TITLE` for `CRATES`. |
| `CRATES.common.MENU.CONFIRM-TITLE` | `str` | Any string text | `&8Confirm Reward` | Configures `CONFIRM-TITLE` for `CRATES`. |
| `CRATES.common.MENU.SIZE` | `int` | Any valid integer | `27` | Configures `SIZE` for `CRATES`. |
| `CRATES.common.MENU.FILLER` | `str` | Any string text | `BLACK_STAINED_GLASS_PANE` | Configures `FILLER` for `CRATES`. |
| `CRATES.common.MENU.BACK-SLOT` | `int` | Any valid integer | `26` | Configures `BACK-SLOT` for `CRATES`. |
| `CRATES.common.MENU.BACK-BUTTON.MATERIAL` | `str` | Any string text | `BARRIER` | Configures `MATERIAL` for `CRATES`. |
| `CRATES.common.MENU.BACK-BUTTON.DISPLAY-NAME` | `str` | Any string text | `&cBack` | Configures `DISPLAY-NAME` for `CRATES`. |
| `CRATES.common.MENU.BACK-BUTTON.LORE` | `list` | Configured values | `['&7Return to the crate list.']` | Configures `LORE` for `CRATES`. |
| `CRATES.common.REWARDS.iron_helmet.SLOT` | `int` | Any valid integer | `10` | Configures `SLOT` for `CRATES`. |
| `CRATES.common.REWARDS.iron_helmet.DISPLAY.MATERIAL` | `str` | Any string text | `IRON_HELMET` | Configures `MATERIAL` for `CRATES`. |
| `CRATES.common.REWARDS.iron_helmet.DISPLAY.DISPLAY-NAME` | `str` | Any string text | `&fIron Helmet` | Configures `DISPLAY-NAME` for `CRATES`. |
| `CRATES.common.REWARDS.iron_helmet.DISPLAY.LORE` | `list` | Configured values | `['&7Choose this reward.']` | Configures `LORE` for `CRATES`. |
| `CRATES.common.REWARDS.iron_helmet.GRANT.TYPE` | `str` | Any string text | `ITEM` | Configures `TYPE` for `CRATES`. |
| `CRATES.common.REWARDS.iron_helmet.GRANT.MATERIAL` | `str` | Any string text | `IRON_HELMET` | Configures `MATERIAL` for `CRATES`. |
| `CRATES.common.REWARDS.iron_helmet.GRANT.AMOUNT` | `int` | Any valid integer | `1` | Configures `AMOUNT` for `CRATES`. |
| `CRATES.common.REWARDS.iron_chestplate.SLOT` | `int` | Any valid integer | `11` | Configures `SLOT` for `CRATES`. |
| `CRATES.common.REWARDS.iron_chestplate.DISPLAY.MATERIAL` | `str` | Any string text | `IRON_CHESTPLATE` | Configures `MATERIAL` for `CRATES`. |
| `CRATES.common.REWARDS.iron_chestplate.DISPLAY.DISPLAY-NAME` | `str` | Any string text | `&fIron Chestplate` | Configures `DISPLAY-NAME` for `CRATES`. |
| `CRATES.common.REWARDS.iron_chestplate.DISPLAY.LORE` | `list` | Configured values | `['&7Choose this reward.']` | Configures `LORE` for `CRATES`. |
| `CRATES.common.REWARDS.iron_chestplate.GRANT.TYPE` | `str` | Any string text | `ITEM` | Configures `TYPE` for `CRATES`. |
| *(147 additional sub-keys configured in crates.yml)* | | | | |

---

# auction-house.yml - Auction House Marketplace

## Section: `AUCTION-HOUSE` - AH Listing Fees, Max Listings & Expirations

### Fully Commented Setup Code Example
```yaml
# Configuration section for AUCTION-HOUSE
AUCTION-HOUSE:
  # See default options in auction-house.yml
```

### Key Options & Setup Breakdown
| Key / Option Path | Data Type | Allowed Values / Options | Default | Functional Behavior & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `AUCTION-HOUSE` | `section` | Configuration Map | *YAML* | Configures `AUCTION-HOUSE` settings in `auction-house.yml`. |
| `AUCTION-HOUSE.BOTS` | `section` | Configuration Map | `BOTS` | Automated Bot Auction Listing configuration (`ENABLED`, `MIN_CHECK_INTERVAL_SECONDS`, `MAX_CHECK_INTERVAL_SECONDS`, `CHANCE`, `MAX_ACTIVE_BOT_LISTINGS`, `BOT_NAMES`, `ITEMS`). |

---

# orders.yml - Buy Orders Board

## Section: `ORDERS` - Buy Orders Board Delivery Fees & Search Rules

### Fully Commented Setup Code Example
```yaml
# Configuration section for ORDERS
ORDERS:
  # See default options in orders.yml
```

### Key Options & Setup Breakdown
| Key / Option Path | Data Type | Allowed Values / Options | Default | Functional Behavior & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `ORDERS` | `section` | Configuration Map | *YAML* | Configures `ORDERS` settings in `orders.yml`. |
| `ORDERS.BOTS` | `section` | Configuration Map | `BOTS` | Automated Bot Item Order configuration (`ENABLED`, `MIN_CHECK_INTERVAL_SECONDS`, `MAX_CHECK_INTERVAL_SECONDS`, `CHANCE`, `MAX_ACTIVE_BOT_ORDERS`, `BOT_NAMES`, `ITEMS`). |

---

# billford.yml - Billford Rotating Trades NPC

## Section: `BILLFORD` - Billford NPC Trade Schedule & Cost Materials

### Fully Commented Setup Code Example
```yaml
BILLFORD:
  # Configuration section for 1.
  1:
    # The text or value for Display Name. Available options: Any valid string text
    DISPLAY_NAME: The Gem Exchange
    # The numerical value for Limit. Available options: Any valid integer
    LIMIT: 3
    # Configuration section for Inputs.
    INPUTS:
      # Configuration section for 1.
      1:
        SLOT: 11
        MATERIAL: DIAMOND
        # The numerical value for Quantity. Available options: Any valid integer
        QUANTITY: 3
      # Configuration section for 2.
      2:
        SLOT: 12
        MATERIAL: EMERALD
        # The numerical value for Quantity. Available options: Any valid integer
        QUANTITY: 5
    # Configuration section for Reward.
    REWARD:
      MATERIAL: PISTON
      # The numerical value for Quantity. Available options: Any valid integer
      QUANTITY: 64
      # The numerical value for Shard Bonus. Available options: Any valid integer
      SHARD_BONUS: 0
      # The numerical value for Money Bonus. Available options: Any valid integer
      MONEY_BONUS: 0
  # Configuration section for 2.
  2:
    # The text or value for Display Name. Available options: Any valid string text
    DISPLAY_NAME: The Utility Crate
    # The numerical value for Limit. Available options: Any valid integer
    LIMIT: 2
    # Configuration section for Inputs.
    INPUTS:
      # Configuration section for 1.
      1:
        SLOT: 10
        MATERIAL: IRON_INGOT
        # The numerical value for Quantity. Available options: Any valid integer
        QUANTITY: 32
      # Configuration section for 2.
      2:
        SLOT: 11
        MATERIAL: REDSTONE
        # The numerical value for Quantity. Available options: Any valid integer
        QUANTITY: 16
      # Configuration section for 3.
      3:
        SLOT: 12
        MATERIAL: EMERALD
        # The numerical value for Quantity. Available options: Any valid integer
        QUANTITY: 12
    # Configuration section for Reward.
    REWARD:
      MATERIAL: OBSERVER
      # The numerical value for Quantity. Available options: Any valid integer
      QUANTITY: 32
      # The numerical value for Shard Bonus. Available options: Any valid integer
      SHARD_BONUS: 40
      # The numerical value for Money Bonus. Available options: Any valid integer
      MONEY_BONUS: 0
  # Configuration section for 3.
  3:
    # The text or value for Display Name. Available options: Any valid string text
    DISPLAY_NAME: The Ingot Deal
    # The numerical value for Limit. Available options: Any valid integer
    LIMIT: 1
    # Configuration section for Inputs.
    INPUTS:
      # Configuration section for 1.
      1:
        SLOT: 11
        MATERIAL: NETHERITE_INGOT
        # The numerical value for Quantity. Available options: Any valid integer
        QUANTITY: 1
      # Configuration section for 2.
      2:
        SLOT: 12
        MATERIAL: BLAZE_ROD
        # The numerical value for Quantity. Available options: Any valid integer
        QUANTITY: 8
    # Configuration section for Reward.
    REWARD:
      MATERIAL: ELYTRA
      # The numerical value for Quantity. Available options: Any valid integer
      QUANTITY: 1
      # The numerical value for Shard Bonus. Available options: Any valid integer
      SHARD_BONUS: 120
      # The numerical value for Money Bonus. Available options: Any valid integer
      MONEY_BONUS: 50000
  # Configuration section for 4.
  4:
    # The text or value for Display Name. Available options: Any valid string text
    DISPLAY_NAME: The Builder Stash
    # The numerical value for Limit. Available options: Any valid integer
    LIMIT: 4
    # Configuration section for Inputs.
    INPUTS:
      # Configuration section for 1.
      1:
        SLOT: 10
        MATERIAL: GOLD_INGOT
        # The numerical value for Quantity. Available options: Any valid integer
        QUANTITY: 16
      # Configuration section for 2.
      2:
        SLOT: 11
        MATERIAL: LAPIS_LAZULI
        # The numerical value for Quantity. Available options: Any valid integer
        QUANTITY: 32
      # Configuration section for 3.
      3:
        SLOT: 12
        MATERIAL: AMETHYST_SHARD
        # The numerical value for Quantity. Available options: Any valid integer
        QUANTITY: 24
    # Configuration section for Reward.
    REWARD:
      MATERIAL: SEA_LANTERN
      # The numerical value for Quantity. Available options: Any valid integer
      QUANTITY: 48
      # The numerical value for Shard Bonus. Available options: Any valid integer
      SHARD_BONUS: 15
      # The numerical value for Money Bonus. Available options: Any valid integer
      MONEY_BONUS: 0
```

### Key Options & Setup Breakdown
| Key / Option Path | Data Type | Allowed Values / Options | Default | Functional Behavior & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `BILLFORD.1.DISPLAY_NAME` | `str` | Any string text | `The Gem Exchange` | Configures `DISPLAY_NAME` for `BILLFORD`. |
| `BILLFORD.1.LIMIT` | `int` | Any valid integer | `3` | Configures `LIMIT` for `BILLFORD`. |
| `BILLFORD.1.INPUTS.1.SLOT` | `int` | Any valid integer | `11` | Configures `SLOT` for `BILLFORD`. |
| `BILLFORD.1.INPUTS.1.MATERIAL` | `str` | Any string text | `DIAMOND` | Configures `MATERIAL` for `BILLFORD`. |
| `BILLFORD.1.INPUTS.1.QUANTITY` | `int` | Any valid integer | `3` | Configures `QUANTITY` for `BILLFORD`. |
| `BILLFORD.1.INPUTS.2.SLOT` | `int` | Any valid integer | `12` | Configures `SLOT` for `BILLFORD`. |
| `BILLFORD.1.INPUTS.2.MATERIAL` | `str` | Any string text | `EMERALD` | Configures `MATERIAL` for `BILLFORD`. |
| `BILLFORD.1.INPUTS.2.QUANTITY` | `int` | Any valid integer | `5` | Configures `QUANTITY` for `BILLFORD`. |
| `BILLFORD.1.REWARD.MATERIAL` | `str` | Any string text | `PISTON` | Configures `MATERIAL` for `BILLFORD`. |
| `BILLFORD.1.REWARD.QUANTITY` | `int` | Any valid integer | `64` | Configures `QUANTITY` for `BILLFORD`. |
| `BILLFORD.1.REWARD.SHARD_BONUS` | `int` | Any valid integer | `0` | Configures `SHARD_BONUS` for `BILLFORD`. |
| `BILLFORD.1.REWARD.MONEY_BONUS` | `int` | Any valid integer | `0` | Configures `MONEY_BONUS` for `BILLFORD`. |
| `BILLFORD.2.DISPLAY_NAME` | `str` | Any string text | `The Utility Crate` | Configures `DISPLAY_NAME` for `BILLFORD`. |
| `BILLFORD.2.LIMIT` | `int` | Any valid integer | `2` | Configures `LIMIT` for `BILLFORD`. |
| `BILLFORD.2.INPUTS.1.SLOT` | `int` | Any valid integer | `10` | Configures `SLOT` for `BILLFORD`. |
| `BILLFORD.2.INPUTS.1.MATERIAL` | `str` | Any string text | `IRON_INGOT` | Configures `MATERIAL` for `BILLFORD`. |
| `BILLFORD.2.INPUTS.1.QUANTITY` | `int` | Any valid integer | `32` | Configures `QUANTITY` for `BILLFORD`. |
| `BILLFORD.2.INPUTS.2.SLOT` | `int` | Any valid integer | `11` | Configures `SLOT` for `BILLFORD`. |
| `BILLFORD.2.INPUTS.2.MATERIAL` | `str` | Any string text | `REDSTONE` | Configures `MATERIAL` for `BILLFORD`. |
| `BILLFORD.2.INPUTS.2.QUANTITY` | `int` | Any valid integer | `16` | Configures `QUANTITY` for `BILLFORD`. |
| `BILLFORD.2.INPUTS.3.SLOT` | `int` | Any valid integer | `12` | Configures `SLOT` for `BILLFORD`. |
| `BILLFORD.2.INPUTS.3.MATERIAL` | `str` | Any string text | `EMERALD` | Configures `MATERIAL` for `BILLFORD`. |
| `BILLFORD.2.INPUTS.3.QUANTITY` | `int` | Any valid integer | `12` | Configures `QUANTITY` for `BILLFORD`. |
| `BILLFORD.2.REWARD.MATERIAL` | `str` | Any string text | `OBSERVER` | Configures `MATERIAL` for `BILLFORD`. |
| `BILLFORD.2.REWARD.QUANTITY` | `int` | Any valid integer | `32` | Configures `QUANTITY` for `BILLFORD`. |
| `BILLFORD.2.REWARD.SHARD_BONUS` | `int` | Any valid integer | `40` | Configures `SHARD_BONUS` for `BILLFORD`. |
| `BILLFORD.2.REWARD.MONEY_BONUS` | `int` | Any valid integer | `0` | Configures `MONEY_BONUS` for `BILLFORD`. |
| `BILLFORD.3.DISPLAY_NAME` | `str` | Any string text | `The Ingot Deal` | Configures `DISPLAY_NAME` for `BILLFORD`. |
| `BILLFORD.3.LIMIT` | `int` | Any valid integer | `1` | Configures `LIMIT` for `BILLFORD`. |
| `BILLFORD.3.INPUTS.1.SLOT` | `int` | Any valid integer | `11` | Configures `SLOT` for `BILLFORD`. |
| *(24 additional sub-keys configured in billford.yml)* | | | | |

---

# rtp.yml - Random Teleport Bounds

## Section: `RTP` - Wilderness Bounds, Min/Max Distance & Cooldowns

### Fully Commented Setup Code Example
```yaml
# Configuration section for RTP
RTP:
  # See default options in rtp.yml
```

### Key Options & Setup Breakdown
| Key / Option Path | Data Type | Allowed Values / Options | Default | Functional Behavior & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `RTP` | `section` | Configuration Map | *YAML* | Configures `RTP` settings in `rtp.yml`. |

---

# server-wipe.yml - Season Server Wipe Security

## Section: `SERVER-WIPE` - Season Wipe Passwords & Automated Pre-Wipe Backups

### Fully Commented Setup Code Example
```yaml
# Configuration section for SERVER-WIPE
SERVER-WIPE:
  # See default options in server-wipe.yml
```

### Key Options & Setup Breakdown
| Key / Option Path | Data Type | Allowed Values / Options | Default | Functional Behavior & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `SERVER-WIPE` | `section` | Configuration Map | *YAML* | Configures `SERVER-WIPE` settings in `server-wipe.yml`. |

---

