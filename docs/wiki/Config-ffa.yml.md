# Detailed Configuration & Setup Guide: `ffa.yml`

This is the official, 100% complete technical setup guide for `ffa.yml` in **UltimateDonutSMP**.
Each section details the exact commented setup code block, allowed option values, data types, default values, and in-depth functional behavior.

---

## Section: `SETTINGS`

### 1. Commented Setup Code Example

```yaml
SETTINGS:
  # Enable or disable the FFA system globally (true / false)
  ENABLED: true
  # Delay before teleporting players back after leaving/dying in FFA (in seconds)
  RETURN_DELAY_SECONDS: 3

# Arena Rollback and restoration settings
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `SETTINGS.ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `SETTINGS` system. Set to `true` to enable, `false` to disable. |
| `SETTINGS.RETURN_DELAY_SECONDS` | `int` | Any valid integer number | `'3'` | Configures the technical `RETURN_DELAY_SECONDS` parameter for `SETTINGS.RETURN_DELAY_SECONDS` in `ffa.yml`. |

### 3. Practical Setup Example

```yaml
SETTINGS:
  # Enable or disable the FFA system globally (true / false)
  ENABLED: true
  # Delay before teleporting players back after leaving/dying in FFA (in seconds)
  RETURN_DELAY_SECONDS: 3

# Arena Rollback and restoration settings
```

---

## Section: `ROLLBACK`

### 1. Commented Setup Code Example

```yaml
ROLLBACK:
  # Enable arena block rollback and cleanup after FFA activity (true / false)
  ENABLED: true
  # Extra horizontal padding blocks preserved around FFA arena (in blocks)
  PADDING_HORIZONTAL: 48
  # Extra vertical padding blocks preserved around FFA arena (in blocks)
  PADDING_VERTICAL: 20
  # Delay in seconds before starting block rollback
  TIMEOUT_SECONDS: 10
  # Automatically remove projectiles (arrows, snowballs) in FFA arena (true / false)
  CLEANUP_PROJECTILES: true
  # Automatically remove dropped item entities in FFA arena (true / false)
  CLEANUP_DROPS: true
  # Automatically extinguish fires and drain fluid blocks placed during FFA (true / false)
  CLEANUP_FIRE_AND_FLUIDS: true

# Player state management upon entering and exiting FFA
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `ROLLBACK.ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `ROLLBACK` system. Set to `true` to enable, `false` to disable. |
| `ROLLBACK.PADDING_HORIZONTAL` | `int` | Any valid integer number | `'48'` | Configures the technical `PADDING_HORIZONTAL` parameter for `ROLLBACK.PADDING_HORIZONTAL` in `ffa.yml`. |
| `ROLLBACK.PADDING_VERTICAL` | `int` | Any valid integer number | `'20'` | Configures the technical `PADDING_VERTICAL` parameter for `ROLLBACK.PADDING_VERTICAL` in `ffa.yml`. |
| `ROLLBACK.TIMEOUT_SECONDS` | `int` | Any valid integer number | `'10'` | Configures the technical `TIMEOUT_SECONDS` parameter for `ROLLBACK.TIMEOUT_SECONDS` in `ffa.yml`. |
| `ROLLBACK.CLEANUP_PROJECTILES` | `bool` | `true`, `false` | `true` | Configures the technical `CLEANUP_PROJECTILES` parameter for `ROLLBACK.CLEANUP_PROJECTILES` in `ffa.yml`. |
| `ROLLBACK.CLEANUP_DROPS` | `bool` | `true`, `false` | `true` | Configures the technical `CLEANUP_DROPS` parameter for `ROLLBACK.CLEANUP_DROPS` in `ffa.yml`. |
| `ROLLBACK.CLEANUP_FIRE_AND_FLUIDS` | `bool` | `true`, `false` | `true` | Configures the technical `CLEANUP_FIRE_AND_FLUIDS` parameter for `ROLLBACK.CLEANUP_FIRE_AND_FLUIDS` in `ffa.yml`. |

### 3. Practical Setup Example

```yaml
ROLLBACK:
  # Enable arena block rollback and cleanup after FFA activity (true / false)
  ENABLED: true
  # Extra horizontal padding blocks preserved around FFA arena (in blocks)
  PADDING_HORIZONTAL: 48
  # Extra vertical padding blocks preserved around FFA arena (in blocks)
  PADDING_VERTICAL: 20
  # Delay in seconds before starting block rollback
  TIMEOUT_SECONDS: 10
  # Automatically remove projectiles (arrows, snowballs) in FFA arena (true / false)
  CLEANUP_PROJECTILES: true
  # Automatically remove dropped item entities in FFA arena (true / false)
  CLEANUP_DROPS: true
  # Automatically extinguish fires and drain fluid blocks placed during FFA (true / false)
  CLEANUP_FIRE_AND_FLUIDS: true

# Player state management upon entering and exiting FFA
```

---

## Section: `PLAYER_STATE`

### 1. Commented Setup Code Example

```yaml
PLAYER_STATE:
  # Restore player inventory upon exiting FFA match (true / false)
  RESTORE_INVENTORY: true
  # Restore health to max upon exiting FFA match (true / false)
  RESTORE_HEALTH: true
  # Clear active potion effects upon entering/exiting FFA (true / false)
  RESTORE_EFFECTS: true

# Match rules and restrictions
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `PLAYER_STATE.RESTORE_INVENTORY` | `bool` | `true`, `false` | `true` | Configures the technical `RESTORE_INVENTORY` parameter for `PLAYER_STATE.RESTORE_INVENTORY` in `ffa.yml`. |
| `PLAYER_STATE.RESTORE_HEALTH` | `bool` | `true`, `false` | `true` | Configures the technical `RESTORE_HEALTH` parameter for `PLAYER_STATE.RESTORE_HEALTH` in `ffa.yml`. |
| `PLAYER_STATE.RESTORE_EFFECTS` | `bool` | `true`, `false` | `true` | Configures the technical `RESTORE_EFFECTS` parameter for `PLAYER_STATE.RESTORE_EFFECTS` in `ffa.yml`. |

### 3. Practical Setup Example

```yaml
PLAYER_STATE:
  # Restore player inventory upon exiting FFA match (true / false)
  RESTORE_INVENTORY: true
  # Restore health to max upon exiting FFA match (true / false)
  RESTORE_HEALTH: true
  # Clear active potion effects upon entering/exiting FFA (true / false)
  RESTORE_EFFECTS: true

# Match rules and restrictions
```

---

## Section: `RULES`

### 1. Commented Setup Code Example

```yaml
RULES:
  # Block general player commands while inside FFA arena (true / false)
  BLOCK_COMMANDS: true
  # Whether FFA kills/deaths count toward global player statistics (true / false)
  COUNT_TOWARD_GLOBAL_STATS: false
  # Whether survival rewards (money/shards) are granted for kills in FFA (true / false)
  GIVE_SURVIVAL_REWARDS: false

# End match screen titles and subtitles
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `RULES.BLOCK_COMMANDS` | `bool` | `true`, `false` | `true` | Configures the technical `BLOCK_COMMANDS` parameter for `RULES.BLOCK_COMMANDS` in `ffa.yml`. |
| `RULES.COUNT_TOWARD_GLOBAL_STATS` | `bool` | `true`, `false` | `false` | Configures the technical `COUNT_TOWARD_GLOBAL_STATS` parameter for `RULES.COUNT_TOWARD_GLOBAL_STATS` in `ffa.yml`. |
| `RULES.GIVE_SURVIVAL_REWARDS` | `bool` | `true`, `false` | `false` | Configures the technical `GIVE_SURVIVAL_REWARDS` parameter for `RULES.GIVE_SURVIVAL_REWARDS` in `ffa.yml`. |

### 3. Practical Setup Example

```yaml
RULES:
  # Block general player commands while inside FFA arena (true / false)
  BLOCK_COMMANDS: true
  # Whether FFA kills/deaths count toward global player statistics (true / false)
  COUNT_TOWARD_GLOBAL_STATS: false
  # Whether survival rewards (money/shards) are granted for kills in FFA (true / false)
  GIVE_SURVIVAL_REWARDS: false

# End match screen titles and subtitles
```

---

## Section: `RESULT-TITLES`

### 1. Commented Setup Code Example

```yaml
RESULT-TITLES:
  victory:
    title: '&e&lVICTORY!'
    subtitle: '&e<player> &fwon the FFA Match!'
  defeat:
    title: '&c&lDEFEAT!'
    subtitle: '&c<opponent> &feliminated you!'

# Configuration for static FFA arena definitions (managed via /ffa commands)
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `RESULT-TITLES.victory.title` | `str` | Any string text | `'&e&lVICTORY!'` | Configures the technical `title` parameter for `RESULT-TITLES.victory.title` in `ffa.yml`. |
| `RESULT-TITLES.victory.subtitle` | `str` | Any string text | `'&e<player> &fwon the FFA Match!'` | Configures the technical `subtitle` parameter for `RESULT-TITLES.victory.subtitle` in `ffa.yml`. |
| `RESULT-TITLES.defeat.title` | `str` | Any string text | `'&c&lDEFEAT!'` | Configures the technical `title` parameter for `RESULT-TITLES.defeat.title` in `ffa.yml`. |
| `RESULT-TITLES.defeat.subtitle` | `str` | Any string text | `'&c<opponent> &feliminated you!'` | Configures the technical `subtitle` parameter for `RESULT-TITLES.defeat.subtitle` in `ffa.yml`. |

### 3. Practical Setup Example

```yaml
RESULT-TITLES:
  victory:
    title: '&e&lVICTORY!'
    subtitle: '&e<player> &fwon the FFA Match!'
  defeat:
    title: '&c&lDEFEAT!'
    subtitle: '&c<opponent> &feliminated you!'

# Configuration for static FFA arena definitions (managed via /ffa commands)
```

---

## Section: `ARENA_SETTINGS`

### 1. Commented Setup Code Example

```yaml
ARENA_SETTINGS: {}
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |

### 3. Practical Setup Example

```yaml
ARENA_SETTINGS: {}
```

---

