# Welcome to the UltimateDonutSMP Wiki

Welcome to the official wiki documentation for **UltimateDonutSMP**, an all-in-one, high-performance Paper, Spigot, and Folia plugin designed for DonutSMP-style Minecraft survival networks.

UltimateDonutSMP replaces dozens of separate plugins by integrating economy, marketplaces, team systems, teleportation, PvP duels, FFA and ranked arenas, custom spawners, crates, staff utilities, security tools, and multi-server network syncing into a single, cohesive plugin.

---

## Technical Quick Facts

| Property | Details |
| :--- | :--- |
| **Plugin Version** | `1.4.1` |
| **Supported Server Engines** | Paper, Spigot, Folia |
| **Supported Minecraft Versions** | Paper/Spigot: `1.21.10` – `26.2`<br>Folia: `1.21.11` – `26.2` |
| **Java Requirement** | Java 21+ (Java 25 for MC 26.1+) |
| **Storage Engines** | SQLite (Default), MySQL, MongoDB |
| **Network Sync Layer** | Redis (Cross-server staff chat, alerts, maintenance) |
| **Soft Dependencies** | PlaceholderAPI, LuckPerms, Vault, ProtocolLib, Apollo, SkinsRestorer, Multiverse-Core, Floodgate |

---

## Documentation Directory

Explore the complete feature guide and documentation pages:

- **[Installation & Setup](Installation-and-Setup)**  
  Requirements, server engine installation, storage setup (SQLite, MySQL, MongoDB), and Redis multi-server networking.

- **[Commands & Permissions](Commands-and-Permissions)**  
  Comprehensive reference table of all player and administrator commands, syntax, aliases, and permission nodes (`ultimatedonutsmp.command.*`, `ultimatedonutsmp.admin.*`).

- **[Cuboids & Portals](Cuboids-and-Portals)**  
  Guide to region selections, `/cuboid` creation, binding regions to feature zones (Spawn, AFK, Shard Cuboids, RTP), and creating custom `/portal` triggers.

- **[Duels & Instanced FFA](Duels-and-FFA)**  
  Setting up duel arenas (`/arena`), player duel queues (`/duel`, `/queue`), arena rollbacks, crystal speed tweaks, and instanced FFA arenas (`/ffaarena`, `/ffa`).

- **[Ranked PvP Arena](Ranked-PvP-Arena)**  
  Persistent ranked arena (`/pvp`) with editable kits, Elo, an LT/HT rank ladder, PvP levels, kill-farm protection, its own scoreboard, and scheduled schematic resets through WorldEdit or FAWE.

- **[Economy & Marketplaces](Economy-and-Marketplaces)**  
  Vault economy, Shard currency, shop systems (`/shop`, `/sell`, `/worth`), Auction House (`/ah`), Orders board (`/orders`), and Billford rotating NPC trades (`/billford`).

- **[Crates & Spawners](Crates-and-Spawners)**  
  Virtual & physical crates (`/crate`), key distribution (`/keyall`), Donut-style stacked spawners (`/spawner`), Amethyst Tools, and Enchantment GUI.

- **[Staff & Security Utilities](Staff-and-Security)**  
  Staff Mode (`/staffmode`), Vanish (`/vanish`), Freeze, Inventory Inspect (`/invsee`), Spawn-Stash Bait, Fake Player Bait, Anti-ESP alerts, and guarded Server Wipes (`/serverwipe`).

- **[Placeholders & Integrations](Placeholders-and-Integrations)**  
  Full catalog of `%economy_*%` PlaceholderAPI placeholders, LuckPerms integration, Apollo (Lunar Client) support, and Bedrock/Floodgate compatibility.

- **[Configuration Reference](Configuration-Reference)**  
  In-depth guide to customizing `config.yml`, `messages.yml`, `menus.yml`, `duels.yml`, `crates.yml`, `spawners.yml`, `database.yml`, and `network.yml`.

- **[FAQ & Troubleshooting](FAQ)**  
  100% complete answers and troubleshooting steps for PlaceholderAPI issues, Vault hook failures, database persistence, cuboids, spawners, vanish, and fast crystal settings.
