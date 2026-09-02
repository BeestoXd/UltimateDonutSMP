# Detailed Configuration & Setup Guide: `offenses.yml`

This is the technical setup guide for `offenses.yml` in **UltimateDonutSMP**.
The file holds the preset offences staff pick from, and the escalating punishment tiers each one
runs through. 61 offences ship with it, covering advertising, cheating, chat behaviour and the
rest; the list is meant to be edited rather than treated as fixed.

The file opens with its own summary of the syntax, which is worth reading before you change
anything:

```yaml
# Configuration file for preset offenses and escalating punishment tiers.
# Syntax per offense:
#   <key>:
#     name: "Display Name"
#     type: BAN | MUTE | WARN | KICK
#     wipe: false  # optional, defaults to false
#     durations:
#       - "30d"  # 1st offense
#       - "60d"  # 2nd offense
#       - "perm" # 3rd offense (and beyond)
#
# wipe only fires when the offense actually bans someone. It clears their stats, balance, homes,
# ender chest, crate keys, auctions, orders and the rest of their progress, the same thing
# /playerwipe does, and there is no undo. Their punishment history, IP history and any spawners
# they placed are kept. A tier of "0s" is issued as a warning rather than a ban, so it does not
# wipe, and neither do MUTE, WARN or KICK offenses.
```

---

## Section: `offenses`

### 1. Commented Setup Code Example

```yaml
offenses:
  advertising:
    name: "Advertising"
    type: BAN
    durations:
      - "30d"

  advertising-invite-rewards:
    name: "Advertising Invite Rewards"
    type: BAN
    durations:
      - "30d"

  advertising-irl-trade:
    name: "Advertising IRL Trade"
    type: BAN
    durations:
      - "3d"

  alt-limit:
    name: "Alt Limit Violation"
    type: BAN
    durations:
      - "perm"
  # ... 57 more offences, each in the same shape
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `offenses.<key>` | `section` | Any lower case id | 61 shipped | One offence. The key is the id staff type, so `/offend <player> ban-evasion` picks the `ban-evasion` entry. It is lower cased when read, and it is also what the punishment record stores, so renaming a key detaches it from the history already filed under the old name. |
| `offenses.<key>.name` | `str` | Any string text | the key itself | The wording shown to players and staff. Left out, the raw key is used instead, so `ban-evasion` would appear in place of `Ban Evasion`. |
| `offenses.<key>.type` | `str` | `BAN`, `MUTE`, `WARN`, `KICK` | `BAN` | What the offence issues. Case does not matter. Anything the plugin does not recognise silently falls back to `BAN`, so a typo here punishes harder rather than failing loudly. |
| `offenses.<key>.durations` | `list` | Duration strings, or `perm` | none | One entry per offence tier, in order. The first is the first time somebody commits it, the second the next time, and so on. Once the list runs out the last entry repeats forever, so ending on `perm` makes the third strike permanent. A plain string works as well as a list where an offence only ever has one tier. |
| `offenses.<key>.wipe` | `bool` | `true`, `false` | `false` | Whether the punishment also wipes the player. Read under any casing, since the rest of the file is lower case. See the warning below before switching it on anywhere. |

Durations take the usual suffixes, `30s`, `15m`, `2h`, `5d`, and can be combined as `5d 15m 30s`.
`perm` never expires. A tier of `0s`, or a plain `0`, is issued as a warning rather than a ban,
which is how you
give somebody a formal first strike that shows in their history without keeping them out.

`wipe` deserves care. It only fires when the offence actually bans somebody, and it clears their
stats, balance, homes, ender chest, crate keys, auctions, orders and the rest of their progress,
the same thing `/playerwipe` does. There is no undo. Punishment history, IP history and any
spawners they placed survive it. Because a `0s` tier is a warning rather than a ban it does not
wipe, and neither does a `MUTE`, `WARN` or `KICK` offence however the flag is set.

### 3. Practical Setup Example

```yaml
offenses:
  # three strikes, then it stops expiring
  chat-spam:
    name: "Chat Spam"
    type: MUTE
    durations:
      - "1h"
      - "12h"
      - "7d"

  # a formal first warning that costs nothing, then a real ban
  team-griefing:
    name: "Team Griefing"
    type: BAN
    durations:
      - "0s"
      - "14d"
      - "perm"
```

---
