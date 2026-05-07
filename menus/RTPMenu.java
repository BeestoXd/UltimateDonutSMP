package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.RTPManager;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class RTPMenu extends BaseMenu {

    public RTPMenu(UltimateDonutSmp plugin) {
        super(plugin,
                plugin.getConfigManager().getRtp().getString("RTP-MENU.TITLE", "&8RTP Menu"),
                normalizeSize(plugin.getConfigManager().getRtp().getInt("RTP-MENU.SIZE", 27)));
    }

    @Override
    public void build(Player player) {
        clear();
        if (plugin.getConfigManager().getRtp().getBoolean("RTP-MENU.PLACEHOLDER", true)) {
            fill(Material.GRAY_STAINED_GLASS_PANE);
        }

        List<RTPManager.RTPDestination> destinations = plugin.getRtpManager().getMenuDestinations();
        if (destinations.isEmpty()) {
            set(inventory.getSize() / 2, ItemUtils.createItem(
                    Material.BARRIER,
                    "&cNo RTP destinations",
                    List.of("&7Belum ada destination RTP yang valid di config.")
            ));
            return;
        }

        for (RTPManager.RTPDestination destination : destinations) {
            ItemStack item = ItemUtils.createItem(
                    destination.material(),
                    replacePlaceholders(player, destination, destination.displayName()),
                    replacePlaceholders(player, destination, destination.lore())
            );
            set(destination.slot(), item);
        }
    }

    @Override
    public void handleClick(int slot, Player player) {
        RTPManager.RTPDestination destination = plugin.getRtpManager().getDestinationBySlot(slot);
        if (destination == null) {
            return;
        }

        SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
        plugin.getRtpManager().queueMenuTeleport(player, destination);
    }

    private List<String> replacePlaceholders(Player player, RTPManager.RTPDestination destination, List<String> lines) {
        List<String> replaced = new ArrayList<>();
        for (String line : lines) {
            replaced.add(replacePlaceholders(player, destination, line));
        }
        return replaced;
    }

    private String replacePlaceholders(Player player, RTPManager.RTPDestination destination, String text) {
        RTPManager.SearchSettings settings = plugin.getRtpManager().getWorldSearchSettings(destination.worldName());
        String minRadius = settings == null ? "0" : String.valueOf(settings.minRadius());
        String maxRadius = settings == null ? "0" : String.valueOf(settings.maxRadius());

        return text
                .replace("{players}", String.valueOf(plugin.getRtpManager().getPlayersInWorld(destination.worldName())))
                .replace("{ping}", String.valueOf(player.getPing()))
                .replace("{world}", plugin.getRtpManager().describeWorld(destination.worldName()))
                .replace("{min_radius}", minRadius)
                .replace("{max_radius}", maxRadius)
                .replace("{cooldown}", String.valueOf(plugin.getRtpManager().getWorldCooldownSeconds(destination.worldName())))
                .replace("{status}", destination.enabled() ? "&aEnabled" : "&cDisabled");
    }

    private static int normalizeSize(int size) {
        int normalized = Math.max(9, ((size + 8) / 9) * 9);
        return Math.min(54, normalized);
    }
}
