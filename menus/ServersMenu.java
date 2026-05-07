package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.ServerStatusSnapshot;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ServersMenu extends BaseMenu {

    private static final String MENU_PATH = "SERVERS-MENU";
    private static final String TEMPLATE_PATH = MENU_PATH + ".SERVER_STATUS";
    private static final String SERVERS_PATH = MENU_PATH + ".SERVERS";
    private static final String CLICK_SOUND_PATH = "MENUS.BUTTON-CLICK";

    private final Map<Integer, String> slotServerIds = new HashMap<>();
    private ScheduledTask refreshTask;

    public ServersMenu(UltimateDonutSmp plugin) {
        super(plugin, configuredTitle(plugin), configuredSize(plugin));
    }

    public boolean hasRenderableServers() {
        return !resolveMenuEntries().isEmpty();
    }

    @Override
    public void open(Player player) {
        build(player);
        player.openInventory(inventory);
        startRefreshTask(player);
    }

    @Override
    public void build(Player player) {
        cancelRefreshTask();
        render(player);
    }

    @Override
    public void handleClick(int slot, Player player) {
        String serverId = slotServerIds.get(slot);
        if (serverId == null) {
            return;
        }

        SoundUtils.play(player, plugin.getConfigManager().getSound(CLICK_SOUND_PATH));

        if (!plugin.getNetworkStatusManager().isKnownServer(serverId)) {
            player.sendMessage(ColorUtils.toComponent("&cThat server is not configured in network.yml."));
            return;
        }

        player.sendMessage(ColorUtils.toComponent(
                "&7Refreshing status for &f" + plugin.getNetworkStatusManager().resolveDisplayName(serverId) + "&7..."
        ));
        plugin.getNetworkStatusManager().requestImmediateRefresh(serverId);
    }

    @Override
    public void onClose(Player player) {
        cancelRefreshTask();
    }

    private void render(Player player) {
        clear();
        fill(configuredPlaceholderMaterial());
        slotServerIds.clear();

        List<MenuEntry> entries = resolveMenuEntries();
        if (entries.isEmpty()) {
            setFallbackItem("&cNo servers configured", "&7Configure SERVERS-MENU and network.yml first.");
            return;
        }

        int rendered = 0;
        Set<Integer> usedSlots = new HashSet<>();
        for (MenuEntry entry : entries) {
            if (!usedSlots.add(entry.slot())) {
                plugin.getLogger().warning("Skipping duplicate Servers Menu slot " + entry.slot()
                        + " for server " + entry.serverId() + ".");
                continue;
            }

            ServerStatusSnapshot snapshot = plugin.getNetworkStatusManager().getSnapshot(entry.serverId());
            set(entry.slot(), createServerItem(snapshot));
            slotServerIds.put(entry.slot(), entry.serverId());
            rendered++;
        }

        if (rendered == 0) {
            setFallbackItem("&cNo usable server buttons", "&7Fix SERVERS-MENU.SERVERS first.");
        }
    }

    private void startRefreshTask(Player player) {
        long refreshTicks = configuredRefreshTicks();
        if (refreshTicks <= 0L) {
            return;
        }

        refreshTask = plugin.getPaperScheduler().runEntityTimer(player, () -> {
            if (!player.isOnline() || player.getOpenInventory().getTopInventory() != inventory) {
                cancelRefreshTask();
                return;
            }

            render(player);
        }, refreshTicks, refreshTicks);
    }

    private void cancelRefreshTask() {
        if (refreshTask != null) {
            refreshTask.cancel();
            refreshTask = null;
        }
    }

    private org.bukkit.inventory.ItemStack createServerItem(ServerStatusSnapshot snapshot) {
        return ItemUtils.createItem(
                snapshot.online() ? configuredOnlineMaterial() : configuredOfflineMaterial(),
                applyPlaceholders(configuredServerNameTemplate(), snapshot),
                applyPlaceholders(configuredLoreTemplate(), snapshot)
        );
    }

    private void setFallbackItem(String title, String lore) {
        set(inventory.getSize() / 2, ItemUtils.createItem(Material.BARRIER, title, List.of(lore)));
    }

    private List<MenuEntry> resolveMenuEntries() {
        List<MenuEntry> entries = loadConfiguredEntries();
        if (!entries.isEmpty()) {
            return entries;
        }

        List<Integer> fallbackSlots = defaultContentSlots(inventory.getSize());
        List<MenuEntry> fallbackEntries = new ArrayList<>();
        int index = 0;
        for (String serverId : plugin.getNetworkStatusManager().getOrderedServerIds()) {
            if (index >= fallbackSlots.size()) {
                break;
            }
            fallbackEntries.add(new MenuEntry(serverId, fallbackSlots.get(index++)));
        }
        return fallbackEntries;
    }

    private List<MenuEntry> loadConfiguredEntries() {
        List<MenuEntry> entries = new ArrayList<>();
        ConfigurationSection serversSection = menus().getConfigurationSection(SERVERS_PATH);
        if (serversSection == null || serversSection.getKeys(false).isEmpty()) {
            return entries;
        }

        for (String serverId : serversSection.getKeys(false)) {
            ConfigurationSection serverSection = serversSection.getConfigurationSection(serverId);
            if (serverSection == null) {
                plugin.getLogger().warning("Skipping " + SERVERS_PATH + "." + serverId
                        + " because it is not a section.");
                continue;
            }

            int slot = serverSection.getInt("SLOT", -1);
            if (slot < 0 || slot >= inventory.getSize()) {
                plugin.getLogger().warning("Skipping " + serverSection.getCurrentPath()
                        + " because slot " + slot + " is outside menu size " + inventory.getSize() + ".");
                continue;
            }

            entries.add(new MenuEntry(serverId.toLowerCase(Locale.ROOT), slot));
        }

        entries.sort(Comparator.comparingInt(MenuEntry::slot));
        return entries;
    }

    private List<Integer> defaultContentSlots(int inventorySize) {
        List<Integer> slots = new ArrayList<>();
        int rows = inventorySize / 9;

        if (rows >= 3) {
            for (int row = 1; row <= rows - 2; row++) {
                for (int column = 1; column <= 7; column++) {
                    slots.add(row * 9 + column);
                }
            }
        }

        for (int slot = 0; slot < inventorySize; slot++) {
            if (!slots.contains(slot)) {
                slots.add(slot);
            }
        }
        return slots;
    }

    private String applyPlaceholders(String template, ServerStatusSnapshot snapshot) {
        return template
                .replace("%server%", snapshot.displayName())
                .replace("%status%", snapshot.online() ? "&aOnline" : "&cOffline")
                .replace("%players%", String.valueOf(snapshot.playerCount()))
                .replace("%software%", snapshot.softwareLabel())
                .replace("%performance%", snapshot.performanceLabel())
                .replace("%latency%", String.valueOf(snapshot.latencyMs()));
    }

    private List<String> applyPlaceholders(List<String> lines, ServerStatusSnapshot snapshot) {
        List<String> replaced = new ArrayList<>();
        for (String line : lines) {
            replaced.add(applyPlaceholders(line, snapshot));
        }
        return replaced;
    }

    private FileConfiguration menus() {
        return plugin.getConfigManager().getMenus();
    }

    private Material configuredPlaceholderMaterial() {
        String raw = menus().getString(MENU_PATH + ".PLACEHOLDER-MATERIAL", "BLACK_STAINED_GLASS_PANE");
        Material material = Material.matchMaterial(raw == null ? "" : raw.trim().toUpperCase(Locale.ROOT));
        return material == null ? Material.BLACK_STAINED_GLASS_PANE : material;
    }

    private Material configuredOnlineMaterial() {
        return configuredStatusMaterial("ONLINE", Material.LIME_CONCRETE);
    }

    private Material configuredOfflineMaterial() {
        return configuredStatusMaterial("OFFLINE", Material.RED_CONCRETE);
    }

    private Material configuredStatusMaterial(String key, Material fallback) {
        String raw = menus().getString(TEMPLATE_PATH + ".MATERIALS." + key, fallback.name());
        Material material = Material.matchMaterial(raw == null ? "" : raw.trim().toUpperCase(Locale.ROOT));
        if (material != null) {
            return material;
        }

        plugin.getLogger().warning("Invalid " + TEMPLATE_PATH + ".MATERIALS." + key
                + " value '" + raw + "'. Falling back to " + fallback + ".");
        return fallback;
    }

    private String configuredServerNameTemplate() {
        return menus().getString(TEMPLATE_PATH + ".SERVER_NAME",
                menus().getString(TEMPLATE_PATH + ".NAME", "&b%server%"));
    }

    private List<String> configuredLoreTemplate() {
        List<String> lore = menus().getStringList(TEMPLATE_PATH + ".LORE");
        if (!lore.isEmpty()) {
            return lore;
        }

        return List.of(
                "&8&m---------------------",
                "&bStatus: %status%",
                "&aPlayers: &a%players% online",
                "&eSoftware: &a%software%",
                "&6Performance: %performance%",
                "&8&m---------------------"
        );
    }

    private long configuredRefreshTicks() {
        return Math.max(10L, menus().getLong(MENU_PATH + ".REFRESH-TICKS", 40L));
    }

    private static String configuredTitle(UltimateDonutSmp plugin) {
        return plugin.getConfigManager().getMenus().getString(MENU_PATH + ".TITLE",
                plugin.getConfigManager().getMenus().getString(TEMPLATE_PATH + ".TITLE", "&8Ongoing Servers"));
    }

    private static int configuredSize(UltimateDonutSmp plugin) {
        int rawSize = plugin.getConfigManager().getMenus().getInt(MENU_PATH + ".SIZE", 27);
        if (rawSize >= 9 && rawSize <= 54 && rawSize % 9 == 0) {
            return rawSize;
        }

        plugin.getLogger().warning("Invalid " + MENU_PATH + ".SIZE value '" + rawSize
                + "'. Falling back to 27.");
        return 27;
    }

    private record MenuEntry(String serverId, int slot) {}
}
