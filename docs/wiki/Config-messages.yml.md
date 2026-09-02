# Detailed Configuration & Setup Guide: `messages.yml`

This is the official technical setup guide for `messages.yml` in **UltimateDonutSMP**.
Each section details the exact commented setup code block, allowed option values, data types, default values, and in-depth functional behavior.

One formatting rule catches people out. A line whose visible letters are all uppercase is shown
in Title Case instead, whenever that line also carries a colour code or a placeholder.
[FAQ entry 16](FAQ) covers why, and how to write a shouting line that survives it.

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
  # The text or value for Cannot Invite Yourself. Available options: Any valid string text
  CANNOT-INVITE-YOURSELF: '&cYou cannot invite yourself!'
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
| `TEAM.CANNOT-INVITE-YOURSELF` | `str` | Any string text | `'&cYou cannot invite yourself!'` | Configures the technical `CANNOT-INVITE-YOURSELF` parameter for `TEAM.CANNOT-INVITE-YOURSELF` in `messages.yml`. |
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
  # The text or value for Cannot Invite Yourself. Available options: Any valid string text
  CANNOT-INVITE-YOURSELF: '&cYou cannot invite yourself!'
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
| `IGNORE.USAGE` | `str` | Any string text | `'&cUsage: /ignore <player\|list>'` | Configures the technical `USAGE` parameter for `IGNORE.USAGE` in `messages.yml`. |
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
  # The text or value for Cancelled. Available options: Any valid string text
  CANCELLED: '&7Home input cancelled.'
# Configuration section for Warp.
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
  CREATED: '&aWarp &b{name} &ahas been created.'
  # The text or value for Deleted. Available options: Any valid string text
  DELETED: '&aWarp &b{name} &ahas been deleted.'
  # The text or value for Already Exists. Available options: Any valid string text
  ALREADY-EXISTS: '&cWarp ''&e{name}&c'' already exists.'
  # The text or value for Invalid Name. Available options: Any valid string text
  INVALID-NAME: '&cInvalid warp name. Use only letters, numbers, dashes, and underscores.'
# Configuration section for Warpmanager.
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
| `WARPMANAGER.USAGE` | `str` | Any string text | `'&cUsage: /warpmanager <create\|delet...'` | Configures the technical `USAGE` parameter for `WARPMANAGER.USAGE` in `messages.yml`. |
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
  # The text or value for Create Failed. Available options: Any valid string text
  CREATE-FAILED: '&cFailed to create warp ''&e{name}&c''.'
# Configuration section for Portal.
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
| `PORTALMANAGER.USAGE` | `str` | Any string text | `'&cUsage: /portalmanager <list\|info\|...'` | Configures the technical `USAGE` parameter for `PORTALMANAGER.USAGE` in `messages.yml`. |
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
  # The text or value for Meta Current. Available options: Any valid string text
  META-CURRENT: '&b{item} &fis the farming meta at &a{multiplier}x&f and sells for &a{price_formatted}&f, up from &7{base_formatted}&f. Next rotation in &e{countdown}&f.'
  # The text or value for Meta Inactive. Available options: Any valid string text
  META-INACTIVE: '&cNo farming meta is running right now.'
  # The text or value for Meta Rotated. Available options: Any valid string text
  META-ROTATED: '&b{item} &fis the new farming meta at &a{multiplier}x&f and now sells for &a{price_formatted}&f. Next rotation in &e{countdown}&f.'
# Configuration section for Bounty.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `WORTH.DEFAULT` | `str` | Any string text | `'&b1 {item} &fis worth &a{price_form...'` | Configures the technical `DEFAULT` parameter for `WORTH.DEFAULT` in `messages.yml`. |
| `WORTH.HAND-ITEM` | `str` | Any string text | `'&b{amount} {item} &fis worth &a{tot...'` | Configures the technical `HAND-ITEM` parameter for `WORTH.HAND-ITEM` in `messages.yml`. |
| `WORTH.NO-SELLABLE` | `str` | Any string text | `'&cThis item is not sellable.'` | Configures the technical `NO-SELLABLE` parameter for `WORTH.NO-SELLABLE` in `messages.yml`. |
| `WORTH.CONTAINER-BREAKDOWN` | `str` | Any string text | `'&7Base: &f${base} &8\| &7Contents: &...'` | Configures the technical `CONTAINER-BREAKDOWN` parameter for `WORTH.CONTAINER-BREAKDOWN` in `messages.yml`. |
| `WORTH.RELOADED` | `str` | Any string text | `'&aWorth config reloaded.'` | Configures the technical `RELOADED` parameter for `WORTH.RELOADED` in `messages.yml`. |
| `WORTH.NO-ADMIN-PERMISSION` | `str` | Any string text | `'&cYou do not have permission to rel...'` | Configures the technical `NO-ADMIN-PERMISSION` parameter for `WORTH.NO-ADMIN-PERMISSION` in `messages.yml`. |
| `WORTH.META-CURRENT` | `str` | Any string text | `'&b{item} &fis the farming meta at &a{mult...'` | Answer to `/meta` while a rotation is running. Supports `{item}`, `{multiplier}`, `{base}`, `{base_formatted}`, `{price}`, `{price_formatted}`, and `{countdown}`. |
| `WORTH.META-INACTIVE` | `str` | Any string text | `'&cNo farming meta is running right now.'` | Answer to `/meta` when `META.ENABLED` is off in `worth.yml` or no configured item has a price. |
| `WORTH.META-ROTATED` | `str` | Any string text | `'&b{item} &fis the new farming meta at &a{mult...'` | Broadcast when the meta moves to the next item, unless `META.ANNOUNCE_ON_ROTATE` is off. Supports the same placeholders as `WORTH.META-CURRENT`. |

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
  # The text or value for Meta Current. Available options: Any valid string text
  META-CURRENT: '&b{item} &fis the farming meta at &a{multiplier}x&f and sells for &a{price_formatted}&f, up from &7{base_formatted}&f. Next rotation in &e{countdown}&f.'
  # The text or value for Meta Inactive. Available options: Any valid string text
  META-INACTIVE: '&cNo farming meta is running right now.'
  # The text or value for Meta Rotated. Available options: Any valid string text
  META-ROTATED: '&b{item} &fis the new farming meta at &a{multiplier}x&f and now sells for &a{price_formatted}&f. Next rotation in &e{countdown}&f.'
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
  # The text or value for Cant Self Bounty. Available options: Any valid string text
  CANT-SELF-BOUNTY: '&cYou can''t do this yourself.'
  # The text or value for Minimum Price. Available options: Any valid string text
  MINIMUM-PRICE: '&cMinimum price is $1.00.'
# Configuration section for Billford.
```

---

## Section: `BILLFORD`

### 1. Commented Setup Code Example

```yaml
BILLFORD:
  # The text or value for Required Contents. Available options: Any valid string text
  REQUIRED_CONTENTS: '&cYou don''t have all the required items for this trade.'
  # The text or value for Full Inventory. Available options: Any valid string text
  FULL-INVENTORY: '&cYour inventory is full! Free up a slot before confirming the
    trade.'
  # The text or value for Trade Completed. Available options: Any valid string text
  TRADE-COMPLETED: '&aTrade complete! You received &f{reward}&a. &7Shards: &#A303F9{shard_bonus}&7, Money: &a${money_bonus}&7. &8Next refresh in &f{next_rotation}&8.'
  # The text or value for Limit Reached. Available options: Any valid string text
  LIMIT-REACHED: '&cYou''ve reached the trade limit for this rotation. Come back after
    the next trade change!'
  # The text or value for Click Cooldown. Available options: Any valid string text
  CLICK-COOLDOWN: '&cSlow down. Billford is still processing your last click.'
  # The text or value for Trade Changed. Available options: Any valid string text
  TRADE-CHANGED: '&eBillford rotated to a new offer while you had the menu open. &7Next
    change in &b{countdown}&7.'
  # The text or value for Busy. Available options: Any valid string text
  BUSY: '&cBillford is already processing your last click.'
  # The text or value for Not Configured. Available options: Any valid string text
  NOT-CONFIGURED: '&cBillford does not have an active trade configured right now.'
  # The text or value for No Permission. Available options: Any valid string text
  NO-PERMISSION: '&cYou need &f{permission} &cto use Billford.'
# Configuration section for Balance.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `BILLFORD.REQUIRED_CONTENTS` | `str` | Any string text | `'&cYou don't have all the required i...'` | Sent when the player confirms a trade without every item the offer asks for. |
| `BILLFORD.FULL-INVENTORY` | `str` | Any string text | `'&cYour inventory is full! Free up a...'` | Sent when the reward has nowhere to go. The trade is not taken, so nothing is lost. |
| `BILLFORD.TRADE-COMPLETED` | `str` | Any string text | `'&aTrade complete! You received &f{r...'` | The receipt after a successful trade. `{reward}` is the item, `{shard_bonus}` and the money placeholder carry the extras paid on top. |
| `BILLFORD.LIMIT-REACHED` | `str` | Any string text | `'&cYou've reached the trade limit fo...'` | Sent once the player has traded as often as this rotation allows. They can trade again after the next rotation. |
| `BILLFORD.CLICK-COOLDOWN` | `str` | Any string text | `'&cSlow down. Billford is still proc...'` | Sent when clicks arrive faster than the trade can be processed. Guards against double-spending a click. |
| `BILLFORD.TRADE-CHANGED` | `str` | Any string text | `'&eBillford rotated to a new offer w...'` | Sent when the rotation moved on while the menu was still open, so the offer on screen is stale. |
| `BILLFORD.BUSY` | `str` | Any string text | `'&cBillford is already processing yo...'` | Sent when a previous click from the same player is still being handled. |
| `BILLFORD.NOT-CONFIGURED` | `str` | Any string text | `'&cBillford does not have an active ...'` | Sent when no trade is set up at all, so there is nothing for Billford to offer. |
| `BILLFORD.NO-PERMISSION` | `str` | Any string text | `'&cYou need &f{permission} &cto use ...'` | Sent when the player lacks the node. `{permission}` is filled with the node they need, so they can be told exactly what to ask for. |

Billford is the rotating trade NPC. These cover the refusals and the receipt.

### 3. Practical Setup Example

```yaml
BILLFORD:
  # The text or value for Required Contents. Available options: Any valid string text
  REQUIRED_CONTENTS: '&cYou don''t have all the required items for this trade.'
  # The text or value for Full Inventory. Available options: Any valid string text
  FULL-INVENTORY: '&cYour inventory is full! Free up a slot before confirming the
    trade.'
  # The text or value for Trade Completed. Available options: Any valid string text
  TRADE-COMPLETED: '&aTrade complete! You received &f{reward}&a. &7Shards: &#A303F9{shard_bonus}&7, Money: &a${money_bonus}&7. &8Next refresh in &f{next_rotation}&8.'
  # The text or value for Limit Reached. Available options: Any valid string text
  LIMIT-REACHED: '&cYou''ve reached the trade limit for this rotation. Come back after
    the next trade change!'
  # The text or value for Click Cooldown. Available options: Any valid string text
  CLICK-COOLDOWN: '&cSlow down. Billford is still processing your last click.'
  # The text or value for Trade Changed. Available options: Any valid string text
  TRADE-CHANGED: '&eBillford rotated to a new offer while you had the menu open. &7Next
    change in &b{countdown}&7.'
  # The text or value for Busy. Available options: Any valid string text
  BUSY: '&cBillford is already processing your last click.'
  # The text or value for Not Configured. Available options: Any valid string text
  NOT-CONFIGURED: '&cBillford does not have an active trade configured right now.'
  # The text or value for No Permission. Available options: Any valid string text
  NO-PERMISSION: '&cYou need &f{permission} &cto use Billford.'
# Configuration section for Balance.
```

---
## Section: `BALANCE`

### 1. Commented Setup Code Example

```yaml
BALANCE:
  # The text or value for Your Balance. Available options: Any valid string text
  YOUR-BALANCE: '&7You have &a${amount}&7.'
  # The text or value for Other Balance. Available options: Any valid string text
  OTHER-BALANCE: '&b{player} &7has &a${amount}&7.'
  # The text or value for Your Shards. Available options: Any valid string text
  YOUR-SHARDS: '&7Your shards: &#A303F9{amount}'
  # The text or value for Other Shards. Available options: Any valid string text
  OTHER-SHARDS: '&7{player}''s shards: &#A303F9{amount}'
  # Configuration section for Admin.
  ADMIN:
    # The text or value for Invalid Amount. Available options: Any valid string text
    INVALID-AMOUNT: '&cInvalid amount format. Use numbers with optional K, M, or B
      suffix (e.g. 100K, 1.5M, 2B)'
    # The text or value for Must Be Positive. Available options: Any valid string text
    MUST-BE-POSITIVE: '&cThe amount must be positive.'
    # The text or value for Must Be Non Negative. Available options: Any valid string text
    MUST-BE-NON-NEGATIVE: '&cThe amount must be zero or positive.'
    # The text or value for Player Not Found. Available options: Any valid string text
    PLAYER-NOT-FOUND: '&cPlayer not found.'
    # The text or value for Target Not Enough Money. Available options: Any valid string text
    TARGET-NOT-ENOUGH-MONEY: '&c{player} does not have enough money. Current balance:
      &f${balance}&c.'
    # The text or value for Add Money Success. Available options: Any valid string text
    ADD-MONEY-SUCCESS: '&aAdded &f${amount} &ato &b{player}&a. New balance: &f${balance}&a.'
    # The text or value for Add Money Received. Available options: Any valid string text
    ADD-MONEY-RECEIVED: '&a{admin} added &f${amount} &ato your balance. New balance:
      &f${balance}&a.'
    # The text or value for Remove Money Success. Available options: Any valid string text
    REMOVE-MONEY-SUCCESS: '&aRemoved &f${amount} &afrom &b{player}&a. New balance:
      &f${balance}&a.'
    # The text or value for Remove Money Received. Available options: Any valid string text
    REMOVE-MONEY-RECEIVED: '&c{admin} removed &f${amount} &cfrom your balance. New balance: &f${balance}&c.'
    # The text or value for Set Money Success. Available options: Any valid string text
    SET-MONEY-SUCCESS: '&aSet &b{player}&a''s balance to &f${amount}&a. Previous balance:
      &f${previous_balance}&a.'
    # The text or value for Set Money Received. Available options: Any valid string text
    SET-MONEY-RECEIVED: '&e{admin} set your balance to &f${amount}&e. Previous balance:
      &f${previous_balance}&e.'
  # Configuration section for Pay.
  PAY:
    # The text or value for Cant Pay Self. Available options: Any valid string text
    CANT-PAY-SELF: '&cYou cannot pay yourself.'
    # The text or value for Invalid Amount. Available options: Any valid string text
    INVALID-AMOUNT: '&cInvalid amount format. Use numbers with optional K, M, or B
      suffix (e.g. 100K, 1.5M, 2B)'
    # The text or value for Must Be Positive. Available options: Any valid string text
    MUST-BE-POSITIVE: '&cThe amount must be positive.'
    # The text or value for Not Enough Money. Available options: Any valid string text
    NOT-ENOUGH-MONEY: '&cYou don''t have enough money.'
    # The text or value for Transaction Error. Available options: Any valid string text
    TRANSACTION-ERROR: '&cError occurred during transaction. Your money has been refunded.'
    # The text or value for Player Not Online. Available options: Any valid string text
    PLAYER-NOT-ONLINE: '&cPlayer not online.'
    # The text or value for Target Profile Not Found. Available options: Any valid string text
    TARGET-PROFILE-NOT-FOUND: '&cTarget profile not found.'
    # The text or value for Success Sender. Available options: Any valid string text
    SUCCESS-SENDER: '&7You paid &b{player} &a${amount}&7.'
    # The text or value for Success Receiver. Available options: Any valid string text
    SUCCESS-RECEIVER: '&7You received &a${amount}&7 from &b{player}&7.'
    # The text or value for Target Disabled Payments. Available options: Any valid string text
    TARGET-DISABLED-PAYMENTS: '&cThe target player has the payments disabled.'
# Configuration section for Shard Pay.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `BALANCE.YOUR-BALANCE` | `str` | Any string text | `'&7You have &a${amount}&7.'` | Reply to checking your own money. `{amount}` is the balance. |
| `BALANCE.OTHER-BALANCE` | `str` | Any string text | `'&b{player} &7has &a${amount}&7.'` | Reply to checking somebody else's money. `{player}` and `{amount}`. |
| `BALANCE.YOUR-SHARDS` | `str` | Any string text | `'&7Your shards: &#A303F9{amount}'` | Reply to checking your own shard balance. |
| `BALANCE.OTHER-SHARDS` | `str` | Any string text | `'&7{player}'s shards: &#A303F9{amoun...'` | Reply to checking somebody else's shard balance. |
| `BALANCE.ADMIN.INVALID-AMOUNT` | `str` | Any string text | `'&cInvalid amount format. Use number...'` | Sent when an admin types an amount the parser cannot read. The suffix forms it does accept are listed in the message itself. |
| `BALANCE.ADMIN.MUST-BE-POSITIVE` | `str` | Any string text | `'&cThe amount must be positive.'` | Sent when an add or remove is given zero or a negative amount. |
| `BALANCE.ADMIN.MUST-BE-NON-NEGATIVE` | `str` | Any string text | `'&cThe amount must be zero or positi...'` | The set variant, which does allow zero but not a negative balance. |
| `BALANCE.ADMIN.PLAYER-NOT-FOUND` | `str` | Any string text | `'&cPlayer not found.'` | Sent when the named target has no profile on this server. |
| `BALANCE.ADMIN.TARGET-NOT-ENOUGH-MONEY` | `str` | Any string text | `'&c{player} does not have enough mon...'` | Sent when removing more money than the target holds. `{player}` and `{balance}`. |
| `BALANCE.ADMIN.ADD-MONEY-SUCCESS` | `str` | Any string text | `'&aAdded &f${amount} &ato &b{player}...'` | Confirmation to the admin after adding money. `{amount}`, `{player}` and the resulting `{balance}`. |
| `BALANCE.ADMIN.ADD-MONEY-RECEIVED` | `str` | Any string text | `'&a{admin} added &f${amount} &ato yo...'` | What the target sees. `{admin}` names who did it. |
| `BALANCE.ADMIN.REMOVE-MONEY-SUCCESS` | `str` | Any string text | `'&aRemoved &f${amount} &afrom &b{pla...'` | Confirmation to the admin after taking money away. |
| `BALANCE.ADMIN.REMOVE-MONEY-RECEIVED` | `str` | Any string text | `'&c{admin} removed &f${amount} &cfro...'` | What the target sees when money is taken from them. |
| `BALANCE.ADMIN.SET-MONEY-SUCCESS` | `str` | Any string text | `'&aSet &b{player}&a's balance to &f$...'` | Confirmation after setting a balance outright. `{previous_balance}` is what it was before. |
| `BALANCE.ADMIN.SET-MONEY-RECEIVED` | `str` | Any string text | `'&e{admin} set your balance to &f${a...'` | What the target sees when their balance is set. |
| `BALANCE.PAY.CANT-PAY-SELF` | `str` | Any string text | `'&cYou cannot pay yourself.'` | Sent when somebody tries to pay themselves. |
| `BALANCE.PAY.INVALID-AMOUNT` | `str` | Any string text | `'&cInvalid amount format. Use number...'` | Sent when the amount cannot be parsed. |
| `BALANCE.PAY.MUST-BE-POSITIVE` | `str` | Any string text | `'&cThe amount must be positive.'` | Sent when the amount is zero or negative. |
| `BALANCE.PAY.NOT-ENOUGH-MONEY` | `str` | Any string text | `'&cYou don't have enough money.'` | Sent when the sender cannot cover the payment. |
| `BALANCE.PAY.TRANSACTION-ERROR` | `str` | Any string text | `'&cError occurred during transaction...'` | Sent when the transfer failed part way through. The money is refunded, which is why the message says so. |
| `BALANCE.PAY.PLAYER-NOT-ONLINE` | `str` | Any string text | `'&cPlayer not online.'` | Sent when the target is offline. Payments need both players present. |
| `BALANCE.PAY.TARGET-PROFILE-NOT-FOUND` | `str` | Any string text | `'&cTarget profile not found.'` | Sent when the target is online but has no economy profile loaded. |
| `BALANCE.PAY.SUCCESS-SENDER` | `str` | Any string text | `'&7You paid &b{player} &a${amount}&7...'` | Receipt for the payer. |
| `BALANCE.PAY.SUCCESS-RECEIVER` | `str` | Any string text | `'&7You received &a${amount}&7 from &...'` | Notice to the player being paid. |
| `BALANCE.PAY.TARGET-DISABLED-PAYMENTS` | `str` | Any string text | `'&cThe target player has the payment...'` | Sent when the target has turned payments off in their own settings. |

Balance lookups, the admin money commands, and player to player payments.

### 3. Practical Setup Example

```yaml
BALANCE:
  # The text or value for Your Balance. Available options: Any valid string text
  YOUR-BALANCE: '&7You have &a${amount}&7.'
  # The text or value for Other Balance. Available options: Any valid string text
  OTHER-BALANCE: '&b{player} &7has &a${amount}&7.'
  # The text or value for Your Shards. Available options: Any valid string text
  YOUR-SHARDS: '&7Your shards: &#A303F9{amount}'
  # The text or value for Other Shards. Available options: Any valid string text
  OTHER-SHARDS: '&7{player}''s shards: &#A303F9{amount}'
  # Configuration section for Admin.
  ADMIN:
    # The text or value for Invalid Amount. Available options: Any valid string text
    INVALID-AMOUNT: '&cInvalid amount format. Use numbers with optional K, M, or B
      suffix (e.g. 100K, 1.5M, 2B)'
    # The text or value for Must Be Positive. Available options: Any valid string text
    MUST-BE-POSITIVE: '&cThe amount must be positive.'
    # The text or value for Must Be Non Negative. Available options: Any valid string text
    MUST-BE-NON-NEGATIVE: '&cThe amount must be zero or positive.'
    # The text or value for Player Not Found. Available options: Any valid string text
    PLAYER-NOT-FOUND: '&cPlayer not found.'
    # The text or value for Target Not Enough Money. Available options: Any valid string text
    TARGET-NOT-ENOUGH-MONEY: '&c{player} does not have enough money. Current balance:
      &f${balance}&c.'
    # The text or value for Add Money Success. Available options: Any valid string text
    ADD-MONEY-SUCCESS: '&aAdded &f${amount} &ato &b{player}&a. New balance: &f${balance}&a.'
    # The text or value for Add Money Received. Available options: Any valid string text
    ADD-MONEY-RECEIVED: '&a{admin} added &f${amount} &ato your balance. New balance:
      &f${balance}&a.'
    # The text or value for Remove Money Success. Available options: Any valid string text
    REMOVE-MONEY-SUCCESS: '&aRemoved &f${amount} &afrom &b{player}&a. New balance:
      &f${balance}&a.'
    # The text or value for Remove Money Received. Available options: Any valid string text
    REMOVE-MONEY-RECEIVED: '&c{admin} removed &f${amount} &cfrom your balance. New balance: &f${balance}&c.'
    # The text or value for Set Money Success. Available options: Any valid string text
    SET-MONEY-SUCCESS: '&aSet &b{player}&a''s balance to &f${amount}&a. Previous balance:
      &f${previous_balance}&a.'
    # The text or value for Set Money Received. Available options: Any valid string text
    SET-MONEY-RECEIVED: '&e{admin} set your balance to &f${amount}&e. Previous balance:
      &f${previous_balance}&e.'
  # Configuration section for Pay.
  PAY:
    # The text or value for Cant Pay Self. Available options: Any valid string text
    CANT-PAY-SELF: '&cYou cannot pay yourself.'
    # The text or value for Invalid Amount. Available options: Any valid string text
    INVALID-AMOUNT: '&cInvalid amount format. Use numbers with optional K, M, or B
      suffix (e.g. 100K, 1.5M, 2B)'
    # The text or value for Must Be Positive. Available options: Any valid string text
    MUST-BE-POSITIVE: '&cThe amount must be positive.'
    # The text or value for Not Enough Money. Available options: Any valid string text
    NOT-ENOUGH-MONEY: '&cYou don''t have enough money.'
    # The text or value for Transaction Error. Available options: Any valid string text
    TRANSACTION-ERROR: '&cError occurred during transaction. Your money has been refunded.'
    # The text or value for Player Not Online. Available options: Any valid string text
    PLAYER-NOT-ONLINE: '&cPlayer not online.'
    # The text or value for Target Profile Not Found. Available options: Any valid string text
    TARGET-PROFILE-NOT-FOUND: '&cTarget profile not found.'
    # The text or value for Success Sender. Available options: Any valid string text
    SUCCESS-SENDER: '&7You paid &b{player} &a${amount}&7.'
    # The text or value for Success Receiver. Available options: Any valid string text
    SUCCESS-RECEIVER: '&7You received &a${amount}&7 from &b{player}&7.'
    # The text or value for Target Disabled Payments. Available options: Any valid string text
    TARGET-DISABLED-PAYMENTS: '&cThe target player has the payments disabled.'
# Configuration section for Shard Pay.
```

---
## Section: `SHARD_PAY`

### 1. Commented Setup Code Example

```yaml
SHARD_PAY:
  # The text or value for Cant Pay Self. Available options: Any valid string text
  CANT-PAY-SELF: '&cYou cannot pay yourself.'
  # The text or value for Invalid Amount. Available options: Any valid string text
  INVALID-AMOUNT: '&cInvalid amount format. Use numbers with optional K, M, or B suffix
    (e.g. 100K, 1.5M, 2B)'
  # The text or value for Must Be Positive. Available options: Any valid string text
  MUST-BE-POSITIVE: '&cThe amount must be positive.'
  # The text or value for Not Enough Shards. Available options: Any valid string text
  NOT-ENOUGH-SHARDS: '&cYou don''t have enough shards.'
  # The text or value for Target Profile Not Found. Available options: Any valid string text
  TARGET-PROFILE-NOT-FOUND: '&cTarget profile not found.'
  # The text or value for Target Disabled Payments. Available options: Any valid string text
  TARGET-DISABLED-PAYMENTS: '&cThe target player has the payments disabled.'
  # The text or value for Success Sender. Available options: Any valid string text
  SUCCESS-SENDER: '&7You paid &b{player} &#A303F9{amount}&7 shards.'
  # The text or value for Success Receiver. Available options: Any valid string text
  SUCCESS-RECEIVER: '&7You received &#A303F9{amount}&7 shards from &b{player}&7.'
# Configuration section for Findplayer.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `SHARD_PAY.CANT-PAY-SELF` | `str` | Any string text | `'&cYou cannot pay yourself.'` | Sent when somebody tries to pay themselves shards. |
| `SHARD_PAY.INVALID-AMOUNT` | `str` | Any string text | `'&cInvalid amount format. Use number...'` | Sent when the amount cannot be parsed. |
| `SHARD_PAY.MUST-BE-POSITIVE` | `str` | Any string text | `'&cThe amount must be positive.'` | Sent when the amount is zero or negative. |
| `SHARD_PAY.NOT-ENOUGH-SHARDS` | `str` | Any string text | `'&cYou don't have enough shards.'` | Sent when the sender does not hold that many shards. |
| `SHARD_PAY.TARGET-PROFILE-NOT-FOUND` | `str` | Any string text | `'&cTarget profile not found.'` | Sent when the target has no profile to pay into. |
| `SHARD_PAY.TARGET-DISABLED-PAYMENTS` | `str` | Any string text | `'&cThe target player has the payment...'` | Sent when the target has payments switched off. |
| `SHARD_PAY.SUCCESS-SENDER` | `str` | Any string text | `'&7You paid &b{player} &#A303F9{amou...'` | Receipt for the sender. |
| `SHARD_PAY.SUCCESS-RECEIVER` | `str` | Any string text | `'&7You received &#A303F9{amount}&7 s...'` | Notice to the player receiving the shards. |

The shard equivalent of paying money. Note the command itself ships disabled at `COMMANDS.SHARDPAY` in `config.yml`.

### 3. Practical Setup Example

```yaml
SHARD_PAY:
  # The text or value for Cant Pay Self. Available options: Any valid string text
  CANT-PAY-SELF: '&cYou cannot pay yourself.'
  # The text or value for Invalid Amount. Available options: Any valid string text
  INVALID-AMOUNT: '&cInvalid amount format. Use numbers with optional K, M, or B suffix
    (e.g. 100K, 1.5M, 2B)'
  # The text or value for Must Be Positive. Available options: Any valid string text
  MUST-BE-POSITIVE: '&cThe amount must be positive.'
  # The text or value for Not Enough Shards. Available options: Any valid string text
  NOT-ENOUGH-SHARDS: '&cYou don''t have enough shards.'
  # The text or value for Target Profile Not Found. Available options: Any valid string text
  TARGET-PROFILE-NOT-FOUND: '&cTarget profile not found.'
  # The text or value for Target Disabled Payments. Available options: Any valid string text
  TARGET-DISABLED-PAYMENTS: '&cThe target player has the payments disabled.'
  # The text or value for Success Sender. Available options: Any valid string text
  SUCCESS-SENDER: '&7You paid &b{player} &#A303F9{amount}&7 shards.'
  # The text or value for Success Receiver. Available options: Any valid string text
  SUCCESS-RECEIVER: '&7You received &#A303F9{amount}&7 shards from &b{player}&7.'
# Configuration section for Findplayer.
```

---
## Section: `FINDPLAYER`

### 1. Commented Setup Code Example

```yaml
FINDPLAYER:
  # The text or value for Afk. Available options: Any valid string text
  AFK: '&7{player}''s in the &#A303F9afk'
  # The text or value for Rtp Zone. Available options: Any valid string text
  RTP_ZONE: '&7{player}''s in the &crtpzone'
  # The text or value for Spawn. Available options: Any valid string text
  SPAWN: '&7{player}''s in the &bspawn'
  # The text or value for Overworld. Available options: Any valid string text
  OVERWORLD: '&7{player}''s in the &boverworld &7(&b{biome}&7)'
  # The text or value for Nether. Available options: Any valid string text
  NETHER: '&7{player}''s in the &bnether &7(&b{biome}&7)'
  # The text or value for The End. Available options: Any valid string text
  THE_END: '&7{player}''s in the &bthe end &7(&b{biome}&7)'
  # The text or value for Unknown. Available options: Any valid string text
  UNKNOWN: '&7{player}''s in the &b{world}'
# Configuration section for Punishments.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `FINDPLAYER.AFK` | `str` | Any string text | `'&7{player}'s in the &#A303F9afk'` | Used when the target is inside the AFK area. |
| `FINDPLAYER.RTP_ZONE` | `str` | Any string text | `'&7{player}'s in the &crtpzone'` | Used when the target is standing in the RTP zone cuboid. |
| `FINDPLAYER.SPAWN` | `str` | Any string text | `'&7{player}'s in the &bspawn'` | Used when the target is at spawn. |
| `FINDPLAYER.OVERWORLD` | `str` | Any string text | `'&7{player}'s in the &boverworld &7(...'` | Used for the overworld. `{biome}` is filled in alongside `{player}`. |
| `FINDPLAYER.NETHER` | `str` | Any string text | `'&7{player}'s in the &bnether &7(&b{...'` | The nether variant. |
| `FINDPLAYER.THE_END` | `str` | Any string text | `'&7{player}'s in the &bthe end &7(&b...'` | The end variant. |
| `FINDPLAYER.UNKNOWN` | `str` | Any string text | `'&7{player}'s in the &b{world}'` | The fallback for any other world, where `{world}` is used instead of a friendly name. |

One line per place the staff find command can report. The plugin picks the entry matching where the target actually is.

### 3. Practical Setup Example

```yaml
FINDPLAYER:
  # The text or value for Afk. Available options: Any valid string text
  AFK: '&7{player}''s in the &#A303F9afk'
  # The text or value for Rtp Zone. Available options: Any valid string text
  RTP_ZONE: '&7{player}''s in the &crtpzone'
  # The text or value for Spawn. Available options: Any valid string text
  SPAWN: '&7{player}''s in the &bspawn'
  # The text or value for Overworld. Available options: Any valid string text
  OVERWORLD: '&7{player}''s in the &boverworld &7(&b{biome}&7)'
  # The text or value for Nether. Available options: Any valid string text
  NETHER: '&7{player}''s in the &bnether &7(&b{biome}&7)'
  # The text or value for The End. Available options: Any valid string text
  THE_END: '&7{player}''s in the &bthe end &7(&b{biome}&7)'
  # The text or value for Unknown. Available options: Any valid string text
  UNKNOWN: '&7{player}''s in the &b{world}'
# Configuration section for Punishments.
```

---
## Section: `PUNISHMENTS`

### 1. Commented Setup Code Example

```yaml
PUNISHMENTS:
  # The text or value for Player Only. Available options: Any valid string text
  PLAYER-ONLY: '&cOnly players can use this command.'
  # The text or value for No Permission. Available options: Any valid string text
  NO-PERMISSION: '&cYou do not have permission to view punishment history.'
  # The text or value for No Create Permission. Available options: Any valid string text
  NO-CREATE-PERMISSION: '&cYou do not have permission to create punishments.'
  # The text or value for No Remove Permission. Available options: Any valid string text
  NO-REMOVE-PERMISSION: '&cYou do not have permission to remove punishments.'
  # The text or value for No Delete Permission. Available options: Any valid string text
  NO-DELETE-PERMISSION: '&cYou do not have permission to delete punishment history
    records.'
  # The text or value for Usage. Available options: Any valid string text
  USAGE: '&cUsage: /punishments [player]'
  # The text or value for Usage Ban. Available options: Any valid string text
  USAGE-BAN: '&cUsage: /ban <player> [reason]'
  # The text or value for Usage Tempban. Available options: Any valid string text
  USAGE-TEMPBAN: '&cUsage: /tempban <player> <time> [reason] &7(Time: 30s, 15m, 2h,
    5d, or 5d 15m 30s)'
  # The text or value for Usage Mute. Available options: Any valid string text
  USAGE-MUTE: '&cUsage: /mute <player> [reason]'
  # The text or value for Usage Tempmute. Available options: Any valid string text
  USAGE-TEMPMUTE: '&cUsage: /tempmute <player> <time> [reason] &7(Time: 30s, 15m,
    2h, 5d, or 5d 15m 30s)'
  # The text or value for Usage Vcmute. Available options: Any valid string text
  USAGE-VCMUTE: '&cUsage: /vcmute <player> [reason]'
  # The text or value for Usage Warn. Available options: Any valid string text
  USAGE-WARN: '&cUsage: /warn <player> [reason]'
  # The text or value for Usage Kick. Available options: Any valid string text
  USAGE-KICK: '&cUsage: /kick <player> [reason]'
  # The text or value for Usage Blacklist. Available options: Any valid string text
  USAGE-BLACKLIST: '&cUsage: /blacklist <player> [reason]'
  # The text or value for Usage Unban. Available options: Any valid string text
  USAGE-UNBAN: '&cUsage: /unban <player> [reason]'
  # The text or value for Usage Pardon. Available options: Any valid string text
  USAGE-PARDON: '&cUsage: /pardon <player> [reason]'
  # The text or value for Usage Unmute. Available options: Any valid string text
  USAGE-UNMUTE: '&cUsage: /unmute <player> [reason]'
  # The text or value for Usage Vcunmute. Available options: Any valid string text
  USAGE-VCUNMUTE: '&cUsage: /vcunmute <player> [reason]'
  # The text or value for Usage Unblacklist. Available options: Any valid string text
  USAGE-UNBLACKLIST: '&cUsage: /unblacklist <player> [reason]'
  # The text or value for Not Found. Available options: Any valid string text
  NOT-FOUND: '&cPlayer not found.'
  # The text or value for Target Offline. Available options: Any valid string text
  TARGET-OFFLINE: '&cThat player is not online.'
  # The text or value for Target Exempt. Available options: Any valid string text
  TARGET-EXEMPT: '&cYou cannot punish that player.'
  # The text or value for Invalid Duration. Available options: Any valid string text
  INVALID-DURATION: '&cInvalid time. Use values like 30s, 15m, 2h, 5d, or combine:
    5d 15m 30s.'
  # The text or value for Create Failed. Available options: Any valid string text
  CREATE-FAILED: '&cFailed to create punishment record.'
  # The text or value for Delete Failed. Available options: Any valid string text
  DELETE-FAILED: '&cFailed to delete punishment record #{id}.'
  # The text or value for Created. Available options: Any valid string text
  CREATED: '&aCreated &f{type} &apunishment for &b{player}&a. ID: &f#{id}'
  # The text or value for Deleted Record. Available options: Any valid string text
  DELETED-RECORD: '&aDeleted punishment history record &f#{id}&a.'
  # The text or value for Removed. Available options: Any valid string text
  REMOVED: '&aRemoved active &f{type} &apunishment(s) for &b{player}&a.'
  # The text or value for No Active. Available options: Any valid string text
  NO-ACTIVE: '&cNo active {type} punishment found for {player}.'
  # The text or value for Warn Received. Available options: Any valid string text
  WARN-RECEIVED: '&cWarning: &f{reason}'
  # The text or value for Ban. Available options: Any valid string text
  BAN: |
    &c&lYou have been banned!
    &8&m----------------------------
    &7Reason: &f%reason%
    &7Expires: &f%nicest_expiration%
    &7Banned by: &f%issuer%
    &8&m----------------------------
    &7Appeal at: &fdiscord.example.space
  # The text or value for Kick. Available options: Any valid string text
  KICK: |
    &c&lYou have been kicked!
    &8&m----------------------------
    &7Reason: &f%reason%
    &7Kicked by: &f%issuer%
    &8&m----------------------------
    &7You may reconnect
  # The text or value for Mute. Available options: Any valid string text
  MUTE: |
    &c&lYou have been muted!
    &8&m----------------------------
    &7Reason: &f%reason%
    &7Expires: &f%nicest_expiration%
    &7Muted by: &f%issuer%
    &8&m----------------------------
    &7You cannot speak in chat
  # The text or value for Voice Mute. Available options: Any valid string text
  VOICE-MUTE: |
    &c&lYou have been voice muted!
    &8&m----------------------------
    &7Reason: &f%reason%
    &7Expires: &f%nicest_expiration%
    &7Muted by: &f%issuer%
    &8&m----------------------------
    &7You cannot speak in voice chat
  # The text or value for Blacklist. Available options: Any valid string text
  BLACKLIST: |
    &4&lYOU HAVE BEEN BLACKLISTED!
    &8&m----------------------------
    &7Reason: &f%reason%
    &7Blacklisted by: &f%issuer%
    &8&m----------------------------
    &4You cannot join the server
# Configuration section for Social.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `PUNISHMENTS.PLAYER-ONLY` | `str` | Any string text | `'&cOnly players can use this command...'` | Sent when the console runs a subcommand that needs a player. |
| `PUNISHMENTS.NO-PERMISSION` | `str` | Any string text | `'&cYou do not have permission to vie...'` | Sent when somebody without the node tries to view punishment history. |
| `PUNISHMENTS.NO-CREATE-PERMISSION` | `str` | Any string text | `'&cYou do not have permission to cre...'` | Sent when they may look but not issue punishments. |
| `PUNISHMENTS.NO-REMOVE-PERMISSION` | `str` | Any string text | `'&cYou do not have permission to rem...'` | Sent when they may not lift an active punishment. |
| `PUNISHMENTS.NO-DELETE-PERMISSION` | `str` | Any string text | `'&cYou do not have permission to del...'` | Sent when they may not delete a history record. Deleting differs from removing: it erases the record rather than ending the punishment. |
| `PUNISHMENTS.USAGE` | `str` | Any string text | `'&cUsage: /punishments [player]'` | Usage line for the history command. |
| `PUNISHMENTS.USAGE-BAN` | `str` | Any string text | `'&cUsage: /ban <player> [reason]'` | Usage line for `/ban`. |
| `PUNISHMENTS.USAGE-TEMPBAN` | `str` | Any string text | `'&cUsage: /tempban <player> <time> [...'` | Usage line for `/tempban`, including the duration formats accepted. |
| `PUNISHMENTS.USAGE-MUTE` | `str` | Any string text | `'&cUsage: /mute <player> [reason]'` | Usage line for `/mute`. |
| `PUNISHMENTS.USAGE-TEMPMUTE` | `str` | Any string text | `'&cUsage: /tempmute <player> <time> ...'` | Usage line for `/tempmute`. |
| `PUNISHMENTS.USAGE-VCMUTE` | `str` | Any string text | `'&cUsage: /vcmute <player> [reason]'` | Usage line for `/vcmute`, the voice chat mute. |
| `PUNISHMENTS.USAGE-WARN` | `str` | Any string text | `'&cUsage: /warn <player> [reason]'` | Usage line for `/warn`. |
| `PUNISHMENTS.USAGE-KICK` | `str` | Any string text | `'&cUsage: /kick <player> [reason]'` | Usage line for `/kick`. |
| `PUNISHMENTS.USAGE-BLACKLIST` | `str` | Any string text | `'&cUsage: /blacklist <player> [reaso...'` | Usage line for `/blacklist`. |
| `PUNISHMENTS.USAGE-UNBAN` | `str` | Any string text | `'&cUsage: /unban <player> [reason]'` | Usage line for `/unban`. |
| `PUNISHMENTS.USAGE-PARDON` | `str` | Any string text | `'&cUsage: /pardon <player> [reason]'` | Usage line for `/pardon`. |
| `PUNISHMENTS.USAGE-UNMUTE` | `str` | Any string text | `'&cUsage: /unmute <player> [reason]'` | Usage line for `/unmute`. |
| `PUNISHMENTS.USAGE-VCUNMUTE` | `str` | Any string text | `'&cUsage: /vcunmute <player> [reason...'` | Usage line for `/vcunmute`. |
| `PUNISHMENTS.USAGE-UNBLACKLIST` | `str` | Any string text | `'&cUsage: /unblacklist <player> [rea...'` | Usage line for `/unblacklist`. |
| `PUNISHMENTS.NOT-FOUND` | `str` | Any string text | `'&cPlayer not found.'` | Sent when no player by that name is known. |
| `PUNISHMENTS.TARGET-OFFLINE` | `str` | Any string text | `'&cThat player is not online.'` | Sent when the action needs the target online and they are not. |
| `PUNISHMENTS.TARGET-EXEMPT` | `str` | Any string text | `'&cYou cannot punish that player.'` | Sent when the target is protected from punishment, so staff cannot punish each other by accident. |
| `PUNISHMENTS.INVALID-DURATION` | `str` | Any string text | `'&cInvalid time. Use values like 30s...'` | Sent when a duration cannot be read. The accepted forms are spelled out in the message. |
| `PUNISHMENTS.CREATE-FAILED` | `str` | Any string text | `'&cFailed to create punishment recor...'` | Sent when the record could not be written, usually a database problem. |
| `PUNISHMENTS.DELETE-FAILED` | `str` | Any string text | `'&cFailed to delete punishment recor...'` | Sent when a record could not be deleted. `{id}` is the record number. |
| `PUNISHMENTS.CREATED` | `str` | Any string text | `'&aCreated &f{type} &apunishment for...'` | Confirmation after issuing a punishment, quoting `{type}`, `{player}` and the new `{id}`. |
| `PUNISHMENTS.DELETED-RECORD` | `str` | Any string text | `'&aDeleted punishment history record...'` | Confirmation after erasing a history record. |
| `PUNISHMENTS.REMOVED` | `str` | Any string text | `'&aRemoved active &f{type} &apunishm...'` | Confirmation after lifting an active punishment. |
| `PUNISHMENTS.NO-ACTIVE` | `str` | Any string text | `'&cNo active {type} punishment found...'` | Sent when there was nothing of that type to lift. |
| `PUNISHMENTS.WARN-RECEIVED` | `str` | Any string text | `'&cWarning: &f{reason}'` | What a warned player sees. `{reason}` carries the staff note. |
| `PUNISHMENTS.BAN` | `str` | Any string text | `'&c&lYou have been banned! &8&m-----...'` | The screen a banned player is disconnected with, and what they see on every attempt to rejoin. Takes `%reason%`, `%nicest_expiration%` and `%issuer%`, each of which also works in `{brace}` form. |
| `PUNISHMENTS.KICK` | `str` | Any string text | `'&c&lYou have been kicked! &8&m-----...'` | Shown to a player as they are kicked, with `%reason%` and `%issuer%`. |
| `PUNISHMENTS.MUTE` | `str` | Any string text | `'&c&lYou have been muted! &8&m------...'` | Shown when the mute lands and again every time the muted player tries to speak, so it carries the whole explanation rather than a one-liner. Takes `%reason%`, `%nicest_expiration%` and `%issuer%`. |
| `PUNISHMENTS.VOICE-MUTE` | `str` | Any string text | `'&c&lYou have been voice muted! &8&m...'` | Shown to a player when a voice chat mute lands on them. |
| `PUNISHMENTS.BLACKLIST` | `str` | Any string text | `'&4&lYOU HAVE BEEN BLACKLISTED! &8&m...'` | The screen a blacklisted player is disconnected with, and what they see if they try to come back. |

Everything the punishment commands say, from the usage lines through to the screens a banned player sees.

### 3. Practical Setup Example

```yaml
PUNISHMENTS:
  # The text or value for Player Only. Available options: Any valid string text
  PLAYER-ONLY: '&cOnly players can use this command.'
  # The text or value for No Permission. Available options: Any valid string text
  NO-PERMISSION: '&cYou do not have permission to view punishment history.'
  # The text or value for No Create Permission. Available options: Any valid string text
  NO-CREATE-PERMISSION: '&cYou do not have permission to create punishments.'
  # The text or value for No Remove Permission. Available options: Any valid string text
  NO-REMOVE-PERMISSION: '&cYou do not have permission to remove punishments.'
  # The text or value for No Delete Permission. Available options: Any valid string text
  NO-DELETE-PERMISSION: '&cYou do not have permission to delete punishment history
    records.'
  # The text or value for Usage. Available options: Any valid string text
  USAGE: '&cUsage: /punishments [player]'
  # The text or value for Usage Ban. Available options: Any valid string text
  USAGE-BAN: '&cUsage: /ban <player> [reason]'
  # The text or value for Usage Tempban. Available options: Any valid string text
  USAGE-TEMPBAN: '&cUsage: /tempban <player> <time> [reason] &7(Time: 30s, 15m, 2h,
    5d, or 5d 15m 30s)'
  # The text or value for Usage Mute. Available options: Any valid string text
  USAGE-MUTE: '&cUsage: /mute <player> [reason]'
  # The text or value for Usage Tempmute. Available options: Any valid string text
  USAGE-TEMPMUTE: '&cUsage: /tempmute <player> <time> [reason] &7(Time: 30s, 15m,
    2h, 5d, or 5d 15m 30s)'
  # The text or value for Usage Vcmute. Available options: Any valid string text
  USAGE-VCMUTE: '&cUsage: /vcmute <player> [reason]'
  # The text or value for Usage Warn. Available options: Any valid string text
  USAGE-WARN: '&cUsage: /warn <player> [reason]'
  # The text or value for Usage Kick. Available options: Any valid string text
  USAGE-KICK: '&cUsage: /kick <player> [reason]'
  # The text or value for Usage Blacklist. Available options: Any valid string text
  USAGE-BLACKLIST: '&cUsage: /blacklist <player> [reason]'
  # The text or value for Usage Unban. Available options: Any valid string text
  USAGE-UNBAN: '&cUsage: /unban <player> [reason]'
  # The text or value for Usage Pardon. Available options: Any valid string text
  USAGE-PARDON: '&cUsage: /pardon <player> [reason]'
  # The text or value for Usage Unmute. Available options: Any valid string text
  USAGE-UNMUTE: '&cUsage: /unmute <player> [reason]'
  # The text or value for Usage Vcunmute. Available options: Any valid string text
  USAGE-VCUNMUTE: '&cUsage: /vcunmute <player> [reason]'
  # The text or value for Usage Unblacklist. Available options: Any valid string text
  USAGE-UNBLACKLIST: '&cUsage: /unblacklist <player> [reason]'
  # The text or value for Not Found. Available options: Any valid string text
  NOT-FOUND: '&cPlayer not found.'
  # The text or value for Target Offline. Available options: Any valid string text
  TARGET-OFFLINE: '&cThat player is not online.'
  # The text or value for Target Exempt. Available options: Any valid string text
  TARGET-EXEMPT: '&cYou cannot punish that player.'
  # The text or value for Invalid Duration. Available options: Any valid string text
  INVALID-DURATION: '&cInvalid time. Use values like 30s, 15m, 2h, 5d, or combine:
    5d 15m 30s.'
  # The text or value for Create Failed. Available options: Any valid string text
  CREATE-FAILED: '&cFailed to create punishment record.'
  # The text or value for Delete Failed. Available options: Any valid string text
  DELETE-FAILED: '&cFailed to delete punishment record #{id}.'
  # The text or value for Created. Available options: Any valid string text
  CREATED: '&aCreated &f{type} &apunishment for &b{player}&a. ID: &f#{id}'
  # The text or value for Deleted Record. Available options: Any valid string text
  DELETED-RECORD: '&aDeleted punishment history record &f#{id}&a.'
  # The text or value for Removed. Available options: Any valid string text
  REMOVED: '&aRemoved active &f{type} &apunishment(s) for &b{player}&a.'
  # The text or value for No Active. Available options: Any valid string text
  NO-ACTIVE: '&cNo active {type} punishment found for {player}.'
  # The text or value for Warn Received. Available options: Any valid string text
  WARN-RECEIVED: '&cWarning: &f{reason}'
  # The text or value for Ban. Available options: Any valid string text
  BAN: |
    &c&lYou have been banned!
    &8&m----------------------------
    &7Reason: &f%reason%
    &7Expires: &f%nicest_expiration%
    &7Banned by: &f%issuer%
    &8&m----------------------------
    &7Appeal at: &fdiscord.example.space
  # The text or value for Kick. Available options: Any valid string text
  KICK: |
    &c&lYou have been kicked!
    &8&m----------------------------
    &7Reason: &f%reason%
    &7Kicked by: &f%issuer%
    &8&m----------------------------
    &7You may reconnect
  # The text or value for Mute. Available options: Any valid string text
  MUTE: |
    &c&lYou have been muted!
    &8&m----------------------------
    &7Reason: &f%reason%
    &7Expires: &f%nicest_expiration%
    &7Muted by: &f%issuer%
    &8&m----------------------------
    &7You cannot speak in chat
  # The text or value for Voice Mute. Available options: Any valid string text
  VOICE-MUTE: |
    &c&lYou have been voice muted!
    &8&m----------------------------
    &7Reason: &f%reason%
    &7Expires: &f%nicest_expiration%
    &7Muted by: &f%issuer%
    &8&m----------------------------
    &7You cannot speak in voice chat
  # The text or value for Blacklist. Available options: Any valid string text
  BLACKLIST: |
    &4&lYOU HAVE BEEN BLACKLISTED!
    &8&m----------------------------
    &7Reason: &f%reason%
    &7Blacklisted by: &f%issuer%
    &8&m----------------------------
    &4You cannot join the server
# Configuration section for Social.
```

---
## Section: `SOCIAL`

### 1. Commented Setup Code Example

```yaml
SOCIAL:
  # Configuration section for Discord.
  DISCORD:
  - ''
  - '&fJoin our discord: &#6BF18Ddiscord.example.space'
  - ''
  # Configuration section for Twitter.
  TWITTER:
  - ''
  - '&fFollow on X: &#6BF18D@ElonMusk'
  - ''
  # Configuration section for Store.
  STORE:
  - ''
  - '&fVisit our store: &#6BF18Dstore.example.space'
  - ''
# Configuration section for Auction House.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `SOCIAL.DISCORD` | `list` | Lines of text | `['', '&fJoin our discord: &#6BF18Ddiscord.examp...]` | Printed by the discord command. Put your own invite here; the shipped address is a placeholder. |
| `SOCIAL.TWITTER` | `list` | Lines of text | `['', '&fFollow on X: &#6BF18D@ElonMusk', '']` | Printed by the twitter command. The shipped handle is a placeholder. |
| `SOCIAL.STORE` | `list` | Lines of text | `['', '&fVisit our store: &#6BF18Dstore.example....]` | Printed by the store command. The shipped address is a placeholder. |

The blocks of text the social commands print. Each is a list, so a line is one line in chat, and blank entries give the spacing.

### 3. Practical Setup Example

```yaml
SOCIAL:
  # Configuration section for Discord.
  DISCORD:
  - ''
  - '&fJoin our discord: &#6BF18Ddiscord.example.space'
  - ''
  # Configuration section for Twitter.
  TWITTER:
  - ''
  - '&fFollow on X: &#6BF18D@ElonMusk'
  - ''
  # Configuration section for Store.
  STORE:
  - ''
  - '&fVisit our store: &#6BF18Dstore.example.space'
  - ''
# Configuration section for Auction House.
```

---
## Section: `AUCTION_HOUSE`

### 1. Commented Setup Code Example

```yaml
AUCTION_HOUSE:
  # The text or value for Disabled. Available options: Any valid string text
  DISABLED: '&cAuction House is currently disabled.'
  # The text or value for Sell Usage. Available options: Any valid string text
  SELL_USAGE: '&cUsage: /ah sell <price>'
  # The text or value for Cancel Usage. Available options: Any valid string text
  CANCEL_USAGE: '&cUsage: /ah cancel <listingId>'
  # The text or value for Invalid Price. Available options: Any valid string text
  INVALID_PRICE: '&cInvalid price format. Use numbers like 100, 5K, or 1.5M.'
  # The text or value for Invalid Listing Id. Available options: Any valid string text
  INVALID_LISTING_ID: '&cInvalid listing id.'
  # The text or value for No Admin Permission. Available options: Any valid string text
  NO_ADMIN_PERMISSION: '&cYou do not have permission to reload Auction House settings.'
  # The text or value for Reloaded. Available options: Any valid string text
  RELOADED: '&aAuction House config reloaded.'
  # The text or value for No Item In Hand. Available options: Any valid string text
  NO_ITEM_IN_HAND: '&cHold the item you want to list in your main hand.'
  # The text or value for Item Blocked. Available options: Any valid string text
  ITEM_BLOCKED: '&cThat item cannot be listed in the Auction House.'
  # The text or value for Price Out Of Range. Available options: Any valid string text
  PRICE_OUT_OF_RANGE: '&cThat price is outside the allowed range.'
  # The text or value for No Money For Fee. Available options: Any valid string text
  NO_MONEY_FOR_FEE: '&cYou do not have enough money to pay the listing fee.'
  # The text or value for Max Listings Reached. Available options: Any valid string text
  MAX_LISTINGS_REACHED: '&cYou have reached your active listing limit.'
  # The text or value for Listing Created. Available options: Any valid string text
  LISTING_CREATED: '&aListing created! &7#{listing_id} &f{item} &7for &a${price}&7. Fee: &a${fee}&7. Expires in &f{expires}&7.'
  # The text or value for Listing Cancelled. Available options: Any valid string text
  LISTING_CANCELLED: '&eListing #{listing_id} &f({item}) &ehas been moved to your
    claim queue.'
  # The text or value for Listing Not Found. Available options: Any valid string text
  LISTING_NOT_FOUND: '&cThat listing no longer exists.'
  # The text or value for Listing Not Active. Available options: Any valid string text
  LISTING_NOT_ACTIVE: '&cThat listing is no longer active.'
  # The text or value for Not Your Listing. Available options: Any valid string text
  NOT_YOUR_LISTING: '&cThat listing does not belong to you.'
  # The text or value for Cannot Buy Own. Available options: Any valid string text
  CANNOT_BUY_OWN: '&cYou cannot buy your own listing.'
  # The text or value for Not Enough Money. Available options: Any valid string text
  NOT_ENOUGH_MONEY: '&cYou do not have enough money.'
  # The text or value for Full Inventory. Available options: Any valid string text
  FULL_INVENTORY: '&cYou need free inventory space to buy that item.'
  # The text or value for Purchase Success. Available options: Any valid string text
  PURCHASE_SUCCESS: '&aPurchased &f{item} &afor &a${price} &afrom &f{seller}&a.'
  # The text or value for Item Sold. Available options: Any valid string text
  ITEM_SOLD: '&aYour listing sold! &f{buyer} &abought &f{item} &afor &a${price}&a. Claimable payout: &a${payout}&a.'
  # The text or value for Claim Not Found. Available options: Any valid string text
  CLAIM_NOT_FOUND: '&cThat claim no longer exists.'
  # The text or value for Not Your Claim. Available options: Any valid string text
  NOT_YOUR_CLAIM: '&cThat claim does not belong to you.'
  # The text or value for Claim Already Claimed. Available options: Any valid string text
  CLAIM_ALREADY_CLAIMED: '&cThat claim was already collected.'
  # The text or value for Claim Inventory Full. Available options: Any valid string text
  CLAIM_INVENTORY_FULL: '&cYou need a free inventory slot to claim that item.'
  # The text or value for Claimed Money. Available options: Any valid string text
  CLAIMED_MONEY: '&aClaimed &a${amount} &afrom your Auction House sales.'
  # The text or value for Claimed Item. Available options: Any valid string text
  CLAIMED_ITEM: '&aClaimed returned item: &f{item}&a.'
# Configuration section for Orders.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `AUCTION_HOUSE.DISABLED` | `str` | Any string text | `'&cAuction House is currently disabl...'` | Sent when the auction house is switched off. |
| `AUCTION_HOUSE.SELL_USAGE` | `str` | Any string text | `'&cUsage: /ah sell <price>'` | Usage line for listing an item. |
| `AUCTION_HOUSE.CANCEL_USAGE` | `str` | Any string text | `'&cUsage: /ah cancel <listingId>'` | Usage line for cancelling a listing. |
| `AUCTION_HOUSE.INVALID_PRICE` | `str` | Any string text | `'&cInvalid price format. Use numbers...'` | Sent when the price cannot be parsed. |
| `AUCTION_HOUSE.INVALID_LISTING_ID` | `str` | Any string text | `'&cInvalid listing id.'` | Sent when the listing id is not a number. |
| `AUCTION_HOUSE.NO_ADMIN_PERMISSION` | `str` | Any string text | `'&cYou do not have permission to rel...'` | Sent when somebody without the node tries to reload the settings. |
| `AUCTION_HOUSE.RELOADED` | `str` | Any string text | `'&aAuction House config reloaded.'` | Confirmation after a successful reload. |
| `AUCTION_HOUSE.NO_ITEM_IN_HAND` | `str` | Any string text | `'&cHold the item you want to list in...'` | Sent when listing with an empty hand. |
| `AUCTION_HOUSE.ITEM_BLOCKED` | `str` | Any string text | `'&cThat item cannot be listed in the...'` | Sent when the item is on the blocked list. |
| `AUCTION_HOUSE.PRICE_OUT_OF_RANGE` | `str` | Any string text | `'&cThat price is outside the allowed...'` | Sent when the price sits outside the configured minimum and maximum. |
| `AUCTION_HOUSE.NO_MONEY_FOR_FEE` | `str` | Any string text | `'&cYou do not have enough money to p...'` | Sent when the seller cannot pay the listing fee. |
| `AUCTION_HOUSE.MAX_LISTINGS_REACHED` | `str` | Any string text | `'&cYou have reached your active list...'` | Sent when the seller already has as many active listings as they are allowed. |
| `AUCTION_HOUSE.LISTING_CREATED` | `str` | Any string text | `'&aListing created! &7#{listing_id} ...'` | The receipt for a new listing, with `{listing_id}`, `{item}`, `{price}` and the `{fee}` charged. |
| `AUCTION_HOUSE.LISTING_CANCELLED` | `str` | Any string text | `'&eListing #{listing_id} &f({item}) ...'` | Sent when a listing is pulled. The item goes to the claim store rather than straight back to the inventory. |
| `AUCTION_HOUSE.LISTING_NOT_FOUND` | `str` | Any string text | `'&cThat listing no longer exists.'` | Sent when the listing has already gone. |
| `AUCTION_HOUSE.LISTING_NOT_ACTIVE` | `str` | Any string text | `'&cThat listing is no longer active.'` | Sent when the listing exists but is no longer on sale. |
| `AUCTION_HOUSE.NOT_YOUR_LISTING` | `str` | Any string text | `'&cThat listing does not belong to y...'` | Sent when acting on somebody else s listing. |
| `AUCTION_HOUSE.CANNOT_BUY_OWN` | `str` | Any string text | `'&cYou cannot buy your own listing.'` | Sent when a seller tries to buy their own listing. |
| `AUCTION_HOUSE.NOT_ENOUGH_MONEY` | `str` | Any string text | `'&cYou do not have enough money.'` | Sent when the buyer cannot afford it. |
| `AUCTION_HOUSE.FULL_INVENTORY` | `str` | Any string text | `'&cYou need free inventory space to ...'` | Sent when the buyer has no room. The purchase does not go through. |
| `AUCTION_HOUSE.PURCHASE_SUCCESS` | `str` | Any string text | `'&aPurchased &f{item} &afor &a${pric...'` | Receipt for the buyer, naming the `{seller}`. |
| `AUCTION_HOUSE.ITEM_SOLD` | `str` | Any string text | `'&aYour listing sold! &f{buyer} &abo...'` | Notice to the seller when their listing sells, including the payout waiting to be claimed. |
| `AUCTION_HOUSE.CLAIM_NOT_FOUND` | `str` | Any string text | `'&cThat claim no longer exists.'` | Sent when the claim has already gone. |
| `AUCTION_HOUSE.NOT_YOUR_CLAIM` | `str` | Any string text | `'&cThat claim does not belong to you...'` | Sent when claiming something belonging to somebody else. |
| `AUCTION_HOUSE.CLAIM_ALREADY_CLAIMED` | `str` | Any string text | `'&cThat claim was already collected.'` | Sent when the claim was collected already. |
| `AUCTION_HOUSE.CLAIM_INVENTORY_FULL` | `str` | Any string text | `'&cYou need a free inventory slot to...'` | Sent when there is no free slot for the item being claimed. |
| `AUCTION_HOUSE.CLAIMED_MONEY` | `str` | Any string text | `'&aClaimed &a${amount} &afrom your A...'` | Receipt for collecting sale proceeds. |
| `AUCTION_HOUSE.CLAIMED_ITEM` | `str` | Any string text | `'&aClaimed returned item: &f{item}&a...'` | Receipt for collecting a returned or bought item. |

Listing, buying and claiming in the auction house.

### 3. Practical Setup Example

```yaml
AUCTION_HOUSE:
  # The text or value for Disabled. Available options: Any valid string text
  DISABLED: '&cAuction House is currently disabled.'
  # The text or value for Sell Usage. Available options: Any valid string text
  SELL_USAGE: '&cUsage: /ah sell <price>'
  # The text or value for Cancel Usage. Available options: Any valid string text
  CANCEL_USAGE: '&cUsage: /ah cancel <listingId>'
  # The text or value for Invalid Price. Available options: Any valid string text
  INVALID_PRICE: '&cInvalid price format. Use numbers like 100, 5K, or 1.5M.'
  # The text or value for Invalid Listing Id. Available options: Any valid string text
  INVALID_LISTING_ID: '&cInvalid listing id.'
  # The text or value for No Admin Permission. Available options: Any valid string text
  NO_ADMIN_PERMISSION: '&cYou do not have permission to reload Auction House settings.'
  # The text or value for Reloaded. Available options: Any valid string text
  RELOADED: '&aAuction House config reloaded.'
  # The text or value for No Item In Hand. Available options: Any valid string text
  NO_ITEM_IN_HAND: '&cHold the item you want to list in your main hand.'
  # The text or value for Item Blocked. Available options: Any valid string text
  ITEM_BLOCKED: '&cThat item cannot be listed in the Auction House.'
  # The text or value for Price Out Of Range. Available options: Any valid string text
  PRICE_OUT_OF_RANGE: '&cThat price is outside the allowed range.'
  # The text or value for No Money For Fee. Available options: Any valid string text
  NO_MONEY_FOR_FEE: '&cYou do not have enough money to pay the listing fee.'
  # The text or value for Max Listings Reached. Available options: Any valid string text
  MAX_LISTINGS_REACHED: '&cYou have reached your active listing limit.'
  # The text or value for Listing Created. Available options: Any valid string text
  LISTING_CREATED: '&aListing created! &7#{listing_id} &f{item} &7for &a${price}&7. Fee: &a${fee}&7. Expires in &f{expires}&7.'
  # The text or value for Listing Cancelled. Available options: Any valid string text
  LISTING_CANCELLED: '&eListing #{listing_id} &f({item}) &ehas been moved to your
    claim queue.'
  # The text or value for Listing Not Found. Available options: Any valid string text
  LISTING_NOT_FOUND: '&cThat listing no longer exists.'
  # The text or value for Listing Not Active. Available options: Any valid string text
  LISTING_NOT_ACTIVE: '&cThat listing is no longer active.'
  # The text or value for Not Your Listing. Available options: Any valid string text
  NOT_YOUR_LISTING: '&cThat listing does not belong to you.'
  # The text or value for Cannot Buy Own. Available options: Any valid string text
  CANNOT_BUY_OWN: '&cYou cannot buy your own listing.'
  # The text or value for Not Enough Money. Available options: Any valid string text
  NOT_ENOUGH_MONEY: '&cYou do not have enough money.'
  # The text or value for Full Inventory. Available options: Any valid string text
  FULL_INVENTORY: '&cYou need free inventory space to buy that item.'
  # The text or value for Purchase Success. Available options: Any valid string text
  PURCHASE_SUCCESS: '&aPurchased &f{item} &afor &a${price} &afrom &f{seller}&a.'
  # The text or value for Item Sold. Available options: Any valid string text
  ITEM_SOLD: '&aYour listing sold! &f{buyer} &abought &f{item} &afor &a${price}&a. Claimable payout: &a${payout}&a.'
  # The text or value for Claim Not Found. Available options: Any valid string text
  CLAIM_NOT_FOUND: '&cThat claim no longer exists.'
  # The text or value for Not Your Claim. Available options: Any valid string text
  NOT_YOUR_CLAIM: '&cThat claim does not belong to you.'
  # The text or value for Claim Already Claimed. Available options: Any valid string text
  CLAIM_ALREADY_CLAIMED: '&cThat claim was already collected.'
  # The text or value for Claim Inventory Full. Available options: Any valid string text
  CLAIM_INVENTORY_FULL: '&cYou need a free inventory slot to claim that item.'
  # The text or value for Claimed Money. Available options: Any valid string text
  CLAIMED_MONEY: '&aClaimed &a${amount} &afrom your Auction House sales.'
  # The text or value for Claimed Item. Available options: Any valid string text
  CLAIMED_ITEM: '&aClaimed returned item: &f{item}&a.'
# Configuration section for Orders.
```

---
## Section: `ORDERS`

### 1. Commented Setup Code Example

```yaml
ORDERS:
  # The text or value for Disabled. Available options: Any valid string text
  DISABLED: '&cOrders is currently disabled.'
  # The text or value for No Admin Permission. Available options: Any valid string text
  NO_ADMIN_PERMISSION: '&cYou do not have permission to reload Orders settings.'
  # The text or value for Reloaded. Available options: Any valid string text
  RELOADED: '&aOrders config reloaded.'
  # The text or value for Prompt Quantity. Available options: Any valid string text
  PROMPT_QUANTITY: '&7Enter the order quantity for &f{item}&7 in chat. Type &ccancel&7
    to abort.'
  # The text or value for Prompt Price. Available options: Any valid string text
  PROMPT_PRICE: '&7Enter the price each for &f{item}&7 in chat. Type &ccancel&7 to
    abort.'
  # The text or value for Input Cancelled. Available options: Any valid string text
  INPUT_CANCELLED: '&7Order creation cancelled.'
  # The text or value for No Pending Order. Available options: Any valid string text
  NO_PENDING_ORDER: '&cThere is no pending order draft to confirm.'
  # The text or value for Item Blocked. Available options: Any valid string text
  ITEM_BLOCKED: '&cThat item cannot be ordered.'
  # The text or value for Invalid Quantity. Available options: Any valid string text
  INVALID_QUANTITY: '&cInvalid quantity. Use a whole number greater than 0.'
  # The text or value for Quantity Out Of Range. Available options: Any valid string text
  QUANTITY_OUT_OF_RANGE: '&cQuantity must be between 1 and {max}.'
  # The text or value for Invalid Price. Available options: Any valid string text
  INVALID_PRICE: '&cInvalid price format. Use numbers like 100, 5K, or 1.5M.'
  # The text or value for Price Out Of Range. Available options: Any valid string text
  PRICE_OUT_OF_RANGE: '&cPrice each must be between &f{min_formatted}&c and &f{max_formatted}&c.'
  # The text or value for Total Too High. Available options: Any valid string text
  TOTAL_TOO_HIGH: '&cTotal order budget cannot exceed &f{max_formatted}&c.'
  # The text or value for Not Enough Money. Available options: Any valid string text
  NOT_ENOUGH_MONEY: '&cYou do not have enough money for that order.'
  # The text or value for Max Active Reached. Available options: Any valid string text
  MAX_ACTIVE_REACHED: '&cYou have reached your active order limit.'
  # The text or value for Created. Available options: Any valid string text
  CREATED: '&aOrder created! &7#{order_id} &ffor &e{quantity} {item}&7 at &a${price_each} &7each. Budget locked: &a${budget}&7.'
  # The text or value for Order Not Found. Available options: Any valid string text
  ORDER_NOT_FOUND: '&cThat order no longer exists.'
  # The text or value for Order Not Active. Available options: Any valid string text
  ORDER_NOT_ACTIVE: '&cThat order is no longer active.'
  # The text or value for Not Your Order. Available options: Any valid string text
  NOT_YOUR_ORDER: '&cThat order does not belong to you.'
  # The text or value for Cannot Deliver Own. Available options: Any valid string text
  CANNOT_DELIVER_OWN: '&cYou cannot deliver to your own order.'
  # The text or value for No Matching Items. Available options: Any valid string text
  NO_MATCHING_ITEMS: '&cYou do not have the required items to deliver.'
  # The text or value for Order Full. Available options: Any valid string text
  ORDER_FULL: '&cThat order is already full.'
  # The text or value for Delivery Success. Available options: Any valid string text
  DELIVERY_SUCCESS: '&aDelivered &e{quantity} {item}&a and received &a${payout}&a.'
  # The text or value for Cancelled. Available options: Any valid string text
  CANCELLED: '&eOrder #{order_id} &ehas been closed. Remaining escrow was moved to
    your collect queue.'
  # The text or value for Claim Not Found. Available options: Any valid string text
  CLAIM_NOT_FOUND: '&cThat claim no longer exists.'
  # The text or value for Not Your Claim. Available options: Any valid string text
  NOT_YOUR_CLAIM: '&cThat claim does not belong to you.'
  # The text or value for Claim Already Claimed. Available options: Any valid string text
  CLAIM_ALREADY_CLAIMED: '&cThat claim was already collected.'
  # The text or value for Claim Inventory Full. Available options: Any valid string text
  CLAIM_INVENTORY_FULL: '&cYou need a free inventory slot to claim that item.'
  # The text or value for Claimed Refund. Available options: Any valid string text
  CLAIMED_REFUND: '&aClaimed escrow refund of &a${amount}&a.'
  # The text or value for Claimed Item. Available options: Any valid string text
  CLAIMED_ITEM: '&aClaimed delivered item: &f{item}&a.'
# Configuration section for Stats Wipe.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `ORDERS.DISABLED` | `str` | Any string text | `'&cOrders is currently disabled.'` | Sent when orders are switched off. |
| `ORDERS.NO_ADMIN_PERMISSION` | `str` | Any string text | `'&cYou do not have permission to rel...'` | Sent when somebody without the node tries to reload the settings. |
| `ORDERS.RELOADED` | `str` | Any string text | `'&aOrders config reloaded.'` | Confirmation after a successful reload. |
| `ORDERS.PROMPT_QUANTITY` | `str` | Any string text | `'&7Enter the order quantity for &f{i...'` | Asks how many of `{item}` the order should be for. Typing cancel backs out. |
| `ORDERS.PROMPT_PRICE` | `str` | Any string text | `'&7Enter the price each for &f{item}...'` | Asks the price per item. |
| `ORDERS.INPUT_CANCELLED` | `str` | Any string text | `'&7Order creation cancelled.'` | Sent when the player cancels part way through creating an order. |
| `ORDERS.NO_PENDING_ORDER` | `str` | Any string text | `'&cThere is no pending order draft t...'` | Sent when confirming with no draft in progress. |
| `ORDERS.ITEM_BLOCKED` | `str` | Any string text | `'&cThat item cannot be ordered.'` | Sent when the item may not be ordered. |
| `ORDERS.INVALID_QUANTITY` | `str` | Any string text | `'&cInvalid quantity. Use a whole num...'` | Sent when the quantity is not a whole number above zero. |
| `ORDERS.QUANTITY_OUT_OF_RANGE` | `str` | Any string text | `'&cQuantity must be between 1 and {m...'` | Sent when the quantity is above the configured `{max}`. |
| `ORDERS.INVALID_PRICE` | `str` | Any string text | `'&cInvalid price format. Use numbers...'` | Sent when the price cannot be parsed. |
| `ORDERS.PRICE_OUT_OF_RANGE` | `str` | Any string text | `'&cPrice each must be between &f{min...'` | Sent when the price each falls outside the allowed band. |
| `ORDERS.TOTAL_TOO_HIGH` | `str` | Any string text | `'&cTotal order budget cannot exceed ...'` | Sent when quantity times price exceeds the maximum budget for one order. |
| `ORDERS.NOT_ENOUGH_MONEY` | `str` | Any string text | `'&cYou do not have enough money for ...'` | Sent when the buyer cannot fund the order. The whole budget is taken up front and held in escrow. |
| `ORDERS.MAX_ACTIVE_REACHED` | `str` | Any string text | `'&cYou have reached your active orde...'` | Sent when the buyer already has as many open orders as they are allowed. |
| `ORDERS.CREATED` | `str` | Any string text | `'&aOrder created! &7#{order_id} &ffo...'` | The receipt for a new order. |
| `ORDERS.ORDER_NOT_FOUND` | `str` | Any string text | `'&cThat order no longer exists.'` | Sent when the order has already gone. |
| `ORDERS.ORDER_NOT_ACTIVE` | `str` | Any string text | `'&cThat order is no longer active.'` | Sent when the order exists but is closed. |
| `ORDERS.NOT_YOUR_ORDER` | `str` | Any string text | `'&cThat order does not belong to you...'` | Sent when acting on somebody else s order. |
| `ORDERS.CANNOT_DELIVER_OWN` | `str` | Any string text | `'&cYou cannot deliver to your own or...'` | Sent when the buyer tries to fill their own order. |
| `ORDERS.NO_MATCHING_ITEMS` | `str` | Any string text | `'&cYou do not have the required item...'` | Sent when the deliverer is not carrying what the order asks for. |
| `ORDERS.ORDER_FULL` | `str` | Any string text | `'&cThat order is already full.'` | Sent when the order has already been filled. |
| `ORDERS.DELIVERY_SUCCESS` | `str` | Any string text | `'&aDelivered &e{quantity} {item}&a a...'` | Receipt for delivering into an order, with the `{payout}` earned. |
| `ORDERS.CANCELLED` | `str` | Any string text | `'&eOrder #{order_id} &ehas been clos...'` | Sent when an order is closed early. Whatever escrow is left becomes a claim. |
| `ORDERS.CLAIM_NOT_FOUND` | `str` | Any string text | `'&cThat claim no longer exists.'` | Sent when the claim has already gone. |
| `ORDERS.NOT_YOUR_CLAIM` | `str` | Any string text | `'&cThat claim does not belong to you...'` | Sent when claiming something belonging to somebody else. |
| `ORDERS.CLAIM_ALREADY_CLAIMED` | `str` | Any string text | `'&cThat claim was already collected.'` | Sent when the claim was collected already. |
| `ORDERS.CLAIM_INVENTORY_FULL` | `str` | Any string text | `'&cYou need a free inventory slot to...'` | Sent when there is no free slot for the item being claimed. |
| `ORDERS.CLAIMED_REFUND` | `str` | Any string text | `'&aClaimed escrow refund of &a${amou...'` | Receipt for collecting escrow back off a closed order. |
| `ORDERS.CLAIMED_ITEM` | `str` | Any string text | `'&aClaimed delivered item: &f{item}&...'` | Receipt for collecting items delivered into your order. |

Buy orders: creating one, delivering into somebody else s, and claiming afterwards.

### 3. Practical Setup Example

```yaml
ORDERS:
  # The text or value for Disabled. Available options: Any valid string text
  DISABLED: '&cOrders is currently disabled.'
  # The text or value for No Admin Permission. Available options: Any valid string text
  NO_ADMIN_PERMISSION: '&cYou do not have permission to reload Orders settings.'
  # The text or value for Reloaded. Available options: Any valid string text
  RELOADED: '&aOrders config reloaded.'
  # The text or value for Prompt Quantity. Available options: Any valid string text
  PROMPT_QUANTITY: '&7Enter the order quantity for &f{item}&7 in chat. Type &ccancel&7
    to abort.'
  # The text or value for Prompt Price. Available options: Any valid string text
  PROMPT_PRICE: '&7Enter the price each for &f{item}&7 in chat. Type &ccancel&7 to
    abort.'
  # The text or value for Input Cancelled. Available options: Any valid string text
  INPUT_CANCELLED: '&7Order creation cancelled.'
  # The text or value for No Pending Order. Available options: Any valid string text
  NO_PENDING_ORDER: '&cThere is no pending order draft to confirm.'
  # The text or value for Item Blocked. Available options: Any valid string text
  ITEM_BLOCKED: '&cThat item cannot be ordered.'
  # The text or value for Invalid Quantity. Available options: Any valid string text
  INVALID_QUANTITY: '&cInvalid quantity. Use a whole number greater than 0.'
  # The text or value for Quantity Out Of Range. Available options: Any valid string text
  QUANTITY_OUT_OF_RANGE: '&cQuantity must be between 1 and {max}.'
  # The text or value for Invalid Price. Available options: Any valid string text
  INVALID_PRICE: '&cInvalid price format. Use numbers like 100, 5K, or 1.5M.'
  # The text or value for Price Out Of Range. Available options: Any valid string text
  PRICE_OUT_OF_RANGE: '&cPrice each must be between &f{min_formatted}&c and &f{max_formatted}&c.'
  # The text or value for Total Too High. Available options: Any valid string text
  TOTAL_TOO_HIGH: '&cTotal order budget cannot exceed &f{max_formatted}&c.'
  # The text or value for Not Enough Money. Available options: Any valid string text
  NOT_ENOUGH_MONEY: '&cYou do not have enough money for that order.'
  # The text or value for Max Active Reached. Available options: Any valid string text
  MAX_ACTIVE_REACHED: '&cYou have reached your active order limit.'
  # The text or value for Created. Available options: Any valid string text
  CREATED: '&aOrder created! &7#{order_id} &ffor &e{quantity} {item}&7 at &a${price_each} &7each. Budget locked: &a${budget}&7.'
  # The text or value for Order Not Found. Available options: Any valid string text
  ORDER_NOT_FOUND: '&cThat order no longer exists.'
  # The text or value for Order Not Active. Available options: Any valid string text
  ORDER_NOT_ACTIVE: '&cThat order is no longer active.'
  # The text or value for Not Your Order. Available options: Any valid string text
  NOT_YOUR_ORDER: '&cThat order does not belong to you.'
  # The text or value for Cannot Deliver Own. Available options: Any valid string text
  CANNOT_DELIVER_OWN: '&cYou cannot deliver to your own order.'
  # The text or value for No Matching Items. Available options: Any valid string text
  NO_MATCHING_ITEMS: '&cYou do not have the required items to deliver.'
  # The text or value for Order Full. Available options: Any valid string text
  ORDER_FULL: '&cThat order is already full.'
  # The text or value for Delivery Success. Available options: Any valid string text
  DELIVERY_SUCCESS: '&aDelivered &e{quantity} {item}&a and received &a${payout}&a.'
  # The text or value for Cancelled. Available options: Any valid string text
  CANCELLED: '&eOrder #{order_id} &ehas been closed. Remaining escrow was moved to
    your collect queue.'
  # The text or value for Claim Not Found. Available options: Any valid string text
  CLAIM_NOT_FOUND: '&cThat claim no longer exists.'
  # The text or value for Not Your Claim. Available options: Any valid string text
  NOT_YOUR_CLAIM: '&cThat claim does not belong to you.'
  # The text or value for Claim Already Claimed. Available options: Any valid string text
  CLAIM_ALREADY_CLAIMED: '&cThat claim was already collected.'
  # The text or value for Claim Inventory Full. Available options: Any valid string text
  CLAIM_INVENTORY_FULL: '&cYou need a free inventory slot to claim that item.'
  # The text or value for Claimed Refund. Available options: Any valid string text
  CLAIMED_REFUND: '&aClaimed escrow refund of &a${amount}&a.'
  # The text or value for Claimed Item. Available options: Any valid string text
  CLAIMED_ITEM: '&aClaimed delivered item: &f{item}&a.'
# Configuration section for Stats Wipe.
```

---
## Section: `STATS-WIPE`

### 1. Commented Setup Code Example

```yaml
STATS-WIPE:
  # The text or value for No Permission. Available options: Any valid string text
  NO-PERMISSION: '&cYou do not have permission to use Stats Wipe.'
  # The text or value for Player Only Gui. Available options: Any valid string text
  PLAYER-ONLY-GUI: '&cOpen the Stats Wipe GUI in-game, or use /uds statswipe <target>
    confirm.'
  # The text or value for Invalid Target. Available options: Any valid string text
  INVALID-TARGET: '&cInvalid Stats Wipe target. Available: {targets}'
  # The text or value for Direct Usage. Available options: Any valid string text
  DIRECT-USAGE: '&cUse /uds statswipe <target> confirm to run directly, or /uds statswipe
    to open the GUI.'
  # The text or value for Busy. Available options: Any valid string text
  BUSY: '&cA wipe is already in progress.'
  # The text or value for Failed. Available options: Any valid string text
  FAILED: '&cStats Wipe failed: {error}'
  # The text or value for Success. Available options: Any valid string text
  SUCCESS: '&aWipe complete: &f{target}&a. Affected records: &f{count}&a.'
# Configuration section for Staff.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `STATS-WIPE.NO-PERMISSION` | `str` | Any string text | `'&cYou do not have permission to use...'` | Sent when somebody without the node tries to use it. |
| `STATS-WIPE.PLAYER-ONLY-GUI` | `str` | Any string text | `'&cOpen the Stats Wipe GUI in-game, ...'` | Sent when the console asks for the menu, which only exists in game. |
| `STATS-WIPE.INVALID-TARGET` | `str` | Any string text | `'&cInvalid Stats Wipe target. Availa...'` | Sent when the named target is not one the wipe understands. `{targets}` lists the valid ones. |
| `STATS-WIPE.DIRECT-USAGE` | `str` | Any string text | `'&cUse /uds statswipe <target> confi...'` | The usage line, spelling out both the direct form and the menu. |
| `STATS-WIPE.BUSY` | `str` | Any string text | `'&cA wipe is already in progress.'` | Sent when a wipe is already running. They do not queue. |
| `STATS-WIPE.FAILED` | `str` | Any string text | `'&cStats Wipe failed: {error}'` | Sent when the wipe stopped part way. `{error}` carries the reason. |
| `STATS-WIPE.SUCCESS` | `str` | Any string text | `'&aWipe complete: &f{target}&a. Affe...'` | Confirmation, with `{target}` and the `{count}` of records affected. |

The season stats wipe, which is guarded because it destroys records.

### 3. Practical Setup Example

```yaml
STATS-WIPE:
  # The text or value for No Permission. Available options: Any valid string text
  NO-PERMISSION: '&cYou do not have permission to use Stats Wipe.'
  # The text or value for Player Only Gui. Available options: Any valid string text
  PLAYER-ONLY-GUI: '&cOpen the Stats Wipe GUI in-game, or use /uds statswipe <target>
    confirm.'
  # The text or value for Invalid Target. Available options: Any valid string text
  INVALID-TARGET: '&cInvalid Stats Wipe target. Available: {targets}'
  # The text or value for Direct Usage. Available options: Any valid string text
  DIRECT-USAGE: '&cUse /uds statswipe <target> confirm to run directly, or /uds statswipe
    to open the GUI.'
  # The text or value for Busy. Available options: Any valid string text
  BUSY: '&cA wipe is already in progress.'
  # The text or value for Failed. Available options: Any valid string text
  FAILED: '&cStats Wipe failed: {error}'
  # The text or value for Success. Available options: Any valid string text
  SUCCESS: '&aWipe complete: &f{target}&a. Affected records: &f{count}&a.'
# Configuration section for Staff.
```

---
## Section: `STAFF`

### 1. Commented Setup Code Example

```yaml
STAFF:
  # The text or value for No Permission Others. Available options: Any valid string text
  NO_PERMISSION_OTHERS: '&c&lERROR &7» &cYou don''t have permission to manage other
    players'' staff mode!'
  # The text or value for Toggle Error. Available options: Any valid string text
  TOGGLE_ERROR: '&c&lERROR &7» &cFailed to toggle staff mode!'
  # The text or value for Status Enabled. Available options: Any valid string text
  STATUS_ENABLED: '&aEnabled'
  # The text or value for Status Disabled. Available options: Any valid string text
  STATUS_DISABLED: '&cDisabled'
  # The text or value for Icon Enabled. Available options: Any valid string text
  ICON_ENABLED: '&a✓'
  # The text or value for Icon Disabled. Available options: Any valid string text
  ICON_DISABLED: '&c✗'
  # The text or value for State Active. Available options: Any valid string text
  STATE_ACTIVE: '&aActive'
  # The text or value for State Inactive. Available options: Any valid string text
  STATE_INACTIVE: '&cInactive'
  # The text or value for Self Toggle. Available options: Any valid string text
  SELF_TOGGLE: '&6&lSTAFF MODE &7» %status%&7!'
  # The text or value for Self Status. Available options: Any valid string text
  SELF_STATUS: '&7Status: %icon% %status%'
  # The text or value for Other Toggle. Available options: Any valid string text
  OTHER_TOGGLE: '&6&lSTAFF MODE &7» %status% &7staff mode for &e%target%&7!'
  # The text or value for Target Notify. Available options: Any valid string text
  TARGET_NOTIFY: '&6&lSTAFF MODE &7» Your staff mode has been %status% &7by &e%staff%&7!'
  # The text or value for Broadcast. Available options: Any valid string text
  BROADCAST: '&8[&6STAFF&8] &e%staff% &7has %status% &7staff mode for &e%target%'
  # Configuration section for Activation Messages.
  ACTIVATION_MESSAGES:
  - '&7&m------------------------------'
  - '&6&lSTAFF MODE ACTIVATED'
  - '&7• Inventory cleared and saved'
  - '&7• Game mode set to Creative'
  - '&7• Flight enabled'
  - '&7• Use &e/vanish &7to become invisible'
  - '&7&m------------------------------'
# Configuration section for Fly.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `STAFF.NO_PERMISSION_OTHERS` | `str` | Any string text | `'&c&lERROR &7» &cYou don't have perm...'` | Sent when somebody may toggle their own staff mode but not other people's. |
| `STAFF.TOGGLE_ERROR` | `str` | Any string text | `'&c&lERROR &7» &cFailed to toggle st...'` | Sent when the toggle failed outright. |
| `STAFF.STATUS_ENABLED` | `str` | Any string text | `'&aEnabled'` | The word substituted for `%status%` when staff mode is being turned on. |
| `STAFF.STATUS_DISABLED` | `str` | Any string text | `'&cDisabled'` | The same for turning it off. |
| `STAFF.ICON_ENABLED` | `str` | Any string text | `'&a✓'` | The symbol substituted for `%icon%` in the on state. |
| `STAFF.ICON_DISABLED` | `str` | Any string text | `'&c✗'` | The symbol for the off state. |
| `STAFF.STATE_ACTIVE` | `str` | Any string text | `'&aActive'` | Wording used where a state rather than an action is shown. |
| `STAFF.STATE_INACTIVE` | `str` | Any string text | `'&cInactive'` | The inactive counterpart. |
| `STAFF.SELF_TOGGLE` | `str` | Any string text | `'&6&lSTAFF MODE &7» %status%&7!'` | Shown to staff toggling their own mode. |
| `STAFF.SELF_STATUS` | `str` | Any string text | `'&7Status: %icon% %status%'` | The status line that follows it, using both `%icon%` and `%status%`. |
| `STAFF.OTHER_TOGGLE` | `str` | Any string text | `'&6&lSTAFF MODE &7» %status% &7staff...'` | Shown to staff toggling somebody else's mode. `%target%` is that player. |
| `STAFF.TARGET_NOTIFY` | `str` | Any string text | `'&6&lSTAFF MODE &7» Your staff mode ...'` | Shown to the player whose mode was changed, naming the `%staff%` who did it. |
| `STAFF.BROADCAST` | `str` | Any string text | `'&8[&6STAFF&8] &e%staff% &7has %stat...'` | The announcement other staff see. |
| `STAFF.ACTIVATION_MESSAGES` | `list` | Lines of text | `['&7&m------------------------------', '&6&lSTA...]` | The block printed on entering staff mode. It describes what staff mode did to the inventory and game mode, so keep it in step if you change that behaviour. |

Staff mode toggling. Several of these are fragments rather than whole messages: the status and icon values are substituted into the others through `%status%` and `%icon%`.

### 3. Practical Setup Example

```yaml
STAFF:
  # The text or value for No Permission Others. Available options: Any valid string text
  NO_PERMISSION_OTHERS: '&c&lERROR &7» &cYou don''t have permission to manage other
    players'' staff mode!'
  # The text or value for Toggle Error. Available options: Any valid string text
  TOGGLE_ERROR: '&c&lERROR &7» &cFailed to toggle staff mode!'
  # The text or value for Status Enabled. Available options: Any valid string text
  STATUS_ENABLED: '&aEnabled'
  # The text or value for Status Disabled. Available options: Any valid string text
  STATUS_DISABLED: '&cDisabled'
  # The text or value for Icon Enabled. Available options: Any valid string text
  ICON_ENABLED: '&a✓'
  # The text or value for Icon Disabled. Available options: Any valid string text
  ICON_DISABLED: '&c✗'
  # The text or value for State Active. Available options: Any valid string text
  STATE_ACTIVE: '&aActive'
  # The text or value for State Inactive. Available options: Any valid string text
  STATE_INACTIVE: '&cInactive'
  # The text or value for Self Toggle. Available options: Any valid string text
  SELF_TOGGLE: '&6&lSTAFF MODE &7» %status%&7!'
  # The text or value for Self Status. Available options: Any valid string text
  SELF_STATUS: '&7Status: %icon% %status%'
  # The text or value for Other Toggle. Available options: Any valid string text
  OTHER_TOGGLE: '&6&lSTAFF MODE &7» %status% &7staff mode for &e%target%&7!'
  # The text or value for Target Notify. Available options: Any valid string text
  TARGET_NOTIFY: '&6&lSTAFF MODE &7» Your staff mode has been %status% &7by &e%staff%&7!'
  # The text or value for Broadcast. Available options: Any valid string text
  BROADCAST: '&8[&6STAFF&8] &e%staff% &7has %status% &7staff mode for &e%target%'
  # Configuration section for Activation Messages.
  ACTIVATION_MESSAGES:
  - '&7&m------------------------------'
  - '&6&lSTAFF MODE ACTIVATED'
  - '&7• Inventory cleared and saved'
  - '&7• Game mode set to Creative'
  - '&7• Flight enabled'
  - '&7• Use &e/vanish &7to become invisible'
  - '&7&m------------------------------'
# Configuration section for Fly.
```

---
## Section: `FLY`

### 1. Commented Setup Code Example

```yaml
FLY:
  # The text or value for Enabled. Available options: Any valid string text
  ENABLED: '&a✈ &7Flight mode &aactivated'
  # The text or value for Disabled. Available options: Any valid string text
  DISABLED: '&c✗ &7Flight mode &cdeactivated'
  # Message when player tries to enable fly outside spawn/cuboids
  PLAYER_RESTRICTED: '&c✗ &7You can only use flight in spawn or cuboids.'
  # Message when player tries to enable fly in combat
  PLAYER_IN_COMBAT: '&c✗ &7You cannot fly while in combat.'
  # Message when player flight is automatically disabled
  PLAYER_DISABLED: '&c✗ &7Flight deactivated because you left the allowed area or entered combat.'
# Configuration section for Flyspeed.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `FLY.ENABLED` | `str` | Any string text | `'&a✈ &7Flight mode &aactivated'` | Shown when flight turns on. |
| `FLY.DISABLED` | `str` | Any string text | `'&c✗ &7Flight mode &cdeactivated'` | Shown when the player turns it off themselves. |
| `FLY.PLAYER_RESTRICTED` | `str` | Any string text | `'&c✗ &7You can only use flight in sp...'` | Sent when flight is not allowed where they are standing. |
| `FLY.PLAYER_IN_COMBAT` | `str` | Any string text | `'&c✗ &7You cannot fly while in comba...'` | Sent when they are combat tagged. |
| `FLY.PLAYER_DISABLED` | `str` | Any string text | `'&c✗ &7Flight deactivated because yo...'` | Sent when flight is taken away mid-flight, because they left the allowed area or were pulled into combat. |

The flight toggle and the reasons it can refuse or switch itself off.

### 3. Practical Setup Example

```yaml
FLY:
  # The text or value for Enabled. Available options: Any valid string text
  ENABLED: '&a✈ &7Flight mode &aactivated'
  # The text or value for Disabled. Available options: Any valid string text
  DISABLED: '&c✗ &7Flight mode &cdeactivated'
  # Message when player tries to enable fly outside spawn/cuboids
  PLAYER_RESTRICTED: '&c✗ &7You can only use flight in spawn or cuboids.'
  # Message when player tries to enable fly in combat
  PLAYER_IN_COMBAT: '&c✗ &7You cannot fly while in combat.'
  # Message when player flight is automatically disabled
  PLAYER_DISABLED: '&c✗ &7Flight deactivated because you left the allowed area or entered combat.'
# Configuration section for Flyspeed.
```

---
## Section: `FLYSPEED`

### 1. Commented Setup Code Example

```yaml
FLYSPEED:
  # The text or value for Set. Available options: Any valid string text
  SET: '&aFly speed set to &f{speed}&a.'
  # The text or value for Invalid. Available options: Any valid string text
  INVALID: '&cInvalid speed. Please enter a number from 1 to 10.'
# Configuration section for Walkspeed.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `FLYSPEED.SET` | `str` | Any string text | `'&aFly speed set to &f{speed}&a.'` | Confirmation, with the `{speed}` applied. |
| `FLYSPEED.INVALID` | `str` | Any string text | `'&cInvalid speed. Please enter a num...'` | Sent when the value is outside the range the message names. |

Setting fly speed.

### 3. Practical Setup Example

```yaml
FLYSPEED:
  # The text or value for Set. Available options: Any valid string text
  SET: '&aFly speed set to &f{speed}&a.'
  # The text or value for Invalid. Available options: Any valid string text
  INVALID: '&cInvalid speed. Please enter a number from 1 to 10.'
# Configuration section for Walkspeed.
```

---
## Section: `WALKSPEED`

### 1. Commented Setup Code Example

```yaml
WALKSPEED:
  # The text or value for Set. Available options: Any valid string text
  SET: '&aWalk speed set to &f{speed}&a.'
  # The text or value for Invalid. Available options: Any valid string text
  INVALID: '&cInvalid speed. Please enter a number from 1 to 10.'
# Configuration section for Heal.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `WALKSPEED.SET` | `str` | Any string text | `'&aWalk speed set to &f{speed}&a.'` | Confirmation, with the `{speed}` applied. |
| `WALKSPEED.INVALID` | `str` | Any string text | `'&cInvalid speed. Please enter a num...'` | Sent when the value is outside the accepted range. |

Setting walk speed.

### 3. Practical Setup Example

```yaml
WALKSPEED:
  # The text or value for Set. Available options: Any valid string text
  SET: '&aWalk speed set to &f{speed}&a.'
  # The text or value for Invalid. Available options: Any valid string text
  INVALID: '&cInvalid speed. Please enter a number from 1 to 10.'
# Configuration section for Heal.
```

---
## Section: `HEAL`

### 1. Commented Setup Code Example

```yaml
HEAL:
  # The text or value for Self. Available options: Any valid string text
  SELF: '&a♡ &7Your health has been restored!'
  # The text or value for Other. Available options: Any valid string text
  OTHER: '&8[&cHeal&8] &7You healed &d%player%'
  # The text or value for Notify. Available options: Any valid string text
  NOTIFY: '&8[&cHeal&8] &d%sender% &7restored your health'
# Configuration section for Feed.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `HEAL.SELF` | `str` | Any string text | `'&a♡ &7Your health has been restored...'` | Shown when healing yourself. |
| `HEAL.OTHER` | `str` | Any string text | `'&8[&cHeal&8] &7You healed &d%player...'` | Shown to the staff member who healed somebody else. |
| `HEAL.NOTIFY` | `str` | Any string text | `'&8[&cHeal&8] &d%sender% &7restored ...'` | Shown to the player who was healed, naming the `%sender%`. |

The heal command.

### 3. Practical Setup Example

```yaml
HEAL:
  # The text or value for Self. Available options: Any valid string text
  SELF: '&a♡ &7Your health has been restored!'
  # The text or value for Other. Available options: Any valid string text
  OTHER: '&8[&cHeal&8] &7You healed &d%player%'
  # The text or value for Notify. Available options: Any valid string text
  NOTIFY: '&8[&cHeal&8] &d%sender% &7restored your health'
# Configuration section for Feed.
```

---
## Section: `FEED`

### 1. Commented Setup Code Example

```yaml
FEED:
  # The text or value for Self. Available options: Any valid string text
  SELF: '&7Your hunger has been satisfied!'
  # The text or value for Other. Available options: Any valid string text
  OTHER: '&8[&6Feed&8] &7You fed &d%player%'
  # The text or value for Notify. Available options: Any valid string text
  NOTIFY: '&8[&6Feed&8] &d%sender% &7restored your hunger'
# Configuration section for Gamemode.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `FEED.SELF` | `str` | Any string text | `'&7Your hunger has been satisfied!'` | Shown when feeding yourself. |
| `FEED.OTHER` | `str` | Any string text | `'&8[&6Feed&8] &7You fed &d%player%'` | Shown to the staff member who fed somebody else. |
| `FEED.NOTIFY` | `str` | Any string text | `'&8[&6Feed&8] &d%sender% &7restored ...'` | Shown to the player who was fed. |

The feed command, arranged the same way as heal.

### 3. Practical Setup Example

```yaml
FEED:
  # The text or value for Self. Available options: Any valid string text
  SELF: '&7Your hunger has been satisfied!'
  # The text or value for Other. Available options: Any valid string text
  OTHER: '&8[&6Feed&8] &7You fed &d%player%'
  # The text or value for Notify. Available options: Any valid string text
  NOTIFY: '&8[&6Feed&8] &d%sender% &7restored your hunger'
# Configuration section for Gamemode.
```

---
## Section: `GAMEMODE`

### 1. Commented Setup Code Example

```yaml
GAMEMODE:
  # The text or value for Message. Available options: Any valid string text
  MESSAGE: '&d%player% &7is now in &e%mode% mode'
  # The text or value for Target Message. Available options: Any valid string text
  TARGET_MESSAGE: '&7Your gamemode has been changed to &e%mode% &7by &d%sender%'
  # The text or value for No Permission. Available options: Any valid string text
  NO_PERMISSION: '&cYou do not have permission.'
  # The text or value for No Permission Others. Available options: Any valid string text
  NO_PERMISSION_OTHERS: '&cYou do not have permission to change other players'' gamemode.'
  # The text or value for Player Only. Available options: Any valid string text
  PLAYER_ONLY: '&cOnly players can use this command without a target.'
  # The text or value for Player Not Online. Available options: Any valid string text
  PLAYER_NOT_ONLINE: '&cPlayer not online.'
  # The text or value for Invalid Mode. Available options: Any valid string text
  INVALID_MODE: '&cInvalid gamemode. Use survival, creative, adventure, or spectator.'
  # The text or value for Usage. Available options: Any valid string text
  USAGE: '&cUsage: /%label% <survival|creative|adventure|spectator> [player]'
  # The text or value for Usage Short. Available options: Any valid string text
  USAGE_SHORT: '&cUsage: /%label% [player]'
  # The text or value for Disabled. Available options: Any valid string text
  DISABLED: '&cGamemode commands are currently disabled.'
# Configuration section for Randomtp.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `GAMEMODE.MESSAGE` | `str` | Any string text | `'&d%player% &7is now in &e%mode% mod...'` | The confirmation, naming the `%player%` and the `%mode%`. |
| `GAMEMODE.TARGET_MESSAGE` | `str` | Any string text | `'&7Your gamemode has been changed to...'` | Shown to a player whose mode somebody else changed. |
| `GAMEMODE.NO_PERMISSION` | `str` | Any string text | `'&cYou do not have permission.'` | Sent when they may not change their own mode. |
| `GAMEMODE.NO_PERMISSION_OTHERS` | `str` | Any string text | `'&cYou do not have permission to cha...'` | Sent when they may not change other people's. |
| `GAMEMODE.PLAYER_ONLY` | `str` | Any string text | `'&cOnly players can use this command...'` | Sent when the console runs it without naming a target. |
| `GAMEMODE.PLAYER_NOT_ONLINE` | `str` | Any string text | `'&cPlayer not online.'` | Sent when the named target is not online. |
| `GAMEMODE.INVALID_MODE` | `str` | Any string text | `'&cInvalid gamemode. Use survival, c...'` | Sent when the mode is not one of the four. |
| `GAMEMODE.USAGE` | `str` | Any string text | `'&cUsage: /%label% <survival\|creativ...'` | The usage line for the general command. `%label%` is the alias actually typed, so aliases show their own name. |
| `GAMEMODE.USAGE_SHORT` | `str` | Any string text | `'&cUsage: /%label% [player]'` | The usage line for the per-mode aliases, which take only a player. |
| `GAMEMODE.DISABLED` | `str` | Any string text | `'&cGamemode commands are currently d...'` | Sent when the gamemode commands are switched off. |

The staff gamemode commands.

### 3. Practical Setup Example

```yaml
GAMEMODE:
  # The text or value for Message. Available options: Any valid string text
  MESSAGE: '&d%player% &7is now in &e%mode% mode'
  # The text or value for Target Message. Available options: Any valid string text
  TARGET_MESSAGE: '&7Your gamemode has been changed to &e%mode% &7by &d%sender%'
  # The text or value for No Permission. Available options: Any valid string text
  NO_PERMISSION: '&cYou do not have permission.'
  # The text or value for No Permission Others. Available options: Any valid string text
  NO_PERMISSION_OTHERS: '&cYou do not have permission to change other players'' gamemode.'
  # The text or value for Player Only. Available options: Any valid string text
  PLAYER_ONLY: '&cOnly players can use this command without a target.'
  # The text or value for Player Not Online. Available options: Any valid string text
  PLAYER_NOT_ONLINE: '&cPlayer not online.'
  # The text or value for Invalid Mode. Available options: Any valid string text
  INVALID_MODE: '&cInvalid gamemode. Use survival, creative, adventure, or spectator.'
  # The text or value for Usage. Available options: Any valid string text
  USAGE: '&cUsage: /%label% <survival|creative|adventure|spectator> [player]'
  # The text or value for Usage Short. Available options: Any valid string text
  USAGE_SHORT: '&cUsage: /%label% [player]'
  # The text or value for Disabled. Available options: Any valid string text
  DISABLED: '&cGamemode commands are currently disabled.'
# Configuration section for Randomtp.
```

---
## Section: `RANDOMTP`

### 1. Commented Setup Code Example

```yaml
RANDOMTP:
  # The text or value for Success. Available options: Any valid string text
  SUCCESS: '&8[&dRTP&8] &7You were teleported to &e%player%'
  # The text or value for Notify. Available options: Any valid string text
  NOTIFY: '&8[&dRTP&8] &e%player% &7appeared near you!'
  # The text or value for No Players. Available options: Any valid string text
  NO_PLAYERS: '&c✖ &7No other players available for random teleport'
# Configuration section for Rename.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `RANDOMTP.SUCCESS` | `str` | Any string text | `'&8[&dRTP&8] &7You were teleported t...'` | Shown to the player who teleported, naming who they landed on. |
| `RANDOMTP.NOTIFY` | `str` | Any string text | `'&8[&dRTP&8] &e%player% &7appeared n...'` | Shown to the player who was teleported to. |
| `RANDOMTP.NO_PLAYERS` | `str` | Any string text | `'&c✖ &7No other players available fo...'` | Sent when nobody else is online to pick. |

Teleporting to a random online player, which is separate from `/rtp` to a random location.

### 3. Practical Setup Example

```yaml
RANDOMTP:
  # The text or value for Success. Available options: Any valid string text
  SUCCESS: '&8[&dRTP&8] &7You were teleported to &e%player%'
  # The text or value for Notify. Available options: Any valid string text
  NOTIFY: '&8[&dRTP&8] &e%player% &7appeared near you!'
  # The text or value for No Players. Available options: Any valid string text
  NO_PLAYERS: '&c✖ &7No other players available for random teleport'
# Configuration section for Rename.
```

---
## Section: `RENAME`

### 1. Commented Setup Code Example

```yaml
RENAME:
  # The text or value for No Item. Available options: Any valid string text
  NO_ITEM: '&c✖ &7You must hold an item to rename it'
  # The text or value for Meta Error. Available options: Any valid string text
  META_ERROR: '&c⚠ &7This item cannot be renamed'
  # The text or value for Staffmode Blocked. Available options: Any valid string text
  STAFFMODE_BLOCKED: '&c✖ &7Staff mode items cannot be renamed'
  # The text or value for Reset Success. Available options: Any valid string text
  RESET_SUCCESS: '&8[&aRename&8] &7Item name has been reset'
  # The text or value for Success. Available options: Any valid string text
  SUCCESS: '&8[&aRename&8] &7New name: &f%name%'
# Configuration section for Ping.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `RENAME.NO_ITEM` | `str` | Any string text | `'&c✖ &7You must hold an item to rena...'` | Sent when their hand is empty. |
| `RENAME.META_ERROR` | `str` | Any string text | `'&c⚠ &7This item cannot be renamed'` | Sent when the item cannot carry a name. |
| `RENAME.STAFFMODE_BLOCKED` | `str` | Any string text | `'&c✖ &7Staff mode items cannot be re...'` | Sent when the item is a staff mode tool, which is protected so the toolbar cannot be disguised. |
| `RENAME.RESET_SUCCESS` | `str` | Any string text | `'&8[&aRename&8] &7Item name has been...'` | Confirmation after clearing a custom name. |
| `RENAME.SUCCESS` | `str` | Any string text | `'&8[&aRename&8] &7New name: &f%name%'` | Confirmation after setting one, echoing the `%name%`. |

Renaming a held item.

### 3. Practical Setup Example

```yaml
RENAME:
  # The text or value for No Item. Available options: Any valid string text
  NO_ITEM: '&c✖ &7You must hold an item to rename it'
  # The text or value for Meta Error. Available options: Any valid string text
  META_ERROR: '&c⚠ &7This item cannot be renamed'
  # The text or value for Staffmode Blocked. Available options: Any valid string text
  STAFFMODE_BLOCKED: '&c✖ &7Staff mode items cannot be renamed'
  # The text or value for Reset Success. Available options: Any valid string text
  RESET_SUCCESS: '&8[&aRename&8] &7Item name has been reset'
  # The text or value for Success. Available options: Any valid string text
  SUCCESS: '&8[&aRename&8] &7New name: &f%name%'
# Configuration section for Ping.
```

---
## Section: `PING`

### 1. Commented Setup Code Example

```yaml
PING:
  # The text or value for Self. Available options: Any valid string text
  SELF: '&7Your ping is &b%ping%ms'
  # The text or value for Other. Available options: Any valid string text
  OTHER: '&e%player%''s &7ping is &b%ping%ms'
# Configuration section for Playtime.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `PING.SELF` | `str` | Any string text | `'&7Your ping is &b%ping%ms'` | Reply when checking your own ping. |
| `PING.OTHER` | `str` | Any string text | `'&e%player%'s &7ping is &b%ping%ms'` | Reply when checking somebody else's. |

The ping command.

### 3. Practical Setup Example

```yaml
PING:
  # The text or value for Self. Available options: Any valid string text
  SELF: '&7Your ping is &b%ping%ms'
  # The text or value for Other. Available options: Any valid string text
  OTHER: '&e%player%''s &7ping is &b%ping%ms'
# Configuration section for Playtime.
```

---
## Section: `PLAYTIME`

### 1. Commented Setup Code Example

```yaml
PLAYTIME:
  # The text or value for Message. Available options: Any valid string text
  MESSAGE: '&a%time% &eplaying on &a%server%'
  # The text or value for Other. Available options: Any valid string text
  OTHER: '&e%player% &7has played for &a%time% &7on &a%server%'
# Configuration section for Staffchat.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `PLAYTIME.MESSAGE` | `str` | Any string text | `'&a%time% &eplaying on &a%server%'` | Reply when checking your own playtime. |
| `PLAYTIME.OTHER` | `str` | Any string text | `'&e%player% &7has played for &a%time...'` | Reply when checking somebody else's. |

The playtime command. `%server%` lets a network name each server in the reply.

### 3. Practical Setup Example

```yaml
PLAYTIME:
  # The text or value for Message. Available options: Any valid string text
  MESSAGE: '&a%time% &eplaying on &a%server%'
  # The text or value for Other. Available options: Any valid string text
  OTHER: '&e%player% &7has played for &a%time% &7on &a%server%'
# Configuration section for Staffchat.
```

---
## Section: `STAFFCHAT`

### 1. Commented Setup Code Example

```yaml
STAFFCHAT:
  # The text or value for No Permission. Available options: Any valid string text
  NO_PERMISSION: '&cYou do not have permission to use staff chat.'
  # The text or value for Usage. Available options: Any valid string text
  USAGE: '&cUsage: /staffchat <message>'
  # The text or value for Format. Available options: Any valid string text
  FORMAT: '&8[&6StaffChat&8] &e%player%&7: %message%'
  # The text or value for Disabled. Available options: Any valid string text
  DISABLED: '&cNetwork staff chat is currently disabled.'
  # The text or value for Message Too Long. Available options: Any valid string text
  MESSAGE_TOO_LONG: '&cStaff chat message is too long. Max: %max% characters.'
  # The text or value for Redis Unavailable. Available options: Any valid string text
  REDIS_UNAVAILABLE: '&eStaff chat was delivered locally, but Redis is unavailable
    for cross-server delivery.'
# Configuration section for Helpop.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `STAFFCHAT.NO_PERMISSION` | `str` | Any string text | `'&cYou do not have permission to use...'` | Sent when somebody without the node tries to use it. |
| `STAFFCHAT.USAGE` | `str` | Any string text | `'&cUsage: /staffchat <message>'` | The usage line. |
| `STAFFCHAT.FORMAT` | `str` | Any string text | `'&8[&6StaffChat&8] &e%player%&7: %me...'` | How a staff chat line is rendered, with `%player%` and `%message%`. |
| `STAFFCHAT.DISABLED` | `str` | Any string text | `'&cNetwork staff chat is currently d...'` | Sent when network staff chat is switched off. |
| `STAFFCHAT.MESSAGE_TOO_LONG` | `str` | Any string text | `'&cStaff chat message is too long. M...'` | Sent when the message passes the configured limit, quoted as `%max%`. |
| `STAFFCHAT.REDIS_UNAVAILABLE` | `str` | Any string text | `'&eStaff chat was delivered locally,...'` | Warns the sender that their message reached this server but not the others. Whether it is sent at all is `NETWORK.SEND_LOCAL_FALLBACK_ON_REDIS_ERROR` in `network.yml`, and each player sees it once per session. |

The staff chat channel, which spans servers over Redis when networking is on.

### 3. Practical Setup Example

```yaml
STAFFCHAT:
  # The text or value for No Permission. Available options: Any valid string text
  NO_PERMISSION: '&cYou do not have permission to use staff chat.'
  # The text or value for Usage. Available options: Any valid string text
  USAGE: '&cUsage: /staffchat <message>'
  # The text or value for Format. Available options: Any valid string text
  FORMAT: '&8[&6StaffChat&8] &e%player%&7: %message%'
  # The text or value for Disabled. Available options: Any valid string text
  DISABLED: '&cNetwork staff chat is currently disabled.'
  # The text or value for Message Too Long. Available options: Any valid string text
  MESSAGE_TOO_LONG: '&cStaff chat message is too long. Max: %max% characters.'
  # The text or value for Redis Unavailable. Available options: Any valid string text
  REDIS_UNAVAILABLE: '&eStaff chat was delivered locally, but Redis is unavailable
    for cross-server delivery.'
# Configuration section for Helpop.
```

---
## Section: `HELPOP`

### 1. Commented Setup Code Example

```yaml
HELPOP:
  # The text or value for No Permission. Available options: Any valid string text
  NO_PERMISSION: '&cYou do not have permission to request staff assistance.'
  # The text or value for Player Only. Available options: Any valid string text
  PLAYER_ONLY: '&cOnly players can use helpop.'
  # The text or value for Usage. Available options: Any valid string text
  USAGE: '&cUsage: /helpop <message>'
  # The text or value for Disabled. Available options: Any valid string text
  DISABLED: '&cHelpop is currently disabled.'
  # The text or value for Message Too Long. Available options: Any valid string text
  MESSAGE_TOO_LONG: '&cYour request is too long. Max: %max% characters.'
  # The text or value for Cooldown. Available options: Any valid string text
  COOLDOWN: '&cPlease wait %seconds%s before using helpop again.'
  # The text or value for Redis Unavailable. Available options: Any valid string text
  REDIS_UNAVAILABLE: '&eYour request was delivered locally, but Redis is unavailable
    for cross-server delivery.'
  # Configuration section for Format.
  FORMAT:
  - ''
  - '&9[request] &7[%server%] &a%player% &bhas requested assistance'
  - '     &9reason: &b%message%'
  - ''
  # The text or value for Confirmation. Available options: Any valid string text
  CONFIRMATION: '&aYour request has been sent to all staff members.'
# Configuration section for Report.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `HELPOP.NO_PERMISSION` | `str` | Any string text | `'&cYou do not have permission to req...'` | Sent when the player may not ask for help. |
| `HELPOP.PLAYER_ONLY` | `str` | Any string text | `'&cOnly players can use helpop.'` | Sent when the console tries to use it. |
| `HELPOP.USAGE` | `str` | Any string text | `'&cUsage: /helpop <message>'` | The usage line. |
| `HELPOP.DISABLED` | `str` | Any string text | `'&cHelpop is currently disabled.'` | Sent when helpop is switched off. |
| `HELPOP.MESSAGE_TOO_LONG` | `str` | Any string text | `'&cYour request is too long. Max: %m...'` | Sent when the request exceeds `%max%` characters. |
| `HELPOP.COOLDOWN` | `str` | Any string text | `'&cPlease wait %seconds%s before usi...'` | Sent when asking again too soon, with `%seconds%` left. |
| `HELPOP.REDIS_UNAVAILABLE` | `str` | Any string text | `'&eYour request was delivered locall...'` | Warns that the request reached local staff but not the other servers. Controlled by `NETWORK.STAFF_ALERTS_WARN_SENDER_ON_REDIS_ERROR`, which ships off. |
| `HELPOP.FORMAT` | `list` | Lines of text | `['', '&9[request] &7[%server%] &a%player% &bhas...]` | The block staff see, with `%server%`, `%player%` and `%message%`. |
| `HELPOP.CONFIRMATION` | `str` | Any string text | `'&aYour request has been sent to all...'` | The acknowledgement back to the player who asked. |

Player requests for staff help.

### 3. Practical Setup Example

```yaml
HELPOP:
  # The text or value for No Permission. Available options: Any valid string text
  NO_PERMISSION: '&cYou do not have permission to request staff assistance.'
  # The text or value for Player Only. Available options: Any valid string text
  PLAYER_ONLY: '&cOnly players can use helpop.'
  # The text or value for Usage. Available options: Any valid string text
  USAGE: '&cUsage: /helpop <message>'
  # The text or value for Disabled. Available options: Any valid string text
  DISABLED: '&cHelpop is currently disabled.'
  # The text or value for Message Too Long. Available options: Any valid string text
  MESSAGE_TOO_LONG: '&cYour request is too long. Max: %max% characters.'
  # The text or value for Cooldown. Available options: Any valid string text
  COOLDOWN: '&cPlease wait %seconds%s before using helpop again.'
  # The text or value for Redis Unavailable. Available options: Any valid string text
  REDIS_UNAVAILABLE: '&eYour request was delivered locally, but Redis is unavailable
    for cross-server delivery.'
  # Configuration section for Format.
  FORMAT:
  - ''
  - '&9[request] &7[%server%] &a%player% &bhas requested assistance'
  - '     &9reason: &b%message%'
  - ''
  # The text or value for Confirmation. Available options: Any valid string text
  CONFIRMATION: '&aYour request has been sent to all staff members.'
# Configuration section for Report.
```

---
## Section: `REPORT`

### 1. Commented Setup Code Example

```yaml
REPORT:
  # The text or value for No Permission. Available options: Any valid string text
  NO_PERMISSION: '&cYou do not have permission to report players.'
  # The text or value for Player Only. Available options: Any valid string text
  PLAYER_ONLY: '&cOnly players can use report.'
  # The text or value for Usage. Available options: Any valid string text
  USAGE: '&cUsage: /report <player> <reason>'
  # The text or value for Disabled. Available options: Any valid string text
  DISABLED: '&cReports are currently disabled.'
  # The text or value for Player Not Found. Available options: Any valid string text
  PLAYER_NOT_FOUND: '&cPlayer not found.'
  # The text or value for Cannot Report Self. Available options: Any valid string text
  CANNOT_REPORT_SELF: '&cYou can''t report yourself!'
  # The text or value for Message Too Long. Available options: Any valid string text
  MESSAGE_TOO_LONG: '&cYour report reason is too long. Max: %max% characters.'
  # The text or value for Cooldown. Available options: Any valid string text
  COOLDOWN: '&cPlease wait %seconds%s before reporting again.'
  # The text or value for Redis Unavailable. Available options: Any valid string text
  REDIS_UNAVAILABLE: '&eYour report was delivered locally, but Redis is unavailable
    for cross-server delivery.'
  # Configuration section for Format.
  FORMAT:
  - ''
  - '&9[report] &7[%server%] &c%reported% &bhas been reported by &a%reporter%'
  - '     &9reason: &b%reason%'
  - ''
  # The text or value for Confirmation. Available options: Any valid string text
  CONFIRMATION: '&aYour report has been sent to all staff members.'
# Configuration section for Alts.
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `REPORT.NO_PERMISSION` | `str` | Any string text | `'&cYou do not have permission to rep...'` | Sent when the player may not report. |
| `REPORT.PLAYER_ONLY` | `str` | Any string text | `'&cOnly players can use report.'` | Sent when the console tries to use it. |
| `REPORT.USAGE` | `str` | Any string text | `'&cUsage: /report <player> <reason>'` | The usage line. |
| `REPORT.DISABLED` | `str` | Any string text | `'&cReports are currently disabled.'` | Sent when reports are switched off. |
| `REPORT.PLAYER_NOT_FOUND` | `str` | Any string text | `'&cPlayer not found.'` | Sent when the reported name is not known. |
| `REPORT.CANNOT_REPORT_SELF` | `str` | Any string text | `'&cYou can't report yourself!'` | Sent when somebody reports themselves. |
| `REPORT.MESSAGE_TOO_LONG` | `str` | Any string text | `'&cYour report reason is too long. M...'` | Sent when the reason exceeds `%max%` characters. |
| `REPORT.COOLDOWN` | `str` | Any string text | `'&cPlease wait %seconds%s before rep...'` | Sent when reporting again too soon. |
| `REPORT.REDIS_UNAVAILABLE` | `str` | Any string text | `'&eYour report was delivered locally...'` | Warns that the report reached local staff but not the other servers. |
| `REPORT.FORMAT` | `list` | Lines of text | `['', '&9[report] &7[%server%] &c%reported% &bha...]` | The block staff see, with `%reported%`, `%reporter%` and `%reason%`. |
| `REPORT.CONFIRMATION` | `str` | Any string text | `'&aYour report has been sent to all ...'` | The acknowledgement back to the reporter. |

Player reports, arranged like helpop but naming a target.

### 3. Practical Setup Example

```yaml
REPORT:
  # The text or value for No Permission. Available options: Any valid string text
  NO_PERMISSION: '&cYou do not have permission to report players.'
  # The text or value for Player Only. Available options: Any valid string text
  PLAYER_ONLY: '&cOnly players can use report.'
  # The text or value for Usage. Available options: Any valid string text
  USAGE: '&cUsage: /report <player> <reason>'
  # The text or value for Disabled. Available options: Any valid string text
  DISABLED: '&cReports are currently disabled.'
  # The text or value for Player Not Found. Available options: Any valid string text
  PLAYER_NOT_FOUND: '&cPlayer not found.'
  # The text or value for Cannot Report Self. Available options: Any valid string text
  CANNOT_REPORT_SELF: '&cYou can''t report yourself!'
  # The text or value for Message Too Long. Available options: Any valid string text
  MESSAGE_TOO_LONG: '&cYour report reason is too long. Max: %max% characters.'
  # The text or value for Cooldown. Available options: Any valid string text
  COOLDOWN: '&cPlease wait %seconds%s before reporting again.'
  # The text or value for Redis Unavailable. Available options: Any valid string text
  REDIS_UNAVAILABLE: '&eYour report was delivered locally, but Redis is unavailable
    for cross-server delivery.'
  # Configuration section for Format.
  FORMAT:
  - ''
  - '&9[report] &7[%server%] &c%reported% &bhas been reported by &a%reporter%'
  - '     &9reason: &b%reason%'
  - ''
  # The text or value for Confirmation. Available options: Any valid string text
  CONFIRMATION: '&aYour report has been sent to all staff members.'
# Configuration section for Alts.
```

---
## Section: `ALTS`

### 1. Commented Setup Code Example

```yaml
ALTS:
  # The text or value for Usage. Available options: Any valid string text
  USAGE: '&cUsage: /alts <player>'
  # The text or value for Not Found. Available options: Any valid string text
  NOT_FOUND: '&cPlayer not found.'
  # The text or value for No Data. Available options: Any valid string text
  NO_DATA: '&cNo IP history found for that player.'
  # The text or value for None. Available options: Any valid string text
  NONE: '&7No alternate accounts found.'
  # The text or value for Header. Available options: Any valid string text
  HEADER: '&8[&6Alts&8] &e%player%'
  # The text or value for Known Ips. Available options: Any valid string text
  KNOWN_IPS: '&7Known IPs: &f%ips%'
  # The text or value for Entry. Available options: Any valid string text
  ENTRY: '&8- &e%player% &7[%status%&7] &fshared: %ips%'
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `ALTS.USAGE` | `str` | Any string text | `'&cUsage: /alts <player>'` | The usage line. |
| `ALTS.NOT_FOUND` | `str` | Any string text | `'&cPlayer not found.'` | Sent when the name is not known. |
| `ALTS.NO_DATA` | `str` | Any string text | `'&cNo IP history found for that play...'` | Sent when the player is known but has no recorded IP history to match on. |
| `ALTS.NONE` | `str` | Any string text | `'&7No alternate accounts found.'` | Sent when the history exists but nobody else shares it. |
| `ALTS.HEADER` | `str` | Any string text | `'&8[&6Alts&8] &e%player%'` | The heading above the results. |
| `ALTS.KNOWN_IPS` | `str` | Any string text | `'&7Known IPs: &f%ips%'` | The line listing the addresses the lookup matched on. |
| `ALTS.ENTRY` | `str` | Any string text | `'&8- &e%player% &7[%status%&7] &fsha...'` | One result row, repeated per match, with `%status%` showing whether that account is banned. |

The alt account lookup, which matches players by shared IP history.

### 3. Practical Setup Example

```yaml
ALTS:
  # The text or value for Usage. Available options: Any valid string text
  USAGE: '&cUsage: /alts <player>'
  # The text or value for Not Found. Available options: Any valid string text
  NOT_FOUND: '&cPlayer not found.'
  # The text or value for No Data. Available options: Any valid string text
  NO_DATA: '&cNo IP history found for that player.'
  # The text or value for None. Available options: Any valid string text
  NONE: '&7No alternate accounts found.'
  # The text or value for Header. Available options: Any valid string text
  HEADER: '&8[&6Alts&8] &e%player%'
  # The text or value for Known Ips. Available options: Any valid string text
  KNOWN_IPS: '&7Known IPs: &f%ips%'
  # The text or value for Entry. Available options: Any valid string text
  ENTRY: '&8- &e%player% &7[%status%&7] &fshared: %ips%'
```

---
## Section: `VOICE-CHAT`

### 1. Commented Setup Code Example

```yaml
VOICE-CHAT:
  ACCEPTED: '&aVoice chat is now enabled for you.'
  DECLINED: '&cVoice chat will stay disabled.'
  REVOKED: '&cYou withdrew your voice chat consent. Voice chat is disabled again.'
  NOTHING-TO-REVOKE: '&cYou have not agreed to the voice chat policy yet.'
  PLAYERS-ONLY: '&cOnly players can use this command.'
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `VOICE-CHAT.ACCEPTED` | `str` | Any string text | `'&aVoice chat is now enabled for you...'` | Shown when they agree. |
| `VOICE-CHAT.DECLINED` | `str` | Any string text | `'&cVoice chat will stay disabled.'` | Shown when they decline. Voice chat stays off rather than being blocked permanently. |
| `VOICE-CHAT.REVOKED` | `str` | Any string text | `'&cYou withdrew your voice chat cons...'` | Shown when they withdraw consent later. |
| `VOICE-CHAT.NOTHING-TO-REVOKE` | `str` | Any string text | `'&cYou have not agreed to the voice ...'` | Sent when they try to withdraw without having agreed. |
| `VOICE-CHAT.PLAYERS-ONLY` | `str` | Any string text | `'&cOnly players can use this command...'` | Sent when the console tries to use the command. |

The consent prompt players answer before voice chat turns on for them.

### 3. Practical Setup Example

```yaml
VOICE-CHAT:
  ACCEPTED: '&aVoice chat is now enabled for you.'
  DECLINED: '&cVoice chat will stay disabled.'
  REVOKED: '&cYou withdrew your voice chat consent. Voice chat is disabled again.'
  NOTHING-TO-REVOKE: '&cYou have not agreed to the voice chat policy yet.'
  PLAYERS-ONLY: '&cOnly players can use this command.'
```

---
