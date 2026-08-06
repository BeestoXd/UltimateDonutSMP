# Detailed Configuration & Setup Guide: `hide.yml`

This is the official, 100% complete technical setup guide for `hide.yml` in **UltimateDonutSMP**.
Each section details the exact commented setup code block, allowed option values, data types, default values, and in-depth functional behavior.

---

## Section: `ENABLED`

### 1. Commented Setup Code Example

```yaml
ENABLED: true
# The numerical value for Cooldown Seconds. Available options: Any valid integer
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `ENABLED` system. Set to `true` to enable, `false` to disable. |

### 3. Practical Setup Example

```yaml
ENABLED: true
# The numerical value for Cooldown Seconds. Available options: Any valid integer
```

---

## Section: `COOLDOWN-SECONDS`

### 1. Commented Setup Code Example

```yaml
COOLDOWN-SECONDS: 30
# The numerical value for Max Name Length. Available options: Any valid integer
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `COOLDOWN-SECONDS` | `int` | Any valid integer number | `'30'` | Configures the technical `COOLDOWN-SECONDS` parameter for `COOLDOWN-SECONDS` in `hide.yml`. |

### 3. Practical Setup Example

```yaml
COOLDOWN-SECONDS: 30
# The numerical value for Max Name Length. Available options: Any valid integer
```

---

## Section: `MAX-NAME-LENGTH`

### 1. Commented Setup Code Example

```yaml
MAX-NAME-LENGTH: 16
# The text or value for Staff Marker. Available options: Any valid string text
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `MAX-NAME-LENGTH` | `int` | Any valid integer number | `'16'` | Configures the technical `MAX-NAME-LENGTH` parameter for `MAX-NAME-LENGTH` in `hide.yml`. |

### 3. Practical Setup Example

```yaml
MAX-NAME-LENGTH: 16
# The text or value for Staff Marker. Available options: Any valid string text
```

---

## Section: `STAFF-MARKER`

### 1. Commented Setup Code Example

```yaml
STAFF-MARKER: '&8[&cH&8] '
# Configuration section for Scramble.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `STAFF-MARKER` | `str` | Any string text | `'&8[&cH&8] '` | Configures the technical `STAFF-MARKER` parameter for `STAFF-MARKER` in `hide.yml`. |

### 3. Practical Setup Example

```yaml
STAFF-MARKER: '&8[&cH&8] '
# Configuration section for Scramble.
```

---

## Section: `SCRAMBLE`

### 1. Commented Setup Code Example

```yaml
SCRAMBLE:
  # The numerical value for Length. Available options: Any valid integer
  LENGTH: 10
  # The text or value for Characters. Available options: Any valid string text
  CHARACTERS: ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_
  # Determines whether Obfuscated is enabled or disabled. Available options: true, false
  OBFUSCATED: true
# Configuration section for Aliases.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `SCRAMBLE.LENGTH` | `int` | Any valid integer number | `'10'` | Configures the technical `LENGTH` parameter for `SCRAMBLE.LENGTH` in `hide.yml`. |
| `SCRAMBLE.CHARACTERS` | `str` | Any string text | `'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghi...'` | Configures the technical `CHARACTERS` parameter for `SCRAMBLE.CHARACTERS` in `hide.yml`. |
| `SCRAMBLE.OBFUSCATED` | `bool` | `true`, `false` | `true` | Configures the technical `OBFUSCATED` parameter for `SCRAMBLE.OBFUSCATED` in `hide.yml`. |

### 3. Practical Setup Example

```yaml
SCRAMBLE:
  # The numerical value for Length. Available options: Any valid integer
  LENGTH: 10
  # The text or value for Characters. Available options: Any valid string text
  CHARACTERS: ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_
  # Determines whether Obfuscated is enabled or disabled. Available options: true, false
  OBFUSCATED: true
# Configuration section for Aliases.
```

---

## Section: `ALIASES`

### 1. Commented Setup Code Example

```yaml
ALIASES:
  # Configuration section for Mrbeast.
  mrbeast:
    NAME: MrBeast
    # The text or value for Skin. Available options: Any valid string text
    SKIN: mrbeast
  # Configuration section for Technoblade.
  technoblade:
    NAME: Technoblade
    # The text or value for Skin. Available options: Any valid string text
    SKIN: technoblade
  # Configuration section for Dream.
  dream:
    NAME: Dream
    # The text or value for Skin. Available options: Any valid string text
    SKIN: dream
  # Configuration section for Drdonutt.
  drdonutt:
    NAME: DrDonutt
    # The text or value for Skin. Available options: Any valid string text
    SKIN: drdonutt
# Configuration section for Skins.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `ALIASES.mrbeast.NAME` | `str` | Any string text | `'MrBeast'` | Configures the technical `NAME` parameter for `ALIASES.mrbeast.NAME` in `hide.yml`. |
| `ALIASES.mrbeast.SKIN` | `str` | Any string text | `'mrbeast'` | Configures the technical `SKIN` parameter for `ALIASES.mrbeast.SKIN` in `hide.yml`. |
| `ALIASES.technoblade.NAME` | `str` | Any string text | `'Technoblade'` | Configures the technical `NAME` parameter for `ALIASES.technoblade.NAME` in `hide.yml`. |
| `ALIASES.technoblade.SKIN` | `str` | Any string text | `'technoblade'` | Configures the technical `SKIN` parameter for `ALIASES.technoblade.SKIN` in `hide.yml`. |
| `ALIASES.dream.NAME` | `str` | Any string text | `'Dream'` | Configures the technical `NAME` parameter for `ALIASES.dream.NAME` in `hide.yml`. |
| `ALIASES.dream.SKIN` | `str` | Any string text | `'dream'` | Configures the technical `SKIN` parameter for `ALIASES.dream.SKIN` in `hide.yml`. |
| `ALIASES.drdonutt.NAME` | `str` | Any string text | `'DrDonutt'` | Configures the technical `NAME` parameter for `ALIASES.drdonutt.NAME` in `hide.yml`. |
| `ALIASES.drdonutt.SKIN` | `str` | Any string text | `'drdonutt'` | Configures the technical `SKIN` parameter for `ALIASES.drdonutt.SKIN` in `hide.yml`. |

### 3. Practical Setup Example

```yaml
ALIASES:
  # Configuration section for Mrbeast.
  mrbeast:
    NAME: MrBeast
    # The text or value for Skin. Available options: Any valid string text
    SKIN: mrbeast
  # Configuration section for Technoblade.
  technoblade:
    NAME: Technoblade
    # The text or value for Skin. Available options: Any valid string text
    SKIN: technoblade
  # Configuration section for Dream.
  dream:
    NAME: Dream
    # The text or value for Skin. Available options: Any valid string text
    SKIN: dream
  # Configuration section for Drdonutt.
  drdonutt:
    NAME: DrDonutt
    # The text or value for Skin. Available options: Any valid string text
    SKIN: drdonutt
# Configuration section for Skins.
```

---

## Section: `SKINS`

### 1. Commented Setup Code Example

```yaml
SKINS:
  # Configuration section for Mrbeast.
  mrbeast:
    DISPLAY-NAME: MrBeast
    # The text or value for Username. Available options: Any valid string text
    USERNAME: MrBeast
  # Configuration section for Technoblade.
  technoblade:
    DISPLAY-NAME: Technoblade
    # The text or value for Username. Available options: Any valid string text
    USERNAME: Technoblade
  # Configuration section for Dream.
  dream:
    DISPLAY-NAME: Dream
    # The text or value for Username. Available options: Any valid string text
    USERNAME: Dream
  # Configuration section for Drdonutt.
  drdonutt:
    DISPLAY-NAME: DrDonutt
    # The text or value for Username. Available options: Any valid string text
    USERNAME: DrDonutt
# Configuration section for Gui.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `SKINS.mrbeast.DISPLAY-NAME` | `str` | Any string text | `'MrBeast'` | Configures the technical `DISPLAY-NAME` parameter for `SKINS.mrbeast.DISPLAY-NAME` in `hide.yml`. |
| `SKINS.mrbeast.USERNAME` | `str` | Any string text | `'MrBeast'` | Configures the technical `USERNAME` parameter for `SKINS.mrbeast.USERNAME` in `hide.yml`. |
| `SKINS.technoblade.DISPLAY-NAME` | `str` | Any string text | `'Technoblade'` | Configures the technical `DISPLAY-NAME` parameter for `SKINS.technoblade.DISPLAY-NAME` in `hide.yml`. |
| `SKINS.technoblade.USERNAME` | `str` | Any string text | `'Technoblade'` | Configures the technical `USERNAME` parameter for `SKINS.technoblade.USERNAME` in `hide.yml`. |
| `SKINS.dream.DISPLAY-NAME` | `str` | Any string text | `'Dream'` | Configures the technical `DISPLAY-NAME` parameter for `SKINS.dream.DISPLAY-NAME` in `hide.yml`. |
| `SKINS.dream.USERNAME` | `str` | Any string text | `'Dream'` | Configures the technical `USERNAME` parameter for `SKINS.dream.USERNAME` in `hide.yml`. |
| `SKINS.drdonutt.DISPLAY-NAME` | `str` | Any string text | `'DrDonutt'` | Configures the technical `DISPLAY-NAME` parameter for `SKINS.drdonutt.DISPLAY-NAME` in `hide.yml`. |
| `SKINS.drdonutt.USERNAME` | `str` | Any string text | `'DrDonutt'` | Configures the technical `USERNAME` parameter for `SKINS.drdonutt.USERNAME` in `hide.yml`. |

### 3. Practical Setup Example

```yaml
SKINS:
  # Configuration section for Mrbeast.
  mrbeast:
    DISPLAY-NAME: MrBeast
    # The text or value for Username. Available options: Any valid string text
    USERNAME: MrBeast
  # Configuration section for Technoblade.
  technoblade:
    DISPLAY-NAME: Technoblade
    # The text or value for Username. Available options: Any valid string text
    USERNAME: Technoblade
  # Configuration section for Dream.
  dream:
    DISPLAY-NAME: Dream
    # The text or value for Username. Available options: Any valid string text
    USERNAME: Dream
  # Configuration section for Drdonutt.
  drdonutt:
    DISPLAY-NAME: DrDonutt
    # The text or value for Username. Available options: Any valid string text
    USERNAME: DrDonutt
# Configuration section for Gui.
```

---

## Section: `GUI`

### 1. Commented Setup Code Example

```yaml
GUI:
  # Configuration section for Main.
  MAIN:
    TITLE: '&8hide'
    SIZE: 27
  # Configuration section for Aliases.
  ALIASES:
    TITLE: '&8select a name - {page}/{pages}'
    SIZE: 54
  # Configuration section for Skins.
  SKINS:
    TITLE: '&8select a skin - {page}/{pages}'
    SIZE: 54
  # Configuration section for List.
  LIST:
    TITLE: '&8hidden players - {page}/{pages}'
    SIZE: 54
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `GUI.MAIN.TITLE` | `str` | Any string text | `'&8hide'` | Configures the technical `TITLE` parameter for `GUI.MAIN.TITLE` in `hide.yml`. |
| `GUI.MAIN.SIZE` | `int` | Any valid integer number | `'27'` | Configures the technical `SIZE` parameter for `GUI.MAIN.SIZE` in `hide.yml`. |
| `GUI.ALIASES.TITLE` | `str` | Any string text | `'&8select a name - {page}/{pages}'` | Configures the technical `TITLE` parameter for `GUI.ALIASES.TITLE` in `hide.yml`. |
| `GUI.ALIASES.SIZE` | `int` | Any valid integer number | `'54'` | Configures the technical `SIZE` parameter for `GUI.ALIASES.SIZE` in `hide.yml`. |
| `GUI.SKINS.TITLE` | `str` | Any string text | `'&8select a skin - {page}/{pages}'` | Configures the technical `TITLE` parameter for `GUI.SKINS.TITLE` in `hide.yml`. |
| `GUI.SKINS.SIZE` | `int` | Any valid integer number | `'54'` | Configures the technical `SIZE` parameter for `GUI.SKINS.SIZE` in `hide.yml`. |
| `GUI.LIST.TITLE` | `str` | Any string text | `'&8hidden players - {page}/{pages}'` | Configures the technical `TITLE` parameter for `GUI.LIST.TITLE` in `hide.yml`. |
| `GUI.LIST.SIZE` | `int` | Any valid integer number | `'54'` | Configures the technical `SIZE` parameter for `GUI.LIST.SIZE` in `hide.yml`. |

### 3. Practical Setup Example

```yaml
GUI:
  # Configuration section for Main.
  MAIN:
    TITLE: '&8hide'
    SIZE: 27
  # Configuration section for Aliases.
  ALIASES:
    TITLE: '&8select a name - {page}/{pages}'
    SIZE: 54
  # Configuration section for Skins.
  SKINS:
    TITLE: '&8select a skin - {page}/{pages}'
    SIZE: 54
  # Configuration section for List.
  LIST:
    TITLE: '&8hidden players - {page}/{pages}'
    SIZE: 54
```

---

## Section: `MESSAGES`

### 1. Commented Setup Code Example

```yaml
MESSAGES:
  # The text or value for Player Only. Available options: Any valid string text
  PLAYER-ONLY: '&conly players can use this command.'
  # The text or value for No Permission. Available options: Any valid string text
  NO-PERMISSION: '&cyou do not have permission.'
  # The text or value for Dependency Missing. Available options: Any valid string text
  DEPENDENCY-MISSING: '&chide requires protocollib to be installed and enabled.'
  # The text or value for Disabled. Available options: Any valid string text
  DISABLED: '&cthe hide feature is currently disabled.'
  # The text or value for In Combat. Available options: Any valid string text
  IN-COMBAT: '&cyou cannot change your hide state while in combat.'
  # The text or value for Cooldown. Available options: Any valid string text
  COOLDOWN: '&cwait &f{seconds}s &cbefore changing hide again.'
  # The text or value for Already Hidden. Available options: Any valid string text
  ALREADY-HIDDEN: '&cyou are already using that hide identity.'
  # The text or value for Not Hidden. Available options: Any valid string text
  NOT-HIDDEN: '&cthat player is not hidden.'
  # The text or value for Player Not Found. Available options: Any valid string text
  PLAYER-NOT-FOUND: '&cplayer not found.'
  # The text or value for Invalid Alias. Available options: Any valid string text
  INVALID-ALIAS: '&cthat disguise alias is not configured or is invalid.'
  # The text or value for Invalid Skin. Available options: Any valid string text
  INVALID-SKIN: '&cunable to resolve that skin name or url.'
  # The text or value for Skin Searching. Available options: Any valid string text
  SKIN-SEARCHING: '&7searching skin for &f{skin}&7...'
  # The text or value for Alias In Use. Available options: Any valid string text
  ALIAS-IN-USE: '&cthat alias is already in use.'
  # The text or value for Scrambled. Available options: Any valid string text
  SCRAMBLED: '&ayour identity is now scrambled as &f{alias}&a.'
  # The text or value for Disguised. Available options: Any valid string text
  DISGUISED: '&ayou are now disguised as &f{alias}&a.'
  # The text or value for Removed. Available options: Any valid string text
  REMOVED: '&ayour hide state has been removed.'
  # The text or value for Admin Removed. Available options: Any valid string text
  ADMIN-REMOVED: '&asuccessfully removed hide from &f{player}&a.'
  # The text or value for Removed By Admin. Available options: Any valid string text
  REMOVED-BY-ADMIN: '&cyour hide state has been removed by an administrator.'
  # The text or value for Status None. Available options: Any valid string text
  STATUS-NONE: '&7hide status: &cinactive'
  # The text or value for Status Active. Available options: Any valid string text
  STATUS-ACTIVE: '&7hide status: &a{mode} &8- &f{alias}'
  # The text or value for Check. Available options: Any valid string text
  CHECK: '&bhide check

    # The text or value for # The Text Or Mode For &7Ʀeal name. Available Options. Available options: Any valid string text
    # The text or mode for &7Ʀeal name. Available options: Any string text &7real
    # The text or value for name. Available options: Any valid string text
    name: &f{real}

    # The text or value for # The Text Or Mode For &7alias. Available Options. Available options: Any valid string text
    # The text or mode for &7alias. Available options: Any string text &7alias: &f{alias}

    # The text or value for # The Text Or Mode For &7mode. Available Options. Available options: Any valid string text
    # The text or mode for &7mode. Available options: Any string text &7mode: &f{mode}

    # The text or value for # The Text Or Mode For &7Ѕkin. Available Options. Available options: Any valid string text
    # The text or mode for &7Ѕkin. Available options: Any string text &7skin: &f{skin}'
  # The text or value for Permission Removed. Available options: Any valid string text
  PERMISSION-REMOVED: '&cyour hide state was removed because its permission is no
    longer available.'
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `MESSAGES.PLAYER-ONLY` | `str` | Any string text | `'&conly players can use this command...'` | Configures the technical `PLAYER-ONLY` parameter for `MESSAGES.PLAYER-ONLY` in `hide.yml`. |
| `MESSAGES.NO-PERMISSION` | `str` | Any string text | `'&cyou do not have permission.'` | Configures the technical `NO-PERMISSION` parameter for `MESSAGES.NO-PERMISSION` in `hide.yml`. |
| `MESSAGES.DEPENDENCY-MISSING` | `str` | Any string text | `'&chide requires protocollib to be i...'` | Configures the technical `DEPENDENCY-MISSING` parameter for `MESSAGES.DEPENDENCY-MISSING` in `hide.yml`. |
| `MESSAGES.DISABLED` | `str` | Any string text | `'&cthe hide feature is currently dis...'` | Configures the technical `DISABLED` parameter for `MESSAGES.DISABLED` in `hide.yml`. |
| `MESSAGES.IN-COMBAT` | `str` | Any string text | `'&cyou cannot change your hide state...'` | Configures the technical `IN-COMBAT` parameter for `MESSAGES.IN-COMBAT` in `hide.yml`. |
| `MESSAGES.COOLDOWN` | `str` | Any string text | `'&cwait &f{seconds}s &cbefore changi...'` | Configures the technical `COOLDOWN` parameter for `MESSAGES.COOLDOWN` in `hide.yml`. |
| `MESSAGES.ALREADY-HIDDEN` | `str` | Any string text | `'&cyou are already using that hide i...'` | Configures the technical `ALREADY-HIDDEN` parameter for `MESSAGES.ALREADY-HIDDEN` in `hide.yml`. |
| `MESSAGES.NOT-HIDDEN` | `str` | Any string text | `'&cthat player is not hidden.'` | Configures the technical `NOT-HIDDEN` parameter for `MESSAGES.NOT-HIDDEN` in `hide.yml`. |
| `MESSAGES.PLAYER-NOT-FOUND` | `str` | Any string text | `'&cplayer not found.'` | Configures the technical `PLAYER-NOT-FOUND` parameter for `MESSAGES.PLAYER-NOT-FOUND` in `hide.yml`. |
| `MESSAGES.INVALID-ALIAS` | `str` | Any string text | `'&cthat disguise alias is not config...'` | Configures the technical `INVALID-ALIAS` parameter for `MESSAGES.INVALID-ALIAS` in `hide.yml`. |
| `MESSAGES.INVALID-SKIN` | `str` | Any string text | `'&cunable to resolve that skin name ...'` | Configures the technical `INVALID-SKIN` parameter for `MESSAGES.INVALID-SKIN` in `hide.yml`. |
| `MESSAGES.SKIN-SEARCHING` | `str` | Any string text | `'&7searching skin for &f{skin}&7...'` | Configures the technical `SKIN-SEARCHING` parameter for `MESSAGES.SKIN-SEARCHING` in `hide.yml`. |
| `MESSAGES.ALIAS-IN-USE` | `str` | Any string text | `'&cthat alias is already in use.'` | Configures the technical `ALIAS-IN-USE` parameter for `MESSAGES.ALIAS-IN-USE` in `hide.yml`. |
| `MESSAGES.SCRAMBLED` | `str` | Any string text | `'&ayour identity is now scrambled as...'` | Configures the technical `SCRAMBLED` parameter for `MESSAGES.SCRAMBLED` in `hide.yml`. |
| `MESSAGES.DISGUISED` | `str` | Any string text | `'&ayou are now disguised as &f{alias...'` | Configures the technical `DISGUISED` parameter for `MESSAGES.DISGUISED` in `hide.yml`. |
| `MESSAGES.REMOVED` | `str` | Any string text | `'&ayour hide state has been removed.'` | Configures the technical `REMOVED` parameter for `MESSAGES.REMOVED` in `hide.yml`. |
| `MESSAGES.ADMIN-REMOVED` | `str` | Any string text | `'&asuccessfully removed hide from &f...'` | Configures the technical `ADMIN-REMOVED` parameter for `MESSAGES.ADMIN-REMOVED` in `hide.yml`. |
| `MESSAGES.REMOVED-BY-ADMIN` | `str` | Any string text | `'&cyour hide state has been removed ...'` | Configures the technical `REMOVED-BY-ADMIN` parameter for `MESSAGES.REMOVED-BY-ADMIN` in `hide.yml`. |
| `MESSAGES.STATUS-NONE` | `str` | Any string text | `'&7hide status: &cinactive'` | Configures the technical `STATUS-NONE` parameter for `MESSAGES.STATUS-NONE` in `hide.yml`. |
| `MESSAGES.STATUS-ACTIVE` | `str` | Any string text | `'&7hide status: &a{mode} &8- &f{alia...'` | Configures the technical `STATUS-ACTIVE` parameter for `MESSAGES.STATUS-ACTIVE` in `hide.yml`. |
| `MESSAGES.CHECK` | `str` | Any string text | `'&bhide check # The text or value fo...'` | Configures the technical `CHECK` parameter for `MESSAGES.CHECK` in `hide.yml`. |
| `MESSAGES.PERMISSION-REMOVED` | `str` | Any string text | `'&cyour hide state was removed becau...'` | Configures the technical `PERMISSION-REMOVED` parameter for `MESSAGES.PERMISSION-REMOVED` in `hide.yml`. |

### 3. Practical Setup Example

```yaml
MESSAGES:
  # The text or value for Player Only. Available options: Any valid string text
  PLAYER-ONLY: '&conly players can use this command.'
  # The text or value for No Permission. Available options: Any valid string text
  NO-PERMISSION: '&cyou do not have permission.'
  # The text or value for Dependency Missing. Available options: Any valid string text
  DEPENDENCY-MISSING: '&chide requires protocollib to be installed and enabled.'
  # The text or value for Disabled. Available options: Any valid string text
  DISABLED: '&cthe hide feature is currently disabled.'
  # The text or value for In Combat. Available options: Any valid string text
  IN-COMBAT: '&cyou cannot change your hide state while in combat.'
  # The text or value for Cooldown. Available options: Any valid string text
  COOLDOWN: '&cwait &f{seconds}s &cbefore changing hide again.'
  # The text or value for Already Hidden. Available options: Any valid string text
  ALREADY-HIDDEN: '&cyou are already using that hide i
```

---

