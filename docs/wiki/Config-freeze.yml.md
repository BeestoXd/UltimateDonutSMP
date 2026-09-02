# Detailed Configuration & Setup Guide: `freeze.yml`

This is the official, 100% complete technical setup guide for `freeze.yml` in **UltimateDonutSMP**.
Each section details the exact commented setup code block, allowed option values, data types, default values, and in-depth functional behavior.

---

## Section: `FREEZE`

### 1. Commented Setup Code Example

```yaml
FREEZE:
  # Enable or disable the freeze system globally (true / false)
  ENABLED: true

  # Preserve frozen status across player logouts (true / false)
  PERSIST-ON-QUIT: true

  # Preserve frozen status across server restarts (true / false)
  PERSIST-ON-RESTART: true

  # Allow frozen players to look around (true / false)
  ALLOW-LOOK: true

  # Interval in ticks between periodic freeze alert messages sent to frozen players (20 ticks = 1s)
  ALERT-INTERVAL-TICKS: 100

  # Log freeze system actions to console/database (true / false)
  LOG-USAGE: true

  # Server identifier used in quit broadcast alerts
  SERVER-NAME: survival-01

  # Required permission node to execute /freeze
  STAFF-PERMISSION: ultimatedonutsmp.staff.freeze

  # Permission node to receive staff freeze broadcast alerts
  ALERT-PERMISSION: ultimatedonutsmp.staff.freeze.alert

  # Permission node granting exemption from being frozen
  EXEMPT-PERMISSION: ultimatedonutsmp.staff.freeze.exempt

  # Permission node to reload/administer freeze system
  ADMIN-PERMISSION: ultimatedonutsmp.admin.freeze

  # Commands allowed for frozen players (e.g., messaging staff or discord)
  ALLOWED-COMMANDS:
    - /discord
    - /social
    - /msg
    - /r
    - /helpop

  # Display tag for active freeze status
  STATUS_ON: '&a&lON'

  # Display tag for inactive freeze status
  STATUS_OFF: '&c&lOFF'

  # Toggle feedback message sent to staff (%player% and %status% placeholders)
  MESSAGE: '&bFreeze &a%player% &7is now %status%'

  # Periodic notification screen broadcast to frozen players
  ALERT:
    - ''
    - '&c&lYou''re currently frozen!'
    - ''
    - '&7- You cannot move or interact'
    - '&7- Staff members are reviewing your actions'
    - '&7- Join our Discord for support:'
    - '&6&ohttps://discord.com'
    - ''

  # Alert broadcast to staff when a frozen player leaves the server
  QUIT_MESSAGE: '&c[Freeze] &4%player% &cleft while frozen on &4%server%'

# System and user feedback messages
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `FREEZE.ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `FREEZE` system. Set to `true` to enable, `false` to disable. |
| `FREEZE.PERSIST-ON-QUIT` | `bool` | `true`, `false` | `true` | Configures the technical `PERSIST-ON-QUIT` parameter for `FREEZE.PERSIST-ON-QUIT` in `freeze.yml`. |
| `FREEZE.PERSIST-ON-RESTART` | `bool` | `true`, `false` | `true` | Configures the technical `PERSIST-ON-RESTART` parameter for `FREEZE.PERSIST-ON-RESTART` in `freeze.yml`. |
| `FREEZE.ALLOW-LOOK` | `bool` | `true`, `false` | `true` | Configures the technical `ALLOW-LOOK` parameter for `FREEZE.ALLOW-LOOK` in `freeze.yml`. |
| `FREEZE.ALERT-INTERVAL-TICKS` | `int` | Any valid integer number | `'100'` | Configures the technical `ALERT-INTERVAL-TICKS` parameter for `FREEZE.ALERT-INTERVAL-TICKS` in `freeze.yml`. |
| `FREEZE.LOG-USAGE` | `bool` | `true`, `false` | `true` | Configures the technical `LOG-USAGE` parameter for `FREEZE.LOG-USAGE` in `freeze.yml`. |
| `FREEZE.SERVER-NAME` | `str` | Any string text | `'survival-01'` | Configures the technical `SERVER-NAME` parameter for `FREEZE.SERVER-NAME` in `freeze.yml`. |
| `FREEZE.STAFF-PERMISSION` | `str` | Any string text | `'ultimatedonutsmp.staff.freeze'` | Configures the technical `STAFF-PERMISSION` parameter for `FREEZE.STAFF-PERMISSION` in `freeze.yml`. |
| `FREEZE.ALERT-PERMISSION` | `str` | Any string text | `'ultimatedonutsmp.staff.freeze.alert'` | Configures the technical `ALERT-PERMISSION` parameter for `FREEZE.ALERT-PERMISSION` in `freeze.yml`. |
| `FREEZE.EXEMPT-PERMISSION` | `str` | Any string text | `'ultimatedonutsmp.staff.freeze.exemp...'` | Configures the technical `EXEMPT-PERMISSION` parameter for `FREEZE.EXEMPT-PERMISSION` in `freeze.yml`. |
| `FREEZE.ADMIN-PERMISSION` | `str` | Any string text | `'ultimatedonutsmp.admin.freeze'` | Configures the technical `ADMIN-PERMISSION` parameter for `FREEZE.ADMIN-PERMISSION` in `freeze.yml`. |
| `FREEZE.ALLOWED-COMMANDS` | `list` | List of configured items/strings | `[/discord, /social, /msg...]` | Configures the technical `ALLOWED-COMMANDS` parameter for `FREEZE.ALLOWED-COMMANDS` in `freeze.yml`. |
| `FREEZE.STATUS_ON` | `str` | Any string text | `'&a&lON'` | Configures the technical `STATUS_ON` parameter for `FREEZE.STATUS_ON` in `freeze.yml`. |
| `FREEZE.STATUS_OFF` | `str` | Any string text | `'&c&lOFF'` | Configures the technical `STATUS_OFF` parameter for `FREEZE.STATUS_OFF` in `freeze.yml`. |
| `FREEZE.MESSAGE` | `str` | Any string text | `'&bFreeze &a%player% &7is now %statu...'` | Configures the technical `MESSAGE` parameter for `FREEZE.MESSAGE` in `freeze.yml`. |
| `FREEZE.ALERT` | `list` | List of configured items/strings | `[, &c&lYou're currently frozen!, ...]` | Configures the technical `ALERT` parameter for `FREEZE.ALERT` in `freeze.yml`. |
| `FREEZE.QUIT_MESSAGE` | `str` | Any string text | `'&c[Freeze] &4%player% &cleft while ...'` | Configures the technical `QUIT_MESSAGE` parameter for `FREEZE.QUIT_MESSAGE` in `freeze.yml`. |

### 3. Practical Setup Example

```yaml
FREEZE:
  # Enable or disable the freeze system globally (true / false)
  ENABLED: true

  # Preserve frozen status across player logouts (true / false)
  PERSIST-ON-QUIT: true

  # Preserve frozen status across server restarts (true / false)
  PERSIST-ON-RESTART: true

  # Allow frozen players to look around (true / false)
  ALLOW-LOOK: true

  # Interval in ticks between periodic freeze alert messages sent to frozen players (20 ticks = 1s)
  ALERT-INTERVAL-TICKS: 100

  # Log freeze system actions to console/database (true / false)
  LOG-USAGE: true

  # Server identifier used in quit broadcast alerts
  SERVER-NAME: survival-01

  # Required permission node to execute /freeze
  STAFF-PERMISSION: ultimatedonutsmp.staff.freeze

  # Permission node to receive staff freeze broadcast alerts
  ALERT-PERMISSION: ultimatedonutsmp.staff.freeze.alert

  # Permission node granting exemption from being frozen
  EXEMPT-PERMISSION: ultimatedonutsmp.staff.freeze.exempt

  # Permission node to reload/administer freeze system
  ADMIN-PERMISSION: ultimatedonutsmp.admin.freeze

  # Commands allowed for frozen players (e.g., messaging staff or discord)
  ALLOWED-COMMANDS:
    - /discord
    - /social
    - /msg
    - /r
    - /helpop

  # Display tag for active freeze status
  STATUS_ON: '&a&lON'

  # Display tag for inactive freeze status
  STATUS_OFF: '&c&lOFF'

  # Toggle feedback message sent to staff (%player% and %status% placeholders)
  MESSAGE: '&bFreeze &a%player% &7is now %status%'

  # Periodic notification screen broadcast to frozen players
  ALERT:
    - ''
    - '&c&lYou''re currently frozen!'
    - ''
    - '&7- You cannot move or interact'
    - '&7- Staff members are reviewing your actions'
    - '&7- Join our Discord for support:'
    - '&6&ohttps://discord.com'
    - ''

  # Alert broadcast to staff when a frozen player leaves the server
  QUIT_MESSAGE: '&c[Freeze] &4%player% &cleft while frozen on &4%server%'

# System and user feedback messages
```

---

## Section: `MESSAGES`

### 1. Commented Setup Code Example

```yaml
MESSAGES:
  NO-PERMISSION: '&cYou do not have permission.'
  FEATURE-DISABLED: '&cThe Freeze system is disabled.'
  PLAYER-NOT-FOUND: '&cPlayer not found.'
  TARGET-OFFLINE: '&cThat player must be online.'
  TARGET-EXEMPT: '&cYou cannot freeze that player.'
  SELF-TARGET: '&cYou cannot freeze yourself.'
  COMMAND-BLOCKED: '&cYou cannot use commands while frozen.'
  STILL-FROZEN: '&cYou are still frozen. Wait for staff instructions.'
  UNFROZEN: '&aYou are no longer frozen.'
  RELOAD-SUCCESS: '&aFreeze config reloaded.'
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `MESSAGES.NO-PERMISSION` | `str` | Any string text | `'&cYou do not have permission.'` | Configures the technical `NO-PERMISSION` parameter for `MESSAGES.NO-PERMISSION` in `freeze.yml`. |
| `MESSAGES.FEATURE-DISABLED` | `str` | Any string text | `'&cThe Freeze system is disabled.'` | Configures the technical `FEATURE-DISABLED` parameter for `MESSAGES.FEATURE-DISABLED` in `freeze.yml`. |
| `MESSAGES.PLAYER-NOT-FOUND` | `str` | Any string text | `'&cPlayer not found.'` | Configures the technical `PLAYER-NOT-FOUND` parameter for `MESSAGES.PLAYER-NOT-FOUND` in `freeze.yml`. |
| `MESSAGES.TARGET-OFFLINE` | `str` | Any string text | `'&cThat player must be online.'` | Configures the technical `TARGET-OFFLINE` parameter for `MESSAGES.TARGET-OFFLINE` in `freeze.yml`. |
| `MESSAGES.TARGET-EXEMPT` | `str` | Any string text | `'&cYou cannot freeze that player.'` | Configures the technical `TARGET-EXEMPT` parameter for `MESSAGES.TARGET-EXEMPT` in `freeze.yml`. |
| `MESSAGES.SELF-TARGET` | `str` | Any string text | `'&cYou cannot freeze yourself.'` | Configures the technical `SELF-TARGET` parameter for `MESSAGES.SELF-TARGET` in `freeze.yml`. |
| `MESSAGES.COMMAND-BLOCKED` | `str` | Any string text | `'&cYou cannot use commands while fro...'` | Configures the technical `COMMAND-BLOCKED` parameter for `MESSAGES.COMMAND-BLOCKED` in `freeze.yml`. |
| `MESSAGES.STILL-FROZEN` | `str` | Any string text | `'&cYou are still frozen. Wait for st...'` | Configures the technical `STILL-FROZEN` parameter for `MESSAGES.STILL-FROZEN` in `freeze.yml`. |
| `MESSAGES.UNFROZEN` | `str` | Any string text | `'&aYou are no longer frozen.'` | Configures the technical `UNFROZEN` parameter for `MESSAGES.UNFROZEN` in `freeze.yml`. |
| `MESSAGES.RELOAD-SUCCESS` | `str` | Any string text | `'&aFreeze config reloaded.'` | Configures the technical `RELOAD-SUCCESS` parameter for `MESSAGES.RELOAD-SUCCESS` in `freeze.yml`. |

### 3. Practical Setup Example

```yaml
MESSAGES:
  NO-PERMISSION: '&cYou do not have permission.'
  FEATURE-DISABLED: '&cThe Freeze system is disabled.'
  PLAYER-NOT-FOUND: '&cPlayer not found.'
  TARGET-OFFLINE: '&cThat player must be online.'
  TARGET-EXEMPT: '&cYou cannot freeze that player.'
  SELF-TARGET: '&cYou cannot freeze yourself.'
  COMMAND-BLOCKED: '&cYou cannot use commands while frozen.'
  STILL-FROZEN: '&cYou are still frozen. Wait for staff instructions.'
  UNFROZEN: '&aYou are no longer frozen.'
  RELOAD-SUCCESS: '&aFreeze config reloaded.'
```

---

