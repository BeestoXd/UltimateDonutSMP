package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.commands.HideCommand;
import com.bx.ultimateDonutSmp.managers.HideManager;
import com.bx.ultimateDonutSmp.models.HideState;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.PermissionUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.List;

public class HideMenu extends BaseMenu {

    public HideMenu(UltimateDonutSmp plugin) {
        super(
                plugin,
                plugin.getConfigManager().getHide().getString("GUI.MAIN.TITLE", "&8ʜɪᴅᴇ"),
                normalizeSize(plugin.getConfigManager().getHide().getInt("GUI.MAIN.SIZE", 27))
        );
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.BLACK_STAINED_GLASS_PANE);
        HideManager manager = plugin.getHideManager();
        HideState state = manager.getState(player.getUniqueId());

        set(4, ItemUtils.createItem(
                Material.NAME_TAG,
                "&bʜɪᴅᴇ ѕᴛᴀᴛᴜѕ",
                state == null
                        ? List.of("&7ѕᴛᴀᴛᴜѕ: &cɪɴᴀᴄᴛɪᴠᴇ")
                        : List.of(
                                "&7ѕᴛᴀᴛᴜѕ: &a" + state.mode().name(),
                                "&7ᴀʟɪᴀѕ: &f" + state.alias(),
                                "&7ѕᴋɪɴ: &f" + (state.skinUsername().isBlank() ? "ᴏʀɪɢɪɴᴀʟ" : state.skinUsername())
                        )
        ));
        set(11, ItemUtils.createItem(
                Material.ENDER_EYE,
                "&bѕᴄʀᴀᴍʙʟᴇ",
                List.of("&7ɢᴇɴᴇʀᴀᴛᴇ ᴀ ѕᴛᴀʙʟᴇ ʀᴀɴᴅᴏᴍ ɪᴅᴇɴᴛɪᴛʏ.", "", "&eᴄʟɪᴄᴋ ᴛᴏ ᴀᴄᴛɪᴠᴀᴛᴇ.")
        ));
        set(13, ItemUtils.createItem(
                Material.PLAYER_HEAD,
                "&dᴅɪѕɢᴜɪѕᴇ",
                List.of("&7ᴄʜᴏᴏѕᴇ ᴀ ᴄᴏɴꜰɪɢᴜʀᴇᴅ ɴᴀᴍᴇ ᴀɴᴅ ѕᴋɪɴ.", "", "&eᴄʟɪᴄᴋ ᴛᴏ ѕᴇʟᴇᴄᴛ.")
        ));
        set(15, ItemUtils.createItem(
                Material.RED_DYE,
                "&cʀᴇᴍᴏᴠᴇ ʜɪᴅᴇ",
                List.of("&7ʀᴇѕᴛᴏʀᴇ ʏᴏᴜʀ ʀᴇᴀʟ ɪᴅᴇɴᴛɪᴛʏ.", "", "&eᴄʟɪᴄᴋ ᴛᴏ ʀᴇᴍᴏᴠᴇ.")
        ));
        if (PermissionUtils.has(player, HideManager.ADMIN_PERMISSION)) {
            set(22, ItemUtils.createItem(
                    Material.SPYGLASS,
                    "&cʜɪᴅᴅᴇɴ ᴘʟᴀʏᴇʀѕ",
                    List.of("&7ɪɴѕᴘᴇᴄᴛ ᴀɴᴅ ᴍᴀɴᴀɢᴇ ᴀᴄᴛɪᴠᴇ ʜɪᴅᴇ ѕᴛᴀᴛᴇѕ.", "", "&eᴄʟɪᴄᴋ ᴛᴏ ᴏᴘᴇɴ.")
            ));
        }
    }

    @Override
    public void handleClick(int slot, Player player, ClickType clickType) {
        HideCommand command = new HideCommand(plugin);
        if (slot == 11) {
            player.closeInventory();
            command.sendResult(player, plugin.getHideManager().scramble(player));
        } else if (slot == 13) {
            new DisguiseAliasMenu(plugin, 0).open(player);
        } else if (slot == 15) {
            player.closeInventory();
            command.sendResult(player, plugin.getHideManager().remove(player, false));
        } else if (slot == 22 && PermissionUtils.has(player, HideManager.ADMIN_PERMISSION)) {
            new HideListMenu(plugin, 0).open(player);
        }
    }

    private static int normalizeSize(int configured) {
        int size = Math.max(9, Math.min(54, configured));
        return size - (size % 9);
    }
}
