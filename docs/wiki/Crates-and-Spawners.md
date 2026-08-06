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
- **Break Safeguards**: Requires Silk Touch or admin bypass permissions to break stacked spawners without losing count.

### Admin Spawner Commands (`/spawner`):
- `/spawner give <player> <entity_type> <amount>` – Give stacked spawner item to player.
- `/spawner set <entity_type>` – Change targeted spawner type.
- `/spawner type <entity_type>` – Change spawner type in hand.
- `/spawner stack <amount>` – Set stack count of targeted spawner block.
- `/spawner reload` – Reload `spawners.yml`.

---

## Custom Items & Enchantments

### 1. Amethyst Tools (`amethyst-tools.yml`)
Special end-game toolset crafted or rewarded via crates:
- **Amethyst Pickaxe**: Has special vein-mine and auto-smelt abilities.
- **Amethyst Sword**: Deals extra damage to mobs and triggers custom particle effects.
- Configure properties, lore, durability, and abilities in `amethyst-tools.yml`.

### 2. Custom Enchantment GUI (`enchantments.yml`)
Replaces basic vanilla enchanting with a sleek GUI menu where players can purchase custom enchantments using money or shards.
