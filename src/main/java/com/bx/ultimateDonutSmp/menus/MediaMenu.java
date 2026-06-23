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

        String displayName = menus.getString(BUTTON_PATH + ".DISPLAY-NAME", "&dᴍᴇᴅɪᴀ ʀᴀɴᴋ");
        List<String> lore = menus.getStringList(BUTTON_PATH + ".LORE");

        if (lore.isEmpty()) {
            lore = List.of(
                    "&dʀᴇǫᴜɪʀᴇᴍᴇɴᴛѕ: (ᴏɴʟʏ ᴏɴᴇ ɴᴇᴇᴅᴇᴅ)",
                    "&d- &f25 ᴀᴠᴇʀᴀɢᴇ ᴠɪᴇᴡᴇʀѕ ᴏɴ ѕᴛʀᴇᴀᴍ",
                    "&d- &f5ᴋ ᴠɪᴇᴡѕ ᴏɴ ᴀ ʏᴏᴜᴛᴜʙᴇ ᴠɪᴅᴇᴏ",
                    "&d- &f25ᴋ ᴠɪᴇᴡѕ ᴏɴ ᴀ ᴛɪᴋᴛᴏᴋ",
                    "&d- &f50ᴋ ᴠɪᴇᴡѕ ᴏɴ ʏᴏᴜᴛᴜʙᴇ ѕʜᴏʀᴛ",
                    "",
                    "&dʀᴇᴍɪɴᴅᴇʀѕ:",
                    "&8- &7ᴍᴜѕᴛ ʜᴀᴠᴇ ᴛʜᴇ ɪᴘ ᴏɴ ѕᴄʀᴇᴇɴ",
                    "&8- &7ᴍᴜѕᴛ ʙᴇ ꜰʀᴏᴍ ᴛʜᴇ ɴᴇᴡ ѕᴇᴀѕᴏɴ",
                    "&8- &7ᴄʀᴇᴀᴛᴇ ᴛɪᴄᴋᴇᴛ ɪɴ ᴅɪѕᴄᴏʀᴅ ꜰᴏʀ ᴛʜᴇ ʀᴀɴᴋ",
                    "&8- &7ɪᴛ ʟᴀѕᴛѕ 90 ᴅᴀʏѕ ᴀɴᴅ ʜᴀѕ ᴀʟʟ ᴛᴏᴘ ʀᴀɴᴋѕ ᴘᴇʀᴋѕ"
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
        player.sendMessage(ColorUtils.toComponent("&dᴄʀᴇᴀᴛᴇ ᴀ ᴛɪᴄᴋᴇᴛ ɪɴ ᴅɪѕᴄᴏʀᴅ ᴛᴏ ᴀᴘᴘʟʏ ꜰᴏʀ ᴍᴇᴅɪᴀ ʀᴀɴᴋ."));
        player.sendMessage(ColorUtils.toComponent("&7ʏᴏᴜ ᴏɴʟʏ ɴᴇᴇᴅ ᴛᴏ ᴍᴇᴇᴛ &fᴏɴᴇ&7 ᴏꜰ ᴛʜᴇ ʟɪѕᴛᴇᴅ ʀᴇǫᴜɪʀᴇᴍᴇɴᴛѕ."));
    }

    private static String configuredTitle(UltimateDonutSmp plugin) {
        return plugin.getConfigManager().getMenus().getString(MENU_PATH + ".TITLE", "&8ᴍᴇᴅɪᴀ ʀᴀɴᴋ");
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

    private static int configuredSlot(UltimateDonutSmp plugin, int inventorySize) {
        int slot = plugin.getConfigManager().getMenus().getInt(BUTTON_PATH + ".SLOT", 13);
        if (slot >= 0 && slot < inventorySize) {
            return slot;
        }

        int fallback = Math.min(13, inventorySize - 1);
        plugin.getLogger().warning("invalid " + BUTTON_PATH + ".SLOT value '" + slot
                + "'. ꜰᴀʟʟɪɴɢ ʙᴀᴄᴋ ᴛᴏ ѕʟᴏᴛ " + fallback + ".");
        return fallback;
    }

    private static Material configuredMaterial(UltimateDonutSmp plugin) {
        String rawMaterial = plugin.getConfigManager().getMenus()
                .getString(BUTTON_PATH + ".MATERIAL", "PINK_DYE");
        Material material = rawMaterial == null ? null : Material.matchMaterial(rawMaterial.trim().toUpperCase());
        if (material != null) {
            return material;
        }

        plugin.getLogger().warning("invalid " + BUTTON_PATH + ".MATERIAL value '" + rawMaterial
                + "'. ꜰᴀʟʟɪɴɢ ʙᴀᴄᴋ ᴛᴏ ᴘɪɴᴋ_ᴅʏᴇ.");
        return Material.PINK_DYE;
    }
}
