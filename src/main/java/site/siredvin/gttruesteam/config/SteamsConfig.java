package site.siredvin.gttruesteam.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class SteamsConfig {

    public static ForgeConfigSpec.DoubleValue superhotDensity;
    public static ForgeConfigSpec.DoubleValue hellishDensity;

    public static void setupConfig(ForgeConfigSpec.Builder builder) {
        builder.push("steams");
        superhotDensity = builder
                .defineInRange("superhotDensity", 2.1, 0.1, 100);
        hellishDensity = builder
                .defineInRange("hellishDensity", 2.5, 0.1, 100);
        builder.pop();
    }
}
