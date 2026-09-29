# Detailed Configuration & Setup Guide: `spawners.yml`

This is the official, 100% complete technical setup guide for `spawners.yml` in **UltimateDonutSMP**.
Each section details the exact commented setup code block, allowed option values, data types, default values, and in-depth functional behavior.

---

## Section: `SETTINGS`

### 1. Commented Setup Code Example

```yaml
SETTINGS:
  # Determines whether Enabled is enabled or disabled. Available options: true, false
  ENABLED: true
  # The Access Mode setting. Available options: OWNER_ONLY, OWNER_AND_TEAM, PUBLIC
  ACCESS_MODE: OWNER_ONLY
  # Determines whether spawner stealing (breaking or accessing other players' spawners) is allowed globally. Available options: true, false
  ALLOW_SPAWNER_STEAL: false
  # The numerical value for Generation Interval Seconds. Available options: Any valid integer
  GENERATION_INTERVAL_SECONDS: 5
  # Determines whether Process Only Loaded Chunks is enabled or disabled. Available options: true, false
  PROCESS_ONLY_LOADED_CHUNKS: true
  # Determines whether Require Player Nearby is enabled or disabled. Available options: true, false
  REQUIRE_PLAYER_NEARBY: false
  # The numerical value for Player Nearby Radius. Available options: Any valid integer
  PLAYER_NEARBY_RADIUS: 16
  # The numerical value for Max Stack Per Block. Available options: Any valid integer
  MAX_STACK_PER_BLOCK: 100000
  # The numerical value for Storage Cap Per Loot Key. Available options: Any valid integer
  STORAGE_CAP_PER_LOOT_KEY: 1000000
  # Determines whether Drop On Break If Inventory Full is enabled or disabled. Available options: true, false
  DROP_ON_BREAK_IF_INVENTORY_FULL: true
  # Determines whether a Silk Touch pickaxe is required to break and collect spawners.
  REQUIRE_SILK_TOUCH: true
  # Determines whether physical vanilla mob spawning is cancelled (set to true for virtual storage anti-lag drops, set to false to allow physical mobs to spawn in world).
  CANCEL_MOB_SPAWN: true
  # Determines whether XP generation and XP collection is enabled for spawners. Available options: true, false
  XP_ENABLED: true
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `SETTINGS.ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `SETTINGS` system. Set to `true` to enable, `false` to disable. |
| `SETTINGS.ACCESS_MODE` | `str` | `OWNER_ONLY`, `OWNER_AND_TEAM`, `PUBLIC` | `'OWNER_ONLY'` | Controls spawner access permissions:<br>- `OWNER_ONLY`: Only spawner owner.<br>- `OWNER_AND_TEAM`: Owner and team members.<br>- `PUBLIC`: Anyone on the server. |
| `SETTINGS.ALLOW_SPAWNER_STEAL` | `bool` | `true`, `false` | `false` | Configures the technical `ALLOW_SPAWNER_STEAL` parameter for `SETTINGS.ALLOW_SPAWNER_STEAL` in `spawners.yml`. |
| `SETTINGS.GENERATION_INTERVAL_SECONDS` | `int` | Any valid integer number | `'5'` | Configures the technical `GENERATION_INTERVAL_SECONDS` parameter for `SETTINGS.GENERATION_INTERVAL_SECONDS` in `spawners.yml`. |
| `SETTINGS.PROCESS_ONLY_LOADED_CHUNKS` | `bool` | `true`, `false` | `true` | Configures the technical `PROCESS_ONLY_LOADED_CHUNKS` parameter for `SETTINGS.PROCESS_ONLY_LOADED_CHUNKS` in `spawners.yml`. |
| `SETTINGS.REQUIRE_PLAYER_NEARBY` | `bool` | `true`, `false` | `false` | Configures the technical `REQUIRE_PLAYER_NEARBY` parameter for `SETTINGS.REQUIRE_PLAYER_NEARBY` in `spawners.yml`. |
| `SETTINGS.PLAYER_NEARBY_RADIUS` | `int` | Any valid integer number | `'16'` | Configures the technical `PLAYER_NEARBY_RADIUS` parameter for `SETTINGS.PLAYER_NEARBY_RADIUS` in `spawners.yml`. |
| `SETTINGS.MAX_STACK_PER_BLOCK` | `int` | Any valid integer number | `'100000'` | Configures the technical `MAX_STACK_PER_BLOCK` parameter for `SETTINGS.MAX_STACK_PER_BLOCK` in `spawners.yml`. |
| `SETTINGS.STORAGE_CAP_PER_LOOT_KEY` | `int` | Any valid integer number | `'1000000'` | Configures the technical `STORAGE_CAP_PER_LOOT_KEY` parameter for `SETTINGS.STORAGE_CAP_PER_LOOT_KEY` in `spawners.yml`. |
| `SETTINGS.DROP_ON_BREAK_IF_INVENTORY_FULL` | `bool` | `true`, `false` | `true` | Configures the technical `DROP_ON_BREAK_IF_INVENTORY_FULL` parameter for `SETTINGS.DROP_ON_BREAK_IF_INVENTORY_FULL` in `spawners.yml`. |
| `SETTINGS.REQUIRE_SILK_TOUCH` | `bool` | `true`, `false` | `true` | Requires a Silk Touch pickaxe to break spawners, covering both plugin-managed and vanilla spawners. Creative mode and `ultimatedonutsmp.spawner.bypass` are exempt; operators are not. |
| `SETTINGS.CANCEL_MOB_SPAWN` | `bool` | `true`, `false` | `true` | Cancels physical mob entity spawning in the world and routes loot directly to virtual storage, eliminating mob AI server lag. |
| `SETTINGS.XP_ENABLED` | `bool` | `true`, `false` | `true` | Configures the technical `XP_ENABLED` parameter for `SETTINGS.XP_ENABLED` in `spawners.yml`. |

### 3. Practical Setup Example

```yaml
SETTINGS:
  # Determines whether Enabled is enabled or disabled. Available options: true, false
  ENABLED: true
  # The Access Mode setting. Available options: OWNER_ONLY, OWNER_AND_TEAM, PUBLIC
  ACCESS_MODE: OWNER_ONLY
  # Determines whether spawner stealing (breaking or accessing other players' spawners) is allowed globally. Available options: true, false
  ALLOW_SPAWNER_STEAL: false
  # The numerical value for Generation Interval Seconds. Available options: Any valid integer
  GENERATION_INTERVAL_SECONDS: 5
  # Determines whether Process Only Loaded Chunks is enabled or disabled. Available options: true, false
  PROCESS_ONLY_LOADED_CHUNKS: true
  # Determines whether Require Player Nearby is enabled or disabled. Available options: true, false
  REQUIRE_PLAYER_NEARBY: false
  # The numerical value for Player Nearby Radius. Available options: Any valid integer
  PLAYER_NEARBY_RADIUS: 16
  # The numerical value for Max Stack Per Block. Available options: Any valid integer
  MAX_STACK_PER_BLOCK: 100000
  # The numerical value for Storage Cap Per Loot Key. Available options: Any valid integer
  STORAGE_CAP_PER_LOOT_KEY: 1000000
  # Determines whether Drop On Break If Inventory Full is enabled or disabled. Available options: true, false
  DROP_ON_BREAK_IF_INVENTORY_FULL: true
  # Determines whether a Silk Touch pickaxe is required to break and collect spawners.
  REQUIRE_SILK_TOUCH: true
  # Determines whether physical vanilla mob spawning is cancelled (set to true for virtual storage anti-lag drops, set to false to allow physical mobs to spawn in world).
  CANCEL_MOB_SPAWN: true
  # Determines whether XP generation and XP collection is enabled for spawners. Available options: true, false
  XP_ENABLED: true
```

---

## Section: `GUI`

### 1. Commented Setup Code Example

```yaml
GUI:
  # Configuration section for Main Menu.
  MAIN_MENU:
    TITLE: '{stack} {mob}'
    SIZE: 27
  # Configuration section for Storage.
  STORAGE:
    TITLE: '&8{mob} Spawners - {page}/{max_page}'
    SIZE: 54
    # The numerical value for Items Per Page. Available options: Any valid integer
    ITEMS_PER_PAGE: 45
  # Configuration section for Panel.
  PANEL:
    TITLE: '&8Spawners - {world}'
    SIZE: 54
  # Configuration section for World List.
  WORLD_LIST:
    TITLE: '&8Spawners Panel'
    SIZE: 27
# Configuration section for Types.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `GUI.MAIN_MENU.TITLE` | `str` | Any string text | `'{stack} {mob}'` | Configures the technical `TITLE` parameter for `GUI.MAIN_MENU.TITLE` in `spawners.yml`. |
| `GUI.MAIN_MENU.SIZE` | `int` | Any valid integer number | `'27'` | Configures the technical `SIZE` parameter for `GUI.MAIN_MENU.SIZE` in `spawners.yml`. |
| `GUI.STORAGE.TITLE` | `str` | Any string text | `'&8{mob} Spawners - {page}/{max_page...'` | Configures the technical `TITLE` parameter for `GUI.STORAGE.TITLE` in `spawners.yml`. |
| `GUI.STORAGE.SIZE` | `int` | Any valid integer number | `'54'` | Configures the technical `SIZE` parameter for `GUI.STORAGE.SIZE` in `spawners.yml`. |
| `GUI.STORAGE.ITEMS_PER_PAGE` | `int` | Any valid integer number | `'45'` | Configures the technical `ITEMS_PER_PAGE` parameter for `GUI.STORAGE.ITEMS_PER_PAGE` in `spawners.yml`. |
| `GUI.PANEL.TITLE` | `str` | Any string text | `'&8Spawners - {world}'` | Configures the technical `TITLE` parameter for `GUI.PANEL.TITLE` in `spawners.yml`. |
| `GUI.PANEL.SIZE` | `int` | Any valid integer number | `'54'` | Configures the technical `SIZE` parameter for `GUI.PANEL.SIZE` in `spawners.yml`. |
| `GUI.WORLD_LIST.TITLE` | `str` | Any string text | `'&8Spawners Panel'` | Configures the technical `TITLE` parameter for `GUI.WORLD_LIST.TITLE` in `spawners.yml`. |
| `GUI.WORLD_LIST.SIZE` | `int` | Any valid integer number | `'27'` | Configures the technical `SIZE` parameter for `GUI.WORLD_LIST.SIZE` in `spawners.yml`. |

### 3. Practical Setup Example

```yaml
GUI:
  # Configuration section for Main Menu.
  MAIN_MENU:
    TITLE: '{stack} {mob}'
    SIZE: 27
  # Configuration section for Storage.
  STORAGE:
    TITLE: '&8{mob} Spawners - {page}/{max_page}'
    SIZE: 54
    # The numerical value for Items Per Page. Available options: Any valid integer
    ITEMS_PER_PAGE: 45
  # Configuration section for Panel.
  PANEL:
    TITLE: '&8Spawners - {world}'
    SIZE: 54
  # Configuration section for World List.
  WORLD_LIST:
    TITLE: '&8Spawners Panel'
    SIZE: 27
```

---

## Section: `MESSAGES`

### 1. Commented Setup Code Example

```yaml
MESSAGES:
  # The text or value for Placed. Available options: Any valid string text
  PLACED: '&aplaced &f{amount}x {type}&a.'
  # The text or value for Stacked. Available options: Any valid string text
  STACKED: '&aspawner stack updated to &f{amount}&a.'
  # The text or value for Picked Up. Available options: Any valid string text
  PICKED-UP: '&apicked up &f{amount}x {type}&a.'
  # The text or value for Removed. Available options: Any valid string text
  REMOVED: '&aremoved &f{amount}x {type}&a.'
  # The text or value for Removed Admin. Available options: Any valid string text
  REMOVED-ADMIN: '&aspawner removed.'
  # The text or value for Gave Spawner. Available options: Any valid string text
  GAVE-SPAWNER: '&aGave &f{amount}x {type}&a to &f{player}&a.'
  # The text or value for Received Spawner. Available options: Any valid string text
  RECEIVED-SPAWNER: '&aYou received &f{amount}x {type}&a.'
  # The text or value for Collected. Available options: Any valid string text
  COLLECTED: '&acollected &f{amount}x {item}&a.'
  # The text or value for Collected All. Available options: Any valid string text
  COLLECTED-ALL: '&acollected &f{amount}&a items from this spawner.'
  # The text or value for Dropped Page. Available options: Any valid string text
  DROPPED-PAGE: '&adropped &f{amount}&a stored items from this page on the ground.'
  # The text or value for Sold. Available options: Any valid string text
  SOLD: '&asold &f{amount}&a items for {price}&a.'
  # The text or value for Collected Xp. Available options: Any valid string text
  COLLECTED-XP: '&acollected &f{amount} &aXP points!'
  # The text or value for Reloaded. Available options: Any valid string text
  RELOADED: '&aSpawner settings reloaded.'
  # The text or value for Split Success. Available options: Any valid string text
  SPLIT-SUCCESS: '&aSplit &f{amount}x &aspawners. &7Remaining in hand: &f{remaining}&7.'
  # The text or value for Filter Enabled All. Available options: Any valid string text
  FILTER-ENABLED-ALL: '&aEnabled storing for all drops on this spawner.'
  # The text or value for Filter Disabled All. Available options: Any valid string text
  FILTER-DISABLED-ALL: '&cDisabled storing for all drops on this spawner.'
  # The text or value for Filter Toggled. Available options: Any valid string text
  FILTER-TOGGLED: '&aToggled filter for &f{item} &ato {status}&a.'
  # The text or value for Filter Status Enabled. Available options: Any valid string text
  FILTER-STATUS-ENABLED: '&aEnabled &7(Storing)'
  # The text or value for Filter Status Disabled. Available options: Any valid string text
  FILTER-STATUS-DISABLED: '&cDisabled &7(Not Storing)'
  # The text or value for Teleported. Available options: Any valid string text
  TELEPORTED: '&aTeleported to spawner at &f{x}, {y}, {z}&a in &f{world}&a.'
  # The text or value for Disabled. Available options: Any valid string text
  DISABLED: '&cspawner system is currently disabled.'
  # The text or value for Not Spawner Item. Available options: Any valid string text
  NOT-SPAWNER-ITEM: '&cthat is not a managed spawner item.'
  # The text or value for Invalid Amount. Available options: Any valid string text
  INVALID-AMOUNT: '&cthis spawner item has an invalid amount.'
  # The text or value for Max Stack Exceeded. Available options: Any valid string text
  MAX-STACK-EXCEEDED: '&cthat would exceed the max stack per block (&f{max}&c).'
  # The text or value for Already At Max Stack. Available options: Any valid string text
  ALREADY-AT-MAX-STACK: '&cthat spawner is already at the max stack per block (&f{max}&c).'
  # The text or value for Already Spawner. Available options: Any valid string text
  ALREADY-SPAWNER: '&cthat block is already a managed spawner.'
  # The text or value for Not Owner. Available options: Any valid string text
  NOT-OWNER: '&cyou do not own that spawner.'
  # The text or value for No Access. Available options: Any valid string text
  NO-ACCESS: '&cyou do not have access to that spawner.'
  # The text or value for No Break Permission. Available options: Any valid string text
  NO-BREAK-PERMISSION: '&cyou do not have permission to break that spawner.'
  # The text or value for Silk Touch Required. Available options: Any valid string text
  SILK-TOUCH-REQUIRED: '&cYou need Silk Touch to break this spawner!'
  # The text or value for Type Mismatch. Available options: Any valid string text
  TYPE-MISMATCH: '&cyou can only stack the same spawner type onto this block.'
  # The text or value for Hold To Stack. Available options: Any valid string text
  HOLD-TO-STACK: '&chold a managed spawner item to stack.'
  # The text or value for Not Managed Spawner. Available options: Any valid string text
  NOT-MANAGED-SPAWNER: '&cthat is not a managed spawner.'
  # The text or value for Not Found. Available options: Any valid string text
  NOT-FOUND: '&cspawner not found.'
  # The text or value for Unknown Type. Available options: Any valid string text
  UNKNOWN-TYPE: '&cunknown spawner type ''&f{type}&c''.'
  # The text or value for Save Failed. Available options: Any valid string text
  SAVE-FAILED: '&cfailed to save that spawner. please try again.'
  # The text or value for Inventory Full. Available options: Any valid string text
  INVENTORY-FULL: '&cyour inventory is full.'
  # The text or value for Loot Empty. Available options: Any valid string text
  LOOT-EMPTY: '&cthat loot entry is empty.'
  # The text or value for No Space. Available options: Any valid string text
  NO-SPACE: '&cthere was no space to collect your spawner loot.'
  # The text or value for No Loot On Page. Available options: Any valid string text
  NO-LOOT-ON-PAGE: '&cthere is no loot stored on this page.'
  # The text or value for No Sellable Items. Available options: Any valid string text
  NO-SELLABLE-ITEMS: '&cthere are no sellable items stored in that spawner.'
  # The text or value for Sell Failed. Available options: Any valid string text
  SELL-FAILED: '&cfailed to pay out the spawner loot sale.'
  # The text or value for Xp Disabled. Available options: Any valid string text
  XP-DISABLED: '&cXP collection is disabled on this server.'
  # The text or value for No Xp Stored. Available options: Any valid string text
  NO-XP-STORED: '&cthere is no XP stored in that spawner.'
  # The text or value for World Not Loaded. Available options: Any valid string text
  WORLD-NOT-LOADED: '&cThat spawner''s world is not currently loaded.'
  # The text or value for Temp Registered. Available options: Any valid string text
  TEMP-REGISTERED: '&atemporary spawner registered.'
  # The text or value for Temp Removed. Available options: Any valid string text
  TEMP-REMOVED: '&atemporary spawner removed.'
  # The text or value for Temp Cannot Stack. Available options: Any valid string text
  TEMP-CANNOT-STACK: '&ctemporary spawners cannot be stacked.'
  # The text or value for Temp Needs Player Block. Available options: Any valid string text
  TEMP-NEEDS-PLAYER-BLOCK: '&ctemporary spawner needs a player and block.'
  # The text or value for No Panel Permission. Available options: Any valid string text
  NO-PANEL-PERMISSION: '&cYou do not have permission to open the spawner admin panel.'
  # The text or value for No Give Permission. Available options: Any valid string text
  NO-GIVE-PERMISSION: '&cYou do not have permission to give spawners.'
  # The text or value for No Reload Permission. Available options: Any valid string text
  NO-RELOAD-PERMISSION: '&cYou do not have permission to reload spawners.'
  # The text or value for No Remove Permission. Available options: Any valid string text
  NO-REMOVE-PERMISSION: '&cYou do not have permission to remove spawners.'
  # The text or value for Player Only. Available options: Any valid string text
  PLAYER-ONLY: 'Player only.'
  # The text or value for Target Not Online. Available options: Any valid string text
  TARGET-NOT-ONLINE: '&ctarget player must be online.'
  # The text or value for Player Not Online. Available options: Any valid string text
  PLAYER-NOT-ONLINE: '&cPlayer ''&f{player}&c'' must be online.'
  # The text or value for Amount Positive. Available options: Any valid string text
  AMOUNT-POSITIVE: '&cspawner amount must be positive.'
  # The text or value for Amount Invalid. Available options: Any valid string text
  AMOUNT-INVALID: '&cAmount must be a valid positive number.'
  # The text or value for Amount Greater Than Zero. Available options: Any valid string text
  AMOUNT-GREATER-THAN-ZERO: '&cAmount must be greater than zero.'
  # The text or value for Create Item Failed. Available options: Any valid string text
  CREATE-ITEM-FAILED: '&cfailed to create the spawner item.'
  # The text or value for Give Usage. Available options: Any valid string text
  GIVE-USAGE: 'Use /{label} give <player> <type> [amount]'
  # The text or value for Give Usage Command. Available options: Any valid string text
  GIVE-USAGE-COMMAND: '&cUsage: /spawner give <player> <type> [amount]'
  # The text or value for Look At Spawner Inspect. Available options: Any valid string text
  LOOK-AT-SPAWNER-INSPECT: '&cLook at a managed spawner to inspect it.'
  # The text or value for Look At Spawner Remove. Available options: Any valid string text
  LOOK-AT-SPAWNER-REMOVE: '&cLook at a managed spawner to remove it.'
  # The text or value for Hold Spawner To Split. Available options: Any valid string text
  HOLD-SPAWNER-TO-SPLIT: '&cYou must be holding a managed spawner item.'
  # The text or value for Cannot Split One. Available options: Any valid string text
  CANNOT-SPLIT-ONE: '&cThis spawner item cannot be split (amount is 1).'
  # The text or value for Split Usage. Available options: Any valid string text
  SPLIT-USAGE: '&cUsage: /spawner split <amount>'
  # The text or value for Invalid Split Amount. Available options: Any valid string text
  INVALID-SPLIT-AMOUNT: '&cInvalid split amount.'
  # The text or value for Split Greater Than Zero. Available options: Any valid string text
  SPLIT-GREATER-THAN-ZERO: '&cSplit amount must be greater than zero.'
  # The text or value for Split Less Than Current. Available options: Any valid string text
  SPLIT-LESS-THAN-CURRENT: '&cSplit amount must be less than the current stack size (&f{max}&c).'
  # The text or value for Split Failed. Available options: Any valid string text
  SPLIT-FAILED: '&cFailed to create split spawner item.'
  # Configuration section for Usage.
  USAGE:
  - '&8&m----------- &dSpawner &8&m-----------'
  - '&f/{label} &7- Open the spawner panel'
  - '&f/{label} info &7- Inspect the looked-at spawner'
  - '&f/{label} panel &7- Open the spawner admin panel'
  - '&f/{label} give <player> <type> [amount] &7- Give a spawner item'
  - '&f/{label} split <amount> &7- Split the held spawner item'
  - '&f/{label} reload &7- Reload spawner settings'
  - '&f/{label} remove &7- Remove the looked-at spawner'
  # The text or value for Info Header. Available options: Any valid string text
  INFO-HEADER: '&8&m----------- &bSpawner Info &8&m-----------'
  # The text or value for Info Type. Available options: Any valid string text
  INFO-TYPE: '&7Type: &f{type}'
  # The text or value for Info Rest Hidden. Available options: Any valid string text
  INFO-REST-HIDDEN: '&7The rest is hidden on spawners you cannot access.'
  # The text or value for Info Owner. Available options: Any valid string text
  INFO-OWNER: '&7Owner: &f{owner}'
  # The text or value for Info Stack. Available options: Any valid string text
  INFO-STACK: '&7Stack: &f{amount}'
  # The text or value for Info Stored Loot. Available options: Any valid string text
  INFO-STORED-LOOT: '&7Stored Loot: &f{amount}'
  # The text or value for Info Location. Available options: Any valid string text
  INFO-LOCATION: '&7Location: &f{world} {x}, {y}, {z}'
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `MESSAGES.PLACED` | `str` | Any string text | `'&aplaced &f{amount}x {type}&a.'` | Sent when a player places a spawner or stacks by placing. |
| `MESSAGES.STACKED` | `str` | Any string text | `'&aspawner stack updated to &f{amount}&a.'` | Sent when stacking onto an existing spawner block. |
| `MESSAGES.PICKED-UP` | `str` | Any string text | `'&apicked up &f{amount}x {type}&a.'` | Sent when breaking/collecting a spawner. |
| `MESSAGES.SILK-TOUCH-REQUIRED` | `str` | Any string text | `'&cYou need Silk Touch to break this spawner!'` | Sent when attempting to break without Silk Touch. |
| `MESSAGES.NO-ACCESS` | `str` | Any string text | `'&cyou do not have access to that spawner.'` | Sent when attempting to interact with or access another player's spawner. |
| `MESSAGES.SOLD` | `str` | Any string text | `'&asold &f{amount}&a items for {price}&a.'` | Sent upon selling stored spawner loot. |
| `MESSAGES.COLLECTED-XP` | `str` | Any string text | `'&acollected &f{amount} &aXP points!'` | Sent upon collecting stored XP. |
| `MESSAGES.USAGE` | `list` | List of strings | Standard help lines | Command usage lines displayed by `/spawner help` or invalid syntax. |
| `MESSAGES.INFO-HEADER` | `str` | Any string text | `'&8&m----------- &bSpawner Info &8&m-----------'` | Header line for `/spawner info`. |
| *(and all other sub-keys in section)* | | | | Full customization of in-game spawner messaging. |

### 3. Practical Setup Example

```yaml
MESSAGES:
  PLACED: '&aplaced &f{amount}x {type}&a.'
  STACKED: '&aspawner stack updated to &f{amount}&a.'
  PICKED-UP: '&apicked up &f{amount}x {type}&a.'
  SILK-TOUCH-REQUIRED: '&cYou need Silk Touch to break this spawner!'
  NO-ACCESS: '&cyou do not have access to that spawner.'
  SOLD: '&asold &f{amount}&a items for {price}&a.'
  COLLECTED-XP: '&acollected &f{amount} &aXP points!'
```

---

## Section: `TYPES`

### 1. Commented Setup Code Example

```yaml
TYPES:
  # Configuration section for Pig.
  PIG:
    # The text or value for Display Name. Available options: Any valid string text
    DISPLAY_NAME: '&dPig Spawner'
    # The text or value for Entity Type. Available options: Any valid string text
    ENTITY_TYPE: PIG
    # Custom head texture URL or Base64 (leave empty to use default mob head).
    HEAD_TEXTURE: 'https://textures.minecraft.net/texture/d875eb45aca34a4d24c3dc1395fc020ccf37f825a17b054a22fd24b189c24c'
    # The text or value for Icon Material. Available options: Any valid string text
    ICON_MATERIAL: PORKCHOP
    # The numerical value for Base Items Per Cycle. Available options: Any valid integer
    BASE_ITEMS_PER_CYCLE: 1
    # Configuration section for Drops.
    DROPS:
      # Configuration section for Porkchop.
      PORKCHOP:
        MATERIAL: PORKCHOP
        # The numerical value for Min. Available options: Any valid integer
        MIN: 1
        # The numerical value for Max. Available options: Any valid integer
        MAX: 3
        # The decimal value for Chance. Available options: Any decimal number
        CHANCE: 1.0
      # Configuration section for Leather.
      LEATHER:
        MATERIAL: LEATHER
        # The numerical value for Min. Available options: Any valid integer
        MIN: 0
        # The numerical value for Max. Available options: Any valid integer
        MAX: 1
        # The decimal value for Chance. Available options: Any decimal number
        CHANCE: 0.35
  # Configuration section for Cow.
  COW:
    # The text or value for Display Name. Available options: Any valid string text
    DISPLAY_NAME: '&dCow Spawner'
    # The text or value for Entity Type. Available options: Any valid string text
    ENTITY_TYPE: COW
    # Custom head texture URL or Base64 (leave empty to use default mob head).
    HEAD_TEXTURE: ''
    # The text or value for Icon Material. Available options: Any valid string text
    ICON_MATERIAL: BEEF
    # The numerical value for Base Items Per Cycle. Available options: Any valid integer
    BASE_ITEMS_PER_CYCLE: 1
    # The decimal value for XP generated per spawner cycle per stack.
    XP_PER_CYCLE: 3.7
    # Configuration section for Drops.
    DROPS:
      # Configuration section for Beef.
      BEEF:
        MATERIAL: BEEF
        # The numerical value for Min. Available options: Any valid integer
        MIN: 1
        # The numerical value for Max. Available options: Any valid integer
        MAX: 3
        # The decimal value for Chance. Available options: Any decimal number
        CHANCE: 1.0
      # Configuration section for Leather.
      LEATHER:
        MATERIAL: LEATHER
        # The numerical value for Min. Available options: Any valid integer
        MIN: 1
        # The numerical value for Max. Available options: Any valid integer
        MAX: 2
        # The decimal value for Chance. Available options: Any decimal number
        CHANCE: 0.8
  # Configuration section for Zombie.
  ZOMBIE:
    # The text or value for Display Name. Available options: Any valid string text
    DISPLAY_NAME: '&dZombie Spawner'
    # The text or value for Entity Type. Available options: Any valid string text
    ENTITY_TYPE: ZOMBIE
    # Custom head texture URL or Base64 (leave empty to use default mob head).
    HEAD_TEXTURE: ''
    # The text or value for Icon Material. Available options: Any valid string text
    ICON_MATERIAL: ROTTEN_FLESH
    # The numerical value for Base Items Per Cycle. Available options: Any valid integer
    BASE_ITEMS_PER_CYCLE: 1
    # Configuration section for Drops.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `TYPES.PIG.DISPLAY_NAME` | `str` | Any string text | `'&dPig Spawner'` | Configures the technical `DISPLAY_NAME` parameter for `TYPES.PIG.DISPLAY_NAME` in `spawners.yml`. |
| `TYPES.PIG.ENTITY_TYPE` | `str` | Any string text | `'PIG'` | Configures the technical `ENTITY_TYPE` parameter for `TYPES.PIG.ENTITY_TYPE` in `spawners.yml`. |
| `TYPES.PIG.HEAD_TEXTURE` | `str` | Any string text | `'https://textures.minecraft.net/text...'` | Configures the technical `HEAD_TEXTURE` parameter for `TYPES.PIG.HEAD_TEXTURE` in `spawners.yml`. |
| `TYPES.PIG.ICON_MATERIAL` | `str` | Any string text | `'PORKCHOP'` | Configures the technical `ICON_MATERIAL` parameter for `TYPES.PIG.ICON_MATERIAL` in `spawners.yml`. |
| `TYPES.PIG.BASE_ITEMS_PER_CYCLE` | `int` | Any valid integer number | `'1'` | Configures the technical `BASE_ITEMS_PER_CYCLE` parameter for `TYPES.PIG.BASE_ITEMS_PER_CYCLE` in `spawners.yml`. |
| `TYPES.PIG.DROPS.PORKCHOP.MATERIAL` | `str` | Any string text | `'PORKCHOP'` | Configures the technical `MATERIAL` parameter for `TYPES.PIG.DROPS.PORKCHOP.MATERIAL` in `spawners.yml`. |
| `TYPES.PIG.DROPS.PORKCHOP.MIN` | `int` | Any valid integer number | `'1'` | Configures the technical `MIN` parameter for `TYPES.PIG.DROPS.PORKCHOP.MIN` in `spawners.yml`. |
| `TYPES.PIG.DROPS.PORKCHOP.MAX` | `int` | Any valid integer number | `'3'` | Configures the technical `MAX` parameter for `TYPES.PIG.DROPS.PORKCHOP.MAX` in `spawners.yml`. |
| `TYPES.PIG.DROPS.PORKCHOP.CHANCE` | `float` | Any decimal number | `'1.0'` | Configures the technical `CHANCE` parameter for `TYPES.PIG.DROPS.PORKCHOP.CHANCE` in `spawners.yml`. |
| `TYPES.PIG.DROPS.LEATHER.MATERIAL` | `str` | Any string text | `'LEATHER'` | Configures the technical `MATERIAL` parameter for `TYPES.PIG.DROPS.LEATHER.MATERIAL` in `spawners.yml`. |
| `TYPES.PIG.DROPS.LEATHER.MIN` | `int` | Any valid integer number | `'0'` | Configures the technical `MIN` parameter for `TYPES.PIG.DROPS.LEATHER.MIN` in `spawners.yml`. |
| `TYPES.PIG.DROPS.LEATHER.MAX` | `int` | Any valid integer number | `'1'` | Configures the technical `MAX` parameter for `TYPES.PIG.DROPS.LEATHER.MAX` in `spawners.yml`. |
| `TYPES.PIG.DROPS.LEATHER.CHANCE` | `float` | Any decimal number | `'0.35'` | Configures the technical `CHANCE` parameter for `TYPES.PIG.DROPS.LEATHER.CHANCE` in `spawners.yml`. |
| `TYPES.COW.DISPLAY_NAME` | `str` | Any string text | `'&dCow Spawner'` | Configures the technical `DISPLAY_NAME` parameter for `TYPES.COW.DISPLAY_NAME` in `spawners.yml`. |
| `TYPES.COW.ENTITY_TYPE` | `str` | Any string text | `'COW'` | Configures the technical `ENTITY_TYPE` parameter for `TYPES.COW.ENTITY_TYPE` in `spawners.yml`. |
| `TYPES.COW.HEAD_TEXTURE` | `str` | Any string text | `''` | Configures the technical `HEAD_TEXTURE` parameter for `TYPES.COW.HEAD_TEXTURE` in `spawners.yml`. |
| `TYPES.COW.ICON_MATERIAL` | `str` | Any string text | `'BEEF'` | Configures the technical `ICON_MATERIAL` parameter for `TYPES.COW.ICON_MATERIAL` in `spawners.yml`. |
| `TYPES.COW.BASE_ITEMS_PER_CYCLE` | `int` | Any valid integer number | `'1'` | Configures the technical `BASE_ITEMS_PER_CYCLE` parameter for `TYPES.COW.BASE_ITEMS_PER_CYCLE` in `spawners.yml`. |
| `TYPES.COW.XP_PER_CYCLE` | `float` | Any decimal number | `'3.7'` | Configures the technical `XP_PER_CYCLE` parameter for `TYPES.COW.XP_PER_CYCLE` in `spawners.yml`. |
| `TYPES.COW.DROPS.BEEF.MATERIAL` | `str` | Any string text | `'BEEF'` | Configures the technical `MATERIAL` parameter for `TYPES.COW.DROPS.BEEF.MATERIAL` in `spawners.yml`. |
| `TYPES.COW.DROPS.BEEF.MIN` | `int` | Any valid integer number | `'1'` | Configures the technical `MIN` parameter for `TYPES.COW.DROPS.BEEF.MIN` in `spawners.yml`. |
| `TYPES.COW.DROPS.BEEF.MAX` | `int` | Any valid integer number | `'3'` | Configures the technical `MAX` parameter for `TYPES.COW.DROPS.BEEF.MAX` in `spawners.yml`. |
| `TYPES.COW.DROPS.BEEF.CHANCE` | `float` | Any decimal number | `'1.0'` | Configures the technical `CHANCE` parameter for `TYPES.COW.DROPS.BEEF.CHANCE` in `spawners.yml`. |
| `TYPES.COW.DROPS.LEATHER.MATERIAL` | `str` | Any string text | `'LEATHER'` | Configures the technical `MATERIAL` parameter for `TYPES.COW.DROPS.LEATHER.MATERIAL` in `spawners.yml`. |
| `TYPES.COW.DROPS.LEATHER.MIN` | `int` | Any valid integer number | `'1'` | Configures the technical `MIN` parameter for `TYPES.COW.DROPS.LEATHER.MIN` in `spawners.yml`. |
| `TYPES.COW.DROPS.LEATHER.MAX` | `int` | Any valid integer number | `'2'` | Configures the technical `MAX` parameter for `TYPES.COW.DROPS.LEATHER.MAX` in `spawners.yml`. |
| `TYPES.COW.DROPS.LEATHER.CHANCE` | `float` | Any decimal number | `'0.8'` | Configures the technical `CHANCE` parameter for `TYPES.COW.DROPS.LEATHER.CHANCE` in `spawners.yml`. |
| `TYPES.ZOMBIE.DISPLAY_NAME` | `str` | Any string text | `'&dZombie Spawner'` | Configures the technical `DISPLAY_NAME` parameter for `TYPES.ZOMBIE.DISPLAY_NAME` in `spawners.yml`. |
| `TYPES.ZOMBIE.ENTITY_TYPE` | `str` | Any string text | `'ZOMBIE'` | Configures the technical `ENTITY_TYPE` parameter for `TYPES.ZOMBIE.ENTITY_TYPE` in `spawners.yml`. |
| `TYPES.ZOMBIE.HEAD_TEXTURE` | `str` | Any string text | `''` | Configures the technical `HEAD_TEXTURE` parameter for `TYPES.ZOMBIE.HEAD_TEXTURE` in `spawners.yml`. |
| *(92 additional sub-keys configured in section)* | | | | |

### 3. Practical Setup Example

```yaml
TYPES:
  # Configuration section for Pig.
  PIG:
    # The text or value for Display Name. Available options: Any valid string text
    DISPLAY_NAME: '&dPig Spawner'
    # The text or value for Entity Type. Available options: Any valid string text
    ENTITY_TYPE: PIG
    # Custom head texture URL or Base64 (leave empty to use default mob head).
    HEAD_TEXTURE: 'https://textures.minecraft.net/texture/d875eb45aca34a4d24c3dc1395fc020ccf37f825a17b054a22fd24b189c24c'
    # The text or value for Icon Material. Available options: Any valid string text
    ICON_MATERIAL: PORKCHOP
    # The numerical value for Base Items Per Cycle. Available options: Any valid integer
    BASE_ITEMS_PER_CYCLE: 1
    # Configuration section for Drops.
    DROPS:
      # Configuration section for Porkchop.
      PORKCHOP:
        MATERIAL: PORKCHOP
        # The numerical value for Min. Available options: Any valid integer
        MIN: 1
        # The numerical value for Max. Available options: Any valid integer
        MAX: 3
        # The decimal value for Chance. Available options: Any decimal number
        CHANCE: 1.0
      # Configuration section for Leather.
      LEATHER:
        MATERIAL: LEATHER
        # The numerical value for Min. Available options: Any valid integer
        MIN: 0
        # The numerical value for Max. Available options: Any valid integer
        MAX: 1
        # The decimal value for Chance. Available options: Any decimal number
        CHANCE: 0.35
  # Configuration section for Cow.
  COW:
    # The text or value for Display Name. Available options: Any valid string text
    DISPLAY_NAME: '&dCow Spawner'
    # The text or value for Entity Type. Available options: Any valid string text
    ENTITY_TYPE: COW
    # Custom head texture URL or Base64 (leave empty to use default mob head).
    HEAD_TEXTURE: ''
    # The text or value for Icon Material. Available options: Any valid string text
    ICON_MATERIAL: BEEF
    # The numerical value for Base Items Per Cycle. Available options: Any valid integer
    BASE_ITEMS_PER_CYCLE: 1
    # The decimal value for XP generated per spawner cycle per stack.
    XP_PER_CYCLE: 3.7
    # Configuration section for Drops.
    DROPS:
      # Configuration section for Beef.
      BEEF:
        MATERIAL: BEEF
        # The numerical value for Min. Available options: Any valid integer
        MIN: 1
        # The numerical value for Max. Available options: Any valid integer
        MAX: 3
        # The decimal value for Chance. Available options: Any decimal number
        CHANCE: 1.0
      # Configuration section for Leather.
      LEATHER:
        MATERIAL: LEATHER
        # The numerical value for Min. Available options: Any valid integer
        MIN: 1
        # The numerical value for Max. Available options: Any valid integer
        MAX: 2
        # The decimal value for Chance. Available options: Any decimal number
        CHANCE: 0.8
  # Configuration section for Zombie.
  ZOMBIE:
    # The text or value for Display Name. Available options: Any valid string text
    DISPLAY_NAME: '&dZombie Spawner'
    # The text or value for Entity Type. Available options: Any valid string text
    ENTITY_TYPE: ZOMBIE
    # Custom head texture URL or Base64 (leave empty to use default mob head).
    HEAD_TEXTURE: ''
    # The text or value for Icon Material. Available options: Any valid string text
    ICON_MATERIAL: ROTTEN_FLESH
    # The numerical value for Base Items Per Cycle. Available options: Any valid integer
    BASE_ITEMS_PER_CYCLE: 1
    # Configuration section for Drops.
```

---

