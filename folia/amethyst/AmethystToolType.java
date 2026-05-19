package com.bx.ultimateDonutSmp.amethyst;

public enum AmethystToolType {
    DRILL("ᴀᴍᴇᴛʜʏѕᴛ ᴅʀɪʟʟ"),
    CHOPPER("ᴀᴍᴇᴛʜʏѕᴛ ᴛʀᴇᴇ ᴄʜᴏᴘᴘᴇʀ"),
    SELL_AXE("ᴀᴍᴇᴛʜʏѕᴛ ѕᴇʟʟ ᴀxᴇ"),
    SHOVEL("ᴀᴍᴇᴛʜʏѕᴛ ѕʜᴏᴠᴇʟ"),
    BUCKET("ᴀᴍᴇᴛʜʏѕᴛ ʙᴜᴄᴋᴇᴛ"),
    SHARD_BOOSTER("ѕʜᴀʀᴅ ʙᴏᴏѕᴛᴇʀ");

    private final String displayName;

    AmethystToolType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /** Returns null on unknown name */
    public static AmethystToolType fromString(String name) {
        if (name == null) return null;
        try {
            return valueOf(name.toUpperCase().replace(" ", "_").replace("-", "_"));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /** Config section key for this type */
    public String getConfigKey() {
        return switch (this) {
            case DRILL       -> "DRILL";
            case CHOPPER     -> "CHOPPER";
            case SELL_AXE    -> "SELL-AXE";
            case SHOVEL      -> "SHOVEL";
            case BUCKET      -> "BUCKET";
            case SHARD_BOOSTER -> "SHARD-BOOSTER";
        };
    }
}
