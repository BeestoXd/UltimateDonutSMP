package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.EnchantmentsManager;
import com.bx.ultimateDonutSmp.managers.EnchantmentsManager.EnchantOption;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;

public class EnchantSelectMenu extends BaseMenu {

    private final Material material;
    private final Map<Enchantment, Integer> selected = new LinkedHashMap<>();
    private final List<EnchantOption> options;
    private int page = 1;
    private final int maxPage;

    public EnchantSelectMenu(UltimateDonutSmp plugin, Player player, Material material) {
        super(plugin, plugin.getEnchantmentsManager().getGuiTitle(), plugin.getEnchantmentsManager().getGuiRows() * 9);
        this.material = material;
        this.options = plugin.getEnchantmentsManager().optionsFor(material);
        this.maxPage = plugin.getEnchantmentsManager().maxPage(this.options);

        // Load existing enchants from draft session if present
        var draft = plugin.getOrdersManager().getPendingCreation(player.getUniqueId());
        if (draft != null && draft.requestedItem() != null) {
            ItemStack draftItem = draft.requestedItem();
            if (draftItem.getType() == Material.ENCHANTED_BOOK) {
                if (draftItem.getItemMeta() instanceof org.bukkit.inventory.meta.EnchantmentStorageMeta esm) {
                    selected.putAll(esm.getStoredEnchants());
                }
            } else {
                selected.putAll(draftItem.getEnchantments());
            }
        }
    }

    @Override
    public void build(Player player) {
        clear();
        EnchantmentsManager em = plugin.getEnchantmentsManager();

        // 1. Fill background if enabled
        fill(Material.GRAY_STAINED_GLASS_PANE);

        // 2. Preview item slot (usually 0)
        ItemStack preview = new ItemStack(material);
        ItemMeta previewMeta = preview.getItemMeta();
        if (previewMeta != null) {
            if (material == Material.ENCHANTED_BOOK) {
                org.bukkit.inventory.meta.EnchantmentStorageMeta esm = (org.bukkit.inventory.meta.EnchantmentStorageMeta) previewMeta;
                for (Map.Entry<Enchantment, Integer> entry : selected.entrySet()) {
                    esm.addStoredEnchant(entry.getKey(), entry.getValue(), true);
                }
            } else {
                for (Map.Entry<Enchantment, Integer> entry : selected.entrySet()) {
                    previewMeta.addEnchant(entry.getKey(), entry.getValue(), true);
                }
            }
            preview.setItemMeta(previewMeta);
        }
        set(em.getItemSlot(), preview);

        // 3. Navigation and action buttons
        set(em.getCancelSlot(), ItemUtils.createItem(
                Material.RED_STAINED_GLASS_PANE,
                "&cᴄᴀɴᴄᴇʟ",
                List.of("&7ᴄʟɪᴄᴋ ᴛᴏ ʀᴇᴛᴜʀɴ ᴡɪᴛʜᴏᴜᴛ ѕᴀᴠɪɴɢ")
        ));

        set(em.getConfirmSlot(), ItemUtils.createItem(
                Material.LIME_STAINED_GLASS_PANE,
                "&aᴄᴏɴꜰɪʀᴍ",
                List.of("&7ᴄʟɪᴄᴋ ᴛᴏ ᴄᴏɴꜰɪʀᴍ ᴛʜᴇѕᴇ ᴇɴᴄʜᴀɴᴛᴍᴇɴᴛѕ")
        ));

        if (page > 1) {
            set(em.getPrevSlot(), ItemUtils.createItem(
                    Material.ARROW,
                    "&bᴘʀᴇᴠɪᴏᴜѕ ᴘᴀɢᴇ",
                    List.of("&7ɢᴏ ᴛᴏ ᴘᴀɢᴇ &f" + (page - 1))
            ));
        } else {
            set(em.getPrevSlot(), ItemUtils.createPlaceholder(Material.GRAY_STAINED_GLASS_PANE));
        }

        if (page < maxPage) {
            set(em.getNextSlot(), ItemUtils.createItem(
                    Material.ARROW,
                    "&bɴᴇxᴛ ᴘᴀɢᴇ",
                    List.of("&7ɢᴏ ᴛᴏ ᴘᴀɢᴇ &f" + (page + 1))
            ));
        } else {
            set(em.getNextSlot(), ItemUtils.createPlaceholder(Material.GRAY_STAINED_GLASS_PANE));
        }

        // 4. Render enchant option books for current page
        for (EnchantOption opt : options) {
            if (opt.page != page) {
                continue;
            }

            boolean isCurrent = selected.containsKey(opt.ench) && selected.get(opt.ench) == opt.level;
            boolean hasConflict = !isCurrent && conflictsWithCurrent(opt.ench);

            Material bookMat = Material.ENCHANTED_BOOK;
            String displayName = "&e" + formatEnchantName(opt.ench) + " " + roman(opt.level);
            List<String> lore = new ArrayList<>();

            if (isCurrent) {
                lore.add(em.getMessageSelected());
            } else if (hasConflict) {
                lore.add(em.getMessageCannot());
                lore.add("&7(ᴄᴏɴꜰʟɪᴄᴛѕ ᴡɪᴛʜ ᴀɴᴏᴛʜᴇʀ ᴇɴᴄʜᴀɴᴛᴍᴇɴᴛ)");
            } else {
                lore.add(em.getMessageSelect());
            }

            set(opt.slot, ItemUtils.createItem(bookMat, displayName, lore));
        }
    }

    @Override
    public void handleClick(int slot, Player player, ClickType clickType) {
        EnchantmentsManager em = plugin.getEnchantmentsManager();

        if (slot == em.getCancelSlot()) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            // Return to item selection or draft builder
            plugin.getOrdersManager().openNewOrderItemSelection(player);
            return;
        }

        if (slot == em.getConfirmSlot()) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            // Apply selected enchants to the draft session
            ItemStack draftStack = new ItemStack(material);
            ItemMeta meta = draftStack.getItemMeta();
            if (meta != null) {
                if (material == Material.ENCHANTED_BOOK) {
                    org.bukkit.inventory.meta.EnchantmentStorageMeta esm = (org.bukkit.inventory.meta.EnchantmentStorageMeta) meta;
                    for (Map.Entry<Enchantment, Integer> entry : selected.entrySet()) {
                        esm.addStoredEnchant(entry.getKey(), entry.getValue(), true);
                    }
                } else {
                    for (Map.Entry<Enchantment, Integer> entry : selected.entrySet()) {
                        meta.addEnchant(entry.getKey(), entry.getValue(), true);
                    }
                }
                draftStack.setItemMeta(meta);
            }

            // Save stack as the draft item
            plugin.getOrdersManager().updateDraftItem(player.getUniqueId(), draftStack);
            new OrdersNewMenu(plugin).open(player);
            return;
        }

        if (slot == em.getPrevSlot()) {
            if (page > 1) {
                SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.PAGE-TURN"));
                page--;
                build(player);
            }
            return;
        }

        if (slot == em.getNextSlot()) {
            if (page < maxPage) {
                SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.PAGE-TURN"));
                page++;
                build(player);
            }
            return;
        }

        // Check if player clicked an enchantment option book
        for (EnchantOption opt : options) {
            if (opt.page == page && opt.slot == slot) {
                toggle(opt, player);
                return;
            }
        }
    }

    private void toggle(EnchantOption opt, Player player) {
        boolean isCurrent = selected.containsKey(opt.ench) && selected.get(opt.ench) == opt.level;
        if (isCurrent) {
            selected.remove(opt.ench);
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
        } else if (!conflictsWithCurrent(opt.ench)) {
            selected.put(opt.ench, opt.level);
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
        } else {
            // Conflicts
            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            player.sendMessage(ColorUtils.toComponent(plugin.getEnchantmentsManager().getMessageCannot()));
        }
        build(player);
    }

    private boolean conflictsWithCurrent(Enchantment ench) {
        for (Enchantment selectedEnch : selected.keySet()) {
            if (selectedEnch.equals(ench)) {
                // Toggling levels of same enchant is allowed
                continue;
            }
            try {
                if (ench.conflictsWith(selectedEnch) || selectedEnch.conflictsWith(ench)) {
                    return true;
                }
            } catch (Throwable ignored) {}
        }
        return false;
    }

    private String formatEnchantName(Enchantment ench) {
        String key = ench.getKey().getKey().replace('_', ' ');
        return Arrays.stream(key.split(" "))
                .map(word -> word.isEmpty() ? "" : Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }

    private static String roman(int n) {
        int[] values = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        String[] symbols = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            while (n >= values[i]) {
                n -= values[i];
                sb.append(symbols[i]);
            }
        }
        return sb.toString();
    }
}
