# Detailed Configuration & Setup Guide: `spawn-stash.yml`

This is the official, 100% complete technical setup guide for `spawn-stash.yml` in **UltimateDonutSMP**.
Each section details the exact commented setup code block, allowed option values, data types, default values, and in-depth functional behavior.

---

## Section: `SETTINGS`

### 1. Commented Setup Code Example

```yaml
SETTINGS:
  # Determines whether Enabled is enabled or disabled. Available options: true, false
  ENABLED: true
  # The numerical value for Default Ttl Seconds. Available options: Any valid integer
  DEFAULT_TTL_SECONDS: 900
  # The decimal value for Default Alert Radius. Available options: Any decimal number
  DEFAULT_ALERT_RADIUS: 8.0
  # The numerical value for Alert Cooldown Seconds. Available options: Any valid integer
  ALERT_COOLDOWN_SECONDS: 30
  # The numerical value for Check Interval Ticks. Available options: Any valid integer
  CHECK_INTERVAL_TICKS: 20
  # Determines whether Overwrite Blocks is enabled or disabled. Available options: true, false
  OVERWRITE_BLOCKS: true
  # Determines whether Protect Blocks is enabled or disabled. Available options: true, false
  PROTECT_BLOCKS: false
  # Determines whether Claim Spawners On Break is enabled or disabled. Available options: true, false
  CLAIM_SPAWNERS_ON_BREAK: false
  # Determines whether Rollback On Reload is enabled or disabled. Available options: true, false
  ROLLBACK_ON_RELOAD: true
  # Determines whether Log To Console is enabled or disabled. Available options: true, false
  LOG_TO_CONSOLE: true
  # The numerical value for Max Blocks Per Stash. Available options: Any valid integer
  MAX_BLOCKS_PER_STASH: 256
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `SETTINGS.ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `SETTINGS` system. Set to `true` to enable, `false` to disable. |
| `SETTINGS.DEFAULT_TTL_SECONDS` | `int` | Any valid integer number | `'900'` | Configures the technical `DEFAULT_TTL_SECONDS` parameter for `SETTINGS.DEFAULT_TTL_SECONDS` in `spawn-stash.yml`. |
| `SETTINGS.DEFAULT_ALERT_RADIUS` | `float` | Any decimal number | `'8.0'` | Configures the technical `DEFAULT_ALERT_RADIUS` parameter for `SETTINGS.DEFAULT_ALERT_RADIUS` in `spawn-stash.yml`. |
| `SETTINGS.ALERT_COOLDOWN_SECONDS` | `int` | Any valid integer number | `'30'` | Configures the technical `ALERT_COOLDOWN_SECONDS` parameter for `SETTINGS.ALERT_COOLDOWN_SECONDS` in `spawn-stash.yml`. |
| `SETTINGS.CHECK_INTERVAL_TICKS` | `int` | Any valid integer number | `'20'` | Configures the technical `CHECK_INTERVAL_TICKS` parameter for `SETTINGS.CHECK_INTERVAL_TICKS` in `spawn-stash.yml`. |
| `SETTINGS.OVERWRITE_BLOCKS` | `bool` | `true`, `false` | `true` | Configures the technical `OVERWRITE_BLOCKS` parameter for `SETTINGS.OVERWRITE_BLOCKS` in `spawn-stash.yml`. |
| `SETTINGS.PROTECT_BLOCKS` | `bool` | `true`, `false` | `false` | Configures the technical `PROTECT_BLOCKS` parameter for `SETTINGS.PROTECT_BLOCKS` in `spawn-stash.yml`. |
| `SETTINGS.CLAIM_SPAWNERS_ON_BREAK` | `bool` | `true`, `false` | `false` | Configures the technical `CLAIM_SPAWNERS_ON_BREAK` parameter for `SETTINGS.CLAIM_SPAWNERS_ON_BREAK` in `spawn-stash.yml`. |
| `SETTINGS.ROLLBACK_ON_RELOAD` | `bool` | `true`, `false` | `true` | Configures the technical `ROLLBACK_ON_RELOAD` parameter for `SETTINGS.ROLLBACK_ON_RELOAD` in `spawn-stash.yml`. |
| `SETTINGS.LOG_TO_CONSOLE` | `bool` | `true`, `false` | `true` | Configures the technical `LOG_TO_CONSOLE` parameter for `SETTINGS.LOG_TO_CONSOLE` in `spawn-stash.yml`. |
| `SETTINGS.MAX_BLOCKS_PER_STASH` | `int` | Any valid integer number | `'256'` | Configures the technical `MAX_BLOCKS_PER_STASH` parameter for `SETTINGS.MAX_BLOCKS_PER_STASH` in `spawn-stash.yml`. |

### 3. Practical Setup Example

```yaml
SETTINGS:
  # Determines whether Enabled is enabled or disabled. Available options: true, false
  ENABLED: true
  # The numerical value for Default Ttl Seconds. Available options: Any valid integer
  DEFAULT_TTL_SECONDS: 900
  # The decimal value for Default Alert Radius. Available options: Any decimal number
  DEFAULT_ALERT_RADIUS: 8.0
  # The numerical value for Alert Cooldown Seconds. Available options: Any valid integer
  ALERT_COOLDOWN_SECONDS: 30
  # The numerical value for Check Interval Ticks. Available options: Any valid integer
  CHECK_INTERVAL_TICKS: 20
  # Determines whether Overwrite Blocks is enabled or disabled. Available options: true, false
  OVERWRITE_BLOCKS: true
  # Determines whether Protect Blocks is enabled or disabled. Available options: true, false
  PROTECT_BLOCKS: false
  # Determines whether Claim Spawners On Break is enabled or disabled. Available options: true, false
  CLAIM_SPAWNERS_ON_BREAK: false
  # Determines whether Rollback On Reload is enabled or disabled. Available options: true, false
  ROLLBACK_ON_RELOAD: true
  # Determines whether Log To Console is enabled or disabled. Available options: true, false
  LOG_TO_CONSOLE: true
  # The numerical value for Max Blocks Per Stash. Available options: Any valid integer
  MAX_BLOCKS_PER_STASH: 256
```

---

## Section: `MESSAGES`

### 1. Commented Setup Code Example

```yaml
MESSAGES:
  # Configuration section for Usage.
  USAGE:
  - '&8&m----------- &dspawnstash &8&m-----------'
  - '&f/{label} &7- spawn random bait stash'
  - '&f/{label} <type> &7- spawn a bait stash'
  - '&f/{label} spawn <type> &7- spawn a bait stash'
  - '&f/{label} list &7- list active/configured stashes'
  - '&f/{label} remove <id|nearest|all> &7- rollback stashes'
  - '&f/{label} reload &7- reload spawn-stash.yml'
  # The text or value for Spawned. Available options: Any valid string text
  SPAWNED: '&aspawned stash #{type} successfully. &7(id: {id})'
  # The text or value for Removed. Available options: Any valid string text
  REMOVED: '&aremoved stash &f#{id}&a.'
  # The text or value for Removed All. Available options: Any valid string text
  REMOVED-ALL: '&aremoved &f{count}&a active stash(es).'
  # The text or value for Expired. Available options: Any valid string text
  EXPIRED: '&7spawnstash #{id} expired and was rolled back.'
  # The text or value for Reloaded. Available options: Any valid string text
  RELOADED: '&aspawnstash settings reloaded.'
  # The text or value for Disabled. Available options: Any valid string text
  DISABLED: '&cspawnstash is currently disabled.'
  # The text or value for Player Only. Available options: Any valid string text
  PLAYER-ONLY: '&conly players can use this command.'
  # The text or value for No Permission. Available options: Any valid string text
  NO-PERMISSION: '&cyou do not have permission.'
  # The text or value for No Active. Available options: Any valid string text
  NO-ACTIVE: '&cno active stash found.'
  # The text or value for Blocked Break. Available options: Any valid string text
  BLOCKED-BREAK: '&cthis stash is protected. use &f/spawnstash remove nearest &cto
    rollback it.'
  # The text or value for Spawner Claimed. Available options: Any valid string text
  SPAWNER-CLAIMED: '&aclaimed &f{amount}x {spawner}&a from spawnstash.'
  # The text or value for Spawner Claimed Dropped. Available options: Any valid string text
  SPAWNER-CLAIMED-DROPPED: '&aclaimed &f{amount}x {spawner}&a. &7inventory full, item
    dropped.'
  # The text or value for Invalid Type. Available options: Any valid string text
  INVALID-TYPE: '&cunknown stash type ''&f{type}&c''. use &f/spawnstash list&c.'
  # The text or value for Invalid Config. Available options: Any valid string text
  INVALID-CONFIG: '&cspawnstash config is invalid: &f{reason}&c.'
  # Configuration section for Alert.
  ALERT:
  - '&8[&dspawnstash&8] &f{player} &7triggered &d{reason}&7 on stash &f#{id}&7 (&f{type}&7)'
  - '&7location: &f{world} {x}, {y}, {z} &8| &7created by: &f{creator}'
  # The text or value for Alert Hover. Available options: Any valid string text
  ALERT-HOVER: '&eclick to teleport to &f{player}'
# Configuration section for Types.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `MESSAGES.USAGE` | `list` | List of configured items/strings | `[&8&m----------- &dspawnstash &8&m-----------, &f/{label} &7- spawn random bait stash, &f/{label} <type> &7- spawn a bait stash...]` | Configures the technical `USAGE` parameter for `MESSAGES.USAGE` in `spawn-stash.yml`. |
| `MESSAGES.SPAWNED` | `str` | Any string text | `'&aspawned stash #{type} successfull...'` | Configures the technical `SPAWNED` parameter for `MESSAGES.SPAWNED` in `spawn-stash.yml`. |
| `MESSAGES.REMOVED` | `str` | Any string text | `'&aremoved stash &f#{id}&a.'` | Configures the technical `REMOVED` parameter for `MESSAGES.REMOVED` in `spawn-stash.yml`. |
| `MESSAGES.REMOVED-ALL` | `str` | Any string text | `'&aremoved &f{count}&a active stash(...'` | Configures the technical `REMOVED-ALL` parameter for `MESSAGES.REMOVED-ALL` in `spawn-stash.yml`. |
| `MESSAGES.EXPIRED` | `str` | Any string text | `'&7spawnstash #{id} expired and was ...'` | Configures the technical `EXPIRED` parameter for `MESSAGES.EXPIRED` in `spawn-stash.yml`. |
| `MESSAGES.RELOADED` | `str` | Any string text | `'&aspawnstash settings reloaded.'` | Configures the technical `RELOADED` parameter for `MESSAGES.RELOADED` in `spawn-stash.yml`. |
| `MESSAGES.DISABLED` | `str` | Any string text | `'&cspawnstash is currently disabled.'` | Configures the technical `DISABLED` parameter for `MESSAGES.DISABLED` in `spawn-stash.yml`. |
| `MESSAGES.PLAYER-ONLY` | `str` | Any string text | `'&conly players can use this command...'` | Configures the technical `PLAYER-ONLY` parameter for `MESSAGES.PLAYER-ONLY` in `spawn-stash.yml`. |
| `MESSAGES.NO-PERMISSION` | `str` | Any string text | `'&cyou do not have permission.'` | Configures the technical `NO-PERMISSION` parameter for `MESSAGES.NO-PERMISSION` in `spawn-stash.yml`. |
| `MESSAGES.NO-ACTIVE` | `str` | Any string text | `'&cno active stash found.'` | Configures the technical `NO-ACTIVE` parameter for `MESSAGES.NO-ACTIVE` in `spawn-stash.yml`. |
| `MESSAGES.BLOCKED-BREAK` | `str` | Any string text | `'&cthis stash is protected. use &f/s...'` | Configures the technical `BLOCKED-BREAK` parameter for `MESSAGES.BLOCKED-BREAK` in `spawn-stash.yml`. |
| `MESSAGES.SPAWNER-CLAIMED` | `str` | Any string text | `'&aclaimed &f{amount}x {spawner}&a f...'` | Configures the technical `SPAWNER-CLAIMED` parameter for `MESSAGES.SPAWNER-CLAIMED` in `spawn-stash.yml`. |
| `MESSAGES.SPAWNER-CLAIMED-DROPPED` | `str` | Any string text | `'&aclaimed &f{amount}x {spawner}&a. ...'` | Configures the technical `SPAWNER-CLAIMED-DROPPED` parameter for `MESSAGES.SPAWNER-CLAIMED-DROPPED` in `spawn-stash.yml`. |
| `MESSAGES.INVALID-TYPE` | `str` | Any string text | `'&cunknown stash type '&f{type}&c'. ...'` | Configures the technical `INVALID-TYPE` parameter for `MESSAGES.INVALID-TYPE` in `spawn-stash.yml`. |
| `MESSAGES.INVALID-CONFIG` | `str` | Any string text | `'&cspawnstash config is invalid: &f{...'` | Configures the technical `INVALID-CONFIG` parameter for `MESSAGES.INVALID-CONFIG` in `spawn-stash.yml`. |
| `MESSAGES.ALERT` | `list` | List of configured items/strings | `['&8[&dspawnstash&8] &f{player} &7triggered &d{reason}&7 on stash &f#{id}&7 (&f{type}&7)', '&7location: &f{world} {x}, {y}, {z} &8\| &7created by: &f{creator}']` | Configures the technical `ALERT` parameter for `MESSAGES.ALERT` in `spawn-stash.yml`. |
| `MESSAGES.ALERT-HOVER` | `str` | Any string text | `'&eclick to teleport to &f{player}'` | Configures the technical `ALERT-HOVER` parameter for `MESSAGES.ALERT-HOVER` in `spawn-stash.yml`. |

### 3. Practical Setup Example

```yaml
MESSAGES:
  # Configuration section for Usage.
  USAGE:
  - '&8&m----------- &dspawnstash &8&m-----------'
  - '&f/{label} &7- spawn random bait stash'
  - '&f/{label} <type> &7- spawn a bait stash'
  - '&f/{label} spawn <type> &7- spawn a bait stash'
  - '&f/{label} list &7- list active/configured stashes'
  - '&f/{label} remove <id|nearest|all> &7- rollback stashes'
  - '&f/{label} reload &7- reload spawn-stash.yml'
  # The text or value for Spawned. Available options: Any valid string text
  SPAWNED: '&aspawned stash #{type} successfully. &7(id: {id})'
  # The text or value for Removed. Available options: Any valid string text
  REMOVED: '&aremoved stash &f#{id}&a.'
  # The text or value for Removed All. Available options: Any valid string text
  REMOVED-ALL: '&aremoved &f{count}&a active stash(es).'
  # The text or value for Expired. Available options: Any valid string text
  EXPIRED: '&7spawnstash #{id} expired and was rolled back.'
  # The text or value for Reloaded. Available options: Any valid string text
  RELOADED: '&aspawnstash settings reloaded.'
  # The text or value for Disabled. Available options: Any valid string text
  DISABLED: '&cspawnstash is currently disabled.'
  # The text or value for Player Only. Available options: Any valid string text
  PLAYER-ONLY: '&conly players can use this command.'
  # The text or value for No Permission. Available options: Any valid string text
  NO-PERMISSION: '&cyou do not have permission.'
  # The text or value for No Active. Available options: Any valid string text
  NO-ACTIVE: '&cno active stash found.'
  # The text or value for Blocked Break. Available options: Any valid string text
  BLOCKED-BREAK: '&cthis stash is protected. use &f/spawnstash remove nearest &cto
    rollback it.'
  # The text or value for Spawner Claimed. Available options: Any valid string text
  SPAWNER-CLAIMED: '&aclaimed &f{amount}x {spawner}&a from spawnstash.'
  # The text or value for Spawner Claimed Dropped. Available options: Any valid string text
  SPAWNER-CLAIMED-DROPPED: '&aclaimed &f{amount}x {spawner}&a. &7inventory full, item
    dropped.'
  # The text or value for Invalid Type. Available options: Any valid string text
  INVALID-TYPE: '&cunknown stash type ''&f{type}&c''. use &f/spawnstash list&c.'
  # The text or value for Invalid Config. Available options: Any valid string text
  INVALID-CONFIG: '&cspawnstash config is invalid: &f{reason}&c.'
  # Configuration section for Alert.
  ALERT:
  - '&8[&dspawnstash&8] &f{player} &7triggered &d{reason}&7 on stash &f#{id}&7 (&f{type}&7)'
  - '&7location: &f{world} {x}, {y}, {z} &8| &7created by: &f{creator}'
  # The text or value for Alert Hover. Available options: Any valid string text
  ALERT-HOVER: '&eclick to teleport to &f{player}'
# Configuration section for Types.
```

---

## Section: `TYPES`

### 1. Commented Setup Code Example

```yaml
TYPES:
  # Configuration section for '1'.
  '1':
    # Determines whether Enabled is enabled or disabled. Available options: true, false
    ENABLED: true
    # The text or value for Display Name. Available options: Any valid string text
    DISPLAY_NAME: '&dcompact amethyst stash'
    # The numerical value for Ttl Seconds. Available options: Any valid integer
    TTL_SECONDS: 900
    # The decimal value for Alert Radius. Available options: Any decimal number
    ALERT_RADIUS: 8.0
    # Configuration section for Paste Offset.
    PASTE_OFFSET:
    - 0
    - 0
    - 0
    # Configuration section for Blocks.
    BLOCKS:
    - OFFSET:
      - -1
      - 0
      - 0
      MATERIAL: DEEPSLATE
    - OFFSET:
      - 0
      - 0
      - 0
      MATERIAL: SPAWNER
      # The text or value for Spawner Type. Available options: Any valid string text
      SPAWNER_TYPE: SKELETON
      # The text or value for Spawner Access. Available options: Any valid string text
      SPAWNER_ACCESS: PUBLIC
    - OFFSET:
      - 1
      - 0
      - 0
      MATERIAL: DEEPSLATE
    - OFFSET:
      - -1
      - 0
      - 1
      MATERIAL: COBBLED_DEEPSLATE
    - OFFSET:
      - 0
      - 0
      - 1
      MATERIAL: AMETHYST_BLOCK
    - OFFSET:
      - 1
      - 0
      - 1
      MATERIAL: COBBLED_DEEPSLATE
    - OFFSET:
      - -1
      - 1
      - 0
      MATERIAL: DEEPSLATE_TILES
    - OFFSET:
      - 1
      - 1
      - 0
      MATERIAL: DEEPSLATE_TILES
    - OFFSET:
      - -1
      - 1
      - 1
      MATERIAL: DEEPSLATE
    - OFFSET:
      - 0
      - 1
      - 1
      MATERIAL: BUDDING_AMETHYST
    - OFFSET:
      - 1
      - 1
      - 1
      MATERIAL: DEEPSLATE
    - OFFSET:
      - 0
      - 2
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `TYPES.1.ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `TYPES` system. Set to `true` to enable, `false` to disable. |
| `TYPES.1.DISPLAY_NAME` | `str` | Any string text | `'&dcompact amethyst stash'` | Configures the technical `DISPLAY_NAME` parameter for `TYPES.1.DISPLAY_NAME` in `spawn-stash.yml`. |
| `TYPES.1.TTL_SECONDS` | `int` | Any valid integer number | `'900'` | Configures the technical `TTL_SECONDS` parameter for `TYPES.1.TTL_SECONDS` in `spawn-stash.yml`. |
| `TYPES.1.ALERT_RADIUS` | `float` | Any decimal number | `'8.0'` | Configures the technical `ALERT_RADIUS` parameter for `TYPES.1.ALERT_RADIUS` in `spawn-stash.yml`. |
| `TYPES.1.PASTE_OFFSET` | `list` | List of configured items/strings | `[0, 0, 0]` | Configures the technical `PASTE_OFFSET` parameter for `TYPES.1.PASTE_OFFSET` in `spawn-stash.yml`. |
| `TYPES.1.BLOCKS` | `list` | List of configured items/strings | `[{'OFFSET': [-1, 0, 0], 'MATERIAL': 'DEEPSLATE'}, {'OFFSET': [0, 0, 0], 'MATERIAL': 'SPAWNER', 'SPAWNER_TYPE': 'SKELETON', 'SPAWNER_ACCESS': 'PUBLIC'}, {'OFFSET': [1, 0, 0], 'MATERIAL': 'DEEPSLATE'}...]` | Configures the technical `BLOCKS` parameter for `TYPES.1.BLOCKS` in `spawn-stash.yml`. |
| `TYPES.2.ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `TYPES` system. Set to `true` to enable, `false` to disable. |
| `TYPES.2.DISPLAY_NAME` | `str` | Any string text | `'&5double spawner chest stash'` | Configures the technical `DISPLAY_NAME` parameter for `TYPES.2.DISPLAY_NAME` in `spawn-stash.yml`. |
| `TYPES.2.PASTE_OFFSET` | `list` | List of configured items/strings | `[0, 0, 0]` | Configures the technical `PASTE_OFFSET` parameter for `TYPES.2.PASTE_OFFSET` in `spawn-stash.yml`. |
| `TYPES.2.BLOCKS` | `list` | List of configured items/strings | `[{'OFFSET': [-1, 0, 0], 'MATERIAL': 'COBBLED_DEEPSLATE'}, {'OFFSET': [0, 0, 0], 'MATERIAL': 'COBBLED_DEEPSLATE'}, {'OFFSET': [1, 0, 0], 'MATERIAL': 'COBBLED_DEEPSLATE'}...]` | Configures the technical `BLOCKS` parameter for `TYPES.2.BLOCKS` in `spawn-stash.yml`. |
| `TYPES.3.ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `TYPES` system. Set to `true` to enable, `false` to disable. |
| `TYPES.3.DISPLAY_NAME` | `str` | Any string text | `'&amossy ore stash'` | Configures the technical `DISPLAY_NAME` parameter for `TYPES.3.DISPLAY_NAME` in `spawn-stash.yml`. |
| `TYPES.3.PASTE_OFFSET` | `list` | List of configured items/strings | `[0, 0, 0]` | Configures the technical `PASTE_OFFSET` parameter for `TYPES.3.PASTE_OFFSET` in `spawn-stash.yml`. |
| `TYPES.3.BLOCKS` | `list` | List of configured items/strings | `[{'OFFSET': [-1, 0, 0], 'MATERIAL': 'MOSSY_COBBLESTONE'}, {'OFFSET': [0, 0, 0], 'MATERIAL': 'MOSSY_COBBLESTONE'}, {'OFFSET': [1, 0, 0], 'MATERIAL': 'MOSSY_COBBLESTONE'}...]` | Configures the technical `BLOCKS` parameter for `TYPES.3.BLOCKS` in `spawn-stash.yml`. |
| `TYPES.4.ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `TYPES` system. Set to `true` to enable, `false` to disable. |
| `TYPES.4.DISPLAY_NAME` | `str` | Any string text | `'&6mineshaft sign stash'` | Configures the technical `DISPLAY_NAME` parameter for `TYPES.4.DISPLAY_NAME` in `spawn-stash.yml`. |
| `TYPES.4.PASTE_OFFSET` | `list` | List of configured items/strings | `[0, 0, 0]` | Configures the technical `PASTE_OFFSET` parameter for `TYPES.4.PASTE_OFFSET` in `spawn-stash.yml`. |
| `TYPES.4.BLOCKS` | `list` | List of configured items/strings | `[{'OFFSET': [-1, 0, 0], 'MATERIAL': 'DEEPSLATE'}, {'OFFSET': [0, 0, 0], 'MATERIAL': 'RAIL'}, {'OFFSET': [1, 0, 0], 'MATERIAL': 'DEEPSLATE'}...]` | Configures the technical `BLOCKS` parameter for `TYPES.4.BLOCKS` in `spawn-stash.yml`. |
| `TYPES.5.ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `TYPES` system. Set to `true` to enable, `false` to disable. |
| `TYPES.5.DISPLAY_NAME` | `str` | Any string text | `'&dpremium amethyst spawner stash'` | Configures the technical `DISPLAY_NAME` parameter for `TYPES.5.DISPLAY_NAME` in `spawn-stash.yml`. |
| `TYPES.5.TTL_SECONDS` | `int` | Any valid integer number | `'1200'` | Configures the technical `TTL_SECONDS` parameter for `TYPES.5.TTL_SECONDS` in `spawn-stash.yml`. |
| `TYPES.5.ALERT_RADIUS` | `float` | Any decimal number | `'10.0'` | Configures the technical `ALERT_RADIUS` parameter for `TYPES.5.ALERT_RADIUS` in `spawn-stash.yml`. |
| `TYPES.5.PASTE_OFFSET` | `list` | List of configured items/strings | `[0, 0, 0]` | Configures the technical `PASTE_OFFSET` parameter for `TYPES.5.PASTE_OFFSET` in `spawn-stash.yml`. |
| `TYPES.5.BLOCKS` | `list` | List of configured items/strings | `[{'OFFSET': [-1, 0, 0], 'MATERIAL': 'CHISELED_DEEPSLATE'}, {'OFFSET': [0, 0, 0], 'MATERIAL': 'AMETHYST_BLOCK'}, {'OFFSET': [1, 0, 0], 'MATERIAL': 'CHISELED_DEEPSLATE'}...]` | Configures the technical `BLOCKS` parameter for `TYPES.5.BLOCKS` in `spawn-stash.yml`. |

### 3. Practical Setup Example

```yaml
TYPES:
  # Configuration section for '1'.
  '1':
    # Determines whether Enabled is enabled or disabled. Available options: true, false
    ENABLED: true
    # The text or value for Display Name. Available options: Any valid string text
    DISPLAY_NAME: '&dcompact amethyst stash'
    # The numerical value for Ttl Seconds. Available options: Any valid integer
    TTL_SECONDS: 900
    # The decimal value for Alert Radius. Available options: Any decimal number
    ALERT_RADIUS: 8.0
    # Configuration section for Paste Offset.
    PASTE_OFFSET:
    - 0
    - 0
    - 0
    # Configuration section for Blocks.
    BLOCKS:
    - OFFSET:
      - -1
      - 0
      - 0
      MATERIAL: DEEPSLATE
    - OFFSET:
      - 0
      - 0
      - 0
      MATERIAL: SPAWNER
      # The text or value for Spawner Type. Available options: Any valid string text
      SPAWNER_TYPE: SKELETON
      # The text or value for Spawner Access. Available options: Any valid string text
      SPAWNER_ACCESS: PUBLIC
    - OFFSET:
      - 1
      - 0
      - 0
      MATERIAL: DEEPSLATE
    - OFFSET:
      - -1
      - 0
      - 1
      MATERIAL: COBBLED_DEEPSLATE
    - OFFSET:
      - 0
      - 0
      - 1
      MATERIAL: AMETHYST_BLOCK
    - OFFSET:
      - 1
      - 0
      - 1
      MATERIAL: COBBLED_DEEPSLATE
    - OFFSET:
      - -1
      - 1
      - 0
      MATERIAL: DEEPSLATE_TILES
    - OFFSET:
      - 1
      - 1
      - 0
      MATERIAL: DEEPSLATE_TILES
    - OFFSET:
      - -1
      - 1
      - 1
      MATERIAL: DEEPSLATE
    - OFFSET:
      - 0
      - 1
      - 1
      MATERIAL: BUDDING_AMETHYST
    - OFFSET:
      - 1
      - 1
      - 1
      MATERIAL: DEEPSLATE
    - OFFSET:
      - 0
      - 2
```

---

