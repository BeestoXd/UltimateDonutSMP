<p align="center">
  <img src="images/mainn.png" alt="UltimateDonutSmp" width="720">
</p>

<h1 align="center">UltimateDonutSmp (FREE)</h1>

<p align="center">
  Premium Paper plugin for DonutSMP-style Minecraft servers.
  Economy, PvP, marketplace, staff tools, menus, and network utilities in one production-focused plugin.
</p>

<p align="center">
  <img alt="Java 21" src="https://img.shields.io/badge/Java-21-007396?style=for-the-badge&logo=openjdk&logoColor=white">
  <img alt="Paper" src="https://img.shields.io/badge/Platform-Paper_MC-2ea44f?style=for-the-badge">
  <img alt="Premium" src="https://img.shields.io/badge/Distribution-Premium-gold?style=for-the-badge">
  <img alt="Distribution" src="https://img.shields.io/badge/Access-Commercial-red?style=for-the-badge">
</p>

> [!IMPORTANT]
> This is proprietary premium software. Access, use, modification, and distribution are limited to authorized maintainers and customers only.
>
> Discord: https://dsc.gg/hellstarr

## Overview

UltimateDonutSmp is a complete Paper Minecraft server plugin built for DonutSMP-style survival networks. It combines player economy, teams, homes, warps, random teleport, shop, sell, worth, crates, shards, PvP systems, staff utilities, network communication, and GUI-driven workflows into one plugin.

The goal is to reduce the number of separate plugins required for a modern SMP server while keeping configuration, player data, permissions, placeholders, and staff operations consistent across the entire server experience.

## Highlights

| Area | Included systems |
| --- | --- |
| Economy | Money, shards, payments, shop, sell menu, worth browser, sell history, Vault support |
| Player progression | Stats, playtime, leaderboards, profiles, settings, scoreboard, tablist data |
| Teleportation | Spawn, AFK, homes, TPA, RTP, warps, portals, cuboid-triggered regions |
| Teams | Team creation, invites, homes, PvP toggle, chat, management menus |
| PvP | Duels, duel queue, FFA arenas, bounty system, fast crystal support, match stats |
| Marketplace | Auction House, Orders board, Billford trades, crates, key menus |
| Staff tools | Staff mode, freeze, vanish, invsee, profile viewer, punishment history, alts, reports, helpop |
| Network | Redis staff chat, network alerts, server status menu, Discord webhook support |
| Operations | Config reloads, stats wipe tools, optimization controls, and database support |

## Screenshots

<p align="center">
  <img src="images/uds1.png" alt="UltimateDonutSmp screenshot 1" width="32%">
  <img src="images/uds2.png" alt="UltimateDonutSmp screenshot 2" width="32%">
  <img src="images/uds3.png" alt="UltimateDonutSmp screenshot 3" width="32%">
  <img src="images/uds4.png" alt="UltimateDonutSmp screenshot 4" width="32%">
  <img src="images/uds5.png" alt="UltimateDonutSmp screenshot 5" width="32%">
  <img src="images/uds6.png" alt="UltimateDonutSmp screenshot 6" width="32%">
  <img src="images/uds7.png" alt="UltimateDonutSmp screenshot 7" width="32%">
  <img src="images/uds8.png" alt="UltimateDonutSmp screenshot 8" width="32%">
  <img src="images/uds9.png" alt="UltimateDonutSmp screenshot 9" width="32%">
  <img src="images/uds10.png" alt="UltimateDonutSmp screenshot 10" width="32%">
  <img src="images/uds11.png" alt="UltimateDonutSmp screenshot 11" width="32%">
  <img src="images/uds12.png" alt="UltimateDonutSmp screenshot 12" width="32%">
  <img src="images/uds13.png" alt="UltimateDonutSmp screenshot 13" width="32%">
  <img src="images/uds14.png" alt="UltimateDonutSmp screenshot 14" width="32%">
  <img src="images/uds16.png" alt="UltimateDonutSmp screenshot 16" width="32%">
  <img src="images/gif1.gif" alt="UltimateDonutSmp screenshot 1" width="32%">
  <img src="images/gif2.gif" alt="UltimateDonutSmp screenshot 2" width="32%">
  <img src="images/gif3.gif" alt="UltimateDonutSmp screenshot 3" width="32%">
  <img src="images/gif4.gif" alt="UltimateDonutSmp screenshot 4" width="32%">
  <img src="images/gif5.gif" alt="UltimateDonutSmp screenshot 5" width="32%">
  <img src="images/gif6.gif" alt="UltimateDonutSmp screenshot 6" width="32%">
</p>


## Requirements

| Requirement | Notes |
| --- | --- |
| Java | Java 21 |
| Server | Paper or a compatible Paper-based server matching the configured API target |
| PlaceholderAPI | Required; the plugin disables itself if PlaceholderAPI is missing or disabled |
| Storage | SQLite by default, with MySQL and MongoDB-backed modes available |
| Optional network layer | Redis for cross-server staff chat, reports, helpop, and server status |

Soft integrations:

- LuckPerms
- Vault
- ProtocolLib
- Apollo
- NickPlus

The plugin can run without these soft dependencies, but enabling them unlocks deeper permission, economy, packet, client, and network behavior.

## Installation

1. Stop the Minecraft server.
2. Place the plugin jar into the server `plugins/` directory.
3. Start the server once so the default configuration files are generated.
4. Configure storage in `database.yml`.
5. Review the core gameplay files such as `config.yml`, `menus.yml`, `shop.yml`, `worth.yml`, `rtp.yml`, and `messages.yml`.
6. Restart the server after first setup.

For production networks, MySQL plus Redis is recommended. For a single-server setup, SQLite is usually enough.

> [!WARNING]
> Do not share private customer files, database credentials, Discord tokens, Redis passwords, or other sensitive server data.

## Configuration

| File | Purpose |
| --- | --- |
| `config.yml` | Global feature toggles, locations, teleport cooldowns, combat, shards, key-all, tablist, optimization, and gameplay behavior |
| `messages.yml` | Command, system, and moderation messages |
| `menus.yml` | GUI layouts for player, economy, staff, profile, rules, server, and admin workflows |
| `database.yml` | SQLite, MySQL, MongoDB, and Redis connection settings |
| `shop.yml` | Shop categories, items, prices, currencies, and command rewards |
| `worth.yml` | Sell prices and worth browser settings |
| `rtp.yml` | Random teleport world settings, cooldowns, radius, menu, and safety behavior |
| `network.yml` | Network staff chat, helpop, reports, and server status |
| `auction-house.yml` | Auction House behavior and limits |
| `orders.yml` | Orders marketplace settings |
| `duels.yml` | Duel arenas, queues, countdowns, rules, and menus |
| `ffa.yml` | FFA arenas, rollback behavior, match rules, and stats |
| `crates.yml` | Crates, keys, rewards, animations, holograms, and particles |
| `spawners.yml` | Donut-style spawners, anti-ESP, storage, drops, and menus |
| `staff-mode.yml` | Staff mode hotbar, vanish, better view, staff list, and moderation menus |

Most player-facing text, GUI layouts, prices, cooldowns, permissions, and feature toggles are configurable without recompiling the plugin.

## Commands

UltimateDonutSmp registers a large command surface. Common entry points include:

| Category | Commands |
| --- | --- |
| Player | `/spawn`, `/afk`, `/home`, `/sethome`, `/rtp`, `/warp`, `/tpa`, `/settings`, `/stats` |
| Economy | `/balance`, `/pay`, `/shop`, `/sell`, `/sellhand`, `/sellall`, `/worth`, `/shards` |
| Marketplace | `/auctionhouse`, `/ah`, `/orders`, `/billford`, `/bounty` |
| PvP | `/duel`, `/queue`, `/leave`, `/draw`, `/ffa`, `/ffastats` |
| Crates | `/crates`, `/keys`, `/crate` |
| Staff | `/staffmode`, `/freeze`, `/vanish`, `/invsee`, `/profileviewer`, `/punishments`, `/alts` |
| Moderation | `/ban`, `/tempban`, `/mute`, `/tempmute`, `/warn`, `/kick`, `/blacklist`, `/unban`, `/unmute` |
| Network support | `/staffchat`, `/helpop`, `/report`, `/servers` |
| Admin | `/ultimatedonutsmp`, `/uds`, `/arena`, `/ffaarena`, `/portalmanager`, `/cuboid`, `/amethysttool` |

The complete command and permission list is available in the packaged plugin metadata and customer documentation.

## Documentation

- [Full feature documentation](docs/fitur-ultimatedonutsmp.md)
- [Changelog](CHANGELOG.md)
- Individual system plans are available in the `docs/` directory.

## Commercial Use

UltimateDonutSmp is proprietary commercial software.

- The plugin may only be used by authorized customers and maintainers.
- Redistribution, resale, sublicensing, public mirroring, or unauthorized sharing is not permitted without written permission.

Copyright (c) 2026 UltimateDonutSmp. All rights reserved.

## Support

Support is handled through the official purchase or customer support channel. When reporting an issue, include:

- Plugin version and jar file name
- Server software and version
- Java version
- Relevant configuration snippets with secrets removed
- Console errors or stack traces
- Steps to reproduce the issue

Do not share private customer files, database credentials, Redis passwords, Discord tokens, or other sensitive server data in public channels.
