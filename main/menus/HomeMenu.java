package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.Home;
import com.bx.ultimateDonutSmp.models.Team;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeMenu extends BaseMenu {

    private static final int HOMES_PER_PAGE = 5;

    private static final int PREV_PAGE_SLOT = 0;
    private static final int NEXT_PAGE_SLOT = 8;

    private static final int TEAM_TELEPORT_SLOT = 10;
    private static final int TEAM_ACTION_SLOT = 19;

    private static final int[] HOME_TELEPORT_SLOTS = {12, 13, 14, 15, 16};
    private static final int[] HOME_ACTION_SLOTS = {21, 22, 23, 24, 25};

    private final Map<Integer, SlotAction> slotActions = new HashMap<>();

    private int page = 0;

    public HomeMenu(UltimateDonutSmp plugin) {
        super(plugin, configuredTitle(plugin), configuredSize(plugin));
    }

    @Override
    public void build(Player player) {
        clear();
        slotActions.clear();

        fillWithFiller();

        List<Home> homes = new ArrayList<>(plugin.getHomeManager().getHomes(player.getUniqueId()));
        homes.sort(Comparator.comparing(Home::getName, String.CASE_INSENSITIVE_ORDER));

        int maxHomes = plugin.getHomeManager().getMaxHomes(player);
        int totalPages = plugin.getHomeManager().getMaxHomePages(player);
        page = Math.max(0, Math.min(page, totalPages - 1));

        buildPageButtons(totalPages);
        buildTeamButtons(player);
        buildHomeButtons(player, homes, maxHomes);
    }

    @Override
    public void handleClick(int slot, Player player, ClickType clickType) {
        SlotAction action = slotActions.get(slot);
        if (action == null) return;

        SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
        action.execute(player, clickType);
    }

    private void buildPageButtons(int totalPages) {
        if (page > 0) {
            set(PREV_PAGE_SLOT, ItemUtils.createItem(
                    Material.ARROW,
                    "&fᴘʀᴇᴠɪᴏᴜѕ ᴘᴀɢᴇ",
                    List.of("&7ɢᴏ ᴛᴏ ᴘᴀɢᴇ &f" + page, "&7ᴄᴜʀʀᴇɴᴛ ᴘᴀɢᴇ: &f" + (page + 1) + "/" + totalPages)
            ));
            slotActions.put(PREV_PAGE_SLOT, (p, click) -> {
                page--;
                build(p);
            });
        }

        if (page < totalPages - 1) {
            set(NEXT_PAGE_SLOT, ItemUtils.createItem(
                    Material.ARROW,
                    "&fɴᴇxᴛ ᴘᴀɢᴇ",
                    List.of("&7ɢᴏ ᴛᴏ ᴘᴀɢᴇ &f" + (page + 2), "&7ᴄᴜʀʀᴇɴᴛ ᴘᴀɢᴇ: &f" + (page + 1) + "/" + totalPages)
            ));
            slotActions.put(NEXT_PAGE_SLOT, (p, click) -> {
                page++;
                build(p);
            });
        }
    }

    private void buildTeamButtons(Player player) {
        Team team = plugin.getTeamManager().getTeam(player);
        boolean canEditTeamHome = canEditTeamHome(player, team);
        boolean canVisitTeamHome = canVisitTeamHome(player, team);

        String teleportState = team == null ? "NO_TEAM" : (team.hasHome() ? "HAS_HOME" : "NO_HOME");
        Map<String, String> teleportPlaceholders = new HashMap<>();
        teleportPlaceholders.put("world", team != null && team.hasHome() ? friendlyWorldName(team.getHome()) : "overworld");

        set(TEAM_TELEPORT_SLOT, ItemUtils.createItem(
                material("HOME-MENU.TEAM_HOME.TELEPORT.MATERIALS." + teleportState, Material.WHITE_BANNER),
                text("HOME-MENU.TEAM_HOME.TELEPORT.DISPLAY_NAME." + teleportState, "&fᴛᴇᴀᴍ ʜᴏᴍᴇ", teleportPlaceholders),
                lore("HOME-MENU.TEAM_HOME.TELEPORT.LORE." + teleportState,
                        defaultTeamTeleportLore(team, teleportState), teleportPlaceholders)
        ));

        if (team != null && team.hasHome() && canVisitTeamHome) {
            slotActions.put(TEAM_TELEPORT_SLOT, (p, click) -> {
                p.closeInventory();
                plugin.getTeleportManager().queue(p, team.getHome(), "TEAM-HOME", null);
            });
        } else if (team != null && !team.hasHome() && canEditTeamHome) {
            slotActions.put(TEAM_TELEPORT_SLOT, (p, click) -> setTeamHome(p, team));
        }

        String actionState = team == null ? "NO_TEAM" : (team.hasHome() ? "HAS_HOME" : "NO_HOME");
        List<String> actionLore = lore(
                "HOME-MENU.TEAM_HOME.SAVE.LORE." + actionState,
                defaultTeamActionLore(team, actionState),
                teleportPlaceholders
        );
        if (team != null && !canEditTeamHome) {
            actionLore = new ArrayList<>(actionLore);
            actionLore.add("&cʏᴏᴜ ᴄᴀɴɴᴏᴛ ᴇᴅɪᴛ ᴛʜᴇ ᴛᴇᴀᴍ ʜᴏᴍᴇ.");
        }
        if (team != null && team.hasHome() && !canVisitTeamHome) {
            actionLore = new ArrayList<>(actionLore);
            actionLore.add("&cʏᴏᴜ ᴄᴀɴɴᴏᴛ ᴠɪѕɪᴛ ᴛʜᴇ ᴛᴇᴀᴍ ʜᴏᴍᴇ.");
        }

        set(TEAM_ACTION_SLOT, ItemUtils.createItem(
                material("HOME-MENU.TEAM_HOME.SAVE.MATERIALS." + actionState, Material.GRAY_DYE),
                text("HOME-MENU.TEAM_HOME.SAVE.DISPLAY_NAME." + actionState, "&7ᴍᴀɴᴀɢᴇ ᴛᴇᴀᴍ ʜᴏᴍᴇ", teleportPlaceholders),
                actionLore
        ));

        if (team == null || !canEditTeamHome) return;

        if (team.hasHome()) {
            slotActions.put(TEAM_ACTION_SLOT, (p, click) -> {
                if (click.isRightClick()) {
                    deleteTeamHome(p, team);
                } else {
                    setTeamHome(p, team);
                }
            });
            return;
        }

        slotActions.put(TEAM_ACTION_SLOT, (p, click) -> setTeamHome(p, team));
    }

    private void buildHomeButtons(Player player, List<Home> homes, int maxHomes) {
        int startIndex = page * HOMES_PER_PAGE;

        for (int i = 0; i < HOMES_PER_PAGE; i++) {
            int globalIndex = startIndex + i;
            int teleportSlot = HOME_TELEPORT_SLOTS[i];
            int actionSlot = HOME_ACTION_SLOTS[i];
            String key = "HOME-" + (i + 1);

            if (globalIndex < homes.size()) {
                setUsedHomeButtons(player, homes.get(globalIndex), key, globalIndex, teleportSlot, actionSlot);
                continue;
            }

            if (globalIndex < maxHomes) {
                setAvailableHomeButtons(player, key, globalIndex, teleportSlot, actionSlot);
            } else {
                setLockedHomeButtons(key, globalIndex, teleportSlot, actionSlot);
            }
        }
    }

    private void setUsedHomeButtons(Player player, Home home, String key, int globalIndex, int teleportSlot, int actionSlot) {
        Map<String, String> placeholders = homePlaceholders(home.getName(), globalIndex, home.getLocation());

        set(teleportSlot, ItemUtils.createItem(
                material("HOME-MENU.TELEPORT-USED-MATERIAL", Material.LIGHT_BLUE_BED),
                text("HOME-MENU.TELEPORT." + key + ".DISPLAY-NAME.USED", "&b{name}", placeholders),
                lore("HOME-MENU.TELEPORT." + key + ".LORE.USED",
                        List.of("&7ᴡᴏʀʟᴅ: &f{world}", "&aʟᴇꜰᴛ-ᴄʟɪᴄᴋ ᴛᴏ ᴛᴇʟᴇᴘᴏʀᴛ"),
                        placeholders)
        ));
        slotActions.put(teleportSlot, (p, click) -> {
            p.closeInventory();
            plugin.getTeleportManager().queue(p, home.getLocation(), "HOME", null);
        });

        set(actionSlot, ItemUtils.createItem(
                material("HOME-MENU.CREATE-USED-MATERIAL", Material.BLUE_DYE),
                text("HOME-MENU.CREATE." + key + ".DISPLAY-NAME.USED", "&b{name}", placeholders),
                lore("HOME-MENU.CREATE." + key + ".LORE.USED",
                        List.of("&eʟᴇꜰᴛ-ᴄʟɪᴄᴋ ᴛᴏ ʀᴇɴᴀᴍᴇ ᴛʜɪѕ ʜᴏᴍᴇ", "&cʀɪɢʜᴛ-ᴄʟɪᴄᴋ ᴛᴏ ᴅᴇʟᴇᴛᴇ"),
                        placeholders)
        ));
        slotActions.put(actionSlot, (p, click) -> {
            if (click.isRightClick()) {
                deleteHome(p, home);
            } else {
                plugin.getHomeManager().promptRenameHome(p, home.getName());
            }
        });
    }

    private void setAvailableHomeButtons(Player player, String key, int globalIndex, int teleportSlot, int actionSlot) {
        String suggestedName = defaultHomeName(globalIndex);
        Map<String, String> placeholders = emptyHomePlaceholders(globalIndex);

        set(teleportSlot, ItemUtils.createItem(
                material("HOME-MENU.TELEPORT-NO-USED-MATERIAL", Material.LIGHT_GRAY_BED),
                text("HOME-MENU.TELEPORT." + key + ".DISPLAY-NAME.NO-USED", "&7{slot}", placeholders),
                lore("HOME-MENU.TELEPORT." + key + ".LORE.NO-USED",
                        List.of("&7ᴄʟɪᴄᴋ ᴛᴏ ᴄʀᴇᴀᴛᴇ ᴀ ʜᴏᴍᴇ."), placeholders)
        ));
        set(actionSlot, ItemUtils.createItem(
                material("HOME-MENU.CREATE-NO-USED-MATERIAL", Material.GRAY_DYE),
                text("HOME-MENU.CREATE." + key + ".DISPLAY-NAME.NO-USED", "&7{slot}", placeholders),
                lore("HOME-MENU.CREATE." + key + ".LORE.NO-USED",
                        List.of("&7ᴄʟɪᴄᴋ ᴛᴏ ɴᴀᴍᴇ ᴀɴᴅ ᴄʀᴇᴀᴛᴇ ᴀ ʜᴏᴍᴇ."), placeholders)
        ));

        slotActions.put(teleportSlot, (p, click) ->
                plugin.getHomeManager().promptCreateHome(p, p.getLocation(), suggestedName));
        slotActions.put(actionSlot, (p, click) ->
                plugin.getHomeManager().promptCreateHome(p, p.getLocation(), suggestedName));
    }

    private void setLockedHomeButtons(String key, int globalIndex, int teleportSlot, int actionSlot) {
        Map<String, String> placeholders = emptyHomePlaceholders(globalIndex);

        set(teleportSlot, ItemUtils.createItem(
                material("HOME-MENU.TELEPORT-NO-PERMISSION-MATERIAL", Material.RED_BED),
                text("HOME-MENU.TELEPORT." + key + ".DISPLAY-NAME.NO-PERMISSION", "&cʟᴏᴄᴋᴇᴅ", placeholders),
                lore("HOME-MENU.TELEPORT." + key + ".LORE.NO-PERMISSION",
                        List.of("&7ʏᴏᴜ ɴᴇᴇᴅ ᴀ ʜɪɢʜᴇʀ ʀᴀɴᴋ ꜰᴏʀ ᴛʜɪѕ ʜᴏᴍᴇ."), placeholders)
        ));
        set(actionSlot, ItemUtils.createItem(
                material("HOME-MENU.CREATE-NO-PERMISSION-MATERIAL", Material.RED_DYE),
                text("HOME-MENU.CREATE." + key + ".DISPLAY-NAME.NO-PERMISSION", "&cʟᴏᴄᴋᴇᴅ", placeholders),
                lore("HOME-MENU.CREATE." + key + ".LORE.NO-PERMISSION",
                        List.of("&7ʏᴏᴜ ɴᴇᴇᴅ ᴀ ʜɪɢʜᴇʀ ʀᴀɴᴋ ꜰᴏʀ ᴛʜɪѕ ʜᴏᴍᴇ."), placeholders)
        ));
    }

    private void deleteHome(Player player, Home home) {
        if (!plugin.getHomeManager().deleteHome(player.getUniqueId(), home.getName())) {
            player.sendMessage(ColorUtils.toComponent("&cʜᴏᴍᴇ ɴᴏᴛ ꜰᴏᴜɴᴅ."));
            return;
        }

        player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage("HOME.DELETED")));
        build(player);
    }

    private void setTeamHome(Player player, Team team) {
        if (!canEditTeamHome(player, team)) {
            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage("TEAM.NO-EDIT-HOME-PERMISSION")));
            return;
        }

        team.setHome(player.getLocation());
        plugin.getTeamManager().save(team);
        player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage("TEAM.TEAM-HOME-SET")));
        build(player);
    }

    private void deleteTeamHome(Player player, Team team) {
        if (!canEditTeamHome(player, team)) {
            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage("TEAM.NO-EDIT-HOME-PERMISSION")));
            return;
        }
        if (!team.hasHome()) {
            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage("TEAM.NO-TEAM-HOME")));
            return;
        }

        team.setHome(null);
        plugin.getTeamManager().save(team);
        player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage("TEAM.TEAM-HOME-DELETED")));
        build(player);
    }

    private boolean canEditTeamHome(Player player, Team team) {
        return team != null && plugin.getTeamManager().canEditHome(team, player.getUniqueId());
    }

    private boolean canVisitTeamHome(Player player, Team team) {
        return team != null && plugin.getTeamManager().canVisitHome(team, player.getUniqueId());
    }

    private Map<String, String> homePlaceholders(String name, int globalIndex, Location location) {
        Map<String, String> placeholders = emptyHomePlaceholders(globalIndex);
        placeholders.put("name", name);
        placeholders.put("world", friendlyWorldName(location));
        return placeholders;
    }

    private Map<String, String> emptyHomePlaceholders(int globalIndex) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("name", slotLabel(globalIndex));
        placeholders.put("slot", slotLabel(globalIndex));
        placeholders.put("world", "overworld");
        return placeholders;
    }

    private String slotLabel(int index) {
        return "ʜᴏᴍᴇ " + (index + 1);
    }

    private String defaultHomeName(int index) {
        return index == 0 ? "home" : "home" + (index + 1);
    }

    private String friendlyWorldName(Location location) {
        if (location == null || location.getWorld() == null) return "overworld";

        World.Environment environment = location.getWorld().getEnvironment();
        return switch (environment) {
            case NETHER -> "nether";
            case THE_END -> "end";
            default -> "overworld";
        };
    }

    private Material material(String path, Material fallback) {
        return ItemUtils.parseMaterial(menus().getString(path, fallback.name()));
    }

    private String text(String path, String fallback, Map<String, String> placeholders) {
        return replacePlaceholders(menus().getString(path, fallback), placeholders);
    }

    private List<String> lore(String path, List<String> fallback, Map<String, String> placeholders) {
        List<String> lines = new ArrayList<>();
        Object raw = menus().get(path);

        if (raw instanceof List<?> list) {
            for (Object entry : list) {
                lines.add(String.valueOf(entry));
            }
        } else if (raw instanceof String string && !string.isBlank()) {
            lines.add(string);
        } else {
            lines.addAll(fallback);
        }

        for (int i = 0; i < lines.size(); i++) {
            lines.set(i, replacePlaceholders(lines.get(i), placeholders));
        }
        return lines;
    }

    private List<String> defaultTeamTeleportLore(Team team, String state) {
        if ("NO_TEAM".equals(state)) return List.of("&7ʏᴏᴜ ᴀʀᴇ ɴᴏᴛ ɪɴ ᴀ ᴛᴇᴀᴍ.");
        if ("NO_HOME".equals(state)) return List.of("&7ᴄʟɪᴄᴋ ᴛᴏ ᴄʀᴇᴀᴛᴇ ʏᴏᴜʀ ᴛᴇᴀᴍ ʜᴏᴍᴇ.");
        return List.of("&7ᴡᴏʀʟᴅ: &f{world}", "&aʟᴇꜰᴛ-ᴄʟɪᴄᴋ ᴛᴏ ᴛᴇʟᴇᴘᴏʀᴛ");
    }

    private List<String> defaultTeamActionLore(Team team, String state) {
        if ("NO_TEAM".equals(state)) return List.of("&7ʏᴏᴜ ᴀʀᴇ ɴᴏᴛ ɪɴ ᴀ ᴛᴇᴀᴍ.");
        if ("NO_HOME".equals(state)) return List.of("&7ʟᴇꜰᴛ-ᴄʟɪᴄᴋ ᴛᴏ ѕᴀᴠᴇ ʏᴏᴜʀ ᴛᴇᴀᴍ ʜᴏᴍᴇ.");
        return List.of("&bʟᴇꜰᴛ-ᴄʟɪᴄᴋ ᴛᴏ ᴜᴘᴅᴀᴛᴇ ᴛʜᴇ ᴛᴇᴀᴍ ʜᴏᴍᴇ", "&cʀɪɢʜᴛ-ᴄʟɪᴄᴋ ᴛᴏ ᴅᴇʟᴇᴛᴇ");
    }

    private String replacePlaceholders(String value, Map<String, String> placeholders) {
        String output = value == null ? "" : value;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            output = output.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return output;
    }

    private FileConfiguration menus() {
        return plugin.getConfigManager().getMenus();
    }

    private void fillWithFiller() {
        ItemStack filler = blank(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < inventory.getSize(); i++) {
            set(i, filler);
        }
    }

    private ItemStack blank(Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ColorUtils.toComponent(" "));
            item.setItemMeta(meta);
        }
        return item;
    }

    private static String configuredTitle(UltimateDonutSmp plugin) {
        return plugin.getConfigManager().getMenus().getString("HOME-MENU.TITLE", "&8ʜᴏᴍᴇѕ");
    }

    private static int configuredSize(UltimateDonutSmp plugin) {
        int size = plugin.getConfigManager().getMenus().getInt("HOME-MENU.SIZE", 36);
        return size >= 27 && size % 9 == 0 ? size : 36;
    }

    @FunctionalInterface
    private interface SlotAction {
        void execute(Player player, ClickType clickType);
    }
}
