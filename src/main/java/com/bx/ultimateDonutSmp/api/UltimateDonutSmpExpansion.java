package com.bx.ultimateDonutSmp.api;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import org.jetbrains.annotations.NotNull;

public class UltimateDonutSmpExpansion extends EconomyExpansion {

    public UltimateDonutSmpExpansion(UltimateDonutSmp plugin) {
        super(plugin);
    }

    @Override
    public @NotNull String getIdentifier() {
        return "ultimatedonutsmp";
    }
}
