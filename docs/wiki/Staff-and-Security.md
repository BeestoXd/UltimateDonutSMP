# Staff & Security Utilities Guide

UltimateDonutSMP provides a comprehensive suite of staff moderation tools, anti-cheat detection lures, anti-ESP alerts, crash protection, chat filtering, Discord webhooks, and guarded server wipes.

---

## Staff Utilities

### 1. Staff Mode (`/staffmode` or `/staff`)
Toggles Staff Mode for authorized moderators:
- Gives custom hotbar items: Fast-Fly, Vanish toggle, Freeze tool, Random Teleport, Player Inspector, and Counter.
- Separates staff inventory from normal player survival inventory.
- Permission: `ultimatedonutsmp.admin.staffmode`

### 2. Vanish (`/vanish` or `/v`)
Hides the moderator completely from online tab lists, player join/leave messages, and in-game rendering.
- Suppresses chest opening animations and footstep sounds.
- Permission: `ultimatedonutsmp.admin.vanish`

### 3. Freeze (`/freeze <player>`)
Freezes the target player on top of an ice block and disables movement, block breaking, and command execution.
- Prompts target player with unfreeze instructions or Discord support links.
- Permission: `ultimatedonutsmp.admin.freeze`

### 4. Inventory & Ender Chest Inspection
- Inspect player inventory in real-time: `/invsee <player>`
- Inspect player Ender Chest: `/ecsee <player>`
- Allows staff to add, remove, or modify items directly inside player inventories.

---

## Punishments

### 1. Punishment List (`/punishments`)
Run `/punishments` with no arguments to browse every punishment on the server in one GUI, newest first. Each entry shows the punished player, the type, the reason, the staff member who issued it, the date, and the expiry (`Never` for a permanent punishment).

Controls sit along the bottom row:

| Button | Action |
| --- | --- |
| State Filter | Cycles All / Active / Inactive. Inactive covers both expired and manually removed records |
| Type Filter | Cycles All / Ban / Mute / Warn / Kick / Blacklist |
| Sort Order | Switches between newest and oldest first |
| Search | Left-click opens a sign to type a player name, right-click clears it |
| Refresh | Re-reads the list |

Search matches any part of the stored player name and ignores case, so `rod` finds `Cuteboyrodney`. A full UUID also works. Left-clicking an entry opens that player's full history; shift-right-clicking deletes the record if the viewer holds `ultimatedonutsmp.staff.punishments.delete`.

Pages are read in the background rather than on the server thread, so the menu opens on a loading placeholder and fills in once the query returns. On a large history the first frame may be visible for a moment.

### 2. Player History (`/punishments <player>`)
Passing a player name opens that player's history on its own, with the same state and type filters. This is also reachable from the profile viewer.

- Both views require `ultimatedonutsmp.staff.punishments.view`.
- Both are styled from `PUNISHMENTS-LIST-MENU` and `PUNISHMENT-HISTORY-MENU` in `menus.yml`.

---

## Chat & Anvil Moderation

### 1. Chat Filtering (`filter.yml`)
Automatically filters player chat for:
- Profanity & blacklisted words
- Anti-advertising (IP address & domain URL blocking)
- Anti-caps (converts ALL CAPS messages to lowercase)
- Anti-spam delay between messages

### 2. Anvil Moderation (`/anvilmoderation` & `anvil-moderation.yml`)
Filters illegal, profane, or scam links renamed on item anvils before the item is created.

---

## Discord Webhook Logging (`discord.yml`)

Integrates your server directly with Discord webhooks to broadcast live events to private staff channels:
- **Death Logs**: Broadcasts player death events with killer, weapon, and coordinates.
- **Staff Action Logs**: Logs staff mode toggles, vanish, freeze, invsee, and punishments.
- **Reports & HelpOp**: Sends player `/report` and `/helpop` alerts directly to Discord staff channels.
- **Auction House Logs**: Logs high-value marketplace listings and sales.

---

## Detection & Anti-Cheat Lures

### 1. Spawn-Stash Bait (`/spawnstash` & `spawn-stash.yml`)
Spawns fake hidden chests populated with high-tier loot under spawn or wild areas:
- Alerts staff whenever an x-raying player digs directly to the bait chest.
- Commands: `/spawnstash setup`, `/spawnstash list`, `/spawnstash give`.

### 2. Fake Player Bait
Generates invisible fake player entities around players suspected of using KillAura or Auto-Clickers.

### 3. Spawner Anti-ESP
Hides spawner block packet data from players beyond visual raycast distance to prevent X-Ray / ESP client hacks from discovering spawner coordinates.

---

## Operations & Guarded Server Wipes (`/serverwipe`)

UltimateDonutSMP includes a guarded `/serverwipe` command to safely reset player statistics, balances, inventories, and homes for new server seasons:

- Command: `/serverwipe confirm`
- Prompts multi-stage confirmation to prevent accidental wipe execution.
- Automatically creates a pre-wipe SQL/JSON backup archive before resetting player state.
