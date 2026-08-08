# Changelog - UltimateDonutSmp

All notable changes and behavioral modifications for the UltimateDonutSmp plugin on Paper, Spigot, and Folia server environments.

## [1.5-pre] - 2026-08-09

- fix(billford): prevent bundled billford default items from merging into user trade definitions (#60)
- fix(spawners): prevent spawners from disappearing after disconnect (#59)
- ci: replace action dependency with native git log extractor
- ci: guarantee changelog generation with fallback entry
- ci: rewrite changelog update step using python engine with full logging

## [1.4] - July – August 2026

### Added
- Added the `/offend` command and configurable `offenses.yml` rule preset system for staff moderation.
- Added fake join and leave broadcast messages when toggling vanish mode in staff mode.
- Added settings options to permanently disable mob spawning and hide particle effects while in vanish mode.
- Added a god mode command providing invincibility and damage protection for Paper and Folia servers.
- Added an auto-sell toggle button inside the sell GUI menu.
- Added shulker container inventory worth inspection displays.
- Added global currency customization and formatting options.
- Added shulker crate binding support.
- Added an administrative feature toggle menu.
- Added a startup console banner displaying custom ASCII art title, credits, and version details.
- Added command tab completion and auto-suggestions across custom plugin commands.
- Added an administrative sell statistics GUI (`/topsell` or `/sellstats`) to monitor top revenue-generating items, top volume items, top selling players, global sales history, and export detailed sales reports for economy rebalancing.
- Added automatic held-item filtering when opening the Auction House browser.
- Added automatic item category detection for Auction House listings.
- Added packet-based client-side item worth rendering to display price information without modifying item stack data.
- Added hopper extraction support for virtual spawner storage.
- Added localization support for friends system messages.
- Added a configuration option to disable movement distance requirements for earning shards by setting the threshold to zero.
- Added spawner drop filters to customize which mob drops are collected or filtered out.
- Added drop prevention rules to stop players from dropping items in the spawn region or while marked AFK.
- Added currency-specific wipe options to the administrative server reset system.
- Added flight support controls inside spawn and designated cuboid regions.
- Added playtime-restricted requirements before players can use Random Teleportation (RTP).
- Added an interactive confirmation GUI menu before executing `/sellall`.
- Added support for configuring specific destinations for AFK portal teleports.
- Added enchantment values into item worth calculations.
- Added editable and interactive ender chest inspections, allowing authorized players to modify contents.
- Added custom PlaceholderAPI expansion support to integrate player context parsing across external plugins.
- Added the ability to stack spawners by sneaking while placing them.
- Added PlaceholderAPI support to allow custom variables and player data in messages and menus.
- Added configuration options to toggle between global instant-sell and confirm-sell modes.
- Added custom sound effects when players sell items.
- Added a configuration setting to adjust the vertical position of key holograms above crates.
- Added a player activity logging system that records shop purchases, shop sales, auction listings, auction purchases, listing cancellations, balance transfers, admin balance adjustments, crate reward claims, and spawner placements and breakages.
- Added an in-game administrative logs viewer command `/logs <player>` with a paginated GUI displaying chronological action entries using distinct categoric icons.
- Added a simulated bot activity system to populate the Auction House and Orders marketplace on small servers.
- Added BOTS configuration settings in `auction-house.yml` and `orders.yml` to customize bot names, check intervals, listing chances, active limit caps, durations, and item templates.
- Added support for randomizing listing price, quantity, and vanilla enchantments for bot items and orders.
- Added inline documentation explaining each option in the bot configuration templates.
- Added configuration options to prevent custom-named and tamed entities from being removed by the clear-lag system.
- Added support for excluding specific entity types and dropped item materials from the clear-lag system via configuration lists.
- Unified Paper, Spigot, and Folia builds into a single JAR distribution.
- Added a startup update checker to notify server administrators of new plugin versions.
- Added Minecraft version and platform compatibility validation on server startup.
- Added listbound and extended unbind command support for bound items.
- Added configuration controls for explosion damage.
- Added automatic teleportation to spawn location for first-time joining players.
- Added maintenance mode commands and management functionality for Folia server environments.
- Added a spawner split command allowing players to easily divide stacked spawners.
- Added a fake-player anti-ESP NPC system to prevent radar and ESP exploits on Folia servers.
- Added a SpawnStash system that spawns interactive bait loot stashes around the spawn area.
- Added particle and visual effects when opening or interacting with ender chests.
- Added customizable rule settings and configurations for duel arena matches.
- Added interactive GUI menus to review and manage pending teleportation requests.

### Fixed
- Fixed SQLite database lock contention, `SQLITE_FULL` storage errors, MySQL TPS lag in sell stats, and main thread watchdog freezes.
- Fixed unformatted minimum and maximum price placeholders in the orders marketplace GUI menu.
- Fixed automatic database table and column migrations on startup with SQLite metadata compatibility.
- Fixed compact currency permissions and tablist refresh issues.
- Fixed tablist media badge permission node checks.
- Fixed default home slot limits to enforce five default slots.
- Fixed shop purchase payouts and currency display formatting.
- Fixed combat logout penalty handling to drop items on quit and apply death penalties safely on reconnecting.
- Fixed crate configuration synchronization preserving custom rewards.
- Fixed container sell payout calculations.
- Fixed Spigot tablist component parsing and skin avatar fallbacks.
- Fixed 1.21 API compatibility for Paper and Folia platforms.
- Fixed teleport menu area deletion interactions.
- Fixed sequential setup spawn and AFK slot persistence in main menus.
- Fixed god mode knockback damage cancellation.
- Fixed bound crate interactions for protected blocks.
- Fixed shard cuboid bind world assignment.
- Fixed compact money suffix casing display.
- Fixed setup locations and shard region boundaries.
- Fixed cuboid wand selection.
- Fixed Folia feature toggle switches.
- Fixed RTP world resolution and teammate particle rendering.
- Fixed spawner block state synchronization scheduling.
- Fixed TPA confirmation menu notifications.
- Fixed cave spiders and spawner mobs bypassing nearby mob spawn prevention settings.
- Fixed combat action bar displays to support both `{time}` and `${time}` placeholders.
- Fixed item worth visual overlays disappearing upon re-opening inventory menus.
- Fixed automatic configuration synchronization from overwriting custom market bot settings.
- Fixed fallback resolution prioritizing root-level item lists over nested bot templates for marketplace bots.
- Fixed region threading crash exceptions during player teleportation on Folia servers.
- Fixed player home and location loading errors when targeting unloaded worlds.
- Fixed crate interaction spamming exploits and database write lock bypasses.
- Fixed block mining progress resets, crafting table interaction errors, and ghost item glitches caused by worth display updates.
- Fixed creative mode interactions triggering item modification handlers and tool countdown timers.
- Fixed inventory item stacking glitches by isolating worth display stripping to identical item materials.
- Fixed native item pickup stacking when holding matching items in the main hand.
