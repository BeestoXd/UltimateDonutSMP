# Staff & Security Utilities Guide

UltimateDonutSMP provides a comprehensive suite of staff moderation tools, anti-cheat detection lures, anti-ESP alerts, crash protection, chat filtering, Discord webhooks, and guarded server wipes.

---

## Staff Utilities

### 1. Staff Mode (`/staffmode` or `/staff`)
Toggles Staff Mode for authorized moderators:
- Gives custom hotbar items: Fast-Fly, Vanish toggle, Freeze tool, Random Teleport, Player Inspector, and Counter.
- Separates staff inventory from normal player survival inventory.
- Pins those hotbar items to the moderator so they cannot be dropped or handed to anyone (`LOCK-TOOLS` in `staff-mode.yml`). Other items stay droppable.
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

### 5. Home Inspection (`/seehomes <player>`)
Opens the target's saved homes as a paged list, one bed per home, and clicking a bed sends the
staff member there. The world, along with the exact block the home sits on, is written into the
lore, and a home pointing at a world the server no longer loads shows as a barrier rather than
teleporting anyone into nothing. Offline players work too, since the homes come from the database
when nobody is holding them in memory.

This is the same list the profile viewer has always shown behind its Homes button; the command
skips the profile screen and drops you straight into it, which matters when you only need to check
where somebody built. `/homesee` does the same thing.

- Permission: `ultimatedonutsmp.staff.seehomes`, plus `ultimatedonutsmp.command.seehomes` to run the
  command at all. Anyone already carrying `ultimatedonutsmp.staff.profileviewer` passes the first
  check without a new node, since that permission already opens this menu.
- Styled from `PROFILE-VIEWER-HOMES-MENU` in `menus.yml`. Arriving from `/seehomes` swaps the Back
  button for `CLOSE-BUTTON`, because there is no profile screen to go back to.
- Turning off the `PROFILE_VIEWER` feature disables `/seehomes` alongside `/profileviewer`.

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

### 3. Protecting Ranks From Punishment

A punishment permission carries no notion of who it may be used on. A rank given
`ultimatedonutsmp.staff.punishments.offend` so it can hand out preset offenses can point `/offend` at
the owner just as easily as at a rule breaker, and the same goes for `/ban`, `/mute`, `/warn`,
`/kick` and `/blacklist`.

Give the ranks you want protected `ultimatedonutsmp.admin.punishments.exempt`. Staff below them are
told they cannot punish that player and nothing is recorded. Ranks that should still be able to act
on each other get `ultimatedonutsmp.admin.punishments.exempt.bypass` as well — `ultimatedonutsmp.admin.*`
covers both, so an admin group keeps its protection without losing the ability to punish other admins.

Both nodes default to `op`, so operators are protected from non-operator staff out of the box and can
still punish one another. The console is never blocked.

Keep the nodes out of `ultimatedonutsmp.staff.punishments.*`. A wildcard over that branch is a normal
way to set up a moderator rank, and it would hand the exemption to every moderator on the server.

Permissions are read off the player, so the check only applies while the target is online. Banning an
exempt player while they are offline still works.

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

### 3. Chat Logging (`/chatlog` & `CHAT.LOGGING` in `config.yml`)
Every public chat message is written to the sender's own log, alongside their `/msg` conversations
and the rest of their activity. `/chatlog` opens the whole server's chat newest first, `/chatlog
<player>` narrows it to one player, and `/logs <player>` shows that player's chat mixed in with
their shop, economy and death history. Each entry carries the player, the message and the time it
was sent.

Only messages that actually reach chat are stored, so anything a mute, the filter or the chat delay
blocked never appears. Turn the whole thing off with `CHAT.LOGGING.ENABLED`, or drop public and
private messages separately with `CHAT.LOGGING.PUBLIC-MESSAGES` and
`CHAT.LOGGING.PRIVATE-MESSAGES`.

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

### 2. Fake Player Bait (`/fakeplayer`)
Spawns a short-lived player bait at your feet. It copies your skin unless `FAKE-PLAYER.USE-DEFAULT-SKIN` is `true` in `staff-mode.yml`, in which case every bait uses Minecraft's default Steve or Alex skin. `HIDE-NAMETAG: true` (the bundled default) keeps prefix, name, and money text off the bait's head.

### 3. Spawner Anti-ESP
Hides spawner block packet data from players beyond visual raycast distance to prevent X-Ray / ESP client hacks from discovering spawner coordinates.

---

## Operations & Guarded Server Wipes (`/serverwipe`)

UltimateDonutSMP includes a guarded `/serverwipe` command to safely reset player statistics, balances, inventories, and homes for new server seasons:

- Command: `/serverwipe confirm`
- Prompts multi-stage confirmation to prevent accidental wipe execution.
- Automatically creates a pre-wipe SQL/JSON backup archive before resetting player state.

---

## Wiping One Player (`/playerwipe`)

Where `/serverwipe` resets the whole server, `/playerwipe` clears a single player. It is the command
to reach for when someone asks for a fresh start, or when a punished account should lose what it
gained. `/pwipe` and `/wipe` do the same thing.

Running `/playerwipe <player>` on its own shows what would go, broken down by category, and changes
nothing. Adding `confirm` carries it out:

```
/playerwipe Notch
/playerwipe Notch confirm
```

It works on offline players as well as online ones, and it clears:

- Kills, deaths, kill streaks, playtime, blocks placed and broken, and mobs killed
- Money (back to `SETTINGS.MONEY-PER-DEFAULT` in `config.yml`) and shards
- Homes, and their team. A leader taking a wipe disbands the team
- Ender chest contents and crate keys
- Shop favourites, sell history and sell totals
- Auction listings and claims, orders and deliveries
- Duel and FFA records, bounties on them and bounties they placed
- Friends, ignores, and their activity log

Punishments, IP history, and freeze or staff-mode state survive a wipe, so a ban history stays intact
and alt tracking still works. Spawners they placed are left standing as well, since those are blocks
in the world rather than stored progress.

### Undoing A Wipe (`/playerunwipe`)

Every wipe writes what it removed to a file in `plugins/UltimateDonutSmp/player-wipe-backups/` before
deleting anything, and `/playerunwipe` reads that file back. `/punwipe` and `/unwipe` are the same
command. It is the answer to a ban that turns out to have been wrong: clear the punishment, then hand
the account back what it had.

`/playerunwipe <player>` on its own reports who ran the wipe, when, and what the backup holds.
`confirm` puts it all back:

```
/playerunwipe Notch
/playerunwipe Notch confirm
```

The restore is exact rather than additive, so anything the account picked up between the wipe and the
restore is dropped in favour of what the backup holds. On a wrongly banned player that difference is
usually nothing, since they could not log in to earn any of it.

Backups are never deleted or overwritten, so a player wiped more than once keeps a file per wipe and
the restore uses the most recent. The one thing a restore cannot bring back is a team the player led,
because wiping a leader disbands the team outright and the team itself is not part of any single
player's data; their membership row is dropped rather than restored, leaving them free to join
another team. Filenames carry the player's name, their uuid and the time of the wipe, so an older
backup can be restored by hand: delete the newer files, or move the one you want into place.

### Wiping On A Ban (`offenses.yml`)

A wipe can also ride along with a punishment instead of being a second command. Each preset in
`offenses.yml` takes an optional `wipe` flag, and when `/offend` issues that offense the account is
cleared straight after the kick:

```yaml
offenses:
  duping:
    name: "Item Duping"
    type: BAN
    wipe: true
    durations:
      - "3d"
      - "perm"
```

The flag defaults to `false`, so nothing changes on a preset that does not mention it, and it is
read under any casing, so `wipe`, `WIPE` and `Wipe` all work.

It only fires on a real ban. A `MUTE`, `WARN` or `KICK` preset ignores it, and so does a tier of
`0s`, since that tier is issued as a warning rather than a ban. Staff running the command see the
number of records removed underneath the usual punishment confirmation.

The wipe itself is the same one `/playerwipe` performs, down to what survives it and the backup it
leaves behind, so `/playerunwipe` undoes it the same way. Turning it on for an offense your team hands
out often still fills the backup folder with accounts you meant to keep, so it suits things like
duping or botting rather than a first-strike chat rule.
