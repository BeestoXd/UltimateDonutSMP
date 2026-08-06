# Detailed Configuration & Setup Guide: `server-wipe.yml`

This is the official, 100% complete technical setup guide for `server-wipe.yml` in **UltimateDonutSMP**.
Each section details the exact commented setup code block, allowed option values, data types, default values, and in-depth functional behavior.

---

## Section: `ENABLED`

### 1. Commented Setup Code Example

```yaml
ENABLED: true

# List of world names to be reset during a server wipe (e.g., ["world", "world_nether", "world_the_end"])
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `ENABLED` system. Set to `true` to enable, `false` to disable. |

### 3. Practical Setup Example

```yaml
ENABLED: true

# List of world names to be reset during a server wipe (e.g., ["world", "world_nether", "world_the_end"])
```

---

## Section: `RESET-WORLDS`

### 1. Commented Setup Code Example

```yaml
RESET-WORLDS: []

# List of world names protected from being reset during a server wipe (e.g., ["spawn", "lobby"])
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `RESET-WORLDS` | `list` | List of configured items/strings | `[]` | Configures the technical `RESET-WORLDS` parameter for `RESET-WORLDS` in `server-wipe.yml`. |

### 3. Practical Setup Example

```yaml
RESET-WORLDS: []

# List of world names protected from being reset during a server wipe (e.g., ["spawn", "lobby"])
```

---

## Section: `PROTECTED-WORLDS`

### 1. Commented Setup Code Example

```yaml
PROTECTED-WORLDS: []

# Time-to-live duration in seconds for admin confirmation tokens before expiration
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `PROTECTED-WORLDS` | `list` | List of configured items/strings | `[]` | Configures the technical `PROTECTED-WORLDS` parameter for `PROTECTED-WORLDS` in `server-wipe.yml`. |

### 3. Practical Setup Example

```yaml
PROTECTED-WORLDS: []

# Time-to-live duration in seconds for admin confirmation tokens before expiration
```

---

## Section: `TOKEN-TTL-SECONDS`

### 1. Commented Setup Code Example

```yaml
TOKEN-TTL-SECONDS: 300

# Subfolder path where world backups are saved before wiping
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `TOKEN-TTL-SECONDS` | `int` | Any valid integer number | `'300'` | Configures the technical `TOKEN-TTL-SECONDS` parameter for `TOKEN-TTL-SECONDS` in `server-wipe.yml`. |

### 3. Practical Setup Example

```yaml
TOKEN-TTL-SECONDS: 300

# Subfolder path where world backups are saved before wiping
```

---

## Section: `BACKUP-DIRECTORY`

### 1. Commented Setup Code Example

```yaml
BACKUP-DIRECTORY: server-wipe-backups

# Notification messages broadcast during server reset maintenance
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `BACKUP-DIRECTORY` | `str` | Any string text | `'server-wipe-backups'` | Configures the technical `BACKUP-DIRECTORY` parameter for `BACKUP-DIRECTORY` in `server-wipe.yml`. |

### 3. Practical Setup Example

```yaml
BACKUP-DIRECTORY: server-wipe-backups

# Notification messages broadcast during server reset maintenance
```

---

## Section: `MESSAGES`

### 1. Commented Setup Code Example

```yaml
MESSAGES:
  # Maintenance message shown when players attempt to log in during a wipe
  MAINTENANCE: '&cthe server is preparing a season reset. try again after the restart.'
  # Kick message shown to all online players when a wipe sequence begins
  KICK: '&ca season reset is starting. the server will restart shortly.'
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `MESSAGES.MAINTENANCE` | `str` | Any string text | `'&cthe server is preparing a season ...'` | Configures the technical `MAINTENANCE` parameter for `MESSAGES.MAINTENANCE` in `server-wipe.yml`. |
| `MESSAGES.KICK` | `str` | Any string text | `'&ca season reset is starting. the s...'` | Configures the technical `KICK` parameter for `MESSAGES.KICK` in `server-wipe.yml`. |

### 3. Practical Setup Example

```yaml
MESSAGES:
  # Maintenance message shown when players attempt to log in during a wipe
  MAINTENANCE: '&cthe server is preparing a season reset. try again after the restart.'
  # Kick message shown to all online players when a wipe sequence begins
  KICK: '&ca season reset is starting. the server will restart shortly.'
```

---

