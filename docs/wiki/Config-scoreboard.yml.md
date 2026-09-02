# Detailed Configuration & Setup Guide: `scoreboard.yml`

This is the official, 100% complete technical setup guide for `scoreboard.yml` in **UltimateDonutSMP**.
Each section details the exact commented setup code block, allowed option values, data types, default values, and in-depth functional behavior.

Rows typed entirely in capitals do not stay that way. Any line carrying a colour code or a
placeholder gets retyped in Title Case first, which is worth knowing before you write a title in
block capitals. [FAQ entry 16](FAQ) has the rule and the way around it.

---

## Section: `SCOREBOARD`

### 1. Commented Setup Code Example

```yaml
SCOREBOARD:
  # Enable or disable the animated sidebar scoreboard (true / false)
  ENABLED: true

  # Ticks between title animation frames (20 ticks = 1 second)
  TITLE-UPDATE-TICKS: 2

  # Enable column alignment for icons on the left side of scoreboard lines (true / false)
  ALIGN-ICON-COLUMN: true

  # Column width in pixels for icon alignment padding
  ICON-COLUMN-WIDTH: 10

  # Animated title frames displayed at the top of the scoreboard
  TITLE:
    - '&#0069d6&lE&#0374da&lc&#067fdf&lo&#0a8be3&ln&#0d96e7&lo&#10a1ec&lm&#13acf0&ly&#17b8f4&lS&#1ac3f9&lM&#1dcefd&lP'

  # Dynamic line templates rendered on the scoreboard
  LINES:
    - ''
    - '&#00FC00&l$ &fMoney &#00FC00%economy_nicestMoney%     '
    - '&#A303F9★ &fShards &#A303F9%economy_shards%     '
    - '&#FC0000🗡 &fKills &#FC0000%economy_kills%      '
    - '&#F97603☠ &fDeaths &#F97603%economy_deaths%   '
    - '&#00A4FC⌛ &fKeyall &#00A4FC%economy_keyall_countdown%'
    - '&#FCE300⌚ &fPlaytime &#FCE300%economy_playtime%   '
    - '{team}'
    - '{shard_cuboid}'
    - '{shard_booster}'
    - ''
    - '&7NA East &7(&#0069D6%economy_ping%ms&7)'

  # Formatting template for the team display line, only shown while the player is in a team
  TEAM: '&#00A4FC🪓 &fTeam &#00A4FC%economy_team%     '

  # Formatting template for the active shard booster display line
  SHARD-BOOSTER: '&#A303F9⚡ &fBooster &#A303F9%economy_booster_countdown%     '

  # Formatting template for the shard cuboid display line
  SHARD-CUBOID: '&#A303F9⌛ &fShards &#A303F9%economy_shard_cuboid_display%     '
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `SCOREBOARD.ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `SCOREBOARD` system. Set to `true` to enable, `false` to disable. |
| `SCOREBOARD.TITLE-UPDATE-TICKS` | `int` | Any valid integer number | `'2'` | Configures the technical `TITLE-UPDATE-TICKS` parameter for `SCOREBOARD.TITLE-UPDATE-TICKS` in `scoreboard.yml`. |
| `SCOREBOARD.ALIGN-ICON-COLUMN` | `bool` | `true`, `false` | `true` | Configures the technical `ALIGN-ICON-COLUMN` parameter for `SCOREBOARD.ALIGN-ICON-COLUMN` in `scoreboard.yml`. |
| `SCOREBOARD.ICON-COLUMN-WIDTH` | `int` | Any valid integer number | `'10'` | Configures the technical `ICON-COLUMN-WIDTH` parameter for `SCOREBOARD.ICON-COLUMN-WIDTH` in `scoreboard.yml`. |
| `SCOREBOARD.TITLE` | `list` | List of configured items/strings | `['&#0069d6&lE&#0374da&lc&#067fdf&lo&#0a8be3&ln&#0d96e7&lo&#10a1ec&lm&#13acf0&ly&#17b8f4&lS&#1ac3f9&lM&#1dcefd&lP']` | Configures the technical `TITLE` parameter for `SCOREBOARD.TITLE` in `scoreboard.yml`. |
| `SCOREBOARD.LINES` | `list` | List of configured items/strings | `[, &#00FC00&l$ &fMoney &#00FC00%economy_nicestMoney%     , &#A303F9★ &fShards &#A303F9%economy_shards%     ...]` | Sidebar lines, top to bottom. Five of them are also per-player switches under `/settings`: a line goes away for that player when they turn off Show Money, Show Shards, Show Kills, Show Deaths or Show Playtime. The placeholder inside a line is what ties it to a switch, so reworded lines keep working and a line carrying none of those placeholders is always shown. Show Scoreboard hides the sidebar outright. |
| `SCOREBOARD.TEAM` | `str` | Any string text | `'&#00A4FC🪓 &fTeam &#00A4FC%economy_t...'` | Formatting for the `{team}` entry listed in `SCOREBOARD.LINES`. The line only renders while the player is in a team, so anyone without one sees no Team line at all. To show it unconditionally, remove `{team}` from `LINES` and put the raw text containing `%economy_team%` there instead. |
| `SCOREBOARD.SHARD-BOOSTER` | `str` | Any string text | `'&#A303F9⚡ &fBooster &#A303F9%econom...'` | Configures the technical `SHARD-BOOSTER` parameter for `SCOREBOARD.SHARD-BOOSTER` in `scoreboard.yml`. |
| `SCOREBOARD.SHARD-CUBOID` | `str` | Any string text | `'&#A303F9⌛ &fShards &#A303F9%economy...'` | Configures the technical `SHARD-CUBOID` parameter for `SCOREBOARD.SHARD-CUBOID` in `scoreboard.yml`. |

### 3. Practical Setup Example

```yaml
SCOREBOARD:
  # Enable or disable the animated sidebar scoreboard (true / false)
  ENABLED: true

  # Ticks between title animation frames (20 ticks = 1 second)
  TITLE-UPDATE-TICKS: 2

  # Enable column alignment for icons on the left side of scoreboard lines (true / false)
  ALIGN-ICON-COLUMN: true

  # Column width in pixels for icon alignment padding
  ICON-COLUMN-WIDTH: 10

  # Animated title frames displayed at the top of the scoreboard
  TITLE:
    - '&#0069d6&lE&#0374da&lc&#067fdf&lo&#0a8be3&ln&#0d96e7&lo&#10a1ec&lm&#13acf0&ly&#17b8f4&lS&#1ac3f9&lM&#1dcefd&lP'

  # Dynamic line templates rendered on the scoreboard
  LINES:
    - ''
    - '&#00FC00&l$ &fMoney &#00FC00%economy_nicestMoney%     '
    - '&#A303F9★ &fShards &#A303F9%economy_shards%     '
    - '&#FC0000🗡 &fKills &#FC0000%economy_kills%      '
    - '&#F97603☠ &fDeaths &#F97603%economy_deaths%   '
    - '&#00A4FC⌛ &fKeyall &#00A4FC%economy_keyall_countdown%'
    - '&#FCE300⌚ &fPlaytime &#FCE300%economy_playtime%   '
    - '{team}'
    - '{shard_cuboid}'
    - '{shard_booster}'
    - ''
    - '&7NA East &7(&#0069D6%economy_ping%ms&7)'

  # Formatting template for the team display line, only shown while the player is in a team
  TEAM: '&#00A4FC🪓 &fTeam &#00A4FC%economy_team%     '

  # Formatting template for the active shard booster display line
  SHARD-BOOSTER: '&#A303F9⚡ &fBooster &#A303F9%economy_booster_countdown%     '

  # Formatting template for the shard cuboid display line
  SHARD-CUBOID: '&#A303F9⌛ &fShards &#A303F9%economy_shard_cuboid_display%     '
```

---

