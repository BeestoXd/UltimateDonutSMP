package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.utils.PermissionUtils;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.PunishmentManager;
import com.bx.ultimateDonutSmp.models.PunishmentRecord;
import com.bx.ultimateDonutSmp.models.PunishmentScope;
import com.bx.ultimateDonutSmp.models.PunishmentType;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PunishmentCommand implements CommandExecutor {

    private static final String CREATE_PERMISSION = "ultimatedonutsmp.staff.punishments.create";
    private static final String REMOVE_PERMISSION = "ultimatedonutsmp.staff.punishments.remove";
    private static final Pattern DURATION_TOKEN = Pattern.compile("(\\d+)([smhdw])", Pattern.CASE_INSENSITIVE);
    private static final Map<String, String> USAGE_MESSAGES = Map.ofEntries(
            Map.entry("ban", "&cᴜѕᴀɢᴇ: /ban <player> [reason]"),
            Map.entry("tempban", "&cᴜѕᴀɢᴇ: /tempban <player> <time> [reason] &7(ᴛɪᴍᴇ: 30ѕ, 15ᴍ, 2ʜ, 5ᴅ, ᴏʀ 5ᴅ 15ᴍ 30ѕ)"),
            Map.entry("mute", "&cᴜѕᴀɢᴇ: /mute <player> [reason]"),
            Map.entry("tempmute", "&cᴜѕᴀɢᴇ: /tempmute <player> <time> [reason] &7(ᴛɪᴍᴇ: 30ѕ, 15ᴍ, 2ʜ, 5ᴅ, ᴏʀ 5ᴅ 15ᴍ 30ѕ)"),
            Map.entry("warn", "&cᴜѕᴀɢᴇ: /warn <player> [reason]"),
            Map.entry("kick", "&cᴜѕᴀɢᴇ: /kick <player> [reason]"),
            Map.entry("blacklist", "&cᴜѕᴀɢᴇ: /blacklist <player> [reason]"),
            Map.entry("unban", "&cᴜѕᴀɢᴇ: /unban <player> [reason]"),
            Map.entry("pardon", "&cᴜѕᴀɢᴇ: /pardon <player> [reason]"),
            Map.entry("unmute", "&cᴜѕᴀɢᴇ: /unmute <player> [reason]"),
            Map.entry("unblacklist", "&cᴜѕᴀɢᴇ: /unblacklist <player> [reason]")
    );

    private final UltimateDonutSmp plugin;

    public PunishmentCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String action = normalizeLabel(label, command);

        return switch (action) {
            case "ban" -> handleCreate(sender, PunishmentType.BAN, args, false, false, action);
            case "tempban" -> handleCreate(sender, PunishmentType.BAN, args, true, false, action);
            case "mute" -> handleCreate(sender, PunishmentType.MUTE, args, false, false, action);
            case "tempmute" -> handleCreate(sender, PunishmentType.MUTE, args, true, false, action);
            case "warn" -> handleCreate(sender, PunishmentType.WARN, args, false, false, action);
            case "kick" -> handleCreate(sender, PunishmentType.KICK, args, false, true, action);
            case "blacklist" -> handleCreate(sender, PunishmentType.BLACKLIST, args, false, false, action);
            case "unban", "pardon" -> handleRemove(sender, PunishmentType.BAN, args, action);
            case "unmute" -> handleRemove(sender, PunishmentType.MUTE, args, action);
            case "unblacklist" -> handleRemove(sender, PunishmentType.BLACKLIST, args, action);
            default -> false;
        };
    }

    private String normalizeLabel(String label, Command command) {
        String normalized = label == null || label.isBlank() ? command.getName() : label;
        int namespaceSeparator = normalized.indexOf(':');
        if (namespaceSeparator >= 0 && namespaceSeparator + 1 < normalized.length()) {
            normalized = normalized.substring(namespaceSeparator + 1);
        }
        return normalized.toLowerCase(Locale.ROOT);
    }

    private boolean handleCreate(CommandSender sender,
                                 PunishmentType type,
                                 String[] args,
                                 boolean temporary,
                                 boolean onlineOnly,
                                 String usageLabel) {
        if (!hasPermission(sender, CREATE_PERMISSION)) {
            send(sender, plugin.getConfigManager().getMessageOrDefault(
                    "PUNISHMENTS.NO-CREATE-PERMISSION",
                    "&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ ᴛᴏ ᴄʀᴇᴀᴛᴇ ᴘᴜɴɪѕʜᴍᴇɴᴛѕ."
            ));
            return true;
        }

        int minimumArgs = temporary ? 2 : 1;
        if (args.length < minimumArgs) {
            sendUsage(sender, usageLabel);
            return true;
        }

        ResolvedTarget target = resolveTarget(args[0]);
        if (target == null || target.uuid() == null) {
            send(sender, plugin.getConfigManager().getMessageOrDefault("PUNISHMENTS.NOT-FOUND", "&cᴘʟᴀʏᴇʀ ɴᴏᴛ ꜰᴏᴜɴᴅ."));
            return true;
        }

        Player onlineTarget = Bukkit.getPlayer(target.uuid());
        if (onlineOnly && onlineTarget == null) {
            send(sender, plugin.getConfigManager().getMessageOrDefault(
                    "PUNISHMENTS.TARGET-OFFLINE",
                    "&cᴛʜᴀᴛ ᴘʟᴀʏᴇʀ ɪѕ ɴᴏᴛ ᴏɴʟɪɴᴇ."
            ));
            return true;
        }

        Long expiresAt = null;
        int reasonStart = 1;
        if (temporary) {
            DurationParseResult duration = parseDuration(args, 1);
            if (duration.millis() <= 0L) {
                send(sender, plugin.getConfigManager().getMessageOrDefault(
                        "PUNISHMENTS.INVALID-DURATION",
                        "&cɪɴᴠᴀʟɪᴅ ᴛɪᴍᴇ. ᴜѕᴇ ᴠᴀʟᴜᴇѕ ʟɪᴋᴇ 30ѕ, 15ᴍ, 2ʜ, 5ᴅ, ᴏʀ ᴄᴏᴍʙɪɴᴇ: 5ᴅ 15ᴍ 30ѕ."
                ));
                return true;
            }
            expiresAt = System.currentTimeMillis() + duration.millis();
            reasonStart = duration.nextArgIndex();
        }

        String reason = joinReason(args, reasonStart);
        Actor actor = resolveActor(sender);
        PunishmentRecord record = plugin.getPunishmentManager().createRecord(new PunishmentManager.PunishmentCreateRequest(
                target.uuid(),
                target.name(),
                type,
                reason,
                actor.uuid(),
                actor.name(),
                System.currentTimeMillis(),
                expiresAt,
                "local",
                PunishmentScope.SERVER
        ));

        if (record == null) {
            send(sender, plugin.getConfigManager().getMessageOrDefault(
                    "PUNISHMENTS.CREATE-FAILED",
                    "&cꜰᴀɪʟᴇᴅ ᴛᴏ ᴄʀᴇᴀᴛᴇ ᴘᴜɴɪѕʜᴍᴇɴᴛ ʀᴇᴄᴏʀᴅ."
            ));
            return true;
        }

        applyRuntimeEffect(type, onlineTarget, record);
        plugin.getDiscordWebhookManager().sendPunishment(record);
        send(sender, plugin.getConfigManager().getMessageOrDefault(
                "PUNISHMENTS.CREATED",
                "&aᴄʀᴇᴀᴛᴇᴅ &f{type} &aᴘᴜɴɪѕʜᴍᴇɴᴛ ꜰᴏʀ &b{player}&a. ɪᴅ: &f#{id}",
                "{type}", plugin.getPunishmentManager().getDisplayType(record),
                "{player}", target.name(),
                "{id}", String.valueOf(record.getId())
        ));
        return true;
    }

    private boolean handleRemove(CommandSender sender, PunishmentType type, String[] args, String label) {
        if (!hasPermission(sender, REMOVE_PERMISSION)) {
            send(sender, plugin.getConfigManager().getMessageOrDefault(
                    "PUNISHMENTS.NO-REMOVE-PERMISSION",
                    "&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ ᴛᴏ ʀᴇᴍᴏᴠᴇ ᴘᴜɴɪѕʜᴍᴇɴᴛѕ."
            ));
            return true;
        }

        if (args.length < 1) {
            sendUsage(sender, label);
            return true;
        }

        ResolvedTarget target = resolveTarget(args[0]);
        if (target == null || target.uuid() == null) {
            send(sender, plugin.getConfigManager().getMessageOrDefault("PUNISHMENTS.NOT-FOUND", "&cᴘʟᴀʏᴇʀ ɴᴏᴛ ꜰᴏᴜɴᴅ."));
            return true;
        }

        String reason = joinReason(args, 1);
        if (reason.equals("ɴᴏ ʀᴇᴀѕᴏɴ ѕᴘᴇᴄɪꜰɪᴇᴅ")) {
            reason = "ʀᴇᴍᴏᴠᴇᴅ ʙʏ ѕᴛᴀꜰꜰ";
        }

        Actor actor = resolveActor(sender);
        boolean removed = plugin.getPunishmentManager().markActiveRecordsRemoved(
                target.uuid(),
                type,
                new PunishmentManager.PunishmentRemovalRequest(
                        actor.uuid(),
                        actor.name(),
                        System.currentTimeMillis(),
                        reason
                )
        );

        if (!removed) {
            send(sender, plugin.getConfigManager().getMessageOrDefault(
                    "PUNISHMENTS.NO-ACTIVE",
                    "&cɴᴏ ᴀᴄᴛɪᴠᴇ {type} ᴘᴜɴɪѕʜᴍᴇɴᴛ ꜰᴏᴜɴᴅ ꜰᴏʀ {player}.",
                    "{type}", type.name(),
                    "{player}", target.name()
            ));
            return true;
        }

        send(sender, plugin.getConfigManager().getMessageOrDefault(
                "PUNISHMENTS.REMOVED",
                "&aʀᴇᴍᴏᴠᴇᴅ ᴀᴄᴛɪᴠᴇ &f{type} &aᴘᴜɴɪѕʜᴍᴇɴᴛ(ѕ) ꜰᴏʀ &b{player}&a.",
                "{type}", type.name(),
                "{player}", target.name()
        ));
        return true;
    }

    private void applyRuntimeEffect(PunishmentType type, Player onlineTarget, PunishmentRecord record) {
        if (onlineTarget == null) {
            return;
        }

        switch (type) {
            case BAN, BLACKLIST, KICK -> onlineTarget.kick(ColorUtils.toComponent(buildPunishmentMessage(record)));
            case WARN -> onlineTarget.sendMessage(ColorUtils.toComponent(
                    plugin.getConfigManager().getMessageOrDefault(
                            "PUNISHMENTS.WARN-RECEIVED",
                            "&cᴡᴀʀɴɪɴɢ: &f{reason}",
                            "{reason}", record.getReason()
                    )
            ));
            case MUTE -> onlineTarget.sendMessage(ColorUtils.toComponent(buildPunishmentMessage(record)));
        }
    }

    private String buildPunishmentMessage(PunishmentRecord record) {
        return plugin.getConfigManager().getMessageOrDefault(
                punishmentMessagePath(record.getType()),
                defaultPunishmentMessage(record.getType()),
                punishmentPlaceholders(record)
        );
    }

    private String punishmentMessagePath(PunishmentType type) {
        return switch (type) {
            case BAN -> "PUNISHMENTS.BAN";
            case KICK -> "PUNISHMENTS.KICK";
            case MUTE -> "PUNISHMENTS.MUTE";
            case BLACKLIST -> "PUNISHMENTS.BLACKLIST";
            case WARN -> "PUNISHMENTS.WARN-RECEIVED";
        };
    }

    private String defaultPunishmentMessage(PunishmentType type) {
        return switch (type) {
            case BAN -> "&c&lʏᴏᴜ ʜᴀᴠᴇ ʙᴇᴇɴ ʙᴀɴɴᴇᴅ!\n&8&m----------------------------\n&7ʀᴇᴀѕᴏɴ: &f%reason%\n&7ᴇxᴘɪʀᴇѕ: &f%nicest_expiration%\n&7ʙᴀɴɴᴇᴅ ʙʏ: &f%issuer%\n&8&m----------------------------\n&7ᴀᴘᴘᴇᴀʟ ᴀᴛ: &fdiscord.example.space";
            case KICK -> "&c&lʏᴏᴜ ʜᴀᴠᴇ ʙᴇᴇɴ ᴋɪᴄᴋᴇᴅ!\n&8&m----------------------------\n&7ʀᴇᴀѕᴏɴ: &f%reason%\n&7ᴋɪᴄᴋᴇᴅ ʙʏ: &f%issuer%\n&8&m----------------------------\n&7ʏᴏᴜ ᴍᴀʏ ʀᴇᴄᴏɴɴᴇᴄᴛ";
            case MUTE -> "&c&lʏᴏᴜ ʜᴀᴠᴇ ʙᴇᴇɴ ᴍᴜᴛᴇᴅ!\n&8&m----------------------------\n&7ʀᴇᴀѕᴏɴ: &f%reason%\n&7ᴇxᴘɪʀᴇѕ: &f%nicest_expiration%\n&7ᴍᴜᴛᴇᴅ ʙʏ: &f%issuer%\n&8&m----------------------------\n&7ʏᴏᴜ ᴄᴀɴɴᴏᴛ ѕᴘᴇᴀᴋ ɪɴ ᴄʜᴀᴛ";
            case BLACKLIST -> "&4&lʏᴏᴜ ʜᴀᴠᴇ ʙᴇᴇɴ ʙʟᴀᴄᴋʟɪѕᴛᴇᴅ!\n&8&m----------------------------\n&7ʀᴇᴀѕᴏɴ: &f%reason%\n&7ʙʟᴀᴄᴋʟɪѕᴛᴇᴅ ʙʏ: &f%issuer%\n&8&m----------------------------\n&4ʏᴏᴜ ᴄᴀɴɴᴏᴛ ᴊᴏɪɴ ᴛʜᴇ ѕᴇʀᴠᴇʀ";
            case WARN -> "&cᴡᴀʀɴɪɴɢ: &f{reason}";
        };
    }

    private String[] punishmentPlaceholders(PunishmentRecord record) {
        String expires = formatExpires(record);
        String issuer = formatIssuer(record);
        return new String[]{
                "%reason%", record.getReason(),
                "%nicest_expiration%", expires,
                "%issuer%", issuer,
                "{reason}", record.getReason(),
                "{expires}", expires,
                "{issuer}", issuer
        };
    }

    private String formatExpires(PunishmentRecord record) {
        if (record.getExpiresAt() == null) {
            return "Never";
        }
        long remainingSeconds = Math.max(0L, (record.getExpiresAt() - System.currentTimeMillis()) / 1000L);
        return NumberUtils.formatCountdown(remainingSeconds);
    }

    private String formatIssuer(PunishmentRecord record) {
        String issuer = record.getIssuerNameSnapshot();
        return issuer == null || issuer.isBlank() ? "ᴜɴᴋɴᴏᴡɴ" : issuer;
    }

    private ResolvedTarget resolveTarget(String input) {
        Player online = Bukkit.getPlayerExact(input);
        if (online == null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().equalsIgnoreCase(input)) {
                    online = player;
                    break;
                }
            }
        }

        if (online != null) {
            return new ResolvedTarget(online.getUniqueId(), online.getName());
        }

        UUID knownUuid = plugin.getPunishmentManager().resolveTargetUuid(input, true).orElse(null);
        if (knownUuid != null) {
            return new ResolvedTarget(knownUuid, plugin.getPunishmentManager().resolveTargetName(knownUuid, input));
        }
        return null;
    }

    private Actor resolveActor(CommandSender sender) {
        if (sender instanceof Player player) {
            return new Actor(player.getUniqueId(), player.getName());
        }
        return new Actor(null, "ᴄᴏɴѕᴏʟᴇ");
    }

    private boolean hasPermission(CommandSender sender, String permission) {
        return !(sender instanceof Player) || PermissionUtils.has(sender, permission);
    }

    private void sendUsage(CommandSender sender, String label) {
        String normalizedLabel = label.toLowerCase(Locale.ROOT);
        String fallback = USAGE_MESSAGES.getOrDefault(normalizedLabel, "&cᴜѕᴀɢᴇ: /" + normalizedLabel + " <player> [reason]");
        send(sender, plugin.getConfigManager().getMessageOrDefault(
                "PUNISHMENTS.USAGE-" + normalizedLabel.toUpperCase(Locale.ROOT),
                fallback
        ));
    }

    private long parseDurationMillis(String input) {
        if (input == null || input.isBlank()) {
            return -1L;
        }

        Matcher matcher = DURATION_TOKEN.matcher(input.trim());
        long totalMillis = 0L;
        int matchedCharacters = 0;
        while (matcher.find()) {
            long amount;
            try {
                amount = Long.parseLong(matcher.group(1));
            } catch (NumberFormatException e) {
                return -1L;
            }

            long multiplier = switch (matcher.group(2).toLowerCase(Locale.ROOT)) {
                case "s" -> 1_000L;
                case "m" -> 60_000L;
                case "h" -> 3_600_000L;
                case "d" -> 86_400_000L;
                case "w" -> 604_800_000L;
                default -> -1L;
            };
            if (multiplier <= 0L) {
                return -1L;
            }

            totalMillis += amount * multiplier;
            matchedCharacters += matcher.group(0).length();
        }

        return matchedCharacters == input.trim().length() ? totalMillis : -1L;
    }

    private DurationParseResult parseDuration(String[] args, int startIndex) {
        long totalMillis = 0L;
        int index = startIndex;
        while (index < args.length) {
            long tokenMillis = parseDurationMillis(args[index]);
            if (tokenMillis <= 0L) {
                break;
            }
            totalMillis += tokenMillis;
            index++;
        }
        return new DurationParseResult(totalMillis, index);
    }

    private String joinReason(String[] args, int startIndex) {
        if (args.length <= startIndex) {
            return "ɴᴏ ʀᴇᴀѕᴏɴ ѕᴘᴇᴄɪꜰɪᴇᴅ";
        }

        StringBuilder builder = new StringBuilder();
        for (int index = startIndex; index < args.length; index++) {
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(args[index]);
        }
        return builder.isEmpty() ? "ɴᴏ ʀᴇᴀѕᴏɴ ѕᴘᴇᴄɪꜰɪᴇᴅ" : builder.toString();
    }

    private void send(CommandSender sender, String message) {
        sender.sendMessage(ColorUtils.toComponent(message));
    }

    private record ResolvedTarget(UUID uuid, String name) {
    }

    private record Actor(UUID uuid, String name) {
    }

    private record DurationParseResult(long millis, int nextArgIndex) {
    }
}
