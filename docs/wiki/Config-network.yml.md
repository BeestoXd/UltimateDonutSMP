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
| `NETWORK.ENABLED` | `bool` | `true`, `false` | `true` | Global toggle for `NETWORK` system. Set to `true` to enable, `false` to disable. |
| `NETWORK.STAFF_CHAT_ENABLED` | `bool` | `true`, `false` | `true` | Configures the technical `STAFF_CHAT_ENABLED` parameter for `NETWORK.STAFF_CHAT_ENABLED` in `network.yml`. |
| `NETWORK.HELPOP_ENABLED` | `bool` | `true`, `false` | `true` | Configures the technical `HELPOP_ENABLED` parameter for `NETWORK.HELPOP_ENABLED` in `network.yml`. |
| `NETWORK.REPORT_ENABLED` | `bool` | `true`, `false` | `true` | Configures the technical `REPORT_ENABLED` parameter for `NETWORK.REPORT_ENABLED` in `network.yml`. |
| `NETWORK.STAFF_JOIN_LEAVE_ENABLED` | `bool` | `true`, `false` | `true` | Configures the technical `STAFF_JOIN_LEAVE_ENABLED` parameter for `NETWORK.STAFF_JOIN_LEAVE_ENABLED` in `network.yml`. |
| `NETWORK.SERVER_STATUS_ENABLED` | `bool` | `true`, `false` | `true` | Configures the technical `SERVER_STATUS_ENABLED` parameter for `NETWORK.SERVER_STATUS_ENABLED` in `network.yml`. |
| `NETWORK.LOCAL_SERVER_ID` | `str` | Any string text | `'crystal'` | Configures the technical `LOCAL_SERVER_ID` parameter for `NETWORK.LOCAL_SERVER_ID` in `network.yml`. |
| `NETWORK.LOCAL_DISPLAY_NAME` | `str` | Any string text | `'Crystal'` | Configures the technical `LOCAL_DISPLAY_NAME` parameter for `NETWORK.LOCAL_DISPLAY_NAME` in `network.yml`. |
| `NETWORK.REDIS_CHANNEL` | `str` | Any string text | `'ultimatedonutsmp:staff-chat'` | Configures the technical `REDIS_CHANNEL` parameter for `NETWORK.REDIS_CHANNEL` in `network.yml`. |
| `NETWORK.HELPOP_REDIS_CHANNEL` | `str` | Any string text | `'ultimatedonutsmp:staff-alerts'` | Configures the technical `HELPOP_REDIS_CHANNEL` parameter for `NETWORK.HELPOP_REDIS_CHANNEL` in `network.yml`. |
| `NETWORK.REPORT_REDIS_CHANNEL` | `str` | Any string text | `'ultimatedonutsmp:staff-alerts'` | Configures the technical `REPORT_REDIS_CHANNEL` parameter for `NETWORK.REPORT_REDIS_CHANNEL` in `network.yml`. |
| `NETWORK.SEND_LOCAL_FALLBACK_ON_REDIS_ERROR` | `bool` | `true`, `false` | `true` | Configures the technical `SEND_LOCAL_FALLBACK_ON_REDIS_ERROR` parameter for `NETWORK.SEND_LOCAL_FALLBACK_ON_REDIS_ERROR` in `network.yml`. |
| `NETWORK.STAFF_ALERTS_LOCAL_FALLBACK_ON_REDIS_ERROR` | `bool` | `true`, `false` | `true` | Configures the technical `STAFF_ALERTS_LOCAL_FALLBACK_ON_REDIS_ERROR` parameter for `NETWORK.STAFF_ALERTS_LOCAL_FALLBACK_ON_REDIS_ERROR` in `network.yml`. |
| `NETWORK.STAFF_ALERTS_WARN_SENDER_ON_REDIS_ERROR` | `bool` | `true`, `false` | `false` | Configures the technical `STAFF_ALERTS_WARN_SENDER_ON_REDIS_ERROR` parameter for `NETWORK.STAFF_ALERTS_WARN_SENDER_ON_REDIS_ERROR` in `network.yml`. |
| `NETWORK.LOG_TO_CONSOLE` | `bool` | `true`, `false` | `true` | Configures the technical `LOG_TO_CONSOLE` parameter for `NETWORK.LOG_TO_CONSOLE` in `network.yml`. |
| `NETWORK.STAFF_ALERTS_LOG_TO_CONSOLE` | `bool` | `true`, `false` | `true` | Configures the technical `STAFF_ALERTS_LOG_TO_CONSOLE` parameter for `NETWORK.STAFF_ALERTS_LOG_TO_CONSOLE` in `network.yml`. |
| `NETWORK.MAX_MESSAGE_LENGTH` | `int` | Any valid integer number | `'512'` | Configures the technical `MAX_MESSAGE_LENGTH` parameter for `NETWORK.MAX_MESSAGE_LENGTH` in `network.yml`. |
| `NETWORK.STAFF_ALERTS_MAX_REASON_LENGTH` | `int` | Any valid integer number | `'256'` | Configures the technical `STAFF_ALERTS_MAX_REASON_LENGTH` parameter for `NETWORK.STAFF_ALERTS_MAX_REASON_LENGTH` in `network.yml`. |
| `NETWORK.HELPOP_COOLDOWN_SECONDS` | `int` | Any valid integer number | `'30'` | Configures the technical `HELPOP_COOLDOWN_SECONDS` parameter for `NETWORK.HELPOP_COOLDOWN_SECONDS` in `network.yml`. |
| `NETWORK.REPORT_COOLDOWN_SECONDS` | `int` | Any valid integer number | `'60'` | Configures the technical `REPORT_COOLDOWN_SECONDS` parameter for `NETWORK.REPORT_COOLDOWN_SECONDS` in `network.yml`. |
| `NETWORK.SERVER_STATUS` | `str` | Any string text | `'&6%server% &eis now %status%&e.'` | Configures the technical `SERVER_STATUS` parameter for `NETWORK.SERVER_STATUS` in `network.yml`. |
| `NETWORK.STAFF_CHAT` | `str` | Any string text | `'&8[&dNetwork&8] &7[%server%] &e%pla...'` | Configures the technical `STAFF_CHAT` parameter for `NETWORK.STAFF_CHAT` in `network.yml`. |
| `NETWORK.STAFF_JOIN` | `str` | Any string text | `'&8[&a+&8] &a%player% &7joined &b%se...'` | Configures the technical `STAFF_JOIN` parameter for `NETWORK.STAFF_JOIN` in `network.yml`. |
| `NETWORK.STAFF_LEAVE` | `str` | Any string text | `'&8[&c-&8] &a%player% &7left &b%serv...'` | Configures the technical `STAFF_LEAVE` parameter for `NETWORK.STAFF_LEAVE` in `network.yml`. |

### 3. Practical Setup Example

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
# Maintenance mode behaviour (/maintenance on|off|status|setlobby)
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
  # Falls back to the spawn location when that world is not loaded, and refuses the connection
  # at login when neither one resolves
  LOBBY_WORLD: WORLD

  # Countdown in seconds shown before players are sent back once the server returns
  RECONNECT_DELAY_SECONDS: 5
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `MAINTENANCE.BYPASS_PERMISSION` | `str` | Any permission node | `'ULTIMATEDONUTSMP.ADMIN.MAINTENANCE.BYPASS'` | Players holding this node join normally while maintenance is active and are never moved or kicked by it. |
| `MAINTENANCE.USE_PROXY` | `bool` | `true`, `false` | `true` | `true` hands players to another server over the BungeeCord/Velocity plugin channel. `false` keeps them on this server and teleports them instead. |
| `MAINTENANCE.LOBBY_SERVER` | `str` | Any proxy server name, or empty | `'lobby'` | Destination used when `USE_PROXY` is `true`. `/maintenance setlobby <server>` overrides it at runtime. Leave it empty on a server with no lobby: the connection is then refused during login, so players never enter the world. |
| `MAINTENANCE.LOBBY_WORLD` | `str` | Any loaded world name | `'WORLD'` | Destination used when `USE_PROXY` is `false`. When that world is not loaded the spawn location is used, and when neither resolves the connection is refused during login. |
| `MAINTENANCE.RECONNECT_DELAY_SECONDS` | `int` | Any valid integer number | `'5'` | Countdown shown to players waiting to be sent back once the server reports itself online again over Redis. `0` sends them back straight away. |

### 3. Practical Setup Example

A single server with no lobby to hand players to. `/maintenance on` refuses every connection except staff holding the bypass node:

```yaml
MAINTENANCE:
  BYPASS_PERMISSION: ULTIMATEDONUTSMP.ADMIN.MAINTENANCE.BYPASS
  USE_PROXY: true
  LOBBY_SERVER: ''
```

---
