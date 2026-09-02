# Detailed Configuration & Setup Guide: `orders.yml`

This is the official technical setup guide for `orders.yml` in **UltimateDonutSMP**.
Each section details the exact commented setup code block, allowed option values, data types, default values, and in-depth functional behavior.

Three sections have no write-up here yet: `SEARCH_SIGN`, `AMOUNT_SIGN` and `PRICE_SIGN`. Each sets
the four lines of the sign an order menu opens when it asks for a search term, an amount or a price,
plus the `input-line` that decides which line the player types on.

---

## Section: `SETTINGS`

### 1. Commented Setup Code Example

```yaml
SETTINGS:
  # Enable or disable the Orders system globally (true / false)
  ENABLED: true
  # Duration in hours before an active order expires automatically
  ORDER_DURATION_HOURS: 168
  # Default maximum active orders a player can create simultaneously
  MAX_ACTIVE_ORDERS_DEFAULT: 5
  # Maximum active order limits granted by permission node
  MAX_ACTIVE_ORDERS_BY_PERMISSION:
    ultimatedonutsmp.orders.limit.10: 10
    ultimatedonutsmp.orders.limit.15: 15
  # Maximum quantity of items requested per single order listing
  MAX_QUANTITY_PER_ORDER: 2304
  # Anti-spam click cooldown between menu interactions (in milliseconds)
  CLICK_COOLDOWN_MS: 750
  # Interval in seconds to check for expired orders
  EXPIRE_CHECK_SECONDS: 30

# Pricing, budget, and fee limits
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `SETTINGS.ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `SETTINGS` system. Set to `true` to enable, `false` to disable. |
| `SETTINGS.ORDER_DURATION_HOURS` | `int` | Any valid integer number | `'168'` | Configures the technical `ORDER_DURATION_HOURS` parameter for `SETTINGS.ORDER_DURATION_HOURS` in `orders.yml`. |
| `SETTINGS.MAX_ACTIVE_ORDERS_DEFAULT` | `int` | Any valid integer number | `'5'` | Configures the technical `MAX_ACTIVE_ORDERS_DEFAULT` parameter for `SETTINGS.MAX_ACTIVE_ORDERS_DEFAULT` in `orders.yml`. |
| `SETTINGS.MAX_ACTIVE_ORDERS_BY_PERMISSION.ultimatedonutsmp.orders.limit.10` | `int` | Any valid integer number | `'10'` | Configures the technical `10` parameter for `SETTINGS.MAX_ACTIVE_ORDERS_BY_PERMISSION.ultimatedonutsmp.orders.limit.10` in `orders.yml`. |
| `SETTINGS.MAX_ACTIVE_ORDERS_BY_PERMISSION.ultimatedonutsmp.orders.limit.15` | `int` | Any valid integer number | `'15'` | Configures the technical `15` parameter for `SETTINGS.MAX_ACTIVE_ORDERS_BY_PERMISSION.ultimatedonutsmp.orders.limit.15` in `orders.yml`. |
| `SETTINGS.MAX_QUANTITY_PER_ORDER` | `int` | Any valid integer number | `'2304'` | Configures the technical `MAX_QUANTITY_PER_ORDER` parameter for `SETTINGS.MAX_QUANTITY_PER_ORDER` in `orders.yml`. |
| `SETTINGS.CLICK_COOLDOWN_MS` | `int` | Any valid integer number | `'750'` | Configures the technical `CLICK_COOLDOWN_MS` parameter for `SETTINGS.CLICK_COOLDOWN_MS` in `orders.yml`. |
| `SETTINGS.EXPIRE_CHECK_SECONDS` | `int` | Any valid integer number | `'30'` | Configures the technical `EXPIRE_CHECK_SECONDS` parameter for `SETTINGS.EXPIRE_CHECK_SECONDS` in `orders.yml`. |

### 3. Practical Setup Example

```yaml
SETTINGS:
  # Enable or disable the Orders system globally (true / false)
  ENABLED: true
  # Duration in hours before an active order expires automatically
  ORDER_DURATION_HOURS: 168
  # Default maximum active orders a player can create simultaneously
  MAX_ACTIVE_ORDERS_DEFAULT: 5
  # Maximum active order limits granted by permission node
  MAX_ACTIVE_ORDERS_BY_PERMISSION:
    ultimatedonutsmp.orders.limit.10: 10
    ultimatedonutsmp.orders.limit.15: 15
  # Maximum quantity of items requested per single order listing
  MAX_QUANTITY_PER_ORDER: 2304
  # Anti-spam click cooldown between menu interactions (in milliseconds)
  CLICK_COOLDOWN_MS: 750
  # Interval in seconds to check for expired orders
  EXPIRE_CHECK_SECONDS: 30

# Pricing, budget, and fee limits
```

---

## Section: `PRICING`

### 1. Commented Setup Code Example

```yaml
PRICING:
  # Minimum price per individual item offered in an order
  MIN_PRICE_EACH: 10
  # Maximum price per individual item offered in an order
  MAX_PRICE_EACH: 1000000
  # Maximum total money budget allowed for a single order listing
  MAX_TOTAL_BUDGET: 250000000
  # Flat creation fee charged when listing a new item order
  ORDER_CREATION_FEE: 0

# Delivery system configuration
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `PRICING.MIN_PRICE_EACH` | `int` | Any valid integer number | `'10'` | Configures the technical `MIN_PRICE_EACH` parameter for `PRICING.MIN_PRICE_EACH` in `orders.yml`. |
| `PRICING.MAX_PRICE_EACH` | `int` | Any valid integer number | `'1000000'` | Configures the technical `MAX_PRICE_EACH` parameter for `PRICING.MAX_PRICE_EACH` in `orders.yml`. |
| `PRICING.MAX_TOTAL_BUDGET` | `int` | Any valid integer number | `'250000000'` | Configures the technical `MAX_TOTAL_BUDGET` parameter for `PRICING.MAX_TOTAL_BUDGET` in `orders.yml`. |
| `PRICING.ORDER_CREATION_FEE` | `int` | Any valid integer number | `'0'` | Configures the technical `ORDER_CREATION_FEE` parameter for `PRICING.ORDER_CREATION_FEE` in `orders.yml`. |

### 3. Practical Setup Example

```yaml
PRICING:
  # Minimum price per individual item offered in an order
  MIN_PRICE_EACH: 10
  # Maximum price per individual item offered in an order
  MAX_PRICE_EACH: 1000000
  # Maximum total money budget allowed for a single order listing
  MAX_TOTAL_BUDGET: 250000000
  # Flat creation fee charged when listing a new item order
  ORDER_CREATION_FEE: 0

# Delivery system configuration
```

---

## Section: `DELIVERY`

### 1. Commented Setup Code Example

```yaml
DELIVERY:
  # Maximum item quantity delivered in a single menu click
  MAX_DELIVER_PER_CLICK: 64
  # Delivery interaction mode: DEPOSIT_GUI or DIRECT
  MODE: DEPOSIT_GUI
  # Maximum item quantity delivered per single transaction
  MAX_DELIVER_PER_TRANSACTION: 2304

# Item matching and restriction filters
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `DELIVERY.MAX_DELIVER_PER_CLICK` | `int` | Any valid integer number | `'64'` | Configures the technical `MAX_DELIVER_PER_CLICK` parameter for `DELIVERY.MAX_DELIVER_PER_CLICK` in `orders.yml`. |
| `DELIVERY.MODE` | `str` | Any string text | `'DEPOSIT_GUI'` | Configures the technical `MODE` parameter for `DELIVERY.MODE` in `orders.yml`. |
| `DELIVERY.MAX_DELIVER_PER_TRANSACTION` | `int` | Any valid integer number | `'2304'` | Configures the technical `MAX_DELIVER_PER_TRANSACTION` parameter for `DELIVERY.MAX_DELIVER_PER_TRANSACTION` in `orders.yml`. |

### 3. Practical Setup Example

```yaml
DELIVERY:
  # Maximum item quantity delivered in a single menu click
  MAX_DELIVER_PER_CLICK: 64
  # Delivery interaction mode: DEPOSIT_GUI or DIRECT
  MODE: DEPOSIT_GUI
  # Maximum item quantity delivered per single transaction
  MAX_DELIVER_PER_TRANSACTION: 2304

# Item matching and restriction filters
```

---

## Section: `MATCHING`

### 1. Commented Setup Code Example

```yaml
MATCHING:
  # Materials blocked from being requested via item orders
  BLOCKED_MATERIALS:
    - BEDROCK
    - BARRIER
    - COMMAND_BLOCK
    - CHAIN_COMMAND_BLOCK
    - REPEATING_COMMAND_BLOCK
    - STRUCTURE_BLOCK
    - STRUCTURE_VOID
    - JIGSAW
    - LIGHT

# Item categories shown in order creation & browser menus
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `MATCHING.BLOCKED_MATERIALS` | `list` | List of configured items/strings | `[BEDROCK, BARRIER, COMMAND_BLOCK...]` | Configures the technical `BLOCKED_MATERIALS` parameter for `MATCHING.BLOCKED_MATERIALS` in `orders.yml`. |

### 3. Practical Setup Example

```yaml
MATCHING:
  # Materials blocked from being requested via item orders
  BLOCKED_MATERIALS:
    - BEDROCK
    - BARRIER
    - COMMAND_BLOCK
    - CHAIN_COMMAND_BLOCK
    - REPEATING_COMMAND_BLOCK
    - STRUCTURE_BLOCK
    - STRUCTURE_VOID
    - JIGSAW
    - LIGHT

# Item categories shown in order creation & browser menus
```

---

## Section: `CATEGORY_FILTERS`

### 1. Commented Setup Code Example

```yaml
CATEGORY_FILTERS:
  ALL: []
  BLOCKS:
    - STONE
    - COBBLESTONE
    - DIRT
    - OAK_LOG
    - GLASS
    - OBSIDIAN
  TOOLS:
    - IRON_PICKAXE
    - DIAMOND_PICKAXE
    - DIAMOND_AXE
    - DIAMOND_SHOVEL
  FOOD:
    - APPLE
    - BREAD
    - COOKED_BEEF
    - COOKED_CHICKEN
    - GOLDEN_CARROT
  COMBAT:
    - ARROW
    - SHIELD
    - DIAMOND_SWORD
    - NETHERITE_SWORD
  POTIONS:
    - GLASS_BOTTLE
    - FERMENTED_SPIDER_EYE
    - BLAZE_POWDER
  BOOKS:
    - BOOK
    - BOOKSHELF
    - LECTERN
  INGREDIENTS:
    - WHEAT
    - SUGAR_CANE
    - BLAZE_ROD
    - ENDER_PEARL
    - SLIME_BALL
  UTILITIES:
    - HOPPER
    - PISTON
    - OBSERVER
    - REDSTONE
    - DISPENSER

# GUI Inventory Titles and Sizes
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `CATEGORY_FILTERS.ALL` | `list` | List of configured items/strings | `[]` | Configures the technical `ALL` parameter for `CATEGORY_FILTERS.ALL` in `orders.yml`. |
| `CATEGORY_FILTERS.BLOCKS` | `list` | List of configured items/strings | `[STONE, COBBLESTONE, DIRT...]` | Configures the technical `BLOCKS` parameter for `CATEGORY_FILTERS.BLOCKS` in `orders.yml`. |
| `CATEGORY_FILTERS.TOOLS` | `list` | List of configured items/strings | `[IRON_PICKAXE, DIAMOND_PICKAXE, DIAMOND_AXE...]` | Configures the technical `TOOLS` parameter for `CATEGORY_FILTERS.TOOLS` in `orders.yml`. |
| `CATEGORY_FILTERS.FOOD` | `list` | List of configured items/strings | `[APPLE, BREAD, COOKED_BEEF...]` | Configures the technical `FOOD` parameter for `CATEGORY_FILTERS.FOOD` in `orders.yml`. |
| `CATEGORY_FILTERS.COMBAT` | `list` | List of configured items/strings | `[ARROW, SHIELD, DIAMOND_SWORD...]` | Configures the technical `COMBAT` parameter for `CATEGORY_FILTERS.COMBAT` in `orders.yml`. |
| `CATEGORY_FILTERS.POTIONS` | `list` | List of configured items/strings | `['GLASS_BOTTLE', 'FERMENTED_SPIDER_EYE', 'BLAZE_POWDER']` | Configures the technical `POTIONS` parameter for `CATEGORY_FILTERS.POTIONS` in `orders.yml`. |
| `CATEGORY_FILTERS.BOOKS` | `list` | List of configured items/strings | `['BOOK', 'BOOKSHELF', 'LECTERN']` | Configures the technical `BOOKS` parameter for `CATEGORY_FILTERS.BOOKS` in `orders.yml`. |
| `CATEGORY_FILTERS.INGREDIENTS` | `list` | List of configured items/strings | `[WHEAT, SUGAR_CANE, BLAZE_ROD...]` | Configures the technical `INGREDIENTS` parameter for `CATEGORY_FILTERS.INGREDIENTS` in `orders.yml`. |
| `CATEGORY_FILTERS.UTILITIES` | `list` | List of configured items/strings | `[HOPPER, PISTON, OBSERVER...]` | Configures the technical `UTILITIES` parameter for `CATEGORY_FILTERS.UTILITIES` in `orders.yml`. |

### 3. Practical Setup Example

```yaml
CATEGORY_FILTERS:
  ALL: []
  BLOCKS:
    - STONE
    - COBBLESTONE
    - DIRT
    - OAK_LOG
    - GLASS
    - OBSIDIAN
  TOOLS:
    - IRON_PICKAXE
    - DIAMOND_PICKAXE
    - DIAMOND_AXE
    - DIAMOND_SHOVEL
  FOOD:
    - APPLE
    - BREAD
    - COOKED_BEEF
    - COOKED_CHICKEN
    - GOLDEN_CARROT
  COMBAT:
    - ARROW
    - SHIELD
    - DIAMOND_SWORD
    - NETHERITE_SWORD
  POTIONS:
    - GLASS_BOTTLE
    - FERMENTED_SPIDER_EYE
    - BLAZE_POWDER
  BOOKS:
    - BOOK
    - BOOKSHELF
    - LECTERN
  INGREDIENTS:
    - WHEAT
    - SUGAR_CANE
    - BLAZE_ROD
    - ENDER_PEARL
    - SLIME_BALL
  UTILITIES:
    - HOPPER
    - PISTON
    - OBSERVER
    - REDSTONE
    - DISPENSER

# GUI Inventory Titles and Sizes
```

---

## Section: `GUI`

### 1. Commented Setup Code Example

```yaml
GUI:
  MAIN:
    TITLE: '&8Orders'
    SIZE: 54
    ITEMS_PER_PAGE: 45
  MY_ORDERS:
    TITLE: '&8Orders -> My Orders'
    SIZE: 27
    ITEMS_PER_PAGE: 45
    BUTTONS:
      NEW:
        SLOT: 26
  NEW_ORDER:
    TITLE: '&8Orders -> New Order'
    SIZE: 27
  SELECT_ITEM:
    TITLE: '&8Orders -> Select Item'
    SIZE: 54
    ITEMS_PER_PAGE: 45
  EDIT_ORDER:
    TITLE: '&8Orders -> Edit Order'
    SIZE: 27
  DELIVER_CONFIRM:
    TITLE: '&8Orders -> Deliver'
    SIZE: 27
  COLLECT:
    TITLE: '&8Orders -> Collect'
    SIZE: 54
    ITEMS_PER_PAGE: 45
  DELIVERY_DEPOSIT:
    BUTTONS:
      CONFIRM:
        SLOT: 35

# Order sorting options in GUI
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `GUI.MAIN.TITLE` | `str` | Any string text | `'&8Orders'` | Configures the technical `TITLE` parameter for `GUI.MAIN.TITLE` in `orders.yml`. |
| `GUI.MAIN.SIZE` | `int` | Any valid integer number | `'54'` | Configures the technical `SIZE` parameter for `GUI.MAIN.SIZE` in `orders.yml`. |
| `GUI.MAIN.ITEMS_PER_PAGE` | `int` | Any valid integer number | `'45'` | Configures the technical `ITEMS_PER_PAGE` parameter for `GUI.MAIN.ITEMS_PER_PAGE` in `orders.yml`. |
| `GUI.MY_ORDERS.TITLE` | `str` | Any string text | `'&8Orders -> My Orders'` | Configures the technical `TITLE` parameter for `GUI.MY_ORDERS.TITLE` in `orders.yml`. |
| `GUI.MY_ORDERS.SIZE` | `int` | Any valid integer number | `'27'` | Configures the technical `SIZE` parameter for `GUI.MY_ORDERS.SIZE` in `orders.yml`. |
| `GUI.MY_ORDERS.ITEMS_PER_PAGE` | `int` | Any valid integer number | `'45'` | Configures the technical `ITEMS_PER_PAGE` parameter for `GUI.MY_ORDERS.ITEMS_PER_PAGE` in `orders.yml`. |
| `GUI.MY_ORDERS.BUTTONS.NEW.SLOT` | `int` | Any valid integer number | `'26'` | Configures the technical `SLOT` parameter for `GUI.MY_ORDERS.BUTTONS.NEW.SLOT` in `orders.yml`. |
| `GUI.NEW_ORDER.TITLE` | `str` | Any string text | `'&8Orders -> New Order'` | Configures the technical `TITLE` parameter for `GUI.NEW_ORDER.TITLE` in `orders.yml`. |
| `GUI.NEW_ORDER.SIZE` | `int` | Any valid integer number | `'27'` | Configures the technical `SIZE` parameter for `GUI.NEW_ORDER.SIZE` in `orders.yml`. |
| `GUI.SELECT_ITEM.TITLE` | `str` | Any string text | `'&8Orders -> Select Item'` | Configures the technical `TITLE` parameter for `GUI.SELECT_ITEM.TITLE` in `orders.yml`. |
| `GUI.SELECT_ITEM.SIZE` | `int` | Any valid integer number | `'54'` | Configures the technical `SIZE` parameter for `GUI.SELECT_ITEM.SIZE` in `orders.yml`. |
| `GUI.SELECT_ITEM.ITEMS_PER_PAGE` | `int` | Any valid integer number | `'45'` | Configures the technical `ITEMS_PER_PAGE` parameter for `GUI.SELECT_ITEM.ITEMS_PER_PAGE` in `orders.yml`. |
| `GUI.EDIT_ORDER.TITLE` | `str` | Any string text | `'&8Orders -> Edit Order'` | Configures the technical `TITLE` parameter for `GUI.EDIT_ORDER.TITLE` in `orders.yml`. |
| `GUI.EDIT_ORDER.SIZE` | `int` | Any valid integer number | `'27'` | Configures the technical `SIZE` parameter for `GUI.EDIT_ORDER.SIZE` in `orders.yml`. |
| `GUI.DELIVER_CONFIRM.TITLE` | `str` | Any string text | `'&8Orders -> Deliver'` | Configures the technical `TITLE` parameter for `GUI.DELIVER_CONFIRM.TITLE` in `orders.yml`. |
| `GUI.DELIVER_CONFIRM.SIZE` | `int` | Any valid integer number | `'27'` | Configures the technical `SIZE` parameter for `GUI.DELIVER_CONFIRM.SIZE` in `orders.yml`. |
| `GUI.COLLECT.TITLE` | `str` | Any string text | `'&8Orders -> Collect'` | Configures the technical `TITLE` parameter for `GUI.COLLECT.TITLE` in `orders.yml`. |
| `GUI.COLLECT.SIZE` | `int` | Any valid integer number | `'54'` | Configures the technical `SIZE` parameter for `GUI.COLLECT.SIZE` in `orders.yml`. |
| `GUI.COLLECT.ITEMS_PER_PAGE` | `int` | Any valid integer number | `'45'` | Configures the technical `ITEMS_PER_PAGE` parameter for `GUI.COLLECT.ITEMS_PER_PAGE` in `orders.yml`. |
| `GUI.DELIVERY_DEPOSIT.BUTTONS.CONFIRM.SLOT` | `int` | Any valid integer number | `'35'` | Configures the technical `SLOT` parameter for `GUI.DELIVERY_DEPOSIT.BUTTONS.CONFIRM.SLOT` in `orders.yml`. |

### 3. Practical Setup Example

```yaml
GUI:
  MAIN:
    TITLE: '&8Orders'
    SIZE: 54
    ITEMS_PER_PAGE: 45
  MY_ORDERS:
    TITLE: '&8Orders -> My Orders'
    SIZE: 27
    ITEMS_PER_PAGE: 45
    BUTTONS:
      NEW:
        SLOT: 26
  NEW_ORDER:
    TITLE: '&8Orders -> New Order'
    SIZE: 27
  SELECT_ITEM:
    TITLE: '&8Orders -> Select Item'
    SIZE: 54
    ITEMS_PER_PAGE: 45
  EDIT_ORDER:
    TITLE: '&8Orders -> Edit Order'
    SIZE: 27
  DELIVER_CONFIRM:
    TITLE: '&8Orders -> Deliver'
    SIZE: 27
  COLLECT:
    TITLE: '&8Orders -> Collect'
    SIZE: 54
    ITEMS_PER_PAGE: 45
  DELIVERY_DEPOSIT:
    BUTTONS:
      CONFIRM:
        SLOT: 35

# Order sorting options in GUI
```

---

## Section: `SORTING`

### 1. Commented Setup Code Example

```yaml
SORTING:
  # Default sorting: MOST_PAID, MOST_DELIVERED, RECENTLY_LISTED, MOST_MONEY_PER_ITEM
  DEFAULT: MOST_PAID
  ALLOWED:
    - MOST_PAID
    - MOST_DELIVERED
    - RECENTLY_LISTED
    - MOST_MONEY_PER_ITEM

# Visual layout style mode
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `SORTING.DEFAULT` | `str` | Any string text | `'MOST_PAID'` | Configures the technical `DEFAULT` parameter for `SORTING.DEFAULT` in `orders.yml`. |
| `SORTING.ALLOWED` | `list` | List of configured items/strings | `[MOST_PAID, MOST_DELIVERED, RECENTLY_LISTED...]` | Configures the technical `ALLOWED` parameter for `SORTING.ALLOWED` in `orders.yml`. |

### 3. Practical Setup Example

```yaml
SORTING:
  # Default sorting: MOST_PAID, MOST_DELIVERED, RECENTLY_LISTED, MOST_MONEY_PER_ITEM
  DEFAULT: MOST_PAID
  ALLOWED:
    - MOST_PAID
    - MOST_DELIVERED
    - RECENTLY_LISTED
    - MOST_MONEY_PER_ITEM

# Visual layout style mode
```

---

## Section: `DONUT-STYLE`

### 1. Commented Setup Code Example

```yaml
DONUT-STYLE: DEPOSIT_GUI

# Geyser/Floodgate Bedrock player compatibility
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `DONUT-STYLE` | `str` | Any string text | `'DEPOSIT_GUI'` | Configures the technical `DONUT-STYLE` parameter for `DONUT-STYLE` in `orders.yml`. |

### 3. Practical Setup Example

```yaml
DONUT-STYLE: DEPOSIT_GUI

# Geyser/Floodgate Bedrock player compatibility
```

---

## Section: `BEDROCK`

### 1. Commented Setup Code Example

```yaml
BEDROCK:
  ENABLED: true

# Cross-server Redis sync for orders
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `BEDROCK.ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `BEDROCK` system. Set to `true` to enable, `false` to disable. |

### 3. Practical Setup Example

```yaml
BEDROCK:
  ENABLED: true

# Cross-server Redis sync for orders
```

---

## Section: `NETWORK`

### 1. Commented Setup Code Example

```yaml
NETWORK:
  ENABLED: true
  REDIS_CHANNEL: ultimate-donut-smp:orders
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `NETWORK.ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `NETWORK` system. Set to `true` to enable, `false` to disable. |
| `NETWORK.REDIS_CHANNEL` | `str` | Any string text | `'ultimate-donut-smp:orders'` | Configures the technical `REDIS_CHANNEL` parameter for `NETWORK.REDIS_CHANNEL` in `orders.yml`. |

### 3. Practical Setup Example

```yaml
NETWORK:
  ENABLED: true
  REDIS_CHANNEL: ultimate-donut-smp:orders

---

## Section: `BOTS`

### 1. Commented Setup Code Example

```yaml
# Automated Order Bot System Configuration
BOTS:
  # Enable or disable automated bot item orders (true / false)
  ENABLED: false
  # Minimum interval in seconds between bot order checks
  MIN_CHECK_INTERVAL_SECONDS: 60
  # Maximum interval in seconds between bot order checks
  MAX_CHECK_INTERVAL_SECONDS: 300
  # Chance (0.0 to 1.0) for a bot to post an order on each interval check
  CHANCE: 0.5
  # Maximum active bot orders allowed concurrently
  MAX_ACTIVE_BOT_ORDERS: 10
  # Minimum duration in hours for bot order listings
  MIN_DURATION_HOURS: 24
  # Maximum duration in hours for bot order listings
  MAX_DURATION_HOURS: 72
  # List of bot names displayed as order buyers
  BOT_NAMES:
    - "OrderBot"
    - "BuyerBot"
    - "ItemCollector"
  # Items that bots can request in orders
  ITEMS:
    - MATERIAL: DIAMOND
      MIN_AMOUNT: 16
      MAX_AMOUNT: 64
      MIN_PRICE_EACH: 500
      MAX_PRICE_EACH: 1000
    - MATERIAL: OAK_LOG
      MIN_AMOUNT: 64
      MAX_AMOUNT: 512
      MIN_PRICE_EACH: 10
      MAX_PRICE_EACH: 25
    - MATERIAL: NETHERITE_INGOT
      MIN_AMOUNT: 1
      MAX_AMOUNT: 4
      MIN_PRICE_EACH: 25000
      MAX_PRICE_EACH: 50000
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `BOTS.ENABLED` | `bool` | `true`, `false` | `false` | Global toggle for automated bot item orders. |
| `BOTS.MIN_CHECK_INTERVAL_SECONDS` | `int` | Any positive integer | `60` | Minimum interval in seconds between automated bot execution ticks. |
| `BOTS.MAX_CHECK_INTERVAL_SECONDS` | `int` | Any positive integer | `300` | Maximum interval in seconds between automated bot execution ticks. |
| `BOTS.CHANCE` | `float` | `0.0` to `1.0` | `0.5` | Probability chance of a bot order being posted when interval check runs. |
| `BOTS.MAX_ACTIVE_BOT_ORDERS` | `int` | Any positive integer | `10` | Cap on simultaneous active item orders created by bots. |
| `BOTS.MIN_DURATION_HOURS` | `int` | Any positive integer | `24` | Minimum duration in hours for bot-generated item orders. |
| `BOTS.MAX_DURATION_HOURS` | `int` | Any positive integer | `72` | Maximum duration in hours for bot-generated item orders. |
| `BOTS.BOT_NAMES` | `list` | List of strings | `[OrderBot, BuyerBot, ItemCollector]` | List of buyer usernames randomly chosen for bot orders. |
| `BOTS.ITEMS` | `list` | List of maps | Configured item maps | List of item templates (material, amounts, price ranges per item) available for bot buy orders. |

### 3. Practical Setup Example

```yaml
BOTS:
  ENABLED: true
  MIN_CHECK_INTERVAL_SECONDS: 120
  MAX_CHECK_INTERVAL_SECONDS: 600
  CHANCE: 0.8
  MAX_ACTIVE_BOT_ORDERS: 15
  MIN_DURATION_HOURS: 24
  MAX_DURATION_HOURS: 96
  BOT_NAMES:
    - "ServerBuyer"
    - "ResourceCollector"
  ITEMS:
    - MATERIAL: DIAMOND
      MIN_AMOUNT: 32
      MAX_AMOUNT: 64
      MIN_PRICE_EACH: 600
      MAX_PRICE_EACH: 1200
```

```

---

