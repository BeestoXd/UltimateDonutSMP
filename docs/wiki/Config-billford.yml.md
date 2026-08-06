# Detailed Configuration & Setup Guide: `billford.yml`

This is the official, 100% complete technical setup guide for `billford.yml` in **UltimateDonutSMP**.
Each section details the exact commented setup code block, allowed option values, data types, default values, and in-depth functional behavior.

---

## Section: `CURRENT`

### 1. Commented Setup Code Example

```yaml
CURRENT: 1
# The text or value for Rotation Mode. Available options: Any valid string text
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `CURRENT` | `int` | Any valid integer number | `'1'` | Configures the technical `CURRENT` parameter for `CURRENT` in `billford.yml`. |

### 3. Practical Setup Example

```yaml
CURRENT: 1
# The text or value for Rotation Mode. Available options: Any valid string text
```

---

## Section: `ROTATION_MODE`

### 1. Commented Setup Code Example

```yaml
ROTATION_MODE: SEQUENTIAL
# Configuration section for Countdown.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `ROTATION_MODE` | `str` | Any string text | `'SEQUENTIAL'` | Configures the technical `ROTATION_MODE` parameter for `ROTATION_MODE` in `billford.yml`. |

### 3. Practical Setup Example

```yaml
ROTATION_MODE: SEQUENTIAL
# Configuration section for Countdown.
```

---

## Section: `COUNTDOWN`

### 1. Commented Setup Code Example

```yaml
COUNTDOWN:
  # Determines whether Enabled is enabled or disabled. Available options: true, false
  ENABLED: true
  # The text or value for Time Zone. Available options: Any valid string text
  TIME_ZONE: SYSTEM
  # The text or value for Start Date. Available options: Any valid string text
  START_DATE: '2026-01-31 00:00:00'
  # The numerical value for Interval Days. Available options: Any valid integer
  INTERVAL_DAYS: 3
  # The numerical value for Interval Hours. Available options: Any valid integer
  INTERVAL_HOURS: 0
# Determines whether Announce On Rotate is enabled or disabled. Available options: true, false
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `COUNTDOWN.ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `COUNTDOWN` system. Set to `true` to enable, `false` to disable. |
| `COUNTDOWN.TIME_ZONE` | `str` | Any string text | `'SYSTEM'` | Configures the technical `TIME_ZONE` parameter for `COUNTDOWN.TIME_ZONE` in `billford.yml`. |
| `COUNTDOWN.START_DATE` | `str` | Any string text | `'2026-01-31 00:00:00'` | Configures the technical `START_DATE` parameter for `COUNTDOWN.START_DATE` in `billford.yml`. |
| `COUNTDOWN.INTERVAL_DAYS` | `int` | Any valid integer number | `'3'` | Configures the technical `INTERVAL_DAYS` parameter for `COUNTDOWN.INTERVAL_DAYS` in `billford.yml`. |
| `COUNTDOWN.INTERVAL_HOURS` | `int` | Any valid integer number | `'0'` | Configures the technical `INTERVAL_HOURS` parameter for `COUNTDOWN.INTERVAL_HOURS` in `billford.yml`. |

### 3. Practical Setup Example

```yaml
COUNTDOWN:
  # Determines whether Enabled is enabled or disabled. Available options: true, false
  ENABLED: true
  # The text or value for Time Zone. Available options: Any valid string text
  TIME_ZONE: SYSTEM
  # The text or value for Start Date. Available options: Any valid string text
  START_DATE: '2026-01-31 00:00:00'
  # The numerical value for Interval Days. Available options: Any valid integer
  INTERVAL_DAYS: 3
  # The numerical value for Interval Hours. Available options: Any valid integer
  INTERVAL_HOURS: 0
# Determines whether Announce On Rotate is enabled or disabled. Available options: true, false
```

---

## Section: `ANNOUNCE_ON_ROTATE`

### 1. Commented Setup Code Example

```yaml
ANNOUNCE_ON_ROTATE: true
# The text or value for Announce Message. Available options: Any valid string text
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `ANNOUNCE_ON_ROTATE` | `bool` | `true`, `false` | `true` | Configures the technical `ANNOUNCE_ON_ROTATE` parameter for `ANNOUNCE_ON_ROTATE` in `billford.yml`. |

### 3. Practical Setup Example

```yaml
ANNOUNCE_ON_ROTATE: true
# The text or value for Announce Message. Available options: Any valid string text
```

---

## Section: `ANNOUNCE_MESSAGE`

### 1. Commented Setup Code Example

```yaml
ANNOUNCE_MESSAGE: '&6&l[Billford] &eTrade has rotated! &7New trade is live. &bNext
  change in &f{countdown}&b.'
# Configuration section for Access.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `ANNOUNCE_MESSAGE` | `str` | Any string text | `'&6&l[Billford] &eTrade has rotated!...'` | Configures the technical `ANNOUNCE_MESSAGE` parameter for `ANNOUNCE_MESSAGE` in `billford.yml`. |

### 3. Practical Setup Example

```yaml
ANNOUNCE_MESSAGE: '&6&l[Billford] &eTrade has rotated! &7New trade is live. &bNext
  change in &f{countdown}&b.'
# Configuration section for Access.
```

---

## Section: `ACCESS`

### 1. Commented Setup Code Example

```yaml
ACCESS:
  # The text or value for Permission. Available options: Any valid string text
  PERMISSION: ''
  # The text or value for Command. Available options: Any valid string text
  COMMAND: /billford
  # Configuration section for Npc.
  NPC:
    # Determines whether Enabled is enabled or disabled. Available options: true, false
    ENABLED: false
    # The text or value for Display Name. Available options: Any valid string text
    DISPLAY_NAME: '&aBillford'
    # The text or value for Interact Command. Available options: Any valid string text
    INTERACT_COMMAND: /billford
# Configuration section for Settings.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `ACCESS.PERMISSION` | `str` | Any string text | `''` | Configures the technical `PERMISSION` parameter for `ACCESS.PERMISSION` in `billford.yml`. |
| `ACCESS.COMMAND` | `str` | Any string text | `'/billford'` | Configures the technical `COMMAND` parameter for `ACCESS.COMMAND` in `billford.yml`. |
| `ACCESS.NPC.ENABLED` | `bool` | `true`, `false` | `false` | Global toggle for `ACCESS` system. Set to `true` to enable, `false` to disable. |
| `ACCESS.NPC.DISPLAY_NAME` | `str` | Any string text | `'&aBillford'` | Configures the technical `DISPLAY_NAME` parameter for `ACCESS.NPC.DISPLAY_NAME` in `billford.yml`. |
| `ACCESS.NPC.INTERACT_COMMAND` | `str` | Any string text | `'/billford'` | Configures the technical `INTERACT_COMMAND` parameter for `ACCESS.NPC.INTERACT_COMMAND` in `billford.yml`. |

### 3. Practical Setup Example

```yaml
ACCESS:
  # The text or value for Permission. Available options: Any valid string text
  PERMISSION: ''
  # The text or value for Command. Available options: Any valid string text
  COMMAND: /billford
  # Configuration section for Npc.
  NPC:
    # Determines whether Enabled is enabled or disabled. Available options: true, false
    ENABLED: false
    # The text or value for Display Name. Available options: Any valid string text
    DISPLAY_NAME: '&aBillford'
    # The text or value for Interact Command. Available options: Any valid string text
    INTERACT_COMMAND: /billford
# Configuration section for Settings.
```

---

## Section: `SETTINGS`

### 1. Commented Setup Code Example

```yaml
SETTINGS:
  # The numerical value for Click Cooldown Ms. Available options: Any valid integer
  CLICK_COOLDOWN_MS: 1000
  # Determines whether Close Menu On Success is enabled or disabled. Available options: true, false
  CLOSE_MENU_ON_SUCCESS: true
  # Determines whether Reopen On Trade Change is enabled or disabled. Available options: true, false
  REOPEN_ON_TRADE_CHANGE: true
  # Configuration section for Allowed Click Types.
  ALLOWED_CLICK_TYPES:
  - LEFT
  - RIGHT
# Configuration section for Gui.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `SETTINGS.CLICK_COOLDOWN_MS` | `int` | Any valid integer number | `'1000'` | Configures the technical `CLICK_COOLDOWN_MS` parameter for `SETTINGS.CLICK_COOLDOWN_MS` in `billford.yml`. |
| `SETTINGS.CLOSE_MENU_ON_SUCCESS` | `bool` | `true`, `false` | `true` | Configures the technical `CLOSE_MENU_ON_SUCCESS` parameter for `SETTINGS.CLOSE_MENU_ON_SUCCESS` in `billford.yml`. |
| `SETTINGS.REOPEN_ON_TRADE_CHANGE` | `bool` | `true`, `false` | `true` | Configures the technical `REOPEN_ON_TRADE_CHANGE` parameter for `SETTINGS.REOPEN_ON_TRADE_CHANGE` in `billford.yml`. |
| `SETTINGS.ALLOWED_CLICK_TYPES` | `list` | List of configured items/strings | `['LEFT', 'RIGHT']` | Configures the technical `ALLOWED_CLICK_TYPES` parameter for `SETTINGS.ALLOWED_CLICK_TYPES` in `billford.yml`. |

### 3. Practical Setup Example

```yaml
SETTINGS:
  # The numerical value for Click Cooldown Ms. Available options: Any valid integer
  CLICK_COOLDOWN_MS: 1000
  # Determines whether Close Menu On Success is enabled or disabled. Available options: true, false
  CLOSE_MENU_ON_SUCCESS: true
  # Determines whether Reopen On Trade Change is enabled or disabled. Available options: true, false
  REOPEN_ON_TRADE_CHANGE: true
  # Configuration section for Allowed Click Types.
  ALLOWED_CLICK_TYPES:
  - LEFT
  - RIGHT
# Configuration section for Gui.
```

---

## Section: `GUI`

### 1. Commented Setup Code Example

```yaml
GUI:
  TITLE: '&8&lBillford &7Trade'
  SIZE: 54
  # The text or value for Filler Material. Available options: Any valid string text
  FILLER_MATERIAL: GRAY_STAINED_GLASS_PANE
  # The numerical value for Reward Slot. Available options: Any valid integer
  REWARD_SLOT: 25
  # The numerical value for Countdown Slot. Available options: Any valid integer
  COUNTDOWN_SLOT: 31
  # The numerical value for Info Slot. Available options: Any valid integer
  INFO_SLOT: 49
  # The numerical value for Confirm Slot. Available options: Any valid integer
  CONFIRM_SLOT: 22
  # The text or value for Info Material. Available options: Any valid string text
  INFO_MATERIAL: BOOK
  # The text or value for Input Name. Available options: Any valid string text
  INPUT_NAME: '&f{item}'
  # Configuration section for Input Lore.
  INPUT_LORE:
  - '&7Cost: &f{amount}x &e{item}'
  - ''
  - '&8Must already be in your inventory.'
  # The text or value for Reward Name. Available options: Any valid string text
  REWARD_NAME: '&aReward'
  # Configuration section for Reward Lore.
  REWARD_LORE:
  - '&7Reward: &a{amount}x &f{item}'
  - '{money_line}'
  - '{shard_line}'
  - ''
  - '&8Granted when the trade succeeds.'
  # The text or value for Countdown Name. Available options: Any valid string text
  COUNTDOWN_NAME: '&e{trade_name}'
  # Configuration section for Countdown Lore.
  COUNTDOWN_LORE:
  - '&7Next rotation: &b{countdown}'
  - ''
  - '&8Trade &f{trade_id} &8of &f{trade_count}'
  # The text or value for Info Name. Available options: Any valid string text
  INFO_NAME: '&6Trade Info'
  # Configuration section for Info Limited Lore.
  INFO_LIMITED_LORE:
  - '&7Trades: &f{used} &7/ &f{limit}'
  - '{status_line}'
  # Configuration section for Info Unlimited Lore.
  INFO_UNLIMITED_LORE:
  - '&7Trades done: &f{used}'
  - '&aUnlimited trades this rotation.'
  # Configuration section for Confirm Button.
  CONFIRM_BUTTON:
    MATERIAL: EMERALD
    NAME: '&a&lConfirm Trade'
    LORE:
    - '&7Bring the required items,'
    - '&7then click to exchange them.'
    - ''
    - '&a&lClick to Confirm'
  # Configuration section for Limit Button.
  LIMIT_BUTTON:
    MATERIAL: BARRIER
    NAME: '&c&lLimit Reached'
    LORE:
    - '&cYou already used this deal enough'
    - '&cfor the current rotation.'
    - '&7Come back after the next refresh.'
# Configuration section for Feedback.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `GUI.TITLE` | `str` | Any string text | `'&8&lBillford &7Trade'` | Configures the technical `TITLE` parameter for `GUI.TITLE` in `billford.yml`. |
| `GUI.SIZE` | `int` | Any valid integer number | `'54'` | Configures the technical `SIZE` parameter for `GUI.SIZE` in `billford.yml`. |
| `GUI.FILLER_MATERIAL` | `str` | Any string text | `'GRAY_STAINED_GLASS_PANE'` | Configures the technical `FILLER_MATERIAL` parameter for `GUI.FILLER_MATERIAL` in `billford.yml`. |
| `GUI.REWARD_SLOT` | `int` | Any valid integer number | `'25'` | Configures the technical `REWARD_SLOT` parameter for `GUI.REWARD_SLOT` in `billford.yml`. |
| `GUI.COUNTDOWN_SLOT` | `int` | Any valid integer number | `'31'` | Configures the technical `COUNTDOWN_SLOT` parameter for `GUI.COUNTDOWN_SLOT` in `billford.yml`. |
| `GUI.INFO_SLOT` | `int` | Any valid integer number | `'49'` | Configures the technical `INFO_SLOT` parameter for `GUI.INFO_SLOT` in `billford.yml`. |
| `GUI.CONFIRM_SLOT` | `int` | Any valid integer number | `'22'` | Configures the technical `CONFIRM_SLOT` parameter for `GUI.CONFIRM_SLOT` in `billford.yml`. |
| `GUI.INFO_MATERIAL` | `str` | Any string text | `'BOOK'` | Configures the technical `INFO_MATERIAL` parameter for `GUI.INFO_MATERIAL` in `billford.yml`. |
| `GUI.INPUT_NAME` | `str` | Any string text | `'&f{item}'` | Configures the technical `INPUT_NAME` parameter for `GUI.INPUT_NAME` in `billford.yml`. |
| `GUI.INPUT_LORE` | `list` | List of configured items/strings | `['&7Cost: &f{amount}x &e{item}', '', '&8Must already be in your inventory.']` | Configures the technical `INPUT_LORE` parameter for `GUI.INPUT_LORE` in `billford.yml`. |
| `GUI.REWARD_NAME` | `str` | Any string text | `'&aReward'` | Configures the technical `REWARD_NAME` parameter for `GUI.REWARD_NAME` in `billford.yml`. |
| `GUI.REWARD_LORE` | `list` | List of configured items/strings | `[&7Reward: &a{amount}x &f{item}, {money_line}, {shard_line}...]` | Configures the technical `REWARD_LORE` parameter for `GUI.REWARD_LORE` in `billford.yml`. |
| `GUI.COUNTDOWN_NAME` | `str` | Any string text | `'&e{trade_name}'` | Configures the technical `COUNTDOWN_NAME` parameter for `GUI.COUNTDOWN_NAME` in `billford.yml`. |
| `GUI.COUNTDOWN_LORE` | `list` | List of configured items/strings | `['&7Next rotation: &b{countdown}', '', '&8Trade &f{trade_id} &8of &f{trade_count}']` | Configures the technical `COUNTDOWN_LORE` parameter for `GUI.COUNTDOWN_LORE` in `billford.yml`. |
| `GUI.INFO_NAME` | `str` | Any string text | `'&6Trade Info'` | Configures the technical `INFO_NAME` parameter for `GUI.INFO_NAME` in `billford.yml`. |
| `GUI.INFO_LIMITED_LORE` | `list` | List of configured items/strings | `['&7Trades: &f{used} &7/ &f{limit}', '{status_line}']` | Configures the technical `INFO_LIMITED_LORE` parameter for `GUI.INFO_LIMITED_LORE` in `billford.yml`. |
| `GUI.INFO_UNLIMITED_LORE` | `list` | List of configured items/strings | `['&7Trades done: &f{used}', '&aUnlimited trades this rotation.']` | Configures the technical `INFO_UNLIMITED_LORE` parameter for `GUI.INFO_UNLIMITED_LORE` in `billford.yml`. |
| `GUI.CONFIRM_BUTTON.MATERIAL` | `str` | Any string text | `'EMERALD'` | Configures the technical `MATERIAL` parameter for `GUI.CONFIRM_BUTTON.MATERIAL` in `billford.yml`. |
| `GUI.CONFIRM_BUTTON.NAME` | `str` | Any string text | `'&a&lConfirm Trade'` | Configures the technical `NAME` parameter for `GUI.CONFIRM_BUTTON.NAME` in `billford.yml`. |
| `GUI.CONFIRM_BUTTON.LORE` | `list` | List of configured items/strings | `[&7Bring the required items,, &7then click to exchange them., ...]` | Configures the technical `LORE` parameter for `GUI.CONFIRM_BUTTON.LORE` in `billford.yml`. |
| `GUI.LIMIT_BUTTON.MATERIAL` | `str` | Any string text | `'BARRIER'` | Configures the technical `MATERIAL` parameter for `GUI.LIMIT_BUTTON.MATERIAL` in `billford.yml`. |
| `GUI.LIMIT_BUTTON.NAME` | `str` | Any string text | `'&c&lLimit Reached'` | Configures the technical `NAME` parameter for `GUI.LIMIT_BUTTON.NAME` in `billford.yml`. |
| `GUI.LIMIT_BUTTON.LORE` | `list` | List of configured items/strings | `['&cYou already used this deal enough', '&cfor the current rotation.', '&7Come back after the next refresh.']` | Configures the technical `LORE` parameter for `GUI.LIMIT_BUTTON.LORE` in `billford.yml`. |

### 3. Practical Setup Example

```yaml
GUI:
  TITLE: '&8&lBillford &7Trade'
  SIZE: 54
  # The text or value for Filler Material. Available options: Any valid string text
  FILLER_MATERIAL: GRAY_STAINED_GLASS_PANE
  # The numerical value for Reward Slot. Available options: Any valid integer
  REWARD_SLOT: 25
  # The numerical value for Countdown Slot. Available options: Any valid integer
  COUNTDOWN_SLOT: 31
  # The numerical value for Info Slot. Available options: Any valid integer
  INFO_SLOT: 49
  # The numerical value for Confirm Slot. Available options: Any valid integer
  CONFIRM_SLOT: 22
  # The text or value for Info Material. Available options: Any valid string text
  INFO_MATERIAL: BOOK
  # The text or value for Input Name. Available options: Any valid string text
  INPUT_NAME: '&f{item}'
  # Configuration section for Input Lore.
  INPUT_LORE:
  - '&7Cost: &f{amount}x &e{item}'
  - ''
  - '&8Must already be in your inventory.'
  # The text or value for Reward Name. Available options: Any valid string text
  REWARD
```

---

## Section: `FEEDBACK`

### 1. Commented Setup Code Example

```yaml
FEEDBACK:
  # Configuration section for Success Particle.
  SUCCESS_PARTICLE:
    # Determines whether Enabled is enabled or disabled. Available options: true, false
    ENABLED: true
    TYPE: TOTEM_OF_UNDYING
    # The numerical value for Count. Available options: Any valid integer
    COUNT: 24
# Configuration section for Billford.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `FEEDBACK.SUCCESS_PARTICLE.ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `FEEDBACK` system. Set to `true` to enable, `false` to disable. |
| `FEEDBACK.SUCCESS_PARTICLE.TYPE` | `str` | Any string text | `'TOTEM_OF_UNDYING'` | Configures the technical `TYPE` parameter for `FEEDBACK.SUCCESS_PARTICLE.TYPE` in `billford.yml`. |
| `FEEDBACK.SUCCESS_PARTICLE.COUNT` | `int` | Any valid integer number | `'24'` | Configures the technical `COUNT` parameter for `FEEDBACK.SUCCESS_PARTICLE.COUNT` in `billford.yml`. |

### 3. Practical Setup Example

```yaml
FEEDBACK:
  # Configuration section for Success Particle.
  SUCCESS_PARTICLE:
    # Determines whether Enabled is enabled or disabled. Available options: true, false
    ENABLED: true
    TYPE: TOTEM_OF_UNDYING
    # The numerical value for Count. Available options: Any valid integer
    COUNT: 24
# Configuration section for Billford.
```

---

## Section: `BILLFORD`

### 1. Commented Setup Code Example

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
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `BILLFORD.1.DISPLAY_NAME` | `str` | Any string text | `'The Gem Exchange'` | Configures the technical `DISPLAY_NAME` parameter for `BILLFORD.1.DISPLAY_NAME` in `billford.yml`. |
| `BILLFORD.1.LIMIT` | `int` | Any valid integer number | `'3'` | Configures the technical `LIMIT` parameter for `BILLFORD.1.LIMIT` in `billford.yml`. |
| `BILLFORD.1.INPUTS.1.SLOT` | `int` | Any valid integer number | `'11'` | Configures the technical `SLOT` parameter for `BILLFORD.1.INPUTS.1.SLOT` in `billford.yml`. |
| `BILLFORD.1.INPUTS.1.MATERIAL` | `str` | Any string text | `'DIAMOND'` | Configures the technical `MATERIAL` parameter for `BILLFORD.1.INPUTS.1.MATERIAL` in `billford.yml`. |
| `BILLFORD.1.INPUTS.1.QUANTITY` | `int` | Any valid integer number | `'3'` | Configures the technical `QUANTITY` parameter for `BILLFORD.1.INPUTS.1.QUANTITY` in `billford.yml`. |
| `BILLFORD.1.INPUTS.2.SLOT` | `int` | Any valid integer number | `'12'` | Configures the technical `SLOT` parameter for `BILLFORD.1.INPUTS.2.SLOT` in `billford.yml`. |
| `BILLFORD.1.INPUTS.2.MATERIAL` | `str` | Any string text | `'EMERALD'` | Configures the technical `MATERIAL` parameter for `BILLFORD.1.INPUTS.2.MATERIAL` in `billford.yml`. |
| `BILLFORD.1.INPUTS.2.QUANTITY` | `int` | Any valid integer number | `'5'` | Configures the technical `QUANTITY` parameter for `BILLFORD.1.INPUTS.2.QUANTITY` in `billford.yml`. |
| `BILLFORD.1.REWARD.MATERIAL` | `str` | Any string text | `'PISTON'` | Configures the technical `MATERIAL` parameter for `BILLFORD.1.REWARD.MATERIAL` in `billford.yml`. |
| `BILLFORD.1.REWARD.QUANTITY` | `int` | Any valid integer number | `'64'` | Configures the technical `QUANTITY` parameter for `BILLFORD.1.REWARD.QUANTITY` in `billford.yml`. |
| `BILLFORD.1.REWARD.SHARD_BONUS` | `int` | Any valid integer number | `'0'` | Configures the technical `SHARD_BONUS` parameter for `BILLFORD.1.REWARD.SHARD_BONUS` in `billford.yml`. |
| `BILLFORD.1.REWARD.MONEY_BONUS` | `int` | Any valid integer number | `'0'` | Configures the technical `MONEY_BONUS` parameter for `BILLFORD.1.REWARD.MONEY_BONUS` in `billford.yml`. |
| `BILLFORD.2.DISPLAY_NAME` | `str` | Any string text | `'The Utility Crate'` | Configures the technical `DISPLAY_NAME` parameter for `BILLFORD.2.DISPLAY_NAME` in `billford.yml`. |
| `BILLFORD.2.LIMIT` | `int` | Any valid integer number | `'2'` | Configures the technical `LIMIT` parameter for `BILLFORD.2.LIMIT` in `billford.yml`. |
| `BILLFORD.2.INPUTS.1.SLOT` | `int` | Any valid integer number | `'10'` | Configures the technical `SLOT` parameter for `BILLFORD.2.INPUTS.1.SLOT` in `billford.yml`. |
| `BILLFORD.2.INPUTS.1.MATERIAL` | `str` | Any string text | `'IRON_INGOT'` | Configures the technical `MATERIAL` parameter for `BILLFORD.2.INPUTS.1.MATERIAL` in `billford.yml`. |
| `BILLFORD.2.INPUTS.1.QUANTITY` | `int` | Any valid integer number | `'32'` | Configures the technical `QUANTITY` parameter for `BILLFORD.2.INPUTS.1.QUANTITY` in `billford.yml`. |
| `BILLFORD.2.INPUTS.2.SLOT` | `int` | Any valid integer number | `'11'` | Configures the technical `SLOT` parameter for `BILLFORD.2.INPUTS.2.SLOT` in `billford.yml`. |
| `BILLFORD.2.INPUTS.2.MATERIAL` | `str` | Any string text | `'REDSTONE'` | Configures the technical `MATERIAL` parameter for `BILLFORD.2.INPUTS.2.MATERIAL` in `billford.yml`. |
| `BILLFORD.2.INPUTS.2.QUANTITY` | `int` | Any valid integer number | `'16'` | Configures the technical `QUANTITY` parameter for `BILLFORD.2.INPUTS.2.QUANTITY` in `billford.yml`. |
| `BILLFORD.2.INPUTS.3.SLOT` | `int` | Any valid integer number | `'12'` | Configures the technical `SLOT` parameter for `BILLFORD.2.INPUTS.3.SLOT` in `billford.yml`. |
| `BILLFORD.2.INPUTS.3.MATERIAL` | `str` | Any string text | `'EMERALD'` | Configures the technical `MATERIAL` parameter for `BILLFORD.2.INPUTS.3.MATERIAL` in `billford.yml`. |
| `BILLFORD.2.INPUTS.3.QUANTITY` | `int` | Any valid integer number | `'12'` | Configures the technical `QUANTITY` parameter for `BILLFORD.2.INPUTS.3.QUANTITY` in `billford.yml`. |
| `BILLFORD.2.REWARD.MATERIAL` | `str` | Any string text | `'OBSERVER'` | Configures the technical `MATERIAL` parameter for `BILLFORD.2.REWARD.MATERIAL` in `billford.yml`. |
| `BILLFORD.2.REWARD.QUANTITY` | `int` | Any valid integer number | `'32'` | Configures the technical `QUANTITY` parameter for `BILLFORD.2.REWARD.QUANTITY` in `billford.yml`. |
| `BILLFORD.2.REWARD.SHARD_BONUS` | `int` | Any valid integer number | `'40'` | Configures the technical `SHARD_BONUS` parameter for `BILLFORD.2.REWARD.SHARD_BONUS` in `billford.yml`. |
| `BILLFORD.2.REWARD.MONEY_BONUS` | `int` | Any valid integer number | `'0'` | Configures the technical `MONEY_BONUS` parameter for `BILLFORD.2.REWARD.MONEY_BONUS` in `billford.yml`. |
| `BILLFORD.3.DISPLAY_NAME` | `str` | Any string text | `'The Ingot Deal'` | Configures the technical `DISPLAY_NAME` parameter for `BILLFORD.3.DISPLAY_NAME` in `billford.yml`. |
| `BILLFORD.3.LIMIT` | `int` | Any valid integer number | `'1'` | Configures the technical `LIMIT` parameter for `BILLFORD.3.LIMIT` in `billford.yml`. |
| `BILLFORD.3.INPUTS.1.SLOT` | `int` | Any valid integer number | `'11'` | Configures the technical `SLOT` parameter for `BILLFORD.3.INPUTS.1.SLOT` in `billford.yml`. |
| *(24 additional sub-keys configured in section)* | | | | |

### 3. Practical Setup Example

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
      # The numerical value for Money B
```

---

