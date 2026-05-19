package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.models.ProfileSnapshot;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class ProfileViewerMenu extends BaseMenu {

    private static final String MENU_PATH = "PROFILE-VIEWER-MENU";
    private static final String STATS_PATH = "STATS-MENU.BUTTONS";

    private final UUID targetUuid;
    private final Map<Integer, SlotAction> slotActions = new HashMap<>();
    private ProfileSnapshot snapshot;

    public ProfileViewerMenu(UltimateDonutSmp plugin, UUID targetUuid) {
        super(plugin, configuredTitle(plugin, targetUuid), configuredSize(plugin));
        this.targetUuid = targetUuid;
    }

    @Override
    public void build(Player player) {
        clear();
        slotActions.clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);

        snapshot = plugin.getProfileViewerManager().resolveProfile(targetUuid).orElse(null);
        if (snapshot == null) {
            set(inventory.getSize() / 2, ItemUtils.createItem(
                    Material.BARRIER,
                    "&cᴘʀᴏꜰɪʟᴇ ɴᴏᴛ ꜰᴏᴜɴᴅ",
                    List.of("&7ᴛʜɪѕ ᴘʟᴀʏᴇʀ ɴᴏ ʟᴏɴɢᴇʀ ʜᴀѕ ᴘʀᴏꜰɪʟᴇ ᴅᴀᴛᴀ.")
            ));
            return;
        }

        buildSummary();
        buildStats();
        buildHomesButton();
        buildCurrentLocationButton();
        buildPunishmentsButton(player);
        buildRefreshButton();
    }

    @Override
    public void handleClick(int slot, Player player, ClickType clickType) {
        SlotAction action = slotActions.get(slot);
        if (action == null) {
            return;
        }

        SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
        action.execute(player, clickType);
    }

    private void buildSummary() {
        int slot = menus().getInt(MENU_PATH + ".BUTTONS.SUMMARY.SLOT", 4);
        Material material = ItemUtils.parseMaterial(menus().getString(MENU_PATH + ".BUTTONS.SUMMARY.MATERIAL", "PLAYER_HEAD"));

        List<String> lore = replacePlaceholders(
                menus().getStringList(MENU_PATH + ".BUTTONS.SUMMARY.LORE"),
                snapshot
        );
        if (lore.isEmpty()) {
            lore = List.of(
                    "&7ѕᴛᴀᴛᴜѕ: &f" + statusLabel(snapshot),
                    "&7ᴛᴇᴀᴍ: &f" + safeTeamName(snapshot),
                    "&7ʜᴏᴍᴇѕ: &f" + snapshot.getHomeCount(),
                    "&7ᴀꜰᴋ: &f" + yesNo(snapshot.isAfk()),
                    "&7ʟᴏᴄᴀᴛɪᴏɴ: &f" + currentLocationSummary(snapshot)
            );
        }

        set(slot, createPlayerItem(
                material,
                replacePlaceholders(menus().getString(MENU_PATH + ".BUTTONS.SUMMARY.DISPLAY-NAME", "&b{username}"), snapshot),
                lore,
                snapshot.getUuid()
        ));
    }

    private void buildStats() {
        ConfigurationSection buttons = menus().getConfigurationSection(STATS_PATH);
        if (buttons == null || snapshot == null) {
            return;
        }

        for (String key : buttons.getKeys(false)) {
            ConfigurationSection section = buttons.getConfigurationSection(key);
            if (section == null) {
                continue;
            }

            int slot = section.getInt("SLOT", -1);
            if (slot < 0 || slot >= inventory.getSize()) {
                continue;
            }

            String value = resolveStatValue(key);
            List<String> lore = new ArrayList<>(section.getStringList("LORE"));
            if (lore.isEmpty()) {
                lore.add("&7{value}");
            }

            for (int i = 0; i < lore.size(); i++) {
                lore.set(i, lore.get(i).replace("{value}", value));
            }

            set(slot, ItemUtils.createItem(
                    ItemUtils.parseMaterial(section.getString("MATERIAL", "STONE")),
                    section.getString("DISPLAY-NAME", "&b" + key),
                    lore
            ));
        }
    }

    private void buildHomesButton() {
        int slot = menus().getInt(MENU_PATH + ".BUTTONS.HOMES.SLOT", 40);
        List<String> lore = replacePlaceholders(
                menus().getStringList(MENU_PATH + ".BUTTONS.HOMES.LORE"),
                snapshot
        );
        if (lore.isEmpty()) {
            lore = List.of(
                    "&7ᴠɪᴇᴡ ᴀɴᴅ ɴᴀᴠɪɢᴀᴛᴇ ᴛʜʀᴏᴜɢʜ ᴛʜɪѕ ᴘʟᴀʏᴇʀ'ѕ ʜᴏᴍᴇѕ.",
                    "&7ʜᴏᴍᴇѕ ѕᴀᴠᴇᴅ: &f" + snapshot.getHomeCount(),
                    "&aᴄʟɪᴄᴋ ᴛᴏ ᴏᴘᴇɴ"
            );
        }

        set(slot, ItemUtils.createItem(
                ItemUtils.parseMaterial(menus().getString(MENU_PATH + ".BUTTONS.HOMES.MATERIAL", "RED_BED")),
                replacePlaceholders(menus().getString(MENU_PATH + ".BUTTONS.HOMES.DISPLAY-NAME", "&bʜᴏᴍᴇѕ"), snapshot),
                lore
        ));
        slotActions.put(slot, (player, clickType) -> new ProfileViewerHomesMenu(plugin, targetUuid).open(player));
    }

    private void buildCurrentLocationButton() {
        int slot = menus().getInt(MENU_PATH + ".BUTTONS.CURRENT-LOCATION.SLOT", 41);
        boolean canTeleport = snapshot.isOnline() && snapshot.hasCurrentLocation();

        String namePath = canTeleport
                ? MENU_PATH + ".BUTTONS.CURRENT-LOCATION.DISPLAY-NAME"
                : MENU_PATH + ".BUTTONS.CURRENT-LOCATION.DISPLAY-NAME-OFFLINE";
        String lorePath = canTeleport
                ? MENU_PATH + ".BUTTONS.CURRENT-LOCATION.LORE"
                : MENU_PATH + ".BUTTONS.CURRENT-LOCATION.LORE-OFFLINE";
        String fallbackName = canTeleport ? "&bᴄᴜʀʀᴇɴᴛ ʟᴏᴄᴀᴛɪᴏɴ" : "&cᴄᴜʀʀᴇɴᴛ ʟᴏᴄᴀᴛɪᴏɴ";
        List<String> fallbackLore = canTeleport
                ? List.of("&7" + currentLocationSummary(snapshot), "&aᴄʟɪᴄᴋ ᴛᴏ ᴛᴇʟᴇᴘᴏʀᴛ")
                : List.of("&7ᴛʜɪѕ ᴘʟᴀʏᴇʀ ɪѕ ᴏꜰꜰʟɪɴᴇ ʀɪɢʜᴛ ɴᴏᴡ.");

        List<String> lore = replacePlaceholders(menus().getStringList(lorePath), snapshot);
        if (lore.isEmpty()) {
            lore = fallbackLore;
        }

        set(slot, ItemUtils.createItem(
                ItemUtils.parseMaterial(menus().getString(MENU_PATH + ".BUTTONS.CURRENT-LOCATION.MATERIAL", "COMPASS")),
                replacePlaceholders(menus().getString(namePath, fallbackName), snapshot),
                lore
        ));

        if (!canTeleport) {
            return;
        }

        slotActions.put(slot, (player, clickType) -> teleportToCurrentLocation(player));
    }

    private void buildPunishmentsButton(Player viewer) {
        if (!plugin.getPunishmentManager().canView(viewer)) {
            return;
        }

        int slot = menus().getInt(MENU_PATH + ".BUTTONS.PUNISHMENTS.SLOT", 42);
        List<String> lore = replacePlaceholders(
                menus().getStringList(MENU_PATH + ".BUTTONS.PUNISHMENTS.LORE"),
                snapshot
        );
        if (lore.isEmpty()) {
            lore = List.of(
                    "&7ᴠɪᴇᴡ ᴛʜɪѕ ᴘʟᴀʏᴇʀ'ѕ ᴘᴜɴɪѕʜᴍᴇɴᴛ ʜɪѕᴛᴏʀʏ.",
                    "&aᴄʟɪᴄᴋ ᴛᴏ ᴏᴘᴇɴ"
            );
        }

        set(slot, ItemUtils.createItem(
                ItemUtils.parseMaterial(menus().getString(MENU_PATH + ".BUTTONS.PUNISHMENTS.MATERIAL", "IRON_BARS")),
                replacePlaceholders(menus().getString(MENU_PATH + ".BUTTONS.PUNISHMENTS.DISPLAY-NAME", "&bᴘᴜɴɪѕʜᴍᴇɴᴛѕ"), snapshot),
                lore
        ));
        slotActions.put(slot, (player, clickType) -> new PunishmentHistoryMenu(plugin, targetUuid, true).open(player));
    }

    private void buildRefreshButton() {
        int slot = menus().getInt(MENU_PATH + ".BUTTONS.REFRESH.SLOT", 49);
        List<String> lore = replacePlaceholders(
                menus().getStringList(MENU_PATH + ".BUTTONS.REFRESH.LORE"),
                snapshot
        );
        if (lore.isEmpty()) {
            lore = List.of("&7ʀᴇʟᴏᴀᴅ ᴛʜɪѕ ᴘʟᴀʏᴇʀ'ѕ ᴘʀᴏꜰɪʟᴇ.");
        }

        set(slot, ItemUtils.createItem(
                ItemUtils.parseMaterial(menus().getString(MENU_PATH + ".BUTTONS.REFRESH.MATERIAL", "CLOCK")),
                replacePlaceholders(menus().getString(MENU_PATH + ".BUTTONS.REFRESH.DISPLAY-NAME", "&bʀᴇꜰʀᴇѕʜ"), snapshot),
                lore
        ));
        slotActions.put(slot, (player, clickType) -> build(player));
    }

    private void teleportToCurrentLocation(Player viewer) {
        ProfileSnapshot latest = plugin.getProfileViewerManager().resolveProfile(targetUuid).orElse(null);
        if (latest == null || !latest.isOnline() || !latest.hasCurrentLocation()) {
            viewer.sendMessage(ColorUtils.toComponent("&cᴛʜᴀᴛ ᴘʟᴀʏᴇʀ ɪѕ ɴᴏ ʟᴏɴɢᴇʀ ᴏɴʟɪɴᴇ."));
            build(viewer);
            return;
        }

        Location destination = latest.getCurrentLocation();
        viewer.closeInventory();
        plugin.getTeleportManager().queue(viewer, destination, "PROFILE", null);
    }

    private String resolveStatValue(String key) {
        PlayerData data = snapshot.getPlayerData();
        if (data == null) {
            return "ɴᴏ ᴅᴀᴛᴀ";
        }

        return switch (key.toUpperCase(Locale.ROOT)) {
            case "MONEY" -> "$" + NumberUtils.formatNice(data.getMoney());
            case "SHARDS" -> NumberUtils.format(data.getShards());
            case "KILLS" -> NumberUtils.format(data.getKills());
            case "DEATHS" -> NumberUtils.format(data.getDeaths());
            case "PLAYTIME" -> NumberUtils.formatTimeLong(data.getTotalPlaytimeSeconds());
            case "BLOCKS_PLACED" -> NumberUtils.format(data.getBlocksPlaced());
            case "BLOCKS_BROKEN" -> NumberUtils.format(data.getBlocksBroken());
            case "MOBS_KILLED" -> NumberUtils.format(data.getMobsKilled());
            case "KILL_STREAK" -> NumberUtils.format(data.getKillStreak());
            case "HIGHEST_KILL_STREAK" -> NumberUtils.format(data.getHighestKillStreak());
            case "MONEY_SPENT" -> "$" + NumberUtils.formatNice(data.getMoneySpent());
            case "MONEY_MADE" -> "$" + NumberUtils.formatNice(data.getMoneyMade());
            default -> "ᴜɴᴋɴᴏᴡɴ";
        };
    }

    private ItemStack createPlayerItem(Material material, String displayName, List<String> lore, UUID uuid) {
        if (material != Material.PLAYER_HEAD) {
            return ItemUtils.createItem(material, displayName, lore);
        }

        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta itemMeta = item.getItemMeta();
        if (!(itemMeta instanceof SkullMeta meta)) {
            return ItemUtils.createItem(Material.PLAYER_HEAD, displayName, lore);
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        meta.setOwningPlayer(offlinePlayer);
        meta.displayName(ColorUtils.toComponent(displayName));
        meta.lore(ColorUtils.toComponentList(lore));
        item.setItemMeta(meta);
        return item;
    }

    private String replacePlaceholders(String input, ProfileSnapshot snapshot) {
        if (input == null) {
            return "";
        }

        Location currentLocation = snapshot.getCurrentLocation();
        String worldName = currentLocation == null || currentLocation.getWorld() == null
                ? "ᴜɴᴋɴᴏᴡɴ"
                : friendlyWorldName(currentLocation);
        int x = currentLocation == null ? 0 : currentLocation.getBlockX();
        int y = currentLocation == null ? 0 : currentLocation.getBlockY();
        int z = currentLocation == null ? 0 : currentLocation.getBlockZ();

        return input
                .replace("{username}", snapshot.getUsername())
                .replace("{status}", statusLabel(snapshot))
                .replace("{team}", safeTeamName(snapshot))
                .replace("{homes}", String.valueOf(snapshot.getHomeCount()))
                .replace("{world}", worldName)
                .replace("{x}", String.valueOf(x))
                .replace("{y}", String.valueOf(y))
                .replace("{z}", String.valueOf(z))
                .replace("{afk}", yesNo(snapshot.isAfk()));
    }

    private List<String> replacePlaceholders(List<String> lines, ProfileSnapshot snapshot) {
        List<String> replaced = new ArrayList<>();
        for (String line : lines) {
            replaced.add(replacePlaceholders(line, snapshot));
        }
        return replaced;
    }

    private String safeTeamName(ProfileSnapshot snapshot) {
        return snapshot.getTeamName() == null || snapshot.getTeamName().isBlank()
                ? "ɴᴏɴᴇ"
                : snapshot.getTeamName();
    }

    private String statusLabel(ProfileSnapshot snapshot) {
        if (!snapshot.isOnline()) {
            return "Offline";
        }
        return snapshot.isAfk() ? "ᴏɴʟɪɴᴇ (ᴀꜰᴋ)" : "Online";
    }

    private String currentLocationSummary(ProfileSnapshot snapshot) {
        Location location = snapshot.getCurrentLocation();
        if (location == null || location.getWorld() == null) {
            return "Unavailable";
        }
        return friendlyWorldName(location)
                + " "
                + location.getBlockX() + ", "
                + location.getBlockY() + ", "
                + location.getBlockZ();
    }

    private String friendlyWorldName(Location location) {
        if (location == null || location.getWorld() == null) {
            return "ᴜɴᴋɴᴏᴡɴ";
        }

        World.Environment environment = location.getWorld().getEnvironment();
        return switch (environment) {
            case NETHER -> "Nether";
            case THE_END -> "End";
            default -> "Overworld";
        };
    }

    private String yesNo(boolean value) {
        return value ? "Yes" : "No";
    }

    private FileConfiguration menus() {
        return plugin.getConfigManager().getMenus();
    }

    private static String configuredTitle(UltimateDonutSmp plugin, UUID uuid) {
        String template = plugin.getConfigManager().getMenus().getString(MENU_PATH + ".TITLE", "&8{username}'ѕ ᴘʀᴏꜰɪʟᴇ");
        String username = plugin.getProfileViewerManager().resolveProfile(uuid)
                .map(ProfileSnapshot::getUsername)
                .orElse("ᴜɴᴋɴᴏᴡɴ");
        return template.replace("{username}", username);
    }

    private static int configuredSize(UltimateDonutSmp plugin) {
        int size = plugin.getConfigManager().getMenus().getInt(MENU_PATH + ".SIZE", 54);
        return size >= 27 && size % 9 == 0 ? size : 54;
    }

    @FunctionalInterface
    private interface SlotAction {
        void execute(Player player, ClickType clickType);
    }
}
