# Detailed Configuration & Setup Guide: `shop.yml`

This is the official, 100% complete technical setup guide for `shop.yml` in **UltimateDonutSMP**.
Each section details the exact commented setup code block, allowed option values, data types, default values, and in-depth functional behavior.

---

## Section: `CATEGORIES`

### 1. Commented Setup Code Example

```yaml
CATEGORIES:
  # The text or value for Menu Title. Available options: Any valid string text
  MENU-TITLE: '&8Shop'
  # The numerical value for Menu Size. Available options: Any valid integer
  MENU-SIZE: 27
  # Configuration section for End.
  END:
    MATERIAL: END_STONE
    DISPLAY-NAME: '&#6BF18DEnd'
    SLOT: 11
    LORE:
    - '&fClick to view the end shop'
    # The text or value for Open Menu. Available options: Any valid string text
    OPEN-MENU: '{end-menu}'
  # Configuration section for Nether.
  NETHER:
    MATERIAL: NETHERRACK
    DISPLAY-NAME: '&#6BF18DNether'
    SLOT: 12
    LORE:
    - '&fClick to view the nether shop'
    # The text or value for Open Menu. Available options: Any valid string text
    OPEN-MENU: '{nether-menu}'
  # Configuration section for Gear.
  GEAR:
    MATERIAL: TOTEM_OF_UNDYING
    DISPLAY-NAME: '&#6BF18DGear'
    SLOT: 13
    LORE:
    - '&fClick to view the gear shop'
    # The text or value for Open Menu. Available options: Any valid string text
    OPEN-MENU: '{gear-menu}'
  # Configuration section for Food.
  FOOD:
    MATERIAL: COOKED_BEEF
    DISPLAY-NAME: '&#6BF18DFood'
    SLOT: 14
    LORE:
    - '&fClick to view the food shop'
    # The text or value for Open Menu. Available options: Any valid string text
    OPEN-MENU: '{food-menu}'
  # Configuration section for Shard.
  SHARD:
    MATERIAL: AMETHYST_SHARD
    DISPLAY-NAME: '&#6BF18DShard shop'
    SLOT: 15
    LORE:
    - '&fClick to view the shard shop'
    # The text or value for Open Menu. Available options: Any valid string text
    OPEN-MENU: '{shard-menu}'
  # Configuration section for Extra 1.
  EXTRA-1:
    # Determines whether Enabled is enabled or disabled. Available options: true, false
    ENABLED: false
    MATERIAL: DIAMOND_ORE
    DISPLAY-NAME: '&#6BF18DExtra Menu (1)'
    SLOT: 22
    LORE:
    - '&fClick to view extra menu'
    # The text or value for Open Menu. Available options: Any valid string text
    OPEN-MENU: '{extra-1-menu}'
  # Configuration section for Extra 2.
  EXTRA-2:
    # Determines whether Enabled is enabled or disabled. Available options: true, false
    ENABLED: false
    MATERIAL: IRON_ORE
    DISPLAY-NAME: '&#6BF18DExtra Menu (2)'
    SLOT: 23
    LORE:
    - '&fClick to view extra menu'
    # The text or value for Open Menu. Available options: Any valid string text
    OPEN-MENU: '{extra-2-menu}'
  # Configuration section for Extra 3.
  EXTRA-3:
    # Determines whether Enabled is enabled or disabled. Available options: true, false
    ENABLED: false
    MATERIAL: GOLD_ORE
    DISPLAY-NAME: '&#6BF18DExtra Menu (3)'
    SLOT: 24
    LORE:
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `CATEGORIES.MENU-TITLE` | `str` | Any string text | `'&8Shop'` | Configures the technical `MENU-TITLE` parameter for `CATEGORIES.MENU-TITLE` in `shop.yml`. |
| `CATEGORIES.MENU-SIZE` | `int` | Any valid integer number | `'27'` | Configures the technical `MENU-SIZE` parameter for `CATEGORIES.MENU-SIZE` in `shop.yml`. |
| `CATEGORIES.END.MATERIAL` | `str` | Any string text | `'END_STONE'` | Configures the technical `MATERIAL` parameter for `CATEGORIES.END.MATERIAL` in `shop.yml`. |
| `CATEGORIES.END.DISPLAY-NAME` | `str` | Any string text | `'&#6BF18DEnd'` | Configures the technical `DISPLAY-NAME` parameter for `CATEGORIES.END.DISPLAY-NAME` in `shop.yml`. |
| `CATEGORIES.END.SLOT` | `int` | Any valid integer number | `'11'` | Configures the technical `SLOT` parameter for `CATEGORIES.END.SLOT` in `shop.yml`. |
| `CATEGORIES.END.LORE` | `list` | List of configured items/strings | `['&fClick to view the end shop']` | Configures the technical `LORE` parameter for `CATEGORIES.END.LORE` in `shop.yml`. |
| `CATEGORIES.END.OPEN-MENU` | `str` | Any string text | `'{end-menu}'` | Configures the technical `OPEN-MENU` parameter for `CATEGORIES.END.OPEN-MENU` in `shop.yml`. |
| `CATEGORIES.NETHER.MATERIAL` | `str` | Any string text | `'NETHERRACK'` | Configures the technical `MATERIAL` parameter for `CATEGORIES.NETHER.MATERIAL` in `shop.yml`. |
| `CATEGORIES.NETHER.DISPLAY-NAME` | `str` | Any string text | `'&#6BF18DNether'` | Configures the technical `DISPLAY-NAME` parameter for `CATEGORIES.NETHER.DISPLAY-NAME` in `shop.yml`. |
| `CATEGORIES.NETHER.SLOT` | `int` | Any valid integer number | `'12'` | Configures the technical `SLOT` parameter for `CATEGORIES.NETHER.SLOT` in `shop.yml`. |
| `CATEGORIES.NETHER.LORE` | `list` | List of configured items/strings | `['&fClick to view the nether shop']` | Configures the technical `LORE` parameter for `CATEGORIES.NETHER.LORE` in `shop.yml`. |
| `CATEGORIES.NETHER.OPEN-MENU` | `str` | Any string text | `'{nether-menu}'` | Configures the technical `OPEN-MENU` parameter for `CATEGORIES.NETHER.OPEN-MENU` in `shop.yml`. |
| `CATEGORIES.GEAR.MATERIAL` | `str` | Any string text | `'TOTEM_OF_UNDYING'` | Configures the technical `MATERIAL` parameter for `CATEGORIES.GEAR.MATERIAL` in `shop.yml`. |
| `CATEGORIES.GEAR.DISPLAY-NAME` | `str` | Any string text | `'&#6BF18DGear'` | Configures the technical `DISPLAY-NAME` parameter for `CATEGORIES.GEAR.DISPLAY-NAME` in `shop.yml`. |
| `CATEGORIES.GEAR.SLOT` | `int` | Any valid integer number | `'13'` | Configures the technical `SLOT` parameter for `CATEGORIES.GEAR.SLOT` in `shop.yml`. |
| `CATEGORIES.GEAR.LORE` | `list` | List of configured items/strings | `['&fClick to view the gear shop']` | Configures the technical `LORE` parameter for `CATEGORIES.GEAR.LORE` in `shop.yml`. |
| `CATEGORIES.GEAR.OPEN-MENU` | `str` | Any string text | `'{gear-menu}'` | Configures the technical `OPEN-MENU` parameter for `CATEGORIES.GEAR.OPEN-MENU` in `shop.yml`. |
| `CATEGORIES.FOOD.MATERIAL` | `str` | Any string text | `'COOKED_BEEF'` | Configures the technical `MATERIAL` parameter for `CATEGORIES.FOOD.MATERIAL` in `shop.yml`. |
| `CATEGORIES.FOOD.DISPLAY-NAME` | `str` | Any string text | `'&#6BF18DFood'` | Configures the technical `DISPLAY-NAME` parameter for `CATEGORIES.FOOD.DISPLAY-NAME` in `shop.yml`. |
| `CATEGORIES.FOOD.SLOT` | `int` | Any valid integer number | `'14'` | Configures the technical `SLOT` parameter for `CATEGORIES.FOOD.SLOT` in `shop.yml`. |
| `CATEGORIES.FOOD.LORE` | `list` | List of configured items/strings | `['&fClick to view the food shop']` | Configures the technical `LORE` parameter for `CATEGORIES.FOOD.LORE` in `shop.yml`. |
| `CATEGORIES.FOOD.OPEN-MENU` | `str` | Any string text | `'{food-menu}'` | Configures the technical `OPEN-MENU` parameter for `CATEGORIES.FOOD.OPEN-MENU` in `shop.yml`. |
| `CATEGORIES.SHARD.MATERIAL` | `str` | Any string text | `'AMETHYST_SHARD'` | Configures the technical `MATERIAL` parameter for `CATEGORIES.SHARD.MATERIAL` in `shop.yml`. |
| `CATEGORIES.SHARD.DISPLAY-NAME` | `str` | Any string text | `'&#6BF18DShard shop'` | Configures the technical `DISPLAY-NAME` parameter for `CATEGORIES.SHARD.DISPLAY-NAME` in `shop.yml`. |
| `CATEGORIES.SHARD.SLOT` | `int` | Any valid integer number | `'15'` | Configures the technical `SLOT` parameter for `CATEGORIES.SHARD.SLOT` in `shop.yml`. |
| `CATEGORIES.SHARD.LORE` | `list` | List of configured items/strings | `['&fClick to view the shard shop']` | Configures the technical `LORE` parameter for `CATEGORIES.SHARD.LORE` in `shop.yml`. |
| `CATEGORIES.SHARD.OPEN-MENU` | `str` | Any string text | `'{shard-menu}'` | Configures the technical `OPEN-MENU` parameter for `CATEGORIES.SHARD.OPEN-MENU` in `shop.yml`. |
| `CATEGORIES.EXTRA-1.ENABLED` | `bool` | `true`, `false` | `false` | Global toggle for `CATEGORIES` system. Set to `true` to enable, `false` to disable. |
| `CATEGORIES.EXTRA-1.MATERIAL` | `str` | Any string text | `'DIAMOND_ORE'` | Configures the technical `MATERIAL` parameter for `CATEGORIES.EXTRA-1.MATERIAL` in `shop.yml`. |
| `CATEGORIES.EXTRA-1.DISPLAY-NAME` | `str` | Any string text | `'&#6BF18DExtra Menu (1)'` | Configures the technical `DISPLAY-NAME` parameter for `CATEGORIES.EXTRA-1.DISPLAY-NAME` in `shop.yml`. |
| *(15 additional sub-keys configured in section)* | | | | |

### 3. Practical Setup Example

```yaml
CATEGORIES:
  # The text or value for Menu Title. Available options: Any valid string text
  MENU-TITLE: '&8Shop'
  # The numerical value for Menu Size. Available options: Any valid integer
  MENU-SIZE: 27
  # Configuration section for End.
  END:
    MATERIAL: END_STONE
    DISPLAY-NAME: '&#6BF18DEnd'
    SLOT: 11
    LORE:
    - '&fClick to view the end shop'
    # The text or value for Open Menu. Available options: Any valid string text
    OPEN-MENU: '{end-menu}'
  # Configuration section for Nether.
  NETHER:
    MATERIAL: NETHERRACK
    DISPLAY-NAME: '&#6BF18DNether'
    SLOT: 12
    LORE:
    - '&fClick to view the nether shop'
    # The text or value for Open Menu. Available options: Any valid string text
    OPEN-MENU: '{nether-menu}'
  # Configuration section for Gear.
  GEAR:
    MATERIAL: TOTEM_OF_UNDYING
    DISPLAY-NAME: '&#6BF18DGear'
    SLOT: 13
    LORE:
    - '&fClick to view the gear shop'
    # The text or value for Open Menu. Available options: Any valid string te
```

---

## Section: `END-MENU`

### 1. Commented Setup Code Example

```yaml
END-MENU:
  # The text or value for Currency. Available options: Any valid string text
  CURRENCY: MONEY
  TITLE: '&8shop - end'
  SIZE: 27
  # Configuration section for Ender Chest Item.
  ENDER-CHEST-ITEM:
    MATERIAL: ENDER_CHEST
    DISPLAY-NAME: '&fEnder Chest'
    SLOT: 9
    # The decimal value for Price Per Unit. Available options: Any decimal number
    PRICE-PER-UNIT: 2500.0
    LORE:
    - '&fBuy price: &a$2,500'
    # The text or value for Command. Available options: Any valid string text
    COMMAND: ''
  # Configuration section for Ender Pearl Item.
  ENDER-PEARL-ITEM:
    # The text or value for Currency. Available options: Any valid string text
    CURRENCY: MONEY
    MATERIAL: ENDER_PEARL
    DISPLAY-NAME: '&fEnder Pearl'
    SLOT: 10
    # The decimal value for Price Per Unit. Available options: Any decimal number
    PRICE-PER-UNIT: 75.0
    LORE:
    - '&fBuy price: &a$75'
    # The text or value for Command. Available options: Any valid string text
    COMMAND: ''
  # Configuration section for End Stone Item.
  END-STONE-ITEM:
    # The text or value for Currency. Available options: Any valid string text
    CURRENCY: MONEY
    MATERIAL: END_STONE
    DISPLAY-NAME: '&fEnd Stone'
    SLOT: 11
    # The decimal value for Price Per Unit. Available options: Any decimal number
    PRICE-PER-UNIT: 8.0
    LORE:
    - '&fBuy price: &a$8'
    # The text or value for Command. Available options: Any valid string text
    COMMAND: ''
  # Configuration section for Dragon Breath Item.
  DRAGON-BREATH-ITEM:
    # The text or value for Currency. Available options: Any valid string text
    CURRENCY: MONEY
    MATERIAL: DRAGON_BREATH
    DISPLAY-NAME: '&eDragon''s Breath'
    SLOT: 12
    # The decimal value for Price Per Unit. Available options: Any decimal number
    PRICE-PER-UNIT: 1000.0
    LORE:
    - '&fBuy price: &a$1,000'
    # The text or value for Command. Available options: Any valid string text
    COMMAND: ''
  # Configuration section for End Rod Item.
  END-ROD-ITEM:
    # The text or value for Currency. Available options: Any valid string text
    CURRENCY: MONEY
    MATERIAL: END_ROD
    DISPLAY-NAME: '&fEnd Rod'
    SLOT: 13
    # The decimal value for Price Per Unit. Available options: Any decimal number
    PRICE-PER-UNIT: 100.0
    LORE:
    - '&fBuy price: &a$100'
  # Configuration section for Chorus Fruit Item.
  CHORUS-FRUIT-ITEM:
    # The text or value for Currency. Available options: Any valid string text
    CURRENCY: MONEY
    MATERIAL: CHORUS_FRUIT
    DISPLAY-NAME: '&fChorus Fruit'
    SLOT: 14
    # The decimal value for Price Per Unit. Available options: Any decimal number
    PRICE-PER-UNIT: 25.0
    LORE:
    - '&fBuy price: &a$25'
  # Configuration section for Popped Chorus Fruit Item.
  POPPED-CHORUS-FRUIT-ITEM:
    # The text or value for Currency. Available options: Any valid string text
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `END-MENU.CURRENCY` | `str` | Any string text | `'MONEY'` | Configures the technical `CURRENCY` parameter for `END-MENU.CURRENCY` in `shop.yml`. |
| `END-MENU.TITLE` | `str` | Any string text | `'&8shop - end'` | Configures the technical `TITLE` parameter for `END-MENU.TITLE` in `shop.yml`. |
| `END-MENU.SIZE` | `int` | Any valid integer number | `'27'` | Configures the technical `SIZE` parameter for `END-MENU.SIZE` in `shop.yml`. |
| `END-MENU.ENDER-CHEST-ITEM.MATERIAL` | `str` | Any string text | `'ENDER_CHEST'` | Configures the technical `MATERIAL` parameter for `END-MENU.ENDER-CHEST-ITEM.MATERIAL` in `shop.yml`. |
| `END-MENU.ENDER-CHEST-ITEM.DISPLAY-NAME` | `str` | Any string text | `'&fEnder Chest'` | Configures the technical `DISPLAY-NAME` parameter for `END-MENU.ENDER-CHEST-ITEM.DISPLAY-NAME` in `shop.yml`. |
| `END-MENU.ENDER-CHEST-ITEM.SLOT` | `int` | Any valid integer number | `'9'` | Configures the technical `SLOT` parameter for `END-MENU.ENDER-CHEST-ITEM.SLOT` in `shop.yml`. |
| `END-MENU.ENDER-CHEST-ITEM.PRICE-PER-UNIT` | `float` | Any decimal number | `'2500.0'` | Configures the technical `PRICE-PER-UNIT` parameter for `END-MENU.ENDER-CHEST-ITEM.PRICE-PER-UNIT` in `shop.yml`. |
| `END-MENU.ENDER-CHEST-ITEM.LORE` | `list` | List of configured items/strings | `['&fBuy price: &a$2,500']` | Configures the technical `LORE` parameter for `END-MENU.ENDER-CHEST-ITEM.LORE` in `shop.yml`. |
| `END-MENU.ENDER-CHEST-ITEM.COMMAND` | `str` | Any string text | `''` | Configures the technical `COMMAND` parameter for `END-MENU.ENDER-CHEST-ITEM.COMMAND` in `shop.yml`. |
| `END-MENU.ENDER-PEARL-ITEM.CURRENCY` | `str` | Any string text | `'MONEY'` | Configures the technical `CURRENCY` parameter for `END-MENU.ENDER-PEARL-ITEM.CURRENCY` in `shop.yml`. |
| `END-MENU.ENDER-PEARL-ITEM.MATERIAL` | `str` | Any string text | `'ENDER_PEARL'` | Configures the technical `MATERIAL` parameter for `END-MENU.ENDER-PEARL-ITEM.MATERIAL` in `shop.yml`. |
| `END-MENU.ENDER-PEARL-ITEM.DISPLAY-NAME` | `str` | Any string text | `'&fEnder Pearl'` | Configures the technical `DISPLAY-NAME` parameter for `END-MENU.ENDER-PEARL-ITEM.DISPLAY-NAME` in `shop.yml`. |
| `END-MENU.ENDER-PEARL-ITEM.SLOT` | `int` | Any valid integer number | `'10'` | Configures the technical `SLOT` parameter for `END-MENU.ENDER-PEARL-ITEM.SLOT` in `shop.yml`. |
| `END-MENU.ENDER-PEARL-ITEM.PRICE-PER-UNIT` | `float` | Any decimal number | `'75.0'` | Configures the technical `PRICE-PER-UNIT` parameter for `END-MENU.ENDER-PEARL-ITEM.PRICE-PER-UNIT` in `shop.yml`. |
| `END-MENU.ENDER-PEARL-ITEM.LORE` | `list` | List of configured items/strings | `['&fBuy price: &a$75']` | Configures the technical `LORE` parameter for `END-MENU.ENDER-PEARL-ITEM.LORE` in `shop.yml`. |
| `END-MENU.ENDER-PEARL-ITEM.COMMAND` | `str` | Any string text | `''` | Configures the technical `COMMAND` parameter for `END-MENU.ENDER-PEARL-ITEM.COMMAND` in `shop.yml`. |
| `END-MENU.END-STONE-ITEM.CURRENCY` | `str` | Any string text | `'MONEY'` | Configures the technical `CURRENCY` parameter for `END-MENU.END-STONE-ITEM.CURRENCY` in `shop.yml`. |
| `END-MENU.END-STONE-ITEM.MATERIAL` | `str` | Any string text | `'END_STONE'` | Configures the technical `MATERIAL` parameter for `END-MENU.END-STONE-ITEM.MATERIAL` in `shop.yml`. |
| `END-MENU.END-STONE-ITEM.DISPLAY-NAME` | `str` | Any string text | `'&fEnd Stone'` | Configures the technical `DISPLAY-NAME` parameter for `END-MENU.END-STONE-ITEM.DISPLAY-NAME` in `shop.yml`. |
| `END-MENU.END-STONE-ITEM.SLOT` | `int` | Any valid integer number | `'11'` | Configures the technical `SLOT` parameter for `END-MENU.END-STONE-ITEM.SLOT` in `shop.yml`. |
| `END-MENU.END-STONE-ITEM.PRICE-PER-UNIT` | `float` | Any decimal number | `'8.0'` | Configures the technical `PRICE-PER-UNIT` parameter for `END-MENU.END-STONE-ITEM.PRICE-PER-UNIT` in `shop.yml`. |
| `END-MENU.END-STONE-ITEM.LORE` | `list` | List of configured items/strings | `['&fBuy price: &a$8']` | Configures the technical `LORE` parameter for `END-MENU.END-STONE-ITEM.LORE` in `shop.yml`. |
| `END-MENU.END-STONE-ITEM.COMMAND` | `str` | Any string text | `''` | Configures the technical `COMMAND` parameter for `END-MENU.END-STONE-ITEM.COMMAND` in `shop.yml`. |
| `END-MENU.DRAGON-BREATH-ITEM.CURRENCY` | `str` | Any string text | `'MONEY'` | Configures the technical `CURRENCY` parameter for `END-MENU.DRAGON-BREATH-ITEM.CURRENCY` in `shop.yml`. |
| `END-MENU.DRAGON-BREATH-ITEM.MATERIAL` | `str` | Any string text | `'DRAGON_BREATH'` | Configures the technical `MATERIAL` parameter for `END-MENU.DRAGON-BREATH-ITEM.MATERIAL` in `shop.yml`. |
| `END-MENU.DRAGON-BREATH-ITEM.DISPLAY-NAME` | `str` | Any string text | `'&eDragon's Breath'` | Configures the technical `DISPLAY-NAME` parameter for `END-MENU.DRAGON-BREATH-ITEM.DISPLAY-NAME` in `shop.yml`. |
| `END-MENU.DRAGON-BREATH-ITEM.SLOT` | `int` | Any valid integer number | `'12'` | Configures the technical `SLOT` parameter for `END-MENU.DRAGON-BREATH-ITEM.SLOT` in `shop.yml`. |
| `END-MENU.DRAGON-BREATH-ITEM.PRICE-PER-UNIT` | `float` | Any decimal number | `'1000.0'` | Configures the technical `PRICE-PER-UNIT` parameter for `END-MENU.DRAGON-BREATH-ITEM.PRICE-PER-UNIT` in `shop.yml`. |
| `END-MENU.DRAGON-BREATH-ITEM.LORE` | `list` | List of configured items/strings | `['&fBuy price: &a$1,000']` | Configures the technical `LORE` parameter for `END-MENU.DRAGON-BREATH-ITEM.LORE` in `shop.yml`. |
| `END-MENU.DRAGON-BREATH-ITEM.COMMAND` | `str` | Any string text | `''` | Configures the technical `COMMAND` parameter for `END-MENU.DRAGON-BREATH-ITEM.COMMAND` in `shop.yml`. |
| *(30 additional sub-keys configured in section)* | | | | |

### 3. Practical Setup Example

```yaml
END-MENU:
  # The text or value for Currency. Available options: Any valid string text
  CURRENCY: MONEY
  TITLE: '&8shop - end'
  SIZE: 27
  # Configuration section for Ender Chest Item.
  ENDER-CHEST-ITEM:
    MATERIAL: ENDER_CHEST
    DISPLAY-NAME: '&fEnder Chest'
    SLOT: 9
    # The decimal value for Price Per Unit. Available options: Any decimal number
    PRICE-PER-UNIT: 2500.0
    LORE:
    - '&fBuy price: &a$2,500'
    # The text or value for Command. Available options: Any valid string text
    COMMAND: ''
  # Configuration section for Ender Pearl Item.
  ENDER-PEARL-ITEM:
    # The text or value for Currency. Available options: Any valid string text
    CURRENCY: MONEY
    MATERIAL: ENDER_PEARL
    DISPLAY-NAME: '&fEnder Pearl'
    SLOT: 10
    # The decimal value for Price Per Unit. Available options: Any decimal number
    PRICE-PER-UNIT: 75.0
    LORE:
    - '&fBuy price: &a$75'
    # The text or value for Command. Available options: Any valid string text
    COMMAND: ''
```

---

## Section: `NETHER-MENU`

### 1. Commented Setup Code Example

```yaml
NETHER-MENU:
  TITLE: '&8shop - nether'
  SIZE: 27
  # Configuration section for Blaze Rod Item.
  BLAZE-ROD-ITEM:
    # The text or value for Currency. Available options: Any valid string text
    CURRENCY: MONEY
    MATERIAL: BLAZE_ROD
    DISPLAY-NAME: '&fBlaze Rod'
    SLOT: 9
    # The decimal value for Price Per Unit. Available options: Any decimal number
    PRICE-PER-UNIT: 150.0
    LORE:
    - '&fBuy price: &a$150'
  # Configuration section for Nether Wart Item.
  NETHER-WART-ITEM:
    # The text or value for Currency. Available options: Any valid string text
    CURRENCY: MONEY
    MATERIAL: NETHER_WART
    DISPLAY-NAME: '&fNether Wart'
    SLOT: 10
    # The numerical value for Price Per Unit. Available options: Any valid integer
    PRICE-PER-UNIT: 25
    LORE:
    - '&fBuy price: &a$25'
  # Configuration section for Glowstone Dust Item.
  GLOWSTONE-DUST-ITEM:
    # The text or value for Currency. Available options: Any valid string text
    CURRENCY: MONEY
    MATERIAL: GLOWSTONE_DUST
    DISPLAY-NAME: '&fGlowstone Dust'
    SLOT: 11
    # The numerical value for Price Per Unit. Available options: Any valid integer
    PRICE-PER-UNIT: 15
    LORE:
    - '&fBuy price: &a$15'
  # Configuration section for Magma Cream Item.
  MAGMA-CREAM-ITEM:
    # The text or value for Currency. Available options: Any valid string text
    CURRENCY: MONEY
    MATERIAL: MAGMA_CREAM
    DISPLAY-NAME: '&fMagma Cream'
    SLOT: 12
    # The numerical value for Price Per Unit. Available options: Any valid integer
    PRICE-PER-UNIT: 25
    LORE:
    - '&fBuy price: &a$25'
  # Configuration section for Ghast Tear Item.
  GHAST-TEAR-ITEM:
    # The text or value for Currency. Available options: Any valid string text
    CURRENCY: MONEY
    MATERIAL: GHAST_TEAR
    DISPLAY-NAME: '&fGhast Tear'
    SLOT: 13
    # The numerical value for Price Per Unit. Available options: Any valid integer
    PRICE-PER-UNIT: 350
    LORE:
    - '&fBuy price: &a$350'
  # Configuration section for Nether Quartz Item.
  NETHER-QUARTZ-ITEM:
    # The text or value for Currency. Available options: Any valid string text
    CURRENCY: MONEY
    MATERIAL: QUARTZ
    DISPLAY-NAME: '&fNether Quartz'
    SLOT: 14
    # The numerical value for Price Per Unit. Available options: Any valid integer
    PRICE-PER-UNIT: 15
    LORE:
    - '&fBuy price: &a$15'
  # Configuration section for Soul Sand Item.
  SOUL-SAND-ITEM:
    # The text or value for Currency. Available options: Any valid string text
    CURRENCY: MONEY
    MATERIAL: SOUL_SAND
    DISPLAY-NAME: '&fSoul Sand'
    SLOT: 15
    # The numerical value for Price Per Unit. Available options: Any valid integer
    PRICE-PER-UNIT: 50
    LORE:
    - '&fBuy price: &a$50'
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `NETHER-MENU.TITLE` | `str` | Any string text | `'&8shop - nether'` | Configures the technical `TITLE` parameter for `NETHER-MENU.TITLE` in `shop.yml`. |
| `NETHER-MENU.SIZE` | `int` | Any valid integer number | `'27'` | Configures the technical `SIZE` parameter for `NETHER-MENU.SIZE` in `shop.yml`. |
| `NETHER-MENU.BLAZE-ROD-ITEM.CURRENCY` | `str` | Any string text | `'MONEY'` | Configures the technical `CURRENCY` parameter for `NETHER-MENU.BLAZE-ROD-ITEM.CURRENCY` in `shop.yml`. |
| `NETHER-MENU.BLAZE-ROD-ITEM.MATERIAL` | `str` | Any string text | `'BLAZE_ROD'` | Configures the technical `MATERIAL` parameter for `NETHER-MENU.BLAZE-ROD-ITEM.MATERIAL` in `shop.yml`. |
| `NETHER-MENU.BLAZE-ROD-ITEM.DISPLAY-NAME` | `str` | Any string text | `'&fBlaze Rod'` | Configures the technical `DISPLAY-NAME` parameter for `NETHER-MENU.BLAZE-ROD-ITEM.DISPLAY-NAME` in `shop.yml`. |
| `NETHER-MENU.BLAZE-ROD-ITEM.SLOT` | `int` | Any valid integer number | `'9'` | Configures the technical `SLOT` parameter for `NETHER-MENU.BLAZE-ROD-ITEM.SLOT` in `shop.yml`. |
| `NETHER-MENU.BLAZE-ROD-ITEM.PRICE-PER-UNIT` | `float` | Any decimal number | `'150.0'` | Configures the technical `PRICE-PER-UNIT` parameter for `NETHER-MENU.BLAZE-ROD-ITEM.PRICE-PER-UNIT` in `shop.yml`. |
| `NETHER-MENU.BLAZE-ROD-ITEM.LORE` | `list` | List of configured items/strings | `['&fBuy price: &a$150']` | Configures the technical `LORE` parameter for `NETHER-MENU.BLAZE-ROD-ITEM.LORE` in `shop.yml`. |
| `NETHER-MENU.NETHER-WART-ITEM.CURRENCY` | `str` | Any string text | `'MONEY'` | Configures the technical `CURRENCY` parameter for `NETHER-MENU.NETHER-WART-ITEM.CURRENCY` in `shop.yml`. |
| `NETHER-MENU.NETHER-WART-ITEM.MATERIAL` | `str` | Any string text | `'NETHER_WART'` | Configures the technical `MATERIAL` parameter for `NETHER-MENU.NETHER-WART-ITEM.MATERIAL` in `shop.yml`. |
| `NETHER-MENU.NETHER-WART-ITEM.DISPLAY-NAME` | `str` | Any string text | `'&fNether Wart'` | Configures the technical `DISPLAY-NAME` parameter for `NETHER-MENU.NETHER-WART-ITEM.DISPLAY-NAME` in `shop.yml`. |
| `NETHER-MENU.NETHER-WART-ITEM.SLOT` | `int` | Any valid integer number | `'10'` | Configures the technical `SLOT` parameter for `NETHER-MENU.NETHER-WART-ITEM.SLOT` in `shop.yml`. |
| `NETHER-MENU.NETHER-WART-ITEM.PRICE-PER-UNIT` | `int` | Any valid integer number | `'25'` | Configures the technical `PRICE-PER-UNIT` parameter for `NETHER-MENU.NETHER-WART-ITEM.PRICE-PER-UNIT` in `shop.yml`. |
| `NETHER-MENU.NETHER-WART-ITEM.LORE` | `list` | List of configured items/strings | `['&fBuy price: &a$25']` | Configures the technical `LORE` parameter for `NETHER-MENU.NETHER-WART-ITEM.LORE` in `shop.yml`. |
| `NETHER-MENU.GLOWSTONE-DUST-ITEM.CURRENCY` | `str` | Any string text | `'MONEY'` | Configures the technical `CURRENCY` parameter for `NETHER-MENU.GLOWSTONE-DUST-ITEM.CURRENCY` in `shop.yml`. |
| `NETHER-MENU.GLOWSTONE-DUST-ITEM.MATERIAL` | `str` | Any string text | `'GLOWSTONE_DUST'` | Configures the technical `MATERIAL` parameter for `NETHER-MENU.GLOWSTONE-DUST-ITEM.MATERIAL` in `shop.yml`. |
| `NETHER-MENU.GLOWSTONE-DUST-ITEM.DISPLAY-NAME` | `str` | Any string text | `'&fGlowstone Dust'` | Configures the technical `DISPLAY-NAME` parameter for `NETHER-MENU.GLOWSTONE-DUST-ITEM.DISPLAY-NAME` in `shop.yml`. |
| `NETHER-MENU.GLOWSTONE-DUST-ITEM.SLOT` | `int` | Any valid integer number | `'11'` | Configures the technical `SLOT` parameter for `NETHER-MENU.GLOWSTONE-DUST-ITEM.SLOT` in `shop.yml`. |
| `NETHER-MENU.GLOWSTONE-DUST-ITEM.PRICE-PER-UNIT` | `int` | Any valid integer number | `'15'` | Configures the technical `PRICE-PER-UNIT` parameter for `NETHER-MENU.GLOWSTONE-DUST-ITEM.PRICE-PER-UNIT` in `shop.yml`. |
| `NETHER-MENU.GLOWSTONE-DUST-ITEM.LORE` | `list` | List of configured items/strings | `['&fBuy price: &a$15']` | Configures the technical `LORE` parameter for `NETHER-MENU.GLOWSTONE-DUST-ITEM.LORE` in `shop.yml`. |
| `NETHER-MENU.MAGMA-CREAM-ITEM.CURRENCY` | `str` | Any string text | `'MONEY'` | Configures the technical `CURRENCY` parameter for `NETHER-MENU.MAGMA-CREAM-ITEM.CURRENCY` in `shop.yml`. |
| `NETHER-MENU.MAGMA-CREAM-ITEM.MATERIAL` | `str` | Any string text | `'MAGMA_CREAM'` | Configures the technical `MATERIAL` parameter for `NETHER-MENU.MAGMA-CREAM-ITEM.MATERIAL` in `shop.yml`. |
| `NETHER-MENU.MAGMA-CREAM-ITEM.DISPLAY-NAME` | `str` | Any string text | `'&fMagma Cream'` | Configures the technical `DISPLAY-NAME` parameter for `NETHER-MENU.MAGMA-CREAM-ITEM.DISPLAY-NAME` in `shop.yml`. |
| `NETHER-MENU.MAGMA-CREAM-ITEM.SLOT` | `int` | Any valid integer number | `'12'` | Configures the technical `SLOT` parameter for `NETHER-MENU.MAGMA-CREAM-ITEM.SLOT` in `shop.yml`. |
| `NETHER-MENU.MAGMA-CREAM-ITEM.PRICE-PER-UNIT` | `int` | Any valid integer number | `'25'` | Configures the technical `PRICE-PER-UNIT` parameter for `NETHER-MENU.MAGMA-CREAM-ITEM.PRICE-PER-UNIT` in `shop.yml`. |
| `NETHER-MENU.MAGMA-CREAM-ITEM.LORE` | `list` | List of configured items/strings | `['&fBuy price: &a$25']` | Configures the technical `LORE` parameter for `NETHER-MENU.MAGMA-CREAM-ITEM.LORE` in `shop.yml`. |
| `NETHER-MENU.GHAST-TEAR-ITEM.CURRENCY` | `str` | Any string text | `'MONEY'` | Configures the technical `CURRENCY` parameter for `NETHER-MENU.GHAST-TEAR-ITEM.CURRENCY` in `shop.yml`. |
| `NETHER-MENU.GHAST-TEAR-ITEM.MATERIAL` | `str` | Any string text | `'GHAST_TEAR'` | Configures the technical `MATERIAL` parameter for `NETHER-MENU.GHAST-TEAR-ITEM.MATERIAL` in `shop.yml`. |
| `NETHER-MENU.GHAST-TEAR-ITEM.DISPLAY-NAME` | `str` | Any string text | `'&fGhast Tear'` | Configures the technical `DISPLAY-NAME` parameter for `NETHER-MENU.GHAST-TEAR-ITEM.DISPLAY-NAME` in `shop.yml`. |
| `NETHER-MENU.GHAST-TEAR-ITEM.SLOT` | `int` | Any valid integer number | `'13'` | Configures the technical `SLOT` parameter for `NETHER-MENU.GHAST-TEAR-ITEM.SLOT` in `shop.yml`. |
| *(26 additional sub-keys configured in section)* | | | | |

### 3. Practical Setup Example

```yaml
NETHER-MENU:
  TITLE: '&8shop - nether'
  SIZE: 27
  # Configuration section for Blaze Rod Item.
  BLAZE-ROD-ITEM:
    # The text or value for Currency. Available options: Any valid string text
    CURRENCY: MONEY
    MATERIAL: BLAZE_ROD
    DISPLAY-NAME: '&fBlaze Rod'
    SLOT: 9
    # The decimal value for Price Per Unit. Available options: Any decimal number
    PRICE-PER-UNIT: 150.0
    LORE:
    - '&fBuy price: &a$150'
  # Configuration section for Nether Wart Item.
  NETHER-WART-ITEM:
    # The text or value for Currency. Available options: Any valid string text
    CURRENCY: MONEY
    MATERIAL: NETHER_WART
    DISPLAY-NAME: '&fNether Wart'
    SLOT: 10
    # The numerical value for Price Per Unit. Available options: Any valid integer
    PRICE-PER-UNIT: 25
    LORE:
    - '&fBuy price: &a$25'
  # Configuration section for Glowstone Dust Item.
  GLOWSTONE-DUST-ITEM:
    # The text or value for Currency. Available options: Any valid string text
    CURRENCY: MONEY
    MATERIAL: GL
```

---

## Section: `GEAR-MENU`

### 1. Commented Setup Code Example

```yaml
GEAR-MENU:
  TITLE: '&8gear - shop'
  SIZE: 27
  # Configuration section for Obsidian Item.
  OBSIDIAN-ITEM:
    # The text or value for Currency. Available options: Any valid string text
    CURRENCY: MONEY
    MATERIAL: OBSIDIAN
    DISPLAY-NAME: '&fObsidian'
    SLOT: 9
    # The numerical value for Price Per Unit. Available options: Any valid integer
    PRICE-PER-UNIT: 100
    LORE:
    - '&fBuy price: &a$100'
  # Configuration section for End Crystal Item.
  END-CRYSTAL-ITEM:
    # The text or value for Currency. Available options: Any valid string text
    CURRENCY: MONEY
    MATERIAL: END_CRYSTAL
    DISPLAY-NAME: '&bEnd Crystal'
    SLOT: 10
    # The numerical value for Price Per Unit. Available options: Any valid integer
    PRICE-PER-UNIT: 350
    LORE:
    - '&fBuy price: &a$350'
  # Configuration section for Respawn Anchor Item.
  RESPAWN-ANCHOR-ITEM:
    # The text or value for Currency. Available options: Any valid string text
    CURRENCY: MONEY
    MATERIAL: RESPAWN_ANCHOR
    DISPLAY-NAME: '&fRespawn Anchor'
    SLOT: 11
    # The numerical value for Price Per Unit. Available options: Any valid integer
    PRICE-PER-UNIT: 1000
    LORE:
    - '&fBuy price: &a$1000'
  # Configuration section for Glowstone Item.
  GLOWSTONE-ITEM:
    # The text or value for Currency. Available options: Any valid string text
    CURRENCY: MONEY
    MATERIAL: GLOWSTONE
    DISPLAY-NAME: '&fGlowstone'
    SLOT: 12
    # The numerical value for Price Per Unit. Available options: Any valid integer
    PRICE-PER-UNIT: 100
    LORE:
    - '&fBuy price: &a$100'
  # Configuration section for Totem Of Undying Item.
  TOTEM-OF-UNDYING-ITEM:
    # The text or value for Currency. Available options: Any valid string text
    CURRENCY: MONEY
    MATERIAL: TOTEM_OF_UNDYING
    DISPLAY-NAME: '&eTotem of Undying'
    SLOT: 13
    # The numerical value for Price Per Unit. Available options: Any valid integer
    PRICE-PER-UNIT: 1250
    LORE:
    - '&fBuy price: &a$1250'
  # Configuration section for Ender Pearl Item.
  ENDER-PEARL-ITEM:
    # The text or value for Currency. Available options: Any valid string text
    CURRENCY: MONEY
    MATERIAL: ENDER_PEARL
    DISPLAY-NAME: '&fEnder Pearl'
    SLOT: 14
    # The numerical value for Price Per Unit. Available options: Any valid integer
    PRICE-PER-UNIT: 75
    LORE:
    - '&fBuy price: &a$75'
  # Configuration section for Golden Apple Item.
  GOLDEN-APPLE-ITEM:
    # The text or value for Currency. Available options: Any valid string text
    CURRENCY: MONEY
    MATERIAL: GOLDEN_APPLE
    DISPLAY-NAME: '&bGolden Apple'
    SLOT: 15
    # The numerical value for Price Per Unit. Available options: Any valid integer
    PRICE-PER-UNIT: 250
    LORE:
    - '&fBuy price: &a$250'
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `GEAR-MENU.TITLE` | `str` | Any string text | `'&8gear - shop'` | Configures the technical `TITLE` parameter for `GEAR-MENU.TITLE` in `shop.yml`. |
| `GEAR-MENU.SIZE` | `int` | Any valid integer number | `'27'` | Configures the technical `SIZE` parameter for `GEAR-MENU.SIZE` in `shop.yml`. |
| `GEAR-MENU.OBSIDIAN-ITEM.CURRENCY` | `str` | Any string text | `'MONEY'` | Configures the technical `CURRENCY` parameter for `GEAR-MENU.OBSIDIAN-ITEM.CURRENCY` in `shop.yml`. |
| `GEAR-MENU.OBSIDIAN-ITEM.MATERIAL` | `str` | Any string text | `'OBSIDIAN'` | Configures the technical `MATERIAL` parameter for `GEAR-MENU.OBSIDIAN-ITEM.MATERIAL` in `shop.yml`. |
| `GEAR-MENU.OBSIDIAN-ITEM.DISPLAY-NAME` | `str` | Any string text | `'&fObsidian'` | Configures the technical `DISPLAY-NAME` parameter for `GEAR-MENU.OBSIDIAN-ITEM.DISPLAY-NAME` in `shop.yml`. |
| `GEAR-MENU.OBSIDIAN-ITEM.SLOT` | `int` | Any valid integer number | `'9'` | Configures the technical `SLOT` parameter for `GEAR-MENU.OBSIDIAN-ITEM.SLOT` in `shop.yml`. |
| `GEAR-MENU.OBSIDIAN-ITEM.PRICE-PER-UNIT` | `int` | Any valid integer number | `'100'` | Configures the technical `PRICE-PER-UNIT` parameter for `GEAR-MENU.OBSIDIAN-ITEM.PRICE-PER-UNIT` in `shop.yml`. |
| `GEAR-MENU.OBSIDIAN-ITEM.LORE` | `list` | List of configured items/strings | `['&fBuy price: &a$100']` | Configures the technical `LORE` parameter for `GEAR-MENU.OBSIDIAN-ITEM.LORE` in `shop.yml`. |
| `GEAR-MENU.END-CRYSTAL-ITEM.CURRENCY` | `str` | Any string text | `'MONEY'` | Configures the technical `CURRENCY` parameter for `GEAR-MENU.END-CRYSTAL-ITEM.CURRENCY` in `shop.yml`. |
| `GEAR-MENU.END-CRYSTAL-ITEM.MATERIAL` | `str` | Any string text | `'END_CRYSTAL'` | Configures the technical `MATERIAL` parameter for `GEAR-MENU.END-CRYSTAL-ITEM.MATERIAL` in `shop.yml`. |
| `GEAR-MENU.END-CRYSTAL-ITEM.DISPLAY-NAME` | `str` | Any string text | `'&bEnd Crystal'` | Configures the technical `DISPLAY-NAME` parameter for `GEAR-MENU.END-CRYSTAL-ITEM.DISPLAY-NAME` in `shop.yml`. |
| `GEAR-MENU.END-CRYSTAL-ITEM.SLOT` | `int` | Any valid integer number | `'10'` | Configures the technical `SLOT` parameter for `GEAR-MENU.END-CRYSTAL-ITEM.SLOT` in `shop.yml`. |
| `GEAR-MENU.END-CRYSTAL-ITEM.PRICE-PER-UNIT` | `int` | Any valid integer number | `'350'` | Configures the technical `PRICE-PER-UNIT` parameter for `GEAR-MENU.END-CRYSTAL-ITEM.PRICE-PER-UNIT` in `shop.yml`. |
| `GEAR-MENU.END-CRYSTAL-ITEM.LORE` | `list` | List of configured items/strings | `['&fBuy price: &a$350']` | Configures the technical `LORE` parameter for `GEAR-MENU.END-CRYSTAL-ITEM.LORE` in `shop.yml`. |
| `GEAR-MENU.RESPAWN-ANCHOR-ITEM.CURRENCY` | `str` | Any string text | `'MONEY'` | Configures the technical `CURRENCY` parameter for `GEAR-MENU.RESPAWN-ANCHOR-ITEM.CURRENCY` in `shop.yml`. |
| `GEAR-MENU.RESPAWN-ANCHOR-ITEM.MATERIAL` | `str` | Any string text | `'RESPAWN_ANCHOR'` | Configures the technical `MATERIAL` parameter for `GEAR-MENU.RESPAWN-ANCHOR-ITEM.MATERIAL` in `shop.yml`. |
| `GEAR-MENU.RESPAWN-ANCHOR-ITEM.DISPLAY-NAME` | `str` | Any string text | `'&fRespawn Anchor'` | Configures the technical `DISPLAY-NAME` parameter for `GEAR-MENU.RESPAWN-ANCHOR-ITEM.DISPLAY-NAME` in `shop.yml`. |
| `GEAR-MENU.RESPAWN-ANCHOR-ITEM.SLOT` | `int` | Any valid integer number | `'11'` | Configures the technical `SLOT` parameter for `GEAR-MENU.RESPAWN-ANCHOR-ITEM.SLOT` in `shop.yml`. |
| `GEAR-MENU.RESPAWN-ANCHOR-ITEM.PRICE-PER-UNIT` | `int` | Any valid integer number | `'1000'` | Configures the technical `PRICE-PER-UNIT` parameter for `GEAR-MENU.RESPAWN-ANCHOR-ITEM.PRICE-PER-UNIT` in `shop.yml`. |
| `GEAR-MENU.RESPAWN-ANCHOR-ITEM.LORE` | `list` | List of configured items/strings | `['&fBuy price: &a$1000']` | Configures the technical `LORE` parameter for `GEAR-MENU.RESPAWN-ANCHOR-ITEM.LORE` in `shop.yml`. |
| `GEAR-MENU.GLOWSTONE-ITEM.CURRENCY` | `str` | Any string text | `'MONEY'` | Configures the technical `CURRENCY` parameter for `GEAR-MENU.GLOWSTONE-ITEM.CURRENCY` in `shop.yml`. |
| `GEAR-MENU.GLOWSTONE-ITEM.MATERIAL` | `str` | Any string text | `'GLOWSTONE'` | Configures the technical `MATERIAL` parameter for `GEAR-MENU.GLOWSTONE-ITEM.MATERIAL` in `shop.yml`. |
| `GEAR-MENU.GLOWSTONE-ITEM.DISPLAY-NAME` | `str` | Any string text | `'&fGlowstone'` | Configures the technical `DISPLAY-NAME` parameter for `GEAR-MENU.GLOWSTONE-ITEM.DISPLAY-NAME` in `shop.yml`. |
| `GEAR-MENU.GLOWSTONE-ITEM.SLOT` | `int` | Any valid integer number | `'12'` | Configures the technical `SLOT` parameter for `GEAR-MENU.GLOWSTONE-ITEM.SLOT` in `shop.yml`. |
| `GEAR-MENU.GLOWSTONE-ITEM.PRICE-PER-UNIT` | `int` | Any valid integer number | `'100'` | Configures the technical `PRICE-PER-UNIT` parameter for `GEAR-MENU.GLOWSTONE-ITEM.PRICE-PER-UNIT` in `shop.yml`. |
| `GEAR-MENU.GLOWSTONE-ITEM.LORE` | `list` | List of configured items/strings | `['&fBuy price: &a$100']` | Configures the technical `LORE` parameter for `GEAR-MENU.GLOWSTONE-ITEM.LORE` in `shop.yml`. |
| `GEAR-MENU.TOTEM-OF-UNDYING-ITEM.CURRENCY` | `str` | Any string text | `'MONEY'` | Configures the technical `CURRENCY` parameter for `GEAR-MENU.TOTEM-OF-UNDYING-ITEM.CURRENCY` in `shop.yml`. |
| `GEAR-MENU.TOTEM-OF-UNDYING-ITEM.MATERIAL` | `str` | Any string text | `'TOTEM_OF_UNDYING'` | Configures the technical `MATERIAL` parameter for `GEAR-MENU.TOTEM-OF-UNDYING-ITEM.MATERIAL` in `shop.yml`. |
| `GEAR-MENU.TOTEM-OF-UNDYING-ITEM.DISPLAY-NAME` | `str` | Any string text | `'&eTotem of Undying'` | Configures the technical `DISPLAY-NAME` parameter for `GEAR-MENU.TOTEM-OF-UNDYING-ITEM.DISPLAY-NAME` in `shop.yml`. |
| `GEAR-MENU.TOTEM-OF-UNDYING-ITEM.SLOT` | `int` | Any valid integer number | `'13'` | Configures the technical `SLOT` parameter for `GEAR-MENU.TOTEM-OF-UNDYING-ITEM.SLOT` in `shop.yml`. |
| *(20 additional sub-keys configured in section)* | | | | |

### 3. Practical Setup Example

```yaml
GEAR-MENU:
  TITLE: '&8gear - shop'
  SIZE: 27
  # Configuration section for Obsidian Item.
  OBSIDIAN-ITEM:
    # The text or value for Currency. Available options: Any valid string text
    CURRENCY: MONEY
    MATERIAL: OBSIDIAN
    DISPLAY-NAME: '&fObsidian'
    SLOT: 9
    # The numerical value for Price Per Unit. Available options: Any valid integer
    PRICE-PER-UNIT: 100
    LORE:
    - '&fBuy price: &a$100'
  # Configuration section for End Crystal Item.
  END-CRYSTAL-ITEM:
    # The text or value for Currency. Available options: Any valid string text
    CURRENCY: MONEY
    MATERIAL: END_CRYSTAL
    DISPLAY-NAME: '&bEnd Crystal'
    SLOT: 10
    # The numerical value for Price Per Unit. Available options: Any valid integer
    PRICE-PER-UNIT: 350
    LORE:
    - '&fBuy price: &a$350'
  # Configuration section for Respawn Anchor Item.
  RESPAWN-ANCHOR-ITEM:
    # The text or value for Currency. Available options: Any valid string text
    CURRENCY: MONEY
    MATERIAL: RESPAWN_A
```

---

## Section: `FOOD-MENU`

### 1. Commented Setup Code Example

```yaml
FOOD-MENU:
  TITLE: '&8food - shop'
  SIZE: 27
  # Configuration section for Potato Item.
  POTATO-ITEM:
    # The text or value for Currency. Available options: Any valid string text
    CURRENCY: MONEY
    MATERIAL: POTATO
    DISPLAY-NAME: '&fPotato'
    SLOT: 9
    # The numerical value for Price Per Unit. Available options: Any valid integer
    PRICE-PER-UNIT: 75
    LORE:
    - '&fBuy price: &a$75'
  # Configuration section for Sweet Berries Item.
  SWEET-BERRIES-ITEM:
    # The text or value for Currency. Available options: Any valid string text
    CURRENCY: MONEY
    MATERIAL: SWEET_BERRIES
    DISPLAY-NAME: '&fSweet Berries'
    SLOT: 10
    # The numerical value for Price Per Unit. Available options: Any valid integer
    PRICE-PER-UNIT: 50
    LORE:
    - '&fBuy price: &a$50'
  # Configuration section for Melon Slice Item.
  MELON-SLICE-ITEM:
    # The text or value for Currency. Available options: Any valid string text
    CURRENCY: MONEY
    MATERIAL: MELON_SLICE
    DISPLAY-NAME: '&fMelon Slice'
    SLOT: 11
    # The numerical value for Price Per Unit. Available options: Any valid integer
    PRICE-PER-UNIT: 10
    LORE:
    - '&fBuy price: &a$10'
  # Configuration section for Carrot Item.
  CARROT-ITEM:
    # The text or value for Currency. Available options: Any valid string text
    CURRENCY: MONEY
    MATERIAL: CARROT
    DISPLAY-NAME: '&fCarrot'
    SLOT: 12
    # The numerical value for Price Per Unit. Available options: Any valid integer
    PRICE-PER-UNIT: 65
    LORE:
    - '&fBuy price: &a$65'
  # Configuration section for Apple Item.
  APPLE-ITEM:
    # The text or value for Currency. Available options: Any valid string text
    CURRENCY: MONEY
    MATERIAL: APPLE
    DISPLAY-NAME: '&fApple'
    SLOT: 13
    # The numerical value for Price Per Unit. Available options: Any valid integer
    PRICE-PER-UNIT: 25
    LORE:
    - '&fBuy price: &a$25'
  # Configuration section for Cooked Chicken Item.
  COOKED-CHICKEN-ITEM:
    # The text or value for Currency. Available options: Any valid string text
    CURRENCY: MONEY
    MATERIAL: COOKED_CHICKEN
    DISPLAY-NAME: '&fApple'
    SLOT: 14
    # The numerical value for Price Per Unit. Available options: Any valid integer
    PRICE-PER-UNIT: 30
    LORE:
    - '&fBuy price: &a$30'
  # Configuration section for Steak Item.
  STEAK-ITEM:
    # The text or value for Currency. Available options: Any valid string text
    CURRENCY: MONEY
    MATERIAL: COOKED_BEEF
    DISPLAY-NAME: '&fSteak'
    SLOT: 15
    # The numerical value for Price Per Unit. Available options: Any valid integer
    PRICE-PER-UNIT: 35
    LORE:
    - '&fBuy price: &a$35'
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `FOOD-MENU.TITLE` | `str` | Any string text | `'&8food - shop'` | Configures the technical `TITLE` parameter for `FOOD-MENU.TITLE` in `shop.yml`. |
| `FOOD-MENU.SIZE` | `int` | Any valid integer number | `'27'` | Configures the technical `SIZE` parameter for `FOOD-MENU.SIZE` in `shop.yml`. |
| `FOOD-MENU.POTATO-ITEM.CURRENCY` | `str` | Any string text | `'MONEY'` | Configures the technical `CURRENCY` parameter for `FOOD-MENU.POTATO-ITEM.CURRENCY` in `shop.yml`. |
| `FOOD-MENU.POTATO-ITEM.MATERIAL` | `str` | Any string text | `'POTATO'` | Configures the technical `MATERIAL` parameter for `FOOD-MENU.POTATO-ITEM.MATERIAL` in `shop.yml`. |
| `FOOD-MENU.POTATO-ITEM.DISPLAY-NAME` | `str` | Any string text | `'&fPotato'` | Configures the technical `DISPLAY-NAME` parameter for `FOOD-MENU.POTATO-ITEM.DISPLAY-NAME` in `shop.yml`. |
| `FOOD-MENU.POTATO-ITEM.SLOT` | `int` | Any valid integer number | `'9'` | Configures the technical `SLOT` parameter for `FOOD-MENU.POTATO-ITEM.SLOT` in `shop.yml`. |
| `FOOD-MENU.POTATO-ITEM.PRICE-PER-UNIT` | `int` | Any valid integer number | `'75'` | Configures the technical `PRICE-PER-UNIT` parameter for `FOOD-MENU.POTATO-ITEM.PRICE-PER-UNIT` in `shop.yml`. |
| `FOOD-MENU.POTATO-ITEM.LORE` | `list` | List of configured items/strings | `['&fBuy price: &a$75']` | Configures the technical `LORE` parameter for `FOOD-MENU.POTATO-ITEM.LORE` in `shop.yml`. |
| `FOOD-MENU.SWEET-BERRIES-ITEM.CURRENCY` | `str` | Any string text | `'MONEY'` | Configures the technical `CURRENCY` parameter for `FOOD-MENU.SWEET-BERRIES-ITEM.CURRENCY` in `shop.yml`. |
| `FOOD-MENU.SWEET-BERRIES-ITEM.MATERIAL` | `str` | Any string text | `'SWEET_BERRIES'` | Configures the technical `MATERIAL` parameter for `FOOD-MENU.SWEET-BERRIES-ITEM.MATERIAL` in `shop.yml`. |
| `FOOD-MENU.SWEET-BERRIES-ITEM.DISPLAY-NAME` | `str` | Any string text | `'&fSweet Berries'` | Configures the technical `DISPLAY-NAME` parameter for `FOOD-MENU.SWEET-BERRIES-ITEM.DISPLAY-NAME` in `shop.yml`. |
| `FOOD-MENU.SWEET-BERRIES-ITEM.SLOT` | `int` | Any valid integer number | `'10'` | Configures the technical `SLOT` parameter for `FOOD-MENU.SWEET-BERRIES-ITEM.SLOT` in `shop.yml`. |
| `FOOD-MENU.SWEET-BERRIES-ITEM.PRICE-PER-UNIT` | `int` | Any valid integer number | `'50'` | Configures the technical `PRICE-PER-UNIT` parameter for `FOOD-MENU.SWEET-BERRIES-ITEM.PRICE-PER-UNIT` in `shop.yml`. |
| `FOOD-MENU.SWEET-BERRIES-ITEM.LORE` | `list` | List of configured items/strings | `['&fBuy price: &a$50']` | Configures the technical `LORE` parameter for `FOOD-MENU.SWEET-BERRIES-ITEM.LORE` in `shop.yml`. |
| `FOOD-MENU.MELON-SLICE-ITEM.CURRENCY` | `str` | Any string text | `'MONEY'` | Configures the technical `CURRENCY` parameter for `FOOD-MENU.MELON-SLICE-ITEM.CURRENCY` in `shop.yml`. |
| `FOOD-MENU.MELON-SLICE-ITEM.MATERIAL` | `str` | Any string text | `'MELON_SLICE'` | Configures the technical `MATERIAL` parameter for `FOOD-MENU.MELON-SLICE-ITEM.MATERIAL` in `shop.yml`. |
| `FOOD-MENU.MELON-SLICE-ITEM.DISPLAY-NAME` | `str` | Any string text | `'&fMelon Slice'` | Configures the technical `DISPLAY-NAME` parameter for `FOOD-MENU.MELON-SLICE-ITEM.DISPLAY-NAME` in `shop.yml`. |
| `FOOD-MENU.MELON-SLICE-ITEM.SLOT` | `int` | Any valid integer number | `'11'` | Configures the technical `SLOT` parameter for `FOOD-MENU.MELON-SLICE-ITEM.SLOT` in `shop.yml`. |
| `FOOD-MENU.MELON-SLICE-ITEM.PRICE-PER-UNIT` | `int` | Any valid integer number | `'10'` | Configures the technical `PRICE-PER-UNIT` parameter for `FOOD-MENU.MELON-SLICE-ITEM.PRICE-PER-UNIT` in `shop.yml`. |
| `FOOD-MENU.MELON-SLICE-ITEM.LORE` | `list` | List of configured items/strings | `['&fBuy price: &a$10']` | Configures the technical `LORE` parameter for `FOOD-MENU.MELON-SLICE-ITEM.LORE` in `shop.yml`. |
| `FOOD-MENU.CARROT-ITEM.CURRENCY` | `str` | Any string text | `'MONEY'` | Configures the technical `CURRENCY` parameter for `FOOD-MENU.CARROT-ITEM.CURRENCY` in `shop.yml`. |
| `FOOD-MENU.CARROT-ITEM.MATERIAL` | `str` | Any string text | `'CARROT'` | Configures the technical `MATERIAL` parameter for `FOOD-MENU.CARROT-ITEM.MATERIAL` in `shop.yml`. |
| `FOOD-MENU.CARROT-ITEM.DISPLAY-NAME` | `str` | Any string text | `'&fCarrot'` | Configures the technical `DISPLAY-NAME` parameter for `FOOD-MENU.CARROT-ITEM.DISPLAY-NAME` in `shop.yml`. |
| `FOOD-MENU.CARROT-ITEM.SLOT` | `int` | Any valid integer number | `'12'` | Configures the technical `SLOT` parameter for `FOOD-MENU.CARROT-ITEM.SLOT` in `shop.yml`. |
| `FOOD-MENU.CARROT-ITEM.PRICE-PER-UNIT` | `int` | Any valid integer number | `'65'` | Configures the technical `PRICE-PER-UNIT` parameter for `FOOD-MENU.CARROT-ITEM.PRICE-PER-UNIT` in `shop.yml`. |
| `FOOD-MENU.CARROT-ITEM.LORE` | `list` | List of configured items/strings | `['&fBuy price: &a$65']` | Configures the technical `LORE` parameter for `FOOD-MENU.CARROT-ITEM.LORE` in `shop.yml`. |
| `FOOD-MENU.APPLE-ITEM.CURRENCY` | `str` | Any string text | `'MONEY'` | Configures the technical `CURRENCY` parameter for `FOOD-MENU.APPLE-ITEM.CURRENCY` in `shop.yml`. |
| `FOOD-MENU.APPLE-ITEM.MATERIAL` | `str` | Any string text | `'APPLE'` | Configures the technical `MATERIAL` parameter for `FOOD-MENU.APPLE-ITEM.MATERIAL` in `shop.yml`. |
| `FOOD-MENU.APPLE-ITEM.DISPLAY-NAME` | `str` | Any string text | `'&fApple'` | Configures the technical `DISPLAY-NAME` parameter for `FOOD-MENU.APPLE-ITEM.DISPLAY-NAME` in `shop.yml`. |
| `FOOD-MENU.APPLE-ITEM.SLOT` | `int` | Any valid integer number | `'13'` | Configures the technical `SLOT` parameter for `FOOD-MENU.APPLE-ITEM.SLOT` in `shop.yml`. |
| *(26 additional sub-keys configured in section)* | | | | |

### 3. Practical Setup Example

```yaml
FOOD-MENU:
  TITLE: '&8food - shop'
  SIZE: 27
  # Configuration section for Potato Item.
  POTATO-ITEM:
    # The text or value for Currency. Available options: Any valid string text
    CURRENCY: MONEY
    MATERIAL: POTATO
    DISPLAY-NAME: '&fPotato'
    SLOT: 9
    # The numerical value for Price Per Unit. Available options: Any valid integer
    PRICE-PER-UNIT: 75
    LORE:
    - '&fBuy price: &a$75'
  # Configuration section for Sweet Berries Item.
  SWEET-BERRIES-ITEM:
    # The text or value for Currency. Available options: Any valid string text
    CURRENCY: MONEY
    MATERIAL: SWEET_BERRIES
    DISPLAY-NAME: '&fSweet Berries'
    SLOT: 10
    # The numerical value for Price Per Unit. Available options: Any valid integer
    PRICE-PER-UNIT: 50
    LORE:
    - '&fBuy price: &a$50'
  # Configuration section for Melon Slice Item.
  MELON-SLICE-ITEM:
    # The text or value for Currency. Available options: Any valid string text
    CURRENCY: MONEY
    MATERIAL: MELON_SLICE
    DIS
```

---

## Section: `SHARD-MENU`

### 1. Commented Setup Code Example

```yaml
SHARD-MENU:
  TITLE: '&8shard - shop'
  SIZE: 27
  # Configuration section for Pig Spawner Item.
  PIG-SPAWNER-ITEM:
    # The text or value for Currency. Available options: Any valid string text
    CURRENCY: SHARD
    MATERIAL: SPAWNER
    DISPLAY-NAME: '&dSpawner'
    SLOT: 9
    # The decimal value for Price Per Unit. Available options: Any decimal number
    PRICE-PER-UNIT: 250.0
    LORE:
    - '&ePig'
    - '&fBuy price: &5250x &lShards'
    # The text or value for Command. Available options: Any valid string text
    COMMAND: spawner give {username} pig {amount}
    # Determines whether Give Item is enabled or disabled. Available options: true, false
    GIVE-ITEM: false
  # Configuration section for Cow Spawner Item.
  COW-SPAWNER-ITEM:
    # The text or value for Currency. Available options: Any valid string text
    CURRENCY: SHARD
    MATERIAL: SPAWNER
    DISPLAY-NAME: '&dSpawner'
    SLOT: 10
    # The decimal value for Price Per Unit. Available options: Any decimal number
    PRICE-PER-UNIT: 350.0
    LORE:
    - '&eCow'
    - '&fBuy price: &5350x &lShards'
    # The text or value for Command. Available options: Any valid string text
    COMMAND: spawner give {username} cow {amount}
    # Determines whether Give Item is enabled or disabled. Available options: true, false
    GIVE-ITEM: false
  # Configuration section for Zombie Spawner Item.
  ZOMBIE-SPAWNER-ITEM:
    # The text or value for Currency. Available options: Any valid string text
    CURRENCY: SHARD
    MATERIAL: SPAWNER
    DISPLAY-NAME: '&dSpawner'
    SLOT: 11
    # The decimal value for Price Per Unit. Available options: Any decimal number
    PRICE-PER-UNIT: 400.0
    LORE:
    - '&eZombie'
    - '&fBuy price: &5400x &lShards'
    # The text or value for Command. Available options: Any valid string text
    COMMAND: spawner give {username} zombie {amount}
    # Determines whether Give Item is enabled or disabled. Available options: true, false
    GIVE-ITEM: false
  # Configuration section for Spider Spawner Item.
  SPIDER-SPAWNER-ITEM:
    # The text or value for Currency. Available options: Any valid string text
    CURRENCY: SHARD
    MATERIAL: SPAWNER
    DISPLAY-NAME: '&dSpawner'
    SLOT: 12
    # The decimal value for Price Per Unit. Available options: Any decimal number
    PRICE-PER-UNIT: 750.0
    LORE:
    - '&eSpider'
    - '&fBuy price: &5750x &lShards'
    # The text or value for Command. Available options: Any valid string text
    COMMAND: spawner give {username} spider {amount}
    # Determines whether Give Item is enabled or disabled. Available options: true, false
    GIVE-ITEM: false
  # Configuration section for Skeleton Spawner Item.
  SKELETON-SPAWNER-ITEM:
    # The text or value for Currency. Available options: Any valid string text
    CURRENCY: SHARD
    MATERIAL: SPAWNER
    DISPLAY-NAME: '&dSpawner'
    SLOT: 13
    # The decimal value for Price Per Unit. Available options: Any decimal number
    PRICE-PER-UNIT: 500.0
    LORE:
    - '&eSkeleton'
    - '&fBuy price: &5500x &lShards'
    # The text or value for Command. Available options: Any valid string text
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `SHARD-MENU.TITLE` | `str` | Any string text | `'&8shard - shop'` | Configures the technical `TITLE` parameter for `SHARD-MENU.TITLE` in `shop.yml`. |
| `SHARD-MENU.SIZE` | `int` | Any valid integer number | `'27'` | Configures the technical `SIZE` parameter for `SHARD-MENU.SIZE` in `shop.yml`. |
| `SHARD-MENU.PIG-SPAWNER-ITEM.CURRENCY` | `str` | Any string text | `'SHARD'` | Configures the technical `CURRENCY` parameter for `SHARD-MENU.PIG-SPAWNER-ITEM.CURRENCY` in `shop.yml`. |
| `SHARD-MENU.PIG-SPAWNER-ITEM.MATERIAL` | `str` | Any string text | `'SPAWNER'` | Configures the technical `MATERIAL` parameter for `SHARD-MENU.PIG-SPAWNER-ITEM.MATERIAL` in `shop.yml`. |
| `SHARD-MENU.PIG-SPAWNER-ITEM.DISPLAY-NAME` | `str` | Any string text | `'&dSpawner'` | Configures the technical `DISPLAY-NAME` parameter for `SHARD-MENU.PIG-SPAWNER-ITEM.DISPLAY-NAME` in `shop.yml`. |
| `SHARD-MENU.PIG-SPAWNER-ITEM.SLOT` | `int` | Any valid integer number | `'9'` | Configures the technical `SLOT` parameter for `SHARD-MENU.PIG-SPAWNER-ITEM.SLOT` in `shop.yml`. |
| `SHARD-MENU.PIG-SPAWNER-ITEM.PRICE-PER-UNIT` | `float` | Any decimal number | `'250.0'` | Configures the technical `PRICE-PER-UNIT` parameter for `SHARD-MENU.PIG-SPAWNER-ITEM.PRICE-PER-UNIT` in `shop.yml`. |
| `SHARD-MENU.PIG-SPAWNER-ITEM.LORE` | `list` | List of configured items/strings | `['&ePig', '&fBuy price: &5250x &lShards']` | Configures the technical `LORE` parameter for `SHARD-MENU.PIG-SPAWNER-ITEM.LORE` in `shop.yml`. |
| `SHARD-MENU.PIG-SPAWNER-ITEM.COMMAND` | `str` | Any string text | `'spawner give {username} pig {amount...'` | Configures the technical `COMMAND` parameter for `SHARD-MENU.PIG-SPAWNER-ITEM.COMMAND` in `shop.yml`. |
| `SHARD-MENU.PIG-SPAWNER-ITEM.GIVE-ITEM` | `bool` | `true`, `false` | `false` | Configures the technical `GIVE-ITEM` parameter for `SHARD-MENU.PIG-SPAWNER-ITEM.GIVE-ITEM` in `shop.yml`. |
| `SHARD-MENU.COW-SPAWNER-ITEM.CURRENCY` | `str` | Any string text | `'SHARD'` | Configures the technical `CURRENCY` parameter for `SHARD-MENU.COW-SPAWNER-ITEM.CURRENCY` in `shop.yml`. |
| `SHARD-MENU.COW-SPAWNER-ITEM.MATERIAL` | `str` | Any string text | `'SPAWNER'` | Configures the technical `MATERIAL` parameter for `SHARD-MENU.COW-SPAWNER-ITEM.MATERIAL` in `shop.yml`. |
| `SHARD-MENU.COW-SPAWNER-ITEM.DISPLAY-NAME` | `str` | Any string text | `'&dSpawner'` | Configures the technical `DISPLAY-NAME` parameter for `SHARD-MENU.COW-SPAWNER-ITEM.DISPLAY-NAME` in `shop.yml`. |
| `SHARD-MENU.COW-SPAWNER-ITEM.SLOT` | `int` | Any valid integer number | `'10'` | Configures the technical `SLOT` parameter for `SHARD-MENU.COW-SPAWNER-ITEM.SLOT` in `shop.yml`. |
| `SHARD-MENU.COW-SPAWNER-ITEM.PRICE-PER-UNIT` | `float` | Any decimal number | `'350.0'` | Configures the technical `PRICE-PER-UNIT` parameter for `SHARD-MENU.COW-SPAWNER-ITEM.PRICE-PER-UNIT` in `shop.yml`. |
| `SHARD-MENU.COW-SPAWNER-ITEM.LORE` | `list` | List of configured items/strings | `['&eCow', '&fBuy price: &5350x &lShards']` | Configures the technical `LORE` parameter for `SHARD-MENU.COW-SPAWNER-ITEM.LORE` in `shop.yml`. |
| `SHARD-MENU.COW-SPAWNER-ITEM.COMMAND` | `str` | Any string text | `'spawner give {username} cow {amount...'` | Configures the technical `COMMAND` parameter for `SHARD-MENU.COW-SPAWNER-ITEM.COMMAND` in `shop.yml`. |
| `SHARD-MENU.COW-SPAWNER-ITEM.GIVE-ITEM` | `bool` | `true`, `false` | `false` | Configures the technical `GIVE-ITEM` parameter for `SHARD-MENU.COW-SPAWNER-ITEM.GIVE-ITEM` in `shop.yml`. |
| `SHARD-MENU.ZOMBIE-SPAWNER-ITEM.CURRENCY` | `str` | Any string text | `'SHARD'` | Configures the technical `CURRENCY` parameter for `SHARD-MENU.ZOMBIE-SPAWNER-ITEM.CURRENCY` in `shop.yml`. |
| `SHARD-MENU.ZOMBIE-SPAWNER-ITEM.MATERIAL` | `str` | Any string text | `'SPAWNER'` | Configures the technical `MATERIAL` parameter for `SHARD-MENU.ZOMBIE-SPAWNER-ITEM.MATERIAL` in `shop.yml`. |
| `SHARD-MENU.ZOMBIE-SPAWNER-ITEM.DISPLAY-NAME` | `str` | Any string text | `'&dSpawner'` | Configures the technical `DISPLAY-NAME` parameter for `SHARD-MENU.ZOMBIE-SPAWNER-ITEM.DISPLAY-NAME` in `shop.yml`. |
| `SHARD-MENU.ZOMBIE-SPAWNER-ITEM.SLOT` | `int` | Any valid integer number | `'11'` | Configures the technical `SLOT` parameter for `SHARD-MENU.ZOMBIE-SPAWNER-ITEM.SLOT` in `shop.yml`. |
| `SHARD-MENU.ZOMBIE-SPAWNER-ITEM.PRICE-PER-UNIT` | `float` | Any decimal number | `'400.0'` | Configures the technical `PRICE-PER-UNIT` parameter for `SHARD-MENU.ZOMBIE-SPAWNER-ITEM.PRICE-PER-UNIT` in `shop.yml`. |
| `SHARD-MENU.ZOMBIE-SPAWNER-ITEM.LORE` | `list` | List of configured items/strings | `['&eZombie', '&fBuy price: &5400x &lShards']` | Configures the technical `LORE` parameter for `SHARD-MENU.ZOMBIE-SPAWNER-ITEM.LORE` in `shop.yml`. |
| `SHARD-MENU.ZOMBIE-SPAWNER-ITEM.COMMAND` | `str` | Any string text | `'spawner give {username} zombie {amo...'` | Configures the technical `COMMAND` parameter for `SHARD-MENU.ZOMBIE-SPAWNER-ITEM.COMMAND` in `shop.yml`. |
| `SHARD-MENU.ZOMBIE-SPAWNER-ITEM.GIVE-ITEM` | `bool` | `true`, `false` | `false` | Configures the technical `GIVE-ITEM` parameter for `SHARD-MENU.ZOMBIE-SPAWNER-ITEM.GIVE-ITEM` in `shop.yml`. |
| `SHARD-MENU.SPIDER-SPAWNER-ITEM.CURRENCY` | `str` | Any string text | `'SHARD'` | Configures the technical `CURRENCY` parameter for `SHARD-MENU.SPIDER-SPAWNER-ITEM.CURRENCY` in `shop.yml`. |
| `SHARD-MENU.SPIDER-SPAWNER-ITEM.MATERIAL` | `str` | Any string text | `'SPAWNER'` | Configures the technical `MATERIAL` parameter for `SHARD-MENU.SPIDER-SPAWNER-ITEM.MATERIAL` in `shop.yml`. |
| `SHARD-MENU.SPIDER-SPAWNER-ITEM.DISPLAY-NAME` | `str` | Any string text | `'&dSpawner'` | Configures the technical `DISPLAY-NAME` parameter for `SHARD-MENU.SPIDER-SPAWNER-ITEM.DISPLAY-NAME` in `shop.yml`. |
| `SHARD-MENU.SPIDER-SPAWNER-ITEM.SLOT` | `int` | Any valid integer number | `'12'` | Configures the technical `SLOT` parameter for `SHARD-MENU.SPIDER-SPAWNER-ITEM.SLOT` in `shop.yml`. |
| *(44 additional sub-keys configured in section)* | | | | |

### 3. Practical Setup Example

```yaml
SHARD-MENU:
  TITLE: '&8shard - shop'
  SIZE: 27
  # Configuration section for Pig Spawner Item.
  PIG-SPAWNER-ITEM:
    # The text or value for Currency. Available options: Any valid string text
    CURRENCY: SHARD
    MATERIAL: SPAWNER
    DISPLAY-NAME: '&dSpawner'
    SLOT: 9
    # The decimal value for Price Per Unit. Available options: Any decimal number
    PRICE-PER-UNIT: 250.0
    LORE:
    - '&ePig'
    - '&fBuy price: &5250x &lShards'
    # The text or value for Command. Available options: Any valid string text
    COMMAND: spawner give {username} pig {amount}
    # Determines whether Give Item is enabled or disabled. Available options: true, false
    GIVE-ITEM: false
  # Configuration section for Cow Spawner Item.
  COW-SPAWNER-ITEM:
    # The text or value for Currency. Available options: Any valid string text
    CURRENCY: SHARD
    MATERIAL: SPAWNER
    DISPLAY-NAME: '&dSpawner'
    SLOT: 10
    # The decimal value for Price Per Unit. Available options: Any decimal numbe
```

---

## Section: `EXTRA-1-MENU`

### 1. Commented Setup Code Example

```yaml
EXTRA-1-MENU:
  TITLE: '&8extra - shop'
  SIZE: 27
  # The numerical value for Back Button Slot. Available options: Any valid integer
  BACK-BUTTON-SLOT: 18
# Configuration section for Extra 2 Menu.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `EXTRA-1-MENU.TITLE` | `str` | Any string text | `'&8extra - shop'` | Configures the technical `TITLE` parameter for `EXTRA-1-MENU.TITLE` in `shop.yml`. |
| `EXTRA-1-MENU.SIZE` | `int` | Any valid integer number | `'27'` | Configures the technical `SIZE` parameter for `EXTRA-1-MENU.SIZE` in `shop.yml`. |
| `EXTRA-1-MENU.BACK-BUTTON-SLOT` | `int` | Any valid integer number | `'18'` | Configures the technical `BACK-BUTTON-SLOT` parameter for `EXTRA-1-MENU.BACK-BUTTON-SLOT` in `shop.yml`. |

### 3. Practical Setup Example

```yaml
EXTRA-1-MENU:
  TITLE: '&8extra - shop'
  SIZE: 27
  # The numerical value for Back Button Slot. Available options: Any valid integer
  BACK-BUTTON-SLOT: 18
# Configuration section for Extra 2 Menu.
```

---

## Section: `EXTRA-2-MENU`

### 1. Commented Setup Code Example

```yaml
EXTRA-2-MENU:
  TITLE: '&8extra - shop'
  SIZE: 27
  # The numerical value for Back Button Slot. Available options: Any valid integer
  BACK-BUTTON-SLOT: 18
# Configuration section for Extra 3 Menu.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `EXTRA-2-MENU.TITLE` | `str` | Any string text | `'&8extra - shop'` | Configures the technical `TITLE` parameter for `EXTRA-2-MENU.TITLE` in `shop.yml`. |
| `EXTRA-2-MENU.SIZE` | `int` | Any valid integer number | `'27'` | Configures the technical `SIZE` parameter for `EXTRA-2-MENU.SIZE` in `shop.yml`. |
| `EXTRA-2-MENU.BACK-BUTTON-SLOT` | `int` | Any valid integer number | `'18'` | Configures the technical `BACK-BUTTON-SLOT` parameter for `EXTRA-2-MENU.BACK-BUTTON-SLOT` in `shop.yml`. |

### 3. Practical Setup Example

```yaml
EXTRA-2-MENU:
  TITLE: '&8extra - shop'
  SIZE: 27
  # The numerical value for Back Button Slot. Available options: Any valid integer
  BACK-BUTTON-SLOT: 18
# Configuration section for Extra 3 Menu.
```

---

## Section: `EXTRA-3-MENU`

### 1. Commented Setup Code Example

```yaml
EXTRA-3-MENU:
  TITLE: '&8extra - shop'
  SIZE: 27
  # The numerical value for Back Button Slot. Available options: Any valid integer
  BACK-BUTTON-SLOT: 18
# Configuration section for Back Button.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `EXTRA-3-MENU.TITLE` | `str` | Any string text | `'&8extra - shop'` | Configures the technical `TITLE` parameter for `EXTRA-3-MENU.TITLE` in `shop.yml`. |
| `EXTRA-3-MENU.SIZE` | `int` | Any valid integer number | `'27'` | Configures the technical `SIZE` parameter for `EXTRA-3-MENU.SIZE` in `shop.yml`. |
| `EXTRA-3-MENU.BACK-BUTTON-SLOT` | `int` | Any valid integer number | `'18'` | Configures the technical `BACK-BUTTON-SLOT` parameter for `EXTRA-3-MENU.BACK-BUTTON-SLOT` in `shop.yml`. |

### 3. Practical Setup Example

```yaml
EXTRA-3-MENU:
  TITLE: '&8extra - shop'
  SIZE: 27
  # The numerical value for Back Button Slot. Available options: Any valid integer
  BACK-BUTTON-SLOT: 18
# Configuration section for Back Button.
```

---

## Section: `BACK-BUTTON`

### 1. Commented Setup Code Example

```yaml
BACK-BUTTON:
  MATERIAL: RED_STAINED_GLASS_PANE
  DISPLAY-NAME: '&cback'
  LORE:
  - '&fClick to return'

# Configuration section for Shop Web Analytics Server
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `BACK-BUTTON.MATERIAL` | `str` | Any string text | `'RED_STAINED_GLASS_PANE'` | Configures the technical `MATERIAL` parameter for `BACK-BUTTON.MATERIAL` in `shop.yml`. |
| `BACK-BUTTON.DISPLAY-NAME` | `str` | Any string text | `'&cback'` | Configures the technical `DISPLAY-NAME` parameter for `BACK-BUTTON.DISPLAY-NAME` in `shop.yml`. |
| `BACK-BUTTON.LORE` | `list` | List of configured items/strings | `['&fClick to return']` | Configures the technical `LORE` parameter for `BACK-BUTTON.LORE` in `shop.yml`. |

### 3. Practical Setup Example

```yaml
BACK-BUTTON:
  MATERIAL: RED_STAINED_GLASS_PANE
  DISPLAY-NAME: '&cback'
  LORE:
  - '&fClick to return'

# Configuration section for Shop Web Analytics Server
```

---

## Section: `SHOP-GUI`

### 1. Commented Setup Code Example

```yaml
SHOP-GUI:
  SHOW-AUCTION-PRICE: true
  FAVORITES:
    ENABLED: true
  WEB-SERVER:
    # Set to true to enable the embedded Shop Analytics web server
    ENABLED: false
    # Port for the embedded web server (e.g. 8080 or 25580)
    PORT: 8080
    # Optional custom domain or public URL (e.g. "https://stats.ultimatedonutsmpmc.net" or "http://192.168.1.15:8080/stats")
    # If left empty (""), it automatically resolves to http://localhost:<PORT>/stats
    PUBLIC-URL: ""
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `SHOP-GUI.SHOW-AUCTION-PRICE` | `bool` | `true`, `false` | `true` | Configures the technical `SHOW-AUCTION-PRICE` parameter for `SHOP-GUI.SHOW-AUCTION-PRICE` in `shop.yml`. |
| `SHOP-GUI.FAVORITES.ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `SHOP-GUI` system. Set to `true` to enable, `false` to disable. |
| `SHOP-GUI.WEB-SERVER.ENABLED` | `bool` | `true`, `false` | `false` | Global toggle for `SHOP-GUI` system. Set to `true` to enable, `false` to disable. |
| `SHOP-GUI.WEB-SERVER.PORT` | `int` | Any valid integer number | `'8080'` | Configures the technical `PORT` parameter for `SHOP-GUI.WEB-SERVER.PORT` in `shop.yml`. |
| `SHOP-GUI.WEB-SERVER.PUBLIC-URL` | `str` | Any string text | `''` | Configures the technical `PUBLIC-URL` parameter for `SHOP-GUI.WEB-SERVER.PUBLIC-URL` in `shop.yml`. |

### 3. Practical Setup Example

```yaml
SHOP-GUI:
  SHOW-AUCTION-PRICE: true
  FAVORITES:
    ENABLED: true
  WEB-SERVER:
    # Set to true to enable the embedded Shop Analytics web server
    ENABLED: false
    # Port for the embedded web server (e.g. 8080 or 25580)
    PORT: 8080
    # Optional custom domain or public URL (e.g. "https://stats.ultimatedonutsmpmc.net" or "http://192.168.1.15:8080/stats")
    # If left empty (""), it automatically resolves to http://localhost:<PORT>/stats
    PUBLIC-URL: ""
```

---

