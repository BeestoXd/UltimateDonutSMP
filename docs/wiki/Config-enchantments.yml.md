# Detailed Configuration & Setup Guide: `enchantments.yml`

This is the technical setup guide for `enchantments.yml` in **UltimateDonutSMP**.
The file drives the enchantment picker: which enchantments each kind of item can be given, at
what level, and where each option sits in the menu.

Two things are worth knowing before you edit it. The keys in this file are lower case, unlike
every other config the plugin ships, and the plugin matches them exactly as written, so `gui.title`
works and `GUI.TITLE` does not. Get the case wrong and nothing complains; the built in default is
used instead.

The second is `trident`. The plugin looks for a `trident` section alongside the fourteen below,
and the file does not ship one, so tridents have no options until you add it yourself.

---

## Section: `messages`

### 1. Commented Setup Code Example

```yaml
messages:
  # The text or value for Select. Available options: Any valid string text
  select: '&fClick to select'
  # The text or value for Selected. Available options: Any valid string text
  selected: '&aSelected'
  # The text or value for Cannot. Available options: Any valid string text
  cannot: '&fCannot add this enchantment'
# Configuration section for Gui.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `messages.select` | `str` | Any string text | `'&fClick to select'` | The lore line on an option the player has not picked yet. |
| `messages.selected` | `str` | Any string text | `'&aSelected'` | The lore line on an option they have picked. |
| `messages.cannot` | `str` | Any string text | `'&fCannot add this enchantment'` | The lore line on an option that conflicts with something already chosen. |

### 3. Practical Setup Example

```yaml
messages:
  # The text or value for Select. Available options: Any valid string text
  select: '&fClick to select'
  # The text or value for Selected. Available options: Any valid string text
  selected: '&aSelected'
  # The text or value for Cannot. Available options: Any valid string text
  cannot: '&fCannot add this enchantment'
# Configuration section for Gui.
```

---
## Section: `gui`

### 1. Commented Setup Code Example

```yaml
gui:
  # The text or value for Title. Available options: Any valid string text
  title: '&#444444pick enchantments'
  # The numerical value for Rows. Available options: Any valid integer
  rows: 6
  # Configuration section for Slots.
  slots:
    # The numerical value for Item. Available options: Any valid integer
    item: 0
    # The numerical value for Cancel. Available options: Any valid integer
    cancel: 46
    # The numerical value for Prev. Available options: Any valid integer
    prev: 45
    # The numerical value for Next. Available options: Any valid integer
    next: 53
    # The numerical value for Confirm. Available options: Any valid integer
    confirm: 52
# Configuration section for Helmet.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `gui.title` | `str` | Any string text | `'&#444444pick enchantments'` | The menu title. |
| `gui.rows` | `int` | `1` to `6` | `6` | How tall the menu is. Slot numbers are counted against this, so shrinking it can push options off the bottom. |
| `gui.slots.item` | `int` | A slot number | `0` | Where the item being enchanted is previewed. |
| `gui.slots.cancel` | `int` | A slot number | `46` | The cancel button. |
| `gui.slots.prev` | `int` | A slot number | `45` | The previous page button. |
| `gui.slots.next` | `int` | A slot number | `53` | The next page button. |
| `gui.slots.confirm` | `int` | A slot number | `52` | The confirm button. |

`gui.buttons` and `gui.sounds` used to sit in this section, describing button materials, names,
lore, filler panes and a click sound. No code ever read them, under any casing, so they were taken
out rather than left advertising settings that did nothing. The cancel, confirm and page arrows are
built into the menu; changing them needs a code change rather than a config one.

### 3. Practical Setup Example

```yaml
gui:
  # The text or value for Title. Available options: Any valid string text
  title: '&#444444pick enchantments'
  # The numerical value for Rows. Available options: Any valid integer
  rows: 6
  # Configuration section for Slots.
  slots:
    # The numerical value for Item. Available options: Any valid integer
    item: 0
    # The numerical value for Cancel. Available options: Any valid integer
    cancel: 46
    # The numerical value for Prev. Available options: Any valid integer
    prev: 45
    # The numerical value for Next. Available options: Any valid integer
    next: 53
    # The numerical value for Confirm. Available options: Any valid integer
    confirm: 52
# Configuration section for Helmet.
```

---
## Section: `helmet`

### 1. Commented Setup Code Example

```yaml
helmet:
  # Configuration section for Protection1.
  protection1:
    # The text or value for Enchantment. Available options: Any valid string text
    enchantment: protection;1
    # The numerical value for Slot. Available options: Any valid integer
    slot: 2
    # The numerical value for Page. Available options: Any valid integer
    page: 1
  # Configuration section for Protection2.
  protection2:
    # The text or value for Enchantment. Available options: Any valid string text
    enchantment: protection;2
    # The numerical value for Slot. Available options: Any valid integer
    slot: 3
    # The numerical value for Page. Available options: Any valid integer
    page: 1
  # ... 31 more options for this item, each in the same shape
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `helmet.<option>` | `section` | Any lower case id | 33 shipped | One clickable option in the menu for a helmet. The id is only a label; it is never shown to the player and only has to be unique inside this item. |
| `helmet.<option>.enchantment` | `str` | `name;level` | for example `aqua_affinity;1` | The enchantment and the level the option grants, separated by a semicolon. The name is lower cased and a `minecraft:` prefix is stripped, so `Sharpness;5` and `minecraft:sharpness;5` both work. An unrecognised name means the option is quietly skipped, but a level that is not a whole number throws while the file is loading, so keep it numeric. |
| `helmet.<option>.slot` | `int` | `0` to one less than `rows` times nine | none, so `0` | Where the option sits in the menu. Two options sharing a slot on the same page means only one of them is reachable, so check the layout after adding any. |
| `helmet.<option>.page` | `int` | `1` or higher | `1` | Which page the option appears on. The menu works out its own page count from the highest number used here, so raising this on one option is all it takes to add a page. |

The shipped list gives a helmet 33 options across pages 1 and 2: `aqua_affinity` 1, `binding_curse` 1, `blast_protection` 1 to 4, `fire_protection` 1 to 4, `mending` 1, `projectile_protection` 1 to 4, `protection` 1 to 4, `respiration` 1 to 3, `thorns` 1 to 3, `unbreaking` 1 to 3, `vanishing_curse` 1.

Removing an option takes it out of the menu without touching anything a player already has enchanted.

### 3. Practical Setup Example

```yaml
helmet:
  # keep the shipped options and add one more on a new page
  custom_option:
    enchantment: aqua_affinity;1
    slot: 10
    page: 3
```

---
## Section: `chestplate`

### 1. Commented Setup Code Example

```yaml
chestplate:
  # Configuration section for Protection1.
  protection1:
    # The text or value for Enchantment. Available options: Any valid string text
    enchantment: protection;1
    # The numerical value for Slot. Available options: Any valid integer
    slot: 2
    # The numerical value for Page. Available options: Any valid integer
    page: 1
  # Configuration section for Protection2.
  protection2:
    # The text or value for Enchantment. Available options: Any valid string text
    enchantment: protection;2
    # The numerical value for Slot. Available options: Any valid integer
    slot: 3
    # The numerical value for Page. Available options: Any valid integer
    page: 1
  # ... 27 more options for this item, each in the same shape
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `chestplate.<option>` | `section` | Any lower case id | 29 shipped | One clickable option in the menu for a chestplate. The id is only a label; it is never shown to the player and only has to be unique inside this item. |
| `chestplate.<option>.enchantment` | `str` | `name;level` | for example `binding_curse;1` | The enchantment and the level the option grants, separated by a semicolon. The name is lower cased and a `minecraft:` prefix is stripped, so `Sharpness;5` and `minecraft:sharpness;5` both work. An unrecognised name means the option is quietly skipped, but a level that is not a whole number throws while the file is loading, so keep it numeric. |
| `chestplate.<option>.slot` | `int` | `0` to one less than `rows` times nine | none, so `0` | Where the option sits in the menu. Two options sharing a slot on the same page means only one of them is reachable, so check the layout after adding any. |
| `chestplate.<option>.page` | `int` | `1` or higher | `1` | Which page the option appears on. The menu works out its own page count from the highest number used here, so raising this on one option is all it takes to add a page. |

The shipped list gives a chestplate 29 options across pages 1 and 2: `binding_curse` 1, `blast_protection` 1 to 4, `fire_protection` 1 to 4, `mending` 1, `projectile_protection` 1 to 4, `protection` 1 to 4, `thorns` 1 to 3, `unbreaking` 1 to 3, `vanishing_curse` 1.

Removing an option takes it out of the menu without touching anything a player already has enchanted.

### 3. Practical Setup Example

```yaml
chestplate:
  # keep the shipped options and add one more on a new page
  custom_option:
    enchantment: binding_curse;1
    slot: 10
    page: 3
```

---
## Section: `leggings`

### 1. Commented Setup Code Example

```yaml
leggings:
  # Configuration section for Protection1.
  protection1:
    # The text or value for Enchantment. Available options: Any valid string text
    enchantment: protection;1
    # The numerical value for Slot. Available options: Any valid integer
    slot: 2
    # The numerical value for Page. Available options: Any valid integer
    page: 1
  # Configuration section for Protection2.
  protection2:
    # The text or value for Enchantment. Available options: Any valid string text
    enchantment: protection;2
    # The numerical value for Slot. Available options: Any valid integer
    slot: 3
    # The numerical value for Page. Available options: Any valid integer
    page: 1
  # ... 30 more options for this item, each in the same shape
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `leggings.<option>` | `section` | Any lower case id | 32 shipped | One clickable option in the menu for a leggings. The id is only a label; it is never shown to the player and only has to be unique inside this item. |
| `leggings.<option>.enchantment` | `str` | `name;level` | for example `binding_curse;1` | The enchantment and the level the option grants, separated by a semicolon. The name is lower cased and a `minecraft:` prefix is stripped, so `Sharpness;5` and `minecraft:sharpness;5` both work. An unrecognised name means the option is quietly skipped, but a level that is not a whole number throws while the file is loading, so keep it numeric. |
| `leggings.<option>.slot` | `int` | `0` to one less than `rows` times nine | none, so `0` | Where the option sits in the menu. Two options sharing a slot on the same page means only one of them is reachable, so check the layout after adding any. |
| `leggings.<option>.page` | `int` | `1` or higher | `1` | Which page the option appears on. The menu works out its own page count from the highest number used here, so raising this on one option is all it takes to add a page. |

The shipped list gives a leggings 32 options across pages 1 and 2: `binding_curse` 1, `blast_protection` 1 to 4, `fire_protection` 1 to 4, `mending` 1, `projectile_protection` 1 to 4, `protection` 1 to 4, `swift_sneak` 1 to 3, `thorns` 1 to 3, `unbreaking` 1 to 3, `vanishing_curse` 1.

Removing an option takes it out of the menu without touching anything a player already has enchanted.

### 3. Practical Setup Example

```yaml
leggings:
  # keep the shipped options and add one more on a new page
  custom_option:
    enchantment: binding_curse;1
    slot: 10
    page: 3
```

---
## Section: `boots`

### 1. Commented Setup Code Example

```yaml
boots:
  # Configuration section for Protection1.
  protection1:
    # The text or value for Enchantment. Available options: Any valid string text
    enchantment: protection;1
    # The numerical value for Slot. Available options: Any valid integer
    slot: 2
    # The numerical value for Page. Available options: Any valid integer
    page: 1
  # Configuration section for Protection2.
  protection2:
    # The text or value for Enchantment. Available options: Any valid string text
    enchantment: protection;2
    # The numerical value for Slot. Available options: Any valid integer
    slot: 3
    # The numerical value for Page. Available options: Any valid integer
    page: 1
  # ... 42 more options for this item, each in the same shape
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `boots.<option>` | `section` | Any lower case id | 44 shipped | One clickable option in the menu for a boots. The id is only a label; it is never shown to the player and only has to be unique inside this item. |
| `boots.<option>.enchantment` | `str` | `name;level` | for example `binding_curse;1` | The enchantment and the level the option grants, separated by a semicolon. The name is lower cased and a `minecraft:` prefix is stripped, so `Sharpness;5` and `minecraft:sharpness;5` both work. An unrecognised name means the option is quietly skipped, but a level that is not a whole number throws while the file is loading, so keep it numeric. |
| `boots.<option>.slot` | `int` | `0` to one less than `rows` times nine | none, so `0` | Where the option sits in the menu. Two options sharing a slot on the same page means only one of them is reachable, so check the layout after adding any. |
| `boots.<option>.page` | `int` | `1` or higher | `1` | Which page the option appears on. The menu works out its own page count from the highest number used here, so raising this on one option is all it takes to add a page. |

The shipped list gives a boots 44 options across pages 1 and 2: `binding_curse` 1, `blast_protection` 1 to 4, `depth_strider` 1 to 3, `feather_falling` 1 to 4, `fire_protection` 1 to 4, `frost_walker` 1 to 2, `mending` 1, `projectile_protection` 1 to 4, `protection` 1 to 4, `soul_speed` 1 to 3, `swift_sneak` 1 to 3, `thorns` 1 to 3, `unbreaking` 1 to 3, `vanishing_curse` 1.

Removing an option takes it out of the menu without touching anything a player already has enchanted.

### 3. Practical Setup Example

```yaml
boots:
  # keep the shipped options and add one more on a new page
  custom_option:
    enchantment: binding_curse;1
    slot: 10
    page: 3
```

---
## Section: `elytra`

### 1. Commented Setup Code Example

```yaml
elytra:
  # Configuration section for Mending1.
  mending1:
    # The text or value for Enchantment. Available options: Any valid string text
    enchantment: mending;1
    # The numerical value for Slot. Available options: Any valid integer
    slot: 18
    # The numerical value for Page. Available options: Any valid integer
    page: 1
  # Configuration section for Unbreaking1.
  unbreaking1:
    # The text or value for Enchantment. Available options: Any valid string text
    enchantment: unbreaking;1
    # The numerical value for Slot. Available options: Any valid integer
    slot: 17
    # The numerical value for Page. Available options: Any valid integer
    page: 1
  # ... 4 more options for this item, each in the same shape
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `elytra.<option>` | `section` | Any lower case id | 6 shipped | One clickable option in the menu for a elytra. The id is only a label; it is never shown to the player and only has to be unique inside this item. |
| `elytra.<option>.enchantment` | `str` | `name;level` | for example `binding_curse;1` | The enchantment and the level the option grants, separated by a semicolon. The name is lower cased and a `minecraft:` prefix is stripped, so `Sharpness;5` and `minecraft:sharpness;5` both work. An unrecognised name means the option is quietly skipped, but a level that is not a whole number throws while the file is loading, so keep it numeric. |
| `elytra.<option>.slot` | `int` | `0` to one less than `rows` times nine | none, so `0` | Where the option sits in the menu. Two options sharing a slot on the same page means only one of them is reachable, so check the layout after adding any. |
| `elytra.<option>.page` | `int` | `1` or higher | `1` | Which page the option appears on. The menu works out its own page count from the highest number used here, so raising this on one option is all it takes to add a page. |

The shipped list gives a elytra 6 options across one page: `binding_curse` 1, `mending` 1, `unbreaking` 1 to 3, `vanishing_curse` 1.

Removing an option takes it out of the menu without touching anything a player already has enchanted.

### 3. Practical Setup Example

```yaml
elytra:
  # keep the shipped options and add one more on a new page
  custom_option:
    enchantment: binding_curse;1
    slot: 10
    page: 2
```

---
## Section: `bow`

### 1. Commented Setup Code Example

```yaml
bow:
  # Configuration section for Power1.
  power1:
    # The text or value for Enchantment. Available options: Any valid string text
    enchantment: power;1
    # The numerical value for Slot. Available options: Any valid integer
    slot: 2
    # The numerical value for Page. Available options: Any valid integer
    page: 1
  # Configuration section for Power2.
  power2:
    # The text or value for Enchantment. Available options: Any valid string text
    enchantment: power;2
    # The numerical value for Slot. Available options: Any valid integer
    slot: 3
    # The numerical value for Page. Available options: Any valid integer
    page: 1
  # ... 12 more options for this item, each in the same shape
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `bow.<option>` | `section` | Any lower case id | 14 shipped | One clickable option in the menu for a bow. The id is only a label; it is never shown to the player and only has to be unique inside this item. |
| `bow.<option>.enchantment` | `str` | `name;level` | for example `flame;1` | The enchantment and the level the option grants, separated by a semicolon. The name is lower cased and a `minecraft:` prefix is stripped, so `Sharpness;5` and `minecraft:sharpness;5` both work. An unrecognised name means the option is quietly skipped, but a level that is not a whole number throws while the file is loading, so keep it numeric. |
| `bow.<option>.slot` | `int` | `0` to one less than `rows` times nine | none, so `0` | Where the option sits in the menu. Two options sharing a slot on the same page means only one of them is reachable, so check the layout after adding any. |
| `bow.<option>.page` | `int` | `1` or higher | `1` | Which page the option appears on. The menu works out its own page count from the highest number used here, so raising this on one option is all it takes to add a page. |

The shipped list gives a bow 14 options across one page: `flame` 1, `infinity` 1, `mending` 1, `power` 1 to 5, `punch` 1 to 2, `unbreaking` 1 to 3, `vanishing_curse` 1.

Removing an option takes it out of the menu without touching anything a player already has enchanted.

### 3. Practical Setup Example

```yaml
bow:
  # keep the shipped options and add one more on a new page
  custom_option:
    enchantment: flame;1
    slot: 10
    page: 2
```

---
## Section: `crossbow`

### 1. Commented Setup Code Example

```yaml
crossbow:
  # Configuration section for Multishot.
  multishot:
    # The text or value for Enchantment. Available options: Any valid string text
    enchantment: multishot;1
    # The numerical value for Slot. Available options: Any valid integer
    slot: 2
    # The numerical value for Page. Available options: Any valid integer
    page: 1
  # Configuration section for Quick Charge1.
  quick_charge1:
    # The text or value for Enchantment. Available options: Any valid string text
    enchantment: quick_charge;1
    # The numerical value for Slot. Available options: Any valid integer
    slot: 11
    # The numerical value for Page. Available options: Any valid integer
    page: 1
  # ... 11 more options for this item, each in the same shape
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `crossbow.<option>` | `section` | Any lower case id | 13 shipped | One clickable option in the menu for a crossbow. The id is only a label; it is never shown to the player and only has to be unique inside this item. |
| `crossbow.<option>.enchantment` | `str` | `name;level` | for example `mending;1` | The enchantment and the level the option grants, separated by a semicolon. The name is lower cased and a `minecraft:` prefix is stripped, so `Sharpness;5` and `minecraft:sharpness;5` both work. An unrecognised name means the option is quietly skipped, but a level that is not a whole number throws while the file is loading, so keep it numeric. |
| `crossbow.<option>.slot` | `int` | `0` to one less than `rows` times nine | none, so `0` | Where the option sits in the menu. Two options sharing a slot on the same page means only one of them is reachable, so check the layout after adding any. |
| `crossbow.<option>.page` | `int` | `1` or higher | `1` | Which page the option appears on. The menu works out its own page count from the highest number used here, so raising this on one option is all it takes to add a page. |

The shipped list gives a crossbow 13 options across one page: `mending` 1, `multishot` 1, `piercing` 1 to 4, `quick_charge` 1 to 3, `unbreaking` 1 to 3, `vanishing_curse` 1.

Removing an option takes it out of the menu without touching anything a player already has enchanted.

### 3. Practical Setup Example

```yaml
crossbow:
  # keep the shipped options and add one more on a new page
  custom_option:
    enchantment: mending;1
    slot: 10
    page: 2
```

---
## Section: `fishing_rod`

### 1. Commented Setup Code Example

```yaml
fishing_rod:
  # Configuration section for Mending.
  mending:
    # The text or value for Enchantment. Available options: Any valid string text
    enchantment: mending;1
    # The numerical value for Slot. Available options: Any valid integer
    slot: 18
    # The numerical value for Page. Available options: Any valid integer
    page: 1
  # Configuration section for Unbreaking1.
  unbreaking1:
    # The text or value for Enchantment. Available options: Any valid string text
    enchantment: unbreaking;1
    # The numerical value for Slot. Available options: Any valid integer
    slot: 17
    # The numerical value for Page. Available options: Any valid integer
    page: 1
  # ... 9 more options for this item, each in the same shape
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `fishing_rod.<option>` | `section` | Any lower case id | 11 shipped | One clickable option in the menu for a fishing rod. The id is only a label; it is never shown to the player and only has to be unique inside this item. |
| `fishing_rod.<option>.enchantment` | `str` | `name;level` | for example `luck_of_the_sea;1` | The enchantment and the level the option grants, separated by a semicolon. The name is lower cased and a `minecraft:` prefix is stripped, so `Sharpness;5` and `minecraft:sharpness;5` both work. An unrecognised name means the option is quietly skipped, but a level that is not a whole number throws while the file is loading, so keep it numeric. |
| `fishing_rod.<option>.slot` | `int` | `0` to one less than `rows` times nine | none, so `0` | Where the option sits in the menu. Two options sharing a slot on the same page means only one of them is reachable, so check the layout after adding any. |
| `fishing_rod.<option>.page` | `int` | `1` or higher | `1` | Which page the option appears on. The menu works out its own page count from the highest number used here, so raising this on one option is all it takes to add a page. |

The shipped list gives a fishing rod 11 options across one page: `luck_of_the_sea` 1 to 3, `lure` 1 to 3, `mending` 1, `unbreaking` 1 to 3, `vanishing_curse` 1.

Removing an option takes it out of the menu without touching anything a player already has enchanted.

### 3. Practical Setup Example

```yaml
fishing_rod:
  # keep the shipped options and add one more on a new page
  custom_option:
    enchantment: luck_of_the_sea;1
    slot: 10
    page: 2
```

---
## Section: `shovel`

### 1. Commented Setup Code Example

```yaml
shovel:
  # Configuration section for Mending.
  mending:
    # The text or value for Enchantment. Available options: Any valid string text
    enchantment: mending;1
    # The numerical value for Slot. Available options: Any valid integer
    slot: 18
    # The numerical value for Page. Available options: Any valid integer
    page: 1
  # Configuration section for Unbreaking1.
  unbreaking1:
    # The text or value for Enchantment. Available options: Any valid string text
    enchantment: unbreaking;1
    # The numerical value for Slot. Available options: Any valid integer
    slot: 17
    # The numerical value for Page. Available options: Any valid integer
    page: 1
  # ... 12 more options for this item, each in the same shape
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `shovel.<option>` | `section` | Any lower case id | 14 shipped | One clickable option in the menu for a shovel. The id is only a label; it is never shown to the player and only has to be unique inside this item. |
| `shovel.<option>.enchantment` | `str` | `name;level` | for example `efficiency;1` | The enchantment and the level the option grants, separated by a semicolon. The name is lower cased and a `minecraft:` prefix is stripped, so `Sharpness;5` and `minecraft:sharpness;5` both work. An unrecognised name means the option is quietly skipped, but a level that is not a whole number throws while the file is loading, so keep it numeric. |
| `shovel.<option>.slot` | `int` | `0` to one less than `rows` times nine | none, so `0` | Where the option sits in the menu. Two options sharing a slot on the same page means only one of them is reachable, so check the layout after adding any. |
| `shovel.<option>.page` | `int` | `1` or higher | `1` | Which page the option appears on. The menu works out its own page count from the highest number used here, so raising this on one option is all it takes to add a page. |

The shipped list gives a shovel 14 options across one page: `efficiency` 1 to 5, `fortune` 1 to 3, `mending` 1, `silk_touch` 1, `unbreaking` 1 to 3, `vanishing_curse` 1.

Removing an option takes it out of the menu without touching anything a player already has enchanted.

### 3. Practical Setup Example

```yaml
shovel:
  # keep the shipped options and add one more on a new page
  custom_option:
    enchantment: efficiency;1
    slot: 10
    page: 2
```

---
## Section: `pickaxe`

### 1. Commented Setup Code Example

```yaml
pickaxe:
  # Configuration section for Mending.
  mending:
    # The text or value for Enchantment. Available options: Any valid string text
    enchantment: mending;1
    # The numerical value for Slot. Available options: Any valid integer
    slot: 18
    # The numerical value for Page. Available options: Any valid integer
    page: 1
  # Configuration section for Unbreaking1.
  unbreaking1:
    # The text or value for Enchantment. Available options: Any valid string text
    enchantment: unbreaking;1
    # The numerical value for Slot. Available options: Any valid integer
    slot: 17
    # The numerical value for Page. Available options: Any valid integer
    page: 1
  # ... 12 more options for this item, each in the same shape
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `pickaxe.<option>` | `section` | Any lower case id | 14 shipped | One clickable option in the menu for a pickaxe. The id is only a label; it is never shown to the player and only has to be unique inside this item. |
| `pickaxe.<option>.enchantment` | `str` | `name;level` | for example `efficiency;1` | The enchantment and the level the option grants, separated by a semicolon. The name is lower cased and a `minecraft:` prefix is stripped, so `Sharpness;5` and `minecraft:sharpness;5` both work. An unrecognised name means the option is quietly skipped, but a level that is not a whole number throws while the file is loading, so keep it numeric. |
| `pickaxe.<option>.slot` | `int` | `0` to one less than `rows` times nine | none, so `0` | Where the option sits in the menu. Two options sharing a slot on the same page means only one of them is reachable, so check the layout after adding any. |
| `pickaxe.<option>.page` | `int` | `1` or higher | `1` | Which page the option appears on. The menu works out its own page count from the highest number used here, so raising this on one option is all it takes to add a page. |

The shipped list gives a pickaxe 14 options across one page: `efficiency` 1 to 5, `fortune` 1 to 3, `mending` 1, `silk_touch` 1, `unbreaking` 1 to 3, `vanishing_curse` 1.

Removing an option takes it out of the menu without touching anything a player already has enchanted.

### 3. Practical Setup Example

```yaml
pickaxe:
  # keep the shipped options and add one more on a new page
  custom_option:
    enchantment: efficiency;1
    slot: 10
    page: 2
```

---
## Section: `axe`

### 1. Commented Setup Code Example

```yaml
axe:
  # Configuration section for Mending.
  mending:
    # The text or value for Enchantment. Available options: Any valid string text
    enchantment: mending;1
    # The numerical value for Slot. Available options: Any valid integer
    slot: 18
    # The numerical value for Page. Available options: Any valid integer
    page: 1
  # Configuration section for Mending201.
  mending201:
    # The text or value for Enchantment. Available options: Any valid string text
    enchantment: mending;1
    # The numerical value for Slot. Available options: Any valid integer
    slot: 18
    # The numerical value for Page. Available options: Any valid integer
    page: 2
  # ... 31 more options for this item, each in the same shape
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `axe.<option>` | `section` | Any lower case id | 33 shipped | One clickable option in the menu for a axe. The id is only a label; it is never shown to the player and only has to be unique inside this item. |
| `axe.<option>.enchantment` | `str` | `name;level` | for example `bane_of_arthropods;1` | The enchantment and the level the option grants, separated by a semicolon. The name is lower cased and a `minecraft:` prefix is stripped, so `Sharpness;5` and `minecraft:sharpness;5` both work. An unrecognised name means the option is quietly skipped, but a level that is not a whole number throws while the file is loading, so keep it numeric. |
| `axe.<option>.slot` | `int` | `0` to one less than `rows` times nine | none, so `0` | Where the option sits in the menu. Two options sharing a slot on the same page means only one of them is reachable, so check the layout after adding any. |
| `axe.<option>.page` | `int` | `1` or higher | `1` | Which page the option appears on. The menu works out its own page count from the highest number used here, so raising this on one option is all it takes to add a page. |

The shipped list gives a axe 33 options across pages 1 and 2: `bane_of_arthropods` 1 to 5, `efficiency` 1 to 5, `fortune` 1 to 3, `mending` 1, `sharpness` 1 to 5, `silk_touch` 1, `smite` 1 to 5, `unbreaking` 1 to 3, `vanishing_curse` 1.

Removing an option takes it out of the menu without touching anything a player already has enchanted.

### 3. Practical Setup Example

```yaml
axe:
  # keep the shipped options and add one more on a new page
  custom_option:
    enchantment: bane_of_arthropods;1
    slot: 10
    page: 3
```

---
## Section: `hoe`

### 1. Commented Setup Code Example

```yaml
hoe:
  # Configuration section for Mending.
  mending:
    # The text or value for Enchantment. Available options: Any valid string text
    enchantment: mending;1
    # The numerical value for Slot. Available options: Any valid integer
    slot: 18
    # The numerical value for Page. Available options: Any valid integer
    page: 1
  # Configuration section for Unbreaking1.
  unbreaking1:
    # The text or value for Enchantment. Available options: Any valid string text
    enchantment: unbreaking;1
    # The numerical value for Slot. Available options: Any valid integer
    slot: 17
    # The numerical value for Page. Available options: Any valid integer
    page: 1
  # ... 12 more options for this item, each in the same shape
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `hoe.<option>` | `section` | Any lower case id | 14 shipped | One clickable option in the menu for a hoe. The id is only a label; it is never shown to the player and only has to be unique inside this item. |
| `hoe.<option>.enchantment` | `str` | `name;level` | for example `efficiency;1` | The enchantment and the level the option grants, separated by a semicolon. The name is lower cased and a `minecraft:` prefix is stripped, so `Sharpness;5` and `minecraft:sharpness;5` both work. An unrecognised name means the option is quietly skipped, but a level that is not a whole number throws while the file is loading, so keep it numeric. |
| `hoe.<option>.slot` | `int` | `0` to one less than `rows` times nine | none, so `0` | Where the option sits in the menu. Two options sharing a slot on the same page means only one of them is reachable, so check the layout after adding any. |
| `hoe.<option>.page` | `int` | `1` or higher | `1` | Which page the option appears on. The menu works out its own page count from the highest number used here, so raising this on one option is all it takes to add a page. |

The shipped list gives a hoe 14 options across one page: `efficiency` 1 to 5, `fortune` 1 to 3, `mending` 1, `silk_touch` 1, `unbreaking` 1 to 3, `vanishing_curse` 1.

Removing an option takes it out of the menu without touching anything a player already has enchanted.

### 3. Practical Setup Example

```yaml
hoe:
  # keep the shipped options and add one more on a new page
  custom_option:
    enchantment: efficiency;1
    slot: 10
    page: 2
```

---
## Section: `shield`

### 1. Commented Setup Code Example

```yaml
shield:
  # Configuration section for Mending.
  mending:
    # The text or value for Enchantment. Available options: Any valid string text
    enchantment: mending;1
    # The numerical value for Slot. Available options: Any valid integer
    slot: 18
    # The numerical value for Page. Available options: Any valid integer
    page: 1
  # Configuration section for Unbreaking1.
  unbreaking1:
    # The text or value for Enchantment. Available options: Any valid string text
    enchantment: unbreaking;1
    # The numerical value for Slot. Available options: Any valid integer
    slot: 17
    # The numerical value for Page. Available options: Any valid integer
    page: 1
  # ... 3 more options for this item, each in the same shape
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `shield.<option>` | `section` | Any lower case id | 5 shipped | One clickable option in the menu for a shield. The id is only a label; it is never shown to the player and only has to be unique inside this item. |
| `shield.<option>.enchantment` | `str` | `name;level` | for example `mending;1` | The enchantment and the level the option grants, separated by a semicolon. The name is lower cased and a `minecraft:` prefix is stripped, so `Sharpness;5` and `minecraft:sharpness;5` both work. An unrecognised name means the option is quietly skipped, but a level that is not a whole number throws while the file is loading, so keep it numeric. |
| `shield.<option>.slot` | `int` | `0` to one less than `rows` times nine | none, so `0` | Where the option sits in the menu. Two options sharing a slot on the same page means only one of them is reachable, so check the layout after adding any. |
| `shield.<option>.page` | `int` | `1` or higher | `1` | Which page the option appears on. The menu works out its own page count from the highest number used here, so raising this on one option is all it takes to add a page. |

The shipped list gives a shield 5 options across one page: `mending` 1, `unbreaking` 1 to 3, `vanishing_curse` 1.

Removing an option takes it out of the menu without touching anything a player already has enchanted.

### 3. Practical Setup Example

```yaml
shield:
  # keep the shipped options and add one more on a new page
  custom_option:
    enchantment: mending;1
    slot: 10
    page: 2
```

---
## Section: `sword`

### 1. Commented Setup Code Example

```yaml
sword:
  # Configuration section for Mending.
  mending:
    # The text or value for Enchantment. Available options: Any valid string text
    enchantment: mending;1
    # The numerical value for Slot. Available options: Any valid integer
    slot: 18
    # The numerical value for Page. Available options: Any valid integer
    page: 1
  # Configuration section for Mending201.
  mending201:
    # The text or value for Enchantment. Available options: Any valid string text
    enchantment: mending;1
    # The numerical value for Slot. Available options: Any valid integer
    slot: 18
    # The numerical value for Page. Available options: Any valid integer
    page: 2
  # ... 32 more options for this item, each in the same shape
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `sword.<option>` | `section` | Any lower case id | 34 shipped | One clickable option in the menu for a sword. The id is only a label; it is never shown to the player and only has to be unique inside this item. |
| `sword.<option>.enchantment` | `str` | `name;level` | for example `bane_of_arthropods;1` | The enchantment and the level the option grants, separated by a semicolon. The name is lower cased and a `minecraft:` prefix is stripped, so `Sharpness;5` and `minecraft:sharpness;5` both work. An unrecognised name means the option is quietly skipped, but a level that is not a whole number throws while the file is loading, so keep it numeric. |
| `sword.<option>.slot` | `int` | `0` to one less than `rows` times nine | none, so `0` | Where the option sits in the menu. Two options sharing a slot on the same page means only one of them is reachable, so check the layout after adding any. |
| `sword.<option>.page` | `int` | `1` or higher | `1` | Which page the option appears on. The menu works out its own page count from the highest number used here, so raising this on one option is all it takes to add a page. |

The shipped list gives a sword 34 options across pages 1 and 2: `bane_of_arthropods` 1 to 5, `fire_aspect` 1 to 2, `knockback` 1 to 2, `looting` 1 to 3, `mending` 1, `sharpness` 1 to 5, `smite` 1 to 5, `sweeping_edge` 1 to 3, `unbreaking` 1 to 3, `vanishing_curse` 1.

Removing an option takes it out of the menu without touching anything a player already has enchanted.

### 3. Practical Setup Example

```yaml
sword:
  # keep the shipped options and add one more on a new page
  custom_option:
    enchantment: bane_of_arthropods;1
    slot: 10
    page: 3
```

---
