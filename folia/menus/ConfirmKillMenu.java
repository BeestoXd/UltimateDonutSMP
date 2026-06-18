package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ConfirmKillMenu extends BaseMenu {

    public ConfirmKillMenu(UltimateDonutSmp plugin) {
        super(
                plugin,
                plugin.getConfigManager().getMenus().getString("CONFIRM-KILL-MENU.TITLE", "&8ᴄᴏɴꜰɪʀᴍ ᴋɪʟʟ"),
                plugin.getConfigManager().getMenus().getInt("CONFIRM-KILL-MENU.SIZE", 27)
        );
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);

        String cancelTitle = plugin.getConfigManager().getMenus().getString("CONFIRM-KILL-MENU.CANCEL-BUTTON.TITLE", "&cᴄᴀɴᴄᴇʟ");
        String cancelMaterialStr = plugin.getConfigManager().getMenus().getString("CONFIRM-KILL-MENU.CANCEL-BUTTON.MATERIAL", "RED_STAINED_GLASS_PANE");
        Material cancelMaterial = Material.matchMaterial(cancelMaterialStr);
        if (cancelMaterial == null) cancelMaterial = Material.RED_STAINED_GLASS_PANE;
        List<String> cancelLore = plugin.getConfigManager().getMenus().getStringList("CONFIRM-KILL-MENU.CANCEL-BUTTON.LORE");
        if (cancelLore.isEmpty()) {
            cancelLore = List.of("&7ᴄʟɪᴄᴋ ᴛᴏ ᴄᴀɴᴄᴇʟ ᴀɴᴅ ʟɪᴠᴇ.");
        }

        String confirmTitle = plugin.getConfigManager().getMenus().getString("CONFIRM-KILL-MENU.CONFIRM-BUTTON.TITLE", "&aᴄᴏɴꜰɪʀᴍ");
        String confirmMaterialStr = plugin.getConfigManager().getMenus().getString("CONFIRM-KILL-MENU.CONFIRM-BUTTON.MATERIAL", "LIME_STAINED_GLASS_PANE");
        Material confirmMaterial = Material.matchMaterial(confirmMaterialStr);
        if (confirmMaterial == null) confirmMaterial = Material.LIME_STAINED_GLASS_PANE;
        List<String> confirmLore = plugin.getConfigManager().getMenus().getStringList("CONFIRM-KILL-MENU.CONFIRM-BUTTON.LORE");
        if (confirmLore.isEmpty()) {
            confirmLore = List.of("&7ᴄʟɪᴄᴋ ᴛᴏ ᴄᴏɴꜰɪʀᴍ ѕᴜɪᴄɪᴅᴇ.");
        }

        String skullTitle = plugin.getConfigManager().getMenus().getString("CONFIRM-KILL-MENU.SKULL-BUTTON.TITLE", "&cᴋɪʟʟ ʏᴏᴜʀѕᴇʟꜰ");
        String skullMaterialStr = plugin.getConfigManager().getMenus().getString("CONFIRM-KILL-MENU.SKULL-BUTTON.MATERIAL", "SKELETON_SKULL");
        Material skullMaterial = Material.matchMaterial(skullMaterialStr);
        if (skullMaterial == null) skullMaterial = Material.SKELETON_SKULL;
        List<String> skullLore = plugin.getConfigManager().getMenus().getStringList("CONFIRM-KILL-MENU.SKULL-BUTTON.LORE");
        if (skullLore.isEmpty()) {
            skullLore = List.of("&7ᴀʀᴇ ʏᴏᴜ ѕᴜʀᴇ ʏᴏᴜ ᴡᴀɴᴛ", "&7ᴛᴏ ᴇɴᴅ ʏᴏᴜʀ ʟɪꜰᴇ?");
        }

        int cancelSlot = plugin.getConfigManager().getMenus().getInt("CONFIRM-KILL-MENU.CANCEL-BUTTON.SLOT", 11);
        int confirmSlot = plugin.getConfigManager().getMenus().getInt("CONFIRM-KILL-MENU.CONFIRM-BUTTON.SLOT", 15);
        int skullSlot = plugin.getConfigManager().getMenus().getInt("CONFIRM-KILL-MENU.SKULL-BUTTON.SLOT", 13);

        set(cancelSlot, ItemUtils.createItem(cancelMaterial, cancelTitle, cancelLore));
        set(confirmSlot, ItemUtils.createItem(confirmMaterial, confirmTitle, confirmLore));

        ItemStack skullItem;
        if (skullMaterial == Material.PLAYER_HEAD) {
            skullItem = ItemUtils.createPlayerHead(player, skullTitle, skullLore);
        } else {
            skullItem = ItemUtils.createItem(skullMaterial, skullTitle, skullLore);
        }
        set(skullSlot, skullItem);
    }

    @Override
    public void handleClick(int slot, Player player) {
        int cancelSlot = plugin.getConfigManager().getMenus().getInt("CONFIRM-KILL-MENU.CANCEL-BUTTON.SLOT", 11);
        int confirmSlot = plugin.getConfigManager().getMenus().getInt("CONFIRM-KILL-MENU.CONFIRM-BUTTON.SLOT", 15);

        if (slot != cancelSlot && slot != confirmSlot) {
            return;
        }

        SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
        player.closeInventory();

        if (slot == confirmSlot) {
            player.setHealth(0.0D);
        }
    }
}
