Crates CLI additions

This document explains new /crate subcommand syntaxes added in the recent update.

Add non-item rewards via CLI
- Command reward:
  /crate add <crate> <slot> command <console command...>
  Example: /crate add common 10 command say {player} won a prize
  Writes a COMMAND-type reward to crates.yml at CRATES.<crate>.REWARDS.reward_<slot>

- Money reward:
  /crate add <crate> <slot> money <amount>
  Example: /crate add common 11 money 10.5
  Writes a MONEY-type reward with GRANT.AMOUNT to crates.yml

- Shards reward:
  /crate add <crate> <slot> shards <amount>
  Example: /crate add common 12 shards 100
  Writes a SHARDS-type reward with GRANT.AMOUNT to crates.yml

Notes
- The GUI editor now supports a lightweight template shorthand to create non-item rewards: give an item a display name starting with one of the tags below and place it into a crate slot in the editor.
  - [CMD] <console command...> — creates a COMMAND reward (e.g. an item named "[CMD] say {player} won").
  - [MONEY] <amount> — creates a MONEY reward (e.g. "[MONEY] 10.5").
  - [SHARDS] <amount> — creates a SHARDS reward (e.g. "[SHARDS] 100").
- The /crate type command now accepts both "choose_one" and "choose-one" (hyphen) for convenience.
- After adding rewards via the CLI or the template shorthand the plugin saves crates.yml and reloads crate data automatically.

Migration
- None required — new CLI writes crates.yml entries compatible with existing loader logic.

If you prefer GUI editing for non-item rewards, next step is to add editor affordances in CrateEditorMenu to create/edit MONEY/SHARDS/COMMAND grants.