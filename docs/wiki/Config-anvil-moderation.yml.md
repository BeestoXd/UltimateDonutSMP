# Detailed Configuration & Setup Guide: `anvil-moderation.yml`

This is the official, 100% complete technical setup guide for `anvil-moderation.yml` in **UltimateDonutSMP**.
Each section details the exact commented setup code block, allowed option values, data types, default values, and in-depth functional behavior.

---

## Section: `banned-words`

### 1. Commented Setup Code Example

```yaml
banned-words:
  - example
  - bannedword
  - inappropriateword
  - offensivephrase
  - slur

# Progressive punishments executed when players attempt to rename items using banned words
# Options: Commands executed sequentially per offense count (%player% placeholder supported)
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `banned-words` | `list` | List of configured items/strings | `[example, bannedword, inappropriateword...]` | Configures the technical `banned-words` parameter for `banned-words` in `anvil-moderation.yml`. |

### 3. Practical Setup Example

```yaml
banned-words:
  - example
  - bannedword
  - inappropriateword
  - offensivephrase
  - slur

# Progressive punishments executed when players attempt to rename items using banned words
# Options: Commands executed sequentially per offense count (%player% placeholder supported)
```

---

## Section: `punishments`

### 1. Commented Setup Code Example

```yaml
punishments:
  - mute %player% 30d Anvil inappropriate content - 1st offense
  - tempban %player% 7d Anvil inappropriate content - 2nd offense
  - tempban %player% 14d Anvil inappropriate content - 3rd offense
  - tempban %player% 30d Anvil inappropriate content - 4th offense
  - tempban %player% 50d Anvil inappropriate content - 5th offense

# Persistent player offense tracking data (managed automatically by plugin)
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `punishments` | `list` | List of configured items/strings | `[mute %player% 30d Anvil inappropriate content - 1st offense, tempban %player% 7d Anvil inappropriate content - 2nd offense, tempban %player% 14d Anvil inappropriate content - 3rd offense...]` | Configures the technical `punishments` parameter for `punishments` in `anvil-moderation.yml`. |

### 3. Practical Setup Example

```yaml
punishments:
  - mute %player% 30d Anvil inappropriate content - 1st offense
  - tempban %player% 7d Anvil inappropriate content - 2nd offense
  - tempban %player% 14d Anvil inappropriate content - 3rd offense
  - tempban %player% 30d Anvil inappropriate content - 4th offense
  - tempban %player% 50d Anvil inappropriate content - 5th offense

# Persistent player offense tracking data (managed automatically by plugin)
```

---

## Section: `players`

### 1. Commented Setup Code Example

```yaml
players: {}
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |

### 3. Practical Setup Example

```yaml
players: {}
```

---

