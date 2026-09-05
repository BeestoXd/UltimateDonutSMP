# Economy & Marketplaces Guide

UltimateDonutSMP includes a complete dual-currency economy (Vault Money & Virtual Shards), GUI shops, sell menus, worth browsers, Auction House, Orders board, and Billford rotating trades NPC.

---

## Currency Systems

### 1. Money Economy (Vault Integration)
- **Check Balance**: `/balance [player]` (Aliases: `/bal`, `/money`)
- **Pay Player**: `/pay <player> <amount>`
- **Admin Money Management**:
  - `/addmoney <player> <amount>`
  - `/removemoney <player> <amount>`
  - `/setmoney <player> <amount>`

### 2. Virtual Shards Economy
Shards are an exclusive secondary currency earned from Shard Cuboids, play time, crates, or mob kills.
- **Check Shards**: `/shards [player]`
- **Pay Shards**: `/shardpay <player> <amount>`
- **Admin Shards Command**: `/shards everywhere <status|debug> [player]`

---

## Shop, Sell, & Worth Systems

### 1. Shop GUI (`/shop`)
Opens the configured multi-category item shop where players can buy blocks, items, spawners, and gear.

**Editing a shop in game (`/shopedit <menu>`)**: opens the chosen shop menu as an editor, the same way `/crate edit` works for crates. Click an item in your own inventory to pick it up as a template, then click a shop slot to put it there. Clicking a filled slot with nothing selected clears it, and every change is written to `shop.yml` straight away.

Items placed this way are stored whole, so enchantments, potion data, firework flight duration, custom names and any other item data survive into what the buyer receives. Set the price by renaming the item to `[PRICE] 250` before you place it; without that rename the item's `worth.yml` value is used, and an item with no worth entry is refused rather than listed for nothing. The slots the menu keeps for its own back and paging buttons are blocked off in the editor.

### 2. Sell Container & Commands (`/sell`)
- `/sell` (Aliases: `/sellmulti`, `/sellmultiplier`, `/sellprogress`): Opens a GUI chest container and Sell Multiplier progress menu. `menus.yml` ships `SELL-MENU.MODE` as `confirm`, so players drop items in and click Confirm Sell to get paid; closing the menu hands the items back. Switch MODE to `close` to sell the grid when the menu shuts, or `instant` to pay as items land.
- **Sell Category Item Preview**: Clicking the category header icon inside `/sellmulti` (or right-clicking category buttons in `/sell`) opens an item catalog GUI in `WorthMenu` showing only the items that fit in that category.
- **Barrier Back Navigation**: Navigation across sell and progress menus uses a `BARRIER` icon for returning to previous menus.
- `/sellhand [amount]`: Sells the item currently held in main hand.
- `/sellall`: Sells all sellable items in inventory.
- `/sellhistory`: Displays recent sell transactions.
- `/topsell`: Admin analytical command (`/topsell gui`, `/topsell export`, `/topsell items`).

### 3. Worth Catalog (`/worth`)
- `/worth`: Opens the full price catalog GUI displaying buy/sell values for every item.
- `/worth hand`: Checks the sell price of the item currently held in hand.

### 4. Farming Meta Rotation (`/meta`)
One item from a configured list is the farming meta at any time. It keeps its normal `worth.yml` price
as a base and sells for a multiple of it, so the item worth farming changes every few weeks without
anyone editing prices by hand.
- `/meta` (Alias: `/farmingmeta`): Shows the current meta item, its multiplier, what it sells for now
  compared to its usual price, and how long is left before the rotation moves on.
- The boost applies everywhere a price is shown or paid: `/worth`, the price catalog, the worth lore on
  items, `/sell` and the sell menus, and spawner auto-sell.
- Rotation is off until `META.ENABLED` is turned on in `worth.yml`, so updating the plugin never moves
  prices on its own.
- The countdown is stored in `farming-meta-data.yml`, so restarts do not reset it.

---

## Marketplaces

### 1. Auction House (`/auctionhouse` or `/ah`)
Player-driven marketplace where players can list items for sale to other players:
- `/ah`: Open Auction House browser menu.
- `/ah sell <price>`: List held item on the Auction House.
- `/ah my`: View active personal listings.
- `/ah claims`: Collect money earned from sold items or expired listings.
- `/ah cancel`: Cancel active listing.

### 2. Orders Board (`/orders`)
Player buy-order request system where players request items and set a reward price for anyone who fulfills the order:
- `/orders`: Open Orders Board GUI.
- `/orders my`: Manage personal buy orders.
- `/orders collect`: Collect items delivered by suppliers.

### 3. Billford Rotating NPC Trades (`/billford`)
Billford is a special rotating trade NPC system that offers limited-time, high-value trades refreshed on a scheduled timer.
- Open Billford menu: `/billford gui` or interact with Billford NPC.
- Check countdown placeholder: `%economy_billford_countdown%`
- Admin command: `/billford reload`

### 4. Automated Economy Bots (Auction & Order Bots)
Simulate player trading activity on the Auction House and Orders board with NPC bot listings and buy orders:
- **Auction Bot**: Posts random item listings under configured bot names (`auction-house.yml`).
- **Order Bot**: Posts item purchase requests under configured bot names (`orders.yml`).
- **Configuration**: Managed via the `BOTS:` section in `auction-house.yml` and `orders.yml`.

