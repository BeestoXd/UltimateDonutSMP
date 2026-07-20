package com.bx.ultimateDonutSmp.api;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import org.jetbrains.annotations.NotNull;

public class UdsExpansion extends EconomyExpansion {

    public UdsExpansion(UltimateDonutSmp plugin) {
        super(plugin);
    }

    @Override
    public @NotNull String getIdentifier() {
        return "uds";
    }
}
