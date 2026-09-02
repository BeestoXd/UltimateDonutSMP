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
# Configuration section for Anti Esp.
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
# Configuration section for Anti Esp.
```

---

## Section: `ANTI_ESP`

### 1. Commented Setup Code Example

```yaml
ANTI_ESP:
  # Determines whether Enabled is enabled or disabled. Available options: true, false
  ENABLED: true
  # The numerical value for Reveal Radius. Available options: Any valid integer
  REVEAL_RADIUS: 7
  # The numerical value for Owner See Radius. Available options: Any valid integer
  OWNER_SEE_RADIUS: 9
  # The numerical value for Tracking Radius. Available options: Any valid integer
  TRACKING_RADIUS: 128
  # Determines whether Require Line Of Sight is enabled or disabled. Available options: true, false
  REQUIRE_LINE_OF_SIGHT: true
  # The text or value for Staff Bypass Permission. Available options: Any valid string text
  STAFF_BYPASS_PERMISSION: ultimatedonutsmp.admin.spawner.seeall
  # Configuration section for Camouflage.
  CAMOUFLAGE:
    # The text or value for Overworld. Available options: Any valid string text
    OVERWORLD: DEEPSLATE
    # The text or value for Nether. Available options: Any valid string text
    NETHER: NETHERRACK
    # The text or value for The End. Available options: Any valid string text
    THE_END: END_STONE
# Configuration section for Gui.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `ANTI_ESP.ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `ANTI_ESP` system. Set to `true` to enable, `false` to disable. |
| `ANTI_ESP.REVEAL_RADIUS` | `int` | Any valid integer number | `'7'` | Configures the technical `REVEAL_RADIUS` parameter for `ANTI_ESP.REVEAL_RADIUS` in `spawners.yml`. |
| `ANTI_ESP.OWNER_SEE_RADIUS` | `int` | Any valid integer number | `'9'` | Configures the technical `OWNER_SEE_RADIUS` parameter for `ANTI_ESP.OWNER_SEE_RADIUS` in `spawners.yml`. |
| `ANTI_ESP.TRACKING_RADIUS` | `int` | Any valid integer number | `'128'` | Configures the technical `TRACKING_RADIUS` parameter for `ANTI_ESP.TRACKING_RADIUS` in `spawners.yml`. |
| `ANTI_ESP.REQUIRE_LINE_OF_SIGHT` | `bool` | `true`, `false` | `true` | Configures the technical `REQUIRE_LINE_OF_SIGHT` parameter for `ANTI_ESP.REQUIRE_LINE_OF_SIGHT` in `spawners.yml`. |
| `ANTI_ESP.STAFF_BYPASS_PERMISSION` | `str` | Any string text | `'ultimatedonutsmp.admin.spawner.seea...'` | Configures the technical `STAFF_BYPASS_PERMISSION` parameter for `ANTI_ESP.STAFF_BYPASS_PERMISSION` in `spawners.yml`. |
| `ANTI_ESP.CAMOUFLAGE.OVERWORLD` | `str` | Any string text | `'DEEPSLATE'` | Configures the technical `OVERWORLD` parameter for `ANTI_ESP.CAMOUFLAGE.OVERWORLD` in `spawners.yml`. |
| `ANTI_ESP.CAMOUFLAGE.NETHER` | `str` | Any string text | `'NETHERRACK'` | Configures the technical `NETHER` parameter for `ANTI_ESP.CAMOUFLAGE.NETHER` in `spawners.yml`. |
| `ANTI_ESP.CAMOUFLAGE.THE_END` | `str` | Any string text | `'END_STONE'` | Configures the technical `THE_END` parameter for `ANTI_ESP.CAMOUFLAGE.THE_END` in `spawners.yml`. |

### 3. Practical Setup Example

```yaml
ANTI_ESP:
  # Determines whether Enabled is enabled or disabled. Available options: true, false
  ENABLED: true
  # The numerical value for Reveal Radius. Available options: Any valid integer
  REVEAL_RADIUS: 7
  # The numerical value for Owner See Radius. Available options: Any valid integer
  OWNER_SEE_RADIUS: 9
  # The numerical value for Tracking Radius. Available options: Any valid integer
  TRACKING_RADIUS: 128
  # Determines whether Require Line Of Sight is enabled or disabled. Available options: true, false
  REQUIRE_LINE_OF_SIGHT: true
  # The text or value for Staff Bypass Permission. Available options: Any valid string text
  STAFF_BYPASS_PERMISSION: ultimatedonutsmp.admin.spawner.seeall
  # Configuration section for Camouflage.
  CAMOUFLAGE:
    # The text or value for Overworld. Available options: Any valid string text
    OVERWORLD: DEEPSLATE
    # The text or value for Nether. Available options: Any valid string text
    NETHER: NETHERRACK
    # The text or value for The End. Available options: Any valid string text
    THE_END: END_STONE
# Configuration section for Gui.
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
# Configuration section for Types.
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

