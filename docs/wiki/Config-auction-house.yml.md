# Detailed Configuration & Setup Guide: `auction-house.yml`

This is the official, 100% complete technical setup guide for `auction-house.yml` in **UltimateDonutSMP**.
Each section details the exact commented setup code block, allowed option values, data types, default values, and in-depth functional behavior.

---

## Section: `SETTINGS`

### 1. Commented Setup Code Example

```yaml
SETTINGS:
  # Enable or disable the Auction House system globally (true / false)
  ENABLED: true
  # Duration in hours before an unpurchased auction listing expires
  LISTING_DURATION_HOURS: 48
  # Default maximum active listings a player can post simultaneously
  MAX_ACTIVE_LISTINGS_DEFAULT: 5
  # Maximum active listing limits granted by permission node
  MAX_ACTIVE_LISTINGS_BY_PERMISSION:
    ultimatedonutsmp.auctionhouse.limit.10: 10
    ultimatedonutsmp.auctionhouse.limit.15: 15
  # Anti-spam click cooldown between menu interactions (in milliseconds)
  CLICK_COOLDOWN_MS: 750
  # Interval in seconds to check for expired auction listings
  EXPIRE_CHECK_SECONDS: 30

# Pricing, fees, and tax limits
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `SETTINGS.ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `SETTINGS` system. Set to `true` to enable, `false` to disable. |
| `SETTINGS.LISTING_DURATION_HOURS` | `int` | Any valid integer number | `'48'` | Configures the technical `LISTING_DURATION_HOURS` parameter for `SETTINGS.LISTING_DURATION_HOURS` in `auction-house.yml`. |
| `SETTINGS.MAX_ACTIVE_LISTINGS_DEFAULT` | `int` | Any valid integer number | `'5'` | Configures the technical `MAX_ACTIVE_LISTINGS_DEFAULT` parameter for `SETTINGS.MAX_ACTIVE_LISTINGS_DEFAULT` in `auction-house.yml`. |
| `SETTINGS.MAX_ACTIVE_LISTINGS_BY_PERMISSION.ultimatedonutsmp.auctionhouse.limit.10` | `int` | Any valid integer number | `'10'` | Configures the technical `10` parameter for `SETTINGS.MAX_ACTIVE_LISTINGS_BY_PERMISSION.ultimatedonutsmp.auctionhouse.limit.10` in `auction-house.yml`. |
| `SETTINGS.MAX_ACTIVE_LISTINGS_BY_PERMISSION.ultimatedonutsmp.auctionhouse.limit.15` | `int` | Any valid integer number | `'15'` | Configures the technical `15` parameter for `SETTINGS.MAX_ACTIVE_LISTINGS_BY_PERMISSION.ultimatedonutsmp.auctionhouse.limit.15` in `auction-house.yml`. |
| `SETTINGS.CLICK_COOLDOWN_MS` | `int` | Any valid integer number | `'750'` | Configures the technical `CLICK_COOLDOWN_MS` parameter for `SETTINGS.CLICK_COOLDOWN_MS` in `auction-house.yml`. |
| `SETTINGS.EXPIRE_CHECK_SECONDS` | `int` | Any valid integer number | `'30'` | Configures the technical `EXPIRE_CHECK_SECONDS` parameter for `SETTINGS.EXPIRE_CHECK_SECONDS` in `auction-house.yml`. |

### 3. Practical Setup Example

```yaml
SETTINGS:
  # Enable or disable the Auction House system globally (true / false)
  ENABLED: true
  # Duration in hours before an unpurchased auction listing expires
  LISTING_DURATION_HOURS: 48
  # Default maximum active listings a player can post simultaneously
  MAX_ACTIVE_LISTINGS_DEFAULT: 5
  # Maximum active listing limits granted by permission node
  MAX_ACTIVE_LISTINGS_BY_PERMISSION:
    ultimatedonutsmp.auctionhouse.limit.10: 10
    ultimatedonutsmp.auctionhouse.limit.15: 15
  # Anti-spam click cooldown between menu interactions (in milliseconds)
  CLICK_COOLDOWN_MS: 750
  # Interval in seconds to check for expired auction listings
  EXPIRE_CHECK_SECONDS: 30

# Pricing, fees, and tax limits
```

---

## Section: `PRICING`

### 1. Commented Setup Code Example

```yaml
PRICING:
  # Minimum allowed listing price for an item
  MIN_PRICE: 100
  # Maximum allowed listing price for an item
  MAX_PRICE: 100000000
  # Flat fee charged upfront to create an auction listing
  LISTING_FEE: 0
  # Percentage tax deducted from the final sale price upon purchase (%)
  TAX_PERCENT: 5.0

# Item restrictions and blacklist
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `PRICING.MIN_PRICE` | `int` | Any valid integer number | `'100'` | Configures the technical `MIN_PRICE` parameter for `PRICING.MIN_PRICE` in `auction-house.yml`. |
| `PRICING.MAX_PRICE` | `int` | Any valid integer number | `'100000000'` | Configures the technical `MAX_PRICE` parameter for `PRICING.MAX_PRICE` in `auction-house.yml`. |
| `PRICING.LISTING_FEE` | `int` | Any valid integer number | `'0'` | Configures the technical `LISTING_FEE` parameter for `PRICING.LISTING_FEE` in `auction-house.yml`. |
| `PRICING.TAX_PERCENT` | `float` | Any decimal number | `'5.0'` | Configures the technical `TAX_PERCENT` parameter for `PRICING.TAX_PERCENT` in `auction-house.yml`. |

### 3. Practical Setup Example

```yaml
PRICING:
  # Minimum allowed listing price for an item
  MIN_PRICE: 100
  # Maximum allowed listing price for an item
  MAX_PRICE: 100000000
  # Flat fee charged upfront to create an auction listing
  LISTING_FEE: 0
  # Percentage tax deducted from the final sale price upon purchase (%)
  TAX_PERCENT: 5.0

# Item restrictions and blacklist
```

---

## Section: `RESTRICTIONS`

### 1. Commented Setup Code Example

```yaml
RESTRICTIONS:
  # Materials blocked from being listed on the Auction House
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
  # Lore text phrases that block an item from being listed if present
  BLOCKED_IF_HAS_LORE_CONTAINS: []

# Claims system configuration
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `RESTRICTIONS.BLOCKED_MATERIALS` | `list` | List of configured items/strings | `[BEDROCK, BARRIER, COMMAND_BLOCK...]` | Configures the technical `BLOCKED_MATERIALS` parameter for `RESTRICTIONS.BLOCKED_MATERIALS` in `auction-house.yml`. |
| `RESTRICTIONS.BLOCKED_IF_HAS_LORE_CONTAINS` | `list` | List of configured items/strings | `[]` | Configures the technical `BLOCKED_IF_HAS_LORE_CONTAINS` parameter for `RESTRICTIONS.BLOCKED_IF_HAS_LORE_CONTAINS` in `auction-house.yml`. |

### 3. Practical Setup Example

```yaml
RESTRICTIONS:
  # Materials blocked from being listed on the Auction House
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
  # Lore text phrases that block an item from being listed if present
  BLOCKED_IF_HAS_LORE_CONTAINS: []

# Claims system configuration
```

---

## Section: `CLAIMS`

### 1. Commented Setup Code Example

```yaml
CLAIMS:
  # Enable or disable auction claims collection system (true / false)
  ENABLED: true

# GUI Inventory Titles and Sizes
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `CLAIMS.ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `CLAIMS` system. Set to `true` to enable, `false` to disable. |

### 3. Practical Setup Example

```yaml
CLAIMS:
  # Enable or disable auction claims collection system (true / false)
  ENABLED: true

# GUI Inventory Titles and Sizes
```

---

## Section: `GUI`

### 1. Commented Setup Code Example

```yaml
GUI:
  BROWSE:
    TITLE: '&8Auction House'
    SIZE: 54
    ITEMS_PER_PAGE: 45
  MY_LISTINGS:
    TITLE: '&8My Auctions'
    SIZE: 54
    ITEMS_PER_PAGE: 45
  CLAIMS:
    TITLE: '&8Auction Claims'
    SIZE: 54
    ITEMS_PER_PAGE: 45

# Auction sorting configuration
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `GUI.BROWSE.TITLE` | `str` | Any string text | `'&8Auction House'` | Configures the technical `TITLE` parameter for `GUI.BROWSE.TITLE` in `auction-house.yml`. |
| `GUI.BROWSE.SIZE` | `int` | Any valid integer number | `'54'` | Configures the technical `SIZE` parameter for `GUI.BROWSE.SIZE` in `auction-house.yml`. |
| `GUI.BROWSE.ITEMS_PER_PAGE` | `int` | Any valid integer number | `'45'` | Configures the technical `ITEMS_PER_PAGE` parameter for `GUI.BROWSE.ITEMS_PER_PAGE` in `auction-house.yml`. |
| `GUI.MY_LISTINGS.TITLE` | `str` | Any string text | `'&8My Auctions'` | Configures the technical `TITLE` parameter for `GUI.MY_LISTINGS.TITLE` in `auction-house.yml`. |
| `GUI.MY_LISTINGS.SIZE` | `int` | Any valid integer number | `'54'` | Configures the technical `SIZE` parameter for `GUI.MY_LISTINGS.SIZE` in `auction-house.yml`. |
| `GUI.MY_LISTINGS.ITEMS_PER_PAGE` | `int` | Any valid integer number | `'45'` | Configures the technical `ITEMS_PER_PAGE` parameter for `GUI.MY_LISTINGS.ITEMS_PER_PAGE` in `auction-house.yml`. |
| `GUI.CLAIMS.TITLE` | `str` | Any string text | `'&8Auction Claims'` | Configures the technical `TITLE` parameter for `GUI.CLAIMS.TITLE` in `auction-house.yml`. |
| `GUI.CLAIMS.SIZE` | `int` | Any valid integer number | `'54'` | Configures the technical `SIZE` parameter for `GUI.CLAIMS.SIZE` in `auction-house.yml`. |
| `GUI.CLAIMS.ITEMS_PER_PAGE` | `int` | Any valid integer number | `'45'` | Configures the technical `ITEMS_PER_PAGE` parameter for `GUI.CLAIMS.ITEMS_PER_PAGE` in `auction-house.yml`. |

### 3. Practical Setup Example

```yaml
GUI:
  BROWSE:
    TITLE: '&8Auction House'
    SIZE: 54
    ITEMS_PER_PAGE: 45
  MY_LISTINGS:
    TITLE: '&8My Auctions'
    SIZE: 54
    ITEMS_PER_PAGE: 45
  CLAIMS:
    TITLE: '&8Auction Claims'
    SIZE: 54
    ITEMS_PER_PAGE: 45

# Auction sorting configuration
```

---

## Section: `SORTING`

### 1. Commented Setup Code Example

```yaml
SORTING:
  # Default sorting method: NEWEST, PRICE_LOWEST, PRICE_HIGHEST, EXPIRING_SOON, OLDEST
  DEFAULT: NEWEST
  # List of allowed sorting options in the GUI
  ALLOWED:
    - NEWEST
    - PRICE_LOWEST
    - PRICE_HIGHEST
    - EXPIRING_SOON
    - OLDEST
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `SORTING.DEFAULT` | `str` | Any string text | `'NEWEST'` | Configures the technical `DEFAULT` parameter for `SORTING.DEFAULT` in `auction-house.yml`. |
| `SORTING.ALLOWED` | `list` | List of configured items/strings | `[NEWEST, PRICE_LOWEST, PRICE_HIGHEST...]` | Configures the technical `ALLOWED` parameter for `SORTING.ALLOWED` in `auction-house.yml`. |

### 3. Practical Setup Example

```yaml
SORTING:
  # Default sorting method: NEWEST, PRICE_LOWEST, PRICE_HIGHEST, EXPIRING_SOON, OLDEST
  DEFAULT: NEWEST
  # List of allowed sorting options in the GUI
  ALLOWED:
    - NEWEST
    - PRICE_LOWEST
    - PRICE_HIGHEST
    - EXPIRING_SOON
    - OLDEST

---

## Section: `BOTS`

### 1. Commented Setup Code Example

```yaml
# Automated Auction House Bot System Configuration
BOTS:
  # Enable or disable automated bot auction listings (true / false)
  ENABLED: false
  # Minimum interval in seconds between bot listing checks
  MIN_CHECK_INTERVAL_SECONDS: 60
  # Maximum interval in seconds between bot listing checks
  MAX_CHECK_INTERVAL_SECONDS: 300
  # Chance (0.0 to 1.0) for a bot to post a listing on each interval check
  CHANCE: 0.5
  # Maximum active bot listings allowed concurrently
  MAX_ACTIVE_BOT_LISTINGS: 10
  # Minimum duration in hours for bot auction listings
  MIN_DURATION_HOURS: 12
  # Maximum duration in hours for bot auction listings
  MAX_DURATION_HOURS: 48
  # List of bot names displayed as sellers
  BOT_NAMES:
    - "DonutBot"
    - "AuctionBot"
    - "ShopKeeper"
  # Items that bots can list on the Auction House
  ITEMS:
    - MATERIAL: DIAMOND_SWORD
      MIN_AMOUNT: 1
      MAX_AMOUNT: 1
      MIN_PRICE: 5000
      MAX_PRICE: 15000
      ENCHANTS:
        - "SHARPNESS:5"
        - "UNBREAKING:3"
    - MATERIAL: GOLDEN_APPLE
      MIN_AMOUNT: 4
      MAX_AMOUNT: 16
      MIN_PRICE: 2000
      MAX_PRICE: 8000
    - MATERIAL: COBBLESTONE
      MIN_AMOUNT: 64
      MAX_AMOUNT: 320
      MIN_PRICE: 100
      MAX_PRICE: 500
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `BOTS.ENABLED` | `bool` | `true`, `false` | `false` | Global toggle for automated bot auction listings. |
| `BOTS.MIN_CHECK_INTERVAL_SECONDS` | `int` | Any positive integer | `60` | Minimum interval in seconds between automated bot execution ticks. |
| `BOTS.MAX_CHECK_INTERVAL_SECONDS` | `int` | Any positive integer | `300` | Maximum interval in seconds between automated bot execution ticks. |
| `BOTS.CHANCE` | `float` | `0.0` to `1.0` | `0.5` | Probability chance of a bot listing being posted when interval check runs. |
| `BOTS.MAX_ACTIVE_BOT_LISTINGS` | `int` | Any positive integer | `10` | Cap on simultaneous active auction listings created by bots. |
| `BOTS.MIN_DURATION_HOURS` | `int` | Any positive integer | `12` | Minimum duration in hours for bot-generated auction listings. |
| `BOTS.MAX_DURATION_HOURS` | `int` | Any positive integer | `48` | Maximum duration in hours for bot-generated auction listings. |
| `BOTS.BOT_NAMES` | `list` | List of strings | `[DonutBot, AuctionBot, ShopKeeper]` | List of seller usernames randomly chosen for bot listings. |
| `BOTS.ITEMS` | `list` | List of maps | Configured item maps | List of item templates (material, amounts, price ranges, enchantments) available for bots to post. |

### 3. Practical Setup Example

```yaml
BOTS:
  ENABLED: true
  MIN_CHECK_INTERVAL_SECONDS: 120
  MAX_CHECK_INTERVAL_SECONDS: 600
  CHANCE: 0.75
  MAX_ACTIVE_BOT_LISTINGS: 15
  MIN_DURATION_HOURS: 24
  MAX_DURATION_HOURS: 48
  BOT_NAMES:
    - "ServerEconomyBot"
    - "DonutShop"
  ITEMS:
    - MATERIAL: DIAMOND_SWORD
      MIN_AMOUNT: 1
      MAX_AMOUNT: 1
      MIN_PRICE: 5000
      MAX_PRICE: 10000
```

```

---

