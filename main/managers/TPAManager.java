package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Manages /tpa and /tpahere requests.
 * tpaHere = false -> requester teleports to target.
 * tpaHere = true -> target teleports to requester.
 */
public class TPAManager {

    private static final long REQUEST_EXPIRY_TICKS = 15 * 60 * 20L;
    private static final long REQUEST_EXPIRY_MILLIS = REQUEST_EXPIRY_TICKS * 50L;

    public record TpaRequest(UUID requester, UUID target, boolean tpaHere, boolean resumeWhenRequestsEnabled) {}
    public record QueuedTpaRequest(UUID requester, UUID target, boolean tpaHere, long queuedAtMillis, long expiresAtMillis) {
        public TpaRequest toRequest() {
            return new TpaRequest(requester, target, tpaHere, false);
        }
    }
    public record TpaQueueEntry(UUID requester, UUID target, boolean tpaHere, int position, long queuedAtMillis, long expiresAtMillis) {}

    private final UltimateDonutSmp plugin;
    private final Map<UUID, TpaRequest> pendingRequests = new ConcurrentHashMap<>();
    private final Map<UUID, Deque<TpaRequest>> autoTpaQueues = new ConcurrentHashMap<>();
    private final Map<UUID, Deque<TpaRequest>> autoTpaHereQueues = new ConcurrentHashMap<>();
    private final Map<UUID, Deque<QueuedTpaRequest>> manualTpaQueues = new ConcurrentHashMap<>();
    private final Map<UUID, Deque<QueuedTpaRequest>> manualTpaHereQueues = new ConcurrentHashMap<>();
    private final Set<UUID> autoTpaWorkers = ConcurrentHashMap.newKeySet();
    private final Set<UUID> autoTpaHereWorkers = ConcurrentHashMap.newKeySet();

    public TPAManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public boolean sendTPA(Player requester, Player target) {
        if (!target.isOnline()) {
            return false;
        }

        return storePendingRequest(new TpaRequest(requester.getUniqueId(), target.getUniqueId(), false, false));
    }

    public boolean sendTPAHere(Player requester, Player target) {
        if (!target.isOnline()) {
            return false;
        }

        return storePendingRequest(new TpaRequest(requester.getUniqueId(), target.getUniqueId(), true, false));
    }

    public int queueAutoTPA(Player requester, Player target, boolean resumeWhenRequestsEnabled) {
        if (!target.isOnline()) {
            return -1;
        }

        UUID targetUuid = target.getUniqueId();
        UUID requesterUuid = requester.getUniqueId();
        if (hasMatchingRequest(requesterUuid, targetUuid, false)) {
            return 0;
        }

        boolean workerActive = autoTpaWorkers.contains(targetUuid);
        Deque<TpaRequest> queue = autoTpaQueues.computeIfAbsent(targetUuid, ignored -> new ArrayDeque<>());
        queue.offerLast(new TpaRequest(requesterUuid, targetUuid, false, resumeWhenRequestsEnabled));
        int position = queue.size() + (workerActive ? 1 : 0);

        if (autoTpaWorkers.add(targetUuid)) {
            processNextAutoTpa(targetUuid);
        }

        return Math.max(position, 1);
    }

    public int queueAutoTPAHere(Player requester, Player target, boolean resumeWhenRequestsEnabled) {
        if (!target.isOnline()) {
            return -1;
        }

        UUID targetUuid = target.getUniqueId();
        UUID requesterUuid = requester.getUniqueId();
        if (hasMatchingRequest(requesterUuid, targetUuid, true)) {
            return 0;
        }

        boolean workerActive = autoTpaHereWorkers.contains(targetUuid);
        Deque<TpaRequest> queue = autoTpaHereQueues.computeIfAbsent(targetUuid, ignored -> new ArrayDeque<>());
        queue.offerLast(new TpaRequest(requesterUuid, targetUuid, true, resumeWhenRequestsEnabled));

        int position = queue.size() + (workerActive ? 1 : 0);
        if (autoTpaHereWorkers.add(targetUuid)) {
            processNextAutoTpaHere(targetUuid);
        }

        return Math.max(position, 1);
    }

    public int queueManualTPA(Player requester, Player target) {
        return queueManualRequest(requester, target, false);
    }

    public int queueManualTPAHere(Player requester, Player target) {
        return queueManualRequest(requester, target, true);
    }

    public void processQueuedAutoRequests(UUID targetUuid) {
        tryAutoAcceptPendingRequest(targetUuid, false);
        tryAutoAcceptPendingRequest(targetUuid, true);
        if (autoTpaWorkers.add(targetUuid)) {
            processNextAutoTpa(targetUuid);
        }
        if (autoTpaHereWorkers.add(targetUuid)) {
            processNextAutoTpaHere(targetUuid);
        }
    }

    public TpaRequest getRequest(UUID targetUuid) {
        return pendingRequests.get(targetUuid);
    }

    public boolean hasRequest(UUID targetUuid) {
        return pendingRequests.containsKey(targetUuid);
    }

    public void removeRequest(UUID targetUuid) {
        pendingRequests.remove(targetUuid);
    }

    public boolean acceptPendingRequest(Player target) {
        if (target == null || !target.isOnline()) {
            return false;
        }

        TpaRequest request = pendingRequests.remove(target.getUniqueId());
        if (request == null) {
            return false;
        }

        return acceptRequest(target, request);
    }

    public boolean acceptQueuedRequest(Player target, UUID requesterUuid, boolean tpaHere) {
        if (target == null || requesterUuid == null || !target.isOnline()) {
            return false;
        }

        UUID targetUuid = target.getUniqueId();
        cleanupExpiredManualQueue(targetUuid, tpaHere);

        Deque<QueuedTpaRequest> queue = manualQueueMap(tpaHere).get(targetUuid);
        if (queue == null || queue.isEmpty()) {
            return false;
        }

        QueuedTpaRequest selected = null;
        for (QueuedTpaRequest request : queue) {
            if (request.requester().equals(requesterUuid)) {
                selected = request;
                break;
            }
        }

        if (selected == null) {
            return false;
        }

        queue.remove(selected);
        if (queue.isEmpty()) {
            manualQueueMap(tpaHere).remove(targetUuid);
        }

        if (isExpired(selected)) {
            return false;
        }

        return acceptRequest(target, selected.toRequest());
    }

    public boolean acceptRandomQueuedRequest(Player target, boolean tpaHere) {
        if (target == null || !target.isOnline()) {
            return false;
        }

        UUID targetUuid = target.getUniqueId();
        cleanupExpiredManualQueue(targetUuid, tpaHere);

        Deque<QueuedTpaRequest> queue = manualQueueMap(tpaHere).get(targetUuid);
        if (queue == null || queue.isEmpty()) {
            return false;
        }

        queue.removeIf(request -> Bukkit.getPlayer(request.requester()) == null);
        if (queue.isEmpty()) {
            manualQueueMap(tpaHere).remove(targetUuid);
            return false;
        }

        List<QueuedTpaRequest> candidates = new ArrayList<>(queue);
        QueuedTpaRequest selected = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
        queue.remove(selected);
        if (queue.isEmpty()) {
            manualQueueMap(tpaHere).remove(targetUuid);
        }

        return acceptRequest(target, selected.toRequest());
    }

    public List<TpaQueueEntry> getQueuedRequests(UUID targetUuid, boolean tpaHere) {
        if (targetUuid == null) {
            return List.of();
        }

        cleanupExpiredManualQueue(targetUuid, tpaHere);
        Deque<QueuedTpaRequest> queue = manualQueueMap(tpaHere).get(targetUuid);
        if (queue == null || queue.isEmpty()) {
            return List.of();
        }

        List<TpaQueueEntry> entries = new ArrayList<>();
        int position = 1;
        for (QueuedTpaRequest request : queue) {
            entries.add(new TpaQueueEntry(
                    request.requester(),
                    request.target(),
                    request.tpaHere(),
                    position++,
                    request.queuedAtMillis(),
                    request.expiresAtMillis()
            ));
        }
        return entries;
    }

    public void cancelRequestsByRequester(UUID requesterUuid) {
        pendingRequests.entrySet().removeIf(entry -> entry.getValue().requester().equals(requesterUuid));
        autoTpaQueues.entrySet().removeIf(entry -> {
            entry.getValue().removeIf(request -> request.requester().equals(requesterUuid));
            return entry.getValue().isEmpty();
        });
        autoTpaHereQueues.entrySet().removeIf(entry -> {
            entry.getValue().removeIf(request -> request.requester().equals(requesterUuid));
            return entry.getValue().isEmpty();
        });
        manualTpaQueues.entrySet().removeIf(entry -> {
            entry.getValue().removeIf(request -> request.requester().equals(requesterUuid));
            return entry.getValue().isEmpty();
        });
        manualTpaHereQueues.entrySet().removeIf(entry -> {
            entry.getValue().removeIf(request -> request.requester().equals(requesterUuid));
            return entry.getValue().isEmpty();
        });
    }

    public void clearQueuedRequestsForTarget(UUID targetUuid) {
        if (targetUuid == null) {
            return;
        }

        autoTpaQueues.remove(targetUuid);
        autoTpaHereQueues.remove(targetUuid);
        manualTpaQueues.remove(targetUuid);
        manualTpaHereQueues.remove(targetUuid);
        autoTpaWorkers.remove(targetUuid);
        autoTpaHereWorkers.remove(targetUuid);
    }

    public void clearAutoTpaHereQueue(UUID targetUuid) {
        clearAutoTpaHereQueue(targetUuid, "&cᴛᴘᴀʜᴇʀᴇ ᴀᴜᴛᴏ-ᴀᴄᴄᴇᴘᴛ&7 ᴡᴀѕ ᴅɪѕᴀʙʟᴇᴅ.");
    }

    public void clearIncomingRequests(UUID targetUuid, boolean tpaHere, String reason) {
        TpaRequest pendingRequest = pendingRequests.get(targetUuid);
        if (pendingRequest != null && pendingRequest.tpaHere() == tpaHere) {
            pendingRequests.remove(targetUuid);
            notifyRequesterCleared(pendingRequest, reason);
        }

        if (tpaHere) {
            clearAutoTpaHereQueue(targetUuid, reason);
        }
        clearManualQueue(targetUuid, tpaHere, reason);
    }

    private int queueManualRequest(Player requester, Player target, boolean tpaHere) {
        if (requester == null || target == null || !target.isOnline()) {
            return -1;
        }

        UUID targetUuid = target.getUniqueId();
        UUID requesterUuid = requester.getUniqueId();
        cleanupExpiredManualQueue(targetUuid, tpaHere);
        if (hasMatchingRequest(requesterUuid, targetUuid, tpaHere)) {
            return 0;
        }

        long now = System.currentTimeMillis();
        QueuedTpaRequest request = new QueuedTpaRequest(
                requesterUuid,
                targetUuid,
                tpaHere,
                now,
                now + REQUEST_EXPIRY_MILLIS
        );

        Deque<QueuedTpaRequest> queue = manualQueueMap(tpaHere).computeIfAbsent(targetUuid, ignored -> new ArrayDeque<>());
        queue.offerLast(request);
        scheduleManualExpiry(request);
        return queue.size();
    }

    private boolean storePendingRequest(TpaRequest request) {
        if (hasMatchingRequest(request.requester(), request.target(), request.tpaHere())) {
            return false;
        }

        pendingRequests.put(request.target(), request);
        scheduleExpiry(request);
        return true;
    }

    private boolean hasMatchingRequest(UUID requesterUuid, UUID targetUuid, boolean tpaHere) {
        TpaRequest pendingRequest = pendingRequests.get(targetUuid);
        if (pendingRequest != null
                && pendingRequest.requester().equals(requesterUuid)
                && pendingRequest.tpaHere() == tpaHere) {
            return true;
        }

        if (containsMatchingRequest(autoTpaQueues.get(targetUuid), requesterUuid, tpaHere)) {
            return true;
        }
        if (containsMatchingRequest(autoTpaHereQueues.get(targetUuid), requesterUuid, tpaHere)) {
            return true;
        }
        if (containsMatchingQueuedRequest(manualTpaQueues.get(targetUuid), requesterUuid, tpaHere)) {
            return true;
        }
        return containsMatchingQueuedRequest(manualTpaHereQueues.get(targetUuid), requesterUuid, tpaHere);
    }

    private boolean containsMatchingRequest(Deque<TpaRequest> queue, UUID requesterUuid, boolean tpaHere) {
        if (queue == null) {
            return false;
        }

        for (TpaRequest request : queue) {
            if (request.requester().equals(requesterUuid) && request.tpaHere() == tpaHere) {
                return true;
            }
        }
        return false;
    }

    private boolean containsMatchingQueuedRequest(Deque<QueuedTpaRequest> queue, UUID requesterUuid, boolean tpaHere) {
        if (queue == null) {
            return false;
        }

        for (QueuedTpaRequest request : queue) {
            if (request.requester().equals(requesterUuid) && request.tpaHere() == tpaHere && !isExpired(request)) {
                return true;
            }
        }
        return false;
    }

    private void processNextAutoTpa(UUID targetUuid) {
        Deque<TpaRequest> queue = autoTpaQueues.get(targetUuid);
        if (queue == null || queue.isEmpty()) {
            cleanupAutoTpa(targetUuid);
            return;
        }

        Player target = Bukkit.getPlayer(targetUuid);
        if (target == null || !target.isOnline()) {
            cleanupAutoTpa(targetUuid);
            return;
        }

        PlayerData targetData = plugin.getPlayerDataManager().get(target);
        TpaRequest nextRequest = queue.peekFirst();
        boolean canResumeFromRequestsToggle = nextRequest != null && nextRequest.resumeWhenRequestsEnabled();
        if (targetData == null || !targetData.isTpaRequestsEnabled()
                || (!targetData.isTpauto() && !canResumeFromRequestsToggle)) {
            autoTpaWorkers.remove(targetUuid);
            return;
        }

        if (pendingRequests.containsKey(targetUuid)) {
            long delayTicks = tryAutoAcceptPendingRequest(targetUuid, false)
                    ? Math.max(20L, getTpaCooldownTicks() + 20L)
                    : 20L;
            plugin.getSpigotScheduler().runEntityLater(target, () -> processNextAutoTpa(targetUuid), delayTicks);
            return;
        }

        TpaRequest request = queue.pollFirst();
        if (queue.isEmpty()) {
            autoTpaQueues.remove(targetUuid);
        }
        if (request == null) {
            cleanupAutoTpa(targetUuid);
            return;
        }

        Player requester = Bukkit.getPlayer(request.requester());
        if (requester == null || !requester.isOnline()) {
            plugin.getSpigotScheduler().runEntity(target, () -> processNextAutoTpa(targetUuid));
            return;
        }

        pendingRequests.put(targetUuid, request);
        scheduleExpiry(request);

        target.sendMessage(ColorUtils.toComponent(
                "&7ᴀᴜᴛᴏ-ᴀᴄᴄᴇᴘᴛɪɴɢ &b/tpa&7 ʀᴇǫᴜᴇѕᴛ ꜰʀᴏᴍ &f" + publicName(requester) + "&7."
        ));
        requester.sendMessage(ColorUtils.toComponent(
                "&7" + publicName(target) + " ʜᴀѕ &aᴛᴘᴀ ᴀᴜᴛᴏ-ᴀᴄᴄᴇᴘᴛ&7 ᴇɴᴀʙʟᴇᴅ. ʏᴏᴜʀ &b/tpa&7 ʀᴇǫᴜᴇѕᴛ ɪѕ ʙᴇɪɴɢ ᴘʀᴏᴄᴇѕѕᴇᴅ."
        ));

        plugin.getSpigotScheduler().runEntity(target, () ->
                target.performCommand("tpaccept " + plainPublicName(requester)));

        long delayTicks = Math.max(20L, getTpaCooldownTicks() + 20L);
        plugin.getSpigotScheduler().runEntityLater(target, () -> processNextAutoTpa(targetUuid), delayTicks);
    }

    private void processNextAutoTpaHere(UUID targetUuid) {
        Deque<TpaRequest> queue = autoTpaHereQueues.get(targetUuid);
        if (queue == null || queue.isEmpty()) {
            cleanupAutoTpaHere(targetUuid);
            return;
        }

        Player target = Bukkit.getPlayer(targetUuid);
        if (target == null || !target.isOnline()) {
            cleanupAutoTpaHere(targetUuid);
            return;
        }

        PlayerData targetData = plugin.getPlayerDataManager().get(target);
        TpaRequest nextRequest = queue.peekFirst();
        boolean canResumeFromRequestsToggle = nextRequest != null && nextRequest.resumeWhenRequestsEnabled();
        if (targetData == null || !targetData.isTpaHereRequestsEnabled()
                || (!targetData.isAutoTpaHereEnabled() && !canResumeFromRequestsToggle)) {
            autoTpaHereWorkers.remove(targetUuid);
            return;
        }

        if (pendingRequests.containsKey(targetUuid)) {
            long delayTicks = tryAutoAcceptPendingRequest(targetUuid, true)
                    ? Math.max(20L, getTpaCooldownTicks() + 20L)
                    : 20L;
            plugin.getSpigotScheduler().runEntityLater(target, () -> processNextAutoTpaHere(targetUuid), delayTicks);
            return;
        }

        TpaRequest request = queue.pollFirst();
        if (queue.isEmpty()) {
            autoTpaHereQueues.remove(targetUuid);
        }
        if (request == null) {
            cleanupAutoTpaHere(targetUuid);
            return;
        }

        Player requester = Bukkit.getPlayer(request.requester());
        if (requester == null || !requester.isOnline()) {
            plugin.getSpigotScheduler().runEntity(target, () -> processNextAutoTpaHere(targetUuid));
            return;
        }

        pendingRequests.put(targetUuid, request);
        scheduleExpiry(request);

        target.sendMessage(ColorUtils.toComponent(
                "&7ᴀᴜᴛᴏ-ᴀᴄᴄᴇᴘᴛɪɴɢ &b/tpahere&7 ʀᴇǫᴜᴇѕᴛ ꜰʀᴏᴍ &f" + publicName(requester) + "&7."
        ));
        requester.sendMessage(ColorUtils.toComponent(
                "&7" + publicName(target) + " ʜᴀѕ &aᴛᴘᴀʜᴇʀᴇ ᴀᴜᴛᴏ-ᴀᴄᴄᴇᴘᴛ&7 ᴇɴᴀʙʟᴇᴅ. ʏᴏᴜʀ &b/tpahere&7 ʀᴇǫᴜᴇѕᴛ ɪѕ ʙᴇɪɴɢ ᴘʀᴏᴄᴇѕѕᴇᴅ."
        ));

        plugin.getSpigotScheduler().runEntity(target, () ->
                target.performCommand("tpaccept " + plainPublicName(requester)));

        long delayTicks = Math.max(20L, getTpaCooldownTicks() + 20L);
        plugin.getSpigotScheduler().runEntityLater(target, () -> processNextAutoTpaHere(targetUuid), delayTicks);
    }

    private void cleanupAutoTpaHere(UUID targetUuid) {
        autoTpaHereQueues.remove(targetUuid);
        autoTpaHereWorkers.remove(targetUuid);
    }

    private void cleanupAutoTpa(UUID targetUuid) {
        autoTpaQueues.remove(targetUuid);
        autoTpaWorkers.remove(targetUuid);
    }

    private void clearAutoTpaHereQueue(UUID targetUuid, String reason) {
        Deque<TpaRequest> queue = autoTpaHereQueues.remove(targetUuid);
        autoTpaHereWorkers.remove(targetUuid);
        if (queue == null || queue.isEmpty()) {
            return;
        }

        for (TpaRequest request : queue) {
            notifyRequesterCleared(request, reason);
        }
    }

    private void clearManualQueue(UUID targetUuid, boolean tpaHere, String reason) {
        Deque<QueuedTpaRequest> queue = manualQueueMap(tpaHere).remove(targetUuid);
        if (queue == null || queue.isEmpty()) {
            return;
        }

        for (QueuedTpaRequest request : queue) {
            notifyRequesterCleared(request.toRequest(), reason);
        }
    }

    private void notifyRequesterCleared(TpaRequest request, String reason) {
        Player requester = Bukkit.getPlayer(request.requester());
        if (requester == null) {
            return;
        }

        Player target = Bukkit.getPlayer(request.target());
        String targetName = target != null ? publicName(target) : "ᴛʜɪѕ ᴘʟᴀʏᴇʀ";
        String requestType = request.tpaHere() ? "/tpahere" : "/tpa";
        requester.sendMessage(ColorUtils.toComponent(
                "&7ʏᴏᴜʀ &b" + requestType + "&7 ʀᴇǫᴜᴇѕᴛ ᴛᴏ &f" + targetName + "&7 ᴡᴀѕ ᴄʟᴇᴀʀᴇᴅ ʙᴇᴄᴀᴜѕᴇ " + reason
        ));
    }

    private boolean tryAutoAcceptPendingRequest(UUID targetUuid, boolean tpaHere) {
        TpaRequest request = pendingRequests.get(targetUuid);
        if (request == null || request.tpaHere() != tpaHere) {
            return false;
        }

        Player target = Bukkit.getPlayer(targetUuid);
        if (target == null || !target.isOnline()) {
            return false;
        }

        Player requester = Bukkit.getPlayer(request.requester());
        if (requester == null || !requester.isOnline()) {
            return false;
        }

        PlayerData targetData = plugin.getPlayerDataManager().get(target);
        if (!canAutoAcceptRequest(targetData, tpaHere)) {
            return false;
        }

        String command = "tpaccept " + plainPublicName(requester);
        String requestType = tpaHere ? "/tpahere" : "/tpa";
        String autoName = tpaHere ? "tpahere auto-accept" : "tpa auto-accept";

        target.sendMessage(ColorUtils.toComponent(
                "&7ᴀᴜᴛᴏ-ᴀᴄᴄᴇᴘᴛɪɴɢ ᴘᴇɴᴅɪɴɢ &b" + requestType + "&7 ʀᴇǫᴜᴇѕᴛ ꜰʀᴏᴍ &f" + publicName(requester) + "&7."
        ));
        requester.sendMessage(ColorUtils.toComponent(
                "&7" + publicName(target) + " ʜᴀѕ &a" + autoName + "&7 ᴇɴᴀʙʟᴇᴅ. ʏᴏᴜʀ &b" + requestType + "&7 ʀᴇǫᴜᴇѕᴛ ɪѕ ʙᴇɪɴɢ ᴘʀᴏᴄᴇѕѕᴇᴅ."
        ));

        plugin.getSpigotScheduler().runEntity(target, () -> target.performCommand(command));
        return true;
    }

    private boolean acceptRequest(Player target, TpaRequest request) {
        if (target == null || request == null || !target.isOnline()
                || !target.getUniqueId().equals(request.target())) {
            return false;
        }

        Player requester = Bukkit.getPlayer(request.requester());
        if (requester == null || !requester.isOnline()) {
            target.sendMessage(ColorUtils.toComponent("&cRequester is no longer online."));
            return false;
        }

        if (request.tpaHere()) {
            plugin.getTeleportManager().queue(target, requester.getLocation(), "TPA", null);
            target.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage("TPA.ACCEPTED-HERE",
                    "{player}", publicName(requester))));
            requester.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage("TPA.YOUR-REQUEST-HERE-ACCEPTED",
                    "{player}", publicName(target))));
        } else {
            plugin.getTeleportManager().queue(requester, target.getLocation(), "TPA", null);
            target.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage("TPA.ACCEPTED",
                    "{player}", publicName(requester))));
            requester.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage("TPA.YOUR-REQUEST-ACCEPTED",
                    "{player}", publicName(target))));
        }

        SoundUtils.play(target, plugin.getConfigManager().getSound("TPA.CONFIRM"));
        SoundUtils.play(requester, plugin.getConfigManager().getSound("TPA.CONFIRM"));
        return true;
    }

    private boolean canAutoAcceptRequest(PlayerData data, boolean tpaHere) {
        if (data == null) {
            return false;
        }

        if (tpaHere) {
            return data.isAutoTpaHereEnabled() && data.isTpaHereRequestsEnabled();
        }
        return data.isTpauto() && data.isTpaRequestsEnabled();
    }

    private void scheduleExpiry(TpaRequest request) {
        Player target = Bukkit.getPlayer(request.target());
        Runnable expire = () -> pendingRequests.computeIfPresent(request.target(), (uuid, current) ->
                current.equals(request) ? null : current
        );
        if (target != null && target.isOnline()) {
            plugin.getSpigotScheduler().runEntityLater(target, expire, REQUEST_EXPIRY_TICKS);
        } else {
            plugin.getSpigotScheduler().runGlobalLater(expire, REQUEST_EXPIRY_TICKS);
        }
    }

    private void scheduleManualExpiry(QueuedTpaRequest request) {
        Player target = Bukkit.getPlayer(request.target());
        Runnable expire = () -> removeQueuedRequest(request);
        if (target != null && target.isOnline()) {
            plugin.getSpigotScheduler().runEntityLater(target, expire, REQUEST_EXPIRY_TICKS);
        } else {
            plugin.getSpigotScheduler().runGlobalLater(expire, REQUEST_EXPIRY_TICKS);
        }
    }

    private void removeQueuedRequest(QueuedTpaRequest request) {
        Deque<QueuedTpaRequest> queue = manualQueueMap(request.tpaHere()).get(request.target());
        if (queue == null) {
            return;
        }

        queue.remove(request);
        if (queue.isEmpty()) {
            manualQueueMap(request.tpaHere()).remove(request.target());
        }
    }

    private void cleanupExpiredManualQueue(UUID targetUuid, boolean tpaHere) {
        Deque<QueuedTpaRequest> queue = manualQueueMap(tpaHere).get(targetUuid);
        if (queue == null) {
            return;
        }

        queue.removeIf(this::isExpired);
        if (queue.isEmpty()) {
            manualQueueMap(tpaHere).remove(targetUuid);
        }
    }

    private boolean isExpired(QueuedTpaRequest request) {
        return request.expiresAtMillis() <= System.currentTimeMillis();
    }

    private Map<UUID, Deque<QueuedTpaRequest>> manualQueueMap(boolean tpaHere) {
        return tpaHere ? manualTpaHereQueues : manualTpaQueues;
    }

    private long getTpaCooldownTicks() {
        int seconds = plugin.getConfigManager().getConfig().getInt("TELEPORT-COOLDOWN.TPA", 5);
        return seconds * 20L;
    }

    private String publicName(Player player) {
        return plugin.getHideManager() == null ? player.getName() : plugin.getHideManager().publicName(player);
    }

    private String plainPublicName(Player player) {
        return plugin.getHideManager() == null ? player.getName() : plugin.getHideManager().plainPublicName(player);
    }
}
