# Detailed Configuration & Setup Guide: `network.yml`

This is the official, 100% complete technical setup guide for `network.yml` in **UltimateDonutSMP**.
Each section details the exact commented setup code block, allowed option values, data types, default values, and in-depth functional behavior.

---

## Section: `NETWORK`

### 1. Commented Setup Code Example

```yaml
NETWORK:
  # Enable or disable the cross-server network system globally (true / false)
  ENABLED: true

  # Enable cross-server staff chat sync via Redis (true / false)
  STAFF_CHAT_ENABLED: true

  # Enable cross-server helpop notification sync (true / false)
  HELPOP_ENABLED: true

  # Enable cross-server report notification sync (true / false)
  REPORT_ENABLED: true

  # Enable cross-server staff join/leave notifications (true / false)
  STAFF_JOIN_LEAVE_ENABLED: true

  # Enable cross-server status heartbeat monitoring (true / false)
  SERVER_STATUS_ENABLED: true

  # Unique server identifier for this local server instance
  LOCAL_SERVER_ID: crystal

  # User-friendly server display name
  LOCAL_DISPLAY_NAME: Crystal

  # Redis pub/sub channel for staff chat messages
  REDIS_CHANNEL: ultimatedonutsmp:staff-chat

  # Redis pub/sub channel for helpop alerts
  HELPOP_REDIS_CHANNEL: ultimatedonutsmp:staff-alerts

  # Redis pub/sub channel for player reports
  REPORT_REDIS_CHANNEL: ultimatedonutsmp:staff-alerts

  # Broadcast staff chat locally if Redis connection fails (true / false)
  SEND_LOCAL_FALLBACK_ON_REDIS_ERROR: true

  # Broadcast staff alerts locally if Redis connection fails (true / false)
  STAFF_ALERTS_LOCAL_FALLBACK_ON_REDIS_ERROR: true

  # Warn sending player if staff alert Redis delivery fails (true / false)
  STAFF_ALERTS_WARN_SENDER_ON_REDIS_ERROR: false

  # Log staff chat messages to local server console (true / false)
  LOG_TO_CONSOLE: true

  # Log staff alerts to local server console (true / false)
  STAFF_ALERTS_LOG_TO_CONSOLE: true

  # Maximum allowed staff chat message length (in characters)
  MAX_MESSAGE_LENGTH: 512

  # Maximum allowed report/helpop reason text length (in characters)
  STAFF_ALERTS_MAX_REASON_LENGTH: 256

  # Cooldown between helpop submissions per player (in seconds)
  HELPOP_COOLDOWN_SECONDS: 30

  # Cooldown between report submissions per player (in seconds)
  REPORT_COOLDOWN_SECONDS: 60

  # Message format for server online/offline status broadcasts
  SERVER_STATUS: '&6%server% &eis now %status%&e.'

  # Message format for cross-server staff chat messages
  STAFF_CHAT: '&8[&dNetwork&8] &7[%server%] &e%player%&8: &f%message%'

  # Message format for staff member server join alert
  STAFF_JOIN: '&8[&a+&8] &a%player% &7joined &b%server%'

  # Message format for staff member server leave alert
  STAFF_LEAVE: '&8[&c-&8] &a%player% &7left &b%server%'

# Network status monitoring & HTTP endpoint configuration
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `NETWORK.ENABLED` | `bool` | `true`, `false` | `true` | Master switch for everything on this page. Turning it off does more than stop cross-server sync: `/staffchat`, `/helpop` and `/report` all start answering with their disabled message. A single server with no Redis should leave this on and switch off the individual features it does not want. |
| `NETWORK.STAFF_CHAT_ENABLED` | `bool` | `true`, `false` | `true` | Controls `/staffchat` completely, not only its cross-server half. With this off the command replies with the `STAFFCHAT.DISABLED` message even on a standalone server. The `STAFF_CHAT` feature toggle has to be on as well. |
| `NETWORK.HELPOP_ENABLED` | `bool` | `true`, `false` | `true` | Controls `/helpop` completely. With this off the command replies with the `HELPOP.DISABLED` message, and helpops arriving from other servers are dropped instead of being shown to staff here. |
| `NETWORK.REPORT_ENABLED` | `bool` | `true`, `false` | `true` | Controls `/report` completely, the same way `HELPOP_ENABLED` controls helpop. Reports arriving from other servers are dropped while it is off. |
| `NETWORK.STAFF_JOIN_LEAVE_ENABLED` | `bool` | `true`, `false` | `true` | Announces staff logging in and out to the other servers, drawn with the `STAFF_JOIN` and `STAFF_LEAVE` formats. Needs `ENABLED` and `STAFF_CHAT_ENABLED` on as well. The join notice waits a second and rechecks the player, so somebody who joins and drops straight back out produces neither notice. |
| `NETWORK.SERVER_STATUS_ENABLED` | `bool` | `true`, `false` | `true` | Sends an online notice when this server finishes loading and an offline notice as it shuts down, drawn with the `SERVER_STATUS` format. Only the other servers see it; a server never prints its own status. |
| `NETWORK.LOCAL_SERVER_ID` | `str` | Lower-case letters, digits, `_` and `-` | `'crystal'` | Identifies this server inside every message it sends. The value is lower-cased and anything outside letters, digits, `_` and `-` is turned into `-`, so `Crystal SMP` is stored as `crystal-smp`. Left blank it falls back to `NETWORK-STATUS.LOCAL-SERVER-ID` and then to `local`. Give each server its own or their messages cannot be told apart. |
| `NETWORK.LOCAL_DISPLAY_NAME` | `str` | Any string text | `'Crystal'` | The name `%server%` prints in the formats below, so this is the one staff actually read. Left blank it falls back to `NETWORK-STATUS.LOCAL-DISPLAY-NAME`, and then to the server id with its separators turned into spaces and each word capitalised. |
| `NETWORK.REDIS_CHANNEL` | `str` | Any string text | `'ultimatedonutsmp:staff-chat'` | The Redis channel carrying staff chat, staff join and leave notices, and server status. Every server meant to share one staff chat has to name the same channel here. Two servers on different channels each work on their own and simply never see each other. |
| `NETWORK.HELPOP_REDIS_CHANNEL` | `str` | Any string text | `'ultimatedonutsmp:staff-alerts'` | The Redis channel carrying `/helpop` alerts. It ships pointing at the same channel as reports, which is why the two arrive together. Split them only when some servers should receive one kind of alert and not the other. |
| `NETWORK.REPORT_REDIS_CHANNEL` | `str` | Any string text | `'ultimatedonutsmp:staff-alerts'` | The Redis channel carrying `/report` alerts, sharing the helpop channel by default. A server listening on neither channel still delivers its own alerts to its own staff. |
| `NETWORK.SEND_LOCAL_FALLBACK_ON_REDIS_ERROR` | `bool` | `true`, `false` | `true` | The name oversells it: local delivery is not optional and never was. A staff chat message reaches the staff on this server before Redis is attempted at all. What this key decides is whether the sender is told, through the `STAFFCHAT.REDIS_UNAVAILABLE` message, that their message did not make it to the other servers. Each player is warned once per session. |
| `NETWORK.STAFF_ALERTS_LOCAL_FALLBACK_ON_REDIS_ERROR` | `bool` | `true`, `false` | `true` | Nothing reads this key. Helpop and report alerts always reach the staff on this server before Redis is attempted, so the behaviour it describes is permanently on and editing the value changes nothing. |
| `NETWORK.STAFF_ALERTS_WARN_SENDER_ON_REDIS_ERROR` | `bool` | `true`, `false` | `false` | The alert counterpart of `SEND_LOCAL_FALLBACK_ON_REDIS_ERROR`. With it on, a player whose `/helpop` or `/report` reached local staff but could not be published to the other servers is told so, once per session. Off by default, since the alert did reach somebody. |
| `NETWORK.LOG_TO_CONSOLE` | `bool` | `true`, `false` | `true` | Writes every staff chat line to this server's console with the colour codes stripped, which is what puts it in `logs/latest.log` for reading back later. Turn it off on a server where staff chat should leave no trace in the log. |
| `NETWORK.STAFF_ALERTS_LOG_TO_CONSOLE` | `bool` | `true`, `false` | `true` | The same for helpop and report alerts. Delete the key rather than setting it and alerts follow whatever `LOG_TO_CONSOLE` says, instead of falling back to `true` on their own. |
| `NETWORK.MAX_MESSAGE_LENGTH` | `int` | `1` or greater | `'512'` | Longest staff chat message accepted. Anything longer is refused with `STAFFCHAT.MESSAGE_TOO_LONG` and never sent anywhere. Values below `1` are read as `1`. |
| `NETWORK.STAFF_ALERTS_MAX_REASON_LENGTH` | `int` | `1` or greater | `'256'` | Longest `/helpop` message or `/report` reason accepted. It doubles as the default for two finer keys that do not ship in the file, `HELPOP_MAX_MESSAGE_LENGTH` and `REPORT_MAX_REASON_LENGTH`; add one of those to give that command a limit of its own. |
| `NETWORK.HELPOP_COOLDOWN_SECONDS` | `int` | `0` or greater | `'30'` | How long a player waits between `/helpop` submissions. `0` removes the wait entirely, and anyone holding `ultimatedonutsmp.staff.alerts.bypass-cooldown` skips it whatever the value. |
| `NETWORK.REPORT_COOLDOWN_SECONDS` | `int` | `0` or greater | `'60'` | The same for `/report`, with the same bypass permission and the same meaning for `0`. The two cooldowns are counted separately, so using one does not delay the other. |
| `NETWORK.SERVER_STATUS` | `str` | Any string text | `'&6%server% &eis now %status%&e.'` | Format for the online and offline notices described under `SERVER_STATUS_ENABLED`. `%server%` is the server that changed and `%status%` is the bare word `online` or `offline`. See the placeholder section below. |
| `NETWORK.STAFF_CHAT` | `str` | Any string text | `'&8[&dNetwork&8] &7[%server%] &e%pla...'` | Format every staff chat line is drawn with, both on the server it was sent from and on the ones it reaches. Remove the key and the plugin falls back to `STAFFCHAT.FORMAT` in `messages.yml`. See the placeholder section below. |
| `NETWORK.STAFF_JOIN` | `str` | Any string text | `'&8[&a+&8] &a%player% &7joined &b%se...'` | Format for the staff join notice. `%player%` is the staff member and `%server%` the server they joined, and the notice is shown to every other staff member rather than to them. See the placeholder section below. |
| `NETWORK.STAFF_LEAVE` | `str` | Any string text | `'&8[&c-&8] &a%player% &7left &b%serv...'` | Format for the staff leave notice, sent as they disconnect and shown to everyone except them. See the placeholder section below. |

### 3. Placeholders In The Message Formats

`NETWORK.STAFF_CHAT`, `NETWORK.STAFF_JOIN`, `NETWORK.STAFF_LEAVE` and `NETWORK.SERVER_STATUS` take
four built-in tokens: `%server%` is the display name of the server the message came from, `%player%`
the staff member, `%message%` what they said, and `%status%` the online or offline word on a status
broadcast. Those same tokens work in `STAFFCHAT.FORMAT` in `messages.yml`, which is what the plugin
falls back to when `NETWORK.STAFF_CHAT` has been removed.

PlaceholderAPI placeholders work in these formats as well, and they resolve against the staff member
who sent the message rather than the person reading it, so a rank or prefix placeholder shows the
sender's rank on every screen the line lands on. That holds across servers: a message arriving over
Redis was sent by somebody who is usually not online locally, so their placeholders are read from
offline data instead. An expansion with no answer for an offline player leaves its placeholder
unfilled.

A placeholder typed into the message itself is not expanded; it prints as the text that was typed.

Colour codes typed into the message follow `ultimatedonutsmp.chat.color`, which defaults to `op` and
comes with the `ultimatedonutsmp.admin` and `ultimatedonutsmp.staff.mode` bundles. A sender holding
it can colour their own staff chat text; for anyone else the codes are removed before the line goes
out, on the server the message was sent from rather than the ones it arrives at.

```yaml
NETWORK:
  STAFF_CHAT: '&8[&dNetwork&8] &7[%server%] %luckperms_prefix%&e%player%&8: &f%message%'
```

### 4. Practical Setup Example

```yaml
NETWORK:
  # Enable or disable the cross-server network system globally (true / false)
  ENABLED: true

  # Enable cross-server staff chat sync via Redis (true / false)
  STAFF_CHAT_ENABLED: true

  # Enable cross-server helpop notification sync (true / false)
  HELPOP_ENABLED: true

  # Enable cross-server report notification sync (true / false)
  REPORT_ENABLED: true

  # Enable cross-server staff join/leave notifications (true / false)
  STAFF_JOIN_LEAVE_ENABLED: true

  # Enable cross-server status heartbeat monitoring (true / false)
  SERVER_STATUS_ENABLED: true

  # Unique server identifier for this local server instance
  LOCAL_SERVER_ID: crystal

  # User-friendly server display name
  LOCAL_DISPLAY_NAME: Crystal

  # Redis pub/sub channel for staff chat messages
  REDIS_CHANNEL: ultimatedonutsmp:staff-chat

  # Redis pub/sub channel for helpop alerts
  HELPOP_REDIS_CHANNEL: ultimatedonutsmp:staff-alerts

  # Redis pub/sub channel for player reports
  REPORT_REDIS_CHANNEL: ulti
```

---

## Section: `NETWORK-STATUS`

### 1. Commented Setup Code Example

```yaml
NETWORK-STATUS:
  # Enable network status monitoring dashboard (true / false)
  ENABLED: true

  # Local server ID alias for status check
  LOCAL-SERVER-ID: crystal

  # Local display name alias for status check
  LOCAL-DISPLAY-NAME: Crystal

  # Interval in seconds between network heartbeat status refreshes
  REFRESH-SECONDS: 5

  # Timeout in milliseconds for server ping status checks
  TIMEOUT-MS: 1500

  # Internal REST API HTTP endpoint for external monitoring
  ENDPOINT:
    # Enable HTTP status endpoint server (true / false)
    ENABLED: false
    # Host IP address to bind HTTP endpoint server
    HOST: 0.0.0.0
    # Port number for HTTP status endpoint
    PORT: 8123
    # Endpoint URI path
    PATH: /status
    # Secret authorization token for HTTP status queries
    TOKEN: change-me

  # Configuration for remote network servers to monitor
  SERVERS:
    crystal:
      DISPLAY: Crystal
      SOURCE:
        TYPE: LOCAL
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `NETWORK-STATUS.ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `NETWORK-STATUS` system. Set to `true` to enable, `false` to disable. |
| `NETWORK-STATUS.LOCAL-SERVER-ID` | `str` | Any string text | `'crystal'` | Configures the technical `LOCAL-SERVER-ID` parameter for `NETWORK-STATUS.LOCAL-SERVER-ID` in `network.yml`. |
| `NETWORK-STATUS.LOCAL-DISPLAY-NAME` | `str` | Any string text | `'Crystal'` | Configures the technical `LOCAL-DISPLAY-NAME` parameter for `NETWORK-STATUS.LOCAL-DISPLAY-NAME` in `network.yml`. |
| `NETWORK-STATUS.REFRESH-SECONDS` | `int` | Any valid integer number | `'5'` | Configures the technical `REFRESH-SECONDS` parameter for `NETWORK-STATUS.REFRESH-SECONDS` in `network.yml`. |
| `NETWORK-STATUS.TIMEOUT-MS` | `int` | Any valid integer number | `'1500'` | Configures the technical `TIMEOUT-MS` parameter for `NETWORK-STATUS.TIMEOUT-MS` in `network.yml`. |
| `NETWORK-STATUS.ENDPOINT.ENABLED` | `bool` | `true`, `false` | `false` | Global toggle for `NETWORK-STATUS` system. Set to `true` to enable, `false` to disable. |
| `NETWORK-STATUS.ENDPOINT.HOST` | `str` | Any string text | `'0.0.0.0'` | Configures the technical `HOST` parameter for `NETWORK-STATUS.ENDPOINT.HOST` in `network.yml`. |
| `NETWORK-STATUS.ENDPOINT.PORT` | `int` | Any valid integer number | `'8123'` | Configures the technical `PORT` parameter for `NETWORK-STATUS.ENDPOINT.PORT` in `network.yml`. |
| `NETWORK-STATUS.ENDPOINT.PATH` | `str` | Any string text | `'/status'` | Configures the technical `PATH` parameter for `NETWORK-STATUS.ENDPOINT.PATH` in `network.yml`. |
| `NETWORK-STATUS.ENDPOINT.TOKEN` | `str` | Any string text | `'change-me'` | Configures the technical `TOKEN` parameter for `NETWORK-STATUS.ENDPOINT.TOKEN` in `network.yml`. |
| `NETWORK-STATUS.SERVERS.crystal.DISPLAY` | `str` | Any string text | `'Crystal'` | Configures the technical `DISPLAY` parameter for `NETWORK-STATUS.SERVERS.crystal.DISPLAY` in `network.yml`. |
| `NETWORK-STATUS.SERVERS.crystal.SOURCE.TYPE` | `str` | Any string text | `'LOCAL'` | Configures the technical `TYPE` parameter for `NETWORK-STATUS.SERVERS.crystal.SOURCE.TYPE` in `network.yml`. |

### 3. Practical Setup Example

```yaml
NETWORK-STATUS:
  # Enable network status monitoring dashboard (true / false)
  ENABLED: true

  # Local server ID alias for status check
  LOCAL-SERVER-ID: crystal

  # Local display name alias for status check
  LOCAL-DISPLAY-NAME: Crystal

  # Interval in seconds between network heartbeat status refreshes
  REFRESH-SECONDS: 5

  # Timeout in milliseconds for server ping status checks
  TIMEOUT-MS: 1500

  # Internal REST API HTTP endpoint for external monitoring
  ENDPOINT:
    # Enable HTTP status endpoint server (true / false)
    ENABLED: false
    # Host IP address to bind HTTP endpoint server
    HOST: 0.0.0.0
    # Port number for HTTP status endpoint
    PORT: 8123
    # Endpoint URI path
    PATH: /status
    # Secret authorization token for HTTP status queries
    TOKEN: change-me

  # Configuration for remote network servers to monitor
  SERVERS:
    crystal:
      DISPLAY: Crystal
      SOURCE:
        TYPE: LOCAL
```
---

## Section: `MAINTENANCE`

### 1. Commented Setup Code Example

```yaml
# Maintenance mode behaviour (/maintenance on [duration]|off|status|setlobby)
MAINTENANCE:
  # Permission node that lets a player join while maintenance mode is active
  BYPASS_PERMISSION: ULTIMATEDONUTSMP.ADMIN.MAINTENANCE.BYPASS

  # Move players to another server through the proxy (true) or keep them on this one (false)
  USE_PROXY: true

  # Proxy server players are moved to while maintenance is active, used when USE_PROXY is true.
  # Leave it empty when this server has no lobby to hand players to: maintenance then refuses
  # the connection at login instead of letting players in and kicking them a moment later
  LOBBY_SERVER: lobby

  # World players are teleported to while maintenance is active, used when USE_PROXY is false.
  # Falls back to the spawn location when that world is not loaded. Leave it empty when this
  # server has nowhere to put players: maintenance then kicks everyone who is online and refuses
  # the connection at login, so nobody is left standing in the world while the server is shut
  LOBBY_WORLD: WORLD

  # Countdown in seconds shown before players are sent back once the server returns
  RECONNECT_DELAY_SECONDS: 5

  # Everything maintenance says to players. Colour codes work in all of them
  MESSAGES:
    # Sent to everyone still online when /maintenance on runs
    ENTERING: '&d[Maintenance] &7server is entering maintenance. Moving you to the lobby...'

    # Sent to a player who joins during maintenance without the bypass permission
    NOT_ALLOWED: '&d[Maintenance] &cthis server is currently in maintenance. Redirecting to lobby...'

    # Sent instead to a player who joins holding the bypass permission
    BYPASS_JOIN: '&d[Maintenance] &7you joined while maintenance mode is active.'

    # Disconnect screen text, used when a proxy handoff fails and on every login refused
    # because no lobby is set
    KICK_FALLBACK: '&cThis server is in maintenance and no lobby is available.'

    # Title shown while players wait to be sent back once the server returns
    RECONNECTING_TITLE: '&a&lServer online'

    # Subtitle under it. %seconds% is the time left on RECONNECT_DELAY_SECONDS
    RECONNECTING_SUBTITLE: '&7Sending you back in %seconds% seconds...'

  # How this server looks in the multiplayer list while maintenance mode is active
  SERVER_LIST:
    # Set to false to leave the server list entry alone and only gate the login
    ENABLED: true

    # The lines shown under the server name. The client draws the first two. %time% is the
    # time left before maintenance lifts itself, written as 03:29, or 1:03:29 once more than an
    # hour is left
    LINES:
    - '&cCurrently under maintenance'
    - '&bCome back in: &d%time%'

    # Used instead of LINES when maintenance was started with no duration, as in a plain
    # /maintenance on, so the entry never shows a countdown with nothing to count down to
    LINES_NO_TIMER:
    - '&cCurrently under maintenance'
    - '&7come back later'

    # Text shown where the player count normally sits. The client draws it in red next to a
    # broken connection icon, which is what makes the entry stand out in a long server list.
    # Leave it empty to keep the real player count on show
    VERSION_LABEL: '&cMaintenance'

    # Lines shown when the player count is hovered. Leaving the list empty keeps the usual
    # sample of online player names
    HOVER:
    - '&cCurrently under maintenance'
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `MAINTENANCE.BYPASS_PERMISSION` | `str` | Any permission node | `'ULTIMATEDONUTSMP.ADMIN.MAINTENANCE.BYPASS'` | Players holding this node join normally while maintenance is active and are never moved or kicked by it. |
| `MAINTENANCE.USE_PROXY` | `bool` | `true`, `false` | `true` | `true` hands players to another server over the BungeeCord/Velocity plugin channel. `false` keeps them on this server and teleports them instead. |
| `MAINTENANCE.LOBBY_SERVER` | `str` | Any proxy server name, or empty | `'lobby'` | Destination used when `USE_PROXY` is `true`. `/maintenance setlobby <server>` overrides it at runtime, and `/maintenance setlobby` with no name clears that override and hands the decision back to this key. Leave it empty on a server with no lobby: the connection is then refused during login, so players never enter the world. |
| `MAINTENANCE.LOBBY_WORLD` | `str` | Any loaded world name, or empty | `'WORLD'` | Destination used when `USE_PROXY` is `false`. When that world is not loaded the spawn location is used in its place. Leave it empty on a server with nowhere to put players: maintenance kicks everyone who is online and refuses every connection after that, which is how an empty `LOBBY_SERVER` already behaves in proxy mode. |
| `MAINTENANCE.RECONNECT_DELAY_SECONDS` | `int` | Any valid integer number | `'5'` | Countdown shown to players waiting to be sent back once the server reports itself online again over Redis. `0` sends them back straight away. |
| `MAINTENANCE.SERVER_LIST.ENABLED` | `bool` | `true`, `false` | `true` | `true` rewrites this server's entry in the multiplayer list while maintenance is active. `false` leaves the entry alone, and maintenance only shows itself when someone tries to join. |
| `MAINTENANCE.SERVER_LIST.LINES` | `list` | Any lines of text | `['&cCurrently under maintenance', '&bCome back in: &d%time%']` | The MOTD shown while a duration is running. The client draws the first two entries. `%time%` becomes the time left, written as `03:29` and widening to `1:03:29` past an hour. |
| `MAINTENANCE.SERVER_LIST.LINES_NO_TIMER` | `list` | Any lines of text | `['&cCurrently under maintenance', '&7come back later']` | Used instead of `LINES` when maintenance was started without a duration, so the entry never shows a countdown that has nothing to count down to. |
| `MAINTENANCE.SERVER_LIST.VERSION_LABEL` | `str` | Any string text, or empty | `'&cMaintenance'` | Replaces the player count on the entry. The client draws it in red beside a broken connection icon, which is what makes a closed server stand out in a long list. Empty leaves the real player count on show. |
| `MAINTENANCE.SERVER_LIST.HOVER` | `list` | Any lines of text | `['&cCurrently under maintenance']` | Shown when the player count is hovered, in place of the usual sample of online names. An empty list keeps that sample, staff working through the maintenance included. |
| `MAINTENANCE.MESSAGES.ENTERING` | `str` | Any string text | `'&d[Maintenance] &7server is entering maintenance. Moving you to the lobby...'` | Sent to everyone online the moment `/maintenance on` runs, just before they are moved. |
| `MAINTENANCE.MESSAGES.NOT_ALLOWED` | `str` | Any string text | `'&d[Maintenance] &cthis server is currently in maintenance. Redirecting to lobby...'` | Sent to a player who joins during maintenance without the bypass node, on a server that still has a lobby to move them to. |
| `MAINTENANCE.MESSAGES.BYPASS_JOIN` | `str` | Any string text | `'&d[Maintenance] &7you joined while maintenance mode is active.'` | Sent instead to staff holding the bypass node, so nobody forgets the server is shut while they work. |
| `MAINTENANCE.MESSAGES.KICK_FALLBACK` | `str` | Any string text | `'&cThis server is in maintenance and no lobby is available.'` | The disconnect screen text: shown when a proxy handoff fails, and on every login refused because no lobby is set. |
| `MAINTENANCE.MESSAGES.RECONNECTING_TITLE` | `str` | Any string text | `'&a&lServer online'` | Title shown while players wait to be sent back once the server reports itself online again over Redis. |
| `MAINTENANCE.MESSAGES.RECONNECTING_SUBTITLE` | `str` | Any string text | `'&7Sending you back in %seconds% seconds...'` | Subtitle under that title. `%seconds%` becomes the time left on `RECONNECT_DELAY_SECONDS`. |

### 3. Practical Setup Example

A single server with no lobby to hand players to. `/maintenance on` refuses every connection except staff holding the bypass node:

```yaml
MAINTENANCE:
  BYPASS_PERMISSION: ULTIMATEDONUTSMP.ADMIN.MAINTENANCE.BYPASS
  USE_PROXY: true
  LOBBY_SERVER: ''
```

A single server with no proxy behind it and no world to park anyone in. `/maintenance on` kicks
whoever is online and turns away every connection after that, with the disconnect text written to
suit the server:

```yaml
MAINTENANCE:
  USE_PROXY: false
  LOBBY_WORLD: ''
  MESSAGES:
    KICK_FALLBACK: '&cWe are down for maintenance, back shortly.'
```

An hour of scheduled downtime, announced in the multiplayer list. `/maintenance on 1h` starts the
countdown, players watch it run down on the server entry, and maintenance lifts itself when it
reaches zero:

```yaml
MAINTENANCE:
  SERVER_LIST:
    ENABLED: true
    LINES:
    - '&cUpgrading the server'
    - '&bBack in: &d%time%'
    VERSION_LABEL: '&cMaintenance'
```

---
