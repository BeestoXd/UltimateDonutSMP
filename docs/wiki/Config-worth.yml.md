# Detailed Configuration & Setup Guide: `worth.yml`

This is the official, 100% complete technical setup guide for `worth.yml` in **UltimateDonutSMP**.
Each section details the exact commented setup code block, allowed option values, data types, default values, and in-depth functional behavior.

---

## Section: `DISPLAY`

### 1. Commented Setup Code Example

```yaml
DISPLAY:
  # The text or value for Format. Available options: Any valid string text
  FORMAT: '&7Worth: &a$${price}'
# Configuration section for Container.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `DISPLAY.FORMAT` | `str` | Any string text | `'&7Worth: &a$${price}'` | Configures the technical `FORMAT` parameter for `DISPLAY.FORMAT` in `worth.yml`. |

### 3. Practical Setup Example

```yaml
DISPLAY:
  # The text or value for Format. Available options: Any valid string text
  FORMAT: '&7Worth: &a$${price}'
# Configuration section for Container.
```

---

## Section: `CONTAINER`

### 1. Commented Setup Code Example

```yaml
CONTAINER:
  # Determines whether Enabled is enabled or disabled. Available options: true, false
  ENABLED: true
  # Determines whether Include Container Base Price is enabled or disabled. Available options: true, false
  INCLUDE-CONTAINER-BASE-PRICE: true
  # Determines whether Allow Nested Containers is enabled or disabled. Available options: true, false
  ALLOW-NESTED-CONTAINERS: false
  # The numerical value for Max Container Depth. Available options: Any valid integer
  MAX-CONTAINER-DEPTH: 1
# Configuration section for Browser.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `CONTAINER.ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `CONTAINER` system. Set to `true` to enable, `false` to disable. |
| `CONTAINER.INCLUDE-CONTAINER-BASE-PRICE` | `bool` | `true`, `false` | `true` | Configures the technical `INCLUDE-CONTAINER-BASE-PRICE` parameter for `CONTAINER.INCLUDE-CONTAINER-BASE-PRICE` in `worth.yml`. |
| `CONTAINER.ALLOW-NESTED-CONTAINERS` | `bool` | `true`, `false` | `false` | Configures the technical `ALLOW-NESTED-CONTAINERS` parameter for `CONTAINER.ALLOW-NESTED-CONTAINERS` in `worth.yml`. |
| `CONTAINER.MAX-CONTAINER-DEPTH` | `int` | Any valid integer number | `'1'` | Configures the technical `MAX-CONTAINER-DEPTH` parameter for `CONTAINER.MAX-CONTAINER-DEPTH` in `worth.yml`. |

### 3. Practical Setup Example

```yaml
CONTAINER:
  # Determines whether Enabled is enabled or disabled. Available options: true, false
  ENABLED: true
  # Determines whether Include Container Base Price is enabled or disabled. Available options: true, false
  INCLUDE-CONTAINER-BASE-PRICE: true
  # Determines whether Allow Nested Containers is enabled or disabled. Available options: true, false
  ALLOW-NESTED-CONTAINERS: false
  # The numerical value for Max Container Depth. Available options: Any valid integer
  MAX-CONTAINER-DEPTH: 1
# Configuration section for Browser.
```

---

## Section: `BROWSER`

### 1. Commented Setup Code Example

```yaml
BROWSER:
  TITLE: '&8Item Prices'
  SIZE: 54
  # The numerical value for Items Per Page. Available options: Any valid integer
  ITEMS-PER-PAGE: 45
  # The text or value for Default Sort. Available options: Any valid string text
  DEFAULT-SORT: CATEGORY
  # Configuration section for Category Sort.
  CATEGORY-SORT:
  - CROPS
  - ORES
  - MOBS
  - NATURAL
  - ARMOR_AND_TOOLS
  - FISH
  - BOOK
  - POTION
  - BLOCKS
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `BROWSER.TITLE` | `str` | Any string text | `'&8Item Prices'` | Configures the technical `TITLE` parameter for `BROWSER.TITLE` in `worth.yml`. |
| `BROWSER.SIZE` | `int` | Any valid integer number | `'54'` | Configures the technical `SIZE` parameter for `BROWSER.SIZE` in `worth.yml`. |
| `BROWSER.ITEMS-PER-PAGE` | `int` | Any valid integer number | `'45'` | Configures the technical `ITEMS-PER-PAGE` parameter for `BROWSER.ITEMS-PER-PAGE` in `worth.yml`. |
| `BROWSER.DEFAULT-SORT` | `str` | Any string text | `'CATEGORY'` | Configures the technical `DEFAULT-SORT` parameter for `BROWSER.DEFAULT-SORT` in `worth.yml`. |
| `BROWSER.CATEGORY-SORT` | `list` | List of configured items/strings | `[CROPS, ORES, MOBS...]` | Configures the technical `CATEGORY-SORT` parameter for `BROWSER.CATEGORY-SORT` in `worth.yml`. |

### 3. Practical Setup Example

```yaml
BROWSER:
  TITLE: '&8Item Prices'
  SIZE: 54
  # The numerical value for Items Per Page. Available options: Any valid integer
  ITEMS-PER-PAGE: 45
  # The text or value for Default Sort. Available options: Any valid string text
  DEFAULT-SORT: CATEGORY
  # Configuration section for Category Sort.
  CATEGORY-SORT:
  - CROPS
  - ORES
  - MOBS
  - NATURAL
  - ARMOR_AND_TOOLS
  - FISH
  - BOOK
  - POTION
  - BLOCKS
```

---

## Section: `TYPE`

### 1. Commented Setup Code Example

```yaml
TYPE:
  # Configuration section for Crops.
  CROPS:
    # The decimal value for Wheat. Available options: Any decimal number
    WHEAT: 18.0
    # The decimal value for Beetroot. Available options: Any decimal number
    BEETROOT: 3.0
    # The decimal value for Carrot. Available options: Any decimal number
    CARROT: 16.0
    # The decimal value for Potato. Available options: Any decimal number
    POTATO: 16.0
    # The decimal value for Poisonous Potato. Available options: Any decimal number
    POISONOUS_POTATO: 32.0
    # The decimal value for Melon. Available options: Any decimal number
    MELON: 6.0
    # The decimal value for Pumpkin. Available options: Any decimal number
    PUMPKIN: 24.0
    # The decimal value for Bamboo. Available options: Any decimal number
    BAMBOO: 4.0
    # The decimal value for Cocoa Beans. Available options: Any decimal number
    COCOA_BEANS: 15.0
    # The decimal value for Cactus. Available options: Any decimal number
    CACTUS: 20.0
    # The decimal value for Sweet Berries. Available options: Any decimal number
    SWEET_BERRIES: 8.0
    # The decimal value for Sugar Cane. Available options: Any decimal number
    SUGAR_CANE: 20.0
    # The decimal value for Red Mushroom. Available options: Any decimal number
    RED_MUSHROOM: 1.0
    # The decimal value for Brown Mushroom. Available options: Any decimal number
    BROWN_MUSHROOM: 1.0
    # The decimal value for Kelp. Available options: Any decimal number
    KELP: 8.0
    # The decimal value for Sea Pickle. Available options: Any decimal number
    SEA_PICKLE: 2.0
    # The decimal value for Glow Berries. Available options: Any decimal number
    GLOW_BERRIES: 4.0
    # The decimal value for Wheat Seeds. Available options: Any decimal number
    WHEAT_SEEDS: 2.0
    # The decimal value for Pumpkin Seeds. Available options: Any decimal number
    PUMPKIN_SEEDS: 8.0
    # The decimal value for Melon Seeds. Available options: Any decimal number
    MELON_SEEDS: 4.0
    # The decimal value for Beetroot Seeds. Available options: Any decimal number
    BEETROOT_SEEDS: 2.0
    # The decimal value for Torchflower Seeds. Available options: Any decimal number
    TORCHFLOWER_SEEDS: 3.0
    # The decimal value for Torchflower. Available options: Any decimal number
    TORCHFLOWER: 4.0
    # The decimal value for Nether Wart. Available options: Any decimal number
    NETHER_WART: 6.0
    # The decimal value for Spruce Sapling. Available options: Any decimal number
    SPRUCE_SAPLING: 3.0
    # The decimal value for Twisting Vines. Available options: Any decimal number
    TWISTING_VINES: 5.0
    # The decimal value for Nether Sprouts. Available options: Any decimal number
    NETHER_SPROUTS: 5.0
    # The decimal value for Crimson Fungus. Available options: Any decimal number
    CRIMSON_FUNGUS: 2.0
    # The decimal value for Bone Meal. Available options: Any decimal number
    BONE_MEAL: 1.0
    # The decimal value for Jungle Sapling. Available options: Any decimal number
    JUNGLE_SAPLING: 3.0
    # The decimal value for Birch Sapling. Available options: Any decimal number
    BIRCH_SAPLING: 3.0
    # The decimal value for Oak Sapling. Available options: Any decimal number
    OAK_SAPLING: 3.0
    # The decimal value for Cherry Sapling. Available options: Any decimal number
    CHERRY_SAPLING: 3.0
    # The decimal value for Dark Oak Sapling. Available options: Any decimal number
    DARK_OAK_SAPLING: 3.0
    # The decimal value for Acacia Sapling. Available options: Any decimal number
    ACACIA_SAPLING: 3.0
    # The decimal value for Warped Fungus. Available options: Any decimal number
    WARPED_FUNGUS: 3.0
    # The decimal value for Hanging Roots. Available options: Any decimal number
    HANGING_ROOTS: 2.0
    # The decimal value for Crimson Roots. Available options: Any decimal number
    CRIMSON_ROOTS: 1.0
    # The decimal value for Warped Roots. Available options: Any decimal number
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `TYPE.CROPS.WHEAT` | `float` | Any decimal number | `'18.0'` | Configures the technical `WHEAT` parameter for `TYPE.CROPS.WHEAT` in `worth.yml`. |
| `TYPE.CROPS.BEETROOT` | `float` | Any decimal number | `'3.0'` | Configures the technical `BEETROOT` parameter for `TYPE.CROPS.BEETROOT` in `worth.yml`. |
| `TYPE.CROPS.CARROT` | `float` | Any decimal number | `'16.0'` | Configures the technical `CARROT` parameter for `TYPE.CROPS.CARROT` in `worth.yml`. |
| `TYPE.CROPS.POTATO` | `float` | Any decimal number | `'16.0'` | Configures the technical `POTATO` parameter for `TYPE.CROPS.POTATO` in `worth.yml`. |
| `TYPE.CROPS.POISONOUS_POTATO` | `float` | Any decimal number | `'32.0'` | Configures the technical `POISONOUS_POTATO` parameter for `TYPE.CROPS.POISONOUS_POTATO` in `worth.yml`. |
| `TYPE.CROPS.MELON` | `float` | Any decimal number | `'6.0'` | Configures the technical `MELON` parameter for `TYPE.CROPS.MELON` in `worth.yml`. |
| `TYPE.CROPS.PUMPKIN` | `float` | Any decimal number | `'24.0'` | Configures the technical `PUMPKIN` parameter for `TYPE.CROPS.PUMPKIN` in `worth.yml`. |
| `TYPE.CROPS.BAMBOO` | `float` | Any decimal number | `'4.0'` | Configures the technical `BAMBOO` parameter for `TYPE.CROPS.BAMBOO` in `worth.yml`. |
| `TYPE.CROPS.COCOA_BEANS` | `float` | Any decimal number | `'15.0'` | Configures the technical `COCOA_BEANS` parameter for `TYPE.CROPS.COCOA_BEANS` in `worth.yml`. |
| `TYPE.CROPS.CACTUS` | `float` | Any decimal number | `'20.0'` | Configures the technical `CACTUS` parameter for `TYPE.CROPS.CACTUS` in `worth.yml`. |
| `TYPE.CROPS.SWEET_BERRIES` | `float` | Any decimal number | `'8.0'` | Configures the technical `SWEET_BERRIES` parameter for `TYPE.CROPS.SWEET_BERRIES` in `worth.yml`. |
| `TYPE.CROPS.SUGAR_CANE` | `float` | Any decimal number | `'20.0'` | Configures the technical `SUGAR_CANE` parameter for `TYPE.CROPS.SUGAR_CANE` in `worth.yml`. |
| `TYPE.CROPS.RED_MUSHROOM` | `float` | Any decimal number | `'1.0'` | Configures the technical `RED_MUSHROOM` parameter for `TYPE.CROPS.RED_MUSHROOM` in `worth.yml`. |
| `TYPE.CROPS.BROWN_MUSHROOM` | `float` | Any decimal number | `'1.0'` | Configures the technical `BROWN_MUSHROOM` parameter for `TYPE.CROPS.BROWN_MUSHROOM` in `worth.yml`. |
| `TYPE.CROPS.KELP` | `float` | Any decimal number | `'8.0'` | Configures the technical `KELP` parameter for `TYPE.CROPS.KELP` in `worth.yml`. |
| `TYPE.CROPS.SEA_PICKLE` | `float` | Any decimal number | `'2.0'` | Configures the technical `SEA_PICKLE` parameter for `TYPE.CROPS.SEA_PICKLE` in `worth.yml`. |
| `TYPE.CROPS.GLOW_BERRIES` | `float` | Any decimal number | `'4.0'` | Configures the technical `GLOW_BERRIES` parameter for `TYPE.CROPS.GLOW_BERRIES` in `worth.yml`. |
| `TYPE.CROPS.WHEAT_SEEDS` | `float` | Any decimal number | `'2.0'` | Configures the technical `WHEAT_SEEDS` parameter for `TYPE.CROPS.WHEAT_SEEDS` in `worth.yml`. |
| `TYPE.CROPS.PUMPKIN_SEEDS` | `float` | Any decimal number | `'8.0'` | Configures the technical `PUMPKIN_SEEDS` parameter for `TYPE.CROPS.PUMPKIN_SEEDS` in `worth.yml`. |
| `TYPE.CROPS.MELON_SEEDS` | `float` | Any decimal number | `'4.0'` | Configures the technical `MELON_SEEDS` parameter for `TYPE.CROPS.MELON_SEEDS` in `worth.yml`. |
| `TYPE.CROPS.BEETROOT_SEEDS` | `float` | Any decimal number | `'2.0'` | Configures the technical `BEETROOT_SEEDS` parameter for `TYPE.CROPS.BEETROOT_SEEDS` in `worth.yml`. |
| `TYPE.CROPS.TORCHFLOWER_SEEDS` | `float` | Any decimal number | `'3.0'` | Configures the technical `TORCHFLOWER_SEEDS` parameter for `TYPE.CROPS.TORCHFLOWER_SEEDS` in `worth.yml`. |
| `TYPE.CROPS.TORCHFLOWER` | `float` | Any decimal number | `'4.0'` | Configures the technical `TORCHFLOWER` parameter for `TYPE.CROPS.TORCHFLOWER` in `worth.yml`. |
| `TYPE.CROPS.NETHER_WART` | `float` | Any decimal number | `'6.0'` | Configures the technical `NETHER_WART` parameter for `TYPE.CROPS.NETHER_WART` in `worth.yml`. |
| `TYPE.CROPS.SPRUCE_SAPLING` | `float` | Any decimal number | `'3.0'` | Configures the technical `SPRUCE_SAPLING` parameter for `TYPE.CROPS.SPRUCE_SAPLING` in `worth.yml`. |
| `TYPE.CROPS.TWISTING_VINES` | `float` | Any decimal number | `'5.0'` | Configures the technical `TWISTING_VINES` parameter for `TYPE.CROPS.TWISTING_VINES` in `worth.yml`. |
| `TYPE.CROPS.NETHER_SPROUTS` | `float` | Any decimal number | `'5.0'` | Configures the technical `NETHER_SPROUTS` parameter for `TYPE.CROPS.NETHER_SPROUTS` in `worth.yml`. |
| `TYPE.CROPS.CRIMSON_FUNGUS` | `float` | Any decimal number | `'2.0'` | Configures the technical `CRIMSON_FUNGUS` parameter for `TYPE.CROPS.CRIMSON_FUNGUS` in `worth.yml`. |
| `TYPE.CROPS.BONE_MEAL` | `float` | Any decimal number | `'1.0'` | Configures the technical `BONE_MEAL` parameter for `TYPE.CROPS.BONE_MEAL` in `worth.yml`. |
| `TYPE.CROPS.JUNGLE_SAPLING` | `float` | Any decimal number | `'3.0'` | Configures the technical `JUNGLE_SAPLING` parameter for `TYPE.CROPS.JUNGLE_SAPLING` in `worth.yml`. |
| *(1339 additional sub-keys configured in section)* | | | | |

### 3. Practical Setup Example

```yaml
TYPE:
  # Configuration section for Crops.
  CROPS:
    # The decimal value for Wheat. Available options: Any decimal number
    WHEAT: 18.0
    # The decimal value for Beetroot. Available options: Any decimal number
    BEETROOT: 3.0
    # The decimal value for Carrot. Available options: Any decimal number
    CARROT: 16.0
    # The decimal value for Potato. Available options: Any decimal number
    POTATO: 16.0
    # The decimal value for Poisonous Potato. Available options: Any decimal number
    POISONOUS_POTATO: 32.0
    # The decimal value for Melon. Available options: Any decimal number
    MELON: 6.0
    # The decimal value for Pumpkin. Available options: Any decimal number
    PUMPKIN: 24.0
    # The decimal value for Bamboo. Available options: Any decimal number
    BAMBOO: 4.0
    # The decimal value for Cocoa Beans. Available options: Any decimal number
    COCOA_BEANS: 15.0
    # The decimal value for Cactus. Available options: Any decimal number
    CACTUS: 20.0
    # The
```

---

## Section: `BLOCK-ITEMS`

### 1. Commented Setup Code Example

```yaml
BLOCK-ITEMS:
- SPAWNER
- VAULT
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `BLOCK-ITEMS` | `list` | List of configured items/strings | `['SPAWNER', 'VAULT']` | Configures the technical `BLOCK-ITEMS` parameter for `BLOCK-ITEMS` in `worth.yml`. |

### 3. Practical Setup Example

```yaml
BLOCK-ITEMS:
- SPAWNER
- VAULT
```

---

