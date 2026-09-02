# Frequently Asked Questions (FAQ) & Troubleshooting

This page provides answers and troubleshooting steps for common questions, configuration issues, and error resolutions when running **UltimateDonutSMP**.

---

## 1. PlaceholderAPI & Placeholders Troubleshooting

### Question: Why are my `%economy_*%` placeholders showing as unparsed raw text (e.g. `%economy_money%`) or blank in scoreboards, chat, tablists, or DeluxeMenus?

#### Root Causes & Solutions:

1. **PlaceholderAPI is not installed**:
   - **Fix**: UltimateDonutSMP requires the **PlaceholderAPI** plugin to parse `%economy_*%` tags. Download and place `PlaceholderAPI.jar` into your server's `plugins/` folder and restart the server.

2. **UltimateDonutSMP started before PlaceholderAPI**:
   - **Fix**: Place `PlaceholderAPI.jar` in the `plugins/` directory and ensure soft dependencies load correctly. Run `/uds reload` or restart the server to trigger the expansion registration.

3. **Typo in Placeholder Identifier**:
   - **Fix**: UltimateDonutSMP uses `%economy_*%` as its identifier prefix.
     - Correct: `%economy_money%`, `%economy_shards%`, `%economy_team%`
     - Incorrect: `%ultimatedonutsmp_money%`, `%uds_money%`

4. **Player is Offline or Context Missing**:
   - **Fix**: Placeholders like `%economy_ping%`, `%economy_shard_cuboid_status%`, or `%economy_shard_cuboid_display%` require an active online player session. When called from console or headless holograms without player context, they return default fallback values (`0`, `outside`, `-`).

5. **Display plugin does not support PlaceholderAPI**:
   - **Fix**: Verify that your scoreboard, tablist, chat formatting, or hologram plugin has PlaceholderAPI integration enabled in its configuration.

---

## 2. Vault & Economy Integration

### Question: Why are third-party plugins (like ChestShop, CMI, or ShopGUIPlus) not detecting player money balances?

#### Solutions:
- **Install Vault**: UltimateDonutSMP registers itself as a Vault Economy provider on startup. Ensure `Vault.jar` is installed in your `plugins/` folder.
- **Check Startup Logs**: Look for `[UltimateDonutSMP] Vault economy provider registered successfully` in server startup logs.
- **Remove Conflicting Economy Plugins**: Remove plugins like EssentialsX Economy or Reserve that might override the Vault economy registration.

---

## 3. Database & Storage Issues

### Question: Why is player money, shards, or home data resetting after a server restart?

#### Solutions:
- **SQLite File Permissions**: If using SQLite (default), ensure the server process has read/write permissions for `plugins/UltimateDonutSmp/database.db`.
- **MySQL / MongoDB Credentials**: If using MySQL or MongoDB (`database.yml`), check that your database host, port, username, and password are correct and that the database server allows remote connections.
- **Check Console Logs**: Check for SQL connection timeouts or pool exhaustion errors on startup.

---

## 4. Cuboids & Portals Troubleshooting

### Question: Why is `/portal` not teleporting players or `/cuboid bind` not giving AFK/Shard rewards?

#### Solutions:
- **Verify Region Bounds**: Use `/cuboid list` to ensure the target cuboid region exists and spans the correct coordinates.
- **Check Binding Status**: For AFK or Shard zones, run:
  ```bash
  /cuboid bind <cuboid_name> spawn true
  /cuboid bind <cuboid_name> shard true
  ```
- **Set Warp / Spawn Locations**: If a portal has destination type `WARP`, ensure the specified warp name exists (`/warp`).

---

## 5. Spawners & Silk Touch

### Question: Why do custom spawners break into default pig spawners or fail to drop when mined?

#### Solutions:
- **Silk Touch Requirement**: By default, breaking spawners requires a pickaxe enchanted with Silk Touch. Only Creative mode and the `ultimatedonutsmp.spawner.bypass` permission are exempt. Operators are **not** exempt: that node is registered with `default: false` and must be assigned explicitly via LuckPerms.
- **Check `spawners.yml`**: Ensure `SETTINGS.REQUIRE_SILK_TOUCH: true/false` is configured according to your server rules.

---

## 6. Staff Mode & Vanish

### Question: Why can players still see vanished staff members in tablist or game?

#### Solutions:
- **Install ProtocolLib**: UltimateDonutSMP uses **ProtocolLib** for packet-level vanish suppression, chest animation silencing, and equipment hiding. Install `ProtocolLib.jar` for full vanish protection.
- **Check Staff Permissions**: Ensure normal players do not have see-vanish permissions (`ultimatedonutsmp.admin.vanish.see`).

---

## 7. Duels & Fast Crystals

### Question: Why are End Crystals placing at normal vanilla speed during duels?

#### Solutions:
- Check `duels.yml` and ensure `FAST-CRYSTAL.ENABLED: true` and `PLACEMENT-DELAY-TICKS: 0`.
- Verify that ProtocolLib is active to handle zero-tick crystal packet processing.

---

## 8. Player Ranks, Plus (+), and Media Privileges

### Question: How do I give Plus (+), Media, Streamer, or Donut+ ranks and privileges to a player?

#### 1. Assigning Rank Groups via LuckPerms
To assign a permanent or temporary rank group to a player using **LuckPerms**:
- **Permanent Rank**:
  ```bash
  /lp user <player> parent add <group_name>
  ```
  *Example*: `/lp user Steve parent add plus` or `/lp user Steve parent add media`
- **Temporary Rank (e.g. 30 Days)**:
  ```bash
  /lp user <player> parent addtemp <group_name> 30d
  ```

#### 2. Configuring Prefix Tags (Chat & Tablist)
Set custom rank prefixes for the `+` or `MEDIA` rank groups in LuckPerms:
```bash
/lp group plus meta setprefix 100 "&d[DONUT+] "
/lp group media meta setprefix 100 "&c[MEDIA] "
```

#### 3. Granting UltimateDonutSMP Rank Perks
Attach specific UltimateDonutSMP permission nodes to your `plus` or `media` groups:
- **Expanded Home Limits**: `ultimatedonutsmp.command.sethome.multiple.<amount>`
- **Ender Chest Row Expansion**: `ultimatedonutsmp.enderchest.rows.<1-6>`
- **RTP Cooldown Bypass**: `ultimatedonutsmp.command.rtp.bypasscooldown`
- **Key Giveaways & Rewards**:
  - Give Crate Keys: `/crate key <player> <crate_name> <amount>`
  - KeyAll Broadcast: `/crate keyall <crate_name> <amount>`
  - Server Multiplier Booster: `/booster give <player> <money|shards> <multiplier> <seconds>`

---

## 9. Transferring & Importing Player Homes (Home Import / Export)

### Question: How do I transfer or import player homes from an old server or database to a new one?

#### Option 1: Direct SQLite File Copy (Same Plugin Instance)
If you are moving your server to a new host or updating server files:
1. Stop both old and new Minecraft servers.
2. Copy `plugins/UltimateDonutSmp/database.db` from your old server folder to `plugins/UltimateDonutSmp/database.db` on your new server.
3. Start the new server. All player homes, balances, and statistics will be loaded automatically.

#### Option 2: MySQL / MongoDB Migration
If migrating from SQLite to MySQL or between database instances:
1. Configure connection credentials in `plugins/UltimateDonutSmp/database.yml`.
2. Export the `homes` table from your old database.
3. Import the SQL table structure:
   ```sql
   CREATE TABLE IF NOT EXISTS homes (
       player_uuid VARCHAR(36) NOT NULL,
       home_name VARCHAR(64) NOT NULL,
       world VARCHAR(64) NOT NULL,
       x DOUBLE NOT NULL,
       y DOUBLE NOT NULL,
       z DOUBLE NOT NULL,
       yaw FLOAT NOT NULL,
       pitch FLOAT NOT NULL,
       created_at BIGINT DEFAULT 0,
       PRIMARY KEY (player_uuid, home_name)
   );
   ```
4. Insert your home records:
   ```sql
   INSERT INTO homes (player_uuid, home_name, world, x, y, z, yaw, pitch, created_at)
   VALUES ('player-uuid-here', 'home_name', 'world', 100.5, 64.0, -200.5, 0.0, 0.0, 1600000000);
   ```

#### Option 3: Importing Legacy Homes from EssentialsX
If migrating from EssentialsX to UltimateDonutSMP:
- EssentialsX stores player homes inside `plugins/Essentials/userdata/<uuid>.yml` under the `homes:` key.
- You can parse the YAML coordinates from Essentials userdata files and insert them into the `homes` SQL table using a standard SQL script or database utility (such as DB Browser for SQLite).

---

## 10. Shard Anti-AFK & Disabling "Move to keep earning shards" (`MIN-MOVEMENT-BLOCKS`)

### Question: Why do players see "Move to keep earning shards (0/5)" and how do I disable the movement requirement?

#### Explanation:
By default, UltimateDonutSMP enforces anti-AFK movement checks inside Shard Cuboids (or Everywhere Shards) so that players cannot earn free passive Shards by standing completely still or macroing without moving.

#### How to Disable the Movement Requirement Completely:
If you want players to earn Shards passively while standing completely still inside Shard Cuboids (or anywhere on the server):

1. Open `plugins/UltimateDonutSmp/config.yml`.
2. Locate the `SHARDS` section under `CUBOIDS` or `EVERYWHERE`.
3. Change `MIN-MOVEMENT-BLOCKS` from `5` to `0`:

```yaml
SHARDS:
  CUBOIDS:
    REGIONS:
      spawn:
        # Set to 0 to allow passive AFK shard earning without moving
        MIN-MOVEMENT-BLOCKS: 0
        RECENT-MOVEMENT-WINDOW: 15
```

4. Save `config.yml` and run `/uds reload` in-game or from console.

---

## 11. Enabling Global Server-Wide Death Messages (`death-messages.yml`)

### Question: Why are death messages only showing to nearby players, and how do I turn on global death messages server-wide?

#### Explanation:
By default, UltimateDonutSMP limits death messages to nearby players within a chunk radius (`RADIUS: true`, `CHUNKS: 5`) to reduce chat spam in crowded areas or spawn arenas.

#### How to Enable Global Death Messages:
To make all death messages broadcast globally across the entire server to every online player:

1. Open `plugins/UltimateDonutSmp/death-messages.yml`.
2. Locate the `SETTINGS` block at the top of the file:

```yaml
SETTINGS:
  # Set RADIUS to false to disable distance limits and broadcast death messages globally
  RADIUS: false
  CHUNKS: 5

MESSAGES:
  ENABLED: true
  PREFIX: '&c☠ '
```

3. Ensure `MESSAGES.ENABLED` is set to `true`.
4. Save `death-messages.yml` and run `/uds reload` in-game or from console.

---

## 12. Clarification on `/uds setup setspawn` vs Player Death Respawn

### Question: Does `/uds setup setspawn` set where players respawn when they die?

#### Clarification & Actual Purpose:
- **Yes, through the spawn hub**. `/uds setup setspawn` (or `/setspawn`) saves one location, the **Global Server Spawn Hub Location** (e.g., Central Marketplace, Server Shop Hub, or Spawn Arena). That is where players land when executing `/spawn` or walking through Portal triggers (`/portal`), and it is also where they land when they die.
- **Player Death Respawn**: On death the plugin replaces the vanilla respawn point with that same spawn hub. There is no separate death location to configure, so moving spawn moves where your players come back.
- **The one exception**: `SETTINGS.RESPAWN-ON-BED`. Turn it on and a player who has slept in a bed or charged a **Respawn Anchor** returns there instead. Everybody without one still goes to the spawn hub.
- **Shipped default**: `RESPAWN-ON-BED` is `false` out of the box, so a fresh install sends every death to the spawn hub. Leave it on `false` to keep beds out of it entirely.

```yaml
SETTINGS:
  # false: every death sends the player to the spawn hub
  # true: beds and charged Respawn Anchors win, everyone else still goes to the spawn hub
  RESPAWN-ON-BED: false
```

> If `/setspawn` has never been run there is no hub to send anybody to, and deaths fall back to vanilla behaviour. Run it once while standing where you want players to land.

---

## 13. LuckPerms & LuckPermsChat (LPC) Integration & Tablist Displays

### Question: I use LuckPerms and LuckPermsChat (LPC) for my own rank system. Does UltimateDonutSMP override my chat format or tablist display, and is it visible to other players?

#### Explanation:
UltimateDonutSMP includes its own built-in chat formatting engine and tablist manager, which read player rank prefixes and suffixes from **LuckPerms** via Vault or PlaceholderAPI. Depending on your configuration, UltimateDonutSMP can either format chat for you or step aside to let **LuckPermsChat (LPC)**, **EssentialsChat**, or **TAB** manage your chat and tablist display for all players.

#### How to Prevent Overrides & Configure Your Rank System:

1. **If you want LuckPermsChat (LPC) or EssentialsChat to handle chat formatting**:
   - Disable UltimateDonutSMP's chat formatting in `plugins/UltimateDonutSmp/config.yml`:
     ```yaml
     CHAT:
       FORMAT-ENABLED: false  # Set to false to allow LPC / EssentialsChat to handle chat
     ```
   - Run `/uds reload`. Now your LuckPermsChat format will be 100% active without any overrides or double-prefixing.

2. **If you want UltimateDonutSMP to handle chat formatting with LuckPerms ranks**:
   - Keep `CHAT.FORMAT-ENABLED: true` in `config.yml`.
   - Configure the chat format string to include the LuckPerms prefix tag:
     ```yaml
     CHAT:
       FORMAT: '&f%luckperms_prefix%%player%&7: &f%message%'
     ```
   - All online players will see your LuckPerms rank prefix in global chat.

3. **Configuring Rank Displays in Tablist**:
   - If using UltimateDonutSMP's built-in tablist (`scoreboard.yml`), set player prefixes using PlaceholderAPI:
     ```yaml
     TABLIST:
       ENABLED: true
       HEADER:
         - '&e&lYOUR SERVER NAME'
       FOOTER:
         - '&7Rank: %luckperms_prefix%'
     ```
   - If using a third-party tablist plugin (e.g. **TAB by NEYNAM**), disable UltimateDonutSMP's tablist in `scoreboard.yml` by setting `TABLIST.ENABLED: false` so it does not conflict with your custom tablist display.

---

## 14. Adding Splash Potions of Strength & Mixed Potions to `/shop` (`shop.yml`)

### Question: How do I add a Splash Potion of Strength to `/shop`, and can I create mixed potions with multiple effects (e.g., Fire Resistance + Strength + Speed)?

#### Explanation:
In UltimateDonutSMP's `shop.yml`, you can add custom potion items to any shop category (e.g., `GEAR-MENU` or a dedicated `POTION-MENU`).

There are two primary methods to set up potions in `/shop`:
1. **Method 1 (Recommended for Custom & Mixed Potions)**: Using the `COMMAND:` property to execute a vanilla `/give` command with custom potion component NBT data when purchased. This allows unlimited mixing of effects (Strength, Speed, Fire Resistance, Haste, etc.), custom amplifiers, and custom durations.
2. **Method 2 (Standard Vanilla Potions)**: Using `MATERIAL: SPLASH_POTION` combined with `POTION_TYPE`.

---

#### Method 1: Custom & Mixed Potions via Console Command (`COMMAND:`)

To add a **Splash Potion of Strength II**:
```yaml
POTION-STRENGTH-2:
  CURRENCY: MONEY
  MATERIAL: SPLASH_POTION
  DISPLAY-NAME: '&c&lSplash Potion of Strength II'
  SLOT: 10
  PRICE-PER-UNIT: 500.0
  COMMAND: 'minecraft:give {player} splash_potion[potion_contents={custom_effects:[{id:"strength",amplifier:1,duration:1800}]}] 1'
  LORE:
    - '&fBuy price: &a$500'
    - '&7Effect:'
    - ' &c• Strength II (1:30)'
```

To add a **Mixed Multi-Effect Potion** (Strength II + Speed II + Fire Resistance):
```yaml
SUPER-BUFF-POTION:
  CURRENCY: MONEY
  MATERIAL: SPLASH_POTION
  DISPLAY-NAME: '&e&lSuper Battle Elixir (Mixed Potion)'
  SLOT: 11
  PRICE-PER-UNIT: 2500.0
  COMMAND: 'minecraft:give {player} splash_potion[potion_contents={custom_effects:[{id:"strength",amplifier:1,duration:1800},{id:"speed",amplifier:1,duration:3600},{id:"fire_resistance",amplifier:0,duration:6000}]}] 1'
  LORE:
    - '&fBuy price: &a$2,500'
    - '&7Effects:'
    - ' &c• Strength II (1:30)'
    - ' &b• Speed II (3:00)'
    - ' &6• Fire Resistance (5:00)'
```

*Note*:
- `amplifier: 0` = Level 1 effect (e.g. Strength I, Speed I).
- `amplifier: 1` = Level 2 effect (e.g. Strength II, Speed II).
- `duration: 20` = 1 second (e.g. `1800` ticks = 90 seconds / 1:30 min).

---

#### Method 2: Standard Vanilla Splash Potions (`POTION_TYPE:`)

For single-effect vanilla potions, specify the `POTION_TYPE` key:
```yaml
SPLASH-STRENGTH-POTION:
  CURRENCY: MONEY
  MATERIAL: SPLASH_POTION
  POTION_TYPE: STRONG_STRENGTH  # Options: STRENGTH, STRONG_STRENGTH (Strength II), LONG_STRENGTH (Strength 8:00)
  DISPLAY-NAME: '&cSplash Potion of Strength II'
  SLOT: 12
  PRICE-PER-UNIT: 500.0
  LORE:
    - '&fBuy price: &a$500'
```

#### Step-by-Step Setup Guide:
1. Open `plugins/UltimateDonutSmp/shop.yml`.
2. Locate your target menu section (e.g., `GEAR-MENU` or create a new `POTION-MENU`).
3. Paste the potion configuration entry into the `ITEMS:` section.
4. Save `shop.yml` and run `/uds reload` in-game or from console.

---

## 15. Keeping `Buy price:` Lore in Step With `PRICE-PER-UNIT` (`shop.yml`)

### Question: I changed a shop item's price, or moved a menu from shards to money. Do I have to rewrite every `Buy price:` lore line by hand?

No. A lore line that reads `Buy price:` and carries no `{price` placeholder has everything after its first colon replaced with the live price, in that item's own currency, every time the menu is drawn. The amount sitting in the file is never shown, so `'&fBuy price: &a$1'` still displays the real figure. `Harga beli:` behaves the same way on Indonesian menus, and the confirm screen hides the duplicate line rather than printing the price twice.

If you would rather write the label yourself, `{price_formatted}` renders the price with its symbol and colour anywhere in the line, and `{price}` renders the bare number.

The full list of placeholders, and what the auto-filled line does with colour codes and small caps, is on the [`shop.yml` configuration page](Config-shop.yml).

Wording you invented is still yours to maintain: effect lines, menu titles and anything else that mentions a currency by name does not update itself.

---

## 16. Text Written In Capitals Comes Back In Title Case

### Question: I put `'&cWELCOME TO THE SERVER'` in my config and players see `Welcome To The Server`. What is rewriting it?

The plugin is, on purpose. It checks every line it colours, and any line whose visible letters are
all uppercase gets retyped in Title Case before it reaches the screen. The same check covers chat
lines, menu titles and lore, scoreboard rows, the tablist and the server list MOTD, since all of
them run through the one formatter.

Two conditions have to hold before a line is touched. It needs at least one colour code, hex code,
MiniMessage tag or placeholder in it, and every letter outside those markers has to be uppercase.
Break either one and the line is printed exactly as you typed it:

```yaml
# shown as: Welcome To The Server
'&cWELCOME TO THE SERVER'

# shown as written, because no colour code or placeholder is there to trigger the check
'WELCOME TO THE SERVER'

# shown as written, because "to the server" is lowercase
'&cWELCOME to the server'
```

Colour codes, hex codes and placeholders are stepped over rather than rewritten, so `%player%` and
`&#FF0000` survive intact. Capitalisation starts again after a space, a hyphen, a slash or an
underscore, which is why `'&cWELCOME-TO/THE_SERVER'` reads back as `Welcome-To/The_Server`.
Acronyms come off worst: `'&c&lDONUT SMP'` lands as `Donut Smp`, and there is no list of words you
can exempt.

The check also ignores the words `currently` and `status` while it looks, so `'&aStatus: ONLINE'`
counts as all caps even though it is not, and shows as `Status: Online`.

If you want a shouty line and its colour at the same time, unicode small caps get through. The check
does not read them as uppercase letters, so they survive untouched. Paste the characters straight
in, or write them as escapes if your editor makes that awkward:

```yaml
# both shown as: ᴡᴇʟᴄᴏᴍᴇ
'&cᴡᴇʟᴄᴏᴍᴇ'
'&c\u1D21\u1D07\u029F\u1D04\u1D0F\u1D0D\u1D07'
```
