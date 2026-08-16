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
  # Rows every player gets when no ROW-PERMISSIONS entry applies to them
  # 6 rows is 54 slots, already the largest a chest can be, so lower this if you want
  # ROW-PERMISSIONS below to hand out bigger chests as a rank perk
  # Available options: 1 to 6
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
  # Per-rank Ender Chest size resolved from permissions
  ROW-PERMISSIONS:
    # Enable or disable permission based Ender Chest sizes
    ENABLED: true
    # What happens when a player's permissions no longer cover the size their chest was saved at,
    # for example after a rank expires
    # KEEP-SIZE keeps the chest at the larger size so nothing can be lost
    # RETURN-ITEMS shrinks the chest and hands back everything that no longer fits
    # Available options: KEEP-SIZE, RETURN-ITEMS
    ON-DOWNGRADE: KEEP-SIZE
    # Explicit mapping from permission node to Ender Chest rows
    # Players can also be given ultimatedonutsmp.enderchest.rows.<1-6> directly, for example
    # ultimatedonutsmp.enderchest.rows.4 for a 36 slot chest
    # The highest value the player has wins
    # Players without any of these permissions keep DEFAULT-ROWS above
    PERMISSIONS:
      "ultimatedonutsmp.enderchest.rows.vip++": 6
      "ultimatedonutsmp.enderchest.rows.vip+": 5
      "ultimatedonutsmp.enderchest.rows.vip": 4
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
| `ENDER-CHEST.DEFAULT-ROWS` | `int` | `1` to `6` | `'6'` | Rows every player gets when no `ROW-PERMISSIONS` entry applies to them. One row is 9 slots, so `6` is 54 slots, already the largest a chest can be. Lower it if you want `ROW-PERMISSIONS` to hand out bigger chests as a rank perk. |
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
  # Rows every player gets when no ROW-PERMISSIONS entry applies to them
  # 6 rows is 54 slots, already the largest a chest can be, so lower this if you want
  # ROW-PERMISSIONS below to hand out bigger chests as a rank perk
  # Available options: 1 to 6
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
  # Per-rank Ender Chest size resolved from permissions
  ROW-PERMISSIONS:
    # Enable or disable permission based Ender Chest sizes
    ENABLED: true
    # What happens when a player's permissions no longer cover the size their chest was saved at,
    # for example after a rank expires
    # KEEP-SIZE keeps the chest at the larger size so nothing can be lost
    # RETURN-ITEMS shrinks the chest and hands back everything that no longer fits
    # Available options: KEEP-SIZE, RETURN-ITEMS
    ON-DOWNGRADE: KEEP-SIZE
    # Explicit mapping from permission node to Ender Chest rows
    # Players can also be given ultimatedonutsmp.enderchest.rows.<1-6> directly, for example
    # ultimatedonutsmp.enderchest.rows.4 for a 36 slot chest
    # The highest value the player has wins
    # Players without any of these permissions keep DEFAULT-ROWS above
    PERMISSIONS:
      "ultimatedonutsmp.enderchest.rows.vip++": 6
      "ultimatedonutsmp.enderchest.rows.vip+": 5
      "ultimatedonutsmp.enderchest.rows.vip": 4
  # Configuration section for Ecsee.
  ECSEE:
    # Determines whether Enabled is enabled or disabled. Available options: true, false
    ENABLED: 
```

---

## Section: `ENDER-CHEST.ROW-PERMISSIONS`

Per-rank Ender Chest sizes resolved from permissions, so different ranks can have different chest sizes
without a separate config entry per rank.

### 1. Commented Setup Code Example

```yaml
  # Per-rank Ender Chest size resolved from permissions
  ROW-PERMISSIONS:
    # Enable or disable permission based Ender Chest sizes
    ENABLED: true
    # What happens when a player's permissions no longer cover the size their chest was saved at,
    # for example after a rank expires
    # KEEP-SIZE keeps the chest at the larger size so nothing can be lost
    # RETURN-ITEMS shrinks the chest and hands back everything that no longer fits
    # Available options: KEEP-SIZE, RETURN-ITEMS
    ON-DOWNGRADE: KEEP-SIZE
    # Explicit mapping from permission node to Ender Chest rows
    # Players can also be given ultimatedonutsmp.enderchest.rows.<1-6> directly, for example
    # ultimatedonutsmp.enderchest.rows.4 for a 36 slot chest
    # The highest value the player has wins
    # Players without any of these permissions keep DEFAULT-ROWS above
    PERMISSIONS:
      "ultimatedonutsmp.enderchest.rows.vip++": 6
      "ultimatedonutsmp.enderchest.rows.vip+": 5
      "ultimatedonutsmp.enderchest.rows.vip": 4
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `ENDER-CHEST.ROW-PERMISSIONS.ENABLED` | `bool` | `true`, `false` | `true` | Master toggle. When `false` every player gets `DEFAULT-ROWS` and all row permissions are ignored. |
| `ENDER-CHEST.ROW-PERMISSIONS.ON-DOWNGRADE` | `str` | `KEEP-SIZE`, `RETURN-ITEMS` | `'KEEP-SIZE'` | What happens when a player's permissions no longer cover the size their chest was saved at. See **Losing a rank** below. |
| `ENDER-CHEST.ROW-PERMISSIONS.PERMISSIONS` | `section` | Permission node to rows | See example | Maps an arbitrary permission node to a row count. Use this to reuse rank nodes you already have instead of adding numeric nodes. |

### 3. Row Sizes

| Rows | Slots | Equivalent |
| :--- | :--- | :--- |
| `1` | 9 | One hotbar |
| `2` | 18 | |
| `3` | 27 | A vanilla Ender Chest |
| `4` | 36 | |
| `5` | 45 | |
| `6` | 54 | A double chest, the largest a chest can be |

### 4. Resolution Order

1. Every entry under `PERMISSIONS` the player holds is collected.
2. Every `ultimatedonutsmp.enderchest.rows.<1-6>` node the player holds is collected. A non-numeric
   suffix is ignored, and a value above 6 counts as 6.
3. The **highest** collected value becomes the player size, so a player with both `.rows.2` and
   `.rows.4` gets 36 slots.
4. If the player holds none of these nodes, `DEFAULT-ROWS` applies.

Wildcards do not grant a tier. `ultimatedonutsmp.*` leaves the player on `DEFAULT-ROWS`, the same way it
does for `ultimatedonutsmp.homes.<amount>`, so a staff wildcard cannot quietly resize everyone.

The size is evaluated every time the chest is opened, so a rank change applies on the next open.

### 5. Losing a Rank

A chest remembers the size it was last saved at. If a player's permissions later cover fewer rows than
that — an expired rank, a removed node, a lowered `DEFAULT-ROWS` — `ON-DOWNGRADE` decides what happens:

- `KEEP-SIZE` (default) leaves the chest at the larger size. Nothing can be lost, and the player keeps
  the extra space until an admin clears it.
- `RETURN-ITEMS` shrinks the chest to the size the player is now entitled to and hands back everything
  that no longer fits, into their inventory or onto the ground if their inventory is full. They are told
  what happened through `MESSAGES.ROWS-DOWNGRADED`.

`RETURN-ITEMS` writes the smaller chest to the database before handing anything back, so a failed
hand-back cannot duplicate items, and a failed write leaves the chest at its old size.

### 6. Practical Setup Example

Give the default rank a vanilla-sized chest and sell the bigger ones as a rank perk:

```yaml
ENDER-CHEST:
  DEFAULT-ROWS: 3
  ROW-PERMISSIONS:
    ENABLED: true
    ON-DOWNGRADE: RETURN-ITEMS
    PERMISSIONS:
      "group.vip": 4
      "group.vip+": 5
      "group.mvp": 6
```

Or skip the config entirely and drive it from LuckPerms alone:

```
/lp group vip permission set ultimatedonutsmp.enderchest.rows.4
/lp group vip+ permission set ultimatedonutsmp.enderchest.rows.5
/lp group mvp permission set ultimatedonutsmp.enderchest.rows.6
```

`/ecsee <player>` always opens a target's chest at its real stored size, so staff see exactly what the
player sees.

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
  # Shown when a chest shrinks and the items that no longer fit are handed back
  # The text or value for Rows Downgraded. Available options: Any valid string text
  ROWS-DOWNGRADED: '&eYour Ender Chest is now {rows} rows. {amount} item(s) that no longer fit were returned to you.'
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
| `MESSAGES.ROWS-DOWNGRADED` | `str` | Any string text | `'&eYour Ender Chest is now {rows} row...'` | Sent when `ROW-PERMISSIONS.ON-DOWNGRADE` is `RETURN-ITEMS` and a shrinking chest hands items back. `{rows}` is the new row count and `{amount}` is how many items were returned. |

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
  # Shown when a chest shrinks and the items that no longer fit are handed back
  # The text or value for Rows Downgraded. Available options: Any valid string text
  ROWS-DOWNGRADED: '&eYour Ender Chest is now {rows} rows. {amount} item(s) that no longer fit were returned to you.'
```

---

