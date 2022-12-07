package com.matyrobbrt.trowels;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.IntValue TROWEL_DURABILITY;
    public static final ForgeConfigSpec.BooleanValue TROWEL_USES_DURABILITY;

    static {
        final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        {
            builder.comment("Configuration regarding to the Trowel").push("trowel");
            TROWEL_USES_DURABILITY = builder.comment("If the trowel should use durability when placing a block.").define("use_durability", false);
            TROWEL_DURABILITY = builder.comment("The durability the trowel has, if 'use_durability' is enabled.").defineInRange("durability", 256, 1, Integer.MAX_VALUE);
            builder.pop();
        }

        SPEC = builder.build();
    }
}
