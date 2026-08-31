# Placeholders & Integrations Guide

UltimateDonutSMP includes native PlaceholderAPI expansions, soft-dependency hooks, and network integrations.

---

## PlaceholderAPI Expansions Catalog

UltimateDonutSMP registers multiple PlaceholderAPI expansion prefixes (`%economy_*%`, `%economylb_*%`, `%economyrank_*%`, `%hide_*%`, `%uds_*%`, `%ultimatedonutsmp_*%`).

### 1. Main Economy & Stats Expansion (`%economy_*%`, `%uds_*%`, `%ultimatedonutsmp_*%`)

#### Money & Currency Placeholders
| Placeholder | Output Description |
| :--- | :--- |
| `%economy_money%` | Raw player money balance (e.g. `1500.50`) |
| `%economy_nicestMoney%` | Compact money format (e.g. `1.5K`, `2.3M`) |
| `%economy_money_short%` | Short money format |
| `%economy_money_formatted%` | Formatted money with configured currency symbol |
| `%economy_money_symbol%` | Configured currency symbol (e.g. `$`) |
| `%economy_money_symbol_color%` | Currency symbol color code |
| `%economy_money_color%` | Money value color code |
| `%economy_money_name%` | Money currency singular name |

#### Shards Placeholders
| Placeholder | Output Description |
| :--- | :--- |
| `%economy_shards%` | Raw shard count |
| `%economy_nicestShards%` | Compact shard count format |
| `%economy_shards_short%` | Short shard format |
| `%economy_shards_formatted%` | Formatted shard count with symbol |
| `%economy_shards_symbol%` | Configured shard symbol (e.g. `✦`) |

#### Player Stats & Timers
| Placeholder | Output Description |
| :--- | :--- |
| `%economy_kills%` | Player kill count |
| `%economy_deaths%` | Player death count |
| `%economy_playtime%` | Formatted player playtime |
| `%economy_team%` | Team name (or `none`) |
| `%economy_ping%` | Player latency in milliseconds |
| `%economy_username%` | Player name (respects vanish/hide settings) |
| `%economy_keyall_countdown%` | Time remaining until next Key-All broadcast |
| `%economy_booster_countdown%` | Time remaining for active server booster |
| `%economy_rtp_countdown%` | Time remaining for RTP zone cooldown |
| `%economy_billford_countdown%` | Time remaining until Billford trade rotation |
| `%economy_shard_cuboid_display%` | Shard cuboid HUD action text |
| `%economy_shard_cuboid_status%` | Shard cuboid state (`inside` / `outside`) |
| `%economy_shard_cuboid_name%` | Active Shard Cuboid region name |
| `%economy_coords%` | Player display coordinates `(X, Y, Z)` |

---

### 2. Leaderboard Expansion (`%economylb_*%` or `%economy_top_*%`)

Used for scoreboards, holograms, and tablists to display top rankings (`1-10`):

| Syntax | Example | Description |
| :--- | :--- | :--- |
| `%economylb_<type>_<rank>_name%` | `%economylb_money_1_name%` | Player name at rank 1 |
| `%economylb_<type>_<rank>_value%` | `%economylb_money_1_value%` | Formatted score value at rank 1 |
| `%economylb_<type>_<rank>_display%` | `%economylb_money_1_display%` | Complete leaderboard row |

*Supported Types*: `money`, `shards`, `kills`, `deaths`, `playtime`, `blocksPlaced`, `blocksBroken`, `mobsKilled`, `killStreak`, `highestKillStreak`, `moneySpent`, `moneyMade`, `bounties`.

> `bounties` ranks players by the bounty currently placed on their head and only lists players who actually have one, so a rank past the last active bounty renders as `none`.

---

### 3. Personal Rank Expansion (`%economyrank_*%`)

Displays the player's personal numerical leaderboard rank, using the same type names as the leaderboard expansion above:
- `%economyrank_money%` – Player's money leaderboard rank
- `%economyrank_shards%` – Player's shards leaderboard rank
- `%economyrank_kills%` – Player's kills leaderboard rank
- `%economyrank_deaths%` – Player's deaths leaderboard rank
- `%economyrank_playtime%` – Player's playtime leaderboard rank
- `%economyrank_bounties%` – Player's bounty leaderboard rank (`0` when no bounty is placed on them)

Any unranked player returns `0`.

---

### 4. Hide & Disguise Expansion (`%hide_*%`)

Used when Staff Mode or Streamer/Disguise mode is enabled:
- `%hide_active%` – Returns `true` or `false` if player is in disguise/hide mode
- `%hide_name%` / `%hide_public_name%` – Public display name (alias if hidden, real name if not)
- `%hide_plain_name%` – Plain unformatted public name
- `%hide_mode%` – Current disguise mode (`NONE`, `DISGUISE`, `STREAMER`)
- `%hide_alias%` – Active fake nickname
- `%hide_skin%` – Active skin username

---

### 5. Ranked PvP Arena Expansion (`%pvp_*%`)

Registered by the [ranked PvP arena](Ranked-PvP-Arena). Every number the arena tracks is published here rather than drawn by the plugin, so TAB keeps owning the nametag and any scoreboard or chat plugin can read the same values.

| Placeholder | Output Description |
| :--- | :--- |
| `%pvp_rank%` | Rank display text, e.g. `HT5` |
| `%pvp_rank_id%` | Rank id without colour codes |
| `%pvp_elo%` | Current Elo |
| `%pvp_level%` | Current PvP level |
| `%pvp_xp%` | XP toward the next level |
| `%pvp_next_xp%` | XP the next level costs |
| `%pvp_kills%` | Arena kills |
| `%pvp_deaths%` | Arena deaths |
| `%pvp_kd%` | Kill/death ratio to two decimals |
| `%pvp_streak%` | Current kill streak |
| `%pvp_best_streak%` | Best kill streak |
| `%pvp_joins%` | Times the player has entered the arena |
| `%pvp_in_arena%` | `true` while the player is inside the arena |
| `%pvp_arena%` | Configured arena name |
| `%pvp_arena_reset%` | Countdown to the next reset, in `SCOREBOARD.RESET_FORMAT` |
| `%pvp_arena_players%` | How many players are in the arena right now |

A typical TAB nametag format:

```
%pvp_rank% | %luckperms-prefix% %player%
```

The bundled [Discord bot example](https://github.com/BeestoXd/UltimateDonutSMP/tree/main/discord-bot-example)
reads the same ranked data straight from the database rather than through placeholders, and adds
`/mystats`, `/tier stats`, `/tier leaderboard`, `/sync` and `/unsync` along with a match result
posted after every ranked fight. See [Ranked PvP Arena](Ranked-PvP-Arena) for the linking flow.

---

### 6. Punishment & Expiry Message Placeholders

Available for config and language message templates (`messages.yml` & `languages/*.yml`) when issuing punishments (`/offend`, `/ban`, `/mute`, etc.) or sending kick/mute screens:

| Category | Supported Placeholders | Description / Output Example |
| :--- | :--- | :--- |
| **Expiration** | `%expires_at%`, `{expires_at}`, `%expiration%`, `{expiration}`, `%expiry%`, `{expiry}`, `%expires%`, `{expires}`, `%duration%`, `{duration}`, `%nicest_expiration%`, `{nicest_expiration}` | Formatted remaining time (e.g. `30d 0h 0m`, `3d 0h 0m`, `15m 30s`) or `Permanent` / `Expired` |
| **Issuer / Staff** | `%issuer%`, `{issuer}`, `%staff%`, `{staff}`, `%by%`, `{by}` | Moderator name who issued punishment (or `Console` / `unknown`) |
| **Reason** | `%reason%`, `{reason}` | Configured or specified punishment reason string |
| **Target Player** | `%player%`, `{player}`, `%target%`, `{target}` | Target player username |
| **Punishment Details** | `%id%`, `{id}`, `%type%`, `{type}` | Punishment database ID (`#101`) and type (`BAN`, `MUTE`, `WARN`, `KICK`, `BLACKLIST`) |

---

## Soft Dependencies & Integrations

UltimateDonutSMP seamlessly hooks into the following plugins when installed:

1. **Vault**: Registers UltimateDonutSMP as the primary economy provider for third-party plugins.
2. **LuckPerms**: Inherits group ranks and checks permission nodes dynamically.
3. **ProtocolLib**: Used for packet-level entity lures, fast crystal placements, and disguise rendering.
4. **Apollo (Lunar Client)**: Displays custom waypoints, team member markers, and combat cooldown indicators on Lunar Client.
5. **SkinsRestorer**: Preserves custom player skin textures in GUI menus and head skulls.
6. **Multiverse-Core**: Inherits world safety flags and spawn points.
7. **Floodgate (Bedrock)**: Fixes form GUI layouts for Bedrock Geyser players joining from mobile or consoles.
8. **Simple Voice Chat**: Gates the microphone behind the consent menu. Players who have not accepted the policy can still hear a voice channel, but their microphone audio is dropped before anyone receives it. Without Simple Voice Chat installed the menu still records an answer, it just has nothing to gate.
