# Detailed Configuration & Setup Guide: `discord.yml`

This is the official, 100% complete technical setup guide for `discord.yml` in **UltimateDonutSMP**.
Each section details the exact commented setup code block, allowed option values, data types, default values, and in-depth functional behavior.

---

## Section: `WEBHOOKS`

### 1. Commented Setup Code Example

```yaml
WEBHOOKS:
  # Determines whether Enabled is enabled or disabled. Available options: true, false
  ENABLED: true
  # The text or value for Url. Available options: Any valid string text
  URL: https://discord.com/api/webhooks/your_webhook_here
  # The text or value for Avatar Api. Available options: Any valid string text
  AVATAR_API: https://visage.surgeplay.com/face/128/%uuid_no_dash%
  # The text or value for Model Api. Available options: Any valid string text
  MODEL_API: https://visage.surgeplay.com/full/384/%uuid_no_dash%
  # The text or value for Bust Api. Available options: Any valid string text
  BUST_API: https://visage.surgeplay.com/bust/384/%uuid_no_dash%
  MESSAGES:
    # Configuration section for Ban.
    BAN:
      # Determines whether Enabled is enabled or disabled. Available options: true, false
      ENABLED: true
      TITLE: Player Banned - %player%
      # The text or value for Color. Available options: Any valid string text
      COLOR: '#FF0000'
      # The text or value for Description. Available options: Any valid string text
      DESCRIPTION: ':hammer: **Punishment Type:** Ban

        # The text or value for # The Text Or Mode For **Player. Available Options. Available options: Any valid string text
        # The text or mode for **Player. Available options: Any string text **Player:**
        %player%

        # The text or value for # The Text Or Mode For **Staff. Available Options. Available options: Any valid string text
        # The text or mode for **Staff. Available options: Any string text **Staff:**
        %staff%

        # The text or value for # The Text Or Mode For **Reason. Available Options. Available options: Any valid string text
        # The text or mode for **Reason. Available options: Any string text **Reason:**
        ||%reason%||

        # The text or value for # The Text Or Mode For **Duration. Available Options. Available options: Any valid string text
        # The text or mode for **Duration. Available options: Any string text **Duration:**
        %duration%

        # The text or value for # The Text Or Mode For **Date. Available Options. Available options: Any valid string text
        # The text or mode for **Date. Available options: Any string text **Date:**
        %date%

        # The text or value for # The Text Or Mode For **Id. Available Options. Available options: Any valid string text
        # The text or mode for **Id. Available options: Any string text **ID:** `%id%`

        '
      # The text or value for Thumbnail. Available options: Any valid string text
      THUMBNAIL: '%skin_bust%'
      # The text or value for Author Name. Available options: Any valid string text
      AUTHOR_NAME: Ban System
      # The text or value for Footer. Available options: Any valid string text
      FOOTER: Punishment issued via server
    # Configuration section for Mute.
    MUTE:
      # Determines whether Enabled is enabled or disabled. Available options: true, false
      ENABLED: true
      TITLE: Player Muted - %player%
      # The text or value for Color. Available options: Any valid string text
      COLOR: '#FFFF00'
      # The text or value for Description. Available options: Any valid string text
      DESCRIPTION: ':mute: **Punishment Type:** Mute

        # The text or value for # The Text Or Mode For **Player. Available Options. Available options: Any valid string text
        # The text or mode for **Player. Available options: Any string text **Player:**
        %player%

        # The text or value for # The Text Or Mode For **Staff. Available Options. Available options: Any valid string text
        # The text or mode for **Staff. Available options: Any string text **Staff:**
        %staff%

        # The text or value for # The Text Or Mode For **Reason. Available Options. Available options: Any valid string text
        # The text or mode for **Reason. Available options: Any string text **Reason:**
        ||%reason%||

        # The text or value for # The Text Or Mode For **Duration. Available Options. Available options: Any valid string text
        # The text or mode for **Duration. Available options: Any string text **Duration:**
        %duration%

        # The text or value for # The Text Or Mode For **Date. Available Options. Available options: Any valid string text
        # The text or mode for **Date. Available options: Any string text **Date:**
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `WEBHOOKS.ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `WEBHOOKS` system. Set to `true` to enable, `false` to disable. |
| `WEBHOOKS.URL` | `str` | Any string text | `'https://discord.com/api/webhooks/yo...'` | Configures the technical `URL` parameter for `WEBHOOKS.URL` in `discord.yml`. |
| `WEBHOOKS.AVATAR_API` | `str` | Any string text | `'https://visage.surgeplay.com/face/1...'` | Configures the technical `AVATAR_API` parameter for `WEBHOOKS.AVATAR_API` in `discord.yml`. |
| `WEBHOOKS.MODEL_API` | `str` | Any string text | `'https://visage.surgeplay.com/full/3...'` | Configures the technical `MODEL_API` parameter for `WEBHOOKS.MODEL_API` in `discord.yml`. |
| `WEBHOOKS.BUST_API` | `str` | Any string text | `'https://visage.surgeplay.com/bust/3...'` | Configures the technical `BUST_API` parameter for `WEBHOOKS.BUST_API` in `discord.yml`. |
| `WEBHOOKS.MESSAGES.BAN.ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `WEBHOOKS` system. Set to `true` to enable, `false` to disable. |
| `WEBHOOKS.MESSAGES.BAN.TITLE` | `str` | Any string text | `'Player Banned - %player%'` | Configures the technical `TITLE` parameter for `WEBHOOKS.MESSAGES.BAN.TITLE` in `discord.yml`. |
| `WEBHOOKS.MESSAGES.BAN.COLOR` | `str` | Any string text | `'#FF0000'` | Configures the technical `COLOR` parameter for `WEBHOOKS.MESSAGES.BAN.COLOR` in `discord.yml`. |
| `WEBHOOKS.MESSAGES.BAN.DESCRIPTION` | `str` | Any string text | `':hammer: **Punishment Type:** Ban #...'` | Configures the technical `DESCRIPTION` parameter for `WEBHOOKS.MESSAGES.BAN.DESCRIPTION` in `discord.yml`. |
| `WEBHOOKS.MESSAGES.BAN.THUMBNAIL` | `str` | Any string text | `'%skin_bust%'` | Configures the technical `THUMBNAIL` parameter for `WEBHOOKS.MESSAGES.BAN.THUMBNAIL` in `discord.yml`. |
| `WEBHOOKS.MESSAGES.BAN.AUTHOR_NAME` | `str` | Any string text | `'Ban System'` | Configures the technical `AUTHOR_NAME` parameter for `WEBHOOKS.MESSAGES.BAN.AUTHOR_NAME` in `discord.yml`. |
| `WEBHOOKS.MESSAGES.BAN.FOOTER` | `str` | Any string text | `'Punishment issued via server'` | Configures the technical `FOOTER` parameter for `WEBHOOKS.MESSAGES.BAN.FOOTER` in `discord.yml`. |
| `WEBHOOKS.MESSAGES.MUTE.ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `WEBHOOKS` system. Set to `true` to enable, `false` to disable. |
| `WEBHOOKS.MESSAGES.MUTE.TITLE` | `str` | Any string text | `'Player Muted - %player%'` | Configures the technical `TITLE` parameter for `WEBHOOKS.MESSAGES.MUTE.TITLE` in `discord.yml`. |
| `WEBHOOKS.MESSAGES.MUTE.COLOR` | `str` | Any string text | `'#FFFF00'` | Configures the technical `COLOR` parameter for `WEBHOOKS.MESSAGES.MUTE.COLOR` in `discord.yml`. |
| `WEBHOOKS.MESSAGES.MUTE.DESCRIPTION` | `str` | Any string text | `':mute: **Punishment Type:** Mute # ...'` | Configures the technical `DESCRIPTION` parameter for `WEBHOOKS.MESSAGES.MUTE.DESCRIPTION` in `discord.yml`. |
| `WEBHOOKS.MESSAGES.MUTE.THUMBNAIL` | `str` | Any string text | `'%skin_bust%'` | Configures the technical `THUMBNAIL` parameter for `WEBHOOKS.MESSAGES.MUTE.THUMBNAIL` in `discord.yml`. |
| `WEBHOOKS.MESSAGES.MUTE.AUTHOR_NAME` | `str` | Any string text | `'Moderation System'` | Configures the technical `AUTHOR_NAME` parameter for `WEBHOOKS.MESSAGES.MUTE.AUTHOR_NAME` in `discord.yml`. |
| `WEBHOOKS.MESSAGES.MUTE.FOOTER` | `str` | Any string text | `'Chat restriction applied'` | Configures the technical `FOOTER` parameter for `WEBHOOKS.MESSAGES.MUTE.FOOTER` in `discord.yml`. |
| `WEBHOOKS.MESSAGES.WARN.ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `WEBHOOKS` system. Set to `true` to enable, `false` to disable. |
| `WEBHOOKS.MESSAGES.WARN.TITLE` | `str` | Any string text | `'Player Warned - %player%'` | Configures the technical `TITLE` parameter for `WEBHOOKS.MESSAGES.WARN.TITLE` in `discord.yml`. |
| `WEBHOOKS.MESSAGES.WARN.COLOR` | `str` | Any string text | `'#FFA500'` | Configures the technical `COLOR` parameter for `WEBHOOKS.MESSAGES.WARN.COLOR` in `discord.yml`. |
| `WEBHOOKS.MESSAGES.WARN.DESCRIPTION` | `str` | Any string text | `':warning: **Punishment Type:** Warn...'` | Configures the technical `DESCRIPTION` parameter for `WEBHOOKS.MESSAGES.WARN.DESCRIPTION` in `discord.yml`. |
| `WEBHOOKS.MESSAGES.WARN.THUMBNAIL` | `str` | Any string text | `'%skin_bust%'` | Configures the technical `THUMBNAIL` parameter for `WEBHOOKS.MESSAGES.WARN.THUMBNAIL` in `discord.yml`. |
| `WEBHOOKS.MESSAGES.WARN.AUTHOR_NAME` | `str` | Any string text | `'Moderation System'` | Configures the technical `AUTHOR_NAME` parameter for `WEBHOOKS.MESSAGES.WARN.AUTHOR_NAME` in `discord.yml`. |
| `WEBHOOKS.MESSAGES.WARN.FOOTER` | `str` | Any string text | `'Warning issued'` | Configures the technical `FOOTER` parameter for `WEBHOOKS.MESSAGES.WARN.FOOTER` in `discord.yml`. |
| `WEBHOOKS.MESSAGES.KICK.ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `WEBHOOKS` system. Set to `true` to enable, `false` to disable. |
| `WEBHOOKS.MESSAGES.KICK.TITLE` | `str` | Any string text | `'Player Kicked - %player%'` | Configures the technical `TITLE` parameter for `WEBHOOKS.MESSAGES.KICK.TITLE` in `discord.yml`. |
| `WEBHOOKS.MESSAGES.KICK.COLOR` | `str` | Any string text | `'#FF6347'` | Configures the technical `COLOR` parameter for `WEBHOOKS.MESSAGES.KICK.COLOR` in `discord.yml`. |
| `WEBHOOKS.MESSAGES.KICK.DESCRIPTION` | `str` | Any string text | `':boot: **Punishment Type:** Kick # ...'` | Configures the technical `DESCRIPTION` parameter for `WEBHOOKS.MESSAGES.KICK.DESCRIPTION` in `discord.yml`. |
| *(10 additional sub-keys configured in section)* | | | | |

### 3. Practical Setup Example

```yaml
WEBHOOKS:
  # Determines whether Enabled is enabled or disabled. Available options: true, false
  ENABLED: true
  # The text or value for Url. Available options: Any valid string text
  URL: https://discord.com/api/webhooks/your_webhook_here
  # The text or value for Avatar Api. Available options: Any valid string text
  AVATAR_API: https://visage.surgeplay.com/face/128/%uuid_no_dash%
  # The text or value for Model Api. Available options: Any valid string text
  MODEL_API: https://visage.surgeplay.com/full/384/%uuid_no_dash%
  # The text or value for Bust Api. Available options: Any valid string text
  BUST_API: https://visage.surgeplay.com/bust/384/%uuid_no_dash%
  MESSAGES:
    # Configuration section for Ban.
    BAN:
      # Determines whether Enabled is enabled or disabled. Available options: true, false
      ENABLED: true
      TITLE: Player Banned - %player%
      # The text or value for Color. Available options: Any valid string text
      COLOR: '#FF0000'
      # The text or
```

---

