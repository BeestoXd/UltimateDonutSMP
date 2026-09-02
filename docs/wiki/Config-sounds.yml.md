# Detailed Configuration & Setup Guide: `sounds.yml`

This is the official technical setup guide for `sounds.yml` in **UltimateDonutSMP**.
Each section details the exact commented setup code block, allowed option values, data types, default values, and in-depth functional behavior.

Two sections have no write-up here yet: `CRATES` and `SPAWNERS`. Both take the same
`sound|volume|pitch` value as every entry covered below, so the examples here apply to them
unchanged.

---

## Section: `MENUS`

### 1. Commented Setup Code Example

```yaml
MENUS:
  # The text or value for Button Click. Available options: Any valid string text
  BUTTON-CLICK: minecraft:ui.button.click|1.0|1.0
  # The text or value for Page Turn. Available options: Any valid string text
  PAGE-TURN: minecraft:item.book.page_turn|2.0|0.1
# Configuration section for Spawn.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `MENUS.BUTTON-CLICK` | `str` | Any string text | `'minecraft:ui.button.click|1.0|1.0'` | Configures the technical `BUTTON-CLICK` parameter for `MENUS.BUTTON-CLICK` in `sounds.yml`. |
| `MENUS.PAGE-TURN` | `str` | Any string text | `'minecraft:item.book.page_turn|2.0|0...'` | Configures the technical `PAGE-TURN` parameter for `MENUS.PAGE-TURN` in `sounds.yml`. |

### 3. Practical Setup Example

```yaml
MENUS:
  # The text or value for Button Click. Available options: Any valid string text
  BUTTON-CLICK: minecraft:ui.button.click|1.0|1.0
  # The text or value for Page Turn. Available options: Any valid string text
  PAGE-TURN: minecraft:item.book.page_turn|2.0|0.1
# Configuration section for Spawn.
```

---

## Section: `SPAWN`

### 1. Commented Setup Code Example

```yaml
SPAWN:
  # The text or value for Slime Jump. Available options: Any valid string text
  SLIME-JUMP: minecraft:entity.slime.jump|1.0|1.0
# Configuration section for Sell.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `SPAWN.SLIME-JUMP` | `str` | Any string text | `'minecraft:entity.slime.jump|1.0|1.0'` | Configures the technical `SLIME-JUMP` parameter for `SPAWN.SLIME-JUMP` in `sounds.yml`. |

### 3. Practical Setup Example

```yaml
SPAWN:
  # The text or value for Slime Jump. Available options: Any valid string text
  SLIME-JUMP: minecraft:entity.slime.jump|1.0|1.0
# Configuration section for Sell.
```

---

## Section: `SELL`

### 1. Commented Setup Code Example

```yaml
SELL:
  # The text or value for Level Up. Available options: Any valid string text
  LEVEL-UP: minecraft:entity.player.levelup|1.0|2.0
# Configuration section for Amethyst.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `SELL.LEVEL-UP` | `str` | Any string text | `'minecraft:entity.player.levelup|1.0...'` | Configures the technical `LEVEL-UP` parameter for `SELL.LEVEL-UP` in `sounds.yml`. |

### 3. Practical Setup Example

```yaml
SELL:
  # The text or value for Level Up. Available options: Any valid string text
  LEVEL-UP: minecraft:entity.player.levelup|1.0|2.0
# Configuration section for Amethyst.
```

---

## Section: `AMETHYST`

### 1. Commented Setup Code Example

```yaml
AMETHYST:
  # The text or value for Expired. Available options: Any valid string text
  EXPIRED: minecraft:entity.item.break|1.0|1.0
  # The text or value for Break. Available options: Any valid string text
  BREAK: minecraft:block.amethyst_block.break|1.0|1.0
# Configuration section for Booster.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `AMETHYST.EXPIRED` | `str` | Any string text | `'minecraft:entity.item.break|1.0|1.0'` | Configures the technical `EXPIRED` parameter for `AMETHYST.EXPIRED` in `sounds.yml`. |
| `AMETHYST.BREAK` | `str` | Any string text | `'minecraft:block.amethyst_block.brea...'` | Configures the technical `BREAK` parameter for `AMETHYST.BREAK` in `sounds.yml`. |

### 3. Practical Setup Example

```yaml
AMETHYST:
  # The text or value for Expired. Available options: Any valid string text
  EXPIRED: minecraft:entity.item.break|1.0|1.0
  # The text or value for Break. Available options: Any valid string text
  BREAK: minecraft:block.amethyst_block.break|1.0|1.0
# Configuration section for Booster.
```

---

## Section: `BOOSTER`

### 1. Commented Setup Code Example

```yaml
BOOSTER:
  # The text or value for Error. Available options: Any valid string text
  ERROR: minecraft:entity.villager.no|1.0|1.0
  # The text or value for Success. Available options: Any valid string text
  SUCCESS: minecraft:entity.player.levelup|1.0|1.0
# Configuration section for Shards.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `BOOSTER.ERROR` | `str` | Any string text | `'minecraft:entity.villager.no|1.0|1....'` | Configures the technical `ERROR` parameter for `BOOSTER.ERROR` in `sounds.yml`. |
| `BOOSTER.SUCCESS` | `str` | Any string text | `'minecraft:entity.player.levelup|1.0...'` | Configures the technical `SUCCESS` parameter for `BOOSTER.SUCCESS` in `sounds.yml`. |

### 3. Practical Setup Example

```yaml
BOOSTER:
  # The text or value for Error. Available options: Any valid string text
  ERROR: minecraft:entity.villager.no|1.0|1.0
  # The text or value for Success. Available options: Any valid string text
  SUCCESS: minecraft:entity.player.levelup|1.0|1.0
# Configuration section for Shards.
```

---

## Section: `SHARDS`

### 1. Commented Setup Code Example

```yaml
SHARDS:
  # The text or value for Reward. Available options: Any valid string text
  REWARD: minecraft:entity.experience_orb.pickup|0.85|1.35
  # The text or value for Reward Boosted. Available options: Any valid string text
  REWARD-BOOSTED: minecraft:entity.player.levelup|0.85|1.45
  # The text or value for Cancelled. Available options: Any valid string text
  CANCELLED: minecraft:entity.villager.no|0.8|1.1
# Configuration section for Commands.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `SHARDS.REWARD` | `str` | Any string text | `'minecraft:entity.experience_orb.pic...'` | Configures the technical `REWARD` parameter for `SHARDS.REWARD` in `sounds.yml`. |
| `SHARDS.REWARD-BOOSTED` | `str` | Any string text | `'minecraft:entity.player.levelup|0.8...'` | Configures the technical `REWARD-BOOSTED` parameter for `SHARDS.REWARD-BOOSTED` in `sounds.yml`. |
| `SHARDS.CANCELLED` | `str` | Any string text | `'minecraft:entity.villager.no|0.8|1....'` | Configures the technical `CANCELLED` parameter for `SHARDS.CANCELLED` in `sounds.yml`. |

### 3. Practical Setup Example

```yaml
SHARDS:
  # The text or value for Reward. Available options: Any valid string text
  REWARD: minecraft:entity.experience_orb.pickup|0.85|1.35
  # The text or value for Reward Boosted. Available options: Any valid string text
  REWARD-BOOSTED: minecraft:entity.player.levelup|0.85|1.45
  # The text or value for Cancelled. Available options: Any valid string text
  CANCELLED: minecraft:entity.villager.no|0.8|1.1
# Configuration section for Commands.
```

---

## Section: `COMMANDS`

### 1. Commented Setup Code Example

```yaml
COMMANDS:
  # The text or value for Error. Available options: Any valid string text
  ERROR: minecraft:entity.villager.no|1.0|1.0
# Configuration section for Bucket.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `COMMANDS.ERROR` | `str` | Any string text | `'minecraft:entity.villager.no|1.0|1....'` | Configures the technical `ERROR` parameter for `COMMANDS.ERROR` in `sounds.yml`. |

### 3. Practical Setup Example

```yaml
COMMANDS:
  # The text or value for Error. Available options: Any valid string text
  ERROR: minecraft:entity.villager.no|1.0|1.0
# Configuration section for Bucket.
```

---

## Section: `BUCKET`

### 1. Commented Setup Code Example

```yaml
BUCKET:
  # The text or value for Fill. Available options: Any valid string text
  FILL: minecraft:item.bucket.fill|1.0|1.0
# Configuration section for Tpauto.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `BUCKET.FILL` | `str` | Any string text | `'minecraft:item.bucket.fill|1.0|1.0'` | Configures the technical `FILL` parameter for `BUCKET.FILL` in `sounds.yml`. |

### 3. Practical Setup Example

```yaml
BUCKET:
  # The text or value for Fill. Available options: Any valid string text
  FILL: minecraft:item.bucket.fill|1.0|1.0
# Configuration section for Tpauto.
```

---

## Section: `TPAUTO`

### 1. Commented Setup Code Example

```yaml
TPAUTO:
  # The text or value for Activate. Available options: Any valid string text
  ACTIVATE: minecraft:block.beacon.activate|1.0|1.0
  # The text or value for Deactivate. Available options: Any valid string text
  DEACTIVATE: minecraft:block.beacon.deactivate|1.0|1.0
# Configuration section for Tpa.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `TPAUTO.ACTIVATE` | `str` | Any string text | `'minecraft:block.beacon.activate|1.0...'` | Configures the technical `ACTIVATE` parameter for `TPAUTO.ACTIVATE` in `sounds.yml`. |
| `TPAUTO.DEACTIVATE` | `str` | Any string text | `'minecraft:block.beacon.deactivate|1...'` | Configures the technical `DEACTIVATE` parameter for `TPAUTO.DEACTIVATE` in `sounds.yml`. |

### 3. Practical Setup Example

```yaml
TPAUTO:
  # The text or value for Activate. Available options: Any valid string text
  ACTIVATE: minecraft:block.beacon.activate|1.0|1.0
  # The text or value for Deactivate. Available options: Any valid string text
  DEACTIVATE: minecraft:block.beacon.deactivate|1.0|1.0
# Configuration section for Tpa.
```

---

## Section: `TPA`

### 1. Commented Setup Code Example

```yaml
TPA:
  # The text or value for Request Received. Available options: Any valid string text
  REQUEST-RECEIVED: minecraft:block.note_block.chime|1.0|1.0
  # The text or value for Request Sent. Available options: Any valid string text
  REQUEST-SENT: minecraft:block.note_block.bit|1.0|1.0
  # The text or value for Request Sent Extra. Available options: Any valid string text
  REQUEST-SENT-EXTRA: minecraft:entity.experience_orb.pickup|1.0|1.0
  # The text or value for No Request. Available options: Any valid string text
  NO-REQUEST: minecraft:entity.ender_pearl.throw|1.0|1.0
  # The text or value for Confirm. Available options: Any valid string text
  CONFIRM: minecraft:entity.experience_orb.pickup|1.0|1.0
# Configuration section for Teleport.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `TPA.REQUEST-RECEIVED` | `str` | Any string text | `'minecraft:block.note_block.chime|1....'` | Configures the technical `REQUEST-RECEIVED` parameter for `TPA.REQUEST-RECEIVED` in `sounds.yml`. |
| `TPA.REQUEST-SENT` | `str` | Any string text | `'minecraft:block.note_block.bit|1.0|...'` | Configures the technical `REQUEST-SENT` parameter for `TPA.REQUEST-SENT` in `sounds.yml`. |
| `TPA.REQUEST-SENT-EXTRA` | `str` | Any string text | `'minecraft:entity.experience_orb.pic...'` | Configures the technical `REQUEST-SENT-EXTRA` parameter for `TPA.REQUEST-SENT-EXTRA` in `sounds.yml`. |
| `TPA.NO-REQUEST` | `str` | Any string text | `'minecraft:entity.ender_pearl.throw|...'` | Configures the technical `NO-REQUEST` parameter for `TPA.NO-REQUEST` in `sounds.yml`. |
| `TPA.CONFIRM` | `str` | Any string text | `'minecraft:entity.experience_orb.pic...'` | Configures the technical `CONFIRM` parameter for `TPA.CONFIRM` in `sounds.yml`. |

### 3. Practical Setup Example

```yaml
TPA:
  # The text or value for Request Received. Available options: Any valid string text
  REQUEST-RECEIVED: minecraft:block.note_block.chime|1.0|1.0
  # The text or value for Request Sent. Available options: Any valid string text
  REQUEST-SENT: minecraft:block.note_block.bit|1.0|1.0
  # The text or value for Request Sent Extra. Available options: Any valid string text
  REQUEST-SENT-EXTRA: minecraft:entity.experience_orb.pickup|1.0|1.0
  # The text or value for No Request. Available options: Any valid string text
  NO-REQUEST: minecraft:entity.ender_pearl.throw|1.0|1.0
  # The text or value for Confirm. Available options: Any valid string text
  CONFIRM: minecraft:entity.experience_orb.pickup|1.0|1.0
# Configuration section for Teleport.
```

---

## Section: `TELEPORT`

### 1. Commented Setup Code Example

```yaml
TELEPORT:
  # The text or value for Success. Available options: Any valid string text
  SUCCESS: minecraft:entity.experience_orb.pickup|1.0|1.0
  # The text or value for Countdown. Available options: Any valid string text
  COUNTDOWN: minecraft:entity.enderman.teleport|1.0|1.0
  # The text or value for Cancelled. Available options: Any valid string text
  CANCELLED: minecraft:entity.villager.no|1.0|1.0
# Configuration section for Rtp Zone.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `TELEPORT.SUCCESS` | `str` | Any string text | `'minecraft:entity.experience_orb.pic...'` | Configures the technical `SUCCESS` parameter for `TELEPORT.SUCCESS` in `sounds.yml`. |
| `TELEPORT.COUNTDOWN` | `str` | Any string text | `'minecraft:entity.enderman.teleport|...'` | Configures the technical `COUNTDOWN` parameter for `TELEPORT.COUNTDOWN` in `sounds.yml`. |
| `TELEPORT.CANCELLED` | `str` | Any string text | `'minecraft:entity.villager.no|1.0|1....'` | Configures the technical `CANCELLED` parameter for `TELEPORT.CANCELLED` in `sounds.yml`. |

### 3. Practical Setup Example

```yaml
TELEPORT:
  # The text or value for Success. Available options: Any valid string text
  SUCCESS: minecraft:entity.experience_orb.pickup|1.0|1.0
  # The text or value for Countdown. Available options: Any valid string text
  COUNTDOWN: minecraft:entity.enderman.teleport|1.0|1.0
  # The text or value for Cancelled. Available options: Any valid string text
  CANCELLED: minecraft:entity.villager.no|1.0|1.0
# Configuration section for Rtp Zone.
```

---

## Section: `RTP-ZONE`

### 1. Commented Setup Code Example

```yaml
RTP-ZONE:
  # The text or value for Countdown. Available options: Any valid string text
  COUNTDOWN: minecraft:block.note_block.hat|0.9|1.5
  # The text or value for Cancelled. Available options: Any valid string text
  CANCELLED: minecraft:entity.villager.no|1.0|1.0
# Configuration section for Rtp.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `RTP-ZONE.COUNTDOWN` | `str` | Any string text | `'minecraft:block.note_block.hat|0.9|...'` | Configures the technical `COUNTDOWN` parameter for `RTP-ZONE.COUNTDOWN` in `sounds.yml`. |
| `RTP-ZONE.CANCELLED` | `str` | Any string text | `'minecraft:entity.villager.no|1.0|1....'` | Configures the technical `CANCELLED` parameter for `RTP-ZONE.CANCELLED` in `sounds.yml`. |

### 3. Practical Setup Example

```yaml
RTP-ZONE:
  # The text or value for Countdown. Available options: Any valid string text
  COUNTDOWN: minecraft:block.note_block.hat|0.9|1.5
  # The text or value for Cancelled. Available options: Any valid string text
  CANCELLED: minecraft:entity.villager.no|1.0|1.0
# Configuration section for Rtp.
```

---

## Section: `RTP`

### 1. Commented Setup Code Example

```yaml
RTP:
  # The text or value for Search Start. Available options: Any valid string text
  SEARCH-START: minecraft:block.note_block.pling|0.9|1.1
  # The text or value for Search Tick. Available options: Any valid string text
  SEARCH-TICK: minecraft:block.note_block.hat|0.5|1.6
  # The text or value for Search Found. Available options: Any valid string text
  SEARCH-FOUND: minecraft:entity.experience_orb.pickup|1.0|1.35
  # The text or value for Search Fail. Available options: Any valid string text
  SEARCH-FAIL: minecraft:entity.villager.no|1.0|1.0
# Configuration section for Shop.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `RTP.SEARCH-START` | `str` | Any string text | `'minecraft:block.note_block.pling|0....'` | Configures the technical `SEARCH-START` parameter for `RTP.SEARCH-START` in `sounds.yml`. |
| `RTP.SEARCH-TICK` | `str` | Any string text | `'minecraft:block.note_block.hat|0.5|...'` | Configures the technical `SEARCH-TICK` parameter for `RTP.SEARCH-TICK` in `sounds.yml`. |
| `RTP.SEARCH-FOUND` | `str` | Any string text | `'minecraft:entity.experience_orb.pic...'` | Configures the technical `SEARCH-FOUND` parameter for `RTP.SEARCH-FOUND` in `sounds.yml`. |
| `RTP.SEARCH-FAIL` | `str` | Any string text | `'minecraft:entity.villager.no|1.0|1....'` | Configures the technical `SEARCH-FAIL` parameter for `RTP.SEARCH-FAIL` in `sounds.yml`. |

### 3. Practical Setup Example

```yaml
RTP:
  # The text or value for Search Start. Available options: Any valid string text
  SEARCH-START: minecraft:block.note_block.pling|0.9|1.1
  # The text or value for Search Tick. Available options: Any valid string text
  SEARCH-TICK: minecraft:block.note_block.hat|0.5|1.6
  # The text or value for Search Found. Available options: Any valid string text
  SEARCH-FOUND: minecraft:entity.experience_orb.pickup|1.0|1.35
  # The text or value for Search Fail. Available options: Any valid string text
  SEARCH-FAIL: minecraft:entity.villager.no|1.0|1.0
# Configuration section for Shop.
```

---

## Section: `SHOP`

### 1. Commented Setup Code Example

```yaml
SHOP:
  # The text or value for No Money. Available options: Any valid string text
  NO-MONEY: minecraft:entity.villager.no|1.0|1.0
  # The text or value for Buy Success. Available options: Any valid string text
  BUY-SUCCESS: minecraft:entity.experience_orb.pickup|1.0|1.0
# Configuration section for Drill.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `SHOP.NO-MONEY` | `str` | Any string text | `'minecraft:entity.villager.no|1.0|1....'` | Configures the technical `NO-MONEY` parameter for `SHOP.NO-MONEY` in `sounds.yml`. |
| `SHOP.BUY-SUCCESS` | `str` | Any string text | `'minecraft:entity.experience_orb.pic...'` | Configures the technical `BUY-SUCCESS` parameter for `SHOP.BUY-SUCCESS` in `sounds.yml`. |

### 3. Practical Setup Example

```yaml
SHOP:
  # The text or value for No Money. Available options: Any valid string text
  NO-MONEY: minecraft:entity.villager.no|1.0|1.0
  # The text or value for Buy Success. Available options: Any valid string text
  BUY-SUCCESS: minecraft:entity.experience_orb.pickup|1.0|1.0
# Configuration section for Drill.
```

---

## Section: `DRILL`

### 1. Commented Setup Code Example

```yaml
DRILL:
  # The text or value for Item Break. Available options: Any valid string text
  ITEM-BREAK: minecraft:entity.item.break|1.0|1.0
  # The text or value for Amethyst Break. Available options: Any valid string text
  AMETHYST-BREAK: minecraft:block.amethyst_block.break|1.0|1.0
# Configuration section for Billford.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `DRILL.ITEM-BREAK` | `str` | Any string text | `'minecraft:entity.item.break|1.0|1.0'` | Configures the technical `ITEM-BREAK` parameter for `DRILL.ITEM-BREAK` in `sounds.yml`. |
| `DRILL.AMETHYST-BREAK` | `str` | Any string text | `'minecraft:block.amethyst_block.brea...'` | Configures the technical `AMETHYST-BREAK` parameter for `DRILL.AMETHYST-BREAK` in `sounds.yml`. |

### 3. Practical Setup Example

```yaml
DRILL:
  # The text or value for Item Break. Available options: Any valid string text
  ITEM-BREAK: minecraft:entity.item.break|1.0|1.0
  # The text or value for Amethyst Break. Available options: Any valid string text
  AMETHYST-BREAK: minecraft:block.amethyst_block.break|1.0|1.0
# Configuration section for Billford.
```

---

## Section: `BILLFORD`

### 1. Commented Setup Code Example

```yaml
BILLFORD:
  # The text or value for Open. Available options: Any valid string text
  OPEN: minecraft:entity.villager.trade|1.0|1.1
  # The text or value for Rotate. Available options: Any valid string text
  ROTATE: minecraft:block.beacon.activate|0.8|1.2
  # The text or value for Success. Available options: Any valid string text
  SUCCESS: minecraft:entity.player.levelup|1.0|1.5
  # The text or value for Fail. Available options: Any valid string text
  FAIL: minecraft:entity.villager.no|1.0|1.0
# Configuration section for Auction House.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `BILLFORD.OPEN` | `str` | Any string text | `'minecraft:entity.villager.trade|1.0...'` | Configures the technical `OPEN` parameter for `BILLFORD.OPEN` in `sounds.yml`. |
| `BILLFORD.ROTATE` | `str` | Any string text | `'minecraft:block.beacon.activate|0.8...'` | Configures the technical `ROTATE` parameter for `BILLFORD.ROTATE` in `sounds.yml`. |
| `BILLFORD.SUCCESS` | `str` | Any string text | `'minecraft:entity.player.levelup|1.0...'` | Configures the technical `SUCCESS` parameter for `BILLFORD.SUCCESS` in `sounds.yml`. |
| `BILLFORD.FAIL` | `str` | Any string text | `'minecraft:entity.villager.no|1.0|1....'` | Configures the technical `FAIL` parameter for `BILLFORD.FAIL` in `sounds.yml`. |

### 3. Practical Setup Example

```yaml
BILLFORD:
  # The text or value for Open. Available options: Any valid string text
  OPEN: minecraft:entity.villager.trade|1.0|1.1
  # The text or value for Rotate. Available options: Any valid string text
  ROTATE: minecraft:block.beacon.activate|0.8|1.2
  # The text or value for Success. Available options: Any valid string text
  SUCCESS: minecraft:entity.player.levelup|1.0|1.5
  # The text or value for Fail. Available options: Any valid string text
  FAIL: minecraft:entity.villager.no|1.0|1.0
# Configuration section for Auction House.
```

---

## Section: `AUCTION_HOUSE`

### 1. Commented Setup Code Example

```yaml
AUCTION_HOUSE:
  # The text or value for Success. Available options: Any valid string text
  SUCCESS: minecraft:entity.experience_orb.pickup|1.0|1.2
  # The text or value for Fail. Available options: Any valid string text
  FAIL: minecraft:entity.villager.no|1.0|1.0
# Configuration section for Orders.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `AUCTION_HOUSE.SUCCESS` | `str` | Any string text | `'minecraft:entity.experience_orb.pic...'` | Configures the technical `SUCCESS` parameter for `AUCTION_HOUSE.SUCCESS` in `sounds.yml`. |
| `AUCTION_HOUSE.FAIL` | `str` | Any string text | `'minecraft:entity.villager.no|1.0|1....'` | Configures the technical `FAIL` parameter for `AUCTION_HOUSE.FAIL` in `sounds.yml`. |

### 3. Practical Setup Example

```yaml
AUCTION_HOUSE:
  # The text or value for Success. Available options: Any valid string text
  SUCCESS: minecraft:entity.experience_orb.pickup|1.0|1.2
  # The text or value for Fail. Available options: Any valid string text
  FAIL: minecraft:entity.villager.no|1.0|1.0
# Configuration section for Orders.
```

---

## Section: `ORDERS`

### 1. Commented Setup Code Example

```yaml
ORDERS:
  # The text or value for Success. Available options: Any valid string text
  SUCCESS: minecraft:entity.experience_orb.pickup|1.0|1.2
  # The text or value for Fail. Available options: Any valid string text
  FAIL: minecraft:entity.villager.no|1.0|1.0
# Configuration section for Duels.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `ORDERS.SUCCESS` | `str` | Any string text | `'minecraft:entity.experience_orb.pic...'` | Configures the technical `SUCCESS` parameter for `ORDERS.SUCCESS` in `sounds.yml`. |
| `ORDERS.FAIL` | `str` | Any string text | `'minecraft:entity.villager.no|1.0|1....'` | Configures the technical `FAIL` parameter for `ORDERS.FAIL` in `sounds.yml`. |

### 3. Practical Setup Example

```yaml
ORDERS:
  # The text or value for Success. Available options: Any valid string text
  SUCCESS: minecraft:entity.experience_orb.pickup|1.0|1.2
  # The text or value for Fail. Available options: Any valid string text
  FAIL: minecraft:entity.villager.no|1.0|1.0
# Configuration section for Duels.
```

---

## Section: `DUELS`

### 1. Commented Setup Code Example

```yaml
DUELS:
  # The text or value for Click. Available options: Any valid string text
  CLICK: minecraft:ui.button.click|1.0|1.0
  # The text or value for Request Sent. Available options: Any valid string text
  REQUEST-SENT: minecraft:entity.experience_orb.pickup|1.0|1.2
  # The text or value for Request Received. Available options: Any valid string text
  REQUEST-RECEIVED: minecraft:block.note_block.pling|1.0|1.0
  # The text or value for Queue Join. Available options: Any valid string text
  QUEUE-JOIN: minecraft:block.note_block.hat|1.0|1.0
  # Configuration section for Start Countdown.
  START-COUNTDOWN:
    # Configuration section for Per Second.
    PER-SECOND:
      # The text or value for 5. Available options: Any valid string text
      5: minecraft:block.note_block.hat|1.0|1.0
      # The text or value for 4. Available options: Any valid string text
      4: minecraft:block.note_block.hat|1.0|1.0
      # The text or value for 3. Available options: Any valid string text
      3: minecraft:block.note_block.hat|1.0|1.0
      # The text or value for 2. Available options: Any valid string text
      2: minecraft:block.note_block.hat|1.0|1.0
      # The text or value for 1. Available options: Any valid string text
      1: minecraft:block.note_block.hat|1.0|1.0
    # The text or value for Start Sound. Available options: Any valid string text
    START-SOUND: minecraft:entity.firework_rocket.blast|1.0|1.0
  # The text or value for Match Found. Available options: Any valid string text
  MATCH-FOUND: minecraft:block.beacon.activate|1.0|1.1
  # The text or value for Match Start. Available options: Any valid string text
  MATCH-START: minecraft:entity.player.levelup|1.0|1.0
  # The text or value for Victory. Available options: Any valid string text
  VICTORY: minecraft:ui.toast.challenge_complete|1.0|1.0
  # The text or value for Defeat. Available options: Any valid string text
  DEFEAT: minecraft:entity.villager.no|1.0|1.0
  # The text or value for Claim. Available options: Any valid string text
  CLAIM: minecraft:entity.item.pickup|1.0|1.0
# Configuration section for Key All.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `DUELS.CLICK` | `str` | Any string text | `'minecraft:ui.button.click|1.0|1.0'` | Configures the technical `CLICK` parameter for `DUELS.CLICK` in `sounds.yml`. |
| `DUELS.REQUEST-SENT` | `str` | Any string text | `'minecraft:entity.experience_orb.pic...'` | Configures the technical `REQUEST-SENT` parameter for `DUELS.REQUEST-SENT` in `sounds.yml`. |
| `DUELS.REQUEST-RECEIVED` | `str` | Any string text | `'minecraft:block.note_block.pling|1....'` | Configures the technical `REQUEST-RECEIVED` parameter for `DUELS.REQUEST-RECEIVED` in `sounds.yml`. |
| `DUELS.QUEUE-JOIN` | `str` | Any string text | `'minecraft:block.note_block.hat|1.0|...'` | Configures the technical `QUEUE-JOIN` parameter for `DUELS.QUEUE-JOIN` in `sounds.yml`. |
| `DUELS.START-COUNTDOWN.PER-SECOND.5` | `str` | Any string text | `'minecraft:block.note_block.hat|1.0|...'` | Configures the technical `5` parameter for `DUELS.START-COUNTDOWN.PER-SECOND.5` in `sounds.yml`. |
| `DUELS.START-COUNTDOWN.PER-SECOND.4` | `str` | Any string text | `'minecraft:block.note_block.hat|1.0|...'` | Configures the technical `4` parameter for `DUELS.START-COUNTDOWN.PER-SECOND.4` in `sounds.yml`. |
| `DUELS.START-COUNTDOWN.PER-SECOND.3` | `str` | Any string text | `'minecraft:block.note_block.hat|1.0|...'` | Configures the technical `3` parameter for `DUELS.START-COUNTDOWN.PER-SECOND.3` in `sounds.yml`. |
| `DUELS.START-COUNTDOWN.PER-SECOND.2` | `str` | Any string text | `'minecraft:block.note_block.hat|1.0|...'` | Configures the technical `2` parameter for `DUELS.START-COUNTDOWN.PER-SECOND.2` in `sounds.yml`. |
| `DUELS.START-COUNTDOWN.PER-SECOND.1` | `str` | Any string text | `'minecraft:block.note_block.hat|1.0|...'` | Configures the technical `1` parameter for `DUELS.START-COUNTDOWN.PER-SECOND.1` in `sounds.yml`. |
| `DUELS.START-COUNTDOWN.START-SOUND` | `str` | Any string text | `'minecraft:entity.firework_rocket.bl...'` | Configures the technical `START-SOUND` parameter for `DUELS.START-COUNTDOWN.START-SOUND` in `sounds.yml`. |
| `DUELS.MATCH-FOUND` | `str` | Any string text | `'minecraft:block.beacon.activate|1.0...'` | Configures the technical `MATCH-FOUND` parameter for `DUELS.MATCH-FOUND` in `sounds.yml`. |
| `DUELS.MATCH-START` | `str` | Any string text | `'minecraft:entity.player.levelup|1.0...'` | Configures the technical `MATCH-START` parameter for `DUELS.MATCH-START` in `sounds.yml`. |
| `DUELS.VICTORY` | `str` | Any string text | `'minecraft:ui.toast.challenge_comple...'` | Configures the technical `VICTORY` parameter for `DUELS.VICTORY` in `sounds.yml`. |
| `DUELS.DEFEAT` | `str` | Any string text | `'minecraft:entity.villager.no|1.0|1....'` | Configures the technical `DEFEAT` parameter for `DUELS.DEFEAT` in `sounds.yml`. |
| `DUELS.CLAIM` | `str` | Any string text | `'minecraft:entity.item.pickup|1.0|1....'` | Configures the technical `CLAIM` parameter for `DUELS.CLAIM` in `sounds.yml`. |

### 3. Practical Setup Example

```yaml
DUELS:
  # The text or value for Click. Available options: Any valid string text
  CLICK: minecraft:ui.button.click|1.0|1.0
  # The text or value for Request Sent. Available options: Any valid string text
  REQUEST-SENT: minecraft:entity.experience_orb.pickup|1.0|1.2
  # The text or value for Request Received. Available options: Any valid string text
  REQUEST-RECEIVED: minecraft:block.note_block.pling|1.0|1.0
  # The text or value for Queue Join. Available options: Any valid string text
  QUEUE-JOIN: minecraft:block.note_block.hat|1.0|1.0
  # Configuration section for Start Countdown.
  START-COUNTDOWN:
    # Configuration section for Per Second.
    PER-SECOND:
      # The text or value for 5. Available options: Any valid string text
      5: minecraft:block.note_block.hat|1.0|1.0
      # The text or value for 4. Available options: Any valid string text
      4: minecraft:block.note_block.hat|1.0|1.0
      # The text or value for 3. Available options: Any valid string text
      3: m
```

---

## Section: `KEY-ALL`

### 1. Commented Setup Code Example

```yaml
KEY-ALL:
  # The text or value for Reward. Available options: Any valid string text
  REWARD: minecraft:entity.player.levelup|1.0|1.1
# Configuration section for Crates.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `KEY-ALL.REWARD` | `str` | Any string text | `'minecraft:entity.player.levelup|1.0...'` | Configures the technical `REWARD` parameter for `KEY-ALL.REWARD` in `sounds.yml`. |

### 3. Practical Setup Example

```yaml
KEY-ALL:
  # The text or value for Reward. Available options: Any valid string text
  REWARD: minecraft:entity.player.levelup|1.0|1.1
# Configuration section for Crates.
```

---

