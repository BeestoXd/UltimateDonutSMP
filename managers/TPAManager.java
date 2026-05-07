package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages /tpa and /tpahere requests.
 * tpaHere = false -> requester teleports to target.
 * tpaHere = true -> target teleports to requester.
 */
public class TPAManager {

    private static final long REQUEST_EXPIRY_TICKS = 15 * 60 * 20L;

    public record TpaRequest(UUID requester, UUID target, boolean tpaHere, boolean resumeWhenRequestsEnabled) {}

    private final UltimateDonutSmp plugin;
    private final Map<UUID, TpaRequest> pendingRequests = new ConcurrentHashMap<>();
    private final Map<UUID, Deque<TpaRequest>> autoTpaQueues = new ConcurrentHashMap<>();
    private final Map<UUID, Deque<TpaRequest>> autoTpaHereQueues = new ConcurrentHashMap<>();
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
    }

    public void clearAutoTpaHereQueue(UUID targetUuid) {
        clearAutoTpaHereQueue(targetUuid, "&ctpahere auto-accept&7 was disabled.");
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
        return containsMatchingRequest(autoTpaHereQueues.get(targetUuid), requesterUuid, tpaHere);
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
            plugin.getPaperScheduler().runEntityLater(target, () -> processNextAutoTpa(targetUuid), delayTicks);
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
            plugin.getPaperScheduler().runEntity(target, () -> processNextAutoTpa(targetUuid));
            return;
        }

        pendingRequests.put(targetUuid, request);
        scheduleExpiry(request);

        target.sendMessage(ColorUtils.toComponent(
                "&7Auto-accepting &b/tpa&7 request from &f" + requester.getName() + "&7."
        ));
        requester.sendMessage(ColorUtils.toComponent(
                "&7" + target.getName() + " has &atpa auto-accept&7 enabled. Your &b/tpa&7 request is being processed."
        ));

        plugin.getPaperScheduler().runEntity(target, () -> target.performCommand("tpaccept " + requester.getName()));

        long delayTicks = Math.max(20L, getTpaCooldownTicks() + 20L);
        plugin.getPaperScheduler().runEntityLater(target, () -> processNextAutoTpa(targetUuid), delayTicks);
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
            plugin.getPaperScheduler().runEntityLater(target, () -> processNextAutoTpaHere(targetUuid), delayTicks);
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
            plugin.getPaperScheduler().runEntity(target, () -> processNextAutoTpaHere(targetUuid));
            return;
        }

        pendingRequests.put(targetUuid, request);
        scheduleExpiry(request);

        target.sendMessage(ColorUtils.toComponent(
                "&7Auto-accepting &b/tpahere&7 request from &f" + requester.getName() + "&7."
        ));
        requester.sendMessage(ColorUtils.toComponent(
                "&7" + target.getName() + " has &atpahere auto-accept&7 enabled. Your &b/tpahere&7 request is being processed."
        ));

        plugin.getPaperScheduler().runEntity(target, () -> target.performCommand("tpaccept " + requester.getName()));

        long delayTicks = Math.max(20L, getTpaCooldownTicks() + 20L);
        plugin.getPaperScheduler().runEntityLater(target, () -> processNextAutoTpaHere(targetUuid), delayTicks);
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

    private void notifyRequesterCleared(TpaRequest request, String reason) {
        Player requester = Bukkit.getPlayer(request.requester());
        if (requester == null) {
            return;
        }

        Player target = Bukkit.getPlayer(request.target());
        String targetName = target != null ? target.getName() : "this player";
        String requestType = request.tpaHere() ? "/tpahere" : "/tpa";
        requester.sendMessage(ColorUtils.toComponent(
                "&7Your &b" + requestType + "&7 request to &f" + targetName + "&7 was cleared because " + reason
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

        String command = "tpaccept " + requester.getName();
        String requestType = tpaHere ? "/tpahere" : "/tpa";
        String autoName = tpaHere ? "tpahere auto-accept" : "tpa auto-accept";

        target.sendMessage(ColorUtils.toComponent(
                "&7Auto-accepting pending &b" + requestType + "&7 request from &f" + requester.getName() + "&7."
        ));
        requester.sendMessage(ColorUtils.toComponent(
                "&7" + target.getName() + " has &a" + autoName + "&7 enabled. Your &b" + requestType + "&7 request is being processed."
        ));

        plugin.getPaperScheduler().runEntity(target, () -> target.performCommand(command));
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
            plugin.getPaperScheduler().runEntityLater(target, expire, REQUEST_EXPIRY_TICKS);
        } else {
            plugin.getPaperScheduler().runGlobalLater(expire, REQUEST_EXPIRY_TICKS);
        }
    }

    private long getTpaCooldownTicks() {
        int seconds = plugin.getConfigManager().getConfig().getInt("TELEPORT-COOLDOWN.TPA", 5);
        return seconds * 20L;
    }
}
