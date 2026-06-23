package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.utils.PermissionUtils;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.invsee.InvseeHolder;
import com.bx.ultimateDonutSmp.invsee.InvseeSession;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class InvseeManager {

    private static final int DEFAULT_SIZE = 54;
    private static final List<Integer> DEFAULT_ARMOR_SLOTS = List.of(0, 1, 2, 3);
    private static final int DEFAULT_OFFHAND_SLOT = 4;
    private static final int DEFAULT_SUMMARY_SLOT = 6;
    private static final int DEFAULT_STATUS_SLOT = 8;
    private static final int DEFAULT_MAIN_START = 18;
    private static final int DEFAULT_HOTBAR_START = 45;

    private final UltimateDonutSmp plugin;
    private final Map<UUID, InvseeSession> sessionsByViewer = new HashMap<>();
    private final Map<UUID, Set<UUID>> viewersByTarget = new HashMap<>();
    private BukkitTask syncTask;

    public InvseeManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        if (!isEnabled()) {
            cancelSyncTask();
            closeAllSessions(true);
            return;
        }

        restartSyncTask();
        refreshOpenSessions();
    }

    public void shutdown() {
        cancelSyncTask();
        closeAllSessions(false);
        sessionsByViewer.clear();
        viewersByTarget.clear();
    }

    public boolean isEnabled() {
        return plugin.getFeatureManager().isEnabled(FeatureManager.Feature.INVSEE)
                && getConfig().getBoolean("INVSEE.ENABLED", true);
    }

    public boolean requiresOnlineTarget() {
        return getConfig().getBoolean("INVSEE.REQUIRE-ONLINE", true);
    }

    public boolean allowSelfView() {
        return getConfig().getBoolean("INVSEE.ALLOW-SELF-VIEW", false);
    }

    public boolean shouldNotifyTarget() {
        return getConfig().getBoolean("INVSEE.NOTIFY-TARGET", false);
    }

    public boolean shouldLogUsage() {
        return getConfig().getBoolean("INVSEE.LOG-USAGE", true);
    }

    public boolean shouldFreezeOnLogout() {
        return getConfig().getBoolean("INVSEE.FREEZE-ON-LOGOUT", true);
    }

    public boolean isEditEnabled() {
        return getConfig().getBoolean("INVSEE.ALLOW-EDIT", false);
    }

    public String getViewPermission() {
        return getConfig().getString("INVSEE.VIEW-PERMISSION", "ULTIMATEDONUTSMP.STAFF.INVSEE");
    }

    public String getModifyPermission() {
        return getConfig().getString("INVSEE.MODIFY-PERMISSION", "ULTIMATEDONUTSMP.STAFF.INVSEE.MODIFY");
    }

    public String getAdminPermission() {
        return getConfig().getString("INVSEE.ADMIN-PERMISSION", "ULTIMATEDONUTSMP.ADMIN.INVSEE");
    }

    public boolean canView(Player viewer) {
        return viewer != null && PermissionUtils.has(viewer, getViewPermission());
    }

    public boolean canModify(Player viewer) {
        return viewer != null && PermissionUtils.has(viewer, getModifyPermission());
    }

    public boolean canOpenEditableSession(Player viewer, UUID targetUuid) {
        if (viewer == null || targetUuid == null) {
            return false;
        }
        if (!isEditEnabled() || !canModify(viewer)) {
            return false;
        }

        UUID activeEditor = findActiveEditor(targetUuid);
        return activeEditor == null || activeEditor.equals(viewer.getUniqueId());
    }

    public boolean canAdmin(CommandSender sender) {
        return sender != null && PermissionUtils.has(sender, getAdminPermission());
    }

    public String getMessage(String path, String fallback) {
        return getConfig().getString("MESSAGES." + path, fallback);
    }

    public String formatMessage(String path, String fallback, String... placeholders) {
        String message = getMessage(path, fallback);
        for (int i = 0; i + 1 < placeholders.length; i += 2) {
            message = message.replace(placeholders[i], placeholders[i + 1]);
        }
        return message;
    }

    public Player findOnlineTarget(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }

        Player exact = Bukkit.getPlayerExact(username);
        if (exact != null) {
            return exact;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getName().equalsIgnoreCase(username)) {
                return player;
            }
        }
        return null;
    }

    public boolean hasKnownPlayer(String username) {
        return username != null
                && !username.isBlank()
                && plugin.getDatabaseManager().findPlayerUuidByUsername(username) != null;
    }

    public boolean isInvseeInventory(Inventory inventory) {
        return inventory != null && inventory.getHolder() instanceof InvseeHolder;
    }

    public boolean isInvseeView(InventoryView view) {
        return view != null && isInvseeInventory(view.getTopInventory());
    }

    public InvseeSession getSession(InventoryView view) {
        if (!isInvseeView(view)) {
            return null;
        }

        InvseeHolder holder = (InvseeHolder) view.getTopInventory().getHolder();
        InvseeSession session = sessionsByViewer.get(holder.getViewerUuid());
        if (session == null || session.getInventory() != view.getTopInventory()) {
            return null;
        }
        return session;
    }

    public boolean isEditableTopSlot(int slot) {
        if (slot < 0 || slot >= getInventorySize()) {
            return false;
        }

        if (getArmorSlots().contains(slot) || slot == getOffhandSlot()) {
            return true;
        }

        return isMainInventorySlot(slot) || isHotbarSlot(slot);
    }

    public void handleInventoryClick(InventoryClickEvent event) {
        InvseeSession session = getSession(event.getView());
        if (session == null) {
            return;
        }

        if (session.isFrozen()) {
            deny(event);
            return;
        }

        if (!session.isEditable()) {
            deny(event);
            return;
        }

        if (event.getClick().isCreativeAction()
                || event.getAction() == InventoryAction.COLLECT_TO_CURSOR) {
            deny(event);
            return;
        }

        int rawSlot = event.getRawSlot();
        int topSize = event.getView().getTopInventory().getSize();
        if (rawSlot >= 0 && rawSlot < topSize && !isEditableTopSlot(rawSlot)) {
            deny(event);
            return;
        }

        scheduleWriteBack(session);
    }

    public void handleInventoryDrag(InventoryDragEvent event) {
        InvseeSession session = getSession(event.getView());
        if (session == null) {
            return;
        }

        if (session.isFrozen() || !session.isEditable()) {
            deny(event);
            return;
        }

        int topSize = event.getView().getTopInventory().getSize();
        for (int rawSlot : event.getRawSlots()) {
            if (rawSlot < topSize && !isEditableTopSlot(rawSlot)) {
                deny(event);
                return;
            }
        }

        scheduleWriteBack(session);
    }

    public void open(Player viewer, Player target) {
        if (viewer == null || target == null) {
            return;
        }

        removeSession(sessionsByViewer.get(viewer.getUniqueId()));
        boolean editable = canOpenEditableSession(viewer, target.getUniqueId());

        InvseeHolder holder = new InvseeHolder(viewer.getUniqueId(), target.getUniqueId());
        Inventory inventory = Bukkit.createInventory(
                holder,
                getInventorySize(),
                ColorUtils.toComponent(buildTitle(target.getName()), viewer)
        );
        holder.bind(inventory);

        InvseeSession session = new InvseeSession(
                viewer.getUniqueId(),
                target.getUniqueId(),
                inventory,
                target.getName(),
                editable
        );
        sessionsByViewer.put(viewer.getUniqueId(), session);
        viewersByTarget.computeIfAbsent(target.getUniqueId(), ignored -> new HashSet<>()).add(viewer.getUniqueId());

        initializeLayout(session);
        syncSession(session, target);
        viewer.openInventory(inventory);

        if (isEditEnabled() && canModify(viewer) && !editable) {
            viewer.sendMessage(ColorUtils.toComponent(formatMessage(
                    "EDIT-CONFLICT",
                    "&eᴀɴᴏᴛʜᴇʀ ѕᴛᴀꜰꜰ ᴍᴇᴍʙᴇʀ ɪѕ ᴀʟʀᴇᴀᴅʏ ᴇᴅɪᴛɪɴɢ ᴛʜɪѕ ɪɴᴠᴇɴᴛᴏʀʏ. ᴏᴘᴇɴᴇᴅ ɪɴ ʀᴇᴀᴅ-ᴏɴʟʏ ᴍᴏᴅᴇ.",
                    "{target}", target.getName(),
                    "{player}", target.getName()
            )));
        }

        if (shouldNotifyTarget()) {
            target.sendMessage(ColorUtils.toComponent(formatMessage(
                    "TARGET-NOTIFY",
                    "&eʏᴏᴜʀ ɪɴᴠᴇɴᴛᴏʀʏ ɪѕ ʙᴇɪɴɢ ᴠɪᴇᴡᴇᴅ ʙʏ ѕᴛᴀꜰꜰ.",
                    "{viewer}", viewer.getName(),
                    "{player}", viewer.getName(),
                    "{mode}", editable ? "ᴇᴅɪᴛᴀʙʟᴇ" : "ʀᴇᴀᴅ-ᴏɴʟʏ"
            )));
        }

        if (shouldLogUsage()) {
            plugin.getLogger().info(
                    "invsee opened: viewer=" + viewer.getName()
                            + " target=" + target.getName()
                            + " mode=" + (editable ? "editable" : "read-only")
            );
        }
    }

    public void handleViewerClose(Player viewer, Inventory inventory) {
        if (!isInvseeInventory(inventory)) {
            return;
        }

        InvseeHolder holder = (InvseeHolder) inventory.getHolder();
        InvseeSession session = sessionsByViewer.get(holder.getViewerUuid());
        if (session == null || session.getInventory() != inventory) {
            return;
        }

        removeSession(session);
    }

    public void handleViewerQuit(Player viewer) {
        if (viewer == null) {
            return;
        }

        removeSession(sessionsByViewer.get(viewer.getUniqueId()));
    }

    public void handleTargetQuit(Player target) {
        if (target == null) {
            return;
        }

        if (shouldFreezeOnLogout()) {
            freezeSessionsForTarget(target.getUniqueId());
            return;
        }

        closeSessionsForTarget(target.getUniqueId(), true);
    }

    private void refreshOpenSessions() {
        for (InvseeSession session : List.copyOf(sessionsByViewer.values())) {
            Player viewer = Bukkit.getPlayer(session.getViewerUuid());
            if (viewer == null || !viewer.isOnline()) {
                removeSession(session);
                continue;
            }

            if (viewer.getOpenInventory().getTopInventory() != session.getInventory()) {
                removeSession(session);
                continue;
            }

            if (session.isEditable() && !canKeepEditable(viewer, session)) {
                session.setEditable(false);
            }

            initializeLayout(session);
            if (session.isFrozen()) {
                updateInfoItems(session, null);
                continue;
            }

            Player target = Bukkit.getPlayer(session.getTargetUuid());
            if (target == null || !target.isOnline()) {
                freezeSession(session);
                continue;
            }

            syncSession(session, target);
        }
    }

    private void syncAllSessions() {
        for (Map.Entry<UUID, Set<UUID>> entry : List.copyOf(viewersByTarget.entrySet())) {
            UUID targetUuid = entry.getKey();
            Set<UUID> viewerUuids = entry.getValue();
            if (viewerUuids == null || viewerUuids.isEmpty()) {
                viewersByTarget.remove(targetUuid);
                continue;
            }

            Player target = Bukkit.getPlayer(targetUuid);
            if (target == null || !target.isOnline()) {
                if (shouldFreezeOnLogout()) {
                    freezeSessionsForTarget(targetUuid);
                } else {
                    closeSessionsForTarget(targetUuid, true);
                }
                continue;
            }

            for (UUID viewerUuid : List.copyOf(viewerUuids)) {
                InvseeSession session = sessionsByViewer.get(viewerUuid);
                if (session == null) {
                    viewerUuids.remove(viewerUuid);
                    continue;
                }

                if (session.isFrozen()) {
                    continue;
                }

                Player viewer = Bukkit.getPlayer(viewerUuid);
                if (viewer == null || !viewer.isOnline()) {
                    removeSession(session);
                    continue;
                }

                if (viewer.getOpenInventory().getTopInventory() != session.getInventory()) {
                    removeSession(session);
                    continue;
                }

                if (session.isEditable() && !canKeepEditable(viewer, session)) {
                    session.setEditable(false);
                }

                if (session.isWriteBackScheduled()) {
                    continue;
                }

                syncSession(session, target);
            }

            if (viewerUuids.isEmpty()) {
                viewersByTarget.remove(targetUuid);
            }
        }
    }

    private void syncSession(InvseeSession session, Player target) {
        if (session == null || target == null) {
            return;
        }

        Inventory inventory = session.getInventory();
        PlayerInventory targetInventory = target.getInventory();

        setArmor(inventory, targetInventory);
        inventory.setItem(getOffhandSlot(), sanitizeForView(targetInventory.getItemInOffHand()));
        setStorage(inventory, targetInventory);
        updateInfoItems(session, target);
        session.markSynced();
    }

    private void setArmor(Inventory inventory, PlayerInventory targetInventory) {
        List<Integer> armorSlots = getArmorSlots();
        ItemStack[] armorItems = new ItemStack[]{
                sanitizeForView(targetInventory.getHelmet()),
                sanitizeForView(targetInventory.getChestplate()),
                sanitizeForView(targetInventory.getLeggings()),
                sanitizeForView(targetInventory.getBoots())
        };

        for (int index = 0; index < Math.min(armorSlots.size(), armorItems.length); index++) {
            inventory.setItem(armorSlots.get(index), armorItems[index]);
        }
    }

    private void setStorage(Inventory inventory, PlayerInventory targetInventory) {
        ItemStack[] storage = targetInventory.getStorageContents();
        for (int index = 0; index < 27; index++) {
            inventory.setItem(getMainInventoryStart() + index, cloneStorageItem(storage, index + 9));
        }

        for (int index = 0; index < 9; index++) {
            inventory.setItem(getHotbarStart() + index, cloneStorageItem(storage, index));
        }
    }

    private ItemStack cloneStorageItem(ItemStack[] storage, int index) {
        if (storage == null || index < 0 || index >= storage.length) {
            return null;
        }
        return sanitizeForView(storage[index]);
    }

    private void updateInfoItems(InvseeSession session, Player target) {
        Inventory inventory = session.getInventory();
        inventory.setItem(getSummarySlot(), buildSummaryItem(session, target));
        inventory.setItem(getStatusSlot(), buildStatusItem(session, target));
    }

    private ItemStack buildSummaryItem(InvseeSession session, Player target) {
        UUID targetUuid = session.getTargetUuid();
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(targetUuid);
        String targetName = target == null ? session.getTargetName() : target.getName();

        List<String> lore = new ArrayList<>();
        lore.add("&7status: &f" + (session.isFrozen() ? "frozen snapshot" : "Live"));
        lore.add("&7player: &f" + targetName);
        lore.add("&7world: &f" + (target == null ? "Offline" : target.getWorld().getName()));
        if (target != null) {
            lore.add("&7coords: &f" + target.getLocation().getBlockX()
                    + ", " + target.getLocation().getBlockY()
                    + ", " + target.getLocation().getBlockZ());
        } else {
            lore.add("&7coords: &funavailable");
        }
        lore.add("&7mode: &f" + (session.isEditable() && !session.isFrozen() ? "Editable" : "read only"));

        return ItemUtils.createPlayerHead(
                offlinePlayer,
                "&6inventory of &f" + targetName,
                lore
        );
    }

    private ItemStack buildStatusItem(InvseeSession session, Player target) {
        boolean live = !session.isFrozen() && target != null && target.isOnline();
        boolean editable = session.isEditable() && !session.isFrozen();
        Material material = live
                ? (editable ? Material.ORANGE_DYE : Material.LIME_DYE)
                : Material.GRAY_DYE;
        String name = live
                ? (editable ? "&6live edit" : "&alive sync")
                : "&7frozen snapshot";
        List<String> lore = live
                ? editable
                ? List.of(
                "&7ᴛʜɪѕ ᴠɪᴇᴡ ɪѕ ᴍɪʀʀᴏʀɪɴɢ ᴛʜᴇ ᴛᴀʀɢᴇᴛ'ѕ",
                "&7ɪɴᴠᴇɴᴛᴏʀʏ ᴀɴᴅ ᴡɪʟʟ ᴡʀɪᴛᴇ ᴄʜᴀɴɢᴇѕ ʙᴀᴄᴋ",
                "&7ᴡʜɪʟᴇ ᴛʜᴇ ᴛᴀʀɢᴇᴛ ʀᴇᴍᴀɪɴѕ ᴏɴʟɪɴᴇ."
        )
                : List.of(
                "&7ᴛʜɪѕ ᴠɪᴇᴡ ɪѕ ᴍɪʀʀᴏʀɪɴɢ ᴛʜᴇ ᴛᴀʀɢᴇᴛ'ѕ",
                "&7ɪɴᴠᴇɴᴛᴏʀʏ ᴡʜɪʟᴇ ᴛʜᴇʏ ʀᴇᴍᴀɪɴ ᴏɴʟɪɴᴇ.",
                "&cᴀʟʟ ɪɴᴛᴇʀᴀᴄᴛɪᴏɴѕ ᴀʀᴇ ʙʟᴏᴄᴋᴇᴅ."
        )
                : List.of(
                "&7ᴛʜᴇ ᴛᴀʀɢᴇᴛ ᴡᴇɴᴛ ᴏꜰꜰʟɪɴᴇ.",
                "&7ᴛʜɪѕ ɢᴜɪ ɴᴏᴡ ѕʜᴏᴡѕ ᴛʜᴇ ʟᴀѕᴛ ѕʏɴᴄᴇᴅ ѕᴛᴀᴛᴇ.",
                "&cᴀʟʟ ɪɴᴛᴇʀᴀᴄᴛɪᴏɴѕ ʀᴇᴍᴀɪɴ ʙʟᴏᴄᴋᴇᴅ."
        );

        return ItemUtils.createItem(material, name, lore);
    }

    private void initializeLayout(InvseeSession session) {
        Inventory inventory = session.getInventory();
        Set<Integer> reservedSlots = getReservedSlots();
        ItemStack filler = ItemUtils.createGlassPane(Material.GRAY_STAINED_GLASS_PANE);
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            if (reservedSlots.contains(slot)) {
                continue;
            }
            inventory.setItem(slot, filler);
        }
    }

    private Set<Integer> getReservedSlots() {
        Set<Integer> reserved = new HashSet<>();
        reserved.addAll(getArmorSlots());
        reserved.add(getOffhandSlot());
        reserved.add(getSummarySlot());
        reserved.add(getStatusSlot());

        for (int index = 0; index < 27; index++) {
            reserved.add(getMainInventoryStart() + index);
        }
        for (int index = 0; index < 9; index++) {
            reserved.add(getHotbarStart() + index);
        }
        return reserved;
    }

    private void freezeSessionsForTarget(UUID targetUuid) {
        Set<UUID> viewerUuids = viewersByTarget.get(targetUuid);
        if (viewerUuids == null || viewerUuids.isEmpty()) {
            return;
        }

        for (UUID viewerUuid : List.copyOf(viewerUuids)) {
            InvseeSession session = sessionsByViewer.get(viewerUuid);
            if (session == null) {
                viewerUuids.remove(viewerUuid);
                continue;
            }
            freezeSession(session);
        }
    }

    private void freezeSession(InvseeSession session) {
        if (session == null || session.isFrozen()) {
            return;
        }

        session.freeze();
        updateInfoItems(session, null);

        Player viewer = Bukkit.getPlayer(session.getViewerUuid());
        if (viewer != null && viewer.isOnline()) {
            viewer.sendMessage(ColorUtils.toComponent(formatMessage(
                    "TARGET-LOGGED-OUT",
                    "&eᴛʜɪѕ ɪɴᴠᴇɴᴛᴏʀʏ ɪѕ ɴᴏᴡ ᴀ ꜰʀᴏᴢᴇɴ ѕɴᴀᴘѕʜᴏᴛ ʙᴇᴄᴀᴜѕᴇ ᴛʜᴇ ᴛᴀʀɢᴇᴛ ʟᴏɢɢᴇᴅ ᴏᴜᴛ.",
                    "{player}", session.getTargetName(),
                    "{target}", session.getTargetName()
            )));
        }

        if (shouldLogUsage()) {
            plugin.getLogger().info(
                    "invsee frozen: viewer=" + session.getViewerUuid()
                            + " target=" + session.getTargetName()
            );
        }
    }

    private void closeSessionsForTarget(UUID targetUuid, boolean closeInventory) {
        Set<UUID> viewerUuids = viewersByTarget.get(targetUuid);
        if (viewerUuids == null || viewerUuids.isEmpty()) {
            return;
        }

        for (UUID viewerUuid : List.copyOf(viewerUuids)) {
            InvseeSession session = sessionsByViewer.get(viewerUuid);
            if (session == null) {
                viewerUuids.remove(viewerUuid);
                continue;
            }

            removeSession(session);
            if (!closeInventory) {
                continue;
            }

            Player viewer = Bukkit.getPlayer(viewerUuid);
            if (viewer != null && viewer.isOnline()
                    && viewer.getOpenInventory().getTopInventory() == session.getInventory()) {
                viewer.closeInventory();
            }
        }
    }

    private void closeAllSessions(boolean closeInventory) {
        for (InvseeSession session : List.copyOf(sessionsByViewer.values())) {
            removeSession(session);
            if (!closeInventory) {
                continue;
            }

            Player viewer = Bukkit.getPlayer(session.getViewerUuid());
            if (viewer != null && viewer.isOnline()
                    && viewer.getOpenInventory().getTopInventory() == session.getInventory()) {
                viewer.closeInventory();
            }
        }
    }

    private void scheduleWriteBack(InvseeSession session) {
        if (session == null || !session.isEditable() || session.isFrozen() || session.isWriteBackScheduled()) {
            return;
        }

        session.scheduleWriteBack();
        Player target = Bukkit.getPlayer(session.getTargetUuid());
        Runnable writeBack = () -> {
            try {
                applyInventoryToTarget(session);
            } finally {
                session.completeWriteBack();
            }
        };
        if (target != null && target.isOnline()) {
            plugin.getSpigotScheduler().runEntity(target, writeBack);
        } else {
            plugin.getSpigotScheduler().runGlobal(writeBack);
        }
    }

    private void applyInventoryToTarget(InvseeSession session) {
        if (session == null || !session.isEditable() || session.isFrozen()) {
            return;
        }

        Player target = Bukkit.getPlayer(session.getTargetUuid());
        if (target == null || !target.isOnline()) {
            freezeSession(session);
            return;
        }

        Player viewer = Bukkit.getPlayer(session.getViewerUuid());
        if (viewer == null || !viewer.isOnline()) {
            removeSession(session);
            return;
        }

        if (viewer.getOpenInventory().getTopInventory() != session.getInventory()) {
            removeSession(session);
            return;
        }

        Inventory inventory = session.getInventory();
        PlayerInventory targetInventory = target.getInventory();
        ItemStack[] storageContents = new ItemStack[36];

        for (int index = 0; index < 27; index++) {
            storageContents[index + 9] = sanitizeForTarget(inventory.getItem(getMainInventoryStart() + index));
        }
        for (int index = 0; index < 9; index++) {
            storageContents[index] = sanitizeForTarget(inventory.getItem(getHotbarStart() + index));
        }

        ItemStack[] armorContents = new ItemStack[4];
        List<Integer> armorSlots = getArmorSlots();
        for (int index = 0; index < 4; index++) {
            armorContents[index] = sanitizeForTarget(inventory.getItem(armorSlots.get(index)));
        }

        targetInventory.setStorageContents(storageContents);
        targetInventory.setHelmet(armorContents[0]);
        targetInventory.setChestplate(armorContents[1]);
        targetInventory.setLeggings(armorContents[2]);
        targetInventory.setBoots(armorContents[3]);
        targetInventory.setItemInOffHand(sanitizeForTarget(inventory.getItem(getOffhandSlot())));

        plugin.getWorthManager().syncWorthDisplay(target);
        target.updateInventory();
        syncSessionsForTarget(session.getTargetUuid());
    }

    private void syncSessionsForTarget(UUID targetUuid) {
        Player target = Bukkit.getPlayer(targetUuid);
        if (target == null || !target.isOnline()) {
            return;
        }

        Set<UUID> viewerUuids = viewersByTarget.get(targetUuid);
        if (viewerUuids == null || viewerUuids.isEmpty()) {
            return;
        }

        for (UUID viewerUuid : List.copyOf(viewerUuids)) {
            InvseeSession currentSession = sessionsByViewer.get(viewerUuid);
            if (currentSession == null || currentSession.isFrozen() || currentSession.isWriteBackScheduled()) {
                continue;
            }

            Player viewer = Bukkit.getPlayer(viewerUuid);
            if (viewer == null || !viewer.isOnline()
                    || viewer.getOpenInventory().getTopInventory() != currentSession.getInventory()) {
                removeSession(currentSession);
                continue;
            }

            syncSession(currentSession, target);
        }
    }

    private boolean canKeepEditable(Player viewer, InvseeSession session) {
        return viewer != null
                && session != null
                && isEditEnabled()
                && canModify(viewer)
                && isCurrentEditor(viewer.getUniqueId(), session.getTargetUuid());
    }

    private boolean isCurrentEditor(UUID viewerUuid, UUID targetUuid) {
        UUID activeEditor = findActiveEditor(targetUuid);
        return activeEditor == null || activeEditor.equals(viewerUuid);
    }

    private UUID findActiveEditor(UUID targetUuid) {
        Set<UUID> viewerUuids = viewersByTarget.get(targetUuid);
        if (viewerUuids == null || viewerUuids.isEmpty()) {
            return null;
        }

        for (UUID viewerUuid : List.copyOf(viewerUuids)) {
            InvseeSession session = sessionsByViewer.get(viewerUuid);
            if (session == null || session.isFrozen() || !session.isEditable()) {
                continue;
            }
            return viewerUuid;
        }
        return null;
    }

    private boolean isMainInventorySlot(int slot) {
        int start = getMainInventoryStart();
        return slot >= start && slot < start + 27;
    }

    private boolean isHotbarSlot(int slot) {
        int start = getHotbarStart();
        return slot >= start && slot < start + 9;
    }

    private void removeSession(InvseeSession session) {
        if (session == null) {
            return;
        }

        sessionsByViewer.remove(session.getViewerUuid(), session);
        Set<UUID> viewerUuids = viewersByTarget.get(session.getTargetUuid());
        if (viewerUuids != null) {
            viewerUuids.remove(session.getViewerUuid());
            if (viewerUuids.isEmpty()) {
                viewersByTarget.remove(session.getTargetUuid());
            }
        }
    }

    private void restartSyncTask() {
        cancelSyncTask();

        long refreshTicks = getConfig().getLong("INVSEE.AUTO-REFRESH-TICKS", 10L);
        if (refreshTicks <= 0L) {
            return;
        }

        syncTask = plugin.getSpigotScheduler().runGlobalTimer(
                this::syncAllSessions,
                refreshTicks,
                refreshTicks
        );
    }

    private void cancelSyncTask() {
        if (syncTask != null) {
            syncTask.cancel();
            syncTask = null;
        }
    }

    private String buildTitle(String targetName) {
        String template = getConfig().getString("INVSEE.TITLE", "&8inventory of {player}");
        return template
                .replace("{player}", targetName)
                .replace("{target}", targetName);
    }

    private int getInventorySize() {
        int configured = getConfig().getInt("LAYOUT.SIZE", DEFAULT_SIZE);
        if (configured < DEFAULT_SIZE || configured > DEFAULT_SIZE || configured % 9 != 0) {
            return DEFAULT_SIZE;
        }
        return configured;
    }

    private List<Integer> getArmorSlots() {
        List<Integer> configured = getConfig().getIntegerList("LAYOUT.ARMOR-SLOTS");
        if (configured.size() < 4) {
            return DEFAULT_ARMOR_SLOTS;
        }

        List<Integer> sanitized = new ArrayList<>(4);
        for (int index = 0; index < 4; index++) {
            sanitized.add(sanitizeSlot(configured.get(index), DEFAULT_ARMOR_SLOTS.get(index)));
        }
        return sanitized;
    }

    private int getOffhandSlot() {
        return sanitizeSlot(getConfig().getInt("LAYOUT.OFFHAND-SLOT", DEFAULT_OFFHAND_SLOT), DEFAULT_OFFHAND_SLOT);
    }

    private int getSummarySlot() {
        return sanitizeSlot(getConfig().getInt("LAYOUT.SUMMARY-SLOT", DEFAULT_SUMMARY_SLOT), DEFAULT_SUMMARY_SLOT);
    }

    private int getStatusSlot() {
        return sanitizeSlot(getConfig().getInt("LAYOUT.STATUS-SLOT", DEFAULT_STATUS_SLOT), DEFAULT_STATUS_SLOT);
    }

    private int getMainInventoryStart() {
        int candidate = sanitizeSlot(
                getConfig().getInt("LAYOUT.MAIN-INVENTORY-START", DEFAULT_MAIN_START),
                DEFAULT_MAIN_START
        );
        return candidate + 26 >= getInventorySize() ? DEFAULT_MAIN_START : candidate;
    }

    private int getHotbarStart() {
        int candidate = sanitizeSlot(
                getConfig().getInt("LAYOUT.HOTBAR-START", DEFAULT_HOTBAR_START),
                DEFAULT_HOTBAR_START
        );
        return candidate + 8 >= getInventorySize() ? DEFAULT_HOTBAR_START : candidate;
    }

    private int sanitizeSlot(int candidate, int fallback) {
        int size = getInventorySize();
        if (candidate < 0 || candidate >= size) {
            return fallback;
        }
        return candidate;
    }

    private ItemStack cloneItem(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return null;
        }
        return item.clone();
    }

    private ItemStack sanitizeForView(ItemStack item) {
        return cloneItem(plugin.getWorthManager().stripWorthDisplay(item));
    }

    private ItemStack sanitizeForTarget(ItemStack item) {
        return cloneItem(plugin.getWorthManager().stripWorthDisplay(item));
    }

    private void deny(InventoryClickEvent event) {
        event.setCancelled(true);
        event.setResult(org.bukkit.event.Event.Result.DENY);
    }

    private void deny(InventoryDragEvent event) {
        event.setCancelled(true);
        event.setResult(org.bukkit.event.Event.Result.DENY);
    }

    private FileConfiguration getConfig() {
        return plugin.getConfigManager().getInvsee();
    }
}
