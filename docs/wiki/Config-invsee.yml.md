# Detailed Configuration & Setup Guide: `invsee.yml`

This is the official, 100% complete technical setup guide for `invsee.yml` in **UltimateDonutSMP**.
Each section details the exact commented setup code block, allowed option values, data types, default values, and in-depth functional behavior.

---

## Section: `INVSEE`

### 1. Commented Setup Code Example

```yaml
INVSEE:
  # Enable or disable the invsee system globally (true / false)
  ENABLED: true

  # Title header displayed on the invsee GUI ({player} placeholder supported)
  TITLE: '&8Inventory of {player}'

  # Require target player to be online to open invsee (true / false)
  REQUIRE-ONLINE: true

  # Allow staff members with modify permission to edit target inventory items (true / false)
  ALLOW-EDIT: false

  # Allow staff members to view their own inventory via /invsee (true / false)
  ALLOW-SELF-VIEW: false

  # Notify target player when staff opens their inventory (true / false)
  NOTIFY-TARGET: false

  # Log invsee usage and item modifications to console/audit log (true / false)
  LOG-USAGE: true

  # Freeze inventory GUI into a snapshot if target logs out while being viewed (true / false)
  FREEZE-ON-LOGOUT: true

  # Auto-refresh interval in ticks to synchronize target inventory updates (20 ticks = 1s)
  AUTO-REFRESH-TICKS: 10

  # Permission node to view target player inventory
  VIEW-PERMISSION: ultimatedonutsmp.staff.invsee

  # Permission node to modify items in target player inventory
  MODIFY-PERMISSION: ultimatedonutsmp.staff.invsee.modify

  # Permission node for admin invsee commands
  ADMIN-PERMISSION: ultimatedonutsmp.admin.invsee

# Inventory GUI layout slot mapping
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `INVSEE.ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `INVSEE` system. Set to `true` to enable, `false` to disable. |
| `INVSEE.TITLE` | `str` | Any string text | `'&8Inventory of {player}'` | Configures the technical `TITLE` parameter for `INVSEE.TITLE` in `invsee.yml`. |
| `INVSEE.REQUIRE-ONLINE` | `bool` | `true`, `false` | `true` | Configures the technical `REQUIRE-ONLINE` parameter for `INVSEE.REQUIRE-ONLINE` in `invsee.yml`. |
| `INVSEE.ALLOW-EDIT` | `bool` | `true`, `false` | `false` | Configures the technical `ALLOW-EDIT` parameter for `INVSEE.ALLOW-EDIT` in `invsee.yml`. |
| `INVSEE.ALLOW-SELF-VIEW` | `bool` | `true`, `false` | `false` | Configures the technical `ALLOW-SELF-VIEW` parameter for `INVSEE.ALLOW-SELF-VIEW` in `invsee.yml`. |
| `INVSEE.NOTIFY-TARGET` | `bool` | `true`, `false` | `false` | Configures the technical `NOTIFY-TARGET` parameter for `INVSEE.NOTIFY-TARGET` in `invsee.yml`. |
| `INVSEE.LOG-USAGE` | `bool` | `true`, `false` | `true` | Configures the technical `LOG-USAGE` parameter for `INVSEE.LOG-USAGE` in `invsee.yml`. |
| `INVSEE.FREEZE-ON-LOGOUT` | `bool` | `true`, `false` | `true` | Configures the technical `FREEZE-ON-LOGOUT` parameter for `INVSEE.FREEZE-ON-LOGOUT` in `invsee.yml`. |
| `INVSEE.AUTO-REFRESH-TICKS` | `int` | Any valid integer number | `'10'` | Configures the technical `AUTO-REFRESH-TICKS` parameter for `INVSEE.AUTO-REFRESH-TICKS` in `invsee.yml`. |
| `INVSEE.VIEW-PERMISSION` | `str` | Any string text | `'ultimatedonutsmp.staff.invsee'` | Configures the technical `VIEW-PERMISSION` parameter for `INVSEE.VIEW-PERMISSION` in `invsee.yml`. |
| `INVSEE.MODIFY-PERMISSION` | `str` | Any string text | `'ultimatedonutsmp.staff.invsee.modif...'` | Configures the technical `MODIFY-PERMISSION` parameter for `INVSEE.MODIFY-PERMISSION` in `invsee.yml`. |
| `INVSEE.ADMIN-PERMISSION` | `str` | Any string text | `'ultimatedonutsmp.admin.invsee'` | Configures the technical `ADMIN-PERMISSION` parameter for `INVSEE.ADMIN-PERMISSION` in `invsee.yml`. |

### 3. Practical Setup Example

```yaml
INVSEE:
  # Enable or disable the invsee system globally (true / false)
  ENABLED: true

  # Title header displayed on the invsee GUI ({player} placeholder supported)
  TITLE: '&8Inventory of {player}'

  # Require target player to be online to open invsee (true / false)
  REQUIRE-ONLINE: true

  # Allow staff members with modify permission to edit target inventory items (true / false)
  ALLOW-EDIT: false

  # Allow staff members to view their own inventory via /invsee (true / false)
  ALLOW-SELF-VIEW: false

  # Notify target player when staff opens their inventory (true / false)
  NOTIFY-TARGET: false

  # Log invsee usage and item modifications to console/audit log (true / false)
  LOG-USAGE: true

  # Freeze inventory GUI into a snapshot if target logs out while being viewed (true / false)
  FREEZE-ON-LOGOUT: true

  # Auto-refresh interval in ticks to synchronize target inventory updates (20 ticks = 1s)
  AUTO-REFRESH-TICKS: 10

  # Permission node to view target player inventory
  VIEW-PERMISSION: ultimatedonutsmp.staff.invsee

  # Permission node to modify items in target player inventory
  MODIFY-PERMISSION: ultimatedonutsmp.staff.invsee.modify

  # Permission node for admin invsee commands
  ADMIN-PERMISSION: ultimatedonutsmp.admin.invsee

# Inventory GUI layout slot mapping
```

---

## Section: `LAYOUT`

### 1. Commented Setup Code Example

```yaml
LAYOUT:
  # Total GUI container size (must be multiple of 9, max 54)
  SIZE: 54

  # GUI slot indexes assigned to armor slots [Helmet, Chestplate, Leggings, Boots]
  ARMOR-SLOTS:
    - 0
    - 1
    - 2
    - 3

  # GUI slot index assigned to offhand item
  OFFHAND-SLOT: 4

  # GUI slot index assigned to player stats summary icon
  SUMMARY-SLOT: 6

  # GUI slot index assigned to staff connection status icon
  STATUS-SLOT: 8

  # GUI slot index start for main inventory items (slots 9-35)
  MAIN-INVENTORY-START: 18

  # GUI slot index start for player hotbar items (slots 0-8)
  HOTBAR-START: 45

# System and feedback messages
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `LAYOUT.SIZE` | `int` | Any valid integer number | `'54'` | Configures the technical `SIZE` parameter for `LAYOUT.SIZE` in `invsee.yml`. |
| `LAYOUT.ARMOR-SLOTS` | `list` | List of configured items/strings | `[0, 1, 2...]` | Configures the technical `ARMOR-SLOTS` parameter for `LAYOUT.ARMOR-SLOTS` in `invsee.yml`. |
| `LAYOUT.OFFHAND-SLOT` | `int` | Any valid integer number | `'4'` | Configures the technical `OFFHAND-SLOT` parameter for `LAYOUT.OFFHAND-SLOT` in `invsee.yml`. |
| `LAYOUT.SUMMARY-SLOT` | `int` | Any valid integer number | `'6'` | Configures the technical `SUMMARY-SLOT` parameter for `LAYOUT.SUMMARY-SLOT` in `invsee.yml`. |
| `LAYOUT.STATUS-SLOT` | `int` | Any valid integer number | `'8'` | Configures the technical `STATUS-SLOT` parameter for `LAYOUT.STATUS-SLOT` in `invsee.yml`. |
| `LAYOUT.MAIN-INVENTORY-START` | `int` | Any valid integer number | `'18'` | Configures the technical `MAIN-INVENTORY-START` parameter for `LAYOUT.MAIN-INVENTORY-START` in `invsee.yml`. |
| `LAYOUT.HOTBAR-START` | `int` | Any valid integer number | `'45'` | Configures the technical `HOTBAR-START` parameter for `LAYOUT.HOTBAR-START` in `invsee.yml`. |

### 3. Practical Setup Example

```yaml
LAYOUT:
  # Total GUI container size (must be multiple of 9, max 54)
  SIZE: 54

  # GUI slot indexes assigned to armor slots [Helmet, Chestplate, Leggings, Boots]
  ARMOR-SLOTS:
    - 0
    - 1
    - 2
    - 3

  # GUI slot index assigned to offhand item
  OFFHAND-SLOT: 4

  # GUI slot index assigned to player stats summary icon
  SUMMARY-SLOT: 6

  # GUI slot index assigned to staff connection status icon
  STATUS-SLOT: 8

  # GUI slot index start for main inventory items (slots 9-35)
  MAIN-INVENTORY-START: 18

  # GUI slot index start for player hotbar items (slots 0-8)
  HOTBAR-START: 45

# System and feedback messages
```

---

## Section: `MESSAGES`

### 1. Commented Setup Code Example

```yaml
MESSAGES:
  NO-PERMISSION: '&cYou do not have permission.'
  FEATURE-DISABLED: '&cThe Invsee system is disabled.'
  PLAYER-NOT-ONLINE: '&cThat player must be online.'
  PLAYER-NOT-FOUND: '&cPlayer not found.'
  SELF-VIEW-DISABLED: '&cYou cannot invsee yourself.'
  EDIT-CONFLICT: '&eAnother staff member is already editing &f{target}&e. Opened in read-only mode.'
  TARGET-LOGGED-OUT: '&eThis inventory is now a frozen snapshot because &f{target}&e logged out.'
  TARGET-NOTIFY: '&eYour inventory is being viewed by &f{viewer}&e in &f{mode}&e mode.'
  RELOAD-SUCCESS: '&aInvsee config reloaded.'
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `MESSAGES.NO-PERMISSION` | `str` | Any string text | `'&cYou do not have permission.'` | Configures the technical `NO-PERMISSION` parameter for `MESSAGES.NO-PERMISSION` in `invsee.yml`. |
| `MESSAGES.FEATURE-DISABLED` | `str` | Any string text | `'&cThe Invsee system is disabled.'` | Configures the technical `FEATURE-DISABLED` parameter for `MESSAGES.FEATURE-DISABLED` in `invsee.yml`. |
| `MESSAGES.PLAYER-NOT-ONLINE` | `str` | Any string text | `'&cThat player must be online.'` | Configures the technical `PLAYER-NOT-ONLINE` parameter for `MESSAGES.PLAYER-NOT-ONLINE` in `invsee.yml`. |
| `MESSAGES.PLAYER-NOT-FOUND` | `str` | Any string text | `'&cPlayer not found.'` | Configures the technical `PLAYER-NOT-FOUND` parameter for `MESSAGES.PLAYER-NOT-FOUND` in `invsee.yml`. |
| `MESSAGES.SELF-VIEW-DISABLED` | `str` | Any string text | `'&cYou cannot invsee yourself.'` | Configures the technical `SELF-VIEW-DISABLED` parameter for `MESSAGES.SELF-VIEW-DISABLED` in `invsee.yml`. |
| `MESSAGES.EDIT-CONFLICT` | `str` | Any string text | `'&eAnother staff member is already e...'` | Configures the technical `EDIT-CONFLICT` parameter for `MESSAGES.EDIT-CONFLICT` in `invsee.yml`. |
| `MESSAGES.TARGET-LOGGED-OUT` | `str` | Any string text | `'&eThis inventory is now a frozen sn...'` | Configures the technical `TARGET-LOGGED-OUT` parameter for `MESSAGES.TARGET-LOGGED-OUT` in `invsee.yml`. |
| `MESSAGES.TARGET-NOTIFY` | `str` | Any string text | `'&eYour inventory is being viewed by...'` | Configures the technical `TARGET-NOTIFY` parameter for `MESSAGES.TARGET-NOTIFY` in `invsee.yml`. |
| `MESSAGES.RELOAD-SUCCESS` | `str` | Any string text | `'&aInvsee config reloaded.'` | Configures the technical `RELOAD-SUCCESS` parameter for `MESSAGES.RELOAD-SUCCESS` in `invsee.yml`. |

### 3. Practical Setup Example

```yaml
MESSAGES:
  NO-PERMISSION: '&cYou do not have permission.'
  FEATURE-DISABLED: '&cThe Invsee system is disabled.'
  PLAYER-NOT-ONLINE: '&cThat player must be online.'
  PLAYER-NOT-FOUND: '&cPlayer not found.'
  SELF-VIEW-DISABLED: '&cYou cannot invsee yourself.'
  EDIT-CONFLICT: '&eAnother staff member is already editing &f{target}&e. Opened in read-only mode.'
  TARGET-LOGGED-OUT: '&eThis inventory is now a frozen snapshot because &f{target}&e logged out.'
  TARGET-NOTIFY: '&eYour inventory is being viewed by &f{viewer}&e in &f{mode}&e mode.'
  RELOAD-SUCCESS: '&aInvsee config reloaded.'
```

---

