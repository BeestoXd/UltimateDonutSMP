# Detailed Configuration & Setup Guide: `config.yml`

This is the official, 100% complete technical setup guide for `config.yml` in **UltimateDonutSMP**.
Each section details the exact commented setup code block, allowed option values, data types, default values, and in-depth functional behavior.

---

## Section: `LOCATIONS`

### 1. Commented Setup Code Example

```yaml
LOCATIONS:
  # The text or value for Spawn Location. Available options: Any valid string text
  SPAWN-LOCATION: ''
  # The text or value for Afk Location. Available options: Any valid string text
  AFK-LOCATION: ''
# Configuration section for Portal System.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `LOCATIONS.SPAWN-LOCATION` | `str` | Any string text | `''` | Configures the technical `SPAWN-LOCATION` parameter for `LOCATIONS.SPAWN-LOCATION` in `config.yml`. |
| `LOCATIONS.AFK-LOCATION` | `str` | Any string text | `''` | Configures the technical `AFK-LOCATION` parameter for `LOCATIONS.AFK-LOCATION` in `config.yml`. |

### 3. Practical Setup Example

```yaml
LOCATIONS:
  # The text or value for Spawn Location. Available options: Any valid string text
  SPAWN-LOCATION: ''
  # The text or value for Afk Location. Available options: Any valid string text
  AFK-LOCATION: ''
# Configuration section for Portal System.
```

---

## Section: `PORTAL-SYSTEM`

### 1. Commented Setup Code Example

```yaml
PORTAL-SYSTEM:
  # Determines whether Enabled is enabled or disabled. Available options: true, false
  ENABLED: true
  # Determines whether Block In Combat is enabled or disabled. Available options: true, false
  BLOCK-IN-COMBAT: true
  # The numerical value for Default Trigger Cooldown Ms. Available options: Any valid integer
  DEFAULT-TRIGGER-COOLDOWN-MS: 1500
  # The numerical value for Post Teleport Grace Ms. Available options: Any valid integer
  POST-TELEPORT-GRACE-MS: 2000
  # Configuration section for Hologram.
  HOLOGRAM:
    # Determines whether Enabled is enabled or disabled. Available options: true, false
    ENABLED: true
    # The text or value for Default Region. Available options: Any valid string text
    DEFAULT-REGION: NA East
    # The text or value for Default Server Id. Available options: Any valid string text
    DEFAULT-SERVER-ID: ''
    # The text or value for Portals. Available options: Any valid string text
    PORTALS: null
    # The decimal value for Offset Y. Available options: Any decimal number
    OFFSET-Y: 1.2
    # The decimal value for Set Here Offset Y. Available options: Any decimal number
    SET-HERE-OFFSET-Y: 1.6
    # The decimal value for Line Spacing. Available options: Any decimal number
    LINE-SPACING: 0.27
    # The numerical value for Update Ticks. Available options: Any valid integer
    UPDATE-TICKS: 40
    # Configuration section for Lines.
    LINES:
    - '&f{portal}'
    - '&7Region {region}'
    - ''
    - '&f<total_player> Players'
# Configuration section for Settings.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `PORTAL-SYSTEM.ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `PORTAL-SYSTEM` system. Set to `true` to enable, `false` to disable. |
| `PORTAL-SYSTEM.BLOCK-IN-COMBAT` | `bool` | `true`, `false` | `true` | Configures the technical `BLOCK-IN-COMBAT` parameter for `PORTAL-SYSTEM.BLOCK-IN-COMBAT` in `config.yml`. |
| `PORTAL-SYSTEM.DEFAULT-TRIGGER-COOLDOWN-MS` | `int` | Any valid integer number | `'1500'` | Configures the technical `DEFAULT-TRIGGER-COOLDOWN-MS` parameter for `PORTAL-SYSTEM.DEFAULT-TRIGGER-COOLDOWN-MS` in `config.yml`. |
| `PORTAL-SYSTEM.POST-TELEPORT-GRACE-MS` | `int` | Any valid integer number | `'2000'` | Configures the technical `POST-TELEPORT-GRACE-MS` parameter for `PORTAL-SYSTEM.POST-TELEPORT-GRACE-MS` in `config.yml`. |
| `PORTAL-SYSTEM.HOLOGRAM.ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `PORTAL-SYSTEM` system. Set to `true` to enable, `false` to disable. |
| `PORTAL-SYSTEM.HOLOGRAM.DEFAULT-REGION` | `str` | Any string text | `'NA East'` | Configures the technical `DEFAULT-REGION` parameter for `PORTAL-SYSTEM.HOLOGRAM.DEFAULT-REGION` in `config.yml`. |
| `PORTAL-SYSTEM.HOLOGRAM.DEFAULT-SERVER-ID` | `str` | Any string text | `''` | Configures the technical `DEFAULT-SERVER-ID` parameter for `PORTAL-SYSTEM.HOLOGRAM.DEFAULT-SERVER-ID` in `config.yml`. |
| `PORTAL-SYSTEM.HOLOGRAM.PORTALS` | `NoneType` | Any string text | `null` | Configures the technical `PORTALS` parameter for `PORTAL-SYSTEM.HOLOGRAM.PORTALS` in `config.yml`. |
| `PORTAL-SYSTEM.HOLOGRAM.OFFSET-Y` | `float` | Any decimal number | `'1.2'` | Configures the technical `OFFSET-Y` parameter for `PORTAL-SYSTEM.HOLOGRAM.OFFSET-Y` in `config.yml`. |
| `PORTAL-SYSTEM.HOLOGRAM.SET-HERE-OFFSET-Y` | `float` | Any decimal number | `'1.6'` | Configures the technical `SET-HERE-OFFSET-Y` parameter for `PORTAL-SYSTEM.HOLOGRAM.SET-HERE-OFFSET-Y` in `config.yml`. |
| `PORTAL-SYSTEM.HOLOGRAM.LINE-SPACING` | `float` | Any decimal number | `'0.27'` | Configures the technical `LINE-SPACING` parameter for `PORTAL-SYSTEM.HOLOGRAM.LINE-SPACING` in `config.yml`. |
| `PORTAL-SYSTEM.HOLOGRAM.UPDATE-TICKS` | `int` | Any valid integer number | `'40'` | Configures the technical `UPDATE-TICKS` parameter for `PORTAL-SYSTEM.HOLOGRAM.UPDATE-TICKS` in `config.yml`. |
| `PORTAL-SYSTEM.HOLOGRAM.LINES` | `list` | List of configured items/strings | `[&f{portal}, &7Region {region}, ...]` | Configures the technical `LINES` parameter for `PORTAL-SYSTEM.HOLOGRAM.LINES` in `config.yml`. |

### 3. Practical Setup Example

```yaml
PORTAL-SYSTEM:
  # Determines whether Enabled is enabled or disabled. Available options: true, false
  ENABLED: true
  # Determines whether Block In Combat is enabled or disabled. Available options: true, false
  BLOCK-IN-COMBAT: true
  # The numerical value for Default Trigger Cooldown Ms. Available options: Any valid integer
  DEFAULT-TRIGGER-COOLDOWN-MS: 1500
  # The numerical value for Post Teleport Grace Ms. Available options: Any valid integer
  POST-TELEPORT-GRACE-MS: 2000
  # Configuration section for Hologram.
  HOLOGRAM:
    # Determines whether Enabled is enabled or disabled. Available options: true, false
    ENABLED: true
    # The text or value for Default Region. Available options: Any valid string text
    DEFAULT-REGION: NA East
    # The text or value for Default Server Id. Available options: Any valid string text
    DEFAULT-SERVER-ID: ''
    # The text or value for Portals. Available options: Any valid string text
    PORTALS: null
    # The decimal value for Offset
```

---

## Section: `SETTINGS`

### 1. Commented Setup Code Example

```yaml
SETTINGS:
  # Determines whether Respawn On Bed is enabled or disabled. Available options: true, false
  RESPAWN-ON-BED: false
  # Determines whether Chainmail On Respawn is enabled or disabled. Available options: true, false
  CHAINMAIL-ON-RESPAWN: true
  # Configuration section for Chainmail Respawn Items.
  CHAINMAIL-RESPAWN-ITEMS:
  - MATERIAL: STONE_SWORD
    # The numerical value for Amount. Available options: Any valid integer
    AMOUNT: 1
  - MATERIAL: CHAINMAIL_HELMET
    NAME: '&eChainmail Helmet'
    # The numerical value for Amount. Available options: Any valid integer
    AMOUNT: 1
  - MATERIAL: CHAINMAIL_CHESTPLATE
    NAME: '&eChainmail Chestplate'
    # The numerical value for Amount. Available options: Any valid integer
    AMOUNT: 1
  - MATERIAL: CHAINMAIL_LEGGINGS
    NAME: '&eChainmail Leggings'
    # The numerical value for Amount. Available options: Any valid integer
    AMOUNT: 1
  - MATERIAL: CHAINMAIL_BOOTS
    NAME: '&eChainmail Boots'
    # The numerical value for Amount. Available options: Any valid integer
    AMOUNT: 1
  - MATERIAL: COOKED_BEEF
    # The numerical value for Amount. Available options: Any valid integer
    AMOUNT: 16
  # Homes every player gets when no HOME-PERMISSIONS entry applies to them
  # Raise the HOME-PERMISSIONS entries below to hand out extra homes as a rank perk
  # Available options: Any valid integer
  HOME-DEFAULT: 2
  # Per-rank home limits resolved from permissions
  HOME-PERMISSIONS:
    # Enable or disable permission based home limits
    ENABLED: true
    # Explicit mapping from permission node to home limit
    # Players can also be given ultimatedonutsmp.homes.<1-100> directly, for example
    # ultimatedonutsmp.homes.10 for ten homes, or ultimatedonutsmp.homes.page.<1-100> to
    # hand out whole pages of five at a time
    # The highest value the player has wins
    # Players without any of these permissions keep HOME-DEFAULT above
    PERMISSIONS:
      "ultimatedonutsmp.homes.vip++": 15
      "ultimatedonutsmp.homes.vip+": 10
      "ultimatedonutsmp.homes.vip": 5
  # The numerical value for Shards Per Kill. Available options: Any valid integer
  SHARDS-PER-KILL: 1
  # The text or value for Shards Kill Message. Available options: Any valid string text
  SHARDS-KILL-MESSAGE: '&#A303F9+{shards} Shard'
  # The text or value for Shards Kill Message Boosted, shown instead of Shards Kill
  # Message while a shard booster multiplies the kill reward. Supports {multiplier}.
  # Available options: Any valid string text
  SHARDS-KILL-MESSAGE-BOOSTED: '&#A303F9+{shards} Shards &7(&ax{multiplier}&7)'
  # The numerical value for Shards Kill Cooldown Seconds. Blocks repeated kill rewards
  # against the same victim until the cooldown expires. Set to 0 to disable.
  # Available options: Any valid integer
  SHARDS-KILL-COOLDOWN-SECONDS: 600
  # The text or value for Shards Kill Cooldown Message, shown when the kill reward is
  # skipped because the same victim was killed recently. Leave empty to stay silent.
  # Available options: Any valid string text
  SHARDS-KILL-COOLDOWN-MESSAGE: '&cNo Shard &7(killed recently, {time} left)'
  # The decimal value for Money Per Default. Available options: Any decimal number
  MONEY-PER-DEFAULT: 1000.0
  # The text or value for Sell Message. Available options: Any valid string text
  SELL-MESSAGE: '&a+$%price%'
  # Determines whether Spawn Menu is enabled or disabled. Available options: true, false
  SPAWN-MENU: true
  # Determines whether Afk Menu is enabled or disabled. Available options: true, false
  AFK-MENU: true
  # Determines whether players are teleported to the spawn location the first time they
  # join the server. Ignored while First Join Rtp Enabled is true. Available options:
  # true, false
  TELEPORT-SPAWN-ON-FIRST-JOIN: true
  # The numerical value for First Join Spawn Delay Ticks, how long to wait after a new
  # player joins before sending them to spawn. Raise it when another plugin moves players
  # around on join. Available options: Any valid integer
  FIRST-JOIN-SPAWN-DELAY-TICKS: 20
  # The decimal value for Worth Default Value. Available options: Any decimal number
  WORTH-DEFAULT-VALUE: 1.0
  # The numerical value for Mob Spawn Radius. Available options: Any valid integer
  MOB-SPAWN-RADIUS: 50
  # The numerical value for Phantom Spawn Radius. Available options: Any valid integer
  PHANTOM-SPAWN-RADIUS: 40
  # The numerical value for Disable Mob Spawn Limit Seconds. Set to -1 for no limit. Available options: Any valid integer
  DISABLE-MOB-SPAWN-LIMIT-SECONDS: -1
  # The numerical value for Disable Phantom Spawn Limit Seconds. Set to -1 for no limit. Available options: Any valid integer
  DISABLE-PHANTOM-SPAWN-LIMIT-SECONDS: 3600
  # Determines whether the per player mob spawn toggle also stops trial spawners in trial
  # chambers. Leave it false so the chamber still has to be fought. A trial spawner ejects
  # its rewards once the mobs it released are gone, so blocking those spawns hands out the
  # loot for free. Available options: true, false
  MOB-SPAWN-TOGGLE-BLOCKS-TRIAL-SPAWNERS: false
# Configuration section for Features.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `SETTINGS.RESPAWN-ON-BED` | `bool` | `true`, `false` | `false` | If `false`, forces all respawns to global Spawn. If `true`, permits Bed and Respawn Anchor respawns. |
| `SETTINGS.CHAINMAIL-ON-RESPAWN` | `bool` | `true`, `false` | `true` | Equips players with starter chainmail armor & stone sword upon death respawn. |
| `SETTINGS.CHAINMAIL-RESPAWN-ITEMS` | `list` | List of configured items/strings | `[{'MATERIAL': 'STONE_SWORD', 'AMOUNT': 1}, {'MATERIAL': 'CHAINMAIL_HELMET', 'NAME': '&eChainmail Helmet', 'AMOUNT': 1}, {'MATERIAL': 'CHAINMAIL_CHESTPLATE', 'NAME': '&eChainmail Chestplate', 'AMOUNT': 1}...]` | Configures the technical `CHAINMAIL-RESPAWN-ITEMS` parameter for `SETTINGS.CHAINMAIL-RESPAWN-ITEMS` in `config.yml`. |
| `SETTINGS.HOME-DEFAULT` | `int` | Any valid integer number | `'2'` | Default maximum `/sethome` limit for non-donor players. |
| `SETTINGS.HOME-PERMISSIONS.ENABLED` | `bool` | `true`, `false` | `true` | Master switch for permission based home limits. Set to `false` to ignore every home permission and give everyone `HOME-DEFAULT`. |
| `SETTINGS.HOME-PERMISSIONS.PERMISSIONS` | `section` | Permission node to home count | `{'ultimatedonutsmp.homes.vip++': 15, 'ultimatedonutsmp.homes.vip+': 10, 'ultimatedonutsmp.homes.vip': 5}` | Named rank nodes mapped to a home limit, for servers that prefer `ultimatedonutsmp.homes.vip` over the numbered nodes. The highest value a player holds wins. See [Commands-and-Permissions](Commands-and-Permissions). |
| `SETTINGS.SHARDS-PER-KILL` | `int` | Any valid integer number | `'1'` | Configures the technical `SHARDS-PER-KILL` parameter for `SETTINGS.SHARDS-PER-KILL` in `config.yml`. |
| `SETTINGS.SHARDS-KILL-MESSAGE` | `str` | Any string text | `'&#A303F9+{shards} Shard'` | Configures the technical `SHARDS-KILL-MESSAGE` parameter for `SETTINGS.SHARDS-KILL-MESSAGE` in `config.yml`. |
| `SETTINGS.SHARDS-KILL-MESSAGE-BOOSTED` | `str` | Any string text | `'&#A303F9+{shards} Shards &7(&ax{multiplier}&7)'` | Action bar shown instead of `SHARDS-KILL-MESSAGE` while a shard booster multiplies the kill reward. Supports `{multiplier}`. |
| `SETTINGS.SHARDS-KILL-COOLDOWN-SECONDS` | `int` | Any valid integer number | `'600'` | Time a killer must wait before the same victim rewards shards again. Set to `0` to reward every kill. |
| `SETTINGS.SHARDS-KILL-COOLDOWN-MESSAGE` | `str` | Any string text | `'&cNo Shard &7(killed recently, {time} left)'` | Action bar shown when a kill reward is skipped by the cooldown. Supports `{time}` and `{seconds}`. Leave empty to stay silent. |
| `SETTINGS.MONEY-PER-DEFAULT` | `float` | Any decimal number | `'1000.0'` | Configures the technical `MONEY-PER-DEFAULT` parameter for `SETTINGS.MONEY-PER-DEFAULT` in `config.yml`. |
| `SETTINGS.SELL-MESSAGE` | `str` | Any string text | `'&a+$%price%'` | Configures the technical `SELL-MESSAGE` parameter for `SETTINGS.SELL-MESSAGE` in `config.yml`. |
| `SETTINGS.SPAWN-MENU` | `bool` | `true`, `false` | `true` | Configures the technical `SPAWN-MENU` parameter for `SETTINGS.SPAWN-MENU` in `config.yml`. |
| `SETTINGS.AFK-MENU` | `bool` | `true`, `false` | `true` | Configures the technical `AFK-MENU` parameter for `SETTINGS.AFK-MENU` in `config.yml`. |
| `SETTINGS.TELEPORT-SPAWN-ON-FIRST-JOIN` | `bool` | `true`, `false` | `true` | Teleports a player to the spawn location the first time they join. Does nothing until `/setspawn` has been run. Ignored while `FIRST-JOIN-RTP.ENABLED` is `true`. |
| `SETTINGS.FIRST-JOIN-SPAWN-DELAY-TICKS` | `int` | Any valid integer number | `20` | How long the plugin waits after the join before running that teleport. Values below `1` are treated as `1` and anything above `1200` is capped there. |
| `SETTINGS.WORTH-DEFAULT-VALUE` | `float` | Any decimal number | `'1.0'` | Configures the technical `WORTH-DEFAULT-VALUE` parameter for `SETTINGS.WORTH-DEFAULT-VALUE` in `config.yml`. |
| `SETTINGS.MOB-SPAWN-RADIUS` | `int` | Any valid integer number | `'50'` | Configures the technical `MOB-SPAWN-RADIUS` parameter for `SETTINGS.MOB-SPAWN-RADIUS` in `config.yml`. |
| `SETTINGS.PHANTOM-SPAWN-RADIUS` | `int` | Any valid integer number | `'40'` | Configures the technical `PHANTOM-SPAWN-RADIUS` parameter for `SETTINGS.PHANTOM-SPAWN-RADIUS` in `config.yml`. |
| `SETTINGS.DISABLE-MOB-SPAWN-LIMIT-SECONDS` | `int` | Any valid integer number | `'-1'` | Configures the technical `DISABLE-MOB-SPAWN-LIMIT-SECONDS` parameter for `SETTINGS.DISABLE-MOB-SPAWN-LIMIT-SECONDS` in `config.yml`. |
| `SETTINGS.DISABLE-PHANTOM-SPAWN-LIMIT-SECONDS` | `int` | Any valid integer number | `'3600'` | Configures the technical `DISABLE-PHANTOM-SPAWN-LIMIT-SECONDS` parameter for `SETTINGS.DISABLE-PHANTOM-SPAWN-LIMIT-SECONDS` in `config.yml`. |
| `SETTINGS.MOB-SPAWN-TOGGLE-BLOCKS-TRIAL-SPAWNERS` | `bool` | `true`, `false` | `false` | Whether the `/settings` mob spawn toggle reaches trial spawners in trial chambers. A trial spawner ejects its rewards as soon as the mobs it released are gone, so cancelling those spawns pays out the chamber without a fight. Set it to `true` only if you would rather have the quiet room than the loot. |

### 3. Practical Setup Example

```yaml
SETTINGS:
  # Determines whether Respawn On Bed is enabled or disabled. Available options: true, false
  RESPAWN-ON-BED: false
  # Determines whether Chainmail On Respawn is enabled or disabled. Available options: true, false
  CHAINMAIL-ON-RESPAWN: true
  # Configuration section for Chainmail Respawn Items.
  CHAINMAIL-RESPAWN-ITEMS:
  - MATERIAL: STONE_SWORD
    # The numerical value for Amount. Available options: Any valid integer
    AMOUNT: 1
  - MATERIAL: CHAINMAIL_HELMET
    NAME: '&eChainmail Helmet'
    # The numerical value for Amount. Available options: Any valid integer
    AMOUNT: 1
  - MATERIAL: CHAINMAIL_CHESTPLATE
    NAME: '&eChainmail Chestplate'
    # The numerical value for Amount. Available options: Any valid integer
    AMOUNT: 1
  - MATERIAL: CHAINMAIL_LEGGINGS
    NAME: '&eChainmail Leggings'
    # The numerical value for Amount. Available options: Any valid integer
    AMOUNT: 1
  - MATERIAL: CHAINMAIL_BOOTS
    NAME: '&eChainmail Boots'
    # The numerical val
```

---

## Section: `FIRST-JOIN-RTP`

Drops brand new players at a random location the first time they join, instead of leaving them on the vanilla world spawn near `0, 0`.
The location search reuses the RTP engine but bypasses RTP cooldowns, playtime requirements, and the RTP queue, so a first join is never blocked.
It does require the `RTP` feature itself to be enabled - with RTP off, the join falls back to `SETTINGS.TELEPORT-SPAWN-ON-FIRST-JOIN`.

### 1. Commented Setup Code Example

```yaml
FIRST-JOIN-RTP:
  # Determines whether First Join Rtp is enabled or disabled. Takes priority over Settings
  # Teleport Spawn On First Join. Available options: true, false
  ENABLED: false
  # Determines whether the player is sent to the spawn location when no safe random
  # location can be found. Available options: true, false
  FALLBACK-TO-SPAWN: true
  # The text or value for Searching Message, sent while the safe location is being looked
  # up. Set to '' to disable. Available options: Any valid string text
  SEARCHING-MESSAGE: '&7Finding you a safe place to start...'
  # The text or value for Success Message, sent once the player has been dropped.
  # Supports {world}, {x}, {y}, {z}. Set to '' to disable. Available options: Any valid
  # string text
  SUCCESS-MESSAGE: '&aWelcome! You spawned at &fX:{x} Y:{y} Z:{z}&a.'
  # The text or value for Failed Message, sent when no safe location could be found. Set
  # to '' to disable. Available options: Any valid string text
  FAILED-MESSAGE: '&cCould not find a random spawn location for you.'
  # Configuration section for World.
  WORLD:
    # The world to drop new players in. Leave empty to use the world they join in.
    # Available options: Any valid string text
    NAME: ''
    # The numerical value for Center X. Available options: Any valid integer
    CENTER-X: 0
    # The numerical value for Center Z. Available options: Any valid integer
    CENTER-Z: 0
    # The numerical value for Min Radius. Available options: Any valid integer
    MIN-RADIUS: 500
    # The numerical value for Max Radius. Available options: Any valid integer
    MAX-RADIUS: 5000
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `FIRST-JOIN-RTP.ENABLED` | `bool` | `true`, `false` | `false` | Master toggle for the random first-join drop. While `true` it takes priority over `SETTINGS.TELEPORT-SPAWN-ON-FIRST-JOIN`. |
| `FIRST-JOIN-RTP.FALLBACK-TO-SPAWN` | `bool` | `true`, `false` | `true` | Sends the player to the spawn location when the search finds nothing safe. Set to `false` to leave them where they landed. |
| `FIRST-JOIN-RTP.SEARCHING-MESSAGE` | `str` | Any string text | `'&7Finding you a safe place to start...'` | Sent as soon as the search starts, so the player knows why they are waiting. Set to `''` to stay silent. |
| `FIRST-JOIN-RTP.SUCCESS-MESSAGE` | `str` | Any string text | `'&aWelcome! You spawned at &fX:{x} Y:{y} Z:{z}&a.'` | Sent after the teleport succeeds. Supports `{world}`, `{x}`, `{y}`, `{z}`. Set to `''` to stay silent. |
| `FIRST-JOIN-RTP.FAILED-MESSAGE` | `str` | Any string text | `'&cCould not find a random spawn location for you.'` | Sent when no safe location was found or the teleport failed. Set to `''` to stay silent. |
| `FIRST-JOIN-RTP.WORLD.NAME` | `str` | Any string text | `''` | World to search in. Leave empty to use whichever world the player joined in. `world`, `nether`, and `end` resolve to the loaded overworld/nether/end. |
| `FIRST-JOIN-RTP.WORLD.CENTER-X` | `int` | Any valid integer number | `0` | X coordinate the search radius is measured from. |
| `FIRST-JOIN-RTP.WORLD.CENTER-Z` | `int` | Any valid integer number | `0` | Z coordinate the search radius is measured from. |
| `FIRST-JOIN-RTP.WORLD.MIN-RADIUS` | `int` | Any valid integer number | `500` | Closest a new player can be dropped to the center. Raise it to keep new players away from spawn. |
| `FIRST-JOIN-RTP.WORLD.MAX-RADIUS` | `int` | Any valid integer number | `5000` | Furthest a new player can be dropped from the center. Keep it inside your pregenerated area, otherwise the search has to fall back to chunk generation. |

### 3. Practical Setup Example

Spread new players between 1000 and 8000 blocks from spawn in the overworld, and say nothing while searching:

```yaml
FIRST-JOIN-RTP:
  ENABLED: true
  FALLBACK-TO-SPAWN: true
  SEARCHING-MESSAGE: ''
  SUCCESS-MESSAGE: '&aWelcome to the server! You start at &fX:{x} Z:{z}&a.'
  FAILED-MESSAGE: '&cCould not find a random spawn location for you.'
  WORLD:
    NAME: 'world'
    CENTER-X: 0
    CENTER-Z: 0
    MIN-RADIUS: 1000
    MAX-RADIUS: 8000
```

The search attempt budget, chunk sample budget, and chunk generation behaviour are shared with `/rtp` and stay in [`rtp.yml`](Config-rtp.yml).

---

## Section: `RESPAWN-RTP`

Sends players back out into the world at a random location after they die, instead of leaving them standing on spawn with everyone else.
The location search reuses the RTP engine but bypasses RTP cooldowns, playtime requirements, and the RTP queue, so dying never leaves a player stuck behind a cooldown.
It does require the `RTP` feature itself to be enabled - with RTP off, respawns behave exactly as they did before.

The teleport only starts once the player has already landed on their normal respawn location, so a search that finds nothing simply leaves them at spawn.
Players who keep their bed or respawn anchor under `SETTINGS.RESPAWN-ON-BED` are left where they are, and so are players respawning into a duel, an FFA arena, or an ender pearl death drop.

### 1. Commented Setup Code Example

```yaml
RESPAWN-RTP:
  # Determines whether Respawn Rtp is enabled or disabled. Available options: true, false
  ENABLED: false
  # The text or value for Searching Message, sent while the safe location is being looked
  # up. Set to '' to disable. Available options: Any valid string text
  SEARCHING-MESSAGE: '&7Finding you a safe place to respawn...'
  # The text or value for Success Message, sent once the player has been dropped. Supports
  # {world}, {x}, {y}, {z}. Set to '' to disable. Available options: Any valid string text
  SUCCESS-MESSAGE: '&aYou respawned at &fX:{x} Y:{y} Z:{z}&a.'
  # The text or value for Failed Message, sent when no safe location could be found. The
  # player is left on the normal respawn location. Set to '' to disable. Available options:
  # Any valid string text
  FAILED-MESSAGE: '&cCould not find a random respawn location for you.'
  # Configuration section for World.
  WORLD:
    # The world to drop dead players in. Leave empty to use the world they died in.
    # Available options: Any valid string text
    NAME: ''
    # Determines whether the boundaries from World Settings in rtp.yml are reused for that
    # world. The Center X, Center Z, Min Radius, and Max Radius below are only read when
    # this is false, or when the world has no entry in rtp.yml. Available options: true,
    # false
    USE-RTP-BOUNDS: true
    # The numerical value for Center X. Available options: Any valid integer
    CENTER-X: 0
    # The numerical value for Center Z. Available options: Any valid integer
    CENTER-Z: 0
    # The numerical value for Min Radius. Available options: Any valid integer
    MIN-RADIUS: 500
    # The numerical value for Max Radius. Available options: Any valid integer
    MAX-RADIUS: 5000
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `RESPAWN-RTP.ENABLED` | `bool` | `true`, `false` | `false` | Master toggle for the random teleport after death. Needs `RTP.ENABLED` in `rtp.yml` to be `true` as well. |
| `RESPAWN-RTP.SEARCHING-MESSAGE` | `str` | Any string text | `'&7Finding you a safe place to respawn...'` | Sent as soon as the search starts, so the player knows why they are standing on spawn for a moment. Set to `''` to stay silent. |
| `RESPAWN-RTP.SUCCESS-MESSAGE` | `str` | Any string text | `'&aYou respawned at &fX:{x} Y:{y} Z:{z}&a.'` | Sent after the teleport succeeds. Supports `{world}`, `{x}`, `{y}`, `{z}`. Set to `''` to stay silent. |
| `RESPAWN-RTP.FAILED-MESSAGE` | `str` | Any string text | `'&cCould not find a random respawn location for you.'` | Sent when no safe location was found or the teleport failed. The player keeps the normal respawn location. Set to `''` to stay silent. |
| `RESPAWN-RTP.WORLD.NAME` | `str` | Any string text | `''` | World to search in. Leave empty to use whichever world the player died in. `world`, `nether`, and `end` resolve to the loaded overworld/nether/end. A world listed in `DENIED-WORLDS` is skipped entirely. |
| `RESPAWN-RTP.WORLD.USE-RTP-BOUNDS` | `bool` | `true`, `false` | `true` | Reuses `WORLD-SETTINGS.<world>` from `rtp.yml`, so `/rtp` and a death drop land in the same area. A world with no entry of its own inherits the entry matching its environment. Set to `false` to use the four values below instead. |
| `RESPAWN-RTP.WORLD.CENTER-X` | `int` | Any valid integer number | `0` | X coordinate the search radius is measured from. Only read when `USE-RTP-BOUNDS` is `false` or the world has no `rtp.yml` entry. |
| `RESPAWN-RTP.WORLD.CENTER-Z` | `int` | Any valid integer number | `0` | Z coordinate the search radius is measured from. Same condition as `CENTER-X`. |
| `RESPAWN-RTP.WORLD.MIN-RADIUS` | `int` | Any valid integer number | `500` | Closest a respawning player can land to the center. Raise it to push people away from spawn after they die. |
| `RESPAWN-RTP.WORLD.MAX-RADIUS` | `int` | Any valid integer number | `5000` | Furthest a respawning player can land from the center. Keep it inside your pregenerated area, otherwise the search has to fall back to chunk generation. |

### 3. Practical Setup Example

Scatter everyone who dies somewhere in the overworld between 1000 and 8000 blocks from spawn, ignoring the `/rtp` boundaries:

```yaml
RESPAWN-RTP:
  ENABLED: true
  SEARCHING-MESSAGE: '&7Sending you back out...'
  SUCCESS-MESSAGE: '&aYou woke up at &fX:{x} Z:{z}&a.'
  FAILED-MESSAGE: ''
  WORLD:
    NAME: 'world'
    USE-RTP-BOUNDS: false
    CENTER-X: 0
    CENTER-Z: 0
    MIN-RADIUS: 1000
    MAX-RADIUS: 8000
```

Leave `USE-RTP-BOUNDS` at `true` and `NAME` empty instead if you want a death in the nether to drop the player back in the nether, inside the same radius `/rtp` already uses there.

The search attempt budget, chunk sample budget, and chunk generation behaviour are shared with `/rtp` and stay in [`rtp.yml`](Config-rtp.yml).

---

## Section: `FEATURES_SETTINGS`

### 1. Commented Setup Code Example

```yaml
FEATURES_SETTINGS:
  # Action when executing a command linked to a disabled feature.
  # Options:
  # - "MESSAGE": Shows "The <feature> feature is currently disabled."
  # - "UNKNOWN": Shows default unknown command message.
  # - "UNREGISTER": Dynamically unregister command from Bukkit command map.
  DISABLED_COMMAND_ACTION: "MESSAGE"
# Configuration section for Chat.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `FEATURES_SETTINGS.DISABLED_COMMAND_ACTION` | `str` | `"MESSAGE"`, `"UNKNOWN"`, `"UNREGISTER"` | `'MESSAGE'` | Action when executing a disabled feature's command:<br>- `"MESSAGE"`: Shows disabled notice.<br>- `"UNKNOWN"`: Shows unknown command message.<br>- `"UNREGISTER"`: Dynamically unregisters command from Bukkit. |

### 3. Practical Setup Example

```yaml
FEATURES_SETTINGS:
  # Action when executing a command linked to a disabled feature.
  # Options:
  # - "MESSAGE": Shows "The <feature> feature is currently disabled."
  # - "UNKNOWN": Shows default unknown command message.
  # - "UNREGISTER": Dynamically unregister command from Bukkit command map.
  DISABLED_COMMAND_ACTION: "MESSAGE"
# Configuration section for Chat.
```

---

## Section: `CHAT`

### 1. Commented Setup Code Example

```yaml
CHAT:
  # Determines whether Format Enabled is enabled or disabled. Available options: true, false
  FORMAT-ENABLED: true
  # The text or value for Format. Available options: Any valid string text
  FORMAT: '&f%prefix%%player%&7: &f%message%'
  # Configuration section for Message Colors.
  MESSAGE-COLORS:
    # The text or value for Default. Available options: Any valid string text
    default: '&f'
    # The text or value for Owner. Available options: Any valid string text
    owner: '&#0000FF'
  # Configuration section for Clickable Name.
  CLICKABLE-NAME:
    # Determines whether Enabled is enabled or disabled. Available options: true, false
    ENABLED: true
    # Configuration section for Hover Text.
    HOVER-TEXT:
    - '%luckperms_prefix%%player%'
    - '&7&m----------'
    - '&#00FC00&l$ &fmoney &#00FC00%economy_money%'
    - '&#FC0000⚔ &fkills &#FC0000%economy_kills%'
    - '&#FCE300⌚ &fplaytime &#FCE300%economy_playtime%'
    - '&#F97603☠ &fdeaths &#F97603%economy_deaths%'
    - '&#A303F9★ &fshards &#A303F9%economy_shards%'
    - '&7&m----------'
    - '&7click to view stats'
    # The text or value for Suggest Command. Available options: Any valid string text
    SUGGEST-COMMAND: '/msg <player> '
  # Determines whether Global Chat Muted is enabled or disabled. Available options: true, false
  GLOBAL-CHAT-MUTED: false
  # Determines whether Global Chat Delay Enabled is enabled or disabled. Available options: true, false
  GLOBAL-CHAT-DELAY-ENABLED: false
  # The numerical value for Global Chat Delay. Available options: Any valid integer
  GLOBAL-CHAT-DELAY: 3
  # The numerical value for Max Delay Seconds. Available options: Any valid integer
  MAX-DELAY-SECONDS: 30
  # The numerical value for Clear Lines. Available options: Any valid integer
  CLEAR-LINES: 150
  # Configuration section for Filter.
  FILTER:
    # Determines whether Enabled is enabled or disabled. Available options: true, false
    ENABLED: true
    # The text or value for Block Message. Available options: Any valid string text
    BLOCK-MESSAGE: '&7Please avoid using inappropriate words.'
    # Configuration section for Words.
    WORDS:
    - fuck
    - shit
    - bitch
    # Configuration section for Language.
    LANGUAGE:
      # Determines whether Enabled is enabled or disabled. Available options: true, false
      ENABLED: false
      # Configuration section for Allowed Alphabets.
      ALLOWED-ALPHABETS:
      - LATIN
      - NUMBERS
      - SYMBOLS
      # The text or value for Block Message. Available options: Any valid string text
      BLOCK-MESSAGE: '&cYour message contains characters that are not allowed on this
        server.'
    # Configuration section for Caps.
    CAPS:
      # Determines whether Enabled is enabled or disabled. Available options: true, false
      ENABLED: false
      # The numerical value for Percentage. Available options: Any valid integer
      PERCENTAGE: 70
      # The numerical value for Min Length. Available options: Any valid integer
      MIN-LENGTH: 5
      # The text or value for Block Message. Available options: Any valid string text
      BLOCK-MESSAGE: '&cPlease avoid using too many capital letters.'
    # Configuration section for Anti Repeat.
    ANTI-REPEAT:
      # Determines whether Enabled is enabled or disabled. Available options: true, false
      ENABLED: false
      # The text or value for Block Message. Available options: Any valid string text
      BLOCK-MESSAGE: '&cYou cannot repeat the same message!'
    # Configuration section for Anti Link.
    ANTI-LINK:
      # Determines whether Enabled is enabled or disabled. Available options: true, false
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `CHAT.FORMAT-ENABLED` | `bool` | `true`, `false` | `true` | Configures the technical `FORMAT-ENABLED` parameter for `CHAT.FORMAT-ENABLED` in `config.yml`. |
| `CHAT.FORMAT` | `str` | Any string text | `'&f%prefix%%player%&7: &f%message%'` | Configures the technical `FORMAT` parameter for `CHAT.FORMAT` in `config.yml`. |
| `CHAT.MESSAGE-COLORS.default` | `str` | Any string text | `'&f'` | Configures the technical `default` parameter for `CHAT.MESSAGE-COLORS.default` in `config.yml`. |
| `CHAT.MESSAGE-COLORS.owner` | `str` | Any string text | `'&#0000FF'` | Configures the technical `owner` parameter for `CHAT.MESSAGE-COLORS.owner` in `config.yml`. |
| `CHAT.CLICKABLE-NAME.ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `CHAT` system. Set to `true` to enable, `false` to disable. |
| `CHAT.CLICKABLE-NAME.HOVER-TEXT` | `list` | List of configured items/strings | `[%luckperms_prefix%%player%, &7&m----------, &#00FC00&l$ &fmoney &#00FC00%economy_money%...]` | Configures the technical `HOVER-TEXT` parameter for `CHAT.CLICKABLE-NAME.HOVER-TEXT` in `config.yml`. |
| `CHAT.CLICKABLE-NAME.SUGGEST-COMMAND` | `str` | Any string text | `'/msg <player> '` | Configures the technical `SUGGEST-COMMAND` parameter for `CHAT.CLICKABLE-NAME.SUGGEST-COMMAND` in `config.yml`. |
| `CHAT.GLOBAL-CHAT-MUTED` | `bool` | `true`, `false` | `false` | Configures the technical `GLOBAL-CHAT-MUTED` parameter for `CHAT.GLOBAL-CHAT-MUTED` in `config.yml`. |
| `CHAT.GLOBAL-CHAT-DELAY-ENABLED` | `bool` | `true`, `false` | `false` | Configures the technical `GLOBAL-CHAT-DELAY-ENABLED` parameter for `CHAT.GLOBAL-CHAT-DELAY-ENABLED` in `config.yml`. |
| `CHAT.GLOBAL-CHAT-DELAY` | `int` | Any valid integer number | `'3'` | Configures the technical `GLOBAL-CHAT-DELAY` parameter for `CHAT.GLOBAL-CHAT-DELAY` in `config.yml`. |
| `CHAT.MAX-DELAY-SECONDS` | `int` | Any valid integer number | `'30'` | Configures the technical `MAX-DELAY-SECONDS` parameter for `CHAT.MAX-DELAY-SECONDS` in `config.yml`. |
| `CHAT.CLEAR-LINES` | `int` | Any valid integer number | `'150'` | Configures the technical `CLEAR-LINES` parameter for `CHAT.CLEAR-LINES` in `config.yml`. |
| `CHAT.FILTER.ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `CHAT` system. Set to `true` to enable, `false` to disable. |
| `CHAT.FILTER.BLOCK-MESSAGE` | `str` | Any string text | `'&7Please avoid using inappropriate ...'` | Configures the technical `BLOCK-MESSAGE` parameter for `CHAT.FILTER.BLOCK-MESSAGE` in `config.yml`. |
| `CHAT.FILTER.WORDS` | `list` | List of configured items/strings | `['fuck', 'shit', 'bitch']` | Configures the technical `WORDS` parameter for `CHAT.FILTER.WORDS` in `config.yml`. |
| `CHAT.FILTER.LANGUAGE.ENABLED` | `bool` | `true`, `false` | `false` | Global toggle for `CHAT` system. Set to `true` to enable, `false` to disable. |
| `CHAT.FILTER.LANGUAGE.ALLOWED-ALPHABETS` | `list` | List of configured items/strings | `['LATIN', 'NUMBERS', 'SYMBOLS']` | Configures the technical `ALLOWED-ALPHABETS` parameter for `CHAT.FILTER.LANGUAGE.ALLOWED-ALPHABETS` in `config.yml`. |
| `CHAT.FILTER.LANGUAGE.BLOCK-MESSAGE` | `str` | Any string text | `'&cYour message contains characters ...'` | Configures the technical `BLOCK-MESSAGE` parameter for `CHAT.FILTER.LANGUAGE.BLOCK-MESSAGE` in `config.yml`. |
| `CHAT.FILTER.CAPS.ENABLED` | `bool` | `true`, `false` | `false` | Global toggle for `CHAT` system. Set to `true` to enable, `false` to disable. |
| `CHAT.FILTER.CAPS.PERCENTAGE` | `int` | Any valid integer number | `'70'` | Configures the technical `PERCENTAGE` parameter for `CHAT.FILTER.CAPS.PERCENTAGE` in `config.yml`. |
| `CHAT.FILTER.CAPS.MIN-LENGTH` | `int` | Any valid integer number | `'5'` | Configures the technical `MIN-LENGTH` parameter for `CHAT.FILTER.CAPS.MIN-LENGTH` in `config.yml`. |
| `CHAT.FILTER.CAPS.BLOCK-MESSAGE` | `str` | Any string text | `'&cPlease avoid using too many capit...'` | Configures the technical `BLOCK-MESSAGE` parameter for `CHAT.FILTER.CAPS.BLOCK-MESSAGE` in `config.yml`. |
| `CHAT.FILTER.ANTI-REPEAT.ENABLED` | `bool` | `true`, `false` | `false` | Global toggle for `CHAT` system. Set to `true` to enable, `false` to disable. |
| `CHAT.FILTER.ANTI-REPEAT.BLOCK-MESSAGE` | `str` | Any string text | `'&cYou cannot repeat the same messag...'` | Configures the technical `BLOCK-MESSAGE` parameter for `CHAT.FILTER.ANTI-REPEAT.BLOCK-MESSAGE` in `config.yml`. |
| `CHAT.FILTER.ANTI-LINK.ENABLED` | `bool` | `true`, `false` | `false` | Global toggle for `CHAT` system. Set to `true` to enable, `false` to disable. |
| `CHAT.FILTER.ANTI-LINK.ALLOWED` | `list` | List of configured items/strings | `['google.com', 'youtube.com']` | Configures the technical `ALLOWED` parameter for `CHAT.FILTER.ANTI-LINK.ALLOWED` in `config.yml`. |
| `CHAT.FILTER.ANTI-LINK.BLOCK-MESSAGE` | `str` | Any string text | `'&cLinks are not allowed in the chat...'` | Configures the technical `BLOCK-MESSAGE` parameter for `CHAT.FILTER.ANTI-LINK.BLOCK-MESSAGE` in `config.yml`. |
| `CHAT.FILTER.LENGTH.MIN.ENABLED` | `bool` | `true`, `false` | `false` | Global toggle for `CHAT` system. Set to `true` to enable, `false` to disable. |
| `CHAT.FILTER.LENGTH.MIN.VALUE` | `int` | Any valid integer number | `'1'` | Configures the technical `VALUE` parameter for `CHAT.FILTER.LENGTH.MIN.VALUE` in `config.yml`. |
| `CHAT.FILTER.LENGTH.MIN.BLOCK-MESSAGE` | `str` | Any string text | `'&cYour message is too short! (Min: ...'` | Configures the technical `BLOCK-MESSAGE` parameter for `CHAT.FILTER.LENGTH.MIN.BLOCK-MESSAGE` in `config.yml`. |
| *(3 additional sub-keys configured in section)* | | | | |

### 3. Practical Setup Example

```yaml
CHAT:
  # Determines whether Format Enabled is enabled or disabled. Available options: true, false
  FORMAT-ENABLED: true
  # The text or value for Format. Available options: Any valid string text
  FORMAT: '&f%prefix%%player%&7: &f%message%'
  # Configuration section for Message Colors.
  MESSAGE-COLORS:
    # The text or value for Default. Available options: Any valid string text
    default: '&f'
    # The text or value for Owner. Available options: Any valid string text
    owner: '&#0000FF'
  # Configuration section for Clickable Name.
  CLICKABLE-NAME:
    # Determines whether Enabled is enabled or disabled. Available options: true, false
    ENABLED: true
    # Configuration section for Hover Text.
    HOVER-TEXT:
    - '%luckperms_prefix%%player%'
    - '&7&m----------'
    - '&#00FC00&l$ &fmoney &#00FC00%economy_money%'
    - '&#FC0000⚔ &fkills &#FC0000%economy_kills%'
    - '&#FCE300⌚ &fplaytime &#FCE300%economy_playtime%'
    - '&#F97603☠ &fdeaths &#F97603%economy_deaths%'
 
```

---

## Section: `AFK-SYSTEM`

### 1. Commented Setup Code Example

```yaml
AFK-SYSTEM:
  # Determines whether Enabled is enabled or disabled. Available options: true, false
  ENABLED: true
  # The numerical value for Time. Available options: Any valid integer
  TIME: 180
  # The text or value for Spawn Cuboid Name. Available options: Any valid string text
  SPAWN-CUBOID-NAME: spawn
  # The text or value for Afk Cuboid Name. Available options: Any valid string text
  AFK-CUBOID-NAME: ''
  # The text or value for Message. Available options: Any valid string text
  MESSAGE: '&7You have been moved to the AFK area for being inactive in the spawn.'
# Configuration section for Item Drop Prevention.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `AFK-SYSTEM.ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `AFK-SYSTEM` system. Set to `true` to enable, `false` to disable. |
| `AFK-SYSTEM.TIME` | `int` | Any valid integer number | `'180'` | Configures the technical `TIME` parameter for `AFK-SYSTEM.TIME` in `config.yml`. |
| `AFK-SYSTEM.SPAWN-CUBOID-NAME` | `str` | Any string text | `'spawn'` | Configures the technical `SPAWN-CUBOID-NAME` parameter for `AFK-SYSTEM.SPAWN-CUBOID-NAME` in `config.yml`. |
| `AFK-SYSTEM.AFK-CUBOID-NAME` | `str` | Any string text | `''` | Configures the technical `AFK-CUBOID-NAME` parameter for `AFK-SYSTEM.AFK-CUBOID-NAME` in `config.yml`. |
| `AFK-SYSTEM.MESSAGE` | `str` | Any string text | `'&7You have been moved to the AFK ar...'` | Configures the technical `MESSAGE` parameter for `AFK-SYSTEM.MESSAGE` in `config.yml`. |

### 3. Practical Setup Example

```yaml
AFK-SYSTEM:
  # Determines whether Enabled is enabled or disabled. Available options: true, false
  ENABLED: true
  # The numerical value for Time. Available options: Any valid integer
  TIME: 180
  # The text or value for Spawn Cuboid Name. Available options: Any valid string text
  SPAWN-CUBOID-NAME: spawn
  # The text or value for Afk Cuboid Name. Available options: Any valid string text
  AFK-CUBOID-NAME: ''
  # The text or value for Message. Available options: Any valid string text
  MESSAGE: '&7You have been moved to the AFK area for being inactive in the spawn.'
# Configuration section for Item Drop Prevention.
```

---

## Section: `PREVENT-ITEM-DROP`

### 1. Commented Setup Code Example

```yaml
PREVENT-ITEM-DROP:
  # Prevent players from dropping items while in the spawn region.
  SPAWN: true
  # Prevent players from dropping items while AFK (either in the AFK area or has AFK status).
  AFK: true
  # Bypass permission for admins/staff to allow item dropping.
  BYPASS-PERMISSION: 'ultimatedonutsmp.preventdrop.bypass'
  # Message sent to player when their drop is cancelled. Set to '' to disable message.
  MESSAGE: '&c✗ You are not allowed to drop items in spawn or AFK areas!'
# Configuration section for Cuboid Binds.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `PREVENT-ITEM-DROP.SPAWN` | `bool` | `true`, `false` | `true` | Configures the technical `SPAWN` parameter for `PREVENT-ITEM-DROP.SPAWN` in `config.yml`. |
| `PREVENT-ITEM-DROP.AFK` | `bool` | `true`, `false` | `true` | Configures the technical `AFK` parameter for `PREVENT-ITEM-DROP.AFK` in `config.yml`. |
| `PREVENT-ITEM-DROP.BYPASS-PERMISSION` | `str` | Any string text | `'ultimatedonutsmp.preventdrop.bypass'` | Configures the technical `BYPASS-PERMISSION` parameter for `PREVENT-ITEM-DROP.BYPASS-PERMISSION` in `config.yml`. |
| `PREVENT-ITEM-DROP.MESSAGE` | `str` | Any string text | `'&c✗ You are not allowed to drop ite...'` | Configures the technical `MESSAGE` parameter for `PREVENT-ITEM-DROP.MESSAGE` in `config.yml`. |

### 3. Practical Setup Example

```yaml
PREVENT-ITEM-DROP:
  # Prevent players from dropping items while in the spawn region.
  SPAWN: true
  # Prevent players from dropping items while AFK (either in the AFK area or has AFK status).
  AFK: true
  # Bypass permission for admins/staff to allow item dropping.
  BYPASS-PERMISSION: 'ultimatedonutsmp.preventdrop.bypass'
  # Message sent to player when their drop is cancelled. Set to '' to disable message.
  MESSAGE: '&c✗ You are not allowed to drop items in spawn or AFK areas!'
# Configuration section for Cuboid Binds.
```

---

## Section: `CUBOID-BINDS`

### 1. Commented Setup Code Example

```yaml
CUBOID-BINDS:
  # A list configuration for Spawn. Available options: Multiple items
  SPAWN: []
  # A list configuration for Afk. Available options: Multiple items
  AFK: []
# Configuration section for Fly System.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `CUBOID-BINDS.SPAWN` | `list` | List of configured items/strings | `[]` | Configures the technical `SPAWN` parameter for `CUBOID-BINDS.SPAWN` in `config.yml`. |
| `CUBOID-BINDS.AFK` | `list` | List of configured items/strings | `[]` | Configures the technical `AFK` parameter for `CUBOID-BINDS.AFK` in `config.yml`. |

### 3. Practical Setup Example

```yaml
CUBOID-BINDS:
  # A list configuration for Spawn. Available options: Multiple items
  SPAWN: []
  # A list configuration for Afk. Available options: Multiple items
  AFK: []
# Configuration section for Fly System.
```

---

## Section: `FLY-SYSTEM`

### 1. Commented Setup Code Example

```yaml
FLY-SYSTEM:
  # The permission required for regular players/ranks to fly in spawn or cuboids.
  PLAYER-FLY-PERMISSION: 'ultimatedonutsmp.player.fly'
  # Disable flight automatically when the player leaves the allowed areas or enters combat.
  AUTO-DISABLE-OUTSIDE: true
# Configuration section for Worth Lore.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `FLY-SYSTEM.PLAYER-FLY-PERMISSION` | `str` | Any string text | `'ultimatedonutsmp.player.fly'` | Configures the technical `PLAYER-FLY-PERMISSION` parameter for `FLY-SYSTEM.PLAYER-FLY-PERMISSION` in `config.yml`. |
| `FLY-SYSTEM.AUTO-DISABLE-OUTSIDE` | `bool` | `true`, `false` | `true` | Configures the technical `AUTO-DISABLE-OUTSIDE` parameter for `FLY-SYSTEM.AUTO-DISABLE-OUTSIDE` in `config.yml`. |

### 3. Practical Setup Example

```yaml
FLY-SYSTEM:
  # The permission required for regular players/ranks to fly in spawn or cuboids.
  PLAYER-FLY-PERMISSION: 'ultimatedonutsmp.player.fly'
  # Disable flight automatically when the player leaves the allowed areas or enters combat.
  AUTO-DISABLE-OUTSIDE: true
# Configuration section for Worth Lore.
```

---

## Section: `WORTH-LORE`

### 1. Commented Setup Code Example

```yaml
WORTH-LORE:
  # Determines whether Enabled is enabled or disabled. Available options: true, false
  ENABLED: true
  # The text or value for Format. Available options: Any valid string text
  FORMAT: '&7Worth: &a$%price%'
# Configuration section for End Crystal.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `WORTH-LORE.ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `WORTH-LORE` system. Set to `true` to enable, `false` to disable. |
| `WORTH-LORE.FORMAT` | `str` | Any string text | `'&7Worth: &a$%price%'` | Configures the technical `FORMAT` parameter for `WORTH-LORE.FORMAT` in `config.yml`. |

### 3. Practical Setup Example

```yaml
WORTH-LORE:
  # Determines whether Enabled is enabled or disabled. Available options: true, false
  ENABLED: true
  # The text or value for Format. Available options: Any valid string text
  FORMAT: '&7Worth: &a$%price%'
# Configuration section for End Crystal.
```

---

## Section: `END-CRYSTAL`

### 1. Commented Setup Code Example

```yaml
END-CRYSTAL:
  # Determines whether Enabled is enabled or disabled. Available options: true, false
  ENABLED: false
  # The decimal value for Damage. Available options: Any decimal number
  DAMAGE: 2.0
# Configuration section for Fast Crystals.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `END-CRYSTAL.ENABLED` | `bool` | `true`, `false` | `false` | Global toggle for `END-CRYSTAL` system. Set to `true` to enable, `false` to disable. |
| `END-CRYSTAL.DAMAGE` | `float` | Any decimal number | `'2.0'` | Configures the technical `DAMAGE` parameter for `END-CRYSTAL.DAMAGE` in `config.yml`. |

### 3. Practical Setup Example

```yaml
END-CRYSTAL:
  # Determines whether Enabled is enabled or disabled. Available options: true, false
  ENABLED: false
  # The decimal value for Damage. Available options: Any decimal number
  DAMAGE: 2.0
# Configuration section for Fast Crystals.
```

---

## Section: `FAST-CRYSTALS`

### 1. Commented Setup Code Example

```yaml
FAST-CRYSTALS:
  # Determines whether Enabled is enabled or disabled. Available options: true, false
  ENABLED: true
  # Determines whether Default Player State is enabled or disabled. Available options: true, false
  DEFAULT-PLAYER-STATE: true
  # Configuration section for Excluded Worlds.
  EXCLUDED-WORLDS:
  - duels
  # Configuration section for Place.
  PLACE:
    # The numerical value for Enabled Cooldown Ticks. Available options: Any valid integer
    ENABLED-COOLDOWN-TICKS: 0
    # The numerical value for Disabled Cooldown Ticks. Available options: Any valid integer
    DISABLED-COOLDOWN-TICKS: 8
    # The numerical value for Debounce Ms. Available options: Any valid integer
    DEBOUNCE-MS: 40
    # Determines whether Require Valid Base is enabled or disabled. Available options: true, false
    REQUIRE-VALID-BASE: true
    # Configuration section for Valid Bases.
    VALID-BASES:
    - OBSIDIAN
    - BEDROCK
    # Determines whether Require Air Above is enabled or disabled. Available options: true, false
    REQUIRE-AIR-ABOVE: true
    # Determines whether Require Air Two Above is enabled or disabled. Available options: true, false
    REQUIRE-AIR-TWO-ABOVE: true
  # Configuration section for Break.
  BREAK:
    # Determines whether Clear Cooldown After Hit is enabled or disabled. Available options: true, false
    CLEAR-COOLDOWN-AFTER-HIT: true
# Configuration section for Respawn Anchor.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `FAST-CRYSTALS.ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `FAST-CRYSTALS` system. Set to `true` to enable, `false` to disable. |
| `FAST-CRYSTALS.DEFAULT-PLAYER-STATE` | `bool` | `true`, `false` | `true` | Configures the technical `DEFAULT-PLAYER-STATE` parameter for `FAST-CRYSTALS.DEFAULT-PLAYER-STATE` in `config.yml`. |
| `FAST-CRYSTALS.EXCLUDED-WORLDS` | `list` | List of configured items/strings | `['duels']` | Configures the technical `EXCLUDED-WORLDS` parameter for `FAST-CRYSTALS.EXCLUDED-WORLDS` in `config.yml`. |
| `FAST-CRYSTALS.PLACE.ENABLED-COOLDOWN-TICKS` | `int` | Any valid integer number | `'0'` | Configures the technical `ENABLED-COOLDOWN-TICKS` parameter for `FAST-CRYSTALS.PLACE.ENABLED-COOLDOWN-TICKS` in `config.yml`. |
| `FAST-CRYSTALS.PLACE.DISABLED-COOLDOWN-TICKS` | `int` | Any valid integer number | `'8'` | Configures the technical `DISABLED-COOLDOWN-TICKS` parameter for `FAST-CRYSTALS.PLACE.DISABLED-COOLDOWN-TICKS` in `config.yml`. |
| `FAST-CRYSTALS.PLACE.DEBOUNCE-MS` | `int` | Any valid integer number | `'40'` | Configures the technical `DEBOUNCE-MS` parameter for `FAST-CRYSTALS.PLACE.DEBOUNCE-MS` in `config.yml`. |
| `FAST-CRYSTALS.PLACE.REQUIRE-VALID-BASE` | `bool` | `true`, `false` | `true` | Configures the technical `REQUIRE-VALID-BASE` parameter for `FAST-CRYSTALS.PLACE.REQUIRE-VALID-BASE` in `config.yml`. |
| `FAST-CRYSTALS.PLACE.VALID-BASES` | `list` | List of configured items/strings | `['OBSIDIAN', 'BEDROCK']` | Configures the technical `VALID-BASES` parameter for `FAST-CRYSTALS.PLACE.VALID-BASES` in `config.yml`. |
| `FAST-CRYSTALS.PLACE.REQUIRE-AIR-ABOVE` | `bool` | `true`, `false` | `true` | Configures the technical `REQUIRE-AIR-ABOVE` parameter for `FAST-CRYSTALS.PLACE.REQUIRE-AIR-ABOVE` in `config.yml`. |
| `FAST-CRYSTALS.PLACE.REQUIRE-AIR-TWO-ABOVE` | `bool` | `true`, `false` | `true` | Configures the technical `REQUIRE-AIR-TWO-ABOVE` parameter for `FAST-CRYSTALS.PLACE.REQUIRE-AIR-TWO-ABOVE` in `config.yml`. |
| `FAST-CRYSTALS.BREAK.CLEAR-COOLDOWN-AFTER-HIT` | `bool` | `true`, `false` | `true` | Configures the technical `CLEAR-COOLDOWN-AFTER-HIT` parameter for `FAST-CRYSTALS.BREAK.CLEAR-COOLDOWN-AFTER-HIT` in `config.yml`. |

### 3. Practical Setup Example

```yaml
FAST-CRYSTALS:
  # Determines whether Enabled is enabled or disabled. Available options: true, false
  ENABLED: true
  # Determines whether Default Player State is enabled or disabled. Available options: true, false
  DEFAULT-PLAYER-STATE: true
  # Configuration section for Excluded Worlds.
  EXCLUDED-WORLDS:
  - duels
  # Configuration section for Place.
  PLACE:
    # The numerical value for Enabled Cooldown Ticks. Available options: Any valid integer
    ENABLED-COOLDOWN-TICKS: 0
    # The numerical value for Disabled Cooldown Ticks. Available options: Any valid integer
    DISABLED-COOLDOWN-TICKS: 8
    # The numerical value for Debounce Ms. Available options: Any valid integer
    DEBOUNCE-MS: 40
    # Determines whether Require Valid Base is enabled or disabled. Available options: true, false
    REQUIRE-VALID-BASE: true
    # Configuration section for Valid Bases.
    VALID-BASES:
    - OBSIDIAN
    - BEDROCK
    # Determines whether Require Air Above is enabled or disabled. Ava
```

---

## Section: `RESPAWN-ANCHOR`

### 1. Commented Setup Code Example

```yaml
RESPAWN-ANCHOR:
  # Determines whether Enabled is enabled or disabled. Available options: true, false
  ENABLED: false
  # The decimal value for Damage. Available options: Any decimal number
  DAMAGE: 2.0
# Configuration section for Ender Chest.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `RESPAWN-ANCHOR.ENABLED` | `bool` | `true`, `false` | `false` | Global toggle for `RESPAWN-ANCHOR` system. Set to `true` to enable, `false` to disable. |
| `RESPAWN-ANCHOR.DAMAGE` | `float` | Any decimal number | `'2.0'` | Configures the technical `DAMAGE` parameter for `RESPAWN-ANCHOR.DAMAGE` in `config.yml`. |

### 3. Practical Setup Example

```yaml
RESPAWN-ANCHOR:
  # Determines whether Enabled is enabled or disabled. Available options: true, false
  ENABLED: false
  # The decimal value for Damage. Available options: Any decimal number
  DAMAGE: 2.0
# Configuration section for Ender Chest.
```

---

## Section: `ENDER-CHEST`

### 1. Commented Setup Code Example

```yaml
ENDER-CHEST:
  # Determines whether Six Row is enabled or disabled. Available options: true, false
  SIX-ROW: true
# Configuration section for Lunar Client.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `ENDER-CHEST.SIX-ROW` | `bool` | `true`, `false` | `true` | Configures the technical `SIX-ROW` parameter for `ENDER-CHEST.SIX-ROW` in `config.yml`. |

### 3. Practical Setup Example

```yaml
ENDER-CHEST:
  # Determines whether Six Row is enabled or disabled. Available options: true, false
  SIX-ROW: true
# Configuration section for Lunar Client.
```

---

## Section: `LUNAR-CLIENT`

### 1. Commented Setup Code Example

```yaml
LUNAR-CLIENT:
  # Configuration section for Rich Presence.
  RICH-PRESENCE:
    # Determines whether Enabled is enabled or disabled. Available options: true, false
    ENABLED: true
    # The numerical value for Update. Available options: Any valid integer
    UPDATE: 1
    # The text or value for Player State. Available options: Any valid string text
    PLAYER-STATE: Playing
    # The text or value for Game State. Available options: Any valid string text
    GAME-STATE: Playing
    # The text or value for Game Name. Available options: Any valid string text
    GAME-NAME: Economy
    # The text or value for Variant. Available options: Any valid string text
    VARIANT: '%economy_username% ($%economy_nicestMoney%)'
    # The text or value for World Name. Available options: Any valid string text
    WORLD-NAME: Economy
    # The text or value for Sub Server Name. Available options: Any valid string text
    SUB-SERVER-NAME: SMP
    # The text or value for Team Current Size. Available options: Any valid string text
    TEAM-CURRENT-SIZE: '{team_size}'
    # The text or value for Team Max Size. Available options: Any valid string text
    TEAM-MAX-SIZE: '{team_max_size}'
    # The numerical value for Max Field Length. Available options: Any valid integer
    MAX-FIELD-LENGTH: 128
  # Configuration section for Team View.
  TEAM-VIEW:
    # Determines whether Enabled is enabled or disabled. Available options: true, false
    ENABLED: true
    # The numerical value for Update. Available options: Any valid integer
    UPDATE: 20
# Configuration section for Shards.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `LUNAR-CLIENT.RICH-PRESENCE.ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `LUNAR-CLIENT` system. Set to `true` to enable, `false` to disable. |
| `LUNAR-CLIENT.RICH-PRESENCE.UPDATE` | `int` | Any valid integer number | `'1'` | Configures the technical `UPDATE` parameter for `LUNAR-CLIENT.RICH-PRESENCE.UPDATE` in `config.yml`. |
| `LUNAR-CLIENT.RICH-PRESENCE.PLAYER-STATE` | `str` | Any string text | `'Playing'` | Configures the technical `PLAYER-STATE` parameter for `LUNAR-CLIENT.RICH-PRESENCE.PLAYER-STATE` in `config.yml`. |
| `LUNAR-CLIENT.RICH-PRESENCE.GAME-STATE` | `str` | Any string text | `'Playing'` | Configures the technical `GAME-STATE` parameter for `LUNAR-CLIENT.RICH-PRESENCE.GAME-STATE` in `config.yml`. |
| `LUNAR-CLIENT.RICH-PRESENCE.GAME-NAME` | `str` | Any string text | `'Economy'` | Configures the technical `GAME-NAME` parameter for `LUNAR-CLIENT.RICH-PRESENCE.GAME-NAME` in `config.yml`. |
| `LUNAR-CLIENT.RICH-PRESENCE.VARIANT` | `str` | Any string text | `'%economy_username% ($%economy_nices...'` | Configures the technical `VARIANT` parameter for `LUNAR-CLIENT.RICH-PRESENCE.VARIANT` in `config.yml`. |
| `LUNAR-CLIENT.RICH-PRESENCE.WORLD-NAME` | `str` | Any string text | `'Economy'` | Configures the technical `WORLD-NAME` parameter for `LUNAR-CLIENT.RICH-PRESENCE.WORLD-NAME` in `config.yml`. |
| `LUNAR-CLIENT.RICH-PRESENCE.SUB-SERVER-NAME` | `str` | Any string text | `'SMP'` | Configures the technical `SUB-SERVER-NAME` parameter for `LUNAR-CLIENT.RICH-PRESENCE.SUB-SERVER-NAME` in `config.yml`. |
| `LUNAR-CLIENT.RICH-PRESENCE.TEAM-CURRENT-SIZE` | `str` | Any string text | `'{team_size}'` | Configures the technical `TEAM-CURRENT-SIZE` parameter for `LUNAR-CLIENT.RICH-PRESENCE.TEAM-CURRENT-SIZE` in `config.yml`. |
| `LUNAR-CLIENT.RICH-PRESENCE.TEAM-MAX-SIZE` | `str` | Any string text | `'{team_max_size}'` | Configures the technical `TEAM-MAX-SIZE` parameter for `LUNAR-CLIENT.RICH-PRESENCE.TEAM-MAX-SIZE` in `config.yml`. |
| `LUNAR-CLIENT.RICH-PRESENCE.MAX-FIELD-LENGTH` | `int` | Any valid integer number | `'128'` | Configures the technical `MAX-FIELD-LENGTH` parameter for `LUNAR-CLIENT.RICH-PRESENCE.MAX-FIELD-LENGTH` in `config.yml`. |
| `LUNAR-CLIENT.TEAM-VIEW.ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `LUNAR-CLIENT` system. Set to `true` to enable, `false` to disable. |
| `LUNAR-CLIENT.TEAM-VIEW.UPDATE` | `int` | Any valid integer number | `'20'` | Configures the technical `UPDATE` parameter for `LUNAR-CLIENT.TEAM-VIEW.UPDATE` in `config.yml`. |

### 3. Practical Setup Example

```yaml
LUNAR-CLIENT:
  # Configuration section for Rich Presence.
  RICH-PRESENCE:
    # Determines whether Enabled is enabled or disabled. Available options: true, false
    ENABLED: true
    # The numerical value for Update. Available options: Any valid integer
    UPDATE: 1
    # The text or value for Player State. Available options: Any valid string text
    PLAYER-STATE: Playing
    # The text or value for Game State. Available options: Any valid string text
    GAME-STATE: Playing
    # The text or value for Game Name. Available options: Any valid string text
    GAME-NAME: Economy
    # The text or value for Variant. Available options: Any valid string text
    VARIANT: '%economy_username% ($%economy_nicestMoney%)'
    # The text or value for World Name. Available options: Any valid string text
    WORLD-NAME: Economy
    # The text or value for Sub Server Name. Available options: Any valid string text
    SUB-SERVER-NAME: SMP
    # The text or value for Team Current Size. Available op
```

---

## Section: `SHARDS`

### 1. Commented Setup Code Example

```yaml
SHARDS:
  # The numerical value for Every. Available options: Any valid integer
  EVERY: 1
  # The numerical value for Amount. Available options: Any valid integer
  AMOUNT: 1
  # The text or value for Countdown. Available options: Any valid string text
  COUNTDOWN: '&7Next shard in &#A303F9%time%'
  # The text or value for Received. Available options: Any valid string text
  RECEIVED: '&#A303F9You received %amount% Shard &7(Total: &#A303F9%total%&7)'
  # The text or value for Received Boosted. Available options: Any valid string text
  RECEIVED-BOOSTED: '&#A303F9You received %amount% Shards &7(&ax%multiplier%&7) &7(Total:
    &#A303F9%total%&7)'
  # The text or value for Cancelled Message. Available options: Any valid string text
  CANCELLED-MESSAGE: '&cShard reward cancelled &7(Left %cuboid% zone)'
  # Determines whether Reset On Leave is enabled or disabled. Available options: true, false
  RESET-ON-LEAVE: true
  # Configuration section for Cuboids.
  CUBOIDS:
    # Configuration section for Regions.
    REGIONS:
      # Configuration section for Spawn.
      spawn:
        # Determines whether Enabled is enabled or disabled. Available options: true, false
        ENABLED: false
        # Determines whether Bound is enabled or disabled. Available options: true, false
        BOUND: false
        # The numerical value for Priority. Available options: Any valid integer
        PRIORITY: 100
        # The text or value for Cuboid. Available options: Any valid string text
        CUBOID: ''
        # The text or value for World. Available options: Any valid string text
        WORLD: world
        # The numerical value for Interval. Available options: Any valid integer
        INTERVAL: 60
        # The numerical value for Amount. Available options: Any valid integer
        AMOUNT: 1
        # The text or value for Countdown Message. Available options: Any valid string text
        COUNTDOWN-MESSAGE: '&7Next shard in &#A303F9%time%'
        # The text or value for Reward Message. Available options: Any valid string text
        REWARD-MESSAGE: '&#A303F9You received %amount% Shard &7(Total: &#A303F9%total%&7)'
        # The text or value for Boosted Reward Message. Available options: Any valid string text
        BOOSTED-REWARD-MESSAGE: '&#A303F9You received %amount% Shards &7(&ax%multiplier%&7)
          # The text or value for # The Text Or Mode For &7(Total. Available Options. Available options: Any valid string text
          # The text or mode for &7(Total. Available options: Any string text &7(Total:
          &#A303F9%total%&7)'
        # The text or value for Leave Message. Available options: Any valid string text
        LEAVE-MESSAGE: '&cShard reward cancelled &7(Left %cuboid% zone)'
        # The numerical value for Afk Time. Available options: Any valid integer
        AFK-TIME: 120
        # The text or value for Afk Cuboid. Available options: Any valid string text
        AFK-CUBOID: ''
        # The text or value for Afk Location. Available options: Any valid string text
        AFK-LOCATION: ''
        # The text or value for Afk Message. Available options: Any valid string text
        AFK-MESSAGE: '&7You have been moved to the AFK area for being inactive in
          the shard zone.'
        # Determines whether Teleport On Afk is enabled or disabled. Available options: true, false
        TELEPORT-ON-AFK: true
        # Configuration section for Excluded Worlds.
        EXCLUDED-WORLDS:
        - duels
        # The numerical value for Recent Movement Window. Available options: Any valid integer
        RECENT-MOVEMENT-WINDOW: 15
        # The numerical value for Min Movement Blocks. Available options: Any valid integer
        MIN-MOVEMENT-BLOCKS: 5
        # Determines whether Reset On Leave is enabled or disabled. Available options: true, false
        RESET-ON-LEAVE: true
        # The text or value for Paused Message. Available options: Any valid string text
        PAUSED-MESSAGE: '&eMove to keep earning shards &7(%movement%/%required_movement%)'
        # The text or value for Afk Paused Message. Available options: Any valid string text
        AFK-PAUSED-MESSAGE: '&cYou are AFK. Move to resume shard gain'
        # The text or value for Excluded World Message. Available options: Any valid string text
        EXCLUDED-WORLD-MESSAGE: '&cShards are disabled in this world'
  # Configuration section for Everywhere.
  EVERYWHERE:
    # Determines whether Enabled is enabled or disabled. Available options: true, false
    ENABLED: false
    # The numerical value for Every. Available options: Any valid integer
    EVERY: 3
    # The numerical value for Amount. Available options: Any valid integer
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `SHARDS.EVERY` | `int` | Any valid integer number | `'1'` | Configures the technical `EVERY` parameter for `SHARDS.EVERY` in `config.yml`. |
| `SHARDS.AMOUNT` | `int` | Any valid integer number | `'1'` | Configures the technical `AMOUNT` parameter for `SHARDS.AMOUNT` in `config.yml`. |
| `SHARDS.COUNTDOWN` | `str` | Any string text | `'&7Next shard in &#A303F9%time%'` | Configures the technical `COUNTDOWN` parameter for `SHARDS.COUNTDOWN` in `config.yml`. |
| `SHARDS.RECEIVED` | `str` | Any string text | `'&#A303F9You received %amount% Shard...'` | Configures the technical `RECEIVED` parameter for `SHARDS.RECEIVED` in `config.yml`. |
| `SHARDS.RECEIVED-BOOSTED` | `str` | Any string text | `'&#A303F9You received %amount% Shard...'` | Configures the technical `RECEIVED-BOOSTED` parameter for `SHARDS.RECEIVED-BOOSTED` in `config.yml`. |
| `SHARDS.CANCELLED-MESSAGE` | `str` | Any string text | `'&cShard reward cancelled &7(Left %c...'` | Configures the technical `CANCELLED-MESSAGE` parameter for `SHARDS.CANCELLED-MESSAGE` in `config.yml`. |
| `SHARDS.RESET-ON-LEAVE` | `bool` | `true`, `false` | `true` | Configures the technical `RESET-ON-LEAVE` parameter for `SHARDS.RESET-ON-LEAVE` in `config.yml`. |
| `SHARDS.CUBOIDS.REGIONS.spawn.ENABLED` | `bool` | `true`, `false` | `false` | Global toggle for `SHARDS` system. Set to `true` to enable, `false` to disable. |
| `SHARDS.CUBOIDS.REGIONS.spawn.BOUND` | `bool` | `true`, `false` | `false` | Configures the technical `BOUND` parameter for `SHARDS.CUBOIDS.REGIONS.spawn.BOUND` in `config.yml`. |
| `SHARDS.CUBOIDS.REGIONS.spawn.PRIORITY` | `int` | Any valid integer number | `'100'` | Configures the technical `PRIORITY` parameter for `SHARDS.CUBOIDS.REGIONS.spawn.PRIORITY` in `config.yml`. |
| `SHARDS.CUBOIDS.REGIONS.spawn.CUBOID` | `str` | Any string text | `''` | Configures the technical `CUBOID` parameter for `SHARDS.CUBOIDS.REGIONS.spawn.CUBOID` in `config.yml`. |
| `SHARDS.CUBOIDS.REGIONS.spawn.WORLD` | `str` | Any string text | `'world'` | Configures the technical `WORLD` parameter for `SHARDS.CUBOIDS.REGIONS.spawn.WORLD` in `config.yml`. |
| `SHARDS.CUBOIDS.REGIONS.spawn.INTERVAL` | `int` | Any valid integer number | `'60'` | Configures the technical `INTERVAL` parameter for `SHARDS.CUBOIDS.REGIONS.spawn.INTERVAL` in `config.yml`. |
| `SHARDS.CUBOIDS.REGIONS.spawn.AMOUNT` | `int` | Any valid integer number | `'1'` | Configures the technical `AMOUNT` parameter for `SHARDS.CUBOIDS.REGIONS.spawn.AMOUNT` in `config.yml`. |
| `SHARDS.CUBOIDS.REGIONS.spawn.COUNTDOWN-MESSAGE` | `str` | Any string text | `'&7Next shard in &#A303F9%time%'` | Configures the technical `COUNTDOWN-MESSAGE` parameter for `SHARDS.CUBOIDS.REGIONS.spawn.COUNTDOWN-MESSAGE` in `config.yml`. |
| `SHARDS.CUBOIDS.REGIONS.spawn.REWARD-MESSAGE` | `str` | Any string text | `'&#A303F9You received %amount% Shard...'` | Configures the technical `REWARD-MESSAGE` parameter for `SHARDS.CUBOIDS.REGIONS.spawn.REWARD-MESSAGE` in `config.yml`. |
| `SHARDS.CUBOIDS.REGIONS.spawn.BOOSTED-REWARD-MESSAGE` | `str` | Any string text | `'&#A303F9You received %amount% Shard...'` | Configures the technical `BOOSTED-REWARD-MESSAGE` parameter for `SHARDS.CUBOIDS.REGIONS.spawn.BOOSTED-REWARD-MESSAGE` in `config.yml`. |
| `SHARDS.CUBOIDS.REGIONS.spawn.LEAVE-MESSAGE` | `str` | Any string text | `'&cShard reward cancelled &7(Left %c...'` | Configures the technical `LEAVE-MESSAGE` parameter for `SHARDS.CUBOIDS.REGIONS.spawn.LEAVE-MESSAGE` in `config.yml`. |
| `SHARDS.CUBOIDS.REGIONS.spawn.AFK-TIME` | `int` | Any valid integer number | `'120'` | Configures the technical `AFK-TIME` parameter for `SHARDS.CUBOIDS.REGIONS.spawn.AFK-TIME` in `config.yml`. |
| `SHARDS.CUBOIDS.REGIONS.spawn.AFK-CUBOID` | `str` | Any string text | `''` | Configures the technical `AFK-CUBOID` parameter for `SHARDS.CUBOIDS.REGIONS.spawn.AFK-CUBOID` in `config.yml`. |
| `SHARDS.CUBOIDS.REGIONS.spawn.AFK-LOCATION` | `str` | Any string text | `''` | Configures the technical `AFK-LOCATION` parameter for `SHARDS.CUBOIDS.REGIONS.spawn.AFK-LOCATION` in `config.yml`. |
| `SHARDS.CUBOIDS.REGIONS.spawn.AFK-MESSAGE` | `str` | Any string text | `'&7You have been moved to the AFK ar...'` | Configures the technical `AFK-MESSAGE` parameter for `SHARDS.CUBOIDS.REGIONS.spawn.AFK-MESSAGE` in `config.yml`. |
| `SHARDS.CUBOIDS.REGIONS.spawn.TELEPORT-ON-AFK` | `bool` | `true`, `false` | `true` | Configures the technical `TELEPORT-ON-AFK` parameter for `SHARDS.CUBOIDS.REGIONS.spawn.TELEPORT-ON-AFK` in `config.yml`. |
| `SHARDS.CUBOIDS.REGIONS.spawn.EXCLUDED-WORLDS` | `list` | List of configured items/strings | `['duels']` | Configures the technical `EXCLUDED-WORLDS` parameter for `SHARDS.CUBOIDS.REGIONS.spawn.EXCLUDED-WORLDS` in `config.yml`. |
| `SHARDS.CUBOIDS.REGIONS.spawn.RECENT-MOVEMENT-WINDOW` | `int` | Any valid integer number | `'15'` | Configures the technical `RECENT-MOVEMENT-WINDOW` parameter for `SHARDS.CUBOIDS.REGIONS.spawn.RECENT-MOVEMENT-WINDOW` in `config.yml`. |
| `SHARDS.CUBOIDS.REGIONS.spawn.MIN-MOVEMENT-BLOCKS` | `int` | Any non-negative integer (e.g. `0`, `5`) | `'5'` | Minimum blocks player must move to keep earning shards. Set to `0` to completely disable movement check (allow passive AFK shard earning). |
| `SHARDS.CUBOIDS.REGIONS.spawn.RESET-ON-LEAVE` | `bool` | `true`, `false` | `true` | Configures the technical `RESET-ON-LEAVE` parameter for `SHARDS.CUBOIDS.REGIONS.spawn.RESET-ON-LEAVE` in `config.yml`. |
| `SHARDS.CUBOIDS.REGIONS.spawn.PAUSED-MESSAGE` | `str` | Any string text | `'&eMove to keep earning shards &7(%m...'` | Configures the technical `PAUSED-MESSAGE` parameter for `SHARDS.CUBOIDS.REGIONS.spawn.PAUSED-MESSAGE` in `config.yml`. |
| `SHARDS.CUBOIDS.REGIONS.spawn.AFK-PAUSED-MESSAGE` | `str` | Any string text | `'&cYou are AFK. Move to resume shard...'` | Configures the technical `AFK-PAUSED-MESSAGE` parameter for `SHARDS.CUBOIDS.REGIONS.spawn.AFK-PAUSED-MESSAGE` in `config.yml`. |
| `SHARDS.CUBOIDS.REGIONS.spawn.EXCLUDED-WORLD-MESSAGE` | `str` | Any string text | `'&cShards are disabled in this world'` | Configures the technical `EXCLUDED-WORLD-MESSAGE` parameter for `SHARDS.CUBOIDS.REGIONS.spawn.EXCLUDED-WORLD-MESSAGE` in `config.yml`. |
| `SHARDS.BOOSTER-APPLIES-TO-KILLS` | `bool` | `true`, `false` | `true` | Whether an active shard booster also multiplies player kill rewards. Set to `false` to keep the booster on passive and cuboid shards only. |
| `SHARDS.BOOSTER-KILL-MULTIPLIER` | `int` | Any valid integer number | `'0'` | Separate booster multiplier used only for kill rewards. Set to `0` to reuse `SHARDS.BOOSTER-MULTIPLIER`. |
| *(10 additional sub-keys configured in section)* | | | | |

### 3. Practical Setup Example

```yaml
SHARDS:
  # The numerical value for Every. Available options: Any valid integer
  EVERY: 1
  # The numerical value for Amount. Available options: Any valid integer
  AMOUNT: 1
  # The text or value for Countdown. Available options: Any valid string text
  COUNTDOWN: '&7Next shard in &#A303F9%time%'
  # The text or value for Received. Available options: Any valid string text
  RECEIVED: '&#A303F9You received %amount% Shard &7(Total: &#A303F9%total%&7)'
  # The text or value for Received Boosted. Available options: Any valid string text
  RECEIVED-BOOSTED: '&#A303F9You received %amount% Shards &7(&ax%multiplier%&7) &7(Total:
    &#A303F9%total%&7)'
  # The text or value for Cancelled Message. Available options: Any valid string text
  CANCELLED-MESSAGE: '&cShard reward cancelled &7(Left %cuboid% zone)'
  # Determines whether Reset On Leave is enabled or disabled. Available options: true, false
  RESET-ON-LEAVE: true
  # Configuration section for Cuboids.
  CUBOIDS:
    # Configuration se
```

---

## Section: `KEY-ALL`

### 1. Commented Setup Code Example

```yaml
KEY-ALL:
  # Determines whether Enabled is enabled or disabled. Available options: true, false
  ENABLED: true
  # The numerical value for Every. Available options: Any valid integer
  EVERY: 60
  # Configuration section for Commands.
  COMMANDS:
  - ''
  TYPE: RANDOM
  # Configuration section for Random.
  RANDOM:
    # Configuration section for Keys.
    KEYS:
      # The numerical value for Common. Available options: Any valid integer
      common: 60
      # The numerical value for Rare. Available options: Any valid integer
      rare: 30
      # The numerical value for Epic. Available options: Any valid integer
      epic: 10
  # Configuration section for One Key Only.
  ONE-KEY-ONLY:
    # The text or value for Key. Available options: Any valid string text
    KEY: common
  # Configuration section for Notification.
  NOTIFICATION:
    # Determines whether Enabled is enabled or disabled. Available options: true, false
    ENABLED: true
    # Configuration section for Message.
    MESSAGE:
    - ''
    - '&#00A4FCKey-All reward!'
    - '&fYou received &b{amount}x {crate}&f key.'
    - ''
# Configuration section for Team.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `KEY-ALL.ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `KEY-ALL` system. Set to `true` to enable, `false` to disable. |
| `KEY-ALL.EVERY` | `int` | Any valid integer number | `'60'` | Configures the technical `EVERY` parameter for `KEY-ALL.EVERY` in `config.yml`. |
| `KEY-ALL.COMMANDS` | `list` | List of configured items/strings | `['']` | Configures the technical `COMMANDS` parameter for `KEY-ALL.COMMANDS` in `config.yml`. |
| `KEY-ALL.TYPE` | `str` | Any string text | `'RANDOM'` | Configures the technical `TYPE` parameter for `KEY-ALL.TYPE` in `config.yml`. |
| `KEY-ALL.RANDOM.KEYS.common` | `int` | Any valid integer number | `'60'` | Configures the technical `common` parameter for `KEY-ALL.RANDOM.KEYS.common` in `config.yml`. |
| `KEY-ALL.RANDOM.KEYS.rare` | `int` | Any valid integer number | `'30'` | Configures the technical `rare` parameter for `KEY-ALL.RANDOM.KEYS.rare` in `config.yml`. |
| `KEY-ALL.RANDOM.KEYS.epic` | `int` | Any valid integer number | `'10'` | Configures the technical `epic` parameter for `KEY-ALL.RANDOM.KEYS.epic` in `config.yml`. |
| `KEY-ALL.ONE-KEY-ONLY.KEY` | `str` | Any string text | `'common'` | Configures the technical `KEY` parameter for `KEY-ALL.ONE-KEY-ONLY.KEY` in `config.yml`. |
| `KEY-ALL.NOTIFICATION.ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `KEY-ALL` system. Set to `true` to enable, `false` to disable. |
| `KEY-ALL.NOTIFICATION.MESSAGE` | `list` | List of configured items/strings | `[, &#00A4FCKey-All reward!, &fYou received &b{amount}x {crate}&f key....]` | Configures the technical `MESSAGE` parameter for `KEY-ALL.NOTIFICATION.MESSAGE` in `config.yml`. |

### 3. Practical Setup Example

```yaml
KEY-ALL:
  # Determines whether Enabled is enabled or disabled. Available options: true, false
  ENABLED: true
  # The numerical value for Every. Available options: Any valid integer
  EVERY: 60
  # Configuration section for Commands.
  COMMANDS:
  - ''
  TYPE: RANDOM
  # Configuration section for Random.
  RANDOM:
    # Configuration section for Keys.
    KEYS:
      # The numerical value for Common. Available options: Any valid integer
      common: 60
      # The numerical value for Rare. Available options: Any valid integer
      rare: 30
      # The numerical value for Epic. Available options: Any valid integer
      epic: 10
  # Configuration section for One Key Only.
  ONE-KEY-ONLY:
    # The text or value for Key. Available options: Any valid string text
    KEY: common
  # Configuration section for Notification.
  NOTIFICATION:
    # Determines whether Enabled is enabled or disabled. Available options: true, false
    ENABLED: true
    # Configuration section for Message.
    
```

---

## Section: `TEAM`

### 1. Commented Setup Code Example

```yaml
TEAM:
  # The numerical value for Name Min Length. Available options: Any valid integer
  NAME-MIN-LENGTH: 3
  # The numerical value for Name Max Length. Available options: Any valid integer
  NAME-MAX-LENGTH: 5
  # The numerical value for Limit Members. Available options: Any valid integer
  LIMIT-MEMBERS: 10
# Configuration section for Leaderboard.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `TEAM.NAME-MIN-LENGTH` | `int` | Any valid integer number | `'3'` | Configures the technical `NAME-MIN-LENGTH` parameter for `TEAM.NAME-MIN-LENGTH` in `config.yml`. |
| `TEAM.NAME-MAX-LENGTH` | `int` | Any valid integer number | `'5'` | Configures the technical `NAME-MAX-LENGTH` parameter for `TEAM.NAME-MAX-LENGTH` in `config.yml`. |
| `TEAM.LIMIT-MEMBERS` | `int` | Any valid integer number | `'10'` | Configures the technical `LIMIT-MEMBERS` parameter for `TEAM.LIMIT-MEMBERS` in `config.yml`. |

### 3. Practical Setup Example

```yaml
TEAM:
  # The numerical value for Name Min Length. Available options: Any valid integer
  NAME-MIN-LENGTH: 3
  # The numerical value for Name Max Length. Available options: Any valid integer
  NAME-MAX-LENGTH: 5
  # The numerical value for Limit Members. Available options: Any valid integer
  LIMIT-MEMBERS: 10
# Configuration section for Leaderboard.
```

---

## Section: `LEADERBOARD`

### 1. Commented Setup Code Example

```yaml
LEADERBOARD:
  # The numerical value for Update. Available options: Any valid integer
  UPDATE: 10
  # The numerical value for Npc Refresh. Available options: Any valid integer
  NPC-REFRESH: 1
# Configuration section for Tablist.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `LEADERBOARD.UPDATE` | `int` | Any valid integer number | `'10'` | Configures the technical `UPDATE` parameter for `LEADERBOARD.UPDATE` in `config.yml`. |
| `LEADERBOARD.NPC-REFRESH` | `int` | Any valid integer number | `'1'` | Configures the technical `NPC-REFRESH` parameter for `LEADERBOARD.NPC-REFRESH` in `config.yml`. |

### 3. Practical Setup Example

```yaml
LEADERBOARD:
  # The numerical value for Update. Available options: Any valid integer
  UPDATE: 10
  # The numerical value for Npc Refresh. Available options: Any valid integer
  NPC-REFRESH: 1
# Configuration section for Tablist.
```

---

## Section: `TABLIST`

### 1. Commented Setup Code Example

```yaml
TABLIST:
  # Determines whether Enabled is enabled or disabled. Available options: true, false
  ENABLED: true
  # Determines whether Luckperms Priority is enabled or disabled. Available options: true, false
  LUCKPERMS-PRIORITY: true
  # Determines whether Show Team Name is enabled or disabled. Available options: true, false
  SHOW-TEAM-NAME: true
  # The text or value for Icon Head Skin. Available options: Any valid string text
  ICON-HEAD-SKIN: <head:%player_name%>
  # The text or value for Icon Media. Available options: Any valid string text
  ICON-MEDIA: 📹
  # The text or value for Media Badge Format. Available options: Any valid string text
  MEDIA-BADGE-FORMAT: '&d<icon_media>&#37BFF9+'
  # The text or value for Media Badge Permission. Available options: Any valid string text
  MEDIA-BADGE-PERMISSION: rank.media
  # The text or value for Name Format. Available options: Any valid string text
  NAME-FORMAT: <icon_head_skin> <media_badge>&f<nick>%team_suffix%
  # Configuration section for Header.
  HEADER:
  - ''
  - <#00ADFC>&lServer Name</#00FCFC>
  - '&f%online% Players'
  - ''
  # Configuration section for Footer.
  FOOTER:
  - ''
  - '   &#37BFF9/discord  /guide  /store   '
  - ''
# Configuration section for Optimization.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `TABLIST.ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `TABLIST` system. Set to `true` to enable, `false` to disable. |
| `TABLIST.LUCKPERMS-PRIORITY` | `bool` | `true`, `false` | `true` | Configures the technical `LUCKPERMS-PRIORITY` parameter for `TABLIST.LUCKPERMS-PRIORITY` in `config.yml`. |
| `TABLIST.SHOW-TEAM-NAME` | `bool` | `true`, `false` | `true` | Configures the technical `SHOW-TEAM-NAME` parameter for `TABLIST.SHOW-TEAM-NAME` in `config.yml`. |
| `TABLIST.ICON-HEAD-SKIN` | `str` | Any string text | `'<head:%player_name%>'` | Configures the technical `ICON-HEAD-SKIN` parameter for `TABLIST.ICON-HEAD-SKIN` in `config.yml`. |
| `TABLIST.ICON-MEDIA` | `str` | Any string text | `'📹'` | Configures the technical `ICON-MEDIA` parameter for `TABLIST.ICON-MEDIA` in `config.yml`. |
| `TABLIST.MEDIA-BADGE-FORMAT` | `str` | Any string text | `'&d<icon_media>&#37BFF9+'` | Configures the technical `MEDIA-BADGE-FORMAT` parameter for `TABLIST.MEDIA-BADGE-FORMAT` in `config.yml`. |
| `TABLIST.MEDIA-BADGE-PERMISSION` | `str` | Any string text | `'rank.media'` | Configures the technical `MEDIA-BADGE-PERMISSION` parameter for `TABLIST.MEDIA-BADGE-PERMISSION` in `config.yml`. Note: Media permissions (`rank.media`, `rank.media.plus`, `rank.media.include`) require explicit LuckPerms assignment and are not auto-granted to OP players. |
| `TABLIST.NAME-FORMAT` | `str` | Any string text | `'<icon_head_skin> <media_badge>&f<ni...'` | Configures the technical `NAME-FORMAT` parameter for `TABLIST.NAME-FORMAT` in `config.yml`. |
| `TABLIST.HEADER` | `list` | List of configured items/strings | `[, <#00ADFC>&lServer Name</#00FCFC>, &f%online% Players...]` | Configures the technical `HEADER` parameter for `TABLIST.HEADER` in `config.yml`. |
| `TABLIST.FOOTER` | `list` | List of configured items/strings | `['', '   &#37BFF9/discord  /guide  /store   ', '']` | Configures the technical `FOOTER` parameter for `TABLIST.FOOTER` in `config.yml`. |

### 3. Practical Setup Example

```yaml
TABLIST:
  # Determines whether Enabled is enabled or disabled. Available options: true, false
  ENABLED: true
  # Determines whether Luckperms Priority is enabled or disabled. Available options: true, false
  LUCKPERMS-PRIORITY: true
  # Determines whether Show Team Name is enabled or disabled. Available options: true, false
  SHOW-TEAM-NAME: true
  # The text or value for Icon Head Skin. Available options: Any valid string text
  ICON-HEAD-SKIN: <head:%player_name%>
  # The text or value for Icon Media. Available options: Any valid string text
  ICON-MEDIA: 📹
  # The text or value for Media Badge Format. Available options: Any valid string text
  MEDIA-BADGE-FORMAT: '&d<icon_media>&#37BFF9+'
  # The text or value for Media Badge Permission. Available options: Any valid string text
  MEDIA-BADGE-PERMISSION: rank.media
  # The text or value for Name Format. Available options: Any valid string text
  NAME-FORMAT: <icon_head_skin> <media_badge>&f<nick>%team_suffix%
  # Configuration sect
```

---

## Section: `CLEAR-LAG`

### 1. Commented Setup Code Example

```yaml
CLEAR-LAG:
  # Determines whether Enabled is enabled or disabled. Available options: true, false
  ENABLED: true
  # The numerical value for Every. Available options: Any valid integer
  EVERY: 5
  # Determines whether Animals is enabled or disabled. Available options: true, false
  ANIMALS: false
  # Determines whether Monsters is enabled or disabled. Available options: true, false
  MONSTERS: false
  # Determines whether Dropped Items is enabled or disabled. Available options: true, false
  DROPPED-ITEMS: true
  # The numerical value for Min Item Age Seconds. Dropped items younger than this are kept,
  # so items dropped just before a cleanup are not wiped. Set to 0 to disable the delay.
  # Available options: Any valid integer
  MIN-ITEM-AGE-SECONDS: 60
  # Configuration section for Excluded Worlds.
  EXCLUDED-WORLDS:
  - duels
  EXCLUDE-NAMED: true
  EXCLUDE-TAMED: true
  EXCLUDE-VILLAGERS: true
  # Configuration section for Excluded Entity Types. Entity types listed here are never
  # cleared, for example ALLAY or IRON_GOLEM.
  EXCLUDED-ENTITY-TYPES: []
  # Configuration section for Excluded Item Materials. Dropped items of these materials are
  # never cleared, for example NETHERITE_INGOT or ELYTRA.
  EXCLUDED-ITEM-MATERIALS: []
# Configuration section for Combat Manager.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `CLEAR-LAG.ENABLED` | `bool` | `true`, `false` | `true` | Master switch for the cleanup task and `/clearlag`. The `CLEAR_LAG` feature toggle must also be enabled. |
| `CLEAR-LAG.EVERY` | `int` | Any valid integer | `5` | Minutes between cleanup runs. Countdown warnings are broadcast 60, 30, 15, 10, 5, 4, 3, 2 and 1 second before each run. Values below `1` are treated as `1`. |
| `CLEAR-LAG.ANIMALS` | `bool` | `true`, `false` | `false` | Removes passive animals during a cleanup run. |
| `CLEAR-LAG.MONSTERS` | `bool` | `true`, `false` | `false` | Removes monsters, slimes and flying hostiles during a cleanup run. |
| `CLEAR-LAG.DROPPED-ITEMS` | `bool` | `true`, `false` | `true` | Removes dropped item entities during a cleanup run. |
| `CLEAR-LAG.MIN-ITEM-AGE-SECONDS` | `int` | Any valid integer | `60` | Grace period for dropped items. Items that have existed for fewer seconds than this are skipped, so items dropped shortly before a run survive until the next one. Set to `0` to clear items regardless of age. |
| `CLEAR-LAG.EXCLUDED-WORLDS` | `list` | List of configured items/strings | `['duels']` | World names that are skipped entirely, so nothing inside them is ever cleared. |
| `CLEAR-LAG.EXCLUDE-NAMED` | `bool` | `true`, `false` | `true` | Skips entities that carry a custom name. |
| `CLEAR-LAG.EXCLUDE-TAMED` | `bool` | `true`, `false` | `true` | Skips tamed entities such as pets and horses. |
| `CLEAR-LAG.EXCLUDE-VILLAGERS` | `bool` | `true`, `false` | `true` | Skips villagers, wandering traders and NPCs. |
| `CLEAR-LAG.EXCLUDED-ENTITY-TYPES` | `list` | List of configured items/strings | `[]` | Entity type names that are never cleared, for example `ALLAY` or `IRON_GOLEM`. Matched case-insensitively against the entity type. |
| `CLEAR-LAG.EXCLUDED-ITEM-MATERIALS` | `list` | List of configured items/strings | `[]` | Material names that are never cleared when they lie on the ground, for example `NETHERITE_INGOT` or `ELYTRA`. Matched case-insensitively against the dropped stack. |

### 3. Practical Setup Example

```yaml
CLEAR-LAG:
  # Determines whether Enabled is enabled or disabled. Available options: true, false
  ENABLED: true
  # The numerical value for Every. Available options: Any valid integer
  EVERY: 5
  # Determines whether Animals is enabled or disabled. Available options: true, false
  ANIMALS: false
  # Determines whether Monsters is enabled or disabled. Available options: true, false
  MONSTERS: false
  # Determines whether Dropped Items is enabled or disabled. Available options: true, false
  DROPPED-ITEMS: true
  # The numerical value for Min Item Age Seconds. Dropped items younger than this are kept,
  # so items dropped just before a cleanup are not wiped. Set to 0 to disable the delay.
  # Available options: Any valid integer
  MIN-ITEM-AGE-SECONDS: 60
  # Configuration section for Excluded Worlds.
  EXCLUDED-WORLDS:
  - duels
  EXCLUDE-NAMED: true
  EXCLUDE-TAMED: true
  EXCLUDE-VILLAGERS: true
  # Configuration section for Excluded Entity Types. Entity types listed here are never
  # cleared, for example ALLAY or IRON_GOLEM.
  EXCLUDED-ENTITY-TYPES: []
  # Configuration section for Excluded Item Materials. Dropped items of these materials are
  # never cleared, for example NETHERITE_INGOT or ELYTRA.
  EXCLUDED-ITEM-MATERIALS: []
# Configuration section for Combat Manager.
```

---
