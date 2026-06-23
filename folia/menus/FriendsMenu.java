package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.FollowEntry;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.SignInputUtil;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class FriendsMenu extends BaseMenu {

    public enum FilterType {
        ALL("All"),
        FRIENDS("Friends"),
        FOLLOWING("Following"),
        FOLLOWERS("Followers");

        private final String displayName;

        FilterType(String displayName) {
            this.displayName = displayName;
        }

        public FilterType next() {
            int index = this.ordinal();
            FilterType[] vals = values();
            return vals[(index + 1) % vals.length];
        }
    }

    private record PlayerEntry(UUID uuid, String name, boolean following, boolean follower) {
        public boolean isFriend() {
            return following && follower;
        }
    }

    private final int page;
    private final String searchQuery;
    private final FilterType filterType;
    private final List<PlayerEntry> renderedEntries = new ArrayList<>();

    public FriendsMenu(UltimateDonutSmp plugin) {
        this(plugin, 0, null, FilterType.ALL);
    }

    public FriendsMenu(UltimateDonutSmp plugin, int page, String searchQuery, FilterType filterType) {
        super(plugin, "&8ꜰʀɪᴇɴᴅѕ", 54);
        this.page = page;
        this.searchQuery = searchQuery;
        this.filterType = filterType;
    }

    @Override
    public void build(Player player) {
        clear();
        renderedEntries.clear();

        // 1. Gather all follow/follower relations
        UUID playerUuid = player.getUniqueId();
        Collection<FollowEntry> following = plugin.getFriendsManager().getFollowing(playerUuid);
        Collection<FollowEntry> followers = plugin.getFriendsManager().getFollowers(playerUuid);

        Map<UUID, PlayerEntry> entryMap = new HashMap<>();

        for (FollowEntry entry : following) {
            entryMap.put(entry.followedUuid(), new PlayerEntry(
                    entry.followedUuid(),
                    entry.followedNameSnapshot(),
                    true,
                    false
            ));
        }

        for (FollowEntry entry : followers) {
            UUID fid = entry.followerUuid();
            PlayerEntry existing = entryMap.get(fid);
            if (existing != null) {
                entryMap.put(fid, new PlayerEntry(fid, existing.name(), true, true));
            } else {
                String name = plugin.getDatabaseManager().getLastKnownUsername(fid);
                if (name == null || name.isBlank()) {
                    name = fid.toString();
                }
                entryMap.put(fid, new PlayerEntry(fid, name, false, true));
            }
        }

        // 2. Filter entries
        List<PlayerEntry> list = new ArrayList<>();
        for (PlayerEntry pe : entryMap.values()) {
            // Apply filter type
            switch (filterType) {
                case FRIENDS -> {
                    if (!pe.isFriend()) continue;
                }
                case FOLLOWING -> {
                    if (!pe.following()) continue;
                }
                case FOLLOWERS -> {
                    if (!pe.follower()) continue;
                }
                default -> {}
            }

            // Apply search query
            if (searchQuery != null && !searchQuery.isBlank()) {
                if (!pe.name().toLowerCase(Locale.ROOT).contains(searchQuery.toLowerCase(Locale.ROOT))) {
                    continue;
                }
            }

            list.add(pe);
        }

        // 3. Sort entries (Online first, then alphabetically)
        list.sort((o1, o2) -> {
            boolean online1 = Bukkit.getPlayer(o1.uuid()) != null && Bukkit.getPlayer(o1.uuid()).isOnline();
            boolean online2 = Bukkit.getPlayer(o2.uuid()) != null && Bukkit.getPlayer(o2.uuid()).isOnline();
            if (online1 && !online2) return -1;
            if (!online1 && online2) return 1;
            return o1.name().compareToIgnoreCase(o2.name());
        });

        // 4. Populate page slots (0 to 44)
        int totalItems = list.size();
        int totalPages = (totalItems - 1) / 45 + 1;
        int currentPage = Math.max(0, Math.min(page, totalPages - 1));

        int startIndex = currentPage * 45;
        int endIndex = Math.min(totalItems, startIndex + 45);

        for (int i = startIndex; i < endIndex; i++) {
            PlayerEntry pe = list.get(i);
            renderedEntries.add(pe);

            boolean isOnline = Bukkit.getPlayer(pe.uuid()) != null && Bukkit.getPlayer(pe.uuid()).isOnline();
            String status = isOnline ? "&aᴏɴʟɪɴᴇ" : "&cᴏꜰꜰʟɪɴᴇ";

            String rel;
            if (pe.isFriend()) rel = "&dꜰʀɪᴇɴᴅ";
            else if (pe.following()) rel = "&9ꜰᴏʟʟᴏᴡɪɴɢ";
            else rel = "&bꜰᴏʟʟᴏᴡᴇʀ";

            List<String> lore = new ArrayList<>();
            lore.add("&7ѕᴛᴀᴛᴜѕ: " + status);
            lore.add("&7ʀᴇʟᴀᴛɪᴏɴѕʜɪᴘ: " + rel);
            lore.add("");
            lore.add("&eᴄʟɪᴄᴋ ᴛᴏ ᴇᴅɪᴛ");

            ItemStack head = ItemUtils.createPlayerHead(
                    Bukkit.getOfflinePlayer(pe.uuid()),
                    "&e&l" + pe.name(),
                    lore
            );
            set(i - startIndex, head);
        }

        // 5. Build bottom row buttons (45 to 53)
        for (int s = 45; s <= 53; s++) {
            set(s, new ItemStack(Material.BLACK_STAINED_GLASS_PANE));
        }

        // Previous Page (45)
        if (currentPage > 0) {
            set(45, ItemUtils.createItem(
                    Material.ARROW,
                    "&aᴘʀᴇᴠɪᴏᴜѕ ᴘᴀɢᴇ",
                    List.of("&7ɢᴏ ᴛᴏ ᴘᴀɢᴇ " + currentPage)
            ));
        }

        // Search Button (48)
        List<String> searchLore = new ArrayList<>();
        searchLore.add("&7ᴄᴜʀʀᴇɴᴛʟʏ: &f" + (searchQuery == null ? "None" : searchQuery));
        searchLore.add("");
        searchLore.add("&eʟᴇꜰᴛ-ᴄʟɪᴄᴋ &7ᴛᴏ ѕᴇᴀʀᴄʜ");
        searchLore.add("&eʀɪɢʜᴛ-ᴄʟɪᴄᴋ &7ᴛᴏ ᴄʟᴇᴀʀ ꜰɪʟᴛᴇʀ");
        set(48, ItemUtils.createItem(Material.COMPASS, "&eѕᴇᴀʀᴄʜ", searchLore));

        // Filter Button (49)
        List<String> filterLore = new ArrayList<>();
        for (FilterType ft : FilterType.values()) {
            if (ft == filterType) {
                filterLore.add("&e- " + ft.displayName);
            } else {
                filterLore.add("&7- " + ft.displayName);
            }
        }
        filterLore.add("");
        filterLore.add("&eᴄʟɪᴄᴋ ᴛᴏ ᴄʜᴀɴɢᴇ");
        set(49, ItemUtils.createItem(Material.HOPPER, "&eꜰɪʟᴛᴇʀ", filterLore));

        // Refresh Button (50)
        set(50, ItemUtils.createItem(
                Material.CLOCK,
                "&aꜰʀɪᴇɴᴅѕ",
                List.of("&7ᴄʟɪᴄᴋ ᴛᴏ ʀᴇꜰʀᴇѕʜ")
        ));

        // Next Page (53)
        if (currentPage < totalPages - 1) {
            set(53, ItemUtils.createItem(
                    Material.ARROW,
                    "&aɴᴇxᴛ ᴘᴀɢᴇ",
                    List.of("&7ɢᴏ ᴛᴏ ᴘᴀɢᴇ " + (currentPage + 2))
            ));
        }
    }

    @Override
    public void handleClick(int slot, Player player, ClickType clickType) {
        if (slot >= 0 && slot < renderedEntries.size()) {
            PlayerEntry pe = renderedEntries.get(slot);
            playButtonClick(player);
            new FriendDetailMenu(plugin, pe.uuid(), pe.name(), page, searchQuery, filterType).open(player);
            return;
        }

        int totalItems = plugin.getFriendsManager().getFollowing(player.getUniqueId()).size() +
                plugin.getFriendsManager().getFollowers(player.getUniqueId()).size();
        int totalPages = (totalItems - 1) / 45 + 1;

        if (slot == 45 && page > 0) {
            playPageTurn(player);
            new FriendsMenu(plugin, page - 1, searchQuery, filterType).open(player);
        } else if (slot == 53 && page < totalPages - 1) {
            playPageTurn(player);
            new FriendsMenu(plugin, page + 1, searchQuery, filterType).open(player);
        } else if (slot == 48) {
            playButtonClick(player);
            if (clickType.isRightClick()) {
                new FriendsMenu(plugin, 0, null, filterType).open(player);
            } else {
                promptSearch(player);
            }
        } else if (slot == 49) {
            playButtonClick(player);
            new FriendsMenu(plugin, 0, searchQuery, filterType.next()).open(player);
        } else if (slot == 50) {
            playButtonClick(player);
            new FriendsMenu(plugin, page, searchQuery, filterType).open(player);
        }
    }

    private void playButtonClick(Player player) {
        SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
    }

    private void playPageTurn(Player player) {
        SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.PAGE-TURN"));
    }

    private void promptSearch(Player player) {
        SignInputUtil.open(
                plugin,
                player,
                List.of("", "↑↑↑↑↑↑↑↑↑↑↑↑↑↑", "Search", ""),
                0,
                text -> {
                    String input = text == null ? "" : text.trim();
                    if (input.isBlank() || input.equalsIgnoreCase("cancel")) {
                        new FriendsMenu(plugin, page, searchQuery, filterType).open(player);
                    } else if (input.equalsIgnoreCase("clear")) {
                        new FriendsMenu(plugin, 0, null, filterType).open(player);
                    } else {
                        new FriendsMenu(plugin, 0, input, filterType).open(player);
                    }
                }
        );
    }
}
