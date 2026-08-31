package com.bx.ultimateDonutSmp.utils;

import org.bukkit.permissions.Permissible;

/**
 * Decides whether one player is allowed to punish another.
 *
 * <p>The permission that grants a punishment says nothing about who it may be used on, so a rank
 * handed {@code ultimatedonutsmp.staff.punishments.offend} could ban the owner. A protected player
 * carries {@link #EXEMPT_PERMISSION}, and only somebody holding {@link #BYPASS_PERMISSION} can
 * still punish them.
 *
 * <p>Both nodes deliberately sit under {@code ultimatedonutsmp.admin.} rather than beside the other
 * punishment nodes. A server that hands its moderators {@code ultimatedonutsmp.staff.punishments.*}
 * would otherwise make every one of them exempt, which is the opposite of the point.
 */
public final class PunishmentExemptPolicy {

    public static final String EXEMPT_PERMISSION = "ultimatedonutsmp.admin.punishments.exempt";
    public static final String BYPASS_PERMISSION = "ultimatedonutsmp.admin.punishments.exempt.bypass";

    private PunishmentExemptPolicy() {
    }

    public static boolean isExempt(Permissible target) {
        return PermissionUtils.has(target, EXEMPT_PERMISSION);
    }

    /**
     * A null target is an offline player. Their permissions cannot be read without them being on
     * the server, so an offline punishment goes through as it always has.
     */
    public static boolean canPunish(Permissible issuer, Permissible target) {
        if (target == null || !isExempt(target)) {
            return true;
        }
        return PermissionUtils.has(issuer, BYPASS_PERMISSION);
    }
}
