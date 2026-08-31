package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RanksMenu extends BaseMenu {

    private static final String MENU_PATH = "RANKS-MENU";
    private static final String BUTTONS_PATH = MENU_PATH + ".BUTTONS";
    private static final String CLICK_SOUND_PATH = "MENUS.BUTTON-CLICK";

    private final List<RankButton> buttons;
    private final Map<Integer, RankButton> slotButtons = new HashMap<>();

    public RanksMenu(UltimateDonutSmp plugin) {
        super(plugin, configuredTitle(plugin), configuredSize(plugin));
        this.buttons = loadButtons(plugin, inventory.getSize());
    }

    public boolean hasValidButtons() {
        return !buttons.isEmpty();
    }

    @Override
    public void build(Player player) {
        clear();
        // The bundled layout leaves the empty slots empty; owners who want a border set a filler.
        Material filler = configuredFiller(plugin);
        if (filler != null) {
            fill(filler);
        }
        slotButtons.clear();

        for (RankButton button : buttons) {
            if (slotButtons.containsKey(button.slot())) {
                plugin.getLogger().warning("skipping duplicate ranks menu slot " + button.slot()
                        + " for button " + button.key() + ".");
                continue;
            }

            set(button.slot(), createIcon(button));
            slotButtons.put(button.slot(), button);
        }

        if (slotButtons.isEmpty()) {
            set(inventory.getSize() / 2, ItemUtils.createItem(
                    Material.BARRIER,
                    "&cNo usable rank buttons",
                    List.of("&7Fix " + BUTTONS_PATH + " in menus.yml to use the GUI.")
            ));
        }
    }

    @Override
    public void handleClick(int slot, Player player) {
        RankButton button = slotButtons.get(slot);
        if (button == null) {
            return;
        }

        SoundUtils.play(player, plugin.getConfigManager().getSound(CLICK_SOUND_PATH));

        // The bundled ranks ship without a command because their buttons only advertise perks.
        if (button.command() == null) {
            return;
        }

        player.closeInventory();
        plugin.getSpigotScheduler().runEntity(player, () -> {
            if (!player.isOnline()) {
                return;
            }

            if (!player.performCommand(button.command())) {
                player.sendMessage(ColorUtils.toComponent("&cThat action is unavailable right now."));
            }
        });
    }

    private ItemStack createIcon(RankButton button) {
        if (button.material() == Material.PLAYER_HEAD && button.headTexture() != null) {
            return ItemUtils.createHeadFromSkinUrl(button.headTexture(), button.displayName(), button.lore());
        }
        return ItemUtils.createItem(button.material(), button.displayName(), button.lore());
    }

    private static List<RankButton> loadButtons(UltimateDonutSmp plugin, int inventorySize) {
        FileConfiguration menus = plugin.getConfigManager().getMenus();
        ConfigurationSection buttonsSection = menus.getConfigurationSection(BUTTONS_PATH);
        List<RankButton> loadedButtons = new ArrayList<>();

        if (buttonsSection == null || buttonsSection.getKeys(false).isEmpty()) {
            plugin.getLogger().warning("no buttons found at " + BUTTONS_PATH + ".");
            return loadedButtons;
        }

        for (String key : buttonsSection.getKeys(false)) {
            ConfigurationSection buttonSection = buttonsSection.getConfigurationSection(key);
            if (buttonSection == null) {
                plugin.getLogger().warning("Skipping " + BUTTONS_PATH + "." + key
                        + " because it is not a section.");
                continue;
            }

            int slot = buttonSection.getInt("SLOT", -1);
            if (slot < 0 || slot >= inventorySize) {
                plugin.getLogger().warning("Skipping " + buttonSection.getCurrentPath()
                        + " because slot " + slot + " is outside menu size " + inventorySize + ".");
                continue;
            }

            String rawMaterial = buttonSection.getString("MATERIAL");
            if (rawMaterial == null || rawMaterial.isBlank()) {
                plugin.getLogger().warning("Skipping " + buttonSection.getCurrentPath()
                        + " because MATERIAL is missing.");
                continue;
            }

            Material material = Material.matchMaterial(rawMaterial.trim().toUpperCase(Locale.ROOT));
            if (material == null) {
                plugin.getLogger().warning("Skipping " + buttonSection.getCurrentPath()
                        + " because material '" + rawMaterial + "' is invalid.");
                continue;
            }

            String displayName = firstNonBlank(
                    buttonSection.getString("DISPLAY-NAME"),
                    buttonSection.getString("NAME"),
                    prettifyKey(key)
            );

            loadedButtons.add(new RankButton(
                    key,
                    slot,
                    material,
                    plugin.getCurrencyManager().applyStaticPlaceholders(displayName),
                    plugin.getCurrencyManager().applyStaticPlaceholders(buttonSection.getStringList("LORE")),
                    blankToNull(buttonSection.getString("HEAD-TEXTURE")),
                    sanitizeCommand(buttonSection.getString("COMMAND"))
            ));
        }

        loadedButtons.sort(Comparator.comparingInt(RankButton::slot));
        return loadedButtons;
    }

    private static String configuredTitle(UltimateDonutSmp plugin) {
        return plugin.getConfigManager().getMenus().getString(MENU_PATH + ".TITLE", "&8Ranks");
    }

    private static int configuredSize(UltimateDonutSmp plugin) {
        int rawSize = plugin.getConfigManager().getMenus().getInt(MENU_PATH + ".SIZE", 27);
        if (rawSize >= 9 && rawSize <= 54 && rawSize % 9 == 0) {
            return rawSize;
        }

        plugin.getLogger().warning("invalid " + MENU_PATH + ".SIZE value '" + rawSize
                + "'. Falling back to 27.");
        return 27;
    }

    private static Material configuredFiller(UltimateDonutSmp plugin) {
        String rawMaterial = blankToNull(plugin.getConfigManager().getMenus()
                .getString(MENU_PATH + ".FILLER-MATERIAL"));
        if (rawMaterial == null) {
            return null;
        }

        Material material = Material.matchMaterial(rawMaterial.toUpperCase(Locale.ROOT));
        if (material == null) {
            plugin.getLogger().warning("invalid " + MENU_PATH + ".FILLER-MATERIAL value '" + rawMaterial
                    + "'. Leaving the empty slots empty.");
        }
        return material;
    }

    static String sanitizeCommand(String rawCommand) {
        String command = blankToNull(rawCommand);
        if (command == null) {
            return null;
        }

        if (command.startsWith("/")) {
            command = command.substring(1);
        }
        return blankToNull(command);
    }

    private static String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    static String prettifyKey(String key) {
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
        return builder.isEmpty() ? "Rank" : builder.toString();
    }

    private record RankButton(
            String key,
            int slot,
            Material material,
            String displayName,
            List<String> lore,
            String headTexture,
            String command
    ) {}
}
