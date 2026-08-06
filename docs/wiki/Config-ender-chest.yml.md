# Detailed Configuration & Setup Guide: `ender-chest.yml`

This is the official, 100% complete technical setup guide for `ender-chest.yml` in **UltimateDonutSMP**.
Each section details the exact commented setup code block, allowed option values, data types, default values, and in-depth functional behavior.

---

## Section: `ENDER-CHEST`

### 1. Commented Setup Code Example

```yaml
ENDER-CHEST:
  # Determines whether Enabled is enabled or disabled. Available options: true, false
  ENABLED: true
  # The numerical value for Default Rows. Available options: Any valid integer
  DEFAULT-ROWS: 6
  TITLE: '&5Ender Chest'
  # Determines whether Intercept Vanilla Open is enabled or disabled. Available options: true, false
  INTERCEPT-VANILLA-OPEN: true
  # Determines whether Allow Command is enabled or disabled. Available options: true, false
  ALLOW-COMMAND: true
  # Determines whether Command Requires Permission is enabled or disabled. Available options: true, false
  COMMAND-REQUIRES-PERMISSION: false
  # The text or value for Permission. Available options: Any valid string text
  PERMISSION: ultimatedonutsmp.enderchest
  # The numerical value for Auto Save Ticks. Available options: Any valid integer
  AUTO-SAVE-TICKS: 1200
  # Configuration section for Ecsee.
  ECSEE:
    # Determines whether Enabled is enabled or disabled. Available options: true, false
    ENABLED: true
    # The text or value for Permission. Available options: Any valid string text
    PERMISSION: ultimatedonutsmp.admin.ecsee
    # The numerical value for Auto Refresh Ticks. Available options: Any valid integer
    AUTO-REFRESH-TICKS: 20
    # Determines whether staff can edit other players' ender chests. Available options: true, false
    EDITABLE: false
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `ENDER-CHEST.ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `ENDER-CHEST` system. Set to `true` to enable, `false` to disable. |
| `ENDER-CHEST.DEFAULT-ROWS` | `int` | Any valid integer number | `'6'` | Configures the technical `DEFAULT-ROWS` parameter for `ENDER-CHEST.DEFAULT-ROWS` in `ender-chest.yml`. |
| `ENDER-CHEST.TITLE` | `str` | Any string text | `'&5Ender Chest'` | Configures the technical `TITLE` parameter for `ENDER-CHEST.TITLE` in `ender-chest.yml`. |
| `ENDER-CHEST.INTERCEPT-VANILLA-OPEN` | `bool` | `true`, `false` | `true` | Configures the technical `INTERCEPT-VANILLA-OPEN` parameter for `ENDER-CHEST.INTERCEPT-VANILLA-OPEN` in `ender-chest.yml`. |
| `ENDER-CHEST.ALLOW-COMMAND` | `bool` | `true`, `false` | `true` | Configures the technical `ALLOW-COMMAND` parameter for `ENDER-CHEST.ALLOW-COMMAND` in `ender-chest.yml`. |
| `ENDER-CHEST.COMMAND-REQUIRES-PERMISSION` | `bool` | `true`, `false` | `false` | Configures the technical `COMMAND-REQUIRES-PERMISSION` parameter for `ENDER-CHEST.COMMAND-REQUIRES-PERMISSION` in `ender-chest.yml`. |
| `ENDER-CHEST.PERMISSION` | `str` | Any string text | `'ultimatedonutsmp.enderchest'` | Configures the technical `PERMISSION` parameter for `ENDER-CHEST.PERMISSION` in `ender-chest.yml`. |
| `ENDER-CHEST.AUTO-SAVE-TICKS` | `int` | Any valid integer number | `'1200'` | Configures the technical `AUTO-SAVE-TICKS` parameter for `ENDER-CHEST.AUTO-SAVE-TICKS` in `ender-chest.yml`. |
| `ENDER-CHEST.ECSEE.ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `ENDER-CHEST` system. Set to `true` to enable, `false` to disable. |
| `ENDER-CHEST.ECSEE.PERMISSION` | `str` | Any string text | `'ultimatedonutsmp.admin.ecsee'` | Configures the technical `PERMISSION` parameter for `ENDER-CHEST.ECSEE.PERMISSION` in `ender-chest.yml`. |
| `ENDER-CHEST.ECSEE.AUTO-REFRESH-TICKS` | `int` | Any valid integer number | `'20'` | Configures the technical `AUTO-REFRESH-TICKS` parameter for `ENDER-CHEST.ECSEE.AUTO-REFRESH-TICKS` in `ender-chest.yml`. |
| `ENDER-CHEST.ECSEE.EDITABLE` | `bool` | `true`, `false` | `false` | Configures the technical `EDITABLE` parameter for `ENDER-CHEST.ECSEE.EDITABLE` in `ender-chest.yml`. |

### 3. Practical Setup Example

```yaml
ENDER-CHEST:
  # Determines whether Enabled is enabled or disabled. Available options: true, false
  ENABLED: true
  # The numerical value for Default Rows. Available options: Any valid integer
  DEFAULT-ROWS: 6
  TITLE: '&5Ender Chest'
  # Determines whether Intercept Vanilla Open is enabled or disabled. Available options: true, false
  INTERCEPT-VANILLA-OPEN: true
  # Determines whether Allow Command is enabled or disabled. Available options: true, false
  ALLOW-COMMAND: true
  # Determines whether Command Requires Permission is enabled or disabled. Available options: true, false
  COMMAND-REQUIRES-PERMISSION: false
  # The text or value for Permission. Available options: Any valid string text
  PERMISSION: ultimatedonutsmp.enderchest
  # The numerical value for Auto Save Ticks. Available options: Any valid integer
  AUTO-SAVE-TICKS: 1200
  # Configuration section for Ecsee.
  ECSEE:
    # Determines whether Enabled is enabled or disabled. Available options: true, false
    ENABLED: 
```

---

## Section: `MESSAGES`

### 1. Commented Setup Code Example

```yaml
MESSAGES:
  # The text or value for Feature Disabled. Available options: Any valid string text
  FEATURE-DISABLED: '&cThe Ender Chest 6 Rows system is disabled.'
  # The text or value for Command Disabled. Available options: Any valid string text
  COMMAND-DISABLED: '&cThe /enderchest command is disabled.'
  # The text or value for No Permission. Available options: Any valid string text
  NO-PERMISSION: '&cYou do not have permission to use this command.'
  # The text or value for Open Failed. Available options: Any valid string text
  OPEN-FAILED: '&cFailed to open your Ender Chest. Please try again.'
  # The text or value for Save Failed. Available options: Any valid string text
  SAVE-FAILED: '&cFailed to save your Ender Chest. Contact staff.'
  # The text or value for Reload Success. Available options: Any valid string text
  RELOAD-SUCCESS: '&aEnder Chest config reloaded.'
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `MESSAGES.FEATURE-DISABLED` | `str` | Any string text | `'&cThe Ender Chest 6 Rows system is ...'` | Configures the technical `FEATURE-DISABLED` parameter for `MESSAGES.FEATURE-DISABLED` in `ender-chest.yml`. |
| `MESSAGES.COMMAND-DISABLED` | `str` | Any string text | `'&cThe /enderchest command is disabl...'` | Configures the technical `COMMAND-DISABLED` parameter for `MESSAGES.COMMAND-DISABLED` in `ender-chest.yml`. |
| `MESSAGES.NO-PERMISSION` | `str` | Any string text | `'&cYou do not have permission to use...'` | Configures the technical `NO-PERMISSION` parameter for `MESSAGES.NO-PERMISSION` in `ender-chest.yml`. |
| `MESSAGES.OPEN-FAILED` | `str` | Any string text | `'&cFailed to open your Ender Chest. ...'` | Configures the technical `OPEN-FAILED` parameter for `MESSAGES.OPEN-FAILED` in `ender-chest.yml`. |
| `MESSAGES.SAVE-FAILED` | `str` | Any string text | `'&cFailed to save your Ender Chest. ...'` | Configures the technical `SAVE-FAILED` parameter for `MESSAGES.SAVE-FAILED` in `ender-chest.yml`. |
| `MESSAGES.RELOAD-SUCCESS` | `str` | Any string text | `'&aEnder Chest config reloaded.'` | Configures the technical `RELOAD-SUCCESS` parameter for `MESSAGES.RELOAD-SUCCESS` in `ender-chest.yml`. |

### 3. Practical Setup Example

```yaml
MESSAGES:
  # The text or value for Feature Disabled. Available options: Any valid string text
  FEATURE-DISABLED: '&cThe Ender Chest 6 Rows system is disabled.'
  # The text or value for Command Disabled. Available options: Any valid string text
  COMMAND-DISABLED: '&cThe /enderchest command is disabled.'
  # The text or value for No Permission. Available options: Any valid string text
  NO-PERMISSION: '&cYou do not have permission to use this command.'
  # The text or value for Open Failed. Available options: Any valid string text
  OPEN-FAILED: '&cFailed to open your Ender Chest. Please try again.'
  # The text or value for Save Failed. Available options: Any valid string text
  SAVE-FAILED: '&cFailed to save your Ender Chest. Contact staff.'
  # The text or value for Reload Success. Available options: Any valid string text
  RELOAD-SUCCESS: '&aEnder Chest config reloaded.'
```

---

