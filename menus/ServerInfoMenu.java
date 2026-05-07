package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ServerInfoMenu extends BaseMenu {

    private static final String MENU_PATH = "SERVER-INFO-MENU";
    private static final String BUTTONS_PATH = MENU_PATH + ".BUTTONS";
    private static final String PAGES_PATH = MENU_PATH + ".PAGES";
    private static final String NAVIGATION_PATH = MENU_PATH + ".NAVIGATION";
    private static final String GLOBAL_PAGE_PATH = "GLOBAL.PAGE-MENU";
    private static final String CLICK_SOUND_PATH = "MENUS.BUTTON-CLICK";
    private static final String PAGE_SOUND_PATH = "MENUS.PAGE-TURN";

    private final int page;
    private final List<PageDefinition> pages;
    private final Map<Integer, ButtonAction> slotActions = new HashMap<>();

    private int backSlot = -1;
    private int nextSlot = -1;

    public ServerInfoMenu(UltimateDonutSmp plugin) {
        this(plugin, 1);
    }

    public ServerInfoMenu(UltimateDonutSmp plugin, int page) {
        this(plugin, page, loadPages(plugin));
    }

    private ServerInfoMenu(UltimateDonutSmp plugin, int requestedPage, List<PageDefinition> loadedPages) {
        super(plugin, configuredTitle(plugin, requestedPage, loadedPages), configuredSize(plugin, requestedPage, loadedPages));
        this.pages = loadedPages;
        int maxPage = Math.max(1, loadedPages.size());
        this.page = Math.max(1, Math.min(requestedPage, maxPage));
    }

    public boolean hasValidButtons() {
        return !pages.isEmpty();
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.BLACK_STAINED_GLASS_PANE);
        slotActions.clear();
        backSlot = -1;
        nextSlot = -1;

        PageDefinition pageDefinition = getCurrentPage();
        if (pageDefinition == null) {
            setFallbackItem("&cNo server info pages", "&7Configure SERVER-INFO-MENU first.");
            return;
        }

        int renderedButtons = 0;
        for (ButtonDefinition button : pageDefinition.buttons()) {
            if (slotActions.containsKey(button.slot())) {
                plugin.getLogger().warning("Skipping duplicated Server Info slot " + button.slot()
                        + " on page " + page + ".");
                continue;
            }

            set(button.slot(), ItemUtils.createItem(button.material(), button.displayName(), button.lore()));
            slotActions.put(button.slot(), button.action());
            renderedButtons++;
        }

        if (pages.size() > 1) {
            renderNavigation();
        }

        if (renderedButtons == 0) {
            setFallbackItem("&cNo usable help buttons", "&7Fix this help page or use the legacy chat fallback.");
        }
    }

    @Override
    public void handleClick(int slot, Player player) {
        if (slot == backSlot && page > 1) {
            SoundUtils.play(player, plugin.getConfigManager().getSound(PAGE_SOUND_PATH));
            new ServerInfoMenu(plugin, page - 1).open(player);
            return;
        }

        if (slot == nextSlot && page < pages.size()) {
            SoundUtils.play(player, plugin.getConfigManager().getSound(PAGE_SOUND_PATH));
            new ServerInfoMenu(plugin, page + 1).open(player);
            return;
        }

        ButtonAction action = slotActions.get(slot);
        if (action == null) {
            return;
        }

        SoundUtils.play(player, plugin.getConfigManager().getSound(CLICK_SOUND_PATH));

        if (action.type() == ActionType.COMMAND) {
            if (action.command() == null || action.command().isBlank()) {
                player.sendMessage(ColorUtils.toComponent("&cThis menu button is missing a command."));
                return;
            }

            player.closeInventory();
            plugin.getPaperScheduler().runEntity(player, () -> {
                if (!player.isOnline()) {
                    return;
                }

                boolean executed = player.performCommand(action.command());
                if (!executed) {
                    player.sendMessage(ColorUtils.toComponent("&cThat action is unavailable right now."));
                }
            });
            return;
        }

        if (action.messages().isEmpty()) {
            player.sendMessage(ColorUtils.toComponent("&7This button is informational only."));
            return;
        }

        for (String line : action.messages()) {
            player.sendMessage(ColorUtils.toComponent(line));
        }
    }

    private void renderNavigation() {
        int reservedBack = plugin.getConfigManager().getMenus().getInt(NAVIGATION_PATH + ".BACK-SLOT", inventory.getSize() - 9);
        int reservedInfo = plugin.getConfigManager().getMenus().getInt(NAVIGATION_PATH + ".PAGE-INFO-SLOT", inventory.getSize() - 5);
        int reservedNext = plugin.getConfigManager().getMenus().getInt(NAVIGATION_PATH + ".NEXT-SLOT", inventory.getSize() - 1);

        Material navMaterial = ItemUtils.parseMaterial(
                plugin.getConfigManager().getMenus().getString(GLOBAL_PAGE_PATH + ".MATERIAL", "ARROW")
        );

        if (page > 1 && isInBounds(reservedBack)) {
            backSlot = reservedBack;
            set(backSlot, ItemUtils.createItem(
                    navMaterial,
                    plugin.getConfigManager().getMenus().getString(GLOBAL_PAGE_PATH + ".BACK-BUTTON", "&aBack"),
                    plugin.getConfigManager().getMenus().getStringList(GLOBAL_PAGE_PATH + ".BACK-LORE")
            ));
        }

        if (isInBounds(reservedInfo)) {
            set(reservedInfo, ItemUtils.createItem(
                    ItemUtils.parseMaterial(plugin.getConfigManager().getMenus()
                            .getString(NAVIGATION_PATH + ".PAGE-INFO-MATERIAL", "BOOK")),
                    plugin.getConfigManager().getMenus().getString(NAVIGATION_PATH + ".PAGE-INFO-NAME", "&bHelp Pages"),
                    List.of("&7Page: &f" + page + "&7/&f" + pages.size(), "&7Click the arrows to keep reading.")
            ));
        }

        if (page < pages.size() && isInBounds(reservedNext)) {
            nextSlot = reservedNext;
            set(nextSlot, ItemUtils.createItem(
                    navMaterial,
                    plugin.getConfigManager().getMenus().getString(GLOBAL_PAGE_PATH + ".NEXT-BUTTON", "&aNext"),
                    plugin.getConfigManager().getMenus().getStringList(GLOBAL_PAGE_PATH + ".NEXT-LORE")
            ));
        }
    }

    private PageDefinition getCurrentPage() {
        if (pages.isEmpty()) {
            return null;
        }

        int index = Math.max(0, Math.min(page - 1, pages.size() - 1));
        return pages.get(index);
    }

    private boolean isInBounds(int slot) {
        return slot >= 0 && slot < inventory.getSize();
    }

    private void setFallbackItem(String title, String lore) {
        set(inventory.getSize() / 2, ItemUtils.createItem(Material.BARRIER, title, List.of(lore)));
    }

    private static List<PageDefinition> loadPages(UltimateDonutSmp plugin) {
        List<PageDefinition> loadedPages = new ArrayList<>();
        ConfigurationSection pagesSection = plugin.getConfigManager().getMenus().getConfigurationSection(PAGES_PATH);

        if (pagesSection != null && !pagesSection.getKeys(false).isEmpty()) {
            List<String> pageKeys = new ArrayList<>(pagesSection.getKeys(false));
            pageKeys.sort(Comparator.comparingInt(ServerInfoMenu::parsePageOrder));

            for (String pageKey : pageKeys) {
                ConfigurationSection pageSection = pagesSection.getConfigurationSection(pageKey);
                if (pageSection == null) {
                    continue;
                }

                List<ButtonDefinition> buttons = loadButtons(plugin, pageSection.getConfigurationSection("BUTTONS"), pageKey);
                if (!buttons.isEmpty()) {
            loadedPages.add(new PageDefinition(
                    pageKey,
                    pageSection.getString("TITLE", plugin.getConfigManager().getMenus().getString(MENU_PATH + ".TITLE", "&8Server Info")),
                    normalizeSize(pageSection.getInt("SIZE", plugin.getConfigManager().getMenus().getInt(MENU_PATH + ".SIZE", 27))),
                    buttons
            ));
                }
            }
        }

        if (!loadedPages.isEmpty()) {
            return loadedPages;
        }

        List<ButtonDefinition> legacyButtons = loadButtons(
                plugin,
                plugin.getConfigManager().getMenus().getConfigurationSection(BUTTONS_PATH),
                "legacy"
        );
        if (!legacyButtons.isEmpty()) {
            loadedPages.add(new PageDefinition(
                    "1",
                    plugin.getConfigManager().getMenus().getString(MENU_PATH + ".TITLE", "&8Server Info"),
                    normalizeSize(plugin.getConfigManager().getMenus().getInt(MENU_PATH + ".SIZE", 27)),
                    legacyButtons
            ));
            loadedPages.add(createDefaultGettingStartedPage());
            loadedPages.add(createDefaultCommandsPage());
        }
        return loadedPages;
    }

    private static List<ButtonDefinition> loadButtons(UltimateDonutSmp plugin, ConfigurationSection buttonsSection, String pageKey) {
        List<ButtonDefinition> buttons = new ArrayList<>();
        if (buttonsSection == null) {
            return buttons;
        }

        int inventorySize = normalizeSize(plugin.getConfigManager().getMenus().getInt(MENU_PATH + ".SIZE", 27));
        int reservedBack = plugin.getConfigManager().getMenus().getInt(NAVIGATION_PATH + ".BACK-SLOT", inventorySize - 9);
        int reservedInfo = plugin.getConfigManager().getMenus().getInt(NAVIGATION_PATH + ".PAGE-INFO-SLOT", inventorySize - 5);
        int reservedNext = plugin.getConfigManager().getMenus().getInt(NAVIGATION_PATH + ".NEXT-SLOT", inventorySize - 1);

        for (String key : buttonsSection.getKeys(false)) {
            ConfigurationSection buttonSection = buttonsSection.getConfigurationSection(key);
            if (buttonSection == null) {
                plugin.getLogger().warning("Skipping " + buttonsSection.getCurrentPath() + "." + key
                        + " because it is not a section.");
                continue;
            }

            int slot = buttonSection.getInt("SLOT", -1);
            if (slot < 0 || slot >= inventorySize) {
                plugin.getLogger().warning("Skipping " + buttonSection.getCurrentPath()
                        + " because slot " + slot + " is outside menu size " + inventorySize + ".");
                continue;
            }

            if (slot == reservedBack || slot == reservedInfo || slot == reservedNext) {
                plugin.getLogger().warning("Skipping " + buttonSection.getCurrentPath()
                        + " because slot " + slot + " is reserved for page navigation.");
                continue;
            }

            String rawMaterial = buttonSection.getString("MATERIAL");
            if (rawMaterial == null || rawMaterial.isBlank()) {
                plugin.getLogger().warning("Skipping " + buttonSection.getCurrentPath() + " because MATERIAL is missing.");
                continue;
            }

            Material material = Material.matchMaterial(rawMaterial.trim().toUpperCase(Locale.ROOT));
            if (material == null) {
                plugin.getLogger().warning("Skipping " + buttonSection.getCurrentPath()
                        + " because MATERIAL '" + rawMaterial + "' is invalid.");
                continue;
            }

            buttons.add(new ButtonDefinition(
                    slot,
                    material,
                    buttonSection.getString("NAME", prettifyKey(key)),
                    buttonSection.getStringList("LORE"),
                    resolveAction(key, buttonSection)
            ));
        }
        return buttons;
    }

    private static ButtonAction resolveAction(String key, ConfigurationSection buttonSection) {
        String configuredCommand = firstNonBlank(
                sanitizeCommand(buttonSection.getString("COMMAND")),
                sanitizeCommand(buttonSection.getString("ACTION.VALUE"))
        );
        if (configuredCommand != null) {
            return ButtonAction.command(configuredCommand);
        }

        List<String> configuredMessages = buttonSection.getStringList("CLICK-MESSAGE");
        if (!configuredMessages.isEmpty()) {
            return ButtonAction.info(configuredMessages);
        }

        return switch (key.toUpperCase(Locale.ROOT)) {
            case "RULES" -> ButtonAction.command("rules");
            case "LEADERBOARDS" -> ButtonAction.command("leaderboards");
            case "SHARDS" -> ButtonAction.command("afk");
            case "SETTINGS" -> ButtonAction.command("settings");
            case "RTP" -> ButtonAction.command("rtp");
            case "SPAWN" -> ButtonAction.command("spawn");
            case "HOME", "HOMES" -> ButtonAction.command("home");
            case "TEAM", "TEAMS" -> ButtonAction.command("team");
            case "SHOP" -> ButtonAction.command("shop");
            case "SELL" -> ButtonAction.command("sell");
            case "TPA" -> ButtonAction.command("tpa");
            case "STATS" -> ButtonAction.command("stats");
            case "SOCIAL", "DISCORD" -> ButtonAction.command("social");
            case "MEDIA" -> ButtonAction.command("media");
            case "SERVER" -> ButtonAction.info(List.of(
                    "&7Build your base, gear up, and use &f/spawn &7or &f/rtp &7to begin exploring."
            ));
            case "ECONOMY" -> ButtonAction.info(List.of(
                    "&7Earn money with &f/sell &7and trade better gear through &f/auctionhouse&7."
            ));
            default -> ButtonAction.info(List.of("&7This button is informational only."));
        };
    }

    private static String configuredTitle(UltimateDonutSmp plugin, int requestedPage, List<PageDefinition> loadedPages) {
        if (!loadedPages.isEmpty()) {
            int safePage = Math.max(1, Math.min(requestedPage, loadedPages.size()));
            return loadedPages.get(safePage - 1).title();
        }

        return plugin.getConfigManager().getMenus().getString(MENU_PATH + ".TITLE", "&8Server Info");
    }

    private static int configuredSize(UltimateDonutSmp plugin, int requestedPage, List<PageDefinition> loadedPages) {
        if (!loadedPages.isEmpty()) {
            int safePage = Math.max(1, Math.min(requestedPage, loadedPages.size()));
            return loadedPages.get(safePage - 1).size();
        }

        return normalizeSize(plugin.getConfigManager().getMenus().getInt(MENU_PATH + ".SIZE", 27));
    }

    private static int normalizeSize(int rawSize) {
        if (rawSize >= 9 && rawSize <= 54 && rawSize % 9 == 0) {
            return rawSize;
        }
        return 27;
    }

    private static String sanitizeCommand(String rawCommand) {
        if (rawCommand == null || rawCommand.isBlank()) {
            return null;
        }

        String sanitized = rawCommand.trim();
        if (sanitized.startsWith("/")) {
            sanitized = sanitized.substring(1);
        }
        return sanitized.isBlank() ? null : sanitized;
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private static int parsePageOrder(String key) {
        try {
            return Integer.parseInt(key);
        } catch (NumberFormatException ignored) {
            return Integer.MAX_VALUE;
        }
    }

    private static String prettifyKey(String key) {
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
        return builder.isEmpty() ? "Info" : builder.toString();
    }

    private enum ActionType {
        COMMAND,
        INFO
    }

    private static PageDefinition createDefaultGettingStartedPage() {
        return new PageDefinition(
                "2",
                "&8Getting Started",
                27,
                List.of(
                        new ButtonDefinition(
                                10,
                                Material.BOOK,
                                "&#00A4FCStart Here",
                                List.of(
                                        "&f1. Use &b/rtp &fto leave spawn.",
                                        "&f2. Gather wood, food, and iron.",
                                        "&f3. Build a safe base.",
                                        "&f4. Save it with &b/sethome&f."
                                ),
                                ButtonAction.info(List.of(
                                        "&7Start with &f/rtp &7to find land away from spawn.",
                                        "&7After building a base, save it with &f/sethome&7."
                                ))
                        ),
                        new ButtonDefinition(
                                11,
                                Material.GOLD_INGOT,
                                "&#00A4FCMake Money",
                                List.of(
                                        "&fSell blocks, ores, and drops with",
                                        "&b/sell &for list items in &b/auctionhouse&f.",
                                        "",
                                        "&#00A4FCTip: &fKeep rare loot to trade."
                                ),
                                ButtonAction.command("sell")
                        ),
                        new ButtonDefinition(
                                12,
                                Material.RED_BED,
                                "&#00A4FCHomes",
                                List.of(
                                        "&fUse &b/sethome <name> &fto save",
                                        "&fimportant places.",
                                        "",
                                        "&#00A4FCOpen: &f/home"
                                ),
                                ButtonAction.command("home")
                        ),
                        new ButtonDefinition(
                                13,
                                Material.IRON_HELMET,
                                "&#00A4FCTeams",
                                List.of(
                                        "&fPlay with friends, share a base,",
                                        "&fand manage team permissions.",
                                        "",
                                        "&#00A4FCOpen: &f/team"
                                ),
                                ButtonAction.command("team")
                        ),
                        new ButtonDefinition(
                                14,
                                Material.DIAMOND_SWORD,
                                "&#00A4FCCombat Tips",
                                List.of(
                                        "&fDying is punishing because",
                                        "&fkeepInventory is turned off.",
                                        "",
                                        "&#00A4FCTip: &fAvoid risky fights",
                                        "&funtil you are geared."
                                ),
                                ButtonAction.info(List.of(
                                        "&7You drop your items on death here, so gear up before taking big fights.",
                                        "&7Keep backup armor and food if you plan to PvP often."
                                ))
                        ),
                        new ButtonDefinition(
                                15,
                                Material.COMPASS,
                                "&#00A4FCSpawn",
                                List.of(
                                        "&fReturn to spawn when you need",
                                        "&fshops, safety, or a reset.",
                                        "",
                                        "&#00A4FCCommand: &f/spawn"
                                ),
                                ButtonAction.command("spawn")
                        ),
                        new ButtonDefinition(
                                16,
                                Material.OAK_SAPLING,
                                "&#00A4FCRTP Guide",
                                List.of(
                                        "&fUse RTP when spawn feels crowded",
                                        "&for you need fresh land.",
                                        "",
                                        "&#00A4FCCommand: &f/rtp"
                                ),
                                ButtonAction.command("rtp")
                        )
                )
        );
    }

    private static PageDefinition createDefaultCommandsPage() {
        return new PageDefinition(
                "3",
                "&8Useful Commands",
                27,
                List.of(
                        new ButtonDefinition(
                                10,
                                Material.EMERALD,
                                "&#00A4FCShop",
                                List.of(
                                        "&fBuy useful items, blocks,",
                                        "&fand starter gear.",
                                        "",
                                        "&#00A4FCCommand: &f/shop"
                                ),
                                ButtonAction.command("shop")
                        ),
                        new ButtonDefinition(
                                11,
                                Material.CHEST,
                                "&#00A4FCSell",
                                List.of(
                                        "&fTurn farmed or mined items",
                                        "&finto quick money.",
                                        "",
                                        "&#00A4FCCommand: &f/sell"
                                ),
                                ButtonAction.command("sell")
                        ),
                        new ButtonDefinition(
                                12,
                                Material.ENDER_PEARL,
                                "&#00A4FCTeleport Requests",
                                List.of(
                                        "&fUse &b/tpa <player> &for",
                                        "&b/tpahere <player>&f.",
                                        "",
                                        "&#00A4FCTip: &fOnly teleport to",
                                        "&fpeople you trust."
                                ),
                                ButtonAction.info(List.of(
                                        "&7Use &f/tpa <player> &7to request teleporting to them.",
                                        "&7Use &f/tpahere <player> &7if you want them to come to you."
                                ))
                        ),
                        new ButtonDefinition(
                                13,
                                Material.CLOCK,
                                "&#00A4FCLeaderboards",
                                List.of(
                                        "&fCheck who is leading in",
                                        "&fmoney, kills, and more.",
                                        "",
                                        "&#00A4FCCommand: &f/leaderboards"
                                ),
                                ButtonAction.command("leaderboards")
                        ),
                        new ButtonDefinition(
                                14,
                                Material.GRAY_DYE,
                                "&#00A4FCSettings",
                                List.of(
                                        "&fToggle personal options like",
                                        "&falerts and menu preferences.",
                                        "",
                                        "&#00A4FCCommand: &f/settings"
                                ),
                                ButtonAction.command("settings")
                        ),
                        new ButtonDefinition(
                                15,
                                Material.KNOWLEDGE_BOOK,
                                "&#00A4FCRules",
                                List.of(
                                        "&fRead the rules before grinding",
                                        "&for trading with players.",
                                        "",
                                        "&#00A4FCCommand: &f/rules"
                                ),
                                ButtonAction.command("rules")
                        ),
                        new ButtonDefinition(
                                16,
                                Material.PINK_DYE,
                                "&#00A4FCSocial & Media",
                                List.of(
                                        "&fOpen server links and check",
                                        "&fmedia rank requirements.",
                                        "",
                                        "&#00A4FCCommand: &f/media"
                                ),
                                ButtonAction.command("media")
                        )
                )
        );
    }

    private record PageDefinition(String key, String title, int size, List<ButtonDefinition> buttons) {}

    private record ButtonDefinition(
            int slot,
            Material material,
            String displayName,
            List<String> lore,
            ButtonAction action
    ) {}

    private record ButtonAction(ActionType type, String command, List<String> messages) {

        private static ButtonAction command(String command) {
            return new ButtonAction(ActionType.COMMAND, command, List.of());
        }

        private static ButtonAction info(List<String> messages) {
            return new ButtonAction(ActionType.INFO, null, messages);
        }
    }
}
