package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.Home;
import com.bx.ultimateDonutSmp.models.Team;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.cumulus.form.Form;
import org.geysermc.cumulus.form.ModalForm;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.List;

public final class HomeBedrockManager {

    private final UltimateDonutSmp plugin;

    public HomeBedrockManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public boolean isBedrockPlayer(Player player) {
        if (player == null || !enabled()) {
            return false;
        }
        try {
            return FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId());
        } catch (Throwable throwable) {
            return false;
        }
    }

    public boolean openMain(Player player) {
        if (!isBedrockPlayer(player)) {
            return false;
        }

        List<Home> homes = plugin.getHomeManager().getHomes(player.getUniqueId());
        int maxHomes = plugin.getHomeManager().getMaxHomes(player);
        Team team = plugin.getTeamManager().getTeam(player);

        SimpleForm.Builder form = SimpleForm.builder()
                .title(plain(plugin.getConfigManager().getMenus().getString("HOME-MENU.TITLE", "Homes")))
                .content(plain("Homes (" + homes.size() + "/" + maxHomes + "):"));

        // Button 0: Create new home
        if (homes.size() < maxHomes) {
            form.button("Set New Home");
        } else {
            form.button("Home Limit Reached");
        }

        // Button 1: Team Home option
        if (team != null) {
            if (team.hasHome()) {
                form.button("Team Home (Manage)");
            } else {
                form.button("Team Home (Not Set)");
            }
        }

        // Buttons for each home
        for (Home home : homes) {
            String worldName = home.getLocation() != null && home.getLocation().getWorld() != null
                    ? home.getLocation().getWorld().getName()
                    : "overworld";
            form.button(home.getName() + "\n(" + worldName + ")");
        }

        form.validResultHandler(response -> schedule(player, () -> {
            int buttonId = response.clickedButtonId();
            int currentOffset = 0;

            // New Home button
            if (buttonId == currentOffset) {
                if (homes.size() < maxHomes) {
                    openCreateForm(player);
                } else {
                    player.sendMessage(ColorUtils.toComponent("&cYou've reached your home limit."));
                    openMain(player);
                }
                return;
            }
            currentOffset++;

            // Team Home button
            if (team != null) {
                if (buttonId == currentOffset) {
                    openTeamHomeOptions(player);
                    return;
                }
                currentOffset++;
            }

            // Home buttons
            int homeIndex = buttonId - currentOffset;
            if (homeIndex >= 0 && homeIndex < homes.size()) {
                openHomeOptions(player, homes.get(homeIndex));
            }
        }));

        return send(player, form.build());
    }

    public void openHomeOptions(Player player, Home home) {
        if (home == null) {
            openMain(player);
            return;
        }

        SimpleForm.Builder form = SimpleForm.builder()
                .title(plain("Home: " + home.getName()))
                .content(plain("Manage home '" + home.getName() + "':"))
                .button("Teleport")
                .button("Rename Home")
                .button("Delete Home")
                .button("Back");

        form.validResultHandler(response -> schedule(player, () -> {
            int button = response.clickedButtonId();
            if (button == 0) {
                plugin.getTeleportManager().queue(player, home.getLocation(), "HOME", null);
            } else if (button == 1) {
                openRenameForm(player, home);
            } else if (button == 2) {
                openDeleteConfirmation(player, home);
            } else {
                openMain(player);
            }
        }));

        send(player, form.build());
    }

    public void openDeleteConfirmation(Player player, Home home) {
        if (home == null) {
            openMain(player);
            return;
        }

        ModalForm.Builder form = ModalForm.builder()
                .title(plain("Delete Home: " + home.getName()))
                .content(plain("Are you sure you want to delete home '" + home.getName() + "'?\nThis action cannot be undone."))
                .button1("Confirm Delete")
                .button2("Cancel");

        form.validResultHandler(response -> schedule(player, () -> {
            if (response.clickedFirst()) {
                boolean deleted = plugin.getHomeManager().deleteHome(player.getUniqueId(), home.getName());
                if (deleted) {
                    player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage("HOME.DELETED")));
                } else {
                    player.sendMessage(ColorUtils.toComponent("&cHome not found."));
                }
                openMain(player);
            } else {
                openHomeOptions(player, home);
            }
        }));

        send(player, form.build());
    }

    public void openDeleteSelect(Player player) {
        if (!isBedrockPlayer(player)) return;

        List<Home> homes = plugin.getHomeManager().getHomes(player.getUniqueId());
        if (homes.isEmpty()) {
            player.sendMessage(ColorUtils.toComponent("&cYou do not have any homes to delete."));
            return;
        }

        SimpleForm.Builder form = SimpleForm.builder()
                .title(plain("Select Home to Delete"))
                .content(plain("Choose a home you want to delete:"))
                .button("Back");

        for (Home home : homes) {
            form.button("Delete " + home.getName());
        }

        form.validResultHandler(response -> schedule(player, () -> {
            int button = response.clickedButtonId();
            if (button == 0) {
                openMain(player);
            } else {
                int index = button - 1;
                if (index >= 0 && index < homes.size()) {
                    openDeleteConfirmation(player, homes.get(index));
                }
            }
        }));

        send(player, form.build());
    }

    public void openRenameForm(Player player, Home home) {
        if (home == null) {
            openMain(player);
            return;
        }

        CustomForm.Builder form = CustomForm.builder()
                .title(plain("Rename Home: " + home.getName()))
                .input("New Home Name", home.getName(), home.getName());

        form.validResultHandler(response -> schedule(player, () -> {
            String newName = response.asInput(0);
            if (newName == null || newName.isBlank() || newName.contains(" ")) {
                player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage("HOME.INVALID-NAME")));
                openRenameForm(player, home);
                return;
            }

            boolean ok = plugin.getHomeManager().renameHome(player.getUniqueId(), home.getName(), newName.trim());
            if (ok) {
                player.sendMessage(ColorUtils.toComponent(
                        plugin.getConfigManager().getMessage("HOME.RENAME-SUCCESS", "{name}", newName.trim())));
                openMain(player);
            } else {
                player.sendMessage(ColorUtils.toComponent("&cFailed to rename home. Name may already exist."));
                openHomeOptions(player, home);
            }
        }));

        form.closedResultHandler(() -> schedule(player, () -> openHomeOptions(player, home)));

        send(player, form.build());
    }

    public void openCreateForm(Player player) {
        int homeCount = plugin.getHomeManager().getHomeCount(player.getUniqueId());
        String defaultName = homeCount == 0 ? "home" : "home" + (homeCount + 1);

        CustomForm.Builder form = CustomForm.builder()
                .title(plain("Set New Home"))
                .input("Home Name", defaultName, defaultName);

        form.validResultHandler(response -> schedule(player, () -> {
            String name = response.asInput(0);
            if (name == null || name.isBlank() || name.contains(" ")) {
                player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage("HOME.INVALID-NAME")));
                openCreateForm(player);
                return;
            }

            boolean success = plugin.getHomeManager().setHome(player, name.trim());
            if (success) {
                player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage("HOME.SET")));
            } else {
                player.sendMessage(ColorUtils.toComponent("&cYou've reached your home limit or the home already exists."));
            }
            openMain(player);
        }));

        form.closedResultHandler(() -> schedule(player, () -> openMain(player)));

        send(player, form.build());
    }

    public void openTeamHomeOptions(Player player) {
        Team team = plugin.getTeamManager().getTeam(player);
        if (team == null) {
            openMain(player);
            return;
        }

        boolean canEdit = plugin.getTeamManager().canEditHome(team, player.getUniqueId());
        boolean canVisit = plugin.getTeamManager().canVisitHome(team, player.getUniqueId());

        SimpleForm.Builder form = SimpleForm.builder()
                .title(plain("Team Home"))
                .content(plain("Manage team home:"));

        if (team.hasHome() && canVisit) {
            form.button("Teleport to Team Home");
        }
        if (canEdit) {
            form.button("Set Team Home Here");
            if (team.hasHome()) {
                form.button("Delete Team Home");
            }
        }
        form.button("Back");

        form.validResultHandler(response -> schedule(player, () -> {
            String clicked = response.clickedButton().text();
            if (clicked.contains("Teleport")) {
                plugin.getTeleportManager().queue(player, team.getHome(), "TEAM-HOME", null);
            } else if (clicked.contains("Set Team Home")) {
                team.setHome(player.getLocation());
                plugin.getTeamManager().save(team);
                player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage("TEAM.TEAM-HOME-SET")));
                openMain(player);
            } else if (clicked.contains("Delete Team Home")) {
                openDeleteTeamHomeConfirmation(player, team);
            } else {
                openMain(player);
            }
        }));

        send(player, form.build());
    }

    private void openDeleteTeamHomeConfirmation(Player player, Team team) {
        ModalForm.Builder form = ModalForm.builder()
                .title(plain("Delete Team Home"))
                .content(plain("Are you sure you want to delete your team home?"))
                .button1("Confirm Delete")
                .button2("Cancel");

        form.validResultHandler(response -> schedule(player, () -> {
            if (response.clickedFirst()) {
                team.setHome(null);
                plugin.getTeamManager().save(team);
                player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage("TEAM.TEAM-HOME-DELETED")));
                openMain(player);
            } else {
                openTeamHomeOptions(player);
            }
        }));

        send(player, form.build());
    }

    private boolean send(Player player, Form form) {
        try {
            return FloodgateApi.getInstance().sendForm(player.getUniqueId(), form);
        } catch (Throwable throwable) {
            plugin.getLogger().warning("Could not send Home Bedrock form to " + player.getName()
                    + ": " + throwable.getMessage());
            return false;
        }
    }

    private void schedule(Player player, Runnable runnable) {
        plugin.getSpigotScheduler().runEntity(player, runnable);
    }

    private boolean enabled() {
        return plugin.getServer().getPluginManager().isPluginEnabled("floodgate");
    }

    private String plain(String value) {
        return value == null ? "" : value.replaceAll("(?i)[&§][0-9A-FK-ORX]", "");
    }
}
