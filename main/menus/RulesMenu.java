package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RulesMenu extends BaseMenu {

    private static final String MENU_PATH = "RULES-MENU";
    private static final String BUTTONS_PATH = MENU_PATH + ".BUTTONS";
    private static final String CLICK_SOUND_PATH = "MENUS.BUTTON-CLICK";

    private final List<RulesButton> buttons;
    private final Map<Integer, RulesButton> slotButtons = new HashMap<>();

    public RulesMenu(UltimateDonutSmp plugin) {
        super(plugin, configuredTitle(plugin), configuredSize(plugin));
        this.buttons = loadButtons(plugin, inventory.getSize());
    }

    public boolean hasValidButtons() {
        return !buttons.isEmpty();
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.BLACK_STAINED_GLASS_PANE);
        slotButtons.clear();

        int renderedButtons = 0;
        for (RulesButton button : buttons) {
            if (slotButtons.containsKey(button.slot())) {
                plugin.getLogger().warning("skipping duplicate rules menu slot " + button.slot()
                        + " ꜰᴏʀ ʙᴜᴛᴛᴏɴ " + button.key() + ".");
                continue;
            }

            set(button.slot(), ItemUtils.createItem(button.material(), button.displayName(), button.lore()));
            slotButtons.put(button.slot(), button);
            renderedButtons++;
        }

        if (renderedButtons == 0) {
            setFallbackItem("&cɴᴏ ᴜѕᴀʙʟᴇ ʀᴜʟᴇѕ ʙᴜᴛᴛᴏɴѕ", "&7ꜰɪx ʀᴜʟᴇѕ-ᴍᴇɴᴜ.ʙᴜᴛᴛᴏɴѕ ᴛᴏ ᴜѕᴇ ᴛʜᴇ ɢᴜɪ.");
        }
    }

    @Override
    public void handleClick(int slot, Player player) {
        RulesButton button = slotButtons.get(slot);
        if (button == null) {
            return;
        }

        SoundUtils.play(player, plugin.getConfigManager().getSound(CLICK_SOUND_PATH));
        List<String> clickMessages = button.clickMessages().isEmpty()
                ? defaultClickMessages(button.key(), button.displayName())
                : button.clickMessages();

        for (String line : clickMessages) {
            player.sendMessage(ColorUtils.toComponent(line));
        }
    }

    private void setFallbackItem(String title, String lore) {
        set(inventory.getSize() / 2, ItemUtils.createItem(Material.BARRIER, title, List.of(lore)));
    }

    private static List<RulesButton> loadButtons(UltimateDonutSmp plugin, int inventorySize) {
        FileConfiguration menus = plugin.getConfigManager().getMenus();
        ConfigurationSection buttonsSection = menus.getConfigurationSection(BUTTONS_PATH);
        List<RulesButton> loadedButtons = new ArrayList<>();

        if (buttonsSection == null || buttonsSection.getKeys(false).isEmpty()) {
            plugin.getLogger().warning("no buttons found at " + BUTTONS_PATH + ".");
            return loadedButtons;
        }

        for (String key : buttonsSection.getKeys(false)) {
            ConfigurationSection buttonSection = buttonsSection.getConfigurationSection(key);
            if (buttonSection == null) {
                plugin.getLogger().warning("Skipping " + BUTTONS_PATH + "." + key
                        + " ʙᴇᴄᴀᴜѕᴇ ɪᴛ ɪѕ ɴᴏᴛ ᴀ ѕᴇᴄᴛɪᴏɴ.");
                continue;
            }

            int slot = buttonSection.getInt("SLOT", -1);
            if (slot < 0 || slot >= inventorySize) {
                plugin.getLogger().warning("Skipping " + buttonSection.getCurrentPath()
                        + " ʙᴇᴄᴀᴜѕᴇ ѕʟᴏᴛ " + slot + " ɪѕ ᴏᴜᴛѕɪᴅᴇ ᴍᴇɴᴜ ѕɪᴢᴇ " + inventorySize + ".");
                continue;
            }

            String rawMaterial = buttonSection.getString("MATERIAL");
            if (rawMaterial == null || rawMaterial.isBlank()) {
                plugin.getLogger().warning("Skipping " + buttonSection.getCurrentPath()
                        + " ʙᴇᴄᴀᴜѕᴇ ᴍᴀᴛᴇʀɪᴀʟ ɪѕ ᴍɪѕѕɪɴɢ.");
                continue;
            }

            Material material = Material.matchMaterial(rawMaterial.trim().toUpperCase(Locale.ROOT));
            if (material == null) {
                plugin.getLogger().warning("Skipping " + buttonSection.getCurrentPath()
                        + " ʙᴇᴄᴀᴜѕᴇ ᴍᴀᴛᴇʀɪᴀʟ '" + rawMaterial + "' ɪѕ ɪɴᴠᴀʟɪᴅ.");
                continue;
            }

            loadedButtons.add(new RulesButton(
                    key,
                    slot,
                    material,
                    buttonSection.getString("NAME", prettifyKey(key)),
                    buttonSection.getStringList("LORE"),
                    buttonSection.getStringList("CLICK-MESSAGE")
            ));
        }

        loadedButtons.sort(Comparator.comparingInt(RulesButton::slot));
        return loadedButtons;
    }

    private static String configuredTitle(UltimateDonutSmp plugin) {
        return plugin.getConfigManager().getMenus().getString(MENU_PATH + ".TITLE", "&8ʀᴜʟᴇѕ");
    }

    private static int configuredSize(UltimateDonutSmp plugin) {
        int rawSize = plugin.getConfigManager().getMenus().getInt(MENU_PATH + ".SIZE", 27);
        if (rawSize >= 9 && rawSize <= 54 && rawSize % 9 == 0) {
            return rawSize;
        }

        plugin.getLogger().warning("invalid " + MENU_PATH + ".SIZE value '" + rawSize
                + "'. ꜰᴀʟʟɪɴɢ ʙᴀᴄᴋ ᴛᴏ 27.");
        return 27;
    }

    private static List<String> defaultClickMessages(String key, String displayName) {
        String normalizedKey = key.toUpperCase(Locale.ROOT);
        String strippedName = ColorUtils.strip(displayName).toLowerCase(Locale.ROOT);

        if (normalizedKey.contains("CHAT") || strippedName.contains("chat")) {
            return List.of(
                    "&7ᴋᴇᴇᴘ ᴄʜᴀᴛ ʀᴇѕᴘᴇᴄᴛꜰᴜʟ ᴀɴᴅ ʀᴇᴘᴏʀᴛ ѕᴇʀɪᴏᴜѕ ɪѕѕᴜᴇѕ ɪɴ ᴛʜᴇ ᴅɪѕᴄᴏʀᴅ.",
                    "&7ʙʀᴇᴀᴋɪɴɢ ᴄʜᴀᴛ ʀᴜʟᴇѕ ᴄᴀɴ ʟᴇᴀᴅ ᴛᴏ ᴘᴜɴɪѕʜᴍᴇɴᴛѕ."
            );
        }

        if (normalizedKey.contains("SERVER") || strippedName.contains("server")) {
            return List.of(
                    "&7ʀᴇᴀᴅ ᴛʜᴇ ꜰᴜʟʟ ѕᴇʀᴠᴇʀ ʀᴜʟᴇѕ ᴄᴀʀᴇꜰᴜʟʟʏ ʙᴇꜰᴏʀᴇ ᴘʟᴀʏɪɴɢ.",
                    "&7ᴠɪᴏʟᴀᴛɪɴɢ ѕᴇʀᴠᴇʀ ʀᴜʟᴇѕ ᴍᴀʏ ʀᴇѕᴜʟᴛ ɪɴ ᴘᴜɴɪѕʜᴍᴇɴᴛѕ."
            );
        }

        return List.of("&7ᴍᴀᴋᴇ ѕᴜʀᴇ ʏᴏᴜ ᴜɴᴅᴇʀѕᴛᴀɴᴅ ᴛʜᴇѕᴇ ʀᴜʟᴇѕ ʙᴇꜰᴏʀᴇ ᴄᴏɴᴛɪɴᴜɪɴɢ.");
    }

    private static String prettifyKey(String key) {
        String[] parts = key.toLowerCase(Locale.ROOT).split("_");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                builder.append(part.substring(1));
            }
        }
        return builder.isEmpty() ? "Rules" : builder.toString();
    }

    private record RulesButton(
            String key,
            int slot,
            Material material,
            String displayName,
            List<String> lore,
            List<String> clickMessages
    ) {}
}
