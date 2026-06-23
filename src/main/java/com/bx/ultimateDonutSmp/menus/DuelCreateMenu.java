package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.DuelManager;
import com.bx.ultimateDonutSmp.models.DuelMapSelection;
import com.bx.ultimateDonutSmp.models.DuelPrivacyMode;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DuelCreateMenu extends BaseMenu {

    private final UUID targetUuid;
    private final DuelPrivacyMode privacyMode;

    public DuelCreateMenu(UltimateDonutSmp plugin, UUID targetUuid) {
        this(plugin, targetUuid, DuelPrivacyMode.INVITE_ONLY);
    }

    public DuelCreateMenu(UltimateDonutSmp plugin, UUID targetUuid, DuelPrivacyMode privacyMode) {
        super(plugin, plugin.getDuelManager().getCreateTitle(Bukkit.getPlayer(targetUuid)), plugin.getDuelManager().getCreateSize());
        this.targetUuid = targetUuid;
        this.privacyMode = privacyMode == null ? DuelPrivacyMode.INVITE_ONLY : privacyMode;
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);

        Player target = Bukkit.getPlayer(targetUuid);
        if (target == null) {
            set(13, ItemUtils.createItem(Material.BARRIER, "&cᴛᴀʀɢᴇᴛ ᴏꜰꜰʟɪɴᴇ", List.of("&7ᴛʜɪѕ ᴘʟᴀʏᴇʀ ɪѕ ɴᴏ ʟᴏɴɢᴇʀ ᴏɴʟɪɴᴇ.")));
            set(inventory.getSize() - 1, ItemUtils.createItem(Material.BARRIER, "&cᴄʟᴏѕᴇ"));
            return;
        }

        List<DuelManager.DuelMapOption> options = plugin.getDuelManager().getSelectableMapOptions(false);
        int[] slots = contentSlots();
        for (int i = 0; i < Math.min(options.size(), slots.length); i++) {
            DuelManager.DuelMapOption option = options.get(i);
            set(slots[i], ItemUtils.createItem(
                    materialFor(option.selection()),
                    "&a" + option.displayName(),
                    List.of(
                            "&7ᴘʀɪᴠᴀᴄʏ: &f" + privacyMode.displayName(),
                            "&7ᴛᴀʀɢᴇᴛ: &f" + target.getName(),
                            "&7" + option.description(),
                            "&eᴄʟɪᴄᴋ ᴛᴏ ѕᴇɴᴅ ᴄʜᴀʟʟᴇɴɢᴇ."
                    )
            ));
        }

        if (options.isEmpty()) {
            set(13, ItemUtils.createItem(Material.BARRIER, "&cɴᴏ ᴅᴜᴇʟ ᴍᴀᴘѕ ᴀᴠᴀɪʟᴀʙʟᴇ", List.of("&7ᴄᴏɴꜰɪɢᴜʀᴇ ᴀʀᴇɴᴀѕ ᴏʀ ᴇɴᴀʙʟᴇ ʀᴀɴᴅᴏᴍ ʙɪᴏᴍᴇѕ.")));
        }

        int lastRow = inventory.getSize() - 9;
        set(lastRow + 3, ItemUtils.createPlayerHead(target, "&eᴛᴀʀɢᴇᴛ: &f" + target.getName(), List.of("&7ᴄʜᴏᴏѕᴇ ᴀ ᴍᴀᴘ ᴛᴏ ѕᴇɴᴅ ᴀ ᴅᴜᴇʟ ʀᴇǫᴜᴇѕᴛ.")));
        set(lastRow + 5, ItemUtils.createItem(
                privacyMode == DuelPrivacyMode.FRIENDS_ONLY ? Material.OAK_SIGN : Material.PAPER,
                "&bᴘʀɪᴠᴀᴄʏ: &f" + privacyMode.displayName(),
                List.of(privacyMode == DuelPrivacyMode.FRIENDS_ONLY
                        ? "&7ᴏɴʟʏ ѕᴀᴍᴇ-ᴛᴇᴀᴍ ᴍᴇᴍʙᴇʀѕ ᴄᴀɴ ᴀᴄᴄᴇᴘᴛ ᴛʜɪѕ ᴅᴜᴇʟ."
                        : "&7ᴅɪʀᴇᴄᴛ ɪɴᴠɪᴛᴇ ᴅᴜᴇʟ.")
        ));
        set(lastRow + 8, ItemUtils.createItem(Material.BARRIER, "&cᴄʟᴏѕᴇ"));
    }

    @Override
    public void handleClick(int slot, Player player) {
        Player target = Bukkit.getPlayer(targetUuid);
        if (target == null) {
            player.closeInventory();
            return;
        }

        List<DuelManager.DuelMapOption> options = plugin.getDuelManager().getSelectableMapOptions(false);
        int index = optionIndex(slot);
        if (index >= 0 && index < options.size()) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("DUELS.CLICK"));
            plugin.getDuelManager().sendChallenge(player, target, options.get(index).selection(), privacyMode);
            player.closeInventory();
            return;
        }

        if (slot == inventory.getSize() - 1) {
            player.closeInventory();
        }
    }

    private int[] contentSlots() {
        int rows = inventory.getSize() / 9;
        List<Integer> slots = new ArrayList<>();
        for (int row = 0; row < rows - 1; row++) {
            for (int column = 0; column < 9; column++) {
                slots.add(row * 9 + column);
            }
        }
        return slots.stream().mapToInt(Integer::intValue).toArray();
    }

    private int optionIndex(int clickedSlot) {
        int[] slots = contentSlots();
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] == clickedSlot) {
                return i;
            }
        }
        return -1;
    }

    private Material materialFor(DuelMapSelection selection) {
        return switch (selection.type()) {
            case STATIC_ARENA -> Material.IRON_SWORD;
            case RANDOM_STATIC -> Material.COMPASS;
            case BIOME -> Material.GRASS_BLOCK;
            case RANDOM_BIOME -> Material.MAP;
        };
    }
}
