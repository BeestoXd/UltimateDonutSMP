# Detailed Configuration & Setup Guide: `death-messages.yml`

This is the official, 100% complete technical setup guide for `death-messages.yml` in **UltimateDonutSMP**.
Each section details the exact commented setup code block, allowed option values, data types, default values, and in-depth functional behavior.

---

## Section: `SETTINGS`

### 1. Commented Setup Code Example

```yaml
SETTINGS:
  # Determines whether Radius is enabled or disabled. Available options: true, false
  RADIUS: true
  # The numerical value for Chunks. Available options: Any valid integer
  CHUNKS: 5
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `SETTINGS.RADIUS` | `bool` | `true`, `false` | `true` | If `true`, death messages are restricted to nearby chunk radii. Set to `false` to broadcast death messages GLOBALLY server-wide. |
| `SETTINGS.CHUNKS` | `int` | Any valid integer number | `'5'` | Configures the technical `CHUNKS` parameter for `SETTINGS.CHUNKS` in `death-messages.yml`. |

### 3. Practical Setup Example

```yaml
SETTINGS:
  # Determines whether Radius is enabled or disabled. Available options: true, false
  RADIUS: true
  # The numerical value for Chunks. Available options: Any valid integer
  CHUNKS: 5
```

---

## Section: `MESSAGES`

### 1. Commented Setup Code Example

```yaml
MESSAGES:
  # Determines whether Enabled is enabled or disabled. Available options: true, false
  ENABLED: true
  # The text or value for Prefix. Available options: Any valid string text
  PREFIX: '&c☠ '
  # The text or value for Block Explosion. Available options: Any valid string text
  BLOCK-EXPLOSION: '{player} got blown to pieces'
  # The text or value for Contact. Available options: Any valid string text
  CONTACT: '{player} was pricked to death'
  # Configuration section for Drowning.
  DROWNING:
    # The text or value for Normal. Available options: Any valid string text
    NORMAL: '{player} drowned!'
    # The text or value for Pvp. Available options: Any valid string text
    PVP: '{player} drowned whilst trying to escape {killer}'
  # The text or value for Entity Attack. Available options: Any valid string text
  ENTITY-ATTACK: '{player} was slain by {killer}'
  # Configuration section for Fall.
  FALL:
    # The text or value for Normal. Available options: Any valid string text
    NORMAL: '{player} hit the ground too hard'
    # The text or value for Pvp. Available options: Any valid string text
    PVP: '{player} was doomed to fall by {killer}'
  # The text or value for Falling Block. Available options: Any valid string text
  FALLING-BLOCK: '{player} got freaking squashed by a block'
  # Configuration section for Fire.
  FIRE:
    # The text or value for Normal. Available options: Any valid string text
    NORMAL: '{player} went up in flames'
    # The text or value for Pvp. Available options: Any valid string text
    PVP: '{player} walked into a fire whilst fighting {killer}'
  # Configuration section for Fire Tick.
  FIRE-TICK:
    # The text or value for Normal. Available options: Any valid string text
    NORMAL: '{player} burned to death'
    # The text or value for Pvp. Available options: Any valid string text
    PVP: '{player} was burnt to a crisp whilst fighting {killer}'
  # Configuration section for Lava.
  LAVA:
    # The text or value for Normal. Available options: Any valid string text
    NORMAL: '{player} tried to swim in lava'
    # The text or value for Pvp. Available options: Any valid string text
    PVP: '{player} tried to swim in lava while trying to escape {killer}'
  # The text or value for Lightning. Available options: Any valid string text
  LIGHTNING: '{player} got lit the hell up by a lightning'
  # The text or value for Poison. Available options: Any valid string text
  POISON: '{player} was poisoned'
  # Configuration section for Projectile.
  PROJECTILE:
    # The text or value for Normal. Available options: Any valid string text
    NORMAL: '{player} was shot'
    # The text or value for Pvp. Available options: Any valid string text
    PVP: '{player} was shot by {killer}'
  # The text or value for Starvation. Available options: Any valid string text
  STARVATION: '{player} starved to death'
  # The text or value for Suffocation. Available options: Any valid string text
  SUFFOCATION: '{player} suffocated in a wall'
  # The text or value for Suicide. Available options: Any valid string text
  SUICIDE: '{player} took his own life like a peasant'
  # The text or value for Thorns. Available options: Any valid string text
  THORNS: '{player} killed themself by trying to kill someone'
  # Configuration section for Void.
  VOID:
    # The text or value for Normal. Available options: Any valid string text
    NORMAL: '{player} fell out of the world'
    # The text or value for Pvp. Available options: Any valid string text
    PVP: '{player} was knocked into the void by {killer}'
  # The text or value for Wither. Available options: Any valid string text
  WITHER: '{player} withered away'
  # Configuration section for Entity Explosion.
  ENTITY-EXPLOSION:
    # The text or value for Normal. Available options: Any valid string text
    NORMAL: '{player} was blown up'
    # The text or value for Pvp. Available options: Any valid string text
    PVP: '{player} was blown up by {killer}'
  # The text or value for Default. Available options: Any valid string text
  DEFAULT: '{player} died'
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `MESSAGES.ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `MESSAGES` system. Set to `true` to enable, `false` to disable. |
| `MESSAGES.PREFIX` | `str` | Any string text | `'&c☠ '` | Configures the technical `PREFIX` parameter for `MESSAGES.PREFIX` in `death-messages.yml`. |
| `MESSAGES.BLOCK-EXPLOSION` | `str` | Any string text | `'{player} got blown to pieces'` | Configures the technical `BLOCK-EXPLOSION` parameter for `MESSAGES.BLOCK-EXPLOSION` in `death-messages.yml`. |
| `MESSAGES.CONTACT` | `str` | Any string text | `'{player} was pricked to death'` | Configures the technical `CONTACT` parameter for `MESSAGES.CONTACT` in `death-messages.yml`. |
| `MESSAGES.DROWNING.NORMAL` | `str` | Any string text | `'{player} drowned!'` | Configures the technical `NORMAL` parameter for `MESSAGES.DROWNING.NORMAL` in `death-messages.yml`. |
| `MESSAGES.DROWNING.PVP` | `str` | Any string text | `'{player} drowned whilst trying to e...'` | Configures the technical `PVP` parameter for `MESSAGES.DROWNING.PVP` in `death-messages.yml`. |
| `MESSAGES.ENTITY-ATTACK` | `str` | Any string text | `'{player} was slain by {killer}'` | Configures the technical `ENTITY-ATTACK` parameter for `MESSAGES.ENTITY-ATTACK` in `death-messages.yml`. |
| `MESSAGES.FALL.NORMAL` | `str` | Any string text | `'{player} hit the ground too hard'` | Configures the technical `NORMAL` parameter for `MESSAGES.FALL.NORMAL` in `death-messages.yml`. |
| `MESSAGES.FALL.PVP` | `str` | Any string text | `'{player} was doomed to fall by {kil...'` | Configures the technical `PVP` parameter for `MESSAGES.FALL.PVP` in `death-messages.yml`. |
| `MESSAGES.FALLING-BLOCK` | `str` | Any string text | `'{player} got freaking squashed by a...'` | Configures the technical `FALLING-BLOCK` parameter for `MESSAGES.FALLING-BLOCK` in `death-messages.yml`. |
| `MESSAGES.FIRE.NORMAL` | `str` | Any string text | `'{player} went up in flames'` | Configures the technical `NORMAL` parameter for `MESSAGES.FIRE.NORMAL` in `death-messages.yml`. |
| `MESSAGES.FIRE.PVP` | `str` | Any string text | `'{player} walked into a fire whilst ...'` | Configures the technical `PVP` parameter for `MESSAGES.FIRE.PVP` in `death-messages.yml`. |
| `MESSAGES.FIRE-TICK.NORMAL` | `str` | Any string text | `'{player} burned to death'` | Configures the technical `NORMAL` parameter for `MESSAGES.FIRE-TICK.NORMAL` in `death-messages.yml`. |
| `MESSAGES.FIRE-TICK.PVP` | `str` | Any string text | `'{player} was burnt to a crisp whils...'` | Configures the technical `PVP` parameter for `MESSAGES.FIRE-TICK.PVP` in `death-messages.yml`. |
| `MESSAGES.LAVA.NORMAL` | `str` | Any string text | `'{player} tried to swim in lava'` | Configures the technical `NORMAL` parameter for `MESSAGES.LAVA.NORMAL` in `death-messages.yml`. |
| `MESSAGES.LAVA.PVP` | `str` | Any string text | `'{player} tried to swim in lava whil...'` | Configures the technical `PVP` parameter for `MESSAGES.LAVA.PVP` in `death-messages.yml`. |
| `MESSAGES.LIGHTNING` | `str` | Any string text | `'{player} got lit the hell up by a l...'` | Configures the technical `LIGHTNING` parameter for `MESSAGES.LIGHTNING` in `death-messages.yml`. |
| `MESSAGES.POISON` | `str` | Any string text | `'{player} was poisoned'` | Configures the technical `POISON` parameter for `MESSAGES.POISON` in `death-messages.yml`. |
| `MESSAGES.PROJECTILE.NORMAL` | `str` | Any string text | `'{player} was shot'` | Configures the technical `NORMAL` parameter for `MESSAGES.PROJECTILE.NORMAL` in `death-messages.yml`. |
| `MESSAGES.PROJECTILE.PVP` | `str` | Any string text | `'{player} was shot by {killer}'` | Configures the technical `PVP` parameter for `MESSAGES.PROJECTILE.PVP` in `death-messages.yml`. |
| `MESSAGES.STARVATION` | `str` | Any string text | `'{player} starved to death'` | Configures the technical `STARVATION` parameter for `MESSAGES.STARVATION` in `death-messages.yml`. |
| `MESSAGES.SUFFOCATION` | `str` | Any string text | `'{player} suffocated in a wall'` | Configures the technical `SUFFOCATION` parameter for `MESSAGES.SUFFOCATION` in `death-messages.yml`. |
| `MESSAGES.SUICIDE` | `str` | Any string text | `'{player} took his own life like a p...'` | Configures the technical `SUICIDE` parameter for `MESSAGES.SUICIDE` in `death-messages.yml`. |
| `MESSAGES.THORNS` | `str` | Any string text | `'{player} killed themself by trying ...'` | Configures the technical `THORNS` parameter for `MESSAGES.THORNS` in `death-messages.yml`. |
| `MESSAGES.VOID.NORMAL` | `str` | Any string text | `'{player} fell out of the world'` | Configures the technical `NORMAL` parameter for `MESSAGES.VOID.NORMAL` in `death-messages.yml`. |
| `MESSAGES.VOID.PVP` | `str` | Any string text | `'{player} was knocked into the void ...'` | Configures the technical `PVP` parameter for `MESSAGES.VOID.PVP` in `death-messages.yml`. |
| `MESSAGES.WITHER` | `str` | Any string text | `'{player} withered away'` | Configures the technical `WITHER` parameter for `MESSAGES.WITHER` in `death-messages.yml`. |
| `MESSAGES.ENTITY-EXPLOSION.NORMAL` | `str` | Any string text | `'{player} was blown up'` | Configures the technical `NORMAL` parameter for `MESSAGES.ENTITY-EXPLOSION.NORMAL` in `death-messages.yml`. |
| `MESSAGES.ENTITY-EXPLOSION.PVP` | `str` | Any string text | `'{player} was blown up by {killer}'` | Configures the technical `PVP` parameter for `MESSAGES.ENTITY-EXPLOSION.PVP` in `death-messages.yml`. |
| `MESSAGES.DEFAULT` | `str` | Any string text | `'{player} died'` | Configures the technical `DEFAULT` parameter for `MESSAGES.DEFAULT` in `death-messages.yml`. |

### 3. Practical Setup Example

```yaml
MESSAGES:
  # Determines whether Enabled is enabled or disabled. Available options: true, false
  ENABLED: true
  # The text or value for Prefix. Available options: Any valid string text
  PREFIX: '&c☠ '
  # The text or value for Block Explosion. Available options: Any valid string text
  BLOCK-EXPLOSION: '{player} got blown to pieces'
  # The text or value for Contact. Available options: Any valid string text
  CONTACT: '{player} was pricked to death'
  # Configuration section for Drowning.
  DROWNING:
    # The text or value for Normal. Available options: Any valid string text
    NORMAL: '{player} drowned!'
    # The text or value for Pvp. Available options: Any valid string text
    PVP: '{player} drowned whilst trying to escape {killer}'
  # The text or value for Entity Attack. Available options: Any valid string text
  ENTITY-ATTACK: '{player} was slain by {killer}'
  # Configuration section for Fall.
  FALL:
    # The text or value for Normal. Available options: Any valid string
```

---

