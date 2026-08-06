# Installation & Setup Guide

This guide provides step-by-step instructions for installing and configuring **UltimateDonutSMP** across single servers or multi-server Minecraft networks, including native support for **Folia 26.2+** multi-threaded region ticking engines.

---

## System Requirements

| Requirement | Minimum / Supported | Notes |
| :--- | :--- | :--- |
| **Java Version** | Java 21+ | Minecraft 26.1 / 26.2 requires Java 25 |
| **Server Engine** | Paper, Spigot, **Folia 26.2+** | Native multi-threaded region scheduling on Folia |
| **Minecraft Versions** | `1.21.10` – `26.2` | Folia: `1.21.11` – `26.2` |
| **Build Tools** | Maven (`mvn`), Windows PowerShell | Tested on Windows / Linux environments |

---

## Installation Steps

1. **Download or Build Plugin**:
   - Compile the project using Maven:
     ```bash
     mvn clean package
     ```
   - Locate `UltimateDonutSmp-1.4.jar` inside the `target/` directory.

2. **Place JAR File**:
   - Copy `UltimateDonutSmp-1.4.jar` into your server's `plugins/` directory.

3. **Start the Server**:
   - Run your server start script. On first startup, UltimateDonutSMP will generate the default configuration directory:
     `plugins/UltimateDonutSmp/`

---

## Folia 26.2 Engine Compatibility & Multi-Threaded Architecture

UltimateDonutSMP is built from the ground up to support **Folia 26.2+** multi-threaded region ticking servers alongside Paper and Spigot.

### Key Architecture Features under Folia:
- **Region-Aware Schedulers**: Uses Paper/Folia `RegionScheduler`, `EntityScheduler`, `GlobalRegionScheduler`, and `AsyncScheduler` to ensure that region cuboids, portal triggers, Fast Crystal placements, and spawner ticks run safely on their respective region threads without throwing `ConcurrentModificationException` or thread safety errors.
- **Asynchronous Database Queries**: All SQLite, MySQL, and MongoDB transactions execute off the main region threads to maintain 20 TPS performance across all region workers.
- **Thread-Safe Economy Transactions**: Vault economy modifications, Shard balance updates, and Auction House listings use thread-safe atomic operations.

---

## Database Setup (`database.yml`)

UltimateDonutSMP supports three storage backends: **SQLite**, **MySQL**, and **MongoDB**.

Configure your database settings in `plugins/UltimateDonutSmp/database.yml`:

```yaml
STORAGE:
  TYPE: SQLITE # Options: SQLITE, MYSQL, MONGODB

MYSQL:
  HOST: "localhost"
  PORT: 3306
  DATABASE: "ultimatedonutsmp"
  USERNAME: "root"
  PASSWORD: "password"
  POOL-SIZE: 10

MONGODB:
  URI: "mongodb://localhost:27017"
  DATABASE: "ultimatedonutsmp"
```

> [!TIP]
> **SQLite** requires zero external setup and is bundled with a shaded JDBC driver. For multi-server BungeeCord/Velocity networks or Folia clusters, use **MySQL** or **MongoDB** to keep player balances, homes, stats, and inventories synchronized.

---

## Redis Network Layer Setup (`network.yml`)

For multi-server networks, UltimateDonutSMP provides cross-server staff chat, network alerts, maintenance routing, and live status menus via Redis.

Configure `plugins/UltimateDonutSmp/network.yml`:

```yaml
REDIS:
  ENABLED: true
  HOST: "127.0.0.1"
  PORT: 6379
  PASSWORD: ""
  CHANNEL: "uds_network"

SERVER-IDENTIFIER: "smp-01"
```

---

## Automatic Configuration Sync & Backups

UltimateDonutSMP automatically handles configuration updates and player data backups:
- **Auto Sync**: Updated settings in `config.yml` automatically hot-reload or auto-merge without overwriting custom edits.
- **Backups**: Player data and economy state are saved periodically to prevent data loss during sudden server crashes.
