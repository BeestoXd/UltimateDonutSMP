# Detailed Configuration & Setup Guide: `crates.yml`

This is the official, 100% complete technical setup guide for `crates.yml` in **UltimateDonutSMP**.
Each section details the exact commented setup code block, allowed option values, data types, default values, and in-depth functional behavior.

---

## Section: `SETTINGS`

### 1. Commented Setup Code Example

```yaml
SETTINGS:
  # Configuration section for List Menu.
  LIST-MENU:
    TITLE: '&8Crates'
    SIZE: 27
    # The text or value for Filler. Available options: Any valid string text
    FILLER: GRAY_STAINED_GLASS_PANE
    # Configuration section for Content Slots.
    CONTENT-SLOTS:
    - 10
    - 11
    - 12
    - 13
    - 14
    - 15
    - 16
    # The numerical value for Empty Slot. Available options: Any valid integer
    EMPTY-SLOT: 13
    # Configuration section for Empty.
    EMPTY:
      MATERIAL: BARRIER
      DISPLAY-NAME: '&cNo Crates'
      LORE:
      - '&7No crates are available right now.'
    # Configuration section for Close Button.
    CLOSE-BUTTON:
      SLOT: 26
      MATERIAL: BARRIER
      DISPLAY-NAME: '&cClose'
      LORE:
      - '&7Close this menu.'
  # Configuration section for Confirm Menu.
  CONFIRM-MENU:
    SIZE: 27
    # The text or value for Filler. Available options: Any valid string text
    FILLER: GRAY_STAINED_GLASS_PANE
    # The numerical value for Preview Slot. Available options: Any valid integer
    PREVIEW-SLOT: 13
    # The numerical value for Confirm Slot. Available options: Any valid integer
    CONFIRM-SLOT: 15
    # Configuration section for Confirm Button.
    CONFIRM-BUTTON:
      MATERIAL: LIME_STAINED_GLASS_PANE
      DISPLAY-NAME: '&aConfirm'
      LORE:
      - '&7Claim &f{reward}&7 from'
      - '&b{crate}&7.'
    # Configuration section for Cancel Button.
    CANCEL-BUTTON:
      SLOT: 11
      MATERIAL: RED_STAINED_GLASS_PANE
      DISPLAY-NAME: '&cCancel'
      LORE:
      - '&7Return to the reward list.'
  # Configuration section for Hologram.
  HOLOGRAM:
    # The decimal value for Offset Y. Available options: Any decimal number
    OFFSET-Y: 1.6
    # Configuration section for Lines.
    LINES:
    - '{crate}'
    - '&7Right-click to open'
    # The text or value for Key Line. Available options: Any valid string text
    KEY-LINE: '&7Keys: &f{keys}'
  # Configuration section for Particles.
  PARTICLES:
    # Determines whether Enabled is enabled or disabled. Available options: true, false
    ENABLED: true
    TYPE: ENCHANT
    # The numerical value for Count. Available options: Any valid integer
    COUNT: 4
  # Configuration section for Gacha.
  GACHA:
    TITLE: '&8Rolling Reward'
    # The text or value for Filler. Available options: Any valid string text
    FILLER: BLACK_STAINED_GLASS_PANE
    # Configuration section for Preview Slots.
    PREVIEW-SLOTS:
    - 10
    - 11
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `SETTINGS.LIST-MENU.TITLE` | `str` | Any string text | `'&8Crates'` | Configures the technical `TITLE` parameter for `SETTINGS.LIST-MENU.TITLE` in `crates.yml`. |
| `SETTINGS.LIST-MENU.SIZE` | `int` | Any valid integer number | `'27'` | Configures the technical `SIZE` parameter for `SETTINGS.LIST-MENU.SIZE` in `crates.yml`. |
| `SETTINGS.LIST-MENU.FILLER` | `str` | Any string text | `'GRAY_STAINED_GLASS_PANE'` | Configures the technical `FILLER` parameter for `SETTINGS.LIST-MENU.FILLER` in `crates.yml`. |
| `SETTINGS.LIST-MENU.CONTENT-SLOTS` | `list` | List of configured items/strings | `[10, 11, 12...]` | Configures the technical `CONTENT-SLOTS` parameter for `SETTINGS.LIST-MENU.CONTENT-SLOTS` in `crates.yml`. |
| `SETTINGS.LIST-MENU.EMPTY-SLOT` | `int` | Any valid integer number | `'13'` | Configures the technical `EMPTY-SLOT` parameter for `SETTINGS.LIST-MENU.EMPTY-SLOT` in `crates.yml`. |
| `SETTINGS.LIST-MENU.EMPTY.MATERIAL` | `str` | Any string text | `'BARRIER'` | Configures the technical `MATERIAL` parameter for `SETTINGS.LIST-MENU.EMPTY.MATERIAL` in `crates.yml`. |
| `SETTINGS.LIST-MENU.EMPTY.DISPLAY-NAME` | `str` | Any string text | `'&cNo Crates'` | Configures the technical `DISPLAY-NAME` parameter for `SETTINGS.LIST-MENU.EMPTY.DISPLAY-NAME` in `crates.yml`. |
| `SETTINGS.LIST-MENU.EMPTY.LORE` | `list` | List of configured items/strings | `['&7No crates are available right now.']` | Configures the technical `LORE` parameter for `SETTINGS.LIST-MENU.EMPTY.LORE` in `crates.yml`. |
| `SETTINGS.LIST-MENU.CLOSE-BUTTON.SLOT` | `int` | Any valid integer number | `'26'` | Configures the technical `SLOT` parameter for `SETTINGS.LIST-MENU.CLOSE-BUTTON.SLOT` in `crates.yml`. |
| `SETTINGS.LIST-MENU.CLOSE-BUTTON.MATERIAL` | `str` | Any string text | `'BARRIER'` | Configures the technical `MATERIAL` parameter for `SETTINGS.LIST-MENU.CLOSE-BUTTON.MATERIAL` in `crates.yml`. |
| `SETTINGS.LIST-MENU.CLOSE-BUTTON.DISPLAY-NAME` | `str` | Any string text | `'&cClose'` | Configures the technical `DISPLAY-NAME` parameter for `SETTINGS.LIST-MENU.CLOSE-BUTTON.DISPLAY-NAME` in `crates.yml`. |
| `SETTINGS.LIST-MENU.CLOSE-BUTTON.LORE` | `list` | List of configured items/strings | `['&7Close this menu.']` | Configures the technical `LORE` parameter for `SETTINGS.LIST-MENU.CLOSE-BUTTON.LORE` in `crates.yml`. |
| `SETTINGS.CONFIRM-MENU.SIZE` | `int` | Any valid integer number | `'27'` | Configures the technical `SIZE` parameter for `SETTINGS.CONFIRM-MENU.SIZE` in `crates.yml`. |
| `SETTINGS.CONFIRM-MENU.FILLER` | `str` | Any string text | `'GRAY_STAINED_GLASS_PANE'` | Configures the technical `FILLER` parameter for `SETTINGS.CONFIRM-MENU.FILLER` in `crates.yml`. |
| `SETTINGS.CONFIRM-MENU.PREVIEW-SLOT` | `int` | Any valid integer number | `'13'` | Configures the technical `PREVIEW-SLOT` parameter for `SETTINGS.CONFIRM-MENU.PREVIEW-SLOT` in `crates.yml`. |
| `SETTINGS.CONFIRM-MENU.CONFIRM-SLOT` | `int` | Any valid integer number | `'15'` | Configures the technical `CONFIRM-SLOT` parameter for `SETTINGS.CONFIRM-MENU.CONFIRM-SLOT` in `crates.yml`. |
| `SETTINGS.CONFIRM-MENU.CONFIRM-BUTTON.MATERIAL` | `str` | Any string text | `'LIME_STAINED_GLASS_PANE'` | Configures the technical `MATERIAL` parameter for `SETTINGS.CONFIRM-MENU.CONFIRM-BUTTON.MATERIAL` in `crates.yml`. |
| `SETTINGS.CONFIRM-MENU.CONFIRM-BUTTON.DISPLAY-NAME` | `str` | Any string text | `'&aConfirm'` | Configures the technical `DISPLAY-NAME` parameter for `SETTINGS.CONFIRM-MENU.CONFIRM-BUTTON.DISPLAY-NAME` in `crates.yml`. |
| `SETTINGS.CONFIRM-MENU.CONFIRM-BUTTON.LORE` | `list` | List of configured items/strings | `['&7Claim &f{reward}&7 from', '&b{crate}&7.']` | Configures the technical `LORE` parameter for `SETTINGS.CONFIRM-MENU.CONFIRM-BUTTON.LORE` in `crates.yml`. |
| `SETTINGS.CONFIRM-MENU.CANCEL-BUTTON.SLOT` | `int` | Any valid integer number | `'11'` | Configures the technical `SLOT` parameter for `SETTINGS.CONFIRM-MENU.CANCEL-BUTTON.SLOT` in `crates.yml`. |
| `SETTINGS.CONFIRM-MENU.CANCEL-BUTTON.MATERIAL` | `str` | Any string text | `'RED_STAINED_GLASS_PANE'` | Configures the technical `MATERIAL` parameter for `SETTINGS.CONFIRM-MENU.CANCEL-BUTTON.MATERIAL` in `crates.yml`. |
| `SETTINGS.CONFIRM-MENU.CANCEL-BUTTON.DISPLAY-NAME` | `str` | Any string text | `'&cCancel'` | Configures the technical `DISPLAY-NAME` parameter for `SETTINGS.CONFIRM-MENU.CANCEL-BUTTON.DISPLAY-NAME` in `crates.yml`. |
| `SETTINGS.CONFIRM-MENU.CANCEL-BUTTON.LORE` | `list` | List of configured items/strings | `['&7Return to the reward list.']` | Configures the technical `LORE` parameter for `SETTINGS.CONFIRM-MENU.CANCEL-BUTTON.LORE` in `crates.yml`. |
| `SETTINGS.HOLOGRAM.OFFSET-Y` | `float` | Any decimal number | `'1.6'` | Configures the technical `OFFSET-Y` parameter for `SETTINGS.HOLOGRAM.OFFSET-Y` in `crates.yml`. |
| `SETTINGS.HOLOGRAM.LINES` | `list` | List of configured items/strings | `['{crate}', '&7Right-click to open']` | Configures the technical `LINES` parameter for `SETTINGS.HOLOGRAM.LINES` in `crates.yml`. |
| `SETTINGS.HOLOGRAM.KEY-LINE` | `str` | Any string text | `'&7Keys: &f{keys}'` | Configures the technical `KEY-LINE` parameter for `SETTINGS.HOLOGRAM.KEY-LINE` in `crates.yml`. |
| `SETTINGS.PARTICLES.ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `SETTINGS` system. Set to `true` to enable, `false` to disable. |
| `SETTINGS.PARTICLES.TYPE` | `str` | Any string text | `'ENCHANT'` | Configures the technical `TYPE` parameter for `SETTINGS.PARTICLES.TYPE` in `crates.yml`. |
| `SETTINGS.PARTICLES.COUNT` | `int` | Any valid integer number | `'4'` | Configures the technical `COUNT` parameter for `SETTINGS.PARTICLES.COUNT` in `crates.yml`. |
| `SETTINGS.GACHA.TITLE` | `str` | Any string text | `'&8Rolling Reward'` | Configures the technical `TITLE` parameter for `SETTINGS.GACHA.TITLE` in `crates.yml`. |
| *(5 additional sub-keys configured in section)* | | | | |

### 3. Practical Setup Example

```yaml
SETTINGS:
  # Configuration section for List Menu.
  LIST-MENU:
    TITLE: '&8Crates'
    SIZE: 27
    # The text or value for Filler. Available options: Any valid string text
    FILLER: GRAY_STAINED_GLASS_PANE
    # Configuration section for Content Slots.
    CONTENT-SLOTS:
    - 10
    - 11
    - 12
    - 13
    - 14
    - 15
    - 16
    # The numerical value for Empty Slot. Available options: Any valid integer
    EMPTY-SLOT: 13
    # Configuration section for Empty.
    EMPTY:
      MATERIAL: BARRIER
      DISPLAY-NAME: '&cNo Crates'
      LORE:
      - '&7No crates are available right now.'
    # Configuration section for Close Button.
    CLOSE-BUTTON:
      SLOT: 26
      MATERIAL: BARRIER
      DISPLAY-NAME: '&cClose'
      LORE:
      - '&7Close this menu.'
  # Configuration section for Confirm Menu.
  CONFIRM-MENU:
    SIZE: 27
    # The text or value for Filler. Available options: Any valid string text
    FILLER: GRAY_STAINED_GLASS_PANE
    # The numerical value for Prev
```

---

## Section: `CRATES`

### 1. Commented Setup Code Example

```yaml
CRATES:
  # Configuration section for Common.
  common:
    # Determines whether Enabled is enabled or disabled. Available options: true, false
    ENABLED: true
    # The text or value for Open Type. Available options: Any valid string text
    OPEN-TYPE: CHOOSE_ONE
    # Configuration section for Display.
    DISPLAY:
      MATERIAL: CHEST
      DISPLAY-NAME: '&fCommon Crate'
      LORE:
      - '&7Keys: &f{keys}'
      - '&aClick to open and choose 1 reward.'
    # Configuration section for Key Item.
    KEY-ITEM:
      MATERIAL: TRIPWIRE_HOOK
      DISPLAY-NAME: '&fCommon Key'
      LORE:
      - '&7Opens the &fCommon Crate&7.'
    # The text or value for Permission. Available options: Any valid string text
    PERMISSION: ''
    # Determines whether Broadcast On Claim is enabled or disabled. Available options: true, false
    BROADCAST-ON-CLAIM: false
    # Configuration section for Menu.
    MENU:
      # The text or value for Open Title. Available options: Any valid string text
      OPEN-TITLE: '&8Choose 1 Reward'
      # The text or value for Confirm Title. Available options: Any valid string text
      CONFIRM-TITLE: '&8Confirm Reward'
      SIZE: 27
      # The text or value for Filler. Available options: Any valid string text
      FILLER: BLACK_STAINED_GLASS_PANE
      # The numerical value for Back Slot. Available options: Any valid integer
      BACK-SLOT: 26
      # Configuration section for Back Button.
      BACK-BUTTON:
        MATERIAL: BARRIER
        DISPLAY-NAME: '&cBack'
        LORE:
        - '&7Return to the crate list.'
    # Configuration section for Rewards.
    REWARDS:
      # Configuration section for Iron Helmet.
      iron_helmet:
        SLOT: 10
        # Configuration section for Display.
        DISPLAY:
          MATERIAL: IRON_HELMET
          DISPLAY-NAME: '&fIron Helmet'
          LORE:
          - '&7Choose this reward.'
        # Configuration section for Grant.
        GRANT:
          TYPE: ITEM
          MATERIAL: IRON_HELMET
          # The numerical value for Amount. Available options: Any valid integer
          AMOUNT: 1
      # Configuration section for Iron Chestplate.
      iron_chestplate:
        SLOT: 11
        # Configuration section for Display.
        DISPLAY:
          MATERIAL: IRON_CHESTPLATE
          DISPLAY-NAME: '&fIron Chestplate'
          LORE:
          - '&7Choose this reward.'
        # Configuration section for Grant.
        GRANT:
          TYPE: ITEM
          MATERIAL: IRON_CHESTPLATE
          # The numerical value for Amount. Available options: Any valid integer
          AMOUNT: 1
      # Configuration section for Iron Leggings.
      iron_leggings:
        SLOT: 12
        # Configuration section for Display.
        DISPLAY:
          MATERIAL: IRON_LEGGINGS
          DISPLAY-NAME: '&fIron Leggings'
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `CRATES.common.ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `CRATES` system. Set to `true` to enable, `false` to disable. |
| `CRATES.common.OPEN-TYPE` | `str` | Any string text | `'CHOOSE_ONE'` | Configures the technical `OPEN-TYPE` parameter for `CRATES.common.OPEN-TYPE` in `crates.yml`. |
| `CRATES.common.DISPLAY.MATERIAL` | `str` | Any string text | `'CHEST'` | Configures the technical `MATERIAL` parameter for `CRATES.common.DISPLAY.MATERIAL` in `crates.yml`. |
| `CRATES.common.DISPLAY.DISPLAY-NAME` | `str` | Any string text | `'&fCommon Crate'` | Configures the technical `DISPLAY-NAME` parameter for `CRATES.common.DISPLAY.DISPLAY-NAME` in `crates.yml`. |
| `CRATES.common.DISPLAY.LORE` | `list` | List of configured items/strings | `['&7Keys: &f{keys}', '&aClick to open and choose 1 reward.']` | Configures the technical `LORE` parameter for `CRATES.common.DISPLAY.LORE` in `crates.yml`. |
| `CRATES.common.KEY-ITEM.MATERIAL` | `str` | Any string text | `'TRIPWIRE_HOOK'` | Configures the technical `MATERIAL` parameter for `CRATES.common.KEY-ITEM.MATERIAL` in `crates.yml`. |
| `CRATES.common.KEY-ITEM.DISPLAY-NAME` | `str` | Any string text | `'&fCommon Key'` | Configures the technical `DISPLAY-NAME` parameter for `CRATES.common.KEY-ITEM.DISPLAY-NAME` in `crates.yml`. |
| `CRATES.common.KEY-ITEM.LORE` | `list` | List of configured items/strings | `['&7Opens the &fCommon Crate&7.']` | Configures the technical `LORE` parameter for `CRATES.common.KEY-ITEM.LORE` in `crates.yml`. |
| `CRATES.common.PERMISSION` | `str` | Any string text | `''` | Configures the technical `PERMISSION` parameter for `CRATES.common.PERMISSION` in `crates.yml`. |
| `CRATES.common.BROADCAST-ON-CLAIM` | `bool` | `true`, `false` | `false` | Configures the technical `BROADCAST-ON-CLAIM` parameter for `CRATES.common.BROADCAST-ON-CLAIM` in `crates.yml`. |
| `CRATES.common.MENU.OPEN-TITLE` | `str` | Any string text | `'&8Choose 1 Reward'` | Configures the technical `OPEN-TITLE` parameter for `CRATES.common.MENU.OPEN-TITLE` in `crates.yml`. |
| `CRATES.common.MENU.CONFIRM-TITLE` | `str` | Any string text | `'&8Confirm Reward'` | Configures the technical `CONFIRM-TITLE` parameter for `CRATES.common.MENU.CONFIRM-TITLE` in `crates.yml`. |
| `CRATES.common.MENU.SIZE` | `int` | Any valid integer number | `'27'` | Configures the technical `SIZE` parameter for `CRATES.common.MENU.SIZE` in `crates.yml`. |
| `CRATES.common.MENU.FILLER` | `str` | Any string text | `'BLACK_STAINED_GLASS_PANE'` | Configures the technical `FILLER` parameter for `CRATES.common.MENU.FILLER` in `crates.yml`. |
| `CRATES.common.MENU.BACK-SLOT` | `int` | Any valid integer number | `'26'` | Configures the technical `BACK-SLOT` parameter for `CRATES.common.MENU.BACK-SLOT` in `crates.yml`. |
| `CRATES.common.MENU.BACK-BUTTON.MATERIAL` | `str` | Any string text | `'BARRIER'` | Configures the technical `MATERIAL` parameter for `CRATES.common.MENU.BACK-BUTTON.MATERIAL` in `crates.yml`. |
| `CRATES.common.MENU.BACK-BUTTON.DISPLAY-NAME` | `str` | Any string text | `'&cBack'` | Configures the technical `DISPLAY-NAME` parameter for `CRATES.common.MENU.BACK-BUTTON.DISPLAY-NAME` in `crates.yml`. |
| `CRATES.common.MENU.BACK-BUTTON.LORE` | `list` | List of configured items/strings | `['&7Return to the crate list.']` | Configures the technical `LORE` parameter for `CRATES.common.MENU.BACK-BUTTON.LORE` in `crates.yml`. |
| `CRATES.common.REWARDS.iron_helmet.SLOT` | `int` | Any valid integer number | `'10'` | Configures the technical `SLOT` parameter for `CRATES.common.REWARDS.iron_helmet.SLOT` in `crates.yml`. |
| `CRATES.common.REWARDS.iron_helmet.DISPLAY.MATERIAL` | `str` | Any string text | `'IRON_HELMET'` | Configures the technical `MATERIAL` parameter for `CRATES.common.REWARDS.iron_helmet.DISPLAY.MATERIAL` in `crates.yml`. |
| `CRATES.common.REWARDS.iron_helmet.DISPLAY.DISPLAY-NAME` | `str` | Any string text | `'&fIron Helmet'` | Configures the technical `DISPLAY-NAME` parameter for `CRATES.common.REWARDS.iron_helmet.DISPLAY.DISPLAY-NAME` in `crates.yml`. |
| `CRATES.common.REWARDS.iron_helmet.DISPLAY.LORE` | `list` | List of configured items/strings | `['&7Choose this reward.']` | Configures the technical `LORE` parameter for `CRATES.common.REWARDS.iron_helmet.DISPLAY.LORE` in `crates.yml`. |
| `CRATES.common.REWARDS.iron_helmet.GRANT.TYPE` | `str` | Any string text | `'ITEM'` | Configures the technical `TYPE` parameter for `CRATES.common.REWARDS.iron_helmet.GRANT.TYPE` in `crates.yml`. |
| `CRATES.common.REWARDS.iron_helmet.GRANT.MATERIAL` | `str` | Any string text | `'IRON_HELMET'` | Configures the technical `MATERIAL` parameter for `CRATES.common.REWARDS.iron_helmet.GRANT.MATERIAL` in `crates.yml`. |
| `CRATES.common.REWARDS.iron_helmet.GRANT.AMOUNT` | `int` | Any valid integer number | `'1'` | Configures the technical `AMOUNT` parameter for `CRATES.common.REWARDS.iron_helmet.GRANT.AMOUNT` in `crates.yml`. |
| `CRATES.common.REWARDS.iron_chestplate.SLOT` | `int` | Any valid integer number | `'11'` | Configures the technical `SLOT` parameter for `CRATES.common.REWARDS.iron_chestplate.SLOT` in `crates.yml`. |
| `CRATES.common.REWARDS.iron_chestplate.DISPLAY.MATERIAL` | `str` | Any string text | `'IRON_CHESTPLATE'` | Configures the technical `MATERIAL` parameter for `CRATES.common.REWARDS.iron_chestplate.DISPLAY.MATERIAL` in `crates.yml`. |
| `CRATES.common.REWARDS.iron_chestplate.DISPLAY.DISPLAY-NAME` | `str` | Any string text | `'&fIron Chestplate'` | Configures the technical `DISPLAY-NAME` parameter for `CRATES.common.REWARDS.iron_chestplate.DISPLAY.DISPLAY-NAME` in `crates.yml`. |
| `CRATES.common.REWARDS.iron_chestplate.DISPLAY.LORE` | `list` | List of configured items/strings | `['&7Choose this reward.']` | Configures the technical `LORE` parameter for `CRATES.common.REWARDS.iron_chestplate.DISPLAY.LORE` in `crates.yml`. |
| `CRATES.common.REWARDS.iron_chestplate.GRANT.TYPE` | `str` | Any string text | `'ITEM'` | Configures the technical `TYPE` parameter for `CRATES.common.REWARDS.iron_chestplate.GRANT.TYPE` in `crates.yml`. |
| *(147 additional sub-keys configured in section)* | | | | |

### 3. Practical Setup Example

```yaml
CRATES:
  # Configuration section for Common.
  common:
    # Determines whether Enabled is enabled or disabled. Available options: true, false
    ENABLED: true
    # The text or value for Open Type. Available options: Any valid string text
    OPEN-TYPE: CHOOSE_ONE
    # Configuration section for Display.
    DISPLAY:
      MATERIAL: CHEST
      DISPLAY-NAME: '&fCommon Crate'
      LORE:
      - '&7Keys: &f{keys}'
      - '&aClick to open and choose 1 reward.'
    # Configuration section for Key Item.
    KEY-ITEM:
      MATERIAL: TRIPWIRE_HOOK
      DISPLAY-NAME: '&fCommon Key'
      LORE:
      - '&7Opens the &fCommon Crate&7.'
    # The text or value for Permission. Available options: Any valid string text
    PERMISSION: ''
    # Determines whether Broadcast On Claim is enabled or disabled. Available options: true, false
    BROADCAST-ON-CLAIM: false
    # Configuration section for Menu.
    MENU:
      # The text or value for Open Title. Available options: Any valid string text

```

---

