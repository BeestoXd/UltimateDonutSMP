# Detailed Configuration & Setup Guide: `messages.yml`

This is the official, 100% complete technical setup guide for `messages.yml` in **UltimateDonutSMP**.
Each section details the exact commented setup code block, allowed option values, data types, default values, and in-depth functional behavior.

---

## Section: `TEAM`

### 1. Commented Setup Code Example

```yaml
TEAM:
  # The text or value for No Team. Available options: Any valid string text
  NO-TEAM: '&cYou don''t have a team. Type /team create (name) to create a team.'
  # The text or value for Already Exists. Available options: Any valid string text
  ALREADY-EXISTS: '&cYou cannot create a team with this name, it is already taken.'
  # The text or value for Team Created. Available options: Any valid string text
  TEAM-CREATED: '&aTeam created.'
  # The text or value for Team Disbanded. Available options: Any valid string text
  TEAM-DISBANDED: '&7Team disbanded.'
  # The text or value for Already In Team. Available options: Any valid string text
  ALREADY-IN-TEAM: '&cYou are already in a team.'
  # The text or value for Not Leader. Available options: Any valid string text
  NOT-LEADER: '&cYou are not the leader.'
  # The text or value for Player No Invites. Available options: Any valid string text
  PLAYER-NO-INVITES: '&cThis player does not accept invitations.'
  # The text or value for Player In Team. Available options: Any valid string text
  PLAYER-IN-TEAM: '&c{player} is already in a team.'
  # The text or value for Team Full. Available options: Any valid string text
  TEAM-FULL: '&cTeam has reached the maximum members.'
  # The text or value for Invite Sent. Available options: Any valid string text
  INVITE-SENT: '&eYou have invited &a{player} &eto join the team.'
  # The text or value for No Pending Invites. Available options: Any valid string text
  NO-PENDING-INVITES: '&cYou have no pending invites for &6{team}&c.'
  # The text or value for Join Success. Available options: Any valid string text
  JOIN-SUCCESS: '&aYou have joined to &e{team}&a.'
  # The text or value for Cant Kick Self. Available options: Any valid string text
  CANT-KICK-SELF: '&cYou cannot kick yourself!'
  # The text or value for Kick Success. Available options: Any valid string text
  KICK-SUCCESS: '&aSuccessfully kicked &7({player}).'
  # The text or value for Kicked From Team. Available options: Any valid string text
  KICKED-FROM-TEAM: '&aYou have been kicked from the team.'
  # The text or value for Player Not In Team. Available options: Any valid string text
  PLAYER-NOT-IN-TEAM: '&c{player} is not a member of your team.'
  # The text or value for Team Chat Enabled. Available options: Any valid string text
  TEAM-CHAT-ENABLED: '&7You enabled team chat.'
  # The text or value for Team Chat Disabled. Available options: Any valid string text
  TEAM-CHAT-DISABLED: '&7You disabled team chat.'
  # The text or value for No Manage Permission. Available options: Any valid string text
  NO-MANAGE-PERMISSION: '&cYou don''t have permission to invite or kick teammates.'
  # The text or value for No Edit Home Permission. Available options: Any valid string text
  NO-EDIT-HOME-PERMISSION: '&cYou don''t have permission to edit the team home.'
  # The text or value for No Visit Home Permission. Available options: Any valid string text
  NO-VISIT-HOME-PERMISSION: '&cYou don''t have permission to visit the team home.'
  # The text or value for No Team Chat Permission. Available options: Any valid string text
  NO-TEAM-CHAT-PERMISSION: '&cYou don''t have permission to use team chat.'
  # The text or value for No Pvp Permission. Available options: Any valid string text
  NO-PVP-PERMISSION: '&cYou don''t have permission to change team PvP.'
  # The text or value for Team Pvp Enabled. Available options: Any valid string text
  TEAM-PVP-ENABLED: '&7Team PvP is now &aenabled&7.'
  # The text or value for Team Pvp Disabled. Available options: Any valid string text
  TEAM-PVP-DISABLED: '&7Team PvP is now &cdisabled&7.'
  # The text or value for No Team Home. Available options: Any valid string text
  NO-TEAM-HOME: '&7Your team does not have a home.'
  # The text or value for Team Home Deleted. Available options: Any valid string text
  TEAM-HOME-DELETED: '&7Team home deleted.'
  # The text or value for Team Home Set. Available options: Any valid string text
  TEAM-HOME-SET: '&7Team home set'
  # The text or value for Team Not Exist. Available options: Any valid string text
  TEAM-NOT-EXIST: '&cUser/team does not exist.'
  # The text or value for Invited To Join. Available options: Any valid string text
  INVITED-TO-JOIN: '&7You have been invited to join the &a{team}&7 team!'
  # The text or value for Click To Join. Available options: Any valid string text
  CLICK-TO-JOIN: '&b[Click to join]'
  # The text or value for Hover Join. Available options: Any valid string text
  HOVER-JOIN: '&eClick to join the {team} team.'
  # The text or value for Or Type Command. Available options: Any valid string text
  OR-TYPE-COMMAND: '&7or type &f{command}&7 to join.'
  # The text or value for Joined Broadcast. Available options: Any valid string text
  JOINED-BROADCAST: '&a{player} &ehas joined the team.'
# Configuration section for Chat Manager.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `TEAM.NO-TEAM` | `str` | Any string text | `'&cYou don't have a team. Type /team...'` | Configures the technical `NO-TEAM` parameter for `TEAM.NO-TEAM` in `messages.yml`. |
| `TEAM.ALREADY-EXISTS` | `str` | Any string text | `'&cYou cannot create a team with thi...'` | Configures the technical `ALREADY-EXISTS` parameter for `TEAM.ALREADY-EXISTS` in `messages.yml`. |
| `TEAM.TEAM-CREATED` | `str` | Any string text | `'&aTeam created.'` | Configures the technical `TEAM-CREATED` parameter for `TEAM.TEAM-CREATED` in `messages.yml`. |
| `TEAM.TEAM-DISBANDED` | `str` | Any string text | `'&7Team disbanded.'` | Configures the technical `TEAM-DISBANDED` parameter for `TEAM.TEAM-DISBANDED` in `messages.yml`. |
| `TEAM.ALREADY-IN-TEAM` | `str` | Any string text | `'&cYou are already in a team.'` | Configures the technical `ALREADY-IN-TEAM` parameter for `TEAM.ALREADY-IN-TEAM` in `messages.yml`. |
| `TEAM.NOT-LEADER` | `str` | Any string text | `'&cYou are not the leader.'` | Configures the technical `NOT-LEADER` parameter for `TEAM.NOT-LEADER` in `messages.yml`. |
| `TEAM.PLAYER-NO-INVITES` | `str` | Any string text | `'&cThis player does not accept invit...'` | Configures the technical `PLAYER-NO-INVITES` parameter for `TEAM.PLAYER-NO-INVITES` in `messages.yml`. |
| `TEAM.PLAYER-IN-TEAM` | `str` | Any string text | `'&c{player} is already in a team.'` | Configures the technical `PLAYER-IN-TEAM` parameter for `TEAM.PLAYER-IN-TEAM` in `messages.yml`. |
| `TEAM.TEAM-FULL` | `str` | Any string text | `'&cTeam has reached the maximum memb...'` | Configures the technical `TEAM-FULL` parameter for `TEAM.TEAM-FULL` in `messages.yml`. |
| `TEAM.INVITE-SENT` | `str` | Any string text | `'&eYou have invited &a{player} &eto ...'` | Configures the technical `INVITE-SENT` parameter for `TEAM.INVITE-SENT` in `messages.yml`. |
| `TEAM.NO-PENDING-INVITES` | `str` | Any string text | `'&cYou have no pending invites for &...'` | Configures the technical `NO-PENDING-INVITES` parameter for `TEAM.NO-PENDING-INVITES` in `messages.yml`. |
| `TEAM.JOIN-SUCCESS` | `str` | Any string text | `'&aYou have joined to &e{team}&a.'` | Configures the technical `JOIN-SUCCESS` parameter for `TEAM.JOIN-SUCCESS` in `messages.yml`. |
| `TEAM.CANT-KICK-SELF` | `str` | Any string text | `'&cYou cannot kick yourself!'` | Configures the technical `CANT-KICK-SELF` parameter for `TEAM.CANT-KICK-SELF` in `messages.yml`. |
| `TEAM.KICK-SUCCESS` | `str` | Any string text | `'&aSuccessfully kicked &7({player}).'` | Configures the technical `KICK-SUCCESS` parameter for `TEAM.KICK-SUCCESS` in `messages.yml`. |
| `TEAM.KICKED-FROM-TEAM` | `str` | Any string text | `'&aYou have been kicked from the tea...'` | Configures the technical `KICKED-FROM-TEAM` parameter for `TEAM.KICKED-FROM-TEAM` in `messages.yml`. |
| `TEAM.PLAYER-NOT-IN-TEAM` | `str` | Any string text | `'&c{player} is not a member of your ...'` | Configures the technical `PLAYER-NOT-IN-TEAM` parameter for `TEAM.PLAYER-NOT-IN-TEAM` in `messages.yml`. |
| `TEAM.TEAM-CHAT-ENABLED` | `str` | Any string text | `'&7You enabled team chat.'` | Configures the technical `TEAM-CHAT-ENABLED` parameter for `TEAM.TEAM-CHAT-ENABLED` in `messages.yml`. |
| `TEAM.TEAM-CHAT-DISABLED` | `str` | Any string text | `'&7You disabled team chat.'` | Configures the technical `TEAM-CHAT-DISABLED` parameter for `TEAM.TEAM-CHAT-DISABLED` in `messages.yml`. |
| `TEAM.NO-MANAGE-PERMISSION` | `str` | Any string text | `'&cYou don't have permission to invi...'` | Configures the technical `NO-MANAGE-PERMISSION` parameter for `TEAM.NO-MANAGE-PERMISSION` in `messages.yml`. |
| `TEAM.NO-EDIT-HOME-PERMISSION` | `str` | Any string text | `'&cYou don't have permission to edit...'` | Configures the technical `NO-EDIT-HOME-PERMISSION` parameter for `TEAM.NO-EDIT-HOME-PERMISSION` in `messages.yml`. |
| `TEAM.NO-VISIT-HOME-PERMISSION` | `str` | Any string text | `'&cYou don't have permission to visi...'` | Configures the technical `NO-VISIT-HOME-PERMISSION` parameter for `TEAM.NO-VISIT-HOME-PERMISSION` in `messages.yml`. |
| `TEAM.NO-TEAM-CHAT-PERMISSION` | `str` | Any string text | `'&cYou don't have permission to use ...'` | Configures the technical `NO-TEAM-CHAT-PERMISSION` parameter for `TEAM.NO-TEAM-CHAT-PERMISSION` in `messages.yml`. |
| `TEAM.NO-PVP-PERMISSION` | `str` | Any string text | `'&cYou don't have permission to chan...'` | Configures the technical `NO-PVP-PERMISSION` parameter for `TEAM.NO-PVP-PERMISSION` in `messages.yml`. |
| `TEAM.TEAM-PVP-ENABLED` | `str` | Any string text | `'&7Team PvP is now &aenabled&7.'` | Configures the technical `TEAM-PVP-ENABLED` parameter for `TEAM.TEAM-PVP-ENABLED` in `messages.yml`. |
| `TEAM.TEAM-PVP-DISABLED` | `str` | Any string text | `'&7Team PvP is now &cdisabled&7.'` | Configures the technical `TEAM-PVP-DISABLED` parameter for `TEAM.TEAM-PVP-DISABLED` in `messages.yml`. |
| `TEAM.NO-TEAM-HOME` | `str` | Any string text | `'&7Your team does not have a home.'` | Configures the technical `NO-TEAM-HOME` parameter for `TEAM.NO-TEAM-HOME` in `messages.yml`. |
| `TEAM.TEAM-HOME-DELETED` | `str` | Any string text | `'&7Team home deleted.'` | Configures the technical `TEAM-HOME-DELETED` parameter for `TEAM.TEAM-HOME-DELETED` in `messages.yml`. |
| `TEAM.TEAM-HOME-SET` | `str` | Any string text | `'&7Team home set'` | Configures the technical `TEAM-HOME-SET` parameter for `TEAM.TEAM-HOME-SET` in `messages.yml`. |
| `TEAM.TEAM-NOT-EXIST` | `str` | Any string text | `'&cUser/team does not exist.'` | Configures the technical `TEAM-NOT-EXIST` parameter for `TEAM.TEAM-NOT-EXIST` in `messages.yml`. |
| `TEAM.INVITED-TO-JOIN` | `str` | Any string text | `'&7You have been invited to join the...'` | Configures the technical `INVITED-TO-JOIN` parameter for `TEAM.INVITED-TO-JOIN` in `messages.yml`. |
| *(4 additional sub-keys configured in section)* | | | | |

### 3. Practical Setup Example

```yaml
TEAM:
  # The text or value for No Team. Available options: Any valid string text
  NO-TEAM: '&cYou don''t have a team. Type /team create (name) to create a team.'
  # The text or value for Already Exists. Available options: Any valid string text
  ALREADY-EXISTS: '&cYou cannot create a team with this name, it is already taken.'
  # The text or value for Team Created. Available options: Any valid string text
  TEAM-CREATED: '&aTeam created.'
  # The text or value for Team Disbanded. Available options: Any valid string text
  TEAM-DISBANDED: '&7Team disbanded.'
  # The text or value for Already In Team. Available options: Any valid string text
  ALREADY-IN-TEAM: '&cYou are already in a team.'
  # The text or value for Not Leader. Available options: Any valid string text
  NOT-LEADER: '&cYou are not the leader.'
  # The text or value for Player No Invites. Available options: Any valid string text
  PLAYER-NO-INVITES: '&cThis player does not accept invitations.'
  # The text or value for 
```

---

## Section: `CHAT-MANAGER`

### 1. Commented Setup Code Example

```yaml
CHAT-MANAGER:
  # Configuration section for Help.
  HELP:
  - ''
  - '&b&lChat Manager &7(Commands)'
  - ''
  - '&f/chat mute &7- To mute global chat.'
  - '&f/chat unmute &7- To unmute global chat.'
  - '&f/chat delay (time) &7- To add delay to global chat.'
  - '&f/chat clear &7- To clear global chat.'
  - ''
  # The text or value for Muted. Available options: Any valid string text
  MUTED: '&aGlobal chat is now muted.'
  # The text or value for Unmuted. Available options: Any valid string text
  UNMUTED: '&aGlobal chat is now unmuted.'
  # The text or value for Delay. Available options: Any valid string text
  DELAY: '&7Chat is now delayed &a%delay% &7seconds and delay is &a%status%'
  # The text or value for Cleared. Available options: Any valid string text
  CLEARED: '&aGlobal chat is cleared.'
  # The text or value for No Permission. Available options: Any valid string text
  NO-PERMISSION: '&cYou do not have permission.'
  # The text or value for Disabled. Available options: Any valid string text
  DISABLED: '&cChat command is currently disabled.'
  # The text or value for Invalid Delay. Available options: Any valid string text
  INVALID-DELAY: '&cInvalid delay. Use a number between 0 and {max}.'
  # The text or value for Status Enabled. Available options: Any valid string text
  STATUS-ENABLED: enabled
  # The text or value for Status Disabled. Available options: Any valid string text
  STATUS-DISABLED: disabled
  # The text or value for Global Muted Block. Available options: Any valid string text
  GLOBAL-MUTED-BLOCK: '&cGlobal chat is currently muted.'
  # The text or value for Global Delay Block. Available options: Any valid string text
  GLOBAL-DELAY-BLOCK: '&cYou must wait &f{seconds}s &cbefore chatting again.'
# Configuration section for Ignore.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `CHAT-MANAGER.HELP` | `list` | List of configured items/strings | `[, &b&lChat Manager &7(Commands), ...]` | Configures the technical `HELP` parameter for `CHAT-MANAGER.HELP` in `messages.yml`. |
| `CHAT-MANAGER.MUTED` | `str` | Any string text | `'&aGlobal chat is now muted.'` | Configures the technical `MUTED` parameter for `CHAT-MANAGER.MUTED` in `messages.yml`. |
| `CHAT-MANAGER.UNMUTED` | `str` | Any string text | `'&aGlobal chat is now unmuted.'` | Configures the technical `UNMUTED` parameter for `CHAT-MANAGER.UNMUTED` in `messages.yml`. |
| `CHAT-MANAGER.DELAY` | `str` | Any string text | `'&7Chat is now delayed &a%delay% &7s...'` | Configures the technical `DELAY` parameter for `CHAT-MANAGER.DELAY` in `messages.yml`. |
| `CHAT-MANAGER.CLEARED` | `str` | Any string text | `'&aGlobal chat is cleared.'` | Configures the technical `CLEARED` parameter for `CHAT-MANAGER.CLEARED` in `messages.yml`. |
| `CHAT-MANAGER.NO-PERMISSION` | `str` | Any string text | `'&cYou do not have permission.'` | Configures the technical `NO-PERMISSION` parameter for `CHAT-MANAGER.NO-PERMISSION` in `messages.yml`. |
| `CHAT-MANAGER.DISABLED` | `str` | Any string text | `'&cChat command is currently disable...'` | Configures the technical `DISABLED` parameter for `CHAT-MANAGER.DISABLED` in `messages.yml`. |
| `CHAT-MANAGER.INVALID-DELAY` | `str` | Any string text | `'&cInvalid delay. Use a number betwe...'` | Configures the technical `INVALID-DELAY` parameter for `CHAT-MANAGER.INVALID-DELAY` in `messages.yml`. |
| `CHAT-MANAGER.STATUS-ENABLED` | `str` | Any string text | `'enabled'` | Configures the technical `STATUS-ENABLED` parameter for `CHAT-MANAGER.STATUS-ENABLED` in `messages.yml`. |
| `CHAT-MANAGER.STATUS-DISABLED` | `str` | Any string text | `'disabled'` | Configures the technical `STATUS-DISABLED` parameter for `CHAT-MANAGER.STATUS-DISABLED` in `messages.yml`. |
| `CHAT-MANAGER.GLOBAL-MUTED-BLOCK` | `str` | Any string text | `'&cGlobal chat is currently muted.'` | Configures the technical `GLOBAL-MUTED-BLOCK` parameter for `CHAT-MANAGER.GLOBAL-MUTED-BLOCK` in `messages.yml`. |
| `CHAT-MANAGER.GLOBAL-DELAY-BLOCK` | `str` | Any string text | `'&cYou must wait &f{seconds}s &cbefo...'` | Configures the technical `GLOBAL-DELAY-BLOCK` parameter for `CHAT-MANAGER.GLOBAL-DELAY-BLOCK` in `messages.yml`. |

### 3. Practical Setup Example

```yaml
CHAT-MANAGER:
  # Configuration section for Help.
  HELP:
  - ''
  - '&b&lChat Manager &7(Commands)'
  - ''
  - '&f/chat mute &7- To mute global chat.'
  - '&f/chat unmute &7- To unmute global chat.'
  - '&f/chat delay (time) &7- To add delay to global chat.'
  - '&f/chat clear &7- To clear global chat.'
  - ''
  # The text or value for Muted. Available options: Any valid string text
  MUTED: '&aGlobal chat is now muted.'
  # The text or value for Unmuted. Available options: Any valid string text
  UNMUTED: '&aGlobal chat is now unmuted.'
  # The text or value for Delay. Available options: Any valid string text
  DELAY: '&7Chat is now delayed &a%delay% &7seconds and delay is &a%status%'
  # The text or value for Cleared. Available options: Any valid string text
  CLEARED: '&aGlobal chat is cleared.'
  # The text or value for No Permission. Available options: Any valid string text
  NO-PERMISSION: '&cYou do not have permission.'
  # The text or value for Disabled. Available options: Any
```

---

## Section: `IGNORE`

### 1. Commented Setup Code Example

```yaml
IGNORE:
  # The text or value for Added. Available options: Any valid string text
  ADDED: '&7%player% &chas been added to your ignore list.'
  # The text or value for Removed. Available options: Any valid string text
  REMOVED: '&7%player% &chas been removed from your ignore list.'
  # The text or value for Usage. Available options: Any valid string text
  USAGE: '&cUsage: /ignore <player|list>'
  # The text or value for Unignore Usage. Available options: Any valid string text
  UNIGNORE-USAGE: '&cUsage: /unignore <player>'
  # The text or value for Player Only. Available options: Any valid string text
  PLAYER-ONLY: '&cOnly players can use this command.'
  # The text or value for No Permission. Available options: Any valid string text
  NO-PERMISSION: '&cYou do not have permission.'
  # The text or value for Disabled. Available options: Any valid string text
  DISABLED: '&cIgnore command is currently disabled.'
  # The text or value for Cannot Ignore Self. Available options: Any valid string text
  CANNOT-IGNORE-SELF: '&cYou cannot ignore yourself.'
  # The text or value for Player Not Found. Available options: Any valid string text
  PLAYER-NOT-FOUND: '&cPlayer not found.'
  # The text or value for Not Ignored. Available options: Any valid string text
  NOT-IGNORED: '&7%player% &cis not in your ignore list.'
  # The text or value for List Empty. Available options: Any valid string text
  LIST-EMPTY: '&7You are not ignoring anyone.'
  # The text or value for List Header. Available options: Any valid string text
  LIST-HEADER: '&8&m-------- &cIgnored Players &7(%count%) &8&m--------'
  # The text or value for List Entry. Available options: Any valid string text
  LIST-ENTRY: '&8- &7%player%'
  # The text or value for Message Blocked Sender. Available options: Any valid string text
  MESSAGE-BLOCKED-SENDER: '&cYou cannot message %player%.'
  # The text or value for Error. Available options: Any valid string text
  ERROR: '&cCould not update your ignore list.'
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `IGNORE.ADDED` | `str` | Any string text | `'&7%player% &chas been added to your...'` | Configures the technical `ADDED` parameter for `IGNORE.ADDED` in `messages.yml`. |
| `IGNORE.REMOVED` | `str` | Any string text | `'&7%player% &chas been removed from ...'` | Configures the technical `REMOVED` parameter for `IGNORE.REMOVED` in `messages.yml`. |
| `IGNORE.USAGE` | `str` | Any string text | `'&cUsage: /ignore <player|list>'` | Configures the technical `USAGE` parameter for `IGNORE.USAGE` in `messages.yml`. |
| `IGNORE.UNIGNORE-USAGE` | `str` | Any string text | `'&cUsage: /unignore <player>'` | Configures the technical `UNIGNORE-USAGE` parameter for `IGNORE.UNIGNORE-USAGE` in `messages.yml`. |
| `IGNORE.PLAYER-ONLY` | `str` | Any string text | `'&cOnly players can use this command...'` | Configures the technical `PLAYER-ONLY` parameter for `IGNORE.PLAYER-ONLY` in `messages.yml`. |
| `IGNORE.NO-PERMISSION` | `str` | Any string text | `'&cYou do not have permission.'` | Configures the technical `NO-PERMISSION` parameter for `IGNORE.NO-PERMISSION` in `messages.yml`. |
| `IGNORE.DISABLED` | `str` | Any string text | `'&cIgnore command is currently disab...'` | Configures the technical `DISABLED` parameter for `IGNORE.DISABLED` in `messages.yml`. |
| `IGNORE.CANNOT-IGNORE-SELF` | `str` | Any string text | `'&cYou cannot ignore yourself.'` | Configures the technical `CANNOT-IGNORE-SELF` parameter for `IGNORE.CANNOT-IGNORE-SELF` in `messages.yml`. |
| `IGNORE.PLAYER-NOT-FOUND` | `str` | Any string text | `'&cPlayer not found.'` | Configures the technical `PLAYER-NOT-FOUND` parameter for `IGNORE.PLAYER-NOT-FOUND` in `messages.yml`. |
| `IGNORE.NOT-IGNORED` | `str` | Any string text | `'&7%player% &cis not in your ignore ...'` | Configures the technical `NOT-IGNORED` parameter for `IGNORE.NOT-IGNORED` in `messages.yml`. |
| `IGNORE.LIST-EMPTY` | `str` | Any string text | `'&7You are not ignoring anyone.'` | Configures the technical `LIST-EMPTY` parameter for `IGNORE.LIST-EMPTY` in `messages.yml`. |
| `IGNORE.LIST-HEADER` | `str` | Any string text | `'&8&m-------- &cIgnored Players &7(%...'` | Configures the technical `LIST-HEADER` parameter for `IGNORE.LIST-HEADER` in `messages.yml`. |
| `IGNORE.LIST-ENTRY` | `str` | Any string text | `'&8- &7%player%'` | Configures the technical `LIST-ENTRY` parameter for `IGNORE.LIST-ENTRY` in `messages.yml`. |
| `IGNORE.MESSAGE-BLOCKED-SENDER` | `str` | Any string text | `'&cYou cannot message %player%.'` | Configures the technical `MESSAGE-BLOCKED-SENDER` parameter for `IGNORE.MESSAGE-BLOCKED-SENDER` in `messages.yml`. |
| `IGNORE.ERROR` | `str` | Any string text | `'&cCould not update your ignore list...'` | Configures the technical `ERROR` parameter for `IGNORE.ERROR` in `messages.yml`. |

### 3. Practical Setup Example

```yaml
IGNORE:
  # The text or value for Added. Available options: Any valid string text
  ADDED: '&7%player% &chas been added to your ignore list.'
  # The text or value for Removed. Available options: Any valid string text
  REMOVED: '&7%player% &chas been removed from your ignore list.'
  # The text or value for Usage. Available options: Any valid string text
  USAGE: '&cUsage: /ignore <player|list>'
  # The text or value for Unignore Usage. Available options: Any valid string text
  UNIGNORE-USAGE: '&cUsage: /unignore <player>'
  # The text or value for Player Only. Available options: Any valid string text
  PLAYER-ONLY: '&cOnly players can use this command.'
  # The text or value for No Permission. Available options: Any valid string text
  NO-PERMISSION: '&cYou do not have permission.'
  # The text or value for Disabled. Available options: Any valid string text
  DISABLED: '&cIgnore command is currently disabled.'
  # The text or value for Cannot Ignore Self. Available options: Any vali
```

---

## Section: `PRIVATE_MESSAGES`

### 1. Commented Setup Code Example

```yaml
PRIVATE_MESSAGES:
  # The text or value for Pm Enabled. Available options: Any valid string text
  PM_ENABLED: '&aPrivate messages are now enabled'
  # The text or value for Pm Disabled. Available options: Any valid string text
  PM_DISABLED: '&cPrivate messages are now disabled'
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `PRIVATE_MESSAGES.PM_ENABLED` | `str` | Any string text | `'&aPrivate messages are now enabled'` | Configures the technical `PM_ENABLED` parameter for `PRIVATE_MESSAGES.PM_ENABLED` in `messages.yml`. |
| `PRIVATE_MESSAGES.PM_DISABLED` | `str` | Any string text | `'&cPrivate messages are now disabled'` | Configures the technical `PM_DISABLED` parameter for `PRIVATE_MESSAGES.PM_DISABLED` in `messages.yml`. |

### 3. Practical Setup Example

```yaml
PRIVATE_MESSAGES:
  # The text or value for Pm Enabled. Available options: Any valid string text
  PM_ENABLED: '&aPrivate messages are now enabled'
  # The text or value for Pm Disabled. Available options: Any valid string text
  PM_DISABLED: '&cPrivate messages are now disabled'
```

---

## Section: `MESSAGES`

### 1. Commented Setup Code Example

```yaml
MESSAGES:
  # The text or value for Cannot Message Self. Available options: Any valid string text
  CANNOT_MESSAGE_SELF: '&cYou cannot message yourself!'
  # The text or value for Player Blocked. Available options: Any valid string text
  PLAYER_BLOCKED: '&c%player% has blocked you.'
  # The text or value for Pms Disabled. Available options: Any valid string text
  PMS_DISABLED: '&c%player% has private messages disabled.'
  # The text or value for No Conversation. Available options: Any valid string text
  NO_CONVERSATION: '&cYou are currently not in conversation with anyone or the player
    is offline.'
  # The text or value for Sender Format. Available options: Any valid string text
  SENDER_FORMAT: '&d(To &a%player%&d) %message%'
  # The text or value for Receiver Format. Available options: Any valid string text
  RECEIVER_FORMAT: '&d(From &a%player%&d) %message%'
  # The text or value for Usage. Available options: Any valid string text
  USAGE: '&cUsage: /msg <player> <message>'
  # The text or value for Reply Usage. Available options: Any valid string text
  REPLY_USAGE: '&cUsage: /reply <message>'
  # The text or value for Player Not Online. Available options: Any valid string text
  PLAYER_NOT_ONLINE: '&cPlayer not online.'
  # The text or value for Player Only Reply. Available options: Any valid string text
  PLAYER_ONLY_REPLY: '&cOnly players can use /reply.'
  # The text or value for Player Only. Available options: Any valid string text
  PLAYER_ONLY: '&cOnly players can use this command.'
  # The text or value for No Permission. Available options: Any valid string text
  NO_PERMISSION: '&cYou do not have permission.'
  # The text or value for Disabled. Available options: Any valid string text
  DISABLED: '&cPrivate messages are currently disabled.'
# Configuration section for Private Message.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `MESSAGES.CANNOT_MESSAGE_SELF` | `str` | Any string text | `'&cYou cannot message yourself!'` | Configures the technical `CANNOT_MESSAGE_SELF` parameter for `MESSAGES.CANNOT_MESSAGE_SELF` in `messages.yml`. |
| `MESSAGES.PLAYER_BLOCKED` | `str` | Any string text | `'&c%player% has blocked you.'` | Configures the technical `PLAYER_BLOCKED` parameter for `MESSAGES.PLAYER_BLOCKED` in `messages.yml`. |
| `MESSAGES.PMS_DISABLED` | `str` | Any string text | `'&c%player% has private messages dis...'` | Configures the technical `PMS_DISABLED` parameter for `MESSAGES.PMS_DISABLED` in `messages.yml`. |
| `MESSAGES.NO_CONVERSATION` | `str` | Any string text | `'&cYou are currently not in conversa...'` | Configures the technical `NO_CONVERSATION` parameter for `MESSAGES.NO_CONVERSATION` in `messages.yml`. |
| `MESSAGES.SENDER_FORMAT` | `str` | Any string text | `'&d(To &a%player%&d) %message%'` | Configures the technical `SENDER_FORMAT` parameter for `MESSAGES.SENDER_FORMAT` in `messages.yml`. |
| `MESSAGES.RECEIVER_FORMAT` | `str` | Any string text | `'&d(From &a%player%&d) %message%'` | Configures the technical `RECEIVER_FORMAT` parameter for `MESSAGES.RECEIVER_FORMAT` in `messages.yml`. |
| `MESSAGES.USAGE` | `str` | Any string text | `'&cUsage: /msg <player> <message>'` | Configures the technical `USAGE` parameter for `MESSAGES.USAGE` in `messages.yml`. |
| `MESSAGES.REPLY_USAGE` | `str` | Any string text | `'&cUsage: /reply <message>'` | Configures the technical `REPLY_USAGE` parameter for `MESSAGES.REPLY_USAGE` in `messages.yml`. |
| `MESSAGES.PLAYER_NOT_ONLINE` | `str` | Any string text | `'&cPlayer not online.'` | Configures the technical `PLAYER_NOT_ONLINE` parameter for `MESSAGES.PLAYER_NOT_ONLINE` in `messages.yml`. |
| `MESSAGES.PLAYER_ONLY_REPLY` | `str` | Any string text | `'&cOnly players can use /reply.'` | Configures the technical `PLAYER_ONLY_REPLY` parameter for `MESSAGES.PLAYER_ONLY_REPLY` in `messages.yml`. |
| `MESSAGES.PLAYER_ONLY` | `str` | Any string text | `'&cOnly players can use this command...'` | Configures the technical `PLAYER_ONLY` parameter for `MESSAGES.PLAYER_ONLY` in `messages.yml`. |
| `MESSAGES.NO_PERMISSION` | `str` | Any string text | `'&cYou do not have permission.'` | Configures the technical `NO_PERMISSION` parameter for `MESSAGES.NO_PERMISSION` in `messages.yml`. |
| `MESSAGES.DISABLED` | `str` | Any string text | `'&cPrivate messages are currently di...'` | Configures the technical `DISABLED` parameter for `MESSAGES.DISABLED` in `messages.yml`. |

### 3. Practical Setup Example

```yaml
MESSAGES:
  # The text or value for Cannot Message Self. Available options: Any valid string text
  CANNOT_MESSAGE_SELF: '&cYou cannot message yourself!'
  # The text or value for Player Blocked. Available options: Any valid string text
  PLAYER_BLOCKED: '&c%player% has blocked you.'
  # The text or value for Pms Disabled. Available options: Any valid string text
  PMS_DISABLED: '&c%player% has private messages disabled.'
  # The text or value for No Conversation. Available options: Any valid string text
  NO_CONVERSATION: '&cYou are currently not in conversation with anyone or the player
    is offline.'
  # The text or value for Sender Format. Available options: Any valid string text
  SENDER_FORMAT: '&d(To &a%player%&d) %message%'
  # The text or value for Receiver Format. Available options: Any valid string text
  RECEIVER_FORMAT: '&d(From &a%player%&d) %message%'
  # The text or value for Usage. Available options: Any valid string text
  USAGE: '&cUsage: /msg <player> <message>'
 
```

---

## Section: `PRIVATE-MESSAGE`

### 1. Commented Setup Code Example

```yaml
PRIVATE-MESSAGE:
  # The text or value for Usage. Available options: Any valid string text
  USAGE: '&cUsage: /msg <player> <message>'
  # The text or value for Reply Usage. Available options: Any valid string text
  REPLY-USAGE: '&cUsage: /reply <message>'
  # The text or value for Player Only Reply. Available options: Any valid string text
  PLAYER-ONLY-REPLY: '&cOnly players can use /reply.'
  # The text or value for No Permission. Available options: Any valid string text
  NO-PERMISSION: '&cYou do not have permission.'
  # The text or value for Disabled. Available options: Any valid string text
  DISABLED: '&cPrivate messages are currently disabled.'
  # The text or value for Player Not Online. Available options: Any valid string text
  PLAYER-NOT-ONLINE: '&cPlayer not online.'
  # The text or value for Cannot Message Self. Available options: Any valid string text
  CANNOT-MESSAGE-SELF: '&cYou cannot message yourself!'
  # The text or value for No Reply Target. Available options: Any valid string text
  NO-REPLY-TARGET: '&cYou are currently not in conversation with anyone or the player
    is offline.'
  # The text or value for Sent. Available options: Any valid string text
  SENT: '&d(To &a%player%&d) %message%'
  # The text or value for Received. Available options: Any valid string text
  RECEIVED: '&d(From &a%player%&d) %message%'
  # The text or value for Pm Enabled. Available options: Any valid string text
  PM-ENABLED: '&aPrivate messages are now enabled'
  # The text or value for Pm Disabled. Available options: Any valid string text
  PM-DISABLED: '&cPrivate messages are now disabled'
# Configuration section for Tpauto.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `PRIVATE-MESSAGE.USAGE` | `str` | Any string text | `'&cUsage: /msg <player> <message>'` | Configures the technical `USAGE` parameter for `PRIVATE-MESSAGE.USAGE` in `messages.yml`. |
| `PRIVATE-MESSAGE.REPLY-USAGE` | `str` | Any string text | `'&cUsage: /reply <message>'` | Configures the technical `REPLY-USAGE` parameter for `PRIVATE-MESSAGE.REPLY-USAGE` in `messages.yml`. |
| `PRIVATE-MESSAGE.PLAYER-ONLY-REPLY` | `str` | Any string text | `'&cOnly players can use /reply.'` | Configures the technical `PLAYER-ONLY-REPLY` parameter for `PRIVATE-MESSAGE.PLAYER-ONLY-REPLY` in `messages.yml`. |
| `PRIVATE-MESSAGE.NO-PERMISSION` | `str` | Any string text | `'&cYou do not have permission.'` | Configures the technical `NO-PERMISSION` parameter for `PRIVATE-MESSAGE.NO-PERMISSION` in `messages.yml`. |
| `PRIVATE-MESSAGE.DISABLED` | `str` | Any string text | `'&cPrivate messages are currently di...'` | Configures the technical `DISABLED` parameter for `PRIVATE-MESSAGE.DISABLED` in `messages.yml`. |
| `PRIVATE-MESSAGE.PLAYER-NOT-ONLINE` | `str` | Any string text | `'&cPlayer not online.'` | Configures the technical `PLAYER-NOT-ONLINE` parameter for `PRIVATE-MESSAGE.PLAYER-NOT-ONLINE` in `messages.yml`. |
| `PRIVATE-MESSAGE.CANNOT-MESSAGE-SELF` | `str` | Any string text | `'&cYou cannot message yourself!'` | Configures the technical `CANNOT-MESSAGE-SELF` parameter for `PRIVATE-MESSAGE.CANNOT-MESSAGE-SELF` in `messages.yml`. |
| `PRIVATE-MESSAGE.NO-REPLY-TARGET` | `str` | Any string text | `'&cYou are currently not in conversa...'` | Configures the technical `NO-REPLY-TARGET` parameter for `PRIVATE-MESSAGE.NO-REPLY-TARGET` in `messages.yml`. |
| `PRIVATE-MESSAGE.SENT` | `str` | Any string text | `'&d(To &a%player%&d) %message%'` | Configures the technical `SENT` parameter for `PRIVATE-MESSAGE.SENT` in `messages.yml`. |
| `PRIVATE-MESSAGE.RECEIVED` | `str` | Any string text | `'&d(From &a%player%&d) %message%'` | Configures the technical `RECEIVED` parameter for `PRIVATE-MESSAGE.RECEIVED` in `messages.yml`. |
| `PRIVATE-MESSAGE.PM-ENABLED` | `str` | Any string text | `'&aPrivate messages are now enabled'` | Configures the technical `PM-ENABLED` parameter for `PRIVATE-MESSAGE.PM-ENABLED` in `messages.yml`. |
| `PRIVATE-MESSAGE.PM-DISABLED` | `str` | Any string text | `'&cPrivate messages are now disabled'` | Configures the technical `PM-DISABLED` parameter for `PRIVATE-MESSAGE.PM-DISABLED` in `messages.yml`. |

### 3. Practical Setup Example

```yaml
PRIVATE-MESSAGE:
  # The text or value for Usage. Available options: Any valid string text
  USAGE: '&cUsage: /msg <player> <message>'
  # The text or value for Reply Usage. Available options: Any valid string text
  REPLY-USAGE: '&cUsage: /reply <message>'
  # The text or value for Player Only Reply. Available options: Any valid string text
  PLAYER-ONLY-REPLY: '&cOnly players can use /reply.'
  # The text or value for No Permission. Available options: Any valid string text
  NO-PERMISSION: '&cYou do not have permission.'
  # The text or value for Disabled. Available options: Any valid string text
  DISABLED: '&cPrivate messages are currently disabled.'
  # The text or value for Player Not Online. Available options: Any valid string text
  PLAYER-NOT-ONLINE: '&cPlayer not online.'
  # The text or value for Cannot Message Self. Available options: Any valid string text
  CANNOT-MESSAGE-SELF: '&cYou cannot message yourself!'
  # The text or value for No Reply Target. Available options: A
```

---

## Section: `TPAUTO`

### 1. Commented Setup Code Example

```yaml
TPAUTO:
  # The text or value for Enabled. Available options: Any valid string text
  ENABLED: '&7You turned on tpauto.'
  # The text or value for Disabled. Available options: Any valid string text
  DISABLED: '&7You turned off tpauto.'
# Configuration section for Tpahereauto.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `TPAUTO.ENABLED` | `str` | Any string text | `'&7You turned on tpauto.'` | Global toggle for `TPAUTO` system. Set to `true` to enable, `false` to disable. |
| `TPAUTO.DISABLED` | `str` | Any string text | `'&7You turned off tpauto.'` | Configures the technical `DISABLED` parameter for `TPAUTO.DISABLED` in `messages.yml`. |

### 3. Practical Setup Example

```yaml
TPAUTO:
  # The text or value for Enabled. Available options: Any valid string text
  ENABLED: '&7You turned on tpauto.'
  # The text or value for Disabled. Available options: Any valid string text
  DISABLED: '&7You turned off tpauto.'
# Configuration section for Tpahereauto.
```

---

## Section: `TPAHEREAUTO`

### 1. Commented Setup Code Example

```yaml
TPAHEREAUTO:
  # The text or value for Enabled. Available options: Any valid string text
  ENABLED: '&7You turned on tpahere auto-accept.'
  # The text or value for Disabled. Available options: Any valid string text
  DISABLED: '&7You turned off tpahere auto-accept.'
# Configuration section for Phantom.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `TPAHEREAUTO.ENABLED` | `str` | Any string text | `'&7You turned on tpahere auto-accept...'` | Global toggle for `TPAHEREAUTO` system. Set to `true` to enable, `false` to disable. |
| `TPAHEREAUTO.DISABLED` | `str` | Any string text | `'&7You turned off tpahere auto-accep...'` | Configures the technical `DISABLED` parameter for `TPAHEREAUTO.DISABLED` in `messages.yml`. |

### 3. Practical Setup Example

```yaml
TPAHEREAUTO:
  # The text or value for Enabled. Available options: Any valid string text
  ENABLED: '&7You turned on tpahere auto-accept.'
  # The text or value for Disabled. Available options: Any valid string text
  DISABLED: '&7You turned off tpahere auto-accept.'
# Configuration section for Phantom.
```

---

## Section: `PHANTOM`

### 1. Commented Setup Code Example

```yaml
PHANTOM:
  # The text or value for Enabled. Available options: Any valid string text
  ENABLED: '&7You disabled phantoms spawning close to you'
  # The text or value for Disabled. Available options: Any valid string text
  DISABLED: '&7You enabled phantoms spawning close to you'
# Configuration section for Clear Lag.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `PHANTOM.ENABLED` | `str` | Any string text | `'&7You disabled phantoms spawning cl...'` | Global toggle for `PHANTOM` system. Set to `true` to enable, `false` to disable. |
| `PHANTOM.DISABLED` | `str` | Any string text | `'&7You enabled phantoms spawning clo...'` | Configures the technical `DISABLED` parameter for `PHANTOM.DISABLED` in `messages.yml`. |

### 3. Practical Setup Example

```yaml
PHANTOM:
  # The text or value for Enabled. Available options: Any valid string text
  ENABLED: '&7You disabled phantoms spawning close to you'
  # The text or value for Disabled. Available options: Any valid string text
  DISABLED: '&7You enabled phantoms spawning close to you'
# Configuration section for Clear Lag.
```

---

## Section: `CLEAR-LAG`

### 1. Commented Setup Code Example

```yaml
CLEAR-LAG:
  # The text or value for Countdown. Available options: Any valid string text
  COUNTDOWN: '&7Entities will be removed in &b{seconds} &7seconds.'
  # The text or value for Success. Available options: Any valid string text
  SUCCESS: '&7Total of &b{total} &7entities have been cleared.'
# Configuration section for Teleport.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `CLEAR-LAG.COUNTDOWN` | `str` | Any string text | `'&7Entities will be removed in &b{se...'` | Configures the technical `COUNTDOWN` parameter for `CLEAR-LAG.COUNTDOWN` in `messages.yml`. |
| `CLEAR-LAG.SUCCESS` | `str` | Any string text | `'&7Total of &b{total} &7entities hav...'` | Configures the technical `SUCCESS` parameter for `CLEAR-LAG.SUCCESS` in `messages.yml`. |

### 3. Practical Setup Example

```yaml
CLEAR-LAG:
  # The text or value for Countdown. Available options: Any valid string text
  COUNTDOWN: '&7Entities will be removed in &b{seconds} &7seconds.'
  # The text or value for Success. Available options: Any valid string text
  SUCCESS: '&7Total of &b{total} &7entities have been cleared.'
# Configuration section for Teleport.
```

---

## Section: `TELEPORT`

### 1. Commented Setup Code Example

```yaml
TELEPORT:
  # The text or value for Success. Available options: Any valid string text
  SUCCESS: '&7You have been teleported successfully'
  # The text or value for Countdown. Available options: Any valid string text
  COUNTDOWN: '&7Teleporting in &b{seconds}&7 seconds'
  # The text or value for Warning. Available options: Any valid string text
  WARNING: '&eDo not move for &b{seconds}&e seconds. If you move, the teleport will
    be canceled.'
  # The text or value for Canceled. Available options: Any valid string text
  CANCELED: '&cTeleport canceled because you moved.'
  # The text or value for To Player. Available options: Any valid string text
  TO_PLAYER: '&dTeleported &7to %player%'
  # The text or value for Here. Available options: Any valid string text
  HERE: '&dTeleported &7%player% to your location'
  # The text or value for Here Target. Available options: Any valid string text
  HERE_TARGET: '&dYou were teleported to &7%sender%'
  # The text or value for All. Available options: Any valid string text
  ALL: '&dTeleported &7all players to your location'
  # The text or value for All Target. Available options: Any valid string text
  ALL_TARGET: '&dYou were teleported to &7%sender%'
  # The text or value for Position. Available options: Any valid string text
  POSITION: '&7Teleported to: &d%x%,%y%,%z% &7(%world%)'
  # The text or value for Top. Available options: Any valid string text
  TOP: '&dTeleported &7to the highest position'
# Configuration section for Shard Booster.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `TELEPORT.SUCCESS` | `str` | Any string text | `'&7You have been teleported successf...'` | Configures the technical `SUCCESS` parameter for `TELEPORT.SUCCESS` in `messages.yml`. |
| `TELEPORT.COUNTDOWN` | `str` | Any string text | `'&7Teleporting in &b{seconds}&7 seco...'` | Configures the technical `COUNTDOWN` parameter for `TELEPORT.COUNTDOWN` in `messages.yml`. |
| `TELEPORT.WARNING` | `str` | Any string text | `'&eDo not move for &b{seconds}&e sec...'` | Configures the technical `WARNING` parameter for `TELEPORT.WARNING` in `messages.yml`. |
| `TELEPORT.CANCELED` | `str` | Any string text | `'&cTeleport canceled because you mov...'` | Configures the technical `CANCELED` parameter for `TELEPORT.CANCELED` in `messages.yml`. |
| `TELEPORT.TO_PLAYER` | `str` | Any string text | `'&dTeleported &7to %player%'` | Configures the technical `TO_PLAYER` parameter for `TELEPORT.TO_PLAYER` in `messages.yml`. |
| `TELEPORT.HERE` | `str` | Any string text | `'&dTeleported &7%player% to your loc...'` | Configures the technical `HERE` parameter for `TELEPORT.HERE` in `messages.yml`. |
| `TELEPORT.HERE_TARGET` | `str` | Any string text | `'&dYou were teleported to &7%sender%'` | Configures the technical `HERE_TARGET` parameter for `TELEPORT.HERE_TARGET` in `messages.yml`. |
| `TELEPORT.ALL` | `str` | Any string text | `'&dTeleported &7all players to your ...'` | Configures the technical `ALL` parameter for `TELEPORT.ALL` in `messages.yml`. |
| `TELEPORT.ALL_TARGET` | `str` | Any string text | `'&dYou were teleported to &7%sender%'` | Configures the technical `ALL_TARGET` parameter for `TELEPORT.ALL_TARGET` in `messages.yml`. |
| `TELEPORT.POSITION` | `str` | Any string text | `'&7Teleported to: &d%x%,%y%,%z% &7(%...'` | Configures the technical `POSITION` parameter for `TELEPORT.POSITION` in `messages.yml`. |
| `TELEPORT.TOP` | `str` | Any string text | `'&dTeleported &7to the highest posit...'` | Configures the technical `TOP` parameter for `TELEPORT.TOP` in `messages.yml`. |

### 3. Practical Setup Example

```yaml
TELEPORT:
  # The text or value for Success. Available options: Any valid string text
  SUCCESS: '&7You have been teleported successfully'
  # The text or value for Countdown. Available options: Any valid string text
  COUNTDOWN: '&7Teleporting in &b{seconds}&7 seconds'
  # The text or value for Warning. Available options: Any valid string text
  WARNING: '&eDo not move for &b{seconds}&e seconds. If you move, the teleport will
    be canceled.'
  # The text or value for Canceled. Available options: Any valid string text
  CANCELED: '&cTeleport canceled because you moved.'
  # The text or value for To Player. Available options: Any valid string text
  TO_PLAYER: '&dTeleported &7to %player%'
  # The text or value for Here. Available options: Any valid string text
  HERE: '&dTeleported &7%player% to your location'
  # The text or value for Here Target. Available options: Any valid string text
  HERE_TARGET: '&dYou were teleported to &7%sender%'
  # The text or value for All. Available opt
```

---

## Section: `SHARD-BOOSTER`

### 1. Commented Setup Code Example

```yaml
SHARD-BOOSTER:
  # The text or value for Activated. Available options: Any valid string text
  ACTIVATED: '&aYou have activated your &5Shard Booster &afor 24h.'
  # The text or value for Already Activated. Available options: Any valid string text
  ALREADY-ACTIVATED: '&cYou already have an active Shard Booster.'
  # The text or value for Expired. Available options: Any valid string text
  EXPIRED: '&cYour shard booster has expired.'
# Configuration section for Tpa.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `SHARD-BOOSTER.ACTIVATED` | `str` | Any string text | `'&aYou have activated your &5Shard B...'` | Configures the technical `ACTIVATED` parameter for `SHARD-BOOSTER.ACTIVATED` in `messages.yml`. |
| `SHARD-BOOSTER.ALREADY-ACTIVATED` | `str` | Any string text | `'&cYou already have an active Shard ...'` | Configures the technical `ALREADY-ACTIVATED` parameter for `SHARD-BOOSTER.ALREADY-ACTIVATED` in `messages.yml`. |
| `SHARD-BOOSTER.EXPIRED` | `str` | Any string text | `'&cYour shard booster has expired.'` | Configures the technical `EXPIRED` parameter for `SHARD-BOOSTER.EXPIRED` in `messages.yml`. |

### 3. Practical Setup Example

```yaml
SHARD-BOOSTER:
  # The text or value for Activated. Available options: Any valid string text
  ACTIVATED: '&aYou have activated your &5Shard Booster &afor 24h.'
  # The text or value for Already Activated. Available options: Any valid string text
  ALREADY-ACTIVATED: '&cYou already have an active Shard Booster.'
  # The text or value for Expired. Available options: Any valid string text
  EXPIRED: '&cYour shard booster has expired.'
# Configuration section for Tpa.
```

---

## Section: `TPA`

### 1. Commented Setup Code Example

```yaml
TPA:
  # The text or value for Invite Sent. Available options: Any valid string text
  INVITE-SENT: '&eyou have invited &a{player} &eto teleport.'
  # The text or value for Invite Here Sent. Available options: Any valid string text
  INVITE-HERE-SENT: '&eyou have invited &a{player} &eto teleport to you.'
  # The text or value for Already Sent. Available options: Any valid string text
  ALREADY-SENT: '&cYou have already sent the request to &6{player}.'
  # The text or value for Request Received. Available options: Any valid string text
  REQUEST-RECEIVED: '&e[&d&ltpa request&e] &eyou have a request from &a&l{player}&e.
    &a&l(click to accept)'
  # The text or value for Request Here Received. Available options: Any valid string text
  REQUEST-HERE-RECEIVED: '&e[&d&ltpahere request&e] &eyou have a request from &a&l{player}&e
    to teleport to them. &a&l(click to accept)'
  # The text or value for No Request. Available options: Any valid string text
  NO-REQUEST: '&cyou have no tpa request from &a{player}.'
  # The text or value for No Sent Requests. Available options: Any valid string text
  NO-SENT-REQUESTS: '&cThis teleport request doest not exist.'
  # The text or value for Cancelled Requests. Available options: Any valid string text
  CANCELLED-REQUESTS: '&7You canceled your tpa requests.'
  # The text or value for No Request Here. Available options: Any valid string text
  NO-REQUEST-HERE: '&cyou have no tpahere request from &a{player}.'
  # The text or value for Accepted. Available options: Any valid string text
  ACCEPTED: '&ayou have accepted the tpa request from &a{player}.'
  # The text or value for Accepted Here. Available options: Any valid string text
  ACCEPTED-HERE: '&ayou have accepted the tpahere request from &a{player}.'
  # The text or value for Your Request Accepted. Available options: Any valid string text
  YOUR-REQUEST-ACCEPTED: '&a{player} has accepted your tpa request.'
  # The text or value for Your Request Here Accepted. Available options: Any valid string text
  YOUR-REQUEST-HERE-ACCEPTED: '&a{player} has accepted your tpahere request.'
  # The text or value for Cannot Invite Yourself. Available options: Any valid string text
  CANNOT-INVITE-YOURSELF: '&cYou cannot invite yourself!'
  # The text or value for Not Accepting Requests. Available options: Any valid string text
  NOT-ACCEPTING-REQUESTS: '&cThis player is not accepting requests.'
# Configuration section for Home.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `TPA.INVITE-SENT` | `str` | Any string text | `'&eyou have invited &a{player} &eto ...'` | Configures the technical `INVITE-SENT` parameter for `TPA.INVITE-SENT` in `messages.yml`. |
| `TPA.INVITE-HERE-SENT` | `str` | Any string text | `'&eyou have invited &a{player} &eto ...'` | Configures the technical `INVITE-HERE-SENT` parameter for `TPA.INVITE-HERE-SENT` in `messages.yml`. |
| `TPA.ALREADY-SENT` | `str` | Any string text | `'&cYou have already sent the request...'` | Configures the technical `ALREADY-SENT` parameter for `TPA.ALREADY-SENT` in `messages.yml`. |
| `TPA.REQUEST-RECEIVED` | `str` | Any string text | `'&e[&d&ltpa request&e] &eyou have a ...'` | Configures the technical `REQUEST-RECEIVED` parameter for `TPA.REQUEST-RECEIVED` in `messages.yml`. |
| `TPA.REQUEST-HERE-RECEIVED` | `str` | Any string text | `'&e[&d&ltpahere request&e] &eyou hav...'` | Configures the technical `REQUEST-HERE-RECEIVED` parameter for `TPA.REQUEST-HERE-RECEIVED` in `messages.yml`. |
| `TPA.NO-REQUEST` | `str` | Any string text | `'&cyou have no tpa request from &a{p...'` | Configures the technical `NO-REQUEST` parameter for `TPA.NO-REQUEST` in `messages.yml`. |
| `TPA.NO-SENT-REQUESTS` | `str` | Any string text | `'&cThis teleport request doest not e...'` | Configures the technical `NO-SENT-REQUESTS` parameter for `TPA.NO-SENT-REQUESTS` in `messages.yml`. |
| `TPA.CANCELLED-REQUESTS` | `str` | Any string text | `'&7You canceled your tpa requests.'` | Configures the technical `CANCELLED-REQUESTS` parameter for `TPA.CANCELLED-REQUESTS` in `messages.yml`. |
| `TPA.NO-REQUEST-HERE` | `str` | Any string text | `'&cyou have no tpahere request from ...'` | Configures the technical `NO-REQUEST-HERE` parameter for `TPA.NO-REQUEST-HERE` in `messages.yml`. |
| `TPA.ACCEPTED` | `str` | Any string text | `'&ayou have accepted the tpa request...'` | Configures the technical `ACCEPTED` parameter for `TPA.ACCEPTED` in `messages.yml`. |
| `TPA.ACCEPTED-HERE` | `str` | Any string text | `'&ayou have accepted the tpahere req...'` | Configures the technical `ACCEPTED-HERE` parameter for `TPA.ACCEPTED-HERE` in `messages.yml`. |
| `TPA.YOUR-REQUEST-ACCEPTED` | `str` | Any string text | `'&a{player} has accepted your tpa re...'` | Configures the technical `YOUR-REQUEST-ACCEPTED` parameter for `TPA.YOUR-REQUEST-ACCEPTED` in `messages.yml`. |
| `TPA.YOUR-REQUEST-HERE-ACCEPTED` | `str` | Any string text | `'&a{player} has accepted your tpaher...'` | Configures the technical `YOUR-REQUEST-HERE-ACCEPTED` parameter for `TPA.YOUR-REQUEST-HERE-ACCEPTED` in `messages.yml`. |
| `TPA.CANNOT-INVITE-YOURSELF` | `str` | Any string text | `'&cYou cannot invite yourself!'` | Configures the technical `CANNOT-INVITE-YOURSELF` parameter for `TPA.CANNOT-INVITE-YOURSELF` in `messages.yml`. |
| `TPA.NOT-ACCEPTING-REQUESTS` | `str` | Any string text | `'&cThis player is not accepting requ...'` | Configures the technical `NOT-ACCEPTING-REQUESTS` parameter for `TPA.NOT-ACCEPTING-REQUESTS` in `messages.yml`. |

### 3. Practical Setup Example

```yaml
TPA:
  # The text or value for Invite Sent. Available options: Any valid string text
  INVITE-SENT: '&eyou have invited &a{player} &eto teleport.'
  # The text or value for Invite Here Sent. Available options: Any valid string text
  INVITE-HERE-SENT: '&eyou have invited &a{player} &eto teleport to you.'
  # The text or value for Already Sent. Available options: Any valid string text
  ALREADY-SENT: '&cYou have already sent the request to &6{player}.'
  # The text or value for Request Received. Available options: Any valid string text
  REQUEST-RECEIVED: '&e[&d&ltpa request&e] &eyou have a request from &a&l{player}&e.
    &a&l(click to accept)'
  # The text or value for Request Here Received. Available options: Any valid string text
  REQUEST-HERE-RECEIVED: '&e[&d&ltpahere request&e] &eyou have a request from &a&l{player}&e
    to teleport to them. &a&l(click to accept)'
  # The text or value for No Request. Available options: Any valid string text
  NO-REQUEST: '&cyou have no tpa requ
```

---

## Section: `HOME`

### 1. Commented Setup Code Example

```yaml
HOME:
  # The text or value for Name Prompt. Available options: Any valid string text
  NAME-PROMPT: '&7Type the home name in chat for &b{name}&7. Type &ccancel &7to abort.'
  # The text or value for Set. Available options: Any valid string text
  SET: '&7Home set'
  # The text or value for Deleted. Available options: Any valid string text
  DELETED: '&7Home deleted'
  # The text or value for Rename Prompt. Available options: Any valid string text
  RENAME-PROMPT: '&7Type the new name for &b{name}&7 in chat. Type &ccancel &7to abort.'
  # The text or value for Rename Success. Available options: Any valid string text
  RENAME-SUCCESS: '&7You rename your home to &b{name}'
  # The text or value for Invalid Name. Available options: Any valid string text
  INVALID-NAME: '&cInvalid home name. Do not use spaces.'
  # The text or value for Already Exists. Available options: Any valid string text
  ALREADY-EXISTS: '&cA home with that name already exists.'
  # The text or value for Cancelled. Available options: Any valid string text
  CANCELLED: '&7Home input cancelled.'
# Configuration section for Warp.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `HOME.NAME-PROMPT` | `str` | Any string text | `'&7Type the home name in chat for &b...'` | Configures the technical `NAME-PROMPT` parameter for `HOME.NAME-PROMPT` in `messages.yml`. |
| `HOME.SET` | `str` | Any string text | `'&7Home set'` | Configures the technical `SET` parameter for `HOME.SET` in `messages.yml`. |
| `HOME.DELETED` | `str` | Any string text | `'&7Home deleted'` | Configures the technical `DELETED` parameter for `HOME.DELETED` in `messages.yml`. |
| `HOME.RENAME-PROMPT` | `str` | Any string text | `'&7Type the new name for &b{name}&7 ...'` | Configures the technical `RENAME-PROMPT` parameter for `HOME.RENAME-PROMPT` in `messages.yml`. |
| `HOME.RENAME-SUCCESS` | `str` | Any string text | `'&7You rename your home to &b{name}'` | Configures the technical `RENAME-SUCCESS` parameter for `HOME.RENAME-SUCCESS` in `messages.yml`. |
| `HOME.INVALID-NAME` | `str` | Any string text | `'&cInvalid home name. Do not use spa...'` | Configures the technical `INVALID-NAME` parameter for `HOME.INVALID-NAME` in `messages.yml`. |
| `HOME.ALREADY-EXISTS` | `str` | Any string text | `'&cA home with that name already exi...'` | Configures the technical `ALREADY-EXISTS` parameter for `HOME.ALREADY-EXISTS` in `messages.yml`. |
| `HOME.CANCELLED` | `str` | Any string text | `'&7Home input cancelled.'` | Configures the technical `CANCELLED` parameter for `HOME.CANCELLED` in `messages.yml`. |

### 3. Practical Setup Example

```yaml
HOME:
  # The text or value for Name Prompt. Available options: Any valid string text
  NAME-PROMPT: '&7Type the home name in chat for &b{name}&7. Type &ccancel &7to abort.'
  # The text or value for Set. Available options: Any valid string text
  SET: '&7Home set'
  # The text or value for Deleted. Available options: Any valid string text
  DELETED: '&7Home deleted'
  # The text or value for Rename Prompt. Available options: Any valid string text
  RENAME-PROMPT: '&7Type the new name for &b{name}&7 in chat. Type &ccancel &7to abort.'
  # The text or value for Rename Success. Available options: Any valid string text
  RENAME-SUCCESS: '&7You rename your home to &b{name}'
  # The text or value for Invalid Name. Available options: Any valid string text
  INVALID-NAME: '&cInvalid home name. Do not use spaces.'
  # The text or value for Already Exists. Available options: Any valid string text
  ALREADY-EXISTS: '&cA home with that name already exists.'
  # The text or value for Cancelled. Av
```

---

## Section: `WARP`

### 1. Commented Setup Code Example

```yaml
WARP:
  # The text or value for Player Only. Available options: Any valid string text
  PLAYER-ONLY: '&cOnly players can use this warp command.'
  # The text or value for Usage. Available options: Any valid string text
  USAGE: '&cUsage: /warp [name]'
  # The text or value for List Header. Available options: Any valid string text
  LIST-HEADER: '&8&m---------------- &bWarps &7({count}) &8&m----------------'
  # The text or value for List Entry. Available options: Any valid string text
  LIST-ENTRY: '&7- &b{name}'
  # The text or value for List Empty. Available options: Any valid string text
  LIST-EMPTY: '&cNo warps available.'
  # The text or value for Not Found. Available options: Any valid string text
  NOT-FOUND: '&cWarp ''&e{name}&c'' not found.'
  # The text or value for Not Found Suggestion. Available options: Any valid string text
  NOT-FOUND-SUGGESTION: '&7Did you mean: &b{suggestions}'
  # The text or value for Created. Available options: Any valid string text
  CREATED: '&aWarp &b{name} &ahas been created.'
  # The text or value for Deleted. Available options: Any valid string text
  DELETED: '&aWarp &b{name} &ahas been deleted.'
  # The text or value for Already Exists. Available options: Any valid string text
  ALREADY-EXISTS: '&cWarp ''&e{name}&c'' already exists.'
  # The text or value for Invalid Name. Available options: Any valid string text
  INVALID-NAME: '&cInvalid warp name. Use only letters, numbers, dashes, and underscores.'
# Configuration section for Warpmanager.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `WARP.PLAYER-ONLY` | `str` | Any string text | `'&cOnly players can use this warp co...'` | Configures the technical `PLAYER-ONLY` parameter for `WARP.PLAYER-ONLY` in `messages.yml`. |
| `WARP.USAGE` | `str` | Any string text | `'&cUsage: /warp [name]'` | Configures the technical `USAGE` parameter for `WARP.USAGE` in `messages.yml`. |
| `WARP.LIST-HEADER` | `str` | Any string text | `'&8&m---------------- &bWarps &7({co...'` | Configures the technical `LIST-HEADER` parameter for `WARP.LIST-HEADER` in `messages.yml`. |
| `WARP.LIST-ENTRY` | `str` | Any string text | `'&7- &b{name}'` | Configures the technical `LIST-ENTRY` parameter for `WARP.LIST-ENTRY` in `messages.yml`. |
| `WARP.LIST-EMPTY` | `str` | Any string text | `'&cNo warps available.'` | Configures the technical `LIST-EMPTY` parameter for `WARP.LIST-EMPTY` in `messages.yml`. |
| `WARP.NOT-FOUND` | `str` | Any string text | `'&cWarp '&e{name}&c' not found.'` | Configures the technical `NOT-FOUND` parameter for `WARP.NOT-FOUND` in `messages.yml`. |
| `WARP.NOT-FOUND-SUGGESTION` | `str` | Any string text | `'&7Did you mean: &b{suggestions}'` | Configures the technical `NOT-FOUND-SUGGESTION` parameter for `WARP.NOT-FOUND-SUGGESTION` in `messages.yml`. |
| `WARP.CREATED` | `str` | Any string text | `'&aWarp &b{name} &ahas been created.'` | Configures the technical `CREATED` parameter for `WARP.CREATED` in `messages.yml`. |
| `WARP.DELETED` | `str` | Any string text | `'&aWarp &b{name} &ahas been deleted.'` | Configures the technical `DELETED` parameter for `WARP.DELETED` in `messages.yml`. |
| `WARP.ALREADY-EXISTS` | `str` | Any string text | `'&cWarp '&e{name}&c' already exists.'` | Configures the technical `ALREADY-EXISTS` parameter for `WARP.ALREADY-EXISTS` in `messages.yml`. |
| `WARP.INVALID-NAME` | `str` | Any string text | `'&cInvalid warp name. Use only lette...'` | Configures the technical `INVALID-NAME` parameter for `WARP.INVALID-NAME` in `messages.yml`. |

### 3. Practical Setup Example

```yaml
WARP:
  # The text or value for Player Only. Available options: Any valid string text
  PLAYER-ONLY: '&cOnly players can use this warp command.'
  # The text or value for Usage. Available options: Any valid string text
  USAGE: '&cUsage: /warp [name]'
  # The text or value for List Header. Available options: Any valid string text
  LIST-HEADER: '&8&m---------------- &bWarps &7({count}) &8&m----------------'
  # The text or value for List Entry. Available options: Any valid string text
  LIST-ENTRY: '&7- &b{name}'
  # The text or value for List Empty. Available options: Any valid string text
  LIST-EMPTY: '&cNo warps available.'
  # The text or value for Not Found. Available options: Any valid string text
  NOT-FOUND: '&cWarp ''&e{name}&c'' not found.'
  # The text or value for Not Found Suggestion. Available options: Any valid string text
  NOT-FOUND-SUGGESTION: '&7Did you mean: &b{suggestions}'
  # The text or value for Created. Available options: Any valid string text
  CREATED: '&aW
```

---

## Section: `WARPMANAGER`

### 1. Commented Setup Code Example

```yaml
WARPMANAGER:
  # The text or value for Usage. Available options: Any valid string text
  USAGE: '&cUsage: /warpmanager <create|delete|list> [name]'
  # The text or value for Create Usage. Available options: Any valid string text
  CREATE-USAGE: '&cUsage: /warpmanager create <name>'
  # The text or value for Delete Usage. Available options: Any valid string text
  DELETE-USAGE: '&cUsage: /warpmanager delete <name>'
  # The text or value for Create Usage Alias. Available options: Any valid string text
  CREATE-USAGE-ALIAS: '&cUsage: /setwarp <name>'
  # The text or value for Delete Usage Alias. Available options: Any valid string text
  DELETE-USAGE-ALIAS: '&cUsage: /delwarp <name>'
  # The text or value for No Permission. Available options: Any valid string text
  NO-PERMISSION: '&cYou do not have permission to manage warps.'
  # The text or value for Create Player Only. Available options: Any valid string text
  CREATE-PLAYER-ONLY: '&cOnly players can create warps.'
  # The text or value for Create Failed. Available options: Any valid string text
  CREATE-FAILED: '&cFailed to create warp ''&e{name}&c''.'
# Configuration section for Portal.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `WARPMANAGER.USAGE` | `str` | Any string text | `'&cUsage: /warpmanager <create|delet...'` | Configures the technical `USAGE` parameter for `WARPMANAGER.USAGE` in `messages.yml`. |
| `WARPMANAGER.CREATE-USAGE` | `str` | Any string text | `'&cUsage: /warpmanager create <name>'` | Configures the technical `CREATE-USAGE` parameter for `WARPMANAGER.CREATE-USAGE` in `messages.yml`. |
| `WARPMANAGER.DELETE-USAGE` | `str` | Any string text | `'&cUsage: /warpmanager delete <name>'` | Configures the technical `DELETE-USAGE` parameter for `WARPMANAGER.DELETE-USAGE` in `messages.yml`. |
| `WARPMANAGER.CREATE-USAGE-ALIAS` | `str` | Any string text | `'&cUsage: /setwarp <name>'` | Configures the technical `CREATE-USAGE-ALIAS` parameter for `WARPMANAGER.CREATE-USAGE-ALIAS` in `messages.yml`. |
| `WARPMANAGER.DELETE-USAGE-ALIAS` | `str` | Any string text | `'&cUsage: /delwarp <name>'` | Configures the technical `DELETE-USAGE-ALIAS` parameter for `WARPMANAGER.DELETE-USAGE-ALIAS` in `messages.yml`. |
| `WARPMANAGER.NO-PERMISSION` | `str` | Any string text | `'&cYou do not have permission to man...'` | Configures the technical `NO-PERMISSION` parameter for `WARPMANAGER.NO-PERMISSION` in `messages.yml`. |
| `WARPMANAGER.CREATE-PLAYER-ONLY` | `str` | Any string text | `'&cOnly players can create warps.'` | Configures the technical `CREATE-PLAYER-ONLY` parameter for `WARPMANAGER.CREATE-PLAYER-ONLY` in `messages.yml`. |
| `WARPMANAGER.CREATE-FAILED` | `str` | Any string text | `'&cFailed to create warp '&e{name}&c...'` | Configures the technical `CREATE-FAILED` parameter for `WARPMANAGER.CREATE-FAILED` in `messages.yml`. |

### 3. Practical Setup Example

```yaml
WARPMANAGER:
  # The text or value for Usage. Available options: Any valid string text
  USAGE: '&cUsage: /warpmanager <create|delete|list> [name]'
  # The text or value for Create Usage. Available options: Any valid string text
  CREATE-USAGE: '&cUsage: /warpmanager create <name>'
  # The text or value for Delete Usage. Available options: Any valid string text
  DELETE-USAGE: '&cUsage: /warpmanager delete <name>'
  # The text or value for Create Usage Alias. Available options: Any valid string text
  CREATE-USAGE-ALIAS: '&cUsage: /setwarp <name>'
  # The text or value for Delete Usage Alias. Available options: Any valid string text
  DELETE-USAGE-ALIAS: '&cUsage: /delwarp <name>'
  # The text or value for No Permission. Available options: Any valid string text
  NO-PERMISSION: '&cYou do not have permission to manage warps.'
  # The text or value for Create Player Only. Available options: Any valid string text
  CREATE-PLAYER-ONLY: '&cOnly players can create warps.'
  # The text or val
```

---

## Section: `PORTAL`

### 1. Commented Setup Code Example

```yaml
PORTAL:
  # The text or value for List Empty. Available options: Any valid string text
  LIST-EMPTY: '&cNo portals have been configured yet.'
  # The text or value for Entered. Available options: Any valid string text
  ENTERED: '&dEntering {portal}&7...'
  # The text or value for Invalid Cuboid. Available options: Any valid string text
  INVALID-CUBOID: '&cThis portal is not configured correctly right now.'
  # The text or value for Invalid Destination. Available options: Any valid string text
  INVALID-DESTINATION: '&cThis portal destination is currently unavailable.'
  # The text or value for No Permission. Available options: Any valid string text
  NO-PERMISSION: '&cYou do not have permission to use this portal.'
  # The text or value for In Combat. Available options: Any valid string text
  IN-COMBAT: '&cYou cannot use portals while in combat.'
  # The text or value for Teleport In Progress. Available options: Any valid string text
  TELEPORT-IN-PROGRESS: '&cYou are already teleporting.'
  # The text or value for Status Ready. Available options: Any valid string text
  STATUS-READY: '&aready'
  # The text or value for Status Disabled. Available options: Any valid string text
  STATUS-DISABLED: '&cdisabled'
  # The text or value for Status Invalid Cuboid. Available options: Any valid string text
  STATUS-INVALID-CUBOID: '&einvalid cuboid'
  # The text or value for Status Invalid Destination. Available options: Any valid string text
  STATUS-INVALID-DESTINATION: '&einvalid destination'
# Configuration section for Portalmanager.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `PORTAL.LIST-EMPTY` | `str` | Any string text | `'&cNo portals have been configured y...'` | Configures the technical `LIST-EMPTY` parameter for `PORTAL.LIST-EMPTY` in `messages.yml`. |
| `PORTAL.ENTERED` | `str` | Any string text | `'&dEntering {portal}&7...'` | Configures the technical `ENTERED` parameter for `PORTAL.ENTERED` in `messages.yml`. |
| `PORTAL.INVALID-CUBOID` | `str` | Any string text | `'&cThis portal is not configured cor...'` | Configures the technical `INVALID-CUBOID` parameter for `PORTAL.INVALID-CUBOID` in `messages.yml`. |
| `PORTAL.INVALID-DESTINATION` | `str` | Any string text | `'&cThis portal destination is curren...'` | Configures the technical `INVALID-DESTINATION` parameter for `PORTAL.INVALID-DESTINATION` in `messages.yml`. |
| `PORTAL.NO-PERMISSION` | `str` | Any string text | `'&cYou do not have permission to use...'` | Configures the technical `NO-PERMISSION` parameter for `PORTAL.NO-PERMISSION` in `messages.yml`. |
| `PORTAL.IN-COMBAT` | `str` | Any string text | `'&cYou cannot use portals while in c...'` | Configures the technical `IN-COMBAT` parameter for `PORTAL.IN-COMBAT` in `messages.yml`. |
| `PORTAL.TELEPORT-IN-PROGRESS` | `str` | Any string text | `'&cYou are already teleporting.'` | Configures the technical `TELEPORT-IN-PROGRESS` parameter for `PORTAL.TELEPORT-IN-PROGRESS` in `messages.yml`. |
| `PORTAL.STATUS-READY` | `str` | Any string text | `'&aready'` | Configures the technical `STATUS-READY` parameter for `PORTAL.STATUS-READY` in `messages.yml`. |
| `PORTAL.STATUS-DISABLED` | `str` | Any string text | `'&cdisabled'` | Configures the technical `STATUS-DISABLED` parameter for `PORTAL.STATUS-DISABLED` in `messages.yml`. |
| `PORTAL.STATUS-INVALID-CUBOID` | `str` | Any string text | `'&einvalid cuboid'` | Configures the technical `STATUS-INVALID-CUBOID` parameter for `PORTAL.STATUS-INVALID-CUBOID` in `messages.yml`. |
| `PORTAL.STATUS-INVALID-DESTINATION` | `str` | Any string text | `'&einvalid destination'` | Configures the technical `STATUS-INVALID-DESTINATION` parameter for `PORTAL.STATUS-INVALID-DESTINATION` in `messages.yml`. |

### 3. Practical Setup Example

```yaml
PORTAL:
  # The text or value for List Empty. Available options: Any valid string text
  LIST-EMPTY: '&cNo portals have been configured yet.'
  # The text or value for Entered. Available options: Any valid string text
  ENTERED: '&dEntering {portal}&7...'
  # The text or value for Invalid Cuboid. Available options: Any valid string text
  INVALID-CUBOID: '&cThis portal is not configured correctly right now.'
  # The text or value for Invalid Destination. Available options: Any valid string text
  INVALID-DESTINATION: '&cThis portal destination is currently unavailable.'
  # The text or value for No Permission. Available options: Any valid string text
  NO-PERMISSION: '&cYou do not have permission to use this portal.'
  # The text or value for In Combat. Available options: Any valid string text
  IN-COMBAT: '&cYou cannot use portals while in combat.'
  # The text or value for Teleport In Progress. Available options: Any valid string text
  TELEPORT-IN-PROGRESS: '&cYou are already telepo
```

---

## Section: `PORTALMANAGER`

### 1. Commented Setup Code Example

```yaml
PORTALMANAGER:
  # The text or value for Usage. Available options: Any valid string text
  USAGE: '&cUsage: /portalmanager <list|info|create|delete|setcuboid|setdestination|setdisplay|toggle|setpriority|sethologramhere>'
  # The text or value for No Permission. Available options: Any valid string text
  NO-PERMISSION: '&cYou do not have permission to manage portals.'
  # The text or value for Player Only. Available options: Any valid string text
  PLAYER-ONLY: '&cOnly players can use this command.'
  # The text or value for Create Usage. Available options: Any valid string text
  CREATE-USAGE: '&cUsage: /portalmanager create <id> <cuboid> <rtp_selector>'
  # The text or value for Delete Usage. Available options: Any valid string text
  DELETE-USAGE: '&cUsage: /portalmanager delete <id>'
  # The text or value for Info Usage. Available options: Any valid string text
  INFO-USAGE: '&cUsage: /portalmanager info <id>'
  # The text or value for Setcuboid Usage. Available options: Any valid string text
  SETCUBOID-USAGE: '&cUsage: /portalmanager setcuboid <id> <cuboid>'
  # The text or value for Setdestination Usage. Available options: Any valid string text
  SETDESTINATION-USAGE: '&cUsage: /portalmanager setdestination <id> <rtp_selector>'
  # The text or value for Setdisplay Usage. Available options: Any valid string text
  SETDISPLAY-USAGE: '&cUsage: /portalmanager setdisplay <id> <display name...>'
  # The text or value for Toggle Usage. Available options: Any valid string text
  TOGGLE-USAGE: '&cUsage: /portalmanager toggle <id>'
  # The text or value for Setpriority Usage. Available options: Any valid string text
  SETPRIORITY-USAGE: '&cUsage: /portalmanager setpriority <id> <number>'
  # The text or value for Sethologramhere Usage. Available options: Any valid string text
  SETHOLOGRAMHERE-USAGE: '&cUsage: /portalmanager sethologramhere <id>'
  # The text or value for Invalid Id. Available options: Any valid string text
  INVALID-ID: '&cInvalid portal id. Use only letters, numbers, dashes, and underscores.'
  # The text or value for Invalid Cuboid. Available options: Any valid string text
  INVALID-CUBOID: '&cCuboid ''&e{cuboid}&c'' does not exist.'
  # The text or value for Invalid Destination. Available options: Any valid string text
  INVALID-DESTINATION: '&cRTP destination ''&e{destination}&c'' is unavailable.'
  # The text or value for Invalid Priority. Available options: Any valid string text
  INVALID-PRIORITY: '&cPriority must be a whole number.'
  # The text or value for Not Found. Available options: Any valid string text
  NOT-FOUND: '&cPortal ''&e{id}&c'' not found.'
  # The text or value for Already Exists. Available options: Any valid string text
  ALREADY-EXISTS: '&cPortal ''&e{id}&c'' already exists.'
  # The text or value for Created. Available options: Any valid string text
  CREATED: '&aPortal &d{id} &ahas been created.'
  # The text or value for Updated. Available options: Any valid string text
  UPDATED: '&aPortal &d{id} &ahas been updated.'
  # The text or value for Hologram Updated. Available options: Any valid string text
  HOLOGRAM-UPDATED: '&aPortal &d{id} &ahologram has been moved to your location.'
  # The text or value for Deleted. Available options: Any valid string text
  DELETED: '&aPortal &d{id} &ahas been deleted.'
  # The text or value for Toggled. Available options: Any valid string text
  TOGGLED: '&aPortal &d{id} &ais now &f{state}&a.'
  # The text or value for List Header. Available options: Any valid string text
  LIST-HEADER: '&8&m---------------- &dPortals &7({count}) &8&m----------------'
  # The text or value for List Entry. Available options: Any valid string text
  LIST-ENTRY: '&7- &d{id} &8[&f{state}&8] &7cuboid=&f{cuboid} &7destination=&f{destination}'
  # The text or value for Info Header. Available options: Any valid string text
  INFO-HEADER: '&8&m---------------- &dPortal: &f{id} &8&m----------------'
  # The text or value for Info Display. Available options: Any valid string text
  INFO-DISPLAY: '&7Display: &f{display}'
  # The text or value for Info State. Available options: Any valid string text
  INFO-STATE: '&7State: &f{state}'
  # The text or value for Info Cuboid. Available options: Any valid string text
  INFO-CUBOID: '&7Cuboid: &f{cuboid}'
  # The text or value for Info Destination. Available options: Any valid string text
  INFO-DESTINATION: '&7Destination: &f{destination}'
  # The text or value for Info World. Available options: Any valid string text
  INFO-WORLD: '&7Resolved World: &f{world}'
  # The text or value for Info Priority. Available options: Any valid string text
  INFO-PRIORITY: '&7Priority: &f{priority}'
  # The text or value for Info Cooldown. Available options: Any valid string text
  INFO-COOLDOWN: '&7Trigger Cooldown: &f{cooldown}ms'
  # The text or value for Info Permission. Available options: Any valid string text
  INFO-PERMISSION: '&7Permission: &f{permission}'
  # The text or value for Info Hologram. Available options: Any valid string text
  INFO-HOLOGRAM: '&7Hologram: &f{hologram}'
# Configuration section for Worth.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `PORTALMANAGER.USAGE` | `str` | Any string text | `'&cUsage: /portalmanager <list|info|...'` | Configures the technical `USAGE` parameter for `PORTALMANAGER.USAGE` in `messages.yml`. |
| `PORTALMANAGER.NO-PERMISSION` | `str` | Any string text | `'&cYou do not have permission to man...'` | Configures the technical `NO-PERMISSION` parameter for `PORTALMANAGER.NO-PERMISSION` in `messages.yml`. |
| `PORTALMANAGER.PLAYER-ONLY` | `str` | Any string text | `'&cOnly players can use this command...'` | Configures the technical `PLAYER-ONLY` parameter for `PORTALMANAGER.PLAYER-ONLY` in `messages.yml`. |
| `PORTALMANAGER.CREATE-USAGE` | `str` | Any string text | `'&cUsage: /portalmanager create <id>...'` | Configures the technical `CREATE-USAGE` parameter for `PORTALMANAGER.CREATE-USAGE` in `messages.yml`. |
| `PORTALMANAGER.DELETE-USAGE` | `str` | Any string text | `'&cUsage: /portalmanager delete <id>'` | Configures the technical `DELETE-USAGE` parameter for `PORTALMANAGER.DELETE-USAGE` in `messages.yml`. |
| `PORTALMANAGER.INFO-USAGE` | `str` | Any string text | `'&cUsage: /portalmanager info <id>'` | Configures the technical `INFO-USAGE` parameter for `PORTALMANAGER.INFO-USAGE` in `messages.yml`. |
| `PORTALMANAGER.SETCUBOID-USAGE` | `str` | Any string text | `'&cUsage: /portalmanager setcuboid <...'` | Configures the technical `SETCUBOID-USAGE` parameter for `PORTALMANAGER.SETCUBOID-USAGE` in `messages.yml`. |
| `PORTALMANAGER.SETDESTINATION-USAGE` | `str` | Any string text | `'&cUsage: /portalmanager setdestinat...'` | Configures the technical `SETDESTINATION-USAGE` parameter for `PORTALMANAGER.SETDESTINATION-USAGE` in `messages.yml`. |
| `PORTALMANAGER.SETDISPLAY-USAGE` | `str` | Any string text | `'&cUsage: /portalmanager setdisplay ...'` | Configures the technical `SETDISPLAY-USAGE` parameter for `PORTALMANAGER.SETDISPLAY-USAGE` in `messages.yml`. |
| `PORTALMANAGER.TOGGLE-USAGE` | `str` | Any string text | `'&cUsage: /portalmanager toggle <id>'` | Configures the technical `TOGGLE-USAGE` parameter for `PORTALMANAGER.TOGGLE-USAGE` in `messages.yml`. |
| `PORTALMANAGER.SETPRIORITY-USAGE` | `str` | Any string text | `'&cUsage: /portalmanager setpriority...'` | Configures the technical `SETPRIORITY-USAGE` parameter for `PORTALMANAGER.SETPRIORITY-USAGE` in `messages.yml`. |
| `PORTALMANAGER.SETHOLOGRAMHERE-USAGE` | `str` | Any string text | `'&cUsage: /portalmanager sethologram...'` | Configures the technical `SETHOLOGRAMHERE-USAGE` parameter for `PORTALMANAGER.SETHOLOGRAMHERE-USAGE` in `messages.yml`. |
| `PORTALMANAGER.INVALID-ID` | `str` | Any string text | `'&cInvalid portal id. Use only lette...'` | Configures the technical `INVALID-ID` parameter for `PORTALMANAGER.INVALID-ID` in `messages.yml`. |
| `PORTALMANAGER.INVALID-CUBOID` | `str` | Any string text | `'&cCuboid '&e{cuboid}&c' does not ex...'` | Configures the technical `INVALID-CUBOID` parameter for `PORTALMANAGER.INVALID-CUBOID` in `messages.yml`. |
| `PORTALMANAGER.INVALID-DESTINATION` | `str` | Any string text | `'&cRTP destination '&e{destination}&...'` | Configures the technical `INVALID-DESTINATION` parameter for `PORTALMANAGER.INVALID-DESTINATION` in `messages.yml`. |
| `PORTALMANAGER.INVALID-PRIORITY` | `str` | Any string text | `'&cPriority must be a whole number.'` | Configures the technical `INVALID-PRIORITY` parameter for `PORTALMANAGER.INVALID-PRIORITY` in `messages.yml`. |
| `PORTALMANAGER.NOT-FOUND` | `str` | Any string text | `'&cPortal '&e{id}&c' not found.'` | Configures the technical `NOT-FOUND` parameter for `PORTALMANAGER.NOT-FOUND` in `messages.yml`. |
| `PORTALMANAGER.ALREADY-EXISTS` | `str` | Any string text | `'&cPortal '&e{id}&c' already exists.'` | Configures the technical `ALREADY-EXISTS` parameter for `PORTALMANAGER.ALREADY-EXISTS` in `messages.yml`. |
| `PORTALMANAGER.CREATED` | `str` | Any string text | `'&aPortal &d{id} &ahas been created.'` | Configures the technical `CREATED` parameter for `PORTALMANAGER.CREATED` in `messages.yml`. |
| `PORTALMANAGER.UPDATED` | `str` | Any string text | `'&aPortal &d{id} &ahas been updated.'` | Configures the technical `UPDATED` parameter for `PORTALMANAGER.UPDATED` in `messages.yml`. |
| `PORTALMANAGER.HOLOGRAM-UPDATED` | `str` | Any string text | `'&aPortal &d{id} &ahologram has been...'` | Configures the technical `HOLOGRAM-UPDATED` parameter for `PORTALMANAGER.HOLOGRAM-UPDATED` in `messages.yml`. |
| `PORTALMANAGER.DELETED` | `str` | Any string text | `'&aPortal &d{id} &ahas been deleted.'` | Configures the technical `DELETED` parameter for `PORTALMANAGER.DELETED` in `messages.yml`. |
| `PORTALMANAGER.TOGGLED` | `str` | Any string text | `'&aPortal &d{id} &ais now &f{state}&...'` | Configures the technical `TOGGLED` parameter for `PORTALMANAGER.TOGGLED` in `messages.yml`. |
| `PORTALMANAGER.LIST-HEADER` | `str` | Any string text | `'&8&m---------------- &dPortals &7({...'` | Configures the technical `LIST-HEADER` parameter for `PORTALMANAGER.LIST-HEADER` in `messages.yml`. |
| `PORTALMANAGER.LIST-ENTRY` | `str` | Any string text | `'&7- &d{id} &8[&f{state}&8] &7cuboid...'` | Configures the technical `LIST-ENTRY` parameter for `PORTALMANAGER.LIST-ENTRY` in `messages.yml`. |
| `PORTALMANAGER.INFO-HEADER` | `str` | Any string text | `'&8&m---------------- &dPortal: &f{i...'` | Configures the technical `INFO-HEADER` parameter for `PORTALMANAGER.INFO-HEADER` in `messages.yml`. |
| `PORTALMANAGER.INFO-DISPLAY` | `str` | Any string text | `'&7Display: &f{display}'` | Configures the technical `INFO-DISPLAY` parameter for `PORTALMANAGER.INFO-DISPLAY` in `messages.yml`. |
| `PORTALMANAGER.INFO-STATE` | `str` | Any string text | `'&7State: &f{state}'` | Configures the technical `INFO-STATE` parameter for `PORTALMANAGER.INFO-STATE` in `messages.yml`. |
| `PORTALMANAGER.INFO-CUBOID` | `str` | Any string text | `'&7Cuboid: &f{cuboid}'` | Configures the technical `INFO-CUBOID` parameter for `PORTALMANAGER.INFO-CUBOID` in `messages.yml`. |
| `PORTALMANAGER.INFO-DESTINATION` | `str` | Any string text | `'&7Destination: &f{destination}'` | Configures the technical `INFO-DESTINATION` parameter for `PORTALMANAGER.INFO-DESTINATION` in `messages.yml`. |
| *(5 additional sub-keys configured in section)* | | | | |

### 3. Practical Setup Example

```yaml
PORTALMANAGER:
  # The text or value for Usage. Available options: Any valid string text
  USAGE: '&cUsage: /portalmanager <list|info|create|delete|setcuboid|setdestination|setdisplay|toggle|setpriority|sethologramhere>'
  # The text or value for No Permission. Available options: Any valid string text
  NO-PERMISSION: '&cYou do not have permission to manage portals.'
  # The text or value for Player Only. Available options: Any valid string text
  PLAYER-ONLY: '&cOnly players can use this command.'
  # The text or value for Create Usage. Available options: Any valid string text
  CREATE-USAGE: '&cUsage: /portalmanager create <id> <cuboid> <rtp_selector>'
  # The text or value for Delete Usage. Available options: Any valid string text
  DELETE-USAGE: '&cUsage: /portalmanager delete <id>'
  # The text or value for Info Usage. Available options: Any valid string text
  INFO-USAGE: '&cUsage: /portalmanager info <id>'
  # The text or value for Setcuboid Usage. Available options: Any valid s
```

---

## Section: `WORTH`

### 1. Commented Setup Code Example

```yaml
WORTH:
  # The text or value for Default. Available options: Any valid string text
  DEFAULT: '&b1 {item} &fis worth &a{price_formatted}'
  # The text or value for Hand Item. Available options: Any valid string text
  HAND-ITEM: '&b{amount} {item} &fis worth &a{total_formatted}'
  # The text or value for No Sellable. Available options: Any valid string text
  NO-SELLABLE: '&cThis item is not sellable.'
  # The text or value for Container Breakdown. Available options: Any valid string text
  CONTAINER-BREAKDOWN: '&7Base: &f${base} &8| &7Contents: &f${contents}'
  # The text or value for Reloaded. Available options: Any valid string text
  RELOADED: '&aWorth config reloaded.'
  # The text or value for No Admin Permission. Available options: Any valid string text
  NO-ADMIN-PERMISSION: '&cYou do not have permission to reload worth settings.'
# Configuration section for Bounty.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `WORTH.DEFAULT` | `str` | Any string text | `'&b1 {item} &fis worth &a{price_form...'` | Configures the technical `DEFAULT` parameter for `WORTH.DEFAULT` in `messages.yml`. |
| `WORTH.HAND-ITEM` | `str` | Any string text | `'&b{amount} {item} &fis worth &a{tot...'` | Configures the technical `HAND-ITEM` parameter for `WORTH.HAND-ITEM` in `messages.yml`. |
| `WORTH.NO-SELLABLE` | `str` | Any string text | `'&cThis item is not sellable.'` | Configures the technical `NO-SELLABLE` parameter for `WORTH.NO-SELLABLE` in `messages.yml`. |
| `WORTH.CONTAINER-BREAKDOWN` | `str` | Any string text | `'&7Base: &f${base} &8| &7Contents: &...'` | Configures the technical `CONTAINER-BREAKDOWN` parameter for `WORTH.CONTAINER-BREAKDOWN` in `messages.yml`. |
| `WORTH.RELOADED` | `str` | Any string text | `'&aWorth config reloaded.'` | Configures the technical `RELOADED` parameter for `WORTH.RELOADED` in `messages.yml`. |
| `WORTH.NO-ADMIN-PERMISSION` | `str` | Any string text | `'&cYou do not have permission to rel...'` | Configures the technical `NO-ADMIN-PERMISSION` parameter for `WORTH.NO-ADMIN-PERMISSION` in `messages.yml`. |

### 3. Practical Setup Example

```yaml
WORTH:
  # The text or value for Default. Available options: Any valid string text
  DEFAULT: '&b1 {item} &fis worth &a{price_formatted}'
  # The text or value for Hand Item. Available options: Any valid string text
  HAND-ITEM: '&b{amount} {item} &fis worth &a{total_formatted}'
  # The text or value for No Sellable. Available options: Any valid string text
  NO-SELLABLE: '&cThis item is not sellable.'
  # The text or value for Container Breakdown. Available options: Any valid string text
  CONTAINER-BREAKDOWN: '&7Base: &f${base} &8| &7Contents: &f${contents}'
  # The text or value for Reloaded. Available options: Any valid string text
  RELOADED: '&aWorth config reloaded.'
  # The text or value for No Admin Permission. Available options: Any valid string text
  NO-ADMIN-PERMISSION: '&cYou do not have permission to reload worth settings.'
# Configuration section for Bounty.
```

---

## Section: `BOUNTY`

### 1. Commented Setup Code Example

```yaml
BOUNTY:
  # The text or value for New. Available options: Any valid string text
  NEW: '&aA new bounty of ${price} has been placed on {player}!'
  # The text or value for Increased. Available options: Any valid string text
  INCREASED: '&aThe bounty for {player} has been increased by ${price}!'
  # The text or value for Alert. Available options: Any valid string text
  ALERT: '&aYou have a new bounty from {who} for ${price}'
  # The text or value for Claim Success. Available options: Any valid string text
  CLAIM-SUCCESS: '&7You received &b${amount}&7 for killing &c{player}&7.'
  # The text or value for Player Not Exist. Available options: Any valid string text
  PLAYER-NOT-EXIST: '&cThat player does not exist.'
  # The text or value for Player Has Bounty. Available options: Any valid string text
  PLAYER-HAS-BOUNTY: '&b{player} &7has a bounty of &c${amount}'
  # The text or value for No Bounty. Available options: Any valid string text
  NO-BOUNTY: '&cThe user does not have a bounty.'
  # The text or value for Cant Self Bounty. Available options: Any valid string text
  CANT-SELF-BOUNTY: '&cYou can''t do this yourself.'
  # The text or value for Minimum Price. Available options: Any valid string text
  MINIMUM-PRICE: '&cMinimum price is $1.00.'
# Configuration section for Billford.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `BOUNTY.NEW` | `str` | Any string text | `'&aA new bounty of ${price} has been...'` | Configures the technical `NEW` parameter for `BOUNTY.NEW` in `messages.yml`. |
| `BOUNTY.INCREASED` | `str` | Any string text | `'&aThe bounty for {player} has been ...'` | Configures the technical `INCREASED` parameter for `BOUNTY.INCREASED` in `messages.yml`. |
| `BOUNTY.ALERT` | `str` | Any string text | `'&aYou have a new bounty from {who} ...'` | Configures the technical `ALERT` parameter for `BOUNTY.ALERT` in `messages.yml`. |
| `BOUNTY.CLAIM-SUCCESS` | `str` | Any string text | `'&7You received &b${amount}&7 for ki...'` | Configures the technical `CLAIM-SUCCESS` parameter for `BOUNTY.CLAIM-SUCCESS` in `messages.yml`. |
| `BOUNTY.PLAYER-NOT-EXIST` | `str` | Any string text | `'&cThat player does not exist.'` | Configures the technical `PLAYER-NOT-EXIST` parameter for `BOUNTY.PLAYER-NOT-EXIST` in `messages.yml`. |
| `BOUNTY.PLAYER-HAS-BOUNTY` | `str` | Any string text | `'&b{player} &7has a bounty of &c${am...'` | Configures the technical `PLAYER-HAS-BOUNTY` parameter for `BOUNTY.PLAYER-HAS-BOUNTY` in `messages.yml`. |
| `BOUNTY.NO-BOUNTY` | `str` | Any string text | `'&cThe user does not have a bounty.'` | Configures the technical `NO-BOUNTY` parameter for `BOUNTY.NO-BOUNTY` in `messages.yml`. |
| `BOUNTY.CANT-SELF-BOUNTY` | `str` | Any string text | `'&cYou can't do this yourself.'` | Configures the technical `CANT-SELF-BOUNTY` parameter for `BOUNTY.CANT-SELF-BOUNTY` in `messages.yml`. |
| `BOUNTY.MINIMUM-PRICE` | `str` | Any string text | `'&cMinimum price is $1.00.'` | Configures the technical `MINIMUM-PRICE` parameter for `BOUNTY.MINIMUM-PRICE` in `messages.yml`. |

### 3. Practical Setup Example

```yaml
BOUNTY:
  # The text or value for New. Available options: Any valid string text
  NEW: '&aA new bounty of ${price} has been placed on {player}!'
  # The text or value for Increased. Available options: Any valid string text
  INCREASED: '&aThe bounty for {player} has been increased by ${price}!'
  # The text or value for Alert. Available options: Any valid string text
  ALERT: '&aYou have a new bounty from {who} for ${price}'
  # The text or value for Claim Success. Available options: Any valid string text
  CLAIM-SUCCESS: '&7You received &b${amount}&7 for killing &c{player}&7.'
  # The text or value for Player Not Exist. Available options: Any valid string text
  PLAYER-NOT-EXIST: '&cThat player does not exist.'
  # The text or value for Player Has Bounty. Available options: Any valid string text
  PLAYER-HAS-BOUNTY: '&b{player} &7has a bounty of &c${amount}'
  # The text or value for No Bounty. Available options: Any valid string text
  NO-BOUNTY: '&cThe user does not have a bounty.'

```

---

