# Detailed Configuration & Setup Guide: `rtp.yml`

This is the official, 100% complete technical setup guide for `rtp.yml` in **UltimateDonutSMP**.
Each section details the exact commented setup code block, allowed option values, data types, default values, and in-depth functional behavior.

---

## Section: `ENABLED`

### 1. Commented Setup Code Example

```yaml
ENABLED: true

# General RTP settings
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `ENABLED` system. Set to `true` to enable, `false` to disable. |

### 3. Practical Setup Example

```yaml
ENABLED: true

# General RTP settings
```

---

## Section: `SETTINGS`

### 1. Commented Setup Code Example

```yaml
SETTINGS:
  # Maximum number of players allowed to perform RTP simultaneously
  PLAYERS-IN-RTP: 3
  # Maximum safe location search attempts per RTP request
  MAX-ATTEMPTS: 64
  # Maximum chunk samples to inspect while looking for a valid location
  MAX-CHUNK-SAMPLES: 128
  # Ticks between chunk samples
  ATTEMPT-INTERVAL-TICKS: 1
  # Number of location attempts evaluated in parallel per sample interval
  SEARCH-ATTEMPTS-PER-TICK: 4
  # Generate new chunks while searching. Keep false for pregenerated RTP worlds to protect TPS
  GENERATE-CHUNKS: false
  # Generate a limited number of chunks only after pregenerated/loaded RTP search cannot find a safe spot
  GENERATE-FALLBACK-CHUNKS: true
  # Chunk samples to try before limited fallback generation starts
  GENERATE-FALLBACK-AFTER-SAMPLES: 8
  # Maximum fallback chunks allowed to generate during one RTP search
  MAX-GENERATE-FALLBACK-SAMPLES: 32
  # Allow loading already-generated chunks from disk if chunk generation is disabled
  LOAD-GENERATED-CHUNKS: false
  # If random samples cannot be prepared, try already-loaded chunks as a fallback
  FALLBACK-TO-LOADED-CHUNKS: true
  # Chunk samples to try before loaded chunk fallback starts
  LOADED-CHUNK-FALLBACK-AFTER-SAMPLES: 32
  # Safe locations found ahead of time in the background so RTP can teleport without searching
  LOCATION-CACHE:
    # Enable or disable the background safe location cache
    ENABLED: true
    # How many ready locations are kept per RTP world. 0 disables the cache
    SIZE: 3
    # Seconds a cached location stays usable before it is thrown away. 0 keeps it until the next reload
    MAX-AGE-SECONDS: 600

# User feedback and status messages
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `SETTINGS.PLAYERS-IN-RTP` | `int` | Any valid integer number | `'3'` | Configures the technical `PLAYERS-IN-RTP` parameter for `SETTINGS.PLAYERS-IN-RTP` in `rtp.yml`. |
| `SETTINGS.MAX-ATTEMPTS` | `int` | Any valid integer number | `'64'` | Configures the technical `MAX-ATTEMPTS` parameter for `SETTINGS.MAX-ATTEMPTS` in `rtp.yml`. |
| `SETTINGS.MAX-CHUNK-SAMPLES` | `int` | Any valid integer number | `'128'` | Configures the technical `MAX-CHUNK-SAMPLES` parameter for `SETTINGS.MAX-CHUNK-SAMPLES` in `rtp.yml`. |
| `SETTINGS.ATTEMPT-INTERVAL-TICKS` | `int` | Any valid integer number | `'1'` | Configures the technical `ATTEMPT-INTERVAL-TICKS` parameter for `SETTINGS.ATTEMPT-INTERVAL-TICKS` in `rtp.yml`. |
| `SETTINGS.SEARCH-ATTEMPTS-PER-TICK` | `int` | Any valid integer number | `'4'` | How many candidate locations are checked side by side instead of one after another. Higher values find a spot sooner at the cost of more chunk work per search. |
| `SETTINGS.GENERATE-CHUNKS` | `bool` | `true`, `false` | `false` | Configures the technical `GENERATE-CHUNKS` parameter for `SETTINGS.GENERATE-CHUNKS` in `rtp.yml`. |
| `SETTINGS.GENERATE-FALLBACK-CHUNKS` | `bool` | `true`, `false` | `true` | Configures the technical `GENERATE-FALLBACK-CHUNKS` parameter for `SETTINGS.GENERATE-FALLBACK-CHUNKS` in `rtp.yml`. |
| `SETTINGS.GENERATE-FALLBACK-AFTER-SAMPLES` | `int` | Any valid integer number | `'8'` | Configures the technical `GENERATE-FALLBACK-AFTER-SAMPLES` parameter for `SETTINGS.GENERATE-FALLBACK-AFTER-SAMPLES` in `rtp.yml`. |
| `SETTINGS.MAX-GENERATE-FALLBACK-SAMPLES` | `int` | Any valid integer number | `'32'` | Configures the technical `MAX-GENERATE-FALLBACK-SAMPLES` parameter for `SETTINGS.MAX-GENERATE-FALLBACK-SAMPLES` in `rtp.yml`. |
| `SETTINGS.LOAD-GENERATED-CHUNKS` | `bool` | `true`, `false` | `false` | Configures the technical `LOAD-GENERATED-CHUNKS` parameter for `SETTINGS.LOAD-GENERATED-CHUNKS` in `rtp.yml`. |
| `SETTINGS.FALLBACK-TO-LOADED-CHUNKS` | `bool` | `true`, `false` | `true` | Configures the technical `FALLBACK-TO-LOADED-CHUNKS` parameter for `SETTINGS.FALLBACK-TO-LOADED-CHUNKS` in `rtp.yml`. |
| `SETTINGS.LOADED-CHUNK-FALLBACK-AFTER-SAMPLES` | `int` | Any valid integer number | `'32'` | Configures the technical `LOADED-CHUNK-FALLBACK-AFTER-SAMPLES` parameter for `SETTINGS.LOADED-CHUNK-FALLBACK-AFTER-SAMPLES` in `rtp.yml`. |
| `SETTINGS.LOCATION-CACHE` | `section` | See `SETTINGS.LOCATION-CACHE` below | See example | Keeps safe locations ready in the background so `/rtp` can teleport without running a search first. |

### 3. Practical Setup Example

```yaml
SETTINGS:
  # Maximum number of players allowed to perform RTP simultaneously
  PLAYERS-IN-RTP: 3
  # Maximum safe location search attempts per RTP request
  MAX-ATTEMPTS: 64
  # Maximum chunk samples to inspect while looking for a valid location
  MAX-CHUNK-SAMPLES: 128
  # Ticks between chunk samples
  ATTEMPT-INTERVAL-TICKS: 1
  # Number of location attempts evaluated in parallel per sample interval
  SEARCH-ATTEMPTS-PER-TICK: 4
  # Generate new chunks while searching. Keep false for pregenerated RTP worlds to protect TPS
  GENERATE-CHUNKS: false
  # Generate a limited number of chunks only after pregenerated/loaded RTP search cannot find a safe spot
  GENERATE-FALLBACK-CHUNKS: true
  # Chunk samples to try before limited fallback generation starts
  GENERATE-FALLBACK-AFTER-SAMPLES: 8
  # Maximum fallback chunks allowed to generate during one RTP search
  MAX-GENERATE-FALLBACK-SAMPLES: 32
  # Allow loading already-generated chunks from disk if chunk generation is disabled
  LOAD-GEN
```

---

## Section: `SETTINGS.LOCATION-CACHE`

Finding a safe spot is the slow part of `/rtp`, and on a large overworld it can keep a player waiting
for the best part of a minute. This cache does that work in the background, so the command
usually has a verified location waiting for it and teleports straight away.

Filling starts with the server and keeps going whether or not anyone is online, which is the point:
the first player to join should not be the one who pays for the search. What it will not do is fill
in a hurry. Exactly one background search runs at a time across the whole server, it takes the
worlds in turn, it waits a few seconds between searches, and it uses fewer parallel checks than a
search a player is waiting on. A search that has not finished within thirty seconds is stopped
outright rather than left running beside its replacement.

That pace matters most when `GENERATE-CHUNKS` is on, because then every search is generating fresh
terrain and the warm-up is doing real work rather than reading chunks that already exist. On a world
that has never been walked, expect the cache to take a few minutes to fill rather than seconds. That
is the intended trade: the generation happens while nobody is waiting instead of while somebody is.

Each entry is re-checked against the current world and radius before it is handed out, and a search
only starts when a world is short of ready locations. When the cache is empty the command falls back
to searching live, exactly as before.

### 1. Commented Setup Code Example

```yaml
  # Safe locations found ahead of time in the background so RTP can teleport without searching
  LOCATION-CACHE:
    # Enable or disable the background safe location cache
    ENABLED: true
    # How many ready locations are kept per RTP world. 0 disables the cache
    SIZE: 3
    # Seconds a cached location stays usable before it is thrown away. 0 keeps it until the next reload
    MAX-AGE-SECONDS: 600
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `SETTINGS.LOCATION-CACHE.ENABLED` | `bool` | `true`, `false` | `true` | Master toggle. When `false` every `/rtp` searches for a location the moment the player runs it. |
| `SETTINGS.LOCATION-CACHE.SIZE` | `int` | `0` to `16` | `'3'` | Ready locations kept per RTP world. Raise it on a busy server so back to back teleports stay instant, lower it to spend less time searching in the background. `0` turns the cache off. |
| `SETTINGS.LOCATION-CACHE.MAX-AGE-SECONDS` | `int` | Any valid integer number | `'600'` | How long a cached location may sit unused before it is discarded and searched again. `0` keeps entries until the next reload. |

### 3. Practical Setup Example

Keep more spots ready on a busy server and refresh them more often:

```yaml
SETTINGS:
  LOCATION-CACHE:
    ENABLED: true
    SIZE: 8
    MAX-AGE-SECONDS: 300
```

Turn the cache off entirely and go back to searching on demand:

```yaml
SETTINGS:
  LOCATION-CACHE:
    ENABLED: false
```

---

## Section: `SETTINGS.RANK-COOLDOWNS`

Per-rank `/rtp` cooldowns resolved from permissions, so different ranks can have different cooldowns
without a separate config entry per rank.

### 1. Commented Setup Code Example

```yaml
  # Per-rank RTP cooldown overrides resolved from permissions
  RANK-COOLDOWNS:
    # Enable or disable permission based RTP cooldown overrides
    ENABLED: true
    # Explicit mapping from permission node to RTP cooldown in seconds
    # Players can also be given ultimatedonutsmp.rtp.cooldown.<seconds> directly, for example
    # ultimatedonutsmp.rtp.cooldown.3 for a 3 second cooldown
    # The lowest value the player has wins, and 0 removes the cooldown entirely
    # Players without any of these permissions keep the per-world COOLDOWN below
    PERMISSIONS:
      "ultimatedonutsmp.rtp.cooldown.vip++": 3
      "ultimatedonutsmp.rtp.cooldown.vip+": 10
      "ultimatedonutsmp.rtp.cooldown.vip": 15
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `SETTINGS.RANK-COOLDOWNS.ENABLED` | `bool` | `true`, `false` | `true` | Master toggle. When `false` every player uses the per-world `WORLD-SETTINGS.<world>.COOLDOWN` value and all cooldown permissions are ignored. |
| `SETTINGS.RANK-COOLDOWNS.PERMISSIONS` | `section` | Permission node to seconds | See example | Maps an arbitrary permission node to an RTP cooldown in seconds. Use this to reuse rank nodes you already have instead of adding numeric nodes. |

### 3. Resolution Order

1. Every entry under `PERMISSIONS` the player holds is collected.
2. Every `ultimatedonutsmp.rtp.cooldown.<seconds>` node the player holds is collected. A non-numeric or
   negative suffix is ignored.
3. The **lowest** collected value becomes the player cooldown, so stacked ranks always give the player
   the fastest cooldown they are entitled to.
4. If the player holds none of these nodes, the per-world `WORLD-SETTINGS.<world>.COOLDOWN` value applies.

A permission value fully replaces the per-world value, in both directions. `ultimatedonutsmp.rtp.cooldown.60`
gives a 60 second cooldown even where the world is configured at 30, and `ultimatedonutsmp.rtp.cooldown.0`
removes the cooldown entirely.

The cooldown is evaluated on every `/rtp` attempt rather than frozen when the previous teleport finished,
so a rank change applies immediately, including to a cooldown already counting down.

### 4. Practical Setup Example

Give the default rank a 60 second cooldown, VIP 15 seconds and staff no cooldown at all:

```yaml
SETTINGS:
  RANK-COOLDOWNS:
    ENABLED: true
    PERMISSIONS:
      "group.vip": 15
      "group.staff": 0

WORLD-SETTINGS:
  world:
    COOLDOWN: 60
```

Or skip the config entirely and drive it from LuckPerms alone:

```
/lp group default permission set ultimatedonutsmp.rtp.cooldown.60
/lp group vip permission set ultimatedonutsmp.rtp.cooldown.15
/lp group staff permission set ultimatedonutsmp.rtp.cooldown.0
```

The `{cooldown}` placeholder in the RTP menu shows the viewing player their own resolved cooldown.

---

## Section: `MESSAGES`

### 1. Commented Setup Code Example

```yaml
MESSAGES:
  DISABLED: '&cRTP is currently disabled.'
  COOLDOWN: '&cYou can''t rtp for another {remaining}s.'
  MAX-PLAYERS: '&cToo many players are using RTP right now. Please try again later.'
  WORLD-NOT-EXIST: '&cThe world does not exist.'
  SEARCHING: '&aSearching for a safe location in {world}...'
  TP-WARNING: '&eDo not move for &b{countdown}&e seconds or the teleport will be canceled.'
  SEARCH-ACTIONBAR: '&7Searching {world}... &b{elapsed}s &8(&f{attempts}/{max_attempts}&8)'
  SEARCH-FOUND-ACTIONBAR: '&aSafe location found in {world}! &7Preparing teleport...'
  SAFE-LOCATION-FOUND: '&aSafe location found at: X:{x} Y:{y} Z:{z}'
  MAX-ATTEMPTS: '&cCould not find a safe location after %attempts% attempts.'
  DESTINATION-DISABLED: '&cThis RTP destination is currently disabled.'
  PLAYTIME-REQUIRED: '&cYou need at least {required} hours of playtime to RTP to {world}. &7(Current: {current}h)'
  UNSAFE-LOCATION: '&cThe location at X:{x} Y:{y} Z:{z} was rejected: {reason}'
  SAFE-LOCATION-FOUND-HIDDEN: '&aSafe location found! Teleporting you blindly...'
  SEARCH-FOUND-ACTIONBAR-HIDDEN: '&aFound safe location'

# List of world names where RTP execution is denied
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `MESSAGES.DISABLED` | `str` | Any string text | `'&cRTP is currently disabled.'` | Configures the technical `DISABLED` parameter for `MESSAGES.DISABLED` in `rtp.yml`. |
| `MESSAGES.COOLDOWN` | `str` | Any string text | `'&cYou can't rtp for another {remain...'` | Configures the technical `COOLDOWN` parameter for `MESSAGES.COOLDOWN` in `rtp.yml`. |
| `MESSAGES.MAX-PLAYERS` | `str` | Any string text | `'&cToo many players are using RTP ri...'` | Configures the technical `MAX-PLAYERS` parameter for `MESSAGES.MAX-PLAYERS` in `rtp.yml`. |
| `MESSAGES.WORLD-NOT-EXIST` | `str` | Any string text | `'&cThe world does not exist.'` | Configures the technical `WORLD-NOT-EXIST` parameter for `MESSAGES.WORLD-NOT-EXIST` in `rtp.yml`. |
| `MESSAGES.SEARCHING` | `str` | Any string text | `'&aSearching for a safe location in ...'` | Configures the technical `SEARCHING` parameter for `MESSAGES.SEARCHING` in `rtp.yml`. |
| `MESSAGES.TP-WARNING` | `str` | Any string text | `'&eDo not move for &b{countdown}&e s...'` | Configures the technical `TP-WARNING` parameter for `MESSAGES.TP-WARNING` in `rtp.yml`. |
| `MESSAGES.SEARCH-ACTIONBAR` | `str` | Any string text | `'&7Searching {world}... &b{elapsed}s...'` | Configures the technical `SEARCH-ACTIONBAR` parameter for `MESSAGES.SEARCH-ACTIONBAR` in `rtp.yml`. |
| `MESSAGES.SEARCH-FOUND-ACTIONBAR` | `str` | Any string text | `'&aSafe location found in {world}! &...'` | Configures the technical `SEARCH-FOUND-ACTIONBAR` parameter for `MESSAGES.SEARCH-FOUND-ACTIONBAR` in `rtp.yml`. |
| `MESSAGES.SAFE-LOCATION-FOUND` | `str` | Any string text | `'&aSafe location found at: X:{x} Y:{...'` | Configures the technical `SAFE-LOCATION-FOUND` parameter for `MESSAGES.SAFE-LOCATION-FOUND` in `rtp.yml`. |
| `MESSAGES.MAX-ATTEMPTS` | `str` | Any string text | `'&cCould not find a safe location af...'` | Configures the technical `MAX-ATTEMPTS` parameter for `MESSAGES.MAX-ATTEMPTS` in `rtp.yml`. |
| `MESSAGES.DESTINATION-DISABLED` | `str` | Any string text | `'&cThis RTP destination is currently...'` | Configures the technical `DESTINATION-DISABLED` parameter for `MESSAGES.DESTINATION-DISABLED` in `rtp.yml`. |
| `MESSAGES.PLAYTIME-REQUIRED` | `str` | Any string text | `'&cYou need at least {required} hour...'` | Configures the technical `PLAYTIME-REQUIRED` parameter for `MESSAGES.PLAYTIME-REQUIRED` in `rtp.yml`. |
| `MESSAGES.UNSAFE-LOCATION` | `str` | Any string text | `'&cThe location at X:{x} Y:{y} Z:{z}...'` | Configures the technical `UNSAFE-LOCATION` parameter for `MESSAGES.UNSAFE-LOCATION` in `rtp.yml`. |
| `MESSAGES.SAFE-LOCATION-FOUND-HIDDEN` | `str` | Any string text | `'&aSafe location found! Teleporting ...'` | Configures the technical `SAFE-LOCATION-FOUND-HIDDEN` parameter for `MESSAGES.SAFE-LOCATION-FOUND-HIDDEN` in `rtp.yml`. |
| `MESSAGES.SEARCH-FOUND-ACTIONBAR-HIDDEN` | `str` | Any string text | `'&aFound safe location'` | Configures the technical `SEARCH-FOUND-ACTIONBAR-HIDDEN` parameter for `MESSAGES.SEARCH-FOUND-ACTIONBAR-HIDDEN` in `rtp.yml`. |

### 3. Practical Setup Example

```yaml
MESSAGES:
  DISABLED: '&cRTP is currently disabled.'
  COOLDOWN: '&cYou can''t rtp for another {remaining}s.'
  MAX-PLAYERS: '&cToo many players are using RTP right now. Please try again later.'
  WORLD-NOT-EXIST: '&cThe world does not exist.'
  SEARCHING: '&aSearching for a safe location in {world}...'
  TP-WARNING: '&eDo not move for &b{countdown}&e seconds or the teleport will be canceled.'
  SEARCH-ACTIONBAR: '&7Searching {world}... &b{elapsed}s &8(&f{attempts}/{max_attempts}&8)'
  SEARCH-FOUND-ACTIONBAR: '&aSafe location found in {world}! &7Preparing teleport...'
  SAFE-LOCATION-FOUND: '&aSafe location found at: X:{x} Y:{y} Z:{z}'
  MAX-ATTEMPTS: '&cCould not find a safe location after %attempts% attempts.'
  DESTINATION-DISABLED: '&cThis RTP destination is currently disabled.'
  PLAYTIME-REQUIRED: '&cYou need at least {required} hours of playtime to RTP to {world}. &7(Current: {current}h)'
  UNSAFE-LOCATION: '&cThe location at X:{x} Y:{y} Z:{z} was rejected: {reason}'
  SAFE-LOCA
```

---

## Section: `DENIED-WORLDS`

### 1. Commented Setup Code Example

```yaml
DENIED-WORLDS:
  - afk

# Per-world RTP boundary and cooldown settings
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `DENIED-WORLDS` | `list` | List of configured items/strings | `['afk']` | Configures the technical `DENIED-WORLDS` parameter for `DENIED-WORLDS` in `rtp.yml`. |

### 3. Practical Setup Example

```yaml
DENIED-WORLDS:
  - afk

# Per-world RTP boundary and cooldown settings
```

---

## Section: `WORLD-SETTINGS`

### 1. Commented Setup Code Example

```yaml
WORLD-SETTINGS:
  # Overworld configuration
  world:
    MAX-RADIUS: 5000
    MIN-RADIUS: 500
    CENTER-X: 0
    CENTER-Z: 0
    COOLDOWN: 30

  # Nether configuration
  world_nether:
    MAX-RADIUS: 500
    MIN-RADIUS: 50
    CENTER-X: 0
    CENTER-Z: 0
    COOLDOWN: 30
    # Required playtime in hours to use RTP in the Nether (0.0 = no requirement)
    REQUIRED-PLAYTIME-HOURS: 5.0

  # The End configuration
  world_the_end:
    MAX-RADIUS: 2000
    MIN-RADIUS: 150
    CENTER-X: 0
    CENTER-Z: 0
    COOLDOWN: 30
    # Required playtime in hours to use RTP in The End (0.0 = no requirement)
    REQUIRED-PLAYTIME-HOURS: 10.0

# RTP GUI Menu configuration
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `WORLD-SETTINGS.world.MAX-RADIUS` | `int` | Any valid integer number | `'5000'` | Configures the technical `MAX-RADIUS` parameter for `WORLD-SETTINGS.world.MAX-RADIUS` in `rtp.yml`. |
| `WORLD-SETTINGS.world.MIN-RADIUS` | `int` | Any valid integer number | `'500'` | Configures the technical `MIN-RADIUS` parameter for `WORLD-SETTINGS.world.MIN-RADIUS` in `rtp.yml`. |
| `WORLD-SETTINGS.world.CENTER-X` | `int` | Any valid integer number | `'0'` | Configures the technical `CENTER-X` parameter for `WORLD-SETTINGS.world.CENTER-X` in `rtp.yml`. |
| `WORLD-SETTINGS.world.CENTER-Z` | `int` | Any valid integer number | `'0'` | Configures the technical `CENTER-Z` parameter for `WORLD-SETTINGS.world.CENTER-Z` in `rtp.yml`. |
| `WORLD-SETTINGS.world.COOLDOWN` | `int` | Any valid integer number | `'30'` | Configures the technical `COOLDOWN` parameter for `WORLD-SETTINGS.world.COOLDOWN` in `rtp.yml`. |
| `WORLD-SETTINGS.world_nether.MAX-RADIUS` | `int` | Any valid integer number | `'500'` | Configures the technical `MAX-RADIUS` parameter for `WORLD-SETTINGS.world_nether.MAX-RADIUS` in `rtp.yml`. |
| `WORLD-SETTINGS.world_nether.MIN-RADIUS` | `int` | Any valid integer number | `'50'` | Configures the technical `MIN-RADIUS` parameter for `WORLD-SETTINGS.world_nether.MIN-RADIUS` in `rtp.yml`. |
| `WORLD-SETTINGS.world_nether.CENTER-X` | `int` | Any valid integer number | `'0'` | Configures the technical `CENTER-X` parameter for `WORLD-SETTINGS.world_nether.CENTER-X` in `rtp.yml`. |
| `WORLD-SETTINGS.world_nether.CENTER-Z` | `int` | Any valid integer number | `'0'` | Configures the technical `CENTER-Z` parameter for `WORLD-SETTINGS.world_nether.CENTER-Z` in `rtp.yml`. |
| `WORLD-SETTINGS.world_nether.COOLDOWN` | `int` | Any valid integer number | `'30'` | Configures the technical `COOLDOWN` parameter for `WORLD-SETTINGS.world_nether.COOLDOWN` in `rtp.yml`. |
| `WORLD-SETTINGS.world_nether.REQUIRED-PLAYTIME-HOURS` | `float` | Any decimal number | `'5.0'` | Configures the technical `REQUIRED-PLAYTIME-HOURS` parameter for `WORLD-SETTINGS.world_nether.REQUIRED-PLAYTIME-HOURS` in `rtp.yml`. |
| `WORLD-SETTINGS.world_the_end.MAX-RADIUS` | `int` | Any valid integer number | `'2000'` | Configures the technical `MAX-RADIUS` parameter for `WORLD-SETTINGS.world_the_end.MAX-RADIUS` in `rtp.yml`. |
| `WORLD-SETTINGS.world_the_end.MIN-RADIUS` | `int` | Any valid integer number | `'150'` | Configures the technical `MIN-RADIUS` parameter for `WORLD-SETTINGS.world_the_end.MIN-RADIUS` in `rtp.yml`. |
| `WORLD-SETTINGS.world_the_end.CENTER-X` | `int` | Any valid integer number | `'0'` | Configures the technical `CENTER-X` parameter for `WORLD-SETTINGS.world_the_end.CENTER-X` in `rtp.yml`. |
| `WORLD-SETTINGS.world_the_end.CENTER-Z` | `int` | Any valid integer number | `'0'` | Configures the technical `CENTER-Z` parameter for `WORLD-SETTINGS.world_the_end.CENTER-Z` in `rtp.yml`. |
| `WORLD-SETTINGS.world_the_end.COOLDOWN` | `int` | Any valid integer number | `'30'` | Configures the technical `COOLDOWN` parameter for `WORLD-SETTINGS.world_the_end.COOLDOWN` in `rtp.yml`. |
| `WORLD-SETTINGS.world_the_end.REQUIRED-PLAYTIME-HOURS` | `float` | Any decimal number | `'10.0'` | Configures the technical `REQUIRED-PLAYTIME-HOURS` parameter for `WORLD-SETTINGS.world_the_end.REQUIRED-PLAYTIME-HOURS` in `rtp.yml`. |

### 3. Practical Setup Example

```yaml
WORLD-SETTINGS:
  # Overworld configuration
  world:
    MAX-RADIUS: 5000
    MIN-RADIUS: 500
    CENTER-X: 0
    CENTER-Z: 0
    COOLDOWN: 30

  # Nether configuration
  world_nether:
    MAX-RADIUS: 500
    MIN-RADIUS: 50
    CENTER-X: 0
    CENTER-Z: 0
    COOLDOWN: 30
    # Required playtime in hours to use RTP in the Nether (0.0 = no requirement)
    REQUIRED-PLAYTIME-HOURS: 5.0

  # The End configuration
  world_the_end:
    MAX-RADIUS: 2000
    MIN-RADIUS: 150
    CENTER-X: 0
    CENTER-Z: 0
    COOLDOWN: 30
    # Required playtime in hours to use RTP in The End (0.0 = no requirement)
    REQUIRED-PLAYTIME-HOURS: 10.0

# RTP GUI Menu configuration
```

---

## Section: `RTP-MENU`

### 1. Commented Setup Code Example

```yaml
RTP-MENU:
  TITLE: '&8RTP Menu'
  SIZE: 27
  # Enable background glass filler item in GUI (true / false)
  PLACEHOLDER: true
  BUTTONS:
    OVERWORLD:
      DISPLAY-NAME: '&#4B72FFOverworld'
      MATERIAL: GRASS_BLOCK
      SLOT: 11
      WORLD: world
      ENABLED: true
    NETHER:
      DISPLAY-NAME: '&#FF4B4BNether'
      MATERIAL: NETHERRACK
      SLOT: 13
      WORLD: world_nether
      ENABLED: true
    THE_END:
      DISPLAY-NAME: '&#A84BFFThe End'
      MATERIAL: END_STONE
      SLOT: 15
      WORLD: world_the_end
      ENABLED: true
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `RTP-MENU.TITLE` | `str` | Any string text | `'&8RTP Menu'` | Configures the technical `TITLE` parameter for `RTP-MENU.TITLE` in `rtp.yml`. |
| `RTP-MENU.SIZE` | `int` | Any valid integer number | `'27'` | Configures the technical `SIZE` parameter for `RTP-MENU.SIZE` in `rtp.yml`. |
| `RTP-MENU.PLACEHOLDER` | `bool` | `true`, `false` | `true` | Configures the technical `PLACEHOLDER` parameter for `RTP-MENU.PLACEHOLDER` in `rtp.yml`. |
| `RTP-MENU.BUTTONS.OVERWORLD.DISPLAY-NAME` | `str` | Any string text | `'&#4B72FFOverworld'` | Configures the technical `DISPLAY-NAME` parameter for `RTP-MENU.BUTTONS.OVERWORLD.DISPLAY-NAME` in `rtp.yml`. |
| `RTP-MENU.BUTTONS.OVERWORLD.MATERIAL` | `str` | Any string text | `'GRASS_BLOCK'` | Configures the technical `MATERIAL` parameter for `RTP-MENU.BUTTONS.OVERWORLD.MATERIAL` in `rtp.yml`. |
| `RTP-MENU.BUTTONS.OVERWORLD.SLOT` | `int` | Any valid integer number | `'11'` | Configures the technical `SLOT` parameter for `RTP-MENU.BUTTONS.OVERWORLD.SLOT` in `rtp.yml`. |
| `RTP-MENU.BUTTONS.OVERWORLD.WORLD` | `str` | Any string text | `'world'` | Configures the technical `WORLD` parameter for `RTP-MENU.BUTTONS.OVERWORLD.WORLD` in `rtp.yml`. |
| `RTP-MENU.BUTTONS.OVERWORLD.ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `RTP-MENU` system. Set to `true` to enable, `false` to disable. |
| `RTP-MENU.BUTTONS.NETHER.DISPLAY-NAME` | `str` | Any string text | `'&#FF4B4BNether'` | Configures the technical `DISPLAY-NAME` parameter for `RTP-MENU.BUTTONS.NETHER.DISPLAY-NAME` in `rtp.yml`. |
| `RTP-MENU.BUTTONS.NETHER.MATERIAL` | `str` | Any string text | `'NETHERRACK'` | Configures the technical `MATERIAL` parameter for `RTP-MENU.BUTTONS.NETHER.MATERIAL` in `rtp.yml`. |
| `RTP-MENU.BUTTONS.NETHER.SLOT` | `int` | Any valid integer number | `'13'` | Configures the technical `SLOT` parameter for `RTP-MENU.BUTTONS.NETHER.SLOT` in `rtp.yml`. |
| `RTP-MENU.BUTTONS.NETHER.WORLD` | `str` | Any string text | `'world_nether'` | Configures the technical `WORLD` parameter for `RTP-MENU.BUTTONS.NETHER.WORLD` in `rtp.yml`. |
| `RTP-MENU.BUTTONS.NETHER.ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `RTP-MENU` system. Set to `true` to enable, `false` to disable. |
| `RTP-MENU.BUTTONS.THE_END.DISPLAY-NAME` | `str` | Any string text | `'&#A84BFFThe End'` | Configures the technical `DISPLAY-NAME` parameter for `RTP-MENU.BUTTONS.THE_END.DISPLAY-NAME` in `rtp.yml`. |
| `RTP-MENU.BUTTONS.THE_END.MATERIAL` | `str` | Any string text | `'END_STONE'` | Configures the technical `MATERIAL` parameter for `RTP-MENU.BUTTONS.THE_END.MATERIAL` in `rtp.yml`. |
| `RTP-MENU.BUTTONS.THE_END.SLOT` | `int` | Any valid integer number | `'15'` | Configures the technical `SLOT` parameter for `RTP-MENU.BUTTONS.THE_END.SLOT` in `rtp.yml`. |
| `RTP-MENU.BUTTONS.THE_END.WORLD` | `str` | Any string text | `'world_the_end'` | Configures the technical `WORLD` parameter for `RTP-MENU.BUTTONS.THE_END.WORLD` in `rtp.yml`. |
| `RTP-MENU.BUTTONS.THE_END.ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `RTP-MENU` system. Set to `true` to enable, `false` to disable. |

### 3. Practical Setup Example

```yaml
RTP-MENU:
  TITLE: '&8RTP Menu'
  SIZE: 27
  # Enable background glass filler item in GUI (true / false)
  PLACEHOLDER: true
  BUTTONS:
    OVERWORLD:
      DISPLAY-NAME: '&#4B72FFOverworld'
      MATERIAL: GRASS_BLOCK
      SLOT: 11
      WORLD: world
      ENABLED: true
    NETHER:
      DISPLAY-NAME: '&#FF4B4BNether'
      MATERIAL: NETHERRACK
      SLOT: 13
      WORLD: world_nether
      ENABLED: true
    THE_END:
      DISPLAY-NAME: '&#A84BFFThe End'
      MATERIAL: END_STONE
      SLOT: 15
      WORLD: world_the_end
      ENABLED: true
```

---

