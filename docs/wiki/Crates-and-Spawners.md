# Crates & Spawners Guide

UltimateDonutSMP provides custom Crates with virtual & physical keys, key-all broadcast timers, Donut-style stacked spawners, Amethyst toolsets, and GUI enchantment workflows.

---

## Crates System (`/crate` & `/crates`)

### Player Commands:
- `/crates`: Opens the visual Crates menu where players can preview reward odds or open crates using virtual keys.

### Administrator Commands (`/crate`):
- `/crate create <name>` – Create new crate type.
- `/crate delete <name>` – Remove crate type.
- `/crate key <player> <crate_name> <amount>` – Give virtual crate keys to player.
- `/crate keyall <crate_name> <amount>` – Broadcast & distribute keys to all online players.
- `/crate take <player> <crate_name> <amount>` – Remove virtual keys.
- `/crate bind <crate_name>` – Bind targeted physical block to a crate.
- `/crate unbind` – Unbind crate block.
- `/crate edit <crate_name>` – Open GUI reward drop table editor.
- `/crate info <crate_name>` – Display crate properties and reward weights.

---

## Donut-Style Stacked Spawners (`/spawner`)

UltimateDonutSMP includes custom mob spawners engineered for high-performance mob farms without lag:

### Features:
- **Spawner Stacking**: Place matching spawners onto an existing spawner to stack them (e.g., `x64 Pig Spawner`).
- **Upgrade System**: Upgrade spawner rate, spawn count, and mob drop multipliers via GUI.
- **Grouped Loot Storage**: Drops collect with each item type kept together in one run, so a skeleton spawner shows all of its arrows and then all of its bones instead of alternating the two down every page. Storage that filled up before this landed is tidied the next time the spawner produces anything.
- **Break Safeguards**: Requires Silk Touch or admin bypass permissions to break stacked spawners without losing count. Creative mode is exempt from that requirement, and a spawner broken in Creative is removed instead of being handed back, matching the way placing one in Creative costs nothing.

### Player Spawner Commands (`/spawner`):
- `/spawner info` – Inspect the spawner block you are looking at. Owner, stack size, stored loot and coordinates only show on spawners you are allowed to access: your own, a teammate's under `OWNER_AND_TEAM`, any of them under `PUBLIC` or while `ALLOW_SPAWNER_STEAL` is on. Anyone else sees the mob type and nothing further.
- `/spawner split <amount>` – Split the spawner item in your hand into a smaller stack.

### Admin Spawner Commands (`/spawner`):
- `/spawner` or `/spawner panel` – Open the spawner admin panel.
- `/spawner give <player> <entity_type> [amount]` – Give a stacked spawner item to a player.
- `/spawner remove` – Remove the spawner block you are looking at. `/spawner forcebreak` does the same thing.
- `/spawner reload` – Reload `spawners.yml`.

The admin entries need `ultimatedonutsmp.admin.spawner`, which also unlocks the full `/spawner info` breakdown on any spawner. The command itself is gated by `ultimatedonutsmp.command.spawner`, and every player holds that one by default.

---

## Custom Items & Enchantments

### 1. Amethyst Tools (`amethyst-tools.yml`)
Special end-game toolset crafted or rewarded via crates:
- **Amethyst Pickaxe**: Has special vein-mine and auto-smelt abilities.
- **Amethyst Sword**: Deals extra damage to mobs and triggers custom particle effects.
- Configure properties, lore, durability, and abilities in `amethyst-tools.yml`.

### 2. Custom Enchantment GUI (`enchantments.yml`)
Replaces basic vanilla enchanting with a sleek GUI menu where players can purchase custom enchantments using money or shards.
