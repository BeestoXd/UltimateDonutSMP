# Commands & Permissions Reference

This page contains the complete reference guide for all commands, aliases, syntax, descriptions, and permission nodes provided by **UltimateDonutSMP**.

---

## Player Commands & Permissions

| Command | Usage Syntax | Aliases | Description | Permission Node |
| :--- | :--- | :--- | :--- | :--- |
| `/team` | `/team <create\|disband\|invite\|kick\|join\|leave\|home\|sethome\|delhome\|chat\|info\|pvp>` | None | Team management & alliance controls | `ultimatedonutsmp.command.team` |
| `/msg` | `/msg <player> <message>` | `/message`, `/tell`, `/whisper`, `/w` | Send private message to player | `ultimatedonutsmp.command.msg` |
| `/reply` | `/reply <message>` | `/r` | Reply to last private message | `ultimatedonutsmp.command.reply` |
| `/pm` | `/pm` | `/togglepm`, `/privatemessages` | Toggle private messaging on/off | `ultimatedonutsmp.command.pm` |
| `/ignore` | `/ignore <player\|list>` | None | Ignore messages from a player | `ultimatedonutsmp.command.ignore` |
| `/unignore` | `/unignore <player>` | None | Unignore a player | `ultimatedonutsmp.command.unignore` |
| `/home` | `/home [name]` | None | Teleport to a saved home | `ultimatedonutsmp.command.home` |
| `/homes` | `/homes` | None | Open menu or list saved homes | `ultimatedonutsmp.command.homes` |
| `/sethome` | `/sethome [name]` | None | Save current position as home | `ultimatedonutsmp.command.sethome` |
| `/delhome` | `/delhome <name>` | None | Delete a saved home | `ultimatedonutsmp.command.delhome` |
| `/renamehome`| `/renamehome <old> <new>` | None | Rename a saved home | `ultimatedonutsmp.command.renamehome` |
| `/spawn` | `/spawn` | None | Teleport to server spawn | `ultimatedonutsmp.command.spawn` |
| `/afk` | `/afk` | None | Teleport to or enter AFK reward zone | `ultimatedonutsmp.command.afk` |
| `/rtp` | `/rtp [world]` | None | Random teleport into the wilderness | `ultimatedonutsmp.command.rtp` |
| `/balance` | `/balance [player]` | `/bal`, `/money` | Check money balance | `ultimatedonutsmp.command.balance` |
| `/pay` | `/pay <player> <amount>` | None | Pay money to another player | `ultimatedonutsmp.command.pay` |
| `/shards` | `/shards [player]` | None | Check shard balance | `ultimatedonutsmp.command.shards` |
| `/shardpay` | `/shardpay <player> <amount>` | None | Pay shards to another player | `ultimatedonutsmp.command.shardpay` |
| `/shop` | `/shop` | None | Open GUI shop | `ultimatedonutsmp.command.shop` |
| `/sell` | `/sell` | None | Open GUI sell container | `ultimatedonutsmp.command.sell` |
| `/sellhand` | `/sellhand [amount]` | None | Sell item currently held in hand | `ultimatedonutsmp.command.sellhand` |
| `/sellall` | `/sellall` | None | Sell all sellable items in inventory | `ultimatedonutsmp.command.sellall` |
| `/sellhistory`| `/sellhistory` | None | View personal sell transaction history | `ultimatedonutsmp.command.sellhistory` |
| `/sellmulti` | `/sellmulti [category]` | None | Open sell multiplier menu | `ultimatedonutsmp.command.sellmulti` |
| `/sellmultiplier` | `/sellmultiplier [category]` | None | Open sell multiplier menu | `ultimatedonutsmp.command.sellmulti` |
| `/sellprogress` | `/sellprogress [category]` | None | Open sell multiplier progress menu | `ultimatedonutsmp.command.sellprogress` |
| `/worth` | `/worth [hand]` | `/prices` | Check worth of held item or open price catalog | `ultimatedonutsmp.command.worth` |
| `/auctionhouse`| `/auctionhouse [sell\|my\|claims]`| `/ah` | Open Auction House marketplace | `ultimatedonutsmp.command.auctionhouse` |
| `/orders` | `/orders [my\|collect]` | None | Open buy/sell Orders board | `ultimatedonutsmp.command.orders` |
| `/enderchest` | `/enderchest` | `/ec` | Open custom Ender Chest | `ultimatedonutsmp.command.enderchest` |
| `/crates` | `/crates` | None | Open crates overview menu | `ultimatedonutsmp.command.crates` |
| `/duel` | `/duel [player\|accept\|deny\|claims]` | None | Challenge a player or respond to duel | `ultimatedonutsmp.command.duel` |
| `/queue` | `/queue [join\|leave]` | None | Join or leave duel match queues | `ultimatedonutsmp.command.queue` |
| `/draw` | `/draw` | None | Offer or accept draw in active duel | `ultimatedonutsmp.command.draw` |
| `/leave` | `/leave` | None | Leave active duel or FFA instance | `ultimatedonutsmp.command.leave` |
| `/ffa` | `/ffa [join]` | None | Join instanced FFA arena | `ultimatedonutsmp.command.ffa` |
| `/ffastats` | `/ffastats [player]` | None | View FFA kill/death/streak stats | `ultimatedonutsmp.command.ffastats` |
| `/bounty` | `/bounty [place\|list]` | None | Place or view player bounties | `ultimatedonutsmp.command.bounty` |
| `/leaderboard` | `/leaderboard [type]` | `/lb`, `/top`, `/leaderboards`, `/baltop` | Open leaderboard menus; `/baltop` opens the money leaderboard directly | `ultimatedonutsmp.command.leaderboard` |

---

## Staff & Moderation Commands

| Command | Usage Syntax | Description | Permission Node |
| :--- | :--- | :--- | :--- |
| `/staffmode` | `/staffmode` (Alias `/staff`) | Toggle Staff Mode GUI & toolset | `ultimatedonutsmp.admin.staffmode` |
| `/vanish` | `/vanish` | Toggle complete invisibility to players | `ultimatedonutsmp.admin.vanish` |
| `/freeze` | `/freeze <player>` | Freeze or unfreeze a target player | `ultimatedonutsmp.admin.freeze` |
| `/invsee` | `/invsee <player>` | Inspect and edit player inventory in real-time | `ultimatedonutsmp.admin.invsee` |
| `/ecsee` | `/ecsee <player>` | Inspect and edit player Ender Chest | `ultimatedonutsmp.admin.ecsee` |
| `/chat` | `/chat <mute\|unmute\|delay\|clear>` | Global chat moderation controls | `ultimatedonutsmp.admin.chat` |
| `/spawnstash` | `/spawnstash <give\|setup\|list>` (Alias `/stash`) | Manage spawn stash bait chests | `ultimatedonutsmp.admin.spawnstash` |
| `/fakeplayer` | `/fakeplayer` (Alias `/fplayer`) | Spawn fake player bait entities | `ultimatedonutsmp.command.fakeplayer` |
| `/amod` | `/amod <add\|reload>` | Manage the anvil rename word filter | `ultimatedonutsmp.command.amod` |
| `/offend` | `/offend <player> <reason> [time]` | Issue preset offense-based punishment with escalating duration | `ultimatedonutsmp.staff.punishments.offend` |
| `/punishments` | `/punishments <player>` | View punishment history GUI for target player | `ultimatedonutsmp.staff.punishments.view` |
| `/ban` | `/ban <player> [reason]` | Issue permanent ban | `ultimatedonutsmp.staff.punishments.ban` |
| `/tempban` | `/tempban <player> <time> [reason]` | Issue temporary ban | `ultimatedonutsmp.staff.punishments.ban` |
| `/mute` | `/mute <player> [reason]` | Issue permanent mute | `ultimatedonutsmp.staff.punishments.mute` |
| `/tempmute` | `/tempmute <player> <time> [reason]` | Issue temporary mute | `ultimatedonutsmp.staff.punishments.mute` |
| `/warn` | `/warn <player> [reason]` | Issue formal warning | `ultimatedonutsmp.staff.punishments.create` |
| `/kick` | `/kick <player> [reason]` | Kick online player from server | `ultimatedonutsmp.staff.punishments.create` |
| `/blacklist` | `/blacklist <player> [reason]` | Issue IP/account blacklist | `ultimatedonutsmp.staff.punishments.blacklist` |
| `/unban` | `/unban <player> [reason]` | Remove active ban | `ultimatedonutsmp.staff.punishments.unban` |
| `/unmute` | `/unmute <player> [reason]` | Remove active mute | `ultimatedonutsmp.staff.punishments.unmute` |
| `/unblacklist` | `/unblacklist <player> [reason]` | Remove active blacklist | `ultimatedonutsmp.staff.punishments.unblacklist` |

---

## Administrator & Setup Commands

| Command | Usage Syntax | Description | Permission Node |
| :--- | :--- | :--- | :--- |
| `/cuboid` | `/cuboid <wand\|create <name>\|delete <name>\|list\|bind ...>` | Region selection & feature binding | `ultimatedonutsmp.admin.cuboid` |
| `/portal` | `/portal <create\|delete\|list\|setcuboid\|setdestination>` | Custom portal trigger setup | `ultimatedonutsmp.admin.portal` |
| `/arena` | `/arena <create\|delete\|setpos1\|setpos2\|setreturn\|enable>` | Duel arena setup and configuration | `ultimatedonutsmp.admin.arena` |
| `/ffaarena` | `/ffaarena <create\|delete\|setpos\|enable>` | Instanced FFA arena management | `ultimatedonutsmp.admin.ffaarena` |
| `/crate` | `/crate <create\|delete\|key\|keyall\|bind\|edit>` | Crate & virtual key administration | `ultimatedonutsmp.admin.crate` |
| `/spawner` | `/spawner <give\|set\|type\|stack>` | Custom spawner stack administration | `ultimatedonutsmp.admin.spawner` |
| `/addmoney` | `/addmoney <player> <amount>` | Add money to player balance | `ultimatedonutsmp.admin.addmoney` |
| `/removemoney` | `/removemoney <player> <amount>` | Deduct money from player balance | `ultimatedonutsmp.admin.removemoney` |
| `/setmoney` | `/setmoney <player> <amount>` | Set player money balance | `ultimatedonutsmp.admin.setmoney` |
| `/topsell` | `/topsell [gui\|items\|volume\|sellers\|export]` | Admin economy metrics and sell analytics | `ultimatedonutsmp.admin.topsell` |
| `/booster` | `/booster <give\|list>` | Give global server shard/money boosters | `ultimatedonutsmp.admin.booster` |
| `/billford` | `/billford <gui\|reload>` | Manage Billford rotating trades NPC | `ultimatedonutsmp.admin.billford` |
| `/serverwipe` | `/serverwipe <confirm\|cancel>` | Guarded admin server wipe execution | `ultimatedonutsmp.admin.serverwipe` |
| `/uds` | `/uds <reload|version|status>` | Main plugin administration & hot-reload | `ultimatedonutsmp.admin.uds` |

---

## Spawner Permissions

| Permission Node | Default | Description |
| :--- | :--- | :--- |
| `ultimatedonutsmp.spawner.bypass` | `false` | Break spawners without a Silk Touch pickaxe while `SETTINGS.REQUIRE_SILK_TOUCH` is enabled in `spawners.yml`. Registered with `default: false`, so operators do not receive it automatically. Assign it explicitly via LuckPerms. |

---

## RTP Rank Permissions

These nodes are not registered in `plugin.yml` and are read straight off the player, so they work with
LuckPerms or any other permission plugin. Assign them per rank.

| Permission Node | Default | Description |
| :--- | :--- | :--- |
| `ultimatedonutsmp.rtp.cooldown.<seconds>` | `false` | Override the `/rtp` cooldown for this player, in seconds. `ultimatedonutsmp.rtp.cooldown.3` gives a 3 second cooldown, and `ultimatedonutsmp.rtp.cooldown.0` removes the cooldown entirely. |
| `ultimatedonutsmp.rtp.priority.<weight>` | `false` | Position in the RTP waiting queue when all slots are busy. Higher weight is served first. |

When a player holds more than one cooldown node the **lowest** value wins, so stacked ranks always give
the player the fastest cooldown they are entitled to. A player with no cooldown node falls back to the
per-world `WORLD-SETTINGS.<world>.COOLDOWN` value in `rtp.yml`.

Named rank nodes can be mapped to a cooldown instead of using numeric nodes. See
`SETTINGS.RANK-COOLDOWNS.PERMISSIONS` in [Config-rtp.yml](Config-rtp.yml) — the shipped defaults map
`ultimatedonutsmp.rtp.cooldown.vip`, `.vip+` and `.vip++` to 15, 10 and 3 seconds.

Set `SETTINGS.RANK-COOLDOWNS.ENABLED: false` in `rtp.yml` to ignore every cooldown permission and use
the per-world value for everyone.

---

## Media Rank & Badge Permissions

Media permissions are registered with `default: false` and require explicit assignment via LuckPerms (or explicit permission attachment). Being an OP player does not automatically grant media badge status.

| Permission Node | Default | Description |
| :--- | :--- | :--- |
| `rank.media` | `false` | Display configurable Media tablist badge. Must be assigned explicitly via LuckPerms. |
| `rank.media.plus` | `false` | Display configurable Media+ tablist badge. Must be assigned explicitly via LuckPerms. |
| `rank.media.include` | `false` | Include player in media badge handling. Must be assigned explicitly via LuckPerms. |
