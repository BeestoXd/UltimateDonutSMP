package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public class VoiceChatConsentMenu extends BaseMenu {

    private static final String ROOT = "VOICE-CHAT-CONSENT-MENU";

    public VoiceChatConsentMenu(UltimateDonutSmp plugin) {
        super(
                plugin,
                plugin.getLanguageManager().menu(
                        ROOT + ".TITLE",
                        plugin.getConfigManager().getMenus().getString(ROOT + ".TITLE", "&8Confirm Voice Chat")),
                plugin.getConfigManager().getMenus().getInt(ROOT + ".SIZE", 27)
        );
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);

        set(slot("INFO-BUTTON", 13), button("INFO-BUTTON", Material.JUKEBOX,
                "&bVoice Chat Policy", List.of("&7Read this before you choose.")));
        set(slot("CONFIRM-BUTTON", 11), button("CONFIRM-BUTTON", Material.LIME_STAINED_GLASS_PANE,
                "&aConfirm", List.of("&fClick to turn voice chat on")));
        set(slot("DECLINE-BUTTON", 15), button("DECLINE-BUTTON", Material.RED_STAINED_GLASS_PANE,
                "&cDecline", List.of("&fVoice chat will stay disabled")));
    }

    @Override
    public void handleClick(int slot, Player player) {
        int confirmSlot = slot("CONFIRM-BUTTON", 11);
        int declineSlot = slot("DECLINE-BUTTON", 15);

        if (slot != confirmSlot && slot != declineSlot) {
            return;
        }

        SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
        player.closeInventory();

        if (slot == confirmSlot) {
            plugin.getVoiceChatConsentManager().accept(player);
        } else {
            plugin.getVoiceChatConsentManager().decline(player);
        }
    }

    private int slot(String button, int fallback) {
        return plugin.getConfigManager().getMenus().getInt(ROOT + "." + button + ".SLOT", fallback);
    }

    private org.bukkit.inventory.ItemStack button(String button, Material fallbackMaterial,
                                                 String fallbackName, List<String> fallbackLore) {
        String path = ROOT + "." + button;
        Material material = ItemUtils.parseMaterial(
                plugin.getConfigManager().getMenus().getString(path + ".MATERIAL", fallbackMaterial.name()));
        if (material == null) {
            material = fallbackMaterial;
        }

        String name = plugin.getLanguageManager().menu(
                path + ".DISPLAY-NAME",
                plugin.getConfigManager().getMenus().getString(path + ".DISPLAY-NAME", fallbackName));

        List<String> configured = plugin.getConfigManager().getMenus().getStringList(path + ".LORE");
        List<String> lore = plugin.getLanguageManager().menuList(
                path + ".LORE", configured.isEmpty() ? fallbackLore : configured);

        return ItemUtils.createItem(material, name, lore);
    }
}
