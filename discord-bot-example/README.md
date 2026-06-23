# UltimateDonutSmp Discord Bot Example

Example discord.js bot for `/stats` and `/leaderboard`. This replaces stats and leaderboard webhook delivery with real slash commands.

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

Slash commands are registered automatically every time the bot starts. You can still deploy manually without starting the bot:

```bash
npm run deploy
```

`DISCORD_CLIENT_ID` and `DISCORD_GUILD_ID` must be numeric Discord snowflake IDs. Do not leave placeholder values such as `your_test_server_id` in `.env`.

## Commands

```txt
/stats player:<minecraft username or uuid>
/leaderboard type:<money|shards|kills|deaths|playtime|...> limit:<1-10>
```

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

The plugin stores MongoDB snapshots in collections named after SQL tables, so this bot reads the `players` collection by default.

The bot reads the plugin `players` table. Live in-memory player values may only appear after the plugin saves/autosaves them to the database.

## Discord.js References

This example follows the discord.js v14 slash command pattern with `REST`, `Routes`, `Client`, `GatewayIntentBits`, and `SlashCommandBuilder`.
