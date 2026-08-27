# Cuboids & Portals System Guide

The **Cuboid** system in UltimateDonutSMP provides lightweight, high-performance 3D region selection and protection without needing WorldGuard. Cuboids are used for Spawn protection, AFK reward areas, Shard event zones, Random Teleport (RTP) boundaries, and Portal triggers.

---

## Cuboid Management (`/cuboid`)

### 1. Selection Wand
Get the selection tool (Golden Hoe or Wooden Axe):
```bash
/cuboid wand
```
- **Left-Click Block**: Sets Position 1 (Corner 1).
- **Right-Click Block**: Sets Position 2 (Corner 2).

### 2. Creating & Deleting Cuboids
After selecting two corners:
```bash
/cuboid create <name>
```
*Example*: `/cuboid create spawn_zone`

To remove a cuboid:
```bash
/cuboid delete <name>
```

To view all defined cuboids:
```bash
/cuboid list
```

### 3. Choosing Where Players Land

Anything that teleports a player into a cuboid — `/spawn`, a spawn or AFK menu area, the shard AFK
zone — normally aims for the middle of the region and looks downwards for the first block that is
safe to stand on. On a hand-built spawn that is rarely the spot you want, and players always arrive
facing the same direction.

Stand exactly where players should appear, look the way they should be facing, and save it:

```bash
/cuboid setspawn <name>
```

*Example*: `/cuboid setspawn spawn_zone`

The position has to be inside the cuboid, otherwise players would land outside the protection the
region gives them. Your facing is stored along with the coordinates.

To go back to the automatic middle-of-the-region spot:

```bash
/cuboid delspawn <name>
```

Saved points live under `CUBOID-SPAWNS` in `config.yml`, keyed by cuboid name:

```yaml
CUBOID-SPAWNS:
  spawn_zone: world,128.5,71.0,-64.5,90.0,0.0
```

Deleting a cuboid removes its spawn point too, and redefining one with the wand clears the old point
if the new corners no longer cover it.

If a spawn menu area has its own `LOCATION` in `menus.yml`, that still wins for that menu button —
the cuboid spawn point is the fallback everything else uses.

---

## Binding Cuboids to Systems (`/cuboid bind`)

Cuboids can be bound to different server systems to enforce special features or protections:

```bash
/cuboid bind <cuboid_name> <spawn|shard|rtp-zone|rtp-queue> <true|false>
```

### Feature Binds Explained:

1. **`spawn` Bind**:
   - Protects the cuboid area against block breaking, building, and unauthorized PvP.
   - Restricts player flight (`/fly`) or allows flight depending on settings.
   *Command*: `/cuboid bind spawn_zone spawn true`

2. **`shard` Bind**:
   - Defines a active Shard Cuboid zone where players holding position gain passive Shards over time.
   - Activates PlaceholderAPI placeholders `%economy_shard_cuboid_status%` and `%economy_shard_cuboid_display%`.
   *Command*: `/cuboid bind shard_arena shard true`

3. **`rtp-zone` Bind**:
   - Defines the exact region where `/rtp` (Random Teleport) will pick safe destination locations.
   *Command*: `/cuboid bind wilderness_bounds rtp-zone true`

4. **`rtp-queue` Bind**:
   - Puts everyone standing in the region on the RTP matchmaking queue without them typing
     `/rtpq`, and takes them off again when they walk out. Once enough of them are waiting the
     whole group is dropped at one shared random location, so a fight starts where they land.
   - Writes the region name to `QUEUE.CUBOID` in `rtp.yml`, where the match size, destination
     world and spread radius live.
   *Command*: `/cuboid bind rtp_pit rtp-queue true`

---

## Portal Management (`/portal`)

The Portal system allows administrators to turn any Cuboid region into a seamless teleport trigger.

### Portal Creation Syntax:
```bash
/portal create <portal_id> <cuboid_name> <destination_type> <destination_value>
```

### Supported Destination Types:
- **`SPAWN`**: Teleports player to global server spawn.
- **`WARP`**: Teleports player to a defined warp location (`<warp_name>`).
- **`RTP`**: Triggers a random teleport upon entering the portal.
- **`LOCATION`**: Teleports player to precise coordinates (`world,x,y,z,yaw,pitch`).

### Example Portal Commands:
```bash
# Create spawn portal
/portal create spawn_gate spawn_zone SPAWN

# Create nether warp portal
/portal create nether_gate nether_cuboid WARP nether_hub

# Create RTP portal trigger
/portal create rtp_gate rtp_trigger_cuboid RTP
```

### Portal Administration:
- List all active portals: `/portal list`
- Change portal cuboid: `/portal setcuboid <portal_id> <cuboid_name>`
- Change portal destination: `/portal setdestination <portal_id> <type> <value>`
- Delete portal: `/portal delete <portal_id>`
- Reload portal engine: `/portal reload`
