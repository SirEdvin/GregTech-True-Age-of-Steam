package site.siredvin.gttruesteam.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class GasPressurizerConfig {

    public static ForgeConfigSpec.DoubleValue percentageIncrease;
    public static ForgeConfigSpec.DoubleValue lowerThreshold;
    public static ForgeConfigSpec.DoubleValue upperThreshold;

    public static void setupConfig(ForgeConfigSpec.Builder builder) {
        builder.push("industrial_gas_pressurizer");
        percentageIncrease = builder
                .defineInRange("percentageIncrease", 0.25, 0, 5);
        lowerThreshold = builder.defineInRange("lowerThreshold", 0.35, 0.01, 1);
        upperThreshold = builder.defineInRange("upperThreshold", 0.85, 0.01, 1);
        builder.pop();
    }
}
