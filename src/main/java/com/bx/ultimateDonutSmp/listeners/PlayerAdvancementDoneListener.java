package com.bx.ultimateDonutSmp.listeners;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.models.ThreeChoice;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

public class PlayerAdvancementDoneListener implements Listener {

    private final UltimateDonutSmp plugin;

    public PlayerAdvancementDoneListener(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAdvancementDone(PlayerAdvancementDoneEvent event) {
        // Handled in listener
    }

    private boolean shouldReceiveAdvancement(Player receiver, Player victim) {
        PlayerData receiverData = plugin.getPlayerDataManager().get(receiver);
        if (receiverData == null) {
            return true;
        }
        ThreeChoice choice = receiverData.getAdvancementMessagesChoice();
        if (choice == ThreeChoice.OFF) {
            return false;
        }
        if (choice == ThreeChoice.FRIENDS_FOLLOWED) {
            return plugin.getFriendsManager() != null && plugin.getFriendsManager().isFollowing(receiver.getUniqueId(), victim.getUniqueId());
        }
        return true;
    }
}
