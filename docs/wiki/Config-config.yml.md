# Detailed Configuration & Setup Guide: `config.yml`

This is the official technical setup guide for `config.yml` in **UltimateDonutSMP**.
Each section details the exact commented setup code block, allowed option values, data types, default values, and in-depth functional behavior.

A line whose visible letters are all uppercase is shown in Title Case instead, so long as it also
carries a colour code or a placeholder. That reaches the tablist header and the `SERVER-LIST`
MOTD among others, and [FAQ entry 16](FAQ) explains the rule and how to keep your capitals.

---

## Section: `LANGUAGE`

### 1. Commented Setup Code Example

```yaml
LANGUAGE:
  # The language players see, taken from the matching file in the languages folder
  # Bundled locales: en_US, es_ES, id_ID, pt_BR, de_DE, fr_FR, ru_RU, zh_CN
  # Names such as Bahasa Indonesia, Spanish or pt-BR resolve to those, and a custom
  # file dropped into the languages folder can be selected by its file name
  # Available options: Any valid string text
  ACTIVE: en_US
  # Supplies any message the active language is missing
  # Available options: Any valid string text
  FALLBACK: en_US
# Configuration section for Locations.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `LANGUAGE.ACTIVE` | `str` | Any bundled locale, a locale alias, or the file name of a custom language file | `en_US` | Selects the language file under `languages/` that supplies player-facing text. Aliases such as `Bahasa Indonesia`, `Spanish` and `pt-BR` resolve to the bundled locales. Anything you have edited yourself in `messages.yml` still takes priority over the translation. |
| `LANGUAGE.FALLBACK` | `str` | Same values as `LANGUAGE.ACTIVE` | `en_US` | Supplies any key the active language file does not define, so a partial translation still shows text rather than a missing key. |

### 3. Practical Setup Example

```yaml
LANGUAGE:
  # The language players see, taken from the matching file in the languages folder
  # Bundled locales: en_US, es_ES, id_ID, pt_BR, de_DE, fr_FR, ru_RU, zh_CN
  # Names such as Bahasa Indonesia, Spanish or pt-BR resolve to those, and a custom
  # file dropped into the languages folder can be selected by its file name
  # Available options: Any valid string text
  ACTIVE: en_US
  # Supplies any message the active language is missing
  # Available options: Any valid string text
  FALLBACK: en_US
# Configuration section for Locations.
```

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
    - '&f<players> Players'
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
    - '&f<players> Players'
# Configuration section for Settings.
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
  # Configuration section for Logging. Writes chat into each player's own log, so staff can
  # read it back with /logs <player> or browse the whole server with /chatlog. None of these
  # switches change what players see in chat.
  LOGGING:
    # Master switch for chat logging. With this off, neither public nor private messages are
    # recorded, whatever the two switches below say. Available options: true, false
    ENABLED: true
    # Records normal public chat. Only messages that actually reach chat are stored, so muted,
    # filtered and rate-limited messages are left out. Available options: true, false
    PUBLIC-MESSAGES: true
    # Records private messages sent with /msg and /reply, on both sides of the conversation.
    # Available options: true, false
    PRIVATE-MESSAGES: true
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
| `CHAT.LOGGING.ENABLED` | `bool` | `true`, `false` | `true` | Master switch for chat logging. Turn it off and nothing a player types is written to their log, whichever of the two switches below are on. |
| `CHAT.LOGGING.PUBLIC-MESSAGES` | `bool` | `true`, `false` | `true` | Records normal public chat into the sender's log, readable with `/logs <player>` or `/chatlog`. Only messages that reach chat are stored, so muted, filtered and rate-limited ones are left out. |
| `CHAT.LOGGING.PRIVATE-MESSAGES` | `bool` | `true`, `false` | `true` | Records `/msg` and `/reply` conversations into both players' logs, readable with `/logs <player>`. |
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
  # Configuration section for Logging. Writes chat into each player's own log, so staff can
  # read it back with /logs <player> or browse the whole server with /chatlog. None of these
  # switches change what players see in chat.
  LOGGING:
    # Master switch for chat logging. With this off, neither public nor private messages are
    # recorded, whatever the two switches below say. Available options: true, false
    ENABLED: true
    # Records normal public chat. Only messages that actually reach chat are stored, so muted,
    # filtered and rate-limited messages are left out. Available options: true, false
    PUBLIC-MESSAGES: true
    # Records private messages sent with /msg and /reply, on both sides of the conversation.
    # Available options: true, false
    PRIVATE-MESSAGES: true
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

### 4. Colours In The Chat Format

`FORMAT` reads the same colour syntax as the rest of the plugin: `&a` codes, `&#RRGGBB` for a single
hex colour, and MiniMessage tags such as `<red>`, `<bold>`, `<gradient:#FF0000:#0000FF>` or
`<rainbow>`. A gradient may run across `%prefix%` and `%player%`, so

```yaml
CHAT:
  FORMAT: '<gradient:#FF0000:#0000FF>%prefix%%player%</gradient>&7: &f%message%'
```

fades the rank prefix and the name together from red to blue, and the hover stats stay attached to
the name. Drop the `</gradient>` and the fade carries on to the end of the line instead.

Whatever colour is active when the format reaches `%player%` carries onto the name, so a LuckPerms
prefix ending in `&c` gives you a red name unless the format sets a colour of its own after it.

`%message%` sits outside all of this on purpose. What a player types goes out as written, tinted by
`MESSAGE-COLORS` and nothing else, so colour codes someone types into chat stay visible as the
characters they are. Staff chat is assembled in one piece rather than around a clickable name, and
`STAFFCHAT.FORMAT` in `messages.yml` has always taken gradients the same way.

Private messages and staff chat part company with public chat on one point. Both colour the whole
line after the typed message has been put into it, so a code somebody typed would otherwise take
effect, and `ultimatedonutsmp.chat.color` decides whether it does. The node defaults to `op` and
ships inside the `ultimatedonutsmp.admin` and `ultimatedonutsmp.staff.mode` bundles, so staff keep
the colours they already had. For anyone without it the codes come out of the message before the
line is built, which stops a player hiding a private message behind `&k` or wiping the format's own
colours with `&r`. Grant the node to a rank to hand those colours back.

---

## Section: `SERVER-NOTIFICATIONS`

Server-wide announcement lines for joins, leaves, first joins, and the two marketplaces. Every
line ships switched off, so updating the jar never changes how an existing server's chat looks —
turn on the ones you want.

The join, leave and first-join lines can each read differently per rank. Put the permission a rank
carries under that line's `BY-PERMISSION` map with the wording it should get; the first entry a
player matches wins, so the highest rank goes first. A player matching nothing keeps the plain
`MESSAGE`, which is also what an empty map gives you.

### 1. Commented Setup Code Example

```yaml
SERVER-NOTIFICATIONS:
  # The line everyone sees when a player connects. While this is off the server's own join
  # message is relayed instead, exactly as it is today.
  JOIN:
    ENABLED: false
    MESSAGE: '&8[&a+&8] &a{player} &7joined the server.'
    # Per-rank wording. The first node the player holds wins, so list the highest rank first.
    BY-PERMISSION:
      "ultimatedonutsmp.notifications.join.vip++": '&8[&a+&8] &6{player} &7joined the server.'
      "ultimatedonutsmp.notifications.join.vip+": '&8[&a+&8] &b{player} &7joined the server.'
      "ultimatedonutsmp.notifications.join.vip": '&8[&a+&8] &e{player} &7joined the server.'
  LEAVE:
    ENABLED: false
    MESSAGE: '&8[&c-&8] &c{player} &7left the server.'
    BY-PERMISSION:
      "ultimatedonutsmp.notifications.leave.vip++": '&8[&c-&8] &6{player} &7left the server.'
      "ultimatedonutsmp.notifications.leave.vip+": '&8[&c-&8] &b{player} &7left the server.'
      "ultimatedonutsmp.notifications.leave.vip": '&8[&c-&8] &e{player} &7left the server.'
  # Sent in place of the join line the very first time a player ever connects.
  FIRST-JOIN:
    ENABLED: false
    MESSAGE: '&aWelcome &e{player} &ato the server for the first time!'
    BY-PERMISSION:
      "ultimatedonutsmp.notifications.first-join.vip++": '&aWelcome &6{player} &ato the server for the first time!'
      "ultimatedonutsmp.notifications.first-join.vip+": '&aWelcome &b{player} &ato the server for the first time!'
      "ultimatedonutsmp.notifications.first-join.vip": '&aWelcome &e{player} &ato the server for the first time!'
  AUCTION-HOUSE:
    ENABLED: true
    LISTING:
      ENABLED: false
      MESSAGE: '&8[&6AH&8] &f{player} &7listed &e{amount}x {item} &7for &a{price_formatted}&7.'
    PURCHASE:
      ENABLED: false
      MESSAGE: '&8[&6AH&8] &f{player} &7bought &e{amount}x {item} &7for &a{price_formatted}&7.'
  ORDERS:
    ENABLED: true
    CREATE:
      ENABLED: false
      MESSAGE: '&8[&6ORDER&8] &f{player} &7created an order for &e{amount}x {item} &7at &a{price_formatted} &7each.'
    COMPLETE:
      ENABLED: false
      MESSAGE: '&8[&6ORDER&8] &f{player} &7completed &e{owner}&7''s order for &e{amount}x {item}&7.'
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `SERVER-NOTIFICATIONS.JOIN.ENABLED` | `bool` | `true`, `false` | `false` | `true` replaces the server's own join line with `JOIN.MESSAGE`. `false` relays the server's line unchanged. |
| `SERVER-NOTIFICATIONS.JOIN.MESSAGE` | `str` | Any string text | `'&8[&a+&8] &a{player} &7joined the server.'` | Supports `{player}`. Used for any player no `JOIN.BY-PERMISSION` entry applies to. |
| `SERVER-NOTIFICATIONS.JOIN.BY-PERMISSION` | `map` | Permission node to wording | Three `vip` examples | Per-rank join wording. Supports `{player}` like `MESSAGE`. The first node the player holds wins, so list the highest rank first. Matching is on the exact node, so a wildcard such as `ultimatedonutsmp.*` does not pick these up. Deleted entries are never merged back. |
| `SERVER-NOTIFICATIONS.LEAVE.ENABLED` | `bool` | `true`, `false` | `false` | `true` replaces the server's own quit line with `LEAVE.MESSAGE`. |
| `SERVER-NOTIFICATIONS.LEAVE.MESSAGE` | `str` | Any string text | `'&8[&c-&8] &c{player} &7left the server.'` | Supports `{player}`. Used for any player no `LEAVE.BY-PERMISSION` entry applies to. |
| `SERVER-NOTIFICATIONS.LEAVE.BY-PERMISSION` | `map` | Permission node to wording | Three `vip` examples | Per-rank leave wording, resolved exactly as `JOIN.BY-PERMISSION` is. |
| `SERVER-NOTIFICATIONS.FIRST-JOIN.ENABLED` | `bool` | `true`, `false` | `false` | `true` sends `FIRST-JOIN.MESSAGE` instead of the join line the first time a player ever connects, so nobody is announced twice. It works on its own — `JOIN` does not have to be on. |
| `SERVER-NOTIFICATIONS.FIRST-JOIN.MESSAGE` | `str` | Any string text | `'&aWelcome &e{player} &ato the server for the first time!'` | Supports `{player}`. Used for any player no `FIRST-JOIN.BY-PERMISSION` entry applies to. |
| `SERVER-NOTIFICATIONS.FIRST-JOIN.BY-PERMISSION` | `map` | Permission node to wording | Three `vip` examples | Per-rank first-join wording, resolved exactly as `JOIN.BY-PERMISSION` is. |
| `SERVER-NOTIFICATIONS.AUCTION-HOUSE.ENABLED` | `bool` | `true`, `false` | `true` | Master switch for both Auction House lines. Turning it off silences them whatever `LISTING` and `PURCHASE` say. |
| `SERVER-NOTIFICATIONS.AUCTION-HOUSE.LISTING.ENABLED` | `bool` | `true`, `false` | `false` | Announces every item a player puts up for sale. Bot listings are never announced. |
| `SERVER-NOTIFICATIONS.AUCTION-HOUSE.LISTING.MESSAGE` | `str` | Any string text | `'&8[&6AH&8] &f{player} &7listed &e{amount}x {item} &7for &a{price_formatted}&7.'` | Supports `{player}`, `{item}`, `{amount}`, `{price}`, `{price_formatted}` and `{category}`. |
| `SERVER-NOTIFICATIONS.AUCTION-HOUSE.PURCHASE.ENABLED` | `bool` | `true`, `false` | `false` | Announces every completed purchase. |
| `SERVER-NOTIFICATIONS.AUCTION-HOUSE.PURCHASE.MESSAGE` | `str` | Any string text | `'&8[&6AH&8] &f{player} &7bought &e{amount}x {item} &7for &a{price_formatted}&7.'` | Supports `{player}` (the buyer), `{seller}`, `{item}`, `{amount}`, `{price}` and `{price_formatted}`. |
| `SERVER-NOTIFICATIONS.ORDERS.ENABLED` | `bool` | `true`, `false` | `true` | Master switch for both Order lines. |
| `SERVER-NOTIFICATIONS.ORDERS.CREATE.ENABLED` | `bool` | `true`, `false` | `false` | Announces every new order a player opens. Bot orders are never announced. |
| `SERVER-NOTIFICATIONS.ORDERS.CREATE.MESSAGE` | `str` | Any string text | `'&8[&6ORDER&8] &f{player} &7created an order for &e{amount}x {item} &7at &a{price_formatted} &7each.'` | Supports `{player}`, `{item}`, `{amount}`, `{price}`, `{price_formatted}`, `{total}` and `{total_formatted}`. |
| `SERVER-NOTIFICATIONS.ORDERS.COMPLETE.ENABLED` | `bool` | `true`, `false` | `false` | Announces an order once it has been filled all the way. Partial deliveries stay quiet. |
| `SERVER-NOTIFICATIONS.ORDERS.COMPLETE.MESSAGE` | `str` | Any string text | `'&8[&6ORDER&8] &f{player} &7completed &e{owner}&7''s order for &e{amount}x {item}&7.'` | Supports `{player}` (whoever handed over the last of it), `{owner}`, `{item}`, `{amount}`, `{price}`, `{price_formatted}`, `{total}` and `{total_formatted}`. |

### 3. Practical Setup Example

```yaml
SERVER-NOTIFICATIONS:
  JOIN:
    ENABLED: true
    MESSAGE: '&#57F287+ &f{player}'
    # Donors get announced in their rank colour, everyone else gets the plain line above.
    # Highest rank first: the first node the player holds is the one that wins.
    BY-PERMISSION:
      "ultimatedonutsmp.notifications.join.mvp": '&#FEE75C✦ &e{player}'
      "ultimatedonutsmp.notifications.join.vip": '&#57F287+ &a{player}'
  LEAVE:
    ENABLED: true
    MESSAGE: '&#ED4245- &f{player}'
  FIRST-JOIN:
    ENABLED: true
    MESSAGE: '&#FEE75C&l✦ &fwelcome &e{player} &fto the server!'
  AUCTION-HOUSE:
    ENABLED: true
    LISTING:
      ENABLED: true
      MESSAGE: '&8[&6AH&8] &f{player} &7listed &e{amount}x {item} &7for &a{price_formatted}&7.'
    PURCHASE:
      ENABLED: false
  ORDERS:
    ENABLED: true
    CREATE:
      ENABLED: true
    COMPLETE:
      ENABLED: true
```

### 4. Colours And Who Sees Them

Messages here take the same colour codes as the rest of the plugin: `&a` style codes, `&#RRGGBB`
for one hex colour, and `<#RRGGBB>text</#RRGGBB>` for a gradient. MiniMessage tags work as well,
so `<red>`, `<bold>`, `<gradient:#FF0000:#0000FF>` and `<rainbow>` all render, and nothing stops
you mixing them with the `&` codes on the same line. Placeholders are filled in
before the colours are applied, and PlaceholderAPI placeholders resolve too when it is installed.

Two per-player switches under `/settings` still apply on top of everything configured here. Join,
leave and first-join lines follow **Join/Leave Messages**, which is the same option that already
governs the server's own join and quit text and is limited to people the player follows. The Auction House
and Order lines follow **Server Broadcasts**, plus **Auction Alerts** and **Order Alerts**
respectively, so a player who muted one marketplace does not get its announcements back through
this system.

One thing the config cannot override: if another plugin suppresses a join or quit message
entirely, nothing is announced for that player. The configured line replaces the server's own
message rather than being sent alongside it, so there is nothing to send when the server had no
message to begin with.

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
  # Determines whether the worth line is shown at all. Turning this off takes the line away
  # from everyone, whatever players picked under /settings > Worth Display.
  # Available options: true, false
  ENABLED: true
  # The text or value for Format. Available options: Any valid string text
  FORMAT: '&7Worth: &a$%price%'
# Configuration section for End Crystal.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `WORTH-LORE.ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for the worth lore line. `false` hides it from every player, overriding whatever each of them picked under `/settings` > Worth Display. Takes effect on `/uds reload`. |
| `WORTH-LORE.FORMAT` | `str` | Any string text | `'&7Worth: &a$%price%'` | Configures the technical `FORMAT` parameter for `WORTH-LORE.FORMAT` in `config.yml`. |

### 3. Practical Setup Example

```yaml
WORTH-LORE:
  # Determines whether the worth line is shown at all. Turning this off takes the line away
  # from everyone, whatever players picked under /settings > Worth Display.
  # Available options: true, false
  ENABLED: true
  # The text or value for Format. Available options: Any valid string text
  FORMAT: '&7Worth: &a$%price%'
# Configuration section for End Crystal.
```

---

## Section: `MONEY-NAMETAGS`

### 1. Commented Setup Code Example

```yaml
MONEY-NAMETAGS:
  # Determines whether Money Nametags is enabled or disabled. Turning this off takes the
  # line away from everyone, whatever players picked in /settings.
  # Available options: true, false
  ENABLED: true
  # The text or value for Format. Supports {balance} and PlaceholderAPI placeholders.
  # Available options: Any valid string text
  FORMAT: '&a$ &f{balance}'
  # Determines whether balances are shortened to 1.1K, 1.1M, 1.1B and so on instead of
  # being written out as 1,100,000. Available options: true, false
  SHORT-FORMAT: true
  # How quickly a balance change shows up on the line, in ticks. Where the line sits is the
  # client's business, so this only decides how fresh the number is.
  # Available options: 1 to 40
  UPDATE-INTERVAL-TICKS: 10
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `MONEY-NAMETAGS.ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `MONEY-NAMETAGS` system. Set to `false` to take the option out of the game entirely, whatever players picked in `/settings`. |
| `MONEY-NAMETAGS.FORMAT` | `str` | Any string text | `'&a$ &f{balance}'` | The line drawn under the username. `{balance}` is replaced with the player's balance, and PlaceholderAPI placeholders are resolved against the player who owns the line. |
| `MONEY-NAMETAGS.SHORT-FORMAT` | `bool` | `true`, `false` | `true` | `true` writes `1.1K`, `1.1M`, `1.1B`, `1.1T` and `1.1Q` where `false` writes them out in full as `1,100,000`. Leaving it on keeps the line narrow once balances run into the billions. |
| `MONEY-NAMETAGS.UPDATE-INTERVAL-TICKS` | `int` | `1` to `40` | `10` | How often balances are re-read and sent out. It has nothing to do with where the line sits or how it follows a player, since the client draws it as part of the nametag. Lower it if you want balance changes to appear faster. |

### 3. Practical Setup Example

```yaml
MONEY-NAMETAGS:
  ENABLED: true
  FORMAT: '&6&l${balance}'
  SHORT-FORMAT: true
  UPDATE-INTERVAL-TICKS: 4
```

### 4. Where The Line Sits

The username is never touched, moved or hidden. The balance goes in the scoreboard slot Minecraft
reserves for a line under a username, the same slot a health display would use, so the client draws
it itself directly beneath the name. It cannot drift away from the player, lag behind a sprint or
land on top of the name, because it is drawn in the same pass as the name itself.

Two rules come with that slot and belong to the client rather than to this plugin:

- **The line only appears within about ten blocks.** Further out the username still shows and the
  balance does not. There is no setting for this; the distance is baked into Minecraft.
- **Only one objective can hold that slot at a time.** Any other plugin using the below-name slot,
  a health display for instance, will fight over it for players who switched money nametags on.

The slot normally draws a raw score, which is an integer and no use for a balance in the billions,
so each score carries a fixed number format holding the finished text instead. That needs Minecraft
1.20.3 or newer; on anything older the feature logs a warning once and stays off rather than
printing a wrong number.

### 5. Who Sees The Line

Every player carries their own switch under `/settings > Money Nametags`, and it starts turned off.
It only decides what that player sees under other people, never whether their own balance is on
show, so nobody can hide their balance by turning the option off. Nothing is written to anybody's
real scoreboard either: the objective is sent straight to the players who asked for it, so a player
who left the setting off never hears about it. A player who has hidden their identity through
`/hide` never gets a line.

Admins who want the option gone can either set `ENABLED: false` here or drop
`SETTINGS-MENU.BUTTONS.MONEY_NAMETAGS.ENABLED: false` into `menus.yml`; the second one also takes
the button out of `/settings`. `DEFAULT: true` on that same button turns the line on for everybody
who has never touched the setting.

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

---

## Section: `RESPAWN-ANCHOR`

### 1. Commented Setup Code Example

```yaml
RESPAWN-ANCHOR:
  # Determines whether Enabled is enabled or disabled. Available options: true, false
  ENABLED: false
  # The decimal value for Damage. Available options: Any decimal number
  DAMAGE: 2.0
# Configuration section for Boss Sounds.
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
# Configuration section for Boss Sounds.
```

---

## Section: `BOSS-SOUNDS`

### 1. Commented Setup Code Example

```yaml
BOSS-SOUNDS:
  # Determines whether Enabled is enabled or disabled. When true, the two boss sounds Minecraft
  # plays to everyone online are kept to players within RADIUS blocks of the boss. Set it to false
  # to leave the vanilla behaviour alone. Available options: true, false
  ENABLED: true
  # The numerical value for Radius. How far the sound carries, in blocks, measured from the boss to
  # the player. 1600 is 100 chunks. Players in another world never hear it. A value of 0 or less
  # turns the limit off. Available options: Any valid integer
  RADIUS: 1600
  # Determines whether Wither Spawn is enabled or disabled. Covers the roar a wither makes once it
  # finishes charging up. Available options: true, false
  WITHER-SPAWN: true
  # Determines whether Ender Dragon Death is enabled or disabled. Covers the growl an ender dragon
  # makes as it starts dying. Available options: true, false
  ENDER-DRAGON-DEATH: true
# Configuration section for Ender Chest.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `BOSS-SOUNDS.ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `BOSS-SOUNDS` system. Set to `true` to enable, `false` to disable. With it off, Minecraft plays both sounds to every player online, wherever they are. |
| `BOSS-SOUNDS.RADIUS` | `int` | Any valid integer | `'1600'` | How far the sound carries, in blocks, measured from the boss to the player. `1600` is 100 chunks. Height counts towards the distance, and a player in another world never hears it. `0` or less turns the limit off and leaves both sounds server-wide. |
| `BOSS-SOUNDS.WITHER-SPAWN` | `bool` | `true`, `false` | `true` | Whether the radius applies to the roar a wither makes once it finishes charging up. |
| `BOSS-SOUNDS.ENDER-DRAGON-DEATH` | `bool` | `true`, `false` | `true` | Whether the radius applies to the growl an ender dragon makes as it starts dying. |

These are the only two sounds Minecraft plays to the whole server. A wither dying and a dragon
spawning already fade out over distance the way any other mob sound does, so there is nothing to
limit there. Trimming the range needs ProtocolLib, which the plugin already requires.

### 3. Practical Setup Example

```yaml
BOSS-SOUNDS:
  ENABLED: true
  # 320 blocks, so the roar stays inside the fight it belongs to
  RADIUS: 320
  WITHER-SPAWN: true
  # leave the dragon growl reaching the whole server, the way vanilla plays it
  ENDER-DRAGON-DEATH: false
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

---

## Section: `SHARDS`

### 1. Commented Setup Code Example

```yaml
SHARDS:
  # EVERY down to CANCELLED-MESSAGE are a fallback for servers that have no shard region
  # at all. They are read only when CUBOIDS.REGIONS below is empty, and this file ships a
  # 'spawn' region, so on a normal install they do nothing. The settings that actually run
  # are the ones inside CUBOIDS.REGIONS.<region>. RESET-ON-LEAVE is not part of this group:
  # it stays live as the default for the matching per-region key.
  # Minutes between rewards in the fallback zone. Watch the unit, because the per-region
  # INTERVAL is in seconds and this one is multiplied by 60.
  EVERY: 1
  # Shards paid out each time the fallback timer runs down.
  AMOUNT: 1
  # Countdown shown on the action bar in the fallback zone. %time% is the time remaining,
  # %seconds% the same thing as a plain number.
  COUNTDOWN: '&7Next shard in &#A303F9%time%'
  # Shown when a fallback reward lands. %amount% is the payout and %total% the new balance.
  RECEIVED: '&#A303F9You received %amount% Shard &7(Total: &#A303F9%total%&7)'
  # Replaces RECEIVED while a shard booster is running. %multiplier% is the boost.
  RECEIVED-BOOSTED: '&#A303F9You received %amount% Shards &7(&ax%multiplier%&7) &7(Total:
    &#A303F9%total%&7)'
  # Shown when a player leaves the fallback zone before the timer finishes. %cuboid% is the
  # zone name. There is no %total% here, since nothing was paid out.
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
        # Radius in blocks for the point-based fallbacks. LOCATION gets a sphere this wide
        # that pays shards alongside CUBOID, and the AFK point gets one that counts as the
        # AFK area when no AFK cuboid is bound. One number covers both. Set 0 to switch the
        # shard sphere off and leave the AFK sphere at its built-in 16.
        RADIUS: 16
        # The numerical value for Interval. Available options: Any valid integer
        INTERVAL: 60
        # The numerical value for Amount. Available options: Any valid integer
        AMOUNT: 1
        # The text or value for Countdown Message. Available options: Any valid string text
        COUNTDOWN-MESSAGE: '&7Next shard in &#A303F9%time%'
        # The text or value for Reward Message. Available options: Any valid string text
        REWARD-MESSAGE: '&#A303F9You received %amount% Shard &7(Total: &#A303F9%total%&7)'
        # The text or value for Boosted Reward Message. Available options: Any valid string text
        BOOSTED-REWARD-MESSAGE: '&#A303F9You received %amount% Shards &7(&ax%multiplier%&7) &7(Total: &#A303F9%total%&7)'
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
| `SHARDS.EVERY` | `int` | Any valid integer number | `'1'` | Fallback only, read when `SHARDS.CUBOIDS.REGIONS` has no entries. Minutes between rewards in the fallback zone; the per-region `INTERVAL` is in seconds instead. |
| `SHARDS.AMOUNT` | `int` | Any valid integer number | `'1'` | Fallback only. Shards paid out each time the fallback timer runs down. The setting that runs on a normal install is `SHARDS.CUBOIDS.REGIONS.<region>.AMOUNT`. |
| `SHARDS.COUNTDOWN` | `str` | Any string text | `'&7Next shard in &#A303F9%time%'` | Fallback only. Action bar countdown in the fallback zone, supporting `%time%` and `%seconds%`. |
| `SHARDS.RECEIVED` | `str` | Any string text | `'&#A303F9You received %amount% Shard...'` | Fallback only. Shown when a fallback reward lands, supporting `%amount%` and `%total%`. |
| `SHARDS.RECEIVED-BOOSTED` | `str` | Any string text | `'&#A303F9You received %amount% Shard...'` | Fallback only. Replaces `SHARDS.RECEIVED` while a shard booster is running, adding `%multiplier%`. |
| `SHARDS.CANCELLED-MESSAGE` | `str` | Any string text | `'&cShard reward cancelled &7(Left %c...'` | Fallback only. Shown when a player leaves the fallback zone before the timer finishes, supporting `%cuboid%`. |
| `SHARDS.RESET-ON-LEAVE` | `bool` | `true`, `false` | `true` | Configures the technical `RESET-ON-LEAVE` parameter for `SHARDS.RESET-ON-LEAVE` in `config.yml`. |
| `SHARDS.CUBOIDS.REGIONS.spawn.ENABLED` | `bool` | `true`, `false` | `false` | Global toggle for `SHARDS` system. Set to `true` to enable, `false` to disable. |
| `SHARDS.CUBOIDS.REGIONS.spawn.BOUND` | `bool` | `true`, `false` | `false` | Configures the technical `BOUND` parameter for `SHARDS.CUBOIDS.REGIONS.spawn.BOUND` in `config.yml`. |
| `SHARDS.CUBOIDS.REGIONS.spawn.PRIORITY` | `int` | Any valid integer number | `'100'` | Configures the technical `PRIORITY` parameter for `SHARDS.CUBOIDS.REGIONS.spawn.PRIORITY` in `config.yml`. |
| `SHARDS.CUBOIDS.REGIONS.spawn.CUBOID` | `str` | Any string text | `''` | Configures the technical `CUBOID` parameter for `SHARDS.CUBOIDS.REGIONS.spawn.CUBOID` in `config.yml`. |
| `SHARDS.CUBOIDS.REGIONS.spawn.WORLD` | `str` | Any string text | `'world'` | Configures the technical `WORLD` parameter for `SHARDS.CUBOIDS.REGIONS.spawn.WORLD` in `config.yml`. |
| `SHARDS.CUBOIDS.REGIONS.spawn.RADIUS` | `int` | Any valid integer number | `'16'` | Radius in blocks for the point-based fallbacks. `LOCATION` gets a sphere this wide that pays shards alongside `CUBOID`, and the AFK point gets one that counts as the AFK area when no AFK cuboid is bound. One number covers both. Set `0` to switch the shard sphere off and leave the AFK sphere at its built-in 16. |
| `SHARDS.CUBOIDS.REGIONS.spawn.INTERVAL` | `int` | Any valid integer number | `'60'` | Configures the technical `INTERVAL` parameter for `SHARDS.CUBOIDS.REGIONS.spawn.INTERVAL` in `config.yml`. |
| `SHARDS.CUBOIDS.REGIONS.spawn.AMOUNT` | `int` | Any valid integer number | `'1'` | Configures the technical `AMOUNT` parameter for `SHARDS.CUBOIDS.REGIONS.spawn.AMOUNT` in `config.yml`. |
| `SHARDS.CUBOIDS.REGIONS.spawn.COUNTDOWN-MESSAGE` | `str` | Any string text | `'&7Next shard in &#A303F9%time%'` | Configures the technical `COUNTDOWN-MESSAGE` parameter for `SHARDS.CUBOIDS.REGIONS.spawn.COUNTDOWN-MESSAGE` in `config.yml`. |
| `SHARDS.CUBOIDS.REGIONS.spawn.REWARD-MESSAGE` | `str` | Any string text | `'&#A303F9You received %amount% Shard...'` | Configures the technical `REWARD-MESSAGE` parameter for `SHARDS.CUBOIDS.REGIONS.spawn.REWARD-MESSAGE` in `config.yml`. |
| `SHARDS.CUBOIDS.REGIONS.spawn.BOOSTED-REWARD-MESSAGE` | `str` | Any string text | `'&#A303F9You received %amount% Shard...'` | Configures the technical `BOOSTED-REWARD-MESSAGE` parameter for `SHARDS.CUBOIDS.REGIONS.spawn.BOOSTED-REWARD-MESSAGE` in `config.yml`. |
| `SHARDS.CUBOIDS.REGIONS.spawn.LEAVE-MESSAGE` | `str` | Any string text | `'&cShard reward cancelled &7(Left %c...'` | Configures the technical `LEAVE-MESSAGE` parameter for `SHARDS.CUBOIDS.REGIONS.spawn.LEAVE-MESSAGE` in `config.yml`. |
| `SHARDS.CUBOIDS.REGIONS.spawn.AFK-TIME` | `int` | Any valid integer number | `'120'` | Seconds a player can stand still inside this region before it moves them to the AFK area. Only used when `TELEPORT-ON-AFK` is `true`. |
| `SHARDS.CUBOIDS.REGIONS.spawn.AFK-CUBOID` | `str` | Any string text | `''` | Cuboid this region sends idle players to. Leave empty to use the AFK area from `AFK-SYSTEM`. |
| `SHARDS.CUBOIDS.REGIONS.spawn.AFK-LOCATION` | `str` | Any string text | `''` | Exact destination for idle players, which takes priority over `AFK-CUBOID` when set. |
| `SHARDS.CUBOIDS.REGIONS.spawn.AFK-MESSAGE` | `str` | Any string text | `'&7You have been moved to the AFK ar...'` | Message sent after this region moves a player to the AFK area. |
| `SHARDS.CUBOIDS.REGIONS.spawn.TELEPORT-ON-AFK` | `bool` | `true`, `false` | `true` | Whether idle players inside this region are moved to the AFK area. Set to `false` to leave them where they are, in which case they keep their spot and only stop earning shards. |
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
  # EVERY down to CANCELLED-MESSAGE are a fallback for servers that have no shard region
  # at all. They are read only when CUBOIDS.REGIONS below is empty, and this file ships a
  # 'spawn' region, so on a normal install they do nothing. The settings that actually run
  # are the ones inside CUBOIDS.REGIONS.<region>. RESET-ON-LEAVE is not part of this group:
  # it stays live as the default for the matching per-region key.
  # Minutes between rewards in the fallback zone. Watch the unit, because the per-region
  # INTERVAL is in seconds and this one is multiplied by 60.
  EVERY: 1
  # Shards paid out each time the fallback timer runs down.
  AMOUNT: 1
  # Countdown shown on the action bar in the fallback zone. %time% is the time remaining,
  # %seconds% the same thing as a plain number.
  COUNTDOWN: '&7Next shard in &#A303F9%time%'
  # Shown when a fallback reward lands. %amount% is the payout and %total% the new balance.
  RECEIVED: '&#A303F9You received %amount% Shard &7(Total: &#A303F9%total%&7)'
  # Replaces RECEIVED while a shard booster is running. %multiplier% is the boost.
  RECEIVED-BOOSTED: '&#A303F9You received %amount% Shards &7(&ax%multiplier%&7) &7(Total:
    &#A303F9%total%&7)'
  # Shown when a player leaves the fallback zone before the timer finishes. %cuboid% is the
  # zone name. There is no %total% here, since nothing was paid out.
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
    MESSAGE:
    - ''
    - '&#00A4FCKey-All reward!'
    - '&fYou received &b{amount}x {crate}&f key.'
    - ''
# Configuration section for Team.
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

---

## Section: `SERVER-LIST`

### 1. Commented Setup Code Example

```yaml
# Configuration section for Server List.
SERVER-LIST:
  # Determines whether Enabled is enabled or disabled. When true, the text under this server's name
  # in the multiplayer list is taken from MOTD below instead of the motd line in server.properties.
  # Set it to false to leave that entry alone. Available options: true, false
  ENABLED: false
  # Configuration section for Motd. The lines shown under the server name, written in the same
  # colour codes as the rest of the plugin. The client only draws the first two. %online% becomes
  # the number of players on the server and %max_players% the slot count, the same two tokens the
  # tablist header takes. Maintenance mode keeps its own text while it is on, set in network.yml
  MOTD:
  - '&d&lUltimateDonutSMP'
  - '&7%online%&8/&7%max_players% &7online'
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `SERVER-LIST.ENABLED` | `bool` | `true`, `false` | `false` | Global toggle for `SERVER-LIST` system. Set to `true` to enable, `false` to disable. It ships off, so updating the jar leaves the entry reading exactly as it did before. |
| `SERVER-LIST.MOTD` | `list` | Any lines of text | `['&d&lUltimateDonutSMP', '&7%online%&8/&7%max_players% &7online']` | The lines drawn under the server name. The client shows the first two and ignores anything after them, so a longer list costs nothing and gains nothing. `%online%` becomes the number of players on the server and `%max_players%` the slot count. Colour codes, hex codes and PlaceholderAPI server placeholders all work in these lines. An empty list leaves the `motd` line from `server.properties` where it is. |

Nothing here rewrites `server.properties`, and that file still answers for the server whenever this
block is off. What the block adds is two lines instead of one, the `&` colours used everywhere else
in the plugin, counts that move as players come and go, and `/uds reload` picking up an edit without
a restart.

Maintenance mode dresses the same entry. While `/maintenance on` is running and
`MAINTENANCE.SERVER_LIST.ENABLED` in `network.yml` is `true`, the maintenance text is what people
see and this block waits its turn. Switch that maintenance block off and the server keeps this MOTD
even while it is closed, so a maintenance window is not announced to everyone browsing the list.

### 3. Practical Setup Example

```yaml
SERVER-LIST:
  ENABLED: true
  MOTD:
  # the first line carries the name, the second tells people how busy it is
  - '&d&lDonut &f&lSMP &7[1.21]'
  - '&a%online%&7 of &a%max_players%&7 playing right now'
```

---

## Section: `OPTIMIZATION`

### 1. Commented Setup Code Example

```yaml
OPTIMIZATION:
  # Determines whether Enabled is enabled or disabled. Available options: true, false
  ENABLED: true
  # The numerical value for Monitor Interval Ticks. Available options: Any valid integer
  MONITOR-INTERVAL-TICKS: 100
  # The decimal value for Tps Warn Threshold. Available options: Any decimal number
  TPS-WARN-THRESHOLD: 18.5
  # The decimal value for Tps Critical Threshold. Available options: Any decimal number
  TPS-CRITICAL-THRESHOLD: 16.0
  # The decimal value for Mspt Warn Threshold. Available options: Any decimal number
  MSPT-WARN-THRESHOLD: 45.0
  # The decimal value for Mspt Critical Threshold. Available options: Any decimal number
  MSPT-CRITICAL-THRESHOLD: 55.0
  # The numerical value for Recovery Samples. Available options: Any valid integer
  RECOVERY-SAMPLES: 3
  # Determines whether Log State Changes is enabled or disabled. Available options: true, false
  LOG-STATE-CHANGES: true
  # Configuration section for Adaptive Tasks. Each entry below decides how often one task is
  # allowed to run while the server is struggling. None of them switch a feature on or off; the
  # switches for that live in their own sections, either at the top level of this file or in the
  # file named after the feature.
  ADAPTIVE-TASKS:
    # Configuration section for Scoreboard.
    SCOREBOARD:
      # Whether the scoreboard task may be slowed down while the server is under load. Setting this
      # to false removes the slowdown and lets the task keep running at full rate; it does not turn
      # the scoreboard off. That switch is SCOREBOARD.ENABLED in scoreboard.yml.
      # Available options: true, false
      ENABLED: true
      # The numerical value for Warn Min Interval Ticks. Available options: Any valid integer
      WARN-MIN-INTERVAL-TICKS: 4
      # The numerical value for Critical Min Interval Ticks. Available options: Any valid integer
      CRITICAL-MIN-INTERVAL-TICKS: 10
    # Configuration section for Tablist.
    TABLIST:
      # Whether the tablist task may be slowed down while the server is under load. Setting this to
      # false removes the slowdown and lets the task keep running at full rate; it does not turn the
      # tablist off. That switch is the TABLIST section near the top of this file.
      # Available options: true, false
      ENABLED: true
      # The numerical value for Warn Min Interval Ticks. Available options: Any valid integer
      WARN-MIN-INTERVAL-TICKS: 80
      # The numerical value for Critical Min Interval Ticks. Available options: Any valid integer
      CRITICAL-MIN-INTERVAL-TICKS: 140
    # Configuration section for Lunar Teammates.
    LUNAR-TEAMMATES:
      # Whether the lunar teammates task may be slowed down while the server is under load. Setting
      # this to false removes the slowdown and lets the task keep running at full rate; it does not
      # turn the lunar teammate overlay off.
      # Available options: true, false
      ENABLED: true
      # The numerical value for Warn Min Interval Ticks. Available options: Any valid integer
      WARN-MIN-INTERVAL-TICKS: 40
      # The numerical value for Critical Min Interval Ticks. Available options: Any valid integer
      CRITICAL-MIN-INTERVAL-TICKS: 100
# Configuration section for Clear Lag.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `OPTIMIZATION.ENABLED` | `bool` | `true`, `false` | `true` | Whether the load monitor runs. With it off nothing is throttled and every task below keeps its normal rate no matter what the server is doing. |
| `OPTIMIZATION.MONITOR-INTERVAL-TICKS` | `int` | `20` or more | `100` | How often TPS and MSPT are sampled. Anything under `20` is raised to `20`, so it checks at most once a second. |
| `OPTIMIZATION.TPS-WARN-THRESHOLD` | `float` | Any decimal number | `18.5` | TPS below this moves the server into the warning state. |
| `OPTIMIZATION.TPS-CRITICAL-THRESHOLD` | `float` | Any decimal number | `16.0` | TPS below this goes straight to critical. |
| `OPTIMIZATION.MSPT-WARN-THRESHOLD` | `float` | Any decimal number | `45.0` | Milliseconds per tick above this moves the server into the warning state. TPS and MSPT are judged separately and the worse of the two decides. |
| `OPTIMIZATION.MSPT-CRITICAL-THRESHOLD` | `float` | Any decimal number | `55.0` | Milliseconds per tick above this goes to critical. |
| `OPTIMIZATION.RECOVERY-SAMPLES` | `int` | `1` or more | `3` | How many calm samples in a row are needed before the state drops back down. Climbing happens on the first bad sample; only the way down waits. At the default that is three quiet checks, which stops a server hovering on the threshold from flipping the throttle on and off. |
| `OPTIMIZATION.LOG-STATE-CHANGES` | `bool` | `true`, `false` | `true` | Writes a console line on each state change, quoting the TPS and MSPT that triggered it. |
| `OPTIMIZATION.ADAPTIVE-TASKS.SCOREBOARD.ENABLED` | `bool` | `true`, `false` | `true` | Whether the scoreboard task may be slowed down while the server is struggling. Setting it to `false` removes the slowdown only; the scoreboard itself is switched off with `SCOREBOARD.ENABLED` in `scoreboard.yml`. |
| `OPTIMIZATION.ADAPTIVE-TASKS.SCOREBOARD.WARN-MIN-INTERVAL-TICKS` | `int` | Any valid integer | `4` | Smallest gap allowed between runs of that task while the server sits in the warning state. |
| `OPTIMIZATION.ADAPTIVE-TASKS.SCOREBOARD.CRITICAL-MIN-INTERVAL-TICKS` | `int` | Any valid integer | `10` | The same floor for the critical state. |
| `OPTIMIZATION.ADAPTIVE-TASKS.TABLIST.ENABLED` | `bool` | `true`, `false` | `true` | Whether the tablist task may be slowed down while the server is struggling. Setting it to `false` removes the slowdown only; the tablist itself is switched off in the `TABLIST` section of this file. |
| `OPTIMIZATION.ADAPTIVE-TASKS.TABLIST.WARN-MIN-INTERVAL-TICKS` | `int` | Any valid integer | `80` | Smallest gap allowed between runs of that task while the server sits in the warning state. |
| `OPTIMIZATION.ADAPTIVE-TASKS.TABLIST.CRITICAL-MIN-INTERVAL-TICKS` | `int` | Any valid integer | `140` | The same floor for the critical state. |
| `OPTIMIZATION.ADAPTIVE-TASKS.LUNAR-TEAMMATES.ENABLED` | `bool` | `true`, `false` | `true` | Whether the lunar teammates task may be slowed down while the server is struggling. Setting it to `false` removes the slowdown only; the teammate overlay itself stays on. |
| `OPTIMIZATION.ADAPTIVE-TASKS.LUNAR-TEAMMATES.WARN-MIN-INTERVAL-TICKS` | `int` | Any valid integer | `40` | Smallest gap allowed between runs of that task while the server sits in the warning state. |
| `OPTIMIZATION.ADAPTIVE-TASKS.LUNAR-TEAMMATES.CRITICAL-MIN-INTERVAL-TICKS` | `int` | Any valid integer | `100` | The same floor for the critical state. |

### 3. Practical Setup Example

```yaml
OPTIMIZATION:
  ENABLED: true
  # sample twice as often, and give ground sooner on a busy server
  MONITOR-INTERVAL-TICKS: 50
  TPS-WARN-THRESHOLD: 19.0
  TPS-CRITICAL-THRESHOLD: 17.0
  # wait longer before easing off, so the throttle stops flapping
  RECOVERY-SAMPLES: 6
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

## Section: `COMBAT-MANAGER`

### 1. Commented Setup Code Example

```yaml
COMBAT-MANAGER:
  # Determines whether Enabled is enabled or disabled. Available options: true, false
  ENABLED: true
  # The numerical value for Cooldown. Available options: Any valid integer
  COOLDOWN: 16
  # Determines whether Kill On Logout is enabled or disabled. When true, a player who
  # disconnects while their combat tag is still running is killed, so logging out mid-fight
  # is not a way to escape a fight. Available options: true, false
  KILL-ON-LOGOUT: false
  # The text or value for Action Bar. Available options: Any valid string text
  ACTION-BAR: '&fCombat: &b${time}s'
  # Determines whether Mobs is enabled or disabled. When true, damage from mobs also puts
  # players into combat, not just damage dealt by other players. Available options: true, false
  MOBS: false
  # Determines whether Ender Crystal is enabled or disabled. Available options: true, false
  ENDER-CRYSTAL: true
  # Determines whether Ender Pearl is enabled or disabled. Available options: true, false
  ENDER-PEARL: true
  # Determines whether Respawn Anchor is enabled or disabled. Available options: true, false
  RESPAWN-ANCHOR: true
  # The text or value for Block Message. Available options: Any valid string text
  BLOCK-MESSAGE: '&cYou can''t use this command in your current status.'
  # Configuration section for Block Commands.
  BLOCK-COMMANDS:
  - /spawn
  - /afk
  - /rtp
  - /homes
  - /tpa
  # Configuration section for Excluded Worlds.
  EXCLUDED-WORLDS:
  - duels
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `COMBAT-MANAGER.ENABLED` | `bool` | `true`, `false` | `true` | Master switch for combat tagging, the action bar timer, command blocking and the logout kill. The `COMBAT` feature toggle has to be on as well, which it is unless someone has turned it off with `/uds features disable combat`. Being tagged also switches a player's flight off when `FLY-SYSTEM.AUTO-DISABLE-OUTSIDE` is enabled and they do not hold `ultimatedonutsmp.staff.fly`, leaving creative and spectator mode alone. |
| `COMBAT-MANAGER.COOLDOWN` | `int` | Any valid integer | `16` | How many seconds a player stays tagged. Each new hit resets the full duration rather than adding to it, so a player is clear this many seconds after the last hit lands. |
| `COMBAT-MANAGER.KILL-ON-LOGOUT` | `bool` | `true`, `false` | `false` | Kills a player who disconnects while still tagged, so logging out mid-fight is not a way to escape. They die where they logged off and drop their items there. Skipped for players who are already dead, who are in an excluded world, or who are in a duel queue, a duel, or an FFA session. |
| `COMBAT-MANAGER.ACTION-BAR` | `string` | Any valid string text | `'&fCombat: &b${time}s'` | The action bar line shown once a second while a player is tagged. Both `${time}` and `{time}` are replaced with the whole seconds left. |
| `COMBAT-MANAGER.MOBS` | `bool` | `true`, `false` | `false` | Whether mob damage tags a player as well as damage from other players. Covers any living attacker, and arrows or other projectiles one of them fired. |
| `COMBAT-MANAGER.ENDER-CRYSTAL` | `bool` | `true`, `false` | `true` | Whether end crystal damage tags a player. |
| `COMBAT-MANAGER.ENDER-PEARL` | `bool` | `true`, `false` | `true` | Tags a player when they teleport with an ender pearl, and counts the damage the pearl deals on landing as player damage rather than ignoring it. |
| `COMBAT-MANAGER.RESPAWN-ANCHOR` | `bool` | `true`, `false` | `true` | Whether damage from a respawn anchor explosion tags a player. |
| `COMBAT-MANAGER.BLOCK-MESSAGE` | `string` | Any valid string text | `'&cYou can''t use this command in your current status.'` | Sent to the player when one of the blocked commands is refused. |
| `COMBAT-MANAGER.BLOCK-COMMANDS` | `list` | List of configured items/strings | `['/spawn', '/afk', '/rtp', '/homes', '/tpa']` | Commands refused while a player is tagged. Write each one with its leading slash. Matching is case insensitive and looks at the typed command word on its own, so any alias you also want blocked has to be listed in its own right. |
| `COMBAT-MANAGER.EXCLUDED-WORLDS` | `list` | List of configured items/strings | `['duels']` | Worlds where none of this applies. Players there are never tagged, never refused a command, and never killed for logging out. |

### 3. Practical Setup Example

A server that actually punishes combat logging. The tag runs a little longer than the default, disconnecting while it is up is fatal, and mobs stay out of it so nobody gets dragged into combat by a zombie:

```yaml
COMBAT-MANAGER:
  ENABLED: true
  COOLDOWN: 20
  KILL-ON-LOGOUT: true
  ACTION-BAR: '&cCombat: &f${time}s'
  MOBS: false
  ENDER-CRYSTAL: true
  ENDER-PEARL: true
  RESPAWN-ANCHOR: true
  BLOCK-MESSAGE: '&cYou cannot use that while you are in combat.'
  BLOCK-COMMANDS:
  - /spawn
  - /afk
  - /rtp
  - /homes
  - /tpa
  - /warp
  EXCLUDED-WORLDS:
  - duels
```

---

## Section: `RTP-ZONE`

### 1. Commented Setup Code Example

```yaml
RTP-ZONE:
  # Determines whether Enabled is enabled or disabled. Available options: true, false
  ENABLED: true
  # The text or value for Cuboid. Available options: Any valid string text
  CUBOID: ''
  # The numerical value for Every. Available options: Any valid integer
  EVERY: 30
  TITLE: '&c&lRTP Zone'
  # The text or value for Sub Title. Available options: Any valid string text
  SUB-TITLE: '&fTeleporting in %countdown%'
  # The text or value for Cancelled Message. Available options: Any valid string text
  CANCELLED-MESSAGE: '&cRTP cancelled because you left the zone.'
  # The text or value for Failed Message. Available options: Any valid string text
  FAILED-MESSAGE: '&cCould not find a safe RTP zone location.'
  # The text or value for Success Message. Available options: Any valid string text
  SUCCESS-MESSAGE: ''
  # Configuration section for World.
  WORLD:
    NAME: world
    # The numerical value for Center X. Available options: Any valid integer
    CENTER-X: 0
    # The numerical value for Center Z. Available options: Any valid integer
    CENTER-Z: 0
    # The numerical value for Min Radius. Available options: Any valid integer
    MIN-RADIUS: 500
    # The numerical value for Max Radius. Available options: Any valid integer
    MAX-RADIUS: 2000
  # The numerical value for Title Fade Out Ticks. Available options: Any valid integer
  TITLE-FADE-OUT-TICKS: 10
# Configuration section for First Join Rtp. Drops brand new players at a random location
# the first time they join instead of leaving them on the vanilla world spawn. The search
# ignores RTP cooldowns, playtime requirements, and the RTP queue, but it does require the
# RTP feature itself to be enabled.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `RTP-ZONE.ENABLED` | `bool` | `true`, `false` | `true` | Whether the zone runs. The RTP Zone feature toggle has to be on as well, so turning that off disables the zone regardless of this key. |
| `RTP-ZONE.CUBOID` | `str` | The name of a cuboid | `''` | The cuboid that acts as the zone; standing inside it starts the countdown. You normally set this with `/cuboid` rather than by hand, which writes the name in for you. Blank means no zone is bound. |
| `RTP-ZONE.EVERY` | `int` | `1` or more | `30` | Seconds a player has to stay inside the cuboid before they are teleported. Values below `1` are raised to `1`. |
| `RTP-ZONE.TITLE` | `str` | Any string text | `'&c&lRTP Zone'` | The title shown while the countdown runs. |
| `RTP-ZONE.SUB-TITLE` | `str` | Any string text | `'&fTeleporting in %countdown%'` | The subtitle under it. `%countdown%` becomes the seconds left. |
| `RTP-ZONE.CANCELLED-MESSAGE` | `str` | Any string text | `'&cRTP cancelled because you left the zone.'` | Sent when someone walks out before the countdown finishes. |
| `RTP-ZONE.FAILED-MESSAGE` | `str` | Any string text | `'&cCould not find a safe RTP zone location.'` | Sent when the search ran out of attempts without finding anywhere safe. |
| `RTP-ZONE.SUCCESS-MESSAGE` | `str` | Any string text | `''` | Sent after a successful teleport. Blank by default, which sends nothing at all. |
| `RTP-ZONE.WORLD.NAME` | `str` | A world name | `world` | The world the zone drops people into. It does not have to be the world the cuboid sits in. |
| `RTP-ZONE.WORLD.CENTER-X` | `int` | Any valid integer | `0` | X coordinate the search ring is measured from. |
| `RTP-ZONE.WORLD.CENTER-Z` | `int` | Any valid integer | `0` | Z coordinate the search ring is measured from. |
| `RTP-ZONE.WORLD.MIN-RADIUS` | `int` | Any valid integer | `500` | Closest the destination may land to that centre. |
| `RTP-ZONE.WORLD.MAX-RADIUS` | `int` | Any valid integer | `2000` | Furthest it may land. Set it below the minimum and the minimum wins, so the ring can never invert. |
| `RTP-ZONE.TITLE-FADE-OUT-TICKS` | `int` | Any valid integer | `10` | How long the countdown title takes to fade once it is cleared or replaced. |

### 3. Practical Setup Example

```yaml
RTP-ZONE:
  ENABLED: true
  CUBOID: 'spawn-rtp-pad'
  # ten seconds of standing on the pad is enough
  EVERY: 10
  WORLD:
    NAME: world
    MIN-RADIUS: 1000
    MAX-RADIUS: 15000
```

---
## Section: `TELEPORT-COOLDOWN`

### 1. Commented Setup Code Example

```yaml
TELEPORT-COOLDOWN:
  # The numerical value for Home. Available options: Any valid integer
  HOME: 5
  # The numerical value for Team Home. Available options: Any valid integer
  TEAM-HOME: 5
  # The numerical value for Spawn. Available options: Any valid integer
  SPAWN: 5
  # The numerical value for Afk. Available options: Any valid integer
  AFK: 5
  # The numerical value for Tpa. Available options: Any valid integer
  TPA: 5
  # The numerical value for Warp. Available options: Any valid integer
  WARP: 5
  # The numerical value for Rtp. Available options: Any valid integer
  RTP: 5
# Configuration section for Bounty.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `TELEPORT-COOLDOWN.HOME` | `int` | Any valid integer | `5` | Seconds the player has to stand still before `/home` completes. Moving more than half a block cancels the teleport and sends `TELEPORT.CANCELED`. |
| `TELEPORT-COOLDOWN.TEAM-HOME` | `int` | Any valid integer | `5` | Seconds the player has to stand still before `/teamhome` completes. Moving more than half a block cancels the teleport and sends `TELEPORT.CANCELED`. |
| `TELEPORT-COOLDOWN.SPAWN` | `int` | Any valid integer | `5` | Seconds the player has to stand still before `/spawn` completes. Moving more than half a block cancels the teleport and sends `TELEPORT.CANCELED`. |
| `TELEPORT-COOLDOWN.AFK` | `int` | Any valid integer | `5` | Seconds the player has to stand still before the AFK teleport completes. Moving more than half a block cancels the teleport and sends `TELEPORT.CANCELED`. |
| `TELEPORT-COOLDOWN.TPA` | `int` | Any valid integer | `5` | Seconds the player has to stand still before an accepted `/tpa` completes. Moving more than half a block cancels the teleport and sends `TELEPORT.CANCELED`. |
| `TELEPORT-COOLDOWN.WARP` | `int` | Any valid integer | `5` | Seconds the player has to stand still before `/warp` completes. Moving more than half a block cancels the teleport and sends `TELEPORT.CANCELED`. |
| `TELEPORT-COOLDOWN.RTP` | `int` | Any valid integer | `5` | Seconds the player has to stand still before `/rtp` completes. Moving more than half a block cancels the teleport and sends `TELEPORT.CANCELED`. |

### 3. Practical Setup Example

```yaml
TELEPORT-COOLDOWN:
  # instant for the everyday ones
  HOME: 0
  TEAM-HOME: 0
  SPAWN: 0
  AFK: 5
  TPA: 3
  WARP: 3
  # keep a warmup on rtp so it cannot be used to run from a fight
  RTP: 10
```

---
## Section: `BOUNTY`

### 1. Commented Setup Code Example

```yaml
BOUNTY:
  # Configuration section for Excluded Worlds.
  EXCLUDED-WORLDS:
  - duels
# Configuration section for Amethyst Tools.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `BOUNTY.EXCLUDED-WORLDS` | `list` | List of world names | `['duels']` | Worlds where a bounty is not paid out. Kill someone with a bounty on their head in one of these and the killer gets nothing, while the bounty stays on the victim for somebody to claim elsewhere. `duels` is listed so arena matches cannot be used to farm bounties off a friend. |

### 3. Practical Setup Example

```yaml
BOUNTY:
  EXCLUDED-WORLDS:
  - duels
  - ffa
```

---
## Section: `AMETHYST-TOOLS`

### 1. Commented Setup Code Example

```yaml
AMETHYST-TOOLS:
  # Configuration section for Excluded Worlds.
  EXCLUDED-WORLDS:
  - duels
  # Configuration section for Pickaxe.
  PICKAXE:
    # Configuration section for Particle.
    PARTICLE:
      NAME: FALLING_DUST
      MATERIAL: PURPLE_CONCRETE_POWDER
      # The numerical value for Amount. Available options: Any valid integer
      AMOUNT: 10
    # Configuration section for Disabled Blocks.
    DISABLED-BLOCKS:
    - GRASS_BLOCK
    - DIRT_PATH
    - DIRT
    - COARSE_DIRT
    - ROOTED_DIRT
    - CLAY
    - FARMLAND
    - SAND
    - RED_SAND
    - GRAVEL
    - SPAWNER
    NAME: '&#A303F9Amethyst Pickaxe'
    LORE:
    - '&79 Blocks Per Break'
    - '&8Self Destruct'
    - '&8{time}'
    # Configuration section for Enchantments.
    ENCHANTMENTS:
    - MENDING:1
    - EFFICIENCY:5
    - UNBREAKING:3
  # Configuration section for Axe.
  AXE:
    # Configuration section for Particle.
    PARTICLE:
      NAME: FALLING_DUST
      MATERIAL: PURPLE_CONCRETE_POWDER
      # The numerical value for Amount. Available options: Any valid integer
      AMOUNT: 10
    NAME: '&#A303F9Amethyst Axe'
    LORE:
    - '&7Breaks Trees Instantly'
    - '&8Self Destruct'
    - '&8{time}'
    # Configuration section for Enchantments.
    ENCHANTMENTS:
    - MENDING:1
    - EFFICIENCY:5
    - UNBREAKING:3
    - SILK_TOUCH:1
  # Configuration section for Sellaxe.
  SELLAXE:
    # Configuration section for Particle.
    PARTICLE:
      NAME: FALLING_DUST
      MATERIAL: PURPLE_CONCRETE_POWDER
      # The numerical value for Amount. Available options: Any valid integer
      AMOUNT: 10
    NAME: '&#A303F9Amethyst Sell Axe'
    LORE:
    - '&7Instantly Sells All Items in A Chest'
    - '&8Self Destruct'
    - '&8{time}'
    # Configuration section for Enchantments.
    ENCHANTMENTS:
    - MENDING:1
    - EFFICIENCY:5
    - UNBREAKING:3
  # Configuration section for Shovel.
  SHOVEL:
    # Configuration section for Particle.
    PARTICLE:
      NAME: FALLING_DUST
      MATERIAL: PURPLE_CONCRETE_POWDER
      # The numerical value for Amount. Available options: Any valid integer
      AMOUNT: 10
    # Configuration section for Allowed Blocks.
    ALLOWED-BLOCKS:
    - GRASS_BLOCK
    - DIRT_PATH
    - DIRT
    - COARSE_DIRT
    - ROOTED_DIRT
    - CLAY
    - FARMLAND
    - SAND
    - RED_SAND
    - GRAVEL
    NAME: '&#A303F9Amethyst Shovel'
    LORE:
    - '&79 Blocks Per Break'
    - '&8Self Destruct'
    - '&8{time}'
    # Configuration section for Enchantments.
    ENCHANTMENTS:
    - MENDING:1
    - EFFICIENCY:5
    - UNBREAKING:3
  # Configuration section for Bucket.
  BUCKET:
    # Configuration section for Particle.
    PARTICLE:
      NAME: FALLING_DUST
      MATERIAL: PURPLE_CONCRETE_POWDER
      # The numerical value for Amount. Available options: Any valid integer
      AMOUNT: 10
    NAME: '&#A303F9Amethyst Bucket'
    LORE:
    - '&7Drains 27 Water At Once'
    - '&8Self Destruct'
    - '&8{time}'
    # A list configuration for Enchantments. Available options: Multiple items
    ENCHANTMENTS: []
  # Configuration section for Booster.
  BOOSTER:
    # Configuration section for Particle.
    PARTICLE:
      NAME: FALLING_DUST
      MATERIAL: PURPLE_CONCRETE_POWDER
      # The numerical value for Amount. Available options: Any valid integer
      AMOUNT: 10
    NAME: '&#A303F9Shard Booster'
    LORE:
    - '&74x Shard Production for 24 hours'
    - '&8Self Destruct'
    - '&8{time}'
    # A list configuration for Enchantments. Available options: Multiple items
    ENCHANTMENTS: []
# Configuration section for Commands.
# Configuration section for Voice Chat.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `AMETHYST-TOOLS.EXCLUDED-WORLDS` | `list` | List of world names | `['duels']` | Worlds where the amethyst tools do nothing. Their special break behaviour is skipped there, so a sell axe carried into an arena is an ordinary axe. |
| `AMETHYST-TOOLS.<TOOL>.PARTICLE.NAME` | `str` | Any particle name | `FALLING_DUST` | The particle trailed from the tool while it works. |
| `AMETHYST-TOOLS.<TOOL>.PARTICLE.MATERIAL` | `str` | Any block material | `PURPLE_CONCRETE_POWDER` | The block the particle is tinted from. Only used by particles that take a block, such as `FALLING_DUST`; other particles ignore it. |
| `AMETHYST-TOOLS.<TOOL>.PARTICLE.AMOUNT` | `int` | Any valid integer | `10` | How many particles are spawned per effect. Raise it for a heavier trail, drop it to nothing if players complain about the clutter. |
| `AMETHYST-TOOLS.<TOOL>.NAME` | `str` | Any string text | `'&#A303F9Amethyst Pickaxe'` | The item name. Hex colours in `&#RRGGBB` form work here, which is where the purple comes from. |
| `AMETHYST-TOOLS.<TOOL>.LORE` | `list` | Lines of text | `['&79 Blocks Per Break', '&8Self Destruct', '&8{time}']` | The item lore. A line containing `{time}` is rewritten with the time the tool has left and keeps counting down while it sits in the inventory, so keep that placeholder on a line of its own. |
| `AMETHYST-TOOLS.<TOOL>.ENCHANTMENTS` | `list` | `ENCHANTMENT:LEVEL` entries | `['MENDING:1', 'EFFICIENCY:5', 'UNBREAKING:3']` | Enchantments applied when the tool is handed out. Levels beyond the vanilla maximum are allowed. An empty list, as the bucket and booster ship with, gives a clean item. |
| `AMETHYST-TOOLS.PICKAXE.DISABLED-BLOCKS` | `list` | Block materials | `['GRASS_BLOCK', 'DIRT_PATH', ...]` | Blocks the pickaxe refuses to area-break. The list is the soft ground the shovel is meant for, plus `SPAWNER` so a stray swing cannot wipe out a spawner. |
| `AMETHYST-TOOLS.SHOVEL.ALLOWED-BLOCKS` | `list` | Block materials | `['GRASS_BLOCK', 'DIRT_PATH', ...]` | The opposite arrangement: the shovel area-breaks only what is listed here, so it cannot be used as a second pickaxe. |

`<TOOL>` above stands for any of `PICKAXE`, `AXE`, `SELLAXE`, `SHOVEL`, `BUCKET` or `BOOSTER`.
Every one of them takes the same `PARTICLE`, `NAME`, `LORE` and `ENCHANTMENTS` keys; only the
pickaxe adds `DISABLED-BLOCKS` and only the shovel adds `ALLOWED-BLOCKS`.

This section covers how the tools look and which blocks they act on. What they cost, how long
they last and the messages they send live in `amethyst-tools.yml`, which confusingly also has a
root key called `AMETHYST-TOOLS`. The two are separate: the plugin reads the cosmetic and block
settings from this file and the sounds, particles and security settings from that one, so a key
put in the wrong file is simply never read.

### 3. Practical Setup Example

```yaml
AMETHYST-TOOLS:
  EXCLUDED-WORLDS:
  - duels
  - ffa
  PICKAXE:
    PARTICLE:
      # quieter trail on a busy server
      AMOUNT: 3
    NAME: '&#A303F9Amethyst Pickaxe'
    LORE:
    - '&79 Blocks Per Break'
    - '&8Expires in &f{time}'
    ENCHANTMENTS:
    - EFFICIENCY:5
    - UNBREAKING:3
```

---
## Section: `VOICE-CHAT`

Shows every player a consent menu before their microphone works. The menu opens on its own the first
time somebody joins and keeps opening on later joins until they pick an answer, so nobody ends up
talking in a voice channel without having read the policy first. Once they confirm or decline, the
prompt stops. `/voicechatconsent` reopens it, and `/voicechatconsent revoke` throws the answer away
and puts them back at undecided.

The policy wording itself is not here. It lives under `VOICE-CHAT-CONSENT-MENU.INFO-BUTTON.LORE` in
`menus.yml`, because what your server records and how long you keep it is your own policy to write.
The bundled text is a starting point, not legal advice; edit it before you rely on it.

### 1. Commented Setup Code Example

```yaml
# Configuration section for Voice Chat.
VOICE-CHAT:
  # Determines whether Prompt On Join is enabled or disabled. Available options: true, false
  PROMPT-ON-JOIN: true
  # The number for Prompt Delay Ticks. Available options: Any whole number
  PROMPT-DELAY-TICKS: 40
  # Determines whether Mute Until Accepted is enabled or disabled. Available options: true, false
  MUTE-UNTIL-ACCEPTED: true
```

### 2. Key Options & Technical Breakdown

| Path | Type | Accepted Values | Default | Description |
| :--- | :--- | :--- | :--- | :--- |
| `VOICE-CHAT.PROMPT-ON-JOIN` | `boolean` | `true`, `false` | `true` | Opens the menu by itself when an undecided player joins. Turn it off to leave the menu behind `/voicechatconsent` only. The answer is still stored either way. |
| `VOICE-CHAT.PROMPT-DELAY-TICKS` | `integer` | Any whole number, 20 ticks to the second | `40` | How long to wait after the join before the menu opens. Two seconds keeps it clear of join messages and of any other plugin that moves players around on arrival. |
| `VOICE-CHAT.MUTE-UNTIL-ACCEPTED` | `boolean` | `true`, `false` | `true` | Drops microphone audio from anyone who has not confirmed. Set it to `false` and the menu becomes a record of who agreed, without stopping anybody talking. Needs Simple Voice Chat installed to have any effect. |

The whole feature answers to the `VOICE_CHAT` toggle in `/features`, so disabling it there stops the
prompt, the command, and the microphone gate together.

A voice mute issued by staff is a separate thing and none of the settings above reach it. `/vcmute`
writes a punishment record, so it survives a relog, appears in `/punishments` alongside bans and
chat mutes, and keeps the player off the microphone whether or not `MUTE-UNTIL-ACCEPTED` is on.
`/vcunmute` lifts it.

### 3. Practical Setup Example

A server that will not carry a recording of anyone who never agreed to being recorded. The prompt
waits three seconds so it does not collide with a spawn teleport, and nobody transmits until they
have confirmed:

```yaml
VOICE-CHAT:
  PROMPT-ON-JOIN: true
  PROMPT-DELAY-TICKS: 60
  MUTE-UNTIL-ACCEPTED: true
```

---
## Section: `COMMANDS`

### 1. Commented Setup Code Example

```yaml
COMMANDS:
  # Determines whether Chat is enabled or disabled. Available options: true, false
  CHAT: true
  # Determines whether Ignore is enabled or disabled. Available options: true, false
  IGNORE: true
  # Determines whether Message is enabled or disabled. Available options: true, false
  MESSAGE: true
  # Determines whether Bounty is enabled or disabled. Available options: true, false
  BOUNTY: true
  # Determines whether Cuboid is enabled or disabled. Available options: true, false
  CUBOID: true
  # Determines whether Afk is enabled or disabled. Available options: true, false
  AFK: true
  # Determines whether Shards is enabled or disabled. Available options: true, false
  SHARDS: true
  # Determines whether Warp is enabled or disabled. Available options: true, false
  WARP: true
  # Determines whether Team is enabled or disabled. Available options: true, false
  TEAM: true
  # Determines whether Billford is enabled or disabled. Available options: true, false
  BILLFORD: true
  # Determines whether Home is enabled or disabled. Available options: true, false
  HOME: true
  # Determines whether Leaderboards is enabled or disabled. Available options: true, false
  LEADERBOARDS: true
  # Determines whether Night Vision is enabled or disabled. Available options: true, false
  NIGHT-VISION: true
  # Determines whether Phantom is enabled or disabled. Available options: true, false
  PHANTOM: true
  # Determines whether Rtp is enabled or disabled. Available options: true, false
  RTP: true
  # Determines whether Sell is enabled or disabled. Available options: true, false
  SELL: true
  # Determines whether Settings is enabled or disabled. Available options: true, false
  SETTINGS: true
  # Determines whether Shop is enabled or disabled. Available options: true, false
  SHOP: true
  # Determines whether Enderchest is enabled or disabled. Available options: true, false
  ENDERCHEST: true
  # Determines whether Gamemode is enabled or disabled. Available options: true, false
  GAMEMODE: true
  # Determines whether Social is enabled or disabled. Available options: true, false
  SOCIAL: true
  # Determines whether Spawn is enabled or disabled. Available options: true, false
  SPAWN: true
  # Determines whether Stats is enabled or disabled. Available options: true, false
  STATS: true
  # Determines whether Tpa is enabled or disabled. Available options: true, false
  TPA: true
  # Determines whether Tpauto is enabled or disabled. Available options: true, false
  TPAUTO: true
  # Determines whether Findplayer is enabled or disabled. Available options: true, false
  FINDPLAYER: true
  # Determines whether Crate is enabled or disabled. Available options: true, false
  CRATE: true
  # Determines whether Shardpay is enabled or disabled. Available options: true, false
  SHARDPAY: false
  # Determines whether Ranks is enabled or disabled. Available options: true, false
  RANKS: true
  # Determines whether Rules is enabled or disabled. Available options: true, false
  RULES: true
  # Determines whether Help is enabled or disabled. Available options: true, false
  HELP: true
  # Determines whether Servers is enabled or disabled. Available options: true, false
  SERVERS: true
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `COMMANDS.CHAT` | `bool` | `true`, `false` | `true` | Enables global chat commands and the chat moderation controls. |
| `COMMANDS.IGNORE` | `bool` | `true`, `false` | `true` | Enables the ignore and unignore commands. |
| `COMMANDS.MESSAGE` | `bool` | `true`, `false` | `true` | Enables private messages, replies and the private message toggle. |
| `COMMANDS.BOUNTY` | `bool` | `true`, `false` | `true` | Enables the bounty command and its menus. |
| `COMMANDS.CUBOID` | `bool` | `true`, `false` | `true` | Enables cuboid region management and the helpers that bind a region to a feature. |
| `COMMANDS.AFK` | `bool` | `true`, `false` | `true` | Enables the afk command, its menus and the afk movement task. |
| `COMMANDS.SHARDS` | `bool` | `true`, `false` | `true` | Enables shard balances, shard pay, passive rewards and shard cuboids. |
| `COMMANDS.WARP` | `bool` | `true`, `false` | `true` | Enables the warp commands and the warp manager commands. |
| `COMMANDS.TEAM` | `bool` | `true`, `false` | `true` | Enables the team command, team homes and the team menus. |
| `COMMANDS.BILLFORD` | `bool` | `true`, `false` | `true` | Enables the Billford trade menu and its rotation task. |
| `COMMANDS.HOME` | `bool` | `true`, `false` | `true` | Enables the home commands and the home menu. |
| `COMMANDS.LEADERBOARDS` | `bool` | `true`, `false` | `true` | Enables the leaderboard commands and leaderboard menus. |
| `COMMANDS.NIGHT-VISION` | `bool` | `true`, `false` | `true` | Enables the night vision player toggle. |
| `COMMANDS.PHANTOM` | `bool` | `true`, `false` | `true` | Enables the phantom spawning toggle. |
| `COMMANDS.RTP` | `bool` | `true`, `false` | `true` | Enables the random teleport command, its queue command and the rtp menu. |
| `COMMANDS.SELL` | `bool` | `true`, `false` | `true` | Enables the worth browser and the worth display helpers. |
| `COMMANDS.SETTINGS` | `bool` | `true`, `false` | `true` | Enables the player settings menu. |
| `COMMANDS.SHOP` | `bool` | `true`, `false` | `true` | Enables the shop command and its purchase menus. |
| `COMMANDS.ENDERCHEST` | `bool` | `true`, `false` | `true` | Enables the custom ender chest command and its listener. |
| `COMMANDS.GAMEMODE` | `bool` | `true`, `false` | `true` | Enables the staff gamemode commands. |
| `COMMANDS.SOCIAL` | `bool` | `true`, `false` | `true` | Enables the discord, twitter/x, store and media commands. |
| `COMMANDS.SPAWN` | `bool` | `true`, `false` | `true` | Enables the spawn command and spawn menu. |
| `COMMANDS.STATS` | `bool` | `true`, `false` | `true` | Enables the stats, ping and playtime commands. |
| `COMMANDS.TPA` | `bool` | `true`, `false` | `true` | Enables the teleport request commands and the confirm menu. |
| `COMMANDS.TPAUTO` | `bool` | `true`, `false` | `true` | Enables the tpa auto-accept commands. |
| `COMMANDS.FINDPLAYER` | `bool` | `true`, `false` | `true` | Enables the staff find player command. |
| `COMMANDS.CRATE` | `bool` | `true`, `false` | `true` | Enables crate commands, crate menus, key-all and the crate visual effects. |
| `COMMANDS.SHARDPAY` | `bool` | `true`, `false` | `false` | Enables paying shards to another player. This is the one entry that ships off, so shard transfers are opt-in. |
| `COMMANDS.RANKS` | `bool` | `true`, `false` | `true` | Enables the ranks command and ranks menu. |
| `COMMANDS.RULES` | `bool` | `true`, `false` | `true` | Enables the rules command and rules menu. |
| `COMMANDS.HELP` | `bool` | `true`, `false` | `true` | Enables the help command and the server info menu. |
| `COMMANDS.SERVERS` | `bool` | `true`, `false` | `true` | Enables the network server status command and menu. |

These are switches, not a list of what exists: setting one to `false` takes the matching commands
away rather than hiding them. What a blocked command looks like to the player is set by
`FEATURES_SETTINGS.DISABLED_COMMAND_ACTION` near the top of this file, which can show a message,
reply as though the command were unknown, or unregister it from the server outright.

One thing to know before editing here. Most of these keys have a newer counterpart at
`FEATURES.<name>.ENABLED`, and when that path exists it wins; the `COMMANDS` entry is only read as
a fallback. `FEATURES` is not in the shipped file, so on a fresh install these keys are the live
switches, but on a server where the feature toggles have been used the value here can look
ignored. `SHARDPAY` has no feature counterpart at all and is always read from this section.

### 3. Practical Setup Example

```yaml
COMMANDS:
  # a survival server that runs its economy elsewhere
  SHOP: false
  SELL: false
  BILLFORD: false
  # and lets players move shards between accounts
  SHARDPAY: true
```

---
