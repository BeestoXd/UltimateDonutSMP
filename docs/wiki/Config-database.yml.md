# Detailed Configuration & Setup Guide: `database.yml`

This is the official, 100% complete technical setup guide for `database.yml` in **UltimateDonutSMP**.
Each section details the exact commented setup code block, allowed option values, data types, default values, and in-depth functional behavior.

---

## Section: `DATABASE`

### 1. Commented Setup Code Example

```yaml
DATABASE:
  TYPE: SQLITE
  # Configuration section for Sqlite.
  SQLITE:
    # The text or value for File. Available options: Any valid string text
    FILE: data/data.db
  # Configuration section for Mysql.
  MYSQL:
    # The text or value for Host. Available options: Any valid string text
    HOST: localhost
    # The numerical value for Port. Available options: Any valid integer
    PORT: 3306
    # The text or value for Database. Available options: Any valid string text
    DATABASE: ultimatedonutsmp
    # The text or value for Username. Available options: Any valid string text
    USERNAME: root
    # The text or value for Password. Available options: Any valid string text
    PASSWORD: ''
    # Determines whether Create Database is enabled or disabled. Available options: true, false
    CREATE-DATABASE: true
    # The text or value for Parameters. Available options: Any valid string text
    PARAMETERS: useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=utf8
  # Configuration section for Mongodb.
  MONGODB:
    # The text or value for Uri. Available options: Any valid string text
    URI: mongodb://localhost:27017
    # The text or value for Database. Available options: Any valid string text
    DATABASE: ultimatedonutsmp
    # The text or value for Cache File. Available options: Any valid string text
    CACHE-FILE: data/mongodb-cache.db
    # Determines whether Sync On Autosave is enabled or disabled. Available options: true, false
    SYNC-ON-AUTOSAVE: true
# Configuration section for Redis.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `DATABASE.TYPE` | `str` | Any string text | `'SQLITE'` | Selects storage backend engine:<br>- `SQLITE`: Default zero-setup local database file.<br>- `MYSQL`: Centralized database for multi-server networks.<br>- `MONGODB`: MongoDB document database. |
| `DATABASE.SQLITE.FILE` | `str` | Any string text | `'data/data.db'` | Configures the technical `FILE` parameter for `DATABASE.SQLITE.FILE` in `database.yml`. |
| `DATABASE.MYSQL.HOST` | `str` | Any string text | `'localhost'` | Configures the technical `HOST` parameter for `DATABASE.MYSQL.HOST` in `database.yml`. |
| `DATABASE.MYSQL.PORT` | `int` | Any valid integer number | `'3306'` | Configures the technical `PORT` parameter for `DATABASE.MYSQL.PORT` in `database.yml`. |
| `DATABASE.MYSQL.DATABASE` | `str` | Any string text | `'ultimatedonutsmp'` | Configures the technical `DATABASE` parameter for `DATABASE.MYSQL.DATABASE` in `database.yml`. |
| `DATABASE.MYSQL.USERNAME` | `str` | Any string text | `'root'` | Configures the technical `USERNAME` parameter for `DATABASE.MYSQL.USERNAME` in `database.yml`. |
| `DATABASE.MYSQL.PASSWORD` | `str` | Any string text | `''` | Configures the technical `PASSWORD` parameter for `DATABASE.MYSQL.PASSWORD` in `database.yml`. |
| `DATABASE.MYSQL.CREATE-DATABASE` | `bool` | `true`, `false` | `true` | Configures the technical `CREATE-DATABASE` parameter for `DATABASE.MYSQL.CREATE-DATABASE` in `database.yml`. |
| `DATABASE.MYSQL.PARAMETERS` | `str` | Any string text | `'useSSL=false&allowPublicKeyRetrieva...'` | Configures the technical `PARAMETERS` parameter for `DATABASE.MYSQL.PARAMETERS` in `database.yml`. |
| `DATABASE.MONGODB.URI` | `str` | Any string text | `'mongodb://localhost:27017'` | Configures the technical `URI` parameter for `DATABASE.MONGODB.URI` in `database.yml`. |
| `DATABASE.MONGODB.DATABASE` | `str` | Any string text | `'ultimatedonutsmp'` | Configures the technical `DATABASE` parameter for `DATABASE.MONGODB.DATABASE` in `database.yml`. |
| `DATABASE.MONGODB.CACHE-FILE` | `str` | Any string text | `'data/mongodb-cache.db'` | Configures the technical `CACHE-FILE` parameter for `DATABASE.MONGODB.CACHE-FILE` in `database.yml`. |
| `DATABASE.MONGODB.SYNC-ON-AUTOSAVE` | `bool` | `true`, `false` | `true` | Configures the technical `SYNC-ON-AUTOSAVE` parameter for `DATABASE.MONGODB.SYNC-ON-AUTOSAVE` in `database.yml`. |

### 3. Practical Setup Example

```yaml
DATABASE:
  TYPE: SQLITE
  # Configuration section for Sqlite.
  SQLITE:
    # The text or value for File. Available options: Any valid string text
    FILE: data/data.db
  # Configuration section for Mysql.
  MYSQL:
    # The text or value for Host. Available options: Any valid string text
    HOST: localhost
    # The numerical value for Port. Available options: Any valid integer
    PORT: 3306
    # The text or value for Database. Available options: Any valid string text
    DATABASE: ultimatedonutsmp
    # The text or value for Username. Available options: Any valid string text
    USERNAME: root
    # The text or value for Password. Available options: Any valid string text
    PASSWORD: ''
    # Determines whether Create Database is enabled or disabled. Available options: true, false
    CREATE-DATABASE: true
    # The text or value for Parameters. Available options: Any valid string text
    PARAMETERS: useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncodin
```

---

## Section: `REDIS`

### 1. Commented Setup Code Example

```yaml
REDIS:
  # Determines whether Enabled is enabled or disabled. Available options: true, false
  ENABLED: false
  # The text or value for Host. Available options: Any valid string text
  HOST: localhost
  # The numerical value for Port. Available options: Any valid integer
  PORT: 6379
  # The numerical value for Timeout. Available options: Any valid integer
  TIMEOUT: 2000
  # The text or value for Password. Available options: Any valid string text
  PASSWORD: ''
  # The numerical value for Database. Available options: Any valid integer
  DATABASE: 0
  # The numerical value for Max Total. Available options: Any valid integer
  MAX-TOTAL: 50
  # The numerical value for Max Idle. Available options: Any valid integer
  MAX-IDLE: 10
  # The numerical value for Min Idle. Available options: Any valid integer
  MIN-IDLE: 5
  # Determines whether Test On Borrow is enabled or disabled. Available options: true, false
  TEST-ON-BORROW: false
  # Determines whether Test On Return is enabled or disabled. Available options: true, false
  TEST-ON-RETURN: false
  # Determines whether Test While Idle is enabled or disabled. Available options: true, false
  TEST-WHILE-IDLE: false
  # The numerical value for Reconnect Delay Ms. Available options: Any valid integer
  RECONNECT-DELAY-MS: 5000
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `REDIS.ENABLED` | `bool` | `true`, `false` | `false` | Global toggle for `REDIS` system. Set to `true` to enable, `false` to disable. |
| `REDIS.HOST` | `str` | Any string text | `'localhost'` | Configures the technical `HOST` parameter for `REDIS.HOST` in `database.yml`. |
| `REDIS.PORT` | `int` | Any valid integer number | `'6379'` | Configures the technical `PORT` parameter for `REDIS.PORT` in `database.yml`. |
| `REDIS.TIMEOUT` | `int` | Any valid integer number | `'2000'` | Configures the technical `TIMEOUT` parameter for `REDIS.TIMEOUT` in `database.yml`. |
| `REDIS.PASSWORD` | `str` | Any string text | `''` | Configures the technical `PASSWORD` parameter for `REDIS.PASSWORD` in `database.yml`. |
| `REDIS.DATABASE` | `int` | Any valid integer number | `'0'` | Configures the technical `DATABASE` parameter for `REDIS.DATABASE` in `database.yml`. |
| `REDIS.MAX-TOTAL` | `int` | Any valid integer number | `'50'` | Configures the technical `MAX-TOTAL` parameter for `REDIS.MAX-TOTAL` in `database.yml`. |
| `REDIS.MAX-IDLE` | `int` | Any valid integer number | `'10'` | Configures the technical `MAX-IDLE` parameter for `REDIS.MAX-IDLE` in `database.yml`. |
| `REDIS.MIN-IDLE` | `int` | Any valid integer number | `'5'` | Configures the technical `MIN-IDLE` parameter for `REDIS.MIN-IDLE` in `database.yml`. |
| `REDIS.TEST-ON-BORROW` | `bool` | `true`, `false` | `false` | Configures the technical `TEST-ON-BORROW` parameter for `REDIS.TEST-ON-BORROW` in `database.yml`. |
| `REDIS.TEST-ON-RETURN` | `bool` | `true`, `false` | `false` | Configures the technical `TEST-ON-RETURN` parameter for `REDIS.TEST-ON-RETURN` in `database.yml`. |
| `REDIS.TEST-WHILE-IDLE` | `bool` | `true`, `false` | `false` | Configures the technical `TEST-WHILE-IDLE` parameter for `REDIS.TEST-WHILE-IDLE` in `database.yml`. |
| `REDIS.RECONNECT-DELAY-MS` | `int` | Any valid integer number | `'5000'` | Configures the technical `RECONNECT-DELAY-MS` parameter for `REDIS.RECONNECT-DELAY-MS` in `database.yml`. |

### 3. Practical Setup Example

```yaml
REDIS:
  # Determines whether Enabled is enabled or disabled. Available options: true, false
  ENABLED: false
  # The text or value for Host. Available options: Any valid string text
  HOST: localhost
  # The numerical value for Port. Available options: Any valid integer
  PORT: 6379
  # The numerical value for Timeout. Available options: Any valid integer
  TIMEOUT: 2000
  # The text or value for Password. Available options: Any valid string text
  PASSWORD: ''
  # The numerical value for Database. Available options: Any valid integer
  DATABASE: 0
  # The numerical value for Max Total. Available options: Any valid integer
  MAX-TOTAL: 50
  # The numerical value for Max Idle. Available options: Any valid integer
  MAX-IDLE: 10
  # The numerical value for Min Idle. Available options: Any valid integer
  MIN-IDLE: 5
  # Determines whether Test On Borrow is enabled or disabled. Available options: true, false
  TEST-ON-BORROW: false
  # Determines whether Test On Return is enabled or disa
```

---

