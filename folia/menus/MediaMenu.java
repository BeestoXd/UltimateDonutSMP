package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;

public class MediaMenu extends BaseMenu {

    private static final String MENU_PATH = "MEDIA-MENU";
    private static final String BUTTON_PATH = MENU_PATH + ".MEDIA-BUTTON";

    private int mediaButtonSlot = 13;

    public MediaMenu(UltimateDonutSmp plugin) {
        super(plugin, configuredTitle(plugin), configuredSize(plugin));
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.BLACK_STAINED_GLASS_PANE);

        FileConfiguration menus = plugin.getConfigManager().getMenus();
        mediaButtonSlot = configuredSlot(plugin, inventory.getSize());

        String displayName = menus.getString(BUTTON_PATH + ".DISPLAY-NAME", "&dMedia Rank");
        List<String> lore = menus.getStringList(BUTTON_PATH + ".LORE");

        if (lore.isEmpty()) {
            lore = List.of(
                    "&dRequirements: (only one needed)",
                    "&d- &f25 average viewers on Stream",
                    "&d- &f5k views on a YouTube Video",
                    "&d- &f25k views on a TikTok",
                    "&d- &f50k views on YouTube Short",
                    "",
                    "&dReminders:",
                    "&8- &7Must have the IP on screen",
                    "&8- &7Must be from the new season",
                    "&8- &7Create ticket in discord for the rank",
                    "&8- &7It lasts 90 days and has all top ranks perks"
            );
        }

        set(mediaButtonSlot, ItemUtils.createItem(
                configuredMaterial(plugin),
                displayName,
                lore
        ));
    }

    @Override
    public void handleClick(int slot, Player player) {
        if (slot != mediaButtonSlot) {
            return;
        }

        SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
        player.sendMessage(ColorUtils.toComponent("&dCreate a ticket in Discord to apply for Media Rank."));
        player.sendMessage(ColorUtils.toComponent("&7You only need to meet &fone&7 of the listed requirements."));
    }

    private static String configuredTitle(UltimateDonutSmp plugin) {
        return plugin.getConfigManager().getMenus().getString(MENU_PATH + ".TITLE", "&8Media Rank");
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

    private static int configuredSlot(UltimateDonutSmp plugin, int inventorySize) {
        int slot = plugin.getConfigManager().getMenus().getInt(BUTTON_PATH + ".SLOT", 13);
        if (slot >= 0 && slot < inventorySize) {
            return slot;
        }

        int fallback = Math.min(13, inventorySize - 1);
        plugin.getLogger().warning("Invalid " + BUTTON_PATH + ".SLOT value '" + slot
                + "'. Falling back to slot " + fallback + ".");
        return fallback;
    }

    private static Material configuredMaterial(UltimateDonutSmp plugin) {
        String rawMaterial = plugin.getConfigManager().getMenus()
                .getString(BUTTON_PATH + ".MATERIAL", "PINK_DYE");
        Material material = rawMaterial == null ? null : Material.matchMaterial(rawMaterial.trim().toUpperCase());
        if (material != null) {
            return material;
        }

        plugin.getLogger().warning("Invalid " + BUTTON_PATH + ".MATERIAL value '" + rawMaterial
                + "'. Falling back to PINK_DYE.");
        return Material.PINK_DYE;
    }
}
