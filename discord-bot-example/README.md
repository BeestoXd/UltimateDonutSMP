# UltimateDonutSmp Discord Bot Example

Example discord.js bot that reads the plugin database. It covers survival stats and leaderboards,
and the ranked PvP arena: account linking, competitive stats, tier leaderboards, and a match result
posted into a channel every time a ranked fight finishes.

The bot only ever reads the plugin database. Everything it owns, meaning Discord account links and
how far the match watcher has read, lives in its own SQLite file.

## Setup

1. Install Node.js `22.12.0` or newer.
2. Copy `.env.example` to `.env`.
3. Fill:
   - `DISCORD_TOKEN`
   - `DISCORD_CLIENT_ID`
   - `DISCORD_GUILD_ID` for fast test-server command deploys, or leave it empty for global commands
   - database settings
4. Install dependencies:

```bash
npm install
```

5. Start the bot:

```bash
npm start
```

Slash commands are registered automatically every time the bot starts. You can still deploy manually
without starting the bot:

```bash
npm run deploy
```

`DISCORD_CLIENT_ID` and `DISCORD_GUILD_ID` must be numeric Discord snowflake IDs. Do not leave
placeholder values such as `your_test_server_id` in `.env`.

## Commands

```txt
/stats player:<minecraft username or uuid>
/leaderboard type:<money|shards|kills|deaths|playtime|...> limit:<1-10>

/sync code:<code from /pvp sync in game>
/unsync
/mystats
/tier stats player:<minecraft username or uuid>
/tier leaderboard type:<elo|level|kills|deaths|streak|killstreak|wins|losses> limit:<1-10>
```

## Linking an account

A player runs `/pvp sync` in game. The plugin gives them a short one-time code and writes it to the
`pvp_sync_codes` table. They then run `/sync code:<that code>` in Discord, and the bot checks the
code, confirms it has not expired or been spent, and records the link on its own side.

The plugin never learns anybody's Discord id, which is why the bot can keep the plugin database
read-only. Codes expire after ten minutes by default, and asking for a new one in game replaces the
old one. Every `/sync` and `/unsync` reply is ephemeral so a live code never sits in a public
channel.

`/mystats` uses that link. `/tier stats` takes a username instead and needs no link at all.

## Match results

Set `PVP_MATCH_CHANNEL_ID` and the bot posts a result embed after every ranked match, showing the
winner, the duration, and both fighters' rank, Elo change, hits, crystals and final health.

The plugin writes one `pvp_matches` row when a match ends, and the bot polls for rows past the last
one it posted. The first run does not backfill: a server that has been up for months would otherwise
dump its whole history into the channel the moment the bot starts. After that, a restart resumes
where it stopped rather than losing what happened while it was down.

```env
PVP_MATCH_CHANNEL_ID=123456789012345678
PVP_MATCH_POLL_SECONDS=15
```

## Ranks

The rank each Elo score maps to comes from `PVP_RANKS`, which mirrors the `RANKS` section of the
plugin's `pvp.yml`:

```env
PVP_RANKS=LT5:0,LT4:100,LT3:200,LT2:300,LT1:400,HT5:600,HT4:800,HT3:1000,HT2:1200,HT1:1500
```

Keep the two in step. This list only decides what Discord prints; the server awards ranks from
`pvp.yml` regardless of what is set here.

## Database Notes

Supported values:

```env
DB_TYPE=sqlite
DB_TYPE=mysql
DB_TYPE=mongodb
```

Default mode reads the plugin SQLite database:

```env
DB_TYPE=sqlite
SQLITE_FILE=../plugins/UltimateDonutSmp/data/data.db
```

For a production server, prefer an absolute path. If your plugin uses MySQL, set:

```env
DB_TYPE=mysql
MYSQL_HOST=localhost
MYSQL_PORT=3306
MYSQL_DATABASE=ultimatedonutsmp
MYSQL_USER=root
MYSQL_PASSWORD=your_password
```

If your plugin uses MongoDB, set:

```env
DB_TYPE=mongodb
MONGODB_URI=mongodb://localhost:27017
MONGODB_DATABASE=ultimatedonutsmp
MONGODB_PLAYERS_COLLECTION=players
```

The plugin stores MongoDB snapshots in collections named after SQL tables, so this bot reads the
`players`, `pvp_stats`, `pvp_matches` and `pvp_sync_codes` collections.

The survival commands read the plugin `players` table and the ranked commands read `pvp_stats` and
`pvp_matches`. Live in-memory player values may only appear after the plugin saves or autosaves them
to the database. The ranked tables are written as matches finish, so those are current.

A server that has never enabled the ranked arena has no `pvp_*` tables at all. The ranked commands
answer with "nobody has fought a ranked match yet" in that case rather than failing.

## Tests

```bash
npm test
```

The tests build a throwaway database with the plugin's real schema and run every query the bot makes
against it, so a column renamed on the plugin side fails here instead of in a live channel. There is
also a syntax check over every source file:

```bash
npm run check
```

## Discord.js References

This example follows the discord.js v14 slash command pattern with `REST`, `Routes`, `Client`,
`GatewayIntentBits`, and `SlashCommandBuilder`.
