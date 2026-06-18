package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.FeatureManager;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FeatureToggleMenu extends BaseMenu {

    private static final String MENU_PATH = "FEATURE-TOGGLE-MENU";

    private final int page;
    private final Map<Integer, FeatureManager.Feature> featureSlots = new HashMap<>();

    public FeatureToggleMenu(UltimateDonutSmp plugin) {
        this(plugin, 0);
    }

    public FeatureToggleMenu(UltimateDonutSmp plugin, int page) {
        super(plugin, configuredTitle(plugin, page), configuredSize(plugin));
        this.page = Math.max(0, page);
    }

    @Override
    public void build(Player player) {
        clear();
        featureSlots.clear();

        FileConfiguration menus = menus();
        if (menus.getBoolean(MENU_PATH + ".PLACEHOLDER", true)) {
            fill(ItemUtils.parseMaterial(menus.getString(MENU_PATH + ".PLACEHOLDER-MATERIAL", "BLACK_STAINED_GLASS_PANE")));
        }

        List<Integer> contentSlots = contentSlots();
        List<FeatureManager.Feature> features = plugin.getFeatureManager().getFeatures();
        int itemsPerPage = Math.max(1, contentSlots.size());
        int totalPages = Math.max(1, (int) Math.ceil(features.size() / (double) itemsPerPage));
        int safePage = Math.max(0, Math.min(page, totalPages - 1));
        int start = safePage * itemsPerPage;
        int end = Math.min(features.size(), start + itemsPerPage);

        for (int index = start; index < end; index++) {
            FeatureManager.Feature feature = features.get(index);
            int slot = contentSlots.get(index - start);
            set(slot, createFeatureItem(feature));
            featureSlots.put(slot, feature);
        }

        renderButton("PREVIOUS", safePage > 0, safePage, totalPages);
        renderButton("NEXT", safePage + 1 < totalPages, safePage, totalPages);
        renderButton("CLOSE", true, safePage, totalPages);
    }

    @Override
    public void handleClick(int slot, Player player, ClickType clickType) {
        SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));

        int previousSlot = menus().getInt(MENU_PATH + ".BUTTONS.PREVIOUS.SLOT", inventory.getSize() - 9);
        int nextSlot = menus().getInt(MENU_PATH + ".BUTTONS.NEXT.SLOT", inventory.getSize() - 1);
        int closeSlot = menus().getInt(MENU_PATH + ".BUTTONS.CLOSE.SLOT", inventory.getSize() - 5);

        if (slot == previousSlot && page > 0) {
            new FeatureToggleMenu(plugin, page - 1).open(player);
            return;
        }
        if (slot == nextSlot && hasNextPage()) {
            new FeatureToggleMenu(plugin, page + 1).open(player);
            return;
        }
        if (slot == closeSlot) {
            player.closeInventory();
            return;
        }

        FeatureManager.Feature feature = featureSlots.get(slot);
        if (feature == null) {
            return;
        }

        plugin.getFeatureManager().toggle(feature);
        player.sendMessage(com.bx.ultimateDonutSmp.utils.ColorUtils.toComponent(
                plugin.getConfigManager().getMessageOrDefault(
                        "FEATURES.TOGGLED",
                        "&a{feature} ɪѕ ɴᴏᴡ {state}.",
                        "{feature}", feature.displayName(),
                        "{feature_key}", feature.configKey(),
                        "{state}", plugin.getFeatureManager().statusText(feature)
                )
        ));
        new FeatureToggleMenu(plugin, page).open(player);
    }

    private boolean hasNextPage() {
        int itemsPerPage = Math.max(1, contentSlots().size());
        return (page + 1) * itemsPerPage < plugin.getFeatureManager().getFeatures().size();
    }

    private org.bukkit.inventory.ItemStack createFeatureItem(FeatureManager.Feature feature) {
        boolean enabled = plugin.getFeatureManager().isEnabled(feature);
        FileConfiguration menus = menus();
        String featurePath = MENU_PATH + ".FEATURES." + feature.configKey();
        String stateKey = enabled ? "ENABLED" : "DISABLED";

        String material = firstNonBlank(
                menus.getString(featurePath + "." + stateKey + "-MATERIAL"),
                menus.getString(featurePath + ".MATERIAL"),
                menus.getString(MENU_PATH + ".DEFAULTS." + stateKey + "-MATERIAL"),
                feature.iconMaterial()
        );
        String displayName = firstNonBlank(
                menus.getString(featurePath + ".DISPLAY-NAME"),
                menus.getString(MENU_PATH + ".DEFAULTS.DISPLAY-NAME"),
                "&b{feature}"
        );
        List<String> lore = menus.getStringList(featurePath + ".LORE");
        if (lore.isEmpty()) {
            lore = menus.getStringList(MENU_PATH + ".DEFAULTS.LORE");
        }
        if (lore.isEmpty()) {
            lore = List.of("&7{description}", "", "&7ѕᴛᴀᴛᴜѕ: {state}", "&eᴄʟɪᴄᴋ ᴛᴏ ᴛᴏɢɢʟᴇ.");
        }

        Map<String, String> placeholders = placeholders(feature);
        return ItemUtils.createItem(
                ItemUtils.parseMaterial(material),
                replace(displayName, placeholders),
                replace(lore, placeholders)
        );
    }

    private void renderButton(String key, boolean active, int safePage, int totalPages) {
        String path = MENU_PATH + ".BUTTONS." + key;
        if (!menus().contains(path)) {
            return;
        }

        int slot = menus().getInt(path + ".SLOT", -1);
        if (slot < 0 || slot >= inventory.getSize()) {
            return;
        }

        Material material = ItemUtils.parseMaterial(menus().getString(path + ".MATERIAL", active ? "ARROW" : "BARRIER"));
        String displayName = menus().getString(path + ".DISPLAY-NAME", "&b" + prettify(key));
        List<String> lore = menus().getStringList(path + ".LORE");
        Map<String, String> placeholders = Map.of(
                "page", String.valueOf(safePage + 1),
                "next_page", String.valueOf(Math.min(totalPages, safePage + 2)),
                "previous_page", String.valueOf(Math.max(1, safePage)),
                "total_pages", String.valueOf(totalPages)
        );
        set(slot, ItemUtils.createItem(material, replace(displayName, placeholders), replace(lore, placeholders)));
    }

    private List<Integer> contentSlots() {
        List<Integer> configured = menus().getIntegerList(MENU_PATH + ".CONTENT-SLOTS");
        if (!configured.isEmpty()) {
            return configured.stream()
                    .filter(slot -> slot >= 0 && slot < inventory.getSize())
                    .toList();
        }

        List<Integer> fallback = new ArrayList<>();
        int navigationStart = Math.max(0, inventory.getSize() - 9);
        for (int slot = 0; slot < navigationStart; slot++) {
            fallback.add(slot);
        }
        return fallback;
    }

    private Map<String, String> placeholders(FeatureManager.Feature feature) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("feature", feature.displayName());
        placeholders.put("feature_key", feature.configKey());
        placeholders.put("description", feature.description());
        placeholders.put("state", plugin.getFeatureManager().statusText(feature));
        placeholders.put("raw_state", plugin.getFeatureManager().isEnabled(feature) ? "ᴇɴᴀʙʟᴇᴅ" : "ᴅɪѕᴀʙʟᴇᴅ");
        return placeholders;
    }

    private String replace(String value, Map<String, String> placeholders) {
        String output = value == null ? "" : value;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            output = output.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return output;
    }

    private List<String> replace(List<String> values, Map<String, String> placeholders) {
        List<String> replaced = new ArrayList<>();
        for (String value : values) {
            replaced.add(replace(value, placeholders));
        }
        return replaced;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "STONE";
    }

    private static String prettify(String key) {
        return switch (key.toUpperCase(Locale.ROOT)) {
            case "PREVIOUS" -> "ᴘʀᴇᴠɪᴏᴜѕ";
            case "NEXT" -> "ɴᴇxᴛ";
            case "CLOSE" -> "ᴄʟᴏѕᴇ";
            default -> key.toLowerCase(Locale.ROOT).replace('_', ' ');
        };
    }

    private FileConfiguration menus() {
        return plugin.getConfigManager().getMenus();
    }

    private static String configuredTitle(UltimateDonutSmp plugin, int page) {
        return plugin.getConfigManager().getMenus()
                .getString(MENU_PATH + ".TITLE", "&8ꜰᴇᴀᴛᴜʀᴇ ᴛᴏɢɢʟᴇѕ")
                .replace("{page}", String.valueOf(Math.max(1, page + 1)));
    }

    private static int configuredSize(UltimateDonutSmp plugin) {
        int size = plugin.getConfigManager().getMenus().getInt(MENU_PATH + ".SIZE", 54);
        return size >= 9 && size <= 54 && size % 9 == 0 ? size : 54;
    }
}
