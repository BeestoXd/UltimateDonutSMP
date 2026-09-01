# Detailed Configuration & Setup Guide: `amethyst-tools.yml`

This is the official, 100% complete technical setup guide for `amethyst-tools.yml` in **UltimateDonutSMP**.
Each section details the exact commented setup code block, allowed option values, data types, default values, and in-depth functional behavior.

---

## Section: `AMETHYST-TOOLS`

### 1. Commented Setup Code Example

```yaml
AMETHYST-TOOLS:
  # Configuration section for Excluded Worlds.
  EXCLUDED-WORLDS:
  - duels
  # Configuration section for Particles.
  PARTICLES:
    # Determines whether Enabled is enabled or disabled. Available options: true, false
    ENABLED: true
    TYPE: BLOCK
    # The text or value for Block Material. Available options: Any valid string text
    BLOCK-MATERIAL: PURPLE_CONCRETE_POWDER
    # The numerical value for Count. Available options: Any valid integer
    COUNT: 12
    # The decimal value for Spread. Available options: Any decimal number
    SPREAD: 0.4
  # Configuration section for Sounds.
  SOUNDS:
    # The text or value for Use. Available options: Any valid string text
    USE: minecraft:block.amethyst_block.hit|1.0|1.2
    # The text or value for Expire. Available options: Any valid string text
    EXPIRE: minecraft:entity.lightning_bolt.impact|0.5|2.0
    # The text or value for Break. Available options: Any valid string text
    BREAK: minecraft:block.amethyst_block.break|1.0|0.8
    # The text or value for Activate. Available options: Any valid string text
    ACTIVATE: minecraft:block.amethyst_block.resonate|1.0|1.0
  # Configuration section for Security.
  SECURITY:
    # Determines whether Require Item Id is enabled or disabled. Available options: true, false
    REQUIRE-ITEM-ID: true
    # Determines whether Bind To Owner is enabled or disabled. Available options: true, false
    BIND-TO-OWNER: false
    # The numerical value for Click Cooldown Ms. Available options: Any valid integer
    CLICK-COOLDOWN-MS: 250
    # Determines whether Block Hopper Pickup is enabled or disabled. Available options: true, false
    BLOCK-HOPPER-PICKUP: true
  # Configuration section for Drill.
  DRILL:
    MATERIAL: NETHERITE_PICKAXE
    NAME: '&#9B59B6&lAmethyst Drill'
    LORE:
    - '&#BDC3C7Breaks &d9 blocks &7per strike'
    - '&#BDC3C7Powered by amethyst energy'
    - ''
    - '&#9B59B6✦ Self Destruct'
    - '&#BDC3C7{time}'
    # Configuration section for Enchantments.
    ENCHANTMENTS:
    - efficiency:5
    - unbreaking:3
    - fortune:3
    # The numerical value for Radius. Available options: Any valid integer
    RADIUS: 1
    # Configuration section for Disabled Blocks.
    DISABLED-BLOCKS:
    - DIRT
    - GRASS_BLOCK
    - COARSE_DIRT
    - ROOTED_DIRT
    - PODZOL
    - SAND
    - RED_SAND
    - GRAVEL
    - SPAWNER
    - BEDROCK
    - OBSIDIAN
    - CRYING_OBSIDIAN
    - END_PORTAL_FRAME
    - FARMLAND
    - SOUL_SAND
    - SOUL_SOIL
    # The numerical value for Duration. Available options: Any valid integer
    DURATION: 86400
    # Configuration section for Shard Shop.
    SHARD-SHOP:
      # Determines whether Enabled is enabled or disabled. Available options: true, false
      ENABLED: false
      # The currency this tool is sold for. Available options: SHARD, MONEY
      CURRENCY: SHARD
      SLOT: 18
      # The numerical value for Min Quantity. Available options: Any valid integer
      MIN-QUANTITY: 1
      # The numerical value for Max Quantity. Available options: Any valid integer
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `AMETHYST-TOOLS.EXCLUDED-WORLDS` | `list` | List of configured items/strings | `['duels']` | Configures the technical `EXCLUDED-WORLDS` parameter for `AMETHYST-TOOLS.EXCLUDED-WORLDS` in `amethyst-tools.yml`. |
| `AMETHYST-TOOLS.PARTICLES.ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `AMETHYST-TOOLS` system. Set to `true` to enable, `false` to disable. |
| `AMETHYST-TOOLS.PARTICLES.TYPE` | `str` | Any string text | `'BLOCK'` | Configures the technical `TYPE` parameter for `AMETHYST-TOOLS.PARTICLES.TYPE` in `amethyst-tools.yml`. |
| `AMETHYST-TOOLS.PARTICLES.BLOCK-MATERIAL` | `str` | Any string text | `'PURPLE_CONCRETE_POWDER'` | Configures the technical `BLOCK-MATERIAL` parameter for `AMETHYST-TOOLS.PARTICLES.BLOCK-MATERIAL` in `amethyst-tools.yml`. |
| `AMETHYST-TOOLS.PARTICLES.COUNT` | `int` | Any valid integer number | `'12'` | Configures the technical `COUNT` parameter for `AMETHYST-TOOLS.PARTICLES.COUNT` in `amethyst-tools.yml`. |
| `AMETHYST-TOOLS.PARTICLES.SPREAD` | `float` | Any decimal number | `'0.4'` | Configures the technical `SPREAD` parameter for `AMETHYST-TOOLS.PARTICLES.SPREAD` in `amethyst-tools.yml`. |
| `AMETHYST-TOOLS.SOUNDS.USE` | `str` | Any string text | `'minecraft:block.amethyst_block.hit|...'` | Configures the technical `USE` parameter for `AMETHYST-TOOLS.SOUNDS.USE` in `amethyst-tools.yml`. |
| `AMETHYST-TOOLS.SOUNDS.EXPIRE` | `str` | Any string text | `'minecraft:entity.lightning_bolt.imp...'` | Configures the technical `EXPIRE` parameter for `AMETHYST-TOOLS.SOUNDS.EXPIRE` in `amethyst-tools.yml`. |
| `AMETHYST-TOOLS.SOUNDS.BREAK` | `str` | Any string text | `'minecraft:block.amethyst_block.brea...'` | Configures the technical `BREAK` parameter for `AMETHYST-TOOLS.SOUNDS.BREAK` in `amethyst-tools.yml`. |
| `AMETHYST-TOOLS.SOUNDS.ACTIVATE` | `str` | Any string text | `'minecraft:block.amethyst_block.reso...'` | Configures the technical `ACTIVATE` parameter for `AMETHYST-TOOLS.SOUNDS.ACTIVATE` in `amethyst-tools.yml`. |
| `AMETHYST-TOOLS.SECURITY.REQUIRE-ITEM-ID` | `bool` | `true`, `false` | `true` | Configures the technical `REQUIRE-ITEM-ID` parameter for `AMETHYST-TOOLS.SECURITY.REQUIRE-ITEM-ID` in `amethyst-tools.yml`. |
| `AMETHYST-TOOLS.SECURITY.BIND-TO-OWNER` | `bool` | `true`, `false` | `false` | Configures the technical `BIND-TO-OWNER` parameter for `AMETHYST-TOOLS.SECURITY.BIND-TO-OWNER` in `amethyst-tools.yml`. |
| `AMETHYST-TOOLS.SECURITY.CLICK-COOLDOWN-MS` | `int` | Any valid integer number | `'250'` | Configures the technical `CLICK-COOLDOWN-MS` parameter for `AMETHYST-TOOLS.SECURITY.CLICK-COOLDOWN-MS` in `amethyst-tools.yml`. |
| `AMETHYST-TOOLS.SECURITY.BLOCK-HOPPER-PICKUP` | `bool` | `true`, `false` | `true` | Configures the technical `BLOCK-HOPPER-PICKUP` parameter for `AMETHYST-TOOLS.SECURITY.BLOCK-HOPPER-PICKUP` in `amethyst-tools.yml`. |
| `AMETHYST-TOOLS.DRILL.MATERIAL` | `str` | Any string text | `'NETHERITE_PICKAXE'` | Configures the technical `MATERIAL` parameter for `AMETHYST-TOOLS.DRILL.MATERIAL` in `amethyst-tools.yml`. |
| `AMETHYST-TOOLS.DRILL.NAME` | `str` | Any string text | `'&#9B59B6&lAmethyst Drill'` | Configures the technical `NAME` parameter for `AMETHYST-TOOLS.DRILL.NAME` in `amethyst-tools.yml`. |
| `AMETHYST-TOOLS.DRILL.LORE` | `list` | List of configured items/strings | `[&#BDC3C7Breaks &d9 blocks &7per strike, &#BDC3C7Powered by amethyst energy, ...]` | Configures the technical `LORE` parameter for `AMETHYST-TOOLS.DRILL.LORE` in `amethyst-tools.yml`. |
| `AMETHYST-TOOLS.DRILL.ENCHANTMENTS` | `list` | List of configured items/strings | `['efficiency:5', 'unbreaking:3', 'fortune:3']` | Configures the technical `ENCHANTMENTS` parameter for `AMETHYST-TOOLS.DRILL.ENCHANTMENTS` in `amethyst-tools.yml`. |
| `AMETHYST-TOOLS.DRILL.RADIUS` | `int` | Any valid integer number | `'1'` | Configures the technical `RADIUS` parameter for `AMETHYST-TOOLS.DRILL.RADIUS` in `amethyst-tools.yml`. |
| `AMETHYST-TOOLS.DRILL.DISABLED-BLOCKS` | `list` | List of configured items/strings | `[DIRT, GRASS_BLOCK, COARSE_DIRT...]` | Configures the technical `DISABLED-BLOCKS` parameter for `AMETHYST-TOOLS.DRILL.DISABLED-BLOCKS` in `amethyst-tools.yml`. |
| `AMETHYST-TOOLS.DRILL.DURATION` | `int` | Any valid integer number | `'86400'` | Configures the technical `DURATION` parameter for `AMETHYST-TOOLS.DRILL.DURATION` in `amethyst-tools.yml`. |
| `AMETHYST-TOOLS.DRILL.SHARD-SHOP.ENABLED` | `bool` | `true`, `false` | `false` | Global toggle for `AMETHYST-TOOLS` system. Set to `true` to enable, `false` to disable. |
| `AMETHYST-TOOLS.DRILL.SHARD-SHOP.CURRENCY` | `str` | `SHARD`, `MONEY` | `'SHARD'` | Currency this tool is priced in when it shows up in the shard menu. `SHARD` spends shards, `MONEY` charges the player's balance. Every tool has its own key, so a menu can mix both. |
| `AMETHYST-TOOLS.DRILL.SHARD-SHOP.SLOT` | `int` | Any valid integer number | `'18'` | Configures the technical `SLOT` parameter for `AMETHYST-TOOLS.DRILL.SHARD-SHOP.SLOT` in `amethyst-tools.yml`. |
| `AMETHYST-TOOLS.DRILL.SHARD-SHOP.MIN-QUANTITY` | `int` | Any valid integer number | `'1'` | Configures the technical `MIN-QUANTITY` parameter for `AMETHYST-TOOLS.DRILL.SHARD-SHOP.MIN-QUANTITY` in `amethyst-tools.yml`. |
| `AMETHYST-TOOLS.DRILL.SHARD-SHOP.MAX-QUANTITY` | `int` | Any valid integer number | `'1'` | Configures the technical `MAX-QUANTITY` parameter for `AMETHYST-TOOLS.DRILL.SHARD-SHOP.MAX-QUANTITY` in `amethyst-tools.yml`. |
| `AMETHYST-TOOLS.DRILL.SHARD-SHOP.DEFAULT-QUANTITY` | `int` | Any valid integer number | `'1'` | Configures the technical `DEFAULT-QUANTITY` parameter for `AMETHYST-TOOLS.DRILL.SHARD-SHOP.DEFAULT-QUANTITY` in `amethyst-tools.yml`. |
| `AMETHYST-TOOLS.DRILL.SHARD-SHOP.HIDE-QUANTITY-BUTTONS` | `bool` | `true`, `false` | `true` | Configures the technical `HIDE-QUANTITY-BUTTONS` parameter for `AMETHYST-TOOLS.DRILL.SHARD-SHOP.HIDE-QUANTITY-BUTTONS` in `amethyst-tools.yml`. |
| `AMETHYST-TOOLS.CHOPPER.MATERIAL` | `str` | Any string text | `'NETHERITE_AXE'` | Configures the technical `MATERIAL` parameter for `AMETHYST-TOOLS.CHOPPER.MATERIAL` in `amethyst-tools.yml`. |
| `AMETHYST-TOOLS.CHOPPER.NAME` | `str` | Any string text | `'&#9B59B6&lAmethyst Tree Chopper'` | Configures the technical `NAME` parameter for `AMETHYST-TOOLS.CHOPPER.NAME` in `amethyst-tools.yml`. |
| `AMETHYST-TOOLS.CHOPPER.LORE` | `list` | List of configured items/strings | `[&#BDC3C7Chops entire trees &dinstantly, &#BDC3C7Vein-style breaking, ...]` | Configures the technical `LORE` parameter for `AMETHYST-TOOLS.CHOPPER.LORE` in `amethyst-tools.yml`. |
| *(63 additional sub-keys configured in section)* | | | | |

### 3. Practical Setup Example

```yaml
AMETHYST-TOOLS:
  # Configuration section for Excluded Worlds.
  EXCLUDED-WORLDS:
  - duels
  # Configuration section for Particles.
  PARTICLES:
    # Determines whether Enabled is enabled or disabled. Available options: true, false
    ENABLED: true
    TYPE: BLOCK
    # The text or value for Block Material. Available options: Any valid string text
    BLOCK-MATERIAL: PURPLE_CONCRETE_POWDER
    # The numerical value for Count. Available options: Any valid integer
    COUNT: 12
    # The decimal value for Spread. Available options: Any decimal number
    SPREAD: 0.4
  # Configuration section for Sounds.
  SOUNDS:
    # The text or value for Use. Available options: Any valid string text
    USE: minecraft:block.amethyst_block.hit|1.0|1.2
    # The text or value for Expire. Available options: Any valid string text
    EXPIRE: minecraft:entity.lightning_bolt.impact|0.5|2.0
    # The text or value for Break. Available options: Any valid string text
    BREAK: minecraft:block.amethyst_bloc
```

---

## Section: `AMETHYST-MESSAGES`

### 1. Commented Setup Code Example

```yaml
AMETHYST-MESSAGES:
  # The text or value for Prefix. Available options: Any valid string text
  PREFIX: '&#9B59B6[Amethyst] &r'
  # The text or value for Expired. Available options: Any valid string text
  EXPIRED: '{prefix}&#BDC3C7Your &d{tool} &7has &cexpired &7and self-destructed!'
  # The text or value for Excluded World. Available options: Any valid string text
  EXCLUDED-WORLD: '{prefix}&#BDC3C7Amethyst Tools cannot be used in this world.'
  # The text or value for Drill Break. Available options: Any valid string text
  DRILL-BREAK: '{prefix}&#BDC3C7&dAmethyst Drill &7broke &d{count} &7blocks.'
  # The text or value for Chop Break. Available options: Any valid string text
  CHOP-BREAK: '{prefix}&#BDC3C7&dAmethyst Tree Chopper &7chopped &d{count} &7logs.'
  # The text or value for Sell Success. Available options: Any valid string text
  SELL-SUCCESS: '{prefix}&#BDC3C7Sold all chest contents for &a${amount}&7.'
  # The text or value for Sell Empty. Available options: Any valid string text
  SELL-EMPTY: '{prefix}&#BDC3C7That chest is empty or has no sellable items.'
  # The text or value for Sell No Chest. Available options: Any valid string text
  SELL-NO-CHEST: '{prefix}&#BDC3C7You must right-click a chest.'
  # The text or value for Bucket Drain. Available options: Any valid string text
  BUCKET-DRAIN: '{prefix}&#BDC3C7Drained &d{count} &7water blocks.'
  # The text or value for Bucket No Water. Available options: Any valid string text
  BUCKET-NO-WATER: '{prefix}&#BDC3C7No water blocks found nearby.'
  # The text or value for Booster Activated. Available options: Any valid string text
  BOOSTER-ACTIVATED: '{prefix}&#BDC3C7&dShard Booster &7activated! &d4x &7shards for
    &d60 minutes&7.'
  # The text or value for Booster Already. Available options: Any valid string text
  BOOSTER-ALREADY: '{prefix}&#BDC3C7You already have an active shard booster!'
  # The text or value for No Permission. Available options: Any valid string text
  NO-PERMISSION: '{prefix}&#BDC3C7You do not have permission to use this item.'
  # The text or value for Wrong Owner. Available options: Any valid string text
  WRONG-OWNER: '{prefix}&#BDC3C7This Amethyst Tool is bound to another player.'
  # The text or value for Give Success. Available options: Any valid string text
  GIVE-SUCCESS: '{prefix}&#BDC3C7Gave &d{type} &7to &d{player}&7.'
  # The text or value for Give Usage. Available options: Any valid string text
  GIVE-USAGE: '{prefix}&#BDC3C7Usage: &d/amethysttool give <player> <type> [duration_seconds]'
  # The text or value for Give Invalid Type. Available options: Any valid string text
  GIVE-INVALID-TYPE: '{prefix}&#BDC3C7Invalid tool type. Types: DRILL, CHOPPER, SELL_AXE,
    SHOVEL, BUCKET, SHARD_BOOSTER'
  # The text or value for Reload Success. Available options: Any valid string text
  RELOAD-SUCCESS: '{prefix}&#BDC3C7Configuration reloaded.'
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `AMETHYST-MESSAGES.PREFIX` | `str` | Any string text | `'&#9B59B6[Amethyst] &r'` | Configures the technical `PREFIX` parameter for `AMETHYST-MESSAGES.PREFIX` in `amethyst-tools.yml`. |
| `AMETHYST-MESSAGES.EXPIRED` | `str` | Any string text | `'{prefix}&#BDC3C7Your &d{tool} &7has...'` | Configures the technical `EXPIRED` parameter for `AMETHYST-MESSAGES.EXPIRED` in `amethyst-tools.yml`. |
| `AMETHYST-MESSAGES.EXCLUDED-WORLD` | `str` | Any string text | `'{prefix}&#BDC3C7Amethyst Tools cann...'` | Configures the technical `EXCLUDED-WORLD` parameter for `AMETHYST-MESSAGES.EXCLUDED-WORLD` in `amethyst-tools.yml`. |
| `AMETHYST-MESSAGES.DRILL-BREAK` | `str` | Any string text | `'{prefix}&#BDC3C7&dAmethyst Drill &7...'` | Configures the technical `DRILL-BREAK` parameter for `AMETHYST-MESSAGES.DRILL-BREAK` in `amethyst-tools.yml`. |
| `AMETHYST-MESSAGES.CHOP-BREAK` | `str` | Any string text | `'{prefix}&#BDC3C7&dAmethyst Tree Cho...'` | Configures the technical `CHOP-BREAK` parameter for `AMETHYST-MESSAGES.CHOP-BREAK` in `amethyst-tools.yml`. |
| `AMETHYST-MESSAGES.SELL-SUCCESS` | `str` | Any string text | `'{prefix}&#BDC3C7Sold all chest cont...'` | Configures the technical `SELL-SUCCESS` parameter for `AMETHYST-MESSAGES.SELL-SUCCESS` in `amethyst-tools.yml`. |
| `AMETHYST-MESSAGES.SELL-EMPTY` | `str` | Any string text | `'{prefix}&#BDC3C7That chest is empty...'` | Configures the technical `SELL-EMPTY` parameter for `AMETHYST-MESSAGES.SELL-EMPTY` in `amethyst-tools.yml`. |
| `AMETHYST-MESSAGES.SELL-NO-CHEST` | `str` | Any string text | `'{prefix}&#BDC3C7You must right-clic...'` | Configures the technical `SELL-NO-CHEST` parameter for `AMETHYST-MESSAGES.SELL-NO-CHEST` in `amethyst-tools.yml`. |
| `AMETHYST-MESSAGES.BUCKET-DRAIN` | `str` | Any string text | `'{prefix}&#BDC3C7Drained &d{count} &...'` | Configures the technical `BUCKET-DRAIN` parameter for `AMETHYST-MESSAGES.BUCKET-DRAIN` in `amethyst-tools.yml`. |
| `AMETHYST-MESSAGES.BUCKET-NO-WATER` | `str` | Any string text | `'{prefix}&#BDC3C7No water blocks fou...'` | Configures the technical `BUCKET-NO-WATER` parameter for `AMETHYST-MESSAGES.BUCKET-NO-WATER` in `amethyst-tools.yml`. |
| `AMETHYST-MESSAGES.BOOSTER-ACTIVATED` | `str` | Any string text | `'{prefix}&#BDC3C7&dShard Booster &7a...'` | Configures the technical `BOOSTER-ACTIVATED` parameter for `AMETHYST-MESSAGES.BOOSTER-ACTIVATED` in `amethyst-tools.yml`. |
| `AMETHYST-MESSAGES.BOOSTER-ALREADY` | `str` | Any string text | `'{prefix}&#BDC3C7You already have an...'` | Configures the technical `BOOSTER-ALREADY` parameter for `AMETHYST-MESSAGES.BOOSTER-ALREADY` in `amethyst-tools.yml`. |
| `AMETHYST-MESSAGES.NO-PERMISSION` | `str` | Any string text | `'{prefix}&#BDC3C7You do not have per...'` | Configures the technical `NO-PERMISSION` parameter for `AMETHYST-MESSAGES.NO-PERMISSION` in `amethyst-tools.yml`. |
| `AMETHYST-MESSAGES.WRONG-OWNER` | `str` | Any string text | `'{prefix}&#BDC3C7This Amethyst Tool ...'` | Configures the technical `WRONG-OWNER` parameter for `AMETHYST-MESSAGES.WRONG-OWNER` in `amethyst-tools.yml`. |
| `AMETHYST-MESSAGES.GIVE-SUCCESS` | `str` | Any string text | `'{prefix}&#BDC3C7Gave &d{type} &7to ...'` | Configures the technical `GIVE-SUCCESS` parameter for `AMETHYST-MESSAGES.GIVE-SUCCESS` in `amethyst-tools.yml`. |
| `AMETHYST-MESSAGES.GIVE-USAGE` | `str` | Any string text | `'{prefix}&#BDC3C7Usage: &d/amethystt...'` | Configures the technical `GIVE-USAGE` parameter for `AMETHYST-MESSAGES.GIVE-USAGE` in `amethyst-tools.yml`. |
| `AMETHYST-MESSAGES.GIVE-INVALID-TYPE` | `str` | Any string text | `'{prefix}&#BDC3C7Invalid tool type. ...'` | Configures the technical `GIVE-INVALID-TYPE` parameter for `AMETHYST-MESSAGES.GIVE-INVALID-TYPE` in `amethyst-tools.yml`. |
| `AMETHYST-MESSAGES.RELOAD-SUCCESS` | `str` | Any string text | `'{prefix}&#BDC3C7Configuration reloa...'` | Configures the technical `RELOAD-SUCCESS` parameter for `AMETHYST-MESSAGES.RELOAD-SUCCESS` in `amethyst-tools.yml`. |

### 3. Practical Setup Example

```yaml
AMETHYST-MESSAGES:
  # The text or value for Prefix. Available options: Any valid string text
  PREFIX: '&#9B59B6[Amethyst] &r'
  # The text or value for Expired. Available options: Any valid string text
  EXPIRED: '{prefix}&#BDC3C7Your &d{tool} &7has &cexpired &7and self-destructed!'
  # The text or value for Excluded World. Available options: Any valid string text
  EXCLUDED-WORLD: '{prefix}&#BDC3C7Amethyst Tools cannot be used in this world.'
  # The text or value for Drill Break. Available options: Any valid string text
  DRILL-BREAK: '{prefix}&#BDC3C7&dAmethyst Drill &7broke &d{count} &7blocks.'
  # The text or value for Chop Break. Available options: Any valid string text
  CHOP-BREAK: '{prefix}&#BDC3C7&dAmethyst Tree Chopper &7chopped &d{count} &7logs.'
  # The text or value for Sell Success. Available options: Any valid string text
  SELL-SUCCESS: '{prefix}&#BDC3C7Sold all chest contents for &a${amount}&7.'
  # The text or value for Sell Empty. Available options: Any valid stri
```

---

