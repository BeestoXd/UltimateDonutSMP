# Detailed Configuration & Setup Guide: `staff-mode.yml`

This is the official, 100% complete technical setup guide for `staff-mode.yml` in **UltimateDonutSMP**.
Each section details the exact commented setup code block, allowed option values, data types, default values, and in-depth functional behavior.

---

## Section: `STAFF-MODE`

### 1. Commented Setup Code Example

```yaml
STAFF-MODE:
  # Determines whether Enabled is enabled or disabled. Available options: true, false
  ENABLED: true
  # Determines whether Auto Vanish On Enable is enabled or disabled. Available options: true, false
  AUTO-VANISH-ON-ENABLE: false
  # Determines whether Persist On Quit is enabled or disabled. Available options: true, false
  PERSIST-ON-QUIT: true
  # Determines whether Persist On Restart is enabled or disabled. Available options: true, false
  PERSIST-ON-RESTART: true
  # Determines whether Lock Tools is enabled or disabled. Available options: true, false
  LOCK-TOOLS: true
  # Determines whether Restore Inventory On Disable is enabled or disabled. Available options: true, false
  RESTORE-INVENTORY-ON-DISABLE: true
  # Configuration section for Vanish Actionbar.
  VANISH-ACTIONBAR:
    # Determines whether Enabled is enabled or disabled. Available options: true, false
    ENABLED: true
    # The numerical value for Interval Ticks. Available options: Any valid integer
    INTERVAL-TICKS: 40
    # The text or value for Message. Available options: Any valid string text
    MESSAGE: '&aVANISHED &7>> &fYou are hidden from regular players'
  # The text or value for Staff Permission. Available options: Any valid string text
  STAFF-PERMISSION: ultimatedonutsmp.staff.mode
  # The text or value for Admin Permission. Available options: Any valid string text
  ADMIN-PERMISSION: ultimatedonutsmp.admin.staffmode
  # The text or value for Vanish Permission. Available options: Any valid string text
  VANISH-PERMISSION: ultimatedonutsmp.staff.mode.vanish
  # The text or value for Better View Permission. Available options: Any valid string text
  BETTER-VIEW-PERMISSION: ultimatedonutsmp.staff.mode.betterview
  # The text or value for Staff List Permission. Available options: Any valid string text
  STAFF-LIST-PERMISSION: ultimatedonutsmp.staff.mode.stafflist
  # The text or value for Random Teleport Permission. Available options: Any valid string text
  RANDOM-TELEPORT-PERMISSION: ultimatedonutsmp.staff.mode.randomtp
  # The text or value for See Vanished Permission. Available options: Any valid string text
  SEE-VANISHED-PERMISSION: ultimatedonutsmp.staff.mode.seevanished
  # The text or value for Others Permission. Available options: Any valid string text
  OTHERS-PERMISSION: ultimatedonutsmp.staff.mode.others
  # Configuration section for Hotbar Slots.
  HOTBAR-SLOTS:
    # The numerical value for Vanish. Available options: Any valid integer
    VANISH: 0
    # The numerical value for Freeze. Available options: Any valid integer
    FREEZE: 1
    # The numerical value for Staff List. Available options: Any valid integer
    STAFF_LIST: 4
    # The numerical value for Better View. Available options: Any valid integer
    BETTER_VIEW: 7
    # The numerical value for Random Teleport. Available options: Any valid integer
    RANDOM_TELEPORT: 8
  # Configuration section for Better View.
  BETTER-VIEW:
    # Determines whether Enable Night Vision is enabled or disabled. Available options: true, false
    ENABLE-NIGHT-VISION: true
    # Determines whether Enable Flight is enabled or disabled. Available options: true, false
    ENABLE-FLIGHT: true
    # Determines whether Auto Fly is enabled or disabled. Available options: true, false
    AUTO-FLY: true
  # Configuration section for Random Teleport.
  RANDOM-TELEPORT:
    # Determines whether Exclude Staff is enabled or disabled. Available options: true, false
    EXCLUDE-STAFF: true
    # Determines whether Exclude Vanished is enabled or disabled. Available options: true, false
    EXCLUDE-VANISHED: true
    # Determines whether Exclude Frozen is enabled or disabled. Available options: true, false
    EXCLUDE-FROZEN: false
    # Determines whether Exclude Duels is enabled or disabled. Available options: true, false
    EXCLUDE-DUELS: true
    # Determines whether Exclude Ffa is enabled or disabled. Available options: true, false
    EXCLUDE-FFA: true
    # Determines whether Notify Target is enabled or disabled. Available options: true, false
    NOTIFY-TARGET: false
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `STAFF-MODE.ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `STAFF-MODE` system. Set to `true` to enable, `false` to disable. |
| `STAFF-MODE.AUTO-VANISH-ON-ENABLE` | `bool` | `true`, `false` | `false` | Configures the technical `AUTO-VANISH-ON-ENABLE` parameter for `STAFF-MODE.AUTO-VANISH-ON-ENABLE` in `staff-mode.yml`. |
| `STAFF-MODE.PERSIST-ON-QUIT` | `bool` | `true`, `false` | `true` | Configures the technical `PERSIST-ON-QUIT` parameter for `STAFF-MODE.PERSIST-ON-QUIT` in `staff-mode.yml`. |
| `STAFF-MODE.PERSIST-ON-RESTART` | `bool` | `true`, `false` | `true` | Configures the technical `PERSIST-ON-RESTART` parameter for `STAFF-MODE.PERSIST-ON-RESTART` in `staff-mode.yml`. |
| `STAFF-MODE.LOCK-TOOLS` | `bool` | `true`, `false` | `true` | Configures the technical `LOCK-TOOLS` parameter for `STAFF-MODE.LOCK-TOOLS` in `staff-mode.yml`. |
| `STAFF-MODE.RESTORE-INVENTORY-ON-DISABLE` | `bool` | `true`, `false` | `true` | Configures the technical `RESTORE-INVENTORY-ON-DISABLE` parameter for `STAFF-MODE.RESTORE-INVENTORY-ON-DISABLE` in `staff-mode.yml`. |
| `STAFF-MODE.VANISH-ACTIONBAR.ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `STAFF-MODE` system. Set to `true` to enable, `false` to disable. |
| `STAFF-MODE.VANISH-ACTIONBAR.INTERVAL-TICKS` | `int` | Any valid integer number | `'40'` | Configures the technical `INTERVAL-TICKS` parameter for `STAFF-MODE.VANISH-ACTIONBAR.INTERVAL-TICKS` in `staff-mode.yml`. |
| `STAFF-MODE.VANISH-ACTIONBAR.MESSAGE` | `str` | Any string text | `'&aVANISHED &7>> &fYou are hidden fr...'` | Configures the technical `MESSAGE` parameter for `STAFF-MODE.VANISH-ACTIONBAR.MESSAGE` in `staff-mode.yml`. |
| `STAFF-MODE.STAFF-PERMISSION` | `str` | Any string text | `'ultimatedonutsmp.staff.mode'` | Configures the technical `STAFF-PERMISSION` parameter for `STAFF-MODE.STAFF-PERMISSION` in `staff-mode.yml`. |
| `STAFF-MODE.ADMIN-PERMISSION` | `str` | Any string text | `'ultimatedonutsmp.admin.staffmode'` | Configures the technical `ADMIN-PERMISSION` parameter for `STAFF-MODE.ADMIN-PERMISSION` in `staff-mode.yml`. |
| `STAFF-MODE.VANISH-PERMISSION` | `str` | Any string text | `'ultimatedonutsmp.staff.mode.vanish'` | Configures the technical `VANISH-PERMISSION` parameter for `STAFF-MODE.VANISH-PERMISSION` in `staff-mode.yml`. |
| `STAFF-MODE.BETTER-VIEW-PERMISSION` | `str` | Any string text | `'ultimatedonutsmp.staff.mode.betterv...'` | Configures the technical `BETTER-VIEW-PERMISSION` parameter for `STAFF-MODE.BETTER-VIEW-PERMISSION` in `staff-mode.yml`. |
| `STAFF-MODE.STAFF-LIST-PERMISSION` | `str` | Any string text | `'ultimatedonutsmp.staff.mode.staffli...'` | Configures the technical `STAFF-LIST-PERMISSION` parameter for `STAFF-MODE.STAFF-LIST-PERMISSION` in `staff-mode.yml`. |
| `STAFF-MODE.RANDOM-TELEPORT-PERMISSION` | `str` | Any string text | `'ultimatedonutsmp.staff.mode.randomt...'` | Configures the technical `RANDOM-TELEPORT-PERMISSION` parameter for `STAFF-MODE.RANDOM-TELEPORT-PERMISSION` in `staff-mode.yml`. |
| `STAFF-MODE.SEE-VANISHED-PERMISSION` | `str` | Any string text | `'ultimatedonutsmp.staff.mode.seevani...'` | Configures the technical `SEE-VANISHED-PERMISSION` parameter for `STAFF-MODE.SEE-VANISHED-PERMISSION` in `staff-mode.yml`. |
| `STAFF-MODE.OTHERS-PERMISSION` | `str` | Any string text | `'ultimatedonutsmp.staff.mode.others'` | Configures the technical `OTHERS-PERMISSION` parameter for `STAFF-MODE.OTHERS-PERMISSION` in `staff-mode.yml`. |
| `STAFF-MODE.HOTBAR-SLOTS.VANISH` | `int` | Any valid integer number | `'0'` | Configures the technical `VANISH` parameter for `STAFF-MODE.HOTBAR-SLOTS.VANISH` in `staff-mode.yml`. |
| `STAFF-MODE.HOTBAR-SLOTS.FREEZE` | `int` | Any valid integer number | `'1'` | Configures the technical `FREEZE` parameter for `STAFF-MODE.HOTBAR-SLOTS.FREEZE` in `staff-mode.yml`. |
| `STAFF-MODE.HOTBAR-SLOTS.STAFF_LIST` | `int` | Any valid integer number | `'4'` | Configures the technical `STAFF_LIST` parameter for `STAFF-MODE.HOTBAR-SLOTS.STAFF_LIST` in `staff-mode.yml`. |
| `STAFF-MODE.HOTBAR-SLOTS.BETTER_VIEW` | `int` | Any valid integer number | `'7'` | Configures the technical `BETTER_VIEW` parameter for `STAFF-MODE.HOTBAR-SLOTS.BETTER_VIEW` in `staff-mode.yml`. |
| `STAFF-MODE.HOTBAR-SLOTS.RANDOM_TELEPORT` | `int` | Any valid integer number | `'8'` | Configures the technical `RANDOM_TELEPORT` parameter for `STAFF-MODE.HOTBAR-SLOTS.RANDOM_TELEPORT` in `staff-mode.yml`. |
| `STAFF-MODE.BETTER-VIEW.ENABLE-NIGHT-VISION` | `bool` | `true`, `false` | `true` | Configures the technical `ENABLE-NIGHT-VISION` parameter for `STAFF-MODE.BETTER-VIEW.ENABLE-NIGHT-VISION` in `staff-mode.yml`. |
| `STAFF-MODE.BETTER-VIEW.ENABLE-FLIGHT` | `bool` | `true`, `false` | `true` | Configures the technical `ENABLE-FLIGHT` parameter for `STAFF-MODE.BETTER-VIEW.ENABLE-FLIGHT` in `staff-mode.yml`. |
| `STAFF-MODE.BETTER-VIEW.AUTO-FLY` | `bool` | `true`, `false` | `true` | Configures the technical `AUTO-FLY` parameter for `STAFF-MODE.BETTER-VIEW.AUTO-FLY` in `staff-mode.yml`. |
| `STAFF-MODE.RANDOM-TELEPORT.EXCLUDE-STAFF` | `bool` | `true`, `false` | `true` | Configures the technical `EXCLUDE-STAFF` parameter for `STAFF-MODE.RANDOM-TELEPORT.EXCLUDE-STAFF` in `staff-mode.yml`. |
| `STAFF-MODE.RANDOM-TELEPORT.EXCLUDE-VANISHED` | `bool` | `true`, `false` | `true` | Configures the technical `EXCLUDE-VANISHED` parameter for `STAFF-MODE.RANDOM-TELEPORT.EXCLUDE-VANISHED` in `staff-mode.yml`. |
| `STAFF-MODE.RANDOM-TELEPORT.EXCLUDE-FROZEN` | `bool` | `true`, `false` | `false` | Configures the technical `EXCLUDE-FROZEN` parameter for `STAFF-MODE.RANDOM-TELEPORT.EXCLUDE-FROZEN` in `staff-mode.yml`. |
| `STAFF-MODE.RANDOM-TELEPORT.EXCLUDE-DUELS` | `bool` | `true`, `false` | `true` | Configures the technical `EXCLUDE-DUELS` parameter for `STAFF-MODE.RANDOM-TELEPORT.EXCLUDE-DUELS` in `staff-mode.yml`. |
| `STAFF-MODE.RANDOM-TELEPORT.EXCLUDE-FFA` | `bool` | `true`, `false` | `true` | Configures the technical `EXCLUDE-FFA` parameter for `STAFF-MODE.RANDOM-TELEPORT.EXCLUDE-FFA` in `staff-mode.yml`. |
| *(1 additional sub-keys configured in section)* | | | | |

### 3. Practical Setup Example

```yaml
STAFF-MODE:
  # Determines whether Enabled is enabled or disabled. Available options: true, false
  ENABLED: true
  # Determines whether Auto Vanish On Enable is enabled or disabled. Available options: true, false
  AUTO-VANISH-ON-ENABLE: false
  # Determines whether Persist On Quit is enabled or disabled. Available options: true, false
  PERSIST-ON-QUIT: true
  # Determines whether Persist On Restart is enabled or disabled. Available options: true, false
  PERSIST-ON-RESTART: true
  # Determines whether Lock Tools is enabled or disabled. Available options: true, false
  LOCK-TOOLS: true
  # Determines whether Restore Inventory On Disable is enabled or disabled. Available options: true, false
  RESTORE-INVENTORY-ON-DISABLE: true
  # Configuration section for Vanish Actionbar.
  VANISH-ACTIONBAR:
    # Determines whether Enabled is enabled or disabled. Available options: true, false
    ENABLED: true
    # The numerical value for Interval Ticks. Available options: Any valid integer
    
```

---

## Section: `ITEMS`

### 1. Commented Setup Code Example

```yaml
ITEMS:
  # Configuration section for Vanish.
  VANISH:
    # Configuration section for Enabled.
    ENABLED:
      MATERIAL: LIME_DYE
      NAME: '&aVanished'
      LORE:
      - '&7Click to disable vanish'
    # Configuration section for Disabled.
    DISABLED:
      MATERIAL: GRAY_DYE
      NAME: '&7Unvanished'
      LORE:
      - '&7Click to enable vanish'
  # Configuration section for Freeze.
  FREEZE:
    MATERIAL: ICE
    NAME: '&bFreeze Player'
    LORE:
    - '&7Right-click a player to freeze them'
    - '&7Left-click to see frozen players'
  # Configuration section for Staff List.
  STAFF_LIST:
    MATERIAL: CLOCK
    NAME: '&eStaff List'
    LORE:
    - '&7Click to view online staff members'
  # Configuration section for Better View.
  BETTER_VIEW:
    MATERIAL: ORANGE_CARPET
    NAME: '&eBetter View'
    LORE:
    - '&7Click to toggle better view mode'
  # Configuration section for Random Teleport.
  RANDOM_TELEPORT:
    MATERIAL: PLAYER_HEAD
    NAME: '&eRandom Teleport'
    LORE:
    - '&7Click to teleport to a random player'
# Configuration section for Menus.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `ITEMS.VANISH.ENABLED.MATERIAL` | `str` | Any string text | `'LIME_DYE'` | Configures the technical `MATERIAL` parameter for `ITEMS.VANISH.ENABLED.MATERIAL` in `staff-mode.yml`. |
| `ITEMS.VANISH.ENABLED.NAME` | `str` | Any string text | `'&aVanished'` | Configures the technical `NAME` parameter for `ITEMS.VANISH.ENABLED.NAME` in `staff-mode.yml`. |
| `ITEMS.VANISH.ENABLED.LORE` | `list` | List of configured items/strings | `['&7Click to disable vanish']` | Configures the technical `LORE` parameter for `ITEMS.VANISH.ENABLED.LORE` in `staff-mode.yml`. |
| `ITEMS.VANISH.DISABLED.MATERIAL` | `str` | Any string text | `'GRAY_DYE'` | Configures the technical `MATERIAL` parameter for `ITEMS.VANISH.DISABLED.MATERIAL` in `staff-mode.yml`. |
| `ITEMS.VANISH.DISABLED.NAME` | `str` | Any string text | `'&7Unvanished'` | Configures the technical `NAME` parameter for `ITEMS.VANISH.DISABLED.NAME` in `staff-mode.yml`. |
| `ITEMS.VANISH.DISABLED.LORE` | `list` | List of configured items/strings | `['&7Click to enable vanish']` | Configures the technical `LORE` parameter for `ITEMS.VANISH.DISABLED.LORE` in `staff-mode.yml`. |
| `ITEMS.FREEZE.MATERIAL` | `str` | Any string text | `'ICE'` | Configures the technical `MATERIAL` parameter for `ITEMS.FREEZE.MATERIAL` in `staff-mode.yml`. |
| `ITEMS.FREEZE.NAME` | `str` | Any string text | `'&bFreeze Player'` | Configures the technical `NAME` parameter for `ITEMS.FREEZE.NAME` in `staff-mode.yml`. |
| `ITEMS.FREEZE.LORE` | `list` | List of configured items/strings | `['&7Right-click a player to freeze them', '&7Left-click to see frozen players']` | Configures the technical `LORE` parameter for `ITEMS.FREEZE.LORE` in `staff-mode.yml`. |
| `ITEMS.STAFF_LIST.MATERIAL` | `str` | Any string text | `'CLOCK'` | Configures the technical `MATERIAL` parameter for `ITEMS.STAFF_LIST.MATERIAL` in `staff-mode.yml`. |
| `ITEMS.STAFF_LIST.NAME` | `str` | Any string text | `'&eStaff List'` | Configures the technical `NAME` parameter for `ITEMS.STAFF_LIST.NAME` in `staff-mode.yml`. |
| `ITEMS.STAFF_LIST.LORE` | `list` | List of configured items/strings | `['&7Click to view online staff members']` | Configures the technical `LORE` parameter for `ITEMS.STAFF_LIST.LORE` in `staff-mode.yml`. |
| `ITEMS.BETTER_VIEW.MATERIAL` | `str` | Any string text | `'ORANGE_CARPET'` | Configures the technical `MATERIAL` parameter for `ITEMS.BETTER_VIEW.MATERIAL` in `staff-mode.yml`. |
| `ITEMS.BETTER_VIEW.NAME` | `str` | Any string text | `'&eBetter View'` | Configures the technical `NAME` parameter for `ITEMS.BETTER_VIEW.NAME` in `staff-mode.yml`. |
| `ITEMS.BETTER_VIEW.LORE` | `list` | List of configured items/strings | `['&7Click to toggle better view mode']` | Configures the technical `LORE` parameter for `ITEMS.BETTER_VIEW.LORE` in `staff-mode.yml`. |
| `ITEMS.RANDOM_TELEPORT.MATERIAL` | `str` | Any string text | `'PLAYER_HEAD'` | Configures the technical `MATERIAL` parameter for `ITEMS.RANDOM_TELEPORT.MATERIAL` in `staff-mode.yml`. |
| `ITEMS.RANDOM_TELEPORT.NAME` | `str` | Any string text | `'&eRandom Teleport'` | Configures the technical `NAME` parameter for `ITEMS.RANDOM_TELEPORT.NAME` in `staff-mode.yml`. |
| `ITEMS.RANDOM_TELEPORT.LORE` | `list` | List of configured items/strings | `['&7Click to teleport to a random player']` | Configures the technical `LORE` parameter for `ITEMS.RANDOM_TELEPORT.LORE` in `staff-mode.yml`. |

### 3. Practical Setup Example

```yaml
ITEMS:
  # Configuration section for Vanish.
  VANISH:
    # Configuration section for Enabled.
    ENABLED:
      MATERIAL: LIME_DYE
      NAME: '&aVanished'
      LORE:
      - '&7Click to disable vanish'
    # Configuration section for Disabled.
    DISABLED:
      MATERIAL: GRAY_DYE
      NAME: '&7Unvanished'
      LORE:
      - '&7Click to enable vanish'
  # Configuration section for Freeze.
  FREEZE:
    MATERIAL: ICE
    NAME: '&bFreeze Player'
    LORE:
    - '&7Right-click a player to freeze them'
    - '&7Left-click to see frozen players'
  # Configuration section for Staff List.
  STAFF_LIST:
    MATERIAL: CLOCK
    NAME: '&eStaff List'
    LORE:
    - '&7Click to view online staff members'
  # Configuration section for Better View.
  BETTER_VIEW:
    MATERIAL: ORANGE_CARPET
    NAME: '&eBetter View'
    LORE:
    - '&7Click to toggle better view mode'
  # Configuration section for Random Teleport.
  RANDOM_TELEPORT:
    MATERIAL: PLAYER_HEAD
    NAME: '&eRandom Teleport'
  
```

---

## Section: `MENUS`

### 1. Commented Setup Code Example

```yaml
MENUS:
  # Configuration section for Staff List.
  STAFF-LIST:
    TITLE: '&8Online Staff'
    SIZE: 54
    # The numerical value for Refresh Slot. Available options: Any valid integer
    REFRESH-SLOT: 49
    # The text or value for Placeholder Material. Available options: Any valid string text
    PLACEHOLDER-MATERIAL: GRAY_STAINED_GLASS_PANE
    # Configuration section for Content Slots.
    CONTENT-SLOTS:
    - 10
    - 11
    - 12
    - 13
    - 14
    - 15
    - 16
    - 19
    - 20
    - 21
    - 22
    - 23
    - 24
    - 25
    - 28
    - 29
    - 30
    - 31
    - 32
    - 33
    - 34
    # The text or value for Empty Material. Available options: Any valid string text
    EMPTY-MATERIAL: BARRIER
    # The text or value for Empty Name. Available options: Any valid string text
    EMPTY-NAME: '&cNo staff online'
    # Configuration section for Empty Lore.
    EMPTY-LORE:
    - '&7No staff members are currently online.'
  # Configuration section for Frozen Players.
  FROZEN-PLAYERS:
    TITLE: '&8Frozen Players'
    SIZE: 54
    # The numerical value for Refresh Slot. Available options: Any valid integer
    REFRESH-SLOT: 49
    # The text or value for Placeholder Material. Available options: Any valid string text
    PLACEHOLDER-MATERIAL: LIGHT_BLUE_STAINED_GLASS_PANE
    # Configuration section for Content Slots.
    CONTENT-SLOTS:
    - 10
    - 11
    - 12
    - 13
    - 14
    - 15
    - 16
    - 19
    - 20
    - 21
    - 22
    - 23
    - 24
    - 25
    - 28
    - 29
    - 30
    - 31
    - 32
    - 33
    - 34
    # The text or value for Empty Material. Available options: Any valid string text
    EMPTY-MATERIAL: BARRIER
    # The text or value for Empty Name. Available options: Any valid string text
    EMPTY-NAME: '&aNo frozen players'
    # Configuration section for Empty Lore.
    EMPTY-LORE:
    - '&7There are no active frozen players.'
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `MENUS.STAFF-LIST.TITLE` | `str` | Any string text | `'&8Online Staff'` | Configures the technical `TITLE` parameter for `MENUS.STAFF-LIST.TITLE` in `staff-mode.yml`. |
| `MENUS.STAFF-LIST.SIZE` | `int` | Any valid integer number | `'54'` | Configures the technical `SIZE` parameter for `MENUS.STAFF-LIST.SIZE` in `staff-mode.yml`. |
| `MENUS.STAFF-LIST.REFRESH-SLOT` | `int` | Any valid integer number | `'49'` | Configures the technical `REFRESH-SLOT` parameter for `MENUS.STAFF-LIST.REFRESH-SLOT` in `staff-mode.yml`. |
| `MENUS.STAFF-LIST.PLACEHOLDER-MATERIAL` | `str` | Any string text | `'GRAY_STAINED_GLASS_PANE'` | Configures the technical `PLACEHOLDER-MATERIAL` parameter for `MENUS.STAFF-LIST.PLACEHOLDER-MATERIAL` in `staff-mode.yml`. |
| `MENUS.STAFF-LIST.CONTENT-SLOTS` | `list` | List of configured items/strings | `[10, 11, 12...]` | Configures the technical `CONTENT-SLOTS` parameter for `MENUS.STAFF-LIST.CONTENT-SLOTS` in `staff-mode.yml`. |
| `MENUS.STAFF-LIST.EMPTY-MATERIAL` | `str` | Any string text | `'BARRIER'` | Configures the technical `EMPTY-MATERIAL` parameter for `MENUS.STAFF-LIST.EMPTY-MATERIAL` in `staff-mode.yml`. |
| `MENUS.STAFF-LIST.EMPTY-NAME` | `str` | Any string text | `'&cNo staff online'` | Configures the technical `EMPTY-NAME` parameter for `MENUS.STAFF-LIST.EMPTY-NAME` in `staff-mode.yml`. |
| `MENUS.STAFF-LIST.EMPTY-LORE` | `list` | List of configured items/strings | `['&7No staff members are currently online.']` | Configures the technical `EMPTY-LORE` parameter for `MENUS.STAFF-LIST.EMPTY-LORE` in `staff-mode.yml`. |
| `MENUS.FROZEN-PLAYERS.TITLE` | `str` | Any string text | `'&8Frozen Players'` | Configures the technical `TITLE` parameter for `MENUS.FROZEN-PLAYERS.TITLE` in `staff-mode.yml`. |
| `MENUS.FROZEN-PLAYERS.SIZE` | `int` | Any valid integer number | `'54'` | Configures the technical `SIZE` parameter for `MENUS.FROZEN-PLAYERS.SIZE` in `staff-mode.yml`. |
| `MENUS.FROZEN-PLAYERS.REFRESH-SLOT` | `int` | Any valid integer number | `'49'` | Configures the technical `REFRESH-SLOT` parameter for `MENUS.FROZEN-PLAYERS.REFRESH-SLOT` in `staff-mode.yml`. |
| `MENUS.FROZEN-PLAYERS.PLACEHOLDER-MATERIAL` | `str` | Any string text | `'LIGHT_BLUE_STAINED_GLASS_PANE'` | Configures the technical `PLACEHOLDER-MATERIAL` parameter for `MENUS.FROZEN-PLAYERS.PLACEHOLDER-MATERIAL` in `staff-mode.yml`. |
| `MENUS.FROZEN-PLAYERS.CONTENT-SLOTS` | `list` | List of configured items/strings | `[10, 11, 12...]` | Configures the technical `CONTENT-SLOTS` parameter for `MENUS.FROZEN-PLAYERS.CONTENT-SLOTS` in `staff-mode.yml`. |
| `MENUS.FROZEN-PLAYERS.EMPTY-MATERIAL` | `str` | Any string text | `'BARRIER'` | Configures the technical `EMPTY-MATERIAL` parameter for `MENUS.FROZEN-PLAYERS.EMPTY-MATERIAL` in `staff-mode.yml`. |
| `MENUS.FROZEN-PLAYERS.EMPTY-NAME` | `str` | Any string text | `'&aNo frozen players'` | Configures the technical `EMPTY-NAME` parameter for `MENUS.FROZEN-PLAYERS.EMPTY-NAME` in `staff-mode.yml`. |
| `MENUS.FROZEN-PLAYERS.EMPTY-LORE` | `list` | List of configured items/strings | `['&7There are no active frozen players.']` | Configures the technical `EMPTY-LORE` parameter for `MENUS.FROZEN-PLAYERS.EMPTY-LORE` in `staff-mode.yml`. |

### 3. Practical Setup Example

```yaml
MENUS:
  # Configuration section for Staff List.
  STAFF-LIST:
    TITLE: '&8Online Staff'
    SIZE: 54
    # The numerical value for Refresh Slot. Available options: Any valid integer
    REFRESH-SLOT: 49
    # The text or value for Placeholder Material. Available options: Any valid string text
    PLACEHOLDER-MATERIAL: GRAY_STAINED_GLASS_PANE
    # Configuration section for Content Slots.
    CONTENT-SLOTS:
    - 10
    - 11
    - 12
    - 13
    - 14
    - 15
    - 16
    - 19
    - 20
    - 21
    - 22
    - 23
    - 24
    - 25
    - 28
    - 29
    - 30
    - 31
    - 32
    - 33
    - 34
    # The text or value for Empty Material. Available options: Any valid string text
    EMPTY-MATERIAL: BARRIER
    # The text or value for Empty Name. Available options: Any valid string text
    EMPTY-NAME: '&cNo staff online'
    # Configuration section for Empty Lore.
    EMPTY-LORE:
    - '&7No staff members are currently online.'
  # Configuration section for Frozen Players.
  FROZEN-PLAY
```

---

## Section: `MESSAGES`

### 1. Commented Setup Code Example

```yaml
MESSAGES:
  # The text or value for No Permission. Available options: Any valid string text
  NO-PERMISSION: '&cYou do not have permission.'
  # The text or value for Feature Disabled. Available options: Any valid string text
  FEATURE-DISABLED: '&cStaff mode is currently disabled.'
  # The text or value for Player Only. Available options: Any valid string text
  PLAYER-ONLY: '&cOnly players can use this command.'
  # The text or value for Enabled. Available options: Any valid string text
  ENABLED: '&aStaff mode enabled.'
  # The text or value for Disabled. Available options: Any valid string text
  DISABLED: '&cStaff mode disabled.'
  # The text or value for Enabled Actionbar. Available options: Any valid string text
  ENABLED-ACTIONBAR: '&aStaff mode enabled'
  # The text or value for Disabled Actionbar. Available options: Any valid string text
  DISABLED-ACTIONBAR: '&cStaff mode disabled'
  # The text or value for Vanish On. Available options: Any valid string text
  VANISH-ON: '&aVanish enabled.'
  # The text or value for Vanish Off. Available options: Any valid string text
  VANISH-OFF: '&cVanish disabled.'
  # The text or value for Better View On. Available options: Any valid string text
  BETTER-VIEW-ON: '&aBetter View enabled.'
  # The text or value for Better View Off. Available options: Any valid string text
  BETTER-VIEW-OFF: '&cBetter View disabled.'
  # The text or value for Random Teleport No Target. Available options: Any valid string text
  RANDOM-TELEPORT-NO-TARGET: '&cNo eligible player found.'
  # The text or value for Random Teleport Success. Available options: Any valid string text
  RANDOM-TELEPORT-SUCCESS: '&eTeleported to &f{player}&e.'
  # The text or value for Restore Failed. Available options: Any valid string text
  RESTORE-FAILED: '&cStaff mode restore failed. Contact an admin.'
  # The text or value for Recovered After Restart. Available options: Any valid string text
  RECOVERED-AFTER-RESTART: '&eStaff mode was disabled because the server restarted.
    Your inventory was restored.'
  # The text or value for Tool Locked. Available options: Any valid string text
  TOOL-LOCKED: '&cYour staff tools are locked while Staff Mode is active.'
  # The text or value for Reload Success. Available options: Any valid string text
  RELOAD-SUCCESS: '&aStaff mode config reloaded.'
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `MESSAGES.NO-PERMISSION` | `str` | Any string text | `'&cYou do not have permission.'` | Configures the technical `NO-PERMISSION` parameter for `MESSAGES.NO-PERMISSION` in `staff-mode.yml`. |
| `MESSAGES.FEATURE-DISABLED` | `str` | Any string text | `'&cStaff mode is currently disabled.'` | Configures the technical `FEATURE-DISABLED` parameter for `MESSAGES.FEATURE-DISABLED` in `staff-mode.yml`. |
| `MESSAGES.PLAYER-ONLY` | `str` | Any string text | `'&cOnly players can use this command...'` | Configures the technical `PLAYER-ONLY` parameter for `MESSAGES.PLAYER-ONLY` in `staff-mode.yml`. |
| `MESSAGES.ENABLED` | `str` | Any string text | `'&aStaff mode enabled.'` | Global toggle for `MESSAGES` system. Set to `true` to enable, `false` to disable. |
| `MESSAGES.DISABLED` | `str` | Any string text | `'&cStaff mode disabled.'` | Configures the technical `DISABLED` parameter for `MESSAGES.DISABLED` in `staff-mode.yml`. |
| `MESSAGES.ENABLED-ACTIONBAR` | `str` | Any string text | `'&aStaff mode enabled'` | Configures the technical `ENABLED-ACTIONBAR` parameter for `MESSAGES.ENABLED-ACTIONBAR` in `staff-mode.yml`. |
| `MESSAGES.DISABLED-ACTIONBAR` | `str` | Any string text | `'&cStaff mode disabled'` | Configures the technical `DISABLED-ACTIONBAR` parameter for `MESSAGES.DISABLED-ACTIONBAR` in `staff-mode.yml`. |
| `MESSAGES.VANISH-ON` | `str` | Any string text | `'&aVanish enabled.'` | Configures the technical `VANISH-ON` parameter for `MESSAGES.VANISH-ON` in `staff-mode.yml`. |
| `MESSAGES.VANISH-OFF` | `str` | Any string text | `'&cVanish disabled.'` | Configures the technical `VANISH-OFF` parameter for `MESSAGES.VANISH-OFF` in `staff-mode.yml`. |
| `MESSAGES.BETTER-VIEW-ON` | `str` | Any string text | `'&aBetter View enabled.'` | Configures the technical `BETTER-VIEW-ON` parameter for `MESSAGES.BETTER-VIEW-ON` in `staff-mode.yml`. |
| `MESSAGES.BETTER-VIEW-OFF` | `str` | Any string text | `'&cBetter View disabled.'` | Configures the technical `BETTER-VIEW-OFF` parameter for `MESSAGES.BETTER-VIEW-OFF` in `staff-mode.yml`. |
| `MESSAGES.RANDOM-TELEPORT-NO-TARGET` | `str` | Any string text | `'&cNo eligible player found.'` | Configures the technical `RANDOM-TELEPORT-NO-TARGET` parameter for `MESSAGES.RANDOM-TELEPORT-NO-TARGET` in `staff-mode.yml`. |
| `MESSAGES.RANDOM-TELEPORT-SUCCESS` | `str` | Any string text | `'&eTeleported to &f{player}&e.'` | Configures the technical `RANDOM-TELEPORT-SUCCESS` parameter for `MESSAGES.RANDOM-TELEPORT-SUCCESS` in `staff-mode.yml`. |
| `MESSAGES.RESTORE-FAILED` | `str` | Any string text | `'&cStaff mode restore failed. Contac...'` | Configures the technical `RESTORE-FAILED` parameter for `MESSAGES.RESTORE-FAILED` in `staff-mode.yml`. |
| `MESSAGES.RECOVERED-AFTER-RESTART` | `str` | Any string text | `'&eStaff mode was disabled because t...'` | Configures the technical `RECOVERED-AFTER-RESTART` parameter for `MESSAGES.RECOVERED-AFTER-RESTART` in `staff-mode.yml`. |
| `MESSAGES.TOOL-LOCKED` | `str` | Any string text | `'&cYour staff tools are locked while...'` | Configures the technical `TOOL-LOCKED` parameter for `MESSAGES.TOOL-LOCKED` in `staff-mode.yml`. |
| `MESSAGES.RELOAD-SUCCESS` | `str` | Any string text | `'&aStaff mode config reloaded.'` | Configures the technical `RELOAD-SUCCESS` parameter for `MESSAGES.RELOAD-SUCCESS` in `staff-mode.yml`. |

### 3. Practical Setup Example

```yaml
MESSAGES:
  # The text or value for No Permission. Available options: Any valid string text
  NO-PERMISSION: '&cYou do not have permission.'
  # The text or value for Feature Disabled. Available options: Any valid string text
  FEATURE-DISABLED: '&cStaff mode is currently disabled.'
  # The text or value for Player Only. Available options: Any valid string text
  PLAYER-ONLY: '&cOnly players can use this command.'
  # The text or value for Enabled. Available options: Any valid string text
  ENABLED: '&aStaff mode enabled.'
  # The text or value for Disabled. Available options: Any valid string text
  DISABLED: '&cStaff mode disabled.'
  # The text or value for Enabled Actionbar. Available options: Any valid string text
  ENABLED-ACTIONBAR: '&aStaff mode enabled'
  # The text or value for Disabled Actionbar. Available options: Any valid string text
  DISABLED-ACTIONBAR: '&cStaff mode disabled'
  # The text or value for Vanish On. Available options: Any valid string text
  VANISH-ON: '&a
```

---

