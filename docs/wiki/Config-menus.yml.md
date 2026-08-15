# Detailed Configuration & Setup Guide: `menus.yml`

This is the official, 100% complete technical setup guide for `menus.yml` in **UltimateDonutSMP**.
Each section details the exact commented setup code block, allowed option values, data types, default values, and in-depth functional behavior.

---

## Section: `GLOBAL`

### 1. Commented Setup Code Example

```yaml
GLOBAL:
  PAGE-MENU:
    MATERIAL: ARROW
    NEXT-BUTTON: '&aNEXT'
    BACK-BUTTON: '&aBACK'
    FIRST-PAGE-BUTTON: '&aFIRST PAGE'
    LAST-PAGE-BUTTON: '&aLAST PAGE'
    NEXT-LORE:
    - '&fClick to go to the next page'
    BACK-LORE:
    - '&fClick to go to the previous page'
    FIRST-PAGE-LORE:
    - '&fJump to the first page'
    LAST-PAGE-LORE:
    - '&fJump to the last page'
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `GLOBAL.PAGE-MENU.MATERIAL` | `str` | Any string text | `'ARROW'` | Configures the technical `MATERIAL` parameter for `GLOBAL.PAGE-MENU.MATERIAL` in `menus.yml`. |
| `GLOBAL.PAGE-MENU.NEXT-BUTTON` | `str` | Any string text | `'&aNEXT'` | Configures the technical `NEXT-BUTTON` parameter for `GLOBAL.PAGE-MENU.NEXT-BUTTON` in `menus.yml`. |
| `GLOBAL.PAGE-MENU.BACK-BUTTON` | `str` | Any string text | `'&aBACK'` | Configures the technical `BACK-BUTTON` parameter for `GLOBAL.PAGE-MENU.BACK-BUTTON` in `menus.yml`. |
| `GLOBAL.PAGE-MENU.FIRST-PAGE-BUTTON` | `str` | Any string text | `'&aFIRST PAGE'` | Configures the technical `FIRST-PAGE-BUTTON` parameter for `GLOBAL.PAGE-MENU.FIRST-PAGE-BUTTON` in `menus.yml`. |
| `GLOBAL.PAGE-MENU.LAST-PAGE-BUTTON` | `str` | Any string text | `'&aLAST PAGE'` | Configures the technical `LAST-PAGE-BUTTON` parameter for `GLOBAL.PAGE-MENU.LAST-PAGE-BUTTON` in `menus.yml`. |
| `GLOBAL.PAGE-MENU.NEXT-LORE` | `list` | List of configured items/strings | `['&fClick to go to the next page']` | Configures the technical `NEXT-LORE` parameter for `GLOBAL.PAGE-MENU.NEXT-LORE` in `menus.yml`. |
| `GLOBAL.PAGE-MENU.BACK-LORE` | `list` | List of configured items/strings | `['&fClick to go to the previous page']` | Configures the technical `BACK-LORE` parameter for `GLOBAL.PAGE-MENU.BACK-LORE` in `menus.yml`. |
| `GLOBAL.PAGE-MENU.FIRST-PAGE-LORE` | `list` | List of configured items/strings | `['&fJump to the first page']` | Configures the technical `FIRST-PAGE-LORE` parameter for `GLOBAL.PAGE-MENU.FIRST-PAGE-LORE` in `menus.yml`. |
| `GLOBAL.PAGE-MENU.LAST-PAGE-LORE` | `list` | List of configured items/strings | `['&fJump to the last page']` | Configures the technical `LAST-PAGE-LORE` parameter for `GLOBAL.PAGE-MENU.LAST-PAGE-LORE` in `menus.yml`. |

### 3. Practical Setup Example

```yaml
GLOBAL:
  PAGE-MENU:
    MATERIAL: ARROW
    NEXT-BUTTON: '&aNEXT'
    BACK-BUTTON: '&aBACK'
    FIRST-PAGE-BUTTON: '&aFIRST PAGE'
    LAST-PAGE-BUTTON: '&aLAST PAGE'
    NEXT-LORE:
    - '&fClick to go to the next page'
    BACK-LORE:
    - '&fClick to go to the previous page'
    FIRST-PAGE-LORE:
    - '&fJump to the first page'
    LAST-PAGE-LORE:
    - '&fJump to the last page'
```

---

## Section: `TEAM-MENUS`

### 1. Commented Setup Code Example

```yaml
TEAM-MENUS:
  TEAM:
    TITLE: '&8Team'
    SIZE: 54
    MAX-ITEMS-PER-PAGE: 45
    PLAYER-BUTTON:
      ONLINE-SYMBOL: "&a■"
      OFFLINE-SYMBOL: "&4■"
      LORE: '&fClick to edit'
    SEARCH-BUTTON:
      TITLE: '&#6BF18DSearch'
      MATERIAL: OAK_SIGN
      SLOT: 45
      LORE:
      - '&fSearch for team members'
      - '&cIn development.'
    SORT-BUTTON:
      TITLE: '&aSort'
      MATERIAL: HOPPER
      SLOT: 46
      SELECTED-PREFIX: '&a'
      UNSELECTED-PREFIX: '&f'
      SYMBOL: "▪"
    REFRESH-BUTTON:
      TITLE: '&#6BF18DTeam {team_name}'
      MATERIAL: IRON_HELMET
      SLOT: 49
      LORE:
      - '&fClick to refresh'
      - '&7Add up to {max_members} members'
    HOME-BUTTON:
      TITLE: '&#6BF18DTeam Home'
      MATERIAL: WHITE_BANNER
      SLOT: 52
      HOME-LORE: '&fClick to teleport to your team''s home'
      NO-HOME-LORE: '&fSet the team home with /home'
    PVP-BUTTON:
      TITLE: '&#6BF18DPVP'
      MATERIAL: IRON_SWORD
      SLOT: 53
      ON-STATE: '&a&lON'
      OFF-STATE: '&c&lOFF'
      LORE: '&fCurrently: {state}'
    MESSAGES:
      NOT-IN-TEAM: '&cYou are not part of the team.'
      NO-PERMISSION: '&cYou don''t have permissions to do this.'
      CANT-EDIT-SELF: '&cYou can''t do this yourself!'
  TEAM-EDIT-MEMBER:
    TITLE: '&8Edit {player}'
    SIZE: 27
    PLACEHOLDER: false
    PLACEHOLDER-MATERIAL: BLACK_STAINED_GLASS_PANE
    EDIT-HOME-BUTTON:
      TITLE: '&#6BF18DEdit Home'
      MATERIAL: WHITE_BANNER
      SLOT: 10
      ON-STATE: '&a&lON'
      OFF-STATE: '&c&lOFF'
      LORE:
      - '&fLet {player} set and remove the team home'
      - '&fCurrently: {state}'
    KICK-BUTTON:
      TITLE: '&#6BF18DKick'
      MATERIAL: OAK_DOOR
      SLOT: 11
      LORE:
      - '&fClick to kick {player}'
    MANAGE-TEAMMATES-BUTTON:
      TITLE: '&#6BF18DManage Teammates'
      MATERIAL: IRON_HELMET
      SLOT: 12
      ON-STATE: '&a&lON'
      OFF-STATE: '&c&lOFF'
      LORE:
      - '&fLet {player} invite and kick teammates'
      - '&fCurrently: {state}'
    PVP-BUTTON:
      TITLE: '&#6BF18DPVP'
      MATERIAL: IRON_SWORD
      SLOT: 13
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `TEAM-MENUS.TEAM.TITLE` | `str` | Any string text | `'&8Team'` | Configures the technical `TITLE` parameter for `TEAM-MENUS.TEAM.TITLE` in `menus.yml`. |
| `TEAM-MENUS.TEAM.SIZE` | `int` | Any valid integer number | `'54'` | Configures the technical `SIZE` parameter for `TEAM-MENUS.TEAM.SIZE` in `menus.yml`. |
| `TEAM-MENUS.TEAM.MAX-ITEMS-PER-PAGE` | `int` | Any valid integer number | `'45'` | Configures the technical `MAX-ITEMS-PER-PAGE` parameter for `TEAM-MENUS.TEAM.MAX-ITEMS-PER-PAGE` in `menus.yml`. |
| `TEAM-MENUS.TEAM.PLAYER-BUTTON.ONLINE-SYMBOL` | `str` | Any string text | `'&a■'` | Configures the technical `ONLINE-SYMBOL` parameter for `TEAM-MENUS.TEAM.PLAYER-BUTTON.ONLINE-SYMBOL` in `menus.yml`. |
| `TEAM-MENUS.TEAM.PLAYER-BUTTON.OFFLINE-SYMBOL` | `str` | Any string text | `'&4■'` | Configures the technical `OFFLINE-SYMBOL` parameter for `TEAM-MENUS.TEAM.PLAYER-BUTTON.OFFLINE-SYMBOL` in `menus.yml`. |
| `TEAM-MENUS.TEAM.PLAYER-BUTTON.LORE` | `str` | Any string text | `'&fClick to edit'` | Configures the technical `LORE` parameter for `TEAM-MENUS.TEAM.PLAYER-BUTTON.LORE` in `menus.yml`. |
| `TEAM-MENUS.TEAM.SEARCH-BUTTON.TITLE` | `str` | Any string text | `'&#6BF18DSearch'` | Configures the technical `TITLE` parameter for `TEAM-MENUS.TEAM.SEARCH-BUTTON.TITLE` in `menus.yml`. |
| `TEAM-MENUS.TEAM.SEARCH-BUTTON.MATERIAL` | `str` | Any string text | `'OAK_SIGN'` | Configures the technical `MATERIAL` parameter for `TEAM-MENUS.TEAM.SEARCH-BUTTON.MATERIAL` in `menus.yml`. |
| `TEAM-MENUS.TEAM.SEARCH-BUTTON.SLOT` | `int` | Any valid integer number | `'45'` | Configures the technical `SLOT` parameter for `TEAM-MENUS.TEAM.SEARCH-BUTTON.SLOT` in `menus.yml`. |
| `TEAM-MENUS.TEAM.SEARCH-BUTTON.LORE` | `list` | List of configured items/strings | `['&fSearch for team members', '&cIn development.']` | Configures the technical `LORE` parameter for `TEAM-MENUS.TEAM.SEARCH-BUTTON.LORE` in `menus.yml`. |
| `TEAM-MENUS.TEAM.SORT-BUTTON.TITLE` | `str` | Any string text | `'&aSort'` | Configures the technical `TITLE` parameter for `TEAM-MENUS.TEAM.SORT-BUTTON.TITLE` in `menus.yml`. |
| `TEAM-MENUS.TEAM.SORT-BUTTON.MATERIAL` | `str` | Any string text | `'HOPPER'` | Configures the technical `MATERIAL` parameter for `TEAM-MENUS.TEAM.SORT-BUTTON.MATERIAL` in `menus.yml`. |
| `TEAM-MENUS.TEAM.SORT-BUTTON.SLOT` | `int` | Any valid integer number | `'46'` | Configures the technical `SLOT` parameter for `TEAM-MENUS.TEAM.SORT-BUTTON.SLOT` in `menus.yml`. |
| `TEAM-MENUS.TEAM.SORT-BUTTON.SELECTED-PREFIX` | `str` | Any string text | `'&a'` | Configures the technical `SELECTED-PREFIX` parameter for `TEAM-MENUS.TEAM.SORT-BUTTON.SELECTED-PREFIX` in `menus.yml`. |
| `TEAM-MENUS.TEAM.SORT-BUTTON.UNSELECTED-PREFIX` | `str` | Any string text | `'&f'` | Configures the technical `UNSELECTED-PREFIX` parameter for `TEAM-MENUS.TEAM.SORT-BUTTON.UNSELECTED-PREFIX` in `menus.yml`. |
| `TEAM-MENUS.TEAM.SORT-BUTTON.SYMBOL` | `str` | Any string text | `'▪'` | Configures the technical `SYMBOL` parameter for `TEAM-MENUS.TEAM.SORT-BUTTON.SYMBOL` in `menus.yml`. |
| `TEAM-MENUS.TEAM.REFRESH-BUTTON.TITLE` | `str` | Any string text | `'&#6BF18DTeam {team_name}'` | Configures the technical `TITLE` parameter for `TEAM-MENUS.TEAM.REFRESH-BUTTON.TITLE` in `menus.yml`. |
| `TEAM-MENUS.TEAM.REFRESH-BUTTON.MATERIAL` | `str` | Any string text | `'IRON_HELMET'` | Configures the technical `MATERIAL` parameter for `TEAM-MENUS.TEAM.REFRESH-BUTTON.MATERIAL` in `menus.yml`. |
| `TEAM-MENUS.TEAM.REFRESH-BUTTON.SLOT` | `int` | Any valid integer number | `'49'` | Configures the technical `SLOT` parameter for `TEAM-MENUS.TEAM.REFRESH-BUTTON.SLOT` in `menus.yml`. |
| `TEAM-MENUS.TEAM.REFRESH-BUTTON.LORE` | `list` | List of configured items/strings | `['&fClick to refresh', '&7Add up to {max_members} members']` | Configures the technical `LORE` parameter for `TEAM-MENUS.TEAM.REFRESH-BUTTON.LORE` in `menus.yml`. |
| `TEAM-MENUS.TEAM.HOME-BUTTON.TITLE` | `str` | Any string text | `'&#6BF18DTeam Home'` | Configures the technical `TITLE` parameter for `TEAM-MENUS.TEAM.HOME-BUTTON.TITLE` in `menus.yml`. |
| `TEAM-MENUS.TEAM.HOME-BUTTON.MATERIAL` | `str` | Any string text | `'WHITE_BANNER'` | Configures the technical `MATERIAL` parameter for `TEAM-MENUS.TEAM.HOME-BUTTON.MATERIAL` in `menus.yml`. |
| `TEAM-MENUS.TEAM.HOME-BUTTON.SLOT` | `int` | Any valid integer number | `'52'` | Configures the technical `SLOT` parameter for `TEAM-MENUS.TEAM.HOME-BUTTON.SLOT` in `menus.yml`. |
| `TEAM-MENUS.TEAM.HOME-BUTTON.HOME-LORE` | `str` | Any string text | `'&fClick to teleport to your team's ...'` | Configures the technical `HOME-LORE` parameter for `TEAM-MENUS.TEAM.HOME-BUTTON.HOME-LORE` in `menus.yml`. |
| `TEAM-MENUS.TEAM.HOME-BUTTON.NO-HOME-LORE` | `str` | Any string text | `'&fSet the team home with /home'` | Configures the technical `NO-HOME-LORE` parameter for `TEAM-MENUS.TEAM.HOME-BUTTON.NO-HOME-LORE` in `menus.yml`. |
| `TEAM-MENUS.TEAM.PVP-BUTTON.TITLE` | `str` | Any string text | `'&#6BF18DPVP'` | Configures the technical `TITLE` parameter for `TEAM-MENUS.TEAM.PVP-BUTTON.TITLE` in `menus.yml`. |
| `TEAM-MENUS.TEAM.PVP-BUTTON.MATERIAL` | `str` | Any string text | `'IRON_SWORD'` | Configures the technical `MATERIAL` parameter for `TEAM-MENUS.TEAM.PVP-BUTTON.MATERIAL` in `menus.yml`. |
| `TEAM-MENUS.TEAM.PVP-BUTTON.SLOT` | `int` | Any valid integer number | `'53'` | Configures the technical `SLOT` parameter for `TEAM-MENUS.TEAM.PVP-BUTTON.SLOT` in `menus.yml`. |
| `TEAM-MENUS.TEAM.PVP-BUTTON.ON-STATE` | `str` | Any string text | `'&a&lON'` | Configures the technical `ON-STATE` parameter for `TEAM-MENUS.TEAM.PVP-BUTTON.ON-STATE` in `menus.yml`. |
| `TEAM-MENUS.TEAM.PVP-BUTTON.OFF-STATE` | `str` | Any string text | `'&c&lOFF'` | Configures the technical `OFF-STATE` parameter for `TEAM-MENUS.TEAM.PVP-BUTTON.OFF-STATE` in `menus.yml`. |
| *(68 additional sub-keys configured in section)* | | | | |

### 3. Practical Setup Example

```yaml
TEAM-MENUS:
  TEAM:
    TITLE: '&8Team'
    SIZE: 54
    MAX-ITEMS-PER-PAGE: 45
    PLAYER-BUTTON:
      ONLINE-SYMBOL: "&a■"
      OFFLINE-SYMBOL: "&4■"
      LORE: '&fClick to edit'
    SEARCH-BUTTON:
      TITLE: '&#6BF18DSearch'
      MATERIAL: OAK_SIGN
      SLOT: 45
      LORE:
      - '&fSearch for team members'
      - '&cIn development.'
    SORT-BUTTON:
      TITLE: '&aSort'
      MATERIAL: HOPPER
      SLOT: 46
      SELECTED-PREFIX: '&a'
      UNSELECTED-PREFIX: '&f'
      SYMBOL: "▪"
    REFRESH-BUTTON:
      TITLE: '&#6BF18DTeam {team_name}'
      MATERIAL: IRON_HELMET
      SLOT: 49
      LORE:
      - '&fClick to refresh'
      - '&7Add up to {max_members} members'
    HOME-BUTTON:
      TITLE: '&#6BF18DTeam Home'
      MATERIAL: WHITE_BANNER
      SLOT: 52
      HOME-LORE: '&fClick to teleport to your team''s home'
      NO-HOME-LORE: '&fSet the team home with /home'
    PVP-BUTTON:
      TITLE: '&#6BF18DPVP'
      MATERIAL: IRON_SWORD
      SLOT: 53
      ON-STATE: '&
```

---

## Section: `HOME-MENU-LEGACY`

### 1. Commented Setup Code Example

```yaml
HOME-MENU-LEGACY:
  TITLE: '&8Homes'
  SIZE: 36
  TELEPORT-USED-MATERIAL: LIGHT_BLUE_BED
  TELEPORT-NO-USED-MATERIAL: LIGHT_GRAY_BED
  TELEPORT-NO-PERMISSION-MATERIAL: RED_BED
  CREATE-USED-MATERIAL: BLUE_DYE
  CREATE-NO-USED-MATERIAL: GRAY_DYE
  CREATE-NO-PERMISSION-MATERIAL: RED_DYE
  TEAM_HOME:
    TELEPORT:
      MATERIALS:
        NO_TEAM: RED_BANNER
        NO_HOME: WHITE_BANNER
        HAS_HOME: WHITE_BANNER
      DISPLAY_NAME:
        NO_TEAM: '&cTEAM HOME'
        NO_HOME: '&cTEAM HOME'
        HAS_HOME: '&bTEAM HOME'
      LORE:
        NO_TEAM: '&fYou don''t have a team.'
        NO_HOME: '&fClick to create a team home.'
        HAS_HOME: '&fClick to teleport to your team''s home.'
    SAVE:
      MATERIALS:
        NO_TEAM: RED_DYE
        NO_HOME: GRAY_DYE
        HAS_HOME: BLUE_DYE
      DISPLAY_NAME:
        NO_TEAM: '&cTEAM HOME'
        NO_HOME: '&cTEAM HOME'
        HAS_HOME: '&bTEAM HOME'
      LORE:
        NO_TEAM: '&fYou don''t have a team.'
        NO_HOME: '&fClick to create a team home.'
        HAS_HOME: '&fClick to teleport to your team''s home.'
  TELEPORT:
    HOME-1:
      DISPLAY-NAME:
        NO-USED: '&7NO HOME SET'
        USED: '&bHOME 1'
        NO-PERMISSION: '&cNO PERMISSION'
      LORE:
        NO-USED: '&f- Click to create a home'
        USED: '&fClick to teleport to your home'
        NO-PERMISSION: '&fYou need a higher rank for this home'
    HOME-2:
      DISPLAY-NAME:
        NO-USED: '&7NO HOME SET'
        USED: '&bHOME 2'
        NO-PERMISSION: '&cNO PERMISSION'
      LORE:
        NO-USED: '&f- Click to create a home'
        USED: '&fClick to teleport to your home'
        NO-PERMISSION: '&fYou need a higher rank for this home'
    HOME-3:
      DISPLAY-NAME:
        NO-USED: '&7NO HOME SET'
        USED: '&bHOME 3'
        NO-PERMISSION: '&cNO PERMISSION'
      LORE:
        NO-USED: '&f- Click to create a home'
        USED: '&fClick to teleport to your home'
        NO-PERMISSION: '&fYou need a higher rank for this home'
    HOME-4:
      DISPLAY-NAME:
        NO-USED: '&7NO HOME SET'
        USED: '&bHOME 4'
        NO-PERMISSION: '&cNO PERMISSION'
      LORE:
        NO-USED: '&f- Click to create a home'
        USED: '&fClick to teleport to your home'
        NO-PERMISSION: '&fYou need a higher rank for this home'
    HOME-5:
      DISPLAY-NAME:
        NO-USED: '&7NO HOME SET'
        USED: '&bHOME 5'
        NO-PERMISSION: '&cNO PERMISSION'
      LORE:
        NO-USED: '&f- Click to create a home'
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `HOME-MENU-LEGACY.TITLE` | `str` | Any string text | `'&8Homes'` | Configures the technical `TITLE` parameter for `HOME-MENU-LEGACY.TITLE` in `menus.yml`. |
| `HOME-MENU-LEGACY.SIZE` | `int` | Any valid integer number | `'36'` | Configures the technical `SIZE` parameter for `HOME-MENU-LEGACY.SIZE` in `menus.yml`. |
| `HOME-MENU-LEGACY.TELEPORT-USED-MATERIAL` | `str` | Any string text | `'LIGHT_BLUE_BED'` | Configures the technical `TELEPORT-USED-MATERIAL` parameter for `HOME-MENU-LEGACY.TELEPORT-USED-MATERIAL` in `menus.yml`. |
| `HOME-MENU-LEGACY.TELEPORT-NO-USED-MATERIAL` | `str` | Any string text | `'LIGHT_GRAY_BED'` | Configures the technical `TELEPORT-NO-USED-MATERIAL` parameter for `HOME-MENU-LEGACY.TELEPORT-NO-USED-MATERIAL` in `menus.yml`. |
| `HOME-MENU-LEGACY.TELEPORT-NO-PERMISSION-MATERIAL` | `str` | Any string text | `'RED_BED'` | Configures the technical `TELEPORT-NO-PERMISSION-MATERIAL` parameter for `HOME-MENU-LEGACY.TELEPORT-NO-PERMISSION-MATERIAL` in `menus.yml`. |
| `HOME-MENU-LEGACY.CREATE-USED-MATERIAL` | `str` | Any string text | `'BLUE_DYE'` | Configures the technical `CREATE-USED-MATERIAL` parameter for `HOME-MENU-LEGACY.CREATE-USED-MATERIAL` in `menus.yml`. |
| `HOME-MENU-LEGACY.CREATE-NO-USED-MATERIAL` | `str` | Any string text | `'GRAY_DYE'` | Configures the technical `CREATE-NO-USED-MATERIAL` parameter for `HOME-MENU-LEGACY.CREATE-NO-USED-MATERIAL` in `menus.yml`. |
| `HOME-MENU-LEGACY.CREATE-NO-PERMISSION-MATERIAL` | `str` | Any string text | `'RED_DYE'` | Configures the technical `CREATE-NO-PERMISSION-MATERIAL` parameter for `HOME-MENU-LEGACY.CREATE-NO-PERMISSION-MATERIAL` in `menus.yml`. |
| `HOME-MENU-LEGACY.TEAM_HOME.TELEPORT.MATERIALS.NO_TEAM` | `str` | Any string text | `'RED_BANNER'` | Configures the technical `NO_TEAM` parameter for `HOME-MENU-LEGACY.TEAM_HOME.TELEPORT.MATERIALS.NO_TEAM` in `menus.yml`. |
| `HOME-MENU-LEGACY.TEAM_HOME.TELEPORT.MATERIALS.NO_HOME` | `str` | Any string text | `'WHITE_BANNER'` | Configures the technical `NO_HOME` parameter for `HOME-MENU-LEGACY.TEAM_HOME.TELEPORT.MATERIALS.NO_HOME` in `menus.yml`. |
| `HOME-MENU-LEGACY.TEAM_HOME.TELEPORT.MATERIALS.HAS_HOME` | `str` | Any string text | `'WHITE_BANNER'` | Configures the technical `HAS_HOME` parameter for `HOME-MENU-LEGACY.TEAM_HOME.TELEPORT.MATERIALS.HAS_HOME` in `menus.yml`. |
| `HOME-MENU-LEGACY.TEAM_HOME.TELEPORT.DISPLAY_NAME.NO_TEAM` | `str` | Any string text | `'&cTEAM HOME'` | Configures the technical `NO_TEAM` parameter for `HOME-MENU-LEGACY.TEAM_HOME.TELEPORT.DISPLAY_NAME.NO_TEAM` in `menus.yml`. |
| `HOME-MENU-LEGACY.TEAM_HOME.TELEPORT.DISPLAY_NAME.NO_HOME` | `str` | Any string text | `'&cTEAM HOME'` | Configures the technical `NO_HOME` parameter for `HOME-MENU-LEGACY.TEAM_HOME.TELEPORT.DISPLAY_NAME.NO_HOME` in `menus.yml`. |
| `HOME-MENU-LEGACY.TEAM_HOME.TELEPORT.DISPLAY_NAME.HAS_HOME` | `str` | Any string text | `'&bTEAM HOME'` | Configures the technical `HAS_HOME` parameter for `HOME-MENU-LEGACY.TEAM_HOME.TELEPORT.DISPLAY_NAME.HAS_HOME` in `menus.yml`. |
| `HOME-MENU-LEGACY.TEAM_HOME.TELEPORT.LORE.NO_TEAM` | `str` | Any string text | `'&fYou don't have a team.'` | Configures the technical `NO_TEAM` parameter for `HOME-MENU-LEGACY.TEAM_HOME.TELEPORT.LORE.NO_TEAM` in `menus.yml`. |
| `HOME-MENU-LEGACY.TEAM_HOME.TELEPORT.LORE.NO_HOME` | `str` | Any string text | `'&fClick to create a team home.'` | Configures the technical `NO_HOME` parameter for `HOME-MENU-LEGACY.TEAM_HOME.TELEPORT.LORE.NO_HOME` in `menus.yml`. |
| `HOME-MENU-LEGACY.TEAM_HOME.TELEPORT.LORE.HAS_HOME` | `str` | Any string text | `'&fClick to teleport to your team's ...'` | Configures the technical `HAS_HOME` parameter for `HOME-MENU-LEGACY.TEAM_HOME.TELEPORT.LORE.HAS_HOME` in `menus.yml`. |
| `HOME-MENU-LEGACY.TEAM_HOME.SAVE.MATERIALS.NO_TEAM` | `str` | Any string text | `'RED_DYE'` | Configures the technical `NO_TEAM` parameter for `HOME-MENU-LEGACY.TEAM_HOME.SAVE.MATERIALS.NO_TEAM` in `menus.yml`. |
| `HOME-MENU-LEGACY.TEAM_HOME.SAVE.MATERIALS.NO_HOME` | `str` | Any string text | `'GRAY_DYE'` | Configures the technical `NO_HOME` parameter for `HOME-MENU-LEGACY.TEAM_HOME.SAVE.MATERIALS.NO_HOME` in `menus.yml`. |
| `HOME-MENU-LEGACY.TEAM_HOME.SAVE.MATERIALS.HAS_HOME` | `str` | Any string text | `'BLUE_DYE'` | Configures the technical `HAS_HOME` parameter for `HOME-MENU-LEGACY.TEAM_HOME.SAVE.MATERIALS.HAS_HOME` in `menus.yml`. |
| `HOME-MENU-LEGACY.TEAM_HOME.SAVE.DISPLAY_NAME.NO_TEAM` | `str` | Any string text | `'&cTEAM HOME'` | Configures the technical `NO_TEAM` parameter for `HOME-MENU-LEGACY.TEAM_HOME.SAVE.DISPLAY_NAME.NO_TEAM` in `menus.yml`. |
| `HOME-MENU-LEGACY.TEAM_HOME.SAVE.DISPLAY_NAME.NO_HOME` | `str` | Any string text | `'&cTEAM HOME'` | Configures the technical `NO_HOME` parameter for `HOME-MENU-LEGACY.TEAM_HOME.SAVE.DISPLAY_NAME.NO_HOME` in `menus.yml`. |
| `HOME-MENU-LEGACY.TEAM_HOME.SAVE.DISPLAY_NAME.HAS_HOME` | `str` | Any string text | `'&bTEAM HOME'` | Configures the technical `HAS_HOME` parameter for `HOME-MENU-LEGACY.TEAM_HOME.SAVE.DISPLAY_NAME.HAS_HOME` in `menus.yml`. |
| `HOME-MENU-LEGACY.TEAM_HOME.SAVE.LORE.NO_TEAM` | `str` | Any string text | `'&fYou don't have a team.'` | Configures the technical `NO_TEAM` parameter for `HOME-MENU-LEGACY.TEAM_HOME.SAVE.LORE.NO_TEAM` in `menus.yml`. |
| `HOME-MENU-LEGACY.TEAM_HOME.SAVE.LORE.NO_HOME` | `str` | Any string text | `'&fClick to create a team home.'` | Configures the technical `NO_HOME` parameter for `HOME-MENU-LEGACY.TEAM_HOME.SAVE.LORE.NO_HOME` in `menus.yml`. |
| `HOME-MENU-LEGACY.TEAM_HOME.SAVE.LORE.HAS_HOME` | `str` | Any string text | `'&fClick to teleport to your team's ...'` | Configures the technical `HAS_HOME` parameter for `HOME-MENU-LEGACY.TEAM_HOME.SAVE.LORE.HAS_HOME` in `menus.yml`. |
| `HOME-MENU-LEGACY.TELEPORT.HOME-1.DISPLAY-NAME.NO-USED` | `str` | Any string text | `'&7NO HOME SET'` | Configures the technical `NO-USED` parameter for `HOME-MENU-LEGACY.TELEPORT.HOME-1.DISPLAY-NAME.NO-USED` in `menus.yml`. |
| `HOME-MENU-LEGACY.TELEPORT.HOME-1.DISPLAY-NAME.USED` | `str` | Any string text | `'&bHOME 1'` | Configures the technical `USED` parameter for `HOME-MENU-LEGACY.TELEPORT.HOME-1.DISPLAY-NAME.USED` in `menus.yml`. |
| `HOME-MENU-LEGACY.TELEPORT.HOME-1.DISPLAY-NAME.NO-PERMISSION` | `str` | Any string text | `'&cNO PERMISSION'` | Configures the technical `NO-PERMISSION` parameter for `HOME-MENU-LEGACY.TELEPORT.HOME-1.DISPLAY-NAME.NO-PERMISSION` in `menus.yml`. |
| `HOME-MENU-LEGACY.TELEPORT.HOME-1.LORE.NO-USED` | `str` | Any string text | `'&f- Click to create a home'` | Configures the technical `NO-USED` parameter for `HOME-MENU-LEGACY.TELEPORT.HOME-1.LORE.NO-USED` in `menus.yml`. |
| *(56 additional sub-keys configured in section)* | | | | |

### 3. Practical Setup Example

```yaml
HOME-MENU-LEGACY:
  TITLE: '&8Homes'
  SIZE: 36
  TELEPORT-USED-MATERIAL: LIGHT_BLUE_BED
  TELEPORT-NO-USED-MATERIAL: LIGHT_GRAY_BED
  TELEPORT-NO-PERMISSION-MATERIAL: RED_BED
  CREATE-USED-MATERIAL: BLUE_DYE
  CREATE-NO-USED-MATERIAL: GRAY_DYE
  CREATE-NO-PERMISSION-MATERIAL: RED_DYE
  TEAM_HOME:
    TELEPORT:
      MATERIALS:
        NO_TEAM: RED_BANNER
        NO_HOME: WHITE_BANNER
        HAS_HOME: WHITE_BANNER
      DISPLAY_NAME:
        NO_TEAM: '&cTEAM HOME'
        NO_HOME: '&cTEAM HOME'
        HAS_HOME: '&bTEAM HOME'
      LORE:
        NO_TEAM: '&fYou don''t have a team.'
        NO_HOME: '&fClick to create a team home.'
        HAS_HOME: '&fClick to teleport to your team''s home.'
    SAVE:
      MATERIALS:
        NO_TEAM: RED_DYE
        NO_HOME: GRAY_DYE
        HAS_HOME: BLUE_DYE
      DISPLAY_NAME:
        NO_TEAM: '&cTEAM HOME'
        NO_HOME: '&cTEAM HOME'
        HAS_HOME: '&bTEAM HOME'
      LORE:
        NO_TEAM: '&fYou don''t have a team.'
        NO_HOME: '&fCl
```

---

## Section: `HOME-MENU`

### 1. Commented Setup Code Example

```yaml
HOME-MENU:
  TITLE: '&8Homes'
  SIZE: 36
  TELEPORT-USED-MATERIAL: LIGHT_BLUE_BED
  TELEPORT-NO-USED-MATERIAL: LIGHT_GRAY_BED
  TELEPORT-NO-PERMISSION-MATERIAL: RED_BED
  CREATE-USED-MATERIAL: BLUE_DYE
  CREATE-NO-USED-MATERIAL: GRAY_DYE
  CREATE-NO-PERMISSION-MATERIAL: RED_DYE
  TEAM_HOME:
    TELEPORT:
      MATERIALS:
        NO_TEAM: RED_BANNER
        NO_HOME: WHITE_BANNER
        HAS_HOME: WHITE_BANNER
      DISPLAY_NAME:
        NO_TEAM: '&cTeam Home'
        NO_HOME: '&fTeam Home'
        HAS_HOME: '&bTeam Home'
      LORE:
        NO_TEAM:
        - '&7You are not in a team.'
        NO_HOME:
        - '&7Click to create your team home.'
        HAS_HOME:
        - '&7World: &f{world}'
        - '&aLeft-click to teleport'
    SAVE:
      MATERIALS:
        NO_TEAM: RED_DYE
        NO_HOME: GRAY_DYE
        HAS_HOME: BLUE_DYE
      DISPLAY_NAME:
        NO_TEAM: '&cTeam Home'
        NO_HOME: '&7Set Team Home'
        HAS_HOME: '&bManage Team Home'
      LORE:
        NO_TEAM:
        - '&7You are not in a team.'
        NO_HOME:
        - '&7Left-click to save your team home.'
        HAS_HOME:
        - '&bLeft-click to update the team home'
        - '&cRight-click to delete'
  TELEPORT:
    HOME-1:
      DISPLAY-NAME:
        NO-USED: '&7{slot}'
        USED: '&b{name}'
        NO-PERMISSION: '&cLocked'
      LORE:
        NO-USED:
        - '&7Click to create a home.'
        USED:
        - '&7World: &f{world}'
        - '&aLeft-click to teleport'
        NO-PERMISSION:
        - '&7You need a higher rank for this home.'
    HOME-2:
      DISPLAY-NAME:
        NO-USED: '&7{slot}'
        USED: '&b{name}'
        NO-PERMISSION: '&cLocked'
      LORE:
        NO-USED:
        - '&7Click to create a home.'
        USED:
        - '&7World: &f{world}'
        - '&aLeft-click to teleport'
        NO-PERMISSION:
        - '&7You need a higher rank for this home.'
    HOME-3:
      DISPLAY-NAME:
        NO-USED: '&7{slot}'
        USED: '&b{name}'
        NO-PERMISSION: '&cLocked'
      LORE:
        NO-USED:
        - '&7Click to create a home.'
        USED:
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `HOME-MENU.TITLE` | `str` | Any string text | `'&8Homes'` | Configures the technical `TITLE` parameter for `HOME-MENU.TITLE` in `menus.yml`. |
| `HOME-MENU.SIZE` | `int` | Any valid integer number | `'36'` | Configures the technical `SIZE` parameter for `HOME-MENU.SIZE` in `menus.yml`. |
| `HOME-MENU.TELEPORT-USED-MATERIAL` | `str` | Any string text | `'LIGHT_BLUE_BED'` | Configures the technical `TELEPORT-USED-MATERIAL` parameter for `HOME-MENU.TELEPORT-USED-MATERIAL` in `menus.yml`. |
| `HOME-MENU.TELEPORT-NO-USED-MATERIAL` | `str` | Any string text | `'LIGHT_GRAY_BED'` | Configures the technical `TELEPORT-NO-USED-MATERIAL` parameter for `HOME-MENU.TELEPORT-NO-USED-MATERIAL` in `menus.yml`. |
| `HOME-MENU.TELEPORT-NO-PERMISSION-MATERIAL` | `str` | Any string text | `'RED_BED'` | Configures the technical `TELEPORT-NO-PERMISSION-MATERIAL` parameter for `HOME-MENU.TELEPORT-NO-PERMISSION-MATERIAL` in `menus.yml`. |
| `HOME-MENU.CREATE-USED-MATERIAL` | `str` | Any string text | `'BLUE_DYE'` | Configures the technical `CREATE-USED-MATERIAL` parameter for `HOME-MENU.CREATE-USED-MATERIAL` in `menus.yml`. |
| `HOME-MENU.CREATE-NO-USED-MATERIAL` | `str` | Any string text | `'GRAY_DYE'` | Configures the technical `CREATE-NO-USED-MATERIAL` parameter for `HOME-MENU.CREATE-NO-USED-MATERIAL` in `menus.yml`. |
| `HOME-MENU.CREATE-NO-PERMISSION-MATERIAL` | `str` | Any string text | `'RED_DYE'` | Configures the technical `CREATE-NO-PERMISSION-MATERIAL` parameter for `HOME-MENU.CREATE-NO-PERMISSION-MATERIAL` in `menus.yml`. |
| `HOME-MENU.TEAM_HOME.TELEPORT.MATERIALS.NO_TEAM` | `str` | Any string text | `'RED_BANNER'` | Configures the technical `NO_TEAM` parameter for `HOME-MENU.TEAM_HOME.TELEPORT.MATERIALS.NO_TEAM` in `menus.yml`. |
| `HOME-MENU.TEAM_HOME.TELEPORT.MATERIALS.NO_HOME` | `str` | Any string text | `'WHITE_BANNER'` | Configures the technical `NO_HOME` parameter for `HOME-MENU.TEAM_HOME.TELEPORT.MATERIALS.NO_HOME` in `menus.yml`. |
| `HOME-MENU.TEAM_HOME.TELEPORT.MATERIALS.HAS_HOME` | `str` | Any string text | `'WHITE_BANNER'` | Configures the technical `HAS_HOME` parameter for `HOME-MENU.TEAM_HOME.TELEPORT.MATERIALS.HAS_HOME` in `menus.yml`. |
| `HOME-MENU.TEAM_HOME.TELEPORT.DISPLAY_NAME.NO_TEAM` | `str` | Any string text | `'&cTeam Home'` | Configures the technical `NO_TEAM` parameter for `HOME-MENU.TEAM_HOME.TELEPORT.DISPLAY_NAME.NO_TEAM` in `menus.yml`. |
| `HOME-MENU.TEAM_HOME.TELEPORT.DISPLAY_NAME.NO_HOME` | `str` | Any string text | `'&fTeam Home'` | Configures the technical `NO_HOME` parameter for `HOME-MENU.TEAM_HOME.TELEPORT.DISPLAY_NAME.NO_HOME` in `menus.yml`. |
| `HOME-MENU.TEAM_HOME.TELEPORT.DISPLAY_NAME.HAS_HOME` | `str` | Any string text | `'&bTeam Home'` | Configures the technical `HAS_HOME` parameter for `HOME-MENU.TEAM_HOME.TELEPORT.DISPLAY_NAME.HAS_HOME` in `menus.yml`. |
| `HOME-MENU.TEAM_HOME.TELEPORT.LORE.NO_TEAM` | `list` | List of configured items/strings | `['&7You are not in a team.']` | Configures the technical `NO_TEAM` parameter for `HOME-MENU.TEAM_HOME.TELEPORT.LORE.NO_TEAM` in `menus.yml`. |
| `HOME-MENU.TEAM_HOME.TELEPORT.LORE.NO_HOME` | `list` | List of configured items/strings | `['&7Click to create your team home.']` | Configures the technical `NO_HOME` parameter for `HOME-MENU.TEAM_HOME.TELEPORT.LORE.NO_HOME` in `menus.yml`. |
| `HOME-MENU.TEAM_HOME.TELEPORT.LORE.HAS_HOME` | `list` | List of configured items/strings | `['&7World: &f{world}', '&aLeft-click to teleport']` | Configures the technical `HAS_HOME` parameter for `HOME-MENU.TEAM_HOME.TELEPORT.LORE.HAS_HOME` in `menus.yml`. |
| `HOME-MENU.TEAM_HOME.SAVE.MATERIALS.NO_TEAM` | `str` | Any string text | `'RED_DYE'` | Configures the technical `NO_TEAM` parameter for `HOME-MENU.TEAM_HOME.SAVE.MATERIALS.NO_TEAM` in `menus.yml`. |
| `HOME-MENU.TEAM_HOME.SAVE.MATERIALS.NO_HOME` | `str` | Any string text | `'GRAY_DYE'` | Configures the technical `NO_HOME` parameter for `HOME-MENU.TEAM_HOME.SAVE.MATERIALS.NO_HOME` in `menus.yml`. |
| `HOME-MENU.TEAM_HOME.SAVE.MATERIALS.HAS_HOME` | `str` | Any string text | `'BLUE_DYE'` | Configures the technical `HAS_HOME` parameter for `HOME-MENU.TEAM_HOME.SAVE.MATERIALS.HAS_HOME` in `menus.yml`. |
| `HOME-MENU.TEAM_HOME.SAVE.DISPLAY_NAME.NO_TEAM` | `str` | Any string text | `'&cTeam Home'` | Configures the technical `NO_TEAM` parameter for `HOME-MENU.TEAM_HOME.SAVE.DISPLAY_NAME.NO_TEAM` in `menus.yml`. |
| `HOME-MENU.TEAM_HOME.SAVE.DISPLAY_NAME.NO_HOME` | `str` | Any string text | `'&7Set Team Home'` | Configures the technical `NO_HOME` parameter for `HOME-MENU.TEAM_HOME.SAVE.DISPLAY_NAME.NO_HOME` in `menus.yml`. |
| `HOME-MENU.TEAM_HOME.SAVE.DISPLAY_NAME.HAS_HOME` | `str` | Any string text | `'&bManage Team Home'` | Configures the technical `HAS_HOME` parameter for `HOME-MENU.TEAM_HOME.SAVE.DISPLAY_NAME.HAS_HOME` in `menus.yml`. |
| `HOME-MENU.TEAM_HOME.SAVE.LORE.NO_TEAM` | `list` | List of configured items/strings | `['&7You are not in a team.']` | Configures the technical `NO_TEAM` parameter for `HOME-MENU.TEAM_HOME.SAVE.LORE.NO_TEAM` in `menus.yml`. |
| `HOME-MENU.TEAM_HOME.SAVE.LORE.NO_HOME` | `list` | List of configured items/strings | `['&7Left-click to save your team home.']` | Configures the technical `NO_HOME` parameter for `HOME-MENU.TEAM_HOME.SAVE.LORE.NO_HOME` in `menus.yml`. |
| `HOME-MENU.TEAM_HOME.SAVE.LORE.HAS_HOME` | `list` | List of configured items/strings | `['&bLeft-click to update the team home', '&cRight-click to delete']` | Configures the technical `HAS_HOME` parameter for `HOME-MENU.TEAM_HOME.SAVE.LORE.HAS_HOME` in `menus.yml`. |
| `HOME-MENU.TELEPORT.HOME-1.DISPLAY-NAME.NO-USED` | `str` | Any string text | `'&7{slot}'` | Configures the technical `NO-USED` parameter for `HOME-MENU.TELEPORT.HOME-1.DISPLAY-NAME.NO-USED` in `menus.yml`. |
| `HOME-MENU.TELEPORT.HOME-1.DISPLAY-NAME.USED` | `str` | Any string text | `'&b{name}'` | Configures the technical `USED` parameter for `HOME-MENU.TELEPORT.HOME-1.DISPLAY-NAME.USED` in `menus.yml`. |
| `HOME-MENU.TELEPORT.HOME-1.DISPLAY-NAME.NO-PERMISSION` | `str` | Any string text | `'&cLocked'` | Configures the technical `NO-PERMISSION` parameter for `HOME-MENU.TELEPORT.HOME-1.DISPLAY-NAME.NO-PERMISSION` in `menus.yml`. |
| `HOME-MENU.TELEPORT.HOME-1.LORE.NO-USED` | `list` | List of configured items/strings | `['&7Click to create a home.']` | Configures the technical `NO-USED` parameter for `HOME-MENU.TELEPORT.HOME-1.LORE.NO-USED` in `menus.yml`. |
| *(56 additional sub-keys configured in section)* | | | | |

### 3. Practical Setup Example

```yaml
HOME-MENU:
  TITLE: '&8Homes'
  SIZE: 36
  TELEPORT-USED-MATERIAL: LIGHT_BLUE_BED
  TELEPORT-NO-USED-MATERIAL: LIGHT_GRAY_BED
  TELEPORT-NO-PERMISSION-MATERIAL: RED_BED
  CREATE-USED-MATERIAL: BLUE_DYE
  CREATE-NO-USED-MATERIAL: GRAY_DYE
  CREATE-NO-PERMISSION-MATERIAL: RED_DYE
  TEAM_HOME:
    TELEPORT:
      MATERIALS:
        NO_TEAM: RED_BANNER
        NO_HOME: WHITE_BANNER
        HAS_HOME: WHITE_BANNER
      DISPLAY_NAME:
        NO_TEAM: '&cTeam Home'
        NO_HOME: '&fTeam Home'
        HAS_HOME: '&bTeam Home'
      LORE:
        NO_TEAM:
        - '&7You are not in a team.'
        NO_HOME:
        - '&7Click to create your team home.'
        HAS_HOME:
        - '&7World: &f{world}'
        - '&aLeft-click to teleport'
    SAVE:
      MATERIALS:
        NO_TEAM: RED_DYE
        NO_HOME: GRAY_DYE
        HAS_HOME: BLUE_DYE
      DISPLAY_NAME:
        NO_TEAM: '&cTeam Home'
        NO_HOME: '&7Set Team Home'
        HAS_HOME: '&bManage Team Home'
      LORE:
        NO_TEAM:

```

---

## Section: `CONFIRM-MENU`

### 1. Commented Setup Code Example

```yaml
CONFIRM-MENU:
  TITLE: '&8Confirm Delete'
  SIZE: 27
  CONFIRM-BUTTON:
    DISPLAY-NAME: '&aConfirm'
    MATERIAL: LIME_STAINED_GLASS_PANE
    LORE:
    - '&fClick to delete'
  CANCEL-BUTTON:
    DISPLAY-NAME: '&4Cancel'
    MATERIAL: RED_STAINED_GLASS_PANE
    LORE:
    - '&fClick to cancel'
    -
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `CONFIRM-MENU.TITLE` | `str` | Any string text | `'&8Confirm Delete'` | Configures the technical `TITLE` parameter for `CONFIRM-MENU.TITLE` in `menus.yml`. |
| `CONFIRM-MENU.SIZE` | `int` | Any valid integer number | `'27'` | Configures the technical `SIZE` parameter for `CONFIRM-MENU.SIZE` in `menus.yml`. |
| `CONFIRM-MENU.CONFIRM-BUTTON.DISPLAY-NAME` | `str` | Any string text | `'&aConfirm'` | Configures the technical `DISPLAY-NAME` parameter for `CONFIRM-MENU.CONFIRM-BUTTON.DISPLAY-NAME` in `menus.yml`. |
| `CONFIRM-MENU.CONFIRM-BUTTON.MATERIAL` | `str` | Any string text | `'LIME_STAINED_GLASS_PANE'` | Configures the technical `MATERIAL` parameter for `CONFIRM-MENU.CONFIRM-BUTTON.MATERIAL` in `menus.yml`. |
| `CONFIRM-MENU.CONFIRM-BUTTON.LORE` | `list` | List of configured items/strings | `['&fClick to delete']` | Configures the technical `LORE` parameter for `CONFIRM-MENU.CONFIRM-BUTTON.LORE` in `menus.yml`. |
| `CONFIRM-MENU.CANCEL-BUTTON.DISPLAY-NAME` | `str` | Any string text | `'&4Cancel'` | Configures the technical `DISPLAY-NAME` parameter for `CONFIRM-MENU.CANCEL-BUTTON.DISPLAY-NAME` in `menus.yml`. |
| `CONFIRM-MENU.CANCEL-BUTTON.MATERIAL` | `str` | Any string text | `'RED_STAINED_GLASS_PANE'` | Configures the technical `MATERIAL` parameter for `CONFIRM-MENU.CANCEL-BUTTON.MATERIAL` in `menus.yml`. |
| `CONFIRM-MENU.CANCEL-BUTTON.LORE` | `list` | List of configured items/strings | `['&fClick to cancel', None]` | Configures the technical `LORE` parameter for `CONFIRM-MENU.CANCEL-BUTTON.LORE` in `menus.yml`. |

### 3. Practical Setup Example

```yaml
CONFIRM-MENU:
  TITLE: '&8Confirm Delete'
  SIZE: 27
  CONFIRM-BUTTON:
    DISPLAY-NAME: '&aConfirm'
    MATERIAL: LIME_STAINED_GLASS_PANE
    LORE:
    - '&fClick to delete'
  CANCEL-BUTTON:
    DISPLAY-NAME: '&4Cancel'
    MATERIAL: RED_STAINED_GLASS_PANE
    LORE:
    - '&fClick to cancel'
    -
```

---

## Section: `MEDIA-MENU`

### 1. Commented Setup Code Example

```yaml
MEDIA-MENU:
  TITLE: '&8Media Rank'
  SIZE: 27
  MEDIA-BUTTON:
    DISPLAY-NAME: '&dMedia Rank'
    MATERIAL: PINK_DYE
    SLOT: 13
    LORE:
    - '&dReQuirements: (only one needed)'
    - '&d- &f25 average viewers on Stream'
    - '&d- &f5k views on a YouTube Video'
    - '&d- &f25k views on a TikTok'
    - '&d- &f50k views on YouTube Short'
    - ''
    - '&dReminders:'
    - '&8- &7Must have the IP on screen'
    - '&8- &7Must be from the new season'
    - '&8- &7Create ticket in discord for the rank'
    - '&8- &7It lasts 90 days and has all top ranks perks'
    - ''
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `MEDIA-MENU.TITLE` | `str` | Any string text | `'&8Media Rank'` | Configures the technical `TITLE` parameter for `MEDIA-MENU.TITLE` in `menus.yml`. |
| `MEDIA-MENU.SIZE` | `int` | Any valid integer number | `'27'` | Configures the technical `SIZE` parameter for `MEDIA-MENU.SIZE` in `menus.yml`. |
| `MEDIA-MENU.MEDIA-BUTTON.DISPLAY-NAME` | `str` | Any string text | `'&dMedia Rank'` | Configures the technical `DISPLAY-NAME` parameter for `MEDIA-MENU.MEDIA-BUTTON.DISPLAY-NAME` in `menus.yml`. |
| `MEDIA-MENU.MEDIA-BUTTON.MATERIAL` | `str` | Any string text | `'PINK_DYE'` | Configures the technical `MATERIAL` parameter for `MEDIA-MENU.MEDIA-BUTTON.MATERIAL` in `menus.yml`. |
| `MEDIA-MENU.MEDIA-BUTTON.SLOT` | `int` | Any valid integer number | `'13'` | Configures the technical `SLOT` parameter for `MEDIA-MENU.MEDIA-BUTTON.SLOT` in `menus.yml`. |
| `MEDIA-MENU.MEDIA-BUTTON.LORE` | `list` | List of configured items/strings | `[&dReQuirements: (only one needed), &d- &f25 average viewers on Stream, &d- &f5k views on a YouTube Video...]` | Configures the technical `LORE` parameter for `MEDIA-MENU.MEDIA-BUTTON.LORE` in `menus.yml`. |

### 3. Practical Setup Example

```yaml
MEDIA-MENU:
  TITLE: '&8Media Rank'
  SIZE: 27
  MEDIA-BUTTON:
    DISPLAY-NAME: '&dMedia Rank'
    MATERIAL: PINK_DYE
    SLOT: 13
    LORE:
    - '&dReQuirements: (only one needed)'
    - '&d- &f25 average viewers on Stream'
    - '&d- &f5k views on a YouTube Video'
    - '&d- &f25k views on a TikTok'
    - '&d- &f50k views on YouTube Short'
    - ''
    - '&dReminders:'
    - '&8- &7Must have the IP on screen'
    - '&8- &7Must be from the new season'
    - '&8- &7Create ticket in discord for the rank'
    - '&8- &7It lasts 90 days and has all top ranks perks'
    - ''
```

---

## Section: `STATS-MENU`

### 1. Commented Setup Code Example

```yaml
STATS-MENU:
  TITLE: '&8{username} Stats'
  SIZE: 36
  BUTTONS:
    MONEY:
      DISPLAY-NAME: '&#6BF18DMoney'
      MATERIAL: EMERALD
      SLOT: 10
      LORE:
      - '&7{value}'
    SHARDS:
      DISPLAY-NAME: '&#6BF18DShards'
      MATERIAL: AMETHYST_SHARD
      SLOT: 11
      LORE:
      - '&7{value}'
    KILLS:
      DISPLAY-NAME: '&#6BF18DKills'
      MATERIAL: DIAMOND_SWORD
      SLOT: 12
      LORE:
      - '&7{value}'
    DEATHS:
      DISPLAY-NAME: '&#6BF18DDeaths'
      MATERIAL: SKELETON_SKULL
      SLOT: 13
      LORE:
      - '&7{value}'
    PLAYTIME:
      DISPLAY-NAME: '&#6BF18DPlaytime'
      MATERIAL: CLOCK
      SLOT: 14
      LORE:
      - '&7{value}'
    BLOCKS_PLACED:
      DISPLAY-NAME: '&#6BF18DBlocks Placed'
      MATERIAL: STONE
      SLOT: 15
      LORE:
      - '&7{value}'
    BLOCKS_BROKEN:
      DISPLAY-NAME: '&#6BF18DBlocks Broken'
      MATERIAL: COBBLESTONE
      SLOT: 16
      LORE:
      - '&7{value}'
    MOBS_KILLED:
      DISPLAY-NAME: '&#6BF18DMobs Killed'
      MATERIAL: ZOMBIE_HEAD
      SLOT: 19
      LORE:
      - '&7{value}'
    KILL_STREAK:
      DISPLAY-NAME: '&#6BF18DKill Streak'
      MATERIAL: DIAMOND_AXE
      SLOT: 20
      LORE:
      - '&7{value}'
    HIGHEST_KILL_STREAK:
      DISPLAY-NAME: '&#6BF18DHighest Kill Streak'
      MATERIAL: NETHERITE_SWORD
      SLOT: 21
      LORE:
      - '&7{value}'
    MONEY_SPENT:
      DISPLAY-NAME: '&#6BF18DMoney Spent On Shop'
      MATERIAL: GOLD_NUGGET
      SLOT: 22
      LORE:
      - '&7{value}'
    MONEY_MADE:
      DISPLAY-NAME: '&#6BF18DMoney Made On /Sell'
      MATERIAL: IRON_NUGGET
      SLOT: 23
      LORE:
      - '&7{value}'
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `STATS-MENU.TITLE` | `str` | Any string text | `'&8{username} Stats'` | Configures the technical `TITLE` parameter for `STATS-MENU.TITLE` in `menus.yml`. |
| `STATS-MENU.SIZE` | `int` | Any valid integer number | `'36'` | Configures the technical `SIZE` parameter for `STATS-MENU.SIZE` in `menus.yml`. |
| `STATS-MENU.BUTTONS.MONEY.DISPLAY-NAME` | `str` | Any string text | `'&#6BF18DMoney'` | Configures the technical `DISPLAY-NAME` parameter for `STATS-MENU.BUTTONS.MONEY.DISPLAY-NAME` in `menus.yml`. |
| `STATS-MENU.BUTTONS.MONEY.MATERIAL` | `str` | Any string text | `'EMERALD'` | Configures the technical `MATERIAL` parameter for `STATS-MENU.BUTTONS.MONEY.MATERIAL` in `menus.yml`. |
| `STATS-MENU.BUTTONS.MONEY.SLOT` | `int` | Any valid integer number | `'10'` | Configures the technical `SLOT` parameter for `STATS-MENU.BUTTONS.MONEY.SLOT` in `menus.yml`. |
| `STATS-MENU.BUTTONS.MONEY.LORE` | `list` | List of configured items/strings | `['&7{value}']` | Configures the technical `LORE` parameter for `STATS-MENU.BUTTONS.MONEY.LORE` in `menus.yml`. |
| `STATS-MENU.BUTTONS.SHARDS.DISPLAY-NAME` | `str` | Any string text | `'&#6BF18DShards'` | Configures the technical `DISPLAY-NAME` parameter for `STATS-MENU.BUTTONS.SHARDS.DISPLAY-NAME` in `menus.yml`. |
| `STATS-MENU.BUTTONS.SHARDS.MATERIAL` | `str` | Any string text | `'AMETHYST_SHARD'` | Configures the technical `MATERIAL` parameter for `STATS-MENU.BUTTONS.SHARDS.MATERIAL` in `menus.yml`. |
| `STATS-MENU.BUTTONS.SHARDS.SLOT` | `int` | Any valid integer number | `'11'` | Configures the technical `SLOT` parameter for `STATS-MENU.BUTTONS.SHARDS.SLOT` in `menus.yml`. |
| `STATS-MENU.BUTTONS.SHARDS.LORE` | `list` | List of configured items/strings | `['&7{value}']` | Configures the technical `LORE` parameter for `STATS-MENU.BUTTONS.SHARDS.LORE` in `menus.yml`. |
| `STATS-MENU.BUTTONS.KILLS.DISPLAY-NAME` | `str` | Any string text | `'&#6BF18DKills'` | Configures the technical `DISPLAY-NAME` parameter for `STATS-MENU.BUTTONS.KILLS.DISPLAY-NAME` in `menus.yml`. |
| `STATS-MENU.BUTTONS.KILLS.MATERIAL` | `str` | Any string text | `'DIAMOND_SWORD'` | Configures the technical `MATERIAL` parameter for `STATS-MENU.BUTTONS.KILLS.MATERIAL` in `menus.yml`. |
| `STATS-MENU.BUTTONS.KILLS.SLOT` | `int` | Any valid integer number | `'12'` | Configures the technical `SLOT` parameter for `STATS-MENU.BUTTONS.KILLS.SLOT` in `menus.yml`. |
| `STATS-MENU.BUTTONS.KILLS.LORE` | `list` | List of configured items/strings | `['&7{value}']` | Configures the technical `LORE` parameter for `STATS-MENU.BUTTONS.KILLS.LORE` in `menus.yml`. |
| `STATS-MENU.BUTTONS.DEATHS.DISPLAY-NAME` | `str` | Any string text | `'&#6BF18DDeaths'` | Configures the technical `DISPLAY-NAME` parameter for `STATS-MENU.BUTTONS.DEATHS.DISPLAY-NAME` in `menus.yml`. |
| `STATS-MENU.BUTTONS.DEATHS.MATERIAL` | `str` | Any string text | `'SKELETON_SKULL'` | Configures the technical `MATERIAL` parameter for `STATS-MENU.BUTTONS.DEATHS.MATERIAL` in `menus.yml`. |
| `STATS-MENU.BUTTONS.DEATHS.SLOT` | `int` | Any valid integer number | `'13'` | Configures the technical `SLOT` parameter for `STATS-MENU.BUTTONS.DEATHS.SLOT` in `menus.yml`. |
| `STATS-MENU.BUTTONS.DEATHS.LORE` | `list` | List of configured items/strings | `['&7{value}']` | Configures the technical `LORE` parameter for `STATS-MENU.BUTTONS.DEATHS.LORE` in `menus.yml`. |
| `STATS-MENU.BUTTONS.PLAYTIME.DISPLAY-NAME` | `str` | Any string text | `'&#6BF18DPlaytime'` | Configures the technical `DISPLAY-NAME` parameter for `STATS-MENU.BUTTONS.PLAYTIME.DISPLAY-NAME` in `menus.yml`. |
| `STATS-MENU.BUTTONS.PLAYTIME.MATERIAL` | `str` | Any string text | `'CLOCK'` | Configures the technical `MATERIAL` parameter for `STATS-MENU.BUTTONS.PLAYTIME.MATERIAL` in `menus.yml`. |
| `STATS-MENU.BUTTONS.PLAYTIME.SLOT` | `int` | Any valid integer number | `'14'` | Configures the technical `SLOT` parameter for `STATS-MENU.BUTTONS.PLAYTIME.SLOT` in `menus.yml`. |
| `STATS-MENU.BUTTONS.PLAYTIME.LORE` | `list` | List of configured items/strings | `['&7{value}']` | Configures the technical `LORE` parameter for `STATS-MENU.BUTTONS.PLAYTIME.LORE` in `menus.yml`. |
| `STATS-MENU.BUTTONS.BLOCKS_PLACED.DISPLAY-NAME` | `str` | Any string text | `'&#6BF18DBlocks Placed'` | Configures the technical `DISPLAY-NAME` parameter for `STATS-MENU.BUTTONS.BLOCKS_PLACED.DISPLAY-NAME` in `menus.yml`. |
| `STATS-MENU.BUTTONS.BLOCKS_PLACED.MATERIAL` | `str` | Any string text | `'STONE'` | Configures the technical `MATERIAL` parameter for `STATS-MENU.BUTTONS.BLOCKS_PLACED.MATERIAL` in `menus.yml`. |
| `STATS-MENU.BUTTONS.BLOCKS_PLACED.SLOT` | `int` | Any valid integer number | `'15'` | Configures the technical `SLOT` parameter for `STATS-MENU.BUTTONS.BLOCKS_PLACED.SLOT` in `menus.yml`. |
| `STATS-MENU.BUTTONS.BLOCKS_PLACED.LORE` | `list` | List of configured items/strings | `['&7{value}']` | Configures the technical `LORE` parameter for `STATS-MENU.BUTTONS.BLOCKS_PLACED.LORE` in `menus.yml`. |
| `STATS-MENU.BUTTONS.BLOCKS_BROKEN.DISPLAY-NAME` | `str` | Any string text | `'&#6BF18DBlocks Broken'` | Configures the technical `DISPLAY-NAME` parameter for `STATS-MENU.BUTTONS.BLOCKS_BROKEN.DISPLAY-NAME` in `menus.yml`. |
| `STATS-MENU.BUTTONS.BLOCKS_BROKEN.MATERIAL` | `str` | Any string text | `'COBBLESTONE'` | Configures the technical `MATERIAL` parameter for `STATS-MENU.BUTTONS.BLOCKS_BROKEN.MATERIAL` in `menus.yml`. |
| `STATS-MENU.BUTTONS.BLOCKS_BROKEN.SLOT` | `int` | Any valid integer number | `'16'` | Configures the technical `SLOT` parameter for `STATS-MENU.BUTTONS.BLOCKS_BROKEN.SLOT` in `menus.yml`. |
| `STATS-MENU.BUTTONS.BLOCKS_BROKEN.LORE` | `list` | List of configured items/strings | `['&7{value}']` | Configures the technical `LORE` parameter for `STATS-MENU.BUTTONS.BLOCKS_BROKEN.LORE` in `menus.yml`. |
| *(20 additional sub-keys configured in section)* | | | | |

### 3. Practical Setup Example

```yaml
STATS-MENU:
  TITLE: '&8{username} Stats'
  SIZE: 36
  BUTTONS:
    MONEY:
      DISPLAY-NAME: '&#6BF18DMoney'
      MATERIAL: EMERALD
      SLOT: 10
      LORE:
      - '&7{value}'
    SHARDS:
      DISPLAY-NAME: '&#6BF18DShards'
      MATERIAL: AMETHYST_SHARD
      SLOT: 11
      LORE:
      - '&7{value}'
    KILLS:
      DISPLAY-NAME: '&#6BF18DKills'
      MATERIAL: DIAMOND_SWORD
      SLOT: 12
      LORE:
      - '&7{value}'
    DEATHS:
      DISPLAY-NAME: '&#6BF18DDeaths'
      MATERIAL: SKELETON_SKULL
      SLOT: 13
      LORE:
      - '&7{value}'
    PLAYTIME:
      DISPLAY-NAME: '&#6BF18DPlaytime'
      MATERIAL: CLOCK
      SLOT: 14
      LORE:
      - '&7{value}'
    BLOCKS_PLACED:
      DISPLAY-NAME: '&#6BF18DBlocks Placed'
      MATERIAL: STONE
      SLOT: 15
      LORE:
      - '&7{value}'
    BLOCKS_BROKEN:
      DISPLAY-NAME: '&#6BF18DBlocks Broken'
      MATERIAL: COBBLESTONE
      SLOT: 16
      LORE:
      - '&7{value}'
    MOBS_KILLED:
      DISPLAY-NAME: '&#6BF18DMo
```

---

## Section: `SETTINGS-MENU`

### 1. Commented Setup Code Example

```yaml
SETTINGS-MENU:
  TITLE: '&8Settings'
  SIZE: 54
  BUTTONS:
    # Every setting below accepts two optional keys:
    #   DEFAULT: <value>  Starting value for players who never touched the setting.
    #   ENABLED: false    Removes the option from /settings and pins every player to DEFAULT.
    # Example, hide advancement messages and keep them off for everyone:
    # ADVANCEMENT_MESSAGES:
    #   DEFAULT: OFF
    #   ENABLED: false
    # Custom redirect buttons can also be added here:
    # EXTERNAL_FLY:
    #   DISPLAY-NAME: '&bFlight Mode'
    #   MATERIAL: FEATHER
    #   SLOT: 25
    #   COMMAND: '[player] /fly'
    #   STATUS-PLACEHOLDER: '%cmi_user_flying%'
    #   LORE:
    #   - '&7Toggle flight via external plugin'
    #   - '&fCurrently: {status}'
    PUBLIC_CHAT:
      DISPLAY-NAME: '&#6BF18DPublic Chat'
      MATERIAL: OAK_SIGN
      SLOT: 0
      LORE:
      - '&7Receive public chat messages'
      - '&fCurrently: {status}'
    PRIVATE_MESSAGES:
      DISPLAY-NAME: '&#6BF18DPrivate Messages'
      MATERIAL: SPRUCE_SIGN
      SLOT: 1
      LORE:
      - '&7Private messages privacy settings'
      - '&fCurrently: {status}'
    SERVER_BROADCASTS:
      DISPLAY-NAME: '&#6BF18DServer Broadcasts'
      MATERIAL: WARPED_SIGN
      SLOT: 2
      LORE:
      - '&7Receive server broadcasts'
      - '&fCurrently: {status}'
    TEAM_CHAT_VISIBILITY:
      DISPLAY-NAME: '&#6BF18DTeam Chat Visibility'
      MATERIAL: DARK_OAK_SIGN
      SLOT: 3
      LORE:
      - '&7Show team chat in main chat'
      - '&fCurrently: {status}'
    LUNAR_TEAMMATES:
      DISPLAY-NAME: '&#6BF18DLunar Teammates'
      MATERIAL: BIRCH_SIGN
      SLOT: 4
      LORE:
      - '&7Show teammates on Lunar Client'
      - '&fCurrently: {status}'
    TPA_CONFIRM_MENUS:
      DISPLAY-NAME: '&#6BF18DTpa Confirm Menus'
      MATERIAL: JUNGLE_SIGN
      SLOT: 5
      LORE:
      - '&7Show confirmation GUI for TPA'
      - '&fCurrently: {status}'
    QUICK_AUCTION_PURCHASE:
      DISPLAY-NAME: '&#6BF18DQuick Auction Purchase'
      MATERIAL: GOLD_NUGGET
      SLOT: 9
      LORE:
      - '&7Fast buy auction items directly'
      - '&fCurrently: {status}'
    DESTROY_PEARL_ON_DEATH:
      DISPLAY-NAME: '&#6BF18DDestroy Pearl on Death'
      MATERIAL: ENDER_PEARL
      SLOT: 10
      LORE:
      - '&7Destroy thrown ender pearls when you die'
      - '&fCurrently: {status}'
    PAY_CONFIRM_MENUS:
      DISPLAY-NAME: '&#6BF18DPay Confirm Menus'
      MATERIAL: OAK_SIGN
      SLOT: 11
      LORE:
      - '&7Show confirmation GUI for payments'
      - '&fCurrently: {status}'
    AUTO_CONFIRM_TPAS:
      DISPLAY-NAME: '&bTp Auto'
      MATERIAL: ACACIA_SIGN
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `SETTINGS-MENU.TITLE` | `str` | Any string text | `'&8Settings'` | Configures the technical `TITLE` parameter for `SETTINGS-MENU.TITLE` in `menus.yml`. |
| `SETTINGS-MENU.SIZE` | `int` | Any valid integer number | `'54'` | Configures the technical `SIZE` parameter for `SETTINGS-MENU.SIZE` in `menus.yml`. |
| `SETTINGS-MENU.BUTTONS.PUBLIC_CHAT.DISPLAY-NAME` | `str` | Any string text | `'&#6BF18DPublic Chat'` | Configures the technical `DISPLAY-NAME` parameter for `SETTINGS-MENU.BUTTONS.PUBLIC_CHAT.DISPLAY-NAME` in `menus.yml`. |
| `SETTINGS-MENU.BUTTONS.PUBLIC_CHAT.MATERIAL` | `str` | Any string text | `'OAK_SIGN'` | Configures the technical `MATERIAL` parameter for `SETTINGS-MENU.BUTTONS.PUBLIC_CHAT.MATERIAL` in `menus.yml`. |
| `SETTINGS-MENU.BUTTONS.PUBLIC_CHAT.SLOT` | `int` | Any valid integer number | `'0'` | Configures the technical `SLOT` parameter for `SETTINGS-MENU.BUTTONS.PUBLIC_CHAT.SLOT` in `menus.yml`. |
| `SETTINGS-MENU.BUTTONS.PUBLIC_CHAT.LORE` | `list` | List of configured items/strings | `['&7Receive public chat messages', '&fCurrently: {status}']` | Configures the technical `LORE` parameter for `SETTINGS-MENU.BUTTONS.PUBLIC_CHAT.LORE` in `menus.yml`. |
| `SETTINGS-MENU.BUTTONS.PRIVATE_MESSAGES.DISPLAY-NAME` | `str` | Any string text | `'&#6BF18DPrivate Messages'` | Configures the technical `DISPLAY-NAME` parameter for `SETTINGS-MENU.BUTTONS.PRIVATE_MESSAGES.DISPLAY-NAME` in `menus.yml`. |
| `SETTINGS-MENU.BUTTONS.PRIVATE_MESSAGES.MATERIAL` | `str` | Any string text | `'SPRUCE_SIGN'` | Configures the technical `MATERIAL` parameter for `SETTINGS-MENU.BUTTONS.PRIVATE_MESSAGES.MATERIAL` in `menus.yml`. |
| `SETTINGS-MENU.BUTTONS.PRIVATE_MESSAGES.SLOT` | `int` | Any valid integer number | `'1'` | Configures the technical `SLOT` parameter for `SETTINGS-MENU.BUTTONS.PRIVATE_MESSAGES.SLOT` in `menus.yml`. |
| `SETTINGS-MENU.BUTTONS.PRIVATE_MESSAGES.LORE` | `list` | List of configured items/strings | `['&7Private messages privacy settings', '&fCurrently: {status}']` | Configures the technical `LORE` parameter for `SETTINGS-MENU.BUTTONS.PRIVATE_MESSAGES.LORE` in `menus.yml`. |
| `SETTINGS-MENU.BUTTONS.SERVER_BROADCASTS.DISPLAY-NAME` | `str` | Any string text | `'&#6BF18DServer Broadcasts'` | Configures the technical `DISPLAY-NAME` parameter for `SETTINGS-MENU.BUTTONS.SERVER_BROADCASTS.DISPLAY-NAME` in `menus.yml`. |
| `SETTINGS-MENU.BUTTONS.SERVER_BROADCASTS.MATERIAL` | `str` | Any string text | `'WARPED_SIGN'` | Configures the technical `MATERIAL` parameter for `SETTINGS-MENU.BUTTONS.SERVER_BROADCASTS.MATERIAL` in `menus.yml`. |
| `SETTINGS-MENU.BUTTONS.SERVER_BROADCASTS.SLOT` | `int` | Any valid integer number | `'2'` | Configures the technical `SLOT` parameter for `SETTINGS-MENU.BUTTONS.SERVER_BROADCASTS.SLOT` in `menus.yml`. |
| `SETTINGS-MENU.BUTTONS.SERVER_BROADCASTS.LORE` | `list` | List of configured items/strings | `['&7Receive server broadcasts', '&fCurrently: {status}']` | Configures the technical `LORE` parameter for `SETTINGS-MENU.BUTTONS.SERVER_BROADCASTS.LORE` in `menus.yml`. |
| `SETTINGS-MENU.BUTTONS.TEAM_CHAT_VISIBILITY.DISPLAY-NAME` | `str` | Any string text | `'&#6BF18DTeam Chat Visibility'` | Configures the technical `DISPLAY-NAME` parameter for `SETTINGS-MENU.BUTTONS.TEAM_CHAT_VISIBILITY.DISPLAY-NAME` in `menus.yml`. |
| `SETTINGS-MENU.BUTTONS.TEAM_CHAT_VISIBILITY.MATERIAL` | `str` | Any string text | `'DARK_OAK_SIGN'` | Configures the technical `MATERIAL` parameter for `SETTINGS-MENU.BUTTONS.TEAM_CHAT_VISIBILITY.MATERIAL` in `menus.yml`. |
| `SETTINGS-MENU.BUTTONS.TEAM_CHAT_VISIBILITY.SLOT` | `int` | Any valid integer number | `'3'` | Configures the technical `SLOT` parameter for `SETTINGS-MENU.BUTTONS.TEAM_CHAT_VISIBILITY.SLOT` in `menus.yml`. |
| `SETTINGS-MENU.BUTTONS.TEAM_CHAT_VISIBILITY.LORE` | `list` | List of configured items/strings | `['&7Show team chat in main chat', '&fCurrently: {status}']` | Configures the technical `LORE` parameter for `SETTINGS-MENU.BUTTONS.TEAM_CHAT_VISIBILITY.LORE` in `menus.yml`. |
| `SETTINGS-MENU.BUTTONS.LUNAR_TEAMMATES.DISPLAY-NAME` | `str` | Any string text | `'&#6BF18DLunar Teammates'` | Configures the technical `DISPLAY-NAME` parameter for `SETTINGS-MENU.BUTTONS.LUNAR_TEAMMATES.DISPLAY-NAME` in `menus.yml`. |
| `SETTINGS-MENU.BUTTONS.LUNAR_TEAMMATES.MATERIAL` | `str` | Any string text | `'BIRCH_SIGN'` | Configures the technical `MATERIAL` parameter for `SETTINGS-MENU.BUTTONS.LUNAR_TEAMMATES.MATERIAL` in `menus.yml`. |
| `SETTINGS-MENU.BUTTONS.LUNAR_TEAMMATES.SLOT` | `int` | Any valid integer number | `'4'` | Configures the technical `SLOT` parameter for `SETTINGS-MENU.BUTTONS.LUNAR_TEAMMATES.SLOT` in `menus.yml`. |
| `SETTINGS-MENU.BUTTONS.LUNAR_TEAMMATES.LORE` | `list` | List of configured items/strings | `['&7Show teammates on Lunar Client', '&fCurrently: {status}']` | Configures the technical `LORE` parameter for `SETTINGS-MENU.BUTTONS.LUNAR_TEAMMATES.LORE` in `menus.yml`. |
| `SETTINGS-MENU.BUTTONS.TPA_CONFIRM_MENUS.DISPLAY-NAME` | `str` | Any string text | `'&#6BF18DTpa Confirm Menus'` | Configures the technical `DISPLAY-NAME` parameter for `SETTINGS-MENU.BUTTONS.TPA_CONFIRM_MENUS.DISPLAY-NAME` in `menus.yml`. |
| `SETTINGS-MENU.BUTTONS.TPA_CONFIRM_MENUS.MATERIAL` | `str` | Any string text | `'JUNGLE_SIGN'` | Configures the technical `MATERIAL` parameter for `SETTINGS-MENU.BUTTONS.TPA_CONFIRM_MENUS.MATERIAL` in `menus.yml`. |
| `SETTINGS-MENU.BUTTONS.TPA_CONFIRM_MENUS.SLOT` | `int` | Any valid integer number | `'5'` | Configures the technical `SLOT` parameter for `SETTINGS-MENU.BUTTONS.TPA_CONFIRM_MENUS.SLOT` in `menus.yml`. |
| `SETTINGS-MENU.BUTTONS.TPA_CONFIRM_MENUS.LORE` | `list` | List of configured items/strings | `['&7Show confirmation GUI for TPA', '&fCurrently: {status}']` | Configures the technical `LORE` parameter for `SETTINGS-MENU.BUTTONS.TPA_CONFIRM_MENUS.LORE` in `menus.yml`. |
| `SETTINGS-MENU.BUTTONS.QUICK_AUCTION_PURCHASE.DISPLAY-NAME` | `str` | Any string text | `'&#6BF18DQuick Auction Purchase'` | Configures the technical `DISPLAY-NAME` parameter for `SETTINGS-MENU.BUTTONS.QUICK_AUCTION_PURCHASE.DISPLAY-NAME` in `menus.yml`. |
| `SETTINGS-MENU.BUTTONS.QUICK_AUCTION_PURCHASE.MATERIAL` | `str` | Any string text | `'GOLD_NUGGET'` | Configures the technical `MATERIAL` parameter for `SETTINGS-MENU.BUTTONS.QUICK_AUCTION_PURCHASE.MATERIAL` in `menus.yml`. |
| `SETTINGS-MENU.BUTTONS.QUICK_AUCTION_PURCHASE.SLOT` | `int` | Any valid integer number | `'9'` | Configures the technical `SLOT` parameter for `SETTINGS-MENU.BUTTONS.QUICK_AUCTION_PURCHASE.SLOT` in `menus.yml`. |
| `SETTINGS-MENU.BUTTONS.QUICK_AUCTION_PURCHASE.LORE` | `list` | List of configured items/strings | `['&7Fast buy auction items directly', '&fCurrently: {status}']` | Configures the technical `LORE` parameter for `SETTINGS-MENU.BUTTONS.QUICK_AUCTION_PURCHASE.LORE` in `menus.yml`. |
| *(124 additional sub-keys configured in section)* | | | | |

### 3. Practical Setup Example

```yaml
SETTINGS-MENU:
  TITLE: '&8Settings'
  SIZE: 54
  BUTTONS:
    # Every setting below accepts two optional keys:
    #   DEFAULT: <value>  Starting value for players who never touched the setting.
    #   ENABLED: false    Removes the option from /settings and pins every player to DEFAULT.
    # Example, hide advancement messages and keep them off for everyone:
    # ADVANCEMENT_MESSAGES:
    #   DEFAULT: OFF
    #   ENABLED: false
    # Custom redirect buttons can also be added here:
    # EXTERNAL_FLY:
    #   DISPLAY-NAME: '&bFlight Mode'
    #   MATERIAL: FEATHER
    #   SLOT: 25
    #   COMMAND: '[player] /fly'
    #   STATUS-PLACEHOLDER: '%cmi_user_flying%'
    #   LORE:
    #   - '&7Toggle flight via external plugin'
    #   - '&fCurrently: {status}'
    PUBLIC_CHAT:
      DISPLAY-NAME: '&#6BF18DPublic Chat'
      MATERIAL: OAK_SIGN
      SLOT: 0
      LORE:
      - '&7Receive public chat messages'
      - '&fCurrently: {status}'
    PRIVATE_MESSAGES:
      DISPLAY-NAME: '&#6BF18DPrivate Messages'
      MATERIAL: SPRUCE_SIGN
      SLOT: 1
      LORE:
      - '&7Private messages privacy settings'
      - '&fCurrently: {status}'
    SERVER_BROADCASTS:
      DISPLAY-NAME: '&#6BF18DServer Broadcasts'
      MATERIAL: WARPED_SIGN
      SLOT: 2
      LORE:
      - '&7Receive server broadcasts'
      - '&fCurrently: {status}'
    TEAM_CHAT_VISIBILITY:
    
```

### 4. Per-Setting Defaults & Removing Options

Every entry under `SETTINGS-MENU.BUTTONS` accepts two optional keys on top of the display keys
documented above.

| Key | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `SETTINGS-MENU.BUTTONS.<SETTING>.DEFAULT` | `str` / `bool` | `true`, `false`, `ANYONE`, `FRIENDS_FOLLOWED`, `OFF` | Built-in default (`true` / `ANYONE` for most settings) | Value a player starts with before they ever open `/settings`. |
| `SETTINGS-MENU.BUTTONS.<SETTING>.ENABLED` | `bool` | `true`, `false` | `true` | `false` removes the option from `/settings` and pins every player to `DEFAULT`. |

**Which values a setting accepts.** On/off buttons take `true` or `false` (`on`/`off`, `yes`/`no`
and `enabled`/`disabled` are accepted too). The privacy buttons — `PRIVATE_MESSAGES`,
`TPA_REQUESTS`, `TPA_HERE_REQUESTS`, `PAYMENTS`, `ADVANCEMENT_MESSAGES` and
`JOIN_LEAVE_MESSAGES` — take `ANYONE`, `FRIENDS_FOLLOWED` or `OFF`, and `DEATH_MESSAGES` takes
`FRIENDS_FOLLOWED` or `OFF`. On those buttons `true` is shorthand for `ANYONE`
(`FRIENDS_FOLLOWED` for `DEATH_MESSAGES`) and `false` is shorthand for `OFF`. `DISABLE_MOB_SPAWN`
and `DISABLE_PHANTOM_SPAWN` follow the button label, so `DEFAULT: true` means the prevention is
on and the mobs stop spawning. An unusable value is ignored and logged as a console warning.

**Turning a setting off for everyone.** `DEFAULT` alone only affects players who have never
touched that setting; existing players keep whatever they last chose. Pair it with
`ENABLED: false` to also pin players who already toggled it:

```yaml
SETTINGS-MENU:
  BUTTONS:
    ADVANCEMENT_MESSAGES:
      DEFAULT: OFF
      ENABLED: false
    JOIN_LEAVE_MESSAGES:
      DEFAULT: OFF
      ENABLED: false
```

Notes:

- Use `ENABLED: false` rather than deleting the block. Deleted blocks are restored from the
  bundled defaults the next time the plugin loads, which is also how new settings reach an
  existing `menus.yml`.
- While an option is disabled its `DEFAULT` is authoritative: the value is re-applied every time
  a player is loaded and on every `/uds reload`, and the button is neither drawn nor clickable.
  A player's stored choice is not deliberately rewritten, but routine data saves can overwrite
  it, so treat re-enabling an option as a reset for the players affected.
- `QUICK_AUCTION_PURCHASE` and `QUICK_AUCTION_SELL` are stored by the auction house instead of
  the player profile, so they support `ENABLED` but not `DEFAULT`.

---

## Section: `LEADERBOARDS-MENU`

### 1. Commented Setup Code Example

```yaml
LEADERBOARDS-MENU:
  TITLE: '&8Leaderboards'
  SIZE: 36
  TYPE-MENU:
    TITLE: '&8{type}'
    SIZE: 54
    BUTTON:
      MATERIAL: PLAYER_HEAD
      DISPLAY-NAME: '&#6BF18D{player}'
      LORE: '&f{type}: &7{value} &#6BF18D(#{position})'
  TYPE-NAMES:
    money: Money
    moneySpent: Money Spent
    moneyMade: Money Made
    kills: Kills
    deaths: Deaths
    playtime: Playtime
    blocksPlaced: Blocks Placed
    blocksBroken: Blocks Broken
    mobsKilled: Mobs Killed
    killStreak: Kill Streak
    highestKillStreak: Highest Kill Streak
    shards: Shards
    bounties: Bounties
  BUTTONS:
    MONEY:
      TYPE: money
      DISPLAY-NAME: '&#6BF18DMoney Leaderboard'
      MATERIAL: EMERALD
      SLOT: 10
      LORE:
      - '&fClick to view MONEY leaderboard'
    SHARDS:
      TYPE: shards
      DISPLAY-NAME: '&#6BF18DShards Leaderboard'
      MATERIAL: AMETHYST_SHARD
      SLOT: 11
      LORE:
      - '&fClick to view SHARDS leaderboard'
    KILLS:
      TYPE: kills
      DISPLAY-NAME: '&#6BF18DKills Leaderboard'
      MATERIAL: DIAMOND_SWORD
      SLOT: 12
      LORE:
      - '&fClick to view KILLS leaderboard'
    DEATHS:
      TYPE: deaths
      DISPLAY-NAME: '&#6BF18DDeaths Leaderboard'
      MATERIAL: SKELETON_SKULL
      SLOT: 13
      LORE:
      - '&fClick to view DEATHS leaderboard'
    PLAYTIME:
      TYPE: playtime
      DISPLAY-NAME: '&#6BF18DPlaytime Leaderboard'
      MATERIAL: CLOCK
      SLOT: 14
      LORE:
      - '&fClick to view PLAYTIME leaderboard'
    BLOCKS_PLACED:
      TYPE: blocksPlaced
      DISPLAY-NAME: '&#6BF18DBlocks Placed Leaderboard'
      MATERIAL: STONE
      SLOT: 15
      LORE:
      - '&fClick to view BLOCKS PLACED leaderboard'
    BLOCKS_BROKEN:
      TYPE: blocksBroken
      DISPLAY-NAME: '&#6BF18DBlocks Broken Leaderboard'
      MATERIAL: COBBLESTONE
      SLOT: 16
      LORE:
      - '&fClick to view BLOCKS BROKEN leaderboard'
    MOBS_KILLED:
      TYPE: mobsKilled
      DISPLAY-NAME: '&#6BF18DMobs Killed Leaderboard'
      MATERIAL: ZOMBIE_HEAD
      SLOT: 19
      LORE:
      - '&fClick to view MOBS KILLED leaderboard'
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `LEADERBOARDS-MENU.TITLE` | `str` | Any string text | `'&8Leaderboards'` | Configures the technical `TITLE` parameter for `LEADERBOARDS-MENU.TITLE` in `menus.yml`. |
| `LEADERBOARDS-MENU.SIZE` | `int` | Any valid integer number | `'36'` | Configures the technical `SIZE` parameter for `LEADERBOARDS-MENU.SIZE` in `menus.yml`. |
| `LEADERBOARDS-MENU.TYPE-MENU.TITLE` | `str` | Any string text | `'&8{type}'` | Configures the technical `TITLE` parameter for `LEADERBOARDS-MENU.TYPE-MENU.TITLE` in `menus.yml`. |
| `LEADERBOARDS-MENU.TYPE-MENU.SIZE` | `int` | Any valid integer number | `'54'` | Configures the technical `SIZE` parameter for `LEADERBOARDS-MENU.TYPE-MENU.SIZE` in `menus.yml`. |
| `LEADERBOARDS-MENU.TYPE-MENU.BUTTON.MATERIAL` | `str` | Any string text | `'PLAYER_HEAD'` | Configures the technical `MATERIAL` parameter for `LEADERBOARDS-MENU.TYPE-MENU.BUTTON.MATERIAL` in `menus.yml`. |
| `LEADERBOARDS-MENU.TYPE-MENU.BUTTON.DISPLAY-NAME` | `str` | Any string text | `'&#6BF18D{player}'` | Configures the technical `DISPLAY-NAME` parameter for `LEADERBOARDS-MENU.TYPE-MENU.BUTTON.DISPLAY-NAME` in `menus.yml`. |
| `LEADERBOARDS-MENU.TYPE-MENU.BUTTON.LORE` | `str` | Any string text | `'&f{type}: &7{value} &#6BF18D(#{posi...'` | Configures the technical `LORE` parameter for `LEADERBOARDS-MENU.TYPE-MENU.BUTTON.LORE` in `menus.yml`. |
| `LEADERBOARDS-MENU.TYPE-NAMES.money` | `str` | Any string text | `'Money'` | Configures the technical `money` parameter for `LEADERBOARDS-MENU.TYPE-NAMES.money` in `menus.yml`. |
| `LEADERBOARDS-MENU.TYPE-NAMES.moneySpent` | `str` | Any string text | `'Money Spent'` | Configures the technical `moneySpent` parameter for `LEADERBOARDS-MENU.TYPE-NAMES.moneySpent` in `menus.yml`. |
| `LEADERBOARDS-MENU.TYPE-NAMES.moneyMade` | `str` | Any string text | `'Money Made'` | Configures the technical `moneyMade` parameter for `LEADERBOARDS-MENU.TYPE-NAMES.moneyMade` in `menus.yml`. |
| `LEADERBOARDS-MENU.TYPE-NAMES.kills` | `str` | Any string text | `'Kills'` | Configures the technical `kills` parameter for `LEADERBOARDS-MENU.TYPE-NAMES.kills` in `menus.yml`. |
| `LEADERBOARDS-MENU.TYPE-NAMES.deaths` | `str` | Any string text | `'Deaths'` | Configures the technical `deaths` parameter for `LEADERBOARDS-MENU.TYPE-NAMES.deaths` in `menus.yml`. |
| `LEADERBOARDS-MENU.TYPE-NAMES.playtime` | `str` | Any string text | `'Playtime'` | Configures the technical `playtime` parameter for `LEADERBOARDS-MENU.TYPE-NAMES.playtime` in `menus.yml`. |
| `LEADERBOARDS-MENU.TYPE-NAMES.blocksPlaced` | `str` | Any string text | `'Blocks Placed'` | Configures the technical `blocksPlaced` parameter for `LEADERBOARDS-MENU.TYPE-NAMES.blocksPlaced` in `menus.yml`. |
| `LEADERBOARDS-MENU.TYPE-NAMES.blocksBroken` | `str` | Any string text | `'Blocks Broken'` | Configures the technical `blocksBroken` parameter for `LEADERBOARDS-MENU.TYPE-NAMES.blocksBroken` in `menus.yml`. |
| `LEADERBOARDS-MENU.TYPE-NAMES.mobsKilled` | `str` | Any string text | `'Mobs Killed'` | Configures the technical `mobsKilled` parameter for `LEADERBOARDS-MENU.TYPE-NAMES.mobsKilled` in `menus.yml`. |
| `LEADERBOARDS-MENU.TYPE-NAMES.killStreak` | `str` | Any string text | `'Kill Streak'` | Configures the technical `killStreak` parameter for `LEADERBOARDS-MENU.TYPE-NAMES.killStreak` in `menus.yml`. |
| `LEADERBOARDS-MENU.TYPE-NAMES.highestKillStreak` | `str` | Any string text | `'Highest Kill Streak'` | Configures the technical `highestKillStreak` parameter for `LEADERBOARDS-MENU.TYPE-NAMES.highestKillStreak` in `menus.yml`. |
| `LEADERBOARDS-MENU.TYPE-NAMES.shards` | `str` | Any string text | `'Shards'` | Configures the technical `shards` parameter for `LEADERBOARDS-MENU.TYPE-NAMES.shards` in `menus.yml`. |
| `LEADERBOARDS-MENU.TYPE-NAMES.bounties` | `str` | Any string text | `'Bounties'` | Configures the technical `bounties` parameter for `LEADERBOARDS-MENU.TYPE-NAMES.bounties` in `menus.yml`. |
| `LEADERBOARDS-MENU.BUTTONS.MONEY.TYPE` | `str` | Any string text | `'money'` | Configures the technical `TYPE` parameter for `LEADERBOARDS-MENU.BUTTONS.MONEY.TYPE` in `menus.yml`. |
| `LEADERBOARDS-MENU.BUTTONS.MONEY.DISPLAY-NAME` | `str` | Any string text | `'&#6BF18DMoney Leaderboard'` | Configures the technical `DISPLAY-NAME` parameter for `LEADERBOARDS-MENU.BUTTONS.MONEY.DISPLAY-NAME` in `menus.yml`. |
| `LEADERBOARDS-MENU.BUTTONS.MONEY.MATERIAL` | `str` | Any string text | `'EMERALD'` | Configures the technical `MATERIAL` parameter for `LEADERBOARDS-MENU.BUTTONS.MONEY.MATERIAL` in `menus.yml`. |
| `LEADERBOARDS-MENU.BUTTONS.MONEY.SLOT` | `int` | Any valid integer number | `'10'` | Configures the technical `SLOT` parameter for `LEADERBOARDS-MENU.BUTTONS.MONEY.SLOT` in `menus.yml`. |
| `LEADERBOARDS-MENU.BUTTONS.MONEY.LORE` | `list` | List of configured items/strings | `['&fClick to view MONEY leaderboard']` | Configures the technical `LORE` parameter for `LEADERBOARDS-MENU.BUTTONS.MONEY.LORE` in `menus.yml`. |
| `LEADERBOARDS-MENU.BUTTONS.SHARDS.TYPE` | `str` | Any string text | `'shards'` | Configures the technical `TYPE` parameter for `LEADERBOARDS-MENU.BUTTONS.SHARDS.TYPE` in `menus.yml`. |
| `LEADERBOARDS-MENU.BUTTONS.SHARDS.DISPLAY-NAME` | `str` | Any string text | `'&#6BF18DShards Leaderboard'` | Configures the technical `DISPLAY-NAME` parameter for `LEADERBOARDS-MENU.BUTTONS.SHARDS.DISPLAY-NAME` in `menus.yml`. |
| `LEADERBOARDS-MENU.BUTTONS.SHARDS.MATERIAL` | `str` | Any string text | `'AMETHYST_SHARD'` | Configures the technical `MATERIAL` parameter for `LEADERBOARDS-MENU.BUTTONS.SHARDS.MATERIAL` in `menus.yml`. |
| `LEADERBOARDS-MENU.BUTTONS.SHARDS.SLOT` | `int` | Any valid integer number | `'11'` | Configures the technical `SLOT` parameter for `LEADERBOARDS-MENU.BUTTONS.SHARDS.SLOT` in `menus.yml`. |
| `LEADERBOARDS-MENU.BUTTONS.SHARDS.LORE` | `list` | List of configured items/strings | `['&fClick to view SHARDS leaderboard']` | Configures the technical `LORE` parameter for `LEADERBOARDS-MENU.BUTTONS.SHARDS.LORE` in `menus.yml`. |
| `LEADERBOARDS-MENU.BUTTONS.KILLS.TYPE` | `str` | Any string text | `'kills'` | Configures the technical `TYPE` parameter for `LEADERBOARDS-MENU.BUTTONS.KILLS.TYPE` in `menus.yml`. |
| *(54 additional sub-keys configured in section)* | | | | |

### 3. Practical Setup Example

```yaml
LEADERBOARDS-MENU:
  TITLE: '&8Leaderboards'
  SIZE: 36
  TYPE-MENU:
    TITLE: '&8{type}'
    SIZE: 54
    BUTTON:
      MATERIAL: PLAYER_HEAD
      DISPLAY-NAME: '&#6BF18D{player}'
      LORE: '&f{type}: &7{value} &#6BF18D(#{position})'
  TYPE-NAMES:
    money: Money
    moneySpent: Money Spent
    moneyMade: Money Made
    kills: Kills
    deaths: Deaths
    playtime: Playtime
    blocksPlaced: Blocks Placed
    blocksBroken: Blocks Broken
    mobsKilled: Mobs Killed
    killStreak: Kill Streak
    highestKillStreak: Highest Kill Streak
    shards: Shards
    bounties: Bounties
  BUTTONS:
    MONEY:
      TYPE: money
      DISPLAY-NAME: '&#6BF18DMoney Leaderboard'
      MATERIAL: EMERALD
      SLOT: 10
      LORE:
      - '&fClick to view MONEY leaderboard'
    SHARDS:
      TYPE: shards
      DISPLAY-NAME: '&#6BF18DShards Leaderboard'
      MATERIAL: AMETHYST_SHARD
      SLOT: 11
      LORE:
      - '&fClick to view SHARDS leaderboard'
    KILLS:
      TYPE: kills
      DISPLAY-NAME: '&#6BF18DKills Leade
```

---

## Section: `PROGRESS-MENU`

### 1. Commented Setup Code Example

```yaml
PROGRESS-MENU:
  PROGRESS-BAR: "■"
  LEVEL:
  - 25000
  - 150000
  - 500000
  - 1000000
  - 5000000
  - 25000000
  - 250000000
  - 550000000
  - 850000000
  - 1000000000
  - 2000000000
  - 4000000000
  - 8000000000
  - 10000000000
  - 20000000000
  - 40000000000
  - 80000000000
  - 160000000000
  - 320000000000
  - 640000000000
  TITLE:
    CROPS: '&8CROPS PROGRESS'
    ORES: '&8ORE PROGRESS'
    MOBS: '&8MOB DROPS PROGRESS'
    NATURAL: '&8NATURAL ITEMS PROGRESS'
    ARMOR_AND_TOOLS: '&8ARMOR AND TOOLS PROGRESS'
    FISH: '&8FISH PROGRESS'
    BOOK: '&8ENCHANTED BOOK PROGRESS'
    POTIONS: '&8POTIONS PROGRESS'
    BLOCKS: '&8BLOCKS PROGRESS'
  TYPE-BUTTON:
    TITLE:
      CROPS: '&#6BF18DCROPS'
      ORES: '&#6BF18DORE'
      MOBS: '&#6BF18DMOB'
      NATURAL: '&#6BF18DNATURAL ITEMS'
      ARMOR_AND_TOOLS: '&#6BF18DARMOR AND TOOLS'
      FISH: '&#6BF18DFISH'
      BOOK: '&#6BF18DENCHANTED BOOK'
      POTIONS: '&#6BF18DPOTIONS'
      BLOCKS: '&#6BF18DBLOCKS'
    LORE:
      CROPS:
      - '&7Sell crops and farming materials to'
      - '&7upgrade your sell multiplier!'
      ORES:
      - '&7Sell ores and mining materials to'
      - '&7upgrade your sell multiplier!'
      MOBS:
      - '&7Sell mob drops and combat materials to'
      - '&7upgrade your sell multiplier!'
      NATURAL:
      - '&7Sell natural materials and trees to'
      - '&7upgrade your sell multiplier!'
      ARMOR_AND_TOOLS:
      - '&7Sell armor and tools to'
      - '&7upgrade your sell multiplier!'
      FISH:
      - '&7Sell fish and other fishing loot to'
      - '&7upgrade your sell multiplier!'
      BOOK:
      - '&7Sell books and enchanted books to'
      - '&7upgrade your sell multiplier!'
      POTIONS:
      - '&7Sell potions and brewing materials to'
      - '&7upgrade your sell multiplier!'
      BLOCKS:
      - '&7Sell blocks and placeable items to'
      - '&7upgrade your sell multiplier!'
    MATERIAL:
      CROPS: WHEAT
      ORES: DIAMOND
      MOBS: BONE
      NATURAL: OAK_LEAVES
      ARMOR_AND_TOOLS: NETHERITE_HELMET
      FISH: TROPICAL_FISH
      BOOK: BOOK
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `PROGRESS-MENU.PROGRESS-BAR` | `str` | Any string text | `'■'` | Configures the technical `PROGRESS-BAR` parameter for `PROGRESS-MENU.PROGRESS-BAR` in `menus.yml`. |
| `PROGRESS-MENU.LEVEL` | `list` | List of configured items/strings | `[25000, 150000, 500000...]` | Configures the technical `LEVEL` parameter for `PROGRESS-MENU.LEVEL` in `menus.yml`. |
| `PROGRESS-MENU.TITLE.CROPS` | `str` | Any string text | `'&8CROPS PROGRESS'` | Configures the technical `CROPS` parameter for `PROGRESS-MENU.TITLE.CROPS` in `menus.yml`. |
| `PROGRESS-MENU.TITLE.ORES` | `str` | Any string text | `'&8ORE PROGRESS'` | Configures the technical `ORES` parameter for `PROGRESS-MENU.TITLE.ORES` in `menus.yml`. |
| `PROGRESS-MENU.TITLE.MOBS` | `str` | Any string text | `'&8MOB DROPS PROGRESS'` | Configures the technical `MOBS` parameter for `PROGRESS-MENU.TITLE.MOBS` in `menus.yml`. |
| `PROGRESS-MENU.TITLE.NATURAL` | `str` | Any string text | `'&8NATURAL ITEMS PROGRESS'` | Configures the technical `NATURAL` parameter for `PROGRESS-MENU.TITLE.NATURAL` in `menus.yml`. |
| `PROGRESS-MENU.TITLE.ARMOR_AND_TOOLS` | `str` | Any string text | `'&8ARMOR AND TOOLS PROGRESS'` | Configures the technical `ARMOR_AND_TOOLS` parameter for `PROGRESS-MENU.TITLE.ARMOR_AND_TOOLS` in `menus.yml`. |
| `PROGRESS-MENU.TITLE.FISH` | `str` | Any string text | `'&8FISH PROGRESS'` | Configures the technical `FISH` parameter for `PROGRESS-MENU.TITLE.FISH` in `menus.yml`. |
| `PROGRESS-MENU.TITLE.BOOK` | `str` | Any string text | `'&8ENCHANTED BOOK PROGRESS'` | Configures the technical `BOOK` parameter for `PROGRESS-MENU.TITLE.BOOK` in `menus.yml`. |
| `PROGRESS-MENU.TITLE.POTIONS` | `str` | Any string text | `'&8POTIONS PROGRESS'` | Configures the technical `POTIONS` parameter for `PROGRESS-MENU.TITLE.POTIONS` in `menus.yml`. |
| `PROGRESS-MENU.TITLE.BLOCKS` | `str` | Any string text | `'&8BLOCKS PROGRESS'` | Configures the technical `BLOCKS` parameter for `PROGRESS-MENU.TITLE.BLOCKS` in `menus.yml`. |
| `PROGRESS-MENU.TYPE-BUTTON.TITLE.CROPS` | `str` | Any string text | `'&#6BF18DCROPS'` | Configures the technical `CROPS` parameter for `PROGRESS-MENU.TYPE-BUTTON.TITLE.CROPS` in `menus.yml`. |
| `PROGRESS-MENU.TYPE-BUTTON.TITLE.ORES` | `str` | Any string text | `'&#6BF18DORE'` | Configures the technical `ORES` parameter for `PROGRESS-MENU.TYPE-BUTTON.TITLE.ORES` in `menus.yml`. |
| `PROGRESS-MENU.TYPE-BUTTON.TITLE.MOBS` | `str` | Any string text | `'&#6BF18DMOB'` | Configures the technical `MOBS` parameter for `PROGRESS-MENU.TYPE-BUTTON.TITLE.MOBS` in `menus.yml`. |
| `PROGRESS-MENU.TYPE-BUTTON.TITLE.NATURAL` | `str` | Any string text | `'&#6BF18DNATURAL ITEMS'` | Configures the technical `NATURAL` parameter for `PROGRESS-MENU.TYPE-BUTTON.TITLE.NATURAL` in `menus.yml`. |
| `PROGRESS-MENU.TYPE-BUTTON.TITLE.ARMOR_AND_TOOLS` | `str` | Any string text | `'&#6BF18DARMOR AND TOOLS'` | Configures the technical `ARMOR_AND_TOOLS` parameter for `PROGRESS-MENU.TYPE-BUTTON.TITLE.ARMOR_AND_TOOLS` in `menus.yml`. |
| `PROGRESS-MENU.TYPE-BUTTON.TITLE.FISH` | `str` | Any string text | `'&#6BF18DFISH'` | Configures the technical `FISH` parameter for `PROGRESS-MENU.TYPE-BUTTON.TITLE.FISH` in `menus.yml`. |
| `PROGRESS-MENU.TYPE-BUTTON.TITLE.BOOK` | `str` | Any string text | `'&#6BF18DENCHANTED BOOK'` | Configures the technical `BOOK` parameter for `PROGRESS-MENU.TYPE-BUTTON.TITLE.BOOK` in `menus.yml`. |
| `PROGRESS-MENU.TYPE-BUTTON.TITLE.POTIONS` | `str` | Any string text | `'&#6BF18DPOTIONS'` | Configures the technical `POTIONS` parameter for `PROGRESS-MENU.TYPE-BUTTON.TITLE.POTIONS` in `menus.yml`. |
| `PROGRESS-MENU.TYPE-BUTTON.TITLE.BLOCKS` | `str` | Any string text | `'&#6BF18DBLOCKS'` | Configures the technical `BLOCKS` parameter for `PROGRESS-MENU.TYPE-BUTTON.TITLE.BLOCKS` in `menus.yml`. |
| `PROGRESS-MENU.TYPE-BUTTON.LORE.CROPS` | `list` | List of configured items/strings | `['&7Sell crops and farming materials to', '&7upgrade your sell multiplier!']` | Configures the technical `CROPS` parameter for `PROGRESS-MENU.TYPE-BUTTON.LORE.CROPS` in `menus.yml`. |
| `PROGRESS-MENU.TYPE-BUTTON.LORE.ORES` | `list` | List of configured items/strings | `['&7Sell ores and mining materials to', '&7upgrade your sell multiplier!']` | Configures the technical `ORES` parameter for `PROGRESS-MENU.TYPE-BUTTON.LORE.ORES` in `menus.yml`. |
| `PROGRESS-MENU.TYPE-BUTTON.LORE.MOBS` | `list` | List of configured items/strings | `['&7Sell mob drops and combat materials to', '&7upgrade your sell multiplier!']` | Configures the technical `MOBS` parameter for `PROGRESS-MENU.TYPE-BUTTON.LORE.MOBS` in `menus.yml`. |
| `PROGRESS-MENU.TYPE-BUTTON.LORE.NATURAL` | `list` | List of configured items/strings | `['&7Sell natural materials and trees to', '&7upgrade your sell multiplier!']` | Configures the technical `NATURAL` parameter for `PROGRESS-MENU.TYPE-BUTTON.LORE.NATURAL` in `menus.yml`. |
| `PROGRESS-MENU.TYPE-BUTTON.LORE.ARMOR_AND_TOOLS` | `list` | List of configured items/strings | `['&7Sell armor and tools to', '&7upgrade your sell multiplier!']` | Configures the technical `ARMOR_AND_TOOLS` parameter for `PROGRESS-MENU.TYPE-BUTTON.LORE.ARMOR_AND_TOOLS` in `menus.yml`. |
| `PROGRESS-MENU.TYPE-BUTTON.LORE.FISH` | `list` | List of configured items/strings | `['&7Sell fish and other fishing loot to', '&7upgrade your sell multiplier!']` | Configures the technical `FISH` parameter for `PROGRESS-MENU.TYPE-BUTTON.LORE.FISH` in `menus.yml`. |
| `PROGRESS-MENU.TYPE-BUTTON.LORE.BOOK` | `list` | List of configured items/strings | `['&7Sell books and enchanted books to', '&7upgrade your sell multiplier!']` | Configures the technical `BOOK` parameter for `PROGRESS-MENU.TYPE-BUTTON.LORE.BOOK` in `menus.yml`. |
| `PROGRESS-MENU.TYPE-BUTTON.LORE.POTIONS` | `list` | List of configured items/strings | `['&7Sell potions and brewing materials to', '&7upgrade your sell multiplier!']` | Configures the technical `POTIONS` parameter for `PROGRESS-MENU.TYPE-BUTTON.LORE.POTIONS` in `menus.yml`. |
| `PROGRESS-MENU.TYPE-BUTTON.LORE.BLOCKS` | `list` | List of configured items/strings | `['&7Sell blocks and placeable items to', '&7upgrade your sell multiplier!']` | Configures the technical `BLOCKS` parameter for `PROGRESS-MENU.TYPE-BUTTON.LORE.BLOCKS` in `menus.yml`. |
| `PROGRESS-MENU.TYPE-BUTTON.MATERIAL.CROPS` | `str` | Any string text | `'WHEAT'` | Configures the technical `CROPS` parameter for `PROGRESS-MENU.TYPE-BUTTON.MATERIAL.CROPS` in `menus.yml`. |
| *(14 additional sub-keys configured in section)* | | | | |

### 3. Practical Setup Example

```yaml
PROGRESS-MENU:
  PROGRESS-BAR: "■"
  LEVEL:
  - 25000
  - 150000
  - 500000
  - 1000000
  - 5000000
  - 25000000
  - 250000000
  - 550000000
  - 850000000
  - 1000000000
  - 2000000000
  - 4000000000
  - 8000000000
  - 10000000000
  - 20000000000
  - 40000000000
  - 80000000000
  - 160000000000
  - 320000000000
  - 640000000000
  TITLE:
    CROPS: '&8CROPS PROGRESS'
    ORES: '&8ORE PROGRESS'
    MOBS: '&8MOB DROPS PROGRESS'
    NATURAL: '&8NATURAL ITEMS PROGRESS'
    ARMOR_AND_TOOLS: '&8ARMOR AND TOOLS PROGRESS'
    FISH: '&8FISH PROGRESS'
    BOOK: '&8ENCHANTED BOOK PROGRESS'
    POTIONS: '&8POTIONS PROGRESS'
    BLOCKS: '&8BLOCKS PROGRESS'
  TYPE-BUTTON:
    TITLE:
      CROPS: '&#6BF18DCROPS'
      ORES: '&#6BF18DORE'
      MOBS: '&#6BF18DMOB'
      NATURAL: '&#6BF18DNATURAL ITEMS'
      ARMOR_AND_TOOLS: '&#6BF18DARMOR AND TOOLS'
      FISH: '&#6BF18DFISH'
      BOOK: '&#6BF18DENCHANTED BOOK'
      POTIONS: '&#6BF18DPOTIONS'
      BLOCKS: '&#6BF18DBLOCKS'
    LORE:
      CROPS:
   
```

---

## Section: `SELL-MENU`

### 1. Commented Setup Code Example

```yaml
SELL-MENU:
  TITLE: '&8Place Items In Here To Sell'
  MULTIPLIER-TITLE: '&8Sell Multipliers'
  CROPS-BUTTON:
    MATERIAL: WHEAT
    TITLE: '&#6BF18DCrops'
    LORE:
    - '&7Sell crops and farming materials to'
    - '&7upgrade your sell multiplier!'
    - ''
    - '&7Progress to &f{next_multiplier}'
    - '{porcentage_level} &#6BF18D{porcentage}%'
  ORES-BUTTON:
    MATERIAL: DIAMOND
    TITLE: '&#6BF18DOres'
    LORE:
    - '&7Sell ores and mining materials to'
    - '&7upgrade your sell multiplier!'
    - ''
    - '&7Progress to &f{next_multiplier}'
    - '{porcentage_level} &#6BF18D{porcentage}%'
  MOBS-BUTTON:
    MATERIAL: BONE
    TITLE: '&#6BF18DMobs'
    LORE:
    - '&7Sell mob drops and combat materials to'
    - '&7upgrade your sell multiplier!'
    - ''
    - '&7Progress to &f{next_multiplier}'
    - '{porcentage_level} &#6BF18D{porcentage}%'
  NATURAL-BUTTON:
    MATERIAL: OAK_LEAVES
    TITLE: '&#6BF18DNatural Items'
    LORE:
    - '&7Sell natural materials and trees to'
    - '&7upgrade your sell multiplier!'
    - ''
    - '&7Progress to &f{next_multiplier}'
    - '{porcentage_level} &#6BF18D{porcentage}%'
  ARMOR-AND-TOOLS-BUTTON:
    MATERIAL: NETHERITE_HELMET
    TITLE: '&#6BF18DArmor And Tools'
    LORE:
    - '&7Sell armor and tools to'
    - '&7upgrade your sell multiplier!'
    - ''
    - '&7Progress to &f{next_multiplier}'
    - '{porcentage_level} &#6BF18D{porcentage}%'
  FISH-BUTTON:
    MATERIAL: TROPICAL_FISH
    TITLE: '&#6BF18DFish'
    LORE:
    - '&7Sell fish and other fishing loot to'
    - '&7upgrade your sell multiplier!'
    - ''
    - '&7Progress to &f{next_multiplier}'
    - '{porcentage_level} &#6BF18D{porcentage}%'
  BOOK-BUTTON:
    MATERIAL: BOOK
    TITLE: '&#6BF18DEnchanted Book'
    LORE:
    - '&7Sell books and enchanted books to'
    - '&7upgrade your sell multiplier!'
    - ''
    - '&7Progress to &f{next_multiplier}'
    - '{porcentage_level} &#6BF18D{porcentage}%'
  POTIONS-BUTTON:
    MATERIAL: BREWING_STAND
    TITLE: '&#6BF18DPotions'
    LORE:
    - '&7Sell potions and brewing materials to'
    - '&7upgrade your sell multiplier!'
    - ''
    - '&7Progress to &f{next_multiplier}'
    - '{porcentage_level} &#6BF18D{porcentage}%'
  BLOCKS-BUTTON:
    MATERIAL: BRICK
    TITLE: '&#6BF18DBlocks'
    LORE:
    - '&7Sell blocks and placeable items to'
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `SELL-MENU.TITLE` | `str` | Any string text | `'&8Place Items In Here To Sell'` | Configures the technical `TITLE` parameter for `SELL-MENU.TITLE` in `menus.yml`. |
| `SELL-MENU.MULTIPLIER-TITLE` | `str` | Any string text | `'&8Sell Multipliers'` | Configures the technical `MULTIPLIER-TITLE` parameter for `SELL-MENU.MULTIPLIER-TITLE` in `menus.yml`. |
| `SELL-MENU.CROPS-BUTTON.MATERIAL` | `str` | Any string text | `'WHEAT'` | Configures the technical `MATERIAL` parameter for `SELL-MENU.CROPS-BUTTON.MATERIAL` in `menus.yml`. |
| `SELL-MENU.CROPS-BUTTON.TITLE` | `str` | Any string text | `'&#6BF18DCrops'` | Configures the technical `TITLE` parameter for `SELL-MENU.CROPS-BUTTON.TITLE` in `menus.yml`. |
| `SELL-MENU.CROPS-BUTTON.LORE` | `list` | List of configured items/strings | `[&7Sell crops and farming materials to, &7upgrade your sell multiplier!, ...]` | Configures the technical `LORE` parameter for `SELL-MENU.CROPS-BUTTON.LORE` in `menus.yml`. |
| `SELL-MENU.ORES-BUTTON.MATERIAL` | `str` | Any string text | `'DIAMOND'` | Configures the technical `MATERIAL` parameter for `SELL-MENU.ORES-BUTTON.MATERIAL` in `menus.yml`. |
| `SELL-MENU.ORES-BUTTON.TITLE` | `str` | Any string text | `'&#6BF18DOres'` | Configures the technical `TITLE` parameter for `SELL-MENU.ORES-BUTTON.TITLE` in `menus.yml`. |
| `SELL-MENU.ORES-BUTTON.LORE` | `list` | List of configured items/strings | `[&7Sell ores and mining materials to, &7upgrade your sell multiplier!, ...]` | Configures the technical `LORE` parameter for `SELL-MENU.ORES-BUTTON.LORE` in `menus.yml`. |
| `SELL-MENU.MOBS-BUTTON.MATERIAL` | `str` | Any string text | `'BONE'` | Configures the technical `MATERIAL` parameter for `SELL-MENU.MOBS-BUTTON.MATERIAL` in `menus.yml`. |
| `SELL-MENU.MOBS-BUTTON.TITLE` | `str` | Any string text | `'&#6BF18DMobs'` | Configures the technical `TITLE` parameter for `SELL-MENU.MOBS-BUTTON.TITLE` in `menus.yml`. |
| `SELL-MENU.MOBS-BUTTON.LORE` | `list` | List of configured items/strings | `[&7Sell mob drops and combat materials to, &7upgrade your sell multiplier!, ...]` | Configures the technical `LORE` parameter for `SELL-MENU.MOBS-BUTTON.LORE` in `menus.yml`. |
| `SELL-MENU.NATURAL-BUTTON.MATERIAL` | `str` | Any string text | `'OAK_LEAVES'` | Configures the technical `MATERIAL` parameter for `SELL-MENU.NATURAL-BUTTON.MATERIAL` in `menus.yml`. |
| `SELL-MENU.NATURAL-BUTTON.TITLE` | `str` | Any string text | `'&#6BF18DNatural Items'` | Configures the technical `TITLE` parameter for `SELL-MENU.NATURAL-BUTTON.TITLE` in `menus.yml`. |
| `SELL-MENU.NATURAL-BUTTON.LORE` | `list` | List of configured items/strings | `[&7Sell natural materials and trees to, &7upgrade your sell multiplier!, ...]` | Configures the technical `LORE` parameter for `SELL-MENU.NATURAL-BUTTON.LORE` in `menus.yml`. |
| `SELL-MENU.ARMOR-AND-TOOLS-BUTTON.MATERIAL` | `str` | Any string text | `'NETHERITE_HELMET'` | Configures the technical `MATERIAL` parameter for `SELL-MENU.ARMOR-AND-TOOLS-BUTTON.MATERIAL` in `menus.yml`. |
| `SELL-MENU.ARMOR-AND-TOOLS-BUTTON.TITLE` | `str` | Any string text | `'&#6BF18DArmor And Tools'` | Configures the technical `TITLE` parameter for `SELL-MENU.ARMOR-AND-TOOLS-BUTTON.TITLE` in `menus.yml`. |
| `SELL-MENU.ARMOR-AND-TOOLS-BUTTON.LORE` | `list` | List of configured items/strings | `[&7Sell armor and tools to, &7upgrade your sell multiplier!, ...]` | Configures the technical `LORE` parameter for `SELL-MENU.ARMOR-AND-TOOLS-BUTTON.LORE` in `menus.yml`. |
| `SELL-MENU.FISH-BUTTON.MATERIAL` | `str` | Any string text | `'TROPICAL_FISH'` | Configures the technical `MATERIAL` parameter for `SELL-MENU.FISH-BUTTON.MATERIAL` in `menus.yml`. |
| `SELL-MENU.FISH-BUTTON.TITLE` | `str` | Any string text | `'&#6BF18DFish'` | Configures the technical `TITLE` parameter for `SELL-MENU.FISH-BUTTON.TITLE` in `menus.yml`. |
| `SELL-MENU.FISH-BUTTON.LORE` | `list` | List of configured items/strings | `[&7Sell fish and other fishing loot to, &7upgrade your sell multiplier!, ...]` | Configures the technical `LORE` parameter for `SELL-MENU.FISH-BUTTON.LORE` in `menus.yml`. |
| `SELL-MENU.BOOK-BUTTON.MATERIAL` | `str` | Any string text | `'BOOK'` | Configures the technical `MATERIAL` parameter for `SELL-MENU.BOOK-BUTTON.MATERIAL` in `menus.yml`. |
| `SELL-MENU.BOOK-BUTTON.TITLE` | `str` | Any string text | `'&#6BF18DEnchanted Book'` | Configures the technical `TITLE` parameter for `SELL-MENU.BOOK-BUTTON.TITLE` in `menus.yml`. |
| `SELL-MENU.BOOK-BUTTON.LORE` | `list` | List of configured items/strings | `[&7Sell books and enchanted books to, &7upgrade your sell multiplier!, ...]` | Configures the technical `LORE` parameter for `SELL-MENU.BOOK-BUTTON.LORE` in `menus.yml`. |
| `SELL-MENU.POTIONS-BUTTON.MATERIAL` | `str` | Any string text | `'BREWING_STAND'` | Configures the technical `MATERIAL` parameter for `SELL-MENU.POTIONS-BUTTON.MATERIAL` in `menus.yml`. |
| `SELL-MENU.POTIONS-BUTTON.TITLE` | `str` | Any string text | `'&#6BF18DPotions'` | Configures the technical `TITLE` parameter for `SELL-MENU.POTIONS-BUTTON.TITLE` in `menus.yml`. |
| `SELL-MENU.POTIONS-BUTTON.LORE` | `list` | List of configured items/strings | `[&7Sell potions and brewing materials to, &7upgrade your sell multiplier!, ...]` | Configures the technical `LORE` parameter for `SELL-MENU.POTIONS-BUTTON.LORE` in `menus.yml`. |
| `SELL-MENU.BLOCKS-BUTTON.MATERIAL` | `str` | Any string text | `'BRICK'` | Configures the technical `MATERIAL` parameter for `SELL-MENU.BLOCKS-BUTTON.MATERIAL` in `menus.yml`. |
| `SELL-MENU.BLOCKS-BUTTON.TITLE` | `str` | Any string text | `'&#6BF18DBlocks'` | Configures the technical `TITLE` parameter for `SELL-MENU.BLOCKS-BUTTON.TITLE` in `menus.yml`. |
| `SELL-MENU.BLOCKS-BUTTON.LORE` | `list` | List of configured items/strings | `[&7Sell blocks and placeable items to, &7upgrade your sell multiplier!, ...]` | Configures the technical `LORE` parameter for `SELL-MENU.BLOCKS-BUTTON.LORE` in `menus.yml`. |

### 3. Practical Setup Example

```yaml
SELL-MENU:
  TITLE: '&8Place Items In Here To Sell'
  MULTIPLIER-TITLE: '&8Sell Multipliers'
  CROPS-BUTTON:
    MATERIAL: WHEAT
    TITLE: '&#6BF18DCrops'
    LORE:
    - '&7Sell crops and farming materials to'
    - '&7upgrade your sell multiplier!'
    - ''
    - '&7Progress to &f{next_multiplier}'
    - '{porcentage_level} &#6BF18D{porcentage}%'
  ORES-BUTTON:
    MATERIAL: DIAMOND
    TITLE: '&#6BF18DOres'
    LORE:
    - '&7Sell ores and mining materials to'
    - '&7upgrade your sell multiplier!'
    - ''
    - '&7Progress to &f{next_multiplier}'
    - '{porcentage_level} &#6BF18D{porcentage}%'
  MOBS-BUTTON:
    MATERIAL: BONE
    TITLE: '&#6BF18DMobs'
    LORE:
    - '&7Sell mob drops and combat materials to'
    - '&7upgrade your sell multiplier!'
    - ''
    - '&7Progress to &f{next_multiplier}'
    - '{porcentage_level} &#6BF18D{porcentage}%'
  NATURAL-BUTTON:
    MATERIAL: OAK_LEAVES
    TITLE: '&#6BF18DNatural Items'
    LORE:
    - '&7Sell natural materials and trees to
```

---

## Section: `WORTH-MENU`

### 1. Commented Setup Code Example

```yaml
WORTH-MENU:
  TITLE: '&8Item Prices'
  FORMAT: '&7Worth: &a${price}'
  SORT-BUTTON:
    TITLE: '&aSort'
    MATERIAL: CAULDRON
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `WORTH-MENU.TITLE` | `str` | Any string text | `'&8Item Prices'` | Configures the technical `TITLE` parameter for `WORTH-MENU.TITLE` in `menus.yml`. |
| `WORTH-MENU.FORMAT` | `str` | Any string text | `'&7Worth: &a${price}'` | Configures the technical `FORMAT` parameter for `WORTH-MENU.FORMAT` in `menus.yml`. |
| `WORTH-MENU.SORT-BUTTON.TITLE` | `str` | Any string text | `'&aSort'` | Configures the technical `TITLE` parameter for `WORTH-MENU.SORT-BUTTON.TITLE` in `menus.yml`. |
| `WORTH-MENU.SORT-BUTTON.MATERIAL` | `str` | Any string text | `'CAULDRON'` | Configures the technical `MATERIAL` parameter for `WORTH-MENU.SORT-BUTTON.MATERIAL` in `menus.yml`. |

### 3. Practical Setup Example

```yaml
WORTH-MENU:
  TITLE: '&8Item Prices'
  FORMAT: '&7Worth: &a${price}'
  SORT-BUTTON:
    TITLE: '&aSort'
    MATERIAL: CAULDRON
```

---

## Section: `TPA-CONFIRM-MENU`

### 1. Commented Setup Code Example

```yaml
TPA-CONFIRM-MENU:
  TITLE: '&8Confirm TPA {here}'
  SIZE: 27
  BUTTONS:
    CANCEL:
      MATERIAL: RED_STAINED_GLASS_PANE
      NAME: '&cCancel'
      LORE:
      - '&fCLICK TO CANCEL'
    CONFIRM:
      MATERIAL: LIME_STAINED_GLASS_PANE
      NAME: '&aConfirm'
      LORE:
      - '&fCLICK TO CONFIRM'
    PLAYER:
      MATERIAL: PLAYER_HEAD
      NAME: '&#00FC00Player'
      LORE:
      - '&7{player}'
    LOCATION:
      NAME: '&#6BF18DLocation'
      MATERIAL: GRASS_BLOCK
      LORE:
      - '&7{world}'
    REGION:
      NAME: '&#6BF18DRegion'
      MATERIAL: FEATHER
      LORE:
      - '&7NA East (&#0069D6${ping}ms&7)'
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `TPA-CONFIRM-MENU.TITLE` | `str` | Any string text | `'&8Confirm TPA {here}'` | Configures the technical `TITLE` parameter for `TPA-CONFIRM-MENU.TITLE` in `menus.yml`. |
| `TPA-CONFIRM-MENU.SIZE` | `int` | Any valid integer number | `'27'` | Configures the technical `SIZE` parameter for `TPA-CONFIRM-MENU.SIZE` in `menus.yml`. |
| `TPA-CONFIRM-MENU.BUTTONS.CANCEL.MATERIAL` | `str` | Any string text | `'RED_STAINED_GLASS_PANE'` | Configures the technical `MATERIAL` parameter for `TPA-CONFIRM-MENU.BUTTONS.CANCEL.MATERIAL` in `menus.yml`. |
| `TPA-CONFIRM-MENU.BUTTONS.CANCEL.NAME` | `str` | Any string text | `'&cCancel'` | Configures the technical `NAME` parameter for `TPA-CONFIRM-MENU.BUTTONS.CANCEL.NAME` in `menus.yml`. |
| `TPA-CONFIRM-MENU.BUTTONS.CANCEL.LORE` | `list` | List of configured items/strings | `['&fCLICK TO CANCEL']` | Configures the technical `LORE` parameter for `TPA-CONFIRM-MENU.BUTTONS.CANCEL.LORE` in `menus.yml`. |
| `TPA-CONFIRM-MENU.BUTTONS.CONFIRM.MATERIAL` | `str` | Any string text | `'LIME_STAINED_GLASS_PANE'` | Configures the technical `MATERIAL` parameter for `TPA-CONFIRM-MENU.BUTTONS.CONFIRM.MATERIAL` in `menus.yml`. |
| `TPA-CONFIRM-MENU.BUTTONS.CONFIRM.NAME` | `str` | Any string text | `'&aConfirm'` | Configures the technical `NAME` parameter for `TPA-CONFIRM-MENU.BUTTONS.CONFIRM.NAME` in `menus.yml`. |
| `TPA-CONFIRM-MENU.BUTTONS.CONFIRM.LORE` | `list` | List of configured items/strings | `['&fCLICK TO CONFIRM']` | Configures the technical `LORE` parameter for `TPA-CONFIRM-MENU.BUTTONS.CONFIRM.LORE` in `menus.yml`. |
| `TPA-CONFIRM-MENU.BUTTONS.PLAYER.MATERIAL` | `str` | Any string text | `'PLAYER_HEAD'` | Configures the technical `MATERIAL` parameter for `TPA-CONFIRM-MENU.BUTTONS.PLAYER.MATERIAL` in `menus.yml`. |
| `TPA-CONFIRM-MENU.BUTTONS.PLAYER.NAME` | `str` | Any string text | `'&#00FC00Player'` | Configures the technical `NAME` parameter for `TPA-CONFIRM-MENU.BUTTONS.PLAYER.NAME` in `menus.yml`. |
| `TPA-CONFIRM-MENU.BUTTONS.PLAYER.LORE` | `list` | List of configured items/strings | `['&7{player}']` | Configures the technical `LORE` parameter for `TPA-CONFIRM-MENU.BUTTONS.PLAYER.LORE` in `menus.yml`. |
| `TPA-CONFIRM-MENU.BUTTONS.LOCATION.NAME` | `str` | Any string text | `'&#6BF18DLocation'` | Configures the technical `NAME` parameter for `TPA-CONFIRM-MENU.BUTTONS.LOCATION.NAME` in `menus.yml`. |
| `TPA-CONFIRM-MENU.BUTTONS.LOCATION.MATERIAL` | `str` | Any string text | `'GRASS_BLOCK'` | Configures the technical `MATERIAL` parameter for `TPA-CONFIRM-MENU.BUTTONS.LOCATION.MATERIAL` in `menus.yml`. |
| `TPA-CONFIRM-MENU.BUTTONS.LOCATION.LORE` | `list` | List of configured items/strings | `['&7{world}']` | Configures the technical `LORE` parameter for `TPA-CONFIRM-MENU.BUTTONS.LOCATION.LORE` in `menus.yml`. |
| `TPA-CONFIRM-MENU.BUTTONS.REGION.NAME` | `str` | Any string text | `'&#6BF18DRegion'` | Configures the technical `NAME` parameter for `TPA-CONFIRM-MENU.BUTTONS.REGION.NAME` in `menus.yml`. |
| `TPA-CONFIRM-MENU.BUTTONS.REGION.MATERIAL` | `str` | Any string text | `'FEATHER'` | Configures the technical `MATERIAL` parameter for `TPA-CONFIRM-MENU.BUTTONS.REGION.MATERIAL` in `menus.yml`. |
| `TPA-CONFIRM-MENU.BUTTONS.REGION.LORE` | `list` | List of configured items/strings | `['&7NA East (&#0069D6${ping}ms&7)']` | Configures the technical `LORE` parameter for `TPA-CONFIRM-MENU.BUTTONS.REGION.LORE` in `menus.yml`. |

### 3. Practical Setup Example

```yaml
TPA-CONFIRM-MENU:
  TITLE: '&8Confirm TPA {here}'
  SIZE: 27
  BUTTONS:
    CANCEL:
      MATERIAL: RED_STAINED_GLASS_PANE
      NAME: '&cCancel'
      LORE:
      - '&fCLICK TO CANCEL'
    CONFIRM:
      MATERIAL: LIME_STAINED_GLASS_PANE
      NAME: '&aConfirm'
      LORE:
      - '&fCLICK TO CONFIRM'
    PLAYER:
      MATERIAL: PLAYER_HEAD
      NAME: '&#00FC00Player'
      LORE:
      - '&7{player}'
    LOCATION:
      NAME: '&#6BF18DLocation'
      MATERIAL: GRASS_BLOCK
      LORE:
      - '&7{world}'
    REGION:
      NAME: '&#6BF18DRegion'
      MATERIAL: FEATHER
      LORE:
      - '&7NA East (&#0069D6${ping}ms&7)'
```

---

## Section: `BOUNTIES-MENU`

### 1. Commented Setup Code Example

```yaml
BOUNTIES-MENU:
  TITLE: '&8Bounties'
  SIZE: 54
  MAX-ITEMS-PER-PAGE: 45
  BOUNTY-BUTTON:
    MATERIAL: PLAYER_HEAD
    NAME: '&#6BF18D{player}'
    LORE:
    - '&fBounty: &7${price}'
  REFRESH-BUTTON:
    SLOT: 49
    MATERIAL: SKELETON_SKULL
    NAME: '&#6BF18DBounties'
    LORE:
    - '&fClick to refresh'
    - ''
    - '&7Set a bounty using this:'
    - '&7/bounty add (player) (amount)'
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `BOUNTIES-MENU.TITLE` | `str` | Any string text | `'&8Bounties'` | Configures the technical `TITLE` parameter for `BOUNTIES-MENU.TITLE` in `menus.yml`. |
| `BOUNTIES-MENU.SIZE` | `int` | Any valid integer number | `'54'` | Configures the technical `SIZE` parameter for `BOUNTIES-MENU.SIZE` in `menus.yml`. |
| `BOUNTIES-MENU.MAX-ITEMS-PER-PAGE` | `int` | Any valid integer number | `'45'` | Configures the technical `MAX-ITEMS-PER-PAGE` parameter for `BOUNTIES-MENU.MAX-ITEMS-PER-PAGE` in `menus.yml`. |
| `BOUNTIES-MENU.BOUNTY-BUTTON.MATERIAL` | `str` | Any string text | `'PLAYER_HEAD'` | Configures the technical `MATERIAL` parameter for `BOUNTIES-MENU.BOUNTY-BUTTON.MATERIAL` in `menus.yml`. |
| `BOUNTIES-MENU.BOUNTY-BUTTON.NAME` | `str` | Any string text | `'&#6BF18D{player}'` | Configures the technical `NAME` parameter for `BOUNTIES-MENU.BOUNTY-BUTTON.NAME` in `menus.yml`. |
| `BOUNTIES-MENU.BOUNTY-BUTTON.LORE` | `list` | List of configured items/strings | `['&fBounty: &7${price}']` | Configures the technical `LORE` parameter for `BOUNTIES-MENU.BOUNTY-BUTTON.LORE` in `menus.yml`. |
| `BOUNTIES-MENU.REFRESH-BUTTON.SLOT` | `int` | Any valid integer number | `'49'` | Configures the technical `SLOT` parameter for `BOUNTIES-MENU.REFRESH-BUTTON.SLOT` in `menus.yml`. |
| `BOUNTIES-MENU.REFRESH-BUTTON.MATERIAL` | `str` | Any string text | `'SKELETON_SKULL'` | Configures the technical `MATERIAL` parameter for `BOUNTIES-MENU.REFRESH-BUTTON.MATERIAL` in `menus.yml`. |
| `BOUNTIES-MENU.REFRESH-BUTTON.NAME` | `str` | Any string text | `'&#6BF18DBounties'` | Configures the technical `NAME` parameter for `BOUNTIES-MENU.REFRESH-BUTTON.NAME` in `menus.yml`. |
| `BOUNTIES-MENU.REFRESH-BUTTON.LORE` | `list` | List of configured items/strings | `[&fClick to refresh, , &7Set a bounty using this:...]` | Configures the technical `LORE` parameter for `BOUNTIES-MENU.REFRESH-BUTTON.LORE` in `menus.yml`. |

### 3. Practical Setup Example

```yaml
BOUNTIES-MENU:
  TITLE: '&8Bounties'
  SIZE: 54
  MAX-ITEMS-PER-PAGE: 45
  BOUNTY-BUTTON:
    MATERIAL: PLAYER_HEAD
    NAME: '&#6BF18D{player}'
    LORE:
    - '&fBounty: &7${price}'
  REFRESH-BUTTON:
    SLOT: 49
    MATERIAL: SKELETON_SKULL
    NAME: '&#6BF18DBounties'
    LORE:
    - '&fClick to refresh'
    - ''
    - '&7Set a bounty using this:'
    - '&7/bounty add (player) (amount)'
```

---

## Section: `BOUNTY-CONFIRM-MENU`

### 1. Commented Setup Code Example

```yaml
BOUNTY-CONFIRM-MENU:
  TITLE: '&8Confirm Bounty'
  SIZE: 27
  CANCEL-BUTTON:
    MATERIAL: RED_STAINED_GLASS_PANE
    NAME: '&#FC0000Cancel'
    LORE:
    - '&7Click to cancel the bounty adding!'
  PLAYER-BUTTON:
    MATERIAL: PLAYER_HEAD
    NAME: '&#00FC00{player}'
    LORE:
    - '&fYou''re going to set &#00FC00{amount}'
  CONFIRM-BUTTON:
    MATERIAL: LIME_STAINED_GLASS_PANE
    NAME: '&#00FC00Confirm'
    LORE:
    - '&7Click to confirm to add {amount} bounty!'
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `BOUNTY-CONFIRM-MENU.TITLE` | `str` | Any string text | `'&8Confirm Bounty'` | Configures the technical `TITLE` parameter for `BOUNTY-CONFIRM-MENU.TITLE` in `menus.yml`. |
| `BOUNTY-CONFIRM-MENU.SIZE` | `int` | Any valid integer number | `'27'` | Configures the technical `SIZE` parameter for `BOUNTY-CONFIRM-MENU.SIZE` in `menus.yml`. |
| `BOUNTY-CONFIRM-MENU.CANCEL-BUTTON.MATERIAL` | `str` | Any string text | `'RED_STAINED_GLASS_PANE'` | Configures the technical `MATERIAL` parameter for `BOUNTY-CONFIRM-MENU.CANCEL-BUTTON.MATERIAL` in `menus.yml`. |
| `BOUNTY-CONFIRM-MENU.CANCEL-BUTTON.NAME` | `str` | Any string text | `'&#FC0000Cancel'` | Configures the technical `NAME` parameter for `BOUNTY-CONFIRM-MENU.CANCEL-BUTTON.NAME` in `menus.yml`. |
| `BOUNTY-CONFIRM-MENU.CANCEL-BUTTON.LORE` | `list` | List of configured items/strings | `['&7Click to cancel the bounty adding!']` | Configures the technical `LORE` parameter for `BOUNTY-CONFIRM-MENU.CANCEL-BUTTON.LORE` in `menus.yml`. |
| `BOUNTY-CONFIRM-MENU.PLAYER-BUTTON.MATERIAL` | `str` | Any string text | `'PLAYER_HEAD'` | Configures the technical `MATERIAL` parameter for `BOUNTY-CONFIRM-MENU.PLAYER-BUTTON.MATERIAL` in `menus.yml`. |
| `BOUNTY-CONFIRM-MENU.PLAYER-BUTTON.NAME` | `str` | Any string text | `'&#00FC00{player}'` | Configures the technical `NAME` parameter for `BOUNTY-CONFIRM-MENU.PLAYER-BUTTON.NAME` in `menus.yml`. |
| `BOUNTY-CONFIRM-MENU.PLAYER-BUTTON.LORE` | `list` | List of configured items/strings | `["&fYou're going to set &#00FC00{amount}"]` | Configures the technical `LORE` parameter for `BOUNTY-CONFIRM-MENU.PLAYER-BUTTON.LORE` in `menus.yml`. |
| `BOUNTY-CONFIRM-MENU.CONFIRM-BUTTON.MATERIAL` | `str` | Any string text | `'LIME_STAINED_GLASS_PANE'` | Configures the technical `MATERIAL` parameter for `BOUNTY-CONFIRM-MENU.CONFIRM-BUTTON.MATERIAL` in `menus.yml`. |
| `BOUNTY-CONFIRM-MENU.CONFIRM-BUTTON.NAME` | `str` | Any string text | `'&#00FC00Confirm'` | Configures the technical `NAME` parameter for `BOUNTY-CONFIRM-MENU.CONFIRM-BUTTON.NAME` in `menus.yml`. |
| `BOUNTY-CONFIRM-MENU.CONFIRM-BUTTON.LORE` | `list` | List of configured items/strings | `['&7Click to confirm to add {amount} bounty!']` | Configures the technical `LORE` parameter for `BOUNTY-CONFIRM-MENU.CONFIRM-BUTTON.LORE` in `menus.yml`. |

### 3. Practical Setup Example

```yaml
BOUNTY-CONFIRM-MENU:
  TITLE: '&8Confirm Bounty'
  SIZE: 27
  CANCEL-BUTTON:
    MATERIAL: RED_STAINED_GLASS_PANE
    NAME: '&#FC0000Cancel'
    LORE:
    - '&7Click to cancel the bounty adding!'
  PLAYER-BUTTON:
    MATERIAL: PLAYER_HEAD
    NAME: '&#00FC00{player}'
    LORE:
    - '&fYou''re going to set &#00FC00{amount}'
  CONFIRM-BUTTON:
    MATERIAL: LIME_STAINED_GLASS_PANE
    NAME: '&#00FC00Confirm'
    LORE:
    - '&7Click to confirm to add {amount} bounty!'
```

---

## Section: `SELL-HISTORY-MENU`

### 1. Commented Setup Code Example

```yaml
SELL-HISTORY-MENU:
  TITLE: '&8Sell History'
  SIZE: 54
  MAX-ITEMS-PER-PAGE: 45
  BUTTONS:
    SORT:
      MATERIAL: ANVIL
      NAME: '&aSort'
      LORE:
      - '&fClick to sort'
      - ''
      - '&7({sort_state})'
      SLOT: 49
    MATERIAL-ITEM:
      LORE:
      - '&fTotal price: &a{price}'
      - '&fTotal amount: {amount}'
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `SELL-HISTORY-MENU.TITLE` | `str` | Any string text | `'&8Sell History'` | Configures the technical `TITLE` parameter for `SELL-HISTORY-MENU.TITLE` in `menus.yml`. |
| `SELL-HISTORY-MENU.SIZE` | `int` | Any valid integer number | `'54'` | Configures the technical `SIZE` parameter for `SELL-HISTORY-MENU.SIZE` in `menus.yml`. |
| `SELL-HISTORY-MENU.MAX-ITEMS-PER-PAGE` | `int` | Any valid integer number | `'45'` | Configures the technical `MAX-ITEMS-PER-PAGE` parameter for `SELL-HISTORY-MENU.MAX-ITEMS-PER-PAGE` in `menus.yml`. |
| `SELL-HISTORY-MENU.BUTTONS.SORT.MATERIAL` | `str` | Any string text | `'ANVIL'` | Configures the technical `MATERIAL` parameter for `SELL-HISTORY-MENU.BUTTONS.SORT.MATERIAL` in `menus.yml`. |
| `SELL-HISTORY-MENU.BUTTONS.SORT.NAME` | `str` | Any string text | `'&aSort'` | Configures the technical `NAME` parameter for `SELL-HISTORY-MENU.BUTTONS.SORT.NAME` in `menus.yml`. |
| `SELL-HISTORY-MENU.BUTTONS.SORT.LORE` | `list` | List of configured items/strings | `['&fClick to sort', '', '&7({sort_state})']` | Configures the technical `LORE` parameter for `SELL-HISTORY-MENU.BUTTONS.SORT.LORE` in `menus.yml`. |
| `SELL-HISTORY-MENU.BUTTONS.SORT.SLOT` | `int` | Any valid integer number | `'49'` | Configures the technical `SLOT` parameter for `SELL-HISTORY-MENU.BUTTONS.SORT.SLOT` in `menus.yml`. |
| `SELL-HISTORY-MENU.BUTTONS.MATERIAL-ITEM.LORE` | `list` | List of configured items/strings | `['&fTotal price: &a{price}', '&fTotal amount: {amount}']` | Configures the technical `LORE` parameter for `SELL-HISTORY-MENU.BUTTONS.MATERIAL-ITEM.LORE` in `menus.yml`. |

### 3. Practical Setup Example

```yaml
SELL-HISTORY-MENU:
  TITLE: '&8Sell History'
  SIZE: 54
  MAX-ITEMS-PER-PAGE: 45
  BUTTONS:
    SORT:
      MATERIAL: ANVIL
      NAME: '&aSort'
      LORE:
      - '&fClick to sort'
      - ''
      - '&7({sort_state})'
      SLOT: 49
    MATERIAL-ITEM:
      LORE:
      - '&fTotal price: &a{price}'
      - '&fTotal amount: {amount}'
```

---

## Section: `BILLFORD-MENU`

### 1. Commented Setup Code Example

```yaml
BILLFORD-MENU:
  TITLE: '&8Billford'
  SIZE: 54
  TRADE-BUTTON:
    MATERIAL:
      NAME: '&aTrade with Billford'
      LORE:
      - ''
  CONFIRM-TRADE-BUTTON:
    MATERIAL: HOPPER
    NAME: '&8Trade'
    LORE:
    - '&fClick to confirm the trade'
    - ''
    - '&7(you need the items in your inventory)'
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `BILLFORD-MENU.TITLE` | `str` | Any string text | `'&8Billford'` | Configures the technical `TITLE` parameter for `BILLFORD-MENU.TITLE` in `menus.yml`. |
| `BILLFORD-MENU.SIZE` | `int` | Any valid integer number | `'54'` | Configures the technical `SIZE` parameter for `BILLFORD-MENU.SIZE` in `menus.yml`. |
| `BILLFORD-MENU.TRADE-BUTTON.MATERIAL.NAME` | `str` | Any string text | `'&aTrade with Billford'` | Configures the technical `NAME` parameter for `BILLFORD-MENU.TRADE-BUTTON.MATERIAL.NAME` in `menus.yml`. |
| `BILLFORD-MENU.TRADE-BUTTON.MATERIAL.LORE` | `list` | List of configured items/strings | `['']` | Configures the technical `LORE` parameter for `BILLFORD-MENU.TRADE-BUTTON.MATERIAL.LORE` in `menus.yml`. |
| `BILLFORD-MENU.CONFIRM-TRADE-BUTTON.MATERIAL` | `str` | Any string text | `'HOPPER'` | Configures the technical `MATERIAL` parameter for `BILLFORD-MENU.CONFIRM-TRADE-BUTTON.MATERIAL` in `menus.yml`. |
| `BILLFORD-MENU.CONFIRM-TRADE-BUTTON.NAME` | `str` | Any string text | `'&8Trade'` | Configures the technical `NAME` parameter for `BILLFORD-MENU.CONFIRM-TRADE-BUTTON.NAME` in `menus.yml`. |
| `BILLFORD-MENU.CONFIRM-TRADE-BUTTON.LORE` | `list` | List of configured items/strings | `['&fClick to confirm the trade', '', '&7(you need the items in your inventory)']` | Configures the technical `LORE` parameter for `BILLFORD-MENU.CONFIRM-TRADE-BUTTON.LORE` in `menus.yml`. |

### 3. Practical Setup Example

```yaml
BILLFORD-MENU:
  TITLE: '&8Billford'
  SIZE: 54
  TRADE-BUTTON:
    MATERIAL:
      NAME: '&aTrade with Billford'
      LORE:
      - ''
  CONFIRM-TRADE-BUTTON:
    MATERIAL: HOPPER
    NAME: '&8Trade'
    LORE:
    - '&fClick to confirm the trade'
    - ''
    - '&7(you need the items in your inventory)'
```

---

## Section: `PURCHASE-SHOP-MENU`

### 1. Commented Setup Code Example

```yaml
PURCHASE-SHOP-MENU:
  TITLE: '&8Confirmation Menu'
  SIZE: 27
  BUTTONS:
    MAIN:
      SLOT: 13
      LORE:
        MONEY: '&fBUY PRICE: &a${price}'
        SHARD: '&fBUY PRICE: &5${price}X &lShards'
        DEFAULT: '&fBUY PRICE: &a${price}'
    CANCEL:
      SLOT: 21
      MATERIAL: RED_STAINED_GLASS_PANE
      NAME: '&cCancel'
      LORE: '&fCLICK TO CANCEL'
    CONFIRM:
      SLOT: 23
      MATERIAL: LIME_STAINED_GLASS_PANE
      NAME: '&aConfirm'
      LORE: '&fCLICK TO BUY'
    QUANTITY_ADJUST:
      ADD:
        MATERIAL: LIME_STAINED_GLASS_PANE
        ADD_1:
          SLOT: 15
          NAME: '&aAdd 1'
          INCREMENT: 1
        ADD_10:
          SLOT: 16
          NAME: '&aAdd 10'
          INCREMENT: 10
        SET_64:
          SLOT: 17
          NAME: '&aSet To 64'
          INCREMENT: 64
      REMOVE:
        MATERIAL: RED_STAINED_GLASS_PANE
        REMOVE_1:
          SLOT: 11
          NAME: '&cRemove 1'
          DECREMENT: 1
        REMOVE_10:
          SLOT: 10
          NAME: '&cRemove 10'
          DECREMENT: 10
        REMOVE_64:
          SLOT: 9
          NAME: '&cRemove 64'
          DECREMENT: 64
  RESTRICTIONS:
    TOTEM_OF_UNDYING:
      MAX_QUANTITY: 1
      MIN_QUANTITY: 1
      HIDE_QUANTITY_BUTTONS: true
    ENDER_PEARL:
      MAX_QUANTITY: 16
      MIN_QUANTITY: 1
    DEFAULT:
      MAX_QUANTITY: 64
      MIN_QUANTITY: 1
  MESSAGES:
    SUCCESS:
      MONEY: '&7You bought &e{Quantity} {item-name}&7 for &a${amount}'
      SHARDS: '&7You bought {item-name}&7 for &5{amount} shards'
    ERROR:
      NO_MONEY: '&cYOU DON''T HAVE ENOUGH MONEY.'
      NO_SHARDS: '&cYOU DON''T HAVE ENOUGH SHARDS.'
      FULL_INVENTORY: '&cYOUR INVENTORY IS FULL.'
  SOUNDS:
    SUCCESS: ENTITY_EXPERIENCE_ORB_PICKUP
    ERROR: ENTITY_VILLAGER_NO
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `PURCHASE-SHOP-MENU.TITLE` | `str` | Any string text | `'&8Confirmation Menu'` | Configures the technical `TITLE` parameter for `PURCHASE-SHOP-MENU.TITLE` in `menus.yml`. |
| `PURCHASE-SHOP-MENU.SIZE` | `int` | Any valid integer number | `'27'` | Configures the technical `SIZE` parameter for `PURCHASE-SHOP-MENU.SIZE` in `menus.yml`. |
| `PURCHASE-SHOP-MENU.BUTTONS.MAIN.SLOT` | `int` | Any valid integer number | `'13'` | Configures the technical `SLOT` parameter for `PURCHASE-SHOP-MENU.BUTTONS.MAIN.SLOT` in `menus.yml`. |
| `PURCHASE-SHOP-MENU.BUTTONS.MAIN.LORE.MONEY` | `str` | Any string text | `'&fBUY PRICE: &a${price}'` | Configures the technical `MONEY` parameter for `PURCHASE-SHOP-MENU.BUTTONS.MAIN.LORE.MONEY` in `menus.yml`. |
| `PURCHASE-SHOP-MENU.BUTTONS.MAIN.LORE.SHARD` | `str` | Any string text | `'&fBUY PRICE: &5${price}X &lShards'` | Configures the technical `SHARD` parameter for `PURCHASE-SHOP-MENU.BUTTONS.MAIN.LORE.SHARD` in `menus.yml`. |
| `PURCHASE-SHOP-MENU.BUTTONS.MAIN.LORE.DEFAULT` | `str` | Any string text | `'&fBUY PRICE: &a${price}'` | Configures the technical `DEFAULT` parameter for `PURCHASE-SHOP-MENU.BUTTONS.MAIN.LORE.DEFAULT` in `menus.yml`. |
| `PURCHASE-SHOP-MENU.BUTTONS.CANCEL.SLOT` | `int` | Any valid integer number | `'21'` | Configures the technical `SLOT` parameter for `PURCHASE-SHOP-MENU.BUTTONS.CANCEL.SLOT` in `menus.yml`. |
| `PURCHASE-SHOP-MENU.BUTTONS.CANCEL.MATERIAL` | `str` | Any string text | `'RED_STAINED_GLASS_PANE'` | Configures the technical `MATERIAL` parameter for `PURCHASE-SHOP-MENU.BUTTONS.CANCEL.MATERIAL` in `menus.yml`. |
| `PURCHASE-SHOP-MENU.BUTTONS.CANCEL.NAME` | `str` | Any string text | `'&cCancel'` | Configures the technical `NAME` parameter for `PURCHASE-SHOP-MENU.BUTTONS.CANCEL.NAME` in `menus.yml`. |
| `PURCHASE-SHOP-MENU.BUTTONS.CANCEL.LORE` | `str` | Any string text | `'&fCLICK TO CANCEL'` | Configures the technical `LORE` parameter for `PURCHASE-SHOP-MENU.BUTTONS.CANCEL.LORE` in `menus.yml`. |
| `PURCHASE-SHOP-MENU.BUTTONS.CONFIRM.SLOT` | `int` | Any valid integer number | `'23'` | Configures the technical `SLOT` parameter for `PURCHASE-SHOP-MENU.BUTTONS.CONFIRM.SLOT` in `menus.yml`. |
| `PURCHASE-SHOP-MENU.BUTTONS.CONFIRM.MATERIAL` | `str` | Any string text | `'LIME_STAINED_GLASS_PANE'` | Configures the technical `MATERIAL` parameter for `PURCHASE-SHOP-MENU.BUTTONS.CONFIRM.MATERIAL` in `menus.yml`. |
| `PURCHASE-SHOP-MENU.BUTTONS.CONFIRM.NAME` | `str` | Any string text | `'&aConfirm'` | Configures the technical `NAME` parameter for `PURCHASE-SHOP-MENU.BUTTONS.CONFIRM.NAME` in `menus.yml`. |
| `PURCHASE-SHOP-MENU.BUTTONS.CONFIRM.LORE` | `str` | Any string text | `'&fCLICK TO BUY'` | Configures the technical `LORE` parameter for `PURCHASE-SHOP-MENU.BUTTONS.CONFIRM.LORE` in `menus.yml`. |
| `PURCHASE-SHOP-MENU.BUTTONS.QUANTITY_ADJUST.ADD.MATERIAL` | `str` | Any string text | `'LIME_STAINED_GLASS_PANE'` | Configures the technical `MATERIAL` parameter for `PURCHASE-SHOP-MENU.BUTTONS.QUANTITY_ADJUST.ADD.MATERIAL` in `menus.yml`. |
| `PURCHASE-SHOP-MENU.BUTTONS.QUANTITY_ADJUST.ADD.ADD_1.SLOT` | `int` | Any valid integer number | `'15'` | Configures the technical `SLOT` parameter for `PURCHASE-SHOP-MENU.BUTTONS.QUANTITY_ADJUST.ADD.ADD_1.SLOT` in `menus.yml`. |
| `PURCHASE-SHOP-MENU.BUTTONS.QUANTITY_ADJUST.ADD.ADD_1.NAME` | `str` | Any string text | `'&aAdd 1'` | Configures the technical `NAME` parameter for `PURCHASE-SHOP-MENU.BUTTONS.QUANTITY_ADJUST.ADD.ADD_1.NAME` in `menus.yml`. |
| `PURCHASE-SHOP-MENU.BUTTONS.QUANTITY_ADJUST.ADD.ADD_1.INCREMENT` | `int` | Any valid integer number | `'1'` | Configures the technical `INCREMENT` parameter for `PURCHASE-SHOP-MENU.BUTTONS.QUANTITY_ADJUST.ADD.ADD_1.INCREMENT` in `menus.yml`. |
| `PURCHASE-SHOP-MENU.BUTTONS.QUANTITY_ADJUST.ADD.ADD_10.SLOT` | `int` | Any valid integer number | `'16'` | Configures the technical `SLOT` parameter for `PURCHASE-SHOP-MENU.BUTTONS.QUANTITY_ADJUST.ADD.ADD_10.SLOT` in `menus.yml`. |
| `PURCHASE-SHOP-MENU.BUTTONS.QUANTITY_ADJUST.ADD.ADD_10.NAME` | `str` | Any string text | `'&aAdd 10'` | Configures the technical `NAME` parameter for `PURCHASE-SHOP-MENU.BUTTONS.QUANTITY_ADJUST.ADD.ADD_10.NAME` in `menus.yml`. |
| `PURCHASE-SHOP-MENU.BUTTONS.QUANTITY_ADJUST.ADD.ADD_10.INCREMENT` | `int` | Any valid integer number | `'10'` | Configures the technical `INCREMENT` parameter for `PURCHASE-SHOP-MENU.BUTTONS.QUANTITY_ADJUST.ADD.ADD_10.INCREMENT` in `menus.yml`. |
| `PURCHASE-SHOP-MENU.BUTTONS.QUANTITY_ADJUST.ADD.SET_64.SLOT` | `int` | Any valid integer number | `'17'` | Configures the technical `SLOT` parameter for `PURCHASE-SHOP-MENU.BUTTONS.QUANTITY_ADJUST.ADD.SET_64.SLOT` in `menus.yml`. |
| `PURCHASE-SHOP-MENU.BUTTONS.QUANTITY_ADJUST.ADD.SET_64.NAME` | `str` | Any string text | `'&aSet To 64'` | Configures the technical `NAME` parameter for `PURCHASE-SHOP-MENU.BUTTONS.QUANTITY_ADJUST.ADD.SET_64.NAME` in `menus.yml`. |
| `PURCHASE-SHOP-MENU.BUTTONS.QUANTITY_ADJUST.ADD.SET_64.INCREMENT` | `int` | Any valid integer number | `'64'` | Configures the technical `INCREMENT` parameter for `PURCHASE-SHOP-MENU.BUTTONS.QUANTITY_ADJUST.ADD.SET_64.INCREMENT` in `menus.yml`. |
| `PURCHASE-SHOP-MENU.BUTTONS.QUANTITY_ADJUST.REMOVE.MATERIAL` | `str` | Any string text | `'RED_STAINED_GLASS_PANE'` | Configures the technical `MATERIAL` parameter for `PURCHASE-SHOP-MENU.BUTTONS.QUANTITY_ADJUST.REMOVE.MATERIAL` in `menus.yml`. |
| `PURCHASE-SHOP-MENU.BUTTONS.QUANTITY_ADJUST.REMOVE.REMOVE_1.SLOT` | `int` | Any valid integer number | `'11'` | Configures the technical `SLOT` parameter for `PURCHASE-SHOP-MENU.BUTTONS.QUANTITY_ADJUST.REMOVE.REMOVE_1.SLOT` in `menus.yml`. |
| `PURCHASE-SHOP-MENU.BUTTONS.QUANTITY_ADJUST.REMOVE.REMOVE_1.NAME` | `str` | Any string text | `'&cRemove 1'` | Configures the technical `NAME` parameter for `PURCHASE-SHOP-MENU.BUTTONS.QUANTITY_ADJUST.REMOVE.REMOVE_1.NAME` in `menus.yml`. |
| `PURCHASE-SHOP-MENU.BUTTONS.QUANTITY_ADJUST.REMOVE.REMOVE_1.DECREMENT` | `int` | Any valid integer number | `'1'` | Configures the technical `DECREMENT` parameter for `PURCHASE-SHOP-MENU.BUTTONS.QUANTITY_ADJUST.REMOVE.REMOVE_1.DECREMENT` in `menus.yml`. |
| `PURCHASE-SHOP-MENU.BUTTONS.QUANTITY_ADJUST.REMOVE.REMOVE_10.SLOT` | `int` | Any valid integer number | `'10'` | Configures the technical `SLOT` parameter for `PURCHASE-SHOP-MENU.BUTTONS.QUANTITY_ADJUST.REMOVE.REMOVE_10.SLOT` in `menus.yml`. |
| `PURCHASE-SHOP-MENU.BUTTONS.QUANTITY_ADJUST.REMOVE.REMOVE_10.NAME` | `str` | Any string text | `'&cRemove 10'` | Configures the technical `NAME` parameter for `PURCHASE-SHOP-MENU.BUTTONS.QUANTITY_ADJUST.REMOVE.REMOVE_10.NAME` in `menus.yml`. |
| *(18 additional sub-keys configured in section)* | | | | |

### 3. Practical Setup Example

```yaml
PURCHASE-SHOP-MENU:
  TITLE: '&8Confirmation Menu'
  SIZE: 27
  BUTTONS:
    MAIN:
      SLOT: 13
      LORE:
        MONEY: '&fBUY PRICE: &a${price}'
        SHARD: '&fBUY PRICE: &5${price}X &lShards'
        DEFAULT: '&fBUY PRICE: &a${price}'
    CANCEL:
      SLOT: 21
      MATERIAL: RED_STAINED_GLASS_PANE
      NAME: '&cCancel'
      LORE: '&fCLICK TO CANCEL'
    CONFIRM:
      SLOT: 23
      MATERIAL: LIME_STAINED_GLASS_PANE
      NAME: '&aConfirm'
      LORE: '&fCLICK TO BUY'
    QUANTITY_ADJUST:
      ADD:
        MATERIAL: LIME_STAINED_GLASS_PANE
        ADD_1:
          SLOT: 15
          NAME: '&aAdd 1'
          INCREMENT: 1
        ADD_10:
          SLOT: 16
          NAME: '&aAdd 10'
          INCREMENT: 10
        SET_64:
          SLOT: 17
          NAME: '&aSet To 64'
          INCREMENT: 64
      REMOVE:
        MATERIAL: RED_STAINED_GLASS_PANE
        REMOVE_1:
          SLOT: 11
          NAME: '&cRemove 1'
          DECREMENT: 1
        REMOVE_10:
          SLOT: 10
```

---

## Section: `PAY-CONFIRM-MENU`

### 1. Commented Setup Code Example

```yaml
PAY-CONFIRM-MENU:
  TITLE: '&8Confirm Payment'
  SIZE: 27
  CONFIRM-BUTTON:
    TITLE: '&#00FC00Confirm'
    MATERIAL: LIME_STAINED_GLASS_PANE
    LORE:
    - '&7Click to confirm to pay {amount}!'
  CANCEL-BUTTON:
    TITLE: '&#FC0000Cancel'
    MATERIAL: RED_STAINED_GLASS_PANE
    LORE:
    - '&7Click to cancel'
  PLAYER-BUTTON:
    TITLE: '&#00FC00{player}'
    MATERIAL: PLAYER_HEAD
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `PAY-CONFIRM-MENU.TITLE` | `str` | Any string text | `'&8Confirm Payment'` | Configures the technical `TITLE` parameter for `PAY-CONFIRM-MENU.TITLE` in `menus.yml`. |
| `PAY-CONFIRM-MENU.SIZE` | `int` | Any valid integer number | `'27'` | Configures the technical `SIZE` parameter for `PAY-CONFIRM-MENU.SIZE` in `menus.yml`. |
| `PAY-CONFIRM-MENU.CONFIRM-BUTTON.TITLE` | `str` | Any string text | `'&#00FC00Confirm'` | Configures the technical `TITLE` parameter for `PAY-CONFIRM-MENU.CONFIRM-BUTTON.TITLE` in `menus.yml`. |
| `PAY-CONFIRM-MENU.CONFIRM-BUTTON.MATERIAL` | `str` | Any string text | `'LIME_STAINED_GLASS_PANE'` | Configures the technical `MATERIAL` parameter for `PAY-CONFIRM-MENU.CONFIRM-BUTTON.MATERIAL` in `menus.yml`. |
| `PAY-CONFIRM-MENU.CONFIRM-BUTTON.LORE` | `list` | List of configured items/strings | `['&7Click to confirm to pay {amount}!']` | Configures the technical `LORE` parameter for `PAY-CONFIRM-MENU.CONFIRM-BUTTON.LORE` in `menus.yml`. |
| `PAY-CONFIRM-MENU.CANCEL-BUTTON.TITLE` | `str` | Any string text | `'&#FC0000Cancel'` | Configures the technical `TITLE` parameter for `PAY-CONFIRM-MENU.CANCEL-BUTTON.TITLE` in `menus.yml`. |
| `PAY-CONFIRM-MENU.CANCEL-BUTTON.MATERIAL` | `str` | Any string text | `'RED_STAINED_GLASS_PANE'` | Configures the technical `MATERIAL` parameter for `PAY-CONFIRM-MENU.CANCEL-BUTTON.MATERIAL` in `menus.yml`. |
| `PAY-CONFIRM-MENU.CANCEL-BUTTON.LORE` | `list` | List of configured items/strings | `['&7Click to cancel']` | Configures the technical `LORE` parameter for `PAY-CONFIRM-MENU.CANCEL-BUTTON.LORE` in `menus.yml`. |
| `PAY-CONFIRM-MENU.PLAYER-BUTTON.TITLE` | `str` | Any string text | `'&#00FC00{player}'` | Configures the technical `TITLE` parameter for `PAY-CONFIRM-MENU.PLAYER-BUTTON.TITLE` in `menus.yml`. |
| `PAY-CONFIRM-MENU.PLAYER-BUTTON.MATERIAL` | `str` | Any string text | `'PLAYER_HEAD'` | Configures the technical `MATERIAL` parameter for `PAY-CONFIRM-MENU.PLAYER-BUTTON.MATERIAL` in `menus.yml`. |

### 3. Practical Setup Example

```yaml
PAY-CONFIRM-MENU:
  TITLE: '&8Confirm Payment'
  SIZE: 27
  CONFIRM-BUTTON:
    TITLE: '&#00FC00Confirm'
    MATERIAL: LIME_STAINED_GLASS_PANE
    LORE:
    - '&7Click to confirm to pay {amount}!'
  CANCEL-BUTTON:
    TITLE: '&#FC0000Cancel'
    MATERIAL: RED_STAINED_GLASS_PANE
    LORE:
    - '&7Click to cancel'
  PLAYER-BUTTON:
    TITLE: '&#00FC00{player}'
    MATERIAL: PLAYER_HEAD
```

---

## Section: `SELLALL-CONFIRM-MENU`

### 1. Commented Setup Code Example

```yaml
SELLALL-CONFIRM-MENU:
  TITLE: '&8Confirm Sell All'
  SIZE: 27
  CONFIRM-BUTTON:
    TITLE: '&#00FC00Confirm'
    MATERIAL: LIME_STAINED_GLASS_PANE
    SLOT: 15
    LORE:
    - '&7Click to confirm to sell all'
    - '&7sellable items in your inventory.'
  CANCEL-BUTTON:
    TITLE: '&#FC0000Cancel'
    MATERIAL: RED_STAINED_GLASS_PANE
    SLOT: 11
    LORE:
    - '&7Click to cancel'
  INFO-BUTTON:
    TITLE: '&#E69F00Sell All Items'
    MATERIAL: CHEST
    SLOT: 13
    LORE:
    - '&7This will sell all sellable'
    - '&7items in your inventory.'
```

### 2. Key Options & Technical Breakdown

| Option / Key Path | Data Type | Allowed Values | Default | Technical Function & Setup Guide |
| :--- | :--- | :--- | :--- | :--- |
| `SELLALL-CONFIRM-MENU.TITLE` | `str` | Any string text | `'&8Confirm Sell All'` | Configures the technical `TITLE` parameter for `SELLALL-CONFIRM-MENU.TITLE` in `menus.yml`. |
| `SELLALL-CONFIRM-MENU.SIZE` | `int` | Any valid integer number | `'27'` | Configures the technical `SIZE` parameter for `SELLALL-CONFIRM-MENU.SIZE` in `menus.yml`. |
| `SELLALL-CONFIRM-MENU.CONFIRM-BUTTON.TITLE` | `str` | Any string text | `'&#00FC00Confirm'` | Configures the technical `TITLE` parameter for `SELLALL-CONFIRM-MENU.CONFIRM-BUTTON.TITLE` in `menus.yml`. |
| `SELLALL-CONFIRM-MENU.CONFIRM-BUTTON.MATERIAL` | `str` | Any string text | `'LIME_STAINED_GLASS_PANE'` | Configures the technical `MATERIAL` parameter for `SELLALL-CONFIRM-MENU.CONFIRM-BUTTON.MATERIAL` in `menus.yml`. |
| `SELLALL-CONFIRM-MENU.CONFIRM-BUTTON.SLOT` | `int` | Any valid integer number | `'15'` | Configures the technical `SLOT` parameter for `SELLALL-CONFIRM-MENU.CONFIRM-BUTTON.SLOT` in `menus.yml`. |
| `SELLALL-CONFIRM-MENU.CONFIRM-BUTTON.LORE` | `list` | List of configured items/strings | `['&7Click to confirm to sell all', '&7sellable items in your inventory.']` | Configures the technical `LORE` parameter for `SELLALL-CONFIRM-MENU.CONFIRM-BUTTON.LORE` in `menus.yml`. |
| `SELLALL-CONFIRM-MENU.CANCEL-BUTTON.TITLE` | `str` | Any string text | `'&#FC0000Cancel'` | Configures the technical `TITLE` parameter for `SELLALL-CONFIRM-MENU.CANCEL-BUTTON.TITLE` in `menus.yml`. |
| `SELLALL-CONFIRM-MENU.CANCEL-BUTTON.MATERIAL` | `str` | Any string text | `'RED_STAINED_GLASS_PANE'` | Configures the technical `MATERIAL` parameter for `SELLALL-CONFIRM-MENU.CANCEL-BUTTON.MATERIAL` in `menus.yml`. |
| `SELLALL-CONFIRM-MENU.CANCEL-BUTTON.SLOT` | `int` | Any valid integer number | `'11'` | Configures the technical `SLOT` parameter for `SELLALL-CONFIRM-MENU.CANCEL-BUTTON.SLOT` in `menus.yml`. |
| `SELLALL-CONFIRM-MENU.CANCEL-BUTTON.LORE` | `list` | List of configured items/strings | `['&7Click to cancel']` | Configures the technical `LORE` parameter for `SELLALL-CONFIRM-MENU.CANCEL-BUTTON.LORE` in `menus.yml`. |
| `SELLALL-CONFIRM-MENU.INFO-BUTTON.TITLE` | `str` | Any string text | `'&#E69F00Sell All Items'` | Configures the technical `TITLE` parameter for `SELLALL-CONFIRM-MENU.INFO-BUTTON.TITLE` in `menus.yml`. |
| `SELLALL-CONFIRM-MENU.INFO-BUTTON.MATERIAL` | `str` | Any string text | `'CHEST'` | Configures the technical `MATERIAL` parameter for `SELLALL-CONFIRM-MENU.INFO-BUTTON.MATERIAL` in `menus.yml`. |
| `SELLALL-CONFIRM-MENU.INFO-BUTTON.SLOT` | `int` | Any valid integer number | `'13'` | Configures the technical `SLOT` parameter for `SELLALL-CONFIRM-MENU.INFO-BUTTON.SLOT` in `menus.yml`. |
| `SELLALL-CONFIRM-MENU.INFO-BUTTON.LORE` | `list` | List of configured items/strings | `['&7This will sell all sellable', '&7items in your inventory.']` | Configures the technical `LORE` parameter for `SELLALL-CONFIRM-MENU.INFO-BUTTON.LORE` in `menus.yml`. |

### 3. Practical Setup Example

```yaml
SELLALL-CONFIRM-MENU:
  TITLE: '&8Confirm Sell All'
  SIZE: 27
  CONFIRM-BUTTON:
    TITLE: '&#00FC00Confirm'
    MATERIAL: LIME_STAINED_GLASS_PANE
    SLOT: 15
    LORE:
    - '&7Click to confirm to sell all'
    - '&7sellable items in your inventory.'
  CANCEL-BUTTON:
    TITLE: '&#FC0000Cancel'
    MATERIAL: RED_STAINED_GLASS_PANE
    SLOT: 11
    LORE:
    - '&7Click to cancel'
  INFO-BUTTON:
    TITLE: '&#E69F00Sell All Items'
    MATERIAL: CHEST
    SLOT: 13
    LORE:
    - '&7This will sell all sellable'
    - '&7items in your inventory.'
```

---

